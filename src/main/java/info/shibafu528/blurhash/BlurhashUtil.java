package info.shibafu528.blurhash;

import java.util.Arrays;

/*package*/ class BlurhashUtil {
    private static final float[] SRGB2LINEAR;

    static {
        SRGB2LINEAR = new float[256];
        for (int i = 0; i < SRGB2LINEAR.length; ++i) {
            double v = (double) i / 255;
            if (v <= 0.04045) {
                SRGB2LINEAR[i] = (float) (v / 12.92);
            } else {
                SRGB2LINEAR[i] = (float) Math.pow((v + 0.055) / 1.055, 2.4);
            }
        }
    }

    /*package*/ static float[] decodeDC(int value) {
        return new float[]{
                sRGBToLinear((value >> 16) & 255),
                sRGBToLinear((value >> 8) & 255),
                sRGBToLinear((value) & 255)
        };
    }

    /*package*/ static float[] decodeAC(int value, float maximumValue) {
        final int quantR = value / (19 * 19);
        final int quantG = (value / 19) % 19;
        final int quantB = value % 19;

        return new float[]{
                signPow2((quantR - 9) / 9f) * maximumValue,
                signPow2((quantG - 9) / 9f) * maximumValue,
                signPow2((quantB - 9) / 9f) * maximumValue
        };
    }

    /*package*/ static float signPow2(float value) {
        return Math.signum(value) * (value * value);
    }

    /*package*/ static float sRGBToLinear(int value) {
        if (value < 0) {
            return SRGB2LINEAR[0];
        } else if (value >= SRGB2LINEAR.length) {
            return SRGB2LINEAR[SRGB2LINEAR.length - 1];
        } else {
            return SRGB2LINEAR[value];
        }
    }

    /*package*/ static int linearToSRGB(float value) {
        int index = Arrays.binarySearch(SRGB2LINEAR, value);
        if (index < 0) {
            index = ~index;
        }
        if (index < 0) {
            return 0;
        } else if (index >= SRGB2LINEAR.length) {
            return SRGB2LINEAR.length - 1;
        } else {
            return index;
        }
    }
}
