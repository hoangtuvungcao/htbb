package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.Item;
import real.MessageCreator;
import real.Monster;
import real.MonsterTemplate;

/**
 * Menu he sinh thai (npc ao {@link HeSinhThaiGame#NPC_ID}) - chi dung menu + thong bao co san cua
 * client (khong goi tin moi). Mo tu: muc "Hệ sinh thái" o NPC doi thuong (-90), hoac go "hst" trong
 * chat. Phim tat chat: "cauca", "dao", "dua".
 *
 * MOI dieu kien (the luc, tien, vat lieu, so huu pet, cho trong hanh trang) kiem LAI o day khi
 * nguoi choi bam - khong tin trang thai menu cu (CODING_CONVENTION muc 11).
 */
public final class HeSinhThaiMenu {

    private HeSinhThaiMenu() {
    }

    static final int M_GOC = 0, M_CONG_THUC = 1, M_TUI = 2, M_TUI_HD = 3, M_TUI_GIA = 4, M_TUI_SO = 5, M_NHA_TRE = 6,
            M_NHA_TRE_GUI = 7, M_TRUNG = 8, M_TRUNG_HD = 9, M_CHO = 10, M_CHO_LOAI = 11, M_CHO_MUA = 12,
            M_CHO_CUA_TOI = 13, M_DON = 14, M_DON_GIAO = 15, M_DON_TAO_MA = 16, M_DON_TAO_SO = 17, M_DON_TAO_GIA = 18,
            M_VUON = 19, M_VUON_DAT = 20, M_VUON_BAN = 21, M_SUU_TAM = 22, M_GIAI_TRI = 23, M_TUI_BAN_NPC = 24,
            M_VUON_CAT = 25, M_VUNG = 26, M_DON_CUA_TOI = 27, M_TRUNG_GIA = 28, M_DANH_HIEU = 29;

    /** Trang thai menu cua tung nguoi (chon gi o buoc truoc). Chi la goi y - moi buoc kiem lai. */
    static final class NguCanh {
        final List<Integer> ds = new ArrayList<Integer>();
        int khoa, muc, so, ma, chiSo, loai;
        String ban;
    }

    static final Map<Integer, NguCanh> NGU_CANH = new ConcurrentHashMap<Integer, NguCanh>();
    static final Map<Integer, Long> LAN_CUOI = new ConcurrentHashMap<Integer, Long>();

    static NguCanh nc(final Char p) {
        return NGU_CANH.computeIfAbsent(p.charDBID, (k) -> new NguCanh());
    }

    static void menu(final Char p, final int idMenu, final List<String> muc) {
        if (muc.isEmpty()) {
            muc.add("(trống)");
        }
        p.sendMessage(MessageCreator.createMsgMenu(muc.toArray(new String[0]), HeSinhThaiGame.NPC_ID, idMenu));
    }

    static void bao(final Char p, final String s) {
        HeSinhThaiGame.bao(p, s);
    }

    /**
     * 0.9.6.1: ket qua cau / dao / che tao. Client moi: the nhanh (dong 1 = duoc gi, dong 2 = the luc), gap Pokemon la
     * MOT thong bao rieng (huy hieu Pokemon). Client cu: mot hop thoai nhu truoc (giu chu "Câu được" o dau - client
     * giu thong bao cau ca den luc keo cau).
     */
    static void baoKetQua(final Char p, final boolean hiem, final String ketQua, final String phu, final int dex,
            final String tenGap) {
        if (!com.htbb.thongbao.ThongBao.coToast(p)) {
            bao(p, com.htbb.thongbao.ThongBao.motCau(ketQua
                    + (tenGap == null ? "" : ". " + com.htbb.thongbao.ThongBao.gap(tenGap, hiem)), phu));
            return;
        }
        com.htbb.thongbao.ThongBao.gui(p, hiem ? com.htbb.thongbao.ThongBao.HIEM : com.htbb.thongbao.ThongBao.KET_QUA,
                ketQua, phu, dex > 0 ? com.htbb.thongbao.ThongBao.HINH_POKEMON : com.htbb.thongbao.ThongBao.HINH_BIEU_TUONG,
                dex > 0 ? dex : com.htbb.thongbao.ThongBao.BT_NGUYEN_LIEU);
        if (tenGap != null) {
            com.htbb.thongbao.ThongBao.gui(p, hiem ? com.htbb.thongbao.ThongBao.HIEM : com.htbb.thongbao.ThongBao.TIN,
                    com.htbb.thongbao.ThongBao.gap(tenGap, hiem), com.htbb.thongbao.ThongBao.GAP_PHU,
                    com.htbb.thongbao.ThongBao.HINH_POKEMON, dex);
        }
    }

    /** Chan bam lien tuc (macro chat) - 1,5 giay giua hai hanh dong ton the luc. */
    static boolean quaNhanh(final Char p) {
        final long now = com.htbb.pokemon.DongHo.bay();
        final Long cu = LAN_CUOI.get(p.charDBID);
        if (cu != null && now - cu < 1500L) {
            return true;
        }
        LAN_CUOI.put(p.charDBID, now);
        return false;
    }

    static HoSoSinhThai hoSo(final Char p) {
        final HoSoSinhThai h = HeSinhThaiGame.hoSo(p);
        if (h == null) {
            bao(p, "Hệ sinh thái tạm thời không đọc được, thử lại sau.");
        }
        return h;
    }

    // ================================================================== menu goc

    /** Vi tri muc "Hệ sinh thái" trong menu NPC -90: sau Xem doi thuong, Pokédex, So tay (neu co). */
    public static int viTri() {
        return 2 + (LauDai.bat() ? 1 : 0);
    }

    public static void moMenu(final Char p) {
        if (!HeSinhThai.bat() || p == null) {
            return;
        }
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final int tt = HeSinhThaiGame.thoiTietMap(p.mapID);
        menu(p, M_GOC, mucGoc(SinhThaiTheGioi.tenThoiTiet(tt), SinhThaiTheGioi.laDem(HeSinhThaiGame.gio()), h.trung.size(), h.theLuc));
    }

    /**
     * 0.9.6.1: thu tu muc menu goc = thu tu khoa o day; nhan (mucGoc) va xu ly (goc) cung doc mang nay nen chi so khong
     * the lech nhau. "Câu cá tại đây" DA BO khoi menu: cau ca chi bat dau tu nut ngu canh canh ho (lenh chat "cauca" ->
     * {@link #cauCa}). Client cu (0.9.6) gui chi so theo DANH SACH MAY CHU VUA GUI nen van dung muc; client cu chi con
     * hoat canh quang can ngan khi bam muc thu 2 (nay la Khảo cổ) - khong doi ket qua.
     */
    static final String G_THOI_TIET = "thoiTiet", G_KHAO_CO = "khaoCo", G_CHE_TAC = "cheTac", G_TUI = "tui",
            G_NHA_TRE = "nhaTre", G_CHO = "cho", G_VUON = "vuon", G_SUU_TAM = "suuTam", G_GIAI_TRI = "giaiTri",
            G_KY_NANG = "kyNang";
    public static final String[] GOC = {G_THOI_TIET, G_KHAO_CO, G_CHE_TAC, G_TUI, G_NHA_TRE, G_CHO, G_VUON, G_SUU_TAM, G_GIAI_TRI,
        G_KY_NANG};

    /** Nhan menu goc (cung thu tu {@link #GOC}). */
    public static List<String> mucGoc(final String thoiTiet, final boolean dem, final int soTrung, final int theLuc) {
        final List<String> m = new ArrayList<String>();
        for (final String k : GOC) {
            m.add(nhanGoc(k, thoiTiet, dem, soTrung, theLuc));
        }
        return m;
    }

    static String nhanGoc(final String k, final String thoiTiet, final boolean dem, final int soTrung, final int theLuc) {
        switch (k) {
            case G_THOI_TIET:
                return "Thời tiết: " + thoiTiet + (dem ? " (đêm)" : " (ngày)");
            case G_KHAO_CO:
                return "Khảo cổ: đào tại đây";
            case G_CHE_TAC:
                return "Nấu ăn, chế thuốc, chế tác";
            case G_TUI:
                return "Túi sinh thái";
            case G_NHA_TRE:
                return "Nhà trẻ và trứng (" + soTrung + ")";
            case G_CHO:
                return "Chợ sinh thái";
            case G_VUON:
                return "Vườn nhà và bạn bè";
            case G_SUU_TAM:
                return "Sưu tầm và nghiên cứu";
            case G_GIAI_TRI:
                return "Giải trí: đố vui, đua, giải đấu";
            case G_KY_NANG:
                return "Kỹ năng và thể lực (" + theLuc + ")";
            default:
                return k;
        }
    }

    /** Khoa cua muc op trong menu goc; null = ngoai danh sach. */
    public static String khoaGoc(final int op) {
        return op >= 0 && op < GOC.length ? GOC[op] : null;
    }

    /** MapDoMenu: idNpc == HeSinhThaiGame.NPC_ID. */
    public static void doMenu(final Char p, final int idMenu, final int op) {
        if (!HeSinhThai.bat() || p == null || op < 0) {
            return;
        }
        try {
            switch (idMenu) {
                case M_GOC:
                    goc(p, op);
                    break;
                case M_CONG_THUC:
                    lamCongThuc(p, op);
                    break;
                case M_TUI:
                    chonTui(p, op);
                    break;
                case M_TUI_HD:
                    hanhDongTui(p, op);
                    break;
                case M_TUI_BAN_NPC:
                    banNpc(p, op);
                    break;
                case M_TUI_GIA:
                    chonGiaRao(p, op);
                    break;
                case M_TUI_SO:
                    rao(p, op);
                    break;
                case M_NHA_TRE:
                    nhaTre(p, op);
                    break;
                case M_NHA_TRE_GUI:
                    guiPet(p, op);
                    break;
                case M_TRUNG:
                    chonTrung(p, op);
                    break;
                case M_TRUNG_HD:
                    hanhDongTrung(p, op);
                    break;
                case M_TRUNG_GIA:
                    raoTrung(p, op);
                    break;
                case M_CHO:
                    cho(p, op);
                    break;
                case M_CHO_LOAI:
                    chonTin(p, op);
                    break;
                case M_CHO_MUA:
                    if (op == 0) {
                        mua(p);
                    } else {
                        moCho(p);
                    }
                    break;
                case M_CHO_CUA_TOI:
                    huyTin(p, op);
                    break;
                case M_DON:
                    chonDon(p, op);
                    break;
                case M_DON_GIAO:
                    giaoDon(p, op);
                    break;
                case M_DON_TAO_MA:
                    taoDonMa(p, op);
                    break;
                case M_DON_TAO_SO:
                    taoDonSo(p, op);
                    break;
                case M_DON_TAO_GIA:
                    taoDonGia(p, op);
                    break;
                case M_DON_CUA_TOI:
                    huyDon(p, op);
                    break;
                case M_VUON:
                    vuon(p, op);
                    break;
                case M_VUON_DAT:
                    datTrangTri(p, op);
                    break;
                case M_VUON_CAT:
                    catTrangTri(p, op);
                    break;
                case M_VUON_BAN:
                    tuoiBan(p, op);
                    break;
                case M_SUU_TAM:
                    suuTam(p, op);
                    break;
                case M_VUNG:
                    nhanVung(p, op);
                    break;
                case M_GIAI_TRI:
                    giaiTri(p, op);
                    break;
                case M_DANH_HIEU:
                    deoDanhHieu(p, op);
                    break;
                default:
                    break;
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] menu " + idMenu + "/" + op + ": " + e);
        }
    }

    static void goc(final Char p, final int op) {
        final String k = khoaGoc(op);
        if (k == null) {
            return;
        }
        switch (k) {
            case G_THOI_TIET:
                xemTheGioi(p);
                break;
            case G_KHAO_CO:
                dao(p);
                break;
            case G_CHE_TAC:
                moCongThuc(p);
                break;
            case G_TUI:
                moTui(p);
                break;
            case G_NHA_TRE:
                moNhaTre(p);
                break;
            case G_CHO:
                moCho(p);
                break;
            case G_VUON:
                moVuon(p);
                break;
            case G_SUU_TAM:
                moSuuTam(p);
                break;
            case G_GIAI_TRI:
                moGiaiTri(p);
                break;
            case G_KY_NANG:
                xemKyNang(p);
                break;
            default:
                break;
        }
    }

    // ================================================================== the gioi

    static void xemTheGioi(final Char p) {
        final int gio = HeSinhThaiGame.gio();
        final int v = HeSinhThaiGame.vung(p.mapID);
        final int tt = HeSinhThaiGame.thoiTietVung(v);
        final StringBuilder b = new StringBuilder();
        b.append(SinhThaiTheGioi.tenVung(v)).append(" - ").append(real.Map.getNameMap(p.mapID)).append('\n');
        b.append("Thời tiết: ").append(SinhThaiTheGioi.tenThoiTiet(tt)).append(", ")
                .append(SinhThaiTheGioi.laDem(gio) ? "ban đêm" : "ban ngày").append(", mùa ")
                .append(SinhThaiTheGioi.TEN_MUA[SinhThaiTheGioi.mua(HeSinhThaiGame.thang())]).append('\n');
        b.append(moTaUuThe(tt, gio)).append('\n');
        final int[] bp = HeSinhThaiGame.bungPhat;
        if (bp != null) {
            b.append("Bùng phát: ").append(SuuTamSinhThai.tenLoai(HeSinhThaiGame.dexTemplate(bp[1]))).append(" ở ")
                    .append(real.Map.getNameMap(bp[0])).append('\n');
        } else {
            b.append("Bùng phát mỗi ngày lúc ");
            for (int i = 0; i < HeSinhThai.GIO_BUNG_PHAT.length; i++) {
                b.append(i == 0 ? "" : ", ").append(HeSinhThai.GIO_BUNG_PHAT[i]).append("h");
            }
            b.append('\n');
        }
        b.append("Hôm nay:");
        for (final int vv : new int[]{SinhThaiTheGioi.V_DONG_CO, SinhThaiTheGioi.V_RUNG, SinhThaiTheGioi.V_HO,
            SinhThaiTheGioi.V_BIEN, SinhThaiTheGioi.V_NUI, SinhThaiTheGioi.V_TUYET, SinhThaiTheGioi.V_SA_MAC}) {
            b.append("\n- ").append(SinhThaiTheGioi.tenVung(vv)).append(": ")
                    .append(SinhThaiTheGioi.tenThoiTiet(HeSinhThaiGame.thoiTietVung(vv)));
        }
        bao(p, b.toString());
    }

    static String moTaUuThe(final int tt, final int gio) {
        final StringBuilder b = new StringBuilder();
        final List<String> he = new ArrayList<String>();
        for (int i = 0; i < HePokemon.SO_HE; i++) {
            if (SinhThaiTheGioi.heHopThoiTiet(i, tt)) {
                he.add(HePokemon.ten(i));
            }
        }
        if (!he.isEmpty()) {
            b.append("Pokémon hệ ").append(String.join("/", he)).append(" xuất hiện nhiều hơn");
        }
        for (int i = 0; i < HePokemon.SO_HE; i++) {
            final int pct = SinhThaiTheGioi.satThuongPhanTram(i, tt);
            if (pct != 100) {
                b.append(b.length() == 0 ? "" : ", ").append("chiêu ").append(HePokemon.ten(i)).append(pct > 100 ? " +" : " ")
                        .append(pct - 100).append('%');
            }
        }
        if (SinhThaiTheGioi.laDem(gio)) {
            b.append(b.length() == 0 ? "" : ", ").append("đêm: hệ Ma/Bóng tối/Độc hoạt động mạnh");
        }
        return b.length() == 0 ? "Không có ưu thế đặc biệt." : b.toString();
    }

    static void xemKyNang(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final StringBuilder b = new StringBuilder("Kỹ năng sống\n");
        for (int k = 0; k < KyNangSong.SO_KY_NANG; k++) {
            final int c = h.capKyNang(k);
            final long du = h.expKyNang[k] - KyNangSong.tongExpDenCap(c);
            b.append("- ").append(KyNangSong.TEN[k]).append(": cấp ").append(c);
            if (c < HeSinhThai.CAP_KY_NANG_TOI_DA) {
                b.append(" (").append(du).append('/').append(KyNangSong.expLenCap(c)).append(')');
            }
            b.append('\n');
        }
        b.append("Thể lực sinh hoạt: ").append(h.theLuc).append(" (+").append(HeSinhThai.THE_LUC_NGAY)
                .append("/ngày, tích tối đa ").append(HeSinhThai.THE_LUC_TOI_DA).append(")\n");
        b.append("Câu cá ").append(HeSinhThai.TL_CAU_CA).append(", đào ").append(HeSinhThai.TL_DAO).append(", nấu/chế ")
                .append(HeSinhThai.TL_NAU).append(" thể lực\n");
        if (h.buffMoiThom > 0 || h.buffSup > 0) {
            b.append("Hiệu ứng: mồi thơm ").append(h.buffMoiThom).append(" lần, súp berry ").append(h.buffSup).append(" lần\n");
        }
        b.append("Đã bán NPC hôm nay: ").append(h.banNpcHomNay).append('/').append(ChoSinhThai.tranBanNpcNgay(HeSinhThaiGame.cap(p)))
                .append(" xu");
        bao(p, b.toString());
    }

    static void lenCap(final Char p, final HoSoSinhThai h, final int kn, final int cap) {
        if (cap <= 0) {
            return;
        }
        com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, "Kỹ năng " + KyNangSong.TEN[kn] + " lên cấp " + cap + "!",
                null, com.htbb.thongbao.ThongBao.HINH_BIEU_TUONG, com.htbb.thongbao.ThongBao.BT_SINH_THAI);
        if (cap >= 30) {
            final String[] dh = {"Ngư phủ bậc thầy", "Nhà vườn xanh", "Nhà khảo cổ", "Đầu bếp Poké", "Dược sư"};
            if (h.themDanhHieu(dh[kn])) {
                HeSinhThaiGame.baoTheGioi(p.charname + " đạt " + KyNangSong.TEN[kn] + " cấp " + cap + ", nhận danh hiệu " + dh[kn] + "!");
            }
        }
    }

    // ================================================================== cau ca

    /**
     * Lenh chat "cauca" (client cu, toi 0.9.6): tha cau, server TU keo khi ca can (khong co nut keo can) nhung
     * van cho cung thoi gian + them ti le thoat {@link #THOAT_THEM_CLIENT_CU} (bu cho khong phai bam dung luc).
     */
    public static void cauCa(final Char p) {
        thaCau(p, false);
    }

    /** Client cu tu keo: them % ca thoat (nguoi bam tay cung hut khung thinh thoang). */
    static final int THOAT_THEM_CLIENT_CU = 15;

    /**
     * Tha cau. {@code canKeo}: client moi (lenh "cauca2") phai gui "keocan" trong khung sau "Cá cắn câu!";
     * client cu ("cauca") server tu keo. Luat: CHI cau o ho (vung nuoc kin) va dung sat mep nuoc (<= 2 o) -
     * server kiem lai vi tri, khong tin client.
     */
    public static void thaCau(final Char p, final boolean canKeo) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null || quaNhanh(p)) {
            return;
        }
        final long now = DongHo.bay();
        final CauCaPhien.SoLieu sl = CauCaPhien.soLieu();
        if (CauCaPhien.dang(p.charDBID) != null) {
            bao(p, "Đang câu, chờ cá cắn câu.");
            return;
        }
        final long nghi = CauCaPhien.conNghi(p.charDBID, now, sl);
        if (nghi > 0) {
            bao(p, "Chờ " + ((nghi + 999) / 1000) + " giây nữa mới thả câu tiếp.");
            return;
        }
        final HeSinhThaiGame.BanDoPhanTich bd = HeSinhThaiGame.banDo(p);
        // Luat: CHI cau o ho (vung nuoc kin, khong cham mep ban do) - khong cau o bien / song / bo bien.
        int hoId = BanDoSinhThai.hoGan(bd.ho, bd.w, bd.h, p.x / 16, p.y / 16, 2);
        for (final int k : HeSinhThaiGame.MAP_KHONG_DONG) {
            if (k == p.mapID) {
                hoId = -1; // MOBA / nui kho bau: khong cau
            }
        }
        if (hoId < 0) {
            int[] gan = null;
            int d = Integer.MAX_VALUE;
            for (final int[] dc : bd.diemCau) {
                final int dd = Math.abs(dc[1] - p.x / 16) + Math.abs(dc[2] - p.y / 16);
                if (dd < d) {
                    d = dd;
                    gan = dc;
                }
            }
            // Cau dau giu nguyen (luat nguoi dung); phan sau la goi y diem cau tren ban do nay (neu co ho).
            bao(p, "Chỉ có thể câu cá ở hồ." + (gan == null || p.mapID == 40 || p.mapID == 42 ? ""
                    : " Điểm câu gần nhất: (" + gan[1] + "," + gan[2] + ")"));
            return;
        }
        final boolean hoLon = hoId < bd.kichThuocHo.length && bd.kichThuocHo[hoId] >= BanDoSinhThai.HO_LON;
        final long moi = HeSinhThaiGame.phi(p, HeSinhThai.PHI_MOI_CAU_PHAN_NGHIN);
        if (h.theLuc < HeSinhThai.TL_CAU_CA) {
            bao(p, "Hết thể lực sinh hoạt hôm nay. Nghỉ ngơi hoặc ăn Bánh Poké.");
            return;
        }
        // giu cho truoc khi tru (hai lenh song song chi mot lenh qua)
        final CauCaPhien.Phien ph = CauCaPhien.tha(p.charDBID, p.mapID, p.x / 16, p.y / 16, hoId, hoLon, canKeo, now, sl,
                HeSinhThaiGame.R);
        if (ph == null) {
            bao(p, "Đang câu, chờ cá cắn câu.");
            return;
        }
        // moi cau = vat tieu hao (hut xu), mat ca khi khong duoc gi / ca thoat
        if (!HeSinhThaiGame.truXu(p, moi, "moi cau")) {
            CauCaPhien.DANG.remove(p.charDBID, ph);
            bao(p, "Cần " + moi + " xu mua mồi câu.");
            return;
        }
        if (!h.truTheLuc(HeSinhThai.TL_CAU_CA)) {
            CauCaPhien.DANG.remove(p.charDBID, ph);
            HeSinhThaiGame.congXu(p, moi, "hoan moi cau", Char.XU_HOAN);
            return;
        }
        HeSinhThaiGame.danhDau(p);
        real.DiemMocNhiemVu.khiThaCau(p); // huong dan tan thu: buoc tha cau (khong doi ca can)
    }

    /** Lenh chat "keocan" (client moi): keo can. Dung khung -> tra ket qua; som / muon -> ca thoat. */
    public static void keoCan(final Char p) {
        final long now = DongHo.bay();
        final CauCaPhien.Phien ph = CauCaPhien.dang(p.charDBID);
        final int k = CauCaPhien.keo(p.charDBID, now, CauCaPhien.soLieu());
        if (k == CauCaPhien.KHONG_CO || ph == null) {
            return; // khong co phien (da thoat / da xong): im lang
        }
        if (k == CauCaPhien.SOM) {
            bao(p, "Kéo cần quá sớm, cá đã thoát mất…");
            return;
        }
        if (k == CauCaPhien.MUON) {
            bao(p, "Kéo cần quá muộn, cá đã thoát mất…");
            return;
        }
        traCa(p, ph);
    }

    /** Nhip (he-sinh-thai, 200 ms): bao ca can, het khung = ca thoat, roi cho / thoat = huy. */
    static void nhipCauCa() {
        final long now = DongHo.bay();
        final CauCaPhien.SoLieu sl = CauCaPhien.soLieu();
        for (final CauCaPhien.Phien ph : CauCaPhien.denLucCan(now)) {
            final Char p = HeSinhThaiGame.timOnline(ph.charId);
            if (p == null || p.mapID != ph.mapId || Math.abs(p.x / 16 - ph.x) > 2 || Math.abs(p.y / 16 - ph.y) > 2) {
                CauCaPhien.huy(ph.charId, now);
                if (p != null) {
                    bao(p, "Bạn đã rời chỗ câu, cá đã thoát mất…");
                }
                continue;
            }
            if (!ph.canKeo) {
                // client cu: server tu keo
                CauCaPhien.keo(ph.charId, now, sl);
                if (HeSinhThaiGame.R.nextInt(100) < THOAT_THEM_CLIENT_CU) {
                    bao(p, "Cá đã thoát mất…");
                } else {
                    traCa(p, ph);
                }
                continue;
            }
            bao(p, "Cá cắn câu! Kéo cần ngay!");
        }
        for (final CauCaPhien.Phien ph : CauCaPhien.quaKhung(now, sl)) {
            final Char p = HeSinhThaiGame.timOnline(ph.charId);
            if (p != null) {
                bao(p, "Cá đã thoát mất…");
            }
        }
    }

    /** Keo dung luc: co ti le khong duoc gi (tang dan khi cau nhieu trong gio), con lai bang cau ca nhu cu. */
    static void traCa(final Char p, final CauCaPhien.Phien ph) {
        final HoSoSinhThai h = HeSinhThaiGame.hoSo(p);
        if (h == null) {
            return;
        }
        final long now = DongHo.bay();
        final CauCaPhien.SoLieu sl = CauCaPhien.soLieu();
        final int daCau = CauCaPhien.demTrongGio(p.charDBID, now);
        if (CauCaPhien.trong(sl, daCau, HeSinhThaiGame.R)) {
            bao(p, "Không câu được gì. Thể lực còn " + h.theLuc + ".");
            HeSinhThaiGame.danhDau(p);
            return;
        }
        CauCaPhien.ghiCauDuoc(p.charDBID, now);
        final boolean hoLon = ph.hoLon;
        final int v = HeSinhThaiGame.vung(p.mapID);
        // Ho tren map bien / tau = dam pha (nuoc lo): ca nuoc ngot lan nuoc man; con lai: nuoc ngot.
        final int nuoc = v == SinhThaiTheGioi.V_BIEN || v == SinhThaiTheGioi.V_TAU
                ? KyNangSong.NUOC_MAN | KyNangSong.NUOC_NGOT : KyNangSong.NUOC_NGOT;
        final int capKn = h.capKyNang(KyNangSong.CAU_CA);
        final boolean thom;
        synchronized (h) {
            thom = h.buffMoiThom > 0;
            if (thom) {
                h.buffMoiThom--;
            }
        }
        final KyNangSong.KetQua kq = KyNangSong.cauCa(nuoc, capKn, thom, hoLon, HeSinhThaiGame.R);
        final int gia = VatLieuSinhThai.giaPhanNghin(kq.ma);
        final int so = kq.ma == VatLieuSinhThai.RAC ? 1 : kq.soLuong * KyNangSong.soLuongThuong(capKn, HeSinhThaiGame.R);
        final int bac = VatLieuSinhThai.bacTheoCap(HeSinhThaiGame.cap(p));
        h.them(VatLieuSinhThai.khoa(kq.ma, bac), so);
        final int len = h.congExp(KyNangSong.CAU_CA, KyNangSong.expKhiLam(gia));
        // 0.9.6.1 (thong bao): ket qua = the nhanh "Câu được X ×n" + dong "Thể lực còn N"; gap Pokemon = thong bao rieng
        final boolean hiem = gia >= 100;
        final String ketQua = com.htbb.thongbao.ThongBao.duoc("Câu được", VatLieuSinhThai.ten(kq.ma), so, hiem);
        final String theLuc = com.htbb.thongbao.ThongBao.theLuc(h.theLuc);
        if (kq.dex > 0) {
            gapPokedex(p, kq.dex);
            HeSinhThaiGame.nghienCuu(p, h, kq.dex, SuuTamSinhThai.NV_CAU);
            if (gia >= 100) {
                HeSinhThaiGame.chupAnh(p, h, kq.dex, SuuTamSinhThai.A_CA_HIEM, HeSinhThaiGame.thoiTietMap(p.mapID));
            }
            if (gia >= 250) {
                HeSinhThaiGame.baoTheGioi(p.charname + " vừa câu được " + SuuTamSinhThai.tenLoai(kq.dex) + " ở "
                        + real.Map.getNameMap(p.mapID) + "!");
            }
        }
        baoKetQua(p, hiem, ketQua, theLuc, kq.dex, kq.dex > 0 ? SuuTamSinhThai.tenLoai(kq.dex) : null);
        // DIEM MOC DUY NHAT "cau_ca" (NhiemVuEngine: nhiem vu kieu cau_ca + danh hieu cau ca; tham so = dex gap, 0 = khong).
        // Nhanh khac (cau-ca) KHONG them diem moc cau_ca o noi khac - se dem hai lan.
        real.NhiemVuEngine.suKien(p, "cau_ca", String.valueOf(kq.dex), 1);
        lenCap(p, h, KyNangSong.CAU_CA, len);
        HeSinhThaiGame.danhDau(p);
    }

    /** Ghi "da gap" Pokedex cho loai co template quai (qua PokedexPokemon cong khai). */
    static void gapPokedex(final Char p, final int dex) {
        try {
            for (final MonsterTemplate t : real.Map.monsterTemplates.values()) {
                if (t != null && t.dex == dex) {
                    final Monster gia = new Monster((byte) 1);
                    gia.idTemplate = t.id;
                    real.PokedexPokemon.khiDanh(p, gia);
                    return;
                }
            }
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== khao co

    /** Diem da dao: charDBID:map:ngay -> mang da dao. */
    static final Map<String, boolean[]> DA_DAO = new ConcurrentHashMap<String, boolean[]>();
    static final int SO_DIEM_DAO = 6;

    static int[] diemGanNhat(final int[][] diem, final boolean[] da, final int x, final int y) {
        int[] tot = null;
        int d = Integer.MAX_VALUE;
        for (int i = 0; i < diem.length; i++) {
            if (da != null && i < da.length && da[i]) {
                continue;
            }
            final int dd = Math.abs(diem[i][0] - x) + Math.abs(diem[i][1] - y);
            if (dd < d) {
                d = dd;
                tot = diem[i];
            }
        }
        return tot == null ? new int[]{-1, -1} : tot;
    }

    public static void dao(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null || quaNhanh(p)) {
            return;
        }
        final int v = HeSinhThaiGame.vung(p.mapID);
        if (!SinhThaiTheGioi.vungCoDiemDao(v)) {
            bao(p, "Chỉ đào được ở hang động, núi, sa mạc và vùng tuyết.");
            return;
        }
        final HeSinhThaiGame.BanDoPhanTich bd = HeSinhThaiGame.banDo(p);
        final int ngay = HeSinhThaiGame.homNay();
        final int[][] diem = BanDoSinhThai.diemDao(bd.diDuoc, bd.w, bd.h, ngay, p.mapID, SO_DIEM_DAO);
        final String k = p.charDBID + ":" + p.mapID + ":" + ngay;
        final boolean[] da = DA_DAO.computeIfAbsent(k, (x) -> new boolean[SO_DIEM_DAO]);
        final int x = p.x / 16, y = p.y / 16;
        final int i = BanDoSinhThai.diemTaiCho(diem, x, y, 1);
        if (i < 0 || i < da.length && da[i]) {
            final String gy = BanDoSinhThai.goiYDiemGanNhat(diem, da, x, y);
            final int[] gan = diemGanNhat(diem, da, x, y);
            // Client (J2ME/Android) doc dung dang "Điểm đào: (x,y)" de danh dau o - KHONG doi dinh dang.
            bao(p, gy == null ? "Đã đào hết điểm của bản đồ này hôm nay." : "Máy dò rung: điểm đào gần nhất " + gy + ". Điểm đào: ("
                    + gan[0] + "," + gan[1] + ")");
            return;
        }
        if (h.theLuc < HeSinhThai.TL_DAO) {
            bao(p, "Hết thể lực sinh hoạt hôm nay.");
            return;
        }
        final long xeng = HeSinhThaiGame.phi(p, HeSinhThai.PHI_XENG_PHAN_NGHIN);
        if (!HeSinhThaiGame.truXu(p, xeng, "xeng")) {
            bao(p, "Cần " + xeng + " xu thuê xẻng.");
            return;
        }
        if (!h.truTheLuc(HeSinhThai.TL_DAO)) {
            HeSinhThaiGame.congXu(p, xeng, "hoan xeng", Char.XU_HOAN);
            return;
        }
        synchronized (da) {
            if (da[i]) {
                HeSinhThaiGame.congXu(p, xeng, "hoan xeng", Char.XU_HOAN);
                h.congTheLuc(HeSinhThai.TL_DAO);
                return;
            }
            da[i] = true;
        }
        final int capKn = h.capKyNang(KyNangSong.KHAO_CO);
        final boolean mayMan;
        synchronized (h) {
            mayMan = h.buffMoiThom > 0;
            if (mayMan) {
                h.buffMoiThom--;
            }
        }
        final KyNangSong.KetQua kq = KyNangSong.dao(capKn, mayMan, HeSinhThaiGame.R);
        final int bac = VatLieuSinhThai.bacTheoCap(HeSinhThaiGame.cap(p));
        h.them(VatLieuSinhThai.khoa(kq.ma, bac), kq.soLuong);
        final int len = h.congExp(KyNangSong.KHAO_CO, KyNangSong.expKhiLam(VatLieuSinhThai.giaPhanNghin(kq.ma)));
        if (VatLieuSinhThai.loai(kq.ma) == VatLieuSinhThai.L_HOA_THACH) {
            HeSinhThaiGame.chupAnh(p, h, 0, SuuTamSinhThai.A_HOA_THACH, HeSinhThaiGame.thoiTietMap(p.mapID));
            HeSinhThaiGame.baoTheGioi(p.charname + " vừa đào được " + VatLieuSinhThai.ten(kq.ma) + "!");
        }
        baoKetQua(p, VatLieuSinhThai.giaPhanNghin(kq.ma) >= 100,
                com.htbb.thongbao.ThongBao.duoc("Đào được", VatLieuSinhThai.ten(kq.ma), kq.soLuong,
                        VatLieuSinhThai.giaPhanNghin(kq.ma) >= 100), com.htbb.thongbao.ThongBao.theLuc(h.theLuc), 0, null);
        real.DiemMocNhiemVu.khiDao(p, kq.ma); // diem moc dao_dat
        lenCap(p, h, KyNangSong.KHAO_CO, len);
        HeSinhThaiGame.danhDau(p);
    }

    // ================================================================== cong thuc

    static void moCongThuc(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final NguCanh c = nc(p);
        c.ds.clear();
        final List<String> m = new ArrayList<String>();
        for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
            c.ds.add(ct.id);
            final boolean du = duNguyenLieu(h, ct);
            final boolean duCap = h.capKyNang(ct.kyNang) >= ct.capToiThieu;
            m.add(ct.ten + " (" + KyNangSong.TEN[ct.kyNang] + " " + ct.capToiThieu + ")" + (duCap ? (du ? " - làm được" : "") : " - chưa đủ cấp"));
        }
        menu(p, M_CONG_THUC, m);
    }

    static boolean duNguyenLieu(final HoSoSinhThai h, final KyNangSong.CongThuc ct) {
        for (final int[] nl : ct.nguyenLieu) {
            if (h.tongMa(nl[0]) < nl[1]) {
                return false;
            }
        }
        return true;
    }

    static String moTaNguyenLieu(final KyNangSong.CongThuc ct) {
        final StringBuilder b = new StringBuilder();
        for (final int[] nl : ct.nguyenLieu) {
            b.append(b.length() == 0 ? "" : ", ").append(nl[1]).append(" ").append(VatLieuSinhThai.ten(nl[0]));
        }
        return b.toString();
    }

    static void lamCongThuc(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        final KyNangSong.CongThuc ct = KyNangSong.CONG_THUC[c.ds.get(op)];
        final HoSoSinhThai h = hoSo(p);
        if (h == null || quaNhanh(p)) {
            return;
        }
        if (h.capKyNang(ct.kyNang) < ct.capToiThieu) {
            bao(p, "Cần " + KyNangSong.TEN[ct.kyNang] + " cấp " + ct.capToiThieu + ".");
            return;
        }
        if (ct.kyNang == KyNangSong.CHE_THUOC && h.meThuocHomNay >= KyNangSong.ME_THUOC_TOI_DA_NGAY) {
            bao(p, "Hôm nay đã chế đủ " + KyNangSong.ME_THUOC_TOI_DA_NGAY + " mẻ thuốc.");
            return;
        }
        if (ct.ra == 0 && p.isFullInventory()) {
            bao(p, "Hành trang đã đầy.");
            return;
        }
        if (h.theLuc < HeSinhThai.TL_NAU) {
            bao(p, "Hết thể lực sinh hoạt hôm nay.");
            return;
        }
        final long bep = HeSinhThaiGame.phi(p, HeSinhThai.PHI_BEP_PHAN_NGHIN);
        final int[] bacs = new int[ct.nguyenLieu.length];
        synchronized (h) {
            if (!duNguyenLieu(h, ct)) {
                bao(p, "Thiếu nguyên liệu: cần " + moTaNguyenLieu(ct) + ".");
                return;
            }
            if (!HeSinhThaiGame.truXu(p, bep, "bep " + ct.id)) {
                bao(p, "Cần " + bep + " xu nhiên liệu.");
                return;
            }
            h.truTheLuc(HeSinhThai.TL_NAU);
            for (int i = 0; i < ct.nguyenLieu.length; i++) {
                bacs[i] = h.layTheoMa(ct.nguyenLieu[i][0], ct.nguyenLieu[i][1]);
            }
            if (ct.kyNang == KyNangSong.CHE_THUOC) {
                h.meThuocHomNay++;
            }
        }
        final String ten;
        String chatLuong = "";
        if (ct.ra > 0) {
            final int bac = KyNangSong.bacThanhPham(bacs);
            h.them(VatLieuSinhThai.khoa(ct.ra, bac), ct.soRa);
            ten = com.htbb.thongbao.ThongBao.soLuong(VatLieuSinhThai.ten(ct.ra), ct.soRa);
            chatLuong = "Chất lượng " + bac + "/5 · ";
        } else {
            HeSinhThaiGame.phatThuong(p, ct.thuong, "cong thuc " + ct.id);
            ten = ct.ten;
            if (ct.id >= 16 && ct.id <= 20) {
                HeSinhThaiGame.baoTheGioi(p.charname + " đã phục hồi hoá thạch thành Pokémon cổ đại!");
            }
        }
        final int gia = ct.ra > 0 ? VatLieuSinhThai.giaPhanNghin(ct.ra) * ct.soRa : 300;
        final int len = h.congExp(ct.kyNang, KyNangSong.expKhiLam(gia));
        baoKetQua(p, false, (ct.ra > 0 ? "Chế xong " : "Hoàn thành: ") + ten, chatLuong + com.htbb.thongbao.ThongBao.theLuc(h.theLuc), 0, null);
        real.DiemMocNhiemVu.khiNau(p, ct.id, ct.kyNang == KyNangSong.NAU_AN); // diem moc nau_an
        lenCap(p, h, ct.kyNang, len);
        HeSinhThaiGame.danhDau(p);
    }

    // ================================================================== tui

    static void moTui(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final NguCanh c = nc(p);
        c.ds.clear();
        final List<String> m = new ArrayList<String>();
        synchronized (h) {
            for (final Map.Entry<Integer, Integer> e : h.tui.entrySet()) {
                if (m.size() >= 40) {
                    break;
                }
                c.ds.add(e.getKey());
                m.add(VatLieuSinhThai.tenKhoa(e.getKey()) + " x" + e.getValue());
            }
        }
        if (m.isEmpty()) {
            bao(p, "Túi sinh thái trống. Câu cá, đào, trồng berry để có nguyên liệu.");
            return;
        }
        menu(p, M_TUI, m);
    }

    static void chonTui(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        c.khoa = c.ds.get(op);
        final int ma = VatLieuSinhThai.maCuaKhoa(c.khoa);
        final int bac = VatLieuSinhThai.bacCuaKhoa(c.khoa);
        final int loai = VatLieuSinhThai.loai(ma);
        final List<String> m = new ArrayList<String>();
        c.ds.clear();
        if (loai == VatLieuSinhThai.L_MON_AN || loai == VatLieuSinhThai.L_THUOC) {
            m.add("Dùng");
            c.ds.add(0);
        }
        if (VatLieuSinhThai.giaNpc(ma, bac) > 0) {
            m.add("Bán cho NPC (" + VatLieuSinhThai.giaNpc(ma, bac) + " xu/cái)");
            c.ds.add(1);
        }
        m.add("Rao bán trên chợ");
        c.ds.add(2);
        if (loai == VatLieuSinhThai.L_TRANG_TRI) {
            m.add("Đặt vào vườn");
            c.ds.add(3);
        }
        m.add("Bỏ 1 cái");
        c.ds.add(4);
        menu(p, M_TUI_HD, m);
    }

    static void hanhDongTui(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        final int hd = c.ds.get(op);
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final int ma = VatLieuSinhThai.maCuaKhoa(c.khoa);
        final int bac = VatLieuSinhThai.bacCuaKhoa(c.khoa);
        switch (hd) {
            case 0:
                dung(p, h, c.khoa);
                break;
            case 1: {
                final List<String> m = new ArrayList<String>();
                c.ds.clear();
                for (final int so : ChoSinhThai.MUC_SO_LUONG) {
                    m.add("Bán " + so + " (" + VatLieuSinhThai.giaNpc(ma, bac) * so + " xu)");
                    c.ds.add(so);
                }
                menu(p, M_TUI_BAN_NPC, m);
                break;
            }
            case 2: {
                final List<String> m = new ArrayList<String>();
                for (int i = 0; i < ChoSinhThai.MUC_GIA_PHAN_TRAM.length; i++) {
                    m.add("Giá " + ChoSinhThai.giaTheoMuc(ma, bac, i) + " xu/cái (x" + ChoSinhThai.MUC_GIA_PHAN_TRAM[i] / 100.0 + ")");
                }
                menu(p, M_TUI_GIA, m);
                break;
            }
            case 3:
                if (h.bot(c.khoa, 1)) {
                    synchronized (h) {
                        h.trangTri.put(ma, h.trangTri.getOrDefault(ma, 0) + 1);
                    }
                    bao(p, "Đã đặt " + VatLieuSinhThai.ten(ma) + " vào vườn. Điểm trang trí: " + diemTrangTri(h));
                    HeSinhThaiGame.danhDau(p);
                }
                break;
            case 4:
                if (h.bot(c.khoa, 1)) {
                    bao(p, "Đã bỏ 1 " + VatLieuSinhThai.tenKhoa(c.khoa) + ".");
                    HeSinhThaiGame.danhDau(p);
                }
                break;
            default:
                break;
        }
    }

    static void dung(final Char p, final HoSoSinhThai h, final int khoa) {
        final int ma = VatLieuSinhThai.maCuaKhoa(khoa);
        final String thuoc = KyNangSong.thuongDungThuoc(ma);
        if (thuoc != null) {
            if (h.bot(khoa, 1)) {
                HeSinhThaiGame.phatThuong(p, thuoc, "dung thuoc " + ma);
                bao(p, "Đã mở " + VatLieuSinhThai.ten(ma) + ".");
                HeSinhThaiGame.danhDau(p);
            } else {
                bao(p, "Không còn " + VatLieuSinhThai.tenKhoa(khoa) + " trong túi sinh thái."); // truoc: im lang
            }
            return;
        }
        if ((ma == VatLieuSinhThai.BANH_POKE || ma == VatLieuSinhThai.CA_RI) && h.banhPokeHomNay >= 2) {
            bao(p, "Hôm nay đã ăn đủ 2 lần.");
            return;
        }
        if (!h.bot(khoa, 1)) {
            bao(p, "Không còn " + VatLieuSinhThai.tenKhoa(khoa) + " trong túi sinh thái."); // truoc: im lang
            return;
        }
        synchronized (h) {
            switch (ma) {
                case VatLieuSinhThai.BANH_POKE:
                    h.banhPokeHomNay++;
                    h.congTheLuc(30);
                    break;
                case VatLieuSinhThai.CA_RI:
                    h.banhPokeHomNay++;
                    h.congTheLuc(20);
                    h.buffMoiThom += 10;
                    break;
                case VatLieuSinhThai.SUP_BERRY:
                    h.buffSup += 20;
                    break;
                case VatLieuSinhThai.MOI_THOM:
                    h.buffMoiThom += 10;
                    break;
                default:
                    break;
            }
        }
        bao(p, "Đã dùng " + VatLieuSinhThai.ten(ma) + ". Thể lực " + h.theLuc + ", mồi thơm " + h.buffMoiThom + ", súp " + h.buffSup + ".");
        HeSinhThaiGame.danhDau(p);
    }

    static void banNpc(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size() || quaNhanh(p)) {
            return;
        }
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final int ma = VatLieuSinhThai.maCuaKhoa(c.khoa);
        final int bac = VatLieuSinhThai.bacCuaKhoa(c.khoa);
        final long[] kq;
        synchronized (h) {
            final int muon = Math.min(c.ds.get(op), h.so(c.khoa));
            kq = ChoSinhThai.npcMua(ma, bac, muon, h.banNpcHomNay, HeSinhThaiGame.cap(p));
            if (kq[0] <= 0 || !h.bot(c.khoa, (int) kq[0])) {
                bao(p, muon <= 0 ? "Không còn vật phẩm này." : "Hôm nay NPC đã mua đủ (" + ChoSinhThai.tranBanNpcNgay(HeSinhThaiGame.cap(p))
                        + " xu). Hãy rao bán cho người chơi khác.");
                return;
            }
            h.banNpcHomNay += kq[1];
        }
        HeSinhThaiGame.congXu(p, kq[1], "ban npc " + ma);
        real.DiemMocNhiemVu.khiBan(p); // diem moc cho_ban
        bao(p, "Đã bán " + kq[0] + " " + VatLieuSinhThai.tenKhoa(c.khoa) + " được " + kq[1] + " xu.");
        HeSinhThaiGame.danhDau(p);
    }

    static void chonGiaRao(final Char p, final int op) {
        final NguCanh c = nc(p);
        c.muc = op;
        final List<String> m = new ArrayList<String>();
        c.ds.clear();
        for (final int so : ChoSinhThai.MUC_SO_LUONG) {
            m.add("Rao " + so + " cái");
            c.ds.add(so);
        }
        menu(p, M_TUI_SO, m);
    }

    static void rao(final Char p, final int op) {
        if (ThanhVien.chanVaBao(p, "cho_sinh_thai")) { // thanh vien: cho sinh thai
            return;
        }
        if (op >= 0 && ChongClone.chanMotBen(p, ChongClone.KIEU_CHO_SINH_THAI)) { // chong clone: cap / tuoi
            return;
        }
        final NguCanh c = nc(p);
        if (op >= c.ds.size() || quaNhanh(p)) {
            return;
        }
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final int so = c.ds.get(op);
        final int ma = VatLieuSinhThai.maCuaKhoa(c.khoa);
        final int bac = VatLieuSinhThai.bacCuaKhoa(c.khoa);
        if (!VatLieuSinhThai.coMa(ma) || bac < 1 || ma == VatLieuSinhThai.TRUNG) {
            return; // chua chon vat lieu trong tui (menu cu)
        }
        final long gia = ChoSinhThai.giaTheoMuc(ma, bac, c.muc);
        if (DuLieuHeSinhThai.dsTinBan(p.charDBID, -1, HeSinhThai.TOI_DA_TIN_BAN + 1).size() >= HeSinhThai.TOI_DA_TIN_BAN) {
            bao(p, "Bạn đã có " + HeSinhThai.TOI_DA_TIN_BAN + " tin bán đang mở.");
            return;
        }
        final long phi = ChoSinhThai.phiDang(gia * so);
        if (h.so(c.khoa) < so) {
            bao(p, "Không đủ " + so + " " + VatLieuSinhThai.tenKhoa(c.khoa) + ".");
            return;
        }
        if (!HeSinhThaiGame.truXu(p, phi, "phi dang")) {
            bao(p, "Cần " + phi + " xu phí đăng.");
            return;
        }
        if (!h.bot(c.khoa, so)) {
            HeSinhThaiGame.congXu(p, phi, "hoan phi dang", Char.XU_HOAN);
            return;
        }
        final int id = DuLieuHeSinhThai.themTinBan(p.charDBID, p.charname, c.khoa, so, gia, "",
                com.htbb.pokemon.DongHo.bay() + HeSinhThai.HAN_CHO_NGAY * 86400000L);
        if (id < 0) {
            h.them(c.khoa, so);
            HeSinhThaiGame.congXu(p, phi, "hoan phi dang", Char.XU_HOAN);
            bao(p, "Chợ tạm thời lỗi, thử lại sau.");
            return;
        }
        bao(p, "Đã rao " + so + " " + VatLieuSinhThai.tenKhoa(c.khoa) + " giá " + gia + " xu/cái (phí đăng " + phi
                + " xu, thuế " + HeSinhThai.THUE_CHO_PHAN_TRAM + "% khi bán). Hết hạn sau " + HeSinhThai.HAN_CHO_NGAY + " ngày.");
        HeSinhThaiGame.danhDau(p);
    }

    // ================================================================== nha tre & trung

    static void moNhaTre(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final long phi = HeSinhThaiGame.phi(p, HeSinhThai.PHI_NHA_TRE_NGAY_PHAN_NGHIN);
        final List<String> m = new ArrayList<String>();
        m.add("Đang gửi: " + tenGui(h.nhaTre1) + " + " + tenGui(h.nhaTre2) + " (" + h.buocNhaTre + "/" + HeSinhThai.BUOC_DE_TRUNG
                + " bước, " + h.deHomNay + "/" + HeSinhThai.TRUNG_TOI_DA_NGAY + " trứng hôm nay)");
        m.add("Gửi pet đang sở hữu");
        m.add("Đón pet về (huỷ gửi)");
        m.add(h.ngayPhiNhaTre == h.ngay ? "Đã trả phí hôm nay" : "Trả phí hôm nay (" + phi + " xu)");
        m.add("Xem trứng (" + h.trung.size() + "/" + HeSinhThai.TRUNG_TOI_DA_TUI + ")");
        menu(p, M_NHA_TRE, m);
    }

    static String tenGui(final int dex) {
        return dex > 0 ? SuuTamSinhThai.tenLoai(dex) : "(trống)";
    }

    /** Dex cac pet nguoi choi dang so huu (pet di theo + item pet trong hanh trang) co the ra trung. */
    static List<Integer> petSoHuu(final Char p) {
        final List<Integer> r = new ArrayList<Integer>();
        try {
            if (p.pet != null) {
                synchronized (p.pet) {
                    for (final data.Pet pet : p.pet) {
                        if (pet != null && pet.itemPet != null && pet.itemPet.getTemplate() != null) {
                            themPet(r, PetPokemon.dex(pet.itemPet.getTemplate().id));
                        }
                    }
                }
            }
            if (p.iItems != null) {
                synchronized (p.iItems) {
                    for (final Item it : p.iItems) {
                        if (it != null && it.getTemplate() != null) {
                            themPet(r, PetPokemon.dex(it.getTemplate().id));
                        }
                    }
                }
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] pet so huu: " + e);
        }
        return r;
    }

    static void themPet(final List<Integer> r, final int dex) {
        if (dex > 0 && NhaTre.chiSo(NhaTre.loaiGoc(dex)) >= 0 && !r.contains(dex)) {
            r.add(dex);
        }
    }

    static void nhaTre(final Char p, final int op) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        switch (op) {
            case 1: {
                final List<Integer> ds = petSoHuu(p);
                if (ds.isEmpty()) {
                    bao(p, "Bạn chưa có pet nào nhân giống được (Hoothoot, Dratini, Cleffa, Zubat, Rufflet, Ponyta, Pachirisu, "
                            + "Delibird, Litleo, Growlithe, Deerling, Bidoof và dạng tiến hoá).");
                    return;
                }
                final NguCanh c = nc(p);
                c.ds.clear();
                c.ds.addAll(ds);
                final List<String> m = new ArrayList<String>();
                for (final int d : ds) {
                    m.add(SuuTamSinhThai.tenLoai(d) + ": nở ra " + SuuTamSinhThai.tenLoai(NhaTre.loaiGoc(d)));
                }
                menu(p, M_NHA_TRE_GUI, m);
                break;
            }
            case 2:
                synchronized (h) {
                    h.nhaTre1 = 0;
                    h.nhaTre2 = 0;
                    h.buocNhaTre = 0;
                }
                bao(p, "Đã đón pet về. Nhà trẻ trống.");
                HeSinhThaiGame.danhDau(p);
                break;
            case 3: {
                if (h.nhaTre1 <= 0 && h.nhaTre2 <= 0) {
                    bao(p, "Chưa gửi pet nào.");
                    return;
                }
                if (h.ngayPhiNhaTre == h.ngay) {
                    bao(p, "Hôm nay đã trả phí.");
                    return;
                }
                final List<Integer> soHuu = petSoHuu(p);
                if (h.nhaTre1 > 0 && !soHuu.contains(h.nhaTre1) || h.nhaTre2 > 0 && !soHuu.contains(h.nhaTre2)) {
                    bao(p, "Bạn không còn sở hữu pet đang gửi. Hãy đón về và gửi lại.");
                    return;
                }
                final long phi = HeSinhThaiGame.phi(p, HeSinhThai.PHI_NHA_TRE_NGAY_PHAN_NGHIN);
                if (!HeSinhThaiGame.truXu(p, phi, "nha tre")) {
                    bao(p, "Cần " + phi + " xu.");
                    return;
                }
                h.ngayPhiNhaTre = h.ngay;
                bao(p, "Đã trả phí nhà trẻ hôm nay. Mỗi Pokémon bạn hạ được tính 1 bước, đủ " + HeSinhThai.BUOC_DE_TRUNG + " bước sẽ có 1 trứng.");
                HeSinhThaiGame.danhDau(p);
                break;
            }
            case 4:
                moTrung(p);
                break;
            default:
                moNhaTre(p);
                break;
        }
    }

    static void guiPet(final Char p, final int op) {
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || op >= c.ds.size()) {
            return;
        }
        final int dex = c.ds.get(op);
        if (!petSoHuu(p).contains(dex)) {
            bao(p, "Bạn không còn sở hữu pet này.");
            return;
        }
        synchronized (h) {
            if (h.nhaTre1 <= 0) {
                h.nhaTre1 = dex;
            } else if (h.nhaTre2 <= 0 && h.nhaTre1 != dex) {
                h.nhaTre2 = dex;
            } else {
                h.nhaTre2 = dex;
            }
        }
        bao(p, "Đã gửi " + SuuTamSinhThai.tenLoai(dex) + " vào nhà trẻ. Nhớ trả phí mỗi ngày.");
        HeSinhThaiGame.danhDau(p);
        moNhaTre(p);
    }

    static void moTrung(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final List<String> m = new ArrayList<String>();
        synchronized (h) {
            for (final NhaTre.Trung t : h.trung) {
                m.add(t.moTa());
            }
        }
        if (m.isEmpty()) {
            bao(p, "Chưa có trứng. Gửi pet vào nhà trẻ hoặc mua trứng ở Chợ sinh thái.");
            return;
        }
        menu(p, M_TRUNG, m);
    }

    static void chonTrung(final Char p, final int op) {
        final NguCanh c = nc(p);
        c.chiSo = op;
        menu(p, M_TRUNG_HD, new ArrayList<String>(java.util.Arrays.asList("Cho trứng nở", "Rao bán trên chợ", "Bỏ trứng")));
    }

    static void hanhDongTrung(final Char p, final int op) {
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final NhaTre.Trung t;
        synchronized (h) {
            if (c.chiSo < 0 || c.chiSo >= h.trung.size()) {
                return;
            }
            t = h.trung.get(c.chiSo);
        }
        if (op == 0) {
            if (!t.daNo()) {
                bao(p, "Trứng cần thêm " + (NhaTre.buocNo(t.dex) - t.buoc) + " bước (hạ Pokémon).");
                return;
            }
            if (p.isFullInventory()) {
                bao(p, "Hành trang đã đầy.");
                return;
            }
            synchronized (h) {
                if (!h.trung.remove(t)) {
                    return;
                }
                final int[] cu = h.giong.get(t.dex);
                if (cu == null || cu[0] + cu[1] + cu[2] < t.tongIv()) {
                    h.giong.put(t.dex, new int[]{t.ivHp, t.ivCong, t.ivThu});
                }
            }
            HeSinhThaiGame.phatThuong(p, NhaTre.thuongNo(t), "no trung " + t.chuoi());
            HeSinhThaiGame.nghienCuu(p, h, t.dex, SuuTamSinhThai.NV_AP);
            if (t.shiny) {
                HeSinhThaiGame.nghienCuu(p, h, t.dex, SuuTamSinhThai.NV_SHINY);
                HeSinhThaiGame.chupAnh(p, h, t.dex, SuuTamSinhThai.A_SHINY_TRUNG, HeSinhThaiGame.thoiTietMap(p.mapID));
                h.themDanhHieu("Thợ săn shiny");
                HeSinhThaiGame.baoTheGioi(p.charname + " vừa ấp nở " + SuuTamSinhThai.tenLoai(t.dex) + " shiny!");
            }
            if (t.tongIv() >= 84) {
                h.themDanhHieu("Nhà nhân giống");
            }
            bao(p, "Trứng nở: " + t.moTa().replace(" - sắp nở", "") + "! Pet đã vào hành trang.");
            real.DiemMocNhiemVu.khiNoTrung(p, t.dex); // diem moc ap_trung
            HeSinhThaiGame.danhDau(p);
        } else if (op == 1) {
            final List<String> m = new ArrayList<String>();
            for (int i = 0; i < ChoSinhThai.MUC_GIA_PHAN_TRAM.length; i++) {
                m.add("Giá " + giaTrung(t, i) + " xu (x" + ChoSinhThai.MUC_GIA_PHAN_TRAM[i] / 100.0 + ")");
            }
            menu(p, M_TRUNG_GIA, m);
        } else if (op == 2) {
            synchronized (h) {
                h.trung.remove(t);
            }
            bao(p, "Đã bỏ trứng.");
            HeSinhThaiGame.danhDau(p);
        }
    }

    /** Gia trung: gia tham chieu bac 3 x (1 + IV/93) x (shiny ? 5 : 1) x muc. */
    static long giaTrung(final NhaTre.Trung t, final int muc) {
        final long g = ChoSinhThai.giaTheoMuc(VatLieuSinhThai.TRUNG, 3, muc) * (93 + t.tongIv()) / 93L;
        return t.shiny ? g * 5 : g;
    }

    static void raoTrung(final Char p, final int op) {
        if (ThanhVien.chanVaBao(p, "cho_sinh_thai")) { // thanh vien: cho sinh thai
            return;
        }
        if (op >= 0 && ChongClone.chanMotBen(p, ChongClone.KIEU_CHO_SINH_THAI)) { // chong clone: cap / tuoi
            return;
        }
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || quaNhanh(p)) {
            return;
        }
        if (DuLieuHeSinhThai.dsTinBan(p.charDBID, -1, HeSinhThai.TOI_DA_TIN_BAN + 1).size() >= HeSinhThai.TOI_DA_TIN_BAN) {
            bao(p, "Bạn đã có " + HeSinhThai.TOI_DA_TIN_BAN + " tin bán đang mở.");
            return;
        }
        final NhaTre.Trung t;
        synchronized (h) {
            if (c.chiSo < 0 || c.chiSo >= h.trung.size()) {
                return;
            }
            t = h.trung.get(c.chiSo);
        }
        final long gia = giaTrung(t, op);
        final long phi = ChoSinhThai.phiDang(gia);
        if (!HeSinhThaiGame.truXu(p, phi, "phi dang trung")) {
            bao(p, "Cần " + phi + " xu phí đăng.");
            return;
        }
        synchronized (h) {
            if (!h.trung.remove(t)) {
                HeSinhThaiGame.congXu(p, phi, "hoan phi dang", Char.XU_HOAN);
                return;
            }
        }
        final int id = DuLieuHeSinhThai.themTinBan(p.charDBID, p.charname, VatLieuSinhThai.khoa(VatLieuSinhThai.TRUNG, 3), 1, gia,
                t.chuoi(), com.htbb.pokemon.DongHo.bay() + HeSinhThai.HAN_CHO_NGAY * 86400000L);
        if (id < 0) {
            synchronized (h) {
                h.trung.add(t);
            }
            HeSinhThaiGame.congXu(p, phi, "hoan phi dang", Char.XU_HOAN);
            bao(p, "Chợ tạm thời lỗi, thử lại sau.");
            return;
        }
        bao(p, "Đã rao trứng " + t.moTa() + " giá " + gia + " xu.");
        HeSinhThaiGame.danhDau(p);
    }

    // ================================================================== cho

    static final String[] MUC_CHO = {"Mua: Cá", "Mua: Berry", "Mua: Khoáng", "Mua: Hoá thạch", "Mua: Món ăn",
        "Mua: Thuốc", "Mua: Trứng", "Mua: Trang trí", "Tin bán của tôi (huỷ)", "Đơn đặt hàng đang mở (giao hàng)",
        "Tạo đơn đặt hàng", "Đơn của tôi (huỷ)", "Hộp thư: nhận tiền/hàng"};
    static final int[] LOAI_MUC_CHO = {VatLieuSinhThai.L_CA, VatLieuSinhThai.L_BERRY, VatLieuSinhThai.L_KHOANG,
        VatLieuSinhThai.L_HOA_THACH, VatLieuSinhThai.L_MON_AN, VatLieuSinhThai.L_THUOC, VatLieuSinhThai.L_TRUNG,
        VatLieuSinhThai.L_TRANG_TRI};

    static void moCho(final Char p) {
        final List<String> m = new ArrayList<String>(java.util.Arrays.asList(MUC_CHO));
        menu(p, M_CHO, m);
    }

    static void cho(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op < LOAI_MUC_CHO.length) {
            c.loai = LOAI_MUC_CHO[op];
            c.ds.clear();
            final List<String> m = new ArrayList<String>();
            for (final DuLieuHeSinhThai.TinBan t : DuLieuHeSinhThai.dsTinBan(0, c.loai, 30)) {
                c.ds.add(t.id);
                m.add(moTaTin(t));
            }
            if (m.isEmpty()) {
                bao(p, "Chưa có ai rao bán mục này.");
                return;
            }
            menu(p, M_CHO_LOAI, m);
            return;
        }
        switch (op - LOAI_MUC_CHO.length) {
            case 0: {
                c.ds.clear();
                final List<String> m = new ArrayList<String>();
                for (final DuLieuHeSinhThai.TinBan t : DuLieuHeSinhThai.dsTinBan(p.charDBID, -1, 30)) {
                    c.ds.add(t.id);
                    m.add("Huỷ: " + moTaTin(t));
                }
                if (m.isEmpty()) {
                    bao(p, "Bạn không có tin bán nào.");
                    return;
                }
                menu(p, M_CHO_CUA_TOI, m);
                break;
            }
            case 1: {
                c.ds.clear();
                final List<String> m = new ArrayList<String>();
                for (final DuLieuHeSinhThai.Don d : DuLieuHeSinhThai.dsDon(0, 30, false)) {
                    if (d.idMua == p.charDBID) {
                        continue;
                    }
                    c.ds.add(d.id);
                    m.add(moTaDon(d));
                }
                if (m.isEmpty()) {
                    bao(p, "Chưa có đơn đặt hàng nào.");
                    return;
                }
                menu(p, M_DON, m);
                break;
            }
            case 2: {
                c.ds.clear();
                final List<String> m = new ArrayList<String>();
                for (final int ma : VatLieuSinhThai.tatCa()) {
                    final int l = VatLieuSinhThai.loai(ma);
                    if (l == VatLieuSinhThai.L_TRUNG || l == VatLieuSinhThai.L_RAC) {
                        continue;
                    }
                    c.ds.add(ma);
                    m.add(VatLieuSinhThai.ten(ma) + " (" + VatLieuSinhThai.TEN_LOAI[l] + ")");
                }
                menu(p, M_DON_TAO_MA, m);
                break;
            }
            case 3: {
                c.ds.clear();
                final List<String> m = new ArrayList<String>();
                for (final DuLieuHeSinhThai.Don d : DuLieuHeSinhThai.dsDon(p.charDBID, 30, false)) {
                    c.ds.add(d.id);
                    m.add("Huỷ: " + moTaDon(d));
                }
                if (m.isEmpty()) {
                    bao(p, "Bạn không có đơn nào.");
                    return;
                }
                menu(p, M_DON_CUA_TOI, m);
                break;
            }
            case 4:
                nhanHopThu(p);
                break;
            default:
                break;
        }
    }

    static String moTaTin(final DuLieuHeSinhThai.TinBan t) {
        final int ma = VatLieuSinhThai.maCuaKhoa(t.khoaVat);
        if (ma == VatLieuSinhThai.TRUNG) {
            final NhaTre.Trung tr = NhaTre.Trung.doc(t.duLieu);
            return (tr != null ? "Trứng " + tr.moTa() : "Trứng") + " - " + t.giaMoi + " xu - " + t.tenBan;
        }
        return VatLieuSinhThai.tenKhoa(t.khoaVat) + " x" + t.soLuong + " - " + t.giaMoi + " xu/cái - " + t.tenBan;
    }

    static String moTaDon(final DuLieuHeSinhThai.Don d) {
        return "Cần " + (d.soCan - d.soDaGiao) + " " + VatLieuSinhThai.ten(d.maVat) + " - " + d.giaMoi + " xu/cái - " + d.tenMua;
    }

    static void chonTin(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        c.ma = c.ds.get(op);
        final DuLieuHeSinhThai.TinBan t = DuLieuHeSinhThai.layTinBan(c.ma);
        if (t == null) {
            bao(p, "Tin này đã bán hoặc đã huỷ.");
            return;
        }
        menu(p, M_CHO_MUA, new ArrayList<String>(java.util.Arrays.asList("Mua ngay: " + moTaTin(t) + " (tổng "
                + t.giaMoi * t.soLuong + " xu)", "Quay lại")));
    }

    static void mua(final Char p) {
        if (ThanhVien.chanVaBao(p, "cho_sinh_thai")) { // thanh vien: cho sinh thai
            return;
        }
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || quaNhanh(p)) {
            return;
        }
        final DuLieuHeSinhThai.TinBan t = DuLieuHeSinhThai.layTinBan(c.ma);
        if (t == null) {
            bao(p, "Tin này đã bán hoặc đã huỷ.");
            return;
        }
        if (t.idBan == p.charDBID) {
            bao(p, "Không mua hàng của chính mình (vào Tin bán của tôi để huỷ).");
            return;
        }
        final boolean laTrung = VatLieuSinhThai.maCuaKhoa(t.khoaVat) == VatLieuSinhThai.TRUNG;
        if (laTrung && h.trung.size() >= HeSinhThai.TRUNG_TOI_DA_TUI) {
            bao(p, "Túi trứng đã đầy (" + HeSinhThai.TRUNG_TOI_DA_TUI + ").");
            return;
        }
        final long tong = t.giaMoi * t.soLuong;
        // chong clone: nguoi mua (gui xu) -> nguoi ban (co the offline, nhan qua hop thu).
        if (ChongClone.chanChuyen(p, t.idBan, t.tenBan, ChongClone.KIEU_CHO_SINH_THAI, tong)) {
            return;
        }
        if (!HeSinhThaiGame.truXu(p, tong, "mua cho " + t.id, Char.XU_CHUYEN)) {
            bao(p, "Không đủ " + tong + " xu.");
            return;
        }
        if (!DuLieuHeSinhThai.chotTinBan(t.id, 1, 0)) {
            HeSinhThaiGame.congXu(p, tong, "hoan mua cho " + t.id, Char.XU_CHUYEN);
            bao(p, "Người khác vừa mua mất tin này.");
            return;
        }
        LauDai.ghiXuRa(tong - ChoSinhThai.nguoiBanNhan(tong)); // nguoi ban nhan tong - thue; thue bi dot
        nhanHang(h, t.khoaVat, t.soLuong, t.duLieu);
        traTienNguoiBan(t.idBan, ChoSinhThai.nguoiBanNhan(tong), "Bán " + VatLieuSinhThai.tenKhoa(t.khoaVat) + " x" + t.soLuong);
        ChongClone.ghi(p, t.idBan, t.tenBan, ChongClone.KIEU_CHO_SINH_THAI, tong, "sinh thai " + VatLieuSinhThai.tenKhoa(t.khoaVat) + " x" + t.soLuong);
        bao(p, "Đã mua " + moTaTin(t) + ".");
        HeSinhThaiGame.danhDau(p);
        try {
            data.Database.instance.saveOrtherLog("", p.charname, "cho sinh thai mua tin " + t.id + " cua " + t.tenBan + " " + tong
                    + " xu", "hesinhthai");
        } catch (final Exception ignored) {
        }
    }

    static void nhanHang(final HoSoSinhThai h, final int khoa, final int so, final String duLieu) {
        if (VatLieuSinhThai.maCuaKhoa(khoa) == VatLieuSinhThai.TRUNG) {
            final NhaTre.Trung tr = NhaTre.Trung.doc(duLieu);
            if (tr != null) {
                synchronized (h) {
                    h.trung.add(tr);
                }
            }
        } else {
            h.them(khoa, so);
        }
    }

    /** Tra tien nguoi ban: online -> cong thang; offline -> hop thu. */
    static void traTienNguoiBan(final int idBan, final long xu, final String ghiChu) {
        final Char ban = HeSinhThaiGame.timOnline(idBan);
        if (ban != null) {
            HeSinhThaiGame.congXu(ban, xu, "ban cho sinh thai", Char.XU_CHUYEN);
            real.DiemMocNhiemVu.khiBan(ban); // diem moc cho_ban (online: tinh ngay)
            bao(ban, "Chợ sinh thái: " + ghiChu + " +" + xu + " xu (đã trừ thuế).");
        } else {
            DuLieuHeSinhThai.guiHopThu(idBan, xu, 0, 0, "", ghiChu);
            real.DiemMocNhiemVu.ghiChoNguoiBan(idBan, ghiChu); // offline: ghi tob_nhiem_vu_cho, ap luc vao game
        }
    }

    static void huyTin(final Char p, final int op) {
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || op >= c.ds.size()) {
            return;
        }
        final DuLieuHeSinhThai.TinBan t = DuLieuHeSinhThai.layTinBan(c.ds.get(op));
        if (t == null || t.idBan != p.charDBID || !DuLieuHeSinhThai.chotTinBan(t.id, 2, p.charDBID)) {
            bao(p, "Không huỷ được (đã bán hoặc đã huỷ).");
            return;
        }
        nhanHang(h, t.khoaVat, t.soLuong, t.duLieu);
        bao(p, "Đã huỷ tin, hàng đã về Túi sinh thái (phí đăng không hoàn).");
        HeSinhThaiGame.danhDau(p);
    }

    static void chonDon(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        c.ma = c.ds.get(op);
        final List<String> m = new ArrayList<String>();
        final List<Integer> so = new ArrayList<Integer>();
        for (final int s : new int[]{1, 5, 10, 50}) {
            m.add("Giao " + s + " cái");
            so.add(s);
        }
        c.ds.clear();
        c.ds.addAll(so);
        menu(p, M_DON_GIAO, m);
    }

    static void giaoDon(final Char p, final int op) {
        if (ThanhVien.chanVaBao(p, "cho_sinh_thai")) { // thanh vien: cho sinh thai
            return;
        }
        if (op >= 0 && ChongClone.chanMotBen(p, ChongClone.KIEU_CHO_SINH_THAI)) { // chong clone: cap / tuoi
            return;
        }
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || op >= c.ds.size() || quaNhanh(p)) {
            return;
        }
        DuLieuHeSinhThai.Don don = null;
        for (final DuLieuHeSinhThai.Don d : DuLieuHeSinhThai.dsDon(0, 200, false)) {
            if (d.id == c.ma) {
                don = d;
            }
        }
        if (don == null || don.idMua == p.charDBID) {
            bao(p, "Đơn đã đủ hàng, đã huỷ hoặc là đơn của bạn.");
            return;
        }
        final int so = Math.min(c.ds.get(op), don.soCan - don.soDaGiao);
        if (so <= 0 || h.tongMa(don.maVat) < so) {
            bao(p, "Bạn cần " + so + " " + VatLieuSinhThai.ten(don.maVat) + " trong Túi sinh thái.");
            return;
        }
        final int bacThap;
        synchronized (h) {
            bacThap = h.layTheoMa(don.maVat, so);
            if (bacThap == 0) {
                return;
            }
        }
        if (!DuLieuHeSinhThai.giaoDon(don.id, so)) {
            h.them(VatLieuSinhThai.khoa(don.maVat, bacThap), so);
            bao(p, "Không giao được (đơn vừa đủ hoặc đã huỷ).");
            return;
        }
        final long tien = ChoSinhThai.nguoiBanNhan(don.giaMoi * so);
        // tien lay tu ky quy cua nguoi dat (chuyen); thue bi dot
        HeSinhThaiGame.congXu(p, tien, "giao don " + don.id, Char.XU_CHUYEN);
        LauDai.ghiXuRa(don.giaMoi * so - tien);
        ChongClone.ghiNhan(don.idMua, don.tenMua, p, ChongClone.KIEU_CHO_SINH_THAI, tien, "giao don " + don.id + " "
                + VatLieuSinhThai.ten(don.maVat) + " x" + so);
        final Char mua = HeSinhThaiGame.timOnline(don.idMua);
        final HoSoSinhThai hm = mua != null ? HeSinhThaiGame.hoSo(mua) : null;
        if (hm != null) {
            hm.them(VatLieuSinhThai.khoa(don.maVat, bacThap), so);
            HeSinhThaiGame.danhDau(mua);
            bao(mua, "Đơn đặt hàng: " + p.charname + " giao " + so + " " + VatLieuSinhThai.ten(don.maVat) + " vào Túi sinh thái.");
        } else {
            DuLieuHeSinhThai.guiHopThu(don.idMua, 0, VatLieuSinhThai.khoa(don.maVat, bacThap), so, "", "Hàng theo đơn từ " + p.charname);
        }
        bao(p, "Đã giao " + so + " " + VatLieuSinhThai.ten(don.maVat) + ", nhận " + tien + " xu (đã trừ thuế).");
        HeSinhThaiGame.danhDau(p);
    }

    static void taoDonMa(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        c.ma = c.ds.get(op);
        c.ds.clear();
        final List<String> m = new ArrayList<String>();
        for (final int so : new int[]{5, 10, 20, 50}) {
            m.add("Cần " + so + " " + VatLieuSinhThai.ten(c.ma));
            c.ds.add(so);
        }
        menu(p, M_DON_TAO_SO, m);
    }

    static void taoDonSo(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        c.so = c.ds.get(op);
        final int bac = VatLieuSinhThai.bacTheoCap(HeSinhThaiGame.cap(p));
        final List<String> m = new ArrayList<String>();
        for (int i = 0; i < ChoSinhThai.MUC_GIA_PHAN_TRAM.length; i++) {
            final long g = ChoSinhThai.giaTheoMuc(c.ma, bac, i);
            m.add("Trả " + g + " xu/cái (ký quỹ " + ChoSinhThai.kyQuy(g, c.so) + ")");
        }
        menu(p, M_DON_TAO_GIA, m);
    }

    static void taoDonGia(final Char p, final int op) {
        if (ThanhVien.chanVaBao(p, "cho_sinh_thai")) { // thanh vien: cho sinh thai
            return;
        }
        if (op >= 0 && ChongClone.chanMotBen(p, ChongClone.KIEU_CHO_SINH_THAI)) { // chong clone: cap / tuoi
            return;
        }
        final NguCanh c = nc(p);
        if (quaNhanh(p) || op < 0 || op >= ChoSinhThai.MUC_GIA_PHAN_TRAM.length) {
            return;
        }
        if (DuLieuHeSinhThai.dsDon(p.charDBID, HeSinhThai.TOI_DA_DON + 1, false).size() >= HeSinhThai.TOI_DA_DON) {
            bao(p, "Bạn đã có " + HeSinhThai.TOI_DA_DON + " đơn đang mở.");
            return;
        }
        final long gia = ChoSinhThai.giaTheoMuc(c.ma, VatLieuSinhThai.bacTheoCap(HeSinhThaiGame.cap(p)), op);
        final long kq = ChoSinhThai.kyQuy(gia, c.so);
        if (!HeSinhThaiGame.truXu(p, kq, "ky quy don", Char.XU_CHUYEN)) {
            bao(p, "Cần " + kq + " xu ký quỹ.");
            return;
        }
        final int id = DuLieuHeSinhThai.themDon(p.charDBID, p.charname, c.ma, c.so, gia,
                com.htbb.pokemon.DongHo.bay() + HeSinhThai.HAN_CHO_NGAY * 86400000L);
        if (id < 0) {
            HeSinhThaiGame.congXu(p, kq, "hoan ky quy", Char.XU_CHUYEN);
            bao(p, "Chợ tạm thời lỗi, thử lại sau.");
            return;
        }
        // ky quy = gia x so (chuyen, ve nguoi giao / hoan) + phi dang (khong hoan, bi dot)
        LauDai.ghiXuRa(kq - gia * Math.max(0, c.so));
        bao(p, "Đã tạo đơn: cần " + c.so + " " + VatLieuSinhThai.ten(c.ma) + " giá " + gia + " xu/cái. Hàng giao tới Túi sinh thái"
                + " (hoặc hộp thư nếu bạn offline).");
    }

    static void huyDon(final Char p, final int op) {
        final NguCanh c = nc(p);
        if (op >= c.ds.size()) {
            return;
        }
        final int id = c.ds.get(op);
        DuLieuHeSinhThai.Don don = null;
        for (final DuLieuHeSinhThai.Don d : DuLieuHeSinhThai.dsDon(p.charDBID, 200, false)) {
            if (d.id == id) {
                don = d;
            }
        }
        if (don == null) {
            bao(p, "Đơn không còn mở.");
            return;
        }
        final int daGiao = DuLieuHeSinhThai.dongDon(id, 2, p.charDBID);
        if (daGiao < 0) {
            bao(p, "Không huỷ được, thử lại.");
            return;
        }
        final long hoan = ChoSinhThai.hoanHuyDon(don.giaMoi, don.soCan, daGiao);
        HeSinhThaiGame.congXu(p, hoan, "huy don " + id, Char.XU_CHUYEN);
        bao(p, "Đã huỷ đơn, hoàn " + hoan + " xu (phí đăng không hoàn).");
    }

    static void nhanHopThu(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        int n = 0;
        long xu = 0;
        for (final DuLieuHeSinhThai.Thu t : DuLieuHeSinhThai.dsHopThu(p.charDBID)) {
            if (VatLieuSinhThai.maCuaKhoa(t.khoaVat) == VatLieuSinhThai.TRUNG && h.trung.size() >= HeSinhThai.TRUNG_TOI_DA_TUI) {
                continue;
            }
            if (!DuLieuHeSinhThai.xoaThu(t.id, p.charDBID)) {
                continue;
            }
            n++;
            xu += t.xu;
            if (t.so > 0) {
                nhanHang(h, t.khoaVat, t.so, t.duLieu);
            }
        }
        if (xu > 0) {
            HeSinhThaiGame.congXu(p, xu, "hop thu", Char.XU_CHUYEN); // hop thu chi chua tien ban cho / hoan ky quy don
        }
        bao(p, n == 0 ? "Hộp thư trống." : "Đã nhận " + n + " thư" + (xu > 0 ? ", " + xu + " xu" : "") + ".");
        if (n > 0) {
            HeSinhThaiGame.danhDau(p);
        }
    }

    static volatile long lanXuLyHetHan;

    /** Tin ban / don het han -> tra hang / ky quy ve hop thu (goi tu nhip 15 giay, toi da 1 lan/phut). */
    static void xuLyHetHanCho() {
        final long now = com.htbb.pokemon.DongHo.bay();
        if (now - lanXuLyHetHan < 60000L) {
            return;
        }
        lanXuLyHetHan = now;
        final String homNay = ":" + HeSinhThaiGame.homNay();
        DA_DAO.keySet().removeIf((k) -> !k.endsWith(homNay));
        for (final DuLieuHeSinhThai.TinBan t : DuLieuHeSinhThai.tinHetHan()) {
            if (DuLieuHeSinhThai.chotTinBan(t.id, 3, 0)) {
                DuLieuHeSinhThai.guiHopThu(t.idBan, 0, t.khoaVat, t.soLuong, t.duLieu, "Tin bán hết hạn");
            }
        }
        for (final DuLieuHeSinhThai.Don d : DuLieuHeSinhThai.dsDon(0, 500, true)) {
            final int daGiao = DuLieuHeSinhThai.dongDon(d.id, 3, 0);
            if (daGiao >= 0) {
                DuLieuHeSinhThai.guiHopThu(d.idMua, ChoSinhThai.hoanHuyDon(d.giaMoi, d.soCan, daGiao), 0, 0, "", "Đơn hết hạn - hoàn ký quỹ");
            }
        }
    }

    // ================================================================== vuon

    static int diemTrangTri(final HoSoSinhThai h) {
        int s = 0;
        synchronized (h) {
            for (final Map.Entry<Integer, Integer> e : h.trangTri.entrySet()) {
                s += VatLieuSinhThai.giaPhanNghin(e.getKey()) / 100 * e.getValue();
            }
        }
        return s;
    }

    static void moVuon(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final List<String> m = new ArrayList<String>();
        m.add("Xem vườn của tôi (điểm trang trí " + diemTrangTri(h) + ")");
        m.add("Cất trang trí về túi");
        m.add("Tưới vườn bạn bè (" + h.tuoiChoHomNay + "/5 hôm nay)");
        m.add("Thăm vườn bạn bè");
        m.add("Hướng dẫn trồng berry");
        menu(p, M_VUON, m);
    }

    static String moTaVuon(final String chu, final HoSoSinhThai h) {
        final StringBuilder b = new StringBuilder("Vườn của ").append(chu).append('\n');
        synchronized (h) {
            if (h.trangTri.isEmpty()) {
                b.append("(chưa có trang trí)\n");
            }
            for (final Map.Entry<Integer, Integer> e : h.trangTri.entrySet()) {
                b.append("- ").append(VatLieuSinhThai.ten(e.getKey())).append(" x").append(e.getValue()).append('\n');
            }
            b.append("Điểm trang trí: ").append(diemTrangTri(h)).append(", thân thiện: ").append(h.thanThien)
                    .append(", được tưới hôm nay: ").append(h.tuoiNhanHomNay).append("/5");
            if (!h.danhHieu.isEmpty()) {
                b.append("\nDanh hiệu: ").append(String.join(", ", h.danhHieu));
            }
        }
        return b.toString();
    }

    static void vuon(final Char p, final int op) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final NguCanh c = nc(p);
        switch (op) {
            case 0:
                bao(p, moTaVuon(p.charname, h));
                break;
            case 1: {
                c.ds.clear();
                final List<String> m = new ArrayList<String>();
                synchronized (h) {
                    for (final Map.Entry<Integer, Integer> e : h.trangTri.entrySet()) {
                        c.ds.add(e.getKey());
                        m.add("Cất " + VatLieuSinhThai.ten(e.getKey()) + " (" + e.getValue() + ")");
                    }
                }
                if (m.isEmpty()) {
                    bao(p, "Vườn chưa có trang trí. Hãy chế tác đồ trang trí ở mục Nấu ăn, chế thuốc, chế tác (dùng kỹ năng Khảo cổ).");
                    return;
                }
                menu(p, M_VUON_CAT, m);
                break;
            }
            case 2:
            case 3: {
                c.ds.clear();
                c.loai = op;
                final List<String> m = new ArrayList<String>();
                if (p.friendListName != null) {
                    for (final String ten : new ArrayList<String>(p.friendListName)) {
                        final Char b = real.CharManager.instance.getCharByCharName(ten);
                        if (b != null && b.charDBID != p.charDBID) {
                            c.ds.add(b.charDBID);
                            m.add(b.charname);
                        }
                    }
                }
                if (m.isEmpty()) {
                    bao(p, "Không có bạn bè nào đang online.");
                    return;
                }
                menu(p, M_VUON_BAN, m);
                break;
            }
            case 4:
                bao(p, "Trồng berry: dùng khu vườn (ô đất) sẵn có của bạn - các hạt giống nay là berry Oran, Pecha, Rawst,"
                        + " Cheri, Chesto, Leppa, Sitrus, Lum. Mỗi lần thu hoạch nhận thêm berry vào Túi sinh thái (bạn bè tưới:"
                        + " +5%/lần, tối đa +25%). Berry dùng để nấu ăn và chế thuốc.");
                break;
            default:
                break;
        }
    }

    static void catTrangTri(final Char p, final int op) {
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || op >= c.ds.size()) {
            return;
        }
        final int ma = c.ds.get(op);
        synchronized (h) {
            final int so = h.trangTri.getOrDefault(ma, 0);
            if (so <= 0) {
                return;
            }
            if (so == 1) {
                h.trangTri.remove(ma);
            } else {
                h.trangTri.put(ma, so - 1);
            }
            h.them(VatLieuSinhThai.khoa(ma, 1), 1);
        }
        bao(p, "Đã cất " + VatLieuSinhThai.ten(ma) + " về túi.");
        HeSinhThaiGame.danhDau(p);
    }

    static void datTrangTri(final Char p, final int op) {
        moTui(p);
    }

    static void tuoiBan(final Char p, final int op) {
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || op >= c.ds.size()) {
            return;
        }
        final Char b = HeSinhThaiGame.timOnline(c.ds.get(op));
        final HoSoSinhThai hb = b != null ? HeSinhThaiGame.hoSo(b) : null;
        if (hb == null) {
            bao(p, "Bạn ấy vừa offline.");
            return;
        }
        if (c.loai == 3) {
            bao(p, moTaVuon(b.charname, hb));
            return;
        }
        if (h.tuoiChoHomNay >= 5) {
            bao(p, "Hôm nay bạn đã tưới 5 vườn.");
            return;
        }
        if (hb.tuoiNhanHomNay >= 5) {
            bao(p, "Vườn của " + b.charname + " đã được tưới đủ hôm nay.");
            return;
        }
        synchronized (h) {
            h.tuoiChoHomNay++;
            h.thanThien++;
            if (h.thanThien >= 50) {
                h.themDanhHieu("Bạn của muôn loài");
            }
        }
        synchronized (hb) {
            hb.tuoiNhanHomNay++;
            hb.thanThien++;
        }
        bao(p, "Đã tưới vườn của " + b.charname + " (+5% berry lần thu hoạch tới của họ).");
        bao(b, p.charname + " vừa tưới vườn của bạn!");
        HeSinhThaiGame.danhDau(p);
        HeSinhThaiGame.danhDau(b);
    }

    // ================================================================== suu tam

    static void moSuuTam(final Char p) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final List<String> m = new ArrayList<String>();
        m.add("Album ảnh (" + h.album.size() + ")");
        m.add("Nghiên cứu Pokédex (" + h.nghienCuu.size() + " loài)");
        m.add("Nhận thưởng vùng sinh thái");
        m.add("Danh hiệu (" + h.danhHieu.size() + ")");
        menu(p, M_SUU_TAM, m);
    }

    static void suuTam(final Char p, final int op) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        final StringBuilder b = new StringBuilder();
        switch (op) {
            case 0: {
                b.append("Album: ").append(h.album.size()).append('/').append(SuuTamSinhThai.ALBUM_TOI_DA).append(" ảnh\n");
                synchronized (h) {
                    for (int i = h.album.size() - 1, n = 0; i >= 0 && n < 12; i--, n++) {
                        b.append("- ").append(SuuTamSinhThai.moTaAnh(h.album.get(i))).append('\n');
                    }
                }
                b.append("Mốc: ");
                for (int i = 0; i < SuuTamSinhThai.MOC_ALBUM.length; i++) {
                    b.append(i == 0 ? "" : ", ").append(SuuTamSinhThai.MOC_ALBUM[i]).append((h.mocAlbum & (1 << i)) != 0 ? " (đã nhận)" : "");
                }
                bao(p, b.toString());
                break;
            }
            case 1: {
                int xong = 0;
                synchronized (h) {
                    for (final Map.Entry<Integer, Integer> e : h.nghienCuu.entrySet()) {
                        if (SuuTamSinhThai.xongLoai(e.getValue(), HeSinhThaiGame.apDung(e.getKey()))) {
                            xong++;
                        }
                    }
                    b.append("Nghiên cứu: ").append(h.nghienCuu.size()).append(" loài đã có ghi chép, ").append(xong)
                            .append(" loài hoàn tất.\nGần đây:\n");
                    int n = 0;
                    for (final Map.Entry<Integer, Integer> e : h.nghienCuu.descendingMap().entrySet()) {
                        if (n++ >= 10) {
                            break;
                        }
                        b.append("- ").append(SuuTamSinhThai.tenLoai(e.getKey())).append(": ");
                        final int ad = HeSinhThaiGame.apDung(e.getKey());
                        final List<String> nv = new ArrayList<String>();
                        for (int i = 0; i < SuuTamSinhThai.BIT_NV.length; i++) {
                            final int bit = SuuTamSinhThai.BIT_NV[i];
                            if ((ad & bit) != 0) {
                                nv.add(((e.getValue() & bit) != 0 ? "(x) " : "( ) ") + SuuTamSinhThai.TEN_NV[i]);
                            }
                        }
                        b.append(String.join(", ", nv)).append('\n');
                    }
                }
                bao(p, b.toString().trim());
                break;
            }
            case 2: {
                final NguCanh c = nc(p);
                c.ds.clear();
                final List<String> m = new ArrayList<String>();
                for (final Map.Entry<Integer, java.util.Set<Integer>> e : HeSinhThaiGame.loaiTheoVung.entrySet()) {
                    int xong = 0;
                    for (final Integer dex : e.getValue()) {
                        if (SuuTamSinhThai.xongLoai(h.nghienCuu(dex), HeSinhThaiGame.apDung(dex))) {
                            xong++;
                        }
                    }
                    c.ds.add(e.getKey());
                    m.add(SinhThaiTheGioi.tenVung(e.getKey()) + ": " + xong + "/" + e.getValue().size()
                            + ((h.vungXong & (1 << e.getKey())) != 0 ? " (đã nhận)" : ""));
                }
                menu(p, M_VUNG, m);
                break;
            }
            case 3: {
                if (real.DanhHieuGame.moBangNeuDuoc(p)) {
                    break; // 0.9.7: client moi mo bang Danh hieu chung (Ho so -> Danh hieu), khong menu rieng
                }
                if (h.danhHieu.isEmpty()) {
                    bao(p, "Chưa có danh hiệu sinh thái.");
                    break;
                }
                final NguCanh c = nc(p);
                c.ds.clear();
                final List<String> m = new ArrayList<String>();
                synchronized (h) {
                    for (final String t : h.danhHieu) {
                        final int i = java.util.Arrays.asList(HoSoSinhThai.DANH_HIEU).indexOf(t);
                        if (i >= 0) {
                            c.ds.add(i);
                            m.add("Đeo: " + t + (p.idEffDanhHieu == HeSinhThai.ID_EFF_DANH_HIEU_DAU + i ? " (đang đeo)" : ""));
                        }
                    }
                }
                c.ds.add(-1);
                m.add("Tháo danh hiệu sinh thái");
                menu(p, M_DANH_HIEU, m);
                break;
            }
            default:
                break;
        }
    }

    /** Deo / thao danh hieu sinh thai: Char.idEffDanhHieu = ID_EFF_DANH_HIEU_DAU + chi so (client ve tren dau). */
    static void deoDanhHieu(final Char p, final int op) {
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || op >= c.ds.size()) {
            return;
        }
        final int i = c.ds.get(op);
        if (i < 0) {
            if (p.idEffDanhHieu >= HeSinhThai.ID_EFF_DANH_HIEU_DAU
                    && p.idEffDanhHieu < HeSinhThai.ID_EFF_DANH_HIEU_DAU + HoSoSinhThai.DANH_HIEU.length) {
                p.idEffDanhHieu = -1;
            }
        } else {
            if (!h.coDanhHieu(HoSoSinhThai.DANH_HIEU[i])) {
                return;
            }
            // 0.9.7: qua DanhHieuGame (dat ca currentDanhHieu - truoc day CharVongLap thao ngay o nhip sau vi
            // currentDanhHieu = null) ; khong co mau trong data_danh_hieu thi giu cach cu
            if (real.DanhHieuGame.deo(p, HeSinhThai.ID_EFF_DANH_HIEU_DAU + i)) {
                bao(p, "Đang đeo danh hiệu " + HoSoSinhThai.DANH_HIEU[i] + ".");
                return;
            }
            p.idEffDanhHieu = HeSinhThai.ID_EFF_DANH_HIEU_DAU + i;
        }
        try {
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
        } catch (final Exception ignored) {
        }
        bao(p, i < 0 ? "Đã tháo danh hiệu sinh thái." : "Đang đeo danh hiệu " + HoSoSinhThai.DANH_HIEU[i] + ".");
    }

    static void nhanVung(final Char p, final int op) {
        final NguCanh c = nc(p);
        final HoSoSinhThai h = hoSo(p);
        if (h == null || op >= c.ds.size()) {
            return;
        }
        final int v = c.ds.get(op);
        final java.util.Set<Integer> ds = HeSinhThaiGame.loaiTheoVung.get(v);
        if (ds == null || ds.isEmpty()) {
            return;
        }
        final List<String> thieu = new ArrayList<String>();
        for (final Integer dex : ds) {
            if (!SuuTamSinhThai.xongLoai(h.nghienCuu(dex), HeSinhThaiGame.apDung(dex))) {
                thieu.add(SuuTamSinhThai.tenLoai(dex));
            }
        }
        if (!thieu.isEmpty()) {
            bao(p, SinhThaiTheGioi.tenVung(v) + ": còn " + thieu.size() + " loài chưa nghiên cứu xong (cần 2 nhiệm vụ mỗi loài): "
                    + String.join(", ", thieu.subList(0, Math.min(12, thieu.size()))) + (thieu.size() > 12 ? "..." : ""));
            return;
        }
        synchronized (h) {
            if ((h.vungXong & (1 << v)) != 0) {
                bao(p, "Đã nhận thưởng vùng này.");
                return;
            }
            h.vungXong |= 1 << v;
        }
        HeSinhThaiGame.phatThuong(p, "LUONGKHOA:" + HeSinhThai.THUONG_VUNG_LUONG_KHOA, "hoan thanh vung " + v);
        h.themDanhHieu("Nhà thám hiểm vùng");
        HeSinhThaiGame.baoTheGioi(p.charname + " đã hoàn thành nghiên cứu vùng " + SinhThaiTheGioi.tenVung(v) + "!");
        bao(p, "Hoàn thành vùng " + SinhThaiTheGioi.tenVung(v) + ": +" + HeSinhThai.THUONG_VUNG_LUONG_KHOA + " lượng khoá. HP và tấn công vĩnh viễn từ các vùng đã hoàn thành: +"
                + String.valueOf(SuuTamSinhThai.chiSoVinhVien(Integer.bitCount(h.vungXong)) / 10.0).replace('.', ',') + "%.");
        HeSinhThaiGame.danhDau(p);
    }

    // ================================================================== giai tri

    static void moGiaiTri(final Char p) {
        final List<String> m = new ArrayList<String>();
        m.add("Đố vui: lịch, thành tích");
        m.add("Đua Pokémon: đăng ký");
        m.add("Giải đấu tuần: đăng ký (" + HeSinhThaiGame.GiaiDau.DANG_KY.size() + " người)");
        m.add("Giải đấu tuần: xem cây đấu");
        menu(p, M_GIAI_TRI, m);
    }

    static void giaiTri(final Char p, final int op) {
        final HoSoSinhThai h = hoSo(p);
        if (h == null) {
            return;
        }
        switch (op) {
            case 0: {
                final StringBuilder b = new StringBuilder("Ai là Pokémon? diễn ra lúc ");
                for (int i = 0; i < HeSinhThai.GIO_DO_VUI.length; i++) {
                    b.append(i == 0 ? "" : " và ").append(HeSinhThai.GIO_DO_VUI[i]).append(':').append(HeSinhThai.PHUT_DO_VUI);
                }
                b.append(" mỗi ngày trên kênh thế giới. Gõ tên Pokémon vào chat, người đúng đầu tiên nhận ")
                        .append(HeSinhThai.THUONG_DO_VUI_LUONG_KHOA).append(" lượng khoá.\nBạn đã trả lời đúng: ").append(h.doVuiDung)
                        .append(" lần. Đua Pokémon: thắng ").append(h.duaThang).append(" lần (đua lúc ").append(HeSinhThai.GIO_DUA)
                        .append("h, gõ dua vào khung chat để đăng ký).");
                bao(p, b.toString());
                break;
            }
            case 1:
                HeSinhThaiGame.Dua.dangKy(p);
                break;
            case 2:
                if (HeSinhThaiGame.GiaiDau.vong != null) {
                    bao(p, "Giải đang diễn ra, hãy đăng ký cho tuần sau.");
                    break;
                }
                HeSinhThaiGame.GiaiDau.DANG_KY.add(p.charDBID);
                bao(p, "Đã đăng ký giải đấu tuần. Cây đấu công bố lúc 20:00 thứ Bảy, trận đấu được tính khi hạ đối thủ ở Giải Liên minh.");
                break;
            case 3:
                bao(p, HeSinhThaiGame.GiaiDau.moTa());
                break;
            default:
                break;
        }
    }

    static String tenNguoi(final int id) {
        final Char c = HeSinhThaiGame.timOnline(id);
        return c != null ? c.charname : "#" + id;
    }
}
