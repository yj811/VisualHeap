package org.visualheap.world.input;

import java.awt.event.*;

public class InputHandler implements KeyListener, FocusListener, MouseListener, MouseMotionListener {

    public boolean[] key = new boolean[68836];
    public static int mouseX;
    public static int mouseY;

    @Override
    public void focusGained(FocusEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void focusLost(FocusEvent e) {
        //stops when focus not on screen
        for (int i = 0; i < key.length; i++) {
            key[i] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode > 0 && keyCode < key.length) {
            key[keyCode] = true; //keyPressed equals true
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode > 0 && keyCode < key.length) {
            key[keyCode] = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // when mouse enters a particular component
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // mouse exits particular component
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}
