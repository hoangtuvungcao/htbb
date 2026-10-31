import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * 31/10/2026 (báo lỗi 0.9.24 J2ME giả lập trên trình duyệt, có chạm, ~500 px: "sổ nhiệm vụ TRỐNG" dù đang có cửa ải + nhiệm vụ
 * ngày / thành tựu): đăng nhập MÁY CHỦ THẬT (bộ local), đọc gói -64 thật, mở sổ nhiệm vụ từ MỌI lối vào (thẻ nhiệm vụ HUD,
 * Menu › Nhiệm vụ › Sổ nhiệm vụ bằng chạm / bằng phím) và chụp; HỎNG khi đang có nhiệm vụ mà sổ vẽ trống.
 *
 *   java -Djava.awt.headless=true -Dhtbb.mayChu=$PWD/server [-Dso.j2me=1] [-Dhtbb.khongCham=true] [-Dso.co=500x600]
 *       -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemSoNhiemVuThat <cổng> <thư mục ra> <tài khoản> [tên tạo mới]
 */
public class KiemSoNhiemVuThat {
    static int hong;
    static File RA;

    static void kiem(boolean ok, String ten) {
        if (!ok) {
            hong++;
            System.out.println("HONG: " + ten);
        } else {
            System.out.println("ok   " + ten);
        }
    }

    static void chup(String ten) throws Exception {
        ImageIO.write(ChupTrongGame.ve(NenPc.thatW, NenPc.thatH), "png", new File(RA, ten.replaceAll("[^A-Za-z0-9_]+", "_") + ".png"));
    }

    static String ghiQuest(game.model.Quest q) {
        return q == null ? "null" : q.idQuest + "/" + q.name + " st=" + q.stateQuest + " type=" + q.type + " ms=" + q.main_sub + " co=" + q.co;
    }

    /** Chữ đang vẽ (một khung), nối bằng |. */
    static String chuDangVe() throws Exception {
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        ChupTrongGame.ve(NenPc.thatW, NenPc.thatH);
        game.pokemon.GhiChu.bat = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            if (game.pokemon.GhiChu.CS[i] != null) sb.append('|').append(game.pokemon.GhiChu.CS[i]);
        }
        return sb.toString();
    }

    static final String[] TRONG = {"Chưa nhận nhiệm vụ", "Chưa có nhiệm vụ"};

    /** Sổ đang mở: có nhiệm vụ đang làm thì phải vẽ ít nhất một tên nhiệm vụ và không vẽ dòng "trống". */
    static void kiemSo(String loi) throws Exception {
        final StringBuilder ten = new StringBuilder();
        final int[] n = {0};
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.model.Quest[] qs = {game.render.screen.GameScr.mainQuest, game.render.screen.GameScr.subQuest, game.render.screen.GameScr.clanQuest};
                for (game.model.Quest q : qs) {
                    if (q != null && q.stateQuest != 3) {
                        n[0]++;
                        ten.append(q.name).append('\n');
                    }
                }
            }
        });
        String chu = chuDangVe();
        System.out.println("[" + loi + "] man=" + KiemTaiThat.trangThai() + " chu=" + (chu.length() > 700 ? chu.substring(0, 700) : chu));
        if (n[0] == 0) {
            System.out.println("NHAC: [" + loi + "] may chu khong gui nhiem vu dang lam nao (so trong la dung)");
            return;
        }
        boolean coTen = false;
        for (String t : ten.toString().split("\n")) {
            String dau = t.length() > 10 ? t.substring(0, 10) : t;
            if (dau.length() > 0 && chu.contains(dau)) coTen = true;
        }
        boolean trong = false;
        for (String t : TRONG) if (chu.contains(t)) trong = true;
        kiem(coTen && !trong, "[" + loi + "] so nhiem vu hien nhiem vu dang lam (" + ten.toString().replace('\n', ';') + ")");
    }

    /**
     * 31/10/2026 (báo lỗi 0.9.24 J2ME chạm L49: "chỗ chế tạo trang bị hôm trước có, nay tìm không thấy"): tới NPC như người
     * chơi (chạm đất cạnh NPC, chạm NPC), menu máy chủ -> MỌI mục phải chạm tới được: máy chạm lật "Thêm" như ngón tay thật
     * (chữ trên màn), máy phím xuống bằng phím 8 tới mục cuối. Rồi chọn mục cuối ("Chế tạo & khảm ngọc" ở Machamp).
     */
    static void kiemMenuNpc(String ten) throws Exception {
        String mto = System.getProperty("so.mto");
        if (mto != null) {
            KiemHanhTrinhUI.chat("mto " + mto);   // tài khoản GM: dịch chuyển tới bản đồ có NPC
            KiemHanhTrinhUI.ngu(4000);
            KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
                public boolean dung() {
                    return !game.pokemon.ManTai.dangVao && game.render.Canvas.currentScreen == game.render.Canvas.gameScr;
                }
            }, 20000);
            KiemHanhTrinhUI.ngu(2000);
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.render.Canvas.endDlg();
                }
            });
        }
        boolean toi = false;
        // dịch chuyển / tải bản đồ chưa xong lúc chạm (nhân vật còn ở chỗ cũ): thử lại tối đa 3 lần
        for (int lan = 0; lan < 3 && !(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh) && !game.render.Canvas.menu.showMenu; lan++) {
            if (lan > 0) {
                KiemHanhTrinhUI.ngu(3000);
            }
            toi = game.pokemon.MenuNguCanh.bat() && KiemHanhTrinhUI.toiNpc(ten);
            if (!(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh) && !game.render.Canvas.menu.showMenu) {
                // máy phím / chạm trượt: đi bộ tới cạnh NPC (tìm đường), nhắm NPC, phím 5 (nói chuyện) - như người chơi bấm phím
                final game.model.NPC[] n = {null};
                final String tenNpc = ten;
                KiemTaiThat.tren(new Runnable() {
                    public void run() {
                        n[0] = KiemHanhTrinhUI.timNpc(tenNpc);
                        if (n[0] != null) {
                            game.render.Canvas.endDlg();
                            game.render.Canvas.gameScr.findRoad2(n[0].x, n[0].y + 16);
                        }
                    }
                });
                if (n[0] != null) {
                    KiemHanhTrinhUI.choDungYen(25000);
                    KiemTaiThat.tren(new Runnable() {
                        public void run() {
                            game.render.Canvas.gameScr.focusedActor = n[0];
                        }
                    });
                    KiemHanhTrinhUI.phim(-5, "noi chuyen NPC");
                    KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
                        public boolean dung() {
                            return game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh || game.render.Canvas.menu.showMenu;
                        }
                    }, 5000);
                    toi = true;
                }
            }
        }
        KiemHanhTrinhUI.ngu(800);
        chup("06_menu_" + ten.replace(' ', '_'));
        kiem(toi, "toi NPC " + ten);
        if (!toi) {
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    StringBuilder b = new StringBuilder();
                    java.util.Vector v = game.render.Canvas.gameScr.actors;
                    for (int i = 0; v != null && i < v.size(); i++) {
                        if (v.elementAt(i) instanceof game.model.NPC) {
                            game.model.NPC n = (game.model.NPC) v.elementAt(i);
                            b.append(n.name).append('/').append(n.type).append(' ');
                        }
                    }
                    System.out.println("NPC tren ban do " + game.render.screen.GameScr.mapNap + ": " + b);
                }
            });
            return;
        }
        if (System.getProperty("so.cheTao") != null) {
            kiemCheTaoThang(ten, "1".equals(System.getProperty("so.cheTao")));
            return;
        }
        String muc = System.getProperty("so.npcMuc");
        if (muc != null) {
            // menu con của máy chủ (Machamp › Mua bán -> "Mua đồ cầm tay", ..., "Chế tạo & khảm ngọc")
            kiemMotMenu(ten, false);
            if (game.pokemon.MenuNguCanh.bat()) {
                KiemHanhTrinhUI.chonMenu(muc);
            } else if (game.render.Canvas.menu.showMenu) {
                java.util.Vector v = game.render.Canvas.menu.menuItems;
                int vt = -1;
                for (int i = 0; i < v.size(); i++) {
                    if (((game.model.Command) v.elementAt(i)).caption.startsWith(muc)) vt = i;
                }
                for (int i = 0; i < 2 * v.size() && vt >= 0 && game.render.Canvas.menu.selected != vt; i++) {
                    KiemHanhTrinhUI.phim(-2, "xuong");
                    KiemHanhTrinhUI.ngu(150);
                }
                KiemHanhTrinhUI.phim(-5, "chon " + muc);
            }
            KiemHanhTrinhUI.ngu(1500);
            KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
                public boolean dung() {
                    return game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh || game.render.Canvas.menu.showMenu;
                }
            }, 5000);
            chup("06b_menu_" + ten + "_con");
            ten = ten + " > " + muc;
        }
        kiemMotMenu(ten, true);
    }

    /**
     * 0.9.26 (báo lỗi 0.9.25 "không tìm thấy chế tạo"): menu Machamp có lối thẳng "Chế tạo trang bị" / "Khảm ngọc hệ" ngay
     * sau "Mua bán" khi máy chủ có Chế tạo (mong = true), không có khi máy chủ tắt (mong = false). Chạm / phím "Chế tạo
     * trang bị" phải mở màn Chế tạo (ManCheTao) như đường Mua bán › Chế tạo & khảm ngọc › Chế tạo trang bị.
     */
    static void kiemCheTaoThang(String ten, boolean mong) throws Exception {
        KiemHanhTrinhUI.ngu(2500);   // trả lời hỏi ngầm Mua bán
        final String[][] l = {null};
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                l[0] = ChupTrongGame.lenhMoDangMo();
            }
        });
        java.util.List<String> ds = java.util.Arrays.asList(l[0]);
        System.out.println("MENU " + ten + ": " + ds);
        chup("09_menu_" + ten + "_che_tao_thang");
        boolean co = ds.contains("Chế tạo trang bị") && ds.contains("Khảm ngọc hệ");
        kiem(co == mong, "menu " + ten + ": loi thang Che tao trang bi / Kham ngoc he " + (mong ? "CO" : "KHONG co") + " " + ds);
        kiem(ds.contains("Nghiền bột"), "menu " + ten + ": goi tra loi hoi ngam khong de len menu Machamp " + ds);
        if (!mong || !co) {
            return;
        }
        if (game.pokemon.MenuNguCanh.bat()) {
            for (int lat = 0; lat < ds.size() && !chuDangVe().contains("Chế tạo trang bị"); lat++) {
                KiemHanhTrinhUI.chamTheoChuDung("Thêm");
                KiemHanhTrinhUI.ngu(400);
            }
            KiemHanhTrinhUI.chamTheoChuDung("Chế tạo trang bị");
        } else {
            int vt = ds.indexOf("Chế tạo trang bị");
            for (int i = 0; i < 2 * ds.size() && game.render.Canvas.menu.selected != vt; i++) {
                KiemHanhTrinhUI.phim(-2, "xuong");
                KiemHanhTrinhUI.ngu(150);
            }
            KiemHanhTrinhUI.phim(-5, "chon Che tao trang bi");
        }
        boolean mo = KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentScreen instanceof game.pokemon.ManCheTao;
            }
        }, 8000);
        chup("10_che_tao_" + ten);
        kiem(mo, "menu " + ten + ": Che tao trang bi mo man Che tao (" + KiemTaiThat.trangThai() + ")");
    }

    static void kiemMotMenu(String ten, boolean chonCuoi) throws Exception {
        if (game.pokemon.MenuNguCanh.bat()) {
            boolean laThe = game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh;
            kiem(laThe, "menu NPC " + ten + " la the lenh (" + KiemTaiThat.trangThai() + ")");
            if (!laThe) {
                return;
            }
            String[] l = game.pokemon.MenuNguCanh.cacLenh();
            System.out.println("MENU " + ten + ": " + java.util.Arrays.toString(l));
            java.util.Set<String> thay = new java.util.LinkedHashSet<String>();
            for (int lat = 0; lat <= l.length + 1; lat++) {
                String chu = chuDangVe();
                for (String x : l) {
                    if (x != null && chu.contains(x.length() > 14 ? x.substring(0, 14) : x)) thay.add(x);
                }
                chup("07_menu_" + ten + "_trang" + lat);
                if (thay.size() == l.length || !chu.contains("|Thêm")) break;
                KiemHanhTrinhUI.chamTheoChuDung("Thêm");
                KiemHanhTrinhUI.ngu(400);
            }
            for (String x : l) {
                kiem(thay.contains(x), "menu " + ten + ": muc '" + x + "' cham toi duoc (thay " + thay + ")");
            }
            if (!chonCuoi) {
                return;
            }
            String cuoi = l[l.length - 1];
            for (int lat = 0; lat <= l.length && !chuDangVe().contains(cuoi.length() > 14 ? cuoi.substring(0, 14) : cuoi); lat++) {
                KiemHanhTrinhUI.chamTheoChuDung("Thêm");
                KiemHanhTrinhUI.ngu(400);
            }
            KiemHanhTrinhUI.chamTheoChu(cuoi.length() > 14 ? cuoi.substring(0, 14) : cuoi);
        } else {
            boolean chu = game.render.Canvas.menu.showMenu && game.render.Canvas.menu.menuItems != null;
            kiem(chu, "menu NPC " + ten + " (may phim) la menu chu (" + KiemTaiThat.trangThai() + ")");
            if (!chu) {
                return;
            }
            int n = game.render.Canvas.menu.menuItems.size();
            for (int i = 0; i < 2 * n && game.render.Canvas.menu.selected != n - 1; i++) {
                KiemHanhTrinhUI.phim(-2, "xuong");
                KiemHanhTrinhUI.ngu(150);
            }
            kiem(game.render.Canvas.menu.selected == n - 1, "menu " + ten + " (may phim): phim xuong toi muc cuoi " + (n - 1) + " (dang " + game.render.Canvas.menu.selected + ")");
            chup("07_menu_" + ten + "_cuoi");
            if (!chonCuoi) {
                return;
            }
            KiemHanhTrinhUI.phim(-5, "chon muc cuoi");
        }
        KiemHanhTrinhUI.ngu(1500);
        chup("08_sau_muc_cuoi_" + ten);
        System.out.println("sau muc cuoi: " + KiemTaiThat.trangThai() + " | " + (chuDangVe().length() > 400 ? chuDangVe().substring(0, 400) : chuDangVe()));
    }

    static String chuHop() {
        Object d = game.render.Canvas.currentDialog;
        if (!(d instanceof game.render.screen.MsgDlg)) {
            return "";
        }
        try {
            java.lang.reflect.Field f = game.render.screen.MsgDlg.class.getDeclaredField("info");
            f.setAccessible(true);
            StringBuilder b = new StringBuilder();
            for (String x : (String[]) f.get(d)) b.append(x).append(' ');
            return b.toString();
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * 31/10/2026 (nhật ký: hai client cùng tài khoản đá nhau ~3 s một lần): hai JVM cùng tài khoản trên máy chủ thật. Máy
     * A (-Dso.mongBiDa=1) vào trước rồi chờ; máy B vào sau. A phải nhận lý do "đăng nhập ở thiết bị khác", KHÔNG tự nối
     * lại (không có lần thử nào trong suốt thời gian chờ); B (-Dso.mongBiDa=0) ở lại trong game tới hết.
     */
    static void choDangNhapKhac(int giay, final boolean mongBiDa) throws Exception {
        boolean thayBiDa = false, thayNoiLai = false, thayLyDo = false, conTrongGame = true;
        for (int t = 0; t < giay; t += 2) {
            final String[] tt = new String[1];
            final boolean[] co = new boolean[3];
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    co[0] = game.pokemon.TuNoiLai.biDangNhapKhac;
                    co[1] = game.pokemon.TuNoiLai.dangNoi;
                    String h = chuHop();
                    co[2] = h.contains("thiết bị khác");
                    tt[0] = KiemTaiThat.trangThai() + " | hop: " + h;
                }
            });
            thayBiDa |= co[0];
            thayNoiLai |= co[1];
            thayLyDo |= co[2];
            System.out.println("CHO t=" + t + " biDa=" + co[0] + " dangNoi=" + co[1] + " | " + tt[0]);
            if (t == 2 && mongBiDa) chup("cho_dau");
            KiemHanhTrinhUI.ngu(2000);
        }
        chup("cho_cuoi");
        if (mongBiDa) {
            kiem(thayBiDa, "may cu: nhan thong bao 'dang nhap o thiet bi khac'");
            kiem(thayLyDo, "may cu: hop mat ket noi ghi ly do dang nhap o thiet bi khac");
            kiem(!thayNoiLai, "may cu: KHONG tu ket noi lai (khong ping-pong)");
        } else {
            String h = chuHop();
            boolean matKetNoi = h.contains("Mất kết nối") || h.contains("thiết bị khác");
            conTrongGame = game.render.Canvas.currentScreen == game.render.Canvas.gameScr && !matKetNoi;
            kiem(conTrongGame && !thayBiDa && !thayNoiLai, "may moi: o lai trong game, khong bi da nguoc / khong phai noi lai (" + KiemTaiThat.trangThai() + " | " + chuHop() + ")");
        }
    }

    public static void main(String[] a) throws Exception {
        final int cong = Integer.parseInt(a[0]);
        RA = new File(a[1]);
        RA.mkdirs();
        final String tk = a[2];
        final String tenMoi = a.length > 3 ? a[3] : tk;
        KiemHanhTrinhUI.RA = RA;
        KiemHanhTrinhUI.BAO = new PrintWriter(new FileWriter(new File(RA, "bao_cao.tsv")));
        System.setProperty("java.awt.headless", "true");
        System.setProperty("htbb.duLieu", new File(RA, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "Nokia6233/05.60");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:" + cong);
        final boolean j2me = System.getProperty("so.j2me") != null;
        String[] co = System.getProperty("so.co", "500x600").split("x");
        final int W = Integer.parseInt(co[0]), H = Integer.parseInt(co[1]);
        if (j2me) {
            ChupTrongGame.datCoJ2me(W, H);
        } else {
            NenPc.tinhKichThuoc(W, H);
        }
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet.batDau((MIDlet) Class.forName("game.GameMidlet").newInstance());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        if (j2me) {
            javax.microedition.lcdui.NghiemMidp.bat = true;
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
            try {
                Class.forName("htbb.pc.LopPhu");   // khối static của LopPhu cài PhongVe.bo - chạy trước rồi mới bỏ
            } catch (Throwable e) {
                // không có
            }
            game.pokemon.PhongVe.bo = null;   // máy J2ME thật không có bộ vẽ chữ vector (TienTe.fontSo: số tiền bằng arialFont)
        }
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
            }
        });
        // bơm khung như máy thật
        Thread bom = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    KiemHanhTrinhUI.ngu(50);
                    try {
                        NenPc.luong().them(new Runnable() {
                            public void run() {
                                ChupTrongGame.veThang();
                            }
                        });
                    } catch (Throwable e) {
                        // bỏ
                    }
                }
            }
        });
        bom.setDaemon(true);
        bom.start();
        for (int i = 0; i < 300 && !(game.render.Canvas.currentScreen instanceof game.render.screen.LoginScr); i++) {
            if (i == 30) KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.render.Canvas.loginScr.switchToMe();
                }
            });
            KiemHanhTrinhUI.ngu(100);
        }
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.screen.ServerListScr.address = new String[]{"127.0.0.1"};
                game.render.screen.ServerListScr.port = new short[]{(short) cong};
                game.render.screen.ServerListScr.index = 0;
                game.render.Canvas.loginScr.tfUser.setText(tk);
                game.render.Canvas.loginScr.tfPass.setText("123456");
            }
        });
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                try {
                    java.lang.reflect.Method m = game.render.screen.LoginScr.class.getDeclaredMethod("doLogin");
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.loginScr);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
            public boolean dung() {
                Object m = game.render.Canvas.currentScreen;
                return m instanceof game.render.screen.CreateCharScr || m instanceof game.render.screen.CharSelectScr
                        || m == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao;
            }
        }, 30000);
        if (game.render.Canvas.currentScreen instanceof game.render.screen.CreateCharScr) {
            KiemHanhTrinhUI.cham("khoi-dau-0");
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.render.screen.CreateCharScr.gI().tfChat.setText(tenMoi.length() > 10 ? tenMoi.substring(0, 10) : tenMoi);
                }
            });
            KiemHanhTrinhUI.cham("tao-1");
        } else if (game.render.Canvas.currentScreen instanceof game.render.screen.CharSelectScr) {
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    try {
                        java.lang.reflect.Field f = game.render.screen.CharSelectScr.class.getDeclaredField("cmdSelect");
                        f.setAccessible(true);
                        ((game.model.Command) f.get(game.render.screen.CharSelectScr.gI())).action.perform();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        boolean vao = KiemHanhTrinhUI.cho(new KiemHanhTrinhUI.DieuKien() {
            public boolean dung() {
                return game.render.Canvas.currentScreen == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao
                        && game.render.Canvas.gameScr.mainChar != null;
            }
        }, 60000);
        kiem(vao, "vao game " + tk + " (" + KiemTaiThat.trangThai() + ")");
        if (!vao) {
            chup("khong_vao_duoc");
            System.out.println("KiemSoNhiemVuThat: hong " + hong);
            System.exit(1);
        }
        KiemHanhTrinhUI.ngu(5000);
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.endDlg();
                game.render.Canvas.menu.showMenu = false;
            }
        });
        if (System.getProperty("so.cho") != null) {
            choDangNhapKhac(Integer.parseInt(System.getProperty("so.cho")), "1".equals(System.getProperty("so.mongBiDa")));
            KiemHanhTrinhUI.BAO.close();
            System.out.println("KiemSoNhiemVuThat: hong " + hong);
            System.exit(hong == 0 ? 0 : 1);
        }
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                System.out.println("QUEST main=" + ghiQuest(game.render.screen.GameScr.mainQuest) + " | sub=" + ghiQuest(game.render.screen.GameScr.subQuest)
                        + " | clan=" + ghiQuest(game.render.screen.GameScr.clanQuest) + " | coTheNhan=" + game.render.screen.GameScr.allQuestCanReceive.size()
                        + " | coMucTieuNv=" + game.render.screen.GameScr.coMucTieuNv + " | cham=" + game.pokemon.MenuNguCanh.bat()
                        + " | j2me=" + (game.pokemon.ChuUI.bo == null) + " | " + game.render.Canvas.w + "x" + game.render.Canvas.h);
            }
        });
        chup("01_the_gioi");
        // 1) thẻ nhiệm vụ HUD (chạm) / gọi thẳng ManNhiemVu.mo như thẻ
        boolean coThe = KiemHanhTrinhUI.coVung("nv-the");
        System.out.println("vung nv-the: " + coThe);
        if (coThe) {
            KiemHanhTrinhUI.cham("nv-the");
        } else {
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.pokemon.ManNhiemVu.mo();
                }
            });
        }
        KiemHanhTrinhUI.ngu(600);
        chup("02_so_tu_the");
        kiemSo("the HUD / ManNhiemVu");
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.switchToMe();
            }
        });
        KiemHanhTrinhUI.ngu(400);
        // 2) Menu (phím mềm trái) › Nhiệm vụ › Sổ nhiệm vụ
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.left.action.perform();
            }
        });
        KiemHanhTrinhUI.ngu(600);
        chup("03_menu");
        if (game.pokemon.MenuNguCanh.bat()) {
            KiemHanhTrinhUI.chamTheoChuDung("Nhiệm vụ");
            KiemHanhTrinhUI.ngu(500);
            chup("04_menu_nhiem_vu");
            KiemHanhTrinhUI.chamTheoChu("Sổ nhiệm vụ");
        } else {
            // phím: xuống tới hàng Nhiệm vụ (hàng gốc 4) rồi chọn, mục 0 = Sổ nhiệm vụ
            final game.render.screen.PauseMenu pm = game.render.screen.PauseMenu.gI();
            for (int i = 0; i < 12; i++) {
                {
                    final int[] gi = {-1};
                    KiemTaiThat.tren(new Runnable() {
                        public void run() {
                            try {
                                java.lang.reflect.Field fh = game.render.screen.PauseMenu.class.getDeclaredField("hien");
                                fh.setAccessible(true);
                                int[] h = (int[]) fh.get(pm);
                                java.lang.reflect.Field fs = game.render.screen.PauseMenu.class.getDeclaredField("selected");
                                fs.setAccessible(true);
                                int s = fs.getInt(pm);
                                gi[0] = s >= 0 && s < h.length ? h[s] : -1;
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    if (gi[0] == 4) break;
                }
                KiemHanhTrinhUI.phim(-2, "xuong");
            }
            KiemHanhTrinhUI.phim(-5, "chon");
            KiemHanhTrinhUI.ngu(400);
            chup("04_menu_nhiem_vu");
            KiemHanhTrinhUI.phim(-5, "chon so nhiem vu");
        }
        KiemHanhTrinhUI.ngu(800);
        chup("05_so_tu_menu");
        kiemSo("Menu > Nhiem vu > So nhiem vu");
        String npc = System.getProperty("so.npc");
        if (npc != null) {
            KiemTaiThat.tren(new Runnable() {
                public void run() {
                    game.render.Canvas.endDlg();
                    game.render.Canvas.gameScr.switchToMe();
                }
            });
            KiemHanhTrinhUI.ngu(500);
            kiemMenuNpc(npc);
        }
        KiemHanhTrinhUI.BAO.close();
        System.out.println("KiemSoNhiemVuThat: hong " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }
}
