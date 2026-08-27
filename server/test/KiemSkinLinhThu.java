/**
 * Kiem thu tinh nang skin linh thu (real.SkinLinhThu).
 *
 * Chi kiem cac ham THUAN - khong can Char, khong can DataGame da nap anh.
 * Cac ham can Char (dangMang, idAnhBanDo, bay, ghiAnhTrangBiThu...) khong kiem
 * duoc o day vi chung can mot doi tuong Char that, ma tao Char can Session +
 * CSDL. Do la gioi han that su cua bo test nay.
 */
public final class KiemSkinLinhThu {

    public static void chay() {
        Kiem.nhom("real.SkinLinhThu");

        Kiem.bang("co dung 5 skin", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.SO_SKIN));
        Kiem.bang("mang TEN du 5 phan tu", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.TEN.length));
        Kiem.bang("mang ID_ITEM du 5 phan tu", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.ID_ITEM.length));
        Kiem.bang("mang BAY du 5 phan tu", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.BAY.length));

        // hopLe: bien 0..4
        Kiem.sai("hopLe(-1)", real.SkinLinhThu.hopLe(-1));
        Kiem.dung("hopLe(0)", real.SkinLinhThu.hopLe(0));
        Kiem.dung("hopLe(4)", real.SkinLinhThu.hopLe(4));
        Kiem.sai("hopLe(5) - ngoai bien", real.SkinLinhThu.hopLe(5));
        Kiem.sai("hopLe(KHONG_CO)", real.SkinLinhThu.hopLe(real.SkinLinhThu.KHONG_CO));

        // skinCuaItem / laItemSkin: anh xa id vat pham -> so hieu skin
        for (int i = 0; i < real.SkinLinhThu.SO_SKIN; i++) {
            final int idItem = real.SkinLinhThu.ID_ITEM[i];
            Kiem.bang("skinCuaItem(" + idItem + ") = " + i,
                    Integer.valueOf(i), Integer.valueOf(real.SkinLinhThu.skinCuaItem(idItem)));
            Kiem.dung("laItemSkin(" + idItem + ")", real.SkinLinhThu.laItemSkin(idItem));
        }
        Kiem.bang("skinCuaItem(1) = KHONG_CO",
                Integer.valueOf(real.SkinLinhThu.KHONG_CO), Integer.valueOf(real.SkinLinhThu.skinCuaItem(1)));
        Kiem.sai("laItemSkin(1)", real.SkinLinhThu.laItemSkin(1));

        // ID_ITEM khong duoc trung nhau, neu trung thi skinCuaItem tra ve sai skin
        for (int i = 0; i < real.SkinLinhThu.ID_ITEM.length; i++) {
            for (int j = i + 1; j < real.SkinLinhThu.ID_ITEM.length; j++) {
                Kiem.sai("ID_ITEM[" + i + "] khac ID_ITEM[" + j + "]",
                        real.SkinLinhThu.ID_ITEM[i] == real.SkinLinhThu.ID_ITEM[j]);
            }
        }

        // moTa: skin hop le tra ve ten, khong hop le tra ve "khong co"
        Kiem.bang("moTa(0)", real.SkinLinhThu.TEN[0], real.SkinLinhThu.moTa(0));
        Kiem.bang("moTa(4)", real.SkinLinhThu.TEN[4], real.SkinLinhThu.moTa(4));
        Kiem.bang("moTa(-1)", "không có", real.SkinLinhThu.moTa(-1));
        Kiem.bang("moTa(5)", "không có", real.SkinLinhThu.moTa(5));
    }

    private KiemSkinLinhThu() {
    }
}
