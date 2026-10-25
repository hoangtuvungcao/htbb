package game.pokemon;

import game.model.Actor;
import game.model.Char;
import game.model.LiveActor;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.HudRong;
import game.render.screen.PauseMenu;
import javax.microedition.lcdui.Graphics;

/**
 * Thao tác chung của màn chơi (chuột / chạm / Enter), kiểu điều khiển MMO nhập vai:
 *  - chuột TRÁI / chạm = hành động chính ở mọi chỗ (chuột phải chỉ là menu phụ, không gì cần chuột phải mới làm được);
 *  - chạm quái = chọn (khung mục tiêu + vòng dưới chân), chạm lại / nút đánh = đánh (tự đi tới tầm: GameScr.moveToRange);
 *  - chạm NPC = đi tới rồi nói chuyện; chạm người chơi = chọn + menu nhỏ (mời nhóm / trao đổi / nhắn tin / kết bạn);
 *  - chạm đồ rơi = nhặt (tự đi tới); chạm đất = đi tới (GameScr.findRoad) + gợn sóng tại điểm đến;
 *  - Enter (PC) / nút ngữ cảnh: {@link #nguCanh()} - nói chuyện NPC, nhặt đồ, câu cá cạnh hồ, đánh quái gần.
 * Mọi việc đi qua đúng phím / hàm của game gốc (phím 5, doParty, findRoad2...) -> cùng giao thức máy chủ.
 */
public final class HanhDong {
    private HanhDong() {
    }

    // ------------------------------------------------------------------ toạ độ

    /** Toạ độ màn (logic) -> thế giới. PC / APK vá hai hàm này theo thu phóng thế giới riêng. */
    public static int wx(int px) {
        return GameScr.cmx + px;
    }

    public static int wy(int py) {
        return GameScr.cmy + py;
    }

    // ------------------------------------------------------------------ mở bảng (HUD gọi; bản APK / PC gọi được)

    private static GameScr gs() {
        GameScr g = Canvas.gameScr;
        return g != null && g.mainChar != null && Canvas.currentScreen == g ? g : null;
    }

    /** Hồ sơ nhân vật (bảng "Thông tin": chỉ số, Pokémon, trang bị) - chạm thẻ trạng thái góc trái trên. */
    public static void moHoSo() {
        if (gs() == null || Canvas.currentDialog != null) {
            return;
        }
        // 0.9.3: màn Nhân vật mới (Pokémon to + ô trang bị + tab Chỉ số / Kỹ năng / Thú / Ngoại hình)
        ManNhanVat.mo(ManNhanVat.TAB_TRANG_BI);
    }

    /** Menu chính dạng lưới ô to (mọi việc thường dùng: 2 chạm từ HUD). */
    public static void moMenu() {
        if (gs() == null || Canvas.currentDialog != null) {
            return;
        }
        ManMenu.mo();
    }

    /** Menu chính (như phím mềm trái). */
    public static void moMenuChinh() {
        GameScr g = gs();
        if (g == null || g.left == null || g.left.action == null) {
            return;
        }
        Canvas.menu.showMenu = false;
        g.left.action.perform();
    }

    /** Bảng của menu "Bản thân": 0 hành trang, 1 trang bị, 2 tiềm năng, 3 kỹ năng (Pokémon), 4 thông tin, 5 nhóm, 6 nhiệm vụ. */
    public static void moBang(int tab) {
        if (gs() == null || Canvas.currentDialog != null) {
            return;
        }
        Canvas.menu.showMenu = false;
        PauseMenu.gI().showMe(tab);
        AmThanh.phat(AmThanh.MO);
    }

    /** Nút "Huấn luyện" / "Tiềm năng" (tab Chỉ số): máy chủ bật Điểm huấn luyện = BangHuanLuyen; không thì PC / APK = bảng
     * cộng điểm HD, J2ME = tab Tiềm năng của bảng cũ. */
    public static void moTiemNang() {
        if (HocChieu.batHL) {
            BangHuanLuyen.mo();   // 0.9.7: Điểm huấn luyện thay điểm tiềm năng tự do (mọi client)
        } else if (KhungVao.moi()) {
            BangTiemNangHD.mo();
        } else {
            moBang(2);
        }
    }

    /**
     * 0.9.6: hộp NHẬP GIFTCODE (kiểu A) từ Menu -> Thư/Quà và Cài đặt -> Tài khoản: cùng gói -31 mục 7 với NPC quà tặng
     * (máy chủ RealController: idMenu 7 = DatabaseGiftCode.newDoGetGiftCode, giới hạn mỗi tháng ở GiftcodeLauDai).
     */
    public static void moGiftcode() {
        game.render.screen.GameScr g = gs();
        if (g == null || g.mainChar == null) {
            return;
        }
        if (Canvas.currentScreen != g) {
            g.switchToMe();
        }
        g.onTextBox(g.mainChar.ID, (byte) 7, "Nhập giftcode", 0);
    }

    public static void moBanDoLon() {
        if (gs() == null) {
            return;
        }
        PauseMenu.moBanDoLon();
    }

    /** Menu hệ sinh thái của máy chủ (câu cá, khảo cổ, nấu ăn, túi, vườn berry...): lệnh chat "hst". */
    public static void moSinhThai() {
        if (gs() == null) {
            return;
        }
        GameService.gI().chat("hst");
    }

    /**
     * Nút câu / Enter cạnh hồ: chưa câu -> thả phao xuống mặt nước + "cauca2" (server 0.9.6.1: chờ cá cắn, phải kéo
     * đúng lúc); đang câu -> kéo cần ("keocan", một lần mỗi lượt - kéo sớm / muộn thì cá thoát). Xa mặt nước: báo ngắn.
     */
    public static boolean cauCa() {
        if (gs() == null) {
            return false;
        }
        if (SinhThai.dangCau()) {
            if (SinhThai.keoCan()) {
                GameService.gI().chat("keocan");
            }
            return true;
        }
        if (!SinhThai.thuCau()) {
            return true;   // đã báo "Hãy đứng gần mặt nước để câu cá"
        }
        GameService.gI().chat("cauca2");
        return true;
    }

    /** APK (nút đánh lớn kiêm nút câu): đang câu, hoặc cạnh hồ mà không nhắm gì. */
    public static boolean nutDanhLaCau() {
        GameScr g = Canvas.gameScr;
        if (g == null || g.mainChar == null) {
            return false;
        }
        if (SinhThai.dangCau()) {
            return true;
        }
        return g.focusedActor == null && SinhThai.coCho();
    }

    // ------------------------------------------------------------------ chạm trong thế giới

    /** Diện bấm (thế giới) của actor: hộp quanh hình thật (chiều cao hình Pokémon / quái đã cập nhật vào height). */
    static boolean trungActor(Actor ac, int x, int y) {
        int w = ac.width;
        if (w < 22) {
            w = 22;
        }
        int h = ac.height;
        if (h < 26) {
            h = 26;
        }
        if (ac instanceof Char) {
            int dy = 40 - ((Char) ac).dyHangTen();   // đỉnh hình thật (như hàng tên)
            if (dy > h) {
                h = dy;
            }
        }
        if (h > 90) {
            h = 90;
        }
        return x >= ac.x - w / 2 - 3 && x <= ac.x + w / 2 + 3 && y >= ac.y - h - 2 && y <= ac.y + 8;
    }

    static boolean chonDuoc(GameScr g, Actor ac) {
        if (ac == null || ac == g.mainChar || ac.ID == g.mainChar.ID && ac.catagory == 0) {
            return false;
        }
        if (ac.catagory == 10 || ac.catagory == 11 || ac.catagory == 12 || ac.catagory == 13 || ac.catagory == 100 || ac.catagory == 126) {
            return false;
        }
        if (ac.isMonster()) {
            LiveActor la = (LiveActor) ac;
            return la.hp > 0 && !ac.wantDestroy;
        }
        return !ac.wantDestroy;
    }

    /** Actor dưới điểm (thế giới), gần tâm hình nhất; null = không có. */
    public static Actor actorTai(int x, int y) {
        GameScr g = Canvas.gameScr;
        if (g == null) {
            return null;
        }
        Actor tot = null;
        int dTot = 1 << 30;
        int n = g.actors.size();
        for (int i = 0; i < n; i++) {
            Actor ac = (Actor) g.actors.elementAt(i);
            if (!chonDuoc(g, ac) || !trungActor(ac, x, y)) {
                continue;
            }
            int h = ac.height < 26 ? 26 : ac.height;
            int dx = ac.x - x, dy = ac.y - h / 2 - y;
            int d = dx * dx + dy * dy;
            if (ac.isItemCanGet()) {
                d -= 200;   // đồ rơi nằm dưới chân quái: ưu tiên nhặt
            }
            if (d < dTot) {
                dTot = d;
                tot = ac;
            }
        }
        return tot;
    }

    /** Mục tiêu người chơi tự chọn (chạm / Enter): giữ, không để tự chọn theo hướng (findFocusActor) thay mất. */
    private static Actor chon;

    /** GameScr.update: true = giữ focusedActor (người chơi đã chọn và mục tiêu còn hợp lệ, trong ~18 ô). */
    public static boolean giuMucTieu(GameScr g) {
        Actor c = chon;
        if (c == null) {
            return false;
        }
        boolean ok = g.focusedActor == c && !c.wantDestroy && g.actors.contains(c);
        if (ok && c.isMonster()) {
            ok = ((LiveActor) c).hp > 0;
        }
        if (ok) {
            int dx = c.x - g.mainChar.x, dy = c.y - g.mainChar.y;
            ok = dx * dx + dy * dy < 290 * 290;
        }
        if (!ok) {
            chon = null;
        }
        return ok;
    }

    /** Bỏ mục tiêu tự chọn (chạm đất / đổi map). */
    public static void boChon() {
        chon = null;
        choNoi = null;
    }

    /** NPC đang chờ nói chuyện khi đi tới (chạm NPC). */
    private static Actor choNoi;
    private static long choNoiLuc;

    /**
     * Chạm vào thế giới tại (x, y) (toạ độ thế giới). true = đã xử lý (trúng actor); false = chạm đất (GameScr tự đi tới).
     * Gọi từ GameScr.updatePoint (chỉ khi đang không có hộp thoại / menu).
     */
    public static boolean chamThe(GameScr g, int x, int y) {
        Actor ac = actorTai(x, y);
        if (ac == null) {
            choNoi = null;
            return false;
        }
        Canvas.isPointerClick = false;
        MenuNguCanh.boCho();   // 0.9.17: menu tới sau chạm này là của mục tiêu (tiêu đề = tên mục tiêu)
        boolean lai = g.focusedActor == ac;
        if (ac.catagory == 0 && !ac.isNPC() && !lai && GameScr.typeOptionFocus != 0 && !choChonNguoi(g, ac)) {
            return true;   // chế độ focus loại trừ người này: không đổi mục tiêu
        }
        g.focusedActor = ac;
        chon = ac;
        if (ac.isItemCanGet()) {
            Canvas.keyPressed[5] = true;          // nhặt (tự đi tới)
            return true;
        }
        if (ac.isNPC() || ac.catagory == 2) {
            noiVoi(g, ac);
            return true;
        }
        if (ac.catagory == 0 && ac instanceof Char) {
            Char c = (Char) ac;
            if (c.idBoss != -1) {                 // boss dạng nhân vật: như quái
                if (lai) {
                    Canvas.keyPressed[5] = true;
                }
                return true;
            }
            if (g.mainChar.pk <= 0 || g.map / 100 == 3 || !laDich(g, ac)) {
                g.doParty();                      // menu nhỏ: mời nhóm / trao đổi / nhắn tin / kết bạn
                return true;
            }
            if (lai) {
                Canvas.keyPressed[5] = true;      // PK: chạm lại = đánh
            }
            return true;
        }
        if (lai) {
            Canvas.keyPressed[5] = true;          // quái: chạm lần 2 = đánh (tự đi tới tầm)
        }
        return true;
    }

    private static boolean choChonNguoi(GameScr g, Actor ac) {
        switch (GameScr.typeOptionFocus) {
            case 1:
                return ac.iskiller && !ac.isNPC();
            case 2:
                return true;
            case 3:
                return ac.idClan != g.mainChar.idClan;
            case 4:
                return ac.nationID != g.mainChar.nationID;
            default:
                return true;
        }
    }

    private static boolean laDich(GameScr g, Actor ac) {
        return ac.iskiller || g.mainChar.iskiller || ac.pk > 0 && g.mainChar.pk > 0 && ac.pk != g.mainChar.pk;
    }

    /** Đi tới NPC rồi nói chuyện (gần thì nói luôn). */
    static void noiVoi(GameScr g, Actor npc) {
        int dx = npc.x - g.mainChar.x, dy = npc.y - g.mainChar.y;
        if (dx * dx + dy * dy <= 44 * 44) {
            choNoi = null;
            Canvas.keyPressed[5] = true;
            return;
        }
        choNoi = npc;
        choNoiLuc = System.currentTimeMillis();
        int ty = npc.y + 18;
        if (game.model.Tilemap.tileTypeAtPixel(npc.x, ty, 2)) {
            ty = npc.y - 4;
        }
        g.findRoad2(npc.x, ty);
        chamDat(npc.x, ty);
    }

    /** Mỗi khung (GameScr.updateKey): NPC đang chờ -> tới gần (hoặc đi không được nữa) thì nói chuyện. */
    public static void tick(GameScr g) {
        Actor n = choNoi;
        if (n == null) {
            return;
        }
        if (g.focusedActor != n || n.wantDestroy || Canvas.currentDialog != null || Canvas.menu.showMenu) {
            choNoi = null;
            return;
        }
        int dx = n.x - g.mainChar.x, dy = n.y - g.mainChar.y;
        long t = System.currentTimeMillis() - choNoiLuc;
        boolean dung = g.mainChar.posTransRoad == null && t > 500;
        if (dx * dx + dy * dy <= 40 * 40 || dung || t > 7000) {
            choNoi = null;
            g.mainChar.posTransRoad = null;   // đứng lại trước mặt NPC (không đi đè lên NPC)
            Canvas.keyPressed[5] = true;
        }
    }

    // ------------------------------------------------------------------ Enter / nút ngữ cảnh

    /** Có việc ngữ cảnh khác đánh: 1 nói chuyện, 2 nhặt, 3 câu cá; 0 = không (nút chính = đánh). */
    public static int loaiNguCanh() {
        GameScr g = Canvas.gameScr;
        if (g == null || g.mainChar == null) {
            return 0;
        }
        if (SinhThai.dangCau()) {
            return 3;   // đang câu: nút câu giữ nguyên chỗ (bấm = kéo cần)
        }
        Actor f = g.focusedActor;
        if (f != null && (f.isNPC() || f.catagory == 2)) {
            return 1;
        }
        if (f != null && f.isItemCanGet()) {
            return 2;
        }
        if ((f == null || f != chon && f.isMonster()) && SinhThai.coCho()) {
            return 3;   // cạnh hồ, không tự chọn quái nào: nút câu cá (quái chỉ được tự nhắm theo hướng thì không tính)
        }
        return 0;
    }

    /** Phím / nút HÀNH ĐỘNG NGỮ CẢNH (Enter PC). true = đã xử lý (không gửi phím 5). */
    public static boolean nguCanh() {
        GameScr g = gs();
        if (g == null || Canvas.currentDialog != null || Canvas.menu.showMenu || g.chatMode || HopThoaiDuoi.dangBat()) {
            return false;
        }
        if (SinhThai.dangCau()) {
            return cauCa();   // đang câu: Enter = kéo cần
        }
        Actor f = g.focusedActor;
        if (f != null && (f.isNPC() || f.catagory == 2)) {
            noiVoi(g, f);
            return true;
        }
        if (f != null && (f == chon || !f.isMonster() || !SinhThai.coCho())) {
            return false;   // quái người chơi chọn / đồ / người: phím 5 của game (đánh / nhặt)
        }
        if (f != null) {
            return cauCa();   // cạnh hồ, quái chỉ được tự nhắm theo hướng: Enter = câu cá
        }
        // không mục tiêu: NPC gần -> nói chuyện; đồ gần -> nhặt; cạnh hồ -> câu cá; quái gần -> chọn + đánh
        Actor npc = ganNhat(g, 1, 72);
        if (npc != null) {
            g.focusedActor = npc;
            chon = npc;
            noiVoi(g, npc);
            return true;
        }
        Actor doRoi = ganNhat(g, 2, 64);
        if (doRoi != null) {
            g.focusedActor = doRoi;
            chon = doRoi;
            return false;
        }
        if (SinhThai.coCho()) {
            return cauCa();
        }
        Actor quai = ganNhat(g, 3, 120);
        if (quai != null) {
            g.focusedActor = quai;
            chon = quai;
            return false;
        }
        return false;
    }

    /** Nút đánh khi chưa có mục tiêu: chọn quái gần nhất (tầm ~10 ô). */
    /**
     * HUD có nút chiêu RIÊNG (màn rộng HudRong; bản APK vá thêm HUD HD): chiêu không bao giờ là "nói chuyện". J2ME màn
     * nhỏ giữ hành vi gốc (khi nhắm NPC, thanh ô đáy thành nút "Giao tiếp" - phím ô nào cũng nói chuyện).
     */
    public static boolean nutChieuRieng() {
        return HudRong.bat();
    }

    /**
     * GameScr.doFire với phím chiêu (khác 5): đang nhắm NPC -> bỏ nhắm NPC, tự nhắm quái gần nhất (không có thì
     * không mục tiêu: chiêu bổ trợ dùng cho mình, chiêu đánh không làm gì) - không mở cửa hàng / menu NPC.
     * Ô thuốc giữ nguyên mục tiêu.
     */
    public static void boNhamNpcKhiDungChieu(GameScr g, game.model.QuickSlot qs) {
        if (!laNpc(g.focusedActor)) {
            return;
        }
        if (qs != null && qs.quickslotType == game.model.QuickSlot.TYPE_POTION) {
            return;
        }
        boNhamNpc(g);
    }

    /** 0.9.19: actor là NPC (NPC tĩnh, NPC máy chủ, NPC dạng nhân vật) - không bao giờ là mục tiêu đánh. */
    public static boolean laNpc(Actor a) {
        return a != null && (a.catagory == 2 || a.isNPC() || a.isNpcChar());
    }

    /** Bỏ nhắm NPC, nhắm quái gần nhất (không có thì không mục tiêu). */
    public static void boNhamNpc(GameScr g) {
        boChon();
        g.focusedActor = null;
        tuChonQuai(g);
    }

    public static void tuChonQuai(GameScr g) {
        Actor q = ganNhat(g, 3, 160);
        if (q != null) {
            g.focusedActor = q;
            chon = q;
        }
    }

    /** loai 1 NPC, 2 đồ rơi, 3 quái. */
    static Actor ganNhat(GameScr g, int loai, int banKinh) {
        Actor tot = null;
        int dTot = banKinh * banKinh;
        int n = g.actors.size();
        for (int i = 0; i < n; i++) {
            Actor ac = (Actor) g.actors.elementAt(i);
            if (!chonDuoc(g, ac)) {
                continue;
            }
            boolean dung = loai == 1 ? (ac.isNPC() || ac.catagory == 2) && !(ac instanceof Char && ((Char) ac).idBoss != -1 && !ac.isNpcChar())
                    : loai == 2 ? ac.isItemCanGet() : ac.isMonster();
            if (!dung) {
                continue;
            }
            int dx = ac.x - g.mainChar.x, dy = ac.y - g.mainChar.y;
            int d = dx * dx + dy * dy;
            if (d < dTot) {
                dTot = d;
                tot = ac;
            }
        }
        return tot;
    }

    // ------------------------------------------------------------------ vùng bấm (con trỏ bàn tay trên PC)

    /**
     * (x, y) toạ độ LOGIC màn hình có thứ bấm được (nút HUD, thẻ trạng thái, bản đồ nhỏ, NPC / quái / người / đồ rơi)?
     * Bản PC gọi khi rê chuột để đổi con trỏ bàn tay.
     */
    public static boolean laVungBam(int x, int y) {
        try {
            GameScr g = Canvas.gameScr;
            if (g == null || Canvas.currentScreen != g || Canvas.currentDialog != null || Canvas.menu.showMenu) {
                return false;
            }
            if (HudRong.trungNut(x, y)) {
                return true;
            }
            return actorTai(wx(x), wy(y)) != null;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Chuột PHẢI (PC, toạ độ logic) = menu phụ: trên người chơi -> chọn + menu nhỏ (nhóm / trao đổi / nhắn tin / kết bạn);
     * trên NPC -> nói chuyện; trên quái / đồ -> chọn. true = đã xử lý; false = không có gì (lớp nền làm phím Thoát).
     * Không thứ gì CHỈ làm được bằng chuột phải: chuột trái làm được hết.
     */
    public static boolean chuotPhai(int x, int y) {
        if (NgamTay.huy()) {
            return true;   // 0.9.8: chuột phải khi đang ngắm = huỷ phát bắn
        }
        // 0.9.7: chuột phải trên ô đồ của lưới hành trang = thao tác nhanh (mặc / dùng / bán)
        if (Canvas.currentDialog == null && LuoiTui.chuotPhai(x, y)) {
            return true;
        }
        GameScr g = gs();
        if (g == null || Canvas.currentDialog != null || Canvas.menu.showMenu || g.chatMode) {
            return false;
        }
        Actor ac = actorTai(wx(x), wy(y));
        if (ac == null) {
            return false;
        }
        g.focusedActor = ac;
        chon = ac;
        MenuNguCanh.boCho();
        if (ac.catagory == 0 && ac instanceof Char && ((Char) ac).idBoss == -1 && !ac.isNPC()) {
            g.doParty();
        } else if (ac.isNPC() || ac.catagory == 2) {
            noiVoi(g, ac);
        }
        return true;
    }

    // ------------------------------------------------------------------ gợn sóng điểm đến + thông báo nhặt đồ

    private static int gonX, gonY, gonTick = -100;

    /** Điểm đến khi chạm đất (thế giới). */
    public static void chamDat(int x, int y) {
        gonX = x;
        gonY = y;
        gonTick = Canvas.gameTick;
    }

    /** Vẽ trong lớp thế giới (sau mặt đất, trước nhân vật). */
    public static void veGon(Graphics g) {
        int t = Canvas.gameTick - gonTick;
        if (t < 0) {
            t += 10001;
        }
        if (t > 14) {
            return;
        }
        int r = 4 + t;
        g.setColor(t < 7 ? 0xFFFFFF : 0xA8D8FF);
        g.drawArc(gonX - r, gonY - r / 2, r * 2, r, 0, 360);
        if (t < 9) {
            int r2 = r / 2 + 1;
            g.setColor(GiaoDien.VANG);
            g.drawArc(gonX - r2, gonY - r2 / 2, r2 * 2, r2, 0, 360);
        }
    }

    private static final String[] BAO = new String[3];
    private static final long[] BAO_LUC = new long[3];

    /** Thông báo nhanh (nhặt đồ / xu) giữa trên màn, tự mờ. */
    public static void bao(String s) {
        if (s == null || s.length() == 0) {
            return;
        }
        for (int i = BAO.length - 1; i > 0; i--) {
            BAO[i] = BAO[i - 1];
            BAO_LUC[i] = BAO_LUC[i - 1];
        }
        BAO[0] = s;
        BAO_LUC[0] = System.currentTimeMillis();
    }

    /** Vẽ thông báo nhanh, đỉnh tại y (toạ độ màn). */
    public static void veBao(Graphics g, int y) {
        long now = System.currentTimeMillis();
        Font f = Font.borderFont;
        for (int i = 0; i < BAO.length; i++) {
            String s = BAO[i];
            if (s == null) {
                continue;
            }
            long tuoi = now - BAO_LUC[i];
            if (tuoi > 2600) {
                BAO[i] = null;
                continue;
            }
            // 0.9.7 (NRO): bảng tối viền xanh lá; không chồng thẻ người chơi trái trên (rộng quá thì xuống dưới thẻ)
            int wMax = Canvas.w - 16;
            s = GiaoDien.catChu(f, s, wMax - 16);
            int w = f.getWidth(s) + 16;
            int x = Canvas.hw - w / 2;
            int y0 = x < TheNguoiChoi.RONG + 10 ? Math.max(y, TheNguoiChoi.CAO + 8) : y;
            int yy = y0 + i * 18 - (int) (tuoi < 160 ? (160 - tuoi) / 20 : 0);
            g.setColor(ChuDe.LA);
            g.fillRect(x - 1, yy - 1, w + 2, 18);
            if (!KhungNro.ve(g, KhungNroBang.TOOLTIP, x, yy, w, 16)) {
                GiaoDien.khoi(g, x, yy, w, 16, 2, 0x2B1A10);
            }
            f.drawString(g, s, Canvas.hw, yy + 1, Font.CENTER);
        }
    }
}
