package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * THE DAT TEN POKEMON (yeu cau chu game 19/10/2026: "tren dau pet khong co ten ... ban the dat ten de nhan chi so ngau
 * nhien sieu thap vai % co kiem soat de khong lam phat"). Lop THUAN (khong CSDL, khong Char) - test/KiemDatTenPet.java.
 * Phan noi vao game: real.DatTenPetGame. Thiet ke: docs/thiet-ke-game.md muc 4.8.
 *
 * <h3>Luat</h3>
 * <ul>
 *   <li>Dung 1 the -&gt; chon 1 pet -&gt; nhap ten (2..12 ky tu chu Latin co dau tieng Viet, so, dau cach; loc tu tuc)
 *   -&gt; pet nhan ten + DUNG MOT dong chi so quay tu bang {@code data_dong_the_ten} (SQL) x muc gia tri (cau hinh,
 *   trong so nghieng ve muc thap).</li>
 *   <li>Dat ten lai = quay lai dong (ho hut), dong moi THAY dong cu, khong bao gio cong don ({@link #datLai}).</li>
 *   <li>Moi pet toi da 1 dong; chi pet DANG DI THEO (Char.petUsing) duoc tinh.</li>
 *   <li>Dong nam trong ngan sach o "the ten pet" ({@link #K_TRAN_DONG}, co dinh) va trong TRAN TONG TRANG TRI
 *   ({@link #K_TRAN_TRANG_TRI} = 5%, thiet_ke_can_bang 4.5) - {@link #hieuLuc}. HP / tan cong / phong thu cong vao
 *   truoc ChiSoTrangBiGame.kepPhanTram nen con nam trong chi_so.tran_phan_tram_cong; chi mang / ne qua GioiHanChiSo.</li>
 *   <li>Ban tra phi va ban kiem duoc (doi Tem nghien cuu / luong khoa) la CUNG mot vat pham, cung ngan sach.
 *   Pet da dat ten bi KHOA (khong giao dich) - khoa khi mac.</li>
 * </ul>
 *
 * <h3>Goi mang</h3>
 * <pre>
 * -94 POKE_PET (mo rong, chi THEM o cuoi; client cu doc 5 byte dau roi bo qua phan thua):
 *   i16 charId, i16 dexPet, u8 shiny, [u8 phienBan = 1, UTF tenHien, u8 coTenRieng]
 *   tenHien = ten rieng (the ten) hoac ten loai; client ve nhan "tenHien · tenChu" tren dau pet.
 * -106 DAT_TEN_KET_QUA (S-&gt;C, chi client so ban &gt;= dat_ten_pet.ban_client_popup):
 *   u8 kieu (0 thanh cong, 1 loi), UTF tieuDe, UTF noiDung -&gt; hop thoai the trang kieu Pokemon GO.
 *   Client cu / Unity: hop thoai 37 nhu moi thong bao server.
 * </pre>
 */
public final class DatTenPet {

    private DatTenPet() {
    }

    // ================================================================== goi mang

    public static final int LENH_KET_QUA = -106;
    /** Phien ban phan mo rong cua -94. */
    public static final int PHIEN_BAN_TEN = 1;
    public static final byte KQ_OK = 0, KQ_LOI = 1;

    /** Phan mo rong cuoi -94 (sau u8 shiny). */
    public static void ghiTenPet(final DataOutputStream dos, final String tenHien, final boolean coTenRieng) throws IOException {
        dos.writeByte(PHIEN_BAN_TEN);
        dos.writeUTF(tenHien == null ? "" : tenHien);
        dos.writeByte(coTenRieng ? 1 : 0);
    }

    /** Than goi -106. */
    public static void ghiKetQua(final DataOutputStream dos, final byte kieu, final String tieuDe, final String noiDung)
            throws IOException {
        dos.writeByte(kieu);
        dos.writeUTF(tieuDe == null ? "" : tieuDe);
        dos.writeUTF(noiDung == null ? "" : noiDung);
    }

    // ================================================================== cau hinh (tools/cauhinh/danh_muc.json)

    /** Chinh (tunable): bat/tat dung the. Dong da co van tinh (da tra tien, van trong tran). */
    public static final String K_BAT = "dat_ten_pet.bat";
    /** Chinh: gia ban the (luong) trong Cua hang luong - nguon gia DUY NHAT cua mon trao dat_ten_pet. */
    public static final String K_GIA_LUONG = "dat_ten_pet.gia_luong";
    /** Chinh: so the mua bang luong toi da / tuan / nhan vat (0 = tam ngung ban). */
    public static final String K_MUA_TUAN = "dat_ten_pet.mua_tuan_toi_da";
    /** Chinh: cac muc gia tri (phan nghin) cua dong, tang dan. */
    public static final String K_MUC = "dat_ten_pet.muc_phan_nghin";
    /** Chinh: trong so tung muc (cung so phan tu, KHONG tang - nghieng ve muc thap). */
    public static final String K_TRONG_SO = "dat_ten_pet.trong_so_muc";
    /** Co dinh (fixed): tran cung MOT dong = ngan sach o the ten pet (phan nghin). */
    public static final String K_TRAN_DONG = "dat_ten_pet.tran_dong_phan_nghin";
    /** Co dinh: tran tong moi nguon trang tri (phan nghin) - thiet_ke_can_bang 4.5 "trang_tri.tran_tong_phan_nghin". */
    public static final String K_TRAN_TRANG_TRI = "trang_tri.tran_tong_phan_nghin";
    /** Co dinh: do dai ten. */
    public static final String K_DAI_MIN = "dat_ten_pet.do_dai_toi_thieu";
    public static final String K_DAI_MAX = "dat_ten_pet.do_dai_toi_da";
    /** Co dinh: so ban client dau tien biet goi -106. */
    public static final String K_BAN_POPUP = "dat_ten_pet.ban_client_popup";

    public static final int GIA_LUONG_MAC_DINH = 100;
    public static final int MUA_TUAN_MAC_DINH = 3;
    public static final int[] MUC_MAC_DINH = {10, 15, 20, 30};
    public static final int[] TRONG_SO_MAC_DINH = {55, 30, 12, 3};
    public static final int TRAN_DONG_MAC_DINH = 30;
    public static final int TRAN_TRANG_TRI_MAC_DINH = 50;
    public static final int DAI_MIN_MAC_DINH = 2;
    public static final int DAI_MAX_MAC_DINH = 12;
    public static final int BAN_POPUP_MAC_DINH = 908; // 0.9.8: ban 906 / 907 da phat hanh khong co bo doc -106

    // ================================================================== chi so ho tro

    public static final String HP = "hp";
    public static final String TAN_CONG = "tan_cong";
    public static final String PHONG_THU = "phong_thu";
    public static final String CHI_MANG = "chi_mang";
    public static final String NE_TRANH = "ne_tranh";
    /** Ma chi so game da noi vao cong thuc (CharChiSo / ChiSoTrangBiGame / Char.haveNeTranh). Dong SQL ma khac bi bo. */
    public static final String[] MA_HOP_LE = {HP, TAN_CONG, PHONG_THU, CHI_MANG, NE_TRANH};
    static final String[] TEN_CHI_SO = {"HP", "Tấn công", "Phòng thủ", "Chí mạng", "Né tránh"};

    public static boolean maHopLe(final String ma) {
        return viTri(ma) >= 0;
    }

    static int viTri(final String ma) {
        for (int i = 0; i < MA_HOP_LE.length; i++) {
            if (MA_HOP_LE[i].equals(ma)) {
                return i;
            }
        }
        return -1;
    }

    public static String tenChiSo(final String ma) {
        final int i = viTri(ma);
        return i >= 0 ? TEN_CHI_SO[i] : "";
    }

    /** "+1,5% HP" (phan nghin -&gt; phan tram kieu Viet). */
    public static String moTa(final String ma, final int phanNghin) {
        if (!maHopLe(ma) || phanNghin <= 0) {
            return "";
        }
        return "+" + phanTram(phanNghin) + "% " + tenChiSo(ma);
    }

    static String phanTram(final int phanNghin) {
        final int nguyen = phanNghin / 10;
        final int le = phanNghin % 10;
        return le == 0 ? String.valueOf(nguyen) : nguyen + "," + le;
    }

    // ================================================================== bang dong (SQL) + muc (cau hinh)

    /** Mot dong cua data_dong_the_ten: chi so, trong so chon chi so, he so % ap len muc gia tri. */
    public static final class Dong {
        public final int id;
        public final String ma;
        public final int trongSo;
        public final int heSo;

        public Dong(final int id, final String ma, final int trongSo, final int heSo) {
            this.id = id;
            this.ma = ma;
            this.trongSo = trongSo;
            this.heSo = heSo;
        }
    }

    /** Chi giu dong hop le (ma da noi vao cong thuc, trong so &gt; 0, he so 1..100). Bang rong = khong dat ten duoc. */
    public static List<Dong> locBang(final List<Dong> tho) {
        final List<Dong> ra = new ArrayList<Dong>();
        if (tho == null) {
            return ra;
        }
        for (final Dong d : tho) {
            if (d != null && maHopLe(d.ma) && d.trongSo > 0 && d.heSo >= 1 && d.heSo <= 100) {
                ra.add(d);
            }
        }
        return Collections.unmodifiableList(ra);
    }

    /**
     * Muc + trong so tu cau hinh co hop le khong: cung do dai 1..10, muc TANG NGHIEM trong 1..tranDong, trong so &gt; 0
     * va KHONG TANG (muc cao khong bao gio de ra hon muc thap). Sai -&gt; game dung mac dinh.
     */
    public static boolean mucHopLe(final int[] muc, final int[] trongSo, final int tranDong) {
        if (muc == null || trongSo == null || muc.length == 0 || muc.length != trongSo.length || muc.length > 10) {
            return false;
        }
        for (int i = 0; i < muc.length; i++) {
            if (muc[i] < 1 || muc[i] > tranDong || trongSo[i] <= 0) {
                return false;
            }
            if (i > 0 && (muc[i] <= muc[i - 1] || trongSo[i] > trongSo[i - 1])) {
                return false;
            }
        }
        return true;
    }

    /** Ket qua mot lan quay. */
    public static final class KetQua {
        public final String ma;
        public final int phanNghin;
        public final int muc;

        KetQua(final String ma, final int phanNghin, final int muc) {
            this.ma = ma;
            this.phanNghin = phanNghin;
            this.muc = muc;
        }
    }

    static int chon(final int[] trongSo, final Random r) {
        long tong = 0L;
        for (final int w : trongSo) {
            tong += Math.max(0, w);
        }
        if (tong <= 0L) {
            return 0;
        }
        long x = (long) (r.nextDouble() * tong);
        for (int i = 0; i < trongSo.length; i++) {
            x -= Math.max(0, trongSo[i]);
            if (x < 0L) {
                return i;
            }
        }
        return trongSo.length - 1;
    }

    /**
     * Quay MOT dong: chi so theo trong so bang, muc theo trong so cau hinh, gia tri = muc x heSo / 100 (&gt;= 1), kep
     * {@code tranDong}. null neu bang rong. Muc / trong so sai -&gt; dung mac dinh.
     */
    public static KetQua quay(final List<Dong> bang, final int[] muc, final int[] trongSo, final int tranDong, final Random r) {
        if (bang == null || bang.isEmpty() || tranDong <= 0) {
            return null;
        }
        int[] m = muc;
        int[] w = trongSo;
        if (!mucHopLe(m, w, tranDong)) {
            m = MUC_MAC_DINH;
            w = TRONG_SO_MAC_DINH;
        }
        final int[] ws = new int[bang.size()];
        for (int i = 0; i < ws.length; i++) {
            ws[i] = bang.get(i).trongSo;
        }
        final Dong d = bang.get(chon(ws, r));
        final int k = chon(w, r);
        int v = (int) Math.max(1L, (long) Math.min(m[k], tranDong) * d.heSo / 100L);
        v = Math.min(v, tranDong);
        return new KetQua(d.ma, v, k);
    }

    /**
     * Phan nghin THAT dong duoc tinh: min(dong, tran o, tran tong trang tri - trang tri khac), &gt;= 0. Cac o trang tri
     * khac (thiet_ke_can_bang 4.5) chiem truoc; the ten chi lap phan con trong.
     */
    public static int hieuLuc(final int phanNghin, final int tranDong, final int tranTrangTri, final int trangTriKhac) {
        final long con = (long) Math.max(0, tranTrangTri) - Math.max(0, trangTriKhac);
        final long v = Math.min(Math.min((long) Math.max(0, phanNghin), (long) Math.max(0, tranDong)), con);
        return (int) Math.max(0L, v);
    }

    // ================================================================== trang thai pet (khong cong don)

    /** Ten + dong cua MOT pet. null ten = chua dat. */
    public static final class TrangThai {
        public final String ten;
        public final String ma;
        public final int phanNghin;
        public final int soLan;

        public TrangThai(final String ten, final String ma, final int phanNghin, final int soLan) {
            this.ten = ten;
            this.ma = ma;
            this.phanNghin = phanNghin;
            this.soLan = soLan;
        }
    }

    /** Dat ten (lai): dong moi THAY dong cu - so lan tang, gia tri khong cong don. */
    public static TrangThai datLai(final TrangThai cu, final String tenMoi, final KetQua moi) {
        return new TrangThai(tenMoi, moi.ma, moi.phanNghin, (cu == null ? 0 : cu.soLan) + 1);
    }

    /** Phan nghin chi so {@code ma} cua trang thai (0 neu dong la chi so khac / chua dat) sau tran. */
    public static int phanNghin(final TrangThai t, final String ma, final int tranDong, final int tranTrangTri,
            final int trangTriKhac) {
        if (t == null || t.ma == null || !t.ma.equals(ma)) {
            return 0;
        }
        return hieuLuc(t.phanNghin, tranDong, tranTrangTri, trangTriKhac);
    }

    // ================================================================== ten

    /** NFC, bo dau cach dau/cuoi, gop dau cach lien tiep. */
    public static String chuanHoa(final String tho) {
        if (tho == null) {
            return "";
        }
        final String s = Normalizer.normalize(tho, Normalizer.Form.NFC).replace(' ', ' ');
        return s.trim().replaceAll("\\s+", " ");
    }

    /** So ky tu (code point). */
    public static int doDai(final String s) {
        return s == null ? 0 : s.codePointCount(0, s.length());
    }

    /** Mot tu cam: kieu "tu" = ca tu (co dau, khong phan biet hoa thuong); "chuoi" = chuoi con cua ban "khung". */
    public static final class TuCam {
        public final String tu;
        public final boolean caTu;

        public TuCam(final String tu, final String kieu) {
            this.tu = tu == null ? "" : tu.trim().toLowerCase(Locale.ROOT);
            this.caTu = !"chuoi".equals(kieu);
        }
    }

    /**
     * Ban "khung" de bat lach luat: chu thuong, bo dau (đ -&gt; d), so giong chu (0 o, 1 i, 3 e, 4 a, 5 s, 7 t, @ a,
     * $ s), bo moi ky tu khong phai chu / so (ke ca dau cach: "d m m" -&gt; "dmm").
     */
    public static String khung(final String s) {
        if (s == null) {
            return "";
        }
        final String d = Normalizer.normalize(s.toLowerCase(Locale.ROOT).replace('đ', 'd'), Normalizer.Form.NFD);
        final StringBuilder b = new StringBuilder(d.length());
        for (int i = 0; i < d.length(); i++) {
            char c = d.charAt(i);
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                continue;
            }
            switch (c) {
                case '0':
                    c = 'o';
                    break;
                case '1':
                case '!':
                    c = 'i';
                    break;
                case '3':
                    c = 'e';
                    break;
                case '4':
                case '@':
                    c = 'a';
                    break;
                case '5':
                case '$':
                    c = 's';
                    break;
                case '7':
                    c = 't';
                    break;
                default:
                    break;
            }
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                b.append(c);
            }
        }
        return b.toString();
    }

    static boolean coCaTu(final String s, final String tu) {
        if (tu.isEmpty()) {
            return false;
        }
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

    /** true neu ten chua tu cam (khong noi ra tu nao - tranh nguoi choi do danh sach). */
    public static boolean coTuCam(final String ten, final List<TuCam> ds) {
        if (ds == null || ten == null) {
            return false;
        }
        final String thuong = Normalizer.normalize(ten, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        final String k = khung(ten);
        for (final TuCam t : ds) {
            if (t == null || t.tu.isEmpty()) {
                continue;
            }
            if (t.caTu) {
                if (coCaTu(thuong, Normalizer.normalize(t.tu, Normalizer.Form.NFC))) {
                    return true;
                }
            } else {
                final String tk = khung(t.tu);
                if (!tk.isEmpty() && k.contains(tk)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Ky tu duoc phep: chu Latin (gom chu co dau tieng Viet), so 0-9, dau cach. */
    static boolean kyTuHopLe(final int cp) {
        if (cp == ' ') {
            return true;
        }
        if (cp >= '0' && cp <= '9') {
            return true;
        }
        return Character.isLetter(cp) && Character.UnicodeScript.of(cp) == Character.UnicodeScript.LATIN;
    }

    /**
     * Kiem ten (da {@link #chuanHoa}). null = hop le, chuoi = ly do cho nguoi choi. Khong can duy nhat (chu game: khong
     * bat trung).
     */
    public static String kiemTen(final String ten, final int daiMin, final int daiMax, final List<TuCam> tuCam) {
        final String s = ten == null ? "" : ten;
        final int n = doDai(s);
        if (n < daiMin || n > daiMax) {
            return "Tên phải từ " + daiMin + " đến " + daiMax + " ký tự.";
        }
        int chu = 0;
        for (int i = 0; i < s.length(); ) {
            final int cp = s.codePointAt(i);
            if (!kyTuHopLe(cp)) {
                return "Tên chỉ gồm chữ (có dấu), số và dấu cách.";
            }
            if (Character.isLetter(cp)) {
                chu++;
            }
            i += Character.charCount(cp);
        }
        if (chu == 0) {
            return "Tên phải có ít nhất một chữ cái.";
        }
        if (coTuCam(s, tuCam)) {
            return "Tên có từ không phù hợp, hãy chọn tên khác.";
        }
        return null;
    }

    // ================================================================== nhat ky kinh te

    /** Cot cua tob_log_dat_ten theo dung thu tu INSERT trong real.DatTenPetGame. */
    public static final String[] COT_NHAT_KY = {"id_char", "charname", "id_pet", "dex", "ten_cu", "dong_cu", "ten_moi",
        "dong_moi", "id_item", "ket_qua"};

    /** Mot dong nhat ky (gia tri theo COT_NHAT_KY). dong = "ma:phan_nghin" hoac "". */
    public static Object[] dongNhatKy(final int idChar, final String charname, final int idPet, final int dex,
            final TrangThai cu, final TrangThai moi, final int idItem, final String ketQua) {
        return new Object[]{Integer.valueOf(idChar), charname == null ? "" : charname, Integer.valueOf(idPet),
            Integer.valueOf(dex), cu == null || cu.ten == null ? "" : cu.ten, maDong(cu), moi == null || moi.ten == null ? "" : moi.ten,
            maDong(moi), Integer.valueOf(idItem), ketQua == null ? "" : ketQua};
    }

    static String maDong(final TrangThai t) {
        return t == null || t.ma == null ? "" : t.ma + ":" + t.phanNghin;
    }
}
