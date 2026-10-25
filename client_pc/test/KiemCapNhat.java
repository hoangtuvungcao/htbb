import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM CẬP NHẬT GAME (0.9.7): game.pokemon.CapNhat (gói -115, hộp NHẮC / BẮT BUỘC, nhắc một lần mỗi ngày, mở link khi
 * không tự cập nhật được) + bộ cập nhật htbb.capnhat.TaiVe / CapNhatChung / htbb.pc.CapNhatPc với MÁY CHỦ HTTP CỤC BỘ
 * (com.sun.net.httpserver): tải đủ + đúng SHA-256 + tiến độ tăng dần; SHA-256 lệch / thiếu / 404 / tải thiếu / huỷ bị từ
 * chối và không để lại tệp; Linux AppImage thay NGUYÊN TỬ + mở lại; Windows chạy bộ cài /SILENT; nền tảng không cài được
 * -> mở trang tải. VẼ hộp ở mọi cỡ chuẩn của ChupTrongGame: trọn vùng an toàn, nút đủ cỡ chạm, chữ chính đã vẽ.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemCapNhat.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemCapNhat <thư mục ảnh>
 * Mã thoát 1 = có phép kiểm hỏng.
 */
public class KiemCapNhat {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    // ------------------------------------------------------------------ máy chủ HTTP cục bộ
    static HttpServer http;
    static String goc;
    static final byte[] TEP = new byte[3 * 1024 * 1024 + 123];
    static final byte[] TEP_KHAC = new byte[200 * 1024];
    static String shaTep;

    static String sha(byte[] b) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        StringBuilder s = new StringBuilder();
        for (byte x : md.digest(b)) s.append(String.format("%02x", x));
        return s.toString();
    }

    static void batMayChu() throws Exception {
        new java.util.Random(7).nextBytes(TEP);
        new java.util.Random(9).nextBytes(TEP_KHAC);
        shaTep = sha(TEP);
        http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        for (final String duong : new String[]{"/tai/HTBB-PC-Linux-0.9.7.AppImage", "/tai/HTBB-PC-Setup-0.9.7.exe", "/tai/HTBB-0.9.7.apk"}) {
            http.createContext(duong, ex -> {
                ex.sendResponseHeaders(200, TEP.length);
                try (OutputStream o = ex.getResponseBody()) {
                    for (int i = 0; i < TEP.length; i += 65536) o.write(TEP, i, Math.min(65536, TEP.length - i));
                }
            });
        }
        http.createContext("/chuyen", ex -> {
            ex.getResponseHeaders().add("Location", "/tai/HTBB-PC-Linux-0.9.7.AppImage");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        http.createContext("/khac.AppImage", ex -> {
            ex.sendResponseHeaders(200, TEP_KHAC.length);
            try (OutputStream o = ex.getResponseBody()) {
                o.write(TEP_KHAC);
            }
        });
        http.createContext("/thieu.AppImage", ex -> {
            ex.sendResponseHeaders(200, TEP.length);   // khai đủ, gửi một nửa rồi ngắt
            OutputStream o = ex.getResponseBody();
            o.write(TEP, 0, TEP.length / 2);
            o.flush();
            ex.getHttpContext().getServer();
            ex.close();
        });
        http.createContext("/cham.AppImage", ex -> {
            ex.sendResponseHeaders(200, TEP.length);
            try (OutputStream o = ex.getResponseBody()) {
                for (int i = 0; i < TEP.length; i += 16384) {
                    o.write(TEP, i, Math.min(16384, TEP.length - i));
                    o.flush();
                    Thread.sleep(40);
                }
            } catch (Exception e) {
                // máy khách huỷ
            }
        });
        http.start();
        goc = "http://127.0.0.1:" + http.getAddress().getPort();
    }

    // ------------------------------------------------------------------ TaiVe
    static void kiemTaiVe(File tam) throws Exception {
        final List<long[]> td = new ArrayList<>();
        final boolean[] kiemSha = {false};
        htbb.capnhat.TaiVe.TienDo bao = new htbb.capnhat.TaiVe.TienDo() {
            public void tienDo(long da, long tong) {
                td.add(new long[]{da, tong});
            }

            public void dangKiem() {
                kiemSha[0] = true;
            }
        };
        File d = new File(tam, "ok.AppImage");
        File f = htbb.capnhat.TaiVe.tai(goc + "/tai/HTBB-PC-Linux-0.9.7.AppImage", shaTep.toUpperCase(), d, bao, null);
        kiem(f.isFile() && f.length() == TEP.length, "TaiVe: tải đủ " + TEP.length + " byte");
        kiem(shaTep.equals(htbb.capnhat.TaiVe.sha256(f)), "TaiVe: SHA-256 tệp tải về khớp");
        kiem(!new File(d.getPath() + ".part").exists(), "TaiVe: không còn tệp .part");
        boolean tang = td.size() >= 2;
        for (int i = 1; i < td.size(); i++) if (td.get(i)[0] < td.get(i - 1)[0]) tang = false;
        kiem(tang, "TaiVe: tiến độ tăng dần (" + td.size() + " lần báo)");
        kiem(!td.isEmpty() && td.get(td.size() - 1)[0] == TEP.length && td.get(td.size() - 1)[1] == TEP.length, "TaiVe: lần báo cuối = 100%");
        kiem(kiemSha[0], "TaiVe: báo bước kiểm SHA-256");
        // chuyển hướng
        File d2 = htbb.capnhat.TaiVe.tai(goc + "/chuyen", shaTep, new File(tam, "chuyen.bin"), null, null);
        kiem(d2.length() == TEP.length, "TaiVe: theo chuyển hướng 302");
        // SHA-256 lệch: tệp không được giữ, tệp đích cũ giữ nguyên
        File cu = new File(tam, "cu.AppImage");
        Files.write(cu.toPath(), "BAN CU".getBytes("UTF-8"));
        String loi = null;
        try {
            htbb.capnhat.TaiVe.tai(goc + "/khac.AppImage", shaTep, cu, null, null);
        } catch (IOException e) {
            loi = e.getMessage();
        }
        kiem(loi != null && loi.contains("SHA-256"), "TaiVe: SHA-256 lệch bị TỪ CHỐI (" + loi + ")");
        kiem("BAN CU".equals(new String(Files.readAllBytes(cu.toPath()), "UTF-8")), "TaiVe: SHA lệch -> tệp cũ giữ nguyên");
        kiem(!new File(cu.getPath() + ".part").exists(), "TaiVe: SHA lệch -> xoá tệp dở");
        // thiếu SHA-256
        loi = null;
        try {
            htbb.capnhat.TaiVe.tai(goc + "/tai/HTBB-0.9.7.apk", "", new File(tam, "x.apk"), null, null);
        } catch (IOException e) {
            loi = e.getMessage();
        }
        kiem(loi != null && !new File(tam, "x.apk").exists(), "TaiVe: thiếu SHA-256 -> không tải (" + loi + ")");
        // 404
        loi = null;
        try {
            htbb.capnhat.TaiVe.tai(goc + "/khong-co.apk", shaTep, new File(tam, "y.apk"), null, null);
        } catch (IOException e) {
            loi = e.getMessage();
        }
        kiem(loi != null && loi.contains("404"), "TaiVe: 404 -> lỗi rõ ràng (" + loi + ")");
        // tải thiếu
        loi = null;
        try {
            htbb.capnhat.TaiVe.tai(goc + "/thieu.AppImage", shaTep, new File(tam, "thieu.AppImage"), null, null);
        } catch (IOException e) {
            loi = e.getMessage();
        }
        kiem(loi != null && !new File(tam, "thieu.AppImage").exists() && !new File(tam, "thieu.AppImage.part").exists(),
                "TaiVe: mạng ngắt giữa chừng -> lỗi, không để tệp (" + loi + ")");
        // link lạ
        kiem(!htbb.capnhat.TaiVe.urlHopLe("file:///etc/passwd") && !htbb.capnhat.TaiVe.urlHopLe("javascript:x"), "TaiVe: chỉ http/https");
        // máy chủ không có
        loi = null;
        try {
            htbb.capnhat.TaiVe.tai("http://127.0.0.1:1/a.apk", shaTep, new File(tam, "z.apk"), null, null);
        } catch (IOException e) {
            loi = e.getMessage();
        }
        kiem(loi != null && loi.startsWith("Không kết nối"), "TaiVe: không có máy chủ -> 'Không kết nối…' (" + loi + ")");
    }

    // ------------------------------------------------------------------ CapNhatPc
    static void kiemPc(File tam) throws Exception {
        final List<List<String>> lenh = new ArrayList<>();
        final int[] thoat = {0};
        htbb.pc.CapNhatPc.chayLenh = l -> lenh.add(new ArrayList<>(l));
        htbb.pc.CapNhatPc.thoat = () -> thoat[0]++;
        htbb.pc.CapNhatPc.choThoatMs = 10;
        htbb.pc.Chay.thamSo = new String[]{"--may-chu", "127.0.0.1:1"};
        // Linux AppImage: thay nguyên tử + mở lại
        File thu = new File(tam, "appimage");
        thu.mkdirs();
        File ai = new File(thu, "HTBB-PC-Linux-0.9.6.AppImage");
        Files.write(ai.toPath(), "BAN CU".getBytes("UTF-8"));
        htbb.pc.CapNhatPc.heDieuHanh = "linux";
        htbb.pc.CapNhatPc.appImage = ai.getPath();
        htbb.pc.CapNhatPc c = new htbb.pc.CapNhatPc();
        String url = goc + "/tai/HTBB-PC-Linux-0.9.7.AppImage";
        kiem(c.hoTro(url, shaTep), "PC Linux: AppImage ghi được -> tự cập nhật");
        kiem(!c.hoTro(url, ""), "PC Linux: thiếu SHA-256 -> không tự cập nhật (mở trang tải)");
        kiem(!c.hoTro(goc + "/tai/HTBB-PC-Setup-0.9.7.exe", shaTep), "PC Linux: link .exe -> không");
        c.batDau(url, shaTep, "0.9.7");
        c.cho(20000);
        Thread.sleep(100);
        kiem(c.trangThai() == game.pokemon.CapNhatNen.XONG, "PC Linux: tải + cài xong (trạng thái " + c.trangThai() + " " + c.loi() + ")");
        kiem(java.util.Arrays.equals(Files.readAllBytes(ai.toPath()), TEP), "PC Linux: AppImage đã được thay bằng bản mới (cùng đường dẫn)");
        kiem(ai.canExecute(), "PC Linux: AppImage mới chạy được (chmod +x)");
        File[] con = thu.listFiles();
        kiem(con != null && con.length == 1, "PC Linux: không còn tệp tạm cạnh AppImage (" + (con == null ? 0 : con.length) + ")");
        kiem(lenh.size() == 1 && lenh.get(0).get(0).equals(ai.getAbsolutePath()) && lenh.get(0).contains("--may-chu"),
                "PC Linux: mở lại AppImage mới với cùng tham số " + lenh);
        kiem(thoat[0] == 1, "PC Linux: đóng game sau khi mở bản mới");
        kiem(c.ghiChu().contains("0.9.7"), "PC Linux: câu báo có phiên bản (" + c.ghiChu() + ")");
        // Linux: SHA lệch -> LỖI, AppImage giữ nguyên, không mở lại
        Files.write(ai.toPath(), "BAN CU".getBytes("UTF-8"));
        lenh.clear();
        thoat[0] = 0;
        htbb.pc.CapNhatPc c2 = new htbb.pc.CapNhatPc();
        c2.batDau(goc + "/khac.AppImage", shaTep, "0.9.7");
        c2.cho(20000);
        kiem(c2.trangThai() == game.pokemon.CapNhatNen.LOI && c2.loi().contains("SHA-256"), "PC Linux: SHA lệch -> LỖI (" + c2.loi() + ")");
        kiem("BAN CU".equals(new String(Files.readAllBytes(ai.toPath()), "UTF-8")), "PC Linux: SHA lệch -> AppImage cũ còn nguyên");
        kiem(lenh.isEmpty() && thoat[0] == 0, "PC Linux: SHA lệch -> không mở lại / không đóng game");
        // huỷ giữa chừng
        htbb.pc.CapNhatPc c3 = new htbb.pc.CapNhatPc();
        c3.batDau(goc + "/cham.AppImage", shaTep, "0.9.7");
        Thread.sleep(400);
        kiem(c3.trangThai() == game.pokemon.CapNhatNen.DANG_TAI && c3.daTai() > 0, "PC: đang tải chậm -> DANG_TAI, đã có byte (" + c3.daTai() + ")");
        c3.huy();
        c3.cho(10000);
        Thread.sleep(200);
        kiem(c3.trangThai() == game.pokemon.CapNhatNen.CHUA, "PC: huỷ -> về CHƯA (" + c3.trangThai() + ")");
        kiem("BAN CU".equals(new String(Files.readAllBytes(ai.toPath()), "UTF-8")) && thu.listFiles().length == 1, "PC: huỷ -> không đổi gì, xoá tệp dở");
        // không phải AppImage / thư mục không ghi được / macOS
        htbb.pc.CapNhatPc.appImage = new File(tam, "khong-co.AppImage").getPath();
        kiem(!new htbb.pc.CapNhatPc().hoTro(url, shaTep), "PC Linux: không chạy từ AppImage (jar / tar.gz) -> không tự cập nhật");
        htbb.pc.CapNhatPc.appImage = ai.getPath();
        thu.setWritable(false);
        kiem(!new htbb.pc.CapNhatPc().hoTro(url, shaTep), "PC Linux: thư mục AppImage không ghi được -> không tự cập nhật");
        thu.setWritable(true);
        htbb.pc.CapNhatPc.heDieuHanh = "mac os x";
        kiem(!new htbb.pc.CapNhatPc().hoTro(url, shaTep), "PC macOS -> không tự cập nhật");
        // Windows: chạy bộ cài /SILENT rồi đóng game
        htbb.pc.CapNhatPc.heDieuHanh = "windows";
        htbb.pc.CapNhatPc.thuMucTai = new File(tam, "win");
        lenh.clear();
        thoat[0] = 0;
        String exe = goc + "/tai/HTBB-PC-Setup-0.9.7.exe";
        htbb.pc.CapNhatPc w = new htbb.pc.CapNhatPc();
        kiem(w.hoTro(exe, shaTep) && !w.hoTro(url, shaTep), "PC Windows: nhận .exe, không nhận .AppImage");
        w.batDau(exe, shaTep, "0.9.7");
        w.cho(20000);
        Thread.sleep(100);
        File daTai = new File(new File(tam, "win"), "HTBB-PC-Setup-0.9.7.exe");
        kiem(w.trangThai() == game.pokemon.CapNhatNen.XONG && daTai.length() == TEP.length, "PC Windows: tải bộ cài xong");
        kiem(lenh.size() == 1 && lenh.get(0).get(0).equals(daTai.getAbsolutePath()) && lenh.get(0).contains("/SILENT")
                && lenh.get(0).contains("/CLOSEAPPLICATIONS"), "PC Windows: chạy bộ cài nâng cấp tại chỗ " + lenh);
        kiem(thoat[0] == 1, "PC Windows: đóng game để bộ cài thay tệp");
        kiem(w.caiLai() && lenh.size() == 2, "PC Windows: Cài lại chạy lại bộ cài đã tải (không tải lại)");
        htbb.pc.CapNhatPc.heDieuHanh = null;
        htbb.pc.CapNhatPc.appImage = null;
        htbb.pc.CapNhatPc.thuMucTai = null;
    }

    // ------------------------------------------------------------------ gói -115 + hộp
    static network.Message goi(byte[] du) throws Exception {
        java.lang.reflect.Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        return c.newInstance((byte) -115, du);
    }

    /** Mỗi lần nhận một gói MỚI (luồng đọc của gói chỉ đọc được một lần). */
    static network.Message moi(byte[] du) {
        try {
            return goi(du);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] goi115(int loai, int moi, String pb, String url, String sha, String dl, String nk, int toiThieu, int banClient) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream o = new java.io.DataOutputStream(b);
        o.writeByte(loai);
        o.writeInt(moi);
        o.writeUTF(pb);
        o.writeUTF(url);
        o.writeUTF(sha);
        o.writeUTF(dl);
        o.writeUTF(nk);
        o.writeUTF("https://huyenthoaibaoboi.fun/download");
        o.writeInt(toiThieu);
        o.writeInt(banClient);
        return b.toByteArray();
    }

    static final String NHAT_KY = "Cập nhật ngay trong game\n- Sửa lỗi câu cá\n* Thêm bản đồ Hồ Lớn\nMột dòng rất dài để kiểm ngắt dòng trong thẻ cập nhật khi màn hình hẹp của điện thoại";

    /** Bộ cập nhật giả: trạng thái / tiến độ đặt tay (vẽ hộp tải). */
    static final class NenGia implements game.pokemon.CapNhatNen {
        int st = CHUA;
        long da, tong;
        boolean hoTro = true;
        String url;
        int caiLai;

        public boolean hoTro(String u, String s) {
            return hoTro;
        }

        public void batDau(String u, String s, String p) {
            url = u;
            st = DANG_TAI;
        }

        public int trangThai() {
            return st;
        }

        public long daTai() {
            return da;
        }

        public long tong() {
            return tong;
        }

        public String loi() {
            return "Không kết nối được máy chủ tải";
        }

        public String ghiChu() {
            return "Đã tải xong bản 0.9.7. Bấm Cài đặt trong hộp của Android.";
        }

        public void huy() {
            st = CHUA;
        }

        public boolean caiLai() {
            caiLai++;
            return true;
        }
    }

    static boolean daVe(String dau) {
        synchronized (htbb.pc.ChuHD.ghi) {
            for (String x : htbb.pc.ChuHD.ghi) if (x.startsWith(dau)) return true;
        }
        return false;
    }

    static int[] vungTen(String ten) {
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return game.pokemon.VungCham.vungVe(i);
        }
        return null;
    }

    static void kiemGoi() throws Exception {
        final network.Message bb = goi(goi115(2, 907, "0.9.7", "https://huyenthoaibaoboi.fun/tai/HTBB-0.9.7.apk", shaTep, "85,3 MB", NHAT_KY, 907, 905));
        final byte[] nh0 = goi115(1, 908, "0.9.8", "https://huyenthoaibaoboi.fun/tai/HTBB-0.9.8.apk", shaTep, "86 MB", NHAT_KY, 0, 907);
        final NenGia gia = new NenGia();
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.pokemon.CapNhat.datNen(gia);
            game.render.screen.RMS.saveRMSString("htbbcapnhat", "0:0");
            kiem(game.pokemon.CapNhat.nhan(bb), "gói -115 bắt buộc đọc được");
            game.pokemon.CapNhat c = game.pokemon.CapNhat.hienTai();
            kiem(c != null && c.banMoi == 907 && "0.9.7".equals(c.phienBan) && c.banToiThieu == 907 && c.banClient == 905
                    && "85,3 MB".equals(c.dungLuong) && c.trangTai.endsWith("/download"), "gói -115: đủ trường");
            kiem(game.pokemon.CapNhat.dangMo(), "BẮT BUỘC: hộp hiện NGAY (màn đăng nhập)");
            c.deSau();
            kiem(game.pokemon.CapNhat.dangMo(), "BẮT BUỘC: không có đường Để sau");
            c.keyPress(27);
            kiem(game.pokemon.CapNhat.dangMo(), "BẮT BUỘC: Esc / Back không đóng");
            game.render.Canvas.currentDialog = null;   // hộp khác đè (mất kết nối...)
        });
        Thread.sleep(700);
        ChupTrongGame.chay(() -> {
            game.pokemon.CapNhat.tick();
            kiem(game.pokemon.CapNhat.dangMo(), "BẮT BUỘC: bị hộp khác đè -> tự hiện lại");
            game.pokemon.CapNhat c = game.pokemon.CapNhat.hienTai();
            c.capNhatNgay();
            kiem(gia.st == game.pokemon.CapNhatNen.DANG_TAI && c.url.equals(gia.url), "BẮT BUỘC: Tải bản mới -> bộ cập nhật nền tảng tải đúng link");
            gia.st = game.pokemon.CapNhatNen.CHUA;
        });
        // NHẮC: chờ vào thế giới (giả lập: chưa có nhân vật = màn đăng nhập / chọn nhân vật)
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.currentScreen = game.render.Canvas.loginScr;
        });
        ChupTrongGame.chay(() -> {
            kiem(game.pokemon.CapNhat.nhan(moi(nh0)), "gói -115 nhắc đọc được");
            game.pokemon.CapNhat.tick();
            kiem(!game.pokemon.CapNhat.dangMo() && game.pokemon.CapNhat.dangCho(), "NHẮC: chưa vào thế giới -> chờ, không chặn");
            ChupTrongGame.moMan("the_gioi");
            game.render.Canvas.endDlg();
        });
        Thread.sleep(1600);
        ChupTrongGame.chay(() -> {
            game.pokemon.CapNhat.tick();
            kiem(game.pokemon.CapNhat.dangMo(), "NHẮC: vào thế giới -> hiện thẻ Đã có bản mới");
            String luu = game.render.screen.RMS.loadRMSString("htbbcapnhat");
            kiem(game.pokemon.CapNhat.daNhacHomNay(luu, game.pokemon.CapNhat.ngayHomNay(), 908), "NHẮC: ghi đã nhắc hôm nay (" + luu + ")");
            game.pokemon.CapNhat.hienTai().deSau();
            kiem(!game.pokemon.CapNhat.dangMo() && game.render.Canvas.currentDialog == null, "NHẮC: Để sau -> chơi tiếp");
            kiem(game.pokemon.CapNhat.nhan(moi(nh0)) && !game.pokemon.CapNhat.dangCho(), "NHẮC: cùng bản, cùng ngày -> không nhắc lại");
            kiem(!game.pokemon.CapNhat.daNhacHomNay(luu, game.pokemon.CapNhat.ngayHomNay() + 1, 908), "NHẮC: sang ngày mới -> nhắc lại");
            kiem(!game.pokemon.CapNhat.daNhacHomNay(luu, game.pokemon.CapNhat.ngayHomNay(), 909), "NHẮC: có bản mới hơn -> nhắc lại");
            kiem(!game.pokemon.CapNhat.daNhacHomNay("rac", 1, 1) && !game.pokemon.CapNhat.daNhacHomNay(null, 1, 1), "NHẮC: RMS hỏng -> coi như chưa nhắc");
            // không tự cập nhật được (J2ME / thiếu SHA): mở link
            game.pokemon.CapNhat.datNen(null);
            javax.microedition.midlet.MIDlet.daMoLienKet.clear();
            game.render.screen.RMS.saveRMSString("htbbcapnhat", "0:0");
            game.pokemon.CapNhat.nhan(moi(nh0));
        });
        Thread.sleep(1600);
        ChupTrongGame.chay(() -> {
            game.pokemon.CapNhat.tick();
            kiem(game.pokemon.CapNhat.dangMo(), "NHẮC (không có bộ cập nhật): thẻ hiện");
            game.pokemon.CapNhat.hienTai().capNhatNgay();
            kiem(javax.microedition.midlet.MIDlet.daMoLienKet.contains("https://huyenthoaibaoboi.fun/tai/HTBB-0.9.8.apk"),
                    "không có bộ cập nhật -> mở link tệp " + javax.microedition.midlet.MIDlet.daMoLienKet);
            // 0.9.20: J2ME / giả lập (platformRequest không bắt thoát) -> thẻ GIỮ, chỉ cách nạp .jar mới
            game.pokemon.CapNhat h = game.pokemon.CapNhat.hienTai();
            kiem(game.pokemon.CapNhat.dangMo() && h.daMoLink != null && h.daMoLink.contains("nạp file .jar mới"),
                    "J2ME: đã mở link -> thẻ giữ + gợi ý giả lập (" + h.daMoLink + ")");
            String gy = h.goiYNen();
            kiem(gy != null && gy.contains("https://huyenthoaibaoboi.fun/tai/HTBB-0.9.8.apk") && gy.contains("nạp file .jar mới"),
                    "J2ME: thẻ ghi link tải + cách nạp .jar ở giả lập (" + gy + ")");
            h.deSau();
            game.pokemon.CapNhat.datNen(gia);
        });
        // các chuỗi phụ
        kiem("35,8 MB".equals(game.pokemon.CapNhat.mb(37538611L)), "mb(): " + game.pokemon.CapNhat.mb(37538611L));
        kiem(game.pokemon.CapNhat.phanTram(50, 200) == 25 && game.pokemon.CapNhat.phanTram(5, 0) == -1, "phanTram()");
    }

    /**
     * 0.9.20 đầu-cuối PC (chủ dự án: "cập nhật qua game là bản mới luôn"): gói -115 với link gốc tải http (sv.gocTaiCapNhat)
     * -> bộ cập nhật THẬT htbb.pc.CapNhatPc (Windows) -> tải từ máy chủ HTTP cục bộ -> kiểm SHA-256 -> chạy bộ cài /SILENT
     * + đóng game (bộ cài tự mở lại) - không mở trình duyệt. SHA lệch -> không chạy gì.
     */
    static void kiemDauCuoi(File tam) throws Exception {
        final List<List<String>> lenh = java.util.Collections.synchronizedList(new ArrayList<List<String>>());
        final int[] thoat = {0};
        htbb.pc.CapNhatPc.chayLenh = l -> lenh.add(new ArrayList<>(l));
        htbb.pc.CapNhatPc.thoat = () -> thoat[0]++;
        htbb.pc.CapNhatPc.choThoatMs = 10;
        htbb.pc.CapNhatPc.heDieuHanh = "windows";
        htbb.pc.CapNhatPc.thuMucTai = new File(tam, "dau_cuoi");
        final htbb.pc.CapNhatPc pc = new htbb.pc.CapNhatPc();
        final String exe = goc + "/tai/HTBB-PC-Setup-0.9.7.exe";
        final byte[] du = goi115(2, 907, "0.9.7", exe, shaTep, "60 MB", NHAT_KY, 907, 905);
        javax.microedition.midlet.MIDlet.daMoLienKet.clear();
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.pokemon.CapNhat.datNen(pc);
            kiem(game.pokemon.CapNhat.nhan(moi(du)), "đầu-cuối: gói -115 (link gốc tải http) đọc được");
            game.pokemon.CapNhat h = game.pokemon.CapNhat.hienTai();
            String gy = h.goiYNen();
            kiem(gy != null && gy.contains("tự cài và tự mở lại"), "đầu-cuối: thẻ nói rõ game tự cài + mở lại (" + gy + ")");
            h.capNhatNgay();
        });
        pc.cho(20000);
        Thread.sleep(300);
        File daTai = new File(new File(tam, "dau_cuoi"), "HTBB-PC-Setup-0.9.7.exe");
        kiem(pc.trangThai() == game.pokemon.CapNhatNen.XONG && java.util.Arrays.equals(Files.readAllBytes(daTai.toPath()), TEP),
                "đầu-cuối: tải bộ cài từ gốc tải + SHA-256 đúng (" + pc.trangThai() + " " + pc.loi() + ")");
        kiem(lenh.size() == 1 && lenh.get(0).get(0).equals(daTai.getAbsolutePath()) && lenh.get(0).contains("/SILENT"),
                "đầu-cuối: chạy bộ cài im lặng " + lenh);
        kiem(thoat[0] == 1, "đầu-cuối: đóng game để bộ cài thay tệp + tự mở lại");
        kiem(javax.microedition.midlet.MIDlet.daMoLienKet.isEmpty(), "đầu-cuối: KHÔNG mở trình duyệt / trang web " + javax.microedition.midlet.MIDlet.daMoLienKet);
        // SHA lệch (tệp trên gốc tải bị thay) -> lỗi, không chạy bộ cài, hộp có Thử lại / Mở trang tải
        lenh.clear();
        thoat[0] = 0;
        final byte[] sai = goi115(2, 907, "0.9.7", exe, sha(TEP_KHAC), "60 MB", NHAT_KY, 907, 905);
        final htbb.pc.CapNhatPc pc2 = new htbb.pc.CapNhatPc();
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.pokemon.CapNhat.datNen(pc2);
            game.pokemon.CapNhat.nhan(moi(sai));
            game.pokemon.CapNhat.hienTai().capNhatNgay();
        });
        pc2.cho(20000);
        Thread.sleep(200);
        kiem(pc2.trangThai() == game.pokemon.CapNhatNen.LOI && lenh.isEmpty() && thoat[0] == 0,
                "đầu-cuối: SHA-256 lệch -> LỖI, không chạy bộ cài (" + pc2.loi() + ")");
        ChupTrongGame.chay(() -> game.render.Canvas.endDlg());
        htbb.pc.CapNhatPc.heDieuHanh = null;
        htbb.pc.CapNhatPc.thuMucTai = null;
        htbb.pc.CapNhatPc.chayLenh = l -> { };
        htbb.pc.CapNhatPc.thoat = () -> { };
    }

    /** Vẽ mọi trạng thái hộp ở mọi cỡ chuẩn. */
    static void kiemVe(File ra) throws Exception {
        final NenGia gia = new NenGia();
        final Object[][] BO = {
            {"bat_buoc", 2, game.pokemon.CapNhatNen.CHUA, "Cần cập nhật để tiếp tục chơi", "Tải bản mới"},
            {"nhac", 1, game.pokemon.CapNhatNen.CHUA, "Đã có bản mới 0.9.8", "Cập nhật ngay"},
            {"dang_tai", 1, game.pokemon.CapNhatNen.DANG_TAI, "Đang tải bản 0.9.8", "Huỷ"},
            {"loi", 2, game.pokemon.CapNhatNen.LOI, "Không tải được bản mới", "Thử lại"},
            {"xong", 1, game.pokemon.CapNhatNen.XONG, "Đã tải xong bản 0.9.7", "Cài lại"},
        };
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            final String co = ChupTrongGame.TEN_CO[c];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            float s = NenPc.hienW / (float) NenPc.rong;
            int min = dpi > 0 ? (int) Math.ceil(44 * ((dpi / 160f) / s)) : (int) Math.ceil(32 / s);
            game.pokemon.VungCham.MIN = min;
            game.pokemon.Bang.anTrai = dpi > 0 ? (int) Math.ceil(100 / s) : 0;
            for (Object[] bo : BO) {
                final String ten = (String) bo[0];
                final int loai = (Integer) bo[1], st = (Integer) bo[2];
                ChupTrongGame.chay(() -> {
                    try {
                        ChupTrongGame.moMan("the_gioi");
                        game.render.Canvas.endDlg();
                        htbb.pc.ChuHD.ghi.clear();
                        game.pokemon.CapNhat.datNen(gia);
                        gia.st = game.pokemon.CapNhatNen.CHUA;
                        game.render.screen.RMS.saveRMSString("htbbcapnhat", "0:0");
                        game.pokemon.CapNhat.nhan(goi(goi115(loai, loai == 2 ? 907 : 908, loai == 2 ? "0.9.7" : "0.9.8",
                                "https://huyenthoaibaoboi.fun/tai/HTBB.apk", shaTep, "85,3 MB", NHAT_KY, loai == 2 ? 907 : 0, 905)));
                        game.pokemon.CapNhat h = game.pokemon.CapNhat.hienTai();
                        if (st != game.pokemon.CapNhatNen.CHUA) {
                            h.capNhatNgay();
                            gia.st = st;
                            gia.da = 42L * 1024 * 1024;
                            gia.tong = 100L * 1024 * 1024;
                        }
                        game.render.Canvas.currentDialog = h;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                ChupTrongGame.ve(W, H);
                Thread.sleep(200);   // qua hoạt cảnh mở
                BufferedImage anh = ChupTrongGame.ve(W, H);
                ImageIO.write(anh, "png", new File(ra, "cap_nhat_" + ten + "_" + co + ".png"));
                String nhan = ten + " " + co;
                int[] v = game.pokemon.CapNhat.VUA_VE;
                int L = game.pokemon.Bang.trai(), T = game.pokemon.Bang.tren(), R = game.pokemon.Bang.phai(), B = game.pokemon.Bang.duoi();
                kiem(v[2] > 0 && v[0] >= L && v[1] >= T && v[0] + v[2] <= R && v[1] + v[3] <= B,
                        nhan + ": hộp trong vùng an toàn " + java.util.Arrays.toString(v) + " / " + L + "," + T + "," + R + "," + B);
                kiem(Math.abs((v[0] + v[2] / 2) - (L + R) / 2) <= 2, nhan + ": hộp canh giữa");
                int[] chinh = vungTen(st == game.pokemon.CapNhatNen.DANG_TAI ? "cap-nhat-huy" : "cap-nhat-chinh");
                kiem(chinh != null && chinh[3] >= min && chinh[2] >= min, nhan + ": nút chính có vùng chạm >= " + min + " " + java.util.Arrays.toString(chinh));
                kiem(chinh != null && chinh[1] + chinh[3] <= v[1] + v[3] && chinh[1] >= v[1], nhan + ": nút chính nằm trong hộp");
                int[] x = vungTen("dong");
                if (loai == 2 || st == game.pokemon.CapNhatNen.DANG_TAI) {
                    kiem(x == null, nhan + ": KHÔNG có nút X (bắt buộc / đang tải)");
                } else {
                    kiem(x != null, nhan + ": có nút X (= Để sau)");
                }
                kiem(daVe(((String) bo[3]).substring(0, Math.min(14, ((String) bo[3]).length()))), nhan + ": đã vẽ '" + bo[3] + "'");
                kiem(daVe((String) bo[4]), nhan + ": đã vẽ nút '" + bo[4] + "'");
                if (st == game.pokemon.CapNhatNen.DANG_TAI) {
                    kiem(daVe("42% · 42,0 MB / 100,0 MB"), nhan + ": đã vẽ tiến độ 42%");
                }
                if (st == game.pokemon.CapNhatNen.CHUA) {
                    kiem(daVe("Có gì mới") && daVe("• Sửa lỗi câu cá"), nhan + ": đã vẽ Có gì mới + gạch đầu dòng");
                    kiem(daVe("Dung lượng: 85,3 MB"), nhan + ": đã vẽ dung lượng");
                }
                ChupTrongGame.chay(() -> {
                    game.render.Canvas.currentDialog = null;
                    gia.st = game.pokemon.CapNhatNen.CHUA;
                });
            }
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_cap_nhat");
        ra.mkdirs();
        File tam = Files.createTempDirectory("htbb-cap-nhat").toFile();
        batMayChu();
        try {
            kiemTaiVe(tam);
            kiemPc(tam);
            System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
            System.setProperty("microedition.platform", "HTBB-PC/kiem");
            htbb.pc.CaiDat.thuMucDuLieu();
            htbb.pc.CaiDat.nap();
            htbb.pc.ChuHD.bat = true;
            htbb.pc.GiaoDienHD.bat = true;
            htbb.pc.CaiDat.tatTieng = true;
            htbb.pc.LopPhu.nap();
            javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
            NenPc.tinhKichThuoc(1280, 720);
            ChupTrongGame.chay(() -> {
                try {
                    game.render.screen.TField.isQwerty = true;
                    javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
            Thread.sleep(2500);
            htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
            game.pokemon.VungCham.MIN = 16;
            kiem(game.pokemon.CapNhat.nen() instanceof htbb.pc.CapNhatPc, "bản PC tìm thấy bộ cập nhật htbb.pc.CapNhatPc");
            ChupTrongGame.chay(ChupTrongGame::giaLap);
            Thread.sleep(800);
            kiemGoi();
            kiemDauCuoi(tam);
            kiemVe(ra);
        } finally {
            http.stop(0);
        }
        System.out.println("KiemCapNhat: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
