/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real.cmd;

import io.Message;
import io.Session;
import java.io.IOException;
import real.MessageCreator;
import real.RealController;

public class PartyHandler implements ICommandHandler {
    @Override
    public void process(Session session, Message message) throws IOException {
        int size = RealController.intance.idGen.getSize(5);
        if (session.idPrivateParty == -1 && size > 0) {
            int idP = RealController.intance.idGen.getID(5, "tao party");
            session.idPrivateParty = idP;
            Message m = new Message(48);
            m.dos.writeShort(idP);
            session.sendMessage(m);
            m.cleanup();
        } else {
            Message m = MessageCreator.createServerAlertMessage("Không thể tạo thêm party", "");
            session.sendMessage(m);
            m.cleanup();
        }
    }
}
