package com.htbb.pokemon;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Noi dung hai lenh mang cua loai day du (he_thong_day_du.md 3.5, quyet dinh D9). Big-endian (DataOutputStream) nhu moi
 * goi khac; client J2ME doc o client_jar/src/game/pokemon/LoaiMang.java.
 *
 *   -125 POKE_QUAI_CON  S->C  u8 n, n x (i16 idQuai, i16 dex, u8 shiny, u8 bac). dex = 0: ve loai mac dinh cua mau.
 *                             Chi gui cho client co so ban >= loai.ban_client (908), NGAY SAU lenh 4 them quai do.
 *   -126 POKE_LOAI      hai chieu, byte dau = kieu:
 *        C->S 0 XIN_SPRITE    i16 dex, u8 shiny
 *        S->C 0 KHUC_SPRITE   i16 dex, u8 shiny, i32 tong, i32 viTri, u16 len, len byte   (tong = 0: may chu khong co,
 *                             khi do KHONG co len / byte). Moi khuc <= {@link #KHUC}; cung mot goi cung tong.
 *        C->S 1 XIN_BANG_LOAI i32 bamHienCo (0 = chua co)
 *        S->C 1 BANG_LOAI     i32 bam, u16 n, n x (i16 dex, UTF ten, i8 he1, i8 he2, u8 bac, u8 theHe, u8 co)
 *        S->C 2 BANG_KHONG_DOI i32 bam
 *        C->S 3 XIN_POKEDEX   (khong co noi dung)
 *        S->C 3 POKEDEX       u16 n, n x (i16 dex, u8 co: bit0 da gap, bit1 da ha, bit2 da bat)
 *
 * So lenh: da grep server/src + client_jar/src + moi nhanh (ban-098, bat-trade, ket-doi, kenh-chat, khoa-map, pvp-keo,
 * giao-dien-nro, ten-pet, sua-tuyen, do-trang-phuc) ngay 22/10/2026: -125, -126 chua dung (-127 = goi nen).
 *
 * Goi sprite may chu (server/map/loai/NNNN.goi, tools/pokemon/loai/DongGoiLoai.java): 'H','T','P','K', u8 phienBan = 1,
 * i16 dex, u8 shiny, u8 n, n x (UTF tenTep, i32 len, len byte).
 *
 * Lop THUAN, test: test/KiemLoaiDayDu.
 */
public final class GoiLoai {

    public static final int POKE_QUAI_CON = -125;
    public static final int POKE_LOAI = -126;

    public static final int XIN_SPRITE = 0, KHUC_SPRITE = 0, XIN_BANG_LOAI = 1, BANG_LOAI = 1, BANG_KHONG_DOI = 2,
            XIN_POKEDEX = 3, POKEDEX = 3;

    /** Kich thuoc toi da mot khuc sprite. */
    public static final int KHUC = 4096;

    /** Toi da so quai trong mot goi -125 (n la u8). */
    public static final int TOI_DA_QUAI = 255;

    private GoiLoai() {
    }

    /** -125: cac bo {idQuai, dex, shiny, bac}. Ghi toi da 255 bo. */
    public static int ghiQuaiCon(final DataOutputStream dos, final List<int[]> ds) throws IOException {
        final int n = Math.min(TOI_DA_QUAI, ds.size());
        dos.writeByte(n);
        for (int i = 0; i < n; i++) {
            final int[] x = ds.get(i);
            dos.writeShort(x[0]);
            dos.writeShort(Math.max(0, x[1]));
            dos.writeByte(x[2] != 0 ? 1 : 0);
            dos.writeByte(Math.max(0, Math.min(6, x[3])));
        }
        return n;
    }

    /** Mot khuc sprite. {@code goi} null hoac rong = may chu khong co (tong 0). */
    public static void ghiKhucSprite(final DataOutputStream dos, final int dex, final int shiny, final byte[] goi,
            final int viTri) throws IOException {
        dos.writeByte(KHUC_SPRITE);
        dos.writeShort(dex);
        dos.writeByte(shiny != 0 ? 1 : 0);
        if (goi == null || goi.length == 0) {
            dos.writeInt(0);
            dos.writeInt(0);
            return;
        }
        final int len = Math.max(0, Math.min(KHUC, goi.length - viTri));
        dos.writeInt(goi.length);
        dos.writeInt(viTri);
        dos.writeShort(len);
        dos.write(goi, viTri, len);
    }

    /** So khuc cua mot goi. */
    public static int soKhuc(final int tong) {
        return tong <= 0 ? 1 : (tong + KHUC - 1) / KHUC;
    }

    /** -126 kieu 1: bang loai day du. */
    public static void ghiBangLoai(final DataOutputStream dos, final LoaiDuLieu d) throws IOException {
        dos.writeByte(BANG_LOAI);
        dos.writeInt(d.bam());
        final java.util.Collection<LoaiDuLieu.Loai> ds = d.tatCa();
        dos.writeShort(ds.size());
        for (final LoaiDuLieu.Loai x : ds) {
            dos.writeShort(x.dex);
            dos.writeUTF(x.ten);
            dos.writeByte(x.he1);
            dos.writeByte(x.he2);
            dos.writeByte(x.bac);
            dos.writeByte(x.theHe);
            dos.writeByte(LoaiDuLieu.coClient(x));
        }
    }

    public static void ghiBangKhongDoi(final DataOutputStream dos, final int bam) throws IOException {
        dos.writeByte(BANG_KHONG_DOI);
        dos.writeInt(bam);
    }

    /** -126 kieu 3: cac bo {dex, co}. */
    public static void ghiPokedex(final DataOutputStream dos, final List<int[]> ds) throws IOException {
        dos.writeByte(POKEDEX);
        final int n = Math.min(65535, ds.size());
        dos.writeShort(n);
        for (int i = 0; i < n; i++) {
            dos.writeShort(ds.get(i)[0]);
            dos.writeByte(ds.get(i)[1]);
        }
    }

    // ------------------------------------------------------------------ goi sprite may chu

    /** Mot tep trong goi. */
    public static final class Tep {
        public final String ten;
        public final byte[] du;

        Tep(final String ten, final byte[] du) {
            this.ten = ten;
            this.du = du;
        }
    }

    /** Doc goi .goi; null neu hong (sai ma, sai phien ban, sai dex, cat cut, khong co .dl). */
    public static List<Tep> docGoi(final byte[] b, final int dexMongDoi) {
        if (b == null || b.length < 10) {
            return null;
        }
        try {
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
            if (in.readByte() != 'H' || in.readByte() != 'T' || in.readByte() != 'P' || in.readByte() != 'K') {
                return null;
            }
            if (in.readUnsignedByte() != 1) {
                return null;
            }
            final int dex = in.readShort();
            in.readUnsignedByte();
            if (dexMongDoi > 0 && dex != dexMongDoi) {
                return null;
            }
            final int n = in.readUnsignedByte();
            final List<Tep> ds = new ArrayList<Tep>();
            boolean coDl = false;
            for (int i = 0; i < n; i++) {
                final String ten = in.readUTF();
                final int len = in.readInt();
                if (len < 0 || len > b.length) {
                    return null;
                }
                final byte[] du = new byte[len];
                in.readFully(du);
                ds.add(new Tep(ten, du));
                coDl |= ten.endsWith(".dl");
            }
            return coDl ? ds : null;
        } catch (final IOException e) {
            return null;
        }
    }

    /** Ten tep goi may chu cua loai (4 chu so, nhu client_jar/res/pk). */
    public static String tenGoi(final int dex) {
        final String s = String.valueOf(dex);
        return "0000".substring(Math.min(4, s.length())) + s + ".goi";
    }

    /** Ten tep goi theo sac mau: "0016.goi" / ban shiny "0016s.goi" (tools/pokemon/loai/DongGoiLoai --shiny). */
    public static String tenGoi(final int dex, final boolean shiny) {
        final String t = tenGoi(dex);
        return shiny ? t.substring(0, t.length() - 4) + "s.goi" : t;
    }

    /** Co shiny ghi trong dau goi (byte 7); -1 neu hong. */
    public static int shinyCuaGoi(final byte[] b) {
        return b == null || b.length < 10 || b[0] != 'H' || b[1] != 'T' || b[2] != 'P' || b[3] != 'K' ? -1 : b[7] & 0xFF;
    }

    // ------------------------------------------------------------------ gioi han bang thong

    /** Thung token theo byte: nap {@code byteGiay} moi giay, toi da 1 giay du tru. */
    public static final class ThungToken {
        private final long byteGiay;
        private double con;
        private long lan;

        public ThungToken(final long byteGiay, final long bayGioMs) {
            this.byteGiay = Math.max(1, byteGiay);
            this.con = this.byteGiay;
            this.lan = bayGioMs;
        }

        public synchronized boolean lay(final int soByte, final long bayGioMs) {
            final long dt = Math.max(0, bayGioMs - lan);
            lan = bayGioMs;
            con = Math.min(byteGiay, con + dt * byteGiay / 1000.0);
            if (con >= soByte) {
                con -= soByte;
                return true;
            }
            return false;
        }
    }
}
