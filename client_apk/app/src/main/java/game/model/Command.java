package game.model;

import game.core.j2me.Graphics;

public class Command {
    public IAction action;
    public String caption;
    public int idMap = -1;

    public void paint(Graphics graphics, int i, int i2) {
    }

    public Command(String str, IAction iAction) {
        this.caption = str;
        this.action = iAction;
    }
}
