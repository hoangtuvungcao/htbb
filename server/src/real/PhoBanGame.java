package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.PhoBanLogic;
import com.htbb.pokemon.PhongRieng;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;
import data.DatabasePhoBanCup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PHO BAN HAM NGUC (0.9.7) - phan GAME. Mo rong "Ham nguc bi an" (So tay huan luyen -&gt; Ham nguc bi an -&gt; "Phó bản
 * tổ đội (5 tầng)"): to doi 1-4 nguoi vao MOT khu rieng cua ban do {@link PhongRiengGame#MAP_PHO_BAN}, qua 5 tang (tang
 * 5 boss), quai theo chu de cua ngay. Luat thuan: com.htbb.pokemon.PhoBanLogic. Cau hinh: pho_ban.*.
 *
 * <ul>
 *   <li>Vao: doi truong mo; moi thanh vien mat 1 luot (pho_ban.luot_ngay / ngay, tob_pho_ban_luot).</li>
 *   <li>Trong phong: khong Tu dong danh, khong doi khu, khong hoi sinh tai cho (PhongRieng). Guc roi ve Trung tam
 *       = roi phong.</li>
 *   <li>Mat ket noi: dang nhap lai trong {@link PhoBanLogic#GIAY_MAT_KET_NOI} giay thi ve dung phong.</li>
 *   <li>Thang (ha boss tang 5): thuong ngay chung voi Ham nguc cu (0,2 gio EXP + 5 luong khoa + ruong boss tu bang
 *       roi data_noi_dung_roi nguon "pho_ban"), 1 lan / ngay. Thua / het gio: chi giu do quai roi.</li>
 * </ul>
 */
public final class PhoBanGame {

    /** Menu So tay (NPC ao SoTayLauDai.NPC_ID). */
    static final int MENU = 60, MENU_TRONG = 61;

    public static final String K_BAT = "pho_ban.bat", K_LUOT = "pho_ban.luot_ngay", K_PHUT = "pho_ban.phut_toi_da",
            K_CAP = "pho_ban.cap_toi_thieu";

    static boolean bat() {
        return com.htbb.cauhinh.CauHinhTrungTam.so(K_BAT, 1) == 1 && PhongRiengGame.phoBan != null;
    }

    static int luotNgay() {
        return (int) Math.max(1, Math.min(10, com.htbb.cauhinh.CauHinhTrungTam.so(K_LUOT, PhoBanLogic.LUOT_NGAY)));
    }

    static int phutToiDa() {
        return (int) Math.max(5, Math.min(60, com.htbb.cauhinh.CauHinhTrungTam.so(K_PHUT, PhoBanLogic.PHUT_TOI_DA)));
    }

    static int capToiThieu() {
        return (int) Math.max(1, Math.min(100, com.htbb.cauhinh.CauHinhTrungTam.so(K_CAP, PhoBanLogic.CAP_TOI_THIEU)));
    }

    // ================================================================== du lieu (CSDL, nap lai moi 60 giay)

    static volatile List<PhoBanLogic.ChuDe> CHU_DE = PhoBanLogic.chuDeMacDinh();
    static volatile List<PhoBanLogic.Tang> TANG = PhoBanLogic.tangMacDinh();
    private static volatile long napLuc;

    static void napNeuCan(final long bay) {
        if (bay - napLuc < 60000L) {
            return;
        }
        napLuc = bay;
        try {
            final List<PhoBanLogic.ChuDe> c = DatabasePhoBanCup.napChuDe();
            final List<PhoBanLogic.Tang> t = DatabasePhoBanCup.napTang();
            if (!c.isEmpty()) {
                CHU_DE = c;
            }
            if (t.size() == PhoBanLogic.SO_TANG) {
                TANG = t;
            }
        } catch (final Throwable e) {
            System.out.println("PhoBanGame.nap: " + e);
        }
    }

    static PhoBanLogic.Tang tang(final int so) {
        for (final PhoBanLogic.Tang t : TANG) {
            if (t.tang == so) {
                return t;
            }
        }
        return PhoBanLogic.tangMacDinh().get(Math.max(0, Math.min(PhoBanLogic.SO_TANG - 1, so - 1)));
    }

    /** Bang Gym {cap, hp, tc, thu} (data_noi_dung_gym qua GymLauDai.GYM) - goc chi so quai. */
    static List<int[]> bangGym() {
        final List<int[]> ds = new ArrayList<int[]>();
        for (final Object[] g : GymLauDai.GYM) {
            ds.add(new int[]{GymLauDai.so(g[3]), GymLauDai.so(g[6]), GymLauDai.so(g[7]), GymLauDai.so(g[8])});
        }
        return ds;
    }

    // ================================================================== phong

    /** Phong game = vong doi thuan (PhoBanLogic.Phong) + phan chi game moi co (quai, noi cu, toa do vao). */
    static final class Phong {
        final PhoBanLogic.Phong lg;
        final int khu;
        final PhoBanLogic.ChuDe chuDe;
        final int cap;
        final long batDau;
        final java.util.Map<Integer, PhongRiengGame.ViTri> cu = new java.util.HashMap<Integer, PhongRiengGame.ViTri>();
        final int vaoX, vaoY;
        final Random r;
        final List<Monster> quai = new ArrayList<Monster>();
        final List<Monster> tatCa = new ArrayList<Monster>();
        long theLuc;

        Phong(final int khu, final int ngay, final PhoBanLogic.ChuDe chuDe, final int cap, final long batDau, final int[] vao) {
            this.lg = new PhoBanLogic.Phong(khu, ngay, cap, batDau);
            this.khu = khu;
            this.chuDe = chuDe;
            this.cap = cap;
            this.batDau = batDau;
            this.vaoX = vao[0];
            this.vaoY = vao[1];
            this.r = new Random(batDau ^ khu);
        }

        int soNguoi() {
            return lg.tv.size();
        }
    }

    static final ConcurrentHashMap<Integer, Phong> PHONG = new ConcurrentHashMap<Integer, Phong>();

    static Phong phongCua(final int charId) {
        for (final Phong ph : PHONG.values()) {
            if (ph.lg.conTrong(charId)) {
                return ph;
            }
        }
        return null;
    }

    /** Dang o trong phong pho ban cua minh (ban do + khu). */
    static boolean trongPhong(final Char c, final Phong ph) {
        return c != null && c.mapID == PhongRiengGame.MAP_PHO_BAN && c.region == ph.khu;
    }

    // ================================================================== luot ngay

    /** Bo nho dem luot (ngay:charId) - CSDL la nguon, bo nho tranh doc lai moi lan kiem. */
    static final ConcurrentHashMap<String, Integer> LUOT = new ConcurrentHashMap<String, Integer>();

    static int luotDaDung(final int ngay, final int charId) {
        final Integer m = LUOT.get(ngay + ":" + charId);
        if (m != null) {
            return m.intValue();
        }
        final int db = DatabasePhoBanCup.luotDaDung(ngay, charId);
        LUOT.put(ngay + ":" + charId, Integer.valueOf(db));
        return db;
    }

    static void congLuot(final int ngay, final int charId) {
        LUOT.merge(ngay + ":" + charId, Integer.valueOf(1), (a, b) -> Integer.valueOf(a.intValue() + b.intValue()));
        DatabasePhoBanCup.congLuot(ngay, charId);
    }

    static int homNay() {
        return NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
    }

    // ================================================================== menu

    /** So tay -&gt; Ham nguc bi an -&gt; Pho ban to doi. */
    static void moMenu(final Char p) {
        final int ngay = homNay();
        final PhoBanLogic.ChuDe cd = PhoBanLogic.chuDe(CHU_DE, ngay);
        final Phong ph = phongCua(p.charDBID);
        final int con = Math.max(0, luotNgay() - luotDaDung(ngay, p.charDBID));
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        final boolean daThuong = h != null && h.hnNgay == ngay && h.hnDaThuong != 0;
        final String s = "Phó bản Hầm ngục hôm nay: " + (cd == null ? "?" : cd.ten) + "\n" + (cd == null ? "" : cd.moTa + "\n")
                + "5 tầng, tầng 5 là boss. Tổ đội 1-" + PhoBanLogic.NGUOI_TOI_DA + " người, cấp " + capToiThieu()
                + " trở lên, chênh nhau không quá " + PhoBanLogic.CHENH_CAP_TOI_DA + " cấp.\n"
                + "Lượt còn hôm nay: " + con + "/" + luotNgay() + " (đội trưởng mở, mỗi thành viên mất 1 lượt).\n"
                + "Giới hạn " + phutToiDa() + " phút. Không tự động đánh, không hồi sinh tại chỗ.\n"
                + "Thưởng ngày khi hạ boss (chung với Hầm ngục bí ẩn): " + (daThuong ? "đã nhận hôm nay" : "0,2 giờ EXP + 5 lượng khoá + rương boss");
        PhongRiengGame.bao(p, "Phó bản Hầm ngục", cd == null ? "" : "Hôm nay: " + cd.ten);
        p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
        if (ph != null) {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Rời phó bản"}, SoTayLauDai.NPC_ID, MENU_TRONG));
        } else {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Vào phó bản (cả tổ đội)", "Xem thưởng rương boss"}, SoTayLauDai.NPC_ID, MENU));
        }
    }

    /** SoTayLauDai.doMenu: menu {@link #MENU} / {@link #MENU_TRONG}. @return true neu da xu ly. */
    static boolean doMenu(final Char p, final int idMenu, final int idOption) {
        if (idMenu == MENU) {
            if (idOption == 0) {
                final String loi = vao(p);
                if (loi != null) {
                    PhongRiengGame.loi(p, loi);
                }
            } else if (idOption == 1) {
                p.sendMessage(MessageCreator.createServerAlertMessage(moTaRuong(p), ""));
            }
            return true;
        }
        if (idMenu == MENU_TRONG) {
            if (idOption == 0) {
                roi(p);
            }
            return true;
        }
        return false;
    }

    static String moTaRuong(final Char p) {
        final String nhom = PhoBanLogic.nhomRoi(p.lvDetail.lv);
        final StringBuilder b = new StringBuilder("Rương boss phó bản (mỗi người, 1 lần/ngày khi hạ boss tầng 5):\n");
        for (final Object[] r : NoiDungLauDai.ROI) {
            if (PhoBanLogic.NGUON_ROI.equals(r[0]) && nhom.equals(r[1])) {
                final int ppm = ((Integer) r[4]).intValue();
                final String ten = "trang_bi".equals(r[2]) ? "1 trang bị đúng dải cấp (khoá)"
                        : NoiDungLauDai.tenVatPham((String) r[2], ((Integer) r[3]).intValue());
                b.append("- ").append(ten).append(" x").append(r[5]).append(((Integer) r[6]).intValue() > ((Integer) r[5]).intValue() ? "-" + r[6] : "")
                        .append(": ").append(ppm >= 1000000 ? "chắc chắn" : (ppm / 10000) + "%").append('\n');
            }
        }
        return b.append("Mọi phần thưởng đều khoá, không giao dịch được.").toString();
    }

    // ================================================================== vao

    /** Doi cua {@code p} (chinh p o vi tri 0) - thanh vien to doi dang online. */
    static List<Char> doiCua(final Char p) {
        final List<Char> ds = new ArrayList<Char>();
        ds.add(p);
        if (p.partyID == -1) {
            return ds;
        }
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                final Char c = CharManager.instance.vChars.elementAt(i);
                if (c != null && c != p && c.isBot == -1 && c.partyID == p.partyID && c.charDBID != p.charDBID) {
                    ds.add(c);
                }
            }
        } catch (final Exception ignored) {
        }
        return ds;
    }

    static boolean laTruong(final Char p) {
        try {
            return p.partyID == -1 || p.party == null || p.party.idMaster == p.id || p.masterIDParty == p.id;
        } catch (final Exception e) {
            return true;
        }
    }

    /** @return null = da vao, nguoc lai cau bao. */
    static String vao(final Char p) {
        if (!LauDai.bat()) {
            return "Phó bản chưa mở.";
        }
        if (phongCua(p.charDBID) != null) {
            return "Bạn đang ở trong một phó bản.";
        }
        if (!laTruong(p)) {
            return "Chỉ đội trưởng mới mở được phó bản cho cả đội.";
        }
        final List<Char> doi = doiCua(p);
        final int ngay = homNay();
        final int n = doi.size();
        final int[] cap = new int[n];
        final int[] luot = new int[n];
        final boolean[] cungCho = new boolean[n];
        final boolean[] ban = new boolean[n];
        final String[] ten = new String[n];
        for (int i = 0; i < n; i++) {
            final Char c = doi.get(i);
            cap[i] = c.lvDetail.lv;
            luot[i] = luotDaDung(ngay, c.charDBID);
            cungCho[i] = c.mapID == p.mapID && c.hp > 0;
            ban[i] = PhongRiengGame.dangBan(c) || phongCua(c.charDBID) != null || CupGame.dangThiDau(c.charDBID);
            ten[i] = i == 0 ? "Bạn" : c.charname;
        }
        final String loi = PhoBanLogic.kiemVao(bat(), cap, luot, cungCho, ban, ten, capToiThieu(), luotNgay());
        if (loi != null) {
            return loi;
        }
        final PhoBanLogic.ChuDe cd = PhoBanLogic.chuDe(CHU_DE, ngay);
        if (cd == null || cd.mauQuai.length == 0) {
            return "Phó bản hôm nay chưa có dữ liệu.";
        }
        final int khu = PhongRiengGame.KHO_PHO_BAN.muon();
        if (khu < 0) {
            return "Phó bản đang kín phòng, thử lại sau ít phút.";
        }
        final MapPhongRieng m = PhongRiengGame.phoBan;
        final int[] vao = PhongRiengGame.diemDiDuoc(m, 624, 512);
        final Phong ph = new Phong(khu, ngay, cd, PhoBanLogic.capQuai(cap), com.htbb.pokemon.DongHo.bay(), vao);
        for (final Char c : doi) {
            ph.lg.tv.add(new PhoBanLogic.ThanhVien(c.charDBID, c.charname));
            ph.cu.put(Integer.valueOf(c.charDBID), new PhongRiengGame.ViTri(c));
        }
        PHONG.put(Integer.valueOf(khu), ph);
        final StringBuilder tenDoi = new StringBuilder();
        for (final Char c : doi) {
            congLuot(ngay, c.charDBID);
            TuDongGame.khiGapBoss(c); // dung Tu dong danh truoc khi vao (ban do khong cho tu dong)
            PhongRiengGame.dua(c, m, khu, vao[0] + ph.r.nextInt(33) - 16, vao[1] + ph.r.nextInt(33) - 16, 0);
            tenDoi.append(tenDoi.length() == 0 ? "" : ",").append(c.charname);
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "pho ban vao khu " + khu + " " + cd.ma + " cap " + ph.cap + " doi " + tenDoi, "laudai");
        } catch (final Exception ignored) {
        }
        batDauTang(ph, 1);
        return null;
    }

    // ================================================================== tang + quai

    /** Template tham chieu (cap gan nhat) de EXP / xu cua quai pho ban dung dai cap, khong theo template goc. */
    static MonsterTemplate thamChieu(final int cap) {
        MonsterTemplate best = null;
        for (final MonsterTemplate t : Map.monsterTemplates.values()) {
            if (t == null || t.rcvXp <= 0 || t.maxhp <= 0 || com.htbb.pokemon.GoiPokemon.laTemplateBoss(t.id, t.maxhp)) {
                continue;
            }
            if (best == null || Math.abs(t.level - cap) < Math.abs(best.level - cap)) {
                best = t;
            }
        }
        return best;
    }

    static void batDauTang(final Phong ph, final int so) {
        final MapPhongRieng m = PhongRiengGame.phoBan;
        final PhoBanLogic.Tang t = tang(so);
        final int[] goc = PhoBanLogic.chiSoGoc(bangGym(), ph.cap);
        final int[] cs = PhoBanLogic.chiSoQuai(goc, t, ph.soNguoi());
        final int[] mot = PhoBanLogic.chiSoQuai(goc, t, 1);
        final MonsterTemplate tc = thamChieu(ph.cap);
        final int n = t.laBoss ? 1 : Math.max(1, t.soQuai);
        int xp = 1;
        if (tc != null) {
            final long x = (long) Monster.rcvXpSauGiam(tc) * mot[0] / Math.max(1, tc.maxhp);
            xp = (int) Math.max(1L, Math.min((long) Monster.rcvXpSauGiam(tc) * (t.laBoss ? 40L : 4L), Math.max(Monster.rcvXpSauGiam(tc) / 2, x)));
        }
        synchronized (ph) {
            ph.quai.clear();
            for (int i = 0; i < n; i++) {
                final int idMau = t.laBoss ? ph.chuDe.mauBoss : ph.chuDe.mauQuai[i % ph.chuDe.mauQuai.length];
                final MonsterTemplate mt = Map.monsterTemplates.get(Integer.valueOf(idMau));
                if (mt == null) {
                    continue;
                }
                final double goc2 = ph.r.nextDouble() * Math.PI * 2;
                final int kc = t.laBoss ? 160 : 96 + ph.r.nextInt(200);
                final int[] o = PhongRiengGame.diemDiDuoc(m, ph.vaoX + (int) (Math.cos(goc2) * kc), ph.vaoY + (int) (Math.sin(goc2) * kc));
                try {
                    final Monster q = new MonsterPhoBan(m, mt, o[0], o[1], 0);
                    q.level = ph.cap;
                    q.id = RealController.intance.idGen.getID(1, "pho ban");
                    q.region = ph.khu;
                    q.isBoss = t.laBoss;
                    q.maxhp = cs[0];
                    q.hp = cs[0];
                    q.attack = cs[1];
                    q.defend_physic = cs[2];
                    q.maxxp = xp;
                    q.xp = xp;
                    m.addMonster(q, 0, ph.khu);
                    ph.quai.add(q);
                    ph.tatCa.add(q);
                } catch (final Exception e) {
                    System.out.println("PhoBanGame.batDauTang: " + e);
                }
            }
            ph.lg.batDauTang(so, ph.quai.size(), com.htbb.pokemon.DongHo.bay(), 3000L);
        }
        for (final PhoBanLogic.ThanhVien tv : ph.lg.tv) {
            final Char c = PhongRiengGame.online(tv.charId);
            if (c != null && trongPhong(c, ph)) {
                PhongRiengGame.bao(c, t.laBoss ? "Tầng 5: boss " + tenMau(ph.chuDe.mauBoss) + " xuất hiện!" : "Tầng " + so + "/5: hạ " + ph.lg.tong + " Pokémon",
                        ph.chuDe.ten);
            }
        }
        guiTheCaDoi(ph);
    }

    static String tenMau(final int id) {
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(id));
        return t == null ? "" : t.name;
    }

    /** HOOK Monster.onDropItem (moi quai chet). Quai cua pho ban: dem tien do tang. */
    public static void khiQuaiChet(final Monster m, final Char p) {
        if (!(m instanceof MonsterPhoBan) || m.map == null || m.map.mapId != PhongRiengGame.MAP_PHO_BAN) {
            return;
        }
        final Phong ph = PHONG.get(Integer.valueOf(m.region));
        if (ph == null) {
            return;
        }
        final boolean qua;
        synchronized (ph) {
            if (!ph.quai.remove(m)) {
                return; // da dem (actorDie goi lap)
            }
            qua = ph.lg.haQuai(com.htbb.pokemon.DongHo.bay(), 3000L);
        }
        m.bornTime = Long.MAX_VALUE;
        if (qua && !ph.lg.daHaBoss) {
            for (final PhoBanLogic.ThanhVien tv : ph.lg.tv) {
                final Char c = PhongRiengGame.online(tv.charId);
                if (c != null && trongPhong(c, ph)) {
                    PhongRiengGame.bao(c, "Qua tầng " + ph.lg.tang + "/5!", "Tầng tiếp theo sau 3 giây");
                }
            }
        }
        guiTheCaDoi(ph);
    }

    // ================================================================== nhip (moi giay)

    static void nhip(final long bay) {
        napNeuCan(bay);
        for (final Phong ph : PHONG.values()) {
            try {
                nhipPhong(ph, bay);
            } catch (final Throwable e) {
                System.out.println("PhoBanGame.nhip: " + e);
            }
        }
    }

    static void nhipPhong(final Phong ph, final long bay) {
        khongHoiSinhDe(ph);
        final List<PhoBanLogic.TinhTrang> tt = new ArrayList<PhoBanLogic.TinhTrang>();
        for (final PhoBanLogic.ThanhVien tv : ph.lg.tv) {
            final Char c = PhongRiengGame.online(tv.charId);
            tt.add(new PhoBanLogic.TinhTrang(c != null, c != null && trongPhong(c, ph), c != null && c.hp > 0));
        }
        for (final int[] sk : ph.lg.nhip(bay, tt, phutToiDa())) {
            final Char c = sk[1] > 0 ? PhongRiengGame.online(sk[1]) : null;
            switch (sk[0]) {
                case PhoBanLogic.SK_ROI:
                    if (c != null) {
                        PhongRiengGame.anThe(c); // ve Trung tam sau khi guc / qua cong / dich chuyen: roi phong
                        PhongRiengGame.bao(c, "Bạn đã rời phó bản", "Lượt hôm nay đã dùng");
                    }
                    break;
                case PhoBanLogic.SK_VAO_LAI:
                    if (c != null) {
                        PhongRiengGame.bao(c, "Đã về lại phó bản", ph.chuDe.ten + " - tầng " + ph.lg.tang + "/5");
                        guiThe(c, ph, bay);
                    }
                    break;
                case PhoBanLogic.SK_SANG_TANG:
                    batDauTang(ph, sk[1]);
                    break;
                case PhoBanLogic.SK_KET_THUC:
                    ketThuc(ph, sk[1], bay);
                    break;
                case PhoBanLogic.SK_DONG:
                    dongPhong(ph);
                    return;
                default:
                    break;
            }
        }
        if (ph.lg.ketQua == PhoBanLogic.DANG && bay - ph.theLuc >= 30000L) {
            guiTheCaDoi(ph);
        }
    }

    // ================================================================== ket thuc

    static void ketThuc(final Phong ph, final int kq, final long bay) {
        final int tangQua = ph.lg.tangDaQua();
        final StringBuilder ds = new StringBuilder();
        for (final PhoBanLogic.ThanhVien tv : ph.lg.tv) {
            ds.append(ds.length() == 0 ? "" : ",").append(tv.ten);
            final Char c = PhongRiengGame.online(tv.charId);
            if (c == null || tv.roi || !trongPhong(c, ph)) {
                continue;
            }
            if (kq == PhoBanLogic.THANG) {
                thuong(c, ph);
            } else {
                PhongRiengGame.loi(c, kq == PhoBanLogic.HET_GIO ? "Phó bản: hết " + phutToiDa() + " phút, chưa hạ được boss."
                        : "Phó bản thất bại: cả đội đã gục.");
            }
            PhongRiengGame.guiThe(c, PhongRieng.PHO_BAN, ph.chuDe.ten, kq == PhoBanLogic.THANG ? "Chiến thắng! Hạ boss tầng 5" : "Kết thúc: qua " + tangQua + "/5 tầng",
                    "Rời phó bản sau vài giây", -1, kq == PhoBanLogic.THANG ? 100 : PhoBanLogic.phanTram(ph.lg.tang, 0, 1));
        }
        DatabasePhoBanCup.ghiLichSu(ph.lg.ngay, ph.chuDe.ma, ph.cap, ph.soNguoi(), ds.toString(), tangQua, kq,
                (int) ((bay - ph.batDau) / 1000L));
    }

    /** Thuong khi ha boss: luot thuong ngay CHUNG voi Ham nguc bi an (HoSoLauDai.hnDaThuong), 1 lan / ngay. */
    static void thuong(final Char c, final Phong ph) {
        NhiemVuEngine.suKien(c, "pho_ban", ph.chuDe.ma, 1); // danh hieu / nhiem vu kieu pho_ban (tham so rong = moi chu de)
        final HoSoLauDai h = LauDaiNhanVat.lay(c);
        final int hom = homNay();
        boolean daNhan = true;
        if (h != null) {
            synchronized (h) {
                if (h.hnNgay != hom) {
                    h.hnNgay = hom;
                    h.hnTangCao = 0;
                    h.hnDaThuong = 0;
                }
                daNhan = h.hnDaThuong != 0;
                if (!daNhan) {
                    h.hnDaThuong = 1; // danh dau TRUOC, phat SAU
                }
                h.ban = true;
            }
        }
        PhongRiengGame.traoDanhHieu(c, DH_CHINH_PHUC, 0L);
        final int[] tn = PhoBanLogic.thuongNgay(PhoBanLogic.SO_TANG, daNhan);
        if (tn[0] <= 0) {
            PhongRiengGame.hiem(c, "Hạ boss phó bản!", "Hôm nay đã nhận thưởng Hầm ngục (1 lần/ngày)");
            return;
        }
        final long exp = ThongSoLauDai.expMoiGio(LauDai.kepCap(c.lvDetail.lv)) * tn[0] / 1000L;
        try {
            Map.addXPForChar(c, Math.max(1L, exp), false, "pho ban");
        } catch (final Exception ignored) {
        }
        if (tn[1] > 0) {
            c.addLuongLock(tn[1]);
        }
        final String ruong = moRuong(c, ph);
        try {
            Database.instance.saveOrtherLog("", c.charname, "pho ban thang " + ph.chuDe.ma + " exp " + exp + " lk " + tn[1] + " ruong " + ruong, "laudai");
        } catch (final Exception ignored) {
        }
        PhongRiengGame.hiem(c, "Hạ boss phó bản! +" + exp + " EXP, +" + tn[1] + " lượng khoá", ruong.isEmpty() ? "" : "Rương boss: " + ruong);
    }

    /** Danh hieu "Chinh phuc Pho ban" (lan dau ha boss tang 5, vinh vien). */
    static final int DH_CHINH_PHUC = 9810;

    /** Mo ruong boss: dong roi nguon "pho_ban" nhom cap. Moi thu deu KHOA. @return mo ta. */
    static String moRuong(final Char c, final Phong ph) {
        final String nhom = PhoBanLogic.nhomRoi(ph.cap);
        final StringBuilder b = new StringBuilder();
        for (final Object[] r : NoiDungLauDai.ROI) {
            if (!PhoBanLogic.NGUON_ROI.equals(r[0]) || !nhom.equals(r[1])) {
                continue;
            }
            if (Map.randomMillion() >= ((Integer) r[4]).intValue()) {
                continue;
            }
            final int min = ((Integer) r[5]).intValue();
            final int sl = min + Map.random(Math.max(1, ((Integer) r[6]).intValue() - min + 1));
            final String loai = (String) r[2];
            if ("trang_bi".equals(loai)) {
                final String ten = trangBiDungDai(c, ph.cap);
                if (ten != null) {
                    b.append(b.length() > 0 ? ", " : "").append(ten);
                }
                continue;
            }
            NoiDungLauDai.phat(c, loai, ((Integer) r[3]).intValue(), sl, true);
            b.append(b.length() > 0 ? ", " : "").append(NoiDungLauDai.tenVatPham(loai, ((Integer) r[3]).intValue())).append(" x").append(sl);
        }
        return b.toString();
    }

    /** Mot mon trang bi ngau nhien cua dai cap (cac dong "dai" cua bang roi), KHOA. @return ten, null neu khong co. */
    static String trangBiDungDai(final Char c, final int cap) {
        final int d = NoiDungLauDai.daiCap(cap);
        final String dai = d + "-" + (d + 4);
        final List<Integer> ds = new ArrayList<Integer>();
        for (final Object[] r : NoiDungLauDai.ROI) {
            if ("dai".equals(r[0]) && dai.equals(r[1]) && "item".equals(r[2])) {
                ds.add((Integer) r[3]);
            }
        }
        if (ds.isEmpty() || c.isFullInventory()) {
            return null;
        }
        Collections.shuffle(ds);
        for (final Integer id : ds) {
            try {
                final ItemTemplates t = Map.itemTemplates.get(5).get(id);
                if (t == null || t.type == 28) {
                    continue;
                }
                final Item it = Map.dropItemByIdTemplate(c.inCountry, id.intValue());
                if (it == null) {
                    continue;
                }
                it.id = c.getIDItem();
                it.owner = c.charDBID;
                it.lock = 1;
                c.iItems.add(it);
                c.sendMessage(MessageCreator.createCharInventoryMessage(c, 0));
                return t.name;
            } catch (final Exception e) {
                System.out.println("PhoBanGame.trangBiDungDai: " + e);
            }
        }
        return null;
    }

    /** De boss goi ra (MonsterSumon thuong) chet thi khong hoi sinh lai trong phong. */
    static void khongHoiSinhDe(final Phong ph) {
        final MapPhongRieng m = PhongRiengGame.phoBan;
        if (m == null) {
            return;
        }
        for (int phe = 0; phe < 2; phe++) {
            try {
                for (final Monster q : new ArrayList<Monster>(m.getAllMons(phe, ph.khu).values())) {
                    if (q != null && (q.isDead || q.hp <= 0)) {
                        q.bornTime = Long.MAX_VALUE;
                    }
                }
            } catch (final Exception ignored) {
            }
        }
    }

    /** Go moi quai con lai trong khu (ca de boss goi ra) truoc khi tra khu cho phong sau. */
    static void donKhu(final MapPhongRieng m, final int khu) {
        if (m == null) {
            return;
        }
        for (int phe = 0; phe < 2; phe++) {
            try {
                for (final Monster q : new ArrayList<Monster>(m.getAllMons(phe, khu).values())) {
                    q.hp = 0;
                    q.isDead = true;
                    q.bornTime = Long.MAX_VALUE;
                    m.removeMonster(q.id, phe, khu);
                }
            } catch (final Exception ignored) {
            }
        }
    }

    /** Dua moi nguoi con trong phong ve cho cu, go quai, tra khu. */
    static void dongPhong(final Phong ph) {
        if (PHONG.remove(Integer.valueOf(ph.khu)) == null) {
            return;
        }
        for (final PhoBanLogic.ThanhVien tv : ph.lg.tv) {
            final Char c = PhongRiengGame.online(tv.charId);
            if (c != null && trongPhong(c, ph)) {
                PhongRiengGame.anThe(c);
                PhongRiengGame.veCho(c, ph.cu.get(Integer.valueOf(tv.charId)));
            } else if (c != null) {
                PhongRiengGame.anThe(c);
            }
        }
        final MapPhongRieng m = PhongRiengGame.phoBan;
        synchronized (ph) {
            for (final Monster q : ph.tatCa) {
                try {
                    q.hp = 0;
                    q.isDead = true;
                    q.bornTime = Long.MAX_VALUE;
                    if (m != null) {
                        m.removeMonster(q.id, 0, ph.khu);
                    }
                    RealController.intance.idGen.putID(q.id, 1, "pho ban");
                } catch (final Exception ignored) {
                }
            }
            ph.tatCa.clear();
            ph.quai.clear();
        }
        donKhu(m, ph.khu);
        PhongRiengGame.KHO_PHO_BAN.tra(ph.khu);
    }

    /** So tay -&gt; "Rời phó bản" (khong ket thuc phong cua dong doi). */
    static void roi(final Char p) {
        final Phong ph = phongCua(p.charDBID);
        if (ph == null) {
            return;
        }
        ph.lg.roi(p.charDBID);
        PhongRiengGame.anThe(p);
        if (trongPhong(p, ph)) {
            PhongRiengGame.veCho(p, ph.cu.get(Integer.valueOf(p.charDBID)));
        }
        PhongRiengGame.bao(p, "Bạn đã rời phó bản", "Lượt hôm nay đã dùng");
    }

    // ================================================================== the -112

    static void guiThe(final Char c, final Phong ph, final long bay) {
        final int con = (int) Math.max(0L, (ph.batDau + phutToiDa() * 60000L - bay) / 1000L);
        final PhoBanLogic.Tang t = tang(ph.lg.tang);
        final String d1 = t.laBoss ? "Tầng 5/5 - Boss " + tenMau(ph.chuDe.mauBoss) : "Tầng " + ph.lg.tang + "/5 - hạ " + ph.lg.daHa + "/" + ph.lg.tong;
        int song = 0;
        for (final PhoBanLogic.ThanhVien tv : ph.lg.tv) {
            final Char x = PhongRiengGame.online(tv.charId);
            if (x != null && !tv.roi && trongPhong(x, ph) && x.hp > 0) {
                song++;
            }
        }
        PhongRiengGame.guiThe(c, PhongRieng.PHO_BAN, "Phó bản: " + ph.chuDe.ten, d1, "Đội còn " + song + "/" + ph.soNguoi()
                + " - còn " + PhongRieng.mmss(con), con, PhoBanLogic.phanTram(ph.lg.tang, ph.lg.daHa, ph.lg.tong));
    }

    static void guiTheCaDoi(final Phong ph) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        ph.theLuc = bay;
        for (final PhoBanLogic.ThanhVien tv : ph.lg.tv) {
            final Char c = PhongRiengGame.online(tv.charId);
            if (c != null && !tv.roi && trongPhong(c, ph)) {
                guiThe(c, ph, bay);
            }
        }
    }

    // ================================================================== dang nhap lai

    /** PhongRiengGame.khiNapViTri: {map, khu, x, y} neu con phong pho ban cho nhan vat nay, null neu khong. */
    static int[] viTriKhiDangNhap(final Char self) {
        final Phong ph = phongCua(self.charDBID);
        if (ph == null || !ph.lg.dangNhapLai(self.charDBID, com.htbb.pokemon.DongHo.bay())) {
            return null;
        }
        return new int[]{PhongRiengGame.MAP_PHO_BAN, ph.khu, ph.vaoX, ph.vaoY};
    }

    /** Chi test / GM: so phong dang mo. */
    static int soPhong() {
        return PHONG.size();
    }
}
