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

    public Render3D(int width, int height) {
        super(width, height);
    }


    public void floor(Game game) {
        double floorPosition = 8;
        //if infinite, turns black
        double ceilingPosition = 8;
        double forward = game.time/5.0;
        double right = game.time/ 5.0;
        //animates rotation
        double rotation = game.time / 100.0;
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
                int xPix = (int) (xx);
                int yPix = (int) (yy);
                pixels[x + y * width] = ((xPix & 15) * 16) | ((yPix & 15) * 16) << 8;
            }
        }
    }

}
