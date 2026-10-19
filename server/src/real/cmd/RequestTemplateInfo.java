/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real.cmd;

import data.DataGame;
import io.Message;
import io.Session;
import java.io.IOException;
import real.Map;
import real.MonsterTemplate;

public class RequestTemplateInfo implements ICommandHandler {
    @Override
    public void process(Session session, Message message) throws IOException {
        short type = (short)message.dis.readUnsignedByte();
        int idTemplate = message.dis.readShort();

        try {
            if (type == 0) {
                Message msg = new Message(100);
                msg.dos.writeByte(type);
                msg.dos.writeShort(idTemplate);
                msg.dos.writeByte(MonsterTemplate.info[idTemplate].length);
                msg.dos.write(MonsterTemplate.info[idTemplate]);
                MonsterTemplate mt = Map.monsterTemplates.get(idTemplate);
                msg.dos.writeUTF(mt.name);
                msg.dos.writeByte(mt.he);
                msg.dos.writeInt(mt.maxhp);
                msg.dos.writeByte(mt.palate);
                msg.dos.writeByte(mt.spalate);
                msg.dos.writeByte(mt.isNewMonster);
                if (mt.isNewMonster == 1) {
                    msg.dos.writeShort(DataGame.dataBoss[mt.spalate].length);
                    msg.dos.write(DataGame.dataBoss[mt.spalate]);
                }

                session.sendMessage(msg);
            }
        } catch (Exception var7) {
            System.err.println("THONG TIN TEMPLATEINFO " + idTemplate);
            var7.printStackTrace();
        }
    }
}
