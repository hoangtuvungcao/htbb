package real;

/**
 * Quai cua Pho ban Ham nguc (0.9.7): KHONG BAO GIO hoi sinh (Region goi setTimeReBorn khi quai thuong chet - quai
 * trieu hoi MonsterSumon van hoi sinh lai voi HP cua template). Chet roi thi real.PhoBanGame go khoi khu va tra id.
 */
public class MonsterPhoBan extends MonsterSumon {

    public MonsterPhoBan(final Map map, final MonsterTemplate t, final int x, final int y, final int country) {
        super(map, t, x, y, country);
    }

    @Override
    public void setTimeReBorn() {
        this.bornTime = Long.MAX_VALUE;
        this.isDead = true;
    }

    @Override
    public void setTimeReBornInEvent(final long time) {
        this.bornTime = Long.MAX_VALUE;
    }
}
