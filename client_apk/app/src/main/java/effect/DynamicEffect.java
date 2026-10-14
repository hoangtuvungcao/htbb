package effect;

import game.core.j2me.Graphics;
import game.model.Actor;
import game.model.ImageIcon;
import game.render.GCanvas;
import game.render.screen.GameData;
import java.util.Hashtable;

public class DynamicEffect extends Actor {
    public static Hashtable ALL_DATA_DYNAMIC_EFFECT = new Hashtable();
    public short id;
    public long timeExist = -1;
    byte index = 0;
    public byte stop = 0;
    public int power = 2000000000;
    public int dam = 0;

    @Override
    public void setPosTo(short s, short s2) {
    }

    public DynamicEffect(int i) {
        this.id = (short) i;
    }

    public void paint(Graphics graphics, int i, int i2) {
        ImageIcon imgIcon;
        DataEffect dataEffect = (DataEffect) ALL_DATA_DYNAMIC_EFFECT.get(((int) this.id) + "");
        if (dataEffect != null) {
            short s = this.id;
            if (s < 301) {
                imgIcon = GameData.getImgIcon((short) (s + 8700));
            } else {
                imgIcon = GameData.getImgIcon((short) (s + 14100));
            }
            if (imgIcon == null || imgIcon.img == null) {
                return;
            }
            dataEffect.paint(graphics, getSequence(this.id)[this.index], i, i2, 0, imgIcon.img);
        }
    }

    public static void setDataEffect(byte[][] bArr) {
        for (int i = 0; i < bArr.length; i++) {
            ALL_DATA_DYNAMIC_EFFECT.put(i + "", new DataEffect(bArr[i]));
        }
    }

    public static byte[] getSequence(int i) {
        DataEffect dataEffect = (DataEffect) ALL_DATA_DYNAMIC_EFFECT.get(i + "");
        if (dataEffect != null) {
            return dataEffect.sequence;
        }
        return null;
    }

    public boolean canDestroy() {
        return this.timeExist != -1 && System.currentTimeMillis() - this.timeExist >= 0 && this.index == 0;
    }

    @Override
    public void update() {
        try {
            byte[] sequence = getSequence(this.id);
            if (sequence != null) {
                byte length = (byte) ((this.index + 1) % sequence.length);
                this.index = length;
                if (length >= sequence.length / 2 && this.dam > 0) {
                    GCanvas.gameScr.startFlyText("-" + this.dam, 0, this.x, this.y - 15, 1, -2);
                    this.dam = 0;
                }
            }
        } catch (Exception unused) {
        }
        if (canDestroy()) {
            this.wantDestroy = true;
        }
    }

    @Override
    public void paint(Graphics graphics) {
        ImageIcon imgIcon;
        DataEffect dataEffect = (DataEffect) ALL_DATA_DYNAMIC_EFFECT.get(((int) this.id) + "");
        if (dataEffect != null) {
            short s = this.id;
            if (s < 301) {
                imgIcon = GameData.getImgIcon((short) (s + 8700));
            } else {
                imgIcon = GameData.getImgIcon((short) (s + 14100));
            }
            if (imgIcon == null || imgIcon.img == null) {
                return;
            }
            dataEffect.paint(graphics, getSequence(this.id)[this.index], this.x, this.y, 0, imgIcon.img);
        }
    }
}
