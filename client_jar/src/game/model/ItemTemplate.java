package game.model;

import java.util.Hashtable;
import java.util.Vector;

public class ItemTemplate {
    public String name;
    public short type;
    public short style;
    public short index;
    public short he;
    public short gender;
    public short level;
    public short durable;
    public short idIcon;
    public int price;
    public byte clazz;
    public short id;
    public byte colorItem;
    public static final String[] nameItem = {"áo", "Quần", "Nón", "Kiếm", "Đao", "Bút", "Búa", "Cung", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Ngọc bội", "Cuốc", "Giáp linh thú", "Hộ uyển", "Nón linh thú", "Bàn đạp", "Yên cương", "Phi phong"};
    public static Hashtable ALL_NAME_ATTRIBUTE_ITEM = new Hashtable();
    public short ndayLoan = 0;
    public short[] attb = new short[10];
    public byte plus = 0;
    public Vector allAttribute = new Vector();
}
