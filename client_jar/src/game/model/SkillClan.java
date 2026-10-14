package game.model;

public class SkillClan {
    public short time;
    public int timeCur = (int) (System.currentTimeMillis() / 1000);
    public String info;
    public byte idIcon;
    public byte lv;

    public void updateTime() {
        if ((System.currentTimeMillis() / 1000) - ((long) this.timeCur) >= this.time) {
            this.time = (short) 0;
        }
    }
}
