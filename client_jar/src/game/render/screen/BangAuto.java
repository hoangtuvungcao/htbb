package game.render.screen;

import game.model.Char;
import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.model.Tilemap;
import game.model.skill.SkillManager;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class BangAuto extends Screen {
    public int index;
    public int focus;
    private boolean isChangeTab;
    private int countL;
    private int countR;
    public TField tfMp;
    public boolean isAutoParty;
    public boolean isAutoPotion;
    public boolean isAutoItem;
    public boolean isAutoGemItem;
    public boolean isNhatHet;
    public boolean isOptionFocus;
    public boolean isAutoFire;
    public boolean isAutoMHP;
    public boolean isAll;
    private Command cmdCheck;
    private Command cmdComeHome;
    private Command cmdBatTat;
    private Command cmdSelectSkill;
    public static int cmtoX;
    public static int cmx;
    public static int cmdx;
    public static int cmvx;
    public static int cmxLim;
    public static boolean isAutoComeHome;
    String[] info;
    public byte currentIndex;
    public static int bomHp = 50;
    public static int bomMp = 50;
    public static int tgDelay = 1;
    public static String name = "";
    public int seleted = 0;
    String[] optionFC = {"Chế độ thường", "Đánh quái ", "Chọn NPC", "Chế độ chiếm Gym", "Chế độ chiến vùng"};
    String[] optionDetail = {"Chọn toàn bộ", "Chỉ chọn quái, người trong chế độ PK và khác vùng ", "Chỉ chọn NPC", "Chỉ chọn người khác đội", "Chỉ người khác nước"};
    boolean first = false;
    int[] IDSkillBuff = {-1, -1, -1};
    int[] IDSkillAuto = {-1, -1, -1};
    public TField tfHp = new TField();

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
        int i = Canvas.hw;
        tField2.x = i;
        tField.x = i;
        this.tfHp.y = Canvas.hh - 50;
        this.tfMp.y = Canvas.hh - 25;
        this.seleted = 0;
        this.index = 0;
        this.tfHp.isFocus = false;
        this.tfMp.isFocus = false;
        this.isChangeTab = true;
    }

    public BangAuto() {
        this.tfHp.width = 30;
        this.tfHp.height = 20;
        this.tfHp.isFocus = false;
        this.tfHp.setIputType(1);
        this.tfHp.setText(String.valueOf(bomHp));
        this.tfMp = new TField();
        this.tfMp.width = 30;
        this.tfMp.height = 20;
        this.tfMp.isFocus = false;
        this.tfMp.setIputType(1);
        this.tfMp.setText(String.valueOf(bomMp));
        this.cmdBatTat = new Command("", new IAction() {
            @Override
            public void perform() {
                if (Tilemap.lv == 0 || Tilemap.lv == 201 || Tilemap.lv == 70 || Tilemap.lv == 80) {
                    Canvas.startOKDlg("Bản đồ này không cho phép tự động đánh.");
                } else {
                    BangAuto.this.doAuto();
                }
            }
        });
        this.center = this.cmdBatTat;
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
        this.left = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                BangAuto.this.dosetNumbeMpHp();
                Canvas.gameScr.switchToMe();
            }
        });
        this.right = null;
    }

    public void dosetNumbeMpHp() {
        try {
            bomHp = Integer.parseInt(this.tfHp.getText());
            bomMp = Integer.parseInt(this.tfMp.getText());
        } catch (Exception e) {
        }
    }

    public void doAuto() {
        try {
            if (this.tfHp.getText().equals("") || this.tfMp.getText().equals("")) {
                Canvas.startOKDlg("HP, PP không được bỏ trống.");
                return;
            }
            if (Integer.parseInt(this.tfHp.getText()) > 100 || Integer.parseInt(this.tfMp.getText()) > 100) {
                Canvas.startOKDlg("Giá trị không hợp lệ.");
                return;
            }
            Vector mn = new Vector();
            String[] info = {"Auto đánh", "Auto bơm máu", "Auto đánh, bơm máu"};
            if (GameScr.autoFight || GameScr.autoBomHMP) {
                info = new String[]{"Auto đánh", "Auto bơm máu", "Auto đánh, bơm máu", "Tắt auto"};
            }
            for (int i = 0; i < info.length; i++) {
                final int o = i;
                Command cmd = new Command(info[i], new IAction() {
                    @Override
                    public void perform() {
                        BangAuto.this.setOptionAuto(o);
                    }
                });
                mn.addElement(cmd);
            }
            Canvas.menu.startAt(mn, 2);
        } catch (Exception e) {
            Canvas.startOKDlg("Vui lòng chỉ nhập số.");
        }
    }

    /**
     * Auto gốc của game (đánh / bơm máu không qua máy chủ) đã thay bằng Tự động đánh CHÍNH THỨC (TuDong, máy chủ
     * điều khiển giờ / nhịp / vùng): mọi lựa chọn ở đây mở bảng Tự động đánh.
     */
    public void setOptionAuto(int i) {
        this.isAutoFire = false;
        this.isAll = false;
        this.isAutoMHP = false;
        GameScr.autoFight = false;
        GameScr.autoBomHMP = false;
        GameScr.saveAutoFight = false;
        try {
            bomHp = Integer.parseInt(this.tfHp.getText());
            bomMp = Integer.parseInt(this.tfMp.getText());
        } catch (Exception e) {
            // giữ ngưỡng cũ
        }
        Canvas.gameScr.switchToMe();
        BangTuDong.mo();
    }


    public void doCheck() {
        if (this.index == 0) {
            this.isAutoParty = !this.isAutoParty;
            return;
        }
        if (this.index == 2) {
            switch (this.seleted) {
                case 1:
                    this.isAutoPotion = !this.isAutoPotion;
                    break;
                case 2:
                    this.isAutoItem = !this.isAutoItem;
                    break;
                case 3:
                    this.isAutoGemItem = !this.isAutoGemItem;
                    break;
                case 4:
                    this.isNhatHet = !this.isNhatHet;
                    if (this.isNhatHet) {
                        this.isAutoPotion = true;
                        this.isAutoItem = true;
                        this.isAutoGemItem = true;
                    } else {
                        this.isAutoPotion = false;
                        this.isAutoItem = false;
                        this.isAutoGemItem = false;
                    }
                    break;
            }
            if (this.isAutoPotion && this.isAutoItem && this.isAutoGemItem) {
                this.isNhatHet = true;
            } else {
                this.isNhatHet = false;
            }
        }
    }

    private void setLim() {
        cmxLim = (Canvas.gameScr.mainChar.getnSkill() * 23) - 114;
        if (cmxLim < 0) {
            cmxLim = 0;
        }
    }

    @Override
    public void update() {
        Canvas.gameScr.update();
        if (!this.isOptionFocus) {
            if (cmx != cmtoX) {
                cmvx = (cmtoX - cmx) << 2;
                cmdx += cmvx;
                cmx += cmdx >> 4;
                cmdx &= 15;
            }
            if (Math.abs(cmtoX - cmx) < 15 && cmx < 0) {
                cmtoX = 0;
            }
            if (Math.abs(cmtoX - cmx) < 10 && cmx > cmxLim) {
                cmtoX = cmxLim;
            }
            this.tfHp.update();
            this.tfMp.update();
        }
        super.update();
    }

    @Override
    public void updateKey() {
        if (this.isOptionFocus && game.pokemon.ManCu.bat() && (Canvas.isPointerClick || Canvas.isPointerDown)) {
            game.pokemon.ManCu.phimKhongCham(new Runnable() {
                public void run() {
                    BangAuto.this.updateKey();
                }
            });
            return;
        }
        if (this.isOptionFocus) {
            boolean fc4 = false;
            boolean fc6 = false;
            if (Canvas.isPointer((Canvas.w / 2) - 75, (Canvas.h / 2) - 80, 75, 30)) {
                fc4 = true;
            } else if (Canvas.isPointer(Canvas.w / 2, (Canvas.h / 2) - 80, 75, 30)) {
                fc6 = true;
            }
            if (Canvas.isPointerClick) {
                if (fc4) {
                    Canvas.keyPressed[4] = true;
                    Canvas.isPointerClick = false;
                } else if (fc6) {
                    Canvas.keyPressed[6] = true;
                    Canvas.isPointerClick = false;
                }
            }
            if (Canvas.keyPressed[4]) {
                Canvas.keyPressed[4] = false;
                GameScr.typeOptionFocus = (byte) (GameScr.typeOptionFocus - 1);
                if (GameScr.typeOptionFocus < 0) {
                    GameScr.typeOptionFocus = (byte) (this.optionFC.length - 1);
                }
                setInfo();
                this.countL = 5;
            } else if (Canvas.keyPressed[6]) {
                Canvas.keyPressed[6] = false;
                GameScr.typeOptionFocus = (byte) (GameScr.typeOptionFocus + 1);
                if (GameScr.typeOptionFocus > this.optionFC.length - 1) {
                    GameScr.typeOptionFocus = (byte) 0;
                }
                setInfo();
                this.countR = 5;
            }
        } else {
            switch (this.index) {
                case 0:
                    name = "Trị";
                    if (Canvas.keyPressed[2]) {
                        this.seleted--;
                        if (this.seleted == 0) {
                            this.isChangeTab = true;
                            this.tfHp.isFocus = false;
                            this.tfMp.isFocus = false;
                            this.center.caption = "Chọn";
                            this.center = this.cmdBatTat;
                            this.right = null;
                        }
                        if (this.seleted < 0) {
                            this.seleted = 0;
                        }
                    } else if (Canvas.keyPressed[8]) {
                        this.isChangeTab = false;
                        this.seleted++;
                        if (this.seleted > 4) {
                            this.seleted = 4;
                        }
                    }
                    if (this.seleted == 1) {
                        this.tfHp.isFocus = true;
                        this.tfMp.isFocus = false;
                        this.right = this.tfHp.cmdClear;
                    } else if (this.seleted == 2) {
                        this.tfHp.isFocus = false;
                        this.tfMp.isFocus = true;
                        this.right = this.tfMp.cmdClear;
                        this.center.caption = "Chọn";
                        this.center = this.cmdBatTat;
                    } else if (this.seleted == 3) {
                        this.tfHp.isFocus = false;
                        this.tfMp.isFocus = false;
                        this.center.caption = "Chọn";
                        this.center = this.cmdCheck;
                    } else if (this.seleted == 4) {
                        this.tfHp.isFocus = false;
                        this.tfMp.isFocus = false;
                        this.center.caption = "Chọn";
                        this.center = this.cmdComeHome;
                    }
                    break;
                case 1:
                    name = "Đánh";
                    setLim();
                    if (Canvas.keyPressed[2]) {
                        this.seleted--;
                        if (this.seleted < 0) {
                            this.seleted = 0;
                        }
                        if (this.seleted == 0) {
                            this.isChangeTab = true;
                            this.center = this.cmdBatTat;
                            this.center.caption = "Chọn";
                            this.center = this.cmdBatTat;
                        }
                    } else if (Canvas.keyPressed[8]) {
                        this.seleted++;
                        this.isChangeTab = false;
                        if (this.seleted > 1) {
                            this.seleted = 1;
                        }
                        if (this.seleted == 1) {
                            this.center = this.cmdSelectSkill;
                            this.center.caption = "";
                        }
                    }
                    if (this.seleted == 1) {
                        if (Canvas.keyPressed[4]) {
                            this.focus--;
                            if (this.focus < 0) {
                                this.focus = 0;
                            }
                        } else if (Canvas.keyPressed[6]) {
                            this.focus++;
                            if (this.focus > Canvas.gameScr.mainChar.getnSkill() - 1) {
                                this.focus = Canvas.gameScr.mainChar.getnSkill() - 1;
                            }
                        } else if (Canvas.keyPressed[5]) {
                            this.center.action.perform();
                        }
                    }
                    break;
                case 2:
                    name = "Nhặt";
                    if (Canvas.keyPressed[2]) {
                        this.seleted--;
                        if (this.seleted == 0) {
                            this.isChangeTab = true;
                            if (GameScr.autoFight || GameScr.autoBomHMP) {
                                this.center.caption = "Tắt";
                            } else {
                                this.center.caption = "Bật";
                            }
                            this.center = this.cmdBatTat;
                        }
                        if (this.seleted < 0) {
                            this.seleted = 0;
                        }
                    } else if (Canvas.keyPressed[8]) {
                        this.isChangeTab = false;
                        this.seleted++;
                        if (this.seleted == 1) {
                            this.center = this.cmdCheck;
                            this.center.caption = "Chọn";
                        }
                        if (this.seleted > 4) {
                            this.seleted = 4;
                        }
                    }
                    break;
            }
            if (this.seleted == 0) {
                if (Canvas.keyPressed[4]) {
                    this.index--;
                    this.countL = 5;
                    if (this.index < 0) {
                        this.index = 1;
                    }
                } else if (Canvas.keyPressed[6]) {
                    this.index++;
                    this.countR = 5;
                    if (this.index > 1) {
                        this.index = 0;
                    }
                }
            }
            if (this.index == 1 && this.seleted == 1) {
                cmtoX = ((this.focus % Canvas.gameScr.mainChar.getnSkill()) * 23) - 57;
                if (cmtoX < 0) {
                    cmtoX = 0;
                }
                if (cmtoX > cmxLim) {
                    cmtoX = cmxLim;
                }
            } else {
                cmtoX = 0;
                cmx = 0;
                this.focus = 0;
            }
        }
        super.updateKey();
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (this.tfHp.isFocus) {
            boolean swallow = this.tfHp.keyPressed(keyCode);
            if (swallow) {
                return true;
            }
        } else if (this.tfMp.isFocus) {
            boolean swallow2 = this.tfMp.keyPressed(keyCode);
            if (swallow2) {
                return true;
            }
        }
        return super.keyPress(keyCode);
    }

    public void setInfo() {
        this.info = Font.normalFont[0].splitFontBStrInLine(this.optionDetail[GameScr.typeOptionFocus], 135);
        this.center = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                Canvas.gameScr.switchToMe();
            }
        });
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GameScr.typeOptionFocus = BangAuto.this.currentIndex;
                Canvas.gameScr.switchToMe();
            }
        });
        this.left = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        Canvas.gameScr.paint(g);
        if (this.countL > 0) {
            this.countL--;
        }
        if (this.countR > 0) {
            this.countR--;
        }
        if (this.isOptionFocus && game.pokemon.ManCu.bat()) {
            // 31/10/2026: tấm chung - mỗi chế độ một hàng (tên + mô tả), chạm = chọn, nút Chọn áp dụng, X = huỷ
            this.anNutLenh = true;
            int[] c = game.pokemon.ManCu.tam(g, this, "Chế độ chọn mục tiêu", game.pokemon.ManCu.viec(this.right), new Command[]{this.center});
            game.pokemon.ManCu.danhSach(g, c, this.optionFC.length, GameScr.typeOptionFocus, new game.pokemon.ManCu.Dong() {
                public String chinh(int i) {
                    return BangAuto.this.optionFC[i].trim() + (i == GameScr.typeOptionFocus ? " (đang chọn)" : "");
                }

                public String phu(int i) {
                    return BangAuto.this.optionDetail[i].trim();
                }

                public void cham(int i) {
                    GameScr.typeOptionFocus = (byte) i;
                    BangAuto.this.setInfo();
                }
            }, null);
            return;
        }
        if (this.isOptionFocus) {
            Res.paintDlgFull(g, (Canvas.w / 2) - 80, (Canvas.h / 2) - 80, 160, 160);
            game.pokemon.GiaoDien.muiTab(g, ((Canvas.w / 2) - 72) - this.countL, (Canvas.h / 2) - 66, true);
            game.pokemon.GiaoDien.muiTab(g, (Canvas.w / 2) + 72 + this.countR, (Canvas.h / 2) - 66, false);
            Font.normalFont[0].drawString(g, this.optionFC[GameScr.typeOptionFocus], Canvas.w / 2, (Canvas.h / 2) - 73, 2);
            for (int i = 0; i < this.info.length; i++) {
                Font.normalFont[0].drawString(g, this.info[i], (Canvas.w / 2) - 72, ((Canvas.h / 2) - 50) + (i * Screen.ITEM_HEIGHT), 0);
            }
            super.paint(g);
            return;
        }
        g.translate(Canvas.hw - 70, Canvas.hh - 80);
        Res.paintDlgFull(g, 2, 2, 140, 145);
        // pokemon: viên tên tab + mũi tên vàng (thay thanh xám + mũi tên ảnh kiếm hiệp)
        game.pokemon.GiaoDien.muiTab(g, 9 - this.countL, 16, true);
        game.pokemon.GiaoDien.muiTab(g, 133 + this.countR, 16, false);
        game.pokemon.GiaoDien.vienTab(g, 25, 7, 92, 18, this.isChangeTab);
        Font.normalFont[0].drawString(g, name, 72, 9, 2);
        Canvas.resetTrans(g);
        switch (this.index) {
            case 0:
                Font.normalFont[0].drawString(g, "HP: ", Canvas.hw - 60, Canvas.hh - 45, 0);
                Font.normalFont[0].drawString(g, "PP: ", Canvas.hw - 60, Canvas.hh - 20, 0);
                Font.normalFont[0].drawString(g, "%", Canvas.hw + 45, Canvas.hh - 45, 1);
                Font.normalFont[0].drawString(g, "%", Canvas.hw + 45, Canvas.hh - 20, 1);
                g.setColor(16777215);
                g.fillRect(Canvas.hw - 65, Canvas.hh + 5, 135, 1);
                Font.normalFont[0].drawString(g, "Tự vào nhóm:", Canvas.hw - 60, Canvas.hh + 15, 0);
                Res.imgCheck.drawFrame(this.seleted == 3 ? 1 : 0, Canvas.hw + 30, Canvas.hh + 23, 0, 3, g);
                Font.normalFont[0].drawString(g, "Tự về làng:", Canvas.hw - 60, Canvas.hh + 35, 0);
                Res.imgCheck.drawFrame(this.seleted == 4 ? 1 : 0, Canvas.hw + 30, Canvas.hh + 43, 0, 3, g);
                if (this.isAutoParty) {
                    Res.imgCheck.drawFrame(2, Canvas.hw + 30, Canvas.hh + 23, 0, 3, g);
                }
                if (isAutoComeHome) {
                    Res.imgCheck.drawFrame(2, Canvas.hw + 30, Canvas.hh + 43, 0, 3, g);
                }
                this.tfHp.paint(g);
                Canvas.resetTrans(g);
                this.tfMp.paint(g);
                break;
            case 1:
                Font.normalFont[0].drawString(g, "Đánh:", Canvas.hw - 15, Canvas.hh - 50, 0);
                g.setColor(16777215);
                g.drawRect(Canvas.hw - 50, Canvas.hh - 35, 17, 17);
                g.drawRect(Canvas.hw - 8, Canvas.hh - 35, 17, 17);
                g.drawRect(Canvas.hw + 35, Canvas.hh - 35, 17, 17);
                Font.normalFont[0].drawString(g, "Hổ trợ:", Canvas.hw - 15, Canvas.hh - 8, 0);
                g.setColor(16777215);
                g.drawRect(Canvas.hw - 50, Canvas.hh + 7, 17, 17);
                g.drawRect(Canvas.hw - 8, Canvas.hh + 7, 17, 17);
                g.drawRect(Canvas.hw + 35, Canvas.hh + 7, 17, 17);
                g.fillRect(Canvas.hw - 65, Canvas.hh + 33, 135, 1);
                paintSkillAuto(g);
                break;
            case 2:
                Font.normalFont[0].drawString(g, "HP + PP: ", Canvas.hw - 60, Canvas.hh - 45, 0);
                Res.imgCheck.drawFrame(this.seleted == 1 ? 1 : 0, Canvas.hw + 30, Canvas.hh - 38, 0, 3, g);
                if (this.isAutoPotion) {
                    Res.imgCheck.drawFrame(2, Canvas.hw + 30, Canvas.hh - 38, 0, 3, g);
                }
                Font.normalFont[0].drawString(g, "Trang bị: ", Canvas.hw - 60, Canvas.hh - 25, 0);
                Res.imgCheck.drawFrame(this.seleted == 2 ? 1 : 0, Canvas.hw + 30, Canvas.hh - 18, 0, 3, g);
                if (this.isAutoItem) {
                    Res.imgCheck.drawFrame(2, Canvas.hw + 30, Canvas.hh - 18, 0, 3, g);
                }
                Font.normalFont[0].drawString(g, "Nguyên liệu: ", Canvas.hw - 60, Canvas.hh - 5, 0);
                Res.imgCheck.drawFrame(this.seleted == 3 ? 1 : 0, Canvas.hw + 30, Canvas.hh + 2, 0, 3, g);
                if (this.isAutoGemItem) {
                    Res.imgCheck.drawFrame(2, Canvas.hw + 30, Canvas.hh + 2, 0, 3, g);
                }
                Font.normalFont[0].drawString(g, "Nhặt hết: ", Canvas.hw - 60, Canvas.hh + 15, 0);
                Res.imgCheck.drawFrame(this.seleted == 4 ? 1 : 0, Canvas.hw + 30, Canvas.hh + 22, 0, 3, g);
                if (this.isNhatHet) {
                    Res.imgCheck.drawFrame(2, Canvas.hw + 30, Canvas.hh + 22, 0, 3, g);
                }
                break;
        }
        super.paint(g);
    }

    private void paintSkillAuto(Graphics g) {
        g.setClip(Canvas.hw - 60, Canvas.hh + 35, 125, 26);
        g.translate(-cmx, 0);
        for (int skillType = 0; skillType < Canvas.gameScr.mainChar.getnSkill(); skillType++) {
            game.pokemon.BangChieu.icon(g, ((skillType * 23) + 1 + Canvas.hw) - 60, Canvas.hh + 38, 20, skillType);
            Font.smallFontColor[1].drawString(g, new StringBuffer(String.valueOf((int) Char.skillLevelLearnt[skillType])).toString(), ((((skillType * 23) + 23) - 4) + Canvas.hw) - 60, Canvas.hh + 48 + 5, 1);
        }
        if (this.seleted == 1 && Canvas.gameTick % 10 > 3) {
            g.setColor(16388369);
            g.drawRect(((this.focus * 23) + Canvas.hw) - 60, Canvas.hh + 38, 21, 21);
        }
        Canvas.resetTrans(g);
        int[] vitri = {Canvas.hw - 51, Canvas.hw - 9, Canvas.hw + 34};
        if (this.IDSkillAuto != null) {
            for (int i = 0; i < this.IDSkillAuto.length; i++) {
                if (this.IDSkillAuto[i] != -1) {
                    game.pokemon.BangChieu.icon(g, vitri[i], Canvas.hh - 36, 20, this.IDSkillAuto[i]);
                }
            }
        }
        if (this.IDSkillBuff != null) {
            for (int j = 0; j < this.IDSkillBuff.length; j++) {
                if (this.IDSkillBuff[j] != -1) {
                    game.pokemon.BangChieu.icon(g, vitri[j], Canvas.hh + 6, 20, this.IDSkillBuff[j]);
                }
            }
        }
    }

    public boolean isBuffSkillAuto() {
        byte[][] skill = {new byte[]{4, 5}, new byte[]{4, 5}, new byte[]{4, 5, 6, 7}, new byte[]{4, 5}, new byte[]{4, 5}};
        for (int i = 0; i < skill[Canvas.gameScr.mainChar.clazz].length; i++) {
            if (this.focus == skill[Canvas.gameScr.mainChar.clazz][i]) {
                return true;
            }
        }
        return false;
    }

    public void doSelectedSkillAuto() {
        if (Char.skillLevelLearnt[this.focus] == -1) {
            Canvas.startOKDlg("Xin gặp Blaziken để học kỹ năng này");
            return;
        }
        new Vector();
        boolean buff2User = false;
        boolean isBuff = isBuffSkillAuto();
        if (isBuff) {
            buff2User = SkillManager.SKILL_CAN_BUFF_TO_USER[Canvas.gameScr.mainChar.clazz][this.focus - 4] == -1;
        }
        if (!buff2User) {
            if (Char.skillLevelLearnt[this.focus] > 0) {
                if (Canvas.gameScr.mainChar.clazz != 2) {
                    doGiveSkillToQuickSlotAuto(this.focus, isBuff);
                    return;
                } else if (this.focus == 6) {
                    Canvas.startOKDlg("Không thể dùng chiêu này");
                    return;
                } else {
                    doGiveSkillToQuickSlotAuto(this.focus, isBuff);
                    return;
                }
            }
            Canvas.startOKDlg("Chưa học kỹ năng này");
        }
    }

    private void doGiveSkillToQuickSlotAuto(final int skillType, final boolean isBuff) {
        Vector menuItems = new Vector();
        for (int i = 0; i < 3; i++) {
            final int ii = i;
            menuItems.addElement(new Command(new StringBuffer("Phím số ").append(1 + (i * 2)).toString(), new IAction() {
                @Override
                public void perform() {
                    if (isBuff) {
                        GameScr.indexTabSlot = 1;
                        BangAuto.this.IDSkillBuff[ii] = skillType;
                    } else {
                        GameScr.indexTabSlot = 0;
                        BangAuto.this.IDSkillAuto[ii] = skillType;
                    }
                    MainChar.quickSlot[GameScr.indexTabSlot][ii].setIsSkill(skillType, Canvas.gameScr.mainChar.clazz, isBuff);
                    RMS.saveQuickSlot();
                    GameScr.indexTabSlot = 0;
                }
            }));
        }
        Canvas.menu.startAt(menuItems, 2);
    }
}
