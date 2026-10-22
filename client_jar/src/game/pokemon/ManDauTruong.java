package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * ĐẤU TRƯỜNG BẢO BỐI (gói -109, {@link DauTruong}): tab "Thách đấu" (thẻ mùa: điểm, bậc, hạng, lượt hôm nay, lượng khoá
 * tuần; 3 đối thủ máy chủ ghép theo điểm + sức mạnh đội; nút Đấu) và tab "Đội hình" (2 hàng 3 ô: đội tấn công / phòng
 * thủ, lưới Pokémon Hộp PC có chân dung PNG, sao cá thể, sức mạnh cấp 50; chạm ô rồi chạm Pokémon để xếp). Máy chủ
 * kiểm mọi thứ (trùng loài, Huyền thoại, còn trong Hộp PC); client chỉ hiện + gửi yêu cầu.
 */
public final class ManDauTruong extends ManBang {
    public static ManDauTruong me;
    /** Đội đang xếp (id Hộp PC, 0 = trống): [0] tấn công, [1] phòng thủ. */
    final int[][] nhap = new int[2][3];
    /** Ô đang chọn: hàng 0 / 1, cột 0..2. */
    int oHang, oCot;
    boolean daNap;

    public static ManDauTruong gI() {
        if (me == null) {
            me = new ManDauTruong();
        }
        return me;
    }

    /** Mở từ máy chủ (gói trạng thái có cờ mở): giữ tab đang xem nếu màn đã mở. */
    public static void moTuMayChu() {
        ManDauTruong m = gI();
        m.napDoi();
        if (Canvas.currentScreen != m) {
            if (ManTranDauTruong.me != null && Canvas.currentScreen == ManTranDauTruong.me && !DauTruong.coKetQua) {
                return;   // đang trong trận: chỉ cập nhật dữ liệu
            }
            m.moBang();
            m.tab = DauTruong.cong.length == 3 ? 0 : 1;
        }
    }

    /** Chọn tab 0 Thách đấu / 1 Đội hình (lối vào từ ngoài màn, kiểm thử). */
    public void moTab(int t) {
        chonTab(Math.max(0, Math.min(3, t)));
    }

    /** Chép đội đã lưu trên máy chủ vào ô đang xếp. */
    void napDoi() {
        int[][] goc = {DauTruong.cong, DauTruong.thu};
        for (int h = 0; h < 2; h++) {
            for (int c = 0; c < 3; c++) {
                nhap[h][c] = goc[h] != null && c < goc[h].length ? goc[h][c] : 0;
            }
        }
        daNap = true;
    }

    protected String tieuDe() {
        return "Đấu trường Bảo Bối";
    }

    protected String[] tabs() {
        return new String[]{"Thách đấu", "Đội hình", "Lịch sử", "Tuần"};
    }

    protected int rongMuon() {
        return ChuDe.dt() ? 700 : 600;
    }

    protected void chonTab(int t) {
        super.chonTab(t);
        if (t == 0 && !DauTruong.coDoiThu && !DauTruong.choDoiThu && DauTruong.cong.length == 3) {
            DauTruong.tim(false);
        }
        if (t == 3 && !DauTruong.choTuan) {
            DauTruong.xinTuan();
        }
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        if (!DauTruong.coTrangThai) {
            Bang.trong(g, x, y, w, h, "Đang mở Đấu trường...", null);
            return;
        }
        if (!daNap) {
            napDoi();
        }
        if (this.tab == 0) {
            veThachDau(g, x, y, w, h);
        } else if (this.tab == 1) {
            veDoiHinh(g, x, y, w, h);
        } else if (this.tab == 2) {
            veLichSu(g, x, y, w, h);
        } else {
            veTuan(g, x, y, w, h);
        }
    }

    // ---------------------------------------------------------------- tab Thách đấu

    void veThachDau(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int lh = ChuDe.cao(ChuDe.NHAN), lp = ChuDe.cao(ChuDe.PHU);
        int ht = lh + 2 * lp + 16;
        Bang.the(g, x, y + 2, w, ht);
        int xx = x + 10, ww = w - 20, yy = y + 8;
        String hang = DauTruong.hang > 0 ? "Hạng " + DauTruong.hang : "Chưa xếp hạng";
        int wh = ChuDe.rong(hang, ChuDe.NHAN);
        ChuDe.chu(g, ChuDe.cat("Mùa " + DauTruong.mua + " · còn " + DauTruong.ngayConLai + " ngày", ChuDe.NHAN, ww - wh - 12), xx, yy, ChuDe.NHAN, ChuDe.CHU, 0);
        ChuDe.chu(g, hang, xx + ww, yy, ChuDe.NHAN, ChuDe.CHINH_TOI, 1);
        yy += lh + 2;
        ChuDe.chu(g, ChuDe.cat("Điểm " + DauTruong.diem + " (" + DauTruong.tenBac + ") · " + DauTruong.thang + " thắng / " + DauTruong.thua + " thua",
                ChuDe.PHU, ww), xx, yy, ChuDe.PHU, ChuDe.CHU, 0);
        yy += lp + 2;
        ChuDe.chu(g, ChuDe.cat("Lượt hôm nay " + DauTruong.luotCon + "/" + DauTruong.luotNgay + " · Lượng khoá tuần " + DauTruong.luongTuan + "/"
                + DauTruong.tranLuongTuan + (DauTruong.chuoiThang > 1 ? " · Chuỗi thắng " + DauTruong.chuoiThang : ""), ChuDe.PHU, ww), xx, yy,
                ChuDe.PHU, DauTruong.chuoiThang > 1 ? ChuDe.CHINH_TOI : ChuDe.CHU_PHU, 0);
        int yl = y + 2 + ht + k;
        if (DauTruong.cong.length != 3) {
            Bang.trong(g, x, yl, w, y + h - yl - Bang.co(30) - k, "Chưa có đội tấn công",
                    "Xếp 3 Pokémon từ Hộp PC ở tab Đội hình: cấp chuẩn hoá 50, chỉ số cá thể và tính cách có tác dụng.");
            Bang.nut(g, x + w / 4, y + h - Bang.co(30), w / 2, Bang.co(30), "Xếp đội hình", Bang.CHINH, new IAction() {
                public void perform() {
                    ManDauTruong.this.chonTab(1);
                }
            }, "dt-xep-doi");
            return;
        }
        int hb = Bang.co(28);
        int wb = (w - 2 * k) / 3;
        // cài đặt (lưu máy): mặc định máy tự đánh; bật = tự chọn chiêu mỗi lượt
        Bang.nut(g, x + 2 * (wb + k), yl, w - 2 * (wb + k), hb, DauTruong.tuChon() ? "Tự chọn chiêu: Bật" : "Tự chọn chiêu: Tắt",
                DauTruong.tuChon() ? Bang.XAC_NHAN : Bang.GO, new IAction() {
            public void perform() {
                DauTruong.datTuChon(!DauTruong.tuChon());
            }
        }, "dt-tu-chon");
        Bang.nut(g, x, yl, wb, hb, DauTruong.choDoiThu ? "Đang tìm..." : "Tìm đối thủ", DauTruong.choDoiThu ? Bang.TAT : Bang.CHINH, new IAction() {
            public void perform() {
                DauTruong.tim(false);
            }
        }, "dt-tim");
        Bang.nut(g, x + wb + k, yl, wb, hb, "Làm mới (" + DauTruong.lamMoiCon + ")", DauTruong.lamMoiCon > 0 && !DauTruong.choDoiThu ? Bang.PHU : Bang.TAT,
                new IAction() {
                    public void perform() {
                        DauTruong.tim(true);
                    }
                }, "dt-lam-moi");
        yl += hb + k;
        final Vector v = DauTruong.DOI_THU;
        if (v.size() == 0) {
            String phu = DauTruong.ghiDoiThu.length() > 0 ? DauTruong.ghiDoiThu
                    : "Máy chủ ghép 3 đối thủ gần điểm và sức mạnh đội của bạn. Không gặp tài khoản cùng thiết bị / mạng.";
            Bang.trong(g, x, yl, w, y + h - yl, DauTruong.choDoiThu ? "Đang tìm đối thủ..." : "Chưa có đối thủ", phu);
            return;
        }
        final int rh = Math.max(Bang.caoHang() + 8, lh + lp + 16);
        Bang.danhSach(g, this.ds, x, yl, w, y + h - yl, v.size(), rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final DauTruong.DoiThu d = (DauTruong.DoiThu) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, false, null, null);
                int s = h - 8;
                for (int c = 0; c < 3; c++) {
                    PokeBody.paintChanDung(g, d.dex[c], d.shiny[c], x + 6 + s / 2 + c * (s + 2), y + h / 2, s);
                }
                int xt = x + 10 + 3 * (s + 2);
                int wn = Math.max(Bang.co(64), ChuDe.rong("Đấu", ChuDe.SO) + 24);
                int hn = Math.min(h - 8, Bang.co(30));
                int wt = x + w - wn - 8 - xt - 6;
                ChuDe.chu(g, ChuDe.cat(d.ten, ChuDe.NHAN, wt), xt, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
                String phu = "Điểm " + d.diem + " · Sức mạnh " + d.suc + (d.daGap > 0 ? " · đã gặp " + d.daGap : "");
                ChuDe.chu(g, ChuDe.cat(phu, ChuDe.PHU, wt), xt, y + 6 + ChuDe.cao(ChuDe.NHAN), ChuDe.PHU, ChuDe.CHU_PHU, 0);
                Bang.nut(g, x + w - wn - 6, y + (h - hn) / 2, wn, hn, "Đấu", DauTruong.luotCon > 0 ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
                    public void perform() {
                        DauTruong.thach(d.id);
                    }
                }, "dt-dau-" + i);
            }
        });
    }

    // ---------------------------------------------------------------- tab Lịch sử (xem lại 5 trận gần nhất)

    void veLichSu(Graphics g, int x, int y, int w, int h) {
        final int[][] v = DauTruong.LICH_SU;
        if (v.length == 0) {
            Bang.trong(g, x, y, w, h, "Chưa có trận nào", "5 trận gần nhất của bạn sẽ ở đây để xem lại.");
            return;
        }
        final int rh = Math.max(Bang.caoHang() + 8, ChuDe.cao(ChuDe.NHAN) + ChuDe.cao(ChuDe.PHU) + 16);
        Bang.danhSach(g, this.ds, x, y + 2, w, h - 2, v.length, rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final int[] d = DauTruong.LICH_SU[i];
                Bang.hang(g, x, y, w, h, i, false, null, null);
                int wn = Math.max(Bang.co(80), ChuDe.rong("Xem lại", ChuDe.SO) + 24);
                int hn = Math.min(h - 8, Bang.co(30));
                int wt = w - wn - 30;
                String kq = d[1] == 1 ? "Thắng" : "Thua";
                GiaoDien.vien(g, x + 8, y + (h - ChuDe.cao(ChuDe.NHAN) - 6) / 2, ChuDe.rong("Thắng", ChuDe.NHAN) + 14, ChuDe.cao(ChuDe.NHAN) + 6,
                        d[1] == 1 ? ChuDe.LA : ChuDe.DO, null);
                int xk = x + 8 + (ChuDe.rong("Thắng", ChuDe.NHAN) + 14) / 2;
                ChuDe.chuVienGiua(g, kq, xk, y + (h - ChuDe.cao(ChuDe.NHAN) - 6) / 2, ChuDe.cao(ChuDe.NHAN) + 6, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 2);
                int xt = x + 30 + ChuDe.rong("Thắng", ChuDe.NHAN);
                String ten = i < DauTruong.LICH_SU_TEN.length ? DauTruong.LICH_SU_TEN[i] : "";
                ChuDe.chu(g, ChuDe.cat("Gặp " + ten, ChuDe.NHAN, wt - xt + x), xt, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
                ChuDe.chu(g, "Điểm " + (d[2] >= 0 ? "+" : "") + d[2], xt, y + 6 + ChuDe.cao(ChuDe.NHAN), ChuDe.PHU, ChuDe.CHU_PHU, 0);
                Bang.nut(g, x + w - wn - 6, y + (h - hn) / 2, wn, hn, "Xem lại", Bang.PHU, new IAction() {
                    public void perform() {
                        DauTruong.xemLaiTran(d[0]);
                    }
                }, "dt-xem-" + i);
            }
        });
    }

    // ---------------------------------------------------------------- tab Tuần (bảng tuần trong game)

    void veTuan(Graphics g, int x, int y, int w, int h) {
        if (!DauTruong.coTuan) {
            Bang.trong(g, x, y, w, h, DauTruong.choTuan ? "Đang tải bảng tuần..." : "Chưa có dữ liệu", "Xếp theo điểm mùa kiếm được tuần này (thứ Hai 4 giờ đổi tuần).");
            return;
        }
        int k = Bang.cach();
        int rh = Math.max(Bang.caoHang(), ChuDe.cao(ChuDe.NHAN) + 12);
        int[] toi = DauTruong.tuanToi;
        Bang.the(g, x, y + 2, w, rh);
        String chuToi = toi[0] > 0 ? "Bạn: hạng " + toi[0] + " · " + (toi[1] >= 0 ? "+" : "") + toi[1] + " điểm · thắng " + toi[2] + "/" + toi[3]
                : "Bạn chưa đấu tuần này";
        ChuDe.chuGiua(g, ChuDe.cat(chuToi, ChuDe.NHAN, w - 16), x + 8, y + 2, rh, ChuDe.NHAN, ChuDe.CHINH_TOI, 0);
        final int[][] v = DauTruong.TUAN;
        int yl = y + 2 + rh + k;
        if (v.length == 0) {
            Bang.trong(g, x, yl, w, y + h - yl, "Tuần này chưa có trận nào", null);
            return;
        }
        Bang.danhSach(g, this.ds, x, yl, w, y + h - yl, v.length, rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                int[] d = DauTruong.TUAN[i];
                Bang.hang(g, x, y, w, h, i, false, null, null);
                int r = h / 2 - 4;
                int mau = i == 0 ? ChuDe.VANG : (i == 1 ? 0xC9D2DA : (i == 2 ? 0xE0A15E : 0x8E4A1E));
                GiaoDien.khoiPixel(g, x + 5, y + 4, 2 * r, 2 * r, mau);
                ChuDe.chuVienGiua(g, String.valueOf(d[0]), x + 5 + r, y + 4, 2 * r, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
                String so = (d[1] >= 0 ? "+" : "") + d[1] + " · " + d[2] + "/" + d[3];
                int ws = ChuDe.rong(so, ChuDe.NHAN);
                ChuDe.chuGiua(g, ChuDe.cat(i < DauTruong.TUAN_TEN.length ? DauTruong.TUAN_TEN[i] : "", ChuDe.NHAN, w - ws - 2 * r - 30), x + 2 * r + 14, y, h,
                        ChuDe.NHAN, ChuDe.CHU, 0);
                ChuDe.chuGiua(g, so, x + w - 8, y, h, ChuDe.NHAN, ChuDe.CHU_PHU, 1);
            }
        });
    }

    // ---------------------------------------------------------------- tab Đội hình

    static final String[] TEN_HANG = {"Tấn công", "Phòng thủ"};

    int sucNhap(int hg) {
        int t = 0;
        for (int c = 0; c < 3; c++) {
            DauTruong.Kho k = DauTruong.kho(nhap[hg][c]);
            if (k != null) {
                t += k.suc;
            }
        }
        return t;
    }

    boolean du(int hg) {
        for (int c = 0; c < 3; c++) {
            if (DauTruong.kho(nhap[hg][c]) == null) {
                return false;
            }
        }
        return true;
    }

    void dat(DauTruong.Kho k) {
        if (k.camLy != 0) {
            ThongBao.hien(k.ten + " là Huyền thoại, không vào Đấu trường được.", ThongBao.LOI);
            return;
        }
        int[] h = nhap[oHang];
        for (int c = 0; c < 3; c++) {
            DauTruong.Kho o = DauTruong.kho(h[c]);
            if (c != oCot && o != null && (o.id == k.id || o.dex == k.dex)) {
                h[c] = h[oCot];   // đã có trong đội: đổi chỗ hai ô
            }
        }
        h[oCot] = k.id;
        // tự sang ô trống kế tiếp
        for (int c = 0; c < 3; c++) {
            if (DauTruong.kho(h[c]) == null) {
                oCot = c;
                return;
            }
        }
    }

    void veDoiHinh(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int lh = ChuDe.cao(ChuDe.NHAN);
        int so = Math.max(Bang.co(44), Math.min(Bang.co(64), (w - 2 * k) / 8));
        // màn thấp (điện thoại ngang 20:9 / 21:9): thu ô đội để lưới Hộp PC còn ít nhất một hàng chạm được
        int rhKho = Bang.coO() + 8 + ChuDe.cao(ChuDe.PHU) + 14;
        int soMax = (h - 2 - ChuDe.cao(ChuDe.PHU) - rhKho - 3 * k - 4) / 2 - 8;
        so = Math.max(VungCham.MIN + 2, Math.min(so, soMax));
        int wl = Math.max(ChuDe.rong("Phòng thủ", ChuDe.NHAN), ChuDe.rong("Sức mạnh 9999", ChuDe.PHU)) + 12;
        int wnut = Math.max(Bang.co(70), ChuDe.rong("Lưu đội", ChuDe.SO) + 24);
        int yy = y + 2;
        for (int hg = 0; hg < 2; hg++) {
            final int hh = hg;
            int hr = so + 8;
            Bang.the(g, x, yy, w, hr);
            ChuDe.chu(g, TEN_HANG[hg], x + 8, yy + 6, ChuDe.NHAN, ChuDe.CHU, 0);
            ChuDe.chu(g, "Sức mạnh " + sucNhap(hg), x + 8, yy + 8 + lh, ChuDe.PHU, ChuDe.CHU_PHU, 0);
            for (int c = 0; c < 3; c++) {
                final int cc = c;
                int xo = x + wl + c * (so + k);
                boolean chon = oHang == hg && oCot == c;
                DauTruong.Kho kc = DauTruong.kho(nhap[hg][c]);
                Bang.oDo(g, xo, yy + 4, so, kc != null, chon, new IAction() {
                    public void perform() {
                        ManDauTruong.this.oHang = hh;
                        ManDauTruong.this.oCot = cc;
                    }
                }, "o-" + hg + "-" + c);
                if (kc != null) {
                    PokeBody.paintChanDung(g, kc.dex, kc.shiny, xo + so / 2, yy + 4 + so / 2 - 4, so - 14);
                    int is = Math.max(8, Math.min(12, so / 6));
                    int ws = 3 * is + 2 * Math.max(1, is / 8);
                    IconBat.sao(g, kc.sao, xo + (so - ws) / 2, yy + 4 + so - is / 2 - 3, is);
                }
            }
            int xb = x + wl + 3 * (so + k) + k;
            int wb = Math.min(wnut, x + w - 8 - xb);
            if (wb >= Bang.co(44)) {
                Bang.nut(g, x + w - wb - 6, yy + (hr - Bang.co(28)) / 2, wb, Bang.co(28), "Lưu đội", du(hg) ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
                    public void perform() {
                        if (ManDauTruong.this.du(hh)) {
                            DauTruong.luuDoi(hh, ManDauTruong.this.nhap[hh]);
                        }
                    }
                }, hg == 0 ? "luu-cong" : "luu-thu");
            }
            yy += hr + k;
        }
        ChuDe.chu(g, ChuDe.cat("Chạm ô (" + TEN_HANG[oHang] + " " + (oCot + 1) + ") rồi chạm Pokémon. Không trùng loài, không Huyền thoại.",
                ChuDe.PHU, w - 8), x + 4, yy, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        yy += ChuDe.cao(ChuDe.PHU) + k;
        final Vector v = DauTruong.KHO;
        if (v.size() == 0) {
            Bang.trong(g, x, yy, w, y + h - yy, "Hộp PC đang trống", "Thu phục Pokémon hoang dã (Poké Ball ở Y tá Chansey) để lập đội.");
            return;
        }
        final int o = Bang.coO() + 8;
        final int cot = Math.max(3, (w + k) / (o + k));
        int hang = (v.size() + cot - 1) / cot;
        final int rh = o + ChuDe.cao(ChuDe.PHU) + 14;
        Bang.danhSach(g, this.ds, x, yy, w, y + h - yy, hang, rh, new Bang.Hang() {
            public void ve(Graphics g, int r, int x, int y, int w, int h) {
                int kk = Bang.cach();
                int s = (w - (cot - 1) * kk) / cot;
                for (int c = 0; c < cot; c++) {
                    int i = r * cot + c;
                    if (i >= v.size()) {
                        break;
                    }
                    final DauTruong.Kho kc = (DauTruong.Kho) v.elementAt(i);
                    int xi = x + c * (s + kk);
                    boolean trongDoi = false;
                    for (int q = 0; q < 3; q++) {
                        trongDoi |= ManDauTruong.this.nhap[ManDauTruong.this.oHang][q] == kc.id;
                    }
                    Bang.hang(g, xi, y, s, h, i, trongDoi, null, null);
                    veThe(g, kc, xi, y, s, h);
                    Bang.dangKy(g, xi, y, s, h, new IAction() {
                        public void perform() {
                            ManDauTruong.this.dat(kc);
                        }
                    }, "kho-" + i);
                }
            }
        });
    }

    /** Thẻ một Pokémon trong lưới: chân dung PNG trên đĩa màu hệ, tên, sức mạnh cấp 50, sao cá thể. */
    static void veThe(Graphics g, DauTruong.Kho k, int x, int y, int s, int h) {
        int lh1 = ChuDe.cao(ChuDe.NHAN), lh2 = ChuDe.cao(ChuDe.PHU);
        int is = Math.max(10, Math.min(16, s / 7));
        int hi = h - lh1 - lh2 - is - 10;
        int rd = Math.max(8, Math.min(s, hi) / 2 - 2);
        int mh = KieuO.mauHe(k.he1);
        int cy = y + 4 + hi / 2;
        GiaoDien.khoi(g, x + s / 2 - rd, cy - rd, 2 * rd, 2 * rd, rd, GiaoDien.pha(mh, 0xFFFFFF, 4, 5));
        PokeBody.paintChanDung(g, k.dex, k.shiny, x + s / 2, cy, 2 * rd);
        int yt = y + 4 + hi;
        ChuDe.chu(g, ChuDe.cat(k.ten, ChuDe.NHAN, s - 6), x + s / 2, yt, ChuDe.NHAN, k.camLy != 0 ? ChuDe.CHU_NHAT : ChuDe.CHU, 2);
        ChuDe.chu(g, ChuDe.cat(k.camLy != 0 ? "Huyền thoại" : "SM " + k.suc + " · " + k.ivPt + "%", ChuDe.PHU, s - 6), x + s / 2, yt + lh1, ChuDe.PHU,
                ChuDe.CHU_PHU, 2);
        int ws = 3 * is + 2 * Math.max(1, is / 8);
        IconBat.sao(g, k.sao, x + (s - ws) / 2, yt + lh1 + lh2 + is / 2 + 2, is);
        if (k.shiny) {
            int ic = Math.max(12, is);
            IconBat.ve(g, IconBat.SAC_MAU, x + ic / 2 + 3, y + ic / 2 + 3, ic);
        }
    }
}
