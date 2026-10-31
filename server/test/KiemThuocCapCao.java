package real;

import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.MoTaVatPham;
import com.htbb.pokemon.ThuocCapCao;
import com.htbb.pokemon.ThongSoLauDai;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * THUOC CAP CAO BANG XU (31/10/2026, chu game + nguoi choi: cap 50+ 11-22 nghin HP ma thuoc xu chi toi 1.000 HP / 2.000
 * PP, phai dot luong khoa vao goi 200 binh). Kiem: du lieu (cung id / luong hoi voi goi luong khoa, data_potion.price giu 0
 * nen goi 24 khong mua duoc), gia trong khung thiet ke 9.3 o moi cap mo (0,05-5% thu nhap 1 gio / binh, tong chi thuoc
 * &lt;= ~13% theo so do sat thuong nhan, chuoi gia nhat quan voi cua so thuoc), khoa cap, chi mua canh Chansey / Kecleon (nha Machamp / ngoai troi xa: khong),
 * duong mua qua menu -30 (moi client cu / moi deu co) tu Chansey va Kecleon. Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemThuocCapCao {

    private KiemThuocCapCao() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void dung(final String m, final boolean v) {
        BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
    }

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    // ------------------------------------------------------------------ nhan vat ghi goi

    static final class NV extends Char {
        final List<Message> goi = new ArrayList<Message>();

        NV(final int id, final int cap) {
            super(null);
            charDBID = id;
            this.id = (short) id;
            charname = "kiemthuoc" + id;
            lvDetail = new LevelDetail();
            lvDetail.lv = cap;
            hp = 1000;
            maxhp = 1000;
            userTrade = new java.util.Vector<Char>();
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
        }

        /** Menu -30 cuoi {idNpc, idMenu, dong[]} hoac null. */
        Object[] menuCuoi() {
            Object[] kq = null;
            for (final Message m : goi) {
                if (m.command != -30) {
                    continue;
                }
                try {
                    final byte[] b = m.toByteArray();
                    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                    final int npc = in.readShort();
                    final int idMenu = in.readUnsignedByte();
                    final int n = in.readUnsignedByte();
                    final String[] d = new String[n];
                    for (int i = 0; i < n; i++) {
                        d[i] = in.readUTF();
                    }
                    kq = new Object[]{Integer.valueOf(npc), Integer.valueOf(idMenu), d};
                } catch (final Exception e) {
                    kq = new Object[]{Integer.valueOf(0), Integer.valueOf(0), new String[0]};
                }
            }
            return kq;
        }

        String baoCuoi() {
            String kq = "";
            for (final Message m : goi) {
                try {
                    final byte[] b = m.toByteArray();
                    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                    if (m.command == 37 || m.command == -26) {
                        kq = in.readUTF();
                    } else if (m.command == DungVatPham.LENH) {
                        in.readByte();
                        kq = in.readUTF();
                    }
                } catch (final Exception ignored) {
                }
            }
            return kq;
        }
    }

    static int idKe = 993400;

    /** Nhan vat trong nha {@code nha} (cMap that: 100 Chansey, 102 Kecleon, 103 Machamp). */
    static NV trongNha(final int cap, final int nha) {
        final NV p = new NV(idKe++, cap);
        final Map m = new Map();
        m.mapId = nha;
        m.mapIDLoadMap = -1;
        m.isMapOffline = true;
        p.map = m;
        p.mapID = nha;
        p.x = 40;
        p.y = 40;
        ThuocCapCaoGame.quen(p.charDBID);
        return p;
    }

    /** Ngoai troi lang 0 (Pewter) o o {@code ox, oy} (Chansey 38,46; Kecleon 55,40). */
    static NV ngoaiTroi(final int cap, final int ox, final int oy) {
        final NV p = new NV(idKe++, cap);
        final Map m = new Map();
        m.mapId = 0;
        m.mapIDLoadMap = 0;
        m.isMapOffline = false;
        p.map = m;
        p.mapID = 0;
        p.x = ox * 16 + 8;
        p.y = oy * 16 + 8;
        ThuocCapCaoGame.quen(p.charDBID);
        return p;
    }

    // ------------------------------------------------------------------ chay

    public static void chay() {
        final boolean lauDaiCu = LauDai.bat();
        try {
            duLieu();
            gia();
            nganSach();
            chuoiGia();
            menu();
            mua();
            khoaCap();
            dungCho();
        } catch (final Throwable e) {
            e.printStackTrace();
            bang("KiemThuocCapCao khong nem loi", "", String.valueOf(e));
        } finally {
            LauDai.dat(lauDaiCu);
        }
    }

    // ------------------------------------------------------------------ du lieu

    static String doc(final String tep) throws Exception {
        return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
    }

    static void duLieu() throws Exception {
        final String dung = doc("src/real/doUsePotion.java");
        for (int b = 0; b < ThuocCapCao.soBac(); b++) {
            final int id = ThuocCapCao.ID[b];
            final String ham = ThuocCapCao.LA_PP[b] ? "doUseMP" : "doUseHP";
            final java.util.regex.Matcher m = java.util.regex.Pattern.compile("case " + id + ":\\s*\\n\\s*[^\\n]*" + ham
                    + "\\(player, potionType, (\\d+)\\)").matcher(dung);
            bang("id " + id + ": doUsePotion " + ham + " hoi dung " + ThuocCapCao.HOI[b], String.valueOf(ThuocCapCao.HOI[b]),
                    m.find() ? m.group(1) : "?");
            boolean trongMoTa = false;
            for (final int x : ThuocCapCao.LA_PP[b] ? MoTaVatPham.PP : MoTaVatPham.HP) {
                trongMoTa |= x == id;
            }
            dung("id " + id + " nam trong MoTaVatPham." + (ThuocCapCao.LA_PP[b] ? "PP" : "HP") + " (nut thuoc HUD)", trongMoTa);
        }
        // goi luong khoa 200 binh (data_shop 74 / 75 / 76) cho dung o potions 93 / 94 / 95 = cung binh -> giu lam tien loi
        final String mua = doc("src/real/doBuySpecialItem.java");
        dung("goi luong khoa (shop id + 19): 74 -> 93, 75 -> 94, 76 -> 95 = cung binh Chansey ban", mua.contains("var10001 = item.id + 19;"));
        // data_potion phan A: price 0 cho moi id (goi 24 / cua so thuoc tu choi), khong UPDATE nao o phan B dat gia
        final String sql = doc("../sql/htbb.sql");
        final int dau = sql.indexOf("INSERT INTO `data_potion` VALUES");
        final String dong = sql.substring(dau, sql.indexOf('\n', dau));
        for (final int id : ThuocCapCao.ID) {
            final java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(" + id + ",\\d+,\\d+,\\d,'[^']*','[^']*',(\\d+),").matcher(dong);
            bang("data_potion " + id + ": price 0 (goi 24 khong mua duoc - chi menu co khoa cap)", "0", m.find() ? m.group(1) : "?");
            dung("phan B khong dat gia data_potion " + id, !java.util.regex.Pattern.compile(
                    "UPDATE `data_potion` SET `price`\\s*=\\s*\\d+ WHERE `id`\\s*=\\s*" + id + "\\b").matcher(sql).find());
        }
        final String mua24 = doc("src/real/OfflineMap.java");
        dung("goi 24 (cua so thuoc): price <= 0 bo qua", mua24.contains("if (price <= 0L) {"));
        dung("THUOC_BAN (cua so thuoc) khong chua thuoc cap cao", !chua(NpcDichVu.THUOC_BAN, ThuocCapCao.ID));
    }

    static boolean chua(final int[] a, final int[] b) {
        for (final int x : a) {
            for (final int y : b) {
                if (x == y) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ gia

    static void gia() {
        dung("bang thu nhap da nap (data_cap_do_lau_dai)", ThongSoLauDai.XU_MOI_QUAI.length > ThongSoLauDai.CAP_TOI_DA);
        for (int b = 0; b < ThuocCapCao.soBac(); b++) {
            final int c = ThuocCapCao.CAP[b];
            final long thuNhap = LauDai.xuMoiGio(c);
            final long g = ThuocCapCao.giaTheoCap(b, c);
            bang("bac " + b + " (id " + ThuocCapCao.ID[b] + ") cap mo " + c + ": gia = gia goc", Long.valueOf(ThuocCapCao.GIA_GOC[b]), Long.valueOf(g));
            dung("bac " + b + " cap " + c + ": " + g + " xu >= 0,05% thu nhap 1 gio (" + thuNhap + ")", g * 10000L >= thuNhap * ThuocCapCao.SAN_PHAN_VAN);
            dung("bac " + b + " cap " + c + ": " + g + " xu <= 5% thu nhap 1 gio", g * 10000L <= thuNhap * ThuocCapCao.TRAN_PHAN_VAN);
            boolean trongKhung = true;
            boolean khongGiam = true;
            long truoc = 0L;
            for (int cap = c; cap <= ThongSoLauDai.CAP_TOI_DA; cap++) {
                final long gc = ThuocCapCao.giaTheoCap(b, cap);
                final long tn = LauDai.xuMoiGio(cap);
                trongKhung &= gc * 10000L >= tn * ThuocCapCao.SAN_PHAN_VAN && gc * 10000L <= tn * ThuocCapCao.TRAN_PHAN_VAN;
                khongGiam &= gc >= truoc;
                truoc = gc;
            }
            dung("bac " + b + ": moi cap " + c + ".." + ThongSoLauDai.CAP_TOI_DA + " gia trong 0,05-5% thu nhap 1 gio", trongKhung);
            dung("bac " + b + ": gia khong giam khi len cap", khongGiam);
        }
        // binh lon dat hon binh nho cung loai (gia / binh tang theo bac)
        dung("HP: gia / binh tang theo bac (3k < 7k < 15k)", ThuocCapCao.GIA_GOC[0] < ThuocCapCao.GIA_GOC[1]
                && ThuocCapCao.GIA_GOC[1] < ThuocCapCao.GIA_GOC[2]);
        dung("PP: gia / binh tang theo bac (3,5k < 7k)", ThuocCapCao.GIA_GOC[3] < ThuocCapCao.GIA_GOC[4]);
        bang("gia thuan: thu nhap 0 -> gia goc", Long.valueOf(ThuocCapCao.GIA_GOC[2]), Long.valueOf(ThuocCapCao.gia(2, 0L)));
        bang("gia thuan: san 0,05% lam tron len (302.450 -> 152)", Long.valueOf(152L), Long.valueOf(ThuocCapCao.gia(2, 302450L)));
    }

    /**
     * Chi thuoc 1 gio danh quai dung cap o cap mo, theo so do 31/10/2026 (cap 50+: ~3-6% HP toi da moi don, vai don moi
     * con): HP can = (so don x % moi don x quai/gio - hoi ngoai tran) x HP toi da; hoi 1% / 5 giay, ~25% thoi gian ngoai
     * tran = 1,8 x HP toi da / gio. PP gia dinh = 50% so diem HP, mua bang binh PP cung cap. HP toi da: cap 30 ~8k, cap 50
     * ~15k (11-22k), cap 70 ~22k.
     */
    static double phanTramThuNhap(final int cap, final int hpToiDa, final int don, final double phanTramDon, final int bacHp, final int bacPp) {
        final double hpGio = Math.max(0.0, don * phanTramDon * ThongSoLauDai.QUAI_MOI_GIO[cap] - 1.8) * hpToiDa;
        final double xu = hpGio / ThuocCapCao.HOI[bacHp] * ThuocCapCao.giaTheoCap(bacHp, cap)
                + 0.5 * hpGio / ThuocCapCao.HOI[bacPp] * ThuocCapCao.giaTheoCap(bacPp, cap);
        return 100.0 * xu / LauDai.xuMoiGio(cap);
    }

    static void nganSach() {
        // chu game 01/11/2026: vuot 12% chut it o cap 30 chap nhan duoc, truong hop xau nhat cua mo hinh thuong <= ~13%
        final int tran = (ThongSoLauDai.TIEU_HAO_PHAN_TRAM > 0 ? ThongSoLauDai.TIEU_HAO_PHAN_TRAM : 12) + 1;
        final int[][] moc = {{30, 8000, 0, 3}, {50, 15000, 1, 4}, {50, 11000, 1, 4}, {50, 22000, 1, 4}, {70, 22000, 2, 4}};
        for (final int[] m : moc) {
            final double vua = phanTramThuNhap(m[0], m[1], 3, 0.045, m[2], m[3]);
            dung(String.format("cap %d, %d HP, 3 don x 4,5%%: chi thuoc %.1f%% thu nhap <= %d%%", m[0], m[1], vua, tran), vua <= tran);
        }
        // nang nhat so do (4 don x 6%) o cap 50 / 70 (noi do): van <= tran
        for (final int[] m : new int[][]{{50, 15000, 1, 4}, {70, 22000, 2, 4}}) {
            final double nang = phanTramThuNhap(m[0], m[1], 4, 0.06, m[2], m[3]);
            dung(String.format("cap %d, %d HP, 4 don x 6%%: chi thuoc %.1f%% thu nhap <= %d%%", m[0], m[1], nang, tran), nang <= tran);
        }
    }

    /** Gia data_potion phan A cua {@code id} (-1 neu khong doc duoc). */
    static long giaPhanA(final String dong, final int id) {
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(" + id + ",\\d+,\\d+,\\d,'[^']*','[^']*',(\\d+),").matcher(dong);
        return m.find() ? Long.parseLong(m.group(1)) : -1L;
    }

    /** Chuoi cua so thuoc (data_potion 1..6) + thuoc cap cao: binh lon dat hon, xu / diem hoi khong tang. */
    static void chuoiGia() throws Exception {
        final String sql = doc("../sql/htbb.sql");
        final int dau = sql.indexOf("INSERT INTO `data_potion` VALUES");
        final String dong = sql.substring(dau, sql.indexOf('\n', dau));
        // {hoi, gia}: HP 80 / 300 / 1.000 (cua so) -> 3.000 / 7.000 / 15.000 (cap cao); PP 160 / 600 / 2.000 -> 3.500 / 7.000
        final long[][] hp = {{80, giaPhanA(dong, 1)}, {300, giaPhanA(dong, 2)}, {1000, giaPhanA(dong, 3)},
            {3000, ThuocCapCao.GIA_GOC[0]}, {7000, ThuocCapCao.GIA_GOC[1]}, {15000, ThuocCapCao.GIA_GOC[2]}};
        final long[][] pp = {{160, giaPhanA(dong, 4)}, {600, giaPhanA(dong, 5)}, {2000, giaPhanA(dong, 6)},
            {3500, ThuocCapCao.GIA_GOC[3]}, {7000, ThuocCapCao.GIA_GOC[4]}};
        for (final long[][] c : new long[][][]{hp, pp}) {
            final String ten = c == hp ? "HP" : "PP";
            boolean tang = true;
            boolean giam = true;
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < c.length; i++) {
                sb.append(c[i][0]).append('@').append(c[i][1]).append(' ');
                if (i > 0) {
                    tang &= c[i][1] > c[i - 1][1];
                    giam &= c[i][1] * c[i - 1][0] <= c[i - 1][1] * c[i][0]; // gia/diem khong tang
                }
            }
            dung(ten + ": binh lon hon dat hon (" + sb.toString().trim() + ")", tang);
            dung(ten + ": xu / diem hoi khong tang theo bac", giam);
        }
        final long thuNhap1 = LauDai.xuMoiGio(1);
        boolean khung = true;
        for (int id = 1; id <= 6; id++) {
            final long g = giaPhanA(dong, id);
            khung &= g * 10000L >= thuNhap1 * ThuocCapCao.SAN_PHAN_VAN && g * 10000L <= thuNhap1 * ThuocCapCao.TRAN_PHAN_VAN;
        }
        dung("cua so thuoc 1..6: 0,05-5% thu nhap 1 gio o cap 1 (" + thuNhap1 + ") - het vuot 5% duoi cap 23", khung);
        // phan B: UPDATE theo gia cu -> dung gia phan A
        final long[][] cu = {{1, 100}, {2, 500}, {3, 800}, {4, 50}, {5, 150}, {6, 500}};
        for (final long[] c : cu) {
            final String u = "UPDATE `data_potion` SET `price` = " + giaPhanA(dong, (int) c[0]) + " WHERE `id` = " + c[0] + " AND `price` = " + c[1] + ";";
            dung("phan B sua_gia_thuoc: " + u, sql.contains(u));
        }
    }

    // ------------------------------------------------------------------ menu (client cu / moi: chi goi -30)

    static void menu() {
        final NV p = trongNha(50, 100); // nha 100: Chansey
        dung("Chansey (goi 23 loai 0): mo menu", NpcDichVu.moMenu(p, NpcDichVu.NPC_Y_TA));
        Object[] m = p.menuCuoi();
        dung("menu Chansey 4 dong, dong 4 = " + ThuocCapCaoGame.TEN_MUC, m != null && ((String[]) m[2]).length == 4
                && ThuocCapCaoGame.TEN_MUC.equals(((String[]) m[2])[3]));
        p.goi.clear();
        dung("chon 'Thuốc cấp cao' (MapDoMenu -> NpcDichVu.xuLyMenu)", NpcDichVu.xuLyMenu(p, NpcDichVu.NPC_Y_TA, 0, 3));
        m = p.menuCuoi();
        dung("danh sach: NPC ao " + ThuocCapCaoGame.NPC_ID + " idMenu 0, " + (ThuocCapCao.soBac() + 1) + " dong", m != null
                && ((Integer) m[0]).intValue() == ThuocCapCaoGame.NPC_ID && ((Integer) m[1]).intValue() == 0
                && ((String[]) m[2]).length == ThuocCapCao.soBac() + 1);
        final String[] d = m == null ? new String[0] : (String[]) m[2];
        dung("cap 50: dong 7.000 HP ghi gia xu", d.length > 1 && d[1].contains("+7.000 HP") && d[1].endsWith(ThuocCapCao.giaTheoCap(1, 50) + " xu"));
        dung("cap 50: dong 15.000 HP ghi khoa cap 70+", d.length > 2 && d[2].contains("(cấp 70+)"));
        dung("dong cuoi Huy", d.length > 0 && "Huỷ".equals(d[d.length - 1]));
        // Kecleon (nha 102)
        final NV k = trongNha(30, 102);
        dung("Kecleon (goi 23 loai 1): mo menu", NpcDichVu.moMenu(k, VeDichChuyenGame.NPC_KECLEON));
        m = k.menuCuoi();
        dung("menu Kecleon 3 dong, dong 3 = thuoc cap cao", m != null && ((String[]) m[2]).length == 3
                && ThuocCapCaoGame.TEN_MUC.equals(((String[]) m[2])[2]));
        k.goi.clear();
        NpcDichVu.xuLyMenu(k, VeDichChuyenGame.NPC_KECLEON, 0, 2);
        m = k.menuCuoi();
        dung("Kecleon chon dong 3: danh sach thuoc cap cao", m != null && ((Integer) m[0]).intValue() == ThuocCapCaoGame.NPC_ID);
        k.goi.clear();
        NpcDichVu.xuLyMenu(k, ThuocCapCaoGame.NPC_ID, 0, 0);
        m = k.menuCuoi();
        dung("chon bac 0 (cap 30 mo): menu so binh idMenu 1, 10/50/100/200 + Huy", m != null && ((Integer) m[1]).intValue() == 1
                && ((String[]) m[2]).length == ThuocCapCao.SO_MUA.length + 1 && ((String[]) m[2])[0].startsWith("Mua 10 bình: "));
    }

    // ------------------------------------------------------------------ mua

    static void mua() {
        LauDai.dat(true);
        final NV p = trongNha(50, 100);
        final long gia = ThuocCapCao.giaTheoCap(1, 50);
        p.setxu(gia * 1000);
        LauDai.layVaDatLaiXu();
        NpcDichVu.xuLyMenu(p, ThuocCapCaoGame.NPC_ID, 0, 1); // xem gia bac 1 (7.000 HP)
        dung("chon goi 50 binh: xu ly", NpcDichVu.xuLyMenu(p, ThuocCapCaoGame.NPC_ID, 2, 1));
        bang("mua 50 binh 7.000 HP: vao potions[93]", Integer.valueOf(50), Integer.valueOf(p.potions[93]));
        bang("tru dung 50 x gia", Long.valueOf(gia * 950), Long.valueOf(p.getxu()));
        bang("tinh la xu ra (sink)", Long.valueOf(gia * 50), Long.valueOf(LauDai.layVaDatLaiXu()[1]));
        dung("bao da mua", p.baoCuoi().startsWith("Đã mua 50 "));
        dung("chi xu: luong / luong khoa khong doi", p.getLuong() == 0 && p.getLuongLock() == 0);
        // o gan day: chi mua phan con cho
        p.potions[93] = ThuocCapCao.TOI_DA_O - 4;
        dung("o con 4 cho: van mua", ThuocCapCaoGame.mua(p, 1, 10));
        bang("o con 4 cho: chi them 4 (toi da 999)", Integer.valueOf(ThuocCapCao.TOI_DA_O), Integer.valueOf(p.potions[93]));
        bang("o con 4 cho: tru 4 binh", Long.valueOf(gia * 946), Long.valueOf(p.getxu()));
        dung("o day: tu choi", !ThuocCapCaoGame.mua(p, 1, 10));
        bang("o day: khong tru", Long.valueOf(gia * 946), Long.valueOf(p.getxu()));
        // thieu xu
        final NV ngheo = trongNha(50, 100);
        ngheo.setxu(gia * 10 - 1);
        NpcDichVu.xuLyMenu(ngheo, ThuocCapCaoGame.NPC_ID, 0, 1);
        dung("thieu xu: tu choi", !ThuocCapCaoGame.mua(ngheo, 1, 10));
        bang("thieu xu: xu giu nguyen", Long.valueOf(gia * 10 - 1), Long.valueOf(ngheo.getxu()));
        bang("thieu xu: khong co binh", Integer.valueOf(0), Integer.valueOf(ngheo.potions[93]));
        dung("thieu xu: bao ro so xu", ngheo.baoCuoi().startsWith("Không đủ "));
        // gia doi theo cap giua luc xem va luc chon (san 0,05% tang theo cap)
        final NV len = trongNha(50, 100);
        len.setxu(1000000L);
        NpcDichVu.xuLyMenu(len, ThuocCapCaoGame.NPC_ID, 0, 1);
        len.lvDetail.lv = 90;
        final boolean doi = ThuocCapCao.giaTheoCap(1, 90) != ThuocCapCao.giaTheoCap(1, 50);
        dung("cap 50 -> 90 doi gia (san 0,05%)", doi);
        dung("gia doi sau khi xem: khong mua", !ThuocCapCaoGame.mua(len, 1, 10));
        bang("gia doi: khong tru", Long.valueOf(1000000L), Long.valueOf(len.getxu()));
        dung("gia doi: mo lai menu gia moi", len.menuCuoi() != null && ((String[]) len.menuCuoi()[2])[0].contains(HoiSinhSo(ThuocCapCao.giaTheoCap(1, 90) * 10)));
        LauDai.dat(false);
    }

    static String HoiSinhSo(final long n) {
        return com.htbb.pokemon.HoiSinh.so(n);
    }

    // ------------------------------------------------------------------ khoa cap

    static void khoaCap() {
        final NV p = trongNha(49, 100);
        p.setxu(1000000L);
        p.goi.clear();
        NpcDichVu.xuLyMenu(p, ThuocCapCaoGame.NPC_ID, 0, 1); // bac 1 mo cap 50
        dung("cap 49 chon 7.000 HP: khong mo menu so binh", p.menuCuoi() == null);
        dung("cap 49: bao mo tu cap 50", p.baoCuoi().contains("cấp 50"));
        dung("cap 49 goi tu che (idMenu 2): khong mua", NpcDichVu.xuLyMenu(p, ThuocCapCaoGame.NPC_ID, 2, 0) && p.potions[93] == 0);
        dung("cap 49 goi thang mua(): tu choi", !ThuocCapCaoGame.mua(p, 1, 10));
        bang("cap 49: khong tru xu", Long.valueOf(1000000L), Long.valueOf(p.getxu()));
        dung("cap 49 van mua bac cap 30 (3.000 HP)", mo(p, 0) && ThuocCapCaoGame.mua(p, 0, 10) && p.potions[22] == 10);
        final NV non = trongNha(29, 100);
        non.setxu(1000000L);
        NpcDichVu.xuLyMenu(non, NpcDichVu.NPC_Y_TA, 0, 3);
        dung("cap 29: danh sach van hien (moi dong ghi cap mo)", non.menuCuoi() != null
                && ((String[]) non.menuCuoi()[2])[0].contains("(cấp 30+)"));
        dung("cap 29: bao mo tu cap 30", non.baoCuoi().contains("cấp " + ThuocCapCao.CAP_MO_DAU));
        dung("cap 29: khong mua duoc bac nao", !ThuocCapCaoGame.mua(non, 0, 10) && !ThuocCapCaoGame.mua(non, 3, 10));
        final NV cao = trongNha(70, 100);
        cao.setxu(1000000L);
        dung("cap 70 mua 15.000 HP", mo(cao, 2) && ThuocCapCaoGame.mua(cao, 2, 10) && cao.potions[94] == 10);
        dung("luat thuan: mo(bac, cap)", ThuocCapCao.mo(0, 30) && !ThuocCapCao.mo(0, 29) && ThuocCapCao.mo(2, 70) && !ThuocCapCao.mo(2, 69)
                && !ThuocCapCao.mo(-1, 100) && !ThuocCapCao.mo(ThuocCapCao.soBac(), 100));
    }

    /** Xem menu so binh bac {@code bac} (ghi gia da hien), true neu mo. */
    static boolean mo(final NV p, final int bac) {
        p.goi.clear();
        NpcDichVu.xuLyMenu(p, ThuocCapCaoGame.NPC_ID, 0, bac);
        final Object[] m = p.menuCuoi();
        return m != null && ((Integer) m[1]).intValue() == 1 + bac;
    }

    // ------------------------------------------------------------------ dung canh NPC (5db68468)

    static void dungCho() {
        final NV machamp = trongNha(50, 103);
        machamp.setxu(1000000L);
        NpcDichVu.xuLyMenu(machamp, ThuocCapCaoGame.NPC_ID, 0, 1);
        dung("nha 103 (chi Machamp): khong mo menu so binh", machamp.menuCuoi() == null);
        dung("nha 103: goi mua tu che bi chan", !ThuocCapCaoGame.mua(machamp, 1, 10) && machamp.potions[93] == 0);
        bang("nha 103: khong tru xu", Long.valueOf(1000000L), Long.valueOf(machamp.getxu()));
        final NV kec = trongNha(50, 102);
        kec.setxu(1000000L);
        dung("nha 102 (Kecleon): mua duoc", mo(kec, 1) && ThuocCapCaoGame.mua(kec, 1, 10) && kec.potions[93] == 10);
        final NV gan = ngoaiTroi(50, 38, 48);
        gan.setxu(1000000L);
        dung("lang 0 canh Chansey (38,46): mua duoc", mo(gan, 1) && ThuocCapCaoGame.mua(gan, 1, 10) && gan.potions[93] == 10);
        final NV ganK = ngoaiTroi(50, 55, 42);
        ganK.setxu(1000000L);
        dung("lang 0 canh Kecleon (55,40): mua duoc", mo(ganK, 3) && ThuocCapCaoGame.mua(ganK, 3, 10) && ganK.potions[24] == 10);
        final NV xa = ngoaiTroi(50, 10, 10);
        xa.setxu(1000000L);
        ThuocCapCaoGame.moDanhSach(xa);
        dung("lang 0 xa moi NPC: khong mo danh sach", xa.menuCuoi() == null);
        dung("lang 0 xa: goi mua tu che bi chan", !ThuocCapCaoGame.mua(xa, 1, 10) && xa.potions[93] == 0);
        bang("lang 0 xa: khong tru xu", Long.valueOf(1000000L), Long.valueOf(xa.getxu()));
        // goi 24 cua so thuoc (01/11/2026): chi mon THUOC_BAN, chi canh Chansey / Kecleon
        dung("goi 24 thuoc: nha 100 (Chansey) mua thuoc 3", NpcTinhBanDo.thuocBanTaiCho(trongNha(1, 100), 3));
        dung("goi 24 thuoc: nha 102 (Kecleon) mua thuoc 1 / Ether 4", NpcTinhBanDo.thuocBanTaiCho(trongNha(1, 102), 1)
                && NpcTinhBanDo.thuocBanTaiCho(trongNha(1, 102), 4));
        dung("goi 24 thuoc: nha 103 (Machamp) KHONG mua thuoc 1", !NpcTinhBanDo.thuocBanTaiCho(trongNha(1, 103), 1));
        dung("goi 24 thuoc: Keo kinh nghiem S (10, gia 500) khong ban o dau", !NpcTinhBanDo.thuocBanTaiCho(trongNha(1, 100), 10)
                && !NpcTinhBanDo.thuocBanTaiCho(trongNha(1, 102), 10));
        dung("goi 24 thuoc: thuoc cap cao 93 khong qua cua so", !NpcTinhBanDo.thuocBanTaiCho(trongNha(1, 100), 93));
        dung("goi 24 thuoc: lang 0 canh Chansey mua duoc, xa thi khong", NpcTinhBanDo.thuocBanTaiCho(ngoaiTroi(1, 38, 48), 1)
                && !NpcTinhBanDo.thuocBanTaiCho(ngoaiTroi(1, 10, 10), 1));
        try {
            dung("OfflineMap.doBuyItem (loai 4) goi thuocBanTaiCho truoc khi tinh gia",
                    doc("src/real/OfflineMap.java").matches("(?s).*thuocBanTaiCho\\(player, var32\\).*getPrice\\(\\);.*"));
        } catch (final Exception e) {
            dung("doc OfflineMap.java", false);
        }
        dung("dungCanh: nha 100 co Chansey, nha 103 khong", NpcTinhBanDo.dungCanh(trongNha(1, 100), NpcTinhBanDo.NPC_BAN_THUOC)
                && !NpcTinhBanDo.dungCanh(trongNha(1, 103), NpcTinhBanDo.NPC_BAN_THUOC));
    }
}
