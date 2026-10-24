package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.KhoaBanDo;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.VeDichChuyen;
import com.htbb.pokemon.VeLangMienPhi;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * VE LANG MIEN PHI (So tay -> "Về làng miễn phí (còn N lượt)") + QUA HUY HIEU DAU TIEN (1 Ve dich chuyen, moi nhan vat
 * 1 lan): luot ngay dat lai theo ngay, +luot khi thang Gym, tran luot them, luu qua dang nhap lai / khoi dong lai (ho so
 * dai han), moi dieu cam cua Ve dich chuyen, hoi chieu CHUNG voi ve, chi tru khi da toi, menu So tay giu chi so cu,
 * cau hinh ve_lang.* trong danh muc; qua huy hieu: nhan vat moi (khi thang) / cu (khi vao game), tui day hoan lai sau,
 * hai luong cung luc chi 1 ve. Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemVeLangMienPhi {

    private KiemVeLangMienPhi() {
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

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static final List<int[]> DI = new ArrayList<int[]>();

    static KiemVeDichChuyen.NV nv(final HoSoLauDai h) throws Exception {
        final KiemVeDichChuyen.NV p = KiemVeDichChuyen.nv(30);
        GymKiem.datHoSo(p, h);
        return p;
    }

    static void veBaiLuyen(final Char p) {
        p.map = KiemVeDichChuyen.banDo(9, 9);
        p.mapID = 9;
    }

    public static void chay() {
        final VeDichChuyenGame.DiChuyen diCu = VeDichChuyenGame.DI;
        final VeDichChuyenGame.CoBanDo coCu = VeDichChuyenGame.CO;
        final java.util.Map<Integer, KhoaBanDo.Luat> luatCu = new HashMap<Integer, KhoaBanDo.Luat>(KhoaBanDo.bang());
        final boolean lauDaiCu = LauDai.bat();
        VeDichChuyenGame.DI = new VeDichChuyenGame.DiChuyen() {
            @Override
            public void di(final Char p, final int x, final int y, final int mapId) {
                DI.add(new int[]{mapId, x, y});
                p.map = KiemVeDichChuyen.banDo(mapId, mapId);
                p.mapID = mapId;
            }
        };
        VeDichChuyenGame.CO = new VeDichChuyenGame.CoBanDo() {
            @Override
            public boolean co(final int mapId) {
                return true;
            }
        };
        KhoaBanDo.datBang(new HashMap<Integer, KhoaBanDo.Luat>());
        LauDai.dat(true);
        try {
            luat();
            cauHinh();
            luu();
            soTay();
            dungVeLang();
            cam();
            hoiChieuChung();
            thangGym();
            quaHuyHieu();
        } catch (final Throwable e) {
            e.printStackTrace();
            bang("KiemVeLangMienPhi khong nem loi", "", String.valueOf(e));
        } finally {
            VeDichChuyenGame.DI = diCu;
            VeDichChuyenGame.CO = coCu;
            KhoaBanDo.datBang(luatCu);
            LauDai.dat(lauDaiCu);
            com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, null, false);
        }
    }

    // ------------------------------------------------------------------ luat thuan

    static void luat() {
        final int d1 = 20261024, d2 = 20261025;
        final HoSoLauDai h = new HoSoLauDai();
        bang("nhan vat moi: 1 luot hom nay", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.conLai(h, d1, 1, 3)));
        bang("tru: luot ngay", Integer.valueOf(VeLangMienPhi.LUOT_NGAY), Integer.valueOf(VeLangMienPhi.tru(h, d1, 1, 3)));
        bang("sau khi dung: 0 luot", Integer.valueOf(0), Integer.valueOf(VeLangMienPhi.conLai(h, d1, 1, 3)));
        bang("het luot: tru tra KHONG", Integer.valueOf(VeLangMienPhi.KHONG), Integer.valueOf(VeLangMienPhi.tru(h, d1, 1, 3)));
        bang("het luot: bo dem khong am / khong doi", "20261024/1/0", h.vlNgay + "/" + h.vlDaDung + "/" + h.vlThem);
        bang("ngay moi (lich may chu): lai 1 luot", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.conLai(h, d2, 1, 3)));
        bang("luot ngay khong cong don sang ngay sau", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.conLai(h, 20261030, 1, 3)));
        // thang Gym: +1, tran 3
        bang("thang Gym 1: +1", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.congThangGym(h, 1, 3)));
        bang("thang Gym 2: +1", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.congThangGym(h, 1, 3)));
        bang("thang Gym 3: +1", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.congThangGym(h, 1, 3)));
        bang("thang Gym 4: da toi tran 3 -> +0", Integer.valueOf(0), Integer.valueOf(VeLangMienPhi.congThangGym(h, 1, 3)));
        bang("tran luot them = 3", Integer.valueOf(3), Integer.valueOf(h.vlThem));
        bang("ngay 2: 1 ngay + 3 them = 4", Integer.valueOf(4), Integer.valueOf(VeLangMienPhi.conLai(h, d2, 1, 3)));
        bang("ngay 2: dung luot ngay TRUOC", Integer.valueOf(VeLangMienPhi.LUOT_NGAY), Integer.valueOf(VeLangMienPhi.tru(h, d2, 1, 3)));
        bang("roi luot them", Integer.valueOf(VeLangMienPhi.LUOT_THEM), Integer.valueOf(VeLangMienPhi.tru(h, d2, 1, 3)));
        bang("luot them con 2", Integer.valueOf(2), Integer.valueOf(h.vlThem));
        VeLangMienPhi.hoan(h, d2, VeLangMienPhi.LUOT_THEM);
        bang("hoan luot them", Integer.valueOf(3), Integer.valueOf(h.vlThem));
        VeLangMienPhi.hoan(h, d2, VeLangMienPhi.LUOT_NGAY);
        bang("hoan luot ngay", Integer.valueOf(4), Integer.valueOf(VeLangMienPhi.conLai(h, d2, 1, 3)));
        bang("thang Gym +2 khi con 1 cho truoc tran: chi +1", Integer.valueOf(0), Integer.valueOf(VeLangMienPhi.congThangGym(h, 2, 3)));
        // admin ha tran xuong 1: chi dung duoc 1 luot them
        bang("ha tran 1: luot them dung duoc = 1", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.conThem(h, 1)));
        VeLangMienPhi.tru(h, d2, 0, 1);
        bang("ha tran 1 + 0 luot ngay: tru ve 0 (khong dung phan vuot)", Integer.valueOf(0), Integer.valueOf(VeLangMienPhi.conLai(h, d2, 0, 1)));
        // 2 luot / ngay
        final HoSoLauDai h2 = new HoSoLauDai();
        VeLangMienPhi.tru(h2, d1, 2, 3);
        bang("moi_ngay=2: con 1 sau 1 lan", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.conLai(h2, d1, 2, 3)));
        bang("nhan So tay", "Về làng miễn phí (còn 1 lượt)", VeLangMienPhi.nhan(1));
        bang("thang_gym=0: khong cong", Integer.valueOf(0), Integer.valueOf(VeLangMienPhi.congThangGym(h2, 0, 3)));
    }

    // ------------------------------------------------------------------ cau hinh

    static void cauHinh() {
        com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, null, false);
        bang("mac dinh moi_ngay = 1", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.moiNgay()));
        bang("mac dinh thang_gym = 1", Integer.valueOf(1), Integer.valueOf(VeLangMienPhi.thangGym()));
        bang("mac dinh toi_da = 3", Integer.valueOf(3), Integer.valueOf(VeLangMienPhi.toiDa()));
        for (final String[] k : new String[][]{{VeLangMienPhi.K_MOI_NGAY, "1"}, {VeLangMienPhi.K_THANG_GYM, "1"}, {VeLangMienPhi.K_TOI_DA, "3"}}) {
            final com.htbb.cauhinh.DinhNghiaKhoa d = com.htbb.cauhinh.CauHinhTrungTam.dinhNghia(k[0]);
            dung("danh muc (sql/htbb.sql) co " + k[0], d != null);
            if (d != null) {
                bang(k[0] + ": mac dinh = Java", k[1], d.macDinh);
                dung(k[0] + ": chinh tren web (khong an), nhom tu_dong, ap dung ngay", !d.laAn() && "tu_dong".equals(d.nhom) && d.apDungNgay);
            }
        }
        final java.util.Map<String, String> db = new java.util.HashMap<String, String>();
        db.put(VeLangMienPhi.K_MOI_NGAY, "2");
        db.put(VeLangMienPhi.K_THANG_GYM, "0");
        db.put(VeLangMienPhi.K_TOI_DA, "5");
        com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, db, false);
        bang("cau_hinh 2/0/5 doc dung", "2/0/5", VeLangMienPhi.moiNgay() + "/" + VeLangMienPhi.thangGym() + "/" + VeLangMienPhi.toiDa());
        com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, null, false);
    }

    // ------------------------------------------------------------------ luu

    static void luu() {
        final HoSoLauDai h = new HoSoLauDai();
        VeLangMienPhi.tru(h, 20261024, 1, 3);
        VeLangMienPhi.congThangGym(h, 2, 3);
        h.vlQuaHuyHieu = 1;
        final String s = h.maHoa();
        dung("maHoa co l1..l4", s.contains("l1=20261024") && s.contains("l2=1") && s.contains("l3=2") && s.contains("l4=1"));
        final HoSoLauDai lai = HoSoLauDai.giaiMa(s); // dang nhap lai / khoi dong lai: nap tu tob_char_lau_dai
        bang("nap lai: van het luot ngay hom nay (khong dat lai khi dang nhap lai)", Integer.valueOf(2),
                Integer.valueOf(VeLangMienPhi.conLai(lai, 20261024, 1, 3)));
        bang("nap lai: luot them giu nguyen", Integer.valueOf(2), Integer.valueOf(lai.vlThem));
        bang("nap lai: co qua huy hieu giu nguyen", Integer.valueOf(1), Integer.valueOf(lai.vlQuaHuyHieu));
        bang("ho so cu (khong co l*): 0", "0/0/0/0", HoSoLauDai.giaiMa("gh=1;td=5").vlNgay + "/" + HoSoLauDai.giaiMa("gh=1").vlDaDung
                + "/" + HoSoLauDai.giaiMa("gh=1").vlThem + "/" + HoSoLauDai.giaiMa("gh=1").vlQuaHuyHieu);
        dung("luot da dung ghi ngay (luuNgay) sau khi toi lang", doc("src/real/VeLangMienPhiGame.java").contains("LauDaiNhanVat.luuNgay(p, h); // luot da dung"));
    }

    // ------------------------------------------------------------------ So tay

    static void soTay() throws Exception {
        final HoSoLauDai h = new HoSoLauDai();
        final KiemVeDichChuyen.NV p = nv(h);
        SoTayLauDai.moMenu(p);
        final Object[] m = p.menuCuoi();
        dung("So tay: menu -527 / 0", m != null && ((Integer) m[0]).intValue() == SoTayLauDai.NPC_ID && ((Integer) m[1]).intValue() == 0);
        if (m != null) {
            final String[] d = (String[]) m[2];
            bang("So tay: 11 muc co dinh giu nguyen chi so (client 0.9.15/0.9.16)", java.util.Arrays.asList(SoTayLauDai.MUC_GOC).toString(),
                    java.util.Arrays.asList(d).subList(0, SoTayLauDai.MUC_GOC.length).toString());
            bang("So tay: muc 11 = Về làng miễn phí (còn 1 lượt)", "Về làng miễn phí (còn 1 lượt)", d[SoTayLauDai.MUC_VE_LANG]);
        }
        bang("MUC_VE_LANG = 11 (ngay sau MUC_GOC)", Integer.valueOf(SoTayLauDai.MUC_GOC.length), Integer.valueOf(SoTayLauDai.MUC_VE_LANG));
        bang("huong dan tan thu bat dau tu 12", Integer.valueOf(12), Integer.valueOf(SoTayLauDai.SO_MUC_DONG));
        final String c = doc("../client_jar/src/game/render/screen/GameScr.java");
        if (!c.isEmpty()) {
            // client 0.9.17 them dong "Về làng miễn phí" (-527/0/11) SAU mang SO_TAY_MUC (hang SO_TAY_VE_LANG): mang 11 dong co
            // dinh van khong chep muc dong
            final int a = c.indexOf("SO_TAY_MUC = {");
            final String mang = a < 0 ? "" : c.substring(a, c.indexOf("};", a));
            dung("client SO_TAY_MUC: 11 dong co dinh (khong chep muc dong)", a >= 0 && !mang.contains(VeLangMienPhi.TEN));
            dung("client 0.9.17: dong Ve lang mien phi gui chi so MUC_VE_LANG", !c.contains(VeLangMienPhi.TEN)
                    || c.contains("MUC_VE_LANG = " + SoTayLauDai.MUC_VE_LANG + ";"));
        }
    }

    // ------------------------------------------------------------------ dung

    static void dungVeLang() throws Exception {
        final HoSoLauDai h = new HoSoLauDai();
        final KiemVeDichChuyen.NV p = nv(h);
        p.potions[VeDichChuyen.ID] = 2;
        DI.clear();
        SoTayLauDai.doMenu(p, 0, SoTayLauDai.MUC_VE_LANG);
        bang("So tay muc 11: dich chuyen 1 lan", Integer.valueOf(1), Integer.valueOf(DI.size()));
        bang("toi lang vung minh (cung lang voi Ve dich chuyen)", Integer.valueOf(VeDichChuyenGame.lang(p)[0]), Integer.valueOf(p.mapID));
        bang("cung diem xuat hien voi Ve dich chuyen", java.util.Arrays.toString(VeDichChuyenGame.lang(p)),
                DI.isEmpty() ? "" : java.util.Arrays.toString(DI.get(0)));
        bang("KHONG tru Ve dich chuyen", Integer.valueOf(2), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        bang("tru 1 luot", Integer.valueOf(0), Integer.valueOf(VeLangMienPhiGame.conLai(p)));
        dung("bao ro: da ve mien phi + con 0 luot", p.baoCuoi().startsWith("Đã về ") && p.baoCuoi().contains("miễn phí") && p.baoCuoi().contains("Còn 0 lượt"));
        dung("hoi chieu CHUNG: dat moc Ve dich chuyen", VeDichChuyenGame.LAN_CUOI.containsKey(Integer.valueOf(p.charDBID)));
        // het luot
        veBaiLuyen(p);
        VeDichChuyenGame.quen(p.charDBID);
        DI.clear();
        dung("het luot: khong di", !VeLangMienPhiGame.chon(p));
        bang("het luot: khong dich chuyen", Integer.valueOf(0), Integer.valueOf(DI.size()));
        dung("het luot: bao het luot + goi y thang Gym", p.baoCuoi().startsWith("Hết lượt") && p.baoCuoi().contains("Gym"));
        SoTayLauDai.moMenu(p);
        bang("So tay sau khi dung: con 0 luot", "Về làng miễn phí (còn 0 lượt)", ((String[]) p.menuCuoi()[2])[SoTayLauDai.MUC_VE_LANG]);
        // dang o lang: khong ton luot
        final KiemVeDichChuyen.NV o = nv(new HoSoLauDai());
        o.map = KiemVeDichChuyen.banDo(VeDichChuyenGame.lang(o)[0], VeDichChuyenGame.lang(o)[0]);
        o.mapID = VeDichChuyenGame.lang(o)[0];
        dung("dang o lang: khong di", !VeLangMienPhiGame.chon(o));
        bang("dang o lang: khong tru luot", Integer.valueOf(1), Integer.valueOf(VeLangMienPhiGame.conLai(o)));
        // khong toi noi: hoan luot, bo hoi chieu
        final KiemVeDichChuyen.NV f = nv(new HoSoLauDai());
        final VeDichChuyenGame.DiChuyen di = VeDichChuyenGame.DI;
        VeDichChuyenGame.DI = new VeDichChuyenGame.DiChuyen() {
            @Override
            public void di(final Char c, final int x, final int y, final int mapId) {
                // move2Map tu chan: o lai cho cu
            }
        };
        try {
            dung("khong toi noi: that bai", !VeLangMienPhiGame.chon(f));
            bang("khong toi noi: luot con nguyen", Integer.valueOf(1), Integer.valueOf(VeLangMienPhiGame.conLai(f)));
            dung("khong toi noi: khong tinh hoi chieu", !VeDichChuyenGame.LAN_CUOI.containsKey(Integer.valueOf(f.charDBID)));
            dung("khong toi noi: bao luot con nguyen", f.baoCuoi().contains("vẫn còn nguyên"));
        } finally {
            VeDichChuyenGame.DI = di;
        }
        // luot them (thang Gym) dung sau luot ngay
        final HoSoLauDai h3 = new HoSoLauDai();
        VeLangMienPhi.congThangGym(h3, 1, 3);
        final KiemVeDichChuyen.NV t = nv(h3);
        dung("luot ngay: di duoc", VeLangMienPhiGame.chon(t));
        veBaiLuyen(t);
        VeDichChuyenGame.quen(t.charDBID);
        dung("luot them: di duoc", VeLangMienPhiGame.chon(t));
        bang("da dung ca luot ngay lan luot them", "0/0", VeLangMienPhiGame.conLai(t) + "/" + h3.vlThem);
        // bam dup: goi thu hai khi goi dau dang chay
        final KiemVeDichChuyen.NV b = nv(new HoSoLauDai());
        VeDichChuyenGame.DANG.add(Integer.valueOf(b.charDBID));
        dung("goi thu hai trong luc goi dau dang chay: bo qua", !VeLangMienPhiGame.chon(b));
        bang("goi thu hai: khong tru", Integer.valueOf(1), Integer.valueOf(VeLangMienPhiGame.conLai(b)));
        VeDichChuyenGame.DANG.remove(Integer.valueOf(b.charDBID));
    }

    // ------------------------------------------------------------------ cam

    interface Dat {
        void dat(KiemVeDichChuyen.NV p) throws Exception;
    }

    static void mot(final String ten, final Dat d, final String chu) throws Exception {
        final KiemVeDichChuyen.NV p = nv(new HoSoLauDai());
        d.dat(p);
        p.goi.clear();
        DI.clear();
        dung(ten + ": khong di", !VeLangMienPhiGame.chon(p));
        bang(ten + ": khong dich chuyen", Integer.valueOf(0), Integer.valueOf(DI.size()));
        bang(ten + ": KHONG tru luot", Integer.valueOf(1), Integer.valueOf(VeLangMienPhiGame.conLai(p)));
        dung(ten + ": bao ly do (" + p.baoCuoi() + ")", p.baoCuoi().contains(chu));
    }

    static void cam() throws Exception {
        mot("dang guc", p -> p.hp = 0, "gục");
        final Monster[] boss = new Monster[1];
        mot("dang danh Gym", p -> {
            final Monster b = new Monster((byte) 1);
            b.maxhp = 1000;
            b.hp = 1000;
            b.isDead = false;
            b.map = p.map;
            BossLauDai.datGym(b, 1, p, 600000L);
            boss[0] = b;
        }, "Gym");
        if (boss[0] != null) {
            boss[0].hp = 0;
            boss[0].isDead = true;
        }
        mot("trong pho ban (phong rieng)", p -> {
            com.htbb.pokemon.PhongRieng.dangKyMap(PhongRiengGame.MAP_PHO_BAN);
            p.map = KiemVeDichChuyen.banDo(PhongRiengGame.MAP_PHO_BAN, PhongRiengGame.NAP_PHO_BAN);
            p.mapID = PhongRiengGame.MAP_PHO_BAN;
        }, "phó bản");
        mot("o dau truong PvP (204)", p -> {
            p.map = KiemVeDichChuyen.banDo(204, 204);
            p.mapID = 204;
        }, "PvP");
        mot("dang giao hang", p -> p.monster = (MonsterVantieu) KiemVeDichChuyen.taoKhongGoiHam(MonsterVantieu.class), "giao hàng");
        mot("deo khan PK", p -> p.pk = 14, "PK");
        mot("ten do", p -> p.killer = 5, "PK");
        mot("dang giao dich", p -> p.userTrade.add(new KiemVeDichChuyen.NV(KiemVeDichChuyen.idKe++, 1)), "giao dịch");
        mot("cau bao dung ten tinh nang (khong noi Vé dịch chuyển)", p -> p.pk = 14, VeLangMienPhi.TEN);
    }

    // ------------------------------------------------------------------ hoi chieu chung

    static void hoiChieuChung() throws Exception {
        final HoSoLauDai h = new HoSoLauDai();
        VeLangMienPhi.congThangGym(h, 1, 3);
        final KiemVeDichChuyen.NV p = nv(h);
        p.potions[VeDichChuyen.ID] = 1;
        dung("ve lang mien phi: di duoc", VeLangMienPhiGame.chon(p));
        veBaiLuyen(p);
        dung("ngay sau do: Ve dich chuyen bi hoi chieu", VeDichChuyenGame.lyDoChan(p) != null && VeDichChuyenGame.lyDoChan(p).contains("chờ"));
        dung("ngay sau do: dung ve khong di", !VeDichChuyenGame.chonDiem(p, VeDichChuyenGame.lang(p)));
        bang("ngay sau do: ve con nguyen", Integer.valueOf(1), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        DI.clear();
        dung("ngay sau do: ve lang mien phi bi hoi chieu", !VeLangMienPhiGame.chon(p));
        dung("bao hoi chieu chung", p.baoCuoi().contains("đang hồi") && p.baoCuoi().contains("chờ"));
        bang("hoi chieu: khong tru luot them", Integer.valueOf(1), Integer.valueOf(h.vlThem));
        // chieu nguoc lai: dung ve truoc -> mien phi bi hoi chieu
        VeDichChuyenGame.quen(p.charDBID);
        dung("Ve dich chuyen: di duoc", VeDichChuyenGame.chonDiem(p, VeDichChuyenGame.lang(p)));
        veBaiLuyen(p);
        dung("sau ve: ve lang mien phi bi hoi chieu", !VeLangMienPhiGame.chon(p));
        bang("sau ve: luot them con nguyen", Integer.valueOf(1), Integer.valueOf(h.vlThem));
        VeDichChuyenGame.LAN_CUOI.put(Integer.valueOf(p.charDBID), Long.valueOf(System.currentTimeMillis() - VeDichChuyen.HOI_CHIEU_MS - 1L));
        dung("het 60 giay: di duoc", VeLangMienPhiGame.chon(p));
    }

    // ------------------------------------------------------------------ thang Gym

    static void thangGym() throws Exception {
        final HoSoLauDai h = new HoSoLauDai();
        final KiemVeDichChuyen.NV p = nv(h);
        int[] v = VeLangMienPhiGame.khiThangGym(p, h);
        bang("thang Gym: con 1 ngay + 1 them, vua cong 1", "2/1", v[0] + "/" + v[1]);
        VeLangMienPhiGame.khiThangGym(p, h);
        VeLangMienPhiGame.khiThangGym(p, h);
        v = VeLangMienPhiGame.khiThangGym(p, h);
        bang("thang Gym lan 4: tran 3, cong 0", "4/0", v[0] + "/" + v[1]);
        dung("cau thang Gym: Về làng miễn phí trong Sổ tay (còn N lượt)",
                GymLauDai.veLangNhanh(new int[]{2, 1}).startsWith("Về làng miễn phí trong Sổ tay huấn luyện (còn 2 lượt, +1 nhờ thắng Gym)."));
        dung("cau thang Gym: van goi y Dây thoát hiểm / Vé dịch chuyển", GymLauDai.veLangNhanh(new int[]{0, 0}).contains("Dây thoát hiểm")
                && GymLauDai.veLangNhanh(new int[]{0, 0}).contains(VeDichChuyen.TEN));
        final String g = doc("src/real/GymLauDai.java");
        dung("khiHa: cong luot TRUOC thong bao, ca lan dau lan danh lai", g.indexOf("VeLangMienPhiGame.khiThangGym(p, h)") > 0
                && g.indexOf("VeLangMienPhiGame.khiThangGym(p, h)") < g.indexOf("veLangNhanh(ve)")
                && g.contains("\"Đánh lại Gym thành công: nhận Huy chương Gym. \" + veLangMienPhi(ve)"));
        dung("khiHa: luot cong sau khi da danh dau thang (khiChet lan hai return truoc)",
                g.indexOf("h.ban = true; // danh dau TRUOC, phat SAU") < g.indexOf("VeLangMienPhiGame.khiThangGym(p, h)"));
    }

    // ------------------------------------------------------------------ qua huy hieu dau

    static void quaHuyHieu() throws Exception {
        // chua co huy hieu: khong co gi
        final HoSoLauDai h0 = new HoSoLauDai();
        final KiemVeDichChuyen.NV p0 = nv(h0);
        VeLangMienPhiGame.khiVaoGame(p0);
        bang("chua co huy hieu: khong trao", Integer.valueOf(0), Integer.valueOf(p0.potions[VeDichChuyen.ID]));
        // nhan vat moi: thang Gym dau tien
        final HoSoLauDai h = new HoSoLauDai();
        final KiemVeDichChuyen.NV p = nv(h);
        p.potions[VeDichChuyen.ID] = 3;
        h.gymHuyHieu |= 1;
        final String s = VeLangMienPhiGame.quaHuyHieuDau(p, h);
        bang("huy hieu dau: cau bao", "Nhận thêm 1 Vé dịch chuyển (quà huy hiệu đầu tiên). Dùng trong Hành trang.", s);
        bang("huy hieu dau: +1 ve", Integer.valueOf(4), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        bang("huy hieu dau: co l4 = 1", Integer.valueOf(1), Integer.valueOf(h.vlQuaHuyHieu));
        h.gymHuyHieu |= 2;
        bang("huy hieu thu hai: khong trao them", null, VeLangMienPhiGame.quaHuyHieuDau(p, h));
        VeLangMienPhiGame.khiVaoGame(p);
        bang("vao game lai: khong trao lai", Integer.valueOf(4), Integer.valueOf(p.potions[VeDichChuyen.ID]));
        final HoSoLauDai lai = HoSoLauDai.giaiMa(h.maHoa());
        final KiemVeDichChuyen.NV pl = nv(lai);
        pl.potions[VeDichChuyen.ID] = 4;
        VeLangMienPhiGame.khiVaoGame(pl);
        bang("khoi dong lai (nap ho so): khong trao lai", Integer.valueOf(4), Integer.valueOf(pl.potions[VeDichChuyen.ID]));
        // nhan vat cu da co huy hieu tu truoc: trao bu khi vao game, dung 1 lan
        final HoSoLauDai hc = HoSoLauDai.giaiMa("gh=5");
        final KiemVeDichChuyen.NV c = nv(hc);
        VeLangMienPhiGame.khiVaoGame(c);
        bang("nhan vat cu: vao game nhan 1 ve", Integer.valueOf(1), Integer.valueOf(c.potions[VeDichChuyen.ID]));
        dung("nhan vat cu: co thong bao (ngoai goi tui)", c.goi.size() > c.dem(16));
        VeLangMienPhiGame.khiVaoGame(c);
        bang("nhan vat cu: vao game lan hai khong nhan nua", Integer.valueOf(1), Integer.valueOf(c.potions[VeDichChuyen.ID]));
        dung("vao game: goi tui sau khi trao", c.dem(16) > 0);
        // tui day: khong trao, giu co de trao sau
        final HoSoLauDai hd = HoSoLauDai.giaiMa("gh=1");
        final KiemVeDichChuyen.NV d = nv(hd);
        d.potions[VeDichChuyen.ID] = VeDichChuyen.GIU_TOI_DA;
        final String sd = VeLangMienPhiGame.quaHuyHieuDau(d, hd);
        dung("tui day: bao ro, trao sau", sd != null && sd.contains("tối đa " + VeDichChuyen.GIU_TOI_DA) && sd.contains("khi còn chỗ"));
        bang("tui day: khong vuot " + VeDichChuyen.GIU_TOI_DA, Integer.valueOf(VeDichChuyen.GIU_TOI_DA), Integer.valueOf(d.potions[VeDichChuyen.ID]));
        bang("tui day: co van 0 (trao lai lan sau)", Integer.valueOf(0), Integer.valueOf(hd.vlQuaHuyHieu));
        d.potions[VeDichChuyen.ID] = VeDichChuyen.GIU_TOI_DA - 1;
        VeLangMienPhiGame.khiVaoGame(d);
        bang("con cho: lan dang nhap sau trao", Integer.valueOf(VeDichChuyen.GIU_TOI_DA), Integer.valueOf(d.potions[VeDichChuyen.ID]));
        bang("con cho: co = 1", Integer.valueOf(1), Integer.valueOf(hd.vlQuaHuyHieu));
        // hai luong cung luc: chi 1 ve
        final HoSoLauDai hs = HoSoLauDai.giaiMa("gh=1");
        final KiemVeDichChuyen.NV ps = nv(hs);
        final java.util.concurrent.CountDownLatch xuat = new java.util.concurrent.CountDownLatch(1);
        final List<Thread> ds = new ArrayList<Thread>();
        for (int i = 0; i < 8; i++) {
            final Thread th = new Thread(() -> {
                try {
                    xuat.await();
                    VeLangMienPhiGame.quaHuyHieuDau(ps, hs);
                } catch (final Exception ignored) {
                }
            });
            th.start();
            ds.add(th);
        }
        xuat.countDown();
        for (final Thread th : ds) {
            th.join(5000);
        }
        bang("8 luong cung luc: dung 1 ve", Integer.valueOf(1), Integer.valueOf(ps.potions[VeDichChuyen.ID]));
        // noi vao game
        final String sc = doc("src/real/cmd/SelectCharHandler.java");
        final int iVao = sc.indexOf("real.VeLangMienPhiGame.khiVaoGame(p);");
        final int iTui = sc.indexOf("p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));", Math.max(0, iVao));
        dung("SelectCharHandler: trao bu sau khi nap, ngay TRUOC goi tui", iVao > sc.indexOf("p.initInfoFromDB(charItem, lastLog);")
                && iVao > 0 && iTui > iVao && iTui - iVao < 120);
        final String g = doc("src/real/GymLauDai.java");
        dung("khiHa: qua huy hieu dau o moi tran thang (lan dau / tui day trao lai)", g.contains("VeLangMienPhiGame.quaHuyHieuDau(p, h)"));
    }
}
