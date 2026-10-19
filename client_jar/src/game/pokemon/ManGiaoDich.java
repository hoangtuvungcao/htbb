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
            Canvas.gameScr.paint(g);
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
        int wl = Math.min(w * 58 / 100, 2 * wOi + 3 * k + 16);
        int nh = Bang.caoHang();
        int hO = h - nh - k;
        int wb = (wl - k) / 2;
        veO(g, x, y, wb, hO, "Bạn đưa", mc().tItems, true);
        veO(g, x + wb + k, y, wl - wb - k, hO, "Bạn nhận", mc().rItems, false);
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
                    HopXacNhan.hoi("Chuyển đồ", "Đồng ý trao đổi? Sau khi chuyển không lấy lại được.", "Chuyển", new IAction() {
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
                    int s = h - 6;
                    KieuO.oChieu(g, x + 3, y + 3, s, s, 1, KieuO.HINH_GIOT, false);
                    ManNhanVat.chuHang(g, x, y, w, h, s, ManCuaHang.tenThuoc(loai), "x" + con, "Chạm để thêm");
                }
            }
        });
    }

    /** Ô đồ một bên: 4 cột x 3 hàng (bảng hẹp: 3 x 4). */
    void veO(Graphics g, int x, int y, int w, int h, String tieuDe, Vector ds, final boolean cuaMinh) {
        Bang.the(g, x, y, w, h);
        Font.blackFont.drawString(g, tieuDe, x + 8, y + 5, 0);
        int k = Bang.cach();
        int y0 = y + 9 + Font.blackFont.getHeight();
        // 4 cột x 3 hàng; bảng hẹp (ô nhỏ hơn cỡ chạm tối thiểu) -> 3 cột x 4 hàng nếu đủ cao
        int cot = 4;
        int s = Math.min(Bang.co(30), (w - 12 - 3 * k) / 4);
        if (s < VungCham.MIN) {
            int s3 = Math.min(Bang.co(30), Math.min((w - 12 - 2 * k) / 3, (y + h - 4 - y0 - 3 * k) / 4));
            if (s3 > s) {
                cot = 3;
                s = s3;
            }
        }
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
                GameData.paintIcon(g, (short) (p.index + 5500), xi + s / 2, yi + s / 2);
                KieuO.soLuong(g, xi + s - 2, yi + s - 2, String.valueOf(p.number));
            }
        }
        if (ds.size() == 0) {
            Font.arialFont.drawString(g, cuaMinh ? "Chạm đồ bên phải để thêm" : "Chưa có", x + w / 2, y + h - 6 - Font.arialFont.getHeight(), Font.CENTER);
        }
    }
}
