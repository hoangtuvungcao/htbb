package game.render.screen;

import game.model.Command;
import game.model.FilePack;
import game.model.FrameImage;
import game.model.IAction;
import game.model.ItemLuckyDraw;
import game.model.Point;
import game.model.Position;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Util;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class DialLuckyScr extends Screen {
    /** 31/10/2026: đang chạy updateKey không chạm thô (ManCu - vùng chạm kiểu chung đã xử lý) */
    boolean boc_updateKey;

    private static DialLuckyScr me;
    private Image imgCau_back;
    private Image imgDo;
    private Image imgDauHoi;
    private FrameImage imgFireWork;
    public int radius;
    public int degree;
    public int part;
    public int g;
    public int degreeKim;
    public int num;
    public int selectedNumber;
    private Position posCenter;
    private boolean isTurn;
    private boolean isPaint;
    public Screen lastScr;
    private Command cmdDial;
    private Command cmdWait;
    private Command cmdClose;
    private Command cmdErr;
    private Command cmdContinue;
    private Command cmdReChose;
    private boolean[] isFireWork;
    private Vector listFireWork;
    public boolean isStart;
    private int hBorder;
    static int[] posPaintX = new int[3];
    static int[] posPaintY = new int[3];
    public static byte[] arrayIndex = null;
    private boolean isable = false;
    private Vector listGift = new Vector();
    private long timePaint = 0;
    Random r = new Random(System.currentTimeMillis());
    byte cout = -1;

    public static class Gift {
        public short idPart;
        public byte type;
        public int xu;
        public int x;
        public int y;
        public int xp;
        public int luong;
        public String expire = "";
    }

    public static DialLuckyScr gI() {
        if (me != null) {
            return me;
        }
        DialLuckyScr dialLuckyScr = new DialLuckyScr();
        me = dialLuckyScr;
        return dialLuckyScr;
    }

    @Override
    public void switchToMe() {
        Canvas.keyHold[5] = false;
        super.switchToMe();
        doContinue();
        this.left = this.cmdErr;
    }

    public DialLuckyScr() {
        FilePack.init("/qs/qs");
        this.imgDo = FilePack.getImg("8Chars");
        this.imgDauHoi = FilePack.getImg("q");
        this.imgFireWork = FrameImage.init("st", 11, 11);
        this.imgCau_back = FilePack.getImg("wood21");
        FilePack.reset();
        if (Canvas.w < 200) {
            this.radius = 80;
        } else {
            this.radius = 90;
        }
        this.posCenter = new Position(Canvas.w, Canvas.hh + 1);
        this.part = 30;
        this.num = 360 / this.part;
        this.cmdDial = new Command("Bắt đầu", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.isable = true;
            }
        });
        this.cmdWait = new Command("Xin chờ", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.lastScr.switchToMe();
                DialLuckyScr.this.doContinue();
                DialLuckyScr.this.isStart = false;
            }
        });
        this.cmdErr = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.cmdReChose = new Command("Chọn lại", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.doContinue();
                ItemQsScreen.gI().switchToMe();
                DialLuckyScr.this.isStart = false;
            }
        });
        this.cmdContinue = new Command("Tiếp tục", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.doContinue();
                DialLuckyScr.this.isStart = false;
                DialLuckyScr.this.center = DialLuckyScr.this.cmdDial;
                DialLuckyScr.this.left = DialLuckyScr.this.cmdErr;
                DialLuckyScr.this.right = DialLuckyScr.this.cmdClose;
            }
        });
        this.center = this.cmdDial;
        this.right = this.cmdClose;
        this.degreeKim = 90;
        this.isFireWork = new boolean[3];
        this.listFireWork = new Vector();
        this.hBorder = Font.borderFont.getHeight();
    }

    protected void doContinue() {
        this.isPaint = false;
        this.isTurn = false;
        this.center = this.cmdDial;
        this.right = this.cmdClose;
        this.timePaint = 0L;
        for (int i = 0; i < 3; i++) {
            this.isFireWork[i] = false;
        }
        this.listFireWork.removeAllElements();
        this.listGift.removeAllElements();
        this.cout = (byte) -1;
    }

    @Override
    public void update() {
        if (this.lastScr != null) {
            this.lastScr.update();
        }
        if (this.g > 0) {
            this.degree -= this.g;
            if (this.degree < 0) {
                this.degree += 7200;
            }
            if (this.g < 10) {
                if ((this.degree / 20) % 30 == 0) {
                    this.g = 0;
                }
            } else {
                this.g--;
            }
        } else if (this.isTurn) {
            stop();
        }
        if (this.cout == 2 && this.listFireWork.size() == 0) {
            getResult();
            this.cout = (byte) 3;
        }
        if (this.isPaint) {
            if (this.center == this.cmdWait) {
                int aa = 0;
                for (int i = 0; i < this.isFireWork.length; i++) {
                    if (this.isFireWork[i]) {
                        aa++;
                    }
                }
                if (aa == 3) {
                    this.center = this.cmdClose;
                    this.left = this.cmdContinue;
                    this.right = this.cmdReChose;
                }
            }
            for (int i2 = 0; i2 < this.listFireWork.size(); i2++) {
                Point p = (Point) this.listFireWork.elementAt(i2);
                p.x += p.g;
                if (p.g > 1 || p.g < -1) {
                    p.g -= p.g / abs(p.g);
                }
                p.y += p.h;
                p.h++;
                p.color++;
                if (p.color > 20) {
                    this.listFireWork.removeElement(p);
                }
            }
            if (arrayIndex != null) {
                for (int i3 = 0; i3 < arrayIndex.length; i3++) {
                    if (!this.isFireWork[i3] && (System.currentTimeMillis() / 100) - this.timePaint > (i3 + 1) * 5) {
                        this.isFireWork[i3] = true;
                        addFire(posPaintX[i3], posPaintY[i3]);
                    }
                }
            }
        }
    }

    int abs(int a) {
        return a < 0 ? -a : a;
    }

    private void addFire(int x, int y) {
        for (int j = 0; j < 10; j++) {
            int dir = 1;
            if (j % 2 == 0) {
                dir = -1;
            }
            Point p = new Point(x, y);
            p.color = 0;
            p.g = dir * (this.r.nextInt(80) / 10);
            p.h = (-this.r.nextInt(70)) / 10;
            this.listFireWork.addElement(p);
        }
    }

    private void stop() {
        int ra;
        posPaintX = new int[arrayIndex.length];
        posPaintY = new int[arrayIndex.length];
        this.isTurn = false;
        this.isPaint = true;
        this.isable = false;
        this.timePaint = System.currentTimeMillis() / 100;
        for (int i = 0; i < arrayIndex.length; i++) {
            if (i == 0) {
                ra = 150;
            } else if (i == 1) {
                ra = 180;
            } else {
                ra = 210;
            }
            int ra2 = Util.fixangle(ra);
            int x1 = (this.radius * Util.cos(ra2)) >> 10;
            int y1 = (-(this.radius * Util.sin(ra2))) >> 10;
            posPaintX[i] = this.posCenter.x + x1;
            posPaintY[i] = this.posCenter.y + y1;
        }
    }

    @Override
    public void updateKey() {
        if (game.pokemon.ManCu.bat() && !this.boc_updateKey && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            this.boc_updateKey = true;
            try {
                game.pokemon.ManCu.phimKhongCham(new Runnable() {
                    public void run() {
                        DialLuckyScr.this.updateKey();
                    }
                });
            } finally {
                this.boc_updateKey = false;
            }
            return;
        }
        if (!this.isPaint) {
            if (Canvas.collisionCmdBar() == 1) {
                if (Canvas.isPointerDown) {
                    this.isable = true;
                    Canvas.keyHold[5] = true;
                }
                if (Canvas.isPointerRelease) {
                    Canvas.keyReleased[5] = true;
                }
            }
            if (Canvas.keyHold[5] && !this.isTurn && this.isable) {
                if (this.degreeKim < 270) {
                    this.degreeKim += 3;
                }
            } else if (this.degreeKim > 90 && this.isStart) {
                this.degreeKim -= 3;
            }
            if (Canvas.keyReleased[5]) {
                if (this.degreeKim > 90) {
                    start();
                }
                Canvas.keyReleased[5] = false;
            }
        }
        super.updateKey();
    }

    private void start() {
        if (this.isTurn || !this.isable) {
            return;
        }
        this.selectedNumber = this.degreeKim;
        GameService.gI().onStartLuckyDraw(ItemQsScreen.index);
        this.center = this.cmdWait;
    }

    public void onStart() {
        this.center = this.cmdWait;
        this.g = 100 + (this.selectedNumber - 90);
        this.isTurn = true;
        this.right = this.cmdErr;
        this.isStart = true;
        Canvas.endDlg();
    }

    @Override
    public void paint(Graphics g) {
        ItemLuckyDraw item;
        // 31/10/2026 (soi giao diện): tâm vòng quay theo cỡ màn hiện tại (trước cố định lúc tạo = mép phải: màn rộng chỉ
        // thấy nửa vòng sát mép, đổi cỡ thì lệch). Màn hẹp giữ nửa vòng ở mép phải như bản gốc.
        this.posCenter.x = Canvas.w >= 2 * this.radius + 160 ? Canvas.hw + this.radius / 2 : Canvas.w;
        this.posCenter.y = Canvas.hh + 1;
        if (this.lastScr != null) {
            this.lastScr.paint(g);
        }
        Canvas.resetTrans(g);
        if (game.pokemon.ManCu.bat()) {
            // 31/10/2026: tấm chung sau vòng quay, nút khối trong tấm (Bắt đầu / Chọn lại / Tiếp tục...), X = Đóng
            this.anNutLenh = true;
            boolean dong = this.right != null && "Đóng".equals(this.right.caption);
            int[] c = game.pokemon.ManCu.tam(g, this, "Vòng quay may mắn", dong ? game.pokemon.ManCu.viec(this.right) : null,
                    new Command[]{dong ? null : this.right, this.left, this.center});
            this.posCenter.x = c[0] + c[2] - 12;   // nửa vòng (bản gốc): mặt phẳng sát mép phải tấm, phần thưởng toả sang trái
            this.posCenter.y = c[1] + c[3] / 2;
            Canvas.resetTrans(g);
        }
        int deg = this.degree / 20;
        for (int i = 0; i < this.num; i++) {
            int d = deg + (i * this.part);
            if (d > 360) {
                d -= 360;
            }
            if (d >= 82 && d <= 278) {
                int dd = Util.fixangle(d);
                int x1 = (this.radius * Util.cos(dd)) >> 10;
                int y1 = (-(this.radius * Util.sin(dd))) >> 10;
                g.drawImage(this.imgCau_back, this.posCenter.x + x1, this.posCenter.y + y1, 3);
            }
        }
        if (this.isPaint) {
            paintGift(g);
        }
        int aa = 0;
        for (int i2 = 0; i2 < this.num; i2++) {
            int d2 = deg + (i2 * this.part);
            if (d2 > 360) {
                d2 -= 360;
            }
            if (d2 >= 82 && d2 <= 278) {
                int dd2 = Util.fixangle(d2);
                int x2 = (this.radius * Util.cos(dd2)) >> 10;
                int y2 = (-(this.radius * Util.sin(dd2))) >> 10;
                long time = (System.currentTimeMillis() / 100) - this.timePaint;
                if (!this.isPaint || d2 < 150 || d2 > 210 || (time <= (aa + 1) * 5 && time > ((aa + 1) * 5) - 5)) {
                    g.drawImage(this.imgDauHoi, this.posCenter.x + x2, this.posCenter.y + y2, 3);
                } else {
                    aa++;
                }
            }
        }
        g.setColor(9013127);
        g.fillRect(this.posCenter.x - 18, this.posCenter.y - 18, 18, 36);
        g.drawRegion(this.imgDo, 0, 0, this.imgDo.getWidth(), this.imgDo.getHeight(), 0, this.posCenter.x, this.posCenter.y, 40);
        g.drawRegion(this.imgDo, 0, 0, this.imgDo.getWidth(), this.imgDo.getHeight(), 1, this.posCenter.x, this.posCenter.y, 24);
        if (ItemQsScreen.index >= 0 && ItemQsScreen.index < ItemQsScreen.allItemLucky.size() && (item = (ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(ItemQsScreen.index)) != null) {
            item.paint(g, (this.posCenter.x - this.imgDo.getWidth()) + 28, this.posCenter.y);
        }
        paintKim(g);
        if (this.isPaint || this.g > 0) {
            paintFireWork(g);
        }
        super.paint(g);
    }

    private void paintFireWork(Graphics g) {
        for (int i = 0; i < this.listFireWork.size(); i++) {
            Point p = (Point) this.listFireWork.elementAt(i);
            int a = p.color / 5;
            if (a < 4) {
                this.imgFireWork.drawFrame(a, p.x, p.y, 0, 3, g);
            }
            this.cout = (byte) (this.cout + 1);
            if (this.cout >= 2) {
                this.cout = (byte) 2;
            }
        }
    }

    private void paintKim(Graphics g) {
        int dKim = Util.fixangle(this.degreeKim);
        int x1 = (((this.radius / 3) + 2) * Util.cos(dKim)) >> 10;
        int y1 = (-(((this.radius / 3) + 2) * Util.sin(dKim))) >> 10;
        int deg2 = this.degreeKim + 90;
        if (deg2 > 360) {
            deg2 -= 360;
        }
        int deg22 = Util.fixangle(deg2);
        int x2 = (6 * Util.cos(deg22)) >> 10;
        int y2 = (-(6 * Util.sin(deg22))) >> 10;
        int deg3 = this.degreeKim - 90;
        if (deg3 < 0) {
            deg3 += 360;
        }
        int deg4 = Util.fixangle(deg3);
        int x3 = (6 * Util.cos(deg4)) >> 10;
        int y3 = (-(6 * Util.sin(deg4))) >> 10;
        g.setColor(13935404);
        g.fillTriangle(this.posCenter.x + x1, this.posCenter.y + y1, this.posCenter.x + x2, this.posCenter.y + y2, this.posCenter.x + x3, this.posCenter.y + y3);
        g.fillRoundRect(this.posCenter.x - 6, this.posCenter.y - 6, 12, 12, 12, 12);
    }

    public void getResult() {
        String temp = "Bạn nhận được: ";
        if (arrayIndex != null) {
            for (int i = 0; i < arrayIndex.length; i++) {
                if ((System.currentTimeMillis() / 100) - this.timePaint > (i + 1) * 5) {
                    ItemLuckyDraw item = (ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(arrayIndex[i]);
                    temp = new StringBuffer(String.valueOf(temp)).append("\n").append(item.name).toString();
                }
            }
        }
        Canvas.startOKDlg(temp);
    }

    private void paintGift(Graphics g) {
        if (arrayIndex != null) {
            for (int i = 0; i < arrayIndex.length; i++) {
                if ((System.currentTimeMillis() / 100) - this.timePaint > (i + 1) * 5) {
                    ItemLuckyDraw item = (ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(arrayIndex[i]);
                    item.paint(g, posPaintX[i], posPaintY[i]);
                }
            }
        }
    }
}
