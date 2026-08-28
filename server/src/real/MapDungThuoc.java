package real;


import data.Database;
import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import real.cmd.LoginHandler;
import server.TeamServer;

/**
 * Than cua Map.doUsePotion - nguoi choi dung binh thuoc / vat pham tieu hao.
 *
 * 760 dong nay truoc nam trong real/Map.java. Map.doUsePotion VAN CON, giu
 * NGUYEN chu ky protected cu, chi uy nhiem sang day.
 *
 * Khong nham voi real/doUsePotion.java - do la CLASS rieng do RealController
 * goi, ban sao da troi lech cua cung tinh nang (xem ghi chu o MapMuaBan.java).
 */
class MapDungThuoc {

    static void doUsePotion(Map map, Char player, Message message) throws IOException {
        if (!map.checkTrade(player) && !player.freeze()) {
            DataInputStream dis = message.dis;
            short potionType = (short) dis.readUnsignedByte();
            if (player.potions[potionType] > 0) {
                if (player.isHoangSo()) {
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (!player.map.isMapChienTruongMoba() && potionType >= 126 && potionType <= 132 && !TeamServer.isServerLocal()) {
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (player.map.isMapChienTruongMoba() && (potionType < 126 || potionType > 132) && potionType != 100 && !TeamServer.isServerLocal()) {
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (potionType >= 14 && potionType <= 18) {
                    if (map.mapId != 204 && map.mapId != 205 && !player.map.isMapLoiDai()) {
                        if (player.killer <= 0 && !player.isKiller) {
                            Message m = new Message(65);
                            m.dos.writeShort(player.id);
                            if (player.pk == 0) {
                                if (player.isKiller) {
                                    return;
                                }

                                player.pk = (byte) potionType;
                                m.dos.writeByte(1);
                                m.dos.writeByte(potionType);
                                player.timeUsePK = System.currentTimeMillis();
                            } else {
                                if (System.currentTimeMillis() - player.timeUsePK <= 150000L) {
                                    player.isKiller = true;
                                    player.killer = (short) (player.killer + 300);
                                    Message msg = new Message(67);
                                    msg.dos.writeShort(player.id);
                                    msg.dos.writeByte(1);
                                    msg.dos.writeShort(player.killer);
                                    player.sendMessage(msg);
                                    player.sendToNearPlayer(msg);
                                    Database.instance.saveOrtherLog("tob_other_log", player.charname, "hack khan ", "hk");
                                    return;
                                }

                                m.dos.writeByte(0);
                                m.dos.writeByte(potionType);
                                player.pk = 0;
                            }

                            player.sendMessage(m);
                            player.sendToNearPlayer(m);
                            m.cleanup();
                        }
                    }
                } else if (potionType != 1
                        && potionType != 2
                        && potionType != 3
                        && potionType != 21
                        && potionType != 22
                        && potionType != 93
                        && potionType != 94
                        && potionType != 126
                        || !player.checkChoang()) {
                    player.potions[potionType]--;
                    if (potionType == 19) {
                        if (player.map.isvanTienTran()) {
                            player.map.doReturnVillage(player);
                        } else if (Char.isSuKienMiniChucNu() && player.charCopy != null && player.map.mapId == player.charCopy.map.mapId) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi đang làm nhiệm vụ tìm Chức Nữ", ""));
                        } else if (player.char_quest != null && player.char_quest.receive == 1 && QuestTemplate.getTypeQuest(player.char_quest.id_quest) == 1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi đang làm nhiệm vụ vận chuyển", ""));
                        } else if (player.map.isMapNuiChauBau()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi đang ở trong núi châu báu", ""));
                        } else {
                            player.isThodia = true;
                        }
                    } else if (player.hp <= 0) {
                        player.potions[potionType]++;
                        map.doUseHP(player, potionType, -10000000);
                    } else {
                        CharChienTruong c = null;
                        switch (potionType) {
                            case 1:
                            map.doUseHP(player, potionType, Map.potionTemplates2.get(1).getRecovered());
                                break;
                            case 2:
                                map.doUseHP(player, potionType, Map.potionTemplates2.get(2).getRecovered());
                                break;
                            case 3:
                                map.doUseHP(player, potionType, Map.potionTemplates2.get(3).getRecovered());
                                break;
                            case 4:
                                map.doUseMP(player, potionType, Map.potionTemplates2.get(4).getRecovered());
                                break;
                            case 5:
                                map.doUseMP(player, potionType, Map.potionTemplates2.get(5).getRecovered());
                                break;
                            case 6:
                                map.doUseMP(player, potionType, Map.potionTemplates2.get(6).getRecovered());
                                break;
                            case 7:
                            case 8:
                            case 13:
                            case 31:
                            case 33:
                            case 36:
                            case 64:
                            case 65:
                            case 66:
                            case 67:
                            case 68:
                            case 69:
                            case 70:
                            case 72:
                            case 73:
                            case 74:
                            case 77:
                            case 83:
                            case 86:
                            case 88:
                            case 89:
                            case 90:
                            case 92:
                            case 104:
                            case 105:
                            case 115:
                            case 117:
                            case 118:
                            case 119:
                            case 120:
                            case 121:
                            case 122:
                            case 124:
                            case 125:
                            case 136:
                            case 137:
                            case 138:
                            case 139:
                            case 142:
                            case 145:
                            case 148:
                            case 149:
                            case 150:
                            case 151:
                            case 152:
                            case 153:
                            case 154:
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng", ""));
                                player.potions[potionType]++;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                break;
                            case 9:
                                if (TeamServer.isServerHoaLu() && Char.isSuKienDuaTopHoaLu()) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng trong thời gian đua top", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                Map.addXPForChar(player, player.lvDetail.get1PCExp(), true, "dousepotion type_nhan_sam");
                                Database.instance.saveOrtherLog("", player.charname, player.lvDetail.get1PCExp() + " su dung exp don " + potionType, "nhansam");
                                break;
                            case 10:
                            case 11:
                            case 12:
                                if ((TeamServer.isServerHoaLu() || TeamServer.isServerLocal()) && Char.isSuKienDuaTopHoaLu()) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng trong thời gian đua top", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                int[] expdon = new int[]{100000, 500000, 1000000};
                                Map.addXpCharEvent(player, (long) expdon[potionType - 10], false, "dousepotion type_exp_don");
                                Database.instance.saveOrtherLog("", player.charname, "su dung exp don " + potionType, "expdon");
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 62:
                            case 76:
                            case 103:
                            case 106:
                            case 107:
                            case 108:
                            case 109:
                            case 110:
                            case 111:
                            case 156:
                            case 157:
                            case 158:
                            default:
                                break;
                            case 21:
                                map.doUseHP(player, potionType, 1500);
                                break;
                            case 22:
                                map.doUseHP(player, potionType, 3000);
                                break;
                            case 23:
                                map.doUseMP(player, potionType, 2500);
                                break;
                            case 24:
                                map.doUseMP(player, potionType, 3500);
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                break;
                            case 25:
                                player.resetTN();
                                Database.instance.saveOrtherLog("tob_other_log", player.charname, "tay tiem nang " + player.potions[25], "dtn");
                                break;
                            case 26:
                                player.resetKN();
                                Database.instance.saveOrtherLog("tob_other_log", player.charname, "tay tiem nang " + player.potions[26], "dkn");
                                break;
                            case 27:
                                if (player.monster != null && player.monster.map.mapId == player.mapID) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi đang vận tiêu.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (System.currentTimeMillis() - player.timeUseThanHanhPhu < 0L) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    "Chỉ có thể sử dụng vật phẩm này sau " + (player.timeUseThanHanhPhu - System.currentTimeMillis()) / 1000L + "s", ""
                                            )
                                    );
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.sendMessage(
                                        MessageCreator.createServerAlertMessage(
                                                "Chỉ có thể sử dụng vật phẩm này sau " + (player.timeUseThanHanhPhu - System.currentTimeMillis()) / 1000L + "s", ""
                                        )
                                );
                                if (player.inCountry != player.myCountry) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể sử dụng Thần Hành Phù trên lãnh thổ của mình", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (player.mapID == 225 || player.mapID == 226 || player.mapID == Map.idMapTown || player.map.isMapNuiChauBau()) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng tại vị trí này", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                Vector<String> name = Map.getCharKiller(player.inCountry);
                                int size = name.size();
                                if (size == 0) {
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (player.rankGov != -1) {
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }

                                player.listKiller.removeAllElements();
                                if (size > 30) {
                                    size = 30;
                                }

                                String[] menu = new String[size];

                                for (int i = 0; i < size; i++) {
                                    try {
                                        String n = (String) name.get(name.size() - (i + 1));
                                        menu[i] = n;
                                        player.listKiller.add(n);
                                    } catch (Exception var19) {
                                    }
                                }

                                player.sendMessage(MessageCreator.createMsgMenu(menu, -400, 0));
                                break;
                            case 28:
                                if (player.rankGov > -1) {
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    player.sendMessage(MessageCreator.createMsgInputText(-32014, 0, "Nhập tên", 0));
                                }
                                break;
                            case 29:
                                short old = player.killer;
                                player.killer = (short) (player.killer - 100);
                                if (player.killer < 0) {
                                    player.killer = 0;
                                }

                                if (player.killer == 0) {
                                    player.isKiller = false;
                                }

                                Message mm = new Message(67);
                                mm.dos.writeShort(player.id);
                                mm.dos.writeByte(player.isKiller ? 1 : 0);
                                mm.dos.writeShort(player.killer);
                                player.sendMessage(mm);
                                player.sendToNearPlayer(mm);
                                mm.cleanup();
                                Database.instance.saveOrtherLog("tob_log_other_potion", player.charname, old + " Sử dụng phiếu công ích ", "upci");
                                break;
                            case 30:
                            case 34:
                                map.doRideHorse(player, potionType);
                                break;
                            case 32:
                                player.sendMessage(MessageCreator.createServerAlertMessage("Đấu trường đang bảo trì.", ""));
                                player.potions[potionType]++;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                break;
                            case 35:
                                player.setX2(4);
                                player.sendMessage(MessageCreator.createServerAlertMessage("Thời gian nhận được 100% kinh nghiệm của bạn còn 60 phút.", ""));
                                Database.instance.saveOrtherLog("", player.charname, "Sử dụng vé h vàng 100% 1h", "exp1h");
                                break;
                            case 37:
                            case 38:
                            case 39:
                            case 40:
                            case 41:
                            case 42:
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 48:
                            case 49:
                                int temp = potionType - 37;
                                if (player.gender == 1) {
                                    if (temp % 2 == 0) {
                                        player.headStyle = (byte) temp;
                                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                    } else {
                                        player.potions[potionType]++;
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Kiểu tóc chỉ dùng cho nữ", ""));
                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    }
                                } else if (player.gender == 2) {
                                    if (temp % 2 == 0) {
                                        player.potions[potionType]++;
                                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                        player.sendMessage(MessageCreator.createServerAlertMessage("Kiểu tóc chỉ dùng cho nam", ""));
                                    } else {
                                        player.headStyle = (byte) temp;
                                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                    }
                                }
                                break;
                            case 50:
                            case 51:
                            case 52:
                            case 53:
                            case 54:
                            case 55:
                            case 56:
                            case 57:
                            case 58:
                            case 59:
                            case 60:
                            case 61:
                                player.potions[potionType]++;
                                player.idPotionUsed = potionType;
                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 0, "Người nhận", 0));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                break;
                            case 63:
                                Map.addXpCharEvent(player, 7463228L, false, "dousepotion type_hop_may_man");
                                break;
                            case 71:
                                map.doCreateHoaKyLan(player);
                                break;
                            case 75:
                                player.setX2(5);
                                player.sendMessage(MessageCreator.createServerAlertMessage("Thời gian nhận được 100% kinh nghiệm của bạn còn 180 phút.", ""));
                                Database.instance.saveOrtherLog("", player.charname, "Sử dụng vé h vàng 100% 3h", "exp3h");
                                break;
                            case 78:
                                player.potions[potionType]++;
                                player.idPotionUsed = potionType;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                map.doSelectPosBuildLienHoa(player);
                                break;
                            case 79:
                                Map.addXpCharEvent(player, 100000L, false, "dousepotion type_hop_than_ky");
                                break;
                            case 80:
                                player.timeUp5Attribute = System.currentTimeMillis() + 259200000L;
                                player.calculateAttrib();
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                break;
                            case 81:
                                player.setX2(3);
                                player.sendMessage(MessageCreator.createServerAlertMessage("Thời gian nhận được 150% kinh nghiệm của bạn còn 60 phút.", ""));
                                Message m = new Message(86);
                                m.dos.writeByte(0);
                                player.sendMessage(m);
                                Database.instance.saveOrtherLog("", player.charname, "Sử dụng vé h vàng 150%", "exp150");
                                break;
                            case 82:
                                player.luot_van_tieu++;
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn có thêm 1 lượt vận tiêu", ""));
                                break;
                            case 84:
                                if (player.timeCrazy != 0L) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi bạn đang trong trạng thái nộ", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.timeCrazy = System.currentTimeMillis();
                                player.isCrazy((short) 0);
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                                MessageCreator.createMsgCharMonster(player, player);
                                break;
                            case 85:
                                player.potions[potionType]++;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                if (player.mapID == 225 || player.mapID == 226) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng tại vị trí này", ""));
                                    return;
                                }

                                map.doSummons(player);
                                break;
                            case 87:
                                player.potions[potionType]++;
                                player.idPotionUsed = potionType;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                map.doPhongChuc(player);
                                break;
                            case 91:
                                if (!player.map.isMapTrain() && !Map.isMapLang(player.map) && !Map.isMapThanh(player.map)) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể vào khu mỏ khi ở làng hoặc map luyện quái.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (player.monster != null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang vận tiêu", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (player.myCountry != player.inCountry) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể vào khu mỏ từ lãnh thổ của mình.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (player.getLevel() < 20) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 20 trở lên mới được vào khu mỏ.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.x = (45 + Database.r.nextInt() % 30) * 16;
                                player.y = (77 + Database.r.nextInt() % 30) * 16;
                                byte[] xx = new byte[]{45, 45, 18, 83};
                                byte[] yx = new byte[]{20, 76, 44, 44};
                                int rdd = Map.r.nextInt(4);
                                map.move2Map(player, xx[rdd], yx[rdd], 17, player.inCountry);
                                break;
                            case 93:
                                map.doUseHP(player, potionType, 7000);
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                break;
                            case 94:
                                map.doUseHP(player, potionType, 15000);
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                break;
                            case 95:
                                map.doUseMP(player, potionType, 7000);
                                player.doSendProperties();
                                break;
                            case 96:
                                map.doUseMP(player, potionType, 15000);
                                player.doSendProperties();
                                break;
                            case 97:
                            case 116:
                                if (player.monster == null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi bạn đang chưa nhận tiêu", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (!player.canChangeTieu()) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đổi tiêu tại vị trí này", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.doChangeTieu(potionType == 116 ? 1 : 0);
                                break;
                            case 98:
                                if (!player.map.isMapTrain() && !Map.isMapLang(player.map) && !Map.isMapThanh(player.map)) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể dịch chuyển khi ở làng hoặc map luyện quái.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (player.monster != null && player.monster.map.mapId == player.mapID) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang vận tiêu", ""));
                                    return;
                                }

                                if (System.currentTimeMillis() - player.timeUseThanHanhPhu < 0L) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    "Chỉ có thể sử dụng vật phẩm này sau " + (player.timeUseThanHanhPhu - System.currentTimeMillis()) / 1000L + "s", ""
                                            )
                                    );
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                Char p1 = CharManager.instance.getCharByCharName(player.nameHusband_wife);
                                if (player.mapID == 225 || player.mapID == 226) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng tại vị trí này", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (!p1.map.isMapTrain() && !Map.isMapLang(p1.map) && !Map.isMapThanh(p1.map)) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage("Chỉ có thể dịch chuyển khi người chơi đang ở làng hoặc map luyện quái.", "")
                                    );
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                String[] namnu = new String[]{"Phu nhân", "Phu quân"};
                                if (p1 == null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage(namnu[player.gender - 1] + " của bạn hiện tại không online.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (p1.mapID == 225 || p1.mapID == 226) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage(namnu[player.gender - 1] + " của bạn đang dự tiệc cưới.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (RealController.mapList.get(p1.mapID) == null) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    "Không thể dịch chuyển đến vị trí "
                                                    + namnu[player.gender - 1]
                                                    + " của bạn khi "
                                                    + namnu[player.gender - 1]
                                                    + " bạn đang giao dịch với npc.",
                                                    ""
                                            )
                                    );
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                map.move2Map(player, p1.x / 16, p1.y / 16, p1.mapID, p1.inCountry);
                                break;
                            case 99:
                                if (player.mapID != 225 && player.mapID != 226) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng tại vị trí này", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                map.sendDynamicEff(player);
                                break;
                            case 100:
                                player.sendMessage(MessageCreator.createMsgInputText(player.id, 1, "Nhập nội dung (50 ký tự).", 0));
                                player.potions[potionType]++;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                break;
                            case 101:
                                MapQuaTang.openBox(map, player, 0);
                                break;
                            case 102:
                                MapQuaTang.openBox(map, player, 1);
                                break;
                            case 112:
                            case 113:
                            case 114:
                                if (player.animalRide == null) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa cưỡi linh thú", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                } else {
                                    player.animalRide.doEat(potionType);
                                    player.calculateAttrib();
                                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                    Database.instance.saveOrtherLog("", player.charname, LoginHandler.PORTION_NAME[potionType], "eFood");
                                }
                                break;
                            case 123:
                                if (player.timeCrazy != 0L) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi bạn đang trong trạng thái nộ", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.timeCrazy = System.currentTimeMillis();
                                player.isCrazy((short) 1);
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                                MessageCreator.createMsgCharMonster(player, player);
                                break;
                            case 126:
                                map.doUseHP(player, potionType, 20000);
                                break;
                            case 127:
                                map.doUseMP(player, potionType, 20000);
                                break;
                            case 128:
                                if (player.timeCrazy != 0L) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi bạn đang trong trạng thái nộ", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.timeCrazy = System.currentTimeMillis();
                                player.isCrazy((short) 0);
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                                break;
                            case 129:
                                c = MapChienTruongMoba.getCharChienTruong(player.charname);
                                c.timeHonDon = System.currentTimeMillis() + 90000L;
                                player.sendInfoChienTruong(Char.ID_PT_HON_DON, 90);
                                break;
                            case 130:
                                c = MapChienTruongMoba.getCharChienTruong(player.charname);
                                c.timeHonNguyen = System.currentTimeMillis() + 90000L;
                                player.sendInfoChienTruong(Char.ID_PT_HON_NGUYEN, 90);
                                break;
                            case 131:
                                c = MapChienTruongMoba.getCharChienTruong(player.charname);
                                c.timeHoaCong = System.currentTimeMillis() + 90000L;
                                player.sendInfoChienTruong(Char.ID_PT_HOA_CONG, 90);
                                break;
                            case 132:
                                player.doUseHoaDuoc();
                                break;
                            case 133:
                                if (player.nBinhTra <= 0) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã sử dụng hết số lượng hộp bánh trong ngày hôm nay.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.nBinhTra--;
                                Map.addXpCharEvent(player, 500000L, false, "");
                                Database.instance.saveOrtherLog("", player.charname, "dung hpo thap cam " + player.potions[133], "hoptc");
                                break;
                            case 134:
                                if (player.nHopMut <= 0) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã sử dụng hết số lượng hộp bánh trong ngày hôm nay.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (player.isFullInventory()) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.nHopMut--;
                                player.doOpenHopBanhTrungThuDacBiet();
                                Database.instance.saveOrtherLog("", player.charname, "dung chu phuc " + player.potions[133], "hoptc");
                                break;
                            case 135:
                                player.setTimeEatSocola();
                                Database.instance.saveOrtherLog("", player.charname, LoginHandler.PORTION_NAME[potionType], "hatmam");
                                break;
                            case 140:
                                Map.doHopQuaHaloween2016(player);
                                break;
                            case 141:
                                MapQuaTang.doHopQuaTangHaloween2016(map, player);
                                break;
                            case 143:
                                if (player.isFullInventory()) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi hành trang đầy.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                if (!Char.isFinishAllSuKienHaloween2016() && !TeamServer.isServerLocal()) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Thời gian mở hộp quà bắt đầu từ 0h ngày 19-11-2020.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.potions[potionType]++;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                Map.doHopQuaBiMatHaloween2016(player);
                                break;
                            case 144:
                                map.doUseGoldKey(player);
                                break;
                            case 146:
                                if (player.nBinhTra <= 0) {
                                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã sử dụng hết số lượng kem trong ngày hôm nay.", ""));
                                    player.potions[potionType]++;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                    return;
                                }

                                player.nBinhTra--;
                                Map.addXpCharEvent(player, 300000L, false, "");
                                Database.instance.saveOrtherLog("", player.charname, "an kem nhan 300k exp " + player.potions[potionType], "ankem");
                                break;
                            case 147:
                                player.doOpenTuiQuaMayManNoel();
                                break;
                            case 155:
                                MapSuKien.doBanPhaoHoaTet2020(map, player);
                                break;
                            case 159:
                                Char.doOpenLixi2021(player);
                        }
                    }
                }
            }
        }
    }

}
