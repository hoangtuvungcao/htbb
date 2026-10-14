package game.model;

public class CharInfo {
    public short id;
    public String name;
    public short x;
    public short y;
    public byte headStyle;
    public byte idBoss;
    public byte clazz;
    public byte pk;
    public byte lv;
    public byte nskill;
    public int hp;
    public int maxhp;
    public int mp;
    public int maxmp;
    public int he;
    public short killer;
    public short defend;
    public boolean isKiller;
    public boolean isMaster;
    public byte speed = 3;
    public byte rideHorse = -1;
    public short idClan = -1;
    public byte[] buffType = {-1, -1, -1, -1, -1, -1, -1};
    public short[] countDown = {-1, -1, -1, -1, -1, -1, -1};
}
