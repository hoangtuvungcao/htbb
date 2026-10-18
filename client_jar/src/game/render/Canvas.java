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
        isLoad = true;
        instance = this;
        this.isClearAll = RMS.loadRMSInt("loadClearRMS");
        if (this.isClearAll == -1) {
            RMS.deleteAllRMS();
            RMS.saveRMSInt("loadClearRMS", 1);
        }
        setFullScreenMode(true);
        System.gc();
        isMoto = getKeyCode(8) == -20;
        loadSize();
        new Thread(Res.load).start();
        getLogo();
        isBB = System.getProperty("microedition.platform").indexOf("RIM") == 0;
        if (isBB) {
            count0 = 5;
            setCommandListener(this);
            cmdMenu = new Command("Menu", 1, 1);
            addCommand(cmdMenu);
        }
        autoTab = new BangAuto();
        if (w * h >= 70400) {
            isSmartPhone = hasPointerEvents();
        }
        if (w < 200) {
            isSmallScreen = true;
        }
        getTileFromRMS();
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

    public static void loadSize() {
        w = instance.getWidth();
        h = instance.getHeight();
    }

    public void sizeChanged(int w0, int h0) {
        loadSize();
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

    @Override
    public void run() {
        bRun = true;
        while (bRun) {
            try {
                long l1 = System.currentTimeMillis();
                gameTick++;
                if (gameTick > 10000) {
                    gameTick = 0;
                }
                if (isVirTualKey) {
                    virKey.update();
                }
                if (menu.showMenu) {
                    menu.updateMenuKey();
                    menu.updateMenu();
                }
                if (currentDialog != null) {
                    currentDialog.update();
                }
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
                clearKeyPressed();
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
                long l2 = System.currentTimeMillis() - l1;
                if (l2 < 40) {
                    try {
                        Thread.sleep(40 - l2);
                    } catch (InterruptedException e2) {
                    }
                } else {
                    Thread.sleep(1L);
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
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

    public void keyPressed(int keyCode) {
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

    public void keyReleased(int keyCode) {
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
        px = x;
        py = y;
    }

    protected void pointerPressed(int x, int y) {
        isPointerDown = true;
        isPointerDownNj = true;
        px = x;
        py = y;
        pxLast = x;
        pyLast = y;
    }

    protected void pointerReleased(int x, int y) {
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
        game.render.screen.Font.nenSang = false;
        try {
            if (currentScreen != null) {
                currentScreen.paint(g);
            }
            if (currentScreen == gameScr) {
                game.pokemon.HopThoaiDuoi.ve(g);   // pokemon: hộp thoại NPC đáy màn
            }
            if (currentDialog != null) {
                currentDialog.paint(g);
            }
            if (menu.showMenu) {
                menu.paintMenu(g);
            }
            if (isLoad) {
                resetTrans(g);
                // pokemon: màn tải = trời xanh + đồi cỏ, biển tên game, Poké Ball lắc chờ
                if (game.pokemon.PokeBody.CHE_DO_POKEMON) {
                    w = getWidth();
                    h = getHeight();
                    if (game.pokemon.Banner.ve(g, -1, true)) {
                        // banner nhiều lớp + logo; Poké Ball chờ giữa trời
                        try {
                            game.pokemon.ThuongHieu.veLogo(g, w >> 1, Math.max(8, (h >> 1) - 90), w);
                        } catch (Throwable th) {
                            // font chưa nạp
                        }
                    } else {
                        game.pokemon.GiaoDien.nenTroi(g, w, h);
                        game.pokemon.ThuongHieu.veTieuDe(g, w >> 1, (h >> 1) - 60, w);
                    }
                    game.pokemon.GiaoDien.dangTai(g, w >> 1, (h >> 1) - 10, 12);
                    try {
                        game.render.screen.Font.borderFont.drawString(g, "Đang tải...", w >> 1, (h >> 1) + 8, 2);
                    } catch (Throwable th) {
                        // font chưa nạp
                    }
                } else {
                    g.setColor(0);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.drawImage(imgLogo, getWidth() >> 1, getHeight() >> 1, 3);
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
        AutoGuard.paint(g);
    }

    public static int abs(int i) {
        return i > 0 ? i : -i;
    }

    public static void endDlg() {
        currentDialog = null;
    }

    public static void startYesNoDlgPutArr(String info, String[] arr, IAction yesAction, IAction noAction) {
        msgdlg.isIcon = false;
        msgdlg.setInfoArr(info, arr, new game.model.Command("Có", yesAction), new game.model.Command("", yesAction), new game.model.Command("Không", noAction));
        msgdlg.show();
    }

    public static void startOKDlg(String info, boolean isIcon) {
        if (AutoGuard.autoEatDlg(info)) {
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
        msgdlg.isIcon = false;
        msgdlg.setInfo(info, new game.model.Command("Có", yesAction), new game.model.Command("", yesAction), new game.model.Command("Không", noAction));
        msgdlg.show();
    }

    public static void startWaitDlg(String info, boolean isIcon) {
        msgdlg.isIcon = isIcon;
        msgdlg.setInfo(info, (game.model.Command) null, new game.model.Command("Cancel", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        }), (game.model.Command) null);
        msgdlg.show();
    }

    public static void startWaitDlg() {
        msgdlg.isIcon = false;
        msgdlg.setInfo("Xin chờ...", (game.model.Command) null, new game.model.Command("Cancel", new IAction() {
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
