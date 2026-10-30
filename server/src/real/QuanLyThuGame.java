package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.OThu;
import com.htbb.pokemon.PetPokemon;
import data.Animal;
import data.Database;
import data.Pet;
import io.Message;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quan ly Thu (thu cuoi + Pokemon di theo): tha ve tu nhien lay it xu, khoa (bao ve con yeu thich), O THU co gioi han
 * + "Vé mở ô thú" (+1 o). Luat thuan: com.htbb.pokemon.OThu. Luu: Char.oThuMo / Char.thuKhoa -&gt; tob_char.o_thu_mo /
 * thu_khoa (Database.luuCotQuanLyThu, ghi ngay moi lan doi).
 *
 * <pre>
 * GIAO THUC (23/10/2026, client &gt;= {@link #BAN_CLIENT}; client cu khong gui -119 va khong doc phan noi them):
 *
 * C-&gt;S  -119 LENH: byte kieu
 *          0 TRANG_THAI  server tra goi 16 loai 2 (co phan noi them ben duoi)
 *          1 THA         byte n, n x (byte loai 0 thu cuoi / 1 pet, short id trong goi 16) - tha nhieu con mot lan
 *          2 KHOA        byte loai, short id, byte khoa (1 khoa / 0 mo)
 *          3 DUNG_VE     dung 1 Ve mo o thu (uu tien ve khoa)
 *          4 MUA_VE      mua 1 ve o Cua hang luong (DUNG duong mua cua hang: NapTienGame.muaTheoMa - so cai, gioi han, hoan)
 *        Tra loi: -104 (DungVatPham.bao / loi) + goi 16 loai 0 (xu) + loai 2 (danh sach + trang thai moi).
 *
 * S-&gt;C  goi 16 loai 2 NOI THEM sau "int luongKhoa" (chi client &gt;= BAN_CLIENT; client cu bo qua byte thua):
 *          byte  phien (1)
 *          short soThu      so thu dang so huu (ca con dang cuoi / di theo)
 *          short sucChua    o hien co (co ban + da mo, &lt;= toi da)
 *          short toiDa      o toi da mo bang ve
 *          short soVe       ve dang giu (thuong + khoa)
 *          short idVe       id nguyen lieu cua ve (-1 = may chu chua co)
 *          int   giaVe      gia luong 1 ve o Cua hang luong (-1 = khong ban / cua hang tat)
 *          int   xuConNgay  xu tha con nhan duoc hom nay (tran thu.tha_xu_ngay; 0 = da dat tran, tha van duoc nhung 0 xu)
 *          byte  n          = so dong danh sach o tren (thu cuoi place != 1, roi pet tru con dang di theo), CUNG thu tu:
 *            byte  co       bit0 da khoa, bit1 khong tha duoc (dang dung / dang giao dich / cho tang)
 *            int   loai     khoa loai (pet: so Pokedex; thu cuoi: 100000 + idImg) - nhom "ban trung"
 *            short cap      cap so sanh (pet: cap pet x 1000 + cap; thu cuoi: level)
 *            int   xu       xu nhan duoc khi tha (0 = thu co han)
 *            UTF   lyDo     vi sao khong tha duoc ("" neu tha duoc)
 *          (client &gt;= {@link #BAN_DANG_DUNG}) khoi DANG DUNG: thu dang cuoi + pet dang di theo - {@link #ghiDangDung}
 * </pre>
 *
 * Day o: moi duong NHAN thu (mua, no trung, su kien, giftcode, duoc tang / giao dich) goi {@link #chanNhan}: tu choi
 * kem thong bao ro (khong co hop thu / kho thu nao de chuyen vao). Duong tra tien truoc kiem truoc khi tru tien.
 */
public final class QuanLyThuGame {

    public static final int LENH = -119;
    public static final byte KIEU_TRANG_THAI = 0, KIEU_THA = 1, KIEU_KHOA = 2, KIEU_DUNG_VE = 3, KIEU_MUA_VE = 4;
    /** Ban client dau tien co man quan ly thu (doc phan noi them goi 16, gui -119). */
    public static final int BAN_CLIENT = 912; // 0.9.12: ban 911 (0.9.11) phat hanh KHONG co bo doc phan noi them
    /** Ban client doc khoi DANG DUNG (thu dang cuoi / pet dang di theo) sau phan noi them - xem {@link #ghiDangDung}. */
    public static final int BAN_DANG_DUNG = 913;
    public static final byte PHIEN = 1;
    /** Id mau nguyen lieu (Map.gemTemplate) cua Ve mo o thu - noi sau ve mo rong hanh trang (270). */
    public static final short ID_VE = 271;
    /** Anh 6500 + 96 = o icon 123 (the do, res/pk_icon.png) - IconPokemonBang.NGOC[96]. */
    public static final int ANH_VE = 96;
    public static final String TEN_VE = "Vé mở ô thú";
    /** So con toi da mot lan tha. */
    public static final int THA_TOI_DA_MOT_LAN = 120;

    public static final byte CO_KHOA = 1, CO_KHONG_THA = 2;

    private QuanLyThuGame() {
    }

    // ================================================================== cau hinh

    public static int coBan() {
        return OThu.coBan((int) CauHinhTrungTam.so(OThu.K_O_CO_BAN, OThu.O_CO_BAN));
    }

    public static int toiDa() {
        return OThu.toiDa(coBan(), (int) CauHinhTrungTam.so(OThu.K_O_TOI_DA, OThu.O_TOI_DA));
    }

    static int so(final String k, final int macDinh, final int min, final int max) {
        return (int) Math.max(min, Math.min(max, CauHinhTrungTam.so(k, macDinh)));
    }

    // ================================================================== dem o

    public static int soThu(final Char p) {
        return (p.animal == null ? 0 : p.animal.size()) + (p.pet == null ? 0 : p.pet.size());
    }

    public static int sucChua(final Char p) {
        return OThu.sucChua(coBan(), toiDa(), p.oThuMo);
    }

    public static boolean day(final Char p) {
        return OThu.day(soThu(p), sucChua(p));
    }

    public static String cauDay(final Char p) {
        return "Ô thú đã đầy (" + soThu(p) + "/" + sucChua(p) + "). Thả bớt thú hoặc dùng " + TEN_VE
                + " để nhận thêm.";
    }

    /**
     * Duong NHAN thu goi truoc khi tao / nhan (va truoc khi tru tien neu co): day -&gt; bao, ghi log, tra true (KHONG
     * nhan). p null -&gt; false.
     */
    public static boolean chanNhan(final Char p, final String nguon) {
        if (p == null || !day(p)) {
            return false;
        }
        DungVatPham.loi(p, cauDay(p));
        // bao cao van hanh (vps/theo-doi): saveOrtherLog "THUDAY" bi LocLogVatPham bo -> dem qua nhat ky (Nhat gop dong lap).
        com.htbb.log.Nhat.thongTin("THUDAY", p.charname + " day " + soThu(p) + "/" + sucChua(p) + ": " + nguon);
        if (Database.instance != null && p.charDBID > 0) {
            try {
                Database.instance.saveOrtherLog("", p.charname, "tu choi nhan thu (day o " + soThu(p) + "/" + sucChua(p)
                        + "): " + nguon, "THUDAY");
            } catch (final Exception ignored) {
            }
        }
        return true;
    }

    // ================================================================== khoa

    static String khoaCua(final Object o) {
        if (o instanceof Animal) {
            final int id = ((Animal) o).dbid;
            return id > 0 ? "a" + id : null;
        }
        if (o instanceof Pet) {
            final int id = ((Pet) o).idDB;
            return id > 0 ? "p" + id : null;
        }
        return null;
    }

    /**
     * Da khoa: khoa tay ("a/p&lt;id&gt;"), hoac pet co ten tra phi (DatTenPetGame) chua bi mo khoa tay ("-p&lt;id&gt;" =
     * nguoi choi da mo khoa con co ten).
     */
    public static boolean daKhoa(final Char p, final Object o) {
        final String k = khoaCua(o);
        if (k == null) {
            return false;
        }
        if (p.thuKhoa.contains(k)) {
            return true;
        }
        return o instanceof Pet && DatTenPetGame.daDatTen((Pet) o) && !p.thuKhoa.contains("-" + k);
    }

    /** Vua mua ten cho pet: khoa (bo dau mo khoa cu), luu ngay. */
    public static void khoaKhiDatTen(final Char p, final Pet pet) {
        final String k = khoaCua(pet);
        if (p == null || k == null) {
            return;
        }
        p.thuKhoa.remove("-" + k);
        p.thuKhoa.add(k);
        Database.luuCotQuanLyThu(null, p);
    }

    public static void docKhoa(final Char p, final String s) {
        p.thuKhoa.clear();
        if (s == null) {
            return;
        }
        for (final String t : s.split(",")) {
            final String k = t.trim();
            if (k.matches("-?[ap][1-9][0-9]{0,9}")) {
                p.thuKhoa.add(k);
            }
        }
    }

    /** Chuoi luu: chi khoa cua con con so huu (bo khoa mo coi), toi da 1024 ky tu. */
    public static String ghiKhoa(final Char p) {
        final StringBuilder b = new StringBuilder();
        synchronized (p.thuKhoa) {
            for (final String k : p.thuKhoa) {
                if (!conSoHuu(p, k.startsWith("-") ? k.substring(1) : k)) {
                    continue;
                }
                if (b.length() + k.length() + 1 > 1024) {
                    break;
                }
                if (b.length() > 0) {
                    b.append(',');
                }
                b.append(k);
            }
        }
        return b.toString();
    }

    static boolean conSoHuu(final Char p, final String k) {
        if (p.animal != null) {
            for (final Animal a : new ArrayList<Animal>(p.animal)) {
                if (k.equals(khoaCua(a))) {
                    return true;
                }
            }
        }
        if (p.pet != null) {
            for (final Pet a : new ArrayList<Pet>(p.pet)) {
                if (k.equals(khoaCua(a))) {
                    return true;
                }
            }
        }
        return false;
    }

    // ================================================================== thong tin tung con

    static boolean coHan(final Object o) {
        if (o instanceof Animal) {
            return ((Animal) o).texpire > 0L;
        }
        final Pet t = (Pet) o;
        return t.totalMinute > 0 || t.ispetthoihan;
    }

    /** Khoa loai cho "ban trung". */
    public static int loai(final Object o) {
        if (o instanceof Animal) {
            return 100000 + ((Animal) o).idImg;
        }
        final Pet t = (Pet) o;
        if (t.itemPet != null && t.itemPet.getTemplate() != null) {
            final int dex = PetPokemon.dex(t.itemPet.getTemplate().id);
            return dex > 0 ? dex : 200000 + t.itemPet.getTemplate().id;
        }
        return 300000 + t.idTemplate;
    }

    /** Cap so sanh (giu con cao nhat khi tha ban trung). */
    public static int capSoSanh(final Object o) {
        if (o instanceof Animal) {
            return Math.max(0, ((Animal) o).level);
        }
        final Pet t = (Pet) o;
        return Math.min(Short.MAX_VALUE, t.capPet() * 1000 + Math.max(0, Math.min(999, t.lvDetail == null ? 0 : t.lvDetail.lv)));
    }

    /** Xu nhan duoc khi tha (OThu.xuTha theo cau hinh). */
    public static long xuTha(final Object o) {
        final int cap;
        final int hiem;
        if (o instanceof Animal) {
            final Animal a = (Animal) o;
            cap = Math.max(1, a.level + 1);
            hiem = a.idImg >= Animal.PHUONG_HOANG && !a.isPoro() ? 3 : 1;
        } else {
            final Pet t = (Pet) o;
            cap = Math.max(1, t.lvDetail == null ? 1 : t.lvDetail.lv);
            hiem = Math.max(1, t.capPet());
        }
        return OThu.xuTha(cap, hiem, coHan(o), so(OThu.K_THA_XU_CO_BAN, OThu.THA_XU_CO_BAN, 0, 1000),
                so(OThu.K_THA_XU_MOI_CAP, OThu.THA_XU_MOI_CAP, 0, 100), so(OThu.K_THA_XU_TOI_DA, OThu.THA_XU_TOI_DA, 0, 5000));
    }

    public static int tranXuNgay() {
        return so(OThu.K_THA_XU_NGAY, OThu.THA_XU_NGAY, 0, 1000000);
    }

    /** Ngay lich (gio may chu) dang yyyymmdd. */
    public static int homNay() {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        return c.get(java.util.Calendar.YEAR) * 10000 + (c.get(java.util.Calendar.MONTH) + 1) * 100 + c.get(java.util.Calendar.DAY_OF_MONTH);
    }

    /** Xu tha con nhan duoc hom nay (tran thu.tha_xu_ngay; qua tran van tha duoc nhung 0 xu). */
    public static long xuConNgay(final Char p) {
        return OThu.xuConNgay(tranXuNgay(), p.thuXuNgay, p.thuNgay, homNay());
    }

    /** Ly do KHONG tha duoc (khong tinh khoa), null = tha duoc. */
    public static String lyDoDangDung(final Char p, final Object o) {
        if (o instanceof Animal) {
            final Animal a = (Animal) o;
            if (a.place == 1 || p.animalRide == a) {
                return "Đang cưỡi";
            }
            if (p.thuChoTang == a) {
                return "Đang chờ tặng";
            }
            return null;
        }
        final Pet t = (Pet) o;
        if (t.place == 1 || p.petUsing == t) {
            return "Đang đi theo";
        }
        if (p.nameTangPet != null && p.nameTangPet.length() > 0 && p.idPetTang == t.id) {
            return "Đang giao dịch";
        }
        return null;
    }

    public static String lyDoKhongTha(final Char p, final Object o) {
        final String l = lyDoDangDung(p, o);
        if (l != null) {
            return l;
        }
        return daKhoa(p, o) ? "Đã khoá" : null;
    }

    /** Nhu lyDoDangDung nhung bo qua "Đang giao dịch" cua chinh lan chao ban dang chot (popup 112). */
    public static String lyDoDangDungTruChao(final Char p, final Pet t) {
        if (t.place == 1 || p.petUsing == t) {
            return "Đang đi theo";
        }
        return p.pet.contains(t) ? null : "Không còn";
    }

    /**
     * BAN THU CHO NPC (-800 / -801, ban do offline - 29/10/2026, ma-tran-bot "Lo hong uu tien" #6): cung luat voi
     * Tha ({@link #lyDoKhongTha}: dang cuoi / di theo / cho tang / dang chao giao dich / da khoa) cong them: khong ban
     * khi dang trao doi (66). Truoc day hai nhanh ban chi kiem {@code place != 1}: ban duoc con DA KHOA va con dang
     * chao giao dich (popup 111/112) / dang cho tang (-32048).
     */
    public static String lyDoKhongBanNpc(final Char p, final Object o) {
        if (p.userTrade != null && p.userTrade.size() > 0) {
            return "Không thể bán thú khi đang trao đổi.";
        }
        return lyDoKhongTha(p, o);
    }

    /**
     * Rut {@code o} khoi danh sach thu cua {@code p} de ban cho NPC - kiem-va-rut duoi khoa nhan vat (chung khoa voi
     * {@link #tha}). @return null = da rut (noi goi xoa CSDL + ghi log), khac = ly do khong ban.
     */
    public static String rutDeBanNpc(final Char p, final Object o) {
        synchronized (p) {
            final String l = lyDoKhongBanNpc(p, o);
            if (l != null) {
                return l;
            }
            final boolean bo = o instanceof Animal ? p.animal.remove(o) : p.pet.remove(o);
            if (!bo) {
                return "Thú không còn trong danh sách.";
            }
            final String k = khoaCua(o);
            if (k != null) {
                p.thuKhoa.remove(k);
                p.thuKhoa.remove("-" + k);
            }
            return null;
        }
    }

    /** Pet co id {@code id} cua {@code p}, null neu khong co (KHONG tra con cuoi danh sach nhu vong lap cu). */
    public static Pet timPet(final Char p, final int id) {
        for (final Pet t : new ArrayList<Pet>(p.pet)) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }

    /** Pet {@code ban} dang chao ban cho {@code mua} (popup 111 -&gt; 112), null neu het chao / khong con. */
    public static Pet petDangChao(final Char ban, final Char mua) {
        if (ban == null || mua == null || ban.nameTangPet == null || !ban.nameTangPet.equalsIgnoreCase(mua.charname)) {
            return null;
        }
        return timPet(ban, ban.idPetTang);
    }

    static String ten(final Object o) {
        try {
            if (o instanceof Animal) {
                return com.htbb.pokemon.ChuPokemon.tenThuCuoi(((Animal) o).getName());
            }
            final String s = ((Pet) o).getName();
            final int k = s.indexOf('\n');
            return k >= 0 ? s.substring(0, k) : s;
        } catch (final Exception e) {
            return "Thú";
        }
    }

    static Object tim(final Char p, final int loai, final int id) {
        if (loai == 0) {
            for (final Animal a : new ArrayList<Animal>(p.animal)) {
                if (a.id == id) {
                    return a;
                }
            }
        } else {
            for (final Pet a : new ArrayList<Pet>(p.pet)) {
                if (a.id == id) {
                    return a;
                }
            }
        }
        return null;
    }

    // ================================================================== tha ve tu nhien

    /** Ket qua mot lan tha. */
    public static final class KetQua {
        public int soTha;
        public int boQua;
        public long xu;
        /** co con le ra co xu nhung bi tran xu tha / ngay cat */
        public boolean hetTranNgay;
        public String loi;
        public final List<String> ten = new ArrayList<String>();
    }

    /**
     * Tha cac con (loai[i], id[i]). Nguyen tu theo tung con duoi khoa nhan vat: con da bien mat (bam hai lan, dang
     * xu ly o goi truoc) / dang dung / khoa -&gt; bo qua, KHONG xu. Tra xu bang addXu thuong (XU_DEM: xu tao ra).
     */
    public static KetQua tha(final Char p, final byte[] loai, final short[] id) {
        final KetQua kq = new KetQua();
        synchronized (p) {
            if (p.userTrade != null && p.userTrade.size() > 0) {
                kq.loi = "Không thể thả thú khi đang trao đổi.";
                return kq;
            }
            for (int i = 0; i < loai.length && i < id.length && i < THA_TOI_DA_MOT_LAN; i++) {
                final Object o = tim(p, loai[i], id[i]);
                if (o == null || lyDoKhongTha(p, o) != null) {
                    kq.boQua++;
                    continue;
                }
                final boolean bo = o instanceof Animal ? p.animal.remove(o) : p.pet.remove(o);
                if (!bo) {
                    kq.boQua++;
                    continue;
                }
                final long xuGoc = xuTha(o);
                final long xu = Math.min(xuGoc, xuConNgay(p));
                if (xu < xuGoc) {
                    kq.hetTranNgay = true;
                }
                final String k = khoaCua(o);
                if (k != null) {
                    p.thuKhoa.remove(k);
                    p.thuKhoa.remove("-" + k);
                }
                if (xu > 0) {
                    final int ngay = homNay();
                    if (p.thuNgay != ngay) {
                        p.thuNgay = ngay;
                        p.thuXuNgay = 0;
                    }
                    p.thuXuNgay = (int) Math.min(Integer.MAX_VALUE, (long) p.thuXuNgay + xu);
                    p.addXu(xu, "tha thu", Char.XU_DEM);
                }
                kq.soTha++;
                kq.xu += xu;
                kq.ten.add(ten(o));
                if (Database.instance != null && p.charDBID > 0) {
                    try {
                        if (o instanceof Animal) {
                            if (((Animal) o).dbid > 0) {
                                Database.instance.delAnimal(((Animal) o).dbid);
                            }
                        } else if (((Pet) o).idDB > 0) {
                            Database.instance.deletePet(((Pet) o).idDB);
                        }
                        Database.instance.saveOrtherLog("", p.charname, "tha thu " + (o instanceof Animal ? "cuoi " : "pet ")
                                + ten(o) + " (id " + (o instanceof Animal ? ((Animal) o).dbid : ((Pet) o).idDB) + ", cap "
                                + capSoSanh(o) + (coHan(o) ? ", co han" : "") + ") +" + xu + " xu, con " + soThu(p) + "/"
                                + sucChua(p) + ", xu " + p.getxu(), "THATHU");
                    } catch (final Exception e) {
                        System.out.println("[QUAN_LY_THU] loi ghi tha thu " + p.charname + ": " + e);
                    }
                }
            }
            if (kq.soTha > 0 && Database.instance != null && p.charDBID > 0) {
                try {
                    Database.luuCotQuanLyThu(null, p);
                    Database.instance.saveCharAuto(p);
                } catch (final Exception e) {
                    System.out.println("[QUAN_LY_THU] luu loi " + p.charname + ": " + e);
                }
            }
        }
        return kq;
    }

    public static String cauKetQua(final KetQua kq) {
        if (kq.loi != null) {
            return kq.loi;
        }
        if (kq.soTha == 0) {
            return kq.boQua > 0 ? "Không thả được: thú đã được thả, đang dùng hoặc đã khoá." : "Chưa chọn thú nào.";
        }
        final String ai = kq.soTha == 1 ? kq.ten.get(0) : kq.soTha + " thú";
        return "Đã thả " + ai + " về tự nhiên" + (kq.xu > 0 ? ", nhận " + HanhDongVatPham.xu(kq.xu) + " xu" : "") + "."
                + (kq.hetTranNgay ? " Đã đạt trần xu thả hôm nay (" + HanhDongVatPham.xu(tranXuNgay()) + " xu): thả tiếp không nhận xu." : "")
                + (kq.boQua > 0 ? " Bỏ qua " + kq.boQua + " con đang dùng / đã khoá." : "");
    }

    // ================================================================== khoa / mo khoa

    /** @return null neu thanh cong, ly do neu khong. */
    public static String doiKhoa(final Char p, final int loai, final int id, final boolean khoa) {
        synchronized (p) {
            final Object o = tim(p, loai, id);
            if (o == null) {
                return "Không tìm thấy thú.";
            }
            final String k = khoaCua(o);
            if (k == null) {
                return "Thú này chưa lưu xong, thử lại sau giây lát.";
            }
            if (khoa) {
                p.thuKhoa.remove("-" + k);
                p.thuKhoa.add(k);
            } else {
                p.thuKhoa.remove(k);
                if (o instanceof Pet && DatTenPetGame.daDatTen((Pet) o)) {
                    p.thuKhoa.add("-" + k); // pet co ten: nho la nguoi choi da mo khoa (khong tu khoa lai khi dang nhap)
                }
            }
            Database.luuCotQuanLyThu(null, p);
            return null;
        }
    }

    // ================================================================== ve mo o thu

    /** Mau nguyen lieu cua ve (MapNapTaiNguyen.loadGemTemplate). Kieu TU_BINH: moi client co nut "Dung" (-67). */
    public static GemTemplate taoMauVe() {
        return new GemTemplate(ID_VE, ANH_VE, TEN_VE, "Dùng: thêm " + OThu.O_MOI_VE
                + " ô thú (thú cưỡi + Pokémon đi theo), tối đa theo máy chủ.", OThu.O_MOI_VE, -1, 0, GemTemplate.TYPE_TU_BINH,
                Map.NOT_SELL, 0);
    }

    public static short idVe() {
        final GemTemplate[] g = Map.gemTemplate;
        return g != null && g.length > ID_VE && g[ID_VE] != null && g[ID_VE].id == ID_VE ? ID_VE : (short) -1;
    }

    public static boolean laVe(final int idGem) {
        return idGem == ID_VE && idVe() == ID_VE;
    }

    static boolean coMang(final int[] a) {
        return a != null && a.length > ID_VE;
    }

    static int soVeThuong(final Char p) {
        if (!coMang(p.listGemitem)) {
            return 0;
        }
        final int ban = coMang(p.listGemitemSell) ? p.listGemitemSell[ID_VE] : 0;
        return Math.max(0, p.listGemitem[ID_VE] - ban);
    }

    static int soVeKhoa(final Char p) {
        return coMang(p.listGemitemLock) ? Math.max(0, p.listGemitemLock[ID_VE]) : 0;
    }

    public static int soVe(final Char p) {
        return idVe() < 0 ? 0 : soVeThuong(p) + soVeKhoa(p);
    }

    /** Dung 1 ve. khoa null = tu chon (ve khoa truoc). @return null neu thanh cong, ly do neu khong (KHONG tru ve). */
    public static String dungVe(final Char p, final Boolean khoa) {
        synchronized (p) {
            if (idVe() < 0) {
                return "Máy chủ chưa có " + TEN_VE + ".";
            }
            final boolean dungKhoa = khoa == null ? soVeKhoa(p) > 0 : khoa.booleanValue();
            if ((dungKhoa ? soVeKhoa(p) : soVeThuong(p)) <= 0) {
                return "Bạn không có " + TEN_VE + ".";
            }
            if (OThu.oConMoDuoc(coBan(), toiDa(), p.oThuMo) <= 0) {
                return "Ô thú đã mở tối đa (" + toiDa() + " ô). Vé chưa bị dùng.";
            }
            p.delGem(ID_VE, 1, dungKhoa);
            p.oThuMo = (short) OThu.kepOMo(p.oThuMo + OThu.O_MOI_VE);
            try {
                p.sendMessage(MessageCreator.createCharGemItem(p));
            } catch (final Exception ignored) {
            }
            if (p.charDBID > 0 && Database.instance != null) {
                try {
                    Database.luuCotQuanLyThu(null, p);
                    Database.instance.saveCharAuto(p);
                    Database.instance.saveOrtherLog("tob_other_log", p.charname, "mo o thu +" + OThu.O_MOI_VE + " bang ve"
                            + (dungKhoa ? " khoa" : "") + " -> " + sucChua(p) + " o", "mothu");
                } catch (final Exception e) {
                    System.out.println("[QUAN_LY_THU] luu loi " + p.charname + ": " + e);
                }
            }
            return null;
        }
    }

    static String thanhCongVe(final Char p) {
        return "Ô thú: " + soThu(p) + "/" + sucChua(p) + " (tối đa " + toiDa() + ").";
    }

    static void dungVeTuGoiCu(final Char p, final boolean khoa) throws IOException {
        HanhDongVatPham.boc(p, HanhDongVatPham.MO_RONG, () -> {
            final String loi = dungVe(p, Boolean.valueOf(khoa));
            traLoi(p, loi == null ? null : loi, loi == null ? "Đã mở thêm ô thú. " + thanhCongVe(p) : null);
        });
    }

    /** Mua ve o cua hang luong duoc khong (null = duoc): khong ban ve thua (tinh ca ve dang giu). */
    public static String kiemMuaVe(final Char p) {
        if (idVe() < 0) {
            return "Máy chủ chưa có " + TEN_VE + ".";
        }
        if (!OThu.muaVeDuoc(coBan(), toiDa(), p.oThuMo, soVe(p))) {
            return soVe(p) > 0 ? "Số vé đang giữ đã đủ mở tới " + toiDa() + " ô thú, hãy dùng vé trước."
                    : "Ô thú đã mở tối đa (" + toiDa() + " ô).";
        }
        return null;
    }

    /** Trao n ve KHOA (khong giao dich) - cua hang luong. */
    public static void traoVe(final Char p, final int n) {
        if (idVe() < 0 || n <= 0) {
            throw new IllegalStateException("may chu chua co ve mo o thu");
        }
        p.doAddGemItem(ID_VE, n, true);
        try {
            p.sendMessage(MessageCreator.createCharGemItem(p));
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== goi tin

    public static boolean coManMoi(final Char p) {
        return BanClientNhanVat.ban(p) >= BAN_CLIENT;
    }

    /** Phan noi them cua goi 16 loai 2 (MessageCreator.createCharInventoryMessage) - chi client &gt;= BAN_CLIENT. */
    public static void ghiNoiThem(final DataOutputStream dos, final Char p, final List<Object> ds) throws IOException {
        dos.writeByte(PHIEN);
        dos.writeShort(Math.min(Short.MAX_VALUE, soThu(p)));
        dos.writeShort(sucChua(p));
        dos.writeShort(toiDa());
        dos.writeShort(Math.min(Short.MAX_VALUE, soVe(p)));
        dos.writeShort(idVe());
        dos.writeInt(p.charDBID > 0 && Database.instance != null
                ? com.htbb.pokemon.NapTienGame.giaLuongTheoMa(com.htbb.pokemon.NapTienLogic.MA_VE_MO_O_THU) : -1);
        dos.writeInt((int) Math.min(Integer.MAX_VALUE, xuConNgay(p)));
        dos.writeByte(ds.size());
        for (final Object o : ds) {
            ghiDong(dos, p, o);
        }
        if (BanClientNhanVat.ban(p) >= BAN_DANG_DUNG) {
            ghiDangDung(dos, p, ds);
        }
    }

    /** Mot dong quan ly (co, loai, cap, xu, ly do) - dung cho danh sach tui va khoi dang dung. */
    static void ghiDong(final DataOutputStream dos, final Char p, final Object o) throws IOException {
        final String ly = lyDoDangDung(p, o);
        final boolean khoa = daKhoa(p, o);
        dos.writeByte((khoa ? CO_KHOA : 0) | (ly != null || khoa ? CO_KHONG_THA : 0));
        dos.writeInt(loai(o));
        dos.writeShort(capSoSanh(o));
        dos.writeInt((int) Math.min(Integer.MAX_VALUE, xuTha(o)));
        dos.writeUTF(ly == null ? (khoa ? "Đã khoá" : "") : ly);
    }

    /**
     * Con DANG DUNG (khong nam trong danh sach tui cua goi 16): thu cuoi place == 1 / animalRide, pet dang di theo. Truoc
     * 0.9.13 client chi doan "dang cuoi" tu goi 15 + goi 3 + -93 va khong bao gio thay pet dang di theo o Trang bi ›
     * Thu cuoi -&gt; o "Ô thú: 5/20" ma danh sach chi 4 dong, o Thu cuoi trong. Cung danh sach {@link #soThu} dem.
     */
    public static List<Object> dangDung(final Char p, final List<Object> ds) {
        final List<Object> r = new ArrayList<Object>();
        if (p.animalRide != null && p.animal != null && p.animal.contains(p.animalRide) && !ds.contains(p.animalRide)) {
            r.add(p.animalRide);
        }
        if (p.animal != null) {
            for (final Animal a : new ArrayList<Animal>(p.animal)) {
                if (!ds.contains(a) && !r.contains(a)) {
                    r.add(a);
                }
            }
        }
        if (p.pet != null) {
            if (p.petUsing != null && p.pet.contains(p.petUsing) && !ds.contains(p.petUsing)) {
                r.add(p.petUsing);
            }
            for (final Pet t : new ArrayList<Pet>(p.pet)) {
                if (!ds.contains(t) && !r.contains(t)) {
                    r.add(t);
                }
            }
        }
        return r;
    }

    /** Thu cuoi cu (cuoi bang thuoc): dang cuoi nhung KHONG phai con trong o thu (animalRide null). */
    public static String tenThuCuoiCu(final Char p) {
        if (p.rideHorse <= 0 || p.animalRide != null) {
            return null;
        }
        return "Thú cưỡi";
    }

    /**
     * Khoi DANG DUNG noi sau n dong (client &gt;= {@link #BAN_DANG_DUNG}):
     * <pre>
     *   byte phien (1)
     *   byte m       so con dang dung (thu cuoi dang cuoi truoc, roi pet dang di theo)
     *   m x  byte loai 0 thu cuoi / 1 pet
     *        loai 0: short id, byte idImg, byte icon, byte level, UTF ten, UTF info, byte 1 dang cuoi / 0 khong
     *        loai 1: short id, byte idImg + 5, byte icon, UTF ten, byte type, UTF info, int phut con, byte 1 di theo / 0
     *        + mot dong quan ly (byte co, int loai, short cap, int xu, UTF ly do)
     *   byte coThuCu 1 = dang cuoi thu cuoi cu khong chiem o (thuoc) + UTF ten
     * </pre>
     */
    static void ghiDangDung(final DataOutputStream dos, final Char p, final List<Object> ds) throws IOException {
        final List<Object> r = dangDung(p, ds);
        final int n = Math.min(r.size(), 120);
        dos.writeByte(PHIEN);
        dos.writeByte(n);
        for (int i = 0; i < n; i++) {
            final Object o = r.get(i);
            if (o instanceof Animal) {
                final Animal a = (Animal) o;
                dos.writeByte(0);
                dos.writeShort(a.id);
                dos.writeByte(a.idImg);
                dos.writeByte(a.idImg >= 0 && a.idImg < Animal.idImgIcon.length ? Animal.idImgIcon[a.idImg] : 0);
                dos.writeByte(a.level);
                dos.writeUTF(com.htbb.pokemon.ChuPokemon.tenThuCuoi(a.getName()));
                dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi(a.getInfoSendUser()));
                dos.writeByte(p.animalRide == a ? 1 : 0);
            } else {
                final Pet t = (Pet) o;
                dos.writeByte(1);
                dos.writeShort(t.id);
                dos.writeByte(t.idImg + 5);
                dos.writeByte(t.getIdIconPaint());
                dos.writeUTF(t.getName());
                dos.writeByte(t.getType());
                dos.writeUTF(t.getInfoSendUser());
                dos.writeInt(t.getTimeExpire());
                dos.writeByte(p.petUsing == t ? 1 : 0);
            }
            ghiDong(dos, p, o);
        }
        final String cu = tenThuCuoiCu(p);
        dos.writeByte(cu != null ? 1 : 0);
        if (cu != null) {
            dos.writeUTF(com.htbb.pokemon.ChuPokemon.tenThuCuoi(cu));
        }
    }

    static void guiTui(final Char p) {
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
        } catch (final Exception ignored) {
        }
    }

    static void traLoi(final Char p, final String loi, final String ok) {
        if (loi != null) {
            DungVatPham.loi(p, loi);
        } else if (ok != null) {
            DungVatPham.bao(p, ok);
        }
        guiTui(p);
    }

    /** Lenh -119 tu client. */
    public static void xuLy(final Char p, final Message m) throws IOException {
        final byte kieu = m.dis.available() > 0 ? m.dis.readByte() : KIEU_TRANG_THAI;
        switch (kieu) {
            case KIEU_THA: {
                final int n = Math.max(0, Math.min(THA_TOI_DA_MOT_LAN, m.dis.readUnsignedByte()));
                final byte[] loai = new byte[n];
                final short[] id = new short[n];
                for (int i = 0; i < n; i++) {
                    loai[i] = m.dis.readByte();
                    id[i] = m.dis.readShort();
                }
                final KetQua kq = tha(p, loai, id);
                if (kq.soTha > 0) {
                    traLoi(p, null, cauKetQua(kq));
                } else {
                    traLoi(p, cauKetQua(kq), null);
                }
                return;
            }
            case KIEU_KHOA: {
                final byte loai = m.dis.readByte();
                final short id = m.dis.readShort();
                final boolean khoa = m.dis.readByte() == 1;
                final String loi = doiKhoa(p, loai, id, khoa);
                traLoi(p, loi, loi == null ? (khoa ? "Đã khoá thú: không thả nhầm được." : "Đã mở khoá thú.") : null);
                return;
            }
            case KIEU_DUNG_VE: {
                final String loi = dungVe(p, null);
                traLoi(p, loi, loi == null ? "Đã mở thêm ô thú. " + thanhCongVe(p) : null);
                return;
            }
            case KIEU_MUA_VE: {
                final String loi = kiemMuaVe(p);
                if (loi != null) {
                    traLoi(p, loi, null);
                    return;
                }
                final String kq = com.htbb.pokemon.NapTienGame.muaTheoMa(p, com.htbb.pokemon.NapTienLogic.MA_VE_MO_O_THU);
                final boolean ok = kq != null && kq.startsWith("Đã mua");
                traLoi(p, ok ? null : kq, ok ? kq : null);
                return;
            }
            default:
                guiTui(p);
        }
    }

    /** Thu cuoi dang cho tang con dung o vi tri {@code viTri} khong (chan tang nham sau khi danh sach doi). */
    public static boolean dungConChoTang(final Char p, final int viTri) {
        return p.thuChoTang != null && viTri >= 0 && viTri < p.animal.size() && p.animal.get(viTri) == p.thuChoTang;
    }
}
