/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real.cmd;

import io.Message;
import io.Session;
import java.io.DataInputStream;
import java.io.IOException;
import real.Char;
import real.CharManager;
import real.MessageCreator;

public class RequestCharInfoHandler implements ICommandHandler {
    @Override
    public void process(Session session, Message message) throws IOException {
        short id = 0;
        Char ch = null;
        DataInputStream dis = message.dis;
        id = (short)dis.readUnsignedShort();
        ch = CharManager.instance.getByCharID(id);
        if (ch != null) {
            if (session.userID != ch.id) {
                Char charGet = CharManager.instance.getByUserID(session.userID);
                if (charGet != null) {
                    try {
                        if (ch.posNPCInVilage != null) {
                            Char p = CharManager.instance.getCharByCharName(session.charname);
                            if (p != null) {
                                session.sendMessage(MessageCreator.createCharInfoNpc(ch, p.inCountry, p.mapID));
                            } else if (charGet.near(ch, charGet.rangeRemoveMonster[0]) && (charGet.map == ch.map || ch.isBot != -1)) {
                                session.sendMessage(MessageCreator.createCharInfo(ch));
                                if (!ch.isCharCopy()) {
                                    MessageCreator.createMsgCharMonster(ch, charGet);
                                } else {
                                    MessageCreator.createMsgCharMonsterCopy(ch, charGet);
                                }
                            }
                        } else if (charGet.near(ch, charGet.rangeRemoveMonster[0]) && charGet.map == ch.map || ch.isBot != -1) {
                            session.sendMessage(MessageCreator.createCharInfo(ch));
                            if (!ch.isCharCopy()) {
                                MessageCreator.createMsgCharMonster(charGet, ch);
                            } else {
                                MessageCreator.createMsgCharMonsterCopy(charGet, ch);
                            }

                            ch.sendEffToChar(charGet);
                        }

                        if (charGet.typeConfig < 3 && charGet.near(ch, charGet.rangeRemoveMonster[0]) && charGet.map == ch.map || ch.isBot != -1) {
                            session.sendMessage(MessageCreator.createCharWearingCharInfo(ch, charGet));
                        }
                    } catch (Exception var8) {
                        var8.printStackTrace();
                    }
                }
            }
        }
    }
}
