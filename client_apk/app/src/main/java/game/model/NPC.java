package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;

public class NPC extends Actor {
    public int idLinhGac;
    private Bitmap[] img;
    public String name;
    public int type;
    public static final String[] NPCNAME = {"Dì út HP", "Bà tám tạp hóa", "Hắc ngưu", "Thiết bì", "Lính gác", "Trưởng làng", "Phú ông", "Xa phu", "ông nội", "Anh bảy", "Hoa tiêu", "Nhất giáp", "Nhị giáp", "Tam giáp", "Tứ giáp", "Ngũ giáp", "Nhất ngưu", "Nhị ngưu", "Tam ngưu", "Tứ ngưu", "Ngũ ngưu", "Lâm tướng quân", "Nhật thương nhân", "Hỏa xích", "Bảo ngọc", "Trần thống lĩnh", "Kim hoa", "Giáp Sư", "Kiếm Sư", "Bội Châu", "An Tâm", "Lộc Phát"};
    static byte[] idImg = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 14, 13, 15, 16, 17, 1, 3, 2, 6, 9, 13};
    public static byte[] NPCX = {0, 0, 2, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static byte[] NPCY = {-7, -4, -5, -5, -5, -9, -9, -10, -7, 0, -11, -10, -9, -10, 0, -5, -9, -5};
    public byte npcType = 0;
    int dy = 0;
    int fra = 0;

    public int getLimxL() {
        return 0;
    }

    public int getLimxR() {
        return 0;
    }

    public int getLimyD() {
        return 0;
    }

    public int getLimyU() {
        return 0;
    }

    @Override
    public boolean isNPC() {
        return true;
    }

    public NPC() {
    }

    public NPC(int i, int i2, int i3, FilePack filePack) {
        this.catagory = (byte) 2;
        this.x = (short) ((i * 16) + 8);
        this.y = (short) ((i2 * 16) + 8);
        this.height = (short) 30;
        this.width = (short) 40;
        this.type = i3;
        Bitmap[] bitmapArr = new Bitmap[2];
        this.img = bitmapArr;
        bitmapArr[0] = filePack.loadImage("npc" + ((int) idImg[i3]) + "0.png");
        this.img[1] = filePack.loadImage("npc" + ((int) idImg[i3]) + "1.png");
        if (i3 == 4) {
            Tilemap.idLinhGac++;
            this.idLinhGac = Tilemap.idLinhGac;
        }
    }

    @Override
    public String getDisplayName() {
        String str = this.name;
        return str != null ? str : NPCNAME[this.type];
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.drawImage(this.img[1], this.x, this.y, Graphics.BOTTOM | Graphics.HCENTER);
        Bitmap bitmap = this.img[0];
        if (bitmap != null) {
            graphics.drawImage(bitmap, this.x + NPCX[idImg[this.type]], this.y + NPCY[idImg[this.type]] + (this.fra / 5), Graphics.BOTTOM | Graphics.HCENTER);
            int i = this.fra + 1;
            this.fra = i;
            if (i > 9) {
                this.fra = 0;
            }
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
        Bitmap[] bitmapArr = this.img;
        Bitmap bitmap = bitmapArr[0];
        if (bitmap != null) {
            graphics.drawImage(bitmap, i, i4 - this.height, Graphics.TOP | Graphics.HCENTER);
        } else {
            Bitmap bitmap2 = bitmapArr[1];
            if (bitmap2 != null) {
                graphics.drawImage(bitmap2, i, i4 - this.height, Graphics.TOP | Graphics.HCENTER);
            }
        }
        graphics.restoreCanvas();
    }

    @Override
    public void setPosTo(short s, short s2) {
        this.x = s;
        this.y = s2;
    }

    @Override
    public void update() {
        super.update();
        if (this.chat == null || this == GCanvas.gameScr.focusedActor) {
            return;
        }
        this.chat = null;
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
