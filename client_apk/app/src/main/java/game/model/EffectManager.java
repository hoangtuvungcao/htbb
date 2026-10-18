package game.model;

import game.core.j2me.Graphics;
import java.util.Vector;

public class EffectManager extends Vector {
    public static EffectManager lowEffects = new EffectManager();
    public static EffectManager hiEffects = new EffectManager();

    public void updateAll() {
        for (int size = size() - 1; size >= 0; size--) {
            Effect effect2 = (Effect) elementAt(size);
            effect2.update();
            if (effect2.wantDetroy) {
                removeElementAt(size);
            }
        }
    }

    public void paintAll(Graphics graphics) {
        for (int i = 0; i < size(); i++) {
            ((Effect) elementAt(i)).paint(graphics);
        }
    }

    public static void addHiEffect(int i, int i2, int i3) {
        hiEffects.addElement(new Effect(i, i2, i3));
    }

    public static void addHiEffect(Effect effect2) {
        hiEffects.addElement(effect2);
    }

    public static void addLowEffect(int i, int i2, int i3) {
        lowEffects.addElement(new Effect(i, i2, i3));
    }

    public static void addLowEffect(Effect effect2) {
        lowEffects.addElement(effect2);
    }
}
