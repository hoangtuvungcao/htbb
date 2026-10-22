package game.pokemon;

import game.model.Actor;
import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.MonsterTemplate;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Phan hoi chien dau kieu Pokemon (ban jar): hat theo mau he khi trung don, dan bay theo he cua
 * quai/boss, chop trang + lac nhe muc tieu, rung man hinh (boss danh manh / chi mang), vong canh
 * bao truoc don boss, no tung len cap / tien hoa, quai guc mo dan + khoi, bieu tuong trang thai
 * (doc / bong / te liet / dong bang).
 *
 * Hieu nang (may yeu): moi thu nam trong MANG CO DINH (hat TOI_DA_HAT, dan, vong, xac), khong cap
 * phat moi khung hinh; day thi bo hat moi. Ve bang fillRect/drawLine/fillArc + anh PMD co san
 * (PokeHieuUng). Hook (// pokemon:) o GameScr (danh/bi danh/boss/quai chet/len cap/map, ve +
 * cap nhat cung chu bay), PokeHieuUng (chieu nguoi choi trung), Monster/Char (chop + lac khi ve).
 */
public final class HieuUngDanh {
    private HieuUngDanh() {
    }

    private static int hat = 987654321;

    static int nn(int n) {
        hat = hat * 1103515245 + 12345;
        int v = (hat >>> 9) % n;
        return v < 0 ? -v : v;
    }

    static int tickTu(int luc) {
        int d = Canvas.gameTick - luc;
        if (d < 0) {
            d += 10001;
        }
        return d;
    }

    // ================================================================== he cua quai / chieu

    /** Dex Pokemon cua quai (0 neu khong phai Pokemon). */
    public static int dexQuai(Monster m) {
        if (m == null) {
            return 0;
        }
        return m.dexVe();
    }

    public static int heQuai(Monster m) {
        int dex = dexQuai(m);
        return dex > 0 ? HePoke.he1(dex) : HePoke.THUONG;
    }

    /** Dex cua actor bat ky (Char: pokeDex, quai: template). */
    public static int dexCua(Actor a) {
        if (a instanceof Char) {
            return ((Char) a).pokeDex;
        }
        if (a instanceof Monster) {
            return dexQuai((Monster) a);
        }
        return 0;
    }

    public static boolean laBoss(Monster m) {
        if (m == null) {
            return false;
        }
        return HePoke.laBoss(m.monster_type) || m.maxhp >= 5000000;
    }

    // ================================================================== rung man hinh

    private static int rungCon, rungManh;

    /** Rung man hinh trong t tick, bien do manh px (lay cai lon hon dang co). */
    public static void rung(int t, int manh) {
        if (t > rungCon) {
            rungCon = t;
        }
        if (manh > rungManh || rungCon <= 0) {
            rungManh = manh;
        }
    }

    public static int rungX() {
        if (rungCon <= 0) {
            return 0;
        }
        int a = rungManh * rungCon / (rungCon + 2);
        return (Canvas.gameTick & 1) == 0 ? a : -a;
    }

    public static int rungY() {
        if (rungCon <= 0) {
            return 0;
        }
        int a = rungManh * rungCon / (rungCon + 3);
        return (Canvas.gameTick % 3) == 0 ? a : ((Canvas.gameTick % 3) == 1 ? -a : 0);
    }

    // ================================================================== hat

    static final int K_CHAM = 0, K_TIA = 1, K_VONG = 2, K_SAO = 3, K_KHOI = 4, K_SET = 5, K_LA = 6, K_COT = 7;
    private static final int TOI_DA_HAT = 110;
    private static final int[] hK = new int[TOI_DA_HAT];
    private static final int[] hX = new int[TOI_DA_HAT];  // x16
    private static final int[] hY = new int[TOI_DA_HAT];
    private static final int[] hVX = new int[TOI_DA_HAT];
    private static final int[] hVY = new int[TOI_DA_HAT];
    private static final int[] hG = new int[TOI_DA_HAT];   // gia toc doc x16
    private static final int[] hTuoi = new int[TOI_DA_HAT];
    private static final int[] hMax = new int[TOI_DA_HAT];
    private static final int[] hMau = new int[TOI_DA_HAT];
    private static final int[] hMau2 = new int[TOI_DA_HAT];
    private static final int[] hCo = new int[TOI_DA_HAT];
    private static int soHat;

    static void themHat(int k, int x, int y, int vx16, int vy16, int g16, int tuoiMax, int mau, int mau2, int co) {
        if (soHat >= TOI_DA_HAT) {
            return;
        }
        int i = soHat++;
        hK[i] = k;
        hX[i] = x << 4;
        hY[i] = y << 4;
        hVX[i] = vx16;
        hVY[i] = vy16;
        hG[i] = g16;
        hTuoi[i] = 0;
        hMax[i] = tuoiMax;
        hMau[i] = mau;
        hMau2[i] = mau2;
        hCo[i] = co;
    }

    /** Vectors don vi x16 cho 16 huong (khong dung sin/cos). */
    private static final int[] HX = {16, 15, 11, 6, 0, -6, -11, -15, -16, -15, -11, -6, 0, 6, 11, 15};
    private static final int[] HY = {0, 6, 11, 15, 16, 15, 11, 6, 0, -6, -11, -15, -16, -15, -11, -6};

    static int sang(int mau) {
        int r = (mau >> 16) & 0xFF, g = (mau >> 8) & 0xFF, b = mau & 0xFF;
        r = r + (255 - r) / 2;
        g = g + (255 - g) / 2;
        b = b + (255 - b) / 2;
        return (r << 16) | (g << 8) | b;
    }

    static int toi(int mau) {
        int r = (mau >> 16) & 0xFF, g = (mau >> 8) & 0xFF, b = mau & 0xFF;
        return ((r * 3 / 5) << 16) | ((g * 3 / 5) << 8) | (b * 3 / 5);
    }

    /**
     * Chum hat trung don theo he tai (x, y) (tam than muc tieu). manh: 0 thuong, 1 chi mang /
     * hieu qua cao (nhieu hat hon, vong lon).
     */
    public static void noHe(int he, int x, int y, int manh) {
        int mau = KieuO.mauHe(he);
        int s = sang(mau);
        int n = (manh > 0 ? 14 : 9);
        if (soHat > TOI_DA_HAT - 20) {
            n = n / 2;   // nhieu hat qua: bot
        }
        // vong va cham chung cho moi he
        themHat(K_VONG, x, y, 0, 0, 0, manh > 0 ? 9 : 7, s, mau, manh > 0 ? 3 : 2);
        switch (he) {
            case HePoke.LUA:
                for (int i = 0; i < n; i++) {
                    int h = nn(16);
                    themHat(K_CHAM, x, y, HX[h] * (2 + nn(3)) / 2, HY[h] * (2 + nn(3)) / 2 - 16, -3, 9 + nn(6), nn(2) == 0 ? 0xFFE060 : 0xFF7820, 0xC02000, 2 + nn(2));
                }
                break;
            case HePoke.NUOC:
                for (int i = 0; i < n; i++) {
                    int h = 8 + nn(9);
                    themHat(K_CHAM, x, y, HX[h & 15] * (2 + nn(3)) / 2, -20 - nn(20), 5, 10 + nn(5), 0x9CD8FF, 0x3070D0, 2);
                }
                themHat(K_VONG, x, y + 10, 0, 0, 0, 8, 0xB8E4FF, 0x4080E0, 1);
                break;
            case HePoke.DIEN:
                for (int i = 0; i < 4 + manh * 2; i++) {
                    themHat(K_SET, x, y, HX[nn(16)] * 2, HY[nn(16)] * 2, 0, 5 + nn(3), 0xFFF8A0, 0xF8D030, 10 + nn(8));
                }
                for (int i = 0; i < n / 2; i++) {
                    int h = nn(16);
                    themHat(K_TIA, x, y, HX[h] * 3, HY[h] * 3, 0, 5, 0xFFFFFF, 0xF8D030, 1);
                }
                break;
            case HePoke.CO:
            case HePoke.BO:
                for (int i = 0; i < n; i++) {
                    int h = nn(16);
                    themHat(K_LA, x, y, HX[h] * (1 + nn(3)) / 2, HY[h] * (1 + nn(3)) / 2 - 8, 2, 12 + nn(6), he == HePoke.CO ? 0x90E070 : 0xC8D830, he == HePoke.CO ? 0x3E8A30 : 0x7A8818, 3);
                }
                break;
            case HePoke.BANG:
                for (int i = 0; i < n; i++) {
                    int h = nn(16);
                    themHat(K_TIA, x, y, HX[h] * 2, HY[h] * 2, 0, 8 + nn(4), 0xE8FCFF, 0x88D8E8, 1);
                }
                themHat(K_SAO, x, y, 0, 0, 0, 8, 0xFFFFFF, 0x98D8D8, 5);
                break;
            case HePoke.GIAC_DAU:
            case HePoke.THUONG:
            case HePoke.THEP:
                for (int i = 0; i < n; i++) {
                    int h = nn(16);
                    themHat(K_TIA, x, y, HX[h] * 3, HY[h] * 3, 0, 5 + nn(3), he == HePoke.THEP ? 0xF0F4FF : 0xFFFFFF, mau, 1);
                }
                themHat(K_SAO, x, y, 0, 0, 0, 5, 0xFFFFFF, mau, manh > 0 ? 9 : 6);
                break;
            case HePoke.DOC:
            case HePoke.MA:
                for (int i = 0; i < n; i++) {
                    themHat(K_KHOI, x - 8 + nn(17), y - 4 + nn(9), (nn(9) - 4) * 2, -10 - nn(12), -1, 12 + nn(6), s, mau, 2 + nn(3));
                }
                break;
            case HePoke.DAT:
            case HePoke.DA:
                for (int i = 0; i < n; i++) {
                    int h = 8 + nn(9);
                    themHat(K_CHAM, x, y + 4, HX[h & 15] * (2 + nn(3)) / 2, -24 - nn(18), 6, 10 + nn(5), s, toi(mau), 2 + nn(3));
                }
                themHat(K_KHOI, x, y + 10, 0, 0, 0, 10, sang(s), mau, 6);
                break;
            case HePoke.BAY:
                for (int i = 0; i < 3; i++) {
                    themHat(K_VONG, x, y, 0, 0, 0, 6 + i * 2, 0xF0F0FF, 0xA890F0, 1);
                }
                for (int i = 0; i < n / 2; i++) {
                    int h = nn(16);
                    themHat(K_TIA, x, y, HX[h] * 3, HY[h], 0, 6, 0xFFFFFF, mau, 1);
                }
                break;
            case HePoke.SIEU_LINH:
            case HePoke.TIEN:
                themHat(K_VONG, x, y, 0, 0, 0, 10, s, mau, 1);
                themHat(K_VONG, x, y, 0, 0, 0, 13, s, mau, 1);
                for (int i = 0; i < n / 2 + 2; i++) {
                    int h = nn(16);
                    themHat(K_SAO, x, y, HX[h] * 3 / 2, HY[h] * 3 / 2, 0, 10 + nn(5), 0xFFFFFF, mau, 2);
                }
                break;
            default:   // rong, bong toi...
                for (int i = 0; i < n; i++) {
                    int h = nn(16);
                    themHat(K_TIA, x, y, HX[h] * 3, HY[h] * 3, 0, 6 + nn(3), s, mau, 1);
                }
                themHat(K_KHOI, x, y, 0, 0, 0, 7, s, mau, 5);
                break;
        }
    }

    // ================================================================== chop / lac muc tieu

    /** Muc tieu bi trung: chop trang + lac (LiveActor.pokeTrungLuc), them chum hat theo he. */
    public static void trung(LiveActor a, int he, int manh) {
        if (a == null) {
            return;
        }
        a.pokeTrungLuc = Canvas.gameTick;
        int cao = a.height > 10 ? a.height / 2 : 14;
        noHe(he, a.x, a.y - cao, manh);
        if (manh > 0) {
            rung(5, 2);
        }
    }

    /**
     * Nguoi choi (nhan vat chinh) danh trung quai - goi GameScr.onPlayerAttackMonster: goi y mau so
     * (khac he uoc luong o client khi server chua gui -95), chop/lac + hat theo he chieu.
     */
    public static void nguoiDanh(Char c, LiveActor dich, int chieu, int hieuUng, int sat) {
        try {
            if (c == null || dich == null || c.pokeDex <= 0) {
                return;
            }
            int he = PokeChieu.he(c.clazz, chieu);
            boolean chiMang = hieuUng == game.model.AttackResult.EFF_CRITICAL || hieuUng == game.model.AttackResult.EFF_BAO_KICH;
            int heSo = hieuUng == game.model.AttackResult.EFF_HIEU_QUA_CAO ? 150 : HePoke.heSoVaoDex(he, dexCua(dich));
            SoBay.goiY(heSo, chiMang, (c.clazz == 2 || c.clazz == 4) ? 1 : 0);
            if (sat > 0 && tickTu(dich.pokeTrungLuc) > 2) {
                trung(dich, he, chiMang || heSo >= 150 ? 1 : 0);
            }
        } catch (Throwable e) {
            // bo qua
        }
    }

    // ================================================================== dan bay (quai danh xa)

    private static final int TOI_DA_DAN = 10;
    private static final LiveActor[] dTu = new LiveActor[TOI_DA_DAN];
    private static final LiveActor[] dDich = new LiveActor[TOI_DA_DAN];
    private static final int[] dX0 = new int[TOI_DA_DAN];
    private static final int[] dY0 = new int[TOI_DA_DAN];
    private static final int[] dX = new int[TOI_DA_DAN];
    private static final int[] dY = new int[TOI_DA_DAN];
    private static final int[] dTick = new int[TOI_DA_DAN];
    private static final int[] dBay = new int[TOI_DA_DAN];
    private static final int[] dHe = new int[TOI_DA_DAN];
    private static final int[] dSat = new int[TOI_DA_DAN];
    private static final int[] dManh = new int[TOI_DA_DAN];
    private static final HieuUngBang[] dAnh = new HieuUngBang[TOI_DA_DAN];
    private static final boolean[] dSong = new boolean[TOI_DA_DAN];

    /** Anh PMD cho tung he: [he] = lop*100+chieu trong hu.bin, -1 = ve bang ma. */
    private static final short[] PMD_GAN = {100, 2, 306, 401, 106, -1, 201, -1, -1, 0, -1, 0, 100, 7, 7, 202, 208, -1};
    private static final short[] PMD_XA = {203, 1, 302, -1, 102, 103, -1, 103, 103, -1, -1, 102, 103, -1, -1, -1, -1, 203};

    private static HieuUngBang pmd(short[] bang, int he) {
        if (he < 0 || he >= bang.length || bang[he] < 0) {
            return null;
        }
        try {
            return PokeHieuUng.lay(bang[he] / 100, bang[he] % 100);
        } catch (Throwable e) {
            return null;
        }
    }

    private static int caoThan(LiveActor a) {
        return a.height > 10 ? a.height / 2 : 14;
    }

    /**
     * Quai (Pokemon) danh nguoi choi: gan -> no PMD + hat theo he tai muc tieu; xa -> dan bay
     * theo he, toi noi moi hien so sat thuong (satThuong &lt; 0 = da hien roi). true = da lo hieu
     * ung (ma cu khong can tao hieu ung chung).
     */
    public static boolean quaiDanh(Monster m, LiveActor dich, int satThuong, boolean xa) {
        try {
            if (m == null || dich == null || dexQuai(m) <= 0) {
                return false;
            }
            int he = heQuai(m);
            boolean boss = laBoss(m);
            if (!xa) {
                anTrung(m, dich, he, boss ? 1 : 0);
                return true;
            }
            int i = -1;
            for (int k = 0; k < TOI_DA_DAN; k++) {
                if (!dSong[k]) {
                    i = k;
                    break;
                }
            }
            if (i < 0) {
                return false;
            }
            dSong[i] = true;
            dTu[i] = m;
            dDich[i] = dich;
            dX0[i] = m.x;
            dY0[i] = m.y - caoThan(m);
            dX[i] = dX0[i];
            dY[i] = dY0[i];
            dTick[i] = 0;
            dBay[i] = PokeHieuUng.thoiGianBay(m, dich) + 1;
            dHe[i] = he;
            dSat[i] = satThuong;
            dManh[i] = boss ? 1 : 0;
            dAnh[i] = pmd(PMD_XA, he);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static void anTrung(LiveActor tu, LiveActor dich, int he, int manh) {
        trung(dich, he, manh);
        HieuUngBang h = pmd(PMD_GAN, he);
        if (h != null && EffectManager.hiEffects.size() < 30) {
            EffectManager.addHiEffect(new PokeHieuUng.HieuUngPoke(h, dich, null, tu != null ? tu.dir : 0));
        }
    }

    private static void capNhatDan() {
        for (int i = 0; i < TOI_DA_DAN; i++) {
            if (!dSong[i]) {
                continue;
            }
            LiveActor d = dDich[i];
            dTick[i]++;
            int x1 = d.x, y1 = d.y - caoThan(d);
            int t = dTick[i], T = dBay[i];
            if (t > T) {
                t = T;
            }
            dX[i] = dX0[i] + (x1 - dX0[i]) * t / T;
            // vong cung nhe
            int vong = (t * (T - t) * 16) / (T * T);
            dY[i] = dY0[i] + (y1 - dY0[i]) * t / T - vong;
            // vet duoi
            if ((dTick[i] & 1) == 0) {
                int mau = KieuO.mauHe(dHe[i]);
                themHat(K_CHAM, dX[i], dY[i], 0, -4, 0, 6, sang(mau), mau, 2);
            }
            if (dTick[i] >= dBay[i]) {
                dSong[i] = false;
                anTrung(dTu[i], d, dHe[i], dManh[i]);
                if (dSat[i] >= 0 && Canvas.gameScr != null) {
                    if (dSat[i] == 0) {
                        Canvas.gameScr.startFlyText("MISS", 0, d.x, d.y - 40, 0, -1);
                    } else {
                        Canvas.gameScr.startFlyText(new StringBuffer("-").append(dSat[i]).toString(), 4, d.x, d.y - 40, 0, -1);
                    }
                }
                dTu[i] = null;
                dDich[i] = null;
                dAnh[i] = null;
            }
        }
    }

    private static void veDan(Graphics g) {
        for (int i = 0; i < TOI_DA_DAN; i++) {
            if (!dSong[i]) {
                continue;
            }
            int x = dX[i], y = dY[i];
            HieuUngBang h = dAnh[i];
            if (h != null) {
                int huong = dTu[i] != null ? dTu[i].dir : 0;
                PokeHieuUng.ve(g, h, h.khungTai(dTick[i], true), x, y, huong);
                continue;
            }
            int mau = KieuO.mauHe(dHe[i]);
            int r = dManh[i] > 0 ? 6 : 4;
            g.setColor(toi(mau));
            g.fillArc(x - r - 1, y - r - 1, 2 * r + 2, 2 * r + 2, 0, 360);
            g.setColor(mau);
            g.fillArc(x - r, y - r, 2 * r, 2 * r, 0, 360);
            g.setColor(sang(mau));
            g.fillArc(x - r / 2 - 1, y - r / 2 - 1, r, r, 0, 360);
            g.setColor(0xFFFFFF);
            g.fillRect(x - 1, y - 2, 2, 2);
        }
    }

    // ================================================================== canh bao boss

    private static final int TOI_DA_CANH = 6;
    private static final LiveActor[] cDich = new LiveActor[TOI_DA_CANH];
    private static final int[] cTick = new int[TOI_DA_CANH];
    private static final int[] cMax = new int[TOI_DA_CANH];
    private static final int[] cHe = new int[TOI_DA_CANH];
    private static final boolean[] cSong = new boolean[TOI_DA_CANH];
    private static int canhBaoLuc = -100;

    /**
     * Boss ra don (GameScr.onBossAttackPlayer): vong do nhap nhay duoi chan tung muc tieu, roi no
     * theo he boss + rung man hinh (server gui don ngay luc danh nen canh bao chay luc ra don).
     */
    public static void bossDanh(Monster boss, Vector dsDich) {
        try {
            if (boss == null || dsDich == null) {
                return;
            }
            int he = heQuai(boss);
            for (int k = 0; k < dsDich.size(); k++) {
                Object o = dsDich.elementAt(k);
                if (!(o instanceof LiveActor)) {
                    continue;
                }
                for (int i = 0; i < TOI_DA_CANH; i++) {
                    if (!cSong[i]) {
                        cSong[i] = true;
                        cDich[i] = (LiveActor) o;
                        cTick[i] = 0;
                        cMax[i] = 10;
                        cHe[i] = he;
                        break;
                    }
                }
            }
            if (tickTu(canhBaoLuc) > 8) {
                canhBaoLuc = Canvas.gameTick;
                AmThanh.phat(AmThanh.CANH_BAO);
            }
            // tia nap nang luong quanh boss
            int cao = caoThan(boss);
            int mau = KieuO.mauHe(he);
            for (int i = 0; i < 6; i++) {
                int h = nn(16);
                themHat(K_CHAM, boss.x + HX[h] * 2, boss.y - cao + HY[h] * 2, -HX[h], -HY[h], 0, 7, sang(mau), mau, 2);
            }
        } catch (Throwable e) {
            // bo qua
        }
    }

    private static void capNhatCanh() {
        for (int i = 0; i < TOI_DA_CANH; i++) {
            if (!cSong[i]) {
                continue;
            }
            cTick[i]++;
            if (cTick[i] >= cMax[i]) {
                cSong[i] = false;
                LiveActor d = cDich[i];
                cDich[i] = null;
                if (d != null) {
                    anTrung(null, d, cHe[i], 1);
                    rung(8, 3);
                }
            }
        }
    }

    private static void veCanh(Graphics g) {
        for (int i = 0; i < TOI_DA_CANH; i++) {
            if (!cSong[i] || cDich[i] == null) {
                continue;
            }
            LiveActor d = cDich[i];
            int t = cTick[i];
            int r = 22 - t;
            if (r < 8) {
                r = 8;
            }
            boolean sangLen = (t & 2) == 0;
            g.setColor(sangLen ? 0xFF3020 : 0xFFB040);
            g.drawArc(d.x - r, d.y - r / 2, 2 * r, r, 0, 360);
            g.drawArc(d.x - r + 1, d.y - r / 2 + 1, 2 * r - 2, r - 2, 0, 360);
            g.setColor(0xFF6040);
            g.drawArc(d.x - 24, d.y - 12, 48, 24, 0, 360);
            // dau "!" tren dau
            int yt = d.y - (d.height > 10 ? d.height : 30) - 14;
            if (sangLen) {
                g.setColor(0x101420);
                g.fillRect(d.x - 3, yt - 1, 6, 12);
                g.setColor(0xFFE040);
                g.fillRect(d.x - 2, yt, 4, 6);
                g.fillRect(d.x - 2, yt + 8, 4, 2);
            }
        }
    }

    // ================================================================== quai guc

    private static final int TOI_DA_XAC = 6;
    private static final int[] xDex = new int[TOI_DA_XAC];
    private static final boolean[] xShiny = new boolean[TOI_DA_XAC];
    private static final int[] xTiLe = new int[TOI_DA_XAC];
    private static final int[] xX = new int[TOI_DA_XAC];
    private static final int[] xY = new int[TOI_DA_XAC];
    private static final int[] xHuong = new int[TOI_DA_XAC];
    private static final int[] xTick = new int[TOI_DA_XAC];
    private static final boolean[] xSong = new boolean[TOI_DA_XAC];
    private static final int TUOI_XAC = 16;

    /** Quai Pokemon guc (hp ve 0): than guc nga nhap nhay mo dan roi "bup" khoi + sao. */
    public static void quaiChet(Monster m) {
        try {
            if (m == null || m.state == 5 || m.state == 1 || m.state == 8) {
                return;   // da guc roi
            }
            int t = m.monster_type;
            int dex = dexQuai(m);
            int cao = caoThan(m);
            if (dex > 0) {
                for (int i = 0; i < TOI_DA_XAC; i++) {
                    if (!xSong[i]) {
                        xSong[i] = true;
                        xDex[i] = dex;
                        xShiny[i] = m.shinyVe();
                        xTiLe[i] = m.tiLeVe();
                        xX[i] = m.x;
                        xY[i] = m.y;
                        xHuong[i] = m.dir;
                        xTick[i] = 0;
                        break;
                    }
                }
            }
            if (laBoss(m)) {
                rung(14, 4);
            }
            AmThanh.phat(AmThanh.CHET);
        } catch (Throwable e) {
            // bo qua
        }
    }

    private static void bupKhoi(int x, int y, int to) {
        for (int i = 0; i < 7 + to * 3; i++) {
            int h = nn(16);
            themHat(K_KHOI, x + HX[h] / 2, y + HY[h] / 3, HX[h] * (1 + to), HY[h] / 2 - 6, -1, 10 + nn(5), 0xF4F4F8, 0xA8ACB8, 3 + nn(3) + to);
        }
        for (int i = 0; i < 4; i++) {
            int h = nn(16);
            themHat(K_SAO, x, y, HX[h] * 2, HY[h] * 2 - 8, 1, 10, 0xFFFFFF, 0xFFE060, 3);
        }
    }

    private static void capNhatXac() {
        for (int i = 0; i < TOI_DA_XAC; i++) {
            if (!xSong[i]) {
                continue;
            }
            xTick[i]++;
            if (xTick[i] == TUOI_XAC - 3) {
                bupKhoi(xX[i], xY[i] - 12, xTiLe[i] > 140 ? 2 : 0);
            }
            if (xTick[i] >= TUOI_XAC) {
                xSong[i] = false;
            }
        }
    }

    private static void veXac(Graphics g) {
        for (int i = 0; i < TOI_DA_XAC; i++) {
            if (!xSong[i]) {
                continue;
            }
            int t = xTick[i];
            if (t >= TUOI_XAC - 3) {
                continue;
            }
            // nhap nhay nhanh dan = mo dan
            if (t > 4 && (t % (t > 9 ? 2 : 3)) == 0) {
                continue;
            }
            try {
                PokeBody.paint(g, xDex[i], xShiny[i], xX[i], xY[i] - t / 3, xHuong[i], PokeDuLieu.GUC_NGA, t * 2, xTiLe[i], t < 3 ? 120 : 0);
            } catch (Throwable e) {
                xSong[i] = false;
            }
        }
    }

    // ================================================================== len cap / tien hoa

    private static final int TOI_DA_NO = 4;
    private static final LiveActor[] nAi = new LiveActor[TOI_DA_NO];
    private static final int[] nTick = new int[TOI_DA_NO];
    private static final int[] nMax = new int[TOI_DA_NO];
    private static final boolean[] nTienHoa = new boolean[TOI_DA_NO];

    private static void themNo(LiveActor a, boolean tienHoa) {
        for (int i = 0; i < TOI_DA_NO; i++) {
            if (nAi[i] == null || nAi[i] == a) {
                nAi[i] = a;
                nTick[i] = 0;
                nMax[i] = tienHoa ? 60 : 30;
                nTienHoa[i] = tienHoa;
                return;
            }
        }
    }

    public static void lenCap(Char c) {
        try {
            if (c == null) {
                return;
            }
            themNo(c, false);
            int cao = caoThan(c);
            themHat(K_VONG, c.x, c.y - cao, 0, 0, 0, 12, 0xFFF4A0, 0xFFC820, 3);
            for (int i = 0; i < 12; i++) {
                int h = nn(16);
                themHat(K_SAO, c.x + HX[h], c.y - 4, HX[h] / 2, -16 - nn(16), 0, 16 + nn(8), 0xFFFFFF, 0xFFD040, 2 + nn(2));
            }
            if (c == mainChar()) {
                AmThanh.phat(AmThanh.LEN_CAP);
            }
        } catch (Throwable e) {
            // bo qua
        }
    }

    public static void tienHoa(Char c) {
        try {
            if (c == null) {
                return;
            }
            themNo(c, true);
            rung(10, 2);
            AmThanh.phat(AmThanh.TIEN_HOA);
        } catch (Throwable e) {
            // bo qua
        }
    }

    private static Char mainChar() {
        return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
    }

    private static void capNhatNo() {
        for (int i = 0; i < TOI_DA_NO; i++) {
            LiveActor a = nAi[i];
            if (a == null) {
                continue;
            }
            int t = ++nTick[i];
            if (t >= nMax[i]) {
                nAi[i] = null;
                if (nTienHoa[i]) {
                    // ket thuc tien hoa: no lon
                    int cao = caoThan(a);
                    themHat(K_VONG, a.x, a.y - cao, 0, 0, 0, 14, 0xFFFFFF, 0xC8E0FF, 4);
                    for (int k = 0; k < 16; k++) {
                        int h = k;
                        themHat(K_SAO, a.x, a.y - cao, HX[h] * 3, HY[h] * 3, 0, 14, 0xFFFFFF, KieuO.mauHe(k % 18), 3);
                    }
                }
                continue;
            }
            int cao = caoThan(a);
            if (nTienHoa[i]) {
                // hat sang hut vao than + vong nhip
                if ((t & 1) == 0) {
                    int h = nn(16);
                    themHat(K_CHAM, a.x + HX[h] * 3 / 2, a.y - cao + HY[h] * 3 / 2, -HX[h] * 3 / 8, -HY[h] * 3 / 8, 0, 8, 0xFFFFFF, 0xA0D0FF, 2);
                }
                if (t % 12 == 0) {
                    themHat(K_VONG, a.x, a.y - cao, 0, 0, 0, 10, 0xE8F4FF, 0x80B0F0, 2);
                }
            } else if ((t & 3) == 0 && t < 20) {
                themHat(K_SAO, a.x - 12 + nn(25), a.y - nn(8), 0, -18, 0, 12, 0xFFFFFF, 0xFFE060, 2);
            }
        }
    }

    private static void veNo(Graphics g) {
        for (int i = 0; i < TOI_DA_NO; i++) {
            LiveActor a = nAi[i];
            if (a == null) {
                continue;
            }
            int t = nTick[i];
            int cao = a.height > 10 ? a.height : 30;
            if (!nTienHoa[i]) {
                // cot sang vang tu chan len (len cap), mo dan
                if (t < 18) {
                    int w = 18 - t;
                    int h = cao + 30;
                    g.setColor(0xFFF0A0);
                    g.drawLine(a.x - w, a.y, a.x - w / 2, a.y - h);
                    g.drawLine(a.x + w, a.y, a.x + w / 2, a.y - h);
                    g.setColor(0xFFFFFF);
                    g.drawLine(a.x - w / 3, a.y, a.x, a.y - h - 6);
                    g.drawLine(a.x + w / 3, a.y, a.x, a.y - h - 6);
                }
            } else {
                // tien hoa: hao quang trang quanh than
                int r = 18 + (t % 8);
                g.setColor((t & 2) == 0 ? 0xFFFFFF : 0xB8D8FF);
                g.drawArc(a.x - r, a.y - cao / 2 - r, 2 * r, 2 * r, 0, 360);
            }
        }
    }

    // ================================================================== trang thai

    /** Muc tieu bi sat thuong theo thoi gian (bong) - GameScr.onBuffAttack loai -1. */
    public static void biBong(Actor a) {
        if (a instanceof LiveActor) {
            ((LiveActor) a).pokeBongLuc = Canvas.gameTick;
        }
    }

    private static void veTrangThai(Graphics g) {
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return;
        }
        Vector ds = gs.actors;
        int n = ds.size();
        long bay = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            Object o = ds.elementAt(i);
            if (!(o instanceof LiveActor)) {
                continue;
            }
            LiveActor a = (LiveActor) o;
            if (a.hp <= 0 || a.x < GameScr.cmx - 20 || a.x > GameScr.cmx + Canvas.w + 20 || a.y < GameScr.cmy || a.y > GameScr.cmy + Canvas.h + 40) {
                continue;
            }
            if (!(a instanceof Char) && !(a instanceof Monster)) {
                continue;
            }
            int x = a.x + 9;
            int y = a.y - (a.height > 10 ? a.height : 30) + 2;
            if (a.poinson > 0 && a.tDelay > 0) {
                huyHieu(g, x, y, 0xA040A0, 0);
                y += 11;
            }
            if (a.pokeBongLuc >= 0 && tickTu(a.pokeBongLuc) < 50) {
                huyHieu(g, x, y, 0xF08030, 1);
                y += 11;
            }
            if (a.beStune && bay < a.timeBeStune) {
                huyHieu(g, x, y, 0xF8D030, 2);
                y += 11;
            }
            if (a.isFreeze) {
                huyHieu(g, x, y, 0x98D8D8, 3);
                y += 11;
            }
            // trang thai server gui (-97): 1 bong, 2 doc, 3 te liet, 4 ngu, 5 dong bang
            int tt = a.pokeTT > 0 && bay < a.pokeTTHet ? a.pokeTT : 0;
            if (tt > 0) {
                huyHieu(g, x, y, MAU_TT[tt], LOAI_TT[tt]);
                if (a.pokeTTChong > 1) {
                    g.setColor(0xFFFFFF);
                    g.drawString(String.valueOf(a.pokeTTChong), x + 11, y - 1, Graphics.TOP | Graphics.LEFT);
                }
            }
            veBoss(g, a, a.x, a.y - (a.height > 10 ? a.height : 30));
        }
    }

    private static final int[] MAU_TT = {0, 0xF08030, 0xA040A0, 0xF8D030, 0x8890B8, 0x98D8D8};
    private static final int[] LOAI_TT = {0, 1, 0, 2, 4, 3};

    /** Huy hieu tron 10px: 0 doc (bong bong), 1 bong (lua), 2 te liet (set), 3 dong bang (bong tuyet), 4 ngu (Z). */
    private static void huyHieu(Graphics g, int x, int y, int mau, int loai) {
        g.setColor(0x101420);
        g.fillArc(x - 1, y - 1, 11, 11, 0, 360);
        g.setColor(mau);
        g.fillArc(x, y, 9, 9, 0, 360);
        g.setColor(0xFFFFFF);
        switch (loai) {
            case 0:
                g.fillRect(x + 2, y + 4, 2, 2);
                g.fillRect(x + 5, y + 2, 2, 2);
                g.fillRect(x + 5, y + 6, 1, 1);
                break;
            case 1:
                g.fillRect(x + 4, y + 2, 1, 5);
                g.fillRect(x + 3, y + 4, 3, 3);
                break;
            case 2:
                g.drawLine(x + 5, y + 1, x + 3, y + 4);
                g.drawLine(x + 3, y + 4, x + 6, y + 4);
                g.drawLine(x + 6, y + 4, x + 4, y + 7);
                break;
            case 3:
                g.drawLine(x + 4, y + 1, x + 4, y + 7);
                g.drawLine(x + 1, y + 4, x + 7, y + 4);
                g.drawLine(x + 2, y + 2, x + 6, y + 6);
                g.drawLine(x + 6, y + 2, x + 2, y + 6);
                break;
            default:
                g.drawLine(x + 2, y + 2, x + 6, y + 2);
                g.drawLine(x + 6, y + 2, x + 2, y + 6);
                g.drawLine(x + 2, y + 6, x + 6, y + 6);
                break;
        }
    }

    // ================================================================== goi server -96..-99 (PokeMangChienDau)

    private static final int TOI_DA_VUNG = 6;
    private static final int[] vX = new int[TOI_DA_VUNG];
    private static final int[] vY = new int[TOI_DA_VUNG];
    private static final int[] vHinh = new int[TOI_DA_VUNG];
    private static final int[] vR = new int[TOI_DA_VUNG];
    private static final int[] vRong = new int[TOI_DA_VUNG];
    private static final int[] vHuong = new int[TOI_DA_VUNG];
    private static final int[] vHe = new int[TOI_DA_VUNG];
    private static final int[] vTick = new int[TOI_DA_VUNG];
    private static final int[] vMax = new int[TOI_DA_VUNG];
    private static final boolean[] vSong = new boolean[TOI_DA_VUNG];
    static final int HINH_TRON = 0, HINH_NON = 1, HINH_CHU_NHAT = 2, HINH_VANH = 3;

    /**
     * -96 POKE_CANH_BAO: vung canh bao dem nguoc msNiem (toa do the gioi, px). hinh 0 tron (r), 1 non
     * (r = tam, rong = goc do, huong), 2 chu nhat (r = dai, rong = ngang, huong), 3 vanh (r ngoai, rong =
     * be day). huong: do, 0 = sang phai, 90 = xuong (toa do man hinh).
     */
    public static void vungCanhBao(int x, int y, int hinh, int r, int rong, int huong, int he, int msNiem) {
        int i = -1;
        for (int k = 0; k < TOI_DA_VUNG; k++) {
            if (!vSong[k]) {
                i = k;
                break;
            }
        }
        if (i < 0) {
            i = 0;
        }
        vSong[i] = true;
        vX[i] = x;
        vY[i] = y;
        vHinh[i] = hinh;
        vR[i] = r < 8 ? 8 : (r > 400 ? 400 : r);
        vRong[i] = rong;
        vHuong[i] = huong;
        vHe[i] = he;
        vTick[i] = 0;
        int t = msNiem / 40;
        vMax[i] = t < 5 ? 5 : (t > 250 ? 250 : t);
        if (tickTu(canhBaoLuc) > 8) {
            canhBaoLuc = Canvas.gameTick;
            AmThanh.phat(AmThanh.CANH_BAO);
        }
    }

    private static void capNhatVung() {
        for (int i = 0; i < TOI_DA_VUNG; i++) {
            if (vSong[i] && ++vTick[i] >= vMax[i]) {
                vSong[i] = false;
                // het niem: no tai tam vung
                noHe(vHe[i], vX[i], vY[i], 1);
                rung(8, 3);
            }
        }
    }

    /** cos/sin x64 theo do (bang 0..90, doi xung). */
    private static final byte[] SIN64 = {0, 6, 11, 17, 22, 27, 32, 37, 41, 45, 49, 52, 55, 58, 60, 62, 63, 64, 64};

    static int sin64(int d) {
        d %= 360;
        if (d < 0) {
            d += 360;
        }
        int q = d / 90, r = d % 90;
        int v;
        switch (q) {
            case 0:
                v = SIN64[r / 5];
                break;
            case 1:
                v = SIN64[(90 - r) / 5];
                break;
            case 2:
                v = -SIN64[r / 5];
                break;
            default:
                v = -SIN64[(90 - r) / 5];
                break;
        }
        return v;
    }

    static int cos64(int d) {
        return sin64(d + 90);
    }

    private static void veVung(Graphics g) {
        for (int i = 0; i < TOI_DA_VUNG; i++) {
            if (!vSong[i]) {
                continue;
            }
            int t = vTick[i], T = vMax[i];
            int x = vX[i], y = vY[i], r = vR[i];
            int p = r * t / T;   // phan da "nap"
            boolean nhay = (t & 2) == 0 || T - t > 12;
            int vien = nhay ? 0xFF2818 : 0xFFA040;
            int trong = 0xC8281C;
            switch (vHinh[i]) {
                case HINH_NON: {
                    int goc = vRong[i] <= 0 ? 60 : vRong[i];
                    int bd = -vHuong[i] - goc / 2;
                    g.setColor(trong);
                    g.fillArc(x - p, y - p * 2 / 3, 2 * p, p * 4 / 3, bd, goc);
                    g.setColor(vien);
                    g.drawArc(x - r, y - r * 2 / 3, 2 * r, r * 4 / 3, bd, goc);
                    g.drawLine(x, y, x + cos64(vHuong[i] - goc / 2) * r / 64, y + sin64(vHuong[i] - goc / 2) * r * 2 / 3 / 64);
                    g.drawLine(x, y, x + cos64(vHuong[i] + goc / 2) * r / 64, y + sin64(vHuong[i] + goc / 2) * r * 2 / 3 / 64);
                    break;
                }
                case HINH_CHU_NHAT: {
                    int c = cos64(vHuong[i]), s = sin64(vHuong[i]);
                    int w = (vRong[i] <= 0 ? 24 : vRong[i]) / 2;
                    int nx = -s * w / 64, ny = c * w / 64;
                    int ax = x + nx, ay = y + ny, bx = x - nx, by = y - ny;
                    int dx = c * r / 64, dy = s * r / 64, px = c * p / 64, py = s * p / 64;
                    g.setColor(trong);
                    g.fillTriangle(ax, ay, bx, by, bx + px, by + py);
                    g.fillTriangle(ax, ay, ax + px, ay + py, bx + px, by + py);
                    g.setColor(vien);
                    g.drawLine(ax, ay, bx, by);
                    g.drawLine(ax + dx, ay + dy, bx + dx, by + dy);
                    g.drawLine(ax, ay, ax + dx, ay + dy);
                    g.drawLine(bx, by, bx + dx, by + dy);
                    break;
                }
                case HINH_VANH: {
                    int day = vRong[i] <= 0 ? r / 3 : vRong[i];
                    int r2 = r - day;
                    g.setColor(trong);
                    for (int k = r2; k <= r2 + day * t / T; k += 2) {
                        g.drawArc(x - k, y - k * 2 / 3, 2 * k, k * 4 / 3, 0, 360);
                    }
                    g.setColor(vien);
                    g.drawArc(x - r, y - r * 2 / 3, 2 * r, r * 4 / 3, 0, 360);
                    g.drawArc(x - r2, y - r2 * 2 / 3, 2 * r2, r2 * 4 / 3, 0, 360);
                    break;
                }
                default: {
                    g.setColor(trong);
                    g.fillArc(x - p, y - p * 2 / 3, 2 * p, p * 4 / 3, 0, 360);
                    g.setColor(vien);
                    g.drawArc(x - r, y - r * 2 / 3, 2 * r, r * 4 / 3, 0, 360);
                    g.drawArc(x - r + 1, y - r * 2 / 3 + 1, 2 * r - 2, r * 4 / 3 - 2, 0, 360);
                    break;
                }
            }
        }
    }

    // ---- -97 trang thai: LiveActor.pokeTT / pokeTTHet / pokeTTChong (xem PokeMangChienDau)

    // ---- -98 boss: pha + dem nguoc no
    private static short bossId = -1;
    private static int bossPha, bossGiayNo;
    private static long bossLuc;

    public static void bossPha(Monster boss, short id, int pha, int suKien, int giayDenNo) {
        bossId = id;
        bossPha = pha;
        bossGiayNo = giayDenNo;
        bossLuc = System.currentTimeMillis();
        if (boss == null) {
            return;
        }
        int he = heQuai(boss);
        int cao = caoThan(boss);
        themHat(K_VONG, boss.x, boss.y - cao, 0, 0, 0, 14, 0xFFFFFF, KieuO.mauHe(he), 4);
        rung(suKien == 1 ? 16 : 10, suKien == 1 ? 4 : 3);
        AmThanh.phat(AmThanh.BOSS);
        String chu;
        switch (suKien) {
            case 1:
                chu = "Nổi giận!";
                break;
            case 2:
                chu = "Gọi đồng bọn!";
                break;
            case 3:
                chu = "Dựng khiên!";
                break;
            default:
                chu = new StringBuffer("Pha ").append(pha + 1).toString();
                break;
        }
        SoBay.nhan(chu, suKien == 1 ? 0xD02818 : 0x7A3CC8, boss.x, boss.y - cao * 2 - 8);
    }

    private static void veBoss(Graphics g, LiveActor a, int x, int y) {
        if (a.ID != bossId || !(a instanceof Monster)) {
            return;
        }
        long qua = (System.currentTimeMillis() - bossLuc) / 1000;
        int con = bossGiayNo - (int) qua;
        StringBuffer sb = new StringBuffer("P").append(bossPha + 1);
        if (bossGiayNo > 0) {
            if (con <= 0) {
                sb.append(" nộ!");
            } else {
                sb.append(' ').append(con / 60).append(':').append(con % 60 < 10 ? "0" : "").append(con % 60);
            }
        }
        String s = sb.toString();
        game.render.screen.Font f = game.render.screen.Font.borderFont;
        if (f == null) {
            return;
        }
        int w = f.getWidth(s) + 8;
        int h = f.getHeight() + 2;
        GiaoDien.vien(g, x - w / 2, y - h - 12, w, h, con <= 0 && bossGiayNo > 0 ? 0xD02818 : 0x5A3C98, s);
    }

    // ---- -99 dong gop sat thuong boss the gioi
    private static final short[] gpId = new short[5];
    private static final int[] gpPhan = new int[5];
    private static int gpSo, gpCuaToi = -1;
    private static long gpLuc;

    public static void dongGop(short[] id, int[] phan, int n, int cuaToi) {
        gpSo = n > 5 ? 5 : n;
        for (int i = 0; i < gpSo; i++) {
            gpId[i] = id[i];
            gpPhan[i] = phan[i];
        }
        gpCuaToi = cuaToi;
        gpLuc = System.currentTimeMillis();
    }

    private static String phanTram(int phanNghin) {
        return new StringBuffer().append(phanNghin / 10).append('.').append(phanNghin % 10).append('%').toString();
    }

    /** Bang dong gop goc phai tren (toa do man hinh, ve qua toa do the gioi + cmx/cmy). */
    private static void veDongGop(Graphics g) {
        if (gpCuaToi < 0 || System.currentTimeMillis() - gpLuc > 12000L || Canvas.gameScr == null) {
            return;
        }
        game.render.screen.Font f = game.render.screen.Font.borderFont;
        if (f == null) {
            return;
        }
        int dong = f.getHeight();
        int w = 92, h = 18 + dong * (gpSo + 1);
        int x = GameScr.cmx + Canvas.w - w - 2, y = GameScr.cmy + 34;
        GiaoDien.khoi(g, x, y, w, h, 5, GiaoDien.VIEN);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 4, 0x24345A);
        f.drawString(g, "Đóng góp", x + w / 2, y + 2, game.render.screen.Font.CENTER);
        int yy = y + 3 + dong;
        for (int i = 0; i < gpSo; i++) {
            Actor a = Canvas.gameScr.findCharByID(gpId[i]);
            String ten = a instanceof Char && ((Char) a).name != null ? ((Char) a).name : ("#" + gpId[i]);
            if (ten.length() > 8) {
                ten = ten.substring(0, 8);
            }
            f.drawString(g, (i + 1) + "." + ten, x + 4, yy, game.render.screen.Font.LEFT);
            f.drawString(g, phanTram(gpPhan[i]), x + w - 4, yy, game.render.screen.Font.RIGHT);
            yy += dong;
        }
        g.setColor(GiaoDien.VANG);
        g.drawLine(x + 4, yy + 1, x + w - 5, yy + 1);
        f.drawString(g, "Bạn: " + phanTram(gpCuaToi), x + w / 2, yy + 2, game.render.screen.Font.CENTER);
    }

    // ================================================================== vong chinh

    /** Moi tick game (GameScr.updateFlyText). */
    public static void capNhat() {
        try {
            if (rungCon > 0) {
                rungCon--;
                if (rungCon == 0) {
                    rungManh = 0;
                }
            }
            capNhatDan();
            capNhatCanh();
            capNhatXac();
            capNhatNo();
            capNhatVung();
            int j = 0;
            for (int i = 0; i < soHat; i++) {
                if (++hTuoi[i] >= hMax[i]) {
                    continue;
                }
                hX[i] += hVX[i];
                hY[i] += hVY[i];
                hVY[i] += hG[i];
                if (hK[i] == K_KHOI || hK[i] == K_LA) {
                    hVX[i] = hVX[i] * 7 / 8;
                }
                if (j != i) {
                    hK[j] = hK[i];
                    hX[j] = hX[i];
                    hY[j] = hY[i];
                    hVX[j] = hVX[i];
                    hVY[j] = hVY[i];
                    hG[j] = hG[i];
                    hTuoi[j] = hTuoi[i];
                    hMax[j] = hMax[i];
                    hMau[j] = hMau[i];
                    hMau2[j] = hMau2[i];
                    hCo[j] = hCo[i];
                }
                j++;
            }
            soHat = j;
            AmThanh.capNhat();
        } catch (Throwable e) {
            soHat = 0;
        }
    }

    /** Ve trong toa do the gioi (GameScr.paintFlyText, truoc so bay). */
    public static void ve(Graphics g) {
        try {
            veVung(g);
            veCanh(g);
            veXac(g);
            veNo(g);
            veDan(g);
            veHat(g);
            veTrangThai(g);
            veDongGop(g);
        } catch (Throwable e) {
            // bo qua khung nay
        }
    }

    private static void veHat(Graphics g) {
        for (int i = 0; i < soHat; i++) {
            int x = hX[i] >> 4, y = hY[i] >> 4;
            int t = hTuoi[i], T = hMax[i];
            int mau = t * 2 < T ? hMau[i] : hMau2[i];
            int c = hCo[i];
            g.setColor(mau);
            switch (hK[i]) {
                case K_CHAM: {
                    int s = t * 3 > T * 2 ? (c > 1 ? c - 1 : 1) : c;
                    g.fillRect(x - s / 2, y - s / 2, s, s);
                    break;
                }
                case K_TIA: {
                    int dx = hVX[i] >> 3, dy = hVY[i] >> 3;
                    g.drawLine(x, y, x - dx, y - dy);
                    if (c > 1) {
                        g.drawLine(x + 1, y, x + 1 - dx, y - dy);
                    }
                    break;
                }
                case K_VONG: {
                    int r = 4 + t * (3 + c);
                    g.drawArc(x - r, y - r * 2 / 3, 2 * r, r * 4 / 3, 0, 360);
                    if (c > 2) {
                        g.drawArc(x - r + 1, y - r * 2 / 3 + 1, 2 * r - 2, r * 4 / 3 - 2, 0, 360);
                    }
                    break;
                }
                case K_SAO: {
                    int r = c * (T - t) / T + 1;
                    g.drawLine(x - r, y, x + r, y);
                    g.drawLine(x, y - r, x, y + r);
                    if (r > 2) {
                        g.setColor(hMau2[i]);
                        g.drawLine(x - r / 2, y - r / 2, x + r / 2, y + r / 2);
                        g.drawLine(x + r / 2, y - r / 2, x - r / 2, y + r / 2);
                    }
                    break;
                }
                case K_KHOI: {
                    int r = c + t / 3;
                    if (t * 3 > T * 2) {
                        r = c * (T - t) * 3 / T + 1;
                    }
                    g.fillArc(x - r, y - r, 2 * r, 2 * r, 0, 360);
                    break;
                }
                case K_SET: {
                    // tia set zic zac tu tam ra (do dai c)
                    int dx = hVX[i] >> 4, dy = hVY[i] >> 4;
                    int x0 = (hX[i] - hVX[i] * t) >> 4, y0 = (hY[i] - hVY[i] * t) >> 4;
                    int mx = (x0 + x) / 2 + ((t & 1) == 0 ? dy / 2 + 2 : -dy / 2 - 2);
                    int my = (y0 + y) / 2 + ((t & 1) == 0 ? -dx / 2 : dx / 2);
                    g.drawLine(x0, y0, mx, my);
                    g.drawLine(mx, my, x, y);
                    g.setColor(0xFFFFFF);
                    g.drawLine(x0, y0 - 1, mx, my - 1);
                    break;
                }
                case K_LA: {
                    int s = c;
                    boolean lat = ((t >> 1) & 1) == 0;
                    if (lat) {
                        g.fillTriangle(x - s, y, x, y - s, x + s, y);
                        g.setColor(hMau2[i]);
                        g.fillTriangle(x - s, y, x, y + s, x + s, y);
                    } else {
                        g.fillTriangle(x, y - s, x + s, y, x, y + s);
                        g.setColor(hMau2[i]);
                        g.fillTriangle(x, y - s, x - s, y, x, y + s);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    /** Xoa het (doi map). */
    public static void xoa() {
        soHat = 0;
        for (int i = 0; i < TOI_DA_DAN; i++) {
            dSong[i] = false;
            dTu[i] = null;
            dDich[i] = null;
            dAnh[i] = null;
        }
        for (int i = 0; i < TOI_DA_CANH; i++) {
            cSong[i] = false;
            cDich[i] = null;
        }
        for (int i = 0; i < TOI_DA_XAC; i++) {
            xSong[i] = false;
        }
        for (int i = 0; i < TOI_DA_NO; i++) {
            nAi[i] = null;
        }
        for (int i = 0; i < TOI_DA_VUNG; i++) {
            vSong[i] = false;
        }
        bossId = -1;
        gpCuaToi = -1;
        rungCon = 0;
        rungManh = 0;
    }
}
