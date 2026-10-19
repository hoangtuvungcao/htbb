package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Luat nap tien - HAM THUAN (khong CSDL, khong Char), test/KiemNapTien.java goi truc tiep.
 * Nguon so lieu DUY NHAT phia server; web (HTBB-Web server/utils/napTienThuan.ts) giu ban sao
 * cac hang so can thiet va sql/pokemon_nap_tien.sql giu ban sao danh muc - test doi chieu ca ba.
 * Thiet ke: docs/van-hanh.md.
 *
 * Nguyen tac (N-NAP):
 *   1. Ban tiet kiem thoi gian / tien ich / trang tri, KHONG ban suc manh. Thoi gian = nhom "tang_toc"
 *      (ve EXP, bua the luc, uy thac, keo hiem - danh sach CO DINH TangToc.MA, gia + gioi han tuan co dinh,
 *      dung chung tran diem ngay/tuan voi ban khoa kiem bang choi; chi EXP, khong xu/vat pham/chi so).
 *   2. Luong (tien cao cap) chi vao qua nap (+ thanh tuu nho co tran). Moi luong tieu bi dot.
 *      Khong doi luong -> xu o bat ky NPC nao.
 *   3. Ty gia CO DINH 10 luong / 1.000 d, khong he so ngau nhien, khong khuyen mai % luong.
 *   4. Mua ban hang cao cap giua nguoi choi chi qua Cho trang tri co thue (6% dot + 1% phi dang).
 *   5. Tran nap tuan/thang theo tai khoan (bao ve nguoi choi), tu dat tran thap hon duoc.
 */
public final class NapTienLogic {

    private NapTienLogic() {
    }

    // ================================================================== ty gia, goi

    /** 10 luong cho moi 1.000 d. KHONG he so, KHONG khuyen mai. */
    public static final int LUONG_MOI_1000_VND = 10;

    public static final String LOAI_LUONG = "luong";
    public static final String LOAI_THE_THANG = "the_thang";
    public static final String LOAI_VE_MUA = "ve_mua";
    /** Thành viên HTBB (theo TÀI KHOẢN): web cấp thẳng khi xác nhận thanh toán, game KHÔNG cộng gì (ThanhVien). */
    public static final String LOAI_THANH_VIEN = "thanh_vien";

    /** Mot goi ban tren web (bang nap_goi). */
    public static final class Goi {
        public final String ma, ten, loai;
        public final int giaVnd, luong;

        public Goi(final String ma, final String ten, final String loai, final int giaVnd, final int luong) {
            this.ma = ma;
            this.ten = ten;
            this.loai = loai;
            this.giaVnd = giaVnd;
            this.luong = luong;
        }
    }

    /** Danh muc mac dinh - trung 100% voi INSERT trong sql/pokemon_nap_tien.sql (test doi chieu). */
    public static final Goi[] GOI_MAC_DINH = {
        new Goi("L20", "200 lượng", LOAI_LUONG, 20000, 200),
        new Goi("L50", "500 lượng", LOAI_LUONG, 50000, 500),
        new Goi("L100", "1.000 lượng", LOAI_LUONG, 100000, 1000),
        new Goi("L200", "2.000 lượng", LOAI_LUONG, 200000, 2000),
        new Goi("L500", "5.000 lượng", LOAI_LUONG, 500000, 5000),
        new Goi("THE_THANG", "Thẻ tháng (30 ngày)", LOAI_THE_THANG, 50000, 0),
        new Goi("VE_MUA", "Vé mùa cao cấp", LOAI_VE_MUA, 99000, 0),
    };

    /** Luong dung ty gia co dinh cho mot muc gia. */
    public static int luongTheoGia(final int giaVnd) {
        if (giaVnd <= 0) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, (long) giaVnd * LUONG_MOI_1000_VND / 1000L);
    }

    /**
     * Mot dong nap_goi co hop le khong (admin sua tren web cung qua luat nay - ban TS).
     * Tra ve null neu hop le, khong thi ly do.
     */
    public static String kiemGoi(final String loai, final int giaVnd, final int luong) {
        if (giaVnd < 10000 || giaVnd > 2000000) {
            return "gia phai trong 10.000 - 2.000.000 d";
        }
        if (giaVnd % 1000 != 0) {
            return "gia phai chan nghin";
        }
        if (LOAI_LUONG.equals(loai)) {
            if (luong != luongTheoGia(giaVnd)) {
                return "goi luong phai dung ty gia " + LUONG_MOI_1000_VND + " luong / 1.000 d (khong khuyen mai %)";
            }
            return null;
        }
        if (LOAI_THE_THANG.equals(loai) || LOAI_VE_MUA.equals(loai) || LOAI_THANH_VIEN.equals(loai)) {
            return luong == 0 ? null : "the thang / ve mua khong kem luong tuc thi";
        }
        return "loai goi khong hop le";
    }

    // ================================================================== trang thai don

    public static final String CHO_THANH_TOAN = "cho_thanh_toan";
    public static final String DA_THANH_TOAN = "da_thanh_toan";
    public static final String DA_CONG = "da_cong";
    public static final String THAT_BAI = "that_bai";
    public static final String HOAN_TIEN = "hoan_tien";
    public static final String CHO_THU_HOI = "cho_thu_hoi";
    public static final String DA_THU_HOI = "da_thu_hoi";

    public static final String[] TRANG_THAI = {CHO_THANH_TOAN, DA_THANH_TOAN, DA_CONG, THAT_BAI, HOAN_TIEN,
        CHO_THU_HOI, DA_THU_HOI};

    /**
     * May trang thai cua nap_don. Web chi duoc: cho->da_thanh_toan/that_bai, that_bai->da_thanh_toan
     * (tien den muon), da_thanh_toan->hoan_tien, da_cong->cho_thu_hoi. Server game chi duoc:
     * da_thanh_toan->da_cong, cho_thu_hoi->da_thu_hoi. Moi buoc chuyen la MOT cau
     * "UPDATE .. WHERE id=? AND trang_thai=<tu>" - dung 1 dong bi doi moi tinh (idempotent).
     */
    public static boolean chuyenHopLe(final String tu, final String den) {
        if (tu == null || den == null) {
            return false;
        }
        if (CHO_THANH_TOAN.equals(tu)) {
            return DA_THANH_TOAN.equals(den) || THAT_BAI.equals(den);
        }
        if (THAT_BAI.equals(tu)) {
            return DA_THANH_TOAN.equals(den);
        }
        if (DA_THANH_TOAN.equals(tu)) {
            return DA_CONG.equals(den) || HOAN_TIEN.equals(den);
        }
        if (DA_CONG.equals(tu)) {
            return CHO_THU_HOI.equals(den);
        }
        if (CHO_THU_HOI.equals(tu)) {
            return DA_THU_HOI.equals(den);
        }
        return false;
    }

    /** Khoa duy nhat cua so cai cho lan cong don (UNIQUE nap_so_cai.ma_tham_chieu). */
    public static String thamChieuCong(final String maDon) {
        return "cong:" + maDon;
    }

    public static String thamChieuThuHoi(final String maDon) {
        return "thuhoi:" + maDon;
    }

    public static String thamChieuTheThang(final int charId, final String ngay) {
        return "thethang:" + charId + ":" + ngay;
    }

    /**
     * Mo phong "cong don" (ban thuan cua NapTienGame.congDon) tren mot so du: tra ve so luong
     * da cong neu buoc chuyen da_thanh_toan->da_cong thanh cong, 0 neu don da xu ly roi.
     * {@code trangThai[0]} bi doi tai cho. Dung cho test goi nhieu lan / song song.
     */
    public static synchronized int congDonThuan(final String[] trangThai, final long[] soDu, final int luong) {
        if (!DA_THANH_TOAN.equals(trangThai[0])) {
            return 0;
        }
        trangThai[0] = DA_CONG;
        soDu[0] += luong;
        return luong;
    }

    // ================================================================== no / thu hoi

    /**
     * Cong luong khi tai khoan dang no (hoan tien sau khi da cong): tra no truoc.
     * @return {tra no, cong that}
     */
    public static long[] congCoNo(final long no, final long luongMoi) {
        final long n = Math.max(0L, no);
        final long m = Math.max(0L, luongMoi);
        final long tra = Math.min(n, m);
        return new long[]{tra, m - tra};
    }

    /**
     * Thu hoi luong tu nhan vat dang online: tru toi da so dang co, phan con lai thanh no.
     * @return {tru tu nhan vat, no con lai}
     */
    public static long[] thuHoi(final long luongDangCo, final long canThu) {
        final long co = Math.max(0L, luongDangCo);
        final long can = Math.max(0L, canThu);
        final long tru = Math.min(co, can);
        return new long[]{tru, can - tru};
    }

    // ================================================================== tran nap

    /** Tran mac dinh / tai khoan (VND). Admin chinh trong nap_cau_hinh, khong vuot TRAN_CUNG. */
    public static final long TRAN_TUAN_VND = 1000000L;
    public static final long TRAN_THANG_VND = 2000000L;
    /** Tran cung: cau hinh lon hon bi kep ve day. */
    public static final long TRAN_CUNG_THANG_VND = 5000000L;
    /** Nang tran tu dat (nguoi choi) chi co hieu luc sau 72 gio (nguoi choi tu bao ve minh). */
    public static final int GIO_CHO_NANG_TRAN = 72;

    /** Kep tran cau hinh vao [0, TRAN_CUNG]. 0 hoac am -> mac dinh. */
    public static long kepTran(final long cauHinh, final long macDinh) {
        if (cauHinh <= 0L) {
            return macDinh;
        }
        return Math.min(cauHinh, TRAN_CUNG_THANG_VND);
    }

    /**
     * Kiem tra tao don moi co vuot tran khong. daTuan/daThang = tong don chua that bai/hoan
     * trong tuan/thang (gom ca don dang cho, de khong lach bang cach tao nhieu don cung luc).
     * tranTuDat <= 0 = khong tu dat.
     * @return null neu duoc, ly do neu vuot.
     */
    public static String kiemTran(final long daTuan, final long daThang, final long giaMoi,
            final long tranTuan, final long tranThang, final long tranTuDat) {
        if (giaMoi <= 0L) {
            return "so tien khong hop le";
        }
        long thang = kepTran(tranThang, TRAN_THANG_VND);
        if (tranTuDat > 0L) {
            thang = Math.min(thang, tranTuDat);
        }
        final long tuan = Math.min(kepTran(tranTuan, TRAN_TUAN_VND), thang);
        if (daTuan + giaMoi > tuan) {
            return String.format(Locale.ROOT, "vuot tran tuan (%d/%d d)", daTuan + giaMoi, tuan);
        }
        if (daThang + giaMoi > thang) {
            return String.format(Locale.ROOT, "vuot tran thang (%d/%d d)", daThang + giaMoi, thang);
        }
        return null;
    }

    // ================================================================== the thang

    public static final int THE_THANG_SO_NGAY = 30;
    public static final int THE_THANG_LUONG_NGAY = 20;
    /** Khong mua them khi con hon chung nay ngay (toi da 2 thang chong nhau). */
    public static final int THE_THANG_CON_TOI_DA_KHI_MUA = 30;
    /** QoL: +1 luot Ham nguc / ngay - thuong cua luot them CHI la diem ve mua (khong EXP, khong vat lieu). */
    public static final int THE_THANG_LUOT_HAM_NGUC_THEM = 1;
    public static final int THE_THANG_DIEM_VE_MUA_LUOT_THEM = 20;
    /** QoL: ban kinh tu nhat do them (o). */
    public static final int THE_THANG_BAN_KINH_NHAT_THEM = 2;
    /** QoL: +1 trang tu do trang tri (trang = 10 o). */
    public static final int THE_THANG_TRANG_TU_DO_THEM = 1;
    /** QoL: +1 gio uy thac luyen tap / ngay (TangToc.UY_THAC_GIO_THE_THANG) - van trong tran diem tang toc. */
    public static final int THE_THANG_GIO_UY_THAC_THEM = TangToc.UY_THAC_GIO_THE_THANG;

    static final long NGAY_MS = 86400000L;

    /**
     * Han moi khi mua them 1 the. Tra ve -1 neu con qua THE_THANG_CON_TOI_DA_KHI_MUA ngay
     * (khong cho tich qua 2 thang). Het han tu truoc (hoac 0) thi tinh tu bay gio.
     */
    public static long giaHanTheThang(final long hetHanMs, final long bayGioMs) {
        final long goc = Math.max(hetHanMs, bayGioMs);
        if (goc - bayGioMs > THE_THANG_CON_TOI_DA_KHI_MUA * NGAY_MS) {
            return -1L;
        }
        return goc + THE_THANG_SO_NGAY * NGAY_MS;
    }

    /** The con hieu luc tai bayGio. */
    public static boolean theConHan(final long hetHanMs, final long bayGioMs) {
        return hetHanMs > bayGioMs;
    }

    /** Nhan luong ngay: chi khi con han va chua nhan hom nay. Ngay bo lo KHONG duoc nhan bu. */
    public static boolean coTheNhanNgay(final long hetHanMs, final long bayGioMs, final String ngayNhanCuoi,
            final String homNay) {
        return theConHan(hetHanMs, bayGioMs) && (ngayNhanCuoi == null || !ngayNhanCuoi.equals(homNay));
    }

    /** Thu hoi 1 the (hoan tien): tru 30 ngay khoi han (khong am duoi bay gio). */
    public static long thuHoiTheThang(final long hetHanMs, final long bayGioMs) {
        if (hetHanMs <= bayGioMs) {
            return hetHanMs;
        }
        return Math.max(bayGioMs, hetHanMs - THE_THANG_SO_NGAY * NGAY_MS);
    }

    // ================================================================== ve mua

    public static final int VE_MUA_SO_NGAY = 56;
    public static final int VE_MUA_SO_BAC = 30;
    public static final int VE_MUA_DIEM_MOI_BAC = 100;
    /** Diem tu thoi gian choi: 1 diem / 3 phut, tran 40 / ngay. */
    public static final int VE_MUA_PHUT_MOI_DIEM = 3;
    public static final int VE_MUA_DIEM_ONLINE_NGAY = 40;
    /** Tran diem / ngay tu MOI nguon (online + nhiem vu + ham nguc). */
    public static final int VE_MUA_DIEM_NGAY_TOI_DA = 120;
    /** Vat lieu nang cap (gem 8 = RoiLauDai.ID_DA), luon KHOA (khong giao dich duoc). */
    public static final int ID_DA_NANG_CAP = 8;

    /** Chi so mua ve (0, 1, ...) theo ngay tu khai truong. */
    public static int muaVe(final int ngayTuKhaiTruong) {
        return Math.max(0, ngayTuKhaiTruong) / VE_MUA_SO_NGAY;
    }

    public static int ngayConLaiMuaVe(final int ngayTuKhaiTruong) {
        final int n = Math.max(0, ngayTuKhaiTruong);
        return VE_MUA_SO_NGAY - n % VE_MUA_SO_NGAY;
    }

    public static int bacTuDiem(final int diem) {
        return Math.min(VE_MUA_SO_BAC, Math.max(0, diem) / VE_MUA_DIEM_MOI_BAC);
    }

    /** Cong diem co tran ngay. @return so diem thuc cong. */
    public static int diemDuocCong(final int diemHomNay, final int them, final int tranNgay) {
        if (them <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(them, tranNgay - Math.max(0, diemHomNay)));
    }

    /**
     * Thuong bac {@code bac} (1..30) cua track. Ngu phap: "DA:n" (da nang cap KHOA), "TT:ma" (so huu
     * trang tri, khong giao dich duoc), "TG:ma:n" (vat pham tang toc ban KHOA, ma trong TangToc.MA), rong = khong
     * co. KHONG BAO GIO co xu, luong, luong khoa, EXP truc tiep, do co chi so (test chan).
     */
    public static String thuongBac(final boolean caoCap, final int bac) {
        if (bac < 1 || bac > VE_MUA_SO_BAC) {
            return "";
        }
        if (bac % 3 == 0) {
            return "DA:3";
        }
        // Tang toc ban KHOA (TangToc): track mien phi bac 7/14/22 ve x1,5, bac 28 bua the luc; cao cap bac 26
        // 2 ve x2. Dung chung tran diem ngay/tuan voi ban cao cap - khong vuot tran du co ca hai track.
        if (!caoCap && (bac == 7 || bac == 14 || bac == 22)) {
            return "TG:" + TangToc.MA[TangToc.VE_15] + ":1";
        }
        if (!caoCap && bac == 28) {
            return "TG:" + TangToc.MA[TangToc.BUA] + ":1";
        }
        if (caoCap && bac == 26) {
            return "TG:" + TangToc.MA[TangToc.VE_2] + ":2";
        }
        if (!caoCap) {
            if (bac == 10) {
                return "TT:danh_hieu_nha_tham_hiem";
            }
            if (bac == 20) {
                return "TT:khung_ten_dong";
            }
            if (bac == 5 || bac == 25) {
                return "TT:bieu_cam_chao";
            }
            return "";
        }
        if (bac == 29) {
            return "TT:hao_quang_mua";
        }
        if (bac % 3 == 1) {
            return "TT:skin_mua_" + ((bac / 3) % 4 + 1);
        }
        return "TT:bieu_cam_mua_" + ((bac / 3) % 5 + 1);
    }

    /** Tong da nang cap cua mot track (ngan sach - test so free vs cao cap). */
    public static int tongDaTrack(final boolean caoCap) {
        int s = 0;
        for (int b = 1; b <= VE_MUA_SO_BAC; b++) {
            final String t = thuongBac(caoCap, b);
            if (t.startsWith("DA:")) {
                s += Integer.parseInt(t.substring(3));
            }
        }
        return s;
    }

    // ================================================================== cua hang luong

    /** Cach trao mot mon cua hang - DANH SACH TRANG (whitelist). Khong co mon nao co chi so. */
    public static final String TRAO_TRANG_TRI = "trang_tri";
    public static final String TRAO_MO_RONG_TUI = "mo_rong_tui";
    public static final String TRAO_DOI_TEN = "doi_ten";
    /**
     * Vat pham tang toc luyen cap (ban cao cap): CHI cac ma trong TangToc.MA, gia = TangToc.GIA_LUONG, gioi_han =
     * so lan mua / TUAN = TangToc.MUA_TUAN_TOI_DA. Trao vao tui la vat pham KHOA (khong giao dich).
     */
    public static final String TRAO_TANG_TOC = "tang_toc";
    /**
     * The dat ten Pokemon (DatTenPet): CHI ma {@link #MA_THE_DAT_TEN}, vao tui la vat pham KHOA. Mon khong mang chi so:
     * dong chi so thuoc NGAN SACH O "the ten pet" (thiet_ke_can_bang 4.5 - tra phi = mien phi), ban kiem duoc bang
     * Tem nghien cuu / luong khoa la CUNG vat pham. Gia + so lan mua / tuan lay tu cau hinh dat_ten_pet.gia_luong /
     * dat_ten_pet.mua_tuan_toi_da (cot gia_luong cua dong chi de hien, gioi_han phai = 0).
     */
    public static final String TRAO_DAT_TEN_PET = "dat_ten_pet";
    public static final String MA_THE_DAT_TEN = "the_dat_ten_pet";
    public static final String[] TRAO_HOP_LE = {TRAO_TRANG_TRI, TRAO_MO_RONG_TUI, TRAO_DOI_TEN, TRAO_TANG_TOC, TRAO_DAT_TEN_PET};

    /** Nhom hien thi. */
    public static final String[] NHOM_CUA_HANG = {"skin", "hao_quang", "thu_cuoi", "mau_ten", "bieu_cam", "nha",
        "khung", "tien_ich", "tang_toc"};
    public static final String[] TEN_NHOM_CUA_HANG = {"Skin Pokémon", "Hào quang", "Skin thú cưỡi", "Màu tên",
        "Biểu cảm", "Trang trí nhà", "Khung và danh hiệu", "Tiện ích", "Tăng tốc luyện cấp"};

    /** Tu cam trong ten/mo ta mon ban bang luong - chan do suc manh lot vao (kem whitelist). */
    static final String[] TU_CAM = {"exp", "kinh nghiệm", "kinh nghiem", "x2", "x3", "tấn công", "tan cong",
        "phòng thủ", "phong thu", "hp", "chí mạng", "chi mang", "tỉ lệ", "ti le", "tỷ lệ", "may mắn", "may man",
        "bảo hộ", "bao ho", "cường hoá", "cuong hoa", "nâng cấp", "nang cap", "đá", "chỉ số", "chi so", "sức mạnh",
        "suc manh", "tiềm năng", "tiem nang", "vé quay", "ve quay", "rương", "ruong", "hộp quà", "hop qua"};

    /** Gia luong toi thieu/toi da cho mot mon. */
    public static final int GIA_MON_TOI_THIEU = 10;
    public static final int GIA_MON_TOI_DA = 5000;
    public static final int GIA_MO_RONG_TUI = 300;
    public static final int GIA_DOI_TEN = 500;
    public static final int DOI_TEN_CACH_NGAY = 30;
    /** So nhom xoay vong (tuan). Mon co nhom_xoay = 0 luon ban; k > 0 ban khi tuan % 4 == k-1. */
    public static final int SO_NHOM_XOAY = 4;

    /** Mot mon cua hang (bang nap_cua_hang). */
    public static final class Mon {
        public final String ma, ten, nhom, trao;
        public final int giaLuong, hanNgay, gioiHan, nhomXoay;
        public final boolean coChiSo, giaoDichDuoc;

        public Mon(final String ma, final String ten, final String nhom, final String trao, final int giaLuong,
                final int hanNgay, final int gioiHan, final int nhomXoay, final boolean giaoDichDuoc) {
            this(ma, ten, nhom, trao, giaLuong, hanNgay, gioiHan, nhomXoay, giaoDichDuoc, false);
        }

        public Mon(final String ma, final String ten, final String nhom, final String trao, final int giaLuong,
                final int hanNgay, final int gioiHan, final int nhomXoay, final boolean giaoDichDuoc,
                final boolean coChiSo) {
            this.ma = ma;
            this.ten = ten;
            this.nhom = nhom;
            this.trao = trao;
            this.giaLuong = giaLuong;
            this.hanNgay = hanNgay;
            this.gioiHan = gioiHan;
            this.nhomXoay = nhomXoay;
            this.giaoDichDuoc = giaoDichDuoc;
            this.coChiSo = coChiSo;
        }
    }

    /** Danh muc mac dinh - trung voi INSERT trong sql/pokemon_nap_tien.sql (test doi chieu). */
    public static final Mon[] MON_MAC_DINH = {
        new Mon("mo_rong_tui", "Mở rộng hành trang (+42 ô)", "tien_ich", TRAO_MO_RONG_TUI, GIA_MO_RONG_TUI, 0, 4, 0, false),
        new Mon("doi_ten", "Đổi tên Pokémon", "tien_ich", TRAO_DOI_TEN, GIA_DOI_TEN, 0, 0, 0, false),
        new Mon("mau_ten_vang", "Màu tên Vàng (30 ngày)", "mau_ten", TRAO_TRANG_TRI, 150, 30, 0, 0, false),
        new Mon("mau_ten_xanh", "Màu tên Xanh ngọc (30 ngày)", "mau_ten", TRAO_TRANG_TRI, 150, 30, 0, 0, false),
        new Mon("khung_ten_bac", "Khung tên Bạc", "khung", TRAO_TRANG_TRI, 200, 0, 1, 0, true),
        new Mon("skin_mu_rom", "Skin: Mũ rơm", "skin", TRAO_TRANG_TRI, 600, 0, 1, 1, true),
        new Mon("hao_quang_lua", "Hào quang Lửa", "hao_quang", TRAO_TRANG_TRI, 400, 0, 1, 1, true),
        new Mon("bieu_cam_vui", "Biểu cảm: Vui vẻ", "bieu_cam", TRAO_TRANG_TRI, 80, 0, 1, 1, true),
        new Mon("skin_khan_quang", "Skin: Khăn quàng mùa đông", "skin", TRAO_TRANG_TRI, 600, 0, 1, 2, true),
        new Mon("thu_cuoi_van_may", "Skin thú cưỡi: Vân mây", "thu_cuoi", TRAO_TRANG_TRI, 500, 0, 1, 2, true),
        new Mon("nha_den_long", "Trang trí nhà: Đèn lồng", "nha", TRAO_TRANG_TRI, 120, 0, 5, 2, true),
        new Mon("skin_kinh_ram", "Skin: Kính râm", "skin", TRAO_TRANG_TRI, 600, 0, 1, 3, true),
        new Mon("hao_quang_nuoc", "Hào quang Nước", "hao_quang", TRAO_TRANG_TRI, 400, 0, 1, 3, true),
        new Mon("bieu_cam_ngu", "Biểu cảm: Buồn ngủ", "bieu_cam", TRAO_TRANG_TRI, 80, 0, 1, 3, true),
        new Mon("skin_no_xinh", "Skin: Nơ xinh", "skin", TRAO_TRANG_TRI, 600, 0, 1, 4, true),
        new Mon("thu_cuoi_cau_vong", "Skin thú cưỡi: Cầu vồng", "thu_cuoi", TRAO_TRANG_TRI, 500, 0, 1, 4, true),
        new Mon("nha_cay_canh", "Trang trí nhà: Cây cảnh", "nha", TRAO_TRANG_TRI, 120, 0, 5, 4, true),
        // Tang toc (TangToc): gioi_han = lan mua / TUAN; han 0; khong giao dich; gia co dinh.
        new Mon("ve_exp_15", "Vé EXP x1,5 (30 phút)", "tang_toc", TRAO_TANG_TOC, 20, 0, 14, 0, false),
        new Mon("ve_exp_2", "Vé EXP x2 (30 phút)", "tang_toc", TRAO_TANG_TOC, 40, 0, 7, 0, false),
        new Mon("bua_the_luc", "Bùa thể lực (+60 phút)", "tang_toc", TRAO_TANG_TOC, 60, 0, 4, 0, false),
        new Mon("uy_thac", "Phiếu ủy thác luyện tập (1 giờ)", "tang_toc", TRAO_TANG_TOC, 15, 0, 17, 0, false),
        new Mon("keo_hiem", "Kẹo hiếm", "tang_toc", TRAO_TANG_TOC, 40, 0, 2, 0, false),
        // The dat ten Pokemon: gia / gioi han tuan that = cau hinh dat_ten_pet.* (DatTenPet), gioi_han dong = 0.
        new Mon(MA_THE_DAT_TEN, "Thẻ đặt tên Pokémon", "tien_ich", TRAO_DAT_TEN_PET, DatTenPet.GIA_LUONG_MAC_DINH, 0, 0, 0, false),
    };

    /** Thuong lan nap dau (moi tai khoan 1 lan): CHI trang tri, khong giao dich duoc. */
    public static final String THUONG_LAN_DAU = "khung_nguoi_ung_ho";

    /**
     * Mon cua hang co hop le khong (server tu choi nap dong sai; web admin kiem cung luat).
     * @return null neu hop le, ly do neu sai.
     */
    public static String kiemMon(final Mon m) {
        if (m == null || m.ma == null || !m.ma.matches("[a-z0-9_]{2,40}")) {
            return "ma mon chi gom a-z 0-9 _ (2-40 ky tu)";
        }
        if (m.coChiSo) {
            return "cam ban mon co chi so";
        }
        boolean traoDung = false;
        for (final String t : TRAO_HOP_LE) {
            traoDung |= t.equals(m.trao);
        }
        if (!traoDung) {
            return "cach trao '" + m.trao + "' khong nam trong danh sach trang (trang_tri / mo_rong_tui / doi_ten / tang_toc / dat_ten_pet)";
        }
        if (TRAO_TANG_TOC.equals(m.trao)) {
            return kiemTangToc(m);
        }
        if (TRAO_DAT_TEN_PET.equals(m.trao)) {
            return kiemDatTen(m);
        }
        if (m.giaLuong < GIA_MON_TOI_THIEU || m.giaLuong > GIA_MON_TOI_DA) {
            return "gia phai trong " + GIA_MON_TOI_THIEU + "-" + GIA_MON_TOI_DA + " luong";
        }
        if (m.nhomXoay < 0 || m.nhomXoay > SO_NHOM_XOAY) {
            return "nhom xoay 0.." + SO_NHOM_XOAY;
        }
        final String ten = m.ten == null ? "" : m.ten.toLowerCase(Locale.ROOT);
        if (ten.trim().isEmpty()) {
            return "thieu ten";
        }
        for (final String c : TU_CAM) {
            if (chuaTu(ten, c)) {
                return "ten chua tu cam '" + c + "' (nghi la do suc manh)";
            }
        }
        if (m.giaoDichDuoc && (m.hanNgay > 0 || !TRAO_TRANG_TRI.equals(m.trao))) {
            return "chi trang tri vinh vien moi duoc giao dich";
        }
        return null;
    }

    /**
     * Mon tang toc: ma PHAI thuoc TangToc.MA (danh sach co dinh - admin khong them duoc mon tang toc moi bang du
     * lieu), gia = TangToc.GIA_LUONG, gioi han tuan 1..TangToc.MUA_TUAN_TOI_DA, nhom tang_toc, vinh vien, khong
     * giao dich, khong chi so. Ten duoc phep co "EXP" (tu cam ap cho moi cach trao khac).
     */
    static String kiemTangToc(final Mon m) {
        final int loai = TangToc.loaiCuaMa(m.ma);
        if (loai < 0) {
            return "mon tang toc chi gom: " + String.join(", ", TangToc.MA);
        }
        if (!"tang_toc".equals(m.nhom)) {
            return "mon tang toc phai thuoc nhom tang_toc";
        }
        if (m.giaLuong != TangToc.GIA_LUONG[loai]) {
            return "gia " + m.ma + " co dinh " + TangToc.GIA_LUONG[loai] + " luong (TangToc.GIA_LUONG)";
        }
        if (m.gioiHan < 1 || m.gioiHan > TangToc.MUA_TUAN_TOI_DA[loai]) {
            return "gioi han tuan " + m.ma + " phai trong 1.." + TangToc.MUA_TUAN_TOI_DA[loai];
        }
        if (m.giaoDichDuoc || m.hanNgay != 0 || m.nhomXoay != 0) {
            return "mon tang toc: khong giao dich, khong han, luon ban";
        }
        return null;
    }

    /** Mon the dat ten: dung ma, nhom tien_ich, khong giao dich, khong han, luon ban, gioi_han 0 (cau hinh quyet). */
    static String kiemDatTen(final Mon m) {
        if (!MA_THE_DAT_TEN.equals(m.ma)) {
            return "mon dat_ten_pet chi co ma " + MA_THE_DAT_TEN;
        }
        if (!"tien_ich".equals(m.nhom)) {
            return "the dat ten thuoc nhom tien_ich";
        }
        if (m.giaLuong < GIA_MON_TOI_THIEU || m.giaLuong > GIA_MON_TOI_DA) {
            return "gia phai trong " + GIA_MON_TOI_THIEU + "-" + GIA_MON_TOI_DA + " luong";
        }
        if (m.giaoDichDuoc || m.hanNgay != 0 || m.nhomXoay != 0 || m.gioiHan != 0) {
            return "the dat ten: khong giao dich, khong han, luon ban, gioi_han = 0 (dat_ten_pet.mua_tuan_toi_da)";
        }
        return null;
    }

    /** Tu {@code tu} xuat hien nhu MOT TU rieng (bien la ky tu khong phai chu/so). */
    static boolean chuaTu(final String s, final String tu) {
        int i = s.indexOf(tu);
        while (i >= 0) {
            final boolean dau = i == 0 || !Character.isLetterOrDigit(s.charAt(i - 1));
            final int j = i + tu.length();
            final boolean cuoi = j >= s.length() || !Character.isLetterOrDigit(s.charAt(j));
            if (dau && cuoi) {
                return true;
            }
            i = s.indexOf(tu, i + 1);
        }
        return false;
    }

    /** Tuan (de xoay vong) tu so ngay epoch. */
    public static int tuanTuNgayEpoch(final long ngayEpoch) {
        // 1970-01-01 la thu Nam -> +3 de tuan doi luc thu Hai.
        return (int) ((ngayEpoch + 3L) / 7L);
    }

    /** Mon co dang ban trong tuan nay khong (xoay vong). */
    public static boolean dangBan(final int nhomXoay, final int tuan) {
        return nhomXoay <= 0 || (tuan % SO_NHOM_XOAY) == nhomXoay - 1;
    }

    /** Danh sach mon dang ban (da loc hop le + xoay vong), giu thu tu. */
    public static List<Mon> monDangBan(final List<Mon> tatCa, final int tuan) {
        final List<Mon> ra = new ArrayList<Mon>();
        for (final Mon m : tatCa) {
            if (kiemMon(m) == null && dangBan(m.nhomXoay, tuan)) {
                ra.add(m);
            }
        }
        return ra;
    }

    /** Doi ten: con phai doi bao nhieu ngay (0 = duoc doi). */
    public static int ngayChoDoiTen(final long lanCuoiMs, final long bayGioMs) {
        if (lanCuoiMs <= 0L) {
            return 0;
        }
        final long het = lanCuoiMs + DOI_TEN_CACH_NGAY * NGAY_MS;
        return het <= bayGioMs ? 0 : (int) ((het - bayGioMs + NGAY_MS - 1) / NGAY_MS);
    }

    // ================================================================== cho trang tri

    /** Thue ban (dot) + phi dang (dot, tra luc dang) - cung muc cho ky gui thiet ke dai han. */
    public static final int THUE_BAN_PHAN_TRAM = ThongSoLauDai.THUE_CHO_PHAN_TRAM;
    public static final int PHI_DANG_PHAN_TRAM = ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM;
    /** Gia rao phai trong [50%, 200%] trung vi 20 giao dich gan nhat (khi da co >= 3 giao dich). */
    public static final int BIEN_GIA_DUOI_PHAN_TRAM = 50;
    public static final int BIEN_GIA_TREN_PHAN_TRAM = 200;
    public static final int SO_GIAO_DICH_THAM_CHIEU = 20;
    public static final int SO_GIAO_DICH_TOI_THIEU = 3;
    /** Moi nhan vat rao toi da bay nhieu mon cung luc. */
    public static final int RAO_TOI_DA = 5;
    /** He so gia de nghi (x 1 gio thu nhap o cap nguoi ban). */
    public static final int[] HE_SO_GIA_GIO_PHAN_TRAM = {50, 100, 200, 400, 800};

    public static long thueBan(final long gia) {
        return Math.max(0L, gia) * THUE_BAN_PHAN_TRAM / 100L;
    }

    public static long phiDang(final long gia) {
        return Math.max(1L, Math.max(0L, gia) * PHI_DANG_PHAN_TRAM / 100L);
    }

    /** Trung vi (khong doi danh sach goc). 0 khi rong. */
    public static long trungVi(final List<Long> gia) {
        if (gia == null || gia.isEmpty()) {
            return 0L;
        }
        final List<Long> s = new ArrayList<Long>(gia);
        Collections.sort(s);
        return s.get(s.size() / 2);
    }

    /**
     * Gia rao hop le? Chua du giao dich tham chieu -> chi can >= 1. Co tham chieu -> phai trong bien.
     * @return null neu hop le, ly do neu sai.
     */
    public static String kiemGiaRao(final long gia, final List<Long> giaoDichGanDay) {
        if (gia < 1L || gia > 2000000000L) {
            return "giá không hợp lệ.";
        }
        if (giaoDichGanDay == null || giaoDichGanDay.size() < SO_GIAO_DICH_TOI_THIEU) {
            return null;
        }
        final long tv = trungVi(giaoDichGanDay);
        final long duoi = tv * BIEN_GIA_DUOI_PHAN_TRAM / 100L;
        final long tren = tv * BIEN_GIA_TREN_PHAN_TRAM / 100L;
        if (gia < duoi || gia > tren) {
            return String.format(Locale.ROOT, "giá phải từ %d đến %d xu (50%%-200%% giá giao dịch trung vị %d xu).",
                    duoi, tren, tv);
        }
        return null;
    }

    // ================================================================== ma don

    /** Ma don (noi dung chuyen khoan) - bang chu cai web dung (bo 0/O, 1/I). */
    public static final String KY_TU_MA = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    /** Tim ma don HTxxxxxxxx trong noi dung chuyen khoan (ngan hang chen chu truoc/sau). */
    public static String timMaDon(final String noiDung) {
        if (noiDung == null) {
            return null;
        }
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)(?<![A-Z0-9])HT([A-HJ-NP-Z2-9]{8})(?![A-Z0-9])")
                .matcher(noiDung);
        return m.find() ? ("HT" + m.group(1)).toUpperCase(Locale.ROOT) : null;
    }
}
