import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * 100 bot "nguoi choi that" theo persona (kiem thu co che dai): moi bot mot tai khoan da kich hoat
 * (5h_active), KHONG GM, choi theo hon hop hanh vi co trong so. Bot dieu phoi GM tua dong ho ao
 * (com.htbb.pokemon.DongHo, chi may thu) va kich hoat su kien: boss the gioi (tua toi thu Bay 19:59),
 * chiem Gym/thanh (GM "setting" -> 1), gio bung phat he sinh thai, reset ngay/tuan.
 *
 * Persona: CAY (grinder), CHUYEN (story), SUU_TAM (collector), BUON (trader/crafter + he sinh thai),
 * PVP, XA_HOI (to doi/bang/chat/su pham), THUONG (casual), MOI (newbie), NAP (payer-sim: chi do khoa/luong khoa).
 *
 *   java -cp /tmp/bot -Ddb=htbb_db_xs -Dnoi=. -Dlog=<log server> -Dphut=60 -Dso=100 -Dra=bao_cao.md \
 *        BotNguoiChoi 127.0.0.1 19179 pb dieuphoi
 * Bao cao: docs/kien-truc.md (sinh tu -Dra + phan tich CSDL o cuoi).
 */
public class BotNguoiChoi {

    enum Persona { CAY, CHUYEN, SUU_TAM, BUON, PVP, XA_HOI, THUONG, MOI, NAP }

    static final int[] TRONG_SO = {18, 18, 10, 12, 10, 10, 10, 7, 5};
    static final AtomicInteger soChat = new AtomicInteger(), soGiaoDich = new AtomicInteger(), soCauCa = new AtomicInteger(),
            soDao = new AtomicInteger(), soNau = new AtomicInteger(), soPvpThang = new AtomicInteger(), soMuaLk = new AtomicInteger(),
            soToDoi = new AtomicInteger(), soDangNhapLai = new AtomicInteger(), soBossTheGioi = new AtomicInteger();
    static final ConcurrentHashMap<String, AtomicInteger> SU_KIEN = new ConcurrentHashMap<>();
    static final List<String> LOI_BOT = Collections.synchronizedList(new ArrayList<>());
    static final List<NguoiChoi> TAT_CA = Collections.synchronizedList(new ArrayList<>());

    static void dem(String k) {
        SU_KIEN.computeIfAbsent(k, z -> new AtomicInteger()).incrementAndGet();
    }

    static class NguoiChoi extends BotXuyenSuot.Dong {
        final Persona ps;
        final Random rd;
        final List<int[]> moc = new ArrayList<>(); // {phut tu luc bat dau, lv, xu/1000, lk, luong}
        long xuDau = -1;
        int lkDau = -1, luongDau = -1;
        String loiCuoi = "";

        NguoiChoi(Persona ps, int lop, String user, long hetGio) {
            super(lop, user, hetGio);
            this.ps = ps;
            this.rd = new Random(user.hashCode());
        }

        void chup() {
            if (xuDau < 0) { xuDau = b.xu; lkDau = b.lk; luongDau = b.luong; }
            moc.add(new int[]{(int) ((System.currentTimeMillis() - batDau) / 60000), b.lv, (int) (b.xu / 1000), b.lk, b.luong});
        }

        @Override
        void vongChinh() throws Exception {
            long lanChup = 0;
            while (System.currentTimeMillis() < hetGio) {
                if (System.currentTimeMillis() - lanChup > 5 * 60000L) { b.lamMoi(); chup(); lanChup = System.currentTimeMillis(); }
                BotXuyenSuot.quetLog();
                kiem();
                muaThuoc();
                switch (ps) {
                    case CAY: luyen(4); if (rd.nextInt(5) == 0) gym(); break;
                    case CHUYEN: nvChinh(); if (!lamNvChinh()) luyen(2); if (rd.nextInt(3) == 0) nvPhu(); if (rd.nextInt(3) == 0) gym(); break;
                    case MOI: nvChinh(); if (!lamNvChinh()) luyen(2); break;
                    case SUU_TAM: suuTam(); break;
                    case BUON: sinhThai(); if (rd.nextInt(3) == 0) luyen(2); break;
                    case PVP: pvp(); break;
                    case XA_HOI: xaHoi(); break;
                    case THUONG: thuong(); break;
                    case NAP: nap(); luyen(3); break;
                    default: luyen(3);
                }
            }
        }

        // -------------------------------------------------------- suu tam: Pokedex, nhieu loai quai
        void suuTam() throws Exception {
            List<Integer> maps = new ArrayList<>(BotXuyenSuot.MAP_THU);
            Collections.shuffle(maps, rd);
            for (int mid : maps) {
                Integer l = BotXuyenSuot.LOAD.get(mid);
                if (l == null || KichBanDS2.laOffline(mid)) continue;
                boolean hop = false;
                for (int t : BotXuyenSuot.luoiQuai(l)) { Integer c = BotXuyenSuot.CAP_QUAI.get(t); if (c != null && c <= b.lv + 2) hop = true; }
                if (!hop || BotXuyenSuot.duong(b.mapId, mid) == null) continue;
                if (!toi(mid)) return;
                danh(-1, 6, 3 * 60000L);
                break;
            }
            if (rd.nextInt(3) == 0) { b.menu(-90, 0, 1); b.bom(800); b.menu(-526, 0, rd.nextInt(2)); b.bom(800); dem("pokedex_menu"); }
        }

        // -------------------------------------------------------- he sinh thai: cau ca / dao / nau / cho
        void sinhThai() throws Exception {
            int vt = -1;
            b.gui(23, (byte) -90);
            b.bom(800);
            vt = b.timMuc("sinh thái");
            if (vt < 0) { dem("sinh_thai_tat"); luyen(2); return; }
            int chon = rd.nextInt(4);
            if (chon == 0) {
                // cau ca: thu tai cho -> neu bao diem cau gan nhat thi di toi
                for (int lan = 0; lan < 2; lan++) {
                    // 0.9.6.1: menu he sinh thai khong con muc cau ca - lenh "cauca" nhu nut ngu canh canh ho
                    int m = b.moc();
                    b.chat("cauca");
                    String t = b.doiChu(m, 2500, "");
                    if (t == null) break;
                    java.util.regex.Matcher mm = java.util.regex.Pattern.compile("\\((\\d+),(\\d+)\\)").matcher(t);
                    if (t.contains("Điểm câu") && mm.find()) {
                        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                        int[] o = bd == null ? null : bd.oGan(Integer.parseInt(mm.group(1)), Integer.parseInt(mm.group(2)), 3);
                        if (o != null) b.diDuong(bd, o[0], o[1]);
                        continue;
                    }
                    if (t.contains("hồ") && t.contains("Chỉ")) { // map khong co ho: sang map khac
                        int ml = mapLuyen();
                        if (ml >= 0) toi(ml);
                        break;
                    }
                    soCauCa.incrementAndGet();
                    dem("cau_ca");
                    break;
                }
            } else if (chon == 1) {
                b.menu(-90, 0, vt);
                b.bom(700);
                int m = b.moc();
                b.menu(-528, 0, 1);   // Khảo cổ
                String t = b.doiChu(m, 2500, "");
                if (t != null && !t.contains("Chỉ đào")) { soDao.incrementAndGet(); dem("dao"); }
                else { int ml = mapLuyen(); if (ml >= 0) toi(ml); }
            } else if (chon == 2) {
                b.menu(-90, 0, vt);
                b.bom(700);
                int m = b.moc();
                b.menu(-528, 0, 2);   // Nấu ăn, chế thuốc, chế tác
                if (b.doiMenu(m, -528, 1, 2000) > 0) {
                    int m2 = b.moc();
                    b.menu(-528, 1, 0);
                    String t = b.doiChu(m2, 2000, "");
                    if (t != null && !t.contains("thiếu") && !t.contains("Thiếu") && !t.contains("chưa")) { soNau.incrementAndGet(); dem("nau"); }
                    else dem("nau_thieu_nl");
                }
            } else {
                b.menu(-90, 0, vt);
                b.bom(700);
                b.menu(-528, 0, 5);   // Chợ sinh thái
                b.bom(1000);
                dem("cho_sinh_thai_xem");
            }
        }

        // -------------------------------------------------------- PvP: bat PK, tim nguoi PvP khac cung map
        void pvp() throws Exception {
            if (b.lv < 12) { luyen(3); return; }
            b.gui(67, (byte) 1);
            b.bom(500);
            NguoiChoi doi = null;
            synchronized (TAT_CA) {
                for (NguoiChoi n : TAT_CA) if (n != this && n.ps == Persona.PVP && n.b != null && !n.b.dut && (n.b.lv - 1) / 10 == (b.lv - 1) / 10) doi = n;
            }
            if (doi == null || doi.b.mapId < 0 || KichBanDS.laLang(doi.b.mapId)) { luyen(2); return; }
            if (!toi(doi.b.mapId)) return;
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            for (int lan = 0; lan < 60 && !b.dut; lan++) {
                int[] vt = b.nguoi.get(doi.b.actor);
                if (vt != null && bd != null && lan % 10 == 0) { int[] o = bd.oGan(vt[0] / 16, vt[1] / 16, 2); if (o != null) b.diDuong(bd, o[0], o[1]); }
                b.gui(6, (short) doi.b.actor, (byte) 0);
                b.bom(400);
            }
            dem("pvp_tran");
        }

        // -------------------------------------------------------- xa hoi: chat (gioi han), to doi, su pham
        static final String[] CAU = {"chào mọi người", "có ai đi Gym không", "map này nhiều Caterpie quá", "ai bán thuốc không",
            "tối nay chiếm Gym nhé", "Pokédex mình được %d loài rồi", "cần người lập đội", "hôm nay câu được cá to",
            "ai biết đường đến Viridian không", "cảm ơn bạn nhé"};

        void xaHoi() throws Exception {
            if (rd.nextInt(2) == 0) {
                String c = String.format(CAU[rd.nextInt(CAU.length)], rd.nextInt(150)) + (rd.nextInt(3) == 0 ? " " + rd.nextInt(99) : "");
                b.chat(c); // <= 1 cau / vong (vai phut): duoi gioi han 5 / 10 giay, khong lap nguyen van
                soChat.incrementAndGet();
            }
            NguoiChoi ban = null;
            synchronized (TAT_CA) {
                for (NguoiChoi n : TAT_CA) if (n != this && n.ps == Persona.XA_HOI && n.b != null && !n.b.dut && n.b.mapId == b.mapId) ban = n;
            }
            if (ban != null && rd.nextInt(2) == 0) {
                int m = ban.b.moc();
                b.gui(48, b.actor);
                b.bom(400);
                b.gui(49, (byte) 0, (short) ban.b.actor);
                if (ban.b.choLenh(m, 49, 2000)) { ban.b.gui(49, (byte) 1, (short) b.actor); soToDoi.incrementAndGet(); dem("to_doi"); }
            }
            luyen(3);
            if (b.lv >= 30 && rd.nextInt(4) == 0) { KichBanDS4.chonSoTay(b, 4); dem("su_pham_thu"); }
        }

        // -------------------------------------------------------- casual: phien ngan
        void thuong() throws Exception {
            KichBanDS4.chonSoTay(b, 5); // lich dang nhap
            KichBanDS4.chonSoTay(b, 6);
            for (int i = 0; i < 4; i++) { b.menu(-527, 4, i); b.bom(400); }
            nvPhu();
            luyen(rd.nextInt(3) + 1);
            // thoat roi vao lai (phien ngan)
            soDangNhapLai.incrementAndGet();
            vaoLai();
        }

        // -------------------------------------------------------- payer-sim: shop luong khoa (ve EXP, keo, thuoc)
        void nap() throws Exception {
            if (rd.nextInt(3) != 0) return;
            int[] mon = {0, 1, 2, 6, 7};
            int id = mon[rd.nextInt(mon.length)];
            int lk0 = b.lk;
            int m = b.moc();
            b.gui(85, (byte) id);
            b.bom(1200);
            b.lamMoi();
            if (b.lk < lk0) { soMuaLk.incrementAndGet(); dem("mua_lk_" + id); }
            else dem("mua_lk_tu_choi");
        }
    }

    // ======================================================================== dieu phoi su kien
    static void dieuPhoiSuKien(long hetGio) {
        long batDau = System.currentTimeMillis();
        int buoc = 0;
        while (System.currentTimeMillis() < hetGio) {
            try {
                Thread.sleep(60000);
                buoc++;
                long phut = (System.currentTimeMillis() - batDau) / 60000;
                // moi 6 phut that = 1 ngay ao (reset ngay/tuan, lich dang nhap, nhiem vu ngay, lich su kien)
                if (buoc % 6 == 0) { BotXuyenSuot.tua("1d", "nhip ngay persona"); dem("tua_ngay"); }
                // boss the gioi: dua dong ho toi 19:59 thu Bay ao (LauDaiJob: thu Bay 20:00)
                if (buoc % 25 == 12) {
                    Calendar c = Calendar.getInstance();
                    long lechHt = 0;
                    // khong biet lech hien tai chinh xac: tua theo gio toi thu Bay 19:59 cua gio that + so ngay da tua
                    c.setTimeInMillis(System.currentTimeMillis() + BotXuyenSuot.tongTuaPhut * 60000L);
                    int ngayToi = (Calendar.SATURDAY - c.get(Calendar.DAY_OF_WEEK) + 7) % 7;
                    long denPhut = ngayToi * 1440L + (19 * 60 + 59) - (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE));
                    if (denPhut < 0) denPhut += 7 * 1440;
                    if (denPhut > 0) { BotXuyenSuot.tua((denPhut / 60) + "h", "boss the gioi (toi T7 ~20:00)"); dem("tua_boss_the_gioi"); }
                }
                // chiem Gym/thanh: GM setting -> 1 (bat dau sau 2 phut)
                if (buoc % 30 == 20 && BotXuyenSuot.dieuPhoi != null) {
                    synchronized (BotXuyenSuot.KHOA_DH) {
                        BotChucNang.Bot d = BotXuyenSuot.dieuPhoi;
                        int m = d.moc();
                        d.chat("setting");
                        d.bom(1000);
                        BotChucNang.SuKien hoi = d.layLenh(m, -31);
                        if (hoi != null) { d.gui(-31, (short) hoi.so[0], (byte) hoi.so[1], "1"); d.bom(1000); dem("chiem_gym_hen"); }
                    }
                }
            } catch (Exception e) {
                LOI_BOT.add("dieu phoi: " + e);
            }
        }
    }

    // ======================================================================== main
    public static void main(String[] a) throws Exception {
        BotChucNang.HOST = a[0];
        BotChucNang.PORT = Integer.parseInt(a[1]);
        BotChucNang.TIEN_TO = a[2];
        String tkDp = a.length > 3 ? a[3] : "dieuphoi";
        int so = Integer.getInteger("so", 100);
        long hetGio = System.currentTimeMillis() + Long.getLong("phut", 60L) * 60000L;
        BotXuyenSuot.quetLog();
        BotChucNang.Bot dp = new BotChucNang.Bot(tkDp);
        String l = dp.dangNhap("123456");
        if (l == null) l = dp.vaoGame(0);
        if (l != null) throw new IllegalStateException("dieu phoi: " + l);
        KichBanDS.veLang(dp);
        BotXuyenSuot.dieuPhoi = dp;
        BotXuyenSuot.napTheGioi(dp);
        Persona[] ps = Persona.values();
        int tong = 0;
        for (int w : TRONG_SO) tong += w;
        ExecutorService ex = Executors.newFixedThreadPool(so + 1);
        Random rd = new Random(42);
        for (int i = 0; i < so; i++) {
            int r = rd.nextInt(tong), k = 0;
            while (r >= TRONG_SO[k]) { r -= TRONG_SO[k]; k++; }
            NguoiChoi n = new NguoiChoi(ps[k], i % 5, String.format("%s%03d", BotChucNang.TIEN_TO, i + 1), hetGio);
            TAT_CA.add(n);
            ex.submit(n);
            Thread.sleep(1500);
        }
        ex.submit(() -> dieuPhoiSuKien(hetGio));
        while (System.currentTimeMillis() < hetGio + 60000) {
            synchronized (BotXuyenSuot.KHOA_DH) {
                if (BotXuyenSuot.dieuPhoi != null) { BotXuyenSuot.dieuPhoi.bom(200); if (!BotXuyenSuot.dieuPhoi.conSong()) { BotXuyenSuot.dieuPhoi.close(); BotXuyenSuot.dieuPhoi = null; } }
                if (BotXuyenSuot.dieuPhoi == null) {
                    BotChucNang.Bot x = new BotChucNang.Bot(tkDp);
                    try { String l2 = x.dangNhap("123456"); if (l2 == null) l2 = x.vaoGame(0); if (l2 == null) BotXuyenSuot.dieuPhoi = x; else x.close(); } catch (IOException e) { x.close(); }
                }
            }
            BotXuyenSuot.quetLog();
            Thread.sleep(10000);
            int song = 0;
            synchronized (TAT_CA) { for (NguoiChoi n : TAT_CA) if (n.b != null && !n.b.dut) song++; }
            System.out.printf("[%s] dang choi %d/%d | loi server %d | tua %d ngay | chat %d to doi %d cau %d dao %d nau %d mua_lk %d%n",
                    new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()), song, so, BotXuyenSuot.LOI_SERVER.size(),
                    BotXuyenSuot.tongTuaPhut / 1440, soChat.get(), soToDoi.get(), soCauCa.get(), soDao.get(), soNau.get(), soMuaLk.get());
            if (System.currentTimeMillis() > hetGio) break;
        }
        ex.shutdownNow();
        ex.awaitTermination(20, TimeUnit.SECONDS);
        baoCao();
        System.exit(0);
    }

    static void baoCao() throws IOException {
        StringBuilder sb = new StringBuilder();
        Map<Persona, List<NguoiChoi>> theo = new EnumMap<>(Persona.class);
        for (NguoiChoi n : TAT_CA) theo.computeIfAbsent(n.ps, z -> new ArrayList<>()).add(n);
        sb.append("| Persona | Số bot | Cấp TB đầu → cuối | Cấp cao nhất | NV chính | NV phụ/ngày | Gym | Hạ quái | Chết | Đổi map | Vào lại | Δxu TB | Δlượng khoá TB | Kẹt (bot) | Lỗi kiểm |\n");
        sb.append("|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|\n");
        for (Map.Entry<Persona, List<NguoiChoi>> e : theo.entrySet()) {
            List<NguoiChoi> l = e.getValue();
            double c0 = 0, c1 = 0, dx = 0, dlk = 0;
            int cmax = 0, nv = 0, phu = 0, gym = 0, ha = 0, chet = 0, dm = 0, vl = 0, ket = 0, loi = 0;
            for (NguoiChoi n : l) {
                c0 += n.capDau; c1 += n.capCao; cmax = Math.max(cmax, n.capCao); nv += n.soNvChinh; phu += n.soNvPhu; gym += n.soGym;
                ha += n.soHaQuai; chet += n.soChet; dm += n.soDoiMap; vl += n.soVaoLai; ket += n.ket.isEmpty() ? 0 : 1; loi += n.loi.size();
                if (n.b != null && n.xuDau >= 0) { dx += n.b.xu - n.xuDau; dlk += n.b.lk - n.lkDau; }
            }
            int k = l.size();
            sb.append(String.format("| %s | %d | %.1f → %.1f | %d | %d | %d | %d | %d | %d | %d | %d | %.0f | %.0f | %d | %d |%n",
                    e.getKey(), k, c0 / k, c1 / k, cmax, nv, phu, gym, ha, chet, dm, vl, dx / k, dlk / k, ket, loi));
        }
        sb.append("\nĐường cấp theo thời gian (mốc 5 phút, cấp TB):\n\n| Persona |");
        int maxMoc = 0;
        for (NguoiChoi n : TAT_CA) maxMoc = Math.max(maxMoc, n.moc.size());
        for (int i = 0; i < maxMoc; i++) sb.append(" ").append(i * 5).append("′ |");
        sb.append("\n|---|");
        for (int i = 0; i < maxMoc; i++) sb.append("---|");
        sb.append('\n');
        for (Map.Entry<Persona, List<NguoiChoi>> e : theo.entrySet()) {
            sb.append("| ").append(e.getKey()).append(" |");
            for (int i = 0; i < maxMoc; i++) {
                double s = 0; int c = 0;
                for (NguoiChoi n : e.getValue()) if (i < n.moc.size()) { s += n.moc.get(i)[1]; c++; }
                sb.append(c == 0 ? " |" : String.format(" %.1f |", s / c));
            }
            sb.append('\n');
        }
        sb.append("\nHoạt động: chat ").append(soChat).append(", tổ đội ").append(soToDoi).append(", câu cá ").append(soCauCa)
          .append(", đào ").append(soDao).append(", nấu ").append(soNau).append(", mua shop lượng khoá ").append(soMuaLk)
          .append(", đăng nhập lại (casual) ").append(soDangNhapLai).append(".\n\nĐếm sự kiện: ").append(new TreeMap<>(SU_KIEN)).append('\n');
        sb.append("\nĐồng hồ ảo: tua ").append(BotXuyenSuot.tongTuaPhut / 1440).append(" ngày, ").append(BotXuyenSuot.NHAT_KY_TUA.size()).append(" lần.\n");
        sb.append("\nLỗi server (E) trong lúc chạy: ").append(BotXuyenSuot.LOI_SERVER.size()).append('\n');
        for (String s : new LinkedHashSet<>(BotXuyenSuot.LOI_SERVER)) sb.append("- `").append(s.replace('`', '\'')).append("`\n");
        Map<String, Integer> ketGom = new TreeMap<>();
        for (NguoiChoi n : TAT_CA) for (String k : n.ket) ketGom.merge(k.replaceAll("\\d+", "N"), 1, Integer::sum);
        sb.append("\nKẹt (gom theo mẫu):\n");
        for (Map.Entry<String, Integer> e : ketGom.entrySet()) sb.append("- ×").append(e.getValue()).append(" ").append(e.getKey()).append('\n');
        Map<String, Integer> loiGom = new TreeMap<>();
        for (NguoiChoi n : TAT_CA) for (String k : n.loi) loiGom.merge(k.replaceAll("\\d+", "N"), 1, Integer::sum);
        sb.append("\nLỗi kiểm (gom):\n");
        for (Map.Entry<String, Integer> e : loiGom.entrySet()) sb.append("- ×").append(e.getValue()).append(" ").append(e.getKey()).append('\n');
        for (String s : LOI_BOT) sb.append("- bot: ").append(s).append('\n');
        System.out.println(sb);
        String ra = System.getProperty("ra");
        if (ra != null) Files.write(new File(ra).toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
