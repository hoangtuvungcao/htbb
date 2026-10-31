package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.DoiLuong;
import com.htbb.pokemon.DoiLuongDb;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.SanLuong;
import data.Database;
import data.LenhWeb;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import server.TeamServer;

/**
 * Doi luong o NPC Tong Quan (-10, muc "Đổi lượng") + lenh web + vong quet san ({@link DoiLuong}, docs/thiet-ke-game.md 9.1).
 * Chi dung menu / hop nhap so / thong bao co san (goi -30 / -31 / alert) nen client cu nao cung dung duoc.
 *
 * Npc ao -570..-578 (danh rieng): -570 goc, -571 chon so doi khoa, -572 xac nhan doi khoa, -573 chon so doi xu,
 * -574 xac nhan doi xu, -575 san, -576 xac nhan dat lenh, -577 lenh cua toi, -578 xac nhan huy lenh.
 * Hop nhap so: -32301..-32306.
 *
 * NGUYEN TU. Doi khoa / doi xu: kiem va doi trong MOT lan giu Char.KHOA_TIEN (Char.truNeuDu), roi saveCharAuto ngay.
 * Doi xu giu tran ngay trong CSDL truoc (UPDATE co dieu kien), doi hong thi tra tran. Lenh web: chot dong lenh
 * (trang_thai CHO / CHO_DANG_NHAP -> DANG_XU_LY, UPDATE co dieu kien) TRUOC khi doi - web huy cung dong thi chi mot ben
 * thang; tob_doi_luong_log.lenh_id UNIQUE chan ap dung lan hai.
 */
public final class DoiLuongGame implements Runnable {

    public static final String TEN_MUC = "Đổi lượng (khoá / xu / sàn)";

    public static final int NPC_GOC = -570;
    public static final int NPC_SO_KHOA = -571;
    public static final int NPC_XN_KHOA = -572;
    public static final int NPC_SO_XU = -573;
    public static final int NPC_XN_XU = -574;
    public static final int NPC_SAN = -575;
    public static final int NPC_XN_LENH = -576;
    public static final int NPC_LENH_TOI = -577;
    public static final int NPC_XN_HUY = -578;

    public static final int INPUT_SO_KHOA = -32301;
    public static final int INPUT_SO_XU = -32302;
    public static final int INPUT_BAN_SO = -32303;
    public static final int INPUT_BAN_GIA = -32304;
    public static final int INPUT_MUA_SO = -32305;
    public static final int INPUT_MUA_GIA = -32306;

    public static final String LENH_KHOA = "doi_luong_khoa";
    public static final String LENH_XU = "doi_luong_xu";

    static final long CHU_KY_MS = 15000L;
    static final int[] MOC = {10, 50, 100};

    /** charDBID -> vi tri muc trong menu NPC -10. */
    private static final ConcurrentHashMap<Integer, Integer> VI_TRI = new ConcurrentHashMap<Integer, Integer>();
    /** charDBID -> {kieu, long[] du lieu} cua buoc menu dang mo (kieu sai = goi tin lac buoc, bo qua). */
    private static final ConcurrentHashMap<Integer, Object[]> NGU_CANH = new ConcurrentHashMap<Integer, Object[]>();

    static final String NC_TOI_DA = "toi_da";
    static final String NC_XN_KHOA = "xn_khoa";
    static final String NC_XN_XU = "xn_xu";
    static final String NC_NHAP_LENH = "nhap_lenh";
    static final String NC_XN_LENH = "xn_lenh";
    static final String NC_DS_LENH = "ds_lenh";
    static final String NC_XN_HUY = "xn_huy";

    static void datNc(final Char p, final String kieu, final long... d) {
        NGU_CANH.put(p.charDBID, new Object[]{kieu, d});
    }

    /** Du lieu cua buoc {@code kieu}, null neu buoc dang mo khac. xoa = dung mot lan. */
    static long[] layNc(final Char p, final String kieu, final boolean xoa) {
        final Object[] c = xoa ? NGU_CANH.remove(p.charDBID) : NGU_CANH.get(p.charDBID);
        return c != null && kieu.equals(c[0]) ? (long[]) c[1] : null;
    }

    private static volatile boolean daChay = false;
    private static volatile LocalDate ngayTiGia = null;
    private static volatile long tiGiaNho = 0L;

    private DoiLuongGame() {
    }

    // ================================================================== cau hinh

    static long tiLeKhoa() {
        return Math.max(0L, Math.min(100L, CauHinhTrungTam.so(DoiLuong.K_TI_LE_KHOA, DoiLuong.TI_LE_KHOA)));
    }

    static long tranXu() {
        return Math.max(0L, CauHinhTrungTam.so(DoiLuong.K_XU_TRAN_NGAY, DoiLuong.XU_TRAN_NGAY));
    }

    static long phiSan() {
        return Math.max(0L, Math.min(50L, CauHinhTrungTam.so(DoiLuong.K_SAN_PHI, DoiLuong.SAN_PHI)));
    }

    static long tranSan() {
        return Math.max(0L, CauHinhTrungTam.so(DoiLuong.K_SAN_TRAN_NGAY, DoiLuong.SAN_TRAN_NGAY));
    }

    static LocalDate homNay() {
        return DoiLuong.ngay(System.currentTimeMillis());
    }

    /**
     * Ti gia he thong hom nay (xu / 1 luong), chung toan may chu, dong bang ca ngay. Chua co dong cua ngay thi tinh
     * (cap trung vi nhan vat that hoat dong 7 ngay) va INSERT IGNORE, roi doc lai dong da ghi (ai ghi truoc thang).
     * 0 = tat (phan_tram_gio = 0) hoac loi CSDL.
     */
    public static long tiGiaHomNay() {
        final LocalDate ngay = homNay();
        final long phanTram = CauHinhTrungTam.so(DoiLuong.K_XU_PHAN_TRAM_GIO, DoiLuong.XU_PHAN_TRAM_GIO);
        if (ngay.equals(ngayTiGia) && tiGiaNho > 0L) {
            return phanTram <= 0L ? 0L : tiGiaNho; // tat giua ngay co hieu luc ngay; doi % thi tu ngay sau
        }
        try {
            long[] d = DoiLuongDb.tiGiaNgay(ngay);
            if (d == null) {
                if (phanTram <= 0L) {
                    return 0L;
                }
                final int[] cap = DoiLuongDb.capHoatDong(System.currentTimeMillis());
                final int tv = DoiLuong.capTrungVi(cap);
                final long xuGio = LauDai.xuMoiGio(tv);
                DoiLuongDb.ghiTiGia(ngay, tv, cap.length, xuGio, phanTram, DoiLuong.tiGia(xuGio, phanTram));
                d = DoiLuongDb.tiGiaNgay(ngay);
                System.out.println("[DOI LUONG] ti gia " + ngay + ": cap trung vi " + tv + " (" + cap.length + " nguoi), "
                        + xuGio + " xu/gio x " + phanTram + "% = " + (d == null ? "?" : String.valueOf(d[4])) + " xu / 1 luong");
            }
            if (d == null) {
                return 0L;
            }
            ngayTiGia = ngay;
            tiGiaNho = d[4];
            return phanTram <= 0L ? 0L : d[4];
        } catch (final Exception e) {
            System.out.println("[DOI LUONG] LOI ti gia: " + e);
            return 0L;
        }
    }

    // ================================================================== tien ich

    static String so(final long n) {
        final String s = String.valueOf(Math.abs(n));
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (n < 0 ? "-" : "") + b;
    }

    static void bao(final Char p, final String s) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
        } catch (final Exception ignored) {
        }
    }

    static void menu(final Char p, final String[] m, final int npc) {
        p.sendMessage(MessageCreator.createMsgMenu(m, npc, 0));
    }

    static boolean chanBot(final Char p) {
        if (p == null || p.isBot != -1 || HlvAiGame.laBot(p)) {
            if (p != null) {
                bao(p, "Huấn luyện viên AI không dùng chức năng đổi lượng.");
            }
            return true;
        }
        return false;
    }

    static void luu(final Char p) {
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception ignored) {
        }
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Exception e) {
            System.out.println("[DOI LUONG] LOI luu " + p.charname + ": " + e);
        }
    }

    // ================================================================== NPC -10

    /** RequestNPCInfoHandler (-10): noi muc "Đổi lượng" vao cuoi menu, nho vi tri. */
    public static String[] noiMenuTongQuan(final Char p, final String[] menu) {
        if (p == null || menu == null) {
            return menu;
        }
        final String[] m = java.util.Arrays.copyOf(menu, menu.length + 1);
        m[menu.length] = TEN_MUC;
        VI_TRI.put(p.charDBID, menu.length);
        return m;
    }

    /** MapDoMenu: true neu da xu ly. */
    public static boolean xuLyMenu(final Char p, final int idNpc, final int idMenu, final int chon) {
        if (p == null) {
            return false;
        }
        if (idNpc == -10) {
            final Integer vt = VI_TRI.get(p.charDBID);
            if (idMenu != 0 || vt == null || vt != chon) {
                return false;
            }
            moGoc(p);
            return true;
        }
        if (idNpc > NPC_GOC || idNpc < NPC_XN_HUY) {
            return false;
        }
        if (chanBot(p)) {
            return true;
        }
        try {
            switch (idNpc) {
                case NPC_GOC:
                    chonGoc(p, chon);
                    break;
                case NPC_SO_KHOA:
                    chonSo(p, chon, false);
                    break;
                case NPC_SO_XU:
                    chonSo(p, chon, true);
                    break;
                case NPC_XN_KHOA:
                    xacNhanDoi(p, chon, false);
                    break;
                case NPC_XN_XU:
                    xacNhanDoi(p, chon, true);
                    break;
                case NPC_SAN:
                    chonSan(p, chon);
                    break;
                case NPC_XN_LENH:
                    xacNhanLenh(p, chon);
                    break;
                case NPC_LENH_TOI:
                    chonLenhToi(p, chon);
                    break;
                case NPC_XN_HUY:
                    xacNhanHuy(p, chon);
                    break;
                default:
                    break;
            }
        } catch (final Exception e) {
            System.out.println("[DOI LUONG] LOI menu " + idNpc + " cua " + p.charname + ": " + e);
            bao(p, "Có lỗi, thử lại sau.");
        }
        return true;
    }

    /** MapNhapText: true neu da xu ly. */
    public static boolean xuLyNhap(final Char p, final int idActor, final String text) {
        if (idActor > INPUT_SO_KHOA || idActor < INPUT_MUA_GIA || p == null) {
            return false;
        }
        if (chanBot(p)) {
            return true;
        }
        long n;
        try {
            n = Long.parseLong(text == null ? "" : text.trim().replace(".", "").replace(",", ""));
        } catch (final Exception e) {
            bao(p, "Số không hợp lệ.");
            return true;
        }
        if (n <= 0L || n > Integer.MAX_VALUE) {
            bao(p, "Số phải lớn hơn 0.");
            return true;
        }
        try {
            nhapSo(p, idActor, n);
        } catch (final Exception e) {
            System.out.println("[DOI LUONG] LOI nhap " + idActor + " cua " + p.charname + ": " + e);
            bao(p, "Có lỗi, thử lại sau.");
        }
        return true;
    }

    static void nhapSo(final Char p, final int idActor, final long n) throws Exception {
        switch (idActor) {
            case INPUT_SO_KHOA:
                hoiXacNhan(p, n, false);
                break;
            case INPUT_SO_XU:
                hoiXacNhan(p, n, true);
                break;
            case INPUT_BAN_SO:
            case INPUT_MUA_SO:
                datNc(p, NC_NHAP_LENH, idActor == INPUT_BAN_SO ? DoiLuongDb.LOAI_BAN : DoiLuongDb.LOAI_MUA, n);
                final long[] k = DoiLuong.khungGia(tiGiaHomNay());
                p.sendMessage(MessageCreator.createMsgInputText(idActor == INPUT_BAN_SO ? INPUT_BAN_GIA : INPUT_MUA_GIA, 0,
                        "Giá mỗi lượng (xu)" + (k == null ? "" : ", từ " + so(k[0]) + " đến " + so(k[1])), 1));
                break;
            default:
                hoiDatLenh(p, n);
                break;
        }
    }

    static void moGoc(final Char p) {
        if (chanBot(p)) {
            return;
        }
        final long tl = tiLeKhoa();
        final long tg = tiGiaHomNay();
        menu(p, new String[]{
            tl > 0L ? "Đổi lượng → lượng khoá (1 lượng = " + tl + " lượng khoá)" : "Đổi lượng → lượng khoá (đang tắt)",
            tg > 0L && tranXu() > 0L ? "Đổi lượng → xu (hôm nay 1 lượng = " + so(tg) + " xu)" : "Đổi lượng → xu (đang tắt)",
            "Sàn đổi lượng - xu (giữa người chơi)",
            "Hướng dẫn"
        }, NPC_GOC);
    }

    static void chonGoc(final Char p, final int chon) throws Exception {
        if (chon == 0 || chon == 1) {
            moChonSo(p, chon == 1);
        } else if (chon == 2) {
            moSan(p);
        } else if (chon == 3) {
            bao(p, "Đổi lượng (chỉ từ lượng thường trong người, không dùng lượng trong kho, không bao giờ dùng lượng khoá):\n"
                    + "• Lượng → lượng khoá: 1 lượng = " + tiLeKhoa() + " lượng khoá, một chiều, không giới hạn.\n"
                    + "• Lượng → xu: tỉ giá chung toàn máy chủ, tính lúc 00:00 mỗi ngày (" + CauHinhTrungTam.so(DoiLuong.K_XU_PHAN_TRAM_GIO,
                            DoiLuong.XU_PHAN_TRAM_GIO) + "% thu nhập 1 giờ ở cấp trung vị), tối đa " + so(tranXu())
                    + " lượng / tài khoản / ngày.\n"
                    + "• Sàn: đặt lệnh bán / mua lượng thường bằng xu với người chơi khác; phí " + phiSan()
                    + "% xu bên mua (bị đốt); lệnh tự huỷ sau " + DoiLuong.SAN_HAN_GIO + " giờ. Lượng khoá không bao giờ lên sàn.\n"
                    + "Không có đường đổi ngược lượng khoá / xu ra lượng.");
        }
    }

    // ------------------------------------------------------------------ doi khoa / xu (he thong)

    static long conXuHomNay(final Char p) throws Exception {
        return DoiLuong.conLai(tranXu(), DoiLuongDb.daDung(p.userID, DoiLuong.TRAN_XU, homNay()));
    }

    static void moChonSo(final Char p, final boolean xu) throws Exception {
        final long luong = p.getLuong();
        long toiDa = luong;
        String dau;
        if (xu) {
            final long tg = tiGiaHomNay();
            if (tg <= 0L || tranXu() <= 0L) {
                bao(p, "Đổi lượng → xu đang tắt.");
                return;
            }
            final long con = conXuHomNay(p);
            toiDa = Math.min(luong, con);
            dau = "Tỉ giá hôm nay: 1 lượng = " + so(tg) + " xu (áp dụng chung toàn máy chủ). Hôm nay còn đổi được " + so(con)
                    + " lượng. Bạn có " + so(luong) + " lượng.";
        } else {
            if (tiLeKhoa() <= 0L) {
                bao(p, "Đổi lượng → lượng khoá đang tắt.");
                return;
            }
            dau = "Tỉ lệ 1 lượng = " + tiLeKhoa() + " lượng khoá (một chiều, không đổi ngược). Bạn có " + so(luong) + " lượng.";
        }
        final String[] m = new String[MOC.length + 3];
        m[0] = dau;
        for (int i = 0; i < MOC.length; i++) {
            m[i + 1] = "Đổi " + MOC[i] + " lượng";
        }
        m[MOC.length + 1] = "Đổi tối đa (" + so(Math.max(0L, toiDa)) + " lượng)";
        m[MOC.length + 2] = "Nhập số khác";
        datNc(p, NC_TOI_DA, toiDa, xu ? 1L : 0L);
        menu(p, m, xu ? NPC_SO_XU : NPC_SO_KHOA);
    }

    static void chonSo(final Char p, final int chon, final boolean xu) throws Exception {
        if (chon == 0) {
            moChonSo(p, xu);
            return;
        }
        if (chon >= 1 && chon <= MOC.length) {
            hoiXacNhan(p, MOC[chon - 1], xu);
        } else if (chon == MOC.length + 1) {
            final long[] c = layNc(p, NC_TOI_DA, false);
            final long toiDa = c != null && c[1] == (xu ? 1L : 0L) ? c[0] : 0L;
            if (toiDa <= 0L) {
                bao(p, xu ? "Không đủ lượng hoặc đã hết hạn mức đổi hôm nay." : "Bạn không có lượng để đổi.");
                return;
            }
            hoiXacNhan(p, toiDa, xu);
        } else if (chon == MOC.length + 2) {
            p.sendMessage(MessageCreator.createMsgInputText(xu ? INPUT_SO_XU : INPUT_SO_KHOA, 0, "Nhập số lượng muốn đổi", 1));
        }
    }

    static void hoiXacNhan(final Char p, final long n, final boolean xu) throws Exception {
        final String cau;
        if (xu) {
            final long tg = tiGiaHomNay();
            final long nhan = DoiLuong.xuNhan(n, tg);
            if (nhan <= 0L) {
                bao(p, "Đổi lượng → xu đang tắt.");
                return;
            }
            cau = "Đồng ý: đổi " + so(n) + " lượng lấy " + so(nhan) + " xu (1 lượng = " + so(tg) + " xu)";
            datNc(p, NC_XN_XU, n, tg);
        } else {
            final long nhan = DoiLuong.luongKhoaNhan(n, tiLeKhoa());
            if (nhan <= 0L) {
                bao(p, "Số không hợp lệ hoặc chức năng đang tắt.");
                return;
            }
            cau = "Đồng ý: đổi " + so(n) + " lượng lấy " + so(nhan) + " lượng khoá";
            datNc(p, NC_XN_KHOA, n, tiLeKhoa());
        }
        menu(p, new String[]{cau, "Huỷ"}, xu ? NPC_XN_XU : NPC_XN_KHOA);
    }

    static void xacNhanDoi(final Char p, final int chon, final boolean xu) throws Exception {
        final long[] d = layNc(p, xu ? NC_XN_XU : NC_XN_KHOA, true);
        if (chon != 0 || d == null) {
            return;
        }
        final String loi = xu ? doiXu(p, d[0], d[1], "npc", 0L) : doiKhoa(p, d[0], d[1], "npc", 0L);
        if (loi != null) {
            bao(p, loi);
        }
    }

    /**
     * Doi n luong THUONG -> luong khoa (ti le tiLe). Nguyen tu tren RAM, luu ngay. @return null = xong (da bao), hoac ly do.
     * @param tiLe ti le da hien cho nguoi choi; khac cau hinh hien tai -> tu choi (khong doi ngam theo ti le moi)
     */
    public static String doiKhoa(final Char p, final long n, final long tiLe, final String nguon, final long lenhId) {
        if (tiLe <= 0L || tiLe != tiLeKhoa()) {
            return "Tỉ lệ đổi vừa thay đổi hoặc chức năng đang tắt, mở lại menu.";
        }
        final long nhan = DoiLuong.luongKhoaNhan(n, tiLe);
        if (nhan <= 0L) {
            return "Số không hợp lệ.";
        }
        if (!p.truNeuDu(() -> p.getLuong() >= n, () -> {
            p.truLuongMoChuyen(n);
            p.addLuongLock((int) nhan);
        })) {
            return "Không đủ lượng (cần " + so(n) + " lượng thường trong người).";
        }
        ghiSauDoi(p, DoiLuong.LY_DO_KHOA, n, nhan, tiLe, nguon, lenhId, "luong_khoa");
        luu(p);
        bao(p, "Đã đổi " + so(n) + " lượng lấy " + so(nhan) + " lượng khoá. Còn " + so(p.getLuong()) + " lượng, "
                + so(p.getLuongLock()) + " lượng khoá.");
        return null;
    }

    /** Doi n luong THUONG -> xu o ti gia tiGia (phai bang ti gia hom nay). Giu tran ngay truoc. */
    public static String doiXu(final Char p, final long n, final long tiGia, final String nguon, final long lenhId)
            throws Exception {
        final long tg = tiGiaHomNay();
        if (tg <= 0L || tranXu() <= 0L) {
            return "Đổi lượng → xu đang tắt.";
        }
        if (tiGia != tg) {
            return "Tỉ giá đã sang ngày mới (1 lượng = " + so(tg) + " xu), mở lại menu để xác nhận.";
        }
        final long xu = DoiLuong.xuNhan(n, tg);
        if (xu <= 0L) {
            return "Số không hợp lệ.";
        }
        final LocalDate ngay = homNay();
        if (!DoiLuongDb.giuTran(p.userID, DoiLuong.TRAN_XU, ngay, n, tranXu())) {
            return "Vượt hạn mức hôm nay: tài khoản còn đổi được " + so(conXuHomNay(p)) + " lượng → xu.";
        }
        if (!p.truNeuDu(() -> p.getLuong() >= n, () -> {
            p.truLuongMoChuyen(n);
            p.addXu(xu, DoiLuong.LY_DO_XU, Char.XU_DEM); // xu SINH RA: dem vao xu_vao (tob_kinh_te_ngay)
        })) {
            DoiLuongDb.traTran(p.userID, DoiLuong.TRAN_XU, ngay, n);
            return "Không đủ lượng (cần " + so(n) + " lượng thường trong người).";
        }
        ghiSauDoi(p, DoiLuong.LY_DO_XU, n, xu, tg, nguon, lenhId, "xu_sinh_ra");
        luu(p);
        bao(p, "Đã đổi " + so(n) + " lượng lấy " + so(xu) + " xu (1 lượng = " + so(tg) + " xu). Hôm nay còn đổi được "
                + so(conXuHomNay(p)) + " lượng.");
        return null;
    }

    static void ghiSauDoi(final Char p, final String lyDo, final long n, final long nhan, final long tiGia,
            final String nguon, final long lenhId, final String loaiTien) {
        try {
            DoiLuongDb.ghiDoi(p.userID, p.charDBID, p.charname, lyDo, (int) n, nhan, tiGia, nguon, lenhId);
        } catch (final Exception e) {
            System.out.println("[DOI LUONG] LOI ghi nhat ky " + lyDo + " " + p.charname + ": " + e);
        }
        try {
            // thong ke chi tieu tuan (tob_log_use_luong, /admin/kinh-te): so luong da doi + so nhan duoc
            Database.instance.saveLogThongKeChiTieu(lyDo, 0, (int) n, nhan, loaiTien);
        } catch (final Exception ignored) {
        }
        if (com.htbb.pokemon.NapTien.bat()) {
            com.htbb.pokemon.NapTien.ghiTieu(n); // luong roi luu thong (dot)
        }
        System.out.println("[DOI LUONG] " + p.charname + " (" + nguon + ") " + lyDo + " " + n + " luong -> " + nhan + " "
                + loaiTien + " (ti gia " + tiGia + ")");
    }

    // ------------------------------------------------------------------ san

    static void moSan(final Char p) throws Exception {
        final long tg = tiGiaHomNay();
        final long[] k24 = DoiLuongDb.khop24h();
        final long tb = DoiLuong.giaTrungBinh(k24[0], k24[1]);
        final int soMo = DoiLuongDb.lenhMoCua(p.charDBID).size();
        menu(p, new String[]{
            "Giá TB 24h: " + (tb > 0L ? so(tb) + " xu/lượng (" + so(k24[1]) + " lượng khớp)" : "chưa có giao dịch")
                    + " · tỉ giá hệ thống " + (tg > 0L ? so(tg) : "—"),
            "Xem sổ lệnh",
            "Đặt lệnh BÁN lượng thường",
            "Đặt lệnh MUA lượng thường",
            "Lệnh của tôi (" + soMo + ")"
        }, NPC_SAN);
    }

    static void chonSan(final Char p, final int chon) throws Exception {
        switch (chon) {
            case 0:
                moSan(p);
                return;
            case 1:
                bao(p, soLenhChu());
                return;
            case 2:
            case 3:
                if (ChongClone.chanMotBen(p, ChongClone.KIEU_KY_GUI)) {
                    return;
                }
                final long tg = tiGiaHomNay();
                if (DoiLuong.khungGia(tg) == null || tranSan() <= 0L) {
                    bao(p, "Sàn chưa mở (chưa có tỉ giá hôm nay hoặc đang tắt).");
                    return;
                }
                final long con = DoiLuong.conLai(tranSan(), DoiLuongDb.daDung(p.userID, DoiLuong.TRAN_SAN, homNay()));
                p.sendMessage(MessageCreator.createMsgInputText(chon == 2 ? INPUT_BAN_SO : INPUT_MUA_SO, 0,
                        (chon == 2 ? "Số lượng thường muốn bán (bạn có " + so(p.getLuong()) + ", lượng khoá không bán được)"
                                : "Số lượng muốn mua (nhận lượng thường)") + ". Hôm nay còn đặt được " + so(con), 1));
                return;
            case 4:
                moLenhToi(p);
                return;
            default:
        }
    }

    static String soLenhChu() throws Exception {
        final StringBuilder b = new StringBuilder("Sổ lệnh lượng thường - xu (giá xu / 1 lượng)\nBÁN (thấp nhất trước):\n");
        final List<Object[]> ban = DoiLuongDb.soLenh(DoiLuongDb.LOAI_BAN, 6);
        for (final Object[] r : ban) {
            b.append("  ").append(so(((Number) r[0]).longValue())).append(" xu × ").append(so(((Number) r[1]).longValue()))
                    .append(" lượng\n");
        }
        if (ban.isEmpty()) {
            b.append("  (trống)\n");
        }
        b.append("MUA (cao nhất trước):\n");
        final List<Object[]> mua = DoiLuongDb.soLenh(DoiLuongDb.LOAI_MUA, 6);
        for (final Object[] r : mua) {
            b.append("  ").append(so(((Number) r[0]).longValue())).append(" xu × ").append(so(((Number) r[1]).longValue()))
                    .append(" lượng\n");
        }
        if (mua.isEmpty()) {
            b.append("  (trống)\n");
        }
        b.append("Phí ").append(phiSan()).append("% xu do bên mua trả, bị đốt. Lượng khoá không giao dịch trên sàn.");
        return b.toString();
    }

    /** Sau khi nhap gia: hien xac nhan voi so tien chinh xac. */
    static void hoiDatLenh(final Char p, final long gia) throws Exception {
        final long[] d = layNc(p, NC_NHAP_LENH, false);
        if (d == null) {
            return;
        }
        final boolean ban = d[0] == DoiLuongDb.LOAI_BAN;
        final long n = d[1];
        final long tg = tiGiaHomNay();
        final long con = DoiLuong.conLai(tranSan(), DoiLuongDb.daDung(p.userID, DoiLuong.TRAN_SAN, homNay()));
        final String loi = DoiLuong.loiLenh(n, gia, tg, con, DoiLuongDb.lenhMoCua(p.charDBID).size());
        if (loi != null) {
            bao(p, loi);
            return;
        }
        final String cau;
        if (ban) {
            cau = "Đồng ý BÁN " + so(n) + " lượng thường giá " + so(gia) + " xu/lượng (giữ " + so(n) + " lượng, nhận tối đa "
                    + so(n * gia) + " xu)";
        } else {
            final long kq = DoiLuong.kyQuyMua(n, gia, phiSan());
            cau = "Đồng ý MUA " + so(n) + " lượng giá tối đa " + so(gia) + " xu/lượng (giữ " + so(kq) + " xu gồm phí "
                    + phiSan() + "%, dư hoàn lại)";
        }
        datNc(p, NC_XN_LENH, d[0], n, gia, tg);
        menu(p, new String[]{cau, "Huỷ"}, NPC_XN_LENH);
    }

    static void xacNhanLenh(final Char p, final int chon) throws Exception {
        final long[] d = layNc(p, NC_XN_LENH, true);
        if (chon != 0 || d == null) {
            return;
        }
        final String loi = datLenh(p, (int) d[0], d[1], d[2], d[3]);
        if (loi != null) {
            bao(p, loi);
        }
    }

    /**
     * Dat lenh tren san. Thu tu an toan (sap may chu giua chung: MAT cua nguoi choi, khong NHAN DOI): giu tran -> tru ky
     * quy (RAM, nguyen tu) -> LUU nhan vat -> ghi lenh + khop (mot giao dich). Hong o buoc sau thi hoan + luu + tra tran.
     */
    public static String datLenh(final Char p, final int loai, final long n, final long gia, final long tiGiaDaHien)
            throws Exception {
        if (chanBot(p) || ChongClone.chanMotBen(p, ChongClone.KIEU_KY_GUI)) {
            return null;
        }
        final long tg = tiGiaHomNay();
        if (tg != tiGiaDaHien) {
            return "Tỉ giá đã sang ngày mới, đặt lại lệnh.";
        }
        final LocalDate ngay = homNay();
        final long con = DoiLuong.conLai(tranSan(), DoiLuongDb.daDung(p.userID, DoiLuong.TRAN_SAN, ngay));
        final String loi = DoiLuong.loiLenh(n, gia, tg, con, DoiLuongDb.lenhMoCua(p.charDBID).size());
        if (loi != null) {
            return loi;
        }
        final boolean ban = loai == DoiLuongDb.LOAI_BAN;
        final long phi = phiSan();
        final long kyQuy = ban ? 0L : DoiLuong.kyQuyMua(n, gia, phi);
        if (!ban && kyQuy <= 0L) {
            return "Số tiền quá lớn.";
        }
        if (!DoiLuongDb.giuTran(p.userID, DoiLuong.TRAN_SAN, ngay, n, tranSan())) {
            return "Vượt hạn mức sàn hôm nay của tài khoản.";
        }
        final boolean daTru = ban ? p.truLuongMoChuyen(n) // CHI luong thuong; luong khoa khong bao gio len san
                : p.truXuNeuDu(kyQuy, false, "san_luong ky quy", Char.XU_CHUYEN);
        if (!daTru) {
            DoiLuongDb.traTran(p.userID, DoiLuong.TRAN_SAN, ngay, n);
            return ban ? "Không đủ " + so(n) + " lượng thường trong người." : "Không đủ " + so(kyQuy) + " xu để ký quỹ.";
        }
        boolean daLuu = false;
        try {
            daLuu = Database.instance.saveCharAuto(p);
        } catch (final Exception ignored) {
        }
        SanLuong.KetQua kq = null;
        if (daLuu) {
            try {
                kq = SanLuong.datLenh(loai, p.charDBID, p.userID, p.charname, gia, (int) n, kyQuy, phi, ngay,
                        (l, xu) -> ChongClone.chanChuyen(p, l.charId, l.ten, ChongClone.KIEU_KY_GUI, xu));
            } catch (final Exception e) {
                System.out.println("[SAN LUONG] LOI dat lenh " + p.charname + ": " + e);
            }
        }
        if (kq == null) {
            if (ban) {
                p.addLuong((int) n);
            } else {
                p.addXu(kyQuy, "san_luong hoan ky quy", Char.XU_CHUYEN);
            }
            DoiLuongDb.traTran(p.userID, DoiLuong.TRAN_SAN, ngay, n);
            luu(p);
            return "Không đặt được lệnh (lỗi máy chủ), đã hoàn lại.";
        }
        if (kq.phi > 0L) {
            LauDai.ghiXuRa(kq.phi); // phi san bi dot
            try {
                Database.instance.saveLogThongKeChiTieu(DoiLuong.LY_DO_SAN_PHI, 0, kq.daKhop, kq.phi, "xu_dot");
            } catch (final Exception ignored) {
            }
        }
        System.out.println("[SAN LUONG] " + p.charname + " " + (ban ? "BAN" : "MUA") + " " + n + " gia " + gia + " -> lenh #"
                + kq.idLenh + ", khop " + kq.daKhop + ", con cho " + kq.conCho + ", phi " + kq.phi);
        luu(p);
        bao(p, "Đã đặt lệnh #" + kq.idLenh + (ban ? " BÁN " : " MUA ") + so(n) + " lượng giá " + so(gia) + " xu/lượng. "
                + (kq.daKhop > 0 ? "Khớp ngay " + so(kq.daKhop) + " lượng" + (ban ? " (nhận " + so(kq.xuNhan) + " xu)" : "")
                + ". " : "") + (kq.conCho > 0 ? "Còn chờ " + so(kq.conCho) + " lượng (tự huỷ sau " + DoiLuong.SAN_HAN_GIO
                + " giờ)." : "Lệnh đã khớp hết."));
        giaoTra(p);
        for (final Integer id : kq.doiTac) {
            final Char o = timOnline(id);
            if (o != null) {
                giaoTra(o);
            }
        }
        return null;
    }

    static void moLenhToi(final Char p) throws Exception {
        final List<Object[]> ds = DoiLuongDb.lenhMoCua(p.charDBID);
        if (ds.isEmpty()) {
            bao(p, "Bạn không có lệnh nào đang mở trên sàn.");
            return;
        }
        final long[] ids = new long[ds.size()];
        final String[] m = new String[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            final Object[] r = ds.get(i);
            final long id = ((Number) r[0]).longValue();
            final boolean ban = ((Number) r[1]).intValue() == DoiLuongDb.LOAI_BAN;
            ids[i] = id;
            m[i] = "Huỷ #" + id + (ban ? " BÁN " : " MUA ") + so(((Number) r[4]).longValue()) + "/" + so(((Number) r[3]).longValue())
                    + " lượng giá " + so(((Number) r[2]).longValue());
        }
        datNc(p, NC_DS_LENH, ids);
        menu(p, m, NPC_LENH_TOI);
    }

    static void chonLenhToi(final Char p, final int chon) {
        final long[] ids = layNc(p, NC_DS_LENH, false);
        if (ids == null || chon < 0 || chon >= ids.length) {
            return;
        }
        final long id = ids[chon];
        datNc(p, NC_XN_HUY, id);
        menu(p, new String[]{"Đồng ý huỷ lệnh #" + id + " (hoàn phần chưa khớp)", "Không"}, NPC_XN_HUY);
    }

    static void xacNhanHuy(final Char p, final int chon) throws Exception {
        final long[] c = layNc(p, NC_XN_HUY, true);
        if (chon != 0 || c == null) {
            return;
        }
        // huyLenh chi dong lenh cua CHINH nhan vat nay (char_id), id gia tu goi tin khong huy duoc lenh nguoi khac
        final long[] d = SanLuong.huyLenh(c[0], p.charDBID);
        if (d == null) {
            bao(p, "Lệnh không còn mở (đã khớp / huỷ / hết hạn).");
            return;
        }
        // huy cung ngay dat: tra lai phan tran chua khop
        if (d[3] == homNay().toEpochDay()) {
            DoiLuongDb.traTran(p.userID, DoiLuong.TRAN_SAN, homNay(), d[1]);
        }
        bao(p, "Đã huỷ lệnh. Hoàn " + (d[0] == DoiLuongDb.LOAI_BAN ? so(d[1]) + " lượng" : so(d[2]) + " xu") + ".");
        giaoTra(p);
    }

    // ------------------------------------------------------------------ tra tien (hang doi)

    /** Giao moi khoan cho tra cho nhan vat dang online (chot 0 -> 1 roi moi cong, luu ngay). */
    public static void giaoTra(final Char p) {
        if (p == null || p.getSession() == null) {
            return;
        }
        try {
            long xu = 0L;
            long luong = 0L;
            for (final Object[] r : DoiLuongDb.traCho(p.charDBID)) {
                final long id = ((Number) r[0]).longValue();
                final long x = ((Number) r[1]).longValue();
                final long l = ((Number) r[2]).longValue();
                if (!DoiLuongDb.chotTra(id)) {
                    continue;
                }
                try {
                    if (x > 0L) {
                        p.addXu(x, "san_luong tra #" + id, Char.XU_CHUYEN);
                    }
                    if (l > 0L) {
                        p.addLuong((int) l);
                    }
                    xu += x;
                    luong += l;
                } catch (final Exception e) {
                    DoiLuongDb.boChotTra(id);
                }
            }
            if (xu > 0L || luong > 0L) {
                luu(p);
                bao(p, "Sàn đổi lượng: nhận " + (xu > 0L ? so(xu) + " xu" : "") + (xu > 0L && luong > 0L ? " và " : "")
                        + (luong > 0L ? so(luong) + " lượng thường" : "") + " (khớp lệnh / hoàn ký quỹ).");
            }
        } catch (final Exception e) {
            System.out.println("[SAN LUONG] LOI giao tra " + p.charname + ": " + e);
        }
    }

    static Char timOnline(final int charId) {
        return LenhWeb.timNguoiChoiOnline(charId);
    }

    // ================================================================== lenh web

    public static boolean laLenh(final String loai) {
        return LENH_KHOA.equals(loai) || LENH_XU.equals(loai);
    }

    /**
     * Lenh web doi luong cua nguoi choi (cap_do = so luong). Online -> chot dong (-> DANG_XU_LY) roi doi ngay tren RAM;
     * offline -> cho, doi ngay khi nhan vat vao game (KHONG ghi thang tob_char: luong khoa nam trong pInfo, lan nap / luu
     * cua game khong khoa dong - ghi tay se bi de hoac de mat). trangThai[0] = trang thai hien tai cua dong (doi thanh
     * DANG_XU_LY sau khi chot).
     */
    public static LenhWeb.KetQua xuLyLenhWeb(final long id, final String loai, final int charId, final int so,
            final int[] trangThai) throws Exception {
        if (so <= 0) {
            throw new IllegalArgumentException("so luong phai > 0");
        }
        final Char p = timOnline(charId);
        if (p == null) {
            if (trangThai[0] == LenhWeb.CHO_DANG_NHAP) {
                return new LenhWeb.KetQua(LenhWeb.CHO_DANG_NHAP, "");
            }
            return new LenhWeb.KetQua(LenhWeb.CHO_DANG_NHAP, "offline: se doi ngay khi Pokemon vao game");
        }
        if (p.isBot != -1 || HlvAiGame.laBot(p)) {
            return new LenhWeb.KetQua(LenhWeb.LOI, "huan luyen vien AI khong doi luong");
        }
        if (!LenhWeb.chotDangXuLy(id, trangThai[0])) {
            return new LenhWeb.KetQua(trangThai[0], ""); // web vua huy / vong khac da chot
        }
        trangThai[0] = LenhWeb.DANG_XU_LY;
        final String loi = LENH_KHOA.equals(loai) ? doiKhoa(p, so, tiLeKhoa(), "web", id)
                : doiXu(p, so, tiGiaHomNay(), "web", id);
        if (loi != null) {
            return new LenhWeb.KetQua(LenhWeb.LOI, loi);
        }
        return new LenhWeb.KetQua(LenhWeb.XONG_ONLINE, LENH_KHOA.equals(loai) ? "da doi " + so + " luong -> luong khoa"
                : "da doi " + so + " luong -> xu");
    }

    // ================================================================== vong quet

    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(new DoiLuongGame());
        t.setName("DOI LUONG");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        DoiLuongDb.taoBang();
        while (TeamServer.running) {
            try {
                motVong();
            } catch (final Throwable e) {
                System.out.println("[DOI LUONG] loi vong quet: " + e);
            }
            try {
                Thread.sleep(CHU_KY_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    static void motVong() throws Exception {
        tiGiaHomNay(); // sang ngay moi (00:00) -> tinh va dong bang ti gia
        for (final long[] d : SanLuong.hetHan()) {
            final Char o = timOnline((int) d[5]);
            if (o != null) {
                bao(o, "Lệnh sàn đổi lượng đã hết hạn " + DoiLuong.SAN_HAN_GIO + " giờ, phần chưa khớp được hoàn lại.");
            }
        }
        final List<Integer> cho = DoiLuongDb.nguoiChoTra();
        if (cho.isEmpty()) {
            return;
        }
        final Vector<Char> ds = CharManager.instance.vChars;
        for (int i = ds.size() - 1; i >= 0; --i) {
            try {
                final Char c = ds.get(i);
                if (c != null && LenhWeb.laNguoiChoiThat(c) && cho.contains(c.charDBID)) {
                    giaoTra(c);
                }
            } catch (final Exception ignored) {
            }
        }
    }

    /** Chi de test / log: moc thoi gian dang dung. */
    static Timestamp bay() {
        return new Timestamp(System.currentTimeMillis());
    }
}
