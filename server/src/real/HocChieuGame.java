package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.HuanLuyen;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.Learnset;
import com.htbb.pokemon.LoaiPokemon;
import com.htbb.pokemon.ThongSoLauDai;
import com.htbb.thongbao.ThongBao;
import data.Database;
import data.DatabaseHocChieu;
import io.Message;
import java.io.DataInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HOC CHIEU THEO LOAI + DIEM HUAN LUYEN (de xuat 1 da duyet; docs/thiet-ke-game.md §2.3, §4.4b). Noi lop thuan
 * {@link Learnset} / {@link HuanLuyen} vao game:
 * <ul>
 *   <li>khoi dong: nap data_learnset, data_huan_luyen, data_tang_truong_dong; ghi CharManager.LEVEL_ADD_SKILL va
 *       Map.priceLearnSkill TU CSDL (client cu 906 doc dung cap hoc / gia qua goi 35 / goi dang nhap);</li>
 *   <li>nap nhan vat (CharNapDuLieu): chuyen doi MOT lan diem tiem nang cu -&gt; Diem huan luyen (dau
 *       tob_chuyen_doi_can_bang 'huan_luyen'), chi so goc tinh lai tu cap + tang truong dong, chuan hoa chieu theo
 *       loai (go chieu loai chua hoc duoc, hoan diem ky nang tu dong, hoan xu chieu NPC mot lan);</li>
 *   <li>len cap / tien hoa: tu hoc chieu moi + the "X da hoc Y!", +Diem huan luyen;</li>
 *   <li>NPC Blaziken (lenh 80): kiem dung loai + du cap; cong diem ky nang (lenh 75): chi nang chieu da hoc;</li>
 *   <li>lenh -122 (client ban &gt;= {@link #BAN}): bang chieu theo loai, trang thai / phan bo / tay Diem huan luyen.</li>
 * </ul>
 * Tat (khoa chieu.learnset.bat / huan_luyen.bat = 0, sv.lau_dai = 0, hoac CSDL chua co du lieu): luat cu nguyen ven.
 */
public final class HocChieuGame {

    /** Lenh rieng (grep server + client truoc khi doi). */
    public static final int LENH = -122;
    /** Ban client dau tien biet lenh -122 (0.9.8). Client cu (906) chi nhan goi cu. */
    public static final int BAN = 908;

    /** C -&gt; S. */
    static final int XIN = 0, PHAN_BO = 1, TAY = 2;
    /** S -&gt; C. */
    static final int GOI_LEARNSET = 0, GOI_HUAN_LUYEN = 1;

    public static final String K_CHIEU = "chieu.learnset.bat", K_HUAN_LUYEN = "huan_luyen.bat",
            K_DIEM_MOI_CAP = "huan_luyen.diem_moi_cap", K_TRAN_THUONG = "huan_luyen.tran_diem_thuong",
            K_GIA_TAY = "huan_luyen.gia_tay_phan_nghin_gio", K_HOI_TAY = "huan_luyen.hoi_tay_gio";

    static final String TIEU_DE_BAO = "Thay đổi hệ thống chiêu và chỉ số";

    /** Trang thai Diem huan luyen trong bo nho (nap luc vao game). */
    static final class TrangThai {
        int[] phanBo;
        int lanTay;
        long tayLuc;
        long tongXuTay;
    }

    private static final ConcurrentHashMap<Integer, TrangThai> TT = new ConcurrentHashMap<Integer, TrangThai>();

    private HocChieuGame() {
    }

    // ================================================================== bat / tat, cau hinh

    public static boolean batChieu() {
        return LauDai.bat() && CauHinhTrungTam.so(K_CHIEU, 1L) == 1L && Learnset.coDuLieu();
    }

    public static boolean batHuanLuyen() {
        return LauDai.bat() && CauHinhTrungTam.so(K_HUAN_LUYEN, 1L) == 1L && HuanLuyen.coDuLieu();
    }

    /** Pokemon nay dang theo luat Diem huan luyen (da chuyen doi va nap trang thai). */
    public static boolean dangHuanLuyen(final Char p) {
        return p != null && batHuanLuyen() && TT.containsKey(p.charDBID);
    }

    static int diemMoiCap() {
        return (int) Math.max(0L, Math.min(10L, CauHinhTrungTam.so(K_DIEM_MOI_CAP, 2L)));
    }

    static int tranThuong() {
        return (int) Math.max(0L, Math.min(30L, CauHinhTrungTam.so(K_TRAN_THUONG, 16L)));
    }

    static long giaTayPhanNghin() {
        return Math.max(0L, CauHinhTrungTam.so(K_GIA_TAY, 1000L));
    }

    static long hoiTayGio() {
        return Math.max(0L, CauHinhTrungTam.so(K_HOI_TAY, 24L));
    }

    static boolean laNguoi(final Char p) {
        return p != null && p.isBot == -1 && p.charDBID > 0 && p.skill != null;
    }

    static boolean coGoiMoi(final Char p) {
        return p != null && p.getSession() != null && p.getSession().banClient >= BAN;
    }

    // ================================================================== khoi dong

    /** Goi luc khoi dong (TeamServer), TRUOC khi nhan nguoi choi. */
    public static void khoiDong() {
        if (!LauDai.bat()) {
            return;
        }
        try {
            DatabaseHocChieu.taoBang();
            napLai();
        } catch (final Throwable t) {
            System.out.println("[HOC CHIEU] LOI khoi dong: " + t);
        }
    }

    public static synchronized void napLai() {
        final List<Learnset.Dong> ls = DatabaseHocChieu.napLearnset();
        Learnset.dat(ls);
        HuanLuyen.datDong(DatabaseHocChieu.napDongHuanLuyen());
        HuanLuyen.datTangTruong(DatabaseHocChieu.napTangTruong());
        if (Learnset.coDuLieu()) {
            apBangCu();
        }
        System.out.println("[HOC CHIEU] learnset " + ls.size() + " dong, Diem huan luyen " + HuanLuyen.soDong()
                + " dong chi so" + (HuanLuyen.coDuLieu() ? "" : " (CHUA DU DU LIEU - giu diem tiem nang cu)"));
    }

    /** LEVEL_ADD_SKILL (cap hoc / cap nang chieu) va gia hoc o NPC lay tu learnset - mot nguon cho moi client. */
    static void apBangCu() {
        for (int c = 0; c < CharManager.LEVEL_ADD_SKILL.length; c++) {
            for (int o = 0; o < CharManager.LEVEL_ADD_SKILL[c].length; o++) {
                final Learnset.Dong r = Learnset.somNhat(c, o);
                if (r == null) {
                    continue;
                }
                final short[] a = CharManager.LEVEL_ADD_SKILL[c][o];
                for (int k = 0; k < a.length; k++) {
                    a[k] = (short) Math.min(127, r.moc[Math.min(k, Learnset.SO_MOC - 1)]);
                }
            }
        }
        for (int c = 0; c < Map.idSkill.length && c < Map.priceLearnSkill.length; c++) {
            for (int i = 0; i < Map.idSkill[c].length && i < Map.priceLearnSkill[c].length; i++) {
                final Learnset.Dong r = Learnset.somNhat(c, Map.idSkill[c][i]);
                if (r != null && r.laNpc()) {
                    Map.priceLearnSkill[c][i] = r.giaXu;
                }
            }
        }
    }

    // ================================================================== nap nhan vat

    /** CharNapDuLieu.initInfoFromDB, sau khi doc chi so / chieu (va HoSoLauDai). */
    public static void khiNap(final Char p) {
        if (!laNguoi(p)) {
            return;
        }
        TT.remove(p.charDBID);
        try {
            if (batHuanLuyen()) {
                napHuanLuyen(p);
            }
        } catch (final Throwable t) {
            TT.remove(p.charDBID);
            System.out.println("[HOC CHIEU] LOI nap huan luyen " + p.charname + ": " + t);
        }
        try {
            if (batChieu()) {
                chuyenDoiChieu(p);
            }
        } catch (final Throwable t) {
            System.out.println("[HOC CHIEU] LOI chuan hoa chieu " + p.charname + ": " + t);
        }
    }

    static int[] chiSoCu(final Char p) {
        return new int[]{p.strength, p.agitity, p.spirit, p.health, p.luck};
    }

    static int[] goc(final Char p) {
        final byte[] b = data.Database.basicPoint[p.charClass];
        return new int[]{b[0], b[1], b[2], b[3], b[4]};
    }

    static int soHuyHieu(final Char p) {
        try {
            final HoSoLauDai h = LauDaiNhanVat.lay(p);
            return h == null ? 0 : Integer.bitCount(h.gymHuyHieu);
        } catch (final Throwable t) {
            return 0;
        }
    }

    public static int tongDiem(final Char p) {
        return HuanLuyen.tongDiem(p.lastLV, soHuyHieu(p), diemMoiCap(), tranThuong());
    }

    static String so(final long n) {
        return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(n);
    }

    /** "HP 40, Tấn công 58" (bo dong 0 diem). */
    static String moTaPhanBo(final int[] pb) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; pb != null && i < pb.length; i++) {
            if (pb[i] <= 0) {
                continue;
            }
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(HuanLuyen.dong(i).ten).append(' ').append(pb[i]);
        }
        return b.toString();
    }

    static void napHuanLuyen(final Char p) {
        final int id = p.charDBID;
        final int coDau = DatabaseHocChieu.coBuoc(id, "huan_luyen");
        if (coDau < 0) {
            return; // loi CSDL: giu luat cu phien nay, lan sau lam lai
        }
        if (coDau == 0) {
            final int tong = tongDiem(p);
            final int[] cu = chiSoCu(p);
            final int[] pb = HuanLuyen.quyDoi(p.charClass, p.lastLV, cu, goc(p), p.basepoint, tong);
            final int conLai = tong - HuanLuyen.daDung(pb);
            final String ten = LoaiPokemon.ten(p.pokeDex).isEmpty() ? "Pokémon" : LoaiPokemon.ten(p.pokeDex);
            final String moTa = moTaPhanBo(pb);
            final boolean coGi = p.lastLV > 1 || p.basepoint > 0;
            final String bao = "Điểm tiềm năng tự do đã được thay bằng Điểm huấn luyện. Chỉ số gốc của " + ten
                    + " giờ tự tăng theo cấp. Bạn nhận " + tong + " Điểm huấn luyện"
                    + (moTa.isEmpty() ? "" : " (đã phân bổ theo cách cộng cũ: " + moTa + ")")
                    + (conLai > 0 ? ", còn " + conLai + " điểm chưa dùng" : "")
                    + " và 1 lần tẩy điểm miễn phí (Pokémon → Chỉ số → Huấn luyện).";
            final String kt = "cap " + p.lastLV + " class " + p.charClass + " cu SM/KL/TT/SK/MM " + cu[0] + "/" + cu[1] + "/"
                    + cu[2] + "/" + cu[3] + "/" + cu[4] + " con " + p.basepoint + " -> " + HuanLuyen.viet(pb) + " / " + tong;
            final int r = DatabaseHocChieu.chuyenDoiHuanLuyen(id, HuanLuyen.viet(pb), (coGi ? "bao=" + bao + "|" : "") + "kt=" + kt,
                    coGi);
            if (r < 0) {
                return;
            }
        }
        final Object[] st = DatabaseHocChieu.napTrangThai(id);
        if (st == null) {
            return;
        }
        final TrangThai t = new TrangThai();
        if (st.length == 0) {
            t.phanBo = new int[HuanLuyen.soDong()];
        } else {
            t.phanBo = HuanLuyen.docChuoi((String) st[0]);
            t.lanTay = ((Integer) st[1]).intValue();
            t.tayLuc = ((Long) st[2]).longValue();
            t.tongXuTay = ((Long) st[3]).longValue();
        }
        t.phanBo = HuanLuyen.catTheoTong(t.phanBo, tongDiem(p));
        TT.put(id, t);
        apChiSoGoc(p);
    }

    /** Chi so goc = goc dong + (cap - 1) x (1 + tang truong dong); het diem tiem nang tu do. */
    static void apChiSoGoc(final Char p) {
        final int[] g = HuanLuyen.chiSoGoc(p.charClass, p.lastLV, goc(p));
        if (g == null) {
            return;
        }
        p.strength = (short) Math.min(Short.MAX_VALUE, g[0]);
        p.agitity = (short) Math.min(Short.MAX_VALUE, g[1]);
        p.spirit = (short) Math.min(Short.MAX_VALUE, g[2]);
        p.health = (short) Math.min(Short.MAX_VALUE, g[3]);
        p.luck = (short) Math.min(Short.MAX_VALUE, g[4]);
        p.basepoint = 0;
    }

    /** Chuan hoa chieu luc nap: lan dau (chua co dau 'learnset') go chieu sai loai + hoan; sau do chi tu hoc them. */
    static void chuyenDoiChieu(final Char p) {
        final int n = Learnset.soO(p.charClass);
        if (n <= 0 || !Learnset.coLoai(p.pokeDex)) {
            return;
        }
        final int coDau = DatabaseHocChieu.coBuoc(p.charDBID, "learnset");
        if (coDau < 0) {
            chuanHoa(p, false);
            return;
        }
        if (coDau == 1) {
            chuanHoa(p, false);
            return;
        }
        final byte[] truoc = p.skill.clone();
        final int diemTruoc = p.skillpoint;
        final Learnset.KetQua kq = chuanHoa(p, true);
        final String ten = LoaiPokemon.ten(p.pokeDex);
        final StringBuilder bao = new StringBuilder();
        final List<String> goTen = new ArrayList<String>();
        for (final int o : kq.go) {
            final Learnset.Dong cua = Learnset.lay(p.pokeDex, o);
            final Learnset.Dong r = cua != null ? cua : Learnset.loaiHocKhac(p.pokeDex, p.charClass, o);
            final String tc = tenChieu(p, o);
            if (r != null) {
                goTen.add(tc + " (" + LoaiPokemon.ten(r.loai) + " cấp " + r.capHoc + ")");
            } else {
                goTen.add(tc);
            }
        }
        if (!goTen.isEmpty()) {
            bao.append("Chiêu học theo loài: ").append(ten).append(" chưa học được ").append(String.join(", ", goTen))
                    .append(" - sẽ tự học / học lại ở NPC khi đủ cấp. ");
        }
        final int hoanDiem = p.skillpoint - diemTruoc;
        if (hoanDiem > 0) {
            bao.append("Hoàn ").append(hoanDiem).append(" điểm kỹ năng (học chiêu giờ miễn phí). ");
        }
        if (!kq.hocMoi.isEmpty()) {
            final List<String> moi = new ArrayList<String>();
            for (final int o : kq.hocMoi) {
                moi.add(tenChieu(p, o));
            }
            bao.append("Đã tự học: ").append(String.join(", ", moi)).append(". ");
        }
        final StringBuilder kt = new StringBuilder("cap ").append(p.lvDetail.lv).append(" dex ").append(p.pokeDex)
                .append(" chieu ").append(java.util.Arrays.toString(truoc)).append(" -> ").append(java.util.Arrays.toString(p.skill))
                .append(" diem ").append(diemTruoc).append(" -> ").append(p.skillpoint);
        // nhan vat moi (cap 1) / chi tu hoc chieu dau: khong can hop thoai
        final String s = p.lastLV <= 1 && kq.go.isEmpty() && kq.hoanNpc.isEmpty() ? "" : bao.toString().trim();
        final int r = DatabaseHocChieu.themBuoc(p.charDBID, "learnset", (s.isEmpty() ? "" : "bao=" + s + "|") + "kt=" + kt, 0L);
        if (r < 0) {
            // khong ghi duoc dau: tra lai nhu cu, lan sau lam lai (khong go chieu khi chua co dau)
            System.arraycopy(truoc, 0, p.skill, 0, Math.min(truoc.length, p.skill.length));
            p.skillpoint = (short) diemTruoc;
            chuanHoa(p, false);
            return;
        }
        if (s.isEmpty()) {
            DatabaseHocChieu.danhDau(p.charDBID, "learnset", "da_bao");
        }
        for (final int o : kq.hoanNpc) {
            final Learnset.Dong d = Learnset.somNhat(p.charClass, o);
            final long gia = d == null ? 0L : d.giaXu;
            if (gia <= 0L) {
                continue;
            }
            DatabaseHocChieu.themBuoc(p.charDBID, "hoan_chieu_" + o, "bao=Hoàn " + so(gia) + " xu đã trả học "
                    + tenChieu(p, o) + ".|kt=o " + o + " cap cu " + truoc[o] + " dex " + p.pokeDex + " cap " + p.lvDetail.lv, gia);
        }
    }

    /** Chuan hoa cap chieu (go = lan dau). Ghi diem ky nang. */
    static Learnset.KetQua chuanHoa(final Char p, final boolean go) {
        final int n = Learnset.soO(p.charClass);
        if (n <= 0 || !Learnset.coLoai(p.pokeDex)) {
            return null;
        }
        final int oLoi = p.charClass >= 0 && p.charClass < Map.idSkill.length && Map.idSkill[p.charClass].length > 0
                ? Map.idSkill[p.charClass][0] : -1;
        final Learnset.KetQua kq = Learnset.chuanHoa(p.skill, n, p.pokeDex, p.charClass, p.lvDetail.lv, p.lastLV,
                oLoi, p.isAdmin, go);
        p.skillpoint = (short) kq.diemKyNang;
        return kq;
    }

    static String tenChieu(final Char p, final int o) {
        Learnset.Dong r = Learnset.lay(p.pokeDex, o);
        if (r == null) {
            r = Learnset.somNhat(p.charClass, o);
        }
        return r == null ? "chiêu " + (o + 1) : r.ten;
    }

    /** SelectCharHandler / LoginHandler: sau goi thong tin nhan vat dau tien. */
    public static void sauVaoGame(final Char p) {
        if (!laNguoi(p) || !LauDai.bat()) {
            return;
        }
        try {
            final List<Object[]> ds = DatabaseHocChieu.dsChoXuLy(p.charDBID);
            long hoan = 0L;
            final List<String> buocHoan = new ArrayList<String>();
            for (final Object[] r : ds) {
                if (((Integer) r[3]).intValue() == 0 && ((Long) r[2]).longValue() > 0L) {
                    hoan += ((Long) r[2]).longValue();
                    buocHoan.add((String) r[0]);
                }
            }
            if (hoan > 0L) {
                p.addXu(hoan, "hoan hoc chieu theo loai");
                Database.instance.saveCharAuto(p);
                for (final String b : buocHoan) {
                    DatabaseHocChieu.danhDau(p.charDBID, b, "da_hoan");
                }
            } else if (!ds.isEmpty()) {
                Database.instance.saveCharAuto(p); // luu ngay chieu / chi so vua chuyen doi
            }
            final StringBuilder bao = new StringBuilder();
            for (final Object[] r : ds) {
                if (((Integer) r[4]).intValue() != 0) {
                    continue;
                }
                final String b = phanBao((String) r[1]);
                if (!b.isEmpty()) {
                    if (bao.length() > 0) {
                        bao.append('\n');
                    }
                    bao.append("• ").append(b);
                }
                DatabaseHocChieu.danhDau(p.charDBID, (String) r[0], "da_bao");
            }
            if (bao.length() > 0) {
                p.sendMessage(MessageCreator.createServerAlertMessage(TIEU_DE_BAO + "\n" + bao, ""));
            }
        } catch (final Throwable t) {
            System.out.println("[HOC CHIEU] LOI sau vao game " + p.charname + ": " + t);
        }
        guiTatCa(p);
    }

    /** "bao=...|kt=..." -> phan bao. */
    static String phanBao(final String chiTiet) {
        if (chiTiet == null || !chiTiet.startsWith("bao=")) {
            return "";
        }
        final int k = chiTiet.indexOf("|kt=");
        return (k < 0 ? chiTiet.substring(4) : chiTiet.substring(4, k)).trim();
    }

    // ================================================================== len cap / tien hoa

    /**
     * Thay khoi cong chi so khi len cap moi (MapXuLyLenh, hai ban sao). Luat cu: +5 diem tiem nang, +1 moi chi so,
     * +1 diem ky nang. Luat moi: chi so goc tinh lai theo cap + tang truong dong, +Diem huan luyen (suy tu cap),
     * tu hoc chieu moi.
     */
    public static void congLenCap(final Char p, final int capMoi) {
        final int d = capMoi - p.lastLV;
        if (d <= 0) {
            return;
        }
        final boolean moi = dangHuanLuyen(p);
        if (!moi) {
            p.basepoint = (short) (p.basepoint + 5 * d);
            p.strength = (short) (p.strength + d);
            p.agitity = (short) (p.agitity + d);
            p.spirit = (short) (p.spirit + d);
            p.health = (short) (p.health + d);
            p.luck = (short) (p.luck + d);
        }
        p.skillpoint = (short) (p.skillpoint + d);
        p.lastLV = (short) capMoi;
        if (moi) {
            apChiSoGoc(p);
            final TrangThai t = TT.get(p.charDBID);
            final int con = tongDiem(p) - (t == null ? 0 : HuanLuyen.daDung(t.phanBo));
            ThongBao.gui(p, ThongBao.TIN, "+" + (diemMoiCap() * d) + " Điểm huấn luyện",
                    "Còn " + con + " điểm · Pokémon → Chỉ số → Huấn luyện", ThongBao.HINH_POKEMON, p.pokeDex);
        }
        if (batChieu()) {
            final Learnset.KetQua kq = chuanHoa(p, false);
            baoHocMoi(p, kq);
        }
        if (coGoiMoi(p)) {
            guiTatCa(p);
        }
    }

    /** TienHoaPokemon.tienHoa: loai moi -&gt; chieu moi cua loai. */
    public static void khiTienHoa(final Char p) {
        if (!laNguoi(p) || !batChieu()) {
            return;
        }
        try {
            final Learnset.KetQua kq = chuanHoa(p, false);
            baoHocMoi(p, kq);
            if (p.lvDetail.lv > p.lastLV) {
                return; // dang len cap: congLenCap tinh lai chi so va gui sau
            }
            p.calculateAttrib();
            guiThongTin(p);
            if (coGoiMoi(p)) {
                guiLearnset(p);
            }
        } catch (final Throwable t) {
            System.out.println("[HOC CHIEU] LOI tien hoa " + p.charname + ": " + t);
        }
    }

    static void baoHocMoi(final Char p, final Learnset.KetQua kq) {
        if (kq == null || kq.hocMoi.isEmpty()) {
            return;
        }
        final String ten = LoaiPokemon.ten(p.pokeDex).isEmpty() ? "Pokémon" : LoaiPokemon.ten(p.pokeDex);
        for (final int o : kq.hocMoi) {
            ThongBao.gui(p, ThongBao.HIEM, ten + " đã học " + tenChieu(p, o) + "!", "Pokémon → Kỹ năng · gán vào ô nhanh",
                    ThongBao.HINH_POKEMON, p.pokeDex);
        }
    }

    // ================================================================== lenh cu (client 906)

    /** Map.doLearnSkil khi bat: hoc o NPC Blaziken theo loai. idx = vi tri trong Map.idSkill[class]. */
    public static void hocNpc(final Char p, final int lop, final int idx) {
        if (lop != p.charClass) {
            ThongBao.loi(p, "Không thể học chiêu của dòng Pokémon khác.", "");
            return;
        }
        if (idx < 0 || idx >= Map.idSkill[lop].length) {
            return;
        }
        final int o = Map.idSkill[lop][idx];
        final String tc = tenChieu(p, o);
        if (p.skill[o] >= 1) {
            ThongBao.loi(p, tc + ": đã học.", "Cộng điểm kỹ năng ở Pokémon → Kỹ năng");
            return;
        }
        final Learnset.Dong r = Learnset.lay(p.pokeDex, o);
        if (r == null) {
            final Learnset.Dong k = Learnset.loaiHocKhac(p.pokeDex, lop, o);
            ThongBao.loi(p, LoaiPokemon.ten(p.pokeDex) + " chưa học được " + tc + ".",
                    k == null ? "Loài này không học chiêu này" : "Tiến hoá thành " + LoaiPokemon.ten(k.loai) + " (cấp " + k.capHoc + ") để học");
            return;
        }
        if (p.lvDetail.lv < r.capHoc) {
            ThongBao.loi(p, tc + ": cần cấp " + r.capHoc + ".", "Pokémon của bạn đang cấp " + p.lvDetail.lv);
            return;
        }
        if (!r.laNpc()) {
            ThongBao.loi(p, tc + " tự học khi đủ cấp.", "");
            return;
        }
        if (p.getxu() < r.giaXu) {
            ThongBao.loi(p, "Không đủ xu.", "Cần " + so(r.giaXu) + " xu để học " + tc);
            return;
        }
        if (r.giaXu > 0) {
            p.subXu(r.giaXu, true, "map 80");
        }
        p.skill[o] = 1;
        chuanHoa(p, false);
        try {
            Database.instance.saveLogLearnSkill(p.charname, "Học skill: " + tc + " với giá: " + r.giaXu + " xu (loài " + p.pokeDex + ")");
        } catch (final Throwable ignored) {
        }
        guiThongTin(p);
        ThongBao.gui(p, ThongBao.HIEM, LoaiPokemon.ten(p.pokeDex) + " đã học " + tc + "!", "−" + so(r.giaXu) + " xu",
                ThongBao.HINH_POKEMON, p.pokeDex);
        if (coGoiMoi(p)) {
            guiLearnset(p);
        }
    }

    /** Map.doAddSkillPoint khi bat: chi nang cap chieu da hoc (cap 1..9). */
    public static void congDiemKyNang(final Char p, final int o) {
        if (o < 0 || o >= p.skill.length) {
            return;
        }
        final String tc = tenChieu(p, o);
        final int v = p.skill[o];
        if (v <= 0) {
            final Learnset.Dong r = Learnset.lay(p.pokeDex, o);
            if (r != null && r.laNpc()) {
                ThongBao.loi(p, tc + ": học ở NPC Blaziken (cấp " + r.capHoc + ").", so(r.giaXu) + " xu");
            } else if (r != null) {
                ThongBao.loi(p, tc + " tự học ở cấp " + r.capHoc + ".", "Không tốn điểm kỹ năng");
            } else {
                final Learnset.Dong k = Learnset.loaiHocKhac(p.pokeDex, p.charClass, o);
                ThongBao.loi(p, LoaiPokemon.ten(p.pokeDex) + " chưa học được " + tc + ".",
                        k == null ? "" : "Tiến hoá thành " + LoaiPokemon.ten(k.loai) + " (cấp " + k.capHoc + ")");
            }
            guiThongTin(p);
            return;
        }
        if (v >= Learnset.CAP_CHIEU_TOI_DA) {
            ThongBao.loi(p, tc + " đã đạt cấp tối đa.", "");
            return;
        }
        if (p.skillpoint <= 0) {
            ThongBao.loi(p, "Hết điểm kỹ năng.", "Lên cấp để nhận thêm (1 điểm / cấp)");
            return;
        }
        final int can = CharManager.getLvAddSkill(p.charClass, o, v);
        if (p.getLevel() < can) {
            ThongBao.loi(p, tc + ": cần Pokémon cấp " + can + " để lên cấp " + (v + 1) + ".", "");
            return;
        }
        p.skillpoint--;
        p.skill[o]++;
        p.setTimeOutBuff();
        try {
            final Message m = new Message(36);
            p.sendMessage(m);
            m.cleanup();
        } catch (final Throwable ignored) {
        }
        guiThongTin(p);
    }

    /** Char.resetKN (Keo ky nang) khi bat: chieu da hoc ve cap 1, tra lai toan bo diem. */
    public static boolean datLaiKyNang(final Char p) {
        if (!laNguoi(p) || !batChieu() || !Learnset.coLoai(p.pokeDex)) {
            return false;
        }
        for (int i = 0; i < p.skill.length; i++) {
            if (p.skill[i] > 1) {
                p.skill[i] = 1;
            }
        }
        chuanHoa(p, false);
        guiThongTin(p);
        return true;
    }

    /** Lenh 34 (cong diem tiem nang) khi da sang Diem huan luyen. true = da xu ly. */
    public static boolean chanCongTiemNang(final Char p) {
        if (!dangHuanLuyen(p)) {
            return false;
        }
        ThongBao.gui(p, ThongBao.TIN, "Điểm tiềm năng đã thay bằng Điểm huấn luyện.",
                coGoiMoi(p) ? "Pokémon → Chỉ số → Huấn luyện" : "Cập nhật game bản mới để phân bổ Điểm huấn luyện",
                ThongBao.HINH_KHONG, 0);
        return true;
    }

    /** Char.resetTN (Keo tiem nang) khi bat: tay Diem huan luyen mien phi (khong tinh lan tay, khong cho). */
    public static boolean tayBangKeo(final Char p) {
        if (!dangHuanLuyen(p)) {
            return false;
        }
        synchronized (p) {
            final TrangThai t = TT.get(p.charDBID);
            final int[] rong = new int[HuanLuyen.soDong()];
            if (t == null || !DatabaseHocChieu.luuTrangThai(p.charDBID, "", t.lanTay, t.tayLuc, t.tongXuTay)) {
                ThongBao.loi(p, "Chưa tẩy được Điểm huấn luyện, thử lại sau.", "");
                return true;
            }
            final int n = HuanLuyen.daDung(t.phanBo);
            t.phanBo = rong;
            p.calculateAttrib();
            guiThongTin(p);
            ThongBao.ketQua(p, "Đã tẩy Điểm huấn luyện bằng Kẹo tiềm năng.", "Nhận lại " + n + " điểm để phân bổ");
            if (coGoiMoi(p)) {
                guiHuanLuyen(p);
            }
        }
        return true;
    }

    // ================================================================== chi so (CharChiSo)

    /** Phan nghin cong them cua dong chi so {@code ma} (HuanLuyen.HP / TAN_CONG / PHONG_THU / PP), 0 neu tat. */
    public static int phanNghin(final Char p, final String ma) {
        if (p == null || !batHuanLuyen()) {
            return 0;
        }
        final TrangThai t = TT.get(p.charDBID);
        if (t == null) {
            return 0;
        }
        final int vt = HuanLuyen.viTri(ma);
        return vt < 0 || vt >= t.phanBo.length ? 0 : HuanLuyen.phanNghin(HuanLuyen.dong(vt), t.phanBo[vt]);
    }

    /** He chieu cua Pokemon (nhanh Eevee doi he o 2/8/10 theo learnset), -1 ngoai bang. */
    public static int heChieu(final Char p, final int idSkill) {
        if (p != null && batChieu() && Learnset.lay(p.pokeDex, idSkill) != null) {
            return Learnset.heChieu(p.pokeDex, p.charClass, idSkill);
        }
        return com.htbb.pokemon.ChieuPokemon.heChieu(p == null ? -1 : p.charClass, idSkill);
    }

    static void guiThongTin(final Char p) {
        try {
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
        } catch (final Throwable ignored) {
        }
    }

    // ================================================================== lenh -122

    public static void xuLy(final Char p, final Message msg) {
        if (!laNguoi(p)) {
            return;
        }
        try {
            final DataInputStream r = msg.dis;
            final int loai = r.readByte();
            switch (loai) {
                case XIN:
                    guiTatCa(p);
                    break;
                case PHAN_BO: {
                    final int n = r.readUnsignedByte();
                    final int[] them = new int[HuanLuyen.soDong()];
                    for (int i = 0; i < n && i < 16; i++) {
                        final int id = r.readByte();
                        final int v = r.readShort();
                        final int vt = HuanLuyen.viTriId(id);
                        if (vt >= 0) {
                            them[vt] += Math.max(-1, Math.min(1000, v));
                        }
                    }
                    phanBo(p, them);
                    break;
                }
                case TAY:
                    tay(p);
                    break;
                default:
                    break;
            }
        } catch (final Throwable t) {
            System.out.println("[HOC CHIEU] LOI lenh -122 " + p.charname + ": " + t);
        }
    }

    static void phanBo(final Char p, final int[] them) {
        if (!dangHuanLuyen(p)) {
            ThongBao.loi(p, "Điểm huấn luyện chưa mở trên máy chủ.", "");
            return;
        }
        synchronized (p) {
            final TrangThai t = TT.get(p.charDBID);
            final String loi = HuanLuyen.kiemPhanBo(t.phanBo, them, tongDiem(p));
            if (loi != null) {
                ThongBao.loi(p, loi, "");
                guiHuanLuyen(p);
                return;
            }
            final int[] moi = t.phanBo.clone();
            int tong = 0;
            final StringBuilder phu = new StringBuilder();
            for (int i = 0; i < moi.length; i++) {
                moi[i] += them[i];
                tong += them[i];
                if (them[i] > 0) {
                    if (phu.length() > 0) {
                        phu.append(" · ");
                    }
                    phu.append(HuanLuyen.dong(i).ten).append(" +")
                            .append(HuanLuyen.phanTram(HuanLuyen.phanNghin(HuanLuyen.dong(i), moi[i])));
                }
            }
            if (!DatabaseHocChieu.luuTrangThai(p.charDBID, HuanLuyen.viet(moi), t.lanTay, t.tayLuc, t.tongXuTay)) {
                ThongBao.loi(p, "Chưa lưu được, thử lại sau.", "");
                return;
            }
            t.phanBo = moi;
            p.calculateAttrib();
            guiThongTin(p);
            ThongBao.ketQua(p, "Đã phân bổ " + tong + " Điểm huấn luyện.", phu.toString());
            guiHuanLuyen(p);
        }
    }

    static void tay(final Char p) {
        if (!dangHuanLuyen(p)) {
            ThongBao.loi(p, "Điểm huấn luyện chưa mở trên máy chủ.", "");
            return;
        }
        synchronized (p) {
            final TrangThai t = TT.get(p.charDBID);
            final int n = HuanLuyen.daDung(t.phanBo);
            if (n <= 0) {
                ThongBao.loi(p, "Chưa phân bổ điểm nào để tẩy.", "");
                return;
            }
            final long bay = System.currentTimeMillis();
            final long cho = HuanLuyen.choTay(t.lanTay, t.tayLuc, bay, hoiTayGio());
            if (cho > 0L) {
                final long phut = (cho + 59999L) / 60000L;
                ThongBao.loi(p, "Tẩy điểm lại sau " + (phut / 60) + " giờ " + (phut % 60) + " phút.", "");
                guiHuanLuyen(p);
                return;
            }
            final long gia = HuanLuyen.giaTay(t.lanTay, ThongSoLauDai.xuMoiGio(Math.max(1, p.lvDetail.lv)), giaTayPhanNghin());
            if (gia > 0L && p.getxu() < gia) {
                ThongBao.loi(p, "Không đủ xu.", "Tẩy điểm cần " + so(gia) + " xu");
                return;
            }
            if (!DatabaseHocChieu.luuTrangThai(p.charDBID, "", t.lanTay + 1, bay, t.tongXuTay + gia)) {
                ThongBao.loi(p, "Chưa tẩy được, thử lại sau.", "");
                return;
            }
            if (gia > 0L) {
                p.subXu(gia, true, "tay diem huan luyen");
            }
            t.phanBo = new int[HuanLuyen.soDong()];
            t.lanTay++;
            t.tayLuc = bay;
            t.tongXuTay += gia;
            p.calculateAttrib();
            guiThongTin(p);
            final long giaSau = HuanLuyen.giaTay(t.lanTay, ThongSoLauDai.xuMoiGio(Math.max(1, p.lvDetail.lv)), giaTayPhanNghin());
            ThongBao.ketQua(p, "Đã tẩy Điểm huấn luyện: nhận lại " + n + " điểm.",
                    (gia > 0L ? "−" + so(gia) + " xu · " : "Lần đầu miễn phí · ") + "lần sau " + so(giaSau) + " xu, chờ " + hoiTayGio() + " giờ");
            guiHuanLuyen(p);
        }
    }

    // ================================================================== goi S -> C

    public static void guiTatCa(final Char p) {
        if (!coGoiMoi(p)) {
            return;
        }
        guiLearnset(p);
        guiHuanLuyen(p);
    }

    static void guiLearnset(final Char p) {
        if (!coGoiMoi(p) || !batChieu()) {
            return;
        }
        try {
            final List<Learnset.Dong> ds = Learnset.cuaDong(p.charClass);
            final Message m = new Message(LENH);
            m.dos.writeByte(GOI_LEARNSET);
            m.dos.writeShort(p.pokeDex);
            m.dos.writeByte(p.charClass);
            m.dos.writeByte(Learnset.soO(p.charClass));
            m.dos.writeByte(Math.min(255, ds.size()));
            int k = 0;
            for (final Learnset.Dong r : ds) {
                if (k++ >= 255) {
                    break;
                }
                m.dos.writeByte(r.o);
                m.dos.writeShort(r.loai);
                m.dos.writeUTF(LoaiPokemon.ten(r.loai));
                m.dos.writeUTF(r.ten);
                m.dos.writeByte(r.he);
                m.dos.writeByte(r.nguon);
                m.dos.writeByte(Math.min(127, r.capHoc));
                m.dos.writeInt(r.giaXu);
            }
            p.sendMessage(m);
            m.cleanup();
        } catch (final Throwable t) {
            System.out.println("[HOC CHIEU] LOI gui learnset: " + t);
        }
    }

    static void guiHuanLuyen(final Char p) {
        if (!coGoiMoi(p)) {
            return;
        }
        try {
            final TrangThai t = TT.get(p.charDBID);
            final boolean bat = t != null && batHuanLuyen();
            final Message m = new Message(LENH);
            m.dos.writeByte(GOI_HUAN_LUYEN);
            m.dos.writeByte(bat ? 1 : 0);
            if (bat) {
                final int tong = tongDiem(p);
                m.dos.writeShort(tong);
                m.dos.writeShort(HuanLuyen.daDung(t.phanBo));
                m.dos.writeByte(HuanLuyen.soDong());
                for (int i = 0; i < HuanLuyen.soDong(); i++) {
                    final HuanLuyen.Dong d = HuanLuyen.dong(i);
                    m.dos.writeByte(d.id);
                    m.dos.writeUTF(d.ten);
                    m.dos.writeUTF(d.moTa);
                    m.dos.writeShort(t.phanBo[i]);
                    m.dos.writeShort(d.tran);
                    m.dos.writeShort(d.nguong);
                    m.dos.writeShort(d.pn1);
                    m.dos.writeShort(d.pn2);
                }
                final long bay = System.currentTimeMillis();
                m.dos.writeLong(HuanLuyen.giaTay(t.lanTay, ThongSoLauDai.xuMoiGio(Math.max(1, p.lvDetail.lv)), giaTayPhanNghin()));
                m.dos.writeInt((int) Math.min(Integer.MAX_VALUE, HuanLuyen.choTay(t.lanTay, t.tayLuc, bay, hoiTayGio()) / 1000L));
                m.dos.writeByte(t.lanTay <= 0 ? 1 : 0);
                m.dos.writeByte(diemMoiCap());
                m.dos.writeByte(Math.min(soHuyHieu(p), tranThuong()));
                m.dos.writeByte(tranThuong());
            }
            p.sendMessage(m);
            m.cleanup();
        } catch (final Throwable e) {
            System.out.println("[HOC CHIEU] LOI gui huan luyen: " + e);
        }
    }
}
