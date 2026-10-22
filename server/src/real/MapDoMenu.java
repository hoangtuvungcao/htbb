package real;


import data.Animal;
import data.CharClan;
import data.CharInfo;
import data.Database;
import data.GemItem;
import data.InfoCharLienDau;
import data.NewClan;
import data.Pet;
import data.Xoso;
import io.Message;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import real.cmd.LoginHandler;
import real.cmd.RequestNPCInfoHandler;
import server.TeamServer;
import BaoVatHuyenThoai.ThoRenBiAn;

/**
 * Than cua Map.doMenu - xu ly nguoi choi chon mot muc trong menu NPC
 * (client gui ma lenh -30). Day la day "if (idNpc == X)" cho cac submenu ao
 * roi den switch(idNpc) khong lo cho tung NPC.
 *
 * 4.601 dong nay truoc nam trong real/Map.java. Map.doMenu VAN CON, giu
 * NGUYEN chu ky protected cu, chi uy nhiem sang day:
 *     protected void doMenu(Char p, Message m) { MapDoMenu.doMenu(this, p, m); }
 * Nho vay lop loi cu (phuc hoi tu htbb-core.jar o P3) goi Map.doMenu van chay binh thuong,
 * va lop con van override duoc nhu cu (hien khong lop nao override).
 */
// Thanh vien static cua Map ma than doMenu dung khong dinh danh.
import static real.Map.ALL_CHAR_CHUYEN_KIM_CUONG;
import static real.Map.SHIELD;
import static real.Map.adminTest;
import static real.Map.allLV;
import static real.Map.allLuongLucky;
import static real.Map.allXuLucky;
import static real.Map.checkTimeRegEventMonster;
import static real.Map.doBuyModelClothe;
import static real.Map.doCreateMatNa;
import static real.Map.doCreateVuKhiThoiTrang;
import static real.Map.getClaninfoByID;
import static real.Map.getTrungRongBang;
import static real.Map.groupMonster;
import static real.Map.isDayOpenLiendau;
import static real.Map.itemTemplates;
import static real.Map.lvEventMonster;
import static real.Map.nationBeAttack;
import static real.Map.nationWar;
import static real.Map.openLog;
import static real.Map.ALL_CHAR_HS;
import static real.Map.ALL_CHAR_LOI_DAI;
import static real.Map.HASH_ALL_CHAR_HS;
import static real.Map.ID_MAT_NA;
import static real.Map.ID_VU_KHI_THOI_TRANG;
import static real.Map.TYPE_SHOP_NEW_BO_ITEM_VAO_TUI;
import static real.Map.TYPE_SHOP_NEW_CUA_HANG_VIP;
import static real.Map.TYPE_SHOP_NEW_TUI_DO;
import static real.Map.TYPE_SHOP_NEW_TRUNG_THU;
import static real.Map.abs;
import static real.Map.allUser;
import static real.Map.allWedding;
import static real.Map.all_item_shop_special;
import static real.Map.all_item_shop_trungthu;
import static real.Map.checkCanReceiveLixi;
import static real.Map.checkFullNation;
import static real.Map.doTuoiNuocCayLixi;
import static real.Map.event;
import static real.Map.gemTemplate;
import static real.Map.getTown;
import static real.Map.hourGiveLixi;
import static real.Map.idClanTown;
import static real.Map.idMapChanNui;
import static real.Map.idMapChoLoiDai;
import static real.Map.idMapLoiDai;
import static real.Map.idMapMONSTER;
import static real.Map.idMapTown;
import static real.Map.inRangeActor;
import static real.Map.isMapLang;
import static real.Map.isMapThanh;
import static real.Map.listKillPerson;
import static real.Map.minuteGiveLixi;
import static real.Map.r;
import static real.Map.random;
import static real.Map.sendAllCharServer;

class MapDoMenu {

    /** Gia ve tau (Xa Phu, npc -9) tinh bang luong - them 22/09/2026, truoc day mien phi.
     * Sua 22/09/2026 (yeu cau nguoi dung, 2 lan): 20 -> 50 -> 100 luong/chuyen. */
    private static final long GIA_VE_TAU_LUONG = 100L;

    /**
     * NPC dau truong (-31): doi VI TRI HIEN THI tren menu ve VI TRI GOC trong ma.
     *
     * Menu da bo 4 muc (vi tri cu 2 "Dang ky chien truong", 3 "Nhan qua khu lien dau",
     * 4 "Nhan qua Lien tram", 5 "Nhan qua top tru") — xem RequestNPCInfoHandler case -31.
     *
     * VI SAO KHONG DANH SO LAI CHUOI if/else O CASE -31: client gui ve vi tri NGUOI CHOI
     * NHIN THAY, ma bo 4 muc o giua thi moi muc con lai deu tut len. De nguyen la chon
     * "Nui chau bau" (gio nam o vi tri 2) se chay nham nhanh "Dang ky chien truong" —
     * dung loai loi im lang, kho lan ra. Doi vi tri ngay tai cua vao thi toan bo logic
     * ben duoi giu nguyen, va bat lai muc nao cung chi la sua hai cho: them lai chuoi
     * vao menu, them lai vi tri goc vao bang nay, dung thu tu.
     *
     * Bang nay dong thoi la mot lop chan: chi cac vi tri co trong day moi duoc chap nhan,
     * nen client cu hoac client sua gui thang so 3/4/5 cung khong goi lai duoc 4 chuc
     * nang da go.
     *
     * Chi so trong mang = vi tri hien thi; gia tri = vi tri goc ma case -31 dang hieu.
     */
    private static final int[] VI_TRI_MENU_DAU_TRUONG = { 0, 1, 6, 7, 8, 9 };

    static void doMenu(Map map, Char player, Message message) {
        try {
            int rangeNpc = 5;
            int idNpc = message.dis.readShort();
            int idMenu = message.dis.readUnsignedByte();
            int idOptionMenu = message.dis.readByte();
            long n = 0L;
            Map oldMap = player.map;
            int[] var10000 = new int[]{21, 721, 722, 723, 724};
            int[][] mapstart = LangKhoiDau.bang(player);
            // Bang setting cua admin (npc ao -1000)
            if (AdminMenu.xuLyChon(player, idNpc, idMenu, idOptionMenu)) {
                return;
            }
            // Guc / hoi sinh: menu -803 (client cu chon cach hoi sinh), -804 (dong doi cuu)
            if (HoiSinhGame.xuLyMenu(player, idNpc, idMenu, idOptionMenu)) {
                return;
            }
            // The dat ten Pokemon: menu chon pet (NPC ao -561)
            if (DatTenPetGame.xuLyMenu(player, idNpc, idMenu, idOptionMenu)) {
                return;
            }
            // NPC tinh co menu dich vu (Y ta Chansey 0, Meowth ngan hang 22/31) - real.NpcDichVu.
            if (NpcDichVu.xuLyMenu(player, idNpc, idMenu, idOptionMenu)) {
                return;
            }
            // [khoa-khi-mac] NPC ao pha khoa trang bi (mo tu muc 3 cua Machamp tho ren, case 2 ben duoi).
            if (idNpc == PhaKhoaGame.NPC_ID) {
                PhaKhoaGame.doMenu(player, idMenu, idOptionMenu);
                return;
            }
            // [bo-ngoc] Machamp tho ren (loai 2): muc "Che tao & kham ngoc" noi cuoi menu (BoNgocGame.themMucThoRen)
            if (idNpc == 2 && idMenu == 0 && idOptionMenu == BoNgocGame.viTriThoRen) {
                BoNgocGame.moMenu(player);
                return;
            }
            if (idNpc == BoNgocGame.NPC_ID) {
                BoNgocGame.doMenu(player, idMenu, idOptionMenu);
                return;
            }
            // [bat-pokemon] menu bong / Hop PC (npc ao -581..-584)
            if (BatPokemonGame.doMenu(player, idNpc, idMenu, idOptionMenu)) {
                return;
            }
            if (map.isNpcThanh(idNpc) && map.mapId != 201 && !map.isMapLienDau()) {
                return;
            }
            // admin
            if (idNpc == -2) {
                if (idMenu == 0) {
                    switch (idOptionMenu) {
                        case 0:
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang phát triển", ""));
                            break;
                    }
                    return;
                }
            }
            if (idNpc == -519) {
                if (idMenu == 0) {
                    switch (idOptionMenu) {
                        case 0:
                            player.sendMessage(MessageCreator.createMsgInputText(player.id, 99, "Nhập mật khẩu ID pet muốn xóa ", 1));
                            break;
                        case 1:
                            player.sendMessage(MessageCreator.createMsgInputText(player.id, 98, "Nhập mật khẩu ID thú muốn xóa ", 1));
                            break;
                    }
                }
                return;
            }
            // nap tien: muc "Cửa hàng & Nạp" (NPC -10) + npc ao -540..-549. Tat co: khong bat gi.
            if (com.htbb.pokemon.NapTienGame.xuLyMenu(player, idNpc, idMenu, idOptionMenu)) {
                return;
            }
            // NPC Tong Quan (-10): muc "Nhan moc nap" luon la muc cuoi menu, vi tri that su
            // duoc RequestNPCInfoHandler ghi lai vao player.idxMocNapMenu khi mo menu.
            if (idNpc == -10 && player.idxMocNapMenu >= 0 && idMenu == 0 && idOptionMenu == player.idxMocNapMenu) {
                map.showMenuMocNap(player);
                return;
            }
            // NPC Tong Quan (-10), muc "Su kien World Cup": cung la muc cuoi menu, vi tri
            // ghi trong player.idxSuKienWorldCup. CHU Y idMenu == 0 && idOptionMenu == vi tri:
            // client tra lai DUNG idMenu no nhan duoc (o menu goc la 0) va gui dong duoc chon
            // trong idOptionMenu - xem GameService.doMenuOption ben client.
            if (idNpc == -10 && SuKienWorldCup.hienTrongMenu()) {
                if (idMenu == 0 && player.idxSuKienWorldCup >= 0
                        && idOptionMenu == player.idxSuKienWorldCup) {
                    player.sendMessage(MessageCreator.createMsgMenu(
                            SuKienWorldCup.menuGoc(), idNpc, SuKienWorldCup.MENU_GOC));
                    return;
                }
                if (idMenu == SuKienWorldCup.MENU_GOC || idMenu == SuKienWorldCup.MENU_BXH) {
                    SuKienWorldCup.doMenu(player, idNpc, idMenu, idOptionMenu);
                    return;
                }
            }
            // NPC Tong Quan (-10), muc "Nhan thuong đua top": cung la muc cuoi menu, vi
            // tri ghi trong player.idxThuongDuaTop. Xem real/SuKienDuaTop.
            if (idNpc == -10 && idMenu == 0 && player.idxThuongDuaTop >= 0
                    && idOptionMenu == player.idxThuongDuaTop) {
                SuKienDuaTop.nhanThuong(player);
                return;
            }
            // NPC Tong Quan (-10), menu con "chon 1 trong nhieu qua đua top" (mot so
            // phan thuong de NGUOI CHOI tu chon, vd mat na/phi phong khang - xem cot
            // item_chon trong real/SuKienDuaTop.java).
            if (idNpc == -10 && idMenu == SuKienDuaTop.MENU_CHON_THUONG) {
                SuKienDuaTop.doChonThuong(player, idOptionMenu);
                return;
            }
            // NPC Tong Quan (-10), muc "Chuyen luong": cung la muc cuoi menu, vi tri ghi
            // trong player.idxChuyenLuong. Xem real/CharChuyenLuong.
            if (idNpc == -10 && idMenu == 0 && player.idxChuyenLuong >= 0
                    && idOptionMenu == player.idxChuyenLuong) {
                CharChuyenLuong.moNhapTen(player);
                return;
            }
            // NPC Tong Quan (-10), muc "Kho xu/luong": cung la muc cuoi menu, vi tri ghi
            // trong player.idxKhoXuLuong. Xem real/KhoXuLuong.
            if (idNpc == -10 && idMenu == 0 && player.idxKhoXuLuong >= 0
                    && idOptionMenu == player.idxKhoXuLuong) {
                KhoXuLuong.mo(player);
                return;
            }
            if (idNpc == KhoXuLuong.NPC_ID) {
                KhoXuLuong.doMenu(player, idOptionMenu);
                return;
            }
            if (idNpc == KhoXuLuong.NPC_ID_GUI) {
                KhoXuLuong.doMenuGui(player, idOptionMenu);
                return;
            }
            if (idNpc == KhoXuLuong.NPC_ID_RUT) {
                KhoXuLuong.doMenuRut(player, idOptionMenu);
                return;
            }
            if (idNpc == BaiLuyenTap.NPC_ID) {
                BaiLuyenTap.doMenu(player, idOptionMenu);
                return;
            }
            // NPC Ruong (-15), muc "Vong quay": chon loai (idMenu 4), menu con VIP/thuong/luong (5/6/103), bang chon
            // muc dat (104/105/106) - tat ca o real.VongQuay (sua 19/10/2026: nut Dong y chet, muc cuoc lam phat).
            if (idNpc == VongQuay.NPC && isMapLang(player.map)
                    && VongQuay.doMenu(player, idNpc, idMenu, idOptionMenu)) {
                return;
            }
            if (idNpc == -520) {
                map.doChonMocNap(player, idOptionMenu);
                return;
            }
            if (idNpc == -521) {
                map.doChonPhuongHoangMocNap(player, idOptionMenu);
                return;
            }
            // NPC Doi Thuong (-90): mot dong duy nhat "Xem doi thuong" — nut "Go skin linh
            // thu" da bi bo (theo yeu cau nguoi choi), nen moi lua chon o day deu mo bang
            // diem doi thuong. Con hai duong go skin khac: lenh chat "boskin" va goi packet
            // 29 chung cho moi trang bi thu (Map.thaoTrangBiThu).
            // Pokemon (G7.D3): muc "Pokédex" noi vao cuoi menu NPC doi thuong (-90),
            // menu con o npc ao PokedexPokemon.NPC_ID (-526). Tat co: khong co muc nay.
            if (idNpc == -90 && idMenu == 0
                    && idOptionMenu == PokedexPokemon.VI_TRI_MUC_NPC_DOI_THUONG) {
                PokedexPokemon.moMenu(player);
                return;
            }
            // GD2: So tay huan luyen (thiet ke dai han) - npc ao SoTayLauDai.NPC_ID (-527).
            if (idNpc == -90 && idMenu == 0 && com.htbb.pokemon.LauDai.bat()
                    && idOptionMenu == SoTayLauDai.viTri()) {
                SoTayLauDai.moMenu(player);
                return;
            }
            if (idNpc == SoTayLauDai.NPC_ID) {
                SoTayLauDai.doMenu(player, idMenu, idOptionMenu);
                return;
            }
            // he sinh thai: muc cuoi menu NPC -90 + npc ao HeSinhThaiGame.NPC_ID (-528).
            if (idNpc == -90 && idMenu == 0 && com.htbb.pokemon.HeSinhThai.bat()
                    && idOptionMenu == com.htbb.pokemon.HeSinhThaiMenu.viTri()) {
                com.htbb.pokemon.HeSinhThaiMenu.moMenu(player);
                return;
            }
            if (idNpc == com.htbb.pokemon.HeSinhThaiGame.NPC_ID) {
                com.htbb.pokemon.HeSinhThaiMenu.doMenu(player, idMenu, idOptionMenu);
                return;
            }
            if (idNpc == PokedexPokemon.NPC_ID) {
                PokedexPokemon.doMenu(player, idMenu, idOptionMenu);
                return;
            }
            if (idNpc == -90 && idMenu == 0) {
                map.showMenuDiemDoiThuong(player);
                return;
            }
            if (idNpc == -91) {
                map.doChonDoiThuong(player, 0, idOptionMenu);
                return;
            }
            if (idNpc == -92) {
                map.doChonDoiThuong(player, 1, idOptionMenu);
                return;
            }
            if (idNpc == -518) {
                if (idMenu == 0) {
                    switch (idOptionMenu) {
                        case 0:
                        map.showKhoManh(player);
                        break;
                       case 1:
                        map.ShowBoSuuTap(player);
                        break;
                        case 2:
                        map.ShowCaiTrang(player);
                            break;
                        case 3:
                        if(player.isBatCaiTrang()){
                            player.setTatCaiTrang();
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã tắt hiển thị cải trang.", ""));
                        }else{
                            player.setBatCaiTrang();
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã bật hiển thị cải trang.", ""));
                        }
                            break;
                       
                    }
                }
            }

            if (idNpc == -516) {
                if (idMenu == 0) {
                    // Bai luyen cong + Auto Ve Map Up (them 22/09/2026): vi tri trong menu
                    // THAY DOI tuy Shop Trung Thu con bat hay khong nen kiem qua Char.idxXxx
                    // truoc, KHONG dung switch case co dinh (xem Map.java case type==-10000).
                    if (player.idxBaiLuyenCong >= 0 && idOptionMenu == player.idxBaiLuyenCong) {
                        BaiLuyenTap.mo(player);
                        return;
                    }
                    if (player.idxAutoVeMapUp >= 0 && idOptionMenu == player.idxAutoVeMapUp) {
                        player.isAutoVeMapUp = !player.isAutoVeMapUp;
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                "Auto về Map Up: " + (player.isAutoVeMapUp ? "đang bật" : "đang tắt"), ""));
                        return;
                    }
                    switch (idOptionMenu) {
                        case 0:
                            if (player.vip < 1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ Vip 3 trở lên mới sử dụng được chức năng này.", ""));
                                return;
                            }
                            player.sendMessage(MessageCreator.createMessageShopNew(player, player.bagItems, "Lấy ra", "Túi Đồ", TYPE_SHOP_NEW_TUI_DO));
                            break;
            
                        case 1:
                            if (player.vip < 1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ Vip 3 trở lên mới sử dụng được chức năng này.", ""));
                                return;
                            }
                            player.sendMessage(MessageCreator.createMessageShopNew(player, player.iItems, "Bỏ vào", "Hành trang", TYPE_SHOP_NEW_BO_ITEM_VAO_TUI));
                            break;
            
                        case 2:
                            if (player.vip <= 0) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần Vip 1 trở lên để sử dụng chức năng này.", ""));
                                return;
                            }
                            Message m = new Message(23);
                            m.dos.writeByte(2);
                            player.isPutItem2Bag = true;
                            m.dos.writeByte(player.bItem.size());
            
                            for (int i = 0; i < player.bItem.size(); i++) {
                                m.dos.writeByte(((Item) player.bItem.get(i)).clazz);
                                m.dos.writeShort(((Item) player.bItem.get(i)).id);
                                int template = ((Item) player.bItem.get(i)).getTemplate().id;
                                m.dos.writeShort(template);
                                m.dos.writeByte(((Item) player.bItem.get(i)).plus);
                                m.dos.writeByte(((Item) player.bItem.get(i)).level);
                                m.dos.writeShort(((Item) player.bItem.get(i)).mDurable);
                                m.dos.writeShort(((Item) player.bItem.get(i)).durable);
                            }
                            player.sendMessage(m);
                            break;
            
                        case 3:
                            player.sendMessage(MessageCreator.createMessageShopNew(player, all_item_shop_special, "Mua", "Shop Đặc Biệt", TYPE_SHOP_NEW_CUA_HANG_VIP));
                            break;
            
                        case 4:
                            if (player.vip <= 0) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần Vip 1 trở lên để sử dụng chức năng này.", ""));
                                return;
                            }
                            player.rcvInviteVip = !player.rcvInviteVip;
                            break;
                        case 5:
                        if (!player.checkItemTheMuaBan()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Cần có thẻ mua bán trong rương để sử dụng chức năng này.", ""));
                            return;
                        }
                        if(player.isAutoBan){
                            player.isAutoBan = false;
                            break;
                        }else{
                            player.isAutoBan = true;
                            return;
                        }
                        case 6:
                        if (!player.checkItemTheMuaBan()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Cần có thẻ mua bán trong rương để sử dụng chức năng này.", ""));
                            return;
                        }
                        if(player.isNghienAuto)
                        {
                            player.isNghienAuto = false;
                            break;
                        }else{
                            player.isNghienAuto = true;
                            return;
                        }
                        case 7:
                            // Moi 19/09/2026: "Shop Trung Thu" - chi hien khi su kien dang
                            // bat (menu da tu gate o Map.java dong ~8174), nhung kiem tra lai
                            // o day cho chac neu client cache menu cu.
                            if (Char.isFinishAllSuKienTrungThul2016()) {
                                return;
                            }
                            player.sendMessage(MessageCreator.createMessageShopNew(player, all_item_shop_trungthu, "Mua", "Shop Trung Thu", TYPE_SHOP_NEW_TRUNG_THU));
                            break;
                    }
                }
                return;
            }

            if (idNpc == -515) {
                if (!player.isFullInventory()) {
                    if (idMenu != 0) {
                        if (idMenu >= 1 && idMenu <= 5) {
                            if (player.potions[143] < 1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[143], ""));
                                return;
                            }

                            String[] noption = new String[]{"tang dam", "tang thu", "tang exp", "tang bk", "tang lan"};
                            player.potions[143]--;
                            int[] valuemn = new int[]{5, 5, 10, 30, 50};
                            boolean isvv = Char.checkReceiveTrungLinhThuRuongBatbao(player);

                            try {
                                Database.instance.saveEvent(event.getInfo());
                            } catch (Exception var44) {
                            }

                            int time = 129600;
                            if (isvv) {
                                time = 0;
                            }

                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            Item newitem = doCreateMatNa(player, time, ID_MAT_NA[idMenu - 1], idOptionMenu, valuemn[idOptionMenu]);
                            player.sendMessage(
                                    MessageCreator.createServerAlertMessage("Nhận được " + newitem.getName() + (time != 0 ? " 3 tháng" : " vĩnh viễn "), "")
                            );
                            Database.instance
                                    .saveOrtherLog(
                                            "",
                                            player.charname,
                                            player.potions[143] + " doi hop bi mat " + (time != 0 ? " 3 tháng: " : "vv ") + newitem.getName() + "|" + newitem.getInfoSave(),
                                            "hopbimat"
                                    );
                            Database.instance.saveLogThongKeChiTieu(newitem.getName() + "_" + noption[idOptionMenu], 0, 1, 1L, "hopbimat");
                        }
                    } else {
                        if (player.potions[143] < 1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[143], ""));
                            return;
                        }

                        player.sendMessage(
                                MessageCreator.createMsgMenu(
                                        new String[]{"Tăng 5% dam", "Tăng 5% thủ", "Tăng 10% exp", "Bạo kích 3%", "Tăng 5% lan"}, idNpc, 1 + idOptionMenu
                                )
                        );
                    }

                    return;
                }

                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                return;
            }

            if (idNpc == -514) {
                int idPet = player.idPetTang;
                if (idOptionMenu != 0) {
                    if (idOptionMenu == 1) {
                        player.sendMessage(MessageCreator.createMsgInputText(-32046, 0, "Tên người nhận", 0));
                    }
                } else {
                    Pet pet = null;

                    for (int i = 0; i < player.pet.size(); i++) {
                        pet = (Pet) player.pet.get(i);
                        if (pet.id == idPet) {
                            pet.place = 1;
                            break;
                        }
                    }

                    if (pet == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng", ""));
                    } else {
                        if (player.petUsing != null) {
                            player.petUsing.place = 0;
                            if (!player.petUsing.isPetTool() && player.petUsing.getIdEffPet() > -1 && player.petUsing.getIdEffPet() != pet.getIdEffPet()) {
                                player.huyEff(player, player.petUsing.getIdEffPet());
                            }
                        }

                        player.petUsing = pet;
                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                        player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                        player.sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player));
                        if (!player.petUsing.isPetTool()
                                && pet.getIdEffPet() > -1
                                && player.addEffBuff(pet.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
                            player.sendEffToChar(player);
                        }

                        Database.instance.addInfoWebChar(player);
                    }
                }

                return;
            }

            switch (idNpc) {
                case -802:
                    if (HoiSinhGame.chanCu(player)) {
                        return; // menu "Dung co hoi sinh / Hs bang xu" cu -> hop thoai lua chon moi
                    }
                    if (idMenu != 0) {
                        if (idMenu == 1) {
                            if (idOptionMenu != 0) {
                                if (idOptionMenu == 1) {
                                    int cp = 100000;
                                    if (player.getxu() < (long) cp) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Hết tiền", ""));
                                        return;
                                    }

                                    player.subXu((long) cp, true, "map 48");
                                    player.hp = player.maxhp;
                                    player.mp = player.maxmp;
                                    player.timeHs = Char.timeAddMoreHs;
                                    player.desTroy();
                                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                    player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.sendPetEff();
                                    Database.instance.saveOrtherLog("tob_other_log", player.charname, "100k xu", "hs");
                                }
                            } else {
                                if (player.potions[119] == 0) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không có cỏ hồi sinh", ""));
                                    return;
                                }

                                player.potions[119]--;
                                player.hp = player.maxhp;
                                player.mp = player.maxmp;
                                player.timeHs = Char.timeAddMoreHs;
                                player.desTroy();
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                player.sendPetEff();
                                Database.instance.saveOrtherLog("tob_other_log", player.charname, "hs dung tien dan", "hs");
                            }
                        }
                    } else if (idOptionMenu != 0) {
                        if (idOptionMenu == 1 && !map.isvanTienTran()) {
                            int[] cost = new int[]{
                                1000,
                                2000,
                                3000,
                                4000,
                                5000,
                                10000,
                                15000,
                                20000,
                                30000,
                                60000,
                                70000,
                                500000,
                                1000000,
                                1300000,
                                1500000,
                                2000000,
                                2500000,
                                3000000,
                                4500000
                            };
                            int cp = cost[Math.min(player.lvDetail.lv / 5, cost.length - 1)];
                            if (player.myCountry != player.inCountry && player.charthanthu != null) {
                                cp = cost[Math.min(player.lastLV / 5, cost.length - 1)];
                            }

                            if (player.charthanthu != null && player.isKiller) {
                                cp *= 2;
                            }

                            if (player.mapID == 112 || player.mapID == 2541 || player.mapID == 2542 || player.mapID == 2543 || player.mapID == 2544) {
                                cp = 500000;
                            }

                            if (player.mapID == 204 || player.mapID == 205) {
                                cp = 0;
                            }

                            if (player.getxu() < (long) cp) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hết tiền", ""));
                                return;
                            }

                            if (isMapThanh(map) && !player.canHSChiemThanh() && getTown[player.inCountry]) {
                                long time = 1L - (System.currentTimeMillis() - player.timeWaitComeHome) / 1000L;
                                if (time < 1L) {
                                    time = 1L;
                                }

                                if (HASH_ALL_CHAR_HS.get(player.charDBID) == null) {
                                    ALL_CHAR_HS.add(player);
                                    HASH_ALL_CHAR_HS.put(player.charDBID, player);
                                }

                                return;
                            }

                            player.subXu((long) cp, true, "map 44");
                            player.hp = player.maxhp;
                            player.mp = player.maxmp;
                            player.timeHs = Char.timeAddMoreHs;
                            player.desTroy();
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendPetEff();
                            Database.instance.saveOrtherLog("tob_other_log", player.charname, String.valueOf(cp), "hs");
                        }
                    } else {
                        if (player.potions[119] == 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không có cỏ hồi sinh", ""));
                            return;
                        }

                        player.potions[119]--;
                        player.timeHs = Char.timeAddMoreHs;
                        player.hp = player.maxhp;
                        player.mp = player.maxmp;
                        player.desTroy();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendPetEff();
                        Database.instance.saveOrtherLog("tob_other_log", player.charname, "hs dung tien dan", "hs");
                    }
                    break;
                case -801:
                    if (idOptionMenu != 0) {
                        if (idOptionMenu != 1) {
                            break;
                        }

                        Pet pet = (Pet) player.pet.get(idMenu);
                        if (pet.isPetBachThu() && map.isMapOffline && pet.place != 1) {
                            player.pet.remove(idMenu);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                            Database.instance.deletePet(pet.idDB);
                            Database.instance
                                    .saveOrtherLog(
                                            "", player.charname, "ban pet " + pet.getDBInfo() + " > " + pet.getInfoItem() + " > " + pet.idImg + " > " + pet.getName(), "sellpet"
                                    );
                            break;
                        }

                        return;
                    }

                    map.doCuoiThu(player, 1, idMenu);
                    break;
                case -800:
                    if (idOptionMenu != 0) {
                        if (idOptionMenu == 1) {
                            if (!player.map.isMapOffline) {
                                Animal anim = (Animal) player.animal.get(idMenu);
                                if (!anim.isAnimalCanTang() || map.isMapOffline || anim.place == 1) {
                                    return;
                                }

                                player.sendMessage(MessageCreator.createMsgInputText(-32048, idMenu, "Tên người nhận", 0));
                            } else {
                                Animal anim = (Animal) player.animal.get(idMenu);
                                if (!anim.isPoro() && !anim.isLan() && !anim.isAnimalCanTang() || !map.isMapOffline || anim.place == 1) {
                                    return;
                                }

                                player.animal.remove(idMenu);
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                                Database.instance.delAnimal(anim.dbid);
                                Database.instance.saveOrtherLog("", player.charname, "ban pet " + anim.getAttribute() + " > " + anim.getName() + " > " + anim.idImg, "sellanimal");
                            }
                        }
                    } else {
                        Animal anim = (Animal) player.animal.get(idMenu);
                        map.doCuoiThu(player, 0, anim.id);
                    }
                    break;
                case -517:
                    ThoRenBiAn.Option(player, idNpc, idMenu, idOptionMenu);
                    break;
                case -513:
                    if (player.itemTam != null) {
                        // Da toi cap toi da (idItemUpLevel == -1): menu chi con 2 muc, "Nâng cấp"
                        // bi an di (xem Map.java / RealController.java, cho tao menu -513), nen
                        // "Ảo hóa" lui tu idOptionMenu 2 xuong 1. Phai kiem TRUOC khi doc
                        // idOptionMenu ben duoi, khong thi "Ảo hóa" o menu 2 muc se bi hieu nham
                        // thanh "mo bang chon hoả thạch nâng cấp".
                        final boolean daToiDaCap = player.itemTam.getTemplate().idItemUpLevel == -1;
                        if (idMenu != 0) {
                            if (idMenu == 1) {
                                if (player.itemTam.minuteExist > 0) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nâng cấp", ""));
                                    return;
                                }

                                if (idOptionMenu != 0) {
                                    if (player.listGemitem[GemTemplate.HOA_THACH_C2] < 1 && player.listGemitemLock[GemTemplate.HOA_THACH_C2] < 1) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nham thạch", ""));
                                        player.itemTam = null;
                                        return;
                                    }

                                    player.doNangCapHoaKyLan(player.itemTam, GemTemplate.HOA_THACH_C2);
                                    // Bat buoc don itemTam sau khi da xu ly, du thanh cong hay
                                    // that bai: giu lai tham chieu la cho phep gui lai DUNG goi
                                    // -30 nay nhieu lan tren mot doi tuong Item da bi tieu (xem
                                    // chot moi trong Char.doNangCapHoaKyLan/doTrangBiHoaKyLan).
                                    player.itemTam = null;
                                } else {
                                    if (player.listGemitem[GemTemplate.HOA_THACH_C1] < 1 && player.listGemitemLock[GemTemplate.HOA_THACH_C1] < 1) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nham thạch", ""));
                                        player.itemTam = null;
                                        return;
                                    }

                                    player.doNangCapHoaKyLan(player.itemTam, GemTemplate.HOA_THACH_C1);
                                    player.itemTam = null;
                                }
                            }
                        } else if (!daToiDaCap && idOptionMenu == 1) {
                            if (player.itemTam.minuteExist > 0) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nâng cấp", ""));
                                return;
                            }

                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Nham thạch cấp 1", "Nham thạch cấp 2"}, -513, 1));
                        } else if ((daToiDaCap && idOptionMenu == 1) || (!daToiDaCap && idOptionMenu == 2)) {
                            // "Ảo hóa" - xem Char.doAoHoaHoaKyLan. Ham do tu kiem vinh vien/
                            // du luong/mac len nguoi, o day chi don itemTam sau khi xong.
                            player.doAoHoaHoaKyLan(player.itemTam);
                            player.itemTam = null;
                        } else {
                            player.doTrangBiHoaKyLan(player.itemTam);
                            Database.instance.saveOrtherLog("", player.charname, player.itemTam.getInfoSave(), "itemthanthu");
                            player.itemTam = null;
                        }
                    }
                    break;
                case -512:
                    if (idMenu == 0) {
                        if (player.itemLinhDanhThue == null) {
                            return;
                        }

                        // idOptionMenu tu goi tin - menu chon ho ve chi co 10 muc (5 he x 2
                        // gioi tinh, xem Map.doUseItemLinhDanhThue). Thieu kiem tra nay se cho
                        // gia tri ngoai bien lam chi so vao namehe[], nem lai ArrayIndexOutOf
                        // BoundsException (giong lo hong da vay o nhanh -511 ngay duoi).
                        if (idOptionMenu < 0 || idOptionMenu >= 10) {
                            data.NghiVanGianLan.ghiVatPham(player, "hackruong",
                                    "gui chi so chon ho ve ngoai bien: " + idOptionMenu);
                            return;
                        }

                        int clazz = idOptionMenu / 2;
                        int gender = idOptionMenu % 2;
                        String[] namehe = new String[]{"kim", "hoả", "thuỷ", "thổ", "mộc"};
                        if (player.charHire != null) {
                            player.sendMessage(
                                    MessageCreator.createMsgPopUp(
                                            player.id,
                                            14 + idOptionMenu,
                                            "Pokémon hỗ trợ hiện tại của bạn sẽ bị mất. Bạn có muốn triệu hồi Pokémon hỗ trợ "
                                            + (gender != 0 ? "nữ hệ " + namehe[clazz] + " không?" : "nam hệ " + namehe[clazz] + " không?")
                                    )
                            );
                        } else {
                            player.sendMessage(
                                    MessageCreator.createMsgPopUp(
                                            player.id,
                                            14 + idOptionMenu,
                                            "Bạn có muốn triệu hồi Pokémon hỗ trợ " + (gender != 0 ? "nữ hệ " + namehe[clazz] + " không?" : "nam hệ " + namehe[clazz] + " không?")
                                    )
                            );
                        }
                    }
                    break;
                case -511:
                    if (player.idNglieuDoi == -1) {
                        return;
                    }

                    // idOptionMenu tu goi tin, dung lam chi so vao mang 5 phan tu.
                    if (idOptionMenu < 0 || idOptionMenu >= GemTemplate.ID_MATERIAL_LOW[5].length) {
                        data.NghiVanGianLan.ghiVatPham(player, "hackruong",
                                "gui chi so nguyen lieu ruong Dracula ngoai bien: " + idOptionMenu);
                        return;
                    }

                    int sl = player.idNglieuDoi;
                    boolean lock = r.nextInt(2) == 1;
                    player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl, lock);
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn nhận được " + sl + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "")
                    );
                    Database.instance
                            .saveOrtherLog(
                                    "",
                                    player.charname,
                                    "Mo ruong Darkrai nhan duoc " + sl + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name,
                                    "ruongdracula"
                            );
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    player.idNglieuDoi = -1;
                    break;
                case -510:
                    if (player.idItemTach == 32100) {
                        return;
                    }

                    MapEpNgoc.doTachNguyenLieu(map, player, player.idItemTach, idOptionMenu);
                    player.idItemTach = 32100;
                case -509:
                case -508:
                case -500:
                case -300:
                case -63:
                case -62:
                case -61:
                case -60:
                case -59:
                case -58:
                case -57:
                case -56:
                case -55:
                case -54:
                case -53:
                case -52:
                case -30:
                case -14:
                case -7:
                case 1:
                case 26:
                default:
                    break;
                case -507:
                    if (player.itemTemp == -40000) {
                        return;
                    }

                    player.posRing = (byte) (idOptionMenu != 0 ? 0 : 1);
                    Item item = player.getItemFormVector(player.iItems, (short) player.itemTemp);
                    if (item != null) {
                        MapMuaBan.exchangeRingItem(map, player, item, player.posRing);
                    }

                    player.itemTemp = -40000;
                    player.posRing = -1;
                    break;
                case -506:
                    if (!NpcTinhBanDo.oNoiDichVu(player, NpcTinhBanDo.NPC_THO_REN)) {
                        return;
                    }

                    // idOptionMenu doc thang tu goi tin (dis.readByte) nen client gui
                    // gia tri nao cung duoc. Chi co 3 che do nguyen lieu hop le.
                    if (idOptionMenu < 0 || idOptionMenu > 2) {
                        data.NghiVanGianLan.ghiVatPham(player, "hackkham",
                                "gui che do nguyen lieu kham khong hop le: " + idOptionMenu);
                        return;
                    }

                    int priceKham = MapEpNgoc.PRICE_KHAM;
                    Message m1 = new Message(-34);
                    m1.dos.writeShort(priceKham);
                    m1.dos.writeByte(idOptionMenu);
                    player.typeGemKham = (byte) idOptionMenu;
                    player.sendMessage(m1);
                    break;
                case -505:
                    if (!NpcTinhBanDo.oNoiDichVu(player, NpcTinhBanDo.NPC_THO_REN)) {
                        return;
                    }

                    // BAT BUOC chan bien. typeGemImbue quyet dinh CA phep kiem ton kho
                    // (MapEpNgoc.java:379-381) LAN phep tru ngoc (MapEpNgoc.java:584-612),
                    // va ca hai deu la chuoi "if 0 / else if 1 / else if 2" KHONG CO ELSE.
                    // Gui gia tri thu tu (vi du 3) thi: phep kiem khong dieu kien nao dung
                    // -> cho qua ma khong kiem ton kho, va phep tru cung khong nhanh nao
                    // chay -> khong tru vien ngoc nao. Tuc la EP NGOC MIEN PHI, khong can
                    // co ngoc trong tui.
                    if (idOptionMenu < 0 || idOptionMenu > 2) {
                        data.NghiVanGianLan.ghiVatPham(player, "hackepngoc",
                                "gui che do nguyen lieu ep ngoc khong hop le: " + idOptionMenu);
                        return;
                    }

                    int priceImbue = 5000;
                    Message m = new Message(71);
                    m.dos.writeByte(0);
                    m.dos.writeShort(priceImbue);
                    m.dos.writeByte(idOptionMenu);
                    player.typeGemImbue = (byte) idOptionMenu;
                    player.sendMessage(m);
                    break;
                case -504:
                    map.doSendTopNation(player, idOptionMenu);
                    break;
                case -503:
                    if (!isMapThanh(player.map)) {
                        return;
                    }

                    map.doAddLienHoaTru(player, idOptionMenu);
                    break;
                case -502:
                    // infoItemCreate bi dat lai null moi lan mo NPC tho ren
                    // (RequestNPCInfoHandler case -8) va khi tao lai nhan vat.
                    // Client van co the gui lua chon cua bang menu -502 cu sau
                    // do, luc ay khong con vat pham nao dang chon -> NPE.
                    if (player.infoItemCreate == null || player.infoItemCreate.item == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hãy chọn lại vật phẩm cần nâng cấp.", ""));
                        return;
                    }
                    // Menu chi co 3 muc (mo / khoa / tong hop) nhung idOptionMenu doc thang tu
                    // goi tin. Gia tri ngoai 0..2 lam Map.java:6906 (chot khoa do khi dung
                    // nguyen lieu khoa) khong nhan ra che do "tong hop" (chi so sanh == 1 / == 2)
                    // trong khi deleteGemItem van coi moi gia tri khac 0/1 la "tong hop, uu tien
                    // tru kho khoa" - do tao ra bang nguyen lieu khoa (khong giao dich duoc) ma
                    // van khong bi khoa.
                    if (idOptionMenu < 0 || idOptionMenu > 2) {
                        return;
                    }
                    player.infoItemCreate.materialUse = (byte) idOptionMenu;
                    map.doSendIfoMaterial2Client(player, player.infoItemCreate.typeOption);
                    break;
                case -501:
                    if (idMenu != 0) {
                        break;
                    }

                    if (player.idClan == -1) {
                        return;
                    }

                    if (player.rankGov <= 1 && player.rankGov >= 0) {
                        int[][] idInput = new int[][]{{-32008, -32009, -32010, -32011}, {-32012, -32013}};
                        player.sendMessage(MessageCreator.createMsgInputText(idInput[player.rankGov][idOptionMenu], 0, "Nhập tên", 0));
                        break;
                    }

                    return;
                case -400:
                    if (player.monster != null && player.monster.map.mapId == player.mapID) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi đang giao hàng.", ""));
                        return;
                    }

                    if (System.currentTimeMillis() - player.timeUseThanHanhPhu >= 0L) {
                        if (player.map.isMapChienTruongMoba()) {
                            return;
                        }

                        String nameKiller = (String) player.listKiller.get(idOptionMenu);
                        Char px = CharManager.instance.getCharByCharName(nameKiller);
                        if (px != null) {
                            if (player.inCountry != player.myCountry) {
                                return;
                            }

                            if (px.map.isMapTrain()) {
                                if (px.inCountry != player.myCountry) {
                                    ((Vector) listKillPerson.get(player.myCountry)).remove(px.charname);
                                    player.sendMessage(MessageCreator.createServerAlertAutoOffMessage(px.charname + " đã rời khỏi vùng của bạn"));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (!px.map.isMapOffline) {
                                    if (!px.map.isMapTrain()) {
                                        return;
                                    }

                                    if (px.mapID != 225 && px.mapID != 226) {
                                        if (!getTown[px.inCountry]
                                                && !getTown[px.myCountry]
                                                && !getTown[player.inCountry]
                                                && !getTown[player.myCountry]
                                                && player.lvDetail.lv >= 50
                                                || px.mapID != idMapTown && player.mapID != idMapTown) {
                                            if (((Vector) idMapMONSTER.get(px.myCountry)).contains(px.map.mapId)) {
                                                return;
                                            }

                                            if (px.map.nRegion > 0) {
                                                map.playerExit(player);
                                                player.region = px.region;
                                            }

                                            map.move2Map(player, px.x / 16, px.y / 16, px.mapID, px.inCountry);
                                            player.listKiller.removeAllElements();
                                            break;
                                        }

                                        return;
                                    }

                                    player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi đang dự Lễ kết đôi.", ""));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi đang trong cửa hàng mua bán.", ""));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                return;
                            }

                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển đến người chơi trong lúc này.", ""));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            return;
                        }

                        ((Vector) listKillPerson.get(player.myCountry)).remove(nameKiller);
                        player.sendMessage(MessageCreator.createServerAlertAutoOffMessage(nameKiller + " đã rời khỏi vùng của bạn"));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        return;
                    }

                    player.sendMessage(
                            MessageCreator.createServerAlertMessage(
                                    "Chỉ có thể sử dụng vật phẩm này sau " + (player.timeUseThanHanhPhu - System.currentTimeMillis()) / 1000L + "s", ""
                            )
                    );
                    return;
                case -67:
                    if (Char.isSuKienTet2017() && idMenu == 0) {
                        if (idOptionMenu != 0) {
                            if (idOptionMenu != 1) {
                                if (idOptionMenu == 2) {
                                    if (player.potions[8] <= 0) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không có bình tăng tốc vàng"));
                                        return;
                                    }

                                    if (!doTuoiNuocCayLixi()) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể tưới trong thời gian này"));
                                    } else {
                                        player.potions[8]--;
                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                        player.sendMessage(
                                                MessageCreator.createMsgChat(player.id, "Máy chủ sẽ phát Lì xì vào " + hourGiveLixi + "h " + minuteGiveLixi + " p")
                                        );
                                        Database.instance.saveOrtherLog("", player.charname, "tuoi cay lixi", "tuoilixi");
                                    }
                                }
                            } else if (!TeamServer.isServerLienDau()) {
                                player.sendMessage(MessageCreator.createMsgInputText(-32051, 0, "Nhập code", 0));
                            }
                        } else {
                            if (!checkCanReceiveLixi()) {
                                player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể nhận lì xì."));
                                return;
                            }

                            map.doGenCapChaInput(player);
                        }
                    }
                    break;
                case -66:
                    if (Char.isSuKienTet2017()) {
                        map.doChamNuoc(player, idNpc, idMenu, idOptionMenu);
                    }
                    break;
                case -65:
                    if (Char.isSuKienTet2017()) {
                        map.doChamNuoc(player, idNpc, idMenu, idOptionMenu);
                    }

                    // Vo hieu hoa 25/09/2026 (yeu cau nguoi dung): day la duong goi tin 66
                    // (luong map, xem real/Map.doDotDen -> MapXuLyLenh.doDotDen) cho NPC Hang
                    // Nga - ban CU tu 2016, gia 4 luong/5tr xu KHONG khop menu that nguoi choi
                    // thay ("Tha den 20L+1 nen"/"10tr xu+1 nen", RequestNPCInfoHandler case -65),
                    // van con loi "100.000 exp cong thang vo dieu kien" da duoc ghi ro la sua o
                    // ban Trung Thu 2026, va KHONG goi data.DatabaseCanMocThaDen.tangCaNhan() nen
                    // se lam sai so dem "Bang xep hang" moi (xem MapSuKien.doMenuTrungThu2016).
                    // Duong SONG duoc goi tin -30 (lenh rieng, luong phien) trong
                    // RealController.doDotDen() moi la ban dang chay that (khop dung gia menu,
                    // co tangCaNhan()). Giu lai than ham cu (Map.doDotDen/MapXuLyLenh.doDotDen)
                    // de khong dong cham qua sau vao dispatch legacy, chi cat noi goi o day.
                    break;
                case -64:
                    if (player.charCopy != null
                            && player.charCopy.map.equals(player.map)
                            && player.charCopy.inCountry == player.inCountry
                            && player.charCopy.region == player.region
                            && inRangeActor(player, player.charCopy, 50)
                            && ((CharCopy) player.charCopy).owner.charDBID == player.charDBID) {
                        player.charCopy.setFollow();
                    }
                    break;
                case -51:
                    if (player.monster != null) {
                        if (player.map.mapIDLoadMap != 118) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng tại vị trí này", ""));
                            return;
                        }

                        if (player.x / 16 >= 51 && player.x / 16 <= 59 && player.y / 16 >= 16 && player.y / 16 <= 24) {
                            if (player.monster != null && player.monster.isFinish()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng", ""));
                                player.monster.actorDie();
                                player.monster = null;
                                player.typeTieu = -1;
                                return;
                            }

                            if (!player.monster.map.equals(player.map)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng do khác map", ""));
                                return;
                            }

                            if (!inRangeActor(player, player.monster, 120)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng do quá xa", ""));
                                return;
                            }

                            player.monster.isFinish = true;
                            player.doResponseTieu(player, false, player.typeTieu, player.lvDetail.lv);
                            player.typeTieu = -1;
                            Char.isSuKienHe2017();
                            break;
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng tại vị trí này", ""));
                        return;
                    }

                    return;
                case -50:
                    if (idMenu != 0) {
                        if (idMenu != 10) {
                            if (idMenu != 11) {
                                if (idMenu == 12) {
                                    MatchLoiDai match = (MatchLoiDai) MapLoiDai.listMatch.get(idOptionMenu);
                                    player.idRegionLoidai = match.idRegion;
                                    map.move2Map(player, 6, 10, idMapLoiDai, 0);
                                }
                            } else {
                                String[] dh = player.getAllNamDanhHieu();
                                if (dh.length <= 1) {
                                    player.doOffDanhHieu();
                                } else if (idOptionMenu != dh.length - 1) {
                                    player.doSelectDanhHieu(idOptionMenu);
                                } else {
                                    player.doOffDanhHieu();
                                }
                            }
                        } else if (idOptionMenu != 0) {
                            if (idOptionMenu != 1) {
                                if (idOptionMenu != 2) {
                                    if (idOptionMenu != 3) {
                                        if (idOptionMenu == 4) {
                                            if (MapLoiDai.listMatch.size() == 0) {
                                                player.sendMessage(MessageCreator.createMsgChat(player.id, "Các trận sàn đấu chưa bắt đầu"));
                                                return;
                                            }

                                            String[] ld = new String[MapLoiDai.listMatch.size()];

                                            for (int ixxxx = 0; ixxxx < MapLoiDai.listMatch.size(); ixxxx++) {
                                                ld[ixxxx] = ((MatchLoiDai) MapLoiDai.listMatch.get(ixxxx)).getName();
                                            }

                                            player.sendMessage(MessageCreator.createMsgMenu(ld, idNpc, 12));
                                        } else if (idOptionMenu == 5) {
                                            map.showMenuHoTro(player);
                                        }
                                    } else {
                                        if (player.nhomThidau == -1) {
                                            player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể vào sảnh chờ khi chưa đăng ký"));
                                            return;
                                        }

                                        int index = r.nextInt(MapLoiDai.pos_sanh_cho.length / 2);
                                        map.move2Map(player, MapLoiDai.pos_sanh_cho[index * 2], MapLoiDai.pos_sanh_cho[index * 2 + 1], idMapChoLoiDai, 0);
                                    }
                                } else {
                                    if (!MapLoiDai.isRunLoiDai()) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể đăng ký khi sàn đấu chưa mở."));
                                        return;
                                    }

                                    if (player.lvDetail.lv < 40) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Hãy chăm chỉ tập luyện đạt đến cấp độ 40 rồi đến gặp ta nhé."));
                                        return;
                                    }

                                    int level = player.lvDetail.lv;
                                    int index = level / 10 - 4;
                                    if (player.nhomThidau > -1) {
                                        index = player.nhomThidau;
                                    }

                                    if (((Hashtable) ALL_CHAR_LOI_DAI.get(index)).get(player.charDBID) != null) {
                                        if (player.nhomThidau == -1) {
                                            player.nhomThidau = (byte) index;
                                        }

                                        player.sendMessage(
                                                MessageCreator.createMsgChat(
                                                        player.id,
                                                        "Đã đăng ký tham gia rồi thì hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé"
                                                )
                                        );
                                        return;
                                    }

                                    player.nhomThidau = (byte) index;
                                    CharThiDau c = new CharThiDau(player.charname, player.lvDetail.lv, player.charDBID);
                                    c.nhom = index;
                                    ((Hashtable) ALL_CHAR_LOI_DAI.get(index)).put(player.charDBID, c);
                                    player.sendMessage(
                                            MessageCreator.createMsgChat(
                                                    player.id,
                                                    "Đăng ký thành công.Hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé."
                                            )
                                    );
                                    Database.instance.doAddCharThachDau(c, index, true);
                                    Database.instance.saveOrtherLog("", player.charname, "dang ky loi dai", "regloidai");
                                }
                            } else {
                                String[] dh = player.getAllNamDanhHieu();
                                player.sendMessage(MessageCreator.createMsgMenu(dh, idNpc, 11));
                            }
                        } else {
                            map.doXuongThuCuoi(player, 0);
                        }
                    } else if (idOptionMenu != 0) {
                        if (idOptionMenu != 1) {
                            if (idOptionMenu != 2) {
                                if (idOptionMenu != 3) {
                                    if (idOptionMenu != 4) {
                                        if (idOptionMenu == 5) {
                                            if (MapLoiDai.listMatch.size() == 0) {
                                                player.sendMessage(MessageCreator.createMsgChat(player.id, "Các trận sàn đấu chưa bắt đầu"));
                                                return;
                                            }

                                            String[] ld = new String[MapLoiDai.listMatch.size()];

                                            for (int ixxxx = 0; ixxxx < MapLoiDai.listMatch.size(); ixxxx++) {
                                                ld[ixxxx] = ((MatchLoiDai) MapLoiDai.listMatch.get(ixxxx)).getName();
                                            }

                                            player.sendMessage(MessageCreator.createMsgMenu(ld, idNpc, 12));
                                        } else if (idOptionMenu == 6) {
                                            map.showMenuHoTro(player);
                                        }
                                    } else {
                                        if (player.nhomThidau == -1) {
                                            player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể vào sảnh chờ khi chưa đăng ký"));
                                            return;
                                        }

                                        int indexx = r.nextInt(MapLoiDai.pos_sanh_cho.length / 2);
                                        map.move2Map(player, MapLoiDai.pos_sanh_cho[indexx * 2], MapLoiDai.pos_sanh_cho[indexx * 2 + 1], idMapChoLoiDai, 0);
                                    }
                                } else {
                                    if (!MapLoiDai.isRunLoiDai()) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể đăng ký khi sàn đấu chưa mở."));
                                        return;
                                    }

                                    if (player.lvDetail.lv < 40) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Hãy chăm chỉ tập luyện đạt đến cấp độ 40 rồi đến gặp ta nhé."));
                                        return;
                                    }

                                    int levelx = player.lvDetail.lv;
                                    int indexx = levelx / 10 - 4;
                                    if (player.nhomThidau > -1) {
                                        indexx = player.nhomThidau;
                                    }

                                    if (((Hashtable) ALL_CHAR_LOI_DAI.get(indexx)).get(player.charDBID) != null) {
                                        if (player.nhomThidau == -1) {
                                            player.nhomThidau = (byte) indexx;
                                        }

                                        player.sendMessage(
                                                MessageCreator.createMsgChat(
                                                        player.id,
                                                        "Đã đăng ký tham gia rồi thì hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé"
                                                )
                                        );
                                        return;
                                    }

                                    player.nhomThidau = (byte) indexx;
                                    CharThiDau c = new CharThiDau(player.charname, player.lvDetail.lv, player.charDBID);
                                    c.nhom = indexx;
                                    ((Hashtable) ALL_CHAR_LOI_DAI.get(indexx)).put(player.charDBID, c);
                                    player.sendMessage(
                                            MessageCreator.createMsgChat(
                                                    player.id,
                                                    "Đăng ký thành công.Hãy chăm chỉ tập luyện để có thể giành được chức vô địch làm rạng danh cho dòng Pokémon của mình nhé."
                                            )
                                    );
                                    Database.instance.doAddCharThachDau(c, indexx, true);
                                    Database.instance.saveOrtherLog("", player.charname, "dang ky loi dai", "regloidai");
                                }
                            } else {
                                String[] dh = player.getAllNamDanhHieu();
                                player.sendMessage(MessageCreator.createMsgMenu(dh, idNpc, 11));
                            }
                        } else {
                            map.doXuongThuCuoi(player, 0);
                        }
                    } else if (player.map.mapIDLoadMap != 0 || player.map.mapIDLoadMap != 70) {
                        MapMenuNpc.doMenuNpcNhanTieu(map, player, idNpc, idMenu, idOptionMenu);
                    }
                    break;
                case -49:
                    // RealController da xu ly -49 (SuKienNpc); giu nhanh nay cho goi tu noi khac toi Map.doMenu.
                    SuKienNpc.xuLy(map, player, idNpc, idMenu, idOptionMenu);
                    return;
                case -101:
                case -100: {
                    // Hoa Tieu moi cho map 26/113 (lo #4 loi.xlsx) - tu kiem tra map goc
                    // rieng, khong dung mang idMapLoad/xyNPC dung chung voi -38..-48.
                    int homeMap = (idNpc == -100) ? 26 : 113;
                    if (player.map.mapIDLoadMap != homeMap) {
                        return;
                    }
                    if (player.monster != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang giao hàng", ""));
                        return;
                    }
                    map.doHoaTieu(player, idNpc, idMenu, idOptionMenu);
                    break;
                }
                case -48:
                case -47:
                case -46:
                case -45:
                case -44:
                case -43:
                case -42:
                case -41:
                case -40:
                case -39:
                case -38:
                    int idmapload = RequestNPCInfoHandler.idMapLoad[abs(idNpc) - 38];
                    if (player.map.mapIDLoadMap != idmapload) {
                        if (player.charname.equals("kwnjpt")) {
                            System.out.println(player.charname + " >> " + map.mapId);
                        }

                        return;
                    }

                    if (abs(player.x / 16 - RequestNPCInfoHandler.xyNPC[abs(idNpc) - 38][0]) <= rangeNpc
                            && abs(player.y / 16 - RequestNPCInfoHandler.xyNPC[abs(idNpc) - 38][1]) <= rangeNpc) {
                        if (player.monster != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang giao hàng", ""));
                            return;
                        }

                        map.doHoaTieu(player, idNpc, idMenu, idOptionMenu);
                        break;
                    }

                    return;
                case -37:
                    map.doMenuTown(player, idNpc, idMenu, idOptionMenu);
                    break;
                case -34:
                    if (map.nRegion <= 0)
                    {
                        break;
                    }
                    if (com.htbb.pokemon.PhongRieng.laMapRieng(map.mapId))
                    {
                        player.sendMessage(MessageCreator.createServerAlertMessage(com.htbb.pokemon.PhongRieng.LY_DO_DOI_KHU, ""));
                        return;
                    }
                    if (idOptionMenu < 0 || idOptionMenu >= map.nRegion)
                    {
                        return;
                    }
                   long now = System.currentTimeMillis();
                      long last = player.timeNextRegion;
                      final long COOLDOWN = 20_000L;

                      if (last != -1 && now - last < COOLDOWN) 
                      {
                          long remainingMillis = COOLDOWN - (now - last);
                          int remainingSeconds = (int) Math.ceil(remainingMillis / 1000.0);
                          player.sendMessage(MessageCreator.createServerAlertMessage("Không thể chuyển khu quá nhanh, Vui lòng chờ " + remainingSeconds + " giây nữa", ""));
                          return;
                      }
                    
                    player.timeNextRegion = now;
                    if (player.region != idOptionMenu) 
                    {
                        if (player.monster != null) 
                        {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể chuyển khu khi đang giao hàng", ""));
                            return;
                        }

                        if (map.isMapChienTruongMoba() || abs(player.x / 16 - map.pRegion[0]) <= rangeNpc && abs(player.y / 16 - map.pRegion[1]) <= rangeNpc) 
                        {
                            map.playerExit(player);
                            player.region = idOptionMenu;
                            map.move2Map(player, player.x / 16, player.y / 16, player.mapID, player.inCountry);
                            break;
                        }

                        return;
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đang ở tại khu này", ""));
                    return;
                case -32:
                    if (idMenu == 0) {
                        if (player.monster != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể chuyển vùng khi đang giao hàng", ""));
                            return;
                        }

                        if (player.myCountry < 2) {
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    "Chỉ Pokémon vùng Hoenn mới chọn vùng ở đây. Muốn đổi vùng hãy gặp Tổng quản (mục Chuyển vùng).", ""));
                            return;
                        }
                        if (idOptionMenu < 0 || idOptionMenu > 1) {
                            return;
                        }

                        if (player.idClan > -1 && !player.isMasterClan()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể chọn vùng cho đội.", ""));
                            return;
                        }

                        if (player.monster != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể chuyển vùng khi đang giao hàng", ""));
                            return;
                        }

                        byte oldCountry = player.myCountry;
                        if (checkFullNation(idOptionMenu, 1, player.lvDetail.lv)) {
                            player.sendMessage(
                                    MessageCreator.createServerAlertMessage(Map.nameCountry[idOptionMenu] + " lãnh đã có quá nhiều người. Vui lòng chọn vùng khác.", "")
                            );
                            return;
                        }

                        map.playerExit(player);
                        player.myCountry = (byte) idOptionMenu;
                        player.inCountry = player.myCountry;
                        allUser[player.myCountry]++;
                        player.sendMessage(MessageCreator.createMessageLocation(real.LangKhoiDau.oCong(player, player.myCountry)));
                        int mapID = mapstart[player.myCountry][r.nextInt(2)];
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        map.move2Map(player, 16 + Database.r.nextInt() % 5, 24 + Database.r.nextInt(20), mapID, player.myCountry);
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã gia nhập " + Map.nameCountry[player.myCountry], ""));
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        player.charname,
                                        player.charname + "_ từ" + Map.nameCountry[oldCountry] + " chuyển sang " + Map.nameCountry[player.myCountry],
                                        "csnation"
                                );
                        if (player.idClan > -1) {
                            NewClan clan = NewClan.getClan(player.idClan);
                            clan.town = 0;
                            idClanTown[2] = -1;
                            clan.country = player.myCountry;
                            clan.updateNewClandata2DB();
                            Vector<CharClan> mem = clan.getAllMember();
                            allUser[player.myCountry] = mem.size();

                            for (int ixxxx = 0; ixxxx < mem.size(); ixxxx++) {
                                try {
                                    CharClan c = (CharClan) mem.get(ixxxx);
                                    Char p = CharManager.instance.getCharByCharName(c.charname);
                                    if (p != null) {
                                        p.map.playerExit(p);
                                        p.myCountry = player.myCountry;
                                        p.sendMessage(MessageCreator.createMessageLocation(real.LangKhoiDau.oCong(p, p.myCountry)));
                                        p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                                        map.move2Map(p, 16 + Database.r.nextInt() % 5, 24 + Database.r.nextInt(20), mapID, p.myCountry);
                                        p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã gia nhập " + Map.nameCountry[p.myCountry], ""));
                                        Database.instance
                                                .saveOrtherLog(
                                                        "",
                                                        player.charname,
                                                        player.charname + "_ từ" + Map.nameCountry[oldCountry] + " chuyển sang " + Map.nameCountry[player.myCountry],
                                                        "csnationclan"
                                                );
                                    }
                                } catch (Exception var43) {
                                }
                            }
                        }
                    }
                    break;
                case -31:
                    if (!isMapLang(player.map) && player.map.mapIDLoadMap != 201) { // ca Cianwood (80): bot dau truong dung o ca 3 thi tran
                        return;
                    }

                    // Su kien Vu Lan: muc "Su kien Vu Lan" noi CUOI menu goc (idMenu 0),
                    // vi tri that su ghi trong player.idxSuKienVuLanDauTruong luc mo menu
                    // (RequestNPCInfoHandler case -31). Phai chan O DAY, TRUOC khi remap
                    // VI_TRI_MENU_DAU_TRUONG ben duoi - bang do chi biet cac vi tri co san,
                    // vi tri moi nay se bi no return im lang neu khong chan truoc.
                    // 0.9.7 Dau xep hang (noi cuoi menu, truoc Vu Lan): chan truoc khi remap.
                    if (idMenu == 0 && player.idxXepHangDauTruong >= 0 && idOptionMenu == player.idxXepHangDauTruong) {
                        XepHangPvpGame.dangKy(player);
                        return;
                    }
                    if (idMenu == 0 && player.idxDauTruongBaoBoi >= 0 && idOptionMenu == player.idxDauTruongBaoBoi) {
                        DauTruongGame.moTuNpc(player); // Dau truong Bao Boi 3v3 (goi -109)
                        return;
                    }
                    if (idMenu == 0 && SuKien.bat(SuKien.VU_LAN) && player.idxSuKienVuLanDauTruong >= 0
                            && idOptionMenu == player.idxSuKienVuLanDauTruong) {
                        MapSuKien.doMenuDauTruongVuLan(map, player, idNpc, MapSuKien.MENU_VU_LAN_DAU_TRUONG_GOC, -1);
                        return;
                    }
                    // Menu con rieng cua Vu Lan (doi bo hoa / doi nguyen lieu, ke ca menu con
                    // chon loai NLSC voi tag dong idOptionMenu+base): tat ca nam trong dai
                    // [MENU_VU_LAN_DAU_TRUONG_GOC..MENU_VU_LAN_MAX], khong dung idMenu 0/10/11/12
                    // hien co cua NPC nay nen khong dung cham gi toi logic dau truong/nui chau bau.
                    if (idMenu >= MapSuKien.MENU_VU_LAN_DAU_TRUONG_GOC && idMenu <= MapSuKien.MENU_VU_LAN_MAX) {
                        MapSuKien.doMenuDauTruongVuLan(map, player, idNpc, idMenu, idOptionMenu);
                        return;
                    }

                    // Menu goc (idMenu 0) da bo 4 muc: doi vi tri hien thi -> vi tri goc.
                    // Cac menu con (idMenu 10/11/12) khong bi anh huong nen khong doi.
                    if (idMenu == 0) {
                        if (idOptionMenu < 0 || idOptionMenu >= VI_TRI_MENU_DAU_TRUONG.length) {
                            return;
                        }
                        idOptionMenu = VI_TRI_MENU_DAU_TRUONG[idOptionMenu];
                    }

                    if (idMenu != 0) {
                        if (idMenu != 12) {
                            MapMenuNpc.doMenuEventMini(map, player, idNpc, idMenu, idOptionMenu);
                        } else {
                            if (player.lvDetail.lv < 40) {
                                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hãy đi tập luyện đến cấp độ 40 rồi quay lại gặp ta."));
                                return;
                            }

                            if (idOptionMenu != 0) {
                                if (idOptionMenu != 1) {
                                    if (idOptionMenu == 2) {
                                        if (!isMapLang(player.map)) {
                                            return;
                                        }

                                        if (player.getTimeNuiChauBau() < 60L) {
                                            player.sendMessage(MessageCreator.createMsgChat(player.id, "Thời gian tìm châu báu của bạn còn quá ít."));
                                            return;
                                        }

                                        try {
                                            player.map.move2Map(player, 25, 11, idMapChanNui, player.inCountry);
                                        } catch (Exception var40) {
                                            var40.printStackTrace();
                                        }
                                    }
                                    break;
                                }

                                if (!isMapLang(player.map)) {
                                    return;
                                }

                                if (player.potions[77] != 0 && player.potions[105] != 0 && player.potions[117] != 0) {
                                    CharInfo c = new CharInfo();
                                    c.timeNuiChaubau = System.currentTimeMillis() + 900000L;
                                    c.isLockGift = false;
                                    c.fail = 0;
                                    MapChauBau.all_char_nui_kho_bau.put(player.charname, c);
                                    player.potions[77]--;
                                    player.potions[105]--;
                                    player.potions[117]--;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));

                                    try {
                                        player.map.move2Map(player, 25, 11, idMapChanNui, player.inCountry);
                                    } catch (Exception var41) {
                                        var41.printStackTrace();
                                    }

                                    Database.instance.saveOrtherLog("", player.charname, "Dung manh ban do de vao nui chau bau", "manhbando");
                                    Database.instance.saveLogThongKeChiTieu("DI NUI CHAU BAU", 0, 1, 0L, "chan nui");
                                    break;
                                }

                                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hãy đi tìm đủ 3 mảnh bản đồ rồi đến gặp ta."));
                                return;
                            }

                            if (!isMapLang(player.map)) {
                                return;
                            }

                            if (player.listGemitemLock[GemTemplate.BAN_DO_KHO_BAU] == 0) {
                                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hãy đi tìm bản đồ rồi đến gặp ta."));
                                return;
                            }

                            CharInfo c = new CharInfo();
                            c.isLockGift = true;
                            c.fail = 0;
                            c.timeNuiChaubau = System.currentTimeMillis() + 900000L;
                            MapChauBau.all_char_nui_kho_bau.put(player.charname, c);
                            player.delGem(GemTemplate.BAN_DO_KHO_BAU, 1, true);
                            player.sendMessage(MessageCreator.createCharGemItem(player));

                            try {
                                player.map.move2Map(player, 25, 11, idMapChanNui, player.inCountry);
                            } catch (Exception var42) {
                                var42.printStackTrace();
                            }

                            Database.instance.saveOrtherLog("", player.charname, "Dung ban do de vao nui chau bau", "bando");
                            Database.instance.saveLogThongKeChiTieu("DI NUI CHAU BAU lock", 0, 1, 0L, "chan nui lock");
                        }
                    } else if (idOptionMenu == 0) {
                        if (player.monster != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tham gia khi đang giao hàng", ""));
                            return;
                        }

                        if (player.idArena <= -1) {
                            Calendar cl = Calendar.getInstance();
                            int iHour = cl.get(11);
                            if (iHour >= 20 && iHour < 21) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể gửi lời thách đấu trong thời gian từ 20h-21h hàng ngày", ""));
                                return;
                            }

                            if (player.lvDetail.lv < 40) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 40 trở lên mới có thể sử dụng chức năng này.", ""));
                                return;
                            }

                            if (player.idArena > -1) {
                                player.sendMessage(
                                        MessageCreator.createServerAlertMessage("Không thể gửi lời thách đấu khi bạn đã đồng ý tham gia một trận đấu khác.", "")
                                );
                                return;
                            }

                            player.sendMessage(MessageCreator.createMsgInputText(player.id, 5, "Nhập xu cược (" + ThachDauCuoc.moTaMuc(player) + ", phí " + ThachDauCuoc.cauHinh().phiPhanTram + "% bên thắng)", 1));
                        } else {
                            ArenaData arena = ArenaMap.getArenaData(player);
                            if (arena == null) {
                                player.idArena = -1;
                                player.sendMessage(MessageCreator.createServerAlertMessage("Trận đấu bạn muốn tham gia đã kết thúc hoặc không tồn tại.", ""));
                            } else {
                                arena.setTeam(player);
                                map.move2Map(player, 24 + r.nextInt() % 3, 10 + r.nextInt() % 3, 204, player.myCountry);
                            }
                        }
                    } else if (idOptionMenu != 1) {
                        if (idOptionMenu == 2) {
                            if (player.lvDetail.lv < 50) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp 50 trở lên mới được tham gia chiến trường.", ""));
                                return;
                            }

                            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(player.charname);
                            MapChienTruongMoba mapMoba = (MapChienTruongMoba) RealController.mapList.get(40);
                            if (c != null) {
                                if (MapChienTruongMoba.isStart) {
                                    RegionMapMoba rg = mapMoba.getRegionMoba(c.region);
                                    if (!rg.isEnd()) {
                                        mapMoba.doJoinChienTruong(player);
                                    }
                                } else {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chiến trường chưa mở", ""));
                                }
                            } else {
                                if (!MapChienTruongMoba.isDayChienTruong()) {
                                    if (!TeamServer.isServerLienDau()) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng chỉ dành cho máy chủ Giải Liên minh.", ""));
                                    } else {
                                        player.sendMessage(
                                                MessageCreator.createServerAlertMessage(
                                                        !MapChienTruongMoba.isStart
                                                                ? "Bạn chỉ có thể đăng ký trước 21h thứ 4 hoặc chủ nhật hàng tuần."
                                                                : "Không thể đăng ký khi chiến trường đang diễn ra",
                                                        ""
                                                )
                                        );
                                    }

                                    return;
                                }

                                if (player.lvDetail.lv >= 70) {
                                    if (!UtilHTBB.isSunday()) {
                                        player.sendMessage(
                                                MessageCreator.createServerAlertMessage(
                                                        !MapChienTruongMoba.isStart
                                                                ? "Chiến trường hôm nay chỉ dành cho Pokémon có cấp độ dưới 70."
                                                                : "Không thể đăng ký khi chiến trường đang diễn ra",
                                                        ""
                                                )
                                        );
                                        return;
                                    }
                                } else if (!UtilHTBB.isWednesay()) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    !MapChienTruongMoba.isStart
                                                            ? "Chiến trường hôm nay chỉ dành cho Pokémon có cấp độ từ 70 trở lên."
                                                            : "Không thể đăng ký khi chiến trường đang diễn ra",
                                                    ""
                                            )
                                    );
                                    return;
                                }

                                MapChienTruongMoba.doRegisJoinBattle(player);
                            }
                        } else if (idOptionMenu != 3) {
                            if (idOptionMenu != 4) {
                                if (idOptionMenu != 5) {
                                    if (idOptionMenu != 6) {
                                        MapMenuNpc.doMenuEventMini(map, player, idNpc, idMenu, idOptionMenu);
                                    } else {
                                        player.sendMessage(
                                                MessageCreator.createMsgMenu(new String[]{"Sử dụng bản đồ khoá", "Sử dụng mảnh bản đồ", "vào chân núi"}, idNpc, 12)
                                        );
                                    }
                                } else {
                                    if (TeamServer.isServerLienDau()) {
                                        return;
                                    }

                                    if (!UtilHTBB.isMonday() && !UtilHTBB.isThursday() && (UtilHTBB.isWednesay() || UtilHTBB.isSunday()) && UtilHTBB.getHour() < 21) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Chỉ có thể nhận quà đến hết ngày thứ 2 hoặc ngày thứ 5."));
                                        return;
                                    }

                                    int win = Database.instance.getCharTopLiendau(player.charname, 1);
                                    if (win == -1) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể nhận quà."));
                                        return;
                                    }

                                    Database.instance.doUpdateReceiveGiftTopCT(player.charname, 1);
                                    player.addLuong(100);
                                    player.potions[35]++;
                                    int igdem = GemTemplate.ID_MATERIAL_LOW[5][random(GemTemplate.ID_MATERIAL_LOW[5].length)];
                                    player.doAddGemItem(igdem, 6, false);
                                    player.doAddGemItem(GemTemplate.BAN_DO_KHO_BAU, 1, true);
                                    String infox = "Nhận được 100 lượng, 1 vktt 24h và 6 " + gemTemplate[igdem].name;
                                    doCreateVuKhiThoiTrang(player, 1440, ID_VU_KHI_THOI_TRANG[player.charClass]);
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, infox));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.sendMessage(MessageCreator.createCharGemItem(player));
                                    Database.instance.saveOrtherLog("", player.charname, infox, "quatopct");
                                }
                            } else {
                                if (TeamServer.isServerLienDau()) {
                                    return;
                                }

                                if (!UtilHTBB.isTuesday()
                                        && !UtilHTBB.isFriday()
                                        && !UtilHTBB.isSaturday()
                                        && (!UtilHTBB.isWednesay() && !UtilHTBB.isSunday() || UtilHTBB.getHour() >= 21)) {
                                    int win = Database.instance.getCharTopLiendau(player.charname, 0);
                                    if (win == -1) {
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể nhận quà."));
                                        return;
                                    }

                                    Database.instance.doUpdateReceiveGiftTopCT(player.charname, 0);
                                    player.addLuong(100);
                                    player.potions[35]++;
                                    player.doAddGemItem(GemTemplate.BAN_DO_KHO_BAU, 1, true);
                                    int igdem = GemTemplate.ID_MATERIAL_LOW[5][random(GemTemplate.ID_MATERIAL_LOW[5].length)];
                                    player.doAddGemItem(igdem, 3, false);
                                    String infox = "Nhận được 100 lượng,  1 vé giờ vàng 1h và 3 " + gemTemplate[igdem].name;
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, infox));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.sendMessage(MessageCreator.createCharGemItem(player));
                                    Database.instance.saveOrtherLog("", player.charname, infox, "quatopct");
                                    break;
                                }

                                player.sendMessage(MessageCreator.createMsgChat(player.id, "Chỉ có thể nhận quà đến hết ngày thứ 2 hoặc ngày thứ 5."));
                                return;
                            }
                        } else {
                            if (TeamServer.isServerLienDau()) {
                                return;
                            }

                            if (!UtilHTBB.isTuesday()
                                    && !UtilHTBB.isFriday()
                                    && !UtilHTBB.isSaturday()
                                    && (!UtilHTBB.isWednesay() && !UtilHTBB.isSunday() || UtilHTBB.getHour() >= 21)) {
                                int win = Database.instance.getCharWinLiendau(player.charname);
                                if (win < 1) {
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể nhận quà."));
                                    return;
                                }

                                player.addLuongLock(10 * win);
                                player.potions[123] = player.potions[123] + 5 * win;
                                int igdem = GemTemplate.ID_MATERIAL_LOW[5][random(GemTemplate.ID_MATERIAL_LOW[5].length)];
                                player.doAddGemItem(igdem, 3 * win, true);
                                String infox = "Nhận được "
                                        + 10 * win
                                        + " lượng khoá, "
                                        + 5 * win
                                        + " thuốc cường hoá siêu cấp và "
                                        + 3 * win
                                        + " "
                                        + gemTemplate[igdem].name
                                        + " khoá";
                                player.sendMessage(MessageCreator.createMsgChat(player.id, infox));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                Database.instance.saveOrtherLog("", player.charname, infox, "quakhu");
                                Database.instance.doUpdateReceiveGiftCT(player.charname);
                                break;
                            }

                            player.sendMessage(MessageCreator.createMsgChat(player.id, "Chỉ có thể nhận quà đến hết ngày thứ 2 hoặc ngày thứ 5."));
                            return;
                        }
                    } else {
                        if (player.monster != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tham gia khi đang giao hàng", ""));
                            return;
                        }

                        if (TeamServer.isServerHoaLu() && Char.isSuKienDuaTopHoaLu()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng bảo trì trong thời gian đua top", ""));
                            return;
                        }

                        if (player.lvDetail.lv < 50) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Mê cung dành cho Pokémon từ cấp 50 trở lên.", ""));
                            return;
                        }
                        item = player.getLenhBaiTranYeu();
                        if (item == null && !player.canJoinTranYeuTran) {
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    "Cần có Vé Mê cung đúng cấp (" + (player.lvDetail.lv >= 70 ? "7x" : player.lvDetail.lv >= 60 ? "6x" : "5x") + ") để vào Mê cung.", ""));
                            return;
                        }

                        Map mapTran = null;
                        if (player.lvDetail.lv >= 50 && player.lvDetail.lv < 60) {
                            mapTran = (Map) RealController.mapList.get(208);
                        } else if (player.lvDetail.lv >= 60 && player.lvDetail.lv < 70) {
                            mapTran = (Map) RealController.mapList.get(209);
                        } else if (player.lvDetail.lv >= 70) {
                            mapTran = (Map) RealController.mapList.get(210);
                        }

                        if (mapTran != null) {
                            RegionVantienTran rg = mapTran.getRegionJoinMapVanTienTran(player);
                            if (rg == null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể vào Mê cung lúc này", ""));
                            } else {
                                map.playerExit(player);
                                player.region = rg.id;
                                map.move2Map(player, 22, 23, mapTran.mapId, 0);
                                if (!player.canJoinTranYeuTran) {
                                    player.canJoinTranYeuTran = true;
                                    player.iItems.remove(item);
                                    player.removeIDItem(item.id);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    Database.instance.saveOrtherLog("", player.charname, item.getInfoSave(), "vaotranyeu");
                                }
                            }
                        }
                    }
                    break;
                case -29:
                    if (!isMapThanh(player.map) && !map.isMapLienDau()) {
                        return;
                    }

                    if (abs(player.x / 16 - 40) <= rangeNpc && abs(player.y / 16 - 15) <= rangeNpc) {
                        if (idMenu == 0) {
                            map.playerGiveCard(player, 0);
                        }
                        break;
                    }

                    return;
                case -28:
                    if (player.map.mapIDLoadMap != 225 && player.map.mapIDLoadMap != 226) {
                        return;
                    }

                    Wedding wdd = (Wedding) allWedding.get(player.idWedding);
                    if (wdd != null) {
                        if (wdd.idP1 != player.charDBID && wdd.idP2 != player.charDBID) {
                            player.sendMessage(MessageCreator.createMsgChat(player.id, "Chỉ cặp đôi chủ lễ mới có thể gọi boss"));
                            return;
                        }

                        map.doAddMonster(player);
                    }
                    break;
                case -27:
                    if (player.map.mapIDLoadMap != 201) {
                        return;
                    }

                    if (abs(player.x / 16 - 35) <= rangeNpc && abs(player.y / 16 - 10) <= rangeNpc) {
                        if (idMenu != 0) {
                            if (idMenu != 1) {
                                break;
                            }

                            int[] price = new int[]{0, 500, 1000};
                            if (idOptionMenu < 0 || idOptionMenu >= price.length) {
                                return;
                            }
                            // Le ket doi: nhom 2 nguoi (dong y), duc + cai, chua ket doi, cap 40, CHUNG NHOM TRUNG
                            // (com.htbb.pokemon.KetDoi) - xet LUC DANG KY, cap da ket doi khong xet lai.
                            final String lyDoKetDoi = KetDoiGame.lyDoTuChoi(player);
                            if (lyDoKetDoi != null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage(lyDoKetDoi, ""));
                                return;
                            }
                            int priceXu = 20000000;
                            if (player.getxu() < (long) priceXu) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có " + priceXu + " xu để tổ chức Lễ kết đôi", ""));
                                return;
                            }

                            if (player.getLuong() < price[idOptionMenu]) {
                                player.sendMessage(
                                        MessageCreator.createServerAlertMessage("Bạn phải có " + price[idOptionMenu] + " lượng để tổ chức Lễ kết đôi", "")
                                );
                                return;
                            }

                            final String nhomTrung = KetDoiGame.moTaNhomTrung(
                                    (Char) player.party.userParty.get(0), (Char) player.party.userParty.get(1));
                            Database.instance.saveLogThongKeChiTieu("Ket Doi", 0, 1, (long) price[idOptionMenu], "luong");
                            player.subXu((long) priceXu, true, "map 36");
                            player.subLuong((long) price[idOptionMenu]);
                            ((Char) player.party.userParty.get(0)).married = 1;
                            ((Char) player.party.userParty.get(1)).married = 1;
                            Wedding wd = new Wedding();
                            wd.idP1 = ((Char) player.party.userParty.get(0)).charDBID;
                            wd.idP2 = ((Char) player.party.userParty.get(1)).charDBID;
                            ((Char) player.party.userParty.get(0)).idWedding = player.charDBID;
                            ((Char) player.party.userParty.get(1)).idWedding = player.charDBID;
                            int[] weddingCard = new int[]{10, 15, 25};
                            ((Char) player.party.userParty.get(0)).potions[92] = weddingCard[idOptionMenu];
                            ((Char) player.party.userParty.get(1)).potions[92] = weddingCard[idOptionMenu];
                            ((Char) player.party.userParty.get(0)).nameHusband_wife = ((Char) player.party.userParty.get(1)).charname;
                            ((Char) player.party.userParty.get(1)).nameHusband_wife = ((Char) player.party.userParty.get(0)).charname;
                            wd.idParty = player.idWedding;
                            wd.action = 0;
                            wd.typeParty = (byte) idOptionMenu;
                            wd.dayParty = Char.getDayOpen(0L);
                            allWedding.put(wd.idParty, wd);
                            wd.listMember.put(((Char) player.party.userParty.get(0)).charDBID, ((Char) player.party.userParty.get(0)).userID);
                            wd.listMember.put(((Char) player.party.userParty.get(1)).charDBID, ((Char) player.party.userParty.get(1)).userID);
                            Database.instance
                                    .saveWedding((Char) player.party.userParty.get(0), (Char) player.party.userParty.get(1), wd.time, wd.dayParty, wd.typeParty);
                            ((Char) player.party.userParty.get(0)).sendMessage(MessageCreator.createCharInventoryMessage((Char) player.party.userParty.get(0), 0));
                            ((Char) player.party.userParty.get(1)).sendMessage(MessageCreator.createCharInventoryMessage((Char) player.party.userParty.get(1), 0));
                            final String chucMung = "Chúc mừng! " + nhomTrung + " Lễ kết đôi diễn ra ngày " + wd.dayParty
                                    + ", hãy khai lễ trong hôm nay.";
                            ((Char) player.party.userParty.get(0)).sendMessage(MessageCreator.createServerAlertMessage(chucMung, ""));
                            ((Char) player.party.userParty.get(1)).sendMessage(MessageCreator.createServerAlertMessage(chucMung, ""));
                            Database.instance
                                    .saveOrtherLog(
                                            "",
                                            player.charname,
                                            ((Char) player.party.userParty.get(0)).charname
                                            + "_"
                                            + wd.idP1
                                            + "_"
                                            + ((Char) player.party.userParty.get(1)).charname
                                            + "_"
                                            + wd.idP2
                                            + "_loại: "
                                            + wd.typeParty
                                            + "_giờ: "
                                            + wd.time
                                            + "_ngày: "
                                            + wd.dayParty,
                                            "regWedding"
                                    );
                            MapQuaTang.doAddGiftWedding(map, (Char) player.party.userParty.get(0), (Char) player.party.userParty.get(1), idOptionMenu);
                            sendAllCharServer(
                                    17,
                                    MessageCreator.createServerAlertAutoOffMessage(
                                            ((Char) player.party.userParty.get(0)).charname + " và " + ((Char) player.party.userParty.get(1)).charname
                                            + " đã đăng ký Lễ kết đôi"
                                    )
                            );
                            System.out.println("DANG KY LE KET DOI THANH CONG");
                            break;
                        }

                        if (idOptionMenu == 0) {
                            // Xet truoc (chua tru tien) de bao ly do ngay, vd khong chung nhom trung.
                            final String lyDo = KetDoiGame.lyDoTuChoi(player);
                            if (lyDo != null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage(lyDo, ""));
                                return;
                            }
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Đơn giản", "Náo nhiệt (500 lượng)", "Hoành tráng (1000 lượng)"}, idNpc, 1));
                            break;
                        }

                        if (idOptionMenu != 1) {
                            if (idOptionMenu == 2) {
                                Wedding wd = (Wedding) allWedding.get(player.idWedding);
                                if (wd == null || wd.action != 0) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa đăng ký Lễ kết đôi nào để khai lễ.", ""));
                                    return;
                                }
                                if (wd != null && wd.action == 0) {
                                    if (wd.dayParty.equals(Char.getDayOpen(0L))) {
                                        if (wd.idP1 == player.charDBID || wd.idP2 == player.charDBID) {
                                            map.startWedding(player.idWedding);
                                        } else {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ cặp đôi tổ chức mới khai lễ được.", ""));
                                        }
                                    } else {
                                        wd.action = 1;
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Lễ kết đôi của bạn đã bị huỷ vì quá ngày khai lễ", ""));
                                        Database.instance.updateStatusWedding(wd.idParty, 1);
                                        Database.instance.saveOrtherLog("", player.charname, "Huy le ket doi " + wd.dayParty, "delWed");
                                        allWedding.remove(wd.idParty);
                                    }
                                }
                                break;
                            }

                            if (idOptionMenu != 3) {
                                Wedding wd = (Wedding) allWedding.get(player.idWedding);
                                if (wd != null && wd.dayParty.equals(Char.getDayOpen(0L))) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể chọn chức năng này vào ngày mai.", ""));
                                    return;
                                }

                                if (player.getLuong() < 300) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có 300 lượng mới có thể đơn phương huỷ kết đôi.", ""));
                                    return;
                                }

                                player.sendMessage(MessageCreator.createMsgPopUp(player.id, 9, "Bạn thật sự muốn huỷ kết đôi với " + player.nameHusband_wife + " ?"));
                                break;
                            }

                            if (player.gender == 2) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Pokémon đực trong cặp đôi đứng ra huỷ kết đôi, hoặc chọn đơn phương huỷ kết đôi.", ""));
                                return;
                            }

                            if (player.party != null && (player.party == null || player.party.checkConditionLydi())) {
                                if (player.nameHusband_wife.equals("")) {
                                    return;
                                }

                                Wedding wdx = (Wedding) allWedding.get(player.idWedding);
                                if (wdx != null && wdx.dayParty.equals(Char.getDayOpen(0L))) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể chọn chức năng này vào ngày mai.", ""));
                                    return;
                                }

                                Char p = CharManager.instance.getCharByCharName(player.nameHusband_wife);
                                if (p == null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn kết đôi của bạn hiện chưa online", ""));
                                    return;
                                }

                                if (player.getLuong() < 150) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có 150 lượng mới có thể huỷ kết đôi.", ""));
                                    return;
                                }

                                if (p.getLuong() < 150) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn kết đôi của bạn phải có 150 lượng mới có thể huỷ kết đôi.", ""));
                                    return;
                                }

                                player.sendMessage(MessageCreator.createMsgPopUp(player.id, 8, "Bạn thật sự muốn huỷ kết đôi với " + player.nameHusband_wife + " ?"));
                                break;
                            }

                            player.sendMessage(MessageCreator.createServerAlertMessage("Hãy lập nhóm hai người với bạn kết đôi để cùng huỷ kết đôi.", ""));
                            return;
                        }

                        if (player.monster != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tham gia khi đang giao hàng", ""));
                            return;
                        }

                        Wedding wdxx = (Wedding) allWedding.get(player.idWedding);
                        if (wdxx != null && wdxx.action == 1) {
                            if (player.rcvGiftWedding == 0) {
                                player.createIdGiftWedding(wdxx.typeParty);
                            }

                            map.move2Map(player, 16, 24, 226, player.myCountry);
                            player.potions[92] = 0;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            break;
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy Lễ kết đôi nào bạn được mời", ""));
                        break;
                    }

                    return;
                case -26:
                    if (player.monster == null) {
                        if (player.map.mapIDLoadMap != 10) {
                            return;
                        }

                        if (abs(player.x / 16 - 58) <= rangeNpc && abs(player.y / 16 - 125) <= rangeNpc) {
                            // Di dung thi tran nguoi choi chon (truoc day bo qua lua chon, chon ngau nhien).
                            int[] langNha = mapstart[Math.max(0, Math.min(2, player.inCountry))];
                            int idVilage = (idOptionMenu >= 0 && idOptionMenu < langNha.length) ? langNha[idOptionMenu] : langNha[0];
                            if (Char.isSuKienMiniChucNu()) {
                                player.isDoChangeMap = true;
                            }

                            map.move2Map(player, 16 + Database.r.nextInt() % 5, 24 + Database.r.nextInt(20), idVilage, player.inCountry);
                            if (Char.isSuKienMiniChucNu()) {
                                player.doChangeMapMonster(false, oldMap);
                                player.isDoChangeMap = false;
                            }
                            break;
                        }

                        return;
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang giao hàng", ""));
                    return;
                case -25:
                    if (!isMapThanh(player.map)) {
                        return;
                    }

                    int xlequan = 24;
                    int ylequan = 15;
                    if (abs(player.x / 16 - xlequan) <= rangeNpc && abs(player.y / 16 - ylequan) <= rangeNpc) {
                        if (idClanTown[player.myCountry] == -1) {
                            return;
                        }

                        if (player.idClan != -1 && player.myCountry != -1) {
                            if (player.inCountry != player.myCountry) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể đăng ký tại vùng của mình.", ""));
                                return;
                            }

                            if (idClanTown[player.myCountry] != player.idClan) {
                                return;
                            }

                            NewClan cla = getClaninfoByID(player.idClan);
                            if (cla == null) {
                                return;
                            }

                            if (!player.charname.equals(cla.master)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể sử dụng chức năng này", ""));
                                return;
                            }

                            long moneyClan = cla.getMoneyDistributeSalary(player.idClan);
                            if (idOptionMenu < 2 && idMenu == 0 && moneyClan < 15000000L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Đội chỉ có thể sử dụng " + moneyClan + " xu.", ""));
                                return;
                            }

                            if (idMenu != 0) {
                                if (idMenu == 1) {
                                        if (player.rankGov != 0) {
                                                 return;
                                    }

                                    String nt = new Date(System.currentTimeMillis()).toString();
                                   
                                    // Nếu isSkipDayCheck = true thì sẽ bỏ qua check ngày hoàn toàn
                                    if (!MapTown.isSkipDayCheck) {  // Chỉ check ngày khi isSkipDayCheck = false
                                        // Mon/Fri: lien dau. Tue/Sat: chiem thanh.
                                        boolean isDayx = nt.startsWith("Mon") || nt.startsWith("Fri") || nt.startsWith("Tue") || nt.startsWith("Sat");
                                        if (isDayx) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đăng ký trong ngày hôm nay.", ""));
                                            return;
                                        }
                                    }

                                    if (Calendar.getInstance().get(11) >= 19 && !MapTown.isSkipDayCheck) {  // Thêm điều kiện kiểm tra lệnh admin
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Hết thời gian đăng ký.", ""));
                                        return;
                                    }

                                    RegisterAttack reg = (RegisterAttack) nationWar.get(player.myCountry);
                                    if (reg.idMyAttack != -1) {
                                        player.sendMessage(
                                                MessageCreator.createServerAlertMessage(
                                                        "Bạn đã đăng ký tấn công vùng " + Map.nameCountry[reg.idMyAttack] + " vào ngày " + reg.dayAttack, ""
                                                )
                                        );
                                        return;
                                    }

                                    byte[][] idCountry = new byte[][]{{1, 2}, {0, 2}, {0, 1}};
                                    if (idClanTown[idCountry[player.myCountry][idOptionMenu]] == -1) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tấn công vùng này.", ""));
                                        return;
                                    }

                                    if (nationBeAttack[idCountry[player.myCountry][idOptionMenu]] > -1) {
                                        player.sendMessage(
                                                MessageCreator.createServerAlertMessage(
                                                        Map.nameCountry[nationBeAttack[idCountry[player.myCountry][idOptionMenu]]]
                                                        + " đã đăng ký tấn công vùng "
                                                        + Map.nameCountry[idCountry[player.myCountry][idOptionMenu]],
                                                        ""
                                                )
                                        );
                                        return;
                                    }

                                    NewClan clanx = NewClan.getClan(player.idClan);
                                    int cos = 20000000;
                                    if (clanx.money < (long) cos) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Đội phải có " + cos + " xu mới có thể đăng ký.", ""));
                                        return;
                                    }

                                    if (nationBeAttack[player.myCountry] == idCountry[player.myCountry][idOptionMenu]) {
                                        player.sendMessage(
                                                MessageCreator.createServerAlertMessage(
                                                        Map.nameCountry[nationBeAttack[player.myCountry]] + " đã đăng ký tấn công vùng của bạn.", ""
                                                )
                                        );
                                        return;
                                    }

                                    clanx.addMoney2Clan((long) (-cos));
                                    clanx.updateNewClandata2DB();
                                    nationBeAttack[idCountry[player.myCountry][idOptionMenu]] = player.myCountry;
                                    reg.idMyAttack = idCountry[player.myCountry][idOptionMenu];
                                    ((RegisterAttack) nationWar.get(idCountry[player.myCountry][idOptionMenu])).setNationAttackMe(player.myCountry);
                                    reg.dayAttack = Char.getDayOpen(0L);
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    "Bạn đã đăng ký tấn công vùng "
                                                    + Map.nameCountry[reg.idMyAttack]
                                                    + ". Cuộc tấn công sẽ diễn ra lúc 20h ngày "
                                                    + reg.dayAttack,
                                                    ""
                                            )
                                    );
                                    sendAllCharServer(
                                            -1,
                                            MessageCreator.createServerAlertAutoOffMessage(
                                                    Map.nameCountry[player.myCountry]
                                                    + " đã đăng ký tấn công vùng "
                                                    + Map.nameCountry[reg.idMyAttack]
                                                    + ". Cuộc tấn công sẽ diễn ra lúc 20h ngày "
                                                    + reg.dayAttack
                                            )
                                    );
                                    Database.instance.saveEvent(event.getInfo());
                                    Database.instance
                                            .saveOrtherLog(
                                                    "",
                                                    player.charname,
                                                    clanx.name
                                                    + "_"
                                                    + clanx.money
                                                    + " vùng "
                                                    + Map.nameCountry[player.myCountry]
                                                    + "_ đăng ký tấn công "
                                                    + Map.nameCountry[reg.idMyAttack],
                                                    "regWar"
                                            );
                                }
                            } else {
                                if (idOptionMenu != 0) {
                                    if (idOptionMenu != 1) {
                                        // ... existing code ...
                                    if (idOptionMenu == 2) {
                                        String ntx = new Date(System.currentTimeMillis()).toString();
                                        if (!MapTown.isSkipDayCheck) {  // Chỉ check ngày khi isSkipDayCheck = false
                                            // Mon/Fri: lien dau. Tue/Sat: chiem thanh.
                                            boolean isDayx = ntx.startsWith("Mon") || ntx.startsWith("Fri") || ntx.startsWith("Tue") || ntx.startsWith("Sat");
                                            if (isDayx) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đăng ký trong ngày hôm nay.", ""));
                                                return;
                                            }
                                        }

                                        String[][] nameCountry = new String[][]{
                                            {"Tấn công Johto(20t xu đội)"}, 
                                            {"Tấn công Kanto(20t xu đội)"}
                                        };
                                        player.sendMessage(MessageCreator.createMsgMenu(
                                            nameCountry[player.myCountry], 
                                            idNpc, 
                                            1
                                        ));
                                        return;
                                    }

                                    } else {
                                        if (SHIELD[player.myCountry][0] == 1
                                                && SHIELD[player.myCountry][1] == 1
                                                && SHIELD[player.myCountry][2] == 1
                                                && SHIELD[player.myCountry][3] == 1) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Các khung giờ đã được bảo hộ.", ""));
                                            return;
                                        }

                                        if (moneyClan < 50000000L) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Đội chỉ có thể sử dụng " + moneyClan + " xu.", ""));
                                            return;
                                        }

                                        SHIELD[player.myCountry][0] = 1;
                                        SHIELD[player.myCountry][1] = 1;
                                        SHIELD[player.myCountry][2] = 1;
                                        SHIELD[player.myCountry][3] = 1;
                                        cla.addMoney2Clan(-50000000L);
                                        cla.updateNewClandata2DB();
                                        Database.instance
                                                .saveOrtherLog("", player.charname, cla.name + "_" + cla.money + " >> bao ho toan bo 50t" + idOptionMenu, "shieldAll");
                                        sendAllCharServer(
                                                MessageCreator.createServerAlertAutoOffMessage(
                                                        "Vùng " + Map.nameCountry[player.myCountry] + " đã được bảo hộ toàn bộ các khung giờ."
                                                ),
                                                player.myCountry
                                        );
                                    }
                                } else {
                                    int iHour = Char.getIdOpenShield(Calendar.getInstance().get(11));
                                    if (SHIELD[player.myCountry][iHour] == 1) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Khung giờ này đã được bảo hộ.", ""));
                                        return;
                                    }

                                    SHIELD[player.myCountry][iHour] = 1;
                                    NewClan.timeDelayOpenShield[player.myCountry] = 0;
                                    cla.addMoney2Clan(-15000000L);
                                    cla.updateNewClandata2DB();
                                    Database.instance.saveOrtherLog("", player.charname, cla.name + "_" + cla.money + " >> bao ho khung h 15t" + iHour, "shield");
                                    sendAllCharServer(
                                            MessageCreator.createServerAlertAutoOffMessage(
                                                    "Vùng " + Map.nameCountry[player.myCountry] + " đã được bảo hộ trong khung giờ " + (iHour + 1)
                                            ),
                                            player.myCountry
                                    );
                                }

                                Database.instance.saveEvent(event.getInfo());
                            }
                            break;
                        }

                        return;
                    }

                    return;
                    case -24:
                    if (!isMapLang(player.map)) {
                        return;
                    }
                
                    switch (idMenu) {
                        case 0: // Main menu
                            switch (idOptionMenu) {
                                case 0: // Buy lottery number
                                    if (player.getxu() < 1000000L) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                        return;
                                    }
                                    player.doOpenNumber();
                                    if (!Char.timeBuy()) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Vé chỉ bán từ sau 18h ngày hiện tại đến 16h ngày hôm sau.", ""));
                                        return;
                                    }
                                    Xoso.doRequestBuyNumber(player, (byte) 0);
                                    break;
                
                                case 1: // View lottery info
                                    player.sendInfoVeso((byte) 0);
                                    break;
                
                                case 2: // View lucky draw items
                                    if (player.potions[69] == 0 && player.potions[76] == 0 && player.potions[118] == 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Vui lòng mua vé để có thể tham gia quay số", ""));
                                        return;
                                    }
                                    player.doSendListItemLuckyDraw();
                                    break;
                
                                case 3: // Buy regular lottery ticket
                                    if (player.getLuong() < 10) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                        return;
                                    }
                                    player.subLuong(10L);
                                    Database.instance.saveLogThongKeChiTieu(LoginHandler.PORTION_NAME[69], 0, 2, 10L, "luong");
                                    player.potions[69] = player.potions[69] + 2;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    Database.instance.saveOrtherLog("", player.charname, "mua ve quay so " + player.potions[69], "bvqsl");
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã mua được 2 vé quay số.", ""));
                                    break;
                
                                case 4: // Buy xu lottery ticket
                                    if (player.getxu() < 10000000L) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                        return;
                                    }
                                    Database.instance.saveLogThongKeChiTieu(LoginHandler.PORTION_NAME[69] + "_xu", 0, 2, 10000000L, "xu");
                                    player.subXu(10000000L, true, "map 37");
                                    player.potions[69] = player.potions[69] + 2;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    Database.instance.saveOrtherLog("", player.charname, "mua ve quay so " + player.potions[69], "bvqsxu");
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã mua được 2 vé quay số.", ""));
                                    break;
                
                                case 5: // Buy special lottery ticket
                                    if (player.getLuong() < 10) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                        return;
                                    }
                                    Database.instance.saveLogThongKeChiTieu(LoginHandler.PORTION_NAME[76], 0, 2, 10L, "luong");
                                    player.subLuong(10L);
                                    player.potions[76] = player.potions[76] + 2;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    Database.instance.saveOrtherLog("", player.charname, "mua ve quay so " + player.potions[76], "bvqsl");
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã mua được 2 vé quay số đặc biệt.", ""));
                                    break;
                
//                                case 6: // Buy phoenix lottery ticket // TODO vé quay phượng hoàng
//                                    if (player.getLuong() < 50) {
//                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
//                                        return;
//                                    }
//                                    Database.instance.saveLogThongKeChiTieu(LoginHandler.PORTION_NAME[118], 0, 2, 50L, "luong");
//                                    player.subLuong(50L);
//                                    player.potions[118] = player.potions[118] + 2;
//                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
//                                    Database.instance.saveOrtherLog("", player.charname, "mua ve quay phuong hoang " + player.potions[118], "bvqph");
//                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã mua được 2 vé quay Phượng Hoàng.", ""));
//                                    break;
                
                                case 7:
                                    if (player.canReceiveGif35()) {
                                        player.doReceiveGif35(0);
                                    } else if (!openLog) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nhận quà", ""));
                                    } else {
                                        String[] menu = new String[]{"Đổi qua lượng", "Tặng", "Xem kim cương", "Tạo đơn hàng", "Hướng dẫn"};
                                        player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                                    }
                                    break;
                                case 8:
                                    if (player.canReceiveGif35()) {
                                        player.doReceiveGif35(1);
                                    } else if (!openLog) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nhận quà", ""));
                                    } else {
                                        String[] menu = new String[]{"Đổi qua lượng", "Tặng", "Xem kim cương", "Tạo đơn hàng", "Hướng dẫn"};
                                        player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                                    }
                                    break;
                                case 9:
                                    if (openLog) {
                                        if (player.canReceiveGif35()) {
                                            player.sendMessage(MessageCreator.createMsgInputText(-32042, 0, "Mã quà tặng", 0));
                                        } else {
                                            String[] menusms = new String[]{"Nạp 20k", "Nạp 50k", "Nạp 100k"};
                                            player.sendMessage(MessageCreator.createMsgMenu(menusms, idNpc, 3));
                                        }
                                    }
                                    break;
                
                                case 10:
                                    if (openLog) {
                                        if (player.canReceiveGif35()) {
                                            String[] menu = new String[]{"Đổi qua lượng", "Tặng", "Xem kim cương", "Tạo đơn hàng", "Hướng dẫn"};
                                            player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                                        } else {
                                            player.sendMessage(MessageCreator.createMsgInputText(-32045, 0, "Tặng 10 lượng", 0));
                                        }
                                    }
                                    break;
                
                                case 11:
                                    String[] menusms = new String[]{"Nạp 20k", "Nạp 50k", "Nạp 100k"};
                                    player.sendMessage(MessageCreator.createMsgMenu(menusms, idNpc, 3));
                                    break;
                
                                case 12:
                                    if (openLog) {
                                        player.sendMessage(MessageCreator.createMsgInputText(-32045, 0, "Tặng 10 lượng", 0));
                                    }
                                    break;
                            }
                            break;
                
                        case 1: // Currency menu
                            if (idOptionMenu == 0) {
                                String[] menu = new String[]{
                                    "10 kc -> 13 Lượng", "20 kc -> 32 Lượng", "50 kc -> 91 Lượng", 
                                    "100 kc -> 195 Lượng", "500 kc -> 1430 Lượng", "1000 kc -> 3250 Lượng"
                                };
                                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
                            } else {
                                CharChuyenPhieuQua c = (CharChuyenPhieuQua) ALL_CHAR_CHUYEN_KIM_CUONG.get(player.charname);
                                if (c != null) {
                                    if (!c.isTimeOver()) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đang chờ hoàn thành tặng Kim cương. Xin thử lại sau", ""));
                                        return;
                                    }
                                    ALL_CHAR_CHUYEN_KIM_CUONG.remove(player.charname);
                                }
                                player.sendMessage(MessageCreator.createMsgInputText(-32044, 0, "Tên người nhận", 0));
                            }
                            break;
                
                        case 2: // Diamond info
                            // SECURITY: disabled external call to third-party (27.0.14.41) server.
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng này hiện không khả dụng.", ""));
                            break;

                        case 3: // SMS top-up
                            // SECURITY: disabled external teamobi SMS top-up link (leaked charname to third-party).
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng nạp SMS hiện không khả dụng.", ""));
                            break;
                    }
                    break;
                case -23:
                    if (player.myCountry < 2 && player.myCountry > -1) {
                        return;
                    }

                    if (idMenu != 0) {
                        if (idMenu == 1) {
                            if (player.monster != null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi đang giao hàng", ""));
                                return;
                            }

                            if (player.myCountry != -1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã gia nhập " + Map.nameCountry[player.myCountry], ""));
                            } else {
                                if (checkFullNation(idOptionMenu, 1, player.lvDetail.lv)) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    Map.nameCountry[idOptionMenu] + " lãnh đã có quá nhiều người. Vui lòng chọn vùng khác.", ""
                                            )
                                    );
                                    return;
                                }

                                player.myCountry = (byte) idOptionMenu;
                                player.inCountry = player.myCountry;
                                allUser[idOptionMenu]++;
                                player.sendMessage(MessageCreator.createMessageLocation(real.LangKhoiDau.oCong(player, player.myCountry)));
                                int mapID = mapstart[idOptionMenu][r.nextInt(mapstart[idOptionMenu].length)];
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã gia nhập " + Map.nameCountry[idOptionMenu], ""));
                                Database.instance.saveOrtherLog("", player.charname, player.charname + "_" + Map.nameCountry[idOptionMenu], "csnation");
                                map.move2Map(player, 16 + Database.r.nextInt() % 5, 24 + Database.r.nextInt(20), mapID, player.myCountry);
                                MessageCreator.createMsgCharMonster(player, player);
                            }
                        }
                    } else {
                        if (player.idClan < 0) {
                            return;
                        }

                        if (player.monster != null) {
                            return;
                        }

                        NewClan cinfo = getClaninfoByID(player.idClan);
                        if (cinfo != null && cinfo.country == -1 && cinfo.master.toLowerCase().equals(player.charname.toLowerCase())) {
                            if (checkFullNation(idOptionMenu, cinfo.getAllMember().size(), cinfo.allLv)) {
                                player.sendMessage(
                                        MessageCreator.createServerAlertMessage(
                                                Map.nameCountry[idOptionMenu] + " lãnh đã có quá nhiều người. Vui lòng chọn vùng khác.", ""
                                        )
                                );
                                return;
                            }

                            cinfo.country = (byte) idOptionMenu;
                            int mapID = mapstart[idOptionMenu][r.nextInt(mapstart[idOptionMenu].length)];
                            cinfo.country = cinfo.country;

                            for (byte ixxx = 0; ixxx < cinfo.getAllMember().size(); ixxx++) {
                                Char p = CharManager.instance.getCharByCharName(((CharClan) cinfo.getAllMember().get(ixxx)).charname.toLowerCase());
                                if (p != null) {
                                    p.myCountry = cinfo.country;
                                    p.inCountry = p.myCountry;
                                    p.sendMessage(MessageCreator.createMessageLocation(real.LangKhoiDau.oCong(p, p.myCountry)));
                                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                    p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                                    map.move2Map(p, 16 + Database.r.nextInt() % 5, 24 + Database.r.nextInt(20), mapID, p.myCountry);
                                    p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã gia nhập " + Map.nameCountry[cinfo.country], ""));
                                    MessageCreator.createMsgCharMonster(p, p);
                                }
                            }

                            allUser[idOptionMenu] = allUser[idOptionMenu] + cinfo.getMember().size();
                            allLV[idOptionMenu] = allLV[idOptionMenu] + cinfo.allLv;
                            player.myCountry = cinfo.country;
                            player.inCountry = player.myCountry;
                            cinfo.updateNewClandata2DB();
                            Database.instance
                                    .saveOrtherLog(
                                            "",
                                            player.charname,
                                            cinfo.id + "_" + cinfo.name + "_" + "_" + Map.nameCountry[idOptionMenu] + "_" + cinfo.getListMemberClan(),
                                            "snation"
                                    );
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            player.sendMessage(
                                    MessageCreator.createServerAlertMessage("Chào mừng đội " + cinfo.name + " đã gia nhập " + Map.nameCountry[cinfo.country], "")
                            );
                            MessageCreator.createMsgCharMonster(player, player);
                        } else {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Đội của bạn đã gia nhập " + Map.nameCountry[cinfo.country], ""));
                        }
                    }
                    break;
                case -22:
                    if (player.mapID != 118) {
                        return;
                    }

                    if (abs(player.x / 16 - 46) <= 7 && abs(player.y / 16 - 40) <= 7) {
                        if (player.monster != null && player.mapID == player.monster.map.mapId) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi đang giao hàng", ""));
                            return;
                        }

                        if (idMenu != 0) {
                            if (idMenu == 1) {
                                byte[][] idCountry = new byte[][]{{1, 2}, {0, 2}, {1, 0}};
                                if (player.myCountry < 0 || player.myCountry >= idCountry.length
                                        || idOptionMenu < 0 || idOptionMenu >= idCountry[player.myCountry].length) {
                                    return;
                                }
                                byte targetCountry = idCountry[player.myCountry][idOptionMenu];
                                // Day la noi DUY NHAT doi player.myCountry (quoc tich vinh vien,
                                // khac inCountry tam thoi o nhanh idMenu==0 ben duoi) ma khong
                                // qua checkFullNation nhu moi cho doi quoc tich khac. Them vao cho
                                // nhat quan - hien khong doi hanh vi vi checkFullNation dang la
                                // stub luon tra false (Map.java), nhung se tu dong duoc bao ve
                                // ngay khi ham do duoc bat lai that.
                                if (checkFullNation(targetCountry, 1, player.lvDetail.lv)) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage(
                                            Map.nameCountry[targetCountry] + " đã có quá nhiều người. Vui lòng chọn vùng khác.", ""));
                                    return;
                                }
                                player.myCountry = targetCountry;
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                                MessageCreator.createMsgCharMonster(player, player);
                            }
                        } else {
                            byte[][] idCountry = new byte[][]{{1}, new byte[1]};
                            if (idOptionMenu >= 1) {
                                if (idOptionMenu == 1) {
                                    int[] mapstart1 = new int[]{9, 481, 482, 483, 484};
                                    int mapID = mapstart1[r.nextInt(MessageCreator.nclone)];
                                    map.move2Map(player, 16 + Database.r.nextInt() % 5, 84, mapID, player.inCountry);
                                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                }
                            } else {
                                if (player.inCountry < 0 || player.inCountry >= idCountry.length
                                        || idOptionMenu < 0 || idOptionMenu >= idCountry[player.inCountry].length) {
                                    return;
                                }
                                try {
                                    ((Vector) listKillPerson.get(player.inCountry)).remove(player.charname);
                                } catch (Exception var39) {
                                }

                                map.move2Map(player, player.x / 16, player.y / 16, player.mapID, idCountry[player.inCountry][idOptionMenu]);
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                if (Char.haveShieldNation(player.inCountry)) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertAutoOffMessage("Vùng " + Map.nameCountry[player.inCountry] + " đang được bảo hộ")
                                    );
                                }
                            }
                        }
                        break;
                    }

                    return;
                case -21:
                case -20:
                    if (player.monster == null) {
                        if (player.map.mapIDLoadMap != 21 && player.map.mapIDLoadMap != 22) {
                            return;
                        }

                        if (idNpc != -20) {
                            if (idNpc == -21 && (abs(player.x / 16 - 2) > rangeNpc || abs(player.y / 16 - 46) > rangeNpc)) {
                                return;
                            }
                        } else if (abs(player.x / 16 - 38) > rangeNpc || abs(player.y / 16 - 3) > rangeNpc) {
                            return;
                        }

                        int[] var51 = new int[]{114, 2581, 2582, 2583, 2584};
                        if (Char.isSuKienMiniChucNu()) {
                            player.isDoChangeMap = true;
                        }

                        map.move2Map(player, 120, 119, var51[idOptionMenu], player.inCountry);
                        if (Char.isSuKienMiniChucNu()) {
                            player.doChangeMapMonster(false, oldMap);
                            player.isDoChangeMap = false;
                        }
                        break;
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang giao hàng", ""));
                    return;
                case -19:
                    if (player.monster != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang giao hàng", ""));
                        return;
                    }

                    if (player.map.mapIDLoadMap == 114) {
                        if (player.getxu() < 30000L) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chi phí vận chuyển là 30000 xu", ""));
                            return;
                        }

                        if (abs(player.x / 16 - 120) <= rangeNpc && abs(player.y / 16 - 119) <= rangeNpc) {
                            if (idMenu != 0) {
                                if (idMenu != 1) {
                                    if (idMenu == 2) {
                                        int[] tempMap = new int[]{22, 741, 742, 743, 744};
                                        if (Char.isSuKienMiniChucNu()) {
                                            player.isDoChangeMap = true;
                                        }

                                        map.move2Map(player, 2, 46, tempMap[idOptionMenu], player.inCountry);
                                        if (Char.isSuKienMiniChucNu()) {
                                            player.doChangeMapMonster(false, oldMap);
                                            player.isDoChangeMap = false;
                                        }

                                        player.subXu(30000L, true, "map 39");
                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    }
                                } else {
                                    int[] var50 = new int[]{21, 721, 722, 723, 724};
                                    if (Char.isSuKienMiniChucNu()) {
                                        player.isDoChangeMap = true;
                                    }

                                    map.move2Map(player, 35, 3, var50[idOptionMenu], player.inCountry);
                                    if (Char.isSuKienMiniChucNu()) {
                                        player.doChangeMapMonster(false, oldMap);
                                        player.isDoChangeMap = false;
                                    }

                                    player.subXu(30000L, true, "map 38");
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else if (idOptionMenu != 0) {
                                if (idOptionMenu == 1) {
                                    String[] menu = NpcDichVu.tenBanDo(22, 741, 742, 743, 744);
                                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
                                }
                            } else {
                                String[] menu = NpcDichVu.tenBanDo(21, 721, 722, 723, 724);
                                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                            }
                            break;
                        }

                        return;
                    }

                    return;
                case -18:
                    return;
                case -17:
                    return;
                case -16:
                    if (!isMapThanh(player.map)) {
                        return;
                    }

                    if (abs(player.x / 16 - 25) <= rangeNpc && abs(player.y / 16 - 10) <= rangeNpc) {
                        if (idMenu != 0) {
                            if (idMenu == 1) {
                                if (!player.isCorrectPass && !player.passWord.equals("")) {
                                    player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                                    return;
                                }

                                if (((Item) player.itemPetEat.get(idOptionMenu)).isTuHonDan() && player.potions[83] <= 0) {
                                    return;
                                }

                                if (player.petUsing.totalMinute > 0) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nâng cấp thú nuôi có thời hạn.", ""));
                                    return;
                                }

                                if (((Item) player.itemPetEat.get(idOptionMenu)).isSelling) {
                                    System.out.println("");
                                    System.out.println(player.charname + " bug cho pet an item dang ban tren thanh");
                                    Database.instance
                                            .saveOrtherLog("", player.charname, "bug item cho pet an " + ((Item) player.itemPetEat.get(idOptionMenu)).getName(), "bpeat");
                                    return;
                                }

                                try {
                                    if (player.petUsing.doEat(((Item) player.itemPetEat.get(idOptionMenu)).getExpPetEatfromItem(player), player)) {
                                        player.sendMessage(
                                                MessageCreator.createMsgChat(player.id, "Đã cho ăn " + ((Item) player.itemPetEat.get(idOptionMenu)).getNameItemPetEat())
                                        );
                                        if (!player.isAdmin || !adminTest) {
                                            if (!((Item) player.itemPetEat.get(idOptionMenu)).isTuHonDan()) {
                                                player.iItems.remove(player.itemPetEat.get(idOptionMenu));
                                                player.removeIDItem(((Item) player.itemPetEat.get(idOptionMenu)).id);

                                                try {
                                                    Database.instance
                                                            .saveOrtherLog(
                                                                    "",
                                                                    player.charname,
                                                                    "cho pet an Tu hon dan c1 "
                                                                    + ((Item) player.itemPetEat.get(idOptionMenu)).getName()
                                                                    + "_"
                                                                    + ((Item) player.itemPetEat.get(idOptionMenu)).getInfoSave(),
                                                                    "petan"
                                                            );
                                                } catch (Exception var38) {
                                                }
                                            } else {
                                                int truoc = player.potions[83];
                                                player.potions[83]--;
                                                Database.instance
                                                        .saveOrtherLog(
                                                                "",
                                                                player.charname,
                                                                "trc khi cho cho pet an Tu hon dan c1 có: " + truoc + ", sau khi cho an con: " + player.potions[83],
                                                                "petan"
                                                        );
                                            }
                                        }

                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                        player.itemPetEat.removeAllElements();
                                        Database.instance.savePet(player.petUsing);
                                        Database.instance.saveCharAuto(player);
                                    }
                                } catch (Exception var45) {
                                }
                            }
                        } else if (idOptionMenu != 0) {
                            if (idOptionMenu != 1) {
                                if (idOptionMenu != 2) {
                                    if (idOptionMenu != 3) {
                                        if (idOptionMenu != 4) {
                                            if (idOptionMenu == 5) {
                                                if (!player.isCorrectPass && !player.passWord.equals("")) {
                                                    player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                                                    return;
                                                }

                                                if (player.petUsing.lvDetail.canUplevel() && player.petUsing.lvDetail.lv < 40) {
                                                    player.petUsing.doUpdrage(player, GemTemplate.LKD_40);
                                                    Database.instance.savePet(player.petUsing);
                                                }
                                            }
                                        } else {
                                            if (!player.isCorrectPass && !player.passWord.equals("")) {
                                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                                                return;
                                            }

                                            if (player.petUsing.lvDetail.canUplevel() && player.petUsing.lvDetail.lv < 40) {
                                                player.petUsing.doUpdrage(player, GemTemplate.LKD_35);
                                                Database.instance.savePet(player.petUsing);
                                            }
                                        }
                                    } else {
                                        if (!player.isCorrectPass && !player.passWord.equals("")) {
                                            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                                            return;
                                        }

                                        if (player.petUsing.lvDetail.canUplevel() && player.petUsing.lvDetail.lv < 40) {
                                            player.petUsing.doUpdrage(player, GemTemplate.LKD_30);
                                            Database.instance.savePet(player.petUsing);
                                        }
                                    }
                                } else {
                                    if (!player.isCorrectPass && !player.passWord.equals("")) {
                                        player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                                        return;
                                    }

                                    if (player.petUsing.lvDetail.canUplevel() && player.petUsing.lvDetail.lv < 40) {
                                        player.petUsing.doUpdrage(player, -1);
                                        Database.instance.savePet(player.petUsing);
                                    }
                                }
                            } else {
                                if (!player.isCorrectPass && !player.passWord.equals("")) {
                                    player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                                    return;
                                }

                                Vector<Item> allit = player.getAllItemPetEat();
                                if (allit.size() > 0) {
                                    String[] menu = new String[allit.size()];

                                    for (int ixxxx = 0; ixxxx < allit.size(); ixxxx++) {
                                        menu[ixxxx] = ((Item) allit.get(ixxxx)).getNameItemPetEat();
                                    }

                                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                                }
                            }
                        } else {
                            if (!checkTimeRegEventMonster()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể đăng ký từ 19h-21h", ""));
                                return;
                            }

                            if (player.myCountry != player.inCountry) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tham gia sự kiện trong vùng nước của bạn.", ""));
                                return;
                            }

                            for (int ixxxx = lvEventMonster.length - 1; ixxxx >= 0; ixxxx--) {
                                if (player.lvDetail.lv > lvEventMonster[ixxxx]) {
                                    Vector<Short> g = (Vector<Short>) ((Vector) groupMonster.get(player.myCountry)).elementAt(ixxxx);
                                    if (!openLog) {
                                        g = (Vector<Short>) ((Vector) groupMonster.get(0)).elementAt(ixxxx);
                                    }

                                    if (g.size() < 100) {
                                        if (g.contains(player.id)) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã tham gia nhóm " + (ixxxx + 1), ""));
                                        } else {
                                            g.add(player.id);
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Đăng ký thành công vào nhóm " + (ixxxx + 1), ""));
                                        }

                                        return;
                                    }

                                    player.sendMessage(MessageCreator.createServerAlertMessage("Nhóm " + (ixxxx + 1) + " đã hết chỗ.", ""));
                                    return;
                                }
                            }
                        }
                        break;
                    }

                    return;
                case -15:
                    if (!isMapLang(player.map)) {
                        return;
                    }

                    String[] he = new String[]{"hệ Nước", "hệ Cỏ", "hệ Lửa", "hệ Đất", "hệ Thép"};
                    if (idMenu != 0) {
                        if (idMenu != 1) {
                            if (idMenu != 2) {
                                if (idMenu != 3) {
                                    if (idMenu != 4) {
                                        if (idMenu != 5) {
                                            if (idMenu != 6) {
                                                if (idMenu != 7) {
                                                    if (idMenu != 10) {
                                                        if (idMenu != 11) {
                                                            if (idMenu != 12) {
                                                                if (idMenu == 13) {
                                                                    if (!Char.isOpenSellBlackFriday()) {
                                                                        player.sendMessage(
                                                                                MessageCreator.createServerAlertMessage("Chỉ có thể mua sau 10h ngày 30/11 đến hết ngày 02/12", "")
                                                                        );
                                                                        return;
                                                                    }

                                                                    int money = Database.instance.getMoneyDauGia(player.charname, 1);
                                                                    if (money > 0) {
                                                                        int[][] iditem = new int[][]{{725, 723}, {726, 724}};
                                                                        int luong = money - money / 2;
                                                                        if (player.getLuong() < luong) {
                                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + luong + " lượng", ""));
                                                                            return;
                                                                        }

                                                                        Database.instance.updateHangDauGia(player.charname, 0);
                                                                        player.subLuong((long) (money - money / 2));
                                                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                        Item it = doBuyModelClothe(
                                                                                player, !player.isCharNam() ? iditem[1][idOptionMenu] : iditem[0][idOptionMenu], 0, 1
                                                                        );
                                                                        player.sendMessage(
                                                                                MessageCreator.createServerAlertMessage("Nhận được " + it.getName() + " vĩnh viễn", "")
                                                                        );
                                                                        Database.instance.saveLogThongKeChiTieu(it.getName(), 0, 1, (long) (money - money / 2), "luong");
                                                                        Database.instance
                                                                                .saveOrtherLog("", player.charname, "mua " + it.getName() + " " + it.getInfoSave(), "black");
                                                                    }
                                                                }
                                                            } else if (idOptionMenu != 0) {
                                                                if (idOptionMenu != 1) {
                                                                    if (idOptionMenu != 2) {
                                                                        if (idOptionMenu != 3) {
                                                                            if (idOptionMenu != 4) {
                                                                                if (idOptionMenu != 5) {
                                                                                    if (idOptionMenu != 6) {
                                                                                        if (idOptionMenu == 7) {
                                                                                            if (!Char.isOpenSellBlackFriday()) {
                                                                                                player.sendMessage(
                                                                                                        MessageCreator.createServerAlertMessage(
                                                                                                                "Chỉ có thể nhận lại tiền cọc sau 10h ngày 30/11 đến hết ngày 02/12", ""
                                                                                                        )
                                                                                                );
                                                                                                return;
                                                                                            }

                                                                                            player.sendMessage(
                                                                                                    MessageCreator.createMsgMenu(new String[]{"Đồ thời trang", "Lồng đèn"}, idNpc, 11)
                                                                                            );
                                                                                        }
                                                                                    } else {
                                                                                        if (!Char.isOpenSellBlackFriday()) {
                                                                                            player.sendMessage(
                                                                                                    MessageCreator.createServerAlertMessage(
                                                                                                            "Chỉ có thể mua sau 10h ngày 30/11 đến hết ngày 02/12", ""
                                                                                                    )
                                                                                            );
                                                                                            return;
                                                                                        }

                                                                                        int money = Database.instance.getMoneyDauGia2(player.charname, 1);
                                                                                        if (money <= 0) {
                                                                                            player.sendMessage(
                                                                                                    MessageCreator.createServerAlertMessage("Chưa mở bán hoặc bạn không thể mua thêm", "")
                                                                                            );
                                                                                        } else {
                                                                                            int luong = money - money / 2;
                                                                                            if (player.getLuong() < luong) {
                                                                                                player.sendMessage(
                                                                                                        MessageCreator.createServerAlertMessage("Không đủ " + luong + " lượng", "")
                                                                                                );
                                                                                                return;
                                                                                            }

                                                                                            player.createPetVinhVien(28);
                                                                                            Database.instance.updateHangDauGia2(player.charname, 0);
                                                                                            player.subLuong((long) (money - money / 2));
                                                                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                                            player.sendMessage(
                                                                                                    MessageCreator.createServerAlertMessage("Nhận được lồng đèn hoa sen vĩnh viễn", "")
                                                                                            );
                                                                                            Database.instance
                                                                                                    .saveLogThongKeChiTieu("long den hoa sen", 0, 1, (long) (money - money / 2), "luong");
                                                                                            Database.instance.saveOrtherLog("", player.charname, "mua long den hoa sen", "black");
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    if (!Char.isOpenSellBlackFriday()) {
                                                                                        player.sendMessage(
                                                                                                MessageCreator.createServerAlertMessage(
                                                                                                        "Chỉ có thể mua sau 10h ngày 30/11 đến hết ngày 02/12", ""
                                                                                                )
                                                                                        );
                                                                                        return;
                                                                                    }

                                                                                    int money = Database.instance.getMoneyDauGia(player.charname, 1);
                                                                                    if (money <= 0) {
                                                                                        player.sendMessage(
                                                                                                MessageCreator.createServerAlertMessage("Chưa mở bán hoặc bạn không thể mua thêm", "")
                                                                                        );
                                                                                    } else {
                                                                                        String[][] namett = new String[][]{{"Áo Absol", "Áo Zoroark"}, {"Áo Oricorio", "Áo Gardevoir"}};
                                                                                        player.sendMessage(
                                                                                                MessageCreator.createMsgMenu(!player.isCharNam() ? namett[1] : namett[0], idNpc, 13)
                                                                                        );
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                map.sendListItemSell2Shop(player);
                                                                            }
                                                                        } else {
                                                                            VongQuay.moChonLoai(player, idNpc);
                                                                        }
                                                                    } else if (!player.passWord.equals("")) {
                                                                        player.sendMessage(MessageCreator.createMsgInputText(player.id, 3, "Nhập mật khẩu", 0));
                                                                    } else {
                                                                        player.sendMessage(MessageCreator.createMsgInputText(player.id, 2, "Nhập mật khẩu", 0));
                                                                    }
                                                                } else {
                                                                    player.getItemResetSock();
                                                                    String[] menu = new String[player.itemResetSock.size()];

                                                                    for (int ixxx = 0; ixxx < menu.length; ixxx++) {
                                                                        Item it = (Item) player.itemResetSock.get(ixxx);
                                                                        menu[ixxx] = it.getTemplate().name + " " + it.getMoneyReset();
                                                                    }

                                                                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
                                                                }
                                                            } else {
                                                                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Dùng lượng"}, idNpc, 3));
                                                            }
                                                        } else {
                                                            if (!Char.isOpenSellBlackFriday()) {
                                                                return;
                                                            }

                                                            // idOptionMenu chi hop le 0 (bang tob_dau_gia, do thoi trang) hoac 1
                                                            // (bang tob_dau_gia2, long den). Truoc day doc theo "== 1" nhung xoa
                                                            // co theo "!= 0": voi idOptionMenu = 2 (hay bat ky gia tri nao khac
                                                            // 0/1), tien duoc DOC tu bang 1 (== 1 sai) nhung co "da nhan" lai bi
                                                            // XOA o bang 2 (!= 0 dung) - bang 1 khong bao gio duoc dat lai, nen
                                                            // goi lai voi idOptionMenu = 2 nhan duoc CHINH SO TIEN DO vo han lan.
                                                            if (idOptionMenu != 0 && idOptionMenu != 1) {
                                                                return;
                                                            }
                                                            boolean laBang2 = idOptionMenu == 1;
                                                            int money = laBang2
                                                                    ? Database.instance.getMoneyDauGia2(player.charname, 2)
                                                                    : Database.instance.getMoneyDauGia(player.charname, 2);

                                                            if (money > 0) {
                                                                if (laBang2) {
                                                                    Database.instance.updateHangDauGia2(player.charname, 0);
                                                                } else {
                                                                    Database.instance.updateHangDauGia(player.charname, 0);
                                                                }

                                                                player.addLuong(money);
                                                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                player.sendMessage(
                                                                        MessageCreator.createServerAlertMessage(
                                                                                "Bạn nhận lại được "
                                                                                + money
                                                                                + " lượng đã đặt cọc "
                                                                                + (idOptionMenu != 0 ? " lồng đèn" : " đồ thời trang"),
                                                                                ""
                                                                        )
                                                                );
                                                                Database.instance
                                                                        .saveOrtherLog(
                                                                                "",
                                                                                player.charname,
                                                                                "nhan lai " + money + " luong tien coc " + (idOptionMenu != 0 ? " lồng đèn" : " đồ thời trang"),
                                                                                "nhancoc"
                                                                        );
                                                            }
                                                        }
                                                    } else if (idOptionMenu != 0) {
                                                        if (idOptionMenu != 1) {
                                                            if (idOptionMenu != 2) {
                                                                if (idOptionMenu != 3) {
                                                                    if (idOptionMenu != 4) {
                                                                        if (idOptionMenu != 5) {
                                                                            if (idOptionMenu != 6) {
                                                                                if (idOptionMenu != 7) {
                                                                                    if (idOptionMenu != 8) {
                                                                                        if (idOptionMenu != 9) {
                                                                                            if (idOptionMenu != 10) {
                                                                                                if (idOptionMenu == 11) {
                                                                                                    player.sendMessage(
                                                                                                            MessageCreator.createMsgMenu(new String[]{"Đồ thời trang", "Lồng đèn"}, idNpc, 11)
                                                                                                    );
                                                                                                }
                                                                                            } else if (Char.isSuKienBlackFriday()) {
                                                                                                int moneyx = 111;
                                                                                                int timex = 86400;
                                                                                                if (player.getLuong() < moneyx) {
                                                                                                    player.sendMessage(
                                                                                                            MessageCreator.createServerAlertMessage("Không đủ " + moneyx + " lượng", "")
                                                                                                    );
                                                                                                    return;
                                                                                                }

                                                                                                player.doCreatePetBachThu(timex, true);
                                                                                                Database.instance
                                                                                                        .saveLogThongKeChiTieu("pet bach thu blackfriday", 0, 1, (long) moneyx, "luong");
                                                                                                player.subLuong((long) moneyx);
                                                                                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                                                Database.instance
                                                                                                        .saveOrtherLog("", player.charname, "mua pet bach thu black friday", "black");
                                                                                            }
                                                                                        } else if (Char.isSuKienBlackFriday()) {
                                                                                            int moneyx = 1111;
                                                                                            if (moneyx <= 0) {
                                                                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua", ""));
                                                                                            } else {
                                                                                                if (player.getLuong() < moneyx) {
                                                                                                    player.sendMessage(
                                                                                                            MessageCreator.createServerAlertMessage("Không đủ " + moneyx + " lượng", "")
                                                                                                    );
                                                                                                    return;
                                                                                                }

                                                                                                Database.instance
                                                                                                        .saveLogThongKeChiTieu("choi lua ko hoa tuyet", 0, 1, (long) moneyx, "luong");
                                                                                                player.subLuong((long) moneyx);
                                                                                                Char.createChoi(729, player, 43200, 1, false);
                                                                                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                                                Database.instance
                                                                                                        .saveOrtherLog("", player.charname, "mua choi lua black friday", "black");
                                                                                            }
                                                                                        }
                                                                                    } else if (Char.isSuKienBlackFriday()) {
                                                                                        int moneyx = 1111;
                                                                                        if (moneyx <= 0) {
                                                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua", ""));
                                                                                        } else {
                                                                                            if (player.getLuong() < moneyx) {
                                                                                                player.sendMessage(
                                                                                                        MessageCreator.createServerAlertMessage("Không đủ " + moneyx + " lượng", "")
                                                                                                );
                                                                                                return;
                                                                                            }

                                                                                            Database.instance
                                                                                                    .saveLogThongKeChiTieu("choi doc ko hoa tuyet", 0, 1, (long) moneyx, "luong");
                                                                                            player.subLuong((long) moneyx);
                                                                                            Char.createChoi(619, player, 43200, 1, false);
                                                                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                                            Database.instance.saveOrtherLog("", player.charname, "mua choi doc black friday", "black");
                                                                                        }
                                                                                    }
                                                                                } else if (Char.isSuKienBlackFriday()) {
                                                                                    int moneyx = 1111;
                                                                                    if (moneyx <= 0) {
                                                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua", ""));
                                                                                    } else {
                                                                                        if (player.getLuong() < moneyx) {
                                                                                            player.sendMessage(
                                                                                                    MessageCreator.createServerAlertMessage("Không đủ " + moneyx + " lượng", "")
                                                                                            );
                                                                                            return;
                                                                                        }

                                                                                        Database.instance
                                                                                                .saveLogThongKeChiTieu("choi set khong hoa tuyet", 0, 1, (long) moneyx, "luong");
                                                                                        player.subLuong((long) moneyx);
                                                                                        Char.createChoi(618, player, 43200, 1, false);
                                                                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                                        Database.instance.saveOrtherLog("", player.charname, "mua choi set black friday", "black");
                                                                                    }
                                                                                }
                                                                            } else if (Char.isSuKienBlackFriday()) {
                                                                                int moneyx = 1111;
                                                                                if (moneyx <= 0) {
                                                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua", ""));
                                                                                } else {
                                                                                    if (player.getLuong() < moneyx) {
                                                                                        player.sendMessage(
                                                                                                MessageCreator.createServerAlertMessage("Không đủ " + moneyx + " lượng", "")
                                                                                        );
                                                                                        return;
                                                                                    }

                                                                                    Database.instance
                                                                                            .saveLogThongKeChiTieu("choi bang khong hoa tuyet", 0, 1, (long) moneyx, "luong");
                                                                                    player.subLuong((long) moneyx);
                                                                                    Char.createChoi(617, player, 43200, 1, false);
                                                                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                                    Database.instance.saveOrtherLog("", player.charname, "mua choi bang black friday", "black");
                                                                                }
                                                                            }
                                                                        } else {
                                                                            if (Char.isOpenDauGiaChoiBlackFriday()) {
                                                                                player.sendMessage(
                                                                                        MessageCreator.createMsgMenu(new String[]{"Đồ thời trang", "Lồng đèn"}, idNpc, 7)
                                                                                );
                                                                                return;
                                                                            }

                                                                            player.sendMessage(
                                                                                    MessageCreator.createServerAlertMessage(
                                                                                            "Chỉ có thể đấu giá từ 10h ngày 27/11 đến hết 23h59 ngày 27/11", ""
                                                                                    )
                                                                            );
                                                                        }
                                                                    } else {
                                                                        map.sendListItemSell2Shop(player);
                                                                    }
                                                                } else {
                                                                    VongQuay.moChonLoai(player, idNpc);
                                                                }
                                                            } else if (!player.passWord.equals("")) {
                                                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 3, "Nhập mật khẩu", 0));
                                                            } else {
                                                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 2, "Nhập mật khẩu", 0));
                                                            }
                                                        } else {
                                                            player.getItemResetSock();
                                                            String[] menu = new String[player.itemResetSock.size()];

                                                            for (int ixxx = 0; ixxx < menu.length; ixxx++) {
                                                                Item it = (Item) player.itemResetSock.get(ixxx);
                                                                menu[ixxx] = it.getTemplate().name + " " + it.getMoneyReset();
                                                            }

                                                            player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
                                                        }
                                                    } else {
                                                        player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Dùng lượng"}, idNpc, 3));
                                                    }
                                                } else if (Char.isOpenDauGiaChoiBlackFriday()) {
                                                    player.sendMessage(MessageCreator.createMsgInputText(idOptionMenu != 0 ? -32017 : -32018, 0, "Lượng đấu giá", 1));
                                                    return;
                                                }
                                            } else {
                                                // idMenu 4/5/6 (+103..106) cua NPC -15 = vong quay: real.VongQuay, chan o dau doMenu.
                                            }
                                        } else {
                                            // idMenu 4/5/6 (+103..106) cua NPC -15 = vong quay: real.VongQuay, chan o dau doMenu.
                                        }
                                    } else {
                                        // idMenu 4/5/6 (+103..106) cua NPC -15 = vong quay: real.VongQuay, chan o dau doMenu.
                                    }
                                } else {
                                    // Chi hop le 0 (dung luong mo) hoac 1 (dung luong khoa). Ban cu
                                    // chi chan gia tri DUONG vuot 1 ve 0 ("> 0 -> 0"), khong chan so
                                    // AM: idOptionMenu = -1 giu nguyen -1, lam ca hai chot kiem so du
                                    // luong o buoc xac nhan (== 0 va == 1) deu sai va bi bo qua, trong
                                    // khi nhanh tru tien (!= 0) van chon tru luong khoa - doi he mien
                                    // phi khi khong co lượng khoá.
                                    player.type_luong_doi_he = (byte) (idOptionMenu == 1 ? 1 : 0);

                                    int gia = 100;
                                    if (idOptionMenu == 1) {
                                        gia = 200;
                                    }

                                    player.setItemChange5();
                                    if (player.itemChange5.isEmpty()) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không có vật phẩm nào đổi hệ được trong hành trang.", ""));
                                        return;
                                    }
                                    String[] menu = new String[player.itemChange5.size()];

                                    for (int ixxx = 0; ixxx < menu.length; ixxx++) {
                                        Item it = (Item) player.itemChange5.get(ixxx);
                                        menu[ixxx] = it.getTemplate().name + " " + he[it.heItem] + " (" + gia + " lượng)";
                                    }

                                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                                }
                            } else {
                                if (idOptionMenu < 0 || idOptionMenu >= player.itemResetSock.size()) {
                                    return;
                                }
                                Item it = (Item) player.itemResetSock.get(idOptionMenu);
                                it.resetAtbKham(player);
                            }
                        } else {
                            int gia = 100;
                            int giaLock = 200;
                            if (Char.getDayOpen(0L).equals("2014-06-21")) {
                                Calendar cl = Calendar.getInstance();
                                int iHour = cl.get(11);
                                if (iHour >= 12 && iHour < 13) {
                                    gia = 0;
                                }
                            }

                            if (player.getLuong() < gia && player.type_luong_doi_he == 0) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng.", ""));
                                return;
                            }

                            if (player.getLuongLock() < giaLock && player.type_luong_doi_he == 1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng khoá.", ""));
                                return;
                            }

                            Item it = (Item) player.itemChange5.get(idOptionMenu);
                            int oldHe = it.heItem;

                            for (int count = 0; it.heItem == oldHe && count < 3; count++) {
                                it.heItem = (byte) r.nextInt(5);
                            }

                            if (player.type_luong_doi_he != 0) {
                                player.subLuongLock((long) giaLock);
                                Database.instance.saveLogThongKeChiTieu("Doi He luong khoa", 0, 1, (long) giaLock, "luong_lock");
                            } else {
                                player.subLuong((long) gia);
                                Database.instance.saveLogThongKeChiTieu("Doi He luong", 0, 1, (long) gia, "luong");
                            }

                            player.itemChange5.removeAllElements();
                            Database.instance
                                    .saveOrtherLog(
                                            "tob_log_other_item",
                                            player.charname,
                                            player.getLuong()
                                            + "_doi he_"
                                            + it.getTemplate().name
                                            + "_"
                                            + it.getAttribute()
                                            + "|"
                                            + it.getDBInfo()
                                            + "|"
                                            + he[oldHe]
                                            + " sang "
                                            + he[it.heItem]
                                            + "_"
                                            + (player.type_luong_doi_he != 0 ? giaLock + " lượng khoá" : gia + " lượng"),
                                            "c5"
                                    );
                            player.reCheckAtb5();
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                            player.sendMessage(
                                    MessageCreator.createServerAlertMessage(it.getTemplate().name + " đã được đổi từ " + he[oldHe] + " sang " + he[it.heItem], "")
                            );
                        }
                    } else if (idOptionMenu != 0) {
                        if (idOptionMenu != 1) {
                            if (idOptionMenu != 2) {
                                if (idOptionMenu != 3) {
                                    if (idOptionMenu != 4) {
                                        if (idOptionMenu != 5) {
                                            if (idOptionMenu != 6) {
                                                if (idOptionMenu != 7) {
                                                    if (idOptionMenu != 8) {
                                                        if (idOptionMenu == 9 && Char.getDayOpen(0L).compareTo("2019-12-01") < 0) {
                                                            int moneyx = Database.instance.getMoneyDauGia(player.charname, 2);
                                                            if (moneyx > 0) {
                                                                Database.instance.updateHangDauGia(player.charname, 0);
                                                                player.addLuong(moneyx);
                                                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                                player.sendMessage(
                                                                        MessageCreator.createServerAlertMessage("Bạn nhận lại được " + moneyx + " lượng đã đặt cọc.", "")
                                                                );
                                                                Database.instance.saveOrtherLog("", player.charname, "nhan lai " + moneyx + " luong tien coc", "nhancoc");
                                                            }
                                                        }
                                                    } else if (Char.getDayOpen(0L).compareTo("2019-12-01") < 0) {
                                                        int moneyx = Database.instance.getMoneyDauGia(player.charname, 1);
                                                        if (moneyx <= 0) {
                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua", ""));
                                                        } else {
                                                            int luong = moneyx - moneyx / 2;
                                                            if (player.getLuong() < luong) {
                                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + luong + " lượng", ""));
                                                                return;
                                                            }

                                                            Database.instance.saveLogThongKeChiTieu("choi doc bien hinh", 0, 1, (long) (moneyx - moneyx / 2), "luong");
                                                            Database.instance.updateHangDauGia(player.charname, 0);
                                                            player.subLuong((long) (moneyx - moneyx / 2));
                                                            Char.createChoi(619, player, 0, 1, true);
                                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                        }
                                                    }
                                                } else if (Char.getDayOpen(0L).compareTo("2019-12-01") < 0) {
                                                    int moneyx = Database.instance.getMoneyDauGia(player.charname, 1);
                                                    if (moneyx <= 0) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua", ""));
                                                    } else {
                                                        int luong = moneyx - moneyx / 2;
                                                        if (player.getLuong() < luong) {
                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + luong + " lượng", ""));
                                                            return;
                                                        }

                                                        Database.instance.updateHangDauGia(player.charname, 0);
                                                        Database.instance.saveLogThongKeChiTieu("choi set bien hinh", 0, 1, (long) (moneyx - moneyx / 2), "luong");
                                                        player.subLuong((long) (moneyx - moneyx / 2));
                                                        Char.createChoi(618, player, 0, 1, true);
                                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                    }
                                                }
                                            } else if (Char.getDayOpen(0L).compareTo("2019-12-01") < 0) {
                                                int moneyx = Database.instance.getMoneyDauGia(player.charname, 1);
                                                if (moneyx <= 0) {
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua", ""));
                                                } else {
                                                    int luong = moneyx - moneyx / 2;
                                                    if (player.getLuong() < luong) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + luong + " lượng", ""));
                                                        return;
                                                    }

                                                    Database.instance.saveLogThongKeChiTieu("choi bang bien hinh", 0, 1, (long) (moneyx - moneyx / 2), "luong");
                                                    Database.instance.updateHangDauGia(player.charname, 0);
                                                    player.subLuong((long) (moneyx - moneyx / 2));
                                                    Char.createChoi(617, player, 0, 1, true);
                                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                }
                                            }
                                        } else if (Char.isOpenDauGiaChoiBlackFriday()) {
                                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Đồ thời trang", "Lồng đèn"}, idNpc, 7));
                                            return;
                                        }
                                    } else {
                                        map.sendListItemSell2Shop(player);
                                    }
                                } else {
                                    VongQuay.moChonLoai(player, idNpc);
                                }
                            } else if (!player.passWord.equals("")) {
                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 3, "Nhập mật khẩu", 0));
                            } else {
                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 2, "Nhập mật khẩu", 0));
                            }
                        } else {
                            player.getItemResetSock();
                            if (player.itemResetSock.isEmpty()) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không có vật phẩm nào đã khảm ngọc để tháo.", ""));
                                return;
                            }
                            String[] menu = new String[player.itemResetSock.size()];

                            for (int ixxx = 0; ixxx < menu.length; ixxx++) {
                                Item it = (Item) player.itemResetSock.get(ixxx);
                                menu[ixxx] = it.getTemplate().name + " " + it.getMoneyReset();
                            }

                            player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
                        }
                    } else {
                        player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Dùng lượng"}, idNpc, 3));
                    }
                    break;
                case -13:
                    if (idMenu != 0) {
                        if (idMenu != 1) {
                            if (idMenu != 2) {
                                if (idMenu != 3) {
                                    if (idMenu == 4) {
                                        boolean tc = Skill_Clan.canLearnSkill(player, player.idSkillLearnClan, idOptionMenu, 1);
                                        if (tc) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã học thành công kỹ năng này.", ""));
                                            Database.instance
                                                    .saveLogLearnSkill(player.charname, Skill_Clan.SKILL_NAME_CLAN_MEMBER[player.idSkillLearnClan] + "_" + idOptionMenu);
                                        } else {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể học kỹ năng này", ""));
                                        }
                                    }
                                } else {
                                    player.sendMessage(MessageCreator.createMsgMenu(Skill_Clan.getListSkillClan(1, idOptionMenu), idNpc, 4));
                                    player.idSkillLearnClan = (byte) idOptionMenu;
                                }
                            } else {
                                boolean tc = Skill_Clan.canLearnSkill(player, player.idSkillLearnClan, idOptionMenu, 0);
                                if (tc) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã học thành công kỹ năng này.", ""));
                                    Database.instance.saveLogLearnSkill(player.charname, Skill_Clan.SKILL_NAME_CLAN[player.idSkillLearnClan]);
                                } else {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể học kỹ năng này", ""));
                                }
                            }
                        } else {
                            player.sendMessage(MessageCreator.createMsgMenu(Skill_Clan.getListSkillClan(0, idOptionMenu), idNpc, 2));
                            player.idSkillLearnClan = (byte) idOptionMenu;
                        }
                    } else if (idOptionMenu != 0) {
                        if (idOptionMenu != 1) {
                            if (idOptionMenu == 2) {
                                if (player.idClan == -1) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa tham gia đội.", ""));
                                    return;
                                }

                                if (player.getLuong() < 100) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng.", ""));
                                    return;
                                }

                                player.subLuong(100L);
                                Database.instance.saveLogThongKeChiTieu("Diem Cong Hien", 0, 1, 100L, "luong");
                                NewClan cl = NewClan.getClan(player.idClan);
                                if (cl != null) {
                                    cl.pDevote += 500L;
                                }

                                player.pointCongHienClan += 500;
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã mua 500 điểm cống hiến", ""));
                                Database.instance
                                        .saveLogClan(
                                                player.charname,
                                                "devote",
                                                player.charname + " " + player.getLuong() + " >> " + player.pointCongHienClan + " xu " + cl.name + " | " + cl.money
                                        );
                            }
                        } else {
                            if (player.idClan == -1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa tham gia đội.", ""));
                                return;
                            }

                            player.sendMessage(MessageCreator.createMsgMenu(Skill_Clan.SKILL_NAME_CLAN_MEMBER, idNpc, 3));
                        }
                    } else {
                        if (player.rankClan > 1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng hoặc đội phó mới có thể mua kỹ năng đội.", ""));
                            return;
                        }

                        player.sendMessage(MessageCreator.createMsgMenu(Skill_Clan.SKILL_NAME_CLAN, idNpc, 1));
                    }
                    break;
                case -12:
                    if (!isMapLang(player.map)) {
                        return;
                    }

                    if (idMenu != 0) {
                        if (idMenu == 1) {
                            int[] hMaterial = new int[]{
                                103,
                                104,
                                105,
                                106,
                                107,
                                108,
                                110,
                                111,
                                112,
                                113,
                                114,
                                115,
                                117,
                                118,
                                119,
                                120,
                                121,
                                122,
                                124,
                                125,
                                126,
                                127,
                                128,
                                129,
                                131,
                                132,
                                133,
                                134,
                                135,
                                136
                            };
                            if (player.typeHopMaterial >= 2) {
                                hMaterial = new int[]{230, 231, 232, 233, 234};
                            }

                            int gtempid = hMaterial[idOptionMenu];
                            if (player.typeHopMaterial % 2 != 0) {
                                n = (long) (player.listGemitemLock[gtempid - 1] / 5);
                            } else {
                                n = (long) ((player.listGemitem[gtempid - 1] - player.listGemitemSell[gtempid - 1]) / 5);
                            }

                            if (n <= 0L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + gemTemplate[gtempid - 1].name, ""));
                                return;
                            }

                            if (player.getxu() < n * 5000L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cần " + n * 5000L + " xu", ""));
                                return;
                            }

                            player.subXu(n * 5000L, true, "map 41");
                            if (player.listGemitem[gtempid] == 0 && player.listGemitemLock[gtempid] == 0) {
                                GemItem ge = new GemItem(gtempid);
                                ge.ownerId = player.charDBID;
                                ge.realId = player.getIDItem();
                                player.gemItem.add(ge);
                            }

                            if (player.typeHopMaterial % 2 != 0) {
                                player.listGemitemLock[gtempid] = (int) ((long) player.listGemitemLock[gtempid] + n);
                                player.listGemitemLock[gtempid - 1] = (int) ((long) player.listGemitemLock[gtempid - 1] - n * 5L);
                            } else {
                                player.listGemitem[gtempid] = (int) ((long) player.listGemitem[gtempid] + n);
                                player.listGemitem[gtempid - 1] = (int) ((long) player.listGemitem[gtempid - 1] - n * 5L);
                                player.allGemUse[gtempid - 1] = (int) ((long) player.allGemUse[gtempid - 1] + n * 5L);
                                player.allGemGet[gtempid] = (int) ((long) player.allGemGet[gtempid] + n);
                            }

                            if (player.listGemitem[gtempid - 1] - player.listGemitemSell[gtempid - 1] <= 0 && player.listGemitemLock[gtempid - 1] == 0) {
                                for (int ix = 0; ix < player.gemItem.size(); ix++) {
                                    GemItem g = (GemItem) player.gemItem.get(ix);
                                    if (g.idGemTemplate == gtempid - 1) {
                                        player.removeItemOutVector(g.realId, player.gemItem);
                                        break;
                                    }
                                }
                            }

                            player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã hợp được " + n + " " + gemTemplate[gtempid].name, ""));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            Database.instance
                                    .saveOrtherLog(
                                            "tob_log_other_kham",
                                            player.charname,
                                            n + "_" + player.listGemitem[gtempid] + "_" + player.listGemitem[gtempid - 1] + "_" + gemTemplate[gtempid].name,
                                            "hopnguyenlieu"
                                    );
                        }
                    } else {
                        player.typeHopMaterial = (byte) idOptionMenu;
                        if (idOptionMenu >= 2) {
                            int[] tempid = new int[]{230, 231, 232, 233, 234};
                            String[] menu = new String[tempid.length];

                            for (int ixx = 0; ixx < tempid.length; ixx++) {
                                menu[ixx] = gemTemplate[tempid[ixx]].name + (idOptionMenu != 3 ? "" : " khóa");
                            }

                            player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                        } else {
                            int[] tempid = new int[]{
                                103,
                                104,
                                105,
                                106,
                                107,
                                108,
                                110,
                                111,
                                112,
                                113,
                                114,
                                115,
                                117,
                                118,
                                119,
                                120,
                                121,
                                122,
                                124,
                                125,
                                126,
                                127,
                                128,
                                129,
                                131,
                                132,
                                133,
                                134,
                                135,
                                136
                            };
                            String[] menu = new String[tempid.length];

                            for (int ixx = 0; ixx < tempid.length; ixx++) {
                                menu[ixx] = gemTemplate[tempid[ixx]].name + (idOptionMenu != 1 ? "" : " khóa");
                            }

                            player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                        }
                    }
                    break;
                case -11:
                    if (!isMapLang(player.map)) {
                        return;
                    }

                    if (idMenu != 0) {
                        if (idMenu == 1) {
                            int[] lMaterial = new int[]{
                                68, 69, 70, 71, 72, 73, 75, 76, 77, 78, 79, 80, 82, 83, 84, 85, 86, 87, 89, 90, 91, 92, 93, 94, 96, 97, 98, 99, 100, 101
                            };
                            if (player.typeHopMaterial >= 2 && player.typeHopMaterial < 4) {
                                lMaterial = new int[]{236, 237, 238, 239, 240};
                            } else if (player.typeHopMaterial > 3) {
                                lMaterial = new int[]{247, 248, 249};
                            }

                            int gtempidx = lMaterial[idOptionMenu];
                            if (player.typeHopMaterial % 2 != 0) {
                                n = (long) (player.listGemitemLock[gtempidx - 1] / 5);
                            } else {
                                n = (long) ((player.listGemitem[gtempidx - 1] - player.listGemitemSell[gtempidx - 1]) / 5);
                            }

                            if (n <= 0L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + gemTemplate[gtempidx - 1].name, ""));
                                return;
                            }

                            if (player.getxu() < n * 5000L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cần " + n * 5000L + " xu", ""));
                                return;
                            }

                            player.subXu(n * 5000L, true, "map 40");
                            if (player.listGemitem[gtempidx] == 0 && player.listGemitemLock[gtempidx] == 0) {
                                GemItem ge = new GemItem(gtempidx);
                                ge.ownerId = player.charDBID;
                                ge.realId = player.getIDItem();
                                player.gemItem.add(ge);
                            }

                            if (player.typeHopMaterial % 2 != 0) {
                                player.listGemitemLock[gtempidx] = (int) ((long) player.listGemitemLock[gtempidx] + n);
                                player.listGemitemLock[gtempidx - 1] = (int) ((long) player.listGemitemLock[gtempidx - 1] - n * 5L);
                                player.allGemGetLock[gtempidx] = (int) ((long) player.allGemGetLock[gtempidx] + n);
                                player.allGemUseLock[gtempidx - 1] = (int) ((long) player.allGemUseLock[gtempidx - 1] + n * 5L);
                            } else {
                                player.listGemitem[gtempidx] = (int) ((long) player.listGemitem[gtempidx] + n);
                                player.listGemitem[gtempidx - 1] = (int) ((long) player.listGemitem[gtempidx - 1] - n * 5L);
                                player.allGemUse[gtempidx - 1] = (int) ((long) player.allGemUse[gtempidx - 1] + n * 5L);
                                player.allGemGet[gtempidx] = (int) ((long) player.allGemGet[gtempidx] + n);
                            }

                            if (player.listGemitem[gtempidx - 1] - player.listGemitemSell[gtempidx - 1] <= 0 && player.listGemitemLock[gtempidx - 1] == 0) {
                                for (int ixx = 0; ixx < player.gemItem.size(); ixx++) {
                                    GemItem g = (GemItem) player.gemItem.get(ixx);
                                    if (g.idGemTemplate == gtempidx - 1) {
                                        player.removeItemOutVector(g.realId, player.gemItem);
                                        break;
                                    }
                                }
                            }

                            player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã hợp được " + n + " " + gemTemplate[gtempidx].name, ""));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            Database.instance
                                    .saveOrtherLog(
                                            "tob_log_other_kham",
                                            player.charname,
                                            n + "_" + player.listGemitem[gtempidx] + "_" + player.listGemitem[gtempidx - 1] + "_" + gemTemplate[gtempidx].name,
                                            "hopnguyenlieu"
                                    );
                        }
                    } else {
                        player.typeHopMaterial = (byte) idOptionMenu;
                        if (idOptionMenu >= 2) {
                            if (idOptionMenu >= 4) {
                                if (idOptionMenu == 4 || idOptionMenu == 5) {
                                    int[] tempid = new int[]{247, 248, 249};
                                    String[] menu = new String[tempid.length];

                                    for (int ixxx = 0; ixxx < tempid.length; ixxx++) {
                                        menu[ixxx] = gemTemplate[tempid[ixxx]].name + (idOptionMenu != 5 ? "" : " khóa");
                                    }

                                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                                }
                            } else {
                                int[] tempid = new int[]{236, 237, 238, 239, 240};
                                String[] menu = new String[tempid.length];

                                for (int ixxx = 0; ixxx < tempid.length; ixxx++) {
                                    menu[ixxx] = gemTemplate[tempid[ixxx]].name + (idOptionMenu != 3 ? "" : " khóa");
                                }

                                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                            }
                        } else {
                            int[] tempid = new int[]{
                                68, 69, 70, 71, 72, 73, 75, 76, 77, 78, 79, 80, 82, 83, 84, 85, 86, 87, 89, 90, 91, 92, 93, 94, 96, 97, 98, 99, 100, 101
                            };
                            String[] menu = new String[tempid.length];

                            for (int ixxx = 0; ixxx < tempid.length; ixxx++) {
                                menu[ixxx] = gemTemplate[tempid[ixxx]].name + (idOptionMenu != 1 ? "" : " khóa");
                            }

                            player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                        }
                    }
                    break;
                case -10:
                    if (!isMapLang(player.map)) {
                        return;
                    }
                    // Menu goc da bo muc "Dang ky giai Lien minh" (RequestNPCInfoHandler): hien thi -> vi tri goc.
                    if (idMenu == 0 && player.boMucTongQuan >= 0 && idOptionMenu >= player.boMucTongQuan) {
                        idOptionMenu++;
                    }

                    if (idMenu != 0) {
                        if (idMenu == 1) {
                            int[] temid = new int[]{61, 62, 63, 64, 65};
                            int gid = temid[idOptionMenu];
                            n = (long) ((player.listGemitem[gid - 1] - player.listGemitemSell[gid - 1]) / 5);
                            if (n <= 0L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + gemTemplate[gid - 1].name, ""));
                                return;
                            }

                            if (player.getxu() < n * 5000L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cần " + n * 5000L + " xu", ""));
                                return;
                            }

                            player.subXu(n * 5000L, true, "map 42");
                            if (player.listGemitem[gid] == 0) {
                                GemItem ge = new GemItem(gid);
                                ge.ownerId = player.charDBID;
                                ge.realId = player.getIDItem();
                                player.gemItem.add(ge);
                            }

                            player.listGemitem[gid] = (int) ((long) player.listGemitem[gid] + n);
                            player.listGemitem[gid - 1] = (int) ((long) player.listGemitem[gid - 1] - n * 5L);
                            if (player.listGemitem[gid - 1] - player.listGemitemSell[gid - 1] <= 0) {
                                for (int ixxxxx = 0; ixxxxx < player.gemItem.size(); ixxxxx++) {
                                    GemItem gx = (GemItem) player.gemItem.get(ixxxxx);
                                    if (gx.idGemTemplate == gid - 1) {
                                        player.removeItemOutVector(gx.realId, player.gemItem);
                                        break;
                                    }
                                }
                            }

                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            Database.instance
                                    .saveOrtherLog(
                                            "tob_log_other_kham",
                                            player.charname,
                                            n + "_" + player.listGemitem[gid] + "_" + player.listGemitem[gid - 1] + "_" + gemTemplate[gid].name,
                                            "hopnguyenlieu"
                                    );
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã hợp được " + n + " " + gemTemplate[gid].name, ""));
                        } else {
                            if (idMenu != 2) {
                                if (idMenu == 3) {
                                    if ((TeamServer.isServerHoaLu() || TeamServer.isServerLocal()) && Char.isSuKienGioTo2016()) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng sẽ mở vào ngày 9/4/2020", ""));
                                        return;
                                    }

                                    if (player.gifLuckyBox.size() > 2) {
                                        player.sendMessage(MessageCreator.createMsgPopUp(player.id, 5, "Lượt chơi chưa kết thúc. Bạn có muốn mở hết hộp không?"));
                                        return;
                                    }

                                    if (player.gifLuckyBox.size() <= 2) {
                                        player.gifLuckyBox.removeAll(player.gifLuckyBox);
                                        player.nSpecialBox = 2;
                                    }

                                    int[][] moneyx = new int[][]{{200000, 500000, 1000000}, {20, 35, 50}};
                                    if (player.typeBox != 0) {
                                        if (player.getLuong() < moneyx[player.typeBox][idOptionMenu]) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                            return;
                                        }

                                        player.subLuong((long) moneyx[player.typeBox][idOptionMenu]);
                                        Database.instance.saveLogThongKeChiTieu("Luky box luong", 0, 1, (long) moneyx[player.typeBox][idOptionMenu], "luong");
                                    } else {
                                        if (player.getxu() < (long) moneyx[player.typeBox][idOptionMenu]) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                            return;
                                        }

                                        player.subXu((long) moneyx[player.typeBox][idOptionMenu], true, "map 43");
                                        Database.instance.saveLogThongKeChiTieu("Luky box xu", 0, 1, (long) moneyx[player.typeBox][idOptionMenu], "xu");
                                    }

                                    player.createGifLuckyBox((byte) (idOptionMenu * 2 + 5), player.typeBox);
                                    player.sendMessage(MessageCreator.createLuckyBox((byte) (idOptionMenu * 2 + 5), player.getInfoGifLucky(), 0));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    if (player.typeBox != 0) {
                                        if (player.typeBox == 1) {
                                            allLuongLucky = allLuongLucky + (long) moneyx[player.typeBox][idOptionMenu];
                                        }
                                    } else {
                                        allXuLucky = allXuLucky + (long) moneyx[player.typeBox][idOptionMenu];
                                    }

                                    Database.instance
                                            .saveOrtherLog(
                                                    "",
                                                    player.charname,
                                                    "Tham gia luckyBox " + moneyx[player.typeBox][idOptionMenu] + " " + (player.typeBox != 0 ? "luong" : "xu"),
                                                    "joinLucky"
                                            );
                                    break;
                                }

                                if (idMenu != 4) {
                                    if (idMenu == 5 || idMenu == 6) {
                                        break;
                                    }

                                    if (idMenu != 7) {
                                        if (idMenu != 8 && idMenu != 9) {
                                            if (idMenu != 10 && idMenu != 11 && idMenu != 12 && idMenu != 13 && idMenu != 14) {
                                                if (idMenu >= 20) {
                                                    if (player.canReceiveGiftEvent()) {
                                                        MapQuaTang.doReceiveGiftEvent5Star(map, player, idNpc, idMenu, idOptionMenu);
                                                        return;
                                                    }

                                                    if (player.canReceiveGiftOffline < 0) {
                                                        return;
                                                    }

                                                    map.doReceiveGiftTopTet2021(player, idNpc, idMenu, idOptionMenu);
                                                }
                                            } else if (player.canReceiveGiftOffline == 20
                                                    || player.canReceiveGiftOffline == 30
                                                    || player.canReceiveGiftOffline == 40
                                                    || player.canReceiveGiftOffline == 50) {
                                                int[] idTrung = new int[]{684, 685, 686, 687, 688};
                                                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idTrung[idMenu - 10]);
                                                Item newItem = null;
                                                newItem = new Item(template, false, 0, 0, template.id);
                                                newItem.level = newItem.getTemplate().level;
                                                newItem.clazz = 0;
                                                newItem.dateCreate = Char.getDayTime(0L);
                                                player.iItems.add(newItem);
                                                Item it = null;
                                                if (player.canReceiveGiftOffline < 50) {
                                                    it = Char.createPhiPhongMaxBaoKich(
                                                            player, idOptionMenu, 0, null, true, false, player.canReceiveGiftOffline >= 40 ? 0 : 15, false
                                                    );
                                                }

                                                player.sendMessage(
                                                        MessageCreator.createServerAlertMessage(
                                                                "Nhận được 1 " + newItem.getName() + (it == null ? "" : " và " + it.getName()), ""
                                                        )
                                                );
                                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                                Database.instance.saveOrtherLog("", player.charname, "nhan qua top " + player.canReceiveGiftOffline, "quatop");
                                                player.canReceiveGiftOffline = -1;
                                                Database.instance.updateStateReceiveGiftEventOutGame(player.charname);
                                            }
                                        }
                                        break;
                                    }

                                    if (player.isFullInventory()) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                                        break;
                                    }

                                    if ((player.canReceiveGiftOffline < 1 || player.canReceiveGiftOffline > 5)
                                            && player.canReceiveGiftOffline != 20
                                            && player.canReceiveGiftOffline != 30
                                            && player.canReceiveGiftOffline != 40
                                            && player.canReceiveGiftOffline != 50) {
                                        break;
                                    }

                                    if (player.canReceiveGiftOffline != 1) {
                                        if (player.canReceiveGiftOffline != 2) {
                                            if (player.canReceiveGiftOffline != 3) {
                                                if (player.canReceiveGiftOffline != 4) {
                                                    if (player.canReceiveGiftOffline != 5) {
                                                        if (player.canReceiveGiftOffline == 20
                                                                || player.canReceiveGiftOffline == 30
                                                                || player.canReceiveGiftOffline == 40
                                                                || player.canReceiveGiftOffline == 50) {
                                                            player.sendMessage(
                                                                    MessageCreator.createMsgMenu(new String[]{"Ruy băng đặc biệt", "Ruy băng vật lý"}, idNpc, 10 + idOptionMenu)
                                                            );
                                                            return;
                                                        }
                                                    } else {
                                                        Item it = Char.createPhiPhongMaxBaoKich(
                                                                player, idOptionMenu >= 2 ? idOptionMenu - 2 : idOptionMenu, 0, null, true, idOptionMenu < 2, 0, true
                                                        );
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + it.getName(), ""));
                                                    }
                                                } else {
                                                    Item it = Char.createPhiPhongMaxBaoKich(
                                                            player, idOptionMenu >= 2 ? idOptionMenu - 2 : idOptionMenu, 0, null, true, idOptionMenu < 2, 0, true
                                                    );
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + it.getName(), ""));
                                                }
                                            } else {
                                                map.doCreateHoaKyLan(player, 683, 43200L);
                                                Item it = Char.createPhiPhongMaxBaoKich(
                                                        player, idOptionMenu >= 2 ? idOptionMenu - 2 : idOptionMenu, 0, null, true, idOptionMenu < 2, 0, false
                                                );
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được Ponyta 1 tháng và " + it.getName(), ""));
                                            }
                                        } else {
                                            map.doCreateHoaKyLan(player, 683, 129600L);
                                            Item it = Char.createPhiPhongMaxBaoKich(
                                                    player, idOptionMenu >= 2 ? idOptionMenu - 2 : idOptionMenu, 0, null, true, idOptionMenu < 2, 0, false
                                            );
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được Ponyta 3 tháng và " + it.getName(), ""));
                                        }
                                    } else {
                                        player.potions[71]++;
                                        Item it = Char.createPhiPhongMaxBaoKich(
                                                player, idOptionMenu >= 2 ? idOptionMenu - 2 : idOptionMenu, 0, null, true, idOptionMenu < 2, 15, false
                                        );
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được 1 trứng Ponyta và " + it.getName(), ""));
                                    }

                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    Database.instance.saveOrtherLog("", player.charname, "nhan qua top " + player.canReceiveGiftOffline, "quatop");
                                    player.canReceiveGiftOffline = -1;
                                    Database.instance.updateStateReceiveGiftEventOutGame(player.charname);
                                    break;
                                }

                                if (player.idClan > -1) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể chọn lãnh th", ""));
                                    return;
                                }

                                if (player.monster != null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đổi vùng khi đang giao hàng", ""));
                                    return;
                                }

                                if (!isMapLang(player.map)) {
                                    return;
                                }

                                if (player.monster != null) {
                                    return;
                                }

                                if (player.getLuong() < 300) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                                    return;
                                }

                                byte oldCountryx = player.myCountry;
                                if (checkFullNation(player.myCountry != 1 ? 1 : 0, 1, player.lvDetail.lv)) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    Map.nameCountry[player.myCountry != 1 ? 1 : 0] + " lãnh đã có quá nhiều người. Vui lòng chọn vùng khác.", ""
                                            )
                                    );
                                    return;
                                }

                                map.playerExit(player);
                                player.subLuong(300L);
                                Database.instance.saveLogThongKeChiTieu("chuyen lanh tho", 0, 1, 300L, "luong");
                                player.myCountry = (byte) (player.myCountry != 1 ? 1 : 0);
                                player.inCountry = player.myCountry;
                                allUser[player.myCountry != 1 ? 1 : 0]++;
                                player.sendMessage(MessageCreator.createMessageLocation(real.LangKhoiDau.oCong(player, player.myCountry)));
                                int mapID = mapstart[player.myCountry][r.nextInt(2)];
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                map.move2Map(player, 16 + Database.r.nextInt() % 5, 24 + Database.r.nextInt(20), mapID, player.myCountry);
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã gia nhập " + Map.nameCountry[player.myCountry], ""));
                                Database.instance
                                        .saveOrtherLog(
                                                "",
                                                player.charname,
                                                player.charname + "_ từ" + Map.nameCountry[oldCountryx] + " chuyển sang " + Map.nameCountry[player.myCountry],
                                                "csnation"
                                        );
                                break;
                            }

                            player.typeBox = (byte) idOptionMenu;
                            String[][] menu = new String[][]{{"5 hộp (200.000 xu)", "7 hộp (500.000 xu)", "9 hộp (1.000.000 xu)"}, {"5 hộp (20 lượng)", "7 hộp (35 lượng)", "9 hộp (50 lượng)"}};
                            player.sendMessage(MessageCreator.createMsgMenu(menu[idOptionMenu], idNpc, 3));
                        }
                    } else if (idOptionMenu != 0) {
                        if (idOptionMenu != 1) {
                            if (idOptionMenu != 2) {
                                if (idOptionMenu != 3) {
                                    if (idOptionMenu != 4) {
                                        if (idOptionMenu != 5) {
                                            if (idOptionMenu == 6) {
                                                if (!TeamServer.isServerLienDau()) {
                                                    if (!player.canReceiveGiftEvent()) {
                                                        if (player.canReceiveGiftOffline >= 0) {
                                                            map.doReceiveGiftTopTet2021(player, idNpc, idMenu, idOptionMenu);
                                                            return;
                                                        }

                                                        return;
                                                    }

                                                    MapQuaTang.doReceiveGiftEvent5Star(map, player, idNpc, idMenu, idOptionMenu);
                                                    return;
                                                }

                                                int[] idmap = new int[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
                                                Vector<String> name = new Vector();

                                                for (int ixxxxxx = 0; ixxxxxx < idmap.length; ixxxxxx++) {
                                                    Map mapDich = (Map) RealController.mapList.get(idmap[ixxxxxx]);
                                                    if (mapDich != null) {
                                                        name.add(
                                                                MapLienDau.name_town[ixxxxxx]
                                                                + (!mapDich.isStartLienDau()
                                                                ? (!MapLienDau.NAME_WIN[ixxxxxx].equals("") ? "( " + MapLienDau.NAME_WIN[ixxxxxx] + " thắng )" : "")
                                                                : "( đang đấu )")
                                                        );
                                                    }
                                                }

                                                player.sendMessage(MessageCreator.createMsgMenu(name, -5000, 0));
                                            }
                                        } else {
                                            // thanh vien: dang ky lien dau xep hang.
                                            if (com.htbb.pokemon.ThanhVien.chanVaBao(player, "pvp_xep_hang")) {
                                                return;
                                            }
                                            if (!openLog) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang phát triển.", ""));
                                                return;
                                            }

                                            if (TeamServer.isServerLienDau()) {
                                                int[] idmap = new int[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39};

                                                for (int ixxxxxxx = 0; ixxxxxxx < idmap.length; ixxxxxxx++) {
                                                    Map mapDich = (Map) RealController.mapList.get(idmap[ixxxxxxx]);
                                                    if (mapDich != null && mapDich.isStartLienDau() && !mapDich.checkFullMember(player.myCountry)) {
                                                        player.map.move2Map(player, 39 + r.nextInt() % 10, 90 - r.nextInt(10), idmap[ixxxxxxx], player.myCountry);
                                                        return;
                                                    }
                                                }

                                                player.sendMessage(MessageCreator.createServerAlertMessage("Chiến trường đã đầy", ""));
                                            } else {
                                                if (isDayOpenLiendau()) {
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đăng ký trong ngày diễn ra giải Liên minh", ""));
                                                    return;
                                                }

                                                if (player.lvDetail.lv < 50) {
                                                    player.sendMessage(
                                                            MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 50 trở lên mới có thể tham gia giải Liên minh", "")
                                                    );
                                                    return;
                                                }

                                                InfoCharLienDau info = Database.instance
                                                        .addChar1(player.getSession(), player.charname, player.headStyle, player.charClass, player.gender, player.myCountry);
                                                int[] data = info.data;
                                                if (Database.instance.isCharRegisterLiendau(player.charname)) {
                                                    Database.instance.saveCharLienDau(player, data);
                                                    Database.instance.saveOrtherLog("", player.charname, "cap nhat thong tin lien dau", "capnhat");
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Cập nhật thành công", ""));
                                                    return;
                                                }

                                                if (data[1] > -1) {
                                                    player.sendMessage(
                                                            MessageCreator.createServerAlertMessage("Một tài khoản chỉ có thể đăng ký 1 Pokémon tham gia giải Liên minh", "")
                                                    );
                                                    return;
                                                }

                                                Database.instance.saveCharLienDau(player, data);
                                                Database.instance.saveOrtherLog("", player.charname, "dang ky tham gia lien server", "liendau");
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Đăng ký thành công", ""));
                                            }
                                        }
                                    } else {
                                        player.sendMessage(MessageCreator.createMsgInputText(player.id, 7, "Nhập mã", 0));
                                    }
                                } else {
                                    if (player.idClan > -1) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có người chưa tham gia đội mới có thể chuyển vùng", ""));
                                        return;
                                    }

                                    if (!isMapLang(player.map)) {
                                        return;
                                    }

                                    String[][] newvilage = new String[][]{{"Chuyển sang " + Map.nameCountry[1]}, {"Chuyển sang " + Map.nameCountry[0]}};
                                    player.sendMessage(MessageCreator.createMsgMenu(newvilage[player.myCountry], idNpc, 4));
                                }
                            } else {
                                String[] menu = new String[]{"Hộp bạc", "Hộp vàng"};
                                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
                            }
                        } else if (player.maxBag >= Char.MAX_BAG_LIMIT) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mở rộng thêm.", ""));
                        } else {
                            int lb = 150;
                            if (player.maxBag > 1) {
                                lb = 500;
                            }

                            if (player.getLuong() < lb) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có ít nhất " + lb + " lượng", ""));
                                return;
                            }

                            player.luongOpenInventory = (short) lb;
                            player.sendMessage(MessageCreator.createMsgPopUp(player.id, 4, "Bạn thật sự muốn mở rộng hành trang với giá " + lb + " lượng?"));
                        }
                    } else {
                        String[] menu = new String[]{
                            "Đục tương khảm cấp 2", "Đục tương khảm cấp 3", "Đục tương khảm cấp 4", "Đục tương khảm cấp 5", "Đục tương khảm cấp 6"
                        };
                        player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                    }
                    break;
                case -9:
                    if (player.monster == null) {
                        if (player.map.mapIDLoadMap != 17) {
                            return;
                        }

                        if (abs(player.x / 16 - 99) <= rangeNpc && abs(player.y / 16 - 54) <= rangeNpc) {
                            int[][] vilage = LangKhoiDau.bang(player);
                            int[] tmap = vilage[player.inCountry];
                            if (idOptionMenu < 0 || idOptionMenu >= tmap.length) {
                                return;
                            }
                            // Ve tau bang luong (them 22/09/2026, theo yeu cau nguoi dung):
                            // truoc day di xa phu hoan toan mien phi, gio tru 20 luong/chuyen.
                            if (!player.truLuongNeuDu(GIA_VE_TAU_LUONG)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage(
                                        "Cần có " + GIA_VE_TAU_LUONG + " lượng để mua vé tàu.", ""));
                                return;
                            }
                            int x = 16 + r.nextInt() % 5;
                            int y = 24 + r.nextInt(20);
                            player.x = x;
                            player.y = y;
                            map.move2Map(player, x, y, tmap[idOptionMenu], player.inCountry);
                            break;
                        }

                        return;
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang giao hàng", ""));
                    return;
                case -8:
                    if (!isMapThanh(player.map) && !isMapLang(player.map)) {
                        return;
                    }

                    MapCheTao.doCreateItem(map, player, idNpc, idMenu, idOptionMenu);
                    break;
                case -6:
                    if (idMenu != 0) {
                        if (idMenu != 1) {
                            if (idMenu != 2) {
                                if (idMenu == 3) {
                                    if (player.animalRide == null) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cưỡi thú mà bạn muốn luyện", ""));
                                        return;
                                    }

                                    if (idOptionMenu != 0) {
                                        if (idOptionMenu == 1) {
                                            if (player.getLuong() < Animal.price[2][player.animalRide.level]) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                                return;
                                            }

                                            String oldInfo = player.animalRide.level + "_" + player.animalRide.getAttribute();
                                            if (!player.animalRide.upAttribute(1)) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Đã đạt max cấp độ hiện tại", ""));
                                            } else {
                                                player.subLuong((long) Animal.price[2][player.animalRide.level]);
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thành công", ""));
                                                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 100));
                                                Database.instance
                                                        .saveOrtherLog("tob_log_other_animal", player.charname, oldInfo + "|" + player.animalRide.getAttribute(), "ndt");
                                                Database.instance.saveAnimal(player.animalRide);
                                                Database.instance
                                                        .saveLogThongKeChiTieu("nang cap thuoc tinh thu", 0, 1, (long) Animal.price[2][player.animalRide.level], "luong");
                                            }
                                        }
                                    } else {
                                        if (player.getLuong() < 10) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                            return;
                                        }

                                        if (player.animalRide.changeAtt(player, 1)) {
                                            player.subLuong(10L);
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Thay đổi thành công", ""));
                                            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 100));
                                            Database.instance.saveAnimal(player.animalRide);
                                            Database.instance.saveLogThongKeChiTieu("thay doi thuoc tinh thu", 0, 1, 10L, "luong");
                                        }
                                    }
                                }
                            } else {
                                if (player.animalRide == null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cưỡi thú mà bạn muốn luyện", ""));
                                    return;
                                }

                                if (idOptionMenu != 0) {
                                    if (idOptionMenu != 1) {
                                        if (idOptionMenu == 2) {
                                            String[] menu = new String[]{"Thay đổi(10lượng)", "Nâng cấp (" + Animal.price[2][player.animalRide.level] + "lượng)"};
                                            Message msg = MessageCreator.createMsgMenu(menu, idNpc, 3);
                                            player.sendMessage(msg);
                                        }
                                    } else {
                                        if (player.getLuong() < Animal.price[1][player.animalRide.level]) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                            return;
                                        }

                                        String oldInfo = player.animalRide.level + "_" + player.animalRide.getAttribute();
                                        if (!player.animalRide.upAttribute(0)) {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã đạt max cấp độ hiện tại", ""));
                                        } else {
                                            player.subLuong((long) Animal.price[1][player.animalRide.level]);
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thành công", ""));
                                            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 100));
                                            Database.instance
                                                    .saveOrtherLog(
                                                            "tob_log_other_animal",
                                                            player.charname,
                                                            oldInfo + "|" + player.animalRide.getAttribute() + "_" + player.getLuong(),
                                                            "nct"
                                                    );
                                            Database.instance.saveAnimal(player.animalRide);
                                            Database.instance
                                                    .saveLogThongKeChiTieu("nang cap thuoc tinh thu", 0, 1, (long) Animal.price[1][player.animalRide.level], "luong");
                                        }
                                    }
                                } else {
                                    if (player.getLuong() < Animal.price[0][player.animalRide.level]) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                        return;
                                    }

                                    String oldInfo = player.animalRide.level + "_" + player.animalRide.getAttribute();
                                    if (!player.animalRide.upAttribute(2)) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Đã đạt max cấp độ hiện tại", ""));
                                    } else {
                                        player.subLuong((long) Animal.price[0][player.animalRide.level]);
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thành công", ""));
                                        player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 100));
                                        Database.instance
                                                .saveOrtherLog(
                                                        "tob_log_other_animal",
                                                        player.charname,
                                                        oldInfo + "|" + player.animalRide.getAttribute() + "|" + player.animalRide.name + "|" + "_" + player.getLuong(),
                                                        "ncc"
                                                );
                                        Database.instance.saveAnimal(player.animalRide);
                                        Database.instance.saveLogThongKeChiTieu("nang cap thuoc tinh thu", 0, 1, (long) Animal.price[0][player.animalRide.level], "luong");
                                    }
                                }
                            }
                        } else {
                            Animal anima = null;
                            switch (idOptionMenu) {
                                case 0:
                                    if (player.potions[64] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[64]--;
                                    anima.name = "Machamp";
                                    anima.idImg = 2;
                                    break;
                                case 1:
                                    if (player.potions[65] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[65]--;
                                    anima.name = "Arcanine";
                                    anima.idImg = 1;
                                    break;
                                case 2:
                                    if (player.potions[66] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[66]--;
                                    anima.name = "Sói xám";
                                    anima.idImg = 3;
                                    break;
                                case 3:
                                    if (player.potions[67] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[67]--;
                                    anima.name = "Pidgeot";
                                    anima.idImg = 4;
                                    break;
                                case 4:
                                    if (player.potions[68] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[68]--;
                                    anima.name = "Rapidash";
                                    anima.idImg = 0;
                                    break;
                                case 5:
                                    if (player.potions[86] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[86]--;
                                    anima.name = "Moltres";
                                    anima.idImg = 5;
                                    break;
                                case 6:
                                    if (player.potions[70] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[70]--;
                                    anima.name = "Articuno";
                                    anima.idImg = 8;
                                    break;
                                case 7:
                                    if (player.potions[74] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[74]--;
                                    anima.name = "Spectrier";
                                    anima.idImg = Animal.NGUA_XUONG;
                                    break;
                                case 8:
                                    if (player.potions[72] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[72]--;
                                    anima.name = "Piloswine";
                                    anima.idImg = Animal.HEO;
                                    break;
                                case 9:
                                    if (player.potions[115] <= 0) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    anima = new Animal();
                                    player.potions[115]--;
                                    anima.name = "Suicune";
                                    anima.idImg = Animal.LAN;
                                    break;
                                case 10:
                                    // Rong bang: trung la VAT PHAM 887 trong hanh trang, khong phai
                                    // potion nhu 10 truong hop tren, nen tu ap roi return luon.
                                    Item trungRongBang = getTrungRongBang(player);
                                    if (trungRongBang == null) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có trứng thú cưỡi", ""));
                                        return;
                                    }

                                    map.doApTrungRongBang(player, trungRongBang);
                                    return;
                            }

                            if (anima != null) {
                                anima.createAtt();
                                anima.level = 1;
                                anima.place = 0;
                                anima.ownerId = player.charDBID;
                                anima.id = player.getIDItem();
                                player.animal.add(anima);
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                                Database.instance.saveAnimal(anima);
                                Database.instance
                                        .saveOrtherLog(
                                                "tob_log_other_animal", player.charname, "Tao " + anima.name + " | " + anima.getAttribute() + "_" + player.getLuong(), "crt"
                                        );
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã tạo được thú cưỡi " + anima.name, ""));
                            }
                        }
                    } else if (idOptionMenu != 0) {
                        switch (idOptionMenu) {
                            case 1:
                                if (player.animalRide == null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cưỡi thú mà bạn muốn luyện", ""));
                                    return;
                                }
                    
                                if (player.animalRide.idImg == 7) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Thú cưỡi đã đạt max chỉ số.", ""));
                                    return;
                                }
                    
                                String[] menu = new String[]{
                                    "Cơ bản (" + Animal.price[0][player.animalRide.level] + " lượng)",
                                    "Tiềm năng(" + Animal.price[0][player.animalRide.level] + " lượng)",
                                    "Đặc biệt"
                                };
                                Message msg = MessageCreator.createMsgMenu(menu, idNpc, 2);
                                player.sendMessage(msg);
                                break;
                    
                            case 2:
                                if (player.animalRide == null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cưỡi thú mà bạn muốn luyện", ""));
                                    return;
                                }
                    
                                if (player.getLuong() < Animal.price[3][player.animalRide.level]) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                                    return;
                                }
                    
                                if (player.animalRide.upLevel(player)) {
                                    player.subLuong((long) Animal.price[3][player.animalRide.level - 1]);
                                    Database.instance.saveLogThongKeChiTieu("nang cap level thu", 0, 1, (long) Animal.price[3][player.animalRide.level - 1], "luong");
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng, thú cưỡi của bạn đã đạt được cấp độ " + player.animalRide.level, ""));
                                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 100));
                                    Database.instance.saveAnimal(player.animalRide);
                                    return;
                                }
                    
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa thể nâng cấp thêm. " + player.animalRide.level, ""));
                                break;
                            case 3:
                            if (player.animalRide == null ) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải cưỡi thú mà bạn muốn hóa hình", ""));
                                return;
                            }
                            player.animalRide.doHoaHinh(player);

                            break;
                            case 4:
                                if (player.animalRide == null ||
                                    (!player.animalRide.isPhuongHoangBang() && !player.animalRide.isPhuongHoangLua()
                                        && !player.animalRide.isLan() && !player.animalRide.isRongLon()) ||
                                    player.animalRide.level < 4 ||
                                    player.getLuong() < 200) {
                                    return;
                                }
                    
                                Database.instance.saveLogThongKeChiTieu("nang cap khang " + player.animalRide.getName(), 0, 1, 200L, "luong");
                    
                                if (player.animalRide.isPhuongHoangBang()) {
                                    if (player.animalRide.KHANG_BI_CHAY > 0) return;
                                    player.subLuong(200L);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.animalRide.KHANG_BI_CHAY = 95;
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Thú cưỡi Articuno đã được thêm thuộc tính kháng bị cháy", ""));
                                    Database.instance.saveAnimal(player.animalRide);
                                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                    Database.instance.saveOrtherLog("", player.charname, "them dong khang bi chay cho phb 200L", "kchay");
                                } else if (player.animalRide.isPhuongHoangLua()) {
                                    if (player.animalRide.KHANG_DONG_BANG > 0) return;
                                    player.subLuong(200L);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.animalRide.KHANG_DONG_BANG = 95;
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Thú cưỡi Moltres đã được thêm thuộc tính kháng đóng băng", ""));
                                    Database.instance.saveAnimal(player.animalRide);
                                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                    Database.instance.saveOrtherLog("", player.charname, "them dong khang dong bang cho phl 200L", "kdongbang");
                                } else if (player.animalRide.isRongLon()) {
                                    // Rong bang mo CA HAI dong khang trong mot lan 200 luong: phan
                                    // combat da viet san cho no o ca hai (Char.khamKhangBang,
                                    // Char.java:6525 dong bang, Char.java:6601 bi chay) va bang mo ta
                                    // cua thu cung hua ca hai. Truoc day khong co duong nao bat co,
                                    // nen 95% hien tren bang chi so la con so vo nghia.
                                    if (player.animalRide.KHANG_BI_CHAY > 0 && player.animalRide.KHANG_DONG_BANG > 0) return;
                                    player.subLuong(200L);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.animalRide.KHANG_BI_CHAY = 95;
                                    player.animalRide.KHANG_DONG_BANG = 95;
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Thú cưỡi Dragonite đã được thêm thuộc tính kháng bị cháy và kháng đóng băng", ""));
                                    Database.instance.saveAnimal(player.animalRide);
                                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                    Database.instance.saveOrtherLog("", player.charname, "them dong khang cho rong bang 200L", "krongbang");
                                } else if (player.animalRide.isLan()) {
                                    if (player.animalRide.KHANG_HOA_TUYET > 0) return;
                                    player.subLuong(200L);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.animalRide.KHANG_HOA_TUYET = 95;
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Thú cưỡi Suicune đã được thêm thuộc tính kháng hoá tuyết", ""));
                                    Database.instance.saveAnimal(player.animalRide);
                                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                    Database.instance.saveOrtherLog("", player.charname, "them dong khang hoa tuyet cho lan su tu 200L", "khoatuyet");
                                }
                                break;
                        }
                    } else {
                        String[] menu = new String[]{
                            "Machamp", "Arcanine", "Sói xám", "Pidgeot", "Rapidash",
                            "Moltres", "Articuno", "Spectrier", "Piloswine", "Suicune",
                            "Dragonite"
                        };
                        Message msg = MessageCreator.createMsgMenu(menu, idNpc, 1);
                        player.sendMessage(msg);
                    }
                    break;
                case -5:
                    if (!isMapThanh(player.map) && !map.isMapLienDau()) {
                        return;
                    }

                    if (abs(player.x / 16 - 61) <= rangeNpc && abs(player.y / 16 - 45) <= rangeNpc) {
                        if (idMenu == 0) {
                            map.playerGiveCard(player, 2);
                        }
                        break;
                    }

                    return;
                case -4:
                    if (!isMapThanh(player.map) && !map.isMapLienDau()) {
                        return;
                    }

                    if (abs(player.x / 16 - 18) <= rangeNpc && abs(player.y / 16 - 45) <= rangeNpc) {
                        if (idMenu == 0) {
                            map.playerGiveCard(player, 1);
                        }
                        break;
                    }

                    return;
                case -3:
                    if (idMenu != 0) {
                        if (idMenu == 1) {
                            break;
                        }

                        if (idMenu != 2) {
                            if (idMenu != 3) {
                                if (idMenu == 4) {
                                }
                            } else {
                                if (player.rankClan == 0 && player.idClan != -1) {
                                    NewClan.getClan(player.idClan).rankClan = (byte) (idOptionMenu + 2);
                                    player.sendMessage(MessageCreator.createMsgInputText(-32003, 0, "Nhập tên Pokémon", 0));
                                    break;
                                }

                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể giáng chức.", ""));
                                return;
                            }
                        } else {
                            if (player.rankClan == 0 && player.idClan != -1) {
                                NewClan.getClan(player.idClan).rankClan = (byte) (idOptionMenu + 1);
                                player.sendMessage(MessageCreator.createMsgInputText(-32002, 0, "Nhập tên Pokémon", 0));
                                break;
                            }

                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể giáng chức.", ""));
                            return;
                        }
                    } else if (idOptionMenu != 0) {
                        if (idOptionMenu != 1) {
                            if (idOptionMenu != 2) {
                                if (idOptionMenu != 3) {
                                    if (idOptionMenu != 4) {
                                        if (idOptionMenu != 5) {
                                            if (idOptionMenu != 6) {
                                                if (idOptionMenu != 7) {
                                                    break;
                                                }

                                                if (player.rankClan == 0 && player.idClan != -1) {
                                                    player.sendMessage(MessageCreator.createMsgInputText(-32049, 0, "Nhập số tiền ", 0));
                                                    break;
                                                }

                                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể phát lương.", ""));
                                                return;
                                            }

                                            if (player.rankClan == 0 && player.idClan != -1) {
                                                player.sendMessage(MessageCreator.createMsgInputText(-32006, 0, "Nhập số tiền ", 0));
                                                break;
                                            }

                                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể phát lương.", ""));
                                            return;
                                        }

                                        if (!NewClan.isTimeOfferWages()) {
                                            return;
                                        }

                                        if (player.rankClan == 0 && player.idClan != -1) {
                                            player.sendMessage(MessageCreator.createMsgInputText(-32004, 0, "Nhập số tiền ", 0));
                                            break;
                                        }

                                        player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể phát lương.", ""));
                                        return;
                                    }

                                    if (player.rankClan == 0 && player.idClan != -1) {
                                        player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Cố vấn", "thành viên đội"}, idNpc, 3));
                                        break;
                                    }

                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể giáng chức.", ""));
                                    return;
                                }

                                if (player.rankClan == 0 && player.idClan != -1) {
                                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Đội phó", "Cố vấn"}, idNpc, 2));
                                    break;
                                }

                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể phong chức.", ""));
                                return;
                            }

                            if (player.idClan != idClanTown[player.myCountry] && player.inCountry == player.myCountry) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Đội của bạn hiện tại không sở hữu Cao nguyên Indigo.", ""));
                                return;
                            }

                            NewClan clan = NewClan.getClan(player.idClan);
                            if (!clan.master.toLowerCase().equals(player.charname.toLowerCase())) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể đặt thuế.", ""));
                                return;
                            }

                            int timex = clan.canSetTax();
                            if (timex > 0) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể đặt thuế sau " + timex + " phút nữa", ""));
                                return;
                            }

                            player.sendMessage(MessageCreator.createMsgInputText(-32001, 0, "Nhập mức thuế", 1));
                            break;
                        }

                        if (player.idClan == -1) {
                            return;
                        }

                        NewClan.getTopMemberClan(player, player.idClan);
                    } else {
                        NewClan clanx = NewClan.getClan(player.idClan);
                        if (clanx != null && clanx.level < NewClan.MAX_LEVEL) {
                            if (!clanx.master.toLowerCase().equals(player.charname.toLowerCase())) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có đội trưởng mới có thể nâng cấp đội.", ""));
                                return;
                            }

                            player.sendMessage(
                                    MessageCreator.createMsgPopUp(
                                            player.id,
                                            0,
                                            "phí nâng cấp đội là "
                                            + NewClan.getMoneyUplevelClan(player.idClan)
                                            + " xu và phải có "
                                            + NewClan.getXPUplevelClan(player.idClan)
                                            + " xp bang."
                                    )
                            );
                            break;
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại bạn không thể nâng cấp tiếp.", ""));
                        return;
                    }
                    break;
               
                case 2:
                    if (idMenu != 0) {
                        if (idMenu == 1) {
                            player.buyAnimalArmor = (byte) idOptionMenu;
                            player.sendMessage(MessageCreator.createMsgNPCSellWeapone(player, false));
                        }
                    } else {
                        player.buyAnimalArmor = -1;
                        if (idOptionMenu == PhaKhoaGame.VI_TRI_MUC_THO_REN) {
                            PhaKhoaGame.moMenu(player); // [khoa-khi-mac]
                        } else if (idOptionMenu != 0) {
                            if (idOptionMenu == 1) {
                                String[] menu = new String[]{"Trang bị đặc biệt", "Trang bị vật lý"};
                                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                            }
                        } else {
                            player.sendMessage(MessageCreator.createMsgNPCSellWeapone(player, true));
                        }
                    }
                    break;
                case 3:
                case 27:
                    // Bronzong ban giap: trong nha hoac dung canh Bronzong o thi tran (truoc chi trong nha).
                    if (!NpcTinhBanDo.oNoiDichVu(player, NpcTinhBanDo.NPC_BAN_GIAP)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hãy đến gần Bronzong để mua trang bị.", ""));
                        return;
                    }

                    player.typeItemBuy = (byte) idOptionMenu;
                    player.sendMessage(MessageCreator.createMsgSellArmoOfNPC(player, player.classBuy, idNpc));
            }
        } catch (Exception var46) {
            var46.printStackTrace();
        }
    }

}
