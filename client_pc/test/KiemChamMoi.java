import java.io.File;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM "DẤU DẪN ĐƯỜNG" (0.9.6, game.pokemon.ChamMoi): với MỖI nguồn chấm đỏ, đặt dữ liệu giả như máy chủ gửi, rồi đi
 * từ HUD xuống đúng thứ cần làm - ở mỗi cấp (nút HUD -> ô Menu -> tab -> chip -> hàng / nút) phải có chấm đỏ VẼ THẬT
 * nằm trong vùng chạm của điều khiển đó (VungCham, tên vùng). Xoá nguồn (đọc thư, cộng điểm...) thì chấm cấp cha tắt.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemChamMoi.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemChamMoi <thư mục>
 * Mã thoát 1 = thiếu chấm ở một cấp.
 */
public class KiemChamMoi {
    static int dat, hong;
    static File ra;
    static int soAnh;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    /** Vẽ khung hiện tại, trả true nếu có chấm đỏ vẽ trong vùng chạm tên `vung` (hoặc tiền tố khi vung kết thúc bằng '*'). */
    static boolean coChamTrong(String nhan, String vung) throws Exception {
        final int truoc0[] = {0};
        ChupTrongGame.chay(() -> truoc0[0] = game.pokemon.ChuDe.demCham);
        ChupTrongGame.ve(1280, 720);
        final int truoc = game.pokemon.ChuDe.demCham;
        java.awt.image.BufferedImage a = ChupTrongGame.ve(1280, 720);
        final int sau = game.pokemon.ChuDe.demCham;
        if (ra != null && soAnh < 40) {
            javax.imageio.ImageIO.write(a, "png", new File(ra, String.format("%02d_%s.png", soAnh++, nhan.replace(' ', '_').replace('/', '-'))));
        }
        boolean tiento = vung.endsWith("*");
        String goc = tiento ? vung.substring(0, vung.length() - 1) : vung;
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            String t = game.pokemon.VungCham.ten(i);
            if (t == null || !(tiento ? t.startsWith(goc) : t.equals(goc))) continue;
            int[] r = game.pokemon.VungCham.vungVe(i);
            for (int c = truoc; c < sau && c - truoc < 64; c++) {
                int x = game.pokemon.ChuDe.CHAM_X[c & 63], y = game.pokemon.ChuDe.CHAM_Y[c & 63];
                if (x >= r[0] - 8 && x <= r[0] + r[2] + 8 && y >= r[1] - 8 && y <= r[1] + r[3] + 8) {
                    return true;
                }
            }
        }
        return false;
    }

    static void buoc(String nguon, String cap, String vung) throws Exception {
        kiem(coChamTrong(nguon + " " + cap, vung), nguon + ": cấp '" + cap + "' có chấm trong vùng " + vung);
    }

    static void tatHet() {
        game.model.MainChar m = game.render.Canvas.gameScr.mainChar;
        m.skillPointLeft = 0;
        m.basePointLeft = 0;
        game.render.screen.MessageScr.nMSG = 0;
        game.pokemon.ChamMoi.daXemChieuMoi();
        game.pokemon.DanhHieuKho.soMoi = 0;
    }

    static void mo(String man) throws Exception {
        ChupTrongGame.chay(() -> ChupTrongGame.moMan(man));
        Thread.sleep(150);
        ChupTrongGame.chay(() -> {
            Object d = game.render.Canvas.currentDialog;
            if (d instanceof game.render.screen.MsgDlg || d instanceof game.pokemon.HopThongBao) game.render.Canvas.endDlg();
        });
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "kiem_cham_moi");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        game.pokemon.VungCham.MIN = 16;
        game.pokemon.VungCham.CACH = 4;

        // THƯ: HUD Menu -> Thư/Quà -> hàng Hộp thư
        ChupTrongGame.chay(() -> {
            tatHet();
            game.render.screen.MessageScr.nMSG = 2;
        });
        mo("the_gioi");
        buoc("thư", "HUD Menu", "hud-tren-3");
        mo("menu");
        buoc("thư", "Menu Thư/Quà", "menu-Thư/Quà");
        ChupTrongGame.chay(() -> {
            try {
                java.lang.reflect.Method m = game.pokemon.ManMenu.class.getDeclaredMethod("lam", int.class);
                m.setAccessible(true);
                m.invoke(null, 6);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        buoc("thư", "hàng Hộp thư", "lenh-0");
        // đọc hết thư -> cấp cha tắt
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.screen.MessageScr.nMSG = 0;
        });
        mo("the_gioi");
        kiem(!coChamTrong("thu_da_doc", "hud-tren-3"), "thư: đọc hết thư -> nút Menu HUD hết chấm");

        // ĐIỂM TIỀM NĂNG: HUD Menu -> Pokémon -> tab Trang bị -> nút Tiềm năng
        ChupTrongGame.chay(() -> {
            tatHet();
            game.render.Canvas.gameScr.mainChar.basePointLeft = 3;
        });
        mo("the_gioi");
        buoc("điểm tiềm năng", "HUD Menu", "hud-tren-3");
        mo("menu");
        buoc("điểm tiềm năng", "Menu Pokémon", "menu-Pokémon");
        mo("nv_trang_bi");
        buoc("điểm tiềm năng", "tab Trang bị", "tab-0");
        buoc("điểm tiềm năng", "nút Tiềm năng", "tiem-nang");

        // ĐIỂM KỸ NĂNG: HUD Menu -> Pokémon -> tab Kỹ năng -> chip Chiêu của Pokémon -> hàng chiêu
        ChupTrongGame.chay(() -> {
            tatHet();
            game.render.Canvas.gameScr.mainChar.skillPointLeft = 2;
        });
        mo("the_gioi");
        buoc("điểm kỹ năng", "HUD Menu", "hud-tren-3");
        mo("menu");
        buoc("điểm kỹ năng", "Menu Pokémon", "menu-Pokémon");
        mo("nv_ky_nang");
        buoc("điểm kỹ năng", "tab Kỹ năng", "tab-2");
        buoc("điểm kỹ năng", "chip Chiêu của Pokémon", "chip-0");
        buoc("điểm kỹ năng", "hàng chiêu", "chieu-*");

        // CHIÊU MỚI: HUD Menu -> Pokémon -> tab Kỹ năng -> chip Học chiêu mới -> hàng chiêu mới
        ChupTrongGame.chay(() -> {
            tatHet();
            try {
                ChupTrongGame.datTinh(game.pokemon.ChamMoi.class, "daXemChieu", Integer.valueOf(-1));
                ChupTrongGame.datTinh(game.pokemon.ChamMoi.class, "daXemLop", Integer.valueOf(-1));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        boolean coMoi = game.pokemon.ChamMoi.co(game.pokemon.ChamMoi.CHIEU_MOI);
        kiem(coMoi, "chiêu mới: dữ liệu giả có chiêu NPC dạy");
        mo("the_gioi");
        buoc("chiêu mới", "HUD Menu", "hud-tren-3");
        mo("menu");
        buoc("chiêu mới", "Menu Pokémon", "menu-Pokémon");
        mo("nv_ky_nang");
        buoc("chiêu mới", "tab Kỹ năng", "tab-2");
        buoc("chiêu mới", "chip Học chiêu mới", "chip-1");
        mo("ky_nang_hoc");
        buoc("chiêu mới", "hàng chiêu mới", "hoc-*");
        // rời danh sách = đã xem -> Menu Pokémon hết chấm
        ChupTrongGame.chay(() -> ChupTrongGame.dat(game.pokemon.ManNhanVat.gI(), "hocMoi", Boolean.FALSE));
        ChupTrongGame.ve(1280, 720);
        mo("menu");
        kiem(!coChamTrong("chieu_da_xem", "menu-Pokémon"), "chiêu mới: đã xem danh sách -> ô Pokémon ở Menu hết chấm");

        // NHIỆM VỤ: HUD Nhiệm vụ -> tab -> hàng
        ChupTrongGame.chay(KiemChamMoi::tatHet);
        boolean nhan = game.pokemon.ChamMoi.co(game.pokemon.ChamMoi.NV_NHAN), tra = game.pokemon.ChamMoi.co(game.pokemon.ChamMoi.NV_TRA);
        kiem(nhan || tra, "nhiệm vụ: dữ liệu giả có nhiệm vụ mới / đủ để trả");
        mo("the_gioi");
        buoc("nhiệm vụ", "HUD Nhiệm vụ", "hud-tren-1");
        for (int t = 0; t < 2; t++) {
            if (!(t == 0 ? tra : nhan)) continue;
            final int tt = t;
            mo("nhiem_vu");
            ChupTrongGame.chay(() -> {
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "tab", Integer.valueOf(tt));
                ChupTrongGame.dat(game.pokemon.ManNhiemVu.gI(), "chon", null);
            });
            String ng = t == 0 ? "nhiệm vụ đủ để trả" : "nhiệm vụ mới";
            buoc(ng, "tab " + (t == 0 ? "Đang làm" : "Có thể nhận"), "tab-" + t);
            buoc(ng, "hàng nhiệm vụ", "nv-*");
        }
        // DANH HIỆU MỚI (0.9.7): HUD Menu -> Pokémon -> tab Trang bị -> viên Danh hiệu -> hàng danh hiệu mới
        ChupTrongGame.chay(() -> {
            tatHet();
            ChupTrongGame.giaLapDanhHieu();   // bảng giả: 9720 "Master Ball mùa này" mới
            game.pokemon.DanhHieuKho.soMoi = 1;
        });
        mo("the_gioi");
        buoc("danh hiệu mới", "HUD Menu", "hud-tren-3");
        mo("menu");
        buoc("danh hiệu mới", "Menu Pokémon", "menu-Pokémon");
        mo("nv_trang_bi");
        buoc("danh hiệu mới", "tab Trang bị", "tab-0");
        buoc("danh hiệu mới", "viên Danh hiệu", "danh-hieu");
        ChupTrongGame.chay(() -> {
            game.pokemon.ManDanhHieu.moTab(0);
            game.pokemon.DanhHieuKho.soMoi = 1;   // mo() gửi "đã xem" (không máy chủ ở đây): giữ chấm để vẽ hàng
        });
        buoc("danh hiệu mới", "hàng danh hiệu mới", "danh-hieu-9720");
        ChupTrongGame.chay(() -> game.pokemon.DanhHieuKho.daXem());
        mo("menu");
        kiem(!coChamTrong("danh_hieu_da_xem", "menu-Pokémon"), "danh hiệu mới: đã mở bảng -> ô Pokémon ở Menu hết chấm");
        System.out.println("KiemChamMoi: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
