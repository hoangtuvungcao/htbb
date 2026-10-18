package game.render;

import game.model.Actor;
import game.model.Cout;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Util {
    private static short[] sin = {0, 18, 36, 54, 71, 89, 107, 125, 143, 160, 178, 195, 213, 230, 248, 265, 282, 299, 316, 333, 350, 367, 384, 400, 416, 433, 449, 465, 481, 496, 512, 527, 543, 558, 573, 587, 602, 616, 630, 644, 658, 672, 685, 698, 711, 724, 737, 749, 761, 773, 784, 796, 807, 818, 828, 839, 849, 859, 868, 878, 887, 896, 904, 912, 920, 928, 935, 943, 949, 956, 962, 968, 974, 979, 984, 989, 994, 998, 1002, 1005, 1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024, 1024};
    private static short[] cos = new short[91];
    private static int[] tan = new int[91];
    static String[][] st;

    static {
        for (int i = 0; i <= 90; i++) {
            cos[i] = sin[90 - i];
            if (cos[i] == 0) {
                tan[i] = Integer.MAX_VALUE;
            } else {
                tan[i] = (sin[i] << 10) / cos[i];
            }
        }
        st = new String[][]{new String[]{"áàảãạăắằẳẵặâấầẩẫậ", "éèẻẽẹêếềểễệ", "íìỉĩị", "óòỏõọôốồổỗộ", "ơớờởỡợ", "úùủũụưứừửữự", "ýỳỷỹỵ"}, new String[]{"a", "e", "i", "o", "o", "u", "y"}};
    }

    public static final int sin(int a) {
        if (a >= 0 && a < 90) {
            return sin[a];
        }
        if (a >= 90 && a < 180) {
            return sin[180 - a];
        }
        if (a >= 180 && a < 270) {
            return -sin[a - 180];
        }
        return -sin[360 - a];
    }

    public static final int cos(int a) {
        if (a >= 0 && a < 90) {
            return cos[a];
        }
        if (a >= 90 && a < 180) {
            return -cos[180 - a];
        }
        if (a >= 180 && a < 270) {
            return -cos[a - 180];
        }
        return cos[360 - a];
    }

    public static final int tan(int a) {
        if (a >= 0 && a < 90) {
            return tan[a];
        }
        if (a >= 90 && a < 180) {
            return -tan[180 - a];
        }
        if (a >= 180 && a < 270) {
            return tan[a - 180];
        }
        return -tan[360 - a];
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
            int tan2 = Math.abs((dy << 10) / dx);
            angle = atan(tan2);
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
        }
        if (a > 180) {
            return a - 360;
        }
        return a;
    }

    public static int distance(int x1, int y1, int x2, int y2) {
        return sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
    }

    public static int sqrt(int a) {
        int x1;
        if (a <= 0) {
            return 0;
        }
        int x = (a + 1) / 2;
        do {
            x1 = x;
            x = (x / 2) + (a / (2 * x));
        } while (Math.abs(x1 - x) > 1);
        return x;
    }

    public static boolean inDataRange(Actor p1, Actor p2) {
        return Math.abs(p1.x - p2.x) < (Canvas.w / 2) + 100 && Math.abs(p1.y - p2.y) < (Canvas.w / 2) + 100;
    }

    public static void quickSort(Vector actors) {
        recQuickSort(actors, 0, actors.size() - 1);
    }

    private static void recQuickSort(Vector actors, int left, int right) {
        try {
            if (right - left <= 0) {
                return;
            }
            int pivot = ((Actor) actors.elementAt(right)).getY();
            int partition = partitionIt(actors, left, right, pivot);
            recQuickSort(actors, left, partition - 1);
            recQuickSort(actors, partition + 1, right);
        } catch (Exception e) {
        }
    }

    private static int partitionIt(Vector actors, int left, int right, int pivot) {
        int leftPtr = left - 1;
        int rightPtr = right;
        while (true) {
            try {
                leftPtr++;
                if (((Actor) actors.elementAt(leftPtr)).getY() >= pivot) {
                    while (rightPtr > 0) {
                        rightPtr--;
                        if (((Actor) actors.elementAt(rightPtr)).getY() <= pivot) {
                            break;
                        }
                    }
                    if (leftPtr >= rightPtr) {
                        break;
                    }
                    swap(actors, leftPtr, rightPtr);
                }
            } catch (Exception e) {
                Cout.println("LOI PAINT partitionIt TRONG UTIL");
            }
        }
        swap(actors, leftPtr, right);
        return leftPtr;
    }

    private static void swap(Vector actors, int dex1, int dex2) {
        Object temp = actors.elementAt(dex2);
        if (((Actor) actors.elementAt(dex2)).getY() != ((Actor) actors.elementAt(dex1)).getY()) {
            actors.setElementAt(actors.elementAt(dex1), dex2);
            actors.setElementAt(temp, dex1);
        }
    }

    public static int abs(int x) {
        return x > 0 ? x : -x;
    }

    public static short findDirection(Actor from, Actor lookTo) {
        try {
            if (abs(from.x - lookTo.x) < abs(from.y - lookTo.y)) {
                if (lookTo.y > from.y) {
                    return (short) 0;
                }
                return (short) 1;
            }
            if (lookTo.x < from.x) {
                return (short) 2;
            }
            return (short) 3;
        } catch (Exception e) {
            return (short) 0;
        }
    }

    static int pos(char a) {
        for (int i = 0; i < st[0].length; i++) {
            if (st[0][i].indexOf(new StringBuffer(String.valueOf(a)).toString()) != -1) {
                return i;
            }
        }
        return -1;
    }

    public static String converUTF2NoUTF(String st2) {
        String string;
        String result = "";
        for (int i = 0; i < st2.length(); i++) {
            int index = pos(st2.charAt(i));
            if (index != -1) {
                string = new StringBuffer(String.valueOf(result)).append(st[1][index]).toString();
            } else {
                string = new StringBuffer(String.valueOf(result)).append(st2.charAt(i)).toString();
            }
            result = string;
        }
        return result;
    }

    public static String[] split(String original, String separator) {
        Vector nodes = new Vector();
        int iIndexOf = original.indexOf(separator);
        while (true) {
            int index = iIndexOf;
            if (index < 0) {
                break;
            }
            nodes.addElement(original.substring(0, index));
            original = original.substring(index + separator.length());
            iIndexOf = original.indexOf(separator);
        }
        nodes.addElement(original);
        String[] result = new String[nodes.size()];
        if (nodes.size() > 0) {
            for (int loop = 0; loop < nodes.size(); loop++) {
                result[loop] = (String) nodes.elementAt(loop);
            }
        }
        return result;
    }

    public static void paintColor(Graphics g, int row, int r, int gr, int bl, int x, int y, int w, int dr, int dg, int db, int type, int stDegree, int endDegree) {
        for (int i = 0; i < row; i++) {
            int red = r + (((i + 1) * (255 - r)) / row);
            int green = gr + (((i + 1) * (255 - gr)) / (row / dg));
            int blue = bl + (((i + 1) * (255 - bl)) / (row / db));
            g.setColor(red, green, blue);
            if (type == 0) {
                g.fillRect(x, y - i, w, 1);
            } else if (type == 1) {
                g.fillRect(x, y + i, w, 1);
            } else if (type == 2) {
                g.fillRect(x + i, y, 1, w);
            } else if (type == 3) {
                g.fillRect(x - i, y, 1, w);
            } else if (type == 4) {
                g.fillArc(x + i, y + i, w - (i * 2), w - (i * 2), stDegree, endDegree);
            }
        }
    }

    public static void paintColumColor(Graphics g, int x, int y, int row, int r, int gr, int b, int size, int dr, int dg, int db, int type, int stDegree, int endDegree) {
        if (type == 0) {
            paintColor(g, row, r, gr, b, x, y, size, dr, dg, db, 0, 0, 0);
            paintColor(g, row, r, gr, b, x, (y - (row * 2)) + 1, size, dr, dg, db, 1, 0, 0);
        } else if (type == 1) {
            paintColor(g, row, r, gr, b, x, y, size, dr, dg, db, 2, 0, 0);
            paintColor(g, row, r, gr, b, (x + (row * 2)) - 1, y, size, dr, dg, db, 3, 0, 0);
        } else if (type == 2) {
            paintEcliseColor(g, x, y, row, r, gr, b, size, dr, dg, db, type, stDegree, endDegree);
        }
    }

    public static int getDistance(int x, int y, int x2, int y2) {
        return Res.getRange(x, x2, y, y2);
    }

    public static void paintEcliseColor(Graphics g, int x, int y, int row, int r, int gr, int b, int size, int dr, int dg, int db, int type, int stDegree, int endDegree) {
        paintColor(g, row, r, gr, b, x, y, size, 1, 1, 1, 4, stDegree, endDegree);
    }
}
