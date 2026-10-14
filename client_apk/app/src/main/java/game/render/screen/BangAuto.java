package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Char;
import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.model.Tilemap;
import game.model.skill.SkillManager;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class BangAuto extends Screen {
    public static int bomHp = 50;
    public static int bomMp = 50;
    public static int cmdx = 0;
    public static int cmtoX = 0;
    public static int cmvx = 0;
    public static int cmx = 0;
    public static int cmxLim = 0;
    public static boolean isAutoComeHome = false;
    public static String[] name = {"Trị", "Đánh", "Nhặt"};
    public static int tgDelay = 1;
    private Command cmdBatTat;
    private Command cmdCheck;
    private Command cmdComeHome;
    private Command cmdSelectSkill;
    private int countL;
    private int countR;
    public byte currentIndex;
    public int focus;
    public int index;
    String[] info;
    public boolean isAll;
    public boolean isAutoFire;
    public boolean isAutoGemItem;
    public boolean isAutoItem;
    public boolean isAutoMHP;
    public boolean isAutoParty;
    public boolean isAutoPotion;
    private boolean isChangeTab;
    public boolean isNhatHet;
    public boolean isOptionFocus;
    private int pa;
    public TField tfHp;
    public TField tfMp;
    private boolean trans;
    public int seleted = 0;
    String[] optionFC = {"Chế độ thường", "Đánh quái ", "Chọn NPC", "Chế độ chiếm thành", "Chế độ quốc chiến"};
    String[] optionDetail = {"Chọn toàn bộ", "Chỉ chọn quái, người trong chế độ đồ sát và khác quốc gia ", "Chỉ chọn NPC", "Chỉ chọn người khác bang", "Chỉ người khác nước"};
    boolean first = false;
    int[] IDSkillBuff = {-1, -1, -1};
    int[] IDSkillAuto = {-1, -1, -1};
    int[] vitri = {GCanvas.hw - 51, GCanvas.hw - 9, GCanvas.hw + 34};

    @Override
    public void switchToMe() {
        init();
        this.center.caption = "Chọn";
        this.center = this.cmdBatTat;
        this.isOptionFocus = false;
        super.switchToMe();
    }

    @Override
    public void init() {
        TField tField = this.tfMp;
        TField tField2 = this.tfHp;
        int i = GCanvas.hw;
        tField2.x = i;
        tField.x = i;
        this.tfHp.y = GCanvas.hh - 40;
        this.tfMp.y = GCanvas.hh - 15;
        this.seleted = 0;
        this.index = 0;
        this.tfHp.isFocus = false;
        this.tfMp.isFocus = false;
        this.isChangeTab = true;
    }

    public BangAuto() {
        TField tField = new TField();
        this.tfMp = tField;
        tField.width = 30;
        this.tfMp.height = 20;
        this.tfMp.isFocus = false;
        this.tfMp.setIputType(1);
        this.tfMp.setText(String.valueOf(bomMp));
        TField tField2 = new TField();
        this.tfHp = tField2;
        tField2.width = 30;
        this.tfHp.height = 20;
        this.tfHp.isFocus = false;
        this.tfHp.setIputType(1);
        this.tfHp.setText(String.valueOf(bomHp));
        Command command = new Command("", new IAction() {
            @Override
            public void perform() {
                if (Tilemap.lv == 0 || Tilemap.lv == 201 || Tilemap.lv == 70 || Tilemap.lv == 80) {
                    GCanvas.startOKDlg("Không thể bật auto ở đây.");
                } else {
                    BangAuto.this.doAuto();
                }
            }
        });
        this.cmdBatTat = command;
        this.center = command;
        this.cmdCheck = new Command("", new IAction() {
            @Override
            public void perform() {
                BangAuto.this.doCheck();
            }
        });
        this.cmdComeHome = new Command("", new IAction() {
            @Override
            public void perform() {
                BangAuto.isAutoComeHome = !BangAuto.isAutoComeHome;
            }
        });
        this.cmdSelectSkill = new Command("", new IAction() {
            @Override
            public void perform() {
                BangAuto.this.doSelectedSkillAuto();
            }
        });
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                BangAuto.this.dosetNumbeMpHp();
                GCanvas.gameScr.switchToMe();
            }
        });
    }

    public void dosetNumbeMpHp() {
        try {
            bomHp = Integer.parseInt(this.tfHp.getText());
            bomMp = Integer.parseInt(this.tfMp.getText());
        } catch (Exception unused) {
        }
    }

    public void doAuto() {
        try {
            if (!this.tfHp.getText().equals("") && !this.tfMp.getText().equals("")) {
                if (Integer.parseInt(this.tfHp.getText()) <= 100 && Integer.parseInt(this.tfMp.getText()) <= 100) {
                    Vector vector = new Vector();
                    String[] strArr = {"Auto đánh", "Auto bơm máu", "Auto đánh, bơm máu"};
                    if (GameScr.autoFight || GameScr.autoBomHMP) {
                        strArr = new String[]{"Auto đánh", "Auto bơm máu", "Auto đánh, bơm máu", "Tắt auto"};
                    }
                    for (int i0 = 0; i0 < strArr.length; i0++) {
                        final int i = i0;
                        vector.addElement(new Command(strArr[i], new IAction() {
                            @Override
                            public void perform() {
                                BangAuto.this.setOptionAuto(i);
                            }
                        }));
                    }
                    GCanvas.menu.startAt(vector, 2);
                    return;
                }
                GCanvas.startOKDlg("Giá trị không hợp lệ.");
                return;
            }
            GCanvas.startOKDlg("HP, Mp không được bỏ trống.");
        } catch (Exception unused) {
            GCanvas.startOKDlg("Vui lòng chỉ nhập số.");
        }
    }

    public void setOptionAuto(int i) {
        this.isAutoFire = false;
        this.isAll = false;
        this.isAutoMHP = false;
        if (i == 0) {
            this.isAutoFire = true;
            GameScr.autoFight = true;
            GameScr.autoBomHMP = false;
        } else if (i == 1) {
            this.isAutoMHP = true;
            GameScr.autoFight = false;
            GameScr.autoBomHMP = true;
        } else if (i == 2) {
            this.isAll = true;
            GameScr.autoFight = true;
            GameScr.autoBomHMP = true;
        } else {
            GameScr.autoFight = false;
            GameScr.autoBomHMP = false;
        }
        GameScr.saveAutoFight = GameScr.autoFight;
        bomHp = Integer.parseInt(this.tfHp.getText());
        bomMp = Integer.parseInt(this.tfMp.getText());
        tgDelay = ((int) (System.currentTimeMillis() / 100)) + 10;
        GameScr.timeDelayHpMp = ((int) (System.currentTimeMillis() / 1000)) + 1;
        GCanvas.gameScr.switchToMe();
        if (GameScr.autoFight) {
            GCanvas.gameScr.mainChar.lastXAuto = GCanvas.gameScr.mainChar.x;
            GCanvas.gameScr.mainChar.lastYAuto = GCanvas.gameScr.mainChar.y;
        }
    }

    public void doCheck() {
        int i = this.index;
        if (i == 0) {
            this.isAutoParty = !this.isAutoParty;
            return;
        }
        if (i == 2) {
            int i2 = this.seleted;
            if (i2 == 1) {
                this.isAutoPotion = !this.isAutoPotion;
            } else if (i2 == 2) {
                this.isAutoItem = !this.isAutoItem;
            } else if (i2 == 3) {
                this.isAutoGemItem = !this.isAutoGemItem;
            } else if (i2 == 4) {
                boolean z = this.isNhatHet;
                this.isNhatHet = !z;
                if (!z) {
                    this.isAutoPotion = true;
                    this.isAutoItem = true;
                    this.isAutoGemItem = true;
                } else {
                    this.isAutoPotion = false;
                    this.isAutoItem = false;
                    this.isAutoGemItem = false;
                }
            }
            if (this.isAutoPotion && this.isAutoItem && this.isAutoGemItem) {
                this.isNhatHet = true;
            } else {
                this.isNhatHet = false;
            }
        }
    }

    private void setLim() {
        int i = (GCanvas.gameScr.mainChar.getnSkill() * 30) - 114;
        cmxLim = i;
        if (i < 0) {
            cmxLim = 0;
        }
    }

    @Override
    public void update() {
        GCanvas.gameScr.update();
        if (!this.isOptionFocus) {
            int i = cmx;
            int i2 = cmtoX;
            if (i != i2) {
                int i3 = (i2 - i) << 2;
                cmvx = i3;
                int i4 = cmdx + i3;
                cmx = i + (i4 >> 4);
                cmdx = i4 & 15;
            }
            if (Math.abs(i2 - cmx) < 15 && cmx < 0) {
                cmtoX = 0;
            }
            if (Math.abs(cmtoX - cmx) < 10) {
                int i5 = cmx;
                int i6 = cmxLim;
                if (i5 > i6) {
                    cmtoX = i6;
                }
            }
            this.tfHp.update();
            this.tfMp.update();
        }
        super.update();
    }

    @Override
    public void updateKey() {
        boolean z;
        boolean z2;
        if (this.isOptionFocus) {
            if (GCanvas.isPointer((GCanvas.w / 2) - 75, (GCanvas.h / 2) - 80, 75, 30)) {
                z2 = false;
                z = true;
            } else if (GCanvas.isPointer(GCanvas.w / 2, (GCanvas.h / 2) - 80, 75, 30)) {
                z = false;
                z2 = true;
            } else {
                z = false;
                z2 = false;
            }
            if (GCanvas.isPointerClick) {
                if (z) {
                    GCanvas.keyPressed[4] = true;
                    GCanvas.isPointerClick = false;
                } else if (z2) {
                    GCanvas.keyPressed[6] = true;
                    GCanvas.isPointerClick = false;
                }
            }
            if (GCanvas.keyPressed[4]) {
                GCanvas.keyPressed[4] = false;
                GameScr.typeOptionFocus = (byte) (GameScr.typeOptionFocus - 1);
                if (GameScr.typeOptionFocus < 0) {
                    GameScr.typeOptionFocus = (byte) (this.optionFC.length - 1);
                }
                setInfo();
                this.countL = 5;
            } else if (GCanvas.keyPressed[6]) {
                GCanvas.keyPressed[6] = false;
                GameScr.typeOptionFocus = (byte) (GameScr.typeOptionFocus + 1);
                if (GameScr.typeOptionFocus > this.optionFC.length - 1) {
                    GameScr.typeOptionFocus = (byte) 0;
                }
                setInfo();
                this.countR = 5;
            }
        } else {
            if (GCanvas.isPointerClick && GCanvas.isPointer(GCanvas.hw - 80, GCanvas.hh - 80, 100, 23)) {
                int i = (GCanvas.px - (GCanvas.hw - 80)) / 50;
                this.index = i;
                if (i > 1) {
                    this.index = 1;
                }
            }
            int i2 = this.index;
            if (i2 != 0) {
                if (i2 == 1) {
                    setLim();
                    if (GCanvas.isPointer(GCanvas.hw - 60, GCanvas.hh + 35, 130, 30)) {
                        if (GCanvas.isPointerDown) {
                            if (!this.trans) {
                                this.pa = cmx;
                                this.trans = true;
                            }
                            if (Math.abs(GCanvas.pxLast - GCanvas.px) > 10) {
                                int i3 = this.pa + (GCanvas.pxLast - GCanvas.px);
                                cmtoX = i3;
                                if (i3 < 0) {
                                    cmtoX = 0;
                                }
                                int i4 = cmtoX;
                                int i5 = cmxLim;
                                if (i4 > i5 - 10) {
                                    cmtoX = i5 - 10;
                                }
                            }
                        }
                        if (GCanvas.isPointerClick) {
                            this.trans = false;
                            int i6 = ((cmtoX + GCanvas.px) - (GCanvas.hw - 60)) / 30;
                            if (this.focus == i6 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                                doSelectedSkillAuto();
                            } else {
                                this.focus = i6;
                            }
                        }
                        int i7 = cmx;
                        int i8 = cmtoX;
                        if (i7 != i8) {
                            int i9 = (i8 - i7) << 2;
                            cmvx = i9;
                            int i10 = cmdx + i9;
                            cmx = i7 + (i10 >> 4);
                            cmdx = i10 & 15;
                        }
                    }
                } else if (i2 == 2 && GCanvas.isPointerClick && GCanvas.isPointer(GCanvas.hw + 20, GCanvas.hh - 45, 40, 100)) {
                    int i11 = ((GCanvas.py - (GCanvas.hh - 40)) / 25) + 1;
                    this.seleted = i11;
                    if (i11 > 4) {
                        this.seleted = 4;
                    }
                    doCheck();
                }
            } else if (GCanvas.isPointerClick && GCanvas.isPointer(GCanvas.hw + 20, GCanvas.hh + 15, 20, 20)) {
                this.seleted = 3;
                doCheck();
            } else if (GCanvas.isPointerClick && GCanvas.isPointer(GCanvas.hw + 20, GCanvas.hh + 35, 20, 20)) {
                this.seleted = 4;
                isAutoComeHome = !isAutoComeHome;
            }
        }
        super.updateKey();
    }

    @Override
    public boolean keyPress(int i) {
        if (this.tfHp.isFocus) {
            if (this.tfHp.keyPressed(i)) {
                return true;
            }
        } else if (this.tfMp.isFocus && this.tfMp.keyPressed(i)) {
            return true;
        }
        return super.keyPress(i);
    }

    public void setInfo() {
        this.info = Font.normalFont[0].splitFontBStrInLine(this.optionDetail[GameScr.typeOptionFocus], 135);
        this.center = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
            }
        });
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GameScr.typeOptionFocus = BangAuto.this.currentIndex;
                GCanvas.gameScr.switchToMe();
            }
        });
        this.left = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.gameScr.paint(graphics);
        int i = this.countL;
        if (i > 0) {
            this.countL = i - 1;
        }
        int i2 = this.countR;
        if (i2 > 0) {
            this.countR = i2 - 1;
        }
        if (this.isOptionFocus) {
            Res.paintDlgFull(graphics, (GCanvas.w / 2) - 80, (GCanvas.h / 2) - 80, 160, 160);
            graphics.drawImage(Res.imgBar, ((GCanvas.w / 2) - 65) - this.countL, (GCanvas.h / 2) - 66, 3);
            graphics.drawRegion(Res.imgBar, 0, 0, 11, 7, 2, (GCanvas.w / 2) + 65 + this.countR, (GCanvas.h / 2) - 66, 3);
            Font.normalFont[0].drawString(graphics, this.optionFC[GameScr.typeOptionFocus], GCanvas.w / 2, (GCanvas.h / 2) - 73, 2);
            for (int i3 = 0; i3 < this.info.length; i3++) {
                Font.normalFont[0].drawString(graphics, this.info[i3], (GCanvas.w / 2) - 72, (ITEM_HEIGHT * i3) + ((GCanvas.h / 2) - 50), 0);
            }
            super.paint(graphics);
            return;
        }
        graphics.translate(GCanvas.hw - 80, GCanvas.hh - 80);
        Res.paintDlgFull(graphics, 2, 2, 160, 145);
        for (int i4 = 0; i4 < name.length - 1; i4++) {
            graphics.setColor(-6185824);
            int i5 = i4 * 52;
            graphics.fillRect(i5 + 55, 4, 1, 22);
            graphics.setColor(-8684933);
            graphics.fillRect(i5 + 56, 4, 1, 22);
        }
        for (int i6 = 0; i6 < name.length; i6++) {
            if (i6 == this.index) {
                Font.normalFont[0].drawString(graphics, name[i6], (i6 * 52) + 30, 8, 2);
            } else {
                Font.arialFont11.drawString(graphics, name[i6], (i6 * 52) + 30, 8, 2);
            }
            graphics.setColor(Res.color[i6]);
            graphics.fillRect(i6 + 2, 25 - i6, 162 - (i6 * 2), 1);
        }
        graphics.setColor(-7022096);
        graphics.setAlpha(70);
        graphics.fillRect((this.index * 52) + 5, 3, 52, 22);
        GCanvas.resetTrans(graphics);
        int i7 = this.index;
        if (i7 == 0) {
            Font.normalFont[0].drawString(graphics, "HP: ", GCanvas.hw - 60, GCanvas.hh - 35, 0);
            Font.normalFont[0].drawString(graphics, "MP: ", GCanvas.hw - 60, GCanvas.hh - 10, 0);
            Font.normalFont[0].drawString(graphics, "%", GCanvas.hw + 45, GCanvas.hh - 35, 1);
            Font.normalFont[0].drawString(graphics, "%", GCanvas.hw + 45, GCanvas.hh - 10, 1);
            graphics.setColor(-1);
            graphics.fillRect(GCanvas.hw - 65, GCanvas.hh + 15, 135, 1);
            Font.normalFont[0].drawString(graphics, "Tự vào nhóm:", GCanvas.hw - 60, GCanvas.hh + 20, 0);
            Res.imgCheck.drawFrame(this.seleted == 3 ? 1 : 0, GCanvas.hw + 30, GCanvas.hh + 28, 0, 3, graphics);
            Font.normalFont[0].drawString(graphics, "Tự về làng:", GCanvas.hw - 60, GCanvas.hh + 35, 0);
            Res.imgCheck.drawFrame(this.seleted == 4 ? 1 : 0, GCanvas.hw + 30, GCanvas.hh + 43, 0, 3, graphics);
            if (isAutoComeHome) {
                Res.imgCheck.drawFrame(2, GCanvas.hw + 30, GCanvas.hh + 43, 0, 3, graphics);
            }
            if (this.isAutoParty) {
                Res.imgCheck.drawFrame(2, GCanvas.hw + 30, GCanvas.hh + 28, 0, 3, graphics);
            }
            GCanvas.resetTrans(graphics);
            this.tfMp.paint(graphics);
            GCanvas.resetTrans(graphics);
            this.tfHp.paint(graphics);
        } else if (i7 == 1) {
            Font.normalFont[0].drawString(graphics, "Đánh:", GCanvas.hw - 15, GCanvas.hh - 50, 0);
            graphics.setColor(-1);
            graphics.drawRect(GCanvas.hw - 50, GCanvas.hh - 35, 17, 17);
            graphics.drawRect(GCanvas.hw - 8, GCanvas.hh - 35, 17, 17);
            graphics.drawRect(GCanvas.hw + 35, GCanvas.hh - 35, 17, 17);
            Font.normalFont[0].drawString(graphics, "Hỗ trợ:", GCanvas.hw - 15, GCanvas.hh - 8, 0);
            graphics.setColor(-1);
            graphics.drawRect(GCanvas.hw - 50, GCanvas.hh + 7, 17, 17);
            graphics.drawRect(GCanvas.hw - 8, GCanvas.hh + 7, 17, 17);
            graphics.drawRect(GCanvas.hw + 35, GCanvas.hh + 7, 17, 17);
            graphics.fillRect(GCanvas.hw - 65, GCanvas.hh + 33, 135, 1);
            paintSkillAuto(graphics);
        } else if (i7 == 2) {
            Font.normalFont[0].drawString(graphics, "HP + MP: ", GCanvas.hw - 60, GCanvas.hh - 45, 0);
            Res.imgCheck.drawFrame(this.seleted == 1 ? 1 : 0, GCanvas.hw + 30, GCanvas.hh - 38, 0, 3, graphics);
            if (this.isAutoPotion) {
                Res.imgCheck.drawFrame(2, GCanvas.hw + 30, GCanvas.hh - 38, 0, 3, graphics);
            }
            Font.normalFont[0].drawString(graphics, "Trang bị: ", GCanvas.hw - 60, GCanvas.hh - 20, 0);
            Res.imgCheck.drawFrame(this.seleted == 2 ? 1 : 0, GCanvas.hw + 30, GCanvas.hh - 13, 0, 3, graphics);
            if (this.isAutoItem) {
                Res.imgCheck.drawFrame(2, GCanvas.hw + 30, GCanvas.hh - 13, 0, 3, graphics);
            }
            Font.normalFont[0].drawString(graphics, "Nguyên liệu: ", GCanvas.hw - 60, GCanvas.hh + 5, 0);
            Res.imgCheck.drawFrame(this.seleted == 3 ? 1 : 0, GCanvas.hw + 30, GCanvas.hh + 13, 0, 3, graphics);
            if (this.isAutoGemItem) {
                Res.imgCheck.drawFrame(2, GCanvas.hw + 30, GCanvas.hh + 13, 0, 3, graphics);
            }
            Font.normalFont[0].drawString(graphics, "Nhặt hết: ", GCanvas.hw - 60, GCanvas.hh + 28, 0);
            Res.imgCheck.drawFrame(this.seleted == 4 ? 1 : 0, GCanvas.hw + 30, GCanvas.hh + 36, 0, 3, graphics);
            if (this.isNhatHet) {
                Res.imgCheck.drawFrame(2, GCanvas.hw + 30, GCanvas.hh + 36, 0, 3, graphics);
            }
        }
        super.paint(graphics);
    }

    private void paintSkillAuto(Graphics graphics) {
        graphics.setClip(GCanvas.hw - 65, GCanvas.hh + 35, 135, 26);
        graphics.ClipRec(GCanvas.hw - 65, GCanvas.hh + 35, 135, 26);
        int i = 0;
        graphics.translate(-cmx, 0);
        if (this.index == 1 && GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-388847);
            graphics.fillRect(((this.focus * 30) + GCanvas.hw) - 60, GCanvas.hh + 37, 22, 22);
        }
        for (int i2 = 0; i2 < GCanvas.gameScr.mainChar.getnSkill(); i2++) {
            int i3 = i2 * 30;
            int i4 = i3 + 11;
            GameData.imgSkillIcon.drawFrame(i2, (GCanvas.hw + i4) - 60, GCanvas.hh + 48, 0, 3, graphics);
            graphics.drawImage(Res.imgKhung, (i4 + GCanvas.hw) - 60, GCanvas.hh + 48, 3);
            Font.smallFontGREEN.drawString(graphics, ((int) Char.skillLevelLearnt[i2]) + "", ((i3 + 19) + GCanvas.hw) - 60, GCanvas.hh + 53, 1);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        if (this.IDSkillAuto != null) {
            int i5 = 0;
            while (true) {
                int[] iArr = this.IDSkillAuto;
                if (i5 >= iArr.length) {
                    break;
                }
                if (iArr[i5] != -1) {
                    GameData.imgSkillIcon.drawFrame(this.IDSkillAuto[i5], this.vitri[i5], GCanvas.hh - 36, 0, 0, graphics);
                }
                i5++;
            }
        }
        if (this.IDSkillBuff == null) {
            return;
        }
        while (true) {
            int[] iArr2 = this.IDSkillBuff;
            if (i >= iArr2.length) {
                return;
            }
            if (iArr2[i] != -1) {
                GameData.imgSkillIcon.drawFrame(this.IDSkillBuff[i], this.vitri[i], GCanvas.hh + 6, 0, 0, graphics);
            }
            i++;
        }
    }

    public boolean isBuffSkillAuto() {
        byte[][] bArr = {new byte[]{4, 5}, new byte[]{4, 5}, new byte[]{4, 5, 6, 7}, new byte[]{4, 5}, new byte[]{4, 5}};
        for (int i = 0; i < bArr[GCanvas.gameScr.mainChar.clazz].length; i++) {
            if (this.focus == bArr[GCanvas.gameScr.mainChar.clazz][i]) {
                return true;
            }
        }
        return false;
    }

    public void doSelectedSkillAuto() {
        if (Char.skillLevelLearnt[this.focus] == -1) {
            GCanvas.startOKDlg("Xin gặp Lâm tướng quân để học kỹ năng này");
            return;
        }
        boolean zIsBuffSkillAuto = isBuffSkillAuto();
        if (zIsBuffSkillAuto && SkillManager.SKILL_CAN_BUFF_TO_USER[GCanvas.gameScr.mainChar.clazz][this.focus - 4] == -1) {
            return;
        }
        if (Char.skillLevelLearnt[this.focus] > 0) {
            if (GCanvas.gameScr.mainChar.clazz != 2) {
                doGiveSkillToQuickSlotAuto(this.focus, zIsBuffSkillAuto);
                return;
            }
            int i = this.focus;
            if (i == 6) {
                GCanvas.startOKDlg("Không thể dùng skill này");
                return;
            } else {
                doGiveSkillToQuickSlotAuto(i, zIsBuffSkillAuto);
                return;
            }
        }
        GCanvas.startOKDlg("Chưa học kỹ năng này");
    }

    private void doGiveSkillToQuickSlotAuto(final int i, final boolean z) {
        Vector vector = new Vector();
        for (int i20 = 0; i20 < 3; i20++) {
            final int i2 = i20;
            vector.addElement(new Command("Phím số " + ((i2 * 2) + 1), new IAction() {
                @Override
                public void perform() {
                    if (z) {
                        GameScr.indexTabSlot = 1;
                        BangAuto.this.IDSkillBuff[i2] = i;
                    } else {
                        GameScr.indexTabSlot = 0;
                        BangAuto.this.IDSkillAuto[i2] = i;
                    }
                    MainChar.quickSlot[GameScr.indexTabSlot][i2].setIsSkill(i, GCanvas.gameScr.mainChar.clazz, z);
                    RMS.saveQuickSlot();
                    GameScr.indexTabSlot = 0;
                }
            }));
        }
        GCanvas.menu.startAt(vector, 2);
    }
}
