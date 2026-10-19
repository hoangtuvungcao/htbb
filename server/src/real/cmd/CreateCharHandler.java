/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real.cmd;

import data.Database;
import io.Message;
import io.Session;
import java.io.DataInputStream;
import java.io.IOException;
import real.Map;
import real.MessageCreator;
import server.InfoClientConnect;
import server.TeamServer;

public class CreateCharHandler implements ICommandHandler {
    @Override
    public void process(Session session, Message message) throws IOException {
        DataInputStream dis = message.dis;
        String charName = dis.readUTF();
        int classID = dis.readByte();
        int headStyle = dis.readByte();
        int gender = -1;
        int nation = 0;

        try {
            gender = dis.readByte();
            nation = dis.readByte();
        } catch (Exception var13) {
        }

        if (TeamServer.isServerLienDau()) {
            session.sendMessage(MessageCreator.createServerAlertMessage("Không thể tạo nhân vật trong máy chủ liên đấu", ""));
        } else if (LoginHandler.stopLogin) {
            Message m = new Message(2);
            m.dos.writeUTF("1Máy chủ đang đang quá tải, vui lòng chọn máy chủ khác.");
            session.sendMessage(m);
            m.cleanup();
            message.cleanup();
        } else if (session.userID != -1) {
            InfoClientConnect info = TeamServer.ALL_IPCONNECT.get(session.ip);
            if (info != null) {
                info.countTaoChar();
                if (info.maxTaoChar >= 10) {
                    System.out.println("IP BLOCK taochar " + session.ip);

                    try {
                        Thread.sleep(3000L);
                    } catch (Exception var12) {
                    }

                    return;
                }
            }

            if (nation == 2) {
                session.sendMessage(MessageCreator.createServerAlertMessage("Lãnh thổ Huyền Vũ đã đóng. Vui lòng chọn lãnh thổ Thanh Long hoặc Hắc Hổ.", ""));
            } else {
                if (!checkInfo(charName) || Database.haveBadWord(charName) || charName.length() > 10 || charName.length() < 4) {
                    try {
                        session.sendMessage(MessageCreator.createServerAlertMessage("Tên nhân vật không hợp lệ", ""));
                        return;
                    } catch (Exception var14) {
                    }
                }

                if (Map.checkFullNation(nation, 1, 1)) {
                    session.sendMessage(MessageCreator.createServerAlertMessage(Map.nameCountry[nation] + " lãnh đã quá đông. Vui lòng chọn lãnh thổ khác.", ""));
                } else {
                    int chID = Database.instance.addChar(session, charName, headStyle, classID, gender, nation);
                    if (chID == -1) {
                        Message m = new Message(14);
                        session.sendMessage(m);
                        m.cleanup();
                    } else {
                        Message m = MessageCreator.createCharListMessage(session.userID, session);
                        session.sendMessage(m);
                        m.cleanup();
                    }
                }
            }
        }
    }

    public static boolean checkInfo(String userName) {
        String nick = userName;
        if (userName.equals("")) {
            return false;
        } else if (userName.length() >= 4 && userName.length() <= 15) {
            for (int i = 0; i < nick.length(); i++) {
                char ch = nick.charAt(i);
                if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean checkInfoLogin(String userName) {
        String nick = userName;
        if (userName.equals("")) {
            return false;
        } else {
            for (int i = 0; i < nick.length(); i++) {
                char ch = nick.charAt(i);
                if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                    return false;
                }
            }

            return true;
        }
    }
}
