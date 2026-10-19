package real;

import data.CharInfo;
import data.DatabaseSuKienWorldCup;
import data.ItemLuckyDraw;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


/**
 * SU KIEN WORLD CUP - binh chon Messi / Ronaldo.
 *
 * Train quai nhat Qua bong vang; mang 100 bong vang + 5000 luong den NPC Tong Quan
 * de bo mot phieu cho Messi hoac Ronaldo, moi lan bo phieu duoc mot Hop qua may man.
 * Het han binh chon thi ben nhieu phieu hon thang, ai da bo phieu cho ben do vao nhan
 * qua, Top 1 cua ben thang nhan them phi phong max chi so.
 *
 * NGUON: lay tu ban goc khac (real/EventQueGoTre.java - ten lop ben do khong lien
 * quan gi den World Cup). Nhung cho DA SUA cho hop voi ban nay, ghi ro de sau nay doi
 * chieu:
 *
 * 1. VAT PHAM. Ban goc them thuoc id 162 lam "Hop qua may man". Ban nay khong the:
 *    data_potion dang co dung 162 dong (id 0..161) va Char.SO_O_THUOC = 162 phai khop
 *    voi so dong do - client cap phat mang listPotion theo con so server gui trong goi
 *    dang nhap, gui id vuot tran la NGUOI CHOI THAY HANH TRANG RONG (xem chu thich o
 *    Char.getAllPotion). Nen dung lai dong 108, von la mot dong TRONG san co:
 *        161 = Qua bong vang     (da co san)
 *        108 = Hop qua may man   (dong trong, tools/luu_tru_sinh_du_lieu/sql_nguon/them_su_kien_world_cup.sql dat ten)
 *        143 = Hop qua bi mat    (da co san)
 *    Vi the KHONG port applyPotionMeta() cua ban goc: ban do doc ten/icon thuoc tu
 *    mang LoginHandler.PORTION_NAME, ban nay doc tu BANG data_potion
 *    (LoginHandler.createMsgLogin gui Map.potionTemplates2) - nen ban migrate SQL
 *    chinh la thu thay cho ham do.
 *
 * 2. THOI GIAN lay tu server.ini chu khong nen cung trong ma nguon, va MAC DINH TAT:
 *        sv.worldCupBatDau     (de trong = tat han su kien)
 *        sv.worldCupKetThuc
 *        sv.worldCupHanNhanQua
 *    Ban goc de cung "2026-06-01".."2026-12-31" trong ma nguon, nghia la vua cap nhat
 *    la su kien chay ngay va quai bat dau roi bong vang - khong ai kip chuan bi.
 *
 * 3. QUA TOP 1 dung ItemLuckyDraw.createItemCoat(p, mavat, true, 0) - phi phong max
 *    chi so vinh vien, cung duong tao voi phi phong trong hop qua. Ban goc goi
 *    createMaxItemCoatLucky(616, ...) tuc ep tempateID = 616 "Phi phong long thu";
 *    duong do tra cuu itemTemplates.get(charClass).get(616) va nuot ngoai le neu
 *    khong thay, tao ra vat pham thieu type/durable.
 *
 * 4. HANG trong bang xep hang dem bang COUNT thay vi bien nguoi dung MySQL (@r:=@r+1)
 *    - xem chu thich o DatabaseSuKienWorldCup.hangCuaNhanVat.
 *
 * MENU: muc "Su kien World Cup" duoc THEM VAO CUOI menu NPC Tong Quan (-10), vi tri
 * that su ghi vao Char.idxSuKienWorldCup - giong cach muc "Nhan moc nap" dang lam.
 * Ban goc chen vao GIUA menu (getTongQuanEventRootIndex), lam lech het cac muc phia sau.
 */
public final class SuKienWorldCup {

    // ============================ VAT PHAM ============================

    /** Nguyen lieu binh chon, roi ra tu quai. */
    public static final int ITEM_BONG_VANG = Potion.TYPE_QUA_BONG_VANG;   // 161
    /** Phan thuong moi lan binh chon. Dong data_potion trong, xem chu thich dau lop. */
    public static final int ITEM_HOP_MAY_MAN = 108;
    /** Qua kem khi nhan thuong cuoi su kien. */
    public static final int ITEM_HOP_BI_MAT = Potion.TYPE_HOP_QUA_BI_MAT; // 143

    // ============================ GIA ============================

    private static final int GIA_BONG_VANG = 100;
    private static final long GIA_LUONG = 5000L;
    private static final int THUONG_LUONG_BEN_THANG = 10000;
    private static final int THUONG_LUONG_TOP1 = 50000;

    // ============================ TI LE ROI ============================

    /** Quai thuong: 2%..3% (tren mot trieu). */
    private static final int TILE_QUAI_MIN = 20000;
    private static final int TILE_QUAI_MAX = 30000;
    /** Boss: luon roi, so luong 20..50. */
    private static final int BOSS_SL_MIN = 20;
    private static final int BOSS_SL_MAX = 50;
    /** Chenh lech cap giua nguoi choi va quai, hon la khong roi. */
    private static final int LECH_CAP_TOI_DA = 5;

    // ============================ MENU ============================

    /** Ma trang menu con cua su kien. Khong duoc trung voi idMenu nao NPC -10 dang dung. */
    public static final int MENU_GOC = 101;
    /** Trang "bang xep hang" - bam vao mot dong de xem chi tiet. */
    public static final int MENU_BXH = 100;

    // ============================ THOI GIAN ============================

    /**
     * Cua so thoi gian, dat tu server.ini luc khoi dong va moi lan admin nap lai
     * (server.CauHinh). De trong = su kien TAT.
     *
     * Dinh dang "yyyy-MM-dd HH:mm:ss" de so sanh thang bang String voi
     * Char.getDayTime(0) - dung mot dinh dang thi so sanh chuoi = so sanh thoi gian.
     */
    public static volatile String batDau = "";
    public static volatile String ketThuc = "";
    public static volatile String hanNhanQua = "";

    /**
     * Bam lan mot xem thong tin, bam lan hai trong 10 giay moi mo hop.
     * Khoa theo charDBID chu khong theo doi tuong Char: nguoi choi thoat ra vao lai
     * la doi tuong khac, con khoa nay chi de chong bam nham nen song qua phien la duoc.
     */
    private static final long HAN_XAC_NHAN_MS = 10000L;
    private static final ConcurrentHashMap<Integer, Long> CHO_XAC_NHAN =
            new ConcurrentHashMap<Integer, Long>();

    private SuKienWorldCup() {
    }

    // ============================ TRANG THAI ============================

    /**
     * Dang trong thoi gian binh chon (quai roi bong vang, doi phieu duoc).
     *
     * Cong bat/tat o trang quan tri (SuKien) la thu QUYET DINH; cua so thoi gian
     * sv.worldCup* trong server.ini chi con la GIA TRI MAC DINH cua cong do, dung khi
     * chua ai dat tay trong bang tob_su_kien.
     */
    public static boolean dangDienRa() {
        return SuKien.bat(SuKien.WORLD_CUP);
    }

    /** Da het binh chon nhung con han nhan qua. */
    public static boolean dangHanNhanQua() {
        // Con dang binh chon thi chua toi luc phat qua - hai giai doan khong chong nhau.
        return !dangDienRa() && SuKien.bat(SuKien.WORLD_CUP_NHAN_QUA);
    }

    /** Cua so binh chon trong server.ini co bao trum hien tai khong. */
    static boolean trongCuaSoBinhChon() {
        if (batDau.isEmpty() || ketThuc.isEmpty()) {
            return false;
        }
        final String bayGio = Char.getDayTime(0L);
        return bayGio.compareTo(batDau) >= 0 && bayGio.compareTo(ketThuc) <= 0;
    }

    /** Cua so nhan qua trong server.ini co bao trum hien tai khong. */
    static boolean trongCuaSoNhanQua() {
        if (ketThuc.isEmpty() || hanNhanQua.isEmpty()) {
            return false;
        }
        final String bayGio = Char.getDayTime(0L);
        return bayGio.compareTo(ketThuc) > 0 && bayGio.compareTo(hanNhanQua) <= 0;
    }

    /** Co hien muc su kien trong menu NPC Tong Quan khong. */
    public static boolean hienTrongMenu() {
        return dangDienRa() || dangHanNhanQua();
    }

    // ============================ MENU ============================

    public static String[] menuGoc() {
        return new String[]{
            "Bình chọn Messi",
            "Bình chọn Ronaldo",
            "Bảng xếp hạng Messi/Ronaldo",
            "Hướng dẫn",
            "Nhận quà World Cup"
        };
    }

    /** Dieu huong khi nguoi choi bam mot dong trong menu su kien. */
    public static void doMenu(final Char player, final int idNpc, final int idMenu,
            final int idOptionMenu) throws IOException {
        if (idMenu == MENU_BXH) {
            if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createServerAlertMessage(dongTop1(true), ""));
            } else if (idOptionMenu == 3) {
                player.sendMessage(MessageCreator.createServerAlertMessage(dongTop1(false), ""));
            }
            return;
        }
        switch (idOptionMenu) {
            case 0:
                doiPhieu(player, true);
                return;
            case 1:
                doiPhieu(player, false);
                return;
            case 2:
                xemBangXepHang(player, idNpc);
                return;
            case 3:
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Train quái để nhặt Quả bóng vàng. Mỗi lượt bình chọn tốn "
                        + GIA_BONG_VANG + " bóng vàng + " + GIA_LUONG
                        + " lượng và nhận 1 Hộp quà may mắn.", ""));
                return;
            case 4:
                nhanQua(player);
                return;
            default:
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Tùy chọn không hợp lệ, vui lòng mở lại menu World Cup.", ""));
        }
    }

    private static void doiPhieu(final Char player, final boolean chonMessi) throws IOException {
        if (!dangDienRa()) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Đã kết thúc bình chọn World Cup.", ""));
            return;
        }
        if (player.isFullInventory()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
            return;
        }
        if (player.getLuong() < GIA_LUONG) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Không đủ " + GIA_LUONG + " lượng.", ""));
            return;
        }
        if (player.potions[ITEM_BONG_VANG] < GIA_BONG_VANG) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Không đủ " + GIA_BONG_VANG + " Quả bóng vàng.", ""));
            return;
        }

        player.subLuong(GIA_LUONG);
        player.potions[ITEM_BONG_VANG] -= GIA_BONG_VANG;
        player.potions[ITEM_HOP_MAY_MAN]++;
        DatabaseSuKienWorldCup.themPhieu(player.charname, chonMessi ? 1 : 2, 1);

        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createMsgChat(player.id, "Bình chọn "
                + (chonMessi ? "Messi" : "Ronaldo") + " thành công, nhận được 1 Hộp quà may mắn."));
    }

    private static void xemBangXepHang(final Char player, final int idNpc) {
        final int[] tong = DatabaseSuKienWorldCup.tongPhieu();
        final String[] menu = {
            "Messi: " + tong[0] + " phiếu",
            "Ronaldo: " + tong[1] + " phiếu",
            dongTop1(true),
            dongTop1(false)
        };
        player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, MENU_BXH));
    }

    private static String dongTop1(final boolean laMessi) {
        final Vector<CharInfo> top = DatabaseSuKienWorldCup.bangXepHang(laMessi);
        final String ten = laMessi ? "Messi" : "Ronaldo";
        if (top == null || top.isEmpty()) {
            return "Top chọn cho " + ten + ": chưa có";
        }
        final CharInfo c = top.get(0);
        return "Top chọn cho " + ten + ": Top1 " + c.name + " (" + c.honor + " phiếu)";
    }

    // ============================ QUAI ROI BONG VANG ============================

    /**
     * Goi tu Monster khi quai chet. Da kiem dangDienRa() o noi goi, nhung kiem lai
     * o day cho chac vi ham nay tao vat pham.
     */
    public static void quaiRoiBongVang(final Map map, final Char p, final Monster mt,
            final Vector<Actor> droplist) {
        if (!dangDienRa()) {
            return;
        }

        int soLuong = 1;
        if (mt.isBoss) {
            soLuong = BOSS_SL_MIN + Map.r.nextInt(BOSS_SL_MAX - BOSS_SL_MIN + 1);
        } else {
            final int tile = TILE_QUAI_MIN + Map.r.nextInt(TILE_QUAI_MAX - TILE_QUAI_MIN + 1);
            if (Map.randomMillion() >= tile) {
                return;
            }
            if (Map.abs(p.lvDetail.lv - mt.level) > LECH_CAP_TOI_DA) {
                return;
            }
        }

        final Potion pt = new Potion((short) ITEM_BONG_VANG, soLuong, map);
        pt.id = map.getIDITEM();
        pt.x = mt.x - 4;
        pt.y = mt.y - 4;
        pt.belongUser = p.charDBID;
        map.addPotion(pt, mt.inCountry);
        pt.time_drop = System.currentTimeMillis();
        droplist.add(pt);
        if (p.autoGetItem == 1) {
            p.idPotion.add(pt.id);
        }
    }

    // ============================ DUNG VAT PHAM ============================

    public static boolean laBongVang(final int idThuoc) {
        return idThuoc == ITEM_BONG_VANG;
    }

    public static boolean laHopMayMan(final int idThuoc) {
        return idThuoc == ITEM_HOP_MAY_MAN;
    }

    /** Bam vao Qua bong vang trong hanh trang: chi hien huong dan, khong tieu vat pham. */
    public static void xemBongVang(final Char player) {
        final String info = "Quả bóng vàng: nguyên liệu bình chọn World Cup. Mang đến NPC Tổng"
                + " quản để bình chọn (" + GIA_BONG_VANG + " bóng vàng + " + GIA_LUONG + " lượng).";
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        player.sendMessage(MessageCreator.createMsgChat(player.id, info));
    }

    /**
     * Bam lan mot: hien thong tin va KHONG mo. Bam lan hai trong 10 giay: mo.
     *
     * @return true neu day la lan bam xac nhan (nguoi goi mo hop)
     */
    public static boolean xacNhanMoHop(final Char player) {
        final long bayGio = System.currentTimeMillis();
        final Integer khoa = Integer.valueOf(player.charDBID);
        final Long luocTruoc = CHO_XAC_NHAN.get(khoa);
        if (luocTruoc == null || bayGio - luocTruoc.longValue() > HAN_XAC_NHAN_MS) {
            CHO_XAC_NHAN.put(khoa, Long.valueOf(bayGio));
            final int con = (ITEM_HOP_MAY_MAN < player.potions.length)
                    ? player.potions[ITEM_HOP_MAY_MAN] : 0;
            final String info = "Hộp quà may mắn World Cup: có thể ra ruy băng, nguyên liệu,"
                    + " xu/lượng hoặc pet. Số lượng: " + con + ". Bấm lần nữa trong 10 giây để mở.";
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            return false;
        }
        CHO_XAC_NHAN.remove(khoa);
        return true;
    }

    /**
     * Mo mot Hop qua may man. Vat pham DA BI TRU truoc khi vao day (doUsePotion tru
     * ngay dau ham), nen loi giua chung phai hoan lai.
     */
    public static void moHopMayMan(final Char player) {
        if (!hienTrongMenu()) {
            hoanLaiHop(player, "Sự kiện chưa mở hoặc đã kết thúc.");
            return;
        }
        if (player.isFullInventory()) {
            hoanLaiHop(player, "Hành trang đầy.");
            return;
        }

        try {
            final int quay = Map.r.nextInt(100);
            if (quay < 20) {                 // 20% phi phong 3-5 ngay
                thuongPhiPhong(player, new int[]{4320, 5760, 7200});
            } else if (quay < 52) {          // 32% da nang cap
                thuongNguyenLieu(player);
            } else if (quay < 72) {          // 20% xu / luong
                thuongTienTe(player);
            } else if (quay < 90) {          // 18% da hiem
                thuongDaHiem(player);
            } else {                         // 10% jackpot
                thuongJackpot(player);
            }
        } catch (final Throwable t) {
            hoanLaiHop(player, "Mở Hộp quà may mắn thất bại, hộp quà đã được hoàn lại.");
        }
    }

    private static void hoanLaiHop(final Char player, final String loi) {
        player.potions[ITEM_HOP_MAY_MAN]++;
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(loi, ""));
    }

    private static void thuongPhiPhong(final Char player, final int[] soPhut) {
        final Item vp = ItemLuckyDraw.createItemCoat(player, Map.r.nextInt(2), false,
                soPhut[Map.r.nextInt(soPhut.length)]);
        if (vp == null) {
            throw new IllegalStateException("khong tao duoc phi phong");
        }
        player.iItems.add(vp);
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        player.sendMessage(MessageCreator.createServerAlertMessage("Mở Hộp quà may mắn nhận được "
                + vp.getName() + " thời hạn " + vp.minuteExist + " phút.", ""));
        player.sendMessage(MessageCreator.createMsgChat(player.id, "Bạn nhận được "
                + vp.getName() + " thời hạn " + vp.minuteExist + " phút."));
    }

    private static void thuongNguyenLieu(final Char player) {
        final int[] ids = {GemTemplate.BOT_XANH, GemTemplate.BOT_XANH_LA, GemTemplate.DA_TIEN_GIAI};
        final int idGem = ids[Map.r.nextInt(ids.length)];
        final int soLuong = 5 + Map.r.nextInt(16); // 5..20
        player.doAddGemItem(idGem, soLuong, true);
        player.sendMessage(MessageCreator.createCharGemItem(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Mở Hộp quà may mắn nhận được "
                + soLuong + " " + Map.gemTemplate[idGem].name + " khóa.", ""));
    }

    private static void thuongTienTe(final Char player) throws IOException {
        if (Map.r.nextBoolean()) {
            final int xu = 200000 + Map.r.nextInt(9800001); // 200k..10tr
            player.addXu(xu, "worldcup_box");
            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Mở Hộp quà may mắn nhận được " + xu + " xu.", ""));
        } else {
            final int luong = 500 + Map.r.nextInt(1501); // 500..2000
            player.addLuong(luong);
            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Mở Hộp quà may mắn nhận được " + luong + " lượng.", ""));
        }
    }

    private static void thuongDaHiem(final Char player) {
        final int[] ids = {GemTemplate.LKD_35, GemTemplate.LKD_40, GemTemplate.DA_MAY_MAN_C4};
        final int idGem = ids[Map.r.nextInt(ids.length)];
        final int soLuong = 1 + Map.r.nextInt(3); // 1..3
        player.doAddGemItem(idGem, soLuong, true);
        player.sendMessage(MessageCreator.createCharGemItem(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Mở Hộp quà may mắn nhận được "
                + soLuong + " " + Map.gemTemplate[idGem].name + " khóa.", ""));
    }

    private static void thuongJackpot(final Char player) {
        if (Map.r.nextInt(100) < 65) {
            thuongPhiPhong(player, new int[]{10080, 11520, 14400}); // 7-10 ngay
            return;
        }
        final int[] idPet = {9, 10, 11, 13}; // Armadillo bang / set / doc, Chim canh cut
        player.createPet(idPet[Map.r.nextInt(idPet.length)], 10080); // 7 ngay
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "JACKPOT: Bạn nhận được pet thời hạn 7 ngày.", ""));
    }

    // ============================ NHAN QUA CUOI SU KIEN ============================

    private static void nhanQua(final Char player) throws IOException {
        if (!dangHanNhanQua()) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Chưa đến thời gian nhận quà World Cup hoặc đã hết hạn nhận.", ""));
            return;
        }

        final int benThang = DatabaseSuKienWorldCup.benThang();
        if (benThang == 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Kết quả World Cup đang hoà phiếu, chưa thể nhận quà.", ""));
            return;
        }

        final boolean messiThang = (benThang == 1);
        final int[] phieuCuaToi = DatabaseSuKienWorldCup.phieuCuaNhanVat(player.charname);
        if ((messiThang ? phieuCuaToi[0] : phieuCuaToi[1]) <= 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Bạn không thuộc bên thắng cuộc nên không thể nhận quà.", ""));
            return;
        }

        final boolean[] daNhan = DatabaseSuKienWorldCup.daNhanQua(player.charname);
        boolean nhanDuoc = false;

        // ---- Qua thuong cho moi nguoi ben thang ----
        if (!daNhan[0] && DatabaseSuKienWorldCup.danhDauNhanQua(player.charname, false)) {
            player.addLuong(THUONG_LUONG_BEN_THANG);
            player.potions[ITEM_HOP_BI_MAT]++;
            nhanDuoc = true;
        }

        // ---- Qua rieng cho Top 1 ----
        if (!daNhan[1]
                && DatabaseSuKienWorldCup.hangCuaNhanVat(player.charname, messiThang) == 1) {
            if (player.isFullInventory()) {
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Hành trang đầy, cần trống để nhận quà Top 1 World Cup.", ""));
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                return;
            }
            // Tao vat pham TRUOC roi moi danh dau: danh dau xong ma tao that bai la mat qua.
            final Item quaTop = ItemLuckyDraw.createItemCoat(player, Map.r.nextInt(2), true, 0);
            if (quaTop != null && DatabaseSuKienWorldCup.danhDauNhanQua(player.charname, true)) {
                player.iItems.add(quaTop);
                player.addLuong(THUONG_LUONG_TOP1);
                player.potions[ITEM_HOP_BI_MAT]++;
                nhanDuoc = true;
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Top 1 bên thắng: nhận"
                        + " thêm " + THUONG_LUONG_TOP1 + " lượng + " + quaTop.getName()
                        + " max chỉ số + 1 Hộp quà bí mật."));
            }
        }

        if (!nhanDuoc) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Bạn đã nhận hết quà World Cup.", ""));
            return;
        }

        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Nhận quà World Cup thành công.", ""));
    }

    // ============================ CAU HINH ============================

    /**
     * Dat cua so thoi gian. Goi tu TeamServer luc khoi dong va tu CauHinh khi admin
     * nap lai server.ini. Thieu mot khoa nao la TAT ca su kien - tha tat con hon chay
     * voi moc thoi gian nua voi.
     */
    public static void datThoiGian(final String bd, final String kt, final String han) {
        final String a = (bd == null) ? "" : bd.trim();
        final String b = (kt == null) ? "" : kt.trim();
        final String c = (han == null) ? "" : han.trim();
        if (a.isEmpty() || b.isEmpty() || c.isEmpty() || a.compareTo(b) >= 0 || b.compareTo(c) > 0) {
            batDau = "";
            ketThuc = "";
            hanNhanQua = "";
            return;
        }
        batDau = a;
        ketThuc = b;
        hanNhanQua = c;
    }

    /** Mot dong mo ta trang thai, in ra luc khoi dong. */
    public static String moTa() {
        final String giaiDoan = dangDienRa() ? "DANG BINH CHON"
                : (dangHanNhanQua() ? "DANG PHAT QUA" : "TAT");
        if (batDau.isEmpty()) {
            return giaiDoan + " (sv.worldCupBatDau de trong, dat tay o trang quan tri)";
        }
        return giaiDoan + " (cua so mac dinh " + batDau + " -> " + ketThuc
                + ", han nhan qua " + hanNhanQua + ")";
    }

    /** Chi dung cho kiem thu - xoa trang thai cho xac nhan mo hop. */
    static void xoaChoXacNhan() {
        CHO_XAC_NHAN.clear();
    }
}
