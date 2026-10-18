/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class PotionUse {
    public long timeUse = 0L;
    public int timeDelay;
    int id = 0;

    public PotionUse(int id, int delay) {
        this.id = id;
        this.timeDelay = delay;
    }

    public boolean checkCooldown() {
        long time = System.currentTimeMillis() - this.timeUse;
        if (time < this.timeDelay) {
            try {
                Thread.sleep(this.timeDelay - time);
            } catch (Exception var4) {
            }
        }

        this.timeUse = System.currentTimeMillis();
        return true;
    }

    public boolean doUsePotion() {
        if (System.currentTimeMillis() - this.timeUse <= this.timeDelay) {
            return false;
        } else {
            this.timeUse = System.currentTimeMillis();
            return true;
        }
    }

    public void setCoolDown(int time) {
        this.timeDelay = time;
        this.timeUse = System.currentTimeMillis();
    }

    public void setCoolDown() {
        this.timeDelay = 350;
        this.timeUse = System.currentTimeMillis();
    }

    public void setCoolDownTown() {
        this.timeDelay = 400;
        this.timeUse = System.currentTimeMillis();
    }
}
