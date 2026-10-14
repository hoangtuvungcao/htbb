package game.render.screen;

import com.teamobi.kpah.GameMidlet;
import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.model.Scroll;
import game.model.ScrollResult;
import game.model.Tilemap;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerListScr extends Screen {
    public static short Port = 0;
    public static int cmdy = 0;
    public static int cmtoY = 0;
    public static int cmvy = 0;
    public static int cmy = 0;
    public static int cmyLim = 0;
    public static int index = -1;
    private static ServerListScr me;
    private int goc;
    public boolean isAction;
    int pa;
    int timeClick;
    public int timeDeplay;
    boolean trans;
    private int xTo;
    private int yTo;
    public static String[] nameServer = {"Cổ loa(Mới)", "Phú xuân", "Đại la", "Tây đô"};
    public static String[] address = {"27.0.12.112", "112.213.94.124", "112.213.94.124", "112.213.94.124", "112.213.94.124", "112.213.94.124"};
    public static byte[] index_server = {0, 0, 0, 0, 3, 4, 1, 2};
    public static short[] port = {19129, 19130, 19129, 19129, 19129};
    public static String linkGetHost = "http://teamobi.com/srvips/ngude2.txt";
    public static String nameSv = "";
    public static String Ip = "";
    private int radius = 200;
    private int w = 135;
    private int h = 91;
    int dem = 0;
    int saveIndex = -1;
    public Scroll myScroll = new Scroll();

    public static ServerListScr gI() {
        ServerListScr serverListScr = me;
        if (serverListScr != null) {
            return serverListScr;
        }
        ServerListScr serverListScr2 = new ServerListScr();
        me = serverListScr2;
        return serverListScr2;
    }

    public static void loadSv(boolean z) {
        // mod: danh sách server NQSH-H2 (NqshMod: server riêng, tải danh sách mới từ GitHub)
        NqshMod.apDungServer();
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        this.myScroll.clear();
        GCanvas.gameScr.loadCamera();
        updateCamera();
        init();
    }

    public ServerListScr() {
        loadIP();
        this.left = new Command("Cập nhật", new IAction() {
            @Override
            public void perform() {
                ServerListScr.this.doUpdateServer();
            }
        });
        this.right = new Command("Thoát", new IAction() {
            @Override
            public void perform() {
                GameMidlet.instance.notifyDestroyed();
            }
        });
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                ServerListScr.this.saveIndex = ServerListScr.index;
                if (ServerListScr.this.saveIndex != -1) {
                    GCanvas.loginScr.switchToMe();
                }
                ServerListScr.this.saveIndex = -1;
            }
        });
    }

    @Override
    public void init() {
        GameScr.cmx = GameScr.cmtoX;
        GameScr.cmy = GameScr.cmtoY;
        int length = (nameServer.length * 25) - this.h;
        cmyLim = length;
        cmtoY = 0;
        cmy = 0;
        index = -1;
        this.saveIndex = -1;
        if (length < 0) {
            cmyLim = 0;
        }
        GCanvas.instance.getFocusCavas();
    }

    public void doUpdateServer() {
        // mod: nút "Cập nhật" = kiểm tra bản mới + tải lại danh sách server (NqshMod)
        NqshMod.capNhat();
    }

    public static void saveIP() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeByte(nameServer.length);
            int i = 0;
            while (true) {
                String[] strArr = nameServer;
                if (i < strArr.length) {
                    dataOutputStream.writeUTF(strArr[i]);
                    dataOutputStream.writeUTF(address[i]);
                    dataOutputStream.writeShort(port[i]);
                    dataOutputStream.writeByte(index_server[i]);
                    i++;
                } else {
                    RMS.saveRMS("ipnqsh", byteArrayOutputStream.toByteArray());
                    dataOutputStream.close();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadIP() {
        byte[] bArrLoadRMS = RMS.loadRMS("ipnqsh");
        if (bArrLoadRMS == null) {
            return;
        }
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArrLoadRMS));
        try {
            int i = dataInputStream.readByte();
            nameServer = new String[i];
            address = new String[i];
            port = new short[i];
            index_server = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                nameServer[i2] = dataInputStream.readUTF();
                address[i2] = dataInputStream.readUTF();
                port[i2] = dataInputStream.readShort();
                index_server[i2] = dataInputStream.readByte();
            }
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        int i = cmy;
        int i2 = cmtoY;
        if (i != i2) {
            int i3 = (i2 - i) << 2;
            cmvy = i3;
            int i4 = cmdy + i3;
            cmy = i + (i4 >> 4);
            cmdy = i4 & 15;
        }
        updateCamera();
    }

    public boolean isPointer(int i, int i2, int i3, int i4) {
        return GCanvas.px >= i && GCanvas.px <= i + i3 && GCanvas.py >= i2 && GCanvas.py <= i2 + i4;
    }

    @Override
    public void updateKey() {
        boolean moved = false;
        if (GCanvas.isKeyPressed(2)) {
            int i3 = index - 1;
            index = i3;
            if (i3 < 0) {
                index = nameServer.length - 1;
            }
            moved = true;
        } else if (GCanvas.isKeyPressed(8)) {
            int i4 = index + 1;
            index = i4;
            if (i4 >= nameServer.length) {
                index = 0;
            }
            moved = true;
        }
        if (moved) {
            this.myScroll.cmtoY = (index * 30) - ((this.h - 10) / 2);
            if (this.myScroll.cmtoY < 0) {
                this.myScroll.cmtoY = 0;
            }
            if (this.myScroll.cmtoY > this.myScroll.cmyLim) {
                Scroll scroll = this.myScroll;
                scroll.cmtoY = scroll.cmyLim;
            }
        }
        super.updateKey();
        ScrollResult scrollResultUpdateKey = this.myScroll.updateKey();
        if (scrollResultUpdateKey.isDowning || scrollResultUpdateKey.isFinish) {
            index = scrollResultUpdateKey.selected;
        }
        if (isPointer(GCanvas.hw - 100, GCanvas.hh - 60, 200, 160) && GCanvas.isPointerClick) {
            GCanvas.isPointerClick = false;
            if (index != -1 && this.center != null) {
                this.saveIndex = index;
                this.isAction = true;
                this.timeDeplay = 7;
            }
        }
        if (this.isAction) {
            int i = this.timeDeplay;
            if (i > 0) {
                int i2 = i - 1;
                this.timeDeplay = i2;
                if (i2 <= 0) {
                    this.timeDeplay = 0;
                    this.isAction = false;
                    if (this.center != null) {
                        this.saveIndex = index;
                        this.center.action.perform();
                    }
                }
            }
        }
        this.myScroll.updatecm();
    }

    public void updateCamera() {
        int i = this.goc + 1;
        this.goc = i;
        if (i > 360) {
            this.goc = 0;
        }
        this.xTo = (Util.cos(this.goc) * this.radius) >> 10;
        this.yTo = (Util.sin(this.goc) * this.radius) >> 10;
        GameScr.cmtoX = this.xTo + 380;
        GameScr.cmtoY = this.yTo + 380;
        GCanvas.gameScr.updateCamera();
    }

    public void paintBg(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        graphics.translate(-GameScr.cmx, -GameScr.cmy);
        Tilemap.paintTile(graphics);
        Tilemap.paintTileTop(graphics);
    }

    @Override
    public void paint(Graphics graphics) {
        paintBg(graphics);
        GCanvas.resetTrans(graphics);
        graphics.drawImage(GCanvas.getLogo(), GCanvas.hw, (GCanvas.hh - 80) + (GameMidlet.isKPAH ? 40 : 30), Graphics.BOTTOM | Graphics.HCENTER);
        paintPanel(graphics);
        super.paint(graphics);
    }

    private void paintPanel(Graphics graphics) {
        Res.paintDlgDragonFull(graphics, GCanvas.hw - 80, GCanvas.hh - 40, 160, 130);
        graphics.setClip(GCanvas.hw - 100, GCanvas.hh - 36, this.w, this.h - 7);
        graphics.ClipRec(GCanvas.hw - 100, GCanvas.hh - 33, 200, 120);
        graphics.translate(0, -cmy);
        GCanvas.resetTrans(graphics);
        this.myScroll.setStyle(nameServer.length + 1, 30, GCanvas.hw - 100, GCanvas.hh - 30, 200, 140, true, 1);
        this.myScroll.setClip(graphics, GCanvas.hw - 100, GCanvas.hh - 33, 200, 120);
        for (int i = 0; i < nameServer.length; i++) {
            if (index == i) {
                graphics.setColor(-4868426);
                graphics.fillRect(GCanvas.hw - 75, (GCanvas.hh - 38) + (index * 30) + 5, 151, 30);
                graphics.setColor(-16742267);
                graphics.fillRect(GCanvas.hw - 74, (GCanvas.hh - 37) + (index * 30) + 5, 149, 28);
            }
            Font.normalFont[0].drawString(graphics, nameServer[i], GCanvas.hw, (i * 30) + (GCanvas.hh - 26), 2);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    public static void close() {
        me = null;
    }
}
