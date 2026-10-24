import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BOT HON LOAN (monkey) cho bo hoi quy: N nguoi choi that (dang ky moi, khong GM) lam hanh dong HOP LE ngau nhien, kem
 * BatBien: bam dup (gui hai lan sat nhau), mo menu ngau nhien, di bo, mua / dung thuoc, nhan / tra nhiem vu, Ve lang,
 * thach dau Gym, len tau, giao dich, VA rot mang giua chung (dong socket ngay sau lenh mua / dung / giao dich / dich
 * chuyen / Gym / len tau) roi vao lai; hai phien cung tai khoan. Sau cung: moi tai khoan tien khong am, luong (tien nap)
 * khong tu tang, may chu con nhan dang nhap moi.
 */
final class BotHonLoan {

    private BotHonLoan() {
    }

    static final AtomicInteger HANH_DONG = new AtomicInteger(), ROT = new AtomicInteger();
    static final ConcurrentHashMap<String, AtomicInteger> DEM = new ConcurrentHashMap<>();

    static void dem(String k) {
        DEM.computeIfAbsent(k, z -> new AtomicInteger()).incrementAndGet();
        HANH_DONG.incrementAndGet();
    }

    static void chay(int phut) throws Exception {
        int so = Integer.getInteger("honloanSo", 4);
        long het = System.currentTimeMillis() + phut * 60000L;
        List<String> tk = new ArrayList<>();
        Map<String, long[]> dau = new ConcurrentHashMap<>();
        ExecutorService ex = Executors.newFixedThreadPool(so);
        List<Future<?>> fs = new ArrayList<>();
        String kb = HoiQuy.KB_HIEN.get();
        for (int i = 0; i < so; i++) {
            BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("hl", i % 5, i % 2);
            KichBanHoiQuy.boQuaHuongDan(b);
            tk.add(b.user);
            dau.put(b.user, new long[]{b.xu, b.luong, b.lk});
            final BotChucNang.Bot b0 = b;
            final long seed = 1000 + i;
            fs.add(ex.submit(() -> {
                HoiQuy.KB_HIEN.set(kb);
                try {
                    mot(b0, new Random(seed), het);
                } catch (Throwable t) {
                    HoiQuy.ket("hon_loan.bot_" + b0.user, false, "ngoai le bot: " + t);
                }
            }));
        }
        for (Future<?> f : fs) {
            try { f.get(phut * 60000L + 180000L, TimeUnit.MILLISECONDS); } catch (Exception e) { f.cancel(true); }
        }
        ex.shutdownNow();
        Thread.sleep(3000);
        // ket qua chung
        List<String> sai = new ArrayList<>();
        for (String u : tk) {
            Integer id = HoiQuy.CHAR_DB.get(u);
            String r = BotChucNang.sql1("SELECT CONCAT(gold,',',luong,',',luonglock) FROM tob_char WHERE id=" + id);
            if (r == null) continue;
            String[] f = r.split(",");
            long xu = Long.parseLong(f[0]);
            long luong = Long.parseLong(f[1]), lk = Long.parseLong(f[2]);
            if (xu < 0 || luong < 0 || lk < 0) sai.add(u + " tien am " + r);
            if (luong > dau.get(u)[1]) sai.add(u + " luong tu tang " + dau.get(u)[1] + "->" + luong);
        }
        HoiQuy.ket("hon_loan.tien_csdl", sai.isEmpty(), tk.size() + " tai khoan; " + sai);
        BotChucNang.Bot moi = HoiQuy.nguoiChoiMoi("hm", 0, 0);
        HoiQuy.ket("hon_loan.may_chu_con_song", moi.mapId >= 0, "dang ky + vao game sau hon loan: map " + moi.mapId);
        moi.close();
        HoiQuy.ket("hon_loan.thong_ke", true, HANH_DONG.get() + " hanh dong, " + ROT.get() + " lan rot mang: " + DEM);
    }

    static void mot(BotChucNang.Bot b, Random r, long het) throws Exception {
        String u = b.user;
        while (System.currentTimeMillis() < het) {
            if (b.dut || !b.conSong()) {
                b.close();
                b = HoiQuy.vaoLai(u);
                KichBanHuongDan.roiSan(b);
            }
            int c = r.nextInt(16);
            try {
            switch (c) {
                case 0: case 1: case 2: { // di bo ngau nhien
                    BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                    KichBanHuongDan.dongBoViTri(b);
                    if (bd != null) {
                        int[] o = bd.oGan(b.px / 16 + r.nextInt(11) - 5, b.py / 16 + r.nextInt(11) - 5, 3);
                        if (o != null && !bd.laCong(o[0], o[1])) b.diDuong(bd, o[0], o[1]);
                    }
                    dem("di_bo");
                    break;
                }
                case 3: { // qua cong ngau nhien
                    if (r.nextBoolean()) KichBanDS2.raNgoai(b); else KichBanHuongDan.quaCong(b, r.nextInt(3));
                    dem("qua_cong");
                    break;
                }
                case 4: { // So tay: muc ngau nhien, bam dup
                    KichBanDS4.moSoTay(b);
                    if (!b.menuChu.isEmpty()) {
                        int i = r.nextInt(b.menuChu.size());
                        b.menu(-527, 0, i);
                        b.menu(-527, 0, i);
                        b.bom(900);
                        // menu con ngau nhien
                        if (b.menuCuoi != null && !b.menuChu.isEmpty() && r.nextBoolean()) {
                            b.menu(b.menuCuoi[0], b.menuCuoi[1], r.nextInt(b.menuChu.size()));
                            b.bom(800);
                        }
                    }
                    dem("so_tay");
                    break;
                }
                case 5: { // Quay doi thuong muc ngau nhien
                    int m = b.moc();
                    b.gui(23, (byte) -90);
                    if (b.doiMenu(m, -90, 0, 2000) > 0) { b.menu(-90, 0, r.nextInt(b.menuChu.size())); b.bom(900); }
                    dem("quay");
                    break;
                }
                case 6: { // dung thuoc / ether bam dup
                    b.gui(22, (byte) 1);
                    b.gui(22, (byte) 1);
                    b.gui(22, (byte) 4);
                    b.bom(500);
                    dem("dung_thuoc");
                    break;
                }
                case 7: { // nhiem vu: xem / nhan / tra bam dup
                    b.gui(-64, (byte) 0, (short) 0, (byte) 0);
                    b.gui(-64, (byte) 1, (short) 0, (byte) 0);
                    b.gui(-64, (byte) 1, (short) 0, (byte) 0);
                    b.gui(-64, (byte) 2, (short) 0, (byte) 0);
                    b.bom(900);
                    dem("nhiem_vu");
                    break;
                }
                case 8: { // danh quai gan
                    List<Integer> song = KichBanDS.timQuai(b, 0, 200);
                    if (!song.isEmpty()) KichBanHoiQuy2.haQuaiDiBo(b, song.get(r.nextInt(song.size())), 25);
                    dem("danh_quai");
                    break;
                }
                case 9: { // mua shop dac biet (het tien = phai tu choi sach) bam dup
                    b.gui(85, (byte) 2);
                    b.gui(85, (byte) 2);
                    b.bom(800);
                    dem("shop_dac_biet");
                    break;
                }
                case 10: { // ROT MANG ngay sau mot lenh
                    int k = r.nextInt(6);
                    if (k == 0) b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 2);
                    else if (k == 1) b.gui(22, (byte) 162);
                    else if (k == 2) { KichBanDS4.moSoTay(b); int i = b.timMuc("Về làng"); if (i >= 0) b.menu(-527, 0, i); }
                    else if (k == 3) b.gui(82);
                    else if (k == 4) { KichBanDS4.moSoTay(b); int i = b.timMuc("Thách đấu Gym"); if (i >= 0) { b.menu(-527, 0, i); b.bom(300); b.menu(-527, 5, 0); } }
                    else b.gui(85, (byte) 8);
                    BatBien.kiemKet(b);
                    b.close();
                    ROT.incrementAndGet();
                    Thread.sleep(300 + r.nextInt(1500));
                    b = HoiQuy.vaoLai(u);
                    KichBanHuongDan.roiSan(b);
                    dem("rot_mang_" + k);
                    break;
                }
                case 11: { // hai phien cung tai khoan
                    BotChucNang.Bot moi = HoiQuy.vaoLai(u);
                    b.bom(500);
                    b.close();
                    b = moi;
                    dem("hai_phien");
                    break;
                }
                case 12: { // Ve lang bam dup
                    KichBanDS4.moSoTay(b);
                    int i = b.timMuc("Về làng");
                    if (i >= 0) { b.menu(-527, 0, i); b.menu(-527, 0, i); b.bom(1500); if (b.coLenh(Math.max(0, b.moc() - 20), 12)) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(600); } }
                    dem("ve_lang");
                    break;
                }
                case 13: { // chat
                    b.chat("hl " + r.nextInt(1000));
                    b.bom(300);
                    dem("chat");
                    break;
                }
                case 14: { // popup / menu dong y vu vo (id ngau nhien)
                    b.gui(-32, (short) b.actor, (byte) (90 + r.nextInt(8)), "", (byte) r.nextInt(2));
                    b.bom(500);
                    dem("popup_vu_vo");
                    break;
                }
                default: { // lam moi + doi
                    b.lamMoi();
                    b.bom(500);
                    dem("cho");
                }
            }
            } catch (IOException e) {
                // ket noi dut giua hanh dong (rot mang / phien moi day phien cu): vao lai nhu nguoi choi
                ROT.incrementAndGet();
                b.close();
                b = HoiQuy.vaoLai(u);
            }
            if (b.mapId == 105) KichBanHuongDan.roiSan(b);
        }
        BatBien.kiemKet(b);
        b.close();
    }
}
