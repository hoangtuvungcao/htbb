package real;

import com.htbb.pokemon.KhoaBanDo;
import com.htbb.pokemon.KinhTeLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.MoTaVatPham;
import com.htbb.pokemon.VeDichChuyen;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * VE DICH CHUYEN (chu game duyet 24/10/2026): du lieu (data_potion 162 phan A + khoi phan B them_ve_dich_chuyen.sql,
 * MoTaVatPham, icon PNG, POTION_SHOP, Char.SO_O_THUOC), gia theo cap (phi.dich_chuyen_phan_tram_gio), mua o Kecleon
 * (tru xu = xu ra / sink, gioi han tui, gia doi theo cap), Su dung -&gt; menu -&gt; dich chuyen -&gt; tru dung 1 ve, bam lien /
 * hai goi cung luc khong tru hai lan, moi dieu cam (guc, Gym, phong rieng, PvP, giao hang, khan PK / ten do, hoi chieu
 * 60 giay) tu choi KHONG tru, ban do khoa khong hien va khong toi duoc, menu J2ME (3 dong goc, trang &lt;= 7 dong).
 * Goi tu Kiem.java qua {@link #BAO} (lop nam trong goi real de dung ham noi bo).
 */
public final class KiemVeDichChuyen {

    private KiemVeDichChuyen() {
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

    // ================================================================== nhan vat ghi goi

    static final class NV extends Char {
        final List<Message> goi = new ArrayList<Message>();

        NV(final int id, final int cap) {
            super(null);
            charDBID = id;
            this.id = (short) id;
            charname = "kiemve" + id;
            lvDetail = new LevelDetail();
            lvDetail.lv = cap;
            hp = 1000;
            maxhp = 1000;
            mp = 500;
            maxmp = 500;
            userTrade = new java.util.Vector<Char>();
            langDungVung = Boolean.TRUE;
            myCountry = 0;
            inCountry = 0;
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
        }

        /** Cac menu -30 {idNpc, idMenu, dong...}. */
        List<Object[]> menu() {
            final List<Object[]> kq = new ArrayList<Object[]>();
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
                    kq.add(new Object[]{Integer.valueOf(npc), Integer.valueOf(idMenu), d});
                } catch (final Exception e) {
                    kq.add(new Object[]{Integer.valueOf(0), Integer.valueOf(0), new String[0]});
                }
            }
            return kq;
        }

        Object[] menuCuoi() {
            final List<Object[]> m = menu();
            return m.isEmpty() ? null : m.get(m.size() - 1);
        }

        /** Chu cua thong bao cuoi (37 hop OK / -26 tin / -104 the). */
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

        int dem(final int cmd) {
            int n = 0;
            for (final Message m : goi) {
                if (m.command == cmd) {
                    n++;
                }
            }
            return n;
        }
    }

    static int idKe = 991200;

    static Map banDo(final int id, final int nap) {
        final Map m = new Map();
        m.mapId = id;
        m.mapIDLoadMap = nap;
        m.isMapOffline = false;
        return m;
    }

    static NV nv(final int cap) {
        final NV p = new NV(idKe++, cap);
        final Map m = banDo(9, 9); // Cinnabar (Kanto): ban do luyen thuong
        p.map = m;
        p.mapID = 9;
        p.x = 160;
        p.y = 160;
        KhoaBanDoGame.datMien(p.charDBID, 1);
        VeDichChuyenGame.quen(p.charDBID);
        return p;
    }

    /** Lan dich chuyen gia: {map, x, y}. */
    static final List<int[]> DI = new ArrayList<int[]>();

    static Message goi(final int cmd, final int b) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        new DataOutputStream(bo).writeByte(b);
        return new Message(cmd, bo.toByteArray());
    }

    // ================================================================== chay

    public static void chay() {
        final VeDichChuyenGame.DiChuyen diCu = VeDichChuyenGame.DI;
        final VeDichChuyenGame.CoBanDo coCu = VeDichChuyenGame.CO;
        final java.util.Map<Integer, KhoaBanDo.Luat> luatCu = new HashMap<Integer, KhoaBanDo.Luat>(KhoaBanDo.bang());
        final boolean lauDaiCu = LauDai.bat();
        VeDichChuyenGame.DI = new VeDichChuyenGame.DiChuyen() {
            @Override
            public void di(final Char p, final int x, final int y, final int mapId) {
                DI.add(new int[]{mapId, x, y});
                p.map = banDo(mapId, mapId);
                p.mapID = mapId;
            }
        };
        VeDichChuyenGame.CO = new VeDichChuyenGame.CoBanDo() {
            @Override
            public boolean co(final int mapId) {
                return mapId != 7777; // 7777 = ban do khong nap
            }
        };
        KhoaBanDo.datBang(new HashMap<Integer, KhoaBanDo.Luat>());
        try {
            duLieu();
            gia();
            kecleon();
            mua();
            dung();
            cam();
            khoa();
            trang();
        } catch (final Throwable e) {
            e.printStackTrace();
            bang("KiemVeDichChuyen khong nem loi", "", String.valueOf(e));
        } finally {
            VeDichChuyenGame.DI = diCu;
            VeDichChuyenGame.CO = coCu;
            KhoaBanDo.datBang(luatCu);
            LauDai.dat(lauDaiCu);
        }
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    // ------------------------------------------------------------------ du lieu

    static void duLieu() {
        final int id = VeDichChuyen.ID;
        bang("id ve = 162 = Potion.TYPE_VE_DICH_CHUYEN", Integer.valueOf(162), Integer.valueOf(Potion.TYPE_VE_DICH_CHUYEN));
        bang("Char.SO_O_THUOC = 163 (them id 162)", Integer.valueOf(163), Integer.valueOf(Char.SO_O_THUOC));
        dung("o thuoc nhan vat moi du cho id 162", new Char(null).potions.length > id);
        bang("MoTaVatPham 162: loai Ve / the", MoTaVatPham.VE, MoTaVatPham.loai(id));
        bang("MoTaVatPham 162: mo ta duyet",
                "Dùng trong Hành trang: dịch chuyển về làng hoặc thành phố đã mở. Dùng 1 lần là hết. Không dùng khi đang đánh Gym, trong hầm ngục, PvP, giao hàng hay khi bị PK.",
                MoTaVatPham.moTa(id));
        dung("MoTaVatPham 162: dung tu tui duoc", MoTaVatPham.dungDuoc(id));
        dung("MoTaVatPham 162: KHONG gan o nhanh HP/PP", !MoTaVatPham.ganONhanhDuoc(id));
        dung("POTION_SHOP co 162 (chi tiet thuoc client cu)", java.util.Arrays.toString(real.cmd.LoginHandler.POTION_SHOP).contains(" 162]"));
        dung("PORTION_NAME[162] = Ve dich chuyen", real.cmd.LoginHandler.PORTION_NAME.length > id
                && VeDichChuyen.TEN.equals(real.cmd.LoginHandler.PORTION_NAME[id]));
        // phan A + phan B
        java.util.Map<String, String> d = null;
        for (final java.util.Map<String, String> r : DayDuCau.dong("data_potion")) {
            if (String.valueOf(id).equals(r.get("id"))) {
                d = r;
            }
        }
        dung("phan A data_potion co dong 162", d != null);
        if (d != null) {
            bang("data_potion 162 ten", VeDichChuyen.TEN, d.get("name"));
            bang("data_potion 162 icon 32 (icon Ve bay Pidgeot)", "32", d.get("idimage"));
            bang("data_potion 162 khong giao dich", "0", d.get("istrade"));
            bang("data_potion 162 price 0 (khong ban o cua so thuoc gia co dinh)", "0", d.get("price"));
            final int n = com.htbb.pokemon.IconPokemon.doiAnh(5500 + Integer.parseInt(d.get("idimage")));
            dung("icon 162 la PNG that (map/icon_pokemon/" + n + ".png)", n >= 0 && new java.io.File("map/icon_pokemon/" + n + ".png").isFile());
        }
        final String b = DayDuCau.phanNangCap("them_ve_dich_chuyen.sql");
        dung("phan B co khoi them_ve_dich_chuyen.sql", !b.isEmpty());
        dung("phan B: INSERT IGNORE dong 162 (chay lai duoc, giu sua cua admin)",
                b.contains("INSERT IGNORE INTO `data_potion`") && b.contains("(162, 32, 0, 0, 'Vé dịch chuyển', 'vedichchuyen', 0, 0);"));
        dung("phan B: khong UPDATE / DELETE", !b.contains("\nUPDATE ") && !b.contains("\nDELETE "));
        // ma xu ly
        final String u = doc("src/real/DungVatPham.java");
        dung("DungVatPham: ve chan TRUOC duong tru cu (khong tru o goi 22)",
                u.indexOf("VeDichChuyenGame.dungTuTui(p)") > 0 && u.indexOf("VeDichChuyenGame.dungTuTui(p)") < u.indexOf("MapDungThuoc.doUsePotion(map, p, message)"));
        final String o = doc("src/real/OfflineMap.java");
        dung("OfflineMap.doBuyItem: chan goi 24 mua ve gia co dinh",
                o.indexOf("var32 == com.htbb.pokemon.VeDichChuyen.ID") > 0 && o.indexOf("var32 == com.htbb.pokemon.VeDichChuyen.ID") < o.indexOf("final long giaMot = Map.potionTemplates2.get(var32).getPrice();"));
    }

    // ------------------------------------------------------------------ gia

    static void gia() {
        for (final int c : new int[]{1, 5, 27, 60, 100}) {
            bang("gia ve cap " + c + " = phiDichChuyen (phi.dich_chuyen_phan_tram_gio % 1 gio) = " + VeDichChuyen.gia(c) + " xu",
                    Long.valueOf(Math.max(1L, KinhTeLauDai.phiDichChuyen(c))), Long.valueOf(VeDichChuyen.gia(c)));
        }
        dung("gia tang theo cap (5 < 27 < 60)", VeDichChuyen.gia(5) < VeDichChuyen.gia(27) && VeDichChuyen.gia(27) < VeDichChuyen.gia(60));
        bang("hoi chieu 0 khi chua dung", Integer.valueOf(0), Integer.valueOf(VeDichChuyen.giayCho(0L, 5000L)));
        bang("hoi chieu con 60 giay ngay sau khi dung", Integer.valueOf(60), Integer.valueOf(VeDichChuyen.giayCho(1000L, 1000L)));
        bang("hoi chieu het sau 60 giay", Integer.valueOf(0), Integer.valueOf(VeDichChuyen.giayCho(1000L, 61000L)));
    }

    // ------------------------------------------------------------------ Kecleon

    static void kecleon() throws Exception {
        final NV p = nv(27);
        final Map nha = banDo(102, 102);
        nha.isMapOffline = true; // trong nha Kecleon: NpcTinhBanDo.oNoiDichVu
        p.map = nha;
        p.mapID = 102;
        dung("goi 23 loai 1 (Kecleon): menu dich vu", NpcDichVu.moMenu(p, VeDichChuyenGame.NPC_KECLEON));
        Object[] m = p.menuCuoi();
        dung("menu Kecleon gui idNpc 1, 3 dong (thuoc / ve / thuoc cap cao)", m != null && ((Integer) m[0]).intValue() == 1 && ((String[]) m[2]).length == 3);
        dung("menu Kecleon dong 2 ghi gia theo cap", m != null && ((String[]) m[2])[1].contains(com.htbb.pokemon.HoiSinh.so(VeDichChuyen.gia(27)) + " xu/vé"));
        dung("Kecleon ban le (26) cung menu", NpcDichVu.moMenu(p, VeDichChuyenGame.NPC_KECLEON_2));
        p.goi.clear();
        NpcDichVu.xuLyMenu(p, 1, 0, 0);
        bang("chon 'Mua thuốc, vật phẩm': cua so thuoc cu (goi 23)", Integer.valueOf(1), Integer.valueOf(p.dem(23)));
        p.goi.clear();
        NpcDichVu.xuLyMenu(p, 1, 0, 1);
        m = p.menuCuoi();
        dung("chon 'Vé dịch chuyển': menu mua NPC_MUA, idMenu = cap", m != null && ((Integer) m[0]).intValue() == VeDichChuyenGame.NPC_MUA
                && ((Integer) m[1]).intValue() == 27);
        dung("menu mua: 1 / 5 / 10 ve + Huy (4 dong)", m != null && ((String[]) m[2]).length == 4 && ((String[]) m[2])[1].startsWith("Mua 5 vé"));
        final NV xa = nv(27); // dung ngoai troi xa Kecleon
        dung("xa Kecleon: chi bao, khong mo menu", NpcDichVu.moMenu(xa, 1) && xa.menu().isEmpty());
    }

    // ------------------------------------------------------------------ mua

    static void mua() throws Exception {
        LauDai.dat(true);
        final NV p = nv(27);
        final long gia = VeDichChuyen.gia(27);
        p.setxu(gia * 100);
        LauDai.layVaDatLaiXu();
        dung("mua 5 ve", VeDichChuyenGame.mua(p, 27, 1));
        bang("mua 5 ve: tru dung 5 x gia", Long.valueOf(gia * 95), Long.valueOf(p.getxu()));
        bang("mua 5 ve: vao tui potions[162]", Integer.valueOf(5), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        bang("mua tinh la xu ra (sink, LauDai.ghiXuRa)", Long.valueOf(gia * 5), Long.valueOf(LauDai.layVaDatLaiXu()[1]));
        dung("chi xu: luong khong doi", p.getLuong() == 0);
        // gia doi theo cap giua luc xem va luc chon
        p.lvDetail.lv = 28;
        dung("cap doi sau khi xem gia: khong mua", !VeDichChuyenGame.mua(p, 27, 0));
        bang("cap doi: khong tru", Long.valueOf(gia * 95), Long.valueOf(p.getxu()));
        p.lvDetail.lv = 27;
        // gioi han tui
        p.potions[VeDichChuyen.ID] = VeDichChuyen.GIU_TOI_DA - 2;
        dung("vuot gioi han tui: tu choi", !VeDichChuyenGame.mua(p, 27, 1));
        bang("vuot gioi han: khong tru xu", Long.valueOf(gia * 95), Long.valueOf(p.getxu()));
        bang("vuot gioi han: khong them ve", Integer.valueOf(VeDichChuyen.GIU_TOI_DA - 2), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        dung("bao gioi han tui", p.baoCuoi().contains("tối đa " + VeDichChuyen.GIU_TOI_DA));
        // thieu xu
        final NV ngheo = nv(27);
        ngheo.setxu(gia - 1);
        dung("thieu xu: tu choi", !VeDichChuyenGame.mua(ngheo, 27, 0));
        bang("thieu xu: xu giu nguyen", Long.valueOf(gia - 1), Long.valueOf(ngheo.getxu()));
        bang("thieu xu: khong co ve", Integer.valueOf(0), Integer.valueOf(ngheo.potions[VeDichChuyen.ID]));
        bang("thieu xu: khong dem xu ra", Long.valueOf(0L), Long.valueOf(LauDai.layVaDatLaiXu()[1]));
        // qua menu (MapDoMenu -> NpcDichVu.xuLyMenu -> NPC_MUA)
        final NV q = nv(60);
        q.setxu(VeDichChuyen.gia(60) * 10);
        dung("menu NPC_MUA: xu ly", NpcDichVu.xuLyMenu(q, VeDichChuyenGame.NPC_MUA, 60, 0));
        bang("menu NPC_MUA cap 60: 1 ve, tru gia cap 60", Long.valueOf(VeDichChuyen.gia(60) * 9), Long.valueOf(q.getxu()));
        LauDai.dat(false);
    }

    // ------------------------------------------------------------------ dung

    static void dung() throws Exception {
        final NV p = nv(27);
        p.potions[VeDichChuyen.ID] = 2;
        DI.clear();
        DungVatPham.dungThuoc(p.map, p, goi(22, VeDichChuyen.ID));
        Object[] m = p.menuCuoi();
        dung("Su dung (goi 22 that): mo menu NPC_VE idMenu 0", m != null && ((Integer) m[0]).intValue() == VeDichChuyenGame.NPC_VE
                && ((Integer) m[1]).intValue() == 0);
        dung("menu goc 3 dong (mot man J2ME): Về làng / Thành phố đã mở / Huỷ", m != null && ((String[]) m[2]).length == 3
                && ((String[]) m[2])[0].startsWith("Về làng") && ((String[]) m[2])[1].startsWith("Thành phố đã mở") && "Huỷ".equals(((String[]) m[2])[2]));
        bang("mo menu: KHONG tru ve", Integer.valueOf(2), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        dung("mo menu: gui lai tui (client cu tru truoc)", p.dem(16) > 0);
        // chon "Ve lang"
        dung("chon Ve lang: xu ly", NpcDichVu.xuLyMenu(p, VeDichChuyenGame.NPC_VE, 0, 0));
        bang("chon Ve lang: dich chuyen 1 lan", Integer.valueOf(1), Integer.valueOf(DI.size()));
        bang("chon Ve lang: toi lang vung minh (Pewter 0)", Integer.valueOf(VeDichChuyenGame.lang(p)[0]), Integer.valueOf(p.mapID));
        bang("toi noi: tru dung 1 ve", Integer.valueOf(1), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        dung("toi noi: bao da dich chuyen", p.baoCuoi().startsWith("Đã dịch chuyển tới"));
        // bam lai ngay (goi 2): hoi chieu
        p.map = banDo(9, 9);
        p.mapID = 9;
        DI.clear();
        VeDichChuyenGame.xuLyMenu(p, VeDichChuyenGame.NPC_VE, 0, 0);
        bang("bam lai ngay: khong dich chuyen", Integer.valueOf(0), Integer.valueOf(DI.size()));
        bang("bam lai ngay: KHONG tru them", Integer.valueOf(1), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        dung("bam lai ngay: bao hoi chieu", p.baoCuoi().contains("đang hồi"));
        p.goi.clear();
        VeDichChuyenGame.dungTuTui(p);
        dung("dung tu tui khi dang hoi: khong mo menu", p.menu().isEmpty());
        // hai goi chon cung luc: goi thu hai trong luc goi dau chua xong
        VeDichChuyenGame.quen(p.charDBID);
        VeDichChuyenGame.DANG.add(Integer.valueOf(p.charDBID));
        dung("goi chon thu hai khi goi dau dang chay: bo qua", !VeDichChuyenGame.chonDiem(p, VeDichChuyenGame.lang(p)));
        bang("goi chon thu hai: khong tru", Integer.valueOf(1), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        VeDichChuyenGame.DANG.remove(Integer.valueOf(p.charDBID));
        // dang o lang roi
        p.map = banDo(0, 0);
        p.mapID = 0;
        dung("dang o lang: khong dich chuyen", !VeDichChuyenGame.chonDiem(p, VeDichChuyenGame.lang(p)));
        bang("dang o lang: khong tru", Integer.valueOf(1), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        // dich chuyen that bai (move2Map tu chan): tra lai ve
        p.map = banDo(9, 9);
        p.mapID = 9;
        final VeDichChuyenGame.DiChuyen di = VeDichChuyenGame.DI;
        VeDichChuyenGame.DI = new VeDichChuyenGame.DiChuyen() {
            @Override
            public void di(final Char c, final int x, final int y, final int mapId) {
                // o lai cho cu (vd move2Map gap khoa / chien truong chua mo)
            }
        };
        try {
            dung("khong toi noi: bao that bai", !VeDichChuyenGame.chonDiem(p, VeDichChuyenGame.lang(p)));
            bang("khong toi noi: tra lai ve", Integer.valueOf(1), Integer.valueOf(p.potions[VeDichChuyen.ID]));
            final Long l = VeDichChuyenGame.LAN_CUOI.get(Integer.valueOf(p.charDBID));
            dung("khong toi noi: khong tinh hoi chieu", l == null);
        } finally {
            VeDichChuyenGame.DI = di;
        }
        // ban do khong nap
        dung("ban do khong nap: tu choi", !VeDichChuyenGame.chonDiem(p, new int[]{7777, 1, 1}));
        bang("ban do khong nap: khong tru", Integer.valueOf(1), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        // het ve
        p.potions[VeDichChuyen.ID] = 0;
        p.goi.clear();
        VeDichChuyenGame.dungTuTui(p);
        dung("het ve: khong mo menu", p.menu().isEmpty());
        dung("het ve: khong dich chuyen", !VeDichChuyenGame.chonDiem(p, new int[]{3, 5, 20}));
        bang("het ve: khong am", Integer.valueOf(0), Integer.valueOf(p.potions[VeDichChuyen.ID]));
    }

    // ------------------------------------------------------------------ cam

    interface Dat {
        void dat(NV p) throws Exception;
    }

    static void mot(final String ten, final Dat d, final String chu) throws Exception {
        final NV p = nv(30);
        p.potions[VeDichChuyen.ID] = 3;
        d.dat(p);
        final String ly = VeDichChuyenGame.lyDoChan(p);
        dung(ten + ": bi chan (" + ly + ")", ly != null && ly.contains(chu));
        p.goi.clear();
        DI.clear();
        VeDichChuyenGame.dungTuTui(p);
        dung(ten + ": Su dung khong mo menu", p.menu().isEmpty());
        dung(ten + ": bao ly do", p.baoCuoi().contains(chu));
        VeDichChuyenGame.xuLyMenu(p, VeDichChuyenGame.NPC_VE, 0, 0); // menu cu (mo truoc khi bi chan)
        VeDichChuyenGame.chonDiem(p, new int[]{3, 5, 20});
        bang(ten + ": khong dich chuyen", Integer.valueOf(0), Integer.valueOf(DI.size()));
        bang(ten + ": KHONG tru ve", Integer.valueOf(3), Integer.valueOf(p.potions[VeDichChuyen.ID]));
    }

    static Object taoKhongGoiHam(final Class<?> c) throws Exception {
        final Class<?> u = Class.forName("sun.misc.Unsafe");
        final java.lang.reflect.Field f = u.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return u.getMethod("allocateInstance", Class.class).invoke(f.get(null), c);
    }

    static void cam() throws Exception {
        mot("dang guc", new Dat() {
            public void dat(final NV p) {
                p.hp = 0;
            }
        }, "gục");
        final Monster[] boss = new Monster[1];
        mot("dang danh Gym", new Dat() {
            public void dat(final NV p) {
                final Monster b = new Monster((byte) 1);
                b.maxhp = 1000;
                b.hp = 1000;
                b.isDead = false;
                b.map = p.map;
                BossLauDai.datGym(b, 1, p, 600000L);
                boss[0] = b;
            }
        }, "Gym");
        if (boss[0] != null) {
            boss[0].hp = 0;
            boss[0].isDead = true;
        }
        mot("trong pho ban (phong rieng)", new Dat() {
            public void dat(final NV p) {
                com.htbb.pokemon.PhongRieng.dangKyMap(PhongRiengGame.MAP_PHO_BAN);
                p.map = banDo(PhongRiengGame.MAP_PHO_BAN, PhongRiengGame.NAP_PHO_BAN);
                p.mapID = PhongRiengGame.MAP_PHO_BAN;
            }
        }, "phó bản");
        mot("o dau truong PvP (204)", new Dat() {
            public void dat(final NV p) {
                p.map = banDo(204, 204);
                p.mapID = 204;
            }
        }, "PvP");
        mot("o Dau truong La Ma (203)", new Dat() {
            public void dat(final NV p) {
                p.map = banDo(203, 203);
                p.mapID = 203;
            }
        }, "PvP");
        mot("dang giao hang", new Dat() {
            public void dat(final NV p) throws Exception {
                p.monster = (MonsterVantieu) taoKhongGoiHam(MonsterVantieu.class);
            }
        }, "giao hàng");
        mot("deo khan PK", new Dat() {
            public void dat(final NV p) {
                p.pk = 14;
            }
        }, "PK");
        mot("ten do (diem PK)", new Dat() {
            public void dat(final NV p) {
                p.killer = 5;
            }
        }, "PK");
        mot("hoi chieu 60 giay", new Dat() {
            public void dat(final NV p) {
                VeDichChuyenGame.LAN_CUOI.put(Integer.valueOf(p.charDBID), Long.valueOf(System.currentTimeMillis() - 30000L));
            }
        }, "chờ");
        mot("dang giao dich", new Dat() {
            public void dat(final NV p) {
                p.userTrade.add(new NV(idKe++, 1));
            }
        }, "giao dịch");
        // khu thuong: dung duoc
        final NV p = nv(30);
        p.potions[VeDichChuyen.ID] = 1;
        dung("ban do luyen thuong: khong bi chan", VeDichChuyenGame.lyDoChan(p) == null);
        p.map = banDo(41, 41);
        p.mapID = 41;
        dung("Chan nui Silver (khu xa): khong bi chan", VeDichChuyenGame.lyDoChan(p) == null);
    }

    // ------------------------------------------------------------------ khoa ban do

    static void khoa() throws Exception {
        final NV p = nv(10);
        p.potions[VeDichChuyen.ID] = 2;
        final List<int[]> mo = VeDichChuyenGame.diemDaMo(p);
        dung("co diem 'Thành phố đã mở' (" + mo.size() + ")", mo.size() >= 3);
        boolean coNha = false, coDang = false;
        for (final int[] d : mo) {
            coNha |= d[0] == VeDichChuyenGame.lang(p)[0];
            coDang |= d[0] == 9;
        }
        boolean coHoenn = false;
        for (final int[] d : mo) {
            coHoenn |= com.htbb.pokemon.BanDoHoenn.laHoenn(d[0]);
        }
        dung("cap 10 o Kanto: khong hien thi tran Hoenn (cong tau Hoenn cap " + com.htbb.pokemon.BanDoHoenn.CAP_VAO + ")", !coHoenn);
        DI.clear();
        dung("cap 10 chon Rustboro (menu cu / goi tu che): khong di", !VeDichChuyenGame.chonDiem(p, new int[]{45, 30, 30}));
        bang("cap 10 chon Hoenn: khong dich chuyen, KHONG tru", "0/2", DI.size() + "/" + p.potions[VeDichChuyen.ID]);
        final NV cao = nv(95);
        boolean caoHoenn = false;
        for (final int[] d : VeDichChuyenGame.diemDaMo(cao)) {
            caoHoenn |= com.htbb.pokemon.BanDoHoenn.laHoenn(d[0]);
        }
        dung("cap 95 (lau dai tat): thi tran Hoenn co Trung tam hien", LauDai.bat() || caoHoenn);
        dung("danh sach khong lap lang vung minh (dong rieng)", !coNha);
        dung("danh sach khong co ban do dang dung", !coDang);
        final java.util.Map<Integer, KhoaBanDo.Luat> l = new HashMap<Integer, KhoaBanDo.Luat>();
        l.put(Integer.valueOf(3), new KhoaBanDo.Luat(3, 99, "", "", true, ""));
        KhoaBanDo.datBang(l);
        try {
            if (KhoaBanDoGame.bat()) {
                boolean co3 = false;
                for (final int[] d : VeDichChuyenGame.diemDaMo(p)) {
                    co3 |= d[0] == 3;
                }
                dung("ban do khoa (cap 99) khong hien", !co3);
                DI.clear();
                dung("chon ban do khoa (menu cu): khong di", !VeDichChuyenGame.chonDiem(p, new int[]{3, 5, 20}));
                bang("chon ban do khoa: khong dich chuyen", Integer.valueOf(0), Integer.valueOf(DI.size()));
                bang("chon ban do khoa: KHONG tru ve", Integer.valueOf(2), Integer.valueOf(p.potions[VeDichChuyen.ID]));
            } else {
                dung("khoa ban do dang tat (ban_do.khoa.bat=0): bo qua kiem khoa", true);
            }
        } finally {
            KhoaBanDo.datBang(new HashMap<Integer, KhoaBanDo.Luat>());
        }
    }

    // ------------------------------------------------------------------ trang J2ME

    static void trang() throws Exception {
        final String[] ten = new String[12];
        for (int i = 0; i < ten.length; i++) {
            ten[i] = "TP" + i;
        }
        final String[] t0 = VeDichChuyen.menuTrang(ten, 0);
        bang("trang 1/3: 5 diem + Trang sau + Quay lai", "[TP0, TP1, TP2, TP3, TP4, Trang sau (2/3), Quay lại]", java.util.Arrays.toString(t0));
        bang("trang 3/3: 2 diem + Trang truoc", "[TP10, TP11, Trang trước]", java.util.Arrays.toString(VeDichChuyen.menuTrang(ten, 2)));
        bang("chon dong 2 trang 2 = diem 7", Integer.valueOf(7), Integer.valueOf(VeDichChuyen.chiSo(12, 1, 2)));
        bang("chon 'Trang sau'", Integer.valueOf(-1), Integer.valueOf(VeDichChuyen.chiSo(12, 0, 5)));
        bang("chon 'Quay lại'", Integer.valueOf(-2), Integer.valueOf(VeDichChuyen.chiSo(12, 0, 6)));
        bang("chon ngoai menu", Integer.valueOf(-3), Integer.valueOf(VeDichChuyen.chiSo(12, 0, 9)));
        dung("moi trang <= 7 dong (J2ME 3 dong / man, cuon it)", t0.length <= 7);
        // qua menu that
        final NV p = nv(10);
        p.potions[VeDichChuyen.ID] = 1;
        final int soMo = VeDichChuyenGame.diemDaMo(p).size();
        VeDichChuyenGame.xuLyMenu(p, VeDichChuyenGame.NPC_VE, 0, 1);
        final Object[] m = p.menuCuoi();
        dung("chon 'Thành phố đã mở': menu trang idMenu 1", m != null && ((Integer) m[0]).intValue() == VeDichChuyenGame.NPC_VE && ((Integer) m[1]).intValue() == 1);
        dung("trang dau <= 7 dong", m != null && ((String[]) m[2]).length <= 7);
        if (soMo > VeDichChuyen.MOI_TRANG) {
            VeDichChuyenGame.xuLyMenu(p, VeDichChuyenGame.NPC_VE, 1, VeDichChuyen.MOI_TRANG);
            final Object[] m2 = p.menuCuoi();
            dung("Trang sau -> idMenu 2", m2 != null && ((Integer) m2[1]).intValue() == 2);
        }
        DI.clear();
        final List<int[]> ds = VeDichChuyenGame.diemDaMo(p);
        VeDichChuyenGame.xuLyMenu(p, VeDichChuyenGame.NPC_VE, 1, 0);
        dung("chon diem dau trang 1: toi dung diem da hien", DI.size() == 1 && DI.get(0)[0] == ds.get(0)[0]);
        bang("chon diem: tru 1 ve", Integer.valueOf(0), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        // nhan diem
        boolean nhanVung = true;
        for (final int[] d : ds) {
            final String n = VeDichChuyenGame.nhan(d[0]);
            nhanVung &= n.contains("Kanto") || n.contains("Johto") || n.contains("Hoenn");
        }
        dung("moi dong diem den ghi vung", nhanVung);
    }

    /** Doc sql/htbb.sql qua DayDu (lop goi mac dinh) bang phan chieu. */
    static final class DayDuCau {
        @SuppressWarnings("unchecked")
        static List<java.util.Map<String, String>> dong(final String bang) {
            try {
                final java.lang.reflect.Method m = Class.forName("DayDu").getDeclaredMethod("dongChu", String.class);
                m.setAccessible(true);
                return (List<java.util.Map<String, String>>) m.invoke(null, bang);
            } catch (final Exception e) {
                return new ArrayList<java.util.Map<String, String>>();
            }
        }

        static String phanNangCap(final String tep) {
            try {
                final java.lang.reflect.Method m = Class.forName("DayDu").getDeclaredMethod("phanNangCap", String.class);
                m.setAccessible(true);
                return (String) m.invoke(null, tep);
            } catch (final Exception e) {
                return "";
            }
        }
    }
}
