package game.render.screen;

import game.model.Actor;
import game.model.AttackResult;
import game.model.Char;
import game.render.Res;
import game.model.ItemTemplate;
import game.model.ItemInInventory;
import game.model.CMLocation;
import game.model.ChatInfo;
import game.model.Command;
import game.model.Dropable;
import game.model.IAction;
import game.model.LiveActor;
import game.model.MainChar;
import game.model.PartyInfo;
import game.model.QuickSlot;
import game.model.Tilemap;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * Auto up (đánh quái), chạy song song với game - KHÔNG dùng khung autoRunning của các auto chế đồ
 * (khung đó ẩn giao diện và nuốt thông báo). Gọi mỗi khung hình từ PauseMenu.autoCheDoTick.
 *  - Đứng im: chỉ đánh quái trong tầm skill, không tự đi (trừ khi bị đẩy xa điểm đứng thì quay về).
 *  - Di chuyển: đi khắp map - tới quái gần nhất (dò đường BFS vòng qua tường), không thấy quái nào
 *    thì đi tuần tới một ô ngẫu nhiên trên map để tìm quái.
 * Cài đặt dùng CHUNG với màn Auto gốc (BangAuto): tab Đánh = skill đánh/buff (ô phím tắt; chưa đặt
 * thì dùng mọi skill đã học), tab Trị = % HP/MP uống bình, tab Nhặt = 3 ô nhặt.
 * Ưu tiên skill lan khi có >= 2 quái; tự lập nhóm + mời (theo danh sách tên / cả map) là tùy chọn của mod.
 * Skill/bình/nhặt đều đi qua đúng hàm của game (doUseSkillToFocusActor, doUsePotion, ...) nên
 * server kiểm như người chơi bấm tay (tầm 130px, hồi chiêu, MP, chờ bình 250ms).
 */
final class AutoUp {
    private static final String RMS_TEN = "kpahup";
    private static final int[] BINH_HP = new int[]{94, 93, 22, 21, 3, 2, 1};
    private static final int[] BINH_MP = new int[]{96, 95, 24, 23, 6, 5, 4};
    /** Loại skill buff theo phái (y BangAuto.isBuffSkillAuto); phái 2 loại 6 là hồi sinh -> bỏ. */
    private static final byte[][] SKILL_BUFF = new byte[][]{{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};

    static boolean dang;
    // cài đặt, lưu RMS
    private static boolean diChuyen;
    /** Mời vào nhóm: 0 = tắt, 1 = theo danh sách tên tự nhập, 2 = tất cả người chơi trong map. */
    private static int cheDoMoi;
    private static final String[] TEN_CHE_DO_MOI = new String[]{"tắt", "danh sách", "cả map"};
    /** Nhận lời mời khi đang auto up: 0 = tắt (hộp thoại như thường), 1 = chỉ người trong danh sách, 2 = mọi người. */
    private static int cheDoNhan = 2;
    private static final String[] TEN_CHE_DO_NHAN = new String[]{"tắt", "danh sách", "tất cả"};
    /** Danh sách tên dùng chung cho mời và nhận lời mời "theo danh sách" (chữ thường). */
    private static final Vector danhSach = new Vector();
    /** Tắt hiệu ứng ra đòn của nhân vật: gửi thẳng lệnh đánh, không diễn animation (không khựng). */
    private static boolean tatHieuUng = true;
    private static boolean daNap;

    private static int neoX;
    private static int neoY;
    private static int neoMap = -1;
    private static boolean daChet;
    private static Actor mucTieu;
    private static Actor monNhat;
    private static long lucDanh;
    private static long lucBinh;
    private static long lucBuff;
    private static long lucNhom;
    private static long lucTaoNhom;
    private static long lucNhuong;
    private static long lucKet;
    private static int ketX;
    private static int ketY;
    private static boolean giuPhim;
    private static int giet;
    private static String trangThai = "";
    // đường đi BFS (các góc rẽ, chỉ số ô) tới ô đích duongDich; tuanTra = ô đích lúc đi tuần
    private static int[] duong;
    private static int duongPos;
    private static int duongDich = -1;
    private static int tuanTra = -1;
    private static int lanKet;
    private static final Hashtable daMoi = new Hashtable();
    private static final Hashtable boQua = new Hashtable();

    private AutoUp() {
    }

    // ------------------------------------------------------------------ menu

    static void moMenu(final int chon) {
        AutoUp.nap();
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(dang ? "Tắt auto up" : "Bật auto up", new IAction(){

            public void perform() {
                if (dang) {
                    AutoUp.tat();
                } else {
                    AutoUp.bat();
                }
            }
        }));
        vector.addElement(new Command(diChuyen ? "Chế độ: di chuyển" : "Chế độ: đứng im", new IAction(){

            public void perform() {
                diChuyen = !diChuyen;
                AutoUp.luu();
                AutoUp.moMenu(1);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Mời PT: ").append(TEN_CHE_DO_MOI[cheDoMoi]).toString(), new IAction(){

            public void perform() {
                cheDoMoi = (cheDoMoi + 1) % TEN_CHE_DO_MOI.length;
                AutoUp.luu();
                AutoUp.moMenu(2);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Nhận PT: ").append(TEN_CHE_DO_NHAN[cheDoNhan]).toString(), new IAction(){

            public void perform() {
                cheDoNhan = (cheDoNhan + 1) % TEN_CHE_DO_NHAN.length;
                AutoUp.luu();
                AutoUp.moMenu(3);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Danh sách (").append(danhSach.size()).append(")").toString(), new IAction(){

            public void perform() {
                AutoUp.moDanhSach(0);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("HP ").append(BangAuto.bomHp).append("% MP ").append(BangAuto.bomMp).append("% (màn Auto)").toString(), new IAction(){

            public void perform() {
                AutoUp.moMenu(5);
            }
        }));
        vector.addElement(new Command(TienIch.autoLogin() ? "Auto login: bật" : "Auto login: tắt", new IAction(){

            public void perform() {
                TienIch.lat(2);
                AutoUp.moMenu(6);
            }
        }));
        vector.addElement(new Command(TienIch.bat(3) ? "Tự mua bình HP/MP 15k: bật" : "Tự mua bình HP/MP 15k: tắt", new IAction(){

            public void perform() {
                TienIch.lat(3);
                AutoUp.moMenu(7);
            }
        }));
        vector.addElement(new Command(TienIch.bat(4) ? "Tự mua vé giờ vàng 100: bật" : "Tự mua vé giờ vàng 100: tắt", new IAction(){

            public void perform() {
                TienIch.lat(4);
                AutoUp.moMenu(8);
            }
        }));
        vector.addElement(new Command(TienIch.bat(5) ? "Tự mua thẻ mua bán: bật" : "Tự mua thẻ mua bán: tắt", new IAction(){

            public void perform() {
                TienIch.lat(5);
                AutoUp.moMenu(9);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    /** Menu danh sách tên (dùng cho mời + nhận lời mời): thêm tên (nhiều tên cách nhau dấu phẩy), bấm một tên để xóa, xóa hết. */
    static void moDanhSach(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command("Thêm tên", new IAction(){

            public void perform() {
                Canvas.inputDlg.setInfo("Tên, cách nhau dấu phẩy", new IAction(){

                    public void perform() {
                        AutoUp.themTen(Canvas.inputDlg.tfInput.getText());
                        Canvas.endDlg();
                        AutoUp.moDanhSach(0);
                    }
                }, 0, 60, true);
                Canvas.inputDlg.show();
            }
        }));
        for (int i = 0; i < danhSach.size(); ++i) {
            final String string = (String)danhSach.elementAt(i);
            vector.addElement(new Command(new StringBuilder().append("Xóa ").append(string).toString(), new IAction(){

                public void perform() {
                    danhSach.removeElement(string);
                    AutoUp.luu();
                    AutoUp.moDanhSach(0);
                }
            }));
        }
        if (danhSach.size() > 0) {
            vector.addElement(new Command("Xóa hết", new IAction(){

                public void perform() {
                    danhSach.removeAllElements();
                    AutoUp.luu();
                    AutoUp.moDanhSach(0);
                }
            }));
        }
        vector.addElement(new Command("Quay lại", new IAction(){

            public void perform() {
                AutoUp.moMenu(4);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    private static void themTen(String string) {
        if (string == null) {
            return;
        }
        int n = 0;
        while (n <= string.length()) {
            int n2 = string.indexOf(44, n);
            if (n2 < 0) {
                n2 = string.length();
            }
            String string2 = string.substring(n, n2).trim().toLowerCase();
            n = n2 + 1;
            if (string2.length() == 0 || danhSach.contains(string2) || danhSach.size() >= 30) continue;
            danhSach.addElement(string2);
        }
        AutoUp.luu();
    }

    private static boolean trongDanhSach(String string) {
        return string != null && danhSach.contains(string.toLowerCase());
    }

    private static void bat() {
        GameScr gameScr = Canvas.gameScr;
        if (gameScr == null || gameScr.mainChar == null) {
            return;
        }
        if (AutoUp.mapCam()) {
            Canvas.startOKDlg("Map này không đánh quái được.");
            return;
        }
        AutoVanTieu.tat();
        AutoVanMay.tat();
        // ra đòn + bơm HP/MP giao cho auto gốc của game (màn Auto: skill ô 1/3/5, bình ô 7/9, ngưỡng
        // HP/MP); auto up chỉ lo tìm quái / đi tới quái / về chỗ cũ / nhóm / nhặt
        AutoUp.batAutoGoc();
        neoMap = -1;
        mucTieu = null;
        monNhat = null;
        giet = 0;
        daChet = false;
        daMoi.clear();
        boQua.clear();
        duong = null;
        duongDich = -1;
        tuanTra = -1;
        dang = true;
        Canvas.menu.showMenu = false;
    }

    /**
     * Auto gốc của game: luôn bơm HP/MP; đánh thì chỉ ở chế độ đứng im - đòn của auto gốc có
     * animation làm nhân vật đứng lại, nên chế độ di chuyển tự đánh (không animation) để vừa đi
     * vừa đánh.
     */
    private static void batAutoGoc() {
        GameScr.autoFight = !diChuyen;
        GameScr.saveAutoFight = !diChuyen;
        GameScr.autoBomHMP = true;
        if (Canvas.autoTab != null) {
            Canvas.autoTab.isAutoFire = true;
            Canvas.autoTab.isAutoMHP = true;
        }
    }

    static void tat() {
        if (dang) {
            GameScr.autoFight = false;
            GameScr.saveAutoFight = false;
            GameScr.autoBomHMP = false;
        }
        dang = false;
        AutoUp.nha();
        mucTieu = null;
        monNhat = null;
        Canvas.menu.showMenu = false;
    }

    // ------------------------------------------------------------------ vòng chính

    static void tick() {
        if (!dang) {
            return;
        }
        GameScr gameScr = Canvas.gameScr;
        if (gameScr == null || gameScr.mainChar == null) {
            return;
        }
        MainChar mainChar = gameScr.mainChar;
        long l = System.currentTimeMillis();
        if (Canvas.currentScreen != gameScr || Canvas.currentDialog != null || Canvas.menu.showMenu) {
            AutoUp.nha();
            trangThai = buocSua > 0 ? "sửa đồ" : "tạm dừng";
            return;
        }
        if (AutoUp.suaDo(gameScr, mainChar, l)) {
            return;
        }
        if (AutoUp.nguoiBam()) {
            lucNhuong = l + 3000L;
            AutoUp.nha();
        }
        if (l < lucNhuong) {
            trangThai = "chờ bạn";
            return;
        }
        if (AutoUp.mapCam()) {
            AutoUp.nha();
            trangThai = "map cấm đánh";
            return;
        }
        if (mainChar.hp <= 0) {
            AutoUp.nha();
            mucTieu = null;
            monNhat = null;
            daChet = true;
            trangThai = "chờ hồi sinh";
            return;
        }
        // điểm đứng: lúc bật, sau khi hồi sinh (server "Auto về map up") hoặc đổi map
        if (daChet || neoMap != gameScr.map) {
            daChet = false;
            neoMap = gameScr.map;
            neoX = mainChar.x;
            neoY = mainChar.y;
            mucTieu = null;
            monNhat = null;
        }
        // game tự tắt auto gốc khi chết / mất kết nối / về làng -> bật lại
        AutoUp.batAutoGoc();
        if (l >= lucNhom) {
            lucNhom = l + 3000L;
            AutoUp.nhom(gameScr, mainChar, l);
        }
        AutoUp.buff(mainChar, l);
        if (AutoUp.nhat(gameScr, mainChar, l)) {
            return;
        }
        AutoUp.danh(gameScr, mainChar, l);
    }

    // ------------------------------------------------------------------ sửa đồ

    /** Độ bền (mDurable) vũ khí đang đeo <= mức này thì đi sửa. Mỗi đòn trừ 1. */
    private static final int BEN_SUA = 50;
    private static int buocSua;
    private static long lucSua;
    private static long lucSuaTiep;

    /**
     * Luôn chạy khi auto up bật. Vũ khí sắp hết bền: dùng Thẻ mua bán (server đưa vào nhà 103, nhớ chỗ đứng) -> sửa tất cả
     * (lệnh 72, sửa được ở mọi nhà) -> ra cửa (server trả về đúng chỗ cũ trên map up). true = đang
     * bận sửa, vòng auto up dừng ở đây.
     */
    private static boolean suaDo(GameScr gameScr, MainChar mainChar, long l) {
        boolean bl = Tilemap.isOfflineMap || gameScr.isOffLineMap(gameScr.map);
        switch (buocSua) {
            case 0: {
                if (bl || l < lucSuaTiep || mainChar.hp <= 0 || !AutoUp.canSua(mainChar)) {
                    return false;
                }
                ItemInInventory itemInInventory = TienIch.theMuaBan();
                if (itemInInventory == null) {
                    lucSuaTiep = l + 60000L;
                    trangThai = "hết thẻ mua bán";
                    return false;
                }
                AutoUp.nha();
                TienIch.anSua = 2;
                GameService.gI().useItem(itemInInventory.itemID);
                buocSua = 1;
                lucSua = l;
                trangThai = "dùng thẻ mua bán";
                return true;
            }
            case 1: {
                if (bl) {
                    buocSua = 2;
                    lucSua = l + 800L;
                } else if (l - lucSua > 10000L) {
                    // không vào được (thẻ đang hồi 5 phút, đang vận tiêu...): thử lại sau 1 phút
                    buocSua = 0;
                    lucSuaTiep = l + 60000L;
                }
                return true;
            }
            case 2: {
                if (l < lucSua) {
                    return true;
                }
                TienIch.anSua = 2;
                GameService.gI().repairItem(2);
                buocSua = 3;
                lucSua = l + 1500L;
                trangThai = "sửa đồ";
                return true;
            }
            case 3: {
                if (l < lucSua) {
                    return true;
                }
                Vector vector = Tilemap.mapchangeLocations;
                CMLocation cMLocation = vector == null || vector.size() == 0 ? null : (CMLocation)vector.elementAt(0);
                if (cMLocation == null) {
                    buocSua = 0;
                    lucSuaTiep = l + 60000L;
                    return false;
                }
                GameService.gI().changeMap(GameScr.oldMap, cMLocation.toX, cMLocation.toY);
                ChangScr.gI().switchToMe();
                Res.removeMonsterTemplet();
                buocSua = 4;
                lucSua = l;
                trangThai = "ra map up";
                return true;
            }
            case 4: {
                if (!bl) {
                    buocSua = 0;
                    lucSuaTiep = l + 30000L;
                    return false;
                }
                if (l - lucSua > 10000L) {
                    buocSua = 3;
                }
                return true;
            }
        }
        buocSua = 0;
        return false;
    }

    /** Vũ khí đang đeo còn ít bền. */
    private static boolean canSua(MainChar mainChar) {
        Vector vector = mainChar.wearingItems;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if (itemTemplate == null || itemTemplate.type < 3 || itemTemplate.type > 7) continue;
            return itemInInventory.mDurable <= BEN_SUA;
        }
        return false;
    }

    private static boolean mapCam() {
        return Tilemap.isOfflineMap || Tilemap.lv == 0 || Tilemap.lv == 70 || Tilemap.lv == 80 || Tilemap.lv == 201;
    }

    /** Chỉ phím di chuyển (2/4/6/8) / chạm màn hình: auto gốc của game tự "bấm" phím 1/3/5 để ra đòn. */
    private static boolean nguoiBam() {
        if (Canvas.keyPressed[2] || Canvas.keyPressed[4] || Canvas.keyPressed[6] || Canvas.keyPressed[8]) {
            return true;
        }
        return Canvas.isPointerDown || Canvas.isPointerClick;
    }

    // ------------------------------------------------------------------ bình

    private static void binh(GameScr gameScr, MainChar mainChar, long l) {
        if (l < lucBinh) {
            return;
        }
        int n = BangAuto.bomHp;
        if (n > 0 && mainChar.maxhp > 0 && (long)mainChar.hp * 100L < (long)mainChar.maxhp * (long)n && AutoUp.uong(gameScr, mainChar, BINH_HP, l)) {
            lucBinh = l + 300L;
            return;
        }
        n = BangAuto.bomMp;
        if (n > 0 && mainChar.maxmp > 0 && (long)mainChar.mp * 100L < (long)mainChar.maxmp * (long)n && AutoUp.uong(gameScr, mainChar, BINH_MP, l)) {
            lucBinh = l + 300L;
        }
    }

    /** Bình mạnh nhất còn trong túi, không phụ thuộc ô phím tắt. */
    private static boolean uong(GameScr gameScr, MainChar mainChar, int[] nArray, long l) {
        for (int i = 0; i < nArray.length; ++i) {
            int n = nArray[i];
            if (n >= mainChar.potions.length || mainChar.potions[n] <= 0) continue;
            if (MainChar.listPotion != null && n < MainChar.listPotion.length && MainChar.listPotion[n] != null && l - mainChar.potionLastTimeUse[n] < MainChar.listPotion[n].delay) {
                return false;
            }
            gameScr.doUsePotion(n);
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ buff

    private static void buff(MainChar mainChar, long l) {
        if (l < lucBuff) {
            return;
        }
        lucBuff = l + 500L;
        byte[] byArray = AutoUp.skillBuff(mainChar);
        for (int i = 0; i < byArray.length; ++i) {
            byte by = byArray[i];
            if (mainChar.clazz == 2 && by == 6 || by < 4 || by >= Char.skillLevelLearnt.length || !AutoUp.buffChuDong(mainChar, by)) continue;
            byte by2 = Char.skillLevelLearnt[by];
            if (by2 <= 0 || l - mainChar.timeLastUseSkills[by] <= mainChar.coolDownSkill[by]) continue;
            // MP tính y như GameScr.doFire (mức skill của loại by-4)
            int n = SkillManager.getSkillMP(by, Char.skillLevelLearnt[by - 4], mainChar.clazz);
            if (n > mainChar.mp) continue;
            GameService.gI().useBuff(mainChar.ID, (byte)0, SkillManager.EFF_BUFF_SKILL[mainChar.clazz][by - 4], (short)0);
            mainChar.mp -= n;
            mainChar.coolDownSkill[by] = SkillManager.getSkillCooldown(by, mainChar.clazz, by2);
            mainChar.timeLastUseSkills[by] = l;
            lucBuff = l + 800L;
            return;
        }
    }

    /** Skill trong ô phím tắt (tab Đánh của màn Auto ghi vào đây): buff = true lấy ô buff. */
    private static byte[] skillOPhim(boolean bl) {
        Vector vector = new Vector();
        for (int i = 0; MainChar.quickSlot != null && i < MainChar.quickSlot.length; ++i) {
            for (int j = 0; MainChar.quickSlot[i] != null && j < MainChar.quickSlot[i].length; ++j) {
                QuickSlot quickSlot = MainChar.quickSlot[i][j];
                if (quickSlot == null || quickSlot.quickslotType != 1 || quickSlot.isBuff != bl) continue;
                Byte by = new Byte(quickSlot.getSkillType());
                if (vector.contains(by)) continue;
                vector.addElement(by);
            }
        }
        byte[] byArray = new byte[vector.size()];
        for (int i = 0; i < byArray.length; ++i) {
            byArray[i] = (Byte)vector.elementAt(i);
        }
        return byArray;
    }

    /**
     * Buff chủ động = dùng được bằng lệnh 51. Skill bị động (SKILL_CAN_BUFF_TO_USER < 0) luôn có
     * tác dụng sẵn, gửi lệnh dùng thì server trả "Không thể sử dụng" (CharManager.isBuffActive).
     */
    private static boolean buffChuDong(MainChar mainChar, int n) {
        try {
            return SkillManager.SKILL_CAN_BUFF_TO_USER[mainChar.clazz][n - 4] > -1;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /** Buff: MỌI buff chủ động của phái (đã học mới dùng), không phụ thuộc ô phím tắt. */
    private static byte[] skillBuff(MainChar mainChar) {
        return SKILL_BUFF[Math.min(Math.max(0, mainChar.clazz), SKILL_BUFF.length - 1)];
    }

    /**
     * Skill đánh: MỌI skill đánh đã học (từ mạnh xuống), không phụ thuộc ô phím tắt - trước đây đặt
     * skill vào ô 1/3/5 cho auto gốc thì auto up chỉ còn dùng đúng mấy skill đó.
     */
    private static byte[] skillDanh(MainChar mainChar) {
        byte[] byArray;
        Vector vector = new Vector();
        for (int i = mainChar.getnSkill() - 1; i >= 0; --i) {
            if (AutoUp.laBuff(mainChar, i)) continue;
            vector.addElement(new Byte((byte)i));
        }
        byArray = new byte[vector.size()];
        for (int i = 0; i < byArray.length; ++i) {
            byArray[i] = (Byte)vector.elementAt(i);
        }
        return byArray;
    }

    private static boolean laBuff(MainChar mainChar, int n) {
        byte[] byArray = SKILL_BUFF[Math.min(Math.max(0, mainChar.clazz), SKILL_BUFF.length - 1)];
        for (int i = 0; i < byArray.length; ++i) {
            if (byArray[i] != n) continue;
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ đánh

    private static boolean quaiSong(GameScr gameScr, Actor actor) {
        return actor != null && actor.catagory == 1 && !actor.isMineral() && actor.state != 8 && actor.state != 3 && actor.state != 5 && ((LiveActor)actor).hp > 0 && gameScr.actors.contains(actor);
    }

    private static void danh(GameScr gameScr, MainChar mainChar, long l) {
        if (diChuyen) {
            // di chuyển: chỉ đi tuần khắp map, không rẽ về phía con quái nào; đi ngang qua con nào
            // trong ô đánh thì đánh con đó (đổi con bị đánh mỗi 0.1s)
            mucTieu = null;
            Actor actor = AutoUp.quaiTrongO(gameScr, mainChar, l);
            if (actor != null && l >= lucDanh) {
                mucTieu = actor; // chonSkill đếm quái quanh mục tiêu để chọn chiêu lan
                byte by = AutoUp.chonSkill(gameScr, mainChar, 0, l);
                mucTieu = null;
                if (by >= 0) {
                    AutoUp.tungChieu(gameScr, mainChar, actor, by, l);
                    lucDanh = l + 350L;
                }
            }
            AutoUp.diTuan(mainChar, l);
            trangThai = actor != null ? "đi tuần + đánh" : "đi tuần";
            return;
        }
        if (mucTieu != null && !AutoUp.quaiSong(gameScr, mucTieu)) {
            if (((LiveActor)mucTieu).hp <= 0 || mucTieu.state == 8 || mucTieu.state == 3) {
                ++giet;
            }
            mucTieu = null;
        }
        int n = AutoUp.tamXaNhat(mainChar);
        if (mucTieu == null) {
            mucTieu = AutoUp.chonQuai(gameScr, mainChar, n, l);
            lucKet = 0L;
        }
        if (mucTieu == null) {
            if (diChuyen) {
                AutoUp.diTuan(mainChar, l);
                return;
            }
            // đứng im: bị đẩy xa > 60px khỏi điểm đứng thì quay về
            if (Util.distance(mainChar.x, mainChar.y, neoX, neoY) > 60) {
                AutoUp.diToi(mainChar, neoX, neoY, l, null);
                trangThai = "về chỗ cũ";
            } else {
                AutoUp.nha();
                trangThai = "chờ quái";
            }
            return;
        }
        // đứng im: đổi mục tiêu mỗi 0.1s sang con khác gần nhất trong tầm (chia đòn cho cả đám).
        // Di chuyển thì giữ mục tiêu để đi (đổi liên tục sẽ đi giật qua lại) - đổi con BỊ ĐÁNH
        // trong vuaDiVuaDanh.
        if (!diChuyen && l - lucDoi >= DOI_MUC_TIEU) {
            lucDoi = l;
            Actor actor = AutoUp.quaiKhac(gameScr, mainChar, n, l);
            if (actor != null) {
                mucTieu = actor;
            }
        }
        if (diChuyen) {
            AutoUp.vuaDiVuaDanh(gameScr, mainChar, l);
            return;
        }
        int n2 = Util.distance(mainChar.x, mainChar.y, mucTieu.x, mucTieu.y);
        int n3 = AutoUp.tamGanNhat(mainChar);
        if (n2 <= n3 - 8 || !diChuyen && n2 <= n + 10) {
            // trong tầm: đứng lại; auto gốc của game đánh bằng ô 1/3/5, auto up đánh thêm bằng mọi
            // chiêu đã học còn lại (chiêu nào hồi xong, đủ MP, tới tầm thì tung)
            AutoUp.nha();
            gameScr.focusedActor = mucTieu;
            byte by = AutoUp.chonSkill(gameScr, mainChar, n2, l);
            if (by >= 0 && l >= lucDanh) {
                AutoUp.tungChieu(gameScr, mainChar, mucTieu, by, l);
                lucDanh = l + 350L;
            }
            trangThai = "đang đánh";
            return;
        }
        if (!diChuyen) {
            // đứng im: quái ra khỏi tầm thì đổi mục tiêu
            mucTieu = null;
            trangThai = "chờ quái";
            return;
        }
        // di chuyển: chưa tới tầm -> đi lại gần (tới tầm skill gần nhất); gần thì giữ phím thẳng
        // tới, xa thì đi theo đường BFS vòng qua tường
        if (n2 <= 64) {
            AutoUp.diToi(mainChar, mucTieu.x, mucTieu.y, l, mucTieu);
        } else {
            AutoUp.diTheoDuong(mainChar, mucTieu.x / 16, mucTieu.y / 16, l, mucTieu);
        }
        trangThai = "đi tới quái";
    }

    /**
     * Ra đòn. Tắt hiệu ứng: làm đúng phần gửi lệnh của GameScr.doUseSkillToFocusActor (trừ MP, hồi
     * chiêu, lệnh 9 / 106 đánh lan) nhưng bỏ mainChar.startAttack - hàm đó chỉ đặt hồi chiêu rồi
     * chuyển nhân vật sang trạng thái ra đòn để diễn animation. Server tính sát thương như thường.
     */
    private static void tungChieu(GameScr gameScr, MainChar mainChar, Actor actor, byte by, long l) {
        gameScr.focusedActor = actor;
        if (!tatHieuUng) {
            gameScr.doUseSkillToFocusActor(by);
            return;
        }
        byte by2 = Char.skillLevelLearnt[by];
        // quay mặt về quái chỉ khi đứng im: camera của game lệch theo hướng mặt (DELTACAMERA[dir]),
        // vừa đi vừa đánh mà quay mặt theo con bị đánh (đổi 0.1s) thì màn hình giật qua lại
        if (!diChuyen) {
            mainChar.dir = (byte)Util.findDirection(mainChar, actor);
        }
        mainChar.mp -= SkillManager.getSkillMP(by, by2, mainChar.clazz);
        mainChar.coolDownSkill[by] = SkillManager.getSkillCooldown(by, mainChar.clazz, by2);
        mainChar.timeLastUseSkills[by] = l;
        AttackResult attackResult = new AttackResult();
        attackResult.dam = 2000000;
        if (SkillManager.isSkillAeo(mainChar.clazz, by)) {
            Vector vector = gameScr.getNearMonster(actor, SkillManager.getRangeSkillAeo(mainChar.clazz, by));
            GameService.gI().attackMultiMonster(actor.ID, by, attackResult.effect, attackResult.dam, 0, attackResult.buffEffect, vector);
        } else {
            GameService.gI().attackMonster(actor.ID, by, attackResult.effect, attackResult.dam, 0, attackResult.buffEffect);
        }
        mainChar.downDurableWeapone();
    }

    private static long lucDoi;
    /** Đổi mục tiêu đánh sau mỗi ngần này ms. */
    private static final long DOI_MUC_TIEU = 100L;
    /** Con vừa bị đánh ở chế độ di chuyển (lần sau ưu tiên con khác). */
    private static Actor vuaDanh;

    /** Server chỉ nhận đòn khi lệch ngang và dọc đều < 130px (Map.MAX_RANGE_CHAR) - chừa lề. */
    private static final int O_DANH = 120;
    /** Tới gần mục tiêu cỡ này thì đứng lại (không cần sát hơn). */
    private static final int DUNG_LAI = 40;

    /**
     * Chế độ di chuyển: giữ phím đi tới mục tiêu, đồng thời tung chiêu (gửi thẳng lệnh, không
     * animation nên không khựng) vào con quái trong ô đánh của server - ưu tiên mục tiêu.
     */
    private static void vuaDiVuaDanh(GameScr gameScr, MainChar mainChar, long l) {
        Actor actor = AutoUp.quaiTrongO(gameScr, mainChar, l);
        if (actor != null && l >= lucDanh) {
            byte by = AutoUp.chonSkill(gameScr, mainChar, 0, l);
            if (by >= 0) {
                AutoUp.tungChieu(gameScr, mainChar, actor, by, l);
                lucDanh = l + 350L;
            }
        }
        int n = Util.distance(mainChar.x, mainChar.y, mucTieu.x, mucTieu.y);
        if (n > DUNG_LAI) {
            if (n <= 64) {
                AutoUp.diToi(mainChar, mucTieu.x, mucTieu.y, l, mucTieu);
            } else {
                AutoUp.diTheoDuong(mainChar, mucTieu.x / 16, mucTieu.y / 16, l, mucTieu);
            }
            trangThai = actor != null ? "vừa đi vừa đánh" : "đi tới quái";
        } else {
            AutoUp.nha();
            trangThai = "đang đánh";
        }
    }

    /** Quái sống trong ô đánh của server quanh nhân vật: mục tiêu nếu nó ở trong, không thì con gần nhất. */
    private static Actor quaiTrongO(GameScr gameScr, MainChar mainChar, long l) {
        // mỗi 0.1s chuyển sang con khác trong ô (con gần nhất không phải con vừa đánh)
        boolean bl = l - lucDoi >= DOI_MUC_TIEU;
        if (bl) {
            lucDoi = l;
        } else if (vuaDanh != null && Math.abs(vuaDanh.x - mainChar.x) < O_DANH && Math.abs(vuaDanh.y - mainChar.y) < O_DANH && AutoUp.quaiSong(gameScr, vuaDanh)) {
            return vuaDanh;
        }
        Actor actor = null;
        Actor actor3 = null;
        int n = Integer.MAX_VALUE;
        Vector vector = gameScr.actors;
        for (int i = 0; i < vector.size(); ++i) {
            Actor actor2 = (Actor)vector.elementAt(i);
            if (!AutoUp.quaiSong(gameScr, actor2) || Math.abs(actor2.x - mainChar.x) >= O_DANH || Math.abs(actor2.y - mainChar.y) >= O_DANH) continue;
            if (actor2 == vuaDanh) {
                actor3 = actor2;
                continue;
            }
            int n2 = Util.distance(mainChar.x, mainChar.y, actor2.x, actor2.y);
            if (n2 >= n) continue;
            n = n2;
            actor = actor2;
        }
        vuaDanh = actor != null ? actor : actor3;
        return vuaDanh;
    }

    /** Quái sống gần nhất KHÁC mục tiêu hiện tại, trong tầm xa nhất của skill. */
    private static Actor quaiKhac(GameScr gameScr, MainChar mainChar, int n, long l) {
        Actor actor = null;
        int n2 = Integer.MAX_VALUE;
        Vector vector = gameScr.actors;
        for (int i = 0; i < vector.size(); ++i) {
            Actor actor2 = (Actor)vector.elementAt(i);
            if (actor2 == mucTieu || !AutoUp.quaiSong(gameScr, actor2) || AutoUp.dangBoQua(actor2, l)) continue;
            int n3 = Util.distance(mainChar.x, mainChar.y, actor2.x, actor2.y);
            if (n3 > n + 10 || n3 >= n2) continue;
            n2 = n3;
            actor = actor2;
        }
        return actor;
    }

    /** Quái gần nhất: đứng im = trong tầm xa nhất của skill; di chuyển = bất kỳ đâu trên map. */
    private static Actor chonQuai(GameScr gameScr, MainChar mainChar, int n, long l) {
        Actor actor = null;
        int n2 = Integer.MAX_VALUE;
        Vector vector = gameScr.actors;
        for (int i = 0; i < vector.size(); ++i) {
            Actor actor2 = (Actor)vector.elementAt(i);
            if (!AutoUp.quaiSong(gameScr, actor2) || AutoUp.dangBoQua(actor2, l)) continue;
            int n3 = Util.distance(mainChar.x, mainChar.y, actor2.x, actor2.y);
            if (!diChuyen && n3 > n + 10) continue;
            if (n3 >= n2) continue;
            n2 = n3;
            actor = actor2;
        }
        return actor;
    }

    /** Skill đánh sẵn sàng (hồi chiêu xong, đủ MP, tới tầm); >= 2 quái quanh mục tiêu thì ưu tiên skill lan. */
    private static byte chonSkill(GameScr gameScr, MainChar mainChar, int n, long l) {
        int n2 = AutoUp.quaiQuanh(gameScr, mucTieu);
        byte by = -1;
        byte[] byArray = AutoUp.skillDanh(mainChar);
        for (int j = 0; j < byArray.length; ++j) {
            byte i = byArray[j];
            if (i < 0 || i >= Char.skillLevelLearnt.length || Char.skillLevelLearnt[i] <= 0) continue;
            if (l - mainChar.timeLastUseSkills[i] <= mainChar.coolDownSkill[i]) continue;
            if (SkillManager.getSkillMP(i, Char.skillLevelLearnt[i], mainChar.clazz) > mainChar.mp) continue;
            if (n > SkillManager.getSkillRange(i, mainChar.clazz)) continue;
            if (n2 >= 2 && SkillManager.isSkillAeo(mainChar.clazz, i)) {
                return i;
            }
            if (by >= 0) continue;
            by = i;
        }
        return by;
    }

    private static int quaiQuanh(GameScr gameScr, Actor actor) {
        int n = 0;
        Vector vector = gameScr.actors;
        for (int i = 0; i < vector.size(); ++i) {
            Actor actor2 = (Actor)vector.elementAt(i);
            if (!AutoUp.quaiSong(gameScr, actor2) || Util.distance(actor.x, actor.y, actor2.x, actor2.y) > 60) continue;
            ++n;
        }
        return n;
    }

    private static int tamXaNhat(MainChar mainChar) {
        int n = 30;
        byte[] byArray = AutoUp.skillDanh(mainChar);
        for (int j = 0; j < byArray.length; ++j) {
            byte i = byArray[j];
            if (i < 0 || i >= Char.skillLevelLearnt.length || Char.skillLevelLearnt[i] <= 0) continue;
            n = Math.max(n, SkillManager.getSkillRange(i, mainChar.clazz));
        }
        return n;
    }

    private static int tamGanNhat(MainChar mainChar) {
        int n = Integer.MAX_VALUE;
        byte[] byArray = AutoUp.skillDanh(mainChar);
        for (int j = 0; j < byArray.length; ++j) {
            byte i = byArray[j];
            if (i < 0 || i >= Char.skillLevelLearnt.length || Char.skillLevelLearnt[i] <= 0) continue;
            n = Math.min(n, SkillManager.getSkillRange(i, mainChar.clazz));
        }
        return n == Integer.MAX_VALUE ? 30 : n;
    }

    // ------------------------------------------------------------------ nhặt đồ

    /** Theo 3 ô tab Nhặt của màn Auto gốc (game gốc lưu ô đó nhưng không tự nhặt). true = đang lo nhặt. */
    private static boolean nhat(GameScr gameScr, MainChar mainChar, long l) {
        BangAuto bangAuto = Canvas.autoTab;
        if (bangAuto == null || !bangAuto.isAutoPotion && !bangAuto.isAutoItem && !bangAuto.isAutoGemItem) {
            return false;
        }
        if (monNhat != null && (!gameScr.actors.contains(monNhat) || ((Dropable)monNhat).isSendGet)) {
            monNhat = null;
        }
        if (monNhat == null) {
            monNhat = AutoUp.chonDo(gameScr, mainChar, bangAuto, l);
            lucKet = 0L;
        }
        if (monNhat == null) {
            return false;
        }
        // đang đánh dở và đồ còn xa thì đánh trước
        if (mucTieu != null && AutoUp.quaiSong(gameScr, mucTieu) && Util.distance(mainChar.x, mainChar.y, monNhat.x, monNhat.y) > 40) {
            return false;
        }
        if (Util.distance(mainChar.x, mainChar.y, monNhat.x, monNhat.y) <= 30) {
            AutoUp.nha();
            Dropable dropable = (Dropable)monNhat;
            mainChar.dir = (byte)Util.findDirection(mainChar, monNhat);
            GameService.gI().getDropableFromGround(dropable.catagory, dropable.ID);
            dropable.isSendGet = true;
            monNhat = null;
            trangThai = "nhặt đồ";
            return true;
        }
        AutoUp.diToi(mainChar, monNhat.x, monNhat.y, l, monNhat);
        trangThai = "đi nhặt";
        return true;
    }

    private static Actor chonDo(GameScr gameScr, MainChar mainChar, BangAuto bangAuto, long l) {
        Actor actor = null;
        int n = Integer.MAX_VALUE;
        Vector vector = gameScr.actors;
        for (int i = 0; i < vector.size(); ++i) {
            Actor actor2 = (Actor)vector.elementAt(i);
            if (!(actor2 instanceof Dropable) || !actor2.isItemCanGet() || ((Dropable)actor2).isSendGet || AutoUp.dangBoQua(actor2, l)) continue;
            int n2 = actor2.catagory;
            if (n2 == 3 ? !bangAuto.isAutoItem || mainChar.isFullInventory() : (n2 == 4 ? !bangAuto.isAutoPotion || ((Dropable)actor2).item_template_id == 0 && mainChar.charXu > 100000000L : !bangAuto.isAutoGemItem)) continue;
            int n3 = Util.distance(mainChar.x, mainChar.y, actor2.x, actor2.y);
            // đứng im: chỉ nhặt quanh điểm đứng; di chuyển: đồ trong 250px quanh mình
            if (diChuyen ? n3 > 250 : Util.distance(neoX, neoY, actor2.x, actor2.y) > 80) continue;
            if (n3 >= n) continue;
            n = n3;
            actor = actor2;
        }
        return actor;
    }

    // ------------------------------------------------------------------ đi lại

    /**
     * Giữ phím mũi tên ảo về phía (x, y) - game tự đi, trượt tường và gửi vị trí đúng nhịp server
     * (lướt bằng lệnh 4 dễ bị server kéo lại). Kẹt 1.5s thì bỏ mục tiêu đó 10s.
     */
    private static void diToi(MainChar mainChar, int n, int n2, long l, Actor actor) {
        if (AutoUp.dangNe(l)) {
            return;
        }
        int n3 = n - mainChar.x;
        int n4 = n2 - mainChar.y;
        int n5 = n3 < 0 ? 4 : 6;
        int n6 = n4 < 0 ? 2 : 8;
        int n7 = Math.abs(n3) >= Math.abs(n4) ? n5 : n6;
        int n8 = n7 == n5 ? n6 : n5;
        // nhìn trước một ô: hướng chính có tường thì đi hướng phụ; cả hai đều tường -> tìm đường
        if (AutoUp.tuongTruoc(mainChar, n7)) {
            if ((n7 == n5 ? n4 : n3) != 0 && !AutoUp.tuongTruoc(mainChar, n8)) {
                n7 = n8;
            } else {
                AutoUp.diTheoDuong(mainChar, n / 16, n2 / 16, l, actor);
                return;
            }
        }
        AutoUp.nha();
        Canvas.keyHold[n7] = true;
        giuPhim = true;
        if (AutoUp.ket(mainChar, l, n7)) {
            if (++lanKet >= 3) {
                lanKet = 0;
                AutoUp.nha();
                if (actor != null) {
                    boQua.put(actor, new Long(l + 10000L));
                    if (actor == mucTieu) {
                        mucTieu = null;
                    }
                    if (actor == monNhat) {
                        monNhat = null;
                    }
                }
            }
        }
    }

    private static int phimNe = -1;
    private static long lucNe;

    /** Đang bước tránh sau khi kẹt: giữ phím né tới hết giờ. */
    private static boolean dangNe(long l) {
        if (phimNe < 0) {
            return false;
        }
        if (l >= lucNe) {
            phimNe = -1;
            return false;
        }
        AutoUp.nha();
        Canvas.keyHold[phimNe] = true;
        giuPhim = true;
        return true;
    }

    /** Ô kế tiếp theo phím (2 lên, 8 xuống, 4 trái, 6 phải) là tường? */
    private static boolean tuongTruoc(MainChar mainChar, int n) {
        int n2 = mainChar.x / 16 + (n == 4 ? -1 : (n == 6 ? 1 : 0));
        int n3 = mainChar.y / 16 + (n == 2 ? -1 : (n == 8 ? 1 : 0));
        return PauseMenu.laTuong(n2, n3);
    }

    /**
     * Kẹt (0.6s không nhúc nhích dù đang giữ phím n): bước ngang sang ô trống bên cạnh 0.35s rồi đi
     * tiếp. true = vừa phát hiện kẹt.
     */
    private static boolean ket(MainChar mainChar, long l, int n) {
        if (lucKet == 0L || mainChar.x != ketX || mainChar.y != ketY) {
            lucKet = l;
            ketX = mainChar.x;
            ketY = mainChar.y;
            return false;
        }
        if (l - lucKet <= 600L) {
            return false;
        }
        lucKet = 0L;
        int[] nArray = n == 4 || n == 6 ? new int[]{2, 8} : new int[]{4, 6};
        if ((l & 1L) == 1L) {
            nArray = new int[]{nArray[1], nArray[0]};
        }
        for (int i = 0; i < nArray.length; ++i) {
            if (AutoUp.tuongTruoc(mainChar, nArray[i])) continue;
            phimNe = nArray[i];
            lucNe = l + 350L;
            break;
        }
        return true;
    }

    /**
     * Đi tới ô (tx, ty) theo đường BFS của PauseMenu.timDuong (vòng qua tường, tránh ô cổng chuyển
     * map), giữ phím ảo từng đoạn thẳng như PauseMenu.diBo. Quái chạy xa > 2 ô so với đích thì tính
     * lại đường. Kẹt thì tính lại; kẹt 3 lần hoặc không có đường thì bỏ mục tiêu đó 10s.
     * true = đã tới nơi.
     */
    private static boolean diTheoDuong(MainChar mainChar, int n, int n2, long l, Actor actor) {
        int n3 = Tilemap.w;
        if (n3 <= 0 || n < 0 || n2 < 0 || n >= n3 || n2 >= Tilemap.h) {
            return true;
        }
        int n4 = n2 * n3 + n;
        if (duongDich >= 0 && (Math.abs(duongDich % n3 - n) > 2 || Math.abs(duongDich / n3 - n2) > 2)) {
            duong = null;
        }
        if (duong == null) {
            boolean[] blArray = new boolean[n3 * Tilemap.h];
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    int n5 = n + j;
                    int n6 = n2 + i;
                    if (n5 < 0 || n6 < 0 || n5 >= n3 || n6 >= Tilemap.h || PauseMenu.laTuong(n5, n6)) continue;
                    blArray[n6 * n3 + n5] = true;
                }
            }
            duong = AutoUp.timDuongXa(blArray);
            duongPos = 0;
            duongDich = n4;
            if (duong == null) {
                AutoUp.nha();
                AutoUp.boMucTieu(actor, l);
                return false;
            }
            if (duong.length == 0) {
                duong = null;
                return true;
            }
        }
        if (AutoUp.dangNe(l)) {
            return false;
        }
        int n7 = duong[duongPos];
        int n8 = n7 % n3 * 16 + 8 - mainChar.x;
        int n9 = n7 / n3 * 16 + 8 - mainChar.y;
        int n10 = Math.max(3, (int)mainChar.speed);
        if (Math.abs(n8) <= n10 && Math.abs(n9) <= n10) {
            if (++duongPos >= duong.length) {
                duong = null;
                AutoUp.nha();
                return true;
            }
            return false;
        }
        AutoUp.nha();
        int n11 = Math.abs(n8) > n10 ? (n8 < 0 ? 4 : 6) : (n9 < 0 ? 2 : 8);
        Canvas.keyHold[n11] = true;
        giuPhim = true;
        if (AutoUp.ket(mainChar, l, n11)) {
            duong = null;
            if (++lanKet >= 3) {
                lanKet = 0;
                AutoUp.nha();
                AutoUp.boMucTieu(actor, l);
            }
        }
        return false;
    }

    /** Không thấy quái: đi tuần tới một ô đi được ngẫu nhiên, cách >= 10 ô, tới nơi thì chọn ô khác. */
    private static void diTuan(MainChar mainChar, long l) {
        int n = Tilemap.w;
        int n2 = Tilemap.h;
        if (n <= 0 || n2 <= 0) {
            return;
        }
        if (tuanTra < 0) {
            java.util.Random random = new java.util.Random(l);
            AutoUp.napKhoangCach();
            // điểm đi tuần: cách tường/địa hình >= 3 ô, cách cổng qua map >= 8 ô; không có thì nới dần
            int[][] nArray = new int[][]{{3, 8}, {2, 6}, {1, 4}, {1, 0}};
            for (int k = 0; k < nArray.length && tuanTra < 0; ++k) {
                for (int i = 0; i < 80 && tuanTra < 0; ++i) {
                    int n3 = (random.nextInt() & Integer.MAX_VALUE) % n;
                    int n4 = (random.nextInt() & Integer.MAX_VALUE) % n2;
                    int n6 = n4 * n + n3;
                    if (PauseMenu.laTuong(n3, n4) || Math.abs(n3 - mainChar.x / 16) + Math.abs(n4 - mainChar.y / 16) < 10) continue;
                    if (kcTuong[n6] < nArray[k][0] || kcCong[n6] < nArray[k][1]) continue;
                    tuanTra = n6;
                }
            }
            duong = null;
            duongDich = -1;
            lanKet = 0;
            if (tuanTra < 0) {
                AutoUp.nha();
                trangThai = "chờ quái";
                return;
            }
        }
        trangThai = "tìm quái";
        int n5 = tuanTra;
        if (AutoUp.diTheoDuong(mainChar, n5 % n, n5 / n, l, null) || duong == null && duongDich == n5 && lanKet == 0 && lucKet == 0L) {
            // tới nơi (hoặc ô đó không có đường): lần sau chọn ô khác
            tuanTra = -1;
        }
    }

    // ------------------------------------------------------------------ tránh tường / cổng

    /** Khoảng cách (ô, 4 hướng) tới tường/địa hình gần nhất và tới ô cổng gần nhất - tính một lần mỗi map. */
    private static int[] kcTuong;
    private static int[] kcCong;
    private static Object kcCua;

    private static void napKhoangCach() {
        int n = Tilemap.w;
        int n2 = Tilemap.h;
        if (kcTuong != null && kcCua == Tilemap.type && kcTuong.length == n * n2) {
            return;
        }
        kcCua = Tilemap.type;
        kcTuong = AutoUp.loang(n, n2, false);
        kcCong = AutoUp.loang(n, n2, true);
    }

    /** BFS nhiều nguồn: nguồn = ô cổng (cong) hoặc mọi ô không đi được. Ô không tới được = 99. */
    private static int[] loang(int n, int n2, boolean bl) {
        int[] nArray = new int[n * n2];
        int[] nArray2 = new int[n * n2];
        int n3 = 0;
        int n4 = 0;
        for (int i = 0; i < nArray.length; ++i) {
            int n5 = i < Tilemap.type.length ? Tilemap.type[i] : 0;
            boolean bl2 = bl ? n5 >= 2000000000 : PauseMenu.laTuong(i % n, i / n);
            if (bl2) {
                nArray[i] = 0;
                nArray2[n4++] = i;
            } else {
                nArray[i] = 99;
            }
        }
        int[] nArray3 = new int[]{1, -1, 0, 0};
        int[] nArray4 = new int[]{0, 0, 1, -1};
        while (n3 < n4) {
            int n6 = nArray2[n3++];
            int n7 = n6 % n;
            int n8 = n6 / n;
            if (nArray[n6] >= 98) continue;
            for (int j = 0; j < 4; ++j) {
                int n9 = n7 + nArray3[j];
                int n10 = n8 + nArray4[j];
                if (n9 < 0 || n10 < 0 || n9 >= n || n10 >= n2) continue;
                int n11 = n10 * n + n9;
                if (nArray[n11] <= nArray[n6] + 1) continue;
                nArray[n11] = nArray[n6] + 1;
                nArray2[n4++] = n11;
            }
        }
        return nArray;
    }

    /** Phạt khi đi qua ô: sát tường nặng, cách 2 ô nhẹ; gần cổng rất nặng (tránh lỡ qua map). */
    private static int phat(int n) {
        int n2 = 0;
        int n3 = kcTuong[n];
        if (n3 <= 1) {
            n2 += 8;
        } else if (n3 == 2) {
            n2 += 3;
        }
        int n4 = kcCong[n];
        if (n4 <= 3) {
            n2 += 40;
        } else if (n4 <= 6) {
            n2 += 15;
        }
        return n2;
    }

    /**
     * Tìm đường như PauseMenu.timDuong (cùng dạng kết quả: các ô góc rẽ, không gồm ô đứng) nhưng
     * Dijkstra có phạt: đi giữa đường, xa tường và cổng; chỗ hẹp bắt buộc thì vẫn đi qua.
     */
    private static int[] timDuongXa(boolean[] blArray) {
        AutoUp.napKhoangCach();
        int n = Tilemap.w;
        int n2 = Tilemap.h;
        MainChar mainChar = Canvas.gameScr.mainChar;
        int n3 = mainChar.x / 16;
        int n4 = mainChar.y / 16;
        if (n3 < 0 || n4 < 0 || n3 >= n || n4 >= n2) {
            return null;
        }
        int n5 = n * n2;
        int[] nArray = new int[n5];
        int[] nArray2 = new int[n5];
        for (int i = 0; i < n5; ++i) {
            nArray[i] = Integer.MAX_VALUE;
            nArray2[i] = -2;
        }
        // đống nhị phân (khoảng cách, ô)
        int[] nArray3 = new int[n5 * 4 + 4];
        int[] nArray4 = new int[n5 * 4 + 4];
        int n6 = 0;
        int n7 = n4 * n + n3;
        nArray[n7] = 0;
        nArray2[n7] = -1;
        nArray3[0] = 0;
        nArray4[0] = n7;
        n6 = 1;
        int n8 = -1;
        int[] nArray5 = new int[]{1, -1, 0, 0};
        int[] nArray6 = new int[]{0, 0, 1, -1};
        while (n6 > 0) {
            int n9 = nArray3[0];
            int n10 = nArray4[0];
            --n6;
            nArray3[0] = nArray3[n6];
            nArray4[0] = nArray4[n6];
            int n11 = 0;
            while (true) {
                int n12 = n11 * 2 + 1;
                if (n12 >= n6) break;
                if (n12 + 1 < n6 && nArray3[n12 + 1] < nArray3[n12]) {
                    ++n12;
                }
                if (nArray3[n11] <= nArray3[n12]) break;
                int n13 = nArray3[n11];
                nArray3[n11] = nArray3[n12];
                nArray3[n12] = n13;
                n13 = nArray4[n11];
                nArray4[n11] = nArray4[n12];
                nArray4[n12] = n13;
                n11 = n12;
            }
            if (n9 > nArray[n10]) continue;
            if (blArray[n10]) {
                n8 = n10;
                break;
            }
            int n14 = n10 % n;
            int n15 = n10 / n;
            for (int i = 0; i < 4; ++i) {
                int n16 = n14 + nArray5[i];
                int n17 = n15 + nArray6[i];
                if (PauseMenu.laTuong(n16, n17)) continue;
                int n18 = n17 * n + n16;
                int n19 = n9 + 1 + AutoUp.phat(n18);
                if (n19 >= nArray[n18] || n6 >= nArray3.length) continue;
                nArray[n18] = n19;
                nArray2[n18] = n10;
                int n20 = n6++;
                nArray3[n20] = n19;
                nArray4[n20] = n18;
                while (n20 > 0) {
                    int n21 = (n20 - 1) / 2;
                    if (nArray3[n21] <= nArray3[n20]) break;
                    int n22 = nArray3[n21];
                    nArray3[n21] = nArray3[n20];
                    nArray3[n20] = n22;
                    n22 = nArray4[n21];
                    nArray4[n21] = nArray4[n20];
                    nArray4[n20] = n22;
                    n20 = n21;
                }
            }
        }
        if (n8 < 0) {
            return null;
        }
        int n23 = 0;
        for (int i = n8; i != -1; i = nArray2[i]) {
            ++n23;
        }
        int[] nArray7 = new int[n23];
        for (int i = n8; i != -1; i = nArray2[i]) {
            nArray7[--n23] = i;
        }
        Vector vector = new Vector();
        for (int i = 1; i < nArray7.length; ++i) {
            if (i < nArray7.length - 1 && nArray7[i] - nArray7[i - 1] == nArray7[i + 1] - nArray7[i]) continue;
            vector.addElement(new Integer(nArray7[i]));
        }
        int[] nArray8 = new int[vector.size()];
        for (int i = 0; i < nArray8.length; ++i) {
            nArray8[i] = (Integer)vector.elementAt(i);
        }
        return nArray8;
    }

    private static void boMucTieu(Actor actor, long l) {
        duong = null;
        duongDich = -1;
        if (actor == null) {
            tuanTra = -1;
            return;
        }
        boQua.put(actor, new Long(l + 10000L));
        if (actor == mucTieu) {
            mucTieu = null;
        }
        if (actor == monNhat) {
            monNhat = null;
        }
    }

    private static boolean dangBoQua(Actor actor, long l) {
        Long l2 = (Long)boQua.get(actor);
        if (l2 == null) {
            return false;
        }
        if (l < l2.longValue()) {
            return true;
        }
        boQua.remove(actor);
        return false;
    }

    /** Nhả phím ảo - chỉ khi chính auto up đang giữ, không đụng phím người chơi. */
    private static void nha() {
        if (!giuPhim) {
            return;
        }
        giuPhim = false;
        Canvas.keyHold[2] = false;
        Canvas.keyHold[4] = false;
        Canvas.keyHold[6] = false;
        Canvas.keyHold[8] = false;
    }

    // ------------------------------------------------------------------ nhóm

    private static boolean trongNhom(String string) {
        Vector vector = Char.party;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            Object object = vector.elementAt(i);
            if (!(object instanceof PartyInfo) || !((PartyInfo)object).name.toLowerCase().equals(string.toLowerCase())) continue;
            return true;
        }
        return false;
    }

    /**
     * Mời lần lượt (1 người mỗi 3s) người trong danh sách hoặc tất cả người chơi trong map - server chỉ
     * cho mời người cùng map, nên người trong danh sách phải đang ở cùng map mới mời được.
     * Chưa có nhóm thì lập nhóm rồi game tự mời người đó (onCreadtePTOK).
     */
    private static void nhom(GameScr gameScr, MainChar mainChar, long l) {
        if (cheDoMoi == 0 || cheDoMoi == 1 && danhSach.size() == 0 || mainChar.IDParty != -1 && mainChar.IDMasterParty != mainChar.ID || Char.party != null && Char.party.size() >= 5) {
            return;
        }
        if (mainChar.IDParty == -1 && l < lucTaoNhom) {
            return;
        }
        Vector vector = gameScr.actors;
        for (int i = 0; i < vector.size(); ++i) {
            Actor actor = (Actor)vector.elementAt(i);
            if (actor.catagory != 0 || actor == mainChar || actor.ID == mainChar.ID || !(actor instanceof Char) || actor.isNPC() || actor.isNpcChar()) continue;
            Char char_ = (Char)actor;
            if (char_.name == null || char_.pk > 0 || char_.iskiller) continue;
            if (AutoUp.trongNhom(char_.name) || cheDoMoi == 1 && !AutoUp.trongDanhSach(char_.name)) continue;
            Long l2 = (Long)daMoi.get(char_.name);
            if (l2 != null && l - l2.longValue() < 60000L) continue;
            daMoi.put(char_.name, new Long(l));
            if (mainChar.IDParty == -1) {
                GameScr.tempIDActorParty = char_.ID;
                GameService.gI().requestCreateParty(mainChar.ID);
                lucTaoNhom = l + 10000L;
            } else {
                GameService.gI().invite2Party(char_.ID, 0);
            }
            return;
        }
    }

    /**
     * Chèn (qua AutoGuard) ở ĐẦU GameScr.onAdd2Party: đang auto up + "Nhận lời mời" bật thì tự vào
     * nhóm (giống nhánh tự vào nhóm của auto gốc); chế độ "theo danh sách" thì lời mời của người
     * ngoài danh sách bị bỏ qua, không bật hộp thoại chắn màn hình.
     * true = đã xử lý xong gói này.
     */
    static boolean moiNhom(Message message) {
        if (!dang || cheDoNhan == 0 || Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return false;
        }
        DataInputStream dataInputStream = message.reader();
        if (dataInputStream == null || !dataInputStream.markSupported()) {
            return false;
        }
        try {
            dataInputStream.mark(4096);
            if (dataInputStream.readByte() != 0) {
                dataInputStream.reset();
                return false;
            }
            short s = dataInputStream.readShort();
            short s2 = dataInputStream.readShort();
            String string = dataInputStream.readUTF();
            byte by = dataInputStream.readByte();
            dataInputStream.readShort();
            if (cheDoNhan == 1 && !AutoUp.trongDanhSach(string)) {
                // người lạ: bỏ qua lặng lẽ, không bật hộp thoại chắn màn hình lúc đang auto
                return true;
            }
            Message message2 = new Message(49);
            message2.writer().writeByte(1);
            message2.writer().writeShort(s2);
            GameService.gI().session.sendMessage(message2);
            message2.cleanup();
            MainChar mainChar = Canvas.gameScr.mainChar;
            mainChar.add2Party(new PartyInfo(s2, string, by, dataInputStream.readByte()));
            mainChar.IDMasterParty = s2;
            mainChar.IDParty = s;
            while (dataInputStream.available() > 0) {
                mainChar.add2Party(new PartyInfo(dataInputStream.readShort(), dataInputStream.readUTF(), dataInputStream.readByte(), dataInputStream.readByte()));
            }
            Canvas.gameScr.addChat(new ChatInfo("", "Đã tham gia nhóm", 0));
            return true;
        }
        catch (Throwable throwable) {
            return true;
        }
    }

    // ------------------------------------------------------------------ thanh trạng thái

    static void ve(Graphics graphics, int n) {
        if (!dang || Font.normalFont == null || Font.normalFont[0] == null) {
            return;
        }
        MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        StringBuilder stringBuilder = new StringBuilder().append(diChuyen ? "Up đi" : "Up đứng").append(": giết ").append(giet);
        if (mainChar != null) {
            stringBuilder.append(" | HP ").append(AutoUp.demBinh(mainChar, BINH_HP)).append(" MP ").append(AutoUp.demBinh(mainChar, BINH_MP));
        }
        stringBuilder.append(" | ").append(trangThai);
        String string = stringBuilder.toString();
        Font font = Font.normalFont[0];
        while (string.length() > 6 && font.getWidth(string) > Canvas.w - 12) {
            string = string.substring(0, string.length() - 1);
        }
        int n2 = Math.min(Canvas.w, font.getWidth(string) + 12);
        int n3 = font.getHeight() + 6;
        int n4 = (Canvas.w - n2) / 2;
        graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
        graphics.setClip(0, 0, Canvas.w, Canvas.h);
        graphics.setColor(0x0B2E1A);
        graphics.fillRect(n4, n, n2, n3);
        graphics.setColor(0x40C040);
        graphics.drawRect(n4, n, n2 - 1, n3 - 1);
        font.drawString(graphics, string, Canvas.w / 2, n + 3, 2);
    }

    static int caoThanh() {
        return Font.normalFont == null || Font.normalFont[0] == null ? 0 : Font.normalFont[0].getHeight() + 6;
    }

    private static int demBinh(MainChar mainChar, int[] nArray) {
        int n = 0;
        for (int i = 0; i < nArray.length; ++i) {
            if (nArray[i] >= mainChar.potions.length) continue;
            n += Math.max(0, mainChar.potions[nArray[i]]);
        }
        return n;
    }

    // ------------------------------------------------------------------ lưu cài đặt

    private static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            byte[] byArray = RMS.loadRMS(RMS_TEN);
            if (byArray == null) {
                return;
            }
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(byArray));
            if (dataInputStream.readByte() != 7) {
                return;
            }
            diChuyen = dataInputStream.readBoolean();
            cheDoMoi = Math.min(Math.max(0, dataInputStream.readByte()), TEN_CHE_DO_MOI.length - 1);
            dataInputStream.readBoolean(); // tắt hiệu ứng: nay luôn tắt (mục menu đã bỏ)
            cheDoNhan = Math.min(Math.max(0, dataInputStream.readByte()), TEN_CHE_DO_NHAN.length - 1);
            int n = dataInputStream.readByte();
            danhSach.removeAllElements();
            for (int i = 0; i < n; ++i) {
                danhSach.addElement(dataInputStream.readUTF());
            }
            dataInputStream.close();
        }
        catch (Throwable throwable) {
            // cài đặt hỏng: dùng mặc định
        }
    }

    private static void luu() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeByte(7);
            dataOutputStream.writeBoolean(diChuyen);
            dataOutputStream.writeByte(cheDoMoi);
            dataOutputStream.writeBoolean(tatHieuUng);
            dataOutputStream.writeByte(cheDoNhan);
            dataOutputStream.writeByte(danhSach.size());
            for (int i = 0; i < danhSach.size(); ++i) {
                dataOutputStream.writeUTF((String)danhSach.elementAt(i));
            }
            dataOutputStream.close();
            RMS.saveRMS(RMS_TEN, byteArrayOutputStream.toByteArray());
        }
        catch (Throwable throwable) {
            // không lưu được thì chỉ giữ trong phiên
        }
    }
}
