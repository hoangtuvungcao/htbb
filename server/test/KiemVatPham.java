/** Kiem thu cac ham phan loai vat pham (thuan, chi phu thuoc tham so). */
public final class KiemVatPham {

    public static void chay() {
        Kiem.nhom("real.Map - phan loai vat pham");

        // isWeapone: type 3..7 la vu khi
        Kiem.sai("isWeapone(2) khong phai vu khi", real.Map.isWeapone(2));
        Kiem.dung("isWeapone(3) la vu khi", real.Map.isWeapone(3));
        Kiem.dung("isWeapone(7) la vu khi", real.Map.isWeapone(7));
        Kiem.sai("isWeapone(8) khong phai vu khi", real.Map.isWeapone(8));
        Kiem.sai("isWeapone(-1) khong phai vu khi", real.Map.isWeapone(-1));

        // isModelClothes: chi id 1 va 2
        Kiem.dung("isModelClothes(1)", real.Map.isModelClothes(1));
        Kiem.dung("isModelClothes(2)", real.Map.isModelClothes(2));
        Kiem.sai("isModelClothes(0)", real.Map.isModelClothes(0));
        Kiem.sai("isModelClothes(3)", real.Map.isModelClothes(3));

        // isPotionUnlimit: danh sach id co dinh
        Kiem.dung("isPotionUnlimit(104)", real.Map.isPotionUnlimit((short) 104));
        Kiem.dung("isPotionUnlimit(74)", real.Map.isPotionUnlimit((short) 74));
        Kiem.sai("isPotionUnlimit(0)", real.Map.isPotionUnlimit((short) 0));
        Kiem.sai("isPotionUnlimit(999)", real.Map.isPotionUnlimit((short) 999));
    }

    private KiemVatPham() {
    }
}
