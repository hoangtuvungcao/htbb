package game.model;

import game.render.Res;
import java.util.Vector;

public class ChatTab {
    public Command center;
    public boolean isInput;
    public String name;
    public Command right;
    public Vector text;
    public boolean isOpen = true;
    public int count = Res.rnd(10);

    public ChatTab(String str, Command command, Command command2, boolean z) {
        this.name = str;
        this.center = command;
        this.right = command2;
        this.isInput = z;
    }
}
