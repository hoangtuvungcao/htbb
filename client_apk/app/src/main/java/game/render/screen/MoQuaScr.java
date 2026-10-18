package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.render.GCanvas;
import game.render.Res;

public class MoQuaScr extends Screen {
    public static Bitmap imgBox = null;
    public static MoQuaScr me = null;
    public static byte soHop = 5;
    int height;
    boolean isBatDau;
    boolean isChon;
    public boolean isFinish;
    boolean isShowKq;
    public int selected;
    public String[] thongBao;
    int width;
    int x;
    int x1;
    int x2;
    int x3;
    int x4;
    int x5;
    int x6;
    int x7;
    int x8;
    int y;
    int y1;
    int y2;
    int y3;
    int y4;
    int y5;
    int y6;
    int y7;
    int y8;
    public String Chuoi = "";
    int t = 0;
    int dem = 0;
    int tg = 0;

    public static MoQuaScr gI() {
        MoQuaScr moQuaScr = me;
        if (moQuaScr != null) {
            return moQuaScr;
        }
        MoQuaScr moQuaScr2 = new MoQuaScr();
        me = moQuaScr2;
        return moQuaScr2;
    }

    @Override
    public void switchToMe() {
        init();
        super.switchToMe();
    }

    public MoQuaScr() {
        this.right = new Command("Mở hết", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.gameService.openLuckyBox((byte) 1, (byte) -100);
                GCanvas.startYesNoDlgPutArr("Bạn có muốn mở hộp không?", MoQuaScr.this.thongBao, new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.gameScr.gameService.openLuckyBox((byte) 2, (byte) -100);
                        MoQuaScr.this.resetGame();
                        GCanvas.endDlg();
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.endDlg();
                    }
                });
            }
        });
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                GCanvas.startYesNoDlgPutArr("Bạn có muốn mở hộp không?", MoQuaScr.this.thongBao, new IAction() {
                    @Override
                    public void perform() {
                        MoQuaScr.this.isShowKq = true;
                        GCanvas.gameScr.gameService.openLuckyBox((byte) 1, (byte) MoQuaScr.this.selected);
                        GCanvas.endDlg();
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.endDlg();
                    }
                });
            }
        });
        this.left = new Command("Bắt đầu", new IAction() {
            @Override
            public void perform() {
                MoQuaScr.this.isBatDau = true;
            }
        });
    }

    public void finishGame() {
        GCanvas.startOKDlg("Bạn đã hết lượt chơi.", new IAction() {
            @Override
            public void perform() {
                MoQuaScr.this.resetGame();
            }
        });
    }

    public void resetGame() {
        this.t = 0;
        this.tg = 0;
        this.isShowKq = false;
        this.isChon = false;
        this.isBatDau = false;
        this.isFinish = false;
        GCanvas.gameScr.switchToMe();
        GCanvas.endDlg();
        this.left = new Command("Bắt đầu", new IAction() {
            @Override
            public void perform() {
                MoQuaScr.this.isBatDau = true;
                MoQuaScr.this.isChon = false;
            }
        });
    }

    public void setInfo(byte b, String[] strArr) {
        soHop = b;
        this.thongBao = strArr;
    }

    @Override
    public void init() {
        this.width = GCanvas.w - 60;
        int i = GCanvas.h - 80;
        this.height = i;
        int i2 = (this.width / 2) + 10;
        this.x8 = i2;
        this.x7 = i2;
        this.x6 = i2;
        this.x5 = i2;
        this.x4 = i2;
        this.x3 = i2;
        this.x2 = i2;
        this.x1 = i2;
        this.x = i2;
        int i3 = (i / 2) + 10;
        this.y8 = i3;
        this.y7 = i3;
        this.y6 = i3;
        this.y5 = i3;
        this.y4 = i3;
        this.y3 = i3;
        this.y2 = i3;
        this.y1 = i3;
        this.y = i3;
    }

    @Override
    public void update() {
        GCanvas.gameScr.update();
        if (this.isShowKq) {
            this.dem++;
        }
        if (this.dem > 50) {
            this.dem = 0;
            this.isShowKq = false;
            this.Chuoi = "";
        }
        int i = this.tg + 1;
        this.tg = i;
        if (i < 20) {
            effectOut();
        }
        if (this.isBatDau) {
            int i2 = this.t + 1;
            this.t = i2;
            if (i2 < 20) {
                effectIn();
            } else {
                effectOut();
            }
            if (this.t > 30) {
                this.t = 0;
                this.tg = 0;
                this.isBatDau = false;
                this.isChon = true;
                this.left = null;
            }
        }
        if (GCanvas.isPointerClick && GCanvas.isPoint(30, 30, this.width, this.height) && this.isChon && !this.isShowKq) {
            if (GCanvas.isPointer(this.x, this.y, 40, 40)) {
                this.selected = 0;
            } else if (GCanvas.isPointer(this.x1, this.y1, 40, 40)) {
                this.selected = 1;
            } else if (GCanvas.isPointer(this.x2, this.y2, 40, 40)) {
                this.selected = 2;
            } else if (GCanvas.isPointer(this.x3, this.y3, 40, 40)) {
                this.selected = 3;
            } else if (GCanvas.isPointer(this.x4, this.y4, 40, 40)) {
                this.selected = 4;
            }
            byte b = soHop;
            if (b == 7 || b == 9) {
                if (GCanvas.isPointer(this.x5, this.y5, 40, 40)) {
                    this.selected = 5;
                } else if (GCanvas.isPointer(this.x6, this.y6, 40, 40)) {
                    this.selected = 6;
                }
            }
            if (soHop == 9) {
                if (GCanvas.isPointer(this.x7, this.y7, 40, 40)) {
                    this.selected = 7;
                } else if (GCanvas.isPointer(this.x8, this.y8, 40, 40)) {
                    this.selected = 8;
                }
            }
            if (this.center != null) {
                this.center.action.perform();
            }
        }
    }

    private void effectOut() {
        int i = this.x - 10;
        this.x = i;
        int i2 = this.y - 6;
        this.y = i2;
        if (i < 40 || i2 < 40) {
            this.x = 40;
            this.y = 40;
        }
        int i3 = this.x1 + 10;
        this.x1 = i3;
        int i4 = this.y1 - 6;
        this.y1 = i4;
        int i5 = this.width;
        if (i3 > i5 - 20 || i4 < 40) {
            this.x1 = i5 - 20;
            this.y1 = 40;
        }
        int i6 = this.x2 - 10;
        this.x2 = i6;
        int i7 = this.y2 + 6;
        this.y2 = i7;
        if (i6 < 40 || i7 > this.height - 20) {
            this.x2 = 40;
            this.y2 = this.height - 20;
        }
        int i8 = this.x3 + 10;
        this.x3 = i8;
        int i9 = this.y3 + 6;
        this.y3 = i9;
        if (i8 > i5 - 20 || i9 > this.height - 20) {
            this.x3 = i5 - 20;
            this.y3 = this.height - 20;
        }
        byte b = soHop;
        if (b == 7 || b == 9) {
            int i10 = this.y5 - 6;
            this.y5 = i10;
            int i11 = this.y6 + 6;
            this.y6 = i11;
            if (i10 < 40) {
                this.y5 = 40;
            }
            int i12 = this.height;
            if (i11 > i12 - 20) {
                this.y6 = i12 - 20;
            }
        }
        if (b == 9) {
            int i13 = this.x7 - 10;
            this.x7 = i13;
            if (i13 < 40) {
                this.x7 = 40;
            }
            int i14 = this.x8 + 10;
            this.x8 = i14;
            if (i14 > i5 - 20) {
                this.x8 = i5 - 20;
            }
        }
    }

    private void effectIn() {
        int i = this.x + 10;
        this.x = i;
        int i2 = this.y + 6;
        this.y = i2;
        int i3 = this.x4;
        if (i > i3 || i2 > this.y4) {
            this.x = i3;
            this.y = this.y4;
        }
        int i4 = this.x1 - 10;
        this.x1 = i4;
        int i5 = this.y1 + 6;
        this.y1 = i5;
        if (i4 < i3 || i5 > this.y4) {
            this.x1 = i3;
            this.y1 = this.y4;
        }
        int i6 = this.x2 + 10;
        this.x2 = i6;
        int i7 = this.y2 - 6;
        this.y2 = i7;
        if (i6 > i3 || i7 < this.y4) {
            this.x2 = i3;
            this.y2 = this.y4;
        }
        int i8 = this.x3 - 10;
        this.x3 = i8;
        int i9 = this.y3 - 6;
        this.y3 = i9;
        if (i8 < i3 || i9 < this.y4) {
            this.x3 = i3;
            this.y3 = this.y4;
        }
        byte b = soHop;
        if (b == 7 || b == 9) {
            int i10 = this.y5 + 6;
            this.y5 = i10;
            int i11 = this.y6 - 6;
            this.y6 = i11;
            int i12 = this.y4;
            if (i10 > i12) {
                this.y5 = i12;
            }
            if (i11 < i12) {
                this.y6 = i12;
            }
        }
        if (b == 9) {
            int i13 = this.x7 + 10;
            this.x7 = i13;
            if (i13 > i3) {
                this.x7 = i3;
            }
            int i14 = this.x8 - 10;
            this.x8 = i14;
            if (i14 < i3) {
                this.x8 = i3;
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.gameScr.paint(graphics);
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        Res.paintDlgDragonFull(graphics, 30, 30, this.width, this.height);
        GCanvas.resetTrans(graphics);
        graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x + 20, this.y + 20, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x1 + 20, this.y1 + 20, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x2 + 20, this.y2 + 20, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x3 + 20, this.y3 + 20, Graphics.VCENTER | Graphics.HCENTER);
        byte b = soHop;
        if (b == 7 || b == 9) {
            graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x5 + 20, this.y5 + 20, Graphics.VCENTER | Graphics.HCENTER);
            graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x6 + 20, this.y6 + 20, Graphics.VCENTER | Graphics.HCENTER);
        }
        graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x4 + 20, this.y4 + 20, Graphics.VCENTER | Graphics.HCENTER);
        if (soHop == 9) {
            graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x7 + 20, this.y7 + 20, Graphics.VCENTER | Graphics.HCENTER);
            graphics.drawRegion(imgBox, 0, 0, 32, 32, 0, this.x8 + 20, this.y8 + 20, Graphics.VCENTER | Graphics.HCENTER);
        }
        if (this.isShowKq) {
            graphics.setColor(0);
            graphics.setAlpha(100);
            switch (this.selected) {
                case 0:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x + 20, this.y + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect((this.x + 40) - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x + 40, (this.y + 20) - this.dem, 2);
                    break;
                case 1:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x1 + 20, this.y1 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect(this.x1 - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y1 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x1, (this.y1 + 20) - this.dem, 2);
                    break;
                case 2:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x2 + 20, this.y2 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect((this.x2 + 40) - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y2 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x2 + 40, (this.y2 + 20) - this.dem, 2);
                    break;
                case 3:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x3 + 20, this.y3 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect(this.x3 - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y3 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x3, (this.y3 + 20) - this.dem, 2);
                    break;
                case 4:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x4 + 20, this.y4 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect((this.x4 + 20) - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y4 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x4 + 20, (this.y4 + 20) - this.dem, 2);
                    break;
                case 5:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x5 + 20, this.y5 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect((this.x5 + 20) - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y5 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x5 + 20, (this.y5 + 20) - this.dem, 2);
                    break;
                case 6:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x6 + 20, this.y6 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect((this.x6 + 20) - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y6 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x6 + 20, (this.y6 + 20) - this.dem, 2);
                    break;
                case 7:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x7 + 20, this.y7 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect((this.x7 + 40) - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y7 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x7 + 40, (this.y7 + 20) - this.dem, 2);
                    break;
                case 8:
                    graphics.drawRegion(imgBox, 0, 32, 32, 32, 0, this.x8 + 20, this.y8 + 20, Graphics.VCENTER | Graphics.HCENTER);
                    graphics.fillRect(this.x8 - (Font.normalFont[1].getWidth(this.Chuoi) / 2), (this.y8 + 18) - this.dem, Font.normalFont[1].getWidth(this.Chuoi), 20);
                    Font.normalFont[0].drawString(graphics, this.Chuoi, this.x8, (this.y8 + 20) - this.dem, 2);
                    break;
            }
        }
        GCanvas.resetTrans(graphics);
        super.paint(graphics);
    }
}
