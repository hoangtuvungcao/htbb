package game.model;

public class PartyInfo {
    public int clazz;
    public int id;
    public int lv;
    public String name;

    public PartyInfo(int i, String str, int i2, int i3) {
        this.id = i;
        this.name = str;
        this.lv = i2;
        this.clazz = i3;
    }
}
