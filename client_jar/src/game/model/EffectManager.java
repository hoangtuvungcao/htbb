package game.model;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class EffectManager extends Vector {
    public static EffectManager lowEffects = new EffectManager();
    public static EffectManager hiEffects = new EffectManager();

    public void updateAll() {
        for (int i = size() - 1; i >= 0; i--) {
            Effect e = (Effect) elementAt(i);
            if (e != null) {
                e.update();
                if (e.wantDetroy) {
                    removeElementAt(i);
                }
            }
        }
    }

    public void paintAll(Graphics g) {
        for (int i = 0; i < size(); i++) {
            Effect e = (Effect) elementAt(i);
            if (e != null) {
                e.paint(g);
            }
        }
    }

    public static void addHiEffect(int x, int y, int type) {
        hiEffects.addElement(new Effect(x, y, type));
    }

    public static void addHiEffect(Effect e) {
        hiEffects.addElement(e);
    }

    public static void addLowEffect(int x, int y, int type) {
        lowEffects.addElement(new Effect(x, y, type));
    }

    public static void addLowEffect(Effect ef) {
        lowEffects.addElement(ef);
    }
}
