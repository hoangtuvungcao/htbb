import data.GemItem;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Method;
import real.Char;
import real.Item;
import real.ItemTemplates;
import real.Map;

/** Reproduce drilling/inlay with actual server handlers, without a database/session. */
public final class KiemDucLoKham {
    private static Method add;
    private static Method finish;

    private static final class Player extends Char {
        long money = 1000000;
        String result = "";
        long syncedMoney = -1;
        Player() {
            super(null);
            map = new Map();
            map.isMapOffline = true;
        }
        @Override public long getxu() { return money; }
        @Override public void subXu(long amount, boolean count, String source) { money -= amount; }
        @Override public void sendMessage(Message message) {
            if (message.command == 16) {
                try {
                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(message.toByteArray()));
                    in.readByte();
                    in.readShort();
                    if (in.readByte() == 0) syncedMoney = in.readLong();
                } catch (Exception e) { throw new AssertionError(e); }
            }
            if (message.command == -35) {
                try {
                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(message.toByteArray()));
                    in.readByte();
                    in.readShort();
                    result = in.readUTF();
                } catch (Exception e) { throw new AssertionError(e); }
            }
        }
    }

    private static final class Equipment extends Item {
        private final ItemTemplates template = new ItemTemplates();
        Equipment() {
            template.type = 9;
            template.name = "Kiem kham";
            template.atb = new short[33];
            id = 123;
        }
        @Override public ItemTemplates getTemplate() { return template; }
    }

    public static void main(String[] args) {
        chay();
        System.exit(Kiem.tongKet());
    }

    public static void chay() {
        Kiem.nhom("Duc lo va kham - dung tui, dung cap, khong mat xu khi loi");
        try {
            Map.loadGemTemplate();
            Class<?> handler = Class.forName("real.MapEpNgoc");
            add = handler.getDeclaredMethod("doAddGemItemImbue", Map.class, Char.class, short.class, Message.class);
            finish = handler.getDeclaredMethod("doFinishKhamItem", Map.class, Char.class, int.class);
            add.setAccessible(true);
            finish.setAccessible(true);
            drillAll(false);
            drillAll(true);
            inlay();
            invalid();
            oneMaterial();
            enhancement();
        } catch (Throwable e) {
            e.printStackTrace();
            Kiem.dung("handler runs without unexpected exception: " + e, false);
        }
    }

    private static Equipment equip(Player p) {
        Equipment item = new Equipment();
        p.iItems.add(item);
        p.itemImbue = item.id;
        return item;
    }

    private static GemItem gem(Player p, int id, int normal, int locked) {
        GemItem gem = new GemItem(id);
        gem.realId = (short) (1000 + id);
        p.gemItem.add(gem);
        p.listGemitem[id] = normal;
        p.listGemitemLock[id] = locked;
        return gem;
    }

    private static void put(Player p, GemItem g) throws Exception {
        add.invoke(null, p.map, p, g.realId, new Message(76, new byte[]{0}));
    }

    private static void done(Player p) throws Exception {
        finish.invoke(null, p.map, p, 1);
    }

    private static void drillAll(boolean locked) throws Exception {
        Player p = new Player();
        Equipment item = equip(p);
        p.typeGemKham = locked ? 1 : 0;
        p.typeGemImbue = locked ? 0 : 1; // Opposite enhancement bank must not reject drilling.
        for (int hole = 1; hole <= 6; hole++) {
            int id = 59 + hole;
            GemItem g = gem(p, id, locked ? 0 : 1, locked ? 1 : 0);
            put(p, g);
            Kiem.dung("select drill " + hole + ", locked=" + locked, p.gemItemImbue.contains(g));
            done(p);
            Kiem.bang("hole " + hole + ", locked=" + locked, hole, (int) item.nSocAdd);
            Kiem.bang("cost for hole " + hole, 1000000L - hole * 20000L, p.money);
            Kiem.bang("consume selected bank " + hole, 1,
                    locked ? p.allGemUseLock[id] : p.allGemUse[id]);
            Kiem.dung("selection cleared for next hole", p.gemItemImbue.isEmpty());
            Kiem.bang("success response", "Vật phẩm hiện có " + hole + " lỗ", p.result);
        }
    }

    private static void inlay() throws Exception {
        Player p = new Player();
        Equipment item = equip(p);
        item.nSocAdd = 2;
        p.typeGemKham = 1;
        p.typeGemImbue = 0;
        GemItem g = gem(p, 12, 0, 2);
        for (int i = 1; i <= 2; i++) {
            put(p, g);
            done(p);
            Kiem.bang("inlay used holes " + i, i, (int) item.nhadSock);
            Kiem.bang("inlay strength accumulates " + i, i, (int) item.atb[10]);
            Kiem.bang("inlay response", "Khảm thành công", p.result);
        }
    }

    private static void rejected(Player p, Equipment item, String description) throws Exception {
        long before = p.money;
        int holes = item.nSocAdd, used = item.nhadSock;
        int[] normal = p.listGemitem.clone(), locked = p.listGemitemLock.clone();
        done(p);
        Kiem.bang(description + ": no fee", before, p.money);
        Kiem.bang(description + ": client balance refreshed", p.money, p.syncedMoney);
        Kiem.bang(description + ": holes unchanged", holes, (int) item.nSocAdd);
        Kiem.bang(description + ": inlays unchanged", used, (int) item.nhadSock);
        Kiem.dung(description + ": no materials lost",
                java.util.Arrays.equals(normal, p.listGemitem)
                && java.util.Arrays.equals(locked, p.listGemitemLock));
    }

    private static void invalid() throws Exception {
        Player p = new Player();
        Equipment item = equip(p);
        rejected(p, item, "empty selection");
        GemItem g = gem(p, 61, 2, 0);
        put(p, g);
        rejected(p, item, "wrong drill grade");
        Kiem.dung("report required drill grade", p.result.contains("đục cấp 1"));
        item.nSocAdd = 1;
        put(p, g);
        p.listGemitem[61] = 0;
        rejected(p, item, "stock consumed after selection");

        p = new Player(); item = equip(p);
        g = gem(p, 12, 2, 0);
        put(p, g);
        rejected(p, item, "inlay without holes");
        item.nSocAdd = 1; item.nhadSock = 1;
        put(p, g);
        rejected(p, item, "inlay full");
        item.nhadSock = 0; item.template.type = 3;
        put(p, g);
        rejected(p, item, "incompatible equipment");

        p = new Player(); item = equip(p);
        g = gem(p, 60, 2, 0);
        item.nSocAdd = 6;
        put(p, g);
        rejected(p, item, "seventh hole");
        item.nSocAdd = 0;
        put(p, g);
        p.iItems.clear();
        rejected(p, item, "equipment removed");
        p.iItems.add(item);
        put(p, g);
        p.money = 19999;
        rejected(p, item, "not enough money");
        p.money = 1000000;
        put(p, g);
        p.listGemitemSell[60] = 2;
        rejected(p, item, "all stock listed for sale");

        p = new Player(); item = equip(p);
        g = gem(p, 66, 1, 0);
        p.gemItemImbue.add(g);
        rejected(p, item, "enhancement material cannot drill");
    }

    private static void oneMaterial() throws Exception {
        Player p = new Player();
        equip(p);
        GemItem first = gem(p, 60, 1, 0), next = gem(p, 61, 1, 0);
        put(p, first); put(p, next);
        done(p);
        Kiem.bang("only applied drill consumed", 1, p.listGemitem[61]);
        p.typeGemKham = 2;
        p.listGemitemLock[61] = 1;
        put(p, next); done(p);
        Kiem.bang("mixed bank uses locked first", 0, p.listGemitemLock[61]);
        Kiem.bang("mixed bank preserves normal", 1, p.listGemitem[61]);
    }

    private static void enhancement() throws Exception {
        Player p = new Player();
        p.typeGemImbue = 1; p.typeGemKham = 0;
        GemItem g = gem(p, 8, 0, 1);
        put(p, g);
        Kiem.dung("enhancement still uses its own bank", p.gemItemImbue.contains(g));
    }
}
