package game.pokemon;

import game.model.Command;
import game.model.IAction;
import game.render.Canvas;
import game.render.screen.TField;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * BẢNG CHAT (0.9.8, kiểu NRO / NSO): tấm 78 % với 6 tab Bản đồ | Thế giới | Bang hội | Đội | Bạn bè | Hệ thống, chấm đỏ
 * (ChamMoi) ở tab có tin chưa đọc, lịch sử mỗi tab (50 dòng, KenhChat), giờ gửi, chạm dòng = menu tên (Nhắn riêng / Mời
 * vào đội / Kết bạn / Xem thông tin / Chặn), ô nhập + chip chọn kênh + nút Gửi (Thế giới: cấp, phí xu, đếm ngược giãn
 * cách). Tab Bạn bè: danh sách bạn + người từng nhắn (online / offline, chưa đọc) -> chạm = hội thoại riêng. "Tuỳ chọn":
 * bật / tắt kênh trên dải chat HUD, danh sách chặn.
 *
 * Mở: nút chat HUD (mở lại tab dùng lần cuối), phím T / "/" (PC), menu phím # (J2ME), "Nhắn tin" ở menu người chơi / bạn
 * bè. Phím (J2ME): trái / phải đổi tab, lên / xuống chọn dòng, 5 / OK = gửi (ô có chữ) hoặc menu tên của dòng chọn, phím
 * số = gõ, phím mềm trái = Tuỳ chọn, phải = lùi / đóng. PC: Enter gửi, Esc đóng.
 * Chỉ dùng thành phần Bang / ChuDe (giao diện NRO thay token là đổi theo), không icon vẽ bằng code.
 */
public final class ManChat extends ManBang {
    public static ManChat me;
    public final TField o = new TField();
    /** kênh gửi khi đang ở tab Hệ thống (chip chọn kênh) */
    int kenhGui = KenhChat.BAN_DO;
    /** 0 = tin, 1 = tuỳ chọn */
    int xem;
    /** dòng đang chọn bằng phím (-1 = không) */
    int chon = -1;
    /** J2ME: đang gõ (true) / đang duyệt dòng (false) */
    boolean go = true;
    boolean bamDay = true;
    int phienDaVe = -1;
    final Cuon dsTin = new Cuon();
    /** kiểm thử: số dòng / vùng đã vẽ ở khung gần nhất */
    public static int soDongVe;
    public static final int[] VUA_NHAP = new int[4];
    public static final int[] VUA_GUI = new int[4];
    public static final int[] VUA_DS = new int[4];

    public static ManChat gI() {
        if (me == null) {
            me = new ManChat();
        }
        return me;
    }

    ManChat() {
        this.o.setMaxTextLenght(80);
        this.o.setIputType(0);
        this.o.isFocus = true;
    }

    /** Mở bảng ở tab dùng lần cuối. */
    public static void mo() {
        moTab(KenhChat.tabCuoi);
    }

    public static void moTab(int t) {
        if (Canvas.gameScr == null) {
            return;
        }
        ManChat m = gI();
        if (Canvas.currentScreen != m) {
            m.xem = 0;
            m.moBang();
            m.go = true;
            m.o.isFocus = true;
            m.ganPhimMem();
        }
        m.chonTab(t < 0 || t >= KenhChat.SO_KENH ? KenhChat.BAN_DO : t);
        if (t == KenhChat.RIENG) {
            KenhChat.xinBan();
        }
    }

    /** Mở hội thoại riêng với ten (menu người chơi / bạn bè "Nhắn tin"). */
    public static void moRieng(String ten) {
        moTab(KenhChat.RIENG);
        if (ten != null && ten.length() > 0) {
            KenhChat.banTao(ten);
            gI().nhanVoi(ten);
        }
    }

    void nhanVoi(String ten) {
        KenhChat.dangNhan = ten;
        KenhChat.daDocRieng(ten);
        this.bamDay = true;
        this.chon = -1;
    }

    /** J2ME bấm phím: nhãn phím mềm (máy cảm ứng / PC không vẽ). */
    void ganPhimMem() {
        boolean cham = Canvas.instance != null && Canvas.instance.hasPointerEvents();
        this.left = cham ? null : new Command("Tuỳ chọn", new IAction() {
            public void perform() {
                ManChat.this.menuTuyChon();
            }
        });
        this.right = cham ? null : new Command("Đóng", new IAction() {
            public void perform() {
                ManChat.this.lui();
            }
        });
        this.center = null;
    }

    protected String tieuDe() {
        return "Trò chuyện";
    }

    /** nhãn tab ngắn khi tấm hẹp (J2ME / máy nhỏ) */
    static final String[] TAB_NGAN = {"B.đồ", "T.giới", "Đội", "Nhóm", "Bạn", "H.thống"};

    protected String[] tabs() {
        int w = Bang.oTam(rongMuon(), 0)[2] - 2 * ChuDe.DEM;
        int wt = Math.min(w, KenhChat.SO_KENH * (ChuDe.dt() ? 150 : 110)) / KenhChat.SO_KENH - 14;
        for (int i = 0; i < KenhChat.SO_KENH; i++) {
            if (ChuDe.rong(KenhChat.tenKenh[i], ChuDe.SO) > wt) {
                return TAB_NGAN;
            }
        }
        return KenhChat.tenKenh;
    }

    protected void chonTab(int t) {
        super.chonTab(t);
        KenhChat.tabCuoi = t;
        KenhChat.dangXem = t;
        KenhChat.daDoc(t);
        if (t == KenhChat.RIENG && KenhChat.dangNhan != null) {
            KenhChat.daDocRieng(KenhChat.dangNhan);
        }
        this.bamDay = true;
        this.chon = -1;
        this.xem = 0;
        KenhChat.luuCaiDat();
    }

    protected void lui() {
        if (this.xem != 0) {
            this.xem = 0;
            return;
        }
        if (this.tab == KenhChat.RIENG && KenhChat.dangNhan != null) {
            KenhChat.dangNhan = null;
            this.chon = -1;
            return;
        }
        dong();
    }

    public void dong() {
        KenhChat.dangXem = -1;
        this.o.isFocus = false;
        super.dong();
    }

    /** Kênh sẽ gửi (theo tab; Hệ thống = chip chọn kênh). */
    public int kenhGui() {
        if (this.tab == KenhChat.HE_THONG) {
            return this.kenhGui;
        }
        return this.tab;
    }

    boolean coNhap() {
        if (this.xem != 0) {
            return false;
        }
        return this.tab != KenhChat.RIENG || KenhChat.dangNhan != null;
    }

    // ------------------------------------------------------------------ gửi

    public void gui() {
        String t = this.o.getText();
        if (t == null || t.trim().length() == 0) {
            return;
        }
        int k = kenhGui();
        String loi = KenhChat.gui(k, k == KenhChat.RIENG ? KenhChat.dangNhan : "", t);
        if (loi == null) {
            this.o.setText("");
            this.bamDay = true;
            AmThanh.phat(AmThanh.XAC_NHAN);
        } else if (loi.length() > 0) {
            KenhChat.baoCuoi = loi;
            KenhChat.baoLoi = true;
            KenhChat.lucBao = System.currentTimeMillis();
        }
    }

    // ------------------------------------------------------------------ menu

    /** Menu tên người gửi. */
    public void menuTen(final String ten, final short id) {
        if (ten == null || ten.length() == 0 || ten.equalsIgnoreCase(KenhChat.tenMinh())) {
            return;
        }
        Vector v = new Vector();
        v.addElement(new Command("Nhắn riêng", new IAction() {
            public void perform() {
                Canvas.endDlg();
                ManChat.moRieng(ten);
            }
        }));
        v.addElement(new Command("Mời vào nhóm", new IAction() {
            public void perform() {
                Canvas.endDlg();
                KenhChat.viecTheoTen(KenhChat.VIEC_MOI_DOI, ten, id);
            }
        }));
        KenhChat.Ban b = KenhChat.ban(ten);
        if (b == null || !b.laBan) {
            v.addElement(new Command("Kết bạn", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    KenhChat.viecTheoTen(KenhChat.VIEC_KET_BAN, ten, id);
                }
            }));
        }
        v.addElement(new Command("Xem thông tin", new IAction() {
            public void perform() {
                Canvas.endDlg();
                KenhChat.viecTheoTen(KenhChat.VIEC_XEM, ten, id);
            }
        }));
        final boolean daChan = KenhChat.daChan(ten);
        v.addElement(new Command(daChan ? "Bỏ chặn" : "Chặn", new IAction() {
            public void perform() {
                Canvas.endDlg();
                if (daChan) {
                    KenhChat.datChan(ten, false);
                    return;
                }
                HopXacNhan.hoiNguyHiem("Chặn " + ten, "Không nhận tin riêng và ẩn mọi tin của " + ten + "?", "Chặn", new IAction() {
                    public void perform() {
                        KenhChat.datChan(ten, true);
                    }
                });
            }
        }));
        MenuNguCanh.mo(v, ten);
    }

    /** Chip chọn kênh gửi (tab Hệ thống). */
    void menuKenh() {
        Vector v = new Vector();
        int[] ds = {KenhChat.BAN_DO, KenhChat.THE_GIOI, KenhChat.BANG, KenhChat.DOI};
        for (int i = 0; i < ds.length; i++) {
            final int k = ds[i];
            v.addElement(new Command(KenhChat.tenKenh[k], new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    ManChat.this.kenhGui = k;
                }
            }));
        }
        MenuNguCanh.mo(v, "Gửi lên kênh");
    }

    void menuTuyChon() {
        Vector v = new Vector();
        v.addElement(new Command("Tuỳ chọn hiển thị", new IAction() {
            public void perform() {
                Canvas.endDlg();
                ManChat.this.xem = 1;
                ManChat.this.ds.den(0);
            }
        }));
        v.addElement(new Command("Nhắn tới tên...", new IAction() {
            public void perform() {
                Canvas.endDlg();
                ManChat.this.hoiTen();
            }
        }));
        if (this.tab == KenhChat.HE_THONG) {
            v.addElement(new Command("Chọn kênh gửi", new IAction() {
                public void perform() {
                    Canvas.endDlg();
                    ManChat.this.menuKenh();
                }
            }));
        }
        v.addElement(new Command("Đóng bảng chat", new IAction() {
            public void perform() {
                Canvas.endDlg();
                ManChat.this.dong();
            }
        }));
        MenuNguCanh.mo(v, "Trò chuyện");
    }

    void hoiTen() {
        Canvas.inputDlg.setInfo("Nhắn tới (tên Pokémon)", new IAction() {
            public void perform() {
                String t = Canvas.inputDlg.tfInput.getText();
                Canvas.endDlg();
                if (t != null && t.trim().length() > 0) {
                    ManChat.moRieng(t.trim());
                }
            }
        }, 0, 15, true);
        Canvas.inputDlg.show();
    }

    // ------------------------------------------------------------------ phím / cập nhật

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
        if (Canvas.currentScreen != this || Canvas.currentDialog != null || Canvas.menu.showMenu) {
            this.viecSau = 0;
            return;
        }
        int vs = this.viecSau;
        this.viecSau = 0;
        if (vs == 1) {
            menuTuyChon();
            return;
        }
        if (vs == 2) {
            chonDong();
            return;
        }
        if (coNhap()) {
            try {
                this.o.update();
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        boolean dangGo = coNhap() && this.o.isFocus && this.go && this.o.getText().length() > 0;
        if (!dangGo) {
            if (Canvas.isKeyPressed(4)) {
                chonTab((this.tab + KenhChat.SO_KENH - 1) % KenhChat.SO_KENH);
            } else if (Canvas.isKeyPressed(6)) {
                chonTab((this.tab + 1) % KenhChat.SO_KENH);
            } else if (Canvas.isKeyPressed(2)) {
                doiChon(-1);
            } else if (Canvas.isKeyPressed(8)) {
                doiChon(1);
            }
        }
    }

    void doiChon(int d) {
        int n = soDongHienTai();
        if (n <= 0) {
            return;
        }
        this.go = false;
        if (this.chon < 0) {
            this.chon = d < 0 ? n - 1 : 0;
        } else {
            this.chon = Math.max(0, Math.min(n - 1, this.chon + d));
        }
        this.bamDay = false;
        this.dinhChon = true;
    }

    boolean dinhChon;
    /** việc chạy ở nhịp cập nhật sau phím: 1 menu Tuỳ chọn, 2 chọn dòng */
    int viecSau;

    int soDongHienTai() {
        if (this.xem != 0) {
            return 0;
        }
        if (this.tab == KenhChat.RIENG && KenhChat.dangNhan == null) {
            return KenhChat.dsBan.size();
        }
        return dongHienTai().size();
    }

    Vector dongHienTai() {
        if (this.tab == KenhChat.RIENG) {
            KenhChat.Ban b = KenhChat.ban(KenhChat.dangNhan);
            return b == null ? new Vector() : b.tin;
        }
        return KenhChat.dong(this.tab);
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        if (keyCode == -7 || keyCode == 27) {
            lui();
            return true;
        }
        // menu mở ở nhịp sau: Canvas.keyPressed còn đưa chính phím này cho hộp thoại vừa mở (menu tự đóng / chọn mục đầu)
        if (keyCode == -6) {
            this.viecSau = 1;
            return true;
        }
        if (keyCode == -5 || keyCode == 10 || keyCode == 13) {
            if (coNhap() && this.o.getText().trim().length() > 0) {
                gui();
            } else {
                this.viecSau = 2;
            }
            return true;
        }
        if (coNhap()) {
            boolean chu = keyCode >= 32 || keyCode == -8 || keyCode == 8;
            if (chu) {
                this.go = true;
                this.o.isFocus = true;
                this.chon = -1;
                this.o.keyPressed(keyCode);
                return true;
            }
            if ((keyCode == -3 || keyCode == -4) && this.o.isFocus && this.o.getText().length() > 0) {
                this.o.keyPressed(keyCode);   // con trỏ trong ô có chữ; ô trống: trái / phải đổi tab (update)
                return true;
            }
        }
        return false;
    }

    /** Phím chọn trên dòng đang chọn: menu tên / mở hội thoại. */
    void chonDong() {
        if (this.chon < 0) {
            if (!coNhap()) {
                doiChon(1);
            }
            return;
        }
        if (this.tab == KenhChat.RIENG && KenhChat.dangNhan == null) {
            Vector v = KenhChat.banXep();
            if (this.chon < v.size()) {
                nhanVoi(((KenhChat.Ban) v.elementAt(this.chon)).ten);
                this.go = true;
            }
            return;
        }
        Vector v = dongHienTai();
        if (this.chon < v.size()) {
            KenhChat.Dong d = (KenhChat.Dong) v.elementAt(this.chon);
            if (!d.cuaMinh() && d.kenh != KenhChat.HE_THONG) {
                menuTen(d.kenh == KenhChat.RIENG ? d.doiPhuong : d.ten, d.id);
            }
        }
    }

    // ------------------------------------------------------------------ vẽ

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        if (this.xem == 1) {
            veTuyChon(g, x, y, w, h);
            return;
        }
        int hNhap = coNhap() ? Math.max(Bang.co(26), KhungVao.caoO()) : 0;
        int yDay = y + h - (hNhap > 0 ? hNhap + k : 0);
        // dòng thông tin + Tuỳ chọn
        int hTt = Bang.co(22);
        veDauTab(g, x, y, w, hTt);
        int yDs = y + hTt + k;
        int hDs = yDay - yDs;
        if (this.tab == KenhChat.RIENG && KenhChat.dangNhan == null) {
            veDsBan(g, x, yDs, w, hDs);
        } else {
            veTin(g, x, yDs, w, hDs);
        }
        if (hNhap > 0) {
            veNhap(g, x, y + h - hNhap, w, hNhap);
        }
    }

    void veDauTab(Graphics g, int x, int y, int w, int h) {
        String chuTc = "Tuỳ chọn";
        int wTc = ChuDe.rong(chuTc, ChuDe.NHAN) + 20;
        Bang.nut(g, x + w - wTc, y, wTc, h, chuTc, Bang.PHU, new IAction() {
            public void perform() {
                ManChat.this.menuTuyChon();
            }
        }, "chat-tuy-chon");
        int xx = x;
        int wConLai = w - wTc - Bang.cach();
        if (this.tab == KenhChat.RIENG && KenhChat.dangNhan != null) {
            int s = h;
            Bang.nutLui(g, x, y, s, new IAction() {
                public void perform() {
                    KenhChat.dangNhan = null;
                    ManChat.this.chon = -1;
                }
            });
            xx += s + Bang.cach();
            KenhChat.Ban b = KenhChat.ban(KenhChat.dangNhan);
            String ten = KenhChat.dangNhan;
            boolean on = b != null && b.online;
            int wTen = Math.min(ChuDe.rong(ten, ChuDe.NHAN) + 4, wConLai - s - 70);
            final String tenNhan = ten;
            Bang.dangKy(g, xx, y, Math.max(wTen, 20), h, new IAction() {
                public void perform() {
                    ManChat.this.menuTen(tenNhan, (short) -1);
                }
            }, "chat-ten-nhan");
            ChuDe.chuGiua(g, ChuDe.cat(ten, ChuDe.NHAN, wTen), xx, y, h, ChuDe.NHAN, ChuDe.CHU, 0);
            ChuDe.chuGiua(g, on ? "Trực tuyến" : "Ngoại tuyến", xx + wTen + 8, y, h, ChuDe.PHU, on ? Bang.TANG : ChuDe.CHU_NHAT, 0);
            return;
        }
        ChuDe.chuGiua(g, ChuDe.cat(thongTinTab(), ChuDe.PHU, wConLai), xx + 2, y, h, ChuDe.PHU, ChuDe.CHU_PHU, 0);
    }

    /** Dòng thông tin luật kênh. */
    String thongTinTab() {
        switch (this.tab) {
            case KenhChat.BAN_DO:
                return "Người cùng bản đồ thấy" + (KenhChat.capKenh[0] > 0 ? " - từ cấp " + KenhChat.capKenh[0] : "");
            case KenhChat.THE_GIOI: {
                if (!KenhChat.coMayChu) {
                    return "Dùng Loa trong hành trang để nói với cả máy chủ";
                }
                StringBuffer b = new StringBuffer("Cả máy chủ thấy");
                if (KenhChat.capKenh[1] > 0) {
                    b.append(" - từ cấp ").append(KenhChat.capKenh[1]);
                }
                if (KenhChat.xuKenh[1] > 0) {
                    b.append(" - ").append(KenhChat.xuKenh[1]).append(" xu / tin");
                }
                if (KenhChat.giayKenh[1] > 0) {
                    b.append(" - ").append(KenhChat.giayKenh[1]).append(" giây / tin");
                }
                return b.toString();
            }
            case KenhChat.BANG:
                return Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Canvas.gameScr.mainChar.idClan == -1
                        ? "Bạn chưa vào đội" : "Chỉ thành viên đội thấy";
            case KenhChat.DOI:
                return game.model.Char.party == null || game.model.Char.party.size() == 0 ? "Bạn chưa vào nhóm" : "Chỉ người trong nhóm thấy";
            case KenhChat.RIENG: {
                int on = 0;
                for (int i = 0; i < KenhChat.dsBan.size(); i++) {
                    KenhChat.Ban b = (KenhChat.Ban) KenhChat.dsBan.elementAt(i);
                    on += b.online && b.laBan ? 1 : 0;
                }
                return on + " bạn đang trực tuyến";
            }
            default:
                return "Thông báo của trò chơi";
        }
    }

    static int lh(int vai) {
        return ChuDe.cao(vai) + 1;
    }

    /** Cao một tin (đầu dòng: giờ + tên; thân ngắt dòng). */
    static int caoTin(KenhChat.Dong d, int w) {
        int than = ChuDe.ngat(d.noiDung, ChuDe.THUONG, w - 16, 6).length * lh(ChuDe.THUONG);
        int dau = d.kenh == KenhChat.HE_THONG ? 0 : lh(ChuDe.PHU);
        return Math.max(Bang.co(24), dau + than + 8);
    }

    void veTin(Graphics g, int x, int y, int w, int h) {
        final Vector v = dongHienTai();
        VUA_DS[0] = x;
        VUA_DS[1] = y;
        VUA_DS[2] = w;
        VUA_DS[3] = h;
        soDongVe = 0;
        if (v.size() == 0) {
            String phu = this.tab == KenhChat.RIENG ? "Gửi lời chào đầu tiên!" : this.tab == KenhChat.THE_GIOI && !KenhChat.coMayChu
                    ? null : "Tin mới sẽ hiện ở đây.";
            Bang.trong(g, x, y, w, h, "Chưa có tin nào", phu);
            return;
        }
        int k = Bang.cach();
        int[] cao = new int[v.size()];
        int tong = 0;
        for (int i = 0; i < v.size(); i++) {
            cao[i] = caoTin((KenhChat.Dong) v.elementAt(i), w - 10);
            tong += cao[i] + k;
        }
        tong -= k;
        boolean oDay = this.dsTin.lech() >= this.dsTin.toiDa() - 4;
        this.dsTin.khung(g, x, y, w, h, tong);
        if (this.bamDay || (oDay && this.phienDaVe != KenhChat.phienBan)) {
            this.dsTin.den(this.dsTin.toiDa());
            this.bamDay = false;
        }
        if (this.dinhChon && this.chon >= 0 && this.chon < v.size()) {
            int y0 = 0;
            for (int i = 0; i < this.chon; i++) {
                y0 += cao[i] + k;
            }
            if (y0 < this.dsTin.lech()) {
                this.dsTin.den(y0);
            } else if (y0 + cao[this.chon] > this.dsTin.lech() + h) {
                this.dsTin.den(y0 + cao[this.chon] - h);
            }
            this.dinhChon = false;
        }
        this.phienDaVe = KenhChat.phienBan;
        this.dsTin.capNhat();
        int lech = this.dsTin.lech();
        boolean thanh = tong > h;
        if (!thanh) {
            lech = tong - h;   // ít tin: dồn xuống sát ô nhập (tin mới nhất ở dưới, như khung chat NRO)
        }
        int wr = thanh ? w - 10 : w;
        g.setClip(x, y, w, h);
        datCat(g, x, y, w, h);
        try {
            int yy = y - lech;
            for (int i = 0; i < v.size(); i++) {
                if (yy + cao[i] >= y && yy < y + h) {
                    veMotTin(g, (KenhChat.Dong) v.elementAt(i), i, x, yy, wr, cao[i]);
                    g.setClip(x, y, w, h);
                    soDongVe++;
                }
                yy += cao[i] + k;
            }
        } finally {
            boCat();
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        if (thanh) {
            this.dsTin.veThanh(g, x + w - 5, y + 2, h - 4);
        }
    }

    // vùng cắt cho đăng ký chạm (Bang.dangKy dùng vùng cắt của danhSach - ở đây tự cắt)
    private int cx0, cy0, cx1, cy1;
    private boolean cat;

    void datCat(Graphics g, int x, int y, int w, int h) {
        this.cat = true;
        this.cx0 = x + g.getTranslateX();
        this.cy0 = y + g.getTranslateY();
        this.cx1 = this.cx0 + w;
        this.cy1 = this.cy0 + h;
    }

    void boCat() {
        this.cat = false;
    }

    /** Đăng ký chạm chỉ phần thấy (như Bang.dangKy trong danh sách cuộn). */
    void dangKyCat(Graphics g, int x, int y, int w, int h, Object viec, String ten) {
        if (!this.cat) {
            Bang.dangKy(g, x, y, w, h, viec, ten);
            return;
        }
        int ax = x + g.getTranslateX(), ay = y + g.getTranslateY();
        int l = Math.max(ax, this.cx0), t = Math.max(ay, this.cy0), r = Math.min(ax + w, this.cx1), b = Math.min(ay + h, this.cy1);
        if (r - l < Math.max(w / 2, Math.min(w, VungCham.MIN)) || b - t < Math.max(h / 2, Math.min(h, VungCham.MIN))) {
            return;
        }
        VungCham.themTuyetDoi(l, t, r - l, b - t, viec, ten);
    }

    static int mauTen(KenhChat.Dong d) {
        if (d.cuaMinh()) {
            return ChuDe.CHINH_TOI;
        }
        if ((d.co & KenhChat.CO_GM) != 0) {
            return ChuDe.DO;
        }
        if ((d.co & KenhChat.CO_LOA) != 0) {
            return ChuDe.VANG_TOI;
        }
        switch (d.kenh) {
            case KenhChat.THE_GIOI:
                return ChuDe.CAM;
            case KenhChat.BANG:
                return ChuDe.TIM;
            case KenhChat.DOI:
                return ChuDe.LA_TOI;
            case KenhChat.RIENG:
                return ChuDe.HONG;
            default:
                return ChuDe.XANH_TOI;
        }
    }

    void veMotTin(Graphics g, final KenhChat.Dong d, int i, int x, int y, int w, int h) {
        final String ten = d.kenh == KenhChat.RIENG ? d.doiPhuong : d.ten;
        boolean bam = !d.cuaMinh() && d.kenh != KenhChat.HE_THONG && ten != null && ten.length() > 0;
        boolean dangChon = i == this.chon && !this.go;
        int mauNen = dangChon ? ChuDe.CHINH_NHAT : d.cuaMinh() ? ChuDe.NEN_PHU : ChuDe.NEN;
        GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_O, dangChon ? ChuDe.CHINH : ChuDe.KE);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, ChuDe.BO_O - 1, mauNen);
        if (bam) {
            dangKyCat(g, x, y, w, h, new IAction() {
                public void perform() {
                    ManChat.this.menuTen(ten, d.id);
                }
            }, "chat-dong-" + i);
        }
        int xx = x + 8, yy = y + 4;
        if (d.kenh != KenhChat.HE_THONG) {
            String gioTen = d.gio == null ? "" : d.gio;
            ChuDe.chu(g, gioTen, xx, yy, ChuDe.PHU, ChuDe.CHU_NHAT, 0);
            int xt = xx + ChuDe.rong(gioTen, ChuDe.PHU) + 6;
            String nguoi = d.kenh == KenhChat.RIENG && d.cuaMinh() ? "Bạn tới " + d.doiPhuong
                    : (d.cuaMinh() ? "Bạn" : (d.nhan == null ? "" : d.nhan) + d.ten);
            if ((d.co & KenhChat.CO_HOP_THU) != 0) {
                nguoi = nguoi + " (lúc bạn offline)";
            }
            ChuDe.chu(g, ChuDe.cat(nguoi, ChuDe.PHU, x + w - 8 - xt), xt, yy, ChuDe.PHU, mauTen(d), 0);
            yy += lh(ChuDe.PHU);
        }
        String[] dong = ChuDe.ngat(d.kenh == KenhChat.HE_THONG && d.gio != null ? d.gio + "  " + d.noiDung : d.noiDung,
                ChuDe.THUONG, w - 16, 6);
        for (int j = 0; j < dong.length; j++) {
            ChuDe.chu(g, dong[j], xx, yy, ChuDe.THUONG, ChuDe.CHU, 0);
            yy += lh(ChuDe.THUONG);
        }
    }

    void veDsBan(Graphics g, int x, int y, int w, int h) {
        final Vector v = KenhChat.banXep();
        VUA_DS[0] = x;
        VUA_DS[1] = y;
        VUA_DS[2] = w;
        VUA_DS[3] = h;
        soDongVe = 0;
        if (v.size() == 0) {
            Bang.trong(g, x, y, w, h, "Chưa có bạn hay tin riêng", "Chạm tên người chơi trong khung chat hoặc trên bản đồ để nhắn riêng / kết bạn.");
            return;
        }
        final int rh = Math.max(Bang.caoHang(), lh(ChuDe.NHAN) + lh(ChuDe.PHU) + 10);
        Bang.danhSach(g, this.ds, x, y, w, h, v.size(), rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final KenhChat.Ban b = (KenhChat.Ban) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, i == ManChat.this.chon && !ManChat.this.go, new IAction() {
                    public void perform() {
                        ManChat.this.nhanVoi(b.ten);
                    }
                }, "chat-ban-" + i);
                soDongVe++;
                int xx = x + 10;
                int yy = y + (h - lh(ChuDe.NHAN) - lh(ChuDe.PHU)) / 2;
                String trangThai = b.online ? (b.cap > 0 ? "Trực tuyến - Lv." + b.cap : "Trực tuyến") : "Ngoại tuyến";
                int wSo = b.chuaDoc > 0 ? ChuDe.rong(String.valueOf(b.chuaDoc) + " mới", ChuDe.PHU) + 16 : 0;
                ChuDe.chu(g, ChuDe.cat(b.ten, ChuDe.NHAN, w - 20 - wSo), xx, yy, ChuDe.NHAN, ChuDe.CHU, 0);
                String phu = trangThai;
                if (b.tin.size() > 0) {
                    KenhChat.Dong cuoi = (KenhChat.Dong) b.tin.elementAt(b.tin.size() - 1);
                    phu = trangThai + " - " + (cuoi.cuaMinh() ? "Bạn: " : "") + cuoi.noiDung;
                } else if (!b.laBan) {
                    phu = trangThai;
                }
                ChuDe.chu(g, ChuDe.cat(phu, ChuDe.PHU, w - 20 - wSo), xx, yy + lh(ChuDe.NHAN), ChuDe.PHU, b.online ? Bang.TANG : ChuDe.CHU_PHU, 0);
                if (b.chuaDoc > 0) {
                    String so = b.chuaDoc + " mới";
                    ChuDe.chu(g, so, x + w - 10, y + (h - lh(ChuDe.PHU)) / 2, ChuDe.PHU, ChuDe.DO, 1);
                    ChuDe.cham(g, x + w - 6, y + 6);
                }
            }
        });
    }

    void veNhap(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int kenh = kenhGui();
        // chip kênh (chỉ tab Hệ thống đổi được; tab khác: nhãn kênh)
        String chuKenh = kenh == KenhChat.RIENG ? "Mật" : KenhChat.tenKenh[kenh];
        int wChip = ChuDe.rong(chuKenh, ChuDe.NHAN) + 20;
        if (this.tab == KenhChat.HE_THONG) {
            Bang.nut(g, x, y, wChip, h, chuKenh, Bang.PHU, new IAction() {
                public void perform() {
                    ManChat.this.menuKenh();
                }
            }, "chat-kenh");
        } else {
            Bang.vienNut(g, x, y, wChip, h, chuKenh, Bang.TRUNG, false);
        }
        int cho = kenh == KenhChat.THE_GIOI ? KenhChat.giayCho() : 0;
        boolean duoc = kenh != KenhChat.HE_THONG && cho == 0;
        String chuGui = cho > 0 ? "Chờ " + cho + "s" : "Gửi";
        int wGui = Math.max(Bang.co(44), ChuDe.rong("Chờ 30s", ChuDe.NHAN) + 22);
        int xO = x + wChip + k, wO = w - wChip - wGui - 2 * k;
        this.o.x = xO;
        this.o.y = y;
        this.o.width = wO;
        this.o.height = h;   // vùng chạm ô nhập = cả hàng (>= cỡ chạm tối thiểu)
        VUA_NHAP[0] = this.o.x;
        VUA_NHAP[1] = this.o.y;
        VUA_NHAP[2] = this.o.width;
        VUA_NHAP[3] = this.o.height;
        this.o.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        if (this.o.getText().length() == 0 && !this.o.isFocus) {
            ChuDe.chuGiua(g, "Nhập tin...", xO + 8, this.o.y, this.o.height, ChuDe.PHU, ChuDe.CHU_NHAT, 0);
        }
        int xg = x + w - wGui;
        VUA_GUI[0] = xg;
        VUA_GUI[1] = y;
        VUA_GUI[2] = wGui;
        VUA_GUI[3] = h;
        Bang.nut(g, xg, y, wGui, h, chuGui, duoc ? Bang.CHINH : Bang.TAT, duoc ? new IAction() {
            public void perform() {
                ManChat.this.gui();
            }
        } : null, "chat-gui");
        // báo kết quả / lỗi gần nhất (4 giây) ngay trên ô nhập
        if (KenhChat.baoCuoi != null && System.currentTimeMillis() - KenhChat.lucBao < 4000) {
            int hb = lh(ChuDe.PHU) + 4;
            String s = ChuDe.cat(KenhChat.baoCuoi, ChuDe.PHU, w - 16);
            int wb = ChuDe.rong(s, ChuDe.PHU) + 16;
            GiaoDien.khoi(g, x + (w - wb) / 2, y - hb - 3, wb, hb, hb / 2, KenhChat.baoLoi ? ChuDe.DO_NHAT : ChuDe.CHINH_NHAT);
            ChuDe.chuGiua(g, s, x + w / 2, y - hb - 3, hb, ChuDe.PHU, KenhChat.baoLoi ? ChuDe.DO_TOI : ChuDe.CHINH_TOI, 2);
        }
    }

    void veTuyChon(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int s = Bang.co(24);
        Bang.nutLui(g, x, y, s, new IAction() {
            public void perform() {
                ManChat.this.xem = 0;
            }
        });
        ChuDe.chuGiua(g, "Hiện trên dải chat HUD", x + s + k, y, s, ChuDe.NHAN, ChuDe.CHU, 0);
        final int nKenh = KenhChat.SO_KENH;
        final Vector chan = KenhChat.chan;
        final int n = nKenh + 1 + chan.size();
        final int rh = Bang.caoHang();
        Bang.danhSach(g, this.ds, x, y + s + k, w, h - s - k, n, rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                if (i < nKenh) {
                    final int kk = i;
                    Bang.the(g, x, y, w, h);
                    Bang.hangCongTac(g, x, y, w, h, KenhChat.tenKenh[i], i == KenhChat.THE_GIOI ? "Tin cả máy chủ" : null,
                            KenhChat.hienHud[i], false, new IAction() {
                        public void perform() {
                            KenhChat.hienHud[kk] = !KenhChat.hienHud[kk];
                            KenhChat.luuCaiDat();
                        }
                    }, "chat-hud-" + i);
                    return;
                }
                if (i == nKenh) {
                    ChuDe.chuGiua(g, chan.size() == 0 ? "Chưa chặn ai" : "Đã chặn (" + chan.size() + ")", x + 4, y, h, ChuDe.NHAN,
                            ChuDe.CHU_PHU, 0);
                    return;
                }
                final String ten = (String) chan.elementAt(i - nKenh - 1);
                Bang.the(g, x, y, w, h);
                ChuDe.chuGiua(g, ChuDe.cat(ten, ChuDe.NHAN, w - 120), x + 10, y, h, ChuDe.NHAN, ChuDe.CHU, 0);
                int wb = ChuDe.rong("Bỏ chặn", ChuDe.NHAN) + 24;
                Bang.nut(g, x + w - wb - 6, y + 4, wb, h - 8, "Bỏ chặn", Bang.PHU, new IAction() {
                    public void perform() {
                        KenhChat.datChan(ten, false);
                    }
                }, "chat-bo-chan-" + (i - nKenh - 1));
            }
        });
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        // chữ trong bảng chat là chữ người chơi (gõ sao hiện vậy) + tên kênh đã đúng thuật ngữ: không qua bộ đổi chữ cũ
        // ChuPokemon của phông ảnh J2ME
        boolean cu = ChuPokemon.KhongDoi;
        ChuPokemon.KhongDoi = true;
        try {
            String[] t = tabs();
            Object[] vt = new Object[t.length];
            for (int i = 0; i < t.length; i++) {
                final int ti = i;
                vt[i] = new IAction() {
                    public void perform() {
                        ManChat.this.chonTab(ti);
                    }
                };
            }
            boolean[] cham = new boolean[KenhChat.SO_KENH];
            for (int i = 0; i < KenhChat.SO_KENH; i++) {
                cham[i] = i != this.tab && ChamMoi.tabChat(i);
            }
            Bang.chamTab = cham;
            int wT = Bang.oTam(rongMuon(), 0)[2];
            int[] c = Bang.khungTam(g, wT, 0, tieuDe(), t, this.tab, vt, new IAction() {
                public void perform() {
                    ManChat.this.dong();
                }
            });
            veNoiDung(g, c[0], c[1], c[2], c[3]);
            Bang.moDan(g);
        } finally {
            ChuPokemon.KhongDoi = cu;
        }
        Canvas.resetTrans(g);
    }
}
