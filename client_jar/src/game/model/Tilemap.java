package game.model;

import game.GameMidlet;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Tilemap {
    public static int w;
    public static int h;
    public static int pxw;
    public static int pxh;
    public static final int T_BLOCK = 2;
    public static short[] map;
    public static int[] type;
    public static int[] typeOfTile;
    public static Image imgMap;
    public static boolean isOfflineMap;
    public static Vector trees = new Vector();
    public static Vector pointPop = new Vector();
    public static short lv = 0;
    public static byte idXaphu = -1;
    public static int idLinhGac = 0;
    public static Hashtable tileTop = new Hashtable();
    static short size = 16;
    public static Vector mapchangeLocations = new Vector();
    public static String mapName = "";
    public static int sip = 4;
    static int song = 0;
    static int tick = 0;

    public static void getImg() {
    }

    public static void reset() {
        Res.removeImgEffect();
        Res.removeWeaPone();
        Res.removeMonsterTemplet();
        Res.removePartInfo();
        Res.resetTrangBi();
        mapchangeLocations.removeAllElements();
        Canvas.gameScr.actors.removeAllElements();
        Canvas.gameScr.npc_limit.removeAllElements();
        Canvas.gameScr.actors.addElement(Canvas.gameScr.mainChar);
        // pokemon: thêm lại đúng MỘT pet (actor trong cảnh, không phải mẫu thông tin myPet) - PetDuyNhat
        game.pokemon.PetDuyNhat.sauDoiMap(Canvas.gameScr.actors, Canvas.gameScr.mainChar);
        IsAnimal.img = new Image[10];
        idLinhGac = 0;
    }

    public static void loadMap(int level, byte[] byteMap) {
        DataInputStream is;
        int xxx;
        int yyy;
        int xxx2;
        int yyy2;
        try {
            Res.imgTile = null;
            Res.loadTileImage(level);
            map = null;
            imgMap = null;
            type = null;
            trees.removeAllElements();
            pointPop.removeAllElements();
            mapchangeLocations.removeAllElements();
            lv = (short) level;
            if (byteMap == null) {
                InputStream iss = "".getClass().getResourceAsStream(new StringBuffer("/").append(level).toString());
                is = new DataInputStream(iss);
            } else {
                ByteArrayInputStream ba = new ByteArrayInputStream(byteMap);
                is = new DataInputStream(ba);
            }
            w = is.read();
            h = is.read();
            pxw = w << 4;
            pxh = h << 4;
            map = new short[w * h];
            tileTop.clear();
            type = new int[w * h];
            for (int i = 0; i < w * h; i++) {
                map[i] = (short) is.read();
                if (map[i] == 254) {
                    map[i] = 255;
                }
                if (map[i] != 255 && map[i] != -1) {
                    type[i] = typeOfTile[map[i]];
                }
            }
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
                            tileTop.put(new Integer((yy * w) + xx), tile);
                        }
                    }
                }
            }
            while (true) {
                int x2 = is.read();
                if (x2 == 255) {
                    break;
                }
                int y2 = is.read();
                short value2 = (short) is.read();
                if (value2 != 255) {
                    TileTop tile2 = new TileTop();
                    tile2.y = (short) y2;
                    tile2.x = (short) x2;
                    tile2.index = value2;
                    tileTop.put(new Integer((y2 * w) + x2), tile2);
                }
            }
            Vector tem = new Vector();
            while (true) {
                int x3 = is.read();
                if (x3 == 254) {
                    x3 = 255;
                }
                if (x3 == 255) {
                    break;
                }
                int y3 = is.read();
                int type2 = is.read();
                tem.addElement(new Tree(x3, y3, type2));
            }
            for (int i2 = 0; i2 < tem.size(); i2++) {
                trees.addElement(tem.elementAt(i2));
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
            for (int i3 = 0; i3 < nLocation; i3++) {
                int x4 = is.read();
                int y4 = is.read();
                if (lci == -1) {
                    preX = x4;
                    preY = y4;
                    lci = 0;
                    tempx.addElement(new StringBuffer(String.valueOf(x4)).toString());
                    tempy.addElement(new StringBuffer(String.valueOf(y4)).toString());
                    lastx = x4;
                    lastY = y4;
                } else if (x4 == preX && y4 != preY && GameScr.abs(y4 - lastY) <= 1) {
                    tempx.addElement(new StringBuffer(String.valueOf(x4)).toString());
                    tempy.addElement(new StringBuffer(String.valueOf(y4)).toString());
                    lastx = x4;
                    lastY = y4;
                } else if (y4 == preY && x4 != preX && x4 - lastx <= 1) {
                    tempx.addElement(new StringBuffer(String.valueOf(x4)).toString());
                    tempy.addElement(new StringBuffer(String.valueOf(y4)).toString());
                    lastx = x4;
                    lastY = y4;
                } else if ((x4 != preX && y4 != preY) || GameScr.abs(y4 - lastY) > 1 || x4 - lastx > 1) {
                    int xxx3 = getMin(tempx);
                    int yyy3 = getMin(tempy);
                    if (tempx.size() > 1) {
                        xxx2 = (16 * (xxx3 + getMax(tempx))) / 2;
                        yyy2 = (16 * (yyy3 + getMax(tempy))) / 2;
                    } else {
                        xxx2 = xxx3 * 16;
                        yyy2 = yyy3 * 16;
                    }
                    int xadd0 = 0;
                    if (w >= 20 && h >= 20) {
                        if (xxx2 / 16 < 3) {
                            xxx2 += 32;
                            yyy2 -= 16;
                        } else if (xxx2 / 16 >= w - 3) {
                            xxx2 -= 32;
                            yyy2 -= 16;
                        } else {
                            xadd0 = 8;
                        }
                        if (yyy2 / 16 < 4) {
                            yyy2 += 32;
                            xadd0 = 4;
                        } else if (yyy2 / 16 > h - 4) {
                            yyy2 -= 32;
                            xadd0 = -5;
                        } else {
                            yyy2 += 24;
                        }
                    }
                    PopupName popup = new PopupName("vào", xxx2 + xadd0, yyy2);
                    pointPop.addElement(popup);
                    tempx.removeAllElements();
                    tempy.removeAllElements();
                    preX = x4;
                    preY = y4;
                    lastx = x4;
                    lastY = y4;
                    lci = 0;
                    tempx.addElement(new StringBuffer(String.valueOf(x4)).toString());
                    tempy.addElement(new StringBuffer(String.valueOf(y4)).toString());
                }
                type[(y4 * w) + x4] = 2000000000 + i3;
                int[] aa1 = {is.read(), is.read()};
                short value3 = 0;
                for (int kk = 1; kk >= 0; kk--) {
                    value3 = (short) (((short) (value3 << 8)) | (255 & aa1[kk]));
                }
                int xx2 = is.read();
                int yy2 = is.read();
                CMLocation cmLocation = new CMLocation(value3, xx2, yy2);
                mapchangeLocations.addElement(cmLocation);
            }
            int xxx4 = getMin(tempx);
            int yyy4 = getMin(tempy);
            if (tempx.size() > 1) {
                xxx = (16 * (xxx4 + getMax(tempx))) / 2;
                yyy = (16 * (yyy4 + getMax(tempy))) / 2;
            } else {
                xxx = xxx4 * 16;
                yyy = yyy4 * 16;
            }
            int xadd = 0;
            if (w >= 20 && h >= 20) {
                if (xxx / 16 < 3) {
                    xxx += 32;
                    yyy -= 16;
                } else if (xxx / 16 >= w - 3) {
                    xxx -= 32;
                    yyy -= 16;
                } else {
                    xadd = 8;
                }
                if (yyy / 16 < 3) {
                    yyy += 32;
                    xadd = 4;
                } else if (yyy / 16 > h - 3) {
                    yyy -= 32;
                    xadd = -5;
                } else {
                    yyy += 24;
                }
            }
            PopupName popup2 = new PopupName("vào", xxx + xadd, yyy);
            pointPop.addElement(popup2);
            int nNPC = is.read();
            FilePack file = new FilePack("/npc.sh", null);
            if (Canvas.gameScr != null) {
                for (int i4 = 0; i4 < nNPC; i4++) {
                    NPC npc = new NPC(is.read(), is.read(), is.read(), file);
                    if (GameScr.posNpcReceiveClan != null && GameScr.posNpcReceiveClan.index == npc.type) {
                        GameScr.posNpcReceiveClan.x = npc.x;
                        GameScr.posNpcReceiveClan.y = npc.y;
                    }
                    Canvas.gameScr.actors.addElement(npc);
                }
            }
            file.close();
            FilePack.reset();
            imgMap = Image.createImage(w, h);
            Graphics g = imgMap.getGraphics();
            for (int a = 0; a < w; a++) {
                for (int b = 0; b < h; b++) {
                    if (tileTop.get(new Integer((b * w) + a)) != null) {
                        ((TileTop) tileTop.get(new Integer((b * w) + a))).paintSmall(g);
                    } else {
                        short s = map[(b * w) + a];
                        if (s != 255) {
                            g.drawRegion(Res.imgtileSmall, 0, s, 1, 1, 0, a, b, 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Cout.println(new StringBuffer("LOAD TILE ERR ").append(level).append(" > ").append(e.toString()).toString());
            GameMidlet.instance.notifyDestroyed();
        }
        if (Canvas.gameScr != null) {
            Canvas.gameScr.setPosMiniMap();
        }
        if (Canvas.instance.hasPointerEvents()) {
            int ww2 = GameScr.wMiniMap;
            if (ww2 > w) {
                ww2 = w;
            }
            int hh2 = GameScr.hMiniMap;
            if (hh2 > h) {
                hh2 = h;
            }
            GameScr.mapFind = new byte[ww2][hh2];
        }
    }

    public static int getMax(Vector cord) {
        int n = -10;
        for (int i = 0; i < cord.size(); i++) {
            int vl = Integer.parseInt((String) cord.elementAt(i));
            if (n < vl) {
                n = vl;
            }
        }
        return n;
    }

    public static int getMin(Vector cord) {
        int n = 10000;
        for (int i = 0; i < cord.size(); i++) {
            int vl = Integer.parseInt((String) cord.elementAt(i));
            if (n > vl) {
                n = vl;
            }
        }
        return n;
    }

    public static void setNameMap(int idmap) {
        switch (idmap) {
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
            case 70:
            case 80:
                idXaphu = (byte) 0;
                break;
            case 100:
                mapName = "Nhà bán HP";
                break;
            case 101:
                mapName = "Nhà bán MP";
                break;
            case 102:
                mapName = "Nhà thợ rèn";
                break;
            case 110:
                mapName = "Hang động";
                break;
            case 111:
                mapName = "Hang mãng xà";
                break;
            case 112:
                mapName = "Hang thằn lằn";
                break;
            case 202:
                mapName = "Đấu trường";
                idXaphu = (byte) 9;
                break;
            default:
                // vung Hoenn (nam 2, ban do moi chi co o server Pokemon): ten theo id
                mapName = game.pokemon.TenMapPokemon.tenHoenn(idmap);
                break;
        }
        // pokemon (W2): ten dia danh viet cung -> dia danh Pokemon
        // 0.9.5: tên bản đồ từ SQL data_map_ten (game.pokemon.TenBanDoBang); bảng tên cũ trên chỉ còn dự phòng
        mapName = game.pokemon.TenBanDoBang.ten(idmap, mapName);
    }

    public static final void paintTile(Graphics g) {
        if (map != null) {
            for (int a = Canvas.gameScr.gssx; a < Canvas.gameScr.gssxe; a++) {
                for (int b = Canvas.gameScr.gssy; b < Canvas.gameScr.gssye; b++) {
                    short s = map[(b * w) + a];
                    if (s != 255) {
                        g.drawRegion(Res.imgTile, 0, s << sip, 16, 16, 0, a << sip, b << sip, 0);
                    }
                }
            }
        }
    }

    public static final void paintTileTop(Graphics g) {
        if (tileTop.size() > 0) {
            Enumeration k = tileTop.keys();
            while (k.hasMoreElements()) {
                Integer key = (Integer) k.nextElement();
                TileTop tile = (TileTop) tileTop.get(key);
                if (tile.y >= Canvas.gameScr.gssy && tile.y <= Canvas.gameScr.gssye && tile.x >= Canvas.gameScr.gssx - 1 && tile.x <= Canvas.gameScr.gssxe + 1) {
                    tile.paint(g);
                }
            }
        }
    }

    public static final int tileTypeAt(int x, int y) {
        return type[(y * w) + x];
    }

    public static final int tileTypeAtPixel(int px, int py) {
        if (((py >> sip) * w) + (px >> sip) < type.length) {
            return type[((py >> sip) * w) + (px >> sip)];
        }
        return -1;
    }

    public static final boolean tileTypeAtPixel(int px, int py, int t) {
        int aa = ((py >> sip) * w) + (px >> sip);
        return aa < 0 || aa >= type.length || (type[aa] & t) == t;
    }

    public static final boolean tileTypeAt(int x, int y, int t) {
        return (type[(y * w) + x] & t) == t;
    }

    public static final CMLocation toOtherMapAt(int px, int py) {
        if (((py >> sip) * w) + (px >> sip) < type.length && ((py >> sip) * w) + (px >> sip) >= 0 && type[((py >> sip) * w) + (px >> sip)] >= 2000000000) {
            return (CMLocation) mapchangeLocations.elementAt(type[((py >> sip) * w) + (px >> sip)] - 2000000000);
        }
        return null;
    }

    public static final void killTileTypeAt(int px, int py, int t) {
        int[] iArr = type;
        int i = ((py >> sip) * w) + (px >> sip);
        iArr[i] = iArr[i] & (t ^ (-1));
    }

    public static final int tileYofPixel(int py) {
        return (py >> sip) << sip;
    }

    public static final int tileXofPixel(int px) {
        return (px >> sip) << sip;
    }
}
