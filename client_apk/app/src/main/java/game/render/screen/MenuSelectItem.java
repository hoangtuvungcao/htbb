package game.render.screen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import com.htbb.client.GameMidlet;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.Char;
import game.model.Command;
import game.model.IAction;
import game.model.Scroll;
import game.model.ScrollResult;
import game.model.Tilemap;
import game.networklogic.GameService;
import game.render.GCanvas;
import network.Session_ME;

public class MenuSelectItem extends Screen {
    public static Bitmap imgAuto;
    public static Bitmap imgAuto36;
    public static Bitmap imgDoParty;
    public static Bitmap imgDoParty36;
    public static Bitmap imgFc;
    public static Bitmap imgIChange;
    public static Bitmap imgIChange36;
    public static Bitmap imgIChat;
    public static Bitmap imgIChat36;
    public static Bitmap imgIDangxuat;
    public static Bitmap imgIDangxuat36;
    public static Bitmap imgIDosat;
    public static Bitmap imgIDosat36;
    public static Bitmap imgINapxu;
    public static Bitmap imgINapxu36;
    public static Bitmap imgIThoat;
    public static Bitmap imgIThoat36;
    public static Bitmap imgIXuongngua;
    public static Bitmap imgIXuongngua36;
    public static Bitmap[] imgMenu;
    public static Bitmap imgWap;
    public static Bitmap imgWap36;
    public static boolean isClose;
    public static MenuSelectItem me;
    public static int wImg;
    int focus;
    public int height;
    Scroll msc = new Scroll();
    int selected;
    public int width;
    public int x1;
    public int x2;
    public int y1;
    public int y2;

    static {
        try {
            if (imgWap == null) {
                imgFc = Image.createImageAsset("fc.png");
                imgWap = Image.loadImageFromAsset("wap.png");
                imgIChat36 = Image.loadImageFromAsset("chat1.png");
                imgIChange36 = Image.loadImageFromAsset("change1.png");
                imgINapxu36 = Image.loadImageFromAsset("napxu1.png");
                imgAuto36 = Image.loadImageFromAsset("icon_automode36.png");
                imgIXuongngua36 = Image.loadImageFromAsset("xuongngua1.png");
                imgIDosat36 = Image.loadImageFromAsset("dosa1t.png");
                imgIDangxuat36 = Image.loadImageFromAsset("dangxuat1.png");
                imgWap36 = Image.loadImageFromAsset("wap36.png");
                imgDoParty36 = Image.loadImageFromAsset("fastmenu36.png");
                wImg = imgAuto.getWidth() / 2;
            }
        } catch (Exception e) {
            System.out.println(" fc loi load " + e.toString());
        }
    }

    public static MenuSelectItem gI() {
        MenuSelectItem menuSelectItem = me;
        if (menuSelectItem != null) {
            return menuSelectItem;
        }
        MenuSelectItem menuSelectItem2 = new MenuSelectItem();
        me = menuSelectItem2;
        return menuSelectItem2;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
        this.msc.clear();
    }

    @Override
    public void init() {
        this.height = 20;
        this.width = 20;
        this.y1 = GCanvas.h - 160;
        this.y2 = GCanvas.h - 50;
    }

    public MenuSelectItem() {
        this.right = new Command("", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
            }
        });
    }

    @Override
    public void update() {
        boolean z;
        int i;
        GCanvas.gameScr.update();
        int i2 = this.y1 + 4;
        this.y1 = i2;
        if (i2 > GCanvas.h - 130) {
            this.y1 = GCanvas.h - 130;
        }
        int i3 = this.y2 - 4;
        this.y2 = i3;
        if (i3 < GCanvas.h - 80) {
            this.y2 = GCanvas.h - 80;
        }
        if (GCanvas.currentDialog != null) {
            return;
        }
        this.focus = -1;
        if (GCanvas.currentDialog == null) {
            ScrollResult scrollResultUpdateKey = this.msc.updateKey();
            if (this.msc.FocusNew && (scrollResultUpdateKey.isDowning || scrollResultUpdateKey.isFinish)) {
                this.focus = scrollResultUpdateKey.selected;
                z = true;
            } else {
                z = false;
            }
            this.msc.updatecm();
            if (!GCanvas.isPointerDown) {
                if (this.msc.FocusNew && z && (i = this.focus) > -1) {
                    switch (i) {
                        case 0:
                            GameService.gI().setConfig(6);
                            GCanvas.endDlg();
                            if (this.right != null) {
                                this.right.action.perform();
                                isClose = true;
                                GCanvas.isPointerClick = false;
                            }
                            break;
                        case 1:
                            if (!GCanvas.gameScr.isVTC()) {
                                GCanvas.gameScr.doNapXu();
                            }
                            GCanvas.gameScr.switchToMe();
                            if (this.right != null) {
                                this.right.action.perform();
                                isClose = true;
                                GCanvas.isPointerClick = false;
                            }
                            break;
                        case 2:
                            GCanvas.gameScr.switchToMe();
                            GCanvas.gameScr.gameService.requestKiller();
                            if (this.right != null) {
                                this.right.action.perform();
                                isClose = true;
                                GCanvas.isPointerClick = false;
                            }
                            break;
                        case 3:
                            GCanvas.gameScr.unRideHorse();
                            if (this.right != null) {
                                this.right.action.perform();
                                isClose = true;
                                GCanvas.isPointerClick = false;
                            }
                            break;
                        case 4:
                            if (Tilemap.lv == 0 || Tilemap.lv == 201 || Tilemap.lv == 70 || Tilemap.lv == 80) {
                                GCanvas.startOKDlg("Không thể bật auto ở đây.");
                                if (this.right != null) {
                                    this.right.action.perform();
                                    isClose = true;
                                    GCanvas.isPointerClick = false;
                                }
                            } else {
                                GCanvas.autoTab.switchToMe();
                                GCanvas.clearKeyPressed();
                                GCanvas.clearKeyHold();
                                return;
                            }
                            break;
                        case 5:
                            GCanvas.gameScr.switchToMe();
                            if (GCanvas.gameScr.focusedActor != null && GCanvas.gameScr.focusedActor.catagory != 10 && GCanvas.gameScr.focusedActor.catagory != 2 && GCanvas.gameScr.focusedActor.catagory != 1 && GCanvas.gameScr.focusedActor.catagory != 6 && GCanvas.gameScr.focusedActor.catagory != 3 && GCanvas.gameScr.focusedActor.catagory != 11 && ((Char) GCanvas.gameScr.focusedActor).idBoss == -1) {
                                GCanvas.gameScr.doParty();
                            }
                            if (this.right != null) {
                                this.right.action.perform();
                                isClose = true;
                                GCanvas.isPointerClick = false;
                            }
                            break;
                        case 6:
                            GCanvas.startYesNoDlg("Bạn có muốn đăng nhập lại không?", new IAction() {
                                @Override
                                public void perform() {
                                    GCanvas.instance.init();
                                    Session_ME.gI().close();
                                    Tilemap.loadMap(0, null);
                                    ServerListScr.gI().switchToMe();
                                    GCanvas.endDlg();
                                }
                            }, new IAction() {
                                @Override
                                public void perform() {
                                    GCanvas.gameScr.switchToMe();
                                    GCanvas.endDlg();
                                }
                            });
                            if (this.right != null) {
                                this.right.action.perform();
                                isClose = true;
                                GCanvas.isPointerClick = false;
                            }
                            break;
                        case 7:
                            try {
                                try {
                                    if (GameMidlet.url != "") {
                                        Intent intent = new Intent("android.intent.action.VIEW");
                                        intent.setData(Uri.parse(GameMidlet.url));
                                        GCanvas.gCanvas.startActivity(intent);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (this.right != null) {
                                    this.right.action.perform();
                                    isClose = true;
                                    GCanvas.isPointerClick = false;
                                }
                                break;
                            } catch (Throwable th) {
                                try {
                                    throw th;
                                } catch (Exception unused) {
                                }
                            }
                            break;
                        default:
                            if (this.right != null) {
                                this.right.action.perform();
                                isClose = true;
                                GCanvas.isPointerClick = false;
                            }
                            break;
                    }
                }
                this.msc.FocusNew = false;
                if (GCanvas.py < GCanvas.h - 50 && this.right != null) {
                    this.right.action.perform();
                    isClose = true;
                    GCanvas.isPointerClick = false;
                }
            }
        }
        super.update();
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.gameScr.paint(graphics);
        this.msc.setStyle(8, 45, 10, GCanvas.h - 50, GCanvas.w - 20, 40, false, 0);
        this.msc.setClip(graphics, 5, GCanvas.h - 60, GCanvas.w - 10, 60);
        Bitmap[] bitmapArr = {imgIChange36, imgINapxu36, imgIDosat36, imgIXuongngua36, imgAuto36, imgDoParty36, imgIDangxuat36, imgWap36};
        for (int i = 0; i < 8; i++) {
            if (this.focus == i) {
                graphics.drawImage(imgFc, (i * 45) + 28, GCanvas.h - 32, Graphics.VCENTER | Graphics.HCENTER);
            }
            graphics.drawImage(bitmapArr[i], (i * 45) + 28, GCanvas.h - 32, Graphics.VCENTER | Graphics.HCENTER);
        }
        super.paint(graphics);
    }
}
