package game.render;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.teamobi.kpah.GameMidlet;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.Command;
import game.model.Cout;
import game.model.FilePack;
import game.model.IAction;
import game.model.ImageMapBig;
import game.model.InputDlg;
import game.model.Position;
import game.model.Tilemap;
import game.render.screen.AutoGuard;
import game.render.screen.BangAuto;
import game.render.screen.ChangScr;
import game.render.screen.CreateCharScr;
import game.render.screen.Dialog;
import game.render.screen.GameScr;
import game.render.screen.ListScr;
import game.render.screen.LoginScr;
import game.render.screen.Menu;
import game.render.screen.MessageScr;
import game.render.screen.MoQuaScr;
import game.render.screen.MsgDlg;
import game.render.screen.PauseMenu;
import game.render.screen.RMS;
import game.render.screen.Screen;
import game.render.screen.ServerListScr;
import game.render.screen.ShopOnline;
import game.render.screen.WindowInfoScr;
import java.util.Calendar;
import java.util.Random;
import network.Session_ME;

public class GCanvas extends SurfaceView implements SurfaceHolder.Callback {
    public static Context Context = null;
    private static final int INVALID_POINTER_ID = -1;
    public static BangAuto autoTab;
    public static Command cmdMenu;
    public static Command cmdNgatKetNoi;
    public static int count0;
    public static int coutPaintLogo;
    public static long currT;
    public static Dialog currentDialog;
    public static Screen currentScreen;
    public static Activity gCanvas;
    public static GameScr gameScr;
    public static int gameTick;
    public static int h;
    public static int hh;
    public static int hw;
    public static Bitmap imgLogo;
    public static InputDlg inputDlg;
    public static GCanvas instance;
    public static boolean isBB;
    public static int isCheat;
    public static boolean isHpUp;
    public static boolean isHpdown;
    public static boolean isLoadTileServer;
    public static boolean isPointerClick;
    public static boolean isPointerDown;
    public static boolean isPointerDownItem;
    public static boolean isPointerDownNj;
    public static boolean isPointerDrange;
    public static boolean isPointerJustRelease;
    public static boolean isPointerRelease;
    public static boolean isSmallScreen;
    public static boolean isSmartPhone;
    public static boolean isWait;
    public static long lastT;
    public static LoginScr loginScr;
    public static int px;
    public static int pxHp;
    public static int pxLast;
    public static int py;
    public static int pyHp;
    public static int pyLast;
    public static Resources res;
    public static int secondRemain;
    public static ShopOnline shop;
    public static int w;
    public Display display;
    int isClearAll;
    private int mActivePointerId;
    public GThread thread;
    public static Graphics g = new Graphics();
    public static int screenlevel = 1;
    public static boolean[] keyPressed = new boolean[15];
    public static boolean[] keyReleased = new boolean[15];
    public static boolean[] keyHold = new boolean[15];
    public static Random r = new Random(System.currentTimeMillis());
    public static Menu menu = new Menu();
    public static MsgDlg msgdlg = new MsgDlg();
    public static boolean isLoad = false;
    public static boolean isWifi = false;
    public static final Object LOCK = new Object();
    public static boolean isShow = false;
    public static String[] nameTile = {"t_hang", "t_thanh"};
    static MsgDlg timeoutDlg = new MsgDlg();
    static int timeout = 0;
    public static Position[] posCmd = new Position[3];

    public static int abs(int i) {
        return i > 0 ? i : -i;
    }

    public boolean hasPointerEvents() {
        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    public GCanvas(Context context) {
        super(context);
        this.mActivePointerId = -1;
        setCanvas(GameMidlet.instance);
        int iLoadRMSInt = RMS.loadRMSInt("loadClearRMS");
        this.isClearAll = iLoadRMSInt;
        if (iLoadRMSInt == -1) {
            RMS.deleteAllRMS();
            RMS.saveRMSInt("loadClearRMS", 1);
        }
        new Thread(Res.load).start();
        Context = context;
        this.thread = new GThread(this);
        new Res();
        getLogo();
        coutPaintLogo = 0;
        isLoad = true;
        instance = this;
        if (isShow) {
            initTime();
        }
        System.gc();
        setFocusable(true);
        setFocusableInTouchMode(true);
        getHolder().addCallback(this);
        if (w < 200) {
            isSmallScreen = true;
        }
        isSmartPhone = true;
    }

    public void getTileFromRMS() {
        if (RMS.loadRMS(nameTile[0]) == null) {
            isLoadTileServer = true;
        }
    }

    public static Bitmap getLogo() {
        if (imgLogo == null) {
            if (GameMidlet.isKPAH) {
                imgLogo = Image.loadImageFromAsset("logo.png");
            } else {
                FilePack.init("/other");
                imgLogo = FilePack.getImg("logo1");
                FilePack.reset();
            }
        }
        return imgLogo;
    }

    public static void connect() {
        try {
            String str = ServerListScr.address[ServerListScr.index];
            short s = ServerListScr.port[ServerListScr.index];
            if (isBB) {
                if (isWifi) {
                    str = str + ";interface=wifi";
                } else {
                    str = str + ";deviceside=true";
                }
            }
            Session_ME.gI().connect(str, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        currentScreen = new Screen() {
        };
        gameScr = new GameScr();
        loginScr = new LoginScr();
        inputDlg = new InputDlg();
        autoTab = new BangAuto();
        shop = new ShopOnline();
        cmdNgatKetNoi = new Command("", new IAction() {
            @Override
            public void perform() {
                MoQuaScr.gI().resetGame();
                GCanvas.instance.init();
                Session_ME.gI().close();
                Tilemap.loadMap(0, null);
                ServerListScr.gI().switchToMe();
                GCanvas.endDlg();
                GCanvas.startOKDlg("Bị ngắt kết nối");
            }
        });
        Cout.println("tao login ");
    }

    public void setCanvas(Activity activity) {
        gCanvas = activity;
        getScreenSize();
    }

    public void getScreenSize() {
        int i;
        int i2;
        Display defaultDisplay = gCanvas.getWindowManager().getDefaultDisplay();
        this.display = defaultDisplay;
        w = defaultDisplay.getWidth();
        h = this.display.getHeight();
        if (GameMidlet.Model.compareTo("A101IT") == 0) {
            w -= 44;
        } else if (GameMidlet.Model.compareTo("GT-P7500") == 0) {
            h -= 48;
        }
        if (GameMidlet.Model.compareTo("HTC ChaCha A810e") == 0) {
            GameMidlet.isChaCha = true;
        }
        int i3 = h;
        if (i3 >= 1000 && (i2 = w) >= 1500) {
            screenlevel = 4;
            int i4 = i3 + (3 - (i3 % 3));
            h = i4;
            int i5 = i2 + (3 - (i4 % 3));
            w = i5;
            w = i5 / 4;
            h = i4 / 4;
        } else if ((i3 >= 720 && w >= 1200) || ((i = w) >= 960 && i3 >= 720)) {
            screenlevel = 3;
            int i6 = i3 + (3 - (i3 % 3));
            h = i6;
            int i7 = w + (3 - (i6 % 3));
            w = i7;
            w = i7 / 3;
            h = i6 / 3;
        } else if (i3 > 400 && i > 600) {
            screenlevel = 2;
            w = i / 2;
            h = i3 / 2;
        }
        hw = w / 2;
        hh = h / 2;
        Screen screen = currentScreen;
        if (screen != null) {
            LoginScr loginScr2 = loginScr;
            if (screen == loginScr2) {
                loginScr2.init();
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
            Res.init();
            Dialog dialog = currentDialog;
            InputDlg inputDlg2 = inputDlg;
            if (dialog == inputDlg2) {
                inputDlg2.init();
            }
        }
    }

    public static void initTime() {
        if (RMS.loadRMS("secondRemain") != null && RMS.loadRMSString("secondRemain") != "") {
            secondRemain = Integer.parseInt(RMS.loadRMSString("secondRemain"));
        } else {
            secondRemain = 10800;
        }
        currT = System.currentTimeMillis();
        lastT = System.currentTimeMillis();
        if (secondRemain <= 0) {
            startOKDlg_("Hôm nay bạn đã chơi đủ 3 tiếng, vui lòng quay lại vào ngày mai", false);
        }
        String strLoadRMSString = RMS.loadRMSString("lastDay");
        String str = Calendar.getInstance().get(6) + "";
        if (strLoadRMSString == null || strLoadRMSString == "" || strLoadRMSString.equals(str)) {
            return;
        }
        secondRemain = 10800;
    }

    public void updateTimeplay() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        currT = jCurrentTimeMillis;
        if (secondRemain == 0) {
            startOKDlg_("Hôm nay bạn đã chơi đủ 3 tiếng, vui lòng quay lại vào ngày mai", false);
            return;
        }
        if (jCurrentTimeMillis - lastT >= 1000) {
            lastT = System.currentTimeMillis();
            int i = secondRemain - 1;
            secondRemain = i;
            if (i % 3 == 0) {
                RMS.saveRMSString("secondRemain", secondRemain + "");
                RMS.saveRMSString("lastDay", Calendar.getInstance().get(6) + "");
            }
        }
    }

    public class GThread extends Thread {
        private SurfaceHolder mHolder;
        private boolean mRun = false;
        private GCanvas mn;

        public GThread(GCanvas gCanvas) {
            this.mn = gCanvas;
            this.mHolder = gCanvas.getHolder();
        }

        public void setRunning(boolean z) {
            this.mRun = z;
        }

        @Override
        public void run() {
            Looper.prepare();
            while (this.mRun) {
                long jCurrentTimeMillis = System.currentTimeMillis();
                if (GCanvas.isShow) {
                    GCanvas.this.updateTimeplay();
                }
                Canvas canvasLockCanvas = this.mHolder.lockCanvas();
                if (canvasLockCanvas != null) {
                    if (Tilemap.isLoadImageBigMapOk) {
                        for (int i = 0; i < Tilemap.totalImageMap.size(); i++) {
                            ImageMapBig imageMapBig = (ImageMapBig) Tilemap.totalImageMap.elementAt(i);
                            if (imageMapBig != null) {
                                imageMapBig.update();
                            }
                        }
                    }
                    GCanvas.gameTick++;
                    if (GCanvas.gameTick > 10000) {
                        GCanvas.gameTick = 0;
                    }
                    if (GCanvas.coutPaintLogo < 30) {
                        GCanvas.coutPaintLogo++;
                    }
                    if (GCanvas.menu.showMenu) {
                        GCanvas.menu.updateMenuKey();
                        GCanvas.menu.updateMenu();
                    }
                    if (GCanvas.currentDialog != null) {
                        GCanvas.currentDialog.update();
                    }
                    if (GCanvas.currentScreen != null) {
                        GCanvas.currentScreen.update();
                        if (!GCanvas.menu.showMenu && GCanvas.currentDialog == null) {
                            GCanvas.currentScreen.updateKey();
                        }
                    }
                    if (GCanvas.timeout > 0) {
                        GCanvas.timeout--;
                        if (GCanvas.timeout == 0 && GCanvas.currentDialog == null) {
                            GCanvas.timeoutDlg.show();
                        }
                    }
                    GCanvas.clearKeyPressed();
                    this.mn.onDraw(canvasLockCanvas);
                    this.mHolder.unlockCanvasAndPost(canvasLockCanvas);
                }
                long jCurrentTimeMillis2 = System.currentTimeMillis() - jCurrentTimeMillis;
                try {
                    if (jCurrentTimeMillis2 < 40) {
                        Thread.sleep(40 - jCurrentTimeMillis2);
                    } else {
                        Thread.sleep(1L);
                    }
                } catch (InterruptedException unused) {
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0) {
            isPointerDown = true;
            isPointerDownNj = true;
            actiondown(((int) motionEvent.getX()) / screenlevel, ((int) motionEvent.getY()) / screenlevel);
            px = ((int) motionEvent.getX()) / screenlevel;
            py = ((int) motionEvent.getY()) / screenlevel;
        } else if (action == 1) {
            isPointerDown = false;
            isPointerClick = true;
            isPointerDownItem = false;
            isPointerRelease = true;
            isPointerJustRelease = true;
            isPointerDownNj = false;
            px = -1;
            py = -1;
            actionup(((int) motionEvent.getX()) / screenlevel, ((int) motionEvent.getY()) / screenlevel);
        } else if (action == 2) {
            actionmove(((int) motionEvent.getX()) / screenlevel, ((int) motionEvent.getY()) / screenlevel);
        } else if (action == 5) {
            isHpUp = true;
            px = ((int) motionEvent.getX()) / screenlevel;
            py = ((int) motionEvent.getY()) / screenlevel;
        } else if (action == 6) {
            gameScr.btnFocusPotion = 0;
        } else if (action == 261) {
            int action2 = (motionEvent.getAction() & 65280) >> 8;
            isHpUp = true;
            px = ((int) motionEvent.getX(action2)) / screenlevel;
            py = ((int) motionEvent.getY(action2)) / screenlevel;
        } else if (action == 262) {
            gameScr.btnFocusPotion = 0;
        }
        return true;
    }

    public void actionup(int i, int i2) {
        px = i;
        py = i2;
        GameScr gameScr2 = gameScr;
        if (gameScr2 != null) {
            gameScr2.btnFocus = 0;
            gameScr.btnFocusPotion = 0;
        }
    }

    public void actionmove(int i, int i2) {
        px = i;
        py = i2;
    }

    public void actiondown(int i, int i2) {
        isPointerDownItem = true;
        px = i;
        py = i2;
        pxLast = i;
        pyLast = i2;
    }

    public void mapKeyPress(int i) {
        // mod: phím tắt PK (Mã pk, nháy map, bật đồ sát, gọi thú/pet)
        if (game.render.screen.AutoGuard.phimPk(i)) {
            return;
        }
        switch (i) {
            case 12:
                keyHold[12] = true;
                break;
            case 13:
                keyHold[13] = true;
                break;
            case 14:
                keyHold[14] = true;
                break;
            default:
                switch (i) {
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
                            keyHold[i - 48] = true;
                            break;
                        }
                        break;
                }
                break;
        }
    }

    public void mapKeyRelease(int i) {
        switch (i) {
            case 12:
                keyHold[12] = false;
                keyPressed[12] = true;
                break;
            case 13:
                keyHold[13] = false;
                keyPressed[13] = true;
                break;
            case 14:
                keyPressed[14] = true;
                break;
            default:
                switch (i) {
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
                        int i2 = i - 48;
                        keyHold[i2] = false;
                        keyPressed[i2] = true;
                        break;
                }
                break;
        }
        GameScr gameScr2 = gameScr;
        if (gameScr2 != null) {
            gameScr2.btnFocus = 0;
            gameScr.btnFocusPotion = 0;
        }
    }

    public void getFocusCavas() {
        try {
            requestFocus();
            setFocusable(true);
        } catch (Exception unused) {
            requestFocus();
            setFocusable(true);
        }
    }

    public static int dy() {
        return pyLast - py;
    }

    public static int dx() {
        return pxLast - px;
    }

    public static boolean canPutKey() {
        return Math.abs(px - pxLast) <= 10 && Math.abs(py - pyLast) <= 10;
    }

    public static boolean canNotPutKey() {
        return Math.abs(px - pxLast) > 10 || Math.abs(py - pyLast) > 10;
    }

    public static boolean beginMoveCmrY() {
        return Math.abs(py - pyLast) >= 10;
    }

    public static boolean beginMoveCmrX() {
        return Math.abs(px - pxLast) >= 10;
    }

    @Override
    public void onDraw(Canvas canvas) {
        g.setCanvas(canvas);
        int i = screenlevel;
        if (i == 1) {
            paint(g);
        } else {
            canvas.scale(i, i);
            paint(g);
        }
    }

    public static void resetTrans(Graphics graphics) {
        graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
        graphics.setClip(0, 0, w, h);
    }

    public static void resetTransAndroid9(Graphics graphics) {
        graphics.ClipRec(0, 0, w, h);
        graphics.restoreCanvas();
    }

    protected void paint(Graphics graphics) {
        Screen screen = currentScreen;
        if (screen != null) {
            screen.paint(graphics);
        }
        Dialog dialog = currentDialog;
        if (dialog != null) {
            dialog.paint(graphics);
        }
        if (menu.showMenu) {
            menu.paintMenu(graphics);
        }
        if (coutPaintLogo < 30) {
            resetTrans(graphics);
            graphics.setColor(0);
            graphics.fillRect(0, 0, w, h);
            graphics.drawImage(imgLogo, w / 2, h / 2, Graphics.VCENTER | Graphics.HCENTER);
        }
        if (!Tilemap.isLoadImageBigMapOk && currentScreen != ChangScr.gI()) {
            graphics.setColor(0);
            graphics.fillRect(0, 0, w, h);
            Bitmap bitmap = imgLogo;
            if (bitmap != null) {
                graphics.drawImage(bitmap, w / 2, h / 2, Graphics.VCENTER | Graphics.HCENTER);
            }
        }
        GameScr.paint12plus(graphics);
        AutoGuard.paint(graphics); // mod: nút UP + thanh trạng thái các auto
    }

    public static void endDlg() {
        currentDialog = null;
    }

    public static void startOKDlg(String str, boolean z) {
        // mod: auto đọc kết quả / nuốt thông báo của server
        if (AutoGuard.autoEatDlg(str)) {
            return;
        }
        msgdlg.isIcon = z;
        msgdlg.setInfo(str, (Command) null, new Command("OK", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
            }
        }), (Command) null);
        msgdlg.show();
    }

    public static void startOKDlg_(String str, boolean z) {
        msgdlg.isIcon = z;
        msgdlg.setInfo(str, (Command) null, new Command("OK", new IAction() {
            @Override
            public void perform() {
                GameMidlet.destroy();
            }
        }), (Command) null);
        msgdlg.show();
    }

    public static void startOKDlg(String[] strArr) {
        startOKDlg(strArr, false);
    }

    public static void startOKDlg(String[] strArr, boolean z) {
        msgdlg.isIcon = z;
        msgdlg.setInfo(strArr, new Command("OK", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
            }
        }), (Command) null, (Command) null);
        msgdlg.show();
    }

    public static void startOKDlg(String str) {
        startOKDlg(str, false);
    }

    public static void startYesNoDlgPutArr(String str, String[] strArr, IAction iAction, IAction iAction2) {
        msgdlg.isIcon = false;
        msgdlg.setInfoArr(str, strArr, new Command("Có", iAction), new Command("", iAction), new Command("Không", iAction2));
        msgdlg.show();
    }

    public static void startYesNoDlg(String str, IAction iAction, IAction iAction2) {
        msgdlg.isIcon = false;
        msgdlg.setInfo(str, new Command("Có", iAction), new Command("", iAction), new Command("Không", iAction2));
        msgdlg.show();
    }

    public static void startWaitDlg(String str, boolean z) {
        msgdlg.isIcon = z;
        msgdlg.setInfo(str, (Command) null, new Command("Cancel", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
            }
        }), (Command) null);
        msgdlg.show();
    }

    public static void startWaitDlg() {
        msgdlg.isIcon = false;
        msgdlg.setInfo("Xin chờ...", (Command) null, new Command("Cancel", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
            }
        }), (Command) null);
        msgdlg.show();
    }

    public static void startOKDlg(String str, boolean z, int i) {
        timeoutDlg.isIcon = z;
        timeoutDlg.setInfo(str, (Command) null, new Command("OK", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
            }
        }), (Command) null);
        timeout = i;
    }

    public static void startOKDlg(String str, IAction iAction) {
        msgdlg.isIcon = false;
        msgdlg.setInfo(str, (Command) null, new Command("OK", iAction), (Command) null);
        msgdlg.show();
    }

    public static void startYesNoDlg(String str, IAction iAction) {
        msgdlg.isIcon = false;
        msgdlg.setInfo(str, new Command("Có", iAction), new Command("", iAction), new Command("Không", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
            }
        }));
        msgdlg.show();
    }

    public static boolean isKeyPressed(int i) {
        boolean[] zArr = keyPressed;
        if (!zArr[i]) {
            return false;
        }
        zArr[i] = false;
        return true;
    }

    public static String getMoneys(long j) {
        long j2 = (j / 1000) + 1;
        String str = "";
        for (int i = 0; i < j2; i++) {
            if (j >= 1000) {
                long j3 = j % 1000;
                if (j3 == 0) {
                    str = ".000" + str;
                } else if (j3 < 10) {
                    str = ".00" + j3 + str;
                } else if (j3 < 100) {
                    str = ".0" + j3 + str;
                } else {
                    str = "." + j3 + str;
                }
                j /= 1000;
            } else {
                return j + str;
            }
        }
        return str;
    }

    public static int collisionCmdBar() {
        if (isPointer(0, h - 35, 70, 45)) {
            return 0;
        }
        if (isPointer(hw - 40, h - 35, 80, 35)) {
            return 1;
        }
        return isPointer(w - 70, h - 35, 70, 40) ? 2 : -1;
    }

    public static boolean isPointer(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        return (isPointerDown || isPointerClick) && (i5 = px) >= i && i5 <= i + i3 && (i6 = py) >= i2 && i6 <= i2 + i4;
    }

    public static String getPriceMoney(long j, int i) {
        String str = "";
        if (j > 0) {
            str = "" + getMoneys(j) + "Xu";
        }
        if (i <= 0) {
            return str;
        }
        if (j > 0) {
            str = str + " - ";
        }
        return str + getMoneys(i) + "L";
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (this.thread.isAlive()) {
            return;
        }
        GThread gThread = new GThread(this);
        this.thread = gThread;
        gThread.setRunning(true);
        this.thread.start();
        Cout.println("Chay 111");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (this.thread.isAlive()) {
            this.thread.setRunning(false);
            Cout.println("Chay 222");
        }
    }

    public static void clearKeyHold() {
        isPointerRelease = false;
        for (int i = 0; i < 14; i++) {
            keyHold[i] = false;
        }
    }

    public static void clearKeyPressed() {
        isPointerRelease = false;
        isPointerClick = false;
        isPointerJustRelease = false;
        for (int i = 0; i < 14; i++) {
            keyPressed[i] = false;
        }
    }

    public static boolean isPoint(int i, int i2, int i3, int i4) {
        int i5;
        int i6 = px;
        return i6 >= i && i6 <= i + i3 && (i5 = py) >= i2 && i5 <= i2 + i4;
    }

    public int focusBtnBar() {
        if (pxLast - px <= 10 && pyLast - py <= 10 && !isPointerClick) {
            if (isPointer(0, h - 40, 70, 40)) {
                return 1;
            }
            if (isPointer(hw - 40, h - 30, 80, 35)) {
                return 2;
            }
            if (isPointer(w - 70, h - 40, 70, 40)) {
                return 3;
            }
        }
        return 0;
    }

    public static String formatNumberDotK(int i) {
        String str = "";
        String str2 = "" + i;
        if (str2.length() <= 3) {
            return str2;
        }
        for (int length = str2.length() - 1; length >= 0; length--) {
            if ((str2.length() - length) % 3 == 0 && length > 0) {
                str = str + str2.charAt(length) + ".";
            } else {
                str = str + str2.charAt(length);
            }
        }
        return new StringBuffer(str).reverse().toString();
    }

    public static boolean isPointerNj(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        return (isPointerDownNj || isPointerJustRelease) && (i5 = px) >= i && i5 <= i + i3 && (i6 = py) >= i2 && i6 <= i2 + i4;
    }

    public static boolean isPointerHoldIn(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        return (isPointerDownNj || isPointerJustRelease) && (i5 = px) >= i && i5 <= i + i3 && (i6 = py) >= i2 && i6 <= i2 + i4;
    }

    public static int random(int i, int i2) {
        return i + r.nextInt(i2 - i);
    }

    public static boolean canTouch() {
        return Math.abs(px - pxLast) < 10 && Math.abs(py - pyLast) < 10;
    }
}
