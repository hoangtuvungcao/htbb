import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * 31/10/2026 (báo lỗi 0.9.24 J2ME giả lập trên trình duyệt, có chạm, ~500 px: "sổ nhiệm vụ TRỐNG" dù đang có cửa ải + nhiệm vụ
 * ngày / thành tựu): đăng nhập MÁY CHỦ THẬT (bộ local), đọc gói -64 thật, mở sổ nhiệm vụ từ MỌI lối vào (thẻ nhiệm vụ HUD,
 * Menu › Nhiệm vụ › Sổ nhiệm vụ bằng chạm / bằng phím) và chụp; HỎNG khi đang có nhiệm vụ mà sổ vẽ trống.
 *
 *   java -Djava.awt.headless=true -Dhtbb.mayChu=$PWD/server [-Dso.j2me=1] [-Dhtbb.khongCham=true] [-Dso.co=500x600]
 *       -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemSoNhiemVuThat <cổng> <thư mục ra> <tài khoản> [tên tạo mới]
 */
public class KiemSoNhiemVuThat {
    static int hong;
    static File RA;

    static void kiem(boolean ok, String ten) {
        if (!ok) {
            hong++;
            System.out.println("HONG: " + ten);
        } else {
            System.out.println("ok   " + ten);
        }
    }

    static void chup(String ten) throws Exception {
        ImageIO.write(ChupTrongGame.ve(NenPc.thatW, NenPc.thatH), "png", new File(RA, ten + ".png"));
    }

    static String ghiQuest(game.model.Quest q) {
        return q == null ? "null" : q.idQuest + "/" + q.name + " st=" + q.stateQuest + " type=" + q.type + " ms=" + q.main_sub + " co=" + q.co;
    }

    /** Chữ đang vẽ (một khung), nối bằng |. */
    static String chuDangVe() throws Exception {
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        ChupTrongGame.ve(NenPc.thatW, NenPc.thatH);
        game.pokemon.GhiChu.bat = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            if (game.pokemon.GhiChu.CS[i] != null) sb.append('|').append(game.pokemon.GhiChu.CS[i]);
        }
        return sb.toString();
    }

    static final String[] TRONG = {"Chưa nhận nhiệm vụ", "Chưa có nhiệm vụ"};

    /** Sổ đang mở: có nhiệm vụ đang làm thì phải vẽ ít nhất một tên nhiệm vụ và không vẽ dòng "trống". */
    static void kiemSo(String loi) throws Exception {
        final StringBuilder ten = new StringBuilder();
        final int[] n = {0};
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.model.Quest[] qs = {game.render.screen.GameScr.mainQuest, game.render.screen.GameScr.subQuest, game.render.screen.GameScr.clanQuest};
                for (game.model.Quest q : qs) {
                    if (q != null && q.stateQuest != 3) {
                        n[0]++;
                        ten.append(q.name).append('\n');
                    }
                }
            }
        });
        String chu = chuDangVe();
        System.out.println("[" + loi + "] man=" + KiemTaiThat.trangThai() + " chu=" + (chu.length() > 700 ? chu.substring(0, 700) : chu));
        if (n[0] == 0) {
            System.out.println("NHAC: [" + loi + "] may chu khong gui nhiem vu dang lam nao (so trong la dung)");
            return;
        }
        boolean coTen = false;
        for (String t : ten.toString().split("\n")) {
            String dau = t.length() > 10 ? t.substring(0, 10) : t;
            if (dau.length() > 0 && chu.contains(dau)) coTen = true;
        }
        boolean trong = false;
        for (String t : TRONG) if (chu.contains(t)) trong = true;
        kiem(coTen && !trong, "[" + loi + "] so nhiem vu hien nhiem vu dang lam (" + ten.toString().replace('\n', ';') + ")");
    }

    public static void main(String[] a) throws Exception {
        final int cong = Integer.parseInt(a[0]);
        RA = new File(a[1]);
        RA.mkdirs();
        final String tk = a[2];
        final String tenMoi = a.length > 3 ? a[3] : tk;
        KiemHanhTrinhUI.RA = RA;
        KiemHanhTrinhUI.BAO = new PrintWriter(new FileWriter(new File(RA, "bao_cao.tsv")));
        System.setProperty("java.awt.headless", "true");
        System.setProperty("htbb.duLieu", new File(RA, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "Nokia6233/05.60");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:" + cong);
        final boolean j2me = System.getProperty("so.j2me") != null;
        String[] co = System.getProperty("so.co", "500x600").split("x");
        final int W = Integer.parseInt(co[0]), H = Integer.parseInt(co[1]);
        if (j2me) {
            ChupTrongGame.datCoJ2me(W, H);
        } else {
            NenPc.tinhKichThuoc(W, H);
        }
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet.batDau((MIDlet) Class.forName("game.GameMidlet").newInstance());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        if (j2me) {
            javax.microedition.lcdui.NghiemMidp.bat = true;
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
            try {
                Class.forName("htbb.pc.LopPhu");   // khối static của LopPhu cài PhongVe.bo - chạy trước rồi mới bỏ
            } catch (Throwable e) {
                // không có
            }
            game.pokemon.PhongVe.bo = null;   // máy J2ME thật không có bộ vẽ chữ vector (TienTe.fontSo: số tiền bằng arialFont)
        }
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
            }
        });
        // bơm khung như máy thật
        Thread bom = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    KiemHanhTrinhUI.ngu(50);
                    try {
                        NenPc.luong().them(new Runnable() {
                            public void run() {
                                ChupTrongGame.veThang();
                            }
                        });
                    } catch (Throwable e) {
                        // bỏ
                    }
                }
            }
        });
        bom.setDaemon(true);
        bom.start();
        for (int i = 0; i < 300 && !(game.render.Canvas.currentScreen instanceof game.render.screen.LoginScr); i++) {
            if (i == 30) KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.render.Canvas.loginScr.switchToMe();
                }
            });
            KiemHanhTrinhUI.ngu(100);
        }
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.screen.ServerListScr.address = new String[]{"127.0.0.1"};
                game.render.screen.ServerListScr.port = new short[]{(short) cong};
                game.render.screen.ServerListScr.index = 0;
                game.render.Canvas.loginScr.tfUser.setText(tk);
                game.render.Canvas.loginScr.tfPass.setText("123456");
            }
        });
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                try {
                    java.lang.reflect.Method m = game.render.screen.LoginScr.class.getDeclaredMethod("doLogin");
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.loginScr);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
            public boolean dung() {
                Object m = game.render.Canvas.currentScreen;
                return m instanceof game.render.screen.CreateCharScr || m instanceof game.render.screen.CharSelectScr
                        || m == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao;
            }
        }, 30000);
        if (game.render.Canvas.currentScreen instanceof game.render.screen.CreateCharScr) {
            KiemHanhTrinhUI.cham("khoi-dau-0");
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.render.screen.CreateCharScr.gI().tfChat.setText(tenMoi.length() > 10 ? tenMoi.substring(0, 10) : tenMoi);
                }
            });
            KiemHanhTrinhUI.cham("tao-1");
        } else if (game.render.Canvas.currentScreen instanceof game.render.screen.CharSelectScr) {
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    try {
                        java.lang.reflect.Field f = game.render.screen.CharSelectScr.class.getDeclaredField("cmdSelect");
                        f.setAccessible(true);
                        ((game.model.Command) f.get(game.render.screen.CharSelectScr.gI())).action.perform();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        boolean vao = KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentScreen == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao
                        && game.render.Canvas.gameScr.mainChar != null;
            }
        }, 60000);
        kiem(vao, "vao game " + tk + " (" + KiemTaiThat.trangThai() + ")");
        if (!vao) {
            chup("khong_vao_duoc");
            System.out.println("KiemSoNhiemVuThat: hong " + hong);
            System.exit(1);
        }
        KiemHanhTrinhUI.ngu(5000);
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.endDlg();
                game.render.Canvas.menu.showMenu = false;
            }
        });
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                System.out.println("QUEST main=" + ghiQuest(game.render.screen.GameScr.mainQuest) + " | sub=" + ghiQuest(game.render.screen.GameScr.subQuest)
                        + " | clan=" + ghiQuest(game.render.screen.GameScr.clanQuest) + " | coTheNhan=" + game.render.screen.GameScr.allQuestCanReceive.size()
                        + " | coMucTieuNv=" + game.render.screen.GameScr.coMucTieuNv + " | cham=" + game.pokemon.MenuNguCanh.bat()
                        + " | j2me=" + (game.pokemon.ChuUI.bo == null) + " | " + game.render.Canvas.w + "x" + game.render.Canvas.h);
            }
        });
        chup("01_the_gioi");
        // 1) thẻ nhiệm vụ HUD (chạm) / gọi thẳng ManNhiemVu.mo như thẻ
        boolean coThe = KiemHanhTrinhUI.coVung("nv-the");
        System.out.println("vung nv-the: " + coThe);
        if (coThe) {
            KiemHanhTrinhUI.cham("nv-the");
        } else {
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.pokemon.ManNhiemVu.mo();
                }
            });
        }
        KiemHanhTrinhUI.ngu(600);
        chup("02_so_tu_the");
        kiemSo("the HUD / ManNhiemVu");
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.switchToMe();
            }
        });
        KiemHanhTrinhUI.ngu(400);
        // 2) Menu (phím mềm trái) › Nhiệm vụ › Sổ nhiệm vụ
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.left.action.perform();
            }
        });
        KiemHanhTrinhUI.ngu(600);
        chup("03_menu");
        if (game.pokemon.MenuNguCanh.bat()) {
            KiemHanhTrinhUI.chamTheoChuDung("Nhiệm vụ");
            KiemHanhTrinhUI.ngu(500);
            chup("04_menu_nhiem_vu");
            KiemHanhTrinhUI.chamTheoChu("Sổ nhiệm vụ");
        } else {
            // phím: xuống tới hàng Nhiệm vụ (hàng gốc 4) rồi chọn, mục 0 = Sổ nhiệm vụ
            final game.render.screen.PauseMenu pm = game.render.screen.PauseMenu.gI();
            for (int i = 0; i < 12; i++) {
                {
                    final int[] gi = {-1};
                    KiemTaiThat.tren(new Runnable() {
                        public void run() {
                            try {
                                java.lang.reflect.Field fh = game.render.screen.PauseMenu.class.getDeclaredField("hien");
                                fh.setAccessible(true);
                                int[] h = (int[]) fh.get(pm);
                                java.lang.reflect.Field fs = game.render.screen.PauseMenu.class.getDeclaredField("selected");
                                fs.setAccessible(true);
                                int s = fs.getInt(pm);
                                gi[0] = s >= 0 && s < h.length ? h[s] : -1;
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    if (gi[0] == 4) break;
                }
                KiemHanhTrinhUI.phim(-2, "xuong");
            }
            KiemHanhTrinhUI.phim(-5, "chon");
            KiemHanhTrinhUI.ngu(400);
            chup("04_menu_nhiem_vu");
            KiemHanhTrinhUI.phim(-5, "chon so nhiem vu");
        }
        KiemHanhTrinhUI.ngu(800);
        chup("05_so_tu_menu");
        kiemSo("Menu > Nhiem vu > So nhiem vu");
        KiemHanhTrinhUI.BAO.close();
        System.out.println("KiemSoNhiemVuThat: hong " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }
}
