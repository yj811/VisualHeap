package org.visualheap.world.graphics;

import java.util.Random;

import org.visualheap.world.display.Game;

/**
 * Created with IntelliJ IDEA.
 * User: Briony
 * Date: 05/11/13
 * Time: 19:47
 * To change this template use File | Settings | File Templates.
 */
public class Screen extends Render {

    private Render test;
    private Render3D render;

    public Screen(int width, int height) {
        super(width, height);

        Random random = new Random();

        render = new Render3D(width, height);

        test = new Render(256, 256);
        for (int i = 0; i < 256 * 256; i++) {
            test.pixels[i] = random.nextInt() * (random.nextInt(5) / 4);
        }
    }

    public void render(Game game) {
        for (int i = 0; i < width * height; i++) {
            //sets pixels to zero every frame
            pixels[i] = 0;
        }

        render.floor(game);
        render.renderDistanceLimiter();
        draw(render, 0, 0);
    }
}
