package real;

import com.htbb.pokemon.SoSatThuong;
import com.htbb.pokemon.TuDongKeHoach;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nguyen nhan GUC (bao loi Zalo 12/11/2026: "dang treo Tu dong danh, ro rang con nua cay mau ma khong hieu sao chet. Khong
 * phai boss, khong phai khac he"). Truoc day may chu khong de lai dau vet nao khi nguoi choi guc. Nay:
 * <ul>
 *   <li>moi cho tru HP nguoi choi chinh (Monster.attack, trang thai bong / doc, doc / ri mau theo giay, phan don PvP, than thu,
 *       nguoi choi khac) ghi mot don vao {@link Char#soSatThuong} ({@link #quaiDanh} / {@link #ghi});</li>
 *   <li>khi guc ({@link #khiGuc}, goi tu HoiSinhGame.khiGuc cho MOI lan guc cua nguoi that): dong kiem toan
 *       {@code [GUC] ten cap map x y maxhp tuDong nguongHp binhHp 3s=-X/n | 5 don cuoi (nguon, sat thuong, HP truoc->sau, luc)}
 *       va cau bao nguoi choi "Gục vì <quái> (cấp N) đánh X HP khi còn Y/Z HP..." (hop thoai guc + tin rieng);</li>
 *   <li>HP mat o cho chua ghi nhan -> don "nguồn khác (không ghi nhận)" (SoSatThuong.chotGuc) thay vi doan sai.</li>
 * </ul>
 */
public final class GucGame {

    /** Cau "Guc vi ..." cua lan guc dang mo (hop thoai guc doc lai) - charDBID -> cau. */
    static final ConcurrentHashMap<Integer, String> CAU = new ConcurrentHashMap<Integer, String>();

    private GucGame() {
    }

    /** HP toi da THAT (gom buff HP cua dong Bulbasaur, Char.calculatorHPMP). */
    static int maxThat(final Char c) {
        try {
            return c.maxhp + Math.max(0, c.percentBuff[2]);
        } catch (final RuntimeException e) {
            return c.maxhp;
        }
    }

    /** Ten quai nhu nguoi choi thay (loai rieng cua Pokemon hoang da) + cap (+ Alpha / boss). */
    static String tenQuai(final Monster m) {
        if (m == null) {
            return "Pokémon hoang dã";
        }
        String ten = null;
        try {
            if (m.dexRieng > 0) {
                ten = com.htbb.pokemon.LoaiPokemon.ten(m.dexRieng);
            }
        } catch (final RuntimeException ignored) {
            // loai chua nap
        }
        try {
            if (ten == null || ten.isEmpty()) {
                ten = m.getName();
            }
        } catch (final RuntimeException ignored) {
            // mau chua nap
        }
        if (ten == null || ten.isEmpty()) {
            ten = "Pokémon #" + m.idTemplate;
        }
        int cap = m.level;
        try {
            if (cap <= 0 && m.getMonsterTemplate() != null) {
                cap = m.getMonsterTemplate().level; // quai moc truoc khi bo nap ban do gan cap
            }
        } catch (final RuntimeException ignored) {
            // mau chua nap
        }
        final StringBuilder b = new StringBuilder(ten).append(" (cấp ").append(cap);
        try {
            if (NoiDungLauDai.laAlpha(m)) {
                b.append(", Alpha");
            }
            if (BossLauDai.laBoss(m)) {
                b.append(", boss");
            }
        } catch (final RuntimeException ignored) {
            // bang alpha / boss chua nap
        }
        return b.append(')').toString();
    }

    /** Quai {@code m} vua tru HP cua {@code c} (Monster.attack): HP truoc don {@code hpTruoc}, HP sau = c.hp. */
    public static void quaiDanh(final Char c, final Monster m, final int hpTruoc) {
        if (c == null || c.isMonster || c.cat != 0) {
            return;
        }
        ghi(c, tenQuai(m), hpTruoc);
    }

    /** Nguon {@code nguon} vua tru HP cua {@code c}: sat thuong = hpTruoc - c.hp (that su mat, sau moi giam / kep). */
    public static void ghi(final Char c, final String nguon, final int hpTruoc) {
        try {
            if (c == null) {
                return;
            }
            final int sau = Math.max(0, c.hp);
            final int mat = hpTruoc - sau;
            if (mat <= 0) {
                return;
            }
            c.soSatThuong.ghi(System.currentTimeMillis(), nguon, mat, hpTruoc, sau, maxThat(c));
        } catch (final RuntimeException ignored) {
            // chan doan khong bao gio lam hong don danh
        }
    }

    /** Nguoi choi khac {@code a} (PvP) vua tru HP cua {@code c}. */
    public static void nguoiDanh(final Char c, final Char a, final int hpTruoc) {
        ghi(c, a == null ? "người chơi khác" : "người chơi " + a.charname, hpTruoc);
    }

    /** So binh HP con trong tui (bang binh Tu dong danh). */
    static int soBinhHp(final Char p) {
        int n = 0;
        try {
            for (final int[] b : TuDongKeHoach.BINH_HP) {
                if (p.potions != null && b[0] < p.potions.length) {
                    n += Math.max(0, p.potions[b[0]]);
                }
            }
        } catch (final RuntimeException ignored) {
            // tui doi giua chung
        }
        return n;
    }

    /**
     * HOOK HoiSinhGame.khiGuc (moi lan guc cua nguoi choi that, ca ban do co luat rieng): chot so sat thuong, ghi dong
     * [GUC], bao nguoi choi nguyen nhan. Goi lai khi van guc: bo qua (SoSatThuong.chotGuc tra null).
     */
    public static void khiGuc(final Char p) {
        try {
            if (p == null || p.isMonster || p.cat != 0) {
                return;
            }
            final long bay = System.currentTimeMillis();
            final SoSatThuong.Don[] ds = p.soSatThuong.chotGuc(bay, maxThat(p));
            if (ds == null) {
                return;
            }
            final boolean tuDong = TuDongGame.dangTuDong(p);
            final StringBuilder b = new StringBuilder();
            b.append(p.charname).append(" cap=");
            try {
                b.append(p.getLevel());
            } catch (final RuntimeException e) {
                b.append('?');
            }
            b.append(" map=").append(p.mapID).append(" x=").append(p.x).append(" y=").append(p.y).append(" maxhp=").append(maxThat(p))
                    .append(" tuDong=").append(tuDong ? 1 : 0);
            if (tuDong) {
                final int[] ng = TuDongGame.nguongNguoi(p);
                b.append(" nguongHp=").append(ng[0]);
            }
            b.append(" binhHp=").append(soBinhHp(p)).append(" 3s=-").append(SoSatThuong.tong(ds, bay, SoSatThuong.CUA_SO_MS)).append('/')
                    .append(SoSatThuong.dem(ds, bay, SoSatThuong.CUA_SO_MS)).append("don | ").append(SoSatThuong.dongLog(ds, bay));
            com.htbb.log.Nhat.kiemToan("GUC", b.toString());
            final String cau = SoSatThuong.cauNguoiChoi(ds, bay);
            if (!cau.isEmpty()) {
                CAU.put(Integer.valueOf(p.charDBID), cau);
                try {
                    com.htbb.thongbao.ThongBao.rieng(p, cau);
                } catch (final Exception ignored) {
                    // mat ket noi
                }
            }
        } catch (final RuntimeException e) {
            System.out.println("GucGame.khiGuc: " + e);
        }
    }

    /** Cau "Guc vi ..." cua lan guc gan nhat ("" neu khong co) - hop thoai guc. */
    public static String cau(final Char p) {
        if (p == null) {
            return "";
        }
        final String s = CAU.get(Integer.valueOf(p.charDBID));
        return s == null ? "" : s;
    }

    /** HOOK moi nhip nhan vat (CharVongLap.update): anh chup HP; hoi sinh thi bo cau guc cu. */
    public static void nhip(final Char p) {
        if (p.soSatThuong.nhip(System.currentTimeMillis(), p.hp)) {
            xoaCau(p);
        }
    }

    /** Da hoi sinh / thoat: bo cau cu. */
    public static void xoaCau(final Char p) {
        if (p != null) {
            CAU.remove(Integer.valueOf(p.charDBID));
        }
    }
}
