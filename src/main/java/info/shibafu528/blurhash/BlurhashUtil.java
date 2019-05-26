package info.shibafu528.blurhash;

/*package*/ class BlurhashUtil {
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
