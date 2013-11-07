package org.visualheap.world.graphics;

import org.visualheap.world.display.Game;

public class Render3D extends Render {

    public double[] zValues;

    public Render3D(int width, int height) {
        super(width, height);
        zValues = new double[width * height];
    }

    public void scene(Game game) {
        double floorPosition = 8;
        double ceilingPosition = 800;  //greater than reference limiter
        double zMove = game.controls.z;
        double xMove = game.controls.x;
        double rotation = game.controls.rotation;
        double hRotation = Math.cos(rotation);
        double vRotation = Math.sin(rotation);

        for (int y = 0; y < height; y++) {
            double ceiling = (y - height / 3.0) / height;

            double z = floorPosition / ceiling;

            if (ceiling < 0) {
                z = ceilingPosition / -ceiling;
            }

            for (int x = 0; x < width; x++) {
                double depth = (x - width / 2.0) / height;
                depth *= z;

                double hMovement = depth * hRotation + z * vRotation;
                double vMovement = z * hRotation - depth * vRotation;
                int xPix = (int) (hMovement + xMove);
                int yPix = (int) (vMovement + zMove);
                zValues[x + y * width] = z;
                pixels[x + y * width] = Texture.floor.pixels[(xPix & 7) + (yPix & 7) * 8];
            }
        }
    }

    public void renderDistanceLimiter() {
        for (int i = 0; i < width * height; i++) {
            int colour = pixels[i];
            double renderDistance = 3000;
            int brightness = (int) (renderDistance / zValues[i]);

            //brightness level ranges from 0 to 255
            if (brightness < 0) {
                brightness = 0;
            }

            if (brightness > 255) {
                brightness = 255;
            }

            int r = (colour >> 16) & 0xff;
            int g = (colour >> 8) & 0xff;
            int b = (colour) & 0xff;

            r = r * brightness / 255;
            g = g * brightness / 255;
            b = b * brightness / 255;

            pixels[i] = r << 16 | g << 8 | b;
        }
    }
}
