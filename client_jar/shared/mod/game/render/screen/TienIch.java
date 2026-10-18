package game.render.screen;

import game.GameMidlet;
import game.model.Command;
import game.model.IAction;
import game.model.Char;
import game.model.GemTemplate;
import game.model.ItemInInventory;
import game.model.MainChar;
import game.model.skill.SkillManager;
import game.model.Tilemap;
import game.networklogic.GameService;
import game.render.Canvas;
import java.util.Vector;
import network.Session_ME;

/**
 * Menu Tiện ích: đăng xuất, bật/tắt thông báo popup, tự đăng nhập lại khi mất kết nối, Thổ địa phù
 * về làng ngay. tick() gọi mỗi khung hình từ AutoGuard.paint (chạy ở MỌI màn hình, kể cả màn đăng
 * nhập - auto đăng nhập cần vậy).
 */
final class TienIch {
    // Cắt chuỗi thanh trạng thái cho vừa màn hình, nhớ 4 kết quả gần nhất (mỗi khung hình vẽ lại cùng chuỗi:
    // trước đây cắt từng ký tự + đo lại mỗi lần). Chỉ gọi từ luồng vẽ.
    private static final String[] CAT_GOC = new String[4];
    private static final String[] CAT_KQ = new String[4];
    private static final int[] CAT_W = new int[4];
    private static int catTiep;

    static String catVua(String s, Font font) {
        for (int i = 0; i < 4; ++i) {
            if (CAT_W[i] == Canvas.w && s.equals(CAT_GOC[i])) {
                return CAT_KQ[i];
            }
        }
        String kq = s;
        while (kq.length() > 6 && font.getWidth(kq) > Canvas.w - 12) {
            kq = kq.substring(0, kq.length() - 1);
        }
        CAT_GOC[catTiep] = s;
        CAT_KQ[catTiep] = kq;
        CAT_W[catTiep] = Canvas.w;
        catTiep = (catTiep + 1) % 4;
        return kq;
    }

    private static final String RMS_TEN = "htbbti";
    /** Tắt thông báo: nuốt mọi hộp thoại OK của server khi đang trong game (màn đăng nhập vẫn hiện). */
    static boolean tatThongBao;
    /** Auto login (bật/tắt ở menu Auto up): mất kết nối thì tự đăng nhập lại + chọn đúng nhân vật. */
    static boolean tuDangNhap;
    /** Thổ địa phù về làng ngay: client gốc đếm 10s rồi mới gửi lệnh về làng; server không kiểm thời gian. */
    private static final boolean thoDiaNhanh = true;
    /** Tầm skill phía client luôn = 300 (server vẫn chỉ nhận đòn trong ô vuông 130px). */
    private static final short TAM_DANH = 300;
    /** Auto up đổi sang quái khác mỗi 1 giây. */
    static boolean doiMucTieu = true;
    static boolean muaThe;
    static boolean muaBinh;
    static boolean muaVe;
    /** Auto up tự dùng thẻ mua bán vào nhà sửa đồ khi vũ khí sắp hết bền. */
    static boolean suaDo;
    /** Hộp thoại do tự sửa đồ sinh ra (Đã sửa xong / Còn ...s nữa ...): nuốt đi. */
    static int anSua;
    private static boolean daNap;

    private static final int THE_MUA_BAN = 675;
    private static final int SHOP_VE_GIO_VANG = 0;
    private static final int SHOP_HP_15K = 127;
    private static final int SHOP_MP_15K = 128;
    private static final int BINH_HP_15K = 94;
    private static final int BINH_MP_15K = 96;
    private static final int CON_IT = 30;
    private static long lucMua;
    /** Hộp "Đã mua..." / "Không đủ..." do chính tự-mua gây ra: nuốt đi. */
    private static int anMua;
    /** [loại] số lần mua liên tiếp chưa thấy kết quả / nghỉ tới lúc. 0 = HP, 1 = MP, 2 = vé, 3 = thẻ */
    private static final int[] thuMua = new int[4];
    private static final long[] nghiToi = new long[4];
    private static final int[] soTruoc = new int[4];

    /** Đã vào game ít nhất một lần (mới có tài khoản / nhân vật để đăng nhập lại). */
    private static boolean daVaoGame;
    /** Người chơi tự bấm Đăng xuất: không tự đăng nhập lại tới khi vào game lần sau. */
    private static boolean chuDongThoat;
    private static String tenNhanVat;
    private static long lucThu;
    private static long lucChon;
    private static boolean dangLam;

    private TienIch() {
    }

    static void moMenu(int chon) {
        TienIch.nap();
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(LenThanh.dang ? "Dừng lên thành" : "Lên thành", new IAction(){

            public void perform() {
                if (LenThanh.dang) {
                    LenThanh.tat();
                } else {
                    LenThanh.bat();
                }
            }
        }));
        vector.addElement(new Command("Đăng xuất", new IAction(){

            public void perform() {
                TienIch.dangXuat();
            }
        }));
        vector.addElement(new Command(tatThongBao ? "Thông báo: tắt" : "Thông báo: bật", new IAction(){

            public void perform() {
                tatThongBao = !tatThongBao;
                TienIch.luu();
                TienIch.moMenu(2);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    /** Auto up gọi: 1 = đổi mục tiêu, 2 = auto login, 3 = mua bình, 4 = mua vé, 5 = mua thẻ. */
    static void lat(int n) {
        TienIch.nap();
        if (n == 2) {
            tuDangNhap = !tuDangNhap;
        } else if (n == 3) {
            muaBinh = !muaBinh;
        } else if (n == 4) {
            muaVe = !muaVe;
        } else if (n == 5) {
            muaThe = !muaThe;
        } else if (n == 6) {
            suaDo = !suaDo;
        } else {
            doiMucTieu = !doiMucTieu;
        }
        TienIch.luu();
    }

    /** Trạng thái để Auto up vẽ menu: 3 = mua bình, 4 = mua vé, 5 = mua thẻ. */
    static boolean bat(int n) {
        TienIch.nap();
        return n == 3 ? muaBinh : (n == 4 ? muaVe : (n == 5 ? muaThe : suaDo));
    }

    static boolean autoLogin() {
        TienIch.nap();
        return tuDangNhap;
    }


    /** Menu xa phu của map đang đứng, mở từ xa (server chỉ đòi map có xa phu, không đòi đứng gần NPC). */
    static void moXaPhu() {
        final GameScr gameScr = Canvas.gameScr;
        final game.model.XaphuTemplate xaphuTemplate = game.render.Res.getXaphu();
        if (gameScr == null || xaphuTemplate == null || xaphuTemplate.mapID == null) {
            Canvas.startOKDlg("Map này không có xa phu.");
            return;
        }
        Vector<Command> vector = new Vector<Command>();
        for (int i = 0; i < xaphuTemplate.mapID.length; ++i) {
            final int n = i;
            final int n2 = xaphuTemplate.mapID[i];
            final String string = n2 >= 0 && n2 < game.render.Res.nameBigMap.length ? game.render.Res.nameBigMap[n2] : String.valueOf(n2);
            vector.addElement(new Command(string, new IAction(){

                public void perform() {
                    if (xaphuTemplate.price != null && n < xaphuTemplate.price.length && (long)xaphuTemplate.price[n] > gameScr.mainChar.charXu) {
                        Canvas.startOKDlg("Không còn đủ tiền");
                        return;
                    }
                    gameScr.doUseXaPhu(n2, n);
                }
            }));
        }
        PauseMenu.moMenuGiuChon(vector, 2, 0);
    }

    private static void dangXuat() {
        chuDongThoat = true;
        Canvas.menu.showMenu = false;
        AutoUp.tat();
        AutoVanTieu.tat();
        AutoVanMay.tat();
        TienIch.veManChonServer();
    }

    /** Y hệt nút OK của hộp "Bị ngắt kết nối" (GameScr.onDisconnect). */
    private static void veManChonServer() {
        Canvas.endDlg();
        Canvas.instance.init();
        Session_ME.gI().close();
        Tilemap.loadMap(0, null);
        ServerListScr.gI().switchToMe();
    }

    /** Gọi từ AutoGuard.autoEatDlg: true = nuốt hộp thoại này. */
    static boolean anThongBao(String string) {
        TienIch.nap();
        if (anSua > 0 && string != null && (string.startsWith("Đã sửa") || string.startsWith("Còn ") || string.startsWith("Hết tiền"))) {
            --anSua;
            return true;
        }
        if (anMua > 0 && string != null && (string.startsWith("Đã mua") || string.startsWith("Không đủ") || string.startsWith("Hành trang"))) {
            --anMua;
            return true;
        }
        return tatThongBao && Canvas.gameScr != null && Canvas.currentScreen == Canvas.gameScr;
    }

    private static boolean coLuong;

    /**
     * Chạy tick() trên luồng riêng mỗi 0.1s. Trước đây gọi từ AutoGuard.paint, mà khi ghép nhiều
     * bản (EmbedAdvMenu) bản đang ẩn KHÔNG được vẽ -> auto login / tự mua / Thổ địa nhanh / tầm 300
     * không chạy ở bản ẩn. Luồng cập nhật của game thì vẫn chạy nên gọi được từ cả hai chỗ.
     */
    static synchronized void batLuong() {
        if (coLuong) {
            return;
        }
        coLuong = true;
        Thread thread = new Thread(){

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100L);
                        TienIch.tick();
                    }
                    catch (Throwable throwable) {
                        // vòng sau chạy tiếp
                    }
                }
            }
        };
        thread.start();
    }

    static void tick() {
        TienIch.nap();
        long l = System.currentTimeMillis();
        GameScr gameScr = Canvas.gameScr;
        MainChar mainChar = gameScr == null ? null : gameScr.mainChar;
        boolean bl = Session_ME.gI().connected;
        if (thoDiaNhanh && mainChar != null && mainChar.comeHome && l - mainChar.timeComeHome < 10000L) {
            mainChar.timeComeHome = l - 10001L;
        }
        TienIch.apTam();
        if (mainChar != null && Canvas.currentScreen == gameScr && bl && mainChar.name != null && mainChar.name.length() > 0) {
            try {
                TienIch.tuMua(gameScr, mainChar, l);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            daVaoGame = true;
            chuDongThoat = false;
            tenNhanVat = mainChar.name;
            lucThu = 0L;
            lucChon = 0L;
            return;
        }
        if (!tuDangNhap || !daVaoGame || chuDongThoat || dangLam || AutoGuard.tkDangNhap == null || AutoGuard.mkDangNhap == null) {
            return;
        }
        // mất kết nối khi đang trong game: hộp "Bị ngắt kết nối" -> bấm OK thay người chơi
        if (Canvas.currentScreen == gameScr && !bl && Canvas.currentDialog != null) {
            TienIch.chay(0);
            return;
        }
        // màn chọn server / đăng nhập: đăng nhập lại (thử lại mỗi 20s)
        if ((Canvas.currentScreen instanceof ServerListScr || Canvas.currentScreen instanceof LoginScr) && l >= lucThu) {
            lucThu = l + 20000L;
            TienIch.chay(1);
            return;
        }
        // màn chọn nhân vật: chọn đúng nhân vật đang chơi lúc trước
        CharSelectScr charSelectScr = CharSelectScr.me;
        if (charSelectScr != null && Canvas.currentScreen == charSelectScr && charSelectScr.charInfos != null && charSelectScr.cmdSelect != null && l >= lucChon) {
            lucChon = l + 10000L;
            for (int i = 0; i < charSelectScr.charInfos.length; ++i) {
                if (charSelectScr.charInfos[i] == null || tenNhanVat == null || !tenNhanVat.equals(charSelectScr.charInfos[i].name)) continue;
                charSelectScr.selected = i;
                break;
            }
            TienIch.chay(2);
        }
    }

    /** Tầm mọi skill phía client = 300 (bảng có thể được nạp lại -> đặt lại mỗi khung hình). */
    private static void apTam() {
        short[][] sArray2 = SkillManager.SKILL_RANGE;
        short[] sArray = sArray2 == null ? null : sArray2[0];
        if (sArray == null) {
            return;
        }
        for (int i = 0; i < sArray.length; ++i) {
            sArray[i] = TAM_DANH;
        }
    }

    /** Mỗi 3s mua tối đa một món; mua 3 lần liền không thấy kết quả (hết tiền...) thì nghỉ 5 phút. */
    private static void tuMua(GameScr gameScr, MainChar mainChar, long l) throws Exception {
        if (l < lucMua || !(muaBinh || muaVe || muaThe)) {
            return;
        }
        lucMua = l + 3000L;
        int[] nArray = mainChar.potions;
        if (muaBinh && nArray != null && nArray.length > BINH_MP_15K) {
            if (TienIch.canMua(0, nArray[BINH_HP_15K], l) && nArray[BINH_HP_15K] < CON_IT) {
                TienIch.daGui(0, nArray[BINH_HP_15K]);
                GameService.gI().buyItemShopSpecial(SHOP_HP_15K);
                return;
            }
            if (TienIch.canMua(1, nArray[BINH_MP_15K], l) && nArray[BINH_MP_15K] < CON_IT) {
                TienIch.daGui(1, nArray[BINH_MP_15K]);
                GameService.gI().buyItemShopSpecial(SHOP_MP_15K);
                return;
            }
        }
        if (muaVe && !TienIch.gioVangCon(mainChar, l)) {
            GemTemplate gemTemplate = TienIch.veTrongTui();
            if (gemTemplate != null) {
                // có vé: dùng (không tính là lần mua)
                GameService.gI().doUseSpecialItem(gemTemplate.rID);
                lucMua = l + 5000L;
                return;
            }
            if (TienIch.canMua(2, 0, l)) {
                TienIch.daGui(2, 0);
                GameService.gI().buyItemShopSpecial(SHOP_VE_GIO_VANG);
                return;
            }
        }
        if (muaThe && gameScr.isOffLineMap(gameScr.map) && !TienIch.coThe() && TienIch.canMua(3, 0, l)) {
            TienIch.daGui(3, 0);
            network.Message message = new network.Message(24);
            message.writer().writeByte(1);
            message.writer().writeByte(3);
            message.writer().writeShort(THE_MUA_BAN);
            message.writer().writeShort(1);
            message.writer().writeByte(0);
            GameService.gI().session.sendMessage(message);
            message.cleanup();
        }
    }

    /** Còn được mua loại k? Số lượng tăng so với lần gửi trước = mua được -> đếm lại từ đầu. */
    private static boolean canMua(int k, int n, long l) {
        if (n > soTruoc[k]) {
            thuMua[k] = 0;
        }
        soTruoc[k] = n;
        if (l < nghiToi[k]) {
            return false;
        }
        if (thuMua[k] >= 3) {
            thuMua[k] = 0;
            nghiToi[k] = l + 300000L;
            return false;
        }
        return true;
    }

    private static void daGui(int k, int n) {
        ++thuMua[k];
        soTruoc[k] = n;
        anMua = Math.min(anMua + 1, 3);
    }

    private static boolean gioVangCon(MainChar mainChar, long l) {
        if (Char.goldTimeMinute <= 0 || mainChar.timeUseGolgTicket <= 0L) {
            return false;
        }
        return l - mainChar.timeUseGolgTicket < (long)Char.goldTimeMinute * 60000L;
    }

    /** Vé giờ vàng 100 (shop id 0) đang có trong túi vật phẩm đặc biệt. */
    private static GemTemplate veTrongTui() {
        Vector vector = MainChar.shopItem;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            GemTemplate gemTemplate = (GemTemplate)vector.elementAt(i);
            if (gemTemplate == null || gemTemplate.id != SHOP_VE_GIO_VANG || gemTemplate.number <= 0) continue;
            return gemTemplate;
        }
        return null;
    }

    private static boolean coThe() {
        return TienIch.theMuaBan() != null;
    }

    /** Thẻ mua bán trong hành trang (null = không có). */
    static ItemInInventory theMuaBan() {
        Vector vector = Char.inventory;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            Object object = vector.elementAt(i);
            if (!(object instanceof ItemInInventory) || ((ItemInInventory)object).idTemplate != THE_MUA_BAN) continue;
            return (ItemInInventory)object;
        }
        return null;
    }

    /** Làm ngoài luồng vẽ (gọi từ paint): 0 = về màn chọn server, 1 = đăng nhập, 2 = chọn nhân vật. */
    private static void chay(final int n) {
        dangLam = true;
        new Thread(){

            public void run() {
                try {
                    if (n == 0) {
                        TienIch.veManChonServer();
                    } else if (n == 1) {
                        Thread.sleep(1500L);
                        Canvas.startWaitDlg("Tự đăng nhập lại...", true);
                        Canvas.connect();
                        GameService.gI().setClientType();
                        GameService.gI().login(AutoGuard.tkDangNhap, AutoGuard.mkDangNhap, GameMidlet.version);
                        GameScr.saveAutoFight = false;
                    } else {
                        Thread.sleep(1000L);
                        CharSelectScr.me.cmdSelect.action.perform();
                    }
                }
                catch (Throwable throwable) {
                    // lần sau thử lại
                }
                dangLam = false;
            }
        }.start();
    }

    private static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            String string = RMS.loadRMSString(RMS_TEN);
            if (string != null && string.length() >= 3) {
                tatThongBao = string.charAt(0) == '1';
                tuDangNhap = string.charAt(1) == '1';
            }
            if (string != null && string.length() >= 8) {
                doiMucTieu = string.charAt(4) == '1';
                muaThe = string.charAt(5) == '1';
                muaBinh = string.charAt(6) == '1';
                muaVe = string.charAt(7) == '1';
            }
            if (string != null && string.length() >= 9) {
                suaDo = string.charAt(8) == '1';
            }
        }
        catch (Throwable throwable) {
            // mặc định
        }
    }

    private static void luu() {
        try {
            RMS.saveRMSString(RMS_TEN, new StringBuffer().append(tatThongBao ? '1' : '0').append(tuDangNhap ? '1' : '0').append('1').append('1').append(doiMucTieu ? '1' : '0').append(muaThe ? '1' : '0').append(muaBinh ? '1' : '0').append(muaVe ? '1' : '0').append(suaDo ? '1' : '0').toString());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}
