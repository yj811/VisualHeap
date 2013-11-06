package display;

import input.Controller;

import java.awt.event.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Briony
 * Date: 05/11/13
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */
public class Game {
    public int time;
    public Controller controls;

    public Game() {
     controls = new Controller();
    }

    public void tick(boolean[] key) {
        time++;
        boolean forward = key[KeyEvent.VK_W]; // W key on keyboard
        boolean back = key[KeyEvent.VK_S];
        boolean left = key[KeyEvent.VK_A];
        boolean right = key[KeyEvent.VK_D];
        boolean turnLeft = key[KeyEvent.VK_LEFT];
        boolean turnRight = key[KeyEvent.VK_RIGHT];

        controls.tick(forward, back, left, right, turnLeft, turnRight);
    }
}
