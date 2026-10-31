import java.util.Vector;
import real.BossDusknoir;
import real.BossLangThang;
import real.Char;
import real.MonsterTemplate;

/**
 * Boss lang thang cu bi dong (quyet dinh chu game 31/10/2026, sau cong kiem bot cb2: Lopunny / Dusknoir 27.000 tan cong
 * dung o Fuchsia 40,40 danh lan 220 px, nguoi di qua guc mot don). Chi danh nguoi da danh no; lan dau danh chi canh bao.
 */
public final class KiemBossLangThang {

    private KiemBossLangThang() {
    }

    public static void chay() {
        Kiem.nhom("Boss lang thang cu bi dong: nguoi dung gan khong bi danh, nguoi danh no thi bi danh");
        BossLangThang.xoaChoKiem();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            logic();
            tichHop();
            nguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            BossLangThang.xoaChoKiem();
            real.Map.gemTemplate = gemCu;
        }
    }

    static MonsterTemplate mau() {
        final MonsterTemplate t = new MonsterTemplate();
        t.id = 113;
        t.name = "Lopunny";
        t.level = 1;
        t.maxhp = 200000000;
        t.attack = 27000;
        t.defend = 25000;
        t.accurate = 690;
        t.dodge = 20;
        return t;
    }

    static Char nv(final int dbid, final short id, final real.Map m, final int x, final int y) {
        final Char p = new Char(null);
        p.charDBID = dbid;
        p.id = id;
        p.isBot = -1;
        p.cat = 0;
        p.map = m;
        p.x = x;
        p.y = y;
        p.hp = 20000;
        p.maxhp = 20000;
        p.charname = "kiemboss" + dbid;
        return p;
    }

    static void logic() {
        final real.Map m = new real.Map();
        final BossDusknoir boss = new BossDusknoir(m, mau(), 640, 640, 0);
        final Char a = nv(9101, (short) 30101, m, 640, 650);
        final Char b = nv(9102, (short) 30102, m, 660, 640);
        Kiem.dung("Lopunny (BossDusknoir) la boss lang thang", BossLangThang.la(boss));
        Kiem.dung("quai thuong khong phai", !BossLangThang.la(new real.Monster(m, mau(), 10, 10, (byte) 0)));
        Kiem.dung("chua ai danh: khong danh ai (A, B)", !BossLangThang.duocDanh(boss, a) && !BossLangThang.duocDanh(boss, b));
        Kiem.dung("A danh lan dau: chi canh bao, khong tinh don", !BossLangThang.khiDanh(boss, a));
        Kiem.dung("lan dau van chua vao danh sach thu", !BossLangThang.duocDanh(boss, a));
        Kiem.dung("A danh lan nua (xac nhan): tinh don", BossLangThang.khiDanh(boss, a));
        Kiem.dung("boss duoc danh A", BossLangThang.duocDanh(boss, a));
        Kiem.dung("boss KHONG danh B (dung gan, chua danh)", !BossLangThang.duocDanh(boss, b));
        a.hp = 0;
        Kiem.dung("A guc: bo khoi danh sach", !BossLangThang.duocDanh(boss, a));
        a.hp = 20000;
        Kiem.dung("A hoi sinh: khong tu vao lai danh sach", !BossLangThang.duocDanh(boss, a));
        Kiem.dung("quai thuong: duocDanh luon true", BossLangThang.duocDanh(new real.Monster(m, mau(), 10, 10, (byte) 0), b));
    }

    /** boss.attack() that: A (da danh) mat mau, B dung canh (trong 220 px) khong mat mau. */
    @SuppressWarnings("unchecked")
    static void tichHop() {
        BossLangThang.xoaChoKiem();
        final real.Map m = new real.Map();
        m.nRegion = 0;
        m.allPlayers = new Vector<Vector<Char>>();
        m.allPlayers.add(new Vector<Char>());
        m.allPlayers.add(new Vector<Char>());
        final BossDusknoir boss = new BossDusknoir(m, mau(), 640, 640, 0);
        boss.inCountry = 0;
        final Char a = nv(9201, (short) 30201, m, 650, 650);
        final Char b = nv(9202, (short) 30202, m, 700, 660);
        boss.region = 0;
        a.region = 0;
        b.region = 0;
        a.inCountry = 0;
        b.inCountry = 0;
        m.allPlayers.get(0).add(a);
        m.allPlayers.get(0).add(b);
        BossLangThang.khiDanh(boss, a);
        BossLangThang.khiDanh(boss, a);
        int hpA = a.hp, hpB = b.hp;
        // boss nham B (vd Region.updateSand gan muc tieu theo khoang cach): khong danh
        boss.target = b;
        boss.lastTimeAttack = 0L;
        boss.attack();
        Kiem.dung("boss nham nguoi dung gan B: khong danh (B " + hpB + " -> " + b.hp + "), bo muc tieu", b.hp == hpB && boss.target == null);
        Throwable loi = null;
        for (int i = 0; i < 6 && a.hp == hpA; i++) {
            boss.target = a;
            boss.lastTimeAttack = 0L;
            try {
                boss.attack();
            } catch (final Throwable t) {
                if (loi == null) {
                    loi = t;
                    t.printStackTrace();
                }
            }
        }
        Kiem.dung("boss danh A (nguoi da danh no): A mat mau (" + hpA + " -> " + a.hp + ", loi " + loi + ")", a.hp < hpA);
        Kiem.dung("don lan cua boss khong trung B o canh A (" + hpB + " -> " + b.hp + ")", b.hp == hpB);
    }

    static void nguon() throws Exception {
        for (final String f : new String[]{"src/real/BossDusknoir.java", "src/real/BossDotKich.java", "src/real/BossSerperior.java",
                "src/real/BossServine.java", "src/data/BossMatQuy.java"}) {
            final String s = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(f)), java.nio.charset.StandardCharsets.UTF_8);
            final int lan = s.split("this\\.near\\(c, 220\\)", -1).length - 1;
            final int chan = s.split("this\\.near\\(c, 220\\) && real\\.BossLangThang\\.duocDanh\\(this, c\\)", -1).length - 1;
            Kiem.dung(f + ": moi don lan (" + lan + ") deu loc duocDanh, attack() bo muc tieu khong hop le", lan > 0 && lan == chan
                    && s.contains("if (this.target != null && !real.BossLangThang.duocDanh(this, this.target)) {"));
        }
    }
}
