package game.model;

import game.render.screen.Font;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ChatPopup {
    public int timeOut;
    public int xc;
    public int yc;
    public int arrowType;
    public int h;
    public int w;
    public int isAr;
    String[] chats;
    public static Image imgPopup;
    public static Image imgArrow;

    public static void getImg() {
        imgPopup = FilePack.getImg("c");
        imgArrow = FilePack.getImg("ar");
    }

    public ChatPopup() {
    }

    public ChatPopup(int time, String chat, int isAr) {
        prepareData(time, chat);
        this.isAr = isAr;
    }

    public void setPos(int x, int y) {
        this.xc = x;
        this.yc = y;
    }

    public boolean setOut() {
        if (this.timeOut > 0) {
            this.timeOut--;
        }
        if (this.timeOut == 0) {
            return true;
        }
        return false;
    }

    public void prepareData(int time, String chat) {
        this.chats = Font.blackFont.splitFontBStrInLine(chat, 100);
        GameScr.timeDelayTask = this.chats.length * 8;
        this.h = (14 * this.chats.length) + 4 + 4;
        this.w = 30;
        for (int i = 0; i < this.chats.length; i++) {
            int aa = Font.blackFont.getWidth(this.chats[i]) + 5;
            if (aa > this.w) {
                this.w = aa;
            }
        }
        this.timeOut = time;
    }

    public void paintAnimal(Graphics g) {
        paintRoundRect(g, this.xc - (this.w / 2), (this.yc - this.h) - 4, this.w, this.h, 16777215, 1);
        if (this.isAr == 1) {
            g.drawImage(imgArrow, this.xc, this.yc - 5, 17);
        }
        int yy = this.yc - this.h;
        for (int i = 0; i < this.chats.length; i++) {
            Font.blackFont.drawString(g, this.chats[i], this.xc, yy, 2);
            yy += 14;
        }
    }

    public static void paintRoundRect(Graphics g, int x, int y, int w, int h, int color1, int color2) {
        // pokemon: bong bóng thoại bo tròn viền tối (cùng kiểu hộp thoại Pokémon)
        game.pokemon.GiaoDien.khoi(g, x, y, w, h, 5, game.pokemon.GiaoDien.VIEN);
        game.pokemon.GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 4, color1);
    }
}
