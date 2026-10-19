import io.Message;
import io.Session;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.CharManager;
import real.ChieuHoTro;

/**
 * "Giap vo han" (chu game 0.9.6.1): chieu giap / khien / phan don dung lai duoc khi hieu ung con chay -> giap 100%
 * thoi gian. Kiem real.ChieuHoTro qua DUNG duong lenh 51 (Char.setBuff2Char) va theo dong ho gia:
 * het thoi luong la mat hieu ung (va client duoc bao xoa bieu tuong), dung lai truoc hoi chieu bi tu choi, khong cong
 * don len muc tieu dang co, uptime qua mot gio mo phong &lt; 100%, du lieu sai (hoi chieu 4 giay) cung khong tao
 * lai giap vo han, va moi dong chieu trong SQL / CSV co hoi chieu &gt; thoi luong.
 */
public final class KiemGiapVoHan {

    private KiemGiapVoHan() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static short idKe = 27000;

    static final String[] TEP_SQL = {"../sql/htbb_day_du.sql"};
    static final String TEP_CSV = "../du_lieu/noi_dung_chieu.csv";
    static final String TEP_SUA = "../sql/sua_096_1_logic.sql";

    public static void chay() {
        Kiem.nhom("Giap vo han: chieu ho tro het han dung luc, hoi chieu > thoi luong, khong cong don (ChieuHoTro)");
        final short[][][] cdCu = chep(CharManager.SKILL_COOLDOWN);
        final boolean lauDaiCu = com.htbb.pokemon.LauDai.bat();
        try {
            final List<Map<String, String>> dump = bangChieu(doc(TEP_SQL[0]));
            com.htbb.pokemon.LauDai.dat(true);
            napHoiChieu(dump); // nhu ChieuLauDai.apDung
            duongLenh51();
            hetHanVaDungLai();
            khongCongDon();
            uptime("du lieu SQL, Tu ve cap 1", 1, 4, 1, 20);
            duLieuXau();
            com.htbb.pokemon.LauDai.dat(false);
            dat(CharManager.SKILL_COOLDOWN, cdCu);
            bangCu();
            uptime("bang cu (sv.lau_dai=0), Tu ve cap 10", 1, 4, 10, 20);
            com.htbb.pokemon.LauDai.dat(true);
            duLieu(dump);
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            dat(CharManager.SKILL_COOLDOWN, cdCu);
            com.htbb.pokemon.LauDai.dat(lauDaiCu);
        }
    }

    // ------------------------------------------------------------------ dung cu

    static Char nhanVat(final int lop, final int capO4) {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.id = idKe++;
        p.charname = "kiemgiap" + p.id;
        p.lvDetail.lv = 50;
        p.charClass = (byte) lop;
        p.maxhp = 1000;
        p.hp = 1000;
        p.maxmp = 500;
        p.mp = 500;
        p.defend_physic = 200;
        p.skill[4] = (byte) capO4;
        return p;
    }

    static Message lenh51(final int charID, final int eff) throws Exception {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(b);
        d.writeShort(charID);
        d.writeByte(0);
        d.writeByte(eff);
        d.writeShort(0);
        return new Message(51, b.toByteArray());
    }

    /** Goi 51 da gui: {kieu, charID, eff, short ke tiep} hoac null. */
    static int[] goi51(final int kieu, final int eff) {
        for (final Message m : GOI) {
            final byte[] a = m.toByteArray();
            if (a.length < 9 || a[0] != 51) {
                continue;
            }
            final int k = a[3];
            final int e = a[6];
            if (k == kieu && e == eff) {
                return new int[]{k, ((a[4] & 0xff) << 8) | (a[5] & 0xff), e, (short) (((a[7] & 0xff) << 8) | (a[8] & 0xff))};
            }
        }
        return null;
    }

    static short[][][] chep(final short[][][] a) {
        final short[][][] r = new short[a.length][][];
        for (int i = 0; i < a.length; i++) {
            r[i] = new short[a[i].length][];
            for (int j = 0; j < a[i].length; j++) {
                r[i][j] = a[i][j].clone();
            }
        }
        return r;
    }

    static void dat(final short[][][] dich, final short[][][] nguon) {
        for (int i = 0; i < dich.length && i < nguon.length; i++) {
            for (int j = 0; j < dich[i].length && j < nguon[i].length; j++) {
                System.arraycopy(nguon[i][j], 0, dich[i][j], 0, Math.min(dich[i][j].length, nguon[i][j].length));
            }
        }
    }

    static void napHoiChieu(final List<Map<String, String>> dump) {
        for (final Map<String, String> r : dump) {
            final int c = Integer.parseInt(r.get("dong"));
            final int o = Integer.parseInt(r.get("so_hieu"));
            final double h = Double.parseDouble(r.get("hoi_chieu_giay"));
            if (c < CharManager.SKILL_COOLDOWN.length && o < CharManager.SKILL_COOLDOWN[c].length && h > 0) {
                java.util.Arrays.fill(CharManager.SKILL_COOLDOWN[c][o], (short) Math.max(1, Math.round(h * 10)));
            }
        }
    }

    // ------------------------------------------------------------------ duong that

    static void duongLenh51() throws Exception {
        GOI.clear();
        final Char p = nhanVat(1, 1);
        final int mp0 = p.mp;
        final long truoc = System.currentTimeMillis();
        p.setBuff2Char(lenh51(p.id, 20));
        Kiem.bang("Tu ve (giap, Bulbasaur): hieu ung 20 len nhan vat", Integer.valueOf(20), Integer.valueOf(p.arrayBuff[1]));
        Kiem.bang("thoi luong theo thiet ke 8 giay (khong phai 50..180 giay bang cu)", Integer.valueOf(8), Integer.valueOf(p.timeOut[1]));
        Kiem.dung("giap that: phong thu cong them > 0", p.percentBuff[0] > 0);
        Kiem.bang("tru PP o that (o 4 = " + CharManager.SKILL_MP[1][4][1] + "), khong phai o danh thuong (0 PP)",
                Integer.valueOf(mp0 - CharManager.SKILL_MP[1][4][1]), Integer.valueOf(p.mp));
        final int[] g = goi51(1, 20);
        Kiem.dung("client nhan goi 51 kieu 1 (bieu tuong) voi thoi luong 8", g != null && g[3] == 8);
        Kiem.dung("moc hoi chieu da dat", p.timeLastUseSkills[4] >= truoc);
        // dung lai ngay: bi tu choi, khong mat PP, khong lam moi moc
        final long moc = p.timeLastUseSkills[4];
        final int mp1 = p.mp;
        p.setBuff2Char(lenh51(p.id, 20));
        Kiem.bang("dung lai truoc hoi chieu: khong tru PP", Integer.valueOf(mp1), Integer.valueOf(p.mp));
        Kiem.bang("dung lai truoc hoi chieu: khong lam moi moc hieu ung / hoi chieu", Long.valueOf(moc), Long.valueOf(p.timeLastUseSkills[4]));
        // het PP: tu choi, khong ghi gi
        final Char q = nhanVat(1, 1);
        q.mp = 0;
        q.setBuff2Char(lenh51(q.id, 20));
        Kiem.bang("thieu PP: khong co hieu ung", Integer.valueOf(-1), Integer.valueOf(q.arrayBuff[1]));
        Kiem.bang("thieu PP: khong vao hoi chieu", Long.valueOf(0L), Long.valueOf(q.timeLastUseSkills[4]));
    }

    static void hetHanVaDungLai() {
        GOI.clear();
        final Char p = nhanVat(1, 1);
        final long t0 = 1000000000L;
        Kiem.bang("san sang luc dau", Integer.valueOf(ChieuHoTro.OK), Integer.valueOf(ChieuHoTro.kiem(p, p, 20, t0)));
        final int tl = ChieuHoTro.ghi(p, p, 20, t0);
        p.upPercentAtribute(p, 20, 1, 1, p);
        Kiem.bang("thoi luong 8 giay", Integer.valueOf(8), Integer.valueOf(tl));
        ChieuHoTro.capNhatHetHan(p, t0 + 7999L);
        Kiem.bang("7,999 giay: con giap", Integer.valueOf(20), Integer.valueOf(p.arrayBuff[1]));
        Kiem.bang("7,999 giay: countDown theo dong ho", Integer.valueOf(7), Integer.valueOf(p.countDown[1]));
        Kiem.bang("dung lai luc dang co giap: tu choi (hoi chieu)", Integer.valueOf(ChieuHoTro.DANG_HOI),
                Integer.valueOf(ChieuHoTro.kiem(p, p, 20, t0 + 7999L)));
        GOI.clear();
        ChieuHoTro.capNhatHetHan(p, t0 + 8000L);
        Kiem.bang("8 giay: het giap", Integer.valueOf(-1), Integer.valueOf(p.arrayBuff[1]));
        Kiem.bang("het giap: phong thu cong them ve 0", Integer.valueOf(0), Integer.valueOf(p.percentBuff[0]));
        final int[] g = goi51(0, 20);
        Kiem.dung("het giap: client nhan goi 51 kieu 0 (xoa bieu tuong) co du short cuoi", g != null && g[1] == p.id);
        final long hoi = ChieuHoTro.hoiChieuMs(1, 4, 1, 8);
        Kiem.dung("hoi chieu (" + hoi + " ms) > thoi luong 8000 ms", hoi > 8000L);
        Kiem.bang("sau khi het giap nhung chua het hoi chieu: van tu choi", Integer.valueOf(ChieuHoTro.DANG_HOI),
                Integer.valueOf(ChieuHoTro.kiem(p, p, 20, t0 + 8001L)));
        Kiem.bang("het hoi chieu: dung lai duoc", Integer.valueOf(ChieuHoTro.OK), Integer.valueOf(ChieuHoTro.kiem(p, p, 20, t0 + hoi)));
    }

    static void khongCongDon() {
        final Char muc = nhanVat(1, 1);
        final Char a = nhanVat(1, 1);
        final long t0 = 2000000000L;
        ChieuHoTro.ghi(a, muc, 20, t0);
        final Char b = nhanVat(1, 1);
        Kiem.bang("nguoi thu hai buff len muc tieu dang co giap: tu choi (khong cong don, khong lam moi)",
                Integer.valueOf(ChieuHoTro.DANG_HIEU_LUC), Integer.valueOf(ChieuHoTro.kiem(b, muc, 20, t0 + 1000L)));
        Kiem.bang("muc tieu giu moc cu", Long.valueOf(t0), Long.valueOf(muc.timeUseBuff[1]));
    }

    /** Mo phong 1 gio, bam chieu moi 100 ms: ti le thoi gian co hieu ung. */
    static double mo(final int lop, final int o, final int cap, final int eff) {
        final Char p = nhanVat(lop, 0);
        p.skill[o] = (byte) cap;
        p.mp = 1000000;
        p.maxmp = 1000000;
        final long t0 = 3000000000L;
        long co = 0;
        long tong = 0;
        for (long t = t0; t < t0 + 3600000L; t += 100L) {
            ChieuHoTro.capNhatHetHan(p, t);
            if (ChieuHoTro.kiem(p, p, eff, t) == ChieuHoTro.OK) {
                ChieuHoTro.ghi(p, p, eff, t);
            }
            if (ChieuHoTro.dangCo(p, eff)) {
                co++;
            }
            tong++;
        }
        return (double) co / tong;
    }

    static void uptime(final String ten, final int lop, final int o, final int cap, final int eff) {
        final double u = mo(lop, o, cap, eff);
        Kiem.dung("uptime 1 gio (" + ten + ") = " + Math.round(u * 1000) / 10.0 + "% < 100%", u > 0 && u < 0.95);
    }

    /** Du lieu sai (hoi chieu 4 giay < thoi luong): chot server van giu hoi chieu >= thoi luong + bien. */
    static void duLieuXau() {
        final short[] cd = CharManager.SKILL_COOLDOWN[1][4];
        final short[] cu = cd.clone();
        try {
            java.util.Arrays.fill(cd, (short) 40);
            final long h = ChieuHoTro.hoiChieuMs(1, 4, 1, 8);
            Kiem.dung("du lieu hoi chieu 4 giay: hoi chieu hieu qua " + h + " ms >= 8 + 5 giay", h >= 13000L);
            uptime("du lieu xau 4 giay", 1, 4, 1, 20);
        } finally {
            System.arraycopy(cu, 0, cd, 0, cd.length);
        }
    }

    /** Bang cu (sv.lau_dai=0): moi chieu ho tro dung duoc, moi cap: hoi chieu > thoi luong. */
    static void bangCu() {
        final List<String> loi = new ArrayList<String>();
        for (int c = 0; c < CharManager.EFF_BUFF_SKILL.length; c++) {
            for (int b = 0; b < CharManager.EFF_BUFF_SKILL[c].length; b++) {
                final int eff = CharManager.EFF_BUFF_SKILL[c][b];
                if (eff == ChieuHoTro.HOI_SINH || !CharManager.isBuffActive(c, eff) || ChieuHoTro.viTri(eff) < 0) {
                    continue;
                }
                for (int lv = 1; lv <= 10; lv++) {
                    final int tl = ChieuHoTro.thoiLuongGiay(c, b + 4, lv, eff);
                    final long h = ChieuHoTro.hoiChieuMs(c, b + 4, lv, tl);
                    if (tl <= 0 || h <= tl * 1000L) {
                        loi.add(c + "/" + (b + 4) + "/cap" + lv + ": " + tl + "s vs " + h + "ms");
                    }
                }
            }
        }
        Kiem.bang("bang cu: moi chieu ho tro moi cap co hoi chieu > thoi luong", "[]", loi.toString());
    }

    // ------------------------------------------------------------------ du lieu

    static final java.util.Set<String> HO_TRO = new java.util.HashSet<String>(
            java.util.Arrays.asList("buff", "khien", "bi_dong", "hoi", "hoi_sinh"));
    static final Pattern GIAY = Pattern.compile("(?<!sau )(?<!ngủ )(\\d+) giây");

    static void duLieu(final List<Map<String, String>> dump) throws Exception {
        final List<List<Map<String, String>>> nguon = new ArrayList<List<Map<String, String>>>();
        nguon.add(dump); // mot tep SQL: CSDL (ban chup htbb_day_du.sql) la nguon duy nhat - khong con pokemon_noi_dung.sql / csv
        final String[] ten = {"htbb_day_du.sql"};
        for (int n = 0; n < nguon.size(); n++) {
            final List<String> loi = new ArrayList<String>();
            int hoTro = 0;
            for (final Map<String, String> r : nguon.get(n)) {
                final int c = Integer.parseInt(r.get("dong"));
                final int o = Integer.parseInt(r.get("so_hieu"));
                final double h = Double.parseDouble(r.get("hoi_chieu_giay"));
                final int pp = Integer.parseInt(r.get("pp"));
                final int mp = Integer.parseInt(r.get("mp_phan_tram"));
                final String kieu = r.get("kieu");
                final String id = c + "/" + o + " " + r.get("ten");
                if (h < 0.5) {
                    loi.add(id + ": hoi chieu " + h + " < 0,5 s");
                }
                if (!"thuong".equals(kieu) && (pp <= 0 || mp <= 0)) {
                    loi.add(id + ": thieu PP (" + pp + "/" + mp + ")");
                }
                if (!HO_TRO.contains(kieu)) {
                    continue;
                }
                hoTro++;
                final int tl = c < ChieuHoTro.THOI_LUONG_THIET_KE.length && o < ChieuHoTro.THOI_LUONG_THIET_KE[c].length
                        ? ChieuHoTro.THOI_LUONG_THIET_KE[c][o] : 0;
                if (!"hoi_sinh".equals(kieu) && tl <= 0) {
                    loi.add(id + ": khong co thoi luong thiet ke");
                }
                final long tlMs = tl * 1000L;
                if (h * 1000 <= tlMs + Math.max(ChieuHoTro.BIEN_TOI_THIEU_MS, tlMs / 4)) {
                    loi.add(id + ": hoi chieu " + h + " s <= thoi luong " + tl + " s + bien");
                }
                final Matcher m = GIAY.matcher(r.get("vai_tro"));
                if (m.find() && Integer.parseInt(m.group(1)) != tl) {
                    loi.add(id + ": vai_tro ghi " + m.group(1) + " giay, server " + tl);
                }
            }
            Kiem.dung(ten[n] + ": co du chieu ho tro (" + hoTro + ")", hoTro >= 12);
            Kiem.bang(ten[n] + ": moi dong chieu: hoi chieu ho tro > thoi luong + bien, >= 0,5 s, co PP", "[]", loi.toString());
        }
        // tep nang cap: moi chieu ho tro co san, san >= thoi luong + bien, khong ha gia tri seed
        final String sua = DayDu.phanNangCap("sua_096_1_logic.sql"); // tep cu gop vao sql/nang_cap.sql phan 10
        Kiem.dung("sua_096_1_logic.sql co khoi [giap-vo-han]", sua.contains("[giap-vo-han]") && sua.contains("[/giap-vo-han]"));
        final Matcher m = Pattern.compile("GREATEST\\(`hoi_chieu_giay`, (\\d+(?:\\.\\d+)?)\\).*?WHERE `dong` = (\\d+) AND `so_hieu` = (\\d+);").matcher(sua);
        final Map<String, Double> san = new HashMap<String, Double>();
        while (m.find()) {
            san.put(m.group(2) + "/" + m.group(3), Double.valueOf(m.group(1)));
        }
        final List<String> loi = new ArrayList<String>();
        for (final Map<String, String> r : dump) {
            if (!HO_TRO.contains(r.get("kieu"))) {
                continue;
            }
            final int c = Integer.parseInt(r.get("dong"));
            final int o = Integer.parseInt(r.get("so_hieu"));
            final Double s = san.get(c + "/" + o);
            final long tlMs = ChieuHoTro.THOI_LUONG_THIET_KE[c][o] * 1000L;
            if (s == null) {
                loi.add(c + "/" + o + ": thieu san");
            } else if (s.doubleValue() * 1000 <= tlMs + Math.max(ChieuHoTro.BIEN_TOI_THIEU_MS, tlMs / 4)) {
                loi.add(c + "/" + o + ": san " + s + " <= thoi luong + bien");
            } else if (s.doubleValue() > Double.parseDouble(r.get("hoi_chieu_giay"))) {
                loi.add(c + "/" + o + ": san " + s + " > seed " + r.get("hoi_chieu_giay") + " (dump chua cap nhat)");
            }
        }
        Kiem.bang("sua_096_1_logic.sql: san hoi chieu moi chieu ho tro > thoi luong + bien, khop seed", "[]", loi.toString());
    }

    static String doc(final String tep) throws Exception {
        return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
    }

    /** Dong data_noi_dung_chieu trong mot tep SQL (cot theo CREATE TABLE). */
    static List<Map<String, String>> bangChieu(final String sql) {
        final List<Map<String, String>> kq = new ArrayList<Map<String, String>>();
        final int ct = sql.indexOf("CREATE TABLE `data_noi_dung_chieu` (");
        if (ct < 0) {
            return kq;
        }
        final List<String> cot = new ArrayList<String>();
        final int het = sql.indexOf("\n)", ct); // ") ENGINE=..." (mysqldump) hoac ") DEFAULT ..." (bo sinh)
        for (final String dong : sql.substring(ct, het).split("\n")) {
            final String d = dong.trim();
            if (d.startsWith("`")) {
                cot.add(d.substring(1, d.indexOf('`', 1)));
            }
        }
        final int ins = sql.indexOf("INSERT INTO `data_noi_dung_chieu`", het);
        int i = sql.indexOf("VALUES", ins) + 6;
        List<String> bo = null;
        StringBuilder o = null;
        boolean nhay = false;
        for (; i < sql.length(); i++) {
            final char ch = sql.charAt(i);
            if (nhay) {
                if (ch == '\\') {
                    o.append(sql.charAt(++i));
                } else if (ch == '\'') {
                    nhay = false;
                } else {
                    o.append(ch);
                }
                continue;
            }
            if (ch == ';' && bo == null) {
                break;
            }
            if (ch == '(' && bo == null) {
                bo = new ArrayList<String>();
                o = new StringBuilder();
            } else if (bo != null && ch == '\'') {
                nhay = true;
            } else if (bo != null && ch == ',') {
                bo.add(o.toString().trim());
                o = new StringBuilder();
            } else if (bo != null && ch == ')') {
                bo.add(o.toString().trim());
                final Map<String, String> r = new HashMap<String, String>();
                for (int k = 0; k < cot.size() && k < bo.size(); k++) {
                    r.put(cot.get(k), bo.get(k));
                }
                kq.add(r);
                bo = null;
            } else if (bo != null) {
                o.append(ch);
            }
        }
        return kq;
    }

    static List<Map<String, String>> csv(final String tep) throws Exception {
        final List<Map<String, String>> kq = new ArrayList<Map<String, String>>();
        final List<String> dong = Files.readAllLines(Paths.get(tep), StandardCharsets.UTF_8);
        final String[] dau = dong.get(0).split(",", -1);
        for (int i = 1; i < dong.size(); i++) {
            if (dong.get(i).trim().isEmpty()) {
                continue;
            }
            final List<String> o = new ArrayList<String>();
            final StringBuilder sb = new StringBuilder();
            boolean nhay = false;
            for (final char ch : dong.get(i).toCharArray()) {
                if (ch == '"') {
                    nhay = !nhay;
                } else if (ch == ',' && !nhay) {
                    o.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(ch);
                }
            }
            o.add(sb.toString());
            final Map<String, String> r = new HashMap<String, String>();
            for (int k = 0; k < dau.length && k < o.size(); k++) {
                r.put(dau[k], o.get(k));
            }
            kq.add(r);
        }
        return kq;
    }
}
