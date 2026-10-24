package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DUONG MENU (nhanh sua-duong-gym). Nguoi choi 0.9.14 bao: muc tieu cua ai ghi "Sổ tay huấn luyện -> Gym", "NPC Đổi
 * thưởng -> ..." nhung NPC that ten "Quầy đổi thưởng" va muc that la "Thách đấu Gym" -> "Làm theo hướng dẫn rồi mà tìm
 * không thấy". Moi chu cho nguoi choi chi duong menu PHAI ghep tu cac hang o day (dung chu menu that, dung thu tu bam):
 * <ul>
 *   <li>Nhan menu dung chung: menu NPC -90 (real.MenuDoiThuong), So tay goc (real.SoTayLauDai), "Nhiệm vụ của tôi"
 *       (real.NhiemVuEngine), "Thách đấu Gym" (real.GymLauDai) doc cung cac hang nay.</li>
 *   <li>{@link #kiem}: tim moi chuoi "A → B → C" trong mot doan chu, chuoi bat dau bang nhan menu may chu thi tung buoc
 *       phai khop cay menu that ({@link Nut}, dung tu ma menu); test/KiemDuongMenu quet CSDL, ma nguon, web.</li>
 * </ul>
 * Lop thuan (khong Char) de test.
 */
public final class DuongMenu {

    public static final String MUI = " → ";

    /** Ten NPC doi thuong o the gioi (MapNapTaiNguyen: bot -90). */
    public static final String QUAY = "Quầy đổi thưởng";
    public static final String SO_TAY = "Sổ tay huấn luyện";
    public static final String NHIEM_VU = "Nhiệm vụ của tôi";
    public static final String GYM = "Thách đấu Gym";
    public static final String DIEM = "Xem đổi thưởng";
    public static final String POKEDEX = "Pokédex";
    public static final String HE_SINH_THAI = "Hệ sinh thái";
    /** Muc con cua "Nhiệm vụ của tôi". */
    public static final String DANG_LAM = "Đang làm";
    public static final String CUA_AI = "Thử thách cửa ải";

    /** Quay -> Nhiệm vụ của tôi -> (muc con): Nhiệm vụ của tôi la muc thang o quay, dau danh sach. */
    public static final String DUONG_NHIEM_VU = duong(QUAY, NHIEM_VU) + MUI;

    private DuongMenu() {
    }

    public static String duong(final String... muc) {
        final StringBuilder sb = new StringBuilder();
        for (final String m : muc) {
            if (sb.length() > 0) {
                sb.append(MUI);
            }
            sb.append(m);
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ Gym

    /** "Trưởng Gym Pewter: Golem" -> "Gym Pewter"; khong dung mau -> ten goc. */
    public static String tenGym(final String tenBoss) {
        String s = tenBoss == null ? "" : tenBoss.trim();
        if (s.startsWith("Trưởng ")) {
            s = s.substring("Trưởng ".length());
        }
        final int c = s.indexOf(':');
        return (c > 0 ? s.substring(0, c) : s).trim();
    }

    /** "Trưởng Gym Pewter: Golem" -> "Golem"; khong co ':' -> "". */
    public static String bossGym(final String tenBoss) {
        final String s = tenBoss == null ? "" : tenBoss;
        final int c = s.indexOf(':');
        return c > 0 ? s.substring(c + 1).trim() : "";
    }

    /** "Gym Pewter (Golem)". */
    public static String gymVaBoss(final String tenBoss) {
        final String b = bossGym(tenBoss);
        return tenGym(tenBoss) + (b.isEmpty() ? "" : " (" + b + ")");
    }

    /** Nhan dong trong menu "Thách đấu Gym": "Gym Pewter (Golem) - cấp 12" (+ " (đã có)"). */
    public static String nhanGym(final String tenBoss, final int cap, final boolean daCo) {
        return gymVaBoss(tenBoss) + " - cấp " + cap + (daCo ? " (đã có)" : "");
    }

    /** Muc tat dau menu quay khi dang lam buoc ha Gym: "Thách đấu Gym Pewter (Golem)". */
    public static String nhanTatGym(final String tenBoss) {
        return "Thách đấu " + gymVaBoss(tenBoss);
    }

    /** "Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Pewter". */
    public static String duongGym(final String tenBoss) {
        return duong(QUAY, SO_TAY, GYM, tenGym(tenBoss));
    }

    /** Cau ngan: Gym danh o dau / danh the nao (hop hoi + chi tiet nhiem vu). */
    public static final String NOI_DANH_GYM = "Trưởng Gym hiện ngay cạnh bạn ở bản đồ đang đứng (không dịch chuyển). "
            + "Đánh tay 1 đấu 1 trong 10 phút, Tự động đánh không dùng được.";

    /** Hop hoi thach dau ngay (popup 95). */
    public static String hoiGym(final String truoc, final String tenBoss) {
        return (truoc == null || truoc.isEmpty() ? "" : truoc + "\n") + "Thách đấu Trưởng " + gymVaBoss(tenBoss) + " ngay?\n"
                + NOI_DANH_GYM;
    }

    /** Cach toi Gym cho chi tiet nhiem vu: muc tat o quay + duong day du. */
    public static String cachToiGym(final String tenBoss) {
        return "Cách nhanh: nói chuyện với " + QUAY + ", chọn mục đầu «" + nhanTatGym(tenBoss) + "» (hoặc " + duongGym(tenBoss)
                + "). " + NOI_DANH_GYM;
    }

    // ------------------------------------------------------------------ cay menu + kiem duong

    /** Mot muc menu. {@code mo} = menu con khong mo hinh hoa (khong kiem buoc sau). */
    public static final class Nut {
        public final String nhan;
        public final List<Nut> con = new ArrayList<Nut>();
        public boolean mo;

        public Nut(final String nhan) {
            this.nhan = nhan;
        }

        public Nut them(final String n) {
            final Nut c = new Nut(n);
            con.add(c);
            return c;
        }

        public Nut them(final Nut c) {
            con.add(c);
            return c;
        }

        /** Muc con khop chu {@code chu} (bang nhan, hoac nhan = chu + " (" / " -", vd "Đang làm (2)"). */
        public Nut tim(final String chu) {
            for (final Nut c : con) {
                if (khopNhan(c.nhan, chu)) {
                    return c;
                }
            }
            return null;
        }

        /** Muc con co goc (nhan hoac phan truoc " (" / " -") la dau cua {@code chu}, dai nhat; null = khong. */
        Nut timDau(final String chu) {
            Nut tot = null;
            int dai = -1;
            for (final Nut c : con) {
                for (final String g : goc(c.nhan)) {
                    if (chu.startsWith(g) && (chu.length() == g.length() || !Character.isLetterOrDigit(chu.charAt(g.length())))
                            && g.length() > dai) {
                        tot = c;
                        dai = g.length();
                    }
                }
            }
            return tot;
        }

        void gom(final List<Nut> ds) {
            ds.add(this);
            for (final Nut c : con) {
                c.gom(ds);
            }
        }
    }

    static boolean khopNhan(final String nhan, final String chu) {
        if (chu.isEmpty()) {
            return false;
        }
        for (final String g : goc(nhan)) {
            if (g.equals(chu)) {
                return true;
            }
        }
        return false;
    }

    /** "Gym Pewter (Golem) - cấp 12" -> {nhan, "Gym Pewter (Golem)", "Gym Pewter"} (+ phan truoc ": "). */
    static List<String> goc(final String nhan) {
        final List<String> ds = new ArrayList<String>();
        ds.add(nhan);
        final int g = nhan.indexOf(" - ");
        if (g > 0) {
            ds.add(nhan.substring(0, g));
        }
        final int n = nhan.indexOf(" (");
        if (n > 0) {
            ds.add(nhan.substring(0, n));
        }
        final int c = nhan.indexOf(": ");
        if (c > 0) {
            ds.add(nhan.substring(0, c)); // "Hướng dẫn tân thủ: công dụng các NPC"
        }
        return ds;
    }

    /** Nhan SAI hay gap (khong phai menu that): chuoi bat dau bang cac chu nay la loi. */
    static final String[] NHAN_SAI = {"NPC Đổi thưởng", "NPC đổi thưởng", "Đổi thưởng", "Sổ tay"};

    /**
     * Moi loi duong menu trong {@code chu} (rong = dung). Chuoi "A → B → …": doan truoc mui dau KET THUC bang nhan mot
     * muc cua cay (vd "Nhận ở: Quầy đổi thưởng") thi la duong may chu - tung buoc sau phai la muc con that; doan cuoi chi
     * can BAT DAU bang muc con (sau do la chu thuong). Chuoi khac ("Menu → Pokédex" menu client, "Xatu → Blaziken" NPC)
     * bo qua. Doan dau ket thuc bang nhan sai ({@link #NHAN_SAI}, vd "NPC Đổi thưởng", "Sổ tay") la loi.
     */
    public static List<String> kiem(final String chu, final Nut goc) {
        final List<String> loi = new ArrayList<String>();
        if (chu == null || chu.indexOf('→') < 0) {
            return loi;
        }
        final List<Nut> moi = new ArrayList<Nut>();
        goc.gom(moi);
        int i = chu.indexOf(MUI);
        while (i >= 0) {
            final String truoc = chu.substring(batDauChuoi(chu, i), i);
            final List<String> buoc = new ArrayList<String>();
            int k = i;
            int sau;
            while (true) {
                sau = k + MUI.length();
                final int ke = chu.indexOf(MUI, sau);
                if (ke >= 0 && ketThucDoan(chu, sau) >= ke) {
                    buoc.add(chu.substring(sau, ke)); // buoc giua: dung chu giua hai mui
                    k = ke;
                } else {
                    final int xd = chu.indexOf('\n', sau);
                    buoc.add(chu.substring(sau, xd < 0 ? chu.length() : xd)); // buoc cuoi: phan con lai cua dong (khop dau)
                    break;
                }
            }
            final String l = kiemChuoi(truoc, buoc, moi);
            if (l != null) {
                loi.add(l);
            }
            i = chu.indexOf(MUI, sau);
        }
        return loi;
    }

    /** Dau doan truoc mui tai {@code i}: sau dau cau / ngoac / dau hai cham gan nhat. */
    static int batDauChuoi(final String chu, final int i) {
        int j = i - 1;
        int sau = 0; // ngoac dong da gap (doan "Sư phạm (nhận trò)" giu nguyen)
        while (j >= 0) {
            final char c = chu.charAt(j);
            if (c == ')') {
                sau++;
            } else if (c == '(' && sau > 0) {
                sau--;
                j--;
                continue;
            }
            if (c == '\n' || c == '(' || c == '«' || c == '"' || c == '\'' || c == '|' || c == '>' || c == '“' || c == '[') {
                break;
            }
            if ((c == '.' || c == ':' || c == ';' || c == ',' || c == '!' || c == '?') && (j + 1 >= chu.length() || chu.charAt(j + 1) == ' ')) {
                break;
            }
            j--;
        }
        return j + 1;
    }

    /** Het doan sau mui (chi so ky tu cuoi + 1): toi dau cau / ngoac dong / xuong dong. */
    static int ketThucDoan(final String chu, final int tu) {
        int j = tu;
        int mo = 0;
        while (j < chu.length()) {
            final char c = chu.charAt(j);
            if (c == '(') {
                mo++;
            } else if (c == ')' && mo > 0) {
                mo--;
                j++;
                continue;
            }
            if (c == '\n' || c == ')' || c == '\'' || c == '|' || c == '<' || c == ']') { // ngoac kep: nhan trich ("Thách đấu Gym")
                break;
            }
            if ((c == '.' || c == ';' || c == ',' || c == '!' || c == '?' || c == ':') && (j + 1 >= chu.length() || !Character.isLetterOrDigit(chu.charAt(j + 1)))) {
                break;
            }
            j++;
        }
        return j;
    }

    static String kiemChuoi(final String truoc, final List<String> buoc, final List<Nut> moi) {
        final String t = truoc.trim();
        // nut bat dau: nhan dai nhat ma doan truoc KET THUC bang
        Nut bd = null;
        for (final Nut n : moi) {
            if (n.nhan.isEmpty()) {
                continue;
            }
            for (final String g : goc(n.nhan)) {
                if (ketThucBang(t, g) && (bd == null || g.length() > bd.nhan.length())) {
                    bd = n;
                }
            }
        }
        if (bd == null) {
            for (final String s : NHAN_SAI) {
                if (ketThucBang(t, s)) {
                    return "nhãn không có thật «" + s + "» trong «" + doan(t, buoc) + "» (menu thật: " + QUAY + MUI + "…)";
                }
            }
            return null; // khong phai duong menu may chu
        }
        Nut n = bd;
        for (int j = 0; j < buoc.size(); j++) {
            if (n.mo) {
                return null;
            }
            final boolean cuoi = j == buoc.size() - 1;
            // bo ngoac kep / ngoac nhon quanh nhan trich: → "Thách đấu Gym" → «Gym Pewter»
            final String b = cuoi ? buoc.get(j).replaceAll("^[\\s\"“«]+", "") : buoc.get(j).trim().replaceAll("^[\"“«]+|[\"”»]+$", "");
            final Nut c = cuoi ? n.timDau(b) : n.tim(b);
            if (c == null) {
                final List<String> co = new ArrayList<String>();
                for (final Nut x : n.con) {
                    co.add(x.nhan);
                }
                return "«" + doan(t, buoc) + "»: sau «" + n.nhan + "» không có mục «" + (cuoi ? ngan(b) : b) + "» (có: " + co + ")";
            }
            n = c;
        }
        return null;
    }

    static boolean ketThucBang(final String t, final String g) {
        if (!t.endsWith(g)) {
            return false;
        }
        final int k = t.length() - g.length();
        return k == 0 || !Character.isLetterOrDigit(t.charAt(k - 1));
    }

    static String ngan(final String s) {
        return s.length() > 40 ? s.substring(0, 40) + "…" : s;
    }

    static String doan(final String t, final List<String> buoc) {
        final List<String> ds = new ArrayList<String>();
        ds.add(t.length() > 40 ? "…" + t.substring(t.length() - 40) : t);
        for (final String b : buoc) {
            ds.add(ngan(b));
        }
        return duong(ds.toArray(new String[0]));
    }

    /** Tien ich test: danh sach nhan cua cac muc con. */
    public static List<String> nhanCon(final Nut n) {
        final List<String> ds = new ArrayList<String>();
        for (final Nut c : n.con) {
            ds.add(c.nhan);
        }
        return Collections.unmodifiableList(ds);
    }
}
