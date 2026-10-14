package game.model;

import game.render.Res;
import java.util.Vector;

public class ChatTab {
    public Command center;
    public Command right;
    public String name;
    public Vector text;
    public boolean isInput;
    public boolean isOpen = true;
    public int count = Res.rnd(10);

    public ChatTab(String name, Command cen, Command right, boolean isInput) {
        this.name = name;
        this.center = cen;
        this.right = right;
        this.isInput = isInput;
    }
}
