package game.model;

public class CharClassInfo {
    public String area;
    public short bodyID;
    public byte classID;
    public String element;
    public short headID;
    public short legID;
    public String name;
    public String sex;
    public String weapon;
    public short weaponImageID;
    public short weaponType;

    public CharClassInfo(byte b, String str, String str2, String str3, String str4, String str5, int i, int i2) {
        this.classID = b;
        this.name = str;
        this.sex = str2;
        this.weapon = str3;
        this.area = str4;
        this.element = str5;
        if (str2.equals("Nam")) {
            this.bodyID = (short) 2;
            this.legID = (short) 2;
            this.headID = (short) 2;
        } else {
            this.bodyID = (short) 3;
            this.legID = (short) 3;
            this.headID = (short) 3;
        }
        this.weaponType = (short) i;
        this.weaponImageID = (short) i2;
    }
}
