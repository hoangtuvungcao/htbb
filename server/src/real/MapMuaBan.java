package real;


import data.Animal;
import real.plugins.GiftCode;
import data.BossMatQuy;
import data.CharClan;
import data.CharCooking;
import data.CharInboxMessage;
import data.CharInfo;
import data.CharWeaponData;
import data.Cooking;
import data.CreateItemTemplate;
import data.DataGame;
import data.Database;
import data.EventGame;
import data.GameCanvas;
import data.GemItem;
import data.Goverment;
import data.InfoCharLienDau;
import data.InfoItemCreate;
import data.InfoQuestTemplate;
import data.ItemChangeColor;
import data.ItemInfo;
import data.ItemLuckyDraw;
import data.ItemQuest;
import data.MapLocation;
import data.Net;
import data.NewClan;
import data.OtherHandle;
import data.Pet;
import data.PetTemplate;
import data.SeedItem;
import data.SeedTemplate;
import data.Text;
import data.ThungGo;
import data.Weather;
import data.Xoso;
import io.Message;
import io.SessionManager;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import real.cmd.CreateCharHandler;
import real.cmd.LoginHandler;
import real.cmd.RequestNPCInfoHandler;
import server.TeamServer;
import util.Logger;
import data.NewClan;
import ThanKhi.ThoRenThanBi;

/**
 * Mua ban va giao dich: mua vat pham dac biet tu NPC, giao dich giua hai
 * nguoi choi, doi vat pham, doi nhan cuoi, dung vat pham dac biet.
 *
 * Tach nguyen van tu real/Map.java. Deu la method PRIVATE cua Map nen khong
 * the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 *
 * LUU Y trung ten: da co san real/doBuySpecialItem.java va real/doTrade.java
 * la CLASS rieng, duoc RealController goi. Chung la ban sao DA TROI LECH cua
 * cung tinh nang (ban trong Map da duoc don tay, ban kia con tho tu trinh
 * dich nguoc). Chua gop vi gop sai la doi hanh vi game that.
 */
// Thanh vien static cua Map ma nhom nay dung khong dinh danh khi con nam trong Map.
// (trung dung bay import ma real/doBuySpecialItem.java dang dung - cung mot doan code)
import static real.Map.getTown;
import static real.Map.isModelClothes;
import static real.Map.isWearingAnimal;
import static real.Map.pause;
import static real.Map.doBuyModelClothe;
import static real.Map.doCreateHuyetBoDe;
import static real.Map.doCreateHuyetLinhThao;
import static real.Map.getShopTemplate;
import static real.Map.isSale;
import static real.Map.isSellHuyetLinhThao;
import static real.Map.petTemplate;

class MapMuaBan {

    static void doBuySpecialItem(Map map, Char player, Message msg) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else {
            String nameItem = "";
            int soluong = 0;
            long sumPrice = 0L;
            String moneyType = "luong";
            if (!map.checkTrade(player)) {
                try {
                    int id = msg.dis.readUnsignedByte();
                    ShopTemplate item = getShopTemplate(id);
                    if (player.isFullInventory()) {
                        try {
                            if (item.id > 1 && player.potions[item.id + 19] == 0 || item.id < 2 && !player.hadSpecialItem(item.id)) {
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

                    if (item.id == 13 && player.idClan == -1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm chỉ dành cho người đã tham gia bang", ""));
                        return;
                    }

                    int oldLuong = player.getLuong();
                    int oldLuongLock = player.getLuongLock();
                    long oldXu = player.getxu();
                    sumPrice = (long) item.getPrice(isSale);
                    int var21 = item.value;
                    nameItem = item.name;
                    if (map.isItemLock(item.id)) {
                        if (player.getLuongLock() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng khoá.", ""));
                            return;
                        }

                        moneyType = "luong_lock";
                        if ((item.id < 116 || item.id > 125) && item.id != 150 && item.id != 176) {
                            if (item.id == 126) {
                                player.potions[35]++;
                                player.subLuongLock((long) item.getPrice(isSale));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                nameItem = item.name + "_khoa";
                            } else if (item.id == 127) {
                                player.potions[94] = player.potions[94] + item.value;
                                player.subLuongLock((long) item.getPrice(isSale));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                nameItem = item.name + "_khoa";
                            } else if (item.id == 128) {
                                player.potions[96] = player.potions[96] + item.value;
                                player.subLuongLock((long) item.getPrice(isSale));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                nameItem = item.name + "_khoa";
                            }
                        } else {
                            if (item.id == 176) {
                                player.doAddGemItem(GemTemplate.BAN_DO_KHO_BAU, item.value, true);
                                nameItem = item.name;
                            } else if (item.id == 150) {
                                player.potions[73] = player.potions[73] + item.value;
                                nameItem = item.name + "_khoa";
                            } else {
                                int[] idgem = new int[]{71, 72, 78, 79, 85, 86, 92, 93, 99, 100};
                                player.doAddGemItem(idgem[item.id - 116], item.value, true);
                                nameItem = item.name;
                            }

                            player.subLuongLock((long) item.getPrice(isSale));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                        }
                    } else if (item.id == 172) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        player.subLuong((long) item.getPrice(isSale));
                        player.potions[77] = player.potions[77] + item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 166) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 736, 1, 0);
                        nameItem = "Ao Son Tinh_luong";
                    } else if (item.id == 167) {
                        moneyType = "xu";
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getxu() < (long) item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subXu((long) item.getPrice(isSale), false, "map 65");
                        doBuyModelClothe(player, 736, 1, 0);
                        nameItem = "Ao Son Tinh_xu";
                    } else if (item.id == 168) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 737, 1, 0);
                        nameItem = "Ao Thuy Tinh_luong";
                    } else if (item.id == 169) {
                        moneyType = "xu";
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getxu() < (long) item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subXu((long) item.getPrice(isSale), false, "map 71");
                        doBuyModelClothe(player, 737, 1, 0);
                        nameItem = "Ao Thuy Tinh_xu";
                    } else if (item.id == 170) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 740, Char.isSuKienBlackFriday() ? 60 : 1, 0);
                        nameItem = "Ao Tieu long nu";
                    } else if (item.id == 171) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 741, Char.isSuKienBlackFriday() ? 60 : 1, 0);
                        nameItem = "Ao Duong qua";
                    } else if (item.id == 174) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 744, 7, 0);
                        nameItem = "Ao tinh quan";
                    } else if (item.id == 175) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 743, 7, 0);
                        nameItem = "Ao tien nu";
                    } else if (item.id == 178) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 754, 7, 0);
                        nameItem = "Ao dai nam 2021";
                    } else if (item.id == 179) {
                        var21 = 1;
                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                            return;
                        }

                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        doBuyModelClothe(player, 755, 7, 0);
                        nameItem = "Ao dai nu 2021";
                    } else if (item.id >= 154 && item.id <= 164) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        if (player.gender == 1) {
                            if (item.id < 161) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Kiểu tóc chỉ dùng cho nữ", ""));
                                return;
                            }
                        } else if (player.gender == 2 && item.id >= 161) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Kiểu tóc chỉ dùng cho nam", ""));
                            return;
                        }

                        var21 = 1;
                        int oldhead = player.headStyle;
                        player.subLuong((long) item.getPrice(isSale));
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

                        if (player.getxu() < (long) item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        if (player.hlt_buy <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua thêm.", ""));
                            return;
                        }

                        var21 = 1;
                        nameItem = item.name;
                        player.hlt_buy--;
                        doCreateHuyetLinhThao(player, 0);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        Database.instance.saveOrtherLog("", player.charname, "mua hlt,sl con lai co the mua: " + player.hlt_buy, "bhlt");
                    } else if (item.id == 129) {
                        moneyType = "xu";
                        if (player.getxu() < (long) item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        player.subXu((long) item.getPrice(isSale), true, "map 66");
                        player.potions[97] = player.potions[97] + item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 130) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        player.subLuong((long) item.getPrice(isSale));
                        player.potions[116] = player.potions[116] + item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 131) {
                        moneyType = "xu";
                        if (player.buy_luot_van_tieu <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Một ngày chỉ có thể mua 3 thẻ vận tiêu.", ""));
                            return;
                        }

                        if (player.getxu() < (long) item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        player.buy_luot_van_tieu--;
                        player.subXu((long) item.getPrice(isSale), true, "map 67");
                        player.potions[82] = player.potions[82] + item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 135) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = nameItem + item.name;
                        player.subLuong((long) item.getPrice(isSale));
                        player.potions[142] = player.potions[142] + item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 177) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = nameItem + item.name;
                        player.subLuong((long) item.getPrice(isSale));
                        player.potions[105] = player.potions[105] + item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 148) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        player.subLuong((long) item.getPrice(isSale));
                        if (Char.isSuKienTrungThul2016()) {
                            player.potions[135] = player.potions[135] + item.value;
                        } else if (Char.isSuKienHaloween2016()) {
                            player.potions[139] = player.potions[139] + item.value;
                        }

                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 149) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        nameItem = item.name;
                        player.subLuong((long) item.getPrice(isSale));
                        player.potions[73] = player.potions[73] + item.value;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 151) {
                        moneyType = "xu";
                        if (player.getxu() < (long) item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                            return;
                        }

                        nameItem = item.name;
                        var21 = 1;
                        player.subXu((long) item.getPrice(isSale), false, "map 73");
                        int hour = 24 - UtilKPAH.getHour();
                        int minute = UtilKPAH.getMinute();
                        int value = hour * 60 - minute;
                        map.doBuyGayGiangSinh(player, value);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else if (item.id == 173) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                            return;
                        }

                        var21 = 1;
                        player.subLuong((long) item.getPrice(isSale));
                        int value = 10080;
                        player.createLan((long) value, true, 95);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        nameItem = item.name;
                    } else if (item.id == 152) {
                        if (player.getLuong() < item.getPrice(isSale)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            return;
                        }

                        if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                            return;
                        }

                        var21 = 1;
                        player.subLuong((long) item.getPrice(isSale));
                        int hour = 24 - UtilKPAH.getHour();
                        int minute = UtilKPAH.getMinute();
                        int value = hour * 60 - minute;
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
                            if (player.getxu() < (long) item.getPrice(isSale)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            nameItem = item.name;
                            player.subXu((long) item.getPrice(isSale), true, "map 68");
                            player.potions[77] = player.potions[77] + item.value;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else if (item.id == 146) {
                            if (player.getLuong() < item.getPrice(isSale)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            nameItem = item.name + "_luong";
                            player.subLuong((long) item.getPrice(isSale));
                            player.potions[104] = player.potions[104] + item.value;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else if (item.id == 147) {
                            moneyType = "xu";
                            if (player.getxu() < (long) item.getPrice(isSale)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            nameItem = item.name + "_xu";
                            player.subXu((long) item.getPrice(isSale), true, "map 69");
                            if (Char.isSuKienHe2017()) {
                                player.potions[104] = player.potions[104] + item.value;
                            } else if (Char.isSuKienHaloween2016()) {
                                player.potions[139] = player.potions[139] + item.value;
                            }

                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else if (item.id == 141) {
                            if (player.getLuong() < item.getPrice(isSale)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            var21 = 1;
                            nameItem = item.name;
                            player.subLuong((long) item.getPrice(isSale));
                            player.createPet(21, petTemplate[21].timeExpire);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else if (item.id == 132) {
                            if (player.getLuong() < item.getPrice(isSale)) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                return;
                            }

                            if (doCreateHuyetBoDe(player, 1)) {
                                nameItem = item.name;
                                var21 = 1;
                                player.subLuong((long) item.getPrice(isSale));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            }
                        } else {
                            label693:
                            if (item.id > 1) {
                                if (item.id != 50
                                        && item.id != 51
                                        && item.id != 52
                                        && item.id != 54
                                        && item.id != 55
                                        && item.id != 53
                                        && item.id != 56
                                        && item.id != 57
                                        && item.id != 58
                                        && item.id != 86
                                        && item.id != 87
                                        && item.id != 88
                                        && item.id != 89
                                        && item.id != 90
                                        && item.id != 91
                                        && item.id != 113
                                        && item.id != 114
                                        && item.id != 142
                                        && item.id != 143
                                        && item.id != 144
                                        && item.id != 145
                                        && item.id != 106
                                        && item.id != 105
                                        && item.id != 137
                                        && item.id != 138
                                        && item.id != 139
                                        && item.id != 140) {
                                    if (item.id != 92 && item.id != 107 && item.id != 110 && item.id != 111 && item.id != 133) {
                                        if (item.id >= 83 && item.id <= 85) {
                                            if (player.potions[item.id - 93 + 112] + item.value > 5) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua thêm.", ""));
                                                return;
                                            }

                                            if (player.getLuong() < item.getPrice(isSale)) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                return;
                                            }

                                            nameItem = item.name;
                                            player.subLuong((long) item.getPrice(isSale));
                                            player.potions[item.id - 83 + 112] = player.potions[item.id - 83 + 112] + item.value;
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                            Database.instance
                                                    .saveLogBuyItemShop(
                                                            player.charname,
                                                            item.name,
                                                            item.type,
                                                            item.getPrice(isSale),
                                                            "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong()
                                                    );
                                            break label693;
                                        }

                                        if (item.id == 108) {
                                            if (player.getLuong() < item.getPrice(isSale)) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                return;
                                            }

                                            nameItem = item.name;
                                            player.subLuong((long) item.getPrice(isSale));
                                            player.potions[13] = player.potions[13] + item.value;
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                            Database.instance
                                                    .saveLogBuyItemShop(
                                                            player.charname,
                                                            item.name,
                                                            item.type,
                                                            item.getPrice(isSale),
                                                            "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong()
                                                    );
                                        } else if (item.id == 134) {
                                            if (player.getLuong() < item.getPrice(isSale)) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                return;
                                            }

                                            nameItem = item.name;
                                            player.subLuong((long) item.getPrice(isSale));
                                            player.potions[77] = player.potions[77] + item.value;
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                            Database.instance
                                                    .saveLogBuyItemShop(
                                                            player.charname,
                                                            item.name,
                                                            item.type,
                                                            item.getPrice(isSale),
                                                            "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong()
                                                    );
                                        } else if (item.id == 109) {
                                            if (player.getLuong() < item.getPrice(isSale)) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                return;
                                            }

                                            nameItem = item.name;
                                            player.subLuong((long) item.getPrice(isSale));
                                            player.potions[8] = player.potions[8] + item.value;
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                            Database.instance
                                                    .saveLogBuyItemShop(
                                                            player.charname,
                                                            item.name,
                                                            item.type,
                                                            item.getPrice(isSale),
                                                            "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong()
                                                    );
                                        } else if (item.id == 112) {
                                            if (player.getLuong() < item.getPrice(isSale)) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                return;
                                            }

                                            var21 = 1;
                                            nameItem = item.name;
                                            player.subLuong((long) item.getPrice(isSale));
                                            player.genItemPhiPhong(608, true, 0, 43200000);
                                            Database.instance
                                                    .saveLogBuyItemShop(
                                                            player.charname,
                                                            item.name,
                                                            item.type,
                                                            item.getPrice(isSale),
                                                            "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong()
                                                    );
                                        } else if (item.id == 115) {
                                            if (player.getLuong() < item.getPrice(isSale)) {
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                return;
                                            }

                                            var21 = 1;
                                            nameItem = item.name;
                                            player.subLuong((long) item.getPrice(isSale));
                                            player.genItemPhiPhong(608, true, 1, 43200000);
                                            Database.instance
                                                    .saveLogBuyItemShop(
                                                            player.charname,
                                                            item.name,
                                                            item.type,
                                                            item.getPrice(isSale),
                                                            "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong()
                                                    );
                                        } else if (player.potions[item.id + 19] + item.value <= 800) {
                                            nameItem = item.name;
                                            if (item.type == 0) {
                                                if (player.getLuong() < item.getPrice(isSale)) {
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                                    return;
                                                }

                                                player.subLuong((long) item.getPrice(isSale));
                                            } else if (item.type == 1) {
                                                moneyType = "xu";
                                                if (player.getxu() < (long) item.getPrice(isSale)) {
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu.", ""));
                                                    return;
                                                }

                                                player.subXu((long) item.getPrice(isSale), true, "map 74");
                                            }

                                            player.potions[item.id + 19] = player.potions[item.id + 19] + item.value;
                                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                        } else {
                                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mua tối đa 800 bình.", ""));
                                        }
                                        break label693;
                                    }

                                    if (player.getLuong() < item.getPrice(isSale)) {
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                        return;
                                    }

                                    player.subLuong((long) item.getPrice(isSale));
                                    int var23 = 1;
                                    Pet pet = null;
                                    if (item.id == 92) {
                                        player.createPet(3, petTemplate[3].timeExpire);
                                        nameItem = item.name;
                                    } else if (item.id == 107) {
                                        player.createPet(9, 10080);
                                        nameItem = item.name;
                                    } else if (item.id == 110) {
                                        player.createPet(10, 10080);
                                        nameItem = item.name;
                                    } else if (item.id == 111) {
                                        player.createPet(11, 10080);
                                        nameItem = item.name;
                                    } else if (item.id == 133) {
                                        player.createPet(13, 10080);
                                        nameItem = item.name;
                                    }

                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Đã mua", ""));
                                    Database.instance
                                            .saveLogBuyItemShop(
                                                    player.charname,
                                                    item.name,
                                                    item.type,
                                                    item.getPrice(isSale),
                                                    "Xu_" + oldXu + "_" + player.getxu() + "_Luong_" + oldLuong + "_" + player.getLuong()
                                            );
                                    Database.instance.saveLogThongKeChiTieu(nameItem, 0, var23, sumPrice, moneyType);
                                    return;
                                }

                                if (player.getLuong() < item.getPrice(isSale)) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                    return;
                                }

                                int var22 = 1;
                                nameItem = item.name;
                                player.subLuong((long) item.getPrice(isSale));
                                if ((item.id == 139 || item.id == 140) && Char.isSuKienBlackFriday()) {
                                    doBuyModelClothe(player, item, 60);
                                } else if (item.id != 90 && item.id != 91) {
                                    doBuyModelClothe(player, item, item.id != 88 && item.id != 86 ? 7 : 15);
                                } else {
                                    doBuyModelClothe(player, item, 10);
                                }

                                Message m = new Message(85);
                                player.sendMessage(m);
                                Database.instance.saveLogThongKeChiTieu(nameItem, 0, var22, sumPrice, moneyType);
                                return;
                            } else {
                                if (player.getLuong() < item.getPrice(isSale)) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                                    return;
                                }

                                if (player.listSpItem[item.id] >= 3) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mua thêm.", ""));
                                    return;
                                }

                                if (player.getAllSP() >= 126) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại bạn chỉ có thể mua tối đa 126 vé", ""));
                                    return;
                                }

                                nameItem = item.name;
                                player.subLuong((long) item.getPrice(isSale));
                                if (player.listSpItem[item.id] == 0) {
                                    ShopTemplate spItem = new ShopTemplate();
                                    spItem.coppy(spItem, item);
                                    spItem.ownerId = player.charDBID;
                                    spItem.realId = player.getIDItem();
                                    player.specialItem.add(spItem);
                                }

                                player.listSpItem[item.id]++;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            }
                        }
                    }

                    Database.instance.saveLogThongKeChiTieu(nameItem, 0, var21, sumPrice, moneyType);
                    Database.instance
                            .saveLogBuyItemShop(
                                    player.charname,
                                    item.name,
                                    item.type,
                                    item.getPrice(isSale),
                                    "Xu_"
                                    + oldXu
                                    + "_"
                                    + player.getxu()
                                    + "_Luong_"
                                    + oldLuong
                                    + "_"
                                    + player.getLuong()
                                    + "_lk_"
                                    + oldLuongLock
                                    + "_"
                                    + player.getLuongLock()
                            );
                    player.sendMessage(MessageCreator.createCharSpecialItem(player));
                    Message m = new Message(85);
                    player.sendMessage(m);
                    m.cleanup();
                } catch (Exception var18) {
                    var18.printStackTrace();
                }
            }
        }
    }

    static void doTrade(Map map, Char player, Message msg, int from) {
        if (pause) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì.", ""));
        } else {
            Message m = null;

            try {
                try {
                    if (player.isKiller) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không thể sử dụng chức năng này khi đang ở chế độ PK", ""));
                        return;
                    }

                    short userId = 0;
                    int cmd = msg.dis.readByte();
                    Char p = null;
                    int itemId = 0;
                    switch (cmd) {
                        case -1:
                            userId = msg.dis.readShort();
                            p = player.getCharFromNearChar(userId);
                            if (p != null) {
                                p.userTrade.removeAllElements();
                                player.userTrade.removeAllElements();
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
                            if (p == null) {
                                return;
                            }

                            if ((getTown[player.myCountry] || getTown[p.myCountry]) && player.mapID == 201) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời trong thời gian chiếm thành", ""));
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

                            if (p.isKiller) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đề nghị khi người chơi đang ở chế độ PK", ""));
                                return;
                            }

                            if (from == 1 && p.idWedding > -1) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Người bạn muốn mời đã đồng ý tham dự 1 tiệc cưới khác.", ""));
                                return;
                            }

                            player.timeInviteTrade = System.currentTimeMillis();
                            m = new Message(66);
                            m.dos.writeByte(0);
                            m.dos.writeShort(player.id);
                            p.sendMessage(m);
                            return;
                        case 1:
                            userId = msg.dis.readShort();
                            p = player.getCharFromNearChar(userId);
                            if (p == null) {
                                return;
                            }

                            if (player.userTrade.size() <= 0 && p.userTrade.size() <= 0) {
                                if (p.userTrade.size() == 0) {
                                    p.userTrade.add(player);
                                }

                                if (player.userTrade.size() == 0) {
                                    player.userTrade.add(p);
                                }

                                m = new Message(66);
                                m.dos.writeByte(1);
                                m.dos.writeShort(player.id);
                                p.sendMessage(m);
                                player.sendMessage(m);
                                return;
                            }

                            return;
                        case 2:
                            if (!player.isCorrectPass && !player.passWord.equals("")) {
                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                                return;
                            }

                            if (player.userTrade.size() == 0) {
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
                                if (player.tPotions[idpotion] <= 0) {
                                    m = MessageCreator.createServerAlertMessage("Không thể lấy ra vật phẩm này", "");
                                    player.sendMessage(m);
                                    m.cleanup();
                                    return;
                                }

                                player.tPotions[idpotion] = 0;
                                ((Char) player.userTrade.get(0)).rPotions[idpotion] = 0;
                                m = new Message(66);
                                m.dos.writeByte(2);
                                m.dos.writeByte(2);
                                m.dos.writeShort(player.id);
                                m.dos.writeByte(idpotion);
                                player.sendMessage(m);
                                ((Char) player.userTrade.get(0)).sendMessage(m);
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

                            if (idPotion == 0) {
                                return;
                            }

                            PotionTemplate2 potion = Map.potionTemplates2.get(idPotion);
                            if (potion == null || !potion.isTrade) {
                                return;
                            }
                            if (((Char) player.userTrade.get(0)).isFullInventory() && ((Char) player.userTrade.get(0)).potions[idPotion] <= 0) {
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

                            if (((Char) player.userTrade.get(0)).potions[idPotion]
                                    + ((Char) player.userTrade.get(0)).rPotions[idPotion]
                                    - player.rPotions[idPotion]
                                    + soluong
                                    <= 999
                                    || idPotion == 85
                                    || idPotion == 79
                                    || idPotion == 82
                                    || idPotion == 80
                                    || idPotion == 81) {
                                player.tPotions[idPotion] = player.tPotions[idPotion] + soluong;
                                ((Char) player.userTrade.get(0)).rPotions[idPotion] += soluong;
                                m = new Message(66);
                                m.dos.writeByte(2);
                                m.dos.writeByte(1);
                                m.dos.writeShort(player.id);
                                m.dos.writeByte(idPotion);
                                m.dos.writeShort(soluong);
                                player.sendMessage(m);
                                ((Char) player.userTrade.get(0)).sendMessage(m);
                                return;
                            }

                            m = MessageCreator.createServerAlertMessage(
                                    "Chỉ có thể trao đổi thêm "
                                    + (999
                                    - player.rPotions[idPotion]
                                    - ((Char) player.userTrade.get(0)).potions[idPotion]
                                    + ((Char) player.userTrade.get(0)).rPotions[idPotion])
                                    + " vật phẩm",
                                    ""
                            );
                            player.sendMessage(m);
                            m.cleanup();
                            return;
                        case 3:
                            player.finishTrade = false;
                            player.lockTrade = false;
                            player.tItems.removeAllElements();
                            player.rItems.removeAllElements();
                            ((Char) player.userTrade.get(0)).finishTrade = false;
                            ((Char) player.userTrade.get(0)).lockTrade = false;
                            ((Char) player.userTrade.get(0)).tItems.removeAllElements();
                            ((Char) player.userTrade.get(0)).rItems.removeAllElements();
                            ((Char) player.userTrade.get(0)).userTrade.removeAllElements();
                            player.tPotions = null;
                            player.rPotions = null;
                            player.tPotions = new int[160];
                            player.rPotions = new int[160];
                            ((Char) player.userTrade.get(0)).tPotions = null;
                            ((Char) player.userTrade.get(0)).rPotions = null;
                            ((Char) player.userTrade.get(0)).tPotions = new int[160];
                            ((Char) player.userTrade.get(0)).rPotions = new int[160];
                            m = new Message(66);
                            m.dos.writeByte(3);
                            ((Char) player.userTrade.get(0)).sendMessage(m);
                            player.userTrade.removeAllElements();
                            m.cleanup();
                            return;
                        case 4:
                            if (!player.finishTrade) {
                                player.finishTrade = true;
                                if (((Char) player.userTrade.get(0)).finishTrade) {
                                    m = new Message(66);
                                    m.dos.writeByte(5);
                                    player.sendMessage(m);
                                    ((Char) player.userTrade.get(0)).sendMessage(m);
                                    return;
                                }
                            }

                            return;
                        case 5:
                            if (player.finishTrade) {
                                player.lockTrade = true;
                                if (((Char) player.userTrade.get(0)).finishTrade && ((Char) player.userTrade.get(0)).lockTrade && player.finishTrade) {
                                    player.doAddItemTrad2Inventory();
                                    m = new Message(66);
                                    m.dos.writeByte(4);
                                    player.sendMessage(m);
                                    ((Char) player.userTrade.get(0)).sendMessage(m);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    Database.instance.saveCharAuto(player);
                                    Database.instance.saveCharAuto((Char) player.userTrade.get(0));
                                    ((Char) player.userTrade.get(0)).sendMessage(MessageCreator.createCharInventoryMessage((Char) player.userTrade.get(0), 0));
                                    ((Char) player.userTrade.get(0)).sendMessage(MessageCreator.createCharInventoryMessage((Char) player.userTrade.get(0), 1));
                                    ((Char) player.userTrade.get(0)).userTrade.removeAllElements();
                                    player.userTrade.removeAllElements();
                                }

                                return;
                            }
                    }
                } catch (Exception var18) {
                }
            } finally {
                if (m != null) {
                    m.cleanup();
                }
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
        // Vat pham skin linh thu mang type 19 giong phi phong nen phai chan ngay o day,
        // khong thi no roi xuong nhanh do nhan vat va chiem mat o phi phong. Phan biet
        // bang ID TEMPLATE, khong bang type — xem real.SkinLinhThu.ID_ITEM.
        if (SkinLinhThu.laItemSkin(iItem.getTemplate().id)) {
            if (player.animalRide == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Phải cưỡi linh thú mới trang bị được skin.", ""));
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
