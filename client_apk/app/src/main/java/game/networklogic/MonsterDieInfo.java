package game.networklogic;

public class MonsterDieInfo {
    public short attacked;
    public short attacker;

    public byte effect;
    public int hpLost;
    public ItemDropInfo[] itemDrop;
    public byte skillID;
    public byte x2 = 1;
    public byte buffAttack = -1;
    public byte level = 0;
}
