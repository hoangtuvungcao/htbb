package real;

import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.ChongCloneLogic;
import com.htbb.pokemon.CupLogic;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.PhongRieng;
import data.Database;
import data.DatabasePhoBanCup;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CUP PVP TUAN (0.9.7) - phan GAME. Che do RIENG voi thang PvP mua / PK the gioi (nhanh danh-hieu dang sua):
 * tran cup dien ra trong khu rieng cua san {@link PhongRiengGame#MAP_CUP}, dung duong danh nguoi cua Region chi qua
 * hai diem moc (choDanh / satThuong / sauDon) - KHONG goi ThangPvpGame, KHONG co do sat, HP khong ve 0.
 *
 * <ul>
 *   <li>Lich / xoay vong / ghep / thuong: com.htbb.pokemon.CupLogic (thuan). Du lieu: data_cup_loai, data_cup_lich.</li>
 *   <li>Tu bat / tat theo lich: tuan nao cung co cup; web tu dang tin qua lich nam (ma {@link CupLogic#MA_LICH},
 *       nhom "Cúp PvP" trong tob_su_kien_lich). Admin tat mot tuan o /admin/sukien (ghi_de = 1) thi tuan do khong
 *       chay - khong can khoi dong lai.</li>
 *   <li>Dang ky / huy: So tay huan luyen -&gt; "Cúp PvP tuần". Cup Doi: doi truong to doi 2 nguoi dang ky ca doi.</li>
 *   <li>Chong clone: cong cap / tuoi tai khoan / gio choi (nguong chong_clone.*); moi thiet bi / tai khoan mot
 *       Pokemon moi tuan; hai doi chung thiet bi / IP / tai khoan khong bao gio gap nhau (CupLogic.ghep).</li>
 *   <li>Thuong: tob_cup_thuong (INSERT IGNORE + da_nhan) - nguoi offline nhan khi dang nhap, khong bao gio phat
 *       hai lan.</li>
 * </ul>
 */
public final class CupGame {

    static final int MENU = 70, MENU_HUY = 71, MENU_BO = 72, MENU_XEM = 73;
    /** Muc cuoi moi menu cup: mo PK 3 vs 3 buoi toi (real.Pk3v3Game) - duong cho client cu qua Menu -> Sự kiện -> Cúp PvP tuần. */
    static final String MUC_PK3V3 = com.htbb.pokemon.Pk3v3.TEN;
    public static final String K_BAT = "cup.bat", K_GIO = "cup.gio_vong_dau", K_SO_VONG = "cup.so_vong",
            K_SO_DOI = "cup.so_doi_toi_thieu";

    static boolean bat() {
        return com.htbb.cauhinh.CauHinhTrungTam.so(K_BAT, 1) == 1 && PhongRiengGame.cup != null && LauDai.bat();
    }

    static int gioVongDau() {
        return (int) Math.max(8, Math.min(22, com.htbb.cauhinh.CauHinhTrungTam.so(K_GIO, CupLogic.GIO_VONG_DAU)));
    }

    static int soVong() {
        return (int) Math.max(1, Math.min(8, com.htbb.cauhinh.CauHinhTrungTam.so(K_SO_VONG, CupLogic.SO_VONG)));
    }

    static int soDoiToiThieu() {
        return (int) Math.max(2, Math.min(64, com.htbb.cauhinh.CauHinhTrungTam.so(K_SO_DOI, CupLogic.SO_DOI_TOI_THIEU)));
    }

    // ================================================================== trang thai tuan

    static final class Tuan {
        final int so;
        final int namIso, tuanIso;
        final CupLogic.Loai loai;
        boolean tat;
        long kiemTatLuc;
        final List<DatabasePhoBanCup.DangKy> dk = new ArrayList<DatabasePhoBanCup.DangKy>();
        final LinkedHashMap<Integer, CupLogic.Doi> doi = new LinkedHashMap<Integer, CupLogic.Doi>();
        int vongDaGhep = -1;
        boolean daNhacTruoc, huy, daChot;

        Tuan(final int so, final int[] iso, final CupLogic.Loai loai) {
            this.so = so;
            this.namIso = iso[0];
            this.tuanIso = iso[1];
            this.loai = loai;
        }

        List<Integer> thanhVien(final int doiId) {
            final List<Integer> ds = new ArrayList<Integer>();
            for (final DatabasePhoBanCup.DangKy k : dk) {
                if (k.doiId == doiId) {
                    ds.add(Integer.valueOf(k.charId));
                }
            }
            return ds;
        }

        DatabasePhoBanCup.DangKy dangKy(final int charId) {
            for (final DatabasePhoBanCup.DangKy k : dk) {
                if (k.charId == charId) {
                    return k;
                }
            }
            return null;
        }

        String tenDoi(final int doiId) {
            final StringBuilder b = new StringBuilder();
            for (final DatabasePhoBanCup.DangKy k : dk) {
                if (k.doiId == doiId) {
                    b.append(b.length() == 0 ? "" : " & ").append(k.ten);
                }
            }
            return b.length() == 0 ? "?" : b.toString();
        }
    }

    static volatile Tuan HIEN;

    static void napDuLieu() {
        try {
            CupLogic.datDuLieu(DatabasePhoBanCup.napLoaiCup(), DatabasePhoBanCup.napLichCup());
        } catch (final Throwable e) {
            System.out.println("CupGame.napDuLieu: " + e);
        }
    }

    /** Tuan hien tai (nap lai tu CSDL khi sang tuan moi / lan dau). */
    static synchronized Tuan tuan(final long bay) {
        final int so = CupLogic.tuan(bay);
        Tuan t = HIEN;
        if (t == null || t.so != so) {
            napDuLieu();
            t = new Tuan(so, CupLogic.namTuanIso(bay), CupLogic.loaiTuan(so));
            t.dk.addAll(DatabasePhoBanCup.napDangKy(so));
            int thuTu = 0;
            for (final DatabasePhoBanCup.DangKy k : t.dk) {
                if (!t.doi.containsKey(Integer.valueOf(k.doiId))) {
                    final CupLogic.Doi d = new CupLogic.Doi(k.doiId, thuTu++, CupLogic.khoaClone(k.may, k.ip, k.userId));
                    d.daMien = k.mien;
                    t.doi.put(Integer.valueOf(k.doiId), d);
                } else {
                    t.doi.get(Integer.valueOf(k.doiId)).khoa.addAll(CupLogic.khoaClone(k.may, k.ip, k.userId));
                }
            }
            for (final int[] r : DatabasePhoBanCup.napTran(so)) {
                t.vongDaGhep = Math.max(t.vongDaGhep, r[0]);
                final CupLogic.Doi a = t.doi.get(Integer.valueOf(r[1]));
                final CupLogic.Doi b = r[2] < 0 ? null : t.doi.get(Integer.valueOf(r[2]));
                if (a == null || r[3] == 0) {
                    continue;
                }
                if (b == null) {
                    CupLogic.ghiMien(a);
                } else {
                    CupLogic.ghiTran(a, b, r[3]);
                }
            }
            for (final DatabasePhoBanCup.DangKy k : t.dk) {
                if (k.hang > 0) {
                    t.daChot = true;
                }
            }
            HIEN = t;
        }
        if (bay - t.kiemTatLuc > 60000L) {
            t.kiemTatLuc = bay;
            t.tat = DatabasePhoBanCup.tuanBiTat(t.namIso, t.tuanIso);
        }
        return t;
    }

    // ================================================================== menu

    static void moMenu(final Char p) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        final Tuan t = tuan(bay);
        final int[] ph = CupLogic.pha(bay, gioVongDau(), CupLogic.PHUT_MOI_VONG, soVong());
        final DatabasePhoBanCup.DangKy k = t.dangKy(p.charDBID);
        final StringBuilder b = new StringBuilder();
        b.append(t.loai.ten).append(" - tuần ").append(t.tuanIso).append('\n').append(t.loai.moTa).append('\n')
                .append("Luật: ").append(t.loai.luat()).append(".\n")
                .append(CupLogic.lichChu(gioVongDau(), soVong(), CupLogic.PHUT_MOI_VONG)).append('\n')
                .append("Đã đăng ký: ").append(t.doi.size()).append(t.loai.soNguoi == 2 ? " đội" : " người").append('\n')
                .append("Thưởng: ").append(CupLogic.phanThuongChu());
        if (!bat() || t.tat) {
            b.append("\n\nCúp tuần này tạm đóng.");
        } else if (k != null) {
            final CupLogic.Doi d = t.doi.get(Integer.valueOf(k.doiId));
            b.append("\n\nBạn đã đăng ký").append(d == null ? "." : ": " + d.diem + " điểm, thắng " + d.thang + " / hoà " + d.hoa + " / thua " + d.thua + ".");
        }
        p.sendMessage(MessageCreator.createServerAlertMessage(b.toString(), ""));
        if (!bat() || t.tat || ph[0] != CupLogic.PHA_DANG_KY) {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Bảng xếp hạng tuần này", MUC_PK3V3}, SoTayLauDai.NPC_ID, MENU_XEM));
            return;
        }
        if (k != null) {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Bảng xếp hạng tuần này", "Huỷ đăng ký", MUC_PK3V3}, SoTayLauDai.NPC_ID,
                    MENU_HUY));
        } else {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{t.loai.soNguoi == 2 ? "Đăng ký cả đội (2 người)" : "Đăng ký",
                "Bảng xếp hạng tuần này", MUC_PK3V3}, SoTayLauDai.NPC_ID, MENU));
        }
    }

    static boolean doMenu(final Char p, final int idMenu, final int idOption) {
        if (idMenu == MENU) {
            if (idOption == 0) {
                final String loi = dangKy(p);
                if (loi != null) {
                    PhongRiengGame.loi(p, loi);
                }
            } else if (idOption == 1) {
                bangXepHang(p);
            } else if (idOption == 2) {
                Pk3v3Game.moMenu(p);
            }
            return true;
        }
        if (idMenu == MENU_XEM) {
            if (idOption == 0) {
                bangXepHang(p);
            } else if (idOption == 1) {
                Pk3v3Game.moMenu(p);
            }
            return true;
        }
        if (idMenu == MENU_BO) {
            if (idOption == 0) {
                boCuoc(p);
            }
            return true;
        }
        if (idMenu == MENU_HUY) {
            if (idOption == 0) {
                bangXepHang(p);
            } else if (idOption == 1) {
                final String loi = huy(p);
                if (loi != null) {
                    PhongRiengGame.loi(p, loi);
                }
            } else if (idOption == 2) {
                Pk3v3Game.moMenu(p);
            }
            return true;
        }
        return false;
    }

    static void bangXepHang(final Char p) {
        final Tuan t = tuan(com.htbb.pokemon.DongHo.bay());
        final List<CupLogic.Doi> ds = CupLogic.xepHang(new ArrayList<CupLogic.Doi>(t.doi.values()));
        final StringBuilder b = new StringBuilder(t.loai.ten + " - bảng xếp hạng\n");
        for (int i = 0; i < Math.min(10, ds.size()); i++) {
            final CupLogic.Doi d = ds.get(i);
            b.append(i + 1).append(". ").append(t.tenDoi(d.id)).append(": ").append(d.diem).append(" điểm (")
                    .append(d.thang).append("-").append(d.hoa).append("-").append(d.thua).append(")\n");
        }
        if (ds.isEmpty()) {
            b.append("Chưa có ai đăng ký.");
        }
        b.append("\nXem đầy đủ trên website, mục Cúp PvP.");
        p.sendMessage(MessageCreator.createServerAlertMessage(b.toString(), ""));
    }

    // ================================================================== dang ky

    /** Nguong tuoi tai khoan / gio choi cua cong chong clone (chong_clone.*), ap cho cup. */
    static int[] nguongClone() {
        final ChongCloneLogic.CauHinh c = ChongClone.cauHinh();
        return c.bat ? new int[]{c.tuoiTaiKhoanGio, c.gioChoiToiThieu} : new int[]{0, 0};
    }

    static String dangKy(final Char p) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        final Tuan t = tuan(bay);
        if (!bat() || t.tat) {
            return "Cúp tuần này tạm đóng.";
        }
        if (CupLogic.pha(bay, gioVongDau(), CupLogic.PHUT_MOI_VONG, soVong())[0] != CupLogic.PHA_DANG_KY) {
            return "Đã hết giờ đăng ký cúp tuần này.";
        }
        final List<Char> ds = new ArrayList<Char>();
        if (t.loai.soNguoi == 2) {
            if (!PhoBanGame.laTruong(p) || p.partyID == -1) {
                return "Cúp Đôi: đội trưởng của tổ đội 2 người đăng ký cho cả đội.";
            }
            ds.addAll(PhoBanGame.doiCua(p));
            if (ds.size() != 2) {
                return "Cúp Đôi cần tổ đội đúng 2 người đang online.";
            }
        } else {
            ds.add(p);
        }
        final int[] ng = nguongClone();
        final List<Set<String>> khoa = new ArrayList<Set<String>>();
        for (final Char c : ds) {
            if (t.dangKy(c.charDBID) != null) {
                return (c == p ? "Bạn" : c.charname) + " đã đăng ký cúp tuần này.";
            }
            final ChongCloneLogic.Ben b = ChongClone.ben(c, true);
            final String loi = CupLogic.kiemThanhVien(t.loai, c == p ? "Bạn" : c.charname, c.lvDetail.lv, c.hePokemon1(),
                    c.hePokemon2(), b.tuoiTaiKhoanMs < 0 ? -1L : b.tuoiTaiKhoanMs / 3600000L, b.phutChoi < 0 ? -1L : b.phutChoi / 60L,
                    ng[0], ng[1]);
            if (loi != null) {
                return loi;
            }
            final Set<String> kh = CupLogic.khoaClone(b.may, "", c.userID);
            for (final DatabasePhoBanCup.DangKy k : t.dk) {
                if (CupLogic.trungClone(kh, CupLogic.khoaClone(k.may, "", k.userId))) {
                    return "Mỗi thiết bị / tài khoản chỉ dự cúp bằng một Pokémon mỗi tuần (" + k.ten + " đã đăng ký).";
                }
            }
            for (final Set<String> x : khoa) {
                if (CupLogic.trungClone(kh, x)) {
                    return "Hai thành viên của đội không được dùng chung thiết bị / tài khoản.";
                }
            }
            khoa.add(kh);
        }
        final int doiId = p.charDBID;
        final CupLogic.Doi d = new CupLogic.Doi(doiId, t.doi.size(), new java.util.HashSet<String>());
        synchronized (t) {
            for (final Char c : ds) {
                final ChongCloneLogic.Ben b = ChongClone.ben(c, false);
                final DatabasePhoBanCup.DangKy k = new DatabasePhoBanCup.DangKy();
                k.charId = c.charDBID;
                k.doiId = doiId;
                k.maLoai = t.loai.ma;
                k.ten = c.charname;
                k.cap = c.lvDetail.lv;
                k.may = b.may;
                k.ip = b.ip;
                k.userId = c.userID;
                k.thuTu = t.doi.size();
                DatabasePhoBanCup.ghiDangKy(t.so, k);
                t.dk.add(k);
                d.khoa.addAll(CupLogic.khoaClone(b.may, b.ip, c.userID));
            }
            t.doi.put(Integer.valueOf(doiId), d);
        }
        for (final Char c : ds) {
            PhongRiengGame.bao(c, "Đã đăng ký " + t.loai.ten, "Chủ nhật " + gioVongDau() + ":00 thi đấu - nhớ online");
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "cup dang ky tuan " + t.so + " " + t.loai.ma, "laudai");
        } catch (final Exception ignored) {
        }
        return null;
    }

    static String huy(final Char p) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        final Tuan t = tuan(bay);
        final DatabasePhoBanCup.DangKy k = t.dangKy(p.charDBID);
        if (k == null) {
            return "Bạn chưa đăng ký cúp tuần này.";
        }
        if (CupLogic.pha(bay, gioVongDau(), CupLogic.PHUT_MOI_VONG, soVong())[0] != CupLogic.PHA_DANG_KY) {
            return "Đã khoá danh sách, không huỷ được nữa.";
        }
        if (k.doiId != p.charDBID) {
            return "Chỉ người đăng ký (đội trưởng) mới huỷ được.";
        }
        synchronized (t) {
            t.dk.removeIf(x -> x.doiId == k.doiId);
            t.doi.remove(Integer.valueOf(k.doiId));
        }
        DatabasePhoBanCup.huyDangKy(t.so, k.doiId);
        PhongRiengGame.bao(p, "Đã huỷ đăng ký cúp", "");
        return null;
    }

    // ================================================================== tran

    static final class Tran {
        final int khu, vong;
        final int doiA, doiB;
        final List<Integer> a = new ArrayList<Integer>(), b = new ArrayList<Integer>();
        final java.util.Map<Integer, PhongRiengGame.ViTri> cu = new java.util.HashMap<Integer, PhongRiengGame.ViTri>();
        final Set<Integer> loai = ConcurrentHashMap.newKeySet();
        final long batDau;
        volatile int ketQua;
        volatile long ketThucLuc;

        Tran(final int khu, final int vong, final int doiA, final int doiB, final long batDau) {
            this.khu = khu;
            this.vong = vong;
            this.doiA = doiA;
            this.doiB = doiB;
            this.batDau = batDau;
        }

        boolean coMat(final int charId) {
            return a.contains(Integer.valueOf(charId)) || b.contains(Integer.valueOf(charId));
        }

        boolean cungDoi(final int x, final int y) {
            return a.contains(Integer.valueOf(x)) && a.contains(Integer.valueOf(y)) || b.contains(Integer.valueOf(x)) && b.contains(Integer.valueOf(y));
        }
    }

    /** Giay chuan bi dau tran (chua danh duoc). */
    static final int GIAY_CHUAN_BI = 5;

    static final ConcurrentHashMap<Integer, Tran> TRAN = new ConcurrentHashMap<Integer, Tran>();

    static Tran tranCua(final int charId) {
        for (final Tran t : TRAN.values()) {
            if (t.coMat(charId) && t.ketQua == 0) {
                return t;
            }
        }
        return null;
    }

    public static boolean dangThiDau(final int charId) {
        return tranCua(charId) != null;
    }

    /** HOOK Region.doAttackPlayer (ban do phong rieng): p danh duoc c khong (cung tran, khac doi, ca hai chua bi loai). */
    static boolean choDanh(final Char p, final Char c) {
        if (p == null || c == null || p.mapID != PhongRiengGame.MAP_CUP) {
            return false;
        }
        final Tran t = TRAN.get(Integer.valueOf(p.region));
        if (t == null || t.ketQua != 0 || com.htbb.pokemon.DongHo.bay() - t.batDau < GIAY_CHUAN_BI * 1000L) {
            return false;
        }
        final Integer x = Integer.valueOf(p.charDBID);
        final Integer y = Integer.valueOf(c.charDBID);
        return t.coMat(p.charDBID) && t.coMat(c.charDBID) && !t.cungDoi(p.charDBID, c.charDBID)
                && !t.loai.contains(x) && !t.loai.contains(y);
    }

    /** p dang thi dau trong mot tran cup chua ket thuc (ngu canh PvP ngam tay, PhongRiengGame.NGU_CANH_CUP). */
    static boolean dangThiDau(final Char p) {
        final Tran t = TRAN.get(Integer.valueOf(p.region));
        return t != null && t.ketQua == 0 && t.coMat(p.charDBID);
    }

    /** HOOK Region.doAttackPlayer: sat thuong chuan hoa cua loai cup tuan nay. {@code d} = sat thuong tho (truoc x0,30). */
    static int satThuong(final Char p, final Char c, final long d) {
        final Tuan t = HIEN;
        final CupLogic.Loai l = t != null ? t.loai : CupLogic.loaiTuan(CupLogic.tuan(com.htbb.pokemon.DongHo.bay()));
        final long tho = d * com.htbb.pokemon.SatThuongLauDai.PVP_PHAN_TRAM / 100L;
        return CupLogic.satThuong(tho, Math.max(1, p.maxhp), Math.max(1, c.maxhp), l.phanNghinMin, l.phanNghinMax);
    }

    /** HOOK Region.doAttackPlayer sau khi tru HP: het HP thi bi loai (HP = 1), doi het nguoi thi ket thuc tran. */
    static void sauDon(final Char p, final Char c) {
        if (c.hp > 0) {
            return;
        }
        c.hp = 1;
        final Tran t = TRAN.get(Integer.valueOf(c.region));
        if (t == null || !t.coMat(c.charDBID)) {
            return;
        }
        t.loai.add(Integer.valueOf(c.charDBID));
        PhongRiengGame.loi(c, "Bạn đã bị loại khỏi trận cúp");
        kiemHetNguoi(t, com.htbb.pokemon.DongHo.bay());
    }

    /** Bo cuoc giua tran: bi loai (doi het nguoi thi thua). */
    static void boCuoc(final Char p) {
        final Tran t = tranCua(p.charDBID);
        if (t == null) {
            return;
        }
        t.loai.add(Integer.valueOf(p.charDBID));
        PhongRiengGame.loi(p, "Bạn đã bỏ cuộc trận cúp");
        kiemHetNguoi(t, com.htbb.pokemon.DongHo.bay());
    }

    static void kiemHetNguoi(final Tran t, final long bay) {
        boolean hetA = true;
        boolean hetB = true;
        for (final Integer id : t.a) {
            hetA &= t.loai.contains(id);
        }
        for (final Integer id : t.b) {
            hetB &= t.loai.contains(id);
        }
        if (hetA || hetB) {
            ketThucTran(t, hetA && hetB ? CupLogic.HOA : (hetA ? CupLogic.B_THANG : CupLogic.A_THANG), bay);
        }
    }

    // ================================================================== nhip

    static void nhip(final long bay) {
        if (!LauDai.bat()) {
            return;
        }
        final Tuan t = tuan(bay);
        for (final Tran tr : TRAN.values()) {
            try {
                nhipTran(tr, bay);
            } catch (final Throwable e) {
                System.out.println("CupGame.nhipTran: " + e);
            }
        }
        if (!bat() || t.tat || t.huy) {
            return;
        }
        final int[] ph = CupLogic.pha(bay, gioVongDau(), CupLogic.PHUT_MOI_VONG, soVong());
        if (ph[0] == CupLogic.PHA_CHO_VONG && !t.daNhacTruoc) {
            t.daNhacTruoc = true;
            if (t.doi.size() < soDoiToiThieu()) {
                t.huy = true;
                com.htbb.chongspam.ThongBaoGop.dua("cup", t.loai.ten + ": chưa đủ " + soDoiToiThieu() + " đội đăng ký, tuần này hoãn.");
                return;
            }
            com.htbb.chongspam.ThongBaoGop.dua("cup", t.loai.ten + " bắt đầu lúc " + gioVongDau() + ":00 với " + t.doi.size()
                    + (t.loai.soNguoi == 2 ? " đội" : " người") + " - hãy online để được ghép trận!");
        }
        if (ph[0] == CupLogic.PHA_VONG && ph[1] > t.vongDaGhep) {
            if (t.doi.size() < soDoiToiThieu()) {
                t.huy = true;
                return;
            }
            ghepVong(t, ph[1], bay);
        }
        if (ph[0] == CupLogic.PHA_XONG && !t.daChot && TRAN.isEmpty()) {
            if (!CupLogic.daDienRa(t.doi.values())) {
                t.huy = true; // khong ai dau tran nao (tuan hoan / khoi dong lai sau khi hoan): khong chot, khong bao vo dich
                return;
            }
            chot(t);
        }
    }

    static boolean coMatDu(final Tuan t, final int doiId) {
        for (final Integer id : t.thanhVien(doiId)) {
            final Char c = PhongRiengGame.online(id.intValue());
            if (c == null || c.hp <= 0 || PhongRieng.laMapRieng(c.mapID) || PhoBanGame.phongCua(c.charDBID) != null) {
                return false;
            }
        }
        return !t.thanhVien(doiId).isEmpty();
    }

    static void ghepVong(final Tuan t, final int vong, final long bay) {
        synchronized (t) {
            if (vong <= t.vongDaGhep) {
                return;
            }
            t.vongDaGhep = vong;
        }
        final List<CupLogic.Doi> co = new ArrayList<CupLogic.Doi>();
        for (final CupLogic.Doi d : t.doi.values()) {
            if (coMatDu(t, d.id)) {
                co.add(d);
            } else {
                for (final Integer id : t.thanhVien(d.id)) {
                    final Char c = PhongRiengGame.online(id.intValue());
                    if (c != null) {
                        PhongRiengGame.loi(c, t.loai.ten + " vòng " + (vong + 1) + ": đội chưa sẵn sàng (vắng / đang bận), không được ghép");
                    }
                }
            }
        }
        final CupLogic.Ghep g = CupLogic.ghep(co, (long) t.so * 31L + vong);
        if (g.mien >= 0) {
            final CupLogic.Doi d = t.doi.get(Integer.valueOf(g.mien));
            CupLogic.ghiMien(d);
            DatabasePhoBanCup.ghiTran(t.so, vong, d.id, -1, CupLogic.A_THANG, 0);
            luuDiem(t, d);
            for (final Integer id : t.thanhVien(d.id)) {
                final Char c = PhongRiengGame.online(id.intValue());
                if (c != null) {
                    PhongRiengGame.bao(c, t.loai.ten + " vòng " + (vong + 1) + ": miễn đấu (+" + CupLogic.DIEM_MIEN + " điểm)", "");
                }
            }
        }
        for (final Integer id : g.khongGhep) {
            for (final Integer cid : t.thanhVien(id.intValue())) {
                final Char c = PhongRiengGame.online(cid.intValue());
                if (c != null) {
                    PhongRiengGame.loi(c, t.loai.ten + " vòng " + (vong + 1) + ": không có đối thủ hợp lệ vòng này");
                }
            }
        }
        for (final int[] cap : g.cap) {
            batDauTran(t, vong, cap[0], cap[1], bay);
        }
        com.htbb.chongspam.ThongBaoGop.dua("cup", t.loai.ten + " - vòng " + (vong + 1) + "/" + soVong() + ": " + g.cap.size() + " trận đang diễn ra.");
    }

    static void batDauTran(final Tuan t, final int vong, final int doiA, final int doiB, final long bay) {
        final int khu = PhongRiengGame.KHO_CUP.muon();
        if (khu < 0) {
            System.out.println("[CUP] het san trong vong " + vong);
            return;
        }
        final Tran tr = new Tran(khu, vong, doiA, doiB, bay);
        tr.a.addAll(t.thanhVien(doiA));
        tr.b.addAll(t.thanhVien(doiB));
        TRAN.put(Integer.valueOf(khu), tr);
        final MapPhongRieng m = PhongRiengGame.cup;
        final int[] oA = PhongRiengGame.diemDiDuoc(m, m.w * 16 * 3 / 10, m.h * 16 / 2);
        final int[] oB = PhongRiengGame.diemDiDuoc(m, m.w * 16 * 7 / 10, m.h * 16 / 2);
        int i = 0;
        for (final List<Integer> phe : java.util.Arrays.asList(tr.a, tr.b)) {
            final int[] o = phe == tr.a ? oA : oB;
            for (final Integer id : phe) {
                final Char c = PhongRiengGame.online(id.intValue());
                if (c == null) {
                    tr.loai.add(id);
                    continue;
                }
                tr.cu.put(id, new PhongRiengGame.ViTri(c));
                TuDongGame.khiPvp(c);
                c.hp = c.maxhp;
                c.mp = c.maxmp;
                PhongRiengGame.dua(c, m, khu, o[0], o[1] + (i++ % 2) * 24, 0);
                try {
                    c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                } catch (final Exception ignored) {
                }
                PhongRiengGame.bao(c, t.loai.ten + " vòng " + (vong + 1) + ": " + t.tenDoi(doiA) + " gặp " + t.tenDoi(doiB),
                        "Bắt đầu sau " + GIAY_CHUAN_BI + " giây, tối đa " + (CupLogic.GIAY_TRAN / 60) + " phút");
                PhongRiengGame.guiThe(c, PhongRieng.CUP, t.loai.ten + " - vòng " + (vong + 1), t.tenDoi(doiA) + " vs " + t.tenDoi(doiB),
                        "Hết giờ: bên còn nhiều % HP hơn thắng", CupLogic.GIAY_TRAN + GIAY_CHUAN_BI, -1);
            }
        }
        System.out.println("[CUP] tuan " + t.so + " vong " + (vong + 1) + " khu " + khu + ": " + t.tenDoi(doiA) + " vs " + t.tenDoi(doiB)
                + " (loai " + tr.loai + ")");
        kiemHetNguoi(tr, bay);
    }

    static void nhipTran(final Tran tr, final long bay) {
        if (tr.ketQua != 0) {
            if (bay - tr.ketThucLuc >= 4000L) {
                dongTran(tr);
            }
            return;
        }
        for (final List<Integer> phe : java.util.Arrays.asList(tr.a, tr.b)) {
            for (final Integer id : phe) {
                if (tr.loai.contains(id)) {
                    continue;
                }
                final Char c = PhongRiengGame.online(id.intValue());
                if (c == null || c.mapID != PhongRiengGame.MAP_CUP || c.region != tr.khu) {
                    tr.loai.add(id); // mat ket noi / roi san = bi loai (khong de "roi mang khi dang hon HP")
                }
            }
        }
        kiemHetNguoi(tr, bay);
        if (tr.ketQua == 0 && bay - tr.batDau >= (CupLogic.GIAY_TRAN + GIAY_CHUAN_BI) * 1000L) {
            ketThucTran(tr, CupLogic.ketQuaHetGio(tongHp(tr, tr.a), tongHp(tr, tr.b)), bay);
        }
    }

    static int tongHp(final Tran tr, final List<Integer> phe) {
        int s = 0;
        for (final Integer id : phe) {
            final Char c = PhongRiengGame.online(id.intValue());
            if (c != null && !tr.loai.contains(id)) {
                s += CupLogic.phanNghinHp(c.hp, c.maxhp);
            }
        }
        return s;
    }

    static void ketThucTran(final Tran tr, final int kq, final long bay) {
        synchronized (tr) {
            if (tr.ketQua != 0) {
                return;
            }
            tr.ketQua = kq;
            tr.ketThucLuc = bay;
        }
        final Tuan t = HIEN;
        if (t == null) {
            return;
        }
        final CupLogic.Doi a = t.doi.get(Integer.valueOf(tr.doiA));
        final CupLogic.Doi b = t.doi.get(Integer.valueOf(tr.doiB));
        if (a != null && b != null) {
            synchronized (t) {
                CupLogic.ghiTran(a, b, kq);
            }
            DatabasePhoBanCup.ghiTran(t.so, tr.vong, a.id, b.id, kq, (int) ((bay - tr.batDau) / 1000L));
            luuDiem(t, a);
            luuDiem(t, b);
        }
        final String chu = kq == CupLogic.HOA ? "Hoà" : (kq == CupLogic.A_THANG ? t.tenDoi(tr.doiA) : t.tenDoi(tr.doiB)) + " thắng";
        for (final List<Integer> phe : java.util.Arrays.asList(tr.a, tr.b)) {
            final boolean thang = kq == CupLogic.A_THANG && phe == tr.a || kq == CupLogic.B_THANG && phe == tr.b;
            for (final Integer id : phe) {
                final Char c = PhongRiengGame.online(id.intValue());
                if (c == null) {
                    continue;
                }
                if (kq == CupLogic.HOA) {
                    PhongRiengGame.bao(c, "Trận cúp: hoà (+" + CupLogic.DIEM_HOA + " điểm)", chu);
                } else if (thang) {
                    PhongRiengGame.hiem(c, "Thắng trận cúp! (+" + CupLogic.DIEM_THANG + " điểm)", chu);
                } else {
                    PhongRiengGame.loi(c, "Thua trận cúp - " + chu);
                }
                PhongRiengGame.guiThe(c, PhongRieng.CUP, t.loai.ten + " - vòng " + (tr.vong + 1), chu, "Về chỗ cũ sau vài giây", -1, -1);
            }
        }
    }

    static void luuDiem(final Tuan t, final CupLogic.Doi d) {
        DatabasePhoBanCup.ghiDiem(t.so, d.id, d.diem, d.thang, d.hoa, d.thua, d.daMien, 0);
    }

    static void dongTran(final Tran tr) {
        if (TRAN.remove(Integer.valueOf(tr.khu)) == null) {
            return;
        }
        for (final List<Integer> phe : java.util.Arrays.asList(tr.a, tr.b)) {
            for (final Integer id : phe) {
                final Char c = PhongRiengGame.online(id.intValue());
                if (c == null) {
                    continue;
                }
                PhongRiengGame.anThe(c);
                if (c.mapID == PhongRiengGame.MAP_CUP && c.region == tr.khu) {
                    c.hp = c.maxhp;
                    c.mp = c.maxmp;
                    PhongRiengGame.veCho(c, tr.cu.get(id));
                }
            }
        }
        PhongRiengGame.KHO_CUP.tra(tr.khu);
    }

    // ================================================================== chot + thuong

    static void chot(final Tuan t) {
        synchronized (t) {
            if (t.daChot) {
                return;
            }
            t.daChot = true;
        }
        final List<CupLogic.Doi> bang = CupLogic.xepHang(new ArrayList<CupLogic.Doi>(t.doi.values()));
        for (int i = 0; i < bang.size(); i++) {
            final CupLogic.Doi d = bang.get(i);
            DatabasePhoBanCup.ghiDiem(t.so, d.id, d.diem, d.thang, d.hoa, d.thua, d.daMien, i + 1);
            final CupLogic.Thuong th = CupLogic.thuong(t.loai, i + 1, d.soTran(), d.thang);
            for (final Integer id : t.thanhVien(d.id)) {
                if (th.luongKhoa > 0 || th.danhHieu > 0) {
                    DatabasePhoBanCup.ghiThuong(t.so, id.intValue(), i + 1, th.luongKhoa, th.danhHieu, th.phut);
                }
                final Char c = PhongRiengGame.online(id.intValue());
                if (c != null) {
                    nhanThuongCho(c);
                }
            }
        }
        if (!bang.isEmpty()) {
            com.htbb.chongspam.ThongBaoGop.dua("cup", "Vô địch " + t.loai.ten + " tuần " + t.tuanIso + ": " + t.tenDoi(bang.get(0).id) + "!");
        }
    }

    /** Phat moi phan thuong cup con cho cua {@code c} (dang nhap / luc chot). Moi dong chi phat MOT lan (da_nhan). */
    static void nhanThuongCho(final Char c) {
        for (final int[] r : DatabasePhoBanCup.thuongChoNhan(c.charDBID)) {
            // r = {tuan, hang, luong_khoa, danh_hieu, phut}
            if (DatabasePhoBanCup.danhDauDaNhan(r[0], c.charDBID) != 1) {
                continue; // da phat (luong khac / lan truoc) hoac CSDL loi: KHONG phat
            }
            if (r[2] > 0) {
                c.addLuongLock(r[2]);
            }
            if (r[1] >= 1 && r[1] <= 8) {
                BatPokemonGame.thuongBong(c, "ultra", r[1] == 1 ? 2 : 1, "Cúp PvP tuần"); // 0.9.8: top 8 cup tuan
            }
            if (r[3] > 0) {
                PhongRiengGame.traoDanhHieu(c, r[3], r[4]);
            }
            NhiemVuEngine.suKien(c, "cup_pvp", String.valueOf(r[1]), 1);
            if (r[1] == 1) {
                NhiemVuEngine.suKien(c, "cup_vo_dich", "", 1);
            }
            try {
                Database.instance.saveOrtherLog("", c.charname, "cup thuong tuan " + r[0] + " hang " + r[1] + " lk " + r[2] + " dh " + r[3], "laudai");
            } catch (final Exception ignored) {
            }
            PhongRiengGame.hiem(c, "Thưởng cúp tuần: hạng " + r[1], (r[2] > 0 ? "+" + r[2] + " lượng khoá" : "")
                    + (r[3] > 0 ? (r[2] > 0 ? ", " : "") + "danh hiệu mới" : ""));
        }
    }

    /** CharNapDuLieu sau khi vao game: thuong cup cho nhan khi offline. */
    static void khiDangNhap(final Char c) {
        try {
            nhanThuongCho(c);
        } catch (final Throwable e) {
            System.out.println("CupGame.khiDangNhap: " + e);
        }
    }
}
