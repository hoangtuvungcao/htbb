package game.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.htbb.client.GameMidlet;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.core.j2me.Images2;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import game.render.screen.RMS;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Tilemap {
    public static final int T_BLOCK = 2;
    public static int a;
    public static int b;
    public static int h;
    public static Images2 imgMap;
    public static boolean isLoadImageBigMapOk;
    public static boolean isOfflineMap;
    public static short[] map;
    public static Bitmap minimap;
    public static int pxh;
    public static int pxw;
    public static int[] type;
    public static int[] typeOfTile;
    public static int w;
    public static Vector trees = new Vector();
    public static Vector pointPop = new Vector();
    public static short lv = 0;
    public static byte idXaphu = -1;
    public static int idLinhGac = 0;
    public static int[] mangMau = {-9329647, -9329647, -9329647, -9329647, -9329647, -11109357, -11178988, -5793447, -5731240, -4937624, -4937624, -7442124, -7442124, -8620962, -6842746, -9674681, -4937624, -4937624, -5731240, -5731240, -3961777, -9674681, -9674681, -6842746, -5731240, -3423044, -5786239, -7754171, -5793447, -5731240, -5793447, -9329647, -7296213, -6052787, -4937624, -6052787, -7296213, -7296213, -7296213, -7442124, -9989105, -9989105, -9989105, -9989105, -9989105, -9329647, -9674681, -12174808, -9674681, -8620962, -8620962, -9674681, -9674681, -9674681, -8620962, -6651836, -6651836, -9674681, -8620962, -9674681, -5725799, -5000536, -8620962, -7568010, -3423044, -6842746, -7568010, -5000536, -5000536, -3423044, -5731240, -5731240, -3884915, -3884915, -5000536, -3423044, -5000536, -6842746, -14737382, -12174808, -3884915, -3423044, -3423044, -4937624, -4937624, -4937624, -6052787, -6052787, -6052787, -8620962, -7568010, -7568010, -7568010, -7568010, -3423044, -5725799, -5725799, -5725799, -3423044, -6842746, -9674681, -12174808, -9674681, -7568010, -7568010, -6842746, -7568010, -7568010, -7568010, -6651836, -9926565, -8620962, -7442124, -8620962, -9926565, -7442124, -9926565, -9674681, -9674681, -6842746, -9674681, -12174808, -7442124, -7442124, -12174808, -14737382, -8620962, -7442124, -7442124, -6651836, -6651836, -6651836, -6651836, -7442124, -4937624, -8798816, -5786239, -5786239, -4937624, -4937624, -5786239, -8798816, -8798816, -8406128, -9329647, -9329647, -9329647, -9329647, -8406128, -3961777, -4937624, -9397969, -12151277, -6651836, -6651836, -7442124, -7442124, -5731240, -7754171, -2561594, -2561594, -6651836, -11109357, -11109357, -11109357, -11109357, -11109357, -11109357, -11109357, -11109357, -11178988, -11109357, -11109357, -11178988, -11178988, -12234988, -11178988, -11178988, -11178988, -11178988, -11178988, -9989105, -10571373, -10571373, -9397969, -9397969, -9926565, -9926565, -10571373, -8798816, -5000536, -7754171, -9926565, -12234988, -12234988, -11109357, -10571373, -10571373, -10571373, -10571373, -12151277, -7296213, -7442124, -9989105, -9397969, -11109357, -9329647, -9926565, -7568010, -8620962, -3961777, -6651836, -7442124, -3961777, -9397969, -9329647, -9397969, -9397969, -9329647, -9329647, -9397969, -9397969, -7568010, -7568010, -9674681, -8620962, -9674681, -9674681, -8620962, -8620962, -7568010, -7568010};
    public static int[] mangMauThanh = {-9130460, -9066231, -8478708, -10185972, -9066231, -11308000, -10849222, -5463955, -4215959, -7772117, -7772117, -6646662, -4293277, -4343630, -7098510, -7821999, -5794217, -5794217, -5794217, -9066231, -7821999, -4807845, -4807845, -7821999, -6832856, -9130460, -7821999, -10185972, -10185972, -10185972, -10185972, -9066231, -4016237, -4477576, -16383486, -12305619, -4081256, -3291962, -3685703, -4807845, -4807845, -4215959, -4807845, -7821999, -5794217, -9209252, -8357008, -7568268, -7041916, -7305089, -3685703, -5989220, -6054252, -6054252, -3291962, -6054252, -10133692, -13030375, -11573189, -7566723, -7568268, -6646662, -7566723, -7566723, -7503257, -5931699, -8354744, -7771319, -8695516, -7383231, -7562116, -13819888, -7635907, -8478708, -9066231, -9066231, -9130460, -6447482, -6442068, -7040369, -11968937, -9605779, -11968937, -11968937, -11968937, -11968937, -10001057, -11968937, -11968937, -10001057, -10519694, -11968937, -11968937, -7564156, -1251610, -1251610, -9605779, -8092532, -11777217, -11968937, -11968937, -8421505, -7041916, -8635615, -9170164, -9357793, -7305089, -2040619, -7305089, -7041916, -4343630, -3620672, -3291962, -4868683, -8092532, -11968937, -13352130, -11968937, -9605779, -7241618, -7241618, -11968937, -11513776, -5675187, -7318988, -5475492, -7383231, -7241618, -7771319, -4607593, -4016237, -4016237, -4607593, -4607593, -4607593, -4607593, -4607593, -6646662, -5266047, -6055037, -6911136, -4343630, -10185972, -10702963, -10702963, -8939220, -8939220, -10388409, -11497622, -10702963, -9717083, -6442068, -10632630, -10849222, -10702963, -9848683, -10702963, -9848683, -11692288, -6832856, -7635907, -10185972, -8939220, -8944894, -9130460, -9337253, -7566723, -9209252, -3763638, -6650554, -4756941, -4491686, -6054252, -6054252, -5199720, -7040369, -5199720, -7040369, -3685703, -2830138, -1251610, -1251610, -4293277, -7383231, -5475492, -4293277, -6054252, -2040619, -5989220, -7041916, -2764854, -8357776, -5475492, -7241618, -5475492, -7241618, -7771319, -5475492, -5475492, -7383231, -5675187, -7305089, -6646662, -10475740, -9170164, -7383231, -7566723, -5002076, -5002076, -4868683, -5989220, -7040369, -7041916, -8357776, -8357776, -8421505};
    public static int[] mangMauHang = {-11122111, -12700109, -12700109, -12897747, -11122111, -11779267, -11779267, -12700109, -15068912, -13489111, -13489111, -13752794, -13489111, -13489111, -12700109, -12700109, -12240074, -11779267, -12897747, -12897747, -12897747, -12700109, -12240074, -12240074, -12700109, -12700109, -12700109, -12700109, -12700109, -12897747, -12240074, -5793435, -4346510, -4608648, -5793435, -7244993, -13489633, -14607855, -9940944, -6059181, -5005724, -5005724, -5005724, -5005724, -5005724, -7959740, -7959740, -4939159, -5005724, -6257328, -5730215, -5730215, -6257328, -4939159, -5005724, -6059181, -5730215, -6059181, -6257328, -5005724, -5005724, -5005724, -5005724, -7244993, -7244993, -7244993, -5929143, -6059181, -5929143, -5929143, -5929143, -5929143, -5210813, -8812703, -9146292, -7250893, -9146292, -8812703, -7244993, -8812703, -9940944, -7250893, -4939159, -4346510, -4673156, -5851513, -4673156, -5851513, -8733539, -8733539, -8733539, -8733539, -8076127, -8733539, -12240074, -13489111, -11122111, -11122111, -13489111, -9940944, -11122111, -12700109, -12897747, -12240074, -12897747, -12240074, -12897747, -12240074, -12700109, -11122111, -10266547, -11779267, -12240074, -10266547, -11779267, -12897747};
    public static Hashtable tileTop = new Hashtable();
    static short size = 16;
    public static Vector mapchangeLocations = new Vector();
    public static String mapName = "";
    public static int sip = 4;
    static int song = 0;
    static int tick = 0;
    public static Vector totalImageMap = new Vector();

    public static void getImg() {
    }

    public static void reset() {
        Res.removeImgEffect();
        Res.removeWeaPone();
        Res.removeMonsterTemplet();
        Res.removePartInfo();
        Res.resetTrangBi();
        mapchangeLocations.removeAllElements();
        GCanvas.gameScr.actors.removeAllElements();
        GCanvas.gameScr.actors.addElement(GCanvas.gameScr.mainChar);
        if (GCanvas.gameScr.mainChar.myPet != null) {
            GCanvas.gameScr.actors.addElement(GCanvas.gameScr.mainChar.myPet);
        }
        IsAnimal.img = new Bitmap[10];
        idLinhGac = 0;
        GCanvas.gameScr.btnFocus = 0;
        GCanvas.gameScr.btnFocusPotion = 0;
        GCanvas.gameScr.npc_limit.removeAllElements();
    }

    public static void loadMap(int i, byte[] bArr) {
        DataInputStream is;
        try {
            isLoadImageBigMapOk = false;
            Res.imgTile = null;
            Res.loadTileImage(i);
            map = null;
            imgMap = null;
            type = null;
            trees.removeAllElements();
            pointPop.removeAllElements();
            mapchangeLocations.removeAllElements();
            lv = (short) i;
            if (bArr == null) {
                is = new DataInputStream(GameMidlet.asset.open("map_" + i));
            } else {
                is = new DataInputStream(new ByteArrayInputStream(bArr));
            }
            a = 15;
            w = is.read();
            h = is.read();
            pxw = w << 4;
            pxh = h << 4;
            map = new short[w * h];
            tileTop.clear();
            type = new int[w * h];
            for (int k = 0; k < w * h; k++) {
                map[k] = (short) is.read();
                if (map[k] == 254) {
                    map[k] = 255;
                }
                short t = map[k];
                if (t != 255 && t != -1) {
                    type[k] = typeOfTile[t];
                }
            }
            a = 25;
            while (true) {
                int x = is.read();
                if (x == 255) {
                    break;
                }
                int y = is.read();
                int ww = is.read();
                int hh = is.read();
                for (int yy = y; yy < y + hh; yy++) {
                    for (int xx = x; xx < x + ww; xx++) {
                        short value = (short) is.read();
                        if (value != 255) {
                            TileTop tile = new TileTop();
                            tile.y = (short) yy;
                            tile.x = (short) xx;
                            tile.index = value;
                            tileTop.put(new Integer((w * yy) + xx), tile);
                        }
                    }
                }
            }
            while (true) {
                int x = is.read();
                if (x == 255) {
                    break;
                }
                int y = is.read();
                short value = (short) is.read();
                if (value != 255) {
                    TileTop tile = new TileTop();
                    tile.y = (short) y;
                    tile.x = (short) x;
                    tile.index = value;
                    tileTop.put(new Integer((y * w) + x), tile);
                }
            }
            a = 45;
            Vector tem = new Vector();
            while (true) {
                int x = is.read();
                if (x == 254) {
                    x = 255;
                }
                if (x == 255) {
                    break;
                }
                int y = is.read();
                int treeType = is.read();
                tem.addElement(new Tree(x, y, treeType));
            }
            int nTree = tem.size();
            for (int k = 0; k < nTree; k++) {
                trees.addElement(tem.elementAt(k));
            }
            isOfflineMap = is.read() == 1;
            int nLocation = is.read();
            Vector tempx = new Vector();
            Vector tempy = new Vector();
            int lci = -1;
            int preX = -1;
            int preY = -1;
            int lastY = 0;
            int lastx = 0;
            for (int k = 0; k < nLocation; k++) {
                int x = is.read();
                int y = is.read();
                if (lci == -1) {
                    tempx.addElement(x + "");
                    tempy.addElement(y + "");
                    preY = y;
                    lastY = y;
                    preX = x;
                    lastx = x;
                    lci = 0;
                } else if (x == preX && y != preY && GCanvas.gameScr.abs(y - lastY) <= 1) {
                    tempx.addElement(x + "");
                    tempy.addElement(y + "");
                    lastY = y;
                    lastx = x;
                } else if (y == preY && x != preX && x - lastx <= 1) {
                    tempx.addElement(x + "");
                    tempy.addElement(y + "");
                    lastY = y;
                    lastx = x;
                } else if ((x != preX && y != preY) || GCanvas.gameScr.abs(y - lastY) > 1 || x - lastx > 1) {
                    int px = getMin(tempx);
                    int py = getMin(tempy);
                    if (tempx.size() > 1) {
                        px = ((px + getMax(tempx)) * 16) / 2;
                        py = ((py + getMax(tempy)) * 16) / 2;
                    } else {
                        px *= 16;
                        py *= 16;
                    }
                    int xadd;
                    if (w >= 20 && h >= 20) {
                        int xaddX = 8;
                        if (px / 16 < 3) {
                            px += 32;
                            py -= 16;
                            xaddX = 0;
                        } else if (px / 16 >= w - 3) {
                            px -= 32;
                            py -= 16;
                            xaddX = 0;
                        }
                        if (py / 16 < 4) {
                            py += 32;
                            xadd = 4;
                        } else if (py / 16 > h - 4) {
                            py -= 32;
                            xadd = -5;
                        } else {
                            py += 24;
                            xadd = xaddX;
                        }
                    } else {
                        xadd = 0;
                    }
                    pointPop.addElement(new PopupName("vào", px + xadd, py));
                    tempx.removeAllElements();
                    tempy.removeAllElements();
                    tempx.addElement(x + "");
                    tempy.addElement(y + "");
                    preY = y;
                    lastY = y;
                    preX = x;
                    lastx = x;
                    lci = 0;
                }
                a = 70;
                type[(y * w) + x] = 2000000000 + k;
                int[] aa1 = {is.read(), is.read()};
                short value = 0;
                for (int kk = 1; kk >= 0; kk--) {
                    value = (short) (((short) (value << 8)) | (aa1[kk] & 255));
                }
                int xx = is.read();
                int yy = is.read();
                mapchangeLocations.addElement(new CMLocation(value, xx, yy));
            }
            int px = getMin(tempx);
            int py = getMin(tempy);
            if (tempx.size() > 1) {
                px = ((px + getMax(tempx)) * 16) / 2;
                py = ((py + getMax(tempy)) * 16) / 2;
            } else {
                px *= 16;
                py *= 16;
            }
            int xadd;
            if (w >= 20 && h >= 20) {
                int xaddX = 8;
                if (px / 16 < 3) {
                    px += 32;
                    py -= 16;
                    xaddX = 0;
                } else if (px / 16 >= w - 3) {
                    px -= 32;
                    py -= 16;
                    xaddX = 0;
                }
                if (py / 16 < 3) {
                    py += 32;
                    xadd = 4;
                } else if (py / 16 > h - 3) {
                    py -= 32;
                    xadd = -5;
                } else {
                    py += 24;
                    xadd = xaddX;
                }
            } else {
                xadd = 0;
            }
            a = 85;
            pointPop.addElement(new PopupName("vào", px + xadd, py));
            int nNPC = is.read();
            FilePack filePack = new FilePack("/npc.sh", null);
            if (GCanvas.gameScr != null) {
                for (int k = 0; k < nNPC; k++) {
                    NPC npc = new NPC(is.read(), is.read(), is.read(), filePack);
                    if (GameScr.posNpcReceiveClan != null && GameScr.posNpcReceiveClan.index == npc.type) {
                        GameScr.posNpcReceiveClan.x = npc.x;
                        GameScr.posNpcReceiveClan.y = npc.y;
                    }
                    GCanvas.gameScr.actors.addElement(npc);
                }
            }
            filePack.close();
            FilePack.reset();
            imgMap = Images2.createImage(w, h);
            GCanvas.gameScr.isMini = true;
            imgMap.getGraphics();
            int[] pixels = new int[w * h];
            for (int xa = 0; xa < w; xa++) {
                for (int yb = 0; yb < h; yb++) {
                    if (tileTop.get(new Integer((w * yb) + xa)) != null) {
                        if (Res.mauTile == 0) {
                            pixels[(w * yb) + xa] = mangMauThanh[((TileTop) tileTop.get(new Integer((w * yb) + xa))).index];
                        } else if (Res.mauTile == 1) {
                            pixels[(w * yb) + xa] = mangMau[((TileTop) tileTop.get(new Integer((w * yb) + xa))).index];
                        } else if (Res.mauTile == 2) {
                            pixels[(w * yb) + xa] = mangMauHang[((TileTop) tileTop.get(new Integer((w * yb) + xa))).index];
                        }
                    } else {
                        short s = map[(w * yb) + xa];
                        if (s != 255) {
                            if (Res.mauTile == 0) {
                                pixels[(w * yb) + xa] = mangMauThanh[s];
                            } else if (Res.mauTile == 1) {
                                pixels[(w * yb) + xa] = mangMau[s];
                            } else if (Res.mauTile == 2) {
                                pixels[(w * yb) + xa] = mangMauHang[s];
                            }
                        }
                    }
                }
            }
            minimap = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            e.printStackTrace();
            GameMidlet.instance.notifyDestroyed();
        }
        if (GCanvas.gameScr != null) {
            GCanvas.gameScr.setPosMiniMap();
        }
        int wFind = GameScr.wMiniMap;
        if (wFind > w) {
            wFind = w;
        }
        int hFind = GameScr.hMiniMap;
        if (hFind > h) {
            hFind = h;
        }
        GameScr.mapFind = new byte[wFind][hFind];
        a = 100;
        if (GCanvas.screenlevel > 1) {
            loadImageMap(w * 16, h * 16, lv);
        }
        isLoadImageBigMapOk = true;
    }

    public static int getMax(Vector vector) {
        int size2 = vector.size();
        int i = -10;
        for (int i2 = 0; i2 < size2; i2++) {
            int i3 = Integer.parseInt((String) vector.elementAt(i2));
            if (i < i3) {
                i = i3;
            }
        }
        return i;
    }

    public static int getMin(Vector vector) {
        int size2 = vector.size();
        int i = 10000;
        for (int i2 = 0; i2 < size2; i2++) {
            int i3 = Integer.parseInt((String) vector.elementAt(i2));
            if (i > i3) {
                i = i3;
            }
        }
        return i;
    }

    public static void setNameMap(int i) {
        if (i == 70 || i == 80) {
            idXaphu = (byte) 0;
            return;
        }
        if (i != 202) {
            switch (i) {
                case 0:
                    mapName = "Sơn Nam";
                    idXaphu = (byte) 0;
                    break;
                case 1:
                    mapName = "Dao Châu";
                    break;
                case 2:
                    mapName = "Tiên Du";
                    break;
                case 3:
                    mapName = "Phù Liệt";
                    idXaphu = (byte) 1;
                    break;
                case 4:
                    mapName = "Kỳ Bố";
                    idXaphu = (byte) 2;
                    break;
                case 5:
                    mapName = "Hàm Tử";
                    idXaphu = (byte) 3;
                    break;
                case 6:
                    mapName = "Thạch Giang";
                    idXaphu = (byte) 4;
                    break;
                case 7:
                    mapName = "Đông Sơn";
                    idXaphu = (byte) 5;
                    break;
                case 8:
                    mapName = "Tử Quan";
                    idXaphu = (byte) 6;
                    break;
                case 9:
                    mapName = "Trường Giang";
                    idXaphu = (byte) 7;
                    break;
                case 10:
                    mapName = "Lộc Trĩ";
                    idXaphu = (byte) 8;
                    break;
                case 11:
                    mapName = "Sơn Lâm";
                    break;
                default:
                    switch (i) {
                        case 100:
                            mapName = "Nhà bán HP";
                            break;
                        case 101:
                            mapName = "Nhà bán MP";
                            break;
                        case 102:
                            mapName = "Nhà thợ rèn";
                            break;
                        default:
                            switch (i) {
                                case 110:
                                    mapName = "Hang động";
                                    break;
                                case 111:
                                    mapName = "Hang mãng xà";
                                    break;
                                case 112:
                                    mapName = "Hang thằn lằn";
                                    break;
                                default:
                                    mapName = "";
                                    break;
                            }
                            break;
                    }
                    break;
            }
            return;
        }
        mapName = "Đấu trường";
        idXaphu = (byte) 9;
    }

    public static final void paintTile(Graphics graphics) {
        if (GCanvas.screenlevel == 1 || isOfflineMap) {
            if (map != null) {
                for (int i = GCanvas.gameScr.gssx; i < GCanvas.gameScr.gssxe; i++) {
                    for (int i2 = GCanvas.gameScr.gssy; i2 < GCanvas.gameScr.gssye; i2++) {
                        short s = map[(w * i2) + i];
                        if (s != 255) {
                            Bitmap bitmap = Res.imgTile;
                            int i3 = sip;
                            graphics.drawRegion(bitmap, 0, s << i3, 16, 16, 0, i << i3, i2 << i3, 0);
                        }
                    }
                }
            }
        } else {
            for (int i4 = 0; i4 < totalImageMap.size(); i4++) {
                ImageMapBig imageMapBig = (ImageMapBig) totalImageMap.elementAt(i4);
                if (imageMapBig != null) {
                    imageMapBig.paint(graphics);
                }
            }
        }
        if (!GameScr.isTarget || GameScr.iTaskAuto == 2) {
            return;
        }
        graphics.drawRegion(GameScr.imgTarget, 0, ((GCanvas.gameScr.lapTarget / 3) % 7) * 38, 38, 38, 0, GCanvas.gameScr.xT + 5, GCanvas.gameScr.yT, Graphics.HCENTER | Graphics.VCENTER);
    }

    public static final void paintTileTop(Graphics graphics) {
        if (tileTop.size() > 0) {
            Enumeration enumerationKeys = tileTop.keys();
            while (enumerationKeys.hasMoreElements()) {
                TileTop tileTop2 = (TileTop) tileTop.get((Integer) enumerationKeys.nextElement());
                if (tileTop2.y >= GCanvas.gameScr.gssy && tileTop2.y <= GCanvas.gameScr.gssye && tileTop2.x >= GCanvas.gameScr.gssx - 1 && tileTop2.x <= GCanvas.gameScr.gssxe + 1) {
                    tileTop2.paint(graphics);
                }
            }
        }
    }

    public static final int tileTypeAt(int i, int i2) {
        return type[(i2 * w) + i];
    }

    public static final int tileTypeAtPixel(int i, int i2) {
        int i3 = w;
        int i4 = sip;
        int i5 = ((i2 >> 4) * i3) + (i >> i4);
        int[] iArr = type;
        if (i5 < iArr.length) {
            return iArr[((i2 >> i4) * i3) + (i >> i4)];
        }
        return -1;
    }

    public static final boolean tileTypeAtPixel(int i, int i2, int i3) {
        int i4 = sip;
        int i5 = ((i2 >> i4) * w) + (i >> i4);
        if (i5 < 0) {
            return true;
        }
        int[] iArr = type;
        return i5 >= iArr.length || (iArr[i5] & i3) == i3;
    }

    public static final boolean tileTypeAt(int i, int i2, int i3) {
        return (type[(i2 * w) + i] & i3) == i3;
    }

    public static final CMLocation toOtherMapAt(int i, int i2) {
        int i3 = sip;
        int i4 = w;
        int i5 = ((i2 >> i3) * i4) + (i >> i3);
        int[] iArr = type;
        if (i5 >= iArr.length || ((i2 >> i3) * i4) + (i >> i3) < 0 || iArr[((i2 >> i3) * i4) + (i >> i3)] < 2000000000) {
            return null;
        }
        return (CMLocation) mapchangeLocations.elementAt(iArr[((i2 >> i3) * i4) + (i >> i3)] - 2000000000);
    }

    public static final void killTileTypeAt(int i, int i2, int i3) {
        int[] iArr = type;
        int i4 = sip;
        int i5 = ((i2 >> i4) * w) + (i >> i4);
        iArr[i5] = iArr[i5] & (~i3);
    }

    public static final int tileYofPixel(int i) {
        int i2 = sip;
        return (i >> i2) << i2;
    }

    public static final int tileXofPixel(int i) {
        int i2 = sip;
        return (i >> i2) << i2;
    }

    public static int getTotalColumOrRow(int i) {
        int i2 = GCanvas.screenlevel * 256;
        if (i <= i2) {
            return 1;
        }
        int i3 = i / i2;
        int i4 = i % i2;
        if (i4 == 0) {
            return i3;
        }
        if (i4 != 0) {
            return i3 + 1;
        }
        return 0;
    }

    public static void loadImageMap(int i, int i2, int i3) {
        if (isOfflineMap) {
            return;
        }
        int i4 = 0;
        for (int i5 = 0; i5 < totalImageMap.size(); i5++) {
            ImageMapBig imageMapBig = (ImageMapBig) totalImageMap.elementAt(i5);
            if (imageMapBig != null) {
                Bitmap bitmap = imageMapBig.img;
            }
        }
        totalImageMap.removeAllElements();
        System.gc();
        int totalColumOrRow = getTotalColumOrRow(i);
        int totalColumOrRow2 = getTotalColumOrRow(i2) * totalColumOrRow;
        int i6 = GCanvas.screenlevel * 256;
        while (i4 < totalColumOrRow2) {
            ImageMapBig imageMapBig2 = new ImageMapBig();
            int i7 = (i4 % totalColumOrRow) * i6;
            if (i7 <= i - i6) {
                imageMapBig2.wimg = i6;
            } else {
                imageMapBig2.wimg = i - i7;
            }
            int i8 = (i4 / totalColumOrRow) * i6;
            if (i8 <= i2 - i6) {
                imageMapBig2.himg = i6;
            } else {
                imageMapBig2.himg = i2 - i8;
            }
            int i9 = i4 + 1;
            imageMapBig2.path = "ImageMapy" + i3 + "m" + i9 + "scale" + GCanvas.screenlevel;
            imageMapBig2.x = i7;
            imageMapBig2.y = i8;
            imageMapBig2.id = i4;
            imageMapBig2.img = loadImageMap(imageMapBig2);
            totalImageMap.addElement(imageMapBig2);
            i4 = i9;
        }
    }

    public static Bitmap loadImageMap(ImageMapBig imageMapBig) {
        byte[] bArrLoadRMS = RMS.loadRMS(imageMapBig.path);
        if (bArrLoadRMS != null) {
            imageMapBig.isLoadOK = true;
            return Image.createImage(bArrLoadRMS, 0, bArrLoadRMS.length);
        }
        Bitmap bitmapCreateImage = Image.createImage(imageMapBig.wimg, imageMapBig.himg);
        Canvas canvas = new Canvas(bitmapCreateImage);
        canvas.drawColor(0);
        for (int i = imageMapBig.x / 16; i < (imageMapBig.x / 16) + (imageMapBig.wimg / 16); i++) {
            for (int i2 = imageMapBig.y / 16; i2 < (imageMapBig.y / 16) + (imageMapBig.himg / 16); i2++) {
                short s = map[(w * i2) + i];
                if (s != 255) {
                    Bitmap bitmapCreateBitmap = Bitmap.createBitmap(Res.imgTile, 0, s * 16, 16, 16);
                    int width = (i * 16) - imageMapBig.x;
                    int height = (i2 * 16) - imageMapBig.y;
                    if (width <= 0) {
                        width = 0;
                    }
                    if (height <= 0) {
                        height = 0;
                    }
                    if (height > bitmapCreateImage.getHeight() - 16) {
                        height = bitmapCreateImage.getHeight() - 16;
                    }
                    if (width > bitmapCreateImage.getWidth() - 16) {
                        width = bitmapCreateImage.getWidth() - 16;
                    }
                    canvas.drawBitmap(bitmapCreateBitmap, width, height, (Paint) null);
                }
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapCreateImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        try {
            RMS.saveRMS(imageMapBig.path, byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            System.out.println(" LOi SAVE IMG -----------------Big" + e.toString());
        }
        imageMapBig.isLoadOK = true;
        System.out.println("tao map oooo");
        return bitmapCreateImage;
    }
}
