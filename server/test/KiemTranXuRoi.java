/**
 * real.Potion.quantity la short (gioi han giao thuc: Char/Map/MapClan gui no
 * ve client bang dos.writeShort(pt.quantity), toi da 32767). Truoc ban sua
 * nay, constructor Potion() ep (short)quantity KHONG kep, nen mon xu roi cua
 * quai co rcvgold lon (co con toi hang tram nghin/hang trieu trong bang
 * data_monster, xem Monster.getGoldRcv()) hoac phan thuong dac biet cua quai
 * ma (Monster: idTemplate==Map.idGhost dat quantity=1000000) bi TRAN so, co
 * khi ra SO AM - tuc nguoi choi giet quai cang manh cang de nhat phai xu vo
 * nghia, co khi bi TRU xu luc nhat. Xem Potion.java cho chi tiet ly do.
 */
public final class KiemTranXuRoi {

    public static void chay() {
        Kiem.nhom("real.Potion - kep so luong trong gioi han short cua giao thuc");

        Kiem.bang("quantity nho hon 32767 giu nguyen",
                Short.valueOf((short) 100), Short.valueOf(new real.Potion((short) 0, 100, null).quantity));

        Kiem.bang("quantity dung bang 32767 (Short.MAX_VALUE) giu nguyen",
                Short.valueOf(Short.MAX_VALUE), Short.valueOf(new real.Potion((short) 0, 32767, null).quantity));

        Kiem.bang("quantity 40000 (vd quai rcvgold cao gap x2 su kien) bi kep ve 32767, KHONG tran am",
                Short.valueOf(Short.MAX_VALUE), Short.valueOf(new real.Potion((short) 0, 40000, null).quantity));

        Kiem.bang("quantity 1000000 (phan thuong dac biet quai ma idGhost) bi kep ve 32767",
                Short.valueOf(Short.MAX_VALUE), Short.valueOf(new real.Potion((short) 0, 1000000, null).quantity));

        Kiem.bang("quantity am duoc kep ve 0, khong giu am",
                Short.valueOf((short) 0), Short.valueOf(new real.Potion((short) 0, -5, null).quantity));
    }

    private KiemTranXuRoi() {
    }
}
