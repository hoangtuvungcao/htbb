package game.model;

import javax.microedition.lcdui.Graphics;

public class Command {
    public String caption;
    public IAction action;
    public int idMap = -1;

    public Command(String caption, IAction action) {
        this.caption = caption;
        this.action = action;
    }

    public void paint(Graphics g, int x, int y) {
    }
}
