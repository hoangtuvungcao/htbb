/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real.cmd;

import io.Message;
import io.Session;
import java.io.IOException;
import real.Char;
import real.CharManager;

public class QuestHandler implements ICommandHandler {
    @Override
    public void process(Session session, Message message) throws IOException {
        byte rcv_finish = message.dis.readByte();
        short idQuest = message.dis.readShort();
        Char p = CharManager.instance.getByUserID(session.userID);
        if (p != null) {
            p.OnQuest(rcv_finish, idQuest, message.dis.readByte());
        }
    }
}
