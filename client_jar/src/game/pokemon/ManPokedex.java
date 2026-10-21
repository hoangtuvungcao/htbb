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
 * chân dung + tên, chip lọc theo hệ, chạm = thẻ chi tiết với Pokémon to động + huy hiệu hệ. Hiện mọi loài đã biết;
 * 0.9.8: loài ĐÃ BẮT (thu phục, gói -121 kiểu 2 - BatPokemon) có dấu Poké Ball ở góc thẻ, chip lọc "Đã bắt", số đã bắt
 * trên đầu lưới.
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
        BatPokemon.xinPokedex();   // 0.9.8: trạng thái "Đã bắt" theo loài
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

    static final String[] CHIP = {"Tất cả", "Lửa", "Nước", "Cỏ", "Điện", "Khác", "Đã bắt"};
    static final int[] HE_CHIP = {-1, HePoke.LUA, HePoke.NUOC, HePoke.CO, HePoke.DIEN, -2, -3};

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
            if (he == -3 ? BatPokemon.daBat(dex) : he == -2 ? khac : (h1 == he || h2 == he)) {
                v.addElement(all.elementAt(i));
            }
        }
        return v;
    }

    /** "#025" */
    static String soDex(int dex) {
        return dex < 10 ? "#00" + dex : dex < 100 ? "#0" + dex : "#" + dex;
    }

    protected void lui() {
        if (this.chon >= 0) {
            this.chon = -1;
        } else {
            dong();
        }
    }

    /** 0.9.6: cao theo số hàng lưới (cột theo bề ngang); chi tiết = tấm cao tối đa. */
    protected int caoMuon(int wNoi) {
        if (this.chon >= 0) {
            return 0;
        }
        int k = Bang.cach(), o = Bang.coO() + 12;
        int cot = Math.max(3, (wNoi + k) / (o + k));
        int n = Math.max(1, loc().size());
        int hang = Math.max(2, (n + cot - 1) / cot);
        return Bang.co(22) + 2 * k + hang * (o + 14 + k) + 4;
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        final Vector v = loc();
        if (this.chon >= 0) {
            veChiTiet(g, x, y, w, h);
            return;
        }
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
        int yl = y + 2;
        yl += Bang.hangChip(g, x, yl, w, CHIP, this.loc, vl) + k;
        if (BatPokemon.coDex) {
            ChuDe.chu(g, "Đã bắt: " + BatPokemon.soDaBat + " / " + dsLoai().size() + " loài", x + 4, yl, ChuDe.PHU, ChuDe.CHU_PHU, 0);
            yl += ChuDe.cao(ChuDe.PHU) + 2;
        }
        if (v.size() == 0) {
            if (this.loc == CHIP.length - 1) {
                Bang.trong(g, x, yl, w, y + h - yl, "Chưa bắt loài nào", "Làm Pokémon hoang dã yếu đi rồi ném Poké Ball (mua ở Y tá Chansey).");
            } else {
                Bang.trong(g, x, yl, w, y + h - yl, "Chưa biết loài nào", "Đi khám phá bản đồ: Pokémon hoang dã sẽ hiện trong Pokédex.");
            }
            return;
        }
        final int o = Bang.coO() + 12;
        final int cot = Math.max(3, (w + k) / (o + k));
        int hang = (v.size() + cot - 1) / cot;
        Bang.danhSach(g, this.ds, x, yl, w, y + h - 2 - yl, hang, o + 14, new Bang.Hang() {
            public void ve(Graphics g, int r, int x, int y, int w, int h) {
                int kk = Bang.cach();
                int s = (w - (cot - 1) * kk) / cot;
                for (int c = 0; c < cot; c++) {
                    final int i = r * cot + c;
                    if (i >= v.size()) {
                        break;
                    }
                    final int[] lt = (int[]) v.elementAt(i);
                    int xi = x + c * (s + kk);
                    // 0.9.6 thẻ GO: trắng bóng mềm, đĩa nhạt màu hệ sau hình, tên đậm + số Pokédex
                    Bang.hang(g, xi, y, s, h, i, false, null, null);
                    int mh = KieuO.mauHe(HePoke.he1(lt[0]));
                    int lh1 = ChuDe.cao(ChuDe.NHAN), lh2 = ChuDe.cao(ChuDe.PHU);
                    int hi = h - lh1 - lh2 - 6;
                    int rd = Math.min(s, hi) / 2 - 3;
                    GiaoDien.khoi(g, xi + s / 2 - rd, y + 4 + hi / 2 - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 4, 5));
                    PokeBody.paintChanDung(g, lt[0], false, xi + s / 2, y + 4 + hi / 2, 2 * rd);
                    String ten = Res.monsterTemplates[lt[1]].name;
                    ChuDe.chu(g, ChuDe.cat(ten, ChuDe.NHAN, s - 6), xi + s / 2, y + 4 + hi, ChuDe.NHAN, ChuDe.CHU, 2);
                    ChuDe.chu(g, soDex(lt[0]), xi + s / 2, y + 4 + hi + lh1, ChuDe.PHU, ChuDe.CHU_PHU, 2);
                    if (BatPokemon.daBat(lt[0])) {
                        GiaoDien.pokeBall(g, xi + s - 10, y + 10, 6, 0);   // 0.9.8: đã bắt
                    }
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
        // 0.9.6: sân khấu sáng theo màu hệ (đĩa tròn dải nhạt) thay khối navy
        int mh = KieuO.mauHe(HePoke.he1(lt[0]));
        float z = Math.max(1.5f, Math.min(3.2f, Math.min(lw / 56f, h / 44f)));
        int cx = x + lw / 2, yChan = y + h - (int) (14 * z);
        int rd = Math.min(lw, h) / 2 - 4;
        GiaoDien.khoiDoc(g, cx - rd, y + h / 2 - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 5, 6), GiaoDien.pha(mh, 0xFFFFFF, 2, 4));
        ManNhanVat.veSanKhau(g, cx, yChan, z);
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
        ChuDe.chuGiua(g, ChuDe.cat(Res.monsterTemplates[lt[1]].name, ChuDe.TIEU_DE, wr - hd - 60), xr + hd + 10, y + 4, hd, ChuDe.TIEU_DE, ChuDe.CHU, 0);
        ChuDe.chuGiua(g, soDex(lt[0]), xr + wr - 10, y + 4, hd, ChuDe.SO, ChuDe.CHU_PHU, 1);
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
        if (BatPokemon.coDex) {
            int co = BatPokemon.coDex(lt[0]);
            String tt = (co & 2) != 0 ? ((co & 4) != 0 ? "Đã bắt (có sắc màu)" : "Đã bắt") : (co & 1) != 0 ? "Đã hạ, chưa bắt" : "Chưa bắt";
            Font.blackFont.drawString(g, tt, xr + 10, yy, 0);
            yy += Font.blackFont.getHeight() + 4;
        }
        Bang.doan(g, Font.arialFont, "Gặp ngoài bản đồ hoang dã. Làm Pokémon yếu đi (HP dưới " + BatPokemon.nguongHp
                + "%) rồi ném Poké Ball để thu phục.", xr + 10, yy, wr - 20, false);
    }
}
