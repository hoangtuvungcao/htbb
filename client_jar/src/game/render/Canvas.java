package game.render;

import game.model.FilePack;
import game.model.FrameImage;
import game.model.IAction;
import game.model.InputDlg;
import game.model.Main;
import game.model.Position;
import game.model.Tilemap;
import game.render.screen.AutoGuard;
import game.render.screen.BangAuto;
import game.render.screen.CreateCharScr;
import game.render.screen.Dialog;
import game.render.screen.GameScr;
import game.render.screen.ListScr;
import game.render.screen.LoginScr;
import game.render.screen.Menu;
import game.render.screen.MessageScr;
import game.render.screen.MsgDlg;
import game.render.screen.PauseMenu;
import game.render.screen.RMS;
import game.render.screen.Screen;
import game.render.screen.ServerListScr;
import game.render.screen.ShopOnline;
import game.render.screen.VirTualKeyB;
import game.render.screen.WindowInfoScr;
import java.io.IOException;
import java.util.Random;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import network.Session_ME;

public class Canvas extends javax.microedition.lcdui.Canvas implements Runnable, CommandListener {
    public static Canvas instance;
    public static boolean bRun;
    public static boolean isLoadTileServer;
    public static boolean isPointerDown;
    public static boolean isPointerClick;
    public static boolean isPointerDownNj;
    public static boolean isPointerJustRelease;
    /**
     * 0.9.6.1: ngón / chuột ĐANG GIỮ thật (nhấn -> thả). Khác isPointerDown: màn / bảng không xoá cờ này mỗi nhịp,
     * nên điều khiển kéo (thanh trượt Bang.thanhTruot) theo được ngón suốt lúc kéo.
     */
    public static boolean dangGiu;
    public static int px;
    public static int py;
    public static int isCheat;
    public static int gameTick;
    public static int w;
    public static int h;
    public static int hw;
    public static int hh;
    public static boolean isMoto;
    public static Screen currentScreen;
    public static boolean isSmallScreen;
    public static GameScr gameScr;
    public static LoginScr loginScr;
    public static Dialog currentDialog;
    public static VirTualKeyB virKey;
    public static InputDlg inputDlg;
    public static boolean isBB;
    public static Image imgLogo;
    public static Command cmdMenu;
    public static int count0;
    public static int pyLast;
    public static int pxLast;
    public static boolean isVirHorizontal;
    public static boolean isPointerRelease;
    public static BangAuto autoTab;
    public static ShopOnline shop;
    public static boolean isSmartPhone;
    int isClearAll;
    public static game.model.Command cmdNgatKetNoi;
    public static boolean[] keyPressed = new boolean[14];
    public static boolean[] keyReleased = new boolean[14];
    public static boolean[] keyHold = new boolean[14];
    public static Random r = new Random(System.currentTimeMillis());
    public static Menu menu = new Menu();
    public static MsgDlg msgdlg = new MsgDlg();
    public static boolean isLoad = false;
    public static boolean isWifi = false;
    public static final Object LOCK = new Object();
    public static boolean isVirTualKey = false;
    static boolean isStart = false;
    public static String[] nameTile = {"t_hang", "t_thanh"};
    static MsgDlg timeoutDlg = new MsgDlg();
    static int timeout = 0;
    public static Position[] posCmd = new Position[3];

    public Canvas() {
        // 0.9.7.2: mỗi bước khởi động trong try riêng (giả lập iOS kẹt "Đang tải dữ liệu game"): một bước hỏng
        // (RMS, thuộc tính hệ thống, ảnh) chỉ ghi chẩn đoán (game.pokemon.KhoiDong) rồi đi tiếp
        isLoad = true;
        instance = this;
        game.pokemon.KhoiDong.buoc("canvas rms");
        try {
            this.isClearAll = RMS.loadRMSInt("loadClearRMS");
            if (this.isClearAll == -1) {
                RMS.deleteAllRMS();
                RMS.saveRMSInt("loadClearRMS", 1);
            }
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("canvas rms", e);
        }
        game.pokemon.KhoiDong.buoc("canvas man hinh");
        try {
            setFullScreenMode(true);
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("fullscreen", e);
        }
        try {
            isMoto = getKeyCode(8) == -20;
        } catch (Throwable e) {
            isMoto = false;
        }
        loadSize();
        game.pokemon.KhoiDong.batCanh(this);
        game.pokemon.KhoiDong.buoc("canvas luong nap");
        try {
            new Thread(Res.load).start();
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("luong nap", e);
        }
        game.pokemon.KhoiDong.buoc("canvas logo");
        try {
            getLogo();
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("logo", e);
        }
        try {
            String pf = System.getProperty("microedition.platform");
            isBB = pf != null && pf.indexOf("RIM") == 0;
        } catch (Throwable e) {
            isBB = false;
        }
        if (isBB) {
            count0 = 5;
            setCommandListener(this);
            cmdMenu = new Command("Menu", 1, 1);
            addCommand(cmdMenu);
        }
        game.pokemon.KhoiDong.buoc("canvas bang auto");
        try {
            autoTab = new BangAuto();
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("BangAuto", e);
        }
        try {
            if (w * h >= 70400) {
                isSmartPhone = hasPointerEvents();
            }
        } catch (Throwable e) {
            isSmartPhone = false;
        }
        if (w < 200) {
            isSmallScreen = true;
        }
        game.pokemon.KhoiDong.buoc("canvas tile rms");
        try {
            getTileFromRMS();
        } catch (Throwable e) {
            isLoadTileServer = true;
            game.pokemon.KhoiDong.loi("tile rms", e);
        }
        game.pokemon.KhoiDong.buoc("cho nap (canvas xong)");
    }

    public void getTileFromRMS() {
        byte[] data = RMS.loadRMS(nameTile[0]);
        if (data == null) {
            isLoadTileServer = true;
        }
    }

    public static Image getLogo() {
        if (imgLogo == null) {
            try {
                imgLogo = Image.createImage("/logo.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imgLogo;
    }

    public static void connect() {
        String host = new StringBuffer("socket://").append(ServerListScr.address[ServerListScr.index]).append(":").append((int) ServerListScr.port[ServerListScr.index]).toString();
        System.out.println(new StringBuffer("ket noi ").append(host).toString());
        if (isBB) {
            if (isWifi) {
                host = new StringBuffer(String.valueOf(host)).append(";interface=wifi").toString();
            } else {
                host = new StringBuffer(String.valueOf(host)).append(";deviceside=true").toString();
            }
        }
        Session_ME.gI().connect(host);
    }

    public void init() {
        gameScr = new GameScr();
        loginScr = new LoginScr();
        inputDlg = new InputDlg();
        shop = new ShopOnline();
        cmdNgatKetNoi = new game.model.Command("", new IAction() {
            @Override
            public void perform() {
                Canvas.endDlg();
                Canvas.instance.init();
                Session_ME.gI().close();
                Tilemap.loadMap(0, null);
                ServerListScr.gI().switchToMe();
            }
        });
    }

    /**
     * 0.9.22 (giả lập Android J2ME Loader / CoffeeCat, màn ảo đặt 1280x720, 1920x1080, 720x1280...): cỡ THẬT của Canvas
     * (getWidth / getHeight) và hệ số phóng nguyên {@link #heSo}. Màn ảo cạnh ngắn &gt;= 684 thì game vẽ ở màn LOGIC
     * w = thatW / heSo, h = thatH / heSo (cạnh ngắn ~360 - cùng bố cục / cỡ chữ / cỡ nút như 640x360) vào ảnh đệm rồi phóng
     * nguyên heSo lần (drawRGB theo dải) - không thì 1280x720 x1: chữ / HUD / nút chỉ còn nửa cỡ trên cùng màn điện thoại.
     * Máy J2ME thật (E72 320x240...) và bản PC / APK (getWidth = màn logic) luôn heSo = 1: vẽ thẳng như cũ.
     */
    public static int thatW, thatH;
    public static int heSo = 1;
    /** Cạnh ngắn logic mục tiêu khi phóng (px) - 360 = cỡ của bản PC / 640x360. */
    static final int CANH_LOGIC = 360;
    /** Hệ số tối đa (màn ảo 4K 3840x2160 -> 4 = 960x540 logic). */
    static final int HE_SO_TOI_DA = 4;
    /** Số hàng logic mỗi dải khi phóng (mảng nguồn / đích nhỏ, không cấp phát mỗi khung). */
    static final int DAI = 8;
    private static Image anhPhong;
    private static int[] dongNguon, dongDich;

    /**
     * Hệ số phóng cho màn thật W x H: cạnh ngắn / 360 làm tròn xuống với dư 36 px (cạnh ngắn &gt;= 684 -> 2: màn ảo
     * 1280x700 hay bị trừ thanh hệ thống vẫn phóng; logic cạnh ngắn &gt;= 342), kẹp 1..4. Cạnh ngắn &lt; 684 -> 1.
     */
    public static int chonHeSo(int W, int H) {
        int ngan = W < H ? W : H;
        int k = (ngan + CANH_LOGIC / 10) / CANH_LOGIC;
        if (k < 1) {
            k = 1;
        }
        if (k > HE_SO_TOI_DA) {
            k = HE_SO_TOI_DA;
        }
        return k;
    }

    public static void loadSize() {
        thatW = instance.getWidth();
        thatH = instance.getHeight();
        heSo = chonHeSo(thatW, thatH);
        w = thatW / heSo;
        h = thatH / heSo;
        if (heSo == 1) {
            anhPhong = null;   // về x1 (xoay / đổi cỡ màn ảo): bỏ ảnh đệm cũ
            dongNguon = null;
            dongDich = null;
        }
    }

    /** Toạ độ chạm thật -> logic (heSo = 1: giữ nguyên). */
    static int chamX(int x) {
        if (heSo <= 1) {
            return x;
        }
        x /= heSo;
        return x < 0 ? 0 : x >= w ? w - 1 : x;
    }

    static int chamY(int y) {
        if (heSo <= 1) {
            return y;
        }
        y /= heSo;
        return y < 0 ? 0 : y >= h ? h - 1 : y;
    }

    /**
     * 0.9.22: cỡ chạm tối thiểu (VungCham.MIN, px logic) của máy J2ME CẢM ỨNG màn ngang (giả lập Android 640x360 /
     * 1280x720...: HudRong) = như bản PC 1280x720 (16 px logic = ~5 mm khi giả lập phóng màn lên điện thoại) - nút nhỏ
     * (viên "1/2", thuốc HP / PP, chip) nới vùng chạm. Bản PC / APK tự đặt MIN theo dpi trước sizeChanged (khác 0) -> giữ.
     */
    static final int MIN_CHAM_J2ME = 16;
    private static boolean minRieng;

    public void sizeChanged(int w0, int h0) {
        loadSize();
        if (game.pokemon.VungCham.MIN == 0 || minRieng) {
            int m = hasPointerEvents() && w >= 400 && h >= 240 ? MIN_CHAM_J2ME : 0;
            minRieng = m > 0;
            game.pokemon.VungCham.MIN = m;
        }
        if (hasPointerEvents()) {
            if (Main.ImgBpa == null) {
                FilePack.init("/main.sh");
                Main.ImgBpa = FilePack.getImg("bpa");
                VirTualKeyB.arrow2 = FrameImage.init("ar2", 4, 6);
            }
            virKey = new VirTualKeyB();
            isVirHorizontal = false;
        }
        hw = w / 2;
        hh = h / 2;
        if (currentScreen != null) {
            if (currentScreen == loginScr) {
                loginScr.init();
            }
            gameScr.init();
            if (currentScreen == WindowInfoScr.me) {
                WindowInfoScr.gI().init();
            }
            if (currentScreen == ListScr.me) {
                ListScr.gI().init();
            }
            if (currentScreen == PauseMenu.me) {
                PauseMenu.gI().init();
            }
            if (currentScreen == MessageScr.me) {
                MessageScr.gI().init();
            }
            if (currentScreen == CreateCharScr.me) {
                CreateCharScr.gI().init();
            }
            currentScreen.init();
            Res.createTab();
            if (currentDialog == inputDlg) {
                inputDlg.init();
            }
        }
    }

    public void start() {
        if (!isStart) {
            new Thread(this).start();
        }
        isStart = true;
    }

    /**
     * 0.9.18: VÒNG GAME BƯỚC CỐ ĐỊNH (lỗi "game chậm như slow motion" 0.9.15-0.9.17). Trước đây mỗi vòng = MỘT nhịp
     * logic + các khung vẽ của nó, rồi ngủ tới "đầu nhịp + 40 ms": vẽ quá giờ (máy chậm, APK 60 khung chờ vsync, J2ME vẽ
     * &gt; 40 ms) là nhịp sau tới trễ -> cả game (đi, hoạt hình, đánh) chậm theo khung (APK 60 máy nhanh 80 %, vẽ 20 ms
     * 27 %; PC vẽ 20 ms 45 %; J2ME vẽ 45 ms 79 % - KiemTocDoGame). Nay logic chạy THEO LỊCH 25 nhịp / giây thời gian thật
     * (nhipKe += 40 ms): trễ thì chạy bù tối đa {@link game.pokemon.NhipVe#BU_TOI_DA} nhịp trước khi vẽ, trễ hơn nữa thì bỏ
     * phần thời gian thừa (không xoáy chết); vẽ bao nhiêu khung tuỳ máy (fps &gt; 25: lưới đều 1000/fps, nội suy theo giờ
     * lịch của nhịp; &lt;= 25: một khung sau mỗi lượt nhịp), ngủ đúng phần còn lại tới việc kế tiếp (nhịp hoặc khung).
     */
    @Override
    public void run() {
        bRun = true;
        long nhipKe = 0;   // giờ theo lịch của nhịp logic kế tiếp (ms)
        while (bRun) {
            try {
                long now = System.currentTimeMillis();
                if (nhipKe == 0 || nhipKe - now > 1000) {
                    nhipKe = now;   // lần đầu / đồng hồ máy lùi
                }
                int chay = 0;
                while (now >= nhipKe && chay < game.pokemon.NhipVe.BU_TOI_DA && bRun) {
                    long lucNhip = nhipKe;
                    nhipKe += game.pokemon.NhipVe.NHIP_MS;
                    chay++;
                    try {
                        nhipLogic(lucNhip);
                    } catch (Throwable e3) {
                        // 0.9.4: mọi lỗi (cả Error) trong một nhịp chỉ bỏ nhịp đó, không giết vòng lặp game
                        game.pokemon.KhoiDong.loi("nhip " + game.pokemon.KhoiDong.pha, e3);
                        try {
                            game.pokemon.GiamSat.loiNhip(e3);
                        } catch (Throwable e5) {
                            // bỏ qua
                        }
                        clearKeyPressed();
                    }
                    now = System.currentTimeMillis();
                }
                if (now >= nhipKe) {
                    // đã bù đủ BU_TOI_DA nhịp mà vẫn trễ (máy đứng / GC / tải map): bỏ phần thời gian thừa thay vì đuổi mãi
                    game.pokemon.NhipVe.boThoiGian(now - nhipKe);
                    nhipKe = now;
                }
                game.pokemon.KhoiDong.pha = "ve";
                long ke = nhipKe;
                if (game.pokemon.NhipVe.veTheoLuoi()) {
                    // > 25 khung/giây (PC, APK): khung theo lưới đều 1000/fps, vị trí nội suy giữa hai nhịp
                    long khungKe = game.pokemon.NhipVe.veKhung(this, now);
                    if (khungKe < ke) {
                        ke = khungKe;
                    }
                } else if (chay > 0) {
                    veMotKhung();   // J2ME / fps <= 25: một khung sau mỗi lượt nhịp (chạy bù vẫn chỉ vẽ một lần)
                }
                long cho = ke - System.currentTimeMillis();
                // luôn nhường CPU ít nhất 1 ms (máy J2ME một lõi: luồng mạng / phím phải được chạy khi game đang bù nhịp)
                Thread.sleep(cho > 1 ? cho : 1L);
            } catch (Throwable e3) {
                game.pokemon.KhoiDong.loi("nhip " + game.pokemon.KhoiDong.pha, e3);
                // 0.9.7.2: nhịp lỗi vẫn VẼ (trước: lỗi ở đầu nhịp mỗi lần = không bao giờ repaint, màn đứng hình)
                try {
                    repaint();
                    serviceRepaints();
                } catch (Throwable e6) {
                    // bỏ qua
                }
                clearKeyPressed();
                try {
                    Thread.sleep(40L);   // lỗi không được thành vòng quay bận (trước: bỏ qua sleep -> 100% CPU)
                } catch (InterruptedException e4) {
                }
                nhipKe = 0;
            }
        }
    }

    /** Vẽ một khung và chờ vẽ xong (J2ME: serviceRepaints; BlackBerry: chờ LOCK như bản gốc). */
    public void veMotKhung() {
        repaint();
        if (isBB) {
            synchronized (LOCK) {
                try {
                    LOCK.wait(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            serviceRepaints();
        }
    }

    /** Một nhịp logic 40 ms (nhập, menu, hộp thoại, màn hiện tại). lucNhip = giờ theo lịch của nhịp. */
    private void nhipLogic(long lucNhip) {
        game.pokemon.KhoiDong.pha = "NhipVe";
        game.pokemon.NhipVe.batDau();
        game.pokemon.NhipVe.dauNhip(lucNhip);   // ghi vị trí đầu nhịp (nội suy) - mốc = giờ THEO LỊCH của nhịp (0.9.18)
        gameTick++;
        if (gameTick > 10000) {
            gameTick = 0;
        }
        if (isVirTualKey) {
            virKey.update();
        }
        // pokemon: cú chạm trúng điều khiển đã đăng ký lúc vẽ (VungCham) - chạy trước, nuốt cú chạm
        game.pokemon.KhoiDong.pha = "VungCham";
        game.pokemon.VungCham.xuLy();
        game.pokemon.KhoiDong.pha = "TuNoiLai";
        game.pokemon.TuNoiLai.tick();   // 0.9.4: tự kết nối lại (luồng game)
        game.pokemon.KhoiDong.pha = "ManTai";
        game.pokemon.ManTai.tick();     // 0.9.7.1: màn tải đứng quá lâu -> hộp lỗi + Thử lại (không quay mãi)
        game.pokemon.KhoiDong.pha = "GiamSat";
        game.pokemon.GiamSat.tick();   // 0.9.4: gỡ lớp modal kẹt (hộp không ai vẽ / hội thoại nhiệm vụ mồ côi)
        game.pokemon.KhoiDong.pha = "CapNhat";
        game.pokemon.CapNhat.tick();   // 0.9.7: hộp bắt buộc cập nhật luôn trên cùng, thẻ "Đã có bản mới" khi vào thế giới
        game.pokemon.NhipVe.xong(game.pokemon.NhipVe.NHAP);
        game.pokemon.KhoiDong.pha = "menu";
        if (menu.showMenu) {
            menu.updateMenuKey();
            menu.updateMenu();
        }
        if (currentDialog != null) {
            currentDialog.update();
        }
        game.pokemon.KhoiDong.pha = "man";
        if (currentScreen != null) {
            currentScreen.update();
            if (!menu.showMenu && currentDialog == null) {
                currentScreen.updateKey();
            }
        }
        if (timeout > 0) {
            timeout--;
            if (timeout == 0 && currentDialog == null) {
                timeoutDlg.show();
            }
        }
        game.pokemon.KhoiDong.soNhip++;
        game.pokemon.KhoiDong.nhipCuoi = System.currentTimeMillis();
        clearKeyPressed();
        game.pokemon.GiamSat.nhipOk();
        game.pokemon.NhipVe.xong(game.pokemon.NhipVe.LOGIC);
    }

    // pokemon: tam dung / tiep nhac khi man hinh bi che (cuoc goi, menu he thong)
    protected void hideNotify() {
        try {
            game.model.Sound.me.pauseMusic();
        } catch (Throwable e) {
            // bo qua
        }
    }

    protected void showNotify() {
        try {
            game.model.Sound.me.resumeMusic();
        } catch (Throwable e) {
            // bo qua
        }
    }

    /**
     * 0.9.22 (Nokia E72 / S60, bàn phím QWERTY): sự kiện phím chạy trên luồng giao diện của máy - lỗi trong xử lý phím
     * không được lọt ra ngoài (máy thật có thể đóng MIDlet). Lỗi ghi chẩn đoán, bỏ phím đó.
     */
    public void keyPressed(int keyCode) {
        try {
            keyPressed0(keyCode);
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("phim keyPressed " + keyCode, e);
        }
    }

    private void keyPressed0(int keyCode) {
        if (isLoad) {
            return;
        }
        // mod: phím tắt PK (Mã pk, nháy map, bật đồ sát, gọi thú/pet)
        if (game.render.screen.AutoGuard.phimPk(keyCode)) {
            return;
        }
        if (isMoto) {
            switch (keyCode) {
                case -23:
                    keyCode = -8;
                    break;
                case -22:
                case 22:
                    keyCode = -7;
                    break;
                case -21:
                case 21:
                    keyCode = -6;
                    break;
                case -20:
                    keyCode = -5;
                    break;
                case -6:
                    keyCode = -2;
                    break;
                case -5:
                    keyCode = -4;
                    break;
                case -2:
                    keyCode = -3;
                    break;
            }
        } else if (isBB) {
            switch (keyCode) {
                case -8:
                    boolean[] zArr = keyHold;
                    keyPressed[5] = true;
                    zArr[5] = true;
                    return;
                case 1:
                    boolean[] zArr2 = keyHold;
                    keyPressed[2] = true;
                    zArr2[2] = true;
                    return;
                case 2:
                    boolean[] zArr3 = keyHold;
                    keyPressed[4] = true;
                    zArr3[4] = true;
                    return;
                case 5:
                    boolean[] zArr4 = keyHold;
                    keyPressed[6] = true;
                    zArr4[6] = true;
                    return;
                case 6:
                    boolean[] zArr5 = keyHold;
                    keyPressed[8] = true;
                    zArr5[8] = true;
                    return;
                default:
                    if (currentScreen == gameScr) {
                        switch (keyCode) {
                            case 101:
                                boolean[] zArr6 = keyHold;
                                keyPressed[2] = true;
                                zArr6[2] = true;
                                break;
                            case 102:
                                boolean[] zArr7 = keyHold;
                                keyPressed[6] = true;
                                zArr7[6] = true;
                                break;
                            case 115:
                                boolean[] zArr8 = keyHold;
                                keyPressed[4] = true;
                                zArr8[4] = true;
                                break;
                            case 120:
                                boolean[] zArr9 = keyHold;
                                keyPressed[8] = true;
                                zArr9[8] = true;
                                break;
                        }
                        return;
                    }
                    break;
            }
        }
        if (keyCode == 10) {
            keyCode = -5;
        }
        if (!currentScreen.keyPress(keyCode)) {
            mapKeyPress(keyCode);
        }
        if (currentDialog != null) {
            currentDialog.keyPress(keyCode);
        }
    }

    public void mapKeyPress(int keyCode) {
        switch (keyCode) {
            case -22:
            case -7:
                boolean[] zArr = keyHold;
                keyPressed[13] = true;
                zArr[13] = true;
                break;
            case -21:
            case -6:
                boolean[] zArr2 = keyHold;
                keyPressed[12] = true;
                zArr2[12] = true;
                break;
            case -5:
                boolean[] zArr3 = keyHold;
                keyPressed[5] = true;
                zArr3[5] = true;
                break;
            case 35:
                boolean[] zArr4 = keyHold;
                keyPressed[11] = true;
                zArr4[11] = true;
                break;
            case 42:
                boolean[] zArr5 = keyHold;
                keyPressed[10] = true;
                zArr5[10] = true;
                break;
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                if (currentDialog == null && currentScreen == gameScr && !isBB) {
                    if (!isVirTualKey || !VirTualKeyB.isABC) {
                        keyHold[keyCode - 48] = true;
                        keyPressed[keyCode - 48] = true;
                    }
                    break;
                }
                break;
            default:
                if (keyCode < 65 || keyCode > 122) {
                    switch (getGameAction(keyCode)) {
                        case 1:
                            boolean[] zArr6 = keyHold;
                            keyPressed[2] = true;
                            zArr6[2] = true;
                            break;
                        case 2:
                            boolean[] zArr7 = keyHold;
                            keyPressed[4] = true;
                            zArr7[4] = true;
                            break;
                        case 5:
                            boolean[] zArr8 = keyHold;
                            keyPressed[6] = true;
                            zArr8[6] = true;
                            break;
                        case 6:
                            boolean[] zArr9 = keyHold;
                            keyPressed[8] = true;
                            zArr9[8] = true;
                            break;
                        case 8:
                            boolean[] zArr10 = keyHold;
                            keyPressed[5] = true;
                            zArr10[5] = true;
                            break;
                    }
                }
                break;
        }
    }

    /**
     * 0.9.22 (Nokia E72 / S60, bàn phím QWERTY): sự kiện phím chạy trên luồng giao diện của máy - lỗi trong xử lý phím
     * không được lọt ra ngoài (máy thật có thể đóng MIDlet). Lỗi ghi chẩn đoán, bỏ phím đó.
     */
    public void keyReleased(int keyCode) {
        try {
            keyReleased0(keyCode);
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("phim keyReleased " + keyCode, e);
        }
    }

    private void keyReleased0(int keyCode) {
        if (isLoad) {
            return;
        }
        if (isMoto) {
            switch (keyCode) {
                case -23:
                    keyCode = -8;
                    break;
                case -22:
                case 22:
                    keyCode = -7;
                    break;
                case -21:
                case 21:
                    keyCode = -6;
                    break;
                case -20:
                    keyCode = -5;
                    break;
                case -6:
                    keyCode = -2;
                    break;
                case -5:
                    keyCode = -4;
                    break;
                case -2:
                    keyCode = -3;
                    break;
            }
        } else if (isBB) {
            if (keyCode == 27) {
                keyCode = -7;
            }
            if (currentScreen == gameScr) {
                switch (keyCode) {
                    case 101:
                        boolean[] zArr = keyHold;
                        keyPressed[2] = false;
                        zArr[2] = false;
                        keyReleased[2] = true;
                        break;
                    case 102:
                        boolean[] zArr2 = keyHold;
                        keyPressed[6] = false;
                        zArr2[6] = false;
                        keyReleased[6] = true;
                        break;
                    case 115:
                        boolean[] zArr3 = keyHold;
                        keyPressed[4] = false;
                        zArr3[4] = false;
                        keyReleased[4] = true;
                        break;
                    case 120:
                        boolean[] zArr4 = keyHold;
                        keyPressed[8] = false;
                        zArr4[8] = false;
                        keyReleased[8] = true;
                        break;
                }
                return;
            }
        }
        mapKeyRelease(keyCode);
    }

    public void mapKeyRelease(int keyCode) {
        switch (keyCode) {
            case -22:
            case -7:
                keyHold[13] = false;
                keyReleased[13] = true;
                break;
            case -21:
            case -6:
                keyHold[12] = false;
                keyReleased[12] = true;
                break;
            case -5:
            case 10:
                keyHold[5] = false;
                keyReleased[5] = true;
                break;
            case 35:
                keyHold[11] = false;
                keyReleased[11] = true;
                break;
            case 42:
                keyHold[10] = false;
                keyReleased[10] = true;
                break;
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                keyHold[keyCode - 48] = false;
                keyReleased[keyCode - 48] = true;
                break;
            default:
                switch (getGameAction(keyCode)) {
                    case 1:
                        keyHold[2] = false;
                        keyReleased[2] = true;
                        break;
                    case 2:
                        keyHold[4] = false;
                        keyReleased[4] = true;
                        break;
                    case 5:
                        keyHold[6] = false;
                        keyReleased[6] = true;
                        break;
                    case 6:
                        keyHold[8] = false;
                        keyReleased[8] = true;
                        break;
                    case 8:
                        keyHold[5] = false;
                        keyReleased[5] = true;
                        break;
                }
                break;
        }
    }

    protected void pointerDragged(int x, int y) {
        x = chamX(x);
        y = chamY(y);
        px = x;
        py = y;
    }

    /**
     * 0.9.22: điểm NHẤN của cú chạm gần nhất (không ai ghi đè - pxLast bị Cuon / VungCham đặt lại) + số lần nhấn (phân biệt
     * cú nhấn mới) - game.pokemon.KeoChieu.
     */
    public static int xNhan, yNhan, soNhan;

    protected void pointerPressed(int x, int y) {
        x = chamX(x);
        y = chamY(y);
        xNhan = x;
        yNhan = y;
        soNhan++;
        dangGiu = true;
        isPointerDown = true;
        isPointerDownNj = true;
        px = x;
        py = y;
        pxLast = x;
        pyLast = y;
    }

    protected void pointerReleased(int x, int y) {
        x = chamX(x);
        y = chamY(y);
        dangGiu = false;
        isPointerDown = false;
        isPointerClick = true;
        isPointerRelease = true;
        isPointerDownNj = false;
        isPointerJustRelease = true;
        px = x;
        py = y;
    }

    public static void clearKeyHold() {
        for (int i = 0; i < 14; i++) {
            keyHold[i] = false;
        }
    }

    public static void clearKeyPressed() {
        isPointerClick = false;
        isPointerRelease = false;
        isPointerJustRelease = false;
        for (int i = 0; i < 14; i++) {
            keyPressed[i] = false;
        }
    }

    public static void resetTrans(Graphics g) {
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        g.setClip(0, 0, w, h);
    }

    protected void paint(Graphics g) {
        if (heSo > 1) {
            try {
                vePhong(g);
                return;
            } catch (Throwable e) {
                // hết bộ nhớ cho ảnh đệm / lỗi phóng: vẽ thẳng x1 (vẫn chơi được, chỉ nhỏ)
                anhPhong = null;
                dongNguon = null;
                dongDich = null;
                game.pokemon.KhoiDong.loi("ve phong x" + heSo, e);
            }
        }
        ve1(g);
    }

    /**
     * Màn ảo to (heSo &gt; 1): vẽ một khung vào ảnh đệm w x h (logic) rồi phóng nguyên heSo lần lên g theo dải DAI hàng
     * (getRGB / drawRGB đúng đặc tả MIDP 2.0: vùng trong ảnh, mảng đủ cỡ). Phần lẻ thatW - w * heSo (&lt; heSo px) tô đen.
     */
    private void vePhong(Graphics g) {
        int k = heSo, lw = w, lh = h;
        Image anh = anhPhong;
        if (anh == null || anh.getWidth() != lw || anh.getHeight() != lh) {
            anhPhong = null;
            anh = Image.createImage(lw, lh);
            anhPhong = anh;
        }
        Graphics ga = anh.getGraphics();
        ve1(ga);
        int dw = lw * k;
        if (dongNguon == null || dongNguon.length != lw * DAI) {
            dongNguon = new int[lw * DAI];
        }
        if (dongDich == null || dongDich.length != dw * k * DAI) {
            dongDich = new int[dw * k * DAI];
        }
        int[] src = dongNguon, dst = dongDich;
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        g.setClip(0, 0, thatW, thatH);
        for (int y0 = 0; y0 < lh; y0 += DAI) {
            int n = lh - y0 < DAI ? lh - y0 : DAI;
            anh.getRGB(src, 0, lw, 0, y0, lw, n);
            for (int r = 0; r < n; r++) {
                int s0 = r * lw, d0 = r * k * dw, d = d0;
                for (int x = 0; x < lw; x++) {
                    int c = src[s0 + x];
                    for (int i = 0; i < k; i++) {
                        dst[d++] = c;
                    }
                }
                for (int i = 1; i < k; i++) {
                    System.arraycopy(dst, d0, dst, d0 + i * dw, dw);
                }
            }
            g.drawRGB(dst, 0, dw, 0, y0 * k, dw, n * k, false);
        }
        g.setColor(0);
        if (thatW > dw) {
            g.fillRect(dw, 0, thatW - dw, thatH);
        }
        if (thatH > lh * k) {
            g.fillRect(0, lh * k, thatW, thatH - lh * k);
        }
    }

    private void ve1(Graphics g) {
        game.render.screen.Font.nenSang = false;
        try {
            game.pokemon.NhipVe.dauKhung();
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("ve NhipVe", e);
        }
        try {
            ve0(g);
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("ve", e);
        } finally {
            try {
                game.pokemon.NhipVe.cuoiKhung();
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        // 0.9.7.2: dòng chẩn đoán khởi động (lỗi / quá 10 s) - phông hệ thống, luôn trên cùng
        try {
            resetTrans(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        game.pokemon.KhoiDong.ve(g, w, h);
    }

    private void ve0(Graphics g) {
        game.pokemon.VungCham.khungMoi();
        if (game.pokemon.ChuUI.bo != null) {
            game.pokemon.ChuUI.bo.khungMoi();
        }
        try {
            // 0.9.4: mỗi lớp vẽ trong try riêng - màn bên dưới ném lỗi không được làm mất hộp thoại / menu đang mở
            // (hộp vô hình nhưng vẫn chặn chạm = treo màn); lỗi ghi qua GiamSat
            try {
                if (currentScreen != null) {
                    currentScreen.paint(g);
                }
                if (currentScreen == gameScr) {
                    game.pokemon.HopThoaiDuoi.ve(g);   // pokemon: hộp thoại NPC đáy màn
                }
            } catch (Throwable e) {
                game.pokemon.GiamSat.loi("ve man " + currentScreen, e);
            }
            Dialog dlg = currentDialog;
            if (dlg != null) {
                game.pokemon.VungCham.lop(1);
                try {
                    resetTrans(g);
                    dlg.paint(g);
                    game.pokemon.GiamSat.daVeHop(dlg);
                } catch (Throwable e) {
                    game.pokemon.GiamSat.loi("ve hop thoai " + dlg, e);
                }
            }
            if (menu.showMenu) {
                game.pokemon.VungCham.lop(2);
                try {
                    resetTrans(g);
                    menu.paintMenu(g);
                } catch (Throwable e) {
                    game.pokemon.GiamSat.loi("ve menu", e);
                }
            }
            game.pokemon.DungDo.ve(g);   // pokemon: kết quả dùng vật phẩm (thông báo nhanh, mọi màn)
            game.pokemon.TienTe.veGoiY(g);   // pokemon: chú thích biểu tượng tiền (chạm biểu tượng)
            if (isLoad) {
                game.pokemon.VungCham.lop(3);   // màn tải phủ: không điều khiển nào bên dưới nhận chạm
                resetTrans(g);
                // pokemon 0.9.5: màn tải khởi động = cùng thành phần màn tải đăng nhập / đổi bản đồ (ManTai)
                try {
                    game.pokemon.ManTai.ve(g, game.pokemon.ManTai.KHOI_DONG);
                } catch (Throwable th) {
                    // font / ảnh chưa nạp
                }
            }
            if (isVirTualKey) {
                virKey.paint(g);
            }
            if (isBB) {
                synchronized (LOCK) {
                    LOCK.notify();
                }
            }
            GameScr.paint12plus(g);
        } catch (Exception e) {
        }
        resetTrans(g);
        try {
            AutoGuard.paint(g);
        } catch (Throwable e) {
            game.pokemon.GiamSat.loi("ve AutoGuard", e);
        }
        game.pokemon.VungCham.khungXong();
        game.pokemon.GiamSat.xongKhung();
    }

    public static int abs(int i) {
        return i > 0 ? i : -i;
    }

    public static void endDlg() {
        currentDialog = null;
    }

    public static void startYesNoDlgPutArr(String info, String[] arr, IAction yesAction, IAction noAction) {
        if (game.pokemon.MenuNguCanh.bat()) {
            // pokemon (APK / PC): hộp xác nhận chung, danh sách nối thành một đoạn
            StringBuffer sb = new StringBuffer(info == null ? "" : info.trim());
            if (arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] != null && arr[i].length() > 0) {
                        sb.append(sb.length() > 0 ? " " : "").append(arr[i].trim());
                    }
                }
            }
            game.pokemon.HopXacNhan.hoi("Xác nhận", sb.toString(), "Đồng ý", yesAction, noAction);
            return;
        }
        msgdlg.isIcon = false;
        msgdlg.setInfoArr(info, arr, new game.model.Command("Có", yesAction), new game.model.Command("", yesAction), new game.model.Command("Không", noAction));
        msgdlg.show();
    }

    public static void startOKDlg(String info, boolean isIcon) {
        if (AutoGuard.autoEatDlg(info)) {
            return;
        }
        // 0.9.6.1: trong game, hộp "OK" chỉ để báo tin (kết quả, lỗi, nhắc) -> thẻ nhanh không chặn (game.pokemon.ThongBao);
        // việc nghiêm trọng / nội dung dài vẫn là hộp
        if (!isIcon && game.pokemon.ThongBao.tuHopOK(info)) {
            return;
        }
        msgdlg.isIcon = isIcon;
        msgdlg.setInfo(info, new game.model.Command("OK", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        }), (game.model.Command) null, (game.model.Command) null);
        msgdlg.show();
    }

    public static void startOKDlg(String[] info) {
        startOKDlg(info, false);
    }

    public static void startOKDlg(String[] info, boolean isIcon) {
        msgdlg.isIcon = isIcon;
        msgdlg.setInfo(info, new game.model.Command("OK", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        }), (game.model.Command) null, (game.model.Command) null);
        msgdlg.show();
    }

    public static void startOKDlg(String info) {
        startOKDlg(info, false);
    }

    public static void startYesNoDlg(String info, IAction yesAction, IAction noAction) {
        if (game.pokemon.MenuNguCanh.bat()) {
            // pokemon (APK / PC): hộp xác nhận chung - hai nút to, chính bên phải
            game.pokemon.HopXacNhan.hoi("Xác nhận", info, "Đồng ý", yesAction, noAction);
            return;
        }
        msgdlg.isIcon = false;
        msgdlg.setInfo(info, new game.model.Command("Có", yesAction), new game.model.Command("", yesAction), new game.model.Command("Không", noAction));
        msgdlg.show();
    }

    public static void startWaitDlg(String info, boolean isIcon) {
        if (game.pokemon.ManTai.dangMo() || currentScreen instanceof game.render.screen.ChangScr) {
            return;   // 0.9.5: màn tải đang hiện - không chồng hộp chờ thứ hai
        }
        msgdlg.isIcon = isIcon;
        msgdlg.setInfo(info, (game.model.Command) null, new game.model.Command("Huỷ", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        }), (game.model.Command) null);
        msgdlg.show();
    }

    public static void startWaitDlg() {
        if (game.pokemon.ManTai.dangMo() || currentScreen instanceof game.render.screen.ChangScr) {
            return;
        }
        msgdlg.isIcon = false;
        msgdlg.setInfo("Xin chờ...", (game.model.Command) null, new game.model.Command("Huỷ", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        }), (game.model.Command) null);
        msgdlg.show();
    }

    public static void startOKDlg(String info, boolean isIcon, int timeout2) {
        timeoutDlg.isIcon = isIcon;
        timeoutDlg.setInfo(info, (game.model.Command) null, new game.model.Command("OK", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        }), (game.model.Command) null);
        timeout = timeout2;
    }

    public static void startOKDlg(String info, IAction action) {
        msgdlg.isIcon = false;
        msgdlg.setInfo(info, (game.model.Command) null, new game.model.Command("OK", action), (game.model.Command) null);
        msgdlg.show();
    }

    public static void startYesNoDlg(String info, IAction yesAction) {
        if (game.pokemon.MenuNguCanh.bat()) {
            game.pokemon.HopXacNhan.hoi("Xác nhận", info, "Đồng ý", yesAction, null);
            return;
        }
        msgdlg.isIcon = false;
        msgdlg.setInfo(info, new game.model.Command("Có", yesAction), new game.model.Command("", yesAction), new game.model.Command("Không", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        }));
        msgdlg.show();
    }

    public static boolean isKeyPressed(int index) {
        if (keyPressed[index]) {
            keyPressed[index] = false;
            return true;
        }
        return false;
    }

    public static String getMoneys(long m) {
        String str = "";
        long mm = (m / 1000) + 1;
        for (int i = 0; i < mm; i++) {
            if (m >= 1000) {
                long a = m % 1000;
                if (a == 0) {
                    str = new StringBuffer(".000").append(str).toString();
                } else if (a < 10) {
                    str = new StringBuffer(".00").append(a).append(str).toString();
                } else if (a < 100) {
                    str = new StringBuffer(".0").append(a).append(str).toString();
                } else {
                    str = new StringBuffer(".").append(a).append(str).toString();
                }
                m /= 1000;
            } else {
                str = new StringBuffer(String.valueOf(m)).append(str).toString();
                break;
            }
        }
        return str;
    }

    public static int collisionCmdBar() {
        if (isPointer(0, h - (Screen.deltaY * 2), 50, Screen.deltaY * 2)) {
            return 0;
        }
        if (isPointer(hw - 25, h - (Screen.deltaY * 2), 50, Screen.deltaY * 2)) {
            return 1;
        }
        if (isPointer(w - 50, h - (Screen.deltaY * 2), 50, Screen.deltaY * 2)) {
            return 2;
        }
        return -1;
    }

    public static boolean isPointer(int x, int y, int w2, int h2) {
        if ((isPointerDown || isPointerClick) && px >= x && px <= x + w2 && py >= y && py <= y + h2) {
            return true;
        }
        return false;
    }

    public static String getPriceMoney(long xu, int gold) {
        String price = "";
        if (xu > 0) {
            price = new StringBuffer(String.valueOf(price)).append(getMoneys(xu)).append("Xu").toString();
        }
        if (gold > 0) {
            if (xu > 0) {
                price = new StringBuffer(String.valueOf(price)).append(" - ").toString();
            }
            price = new StringBuffer(String.valueOf(price)).append(getMoneys(gold)).append("Luong").toString();
        }
        return price;
    }

    public void commandAction(Command arg0, Displayable arg1) {
        if (arg0 == cmdMenu) {
            keyPressed[12] = true;
        }
    }

    public static String formatNumberDotK(int number) {
        String string;
        String strNum = "";
        String strTemp = new StringBuffer().append(number).toString();
        if (strTemp.length() <= 3) {
            return strTemp;
        }
        for (int i = strTemp.length() - 1; i >= 0; i--) {
            if ((strTemp.length() - i) % 3 == 0 && i > 0) {
                string = new StringBuffer(String.valueOf(strNum)).append(strTemp.charAt(i)).append(".").toString();
            } else {
                string = new StringBuffer(String.valueOf(strNum)).append(strTemp.charAt(i)).toString();
            }
            strNum = string;
        }
        String result = new StringBuffer(strNum).reverse().toString();
        return result;
    }

    public static boolean isPointerHoldIn(int x, int y, int w2, int h2) {
        if ((isPointerDownNj || isPointerJustRelease) && px >= x && px <= x + w2 && py >= y && py <= y + h2) {
            return true;
        }
        return false;
    }

    public static boolean isPointerNj(int x, int y, int w2, int h2) {
        if ((isPointerDownNj || isPointerJustRelease) && px >= x && px <= x + w2 && py >= y && py <= y + h2) {
            return true;
        }
        return false;
    }

    public static int random(int a, int b) {
        return a + r.nextInt(b - a);
    }

    public static boolean canTouch() {
        return Math.abs(px - pxLast) < 10 && Math.abs(py - pyLast) < 10;
    }
}
