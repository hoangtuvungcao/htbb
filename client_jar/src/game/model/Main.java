package game.model;

import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.MessageScr;
import game.render.screen.Screen;
import game.render.screen.TField;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Main {
    /** 0.9.5: bảng tự có nút X / nút chạm riêng -> bản PC không vẽ nút lệnh ngữ cảnh (phím Enter / Esc vẫn chạy). */
    public boolean anNutLenh;
    public Command left;
    public Command center;
    public Command right;
    public static Image ImgBpa;
    boolean isQwerty = false;

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        Font.nenSang = false;
        // pokemon: phím mềm dạng viên (trái xanh / giữa vàng / phải đỏ), lún khi đang bấm
        if (this.anNutLenh) {
            return;   // 0.9.5: màn có nút thật trong bảng - không vẽ chip phím mềm (phím vẫn chạy)
        }
        int vung = Canvas.isPointerDown ? Canvas.collisionCmdBar() : -1;
        // chia chỗ: phím giữa theo chữ (tối đa 2/5 màn), hai bên chia phần còn lại -> không đè nhau
        int wGiua = this.center == null ? 0 : Math.min(game.pokemon.GiaoDien.rongNutMem(this.center.caption, 1), Canvas.w * 2 / 5);
        int wBen = (Canvas.w - wGiua) / 2 - 6;
        if (this.left != null) {
            game.pokemon.GiaoDien.nutMem(g, this.left.caption, 0, Canvas.keyHold[12] || vung == 0, wBen);
        }
        if (this.center != null) {
            game.pokemon.GiaoDien.nutMem(g, this.center.caption, 1, Canvas.keyHold[5] || vung == 1, Canvas.w * 2 / 5);
        }
        if (this.right != null) {
            game.pokemon.GiaoDien.nutMem(g, this.right.caption, 2, Canvas.keyHold[13] || vung == 2, wBen);
        }
        // 0.9.7: HUD NRO báo thư bằng chấm đỏ ở Menu -> viên "Thư/Quà" (ChamMoi); không vẽ phong bì cũ đè nút bản đồ.
        // Màn cũ / J2ME: phong bì TRỌN trong màn (trước canh giữa ở w - 7 -> tràn mép phải)
        if (MessageScr.nMSG > 0 && Res.imgMSG != null && !(Canvas.currentScreen == Canvas.gameScr && game.render.screen.HudRong.bat())) {
            int iw = Res.imgMSG.getWidth(), ih = Res.imgMSG.getHeight();
            int mx = Canvas.w - iw - 2, my = 2;
            g.drawImage(Res.imgMSG, mx, my, 0);
            game.pokemon.GhiChu.bieu(g, "phong-bi-thu", mx, my, iw, ih);
        }
        if (GameScr.msgWorld.size() <= 0 && ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey)) {
            // pokemon: ngoài màn chơi đặt nút bàn phím ở góc phải trên (không đè biển tên game)
            g.drawImage(ImgBpa, xBanPhim(), yBanPhim(), 17);
        }
    }

    /** Tâm ngang nút bàn phím ảo: giữa màn trong game, góc phải trên ở màn tiêu đề / đăng nhập. */
    static int xBanPhim() {
        if (ImgBpa == null || Canvas.currentScreen == Canvas.gameScr && Canvas.w >= 220) {
            return Canvas.hw;
        }
        return Canvas.w - (ImgBpa.getWidth() >> 1) - 2;
    }

    /** Màn chơi hẹp (khung HP + khung đối thủ chiếm hết mép trên): nút bàn phím xuống dưới khung đối thủ. */
    static int yBanPhim() {
        if (Canvas.currentScreen == Canvas.gameScr && Canvas.w < 220) {
            return 36;
        }
        return 0;
    }

    public void updateKey() {
        if (Canvas.isPointerClick) {
            if (ImgBpa != null && (Canvas.currentScreen == Canvas.gameScr || Canvas.isVirTualKey) && Canvas.isPointer(xBanPhim() - 40, yBanPhim(), 80, 20)) {
                if (Canvas.currentScreen == Canvas.gameScr) {
                    if (!Canvas.isVirTualKey) {
                        Vector menu = new Vector();
                        menu.addElement(new Command("Bàn phím", new IAction() {
                            @Override
                            public void perform() {
                                Canvas.endDlg();
                                if (!Canvas.isVirTualKey && TField.isQwerty) {
                                    Main.this.isQwerty = true;
                                    TField.isQwerty = false;
                                }
                                GameScr.typechat = GameScr.CHAT_NORMAL;
                                Canvas.isVirTualKey = !Canvas.isVirTualKey;
                                Canvas.instance.sizeChanged(0, 0);
                                Canvas.isPointerClick = false;
                            }
                        }));
                        menu.addElement(new Command("Thay đồ", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().setConfig(6);
                                Canvas.endDlg();
                            }
                        }));
                        menu.addElement(new Command("Tìm bạn", new IAction() {
                            @Override
                            public void perform() {
                                Canvas.endDlg();
                                if (!Canvas.isVirTualKey && TField.isQwerty) {
                                    Main.this.isQwerty = true;
                                    TField.isQwerty = false;
                                }
                                GameScr.typechat = GameScr.CHAT_TIM_BAN;
                                Canvas.isVirTualKey = !Canvas.isVirTualKey;
                                Canvas.instance.sizeChanged(0, 0);
                                Canvas.isPointerClick = false;
                            }
                        }));
                        Canvas.menu.startAt(menu, 3);
                    } else {
                        if (this.isQwerty) {
                            TField.isQwerty = true;
                        }
                        Canvas.isVirTualKey = !Canvas.isVirTualKey;
                        Canvas.instance.sizeChanged(0, 0);
                        Canvas.isPointerClick = false;
                    }
                } else {
                    if (!Canvas.isVirTualKey) {
                        if (TField.isQwerty) {
                            this.isQwerty = true;
                            TField.isQwerty = false;
                        }
                    } else if (this.isQwerty) {
                        TField.isQwerty = true;
                    }
                    Canvas.isVirTualKey = !Canvas.isVirTualKey;
                    Canvas.instance.sizeChanged(0, 0);
                    Canvas.isPointerClick = false;
                }
            }
            if (!this.anNutLenh && Math.abs(Canvas.pyLast - Canvas.py) <= 10 && Math.abs(Canvas.pxLast - Canvas.px) <= 10) {
                switch (Canvas.collisionCmdBar()) {
                    case 0:
                        if (this.left != null && this.left.action != null) {
                            this.left.action.perform();
                            Canvas.isPointerClick = false;
                        }
                        break;
                    case 1:
                        if (this.center != null && this.center.action != null) {
                            Canvas.endDlg();
                            this.center.action.perform();
                            Canvas.isPointerClick = false;
                        }
                        break;
                    case 2:
                        if (this.right != null && this.right.action != null) {
                            this.right.action.perform();
                            Canvas.isPointerClick = false;
                        }
                        break;
                }
            }
        }
        if (Canvas.keyPressed[5]) {
            if (this.center != null && this.center.action != null) {
                this.center.action.perform();
                Canvas.keyPressed[5] = false;
                return;
            }
            return;
        }
        if (Canvas.keyPressed[12]) {
            if (this.left != null && this.left.action != null) {
                this.left.action.perform();
                Canvas.keyPressed[12] = false;
                return;
            }
            return;
        }
        if (Canvas.isBB) {
            if (Canvas.keyReleased[13] && this.right != null && this.right.action != null) {
                this.right.action.perform();
                Canvas.keyReleased[13] = false;
                return;
            }
            return;
        }
        if (Canvas.keyPressed[13] && this.right != null && this.right.action != null) {
            this.right.action.perform();
            Canvas.keyPressed[13] = false;
        }
    }
}
