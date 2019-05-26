package info.shibafu528.blurhash;

import org.jetbrains.annotations.NotNull;

public class Blurhash {
    public static int[] decode(@NotNull String blurhash, int width, int height) {
        return decode(blurhash, width, height, 1);
    }

    public static int[] decode(@NotNull String blurhash, int width, int height, int punch) {
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
                colors[i] = decodeDC(Base83.decode(blurhash.substring(2, 6)));
            } else {
                colors[i] = decodeAC(Base83.decode(blurhash.substring(4 + i * 2, 6 + i * 2)), maximumValue * punch);
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

                pixels[pos] = 255 << 24 | (linearToSRGB(r) & 255) << 16 | (linearToSRGB(g) & 255) << 8 | (linearToSRGB(b) & 255);
                ++pos;
            }
        }

        return pixels;
    }

    /*package*/ static float[] decodeDC(int value) {
        return new float[] {
                sRGBToLinear((value >> 16) & 255),
                sRGBToLinear((value >> 8) & 255),
                sRGBToLinear((value) & 255)
        };
    }

    /*package*/ static float[] decodeAC(int value, float maximumValue) {
        final int quantR = value / (19 * 19);
        final int quantG = (value / 19) % 19;
        final int quantB = value % 19;

        return new float[] {
            signPow2((quantR - 9) / 9f) * maximumValue,
            signPow2((quantG - 9) / 9f) * maximumValue,
            signPow2((quantB - 9) / 9f) * maximumValue
        };
    }

    /*package*/ static float signPow2(float value) {
        return Math.signum(value) * (value * value);
    }

    /*package*/ static float sRGBToLinear(int value) {
        final double v = (double) value / 255;
        if (v <= 0.04045) {
            return (float) (v / 12.92);
        } else {
            return (float) Math.pow((v + 0.055) / 1.055, 2.4);
        }
    }

    /*package*/ static int linearToSRGB(float value) {
        final float v = Math.max(0, Math.min(1, value));
        if (v <= 0.0031308) {
            return (int) Math.round(v * 12.92 * 255 + 0.5);
        } else {
            return (int) Math.round((1.055 * Math.pow(v, 1 / 2.4d) - 0.055) * 255 + 0.5);
        }
    }

}
