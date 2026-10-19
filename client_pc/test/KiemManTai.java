import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM MÀN TẢI DUY NHẤT (0.9.5, game.pokemon.ManTai): đi từng bước đăng nhập -> thế giới, đổi bản đồ, tự kết nối lại
 * bằng đúng các móc của game (LoginScr / GameScr.onConnectOK / CharSelectScr chọn / ChangScr / GameScr.switchToMe /
 * TuNoiLai), chụp từng khung và kiểm: mỗi lúc chỉ MỘT lớp màn tải (hộp chặn HOẶC màn chuyển bản đồ, không cả hai),
 * hộp chờ cũ không chồng lên (Canvas.startWaitDlg bị chặn), bước chỉ tăng, vào thế giới thì hết màn tải.
 *
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemManTai <thư mục ảnh>
 */
public class KiemManTai {
    static int dat, hong, khung;
    static File ra;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static int lop() {
        int n = 0;
        if (game.render.Canvas.currentDialog == game.pokemon.ManTai.HOP) n++;
        if (game.render.Canvas.currentScreen instanceof game.render.screen.ChangScr) n++;
        if (game.render.Canvas.currentScreen instanceof game.render.screen.CharSelectScr && game.pokemon.TuNoiLai.dangNoi) n++;
        return n;
    }

    static void chup(String ten, int lopMongDoi) throws Exception {
        BufferedImage a = ChupTrongGame.ve(NenPc.cuaSoW, NenPc.cuaSoH);
        khung++;
        ImageIO.write(a, "png", new File(ra, String.format("%02d_%s.png", khung, ten)));
        kiem(lop() == lopMongDoi, ten + ": " + lop() + " lớp màn tải, mong " + lopMongDoi);
        Object d = game.render.Canvas.currentDialog;
        kiem(d != game.render.Canvas.msgdlg, ten + ": hộp chờ cũ (msgdlg) không được hiện");
    }

    static void tren(final Runnable r) throws Exception {
        ChupTrongGame.chay(r);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "kiem_man_tai");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        tren(new Runnable() {
            public void run() {
                try {
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        NenPc.tinhKichThuoc(1280, 720);
        // khởi động (Canvas.isLoad): cùng thành phần
        tren(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                game.render.Canvas.isLoad = true;
            }
        });
        chup("khoi_dong", 0);
        Thread.sleep(2500);
        tren(new Runnable() {
            public void run() {
                game.render.Canvas.isLoad = false;
                game.render.Canvas.endDlg();
            }
        });
        final int[] buoc = new int[1];
        // 1) đăng nhập: LoginScr.doLogin -> kết nối
        tren(new Runnable() {
            public void run() {
                game.pokemon.ManTai.batDau(game.pokemon.ManTai.KET_NOI, null);
                game.render.Canvas.startWaitDlg("Đang kết nối", true);   // hộp chờ cũ: phải bị chặn
            }
        });
        Thread.sleep(300);
        chup("ket_noi", 1);
        kiem(game.pokemon.ManTai.buoc == game.pokemon.ManTai.KET_NOI, "bước kết nối");
        // 2) GameScr.onConnectOK -> đăng nhập
        tren(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.onConnectOK();
            }
        });
        Thread.sleep(300);
        chup("dang_nhap", 1);
        kiem(game.pokemon.ManTai.buoc == game.pokemon.ManTai.DANG_NHAP, "bước đăng nhập");
        // 3) chọn Pokémon (CharSelectScr "Chọn"): tải Pokémon + màn chuyển bản đồ, hộp chặn bị gỡ (một lớp)
        tren(new Runnable() {
            public void run() {
                game.pokemon.ManTai.batDau(game.pokemon.ManTai.NHAN_VAT, null);
                game.render.screen.ChangScr.gI().switchToMe();
                game.render.Canvas.startWaitDlg("Xin chờ..", true);
                game.render.Canvas.startWaitDlg();
            }
        });
        Thread.sleep(300);
        chup("tai_pokemon", 1);
        kiem(game.pokemon.ManTai.buoc == game.pokemon.ManTai.NHAN_VAT, "bước tải Pokémon");
        // 4) server gửi thông tin nhân vật (GameScr.onMainCharInfo -> bước tải bản đồ)
        tren(new Runnable() {
            public void run() {
                buoc[0] = game.pokemon.ManTai.buoc;
                game.pokemon.ManTai.toi(game.pokemon.ManTai.BAN_DO);
            }
        });
        Thread.sleep(300);
        chup("tai_ban_do", 1);
        kiem(game.pokemon.ManTai.buoc == game.pokemon.ManTai.BAN_DO && buoc[0] < game.pokemon.ManTai.BAN_DO, "bước tải bản đồ (tăng)");
        // 5) vào thế giới
        tren(new Runnable() {
            public void run() {
                try {
                    game.model.Tilemap.loadMap(0, null);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                game.render.Canvas.gameScr.switchToMe();
            }
        });
        Thread.sleep(300);
        chup("the_gioi", 0);
        kiem(!game.pokemon.ManTai.dangVao, "vào thế giới: hết đường vào");
        // 6) đổi bản đồ trong game (cổng / dịch chuyển): cùng thành phần + tên bản đồ đích
        tren(new Runnable() {
            public void run() {
                game.render.screen.ChangScr.gI().switchToMe();
                game.render.screen.GameScr.nameMap = "Rừng Viridian";
                game.render.Canvas.startWaitDlg();
            }
        });
        Thread.sleep(300);
        chup("doi_ban_do", 1);
        kiem("Rừng Viridian".equals(game.pokemon.ManTai.tenMap), "tên bản đồ đích: " + game.pokemon.ManTai.tenMap);
        tren(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.switchToMe();
            }
        });
        Thread.sleep(200);
        chup("the_gioi_2", 0);
        // 7) mất kết nối -> tự kết nối lại (TuNoiLai): hộp chặn có lần thử, màn chọn Pokémon không lộ ra
        tren(new Runnable() {
            public void run() {
                game.pokemon.TuNoiLai.dangNoi = true;
                game.pokemon.ManTai.batDau(game.pokemon.ManTai.KET_NOI, "Mất kết nối · đang thử lại 1/3");
            }
        });
        Thread.sleep(300);
        chup("ket_noi_lai", 1);
        kiem(game.pokemon.ManTai.phu != null && game.pokemon.ManTai.phu.indexOf("1/3") > 0, "dòng lần thử");
        tren(new Runnable() {
            public void run() {
                game.pokemon.ManTai.batDau(game.pokemon.ManTai.KET_NOI, null);   // LoginScr.doLogin của lần nối
                game.render.Canvas.gameScr.onConnectOK();
            }
        });
        Thread.sleep(200);
        chup("ket_noi_lai_dang_nhap", 1);
        kiem(game.pokemon.ManTai.phu != null, "giữ dòng lần thử khi đăng nhập lại");
        tren(new Runnable() {
            public void run() {
                game.pokemon.ManTai.batDau(game.pokemon.ManTai.NHAN_VAT, null);
                game.render.screen.ChangScr.gI().switchToMe();
            }
        });
        Thread.sleep(200);
        chup("ket_noi_lai_tai", 1);
        tren(new Runnable() {
            public void run() {
                game.pokemon.TuNoiLai.dangNoi = false;
                game.render.Canvas.gameScr.switchToMe();
            }
        });
        Thread.sleep(200);
        chup("ket_noi_lai_xong", 0);
        System.out.println("KiemManTai: " + khung + " khung, dat " + dat + ", hong " + hong + " -> " + ra.getAbsolutePath());
        System.exit(hong > 0 ? 1 : 0);
    }
}
