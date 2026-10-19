package real;


import data.Database;
import data.Pet;
import io.Message;
import java.util.Vector;

/**
 * Giao dich giua hai nguoi choi, doi vat pham, doi nhan cuoi, dung vat pham dac biet.
 *
 * Tach nguyen van tu real/Map.java. Deu la method PRIVATE cua Map nen khong
 * the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 *
 * LUU Y: doBuySpecialItem(Map,Char,Message) (lenh 85) da bi XOA khoi day 15/09/2026 -
 * la ban sao chet, trung ten voi real/doBuySpecialItem.java (CLASS rieng, ban DANG
 * SONG duoc RealController.privateCmd goi thang, khong bao gio roi toi switch cua
 * Map/MapXuLyLenh). Xem git history neu can doi chieu lai ban cu.
 */
import static real.Map.getTown;
import static real.Map.isModelClothes;
import static real.Map.isWearingAnimal;
import static real.Map.pause;

class MapMuaBan {

    // ------------------------------------------------------------------ GIAO DICH THUOC (goi 66)
    //
    // Toan bo than ham chay duoi Char.KHOA_GIAO_DICH. Goi 66 KHONG phai lenh rieng
    // (RealController.privateCmd) nen no chay tren luong cua ban do nguoi gui; hai ben o hai ban
    // do (hai khu) khac nhau la hai luong cung sua mot bo trang thai chung - khong khoa thi hai
    // lan "case 5" chay song song va thuoc duoc cong hai lan. Ngoai khoa, moi buoc con doi hai
    // ben dung CUNG ban do / nuoc / khu va gan nhau; luc hoan tat kiem lai toan bo bang
    // real/GiaoDichThuoc (so thuoc con du khong, hai ben co khop nhau khong, thuoc co duoc phep
    // giao dich khong). Cac duong huy (Map.checkTrade, doi map, chet, thoat) deu di qua
    // Char.huyGiaoDich de xoa SACH ca tPotions/rPotions - ban cu de sot hai mang nay.

    /** Loi moi giao dich chi co gia tri trong khoang nay. */
    static final long HAN_LOI_MOI_MS = 60000L;

    /** Doi phuong hop le: co trong userTrade cua minh va ho cung dang tro ve minh. */
    private static Char doiPhuongHopLe(Char player) {
        Char dp = player.doiPhuongGiaoDich();
        if (dp == null || dp == player) {
            return null;
        }
        return (dp.doiPhuongGiaoDich() == player) ? dp : null;
    }

    /** Hai ben phai con song, dung cung ban do, cung nuoc, cung khu va gan nhau. */
    private static boolean cungChoDung(Char a, Char b) {
        try {
            return a.map != null && a.map == b.map && a.inCountry == b.inCountry && a.region == b.region
                    && a.hp > 0 && b.hp > 0 && Map.inRangeActor(a, b, 640);
        } catch (Exception e) {
            return false;
        }
    }

    /** Huy giao dich, dong cua so ca hai ben (66/3) va bao mot dong. */
    private static void huyVaBao(Char player, String lyDo) {
        try {
            Char dp = player.doiPhuongGiaoDich();
            player.huyGiaoDich(true, true);
            player.sendMessage(MessageCreator.createServerAlertMessage(lyDo, ""));
            if (dp != null && dp != player) {
                dp.sendMessage(MessageCreator.createServerAlertMessage(lyDo, ""));
            }
        } catch (Exception e) {
        }
    }

    /**
     * Luat thuoc lay tu bang data_potion (isTrade) va danh sach thuoc khong gioi han. Dung chung
     * cho buoc bo thuoc vao, buoc hoan tat va cho gian hang Phu ong (OfflineMap.doBuyItem).
     */
    static final GiaoDichThuoc.LuatThuoc LUAT_THUOC = new GiaoDichThuoc.LuatThuoc() {
        @Override
        public boolean giaoDichDuoc(int idThuoc) {
            try {
                if (idThuoc <= 0 || idThuoc >= Map.potionTemplates2.size()) {
                    return false;
                }
                PotionTemplate2 pt = Map.potionTemplates2.get(idThuoc);
                return pt != null && pt.isTrade;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean khongGioiHan(int idThuoc) {
            // Dung danh sach ma buoc bo thuoc vao (case 2) va luc nhat (Map.doGetPotion) dang mien tran 999.
            return idThuoc == 85 || idThuoc == 79 || idThuoc == 82 || idThuoc == 80 || idThuoc == 81 || idThuoc == 88
                    || Map.isPotionUnlimit((short) idThuoc);
        }
    };

    static void doTrade(Map map, Char player, Message msg, int from) {
        if (pause) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì.", ""));
            return;
        }
        synchronized (Char.KHOA_GIAO_DICH) {
            doTradeDuoiKhoa(map, player, msg, from);
        }
    }

    private static void doTradeDuoiKhoa(Map map, Char player, Message msg, int from) {
        Message m = null;

        try {
            try {
                if (player.isKiller) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không thể sử dụng chức năng này khi đang ở chế độ PK", ""));
                    return;
                }

                short userId = 0;
                int cmd = msg.dis.readByte();
                // thanh vien: moi / nhan loi giao dich can Thanh vien HTBB (tat he thong = khong chan).
                if ((cmd == 0 || cmd == 1) && com.htbb.pokemon.ThanhVien.chanVaBao(player, "giao_dich")) {
                    return;
                }
                Char p = null;
                Char dp = null;
                switch (cmd) {
                    case -1:
                        // Tu choi loi moi. Chi dong den ai KHONG dang giao dich voi nguoi thu ba.
                        userId = msg.dis.readShort();
                        p = player.getCharFromNearChar(userId);
                        if (p != null) {
                            if (p.doiPhuongGiaoDich() == null || p.doiPhuongGiaoDich() == player) {
                                p.datLaiTrangThaiGiaoDich();
                            }
                            if (player.doiPhuongGiaoDich() == null || player.doiPhuongGiaoDich() == p) {
                                player.datLaiTrangThaiGiaoDich();
                            }
                            m = new Message(66);
                            m.dos.writeByte(-1);
                            m.dos.writeShort(player.id);
                            p.sendMessage(m);
                            return;
                        }

                        return;
                    case 0:
                        if (map.checkTrade(player)) {
                            return;
                        }

                        userId = msg.dis.readShort();
                        p = player.getCharFromNearChar(userId);
                        if (p == null || p == player) {
                            return;
                        }

                        if ((getTown[player.myCountry] || getTown[p.myCountry]) && player.mapID == 201) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời trong thời gian chiếm Gym", ""));
                            return;
                        }

                        if (System.currentTimeMillis() - player.timeInviteTrade < 60000L) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời quá 2 lần trong 1 phút", ""));
                            return;
                        }

                        if (!p.rcvInviteVip) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                            return;
                        }
                        // thanh vien: nguoi duoc moi cung phai la thanh vien (chan clone chuyen do cho nhau).
                        if (com.htbb.pokemon.ThanhVien.chanHaiNguoi(player, p, "giao_dich", "nhan_tang")) {
                            return;
                        }

                        if (p.isKiller) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đề nghị khi người chơi đang ở chế độ PK", ""));
                            return;
                        }

                        if (from == 1 && p.idWedding > -1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Người bạn muốn mời đã đồng ý tham dự 1 tiệc cưới khác.", ""));
                            return;
                        }

                        player.timeInviteTrade = System.currentTimeMillis();
                        // Ghi lai ai moi ai: buoc nhan (case 1) chi chap nhan khi co loi moi that.
                        p.idNguoiMoiGiaoDich = player.id;
                        p.timeDuocMoiGiaoDich = player.timeInviteTrade;
                        m = new Message(66);
                        m.dos.writeByte(0);
                        m.dos.writeShort(player.id);
                        p.sendMessage(m);
                        return;
                    case 1:
                        userId = msg.dis.readShort();
                        p = player.getCharFromNearChar(userId);
                        if (p == null || p == player) {
                            return;
                        }

                        if (player.userTrade.size() > 0 || p.userTrade.size() > 0) {
                            return;
                        }

                        {
                            // Truoc day ai gui goi 66/1 voi id mot nguoi dung gan la mo duoc giao dich
                            // voi ho, khong can loi moi. Nay phai co loi moi con han tu mot trong hai phia.
                            long bayGio = System.currentTimeMillis();
                            boolean coLoiMoi = player.idNguoiMoiGiaoDich == userId || p.idNguoiMoiGiaoDich == player.id;
                            boolean duocMoi = (player.idNguoiMoiGiaoDich == userId && bayGio - player.timeDuocMoiGiaoDich <= HAN_LOI_MOI_MS)
                                    || (p.idNguoiMoiGiaoDich == player.id && bayGio - p.timeDuocMoiGiaoDich <= HAN_LOI_MOI_MS);
                            if (!duocMoi) {
                                // Chi ghi nghi van khi KHONG HE co loi moi (goi 66/1 tu che). Loi moi co
                                // nhung het han la nguoi choi bam cham - khong phai gian lan, im lang bo qua.
                                if (!coLoiMoi) {
                                    data.NghiVanGianLan.ghiVatPham(player, "hackgd",
                                            "mo giao dich voi " + p.charname + " ma khong co loi moi");
                                }
                                return;
                            }
                        }

                        if (!cungChoDung(player, p)) {
                            return;
                        }

                        // Xoa sach trang thai cu cua CA HAI truoc khi bat dau - phong tPotions/rPotions
                        // con sot tu mot lan giao dich truoc bi huy ngang.
                        player.datLaiTrangThaiGiaoDich();
                        p.datLaiTrangThaiGiaoDich();
                        p.userTrade.add(player);
                        player.userTrade.add(p);
                        m = new Message(66);
                        m.dos.writeByte(1);
                        m.dos.writeShort(player.id);
                        p.sendMessage(m);
                        player.sendMessage(m);
                        return;
                    case 2:
                        if (!player.isCorrectPass && !player.passWord.equals("")) {
                            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                            return;
                        }

                        if (player.userTrade.size() == 0) {
                            return;
                        }

                        dp = doiPhuongHopLe(player);
                        if (dp == null) {
                            huyVaBao(player, "Giao dịch không hợp lệ, đã huỷ.");
                            return;
                        }

                        if (!cungChoDung(player, dp)) {
                            huyVaBao(player, "Hai bên phải đứng gần nhau trên cùng bản đồ khi giao dịch.");
                            return;
                        }

                        if (player.finishTrade) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể bỏ thêm vật phẩm vào.", ""));
                            return;
                        }

                        int type = msg.dis.readByte();
                        if (type == 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trao đổi vật phẩm này.", ""));
                            return;
                        }

                        if (type != 1) {
                            if (type != 2) {
                                return;
                            }

                            if (player.finishTrade) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể lấy vật phẩm ra.", ""));
                                return;
                            }

                            int idpotion = msg.dis.readByte();
                            if (idpotion < 0 || idpotion >= player.tPotions.length) {
                                return;
                            }
                            if (player.tPotions[idpotion] <= 0) {
                                m = MessageCreator.createServerAlertMessage("Không thể lấy ra vật phẩm này", "");
                                player.sendMessage(m);
                                m.cleanup();
                                return;
                            }

                            player.tPotions[idpotion] = 0;
                            dp.rPotions[idpotion] = 0;
                            m = new Message(66);
                            m.dos.writeByte(2);
                            m.dos.writeByte(2);
                            m.dos.writeShort(player.id);
                            m.dos.writeByte(idpotion);
                            player.sendMessage(m);
                            dp.sendMessage(m);
                            return;
                        }

                        if (!player.isCorrectPass && !player.passWord.equals("")) {
                            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                            return;
                        }

                        int idPotion = msg.dis.readByte();
                        int soluong = msg.dis.readShort();
                        if (soluong <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể giao dịch với số lượng này", ""));
                            return;
                        }

                        if (idPotion <= 0 || idPotion >= player.potions.length) {
                            return;
                        }

                        if (!LUAT_THUOC.giaoDichDuoc(idPotion)) {
                            return;
                        }
                        if (dp.isFullInventory() && dp.potions[idPotion] <= 0) {
                            m = MessageCreator.createServerAlertMessage("Hành trang người nhận đã đầy.", "");
                            player.sendMessage(m);
                            m.cleanup();
                            return;
                        }

                        if (player.potions[idPotion] < soluong) {
                            m = MessageCreator.createServerAlertMessage("Chỉ có thể trao đổi " + player.potions[idPotion] + " vật phẩm", "");
                            player.sendMessage(m);
                            m.cleanup();
                            return;
                        }

                        if (player.potions[idPotion] < player.tPotions[idPotion] + soluong) {
                            m = MessageCreator.createServerAlertMessage(
                                    "Chỉ có thể trao đổi thêm " + (player.potions[idPotion] - player.tPotions[idPotion]) + " vật phẩm", ""
                            );
                            player.sendMessage(m);
                            m.cleanup();
                            return;
                        }

                        if (dp.potions[idPotion] + dp.rPotions[idPotion] - player.rPotions[idPotion] + soluong <= GiaoDichThuoc.TOI_DA_MOT_O
                                || LUAT_THUOC.khongGioiHan(idPotion)) {
                            player.tPotions[idPotion] = player.tPotions[idPotion] + soluong;
                            dp.rPotions[idPotion] += soluong;
                            m = new Message(66);
                            m.dos.writeByte(2);
                            m.dos.writeByte(1);
                            m.dos.writeShort(player.id);
                            m.dos.writeByte(idPotion);
                            m.dos.writeShort(soluong);
                            player.sendMessage(m);
                            dp.sendMessage(m);
                            return;
                        }

                        m = MessageCreator.createServerAlertMessage(
                                "Chỉ có thể trao đổi thêm "
                                + (GiaoDichThuoc.TOI_DA_MOT_O
                                - dp.potions[idPotion]
                                - dp.rPotions[idPotion]
                                + player.rPotions[idPotion])
                                + " vật phẩm",
                                ""
                        );
                        player.sendMessage(m);
                        m.cleanup();
                        return;
                    case 3:
                        // Nguoi choi tu huy: xoa sach ca hai ben, doi phuong nhan 66/3.
                        player.huyGiaoDich(false, true);
                        return;
                    case 4:
                        dp = doiPhuongHopLe(player);
                        if (dp == null) {
                            if (player.userTrade.size() > 0) {
                                huyVaBao(player, "Giao dịch không hợp lệ, đã huỷ.");
                            }
                            return;
                        }

                        if (!cungChoDung(player, dp)) {
                            huyVaBao(player, "Hai bên phải đứng gần nhau trên cùng bản đồ khi giao dịch.");
                            return;
                        }

                        if (!player.finishTrade) {
                            player.finishTrade = true;
                            if (dp.finishTrade) {
                                m = new Message(66);
                                m.dos.writeByte(5);
                                player.sendMessage(m);
                                dp.sendMessage(m);
                                return;
                            }
                        }

                        return;
                    case 5:
                        dp = doiPhuongHopLe(player);
                        if (dp == null) {
                            if (player.userTrade.size() > 0) {
                                huyVaBao(player, "Giao dịch không hợp lệ, đã huỷ.");
                            }
                            return;
                        }

                        if (!cungChoDung(player, dp)) {
                            huyVaBao(player, "Hai bên phải đứng gần nhau trên cùng bản đồ khi giao dịch.");
                            return;
                        }

                        if (!player.finishTrade) {
                            return;
                        }

                        player.lockTrade = true;
                        if (dp.finishTrade && dp.lockTrade) {
                            // Kiem lai TOAN BO ngay truoc khi cong/tru: so thuoc con du khong, hai ben co
                            // khop nhau khong, thuoc co duoc phep giao dich khong, ben nhan co vuot 999 khong.
                            int loi = GiaoDichThuoc.kiemTra(player.potions, player.tPotions, player.rPotions,
                                    dp.potions, dp.tPotions, dp.rPotions, LUAT_THUOC);
                            if (loi != GiaoDichThuoc.HOP_LE) {
                                String chiTiet = GiaoDichThuoc.moTaLoi(loi) + " - voi " + dp.charname + " | "
                                        + GiaoDichThuoc.tomTat(player.tPotions, player.rPotions, dp.tPotions, dp.rPotions);
                                data.NghiVanGianLan.ghiVatPham(player, "hackgd", chiTiet);
                                try {
                                    Database.instance.saveOrtherLog("tob_log_other_item", player.charname, chiTiet, "hackgd");
                                } catch (Exception e) {
                                }
                                huyVaBao(player, "Giao dịch không hợp lệ (" + GiaoDichThuoc.moTaLoi(loi) + "), đã huỷ.");
                                return;
                            }

                            if (!player.doAddItemTrad2Inventory()) {
                                // Khe giua kiemTra va luc tru: mot luong khac vua tieu thuoc. Khong
                                // cong gi cho ai, huy va ghi lai.
                                String chiTiet = "so thuoc doi ngay truoc luc tru - voi " + dp.charname + " | "
                                        + GiaoDichThuoc.tomTat(player.tPotions, player.rPotions, dp.tPotions, dp.rPotions);
                                data.NghiVanGianLan.ghiVatPham(player, "hackgd", chiTiet);
                                huyVaBao(player, "Giao dịch không hợp lệ (số thuốc đã thay đổi), đã huỷ.");
                                return;
                            }
                            m = new Message(66);
                            m.dos.writeByte(4);
                            player.sendMessage(m);
                            dp.sendMessage(m);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            Database.instance.saveCharAuto(player);
                            Database.instance.saveCharAuto(dp);
                            dp.sendMessage(MessageCreator.createCharInventoryMessage(dp, 0));
                            dp.sendMessage(MessageCreator.createCharInventoryMessage(dp, 1));
                            // Xoa sach ca hai (thay cho chi userTrade.removeAllElements nhu truoc).
                            dp.datLaiTrangThaiGiaoDich();
                            player.datLaiTrangThaiGiaoDich();
                        }

                        return;
                }
            } catch (Exception var18) {
            }
        } finally {
            if (m != null) {
                m.cleanup();
            }
        }
    }

    static void doUseSpecialITem(Map map, Char player, Message msg) {
        if (!map.checkTrade(player)) {
            try {
                short itemID = msg.dis.readShort();
              
                boolean isExist = false;
                String info = "";

                for (int i = 0; i < player.specialItem.size(); i++) {
                    ShopTemplate shop = (ShopTemplate) player.specialItem.elementAt(i);
                    if (shop.realId == itemID && player.listSpItem[shop.id] > 0) {
                        player.listSpItem[shop.id]--;
                        if (player.listSpItem[shop.id] <= 0) {
                            player.listSpItem[shop.id] = 0;
                            player.specialItem.remove(shop);
                            player.removeIDItem(shop.realId);
                        }

                        player.setX2(shop.value);
                        isExist = true;
                        info = shop.name;
                        break;
                    }
                }

                if (isExist) {
                    Message m = new Message(86);
                    m.dos.writeByte(1);
                    m.dos.writeByte(player.x2);
                    m.dos.writeUTF(player.x2 == 1 ? "50%" : "100%");
                    player.sendMessage(m);
                    m.cleanup();
                    m = MessageCreator.createCharSpecialItem(player);
                    player.sendMessage(m);
                    m.cleanup();
                    Database.instance.saveOrtherLog("tob_other_log", player.charname, "Sử dụng " + info, "usb");
                    DungVatPham.bao(player, "Đã dùng " + info + ": +" + (player.x2 == 1 ? "50%" : "100%") + " kinh nghiệm."); // truoc: khong chu nao
                } else {
                    Message m = MessageCreator.createServerAlertMessage("Không tìm thấy vật phẩm trên", "");
                    player.sendMessage(m);
                    m.cleanup();
                }
            } catch (Exception var8) {
            }
        }
    }

    static void exchangeItem(Map map, Char player, Item iItem, int slot) {
        // Vat pham skin linh thu mang type 30, isWearingAnimal (14..18) khong nhan ra,
        // nen phai chan ngay o day keo no roi xuong nhanh do nhan vat. Phan biet
        // bang ID TEMPLATE, khong bang type — xem real.SkinThuCuoi.ID_ITEM.
        if (SkinThuCuoi.laItemSkin(iItem.getTemplate().id)) {
            if (player.animalRide == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Phải cưỡi thú mới trang bị được skin.", ""));
                return;
            }
            map.exchangeItemAnimal(player, iItem, slot);
            return;
        }
        if (isWearingAnimal(iItem.getTemplate().type)) {
            if (player.animalRide != null) {
                map.exchangeItemAnimal(player, iItem, slot);
            }
        } else {
            String sub = iItem.getTemplate().name + "_" + iItem.plus + "|" + iItem.getAttribute() + "|" + iItem.getDBInfo();
            if (!isModelClothes(iItem.getTemplate().atb[9]) && !iItem.isChoiLuaLanBienHinh()) {
                int i = 0;
                int ring = 0;
                new Vector();
                Item[] wItems = player.getArrayCharWearing();
                int index = Char.getIndexItemWearing(iItem.getTemplate().type, 0);
                if (iItem.getTemplate().type == 8 && !player.fullRing()) {
                    int index1 = Char.getIndexItemWearing(iItem.getTemplate().type, 1);
                    if (wItems[index1] == null) {
                        index = index1;
                        iItem.pos = 1;
                    } else {
                        iItem.pos = 0;
                    }
                }

                if (wItems[index] != null) {
                    sub = sub
                            + " >>> "
                            + wItems[index].getTemplate().name
                            + "_"
                            + wItems[index].plus
                            + "|"
                            + wItems[index].getAttribute()
                            + "|"
                            + wItems[index].getDBInfo();
                    player.iItems.add(wItems[index]);
                    wItems[index].place = 0;
                    wItems[index].beKick = 0;
                }

                int var12 = 0;
                iItem.place = 1;
                iItem.slotWearing = player.slotWearing;
                wItems[index] = iItem;
                player.iItems.remove(iItem);
                if (iItem.getType() == 0 || iItem.getType() == 1 || iItem.getType() == 2) {
                    Database.instance.addInfoWebChar(player);
                }
            } 
            else 
            {
                int oldID = player.wModel.idModel;
                player.wModel.changClothes(player, iItem.isMatna() ? 30 : iItem.getTemplate().type);
                player.wModel.setItemModel(player, iItem, iItem.atb[4]);
                player.iItems.remove(iItem);
            }

            Database.instance.saveOrtherLog("tob_log_other_item", player.charname, player.slotWearing + "_" + sub, "change");
        }
    }

    static void exchangeRingItem(Map map, Char player, Item iItem, int pos) {
        if (player.posRing != -1) {
            String sub = iItem.getTemplate().name + "_" + iItem.plus + "|" + iItem.getAttribute() + "|" + iItem.getDBInfo();
            if (!isModelClothes(iItem.getTemplate().atb[9])) {
                int i = 0;
                Item[] wItems = player.getArrayCharWearing();
                int index = Char.getIndexItemWearing(8, player.posRing);
                if (wItems[index] != null) {
                    sub = sub
                            + " >>> "
                            + wItems[index].getTemplate().name
                            + "_"
                            + wItems[index].plus
                            + "|"
                            + wItems[index].getAttribute()
                            + "|"
                            + wItems[index].getDBInfo();
                    player.iItems.add(wItems[index]);
                    wItems[index].place = 0;
                    wItems[index].beKick = 0;
                }

                iItem.pos = player.posRing;
                iItem.place = 1;
                iItem.slotWearing = player.slotWearing;
                wItems[index] = iItem;
                player.iItems.remove(iItem);
            }

            try {
                player.calculateAttrib();
                Message m = MessageCreator.createMainCharInfoMessage(player);
                player.sendMessage(m);
                m = MessageCreator.createCharWearingMessage(player, player);
                player.sendMessage(m);
                m = MessageCreator.createCharInventoryMessage(player, 1);
                player.sendMessage(m);
                m.cleanup();
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                MessageCreator.createMsgCharMonster(player, player);
            } catch (Exception var8) {
                var8.printStackTrace();
            }

            Database.instance.saveOrtherLog("tob_log_other_item", player.charname, player.slotWearing + "_" + sub, "change");
        }
    }

}
