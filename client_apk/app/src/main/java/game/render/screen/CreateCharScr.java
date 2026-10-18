package game.render.screen;

import game.core.j2me.Graphics;
import game.model.CharClassInfo;
import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;

public class CreateCharScr extends Screen {
    public static CreateCharScr me;
    public MainChar ch = new MainChar();
    Command cmdClear;
    int currentClazz;
    int selected;
    public TField tfChat;
    public static final CharClassInfo[][] clazz = {new CharClassInfo[]{new CharClassInfo((byte) 0, "Dòng Charmander", "Nam", "Than lửa", "Mien Bac", "Lửa", 3, 0), new CharClassInfo((byte) 0, "Dòng Charmander", "Nữ", "Than lửa", "Mien Bac", "Lửa", 3, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 1, "Dòng Bulbasaur", "Nam", "Hạt giống thần", "Mien Bac", "Cỏ", 4, 0), new CharClassInfo((byte) 1, "Dòng Bulbasaur", "Nữ", "Hạt giống thần", "Mien Bac", "Cỏ", 4, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 2, "Dòng Eevee", "Nam", "Khăn lụa", "Mien Nam", "Thường", 5, 0), new CharClassInfo((byte) 2, "Dòng Eevee", "Nữ", "Khăn lụa", "Mien Nam", "Thường", 5, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 3, "Dòng Squirtle", "Nam", "Nước thần", "Mien Nam", "Nước", 6, 0), new CharClassInfo((byte) 3, "Dòng Squirtle", "Nữ", "Nước thần", "Mien Nam", "Nước", 6, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 4, "Dòng Pichu", "Nam", "Nam châm", "Mien Bac", "Điện", 7, 0), new CharClassInfo((byte) 4, "Dòng Pichu", "Nữ", "Nam châm", "Mien Bac", "Điện", 7, 0)}};
    public static String[] hairName = {"Đầu đinh", "Lông búi", "Lông cột cao", "Lông chéo", "Lông xù", "Lông ngang vai"};
    static String[] gender = {"Nam", "Nữ"};
    public static int selectGender = 0;
    public static int selectNation = 0;

    public static CreateCharScr gI() {
        CreateCharScr createCharScr = me;
        if (createCharScr != null) {
            return createCharScr;
        }
        CreateCharScr createCharScr2 = new CreateCharScr();
        me = createCharScr2;
        return createCharScr2;
    }

    @Override
    public void switchToMe() {
        loadImg();
        super.switchToMe();
    }

    @Override
    public void init() {
        this.tfChat.x = GCanvas.hw - 125;
        this.tfChat.y = GCanvas.hh + 25;
    }

    public CreateCharScr() {
        TField tField = new TField();
        this.tfChat = tField;
        tField.width = 115;
        this.tfChat.height = 21;
        this.tfChat.isFocus = true;
        this.tfChat.setIputType(3);
        this.ch.state = (byte) 0;
        this.currentClazz = 0;
        this.ch.hatStyle = (short) -1;
        MainChar mainChar = this.ch;
        CharClassInfo[][] charClassInfoArr = clazz;
        mainChar.currentHead = charClassInfoArr[this.currentClazz][selectGender].headID;
        this.ch.bodyStyle = charClassInfoArr[this.currentClazz][selectGender].bodyID;
        this.ch.legStyle = charClassInfoArr[this.currentClazz][selectGender].legID;
        this.ch.weaponType = charClassInfoArr[this.currentClazz][selectGender].weaponType;
        this.ch.weaponStyle = charClassInfoArr[this.currentClazz][selectGender].weaponImageID;
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                CharSelectScr.gI().switchToMe();
                CreateCharScr.me = null;
            }
        });
        this.left = new Command("Tạo", new IAction() {
            @Override
            public void perform() {
                if (CreateCharScr.this.tfChat.getText().trim().length() < 4 || CreateCharScr.this.tfChat.getText().trim().length() > 15) {
                    GCanvas.startOKDlg("Tên Pokémon phải từ 4 đến 15 ký tự");
                } else {
                    GameService.gI().createChar(CreateCharScr.this.tfChat.getText(), CreateCharScr.this.currentClazz, CreateCharScr.clazz[CreateCharScr.this.currentClazz][CreateCharScr.selectGender].headID, CreateCharScr.selectGender, CreateCharScr.selectNation);
                    GCanvas.startWaitDlg("Xin chờ", true);
                }
            }
        });
        init();
    }

    private void changChar() {
        MainChar mainChar = this.ch;
        CharClassInfo[][] charClassInfoArr = clazz;
        mainChar.currentHead = charClassInfoArr[this.currentClazz][selectGender].headID;
        this.ch.bodyStyle = charClassInfoArr[this.currentClazz][selectGender].bodyID;
        this.ch.legStyle = charClassInfoArr[this.currentClazz][selectGender].legID;
        this.ch.weaponType = charClassInfoArr[this.currentClazz][selectGender].weaponType;
        this.ch.weaponStyle = charClassInfoArr[this.currentClazz][selectGender].weaponImageID;
    }

    private void loadImg() {
        GameService.gI().doRequestWeapone(0, this.ch.weaponType, this.ch.weaponStyle, this.ch.weaponIndex);
    }

    @Override
    public boolean keyPress(int i) {
        boolean z;
        if (this.selected == 0) {
            this.tfChat.keyPressed(i);
        }
        boolean z2 = true;
        if (i == -2) {
            int i2 = this.selected + 1;
            this.selected = i2;
            if (i2 >= 4) {
                this.selected = 0;
            }
            if (this.selected == 0) {
                this.tfChat.isFocus = true;
            } else {
                this.tfChat.isFocus = false;
            }
        }
        if (i == -1) {
            int i3 = this.selected - 1;
            this.selected = i3;
            if (i3 < 0) {
                this.selected = 3;
            }
            if (this.selected == 0) {
                this.tfChat.isFocus = true;
            } else {
                this.tfChat.isFocus = false;
            }
        }
        if (i == -3) {
            int i4 = this.selected;
            if (i4 == 1) {
                int i5 = this.currentClazz + 1;
                this.currentClazz = i5;
                if (i5 >= 5) {
                    this.currentClazz = 0;
                }
                z = true;
            } else {
                z = false;
            }
            if (i4 == 2) {
                selectGender = selectGender == 1 ? 0 : 1;
                changChar();
            }
            if (this.selected == 3) {
                CharClassInfo[][] charClassInfoArr = clazz;
                CharClassInfo charClassInfo = charClassInfoArr[this.currentClazz][selectGender];
                charClassInfo.headID = (short) (charClassInfo.headID - 2);
                if (charClassInfoArr[this.currentClazz][selectGender].headID < 0) {
                    CharClassInfo charClassInfo2 = charClassInfoArr[this.currentClazz][selectGender];
                    charClassInfo2.headID = (byte) (charClassInfo2.headID + 4);
                }
                z = true;
            }
        } else {
            z = false;
        }
        if (i != -4) {
            z2 = z;
        } else {
            int i6 = this.selected;
            if (i6 == 1) {
                int i7 = this.currentClazz - 1;
                this.currentClazz = i7;
                if (i7 < 0) {
                    this.currentClazz = 4;
                }
                z = true;
            }
            if (i6 == 2) {
                selectGender = selectGender == 0 ? 1 : 0;
                changChar();
            }
            if (this.selected == 3) {
                CharClassInfo[][] charClassInfoArr2 = clazz;
                CharClassInfo charClassInfo3 = charClassInfoArr2[this.currentClazz][selectGender];
                charClassInfo3.headID = (short) (charClassInfo3.headID + 2);
                if (charClassInfoArr2[this.currentClazz][selectGender].headID > 5) {
                    CharClassInfo charClassInfo4 = charClassInfoArr2[this.currentClazz][selectGender];
                    charClassInfo4.headID = (byte) (charClassInfo4.headID % 2);
                }
            } else {
                z2 = z;
            }
        }
        if (z2) {
            MainChar mainChar = this.ch;
            CharClassInfo[][] charClassInfoArr3 = clazz;
            mainChar.bodyStyle = charClassInfoArr3[this.currentClazz][selectGender].bodyID;
            this.ch.legStyle = charClassInfoArr3[this.currentClazz][selectGender].legID;
            this.ch.currentHead = charClassInfoArr3[this.currentClazz][selectGender].headID;
            this.ch.weaponType = charClassInfoArr3[this.currentClazz][selectGender].weaponType;
            this.ch.weaponStyle = charClassInfoArr3[this.currentClazz][selectGender].weaponImageID;
            loadImg();
        }
        return super.keyPress(i);
    }

    @Override
    public void paint(Graphics graphics) {
        ServerListScr.gI().paintBg(graphics);
        GCanvas.resetTrans(graphics);
        int i = GCanvas.hw + 20;
        int i2 = GCanvas.hh;
        int i3 = i2 - 60;
        Res.paintDlgFullCreateChar(graphics, GCanvas.hw - 140, GCanvas.hh - 90);
        if (GCanvas.gameTick % 10 > 4) {
            graphics.drawImage(Res.imgBar, GCanvas.hw + 5, (this.selected * 22) + i3 + 5, 0);
            graphics.drawRegion(Res.imgBar, 0, 0, 11, 7, 2, GCanvas.hw + 120, (this.selected * 22) + i3 + 5, 0);
        }
        Font.normalFont[0].drawString(graphics, "Tên:", GCanvas.hw - 125, GCanvas.hh + 10, 0);
        Font font = Font.normalFont[0];
        StringBuilder sb = new StringBuilder("Dòng: ");
        CharClassInfo[][] charClassInfoArr = clazz;
        font.drawString(graphics, sb.append(charClassInfoArr[this.currentClazz][selectGender].name).toString(), i, i3, 0);
        Font.normalFont[0].drawString(graphics, "Giới tính: " + gender[selectGender], i, i2 - 38, 0);
        Font.normalFont[0].drawString(graphics, "Lông: " + hairName[charClassInfoArr[this.currentClazz][selectGender].headID], i, i2 - 16, 0);
        Font font2 = Font.normalFont[0];
        StringBuilder sb2 = new StringBuilder("L.thổ: ");
        GameScr gameScr = GCanvas.gameScr;
        font2.drawString(graphics, sb2.append(GameScr.nameCountry[selectNation]).toString(), i, i2 + 6, 0);
        Font.normalFont[0].drawString(graphics, "Hệ: " + charClassInfoArr[this.currentClazz][selectGender].element, i, i2 + 28, 0);
        this.ch.paintAvatar(graphics, (short) (GCanvas.hw - 65), (short) (GCanvas.hh + 10));
        if (this.ch.weaponType != -1 && this.ch.weaponStyle != -1 && this.ch.imgWpa != null) {
            graphics.drawImage(this.ch.imgWpa, (GCanvas.hw - 65) + this.ch.dxWear, GCanvas.hh + 10 + this.ch.dyWear + (this.ch.frame != 1 ? 0 : 1), 0);
        }
        this.tfChat.paint(graphics);
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        super.paint(graphics);
    }

    @Override
    public void update() {
        if (GCanvas.isPointerClick) {
            pointer(GCanvas.px, GCanvas.py);
        }
        this.tfChat.update();
        this.ch.update();
        super.update();
    }

    public void pointer(int i, int i2) {
        boolean z;
        if (GCanvas.isPointer(GCanvas.hw, GCanvas.hh - 70, GCanvas.hw, GCanvas.hh)) {
            int i3 = (i2 - (GCanvas.hh - 60)) / 22;
            this.selected = i3;
            if (i3 < 0) {
                this.selected = 0;
            } else if (i3 > 3) {
                this.selected = 3;
            }
            int i4 = this.selected;
            if (i4 == 0) {
                int i5 = this.currentClazz - 1;
                this.currentClazz = i5;
                if (i5 < 0) {
                    this.currentClazz = 4;
                }
                z = true;
            } else {
                z = false;
            }
            if (i4 == 1) {
                selectGender = selectGender == 0 ? 1 : 0;
                changChar();
            }
            if (this.selected == 2) {
                CharClassInfo[][] charClassInfoArr = clazz;
                CharClassInfo charClassInfo = charClassInfoArr[this.currentClazz][selectGender];
                charClassInfo.headID = (short) (charClassInfo.headID + 2);
                if (charClassInfoArr[this.currentClazz][selectGender].headID > 5) {
                    CharClassInfo charClassInfo2 = charClassInfoArr[this.currentClazz][selectGender];
                    charClassInfo2.headID = (byte) (charClassInfo2.headID % 2);
                }
                z = true;
            }
            if (this.selected == 3) {
                int i6 = selectNation + 1;
                selectNation = i6;
                if (i6 > 1) {
                    selectNation = 0;
                }
            }
            if (z) {
                MainChar mainChar = this.ch;
                CharClassInfo[][] charClassInfoArr2 = clazz;
                mainChar.bodyStyle = charClassInfoArr2[this.currentClazz][selectGender].bodyID;
                this.ch.legStyle = charClassInfoArr2[this.currentClazz][selectGender].legID;
                this.ch.currentHead = charClassInfoArr2[this.currentClazz][selectGender].headID;
                this.ch.weaponType = charClassInfoArr2[this.currentClazz][selectGender].weaponType;
                this.ch.weaponStyle = charClassInfoArr2[this.currentClazz][selectGender].weaponImageID;
                loadImg();
            }
        }
    }
}
