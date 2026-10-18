package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.pokemon.GiaoDien;
import game.pokemon.KieuO;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * "Túi sinh thái" dạng lưới ô kiểu Pokémon (thay menu danh sách tối đa 40 dòng của server).
 * Server (HTBB HeSinhThaiMenu.moTui) gửi menu -30: NPC ảo -528, idMenu 2, mỗi dòng
 * "Tên (bậc N) xSố". Chọn ô = gửi đúng lựa chọn đó như menu cũ (server mở menu Dùng / Bán / Rao...).
 * Mở từ menu Bản thân -> "Túi sinh thái" (gửi lựa chọn 4 của menu gốc -528 như khi bấm trong menu
 * Hệ sinh thái). Server không bật hệ sinh thái thì không có gói -> không có gì xảy ra.
 */
public class TuiSinhThaiScr extends Screen {
    public static final int NPC_ID = -528;
    public static final byte MENU_GOC = 0;
    public static final byte MENU_TUI = 2;
    private static TuiSinhThaiScr me;

    private String[] ten;
    private int[] bac;
    private int[] soLuong;
    private int npc;
    private byte idMenu;
    private int chon;
    private int cuon;
    private Screen truoc;

    public static TuiSinhThaiScr gI() {
        return me == null ? (me = new TuiSinhThaiScr()) : me;
    }

    /** Mở túi: xin server menu túi (như chọn "Túi sinh thái" trong menu Hệ sinh thái). */
    public static void xin() {
        try {
            game.networklogic.GameService.gI().doMenuOption(NPC_ID, MENU_GOC, 4);
        } catch (Throwable e) {
        }
    }

    private static int soCuoi(String s, int tu) {
        int n = 0;
        boolean co = false;
        for (int i = tu; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                break;
            }
            n = n * 10 + (c - '0');
            co = true;
        }
        return co ? n : -1;
    }

    /** Dòng menu "Tên (bậc N) xSố" -> {bậc, số} hoặc null. */
    private static int[] docDong(String s) {
        int p = s.lastIndexOf('x');
        int q = s.lastIndexOf('(');
        if (p <= 0 || q < 0 || q > p) {
            return null;
        }
        int so = soCuoi(s, p + 1);
        int b = s.indexOf("bậc ", q);
        int bac = b >= 0 ? soCuoi(s, b + 4) : 1;
        if (so < 0) {
            return null;
        }
        return new int[]{bac < 1 ? 1 : bac, so};
    }

    public static boolean laMenuTui(int npc, byte idMenu, String[] ds) {
        if (npc != NPC_ID || idMenu != MENU_TUI || ds == null || ds.length == 0) {
            return false;
        }
        for (int i = 0; i < ds.length; i++) {
            if (ds[i] == null || docDong(ds[i]) == null) {
                return false;
            }
        }
        return true;
    }

    public void moi(int npc, byte idMenu, String[] ds) {
        this.npc = npc;
        this.idMenu = idMenu;
        int n = ds.length;
        this.ten = new String[n];
        this.bac = new int[n];
        this.soLuong = new int[n];
        for (int i = 0; i < n; i++) {
            int[] d = docDong(ds[i]);
            int q = ds[i].lastIndexOf('(');
            this.ten[i] = q > 0 ? ds[i].substring(0, q).trim() : ds[i];
            this.bac[i] = d[0];
            this.soLuong[i] = d[1];
        }
        if (this.chon >= n) {
            this.chon = 0;
        }
        Canvas.endDlg();
        if (Canvas.currentScreen != this) {
            this.truoc = Canvas.currentScreen;
            switchToMe();
        }
    }

    public TuiSinhThaiScr() {
        this.right = new Command("Đóng", new IAction() {
            public void perform() {
                TuiSinhThaiScr.this.dong();
            }
        });
        this.center = new Command("Chọn", new IAction() {
            public void perform() {
                TuiSinhThaiScr.this.chonO();
            }
        });
    }

    void dong() {
        if (this.truoc != null && this.truoc != this) {
            this.truoc.switchToMe();
        } else {
            Canvas.gameScr.switchToMe();
        }
    }

    void chonO() {
        if (this.ten == null || this.chon < 0 || this.chon >= this.ten.length) {
            return;
        }
        int npc0 = this.npc;
        byte m0 = this.idMenu;
        int c0 = this.chon;
        dong();
        game.networklogic.GameService.gI().doMenuOption(npc0, m0, c0);
    }

    // ------------------------------------------------------------------ bố cục

    private int o() {
        return Canvas.w >= 220 ? 24 : 20;
    }

    private int soCot() {
        int c = (Canvas.w - 24) / o();
        return c < 4 ? 4 : (c > 8 ? 8 : c);
    }

    private int x0() {
        return (Canvas.w - soCot() * o()) / 2;
    }

    private int y0() {
        return 40;
    }

    private int soHang() {
        int h = (Canvas.h - Screen.deltaY - y0() - 48) / o();
        return h < 2 ? 2 : h;
    }

    /** Hình + hệ (màu) theo tên nguyên liệu. */
    private static int[] kieu(String s) {
        String t = game.pokemon.ChuPokemon.thuong(s);
        if (t.indexOf("berry") >= 0 || t.indexOf("oran") >= 0 || t.indexOf("rong") >= 0 || t.indexOf("hạt") >= 0) {
            return new int[]{KieuO.HINH_LA, 4};
        }
        if (t.indexOf("rác") >= 0) {
            return new int[]{KieuO.HINH_BOM, 7};
        }
        if (t.indexOf("vỏ sò") >= 0 || t.indexOf("ngọc trai") >= 0) {
            return new int[]{KieuO.HINH_BONG, 2};
        }
        if (t.indexOf("hoá thạch") >= 0 || t.indexOf("hóa thạch") >= 0 || t.indexOf("xương") >= 0) {
            return new int[]{KieuO.HINH_MA, 8};
        }
        if (t.indexOf("đá") >= 0 || t.indexOf("cuội") >= 0 || t.indexOf("mảnh") >= 0 && t.indexOf("sao") < 0) {
            return new int[]{KieuO.HINH_THEP, 12};
        }
        if (t.indexOf("sao") >= 0) {
            return new int[]{KieuO.HINH_SAO, 10};
        }
        if (t.indexOf("vảy") >= 0 || t.indexOf("tim") >= 0) {
            return new int[]{KieuO.HINH_TIM, 17};
        }
        if (t.indexOf("bánh") >= 0 || t.indexOf("súp") >= 0 || t.indexOf("cà ri") >= 0 || t.indexOf("mồi") >= 0) {
            return new int[]{KieuO.HINH_LUA, 1};
        }
        if (t.indexOf("thuốc") >= 0) {
            return new int[]{KieuO.HINH_CONG, 17};
        }
        if (t.indexOf("trang trí") >= 0 || t.indexOf("tượng") >= 0 || t.indexOf("đèn") >= 0) {
            return new int[]{KieuO.HINH_KHIEN, 16};
        }
        return new int[]{KieuO.HINH_GIOT, 2};     // cá (tên loài Pokémon) và còn lại
    }

    public void paint(Graphics g) {
        Canvas.gameScr.paint(g);
        Canvas.resetTrans(g);
        int s = o();
        int cot = soCot();
        int hang = soHang();
        int x0 = x0();
        int y0 = y0();
        int w = cot * s + 16;
        int h = hang * s + 70;
        GiaoDien.bangTieuDe(g, x0 - 8, y0 - 30, w, h, 26, "Túi sinh thái");
        int n = this.ten == null ? 0 : this.ten.length;
        if (this.chon / cot < this.cuon) {
            this.cuon = this.chon / cot;
        } else if (this.chon / cot >= this.cuon + hang) {
            this.cuon = this.chon / cot - hang + 1;
        }
        for (int r = 0; r < hang; r++) {
            for (int c = 0; c < cot; c++) {
                int i = (r + this.cuon) * cot + c;
                int x = x0 + c * s;
                int y = y0 + r * s;
                KieuO.oDo(g, x, y, s, i < n, ((r + c) & 1) == 1);
                if (i >= n) {
                    continue;
                }
                int[] k = kieu(this.ten[i]);
                KieuO.oChieu(g, x + 3, y + 3, s - 6, s - 6, k[1], k[0], false);
                KieuO.soLuong(g, x + s - 1, y + s - 1, KieuO.soGon(this.soLuong[i]));
                // bậc: chấm vàng góc trên-trái
                for (int b = 0; b < this.bac[i] && b < 5; b++) {
                    g.setColor(GiaoDien.VIEN);
                    g.fillRect(x + 2 + b * 3, y + 1, 3, 3);
                    g.setColor(GiaoDien.VANG);
                    g.fillRect(x + 3 + b * 3, y + 2, 1, 1);
                }
            }
        }
        if (n > 0) {
            int c = this.chon % cot;
            int r = this.chon / cot - this.cuon;
            KieuO.chonO(g, x0 + c * s, y0 + r * s, s);
        }
        KieuO.thanhCuon(g, x0 + cot * s + 2, y0, hang * s, (n + cot - 1) / cot, this.cuon, hang);
        // thẻ chi tiết
        int yThe = y0 + hang * s + 4;
        GiaoDien.khoi(g, x0 - 2, yThe, cot * s + 4, 32, 4, GiaoDien.XANH);
        GiaoDien.khoiDoc(g, x0 - 1, yThe + 1, cot * s + 2, 30, 3, GiaoDien.TRANG, GiaoDien.HANG_CHAN);
        if (n == 0) {
            Font.arialFontW.drawString(g, "Túi trống", Canvas.hw, yThe + 9, Font.CENTER);
        } else {
            String tn = GiaoDien.catChu(Font.normalFont[0], this.ten[this.chon], cot * s - 4);
            Font.normalFont[0].drawString(g, tn, x0 + 2, yThe + 2, Font.LEFT);
            Font.arialFontW.drawString(g, new StringBuffer("Bậc ").append(this.bac[this.chon]).append("  -  Số lượng: ").append(this.soLuong[this.chon]).toString(), x0 + 2, yThe + 17, Font.LEFT);
        }
        super.paint(g);
    }

    public void update() {
        Canvas.gameScr.update();
    }

    public void updateKey() {
        int n = this.ten == null ? 0 : this.ten.length;
        int cot = soCot();
        if (n > 0) {
            if (Canvas.isKeyPressed(4)) {
                this.chon = this.chon > 0 ? this.chon - 1 : n - 1;
            } else if (Canvas.isKeyPressed(6)) {
                this.chon = this.chon < n - 1 ? this.chon + 1 : 0;
            } else if (Canvas.isKeyPressed(2)) {
                this.chon = this.chon - cot >= 0 ? this.chon - cot : this.chon;
            } else if (Canvas.isKeyPressed(8)) {
                this.chon = this.chon + cot < n ? this.chon + cot : this.chon;
            }
            if (Canvas.isPointerClick) {
                int s = o();
                int c = (Canvas.px - x0()) / s;
                int r = (Canvas.py - y0()) / s;
                if (Canvas.px >= x0() && c < cot && Canvas.py >= y0() && r < soHang()) {
                    int i = (r + this.cuon) * cot + c;
                    if (i < n) {
                        Canvas.isPointerClick = false;
                        if (i == this.chon) {
                            chonO();
                            return;
                        }
                        this.chon = i;
                    }
                }
            }
        }
        super.updateKey();
    }
}
