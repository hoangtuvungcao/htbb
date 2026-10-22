package real;


import data.CharInboxMessage;
import data.Database;
import io.Message;
import java.io.IOException;

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

    /**
     * Chong spam chat (com.htbb.chongspam.ChongSpam) - kiem tren noi dung GOC truoc moi bo loc
     * tu xau (bo loc cu xoa dau cham nen "abc.com" thanh "abccom" va lot qua). Admin mien.
     *
     * @return true = duoc phat; false = da chan (va da bao nguoi gui, toi da 1 lan / 5 giay).
     */
    static boolean kiemSpamChat(Char player, com.htbb.chongspam.ChongSpam.Kenh kenh, String noiDungGoc) {
        // 0.9.5: tin rỗng / chỉ khoảng trắng / ký tự vô hình: bỏ im lặng (không phát, không tính spam, không báo -
        // client 0.9.5 không gửi tin rỗng, client cũ không cần thông báo)
        if (com.htbb.chongspam.ChongSpam.rong(noiDungGoc)) {
            return false;
        }
        if (player == null || player.isAdmin) {
            return true;
        }
        final long bayGio = System.currentTimeMillis();
        final com.htbb.chongspam.ChongSpam.TrangThai t = com.htbb.chongspam.ChongSpam.cua(Integer.valueOf(player.charDBID));
        final long camTruoc = t.conCam(bayGio);
        final com.htbb.chongspam.ChongSpam.KetQua kq = com.htbb.chongspam.ChongSpam.kiemChat(t, kenh, noiDungGoc, bayGio);
        if (kq == com.htbb.chongspam.ChongSpam.KetQua.CHO_PHEP) {
            return true;
        }
        server.ChiSoMayChu.CHAT_CHAN.incrementAndGet();
        if (camTruoc == 0L && t.conCam(bayGio) > 0L) {
            com.htbb.log.Nhat.kiemToan("CHONG_SPAM", "tu dong cam chat " + com.htbb.log.Nhat.sach(player.charname)
                    + " " + (t.conCam(bayGio) / 1000L) + "s (bac " + t.bac() + ", kenh " + kenh + ", ly do " + kq + "): "
                    + com.htbb.log.Nhat.sach(noiDungGoc));
        } else if (kq == com.htbb.chongspam.ChongSpam.KetQua.QUANG_CAO) {
            com.htbb.log.Nhat.thongTin("CHONG_SPAM", "chan quang cao " + com.htbb.log.Nhat.sach(player.charname)
                    + " (kenh " + kenh + "): " + com.htbb.log.Nhat.sach(noiDungGoc));
        }
        if (com.htbb.chongspam.ChongSpam.nenBao(t, bayGio)) {
            final String lyDo = com.htbb.chongspam.ChongSpam.lyDo(kq, t, bayGio);
            if (lyDo != null) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, lyDo));
            }
        }
        return false;
    }

    /** Chong spam thao tac (moi doi / ket ban / moi giao dich ...). false = bo thao tac nay. */
    static boolean kiemSpamHanhDong(Char player, com.htbb.chongspam.ChongSpam.HanhDong hd) {
        if (player == null || player.isAdmin) {
            return true;
        }
        final long bayGio = System.currentTimeMillis();
        final com.htbb.chongspam.ChongSpam.TrangThai t = com.htbb.chongspam.ChongSpam.cua(Integer.valueOf(player.charDBID));
        if (com.htbb.chongspam.ChongSpam.choHanhDong(t, hd, bayGio)) {
            return true;
        }
        server.ChiSoMayChu.THAO_TAC_CHAN.incrementAndGet();
        if (com.htbb.chongspam.ChongSpam.nenBao(t, bayGio)) {
            player.sendMessage(MessageCreator.createMsgChat(player.id, "Thao tác quá nhanh, chờ một chút."));
        }
        return false;
    }

    static void doSendChallenParty(Map map, Char player, String text, int idMenu) {
        if (idMenu == 0) {
            int money;
            try {
                money = Integer.parseInt(text.trim().replace(".", "").replace(",", ""));
            } catch (Exception exNum) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thách đấu với số tiền này", ""));
                return;
            }

            if (player.partyID == -1) {
                return;
            }
            // 19/10/2026: muc cuoc theo cap thap nhat trong nhom (real.ThachDauCuoc), truoc 0 - 10.000.000 xu.
            final java.util.List<Char> nhom = new java.util.ArrayList<Char>();
            for (final Object o : player.party.userParty) {
                nhom.add((Char) o);
            }
            final String loiMuc = ThachDauCuoc.kiemCuoc(money, nhom.toArray(new Char[0]));
            if (loiMuc != null) {
                player.sendMessage(MessageCreator.createServerAlertMessage(loiMuc, ""));
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

            final java.util.List<Char> moiNguoi = new java.util.ArrayList<Char>();
            for (final Object o : player.party.userParty) {
                moiNguoi.add((Char) o);
            }
            for (final Object o : p.party.userParty) {
                moiNguoi.add((Char) o);
            }
            final String loiMucHai = ThachDauCuoc.kiemCuoc(player.moneyThachdau, moiNguoi.toArray(new Char[0]));
            if (loiMucHai != null) {
                player.sendMessage(MessageCreator.createServerAlertMessage(loiMucHai, ""));
                return;
            }

            // chong clone: moi thanh vien hai nhom deu phai du cap / tuoi (thua co y = chuyen xu).
            for (final Object o : player.party.userParty) {
                if (com.htbb.pokemon.ChongClone.chanCaHai((Char) o, (Char) o, player, com.htbb.pokemon.ChongClone.KIEU_THACH_DAU)) {
                    return;
                }
            }
            for (final Object o : p.party.userParty) {
                if (com.htbb.pokemon.ChongClone.chanCaHai((Char) o, (Char) o, player, com.htbb.pokemon.ChongClone.KIEU_THACH_DAU)) {
                    return;
                }
            }

            p.sendMessage(
                    MessageCreator.createMsgPopUp(
                            p.id, 11, player.charname + " gửi lời thách đấu tới nhóm của bạn với số tiền cược là " + player.moneyThachdau
                                    + " xu (đội trưởng hai bên đặt; bên thắng nhận gấp đôi trừ phí " + ThachDauCuoc.cauHinh().phiPhanTram
                                    + "%, hoà thì hoàn lại). Bạn có đồng ý không?"
                    )
            );
            p.nameCompetitor = player.charname;
        }
    }

    static void doAddFriend(Map map, Char player, Message msg) {
        try {
            if (player.myCountry == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạ chưa chọn vùng", ""));
                return;
            }

            int charid = msg.dis.readShort();
            int ok = msg.dis.readByte();
            Char p = CharManager.instance.getByCharID((short) charid);
            if (p == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Cậu bạn mời hiện đang offline", ""));
                return;
            }

            if (p.myCountry == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người đã chọn vùng", ""));
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
                if (!kiemSpamHanhDong(player, com.htbb.chongspam.ChongSpam.HanhDong.KET_BAN)) {
                    return;
                }
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
                if (!kiemSpamHanhDong(player, com.htbb.chongspam.ChongSpam.HanhDong.MOI_DOI)) {
                    return;
                }
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
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã hết Thiệp mời kết đôi.", ""));
                        return;
                    }
                }

                short userId = message.dis.readShort();
                Char p = player.getCharFromNearChar(userId);
                if (p == null) {
                    return;
                }

                if (p.myCountry != player.myCountry) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ mời được khách cùng vùng với bạn", ""));
                    return;
                }

                if (p.lvDetail.lv < 40) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mời người từ cấp độ 40 trở lên", ""));
                    return;
                }

                if (p.idWedding <= -1 && p.potions[92] <= 0) {
                    p.pInviteWedding = player.charname;
                    p.sendMessage(MessageCreator.createMsgPopUp(p.id, 7, "Bạn có đồng ý dự Lễ kết đôi của " + player.charname + " không?"));
                    return;
                }

                player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " đã nhận lời dự một Lễ kết đôi khác.", ""));
                return;
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }

    static void doSendMessage(Map map, Char player, Message message) {
        if (com.htbb.pokemon.ThanhVien.chanVaBao(player, "gui_thu")) { // thanh vien: nhan tin rieng
            return;
        }
        try {
            if (player.map.mapIDLoadMap == idMapTown && getTown[player.inCountry] || map.mapIDLoadMap == 17) {
                return;
            }

            // Bo Thread.sleep(5000) cu: no ngu NGAY TREN luong doc goi tin cua ket noi (ca ket noi
            // dung hinh 5 giay). Gio chan bang ChongSpam (kenh RIENG) va bo tin, khong ngu.
            if (Char.getDayTime(0L).compareTo(player.dayCamChat) < 0) {
                return;
            }

            String receiver = message.dis.readUTF().trim();
            String info = message.dis.readUTF().trim().replaceAll("\\s+", " ");
            if (!kiemSpamChat(player, com.htbb.chongspam.ChongSpam.Kenh.RIENG, info)) {
                return;
            }
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
        if (!kiemSpamChat(player, com.htbb.chongspam.ChongSpam.Kenh.GAN, msgChat)) {
            return;
        }
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
        // Cam chat cu (timecamchat) van ton trong neu noi khac dat. Nhip chat + cam TU DONG cu
        // (5 tin trong 5 giay -> cam THANG 30 phut, khong canh bao) da thay bang ChongSpam o tren:
        // nhip theo cua so truot, chan trung lap / quang cao, cam tang dan 1 -> 5 -> 30 -> 120 phut.
        if (System.currentTimeMillis() - player.timecamchat < 0L) {
            player.sendMessage(m);
            return;
        }
        if (player.map.mapIDLoadMap == idMapTown && getTown[player.inCountry] || map.mapIDLoadMap == 17) {
            player.sendMessage(m);
            return;
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
