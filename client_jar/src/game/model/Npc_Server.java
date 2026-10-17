package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import game.pokemon.PokeBody;
import game.pokemon.VatThePokemon;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Npc_Server extends NPC {
    public short idImg;
    public byte nFrame;
    public byte index;

    /**
     * pokemon: NPC su kien (lenh -50) theo idImg -> {idImg, dex} (G7.B2, ClientUnity Npc_Server.cs).
     * type cua Npc_Server la ID server, KHONG phai type NPC tinh. Do vat (cong, bang, noi) ve qua
     * VatThePokemon.NPC. Bang day du: KPAH docs/pokemon/bang_npc_su_kien.csv.
     */
    public static final short[][] POKE_DEX_THEO_IDIMG = {{14, 52}, {20, 282}, {21, 667}, {23, 460}, {24, 225}, {27, 448}, {28, 214}, {29, 185}, {30, 185}};

    public static int dexTheoIdImg(int idImg) {
        if (!PokeBody.CHE_DO_POKEMON) {
            return 0;
        }
        for (int i = 0; i < POKE_DEX_THEO_IDIMG.length; i++) {
            if (POKE_DEX_THEO_IDIMG[i][0] == idImg) {
                return POKE_DEX_THEO_IDIMG[i][1];
            }
        }
        return 0;
    }

    public int getPokeDex() {
        return dexTheoIdImg(this.idImg);
    }

    public Npc_Server() {
        this.npcType = (byte) 1;
        this.catagory = (byte) 2;
    }

    public Npc_Server(int tx, int ty, int type, FilePack file) {
        super(tx, ty, type, file);
    }

    @Override
    public void paint(Graphics g) {
        int caoIcon = this.height;
        if (paintPokemon(g)) {
            caoIcon = PokeBody.caoTrenChan(getPokeDex(), false, 100) + 4;
        } else if (VatThePokemon.veNpc(g, this.idImg, this.x, this.y, Canvas.gameTick)) {
            caoIcon = VatThePokemon.caoNpc(this.idImg);
        } else {
            ImageIcon img = GameData.getImgIcon((short) (this.idImg + 4200));
            if (img != null && !img.isLoad) {
                g.drawRegion(GameData.getImgIcon((short) (this.idImg + 4200)).img, 0, this.height * this.index, this.width, this.height, 0, this.x, this.y, 33);
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
        if (VatThePokemon.veGocNpc(g, this.idImg, xCorner, yCorner - 8, 24)) {
            return;
        }
        g.setClip(xCorner - 10, (yCorner + 12) - this.height, 20, 22);
        ImageIcon img = GameData.getImgIcon((short) (this.idImg + 4200));
        if (img != null && !img.isLoad) {
            g.drawRegion(GameData.getImgIcon((short) (this.idImg + 4200)).img, 0, 0, this.width, this.height, 0, xCorner, (yCorner + 12) - this.height, 17);
        }
    }

    @Override
    public void update() {
        if (this.typeLimit != 1) {
            super.update();
            return;
        }
        if (Canvas.gameTick % 4 == 0) {
            this.index = (byte) (this.index + 1);
        }
        if (this.index >= this.nFrame) {
            this.index = (byte) 0;
        }
    }

    @Override
    public void setPosTo(short x, short y) {
    }

    @Override
    public int getLimxL() {
        return (this.x - (this.width / 2)) + 10;
    }

    @Override
    public int getLimxR() {
        return (this.x + (this.width / 2)) - 10;
    }

    @Override
    public int getLimyU() {
        return (this.y - ((this.height * 3) / 4)) - 30;
    }

    @Override
    public int getLimyD() {
        return this.y - 40;
    }

    @Override
    public boolean isNPC() {
        return true;
    }

    @Override
    public int getNpcType() {
        return this.npcType;
    }
}
