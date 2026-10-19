package htbb.pc;

import java.awt.EventQueue;
import java.io.File;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * Điểm vào bản PC Huyền Thoại Bảo Bối (HTBB-PC.jar): mở cửa sổ, chạy MIDlet game.GameMidlet (code bản jar) trên
 * lớp tương thích MIDP cho Java SE.
 *
 *   java -jar HTBB-PC.jar [--may-chu host:cong] [--cua-so 1280x720] [--toan-man] [--du-lieu <thư mục>]
 *                         [--chu-anh] [--khong-am] [--chup <tệp.png> <giây>] [--fps]
 *
 * Máy chủ: --may-chu > biến HTBB_MAY_CHU > may_chu= trong cai_dat.properties > máy chủ gắn lúc build
 * (htbb-app.properties: bản thử 127.0.0.1:19129, bản phát hành MAY_CHU_CHINH / tools/phat-hanh/may_chu.conf).
 */
public final class Chay {
    private static MIDlet midlet;

    private Chay() {
    }

    private static void loi(String s) {
        System.err.println("HTBB: " + s);
        System.exit(2);
    }

    /** Tham số dòng lệnh (CapNhatPc mở lại AppImage mới với cùng tham số). */
    public static String[] thamSo = new String[0];

    public static void main(String[] args) throws Exception {
        thamSo = args.clone();
        // ảnh đọc trong bộ nhớ (không tạo tệp tạm -> không cần SecureRandom; wine thiếu API mạng mà nó dò)
        javax.imageio.ImageIO.setUseCache(false);
        String mayChu = null;
        String cuaSo = null;
        boolean toanMan = false;
        boolean chuAnh = false;
        boolean khongAm = false;
        String chup = null;
        int chupSau = 0;
        boolean fps = false;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            String tiep = i + 1 < args.length ? args[i + 1] : null;
            if (("--may-chu".equals(a) || "--server".equals(a)) && tiep != null) {
                mayChu = tiep;
                i++;
            } else if (a.startsWith("--may-chu=")) {
                mayChu = a.substring(10);
            } else if ("--cua-so".equals(a) && tiep != null) {
                cuaSo = tiep;
                i++;
            } else if ("--toan-man".equals(a) || "--fullscreen".equals(a)) {
                toanMan = true;
            } else if ("--du-lieu".equals(a) && tiep != null) {
                System.setProperty("htbb.duLieu", tiep);
                i++;
            } else if ("--chu-anh".equals(a)) {
                chuAnh = true;
            } else if ("--khong-am".equals(a)) {
                khongAm = true;
            } else if ("--fps".equals(a)) {
                fps = true;
            } else if ("--chup".equals(a) && tiep != null) {
                chup = tiep;
                i++;
                if (i + 1 < args.length) {
                    chupSau = Integer.parseInt(args[++i]);
                }
            } else if ("--help".equals(a) || "-h".equals(a)) {
                System.out.println("java -jar HTBB-PC.jar [--may-chu host:cong] [--cua-so 1280x720] [--toan-man] [--du-lieu <thu muc>] [--chu-anh] [--khong-am] [--fps]");
                return;
            } else {
                System.err.println("HTBB: bo qua tham so " + a);
            }
        }
        System.setProperty("sun.java2d.uiScale.enabled", System.getProperty("sun.java2d.uiScale.enabled", "false"));
        System.setProperty("awt.useSystemAAFontSettings", "on");
        CaiDat.thuMucDuLieu();
        NhatKy.mo();
        CaiDat.nap();
        if (mayChu == null) mayChu = System.getenv("HTBB_MAY_CHU");
        if ((mayChu == null || mayChu.length() == 0) && CaiDat.mayChu.length() > 0) mayChu = CaiDat.mayChu;
        if (mayChu != null && mayChu.length() > 0) {
            if (!mayChu.matches("^[^:\\s]+:\\d+$")) loi("--may-chu phai co dang host:cong (vd 127.0.0.1:19129)");
            MIDlet.GHI_DE.put("HTBB-May-Chu", mayChu);
        }
        if (cuaSo == null && CaiDat.lanDau && !java.awt.GraphicsEnvironment.isHeadless()) {
            // lần đầu: cửa sổ 16:9 lớn nhất vừa ~85% màn hình (màn 4K: 2560x1440 thay vì 1280x720 bé tí)
            try {
                java.awt.Rectangle man = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                int[][] co = {{3840, 2160}, {2560, 1440}, {1920, 1080}, {1600, 900}, {1280, 720}};
                for (int[] c : co) {
                    if (c[0] <= man.width * 0.85 && c[1] <= man.height * 0.85) {
                        CaiDat.cuaSoRong = c[0];
                        CaiDat.cuaSoCao = c[1];
                        break;
                    }
                }
            } catch (Throwable e) {
                // giữ 1280x720
            }
        }
        if (cuaSo != null) {
            String[] p = cuaSo.toLowerCase().split("x");
            CaiDat.cuaSoRong = Integer.parseInt(p[0].trim());
            CaiDat.cuaSoCao = Integer.parseInt(p[1].trim());
            CaiDat.toanMan = false;
        }
        if (toanMan) CaiDat.toanMan = true;
        if (fps) CaiDat.hienFps = true;
        ChuHD.bat = CaiDat.chuHD && !chuAnh;
        GiaoDienHD.bat = CaiDat.khungHD;
        if (khongAm) {
            CaiDat.tatTieng = true;
        }
        CaiDat.apAmThanh();
        NhatKy.ghi("HTBB PC: java " + System.getProperty("java.version") + " " + System.getProperty("os.name")
                + ", du lieu " + CaiDat.thuMucDuLieu() + ", may chu " + MIDlet.thuocTinh("HTBB-May-Chu")
                + ", phien ban " + MIDlet.thuocTinh("HTBB-Phien-Ban"));
        // thuộc tính máy J2ME mà code game đọc
        System.setProperty("microedition.platform", "HTBB-PC/" + System.getProperty("os.name", "?"));
        System.setProperty("microedition.configuration", "CLDC-1.1");
        System.setProperty("microedition.profiles", "MIDP-2.0");
        System.setProperty("microedition.locale", "vi-VN");
        System.setProperty("microedition.encoding", "UTF-8");
        System.setProperty("microedition.media.version", "1.1");
        System.setProperty("supports.mixing", "true");
        MIDlet.khiThoat = new Runnable() {
            public void run() {
                thoat();
            }
        };
        if (!ManHinh.coManHinh()) loi("can man hinh (khong chay duoc o che do headless)");
        NenPc.tinhKichThuoc(CaiDat.cuaSoRong, CaiDat.cuaSoCao);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                ManHinh m = new ManHinh("Huyền Thoại Bảo Bối", ManHinh.napBieuTuong());
                m.hien();
            }
        });
        // MIDlet tạo trên luồng sự kiện (như máy J2ME / bản APK)
        NenPc.luong().them(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;  // bàn phím thật: gõ thẳng chữ
                    midlet = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(midlet);
                } catch (Throwable e) {
                    NhatKy.loi("khong chay duoc game", e);
                }
            }
        });
        final String lenh = System.getProperty("htbb.lenh");
        if (lenh != null) {
            // kiểm thử tự động: tệp lệnh "chup <tệp.png>" (mỗi dòng), đọc xong thì xoá
            Thread t = new Thread(new Runnable() {
                public void run() {
                    File f = new File(lenh);
                    while (true) {
                        try {
                            Thread.sleep(200);
                            if (!f.isFile()) continue;
                            java.util.List<String> ds = java.nio.file.Files.readAllLines(f.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                            f.delete();
                            for (String d : ds) {
                                d = d.trim();
                                ManHinh m = ManHinh.me;
                                if (d.startsWith("kiem ")) {
                                    KiemChong.yeuCau(d.substring(5).trim());
                                    while (KiemChong.dangCho()) Thread.sleep(50);
                                }
                                if (d.startsWith("chup ") && m != null) {
                                    m.chupVao = d.substring(5).trim();
                                    while (m.chupVao != null) Thread.sleep(50);
                                }
                            }
                        } catch (Throwable e) {
                            NhatKy.loi("tep lenh", e);
                        }
                    }
                }
            }, "HTBB-lenh");
            t.setDaemon(true);
            t.start();
        }
        if (chup != null) {
            final String tep = chup;
            final int sau = chupSau;
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(sau * 1000L);
                    } catch (InterruptedException e) {
                        return;
                    }
                    ManHinh m = ManHinh.me;
                    if (m != null) m.chupVao = new File(tep).getAbsolutePath();
                }
            }, "HTBB-chup");
            t.setDaemon(true);
            t.start();
        }
    }

    /** Đóng game: lưu cài đặt, đóng âm thanh, thoát. */
    public static void thoat() {
        try {
            CaiDat.luu();
        } catch (Throwable e) {
            // bỏ qua
        }
        try {
            javax.microedition.media.Manager.dongHet();
        } catch (Throwable e) {
            // bỏ qua
        }
        try {
            if (midlet != null) MIDlet.huy(midlet);
        } catch (Throwable e) {
            // bỏ qua
        }
        Runtime.getRuntime().halt(0);
    }
}
