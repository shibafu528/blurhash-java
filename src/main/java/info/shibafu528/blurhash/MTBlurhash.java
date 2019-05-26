package info.shibafu528.blurhash;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MTBlurhash implements BlurhashDecoder {
    private final ExecutorService executor;
    private final int threads;

    public MTBlurhash(ExecutorService executor, int threads) {
        this.executor = executor;
        this.threads = threads;
        if (threads < 1) {
            throw new IllegalArgumentException("Too small threads.");
        }
    }

    public int[] decode(@NotNull String blurhash, int width, int height) {
        return decode(blurhash, width, height, 1);
    }

    public int[] decode(@NotNull String blurhash, int width, int height, int punch) {
        if (blurhash.length() < 6) {
            throw new IllegalArgumentException("Too short blurhash.");
        }

        final int sizeFlag = Base83.decode(blurhash.substring(0, 1));
        final int numY = (sizeFlag / 9) + 1;
        final int numX = (sizeFlag % 9) + 1;

        final int quantisedMaximumValue = Base83.decode(blurhash.substring(1, 2));
        final float maximumValue = (quantisedMaximumValue + 1) / 166f;

        int expectedLength = 4 + 2 * numX * numY;
        if (blurhash.length() != expectedLength) {
            throw new IllegalArgumentException("blurhash length mismatch. expect = " + expectedLength + ", actual = " + blurhash.length());
        }

        final float[][] colors = new float[numX * numY][];
        for (int i = 0; i < colors.length; ++i) {
            if (i == 0) {
                colors[i] = BlurhashUtil.decodeDC(Base83.decode(blurhash.substring(2, 6)));
            } else {
                colors[i] = BlurhashUtil.decodeAC(Base83.decode(blurhash.substring(4 + i * 2, 6 + i * 2)), maximumValue * punch);
            }
        }

        final int[] pixels = new int[width * height];

        final int perThread = height / threads;
        final Future<?>[] futures = new Future[threads];
        for (int i = 0; i < threads; ++i) {
            futures[i] = executor.submit(new ParallelBlur(colors, pixels, width, height, i * perThread, (i + 1) * perThread, numX, numY));
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return pixels;
    }

    private static class ParallelBlur implements Runnable {
        private final float[][] colors;
        private final int[] pixels;
        private final int width;
        private final int height;
        private final int startY;
        private final int endYExclusive;
        private final int numX;
        private final int numY;

        private ParallelBlur(float[][] colors, int[] pixels, int width, int height, int startY, int endYExclusive, int numX, int numY) {
            this.colors = colors;
            this.pixels = pixels;
            this.width = width;
            this.height = height;
            this.startY = startY;
            this.endYExclusive = endYExclusive;
            this.numX = numX;
            this.numY = numY;
        }

        @Override
        public void run() {
            int pos = startY * width;
            for (int y = startY; y < height && y < endYExclusive; ++y) {
                for (int x = 0; x < width; ++x) {
                    float r = 0;
                    float g = 0;
                    float b = 0;

                    for (int j = 0; j < numY; j++) {
                        for (int i = 0; i < numX; i++) {
                            float basis = (float) (Math.cos(Math.PI * x * i / width) * Math.cos(Math.PI * y * j / height));
                            float[] color = colors[i + j * numX];
                            r += color[0] * basis;
                            g += color[1] * basis;
                            b += color[2] * basis;
                        }
                    }

                    pixels[pos] = 255 << 24 | (BlurhashUtil.linearToSRGB(r) & 255) << 16 | (BlurhashUtil.linearToSRGB(g) & 255) << 8 | (BlurhashUtil.linearToSRGB(b) & 255);
                    ++pos;
                }
            }
        }
    }
}
