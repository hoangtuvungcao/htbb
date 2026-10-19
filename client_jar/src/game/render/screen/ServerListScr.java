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
        // mod: danh sách server = máy chủ gắn lúc build (TaiDanhSach) khi mở màn chọn server
        if (AutoGuard.dsGuard()) {
            return;
        }
        super.switchToMe();
        Canvas.gameScr.loadCamera();
        updateCamera();
        init();
        game.pokemon.AmThanh.manTieuDe();   // pokemon: nhac tieu de
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
        AutoGuard.capNhatGuard();
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
        if (game.pokemon.KhungVao.moi()) {
            return;   // bảng mới: chạm qua VungCham (hàng / nút), không dùng phép thử chạm cũ
        }
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
        paintBg(g, -1, true);
    }

    /** pokemon: nền banner nhiều lớp (game.pokemon.Banner) thay bản đồ game; thiếu ảnh thì vẽ bản đồ như cũ. */
    public void paintBg(Graphics g, int chonKhoiDau, boolean veKhoiDau) {
        Canvas.resetTrans(g);
        if (game.pokemon.Banner.ve(g, chonKhoiDau, veKhoiDau)) {
            Canvas.resetTrans(g);
            return;
        }
        g.translate(-GameScr.cmx, -GameScr.cmy);
        Tilemap.paintTile(g);
        Tilemap.paintTileTop(g);
    }

    private IAction viecChon;

    /** 0.9.5 PC / APK: bảng sáng chung, hàng máy chủ (tên + trạng thái) chạm để chọn, nút "Vào máy chủ" trong bảng. */
    private void veMoi(Graphics g) {
        this.anNutLenh = true;
        if (this.viecChon == null) {
            this.viecChon = new IAction() {
                public void perform() {
                    if (ServerListScr.index != -1) {
                        Canvas.loginScr.switchToMe();
                    }
                }
            };
        }
        paintBg(g);
        int n = nameServer == null ? 0 : nameServer.length;
        int hs = n > 4 ? 4 : n < 1 ? 1 : n;
        int hb = game.pokemon.KhungVao.caoNut(), k = game.pokemon.KhungVao.khe();
        int hh = Math.max(hb + 8, game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN) + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + 10);
        int hc = game.pokemon.ChuDe.caoDau() + 2 * game.pokemon.ChuDe.DEM + hs * (hh + k) + hb;
        int[] r = game.pokemon.KhungVao.logoVaBang(g, 300, hc, hc, 150);
        int[] c = game.pokemon.Bang.khung(g, r[0], r[1], r[2], r[3], "Chọn máy chủ", null, 0, null, null);
        int x = c[0], w = c[2], y = c[1];
        for (int i = 0; i < n && i < hs; i++) {
            final int j = i;
            game.pokemon.Bang.hang(g, x, y, w, hh, i, i == index, new IAction() {
                public void perform() {
                    if (ServerListScr.index == j) {
                        Canvas.loginScr.switchToMe();
                    } else {
                        ServerListScr.index = j;
                    }
                }
            }, "may-chu-" + i);
            game.pokemon.GiaoDien.pokeBall(g, x + 16, y + hh / 2, 7, 0);
            String ten = TaiDanhSach.tenHien(nameServer[i], i);
            boolean thu = ten.indexOf("Thử nghiệm") >= 0;
            int ty = y + (hh - game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN) - game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU)) / 2;
            game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat(ten, game.pokemon.ChuDe.NHAN, w - 40), x + 30, ty, game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 0);
            game.pokemon.ChuDe.chu(g, thu ? "Bản thử nghiệm nội bộ" : "Máy chủ chính thức", x + 30, ty + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN),
                    game.pokemon.ChuDe.PHU, thu ? game.pokemon.ChuDe.CAM : game.pokemon.ChuDe.LA_TOI, 0);
            y += hh + k;
        }
        if (n == 0) {
            game.pokemon.ChuDe.chuGiua(g, "Chưa có máy chủ", x + w / 2, y, hh, game.pokemon.ChuDe.THUONG, game.pokemon.ChuDe.CHU_PHU, 2);
            y += hh + k;
        }
        game.pokemon.Bang.nut(g, x, c[1] + c[3] - hb, w, hb, "Vào máy chủ", game.pokemon.Bang.CHINH, this.viecChon, "chon-may-chu");
    }

    @Override
    public void paint(Graphics g) {
        if (game.pokemon.KhungVao.moi()) {
            veMoi(g);
            return;
        }
        paintBg(g);
        Canvas.resetTrans(g);
        // pokemon: tieu de ThuongHieu (ten game + 5 Pokemon khoi dau) thay logo game goc
        if (game.pokemon.Banner.co()) {
            game.pokemon.ThuongHieu.veLogo(g, Canvas.hw, Math.max(6, Canvas.hh - 100), Canvas.w);
        } else if (!game.pokemon.ThuongHieu.veTieuDe(g, Canvas.hw, Math.max(14, Canvas.hh - 125), Canvas.w, Canvas.hh - 56)) {
            g.drawImage(Canvas.getLogo(), Canvas.hw, Canvas.hh - 70, 3);
        }
        paintPanel(g);
        super.paint(g);
    }

    private void paintPanel(Graphics g) {
        // pokemon: hộp thoại trắng + dải đỏ "Chọn máy chủ", máy chủ đang chọn nền vàng + mũi tên
        int hDs = nameServer == null ? 1 : nameServer.length;
        if (hDs > 6) {
            hDs = 6;
        }
        if (hDs < 2) {
            hDs = 2;
        }
        game.pokemon.GiaoDien.theMo(g, Canvas.hw - 72, Canvas.hh - 52, 144, 36 + hDs * 16);
        game.pokemon.GiaoDien.dai(g, Canvas.hw - 69, Canvas.hh - 49, 138, 18, "Chọn máy chủ");
        g.setClip(Canvas.hw - 69, Canvas.hh - 28, this.w, this.h);
        g.translate(0, -cmy);
        if (index != -1) {
            game.pokemon.GiaoDien.chonSang(g, Canvas.hw - 64, (Canvas.hh - 22) + (index * 16), 128, 17);
            game.pokemon.GiaoDien.muiTen(g, Canvas.hw - 60, (Canvas.hh - 17) + (index * 16), 6, game.pokemon.GiaoDien.VIEN);
        }
        for (int i = 0; i < nameServer.length; i++) {
            if (i != index) {
                game.pokemon.GiaoDien.pokeBall(g, Canvas.hw - 56, (Canvas.hh - 13) + (i * 16), 4, 0);
            }
            Font.blackFont.drawString(g, TaiDanhSach.tenHien(nameServer[i], i), Canvas.hw + 4, (Canvas.hh - 20) + (i * 16), 2);   // pokemon: an ten game cu
        }
    }

    public static void close() {
        me = null;
    }
}
