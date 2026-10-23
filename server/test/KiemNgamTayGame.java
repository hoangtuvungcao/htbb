package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.BanHuong;
import com.htbb.pokemon.TuDong;
import io.Message;
import io.Session;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Phan game cua ngam tay PvP (real.BanHuongGame) tren ban do gia: lenh 6 khoa muc tieu bi tu choi (client 908) / doi
 * thanh ban 4 phia (client 907), lenh -113 trung / truot, tuong, be rong, dan xuyen qua dong minh, bu tre (dat tam vi
 * tri cu cho duong danh cu roi tra lai), hoi chieu / PP tru ca khi truot, spam doi huong, tu dong bi tu choi / dung
 * trong PvP, tat tinh nang (pvp.ngam_tay=0) = nhu 0.9.7.
 */
public final class KiemNgamTayGame {

    private KiemNgamTayGame() {
    }

    /** Lop Kiem nam o goi mac dinh (khong goi duoc tu goi real): KiemNgamTay noi bao cao vao day. */
    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    /** Ban sao toi gian cua Kiem cho goi real. */
    static final class Kiem {
        static void bang(final String m, final Object a, final Object b) {
            BAO.bang(m, a, b);
        }

        static void dung(final String m, final boolean v) {
            BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
        }

        static void sai(final String m, final boolean v) {
            BAO.bang(m, Boolean.FALSE, Boolean.valueOf(v));
        }
    }

    /** Ban do gia: duong danh cu GIA (ghi lai, kiem hoi chieu / tam / PP y nhu MapDanhNhau). */
    static final class MapGia extends Map {
        final java.util.Map<Short, Char> nguoi = new HashMap<Short, Char>();
        final List<String> danh = new ArrayList<String>();
        final Set<Integer> dongMinh = new HashSet<Integer>();

        MapGia(final int id) {
            super();
            initKhoQuaiVaNguoi();
            this.mapId = id;
            this.mapIDLoadMap = id;
            this.w = 40;
            this.type = new int[40 * 40];
        }

        void tuong(final int cot) {
            for (int y = 0; y < 40; y++) {
                this.type[y * 40 + cot] = 2;
            }
        }

        void them(final Char c) {
            this.nguoi.put(Short.valueOf(c.id), c);
            this.allPlayers.get(0).add(c);
            c.map = this;
            c.mapID = this.mapId;
        }

        @Override
        public Char getChar(final short id) {
            return this.nguoi.get(Short.valueOf(id));
        }

        @Override
        protected void doAttackPlayer(final Char p, final Message m) {
            try {
                final short id = m.dis.readShort();
                final int s = m.dis.readByte();
                final Char c = getChar(id);
                if (c == null || this.dongMinh.contains(Integer.valueOf(id))) {
                    return; // luat (to doi / vung an toan...) chan TRUOC moc hoi chieu nhu MapDanhNhau
                }
                final int lv = p.skill[s];
                final long now = System.currentTimeMillis();
                if (now - p.timeLastUseSkills[s] < CharManager.SKILL_COOLDOWN[p.charClass][s][lv] * 100L) {
                    return;
                }
                p.timeLastUseSkills[s] = now;
                if (!Map.inRangeSkill(p, c, CharManager.getSkillRange((byte) s, p.charClass))) {
                    return;
                }
                final int mp = CharManager.SKILL_MP[p.charClass][s][lv];
                if (p.mp < mp) {
                    return;
                }
                p.mp -= mp;
                c.hp -= 100;
                this.danh.add(id + "@" + c.x + "," + c.y);
            } catch (final Exception e) {
                this.danh.add("loi " + e);
            }
        }
    }

    static final List<Integer> LENH = new ArrayList<Integer>();
    static int idKe = 9100;

    static Char nv(final MapGia m, final int ban, final int x, final int y) {
        final Char p = new Char(null);
        final List<Integer> nhan = new ArrayList<Integer>();
        final Session se = new Session(null, null, true) {
            @Override
            public void sendMessage(final Message msg) {
                LENH.add(Integer.valueOf(msg.command));
            }
        };
        p.setSession(se);
        p.getSession().banClient = ban;
        p.id = (short) idKe;
        p.charDBID = idKe++;
        p.userID = p.charDBID;
        p.charname = "ngamtay" + p.charDBID;
        p.isBot = -1;
        p.charClass = 2;
        p.myCountry = 0;
        p.inCountry = 0;
        p.region = 0;
        p.maxhp = 10000;
        p.hp = 10000;
        p.maxmp = 1000;
        p.mp = 1000;
        p.x = x;
        p.y = y;
        p.skill[0] = 1;
        p.skill[1] = 1;
        p.skill[2] = 1;
        p.skill[3] = 1;
        p.skill[4] = 1;
        p.lvDetail.lv = 40;
        m.them(p);
        return p;
    }

    static Message goi6(final int id, final int chieu) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bo);
        d.writeShort(id);
        d.writeByte(chieu);
        return new Message(6, bo.toByteArray());
    }

    static Message goiBan(final int chieu, final int ox, final int oy, final int dx, final int dy) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bo);
        d.writeByte(BanHuong.C_BAN);
        d.writeByte(chieu);
        d.writeShort(ox);
        d.writeShort(oy);
        d.writeShort(dx);
        d.writeShort(dy);
        return new Message(BanHuong.LENH, bo.toByteArray());
    }

    static void hoiChieu(final Char p) {
        for (int i = 0; i < p.timeLastUseSkills.length; i++) {
            p.timeLastUseSkills[i] = 0L;
        }
        BanHuongGame.tt(p).cs.datLaiPhat(); // bo khoang toi thieu 80 ms giua hai phat (test ban lien)
    }

    public static void chay() throws Exception {
        final boolean cuNgay = BanHuongGame.GIAI_NGAY;
        final java.util.function.BiConsumer<Char, String> cuNv = BanHuongGame.NGHI_VAN;
        final List<String> nghiVan = new ArrayList<String>();
        BanHuongGame.GIAI_NGAY = true;
        BanHuongGame.NGHI_VAN = new java.util.function.BiConsumer<Char, String>() {
            @Override
            public void accept(final Char p, final String s) {
                nghiVan.add(s);
            }
        };
        BanHuongGame.datBang(null);
        try {
            CauHinhTrungTam.datChoTest(null, null, false);
            Kiem.dung("pvp.ngam_tay mac dinh bat", BanHuongGame.bat());
            khoaMucTieu();
            clientCu();
            ban();
            tuongRong();
            dongMinhBuTre();
            hoiChieuPp();
            spam(nghiVan);
            tuDong();
            nguCanh();
            khuAnToan();
            mobaChuaMo();
            mepTam();
            tatTinhNang();
        } finally {
            BanHuongGame.GIAI_NGAY = cuNgay;
            BanHuongGame.NGHI_VAN = cuNv;
            CauHinhTrungTam.datChoTest(null, null, false);
            Map.nwar[0] = false;
            Map.getTown[0] = false;
        }
    }

    static void khoaMucTieu() throws Exception {
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 908, 100, 100), b = nv(m, 908, 150, 100);
        LENH.clear();
        final long chanTruoc = BanHuongGame.SO_KHOA_MUC_TIEU_CHAN.get();
        Kiem.dung("client 908: lenh 6 da xu ly (khong qua duong cu)", BanHuongGame.xuLy(m, a, goi6(b.id, 1)));
        Kiem.bang("client 908: lenh 6 khong gay sat thuong (tu choi khoa muc tieu)", "[]", m.danh.toString());
        Kiem.bang("dem lan chan khoa muc tieu", Long.valueOf(chanTruoc + 1), Long.valueOf(BanHuongGame.SO_KHOA_MUC_TIEU_CHAN.get()));
        Kiem.dung("bao bang chu + goi -113 che do PvP", LENH.contains(Integer.valueOf(27)) || LENH.contains(Integer.valueOf(BanHuong.LENH)));
        Kiem.dung("co gang danh nguoi -> nguoi danh vao che do PvP", BanHuongGame.trongPvp(a));
        Kiem.sai("nguoi bi nham (chua trung gi) chua bi coi la PvP", BanHuongGame.trongPvp(b));
        Kiem.sai("lenh khac (9) khong bi BanHuongGame an", BanHuongGame.xuLy(m, a, new Message(9, new byte[]{0, 1, 1})));
    }

    static void clientCu() throws Exception {
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 907, 100, 100), b = nv(m, 907, 160, 104), c = nv(m, 907, 160, 160);
        LENH.clear();
        Kiem.dung("client 907: lenh 6 xu ly bang ban theo huong", BanHuongGame.xuLy(m, a, goi6(b.id, 1)));
        Kiem.bang("client 907: muc tieu thang huong quay (phai) -> trung", "[" + b.id + "@160,104]", m.danh.toString());
        Kiem.dung("client 907: nhac cap nhat 0.9.8 (chu tren dau)", LENH.contains(Integer.valueOf(27)));
        Kiem.sai("client 907: KHONG gui goi -113 (goi moi chi cho 908)", LENH.contains(Integer.valueOf(BanHuong.LENH)));
        hoiChieu(a);
        m.danh.clear();
        b.x = 400; // ra khoi duong ban ngang
        final int mp = a.mp;
        BanHuongGame.xuLy(m, a, goi6(c.id, 1)); // cheo 60/60 -> quay phai (|dx| >= |dy|), dan bay ngang -> truot
        Kiem.bang("client 907: muc tieu cheo -> ban 4 phia khong khoa -> truot", "[]", m.danh.toString());
        Kiem.bang("client 907: truot van mat PP", Integer.valueOf(mp - CharManager.SKILL_MP[2][1][1]), Integer.valueOf(a.mp));
        Kiem.dung("client 907: -113 bi bo qua (khong ngam duoc)", BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0)) && m.danh.isEmpty());
    }

    static void ban() throws Exception {
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 908, 100, 100), b = nv(m, 908, 160, 100);
        final Char xem = nv(m, 908, 120, 140), cu = nv(m, 907, 110, 140);
        LENH.clear();
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("-113 ban phai: trung", "[" + b.id + "@160,100]", m.danh.toString());
        Kiem.dung("trung -> ca hai vao che do PvP", BanHuongGame.trongPvp(a) && BanHuongGame.trongPvp(b));
        Kiem.dung("hieu ung dan bay gui nguoi xung quanh (908)", LENH.contains(Integer.valueOf(BanHuong.LENH)));
        hoiChieu(a);
        m.danh.clear();
        final int mp = a.mp;
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, -1000, 0));
        Kiem.bang("-113 ban trai: truot", "[]", m.danh.toString());
        Kiem.bang("truot: mat PP 1 lan", Integer.valueOf(mp - CharManager.SKILL_MP[2][1][1]), Integer.valueOf(a.mp));
        Kiem.dung("truot: tinh hoi chieu", a.timeLastUseSkills[1] > 0L);
        hoiChieu(a);
        m.danh.clear();
        BanHuongGame.xuLy(m, a, goiBan(1, 300, 300, 1000, 0)); // goc gian lan xa 200 px -> dung vi tri may chu
        Kiem.bang("goc client lech xa: dung vi tri may chu (van trung)", "[" + b.id + "@160,100]", m.danh.toString());
        hoiChieu(a);
        m.danh.clear();
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 0, 0));
        Kiem.bang("huong 0: khong ban", "[]", m.danh.toString());
        Kiem.bang("huong 0: khong mat hoi chieu", Long.valueOf(0), Long.valueOf(a.timeLastUseSkills[1]));
        BanHuongGame.xuLy(m, a, goiBan(4, 100, 100, 1000, 0));
        Kiem.bang("buff (chieu 4) khong ban duoc qua -113", Long.valueOf(0), Long.valueOf(a.timeLastUseSkills[4]));
        Kiem.dung("xem / cu chi la nguoi dung xem", xem != null && cu != null);
    }

    /**
     * Bao cao QA #11 (bot KichBanNgamTay nt_khoang_cach): muc tieu sat mep tam (than cham dau vach ngam, vuot hop vuong
     * canh tam cua duong danh cu &lt;= ban kinh than) truoc "trung" tren mo phong ma khong mat HP. Nay gay sat thuong;
     * vi tri muc tieu tra lai nhu cu; xa hon tam + ban kinh than van truot.
     */
    static void mepTam() throws Exception {
        final int tam = BanHuongGame.thongSo(2, 0).tam;
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 908, 100, 100), b = nv(m, 908, 100 + tam + 6, 100);
        BanHuongGame.xuLy(m, a, goiBan(0, 100, 100, 1000, 0));
        Kiem.bang("mep tam (tam " + tam + " + 6 px): trung, mat HP", "[" + b.id + "@" + (100 + tam) + ",100]", m.danh.toString());
        Kiem.bang("mep tam: vi tri muc tieu tra lai", Integer.valueOf(100 + tam + 6), Integer.valueOf(b.x));
        hoiChieu(a);
        m.danh.clear();
        b.x = 100 + tam + BanHuong.BAN_KINH_THAN + 14;
        BanHuongGame.xuLy(m, a, goiBan(0, 100, 100, 1000, 0));
        Kiem.bang("ngoai tam + ban kinh than: truot", "[]", m.danh.toString());
        hoiChieu(a);
        m.danh.clear();
        b.x = 100 + tam - 10;
        BanHuongGame.xuLy(m, a, goiBan(0, 100, 100, 1000, 0));
        Kiem.bang("trong tam: trung nguyen vi tri", "[" + b.id + "@" + (100 + tam - 10) + ",100]", m.danh.toString());
    }

    static void tuongRong() throws Exception {
        final MapGia m = new MapGia(20);
        m.tuong(8); // x 128..143
        final Char a = nv(m, 908, 100, 100), b = nv(m, 908, 170, 100);
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("tuong giua: khong trung", "[]", m.danh.toString());
        final MapGia m2 = new MapGia(20);
        final Char a2 = nv(m2, 908, 100, 100), b2 = nv(m2, 908, 160, 125), c2 = nv(m2, 908, 160, 180);
        BanHuongGame.xuLy(m2, a2, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("lech 25 px > 20 (nua rong 10 + than 10): truot", "[]", m2.danh.toString());
        b2.y = 118;
        hoiChieu(a2);
        BanHuongGame.xuLy(m2, a2, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("lech 18 px: trung", "[" + b2.id + "@160,118]", m2.danh.toString());
        Kiem.dung("bien b / c2 da dung", b != null && c2 != null);
        // tia xuyen (chieu 2): trung ca hai
        final MapGia m3 = new MapGia(20);
        final Char a3 = nv(m3, 908, 100, 100), d1 = nv(m3, 908, 130, 100), d2 = nv(m3, 908, 170, 104);
        BanHuongGame.xuLy(m3, a3, goiBan(2, 100, 100, 1000, 0));
        Kiem.bang("tia xuyen: trung ca hai theo thu tu", "[" + d1.id + "@130,100, " + d2.id + "@170,104]", m3.danh.toString());
        Kiem.bang("tia xuyen: PP chi tru 1 lan", Integer.valueOf(1000 - CharManager.SKILL_MP[2][2][1]), Integer.valueOf(a3.mp));
    }

    static void dongMinhBuTre() throws Exception {
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 908, 100, 100), dm = nv(m, 908, 130, 100), dich = nv(m, 908, 170, 100);
        m.dongMinh.add(Integer.valueOf(dm.id));
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("dan bay qua dong minh (duong cu khong cho danh) toi ke dich", "[" + dich.id + "@170,100]", m.danh.toString());
        // bu tre (tia tuc thi, chieu 2): b dung (160,100) toi 100 ms truoc, roi chay xuong (160,210) - ngoai tam duong cu
        // (100). Dan bay (chieu 1) thi KHONG: dan toi sau ~125 ms, luc do b da di (cua so tinh theo luc dan toi).
        final MapGia m2 = new MapGia(20);
        final Char a2 = nv(m2, 908, 100, 100), b = nv(m2, 908, 160, 100);
        final long bay = System.currentTimeMillis();
        BanHuongGame.ghiViTri(b, bay - 600);
        BanHuongGame.lichSu(b).ghi(bay - 100, 160, 100);
        BanHuongGame.lichSu(b).ghi(bay - 60, 160, 180);
        BanHuongGame.lichSu(b).ghi(bay - 20, 160, 210);
        b.y = 210;
        BanHuongGame.xuLy(m2, a2, goiBan(2, 100, 100, 1000, 0));
        Kiem.bang("bu tre 150 ms: trung o vi tri cu (duong cu thay vi tri cu)", "[" + b.id + "@160,100]", m2.danh.toString());
        Kiem.bang("bu tre: tra lai vi tri that sau lan danh", "160,210", b.x + "," + b.y);
        // ngoai cua so: chay di 400 ms truoc
        final MapGia m3 = new MapGia(20);
        final Char a3 = nv(m3, 908, 100, 100), c = nv(m3, 908, 160, 100);
        final long b3 = System.currentTimeMillis();
        BanHuongGame.ghiViTri(c, b3 - 900);
        BanHuongGame.lichSu(c).ghi(b3 - 500, 160, 100);
        BanHuongGame.lichSu(c).ghi(b3 - 450, 160, 180);
        BanHuongGame.lichSu(c).ghi(b3 - 400, 160, 200);
        c.y = 200;
        BanHuongGame.xuLy(m3, a3, goiBan(2, 100, 100, 1000, 0));
        Kiem.bang("chay khoi duong dan 400 ms truoc (> 150 ms): truot", "[]", m3.danh.toString());
    }

    static void hoiChieuPp() throws Exception {
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 908, 100, 100), b = nv(m, 908, 160, 100);
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0));
        final int mp = a.mp;
        m.danh.clear();
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("con hoi chieu: phat thu hai bi tu choi", "[]", m.danh.toString());
        Kiem.bang("con hoi chieu: khong tru PP", Integer.valueOf(mp), Integer.valueOf(a.mp));
        hoiChieu(a);
        a.mp = 2;
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("thieu PP: tu choi", "[]", m.danh.toString());
        Kiem.bang("thieu PP: PP giu nguyen", Integer.valueOf(2), Integer.valueOf(a.mp));
        a.mp = 1000;
        a.hp = 0;
        BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0));
        Kiem.bang("da guc: khong ban", "[]", m.danh.toString());
        a.hp = 100;
        a.skill[3] = 0;
        BanHuongGame.xuLy(m, a, goiBan(3, 100, 100, 1000, 0));
        Kiem.bang("chieu chua hoc: khong ban", "[]", m.danh.toString());
        Kiem.dung("b con", b != null);
    }

    static void spam(final List<String> nghiVan) throws Exception {
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 908, 100, 100), b = nv(m, 908, 160, 100);
        nghiVan.clear();
        for (int i = 0; i < 40; i++) {
            final double g = Math.toRadians(i * 37);
            BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, (int) (Math.cos(g) * 1000), (int) (Math.sin(g) * 1000)));
        }
        Kiem.dung("spam doi huong 40 goi: chi 1 phat qua (hoi chieu)", m.danh.size() <= 1);
        Kiem.bang("spam doi huong: ghi nghi van 1 dong / phut", Integer.valueOf(1), Integer.valueOf(nghiVan.size()));
        Kiem.dung("b con", b != null);
    }

    static void tuDong() throws Exception {
        final MapGia m = new MapGia(20);
        final Char a = nv(m, 908, 100, 100);
        a.pk = 1;
        Kiem.dung("co PK: ly do cam tu dong", BanHuongGame.lyDoCamTuDong(a) != null);
        // bat tu dong khi dang PK: bi tu choi bang cau ngam tay (khong phu thuoc tu_dong.tat_khi_pvp)
        CauHinhTrungTam.datChoTest(mm("tu_dong.tat_khi_pvp", "0"), null, true);
        LENH.clear();
        TuDongGame.xuLy(a, new Message(TuDong.LENH, new byte[]{(byte) TuDong.C_BAT}));
        final TuDong.Phien ph = TuDongGame.phien(a, false);
        Kiem.dung("PK + tat_khi_pvp=0: van tu choi bat tu dong", ph == null || !ph.bat);
        Kiem.dung("tu choi: gui trang thai -100", LENH.contains(Integer.valueOf(TuDong.LENH)));
        // dang tu dong (gia lap) roi vao PvP: nhip dung ngay
        a.pk = 0;
        final TuDong.Phien p2 = TuDongGame.phien(a, true);
        p2.bat = true;
        p2.batLuc = System.currentTimeMillis();
        Kiem.dung("ngoai PvP: dang tu dong", TuDongGame.dangTuDong(a));
        BanHuongGame.ghiGiaoChien(a, null, System.currentTimeMillis());
        BanHuongGame.nhip(a);
        Kiem.sai("vua giao chien: nhip dung tu dong", TuDongGame.dangTuDong(a));
        CauHinhTrungTam.datChoTest(null, null, false);
    }

    static java.util.Map<String, String> mm(final String k, final String v) {
        final java.util.Map<String, String> r = new HashMap<String, String>();
        r.put(k, v);
        return r;
    }

    /** Bao cao QA #11: thi tran / lang = khu an toan - khong vao ngam PvP, bao ro "Khu an toàn, không đánh người được." */
    static void khuAnToan() throws Exception {
        final MapGia lang = new MapGia(0); // Pewter: mapIDLoadMap 0 = lang (Map.isMapLang)
        final Char a = nv(lang, 908, 100, 100), b = nv(lang, 908, 116, 100);
        a.pk = 14;
        b.pk = 15;
        Kiem.bang("lang + deo khan PK: khong ngu canh PvP", null, BanHuongGame.lyDoPvp(a, System.currentTimeMillis()));
        Kiem.bang("lang: Tu dong danh khong bi cam vi PK", null, BanHuongGame.lyDoCamTuDong(a));
        LENH.clear();
        BanHuongGame.nhip(a);
        Kiem.dung("lang + PK: nhip bao chu (27) Khu an toan", LENH.contains(Integer.valueOf(27)));
        LENH.clear();
        BanHuongGame.nhip(a);
        Kiem.sai("lang + PK: chi bao mot lan moi ban do", LENH.contains(Integer.valueOf(27)));
        // lenh 6 (bam chon nguoi) trong lang: khong danh, khong ep che do ngam, bao khu an toan
        final int hp = b.hp;
        LENH.clear();
        final java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        final java.io.DataOutputStream d = new java.io.DataOutputStream(bo);
        d.writeShort(b.id);
        d.writeByte(0);
        Kiem.dung("lang: lenh 6 da xu ly", BanHuongGame.xuLy(lang, a, new Message(6, bo.toByteArray())));
        Kiem.bang("lang: lenh 6 khong gay sat thuong", Integer.valueOf(hp), Integer.valueOf(b.hp));
        Kiem.dung("lang: lenh 6 bao chu", LENH.contains(Integer.valueOf(27)));
        Kiem.sai("lang: lenh 6 khong gui che do ngam -113", LENH.contains(Integer.valueOf(BanHuong.LENH)));
        Kiem.bang("cau bao", "Khu an toàn, không đánh người được.", BanHuongGame.BAO_AN_TOAN);
        // ra khoi lang (map thuong) van PK: vao lai PvP
        final MapGia ngoai = new MapGia(20);
        ngoai.them(a);
        Kiem.dung("ra map thuong + PK: PvP", BanHuongGame.lyDoPvp(a, System.currentTimeMillis()) != null);
        a.pk = 0;
        b.pk = 0;
    }

    /** Bao cao QA #10: GM mto vao chien truong MOBA chua co tran -&gt; Map.move2Map tu choi, playerJoin khong nem loi. */
    static void mobaChuaMo() throws Exception {
        MapChienTruongMoba.ALL_REGION.removeAllElements();
        Kiem.sai("MOBA: chua co khu -> coKhuMo false", MapChienTruongMoba.coKhuMo());
        Kiem.sai("chanVao(null) false", MapChienTruongMoba.chanVao(null));
        Kiem.sai("chanVao(map thuong) false", MapChienTruongMoba.chanVao(new MapGia(20)));
        // tao doi tuong khong chay ham dung (ham dung MOBA mo luong + nap tru): Unsafe.allocateInstance qua phan chieu
        final Class<?> lu = Class.forName("sun.misc.Unsafe");
        final java.lang.reflect.Field f = lu.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        final Object u = f.get(null);
        final MapChienTruongMoba moba = (MapChienTruongMoba) lu.getMethod("allocateInstance", Class.class).invoke(u, MapChienTruongMoba.class);
        Kiem.dung("chanVao(MOBA chua co tran) true", MapChienTruongMoba.chanVao(moba));
        final Char p = nv(new MapGia(20), 908, 100, 100);
        p.region = 3;
        try {
            moba.playerJoin(p);
            Kiem.dung("MOBA playerJoin khi chua co khu: khong nem loi", true);
        } catch (final Exception e) {
            Kiem.dung("MOBA playerJoin khi chua co khu: khong nem loi (" + e + ")", false);
        }
        Kiem.dung("cau bao MOBA co chu", MapChienTruongMoba.CHU_CHUA_MO.contains("Chiến trường chưa mở trận"));
    }

    static void nguCanh() throws Exception {
        final MapGia thuong = new MapGia(20);
        final Char a = nv(thuong, 908, 100, 100);
        Kiem.bang("map thuong, khong PK, khong giao chien: khong PvP", null, BanHuongGame.lyDoPvp(a, System.currentTimeMillis()));
        final long bay = System.currentTimeMillis();
        BanHuongGame.ghiGiaoChien(a, null, bay - 9000);
        Kiem.dung("giao chien 9 giay truoc: van PvP", BanHuongGame.lyDoPvp(a, bay) != null);
        BanHuongGame.ghiGiaoChien(a, null, bay - 11000);
        Kiem.bang("giao chien 11 giay truoc: het PvP", null, BanHuongGame.lyDoPvp(a, bay));
        // 0.9.8 + sua-pk: deo khan PK = PvP; thao khan (khong vua danh ai) = het PvP ngay
        a.pk = 14;
        Kiem.dung("deo khan PK: trong PvP", BanHuongGame.lyDoPvp(a, bay) != null);
        final java.lang.reflect.Method dung = Class.forName("real.CoPkGame").getDeclaredMethod("dung", Char.class, int.class);
        dung.setAccessible(true);
        dung.invoke(null, a, Integer.valueOf(14));
        Kiem.bang("thao khan PK: pk = 0", Integer.valueOf(0), Integer.valueOf(a.pk));
        Kiem.bang("thao khan PK: het ngu canh PvP ngay", null, BanHuongGame.lyDoPvp(a, System.currentTimeMillis() + 11000));
        for (final int id : new int[]{202, 204, 205, 227}) {
            final MapGia m = new MapGia(id);
            final Char p = nv(m, 908, 100, 100);
            Kiem.dung("map " + id + ": PvP", BanHuongGame.trongPvp(p));
        }
        final MapGia town = new MapGia(Map.idMapTown);
        final Char t = nv(town, 908, 100, 100);
        Kiem.sai("Cao nguyen 201 luc yen: khong PvP", BanHuongGame.trongPvp(t));
        Map.nwar[0] = true;
        Kiem.dung("Cao nguyen 201 luc chien thanh / chiem Gym: PvP", BanHuongGame.trongPvp(t));
        Map.nwar[0] = false;
        a.isKiller = true;
        Kiem.dung("ten do: PvP", BanHuongGame.trongPvp(a));
        a.isKiller = false;
        BanHuongGame.dangKyNguCanh(new java.util.function.Predicate<Char>() {
            @Override
            public boolean test(final Char p) {
                return p.map != null && p.map.mapId == 555;
            }
        });
        final MapGia cup = new MapGia(555);
        Kiem.dung("ngu canh dang ky them (vd cup tuan): PvP", BanHuongGame.trongPvp(nv(cup, 908, 1, 1)));
        CauHinhTrungTam.datChoTest(mm("pvp.ngam_tay.map", "321"), null, true);
        Kiem.dung("pvp.ngam_tay.map=321: map 321 PvP", BanHuongGame.trongPvp(nv(new MapGia(321), 908, 1, 1)));
        Kiem.sai("pvp.ngam_tay.map=321: map 202 het trong danh sach", BanHuongGame.trongPvp(nv(new MapGia(202), 908, 1, 1)));
        CauHinhTrungTam.datChoTest(null, null, false);
        // nhip gui bang chieu + che do cho client 908, khong gui gi cho client 907
        final MapGia m2 = new MapGia(202);
        final Char moi = nv(m2, 908, 100, 100), cu = nv(m2, 907, 120, 100);
        LENH.clear();
        BanHuongGame.nhip(moi);
        Kiem.dung("nhip client 908: gui -113 (bang + che do)", LENH.contains(Integer.valueOf(BanHuong.LENH)));
        LENH.clear();
        BanHuongGame.nhip(cu);
        Kiem.sai("nhip client 907: khong gui -113", LENH.contains(Integer.valueOf(BanHuong.LENH)));
        Kiem.dung("nhip client 907 vao PvP: nhac cap nhat (chu)", LENH.contains(Integer.valueOf(27)));
        // bang chieu: kieu doc lai dung
        final Message g = BanHuongGame.goiBang(2);
        final DataInputStream d = new DataInputStream(new ByteArrayInputStream(g.toByteArray()));
        d.skipBytes(3); // [lenh][do dai 2 byte]
        final int loai = d.readByte(), n = d.readByte();
        Kiem.dung("goi bang: loai 2, 15-16 chieu", loai == BanHuong.S_BANG && n >= 15);
    }

    static void tatTinhNang() throws Exception {
        CauHinhTrungTam.datChoTest(mm("pvp.ngam_tay", "0"), null, true);
        try {
            final MapGia m = new MapGia(202);
            final Char a = nv(m, 908, 100, 100), b = nv(m, 908, 150, 100);
            Kiem.sai("pvp.ngam_tay=0: tat", BanHuongGame.bat());
            Kiem.sai("tat: lenh 6 di duong cu (khong bi an)", BanHuongGame.xuLy(m, a, goi6(b.id, 1)));
            Kiem.dung("tat: -113 bi bo qua", BanHuongGame.xuLy(m, a, goiBan(1, 100, 100, 1000, 0)) && m.danh.isEmpty());
            Kiem.bang("tat: khong ngu canh PvP", null, BanHuongGame.lyDoCamTuDong(a));
        } finally {
            CauHinhTrungTam.datChoTest(null, null, false);
        }
    }
}
