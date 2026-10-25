package real;

import com.htbb.pokemon.BuocHanhDong;
import data.Database;
import data.InfoQuestTemplate;
import java.util.Collections;

import java.util.concurrent.ConcurrentHashMap;

/**
 * BUOC NOI CHUYEN CO HANH DONG THAT (nhanh sua-buoc-hanh-dong) - noi game cua com.htbb.pokemon.BuocHanhDong. Cung kieu
 * KhuVucNhiemVuGame (buoc dia diem):
 *
 * <pre>
 *  Char.xuLyNhiemVu (tra -64)   -> {@link #chuaXong}: buoc co muc tieu hanh dong, CHUA xong -> nghe NPC = nhan buoc (dang
 *                                  lam), bao muc tieu + tien do, KHONG tra. Tien do chi dem khi buoc DANG LAM (sau khi nhan).
 *  NhiemVuEngine.suKien          -> {@link #suKien}: "nang_cap" (mac do, DiemMocNhiemVu.khiMacDo) / "nang_cap_cong" (+k,
 *                                  NangCapLauDaiGame) cua buoc chinh / phu dang lam.
 *  NhiemVuEngine.khiHaQuai       -> {@link #khiHaQuai}: "quan_sat" (ha 1 con loai do, dung khung gio) / "thu_thach_gio" (ha N
 *                                  con mau do trong X phut).
 *  du                            -> trang thai du (FINISH) roi tra ngay nhu nguoi choi bam tra (Char.OnQuest), giong buoc dia
 *                                  diem; tui day thi giu "du", noi chuyen voi NPC giao de tra.
 *  MessageCreator -64            -> {@link #tienDo}: mo ta dang lam kem (x/N); KhuVucNhiemVuGame.npcTra: dang lam -1, du -> NPC.
 * </pre>
 * Du lieu: data_nhiem_vu_noi_dung.dieu_kien co kiem_hanh_dong=1 (sql/htbb.sql khoi sua_buoc_hanh_dong.sql). Tien do o
 * NewCharQuest.monsterKilled[0] (luu cung chuoi nhiem vu cu).
 */
public final class HanhDongNhiemVuGame {

    static volatile java.util.Map<String, BuocHanhDong> BUOC = Collections.emptyMap();
    /** charDBID * 4 + main_sub -> luc bat dau dem cua buoc ha quai tinh gio (mat khi thoat: vao lai thi dem lai). */
    static final ConcurrentHashMap<Long, Long> BAT_DAU = new ConcurrentHashMap<Long, Long>();

    private HanhDongNhiemVuGame() {
    }

    /** NoiDungLauDai.sauKhiNapTemplate. */
    public static void nap() {
        try {
            BUOC = data.DatabaseLauDai.napBuocHanhDong();
            System.out.println("[LAU DAI] buoc noi chuyen co hanh dong that: " + BUOC.size() + " buoc.");
        } catch (final Exception e) {
            System.out.println("HanhDongNhiemVuGame.nap: " + e);
        }
    }

    /** Test / cong cu. */
    public static void dat(final java.util.Map<String, BuocHanhDong> m) {
        BUOC = m == null ? Collections.<String, BuocHanhDong>emptyMap() : m;
    }

    /** Buoc hanh dong cua mau nhiem vu (chi type 3 co ma), null = buoc thuong. */
    public static BuocHanhDong mau(final InfoQuestTemplate info) {
        if (info == null || info.type != 3 || info.ma == null || info.ma.isEmpty()) {
            return null;
        }
        return BUOC.get(info.ma);
    }

    static int da(final NewCharQuest q) {
        return q != null && q.monsterKilled != null && q.monsterKilled.length > 0 ? q.monsterKilled[0] : 0;
    }

    static void datDa(final NewCharQuest q, final int v) {
        if (q.monsterKilled == null || q.monsterKilled.length == 0) {
            q.monsterKilled = new short[1];
        }
        q.monsterKilled[0] = (short) Math.max(0, Math.min(Short.MAX_VALUE, v));
    }

    /** Char (tra -64): buoc co muc tieu hanh dong va chua du -> true (khong tra). */
    public static boolean chuaXong(final NewCharQuest q, final InfoQuestTemplate info) {
        return mau(info) != null && q != null && q.state_quest != NewCharQuest.FINISH && q.state_quest != NewCharQuest.DONE;
    }

    /** " (3/26)" cho buoc hanh dong dang lam, "" khac. */
    public static String tienDo(final NewCharQuest q, final InfoQuestTemplate info) {
        final BuocHanhDong b = mau(info);
        if (b == null || q == null || q.state_quest != NewCharQuest.WORKING) {
            return "";
        }
        return " " + b.chuTienDo(da(q));
    }

    /** Bao nguoi choi: da nhan buoc, phai lam gi (tien do). */
    public static void baoChuaXong(final Char p, final NewCharQuest q, final InfoQuestTemplate info) {
        final BuocHanhDong b = mau(info);
        if (b == null || p == null) {
            return;
        }
        final String muc = info.shortContent == null || info.shortContent.isEmpty() ? info.name : info.shortContent;
        final String cach = b.cachTinh();
        p.sendMessage(MessageCreator.createServerAlertMessage("Đã nhận " + info.name + ". Mục tiêu: " + muc + " " + b.chuTienDo(da(q))
                + "." + (cach.isEmpty() ? "" : " " + cach) + " Xong là hoàn thành.", ""));
    }

    // ------------------------------------------------------------------ diem moc

    /** NhiemVuEngine.suKien: mac do / nang cap thanh cong. */
    public static void suKien(final Char p, final String kieu, final String thamSo, final int n) {
        if (p == null || p.charDBID <= 0 || n <= 0 || BUOC.isEmpty() || !("nang_cap".equals(kieu) || "nang_cap_cong".equals(kieu))) {
            return;
        }
        try {
            xet(p, (byte) 0, kieu, thamSo, n, null);
            xet(p, (byte) 1, kieu, thamSo, n, null);
        } catch (final Exception e) {
            System.out.println("HanhDongNhiemVuGame.suKien " + kieu + ": " + e);
        }
    }

    /** NhiemVuEngine.khiHaQuai: quan sat (loai) / ha quai tinh gio (mau). */
    public static void khiHaQuai(final Char p, final Monster mt) {
        if (p == null || mt == null || p.charDBID <= 0 || BUOC.isEmpty()) {
            return;
        }
        try {
            xet(p, (byte) 0, null, null, 1, mt);
            xet(p, (byte) 1, null, null, 1, mt);
        } catch (final Exception e) {
            System.out.println("HanhDongNhiemVuGame.khiHaQuai: " + e);
        }
    }

    /** Gio hien tai cua may chu (0..23). */
    static int gio() {
        return com.htbb.pokemon.DongHo.lich().get(java.util.Calendar.HOUR_OF_DAY);
    }

    /** Con quai co phai loai {@code b} (quan sat: so dex cua mau hoac loai rieng nguoi choi thay). */
    static boolean dungLoai(final Char p, final Monster mt, final BuocHanhDong b) {
        final MonsterTemplate t = Map.monsterTemplates == null ? null : Map.monsterTemplates.get(Integer.valueOf(mt.idTemplate));
        if (t != null && t.dex > 0 && b.khopId(String.valueOf(t.dex))) {
            return true;
        }
        try {
            final int dex = LoaiQuaiGame.dexChoNguoi(p, mt);
            return dex > 0 && b.khopId(String.valueOf(dex));
        } catch (final RuntimeException e) {
            return false;
        }
    }

    static void xet(final Char p, final byte ms, final String kieu, final String thamSo, final int n, final Monster mt) {
        final NewCharQuest q = ms == 0 ? p.mainQuest : p.subQuest;
        if (q == null || q.id_quest < 0 || q.state_quest != NewCharQuest.WORKING) {
            return; // nhanh nhat: phan lon nhan vat dung o day
        }
        final InfoQuestTemplate info = q.getTemplate();
        final BuocHanhDong b = mau(info);
        if (b == null) {
            return;
        }
        final long khoa = (long) p.charDBID * 4L + ms;
        int moi;
        String bao = null;
        synchronized (p.khoaNhiemVu()) {
            if (q != (ms == 0 ? p.mainQuest : p.subQuest) || q.state_quest != NewCharQuest.WORKING) {
                return;
            }
            final int cu = da(q);
            if (mt == null) {
                if (!b.kieu.equals(kieu)) {
                    return;
                }
                moi = "nang_cap_cong".equals(kieu) ? BuocHanhDong.sauNangCap(cu, thamSo) : cu + n;
            } else if ("quan_sat".equals(b.kieu)) {
                if (!dungLoai(p, mt, b) || !b.dungGio(gio())) {
                    return;
                }
                moi = cu + 1;
            } else if ("thu_thach_gio".equals(b.kieu)) {
                if (!b.khopId(String.valueOf(mt.idTemplate))) {
                    return;
                }
                final Long bd = BAT_DAU.get(Long.valueOf(khoa));
                final long bay = com.htbb.pokemon.DongHo.bay();
                final long[] r = BuocHanhDong.thuThachGio(cu, bd == null ? 0L : bd.longValue(), bay, b.phut());
                if (r[1] != (bd == null ? 0L : bd.longValue())) {
                    BAT_DAU.put(Long.valueOf(khoa), Long.valueOf(r[1]));
                    bao = (cu > 0 ? "Hết giờ, đếm lại từ đầu. " : "") + "Bắt đầu tính giờ " + b.phut() + " phút: " + info.name + ".";
                }
                moi = (int) r[0];
            } else {
                return;
            }
            if (moi == cu) {
                return;
            }
            datDa(q, Math.min(moi, b.can));
            if (moi >= b.can) {
                q.state_quest = NewCharQuest.FINISH; // du: luu ngay (vao lai game van con), tra ben duoi
                BAT_DAU.remove(Long.valueOf(khoa));
            }
        }
        if (bao != null) {
            LauDaiNhanVat.bao(p, bao);
        }
        if (q.state_quest == NewCharQuest.FINISH) {
            xong(p, q, info, ms);
            return;
        }
        if (!"thu_thach_gio".equals(b.kieu)) {
            luu(p);
        }
        MessageCreator.createMsgCharQuest(p, 2); // tien do x/N trong so nhiem vu
    }

    static void luu(final Char p) {
        try {
            Database.instance.saveCharQuest(p.charDBID, p.getInfoQuestSave());
        } catch (final Exception e) {
            System.out.println("HanhDongNhiemVuGame.luu: " + e);
        }
    }

    /** Du muc tieu: tra nhu nguoi choi bam tra (thuong, sang buoc sau, luu); tui day -> giu "du", bao noi chuyen voi NPC. */
    static void xong(final Char p, final NewCharQuest q, final InfoQuestTemplate info, final byte ms) {
        luu(p);
        final short id = q.id_quest;
        try {
            p.OnQuest((byte) 1, id, ms);
        } catch (final RuntimeException e) {
            System.out.println("HanhDongNhiemVuGame.xong: " + e);
        }
        final NewCharQuest sau = ms == 0 ? p.mainQuest : p.subQuest;
        final boolean daTra = sau != q || sau.id_quest != id || sau.state_quest == NewCharQuest.DONE;
        try {
            if (daTra) {
                final String xong = info.infofinish == null ? "" : info.infofinish.replace("|", "\n");
                if (!xong.isEmpty()) {
                    p.sendMessage(MessageCreator.createServerAlertMessage(xong, ""));
                }
            } else {
                p.sendInfoQuest();
                p.sendMessage(MessageCreator.createServerAlertMessage("Đã xong mục tiêu (" + info.name
                        + "). Quay lại nói chuyện với NPC giao nhiệm vụ để nhận thưởng.", ""));
            }
        } catch (final RuntimeException e) {
            System.out.println("HanhDongNhiemVuGame.xong gui: " + e);
        }
    }

    /** Test: dat luc bat dau dem cua buoc ha quai tinh gio. */
    public static void datBatDauThu(final Char p, final byte ms, final long luc) {
        BAT_DAU.put(Long.valueOf((long) p.charDBID * 4L + ms), Long.valueOf(luc));
    }

    /** NhiemVuEngine.khiThoat. */
    public static void khiThoat(final Char p) {
        if (p != null && p.charDBID > 0) {
            BAT_DAU.remove(Long.valueOf((long) p.charDBID * 4L));
            BAT_DAU.remove(Long.valueOf((long) p.charDBID * 4L + 1L));
        }
    }
}
