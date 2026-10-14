package game.render.screen;

import com.teamobi.kpah.GameMidlet;
import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import network.Session_ME;

public class LoginScr extends Screen {
    private Command cmdLogin;
    private Command cmdRemem;
    int defYL;
    int focus;
    public boolean isDangNhap;
    public TField tfPass;
    public TField tfUser;
    int xTo;
    int yL;
    int yTo;
    public boolean isRememPass = true;
    int dem = 0;
    public String user = "";
    public String pass = "";
    int centerX = 160;
    int centerY = 160;
    int radius = 128;
    int goc = 0;
    int index = 0;
    int tick = 0;
    int wC = 110;
    int xC = GCanvas.hw - (this.wC / 2);

    @Override
    public void init() {
        this.wC = 110;
        int i = GCanvas.hw;
        int i2 = this.wC;
        this.xC = i - (i2 / 2);
        this.tfUser.width = i2;
        this.tfUser.y = GCanvas.hh - 18;
        this.tfPass.y = GCanvas.hh + 21;
        this.tfPass.width = this.wC;
        this.tfUser.x = this.xC;
        this.tfPass.x = this.xC;
    }

    public LoginScr() {
        TField tField = new TField();
        this.tfUser = tField;
        tField.height = 21;
        this.tfUser.isFocus = true;
        this.tfUser.setIputType(3);
        this.tfUser.setText("");
        TField tField2 = new TField();
        this.tfPass = tField2;
        tField2.height = 21;
        this.tfPass.isFocus = false;
        this.tfPass.setIputType(2);
        this.tfPass.setText("");
        loadPass();
        init();
        Command command = new Command(GCanvas.w > 200 ? "Đăng nhập" : "Đ.Nhập", new IAction() {
            @Override
            public void perform() {
                LoginScr.this.doLogin();
            }
        });
        this.cmdLogin = command;
        this.center = command;
        this.cmdRemem = new Command("Nhớ", new IAction() {
            @Override
            public void perform() {
                LoginScr loginScr = LoginScr.this;
                loginScr.isRememPass = !loginScr.isRememPass;
            }
        });
        this.left = new Command("Máy chủ", new IAction() {
            @Override
            public void perform() {
                ServerListScr.gI().switchToMe();
            }
        });
        this.right = new Command("Đăng ký", new IAction() {
            @Override
            public void perform() {
                LoginScr.this.doRegister();
            }
        });
        GameMidlet.numberSupport = loadnumber();
    }

    public static void doautoLogin() {
        String user = "";
        String pass = "";
        boolean saved = false;
        byte[] data = RMS.loadRMS("nqshlogin");
        if (data == null) {
            return;
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        try {
            saved = dis.readBoolean();
            if (saved) {
                user = dis.readUTF();
                pass = dis.readUTF();
            }
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!user.equals("") && !pass.equals("") && saved) {
            doLoginAuto(user, pass);
        }
    }

    String loadnumber() {
        String strLoadRMSString = RMS.loadRMSString("numbersupport");
        return strLoadRMSString == null ? GameMidlet.numberSupport : strLoadRMSString;
    }

    public boolean doShowAgent(String str) {
        return str.equals("showagentpro");
    }

    public void doLogin() {
        this.user = this.tfUser.getText().toLowerCase().trim();
        this.pass = this.tfPass.getText();
        if (doShowAgent(this.user)) {
            GCanvas.startOKDlg("Agent: " + GameMidlet.agent + " Pro: " + GameMidlet.provider + "ClinenPro: " + GameMidlet.client_Provider + (GameMidlet.isLoadAgentPro ? " Ban trang " : " Ban thuong ") + GameMidlet.coutVerSion);
            return;
        }
        if (this.user.equals("")) {
            return;
        }
        if (this.pass.equals("")) {
            this.focus = 1;
            this.tfUser.isFocus = false;
            this.tfPass.isFocus = true;
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException unused) {
                    }
                    GCanvas.connect();
                    GameService.gI().setClientType();
                    GameService.gI().login(LoginScr.this.user, LoginScr.this.pass, GameMidlet.version);
                    GameScr.saveAutoFight = false;
                    GameScr.iTaskAuto = 0;
                }
            }).start();
            GCanvas.startWaitDlg("Đang kết nối", true);
            GameScr.reSetQuest();
        }
    }

    public static void doLoginAuto(final String str, final String str2) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException unused) {
                }
                GCanvas.connect();
                GameService.gI().setClientType();
                GameService.gI().login(str, str2, GameMidlet.version);
                GameScr.saveAutoFight = false;
            }
        }).start();
        GCanvas.startWaitDlg("Đang kết nối", true);
    }

    public void saveAutoLogin() {
        if (this.isRememPass) {
            RMS.saveRMSInt("index", ServerListScr.index);
            RMS.saveRMSString("userAuto", this.user);
            RMS.saveRMSString("passAuto", this.pass);
            RMS.saveRMSInt("rememberAuto", 1);
        }
        savePass();
    }

    public void reloadData() {
        this.tfUser.y = GCanvas.hh - 5;
        this.tfPass.y = GCanvas.hh + 33;
    }

    protected void doRegister() {
        doSendRegisterInfo();
    }

    protected void doSendRegisterInfo() {
        if (!Session_ME.gI().connected) {
            GCanvas.startWaitDlg("Đang kết nối", true);
            GCanvas.connect();
        } else {
            GCanvas.startWaitDlg("Đang đăng ký", true);
        }
        GameService.gI().requestRegister(this.tfUser.getText().toLowerCase(), this.tfPass.getText());
    }

    @Override
    public boolean keyPress(int i) {
        if (this.tfUser.isFocus) {
            if (this.tfUser.keyPressed(i)) {
                return true;
            }
        } else if (this.tfPass.isFocus && this.tfPass.keyPressed(i)) {
            return true;
        }
        return super.keyPress(i);
    }

    @Override
    public void paint(Graphics graphics) {
        ServerListScr.gI().paintBg(graphics);
        GCanvas.resetTrans(graphics);
        graphics.drawImage(GCanvas.getLogo(), GCanvas.hw, GCanvas.hh - 81, 3);
        paintPanel(graphics);
    }

    void paintPanel(Graphics graphics) {
        Res.paintDlgDragonFull(graphics, GCanvas.hw - 70, GCanvas.hh - 45, 140, 115);
        Font.normalFont[0].drawString(graphics, "Game ID:", this.xC + 5, GCanvas.hh - 31, 0);
        Font.normalFont[0].drawString(graphics, "Password:", this.xC + 5, GCanvas.hh + 8, 0);
        this.tfUser.paint(graphics);
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        this.tfPass.paint(graphics);
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        Res.imgCheck.drawFrame(this.focus == 2 ? 1 : 0, this.xC + 12, GCanvas.hh + 54, 0, 3, graphics);
        if (this.isRememPass) {
            Res.imgCheck.drawFrame(2, this.xC + 12, GCanvas.hh + 54, 0, 3, graphics);
        }
        Font.normalFont[0].drawString(graphics, "Nhớ mật khẩu", this.xC + 23, GCanvas.hh + 47, 0);
        if (ServerListScr.index >= 0 && ServerListScr.index < ServerListScr.nameServer.length) {
            Font.normalFontColor1.drawString(graphics, ServerListScr.nameServer[ServerListScr.index], 4, 1, 0);
        }
        super.paint(graphics);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    @Override
    public void update() {
        if (GCanvas.currentDialog != null) {
            return;
        }
        if (GCanvas.isPointerClick) {
            pointer(GCanvas.px, GCanvas.py);
        }
        this.tfUser.update();
        this.tfPass.update();
        int i = this.tick;
        if (i < 20) {
            int i2 = i + 1;
            this.tick = i2;
            if (i2 == 20) {
                GCanvas.gameScr.tfChat.x = 2;
                GCanvas.gameScr.tfChat.y = GCanvas.h - 40;
            }
        }
        if (GCanvas.keyPressed[2]) {
            int i3 = this.index + 1;
            this.index = i3;
            if (i3 >= 52) {
                this.index = 0;
            }
        }
        if (GCanvas.isKeyPressed(2)) {
            int i4 = this.focus - 1;
            this.focus = i4;
            if (i4 < 0) {
                this.focus = 2;
            }
        } else {
            if (GCanvas.isKeyPressed(8)) {
                int i5 = this.focus + 1;
                this.focus = i5;
                if (i5 > 2) {
                    this.focus = 0;
                }
            }
            updateCamera();
            super.update();
        }
        int i6 = this.focus;
        if (i6 == 1) {
            this.tfUser.isFocus = false;
            this.tfPass.isFocus = true;
            this.center = this.cmdLogin;
        } else if (i6 == 0) {
            this.tfUser.isFocus = true;
            this.tfPass.isFocus = false;
            this.center = this.cmdLogin;
        } else {
            this.tfUser.isFocus = false;
            this.tfPass.isFocus = false;
            this.center = this.cmdRemem;
        }
        updateCamera();
        super.update();
    }

    public void updateCamera() {
        int i = this.goc + 1;
        this.goc = i;
        if (i > 360) {
            this.goc = 0;
        }
        this.xTo = (Util.cos(this.goc) * this.radius) >> 10;
        this.yTo = (Util.sin(this.goc) * this.radius) >> 10;
        GameScr.cmtoX = this.xTo + this.centerX;
        GameScr.cmtoY = this.yTo + this.centerY;
        GCanvas.gameScr.updateCamera();
    }

    public void savePass() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeBoolean(this.isRememPass);
            if (this.isRememPass) {
                dataOutputStream.writeUTF(this.tfUser.getText());
                dataOutputStream.writeUTF(this.tfPass.getText());
            }
            RMS.saveRMS("nqshlogin", byteArrayOutputStream.toByteArray());
            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPass() {
        byte[] bArrLoadRMS = RMS.loadRMS("nqshlogin");
        if (bArrLoadRMS == null) {
            return;
        }
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArrLoadRMS));
        try {
            boolean z = dataInputStream.readBoolean();
            this.isRememPass = z;
            if (z) {
                this.tfUser.setText(dataInputStream.readUTF());
                this.tfUser.setOffset();
                this.tfPass.setText(dataInputStream.readUTF());
                this.tfPass.setOffset();
            }
            dataInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pointer(int i, int i2) {
        if (GCanvas.isPointer(this.xC - 5, GCanvas.hh + 45, 120, 30)) {
            this.center = this.cmdLogin;
            this.isRememPass = !this.isRememPass;
            this.focus = 2;
        } else if (xacDinhToaDo(i, i2, this.xC + (this.tfUser.width / 2), this.tfUser.y, 80, 60)) {
            this.center = this.cmdLogin;
            this.focus = 0;
        } else if (xacDinhToaDo(i, i2, this.xC + (this.tfPass.width / 2), this.tfPass.y, 80, 60)) {
            this.center = this.cmdLogin;
            this.focus = 1;
        }
    }
}
