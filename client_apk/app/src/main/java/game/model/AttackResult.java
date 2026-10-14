package game.model;

import game.render.GCanvas;

public class AttackResult {
    public static final byte EFF_BAO_KICH = 4;
    public static final byte EFF_CRITICAL = 2;
    public static final byte EFF_MISS = 1;
    public static final String[] EFF_NAME = {"", "miss", "CHI MANG", "XUYEN GIAP", "BAOKICH"};
    public static final byte EFF_NORMAL = 0;
    public static final byte EFF_XUYEN_GIAP = 3;
    public int buffEffect = -1;
    public int dam;

    public byte effect;
    public int mpLost;

    public AttackResult(int i, byte b) {
        this.dam = i;
        this.effect = b;
    }

    public AttackResult() {
    }

    public static void startEff(byte b, int i, int i2) {
        if (b > 0) {
            GCanvas.gameScr.startFlyText(EFF_NAME[b], 0, i, i2, 1, -2);
        }
    }
}
