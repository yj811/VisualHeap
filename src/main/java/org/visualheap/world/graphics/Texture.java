package org.visualheap.world.graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Texture {
    public static Render floor = loadBitmap("texture.png");

    public static Render loadBitmap(String filename) {
        try {
            BufferedImage img = ImageIO.read(Texture.class.getResource(filename));
            int width = img.getWidth();
            int height = img.getHeight();
            Render result = new Render(width, height);
            img.getRGB(0, 0, width, height, result.pixels, 0, width);
            return result;
        } catch (Exception e) {
            System.out.println("Attempted to load incorrect texture");
            throw new RuntimeException(e);
        }
    }
}
