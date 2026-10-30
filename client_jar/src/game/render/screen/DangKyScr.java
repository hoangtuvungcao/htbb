package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import java.io.DataInputStream;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Session_ME;

/**
 * Màn "Đăng ký tài khoản" ngay trong game (bản jar + APK dùng chung code). Mở từ Menu màn đăng nhập.
 *
 * Quy tắc (độ dài, ký tự, tên cấm, từ cấm, hạn mức) do SERVER kiểm (real.cmd.DangKyTrongGame, chung một
 * tệp quy tắc với web) — ở đây chỉ kiểm ô trống và mật khẩu nhập lại, để quy tắc không lệch giữa các nơi.
 *
 * Luồng: điền 4 ô -> "Đăng ký" -> kết nối, xin câu hỏi chống bot (lệnh 39 bước 0) -> hỏi người chơi ->
 * gửi (bước 1) -> thành công: điền sẵn tài khoản/mật khẩu vào màn đăng nhập. Giao thức: xem DangKyTrongGame.
 */
public class DangKyScr extends Screen {
    public static final String DAU = "#dk2";
    private static DangKyScr me;

    private final TField[] o = new TField[4];
    private static final String[] NHAN = {"Tài khoản:", "Mật khẩu:", "Nhập lại mật khẩu:", "Email (không bắt buộc):"};
    private int focus;
    private int loiO = -1;
    private String cauHoi;
    private boolean choCauHoi;
    private final Command cmdDangKy;

    public static DangKyScr gI() {
        if (me == null) {
            me = new DangKyScr();
        }
        return me;
    }

    /** Mở màn đăng ký; mang sẵn tên tài khoản đang gõ ở màn đăng nhập (nếu có). */
    public static void mo() {
        DangKyScr s = gI();
        String u = Canvas.loginScr != null ? Canvas.loginScr.tfUser.getText().toLowerCase().trim() : "";
        if (s.o[0].getText().length() == 0 && u.length() > 0) {
            s.o[0].setText(u);
        }
        s.datFocus(0);
        s.switchToMe();
    }

    private DangKyScr() {
        for (int i = 0; i < 4; i++) {
            TField t = new TField();
            t.height = Screen.ITEM_HEIGHT + 2;
            t.setText("");
            this.o[i] = t;
        }
        this.o[0].setIputType(TField.INPUT_ALPHA_NUMBER_ONLY);
        this.o[0].setMaxTextLenght(20);
        this.o[1].setIputType(TField.INPUT_TYPE_PASSWORD);
        this.o[1].setMaxTextLenght(20);
        this.o[2].setIputType(TField.INPUT_TYPE_PASSWORD);
        this.o[2].setMaxTextLenght(20);
        this.o[3].setIputType(TField.INPUT_TYPE_ANY);
        this.o[3].setMaxTextLenght(100);
        this.cmdDangKy = new Command(Canvas.w > 200 ? "Đăng ký" : "Đ.Ký", new IAction() {
            public void perform() {
                DangKyScr.this.guiDangKy();
            }
        });
        this.center = this.cmdDangKy;
        this.left = new Command("Quay lại", new IAction() {
            public void perform() {
                Canvas.loginScr.switchToMe();
            }
        });
        init();
    }

    private int buoc() {
        int b = (Canvas.h - Screen.deltaY - 34) / 4;
        int max = Screen.ITEM_HEIGHT + 18;
        return b > max ? max : b;
    }

    private int yDau() {
        int tong = 22 + 4 * buoc() + 6;
        int y = Canvas.hh - tong / 2;
        return y < 2 ? 2 : y;
    }

    private int rong() {
        int w = Canvas.w - 16;
        return w > 200 ? 200 : w;
    }

    public void init() {
        int w = rong() - 16;
        int x = Canvas.hw - w / 2;
        int y = yDau() + 22;
        int b = buoc();
        for (int i = 0; i < 4; i++) {
            this.o[i].x = x;
            this.o[i].width = w;
            this.o[i].y = y + i * b + (b - this.o[i].height) - 2;
        }
    }

    private void datFocus(int i) {
        this.focus = i;
        for (int k = 0; k < 4; k++) {
            this.o[k].isFocus = k == i;
        }
        this.right = this.o[i].cmdClear;
        this.center = this.cmdDangKy;
    }

    /** Mã máy ngẫu nhiên lưu trong RMS (J2ME không có mã máy thật) — server chỉ dùng để đếm hạn mức. */
    static String maMay() {
        String m = RMS.loadRMSString("maMay");
        if (m == null || m.length() < 8) {
            Random r = new Random(System.currentTimeMillis() ^ Runtime.getRuntime().freeMemory());
            StringBuffer sb = new StringBuffer("j2me-");
            for (int i = 0; i < 16; i++) {
                sb.append("0123456789abcdef".charAt((r.nextInt() >>> 1) % 16));
            }
            m = sb.toString();
            RMS.saveRMSString("maMay", m);
        }
        return m;
    }

    private String ten() {
        return this.o[0].getText().toLowerCase().trim();
    }

    protected void guiDangKy() {
        this.loiO = -1;
        if (ten().length() == 0) {
            baoLoi(0, "Vui lòng nhập tên tài khoản muốn đăng ký.");
            return;
        }
        if (this.o[1].getText().length() == 0) {
            baoLoi(1, "Vui lòng nhập mật khẩu.");
            return;
        }
        if (!this.o[1].getText().equals(this.o[2].getText())) {
            baoLoi(2, "Mật khẩu nhập lại không khớp.");
            return;
        }
        if (this.cauHoi != null && Session_ME.gI().connected) {
            hoiCauDo();
            return;
        }
        this.cauHoi = null;
        this.choCauHoi = true;
        if (!Session_ME.gI().connected) {
            Canvas.connect();
        }
        game.pokemon.ManTai.batDau(game.pokemon.ManTai.KET_NOI, null);
        GameService.gI().dangKyXinCauDo(maMay());
    }

    private void baoLoi(final int oLoi, String loi) {
        this.loiO = oLoi;
        Canvas.startOKDlg(loi, new IAction() {
            public void perform() {
                Canvas.endDlg();
                if (oLoi >= 0) {
                    DangKyScr.this.datFocus(oLoi);
                }
            }
        });
    }

    private void hoiCauDo() {
        final String q = this.cauHoi;
        Canvas.inputDlg.setInfo(q, new IAction() {
            public void perform() {
                String tl = Canvas.inputDlg.tfInput.getText().trim();
                if (tl.length() == 0) {
                    return;
                }
                Canvas.endDlg();
                DangKyScr.this.cauHoi = null;
                Canvas.startWaitDlg("Đang đăng ký...", true);
                GameService.gI().dangKyGui(DangKyScr.this.ten(), DangKyScr.this.o[1].getText(), DangKyScr.this.o[2].getText(),
                        DangKyScr.this.o[3].getText().trim(), tl, maMay());
            }
        }, TField.INPUT_TYPE_NUMERIC, 3, true);
        Canvas.inputDlg.show();
    }

    private static int oCuaMa(String ma) {
        if (ma.startsWith("ten") || ma.equals("trungTen")) {
            return 0;
        }
        if (ma.equals("matKhauSai")) {
            return 1;
        }
        if (ma.equals("matKhauKhongKhop")) {
            return 2;
        }
        if (ma.startsWith("email")) {
            return 3;
        }
        return -1;
    }

    /** Gói lệnh 39 dạng mới (sau chuỗi "#dk2"). Gọi từ MessageHandler. */
    public static void nhan(DataInputStream r) throws Exception {
        final DangKyScr s = gI();
        byte loai = r.readByte();
        if (loai == 0) {
            s.cauHoi = r.readUTF();
            if (s.choCauHoi) {
                s.choCauHoi = false;
                Canvas.endDlg();
                s.hoiCauDo();
            }
            return;
        }
        if (loai == 1) {
            final String ten = r.readUTF();
            String tb = r.readUTF();
            r.readBoolean();
            final String mk = s.o[1].getText();
            for (int i = 0; i < 4; i++) {
                s.o[i].setText("");
            }
            s.cauHoi = null;
            Canvas.loginScr.tfUser.setText(ten);
            Canvas.loginScr.tfPass.setText(mk);
            Canvas.loginScr.switchToMe();
            // 31/10/2026 (soi giao diện hành trình): trước đây hộp này tiêu đề "Xác nhận", nút CHÍNH (cam) là "Đóng" còn
            // "Đăng nhập" - việc người chơi cần làm tiếp - là nút phụ. Nay: tiêu đề nói kết quả, "Đăng nhập" là nút chính
            // (máy cảm ứng: nút phải; J2ME: phím mềm trái), "Đóng" = huỷ / phím mềm phải.
            IAction vao = new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    Canvas.loginScr.doLogin();
                }
            };
            IAction dong = new IAction() {
                public void perform() {
                    Canvas.endDlg();
                }
            };
            String noi = "Tên đăng nhập: " + ten + ". " + tb;
            if (game.pokemon.MenuNguCanh.bat()) {
                game.pokemon.HopThongBao.mo("Đăng ký thành công", noi, game.pokemon.HopThongBao.THONG_TIN,
                        new String[]{"Đóng", "Đăng nhập"}, new int[]{game.pokemon.Bang.TRUNG, game.pokemon.Bang.XAC_NHAN},
                        new IAction[]{dong, vao}, dong);
            } else {
                Canvas.msgdlg.isIcon = false;
                Canvas.msgdlg.setInfo("Đăng ký thành công! " + noi, new Command("Đăng nhập", vao), (Command) null,
                        new Command("Đóng", dong));
                Canvas.msgdlg.show();
            }
            DangKyScr.me = null;
            return;
        }
        int n = r.readByte();
        Vector loi = new Vector();
        int oDau = -1;
        boolean saiCauDo = false;
        for (int i = 0; i < n; i++) {
            String ma = r.readUTF();
            loi.addElement(r.readUTF());
            if (oDau < 0) {
                oDau = oCuaMa(ma);
            }
            if (ma.equals("traLoi")) {
                saiCauDo = true;
            }
        }
        String moi = r.readUTF();
        s.cauHoi = moi.length() > 0 ? moi : null;
        s.choCauHoi = false;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < loi.size(); i++) {
            sb.append(i == 0 ? "" : "\n").append((String) loi.elementAt(i));
        }
        final boolean hoiLai = saiCauDo && s.cauHoi != null && oDau < 0;
        final int o = oDau;
        s.loiO = oDau;
        Canvas.startOKDlg(sb.toString(), new IAction() {
            public void perform() {
                Canvas.endDlg();
                if (hoiLai) {
                    s.hoiCauDo();
                } else if (o >= 0) {
                    s.datFocus(o);
                }
            }
        });
    }

    /**
     * Mất kết nối: nếu đang ở màn này thì chỉ quên câu hỏi (server đã quên phiên) và trả true để GameScr
     * không bật hộp thoại "Kết nối lại" (vốn để đăng nhập lại).
     */
    public static boolean matKetNoi() {
        if (me == null || Canvas.currentScreen != me) {
            return false;
        }
        me.cauHoi = null;
        if (me.choCauHoi) {
            me.choCauHoi = false;
            Canvas.startOKDlg("Mất kết nối với máy chủ. Vui lòng bấm Đăng ký lại.");
        }
        return true;
    }

    public boolean keyPress(int keyCode) {
        // Canvas gửi phím cho màn TRƯỚC hộp thoại: đang mở hộp câu hỏi thì phím không được lọt vào ô đang chọn
        if (Canvas.currentDialog != null) {
            return false;
        }
        if (this.o[this.focus].keyPressed(keyCode)) {
            return true;
        }
        return super.keyPress(keyCode);
    }

    public void update() {
        int truoc = this.focus;
        for (int i = 0; i < 4; i++) {
            this.o[i].update();
        }
        if (Canvas.currentDialog != null) {
            // hộp thoại đang mở: không đổi ô
        } else if (Canvas.isKeyPressed(2)) {
            datFocus(this.focus == 0 ? 3 : this.focus - 1);
        } else if (Canvas.isKeyPressed(8)) {
            datFocus(this.focus == 3 ? 0 : this.focus + 1);
        } else {
            // chạm vào ô khác: TField tự đặt isFocus
            int moi = -1;
            for (int i = 0; i < 4; i++) {
                if (this.o[i].isFocus && i != truoc) {
                    moi = i;
                }
            }
            if (moi >= 0) {
                datFocus(moi);
            } else if (!this.o[truoc].isFocus) {
                this.o[truoc].isFocus = true;
            }
        }
        if (Canvas.loginScr != null) {
            Canvas.loginScr.updateCamera();
        }
        super.update();
    }

    private IAction viecDangKy, viecLui;

    /** 0.9.5 PC / APK: bảng sáng chung (không che logo / đội hình), nút Đăng ký / Quay lại trong bảng. */
    private void veMoi(Graphics g) {
        this.anNutLenh = true;
        if (this.viecDangKy == null) {
            this.viecDangKy = new IAction() {
                public void perform() {
                    DangKyScr.this.guiDangKy();
                }
            };
            this.viecLui = new IAction() {
                public void perform() {
                    Canvas.loginScr.switchToMe();
                }
            };
        }
        ServerListScr.gI().paintBg(g);
        int hb = game.pokemon.KhungVao.caoNut(), k = game.pokemon.KhungVao.khe();
        int hn = game.pokemon.KhungVao.caoNhan(), ho = game.pokemon.KhungVao.caoO();
        int dau = game.pokemon.ChuDe.caoDau() + 2 * game.pokemon.ChuDe.DEM;
        int hHep = dau + 4 * (hn + ho) + 4 * k + hb;
        int hRong = dau + 2 * (hn + ho) + 2 * k + hb;   // bảng rộng: ô xếp 2 cột
        int[] r = game.pokemon.KhungVao.logoVaBang(g, 420, hRong, hHep, 150);
        int[] n = game.pokemon.Bang.khung(g, r[0], r[1], r[2], r[3], "Đăng ký tài khoản", null, 0, null, null);
        int x = n[0] + 2, w = n[2] - 4, y = n[1];
        if (r[2] >= 220) {
            int wc = (w - k) / 2;
            int y2 = y;
            for (int i = 0; i < 4; i++) {
                int yy = game.pokemon.KhungVao.oNhap(g, this.o[i], NHAN[i], x + (i % 2) * (wc + k), y + (i / 2) * (hn + ho + k), wc, i == this.loiO);
                y2 = Math.max(y2, yy + k);
            }
            y = y2;
        } else {
            for (int i = 0; i < 4; i++) {
                y = game.pokemon.KhungVao.oNhap(g, this.o[i], NHAN[i], x, y, w, i == this.loiO) + k;
            }
        }
        game.pokemon.KhungVao.hangNut(g, x, y, w, new String[]{"Quay lại", "Đăng ký"}, new int[]{game.pokemon.Bang.TRUNG, game.pokemon.Bang.CHINH},
                new Object[]{this.viecLui, this.viecDangKy}, "dang-ky");
    }

    public void paint(Graphics g) {
        if (game.pokemon.KhungVao.moi()) {
            veMoi(g);
            return;
        }
        ServerListScr.gI().paintBg(g);
        Canvas.resetTrans(g);
        int w = rong();
        int x = Canvas.hw - w / 2;
        int y = yDau();
        int h = 22 + 4 * buoc() + 6;
        game.pokemon.GiaoDien.theMo(g, x, y, w, h);
        game.pokemon.GiaoDien.dai(g, x + 3, y + 3, w - 6, 16, "Đăng ký tài khoản");
        for (int i = 0; i < 4; i++) {
            TField t = this.o[i];
            Font.blackFont.drawString(g, NHAN[i], t.x + 1, t.y - Font.blackFont.getHeight() - 1, 0);
            if (i == this.loiO) {
                game.pokemon.GiaoDien.khoi(g, t.x - 2, t.y - 2, t.width + 5, t.height + 5, 3, game.pokemon.GiaoDien.DO);
            }
            t.paint(g);
            g.setClip(0, 0, Canvas.w, Canvas.h);
        }
        super.paint(g);
    }

    public void switchToMe() {
        super.switchToMe();
        init();
    }
}
