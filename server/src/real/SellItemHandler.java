/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import io.Message;
import io.Session;
import java.io.IOException;
import real.cmd.ICommandHandler;

public class SellItemHandler implements ICommandHandler {
    @Override
    public void process(Session session, Message message) throws IOException {
        try {
            byte type = message.dis.readByte();
            Char p = CharManager.instance.getByUserID(session.userID);
            if (p == null) {
                return;
            }

            if (type == 0) {
                ShopVip.doSearchItem(p, message);
            } else if (type != 1) {
            }
        } catch (Exception var5) {
        }
    }
}
