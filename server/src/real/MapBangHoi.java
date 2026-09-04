package real;


import data.CharClan;
import data.CharInboxMessage;
import data.Database;
import data.NewClan;
import io.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import real.cmd.CreateCharHandler;


/**
 * Xu ly bang hoi (clan): tao/giai tan bang, moi/duoi thanh vien, chat bang,
 * gop quy, chuc vu, slogan, icon, danh sach thanh vien.
 *
 * Tach nguyen van tu real/Map.java (976 dong). Deu la method PRIVATE cua Map
 * nen khong the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 *
 * KHONG nham voi real/MapClan.java - do la "class MapClan extends Map",
 * ban do rieng cua bang hoi, khac han file nay.
 */
// Thanh vien static cua Map ma nhom method nay dung khong dinh danh khi con
// nam trong Map (giu nguyen than method, khong sua tay 976 dong).
import static real.Map.getClaninfoByID;
import static real.Map.giveCardFail;
import static real.Map.hashCodeValue;
import static real.Map.idIconClan;
import static real.Map.idMapDautruong;
import static real.Map.isUseNewClan;
import static real.Map.r;
import static real.Map.replaceExtendDomain;
import static real.Map.replaceStringWithSpace;
import static real.Map.stopGiveMoney;

class MapBangHoi {
    static boolean doSetTitleClan(Map map, Char pMaster, Char player, boolean ordained) {
        try {
            NewClan clandata = NewClan.getClan(player.idClan);
            if (clandata.rankClan >= 3) {
                if (player.rankClan >= clandata.rankClan && !ordained) {
                    System.out.println("KHONG THE GIANG CHUC TU CHUC NHO XUONG CHUC LON HON");
                    return false;
                }

                NewClan.setRankClan(player.charname, clandata.rankClan, player.idClan);
                clandata.downRankMem(player.charname, clandata.rankClan);
                clandata.rankClan = 3;
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendMessage(MessageCreator.createCharInfo(player));
                return true;
            }

            if (clandata.rankClan == 0) {
                System.out.println("KHONG THE PHONG BANG CHU");
                return false;
            }

            if (player.rankClan <= clandata.rankClan && ordained) {
                System.out.println("KHONG THE PHONG CHUC TU CHUC LON XUONG CHUC NHO HON");
                return false;
            }

            if (player.rankClan >= clandata.rankClan && !ordained) {
                System.out.println("KHONG THE GIANG CHUC TU CHUC NHO XUONG CHUC LON HON");
                return false;
            }

            if (!ordained) {
                if (clandata.rankClan == 2 && NewClan.isEnoughtSubLeader(player.idClan, clandata.rankClan)) {
                    pMaster.sendMessage(MessageCreator.createServerAlertMessage("Bang chỉ có thể có tối đa 7 trưởng lão.", ""));
                    return false;
                }

                NewClan.setRankClan(player.charname, clandata.rankClan, player.idClan);
                clandata.downRankMem(player.charname, clandata.rankClan);
                clandata.rankClan = 3;
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendMessage(MessageCreator.createCharInfo(player));
                return true;
            }

            if (!NewClan.isEnoughtSubLeader(player.idClan, clandata.rankClan)) {
                NewClan.setRankClan(player.charname, clandata.rankClan, player.idClan);
                clandata.setRankMem(player.charname, clandata.rankClan);
                clandata.rankClan = 3;
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendMessage(MessageCreator.createCharInfo(player));
                MessageCreator.createMsgCharMonster(player, player);
                return true;
            }

            if (clandata.rankClan == 1) {
                pMaster.sendMessage(MessageCreator.createServerAlertMessage("Bang chỉ có thể có tối đa 2 phó bang.", ""));
            } else if (clandata.rankClan == 2) {
                pMaster.sendMessage(MessageCreator.createServerAlertMessage("Bang chỉ có thể có tối đa 7 trưởng lão.", ""));
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return false;
    }

    static void doQuestClan(Map map, Char player, Message message) {
    }

    static void doInfoClanQuest(Map map, Char player, Message message) {
        try {
            short type = message.dis.readByte();
            switch (type) {
                case 0:
                    player.receiveClanQuest();
                case 1:
                case 2:
                case 3:
                case 4:
            }
        } catch (Exception var4) {
        }
    }

    static void doSendImgIconClan(Map map, Char player, Message message) {
        try {
            int type = message.dis.readByte();
            int hascode = message.dis.readByte();
            if (type == 2) {
                map.doSendIconPotion(player, hascode, 2);
            } else if (type == 3) {
                map.doSendIconPotion(player, hascode, 3);
            } else if (type == 4) {
                map.doSendIconPotion(player, hascode, 4);
            } else if (hascode != hashCodeValue) {
                Message msg = new Message(-8);
                msg.dos.writeByte(type);
                msg.dos.writeByte(hashCodeValue);
                player.sendMessage(MessageCreator.createByteImgIconClan(msg));
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }
    }

    static int getIdOutIdClan(Map map) {
        return idIconClan.size() > 0 ? 0 : -1;
    }

    static boolean getIdOutIdClan(Map map, int id) {
        return idIconClan.size() > 0 ? idIconClan.remove(new Integer(id)) : false;
    }

    static void doRequestCreateClan(Map map, Char player) {
        int price = 100000000;

        try {
            if (idIconClan.size() <= 0) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại không còn icon bang hội nào. Vui lòng quay lại sau.", ""));
                return;
            }

            if (player.idClan != -1) {
                // != -1, khong phai > 0: bang id 0 la id hop le dau tien duoc phat
                // (idIconClan nap tu 0..MAX_CLAN-1), "> 0" cho thanh vien/bang chu cua
                // bang id 0 lap them mot bang thu hai ma khong roi bang cu.
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tạo bang khi đang ở trong bang khác", ""));
                return;
            }

            if (player.potions[31] == 0) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có kim bài", ""));
                return;
            }

            if (player.lvDetail.lv < 50) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 50 trở lên mới có thể lập bang", ""));
                return;
            }

            if (player.getxu() < (long) price) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có " + price + " xu mới có thể lập bang", ""));
                return;
            }

            if (getIdOutIdClan(map) == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể lập bang trong thời gian này.", ""));
                return;
            }

            Message msg = new Message(-9);
            msg.dos.writeShort(idIconClan.size());

            for (int i = 0; i < idIconClan.size(); i++) {
                msg.dos.writeShort((Integer) idIconClan.get(i));
            }

            String info = "Chi phí tạo bang là 100.000.000 xu và phải đạt 10 thành viên trong 3 ngày tính từ thời điểm tạo bang.";
            msg.dos.writeUTF(info);
            player.sendMessage(msg);
        } catch (Exception var5) {
        }
    }

    static void doSetSloganClan(Map map, Char player, Message msg) {
        try {
            NewClan clanInfo = getClaninfoByID(player.idClan);
            if (player.idClan == -1 || clanInfo == null || clanInfo != null && !player.charname.equals(clanInfo.master)) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bang chủ mới có thể đặt thông điệp bang", ""));
                return;
            }

            clanInfo.slogan = msg.dis.readUTF();
            clanInfo.updateNewClandata2DB();
        } catch (Exception var4) {
        }
    }

    static void doSendMessage2Clan(Map map, Char player, Message msg) {
        Message m = null;

        try {
            if (System.currentTimeMillis() - player.timeChatBang < 0L) {
                Thread.sleep(5000L);
                player.timeChatBang = System.currentTimeMillis() + 5000L;
            }

            if (player.idClan == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể gửi khi chưa có bang", ""));
                return;
            }

            byte type = msg.dis.readByte();
            switch (type) {
                case 0:
                    String st = msg.dis.readUTF().trim().replaceAll("\\s+", " ");
                    st = replaceStringWithSpace(st);
                    st = replaceExtendDomain(st);
                    if (BadWord.checkBadWord(st)) {
                        player.sendMessage(
                                MessageCreator.createMsgChat(player.id, "Nội dung bạn trò chuyện không hợp lệ. Người chơi khác sẽ không thấy câu nói của bạn")
                        );
                        return;
                    }

                    st = BadWord.checkBadWord2(st);
                    if (st.length() > 60) {
                        st = st.substring(0, 60);
                    }

                    Database.instance.addNewMsgClan(player, st);
                    break;
                case 1:
                    Vector<MsgClanInfo> vMsg = Database.instance.getMsgClan(player.idClan);
                    if (vMsg.size() == 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hòm thư rỗng", ""));
                        return;
                    }

                    m = new Message(-17);
                    m.dos.writeShort(vMsg.size());

                    for (int i = 0; i < vMsg.size(); i++) {
                        MsgClanInfo clan = (MsgClanInfo) vMsg.get(i);
                        m.dos.writeInt(clan.id);
                        m.dos.writeUTF(clan.sender);
                        m.dos.writeUTF(clan.info);
                    }

                    player.sendMessage(m);
                    break;
                case 2:
                    int id = msg.dis.readInt();
                    Database.instance.delMsgClan(id);
                    player.sendMessage(MessageCreator.createServerAlertMessage("Đã xoá", ""));
            }
        } catch (Exception var9) {
        }
    }

    static NewClan getClanInfo(Map map, int idClan) {
        return NewClan.getClan((short) idClan);
    }

    static void doGiveMoneyNewClan(Map map, Char player, Message message) {
        synchronized (map) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else {
            try {
                if (stopGiveMoney) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại chưa thể quyên góp cho bang.", ""));
                } else if (player.idClan == -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa gia nhập bang.", ""));
                } else {
                    NewClan clandata = NewClan.getClan(player.idClan);
                    if (clandata != null) {
                        int money = message.dis.readInt();
                        if (money > 1) {
                            if (player.giveMoneyClan <= 0) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể quyên góp xu vào bang 1 lần trong 1 ngày.", ""));
                            } else if (player.lvDetail.lv < 50) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 50 trở lên mới có thể quyên góp vào bang.", ""));
                            } else if (player.getxu() < (long) money) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                            } else {
                                player.giveMoneyClan = money;
                                player.sendMessage(
                                        MessageCreator.createMsgPopUp(114, 114, "Bạn có muốn dùng 200 điểm cống hiến để quyên góp nhanh " + money + " xu vào bang không?")
                                );
                            }
                        }
                    }
                }
            } catch (Exception var5) {
                var5.printStackTrace();
                System.out.println("LOI CHO TIEN VAO BANG " + var5.toString());
            }
        }
        }
    }

    static void doGiveMoneyClan(Map map, Char player, Message message) {
        synchronized (map) {
        if (isUseNewClan) {
            doGiveMoneyNewClan(map, player, message);
        }
        }
    }

    static void doChatNewClan(Map map, Char player, Message msg) {
        try {
            if (player.idClan == -1) {
                return;
            }

            String ms = msg.dis.readUTF().trim();
            if (ms.length() > 60) {
                ms = ms.substring(0, 60);
            }

            if (BadWord.checkBadWord(ms)) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Nội dung bạn trò chuyện không hợp lệ. Người chơi khác sẽ không thấy câu nói của bạn"));
                return;
            }

            ms = BadWord.checkBadWord2(ms);
            NewClan clanInfo = NewClan.getClan(player.idClan);
            Message m = new Message(-18);
            String title = "";
            if (player.rankClan < 3 && player.rankClan > -1) {
                String[] tempTitle = new String[]{"(BC)", "(PB)", "(TL)"};
                title = tempTitle[player.rankClan];
            }

            m.dos.writeUTF(title + player.charname + ": " + ms);

            for (CharClan c : clanInfo.member.values()) {
                String name = c.charname;
                if (name.indexOf("-") > -1) {
                    name = name.substring(0, name.indexOf("-"));
                }

                Char pp = CharManager.instance.getCharByCharName(name);
                if (pp != null) {
                    pp.sendMessage(m);
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    static void doChatClan(Map map, Char player, Message msg) {
        if (isUseNewClan) {
            doChatNewClan(map, player, msg);
        }
    }

    static void doDelNewClan(Map map, Char player) {
        try {
            NewClan clanInfo = NewClan.getClan(player.idClan);
            if (player.idClan == -1 || clanInfo == null || clanInfo != null && !clanInfo.master.toLowerCase().equals(player.charname)) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không phải bang chủ.", ""));
                return;
            }

            if (System.currentTimeMillis() - clanInfo.timeStartDel >= 259200000L || clanInfo.member.size() >= 10) {
                clanInfo.timeStartDel = System.currentTimeMillis();
            }

            clanInfo.isDel = !clanInfo.isDel;

            for (CharClan c : clanInfo.member.values()) {
                Char p = CharManager.instance.getCharByCharName(c.charname);
                if (p != null && p.charDBID != player.charDBID) {
                    p.sendMessage(
                            MessageCreator.createServerAlertMessage("Bang " + clanInfo.name + (!clanInfo.isDel ? " đã được khôi phục" : " sẽ bị giải tán sau 3 ngày"), "")
                    );
                } else if (p == null) {
                    CharInboxMessage newMsg = new CharInboxMessage();
                    newMsg.sender = player.charname;
                    newMsg.info = "Bang " + clanInfo.name + (!clanInfo.isDel ? " được khôi phục" : " sẽ bị giải tán sau 3 ngày");
                    newMsg.idMsg = Database.instance.addNewMessage(c.charname, newMsg);
                }
            }

            Database.instance
                    .saveLogClan(
                            player.charname,
                            !clanInfo.isDel ? "getback" : "wait",
                            player.charname
                            + (!clanInfo.isDel ? " khôi phục bang " : " giải tán bang ")
                            + clanInfo.name
                            + " | "
                            + clanInfo.id
                            + "|"
                            + clanInfo.getListMemberClan()
                    );
            if (clanInfo.isDel) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bang " + clanInfo.name + " sẽ bị giải tán sau " + clanInfo.timeDel() + " phút", ""));
            } else if (clanInfo.member.size() < 10) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bang " + clanInfo.name + " đã được phục hồi nhưng sẽ bị giải tán sau " + clanInfo.timeDel() + " phút do chưa đủ 10 thành viên.", ""
                        )
                );
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bang " + clanInfo.name + " đã được khôi phục", ""));
                clanInfo.timeStartDel = 0L;
            }

            Database.instance.updateCharNewClan(clanInfo);
        } catch (Exception var7) {
        }
    }

    static void doDelClan(Map map, Char player) {
        if (isUseNewClan) {
            doDelNewClan(map, player);
        }
    }

    static void doUserOutNewClan(Map map, Char player) {
        int[] priceOut = new int[]{0, 5000000, 2500000, 500000};
        if (player.myCountry == 2) {
            priceOut = new int[4];
        }

        try {
            if (player.idClan == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có bang", ""));
                return;
            }

            if (player.rankClan == 0) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bang chủ không thể rời khỏi bang.", ""));
                return;
            }

            NewClan clanInfo = NewClan.getClan(player.idClan);
            if (clanInfo == null) {
                return;
            }

            try {
                if (player.getxu() < (long) priceOut[player.rankClan]) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có " + priceOut[player.rankClan] + " xu mới có thể rời bang.", ""));
                    return;
                }

                player.subXu((long) priceOut[player.rankClan], false, "map 78");
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                clanInfo.addMoney2Clan((long) (priceOut[player.rankClan] * 90 / 100));
                player.skillClan = new short[20];
                player.sendMessage(MessageCreator.createSkillClan(player));
            } catch (Exception var11) {
            }

            player.idClan = -1;
            player.rankClan = 3;
            player.timeJoinClan = System.currentTimeMillis() + 86400000L;
            Database.instance.updateRankClan(player.charname, -1);
            Char p = CharManager.instance.getCharByCharName(clanInfo.master);
            clanInfo.removeMember(player.charname);
            if (p != null) {
                for (CharClan c : clanInfo.member.values()) {
                    Char pp = CharManager.instance.getCharByCharName(c.charname);
                    if (pp != null) {
                        pp.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " đã rời khỏi bang", ""));
                    } else {
                        CharInboxMessage newMsg = new CharInboxMessage();
                        newMsg.sender = player.charname;
                        newMsg.info = player.charname + " đã rời bang";
                        newMsg.idMsg = Database.instance.addNewMessage(c.charname, newMsg);
                    }
                }

                Database.instance.updateCharNewClan(clanInfo);
                if (clanInfo.member.size() < 10) {
                    if (clanInfo.timeStartDel == 0L) {
                        clanInfo.timeStartDel = System.currentTimeMillis();
                    }

                    Database.instance.updateTimeCheckDelClan(clanInfo.id, clanInfo.timeStartDel);
                    Message msg = new Message(27);
                    msg.dos.writeShort(p.id);
                    msg.dos.writeUTF("Bang sẽ bị xoá sau " + clanInfo.timeDel() + " phút vì không đủ 10 thành viên");
                    p.sendMessage(msg);
                }
            } else {
                Message m = new Message(27);
                m.dos.writeShort(player.id);
                m.dos.writeUTF(player.charname + " đã rời khỏi bang");

                for (CharClan cx : clanInfo.member.values()) {
                    Char pp = CharManager.instance.getCharByCharName(cx.charname);
                    if (pp != null) {
                        pp.sendMessage(m);
                    } else {
                        CharInboxMessage newMsg = new CharInboxMessage();
                        newMsg.sender = player.charname;
                        newMsg.info = player.charname + " đã rời bang";
                        newMsg.idMsg = Database.instance.addNewMessage(cx.charname, newMsg);
                    }
                }

                Database.instance.updateCharNewClan(clanInfo);
            }

            if (clanInfo.member.size() < 10) {
                if (clanInfo.timeStartDel == 0L) {
                    clanInfo.timeStartDel = System.currentTimeMillis();
                }

                Database.instance.updateTimeCheckDelClan(clanInfo.id, clanInfo.timeStartDel);
            }

            if (clanInfo != null) {
                Database.instance.saveLogClan(player.charname, "out", player.charname + " thoát khỏi bang " + clanInfo.name + " | " + clanInfo.id);
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                if (player.mapID == idMapDautruong) {
                    int[][] homeMaps = new int[][]{{70, 1701}, {0, 301}, {80, 1901}};
                    int[] mapstart = homeMaps[player.inCountry];
                    int homeMap = mapstart[r.nextInt(mapstart.length)];
                    map.move2Map(player, 28, 24, homeMap, player.inCountry);
                }
            }

            if (giveCardFail(player)) {
                Vector<Char> players = (Vector<Char>) map.allPlayers.get(player.inCountry);

                for (int i = 0; i < players.size(); i++) {
                    try {
                        ((Char) players.get(i)).sendMessage(MessageCreator.createServerAlertAutoOffMessage(player.charname + " giao thẻ thất bại"));
                        ((Char) players.get(i)).sendMessage(MessageCreator.createMsgStartGetTown(((Char) players.get(i)).inCountry));
                    } catch (Exception var10) {
                    }
                }
            }

            MessageCreator.createMsgCharMonster(player, player);
        } catch (Exception var12) {
            var12.printStackTrace();
        }
    }

    static void doUserOutClan(Map map, Char player) {
        if (isUseNewClan) {
            doUserOutNewClan(map, player);
        }
    }

    static void doKickMemberOutNewClan(Map map, Char player, Message msg) {
        try {
            String name = msg.dis.readUTF();
            if (name.indexOf("-") > -1) {
                name = name.substring(0, name.indexOf("-")).trim();
            }

            if (player.equals(name)) {
                return;
            }

            NewClan clanInfo = NewClan.getClan(player.idClan);
            if (player.idClan == -1 || clanInfo == null || player.rankClan > 1 || !player.isMasterClan() && !clanInfo.isPhoBang(player)) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có bang chủ hoặc phó bang mới có thể mời ra khỏi bang.", ""));
                return;
            }

            if (clanInfo.master.toLowerCase().equals(name.toLowerCase())) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời chủ bang ra khỏi bang", ""));
                return;
            }

            if (player.charname.equals(name)) {
                return;
            }

            if (!player.isMasterClan() && player.rankClan < 2) {
                CharClan ch = (CharClan) clanInfo.member.get(name);
                if (ch.rankClan < 3) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có bang chủ mới có thể mời phó bang hoặc trưởng lão ra khỏi bang", ""));
                    return;
                }
            }

            Char p = CharManager.instance.getCharByCharName(name);
            if (p != null && p.idClan != player.idClan) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời người của bang khác ra khỏi bang", ""));
                return;
            }

            if (p == null && Database.instance.getIDClanChar(name) != player.idClan) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời người của bang khác ra khỏi bang", ""));
                return;
            }

            boolean isexist = clanInfo.removeMember(name);
            if (!isexist) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời " + name + " ra khỏi bang", ""));
                return;
            }

            Database.instance.updateCharNewClan(clanInfo);
            player.sendMessage(MessageCreator.createServerAlertMessage(name + " đã bị đuổi khỏi bang", ""));
            if (p != null) {
                if (p.idClan == player.idClan) {
                    p.idClan = -1;
                    player.sendMessage(MessageCreator.createCharInfo(p));
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn bị đuổi khỏi bang", ""));
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                    p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                    Database.instance.updateCharIDClan(p.charname, -1);
                    p.skillClan = new short[20];
                    p.rankClan = 3;
                    p.timeJoinClan = System.currentTimeMillis() + 86400000L;
                    p.sendMessage(MessageCreator.createSkillClan(p));
                    MessageCreator.createMsgCharMonster(p, p);
                    MessageCreator.createMsgCharMonster(p, player);
                }
            } else {
                Database.instance.updateCharIDClan(name, -1);
                Database.instance.updateTimeJoinClan(name);
                CharInboxMessage newMsg = new CharInboxMessage();
                newMsg.sender = player.charname;
                newMsg.info = "Bạn đã bị đuổi khỏi bang";
                newMsg.idMsg = Database.instance.addNewMessage(name, newMsg);
            }

            Database.instance.saveLogClan(name, "kicked", player.charname + "_kick_" + name + " khỏi clan " + clanInfo.name + " | " + clanInfo.id);
            Database.instance.updateCharNewClan(clanInfo);
            if (clanInfo.member.size() < 10) {
                if (clanInfo.timeStartDel == 0L) {
                    clanInfo.timeStartDel = System.currentTimeMillis();
                }

                Database.instance.updateTimeCheckDelClan(clanInfo.id, clanInfo.timeStartDel);
                Message m = new Message(27);
                m.dos.writeShort(player.id);
                m.dos.writeUTF("Bang sẽ bị xoá sau " + clanInfo.timeDel() + " phút vì không đủ 10 thành viên");
                player.sendMessage(m);
            }

            if (giveCardFail(player)) {
                Vector<Char> players = (Vector<Char>) map.allPlayers.get(player.inCountry);

                for (int i = 0; i < players.size(); i++) {
                    try {
                        ((Char) players.get(i)).sendMessage(MessageCreator.createServerAlertAutoOffMessage(player.charname + " giao thẻ thất bại"));
                        ((Char) players.get(i)).sendMessage(MessageCreator.createMsgStartGetTown(((Char) players.get(i)).inCountry));
                    } catch (Exception var10) {
                    }
                }
            }
        } catch (Exception var11) {
        }
    }

    static void doKickMemberOutClan(Map map, Char player, Message msg) {
        if (isUseNewClan) {
            doKickMemberOutNewClan(map, player, msg);
        }
    }

    static void doSendInfoClan(Map map, Char player, Message msg) {
        try {
            short idClan = msg.dis.readShort();
            if (idClan >= 0) {
                NewClan clan = NewClan.getClan(idClan);
                if (clan == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy bang", ""));
                    return;
                }

                Message m = new Message(-12);
                m.dos.writeShort(clan.id);
                m.dos.writeUTF(clan.name);
                m.dos.writeUTF(clan.master);
                m.dos.writeByte(clan.level);
                m.dos.writeShort(clan.getAllMember().size());
                m.dos.writeLong(clan.money);
                m.dos.writeLong(clan.pDevote);
                m.dos.writeLong(clan.xp);
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
                m.dos.writeUTF(dayFormat.format(clan.date.getTime()));
                m.dos.writeUTF(clan.slogan);
                boolean isdel = clan.isDel;
                if (clan.member.size() < 10) {
                    isdel = true;
                }

                m.dos.writeBoolean(isdel);
                m.dos.writeByte(-1);
                if (isdel) {
                    m.dos.writeUTF("Bang còn " + clan.timeDel() + " phút nữa sẽ giải tán");
                }

                player.sendMessage(m);
            }
        } catch (Exception var8) {
        }
    }

    static void doSendListMemberClan(Map map, Char player, Message msg) {
        try {
            int page = msg.dis.readByte();
            short idClan = msg.dis.readShort();
            if (idClan >= 0) {
                NewClan clan = Database.instance.getInfoClan(idClan);
                if (clan == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không tồn tại bang", ""));
                    return;
                }

                player.sendMessage(MessageCreator.createMsgMemberClan(clan, page));
            }
        } catch (Exception var6) {
        }
    }

    static void doInviteCharJoinClan(Map map, Char player, Message message) {
        try {
            if (player.myCountry == -1) {
                return;
            }

            int type = message.dis.readByte();
            Char p = null;
            if (type == 0) {
                NewClan clan = NewClan.getClan(player.idClan);
                if (clan == null) {
                    return;
                }

                if (player.idClan != -1 && player.rankClan <= 2) {
                    short idChar = message.dis.readShort();
                    p = CharManager.instance.getByCharID(idChar);
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tìm thấy người bạn mời", ""));
                        return;
                    }

                    if (p.myCountry == -1) {
                        return;
                    }

                    if (player.myCountry != p.myCountry) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người trong cùng một làng", ""));
                        return;
                    }

                    if (p.map.isMapLoiDai()) {
                        return;
                    }

                    if (p.rcvInvite && p.rcvInviteVip) {
                        if (p.lvDetail.lv < 50) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Cấp độ của thành viên phải từ 50 trở lên.", ""));
                            return;
                        }

                        if (System.currentTimeMillis() - p.timeJoinClan < 0L) {
                            long time = (p.timeJoinClan - System.currentTimeMillis()) / 1000L;
                            boolean isPhut = false;
                            if (time > 60L) {
                                time /= 60L;
                                isPhut = true;
                            }

                            player.sendMessage(
                                    MessageCreator.createServerAlertMessage(p.charname + " chỉ có thể tham gia vào bang sau " + time + " " + (isPhut ? "phút" : "giây"), "")
                            );
                            return;
                        }

                        if (p.idClan != -1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người chưa gia nhập bang hội.", ""));
                            return;
                        }

                        if (clan.isDel) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mời khi đang trong thời gian chờ giải tán", ""));
                            return;
                        }

                        if (clan.isFullMem()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bang đã có quá đông người", ""));
                            return;
                        }

                        // Ghi lai loi moi phia server: type==1 (chap nhan/tu choi) o duoi phai
                        // doi chieu voi dung dong nay, khong duoc tin idCharx client tu gui.
                        p.idClanVuaDuocMoi = clan.id;
                        p.timeHetHanMoiClan = System.currentTimeMillis() + 120000L;
                        Message m = new Message(-11);
                        m.dos.writeByte(type);
                        m.dos.writeShort(player.id);
                        m.dos.writeUTF(player.charname + " muốn mời bạn tham gia bang " + clan.name + ". Bạn có tham gia không? ");
                        p.sendMessage(m);
                        return;
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại người chơi không chấp nhận bất kỳ lời mời nào.", ""));
                    return;
                }

                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có bang chủ, phó bang, trưởng lão mới được mời.", ""));
                return;
            } else if (type == 1) {
                short idCharx = message.dis.readShort();
                boolean ok = message.dis.readBoolean();
                p = CharManager.instance.getByCharID(idCharx);
                if (p == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi khi xảy ra", ""));
                    return;
                }

                NewClan clanx = NewClan.getClan(p.idClan);
                if (clanx == null) {
                    return;
                }

                // "player" (nguoi goi goi -11 type=1 nay) phai la nguoi VUA DUOC MOI vao DUNG
                // bang cua p, va loi moi con hieu luc. Truoc day khong co dieu kien nao o day:
                // client tu gui idCharx tro toi BAT KY thanh vien nao cua bang muon vao la du,
                // bo qua het quyen si quan/dieu kien cua nhanh type==0 phia tren.
                if (player.idClanVuaDuocMoi != clanx.id || System.currentTimeMillis() > player.timeHetHanMoiClan) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Lời mời đã hết hạn hoặc không hợp lệ", ""));
                    return;
                }
                player.idClanVuaDuocMoi = -1;
                if (clanx.isDel) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tham gia khi bang đang trong thời gian chờ giải tán", ""));
                    return;
                }

                if (player.idClan != -1) {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người chưa gia nhập bang hội.", ""));
                    return;
                }

                if (player.myCountry != p.myCountry) {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người trong cùng một làng", ""));
                    return;
                }

                if (player.lvDetail.lv < 50) {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Cấp độ của thành viên phải từ 50 trở lên.", ""));
                    return;
                }

                if (System.currentTimeMillis() - player.timeJoinClan < 0L) {
                    long time = (player.timeJoinClan - System.currentTimeMillis()) / 1000L;
                    if (time > 1000L) {
                        time /= 60L;
                    }

                    p.sendMessage(
                            MessageCreator.createServerAlertMessage(
                                    player.charname + " chỉ có thể tham gia vào bang sau " + time + " " + (time > 1000L ? "phút" : "giây"), ""
                            )
                    );
                    return;
                }

                if (clanx.isFullMem()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bang đã có quá đông người", ""));
                    return;
                }

                Message m = new Message(-11);
                m.dos.writeByte(type);
                m.dos.writeUTF(player.charname + (ok ? " đã tham gia bang " : " không đồng ý tham gia bang ") + clanx.name);
                m.dos.writeBoolean(ok);
                player.rankClan = 3;

                try {
                    if (player.idClan > -1) {
                        NewClan cl = NewClan.getClan(player.idClan);
                        cl.downRankMem(player.charname, 3);
                        cl.removeMember(player.charname);
                    }
                } catch (Exception var12) {
                }

                if (ok) {
                    Database.instance.updateCharIDClan(player.charname, clanx.id);
                    m.dos.writeShort(player.id);
                    m.dos.writeShort(clanx.id);
                    player.idClan = clanx.id;
                    clanx.addMember(player);
                    player.timeInClan = System.currentTimeMillis() + (long) (clanx.getTimePhatLuong() * 60 * 60000);
                    if (clanx.getAllMember().size() > 10) {
                        clanx.timeStartDel = 0L;
                    }

                    Database.instance.updateCharNewClan(clanx);
                    Database.instance.saveLogClan(player.charname, "join", player.charname + " đồng ý tham gia bang " + clanx.name + " | " + clanx.id);
                }

                player.rankClan = 3;

                try {
                    NewClan.setRankClan(player.charname.toLowerCase(), 3, player.idClan);
                    clanx.downRankMem(player.charname, 3);
                } catch (Exception var11) {
                }

                p.sendMessage(m);
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                p.sendMessage(MessageCreator.createCharInfo(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                MessageCreator.createMsgCharMonster(player, player);
                MessageCreator.createMsgCharMonster(player, p);
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        }
    }

    static void doInviteChar2Clan(Map map, Char player, Message message) {
        if (isUseNewClan) {
            doInviteCharJoinClan(map, player, message);
        }
    }

    static void doCreateNewClan(Map map, Char player, Message message) {
        int price = 100000000;

        try {
            if (player.idClan != -1) {
                // != -1, khong phai > 0: bang id 0 la id hop le dau tien duoc phat
                // (idIconClan nap tu 0..MAX_CLAN-1), "> 0" cho thanh vien/bang chu cua
                // bang id 0 lap them mot bang thu hai ma khong roi bang cu.
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tạo bang khi đang ở trong bang khác", ""));
                return;
            }

            if (player.potions[31] == 0) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có kim bài", ""));
                return;
            }

            if (player.lvDetail.lv < 50) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 50 trở lên mới có thể lập bang", ""));
                return;
            }

            if (player.getxu() < (long) price) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có " + price + " xu mới có thể lập bang", ""));
                return;
            }

            short cID = message.dis.readShort();
            String clanName = message.dis.readUTF();
            if (!CreateCharHandler.checkInfo(clanName)) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Tên bang không hợp lệ.", ""));
                return;
            }

            if (!Database.instance.checkClanName(clanName)) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Tên bang đã tồn tại. Xin chọn tên bang khác", ""));
                Message mg = new Message(-9);
                mg.dos.writeShort(idIconClan.size());

                for (int i = 0; i < idIconClan.size(); i++) {
                    mg.dos.writeShort((Integer) idIconClan.get(i));
                }

                mg.dos.writeUTF("");
                player.sendMessage(mg);
                return;
            }

            boolean id = getIdOutIdClan(map, cID);
            if (!id) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Logo này đã được người khác chọn. Xin chọn lại logo khác", ""));
                Message msg = new Message(-9);
                msg.dos.writeShort(idIconClan.size());

                for (int i = 0; i < idIconClan.size(); i++) {
                    msg.dos.writeShort((Integer) idIconClan.get(i));
                }

                msg.dos.writeUTF("");
                player.sendMessage(msg);
                return;
            }

            // Kiem-va-tru NGUYEN TU truoc khi ghi CSDL, khong tin vao lan kiem getxu() o dau
            // ham: gói -10 KHONG nam trong privateCmd nen chay tren LUONG BAN DO, trong khi
            // hau het duong tieu xu khac chay tren LUONG PHIEN - giua lan kiem do va toi day
            // da di qua hai luot round-trip CSDL (checkClanName, sap toi la addNewClan), du
            // thoi gian de luong phien tieu het xu. Chot cu "getxu() > 0" con sai hon nua:
            // CHI CAN CON 1 XU la qua, subXu(price) roi chan ve 0 - lap bang gan nhu mien phi.
            if (!player.truXuNeuDu((long) price, true, "map 79")) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có " + price + " xu mới có thể lập bang", ""));
                return;
            }

            NewClan clan = new NewClan();
            clan.id = cID;
            clan.master = player.charname;
            clan.timeStartDel = System.currentTimeMillis();
            clan.name = clanName;
            clan.addMember(player);
            clan.country = player.myCountry;
            clan.date = new Date(System.currentTimeMillis());
            if (Database.instance.addNewClan(clan)) {
                Message msg = new Message(-10);
                msg.dos.writeShort(cID);
                msg.dos.writeUTF("Bang " + clanName.toUpperCase() + " được tạo thành công.");
                player.sendMessage(msg);
                player.idClan = cID;
                idIconClan.remove(new Integer(cID));

                if (player.potions[31] > 0) {
                    player.potions[31]--;
                }

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveLogClan(player.charname, "create", "Tạo clan: " + clanName + " với logo " + cID);
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                NewClan.addClan(clan);
                MessageCreator.createMsgCharMonster(player, player);
                Database.instance.saveLogThongKeChiTieu("Tao Bang", 0, 1, (long) price, "xu");
                player.rankClan = 0;
                Database.instance.updateRankClan(player.charname, 0);
            } else {
                // addNewClan that bai SAU khi da tru xu (o tren) - hoan lai, khong de nguoi
                // choi mat xu ma khong nhan duoc gi.
                player.addXu((long) price, "map 79 hoan lai");
                player.sendMessage(MessageCreator.createServerAlertMessage("Tạo bang thất bại. Xin thử lại", ""));
                if (clan.id != -1) {
                    idIconClan.remove(new Integer(cID));
                }

                Message mg = new Message(-9);
                mg.dos.writeShort(idIconClan.size());

                for (int i = 0; i < idIconClan.size(); i++) {
                    mg.dos.writeShort((Integer) idIconClan.get(i));
                }

                mg.dos.writeUTF("");
                player.sendMessage(mg);
            }
        } catch (Exception var10) {
        }
    }

    static void doCreateClan(Map map, Char player, Message message) {
        if (isUseNewClan) {
            doCreateNewClan(map, player, message);
        }
    }

}
