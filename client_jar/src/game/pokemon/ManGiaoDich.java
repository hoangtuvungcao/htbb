package game.pokemon;

import game.model.Char;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.model.Potion;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * GIAO DỊCH (0.9.3, bảng chung Bang): hai ô đồ "Bạn đưa" / "Bạn nhận" (12 ô mỗi bên), bên phải là hành trang (chip
 * Đồ | Thuốc) - chạm món = thêm vào giao dịch, chạm món trong ô "Bạn đưa" = rút ra. Nút to: "Khoá" (xong phần mình)
 * -> chờ đối phương -> "Chuyển" (xác nhận) ; "Huỷ giao dịch" bên trái. Cùng gói 66 như cửa sổ cũ.
 */
public final class ManGiaoDich extends Screen {
    public static ManGiaoDich me;
    /** 0 đang chọn đồ, 1 đã khoá (chờ), 2 hai bên đã khoá: được chuyển, 3 đã chuyển (chờ máy chủ) */
    public static int buoc;
    int loc;
    final Cuon ds = new Cuon();

    public static ManGiaoDich gI() {
        if (me == null) {
            me = new ManGiaoDich();
        }
        return me;
    }

    public static void mo() {
        ManGiaoDich m = gI();
        buoc = 0;
        m.loc = 0;
        m.ds.den(0);
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    /** GameScr.onConfirmTrade: cả hai bên đã khoá. */
    public static void haiBenKhoa() {
        buoc = 2;
    }

    /** Giao dịch xong / bị huỷ: về màn chơi nếu đang ở màn này. */
    public static void ket() {
        buoc = 0;
        if (me != null && Canvas.currentScreen == me) {
            Canvas.gameScr.switchToMe();
        }
    }

    static MainChar mc() {
        return Canvas.gameScr.mainChar;
    }

    void huy() {
        GameService.gI().sendCancelTrade(mc().ID);
        mc().isTrade = false;
        ket();
    }

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
            HopXacNhan.hoi("Huỷ giao dịch", "Dừng giao dịch này?", "Huỷ giao dịch", new IAction() {
                public void perform() {
                    ManGiaoDich.this.huy();
                }
            }, null);
            return true;
        }
        return false;
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // 0.9.5: chỉ lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        String doi = mc().nameUserTrade != null && mc().nameUserTrade.length() > 0 ? mc().nameUserTrade : "người chơi";
        int[] c = Bang.khung(g, "Giao dịch với " + doi, null, 0, null, new IAction() {
            public void perform() {
                ManGiaoDich.this.keyPress(-7);
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        int s = Bang.co(30);
        int wOi = 4 * s + 3 * k;
        int wl = Math.min(w * (ChuDe.dt() ? 66 : 58) / 100, 2 * wOi + 3 * k + 16);
        int nh = Bang.caoHang();
        // 0.9.27: hàng xu dưới mỗi bên (cao = hàng nút) - lưới đồ ngắn lại một hàng
        int hO = h - nh - k - nh - k;
        int wb = (wl - k) / 2;
        // 0.9.22 (APK mật độ cao): lưới chỉ dùng cot x ô - phần dư của hai bên trả cho cột Hành trang (tên đồ không bị cắt)
        int[] cs = cotO(wb, hO - 13 - Font.blackFont.getHeight());
        int wCan = 12 + cs[0] * cs[1] + (cs[0] - 1) * k + 4;
        if (wCan < wb) {
            wb = wCan;
            wl = 2 * wb + k;
        }
        veO(g, x, y, wb, hO, "Bạn đưa", mc().tItems, true);
        veO(g, x + wb + k, y, wl - wb - k, hO, "Bạn nhận", mc().rItems, false);
        veXu(g, x, y + hO + k, wb, nh, true);
        veXu(g, x + wb + k, y + hO + k, wl - wb - k, nh, false);
        // nút
        Bang.nut(g, x, y + h - nh, wb, nh, "Huỷ giao dịch", Bang.TRUNG, new IAction() {
            public void perform() {
                ManGiaoDich.this.keyPress(-7);
            }
        }, "gd-huy");
        String chu = buoc == 0 ? "Khoá" : (buoc == 1 ? "Chờ đối phương..." : (buoc == 2 ? "Chuyển" : "Đang chuyển..."));
        Bang.nut(g, x + wb + k, y + h - nh, wl - wb - k, nh, chu, buoc == 0 || buoc == 2 ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
            public void perform() {
                if (buoc == 0) {
                    GameService.gI().sendOkTrade(mc().ID);
                    buoc = 1;
                } else if (buoc == 2) {
                    HopXacNhan.hoi("Chuyển đồ", "Đồng ý trao đổi? Sau khi chuyển không lấy lại được."
                            + (XuGiaoDich.dua > 0 || XuGiaoDich.nhan > 0 ? " Bạn đưa " + TienTe.chu(TienTe.XU, XuGiaoDich.dua)
                            + ", nhận " + TienTe.chu(TienTe.XU, XuGiaoDich.nhan) + "." : ""), "Chuyển", new IAction() {
                        public void perform() {
                            GameService.gI().sendConfirmTrade();
                            mc().isTrade = false;
                            buoc = 3;
                        }
                    }, null);
                }
            }
        }, "gd-chinh");
        // hành trang
        int xr = x + wl + k, wr = w - wl - k;
        Bang.the(g, xr, y, wr, h);
        Font.blackFont.drawString(g, "Hành trang", xr + 8, y + 6, 0);
        Object[] vl = new Object[2];
        for (int i = 0; i < 2; i++) {
            final int li = i;
            vl[i] = new IAction() {
                public void perform() {
                    ManGiaoDich.this.loc = li;
                    ManGiaoDich.this.ds.den(0);
                }
            };
        }
        int yl = y + 10 + Font.blackFont.getHeight();
        yl += Bang.hangChip(g, xr + 6, yl, wr - 12, new String[]{"Đồ", "Thuốc"}, this.loc, vl) + k;
        final Vector v = new Vector();
        if (this.loc == 0) {
            for (int i = 0; i < Char.inventory.size(); i++) {
                ItemInInventory it = (ItemInInventory) Char.inventory.elementAt(i);
                if (!it.isSelling && mc().isExistInvector(mc().tItems, it) < 0) {
                    v.addElement(it);
                }
            }
        } else {
            for (int i = 0; MainChar.listPotion != null && i < MainChar.listPotion.length; i++) {
                if (MainChar.listPotion[i] != null && MainChar.listPotion[i].isTrade && MainChar.listPotion[i].number - MainChar.listPotion[i].numTrade > 0) {
                    v.addElement(new Integer(i));
                }
            }
        }
        int hl = y + h - 4 - yl;
        if (v.size() == 0) {
            Bang.trong(g, xr + 4, yl, wr - 8, hl, this.loc == 0 ? "Không còn đồ giao dịch được" : "Không có thuốc giao dịch được", null);
            return;
        }
        Bang.danhSach(g, this.ds, xr + 4, yl, wr - 8, hl, v.size(), Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                Object o = v.elementAt(i);
                if (o instanceof ItemInInventory) {
                    final ItemInInventory it = (ItemInInventory) o;
                    ManNhanVat.veHangMon(g, x, y, w, h, i, it, false, buoc != 0 ? null : new IAction() {
                        public void perform() {
                            if (mc().tItems.size() >= 12) {
                                DungDo.loi("Tối đa 12 món mỗi lần.");
                                return;
                            }
                            GameService.gI().sendAddItemTrade(it.itemID, 0, 0);
                        }
                    });
                } else {
                    final int loai = ((Integer) o).intValue();
                    final int con = MainChar.listPotion[loai].number - MainChar.listPotion[loai].numTrade;
                    Bang.hang(g, x, y, w, h, i, false, buoc != 0 ? null : new IAction() {
                        public void perform() {
                            game.render.Canvas.inputDlg.setInfo("Số lượng " + ManCuaHang.tenThuoc(loai), new IAction() {
                                public void perform() {
                                    try {
                                        int n = Integer.parseInt(game.render.Canvas.inputDlg.tfInput.getText().trim());
                                        if (n > 0 && n <= con) {
                                            GameService.gI().sendAddItemTrade((short) loai, 1, n);
                                            Canvas.endDlg();
                                        }
                                    } catch (Throwable e) {
                                        // không phải số
                                    }
                                }
                            }, game.render.screen.TField.INPUT_TYPE_NUMERIC, 4, true);
                            game.render.Canvas.inputDlg.soToiDa = con;
                            game.render.Canvas.inputDlg.show();
                        }
                    }, "gd-thuoc-" + i);
                    int s = ManNhanVat.oDau(g, x, y, h);
                    ManNhanVat.veThuoc(g, loai, x + 3 + s / 2, y + 3 + s / 2, s);
                    ManNhanVat.chuHang(g, x, y, w, h, s, ManCuaHang.tenThuoc(loai), "x" + con, "Chạm để thêm");
                }
            }
        });
    }

    /**
     * 0.9.27 hàng xu (kiểu NRO): bên mình = nút "Đưa [xu] số" (chạm -> nhập số xu, chỉ khi chưa khoá); bên kia = thẻ "Nhận
     * [xu] số". Lượng / lượng khoá không có hàng - không giao dịch được (chỉ NPC "Chuyển lượng").
     */
    void veXu(Graphics g, int x, int y, int w, int h, boolean cuaMinh) {
        long v = cuaMinh ? XuGiaoDich.dua : XuGiaoDich.nhan;
        if (cuaMinh) {
            Bang.nut(g, x, y, w, h, "", buoc == 0 ? Bang.TRUNG : Bang.TAT, buoc != 0 ? null : new IAction() {
                public void perform() {
                    XuGiaoDich.hoi();
                }
            }, "gd-xu");
        } else {
            Bang.the(g, x, y, w, h);
        }
        Font f = Font.blackFont;
        String nhan = cuaMinh ? "Đưa " : "Nhận ";
        int wn = f.getWidth(nhan);
        boolean gon = wn + TienTe.rongGia(TienTe.XU, v, f, false) > w - 8;
        int wTong = wn + TienTe.rongGia(TienTe.XU, v, f, gon);
        int x0 = x + Math.max(4, (w - wTong) / 2);
        f.drawString(g, nhan, x0, y + (h - f.getHeight()) / 2, 0);
        TienTe.veGia(g, TienTe.XU, v, x0 + wn, y + (h - TienTe.coTheoChu(f)) / 2, f, gon);
    }

    /** {số cột, cạnh ô} lưới 12 ô của một bên rộng w, cao hl cho lưới: 4 cột x 3 hàng (bảng hẹp: 3 x 4, rồi 2 cột). */
    static int[] cotO(int w, int hl) {
        int k = Bang.cach();
        int cot = 4;
        int s = Math.min(Bang.co(30), (w - 12 - 3 * k) / 4);
        if (s < VungCham.MIN) {
            int s3 = Math.min(Bang.co(30), Math.min((w - 12 - 2 * k) / 3, (hl - 3 * k) / 4));
            if (s3 > s) {
                cot = 3;
                s = s3;
            }
        }
        if (s < VungCham.MIN) {
            // 0.9.6 (tấm ~78 % bề ngang trên điện thoại): 2 cột x 6 hàng (cuộn mắt: chỉ hàng vừa khung)
            int s2 = Math.min(Bang.co(30), (w - 12 - k) / 2);
            if (s2 > s) {
                cot = 2;
                s = s2;
            }
        }
        return new int[]{cot, s};
    }

    /** Ô đồ một bên: 4 cột x 3 hàng (bảng hẹp: 3 x 4). */
    void veO(Graphics g, int x, int y, int w, int h, String tieuDe, Vector ds, final boolean cuaMinh) {
        Bang.the(g, x, y, w, h);
        Font.blackFont.drawString(g, tieuDe, x + 8, y + 5, 0);
        int k = Bang.cach();
        int y0 = y + 9 + Font.blackFont.getHeight();
        int[] cs = cotO(w, y + h - 4 - y0);
        int cot = cs[0], s = cs[1];
        for (int i = 0; i < 12; i++) {
            int xi = x + 6 + (i % cot) * (s + k), yi = y0 + (i / cot) * (s + k);
            if (yi + s > y + h - 4) {
                break;
            }
            final Object o = i < ds.size() ? ds.elementAt(i) : null;
            Object viec = null;
            if (o != null && cuaMinh && buoc == 0) {
                viec = new IAction() {
                    public void perform() {
                        if (o instanceof ItemInInventory) {
                            GameService.gI().sendAddItemTrade(((ItemInInventory) o).itemID, 0, 0);
                        } else {
                            GameService.gI().sendAddItemTrade(((Potion) o).id, 2, 0);
                        }
                    }
                };
            }
            Bang.oDo(g, xi, yi, s, o != null, false, viec, (cuaMinh ? "dua-" : "nhan-") + i);
            if (o instanceof ItemInInventory) {
                ItemTemplate t = ManNhanVat.mau((ItemInInventory) o);
                if (t != null) {
                    Bang.icon(g, t.idIcon, xi + s / 2, yi + s / 2, s >= 30 ? s / 22f : 1f);
                }
            } else if (o instanceof Potion) {
                Potion p = (Potion) o;
                IconVatPham.ve(g, (short) (p.index + 5500), xi + s / 2, yi + s / 2, s, IconVatPham.DP_THUOC);
                KieuO.soLuong(g, xi + s - 2, yi + s - 2, String.valueOf(p.number));
            }
        }
        if (ds.size() == 0) {
            Font.arialFont.drawString(g, cuaMinh ? "Chạm đồ bên phải để thêm" : "Chưa có", x + w / 2, y + h - 6 - Font.arialFont.getHeight(), Font.CENTER);
        }
    }
}
