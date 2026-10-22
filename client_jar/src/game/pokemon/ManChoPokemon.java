package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * CHỢ POKÉMON (0.9.8, gói -121 kiểu 7 - real.ChoPokemonGame): tab "Pokémon" = tin đang bán (thẻ hình PNG, độ hiếm, sao
 * cá thể, cấp, giá + icon xu PNG), chạm = hộp xác nhận mua; tab "Của tôi" = tin mình đang bán (Huỷ: Pokémon về Hộp PC,
 * phí đăng không hoàn). Đăng bán từ Hộp PC: máy chủ gửi KHUNG GIÁ (sàn / trần theo bậc hiếm, sắc màu, sao, cấp) - thanh
 * trượt chỉ chọn trong khung, hiện phí đăng, thuế và số người bán nhận. Máy chủ kiểm lại mọi thứ.
 */
public final class ManChoPokemon extends ManBang {
    public static ManChoPokemon me;
    /** đang ở tấm đăng bán (khung giá đã nhận) */
    boolean dang;
    int gia;

    public static ManChoPokemon gI() {
        if (me == null) {
            me = new ManChoPokemon();
        }
        return me;
    }

    /** Mở chợ (tab 0) - xin danh sách. */
    public static void mo() {
        ManChoPokemon m = gI();
        m.dang = false;
        m.tab = 0;
        m.moBang();
        BatPokemon.cho(BatPokemon.CHO_XIN, 0, 0);
    }

    /** Nhận danh sách: mở nếu chưa mở (máy chủ trả lời yêu cầu của chính màn này / sau khi đăng). */
    static void moNeuChua(int tab) {
        ManChoPokemon m = gI();
        if (Canvas.currentScreen != m) {
            m.dang = false;
            m.moBang();
        }
        if (!m.dang) {
            m.tab = tab;
        }
    }

    /** Nhận khung giá: mở tấm đăng bán. */
    static void moDang() {
        ManChoPokemon m = gI();
        m.dang = true;
        m.gia = BatPokemon.khungSan + (BatPokemon.khungTran - BatPokemon.khungSan) / 4;
        if (Canvas.currentScreen != m) {
            m.moBang();
        }
    }

    /** Đăng bán con {@code idCon} (nút ở Hộp PC): xin khung giá. */
    public static void dangBan(int idCon) {
        BatPokemon.coKhung = false;
        BatPokemon.cho(BatPokemon.CHO_KHUNG, idCon, 0);
    }

    void sauKetQua() {
        if (this.dang) {
            this.dang = false;
            this.tab = 1;
        }
    }

    protected String tieuDe() {
        return this.dang ? "Đăng bán " + BatPokemon.khungTen : "Chợ Pokémon";
    }

    protected String[] tabs() {
        if (this.dang) {
            return null;
        }
        return new String[]{"Pokémon", "Của tôi (" + BatPokemon.choSoDang + "/" + BatPokemon.choToiDa + ")"};
    }

    protected void chonTab(int t) {
        super.chonTab(t);
        BatPokemon.cho(BatPokemon.CHO_XIN, t, 0);
    }

    protected void lui() {
        if (this.dang) {
            this.dang = false;
        } else {
            dong();
        }
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        if (this.dang) {
            veDang(g, x, y, w, h);
            return;
        }
        if (!BatPokemon.coCho) {
            Bang.trong(g, x, y, w, h, "Đang mở chợ...", null);
            return;
        }
        int k = Bang.cach();
        int yl = y + 2;
        String dau = this.tab == 0 ? "Thuế " + BatPokemon.choThue + "% người bán chịu (đốt) · hôm nay đã mua " + BatPokemon.choMuaHomNay + "/"
                + BatPokemon.choMuaToiDa : "Huỷ tin: Pokémon về Hộp PC, phí đăng không hoàn. Tin hết hạn sau 48 giờ.";
        yl = Bang.doan(g, ChuDe.PHU, dau, x + 4, yl, w - 8, false, ChuDe.CHU_PHU) + k;
        final Vector v = BatPokemon.CHO;
        if (v.size() == 0) {
            Bang.trong(g, x, yl, w, y + h - yl, this.tab == 0 ? "Chợ chưa có Pokémon nào" : "Bạn chưa bán Pokémon nào",
                    "Đăng bán từ Hộp PC: chọn một con, bấm Bán.");
            return;
        }
        final int o = Bang.coO() + 12;
        final int cot = Math.max(3, (w + k) / (o + k));
        int hang = (v.size() + cot - 1) / cot;
        final int lp = Math.max(ChuDe.cao(ChuDe.NHAN), 18);
        final int lhp = ChuDe.cao(ChuDe.PHU);
        final int rh = o + lhp + 16 + lp + lhp + 6;
        Bang.danhSach(g, this.ds, x, yl, w, y + h - 2 - yl, hang, rh, new Bang.Hang() {
            public void ve(Graphics g, int r, int x, int y, int w, int h) {
                int kk = Bang.cach();
                int s = (w - (cot - 1) * kk) / cot;
                for (int c = 0; c < cot; c++) {
                    final int i = r * cot + c;
                    if (i >= v.size()) {
                        break;
                    }
                    final BatPokemon.TinCho t = (BatPokemon.TinCho) v.elementAt(i);
                    int xi = x + c * (s + kk);
                    Bang.hang(g, xi, y, s, h, i, t.cuaToi, null, null);
                    ManHopPc.veThe(g, t.con, xi, y, s, h - lp - lhp - 6);
                    veGia(g, t, xi, y + h - lp - 2, s, lp);
                    Bang.dangKy(g, xi, y, s, h, new IAction() {
                        public void perform() {
                            ManChoPokemon.this.chon(t);
                        }
                    }, "tin-" + i);
                }
            }
        });
    }

    /** Dòng giá: icon xu PNG + số; dưới: độ hiếm (màu) bên trái. */
    static void veGia(Graphics g, BatPokemon.TinCho t, int x, int y, int w, int h) {
        String so = TienTe.so(t.gia);
        int is = Math.min(h, 16);
        int wt = is + 4 + ChuDe.rong(so, ChuDe.NHAN);
        int x0 = x + (w - wt) / 2;
        IconBat.xu(g, x0 + is / 2, y + h / 2, is);
        ChuDe.chuGiua(g, so, x0 + is + 4, y, h, ChuDe.NHAN, ChuDe.VANG_TOI, 0);
        int mau = t.con.doHiem >= 3 ? ChuDe.TIM : t.con.doHiem == 2 ? ChuDe.DO : t.con.doHiem == 1 ? ChuDe.XANH : ChuDe.CHU_PHU;
        ChuDe.chu(g, BatPokemon.tenDoHiem(t.con.doHiem) + (t.con.shiny ? " · sắc màu" : ""), x + w / 2, y - ChuDe.cao(ChuDe.PHU) - 1, ChuDe.PHU, mau, 2);
    }

    void chon(final BatPokemon.TinCho t) {
        if (t.cuaToi) {
            HopXacNhan.hoi("Huỷ tin bán", "Huỷ bán " + t.con.ten + " cấp " + t.con.cap + "? Pokémon về Hộp PC, phí đăng không hoàn.", "Huỷ tin",
                    new IAction() {
                public void perform() {
                    BatPokemon.cho(BatPokemon.CHO_HUY, t.id, 0);
                }
            }, null);
            return;
        }
        String mo = BatPokemon.tenDoHiem(t.con.doHiem) + (t.con.shiny ? ", sắc màu" : "") + ", cấp " + t.con.cap + ", cá thể " + t.con.ivPt + "% ("
                + t.con.sao + " sao). Người bán " + t.tenBan + ". Pokémon mua ở chợ chỉ chuyển đổi ra Kẹo, 72 giờ sau mới bán lại được.";
        HopXacNhan.hoiGia("Mua " + t.con.ten, mo, "Mua", TienTe.XU, t.gia, new IAction() {
            public void perform() {
                BatPokemon.cho(BatPokemon.CHO_MUA, t.id, 0);
            }
        });
    }

    void veDang(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        if (!BatPokemon.coKhung) {
            Bang.trong(g, x, y, w, h, "Đang tính khung giá...", null);
            return;
        }
        int lw = Math.min(w * 30 / 100, 200);
        int mh = KieuO.mauHe(HePoke.he1(BatPokemon.khungDex));
        int rd = Math.min(lw, h) / 2 - 6;
        int cy = y + h / 2;
        GiaoDien.khoi(g, x + lw / 2 - rd, cy - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 4, 5));
        PokeBody.paintChanDung(g, BatPokemon.khungDex, BatPokemon.khungShiny, x + lw / 2, cy, 2 * rd);
        if (BatPokemon.khungShiny) {
            IconBat.ve(g, IconBat.SAC_MAU, x + 14, y + 14, Math.min(Bang.co(18), 24));
        }
        int xr = x + lw + k, wr = w - lw - k;
        Bang.the(g, xr, y, wr, h);
        int xx = xr + 10, ww = wr - 20;
        int yy = y + 8;
        int lh = ChuDe.cao(ChuDe.PHU);
        int is = Math.max(lh, 16);
        ChuDe.chu(g, ChuDe.cat(BatPokemon.khungTen + " cấp " + BatPokemon.khungCap, ChuDe.TIEU_DE, ww - 3 * is - 10), xx, yy, ChuDe.TIEU_DE, ChuDe.CHU, 0);
        IconBat.sao(g, BatPokemon.khungSao, xx + ww - 3 * is - 4, yy + ChuDe.cao(ChuDe.TIEU_DE) / 2, is);
        yy += ChuDe.cao(ChuDe.TIEU_DE) + 6;
        int san = BatPokemon.khungSan, tran = BatPokemon.khungTran;
        yy = Bang.doan(g, ChuDe.PHU, "Khung giá máy chủ: " + TienTe.so(san) + " - " + TienTe.so(tran) + " xu (theo độ hiếm, sắc màu, sao, cấp).",
                xx, yy, ww, false, ChuDe.CHU_PHU) + 4;
        int hn = Bang.co(26);
        if (BatPokemon.khungLyDo.length() > 0) {
            Bang.doan(g, ChuDe.NHAN, BatPokemon.khungLyDo, xx, yy, ww, false, ChuDe.DO_TOI);
            Bang.nut(g, xx, y + h - hn - 8, ww, hn, "Quay lại", Bang.TRUNG, new IAction() {
                public void perform() {
                    ManChoPokemon.this.lui();
                }
            }, "dang-lui");
            return;
        }
        this.gia = Math.max(san, Math.min(tran, this.gia));
        final int buoc = Math.max(1, (tran - san) / 100);
        int wb = Math.max(hn, ChuDe.rong("−", ChuDe.NHAN) + 20);
        Bang.nut(g, xx, yy, wb, hn, "-", Bang.PHU, new IAction() {
            public void perform() {
                ManChoPokemon.this.gia = Math.max(BatPokemon.khungSan, ManChoPokemon.this.gia - buoc);
            }
        }, "gia-giam");
        this.gia = Bang.thanhTruot(g, xx + wb + k, yy, ww - 2 * (wb + k), hn, this.gia, san, tran, buoc, false, new IAction() {
            public void perform() {
                ManChoPokemon.this.gia = Bang.giaTriTha();
            }
        }, "gia-truot");
        Bang.nut(g, xx + ww - wb, yy, wb, hn, "+", Bang.PHU, new IAction() {
            public void perform() {
                ManChoPokemon.this.gia = Math.min(BatPokemon.khungTran, ManChoPokemon.this.gia + buoc);
            }
        }, "gia-tang");
        yy += hn + 6;
        long thue = (long) this.gia * BatPokemon.khungThue / 100;
        long phi = Math.max(1, (long) this.gia * BatPokemon.khungPhi / 100);
        IconBat.xu(g, xx + is / 2, yy + is / 2, is);
        ChuDe.chuGiua(g, "Giá " + TienTe.so(this.gia) + " xu", xx + is + 6, yy, is, ChuDe.NHAN, ChuDe.CHU, 0);
        yy += is + 4;
        Bang.doan(g, ChuDe.PHU, "Bán được bạn nhận " + TienTe.so(this.gia - thue) + " xu (thuế " + BatPokemon.khungThue + "%: " + TienTe.so(thue)
                + "). Phí đăng " + TienTe.so(phi) + " xu, không hoàn.", xx, yy, ww, false, ChuDe.CHU_PHU);
        int wn = (ww - k) / 2;
        Bang.nut(g, xx, y + h - hn - 8, wn, hn, "Thôi", Bang.TRUNG, new IAction() {
            public void perform() {
                ManChoPokemon.this.lui();
            }
        }, "dang-thoi");
        Bang.nut(g, xx + wn + k, y + h - hn - 8, wn, hn, ChuDe.cat("Đăng bán · phí " + TienTe.so(phi), ChuDe.NHAN, wn - 8), Bang.CHINH, new IAction() {
            public void perform() {
                BatPokemon.cho(BatPokemon.CHO_DANG, BatPokemon.khungId, ManChoPokemon.this.gia);
            }
        }, "dang-ban");
    }
}
