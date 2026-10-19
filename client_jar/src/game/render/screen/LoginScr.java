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
        game.pokemon.TuNoiLai.nho(user, pass);   // 0.9.4: "Kết nối lại" / tự nối lại dùng lại (ô nhập có thể bị xoá)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                }
                Canvas.connect();
                game.pokemon.ManTai.toi(game.pokemon.ManTai.DANG_NHAP);
                GameService.gI().setClientType();
                GameService.gI().login(user, pass, GameMidlet.version);
                GameScr.saveAutoFight = false;
            }
        }).start();
        game.pokemon.ManTai.batDau(game.pokemon.ManTai.KET_NOI, null);   // 0.9.5: màn tải duy nhất (không hộp chờ riêng)
    }

    public void reloadData() {
        this.tfUser.y = Canvas.hh - 5;
        this.tfPass.y = Canvas.hh + 33;
    }

    protected void doRegister() {
        // Đăng ký trong game: màn riêng (tài khoản, mật khẩu, nhập lại, email) + câu hỏi chống bot
        DangKyScr.mo();
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (!this.tfUser.isFocus && !this.tfPass.isFocus && (keyCode > 32 && CreateCharScr.laChuGo(keyCode) || keyCode == -8)) {
            // pokemon: đang ở ô "Nhớ mật khẩu" mà gõ chữ -> vào ô Tài khoản (không nuốt phím)
            this.focus = 0;
            this.tfUser.isFocus = true;
            this.center = this.cmdLogin;
            this.right = this.tfUser.cmdClear;
        }
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
        if (game.pokemon.KhungVao.moi()) {
            veMoi(g);
            return;
        }
        ServerListScr.gI().paintBg(g);
        Canvas.resetTrans(g);
        // pokemon: tieu de ThuongHieu thay logo game goc
        if (game.pokemon.Banner.co()) {
            game.pokemon.ThuongHieu.veLogo(g, Canvas.hw, Math.max(6, Canvas.hh - 100), Canvas.w);
        } else if (!game.pokemon.ThuongHieu.veTieuDe(g, Canvas.hw, Math.max(14, Canvas.hh - 125), Canvas.w, Canvas.hh - 54)) {
            g.drawImage(Canvas.imgLogo, Canvas.hw, Canvas.hh - 70, 3);
        }
        paintPanel(g);
    }

    private IAction viecDangNhap, viecDangKy, viecQuen, viecMayChu;

    /**
     * 0.9.5 PC / APK: bảng sáng chung giữa logo và đội hình khởi đầu (hoặc cột bên), nút thật trong bảng: Đăng nhập /
     * Đăng ký / Quên mật khẩu; dòng máy chủ chạm được (thay lệnh "Menu" -> Chọn máy chủ). Không vẽ chip phím mềm.
     */
    void veMoi(Graphics g) {
        this.anNutLenh = true;
        if (this.viecDangNhap == null) {
            this.viecDangNhap = new IAction() {
                public void perform() {
                    LoginScr.this.doLogin();
                }
            };
            this.viecDangKy = new IAction() {
                public void perform() {
                    LoginScr.this.doRegister();
                }
            };
            this.viecQuen = new IAction() {
                public void perform() {
                    game.pokemon.KhungVao.moWeb(game.pokemon.KhungVao.WEB + "/quen-mat-khau");
                }
            };
            this.viecMayChu = new IAction() {
                public void perform() {
                    ServerListScr.gI().switchToMe();
                }
            };
        }
        ServerListScr.gI().paintBg(g);
        int hb = game.pokemon.KhungVao.caoNut(), k = game.pokemon.KhungVao.khe();
        int hn = game.pokemon.KhungVao.caoNhan(), ho = game.pokemon.KhungVao.caoO();
        int hDong = Math.max(20, game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + 6);
        int dau = game.pokemon.ChuDe.caoDau() + 2 * game.pokemon.ChuDe.DEM;
        int than = 2 * (hn + ho) + k + k + hDong + k;
        int hRong = dau + than + hb;
        int hHep = dau + than + 2 * hb + k;
        int[] r = game.pokemon.KhungVao.logoVaBang(g, 300, hRong, hHep, 150);
        int[] n = game.pokemon.Bang.khung(g, r[0], r[1], r[2], r[3], "Đăng nhập", null, 0, null, null);
        int x = n[0] + 2, w = n[2] - 4, y = n[1];
        y = game.pokemon.KhungVao.oNhap(g, this.tfUser, "Tài khoản", x, y, w, false) + k;
        y = game.pokemon.KhungVao.oNhap(g, this.tfPass, "Mật khẩu", x, y, w, false) + k;
        // nhớ mật khẩu (trái) + "Quên mật khẩu?" (phải, mở trang web)
        int xo = x + 1, s = 14, yo = y + (hDong - s) / 2;
        game.pokemon.GiaoDien.khoi(g, xo, yo, s, s, 3, this.focus == 2 ? game.pokemon.ChuDe.DO : game.pokemon.ChuDe.VIEN);
        game.pokemon.GiaoDien.khoi(g, xo + 1, yo + 1, s - 2, s - 2, 2, this.isRememPass ? game.pokemon.ChuDe.LA : 0xFAEECF);
        if (this.isRememPass) {
            g.setColor(0xFFFFFF);
            for (int d = 0; d < 2; d++) {
                g.drawLine(xo + 3, yo + 7 + d, xo + 6, yo + 10 + d);
                g.drawLine(xo + 6, yo + 10 + d, xo + 11, yo + 4 + d);
            }
        }
        String quen = "Quên mật khẩu?";
        int wQuen = game.pokemon.ChuDe.rong(quen, game.pokemon.ChuDe.PHU) + 8;
        int wNho = Math.min(w - wQuen - 4, s + 10 + game.pokemon.ChuDe.rong("Nhớ mật khẩu", game.pokemon.ChuDe.PHU));
        game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat("Nhớ mật khẩu", game.pokemon.ChuDe.PHU, wNho - s - 8), xo + s + 6, y, hDong,
                game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU, 0);
        game.pokemon.VungCham.them(g, x, y, wNho, hDong, this.cmdRemem, "nho-mat-khau");
        game.pokemon.ChuDe.chuGiua(g, quen, x + w - 2, y, hDong, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.XANH, 1);
        game.pokemon.VungCham.them(g, x + w - wQuen, y, wQuen, hDong, this.viecQuen, "quen-mat-khau");
        y += hDong + k;
        int kieuP = game.pokemon.Bang.CHINH, kieuT = game.pokemon.Bang.TRUNG;
        if (w >= 220) {
            y = game.pokemon.KhungVao.hangNut(g, x, y, w, new String[]{"Đăng ký", "Đăng nhập"}, new int[]{kieuT, kieuP},
                    new Object[]{this.viecDangKy, this.viecDangNhap}, "dang-nhap") + k;
        } else {
            y = game.pokemon.KhungVao.hangNut(g, x, y, w, new String[]{"Đăng nhập"}, new int[]{kieuP}, new Object[]{this.viecDangNhap}, "dang-nhap") + k;
            y = game.pokemon.KhungVao.hangNut(g, x, y, w, new String[]{"Đăng ký"}, new int[]{kieuT}, new Object[]{this.viecDangKy}, "dang-ky") + k;
        }
        // máy chủ (chạm = danh sách máy chủ) - chỉ khi còn chỗ dưới bảng (không che logo / đội hình)
        String mc = ServerListScr.nameServer != null && ServerListScr.index >= 0 && ServerListScr.index < ServerListScr.nameServer.length
                ? TaiDanhSach.tenHien(ServerListScr.nameServer[ServerListScr.index], ServerListScr.index) : "Huyền Thoại Bảo Bối";
        int[] kd = game.pokemon.Banner.vungKhoiDau();
        int yMc = r[1] + r[3] + 4;
        boolean choDuoi = yMc + hDong <= Canvas.h - Screen.deltaY - 4
                && (kd == null || yMc + hDong <= kd[1] || r[0] >= kd[2] || r[0] + r[2] <= kd[0]);
        if (choDuoi) {
            int wm = Math.min(r[2], game.pokemon.ChuDe.rong("Máy chủ: " + mc, game.pokemon.ChuDe.PHU) + 30);
            int xm = r[0] + (r[2] - wm) / 2;
            game.pokemon.ChuDe.the(g, xm, yMc, wm, hDong, hDong / 2);
            game.pokemon.GiaoDien.pokeBall(g, xm + 11, yMc + hDong / 2, 5, 0);
            game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat("Máy chủ: " + mc, game.pokemon.ChuDe.PHU, wm - 26), xm + 20, yMc, hDong,
                    game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU, 0);
            game.pokemon.VungCham.them(g, xm, yMc, wm, hDong, this.viecMayChu, "may-chu");
        }
    }

    void paintPanel(Graphics g) {
        // pokemon: hộp thoại trắng viền tối, dải tiêu đề đỏ, chữ tối
        game.pokemon.GiaoDien.theMo(g, Canvas.hw - 72, Canvas.hh - 50, 144, 116);
        game.pokemon.GiaoDien.dai(g, Canvas.hw - 69, Canvas.hh - 47, 138, 16, "Đăng nhập");
        Font.blackFont.drawString(g, "Tài khoản:", this.xC + 1, Canvas.hh - 27, 0);
        Font.blackFont.drawString(g, "Mật khẩu:", this.xC + 1, Canvas.hh + 9, 0);
        this.tfUser.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        this.tfPass.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        // ô "nhớ mật khẩu" vẽ tay: ô trắng viền tối, dấu tick xanh lá
        int xo = this.xC + 2;
        int yo = Canvas.hh + 49;
        game.pokemon.GiaoDien.khoi(g, xo, yo, 11, 11, 2, this.focus == 2 ? game.pokemon.GiaoDien.DO : game.pokemon.GiaoDien.VIEN);
        game.pokemon.GiaoDien.khoi(g, xo + 2, yo + 2, 7, 7, 1, game.pokemon.GiaoDien.TRANG);
        if (this.isRememPass) {
            game.pokemon.GiaoDien.khoi(g, xo + 3, yo + 3, 5, 5, 1, game.pokemon.GiaoDien.LA_TOI);
        }
        Font.blackFont.drawString(g, "Nhớ mật khẩu", this.xC + 18, Canvas.hh + 47, 0);
        // chạm ô / chữ "Nhớ mật khẩu" = bật / tắt (trước chỉ có phím: chọn dòng rồi bấm "Nhớ")
        game.pokemon.VungCham.them(g, xo - 2, yo - 3, 18 + Font.blackFont.getWidth("Nhớ mật khẩu") + 4, 17, this.cmdRemem, "nho-mat-khau");
        if (!game.pokemon.Banner.co() && Canvas.hh + 84 + 14 < Canvas.h - Screen.deltaY) {
            Font.borderFont.drawString(g, new StringBuffer("Hỗ trợ: ").append(numberSupport).toString(), Canvas.hw, Canvas.hh + 84, 2);
        }
        super.paint(g);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
        game.pokemon.AmThanh.manTieuDe();   // pokemon: nhac tieu de
    }

    @Override
    public void update() {
        this.tfUser.update();
        this.tfPass.update();
        // chạm / bấm chuột vào ô (TField tự bật isFocus): đồng bộ focus để phím Lên/Xuống, Tab, Enter đúng ô
        if (this.tfUser.isFocus && this.focus != 0) {
            this.focus = 0;
            this.tfPass.isFocus = false;
            this.center = this.cmdLogin;
        } else if (this.tfPass.isFocus && this.focus != 1) {
            this.focus = 1;
            this.tfUser.isFocus = false;
            this.center = this.cmdLogin;
        }
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
