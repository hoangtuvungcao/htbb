package game.pokemon;

import game.model.IAction;
import game.model.MonsterTemplate;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * POKÉDEX (0.9.3): các loài Pokémon hoang dã của thế giới game (mẫu quái máy chủ gửi: tên, hệ, số dex -91), lưới
 * chân dung + tên, chip lọc theo hệ, chạm = thẻ chi tiết với Pokémon to động + huy hiệu hệ. Client chưa có dữ liệu
 * "đã gặp / đã bắt" -> hiện mọi loài đã biết.
 */
public final class ManPokedex extends ManBang {
    public static ManPokedex me;
    int loc;
    int chon = -1;
    static Vector loai;

    public static ManPokedex gI() {
        if (me == null) {
            me = new ManPokedex();
        }
        return me;
    }

    public static void mo() {
        loai = null;
        gI().chon = -1;
        gI().moBang();
    }

    protected String tieuDe() {
        return "Pokédex";
    }

    /** Danh sách {dex, chỉ số mẫu} không trùng dex, theo dex tăng. */
    static Vector dsLoai() {
        if (loai != null) {
            return loai;
        }
        Vector v = new Vector();
        try {
            MonsterTemplate[] m = Res.monsterTemplates;
            for (int t = 0; m != null && t < m.length && t < MonsterTemplate.pokeDex.length; t++) {
                int dex = MonsterTemplate.pokeDex[t];
                if (m[t] == null || dex <= 0) {
                    continue;
                }
                boolean co = false;
                int chen = v.size();
                for (int i = 0; i < v.size(); i++) {
                    int d = ((int[]) v.elementAt(i))[0];
                    if (d == dex) {
                        co = true;
                        break;
                    }
                    if (d > dex && chen == v.size()) {
                        chen = i;
                    }
                }
                if (!co) {
                    v.insertElementAt(new int[]{dex, t}, chen);
                }
            }
        } catch (Throwable e) {
            // chưa có mẫu quái
        }
        loai = v;
        return v;
    }

    static final String[] CHIP = {"Tất cả", "Lửa", "Nước", "Cỏ", "Điện", "Khác"};
    static final int[] HE_CHIP = {-1, HePoke.LUA, HePoke.NUOC, HePoke.CO, HePoke.DIEN, -2};

    Vector loc() {
        Vector all = dsLoai();
        if (this.loc == 0) {
            return all;
        }
        Vector v = new Vector();
        int he = HE_CHIP[this.loc];
        for (int i = 0; i < all.size(); i++) {
            int dex = ((int[]) all.elementAt(i))[0];
            int h1 = HePoke.he1(dex), h2 = HePoke.he2(dex);
            boolean khac = h1 != HePoke.LUA && h1 != HePoke.NUOC && h1 != HePoke.CO && h1 != HePoke.DIEN;
            if (he == -2 ? khac : (h1 == he || h2 == he)) {
                v.addElement(all.elementAt(i));
            }
        }
        return v;
    }

    protected void lui() {
        if (this.chon >= 0) {
            this.chon = -1;
        } else {
            dong();
        }
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        final Vector v = loc();
        if (this.chon >= 0) {
            veChiTiet(g, x, y, w, h);
            return;
        }
        Bang.the(g, x, y, w, h);
        Object[] vl = new Object[CHIP.length];
        for (int i = 0; i < CHIP.length; i++) {
            final int li = i;
            vl[i] = new IAction() {
                public void perform() {
                    ManPokedex.this.loc = li;
                    ManPokedex.this.ds.den(0);
                }
            };
        }
        int yl = y + 6;
        yl += Bang.hangChip(g, x + 6, yl, w - 12, CHIP, this.loc, vl) + k;
        if (v.size() == 0) {
            Bang.trong(g, x, yl, w, y + h - yl, "Chưa biết loài nào", "Đi khám phá bản đồ: Pokémon hoang dã sẽ hiện trong Pokédex.");
            return;
        }
        final int o = Bang.coO();
        final int cot = Math.max(3, (w - 16 + k) / (o + k));
        int hang = (v.size() + cot - 1) / cot;
        Bang.danhSach(g, this.ds, x + 6, yl, w - 12, y + h - 6 - yl, hang, o, new Bang.Hang() {
            public void ve(Graphics g, int r, int x, int y, int w, int h) {
                int kk = Bang.cach();
                int s = Math.min(h, (w - (cot - 1) * kk) / cot);
                for (int c = 0; c < cot; c++) {
                    final int i = r * cot + c;
                    if (i >= v.size()) {
                        break;
                    }
                    final int[] lt = (int[]) v.elementAt(i);
                    int xi = x + c * (s + kk);
                    GiaoDien.khoi(g, xi, y, s, h, 8, GiaoDien.VIEN);
                    GiaoDien.khoiDoc(g, xi + 1, y + 1, s - 2, h - 2, 7, 0xFFFFFF, 0xE4EAF5);
                    int fh = Font.arialFont.getHeight();
                    PokeBody.paintChanDung(g, lt[0], false, xi + s / 2, y + (h - fh) / 2, Math.min(s, h - fh) - 8);
                    String ten = Res.monsterTemplates[lt[1]].name;
                    Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, ten, s - 4), xi + s / 2, y + h - fh - 2, Font.CENTER);
                    Bang.dangKy(g, xi, y, s, h, new IAction() {
                        public void perform() {
                            ManPokedex.this.chon = i;
                        }
                    }, "dex-" + i);
                }
            }
        });
    }

    private void veChiTiet(Graphics g, int x, int y, int w, int h) {
        Vector v = loc();
        if (this.chon >= v.size()) {
            this.chon = -1;
            return;
        }
        int[] lt = (int[]) v.elementAt(this.chon);
        int k = Bang.cach();
        int lw = Math.min(w * 45 / 100, 260);
        GiaoDien.khoiDoc(g, x, y, lw, h, 8, 0x2C4A86, 0x16285A);
        float z = Math.max(1.5f, Math.min(3.2f, Math.min(lw / 56f, h / 44f)));
        int cx = x + lw / 2, yChan = y + h - (int) (14 * z);
        g.setColor(0x9FC4FF);
        g.fillArc(cx - (int) (26 * z), yChan - (int) (3.5f * z), (int) (52 * z), (int) (7 * z), 0, 360);
        if (PhongVe.co()) {
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            PhongVe.bo.bat(g, z);
            PokeBody.paint(g, lt[0], false, (int) ((cx + tx) / z), (int) ((yChan + ty) / z), NgoaiHinhVe.huongThe(Canvas.gameTick), PokeDuLieu.DUNG, Canvas.gameTick, 100);
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
        } else {
            PokeBody.paint(g, lt[0], false, cx, yChan, 0, PokeDuLieu.DUNG, Canvas.gameTick, (int) (100 * z));
        }
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, xr + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManPokedex.this.chon = -1;
            }
        });
        Font.blackFont.drawString(g, "#" + lt[0] + "  " + Res.monsterTemplates[lt[1]].name, xr + hd + 10, y + 4 + (hd - Font.blackFont.getHeight()) / 2, 0);
        int yy = y + 8 + hd;
        int xb = xr + 10;
        int h1 = HePoke.he1(lt[0]), h2 = HePoke.he2(lt[0]);
        xb += KieuO.vienHe(g, xb, yy, h1) + 6;
        if (h2 >= 0 && h2 != h1) {
            KieuO.vienHe(g, xb, yy, h2);
        }
        yy += 22;
        int hp = Res.monsterTemplates[lt[1]].maxhp;
        if (hp > 0) {
            Font.arialFont.drawString(g, "HP gốc: " + hp, xr + 10, yy, 0);
            yy += Font.arialFont.getHeight() + 4;
        }
        Bang.doan(g, Font.arialFont, "Gặp ngoài bản đồ hoang dã. Chạm Pokémon hoang dã để chọn làm mục tiêu.", xr + 10, yy, wr - 20, false);
    }
}
