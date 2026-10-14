package game.render.screen;

import game.GameMidlet;
import game.model.Command;
import game.model.IAction;
import game.model.Tilemap;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;

public class ServerListScr extends Screen {
    private static ServerListScr me;
    private int goc;
    private int xTo;
    private int yTo;
    private int radius = 200;
    private int w = 135;
    private int h = 91;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int index;
    static String[] nameServer = {"Vạn Xuân(mới)", "Cổ Loa", "Thăng Long", "Hoa Lư", "Kinh Môn", "Hoàng Vương", "Mỹ Sơn", "Tràng An", "Tiên La"};
    public static String[] address = {"112.213.84.163", "27.0.12.112", "103.77.167.209", "103.77.167.209", "112.213.94.124", "112.213.94.124", "112.213.94.124", "112.213.94.124", "112.213.94.124"};
    public static short[] port = {19129, 19130, 19129, 19129, 19129, 19129, 19129, 19129, 19129};
    public static byte[] index_server = {0, 0, 0, 0, 0, 3, 4, 1, 2};
    public static final String linkGetHost = "http://teamobi.com/srvips/NQSH2.txt";
    boolean trans;
    int pa;

    public static ServerListScr gI() {
        if (me != null) {
            return me;
        }
        ServerListScr serverListScr = new ServerListScr();
        me = serverListScr;
        return serverListScr;
    }

    @Override
    public void switchToMe() {
        // mod: danh sách server từ GitHub (TaiDanhSach) khi mở màn chọn server
        if (AutoGuard.dsGuard()) {
            return;
        }
        super.switchToMe();
        Canvas.gameScr.loadCamera();
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
        this.center = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                if (ServerListScr.index != -1) {
                    Canvas.loginScr.switchToMe();
                }
            }
        });
    }

    @Override
    public void init() {
        GameScr.cmx = GameScr.cmtoX;
        GameScr.cmy = GameScr.cmtoY;
        cmtoY = 0;
        cmy = 0;
        index = 0;
        cmyLim = (nameServer.length * 16) - (this.h - 10);
        if (cmyLim < 0) {
            cmyLim = 0;
        }
    }

    public void doUpdateServer() {
        // mod: nút "Cập nhật" danh sách server
        if (AutoGuard.capNhatGuard()) {
            return;
        }
        Canvas.startWaitDlg();
        String aa = GameMidlet.connectHTTP("http://teamobi.com/srvips/NQSH2.txt");
        if (aa == null) {
            Canvas.startOKDlg("Không thể kết nối, xin kiểm tra lại GPRS/3G/Wifi.");
            return;
        }
        String[] temp = Font.splitString(aa, ",");
        nameServer = new String[temp.length];
        address = new String[temp.length];
        port = new short[temp.length];
        index_server = new byte[temp.length];
        for (int i = 0; i < temp.length; i++) {
            String[] sub = Font.splitString(temp[i], ":");
            nameServer[i] = sub[0];
            address[i] = sub[1];
            port[i] = Short.parseShort(sub[2].trim());
            index_server[i] = Byte.parseByte(sub[3].trim());
        }
        saveIP();
        init();
        Canvas.endDlg();
    }

    public static void saveIP() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bo);
        try {
            dos.writeByte(nameServer.length);
            for (int i = 0; i < nameServer.length; i++) {
                dos.writeUTF(nameServer[i]);
                dos.writeUTF(address[i]);
                dos.writeShort(port[i]);
                dos.writeByte(index_server[i]);
            }
            RMS.saveRMS("ipnqsh", bo.toByteArray());
            dos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void loadIP() {
        byte[] bData = RMS.loadRMS("ipnqsh");
        if (bData == null) {
            return;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(bData);
        DataInputStream dis = new DataInputStream(bi);
        if (dis == null) {
            return;
        }
        try {
            int i = dis.readByte();
            nameServer = new String[i];
            address = new String[i];
            port = new short[i];
            index_server = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                nameServer[i2] = dis.readUTF();
                address[i2] = dis.readUTF();
                port[i2] = dis.readShort();
                index_server[i2] = dis.readByte();
            }
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
        updateCamera();
    }

    @Override
    public void updateKey() {
        boolean changeFocus = false;
        if (Canvas.isKeyPressed(2)) {
            index--;
            if (index < 0) {
                index = nameServer.length - 1;
            }
            changeFocus = true;
        } else if (Canvas.isKeyPressed(8)) {
            index++;
            if (index >= nameServer.length) {
                index = 0;
            }
            changeFocus = true;
        }
        if (changeFocus) {
            cmtoY = (index * 16) - ((this.h - 10) / 2);
            if (cmtoY < 0) {
                cmtoY = 0;
            }
            if (cmtoY > cmyLim) {
                cmtoY = cmyLim;
            }
        }
        super.updateKey();
        if (Canvas.isPointerDown && Canvas.isPointer(Canvas.hw - 61, Canvas.hh - 19, 120, 100)) {
            if (!this.trans) {
                this.pa = cmy;
                this.trans = true;
            }
            int a = ((cmtoY + Canvas.py) - (Canvas.hh - 20)) / 16;
            if (a < 0) {
                a = 0;
            }
            if (a > nameServer.length - 1) {
                a = nameServer.length - 1;
            }
            index = a;
            if (Math.abs(Canvas.pyLast - Canvas.py) != 0) {
                cmtoY = this.pa + (Canvas.pyLast - Canvas.py);
                if (cmtoY < 0) {
                    cmtoY = 0;
                }
                if (cmtoY > cmyLim) {
                    cmtoY = cmyLim;
                }
                index = -1;
            }
        }
        if (Canvas.isPointerClick) {
            this.trans = false;
            Canvas.isPointerClick = false;
            if (isPointer(Canvas.hw - 61, Canvas.hh - 19, 120, 100)) {
                int a2 = ((cmtoY + Canvas.py) - (Canvas.hh - 20)) / 16;
                if (a2 < 0) {
                    a2 = 0;
                }
                if (a2 > nameServer.length - 1) {
                    a2 = nameServer.length - 1;
                }
                if (Math.abs(Canvas.pyLast - Canvas.py) < 10) {
                    index = a2;
                    if (cmy == cmtoY && index != -1 && this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
        }
    }

    public boolean isPointer(int x, int y, int w, int h) {
        if (Canvas.px >= x && Canvas.px <= x + w && Canvas.py >= y && Canvas.py <= y + h) {
            return true;
        }
        return false;
    }

    public void updateCamera() {
        this.goc++;
        if (this.goc > 360) {
            this.goc = 0;
        }
        this.xTo = (Util.cos(this.goc) * this.radius) >> 10;
        this.yTo = (Util.sin(this.goc) * this.radius) >> 10;
        GameScr.cmtoX = this.xTo + 380;
        GameScr.cmtoY = this.yTo + 380;
        Canvas.gameScr.updateCamera();
    }

    public void paintBg(Graphics g) {
        Canvas.resetTrans(g);
        g.translate(-GameScr.cmx, -GameScr.cmy);
        Tilemap.paintTile(g);
        Tilemap.paintTileTop(g);
    }

    @Override
    public void paint(Graphics g) {
        paintBg(g);
        Canvas.resetTrans(g);
        g.drawImage(Canvas.getLogo(), Canvas.hw, Canvas.hh - 70, 3);
        paintPanel(g);
        super.paint(g);
    }

    private void paintPanel(Graphics g) {
        Res.paintDlgDragonFull(g, Canvas.hw - 70, (Canvas.hh - 50) + 20, 140, 100);
        g.setClip(Canvas.hw - 69, Canvas.hh - 28, this.w, this.h);
        g.translate(0, -cmy);
        if (index != -1) {
            g.setColor(11908790);
            g.fillRect(Canvas.hw - 61, (Canvas.hh - 21) + (index * 16), 122, 18);
            g.setColor(34949);
            g.fillRect(Canvas.hw - 60, (Canvas.hh - 20) + (index * 16), 120, 16);
        }
        for (int i = 0; i < nameServer.length; i++) {
            Font.normalFont[0].drawString(g, nameServer[i], Canvas.hw, (Canvas.hh - 19) + (i * 16), 2);
        }
    }

    public static void close() {
        me = null;
    }
}
