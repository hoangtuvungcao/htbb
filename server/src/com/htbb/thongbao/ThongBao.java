package com.htbb.thongbao;

import io.Message;
import java.io.IOException;

/**
 * THONG BAO CHO NGUOI CHOI (0.9.6.1): MOT cho dung cau + chon cach hien cho moi thong bao "ket qua" moi viet.
 *
 * <p>Phan loai (client game.pokemon.ThongBao dung cung ma):
 * <ul>
 *   <li>{@link #KET_QUA} - ket qua mot viec vua lam (cau ca, dao, che tao, nhan do): the nhanh xanh la, 2,5-6 giay;</li>
 *   <li>{@link #LOI} - khong lam duoc (thieu xu, het the luc...): the nhanh do, 4-6,5 giay;</li>
 *   <li>{@link #TIN} - tin thuong (gap Pokemon, nhac nho): the nhanh xanh duong, 3-8 giay;</li>
 *   <li>{@link #HIEM} - ket qua hiem / dang nho (do hiem, shiny, len cap): the vang noi bat, 5-8 giay;</li>
 *   <li>{@link #SU_KIEN} - tin toan may chu (boss xuat hien / bi ha, su kien): bieu ngu tren cung 6-8 giay.</li>
 * </ul>
 * Hop thoai chan (bam OK) chi danh cho viec can quyet dinh / nghiem trong - khong gui qua lop nay.
 *
 * <p>Goi tin: lenh -104 (real.DungVatPham, client ban &gt;= 902): byte loai, UTF chu, byte kieu thao tac (0), roi PHAN
 * THEM 0.9.6.1 (client cu bo qua): byte hinh ({@link #HINH_KHONG} / {@link #HINH_ICON} + short id icon /
 * {@link #HINH_POKEMON} + short dex / {@link #HINH_BIEU_TUONG} + byte ma BieuTuong). Chu co toi da 2 dong cach nhau
 * '\n': dong 1 = y chinh (ngan), dong 2 = thong tin phu (vd "The luc con 176"). Client cu hon 902 (Unity) nhan hop
 * thoai 37 voi hai dong noi lai thanh mot cau.
 */
public final class ThongBao {

    public static final byte KET_QUA = 0, LOI = 1, TIN = 2, HIEM = 3, SU_KIEN = 4;
    public static final byte HINH_KHONG = 0, HINH_ICON = 1, HINH_POKEMON = 2, HINH_BIEU_TUONG = 3;
    /** Ma BieuTuong cua client (game.pokemon.BieuTuong) hay dung o day. */
    public static final int BT_SINH_THAI = 7, BT_SU_KIEN = 6, BT_DEX = 4, BT_NGUYEN_LIEU = 28;

    /** Lenh -104 (real.DungVatPham.LENH) va ban client dau tien biet lenh (real.DungVatPham.BAN_TOAST). */
    public static final int LENH = -104, BAN_TOAST = 902;

    private ThongBao() {
    }

    // ================================================================== goi tin

    /** Hai dong -> chu goi tin ("chinh\nphu"; phu rong = mot dong). */
    public static String noi(final String chinh, final String phu) {
        final String c = chinh == null ? "" : chinh.trim();
        if (phu == null || phu.trim().isEmpty()) {
            return c;
        }
        return c + "\n" + phu.trim();
    }

    /** Ban cho client cu (hop thoai 37): hai dong thanh mot cau, dong 1 khong co dau cau thi them ". ". */
    public static String motCau(final String chinh, final String phu) {
        final String c = chinh == null ? "" : chinh.trim();
        if (phu == null || phu.trim().isEmpty()) {
            return c;
        }
        final char cuoi = c.isEmpty() ? '.' : c.charAt(c.length() - 1);
        final boolean coDau = cuoi == '.' || cuoi == '!' || cuoi == '?' || cuoi == ':';
        return c + (coDau ? " " : ". ") + phu.trim();
    }

    /** Goi -104 day du (chu da doi qua ChuPokemon). */
    public static Message tao(final byte loai, final String chinh, final String phu, final byte hinh, final int id)
            throws IOException {
        final Message m = new Message(LENH);
        m.dos.writeByte(loai);
        m.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi(noi(chinh, phu)));
        m.dos.writeByte(0); // kieu thao tac: KHAC (client chi bo qua thong bao "dung binh" luc tu danh)
        m.dos.writeByte(hinh);
        if (hinh == HINH_ICON || hinh == HINH_POKEMON) {
            m.dos.writeShort(id);
        } else if (hinh == HINH_BIEU_TUONG) {
            m.dos.writeByte(id);
        }
        return m;
    }

    public static boolean coToast(final real.Char p) {
        return p != null && p.getSession() != null && p.getSession().banClient >= BAN_TOAST;
    }

    /** Gui mot thong bao cho mot nguoi choi: client moi = the nhanh dung loai; client cu = hop thoai 37. */
    public static void gui(final real.Char p, final byte loai, final String chinh, final String phu, final byte hinh,
            final int id) {
        if (p == null || chinh == null || chinh.trim().isEmpty()) {
            return;
        }
        try {
            if (coToast(p)) {
                p.sendMessage(tao(loai, chinh, phu, hinh, id));
            } else {
                p.sendMessage(real.MessageCreator.createServerAlertMessage(motCau(chinh, phu), ""));
            }
        } catch (final Exception ignored) {
        }
    }

    /**
     * 0.9.8: tin RIENG cua mot nguoi (truoc gui chu chay -26 -> client moi hien thanh bieu ngu "TOAN MAY CHU"). Client moi:
     * the nhanh dung loai (LOI / KET_QUA / TIN theo noi dung); client cu: chu chay -26 nhu truoc. Tin toan may chu that
     * (boss, gio vang, chiem Gym...) van dung -26 / ThongBaoGop.
     */
    public static void rieng(final real.Char p, final String s) throws java.io.IOException {
        if (p == null || s == null || s.trim().isEmpty()) {
            return;
        }
        if (!coToast(p)) {
            p.sendMessage(real.MessageCreator.createServerAlertAutoOffMessage(s));
            return;
        }
        gui(p, loaiRieng(s), s, "", HINH_KHONG, 0);
    }

    /** Loai the cho tin rieng theo noi dung (thuan, test). */
    public static byte loaiRieng(final String s) {
        final String t = s == null ? "" : s.trim().toLowerCase();
        if (t.startsWith("không") || t.startsWith("bạn không") || t.contains("không thể") || t.startsWith("chưa thể")
                || t.startsWith("hiệu ứng vẫn còn") || t.contains("chưa đủ") || t.startsWith("bạn phải") || t.contains("thất bại")
                || t.contains("đã thua")) {
            return LOI;
        }
        if (t.contains("nhận") || t.contains("chúc mừng") || t.contains("thành công") || t.contains("chiến thắng")
                || t.contains("tinh thông +") || t.startsWith("mở khoá") || t.startsWith("đã mở")) {
            return KET_QUA;
        }
        return TIN;
    }

    public static void ketQua(final real.Char p, final String chinh, final String phu) {
        gui(p, KET_QUA, chinh, phu, HINH_KHONG, 0);
    }

    public static void loi(final real.Char p, final String chinh, final String phu) {
        gui(p, LOI, chinh, phu, HINH_KHONG, 0);
    }

    // ================================================================== cau chu (thuan - kiem thu duoc)

    /** "Vảy Magikarp ×1" - ten roi so luong, khong co "bac". */
    public static String soLuong(final String ten, final int so) {
        return ten + " ×" + Math.max(1, so);
    }

    /** Dong phu "Thể lực còn 176" (the luc sinh hoat). */
    public static String theLuc(final int con) {
        return "Thể lực còn " + Math.max(0, con);
    }

    /** Cau ca / dao / che tao: dong 1. hiem = gia tri cao (them chu "hiếm" de nguoi choi biet day la do quy). */
    public static String duoc(final String viec, final String ten, final int so, final boolean hiem) {
        return viec + (hiem ? " đồ hiếm: " : " ") + soLuong(ten, so);
    }

    /** Gap Pokemon khi cau / dao (rieng mot thong bao, khong dinh vao cau ket qua). */
    public static String gap(final String loai, final boolean hiem) {
        return hiem ? "Gặp Pokémon hiếm: " + loai + "!" : "Bạn vừa gặp " + loai + "!";
    }

    public static final String GAP_PHU = "Đã ghi vào Pokédex";

    /** Ten boss co "Boss " dang truoc (mot lan). */
    public static String tenBoss(final String ten) {
        final String t = ten == null ? "" : ten.trim();
        if (t.isEmpty()) {
            return "Boss";
        }
        return t.toLowerCase(java.util.Locale.ROOT).startsWith("boss") ? t : "Boss " + t;
    }

    /** "Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!" (thieu ban do: "... đã xuất hiện!"). */
    public static String bossXuatHien(final String ten, final String banDo) {
        return tenBoss(ten) + " đã xuất hiện" + (banDo == null || banDo.trim().isEmpty() ? "" : " ở " + banDo.trim()) + "!";
    }

    public static String bossBienMat(final String ten, final String banDo) {
        return tenBoss(ten) + " đã rời" + (banDo == null || banDo.trim().isEmpty() ? " đi" : " " + banDo.trim())
                + ". Hẹn lần sau!";
    }

    public static String bossBiHa(final String ten, final String nguoi, final String banDo) {
        return nguoi + " đã hạ gục " + tenBoss(ten)
                + (banDo == null || banDo.trim().isEmpty() ? "" : " ở " + banDo.trim()) + "!";
    }

    /** Ten ban do cua boss (null an toan). */
    public static String banDo(final real.Map m) {
        try {
            return m == null ? "" : real.Map.getNameMap(m.mapId);
        } catch (final Throwable e) {
            return "";
        }
    }
}
