/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Calendar;
import java.util.Random;
import real.Map;
import real.Potion;

public class EventGame {
    public byte[] value = new byte[3];
    public long[] timeBossAppear = new long[7];
    public byte timeDrop_T = 0;
    public byte maxT = 0;
    public byte nTDay = 10;
    public byte countT = 0;
    public byte[] veso = new byte[100];
    public static byte[] COUNTRY_BOSS_APPEAR = new byte[2];
    public static boolean[] ALL_BOSS_DIE = new boolean[2];
    Potion ptT = null;
    static boolean isCheck = false;

    public EventGame() {
        Random r = new Random(System.currentTimeMillis());
        int country = r.nextInt(2);
        COUNTRY_BOSS_APPEAR[0] = (byte)country;
        COUNTRY_BOSS_APPEAR[1] = (byte)country;
    }

    public static void initCountTryBoss() {
        int country = Map.r.nextInt(2);
        COUNTRY_BOSS_APPEAR[0] = (byte)country;
        COUNTRY_BOSS_APPEAR[1] = (byte)country;
    }

    public static synchronized void checkBossDie() {
        if (ALL_BOSS_DIE[0] && ALL_BOSS_DIE[1]) {
            ALL_BOSS_DIE[0] = false;
            ALL_BOSS_DIE[1] = false;
            initCountTryBoss();
        }
    }

    public String getInfo() {
        String info = String.valueOf(this.value[0]);
        info = info + "," + this.value[1];
        info = info + "," + this.value[2];
        info = info + "," + this.timeBossAppear[0];
        info = info + "," + this.timeBossAppear[1];
        info = info + "," + this.timeBossAppear[2];
        info = info + "," + this.timeBossAppear[3];
        info = info + "," + this.timeBossAppear[4];
        info = info + "," + this.timeBossAppear[5];
        return info + "," + this.timeBossAppear[6];
    }

    public String getInfoT() {
        return this.maxT + "," + this.nTDay + "," + this.countT;
    }

    public synchronized byte getMoneyLuckyBuyBag(int percent) {
        if (this.value[0] < 5 && percent <= 10) {
            return 0;
        } else if (this.value[1] < 15 && percent <= 30) {
            return 1;
        } else {
            return (byte)(percent <= 15 ? 2 : 3);
        }
    }

    public synchronized Potion dropT(Map m) {
        if (this.ptT != null) {
            return null;
        } else if (this.maxT >= 500) {
            return null;
        } else if (this.countT >= this.nTDay) {
            return null;
        } else if (Map.randomMillion() < 1000) {
            this.ptT = new Potion((short)118, 1, m);
            return this.ptT;
        } else {
            return null;
        }
    }

    public void reSetPotionT() {
        this.ptT = null;
    }

    public boolean checkNewDay() {
        Calendar cl = Calendar.getInstance();
        int iHour = cl.get(11);
        if (iHour == 16 && Map.winNumber == -1) {
            Xoso.selectWinNumber();
        }

        if (iHour >= 16 && iHour < 17) {
            Xoso.numberBuy = new byte[100];
            Xoso.min = 9;
            Xoso.max = 0;
        }

        if (iHour >= 0 && iHour < 16) {
            Map.winNumber = -1;
        }

        if (iHour == 0 && !isCheck) {
            isCheck = true;
            this.countT = 0;
            this.nTDay = (byte)(Map.r.nextInt(5) + 4);
            return true;
        } else {
            if (iHour != 0 && isCheck) {
                isCheck = false;
            }

            return false;
        }
    }
}
