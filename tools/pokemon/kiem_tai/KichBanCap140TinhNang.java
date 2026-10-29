import java.io.*;
import java.util.*;

/**
 * Vong TINH NANG cua nguoi choi tu nhien cap 1 -> 40 (KichBanCap140): o moi moc cap (khi tinh nang mo), dung tinh nang
 * nhu nguoi choi that bang xu tu kiem, khong GM. Moi buoc: ghi ket qua (bang tinh nang trong bao cao) + doc log server
 * ngay sau hanh dong (Exception -> loi game gan dung buoc do).
 *
 *   quetMenu   : Quay doi thuong (-90) + So tay huan luyen (-527/0): mo MOI muc goc va MOI muc con "xem" (bo qua muc tieu
 *                tien / khong hoan lai duoc: Mua, Bán, Thả, Xoá, Huỷ, Đổi, Nâng, Ghép, Rời, Giải tán...). Muc khong phan
 *                hoi gi trong 3 giay = loi.
 *   quetNpc    : moi NPC tinh trong lang (cMap): di bo toi canh, bam (goi 23) -> menu / cua so; moi muc goc cua menu.
 *   veLang     : ngoai lang -> So tay -> "Về làng" (mien phi) -> phai ve lang.
 *   veDichChuyen: mua 1 Ve dich chuyen o Kecleon (gia theo cap) roi dung o ngoai lang -> ve lang, tru 1 ve.
 *   bong / batPokemon: mua Poke Ball o Chansey (Hop PC) bang xu; gap Pokemon hoang da (-125) luc cay thi ha yeu roi nem.
 */
final class KichBanCap140TinhNang {

    private KichBanCap140TinhNang() {
    }

    /** Muc tieu tien / khong hoan lai duoc (so khop tu dau tu, chu thuong CO DAU - "nâng" khac "năng"). */
    static final String[] NGUY_HIEM = {"mua", "bán", "thả", "xoá", "xóa", "huỷ", "hủy", "đổi", "nâng", "ghép", "rời", "giải tán", "phân rã",
            "đặt cược", "cược", "nạp", "chuyển", "tặng", "gửi", "đăng ký", "tham gia", "bắt đầu", "khiêu chiến", "thách đấu", "quay",
            "mở khoá", "mở khóa", "lượng", "chế tạo", "khảm", "ấp", "bỏ", "tẩy", "học"};

    static boolean nguyHiem(String t) {
        String k = " " + t.toLowerCase(java.util.Locale.ROOT).replaceAll("[^\\p{L}\\p{N} ]", " ") + " ";
        for (String n : NGUY_HIEM) if (k.contains(" " + n + " ")) return true;
        return false;
    }

    /** Ket qua mot hanh dong menu: chu phan hoi rut gon. */
    static String phanHoi(BotChucNang.Bot b, int m) {
        b.bom(1200);
        StringBuilder s = new StringBuilder();
        for (int i = m; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.chu != null) s.append(e.chu.replace('\n', ' ')).append(" | ");
            else if (e.cmd == 23 || e.cmd == 12 || e.cmd == -121 || e.cmd == -64 || e.cmd == -111 || e.cmd == -126) s.append("[goi ").append(e.cmd).append("] ");
        }
        String t = s.toString();
        return t.length() > 220 ? t.substring(0, 220) + "…" : t;
    }

    static void sauHanhDong(KichBanCap140.NguoiChoi p, String buoc) {
        List<String> l = HoiQuy.loiLogMoi();
        if (!l.isEmpty()) KichBanCap140.loi("Exception server sau '" + buoc + "': " + String.join(" || ", l.subList(0, Math.min(3, l.size()))));
    }

    // ------------------------------------------------------------------ menu Quay / So tay
    /** Mo goc: Quay (-90/0) hoac So tay (-527/0). Tra ve moc truoc khi menu goc den, -1 neu khong mo duoc. */
    static boolean moGoc(BotChucNang.Bot b, boolean soTay) throws IOException {
        if (soTay) return KichBanDS4.moSoTay(b);
        int m = b.moc();
        b.gui(23, (byte) -90);
        return b.doiMenu(m, -90, 0, 3000) > 0;
    }

    static void quetMenu(KichBanCap140.NguoiChoi p, boolean soTay) throws IOException {
        BotChucNang.Bot b = p.b;
        String goc = soTay ? "Sổ tay" : "Quầy đổi thưởng";
        int npc = soTay ? -527 : -90;
        if (!moGoc(b, soTay)) { KichBanCap140.loi(goc + " khong mo duoc o map " + b.mapId); return; }
        List<String> muc = new ArrayList<>(b.menuChu);
        int[] cuoiGoc = b.menuCuoi.clone();
        for (int i = 0; i < muc.size(); i++) {
            String ten = muc.get(i);
            if (nguyHiem(ten) && !ten.contains("Nhiệm vụ") && !ten.contains("Sổ tay")) { p.tinhNang(goc + " → " + ten, "bo qua (hanh dong tieu tien)"); continue; }
            if (BotChucNang.khongDau(ten).contains("ve lang")) continue; // rieng: veLang
            if (!moGoc(b, soTay)) break;
            int m = b.moc();
            b.menu(cuoiGoc[0], cuoiGoc[1], i);
            String t = phanHoi(b, m);
            int mapTruoc = b.mapId;
            sauHanhDong(p, goc + " → " + ten);
            if (t.isEmpty()) { KichBanCap140.loi(goc + " → " + ten + ": khong phan hoi (cap " + b.lv + ", map " + b.mapId + ")"); p.tinhNang(goc + " → " + ten, "KHONG PHAN HOI"); continue; }
            p.tinhNang(goc + " → " + ten, t);
            if (b.mapId != mapTruoc) continue;
            // muc con (mot tang), chi muc xem
            int[] con = b.menuCuoi == null ? null : b.menuCuoi.clone();
            List<String> dsCon = new ArrayList<>(b.menuChu);
            if (con == null || (con[0] == cuoiGoc[0] && con[1] == cuoiGoc[1]) || !b.coLenh(m, -30)) continue;
            for (int j = 0; j < dsCon.size() && j < 14; j++) {
                String tc = dsCon.get(j);
                if (nguyHiem(tc)) { p.tinhNang(goc + " → " + ten + " → " + tc, "bo qua (hanh dong)"); continue; }
                if (!moGoc(b, soTay)) break;
                int m1 = b.moc();
                b.menu(cuoiGoc[0], cuoiGoc[1], i);
                b.doiMenu(m1, con[0], con[1], 2500);
                int m2 = b.moc();
                b.menu(con[0], con[1], j);
                String t2 = phanHoi(b, m2);
                sauHanhDong(p, goc + " → " + ten + " → " + tc);
                if (t2.isEmpty()) KichBanCap140.loi(goc + " → " + ten + " → " + tc + ": khong phan hoi (cap " + b.lv + ")");
                p.tinhNang(goc + " → " + ten + " → " + tc, t2.isEmpty() ? "KHONG PHAN HOI" : t2);
                if (b.mapId != mapTruoc) break;
            }
        }
        b.bom(500);
    }

    // ------------------------------------------------------------------ NPC trong lang
    static void quetNpc(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        if (!veLangDiBo(p)) return;
        Set<Integer> daBam = new HashSet<>();
        for (int[] n : KichBanHuongDan.npcCMap(b.mapLoad)) {
            if (!daBam.add(n[2])) continue;
            String l = KichBanHuongDan.toiNpc(b, n[2]);
            if (l != null) { p.tinhNang("NPC loai " + n[2] + " (lang " + b.mapId + ")", "khong toi duoc: " + l); KichBanCap140.loi("lang " + b.mapId + ": " + l); continue; }
            int m = b.moc();
            if (n[2] == 3 || n[2] == 27) b.gui(23, (byte) n[2], (byte) p.lop); // ban giap: client gui kem lop (RequestNPCInfoHandler case 3)
            else b.gui(23, (byte) n[2]);
            String t = phanHoi(b, m);
            sauHanhDong(p, "bam NPC " + n[2]);
            p.tinhNang("NPC loai " + n[2] + " (lang " + b.mapId + ")", t.isEmpty() ? "KHONG PHAN HOI" : t);
            if (b.mapId != p.b.mapId) veLangDiBo(p);
        }
    }

    static boolean veLangDiBo(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        if (KichBanDS2.laOffline(b.mapId)) KichBanDS2.raNgoai(b);
        if (KichBanDS.laLang(b.mapId)) return true;
        int tot = -1, ds = 999;
        for (int c : new int[]{0, 70, 80, 301, 1701, 1901}) { List<int[]> d = BotXuyenSuot.duong(b.mapId, c); if (d != null && d.size() < ds) { ds = d.size(); tot = c; } }
        return tot >= 0 && p.toi(tot);
    }

    // ------------------------------------------------------------------ Ve lang mien phi
    static void veLang(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        if (KichBanDS.laLang(b.mapId)) {
            if (!KichBanGymLang.raPallet(b)) { p.tinhNang("Về làng miễn phí", "khong ra duoc Pallet"); return; }
        }
        int truoc = b.mapId;
        int m = KichBanHoiQuy.soTay(b, "Về làng");
        if (m < 0) { KichBanCap140.loi("So tay khong co muc 'Về làng' (cap " + b.lv + ")"); return; }
        b.cho(x -> x.coLenh(m, 12), 5000);
        if (b.coLenh(m, 12)) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(1200); }
        b.px = b.tx * 16 + 8;
        b.py = b.ty * 16 + 8;
        sauHanhDong(p, "Về làng");
        boolean ok = KichBanDS.laLang(b.mapId);
        p.tinhNang("Về làng miễn phí (từ " + truoc + ")", (ok ? "OK -> " : "HONG -> ") + b.mapId + " | " + b.chuTu(m));
        if (!ok && b.chuTu(m).contains("Hết lượt")) { p.tinhNang("Về làng miễn phí", "het luot hom nay (dung luat): " + b.chuTu(m)); return; }
        if (!ok) KichBanCap140.loi("Ve lang mien phi tu map " + truoc + " khong ve lang: map " + b.mapId + " | " + b.chuTu(m));
    }

    // ------------------------------------------------------------------ Ve dich chuyen
    static void veDichChuyen(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        if (!veLangDiBo(p) || !KichBanDS2.vaoNha(b, 103)) { p.tinhNang("Vé dịch chuyển", "khong vao duoc tiem"); return; }
        b.lamMoi();
        long xu0 = b.xu;
        int ve0 = b.soThuoc(162);
        int m = b.moc();
        b.gui(23, (byte) 1);
        b.doiMenu(m, 1, -1, 3000);
        int i = b.timMuc("Vé dịch chuyển");
        if (i < 0) { KichBanCap140.loi("Kecleon khong co muc Vé dịch chuyển: " + b.menuChu); KichBanDS2.raNgoai(b); return; }
        int m2 = b.moc();
        b.menu(1, b.menuCuoi[1], i);
        if (b.doiMenu(m2, -591, -1, 3000) <= 0) { p.tinhNang("Vé dịch chuyển → mua", "khong co menu: " + b.chuTu(m2)); KichBanDS2.raNgoai(b); return; }
        int m3 = b.moc();
        b.menu(-591, b.menuCuoi[1], 0);
        b.bom(1500);
        b.lamMoi();
        sauHanhDong(p, "mua Vé dịch chuyển");
        p.xuTieu += Math.max(0, xu0 - b.xu);
        p.tinhNang("Vé dịch chuyển → mua 1", "ve " + ve0 + "->" + b.soThuoc(162) + " xu " + xu0 + "->" + b.xu + " | " + b.chuTu(m3));
        KichBanDS2.raNgoai(b);
        if (b.soThuoc(162) <= ve0) return;
        if (!KichBanGymLang.raPallet(b)) return;
        int ve1 = b.soThuoc(162);
        int m4 = b.moc();
        b.gui(22, (byte) 162);
        if (b.doiMenu(m4, -590, 0, 3000) <= 0) {
            // hoi chieu chung voi Ve lang mien phi (dung luat: KiemVeLangMienPhi) - nguoi choi doi het giay roi bam lai
            java.util.regex.Matcher mh = java.util.regex.Pattern.compile("chờ (\\d+) giây").matcher(b.chuTu(m4));
            if (mh.find()) {
                p.tinhNang("Vé dịch chuyển → hồi chiêu chung với Về làng", b.chuTu(m4));
                b.bom(Math.min(120, Integer.parseInt(mh.group(1)) + 2) * 1000L);
                m4 = b.moc();
                b.gui(22, (byte) 162);
            }
        }
        if (b.doiMenu(m4, -590, 0, 3000) <= 0) { KichBanCap140.loi("dung Vé dịch chuyển khong mo menu: " + b.chuTu(m4)); return; }
        int m5 = b.moc();
        b.menu(-590, 0, 0);
        b.cho(x -> x.coLenh(m5, 12), 5000);
        if (b.coLenh(m5, 12)) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(1200); }
        b.px = b.tx * 16 + 8;
        b.py = b.ty * 16 + 8;
        b.lamMoi();
        sauHanhDong(p, "dung Vé dịch chuyển");
        boolean ok = KichBanDS.laLang(b.mapId) && b.soThuoc(162) == ve1 - 1;
        p.tinhNang("Vé dịch chuyển → dùng (về làng)", (ok ? "OK " : "HONG ") + "map " + b.mapId + " ve " + ve1 + "->" + b.soThuoc(162) + " | " + b.chuTu(m5));
        if (!ok) KichBanCap140.loi("dung Ve dich chuyen: map " + b.mapId + " ve " + ve1 + "->" + b.soThuoc(162) + " | " + b.chuTu(m5));
    }

    // ------------------------------------------------------------------ Poke Ball + bat Pokemon
    static boolean muaBong(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        if (!veLangDiBo(p)) return false;
        String l = KichBanHuongDan.toiNpc(b, 0);
        if (l != null) { p.tinhNang("Chansey → Poké Ball", "khong toi Chansey: " + l); return false; }
        long xu0 = b.xu;
        int m = b.moc();
        b.gui(23, (byte) 0);
        int n = b.doiMenu(m, 0, 0, 3000);
        int i = b.timMuc("Hộp PC");
        if (n <= 0 || i < 0) { KichBanCap140.loi("Chansey khong co muc Hop PC / Poke Ball: " + b.chuTu(m)); return false; }
        int m1 = b.moc();
        b.menu(0, 0, i);
        if (b.doiMenu(m1, -581, 0, 3000) <= 0) { KichBanCap140.loi("Chansey -> Hop PC khong mo menu bong: " + b.chuTu(m1)); return false; }
        List<String> ds = new ArrayList<>(b.menuChu);
        int m2 = b.moc();
        b.menu(-581, 0, 0);
        String t = phanHoi(b, m2);
        b.lamMoi();
        sauHanhDong(p, "mua Poké Ball");
        p.xuTieu += Math.max(0, xu0 - b.xu);
        p.tinhNang("Chansey → Hộp PC → " + (ds.isEmpty() ? "?" : ds.get(0)), "xu " + xu0 + "->" + b.xu + " | " + t);
        return b.xu < xu0;
    }

    /** Pokemon hoang da gan day (-125): ha yeu (<35% HP) roi nem bong. true = bat duoc. */
    static boolean batPokemon(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        if (b.hoangDa.isEmpty()) return false;
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        for (int id : new ArrayList<>(b.hoangDa.keySet())) {
            b.gui(7, (short) id);
            b.bom(300);
            int[] q = b.quai.get(id);
            if (q == null || q[2] <= 0 || bd == null) continue;
            int[] o = bd.oGan(q[0] / 16, q[1] / 16, 2);
            KichBanHuongDan.dongBoViTri(b);
            if (o == null || !b.diDuong(bd, o[0], o[1])) continue;
            for (int lan = 0; lan < 60; lan++) {
                b.gui(7, (short) id);
                b.bom(200);
                q = b.quai.get(id);
                if (q == null || q[2] == 0) break;
                if (q[2] > 0 && q[4] > 0 && q[2] * 100 <= q[4] * 35) break;
                b.gui(9, (short) id, (byte) 0);
                b.bom(450);
            }
            q = b.quai.get(id);
            if (q == null || q[2] <= 0) continue;
            StringBuilder ghi = new StringBuilder();
            for (int nem = 0; nem < 5; nem++) {
                int m = b.moc();
                b.gui(-121, (byte) 1, (short) id, (byte) 1);
                b.cho(x -> KichBanThuPhuc.goi121(x, m, 1) != null || x.doiChu(m, 0, "") != null, 2500);
                byte[] d = KichBanThuPhuc.goi121(b, m, 1);
                if (d == null) { ghi.append("[").append(b.chuTu(m)).append("]"); break; }
                DataInputStream r = KichBanThuPhuc.doc(d);
                r.readShort(); r.readShort(); r.readByte();
                int kq = r.readByte();
                r.readByte();
                int dex = r.readShort();
                r.readByte();
                r.readShort();
                String chu = r.readUTF();
                ghi.append("[dex ").append(dex).append(": ").append(chu).append("] ");
                if (kq == 1) {
                    sauHanhDong(p, "bat Pokemon");
                    p.tinhNang("Bắt Pokémon hoang dã (cấp " + b.lv + ", map " + b.mapId + ")", "BAT DUOC " + ghi);
                    return true;
                }
                if (chu.contains("bỏ chạy") || chu.contains("hết")) break;
                b.bom(1600);
            }
            p.tinhNang("Bắt Pokémon hoang dã (cấp " + b.lv + ", map " + b.mapId + ")", "khong bat duoc: " + ghi);
        }
        return false;
    }


    // ------------------------------------------------------------------ Danh hieu
    static void danhHieu(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        int m = b.moc();
        b.gui(KichBanDanhHieu.KHACH, (byte) 0);
        b.bom(1500);
        List<KichBanDanhHieu.Dong> ds = KichBanDanhHieu.bang(b, m);
        if (ds == null) { KichBanCap140.loi("Danh hieu: xin bang (-111 0) khong nhan -110: " + b.chuTu(m)); return; }
        KichBanDanhHieu.Dong co = null;
        int soCo = 0;
        for (KichBanDanhHieu.Dong d : ds) if (d.trangThai >= 1) { soCo++; if (co == null || d.trangThai == 1) co = d; }
        p.tinhNang("Danh hiệu → bảng", ds.size() + " danh hieu, da co " + soCo);
        if (co == null) return;
        int m2 = b.moc();
        b.gui(KichBanDanhHieu.KHACH, (byte) 1, (short) co.id);
        b.bom(1500);
        int[] n = KichBanDanhHieu.nhan(b, m2, b.actor);
        sauHanhDong(p, "đeo danh hiệu");
        boolean ok = n != null && n[0] == co.id;
        p.tinhNang("Danh hiệu → đeo '" + co.ten + "'", ok ? "OK" : "HONG " + KichBanDanhHieu.toast(b, m2));
        if (!ok) KichBanCap140.loi("deo danh hieu '" + co.ten + "' (" + co.id + ") khong nhan -110 cho minh: " + KichBanDanhHieu.toast(b, m2));
    }

    // ------------------------------------------------------------------ Doi khu
    static void doiKhu(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        if (KichBanDS.laLang(b.mapId) && !KichBanGymLang.raPallet(b)) return;
        int m = b.moc();
        b.chat("khu");
        int n = b.doiMenu(m, -34, 0, 2500);
        if (n <= 0) { p.tinhNang("Đổi khu (gõ 'khu') map " + b.mapId, "khong co menu khu: " + b.chuTu(m)); return; }
        int truoc = b.mapId;
        int m2 = b.moc();
        b.menu(-34, 0, n > 1 ? 1 : 0);
        String t = phanHoi(b, m2);
        if (b.coLenh(m2, 12)) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(800); b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8; }
        sauHanhDong(p, "đổi khu");
        p.tinhNang("Đổi khu → Khu 2 (map " + truoc + ", " + n + " khu)", t.isEmpty() ? "KHONG PHAN HOI" : t);
        if (t.isEmpty()) KichBanCap140.loi("chon Khu 2 (map " + truoc + ") khong phan hoi gi (khong doi khu, khong bao ly do)");
    }

    // ------------------------------------------------------------------ hai nguoi choi gap nhau: giao dich
    static final java.util.concurrent.CyclicBarrier GAP = new java.util.concurrent.CyclicBarrier(2);
    static volatile KichBanCap140.NguoiChoi[] CAP_GAP = new KichBanCap140.NguoiChoi[2];

    static boolean cho(String buoc) {
        try {
            GAP.await(4, java.util.concurrent.TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            GAP.reset();
            return false;
        }
    }

    static void giaoDich(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        boolean laA = p.kieu.equals("lam");
        CAP_GAP[laA ? 0 : 1] = p;
        if (!cho("hen")) { p.daGiaoDich = false; p.tinhNang("Giao dịch 2 người", "nguoi kia khong toi diem hen trong 4 phut (thu lai o moc sau)"); return; }
        KichBanCap140.NguoiChoi o = CAP_GAP[laA ? 1 : 0];
        veLangDiBo(p);
        if (b.mapId != 0) { // ve dung lang goc 0 (ban sao 301 la khu khac)
            p.toi(0);
        }
        KichBanGymLang.raPallet(b);
        cho("ra pallet");
        if (laA && o.b != null && b.mapId == o.b.mapId) {
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            KichBanHuongDan.dongBoViTri(b);
            int[] dich = bd == null ? null : bd.oGan(o.b.px / 16 + 1, o.b.py / 16, 3);
            if (dich != null) b.diDuong(bd, dich[0], dich[1]);
        }
        b.bom(1500);
        cho("gan");
        boolean cungMap = o.b != null && b.mapId == o.b.mapId;
        if (laA) {
            int a0 = b.soThuoc(1) + b.soThuoc(2);
            int m = b.moc();
            if (cungMap) b.gui(66, (byte) 0, (short) o.b.actor);
            b.bom(2000);
            p.tinhNang("Giao dịch → mời " + o.user + " (cùng map " + cungMap + ", map " + b.mapId + "/" + (o.b == null ? -1 : o.b.mapId) + ")", phanHoi(b, m));
            sauHanhDong(p, "moi giao dich");
        }
        cho("moi");
        if (!laA && cungMap) {
            int m = b.moc();
            b.gui(66, (byte) 1, (short) o.b.actor);
            b.bom(1500);
            p.tinhNang("Giao dịch → nhận lời mời của " + o.user, phanHoi(b, m));
        }
        cho("nhan");
        if (laA && cungMap) {
            int m = b.moc();
            int loai = b.soThuoc(2) > 0 ? 2 : 1;
            int t0 = b.soThuoc(loai), t1 = o.b.soThuoc(loai);
            b.gui(66, (byte) 2, (byte) 1, (byte) loai, (short) 1);
            b.bom(600);
            b.gui(66, (byte) 4, (short) 0);
            o.b.gui(66, (byte) 4, (short) 0);
            b.bom(800);
            b.gui(66, (byte) 5);
            o.b.gui(66, (byte) 5);
            b.bom(1500);
            b.lamMoi();
            o.b.lamMoi();
            String kq = "thuoc " + loai + ": A " + t0 + "->" + b.soThuoc(loai) + " B " + t1 + "->" + o.b.soThuoc(loai) + " | " + b.chuTu(m);
            p.tinhNang("Giao dịch → 1 bình thuốc " + loai, kq);
            if (b.soThuoc(loai) + o.b.soThuoc(loai) != t0 + t1) KichBanCap140.loi("giao dich thuoc khong bao toan: " + kq);
            sauHanhDong(p, "giao dich");
        }
        cho("xong");
    }

    // ------------------------------------------------------------------ vong tinh nang o moc cap
    static void chay(KichBanCap140.NguoiChoi p) throws Exception {
        BotChucNang.Bot b = p.b;
        p.ghi("== vong tinh nang o cap " + b.lv);
        quetMenu(p, false);
        quetMenu(p, true);
        quetNpc(p);
        veLang(p);
        if (b.xu > 4000) veDichChuyen(p);
        if (!p.coBong && b.xu > 3000) p.coBong = muaBong(p);
        danhHieu(p);
        doiKhu(p);
        if (b.lv >= 20 && !p.daGiaoDich && KichBanCap140.SO_NGUOI_CHOI == 2) { p.daGiaoDich = true; giaoDich(p); }
        p.thoatGiuNguyen();
        p.vao(); // luu + vao lai sau vong tinh nang
    }
}
