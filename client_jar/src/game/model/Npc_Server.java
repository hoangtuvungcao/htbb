package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Npc_Server extends NPC {
    public short idImg;
    public byte nFrame;
    public byte index;

    public Npc_Server() {
        this.npcType = (byte) 1;
        this.catagory = (byte) 2;
    }

    public Npc_Server(int tx, int ty, int type, FilePack file) {
        super(tx, ty, type, file);
    }

    @Override
    public void paint(Graphics g) {
        ImageIcon img = GameData.getImgIcon((short) (this.idImg + 4200));
        if (img != null && !img.isLoad) {
            g.drawRegion(GameData.getImgIcon((short) (this.idImg + 4200)).img, 0, this.height * this.index, this.width, this.height, 0, this.x, this.y, 33);
        }
        Image tempImg = GameScr.getImgQuest(this.type);
        if (!tempImg.equals(Res.imgSelect)) {
            g.drawImage(tempImg, this.x, (this.y - this.height) - 5, 33);
        }
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
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
