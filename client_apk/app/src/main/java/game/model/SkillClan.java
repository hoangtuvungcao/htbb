package game.model;

public class SkillClan {
    public byte idIcon;
    public String info;
    public byte lv;
    public short time;
    public int timeCur = (int) (System.currentTimeMillis() / 1000);

    public void updateTime() {
        if ((System.currentTimeMillis() / 1000) - ((long) this.timeCur) >= this.time) {
            this.time = (short) 0;
        }
    }
}
