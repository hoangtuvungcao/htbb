package real;

import com.htbb.pokemon.TrangThaiPokemon;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

/**
 * Trang thai (bong/doc/te/ngu/bang) tren QUAI khi bat co sv.lau_dai (GD2 6.4). Logic thuan o
 * com.htbb.pokemon.TrangThaiPokemon. Ban toi thieu: chi nguoi -> quai (PvE), chieu cung he
 * trung don co 10% gay trang thai; DoT tinh moi giay o LauDaiJob ({@link #nhip}); quai ngu /
 * dong bang khong danh (Monster.attack), te liet 15% mat luot. PvP va quai -> nguoi: con lai.
 */
final class TrangThaiGame {

    private static final WeakHashMap<LiveActor, TrangThaiPokemon> TT = new WeakHashMap<LiveActor, TrangThaiPokemon>();

    private TrangThaiGame() {
    }

    static synchronized TrangThaiPokemon lay(final LiveActor a) {
        return TT.get(a);
    }

    /** @return trang thai vua gay (0 = khong). */
    static int thuGay(final Char p, final LiveActor dich, final int he) {
        if (he < 0 || dich == null || dich.hp <= 0 || LoaiQuaiGame.botKhongDung(p, dich)) {
            return 0; // bot HLV AI: khong bong / doc con hoang da danh cho nguoi choi (bao loi 12/11/2026)
        }
        final int tt = TrangThaiPokemon.tuHe(he);
        if (tt == 0 || Map.random(100) >= TrangThaiPokemon.TI_LE_KICH_HOAT) {
            return 0;
        }
        if (TrangThaiPokemon.mien(tt, dich.hePokemon1(), dich.hePokemon2())) {
            return 0;
        }
        final boolean pvp = dich instanceof Char;
        final boolean boss = dich instanceof Monster && (((Monster) dich).isBoss || BossLauDai.laBoss((Monster) dich));
        final int ms = TrangThaiPokemon.thoiGian(tt, pvp, boss);
        if (ms <= 0) {
            return 0;
        }
        synchronized (TrangThaiGame.class) {
            TrangThaiPokemon t = TT.get(dich);
            if (t == null) {
                t = TrangThaiPokemon.moi();
                TT.put(dich, t);
            }
            return t.ap(tt, ms, System.currentTimeMillis()) ? tt : 0;
        }
    }

    /**
     * Trang thai {@code tt} tu dong gay trang thai cua trang bi (ChiSoTrangBiGame.khamTrangThai, da quay ti le co
     * tran). Ton trong mien theo he, thoi luong PvP x0,5 / boss 30% (boss khong ngu/bang); gui -97. @return tt / 0.
     */
    static int gayTuTrangBi(final Char p, final LiveActor dich, final int tt) {
        if (tt <= 0 || dich == null || dich.hp <= 0 || LoaiQuaiGame.botKhongDung(p, dich)) {
            return 0;
        }
        if (TrangThaiPokemon.mien(tt, dich.hePokemon1(), dich.hePokemon2())) {
            return 0;
        }
        final boolean pvp = dich instanceof Char;
        final boolean boss = dich instanceof Monster && (((Monster) dich).isBoss || BossLauDai.laBoss((Monster) dich));
        final int ms = TrangThaiPokemon.thoiGian(tt, pvp, boss);
        if (ms <= 0) {
            return 0;
        }
        final TrangThaiPokemon t;
        synchronized (TrangThaiGame.class) {
            TrangThaiPokemon x = TT.get(dich);
            if (x == null) {
                x = TrangThaiPokemon.moi();
                TT.put(dich, x);
            }
            if (!x.ap(tt, ms, System.currentTimeMillis())) {
                return 0;
            }
            t = x;
        }
        final io.Message g = GoiLauDaiGame.trangThai(dich, t);
        if (g != null) {
            if (dich instanceof Char) {
                ((Char) dich).sendMessage(g);
            }
            p.sendMessage(g);
            p.sendToNearPlayer(g);
        }
        return tt;
    }

    /** % quai (khong boss) gay trang thai theo he cua no moi don trung nguoi. */
    static final int QUAI_GAY_PHAN_TRAM = 3;

    /** Quai danh trung nguoi: {@code QUAI_GAY_PHAN_TRAM}% gay trang thai theo he 1 cua quai; gui -97. */
    static void quaiGay(final Monster m, final LiveActor muc) {
        if (!com.htbb.pokemon.LauDai.bat() || !(muc instanceof Char) || m == null || muc.hp <= 0) {
            return;
        }
        if (Map.random(100) >= QUAI_GAY_PHAN_TRAM) {
            return;
        }
        final int tt = TrangThaiPokemon.tuHe(m.hePokemon1());
        if (tt == 0 || TrangThaiPokemon.mien(tt, muc.hePokemon1(), muc.hePokemon2())) {
            return;
        }
        final int ms = TrangThaiPokemon.thoiGian(tt, false, false);
        final TrangThaiPokemon t;
        synchronized (TrangThaiGame.class) {
            TrangThaiPokemon x = TT.get(muc);
            if (x == null) {
                x = TrangThaiPokemon.moi();
                TT.put(muc, x);
            }
            if (!x.ap(tt, ms, System.currentTimeMillis())) {
                return;
            }
            t = x;
        }
        final io.Message g = GoiLauDaiGame.trangThai(muc, t);
        if (g != null) {
            ((Char) muc).sendMessage(g);
            ((Char) muc).sendToNearPlayer(g);
        }
    }

    /** Quai bi khoa hanh dong (ngu / dong bang) hoac te liet mat luot. */
    static synchronized boolean khongDanhDuoc(final LiveActor a) {
        final TrangThaiPokemon t = TT.get(a);
        if (t == null) {
            return false;
        }
        final long bay = System.currentTimeMillis();
        return t.khoa(bay) || t.matLuot(bay, Map.random(100));
    }

    /** Thuoc chua: go trang thai {@code tt} (0 = moi loai) va mien them {@code mienMs}. */
    static synchronized void go(final LiveActor a, final int tt, final long mienMs) {
        final TrangThaiPokemon t = TT.get(a);
        if (t == null) {
            return;
        }
        final long bay = System.currentTimeMillis();
        if (tt == 0 || t.loai == tt) {
            t.go(bay);
            if (mienMs > 0) {
                t.mienToi = Math.max(t.mienToi, bay + mienMs);
            }
            if (a instanceof Char) {
                final io.Message m = GoiLauDaiGame.trangThai(a, t);
                if (m != null) {
                    ((Char) a).sendMessage(m);
                }
            }
        }
    }

    /** Quai trung don: dang ngu thi tinh. */
    static synchronized void trungDon(final LiveActor a) {
        final TrangThaiPokemon t = TT.get(a);
        if (t != null) {
            t.trungDon(System.currentTimeMillis());
        }
    }

    /** Moi giay: sat thuong bong/doc (khong giet - de lai 1 HP, don ket lieu van do nguoi danh). */
    static void nhip() {
        final long bay = System.currentTimeMillis();
        synchronized (TrangThaiGame.class) {
            final Iterator<Entry<LiveActor, TrangThaiPokemon>> it = TT.entrySet().iterator();
            while (it.hasNext()) {
                final Entry<LiveActor, TrangThaiPokemon> e = it.next();
                final LiveActor a = e.getKey();
                final TrangThaiPokemon t = e.getValue();
                if (a == null || a.hp <= 0 || !t.dangCo(bay)) {
                    it.remove();
                    continue;
                }
                final int d = t.nhip(bay, Math.max(1, a.maxhp));
                if (d > 0 && a.hp > 1) {
                    final int hpTruoc = a.hp;
                    a.hp = Math.max(1, a.hp - d);
                    if (a instanceof Char) {
                        GucGame.ghi((Char) a, "trạng thái " + (t.loai == TrangThaiPokemon.BONG ? "Bỏng" : "Độc"), hpTruoc);
                        VatPhamDungLauDai.guiHp((Char) a);
                    }
                }
            }
        }
    }
}
