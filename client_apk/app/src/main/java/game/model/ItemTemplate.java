package game.model;

import java.util.Hashtable;
import java.util.Vector;

public class ItemTemplate {
    public byte clazz;
    public byte colorItem;
    public short durable;
    public short gender;
    public short he;
    public short id;
    public short idIcon;
    public short index;
    public short level;
    public String name;
    public int price;
    public short style;
    public short type;
    public static final String[] nameItem = {"áo", "Quần", "Nón", "Kiếm", "Đao", "Bút", "Búa", "Cung", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Ngọc bội", "Cuốc", "Giáp linh thú", "Hộ uyển", "Nón linh thú", "Bàn đạp", "Yên cương", "Phi phong"};
    public static Hashtable ALL_NAME_ATTRIBUTE_ITEM = new Hashtable();
    public short ndayLoan = 0;
    public short[] attb = new short[10];
    public byte plus = 0;
    public Vector allAttribute = new Vector();
}
