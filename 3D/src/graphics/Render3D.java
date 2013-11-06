package graphics;

import display.Game;

/**
 * Created with IntelliJ IDEA.
 * User: Briony
 * Date: 06/11/13
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
public class Render3D extends Render {

    public double[] zBuffer;
    private double renderDistance = 3000;

    public Render3D(int width, int height) {
        super(width, height);
        zBuffer = new double[width * height];
    }


    public void floor(Game game) {
        double floorPosition = 8;
        //if infinite, turns black
        double ceilingPosition = 8;
        double zMove = game.controls.z;
        double xMove = game.controls.x;
        //animates rotation
        double rotation = game.controls.rotation;
        //horizontal rotation
        double cosine = Math.cos(rotation);
        //vertical rotation
        double sine = Math.sin(rotation);

        for (int y = 0; y < height; y++) {
            double ceiling = (y - height / 2.0) / height;

            double z = floorPosition / ceiling;

            if (ceiling < 0) {
                z = ceilingPosition / -ceiling;
            }


            // frame rate reeeeally slow!!


            for (int x = 0; x < width; x++) {
                double depth = (x - width / 2.0) / height;
                depth *= z;
                //horizontal movement
                double xx = depth * cosine + z * sine;
                //vertical movement
                double yy = z * cosine - depth * sine;
                int xPix = (int) (xx + xMove);
                int yPix = (int) (yy + zMove);
                zBuffer[x + y * width] = z;
               // pixels[x + y * width] = ((xPix & 15) * 16) | ((yPix & 15) * 16) << 8;
                pixels[x + y * width] = Texture.floor.pixels[(xPix & 7) + (yPix & 7) * 8];

                //if (z > 100) {
                //    pixels[x + y * width] = 0;
                //}


            }
        }
    }

    //dont know if we need this if we make the world black
    public void renderDistanceLimiter() {
        for (int i = 0; i < width * height; i++) {
            int colour = pixels[i];
            int brightness = (int) (renderDistance / zBuffer[i]);

            //brightness level ranges from 0 to 255
            if (brightness < 0) {
                brightness = 0;
            }

            if(brightness > 255) {
                brightness = 255;
            }

            int r = (colour >> 16) & 0xff;
            int g = (colour >> 8) & 0xff;
            int b = (colour) & 0xff;

            r = r *brightness / 255;
            g = g * brightness /255;
            b = b * brightness / 255;

            pixels[i] = r << 16 | g << 8 | b;
        }
    }

}
