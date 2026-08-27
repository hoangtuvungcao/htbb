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
 * Tuong tac xa hoi giua nguoi choi: to doi (party), moi thach dau, ban be,
 * chat thuong va gui tin nhan.
 *
 * Tach nguyen van tu real/Map.java. Deu la method PRIVATE cua Map nen khong
 * the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 */
// Thanh vien static cua Map ma nhom nay dung khong dinh danh khi con nam trong Map.
import static real.Map.allWedding;
import static real.Map.checkBadWord;
import static real.Map.createCharInfo;
import static real.Map.getTown;
import static real.Map.idMapTown;
import static real.Map.replaceExtendDomain;
import static real.Map.replaceStringWithSpace;

class MapXaHoi {

    static void doSendChallenParty(Map map, Char player, String text, int idMenu) {
        if (idMenu == 0) {
            int money;
            try {
                money = Integer.parseInt(text.trim());
            } catch (Exception exNum) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thách đấu với số tiền này", ""));
                return;
            }
            if (money < 0 || money > 10000000) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thách đấu với số tiền này", ""));
                return;
            }

            if (player.partyID == -1) {
                return;
            }

            long min = -1L;

            for (int i = 0; i < player.party.userParty.size(); i++) {
                if (((Char) player.party.userParty.get(i)).getxu() < min || min == -1L) {
                    min = ((Char) player.party.userParty.get(i)).getxu();
                }
            }

            if (min < (long) money) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể cược với số xu là : " + min + " xu", ""));
                return;
            }

            player.moneyThachdau = money;
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 9, "Nhập tên đối thủ", 0));
        } else if (idMenu == 1) {
            Char p = CharManager.instance.getCharByCharName(text.trim().toLowerCase());
            if (p == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy đối thủ của bạn", ""));
                return;
            }

            if (p.partyID == -1 || player.partyID == -1) {
                return;
            }

            if (p.idArena > -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " đã chấp nhận tham gia một trận đấu khác.", ""));
                return;
            }

            if (!p.rcvInviteVip) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                return;
            }

            long min = -1L;

            for (int ix = 0; ix < p.party.userParty.size(); ix++) {
                if (((Char) p.party.userParty.get(ix)).getxu() < min || min == -1L) {
                    min = ((Char) p.party.userParty.get(ix)).getxu();
                }
            }

            if (min < (long) player.moneyThachdau) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể cược với số xu là : " + min + " xu", ""));
                return;
            }

            p.sendMessage(
                    MessageCreator.createMsgPopUp(
                            p.id, 11, player.charname + " gửi lời thách đấu tới nhóm của bạn với số tiền cược là " + player.moneyThachdau + " xu. Bạn có đồng ý không?"
                    )
            );
            p.nameCompetitor = player.charname;
        }
    }

    static void doAddFriend(Map map, Char player, Message msg) {
        try {
            if (player.myCountry == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạ chưa chọn quốc gia", ""));
                return;
            }

            int charid = msg.dis.readShort();
            int ok = msg.dis.readByte();
            Char p = CharManager.instance.getByCharID((short) charid);
            if (p == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Ngươi bạn mời hiện đang offline", ""));
                return;
            }

            if (p.myCountry == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người đã chọn quốc gia", ""));
                return;
            }

            if (p.map.isMapLoiDai()) {
                return;
            }

            if (player.myCountry != p.myCountry) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể kết bạn với người trong cùng một làng", ""));
                return;
            }

            Message m = new Message(101);
            if (player.friendListID.size() == 0) {
                Database.instance.getFriendlist(player);
            }

            if (ok == 0) {
                if (!p.rcvInvite || !p.rcvInviteVip) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                    return;
                }

                if (player.friendListID.size() > 0) {
                    for (int i = 0; i < p.friendListID.size(); i++) {
                        if ((Integer) p.friendListID.get(i) == player.charDBID) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã có trong danh sách bạn", ""));
                            return;
                        }
                    }
                }

                m.dos.writeByte(0);
                m.dos.writeUTF(player.charname);
                m.dos.writeShort(player.id);
                p.sendMessage(m);
            } else if (ok == 1) {
                p.friendListID.add(player.charDBID);
                player.friendListID.add(p.charDBID);
                p.friendListName.add(player.charname);
                player.friendListName.add(p.charname);
                p.myFriend.add(createCharInfo(player));
                player.myFriend.add(createCharInfo(p));
                Database.instance.saveFriendList(p);
                Database.instance.saveFriendList(player);
                p.sendMessage(MessageCreator.createMsgFriendList(p));
                player.sendMessage(MessageCreator.createMsgFriendList(player));
                p.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " đã đồng ý kết bạn", ""));
            } else {
                p.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " không đồng ý kết bạn với bạn", ""));
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    static void doParty(Map map, Char player, Message message) {
        try {
            byte value = message.dis.readByte();
            if (value == 0) {
                player.sendInviteParty2User(message);
                return;
            }

            if (value == 1) {
                player.okJoinParty(message);
                return;
            }

            if (value == 2) {
                player.cancelJoinParty(message);
                return;
            }

            if (value == 3) {
                System.out.println("MOI BAN VAO TIEC");
                if (player.idWedding == -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không thể mời", ""));
                    return;
                }

                Wedding wd = (Wedding) allWedding.get(player.idWedding);
                if (wd == null) {
                    player.idWedding = -1;
                    return;
                }

                if (wd != null) {
                    if (wd.idP1 != player.charDBID && wd.idP2 != player.charDBID) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không thể mời", ""));
                        return;
                    }

                    if (player.potions[92] <= 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã hết thiệp mời.", ""));
                        return;
                    }
                }

                short userId = message.dis.readShort();
                Char p = player.getCharFromNearChar(userId);
                if (p == null) {
                    return;
                }

                if (p.myCountry != player.myCountry) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ thể mới khách cũng lãnh thổ với bạn", ""));
                    return;
                }

                if (p.lvDetail.lv < 40) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người từ cấp độ 40 trở lên", ""));
                    return;
                }

                if (p.idWedding <= -1 && p.potions[92] <= 0) {
                    p.pInviteWedding = player.charname;
                    p.sendMessage(MessageCreator.createMsgPopUp(p.id, 7, "Bạn có đồng ý tham gia tiệc cưới của " + player.charname + " không?"));
                    return;
                }

                player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " đã nhận lời tham gia tiệc cưới của người khác.", ""));
                return;
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }

    static void doSendMessage(Map map, Char player, Message message) {
        try {
            if (player.map.mapIDLoadMap == idMapTown && getTown[player.inCountry] || map.mapIDLoadMap == 17) {
                return;
            }

            if (System.currentTimeMillis() - player.timeChatRieng < 0L) {
                Thread.sleep(5000L);
                player.timeChatRieng = System.currentTimeMillis() + 5000L;
            }

            if (Char.getDayTime(0L).compareTo(player.dayCamChat) < 0) {
                return;
            }

            String receiver = message.dis.readUTF().trim();
            String info = message.dis.readUTF().trim().replaceAll("\\s+", " ");
            if (BadWord.checkBadWord(info)) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Nội dung bạn trò chuyện không hợp lệ. Người chơi khác sẽ không thấy câu nói của bạn"));
                return;
            }

            info = replaceStringWithSpace(info);
            info = replaceExtendDomain(info);
            info = BadWord.checkBadWord2(info);
            if (info.length() > 60) {
                info = info.substring(0, 60);
            }

            if (receiver.indexOf("-") > -1) {
                receiver = receiver.substring(0, receiver.indexOf("-")).trim();
            }

            Char p = CharManager.instance.getCharByCharName(receiver);
            if (p != null) {
                if (p.allMsgChatRieng.size() < 10) {
                    InfoChatRieng cr = new InfoChatRieng();
                    cr.charChat = player.charname;
                    cr.infoChat = info;
                    p.allMsgChatRieng.add(cr);
                }
            } else {
                CharInboxMessage newMsg = new CharInboxMessage();
                newMsg.sender = player.charname;
                newMsg.info = info.replace("'", " ");
            }
        } catch (Exception var7) {
        }
    }

    static void doChatBinhThuong(Map map, Char player, Message m, String msgChat) throws IOException {
        if (BadWord.checkBadWord(msgChat)) {
            player.sendMessage(
                    MessageCreator.createMsgChat(player.id, "Nội dung bạn trò chuyện không hợp lệ. Người chơi khác sẽ không thấy câu nói của bạn")
            );
            return;
        }
        msgChat = checkBadWord(msgChat);
        msgChat = msgChat.replaceAll("\\s+", " ").trim();
        msgChat = replaceStringWithSpace(msgChat);
        msgChat = replaceExtendDomain(msgChat);
        if (System.currentTimeMillis() - player.timecamchat < 0L) {
            player.sendMessage(m);
            return;
        }
        player.timecamchat = 0L;
        long time = System.currentTimeMillis() - player.timeChat;
        if (time <= 30000L && player.nMsgChat >= 5) {
            return;
        }
        if (time > 30000L) {
            player.timeChat = System.currentTimeMillis();
            player.nMsgChat = 0;
        }
        player.nMsgChat++;
        if (player.map.mapIDLoadMap == idMapTown && getTown[player.inCountry] || map.mapIDLoadMap == 17) {
            player.nMsgChat = 0;
            player.sendMessage(m);
            return;
        }
        if (player.timeChat > 0L && System.currentTimeMillis() - player.timeChat <= 5000L) {
            if (player.nMsgChat >= 5) {
                player.timecamchat = System.currentTimeMillis() + 1800000L;
                player.nMsgChat = 0;
            }
        } else if (System.currentTimeMillis() - player.timeChat > 5000L) {
            player.timeChat = System.currentTimeMillis();
            player.nMsgChat = 0;
        }
        msgChat = BadWord.checkBadWord2(msgChat);
        if (player.lvDetail.lv >= 20 && msgChat.length() >= 2) {
            byte maxChar = 50;
            if (msgChat.length() > maxChar) {
                msgChat = msgChat.substring(0, maxChar);
            }
            m.dos.writeUTF(msgChat);
            player.sendToNearPlayer(m);
            player.sendMessage(m);
            m.cleanup();
        }
    }

}
