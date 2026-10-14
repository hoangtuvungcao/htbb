package game.model;

import game.render.Canvas;

public class AttackResult {
    public int mpLost;
    public int dam;

    public byte effect;
    public int buffEffect = -1;
    public static final byte EFF_NORMAL = 0;
    public static final byte EFF_MISS = 1;
    public static final byte EFF_CRITICAL = 2;
    public static final byte EFF_XUYEN_GIAP = 3;
    public static final byte EFF_BAO_KICH = 4;
    public static final String[] EFF_NAME = {"", "miss", "CHI MANG", "XUYEN GIAP", "BAOKICH"};

    public AttackResult(int dam, byte effect2) {
        this.dam = dam;
        this.effect = effect2;
    }

    public AttackResult() {
    }

    public static void startEff(byte eff, int x, int y) {
        if (eff > 0) {
            Canvas.gameScr.startFlyText(EFF_NAME[eff], 0, x + 0, y, 1, -2);
        }
    }
}
