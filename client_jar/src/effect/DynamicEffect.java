package effect;

import game.model.Actor;
import game.model.ImageIcon;
import game.render.Canvas;
import game.render.screen.GameData;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;

public class DynamicEffect extends Actor {
    public static Hashtable ALL_DATA_DYNAMIC_EFFECT = new Hashtable();
    public short id;
    public long timeExist = -1;
    byte index = 0;
    public int dam = 0;
    public byte stop = 0;
    public int power = 2000000000;

    public DynamicEffect(int id) {
        this.id = (short) id;
    }

    public void paint(Graphics g, int x, int y) {
        ImageIcon img;
        DataEffect def = (DataEffect) ALL_DATA_DYNAMIC_EFFECT.get(new StringBuffer(String.valueOf((int) this.id)).toString());
        if (def != null) {
            if (this.id < 301) {
                img = GameData.getImgIcon((short) (this.id + 8700));
            } else {
                img = GameData.getImgIcon((short) (this.id + 14100));
            }
            if (img != null && img.img != null) {
                byte[] sequen = getSequence(this.id);
                def.paint(g, sequen[this.index], x, y, 0, img.img);
            }
        }
    }

    public static void setDataEffect(byte[][] array) {
        for (int i = 0; i < array.length; i++) {
            DataEffect def = new DataEffect(array[i]);
            ALL_DATA_DYNAMIC_EFFECT.put(new StringBuffer(String.valueOf(i)).toString(), def);
        }
    }

    public static byte[] getSequence(int id) {
        DataEffect def = (DataEffect) ALL_DATA_DYNAMIC_EFFECT.get(new StringBuffer(String.valueOf(id)).toString());
        if (def != null) {
            return def.sequence;
        }
        return null;
    }

    public boolean canDestroy() {
        return this.timeExist != -1 && System.currentTimeMillis() - this.timeExist >= 0 && this.index == 0;
    }

    @Override
    public void update() {
        try {
            byte[] sequen = getSequence(this.id);
            if (sequen != null) {
                this.index = (byte) ((this.index + 1) % sequen.length);
                if (this.index >= sequen.length / 2 && this.dam > 0) {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.dam).toString(), 0, this.x + 0, this.y - 15, 1, -2);
                    this.dam = 0;
                }
            }
        } catch (Exception e) {
        }
        if (canDestroy()) {
            this.wantDestroy = true;
        }
    }

    @Override
    public void paint(Graphics g) {
        ImageIcon img;
        DataEffect def = (DataEffect) ALL_DATA_DYNAMIC_EFFECT.get(new StringBuffer(String.valueOf((int) this.id)).toString());
        if (def != null) {
            if (this.id < 301) {
                img = GameData.getImgIcon((short) (this.id + 8700));
            } else {
                img = GameData.getImgIcon((short) (this.id + 14100));
            }
            if (img != null && img.img != null) {
                byte[] sequen = getSequence(this.id);
                def.paint(g, sequen[this.index], this.x, this.y, 0, img.img);
            }
        }
    }

    @Override
    public void setPosTo(short x, short y) {
    }
}
