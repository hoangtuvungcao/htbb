package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.ModelCell;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

public class MiniGame extends Screen {
    /** 31/10/2026: đang chạy update không chạm thô (ManCu - vùng chạm kiểu chung đã xử lý) */
    boolean boc_update;

    public static MiniGame me;
    private int idCell;
    private boolean isClose;
    private boolean isOpen;
    private boolean isPointer;
    Command cmdSelcetAll;
    long timeBegin;
    public String[] arrInfo;
    public String[] result;
    public int xchar;
    public int ychar;
    public int yTo;
    Vector modelCell = new Vector();
    Command cmd = new Command("", new IAction() {
        @Override
        public void perform() {
        }
    });
    Command cmdPlay = new Command("Bắt đầu", new IAction() {
        @Override
        public void perform() {
            MiniGame.this.isOpen = true;
            MiniGame.this.timeBegin = (System.currentTimeMillis() / 1000) + 2;
            MiniGame.this.doClose();
            MiniGame.this.center = null;
        }
    });
    Command cmdSelect = new Command("Chọn", new AnonymousClass3());

    public static MiniGame gI() {
        if (me != null) {
            return me;
        }
        MiniGame miniGame = new MiniGame();
        me = miniGame;
        return miniGame;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    public void reSetGame() {
        this.idCell = 0;
        this.isClose = false;
        this.isPointer = false;
        this.isOpen = false;
        this.center = this.cmdPlay;
        this.right = this.cmd;
        this.xchar = 0;
        this.yTo = 0;
        this.ychar = 0;
        if (this.result != null) {
            byte b = 0;
            while (true) {
                byte i = b;
                if (i >= this.result.length) {
                    break;
                }
                this.result[i] = "";
                b = (byte) (i + 1);
            }
        }
        this.modelCell.removeAllElements();
        if (this.arrInfo != null) {
            byte b2 = 0;
            while (true) {
                byte i2 = b2;
                if (i2 < this.arrInfo.length) {
                    this.arrInfo[i2] = "";
                    b2 = (byte) (i2 + 1);
                } else {
                    return;
                }
            }
        }
    }

    public void updatePoint() {
        if (Canvas.isPointerClick && this.modelCell.size() > 0) {
            for (int i = 0; i < this.modelCell.size(); i++) {
                ModelCell obj = (ModelCell) this.modelCell.elementAt(i);
                if (obj != null && Canvas.px >= obj.x - 16 && Canvas.px <= obj.x + 16 && Canvas.py >= obj.y - 16 && Canvas.py <= obj.y + 16) {
                    Canvas.isPointerClick = false;
                    if (this.isPointer) {
                        this.idCell = obj.id;
                        this.center.action.perform();
                        return;
                    }
                }
            }
        }
    }

    public void loadModelCell(int sum) {
        this.modelCell.removeAllElements();
        for (int i = 0; i < sum; i++) {
            ModelCell obj = new ModelCell();
            obj.id = i;
            this.modelCell.addElement(obj);
        }
    }

    public void getInfoBox(byte id, String infoBox, String[] updateInfo) {
        this.arrInfo = updateInfo;
        String[] a = Font.arialFontW.splitFontBStrInLine(infoBox, 100);
        for (int i = 0; i < this.modelCell.size(); i++) {
            ModelCell obj = (ModelCell) this.modelCell.elementAt(i);
            if (obj.id == id) {
                obj.index = 1;
                starFlyString(a, obj.x, obj.y);
                return;
            }
        }
    }

    public void onMiniGame(Message msg) {
        try {
            byte nBox = msg.reader().readByte();
            this.arrInfo = new String[msg.reader().readByte()];
            for (byte i = 0; i < this.arrInfo.length; i = (byte) (i + 1)) {
                this.arrInfo[i] = msg.reader().readUTF();
            }
            loadModelCell(nBox);
            init();
        } catch (Exception e) {
        }
    }

    public void setSit() {
        int xbg = Canvas.w / 2;
        int ybg = Canvas.h / 2;
        int index = 0;
        int[][] arrX = {new int[]{(Canvas.w / 2) - (Canvas.w / 5), (Canvas.w / 2) + (Canvas.w / 5), Canvas.w / 2, (Canvas.w / 2) - (Canvas.w / 5), (Canvas.w / 2) + (Canvas.w / 5)}, new int[]{(Canvas.w / 2) - (Canvas.w / 5), Canvas.w / 2, (Canvas.w / 2) + (Canvas.w / 5), Canvas.w / 2, (Canvas.w / 2) - (Canvas.w / 5), Canvas.w / 2, (Canvas.w / 2) + (Canvas.w / 5)}, new int[]{(Canvas.w / 2) - (Canvas.w / 5), Canvas.w / 2, (Canvas.w / 2) + (Canvas.w / 5), (Canvas.w / 2) - (Canvas.w / 5), Canvas.w / 2, (Canvas.w / 2) + (Canvas.w / 5), (Canvas.w / 2) - (Canvas.w / 5), Canvas.w / 2, (Canvas.w / 2) + (Canvas.w / 5)}};
        int[][] arrY = {new int[]{(Canvas.h / 2) - (Canvas.h / 5), (Canvas.h / 2) - (Canvas.h / 5), Canvas.h / 2, (Canvas.h / 2) + (Canvas.h / 5), (Canvas.h / 2) + (Canvas.h / 5)}, new int[]{(Canvas.h / 2) - (Canvas.h / 5), (Canvas.h / 2) - (Canvas.h / 5), (Canvas.h / 2) - (Canvas.h / 5), Canvas.h / 2, (Canvas.h / 2) + (Canvas.h / 5), (Canvas.h / 2) + (Canvas.h / 5), (Canvas.h / 2) + (Canvas.h / 5)}, new int[]{(Canvas.h / 2) - (Canvas.h / 5), (Canvas.h / 2) - (Canvas.h / 5), (Canvas.h / 2) - (Canvas.h / 5), Canvas.h / 2, Canvas.h / 2, Canvas.h / 2, (Canvas.h / 2) + (Canvas.h / 5), (Canvas.h / 2) + (Canvas.h / 5), (Canvas.h / 2) + (Canvas.h / 5)}};
        if (this.modelCell.size() == 7) {
            index = 1;
        }
        if (this.modelCell.size() == 9) {
            index = 2;
        }
        for (int i = 0; i < this.modelCell.size(); i++) {
            ModelCell obj = (ModelCell) this.modelCell.elementAt(i);
            obj.x = xbg;
            obj.y = ybg;
            obj.xTo = arrX[index][i];
            obj.yTo = arrY[index][i];
            obj.saveX = obj.xTo;
            obj.saveY = obj.yTo;
        }
    }

    @Override
    public void init() {
        if (this.modelCell != null && this.modelCell.size() > 0) {
            setSit();
        }
        super.init();
    }

    public MiniGame() {
        this.center = this.cmdPlay;
        this.cmdSelcetAll = new Command("Mở hết", new AnonymousClass6());
    }

    class AnonymousClass3 implements IAction {
        AnonymousClass3() {
        }

        @Override
        public void perform() {
            Canvas.startYesNoDlgPutArr("Bạn có muốn mở không? ", MiniGame.this.arrInfo, new IAction() {
                @Override
                public void perform() {
                    Canvas.gameScr.gameService.playMiniGame((byte) 1, (byte) MiniGame.this.idCell);
                    Canvas.endDlg();
                }
            }, new IAction() {
                @Override
                public void perform() {
                    Canvas.endDlg();
                }
            });
        }
    }

    class AnonymousClass6 implements IAction {
        AnonymousClass6() {
        }

        @Override
        public void perform() {
            Canvas.startYesNoDlgPutArr("Bạn có muốn mở hết không? ", MiniGame.this.arrInfo, new IAction() {
                @Override
                public void perform() {
                    Canvas.gameScr.gameService.playMiniGame((byte) 2, (byte) 100);
                    Canvas.endDlg();
                    MiniGame.this.idCell = 0;
                    MiniGame.this.isClose = false;
                    MiniGame.this.isPointer = false;
                    MiniGame.this.isOpen = false;
                    MiniGame.this.center = MiniGame.this.cmdPlay;
                    MiniGame.this.right = MiniGame.this.cmd;
                    MiniGame.this.xchar = 0;
                    MiniGame miniGame = MiniGame.this;
                    MiniGame.this.yTo = 0;
                    miniGame.ychar = 0;
                    if (MiniGame.this.result != null) {
                        byte b = 0;
                        while (true) {
                            byte i = b;
                            if (i >= MiniGame.this.result.length) {
                                break;
                            }
                            MiniGame.this.result[i] = "";
                            b = (byte) (i + 1);
                        }
                    }
                    MiniGame.this.modelCell.removeAllElements();
                    if (MiniGame.this.arrInfo != null) {
                        byte b2 = 0;
                        while (true) {
                            byte i2 = b2;
                            if (i2 < MiniGame.this.arrInfo.length) {
                                MiniGame.this.arrInfo[i2] = "";
                                b2 = (byte) (i2 + 1);
                            } else {
                                return;
                            }
                        }
                    }
                }
            }, new IAction() {
                @Override
                public void perform() {
                    Canvas.endDlg();
                }
            });
        }
    }

    public void starFlyString(String[] info, int x, int y) {
        this.result = info;
        this.xchar = x;
        this.ychar = y;
        this.yTo = y - 20;
    }

    public void updateFlyText() {
        if (this.ychar != this.yTo) {
            this.ychar--;
            return;
        }
        this.ychar = this.yTo;
        if (this.result != null) {
            byte b = 0;
            while (true) {
                byte i = b;
                if (i < this.result.length) {
                    this.result[i] = "";
                    b = (byte) (i + 1);
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Canvas.gameScr.paint(g);
        boolean cham = game.pokemon.ManCu.bat();
        if (cham) {
            // 31/10/2026: tấm chung + nút khối trong tấm; hộp chạm được (vùng >= cỡ chạm) khi đến lượt chọn
            this.anNutLenh = true;
            game.pokemon.ManCu.tam(g, this, "Hộp may mắn", null, new Command[]{this.right, this.center});
            Canvas.resetTrans(g);
        } else {
            Res.paintDlgFull(g, 10, 20, Canvas.w - 20, Canvas.h - 60);
            Font.borderFont.drawString(g, "Hộp may mắn", Canvas.hw, 25, 2);
        }
        for (int i = 0; i < this.modelCell.size(); i++) {
            ModelCell obj = (ModelCell) this.modelCell.elementAt(i);
            if (obj != null) {
                obj.paint(g);
                if (cham && this.isPointer) {
                    final int id = obj.id;
                    int s = Math.max(34, game.pokemon.VungCham.MIN);
                    game.pokemon.VungCham.them(g, obj.x - s / 2, obj.y - s / 2, s, s, new IAction() {
                        public void perform() {
                            MiniGame.this.idCell = id;
                            if (MiniGame.this.center != null && MiniGame.this.center.action != null) {
                                MiniGame.this.center.action.perform();
                            }
                        }
                    }, "mini-hop-" + i);
                }
                if (Canvas.gameTick % 4 == 0 && this.idCell == i && !this.isClose) {
                    g.setColor(16777215);
                    g.drawRect(obj.x - 17, obj.y - 17, 34, 34);
                }
            }
        }
        if (this.ychar != this.yTo) {
            byte b = 0;
            while (true) {
                byte i2 = b;
                if (i2 >= this.result.length) {
                    break;
                }
                Font.normalFont[0].drawString(g, this.result[i2], this.xchar, this.ychar + (i2 * 14), 2);
                b = (byte) (i2 + 1);
            }
        }
        super.paint(g);
    }

    @Override
    public void update() {
        if (game.pokemon.ManCu.bat() && !this.boc_update && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            this.boc_update = true;
            try {
                game.pokemon.ManCu.phimKhongCham(new Runnable() {
                    public void run() {
                        MiniGame.this.update();
                    }
                });
            } finally {
                this.boc_update = false;
            }
            return;
        }
        for (int i = 0; i < this.modelCell.size(); i++) {
            ModelCell obj = (ModelCell) this.modelCell.elementAt(i);
            if (obj != null) {
                obj.update();
            }
        }
        if (this.isClose && this.timeBegin - (System.currentTimeMillis() / 1000) <= 0 && this.isOpen) {
            this.isOpen = false;
            doClose();
            this.center = this.cmdSelect;
            this.right = this.cmdSelcetAll;
            this.isPointer = true;
        }
        updateFlyText();
        super.update();
    }

    @Override
    public void updateKey() {
        if (Canvas.keyPressed[4]) {
            Canvas.keyPressed[4] = false;
            if (!this.isClose) {
                this.idCell--;
                if (this.idCell < 0) {
                    this.idCell = 0;
                }
            }
        } else if (Canvas.keyPressed[6]) {
            Canvas.keyPressed[6] = false;
            if (!this.isClose) {
                this.idCell++;
                if (this.idCell > this.modelCell.size() - 1) {
                    this.idCell = this.modelCell.size() - 1;
                }
            }
        }
        updatePoint();
        super.updateKey();
    }

    public void doClose() {
        this.isClose = !this.isClose;
        for (int i = 0; i < this.modelCell.size(); i++) {
            ModelCell obj = (ModelCell) this.modelCell.elementAt(i);
            if (this.isClose) {
                obj.xTo = Canvas.w / 2;
                obj.yTo = Canvas.h / 2;
            } else {
                obj.xTo = obj.saveX;
                obj.yTo = obj.saveY;
            }
        }
    }
}
