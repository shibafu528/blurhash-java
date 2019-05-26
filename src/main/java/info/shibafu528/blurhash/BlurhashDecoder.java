package info.shibafu528.blurhash;

import org.jetbrains.annotations.NotNull;

public interface BlurhashDecoder {
    int[] decode(@NotNull String blurhash, int width, int height);
    int[] decode(@NotNull String blurhash, int width, int height, int punch);
}
