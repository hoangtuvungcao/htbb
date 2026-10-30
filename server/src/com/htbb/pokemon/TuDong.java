package com.htbb.pokemon;

import com.htbb.cauhinh.CauHinhTrungTam;

/**
 * TU DONG DANH chinh thuc (quyet dinh 18/10/2026: bo mod NQSH phia client - AutoPk, Van may, Len thanh, Giao
 * hang tu dong - thay bang MOT tinh nang "Tu dong danh" do MAY CHU dieu khien). Lop THUAN: test/KiemTuDong.java
 * goi truc tiep; game goi qua real.TuDongGame. Tai lieu: docs/thiet-ke-game.md.
 *
 * Nguyen tac:
 *   1. Chi chay khi DANG ONLINE. Treo offline van la "Phieu uy thac luyen tap" (TangToc.UY_THAC) - tach rieng,
 *      khong cong / khong tru lan nhau. Ve treo may vi vay giu nguyen gia tri.
 *   2. KHONG thuong them: EXP / xu / do roi khi tu dong di qua dung duong ha quai thuong, nen chiu nguyen nang
 *      luong huan luyen (NangLuong: 100% -> 50% -> 20% -> 0%). Lop nay khong co ham nao cham toi EXP.
 *   3. Tran gio moi ngay (tu_dong.gio_moi_ngay, + tu_dong.gio_them_the_thang khi con the thang). Het gio mien phi
 *      thi tru tiep vao KHO GIO TREO cua nhan vat (Phien.msKho, nap tu "Ve treo N gio" - real.TuDongGame.dungVeTreo,
 *      tran {@link #KHO_GIO_TOI_DA} gio); het ca hai may chu tat va bao client.
 *   4. May chu tu kiem dieu kien bat (chi bai luyen tap, khong PvP / san dau / Gym / boss neu cau hinh) va tat khi
 *      gap PvP / boss / doi khu vuc / ra khoi ban kinh.
 *   5. Chong bot: luc dang tu dong, nhip danh / uong thuoc / nhat do bi chan o muc nguoi that (cua so truot);
 *      vuot nhieu lan thi tat + ghi nghi van. Chong ngap goi tin (io.BoDemGoiTin) van giu nguyen.
 *
 * Moi phien (MOT nhan vat dang online) la mot {@link Phien}; truy cap dong bo tren chinh doi tuong do.
 */
public final class TuDong {

    private TuDong() {
    }

    // ================================================================== khoa cau hinh (tools/luu_tru_sinh_du_lieu/cauhinh/danh_muc.json)

    public static final String K_BAT = "tu_dong.bat";
    public static final String K_GIO_NGAY = "tu_dong.gio_moi_ngay";
    public static final String K_GIO_THE_THANG = "tu_dong.gio_them_the_thang";
    public static final String K_CHI_MAP_DA_MO = "tu_dong.chi_map_da_mo";
    public static final String K_TAT_KHI_PVP = "tu_dong.tat_khi_pvp";
    public static final String K_TU_DUNG_THUOC = "tu_dong.tu_dung_thuoc";
    public static final String K_BAN_KINH = "tu_dong.ban_kinh";
    public static final String K_NHIP_DANH = "tu_dong.nhip_danh";
    public static final String K_NHIP_THUOC = "tu_dong.nhip_thuoc";
    public static final String K_NHIP_NHAT = "tu_dong.nhip_nhat";
    public static final String K_NHIP_NGUOI = "tu_dong.nhip_nguoi";
    public static final String K_VI_PHAM_TAT = "tu_dong.vi_pham_tat";
    public static final String K_HOI_SINH_GIO = "tu_dong.hoi_sinh_moi_gio";
    // bo nao may chu (TuDongKeHoach) + dieu kien dung them (19/10/2026)
    public static final String K_KE_HOACH = "tu_dong.ke_hoach";
    public static final String K_CAP_TOI_THIEU = "tu_dong.cap_toi_thieu";
    public static final String K_CHENH_CAP_TREN = "tu_dong.chenh_cap_tren";
    public static final String K_CHENH_CAP_DUOI = "tu_dong.chenh_cap_duoi";
    public static final String K_DANH_BOSS = "tu_dong.danh_boss";
    public static final String K_HP_QUAI_TOI_DA = "tu_dong.hp_quai_toi_da_pt";
    public static final String K_CHIEU_THEO_HE = "tu_dong.chon_chieu_theo_he";
    public static final String K_NGUONG_THUOC_HP = "tu_dong.nguong_thuoc_hp";
    public static final String K_NGUONG_THUOC_MP = "tu_dong.nguong_thuoc_mp";
    public static final String K_NGUONG_HP_THAP = "tu_dong.nguong_hp_thap";
    public static final String K_HP_THAP_DUNG = "tu_dong.hp_thap_dung";
    public static final String K_HOI_PHUC_TOI = "tu_dong.hoi_phuc_toi";
    public static final String K_NHAT_BAN_KINH = "tu_dong.nhat_ban_kinh";
    public static final String K_DUNG_DAY_TUI = "tu_dong.dung_khi_day_tui";
    public static final String K_DUNG_TU_DI = "tu_dong.dung_khi_tu_di";
    public static final String K_TRANH_PK = "tu_dong.tranh_pk_px";
    public static final String K_MAP_CAM = "tu_dong.map_cam";
    public static final String K_HOI_SINH_TRA_PHI = "tu_dong.hoi_sinh_tra_phi";

    // ================================================================== lenh mang (-100, hai chieu)

    /** Ma lenh client <-> server cua Tu dong danh. */
    public static final int LENH = -100;
    /**
     * Client -> server: byte loai. C_KE (client 0.9.2+) = xin ke hoach buoc tiep (~0,6 giay / lan khi dang chay).
     * C_XIN / C_BAT cua client moi gui them byte phien ban giao thuc ({@link #GIAO_THUC}) va cai dat bang (xem
     * real.TuDongGame.docCaiDat); C_TAT co the kem byte ly do (TAT_TU_DI khi nguoi choi tu di chuyen).
     */
    public static final int C_XIN = 0, C_BAT = 1, C_TAT = 2, C_NHIP = 3, C_KE = 4;
    /** Phien ban giao thuc -100 cua client moi (client cu khong gui = 1). */
    public static final int GIAO_THUC = 2;
    /** Server -> client: byte dau goi -100. 0 = trang thai (nhu cu), 1 = ke hoach (client moi). */
    public static final int S_TRANG_THAI = 0, S_KE_HOACH = 1;
    /**
     * Co (bit) trong goi trang thai server -> client. CO_KE_HOACH: may chu lap ke hoach (client moi xin C_KE thay vi
     * tu chon); CO_DUNG_TU_DI: nguoi choi tu di chuyen thi DUNG (khong thi nhuong 3 giay).
     */
    public static final int CO_TINH_NANG = 1, CO_THUOC = 2, CO_TAT_PVP = 4, CO_CHI_MAP = 8, CO_KE_HOACH = 16,
            CO_DUNG_TU_DI = 32;

    // ================================================================== ly do tat (ghi kiem toan + bao client)

    public static final int TAT_NGUOI_CHOI = 0, TAT_HET_GIO = 1, TAT_PVP = 2, TAT_BOSS = 3, TAT_DOI_MAP = 4,
            TAT_RA_VUNG = 5, TAT_VI_PHAM = 6, TAT_CHET = 7, TAT_TAT_TINH_NANG = 8, TAT_SAN_DAU = 9, TAT_THOAT = 10,
            TAT_DAY_TUI = 11, TAT_HP_THAP = 12, TAT_TU_DI = 13, TAT_KHU_CAM = 14;
    static final String[] LY_DO = {
        "Đã dừng Tự động đánh.",
        "Hết thời gian Tự động đánh hôm nay. Ngày mai được dùng tiếp.",
        "Tự động đánh dừng: đang giao chiến với Pokémon khác (PvP).",
        "Tự động đánh dừng: boss xuất hiện, hãy tự điều khiển.",
        "Tự động đánh dừng: đã rời bãi luyện tập.",
        "Tự động đánh dừng: Pokémon đi quá xa vùng đã chọn.",
        "Tự động đánh dừng: thao tác nhanh bất thường.",
        "Tự động đánh dừng: Pokémon đã gục.",
        "Tự động đánh đang tạm khoá trên máy chủ.",
        "Tự động đánh dừng: không dùng ở sàn đấu, Gym hay chiến trường.",
        "Tự động đánh dừng khi thoát game.",
        "Tự động đánh dừng: hành trang đã đầy, hãy dọn bớt đồ.",
        "Tự động đánh dừng: HP thấp và hết thuốc hồi phục.",
        "Tự động đánh dừng vì bạn tự di chuyển.",
        "Tự động đánh dừng: bản đồ này không cho dùng Tự động đánh.",
    };

    public static String lyDo(final int ma) {
        return ma >= 0 && ma < LY_DO.length ? LY_DO[ma] : LY_DO[0];
    }

    // ================================================================== mac dinh (khop danh_muc.json)

    public static final int GIO_NGAY_MAC_DINH = 5;
    public static final int GIO_THE_THANG_MAC_DINH = 2;
    public static final int BAN_KINH_MAC_DINH = 240;
    public static final int BAN_KINH_MIN = 48;
    public static final int BAN_KINH_MAX = 960;
    /** Lech them (px) truoc khi coi la ra khoi vung: do tre mang + day lui khi trung don. */
    public static final int BAN_KINH_LE = 64;
    /**
     * KHO GIO TREO (yeu cau 31/10/2026, giftcode TREO400): gio Tu dong danh mua / tang qua vat pham "Ve treo N gio"
     * (id {@link #ID_VE_TREO}, so gio = hieu_ung gio_treo:N cua data_vat_pham_lau_dai). Kho chi bi tru SAU khi het gio
     * mien phi hom nay (gio_moi_ngay + the thang), khong het han, luu trong HoSoLauDai (khoa "a3", ms). Tran kho de
     * khong ai tich hang nghin gio (giftcode / nhieu nguon cong lai).
     */
    public static final int KHO_GIO_TOI_DA = 500;
    public static final long KHO_MS_TOI_DA = KHO_GIO_TOI_DA * 3600000L;
    /** data_item cua "Ve treo 10 gio" (type 28, khoa, xep chong theo Item.quanlity). */
    public static final int ID_VE_TREO = 1260;
    /** So gio moi ve (mac dinh khi dong data_vat_pham_lau_dai thieu so). 400 gio = 40 ve. */
    public static final int GIO_MOI_VE_TREO = 10;

    /** Hai lan ghi nhan cach nhau qua lau (client treo / mat mang) chi tinh toi da ngan nay. */
    public static final long KHOANG_TICH_TOI_DA_MS = 90000L;

    /** Cua so truot: toi da {@code so} lan trong {@code ms}. so <= 0 = khong gioi han. */
    public static final class Nhip {
        public final int so;
        public final long ms;

        public Nhip(final int so, final long ms) {
            this.so = so;
            this.ms = Math.max(1L, ms);
        }

        /** "so/giay" (vd "12/5"); hong = macDinh. */
        public static Nhip doc(final String s, final Nhip macDinh) {
            if (s == null || s.trim().isEmpty()) {
                return macDinh;
            }
            try {
                final String[] a = s.trim().split("/");
                final int n = Integer.parseInt(a[0].trim());
                final long giay = a.length > 1 ? Long.parseLong(a[1].trim()) : 1L;
                return new Nhip(Math.min(1000, n), Math.max(1L, Math.min(3600L, giay)) * 1000L);
            } catch (final NumberFormatException e) {
                return macDinh;
            }
        }

        @Override
        public String toString() {
            return so + "/" + (ms / 1000L) + "s";
        }
    }

    /** Vong moc thoi gian (cua so truot that su). */
    public static final class Vong {
        private long[] moc;
        private int vt;

        /** @return true = con cho (da ghi nhan lan nay). */
        public boolean choPhep(final Nhip n, final long bay) {
            if (n == null || n.so <= 0) {
                return true;
            }
            if (moc == null || moc.length != n.so) {
                moc = new long[n.so];
                vt = 0;
            }
            final long cu = moc[vt];
            if (cu != 0L && bay - cu < n.ms) {
                return false;
            }
            moc[vt] = bay;
            vt = (vt + 1) % moc.length;
            return true;
        }
    }

    public static final Nhip NHIP_DANH_MAC_DINH = new Nhip(12, 5000L);
    public static final Nhip NHIP_THUOC_MAC_DINH = new Nhip(4, 5000L);
    public static final Nhip NHIP_NHAT_MAC_DINH = new Nhip(8, 5000L);
    public static final Nhip NHIP_NGUOI_MAC_DINH = new Nhip(30, 5000L);
    public static final int VI_PHAM_TAT_MAC_DINH = 20;
    public static final int HOI_SINH_GIO_MAC_DINH = 2;
    public static final int CAP_TOI_THIEU_MAC_DINH = 0;

    /** Anh chup cau hinh (de test khong can CauHinhTrungTam). */
    public static final class CauHinh {
        public boolean bat = true;
        public int gioNgay = GIO_NGAY_MAC_DINH;
        public int gioTheThang = GIO_THE_THANG_MAC_DINH;
        public boolean chiMapDaMo = true;
        public boolean tatKhiPvp = true;
        public boolean tuDungThuoc = true;
        public int banKinh = BAN_KINH_MAC_DINH;
        public Nhip nhipDanh = NHIP_DANH_MAC_DINH;
        public Nhip nhipThuoc = NHIP_THUOC_MAC_DINH;
        public Nhip nhipNhat = NHIP_NHAT_MAC_DINH;
        public Nhip nhipNguoi = NHIP_NGUOI_MAC_DINH;
        public int viPhamTat = VI_PHAM_TAT_MAC_DINH;
        /** Toi da so lan TU dung vat pham hoi sinh / gio khi dang tu dong (0 = khong bao gio). */
        public int hoiSinhGio = HOI_SINH_GIO_MAC_DINH;
        /** May chu lap ke hoach tung buoc (client moi). false = client tu chon nhu ban 0.9.1. */
        public boolean keHoach = true;
        /** Cap toi thieu de bat (0 = khong gioi han). */
        public int capToiThieu = CAP_TOI_THIEU_MAC_DINH;
        /** Nguoi choi tu di chuyen: true = dung han, false = nhuong 3 giay roi chay tiep. */
        public boolean dungKhiTuDi = true;
        /** Ban do (id) cam them ngoai san dau / Gym / chien truong. */
        public int[] mapCam = new int[0];
        /**
         * Cho tu dung vat pham hoi sinh TRA PHI (Thuoc hoi sinh toi da - luong khoa) khi nguoi choi DONG Y rieng
         * (bang Tu dong: "Tu hoi sinh: ca vat pham luong"). false = chi Thuoc hoi sinh thuong (xu).
         */
        public boolean hoiSinhTraPhi;
        /** Luat cua bo nao (tu_dong.chenh_cap_*, nguong_*, nhat_ban_kinh...). */
        public final TuDongKeHoach.CauHinh kh = new TuDongKeHoach.CauHinh();

        public int co() {
            return (bat ? CO_TINH_NANG : 0) | (tuDungThuoc ? CO_THUOC : 0) | (tatKhiPvp ? CO_TAT_PVP : 0)
                    | (chiMapDaMo ? CO_CHI_MAP : 0) | (keHoach ? CO_KE_HOACH : 0) | (dungKhiTuDi ? CO_DUNG_TU_DI : 0);
        }

        public boolean mapBiCam(final int map) {
            if (PhongRieng.khongTuDong(map)) {
                return true; // luat ban do: phong rieng (pho ban / san cup) khong cho tu dong danh
            }
            for (final int m : mapCam) {
                if (m == map) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Doc cau hinh trung tam (ap dung ngay, <= 10 giay sau khi admin sua). */
    public static CauHinh cauHinh() {
        final CauHinh c = new CauHinh();
        try {
            c.bat = CauHinhTrungTam.co(K_BAT);
            c.gioNgay = (int) kep(CauHinhTrungTam.so(K_GIO_NGAY, GIO_NGAY_MAC_DINH), 0, 24);
            c.gioTheThang = (int) kep(CauHinhTrungTam.so(K_GIO_THE_THANG, GIO_THE_THANG_MAC_DINH), 0, 24);
            c.chiMapDaMo = CauHinhTrungTam.co(K_CHI_MAP_DA_MO);
            c.tatKhiPvp = CauHinhTrungTam.co(K_TAT_KHI_PVP);
            c.tuDungThuoc = CauHinhTrungTam.co(K_TU_DUNG_THUOC);
            c.banKinh = (int) kep(CauHinhTrungTam.so(K_BAN_KINH, BAN_KINH_MAC_DINH), BAN_KINH_MIN, BAN_KINH_MAX);
            c.nhipDanh = Nhip.doc(CauHinhTrungTam.chuoi(K_NHIP_DANH), NHIP_DANH_MAC_DINH);
            c.nhipThuoc = Nhip.doc(CauHinhTrungTam.chuoi(K_NHIP_THUOC), NHIP_THUOC_MAC_DINH);
            c.nhipNhat = Nhip.doc(CauHinhTrungTam.chuoi(K_NHIP_NHAT), NHIP_NHAT_MAC_DINH);
            c.nhipNguoi = Nhip.doc(CauHinhTrungTam.chuoi(K_NHIP_NGUOI), NHIP_NGUOI_MAC_DINH);
            c.viPhamTat = (int) kep(CauHinhTrungTam.so(K_VI_PHAM_TAT, VI_PHAM_TAT_MAC_DINH), 1, 1000);
            c.hoiSinhGio = (int) kep(CauHinhTrungTam.so(K_HOI_SINH_GIO, HOI_SINH_GIO_MAC_DINH), 0, 20);
            c.keHoach = CauHinhTrungTam.co(K_KE_HOACH);
            c.capToiThieu = (int) kep(CauHinhTrungTam.so(K_CAP_TOI_THIEU, CAP_TOI_THIEU_MAC_DINH), 0, 1000);
            c.dungKhiTuDi = CauHinhTrungTam.co(K_DUNG_TU_DI);
            c.mapCam = dsSo(CauHinhTrungTam.chuoi(K_MAP_CAM));
            c.hoiSinhTraPhi = CauHinhTrungTam.co(K_HOI_SINH_TRA_PHI);
            final TuDongKeHoach.CauHinh k = c.kh;
            k.tuDungThuoc = c.tuDungThuoc;
            k.chenhCapTren = (int) kep(CauHinhTrungTam.so(K_CHENH_CAP_TREN, TuDongKeHoach.CHENH_CAP_TREN_MAC_DINH), -1, 1000);
            k.chenhCapDuoi = (int) kep(CauHinhTrungTam.so(K_CHENH_CAP_DUOI, TuDongKeHoach.CHENH_CAP_DUOI_MAC_DINH), 0, 1000);
            k.danhBoss = CauHinhTrungTam.co(K_DANH_BOSS);
            k.hpQuaiToiDaPt = (int) kep(CauHinhTrungTam.so(K_HP_QUAI_TOI_DA, TuDongKeHoach.HP_QUAI_TOI_DA_PT_MAC_DINH), 0, 100000);
            k.chonChieuTheoHe = CauHinhTrungTam.co(K_CHIEU_THEO_HE);
            k.nguongThuocHp = (int) kep(CauHinhTrungTam.so(K_NGUONG_THUOC_HP, TuDongKeHoach.NGUONG_THUOC_HP_MAC_DINH), 0, 90);
            k.nguongThuocMp = (int) kep(CauHinhTrungTam.so(K_NGUONG_THUOC_MP, TuDongKeHoach.NGUONG_THUOC_MP_MAC_DINH), 0, 90);
            k.nguongHpThap = (int) kep(CauHinhTrungTam.so(K_NGUONG_HP_THAP, TuDongKeHoach.NGUONG_HP_THAP_MAC_DINH), 0, 90);
            k.hpThapDung = CauHinhTrungTam.co(K_HP_THAP_DUNG);
            k.hoiPhucToi = (int) kep(CauHinhTrungTam.so(K_HOI_PHUC_TOI, TuDongKeHoach.HOI_PHUC_TOI_MAC_DINH), 10, 100);
            k.nhatBanKinh = (int) kep(CauHinhTrungTam.so(K_NHAT_BAN_KINH, TuDongKeHoach.NHAT_BAN_KINH_MAC_DINH), 0, 480);
            k.dungKhiDayTui = CauHinhTrungTam.co(K_DUNG_DAY_TUI);
            k.tranhPkPx = (int) kep(CauHinhTrungTam.so(K_TRANH_PK, TuDongKeHoach.TRANH_PK_PX_MAC_DINH), 0, 960);
        } catch (final Throwable t) {
            // cau hinh loi -> giu mac dinh an toan (van co tran gio)
        }
        return c;
    }

    static long kep(final long v, final long lo, final long hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** "12, 20,7" -> {12, 20, 7}; phan hong bo qua. */
    static int[] dsSo(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return new int[0];
        }
        final String[] a = s.split(",");
        final int[] tam = new int[a.length];
        int n = 0;
        for (final String x : a) {
            try {
                tam[n] = Integer.parseInt(x.trim());
                n++;
            } catch (final NumberFormatException e) {
                // bo qua
            }
        }
        return java.util.Arrays.copyOf(tam, n);
    }

    // ================================================================== phien

    /** Hanh dong co gioi han nhip luc dang tu dong. */
    public static final int HD_DANH = 0, HD_THUOC = 1, HD_NHAT = 2;

    public static final class Phien {
        public boolean bat;
        public long batLuc;
        /** Lan cuoi cong thoi gian (ms). */
        public long tichLuc;
        /** Ngay (yyyyMMdd) cua msDung. */
        public int ngay;
        /** Mili-giay tu dong da dung trong ngay (ban sao cua HoSoLauDai a1/a2 neu co). */
        public long msDung;
        /** Kho gio treo con lai (ms, ban sao HoSoLauDai a3): tru sau khi msDung cham tran ngay. */
        public long msKho;
        /** Diem neo (luc bat), id map luc bat. */
        public int neoMap = -1;
        public int neoX;
        public int neoY;
        public int banKinh = BAN_KINH_MAC_DINH;
        public int viPham;
        public int lyDoTat = -1;
        /** Thong ke phien (kiem toan). */
        public int soDanh;
        public int soThuoc;
        public int soNhat;
        public int soChan;
        final Vong vDanh = new Vong();
        final Vong vThuoc = new Vong();
        final Vong vNhat = new Vong();
        final Vong vNguoi = new Vong();
        /** Lan cuoi ghi nghi van nhip nguoi (khong ghi lien tuc). */
        public long nghiVanLuc;
        /** Nguoi choi BAT "Tu hoi sinh" khi bat tu dong (client gui kem lenh bat). */
        public boolean xinHoiSinh;
        public int soHoiSinh;
        /** Luc guc dang cho he thong hoi sinh quyet dinh (ms, 0 = khong). */
        public long chetLuc;
        final Vong vHoiSinh = new Vong();
        /** Phien ban giao thuc cua client (1 = 0.9.1 cu: client tu chon; 2 = xin ke hoach C_KE). */
        public int giaoThuc = 1;
        /** Cai dat bang cua nguoi choi (client moi gui khi bat; -1 = khong gui -> mac dinh may chu). */
        public int nguongHp = -1, nguongMp = -1;
        public int nhat = TuDongKeHoach.NHAT_THUOC_XU;
        public boolean veHoiPhuc = true;
        /** Nguoi choi dong y rieng cho tu dung vat pham hoi sinh tra phi (luong khoa). */
        public boolean xinHoiSinhTraPhi;
        /** Trang thai bo nao giua cac buoc. */
        public int mucTieu = -1;
        public boolean dangHoiPhuc;
        public long thuocLuc;
        /** Lan cuoi gui HP / PP that (goi 60) cho client luc dang tu dong (0.9.22: PP tren client troi lech). */
        public long dongBoLuc;
        public long keLuc;
        public TuDongKeHoach.KeHoach keCuoi;
        /** Ly do cho / dung da bao nguoi choi gan nhat (khong bao lap). */
        public String lyBao = "";
        public long lyBaoLuc;
        public int soKe;
        /** So don danh o lan nhip truoc (client cu: phat hien dung im de goi y ly do), -1 = chua co. */
        public int soDanhLanNhip = -1;
    }

    /** Gio toi da hom nay (giay). */
    public static int giayToiDa(final CauHinh c, final boolean theThang) {
        return (c.gioNgay + (theThang ? c.gioTheThang : 0)) * 3600;
    }

    /** Doi ngay: dat lai bo dem (phien dang chay van tiep tuc, tinh tu 0 cho ngay moi). */
    public static void lamMoiNgay(final Phien p, final int homNay) {
        if (p.ngay != homNay) {
            p.ngay = homNay;
            p.msDung = 0L;
        }
    }

    /** Giay con lai: gio mien phi hom nay + kho gio treo. */
    public static int giayConLai(final CauHinh c, final Phien p, final boolean theThang, final int homNay) {
        synchronized (p) {
            lamMoiNgay(p, homNay);
            final long con = Math.max(0L, giayToiDa(c, theThang) * 1000L - p.msDung) + Math.max(0L, p.msKho);
            return (int) Math.min(Integer.MAX_VALUE, con / 1000L);
        }
    }

    /** Giay mien phi con lai hom nay (khong tinh kho). */
    public static int giayMienPhiConLai(final CauHinh c, final Phien p, final boolean theThang, final int homNay) {
        synchronized (p) {
            lamMoiNgay(p, homNay);
            return (int) Math.max(0L, (giayToiDa(c, theThang) * 1000L - p.msDung) / 1000L);
        }
    }

    /**
     * "Toi da" gui client (goi -100): tran ngay + kho hien co, de client cu (0.9.21/0.9.22, J2ME) ve "con X / Y" khong
     * bao gio X &gt; Y.
     */
    public static int giayToiDaHien(final CauHinh c, final Phien p, final boolean theThang) {
        synchronized (p) {
            return (int) Math.min(Integer.MAX_VALUE, giayToiDa(c, theThang) + Math.max(0L, p.msKho) / 1000L);
        }
    }

    /** Kiem them {@code ms} vao kho: null = duoc, nguoc lai cau bao (vuot tran). */
    public static String kiemThemKho(final Phien p, final long ms) {
        synchronized (p) {
            if (ms <= 0L) {
                return "Vé treo không hợp lệ.";
            }
            if (p.msKho + ms > KHO_MS_TOI_DA) {
                return "Kho giờ treo tối đa " + KHO_GIO_TOI_DA + " giờ (đang có " + gioPhut(p.msKho)
                        + "). Dùng bớt Tự động đánh rồi dùng vé sau.";
            }
            return null;
        }
    }

    /** Cong {@code ms} vao kho (kep tran). @return kho sau khi cong (ms). */
    public static long themKho(final Phien p, final long ms) {
        synchronized (p) {
            p.msKho = Math.max(0L, Math.min(KHO_MS_TOI_DA, p.msKho + Math.max(0L, ms)));
            return p.msKho;
        }
    }

    /** "12 giờ 05 phút" / "40 phút". */
    public static String gioPhut(final long ms) {
        final long phut = Math.max(0L, ms) / 60000L;
        final long h = phut / 60L, m = phut % 60L;
        return h > 0 ? h + " giờ" + (m > 0 ? " " + (m < 10 ? "0" : "") + m + " phút" : "") : m + " phút";
    }

    /** Ngu canh may chu cung cap khi kiem dieu kien (de lop nay thuan). */
    public static final class NguCanh {
        /** Dang o bai luyen tap thuong (map co Pokemon hoang da, khong phai thanh pho / nha / su kien). */
        public boolean baiLuyen = true;
        /** Dang co co PK / vua giao chien PvP. */
        public boolean pvp;
        /** San dau, san Lien minh, chien truong, khu chiem Gym... */
        public boolean sanDau;
        public boolean chet;
        public boolean theThang;
        /** Cap Pokemon (0 = khong ro, khong kiem tu_dong.cap_toi_thieu). */
        public int cap;
        public int map = -1;
        public int x;
        public int y;
        /** Ban do khoa Tu dong danh (data_ban_do_mo_khoa.cho_phep_tu_dong = 0): cau bao, null = cho phep. */
        public String khoaMap;
    }

    /**
     * Kiem dieu kien BAT. @return null = duoc bat (chua doi trang thai), nguoc lai la cau bao nguoi choi.
     */
    public static String kiemBat(final CauHinh c, final Phien p, final NguCanh n, final int homNay) {
        if (!c.bat) {
            return lyDo(TAT_TAT_TINH_NANG);
        }
        if (c.capToiThieu > 0 && n.cap > 0 && n.cap < c.capToiThieu) {
            return "Tự động đánh mở từ cấp " + c.capToiThieu + " (Pokémon của bạn cấp " + n.cap + ").";
        }
        if (n.map >= 0 && c.mapBiCam(n.map)) {
            return lyDo(TAT_KHU_CAM);
        }
        if (n.khoaMap != null) {
            return n.khoaMap;
        }
        if (n.chet) {
            return "Pokémon đang gục, chưa bật được Tự động đánh.";
        }
        if (c.chiMapDaMo && !n.baiLuyen) {
            return "Chỉ bật Tự động đánh ở bãi luyện tập có Pokémon hoang dã.";
        }
        if (c.tatKhiPvp && n.sanDau) {
            return lyDo(TAT_SAN_DAU);
        }
        if (c.tatKhiPvp && n.pvp) {
            return "Đang ở chế độ PK, hãy tắt PK trước khi bật Tự động đánh.";
        }
        if (giayConLai(c, p, n.theThang, homNay) <= 0) {
            return lyDo(TAT_HET_GIO);
        }
        return null;
    }

    /** Bat phien (sau khi kiemBat == null). banKinhXin <= 0 = ban kinh toi da cua may chu. */
    public static void bat(final CauHinh c, final Phien p, final NguCanh n, final int banKinhXin, final long bay) {
        synchronized (p) {
            p.bat = true;
            p.batLuc = bay;
            p.tichLuc = bay;
            p.neoMap = n.map;
            p.neoX = n.x;
            p.neoY = n.y;
            p.banKinh = banKinhXin <= 0 ? c.banKinh : (int) kep(banKinhXin, BAN_KINH_MIN, c.banKinh);
            p.viPham = 0;
            p.lyDoTat = -1;
            p.soDanh = 0;
            p.soThuoc = 0;
            p.soNhat = 0;
            p.soChan = 0;
            p.mucTieu = -1;
            p.dangHoiPhuc = false;
            p.keCuoi = null;
            p.keLuc = 0L;
            p.lyBao = "";
            p.soKe = 0;
            p.soDanhLanNhip = -1;
        }
    }

    /** Ly do bao nguoi choi (chat tren dau) khi ke hoach "cho" doi ly do: chi khi doi, toi da 1 lan / 20 giay. */
    public static boolean nenBao(final Phien p, final String ly, final long bay) {
        synchronized (p) {
            if (ly == null || ly.isEmpty() || ly.equals(p.lyBao) && bay - p.lyBaoLuc < 120000L || bay - p.lyBaoLuc < 20000L) {
                return false;
            }
            p.lyBao = ly;
            p.lyBaoLuc = bay;
            return true;
        }
    }

    /**
     * Cong thoi gian tu lan ghi nhan truoc toi {@code bay} (moi khoang toi da {@link #KHOANG_TICH_TOI_DA_MS}).
     * Doi ngay giua chung: phan truoc 00:00 khong mang sang ngay moi. @return true = vua HET GIO (da tat).
     */
    public static boolean tich(final CauHinh c, final Phien p, final boolean theThang, final long bay, final int homNay) {
        synchronized (p) {
            if (!p.bat) {
                return false;
            }
            final long them = p.tichLuc > 0L && bay > p.tichLuc ? Math.min(KHOANG_TICH_TOI_DA_MS, bay - p.tichLuc) : 0L;
            p.tichLuc = bay;
            if (p.ngay != homNay) {
                lamMoiNgay(p, homNay); // khoang vat qua 00:00 tinh cho ngay moi tu 0 (khong bu, khong tru)
            } else {
                p.msDung += them;
            }
            final long tran = giayToiDa(c, theThang) * 1000L;
            if (p.msDung > tran) {
                // het gio mien phi: phan vuot tru vao kho gio treo
                final long vuot = p.msDung - tran;
                final long tru = Math.min(vuot, Math.max(0L, p.msKho));
                p.msKho -= tru;
                p.msDung = tran;
            }
            if (p.msDung >= tran && p.msKho <= 0L) {
                p.msDung = tran;
                p.msKho = 0L;
                tat(p, TAT_HET_GIO);
                return true;
            }
            return false;
        }
    }

    /**
     * Pokemon dang tu dong vua guc: duoc TU dung vat pham hoi sinh khong? Chi khi nguoi choi da bat "Tu hoi sinh"
     * va chua qua {@link CauHinh#hoiSinhGio} lan trong 60 phut (cua so truot). GHI NHAN mot lan neu tra true
     * (nguoi goi phai dung vat pham ngay). false = phai dung tu dong (TAT_CHET) va ve Trung tam Pokemon.
     */
    public static boolean choHoiSinh(final CauHinh c, final Phien p, final long bay) {
        synchronized (p) {
            if (!p.bat || !p.xinHoiSinh || c.hoiSinhGio <= 0) {
                return false;
            }
            if (!p.vHoiSinh.choPhep(new Nhip(c.hoiSinhGio, 3600000L), bay)) {
                return false;
            }
            p.soHoiSinh++;
            return true;
        }
    }

    public static void tat(final Phien p, final int lyDo) {
        synchronized (p) {
            p.bat = false;
            p.lyDoTat = lyDo;
        }
    }

    /**
     * Mot hanh dong (danh / thuoc / nhat) luc dang tu dong. @return true = cho phep. Vuot nhip thi bo va dem vi
     * pham; du {@link CauHinh#viPhamTat} lan thi tat phien (TAT_VI_PHAM).
     */
    public static boolean choPhep(final CauHinh c, final Phien p, final int hd, final long bay) {
        synchronized (p) {
            if (!p.bat) {
                return true;
            }
            final boolean ok;
            if (hd == HD_DANH) {
                ok = p.vDanh.choPhep(c.nhipDanh, bay);
                if (ok) {
                    p.soDanh++;
                }
            } else if (hd == HD_THUOC) {
                ok = p.vThuoc.choPhep(c.nhipThuoc, bay);
                if (ok) {
                    p.soThuoc++;
                }
            } else {
                ok = p.vNhat.choPhep(c.nhipNhat, bay);
                if (ok) {
                    p.soNhat++;
                }
            }
            if (!ok) {
                p.soChan++;
                if (++p.viPham >= c.viPhamTat) {
                    tat(p, TAT_VI_PHAM);
                }
            }
            return ok;
        }
    }

    /**
     * Nhip danh cua MOI nguoi choi (ke ca khong bat tu dong): chi de do / ghi nghi van, KHONG chan (khong bao gio
     * phat nham nguoi that). @return true = vuot nhip nguoi that.
     */
    public static boolean vuotNhipNguoi(final CauHinh c, final Phien p, final long bay) {
        synchronized (p) {
            return !p.vNguoi.choPhep(c.nhipNguoi, bay);
        }
    }

    /** Ra khoi vung (ban kinh quanh diem neo + le) hoac sang map khac. */
    public static boolean raVung(final Phien p, final int map, final int x, final int y) {
        synchronized (p) {
            if (!p.bat) {
                return false;
            }
            if (map != p.neoMap) {
                return true;
            }
            final long dx = x - p.neoX, dy = y - p.neoY;
            final long r = p.banKinh + BAN_KINH_LE;
            return dx * dx + dy * dy > r * r;
        }
    }

    // ================================================================== ho so (HoSoLauDai a1/a2)

    /** Nap bo dem ngay tu ho so (luc tao phien). */
    public static void napTuHoSo(final Phien p, final HoSoLauDai h) {
        if (h == null) {
            return;
        }
        synchronized (h) {
            synchronized (p) {
                p.ngay = h.tdNgay;
                p.msDung = h.tdMsDung;
                p.msKho = h.tdMsKho;
            }
        }
    }

    /** Ghi bo dem ngay + kho gio treo vao ho so (luu CSDL theo chu ky cua LauDaiNhanVat). */
    public static void ghiHoSo(final Phien p, final HoSoLauDai h) {
        if (h == null) {
            return;
        }
        synchronized (h) {
            synchronized (p) {
                if (h.tdNgay != p.ngay || h.tdMsDung != p.msDung || h.tdMsKho != p.msKho) {
                    h.tdNgay = p.ngay;
                    h.tdMsDung = p.msDung;
                    h.tdMsKho = p.msKho;
                    h.ban = true;
                }
            }
        }
    }
}
