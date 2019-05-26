package info.shibafu528.blurhash;

import org.jetbrains.annotations.NotNull;

public class Blurhash implements BlurhashDecoder {
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
        int pos = 0;
        for (int y = 0; y < height; ++y) {
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

        return pixels;
    }
}
