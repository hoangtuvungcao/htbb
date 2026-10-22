package game.pokemon;

import game.model.Char;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

/**
 * Tab "Trang bị" kiểu Pokémon (WindowInfoScr / ShopNew): 12 ô bo góc quanh hình nhân vật
 * (cột trái, cột phải, ruy băng + thú cưỡi phía dưới) thay ảnh khung kiếm hiệp, ô đang chọn viền
 * vàng dày, tên ô (tên đồ Pokémon mới: Khăn quàng, Đai, Băng đô, Huy hiệu, Vòng cổ, Găng đấm,
 * Ruy băng...) trên viên ở dưới. Vị trí icon đồ giữ như cũ (posPaintWearingX/Y của bảng).
 */
public final class BangTrangBi {
    /** tâm hàng ô (y) của cột trái / phải */
    private static final int[] Y = {11, 32, 52, 73, 94};
    private static final int X_TRAI = 9;
    private static final int X_PHAI = 107;
    private static final int[] AO = {34, 92};
    private static final int[] THU = {82, 92};

    private static final String[] TEN_TRAI = {"Phiến đá hệ", "Khăn quàng", "Băng đô", "Đai", "Lông vũ tốc độ"};
    private static final String[] TEN_PHAI = {null, "Vòng cổ", "Huy hiệu", "Huy hiệu", "Găng đấm"};
    /** đồ cầm tay theo dòng (class) */
    private static final String[] TEN_CAM = {"Than lửa", "Hạt giống thần", "Khăn lụa", "Nước thần", "Nam châm"};

    /** Tên loại đồ (type của ItemTemplate) theo tên Pokémon mới; null nếu không rõ. */
    public static String tenLoai(int type) {
        switch (type) {
            case 0:
                return "Khăn quàng";
            case 1:
                return "Đai";
            case 2:
                return "Băng đô";
            case 3:
                return "Than lửa";
            case 4:
                return "Hạt giống thần";
            case 5:
                return "Khăn lụa";
            case 6:
                return "Nước thần";
            case 7:
                return "Nam châm";
            case 8:
                return "Huy hiệu";
            case 9:
                return "Vòng cổ";
            case 10:
                return "Lông vũ tốc độ";
            case 11:
                return "Găng đấm";
            case 12:
                return "Phiến đá hệ";
            case 13:
                return "Cuốc đào mỏ";
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                return "Đồ thú cưỡi";
            case 19:
                return "Ruy băng";
            case 25:
                return "Thú cưỡi";
            case 26:
                return "Trứng Pokémon";
            case 27:
                return "Pokémon đi theo";
            default:
                return null;
        }
    }

    private BangTrangBi() {
    }

    /** {tâm x, tâm y} của ô đang chọn. */
    private static int[] tamO(int chon) {
        if (chon == 4) {
            return AO;
        }
        int cot = chon % 3;
        int hang = chon / 3;
        if (hang < 0 || hang > 4) {
            hang = 0;
        }
        if (cot == 0) {
            return new int[]{X_TRAI, Y[hang]};
        }
        if (cot == 2) {
            return new int[]{X_PHAI, Y[hang]};
        }
        return THU;
    }

    private static String tenO(int chon, int lop) {
        if (chon == 4) {
            return "Ruy băng";
        }
        int cot = chon % 3;
        int hang = chon / 3;
        if (hang < 0 || hang > 4) {
            return null;
        }
        if (cot == 0) {
            return TEN_TRAI[hang];
        }
        if (cot == 2) {
            if (hang == 0) {
                return lop >= 0 && lop < TEN_CAM.length ? TEN_CAM[lop] : "Đồ cầm tay";
            }
            return TEN_PHAI[hang];
        }
        return "Thú cưỡi";
    }

    private static void o(Graphics g, int cx, int cy, boolean co, boolean chon) {
        KieuO.oDo(g, cx - 9, cy - 9, 18, co, false);
        if (chon) {
            KieuO.nenChon(g, cx - 9, cy - 9, 18);
        }
    }

    /**
     * Vẽ ô + đồ đang mặc + viền chọn + tên ô. posX/posY = posPaintWearingX/Y của bảng gọi.
     */
    public static void ve(Graphics g, Char cw, int chon, boolean doiTab, int[] posX, int[] posY, int w) {
        boolean[] co = new boolean[12];   // 0..4 trái, 5..9 phải, 10 ruy băng, 11 thú
        if (cw.wearingItems != null) {
            for (int i = 0; i < cw.wearingItems.size(); i++) {
                ItemInInventory item = (ItemInInventory) cw.wearingItems.elementAt(i);
                ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
                if (it == null) {
                    continue;
                }
                int[] p = viTri(it.type, item.viTriVe, posX, posY);
                danhDau(co, p[0], p[1]);
            }
        }
        if (cw.idAnimal > -1) {
            co[11] = true;
        }
        int[] sel = doiTab ? null : tamO(chon);
        for (int i = 0; i < 5; i++) {
            o(g, X_TRAI, Y[i], co[i], sel != null && sel[0] == X_TRAI && sel[1] == Y[i]);
            o(g, X_PHAI, Y[i], co[5 + i], sel != null && sel[0] == X_PHAI && sel[1] == Y[i]);
        }
        o(g, AO[0], AO[1], co[10], sel == AO);
        o(g, THU[0], THU[1], co[11], sel == THU);
        if (cw.wearingItems != null) {
            for (int i = 0; i < cw.wearingItems.size(); i++) {
                ItemInInventory item = (ItemInInventory) cw.wearingItems.elementAt(i);
                ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
                if (it == null) {
                    continue;
                }
                int[] p = viTri(it.type, item.viTriVe, posX, posY);
                int hiem = KieuO.mauHiem(item.colorName);
                if (hiem >= 0 && !IconVatPham.veKhungHiem(g, item.colorName, p[0], p[1], 18)) {
                    KieuO.khungHiem(g, p[0] - 9, p[1] - 9, 18, hiem);
                }
                IconVatPham.ve(g, it.idIcon, p[0], p[1], 22, IconVatPham.DP_DO);
                if (item.plusTemplate > 0) {
                    KieuO.capCuong(g, p[0] - 9, p[1] - 9, item.plusTemplate);
                }
            }
        }
        if (cw.idAnimal > -1 && !Canvas.gameScr.mainChar.isDoing) {
            GameData.paintIcon(g, (short) (cw.idPaintIconAnimal + 7500), THU[0], THU[1]);
        }
        if (sel == null) {
            return;
        }
        KieuO.chonO(g, sel[0] - 9, sel[1] - 9, 18);
        String ten = tenO(chon, cw.clazz);
        if (ten != null) {
            Font f = Font.arialFontW;
            int wt = f.getWidth(ten) + 12;
            int xt = w / 2 - wt / 2 + 1;
            GiaoDien.khoi(g, xt, 102, wt, 13, 6, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, xt + 1, 103, wt - 2, 11, 5, GiaoDien.DO_SANG, GiaoDien.DO_TOI);
            f.drawString(g, ten, xt + wt / 2, 101, Font.CENTER);
        }
    }

    private static int[] viTri(int type, int viTriVe, int[] posX, int[] posY) {
        if (type == 8) {
            return new int[]{X_PHAI, viTriVe == 1 ? 52 : 73};
        }
        if (type >= 0 && type < posX.length && type < posY.length) {
            return new int[]{posX[type], posY[type]};
        }
        return new int[]{X_TRAI, Y[0]};
    }

    private static void danhDau(boolean[] co, int x, int y) {
        for (int i = 0; i < 5; i++) {
            if (y == Y[i] && x == X_TRAI) {
                co[i] = true;
            } else if (y == Y[i] && x == X_PHAI) {
                co[5 + i] = true;
            }
        }
        if (x == AO[0] && y == AO[1]) {
            co[10] = true;
        }
        if (x == THU[0] && y == THU[1]) {
            co[11] = true;
        }
    }
}
