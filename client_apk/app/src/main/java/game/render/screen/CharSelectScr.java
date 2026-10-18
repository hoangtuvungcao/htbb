package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.model.Tilemap;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import network.Session_ME;

public class CharSelectScr extends Screen {
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public static Bitmap imgBg;
    public static CharSelectScr me;
    public MainChar[] charInfos;
    MainChar[] chars;
    public Command cmdClose;
    public Command cmdDel;
    public Command cmdNew;
    public Command cmdSelect;
    private int goc;
    boolean isBat;
    private int xTo;
    private int yTo;
    public int selected = 0;
    private int radius = 200;

    public static CharSelectScr gI() {
        CharSelectScr charSelectScr = me;
        if (charSelectScr != null) {
            return charSelectScr;
        }
        CharSelectScr charSelectScr2 = new CharSelectScr();
        me = charSelectScr2;
        return charSelectScr2;
    }

    public void setCharList(MainChar[] mainCharArr) {
        if (Session_ME.gI().connected) {
            GCanvas.loginScr.savePass();
            GCanvas.gameScr.loadCamera();
            if (!GCanvas.gameScr.isNewVersionAvailable) {
                GCanvas.endDlg();
            }
            this.charInfos = mainCharArr;
            MainChar[] mainCharArr2 = new MainChar[mainCharArr.length];
            this.chars = mainCharArr;
            this.cmdSelect = new Command("", new IAction() {
                @Override
                public void perform() {
                    GCanvas.startWaitDlg("Xin chờ..", true);
                    GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 1);
                    GCanvas.gameScr.mainChar.imgWpa = CharSelectScr.this.chars[CharSelectScr.this.selected].imgWpa;
                    GCanvas.gameScr.mainChar.dxWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dxWear;
                    GCanvas.gameScr.mainChar.dyWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dyWear;
                    ChangScr.gI().switchToMe();
                    CharSelectScr.this.chars = null;
                    CharSelectScr.me = null;
                    CharSelectScr.this.isBat = false;
                }
            });
            this.cmdNew = null;
            this.cmdDel = new Command("Xóa", new IAction() {
                @Override
                public void perform() {
                    if (CharSelectScr.this.cmdDel.caption.equals("Xóa")) {
                        GCanvas.startYesNoDlg("Bạn thật sự muốn xóa Pokémon này?", new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.startWaitDlg("Xin chờ..", true);
                                GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 2);
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.currentDialog = null;
                            }
                        });
                    } else if (CharSelectScr.this.cmdDel.caption.equals("Khôi phục")) {
                        GCanvas.startYesNoDlg("Bạn muốn khôi phục Pokémon này?", new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.startWaitDlg("Xin chờ..", true);
                                GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 3);
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.currentDialog = null;
                            }
                        });
                    }
                }
            });
            this.cmdClose = new Command("Thoát", new IAction() {
                @Override
                public void perform() {
                    Session_ME.gI().close();
                    if (!GCanvas.loginScr.isRememPass) {
                        GCanvas.loginScr.switchToMe();
                        return;
                    }
                    GCanvas.instance.init();
                    Session_ME.gI().close();
                    Tilemap.loadMap(0, null);
                    ServerListScr.gI().switchToMe();
                }
            });
            if (this.selected < this.chars.length) {
                this.center = this.cmdSelect;
                this.left = this.cmdDel;
            } else {
                this.center = this.cmdNew;
                this.left = null;
            }
            this.right = this.cmdClose;
            if (mainCharArr.length > 0) {
                if (mainCharArr[this.selected].using == 1) {
                    this.cmdDel.caption = "Xóa";
                } else {
                    this.cmdDel.caption = "Khôi phục";
                }
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        ServerListScr.gI().paintBg(graphics);
        GCanvas.resetTrans(graphics);
        int i = GCanvas.hw - 60;
        int i2 = GCanvas.hh;
        int i3 = 0;
        while (true) {
            if (i3 >= 3) {
                break;
            }
            if (i3 == this.selected) {
                int i4 = -1007872;
                graphics.setColor(-1007872);
                MainChar[] mainCharArr = this.charInfos;
                if (i3 < mainCharArr.length) {
                    if (mainCharArr.length > 0 && mainCharArr[i3].using != 1) {
                        i4 = -14571355;
                    }
                    graphics.setColor(i4);
                }
            } else {
                int i5 = -6132992;
                graphics.setColor(-6132992);
                MainChar[] mainCharArr2 = this.charInfos;
                if (i3 < mainCharArr2.length) {
                    if (mainCharArr2.length > 0 && mainCharArr2[i3].using != 1) {
                        i5 = -16742267;
                    }
                    graphics.setColor(i5);
                }
            }
            int i6 = i + 3;
            int i7 = i2 - 40;
            graphics.fillRect(i6, i7, 34, 84);
            graphics.setColor(-15397376);
            graphics.drawRect(i6, i7, 34, 84);
            MainChar[] mainCharArr3 = this.chars;
            if (i3 < mainCharArr3.length) {
                mainCharArr3[i3].x = (short) (i + 20);
                this.chars[i3].y = (short) (i2 + 34);
                MainChar mainChar = this.chars[i3];
                mainChar.paintAvatar(graphics, mainChar.x, this.chars[i3].y);
            }
            i3++;
            i += 40;
        }
        if (this.selected < this.chars.length) {
            if (GCanvas.screenlevel > 1) {
                Font.arialFontBlack.drawString(graphics, this.charInfos[this.selected].name + " lv: " + ((int) (this.charInfos[this.selected].lastLv > 0 ? this.charInfos[this.selected].lastLv : (short) 1)), GCanvas.hw, GCanvas.hh + 51, 2);
            }
            Font.normalFontColor1.drawString(graphics, this.charInfos[this.selected].name + " lv: " + ((int) (this.charInfos[this.selected].lastLv > 0 ? this.charInfos[this.selected].lastLv : (short) 1)), GCanvas.hw, GCanvas.hh + 50, 2);
            if (this.charInfos[this.selected].using == 0) {
                Font.normalFontColor1.drawString(graphics, "Còn lại: " + ((int) this.charInfos[this.selected].day) + " ngày", GCanvas.hw, GCanvas.hh + 58, 2);
            }
            if (this.charInfos[this.selected].nationID != -1) {
                graphics.drawRegion(Res.imgNation, 0, this.charInfos[this.selected].nationID * 11, 11, 11, 0, (GCanvas.hw - (Font.normalFontColor1.getWidth(this.charInfos[this.selected].name + " lv: " + ((int) (this.charInfos[this.selected].lastLv > 0 ? this.charInfos[this.selected].lastLv : (short) 1))) / 2)) - 15, GCanvas.hh + 50, 20);
            }
        }
        super.paint(graphics);
    }

    @Override
    public void update() {
        try {
            GCanvas.loginScr.updateCamera();
            for (int i4 = 0; i4 < this.chars.length; i4++) {
                this.chars[i4].update();
            }
            boolean z;
            if (GCanvas.isKeyPressed(4)) {
                int sel = this.selected - 1;
                this.selected = sel;
                if (sel < 0) {
                    this.selected = 2;
                }
                MainChar[] infos = this.charInfos;
                if (infos.length > 0) {
                    int s = this.selected;
                    if (s < infos.length) {
                        if (infos[s].using == 1) {
                            this.cmdDel.caption = "Xóa";
                        } else {
                            this.cmdDel.caption = "Khôi phục";
                        }
                    } else {
                        this.cmdDel.caption = "";
                    }
                }
                z = true;
            } else if (GCanvas.isKeyPressed(6)) {
                int sel = this.selected + 1;
                this.selected = sel;
                if (sel > 2) {
                    this.selected = 0;
                }
                MainChar[] infos = this.charInfos;
                if (infos.length > 0) {
                    int s = this.selected;
                    if (s < infos.length) {
                        if (infos[s].using == 1) {
                            this.cmdDel.caption = "Xóa";
                        } else {
                            this.cmdDel.caption = "Khôi phục";
                        }
                    } else {
                        this.cmdDel.caption = "";
                    }
                }
                z = true;
            } else {
                z = false;
            }
            int i;
            if (GCanvas.isPointer(GCanvas.hw - 58, GCanvas.hh - 40, 115, 84)) {
                i = (GCanvas.px - (GCanvas.hw - 58)) / 38;
                z = true;
            } else {
                i = -1;
            }
            if (z) {
                if (GCanvas.isPointerClick) {
                    if (i == this.selected) {
                        if (this.center == this.cmdNew) {
                            CreateCharScr.gI().switchToMe();
                            this.isBat = false;
                            this.left = null;
                        } else if (this.center == this.cmdSelect) {
                            this.left = this.cmdDel;
                            if (this.charInfos[this.selected].using != 1) {
                                return;
                            }
                            if (this.center != null) {
                                this.center.action.perform();
                            }
                        }
                    }
                    if (i < 3 && i >= 0) {
                        this.selected = i;
                        this.isBat = true;
                        MainChar[] infos = this.charInfos;
                        if (infos.length > 0) {
                            if (i < infos.length) {
                                if (infos[i].using == 1) {
                                    this.cmdDel.caption = "Xóa";
                                } else {
                                    this.cmdDel.caption = "Khôi phục";
                                }
                            } else {
                                this.cmdDel.caption = "";
                            }
                        }
                    }
                }
                if (this.selected < this.chars.length) {
                    this.center = this.cmdSelect;
                } else {
                    this.center = this.cmdNew;
                }
            }
            int i2 = cmy;
            int i3 = cmtoY;
            if (i2 != i3) {
                int i9 = (i3 - i2) << 2;
                cmvy = i9;
                int i10 = cmdy + i9;
                cmy = i2 + (i10 >> 4);
                cmdy = i10 & 15;
            }
            super.update();
        } catch (Exception e) {
        }
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
}
