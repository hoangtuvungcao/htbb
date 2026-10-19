package game.render.screen;

import game.model.IAction;
import game.pokemon.Bang;
import game.pokemon.ChuDe;
import game.pokemon.GiaoDien;
import game.pokemon.KieuO;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * "Túi sinh thái" dạng lưới ô kiểu Pokémon (thay menu danh sách tối đa 40 dòng của server).
 * Server (HTBB HeSinhThaiMenu.moTui) gửi menu -30: NPC ảo -528, idMenu 2, mỗi dòng
 * "Tên (bậc N) xSố". Chọn ô = gửi đúng lựa chọn đó như menu cũ (server mở menu Dùng / Bán / Rao...).
 * Mở từ menu Bản thân -> "Túi sinh thái" (gửi lựa chọn 4 của menu gốc -528 như khi bấm trong menu
 * Hệ sinh thái). Server không bật hệ sinh thái thì không có gói -> không có gì xảy ra.
 *
 * 0.9.6.1: TẤM chung (ManBang / Bang.khungTam) thay bảng J2ME cao kín màn: canh giữa vùng an toàn, rộng <= ~78 %,
 * cao THEO SỐ Ô (hàng = số món / số cột), dài hơn chỗ trống thì lưới cuộn bên trong; ô chạm cỡ >= 44 dp (VungCham),
 * thẻ chi tiết + nút "Chọn" dưới lưới. Chạm ô = chọn, chạm lại / "Chọn" / phím 5 = gửi lựa chọn như menu cũ.
 */
public class TuiSinhThaiScr extends game.pokemon.ManBang {
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
    private Screen truoc;
    /** bố cục lần vẽ trước (phím lên / xuống giữ ô chọn trong khung nhìn) */
    private int cotVe = 8, hLuoiVe;

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
            moBang();
        }
    }

    public void dong() {
        game.pokemon.AmThanh.phat(game.pokemon.AmThanh.XAC_NHAN);
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

    protected String tieuDe() {
        return "Túi sinh thái";
    }

    /** Cạnh ô (px logic): >= cỡ chạm tối thiểu. */
    private static int o() {
        return Bang.co(ChuDe.dt() ? 40 : 34);
    }

    private static int soCot(int w) {
        int k = Bang.cach();
        int c = (w - 10 + k) / (o() + k);
        return c < 4 ? 4 : (c > 8 ? 8 : c);
    }

    /** Thẻ chi tiết dưới lưới: tên + bậc / số lượng, nút "Chọn". */
    private static int caoThe() {
        return Math.max(Bang.co(26) + 2 * ChuDe.DEM, ChuDe.cao(ChuDe.NHAN) + ChuDe.cao(ChuDe.PHU) + 14);
    }

    private int so() {
        return this.ten == null ? 0 : this.ten.length;
    }

    protected int rongMuon() {
        int k = Bang.cach();
        return 8 * (o() + k) - k + 10 + 2 * ChuDe.DEM;
    }

    protected int caoMuon(int wNoi) {
        int k = Bang.cach();
        int cot = soCot(wNoi);
        int hang = Math.max(1, (so() + cot - 1) / cot);
        return hang * (o() + k) - k + k + caoThe() + 2;
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

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        final int n = so();
        int k = Bang.cach();
        int hThe = caoThe();
        if (n == 0) {
            Bang.trong(g, x, y, w, h, "Túi trống", "Câu cá, đào, trồng berry để có nguyên liệu.");
            return;
        }
        if (this.chon >= n) {
            this.chon = n - 1;
        }
        final int s = o();
        final int cot = soCot(w);
        int hang = (n + cot - 1) / cot;
        int wLuoi = cot * (s + k) - k + 10;
        int hLuoi = Math.max(s, h - hThe - k);
        this.cotVe = cot;
        this.hLuoiVe = hLuoi;
        int xl = x + (w - wLuoi) / 2;
        Bang.danhSach(g, this.ds, xl, y, wLuoi, hLuoi, hang, s, new Bang.Hang() {
            public void ve(Graphics g, int r, int x, int y, int w, int h) {
                int kk = Bang.cach();
                for (int c = 0; c < cot; c++) {
                    final int i = r * cot + c;
                    if (i >= n) {
                        break;
                    }
                    int xi = x + c * (s + kk);
                    Bang.oDo(g, xi, y, s, true, i == TuiSinhThaiScr.this.chon, new IAction() {
                        public void perform() {
                            if (i == TuiSinhThaiScr.this.chon) {
                                TuiSinhThaiScr.this.chonO();
                            } else {
                                TuiSinhThaiScr.this.chon = i;
                            }
                        }
                    }, "tui-" + i);
                    int[] kq = kieu(TuiSinhThaiScr.this.ten[i]);
                    int p = Math.max(3, s / 8);
                    KieuO.oChieu(g, xi + p, y + p, s - 2 * p, s - 2 * p, kq[1], kq[0], false);
                    KieuO.soLuong(g, xi + s - 2, y + s - 2, KieuO.soGon(TuiSinhThaiScr.this.soLuong[i]));
                    // bậc: chấm vàng góc trên-trái
                    for (int b = 0; b < TuiSinhThaiScr.this.bac[i] && b < 5; b++) {
                        g.setColor(GiaoDien.VIEN);
                        g.fillRect(xi + 3 + b * 4, y + 2, 4, 4);
                        g.setColor(GiaoDien.VANG);
                        g.fillRect(xi + 4 + b * 4, y + 3, 2, 2);
                    }
                }
            }
        });
        // thẻ chi tiết món đang chọn + nút gửi lựa chọn
        int yThe = y + h - hThe;
        Bang.the(g, x, yThe, w, hThe);
        int hn = Bang.co(26);
        int wn = Math.max(hn * 2, ChuDe.rong("Chọn", ChuDe.SO) + 32);
        Bang.nut(g, x + w - ChuDe.DEM - wn, yThe + (hThe - hn) / 2, wn, hn, "Chọn", Bang.CHINH, new IAction() {
            public void perform() {
                TuiSinhThaiScr.this.chonO();
            }
        }, "tui-chon");
        int wc = w - wn - 3 * ChuDe.DEM;
        int l1 = ChuDe.cao(ChuDe.NHAN), l2 = ChuDe.cao(ChuDe.PHU);
        int yc = yThe + (hThe - l1 - l2 - 2) / 2;
        ChuDe.chu(g, ChuDe.cat(this.ten[this.chon], ChuDe.NHAN, wc), x + ChuDe.DEM, yc, ChuDe.NHAN, ChuDe.CHU, 0);
        String phu = new StringBuffer("Bậc ").append(this.bac[this.chon]).append("  ·  Số lượng: ").append(this.soLuong[this.chon]).toString();
        ChuDe.chu(g, ChuDe.cat(phu, ChuDe.PHU, wc), x + ChuDe.DEM, yc + l1 + 2, ChuDe.PHU, ChuDe.CHU_PHU, 0);
    }

    public void update() {
        super.update();
        if (Canvas.currentScreen != this || Canvas.currentDialog != null) {
            return;
        }
        int n = so();
        if (n == 0) {
            return;
        }
        int cot = this.cotVe;
        int cu = this.chon;
        if (Canvas.isKeyPressed(4)) {
            this.chon = this.chon > 0 ? this.chon - 1 : n - 1;
        } else if (Canvas.isKeyPressed(6)) {
            this.chon = this.chon < n - 1 ? this.chon + 1 : 0;
        } else if (Canvas.isKeyPressed(2)) {
            this.chon = this.chon - cot >= 0 ? this.chon - cot : this.chon;
        } else if (Canvas.isKeyPressed(8)) {
            this.chon = this.chon + cot < n ? this.chon + cot : this.chon;
        } else if (Canvas.isKeyPressed(5)) {
            Canvas.clearKeyPressed();
            chonO();
            return;
        }
        if (this.chon != cu || Canvas.isKeyPressed(2) || Canvas.isKeyPressed(8)) {
            // giữ hàng của ô chọn trong khung nhìn lưới
            int k = Bang.cach();
            int yr = (this.chon / Math.max(1, cot)) * (o() + k);
            if (yr < this.ds.lech()) {
                this.ds.den(yr);
            } else if (yr + o() > this.ds.lech() + this.hLuoiVe) {
                this.ds.den(yr + o() - this.hLuoiVe);
            }
        }
    }
}
