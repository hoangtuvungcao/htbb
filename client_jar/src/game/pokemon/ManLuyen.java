package game.pokemon;

import game.model.Char;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.MainChar;
import game.model.RealID;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * LUYỆN ĐỒ / KHẢM kiểu NRO (0.9.7, NPC Thợ rèn; máy chủ 71 / -34) - thay cửa sổ cũ WindowInfoScr tab 21 / 25 ở máy cảm
 * ứng. Trái: ô TRANG BỊ lớn + 8 ô NGUYÊN LIỆU, phí (xu) và nút Luyện / Khảm. Phải: chip "Trang bị | Nguyên liệu" + danh
 * sách; chạm hàng = thẻ vật phẩm có nút "Đặt vào"; chạm ô đã đặt = lấy ra. Luật đặt giữ nguyên cửa sổ cũ
 * (WindowInfoScr.setSelectedImbue): tối đa 8 nguyên liệu, luyện phải đặt luyện kim dược trước, một loại bảo hiểm, một loại
 * ngọc, không đập đồ thuê; gửi addItemImbue / addGemItemImbue / doImbueItem | doKhamItem như cũ.
 */
public final class ManLuyen extends Screen {
    /** HOP (máy chủ -36): HỢP THÀNH ngọc - đặt 3..5 viên cùng nhóm ép (typeEp 1), gửi epNgoc(1, ds) một lần */
    public static final int LUYEN = 21, KHAM = 25, HOP = 26;
    static ManLuyen me;
    int kieu = LUYEN;
    int gia;
    int tab;
    /** nguyên liệu còn lại để đặt (GemTemplate, số đã trừ phần đã đặt) */
    final Vector nguyen = new Vector();
    final Cuon ds = new Cuon();

    public static ManLuyen gI() {
        if (me == null) {
            me = new ManLuyen();
        }
        return me;
    }

    /** GameScr.onImbue / onKham: ds = nguyên liệu dùng được (đã lọc theo kiểu khoá). */
    public static void mo(int kieu, int gia, Vector ds) {
        ManLuyen m = gI();
        m.kieu = kieu;
        m.gia = gia;
        m.tab = kieu == HOP ? 1 : 0;
        m.nguyen.removeAllElements();
        for (int i = 0; ds != null && i < ds.size(); i++) {
            GemTemplate g = (GemTemplate) ds.elementAt(i);
            GemTemplate c = new GemTemplate(g.id);
            GemTemplate.copyData(g, c);
            c.id = g.id;
            if (c.number > 0) {
                m.nguyen.addElement(c);
            }
        }
        MainChar.gemitemImbue.removeAllElements();
        mc().itemImbue = null;
        m.ds.den(0);
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        Canvas.endDlg();
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    public static boolean dangMo() {
        return me != null && Canvas.currentScreen == me;
    }

    static MainChar mc() {
        return Canvas.gameScr.mainChar;
    }

    String tenViec() {
        return this.kieu == LUYEN ? "Luyện" : this.kieu == HOP ? "Hợp" : "Khảm";
    }

    /** GameScr.onEpNgoc: nguyên liệu = ngọc túi thường có typeEp 1 (như WindowInfoScr.setListImbue(true, 1, 0)). */
    public static void moHop(int gia) {
        Vector v = new Vector();
        for (int i = 0; i < MainChar.gemItem.size(); i++) {
            GemTemplate g = (GemTemplate) MainChar.gemItem.elementAt(i);
            GemTemp t = Res.getGemByID(g.id);
            if (t != null && t.typeEp == 1) {
                v.addElement(g);
            }
        }
        mo(HOP, gia, v);
    }

    int soO() {
        return this.kieu == HOP ? 5 : 8;
    }

    void dong() {
        try {
            if (this.kieu == LUYEN) {
                GameService.gI().doImbueItem(-1);
            } else if (this.kieu == KHAM) {
                GameService.gI().doKhamItem(-1);
            }
        } catch (Throwable e) {
            // chưa nối máy chủ
        }
        MainChar.gemitemImbue.removeAllElements();
        mc().itemImbue = null;
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
    }

    // ------------------------------------------------------------------ đặt / lấy (luật như cửa sổ cũ)

    String khongDatDo(ItemInInventory it) {
        if (it.dayUse > 0) {
            return "Không được đập đồ thuê.";
        }
        if (it.isSelling) {
            return "Vật phẩm đang được bán.";
        }
        if (mc().itemImbue != null) {
            return "Đã đặt trang bị - chạm ô trang bị để lấy ra trước.";
        }
        return null;
    }

    void datDo(ItemInInventory it) {
        if (khongDatDo(it) != null) {
            return;
        }
        mc().itemImbue = it;
        GameService.gI().addItemImbue(it.itemID);
    }

    void layDo() {
        ItemInInventory it = mc().itemImbue;
        if (it == null) {
            return;
        }
        GameService.gI().addItemImbue(it.itemID);
        mc().itemImbue = null;
        AmThanh.phat(AmThanh.XAC_NHAN);
    }

    static boolean laNgoc(int id) {
        return id <= 7 || id == 155 || id == 156;
    }

    String khongDatNguyen(GemTemplate g) {
        if (MainChar.gemitemImbue.size() >= soO()) {
            return "Không thể thêm (tối đa " + soO() + ").";
        }
        if (this.kieu == HOP) {
            return null;
        }
        if (this.kieu == LUYEN && MainChar.gemitemImbue.size() == 0 && laNgoc(g.id)) {
            return "Phải đặt luyện kim dược trước.";
        }
        if (mc().countBaoHiem() >= 1 && g.id < 5) {
            return "Chỉ dùng 1 loại bảo hiểm cho 1 lần " + (this.kieu == LUYEN ? "luyện." : "khảm.");
        }
        if (!(mc().countLKD() < 1 || g.id < 8 || g.id == 155 || g.id == 156)) {
            return "Không thể dùng 2 loại ngọc cho 1 lần " + (this.kieu == LUYEN ? "luyện." : "khảm.");
        }
        return null;
    }

    void datNguyen(GemTemplate g) {
        if (khongDatNguyen(g) != null || g.number <= 0) {
            return;
        }
        RealID r = new RealID(g.rID);
        r.ID = g.id;
        MainChar.gemitemImbue.addElement(r);
        g.number--;
        if (g.number <= 0) {
            this.nguyen.removeElement(g);
        }
        if (this.kieu != HOP) {
            GameService.gI().addGemItemImbue(r.realID, (byte) 0);
        }
    }

    void layNguyen(RealID r) {
        MainChar.gemitemImbue.removeElement(r);
        boolean co = false;
        for (int i = 0; i < this.nguyen.size(); i++) {
            GemTemplate g = (GemTemplate) this.nguyen.elementAt(i);
            if (g.id == r.ID) {
                g.number++;
                co = true;
                break;
            }
        }
        if (!co) {
            GemTemplate g = new GemTemplate(r.ID);
            g.id = r.ID;
            g.rID = r.realID;
            g.number = 1;
            this.nguyen.addElement(g);
        }
        if (this.kieu != HOP) {
            GameService.gI().addGemItemImbue(r.realID, (byte) 1);
        }
        AmThanh.phat(AmThanh.XAC_NHAN);
    }

    String khongLam() {
        if (this.kieu == HOP) {
            if (MainChar.gemitemImbue.size() < 3) {
                return "Đặt ít nhất 3 viên.";
            }
            return mc().charXu < this.gia ? "Không đủ xu." : null;
        }
        if (mc().itemImbue == null) {
            return "Chưa đặt trang bị.";
        }
        if (!mc().hadPutItemImbue()) {
            return this.kieu == LUYEN ? "Chưa đặt luyện kim dược." : "Chưa đặt ngọc.";
        }
        if (mc().charXu < this.gia) {
            return "Không đủ xu.";
        }
        return null;
    }

    void lam() {
        if (khongLam() != null) {
            return;
        }
        String hoi = this.kieu == HOP ? "Hợp " + MainChar.gemitemImbue.size() + " viên?" : tenViec() + " " + ManNhanVat.mau(mc().itemImbue).name + "?";
        HopXacNhan.hoiGia(this.kieu == HOP ? "Hợp thành" : tenViec() + " trang bị", hoi, tenViec(), TienTe.XU,
                this.gia, new IAction() {
                    public void perform() {
                        if (mc().charXu < ManLuyen.this.gia) {
                            HopThongBao.mo(null, "Không đủ xu.", HopThongBao.LOI, new String[]{"OK"}, new int[]{Bang.XAC_NHAN},
                                    new IAction[]{null}, null);
                            return;
                        }
                        if (ManLuyen.this.kieu == HOP) {
                            GameService.gI().epNgoc(1, MainChar.gemitemImbue);
                            Canvas.startWaitDlg("Đang hợp..", false);
                            MainChar.gemitemImbue.removeAllElements();
                            return;
                        }
                        mc().charXu -= ManLuyen.this.gia;
                        if (ManLuyen.this.kieu == LUYEN) {
                            GameService.gI().doImbueItem(1);
                        } else {
                            GameService.gI().doKhamItem(1);
                        }
                        Canvas.startWaitDlg();
                    }
                });
    }

    // ------------------------------------------------------------------ vòng game

    public void update() {
        boolean c = Canvas.isPointerClick, d = Canvas.isPointerDown;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        try {
            Canvas.gameScr.update();
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.isPointerClick = c;
        Canvas.isPointerDown = d;
    }

    public void updateKey() {
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        if (keyCode == -7 || keyCode == 27) {
            dong();
            return true;
        }
        return false;
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        int[] c = Bang.khung(g, this.kieu == LUYEN ? "Luyện đồ" : this.kieu == HOP ? "Hợp thành" : "Khảm", null, 0, null, new IAction() {
            public void perform() {
                ManLuyen.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        int lw = Math.min(w * 45 / 100, Math.max(220, w / 2));
        // cỡ chạm lớn (APK mật độ cao, ô >= MIN): bàn đủ rộng cho hàng ô nguyên liệu, không tràn sang danh sách
        int cot = this.kieu == HOP ? 3 : 4;
        lw = Math.max(lw, Math.min(cot * VungCham.MIN + (cot - 1) * k + 16, w * 6 / 10));
        veBan(g, x, y, lw, h);
        veDanhSach(g, x + lw + k, y, w - lw - k, h);
    }

    /** Bàn luyện: ô trang bị lớn, 8 ô nguyên liệu (2 hàng x 4), phí + nút. */
    void veBan(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        ManNhanVat.nenSanKhau(g, x, y, w, h);
        int nh = Bang.caoHang();
        int hPhi = ChuDe.cao(ChuDe.THUONG) + 4;
        boolean hop = this.kieu == HOP;
        int cot = hop ? 3 : 4, n = soO();
        int s = Math.max(VungCham.MIN, Math.min((w - 16 - (cot - 1) * k) / cot, (h - nh - hPhi - 4 * k - 12) / 4));
        int sTo = Math.min(s * 3 / 2, h - nh - hPhi - 2 * s - 5 * k - 12);
        sTo = Math.max(sTo, s);
        int yy = y + 6;
        if (hop) {
            yy += Math.max(0, (h - nh - hPhi - 2 * s - 4 * k - 12) / 3);
        }
        // ô trang bị
        final ItemInInventory it = mc().itemImbue;
        if (!hop) {
            int xo = x + (w - sTo) / 2;
            if (it != null) {
                LuoiTui.veO(g, it, xo, yy, sTo, true, new IAction() {
                    public void perform() {
                        ManLuyen.this.layDo();
                    }
                }, "luyen-do");
            } else {
                Bang.oDo(g, xo, yy, sTo, false, false, new IAction() {
                    public void perform() {
                        ManLuyen.this.tab = 0;
                    }
                }, "luyen-do");
                int ic = sTo / 2;
                BieuTuong.veBong(g, BieuTuong.DA, xo + (sTo - ic) / 2, yy + (sTo - ic) / 2, Math.max(ic, 16), 0x8A6A4A, ChuDe.NEN_PHU);
            }
            yy += sTo + k;
        }
        // ô nguyên liệu (luyện / khảm 8 = 2 x 4; hợp 5 = 3 + 2)
        for (int i = 0; i < n; i++) {
            int hang = i / cot, soHang = Math.min(cot, n - hang * cot);
            int xh = x + (w - (soHang * s + (soHang - 1) * k)) / 2;
            int xx = xh + (i % cot) * (s + k), y1 = yy + hang * (s + k);
            if (i < MainChar.gemitemImbue.size()) {
                final RealID r = (RealID) MainChar.gemitemImbue.elementAt(i);
                GemTemplate gt = new GemTemplate(r.ID);
                gt.id = r.ID;
                LuoiTui.veO(g, gt, xx, y1, s, false, new IAction() {
                    public void perform() {
                        ManLuyen.this.layNguyen(r);
                    }
                }, "luyen-nl-" + i);
            } else {
                Bang.oDo(g, xx, y1, s, false, false, new IAction() {
                    public void perform() {
                        ManLuyen.this.tab = 1;
                    }
                }, "luyen-nl-" + i);
            }
        }
        yy += 2 * s + 2 * k;
        // phí + nút
        String phi = "Phí:";
        int co = Math.min(12, hPhi - 4);
        int wPhi = ChuDe.rong(phi, ChuDe.THUONG) + 6 + co + 3 + ChuDe.rong(TienTe.so(this.gia), ChuDe.SO);
        int xp = x + (w - wPhi) / 2;
        ChuDe.chu(g, phi, xp, yy, ChuDe.THUONG, ChuDe.CHU, 0);
        int xg = xp + ChuDe.rong(phi, ChuDe.THUONG) + 6;
        TienTe.ve(g, TienTe.XU, xg, yy + (hPhi - 4 - co) / 2 + 1, co);
        ChuDe.chu(g, TienTe.so(this.gia), xg + co + 3, yy, ChuDe.SO, mc().charXu < this.gia ? ChuDe.DO : ChuDe.CHU, 0);
        String l = khongLam();
        int yn = y + h - 6 - nh;
        Bang.nut(g, x + 8, yn, w - 16, nh, l == null ? tenViec() : l, l == null ? Bang.XAC_NHAN : Bang.TAT, l == null ? new IAction() {
            public void perform() {
                ManLuyen.this.lam();
            }
        } : null, "luyen-lam");
    }

    /** Phải: chip Trang bị | Nguyên liệu + danh sách; chạm = thẻ có nút "Đặt vào". */
    void veDanhSach(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        Object[] vc = {new IAction() {
            public void perform() {
                ManLuyen.this.tab = 0;
                ManLuyen.this.ds.den(0);
            }
        }, new IAction() {
            public void perform() {
                ManLuyen.this.tab = 1;
                ManLuyen.this.ds.den(0);
            }
        }};
        int yl;
        if (this.kieu == HOP) {
            this.tab = 1;
            ChuDe.chu(g, "Ngọc hợp được", x + 8, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
            yl = y + 4 + ChuDe.cao(ChuDe.NHAN) + 6;
        } else {
            yl = y + 4 + Bang.hangChip(g, x + 6, y + 4, w - 12, new String[]{"Trang bị", this.kieu == LUYEN ? "Nguyên liệu" : "Ngọc"},
                    this.tab, vc) + k;
        }
        int hl = y + h - 4 - yl;
        if (this.tab == 0) {
            final Vector v = new Vector();
            for (int i = 0; i < Char.inventory.size(); i++) {
                Object o = Char.inventory.elementAt(i);
                if (o instanceof ItemInInventory && o != mc().itemImbue) {
                    v.addElement(o);
                }
            }
            if (v.size() == 0) {
                Bang.trong(g, x + 4, yl, w - 8, hl, "Hành trang không có trang bị", null);
                return;
            }
            Bang.danhSach(g, this.ds, x + 4, yl, w - 8, hl, v.size(), ManXemTrangBi.caoHangMon(), new Bang.Hang() {
                public void ve(Graphics g, int i, int x, int y, int w, int h) {
                    final ItemInInventory it = (ItemInInventory) v.elementAt(i);
                    Bang.hang(g, x, y, w, h, i, false, new IAction() {
                        public void perform() {
                            ManLuyen.this.moTheDo(it);
                        }
                    }, "luyen-tui-" + i);
                    ManXemTrangBi.veHangMon(g, it, x, y, w, h, null);
                }
            });
            return;
        }
        if (this.nguyen.size() == 0) {
            Bang.trong(g, x + 4, yl, w - 8, hl, this.kieu == LUYEN ? "Không có nguyên liệu" : "Không có ngọc", null);
            return;
        }
        Bang.danhSach(g, this.ds, x + 4, yl, w - 8, hl, this.nguyen.size(), ManXemTrangBi.caoHangMon(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final GemTemplate gt = (GemTemplate) ManLuyen.this.nguyen.elementAt(i);
                Bang.hang(g, x, y, w, h, i, false, new IAction() {
                    public void perform() {
                        ManLuyen.this.moTheNguyen(gt);
                    }
                }, "luyen-ngoc-" + i);
                int s = ManNhanVat.oDau(g, x, y, h);
                GemTemp t = Res.getGemByID(gt.id);
                if (t != null) {
                    IconVatPham.ve(g, (short) (t.idImage + 6500), x + 3 + s / 2, y + 3 + s / 2, s, IconVatPham.DP_NGOC);
                }
                game.render.screen.Font f = game.render.screen.Font.blackFont, f2 = game.render.screen.Font.arialFont;
                f.drawString(g, GiaoDien.catChu(f, t == null ? "?" : t.name, w - s - 14), x + s + 9, y + 2, 0);
                f2.drawString(g, GiaoDien.catChu(f2, "Còn " + gt.number, w - s - 14), x + s + 9, y + 3 + f.getHeight(), 0);
            }
        });
    }

    void moTheDo(final ItemInInventory it) {
        if (TheVatPham.dangMo() || Canvas.currentDialog != null) {
            return;
        }
        TheVatPham.moMua(it, 0, TienTe.XU, 1, "Đặt vào", new TheVatPham.ViecMua() {
            public String khongMua(int so) {
                return ManLuyen.this.khongDatDo(it);
            }

            public void mua(int so) {
                ManLuyen.this.datDo(it);
            }
        }, null);
    }

    void moTheNguyen(final GemTemplate gt) {
        if (TheVatPham.dangMo() || Canvas.currentDialog != null) {
            return;
        }
        TheVatPham.moMua(gt, 0, TienTe.XU, 1, "Đặt vào", new TheVatPham.ViecMua() {
            public String khongMua(int so) {
                return ManLuyen.this.khongDatNguyen(gt);
            }

            public void mua(int so) {
                ManLuyen.this.datNguyen(gt);
            }
        }, null);
    }
}
