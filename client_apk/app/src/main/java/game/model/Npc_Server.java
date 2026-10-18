package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameData;
import game.render.screen.GameScr;

public class Npc_Server extends NPC {
    public short idImg;
    public byte index;
    public byte nFrame;

    @Override
    public boolean isNPC() {
        return true;
    }

    @Override
    public void setPosTo(short s, short s2) {
    }

    public Npc_Server() {
        this.npcType = (byte) 1;
        this.catagory = (byte) 2;
    }

    public Npc_Server(int i, int i2, int i3, FilePack filePack) {
        super(i, i2, i3, filePack);
    }

    @Override
    public void paint(Graphics graphics) {
        ImageIcon imgIcon = GameData.getImgIcon((short) (this.idImg + 4200));
        if (imgIcon != null && !imgIcon.isLoad) {
            graphics.drawRegion(GameData.getImgIcon((short) (this.idImg + 4200)).img, 0, this.height * this.index, this.width, this.height, 0, this.x, this.y, Graphics.BOTTOM | Graphics.HCENTER);
        }
        Bitmap imgQuest = GameScr.getImgQuest(this.type);
        if (imgQuest.equals(Res.imgSelect)) {
            return;
        }
        graphics.drawImage(imgQuest, this.x, (this.y - this.height) - 5, Graphics.HCENTER | Graphics.BOTTOM);
    }

    @Override
    public void paintCorner(Graphics graphics, int i, int i2) {
        int i3 = i - 10;
        int i4 = i2 + 12;
        graphics.setClip(i3, i4 - this.height, 20, 22);
        graphics.ClipRec(i3, i4 - this.height, 20, 22);
        ImageIcon imgIcon = GameData.getImgIcon((short) (this.idImg + 4200));
        if (imgIcon != null && !imgIcon.isLoad) {
            graphics.drawRegion(GameData.getImgIcon((short) (this.idImg + 4200)).img, 0, 0, this.width, this.height, 0, i, i4 - this.height, Graphics.TOP | Graphics.HCENTER);
        }
        graphics.restoreCanvas();
    }

    @Override
    public void update() {
        if (GCanvas.gameTick % 4 == 0) {
            this.index = (byte) (this.index + 1);
        }
        if (this.index >= this.nFrame) {
            this.index = (byte) 0;
        }
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
    public int getNpcType() {
        return this.npcType;
    }
}
