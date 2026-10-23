package game.pokemon;

import game.model.Actor;
import game.model.IAction;
import game.model.ItemQuest;
import game.model.Quest;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * SỔ NHIỆM VỤ (0.9.3, bảng chung Bang): tab Đang làm | Có thể nhận. Hàng = biểu tượng + tên + tiến độ (x/y); chạm =
 * thẻ chi tiết: mô tả, thanh tiến độ từng mục tiêu, phần thưởng, nút to "Đi tới NPC" (tìm đường nếu NPC ở bản đồ
 * này) và "Huỷ nhiệm vụ" (xác nhận đỏ). Nhiệm vụ mới nhận / trả ở NPC như cũ (menu ngữ cảnh khi chạm NPC).
 */
public final class ManNhiemVu extends Screen {
    public static ManNhiemVu me;
    int tab;
    Quest chon;
    final Cuon ds = new Cuon();

    public static ManNhiemVu gI() {
        if (me == null) {
            me = new ManNhiemVu();
        }
        return me;
    }

    public static void mo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        ManNhiemVu m = gI();
        m.tab = dangLam().size() == 0 && GameScr.allQuestCanReceive.size() > 0 ? 1 : 0;
        m.chon = null;
        m.ds.den(0);
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    /** Có nhiệm vụ mới nhận được (chấm đỏ ô Nhiệm vụ ở Menu). */
    public static boolean coMoi() {
        for (int i = 0; i < GameScr.allQuestCanReceive.size(); i++) {
            Quest q = (Quest) GameScr.allQuestCanReceive.elementAt(i);
            if (q.stateQuest == 0) {
                return true;
            }
        }
        Vector d = dangLam();
        for (int i = 0; i < d.size(); i++) {
            if (xong((Quest) d.elementAt(i))) {
                return true;   // đã đủ, về trả
            }
        }
        return false;
    }

    static Vector dangLam() {
        // 0.9.7.1: ô đã gửi trả / huỷ (stateQuest 3, chờ máy chủ) ẩn ngay - máy chủ từ chối thì gửi lại danh sách
        Vector v = new Vector();
        if (GameScr.mainQuest != null && GameScr.mainQuest.stateQuest != 3) {
            v.addElement(GameScr.mainQuest);
        }
        if (GameScr.subQuest != null && GameScr.subQuest.stateQuest != 3) {
            v.addElement(GameScr.subQuest);
        }
        if (GameScr.clanQuest != null && GameScr.clanQuest.stateQuest != 3) {
            v.addElement(GameScr.clanQuest);
        }
        return v;
    }

    /** Tên NPC theo loại (NPC.NPCNAME, cùng chỉ số với Quest.idNpcReceive / idNpcResponse); null nếu ngoài bảng. */
    static String tenNpc(int loaiNpc) {
        return loaiNpc >= 0 && loaiNpc < game.model.NPC.NPCNAME.length ? game.model.NPC.NPCNAME[loaiNpc] : null;
    }

    /** "Blaziken (bản đồ này)" / "Blaziken" / "NPC nhiệm vụ". */
    static String noiNpc(int loaiNpc) {
        String t = tenNpc(loaiNpc);
        if (t == null) {
            return "NPC nhiệm vụ";
        }
        return npc(loaiNpc) != null ? t + " (bản đồ này)" : t;
    }

    /** Biểu tượng theo loại nhiệm vụ: đánh quái = chiêu, thu thập = túi, nói chuyện = chat, tiến hoá = Pokédex. */
    static int hinhLoai(Quest q) {
        switch (q.type) {
            case Quest.TYPE_KILL_MONS:
            case Quest.TYPE_KILL_MONSTER_LEVEL:
                return BieuTuong.CHIEU;
            case Quest.TYPE_GET_ITEM:
                return BieuTuong.TUI;
            case Quest.TYPE_TALK:
                return BieuTuong.CHAT;
            case Quest.TYPE_TRANSFORM:
                return BieuTuong.DEX;
            default:
                return BieuTuong.NHIEM_VU;
        }
    }

    /** Ô biểu tượng nhiệm vụ cạnh s: nền màu theo tuyến (chính đỏ, đội xanh lá, phụ xanh; xong = vàng). */
    static void veIcon(Graphics g, Quest q, int x, int y, int s) {
        int mau = xong(q) ? ChuDe.VANG : (q == GameScr.mainQuest ? ChuDe.DO : (q == GameScr.clanQuest ? ChuDe.LA : ChuDe.XANH));
        GiaoDien.khoi(g, x, y, s, s, ChuDe.BO_O, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, s - 2, s - 2, ChuDe.BO_O - 1, GiaoDien.pha(mau, GiaoDien.TRANG, 1, 3), mau);
        int k = s * 3 / 5;
        BieuTuong.ve(g, xong(q) ? BieuTuong.NHIEM_VU : hinhLoai(q), x + (s - k) / 2, y + (s - k) / 2, k, 0xFFFFFF, mau);
    }

    static String loai(Quest q) {
        if (q.laCuaAi()) {
            return "Cửa ải";
        }
        if (q.stateQuest == 0) {
            return q.main_sub == 0 ? "Chính tuyến" : (q.main_sub == 2 ? "Đội" : "Phụ");
        }
        return q == GameScr.mainQuest ? "Chính tuyến" : (q == GameScr.clanQuest ? "Đội" : "Phụ");
    }

    /**
     * 0.9.15 dòng 2 của hàng (không cần chạm): việc phải làm + nơi trả / nhận. Máy chủ mới (đuôi -64): "Hạ 20 Weedle ở
     * Lavender · Trả: Machamp (Pewter)", xong = "Xong! Về trả Machamp (Pewter)". Máy chủ cũ: như trước (+ mô tả ngắn).
     */
    public static String dongViec(Quest q, int tab) {
        if (tab != 0) {
            String nhan = (q.deltaLv > 0 ? "Cần cấp " + q.deltaLv + " · " : "") + "Nhận ở " + noiNpc(q.idNpcReceive);
            return q.coMuc() ? q.muc + " · " + nhan : nhan;
        }
        if (q.coMuc()) {
            if (xong(q)) {
                if (q.tra != null && q.tra.length() > 0) {
                    return "Xong! Về trả " + q.tra;
                }
                return q.laCuaAi() ? "Xong! Nhận thưởng ở Sổ tay (Nhiệm vụ của tôi)" : "Xong! Về trả " + noiNpc(q.idNpcResponse);
            }
            if (q.tra != null && q.tra.length() > 0) {
                return q.muc + " · Trả: " + q.tra;
            }
            return q.laCuaAi() ? q.muc + " · Nhận thưởng ở Sổ tay" : q.muc;
        }
        if (xong(q)) {
            return "Xong! Trả ở " + noiNpc(q.idNpcResponse);
        }
        return q.decript != null && q.decript.length() > 0 ? q.decript : "Báo lại: " + noiNpc(q.idNpcResponse);
    }

    /** Dòng khoá (đỏ) của hàng, null = không. */
    static String dongKhoa(Quest q) {
        return q.khoa != null && q.khoa.length() > 0 && !xong(q) ? q.khoa : null;
    }

    static final int DONG_VIEC = 3, DONG_KHOA = 3;

    /** Chữ số bên phải dòng 1 (x/y, "Xong"), null = không. */
    static String soHang(Quest q, int tab) {
        if (tab != 0) {
            return null;
        }
        int[] td = tienDo(q);
        return xong(q) ? "Xong" : (td != null ? td[0] + "/" + td[1] : null);
    }

    /** Bề ngang tên dòng 1 trong hàng rộng w. */
    static int rongTen(Quest q, int tab, int w) {
        String so = soHang(q, tab);
        return w - (Bang.caoHang() - 6) - 16 - (so == null ? 0 : Font.blackFont.getWidth(so) + 10);
    }

    /** Dòng 1 đủ chỗ "tên · loại"? Không thì loại xuống đầu dòng 2 (không cắt "Chín.."). */
    static boolean loaiDong1(Quest q, int tab, int w) {
        return Font.blackFont.getWidth(q.name + " · " + loai(q)) <= rongTen(q, tab, w);
    }

    /** Dòng 2 của hàng rộng w (loại ở đầu nếu dòng 1 hết chỗ). */
    static String dongHai(Quest q, int tab, int w) {
        return loaiDong1(q, tab, w) ? dongViec(q, tab) : loai(q) + " · " + dongViec(q, tab);
    }

    /** Bề ngang chữ dòng 2 trong hàng rộng w (ô biểu tượng theo cao hàng chuẩn). */
    static int rongChu(int w) {
        return w - (Bang.caoHang() - 6) - 14;
    }

    /** Cao từng hàng: đủ chứa mọi dòng (không cắt chữ mục tiêu / khoá), tối thiểu cao hàng chuẩn; bề ngang hàng w. */
    static int[] caoHang(Vector v, int tab, int w) {
        int[] r = new int[v.size()];
        int lh = ChuDe.cao(ChuDe.PHU);
        int wt = rongChu(w);
        for (int i = 0; i < r.length; i++) {
            Quest q = (Quest) v.elementAt(i);
            int n = ChuDe.ngat(dongHai(q, tab, w), ChuDe.PHU, wt, DONG_VIEC).length;
            String kh = dongKhoa(q);
            if (kh != null) {
                n += ChuDe.ngat(kh, ChuDe.PHU, wt, DONG_KHOA).length;
            }
            r[i] = Math.max(Bang.caoHang(), 2 + Font.blackFont.getHeight() + 2 + n * lh + 4);
        }
        return r;
    }

    /** Vẽ một hàng nhiệm vụ (icon, tên · loại, x/y, dòng việc tự xuống dòng, dòng khoá đỏ); hàng xong viền vàng. */
    static void veHang(Graphics g, Quest q, int tab, int x, int y, int w, int h) {
        int s = Bang.caoHang() - 6;
        boolean xg = tab == 0 && xong(q);
        if (xg) {
            g.setColor(ChuDe.VANG);
            g.drawRect(x + 1, y + 1, w - 3, h - 3);
            g.drawRect(x + 2, y + 2, w - 5, h - 5);
            g.fillRect(x + 3, y + 3, 3, h - 6);
        }
        veIcon(g, q, x + 3, y + 3, s);
        if (ChamMoi.hangNhiemVu(tab, q)) {
            ChuDe.cham(g, x + 3 + s - 2, y + 5);
        }
        String so = soHang(q, tab);
        int xt = x + s + 9;
        boolean l1 = loaiDong1(q, tab, w);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, l1 ? q.name + " · " + loai(q) : q.name, rongTen(q, tab, w)), xt, y + 2, 0);
        if (so != null) {
            Font.blackFont.drawString(g, so, x + w - 8, y + 2, 1);
        }
        int wt = rongChu(w);
        int lh = ChuDe.cao(ChuDe.PHU);
        int yy = y + 2 + Font.blackFont.getHeight() + 2;
        String[] d = ChuDe.ngat(dongHai(q, tab, w), ChuDe.PHU, wt, DONG_VIEC);
        for (int i = 0; i < d.length; i++) {
            ChuDe.chu(g, d[i], xt, yy, ChuDe.PHU, xg ? ChuDe.CHINH_TOI : ChuDe.CHU_PHU, 0);
            yy += lh;
        }
        String kh = dongKhoa(q);
        if (kh != null) {
            String[] k = ChuDe.ngat(kh, ChuDe.PHU, wt, DONG_KHOA);
            for (int i = 0; i < k.length; i++) {
                ChuDe.chu(g, k[i], xt, yy, ChuDe.PHU, ChuDe.DO, 0);
                yy += lh;
            }
        }
    }

    /** 0.9.15 nút cuối danh sách: mở "Nhiệm vụ của tôi" (Sổ tay: cửa ải, hằng ngày, tuần...) - chỉ máy chủ mới. */
    static boolean coNutSoTay() {
        return GameScr.coMucTieuNv;
    }

    /** {đã, cần} tổng của các mục tiêu (đánh quái / thu thập); null nếu không đếm được. */
    static int[] tienDo(Quest q) {
        try {
            if (q.stateQuest == 2 && q.type == Quest.TYPE_KILL_MONS && q.totalMonskill != null) {
                int a = 0, b = 0;
                for (int i = 0; i < q.totalMonskill.length; i++) {
                    a += Math.min(q.allMonsKilled[i], q.totalMonskill[i]);
                    b += q.totalMonskill[i];
                }
                return new int[]{a, b};
            }
            if (q.stateQuest == 2 && q.type == Quest.TYPE_GET_ITEM && q.totalItemGet != null) {
                int a = 0, b = 0;
                for (int i = 0; i < q.totalItemGet.length; i++) {
                    a += Math.min(q.allItemGet[i], q.totalItemGet[i]);
                    b += q.totalItemGet[i];
                }
                return new int[]{a, b};
            }
        } catch (Throwable e) {
            // thiếu dữ liệu
        }
        return null;
    }

    static boolean xong(Quest q) {
        int[] t = tienDo(q);
        return q.stateQuest == 1 || t != null && t[1] > 0 && t[0] >= t[1];
    }

    static Actor npc(int loaiNpc) {
        GameScr gs = Canvas.gameScr;
        for (int i = 0; gs != null && i < gs.actors.size(); i++) {
            Actor a = (Actor) gs.actors.elementAt(i);
            try {
                if (a.isNPC() && a.getTypeNpc() == loaiNpc) {
                    return a;
                }
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        return null;
    }

    void dong() {
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
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
        if (Canvas.currentScreen != this || Canvas.currentDialog != null) {
            return;
        }
        if (Canvas.isKeyPressed(4) || Canvas.isKeyPressed(6)) {
            this.tab = 1 - this.tab;
            this.chon = null;
            this.ds.den(0);
        }
        if (Canvas.isKeyPressed(2)) {
            this.ds.them(-(Bang.caoHang() + Bang.cach()));
        } else if (Canvas.isKeyPressed(8)) {
            this.ds.them(Bang.caoHang() + Bang.cach());
        }
    }

    public void updateKey() {
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        if (keyCode == -7 || keyCode == 27) {
            if (this.chon != null) {
                this.chon = null;
            } else {
                dong();
            }
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
        Object[] vt = {new IAction() {
            public void perform() {
                ManNhiemVu.this.tab = 0;
                ManNhiemVu.this.chon = null;
                ManNhiemVu.this.ds.den(0);
            }
        }, new IAction() {
            public void perform() {
                ManNhiemVu.this.tab = 1;
                ManNhiemVu.this.chon = null;
                ManNhiemVu.this.ds.den(0);
            }
        }};
        Bang.chamTab = new boolean[]{ChamMoi.tabNhiemVu(0), ChamMoi.tabNhiemVu(1)};
        int[] c = Bang.khung(g, "Nhiệm vụ", new String[]{"Đang làm", "Có thể nhận"}, this.tab, vt, new IAction() {
            public void perform() {
                ManNhiemVu.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        final Vector v = this.tab == 0 ? dangLam() : GameScr.allQuestCanReceive;
        this.chon = timLai(v, this.chon);   // 0.9.7.1: nhận / trả / huỷ xong thẻ chi tiết không còn giữ nhiệm vụ cũ
        boolean hai = w >= 400;
        int wl = hai ? w * 46 / 100 : w;
        if (!hai && this.chon != null) {
            veChiTiet(g, x, y, w, h);
            return;
        }
        Bang.the(g, x, y, wl, h);
        int hds = h;
        if (coNutSoTay()) {
            int nh0 = Bang.caoHang();
            hds = h - nh0 - k;
            Bang.nut(g, x + 4, y + h - 4 - nh0, wl - 8, nh0, "Nhiệm vụ của tôi (cửa ải, ngày, tuần)", Bang.PHU, new IAction() {
                public void perform() {
                    GameScr.moNhiemVuCuaToi();
                }
            }, "nv-so-tay");
        }
        if (v.size() == 0) {
            Bang.trong(g, x, y, wl, hds, this.tab == 0 ? "Chưa nhận nhiệm vụ" : "Chưa có nhiệm vụ mới",
                    this.tab == 0 ? "Nói chuyện với NPC có dấu chấm than để nhận nhiệm vụ." : "Lên cấp hoặc khám phá thêm bản đồ để mở nhiệm vụ mới.");
        } else {
            final int tabVe = this.tab;
            // hàng cao theo chữ (mục tiêu / khoá xuống dòng, không cắt); chừa thanh cuộn 10 px
            int[] rh = caoHang(v, tabVe, wl - 8 - 10);
            Bang.danhSachCao(g, this.ds, x + 4, y + 4, wl - 8, hds - 8, rh, new Bang.Hang() {
                public void ve(Graphics g, int i, int x, int y, int w, int h) {
                    final Quest q = (Quest) v.elementAt(i);
                    Bang.hang(g, x, y, w, h, i, q == ManNhiemVu.this.chon, new IAction() {
                        public void perform() {
                            ManNhiemVu.this.chon = q;
                        }
                    }, "nv-" + i);
                    veHang(g, q, tabVe, x, y, w, h);
                }
            });
        }
        if (hai) {
            int xr = x + wl + k, wr = w - wl - k;
            if ((this.chon == null || !v.contains(this.chon)) && v.size() > 0) {
                this.chon = (Quest) v.elementAt(0);   // hai cột: luôn có nhiệm vụ đang xem (cái đầu tiên)
            }
            if (this.chon != null && v.contains(this.chon)) {
                veChiTiet(g, xr, y, wr, h);
            } else {
                Bang.the(g, xr, y, wr, h);
                Bang.trong(g, xr, y, wr, h, "Chọn nhiệm vụ để xem", null);
            }
        }
    }

    private void veChiTiet(Graphics g, int x, int y, int w, int h) {
        final Quest q = this.chon;
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, x + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManNhiemVu.this.chon = null;
            }
        });
        veIcon(g, q, x + hd + 10, y + 4, hd);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, q.name, w - 2 * hd - 22), x + 2 * hd + 16, y + 4 + (hd - Font.blackFont.getHeight()) / 2, 0);
        int nh = Bang.caoHang();
        int yn = y + h - 6 - nh;
        int yy = y + 4 + hd + k;
        boolean dangLam = dangLam().contains(q);
        final int idNpc = dangLam ? q.idNpcResponse : q.idNpcReceive;
        if (q.coMuc() && dangLam) {
            // 0.9.15: nơi trả theo máy chủ ("Machamp (Pewter)"; cửa ải: Sổ tay), xuống dòng thay vì cắt
            String noi = q.tra != null && q.tra.length() > 0 ? (xong(q) ? "Xong! Về trả " : "Trả: ") + q.tra
                    : (q.laCuaAi() ? "Nhận thưởng: Sổ tay → Nhiệm vụ của tôi" : (xong(q) ? "Trả ở: " : "Báo lại: ") + noiNpc(idNpc));
            yy = Bang.doan(g, ChuDe.NHAN, noi, x + 10, yy, w - 20, false, xong(q) ? ChuDe.CHINH_TOI : ChuDe.CHU) + 2;
        } else {
            Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, (dangLam ? (xong(q) ? "Trả ở: " : "Báo lại: ") : "Nhận ở: ") + noiNpc(idNpc), w - 20),
                    x + 10, yy, 0);
            yy += Font.blackFont.getHeight() + 3;
        }
        // 0.9.7 NRO: chừa đáy cho HÀNG Ô PHẦN THƯỞNG (nhãn + ô cùng hàng) - mô tả / tiến độ cắt phía trên
        int wNhan = Font.blackFont.getWidth("Phần thưởng:") + 8;
        int hq2 = q.infoGift == null ? 0 : PhanThuong.cao(q.infoGift, w - 20 - wNhan);
        int gh = hq2 > 0 ? yn - k - hq2 - 2 : yn - k;
        g.setClip(x, yy, w, Math.max(0, gh - yy));
        if (q.coMuc()) {
            yy = Bang.doan(g, ChuDe.PHU, "Mục tiêu: " + q.muc, x + 10, yy, w - 20, false, ChuDe.CHU) + 2;
            if (q.khoa != null && q.khoa.length() > 0) {
                yy = Bang.doan(g, ChuDe.PHU, q.khoa, x + 10, yy, w - 20, false, ChuDe.DO) + 2;
            }
        } else {
            yy = Bang.doan(g, Font.arialFont, q.decript, x + 10, yy, w - 20, false) + 2;
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        // tiến độ từng mục tiêu
        try {
            if (q.stateQuest == 2 && q.type == Quest.TYPE_KILL_MONS && q.idMonsKill != null) {
                for (int i = 0; i < q.idMonsKill.length && yy + 14 < gh; i++) {
                    yy = thanhMuc(g, x + 10, yy, w - 20, Res.monsterTemplates[q.idMonsKill[i]].name, q.allMonsKilled[i], q.totalMonskill[i]);
                }
            } else if (q.stateQuest == 2 && q.type == Quest.TYPE_GET_ITEM && q.idItemGet != null) {
                for (int i = 0; i < q.idItemGet.length && yy + 14 < gh; i++) {
                    yy = thanhMuc(g, x + 10, yy, w - 20, ItemQuest.NAME_ITEM[q.idItemGet[i]], q.allItemGet[i], q.totalItemGet[i]);
                }
            }
        } catch (Throwable e) {
            // thiếu dữ liệu quái / vật phẩm
        }
        if (hq2 > 0) {
            // ô hộp gỗ (chạm = thẻ vật phẩm chỉ xem); chữ gốc (có cả mục không nhận ra) dưới hàng nếu còn chỗ
            int yr = Math.min(yy + 2, gh + 2);
            Font.blackFont.drawString(g, "Phần thưởng:", x + 10, yr + (Math.min(hq2, PhanThuong.coO()) - Font.blackFont.getHeight()) / 2, 0);
            int yc = yr + PhanThuong.ve(g, x + 10 + wNhan, yr, w - 20 - wNhan, q.infoGift, null) + 2;
            if (yc + Font.arialFont.getHeight() <= yn - k) {
                g.setClip(x, yc, w, yn - k - yc);
                Bang.doan(g, Font.arialFont, q.infoGift, x + 10, yc, w - 20, false);
                g.setClip(0, 0, Canvas.w, Canvas.h);
            }
        } else if (q.infoGift != null && q.infoGift.length() > 0 && yy + 14 < yn - k) {
            Font.blackFont.drawString(g, "Phần thưởng:", x + 10, yy + 2, 0);
            Bang.doan(g, Font.arialFont, q.infoGift, x + 10, yy + 4 + Font.blackFont.getHeight(), w - 20, false);
        }
        final Actor a = npc(idNpc);
        String tn = tenNpc(idNpc);
        boolean huyDuoc = dangLam && q.stateQuest != 3 && !q.laCuaAi();
        // 0.9.15: cửa ải / không NPC trả (máy chủ mới): nút phải mở Sổ tay "Nhiệm vụ của tôi"
        boolean soTay = GameScr.coMucTieuNv && dangLam && (q.laCuaAi() || q.coMuc() && (q.tra == null || q.tra.length() == 0) && a == null);
        int wb = huyDuoc ? (w - 12 - k) / 2 : w - 12;
        if (huyDuoc) {
            Bang.nut(g, x + 6, yn, wb, nh, "Huỷ nhiệm vụ", Bang.TRUNG, new IAction() {
                public void perform() {
                    HopXacNhan.hoiNguyHiem("Huỷ nhiệm vụ", "Từ bỏ " + q.name + "? Tiến độ sẽ mất.", "Huỷ nhiệm vụ", new IAction() {
                        public void perform() {
                            // 0.9.7.1: -64 huỷ (type 2) đúng nhiệm vụ. Trước đây gửi gói 52 của hệ nhiệm vụ cũ (máy chủ bỏ
                            // qua) + xoá thuốc ô 10..13 cục bộ: bấm Huỷ không huỷ gì, vào lại nhiệm vụ vẫn còn.
                            huy(q);
                            ManNhiemVu.this.chon = null;
                        }
                    });
                }
            }, "huy-nv");
        }
        if (soTay) {
            Bang.nut(g, x + w - 6 - wb, yn, wb, nh, "Mở Nhiệm vụ của tôi", Bang.XAC_NHAN, new IAction() {
                public void perform() {
                    GameScr.moNhiemVuCuaToi();
                }
            }, "di-toi");
            return;
        }
        Bang.nut(g, x + w - 6 - wb, yn, wb, nh, a != null ? "Đi tới " + (tn != null ? tn : "NPC") : (tn != null ? tn + " ở bản đồ khác" : "NPC ở bản đồ khác"), a != null ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
            public void perform() {
                try {
                    Canvas.gameScr.switchToMe();
                    Canvas.gameScr.findRoad2(a.x, a.y);
                    HanhDong.chamDat(a.x, a.y);
                } catch (Throwable e) {
                    // không tìm được đường
                }
            }
        }, "di-toi");
    }

    /** Gửi huỷ nhiệm vụ; ô sẽ được làm mới khi máy chủ trả danh sách (GameScr.onQuest). */
    static void huy(Quest q) {
        q.stateQuest = (byte) 3;   // ẩn nút Huỷ / Trả ngay, chờ máy chủ (chạm đúp không gửi hai lần)
        GameService.gI().onSendInfoQuest(2, q.idQuest, q.main_sub);
    }

    /** Nhiệm vụ đang xem đã được thay bằng bản mới từ máy chủ (cùng id / tuyến) - giữ thẻ chi tiết đúng nhiệm vụ. */
    static Quest timLai(Vector v, Quest c) {
        if (c == null || v.contains(c)) {
            return c;
        }
        for (int i = 0; i < v.size(); i++) {
            Quest q = (Quest) v.elementAt(i);
            if (q.idQuest == c.idQuest && q.main_sub == c.main_sub) {
                return q;
            }
        }
        return null;
    }

    static int thanhMuc(Graphics g, int x, int y, int w, String ten, int da, int can) {
        Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, ten, w - 60), x, y, 0);
        Font.blackFont.drawString(g, Math.min(da, can) + "/" + can, x + w, y, 1);
        int yb = y + Font.arialFont.getHeight() + 1;
        GiaoDien.thanh(g, x, yb, w, 7, Math.min(da, can), Math.max(1, can), 2);
        return yb + 10;
    }
}
