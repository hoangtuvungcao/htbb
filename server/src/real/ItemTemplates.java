/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.Vector;

public class ItemTemplates {
    public static final byte TYPE_AO = 0;
    public static final byte TYPE_QUAN = 1;
    public static final byte TYPE_NON = 2;
    public static final byte TYPE_KIEM = 3;
    public static final byte TYPE_DAO = 4;
    public static final byte TYPE_DUA = 5;
    public static final byte TYPE_BUA = 6;
    public static final byte TYPE_CUNG = 7;
    public static final byte TYPE_QUEST = 8;
    public int id;
    public String name;
    public byte type;
    public byte style;
    public byte gender;
    public byte he;
    public byte level;
    public byte ystart;
    public byte color;
    public short durable;
    public short idIcon;
    public short idItemUpLevel = -1;
    public short idBigImgAvatar = -1;
    public short[] atb = new short[10];
    public byte droppercent;
    public int price;
    public int idEff = -1;
    public byte clazz;
    public int kind_class;
    public short ndayLoan = 0;
    public static Vector<NameAttributeItem> ALL_NAME_ATTRIBUTE_ITEM = new Vector<>();
    public static final String[] ALL_NAME_ATTRIBUTE = new String[]{
        "Tấn công",
        "Thủ vật",
        "Né tránh",
        "Chính xác",
        "Chí mạng",
        "Sức khỏe",
        "Thủ ma",
        "chỉ số 7",
        "chỉ số 8",
        "chỉ số 9",
        "Tăng sức mạnh",
        "Tăng nhanh nhẹn",
        "tăng tinh thần",
        "Tăng sức khoẻ",
        "Gây mù",
        "Đóng băng",
        "Trúng độc",
        "Gây choáng",
        "Hoá thạch",
        "Giảm tốc",
        "Kháng giảm tốc",
        "Kháng trúng độc",
        "Kháng gây mù",
        "Kháng đóng băng",
        "Kháng gây choáng",
        "Kháng hoá thạch",
        "Tăng x2 mỗi lần đánh",
        "Tăng tỷ lệ rớt bảo vật",
        "Giảm st vật",
        "Giảm st ma",
        "Tăng tấn công",
        "Xuyên giáp",
        "Phản sát thương",
        "Tăng mp",
        "Tăng hp",
        "Cộng sức mạnh",
        "Cộng khéo léo",
        "Cộng tinh thần",
        "Cộng sức khoẻ",
        "Cộng kỹ năng",
        "Tăng chí mạng",
        "Tăng st chí mạng",
        "chưa dùng",
        "kỹ năng 1 cộng thêm",
        "kỹ năng 2 cộng thêm",
        "kỹ năng 3 cộng thêm",
        "kỹ năng 4 cộng thêm",
        "kỹ năng 5 cộng thêm",
        "kỹ năng 6 cộng thêm",
        "kỹ năng 7 cộng thêm",
        "kỹ năng 8 cộng thêm",
        "kỹ năng 9 cộng thêm",
        "kỹ năng 10 cộng thêm",
        "kỹ năng 11 cộng thêm",
        "kỹ năng 12 cộng thêm",
        "kỹ năng 13 cộng thêm",
        "kỹ năng 14 cộng thêm",
        "kỹ năng 15 cộng thêm",
        "Tăng công",
        "Tăng thủ ma",
        "Tăng thủ vật",
        "Bỏ qua tc ma",
        "Bỏ qua tc vật"
    };
}
