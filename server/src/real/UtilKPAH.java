/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class UtilKPAH {
    private static short[] sin = new short[]{
        0,
        18,
        36,
        54,
        71,
        89,
        107,
        125,
        143,
        160,
        178,
        195,
        213,
        230,
        248,
        265,
        282,
        299,
        316,
        333,
        350,
        367,
        384,
        400,
        416,
        433,
        449,
        465,
        481,
        496,
        512,
        527,
        543,
        558,
        573,
        587,
        602,
        616,
        630,
        644,
        658,
        672,
        685,
        698,
        711,
        724,
        737,
        749,
        761,
        773,
        784,
        796,
        807,
        818,
        828,
        839,
        849,
        859,
        868,
        878,
        887,
        896,
        904,
        912,
        920,
        928,
        935,
        943,
        949,
        956,
        962,
        968,
        974,
        979,
        984,
        989,
        994,
        998,
        1002,
        1005,
        1008,
        1011,
        1014,
        1016,
        1018,
        1020,
        1022,
        1023,
        1023,
        1024,
        1024
    };
    private static short[] cos = new short[91];
    private static int[] tan = new int[91];
    static Random rnd;

    static {
        for (int i = 0; i <= 90; i++) {
            cos[i] = sin[90 - i];
            if (cos[i] == 0) {
                tan[i] = Integer.MAX_VALUE;
            } else {
                tan[i] = (sin[i] << 10) / cos[i];
            }
        }

        rnd = new Random();
    }

    public static final int sin(int a) {
        if (a >= 0 && a < 90) {
            return sin[a];
        } else if (a >= 90 && a < 180) {
            return sin[180 - a];
        } else {
            return a >= 180 && a < 270 ? -sin[a - 180] : -sin[360 - a];
        }
    }

    public static final int cos(int a) {
        if (a >= 0 && a < 90) {
            return cos[a];
        } else if (a >= 90 && a < 180) {
            return -cos[180 - a];
        } else {
            return a >= 180 && a < 270 ? -cos[a - 180] : cos[360 - a];
        }
    }

    public static final int tan(int a) {
        if (a >= 0 && a < 90) {
            return tan[a];
        } else if (a >= 90 && a < 180) {
            return -tan[180 - a];
        } else {
            return a >= 180 && a < 270 ? tan[a - 180] : -tan[360 - a];
        }
    }

    public static final int atan(int a) {
        for (int i = 0; i <= 90; i++) {
            if (tan[i] >= a) {
                return i;
            }
        }

        return 0;
    }

    public static final int angle(int dx, int dy) {
        int angle;
        if (dx != 0) {
            int tan = Math.abs((dy << 10) / dx);
            angle = atan(tan);
            if (dy >= 0 && dx < 0) {
                angle = 180 - angle;
            }

            if (dy < 0 && dx < 0) {
                angle += 180;
            }

            if (dy < 0 && dx >= 0) {
                angle = 360 - angle;
            }
        } else {
            angle = dy > 0 ? 90 : 270;
        }

        return angle;
    }

    public static final int fixangle(int angle) {
        if (angle >= 360) {
            angle -= 360;
        }

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public static final int subangle(int a1, int a2) {
        int a = a2 - a1;
        if (a < -180) {
            return a + 360;
        } else {
            return a > 180 ? a - 360 : a;
        }
    }

    public static int distance(int x1, int y1, int x2, int y2) {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static int sqrt(int a) {
        if (a <= 0) {
            return 0;
        } else {
            int x = (a + 1) / 2;

            int x1;
            do {
                x1 = x;
                x = x / 2 + a / (2 * x);
            } while (Math.abs(x1 - x) > 1);

            return x;
        }
    }

    public static int[] getDXDy(int degree, int v) {
        int dx = v * cos(degree) >> 10;
        int dy = v * sin(degree) >> 10;
        return new int[]{dx, dy};
    }

    public static int random(int n) {
        return Math.abs(rnd.nextInt(n));
    }

    public static int getHour() {
        Calendar cl = Calendar.getInstance();
        return cl.get(11);
    }

    public static int getMinute() {
        Calendar cl = Calendar.getInstance();
        return cl.get(12);
    }

    public static int getDayOfMonth() {
        Calendar cl = Calendar.getInstance();
        return cl.get(5);
    }

    public static int getMonth() {
        Calendar cl = Calendar.getInstance();
        return cl.get(2) + 1;
    }

    public static int getSecond() {
        Calendar cl = Calendar.getInstance();
        return cl.get(13);
    }

    public static long getSecondByMili(long time) {
        return (System.currentTimeMillis() - time) / 1000L;
    }

    public static int getRandomMinMax(int min, int max) {
        return min == max ? min : min + Map.r.nextInt(max - min);
    }

    public static boolean isMonday() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Mon");
    }

    public static boolean isTuesday() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Tue");
    }

    public static boolean isWednesay() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Wed");
    }

    public static boolean isThursday() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Thu");
    }

    public static boolean isFriday() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Fri");
    }

    public static boolean isSaturday() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Sat");
    }

    public static boolean isSunday() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Sun");
    }

    public static String getDotPercent(int value) {
        if (value % 100 == 0) {
            return String.valueOf(value / 100);
        } else {
            return value % 10 == 0 ? value / 100 + "." + value % 100 / 10 : value / 100 + "." + value % 100 / 10 + value % 10;
        }
    }

    public static String getDotNumber(long value) {
        String str = String.valueOf(value);
        if (value < 0L) {
            str = str.substring(1, str.length());
        }

        int len = str.length() / 3;
        if (str.length() % 3 == 0) {
            len--;
        }

        for (int i = 0; i < len; i++) {
            int index = str.length() - (i + 1) * 3 - i;
            str = str.substring(0, index) + "." + str.substring(index, str.length());
        }

        return value < 0L ? "-" + str : str;
    }

    public static <T> void sort(List<T> list, UtilKPAH.sortDataTop c) {
        if (!AdminHandler.isStopServer) {
            Object[] a = list.toArray();
            Arrays.sort(a, (java.util.Comparator) c);
            ListIterator i = list.listIterator();

            for (int j = 0; j < a.length; j++) {
                if (AdminHandler.isStopServer) {
                    return;
                }

                CharThiDau dt = (CharThiDau)a[j];
                i.next();
                i.set(dt);
                dt.setRank(c.type, j);
            }
        }
    }

    static class sortDataTop implements Comparator<CharThiDau> {
        public int type = 0;

        public sortDataTop(int type) {
            this.type = type;
        }

        public int compare(CharThiDau o1, CharThiDau o2) {
            if (o1.getPoint(this.type) < o2.getPoint(this.type)) {
                return 1;
            } else if (o1.getPoint(this.type) > o2.getPoint(this.type)) {
                return -1;
            } else if (o1.lasttimeout < o2.lasttimeout) {
                return 1;
            } else {
                return o1.lasttimeout > o2.lasttimeout ? -1 : 0;
            }
        }
    }
}
