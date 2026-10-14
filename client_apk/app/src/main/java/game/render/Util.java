package game.render;

import game.core.j2me.Graphics;
import game.model.Actor;
import java.util.Vector;

public class Util {
    static String[][] st;
    private static short[] sin = {0, 18, 36, 54, 71, 89, 107, 125, 143, 160, 178, 195, 213, 230, 248, 265, 282, 299, 316, 333, 350, 367, 384, 400, 416, 433, 449, 465, 481, 496, 512, 527, 543, 558, 573, 587, 602, 616, 630, 644, 658, 672, 685, 698, 711, 724, 737, 749, 761, 773, 784, 796, 807, 818, 828, 839, 849, 859, 868, 878, 887, 896, 904, 912, 920, 928, 935, 943, 949, 956, 962, 968, 974, 979, 984, 989, 994, 998, 1002, 1005, 1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024, 1024};
    private static short[] cos = new short[91];
    private static int[] tan = new int[91];

    public static int abs(int i) {
        return i > 0 ? i : -i;
    }

    public static final int fixangle(int i) {
        if (i >= 360) {
            i -= 360;
        }
        return i < 0 ? i + 360 : i;
    }

    public static final int subangle(int i, int i2) {
        int i3 = i2 - i;
        if (i3 < -180) {
            return i3 + 360;
        }
        return i3 > 180 ? i3 - 360 : i3;
    }

    static {
        for (int i = 0; i <= 90; i++) {
            short[] sArr = cos;
            short[] sArr2 = sin;
            short s = sArr2[90 - i];
            sArr[i] = s;
            if (s == 0) {
                tan[i] = Integer.MAX_VALUE;
            } else {
                tan[i] = (sArr2[i] << 10) / s;
            }
        }
        st = new String[][]{new String[]{"áàảãạăắằẳẵặâấầẩẫậ", "éèẻẽẹêếềểễệ", "íìỉĩị", "óòỏõọôốồổỗộ", "ơớờởỡợ", "úùủũụưứừửữự", "ýỳỷỹỵ"}, new String[]{"a", "e", "i", "o", "o", "u", "y"}};
    }

    public static final int sin(int i) {
        if (i >= 0 && i < 90) {
            return sin[i];
        }
        if (i >= 90 && i < 180) {
            return sin[180 - i];
        }
        if (i >= 180 && i < 270) {
            return -sin[i - 180];
        }
        return -sin[360 - i];
    }

    public static final int cos(int i) {
        if (i >= 0 && i < 90) {
            return cos[i];
        }
        if (i >= 90 && i < 180) {
            return -cos[180 - i];
        }
        if (i >= 180 && i < 270) {
            return -cos[i - 180];
        }
        return cos[360 - i];
    }

    public static final int tan(int i) {
        if (i >= 0 && i < 90) {
            return tan[i];
        }
        if (i >= 90 && i < 180) {
            return -tan[180 - i];
        }
        if (i >= 180 && i < 270) {
            return tan[i - 180];
        }
        return -tan[360 - i];
    }

    public static final int atan(int i) {
        for (int i2 = 0; i2 <= 90; i2++) {
            if (tan[i2] >= i) {
                return i2;
            }
        }
        return 0;
    }

    public static final int angle(int i, int i2) {
        if (i == 0) {
            return i2 > 0 ? 90 : 270;
        }
        int iAtan = atan(Math.abs((i2 << 10) / i));
        if (i2 >= 0 && i < 0) {
            iAtan = 180 - iAtan;
        }
        if (i2 < 0 && i < 0) {
            iAtan += 180;
        }
        return (i2 >= 0 || i < 0) ? iAtan : 360 - iAtan;
    }

    public static int distance(int i, int i2, int i3, int i4) {
        int i5 = i - i3;
        int i6 = i2 - i4;
        return sqrt((i5 * i5) + (i6 * i6));
    }

    public static int sqrt(int i) {
        if (i <= 0) {
            return 0;
        }
        int i2 = (i + 1) / 2;
        while (true) {
            int i3 = (i2 / 2) + (i / (i2 * 2));
            if (Math.abs(i2 - i3) <= 1) {
                return i3;
            }
            i2 = i3;
        }
    }

    public static boolean inDataRange(Actor actor, Actor actor2) {
        return Math.abs(actor.x - actor2.x) < (GCanvas.w / 2) + 100 && Math.abs(actor.y - actor2.y) < (GCanvas.w / 2) + 100;
    }

    public static void quickSort(Vector vector) {
        recQuickSort(vector, 0, vector.size() - 1);
    }

    private static void recQuickSort(Vector vector, int i, int i2) {
        if (i2 - i <= 0) {
            return;
        }
        try {
            int iPartitionIt = partitionIt(vector, i, i2, ((Actor) vector.elementAt(i2)).getY());
            recQuickSort(vector, i, iPartitionIt - 1);
            recQuickSort(vector, iPartitionIt + 1, i2);
        } catch (Exception unused) {
        }
    }

    private static int partitionIt(Vector vector, int i, int i2, int i3) {
        int i4 = i - 1;
        int i5 = i2;
        while (true) {
            i4++;
            try {
                if (((Actor) vector.elementAt(i4)).getY() >= i3) {
                    while (i5 > 0) {
                        i5--;
                        if (((Actor) vector.elementAt(i5)).getY() <= i3) {
                            break;
                        }
                    }
                    if (i4 >= i5) {
                        break;
                    }
                    swap(vector, i4, i5);
                }
            } catch (Exception unused) {
            }
        }
        swap(vector, i4, i2);
        return i4;
    }

    private static void swap(Vector vector, int i, int i2) {
        Object objElementAt = vector.elementAt(i2);
        if (((Actor) vector.elementAt(i2)).getY() != ((Actor) vector.elementAt(i)).getY()) {
            vector.setElementAt(vector.elementAt(i), i2);
            vector.setElementAt(objElementAt, i);
        }
    }

    public static short findDirection(Actor actor, Actor actor2) {
        try {
            if (abs(actor.x - actor2.x) < abs(actor.y - actor2.y)) {
                return actor2.y > actor.y ? (short) 0 : (short) 1;
            }
            return actor2.x < actor.x ? (short) 2 : (short) 3;
        } catch (Exception unused) {
            return (short) 0;
        }
    }

    static int pos(char c) {
        int i = 0;
        while (true) {
            String[] strArr = st[0];
            if (i >= strArr.length) {
                return -1;
            }
            if (strArr[i].indexOf(c + "") != -1) {
                return i;
            }
            i++;
        }
    }

    public static String converUTF2NoUTF(String str) {
        String str2 = "";
        for (int i = 0; i < str.length(); i++) {
            int iPos = pos(str.charAt(i));
            if (iPos != -1) {
                str2 = str2 + st[1][iPos];
            } else {
                str2 = str2 + str.charAt(i);
            }
        }
        return str2;
    }

    public static String[] split(String str, String str2) {
        int i;
        Vector vector = new Vector();
        int iIndexOf = str.indexOf(str2);
        while (true) {
            if (iIndexOf < 0) {
                break;
            }
            vector.addElement(str.substring(0, iIndexOf));
            str = str.substring(iIndexOf + str2.length());
            iIndexOf = str.indexOf(str2);
        }
        vector.addElement(str);
        int size = vector.size();
        String[] strArr = new String[size];
        if (size > 0) {
            for (i = 0; i < size; i++) {
                strArr[i] = (String) vector.elementAt(i);
            }
        }
        return strArr;
    }

    public static void paintColor(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13) {
        int i14 = 0;
        while (i14 < i) {
            int i15 = i14 + 1;
            graphics.setColor((((255 - i2) * i15) / i) + i2, (((255 - i3) * i15) / (i / i9)) + i3, (((255 - i4) * i15) / (i / i10)) + i4);
            if (i11 == 0) {
                graphics.fillRect(i5, i6 - i14, i7, 1);
            } else if (i11 == 1) {
                graphics.fillRect(i5, i14 + i6, i7, 1);
            } else if (i11 == 2) {
                graphics.fillRect(i14 + i5, i6, 1, i7);
            } else if (i11 == 3) {
                graphics.fillRect(i5 - i14, i6, 1, i7);
            } else if (i11 == 4) {
                int i16 = i7 - (i14 * 2);
                graphics.fillArc(i5 + i14, i6 + i14, i16, i16, i12, i13);
            }
            i14 = i15;
        }
    }

    public static void paintColumColor(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13) {
        if (i11 == 0) {
            paintColor(graphics, i3, i4, i5, i6, i, i2, i7, i8, i9, i10, 0, 0, 0);
            paintColor(graphics, i3, i4, i5, i6, i, (i2 - (i3 * 2)) + 1, i7, i8, i9, i10, 1, 0, 0);
        } else if (i11 == 1) {
            paintColor(graphics, i3, i4, i5, i6, i, i2, i7, i8, i9, i10, 2, 0, 0);
            paintColor(graphics, i3, i4, i5, i6, (i + (i3 * 2)) - 1, i2, i7, i8, i9, i10, 3, 0, 0);
        } else if (i11 == 2) {
            paintEcliseColor(graphics, i, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13);
        }
    }

    public static void paintEcliseColor(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13) {
        paintColor(graphics, i3, i4, i5, i6, i, i2, i7, 1, 1, 1, 4, i12, i13);
    }

    public static String converSecon2hours(int i) {
        int i2 = i % 60;
        int i3 = i / 60;
        return (i3 / 60) + ":" + (i3 % 60);
    }

    public static int getDistance(int i, int i2, int i3, int i4) {
        return Res.getRange(i, i3, i2, i4);
    }
}
