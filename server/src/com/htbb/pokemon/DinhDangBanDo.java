package com.htbb.pokemon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Doc/ghi DUNG TUNG BYTE dinh dang ban do cMap/&lt;id&gt; (xem docs/pokemon/DINH_DANG_MAP.md).
 * Ba noi doc dinh dang nay phai khop voi lop nay: server MapNapTaiNguyen.loadTileMap (+ khoi doc
 * tile trong Map/MapKhoiTao/Map*.java), client Unity Tilemap.loadMap, client J2ME
 * game/model/Tilemap.loadMap.
 *
 * <pre>
 *   u8 w, u8 h                       (&lt;= 255; o = 16 px)
 *   w*h x u8 tile                    (chi so trong bo tile t/t_hang/t_thanh; 254 va 255 = o trong)
 *   { u8 x,u8 y,u8 ww,u8 hh, hh*ww x u8 tile } ... u8 255     khoi "tileTop" (ve de len nguoi)
 *   { u8 x,u8 y,u8 tile } ... u8 255                           tileTop le
 *   { u8 x,u8 y,u8 idCay } ... u8 255 (hoac 254)               vat trang tri map/tree/&lt;idCay&gt; (lenh -48)
 *   u8 offline (1 = ban do offline / trong nha)
 *   u8 n, n x { u8 x,u8 y, u16 LE toM, u8 xOut, u8 yOut }       o cong (toM: chi so huong trong locationMap)
 *   u8 nNpc, nNpc x { u8 x, u8 y, u8 loai }                     NPC lang chi client doc (anh npc&lt;idImg[loai]&gt;)
 *   ... byte con lai (thuong 0 hoac khong co) - giu nguyen
 * </pre>
 *
 * KHONG nham voi map/map&lt;N&gt; (luoi sinh quai: u8 w, u8 h, w*h x u8 id mau quai; doc o
 * MapNapTaiNguyen.loadMap) - day la file khac, xem {@link LuoiQuai}.
 */
public final class DinhDangBanDo {

    /** Mot o cong dich chuyen. */
    public static final class Cong {
        public int x, y, toM, xOut, yOut;

        public Cong(final int x, final int y, final int toM, final int xOut, final int yOut) {
            this.x = x;
            this.y = y;
            this.toM = toM;
            this.xOut = xOut;
            this.yOut = yOut;
        }
    }

    /** Diem (x, y, gia tri): tileTop le, cay, NPC. */
    public static final class Diem {
        public int x, y, v;

        public Diem(final int x, final int y, final int v) {
            this.x = x;
            this.y = y;
            this.v = v;
        }
    }

    /** Khoi tileTop hinh chu nhat. */
    public static final class Khoi {
        public int x, y, w, h;
        public int[] tile;

        public Khoi(final int x, final int y, final int w, final int h, final int[] tile) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.tile = tile;
        }
    }

    public int w, h;
    /** Tile nen, gia tri 0..255 nhu trong tep (254 giu nguyen 254 de ghi lai dung byte). */
    public int[] tile;
    public final List<Khoi> khoiTop = new ArrayList<Khoi>();
    public final List<Diem> topLe = new ArrayList<Diem>();
    public final List<Diem> cay = new ArrayList<Diem>();
    /** Byte ket thuc danh sach cay (255 hoac 254). */
    public int ketCay = 255;
    public boolean offline;
    public final List<Cong> cong = new ArrayList<Cong>();
    public final List<Diem> npc = new ArrayList<Diem>();
    /** true neu tep co byte so NPC (mot so tep cu dung ngay sau cong). */
    public boolean coNpc = true;
    public byte[] duoi = new byte[0];

    public DinhDangBanDo() {
    }

    public DinhDangBanDo(final int w, final int h) {
        this.w = w;
        this.h = h;
        this.tile = new int[w * h];
    }

    /** Thu tu bo tile theo id ban do nap (getMapLoad): giong Res.loadTileImage / Map*.java. */
    public static String boTile(final int idNap) {
        return idNap < 110 ? "t" : (idNap > 200 ? "t_thanh" : "t_hang");
    }

    public int o(final int x, final int y) {
        return tile[y * w + x];
    }

    /** O trong (khong ve tile): 254/255. */
    public static boolean trong(final int t) {
        return t == 254 || t == 255;
    }

    public static DinhDangBanDo doc(final byte[] b) throws IOException {
        final DinhDangBanDo m = new DinhDangBanDo();
        final int[] p = {0};
        m.w = u8(b, p);
        m.h = u8(b, p);
        m.tile = new int[m.w * m.h];
        for (int i = 0; i < m.tile.length; i++) {
            m.tile[i] = u8(b, p);
        }
        while (true) {
            final int x = u8(b, p);
            if (x == 255) {
                break;
            }
            final int y = u8(b, p), ww = u8(b, p), hh = u8(b, p);
            final int[] t = new int[ww * hh];
            for (int i = 0; i < t.length; i++) {
                t[i] = u8(b, p);
            }
            m.khoiTop.add(new Khoi(x, y, ww, hh, t));
        }
        while (true) {
            final int x = u8(b, p);
            if (x == 255) {
                break;
            }
            m.topLe.add(new Diem(x, u8(b, p), u8(b, p)));
        }
        while (true) {
            final int x = u8(b, p);
            if (x == 255 || x == 254) {
                m.ketCay = x;
                break;
            }
            m.cay.add(new Diem(x, u8(b, p), u8(b, p)));
        }
        m.offline = u8(b, p) == 1;
        final int n = u8(b, p);
        for (int i = 0; i < n; i++) {
            final int x = u8(b, p), y = u8(b, p), lo = u8(b, p), hi = u8(b, p);
            m.cong.add(new Cong(x, y, lo | (hi << 8), u8(b, p), u8(b, p)));
        }
        if (p[0] >= b.length) {
            m.coNpc = false;
        } else {
            final int nn = u8(b, p);
            for (int i = 0; i < nn; i++) {
                m.npc.add(new Diem(u8(b, p), u8(b, p), u8(b, p)));
            }
        }
        m.duoi = new byte[b.length - p[0]];
        System.arraycopy(b, p[0], m.duoi, 0, m.duoi.length);
        return m;
    }

    public byte[] ghi() {
        if (w < 1 || h < 1 || w > 255 || h > 255) {
            throw new IllegalStateException("kich thuoc ban do ngoai 1..255: " + w + "x" + h);
        }
        final ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(w);
        o.write(h);
        for (final int t : tile) {
            o.write(byte8(t, "tile"));
        }
        for (final Khoi k : khoiTop) {
            o.write(byte8(k.x, "x"));
            o.write(byte8(k.y, "y"));
            o.write(byte8(k.w, "w"));
            o.write(byte8(k.h, "h"));
            for (final int t : k.tile) {
                o.write(byte8(t, "tile"));
            }
        }
        o.write(255);
        for (final Diem d : topLe) {
            o.write(byte8(d.x, "x"));
            o.write(d.y);
            o.write(d.v);
        }
        o.write(255);
        for (final Diem d : cay) {
            o.write(byte8(d.x, "x cay"));
            o.write(d.y);
            o.write(d.v);
        }
        o.write(ketCay);
        o.write(offline ? 1 : 0);
        o.write(byte8(cong.size(), "so cong"));
        for (final Cong c : cong) {
            o.write(c.x);
            o.write(c.y);
            o.write(c.toM & 0xFF);
            o.write((c.toM >> 8) & 0xFF);
            o.write(c.xOut);
            o.write(c.yOut);
        }
        if (coNpc) {
            o.write(byte8(npc.size(), "so npc"));
            for (final Diem d : npc) {
                o.write(d.x);
                o.write(d.y);
                o.write(d.v);
            }
        }
        o.write(duoi, 0, duoi.length);
        return o.toByteArray();
    }

    private static int byte8(final int v, final String ten) {
        if (v < 0 || v > 255) {
            throw new IllegalStateException(ten + " ngoai 0..255: " + v);
        }
        return v;
    }

    private static int u8(final byte[] b, final int[] p) throws IOException {
        if (p[0] >= b.length) {
            throw new IOException("het tep o byte " + p[0]);
        }
        return b[p[0]++] & 0xFF;
    }

    /**
     * Luoi sinh quai map/map&lt;N&gt; (MapNapTaiNguyen.loadMap): u8 w, u8 h, roi h hang x w o u8 =
     * id mau quai (0 va 255 = khong co). Dac biet: map 17 cong them 3 vao id &gt; 81 luc doc.
     */
    public static final class LuoiQuai {
        public int w, h;
        public int[] o;
        public byte[] duoi = new byte[0];

        public static LuoiQuai doc(final byte[] b) {
            final LuoiQuai l = new LuoiQuai();
            l.w = b[0] & 0xFF;
            l.h = b[1] & 0xFF;
            l.o = new int[l.w * l.h];
            for (int i = 0; i < l.o.length && 2 + i < b.length; i++) {
                l.o[i] = b[2 + i] & 0xFF;
            }
            final int het = Math.min(b.length, 2 + l.o.length);
            l.duoi = new byte[b.length - het];
            System.arraycopy(b, het, l.duoi, 0, l.duoi.length);
            return l;
        }

        public boolean coQuai(final int x, final int y) {
            final int v = o[y * w + x];
            return v > 0 && v != 255;
        }

        public byte[] ghi() {
            final byte[] b = new byte[2 + o.length + duoi.length];
            b[0] = (byte) w;
            b[1] = (byte) h;
            for (int i = 0; i < o.length; i++) {
                b[2 + i] = (byte) o[i];
            }
            System.arraycopy(duoi, 0, b, 2 + o.length, duoi.length);
            return b;
        }
    }
}
