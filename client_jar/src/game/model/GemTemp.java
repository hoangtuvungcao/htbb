package game.model;

import game.render.Res;

public class GemTemp {
    public byte shopType;
    public byte type;
    public byte typeEp;
    public short value;
    public short number;
    public short id;
    public byte typeMoney;
    public boolean isSell;
    /** Dòng cửa hàng đặc biệt trả bằng LƯỢNG KHOÁ (gói 25 nối thêm / nhãn "[Lượng khoá] "; game.pokemon.TienShop). */
    public boolean luongKhoa;
    public static byte TYPE_IMBUE = 0;
    public static byte TYPE_KHAM = 1;
    public static byte TYPE_DUC_LO = 2;
    public static byte TYPE_CREATE_ITEM = 3;
    public static byte TYPE_TU_BINH = 4;
    public static byte TYPE_HOP_ITEM_ANIMAL = 5;
    public static byte TYPE_ADD_NEW_ATTRIBUTE = 6;
    public static byte TYPE_LOCK_ANIMAL = 7;
    public static byte TYPE_LOCK_ITEM = 8;
    public static byte TYPE_BOT = 9;
    public static byte TYPE_DA_TIEN_GIAI = 10;
    public static int[] color = {7667443, 3334656, 16705024};
    public String name = "";
    public String decript = "";
    public short idImage = 0;
    public int price = 0;
    public byte isEff = -1;
    public int count = Res.rnd(68);
}
