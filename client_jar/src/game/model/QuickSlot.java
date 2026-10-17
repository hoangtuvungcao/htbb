package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class QuickSlot {
    public byte skillType;
    public byte indexPotion;
    public static final byte TYPE_SKILL = 1;
    public int potionType;
    public static final byte TYPE_POTION = 2;
    public static final byte[] WEAPON_OF_CLAZZ = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4};
    private static Image img = null;
    public int quickslotType = -1;
    public boolean isBuff = false;

    public static void getImg() {
        img = FilePack.getImg("cooldown");
    }

    public void setIsSkill(int skillType, int clazz, boolean isBuff) {
        this.quickslotType = 1;
        this.skillType = (byte) skillType;
        this.isBuff = isBuff;
    }

    /**
     * pokemon: vẽ ô skill nhanh kiểu Pokémon tại (x, y) cỡ w x h: chiêu = ô màu hệ + hình chiêu,
     * bình thuốc = ô xanh + icon + số lượng, ô trống = khung mờ; hồi chiêu = phủ tối từ trên xuống
     * + số giây; thiếu MP = phủ xanh tối. phim = nhãn phím (góc trên-trái).
     */
    public static void paintO(Graphics g, QuickSlot qs, int x, int y, int w, int h, String phim, boolean noiBat) {
        try {
            MainChar mc = Canvas.gameScr.mainChar;
            int loai = qs == null ? -1 : qs.quickslotType;
            long conLai = 0;
            long tong = 1;
            if (loai == TYPE_SKILL) {
                int lop = mc.clazz;
                int sk = qs.skillType;
                game.pokemon.KieuO.oChieu(g, x, y, w, h, game.pokemon.PokeChieu.he(lop, sk), game.pokemon.PokeChieu.hinh(lop, sk), false);
                long da = System.currentTimeMillis() - mc.timeLastUseSkills[sk];
                tong = mc.coolDownSkill[sk];
                if (da < tong) {
                    conLai = tong - da;
                } else {
                    int cap = Char.skillLevelLearnt != null && sk < Char.skillLevelLearnt.length ? Char.skillLevelLearnt[sk] : 0;
                    int mpCan = 0;
                    try {
                        mpCan = game.model.skill.SkillManager.getSkillMP(sk, cap, lop);
                    } catch (Throwable e) {
                    }
                    if (mpCan > 0 && mc.mp < mpCan) {
                        game.pokemon.KieuO.phu(g, x + 1, y + 1, w - 2, h - 2, 0x90101840);
                        g.setColor(game.pokemon.GiaoDien.MP_XANH);
                        g.fillRect(x + 3, y + h - 4, w - 6, 2);
                    }
                }
            } else if (loai == TYPE_POTION) {
                game.pokemon.GiaoDien.khoi(g, x, y, w, h, 4, game.pokemon.GiaoDien.VIEN);
                game.pokemon.GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 3, 0x3A72B8, 0x1A3B6E);
                Res.paintPotion(g, MainChar.listPotion[qs.potionType].index, x + w / 2, y + h / 2, 3);
                game.pokemon.KieuO.soLuong(g, x + w - 1, y + h - 1, game.pokemon.KieuO.soGon(mc.potions[qs.potionType]));
                long da = System.currentTimeMillis() - mc.potionLastTimeUse[qs.potionType];
                tong = MainChar.listPotion[qs.potionType].delay;
                if (da < tong) {
                    conLai = tong - da;
                }
            } else {
                // ô trống: khung mờ, dấu + nhạt
                game.pokemon.GiaoDien.khoi(g, x, y, w, h, 4, 0x0E1A30);
                game.pokemon.KieuO.phu(g, x + 1, y + 1, w - 2, h - 2, 0x80203A66);
                g.setColor(0x6F86AE);
                g.fillRect(x + w / 2 - 3, y + h / 2, 7, 1);
                g.fillRect(x + w / 2, y + h / 2 - 3, 1, 7);
            }
            if (conLai > 0 && tong > 0) {
                int hh = (int) ((h - 2) * conLai / tong);
                if (hh < 1) {
                    hh = 1;
                }
                game.pokemon.KieuO.phuCat(g, x + 1, y + 1, w - 2, h - 2, hh, 0xA8000000);
                g.setColor(game.pokemon.GiaoDien.VANG);
                g.fillRect(x + 2, y + hh, w - 4, 1);
                long giay = (conLai + 999) / 1000;
                Font.borderFont.drawString(g, String.valueOf(giay), x + w / 2, y + (h - Font.borderFont.getHeight()) / 2 + 1, Font.CENTER);
            }
            if (noiBat) {
                g.setColor(game.pokemon.GiaoDien.VANG);
                g.drawRect(x - 1, y - 1, w + 1, h + 1);
            }
            if (phim != null) {
                game.pokemon.KieuO.nhanPhim(g, x - 1, y - 1, phim);
            }
        } catch (Exception e) {
            Cout.println(new StringBuffer("ERROR QUICKSLOT ").append(e.toString()).toString());
        }
    }

    public byte getSkillType() {
        return this.skillType;
    }

    public void update() {
    }

    public int getPotionType() {
        return this.potionType;
    }

    public void setIsPotion(int potionType) {
        this.quickslotType = 2;
        this.potionType = potionType;
    }

    public void setIsNothing() {
        this.quickslotType = -1;
    }
}
