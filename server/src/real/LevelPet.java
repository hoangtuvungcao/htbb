/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class LevelPet {
    private long exp;
    public int lv;
    public long expRemain;
    public long expRequireUplv;
    public int percent;
    public static long[] expMain = new long[]{
        0L,
        100000L,
        400000L,
        1000000L,
        2000000L,
        3500000L,
        5600000L,
        8400000L,
        12000000L,
        16500000L,
        22000000L,
        28600000L,
        36400000L,
        45500000L,
        56000000L,
        68000000L,
        81600000L,
        96900000L,
        114000000L,
        133000000L,
        154000000L,
        177100000L,
        202400000L,
        230000000L,
        260000000L,
        292500000L,
        327600000L,
        365400000L,
        406000000L,
        449500000L,
        496000000L,
        545600000L,
        598400000L,
        654500000L,
        714000000L,
        777000000L,
        843600000L,
        913900000L,
        988000000L,
        1066000000L,
        1148000000L,
        1234100000L,
        1324400000L,
        1419000000L,
        1518000000L,
        1621500000L,
        1729600000L,
        1842400000L,
        1960000000L,
        2082500000L
    };
    public static long[] expRequire = new long[]{
        0L,
        100000L,
        300000L,
        600000L,
        1000000L,
        1500000L,
        2100000L,
        2800000L,
        3600000L,
        4500000L,
        5500000L,
        6600000L,
        7800000L,
        9100000L,
        10500000L,
        12000000L,
        13600000L,
        15300000L,
        17100000L,
        19000000L,
        21000000L,
        23100000L,
        25300000L,
        27600000L,
        30000000L,
        32500000L,
        35100000L,
        37800000L,
        40600000L,
        43500000L,
        46500000L,
        49600000L,
        52800000L,
        56100000L,
        59500000L,
        63000000L,
        66600000L,
        70300000L,
        74100000L,
        78000000L,
        82000000L,
        86100000L,
        90300000L,
        94600000L,
        99000000L,
        103500000L,
        108100000L,
        112800000L,
        117600000L,
        122500000L
    };

    static {
        long[] expMain = LevelPet.expMain;
        System.err.println("TONG SO LEVEL PET " + expMain.length);
        long[] expRequire = LevelPet.expRequire;
        long exp = 129412500L;
        int lv = 0;
        int percent = 0;

        for (short i = 0; i < expMain.length && exp >= expMain[i]; i++) {
            lv++;
        }

        if (lv >= expMain.length) {
            lv = expMain.length;
            exp = expMain[expMain.length - 1];
            int var9 = 0;
        } else {
            percent = (int)((exp - expMain[lv - 1]) * 1000L / expRequire[lv]);
        }
    }

    public static void initExpTemplate() {
        System.out.println("tong chieu dai: " + expRequire.length + " > " + expMain.length);
        System.out.println("exp len 50: " + expRequire[39] + " > " + expMain[39]);
        long value = expRequire[39];

        for (int i = 50; i < 60; i++) {
            value += value;
            System.out.print(value + "L,");
            if (i % 10 == 0) {
                System.out.println("//" + (i + 1));
            }
        }
    }

    public static long getXpFromLevel(int level) {
        return level <= expMain.length ? expMain[level - 1] : 0L;
    }

    public static long[] getArrXpFromLevel(int level) {
        return level <= expMain.length ? new long[]{expMain[level - 1], expRequire[level]} : new long[2];
    }

    public boolean setExp(long exp) {
        this.lv = 0;
        this.exp = exp;

        for (short i = 0; i < expMain.length && exp >= expMain[i]; i++) {
            this.lv++;
        }

        if (this.lv >= expMain.length) {
            this.lv = expMain.length;
            this.exp = expMain[expMain.length - 1];
            exp = this.exp;
            this.percent = 0;
        } else {
            this.percent = (int)((exp - expMain[this.lv - 1]) * 1000L / expRequire[this.lv]);
        }

        return true;
    }

    public boolean addExpUpLevel(long exp) {
        this.exp = exp;

        for (int i = this.lv; i < expMain.length && exp >= expMain[i]; i++) {
            this.lv++;
            if (this.lv != 1 && this.lv % 10 == 0) {
                this.lv--;
                this.exp = expMain[this.lv] - 1L;
                this.percent = (int)((this.exp - expMain[this.lv - 1]) * 1000L / expRequire[this.lv]);
                return true;
            }
        }

        if (this.lv >= expMain.length) {
            this.lv = expMain.length;
            this.exp = expMain[expMain.length - 1];
            exp = this.exp;
            this.percent = 0;
        } else {
            this.percent = (int)((exp - expMain[this.lv - 1]) * 1000L / expRequire[this.lv]);
        }

        return true;
    }

    public boolean addExpUpLevelKhi(long exp, int level) {
        this.exp = exp;
        this.percent = 0;
        this.lv = level;
        return true;
    }

    public boolean canUplevel() {
        return (this.lv + 1) % 10 == 0 && this.exp == expMain[this.lv] - 1L;
    }

    public boolean canTienHoa(int tienhoa) {
        return tienhoa == 0 && this.lv == 40 ? true : (this.lv + 1) % 10 == 0 && this.exp == expMain[this.lv] - 1L;
    }

    public void setMaxXP(long xp) {
        this.exp = xp;
    }

    public long getExp() {
        return this.exp;
    }

    public void resetExp2Lv(int lv) {
        this.exp = expMain[lv - 1];
        this.setExp(this.exp);
    }

    public long getXPLost() {
        try {
            long exp = this.lv * 1000;
            return exp > 30000L ? 30000L : exp;
        } catch (Exception var3) {
            return 0L;
        }
    }

    public static long getPCExp(int pc, int lv) {
        try {
            return expRequire[lv] * pc / 100L;
        } catch (Exception var4) {
            return 0L;
        }
    }

    public static long getPCExpx10(int pc, int lv) {
        try {
            return expRequire[lv] * pc / 1000L;
        } catch (Exception var4) {
            return 0L;
        }
    }
}
