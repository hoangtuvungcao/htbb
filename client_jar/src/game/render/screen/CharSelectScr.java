package game.render.screen;

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
import javax.microedition.lcdui.Image;
import network.Session_ME;

public class CharSelectScr extends Screen {
    public static CharSelectScr me;
    public MainChar[] charInfos;
    MainChar[] chars;
    public int selected = 0;
    public Command cmdSelect;
    public Command cmdNew;
    public Command cmdDel;
    public Command cmdClose;
    public static Image imgBg;

    public static CharSelectScr gI() {
        if (me != null) {
            return me;
        }
        CharSelectScr charSelectScr = new CharSelectScr();
        me = charSelectScr;
        return charSelectScr;
    }

    public void setCharList(MainChar[] charInfos) {
        if (!Session_ME.gI().connected) {
            return;
        }
        Canvas.loginScr.savePass();
        if (!Canvas.gameScr.isNewVersionAvailable) {
            Canvas.endDlg();
        }
        this.charInfos = charInfos;
        this.chars = new MainChar[charInfos.length];
        this.chars = charInfos;
        this.cmdSelect = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                Canvas.startWaitDlg("Xin chờ..", true);
                GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 1);
                Canvas.gameScr.mainChar.imgWpa = CharSelectScr.this.chars[CharSelectScr.this.selected].imgWpa;
                Canvas.gameScr.mainChar.dxWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dxWear;
                Canvas.gameScr.mainChar.dyWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dyWear;
                ChangScr.gI().switchToMe();
                CharSelectScr.this.chars = null;
                CharSelectScr.me = null;
            }
        });
        this.cmdNew = new Command("Tạo mới", new IAction() {
            @Override
            public void perform() {
                CreateCharScr.gI().switchToMe();
            }
        });
        this.cmdDel = new Command("Xóa", new AnonymousClass3());
        this.cmdClose = new Command("Thoát", new IAction() {
            @Override
            public void perform() {
                Session_ME.gI().close();
                Canvas.loginScr.switchToMe();
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
        if (charInfos.length > 0) {
            if (charInfos[this.selected].using == 1) {
                this.cmdDel.caption = "Xóa";
            } else {
                this.cmdDel.caption = "Khôi phục";
            }
        }
    }

    class AnonymousClass3 implements IAction {
        AnonymousClass3() {
        }

        @Override
        public void perform() {
            if (CharSelectScr.this.cmdDel.caption.equals("Xóa")) {
                Canvas.startYesNoDlg("Bạn thật sự muốn xóa Pokémon này?", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg("Xin chờ..", true);
                        GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 2);
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            } else if (CharSelectScr.this.cmdDel.caption.equals("Khôi phục")) {
                Canvas.startYesNoDlg("Bạn muốn khôi phục Pokémon này?", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg("Xin chờ..", true);
                        GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 3);
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            }
        }
    }

    /**
     * pokemon: du doan Pokemon cua nhan vat o man chon (lenh 13 KHONG co class va -90 chi gui sau
     * khi vao game): suy class tu loai vu khi dang mac, weaponType 3..7 = class 0..4. Khong mac vu
     * khi thi -1 (ve paper doll cu). Eevee da chon nhanh van hien Eevee.
     */
    public static int duDoanDex(MainChar c, int lastLv) {
        if (!PokeBody.CHE_DO_POKEMON || c == null || c.weaponType < 3 || c.weaponType > 7) {
            return -1;
        }
        int cap = lastLv <= 0 ? (c.lastLv <= 0 ? 1 : c.lastLv) : lastLv;
        return LoaiPokemonClient.duDoan(c.weaponType - 3, cap);
    }

    /** pokemon: bề ngang một thẻ Pokédex (3 thẻ vừa màn). */
    private static int rongThe() {
        int w = (Canvas.w - 20) / 3 - 4;
        return w > 72 ? 72 : w;
    }

    private static int xThe(int i) {
        int cw = rongThe();
        return Canvas.hw - (3 * cw + 8) / 2 + i * (cw + 4);
    }

    private static int yThe() {
        return Canvas.hh - 62;
    }

    private static final int CAO_THE = 104;

    @Override
    public void paint(Graphics g) {
        // pokemon: banner; Pokémon khởi đầu cùng dòng với nhân vật đang chọn được chiếu sáng
        int chonDong = -1;
        try {
            // lệnh 13 không gửi class: suy từ loại vũ khí như duDoanDex (weaponType 3..7 = class 0..4)
            if (this.chars != null && this.selected >= 0 && this.selected < this.chars.length && this.chars[this.selected] != null) {
                int vk = this.chars[this.selected].weaponType;
                chonDong = vk >= 3 && vk <= 7 ? vk - 3 : -1;
            }
        } catch (Throwable e) {
        }
        ServerListScr.gI().paintBg(g, chonDong, true);
        Canvas.resetTrans(g);
        // pokemon: 3 thẻ kiểu Pokédex - dải đỏ số thứ tự, Pokémon trong vòng sáng, tên, Lv, huy hiệu hệ
        int cw = rongThe();
        int xKhung = xThe(0) - 8;
        int wKhung = 3 * cw + 8 + 16;
        game.pokemon.GiaoDien.bangMo(g, xKhung, yThe() - 30, wKhung, CAO_THE + 76);
        game.pokemon.GiaoDien.dai(g, xKhung + 4, yThe() - 26, wKhung - 8, 18, "Chọn Pokémon");
        for (int i = 0; i < 3; i++) {
            int x = xThe(i);
            int y = yThe();
            boolean chon = i == this.selected;
            boolean co = this.charInfos != null && i < this.charInfos.length && this.chars != null && i < this.chars.length;
            if (chon) {
                game.pokemon.GiaoDien.khoi(g, x - 2, y - 2, cw + 4, CAO_THE + 4, 7, game.pokemon.GiaoDien.VANG);
            }
            game.pokemon.GiaoDien.khoi(g, x, y, cw, CAO_THE, 5, game.pokemon.GiaoDien.VIEN);
            if (!co) {
                game.pokemon.GiaoDien.khoi(g, x + 2, y + 2, cw - 4, CAO_THE - 4, 4, chon ? 0xDCE8F6 : 0xB9C6D8);
                int cx = x + cw / 2;
                int cy = y + 42;
                g.setColor(chon ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.XAM_TOI);
                g.fillRect(cx - 9, cy - 2, 18, 5);
                g.fillRect(cx - 2, cy - 9, 5, 18);
                Font.blackFont.drawString(g, "Tạo mới", cx, y + CAO_THE - 26, 2);
                continue;
            }
            MainChar c = this.chars[i];
            int lv = this.charInfos[i].lastLv > 0 ? this.charInfos[i].lastLv : 1;
            game.pokemon.GiaoDien.khoiDoc(g, x + 2, y + 2, cw - 4, CAO_THE - 4, 4, game.pokemon.GiaoDien.TRANG, 0xE4ECF7);
            game.pokemon.GiaoDien.khoiDoc(g, x + 2, y + 2, cw - 4, 14, 3, game.pokemon.GiaoDien.DO_SANG, game.pokemon.GiaoDien.DO_TOI);
            Font.smallFontColor[0].drawString(g, new StringBuffer("no").append(i + 1).toString(), x + 5, y + 5, 0);
            if (this.charInfos[i].using != 1) {
                Font.smallFontColor[1].drawString(g, "khoa", x + cw - 4, y + 5, 1);
            }
            // vòng sáng sau Pokémon
            int cx = x + cw / 2;
            g.setColor(chon ? 0xBFE0FF : game.pokemon.GiaoDien.XANH_NHAT);
            g.fillArc(cx - 22, y + 46, 44, 14, 0, 360);
            c.x = (short) cx;
            c.y = (short) (y + 54);
            int dexDoan = duDoanDex(c, lv);
            g.setClip(x + 2, y + 16, cw - 4, 44);
            if (dexDoan <= 0 || !PokeBody.paint(g, dexDoan, false, c.x, c.y, 0, PokeDuLieu.DUNG, Canvas.gameTick, 150)) {
                c.paintAvatar(g, c.x, c.y);
            }
            Canvas.resetTrans(g);
            String ten = this.charInfos[i].name == null ? "" : this.charInfos[i].name;
            while (ten.length() > 2 && Font.blackFont.getWidth(ten) > cw - 6) {
                ten = ten.substring(0, ten.length() - 1);
            }
            Font.blackFont.drawString(g, ten, cx, y + 61, 2);
            Font.arialFont.drawString(g, new StringBuffer("Cấp ").append(lv).toString(), cx, y + 74, 2);
            if (c.weaponType >= 3 && c.weaponType <= 7) {
                String he = LoaiPokemonClient.HE_KHOI_DAU[c.weaponType - 3];
                String chu = he.startsWith("Hệ ") ? he.substring(3) : he;
                int wb = Font.borderFont.getWidth(chu) + 10;
                if (wb > cw - 6) {
                    wb = cw - 6;
                }
                game.pokemon.GiaoDien.vien(g, cx - wb / 2, y + CAO_THE - 17, wb, 14, game.pokemon.GiaoDien.mauHe(he), chu);
            }
        }
        int yDuoi = yThe() + CAO_THE + 8;
        if (this.charInfos != null && this.selected < this.charInfos.length && this.chars != null && this.selected < this.chars.length) {
            MainChar ci = this.charInfos[this.selected];
            String dong = new StringBuffer(String.valueOf(ci.name)).append(" - Cấp ").append((int) (ci.lastLv > 0 ? ci.lastLv : (short) 1)).toString();
            int dex = duDoanDex(this.chars[this.selected], ci.lastLv);
            if (dex > 0) {
                dong = new StringBuffer(dong).append(" - ").append(LoaiPokemonClient.ten(dex)).toString();
            }
            // pokemon: cắt ".." cho vừa khung (chừa chỗ cờ vùng bên trái)
            dong = game.pokemon.GiaoDien.catChu(Font.borderFont, dong, Canvas.w - 46);
            Font.borderFont.drawString(g, dong, Canvas.hw, yDuoi, 2);
            if (ci.using == 0) {
                Font.borderFont.drawString(g, new StringBuffer("Còn lại: ").append((int) ci.day).append(" ngày").toString(), Canvas.hw, yDuoi + 14, 2);
            }
            if (ci.nationID != -1) {
                int ww = Font.borderFont.getWidth(dong);
                g.drawRegion(Res.imgNation, 0, ci.nationID * 11, 11, 11, 0, (Canvas.hw - (ww / 2)) - 15, yDuoi + 2, 20);
            }
        } else {
            Font.borderFont.drawString(g, game.pokemon.GiaoDien.catChu(Font.borderFont, "Ô trống: bấm để tạo Pokémon", Canvas.w - 24), Canvas.hw, yDuoi, 2);
        }
        super.paint(g);
    }

    @Override
    public void update() {
        Canvas.loginScr.updateCamera();
        for (int i = 0; i < this.chars.length; i++) {
            this.chars[i].update();
        }
        boolean isChangeFocus = false;
        if (Canvas.isKeyPressed(4)) {
            this.selected--;
            isChangeFocus = true;
            if (this.selected < 0) {
                this.selected = 2;
            }
            if (this.charInfos.length > 0) {
                if (this.selected < this.charInfos.length) {
                    if (this.charInfos[this.selected].using == 1) {
                        this.cmdDel.caption = "Xóa";
                    } else {
                        this.cmdDel.caption = "Khôi phục";
                    }
                } else {
                    this.cmdDel.caption = "";
                }
            }
        } else if (Canvas.isKeyPressed(6)) {
            this.selected++;
            isChangeFocus = true;
            if (this.selected > 2) {
                this.selected = 0;
            }
            if (this.charInfos.length > 0) {
                if (this.selected < this.charInfos.length) {
                    if (this.charInfos[this.selected].using == 1) {
                        this.cmdDel.caption = "Xóa";
                    } else {
                        this.cmdDel.caption = "Khôi phục";
                    }
                } else {
                    this.cmdDel.caption = "";
                }
            }
        }
        int pa = -1;
        // pokemon: chạm vào thẻ Pokédex (vị trí theo rongThe / xThe)
        if (Canvas.isPointerClick && Canvas.isPointer(xThe(0), yThe(), 3 * rongThe() + 8, CAO_THE)) {
            pa = (Canvas.px - xThe(0)) / (rongThe() + 4);
            if (pa > 2) {
                pa = 2;
            }
            isChangeFocus = true;
        }
        if (isChangeFocus) {
            if (this.selected < this.chars.length) {
                this.center = this.cmdSelect;
                this.left = this.cmdDel;
            } else {
                this.center = this.cmdNew;
                this.left = null;
            }
            if (pa == this.selected) {
                this.center.action.perform();
            }
            if (pa < 3 && pa >= 0) {
                this.selected = pa;
            }
        }
        super.update();
    }
}
