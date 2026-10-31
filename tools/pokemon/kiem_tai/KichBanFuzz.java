import java.util.*;

/**
 * Nhom bat thuong (chu game 31/10/2026): MOT bot = MOT ma goi tin (cmd -128..127), kich ban "fz_<cmd>". Nguoi choi THUONG
 * (tai khoan moi, khong GM) gui goi {@code cmd}:
 * <pre>
 *  sai_thu_tu  : ngay sau dang nhap, TRUOC khi vao game (rong + ngau nhien)
 *  rong / cut  : trong game, than rong; 1 byte; 3 byte
 *  rac         : 64 byte ngau nhien; 4000 byte ngau nhien; toan 0xFF; toan 0x00 (so am / do dai lon)
 *  spam        : 200 goi nho ngau nhien lien tiep
 *  dut         : goi ngau nhien roi dong socket ngay, dang nhap lai
 * </pre>
 * Kiem: may chu con nhan dang nhap moi (tai khoan thu hai vao game duoc), nguoi choi vao lai duoc, xu khong am. Ghi so dong
 * Exception moi trong game.log (-Dlog) de doc tay (khong tinh FAIL - nhieu handler cu nem ngoai le khi doc thieu byte).
 */
class KichBanFuzz {

    static void dangKy() {
        for (int c = -128; c <= 127; c++) {
            final int cmd = c;
            BotChucNang.dk("fz_" + c, false, () -> chay(cmd));
        }
    }

    static long dongLog() {
        String f = System.getProperty("log");
        if (f == null) return -1;
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(f))) {
            long n = 0;
            while (r.readLine() != null) n++;
            return n;
        } catch (Exception e) {
            return -1;
        }
    }

    static List<String> loiMoi(long tu) {
        List<String> l = new ArrayList<>();
        String f = System.getProperty("log");
        if (f == null || tu < 0) return l;
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String s;
            long n = 0;
            while ((s = r.readLine()) != null) if (n++ >= tu && (s.contains("Exception") || s.contains("Error"))) l.add(s.length() > 160 ? s.substring(0, 160) : s);
        } catch (Exception ignored) {
        }
        return l;
    }

    static void chay(int cmd) throws Exception {
        String t = "fz_" + cmd;
        Random rd = new Random(cmd * 7919L + System.nanoTime());
        long log0 = dongLog();
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("fz", (cmd & 0xff) % 5, (cmd & 1));
        String u = b.user;
        long xu0 = b.xu;
        List<String> buoc = new ArrayList<>();
        try {
            // trong game
            byte[][] mau = {new byte[0], {1}, {0, 1, 2}, ngau(rd, 64), ngau(rd, 4000), day(64, (byte) -1), day(64, (byte) 0)};
            String[] tenMau = {"rong", "1byte", "3byte", "rac64", "rac4000", "ff", "00"};
            for (int i = 0; i < mau.length && !b.dut; i++) {
                b.gui(cmd, mau[i]);
                b.bom(250);
                buoc.add(tenMau[i] + (b.dut ? "=DUT" : ""));
            }
            for (int i = 0; i < 200 && !b.dut; i++) b.gui(cmd, ngau(rd, rd.nextInt(12)));
            b.bom(1500);
            buoc.add("spam200" + (b.dut ? "=DUT" : ""));
            // dut giua chung
            if (!b.dut) b.gui(cmd, ngau(rd, 16));
        } catch (Exception e) {
            buoc.add("gui loi " + e);
        }
        b.close();
        Thread.sleep(2500);
        // sai thu tu: truoc khi vao game
        BotChucNang.Bot c = new BotChucNang.Bot(u);
        String l = c.dangNhap(HoiQuy.MAT_KHAU);
        try {
            if (l == null) {
                c.gui(cmd, new byte[0]);
                c.gui(cmd, ngau(rd, 32));
                c.bom(500);
                buoc.add("truoc_vao_game" + (c.dut ? "=DUT" : ""));
            }
        } catch (Exception e) {
            buoc.add("truoc_vao_game loi " + e);
        }
        c.close();
        Thread.sleep(2500);
        // may chu con song + nguoi choi vao lai duoc
        BotChucNang.Bot d = new BotChucNang.Bot(u);
        String l2 = d.dangNhap(HoiQuy.MAT_KHAU);
        if (l2 == null) l2 = d.vaoGame(0);
        boolean vaoLai = l2 == null;
        if (vaoLai) d.lamMoi();
        long xu1 = d.xu;
        d.close();
        String l3;
        try {
            BotChucNang.Bot e = HoiQuy.nguoiChoiMoi("fy", 0, 0);
            l3 = null;
            e.close();
        } catch (Exception ex) {
            l3 = String.valueOf(ex);
        }
        List<String> loi = loiMoi(log0);
        boolean ok = vaoLai && l3 == null && xu1 >= 0;
        BotChucNang.ket(t, ok, "cmd " + cmd + " " + buoc + " | vao lai: " + (vaoLai ? "ok" : l2) + " | nguoi moi: " + (l3 == null ? "ok" : l3)
                + " | xu " + xu0 + "->" + xu1 + " | loi log moi: " + loi.size() + (loi.isEmpty() ? "" : " " + loi.get(0)));
    }

    static byte[] ngau(Random r, int n) {
        byte[] a = new byte[n];
        r.nextBytes(a);
        return a;
    }

    static byte[] day(int n, byte v) {
        byte[] a = new byte[n];
        Arrays.fill(a, v);
        return a;
    }
}
