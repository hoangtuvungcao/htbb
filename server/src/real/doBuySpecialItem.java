/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package real;

import data.Database;
import io.Message;
import static real.Map.doBuyModelClothe;
import static real.Map.doCreateHuyetBoDe;
import static real.Map.doCreateHuyetLinhThao;
import static real.Map.getShopTemplate;
import static real.Map.isSale;
import static real.Map.isSellHuyetLinhThao;
import static real.Map.petTemplate;

/**
 *
 *
 */
public class doBuySpecialItem {

    /**
     * Mon hang nay o shop cong vao O NGOC nao? Tra ve -1 neu no khong phai ngoc.
     *
     * Bang tra cuu phai khop VOI CHINH cac nhanh mua ben duoi: shop id 116..125 goi
     * doAddGemItem voi mang {71,72,78,79,85,86,92,93,99,100}, con 176 la ban do kho bau.
     */
    private static int idNgocCuaShopItem(final int idShop) {
        if (idShop >= 116 && idShop <= 125) {
            final int[] idgem = {71, 72, 78, 79, 85, 86, 92, 93, 99, 100};
            return idgem[idShop - 116];
        }
        if (idShop == 176) {
            return GemTemplate.BAN_DO_KHO_BAU;
        }
        return -1;
    }

    /**
     * GD2 2.10 (sv.lau_dai): mon co data_shop_gia_lau_dai.tien_moi = 2 (ve EXP, thuoc...) chuyen
     * sang tra bang LUONG KHOA. Cac nhanh mua ben duoi deu tru tien qua Char.truLuongNeuDu, nen
     * thay vi sua tung nhanh, danh dau LUONG XU LY nay (ThreadLocal - lenh 85 chay tren luong phien,
     * cac lenh tieu luong khac tren luong ban do khong bi anh huong) de truLuongNeuDu tru luong
     * khoa. Truoc 18/10/2026 chanMuaShop chan han cac mon nay ("chi doi bang luong khoa") nhung
     * khong co cho nao doi bang luong khoa -> ve EXP / thuoc / keo khong mua duoc (BotChucNang
     * shop_luong bat duoc).
     */
    private static final ThreadLocal<Boolean> TRA_LUONG_KHOA = new ThreadLocal<Boolean>();

    /** Mon dang mua o lenh 85 nay (bao "Đã mua ...: trừ X lượng khoá" sau khi mua). */
    private static final ThreadLocal<ShopTemplate> MON_DANG_MUA = new ThreadLocal<ShopTemplate>();

    /** Dang xu ly lenh 85 cho mon tra bang luong khoa (xem TRA_LUONG_KHOA). */
    static boolean traLuongKhoa() {
        return Boolean.TRUE.equals(TRA_LUONG_KHOA.get());
    }

    public static void doBuySpecialItem(Char player, Message msg) {
        // Chong clone (19/10/2026): moi thu mua bang LUONG KHOA phai la do khoa. Chup thuoc + hanh trang truoc khi
        // mua; sau khi mua, neu luong khoa da giam thi binh thuoc moi tang = thuoc khoa (real/ThuocKhoa), vat pham
        // moi vao hanh trang = lock 1 (cho ky gui / giao dich da chan lock 1).
        final int[] thuocTruoc = player.potions == null ? null : player.potions.clone();
        final long luongKhoaTruoc = player.getLuongLock();
        final java.util.Set<Object> doTruoc = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Object, Boolean>());
        try {
            doTruoc.addAll(player.iItems);
        } catch (Exception ignored) {
        }
        try {
            doBuySpecialItem0(player, msg);
        } finally {
            TRA_LUONG_KHOA.remove();
            final ShopTemplate mon = MON_DANG_MUA.get();
            MON_DANG_MUA.remove();
            try {
                if (player.getLuongLock() < luongKhoaTruoc) {
                    // Bao ro loai tien (client cu ve gia bang bieu tuong luong): "Đã mua X: trừ 30 lượng khoá, còn 12."
                    player.sendMessage(MessageCreator.createServerAlertMessage(TienShopDacBiet.daMua(
                            mon == null ? null : mon.name, luongKhoaTruoc - player.getLuongLock(), player.getLuongLock()), ""));
                    ThuocKhoa.sauKhiMua(player, thuocTruoc, luongKhoaTruoc);
                    for (Object o : player.iItems.toArray()) {
                        if (o instanceof Item && !doTruoc.contains(o)) {
                            ((Item) o).lock = 1;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void doBuySpecialItem0(Char player, Message msg) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else {
            String nameItem = "";
            int soluong = 0;
            long sumPrice = 0L;
            String moneyType = "luong";
            if (!player.map.checkTrade(player)) {
                try {
                    int id = msg.dis.readUnsignedByte();
                    ShopTemplate item = getShopTemplate(id);
                    if (item == null) {
                        return;   // id shop khong ton tai (goi tin sai / client cu): bo qua, khong NPE ra log
                    }
                    MON_DANG_MUA.set(item);
                    if (player.isFullInventory()) {
                        try {
                            // Hanh trang day thi van phai cho mua nhung thu chi CONG DON
                            // vao mot o da co san. Ban cu kiem bang potions[item.id + 19]
                            // cho MOI mon, ma cong thuc do chi dung voi o THUOC: nguyen
                            // lieu che tao (shop id 116..125 va 176) di vao o NGOC qua
                            // doAddGemItem, nen potions[135..144] luon bang 0 va lenh mua
                            // luon bi tu choi ke ca khi nguoi choi da co san dung loai
                            // nguyen lieu do. Nguoi de auto mua nguyen lieu voi hanh trang
                            // day vi vay ket trong mot vong lap khong bao gio tien trien -
                            // moi don mua la mot goi tin (lenh 85 khong co truong so luong)
                            // nen no ban ra hang tram goi moi giay.
                            final int idNgoc = idNgocCuaShopItem(item.id);
                            final boolean daCoSanO = (idNgoc >= 0)
                                    ? (player.listGemitem[idNgoc] > 0 || player.listGemitemLock[idNgoc] > 0)
                                    : (item.id > 1 ? player.potions[item.id + 19] > 0
                                                   : player.hadSpecialItem(item.id));
                            if (!daCoSanO) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                                return;
                            }
                        } catch (Exception var17) {
                        }
                    }

                    if (item.sell == 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm hiện tại chưa bán.", ""));
                        return;
                    }

                    // GD2 2.10 (Q10): shop luong chi con do trang tri; kiem o CHINH noi xu ly (muc 11).
                    if (com.htbb.pokemon.LauDai.bat()) {
                        final String chan = data.DatabaseLauDai.chanMuaShop(player.charDBID, item.id, LauDaiNhanVat.tuanNay());
                        if (chan != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage(chan, ""));
                            return;
                        }
                        if (data.DatabaseLauDai.traBangLuongKhoa(item.id)) {
                            TRA_LUONG_KHOA.set(Boolean.TRUE);
                        }
                    }

                    if (item.id == 13 && player.idClan == -1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm chỉ dành cho người đã tham gia đội", ""));
                        return;
                    }

                    // Dong tra bang LUONG KHOA (tien_moi = 2 hoac do khoa 116..128/150/176): thieu luong khoa thi tu choi
                    // NGAY, ghi ro can / dang co - KHONG BAO GIO tru luong thuong thay (truLuongNeuDu chuyen sang
                    // truLuongKhoaNeuDu khi TRA_LUONG_KHOA; nhanh 132 kiem rieng ben duoi).
                    if ((traLuongKhoa() || player.map.isItemLock(item.id)) && player.getLuongLock() < (long) item.getPrice(isSale)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                TienShopDacBiet.khongDu(item.getPrice(isSale), player.getLuongLock()), ""));
                        return;
                    }

                    int oldLuong = player.getLuong();
                    int oldLuongLock = player.getLuongLock();
                    long oldXu = player.getxu();
                    sumPrice = (long) item.getPrice(isSale);
                    soluong = item.value;
                    nameItem = item.name;
                    int[] var10000;
                    int var10002;
                    Message m;

                    if (traLuongKhoa()) {
                        moneyType = "luong_lock";
                    }
                    if (player.map.isItemLock(item.id)) {
                        // Kiem-va-tru luong khoa trong MOT buoc (truLuongKhoaNeuDu) thay vi
                        // kiem roi tru rieng: nhanh nay luon di tiep xuong dung MOT nhanh con
                        // (126/127/128 hoac 116..125/150/176) va nhanh do luon co dung mot
                        // lan subLuongLock, nen gop check+tru vao day la an toan, khong doi
                        // hanh vi khi du tien.
                        if (!player.truLuongKhoaNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    TienShopDacBiet.khongDu(item.getPrice(isSale), player.getLuongLock()), ""));
                            return;
                        }

                        moneyType = "luong_lock";
                        if ((item.id < 116 || item.id > 125) && item.id != 150 && item.id != 176) {
                            if (item.id == 126) {
                                var10002 = player.potions[35]++;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                nameItem = item.name + "_khoa";
                            } else if (item.id == 127) {
                                var10000 = player.potions;
                                var10000[94] += item.value;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                nameItem = item.name + "_khoa";
                            } else if (item.id == 128) {
                                var10000 = player.potions;
                                var10000[96] += item.value;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                nameItem = item.name + "_khoa";
                            }
                        } else {
                            if (item.id == 176) {
                                player.doAddGemItem(GemTemplate.BAN_DO_KHO_BAU, item.value, true);
                                nameItem = item.name;
                            } else if (item.id == 150) {
                                var10000 = player.potions;
                                var10000[73] += item.value;
                                nameItem = item.name + "_khoa";
                            } else {
                                int[] idgem = new int[]{71, 72, 78, 79, 85, 86, 92, 93, 99, 100};
                                player.doAddGemItem(idgem[item.id - 116], item.value, true);
                                nameItem = item.name;
                            }

                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                        }
                    } else if (item.id == 172) {
                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        var10000 = player.potions;
                        var10000[77] += item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 166) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 736, 1, 0);
                        nameItem = "Ao Son Tinh_luong";
                    } else if (item.id == 167) {
                        moneyType = "xu";
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truXuNeuDu((long) item.getPrice(isSale), false, "map 65")) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 736, 1, 0);
                        nameItem = "Ao Son Tinh_xu";
                    } else if (item.id == 168) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 737, 1, 0);
                        nameItem = "Ao Thuy Tinh_luong";
                    } else if (item.id == 169) {
                        moneyType = "xu";
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truXuNeuDu((long) item.getPrice(isSale), false, "map 71")) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 737, 1, 0);
                        nameItem = "Ao Thuy Tinh_xu";
                    } else if (item.id == 170) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 740, Char.isSuKienBlackFriday() ? 60 : 1, 0);
                        nameItem = "Ao Tieu long nu";
                    } else if (item.id == 171) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 741, Char.isSuKienBlackFriday() ? 60 : 1, 0);
                        nameItem = "Ao Duong qua";
                    } else if (item.id == 174) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 744, 7, 0);
                        nameItem = "Ao tinh quan";
                    } else if (item.id == Map.SHOP_BONG_DA_NAM || item.id == Map.SHOP_BONG_DA_NU) {
                        // Thoi trang bong da - thu duy nhat nhan doi ti le roi nhanh hoa
                        // hong vang khi danh quai trong ky Vu Lan (xem Monster.java,
                        // nhanh isSuKienVuLan2015). Chi ban trong ky do:
                        // Map.doChangeInfoShopEvent dat co sell theo Char.isSuKienVuLan2015().
                        soluong = 1;
                        final boolean laNam = (item.id == Map.SHOP_BONG_DA_NAM);
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng.", ""));
                            return;
                        }

                        doBuyModelClothe(player, laNam ? 791 : 792, Map.SO_NGAY_BONG_DA, 0);
                        nameItem = laNam ? "Thoi trang bong da nam" : "Thoi trang bong da nu";
                    } else if (item.id == 175) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 743, 7, 0);
                        nameItem = "Ao tien nu";
                    } else if (item.id == 178) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 754, 7, 0);
                        nameItem = "Ao dai nam 2021";
                    } else if (item.id == 179) {
                        soluong = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        doBuyModelClothe(player, 755, 7, 0);
                        nameItem = "Ao dai nu 2021";
                    } else if (item.id >= 154 && item.id <= 164) {
                        // Kiem gioi tinh TRUOC khi dung tien: neu de kiem tien len truoc va
                        // gop tien vao buoc atomiv o day thi mua sai gioi tinh se bi tru tien
                        // oan (kiem qua roi moi tru tien that, khong the hoan lai trong
                        // Runnable). Doi cho khong doi y nghia, chi doi thu tu bao loi.
                        if (player.gender == 1) {
                            if (item.id < 161) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Kiểu lông chỉ dùng cho nữ", ""));
                                return;
                            }
                        } else if (player.gender == 2 && item.id >= 161) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Kiểu lông chỉ dùng cho nam", ""));
                            return;
                        }

                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        soluong = 1;
                        int oldhead = player.headStyle;
                        player.headStyle = (byte) item.value;
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        nameItem = item.name;
                        Database.instance.saveOrtherLog("", player.charname, "doi kieu toc tu  " + oldhead + " qua " + player.headStyle, "doitoc");
                    } else if (item.id == 165) {
                        if (!isSellHuyetLinhThao()) {
                            return;
                        }

                        // Khong doi: nhanh nay chua bao gio thuc su subXu (doCreateHuyetLinhThao
                        // khong tru tien), nen khong co cap kiem-roi-tru can lam atomic o day.
                        if (player.getxu() < (long) item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        if (player.hlt_buy <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua thêm.", ""));
                            return;
                        }

                        soluong = 1;
                        nameItem = item.name;
                        --player.hlt_buy;
                        doCreateHuyetLinhThao(player, 0);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        Database.instance.saveOrtherLog("", player.charname, "mua hlt,sl con lai co the mua: " + player.hlt_buy, "bhlt");
                    } else if (item.id == 129) {
                        moneyType = "xu";
                        if (!player.truXuNeuDu((long) item.getPrice(isSale), true, "map 66")) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        var10000 = player.potions;
                        var10000[97] += item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 130) {
                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        var10000 = player.potions;
                        var10000[116] += item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 131) {
                        moneyType = "xu";
                        if (player.buy_luot_van_tieu <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Một ngày chỉ có thể mua 2 thẻ giao hàng.", ""));
                            return;
                        }

                        if (!player.truXuNeuDu((long) item.getPrice(isSale), true, "map 67")) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        --player.buy_luot_van_tieu;
                        var10000 = player.potions;
                        var10000[82] += item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 135) {
                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = nameItem + item.name;
                        var10000 = player.potions;
                        var10000[142] += item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 177) {
                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = nameItem + item.name;
                        var10000 = player.potions;
                        var10000[105] += item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 148) {
                        // 0.9.14: ngoai su kien (Trung Thu 2016 / Halloween 2016 / He 2017 / Tet 2017 / World Cup 2017)
                        // nhanh nay TRU LUONG ma khong giao gi - tu choi TRUOC khi tru.
                        if (!(Char.isSuKienTrungThul2016() || Char.isSuKienHaloween2016() || Char.isSuKienHe2017()
                                || Char.isSuKienTet2017() || Char.isSuKienWordcup2017())) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm này chỉ bán trong sự kiện. Không bị trừ lượng.", ""));
                            return;
                        }
                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        if (Char.isSuKienTrungThul2016()) {
                            var10000 = player.potions;
                            var10000[135] += item.value;
                        } else if (Char.isSuKienHaloween2016()) {
                            var10000 = player.potions;
                            var10000[139] += item.value;
                        } else if (Char.isSuKienHe2017()) {

                            var10000 = player.potions;
                            var10000[155] += item.value;
                        } else if (Char.isSuKienTet2017()) {

                            var10000 = player.potions;
                            var10000[155] += item.value;
                        }else if (Char.isSuKienWordcup2017()) {
                            var10000 = player.potions;
                            var10000[161] += item.value;
                        }

                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 149) {
                        // 0.9.14: ngoai su kien He 2017 / World Cup 2017 nhanh nay tru xu ma khong giao gi.
                        if (!(Char.isSuKienHe2017() || Char.isSuKienWordcup2017())) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm này chỉ bán trong sự kiện. Không bị trừ xu.", ""));
                            return;
                        }
                        if (!player.truXuNeuDu((long) item.getPrice(isSale), true, "shop cá nhân")) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        if (Char.isSuKienHe2017()) {
                            var10000 = player.potions;
                            var10000[155] += item.value;
                        }else if (Char.isSuKienWordcup2017()) {
                            var10000 = player.potions;
                            var10000[161] += item.value;
                        }
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else {
                        int minute;
                        int value;
                        int hour;
                        if (item.id == 151) {
                            moneyType = "xu";
                            // isFullInventory dua len TRUOC buoc tru tien atomic: giu nguyen
                            // dieu kien/thong bao, chi doi thu tu de khong tru tien roi moi
                            // phat hien het cho (khong the hoan lai trong truXuNeuDu).
                            if (player.isFullInventory()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                                return;
                            }

                            if (!player.truXuNeuDu((long) item.getPrice(isSale), false, "map 73")) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            nameItem = item.name;
                            soluong = 1;
                            hour = 24 - UtilHTBB.getHour();
                            minute = UtilHTBB.getMinute();
                            value = hour * 60 - minute;
                            player.map.doBuyGayGiangSinh(player, value);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else if (item.id == 173) {
                            if (player.isFullInventory()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                                return;
                            }
                            if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:562")) {
                                return; // o thu day: kiem TRUOC khi tru tien / vat pham
                            }

                            if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            soluong = 1;
                            value = 10080; // thời gian
                            if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:565")) {
                                return; // o thu day: khong nhan, khong tru tien sau do
                            }
                            player.createLan((long) value, true, 95);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            nameItem = item.name;
                        } else if (item.id == 152) {
                            if (player.isFullInventory()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                                return;
                            }
                            if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:580")) {
                                return; // o thu day: kiem TRUOC khi tru tien / vat pham
                            }

                            if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            soluong = 1;
                            hour = 24 - UtilHTBB.getHour();
                            minute = UtilHTBB.getMinute();
                            value = hour * 60 - minute;
                            if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:586")) {
                                return; // o thu day: khong nhan, khong tru tien sau do
                            }
                            player.createLan((long) value, true, 0);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            nameItem = item.name;
                        } else {
                            if (item.id == 153) {
                                if (Char.is123Tet()) {
                                }

                                player.sendMessage(MessageCreator.createServerAlertMessage("Rương may mắn chỉ mở bán vào mùng 1,2,3 tết.", ""));
                                return;
                            }

                            if (item.id == 136) {
                                moneyType = "xu";
                                if (!player.truXuNeuDu((long) item.getPrice(isSale), true, "map 68")) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                    return;
                                }

                                nameItem = item.name;
                                var10000 = player.potions;
                                var10000[77] += item.value;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (item.id == 146) {
                                if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                    return;
                                }

                                nameItem = item.name + "_luong";
                                var10000 = player.potions;
                                var10000[104] += item.value;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (item.id == 147) {
                                moneyType = "xu";
                                if (!player.truXuNeuDu((long) item.getPrice(isSale), true, "map 69")) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                    return;
                                }

                                nameItem = item.name + "_xu";
                                if (Char.isSuKienHe2017()) {
                                    var10000 = player.potions;
                                    var10000[104] += item.value;
                                } else if (Char.isSuKienHaloween2016()) {
                                    var10000 = player.potions;
                                    var10000[139] += item.value;
                                }

                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (item.id == 141) {
                                if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:638")) {
                                    return; // o thu day: kiem TRUOC khi tru tien / vat pham
                                }
                                if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                    return;
                                }

                                soluong = 1;
                                nameItem = item.name;
                                if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:647")) {
                                    return; // o thu day: khong nhan, khong tru tien sau do
                                }
                                player.createPet(21, petTemplate[21].timeExpire);
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (item.id == 132) {
                                // Khong doi: subLuong o day chi chay khi doCreateHuyetBoDe(...)
                                // tra ve true (con cho trong hanh trang). Neu gop check+tru
                                // len truoc bang truLuongNeuDu thi luc het cho se tru tien ma
                                // khong co do, va khong the hoan lai trong nhanh nay ma khong
                                // bay them logic hoan tien moi (ngoai pham vi doi mang tinh
                                // co hoc cua dot sua nay).
                                if (traLuongKhoa()) {
                                    // dong nay tung chuyen sang luong khoa: KHONG kiem / tru luong thuong
                                    // tru truoc; het cho trong hanh trang thi hoan lai dung so luong khoa vua tru
                                    if (!player.truLuongKhoaNeuDu((long) item.getPrice(isSale))) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                                TienShopDacBiet.khongDu(item.getPrice(isSale), player.getLuongLock()), ""));
                                        return;
                                    }
                                    if (!doCreateHuyetBoDe(player, 1)) {
                                        player.addLuongLock(item.getPrice(isSale));
                                        return;
                                    }
                                    nameItem = item.name;
                                    soluong = 1;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                } else if (player.getLuong() < item.getPrice(isSale)) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                    return;
                                } else if (doCreateHuyetBoDe(player, 1)) {
                                    nameItem = item.name;
                                    soluong = 1;
                                    player.subLuong((long) item.getPrice(isSale));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else {
                                ShopTemplate spItem;
                                if (item.id > 1) {
                                    label838:
                                    {
                                        if (item.id != 50 && item.id != 51 && item.id != 52 && item.id != 54 && item.id != 55 && item.id != 53 && item.id != 56 && item.id != 57 && item.id != 58 && item.id != 86 && item.id != 87 && item.id != 88 && item.id != 89 && item.id != 90 && item.id != 91 && item.id != 113 && item.id != 114 && item.id != 142 && item.id != 143 && item.id != 144 && item.id != 145 && item.id != 106 && item.id != 105 && item.id != 137 && item.id != 138 && item.id != 139 && item.id != 140) {
                                            if (item.id != 92 && item.id != 107 && item.id != 110 && item.id != 111 && item.id != 133) {
                                                int var10001;
                                                if (item.id >= 83 && item.id <= 85) {
                                                    // Kiem tra va cong don PHAI cung mot o potions[] - ban cu kiem o
                                                    // item.id-93+112 (=id+19) nhung lai cong don o item.id-83+112
                                                    // (=id+29), lech 10 chi so, nen gioi han "toi da 5" khong bao
                                                    // gio chan duoc so luong that su da mua.
                                                    if (player.potions[item.id - 83 + 112] + item.value > 5) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua thêm.", ""));
                                                        return;
                                                    }

                                                    if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                        return;
                                                    }

                                                    nameItem = item.name;
                                                    var10000 = player.potions;
                                                    var10001 = item.id - 83 + 112;
                                                    var10000[var10001] += item.value;
                                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                    Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong());
                                                    break label838;
                                                }

                                                if (item.id == 108) {
                                                    if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                        return;
                                                    }

                                                    nameItem = item.name;
                                                    var10000 = player.potions;
                                                    var10000[13] += item.value;
                                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                    Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong());
                                                } else if (item.id == 134) {
                                                    if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                        return;
                                                    }

                                                    nameItem = item.name;
                                                    var10000 = player.potions;
                                                    var10000[77] += item.value;
                                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                    Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong());
                                                } else if (item.id == 109) {
                                                    if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                        return;
                                                    }

                                                    nameItem = item.name;
                                                    var10000 = player.potions;
                                                    var10000[8] += item.value;
                                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                    Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong());
                                                } else if (item.id == 112) {
                                                    if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                        return;
                                                    }

                                                    soluong = 1;
                                                    nameItem = item.name;
                                                    player.genItemPhiPhong(608, true, 0, 43200000);
                                                    Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong());
                                                } else if (item.id == 115) {
                                                    if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                        return;
                                                    }

                                                    soluong = 1;
                                                    nameItem = item.name;
                                                    player.genItemPhiPhong(608, true, 1, 43200000);
                                                    Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong());
                                                } else if (player.potions[item.id + 19] + item.value <= 800) {
                                                    nameItem = item.name;
                                                    if (item.type == 0) {
                                                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                            return;
                                                        }
                                                    } else if (item.type == 1) {
                                                        moneyType = "xu";
                                                        if (!player.truXuNeuDu((long) item.getPrice(isSale), true, "map 74")) {
                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu.", ""));
                                                            return;
                                                        }
                                                    }

                                                    var10000 = player.potions;
                                                    var10001 = item.id + 19;
                                                    var10000[var10001] += item.value;
                                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                } else {
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mua tối đa 800 bình.", ""));
                                                }
                                                break label838;
                                            }
                                            if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:776")) {
                                                return; // o thu day: kiem TRUOC khi tru tien / vat pham
                                            }

                                            if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                return;
                                            }

                                            soluong = 1;
                                            spItem = null;
                                            if (item.id == 92) {
                                                if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:788")) {
                                                    return; // o thu day: khong nhan, khong tru tien sau do
                                                }
                                                player.createPet(3, petTemplate[3].timeExpire);
                                                nameItem = item.name;
                                            } else if (item.id == 107) {
                                                if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:791")) {
                                                    return; // o thu day: khong nhan, khong tru tien sau do
                                                }
                                                player.createPet(9, 10080);
                                                nameItem = item.name;
                                            } else if (item.id == 110) {
                                                if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:794")) {
                                                    return; // o thu day: khong nhan, khong tru tien sau do
                                                }
                                                player.createPet(10, 10080);
                                                nameItem = item.name;
                                            } else if (item.id == 111) {
                                                if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:797")) {
                                                    return; // o thu day: khong nhan, khong tru tien sau do
                                                }
                                                player.createPet(11, 10080);
                                                nameItem = item.name;
                                            } else if (item.id == 133) {
                                                if (QuanLyThuGame.chanNhan(player, "doBuySpecialItem:800")) {
                                                    return; // o thu day: khong nhan, khong tru tien sau do
                                                }
                                                player.createPet(13, 10080);
                                                nameItem = item.name;
                                            }

                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã mua", ""));
                                            Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong());
                                            Database.instance.saveLogThongKeChiTieu(nameItem, 0, soluong, sumPrice, moneyType);
                                            return;
                                        }

                                        if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                            return;
                                        }

                                        soluong = 1;
                                        nameItem = item.name;
                                        if ((item.id == 139 || item.id == 140) && Char.isSuKienBlackFriday()) {
                                            doBuyModelClothe(player, item, 60);
                                        } else if (item.id != 90 && item.id != 91) {
                                            doBuyModelClothe(player, item, item.id != 88 && item.id != 86 ? 7 : 15);
                                        } else {
                                            doBuyModelClothe(player, item, 10);
                                        }

                                        m = new Message(85);
                                        player.sendMessage(m);
                                        Database.instance.saveLogThongKeChiTieu(nameItem, 0, soluong, sumPrice, moneyType);
                                        return;
                                    }
                                } else {
                                    // listSpItem/getAllSP dua len TRUOC buoc tru tien atomic
                                    // (cung ly do voi nhanh 151/173/152 o tren): tranh tru tien
                                    // roi moi phat hien da mua toi da / het ve, vi truLuongNeuDu
                                    // khong the hoan lai sau khi da tru.
                                    if (player.listSpItem[item.id] >= 3) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua thêm.", ""));
                                        return;
                                    }

                                    if (player.getAllSP() >= 126) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại bạn chỉ có thể mua tối đa 126 vé", ""));
                                        return;
                                    }

                                    if (!player.truLuongNeuDu((long) item.getPrice(isSale))) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                        return;
                                    }

                                    nameItem = item.name;
                                    if (player.listSpItem[item.id] == 0) {
                                        spItem = new ShopTemplate();
                                        spItem.coppy(spItem, item);
                                        spItem.ownerId = player.charDBID;
                                        spItem.realId = player.getIDItem();
                                        player.specialItem.add(spItem);
                                    }

                                    var10002 = player.listSpItem[item.id]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            }
                        }
                    }

                    Database.instance.saveLogThongKeChiTieu(nameItem, 0, soluong, sumPrice, moneyType);
                    Database.instance.saveLogBuyItemShop(player.charname, item.name, item.type, item.getPrice(isSale), "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong() + "_lk_" + oldLuongLock + "_" + player.getLuongLock());
                    player.sendMessage(MessageCreator.createCharSpecialItem(player));
                    m = new Message(85);
                    player.sendMessage(m);
                    m.cleanup();
                } catch (Exception var18) {
                    var18.printStackTrace();
                }

            }
        }
    }
}
