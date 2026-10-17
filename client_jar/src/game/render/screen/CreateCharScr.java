package game.render.screen;

import game.model.CharClassInfo;
import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.pokemon.LoaiPokemonClient;
import game.pokemon.PokeBody;
import game.pokemon.PokeDuLieu;
import javax.microedition.lcdui.Graphics;

public class CreateCharScr extends Screen {
    public static CreateCharScr me;
    public static final CharClassInfo[][] clazz = {new CharClassInfo[]{new CharClassInfo((byte) 0, "Kiếm Khách", "Nam", "Kiếm", "Mien Bac", "Kim", 3, 0), new CharClassInfo((byte) 0, "Kiếm Khách", "Nữ", "Kiếm", "Mien Bac", "Kim", 3, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 1, "Chiến Binh", "Nam", "Đại Đao", "Mien Bac", "Hỏa", 4, 0), new CharClassInfo((byte) 1, "Chiến Binh", "Nữ", "Đại Đao", "Mien Bac", "Hỏa", 4, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 2, "Pháp Sư", "Nam", "Đũa thần", "Mien Nam", "Thủy", 5, 0), new CharClassInfo((byte) 2, "Pháp Sư", "Nữ", "Đũa thần", "Mien Nam", "Thủy", 5, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 3, "Đấu sĩ", "Nam", "Rìu", "Mien Nam", "Thổ", 6, 0), new CharClassInfo((byte) 3, "Đấu sĩ", "Nữ", "Rìu", "Mien Nam", "Thổ", 6, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 4, "Cung Thủ", "Nam", "Cung", "Mien Bac", "Mộc", 7, 0), new CharClassInfo((byte) 4, "Cung Thủ", "Nữ", "Cung", "Mien Bac", "Mộc", 7, 0)}};
    public static String[] hairName = {"Đầu đinh", "Tóc búi", "Tóc cột cao", "Tóc chéo", "Tóc xù", "Tóc ngang vai"};
    static String[] gender = {"Nam", "Nữ"};
    public static int selectGender = 0;
    public static int selectNation = 0;
    Command cmdClear;
    int currentClazz;
    int selected;
    public MainChar ch = new MainChar();
    public TField tfChat = new TField(this);

    public static CreateCharScr gI() {
        if (me != null) {
            return me;
        }
        CreateCharScr createCharScr = new CreateCharScr();
        me = createCharScr;
        return createCharScr;
    }

    @Override
    public void switchToMe() {
        loadImg();
        super.switchToMe();
    }

    @Override
    public void init() {
        this.tfChat.x = Canvas.hw - 28;
        this.tfChat.y = Canvas.hh - 24;
    }

    public CreateCharScr() {
        this.tfChat.width = 80;
        this.tfChat.height = Screen.ITEM_HEIGHT + 2;
        this.tfChat.isFocus = true;
        this.tfChat.setIputType(3);
        if (PokeBody.CHE_DO_POKEMON) {
            // pokemon: khong co gioi tinh/kieu toc - luon gui gender = 0, headStyle = 0 (muc 4.2)
            selectGender = 0;
        }
        this.ch.state = (byte) 0;
        this.currentClazz = 0;
        this.ch.hatStyle = (short) -1;
        this.ch.currentHead = clazz[this.currentClazz][selectGender].headID;
        this.ch.bodyStyle = clazz[this.currentClazz][selectGender].bodyID;
        this.ch.legStyle = clazz[this.currentClazz][selectGender].legID;
        this.ch.weaponType = clazz[this.currentClazz][selectGender].weaponType;
        this.ch.weaponStyle = clazz[this.currentClazz][selectGender].weaponImageID;
        this.left = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                CharSelectScr.gI().switchToMe();
                CreateCharScr.me = null;
            }
        });
        this.cmdClear = new Command("Xóa", new IAction() {
            @Override
            public void perform() {
                CreateCharScr.this.tfChat.clear();
            }
        });
        this.center = new Command("Tạo", new IAction() {
            @Override
            public void perform() {
                if (CreateCharScr.this.tfChat.getText().length() < 4 || CreateCharScr.this.tfChat.getText().length() > 15) {
                    Canvas.startOKDlg("Tên nhân vật phải từ 4 đến 15 ký tự");
                } else {
                    if (PokeBody.CHE_DO_POKEMON) {
                        GameService.gI().createChar(CreateCharScr.this.tfChat.getText(), CreateCharScr.this.currentClazz, 0, 0, CreateCharScr.selectNation);
                    } else {
                        GameService.gI().createChar(CreateCharScr.this.tfChat.getText(), CreateCharScr.this.currentClazz, CreateCharScr.clazz[CreateCharScr.this.currentClazz][CreateCharScr.selectGender].headID, CreateCharScr.selectGender, CreateCharScr.selectNation);
                    }
                    Canvas.startWaitDlg("Xin chờ", true);
                }
            }
        });
        init();
    }

    private void changChar() {
        this.ch.currentHead = clazz[this.currentClazz][selectGender].headID;
        this.ch.bodyStyle = clazz[this.currentClazz][selectGender].bodyID;
        this.ch.legStyle = clazz[this.currentClazz][selectGender].legID;
        this.ch.weaponType = clazz[this.currentClazz][selectGender].weaponType;
        this.ch.weaponStyle = clazz[this.currentClazz][selectGender].weaponImageID;
    }

    private void loadImg() {
        GameService.gI().doRequestWeapone(0, this.ch.weaponType, this.ch.weaponStyle, this.ch.weaponIndex);
    }

    @Override
    public boolean keyPress(int keyCode) {
        boolean isChangeClass = false;
        if (this.selected == 0) {
            this.tfChat.keyPressed(keyCode);
        }
        // pokemon: chi 3 dong - 0 ten, 1 Pokemon khoi dau (class), 2 lanh tho (dong goc 4)
        int dongCuoi = PokeBody.CHE_DO_POKEMON ? 2 : 4;
        if (keyCode == -2) {
            this.selected++;
            if (this.selected > dongCuoi) {
                this.selected = 0;
            }
            if (this.selected == 0) {
                this.tfChat.isFocus = true;
            } else {
                this.tfChat.isFocus = false;
            }
        }
        if (keyCode == -1) {
            this.selected--;
            if (this.selected < 0) {
                this.selected = dongCuoi;
            }
            if (this.selected == 0) {
                this.tfChat.isFocus = true;
            } else {
                this.tfChat.isFocus = false;
            }
        }
        int selected = this.selected;
        if (PokeBody.CHE_DO_POKEMON && selected == 2) {
            selected = 4;
        }
        if (keyCode == -3) {
            if (selected == 1) {
                this.currentClazz++;
                if (this.currentClazz >= 5) {
                    this.currentClazz = 0;
                }
                isChangeClass = true;
            }
            if (selected == 2) {
                selectGender = selectGender == 1 ? 0 : 1;
                changChar();
            }
            if (selected == 3) {
                CharClassInfo charClassInfo = clazz[this.currentClazz][selectGender];
                charClassInfo.headID = (short) (charClassInfo.headID - 2);
                if (clazz[this.currentClazz][selectGender].headID < 0) {
                    clazz[this.currentClazz][selectGender].headID = (byte) (4 + clazz[this.currentClazz][selectGender].headID);
                }
                isChangeClass = true;
            }
            if (selected == 4) {
                selectNation--;
                if (selectNation < 0) {
                    selectNation = 1;
                }
            }
        }
        if (keyCode == -4) {
            if (selected == 1) {
                this.currentClazz--;
                if (this.currentClazz < 0) {
                    this.currentClazz = 4;
                }
                isChangeClass = true;
            }
            if (selected == 2) {
                selectGender = selectGender == 0 ? 1 : 0;
                changChar();
            }
            if (selected == 3) {
                CharClassInfo charClassInfo2 = clazz[this.currentClazz][selectGender];
                charClassInfo2.headID = (short) (charClassInfo2.headID + 2);
                if (clazz[this.currentClazz][selectGender].headID > 5) {
                    clazz[this.currentClazz][selectGender].headID = (byte) (clazz[this.currentClazz][selectGender].headID % 2);
                }
                isChangeClass = true;
            }
            if (selected == 4) {
                selectNation++;
                if (selectNation > 1) {
                    selectNation = 0;
                }
            }
        }
        if (isChangeClass) {
            this.ch.bodyStyle = clazz[this.currentClazz][selectGender].bodyID;
            this.ch.legStyle = clazz[this.currentClazz][selectGender].legID;
            this.ch.currentHead = clazz[this.currentClazz][selectGender].headID;
            this.ch.weaponType = clazz[this.currentClazz][selectGender].weaponType;
            this.ch.weaponStyle = clazz[this.currentClazz][selectGender].weaponImageID;
            loadImg();
        }
        return super.keyPress(keyCode);
    }

    @Override
    public void paint(Graphics g) {
        ServerListScr.gI().paintBg(g);
        Canvas.resetTrans(g);
        int x = Canvas.hw - 53;
        int y = Canvas.hh - 20;
        Res.paintDlgFull(g, Canvas.hw - 75, Canvas.hh - 60);
        if (Canvas.gameTick % 20 > 4) {
            Font.normalFont[0].drawString(g, "*", Canvas.hw - 60, (y + (this.selected * 17)) - 1, 0);
            Font.normalFont[0].drawString(g, "*", Canvas.hw + 57, (y + (this.selected * 17)) - 1, 0);
        }
        Font.normalFont[0].drawString(g, "Tên:", x, y, 0);
        if (PokeBody.CHE_DO_POKEMON) {
            paintPokemon(g, x, y);
            this.tfChat.paint(g);
            g.setClip(0, 0, Canvas.w, Canvas.h);
            super.paint(g);
            return;
        }
        int y2 = y + 17;
        Font.normalFont[0].drawString(g, new StringBuffer("Dòng: ").append(clazz[this.currentClazz][selectGender].name).toString(), x, y2, 0);
        int y3 = y2 + 17;
        Font.normalFont[0].drawString(g, new StringBuffer("Giới tính: ").append(gender[selectGender]).toString(), x, y3, 0);
        int y4 = y3 + 17;
        Font.normalFont[0].drawString(g, new StringBuffer("Tóc: ").append(hairName[clazz[this.currentClazz][selectGender].headID]).toString(), x, y4, 0);
        int y5 = y4 + 17;
        Font.normalFont[0].drawString(g, new StringBuffer("L.thổ: ").append(GameScr.nameCountry[selectNation]).toString(), x, y5, 0);
        Font.normalFont[0].drawString(g, new StringBuffer("Ngũ hành: ").append(clazz[this.currentClazz][selectGender].element).toString(), x, y5 + 17, 0);
        this.ch.paintAvatar(g, (short) Canvas.hw, (short) (Canvas.hh - 30));
        if (this.ch.weaponType != -1 && this.ch.weaponStyle != -1 && this.ch.imgWpa != null) {
            int dy = this.ch.frame == 1 ? 1 : 0;
            g.drawImage(this.ch.imgWpa, Canvas.hw + this.ch.dxWear, (Canvas.hh - 30) + this.ch.dyWear + dy, 0);
        }
        this.tfChat.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        super.paint(g);
    }

    /** pokemon: man tao nhan vat - 5 Pokemon khoi dau thay 5 class (bang 4.1, ClientUnity CreateCharScr.cs). */
    private void paintPokemon(Graphics g, int x, int y) {
        int y2 = y + 17;
        Font.normalFont[0].drawString(g, new StringBuffer("Pokémon: ").append(LoaiPokemonClient.TEN_KHOI_DAU[this.currentClazz]).toString(), x, y2, 0);
        int y3 = y2 + 17;
        Font.normalFont[0].drawString(g, new StringBuffer("L.thổ: ").append(GameScr.nameCountry[selectNation]).toString(), x, y3, 0);
        int y4 = y3 + 17;
        Font.normalFont[0].drawString(g, LoaiPokemonClient.HE_KHOI_DAU[this.currentClazz], x, y4, 0);
        Font.normalFont[0].drawString(g, LoaiPokemonClient.VAI_TRO[this.currentClazz], x, y4 + 17, 0);
        // chan dung + Pokemon dang dung (Idle); thieu sprite thi ve paper doll cu
        int dex = LoaiPokemonClient.khoiDau(this.currentClazz);
        PokeBody.paintChanDung(g, dex, false, Canvas.hw - 40, Canvas.hh - 68, 32);
        if (!PokeBody.paint(g, dex, false, Canvas.hw + 25, Canvas.hh - 50, 0, PokeDuLieu.DUNG, Canvas.gameTick, 100)) {
            this.ch.paintAvatar(g, (short) Canvas.hw, (short) (Canvas.hh - 30));
        }
    }

    @Override
    public void update() {
        this.tfChat.update();
        this.ch.update();
        super.update();
    }
}
