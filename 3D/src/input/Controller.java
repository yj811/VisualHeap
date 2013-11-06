package input;

/**
 * Created with IntelliJ IDEA.
 * User: Briony
 * Date: 06/11/13
 * Time: 20:49
 * To change this template use File | Settings | File Templates.
 */
public class Controller {
    public double x, z; // x is left and right movement. z is forward and backward movement
    public double rotation;
    double xa, za, rotationa;
    public static boolean mouseTurnLeft = false;
    public static boolean mouseTurnRight = false;

    public void tick(boolean forward, boolean back, boolean left, boolean right, boolean turnLeft, boolean turnRight) {
        double rotationSpeed = 0.025;
        double walkSpeed = 1;
        double xMove = 0;
        double zMove = 0;

        if (forward) {
            zMove++;
        }

        if (back) {
            zMove--;
        }

        if (left) {
            xMove--;
        }

        if (right) {
            xMove++;
        }

        if (turnLeft || mouseTurnLeft) {
            rotationa -= rotationSpeed;

        }

        if (turnRight || mouseTurnRight) {
            rotationa += rotationSpeed;
        }

        xa += (xMove * Math.cos(rotation) + zMove * Math.sin(rotation)) * walkSpeed;
        za += (zMove * Math.cos(rotation) + xMove * Math.sin(rotation)) * walkSpeed;

        x += xa;
        z += za;
        xa *= 0.1;
        za *= 0.1;
        rotation += rotationa;
        rotationa *= 0.5;
    }
}
