import dev.peerova.PeerovaTunnel;

/**
 * Bot NGUOI CHOI THUONG (khong GM) vao game QUA PEEROVA P2P: mo PeerovaTunnel client bang ticket cua may chu, roi
 * dung dung giao thuc game cua bot kiem thu (tools/pokemon/kiem_tai/BotChucNang.Bot) tren cong 127.0.0.1 tunnel cap:
 * ket noi (-40 khoa) -> dang nhap (1) -> tao / chon nhan vat (14, 13) -> vao map (12) -> chat (27) -> thoat.
 * Kiem them: N bot P2P cung luc (-Dso=N) va ket noi lai sau khi dong tunnel.
 *
 *   java -Dpeerova.jni.path=<libpeerova_jni.so> -Dnoi=<thu muc stack> -cp <bot>:<lop> KiemDangNhapP2P <ticket|@tep> <cong game> <tk...>
 */
public final class KiemDangNhapP2P {
    static int loi;

    static void kiem(boolean ok, String s) {
        System.out.println((ok ? "  OK   " : "  HONG ") + s);
        if (!ok) loi++;
    }

    public static void main(String[] a) throws Exception {
        String ticket = a[0].startsWith("@") ? new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(a[0].substring(1))), "UTF-8").trim() : a[0];
        int cong = Integer.parseInt(a[1]);
        System.out.println("Peerova " + dev.peerova.Peerova.version() + ", ticket " + ticket.substring(0, Math.min(24, ticket.length())) + "...");
        PeerovaTunnel t = PeerovaTunnel.client(ticket, "{\"forward\":[\"tcp:0=" + cong + "\"]}");
        long t0 = System.currentTimeMillis();
        int st = t.await(20000);
        String l;
        while ((l = t.nextLog()) != null) System.out.println("  [p2p] " + l);
        kiem(st == PeerovaTunnel.UP, "tunnel P2P len sau " + (System.currentTimeMillis() - t0) + " ms (cong cuc bo " + t.localPort(0) + ") " + (st == PeerovaTunnel.UP ? "" : t.error()));
        if (st != PeerovaTunnel.UP) System.exit(1);
        BotChucNang.HOST = "127.0.0.1";
        BotChucNang.PORT = t.localPort(0);
        // nhieu nguoi choi qua MOT tunnel (moi ket noi game = mot stream doc lap)
        java.util.List<BotChucNang.Bot> ds = new java.util.ArrayList<>();
        for (int i = 2; i < a.length; i++) {
            BotChucNang.Bot b = new BotChucNang.Bot(a[i]);
            long d0 = System.currentTimeMillis();
            String r = b.dangNhap("123456");
            kiem(r == null, a[i] + ": dang nhap qua P2P (" + (System.currentTimeMillis() - d0) + " ms) " + (r == null ? "" : r));
            if (r != null) continue;
            r = b.vaoGame(i % 5);
            kiem(r == null, a[i] + ": vao game, map " + b.mapId + " " + (r == null ? "" : r));
            if (r == null) {
                b.chat("xin chao qua P2P");
                b.bom(800);
                kiem(!b.dut, a[i] + ": van ket noi sau chat");
            }
            ds.add(b);
        }
        for (BotChucNang.Bot b : ds) b.close();
        // tunnel chet -> lan ket noi sau tao tunnel moi (nhu htbb.p2p.KetNoiP2P khi mat mang)
        t.close();
        PeerovaTunnel t2 = PeerovaTunnel.client(ticket, "{\"forward\":[\"tcp:0=" + cong + "\"]}");
        kiem(t2.await(20000) == PeerovaTunnel.UP, "tunnel thu hai (ket noi lai) len");
        BotChucNang.PORT = t2.localPort(0);
        BotChucNang.Bot b = new BotChucNang.Bot(a[2]);
        String r = b.dangNhap("123456");
        kiem(r == null, a[2] + ": dang nhap lai qua tunnel moi " + (r == null ? "" : r));
        b.close();
        t2.close();
        // ticket hong -> loi ro rang, khong treo
        try {
            PeerovaTunnel.client("pvt_hong", "{\"forward\":[\"tcp:0=" + cong + "\"]}");
            kiem(false, "ticket hong phai bao loi");
        } catch (RuntimeException e) {
            kiem(true, "ticket hong -> loi: " + e.getMessage());
        }
        System.out.println(loi == 0 ? "DAT KiemDangNhapP2P" : "HONG KiemDangNhapP2P (" + loi + " loi)");
        System.exit(loi == 0 ? 0 : 1);
    }
}
