package com.htbb.pokemon;

import com.htbb.cauhinh.CauHinhTrungTam;

/**
 * TU DONG DANH chinh thuc (quyet dinh 18/10/2026: bo mod NQSH phia client - AutoPk, Van may, Len thanh, Giao
 * hang tu dong - thay bang MOT tinh nang "Tu dong danh" do MAY CHU dieu khien). Lop THUAN: test/KiemTuDong.java
 * goi truc tiep; game goi qua real.TuDongGame. Tai lieu: docs/pokemon/TU_DONG_DANH.md.
 *
 * Nguyen tac:
 *   1. Chi chay khi DANG ONLINE. Treo offline van la "Phieu uy thac luyen tap" (TangToc.UY_THAC) - tach rieng,
 *      khong cong / khong tru lan nhau. Ve treo may vi vay giu nguyen gia tri.
 *   2. KHONG thuong them: EXP / xu / do roi khi tu dong di qua dung duong ha quai thuong, nen chiu nguyen nang
 *      luong huan luyen (NangLuong: 100% -> 50% -> 20% -> 0%). Lop nay khong co ham nao cham toi EXP.
 *   3. Tran gio moi ngay (tu_dong.gio_moi_ngay, + tu_dong.gio_them_the_thang khi con the thang). Het gio may chu
 *      tat va bao client.
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

    // ================================================================== khoa cau hinh (tools/cauhinh/danh_muc.json)

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

    // ================================================================== lenh mang (-100, hai chieu)

    /** Ma lenh client <-> server cua Tu dong danh. */
    public static final int LENH = -100;
    /** Client -> server: byte loai. */
    public static final int C_XIN = 0, C_BAT = 1, C_TAT = 2, C_NHIP = 3;
    /** Co (bit) trong goi trang thai server -> client. */
    public static final int CO_TINH_NANG = 1, CO_THUOC = 2, CO_TAT_PVP = 4, CO_CHI_MAP = 8;

    // ================================================================== ly do tat (ghi kiem toan + bao client)

    public static final int TAT_NGUOI_CHOI = 0, TAT_HET_GIO = 1, TAT_PVP = 2, TAT_BOSS = 3, TAT_DOI_MAP = 4,
            TAT_RA_VUNG = 5, TAT_VI_PHAM = 6, TAT_CHET = 7, TAT_TAT_TINH_NANG = 8, TAT_SAN_DAU = 9, TAT_THOAT = 10;
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

        public int co() {
            return (bat ? CO_TINH_NANG : 0) | (tuDungThuoc ? CO_THUOC : 0) | (tatKhiPvp ? CO_TAT_PVP : 0)
                    | (chiMapDaMo ? CO_CHI_MAP : 0);
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
        } catch (final Throwable t) {
            // cau hinh loi -> giu mac dinh an toan (van co tran gio)
        }
        return c;
    }

    static long kep(final long v, final long lo, final long hi) {
        return Math.max(lo, Math.min(hi, v));
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

    /** Giay con lai hom nay. */
    public static int giayConLai(final CauHinh c, final Phien p, final boolean theThang, final int homNay) {
        synchronized (p) {
            lamMoiNgay(p, homNay);
            final long con = giayToiDa(c, theThang) * 1000L - p.msDung;
            return (int) Math.max(0L, con / 1000L);
        }
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
        public int map = -1;
        public int x;
        public int y;
    }

    /**
     * Kiem dieu kien BAT. @return null = duoc bat (chua doi trang thai), nguoc lai la cau bao nguoi choi.
     */
    public static String kiemBat(final CauHinh c, final Phien p, final NguCanh n, final int homNay) {
        if (!c.bat) {
            return lyDo(TAT_TAT_TINH_NANG);
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
            if (p.msDung >= giayToiDa(c, theThang) * 1000L) {
                p.msDung = giayToiDa(c, theThang) * 1000L;
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
            }
        }
    }

    /** Ghi bo dem ngay vao ho so (luu CSDL theo chu ky cua LauDaiNhanVat). */
    public static void ghiHoSo(final Phien p, final HoSoLauDai h) {
        if (h == null) {
            return;
        }
        synchronized (h) {
            synchronized (p) {
                if (h.tdNgay != p.ngay || h.tdMsDung != p.msDung) {
                    h.tdNgay = p.ngay;
                    h.tdMsDung = p.msDung;
                    h.ban = true;
                }
            }
        }
    }
}
