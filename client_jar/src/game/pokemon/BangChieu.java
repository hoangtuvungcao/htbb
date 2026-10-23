package game.pokemon;

import game.model.Char;
import game.model.skill.SkillManager;
import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Tab "Kỹ năng" của bảng nhân vật (WindowInfoScr / ShopNew) kiểu Pokémon - vẽ trong hệ tọa độ
 * nội dung của tab (gốc x0, y0 của bảng, rộng ~114, từ y = -16 đến ~104):
 *  - đầu: điểm kỹ năng còn lại;
 *  - hàng ô chiêu (rộng 23 px mỗi ô, cuộn ngang cmx như cũ): màu hệ + hình chiêu, cấp ở góc,
 *    chiêu chưa học xám; ô đang chọn viền vàng; mũi tên khi còn chiêu ngoài khung + "3/9";
 *  - thẻ chiêu: tên Pokémon + viên hệ, cấp (x/9 + vạch), sát thương, MP, hồi chiêu, tầm,
 *    mô tả; chưa học: "Học ở cấp X".
 * Phím / chạm vẫn do bảng gốc xử lý (selected, cmx, cmy) - lớp này chỉ vẽ.
 */
public final class BangChieu {
    private static final byte[][] CHIEU_BUFF = {{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};

    private BangChieu() {
    }

    public static boolean laBuff(int lop, int sk) {
        if (lop < 0 || lop >= CHIEU_BUFF.length) {
            return false;
        }
        for (int i = 0; i < CHIEU_BUFF[lop].length; i++) {
            if (CHIEU_BUFF[lop][i] == sk) {
                return true;
            }
        }
        return false;
    }

    /** Ô chiêu s x s (góc trên-trái x, y) của chiêu sk thuộc dòng của nhân vật đang chơi. */
    public static void icon(Graphics g, int x, int y, int s, int sk) {
        int lop = Canvas.gameScr != null && Canvas.gameScr.mainChar != null ? Canvas.gameScr.mainChar.clazz : 0;
        KieuO.oChieuSk(g, x, y, s, s, lop, sk, false);
    }

    private static int cap(int sk) {
        return Char.skillLevelLearnt != null && sk >= 0 && sk < Char.skillLevelLearnt.length ? Char.skillLevelLearnt[sk] : -1;
    }

    /** Cấp nhân vật cần để học (cap <= 0) / nâng chiêu lên cấp tiếp (cap >= 1). */
    public static int capCan(int sk, int cap) {
        try {
            int i = cap < 0 ? 1 : cap;
            return SkillManager.LEVEL_ADD_SKILL[sk][i];
        } catch (Throwable e) {
            return 0;
        }
    }

    private static String giay(long ms) {
        long ds = (ms + 50) / 100;
        return new StringBuffer().append(ds / 10).append('.').append(ds % 10).append('s').toString();
    }

    /**
     * Vẽ tab. soChieu = numW, chon = selected, doiTab = isChangeTab, cmx = cuộn ngang hàng ô,
     * dyThe = độ cuộn dọc của thẻ (cmy khi người chơi kéo thẻ, 0 nếu không), w = bề rộng vùng.
     */
    public static void ve(Graphics g, int soChieu, int chon, boolean doiTab, int cmx, int dyThe, int w) {
        int lop = Canvas.gameScr.mainChar.clazz;
        int diem = Canvas.gameScr.mainChar.skillPointLeft;
        GiaoDien.moClip(g);
        // ---- đầu: điểm chiêu
        // viên điểm sát mép phải, nhãn canh phải sát viên (không đè "x/y" bên trái ở máy hẹp)
        String sd = String.valueOf(diem);
        int wd = Font.borderFont.getWidth(sd) + 10;
        int xd = w + 4 - wd;
        GiaoDien.khoi(g, xd, -15, wd, 14, 6, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, xd + 1, -14, wd - 2, 12, 5, diem > 0 ? GiaoDien.VANG_NHAT : 0x8A93A3, diem > 0 ? GiaoDien.VANG_TOI : 0x5E6A80);
        Font.borderFont.drawString(g, sd, xd + wd / 2, -15, Font.CENTER);
        String nhan = "Điểm chiêu:";
        String so = new StringBuffer().append(chon + 1).append('/').append(soChieu).toString();
        int wSo = Font.smallFont.getWidth(so);
        if (Font.arialFontW.getWidth(nhan) > xd - 3 - (-6 + wSo + 4)) {
            nhan = "Điểm:";
        }
        Font.arialFontW.drawString(g, nhan, xd - 3, -15, Font.RIGHT);
        // chỉ số chiêu đang xem
        Font.smallFont.drawString(g, so, -6, -10, Font.LEFT);
        // ---- hàng ô chiêu
        if (chon < 0) {
            chon = 0;
        }
        if (chon > soChieu - 1) {
            chon = soChieu - 1;
        }
        g.setClip(-4, 0, w + 8, 28);
        g.translate(-cmx, 0);
        for (int i = 0; i < soChieu; i++) {
            int x = i * 23 + 1;
            int c = cap(i);
            KieuO.oChieuSk(g, x, 3, 21, 21, lop, i, c <= 0);
            if (c > 0) {
                KieuO.soLuong(g, x + 21, 3 + 21, c >= PokeChieu.CAP_TOI_DA ? "max" : String.valueOf(c));
            }
        }
        if (!doiTab) {
            KieuO.chonO(g, chon * 23 + 1, 3, 22);
        }
        g.translate(cmx, 0);
        GiaoDien.moClip(g);
        // mũi tên: còn chiêu bên trái / phải khung
        boolean conTrai = cmx > 2;
        boolean conPhai = soChieu * 23 - cmx > w + 4;
        if (Canvas.gameTick % 10 < 7) {
            g.setColor(GiaoDien.VANG);
            if (conTrai) {
                g.fillTriangle(-9, 13, -5, 9, -5, 17);
            }
            if (conPhai) {
                g.fillTriangle(w + 7, 13, w + 3, 9, w + 3, 17);
            }
        }
        if (doiTab) {
            return;
        }
        // ---- thẻ chiêu
        int x0 = -9;
        int wThe = w + 21;
        int y0 = 28;
        int hThe = 77;
        // thẻ chiêu sáng (kiểu trang tóm tắt Pokémon): viền xanh, thân trắng
        GiaoDien.khoi(g, x0, y0, wThe, hThe, 4, GiaoDien.XANH);
        GiaoDien.khoiDoc(g, x0 + 1, y0 + 1, wThe - 2, hThe - 2, 3, GiaoDien.TRANG, GiaoDien.HANG_CHAN);
        g.setClip(x0 + 1, y0 + 1, wThe - 2, hThe - 2);
        int c = cap(chon);
        int he = PokeChieu.he(lop, chon);
        String ten = PokeChieu.ten(lop, chon);
        if (ten == null) {
            try {
                ten = SkillManager.SKILL_NAME[lop][chon];
            } catch (Throwable e) {
                ten = "";
            }
        }
        int y = y0 + 2 - dyThe;
        int wHe = KieuO.rongVienHe(he);
        int xHe = x0 + wThe - 3 - wHe;
        // tên (cắt nếu đè lên viên hệ)
        Font fTen = Font.normalFont[0];
        int wMax = xHe - (x0 + 4) - 3;
        if (fTen.getWidth(ten) > wMax) {
            while (ten.length() > 2 && fTen.getWidth(ten + "..") > wMax) {
                ten = ten.substring(0, ten.length() - 1);
            }
            ten = ten + "..";
        }
        fTen.drawString(g, ten, x0 + 4, y + 1, Font.LEFT);
        KieuO.vienHe(g, xHe, y, he);
        y += 16;
        g.setColor(0x3D6AA8);
        g.fillRect(x0 + 4, y - 1, wThe - 8, 1);
        Font f = Font.arialFontW;
        if (c <= 0) {
            f.drawString(g, "Chưa học", x0 + 4, y, Font.LEFT);
            int can = capCan(chon, c);
            boolean du = Canvas.gameScr.mainChar.level >= can;
            String s = du ? "Có thể học" : new StringBuffer("Học ở cấp ").append(can).toString();
            int ws = Font.borderFont.getWidth(s) + 10;
            int xs = x0 + wThe - 4 - ws;
            GiaoDien.khoi(g, xs, y, ws, 15, 7, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, xs + 1, y + 1, ws - 2, 13, 6, du ? GiaoDien.LA : GiaoDien.DO_SANG, du ? GiaoDien.LA_TOI : GiaoDien.DO_TOI);
            Font.borderFont.drawString(g, s, xs + ws / 2, y + 1, Font.CENTER);
            y += 17;
            if (du && diem <= 0) {
                f.drawString(g, "Cần 1 điểm chiêu", x0 + 4, y, Font.LEFT);
                y += 12;
            }
        } else {
            // "Cấp 3/9" + vạch cấp + (▸ cấp cần để nâng tiếp)
            String sc = new StringBuffer("Cấp ").append(c).append('/').append(PokeChieu.CAP_TOI_DA).toString();
            f.drawString(g, sc, x0 + 4, y, Font.LEFT);
            int xv = x0 + 8 + f.getWidth(sc);
            for (int i = 0; i < PokeChieu.CAP_TOI_DA; i++) {
                int xx = xv + i * 4;
                g.setColor(GiaoDien.VIEN);
                g.fillRect(xx, y + 4, 4, 6);
                g.setColor(i < c ? GiaoDien.VANG : 0x3A4F78);
                g.fillRect(xx + 1, y + 5, 2, 4);
            }
            if (c < PokeChieu.CAP_TOI_DA) {
                int xl = xv + PokeChieu.CAP_TOI_DA * 4 + 4;
                g.setColor(GiaoDien.LA);
                g.fillTriangle(xl, y + 3, xl, y + 11, xl + 4, y + 7);
                Font.normalFont[1].drawString(g, new StringBuffer("Cấp ").append(capCan(chon, c)).toString(), xl + 6, y - 1, Font.LEFT);
            }
            y += 13;
            int cot2 = x0 + 4 + (wThe - 8) / 2 + 4;
            int mp = 0;
            int dam = 0;
            long cd = 0;
            int tam = 0;
            try {
                mp = SkillManager.getSkillMP(chon, c, lop);
                dam = SkillManager.getSkillDamPercent(chon, c, lop);
                cd = SkillManager.getSkillCooldown((byte) chon, (byte) lop, c);
                tam = SkillManager.getSkillRange((byte) chon, (byte) lop);
            } catch (Throwable e) {
            }
            boolean buff = laBuff(lop, chon);
            dong(g, x0 + 4, y, buff ? "Tỉ lệ" : "Uy lực", new StringBuffer().append(dam).append('%').toString());
            dong(g, cot2, y, "PP", String.valueOf(mp));
            y += 12;
            dong(g, x0 + 4, y, "Hồi", giay(cd));
            if (!buff || tam > 0) {
                dong(g, cot2, y, "Tầm", String.valueOf(tam));
            }
            y += 13;
        }
        // mô tả (ngắt dòng)
        String[] mt = f.splitFontBStrInLine(PokeChieu.moTa(lop, chon), wThe - 10);
        for (int i = 0; mt != null && i < mt.length; i++) {
            f.drawString(g, mt[i], x0 + 4, y, Font.LEFT);
            y += 12;
        }
        GiaoDien.moClip(g);
    }

    /** "Nhãn giá trị" - nhãn chữ mảnh, giá trị chữ đậm. */
    private static void dong(Graphics g, int x, int y, String nhan, String gt) {
        Font.arialFontW.drawString(g, nhan, x, y, Font.LEFT);
        int w = Font.arialFontW.getWidth(nhan) + 4;
        Font.normalFont[0].drawString(g, gt, x + w, y - 1, Font.LEFT);
    }
}
