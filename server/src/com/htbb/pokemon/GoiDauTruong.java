package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * GOI -109 DAU TRUONG BAO BOI (client &gt;= 908). Lop THUAN ghi tung kieu goi may chu -&gt; client theo DUNG thu tu
 * client_jar/src/game/pokemon/DauTruong.java doc (test/KiemDauTruong + client_pc/test/KiemDauTruong).
 *
 * <pre>
 * S-&gt;C (byte kieu dau):
 *  0 TRANG_THAI  byte mo | short mua | short ngayConLai | short diem | byte bac | UTF tenBac | int hang | short thang
 *                | short thua | byte luotCon | byte luotNgay | short luongTuan | short tranLuongTuan | byte lamMoiCon
 *                | byte giayLuot | byte nCong + int id.. | byte nThu + int id.. | int sucCong | int sucThu
 *                | short nKho + {int id, short dex, byte cap, byte shiny, byte sao, byte ivPt, short suc50, byte he1,
 *                  byte he2, byte camLy (0 dung duoc, 1 Huyen thoai), UTF ten} | UTF ghiChu
 *  1 DOI_THU     byte n + {int idChar, UTF ten, short diem, byte bac, int suc, byte daGap, 3 x (short dex, byte shiny,
 *                byte sao)} | byte lamMoiCon | UTF ghiChu
 *  2 BAT_DAU     int tran | UTF tenDoiThu | short diemDoiThu | byte giayLuot | byte luotToiDa
 *                | 2 ben x 3 con {short dex, byte shiny, UTF ten, byte he1, byte he2, short hpMax}
 *                | 3 con cua minh x 4 chieu {UTF ten, byte he, byte loai, byte uyLuc x soDon, byte ppMax}
 *                | KHOI TRANG THAI
 *  3 LUOT        int tran | short luot | byte nDong + UTF.. | KHOI TRANG THAI
 *  4 KET_QUA     int tran | byte thang | short diemTruoc | short diemSau | short diemThuDoi | short luong | short heSo
 *                | byte luotCon | UTF chu
 *  5 GHI_CHU     UTF chu
 * KHOI TRANG THAI: 2 ben x {byte dangRa, 3 x (short hp, byte trangThai)} | 3 x 4 byte pp (cua minh) | byte xong
 *                | byte giayConLai | byte tuDong
 * C-&gt;S: 0 mo | 1 luu doi (byte loai 0 cong / 1 thu, byte n, int id..) | 2 tim (byte lamMoi) | 3 thach (int idChar)
 *       | 4 hanh dong (int tran, byte loai 0 chieu / 1 doi, byte o) | 5 tu dong (int tran, byte bat) | 6 dau hang (int tran)
 * </pre>
 */
public final class GoiDauTruong {

    public static final int LENH = -109;
    public static final int BAN_CLIENT = 908;
    public static final int S_TRANG_THAI = 0, S_DOI_THU = 1, S_BAT_DAU = 2, S_LUOT = 3, S_KET_QUA = 4, S_GHI_CHU = 5;
    public static final int C_MO = 0, C_LUU_DOI = 1, C_TIM = 2, C_THACH = 3, C_HANH_DONG = 4, C_TU_DONG = 5, C_DAU_HANG = 6;
    /** Toi da con trong kho gui xuong (Hop PC toi da 100). */
    public static final int KHO_TOI_DA = 120;
    public static final int DONG_TOI_DA = 24;

    private GoiDauTruong() {
    }

    /** Mot con trong kho chon doi. */
    public static final class KhoCon {
        public int id, dex, cap, sao, ivPt, suc50, he1, he2, camLy;
        public boolean shiny;
        public String ten = "";
    }

    /** Trang thai man Dau truong. */
    public static final class TrangThai {
        public boolean mo;
        public int mua, ngayConLai, diem, bac, hang, thang, thua, luotCon, luotNgay, luongTuan, tranLuongTuan, lamMoiCon;
        public int giayLuot = DauTruongLogic.GIAY_LUOT;
        public int[] cong = new int[0], thu = new int[0];
        public int sucCong, sucThu;
        public String tenBac = "", ghiChu = "";
        public KhoCon[] kho = new KhoCon[0];
    }

    /** Mot doi thu de xuat. */
    public static final class DoiThu {
        public int id, diem, bac, suc, daGap;
        public String ten = "";
        public int[] dex = new int[3], sao = new int[3];
        public boolean[] shiny = new boolean[3];
    }

    static int s16(final int x) {
        return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, x));
    }

    static int u8(final int x) {
        return Math.max(0, Math.min(255, x));
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() <= n ? s : s.substring(0, n);
    }

    public static void trangThai(final DataOutputStream o, final TrangThai t) throws IOException {
        o.writeByte(S_TRANG_THAI);
        o.writeByte(t.mo ? 1 : 0);
        o.writeShort(s16(t.mua));
        o.writeShort(s16(t.ngayConLai));
        o.writeShort(s16(t.diem));
        o.writeByte(u8(t.bac));
        o.writeUTF(cat(t.tenBac, 32));
        o.writeInt(t.hang);
        o.writeShort(s16(t.thang));
        o.writeShort(s16(t.thua));
        o.writeByte(u8(t.luotCon));
        o.writeByte(u8(t.luotNgay));
        o.writeShort(s16(t.luongTuan));
        o.writeShort(s16(t.tranLuongTuan));
        o.writeByte(u8(t.lamMoiCon));
        o.writeByte(u8(t.giayLuot));
        ids(o, t.cong);
        ids(o, t.thu);
        o.writeInt(t.sucCong);
        o.writeInt(t.sucThu);
        final int n = Math.min(KHO_TOI_DA, t.kho.length);
        o.writeShort(n);
        for (int i = 0; i < n; i++) {
            final KhoCon c = t.kho[i];
            o.writeInt(c.id);
            o.writeShort(s16(c.dex));
            o.writeByte(u8(c.cap));
            o.writeByte(c.shiny ? 1 : 0);
            o.writeByte(u8(c.sao));
            o.writeByte(u8(c.ivPt));
            o.writeShort(s16(c.suc50));
            o.writeByte(c.he1);
            o.writeByte(c.he2);
            o.writeByte(u8(c.camLy));
            o.writeUTF(cat(c.ten, 24));
        }
        o.writeUTF(cat(t.ghiChu, 200));
    }

    static void ids(final DataOutputStream o, final int[] ids) throws IOException {
        final int n = ids == null ? 0 : Math.min(DauTruongTran.SO_CON, ids.length);
        o.writeByte(n);
        for (int i = 0; i < n; i++) {
            o.writeInt(ids[i]);
        }
    }

    public static void doiThu(final DataOutputStream o, final List<DoiThu> ds, final int lamMoiCon, final String ghiChu)
            throws IOException {
        o.writeByte(S_DOI_THU);
        final int n = Math.min(DauTruongLogic.SO_DE_XUAT, ds.size());
        o.writeByte(n);
        for (int i = 0; i < n; i++) {
            final DoiThu d = ds.get(i);
            o.writeInt(d.id);
            o.writeUTF(cat(d.ten, 24));
            o.writeShort(s16(d.diem));
            o.writeByte(u8(d.bac));
            o.writeInt(d.suc);
            o.writeByte(u8(d.daGap));
            for (int k = 0; k < 3; k++) {
                o.writeShort(s16(d.dex[k]));
                o.writeByte(d.shiny[k] ? 1 : 0);
                o.writeByte(u8(d.sao[k]));
            }
        }
        o.writeByte(u8(lamMoiCon));
        o.writeUTF(cat(ghiChu, 200));
    }

    public static void batDau(final DataOutputStream o, final int tranId, final String tenDoiThu, final int diemDoiThu,
            final int giayLuot, final DauTruongTran t, final int giayConLai, final boolean tuDong) throws IOException {
        o.writeByte(S_BAT_DAU);
        o.writeInt(tranId);
        o.writeUTF(cat(tenDoiThu, 24));
        o.writeShort(s16(diemDoiThu));
        o.writeByte(u8(giayLuot));
        o.writeByte(u8(DauTruongTran.LUOT_TOI_DA));
        for (int ben = 0; ben < 2; ben++) {
            for (int i = 0; i < DauTruongTran.SO_CON; i++) {
                final DauTruongTran.Con c = t.doi[ben][i];
                o.writeShort(s16(c.dex));
                o.writeByte(c.shiny ? 1 : 0);
                o.writeUTF(cat(c.ten, 24));
                o.writeByte(c.he1);
                o.writeByte(c.he2);
                o.writeShort(s16(c.hpMax));
            }
        }
        for (int i = 0; i < DauTruongTran.SO_CON; i++) {
            final DauTruongTran.Con c = t.doi[DauTruongTran.CONG][i];
            for (int k = 0; k < 4; k++) {
                final DauTruongDuLieu.Chieu m = c.chieu[k];
                o.writeUTF(cat(m.ten, 32));
                o.writeByte(m.he);
                o.writeByte(m.loai);
                o.writeByte(u8(m.uyLuc * m.soDon));
                o.writeByte(u8(m.ppToiDa));
            }
        }
        khoi(o, t, giayConLai, tuDong);
    }

    public static void luot(final DataOutputStream o, final int tranId, final DauTruongTran t, final List<String> dong,
            final int giayConLai, final boolean tuDong) throws IOException {
        o.writeByte(S_LUOT);
        o.writeInt(tranId);
        o.writeShort(s16(t.luot));
        final int tu = Math.max(0, dong.size() - DONG_TOI_DA);
        o.writeByte(dong.size() - tu);
        for (int i = tu; i < dong.size(); i++) {
            o.writeUTF(cat(dong.get(i), 160));
        }
        khoi(o, t, giayConLai, tuDong);
    }

    static void khoi(final DataOutputStream o, final DauTruongTran t, final int giayConLai, final boolean tuDong) throws IOException {
        for (int ben = 0; ben < 2; ben++) {
            o.writeByte(t.dangRa[ben]);
            for (int i = 0; i < DauTruongTran.SO_CON; i++) {
                final DauTruongTran.Con c = t.doi[ben][i];
                o.writeShort(s16(c.hp));
                o.writeByte(c.trangThai);
            }
        }
        for (int i = 0; i < DauTruongTran.SO_CON; i++) {
            for (int k = 0; k < 4; k++) {
                o.writeByte(u8(t.doi[DauTruongTran.CONG][i].pp[k]));
            }
        }
        o.writeByte(t.xong() ? 1 : 0);
        o.writeByte(u8(giayConLai));
        o.writeByte(tuDong ? 1 : 0);
    }

    public static void ketQua(final DataOutputStream o, final int tranId, final boolean thang, final int diemTruoc, final int diemSau,
            final int diemThuDoi, final int luong, final int heSo, final int luotCon, final String chu) throws IOException {
        o.writeByte(S_KET_QUA);
        o.writeInt(tranId);
        o.writeByte(thang ? 1 : 0);
        o.writeShort(s16(diemTruoc));
        o.writeShort(s16(diemSau));
        o.writeShort(s16(diemThuDoi));
        o.writeShort(s16(luong));
        o.writeShort(s16(heSo));
        o.writeByte(u8(luotCon));
        o.writeUTF(cat(chu, 200));
    }

    public static void ghiChu(final DataOutputStream o, final String chu) throws IOException {
        o.writeByte(S_GHI_CHU);
        o.writeUTF(cat(chu, 200));
    }
}
