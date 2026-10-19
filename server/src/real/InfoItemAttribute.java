/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class InfoItemAttribute {
    public static final byte WHITE = 0;
    public static final byte GREEN = 1;
    public static final byte RED = 2;
    public static final byte BLUE = 3;
    public static final byte MAGENTA = 4;
    public byte colorPaint = 0;
    public short id;
    public short value;
    public static short ATTACK = 0;
    public static short DEF_PHYS = 1;
    public static short NE_TRANH = 2;
    public static short CHINH_XAC = 3;
    public static short CRIT = 4;
    public static short SUC_KHOE = 5;
    public static short DEF_MAGIC = 6;
    public static short CHI_SO7 = 7;
    public static short CHI_SO8 = 8;
    public static short CHI_SO9 = 9;
    public static short UP_STRENGH = 10;
    public static short UP_NHANH_NHEN = 11;
    public static short UP_TINH_THAN = 12;
    public static short UP_SUC_KHOE = 13;
    public static short GAY_MU = 14;
    public static short DONG_BANG = 15;
    public static short TRUNG_DOC = 16;
    public static short GAY_CHOANG = 17;
    public static short HOA_THACH = 18;
    public static short GIAM_TOC = 19;
    public static short KHANG_GIAM_TOC = 20;
    public static short KHANG_TRUNG_DOC = 21;
    public static short KHANG_GAY_MU = 22;
    public static short KHANG_DONG_BANG = 23;
    public static short KHANG_GAY_CHOANG = 24;
    public static short KHANG_HOA_THACH = 25;
    public static short UP_X2_PER_HIT = 26;
    public static short UP_TYLE_ROT_BAO_VAT = 27;
    public static short GIAM_ST_VAT = 28;
    public static short GIAM_ST_MA = 29;
    public static short UP_ATTACK_NGU_HANH = 30;
    public static short XUYEN_GIAP = 31;
    public static short PHAN_ST = 32;
    public static short UP_HP = 33;
    public static short UP_MP = 34;
    public static short CONG_SM = 35;
    public static short CONG_KL = 36;
    public static short CONG_TT = 37;
    public static short CONG_SK = 38;
    public static short CONG_SKILL = 39;
    public static short UP_CRIT = 40;
    public static short UP_ST_CRIT = 41;
    public static short CHUA_DUNG = 42;
    public static short CONG_SKILL1 = 43;
    public static short CONG_SKILL2 = 44;
    public static short CONG_SKILL3 = 45;
    public static short CONG_SKILL4 = 46;
    public static short CONG_SKILL5 = 47;
    public static short CONG_SKILL6 = 48;
    public static short CONG_SKILL7 = 49;
    public static short CONG_SKILL8 = 50;
    public static short CONG_SKILL9 = 51;
    public static short CONG_SKILL10 = 52;
    public static short CONG_SKILL11 = 53;
    public static short CONG_SKILL12 = 54;
    public static short CONG_SKILL13 = 55;
    public static short CONG_SKILL14 = 56;
    public static short CONG_SKILL15 = 57;
    public static short UP_ATTACK = 58;
    public static short UP_DEF_MAGIC = 59;
    public static short UP_DEF_PHYS = 60;
    public static short BO_QUA_TC_MA = 61;
    public static short BO_QUA_TC_VAT = 62;
    public static short UP_DEF_MAGIC_TRANG_BI = 63;
    public static short UP_DEF_PHYS_TRANG_BI = 64;
    public static short BAO_KICH = 65;
    public static short SET_LAN = 66;
    public static short DOC_LAN = 67;
    public static short BANG_LAN = 68;
    public static short UP_SET_LAN = 69;
    public static short UP_DOC_LAN = 70;
    public static short UP_BANG_LAN = 71;
    public static short UP_ST_BANG_LAN = 72;
    public static short UP_ST_SET_LAN = 73;
    public static short UP_ST_DOC_LAN = 74;
    public static short HOA_NGUOI_TUYET = 75;
    public static short KHANG_BAO_KICH = 76;
    public static short HUT_HP = 77;
    public static short HOI_HP = 78;
    public static short HOI_MP = 79;
    public static short TAN_PHE = 80;
    public static short HAP_THU_SAT_THUONG = 81;
    public static short LAM_THINH = 82;
    public static short UP_TYLE_ROT_ITEM = 83;
    public static short UP_TYLE_ROT_XU = 84;
    public static short KHANG_BANG_LAN = 85;
    public static short KHANG_SET_LAN = 86;
    public static short KHANG_DOC_LAN = 87;

    public InfoItemAttribute(short id, int value) {
        this.id = id;
        this.value = (short)value;
        if (id >= 10 && id < 28) {
            this.colorPaint = 3;
        } else if (id >= 28 && id < 33 || id == 61 || id == 62) {
            this.colorPaint = 1;
        }
    }

    public byte getColorName() {
        return this.colorPaint;
    }

    public String getInfoValue() {
        NameAttributeItem name = ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM.get(this.id);
        if (name.isPercent == 0) {
            return String.valueOf(this.value);
        } else {
            return name.isPercent == 2 ? this.value / 10 + "." + this.value % 10 + "%" : this.value + "%";
        }
    }

    public static String getNameAtt(int id) {
        NameAttributeItem name = ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM.get(id);
        return name != null ? name.name : "";
    }

    public static String getInfoValue(int id, int value) {
        NameAttributeItem name = ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM.get(id);
        if (name.isPercent == 0) {
            return String.valueOf(value);
        } else {
            return name.isPercent == 2 ? value / 10 + "." + value % 10 + "%" : value + "%";
        }
    }
}
