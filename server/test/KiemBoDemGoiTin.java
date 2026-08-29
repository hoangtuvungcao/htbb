import io.BoDemGoiTin;

/**
 * Kiem thu bo chan NGAP GOI TIN (io.BoDemGoiTin).
 *
 * Thoi gian duoc truyen tu ngoai vao nen khong phai ngu mot giay nao — chay het
 * trong vai mili giay va khong bao gio "hen gio" that.
 *
 * Hai dieu quan trong nhat can chac:
 *   - nguoi choi that KHONG bi cat nham (60 goi/giay lien tuc van qua duoc),
 *   - ke ngap goi BI cat, va cat dung o goi thu 301 chu khong phai sau do.
 */
public final class KiemBoDemGoiTin {

    /** Mot lenh thuong bat ky (4 = di chuyen). */
    private static final int LENH_THUONG = 4;
    /** Lenh tai nguyen: -51 la sendByteImg (doc anh tu dia gui ve). */
    private static final int LENH_TAI_NGUYEN = -51;

    public static void chay() {
        Kiem.nhom("io.BoDemGoiTin - chan ngap goi tin");

        phanLoaiLenh();
        chanDungNguong();
        cuaSoTruotDatLai();
        haiLoaiDemRieng();
        khongCatNhamNguoiChoiThat();
        ngapThatSuThiBiCat();
    }

    private static void phanLoaiLenh() {
        Kiem.dung("-47 la lenh tai nguyen", BoDemGoiTin.laLenhTaiNguyen(-47));
        Kiem.dung("-48 la lenh tai nguyen", BoDemGoiTin.laLenhTaiNguyen(-48));
        Kiem.dung("-49 la lenh tai nguyen", BoDemGoiTin.laLenhTaiNguyen(-49));
        Kiem.dung("-51 la lenh tai nguyen", BoDemGoiTin.laLenhTaiNguyen(-51));
        Kiem.sai("4 (di chuyen) KHONG phai tai nguyen", BoDemGoiTin.laLenhTaiNguyen(4));
        Kiem.sai("-31 (nhap text) KHONG phai tai nguyen", BoDemGoiTin.laLenhTaiNguyen(-31));
        Kiem.sai("-50 KHONG phai tai nguyen", BoDemGoiTin.laLenhTaiNguyen(-50));
    }

    private static void chanDungNguong() {
        final BoDemGoiTin bo = new BoDemGoiTin(0L);
        boolean choPhepHet = true;
        for (int i = 1; i <= BoDemGoiTin.GOI_THUONG_TOI_DA; i++) {
            if (bo.vuotNguong(LENH_THUONG, 0L)) {
                choPhepHet = false;
            }
        }
        Kiem.dung("goi thu 1..300 deu qua duoc", choPhepHet);
        Kiem.dung("goi thu 301 bi chan", bo.vuotNguong(LENH_THUONG, 0L));
        Kiem.dung("goi thu 302 van bi chan", bo.vuotNguong(LENH_THUONG, 0L));
    }

    private static void cuaSoTruotDatLai() {
        final BoDemGoiTin bo = new BoDemGoiTin(0L);
        for (int i = 0; i < BoDemGoiTin.GOI_THUONG_TOI_DA + 5; i++) {
            bo.vuotNguong(LENH_THUONG, 0L);
        }
        Kiem.dung("dang bi chan trong cua so cu", bo.vuotNguong(LENH_THUONG, 4999L));
        Kiem.sai("qua 5000ms thi dem lai tu dau", bo.vuotNguong(LENH_THUONG, 5000L));
        Kiem.bang("sau khi dat lai, dem ve 1", Integer.valueOf(1),
                Integer.valueOf(bo.demHienTai(LENH_THUONG)));
    }

    private static void haiLoaiDemRieng() {
        final BoDemGoiTin bo = new BoDemGoiTin(0L);
        for (int i = 0; i < BoDemGoiTin.GOI_THUONG_TOI_DA; i++) {
            bo.vuotNguong(LENH_THUONG, 0L);
        }
        Kiem.dung("lenh thuong da cham tran", bo.vuotNguong(LENH_THUONG, 0L));
        Kiem.sai("lenh tai nguyen van con nguyen han muc", bo.vuotNguong(LENH_TAI_NGUYEN, 0L));

        final BoDemGoiTin bo2 = new BoDemGoiTin(0L);
        for (int i = 0; i < BoDemGoiTin.GOI_TAI_NGUYEN_TOI_DA; i++) {
            bo2.vuotNguong(LENH_TAI_NGUYEN, 0L);
        }
        Kiem.dung("tai nguyen vuot 600 thi bi chan", bo2.vuotNguong(LENH_TAI_NGUYEN, 0L));
        Kiem.sai("lenh thuong van qua duoc", bo2.vuotNguong(LENH_THUONG, 0L));
    }

    /**
     * Dieu quan trong nhat: khong duoc cat nham nguoi choi that.
     * Gia lap 60 goi moi giay LIEN TUC trong 5 phut — cao hon nhieu so voi choi
     * binh thuong — va khong duoc bi chan lan nao.
     */
    private static void khongCatNhamNguoiChoiThat() {
        final BoDemGoiTin bo = new BoDemGoiTin(0L);
        int soLanBiChan = 0;
        long thoiGian = 0L;
        // 5 phut, moi goi cach nhau 1000/60 ms
        for (int i = 0; i < 60 * 300; i++) {
            thoiGian = (long) (i * (1000.0 / 60.0));
            if (bo.vuotNguong(LENH_THUONG, thoiGian)) {
                soLanBiChan++;
            }
        }
        Kiem.bang("60 goi/giay suot 5 phut: khong bi chan lan nao",
                Integer.valueOf(0), Integer.valueOf(soLanBiChan));

        // Va ban cu cong don suot ca phien nen kieu nay se HONG: choi cham nhung lau.
        final BoDemGoiTin bo2 = new BoDemGoiTin(0L);
        int chan2 = 0;
        for (int i = 0; i < 100000; i++) {
            // 2 goi moi giay, suot 50.000 giay (gan 14 tieng)
            if (bo2.vuotNguong(LENH_THUONG, (long) (i * 500))) {
                chan2++;
            }
        }
        Kiem.bang("2 goi/giay suot 14 tieng: khong bi chan lan nao",
                Integer.valueOf(0), Integer.valueOf(chan2));
    }

    /** Con ke that su ngap goi thi phai bi cat, va cat som. */
    private static void ngapThatSuThiBiCat() {
        final BoDemGoiTin bo = new BoDemGoiTin(0L);
        int goiThuMayThiBiCat = -1;
        // Gui lien tuc trong cung mot mili giay (vong lap chat)
        for (int i = 1; i <= 5000; i++) {
            if (bo.vuotNguong(LENH_THUONG, 0L)) {
                goiThuMayThiBiCat = i;
                break;
            }
        }
        Kiem.bang("vong lap chat bi cat ngay o goi thu 301",
                Integer.valueOf(BoDemGoiTin.GOI_THUONG_TOI_DA + 1),
                Integer.valueOf(goiThuMayThiBiCat));

        // Spam lenh -51 (doc anh tu dia) - truoc day hoan toan khong bi dem
        final BoDemGoiTin bo2 = new BoDemGoiTin(0L);
        int catO = -1;
        for (int i = 1; i <= 5000; i++) {
            if (bo2.vuotNguong(LENH_TAI_NGUYEN, 0L)) {
                catO = i;
                break;
            }
        }
        Kiem.bang("spam -51 bi cat o goi thu 601",
                Integer.valueOf(BoDemGoiTin.GOI_TAI_NGUYEN_TOI_DA + 1),
                Integer.valueOf(catO));
    }
}
