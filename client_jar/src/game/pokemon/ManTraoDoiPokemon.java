package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * TRAO ĐỔI POKÉMON (0.9.8, gói -121 kiểu 6 - real.TraoDoiPokemonGame): hai cột "Bạn đưa" / "&lt;tên&gt; đưa", mỗi cột 3 ô
 * thẻ Pokémon (hình PNG, cấp, sao). Bước: chọn (mở Hộp PC chế độ chọn) -&gt; "Khoá" (xác nhận 1) -&gt; cả hai đã khoá -&gt;
 * "Trao đổi" (xác nhận 2). Đổi đề nghị thì cả hai mở khoá. Phí xu hiện trước khi bấm. Máy chủ đổi nguyên tử; rời game /
 * huỷ bất kỳ lúc nào = không đổi gì.
 */
public final class ManTraoDoiPokemon extends ManBang {
    public static ManTraoDoiPokemon me;

    public static ManTraoDoiPokemon gI() {
        if (me == null) {
            me = new ManTraoDoiPokemon();
        }
        return me;
    }

    public static void mo() {
        ManTraoDoiPokemon m = gI();
        if (Canvas.currentScreen != m && (ManHopPc.me == null || Canvas.currentScreen != ManHopPc.me || !ManHopPc.me.chonDoi)) {
            m.moBang();
        }
    }

    public void dongMan() {
        Canvas.gameScr.switchToMe();
    }

    protected String tieuDe() {
        return BatPokemon.tdTrangThai == 3 ? "Trao đổi xong" : "Trao đổi Pokémon với " + BatPokemon.tdDoiTen;
    }

    protected int rongMuon() {
        return ChuDe.dt() ? 640 : 560;
    }

    public void dong() {
        if (BatPokemon.tdTrangThai == 2) {
            lui();
            return;
        }
        super.dong();
    }

    protected void lui() {
        if (BatPokemon.tdTrangThai != 2) {
            BatPokemon.tdTrangThai = 0;
            super.dong();
            return;
        }
        HopXacNhan.hoi("Huỷ trao đổi", "Dừng trao đổi Pokémon với " + BatPokemon.tdDoiTen + "? Không bên nào mất gì.", "Huỷ trao đổi", new IAction() {
            public void perform() {
                BatPokemon.traoDoi(BatPokemon.T_HUY, null);
            }
        }, null);
    }

    static String chuBuoc(int b) {
        return b >= 2 ? "Đã xác nhận" : b == 1 ? "Đã khoá" : "Đang chọn";
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        if (BatPokemon.tdTrangThai == 3) {
            veXong(g, x, y, w, h);
            return;
        }
        int hn = Bang.co(28);
        int hDuoi = hn + k + ChuDe.cao(ChuDe.PHU) * 2 + 6;
        boolean doc = w < 420;
        int wc = doc ? w : (w - k) / 2;
        int hc = doc ? (h - hDuoi - k) / 2 : h - hDuoi;
        veCot(g, x, y, wc, hc, "Bạn đưa", BatPokemon.tdToi, BatPokemon.tdBuocToi, true);
        veCot(g, doc ? x : x + wc + k, doc ? y + hc + k : y, wc, hc, BatPokemon.tdDoiTen + " đưa", BatPokemon.tdDoi, BatPokemon.tdBuocDoi, false);
        int yy = y + h - hDuoi + 2;
        IconBat.ve(g, IconBat.TRAO_DOI, x + 10, yy + ChuDe.cao(ChuDe.PHU) / 2, ChuDe.cao(ChuDe.PHU));
        String phi = BatPokemon.tdPhi > 0 ? "Phí trao đổi của bạn: " + TienTe.so(BatPokemon.tdPhi) + " xu (trả khi xong)." : "Chưa có phí (chưa có Pokémon để nhận).";
        ChuDe.chu(g, ChuDe.cat(phi, ChuDe.PHU, w - 26), x + 22, yy, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        yy += ChuDe.cao(ChuDe.PHU) + 2;
        String ghi = BatPokemon.tdGhiChu.length() > 0 ? BatPokemon.tdGhiChu : "Hai bên khoá rồi cùng bấm Trao đổi. Đổi lựa chọn thì cả hai mở khoá.";
        ChuDe.chu(g, ChuDe.cat(ghi, ChuDe.PHU, w - 8), x + 4, yy, ChuDe.PHU, BatPokemon.tdGhiChu.length() > 0 ? ChuDe.DO_TOI : ChuDe.CHU_PHU, 0);
        int yb = y + h - hn;
        int wb = (w - 2 * k) / 3;
        Bang.nut(g, x, yb, wb, hn, "Huỷ", Bang.TRUNG, new IAction() {
            public void perform() {
                ManTraoDoiPokemon.this.lui();
            }
        }, "td-huy");
        boolean coChon = BatPokemon.tdToi.size() > 0;
        if (BatPokemon.tdBuocToi == 0) {
            Bang.nut(g, x + wb + k, yb, wb, hn, coChon ? "Đổi Pokémon" : "Chọn Pokémon", Bang.PHU, new IAction() {
                public void perform() {
                    ManHopPc.mo(true);
                }
            }, "td-chon");
            Bang.nut(g, x + 2 * (wb + k), yb, wb, hn, "Khoá", coChon ? Bang.CHINH : Bang.TAT, new IAction() {
                public void perform() {
                    if (BatPokemon.tdToi.size() > 0) {
                        BatPokemon.traoDoi(BatPokemon.T_KHOA, null);
                    }
                }
            }, "td-khoa");
        } else {
            Bang.nut(g, x + wb + k, yb, wb, hn, "Đổi Pokémon", Bang.PHU, new IAction() {
                public void perform() {
                    ManHopPc.mo(true);
                }
            }, "td-chon");
            boolean duoc = BatPokemon.tdBuocToi == 1 && BatPokemon.tdBuocDoi >= 1;
            String chu = BatPokemon.tdBuocToi >= 2 ? "Chờ " + BatPokemon.tdDoiTen : BatPokemon.tdBuocDoi < 1 ? "Chờ bên kia khoá" : "Trao đổi";
            Bang.nut(g, x + 2 * (wb + k), yb, wb, hn, ChuDe.cat(chu, ChuDe.NHAN, wb - 8), duoc ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
                public void perform() {
                    if (BatPokemon.tdBuocToi == 1 && BatPokemon.tdBuocDoi >= 1) {
                        HopXacNhan.hoi("Xác nhận trao đổi", "Trao đổi " + tom(BatPokemon.tdToi) + " lấy " + tom(BatPokemon.tdDoi)
                                + (BatPokemon.tdPhi > 0 ? ". Phí " + TienTe.so(BatPokemon.tdPhi) + " xu." : "."), "Trao đổi", new IAction() {
                            public void perform() {
                                BatPokemon.traoDoi(BatPokemon.T_XAC_NHAN, null);
                            }
                        }, null);
                    }
                }
            }, "td-xac-nhan");
        }
    }

    static String tom(Vector v) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < v.size(); i++) {
            BatPokemon.Con c = (BatPokemon.Con) v.elementAt(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(c.ten).append(" cấp ").append(c.cap);
        }
        return sb.length() == 0 ? "không có gì" : sb.toString();
    }

    void veCot(Graphics g, int x, int y, int w, int h, String tieuDe, Vector v, int buoc, boolean cuaToi) {
        Bang.the(g, x, y, w, h);
        int hd = ChuDe.cao(ChuDe.NHAN) + 6;
        ChuDe.chuGiua(g, ChuDe.cat(tieuDe, ChuDe.NHAN, w - 110), x + 8, y + 2, hd, ChuDe.NHAN, ChuDe.CHU, 0);
        int is = ChuDe.cao(ChuDe.PHU);
        if (buoc >= 1) {
            IconBat.ve(g, IconBat.KHOA, x + w - 8 - is / 2 - ChuDe.rong(chuBuoc(buoc), ChuDe.PHU) - 6, y + 2 + hd / 2, is);
        }
        ChuDe.chuGiua(g, chuBuoc(buoc), x + w - 8, y + 2, hd, ChuDe.PHU, buoc >= 1 ? ChuDe.CHINH_TOI : ChuDe.CHU_PHU, 1);
        int k = Bang.cach();
        int yo = y + hd + 4;
        int ho = h - hd - 8;
        int s = Math.min((w - 16 - 2 * k) / 3, ho);
        int x0 = x + (w - (3 * s + 2 * k)) / 2;
        for (int i = 0; i < 3; i++) {
            int xi = x0 + i * (s + k);
            if (i < v.size()) {
                BatPokemon.Con c = (BatPokemon.Con) v.elementAt(i);
                Bang.hang(g, xi, yo, s, ho, i, false, null, null);
                ManHopPc.veThe(g, c, xi, yo, s, ho);
                if (c.tienHoaKhiDoi()) {
                    int ic = Math.max(12, s / 6);
                    IconBat.ve(g, IconBat.TIEN_HOA, xi + s - ic / 2 - 3, yo + ho - ic / 2 - 3, ic);
                }
            } else {
                GiaoDien.khoi(g, xi, yo, s, ho, ChuDe.BO_O, ChuDe.NEN_PHU);
                IconBat.ve(g, IconBat.POKE, xi + s / 2, yo + ho / 2, Math.max(12, Math.min(32, s / 3)));
                if (cuaToi && BatPokemon.tdBuocToi == 0) {
                    Bang.dangKy(g, xi, yo, s, ho, new IAction() {
                        public void perform() {
                            ManHopPc.mo(true);
                        }
                    }, "td-o-" + i);
                }
            }
        }
    }

    void veXong(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int hn = Bang.co(28);
        int yy = Bang.doan(g, ChuDe.NHAN, BatPokemon.tdXong, x + 6, y + 4, w - 12, true, ChuDe.CHU) + k;
        Vector v = BatPokemon.tdNhan;
        int ho = Math.max(40, y + h - hn - k - yy);
        int s = Math.min((w - 2 * k) / 3, ho);
        int x0 = x + (w - (v.size() * s + (v.size() - 1) * k)) / 2;
        for (int i = 0; i < v.size(); i++) {
            int xi = x0 + i * (s + k);
            Bang.hang(g, xi, yy, s, ho, i, true, null, null);
            ManHopPc.veThe(g, (BatPokemon.Con) v.elementAt(i), xi, yy, s, ho);
        }
        int wb = (w - k) / 2;
        Bang.nut(g, x, y + h - hn, wb, hn, "Đóng", Bang.TRUNG, new IAction() {
            public void perform() {
                BatPokemon.tdTrangThai = 0;
                ManTraoDoiPokemon.this.dongMan();
            }
        }, "td-dong");
        Bang.nut(g, x + wb + k, y + h - hn, wb, hn, "Mở Hộp PC", Bang.CHINH, new IAction() {
            public void perform() {
                BatPokemon.tdTrangThai = 0;
                ManHopPc.mo(false);
            }
        }, "td-hop");
    }
}
