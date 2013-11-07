package org.visualheap.world.graphics;

import org.visualheap.world.display.Display;

public class Render {
    public int width;
    public int height;
    public int[] pixels;


    public Render(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];
    }

    public void draw(Render render, int xOffset, int yOffset) {
        for (int y = 0; y < render.height; y++) {
            int yPix = y + yOffset;

            if (yPix < 0 || yPix >= Display.HEIGHT) continue;

            for (int x = 0; x < render.width; x++) {
                int xPix = x + xOffset;

                if (xPix < 0 || xPix >= Display.WIDTH) continue;

                int alpha = render.pixels[x + y * render.width];
                // if alpha greater than 0, then render it
                // alpha support allows pixels that don't contain a value i.e. transparency
                if (alpha > 0) {
                    pixels[xPix + yPix * width] = alpha;
                }
            }
        }
    }
}
