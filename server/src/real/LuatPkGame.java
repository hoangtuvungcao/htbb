package real;

import com.htbb.pokemon.LuatPk;
import data.Database;
import io.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * LUAT PK THE GIOI (0.9.7) - phan GAME cua com.htbb.pokemon.LuatPk: vung an toan (thi tran trong data_map_ten), chan
 * danh nguoi thap hon qua 10 cap ngoai vung PvP, nguong khoa PK nam trong tam short, nghiep phai theo gio online,
 * EXP mat khi guc khong tut cap va khong mien cho VIP.
 *
 * <p>Diem goi: MapDanhNhau.doAttackPlayer + Region.doAttackPlayer ({@link #chan}), CharVongLap moi phut
 * ({@link #nhipPhut}), LevelDetail.getXPLost ({@link #expMat}).
 */
public final class LuatPkGame {

    private LuatPkGame() {
    }

    /** Id ban do thi tran (data_map_ten.loai = 'thi_tran'). Rong = chua nap (chi lang 0 / 70 / 80 la an toan). */
    static volatile Set<Integer> THI_TRAN = Collections.emptySet();

    public static void datThiTran(final Set<Integer> ds) {
        THI_TRAN = ds == null ? Collections.<Integer>emptySet() : Collections.unmodifiableSet(new HashSet<Integer>(ds));
    }

    /** NoiDungLauDai.sauKhiNapTemplate: nap danh sach thi tran. */
    public static void nap() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT map_id FROM data_map_ten WHERE loai = 'thi_tran'");
            rs = pre.executeQuery();
            final Set<Integer> s = new HashSet<Integer>();
            while (rs.next()) {
                s.add(Integer.valueOf(rs.getInt(1)));
            }
            datThiTran(s);
            System.out.println("[LUAT PK] " + s.size() + " thi tran an toan");
        } catch (final Exception e) {
            System.out.println("[LUAT PK] LOI nap thi tran: " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                if (pre != null) {
                    pre.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                if (conn != null) {
                    Database.connPool.free(conn);
                }
            } catch (final Exception ignored) {
            }
        }
    }

    /** Ban do la vung PvP (dau truong, loi dai, chien truong, Cao nguyen chien thanh, ban do boss / quai phe). */
    static boolean vungPvp(final Map map, final Char p) {
        if (map == null) {
            return true;
        }
        final int id = map.mapId;
        if (id == Map.idMapDautruong || id == 204 || id == 205 || id == Map.idMapTown || id == Map.idMapLoiDai) {
            return true;
        }
        try {
            if (map.isMapLoiDai() || map.isMapChienTruongMoba() || map instanceof MapLienDau) {
                return true;
            }
            // Chiem Gym / chien thanh: CHI ban do Gym (va ban sao) la vung PvP them. Truoc 25/10/2026 co cua VUNG lam moi ban
            // do - ca thi tran - thanh vung PvP trong ca gio chiem Gym (bot hoi quy: nguoi o Pewter mat HP).
            final int nuoc = p == null ? 0 : p.inCountry;
            if (Map.vungChiemGym(map, nuoc)) {
                return true;
            }
        } catch (final Exception ignored) {
        }
        return false;
    }

    /** Thi tran / lang (khong PK). */
    static boolean vungAnToan(final Map map) {
        if (map == null) {
            return false;
        }
        return Map.isMapLang(map) || THI_TRAN.contains(Integer.valueOf(map.mapId));
    }

    /**
     * Ly do chan don PK the gioi p -&gt; c (da bao cho p), null = cho danh. Vung PvP / tran xep hang khong chan.
     */
    public static String chan(final Map map, final Char p, final Char c) {
        if (p == null || c == null || vungPvp(map, p)) {
            return null;
        }
        final boolean biDangPk = c.isKiller || c.killer > 0 || c.pk != 0;
        final String ly = LuatPk.chan(vungAnToan(map), p.lvDetail.lv, c.lvDetail.lv, biDangPk, p.isKiller ? p.killer : 0);
        if (ly != null) {
            try {
                p.sendMessage(MessageCreator.createMsgChat(p.id, ly));
            } catch (final Exception ignored) {
            }
        }
        return ly;
    }

    /** CharVongLap moi phut online: nghiep phai dan; het nghiep thi het ten do (bao minh + nguoi gan, goi 67). */
    public static void nhipPhut(final Char p) {
        if (p == null || p.isBot != -1) {
            return;
        }
        final long bay = System.currentTimeMillis();
        if (p.killer <= 0) {
            p.lucPhaiNghiep = bay;
            return;
        }
        // theo DONG HO THAT (5 / phut online): nhip CharVongLap la 300 vong x ~300 ms (~90 giay, cham hon khi tai nang),
        // truoc tru 5 moi nhip -> nghiep giam cham hon thiet ke ~1,5 lan tro len.
        if (com.htbb.pokemon.CoPk.dangGiaoTranh(p.lucDanhNguoiPk, bay)) {
            p.lucPhaiNghiep = bay; // dang danh nguoi: nghiep dung (khong tat ten do / PK giua tran), tinh lai tu luc ngung
            return;
        }
        if (p.lucPhaiNghiep <= 0L) {
            p.lucPhaiNghiep = bay - 60_000L;
        }
        final long[] tg = com.htbb.pokemon.CoPk.phaiTheoGio(p.killer, p.lucPhaiNghiep, bay);
        p.lucPhaiNghiep = tg[1];
        final int cu = p.killer;
        final int moi = (int) tg[0];
        if (moi == cu) {
            return;
        }
        p.killer = (short) moi;
        if (moi <= 0) {
            p.killer = 0;
            p.isKiller = false;
            p.nPKill = 0;
            p.timeKiller = 0L;
        }
        if (moi <= 0 || moi / 100 != cu / 100) { // bao khi het ten do hoac qua moi moc 100 (khong gui moi phut)
            try {
                final Message m = new Message(67);
                m.dos.writeShort(p.id);
                m.dos.writeByte(p.isKiller ? 1 : 0);
                m.dos.writeShort(p.killer);
                p.sendMessage(m);
                p.sendToNearPlayer(m);
            } catch (final Exception ignored) {
            }
        }
    }

    /** EXP mat khi guc trong PK (LevelDetail.getXPLost), khong tut cap. */
    public static long expMat(final LevelDetail lv, final int killer) {
        if (lv == null) {
            return 0L;
        }
        final int cap = lv.lv;
        if (cap <= 0 || cap >= LevelDetail.expRequire.length) {
            return 0L;
        }
        final long moc = cap - 1 < LevelDetail.expMain.length && cap >= 1 ? LevelDetail.expMain[cap - 1] : 0L;
        return LuatPk.expMat(lv.getExp(), LevelDetail.expRequire[cap], moc, killer > 0);
    }
}
