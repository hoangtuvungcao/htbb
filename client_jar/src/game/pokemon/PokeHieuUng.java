package game.pokemon;

import game.model.Actor;
import game.model.AttackResult;
import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.SkillManager;
import game.render.Canvas;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Hieu ung chieu kieu Pokemon (viec K3) - ban J2ME cua Unity PokeHieuUng.cs.
 * Bang /pk/hu.bin (HieuUngBang) + anh /pk/hu_&lt;ten&gt;.png, dong goi boi client_jar/tools/DongGoiPokeJ2me.java
 * tu Resources/poke/hu_bang.bytes + hu_*.bytes (khung cat sat hinh, bot khung, PNG bang mau).
 *
 * Char co pokeDex &gt; 0 dung chieu (lop, chieu) co trong bang thi Char.setCurrentSkillByID lay
 * SkillAnimate o day (layChieu) thay hieu ung kiem hiep cu; thieu bang/anh thi null = giu cu.
 * Chieu bo tro (buff): GameScr goi khiNhanBuff - CharBuff cu van giu, them mot luot particle.
 * Ket qua nap (ke ca that bai) duoc nho, khong doc lai jar moi khung hinh.
 */
public final class PokeHieuUng {
    private static Hashtable bang;
    private static final Hashtable cacheChieu = new Hashtable();
    private static final Object KHONG_CO = new Object();

    private PokeHieuUng() {
    }

    private static Integer khoa(int lop, int chieu) {
        return new Integer(lop * 100 + chieu);
    }

    private static void napBang() {
        if (bang != null) {
            return;
        }
        bang = new Hashtable();
        HieuUngBang[] ds = HieuUngBang.doc(PokeBody.docTaiNguyen(PokeBody.THU_MUC + "hu.bin"));
        if (ds == null) {
            return;
        }
        for (int i = 0; i < ds.length; i++) {
            bang.put(khoa(ds[i].lop, ds[i].chieu), ds[i]);
        }
    }

    /** Thong tin hieu ung cua chieu (null neu khong co bang hoac anh). */
    public static HieuUngBang lay(int lop, int chieu) {
        napBang();
        HieuUngBang h = (HieuUngBang) bang.get(khoa(lop, chieu));
        if (h != null && anh(h) != null) {
            return h;
        }
        return null;
    }

    static Image anh(HieuUngBang h) {
        return PokeBody.napAnhChung(PokeBody.THU_MUC + h.ten + ".png");
    }

    /** Ve khung f, TAM tai (x, y). */
    public static void ve(Graphics g, HieuUngBang h, int f, int x, int y, int dir) {
        Image img = anh(h);
        if (img == null) {
            return;
        }
        int[] n = h.nguon(f, dir);
        if (n[0] + h.rong > img.getWidth() || n[1] + h.cao > img.getHeight()) {
            return;
        }
        g.drawRegion(img, n[0], n[1], h.rong, h.cao, 0, x + h.dx, y + h.dy, Graphics.TOP | Graphics.LEFT);
    }

    /** Cho Char.setCurrentSkillByID: SkillAnimate kieu Pokemon, hoac null de giu cach cu. */
    public static SkillAnimate layChieu(Char c, int chieu) {
        try {
            if (c == null || c.pokeDex <= 0) {
                return null;
            }
            Integer k = khoa(c.clazz, chieu);
            Object o = cacheChieu.get(k);
            if (o == KHONG_CO) {
                return null;
            }
            if (o != null) {
                return (SkillAnimate) o;
            }
            HieuUngBang h = lay(c.clazz, chieu);
            SkillPokemon sp = h == null ? null : new SkillPokemon(h);
            cacheChieu.put(k, sp == null ? KHONG_CO : (Object) sp);
            return sp;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Chieu bo tro (buff) vua dinh len actor: CharBuff cu van giu de bao trang thai, them mot luot
     * particle cua chieu tren nguoi actor neu actor la Pokemon. loaiBuff: so trong
     * SkillManager.EFF_BUFF_SKILL (chieu = 4 + vi tri), tim o lop cua actor truoc.
     */
    public static void khiNhanBuff(Actor actor, int loaiBuff) {
        try {
            if (!(actor instanceof Char)) {
                return;
            }
            Char c = (Char) actor;
            if (c.pokeDex <= 0) {
                return;
            }
            byte[][] eff = SkillManager.EFF_BUFF_SKILL;
            int lop = -1, chieu = -1;
            for (int t = -1; t < eff.length && lop < 0; t++) {
                int l = t < 0 ? c.clazz : t;
                if (l < 0 || l >= eff.length) {
                    continue;
                }
                for (int j = 0; j < eff[l].length; j++) {
                    if (eff[l][j] == loaiBuff) {
                        lop = l;
                        chieu = 4 + j;
                        break;
                    }
                }
            }
            if (lop < 0) {
                return;
            }
            HieuUngBang h = lay(lop, chieu);
            if (h != null) {
                EffectManager.addHiEffect(new HieuUngPoke(h, c, null, c.dir));
            }
        } catch (Throwable e) {
            // hieu ung phu: loi thi bo
        }
    }

    /** Thoi gian bay (tick game, 25/giay) - nua so tick 50/giay cua ban Unity. */
    static int thoiGianBay(Actor tu, Actor dich) {
        int dx = dich.x - tu.x, dy = dich.y - tu.y;
        int kc = canBac2(dx * dx + dy * dy);
        int t = kc / 20;
        return t < 2 ? 2 : (t > 10 ? 10 : t);
    }

    static int canBac2(int n) {
        if (n <= 0) {
            return 0;
        }
        int x = n, y = (x + 1) / 2;
        while (y < x) {
            x = y;
            y = (x + n / x) / 2;
        }
        return x;
    }

    /** Mot luot particle dang chay (them vao EffectManager.hiEffects). */
    static final class HieuUngPoke extends Effect {
        /** Do cao tam hieu ung so voi diem chan actor. */
        static final int CAO_THAN = 18;
        private final HieuUngBang h;
        private final Actor theo;
        private final int huong;
        private int tick, x0, y0, x1, y1, tickBay;

        /** Kieu D/T: ve tai actor "dich" (di theo). Kieu B: bay tu "tu" toi "dich". */
        HieuUngPoke(HieuUngBang h, Actor dich, Actor tu, int dir) {
            super();
            this.h = h;
            this.huong = dir;
            this.type = 0;
            theo = dich;
            x = x1 = dich.x;
            y = y1 = dich.y - CAO_THAN;
            if (tu != null && h.kieuVe == HieuUngBang.VE_BAY) {
                x0 = tu.x;
                y0 = tu.y - CAO_THAN;
                tickBay = thoiGianBay(tu, dich);
                x = x0;
                y = y0;
            }
        }

        public void update() {
            tick++;
            if (theo != null) {
                x1 = theo.x;
                y1 = theo.y - CAO_THAN;
            }
            if (tickBay > 0) {
                int t = tick < tickBay ? tick : tickBay;
                x = x0 + (x1 - x0) * t / tickBay;
                y = y0 + (y1 - y0) * t / tickBay;
                // dan bay: lap hoat anh trong luc bay, toi noi thi het
                if (tick >= tickBay) {
                    wantDetroy = true;
                }
            } else {
                x = x1;
                y = y1;
                if (tick >= h.tongTick()) {
                    wantDetroy = true;
                }
            }
        }

        public void paint(Graphics g) {
            try {
                ve(g, h, h.khungTai(tick, tickBay > 0), x, y, huong);
            } catch (Throwable e) {
                wantDetroy = true;
            }
        }
    }

    /**
     * SkillAnimate thay Skill_*_Type* khi nguoi danh la Pokemon: cung nhip trang thai (c.state/p1,
     * frame danh, tru mau bay chu, muc tieu nhay) nhung ve particle PMD. Dung chung giua cac Char
     * nhu SkillManager.SKILL_ANIMATE (trang thai nam trong Char).
     */
    static final class SkillPokemon extends SkillAnimate {
        private final HieuUngBang h;
        /** Muc tieu phu cua chieu dien rong (setMonster/setActorter), xoa sau khi trung. */
        private final Vector mst = new Vector();

        SkillPokemon(HieuUngBang h) {
            super(-1);
            this.h = h;
        }

        public void setMonster(Vector m) {
            mst.removeAllElements();
            if (m == null) {
                return;
            }
            for (int i = 0; i < m.size(); i++) {
                mst.addElement(m.elementAt(i));
            }
        }

        public void setActorter(Vector m) {
            setMonster(m);
        }

        private int tickTrung(Char c) {
            if (h.kieuVe == HieuUngBang.VE_BAY && c.attackTarget != null) {
                return thoiGianBay(c, c.attackTarget);
            }
            return 2;
        }

        public void updateSkill(Char c) {
            if (c == null) {
                return;
            }
            super.updateSkill(c);
            if (c.state == 0) {
                return;
            }
            LiveActor dich = c.attackTarget;
            int trung = tickTrung(c);
            c.frame = (byte) (c.p1 < trung ? 4 : 5);
            c.weapon_frame = (short) 4;
            if (c.p1 == 0 && dich != null) {
                if (h.kieuVe == HieuUngBang.VE_TREN_NGUOI) {
                    EffectManager.addHiEffect(new HieuUngPoke(h, c, null, c.dir));
                } else if (h.kieuVe == HieuUngBang.VE_BAY) {
                    EffectManager.addHiEffect(new HieuUngPoke(h, dich, c, c.dir));
                } else {
                    EffectManager.addHiEffect(new HieuUngPoke(h, dich, null, c.dir));
                }
            }
            if (c.p1 == trung) {
                trungDon(c, dich, false, h);
                for (int i = 0; i < mst.size(); i++) {
                    try {
                        Object o = mst.elementAt(i);
                        if (o instanceof LiveActor && o != dich && !((LiveActor) o).isNPC()) {
                            LiveActor a = (LiveActor) o;
                            if (h.kieuVe != HieuUngBang.VE_TREN_NGUOI) {
                                EffectManager.addHiEffect(new HieuUngPoke(h, a, null, c.dir));
                            }
                            trungDon(c, a, true, h);
                        }
                    } catch (Throwable e) {
                        // bo muc tieu loi
                    }
                }
                mst.removeAllElements();
            }
            if (c.p1 >= trung + 4) {
                c.state = 0;
                c.p1 = c.p2 = c.p3 = 0;
                c.weapon_frame = 0;
                return;
            }
            c.p1++;
        }

        private static void trungDon(Char c, LiveActor a, boolean phu, HieuUngBang h) {
            if (a == null) {
                return;
            }
            // pokemon: phan hoi trung don - hat theo he chieu, chop/lac muc tieu, goi y mau so + tieng
            try {
                int he = PokeChieu.he(h.lop, h.chieu);
                boolean chiMang = c.attkEffect == AttackResult.EFF_CRITICAL || c.attkEffect == AttackResult.EFF_BAO_KICH;
                int heSo = HePoke.heSoVaoDex(he, HieuUngDanh.dexCua(a));
                if (c.attkPower != 0 && c.attkPower != 2000000 && c.attkEffect != AttackResult.EFF_MISS) {
                    HieuUngDanh.trung(a, he, chiMang || heSo >= 150 ? 1 : 0);
                }
                boolean dacBiet = h.kieuVe == HieuUngBang.VE_BAY || (h.chieu > 0 && he != HePoke.THUONG && he != HePoke.GIAC_DAU
                        && he != HePoke.BONG_TOI && he != HePoke.THEP);
                SoBay.goiY(heSo, chiMang, dacBiet ? 1 : 0);
            } catch (Throwable e) {
                // hieu ung phu
            }
            if (a.catagory == 1) {
                ((Monster) a).jump();
            } else if (phu) {
                a.jump();
            }
            a.realHPSyncTime = 2;
            if (Canvas.gameScr == null) {
                return;
            }
            if (c.attkPower != 0 && c.attkPower != 2000000) {
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(c.attkPower).toString(), 0, a.x, a.y - 15, -1, -2);
            }
            if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.length) {
                Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[c.attkEffect], 0, a.x, a.y - 15, 2, -2);
            }
        }

        public void updateSkill(Monster c) {
        }
    }
}
