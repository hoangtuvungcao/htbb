package com.htbb.cauhinh;

/**
 * Cong tac doc NGAY moi lan dung (khong can nghe): ten khoa + ham doc. Gia tri ap dung trong <= 5-10 giay sau
 * khi admin sua tren trang "Cau hinh he thong" (bang cau_hinh). Web doc CUNG cac khoa nay
 * (HTBB-Web server/utils/cauHinhTrungTam.ts) - khong co cong tac thu hai.
 */
public final class CongTac {

    public static final String K_NAP_TANG_TOC = "nap.tang_toc.bat";
    public static final String K_NAP_HOAN_TU_DONG = "nap.hoan_tien.tu_dong";
    public static final String K_PVP_TINH_DIEM = "pvp.tinh_diem.bat";
    public static final String K_GYM = "gym.bat";
    public static final String K_HAM_NGUC = "ham_nguc.bat";
    public static final String K_TUA_NHANH_NGAY = "sv.tua_nhanh_ngay";

    private CongTac() {
    }

    /** Cua hang luong trong game ban vat pham tang toc (ban cao cap). */
    public static boolean banTangToc() {
        return CauHinhTrungTam.co(K_NAP_TANG_TOC);
    }

    /** May chu game tu thu hoi (luong thanh no, tru the thang, go ve mua) don 'cho_thu_hoi'. */
    public static boolean hoanTienTuDong() {
        return CauHinhTrungTam.co(K_NAP_HOAN_TU_DONG);
    }

    public static boolean pvpTinhDiem() {
        return CauHinhTrungTam.co(K_PVP_TINH_DIEM);
    }

    public static boolean gym() {
        return CauHinhTrungTam.co(K_GYM);
    }

    public static boolean hamNguc() {
        return CauHinhTrungTam.co(K_HAM_NGUC);
    }

    /**
     * So ngay tua nhanh lich dai han (thu nghiem). LUON 0 tren may that: chi co tac dung khi server.ini co
     * sv.cho_phep_thu_nghiem=1 (khoa khoaProd; CSDL khac 0 tren may that bi tu choi tu truoc).
     */
    public static int tuaNhanhNgay() {
        if (!CauHinhTrungTam.choPhepThuNghiem()) {
            return 0;
        }
        return (int) Math.max(0L, Math.min(3650L, CauHinhTrungTam.so(K_TUA_NHANH_NGAY, 0L)));
    }
}
