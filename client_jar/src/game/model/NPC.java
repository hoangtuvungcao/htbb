package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import game.pokemon.PokeBody;
import game.pokemon.PokeDuLieu;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class NPC extends Actor {
    public int type;
    public int idLinhGac;
    private Image[] img;
    public String name;
    /**
     * Tên NPC tĩnh theo type 0..31 = bảng SQL data_npc (sql/pokemon_ten_hien.sql, sửa ở web /admin/ten-hien). Giao
     * thức chưa có gói gửi tên NPC tĩnh nên bản sao này vẫn nằm trong client: server test KiemTenHien bắt lệch với SQL
     * (sửa tên NPC tĩnh -> sửa ở đây + NPC.cs rồi phát hành client).
     */
    public static final String[] NPCNAME = {"Y tá Chansey", "Kecleon tạp hoá", "Machamp thợ rèn", "Bronzong thợ giáp", "Arcanine gác cổng", "Trưởng làng Xatu", "Ngài Persian", "Pidgeot chở bay", "Ông Snorlax", "Kangaskhan thủ kho", "Lapras chở đò", "Golem bán hàng 1", "Golem bán hàng 2", "Golem bán hàng 3", "Golem bán hàng 4", "Golem bán hàng 5", "Tauros bán hàng 1", "Tauros bán hàng 2", "Tauros bán hàng 3", "Tauros bán hàng 4", "Tauros bán hàng 5", "Huấn luyện viên Blaziken", "Meowth thương nhân", "Lão Torkoal", "Sableye buôn đá quý", "Chỉ huy Heracross", "Kecleon bán lẻ", "Bronzong bán giáp", "Farfetch'd thợ cả", "Persian quản gia", "Kangaskhan quản kho", "Meowth đổi xu"};
    static byte[] idImg = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 14, 13, 15, 16, 17, 1, 3, 2, 6, 9, 13};
    public static byte[] NPCX;
    public static byte[] NPCY;
    public byte npcType = 0;
    int dy = 0;
    int fra = 0;

    static {
        byte[] bArr = new byte[18];
        bArr[2] = 2;
        bArr[4] = 1;
        bArr[7] = 2;
        NPCX = bArr;
        NPCY = new byte[]{-7, -4, -5, -5, -5, -9, -9, -10, -7, 0, -11, -10, -9, -10, 0, -5, -9, -5};
    }

    /**
     * pokemon: NPC tinh (type 0..31, cung index voi NPCNAME) -> so Pokedex ve bang PokeBody.
     * 0 = giu anh NPC cu. Bang day du: HTBB du_lieu/bang_npc.csv (Unity NPC.cs).
     */
    public static final short[] POKE_DEX_THEO_TYPE = {113, 352, 68, 437, 59, 178, 53, 18, 143, 115, 131, 76, 76, 76, 76, 76, 128, 128, 128, 128, 128, 257, 52, 324, 302, 214, 352, 437, 83, 53, 115, 52};

    public NPC() {
    }

    /** So Pokedex cua NPC nay (0 = khong doi sang Pokemon). */
    public int getPokeDex() {
        if (this.type < 0 || this.type >= POKE_DEX_THEO_TYPE.length) {
            return 0;
        }
        return POKE_DEX_THEO_TYPE[this.type];
    }

    /** Ve NPC bang PokeBody, (x, y) la diem chan (anh cu cung ve BOTTOM tai y). */
    public boolean paintPokemon(Graphics g) {
        int dex = getPokeDex();
        if (dex <= 0 || !PokeBody.coSprite(dex, false)) {
            return false;
        }
        g.drawImage(Res.imgShadow, this.x, this.y - 2, 3);
        return PokeBody.paint(g, dex, false, this.x, this.y, Char.DOWN, PokeDuLieu.DUNG, Canvas.gameTick, 100);
    }

    public NPC(int tx, int ty, int type, FilePack file) {
        this.catagory = (byte) 2;
        this.x = (short) ((tx * 16) + 8);
        this.y = (short) ((ty * 16) + 8);
        this.height = (short) 30;
        this.width = (short) 40;
        this.type = type;
        this.img = new Image[2];
        this.img[0] = file.loadImage(new StringBuffer("npc").append((int) idImg[type]).append("0").append(".png").toString());
        this.img[1] = file.loadImage(new StringBuffer("npc").append((int) idImg[type]).append("1").append(".png").toString());
        if (type == 4) {
            Tilemap.idLinhGac++;
            this.idLinhGac = Tilemap.idLinhGac;
        }
    }

    @Override
    public String getDisplayName() {
        if (this.name != null) {
            return this.name;
        }
        return NPCNAME[this.type];
    }

    @Override
    public void paint(Graphics g) {
        int caoIcon = this.height;
        if (paintPokemon(g)) {
            // icon nhiem vu tren DAU sprite Pokemon (cao that), khong phai chieu cao co dinh cua anh NPC cu - giong Npc_Server
            caoIcon = PokeBody.caoTrenChan(getPokeDex(), false, 100) + 4;
        } else {
            g.drawImage(this.img[1], this.x, this.y, 33);
            if (this.img[0] != null) {
                g.drawImage(this.img[0], this.x + NPCX[idImg[this.type]], this.y + NPCY[idImg[this.type]] + (this.fra / 5), 33);
            }
        }
        Image tempImg = GameScr.getImgQuest(this.type);
        if (!tempImg.equals(Res.imgSelect)) {
            g.drawImage(tempImg, this.x, (this.y - caoIcon) - 5, 33);
        }
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
        int dex = getPokeDex();
        if (dex > 0 && PokeBody.paintChanDung(g, dex, false, xCorner, yCorner - 8, 24)) {
            return;
        }
        g.setClip(xCorner - 10, (yCorner + 12) - this.height, 20, 22);
        if (this.img[0] != null) {
            g.drawImage(this.img[0], xCorner, (yCorner + 12) - this.height, 17);
        } else if (this.img[1] != null) {
            g.drawImage(this.img[1], xCorner, (yCorner + 12) - this.height, 17);
        }
    }

    @Override
    public void setPosTo(short x, short y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
        super.update();
        if (this.chat != null && this != Canvas.gameScr.focusedActor) {
            this.chat = null;
        }
        if (this.img[0] != null) {
            this.fra++;
            if (this.fra > 9) {
                this.fra = 0;
            }
        }
        if (Canvas.gameScr.posNpcRequest != null && Util.abs((this.x / 16) - (Canvas.gameScr.posNpcRequest.x / 16)) <= 1 && Util.abs((this.y / 16) - (Canvas.gameScr.posNpcRequest.y / 16)) <= 1) {
            if (this.dy > -2) {
                this.dy--;
            } else {
                this.dy = 0;
            }
        }
    }

    public int getLimxL() {
        return 0;
    }

    public int getLimxR() {
        return 0;
    }

    public int getLimyU() {
        return 0;
    }

    public int getLimyD() {
        return 0;
    }

    @Override
    public boolean isNPC() {
        return true;
    }

    @Override
    public int isNpcQuest() {
        return GameScr.isNpcQuest(this.type);
    }

    @Override
    public int getNpcType() {
        return this.npcType;
    }

    @Override
    public int getTypeNpc() {
        return this.type;
    }
}
