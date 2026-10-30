package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuLauDai;
import data.Database;
import data.DatabaseLauDai;
import data.InfoQuestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nhiem vu khi bat co sv.lau_dai (GD2 buoc 4). Logic thuan o com.htbb.pokemon.NhiemVuLauDai.
 *
 *   - {@link #locDanhSach}: thay Char.getAllQuestCanReceive - loc phu theo cap (lv <= cap < lv+10),
 *     toi da 20, tong <= 127 (goi ghi so luong 1 byte);
 *   - {@link #kiemNhanChinh}: Char.OnQuest (nhan nhiem vu chinh) - cap, ngay mo, ve chuong tuan;
 *   - {@link #khiNap}: chuyen nhan vat cu sang chuoi chinh moi MOT lan (theo cap, vao dau chuong);
 *   - bang nhiem vu TUAN (2/tuan, ha quai +-5 cap) - menu o SoTayLauDai, tien do o
 *     tob_nhiem_vu_lau_dai, cong o {@link #khiHaQuai}.
 */
public final class NhiemVuLauDaiGame {

    /** Nhiem vu tuan dang lam: charDBID -> danh sach {ma, tienDo, trangThai, tuan}. */
    private static final ConcurrentHashMap<Integer, List<Object[]>> TUAN = new ConcurrentHashMap<Integer, List<Object[]>>();
    /** Dong nhiem vu tuan (data_quest_lau_dai), nap lan dau dung. */
    private static volatile List<Object[]> mauTuan;

    private NhiemVuLauDaiGame() {
    }

    // ------------------------------------------------------------------ nap / chuyen

    static void khiNap(final Char p, final HoSoLauDai h) {
        if (!LauDai.bat() || h == null) {
            return;
        }
        synchronized (h) {
            capNhatVe(h);
            if (h.nvChuyen != 0) {
                return;
            }
            h.nvChuyen = 1;
            h.nvCu = p.idMainQuest + 1; // de tat co thi tra ve dung cho cu (khiNapTatCo)
            h.ban = true;
        }
        // Nhan vat cu (id nhiem vu chinh tro vao bang CU): vao dau chuong phu hop cap hien tai.
        try {
            final int n = Map.allMainQuest.size();
            if (n == 0) {
                return;
            }
            final int[] lv = new int[n];
            final boolean[] dau = new boolean[n];
            for (int i = 0; i < n; i++) {
                final InfoQuestTemplate t = Map.allMainQuest.get((short) i);
                lv[i] = t != null ? t.lv : 999;
                dau[i] = t != null && t.dauChuong;
            }
            final int moi = p.lvDetail.lv <= 1 && p.idMainQuest == 0 ? 0
                    : NhiemVuLauDai.chiSoChinhChoCap(lv, dau, p.lvDetail.lv);
            p.idMainQuest = (short) moi;
            p.mainQuest = new NewCharQuest((short) moi, (byte) 0);
            Database.instance.saveCharQuest(p.charDBID, p.getInfoQuestSave());
            Database.instance.saveOrtherLog("", p.charname, "chuyen nhiem vu chinh lau dai -> " + moi, "laudai");
        } catch (final Exception e) {
            System.out.println("NhiemVuLauDaiGame.khiNap: " + e);
        }
    }

    /** Co bang tob_char_lau_dai co du lieu (ChuyenDoiLauDai dat luc khoi dong khi tat co). */
    public static volatile boolean coHoSoCu = false;

    /**
     * TAT co: nhan vat da duoc chuyen sang chuoi nhiem vu moi (nc=1) thi tra ve vi tri chuoi CU da
     * luu (nq) va xoa dau "da chuyen" - bat lai co thi chuyen lai theo cap. Doc thang CSDL.
     */
    public static void khiNapTatCo(final Char p) {
        if (LauDai.bat() || !coHoSoCu || p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final String s = data.DatabaseLauDai.napHoSoBatKe(p.charDBID);
            if (s == null || s.isEmpty()) {
                return;
            }
            final HoSoLauDai h = HoSoLauDai.giaiMa(s);
            if (h.nvChuyen == 0) {
                return;
            }
            final int cu = h.nvCu > 0 ? Math.min(h.nvCu - 1, Math.max(0, Map.allMainQuest.size() - 1)) : 0;
            p.idMainQuest = (short) cu;
            p.mainQuest = new NewCharQuest((short) cu, (byte) 0);
            h.nvChuyen = 0;
            h.nvCu = 0;
            data.DatabaseLauDai.luuHoSo(p.charDBID, h.maHoa());
            Database.instance.saveCharQuest(p.charDBID, p.getInfoQuestSave());
            Database.instance.saveOrtherLog("", p.charname, "tat lau dai: tra nhiem vu chinh ve " + cu, "laudai");
        } catch (final Exception e) {
            System.out.println("NhiemVuLauDaiGame.khiNapTatCo: " + e);
        }
    }

    static void capNhatVe(final HoSoLauDai h) {
        final int[] v = NhiemVuLauDai.capNhatVe(h.veChuong, h.veTuanCuoi, LauDaiNhanVat.tuanNay());
        if (v[0] != h.veChuong || v[1] != h.veTuanCuoi) {
            h.veChuong = v[0];
            h.veTuanCuoi = v[1];
            h.ban = true;
        }
    }

    // ------------------------------------------------------------------ danh sach / nhan

    /** Thay Char.getAllQuestCanReceive khi bat co. */
    /**
     * Qua ngay (dang nhap ngay moi / doi ngay luc dang choi). He cu (tat sv.lau_dai): nhiem vu phu la viec hang ngay -> huy
     * nhu ban goc. Bat sv.lau_dai: nhiem vu phu la chuoi theo thi tran lam MOT lan (P-*, 3-5 buoc, khong ton diem hoat
     * dong) -> GIU nguyen tien do. Truoc 30/10/2026 ca hai duong deu xoa phu dang lam luc 0h, khong bao: nguoi choi dang ha
     * 20/35 Kricketot qua nua dem la mat het (bot phu kin: nhan lai cung mot nhiem vu phu 3-4 lan).
     * @return true neu da huy (can gui lai danh sach nhiem vu)
     */
    public static boolean huyPhuQuaNgay(final Char p) {
        if (p == null || p.subQuest == null || p.subQuest.id_quest <= -1 || LauDai.bat()) {
            return false;
        }
        p.subQuest.reset();
        p.subQuest.id_quest = -1;
        return true;
    }

    public static Vector<NewCharQuest> locDanhSach(final Char p) {
        final Vector<NewCharQuest> ds = new Vector<NewCharQuest>();
        if (p.mainQuest.state_quest == NewCharQuest.UN_RECEIVE) {
            ds.add(p.mainQuest);
        }
        if (p.subQuest.id_quest == -1) {
            final List<InfoQuestTemplate> phu = new ArrayList<InfoQuestTemplate>();
            for (final InfoQuestTemplate t : Map.allSubQuest.values()) {
                if (!NhiemVuEngine.phuCuDaDung(p, t.ma)) {
                    phu.add(t); // 0.9.7.1: da nhan / xong o So tay hoac da tra qua NPC -> khong moi lai
                }
            }
            final int[] lv = new int[phu.size()];
            for (int i = 0; i < lv.length; i++) {
                lv[i] = phu.get(i).lv;
            }
            for (final Integer i : NhiemVuLauDai.locPhu(lv, p.lvDetail.lv, NhiemVuLauDai.PHU_TOI_DA)) {
                ds.add(new NewCharQuest(phu.get(i.intValue()).id, (byte) 1));
            }
        }
        if (p.clanQuestNew.id_quest == -1 && p.idClan > -1 && p.countQuestClan < 10) {
            for (final InfoQuestTemplate info : Map.allClanQuest.values()) {
                if (ds.size() >= NhiemVuLauDai.GOI_TOI_DA) {
                    break;
                }
                ds.add(new NewCharQuest(info.id, (byte) 2));
            }
        }
        while (ds.size() > NhiemVuLauDai.GOI_TOI_DA) {
            ds.remove(ds.size() - 1);
        }
        return ds;
    }

    /**
     * Kiem truoc khi nhan nhiem vu chinh; neu duoc va can ve thi TRU ve ngay (danh dau truoc).
     * @return null neu nhan duoc, nguoc lai ly do
     */
    public static String kiemNhanChinh(final Char p, final InfoQuestTemplate info) {
        if (!LauDai.bat() || info == null) {
            return null;
        }
        if (p.mainQuest == null || p.mainQuest.state_quest != NewCharQuest.UN_RECEIVE) {
            return "Bạn đang làm nhiệm vụ này rồi."; // goi nhan lap lai khong duoc tru them ve
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return "Tạm thời không nhận được nhiệm vụ, thử lại sau.";
        }
        synchronized (h) {
            capNhatVe(h);
            final String lyDo = NhiemVuLauDai.kiemChinh(info.lv, p.lvDetail.lv, info.ngayMo, LauDai.ngayMoNoiDung(),
                    info.canVe, h.veChuong);
            if (lyDo != null) {
                return lyDo;
            }
            if (info.canVe) {
                h.veChuong--;
                h.ban = true;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ nhiem vu tuan

    static List<Object[]> mau() {
        List<Object[]> m = mauTuan;
        if (m == null) {
            m = DatabaseLauDai.napNhiemVuTuan();
            mauTuan = m;
        }
        return m;
    }

    static List<Object[]> cuaToi(final Char p) {
        final Integer k = Integer.valueOf(p.charDBID);
        List<Object[]> l = TUAN.get(k);
        if (l == null) {
            final List<Object[]> moi = DatabaseLauDai.napNhiemVu(p.charDBID);
            if (moi == null) {
                return null;
            }
            l = java.util.Collections.synchronizedList(moi);
            final List<Object[]> cu = TUAN.putIfAbsent(k, l);
            if (cu != null) {
                l = cu;
            }
        }
        return l;
    }

    static Object[] timMau(final String ma) {
        for (final Object[] m : mau()) {
            if (m[0].equals(ma)) {
                return m;
            }
        }
        return null;
    }

    /** So nhiem vu tuan da nhan trong tuan nay. */
    static int daNhanTuanNay(final List<Object[]> l, final int tuan) {
        int n = 0;
        synchronized (l) {
            for (final Object[] d : l) {
                if (((Integer) d[3]).intValue() == tuan && ((String) d[0]).startsWith("T-")) {
                    n++;
                }
            }
        }
        return n;
    }

    /** Danh sach nhiem vu tuan nhan duoc cua nhan vat (theo bac cap), toi da 12 dong. */
    static List<Object[]> coTheNhan(final Char p) {
        final List<Object[]> kq = new ArrayList<Object[]>();
        final String tienTo = "T-" + NhiemVuLauDai.bacCap(p.lvDetail.lv) + ".";
        for (final Object[] m : mau()) {
            if (((String) m[0]).startsWith(tienTo) && ((Integer) m[2]).intValue() <= p.lvDetail.lv) {
                kq.add(m);
            }
        }
        return kq;
    }

    /** Nhan nhiem vu tuan {@code ma}. @return thong bao. */
    static String nhanTuan(final Char p, final String ma) {
        final List<Object[]> l = cuaToi(p);
        final Object[] m = timMau(ma);
        if (l == null || m == null) {
            return "Không nhận được nhiệm vụ tuần.";
        }
        final int tuan = LauDaiNhanVat.tuanNay();
        synchronized (l) {
            if (daNhanTuanNay(l, tuan) >= com.htbb.pokemon.ThongSoLauDai.GIOI_HAN_TUAN) {
                return "Mỗi tuần chỉ nhận " + com.htbb.pokemon.ThongSoLauDai.GIOI_HAN_TUAN + " nhiệm vụ tuần.";
            }
            for (final Object[] d : l) {
                if (d[0].equals(ma) && ((Integer) d[3]).intValue() == tuan) {
                    return "Đã nhận nhiệm vụ này tuần này.";
                }
            }
            l.removeIf(d -> d[0].equals(ma));
            l.add(new Object[]{ma, Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(tuan)});
        }
        DatabaseLauDai.luuNhiemVu(p.charDBID, ma, 0, 0, tuan);
        return "Đã nhận: " + m[1] + " (hạ " + m[3] + " quái chênh không quá 5 cấp).";
    }

    /** Ha mot quai: cong tien do nhiem vu tuan dang lam (quai +-5 cap). */
    static void khiHaQuai(final Char p, final Monster mt) {
        if (!LauDai.bat() || Math.abs(p.lvDetail.lv - mt.level) > 5) {
            return;
        }
        final List<Object[]> l = TUAN.get(Integer.valueOf(p.charDBID));
        if (l == null) {
            return;
        }
        final int tuan = LauDaiNhanVat.tuanNay();
        final List<Object[]> luu = new ArrayList<Object[]>();
        synchronized (l) {
            for (final Object[] d : l) {
                if (((Integer) d[2]).intValue() != 0 || ((Integer) d[3]).intValue() != tuan) {
                    continue;
                }
                final Object[] m = timMau((String) d[0]);
                if (m == null) {
                    continue;
                }
                final int td = ((Integer) d[1]).intValue() + 1;
                d[1] = Integer.valueOf(td);
                if (td >= ((Integer) m[3]).intValue()) {
                    d[2] = Integer.valueOf(1);
                    luu.add(d);
                } else if (td % 25 == 0) {
                    luu.add(d);
                }
            }
        }
        for (final Object[] d : luu) {
            DatabaseLauDai.luuNhiemVu(p.charDBID, (String) d[0], ((Integer) d[1]).intValue(), ((Integer) d[2]).intValue(),
                    ((Integer) d[3]).intValue());
            if (((Integer) d[2]).intValue() == 1) {
                LauDaiNhanVat.bao(p, "Đã hoàn thành nhiệm vụ tuần. Nhận thưởng: " + com.htbb.pokemon.DuongMenu.duong(com.htbb.pokemon.DuongMenu.QUAY, com.htbb.pokemon.DuongMenu.SO_TAY, "Nhiệm vụ tuần") + ".");
            }
        }
    }

    /** Nhan thuong mot nhiem vu tuan da xong (danh dau CO DIEU KIEN truoc, phat sau). */
    static String nhanThuong(final Char p, final String ma) {
        final List<Object[]> l = cuaToi(p);
        final Object[] m = timMau(ma);
        if (l == null || m == null) {
            return "Không tìm thấy nhiệm vụ.";
        }
        Object[] d = null;
        synchronized (l) {
            for (final Object[] x : l) {
                if (x[0].equals(ma)) {
                    d = x;
                }
            }
        }
        if (d == null || ((Integer) d[2]).intValue() != 1) {
            return "Nhiệm vụ chưa hoàn thành.";
        }
        DatabaseLauDai.luuNhiemVu(p.charDBID, ma, ((Integer) d[1]).intValue(), 1, ((Integer) d[3]).intValue());
        if (!DatabaseLauDai.danhDauDaNhan(p.charDBID, ma)) {
            return "Đã nhận thưởng rồi.";
        }
        d[2] = Integer.valueOf(2);
        final long exp = ((Long) m[4]).longValue();
        final long xu = ((Long) m[5]).longValue();
        final int lk = ((Integer) m[6]).intValue();
        try {
            Map.addXPForChar(p, exp, false, "nhiem vu tuan " + ma);
        } catch (final Exception ignored) {
        }
        p.addXu(xu, "nhiem vu tuan " + ma);
        p.addLuongLock(lk);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        Database.instance.saveOrtherLog("", p.charname, "nhiem vu tuan " + ma + ": " + exp + " exp " + xu + " xu " + lk + " lượng khoá", "laudai");
        return "Nhận thưởng: " + com.htbb.pokemon.TienTeChu.so(exp) + " EXP, " + com.htbb.pokemon.TienTeChu.chu(com.htbb.pokemon.TienTeChu.XU, xu) + ", " + com.htbb.pokemon.TienTeChu.chu(com.htbb.pokemon.TienTeChu.LUONG_KHOA, lk) + ".";
    }

    /** Mo ta trang thai nhiem vu tuan cho menu. */
    static List<String[]> trangThai(final Char p) {
        final List<String[]> kq = new ArrayList<String[]>();
        final List<Object[]> l = cuaToi(p);
        if (l == null) {
            return kq;
        }
        final int tuan = LauDaiNhanVat.tuanNay();
        synchronized (l) {
            for (final Object[] d : l) {
                if (((Integer) d[3]).intValue() != tuan || !((String) d[0]).startsWith("T-")) {
                    continue;
                }
                final Object[] m = timMau((String) d[0]);
                final int st = ((Integer) d[2]).intValue();
                kq.add(new String[]{(String) d[0], (m != null ? m[1] : d[0]) + ": " + d[1] + "/" + (m != null ? m[3] : "?")
                    + (st == 1 ? " (nhận thưởng)" : st == 2 ? " (đã nhận)" : "")});
            }
        }
        return kq;
    }
}
