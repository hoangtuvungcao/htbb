package game.render.screen;

import game.GameMidlet;
import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Session_ME;

public class LoginScr extends Screen {
    public TField tfPass;
    int focus;
    int xC;
    int wC;
    int yL;
    int defYL;
    private boolean isRememPass;
    private Command cmdRemem;
    private Command cmdLogin;
    public static String numberSupport = "19006610";
    int xTo;
    int yTo;
    int centerX = 160;
    int centerY = 160;
    int radius = 128;
    int goc = 0;
    int index = 0;
    int tick = 0;
    public TField tfUser = new TField();

    @Override
    public void init() {
        this.wC = 110;
        this.xC = Canvas.hw - (this.wC / 2);
        this.tfUser.width = this.wC;
        this.tfUser.y = Canvas.hh - 13;
        this.tfPass.y = Canvas.hh + 23;
        this.tfPass.width = this.wC;
        this.tfUser.x = this.xC;
        this.tfPass.x = this.xC;
    }

    public LoginScr() {
        this.tfUser.height = Screen.ITEM_HEIGHT + 2;
        this.tfUser.isFocus = true;
        this.tfUser.setIputType(3);
        this.tfUser.setText("");
        this.tfPass = new TField();
        this.tfPass.height = Screen.ITEM_HEIGHT + 2;
        this.tfPass.isFocus = false;
        this.tfPass.setIputType(2);
        this.tfPass.setText("");
        loadPass();
        init();
        this.cmdLogin = new Command(Canvas.w > 200 ? "Đăng nhập" : "Đ.Nhập", new IAction() {
            @Override
            public void perform() {
                LoginScr.this.doLogin();
            }
        });
        this.center = this.cmdLogin;
        this.cmdRemem = new Command("Nhớ", new IAction() {
            @Override
            public void perform() {
                LoginScr.this.isRememPass = !LoginScr.this.isRememPass;
            }
        });
        final Command cmdReg = new Command(Canvas.w > 200 ? "Đăng ký" : "Đ.Ký", new IAction() {
            @Override
            public void perform() {
                LoginScr.this.doRegister();
            }
        });
        if (Canvas.isBB) {
            int aa = RMS.loadRMSInt("wifi");
            if (aa == 1) {
                Canvas.isWifi = true;
            }
            this.left = new Command("Menu", new IAction() {
                @Override
                public void perform() {
                    Vector menu = new Vector();
                    menu.addElement(cmdReg);
                    menu.addElement(new Command("Trạng thái kết nối", new IAction() {
                        @Override
                        public void perform() {
                            Vector me = new Vector();
                            me.addElement(new Command("GPRS", new IAction() {
                                @Override
                                public void perform() {
                                    Canvas.isWifi = false;
                                    RMS.saveRMSInt("wifi", 0);
                                }
                            }));
                            me.addElement(new Command("Wifi", new IAction() {
                                @Override
                                public void perform() {
                                    Canvas.isWifi = true;
                                    RMS.saveRMSInt("wifi", 1);
                                }
                            }));
                        }
                    }));
                    Canvas.menu.startAt(menu, 0);
                }
            });
        } else {
            this.left = new Command("Menu", new IAction() {
                @Override
                public void perform() {
                    Vector menu = new Vector();
                    menu.addElement(cmdReg);
                    menu.addElement(new Command("Chọn máy chủ", new IAction() {
                        @Override
                        public void perform() {
                            ServerListScr.gI().switchToMe();
                        }
                    }));
                    Canvas.menu.startAt(menu, 0);
                }
            });
        }
        this.right = this.tfUser.cmdClear;
        numberSupport = loadnumber();
    }

    String loadnumber() {
        String data = RMS.loadRMSString("numbersupport");
        if (data == null) {
            return numberSupport;
        }
        return data;
    }

    public boolean doShowAgent(String user) {
        if (user.equals("showagentpro")) {
            return true;
        }
        return false;
    }

    protected void doLogin() {
        final String user = this.tfUser.getText().toLowerCase().trim();
        final String pass = this.tfPass.getText();
        if (doShowAgent(user)) {
            Canvas.startOKDlg(new StringBuffer("Agent: ").append(GameMidlet.agent).append(" Pro: ").append(GameMidlet.provider).append(" ClinenPro: ").append(GameMidlet.client_Provider).append(GameMidlet.isLoadAgentPro ? " Ban trang " : " Ban thuong ").append(GameMidlet.coutVerSion).toString());
            return;
        }
        if (user.equals("")) {
            return;
        }
        if (pass.equals("")) {
            this.focus = 1;
            this.tfUser.isFocus = false;
            this.tfPass.isFocus = true;
            this.right = this.tfPass.cmdClear;
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                }
                Canvas.connect();
                GameService.gI().setClientType();
                GameService.gI().login(user, pass, GameMidlet.version);
                GameScr.saveAutoFight = false;
            }
        }).start();
        Canvas.startWaitDlg("Đang kết nối", true);
    }

    public void reloadData() {
        this.tfUser.y = Canvas.hh - 5;
        this.tfPass.y = Canvas.hh + 33;
    }

    protected void doRegister() {
        String user = this.tfUser.getText().toLowerCase().trim();
        String pass = this.tfPass.getText();
        if (user.equals("")) {
            Canvas.startOKDlg("Vui lòng nhập Game ID muốn đăng ký vào ô trên.");
            return;
        }
        if (pass.equals("")) {
            Canvas.msgdlg.setInfo("Bạn phải nhập password đăng ký.", (Command) null, new Command("OK", new IAction() {
                @Override
                public void perform() {
                    LoginScr.this.focus = 1;
                    LoginScr.this.tfUser.isFocus = false;
                    LoginScr.this.tfPass.isFocus = true;
                    LoginScr.this.right = LoginScr.this.tfPass.cmdClear;
                    Canvas.endDlg();
                }
            }), (Command) null);
            Canvas.msgdlg.show();
        } else {
            Canvas.msgdlg.isIcon = false;
            Canvas.msgdlg.setInfo(new StringBuffer("Bạn có muốn đăng ký tài khoản: ").append(user).append(" không?").toString(), new Command("Có", new IAction() {
                @Override
                public void perform() {
                    LoginScr.this.doSendRegisterInfo();
                }
            }), (Command) null, new Command("Không", new IAction() {
                @Override
                public void perform() {
                    Canvas.endDlg();
                }
            }));
            Canvas.currentDialog = Canvas.msgdlg;
        }
    }

    protected void doSendRegisterInfo() {
        if (!Session_ME.gI().connected) {
            Canvas.startWaitDlg("Đang kết nối", true);
            Canvas.connect();
        } else {
            Canvas.startWaitDlg("Đang đăng ký", true);
        }
        GameService.gI().requestRegister(this.tfUser.getText().toLowerCase(), this.tfPass.getText());
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (this.tfUser.isFocus) {
            boolean swallow = this.tfUser.keyPressed(keyCode);
            if (swallow) {
                return true;
            }
        } else if (this.tfPass.isFocus) {
            boolean swallow2 = this.tfPass.keyPressed(keyCode);
            if (swallow2) {
                return true;
            }
        }
        return super.keyPress(keyCode);
    }

    @Override
    public void paint(Graphics g) {
        ServerListScr.gI().paintBg(g);
        Canvas.resetTrans(g);
        g.drawImage(Canvas.imgLogo, Canvas.hw, Canvas.hh - 70, 3);
        paintPanel(g);
    }

    void paintPanel(Graphics g) {
        Res.paintDlgDragonFull(g, Canvas.hw - 70, (Canvas.hh - 50) + 15, 140, 100);
        Font.normalFont[0].drawString(g, "Game ID:", this.xC + 5, Canvas.hh - 26, 0);
        Font.normalFont[0].drawString(g, "Password:", this.xC + 5, Canvas.hh + 10, 0);
        this.tfUser.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        this.tfPass.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        Res.imgCheck.drawFrame(this.focus == 2 ? 1 : 0, this.xC + 12, Canvas.hh + 54, 0, 3, g);
        if (this.isRememPass) {
            Res.imgCheck.drawFrame(2, this.xC + 12, Canvas.hh + 54, 0, 3, g);
        }
        Font.normalFont[0].drawString(g, "Nhớ mật khẩu", this.xC + 23, Canvas.hh + 47, 0);
        Font.borderFont.drawString(g, new StringBuffer("Hotline: ").append(numberSupport).toString(), this.xC + 5, Canvas.hh + 70, 0);
        super.paint(g);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    @Override
    public void update() {
        this.tfUser.update();
        this.tfPass.update();
        if (this.tick < 20) {
            this.tick++;
            if (this.tick == 20) {
                Canvas.gameScr.tfChat.x = 2;
                Canvas.gameScr.tfChat.y = Canvas.h - 40;
            }
        }
        if (Canvas.keyPressed[2]) {
            this.index++;
            if (this.index >= 52) {
                this.index = 0;
            }
        }
        boolean is = false;
        if (Canvas.isKeyPressed(2)) {
            this.focus--;
            if (this.focus < 0) {
                this.focus = 2;
            }
            is = true;
        } else if (Canvas.isKeyPressed(8)) {
            this.focus++;
            if (this.focus > 2) {
                this.focus = 0;
            }
            is = true;
        }
        if (is) {
            if (this.focus == 1) {
                this.tfUser.isFocus = false;
                this.tfPass.isFocus = true;
                this.center = this.cmdLogin;
            } else if (this.focus == 0) {
                this.tfUser.isFocus = true;
                this.tfPass.isFocus = false;
                this.center = this.cmdLogin;
            } else {
                this.tfUser.isFocus = false;
                this.tfPass.isFocus = false;
                this.right = null;
                this.center = this.cmdRemem;
            }
        }
        if (this.tfUser.isFocus) {
            this.right = this.tfUser.cmdClear;
        } else if (this.tfPass.isFocus) {
            this.right = this.tfPass.cmdClear;
        }
        updateCamera();
        super.update();
    }

    public void updateCamera() {
        this.goc++;
        if (this.goc > 360) {
            this.goc = 0;
        }
        this.xTo = (Util.cos(this.goc) * this.radius) >> 10;
        this.yTo = (Util.sin(this.goc) * this.radius) >> 10;
        GameScr.cmtoX = this.xTo + this.centerX;
        GameScr.cmtoY = this.yTo + this.centerY;
        Canvas.gameScr.updateCamera();
    }

    public void savePass() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bo);
        try {
            dos.writeBoolean(this.isRememPass);
            if (this.isRememPass) {
                dos.writeUTF(this.tfUser.getText());
                dos.writeUTF(this.tfPass.getText());
            }
            RMS.saveRMS("nqshlogin", bo.toByteArray());
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPass() {
        byte[] data = RMS.loadRMS("nqshlogin");
        if (data == null) {
            return;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bi);
        try {
            this.isRememPass = dis.readBoolean();
            if (this.isRememPass) {
                this.tfUser.setText(dis.readUTF());
                this.tfUser.setOffset();
                this.tfPass.setText(dis.readUTF());
                this.tfPass.setOffset();
            }
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
