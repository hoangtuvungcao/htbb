import java.io.IOException;
import java.util.*;

/**
 * Kich ban "ho_tong" (sua-ho-tong, bao loi kazuhas4 N-3.36): nguoi choi THUONG (tai khoan moi, khong GM) cap 49 lam tron
 * buoc bang ngay "Hộ tống Gói hàng tới Kecleon":
 * <pre>
 *  So tay -> Nhiem vu cua toi -> Nhiem vu hang ngay -> nhan "Hộ tống ..." (bang rut 5 / ngay theo nhan vat: tao nhan vat
 *  moi toi khi bang co buoc ho tong)
 *  Dang lam -> chu nhiem vu co chi dan (noi nhan + noi giao)
 *  Tong tieu dau (-50) trong lang -> muc "Nhận hàng giao (hàng nhiệm vụ)" -> co xe hang
 *  di bo (tung doan, cho xe theo) toi Kecleon tap hoa cung lang -> noi chuyen -> giao
 *  Dang lam -> 1/1 (nhận thưởng) -> nhan thuong -> CSDL trang_thai 2
 * </pre>
 * GM (tai khoan kt -Dtu) chi dung de nang cap nhan vat thuong len 49 (trang thai dung san).
 */
class KichBanHoTong {

    static void dangKy() {
        BotChucNang.dk("ho_tong", true, KichBanHoTong::hoTong);
    }

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static final int CAP = Integer.getInteger("htCap", 49);
    static final String MA = System.getProperty("htMa", "N-3.36");

    static void hoTong() throws Exception {
        BotChucNang.Bot g = BotChucNang.botMoi(0);
        KichBanDS.veLang(g);
        BotChucNang.Bot b = null;
        String tenNv = BotChucNang.sql1("SELECT q.ten FROM data_quest_lau_dai q WHERE q.ma='" + MA + "' LIMIT 1");
        if (tenNv == null || tenNv.isEmpty()) tenNv = "Hộ tống";
        System.out.println("ten buoc " + MA + ": " + tenNv);
        int idx = -1;
        List<String> ds = null;
        String dung = System.getProperty("htUser");
        if (dung != null) { // nhan vat da nhan buoc (lan chay truoc): dang nhap lai, bo qua buoc rut bang
            b = new BotChucNang.Bot(dung);
            String l = b.dangNhap(HoiQuy.MAT_KHAU);
            if (l == null) l = b.vaoGame(0);
            if (l != null) throw new IllegalStateException(dung + ": " + l);
            b.bom(1500);
            ds = Collections.singletonList("(da nhan tu truoc)");
            idx = -2;
        }
        for (int lan = 0; lan < 30 && idx == -1; lan++) {
            if (b != null) b.close();
            b = HoiQuy.nguoiChoiMoi("ht", lan % 5, Integer.getInteger("htVung", lan % 2));
            KichBanDS.veLang(b);
            g.gm("slv " + b.ten + " " + CAP + " 0", 1200);
            b.lamMoi();
            ds = KichBanDS4.dsNhom(b, 2, 12);
            System.out.println("lan " + lan + " " + b.ten + " cap " + b.lv + " map " + b.mapId + " bang ngay: " + ds);
            if (ds != null) for (int i = 0; i < ds.size(); i++) if (BotChucNang.khongDau(ds.get(i)).contains(BotChucNang.khongDau(tenNv))) idx = i;
        }
        g.close();
        if (idx == -1) { ket("ho_tong.co_trong_bang", false, "30 nhan vat khong rut duoc " + MA); if (b != null) b.close(); return; }
        try {
            ket("ho_tong.co_trong_bang", true, b.ten + " cap " + b.lv + ": " + ds);
            String t = idx >= 0 ? KichBanDS4.nhan(b, idx) : "Đã nhận (lan truoc)";
            ket("ho_tong.nhan", t.contains("Đã nhận") || t.contains("Hộ tống") || t.contains("nhận"), t.replace('\n', ' '));
            if (Boolean.getBoolean("htHetLuot")) {
                // nguoi choi da dung het 3 luot van tieu trong ngay: luot_van_tieu (tob_char.pInfo[71]) = 0, dang nhap lai
                String u = b.user;
                b.close();
                Thread.sleep(4000);
                String pi = BotChucNang.sql1("SELECT pInfo FROM tob_char WHERE id=" + b.charDb);
                String[] a = pi.split(",", -1);
                a[71] = "0";
                BotChucNang.sql("UPDATE tob_char SET pInfo='" + String.join(",", a) + "' WHERE id=" + b.charDb);
                b = new BotChucNang.Bot(u);
                String l = b.dangNhap(HoiQuy.MAT_KHAU);
                if (l == null) l = b.vaoGame(0);
                ket("ho_tong.het_luot_vao_lai", l == null, "pInfo[71]=0, vao lai: " + l);
                if (l != null) return;
                b.lamMoi();
            }
            String chu = chuDangLam(b, tenNv);
            ket("ho_tong.chu_co_chi_dan", chu != null && chu.contains("Tổng tiêu đầu") && chu.contains("Kecleon"), String.valueOf(chu).replace('\n', ' '));
            // ---- Tong tieu dau
            int qg = KichBanDS4.quocGia(b);
            int[][] oNhan = {{25, 11}, {29, 20}, {20, 42}}; // VI_TRI_NHAN / 16 theo inCountry (lang 70 / 0 / 80)
            int[] o = oNhan[Math.max(0, qg)];
            java.util.regex.Matcher mo = java.util.regex.Pattern.compile("Tổng tiêu đầu ở [^(]*\\(ô (\\d+),(\\d+)\\)").matcher(String.valueOf(chu));
            if (mo.find()) { o = new int[]{Integer.parseInt(mo.group(1)), Integer.parseInt(mo.group(2))}; System.out.println("theo chi dan: Tong tieu dau o " + o[0] + "," + o[1]); }
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            int[] gan = bd == null ? null : bd.oGan(o[0], o[1] + 1, 3);
            boolean toi = gan != null && b.diDuong(bd, gan[0], gan[1]);
            System.out.println("lang " + b.mapId + " quoc " + qg + " toi Tong tieu dau " + Arrays.toString(gan) + ": " + toi + " o " + b.px / 16 + "," + b.py / 16);
            int m = b.moc();
            b.gui(23, (byte) -50);
            int n = b.doiMenu(m, -50, 0, 3000);
            ket("ho_tong.muc_nhan_hang", n > 0 && b.timMuc("Nhận hàng") == 0, n + " " + b.menuChu + " | " + b.chuTu(m));
            if (n <= 0) return;
            m = b.moc();
            b.menu(-50, 0, 0);
            String nh = b.doiChu(m, 3000, "Hàng nhiệm vụ", "không", "Không", "chưa", "Chưa");
            ket("ho_tong.co_xe_hang", nh != null && nh.contains("Hàng nhiệm vụ"), String.valueOf(nh).replace('\n', ' ') + " | " + b.chuTu(m));
            b.bom(2000);
            System.out.println("sau khi nhan: minh " + b.px + "," + b.py + " xe " + xe(b));
            // ---- di toi Kecleon (tung doan 3 o, cho xe theo)
            int[] kec = KichBanHuongDan.oNpc(b.mapLoad, 1);
            if (kec == null) { ket("ho_tong.giao", false, "khong thay Kecleon tren ban do " + b.mapId); return; }
            int[] dich = bd.oGan(kec[0], kec[1] + 1, 3);
            List<int[]> duong = duong(bd, b.px / 16, b.py / 16, dich[0], dich[1]);
            System.out.println("Kecleon " + Arrays.toString(kec) + " -> dich " + Arrays.toString(dich) + ", " + (duong == null ? "khong co duong" : duong.size() + " o"));
            if (duong == null) { ket("ho_tong.giao", false, "khong co duong toi Kecleon"); return; }
            for (int i = 0; i < duong.size(); i++) {
                b.diToi(duong.get(i)[0] * 16 + 8, duong.get(i)[1] * 16 + 8);
                b.bom(230);
                if (i % 3 == 2) { b.bom(900); System.out.println("  o " + b.px / 16 + "," + b.py / 16 + " xe " + xe(b)); }
            }
            b.bom(3000);
            String giao = null;
            for (int lan = 0; lan < 8; lan++) {
                m = b.moc();
                b.gui(23, (byte) 1);
                b.bom(1500);
                giao = b.chuTu(m);
                System.out.println("noi Kecleon lan " + lan + ": " + giao.replace('\n', ' '));
                if (!giao.contains("còn ở xa")) break;
                b.bom(3000);
            }
            List<String> dang = KichBanDS4.dsNhom(b, 0, 11);
            int iDang = -1;
            if (dang != null) for (int i = 0; i < dang.size(); i++) if (BotChucNang.khongDau(dang.get(i)).contains(BotChucNang.khongDau(tenNv))) iDang = i;
            String dong = iDang >= 0 ? dang.get(iDang) : String.valueOf(dang);
            String tt0 = BotChucNang.sql1("SELECT l.trang_thai FROM tob_nhiem_vu_lau_dai l WHERE l.id_char=" + b.charDb + " AND l.ma='" + MA + "'");
            if (Boolean.getBoolean("htHetLuot")) {
                ket("ho_tong.mien_phi_chi_thuong_nhiem_vu", String.valueOf(giao).contains("chỉ nhận thưởng nhiệm vụ") && !String.valueOf(giao).contains("exp"), String.valueOf(giao).replace('\n', ' '));
            } else {
                ket("ho_tong.luot_thuong_co_thuong_van_tieu", String.valueOf(giao).contains("exp") && !String.valueOf(giao).contains("chỉ nhận thưởng nhiệm vụ"), String.valueOf(giao).replace('\n', ' '));
            }
            ket("ho_tong.giao_tinh_buoc", dong.contains("1/1") || "1".equals(tt0) || "2".equals(tt0), "CSDL trang_thai=" + tt0 + " | Kecleon: " + String.valueOf(giao).replace('\n', ' ') + " | dang lam: " + dong);
            if (iDang >= 0 && dong.contains("1/1")) {
                m = b.moc();
                b.menu(-527, 11, iDang);
                String th = b.doiChu(m, 2500, "");
                b.bom(1500);
                String tt = BotChucNang.sql1("SELECT l.trang_thai FROM tob_nhiem_vu_lau_dai l WHERE l.id_char=" + b.charDb + " AND l.ma='" + MA + "'");
                ket("ho_tong.nhan_thuong", "2".equals(tt), "CSDL trang_thai=" + tt + " | " + String.valueOf(th).replace('\n', ' ') + " | " + b.chuTu(m).replace('\n', ' '));
            }
        } finally {
            b.close();
        }
    }

    /** Vi tri xe hang (quai mau 95..112) bot thay. */
    static String xe(BotChucNang.Bot b) {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) if (e.getValue()[5] >= 95 && e.getValue()[5] <= 112) s.append(e.getKey()).append('@').append(e.getValue()[0]).append(',').append(e.getValue()[1]).append(' ');
        return s.length() == 0 ? "(khong thay)" : s.toString();
    }

    /** Chu "Dang lam" cua buoc ten {@code ten} (chonDangLam). */
    static String chuDangLam(BotChucNang.Bot b, String ten) throws IOException {
        List<String> dang = KichBanDS4.dsNhom(b, 0, 11);
        if (dang == null) return null;
        for (int i = 0; i < dang.size(); i++) {
            if (BotChucNang.khongDau(dang.get(i)).contains(BotChucNang.khongDau(ten))) {
                int m = b.moc();
                b.menu(-527, 11, i);
                b.bom(1500);
                return b.chuTu(m);
            }
        }
        return "khong co trong Dang lam: " + dang;
    }

    /** BFS 4 huong (khong qua cong) tu (sx,sy) toi (dx,dy); null = khong co duong. */
    static List<int[]> duong(BotChucNang.BanDo bd, int sx, int sy, int dx, int dy) {
        int[] truoc = new int[bd.w * bd.h];
        Arrays.fill(truoc, -1);
        ArrayDeque<Integer> q = new ArrayDeque<>();
        int bdau = sy * bd.w + sx, dich = dy * bd.w + dx;
        if (bdau < 0 || bdau >= truoc.length || dich < 0 || dich >= truoc.length) return null;
        truoc[bdau] = bdau;
        q.add(bdau);
        int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty() && truoc[dich] < 0) {
            int c = q.poll(), cx = c % bd.w, cy = c / bd.w;
            for (int[] h : hg) {
                int nx = cx + h[0], ny = cy + h[1];
                if (nx < 0 || ny < 0 || nx >= bd.w || ny >= bd.h) continue;
                int nn = ny * bd.w + nx;
                if (truoc[nn] >= 0 || !bd.diDuoc(nx, ny) || bd.laCong(nx, ny)) continue;
                truoc[nn] = c;
                q.add(nn);
            }
        }
        if (truoc[dich] < 0) return null;
        List<int[]> d = new ArrayList<>();
        for (int c = dich; c != bdau; c = truoc[c]) d.add(new int[]{c % bd.w, c / bd.w});
        Collections.reverse(d);
        return d;
    }
}
