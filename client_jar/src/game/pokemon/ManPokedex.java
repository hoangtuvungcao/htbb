package game.pokemon;

import game.model.IAction;
import game.model.MonsterTemplate;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * POKÉDEX: 0.9.3 các loài hoang dã của thế giới game (mẫu quái -91); 1.0 (máy chủ mới, lệnh -126): ĐỦ 1.025 loài
 * từ bảng loài máy chủ (BangLoai: tên, hệ, bậc, thế hệ, đã mở), cờ đã gặp / đã hạ / đã bắt của nhân vật. Loài chưa
 * mở đợt hiện "Chưa xuất hiện", loài đã mở nhưng chưa gặp hiện bóng đen "???". Lưới chân dung + tên, chip lọc theo hệ
 * và Đã gặp / Đã bắt; chạm = thẻ chi tiết. Ô ngoài khung nhìn không vẽ (Bang.danhSach chỉ vẽ hàng thấy).
 * Chân dung loài không có trong jar chỉ lấy từ kho đã tải (KhoSprite.choXin = false lúc vẽ lưới - không xin 1.025 gói).
 * Máy chủ cũ (không gửi bảng): như 0.9.3.
 */
public final class ManPokedex extends ManBang {
    public static ManPokedex me;
    int loc;
    int chon = -1;
    static Vector loai;
    static int phienDs = -1;

    public static ManPokedex gI() {
        if (me == null) {
            me = new ManPokedex();
        }
        return me;
    }

    public static void mo() {
        loai = null;
        gI().chon = -1;
        LoaiMang.xinBang(false);
        LoaiMang.xinPokedex();
        gI().moBang();
    }

    protected String tieuDe() {
        return "Pokédex";
    }

    /** Chế độ bảng loài máy chủ (1.025) hay danh sách mẫu quái cũ. */
    static boolean cheDoBang() {
        return BangLoai.coBang();
    }

    /** Danh sách {dex, chỉ số mẫu (-1 = bảng loài)} không trùng dex, theo dex tăng. */
    static Vector dsLoai() {
        if (loai != null && phienDs == BangLoai.phienBan) {
            return loai;
        }
        phienDs = BangLoai.phienBan;
        Vector v = new Vector();
        if (cheDoBang()) {
            short[] d = BangLoai.dex;
            for (int i = 0; i < BangLoai.n; i++) {
                if (d[i] > 0) {
                    v.addElement(new int[]{d[i], -1});
                }
            }
            loai = v;
            return v;
        }
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

    static final String[] CHIP = {"Tất cả", "Lửa", "Nước", "Cỏ", "Điện", "Khác", "Đã gặp", "Đã bắt"};
    static final int[] HE_CHIP = {-1, HePoke.LUA, HePoke.NUOC, HePoke.CO, HePoke.DIEN, -2, -3, -4};

    /** Lọc theo chip; danh sách lọc nhớ theo (chip, phiên bảng) để không lọc lại 1.025 loài mỗi khung. */
    private Vector dsLoc;
    private int locDs = -1, phienLoc = -1;
    private Vector dsGoc;

    Vector loc() {
        Vector all = dsLoai();
        if (this.loc == 0) {
            return all;
        }
        if (this.dsLoc != null && this.locDs == this.loc && this.phienLoc == BangLoai.phienBan && this.dsGoc == all) {
            return this.dsLoc;
        }
        Vector v = new Vector();
        int he = HE_CHIP[this.loc];
        for (int i = 0; i < all.size(); i++) {
            int dex = ((int[]) all.elementAt(i))[0];
            boolean lay;
            if (he == -3) {
                lay = !BangLoai.coPokedex || (BangLoai.coPokedex(dex) & (BangLoai.DEX_GAP | BangLoai.DEX_HA | BangLoai.DEX_BAT)) != 0;
            } else if (he == -4) {
                lay = (BangLoai.coPokedex(dex) & BangLoai.DEX_BAT) != 0;
            } else {
                int h1 = HePoke.he1(dex), h2 = HePoke.he2(dex);
                boolean khac = h1 != HePoke.LUA && h1 != HePoke.NUOC && h1 != HePoke.CO && h1 != HePoke.DIEN;
                lay = he == -2 ? khac : (h1 == he || h2 == he);
            }
            if (lay) {
                v.addElement(all.elementAt(i));
            }
        }
        this.dsLoc = v;
        this.locDs = this.loc;
        this.phienLoc = BangLoai.phienBan;
        this.dsGoc = all;
        return v;
    }

    /** "#025" */
    static String soDex(int dex) {
        return dex < 10 ? "#00" + dex : dex < 100 ? "#0" + dex : "#" + dex;
    }

    /** 0 = hiện bình thường, 1 = chưa gặp (bóng "???"), 2 = chưa xuất hiện (đợt sau). */
    static int anHien(int dex, int tmpl) {
        if (tmpl >= 0 || !cheDoBang()) {
            return 0;
        }
        if (!BangLoai.daMo(dex)) {
            return 2;
        }
        if (BangLoai.coPokedex && BangLoai.coPokedex(dex) == 0) {
            return 1;
        }
        return 0;
    }

    static String tenLoai(int[] lt) {
        if (lt[1] >= 0) {
            return Res.monsterTemplates[lt[1]].name;
        }
        String t = BangLoai.ten(lt[0]);
        return t.length() > 0 ? t : soDex(lt[0]);
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
        if (v.size() == 0) {
            Bang.trong(g, x, yl, w, y + h - yl, this.loc >= 6 ? "Chưa có loài nào" : "Chưa biết loài nào", "Đi khám phá bản đồ: Pokémon hoang dã sẽ hiện trong Pokédex.");
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
                    int an = anHien(lt[0], lt[1]);
                    if (an != 0) {
                        mh = 0x9AA4B0;
                    }
                    GiaoDien.khoi(g, xi + s / 2 - rd, y + 4 + hi / 2 - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 4, 5));
                    if (an != 0) {
                        PokeBody.paintChanDungBong(g, xi + s / 2, y + 4 + hi / 2, 2 * rd);
                    } else {
                        // không xin máy chủ 1.025 gói khi lướt Pokédex: chỉ chân dung jar / kho đã tải, thiếu thì bóng
                        boolean cu = KhoSprite.choXin;
                        KhoSprite.choXin = false;
                        try {
                            PokeBody.paintChanDung(g, lt[0], false, xi + s / 2, y + 4 + hi / 2, 2 * rd);
                        } finally {
                            KhoSprite.choXin = cu;
                        }
                    }
                    int co = lt[1] >= 0 ? 0 : BangLoai.coPokedex(lt[0]);
                    if ((co & BangLoai.DEX_BAT) != 0) {
                        // đã bắt: chấm đỏ góc trên (kiểu Poké Ball)
                        g.setColor(0xE03C3C);
                        g.fillArc(xi + s - 10, y + 3, 7, 7, 0, 360);
                    } else if (co != 0) {
                        g.setColor(0x9AA4B0);
                        g.fillArc(xi + s - 9, y + 4, 5, 5, 0, 360);
                    }
                    int bac = lt[1] >= 0 ? 0 : BangLoai.bac(lt[0]);
                    if (bac >= 3 && an != 2) {
                        g.setColor(BangLoai.mauBac(bac));
                        g.fillRect(xi + 4, y + 4, 5, 5);
                    }
                    String ten = an == 2 ? "Chưa xuất hiện" : an == 1 ? "???" : tenLoai(lt);
                    ChuDe.chu(g, ChuDe.cat(ten, ChuDe.NHAN, s - 6), xi + s / 2, y + 4 + hi, ChuDe.NHAN, an == 0 ? ChuDe.CHU : ChuDe.CHU_PHU, 2);
                    ChuDe.chu(g, soDex(lt[0]), xi + s / 2, y + 4 + hi + lh1, ChuDe.PHU, ChuDe.CHU_PHU, 2);
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
        int an = anHien(lt[0], lt[1]);
        int k = Bang.cach();
        int lw = Math.min(w * 45 / 100, 260);
        // 0.9.6: sân khấu sáng theo màu hệ (đĩa tròn dải nhạt) thay khối navy
        int mh = an != 0 ? 0x9AA4B0 : KieuO.mauHe(HePoke.he1(lt[0]));
        float z = Math.max(1.5f, Math.min(3.2f, Math.min(lw / 56f, h / 44f)));
        int cx = x + lw / 2, yChan = y + h - (int) (14 * z);
        int rd = Math.min(lw, h) / 2 - 4;
        GiaoDien.khoiDoc(g, cx - rd, y + h / 2 - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 5, 6), GiaoDien.pha(mh, 0xFFFFFF, 2, 4));
        ManNhanVat.veSanKhau(g, cx, yChan, z);
        if (PhongVe.co()) {
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            PhongVe.bo.bat(g, z);
            int xv = (int) ((cx + tx) / z), yv = (int) ((yChan + ty) / z);
            if (an != 0) {
                PokeBody.paintBong(g, xv, yv, NgoaiHinhVe.huongThe(Canvas.gameTick), Canvas.gameTick, 100);
            } else {
                PokeBody.paint(g, lt[0], false, xv, yv, NgoaiHinhVe.huongThe(Canvas.gameTick), PokeDuLieu.DUNG, Canvas.gameTick, 100);
            }
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
        } else if (an != 0) {
            PokeBody.paintBong(g, cx, yChan, 0, Canvas.gameTick, (int) (100 * z));
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
        String ten = an == 2 ? "Chưa xuất hiện" : an == 1 ? "???" : tenLoai(lt);
        ChuDe.chuGiua(g, ChuDe.cat(ten, ChuDe.TIEU_DE, wr - hd - 60), xr + hd + 10, y + 4, hd, ChuDe.TIEU_DE, ChuDe.CHU, 0);
        ChuDe.chuGiua(g, soDex(lt[0]), xr + wr - 10, y + 4, hd, ChuDe.SO, ChuDe.CHU_PHU, 1);
        int yy = y + 8 + hd;
        int xb = xr + 10;
        if (an == 0) {
            int h1 = HePoke.he1(lt[0]), h2 = HePoke.he2(lt[0]);
            xb += KieuO.vienHe(g, xb, yy, h1) + 6;
            if (h2 >= 0 && h2 != h1) {
                KieuO.vienHe(g, xb, yy, h2);
            }
            yy += 22;
        }
        int fh = Font.arialFont.getHeight() + 4;
        if (lt[1] >= 0) {
            int hp = Res.monsterTemplates[lt[1]].maxhp;
            if (hp > 0) {
                Font.arialFont.drawString(g, "HP gốc: " + hp, xr + 10, yy, 0);
                yy += fh;
            }
            Bang.doan(g, Font.arialFont, "Gặp ngoài bản đồ hoang dã. Chạm Pokémon hoang dã để chọn làm mục tiêu.", xr + 10, yy, wr - 20, false);
            return;
        }
        int bac = BangLoai.bac(lt[0]);
        if (bac > 0 && bac < BangLoai.TEN_BAC.length) {
            g.setColor(BangLoai.mauBac(bac));
            g.fillRect(xr + 10, yy + 3, 8, 8);
            Font.arialFont.drawString(g, "Bậc " + bac + " · " + BangLoai.TEN_BAC[bac], xr + 22, yy, 0);
            yy += fh;
        }
        int th = BangLoai.theHe(lt[0]);
        if (th > 0) {
            Font.arialFont.drawString(g, "Thế hệ " + th, xr + 10, yy, 0);
            yy += fh;
        }
        int co = BangLoai.coPokedex(lt[0]);
        String tt = (co & BangLoai.DEX_BAT) != 0 ? "Đã bắt" : (co & BangLoai.DEX_HA) != 0 ? "Đã hạ" : (co & BangLoai.DEX_GAP) != 0 ? "Đã gặp" : "Chưa gặp";
        Font.arialFont.drawString(g, tt, xr + 10, yy, 0);
        yy += fh;
        String mo = an == 2 ? "Chưa xuất hiện trong thế giới game: loài này sẽ mở ở đợt sau."
                : "Gặp ngoài bản đồ hoang dã theo môi trường sống và cấp quái. Loài hiếm có vòng màu dưới chân.";
        Bang.doan(g, Font.arialFont, mo, xr + 10, yy, wr - 20, false);
    }
}
