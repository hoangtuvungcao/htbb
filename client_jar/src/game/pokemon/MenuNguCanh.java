package game.pokemon;

import game.model.Actor;
import game.model.Command;
import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.GameScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * MENU NGỮ CẢNH (0.9.3; 0.9.7 kiểu NRO): chạm NPC / người chơi / ô đất... -> HỘP GỖ viền vàng neo cạnh mục tiêu
 * (không chồng HUD): NPC có hình = ô chân dung khung vàng + biển tên đỏ trên đầu hộp; mỗi lệnh một NÚT KHỐI giấy da
 * xếp chồng (icon pixel trái + nhãn), tối đa 6 nút mỗi trang ("Thêm" sang trang sau), thay danh sách chữ + phím mềm
 * "Chọn / Đóng" của J2ME.
 *
 * Không sửa từng chỗ gọi: GameScr.update gọi tiepQuan() mỗi nhịp - menu chữ (Canvas.menu) vừa mở trên MÀN CHƠI ở
 * máy cảm ứng / chuột thì lấy danh sách lệnh của nó, tắt nó, mở bảng này (lệnh con mở menu chữ lại được tiếp quản
 * như vậy). Máy J2ME chỉ có phím vẫn dùng menu chữ cũ. Phím: lên / xuống chọn, OK làm, Back / Esc đóng.
 */
public final class MenuNguCanh extends Dialog {
    static MenuNguCanh me;
    public static final int TOI_DA = 6;

    Vector lenh;
    int trang;
    int chon;
    String tieuDe;
    int mx = -1, my = -1;
    /** số Pokédex của NPC đang nói (chân dung), -1 = không có hình */
    int dexNpc = -1;
    /** chấm báo mới theo hàng lệnh (ChamMoi) - null = không */
    public boolean[] cham;

    public static MenuNguCanh gI() {
        if (me == null) {
            me = new MenuNguCanh();
        }
        return me;
    }

    /** Máy có con trỏ (APK / PC) dùng menu nút to. */
    public static boolean bat() {
        return Canvas.instance != null && Canvas.instance.hasPointerEvents();
    }

    /** GameScr.update: tiếp quản menu chữ vừa mở trên màn chơi. */
    public static void tiepQuan() {
        if (!bat() || Canvas.menu == null || !Canvas.menu.showMenu || Canvas.currentScreen != Canvas.gameScr) {
            return;
        }
        if (Canvas.currentDialog != null && Canvas.currentDialog != me) {
            return;
        }
        Vector v = Canvas.menu.menuItems;
        if (v == null || v.size() == 0) {
            return;
        }
        Canvas.menu.showMenu = false;
        mo(v);
    }

    public static void mo(Vector lenh) {
        mo(lenh, true);
    }

    /**
     * 0.9.5: bỏ mục TRÙNG (cùng một việc) - màn cũ đôi khi thêm hai lần cùng một lệnh. Giữ mục đầu. (Cùng chữ mà
     * khác việc - vd "Nói chuyện" của hai nhiệm vụ - vẫn giữ cả hai.)
     */
    public static Vector boTrung(Vector lenh) {
        if (lenh == null) {
            return null;
        }
        Vector r = new Vector();
        for (int i = 0; i < lenh.size(); i++) {
            Object o = lenh.elementAt(i);
            boolean trung = false;
            if (o instanceof game.model.Command) {
                game.model.Command c = (game.model.Command) o;
                for (int k = 0; k < r.size() && !trung; k++) {
                    Object q = r.elementAt(k);
                    if (q instanceof game.model.Command) {
                        game.model.Command d = (game.model.Command) q;
                        trung = c.action != null && c.action == d.action;
                    }
                }
            }
            if (!trung) {
                r.addElement(o);
            }
        }
        return r;
    }

    /** ganMucTieu = đặt cạnh mục tiêu đang chọn trên bản đồ (false: giữa đáy màn - mở từ bảng). */
    public static void mo(Vector lenh, boolean ganMucTieu) {
        MenuNguCanh m = gI();
        m.lenh = boTrung(lenh);
        m.trang = 0;
        m.chon = 0;
        m.left = null;
        m.center = null;
        m.right = null;
        GameScr gs = Canvas.gameScr;
        Actor a = gs == null || !ganMucTieu || Canvas.currentScreen != gs ? null : gs.focusedActor;
        m.tieuDe = null;
        m.cham = null;
        m.mx = -1;
        m.dexNpc = -1;
        if (a instanceof game.model.NPC) {
            try {
                m.dexNpc = ((game.model.NPC) a).getPokeDex();
            } catch (Throwable e) {
                m.dexNpc = -1;
            }
        }
        if (a != null) {
            try {
                m.tieuDe = a.getDisplayName();
            } catch (Throwable e) {
                m.tieuDe = null;
            }
            // toạ độ màn của mục tiêu: thế giới -> màn theo tỉ lệ vùng nhìn (camera cố định phóng 2 ở APK / PC)
            float z = GameScr.gsw > 0 ? Canvas.w / (float) GameScr.gsw : 1f;
            m.mx = (int) ((a.x - GameScr.cmx) * z);
            m.my = (int) ((a.y - GameScr.cmy) * z);
        }
        m.show();
        AmThanh.phat(AmThanh.MO);
    }

    /** 0.9.6: thẻ lệnh có tiêu đề riêng, giữa đáy (mở từ Menu: Thư/Quà...). */
    public static void mo(Vector lenh, String tieuDe) {
        mo(lenh, false);
        gI().tieuDe = tieuDe;
    }

    void dong() {
        if (Canvas.currentDialog == this) {
            Canvas.currentDialog = null;
        }
    }

    void lam(int i) {
        if (this.lenh == null || i < 0 || i >= this.lenh.size()) {
            return;
        }
        Command c = (Command) this.lenh.elementAt(i);
        dong();
        Canvas.clearKeyPressed();   // 0.9.4: phím chọn không lọt xuống màn chơi (bỏ qua lời thoại đầu)
        AmThanh.phat(AmThanh.XAC_NHAN);
        if (c.action != null) {
            c.action.perform();
        }
    }

    int soTrang() {
        int n = this.lenh == null ? 0 : this.lenh.size();
        return n <= TOI_DA ? 1 : (n + TOI_DA - 2) / (TOI_DA - 1);
    }

    /** Lệnh trên trang hiện tại: chỉ số trong lenh, -1 = nút "Thêm". */
    int[] dsTrang() {
        int n = this.lenh.size();
        if (n <= TOI_DA) {
            int[] r = new int[n];
            for (int i = 0; i < n; i++) {
                r[i] = i;
            }
            return r;
        }
        int moi = TOI_DA - 1;
        int dau = this.trang * moi;
        int so = Math.min(moi, n - dau);
        int[] r = new int[so + 1];
        for (int i = 0; i < so; i++) {
            r[i] = dau + i;
        }
        r[so] = -1;
        return r;
    }

    /** Khung thẻ vừa vẽ {x, y, w, h} (kiểm thử: trong vùng an toàn, không chồng HUD). */
    public static final int[] VUA_VE = new int[4];

    /**
     * 0.9.6 (kiểu GO): THẺ TRẮNG nhỏ neo cạnh mục tiêu - tên đậm trên, mỗi lệnh một hàng (đĩa icon màu + nhãn), mũi
     * nhọn chỉ về mục tiêu. Vị trí: thử phải / trái / trên / dưới mục tiêu, chọn chỗ KHÔNG chồng phần HUD nào (thẻ trạng
     * thái, nút trên, bản đồ nhỏ, cụm chiêu, chat...) và nằm trong vùng an toàn; cao quá thì xếp 2 cột.
     */
    public void paint(Graphics g) {
        if (this.lenh == null) {
            dong();
            return;
        }
        Canvas.resetTrans(g);
        // phủ nhẹ: chạm ra ngoài = đóng
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, 0x40120804);
        VungCham.them(g, 0, 0, Canvas.w, Canvas.h, new IAction() {
            public void perform() {
                MenuNguCanh.this.dong();
            }
        }, "nen-ngoai");
        int[] ds = dsTrang();
        int k = Bang.cach();
        int nh = Bang.caoHang();
        int ic = nh - 10;
        int wNhan = 0;
        for (int i = 0; i < ds.length; i++) {
            wNhan = Math.max(wNhan, ChuDe.rong(ds[i] < 0 ? "Thêm" : chu(ds[i]), ChuDe.NHAN));
        }
        int L = Bang.trai(), T = Bang.tren(), R = Bang.phai(), B = Bang.duoi();
        int wc = Math.max(ChuDe.dt() ? 170 : 140, Math.min(ChuDe.dt() ? 260 : 220, wNhan + ic + 30));
        String tieu = this.tieuDe != null && this.tieuDe.length() > 0 ? this.tieuDe : null;
        // 0.9.7: đầu hộp = ô chân dung NPC (nếu có hình) + biển tên đỏ; không hình = biển tên giữa
        boolean chanDung = this.dexNpc > 0 && tieu != null;
        int sCd = ChuDe.dt() ? 44 : 38;
        int hBien = ChuDe.cao(ChuDe.TIEU_DE) + 6;
        int hTieu = chanDung ? sCd + 10 : tieu != null ? hBien + 10 : 6;
        if (tieu != null) {
            wc = Math.max(wc, Math.min(ChuDe.dt() ? 300 : 260, ChuDe.rong(tieu, ChuDe.TIEU_DE) + (chanDung ? sCd + 30 : 36)));
        }
        int cot = 1;
        int hang = ds.length;
        if (hTieu + hang * (nh + k) + 6 > B - T - 4 && ds.length > 1) {
            cot = 2;
            hang = (ds.length + 1) / 2;
        }
        int w = cot * wc + (cot - 1) * k + 12, h = hTieu + hang * (nh + k) - k + 8;
        int[] o = viTri(w, h, L, T, R, B);
        if (o[3] > 0 && cot == 1 && ds.length > 1) {
            // một cột không có chỗ trống không chồng HUD: thử 2 cột (thẻ thấp hơn)
            int hang2 = (ds.length + 1) / 2, w2 = 2 * wc + k + 12, h2 = hTieu + hang2 * (nh + k) - k + 8;
            int[] o2 = viTri(w2, h2, L, T, R, B);
            if (o2[3] < o[3]) {
                cot = 2;
                hang = hang2;
                w = w2;
                h = h2;
                o = o2;
            }
        }
        int x = o[0], y = o[1];
        VUA_VE[0] = x;
        VUA_VE[1] = y;
        VUA_VE[2] = w;
        VUA_VE[3] = h;
        // mũi nhọn về mục tiêu (bên trái / phải hộp): tam giác giấy da viền nâu
        if (this.mx >= 0 && (this.mx < x || this.mx > x + w)) {
            int yt = Math.max(y + 14, Math.min(y + h - 14, this.my));
            g.setColor(ChuDe.VIEN);
            if (this.mx < x) {
                g.fillTriangle(x - 8, yt, x + 2, yt - 8, x + 2, yt + 8);
                g.setColor(ChuDe.VANG);
                g.fillTriangle(x - 5, yt, x + 2, yt - 5, x + 2, yt + 5);
            } else {
                g.fillTriangle(x + w + 8, yt, x + w - 2, yt - 8, x + w - 2, yt + 8);
                g.setColor(ChuDe.VANG);
                g.fillTriangle(x + w + 5, yt, x + w - 2, yt - 5, x + w - 2, yt + 5);
            }
        }
        KhungNro.bong(g, x, y, w, h);
        if (!KhungNro.ve(g, KhungNroBang.HOP, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 4, ChuDe.VIEN);
            GiaoDien.khoi(g, x + 2, y + 2, w - 4, h - 4, 3, ChuDe.NEN);
        }
        Font.nenSang = true;
        if (chanDung) {
            int px = x + 7, py = y + 6;
            if (!KhungNro.ve(g, KhungNroBang.O_CHON, px, py, sCd, sCd)) {
                GiaoDien.khoi(g, px, py, sCd, sCd, 3, ChuDe.VANG);
            }
            int kx = g.getClipX(), ky = g.getClipY(), kw = g.getClipWidth(), kh = g.getClipHeight();
            g.clipRect(px + 4, py + 4, sCd - 8, sCd - 8);
            PokeBody.paintChanDung(g, this.dexNpc, false, px + sCd / 2, py + sCd / 2 + 1, sCd - 8);
            Canvas.resetTrans(g);
            g.setClip(kx, ky, kw, kh);
            int xb = px + sCd + 6, wb = x + w - 8 - xb;
            int yb = py + (sCd - hBien) / 2;
            ChuDe.bien(g, xb, yb, wb, hBien);
            ChuDe.chuVienGiua(g, ChuDe.cat(tieu, ChuDe.TIEU_DE, wb - 24), xb + wb / 2, yb, hBien, ChuDe.TIEU_DE, 0xFFFFFF, ChuDe.VIEN, 2);
        } else if (tieu != null) {
            int wb = Math.min(w - 16, ChuDe.rong(tieu, ChuDe.TIEU_DE) + 40);
            ChuDe.bien(g, x + (w - wb) / 2, y + 6, wb, hBien);
            ChuDe.chuVienGiua(g, ChuDe.cat(tieu, ChuDe.TIEU_DE, wb - 24), x + w / 2, y + 6, hBien, ChuDe.TIEU_DE, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        for (int i = 0; i < ds.length; i++) {
            final int li = ds[i];
            int c = i / hang, r = i % hang;
            int xr = x + 6 + c * (wc + k), yr = y + hTieu + r * (nh + k);
            nutLenh(g, xr, yr, wc, nh, li < 0 ? "Thêm" : chu(li), i == this.chon, new IAction() {
                public void perform() {
                    if (li < 0) {
                        MenuNguCanh.this.trang = (MenuNguCanh.this.trang + 1) % MenuNguCanh.this.soTrang();
                        MenuNguCanh.this.chon = 0;
                    } else {
                        MenuNguCanh.this.lam(li);
                    }
                }
            }, i);
            if (this.cham != null && li >= 0 && li < this.cham.length && this.cham[li]) {
                ChuDe.cham(g, xr + wc - 10, yr + nh / 2);
            }
        }
    }

    /** Vị trí thẻ (w x h): gần mục tiêu, trong vùng an toàn, ít chồng HUD nhất. */
    int[] viTri(int w, int h, int L, int T, int R, int B) {
        Object[][] hud = null;
        try {
            if (game.render.screen.HudRong.bat() && Canvas.gameScr != null) {
                hud = game.render.screen.HudRong.cacVung(Canvas.gameScr);
            }
        } catch (Throwable e) {
            hud = null;
        }
        boolean coMt = this.mx >= 0 && this.mx < Canvas.w && this.my >= 0 && this.my < Canvas.h;
        int ax = coMt ? this.mx : Canvas.hw, ay = coMt ? this.my : B - h / 2;
        int[] xs = {ax + 26, ax - 26 - w, ax - w / 2, ax - w / 2, (L + R - w) / 2};
        int[] ys = {ay - h / 2, ay - h / 2, ay - 30 - h, ay + 20, (T + B - h) / 2};
        int tot = -1;
        long diemTot = Long.MAX_VALUE;
        int bx = 0, by = 0;
        long chongTot = 0;
        for (int i = 0; i < xs.length; i++) {
            for (int dy = 0; dy < 5; dy++) {
                int x = Math.max(L + 2, Math.min(R - 2 - w, xs[i]));
                int y = Math.max(T + 2, Math.min(B - 2 - h, ys[i] + (dy == 0 ? 0 : (dy % 2 == 1 ? 1 : -1) * ((dy + 1) / 2) * h / 3)));
                long chong = 0;
                for (int q = 0; hud != null && q < hud.length; q++) {
                    String ten = (String) hud[q][0];
                    if ("ten_chieu".equals(ten) || "muc_tieu".equals(ten)) {
                        continue;
                    }
                    int[] a = (int[]) hud[q][1];
                    int ox = Math.min(x + w, a[0] + a[2]) - Math.max(x, a[0]);
                    int oy = Math.min(y + h, a[1] + a[3]) - Math.max(y, a[1]);
                    if (ox > 0 && oy > 0) {
                        chong += (long) ox * oy;
                    }
                }
                // che mục tiêu cũng tính phạt (thẻ không đè lên NPC)
                if (coMt && ax >= x - 8 && ax <= x + w + 8 && ay >= y - 8 && ay <= y + h + 8) {
                    chong += 400;
                }
                long diem = chong * 1000 + Math.abs(x + w / 2 - ax) + Math.abs(y + h / 2 - ay) + i * 4;
                if (diem < diemTot) {
                    diemTot = diem;
                    chongTot = chong;
                    tot = i;
                    bx = x;
                    by = y;
                }
            }
        }
        return new int[]{bx, by, tot, (int) Math.min(Integer.MAX_VALUE, chongTot)};
    }

    String chu(int i) {
        Command c = (Command) this.lenh.elementAt(i);
        return c.caption == null ? "" : ChuPokemon.D(c.caption);
    }

    /** Biểu tượng + màu theo chữ lệnh. */
    static int[] bieuLenh(String chu) {
        String t = ChuPokemon.thuong(chu == null ? "" : chu);
        String[] khoa = {"nói chuyện", "trò chuyện", "hỏi", "nhắn", "chat", "mua", "bán", "cửa hàng", "nhiệm vụ", "giao dịch",
            "đổi", "đội", "nhóm", "bạn", "thêm", "thông tin", "xem", "pk", "đấu", "thách", "hồi", "dịch chuyển", "đi tới",
            "bản đồ", "nhận", "quà", "học", "chiêu", "gửi", "thoát", "huỷ", "hủy", "hộp thư", "giftcode"};
        int[][] kq = {{BieuTuong.CHAT, ChuDe.XANH}, {BieuTuong.CHAT, ChuDe.XANH}, {BieuTuong.CHAT, ChuDe.XANH},
            {BieuTuong.CHAT, ChuDe.XANH}, {BieuTuong.CHAT, ChuDe.XANH}, {BieuTuong.CUA_HANG, ChuDe.CHINH},
            {BieuTuong.CUA_HANG, ChuDe.CHINH}, {BieuTuong.CUA_HANG, ChuDe.CHINH}, {BieuTuong.NHIEM_VU, ChuDe.NUT_NV},
            {BieuTuong.XU, ChuDe.CAM}, {BieuTuong.XU, ChuDe.CAM}, {BieuTuong.DOI, ChuDe.LA}, {BieuTuong.DOI, ChuDe.LA},
            {BieuTuong.DOI, ChuDe.HONG}, {BieuTuong.TIEN_ICH, ChuDe.XAM}, {BieuTuong.TIN, ChuDe.XANH},
            {BieuTuong.TIN, ChuDe.XANH}, {BieuTuong.SU_KIEN, ChuDe.DO}, {BieuTuong.SU_KIEN, ChuDe.DO},
            {BieuTuong.SU_KIEN, ChuDe.DO}, {BieuTuong.THUOC, ChuDe.LA}, {BieuTuong.BAN_DO, ChuDe.CHINH},
            {BieuTuong.BAN_DO, ChuDe.CHINH}, {BieuTuong.BAN_DO, ChuDe.CHINH}, {BieuTuong.QUA, ChuDe.VANG_TOI},
            {BieuTuong.QUA, ChuDe.VANG_TOI}, {BieuTuong.CHIEU, ChuDe.TIM}, {BieuTuong.CHIEU, ChuDe.TIM},
            {BieuTuong.THU, ChuDe.XANH}, {BieuTuong.DONG, ChuDe.XAM}, {BieuTuong.DONG, ChuDe.XAM}, {BieuTuong.DONG, ChuDe.XAM},
            {BieuTuong.THU, ChuDe.XANH}, {BieuTuong.QUA, ChuDe.VANG_TOI}};
        if (KhoaMap.laHangKhoa(chu)) {
            return new int[]{BieuTuong.KHOA, ChuDe.XAM};   // điểm đến bị khoá (gói -108 / menu bay của server)
        }
        int dai = 0;
        int[] r = {BieuTuong.POKEBALL, ChuDe.DO};
        for (int i = 0; i < khoa.length; i++) {
            if (khoa[i].length() > dai && t.indexOf(khoa[i]) >= 0) {
                r = kq[i];
                dai = khoa[i].length();
            }
        }
        return r;
    }

    /** Một lệnh (0.9.7): NÚT KHỐI giấy da (chọn bằng phím = viền vàng; nhấn = lõm) + icon pixel trái + nhãn. */
    static void nutLenh(Graphics g, int x, int y, int w, int h, String chu, boolean chon, Object viec, int i) {
        boolean an = viec != null && Bang.dangAn(g, x, y, w, h);
        int m = chon && !an ? KhungNroBang.HANG_CHON : an ? KhungNroBang.NUT_GIAY_NHAN : KhungNroBang.NUT_GIAY;
        if (!an) {
            KhungNro.bong(g, x, y, w, h);
        }
        if (!KhungNro.ve(g, m, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, chon ? ChuDe.VANG : ChuDe.VIEN);
            GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 2, an ? ChuDe.NEN_PHU : ChuDe.NEN);
        }
        int dy = an ? 1 : 0;
        int s = Math.min(h - 6, 16);
        int[] b = bieuLenh(chu);
        int cx = x + 8 + s / 2, cy = y + h / 2 + dy;
        if (!KhungNro.iconGiua(g, b[0], cx, cy, 1, -1)) {
            BieuTuong.ve(g, b[0], cx - s / 2, cy - s / 2, s, b[1], ChuDe.NEN);
        }
        ChuDe.chuGiua(g, ChuDe.cat(chu, ChuDe.NHAN, w - s - 26), x + s + 16, y + dy, h, ChuDe.NHAN,
                b[0] == BieuTuong.KHOA ? ChuDe.CHU_NHAT : ChuDe.CHU, 0);
        VungCham.them(g, x, y, w, h, viec, "lenh-" + i);
    }


    public void keyPress(int keyCode) {
        int[] ds = this.lenh == null ? new int[0] : dsTrang();
        if (keyCode == -1 || keyCode == 50) {
            this.chon = (this.chon + ds.length - 1) % Math.max(1, ds.length);
        } else if (keyCode == -2 || keyCode == 56) {
            this.chon = (this.chon + 1) % Math.max(1, ds.length);
        } else if (keyCode == -5 || keyCode == 10 || keyCode == 13 || keyCode == 53) {
            if (this.chon < ds.length) {
                if (ds[this.chon] < 0) {
                    this.trang = (this.trang + 1) % soTrang();
                    this.chon = 0;
                } else {
                    lam(ds[this.chon]);
                }
            }
        } else if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
            dong();
        }
    }

    public void update() {
    }
}
