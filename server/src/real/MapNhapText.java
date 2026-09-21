package real;


import data.Animal;
import data.Database;
import data.Goverment;
import data.NewClan;
import data.Pet;
import io.Message;
import java.util.Vector;
import real.cmd.CreateCharHandler;
import real.cmd.LoginHandler;
import server.TeamServer;

/**
 * Than cua Map.doProcessInputText - xu ly chuoi nguoi choi go vao o nhap
 * lieu (doi ten, mat khau, tim kiem, nhap so luong...).
 *
 * 1.432 dong nay truoc nam trong real/Map.java. Map.doProcessInputText VAN
 * CON, giu NGUYEN chu ky protected cu, chi uy nhiem sang day.
 */
class MapNhapText {

    static void doProcessInputText(Map map, Char player, Message message) {
        try {
            int idActor = message.dis.readShort();
            int idMenu = message.dis.readByte();
            String text = message.dis.readUTF().trim().toLowerCase();
            if (idActor == MapSuKien.INPUT_VU_LAN_DOI_NHIEU_HOA) {
                MapSuKien.nhapSoBoHoaVuLan(player, idMenu, text);
                return;
            }
            int n = 0;
            int m = 0;
            Char p = null;
            // Bang setting cua admin: nhap gio chiem thanh / ti le drop
            if (AdminMenu.xuLyNhapText(player, idMenu, text)) {
                return;
            }
            // XÓA PET

            if (idMenu == 99) {
                if (!player.isCorrectPass && !player.passWord.equals("")) {
                    player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                    return;
                }

                if (!text.matches("^[1-9]\\d{0,9}$")) {  // Số nguyên dương, tối đa 10 chữ số
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Chỉ Có Thể Nhập Số"));
                    return;
                } 

                if(player.getLuong() < 500L) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng"));
                    return;
                }
            
                int id = Integer.parseInt(text);
                // Tìm pet theo idDB
                data.Pet targetPet = null;
                if (player.pet != null && !player.pet.isEmpty()) {
                    for (data.Pet pet : player.pet) {
                        if (pet.idDB == id) {
                            targetPet = pet;
                            break;
                        }
                    }
                }
            
                // Kiểm tra các điều kiện
                if (targetPet == null) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "vui lòng kiểm tra lại và nhập đúng ID"));
                    return;
                }
            
                if (targetPet.ownerid != player.charDBID) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể xóa pet đang sử dụng"));
                    return;
                }
            
                if (targetPet.place == 1) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể xóa pet đang sử dụng"));
                    return;
                }
            
                // Tạo thông báo xác nhận
                String confirmMessage = String.format(
                    "Bạn có chắc chắn muốn xóa Pet %s (ID: %d) không?\nChi phí: 500 lượng",
                    targetPet.getName(),
                    targetPet.idDB
                );

                // Gửi thông báo popup
                player.idPetChoXoa = id;
                player.sendMessage(MessageCreator.createMsgPopUp(id, 120, confirmMessage));
            }

            // XÓA THÚ
            if (idMenu == 98) {
                if (!player.isCorrectPass && !player.passWord.equals("")) {
                    player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                    return;
                }
            
                if (!text.matches("^[1-9]\\d{0,9}$")) {  // Số nguyên dương, tối đa 10 chữ số
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Chỉ Có Thể Nhập Số"));
                    return;
                } 
            
                if(player.getLuong() < 500L) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng"));
                    return;
                }
            
                int id = Integer.parseInt(text);
                // Tìm thú cưỡi theo idDB
                data.Animal targetAnimal = null;
                if (player.animal != null && !player.animal.isEmpty()) {
                    for (data.Animal animal : player.animal) {
                        if (animal.dbid == id) {
                            targetAnimal = animal;
                            break;
                        }
                    }
                }
            
                // Kiểm tra các điều kiện
                if (targetAnimal == null) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Vui lòng kiểm tra lại và nhập đúng ID"));
                    return;
                }
            
                if (targetAnimal.ownerId != player.charDBID) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Vui lòng kiểm tra lại và nhập đúng ID"));
                    return;
                }
            
                if (targetAnimal.place == 1) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể xóa thú cưỡi đang sử dụng"));
                    return;
                }
            
                // Tạo thông báo xác nhận
                String confirmMessage = String.format(
                    "Bạn có chắc chắn muốn xóa Thú Cưỡi %s có ID %s không?\nChi phí: 500 lượng",
                    targetAnimal.getName(),
                    targetAnimal.dbid
                );

                // Gửi thông báo popup
                player.idThuChoXoa = id;
                player.sendMessage(MessageCreator.createMsgPopUp(id, 119, confirmMessage));
            }
            if (idActor >= -32018 && idActor <= -32015) {
                if (Char.isOpenDauGiaChoiBlackFriday()) {
                    map.doProcessInputTextDauGia(player, idActor, idMenu, text);
                }

                if (!Char.isFinishAllSuKienTrungThul2016()) {
                    map.doProcessInputTextTrungThu(player, idActor, idMenu, text);
                }

                if (!Char.isFinishAllSuKienHaloween2016()) {
                    map.doProcessInputTextHalloWeen2016(player, idActor, idMenu, text);
                }

                if (!Char.isFinishAllSuKienNoel()) {
                    map.doProcessInputTextNoel(player, idActor, idMenu, text);
                }

                if (!Char.isFinishAllSuKienTet2017()) {
                    map.doProcessInputTextTet(player, idActor, idMenu, text);
                }

                return;
            }

           

            if (idActor == -32051) {
                if (TeamServer.isServerLienDau() || !Map.openLog || !Char.is123Tet()) {
                    return;
                }

                if (!Database.instance.checkCanInputCodeLixi(player.charname)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Mỗi nhân vật chỉ được dùng 1 code.", ""));
                    return;
                }

                if (!Database.instance.checkValidCodeLixi(text)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Code không hợp lệ hoặc đã được sử dụng.", ""));
                    return;
                }

                Database.instance.updateValidCodeLixi(text, player.charname);
                int luong = 10 + Map.r.nextInt(91);
                player.addLuong(luong);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được " + luong + " lượng", ""));
                Database.instance.saveOrtherLog("", player.charname, "nhap code: " + text + " nhan duoc " + luong + " luong", "codelixi");
            } else if (idActor == -32050) {
                if (Map.stopGiveMoney) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
                    return;
                }

                String cname = text.trim().toLowerCase();
                if (player.idClan == -1) {
                    return;
                }

                if (player.salaryClan <= 0L) {
                    return;
                }

                p = CharManager.instance.getCharByCharName(cname);
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể phát lương cho người đang chơi", ""));
                    return;
                }

                if (player.idClan != p.idClan) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể phát lương cho người thuộc bang hội khác.", ""));
                    return;
                }

                NewClan clanInfo = Map.getClaninfoByID(player.idClan);
                if (clanInfo == null) {
                    return;
                }

                if (player.rankClan != 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("không thể phát lương", ""));
                    return;
                }

                long max_money = clanInfo.getMoneyDistributeSalary(player.idClan);
                if (player.salaryClan > max_money) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Số lương tối đa có thể phát là: " + max_money, ""));
                    return;
                }

                if (player.pointCongHienClan < clanInfo.getDCHPhatLuongNhanh()) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn cần có " + clanInfo.getDCHPhatLuongNhanh() + " điểm cống hiến mới có thể phát lương", "")
                    );
                    return;
                }

                player.pointCongHienClan = player.pointCongHienClan - clanInfo.getDCHPhatLuongNhanh();
                String info = p.getxu() + " mc= " + clanInfo.money;
                p.addXu(player.salaryClan, "map 35");
                clanInfo.money = clanInfo.money - player.salaryClan;
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                info = info
                        + "_"
                        + p.getxu()
                        + " mc="
                        + clanInfo.money
                        + ">> "
                        + p.charname
                        + " đã nhận được "
                        + player.salaryClan
                        + " xu từ "
                        + player.charname
                        + " bang "
                        + clanInfo.name
                        + " "
                        + clanInfo.id
                        + " DCHBC: "
                        + player.pointCongHienClan
                        + " , "
                        + clanInfo.getDCHPhatLuong()
                        + ", phat nhanh";
                p.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được lương bang hội là " + player.salaryClan + " xu", ""));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                Database.instance.saveOrtherLog("", p.charname, info, "salaryCN");
                player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " đã nhận được " + player.salaryClan + " xu lương bang hội.", ""));
                clanInfo.updateNewClandata2DB();
                player.salaryClan = 0L;
            } else if (idActor == -32049) {
                if (Map.stopGiveMoney) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
                    return;
                }

                boolean timeOffer = NewClan.isTimeOfferWages();
                long money = (long) Integer.parseInt(text);
                if (money < 0L) {
                    return;
                }

                if (!timeOffer || player.rankClan != 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("không thể phát lương", ""));
                    return;
                }

                NewClan cl = NewClan.getClan(player.idClan);
                if (player.pointCongHienClan < cl.getDCHPhatLuongNhanh()) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn cần có " + cl.getDCHPhatLuongNhanh() + " điểm cống hiến mới có thể phát lương", "")
                    );
                    return;
                }

                if (money > cl.money) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bang không đủ tiền.", ""));
                    return;
                }

                long max_moneyx = cl.getMoneyDistributeSalary(player.idClan);
                if (money > max_moneyx) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Số lương tối đa có thể phát là: " + max_moneyx, ""));
                    return;
                }

                player.salaryClan = money;
                player.sendMessage(MessageCreator.createMsgInputText(-32050, 0, "Nhập tên người nhận", 0));
            } else {
                if (idActor == -32048) {
                    if (player.map.isMapOffline) {
                        return;
                    }

                    p = CharManager.instance.getCharByCharName(text);
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trong game. Không thể tặng.", ""));
                        return;
                    }

                    Animal anim = (Animal) player.animal.get(idMenu);
                    if (anim != null && anim.isAnimalCanTang() && !map.isMapOffline && anim.place != 1) {
                        anim.ownerId = p.charDBID;
                        p.animal.add(anim);
                        anim.id = p.getIDItem();
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                        Database.instance.saveAnimal(anim);
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận được " + anim.getName() + " từ " + player.charname));
                        player.animal.remove(idMenu);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Đã tặng " + anim.getName() + " cho " + p.charname, ""));
                        Database.instance
                                .saveOrtherLog(
                                        "", player.charname, "tang thu " + anim.getAttribute() + " > " + anim.getName() + " > " + anim.idImg + " cho " + text, "tanganimal"
                                );
                        return;
                    }

                    return;
                }

                if (idActor == -32047) {
                    player.itemwait.charBuy = text;
                    map.doUserSellItemAfterInputText(player, text);
                    return;
                }

                if (idActor == CharDoiTenNhanVat.INPUT_DOI_TEN) {
                    CharDoiTenNhanVat.doDoiTen(player, text);
                    return;
                }

                if (idActor == CharChuyenLuong.INPUT_TEN_NGUOI_NHAN) {
                    CharChuyenLuong.doNhapTen(player, text);
                    return;
                }

                if (idActor == CharChuyenLuong.INPUT_SO_LUONG) {
                    CharChuyenLuong.doNhapSoLuong(player, text);
                    return;
                }

                if (idActor == KhoXuLuong.INPUT_GUI_XU) {
                    KhoXuLuong.doNhapGuiXu(player, text);
                    return;
                }

                if (idActor == KhoXuLuong.INPUT_RUT_XU) {
                    KhoXuLuong.doNhapRutXu(player, text);
                    return;
                }

                if (idActor == KhoXuLuong.INPUT_GUI_LUONG_MO) {
                    KhoXuLuong.doNhapGuiLuong(player, text, false);
                    return;
                }

                if (idActor == KhoXuLuong.INPUT_RUT_LUONG_MO) {
                    KhoXuLuong.doNhapRutLuong(player, text, false);
                    return;
                }

                if (idActor == KhoXuLuong.INPUT_GUI_LUONG_KHOA) {
                    KhoXuLuong.doNhapGuiLuong(player, text, true);
                    return;
                }

                if (idActor == KhoXuLuong.INPUT_RUT_LUONG_KHOA) {
                    KhoXuLuong.doNhapRutLuong(player, text, true);
                    return;
                }

                if (idActor == VongQuayLuong.INPUT_SO_LUONG) {
                    VongQuayLuong.doNhapSoLuong(player, text);
                    return;
                }

                if (idActor == OfflineMap.INPUT_SO_LUONG_BAN_NGOC) {
                    if (map instanceof OfflineMap) {
                        ((OfflineMap) map).doUserSellGemQuantity(player, text);
                    }
                    return;
                }

                if (idActor == -32046) {
                    if (text.equals(player.charname)) {
                        return;
                    }

                    Pet pet = null;

                    for (int i = 0; i < player.pet.size(); i++) {
                        pet = (Pet) player.pet.get(i);
                        if (pet.id == player.idPetTang) {
                            break;
                        }
                    }

                    if (pet != null && pet.canTrade()) {
                        if (idMenu == 0) {
                            p = CharManager.instance.getCharByCharName(text);
                            if (p == null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trong game. Không thể tặng.", ""));
                                return;
                            }

                            if (p.map.isMapLoiDai()) {
                                return;
                            }

                            if (!p.rcvInvite || !p.rcvInviteVip) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                                return;
                            }

                            player.nameTangPet = p.charname;
                            if (pet.isPetTradeThoiHan()) {
                                player.moneyTrade = 0;
                                player.sendMessage(MessageCreator.createMsgPopUp(111, 111, "Bạn có muốn tặng " + pet.getName() + " cho " + p.charname + " không?"));
                                return;
                            }

                            player.sendMessage(MessageCreator.createMsgInputText(-32046, 1, "Xu giao dịch", 0));
                        } else if (idMenu == 1) {
                            long moneyx = (long) Integer.parseInt(text);
                            p = CharManager.instance.getCharByCharName(player.nameTangPet);
                            if (p == null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trong game. Không thể tặng.", ""));
                                return;
                            }

                            if (!p.rcvInvite || !p.rcvInviteVip) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                                return;
                            }

                            if (moneyx < 0L || moneyx > 2000000000L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Só tiền không hợp lệ.", ""));
                                return;
                            }

                            player.moneyTrade = (int) moneyx;
                            player.sendMessage(
                                    MessageCreator.createMsgPopUp(
                                            111, 111, "Bạn có muốn tặng " + pet.getName() + " cho " + p.charname + " với giá " + moneyx + " xu không?"
                                    )
                            );
                        }

                        return;
                    }

                    return;
                }
            }

            if (idActor == -32045) {
                if (text.toLowerCase().equals(player.charname)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tặng lượng cho người khác", ""));
                    return;
                }

                p = CharManager.instance.getCharByCharName(text);
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trên mạng. Không thể tặng lượng", ""));
                    return;
                }

                if (player.vetangluong < 1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ vé.", ""));
                    return;
                }

                if (!player.truNeuDu(() -> player.getLuong() >= 10L, () -> player.subLuongTang(10L))) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng.", ""));
                    return;
                }

                int old = player.vetangluong--;
                p.addLuong(10);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                p.sendMessage(MessageCreator.createMsgChat(p.id, "Bạn nhận được 10 lượng từ " + player.charname));
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Bạn đã tặng cho " + p.charname + " 10 lượng"));
                Database.instance
                        .saveOrtherLog(
                                "", player.charname, "tang 10 luong cho " + p.charname + " ve trc tang " + old + ", ve sau tang " + player.vetangluong, "tangluong"
                        );
                Database.instance.saveOrtherLog("", p.charname, "nhan 10 luong tu " + player.charname, "nhanluong");
                Database.instance.saveCharAuto(p);
                Database.instance.saveCharAuto(player);
                return;
            }

            if (idActor == -32044) {
                if (idMenu == 0) {
                    p = CharManager.instance.getCharByCharName(text);
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trên mạng. Không thể nhận kim cương", ""));
                        return;
                    }

                    if (Map.ALL_CHAR_CHUYEN_KIM_CUONG.get(player.charname) != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đang chờ hoàn thành tặng Kim cương. Xin thử lại sau", ""));
                        return;
                    }

                    CharChuyenPhieuQua c = (CharChuyenPhieuQua) Map.ALL_CHAR_RECEIVE_KIM_CUONG.get(text);
                    if (c != null) {
                        if (!c.isTimeOver()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi đang chờ giao dịch kim cương. Xin thử lại sau", ""));
                            return;
                        }

                        Map.ALL_CHAR_RECEIVE_KIM_CUONG.remove(text);
                        c = null;
                    }

                    player.nametangkm = text;
                    player.sendMessage(MessageCreator.createMsgInputText(-32044, 1, "số lượng", 0));
                } else if (idMenu == 1) {
                    p = CharManager.instance.getCharByCharName(player.nametangkm);
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trên mạng. Không thể nhận kim cương", ""));
                        return;
                    }

                    if (Map.ALL_CHAR_CHUYEN_KIM_CUONG.get(player.charname) != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đang chờ hoàn thành tặng Kim cương. Xin thử lại sau", ""));
                        return;
                    }

                    int soluong = Integer.parseInt(text);
                    if (soluong <= 0) {
                        return;
                    }

                    CharChuyenPhieuQua c = (CharChuyenPhieuQua) Map.ALL_CHAR_RECEIVE_KIM_CUONG.get(player.nametangkm);
                    if (c != null) {
                        if (!c.isTimeOver()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi đang chờ giao dịch kim cương. Xin thử lại sau", ""));
                            return;
                        }

                        Map.ALL_CHAR_RECEIVE_KIM_CUONG.remove(player.nametangkm);
                        c = null;
                    }

                    c = new CharChuyenPhieuQua();
                    c.nameChuyen = player.charname;
                    c.soluong = soluong;
                    Map.ALL_CHAR_RECEIVE_KIM_CUONG.put(player.nametangkm, c);
                    Map.ALL_CHAR_CHUYEN_KIM_CUONG.put(player.charname, new CharChuyenPhieuQua());
                    p.sendMessage(MessageCreator.createMsgPopUp(110, 110, "Bạn có muốn nhận " + soluong + " kim cương từ " + player.charname + " không?"));
                }

                return;
            }

            if (idActor == -32043) {
                if (idMenu == 0) {
                    String hoten = BadWord.removeAccent(text);
                    if (hoten.length() > 45) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Họ tên phải ngắn hơn 45 ký tự", ""));
                        return;
                    }

                    if (hoten.equals("")) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Họ tên không hợp lệ.", ""));
                        return;
                    }

                    player.hoten = hoten;
                    player.sendMessage(MessageCreator.createMsgInputText(-32043, 1, "Địa chỉ", 0));
                } else if (idMenu == 1) {
                    if (text.equals("")) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Địa chỉ không hợp lệ.", ""));
                        return;
                    }

                    if (text.length() > 100) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Địa chỉ phải ngắn hơn 100 ký tự", ""));
                        return;
                    }

                    player.diachi = text;
                    player.sendMessage(MessageCreator.createMsgInputText(-32043, 2, "Cmnd", 0));
                } else if (idMenu == 2) {
                    if (text.equals("") || text.length() < 9 || text.length() > 12) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Cmnd phải có từ 9-12 ký tự", ""));
                        return;
                    }

                    if (player.hoten.length() > 45) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Họ tên phải ngắn hơn 45 ký tự", ""));
                        return;
                    }

                    if (player.hoten.equals("")) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Họ tên không hợp lệ.", ""));
                        return;
                    }

                    if (player.diachi.equals("")) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Địa chỉ không hợp lệ.", ""));
                        return;
                    }

                    if (player.diachi.length() > 100) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Địa chỉ phải ngắn hơn 100 ký tự", ""));
                        return;
                    }

                    // SECURITY: da bo han chuc nang nay. Truoc day no gui ho ten / dia chi / so CMND
                    // cua nguoi choi bang HTTP thuong sang server ben thu ba (27.0.14.41).
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng này hiện không khả dụng.", ""));
                }

                return;
            }

            if (idActor >= -32013 && idActor <= -32008) {
                if (player.idClan == -1) {
                    return;
                }

                if (player.rankGov > 1 || player.rankGov < 0) {
                    return;
                }

                p = CharManager.instance.getCharByCharName(text);
                if (p != null && p.idClan != player.idClan) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể phong chức cho người trong cùng bang.", ""));
                    return;
                }
            }

            if (idActor == -32040) {
                String moneyxx = text.trim().toLowerCase();
                MapQuaTang.doVongQuayMayMan(map, player, moneyxx, 0);
                return;
            }

            if (idActor == -32041) {
                String moneyxx = text.trim().toLowerCase();
                MapQuaTang.doVongQuayMayMan(map, player, moneyxx, 1);
                return;
            }

            if (idActor == -32042) {
                if (!Map.openLog) {
                    return;
                }

                String code = text.trim();
                map.doNapSMSGiftCode(player, code, 0);
                return;
            }

            if (idActor == -32020) {
                if (!Char.isSuKien83()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Sự kiện đã kết thúc.", ""));
                    return;
                }

                if (text.toLowerCase().equals(player.charname.toLowerCase())) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tặng hoa cho người khác.", ""));
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                    return;
                }

                p = CharManager.instance.getCharByCharName(text.trim().toLowerCase());
                if (p != null && p.isCharNam()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tặng hoa cho nhân vật nữ.", ""));
                    return;
                }

                if (p != null) {
                    if (player.potions[77] <= 0) {
                        return;
                    }

                    player.potions[77]--;
                    int index = Map.r.nextInt(3);
                    int[] idHoa = new int[]{4, 3, 5};
                    int[] nhoa = new int[]{3, 3, 3};
                    String[] chuc = new String[]{
                        "hạnh phúc vui vẻ",
                        "và tất cả các bạn nữ luôn mạnh khoẻ, trẻ trung, vui tính",
                        "nhiều sức khoẻ, luôn thành công trong cuộc sống",
                        "và gia đình 8/3 thật hạnh phúc"
                    };
                    String info = player.charname + " chúc " + text.trim().toLowerCase() + " " + chuc[Map.r.nextInt(chuc.length)];
                    Message msg = MessageCreator.createMsgWeather(idHoa[index], nhoa[Map.r.nextInt(3)], 60000);
                    Message msginfo = MessageCreator.createServerAlertAutoOffMessage(info);

                    for (int ix = 0; ix < CharManager.instance.vChars.size(); ix++) {
                        try {
                            Char c = (Char) CharManager.instance.vChars.get(ix);
                            if (c != null && p.id != c.id && c.id != player.id) {
                                c.sendMessage(msg);
                                c.sendMessage(msginfo);
                            }
                        } catch (Exception var21) {
                        }
                    }

                    player.doChuc83(p.charname);

                    try {
                        Database.instance.saveOrtherLog("tob_other_log", player.charname, LoginHandler.PORTION_NAME[72] + "_" + p.charname, "tangnh");
                        Database.instance.saveOrtherLog("tob_other_log", p.charname, LoginHandler.PORTION_NAME[72] + "_" + player.charname, "dtangnh");
                        player.flower++;
                    } catch (Exception var20) {
                    }

                    player.sendMessage(msg);
                    player.sendMessage(msginfo);
                    p.sendMessage(msg);
                    p.sendMessage(msginfo);
                    player.idPotionUsed = -1;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                }
            } else if (idActor == -32019) {
                if (!Char.isOpenSellBlackFriday() && !Char.isSuKienTet2017() && !TeamServer.isServerLocal()) {
                    return;
                }

                if (!text.equals(player.diachi) || player.diachi.equals("")) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Mã xác thực không chính xác", ""));
                } else if (Char.isOpenDauGiaChoiBlackFriday()) {
                    Map.doBuyItemBlackFriday(player);
                } else if (Char.isSuKienTet2017()) {
                    Map.doReceiveLixi(player);
                }

                player.diachi = "";
            } else if (idActor == -32018) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long sl = (long) Integer.parseInt(text.trim());
                if (sl <= 0L || sl > 2000000000L) {
                    return;
                }

                if (!player.truNeuDu(
                        () -> (long) player.getLuong() >= sl * 5L
                                && (long) player.potions[125] >= sl * 10L
                                && (long) player.potions[124] >= sl * 10L,
                        () -> {
                            player.subLuong(sl * 5L);
                            player.potions[125] = (int) ((long) player.potions[125] - sl * 10L);
                            player.potions[124] = (int) ((long) player.potions[124] - sl * 10L);
                            player.potions[134] = (int) ((long) player.potions[134] + sl);
                        })) {
                    if ((long) player.getLuong() < sl * 5L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    } else if ((long) player.potions[125] < sl * 10L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh nướng", ""));
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh dẻo", ""));
                    }
                    return;
                }

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "doi  " + sl + " hop banh db", "banhdb");
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được " + sl + " hộp bánh đặc biệt", ""));
                Database.instance.saveLogThongKeChiTieu("hop dac biet", 0, (int) sl, sl * 5L, "luong");
            } else if (idActor == -32017) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long slx = (long) Integer.parseInt(text.trim());
                if (slx <= 0L || slx > 2000000000L) {
                    return;
                }

                if (!player.truNeuDu(
                        () -> player.getxu() >= slx * 5000L
                                && (long) player.potions[125] >= slx * 10L
                                && (long) player.potions[124] >= slx * 10L,
                        () -> {
                            player.subXu(slx * 5000L, false, "32017");
                            player.potions[125] = (int) ((long) player.potions[125] - slx * 10L);
                            player.potions[124] = (int) ((long) player.potions[124] - slx * 10L);
                            player.potions[133] = (int) ((long) player.potions[133] + slx);
                        })) {
                    if (player.getxu() < slx * 5000L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                    } else if ((long) player.potions[125] < slx * 10L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh nướng", ""));
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh dẻo", ""));
                    }
                    return;
                }

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "doi  " + slx + " hop banh thap cam", "banhtc");
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được " + slx + " hộp bánh thập cẩm", ""));
                Database.instance.saveLogThongKeChiTieu("hop thap cam", 0, (int) slx, slx * 5000L, "xu");
            } else if (idActor == -32016) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long slxx = (long) Integer.parseInt(text.trim());
                if (slxx <= 0L || slxx > 2000000000L || slxx * 10L <= 0L) {
                    return;
                }

                if ((long) player.potions[142] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[142], ""));
                    return;
                }

                if ((long) player.potions[122] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[122], ""));
                    return;
                }

                if ((long) player.potions[121] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[121], ""));
                    return;
                }

                if ((long) player.potions[120] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[120], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getLuong());
                player.potions[142] = (int) ((long) player.potions[142] - slxx * 10L);
                player.potions[122] = (int) ((long) player.potions[122] - slxx * 10L);
                player.potions[121] = (int) ((long) player.potions[121] - slxx * 10L);
                player.potions[120] = (int) ((long) player.potions[120] - slxx * 10L);
                player.potions[125] = (int) ((long) player.potions[125] + slxx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slxx + " bánh nướng", ""));
                Database.instance.saveOrtherLog("", player.charname, "banh nuong " + slxx, "banhnuong");
                Database.instance.saveLogThongKeChiTieu("Doi banh nuong", 0, (int) slxx, 0L, "banhnuong");
            } else if (idActor == -32015) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long slxxx = (long) Integer.parseInt(text.trim());
                if (slxxx <= 0L || slxxx > 2000000000L || slxxx * 10L <= 0L) {
                    return;
                }

                if ((long) player.potions[142] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[142], ""));
                    return;
                }

                if ((long) player.potions[122] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[122], ""));
                    return;
                }

                if ((long) player.potions[121] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[121], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getxu());
                player.potions[142] = (int) ((long) player.potions[142] - slxxx * 10L);
                player.potions[122] = (int) ((long) player.potions[122] - slxxx * 10L);
                player.potions[121] = (int) ((long) player.potions[121] - slxxx * 10L);
                player.potions[124] = (int) ((long) player.potions[124] + slxxx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slxxx + " bánh dẻo", ""));
                Database.instance.saveOrtherLog("", player.charname, "banh deo " + slxxx + "xu trc" + infoxu + ", xu còn " + player.getxu(), "banhdeo");
                Database.instance.saveLogThongKeChiTieu("Doi banh deo", 0, (int) slxxx, 0L, "banhdeo");
            } else if (idActor == -32014) {
                if (player.rankGov == -1) {
                    return;
                }

                p = CharManager.instance.getCharByCharName(text);
                if (p == null || p != null && p.myCountry != player.myCountry) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại bạn không thể cấm chat " + text, ""));
                    return;
                }

                p.forbitChat = 30;
                player.sendMessage(MessageCreator.createServerAlertMessage(text + " đã bị cấm chat 30 phút.", ""));
                p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã bị" + player.charname + " cấm chat 30 phút.", ""));
            } else if (idActor == -32008) {
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " chưa đăng nhập vào game.", ""));
                    return;
                }

                if (player.myCountry != p.myCountry) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " và bạn không cùng lãnh thổ.", ""));
                    return;
                }

                if (p.rankGov != -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " đang giữ chức " + Goverment.NAME[p.rankGov], ""));
                    return;
                }

                player.addDaiThan(p, player.myCountry);
            } else if (idActor == -32009) {
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " chưa đăng nhập vào game.", ""));
                    return;
                }

                if (player.myCountry != p.myCountry) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " và bạn không cùng lãnh thổ.", ""));
                    return;
                }

                if (p.rankGov != -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " đang giữ chức " + Goverment.NAME[p.rankGov], ""));
                    return;
                }

                player.addTuongQuan(p, player.myCountry);
            } else if (idActor == -32010) {
                if (p != null && p.rankGov == -1) {
                    return;
                }

                player.removeDaiThan(text, player.myCountry);
            } else if (idActor == -32011) {
                if (p != null && p.rankGov == -1) {
                    return;
                }

                player.removeTuongQuan(text, player.myCountry);
            } else if (idActor == -32012) {
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " chưa đăng nhập vào game.", ""));
                    return;
                }

                if (player.myCountry != p.myCountry) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " và bạn không cùng lãnh thổ.", ""));
                    return;
                }

                if (p.rankGov != -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " đang giữ chức " + Goverment.NAME[p.rankGov], ""));
                    return;
                }

                player.addBoDau(p, player.myCountry);
            } else if (idActor == -32013) {
                if (p != null && p.rankGov == -1) {
                    return;
                }

                player.removeBoDau(text, player.myCountry);
            } else if (idActor == -32001) {
                try {
                    n = Integer.parseInt(text);
                } catch (Exception var19) {
                }

                if (n <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đặt mức thuế này", ""));
                    return;
                }

                if (n > 20) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Mức thuế tối đa có thể thu là 20 phần trăm", ""));
                    return;
                }

                if (Map.idClanTown[player.myCountry] == -1) {
                    return;
                }

                if (Map.idClanTown[player.myCountry] == player.idClan && player.inCountry != player.myCountry || Map.idClanTown[player.myCountry] != player.idClan) {
                    return;
                }

                if (player.myCountry == -1) {
                    return;
                }

                if (player.myCountry != player.inCountry) {
                    return;
                }

                if (player.idClan == -1) {
                    return;
                }

                NewClan clan = NewClan.getClan(player.idClan);
                if (!clan.master.toLowerCase().equals(player.charname.toLowerCase())) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có bang chủ mới có thể đặt thuế.", ""));
                    return;
                }

                int time = clan.canSetTax();
                if (time > 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể đặt thuế sau " + time + " phút nữa", ""));
                    return;
                }

                Map.taxOfClan[player.myCountry] = (byte) n;
                NewClan.setTax(player.idClan, n);
                player.sendMessage(MessageCreator.createServerAlertMessage("Mức thuế " + n + " đã được thiết lập", ""));
                Database.instance.saveOrtherLog("", player.charname, clan.name + "_" + clan.master + " thue " + n + "% " + player.myCountry, "settax");
            } else if (idActor == -32002) {
                p = CharManager.instance.getCharByCharName(text.toLowerCase());
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " không đang chơi", ""));
                    return;
                }

                if (p.idClan != player.idClan || p.idClan == -1 || player.idClan == -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " không có trong bang của bạn", ""));
                    return;
                }

                if (MapBangHoi.doSetTitleClan(map, player, p, true)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " đã được phong chức", ""));
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã được phong chức làm " + NewClan.titlesClan[p.rankClan], ""));
                }
            } else if (idActor == -32003) {
                p = CharManager.instance.getCharByCharName(text);
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " không đang chơi", ""));
                    return;
                }

                if (p.idClan != player.idClan) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " không có trong bang của bạn", ""));
                    return;
                }

                if (MapBangHoi.doSetTitleClan(map, player, p, false)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(text + " đã bị giáng chức", ""));
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã bị giáng chức thành " + NewClan.titlesClan[p.rankClan], ""));
                }
            } else if (idActor == -32004) {
                if (Map.stopGiveMoney) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
                    return;
                }

                boolean timeOfferx = NewClan.isTimeOfferWages();
                long moneyxx = (long) Integer.parseInt(text);
                if (moneyxx < 0L) {
                    return;
                }

                if (!timeOfferx || player.rankClan != 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("không thể phát lương", ""));
                    return;
                }

                NewClan clx = NewClan.getClan(player.idClan);
                if (player.pointCongHienClan < clx.getDCHPhatLuong()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần có " + clx.getDCHPhatLuong() + " điểm cống hiến mới có thể phát lương", ""));
                    return;
                }

                if (moneyxx > clx.money) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bang không đủ tiền.", ""));
                    return;
                }

                long max_moneyx = clx.getMoneyDistributeSalary(player.idClan);
                if (moneyxx > max_moneyx) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Số lương tối đa có thể phát là: " + max_moneyx, ""));
                    return;
                }

                player.salaryClan = moneyxx;
                player.sendMessage(MessageCreator.createMsgInputText(-32005, 0, "Nhập tên người nhận", 0));
            } else if (idActor == -32005) {
                if (Map.stopGiveMoney) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
                    return;
                }

                String cnamex = text.trim().toLowerCase();
                if (player.idClan == -1) {
                    return;
                }

                if (player.salaryClan <= 0L) {
                    return;
                }

                p = CharManager.instance.getCharByCharName(cnamex);
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể phát lương cho người đang chơi", ""));
                    return;
                }

                String timeReceiveLuong = p.checkNhanLuongBang();
                if (timeReceiveLuong != null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " chỉ có thể nhận lương sau " + timeReceiveLuong, ""));
                    return;
                }

                if (player.idClan != p.idClan) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể phát lương cho người thuộc bang hội khác.", ""));
                    return;
                }

                NewClan clanInfox = Map.getClaninfoByID(player.idClan);
                if (clanInfox == null) {
                    return;
                }

                if (player.rankClan != 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("không thể phát lương", ""));
                    return;
                }

                long max_moneyx = clanInfox.getMoneyDistributeSalary(player.idClan);
                if (player.salaryClan > max_moneyx) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Số lương tối đa có thể phát là: " + max_moneyx, ""));
                    return;
                }

                if (player.pointCongHienClan < clanInfox.getDCHPhatLuong()) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn cần có " + clanInfox.getDCHPhatLuong() + " điểm cống hiến mới có thể phát lương", "")
                    );
                    return;
                }

                player.pointCongHienClan = player.pointCongHienClan - clanInfox.getDCHPhatLuong();
                String info = p.getxu() + " mc= " + clanInfox.money;
                p.addXu(player.salaryClan, "map 35");
                clanInfox.money = clanInfox.money - player.salaryClan;
                info = info
                        + "_"
                        + p.getxu()
                        + " mc="
                        + clanInfox.money
                        + ">> "
                        + p.charname
                        + " đã nhận được "
                        + player.salaryClan
                        + " xu từ "
                        + player.charname
                        + " bang "
                        + clanInfox.name
                        + " "
                        + clanInfox.id
                        + " DCHBC: "
                        + player.pointCongHienClan
                        + " , "
                        + clanInfox.getDCHPhatLuong();
                p.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được lương bang hội là " + player.salaryClan + " xu", ""));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                Database.instance.saveOrtherLog("", p.charname, info, "salaryC");
                player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " đã nhận được " + player.salaryClan + " xu lương bang hội.", ""));
                clanInfox.updateNewClandata2DB();
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.salaryClan = 0L;
            } else if (idActor == -32006) {
                if (Map.stopGiveMoney) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
                    return;
                }

                long moneyxxx = (long) Integer.parseInt(text);
                if (moneyxxx < 0L) {
                    return;
                }

                if (!NewClan.isTimeOfferWages() || player.rankClan != 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("không thể phát lương", ""));
                    return;
                }

                NewClan clanInfoxx = Map.getClaninfoByID(player.idClan);
                Vector<String> sub = clanInfoxx.getMemberReceiveMoney();
                if (sub.size() <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại không có thành viên nào trong bang đang chơi game", ""));
                    return;
                }

                if (player.pointCongHienClan < clanInfoxx.getDCHPhatLuong()) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn cần có " + clanInfoxx.getDCHPhatLuong() + " điểm cống hiến mới có thể phát lương", "")
                    );
                    return;
                }

                player.pointCongHienClan = player.pointCongHienClan - clanInfoxx.getDCHPhatLuong();
                long moneySub = moneyxxx * (long) sub.size();
                if (clanInfoxx.money < moneySub) {
                    return;
                }

                clanInfoxx.addMoney2Clan(-moneySub);
                clanInfoxx.updateNewClandata2DB();

                for (int ix = 0; ix < sub.size(); ix++) {
                    Char pp = CharManager.instance.getCharByCharName((String) sub.get(ix));
                    if (pp != null && pp.idClan == player.idClan) {
                        pp.addXu((long) Integer.parseInt(text), "map 34");
                        pp.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận được lương bang hội là " + moneyxxx + " xu", ""));
                        pp.sendMessage(MessageCreator.createCharInventoryMessage(pp, 0));
                        Database.instance
                                .saveLogClan(
                                        player.charname,
                                        "salaryall",
                                        pp.charname
                                        + " "
                                        + pp.getxu()
                                        + " >> nhan "
                                        + moneyxxx
                                        + " xu "
                                        + clanInfoxx.name
                                        + " | "
                                        + clanInfoxx.money
                                        + " >> DCHBC: "
                                        + player.pointCongHienClan
                                        + " , "
                                        + clanInfoxx.getDCHPhatLuong()
                                );
                    }
                }

                player.sendMessage(MessageCreator.createServerAlertMessage("Đã phát lương", ""));
            } else if (idActor == player.id) {
                if (idMenu == 0) {
                    if (Map.getTown[player.inCountry]) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tặng hoa trong thời gian chiếm thành.", ""));
                        return;
                    }

                    if (text.toLowerCase().equals(player.charname.toLowerCase())) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tặng hoa cho người khác.", ""));
                        return;
                    }

                    p = CharManager.instance.getCharByCharName(text.trim().toLowerCase());
                    if (p != null && player.idPotionUsed > 49 && player.idPotionUsed < 62) {
                        if (player.potions[player.idPotionUsed] <= 0) {
                            return;
                        }

                        player.potions[player.idPotionUsed]--;
                        if (player.potions[player.idPotionUsed] < 0) {
                            player.potions[player.idPotionUsed] = 0;
                        }

                        int index = (player.idPotionUsed - 50) / 4;
                        int[] idHoa = new int[]{4, 3, 5};
                        int[] nhoa = new int[]{2, 9, 39, 99, 1, 9, 39, 99, 1, 9, 39, 99};
                        String[] chuc = new String[]{"giáng sinh anh lành", "giáng sinh vui vẻ", "merry xmas", "và gia đình giáng sinh thật hạnh phúc"};
                        String info = player.charname.toUpperCase()
                                + " tặng "
                                + nhoa[player.idPotionUsed - 50]
                                + " "
                                + LoginHandler.PORTION_NAME[player.idPotionUsed]
                                + " cho "
                                + p.charname.toUpperCase();
                        info = info + " Chúc " + p.charname.toUpperCase() + " năm mới " + chuc[Map.r.nextInt(4)].toLowerCase();
                        Message msg = MessageCreator.createMsgWeather(idHoa[index], nhoa[player.idPotionUsed - 50], 60000);
                        Message msginfo = MessageCreator.createServerAlertAutoOffMessage(info);

                        for (int ixx = 0; ixx < CharManager.instance.vChars.size(); ixx++) {
                            Char c = (Char) CharManager.instance.vChars.get(ixx);
                            if (c != null && p.id != c.id && c.id != player.id) {
                                if (nhoa[player.idPotionUsed - 50] >= 39 && System.currentTimeMillis() - Map.timeTangHoa >= 0L) {
                                    Map.timeTangHoa = System.currentTimeMillis() + 60000L;
                                }

                                c.sendMessage(msginfo);
                            }
                        }

                        player.sendMessage(msg);
                        p.sendMessage(msg);
                        Char.isSuKienNoel();

                        try {
                            Database.instance
                                    .saveOrtherLog("tob_other_log", player.charname, LoginHandler.PORTION_NAME[player.idPotionUsed] + "_" + p.charname, "tang");
                            Database.instance
                                    .saveOrtherLog("tob_other_log", p.charname, LoginHandler.PORTION_NAME[player.idPotionUsed] + "_" + player.charname, "dtang");
                        } catch (Exception var18) {
                        }

                        player.sendMessage(msg);
                        player.sendMessage(msginfo);
                        p.sendMessage(msg);
                        p.sendMessage(msginfo);
                        player.idPotionUsed = -1;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    }
                } else if (idMenu == 1) {
                    // LOA — phat toi MOI nguoi choi dang online tren may chu.
                    //
                    // Truoc day khoi nay khong he kiem tra nguoi choi co loa hay
                    // khong, va chi tru potions[100] SAU khi da phat. Nghia la bat
                    // ky ai cung gui thang goi -31 (idMenu = 1) trong mot vong lap
                    // duoc: mot goi tin gui len thanh N goi tin gui xuong, voi N la
                    // so nguoi dang online. So loa con lai tut xuong AM khong day.
                    // Day la cho tru binh DUY NHAT trong ca 116 cho ma khong kiem
                    // tra so luong truoc.
                    if (player.potions[100] <= 0) {
                        return;
                    }
                    // Co loa that thi van phai gian cach, vi loa mua duoc hang loat.
                    if (System.currentTimeMillis() - player.timeDungLoa < Char.LOA_COOLDOWN_MS) {
                        return;
                    }

                    if (text.length() > 50) {
                        text = text.substring(0, 50);
                    }

                    text = Map.checkBadWord(text);
                    text = text.trim().replaceAll("\\s+", " ");
                    text = Map.replaceStringWithSpace(text);
                    text = Map.replaceExtendDomain(text);
                    // Loc xong con rong thi khong phat, va cung KHONG tru loa —
                    // ban cu tru ca khi khong phat duoc gi, nguoi choi mat khong.
                    if (text.length() == 0) {
                        return;
                    }

                    Map.sendAllCharServer(-1, MessageCreator.createMsgSpeaker(player.charname + ": " + text));
                    player.potions[100]--;
                    player.timeDungLoa = System.currentTimeMillis();
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (idMenu == 2) {
                    if (!player.passWord.equals("")) {
                        return;
                    }

                    if (text.length() > 25 || text.length() < 4 || !CreateCharHandler.checkInfo(text)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Mật khẩu không hợp lệ. Xin chọn mật khẩu khác", ""));
                        return;
                    }

                    player.passWord = text;
                    player.sendMessage(MessageCreator.createServerAlertMessage("Mật khẩu " + text + " đã được thiết lập thành công", ""));
                } else if (idMenu == 3) {
                    if (!text.equals("") && !player.passWord.equals("")) {
                        if (player.passWord.equals(text) && text.length() <= 25 && text.length() >= 4 && CreateCharHandler.checkInfo(text)) {
                            player.isCorrectPass = false;
                            player.passWord = "";
                            player.sendMessage(MessageCreator.createServerAlertMessage("Mật khẩu đã hủy", ""));
                            return;
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage("Mật khẩu không hợp lệ. Xin chọn mật khẩu khác", ""));
                        return;
                    }

                    return;
                } else if (idMenu == 4) {
                    if (player.isCorrectPass) {
                        return;
                    }

                    if (player.passWord.equals("")) {
                        return;
                    }

                    if (!player.passWord.equals(text) || text.length() > 25 || text.length() < 4 || !CreateCharHandler.checkInfo(text)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Mật khẩu không hợp lệ. Xin chọn mật khẩu khác", ""));
                        return;
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage("Mật khẩu hợp lệ.", ""));
                    player.isCorrectPass = true;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    player.sendMessage(MessageCreator.createCharSpecialItem(player));
                } else if (idMenu == 5) {
                    if (player.mapID != 201 && player.mapID != 0 && player.mapID != 70 && player.mapID != 301 && player.mapID != 1701) {
                        return;
                    }

                    if (player.lvDetail.lv < 40) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 40 trở lên mới có thể sử dụng chức năng này.", ""));
                        return;
                    }

                    int moneyxxxx = Integer.parseInt(text.trim());
                    if (moneyxxxx < 100000 || moneyxxxx > 10000000) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể thách đấu với số tiền từ 100k xu đến 10t xu", ""));
                        return;
                    }

                    if (moneyxxxx < 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thách đấu với số tiền này", ""));
                        return;
                    }

                    if (player.getxu() < (long) moneyxxxx) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                        return;
                    }

                    player.moneyThachdau = moneyxxxx;
                    player.sendMessage(MessageCreator.createMsgInputText(player.id, 6, "Nhập tên đối thủ", 0));
                } else if (idMenu == 6) {
                    if (player.mapID != 201 && player.mapID != 0 && player.mapID != 70 && player.mapID != 301 && player.mapID != 1701) {
                        return;
                    }

                    if (player.charname.equals(text.trim().toLowerCase())) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thách đấu với chính mình", ""));
                        return;
                    }

                    p = CharManager.instance.getCharByCharName(text.trim().toLowerCase());
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy đối thủ của bạn", ""));
                        return;
                    }

                    if (p.idArena > -1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " đã chấp nhận tham gia một trận đấu khác.", ""));
                        return;
                    }

                    if (p.rcvInvite && p.rcvInviteVip) {
                        if (player.moneyThachdau >= 100000 && player.moneyThachdau <= 10000000) {
                            if (p.getxu() < (long) player.moneyThachdau) {
                                player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " không có đủ số tiền cược bạn muốn cược.", ""));
                                return;
                            }

                            if (p.lvDetail.lv < 40) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Đối thủ của bạn phải đạt cấp độ 40 trở lên.", ""));
                                return;
                            }

                            if (p.mapID != player.mapID) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn và đối thủ phải cùng có mặt tại npc đấu trường.", ""));
                                return;
                            }

                            if (p.rcvInvite && p.rcvInviteVip) {
                                p.sendMessage(
                                        MessageCreator.createMsgPopUp(
                                                p.id,
                                                10,
                                                player.charname
                                                + " lv "
                                                + player.lvDetail.lv
                                                + " gửi lời thách đấu tới bạn với số tiền cược là "
                                                + player.moneyThachdau
                                                + " xu. Bạn có đồng ý không?"
                                        )
                                );
                                p.nameCompetitor = player.charname;
                                return;
                            }

                            player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                            return;
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể thách đấu với số tiền từ 100k xu đến 10t xu", ""));
                        return;
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                    return;
                } else if (idMenu == 7) {
                    if (Map.openLog) {
                        MapQuaTang.doSetGiftOfflineKpah(map, player, text.trim());
                    } else if (!Map.openLog) {
                        MapQuaTang.doSetGiftNguDeDaiNam(map, player, text.trim());
                    }
                } else if (idMenu == 8) {
                    MapXaHoi.doSendChallenParty(map, player, text, 0);
                } else if (idMenu == 9) {
                    MapXaHoi.doSendChallenParty(map, player, text, 1);
                }
            }
        } catch (Exception var22) {
        }
    }

}
