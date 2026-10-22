package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * HỘP PC (0.9.8, gói -121 kiểu 4 - real.BatPokemonGame.guiHop): lưới thẻ Pokémon đã thu phục với HÌNH THẬT (chân dung
 * PNG PokeBody), cấp, sao đánh giá cá thể (icon PNG IconBat), dấu sắc màu / khoá trao đổi. Chạm một con = thẻ chi tiết:
 * Pokémon to, Sức mạnh, 6 thanh chỉ số cá thể (0..31), tính cách, Kẹo loài, nút Đi theo / Luyện cấp / Tiến hoá (đúng
 * dòng tiến hoá thật) / Chuyển đổi (con trùng loài) / Thả. Chế độ {@link #chonDoi}: chọn tối đa 3 con cho cửa sổ trao
 * đổi (ManTraoDoiPokemon). Máy chủ quyết định mọi thứ, client chỉ hiện + gửi yêu cầu.
 */
public final class ManHopPc extends ManBang {
    public static ManHopPc me;
    /** id con đang xem chi tiết (-1 = lưới) - theo id, không theo vị trí (danh sách đổi sau mỗi hành động). */
    int chon = -1;
    int loc;
    /** Chế độ chọn Pokémon cho trao đổi. */
    public boolean chonDoi;
    Vector daChon = new Vector();

    public static ManHopPc gI() {
        if (me == null) {
            me = new ManHopPc();
        }
        return me;
    }

    /** Mở (chonDoi = chế độ chọn cho trao đổi). */
    public static void mo(boolean chonDoi) {
        ManHopPc m = gI();
        m.chonDoi = chonDoi;
        m.chon = -1;
        m.daChon.removeAllElements();
        if (chonDoi) {
            for (int i = 0; i < BatPokemon.tdToi.size(); i++) {
                m.daChon.addElement(new Integer(((BatPokemon.Con) BatPokemon.tdToi.elementAt(i)).id));
            }
        }
        m.moBang();
        if (!BatPokemon.coHop) {
            BatPokemon.xinHop();
        }
    }

    protected String tieuDe() {
        if (this.chonDoi) {
            return "Chọn Pokémon trao đổi (" + this.daChon.size() + "/3)";
        }
        return "Hộp PC " + BatPokemon.hopSoCon + "/" + BatPokemon.hopSucChua;
    }

    static final String[] CHIP = {"Tất cả", "Trùng loài", "Sắc màu", "3 sao"};

    Vector loc() {
        Vector all = BatPokemon.HOP;
        if (this.loc == 0) {
            return all;
        }
        Vector v = new Vector();
        for (int i = 0; i < all.size(); i++) {
            BatPokemon.Con c = (BatPokemon.Con) all.elementAt(i);
            if (this.loc == 1 ? c.trungLoai() : this.loc == 2 ? c.shiny : c.sao >= 3) {
                v.addElement(c);
            }
        }
        return v;
    }

    protected void lui() {
        if (this.chon >= 0) {
            this.chon = -1;
        } else if (this.chonDoi) {
            this.chonDoi = false;
            ManTraoDoiPokemon.mo();
        } else {
            dong();
        }
    }

    boolean daChon(int id) {
        for (int i = 0; i < this.daChon.size(); i++) {
            if (((Integer) this.daChon.elementAt(i)).intValue() == id) {
                return true;
            }
        }
        return false;
    }

    void doiChon(BatPokemon.Con c) {
        for (int i = 0; i < this.daChon.size(); i++) {
            if (((Integer) this.daChon.elementAt(i)).intValue() == c.id) {
                this.daChon.removeElementAt(i);
                return;
            }
        }
        if (c.dangTheo()) {
            ThongBao.hien(c.ten + " đang đi theo bạn: chọn Thôi đi theo trước.", ThongBao.LOI);
            return;
        }
        if (c.khoaDoi()) {
            ThongBao.hien(c.ten + " vừa nhận qua trao đổi, chưa trao đổi tiếp được.", ThongBao.LOI);
            return;
        }
        if (this.daChon.size() >= 3) {
            ThongBao.hien("Mỗi lần tối đa 3 Pokémon.", ThongBao.LOI);
            return;
        }
        this.daChon.addElement(new Integer(c.id));
    }

    void guiChon() {
        int[] ids = new int[this.daChon.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ((Integer) this.daChon.elementAt(i)).intValue();
        }
        BatPokemon.traoDoi(BatPokemon.T_DAT, ids);
        this.chonDoi = false;
        ManTraoDoiPokemon.mo();
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        if (!BatPokemon.coHop) {
            Bang.trong(g, x, y, w, h, "Đang mở Hộp PC...", null);
            return;
        }
        final Vector v = loc();
        if (this.chon >= 0 && !this.chonDoi) {
            BatPokemon.Con cc = BatPokemon.conHop(this.chon);
            if (cc != null) {
                veChiTiet(g, x, y, w, h, cc);
                return;
            }
        }
        this.chon = -1;
        int k = Bang.cach();
        Object[] vl = new Object[CHIP.length];
        for (int i = 0; i < CHIP.length; i++) {
            final int li = i;
            vl[i] = new IAction() {
                public void perform() {
                    ManHopPc.this.loc = li;
                    ManHopPc.this.ds.den(0);
                }
            };
        }
        int yl = y + 2;
        yl += Bang.hangChip(g, x, yl, w, CHIP, this.loc, vl) + k;
        int hDuoi = 0;
        if (this.chonDoi) {
            hDuoi = Bang.co(28) + k;
            Bang.nut(g, x, y + h - hDuoi + k, w, hDuoi - k, this.daChon.size() == 0 ? "Chưa chọn Pokémon nào" : "Đưa vào trao đổi (" + this.daChon.size() + ")",
                    this.daChon.size() == 0 ? Bang.TAT : Bang.CHINH, new IAction() {
                public void perform() {
                    if (ManHopPc.this.daChon.size() > 0) {
                        ManHopPc.this.guiChon();
                    }
                }
            }, "dua-trao-doi");
        } else {
            int hb = Math.max(ChuDe.cao(ChuDe.PHU) + 4, Bang.co(22));
            int wb = Math.max(Bang.co(70), ChuDe.rong("Chợ Pokémon", ChuDe.NHAN) + 24);
            ChuDe.chuGiua(g, ChuDe.cat("Giữ tối đa " + BatPokemon.hopGiu + " con thường mỗi loài, con thừa tự chuyển thành Kẹo.", ChuDe.PHU,
                    w - wb - 12), x + 4, yl, hb, ChuDe.PHU, ChuDe.CHU_PHU, 0);
            Bang.nut(g, x + w - wb, yl, wb, hb, "Chợ Pokémon", Bang.PHU, new IAction() {
                public void perform() {
                    ManChoPokemon.mo();
                }
            }, "mo-cho");
            yl += hb + k;
        }
        if (v.size() == 0) {
            Bang.trong(g, x, yl, w, y + h - hDuoi - yl, this.loc == 0 ? "Hộp PC đang trống" : "Không có Pokémon nào ở mục này",
                    "Làm Pokémon hoang dã yếu đi rồi ném Poké Ball (mua ở Y tá Chansey).");
            return;
        }
        final int o = Bang.coO() + 12;
        final int cot = Math.max(3, (w + k) / (o + k));
        int hang = (v.size() + cot - 1) / cot;
        final int rh = o + ChuDe.cao(ChuDe.PHU) + 16;
        Bang.danhSach(g, this.ds, x, yl, w, y + h - hDuoi - 2 - yl, hang, rh, new Bang.Hang() {
            public void ve(Graphics g, int r, int x, int y, int w, int h) {
                int kk = Bang.cach();
                int s = (w - (cot - 1) * kk) / cot;
                for (int c = 0; c < cot; c++) {
                    final int i = r * cot + c;
                    if (i >= v.size()) {
                        break;
                    }
                    final BatPokemon.Con con = (BatPokemon.Con) v.elementAt(i);
                    int xi = x + c * (s + kk);
                    boolean chonDoi = ManHopPc.this.chonDoi;
                    boolean sang = chonDoi && ManHopPc.this.daChon(con.id);
                    Bang.hang(g, xi, y, s, h, i, sang, null, null);
                    veThe(g, con, xi, y, s, h);
                    Bang.dangKy(g, xi, y, s, h, new IAction() {
                        public void perform() {
                            if (ManHopPc.this.chonDoi) {
                                ManHopPc.this.doiChon(con);
                            } else {
                                ManHopPc.this.chon = con.id;
                            }
                        }
                    }, "con-" + i);
                }
            }
        });
    }

    /** Một thẻ: chân dung PNG trên đĩa màu hệ, tên, cấp, sao; góc: sắc màu / khoá / đi theo. */
    static void veThe(Graphics g, BatPokemon.Con con, int x, int y, int s, int h) {
        int lh1 = ChuDe.cao(ChuDe.NHAN), lh2 = ChuDe.cao(ChuDe.PHU);
        int is = Math.max(12, Math.min(18, s / 6));
        int hi = h - lh1 - lh2 - is - 10;
        int rd = Math.max(8, Math.min(s, hi) / 2 - 2);
        int mh = KieuO.mauHe(HePoke.he1(con.dex));
        int cy = y + 4 + hi / 2;
        GiaoDien.khoi(g, x + s / 2 - rd, cy - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 4, 5));
        PokeBody.paintChanDung(g, con.dex, con.shiny, x + s / 2, cy, 2 * rd);
        int yt = y + 4 + hi;
        ChuDe.chu(g, ChuDe.cat(con.ten, ChuDe.NHAN, s - 6), x + s / 2, yt, ChuDe.NHAN, ChuDe.CHU, 2);
        ChuDe.chu(g, "cấp " + con.cap + " · " + con.ivPt + "%", x + s / 2, yt + lh1, ChuDe.PHU, ChuDe.CHU_PHU, 2);
        int ws = 3 * is + 2 * Math.max(1, is / 8);
        IconBat.sao(g, con.sao, x + (s - ws) / 2, yt + lh1 + lh2 + is / 2 + 2, is);
        int ic = Math.max(12, is);
        if (con.shiny) {
            IconBat.ve(g, IconBat.SAC_MAU, x + ic / 2 + 3, y + ic / 2 + 3, ic);
        }
        if (con.khoaDoi()) {
            IconBat.ve(g, IconBat.KHOA, x + s - ic / 2 - 3, y + ic / 2 + 3, ic);
        } else if (con.dangTheo()) {
            IconBat.ve(g, IconBat.POKE, x + s - ic / 2 - 3, y + ic / 2 + 3, ic);
        }
    }

    void veChiTiet(Graphics g, int x, int y, int w, int h, final BatPokemon.Con c) {
        int k = Bang.cach();
        // hàng nút dưới cùng (cả bề ngang): Đi theo / Luyện / Tiến hoá / Chuyển đổi / Thả
        int hn = Bang.co(26);
        int nb = 6;
        int wn = (w - (nb - 1) * k) / nb;
        int yb = y + h - hn;
        String[] chu = new String[nb];
        int[] kieu = new int[nb];
        final int[] hd2 = {BatPokemon.H_THEO, BatPokemon.H_LUYEN, BatPokemon.H_TIEN_HOA, BatPokemon.H_CHUYEN, BatPokemon.H_THA, H_BAN};
        chu[0] = c.dangTheo() ? "Thôi theo" : "Đi theo";
        kieu[0] = Bang.PHU;
        chu[1] = c.keoLuyen <= 0 ? "Cấp tối đa" : "Luyện " + c.keoLuyen + " kẹo";
        kieu[1] = c.keoLuyen <= 0 ? Bang.TAT : Bang.CHINH;
        chu[2] = c.dexSau <= 0 ? "Hết tiến hoá" : (c.cap < c.capTienHoa ? "Tiến hoá cấp " + c.capTienHoa : "Tiến hoá " + c.keoTienHoa + " kẹo");
        kieu[2] = c.dexSau <= 0 || c.cap < c.capTienHoa ? Bang.TAT : Bang.CHINH;
        chu[3] = "Chuyển đổi";
        kieu[3] = c.trungLoai() && !c.dangTheo() ? Bang.PHU : Bang.TAT;
        chu[4] = "Thả";
        kieu[4] = c.dangTheo() ? Bang.TAT : Bang.NGUY;
        chu[5] = "Bán";
        kieu[5] = c.dangTheo() || c.khoaDoi() ? Bang.TAT : Bang.PHU;
        for (int i = 0; i < nb; i++) {
            final int hi = hd2[i];
            Bang.nut(g, x + i * (wn + k), yb, wn, hn, ChuDe.cat(chu[i], ChuDe.NHAN, wn - 8), kieu[i], new IAction() {
                public void perform() {
                    ManHopPc.this.lam(c, hi);
                }
            }, "hd-" + i);
        }
        h -= hn + k;
        int lw = Math.min(w * 36 / 100, 240);
        int mh = KieuO.mauHe(HePoke.he1(c.dex));
        float z = Math.max(1.2f, Math.min(3.2f, Math.min(lw / 56f, h / 44f)));
        int cx = x + lw / 2, yChan = y + h - (int) (12 * z);
        int rd = Math.min(lw, h) / 2 - 4;
        GiaoDien.khoiDoc(g, cx - rd, y + h / 2 - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 5, 6), GiaoDien.pha(mh, 0xFFFFFF, 2, 4));
        ManNhanVat.veSanKhau(g, cx, yChan, z);
        PokeBody.paint(g, c.dex, c.shiny, cx, yChan, 0, PokeDuLieu.DUNG, Canvas.gameTick, (int) (100 * z));
        if (c.shiny) {
            IconBat.ve(g, IconBat.SAC_MAU, x + 14, y + 14, Math.min(Bang.co(20), 28));
        }
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, xr + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManHopPc.this.chon = -1;
            }
        });
        String ten = c.ten + (c.shiny ? " (sắc màu)" : "");
        ChuDe.chuGiua(g, ChuDe.cat(ten, ChuDe.TIEU_DE, wr - hd - 70), xr + hd + 10, y + 4, hd, ChuDe.TIEU_DE, ChuDe.CHU, 0);
        ChuDe.chuGiua(g, "cấp " + c.cap, xr + wr - 10, y + 4, hd, ChuDe.SO, ChuDe.CHU_PHU, 1);
        int yMax = y + h - 4;
        int yy = y + 6 + hd;
        int xx = xr + 10, ww = wr - 20;
        int lh = ChuDe.cao(ChuDe.PHU);
        int is = Math.max(lh, Math.min(Bang.co(16), 24));
        int wSao = IconBat.sao(g, c.sao, xx, yy + is / 2, is);
        ChuDe.chuGiua(g, ChuDe.cat("Cá thể " + c.ivPt + "% · Sức mạnh " + c.sucManh, ChuDe.NHAN, ww - wSao - 8), xx + wSao + 8, yy, is, ChuDe.NHAN, ChuDe.CHU, 0);
        yy += is + 3;
        ChuDe.chu(g, ChuDe.cat("Tính cách: " + c.chuTinhCach, ChuDe.PHU, ww), xx, yy, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        yy += lh + 3;
        // Kẹo loài (một dòng, icon PNG)
        if (yy + is <= yMax) {
            IconBat.ve(g, IconBat.KEO, xx + is / 2, yy + is / 2, is);
            String keo = "Kẹo " + c.tenKeo + ": " + c.keo + (c.tienHoaKhiDoi() && c.dexSau > 0 ? " · tiến hoá khi được trao đổi" : "");
            ChuDe.chuGiua(g, ChuDe.cat(keo, ChuDe.PHU, ww - is - 6), xx + is + 6, yy, is, ChuDe.PHU, ChuDe.CHU, 0);
            yy += is + 3;
        }
        // 6 chỉ số cá thể (0..31): 2 cột khi đủ rộng, hết chỗ thì dừng
        int hb = lh;
        int cot = ww >= 280 ? 2 : 1;
        int wc = (ww - (cot - 1) * 10) / cot;
        int wNhan = Math.min(wc / 2, ChuDe.rong("Phòng thủ ĐB", ChuDe.PHU) + 6);
        for (int i = 0; i < 6; i++) {
            int cxi = xx + (i % cot) * (wc + 10);
            int cyi = yy + (i / cot) * (hb + 2);
            if (cyi + hb > yMax) {
                break;
            }
            ChuDe.chuGiua(g, BatPokemon.TEN_CHI_SO[i], cxi, cyi, hb, ChuDe.PHU, ChuDe.CHU_PHU, 0);
            Bang.thanh(g, cxi + wNhan, cyi + 2, wc - wNhan - 22, Math.max(4, hb - 4), c.iv[i], 31, c.iv[i] >= 31 ? 2 : 1, null);
            ChuDe.chuGiua(g, String.valueOf(c.iv[i]), cxi + wc, cyi, hb, ChuDe.SO, ChuDe.CHU, 1);
        }
    }

    /** Nút "Bán" (chợ Pokémon) - không phải hành động máy chủ kiểu 4. */
    static final int H_BAN = 99;

    void lam(final BatPokemon.Con c, final int hd) {
        if (hd == H_BAN) {
            if (!c.dangTheo() && !c.khoaDoi()) {
                ManChoPokemon.dangBan(c.id);
            }
            return;
        }
        if (hd == BatPokemon.H_THA) {
            if (c.dangTheo()) {
                return;
            }
            HopXacNhan.hoiNguyHiem("Thả " + c.ten, "Thả " + c.ten + " cấp " + c.cap + " (" + c.ivPt + "%) về tự nhiên? Nhận 1 Kẹo " + c.tenKeo + ".",
                    "Thả", new IAction() {
                public void perform() {
                    BatPokemon.hanhDong(hd, c.id);
                    ManHopPc.this.chon = -1;
                }
            });
            return;
        }
        if (hd == BatPokemon.H_CHUYEN) {
            if (!c.trungLoai() || c.dangTheo()) {
                return;
            }
            HopXacNhan.hoiNguyHiem("Chuyển đổi " + c.ten, "Chuyển đổi " + c.ten + " cấp " + c.cap + " (" + c.ivPt + "%): " + c.chuChuyen + ".",
                    "Chuyển đổi", new IAction() {
                public void perform() {
                    BatPokemon.hanhDong(hd, c.id);
                    ManHopPc.this.chon = -1;
                }
            });
            return;
        }
        if (hd == BatPokemon.H_LUYEN && c.keoLuyen <= 0 || hd == BatPokemon.H_TIEN_HOA && (c.dexSau <= 0 || c.cap < c.capTienHoa)) {
            return;
        }
        BatPokemon.hanhDong(hd, c.id);
    }
}
