package info.shibafu528.blurhash;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("usage: decode blurhash width height dest");
            System.exit(1);
        }

        String mode = args[0];

        switch (mode) {
            case "decode":
                new App().decode(args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]), args[4]);
                break;
        }
    }

    public void decode(String blurhash, int width, int height, String dest) {
        int[] pixels = Blurhash.decode(blurhash, width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        try {
            ImageIO.write(image, "png", new File(dest));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
