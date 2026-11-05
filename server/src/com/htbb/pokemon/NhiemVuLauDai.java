package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhiem vu theo thiet ke dai han (docs/thiet-ke-game.md, GD2 buoc 4). Lop THUAN.
 *
 *   - ve chuong: LAM DAY moi ngay 05:00 (toi da 4, bat dau 4 ve) - truoc 05/11/2026 la 2 ve / tuan (reset thu Hai), nguoi
 *     choi len cap nhanh ket chuoi chinh ca tuan (bao loi Snorlax C07.1 "het ve chuong, sang tuan moi lam tiep");
 *   - loc nhiem vu phu theo cap truoc khi gui (danh sach ghi so luong bang 1 byte <= 127): nhiem vu co
 *     cap - 15 <= lv <= cap + 3 (chu game 31/10/2026, ca nhiem vu da vuot cap ma chua lam), xep gan cap nguoi choi nhat
 *     truoc, cung do lech thi thap truoc, roi theo thu tu nhiem vu (thi tran); gui het (toi da PHU_TOI_DA). Nhan theo hopCap (truoc
 *     toi 10 cap, khong chan duoi - So tay).
 *   - bang tuan: 2 nhiem vu / tuan theo bac cap.
 */
public final class NhiemVuLauDai {

    /** Gioi han cung cua goi (writeByte(size); client J2ME doc byte CO DAU -> toi da 127). */
    public static final int GOI_TOI_DA = 127;
    /**
     * So nhiem vu phu toi da gui xuong client cung luc (danh sach NPC -64). Chu game 31/10/2026: gui HET cua so cap - 15 ..
     * cap + 3 (bang that toi da 117 / cap) - chi chua cho dong chinh + doi trong goi; moi NPC giao chi hien nhiem vu cua minh.
     */
    public static final int PHU_TOI_DA = GOI_TOI_DA - 2;
    /**
     * Danh sach NPC (-64) hien nhiem vu phu cap {@code cap - NPC_DUOI .. cap + NPC_TREN} (chu game 31/10/2026, bot cb2: truoc day
     * 20 nhiem vu gan cap nhat trong khoang toi cap + 10 - nguoi choi vuot cap nhiem vu ~3 cap la khong thay o NPC nua, chi
     * con trong So tay). Nhan van theo {@link #hopCap} (truoc toi 10 cap, khong chan duoi).
     */
    public static final int NPC_TREN = 3, NPC_DUOI = 15;
    public static final int DAI_CAP = 10;

    /** Moc tuan: thu Hai 05:00 (gio may). */
    public static final int GIO_DOI_TUAN = 5;

    private NhiemVuLauDai() {
    }

    /** Chi so tuan (tang dan) - doi luc thu Hai 05:00 gio may. */
    public static int tuan(final long ms) {
        // lui 5 gio roi tinh ngay dia phuong; 1970-01-05 (ngay thu 4) la thu Hai
        final long t = ms - GIO_DOI_TUAN * 3600000L;
        final long ngay = Math.floorDiv(t + java.util.TimeZone.getDefault().getOffset(t), 86400000L);
        return (int) Math.floorDiv(ngay - 4L, 7L);
    }

    /** Chi so ngay cho ve chuong - doi luc 05:00 gio may (cung moc gio voi tuan). */
    public static int ngayVe(final long ms) {
        final long t = ms - GIO_DOI_TUAN * 3600000L;
        return (int) Math.floorDiv(t + java.util.TimeZone.getDefault().getOffset(t), 86400000L);
    }

    /**
     * Ve chuong lam DAY moi ngay (chu game 03/11/2026 "đợi 1 ngày là được"; quyet dinh 05/11/2026): sang ngay moi (hoac chua tung
     * lam day theo ngay - ho so truoc ban nay, {@code ngayCuoi <= 0}) -> ve = max(ve, VE_CHUONG_TOI_DA). Cung ngay: giu nguyen.
     * Chuoi chinh khong con bi chan ca tuan; van chan theo cap + nhiem vu truoc (cot truyen).
     * @return {ve moi, ngay moi}
     */
    public static int[] capNhatVeNgay(final int ve, final int ngayCuoi, final int ngayNay) {
        if (ngayCuoi > 0 && ngayNay <= ngayCuoi) {
            return new int[]{ve, ngayCuoi};
        }
        return new int[]{Math.max(Math.max(0, ve), ThongSoLauDai.VE_CHUONG_TOI_DA), ngayNay};
    }

    /** Cau bao het ve (kiemChinh). */
    public static String chuHetVe() {
        return "Bạn đã dùng hết " + ThongSoLauDai.VE_CHUONG_TOI_DA + " vé chương hôm nay. Vé làm mới lúc 05:00 sáng mai.";
    }

    /**
     * (CU, giu cho ban quay lui / kiem) Cap nhat ve chuong khi sang tuan moi: +2 moi tuan da qua, tran 4.
     * @return {ve moi, tuan moi}
     */
    public static int[] capNhatVe(final int ve, final int tuanCuoi, final int tuanNay) {
        if (tuanCuoi <= 0) {
            return new int[]{Math.max(ve, 0), tuanNay};
        }
        if (tuanNay <= tuanCuoi) {
            return new int[]{ve, tuanCuoi};
        }
        final long them = (long) (tuanNay - tuanCuoi) * ThongSoLauDai.VE_CHUONG_TUAN;
        final int moi = (int) Math.min(ThongSoLauDai.VE_CHUONG_TOI_DA, Math.max(0, ve) + them);
        return new int[]{moi, tuanNay};
    }

    /**
     * Nhiem vu phu (cap {@code lv}) co moi nguoi choi cap {@code cap} khong: lv <= cap + DAI_CAP, khong chan duoi (chu game
     * 30/10/2026: nguoi len cap nhanh khong mat vinh vien nhiem vu phu cap thap; thuong giu nguyen o cap cua nhiem vu).
     */
    public static boolean hopCap(final int lv, final int cap) {
        return lv <= cap + DAI_CAP;
    }

    /** Danh sach NPC (-64) co hien nhiem vu phu cap {@code lv} cho nguoi choi cap {@code cap} khong (ke ca nhiem vu da vuot cap). */
    public static boolean hienNpc(final int lv, final int cap) {
        return lv <= cap + NPC_TREN && lv >= cap - NPC_DUOI;
    }

    /** Thu tu hien: gan cap nguoi choi nhat truoc; cung do lech -> nhiem vu thap hon truoc; roi thu tu goc (thi tran). */
    public static List<Integer> xepGanCap(final int[] lv, final int cap, final List<Integer> chiSo) {
        final List<Integer> kq = new ArrayList<Integer>(chiSo);
        java.util.Collections.sort(kq, new java.util.Comparator<Integer>() {
            @Override
            public int compare(final Integer a, final Integer b) {
                final int da = Math.abs(lv[a.intValue()] - cap), db = Math.abs(lv[b.intValue()] - cap);
                if (da != db) {
                    return da < db ? -1 : 1;
                }
                if (lv[a.intValue()] != lv[b.intValue()]) {
                    return lv[a.intValue()] < lv[b.intValue()] ? -1 : 1;
                }
                return a.compareTo(b);
            }
        });
        return kq;
    }

    /**
     * Chon nhiem vu phu de gui (goi -64, client cu khong co trang): moi muc hopCap, xep gan cap nhat truoc, toi da
     * {@code toiDa} (xong / bo bot muc gan thi muc xa hon hien len). Day du + trang: So tay -> Nhan nhiem vu phu.
     * @return chi so (trong mang lv) duoc chon
     */
    public static List<Integer> locPhu(final int[] lv, final int cap, final int toiDa) {
        final List<Integer> hop = new ArrayList<Integer>();
        for (int i = 0; i < lv.length; i++) {
            if (hienNpc(lv[i], cap)) { // chu game 31/10/2026: cap - 15 .. cap + 3 (truoc: hopCap, toi cap + 10, 20 gan nhat)
                hop.add(Integer.valueOf(i));
            }
        }
        final List<Integer> kq = xepGanCap(lv, cap, hop);
        return kq.size() > toiDa ? new ArrayList<Integer>(kq.subList(0, toiDa)) : kq;
    }

    /**
     * Nhiem vu chinh co nhan duoc khong.
     * @return null neu duoc, nguoc lai ly do (de bao cho nguoi choi)
     */
    public static String kiemChinh(final int lvYeuCau, final int cap, final int ngayMo, final int ngayHienTai,
            final boolean canVe, final int ve) {
        if (cap < lvYeuCau) {
            return "Cần đạt cấp " + lvYeuCau + " để nhận nhiệm vụ này.";
        }
        if (ngayMo > ngayHienTai) {
            return "Chương này mở sau " + (ngayMo - ngayHienTai) + " ngày nữa.";
        }
        if (canVe && ve <= 0) {
            return chuHetVe();
        }
        return null;
    }

    /** Bac nhiem vu tuan/ngay theo cap (1..4): 1-29 -> 1, 30-54 -> 2, 55-79 -> 3, 80+ -> 4. */
    public static int bacCap(final int cap) {
        if (cap < 30) {
            return 1;
        }
        if (cap < 55) {
            return 2;
        }
        if (cap < 80) {
            return 3;
        }
        return 4;
    }

    /**
     * Chi so nhiem vu chinh dau tien (theo thu tu chuoi) phu hop cap {@code cap} cho
     * nhan vat CU khi chuyen sang bang data_quest_moi: bo qua cac chuong cap thap hon cap
     * hien tai, vao dau chuong co lv lon nhat <= cap. {@code batDauChuong[i]} = true neu
     * nhiem vu i la buoc 1 cua chuong (hoac buoc dau huong dan).
     */
    public static int chiSoChinhChoCap(final int[] lv, final boolean[] batDauChuong, final int cap) {
        int chon = 0;
        for (int i = 0; i < lv.length; i++) {
            if (batDauChuong[i] && lv[i] <= cap) {
                chon = i;
            }
        }
        return chon;
    }
}
