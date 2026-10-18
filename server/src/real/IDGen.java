/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.concurrent.ArrayBlockingQueue;

public class IDGen {
    public static final int maxCatagory = 10;
    public static String[] catname = new String[]{"PLAYER", "MONSTER", "NPC", "ITEM", "POTION", "PARTY", "GEMITEM", "SPECIAL_ITEM", "CAT8", "CAT9", "CAT10"};
    private ArrayBlockingQueue<Short>[] queue = new ArrayBlockingQueue[10];

    public void setMaxCatalory(int size, int[] indexgen, int[] sizeGen) {
        for (int c = 0; c < size; c++) {
            int queueSize = sizeGen[c];
            this.queue[indexgen[c]] = new ArrayBlockingQueue<>(queueSize * 2 + 10);

            for (int i = -queueSize; i <= queueSize; i++) {
                this.queue[indexgen[c]].add(new Short((short)i));
            }
        }
    }

    public IDGen(int queueSize) {
    }

    public short getID(int catagory, String reason) {
        Short s = this.queue[catagory].poll();
        return s == null ? 0 : s;
    }

    public void putID(short idreturn, int catagory, String reason) {
        if (catagory == 1) {
            System.out.println("PUT CAT=" + catagory + " reason=" + reason + " " + this.queue[catagory].size());
        }

        Short s = new Short(idreturn);
        if (!this.queue[catagory].contains(s)) {
            this.queue[catagory].add(s);
            if (catagory == 1) {
                System.out.println("SL MONS: " + this.queue[catagory].size());
            }
        }
    }

    public int getSize(int catagory) {
        return this.queue[catagory].size();
    }

    public String getSizeAll() {
        String s = "";

        for (int i = 0; i < this.queue.length; i++) {
            s = s + catname[i] + "=" + this.getSize(i) + ", ";
        }

        return s;
    }
}
