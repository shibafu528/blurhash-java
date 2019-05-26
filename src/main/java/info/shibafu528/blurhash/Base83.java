package info.shibafu528.blurhash;

import org.jetbrains.annotations.NotNull;

/*package*/ class Base83 {
    private static final String CHARCTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#$%*+,-.:;=?@[]^_{|}~";

    public static int decode(@NotNull String str) {
        int val = 0;
        int length = str.length();

        for (int i = 0; i < length; ++i) {
            char c = str.charAt(i);
            if (Character.isHighSurrogate(c)) {
                throw new IllegalArgumentException("Invalid input. index = " + i + ", char = " + c);
            }

            int index = CHARCTERS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid input. index = " + i + ", char = " + c);
            }

            val = val * 83 + index;
        }

        return val;
    }
}
