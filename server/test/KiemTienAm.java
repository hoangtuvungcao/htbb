/**
 * Kiem thu phan so hoc tien te cua real.Char.
 *
 * VI SAO CO FILE NAY: mot nhan vat co `luong` am tung co the ha ca server.
 * getLuong()/getLuongLock()/getxu() goi Database.banChar() MOI LAN thay so am ma khong
 * sua so am do, nen chung ban lien tuc; moi lan ban lai ro ri mot ket noi MySQL. Ba ham
 * do co hon 400 diem goi (rieng createCharInventoryMessage cham ca hai), nen pool can
 * trong vai phut va CA SERVER mat CSDL.
 *
 * Duong duy nhat lam `luong` am ma khong qua ham tru nao la TRAN INT trong addLuong().
 * Va cac ham subLuong* thi ep long ve int TRUOC khi tru — l > 2^31 la (int)l cuon vong
 * am, tuc TRU tien lai thanh CONG tien.
 *
 * Ba ham duoi day la cho chan ca hai. Test o day khoa lai hanh vi chan tran do.
 */
public final class KiemTienAm {

    public static void chay() {
        Kiem.nhom("real.Char - congChan (cong int co chan tran)");

        Kiem.bang("cong binh thuong", Integer.valueOf(30),
                Integer.valueOf(real.Char.congChan(10, 20)));
        Kiem.bang("cong voi 0 giu nguyen", Integer.valueOf(7),
                Integer.valueOf(real.Char.congChan(7, 0)));

        // Day la ca gay ra ca chuoi loi: truoc khi vá, MAX_VALUE + 1 = so AM.
        Kiem.bang("MAX_VALUE + 1 chan o MAX chu KHONG cuon vong am",
                Integer.valueOf(Integer.MAX_VALUE),
                Integer.valueOf(real.Char.congChan(Integer.MAX_VALUE, 1)));
        Kiem.bang("MAX_VALUE + MAX_VALUE van chan o MAX",
                Integer.valueOf(Integer.MAX_VALUE),
                Integer.valueOf(real.Char.congChan(Integer.MAX_VALUE, Integer.MAX_VALUE)));
        Kiem.bang("gan tran: MAX-5 cong 3 van dung",
                Integer.valueOf(Integer.MAX_VALUE - 2),
                Integer.valueOf(real.Char.congChan(Integer.MAX_VALUE - 5, 3)));

        Kiem.dung("khong bao gio tra ve so am du cong the nao",
                real.Char.congChan(Integer.MAX_VALUE, Integer.MAX_VALUE) >= 0
                        && real.Char.congChan(Integer.MAX_VALUE, 1) >= 0);

        // Cong so am (khong loi goi nao trong src/ lam vay, nhung phai an toan)
        Kiem.bang("cong so am ra duoi 0 thi chan ve 0", Integer.valueOf(0),
                Integer.valueOf(real.Char.congChan(3, -10)));
        Kiem.bang("MIN_VALUE + MIN_VALUE chan ve 0", Integer.valueOf(0),
                Integer.valueOf(real.Char.congChan(Integer.MIN_VALUE, Integer.MIN_VALUE)));

        Kiem.nhom("real.Char - truChan (tru long khoi int, chan duoi 0)");

        Kiem.bang("tru binh thuong", Integer.valueOf(70),
                Integer.valueOf(real.Char.truChan(100, 30L)));
        Kiem.bang("tru het sach ve 0", Integer.valueOf(0),
                Integer.valueOf(real.Char.truChan(100, 100L)));
        Kiem.bang("tru qua so co van chan o 0", Integer.valueOf(0),
                Integer.valueOf(real.Char.truChan(100, 999L)));

        // Ca quan trong nhat: truoc khi vá, `luong -= (int) l` voi l = 2^31 lam
        // (int) l = MIN_VALUE, tuc `luong - MIN_VALUE` = CONG tien.
        Kiem.bang("tru 2^31 khong bien thanh cong tien", Integer.valueOf(0),
                Integer.valueOf(real.Char.truChan(100, 2147483648L)));
        Kiem.bang("tru MAX_VALUE cua long van ve 0", Integer.valueOf(0),
                Integer.valueOf(real.Char.truChan(1000000, Long.MAX_VALUE)));

        // CA SAC NET NHAT — da chay thu de doi chieu:
        //   nguoi choi co 100 luong, bi tru 4.294.967.295 (2^32-1) luong
        //   ma cu : (int) 4294967295L = -1  ->  100 - (-1) = 101  -> TRU thanh CONG
        //   ma moi: 0
        Kiem.bang("tru 2^32-1 KHONG duoc bien thanh cong tien", Integer.valueOf(0),
                Integer.valueOf(real.Char.truChan(100, 4294967295L)));
        Kiem.dung("va chac chan khong nhieu hon so ban dau",
                real.Char.truChan(100, 4294967295L) <= 100);
        Kiem.dung("moi phep tru deu cho ket qua khong am",
                real.Char.truChan(100, 2147483648L) >= 0
                        && real.Char.truChan(0, Long.MAX_VALUE) >= 0
                        && real.Char.truChan(Integer.MAX_VALUE, 1L) >= 0);

        Kiem.bang("tru 0 giu nguyen so dang co", Integer.valueOf(55),
                Integer.valueOf(real.Char.truChan(55, 0L)));
        Kiem.bang("so dang co am thi doc ra 0", Integer.valueOf(0),
                Integer.valueOf(real.Char.truChan(-5, 0L)));

        Kiem.nhom("real.Char - congChanDai (cong long co chan tran)");

        Kiem.bang("cong binh thuong", Long.valueOf(3000000000L),
                Long.valueOf(real.Char.congChanDai(1000000000L, 2000000000L)));
        Kiem.bang("MAX_VALUE + 1 chan o MAX chu khong cuon vong",
                Long.valueOf(Long.MAX_VALUE),
                Long.valueOf(real.Char.congChanDai(Long.MAX_VALUE, 1L)));
        Kiem.bang("MAX + MAX van chan o MAX", Long.valueOf(Long.MAX_VALUE),
                Long.valueOf(real.Char.congChanDai(Long.MAX_VALUE, Long.MAX_VALUE)));
        Kiem.bang("cong ra so am thi chan ve 0", Long.valueOf(0L),
                Long.valueOf(real.Char.congChanDai(5L, -100L)));
        Kiem.dung("khong bao gio tra ve so am",
                real.Char.congChanDai(Long.MAX_VALUE, Long.MAX_VALUE) >= 0L
                        && real.Char.congChanDai(Long.MIN_VALUE, Long.MIN_VALUE) >= 0L);

        Kiem.nhom("real.Char - mo phong chuoi loi cu");

        // Mo phong: nap lien tiep cho den khi tran. Truoc khi va, den luot thu 3 la am.
        int luong = 0;
        for (int i = 0; i < 5; i++) {
            luong = real.Char.congChan(luong, 1000000000);
        }
        Kiem.dung("nap 5 ty luong lien tiep: van duong, khong roi vao vong ban lien tuc",
                luong > 0);
        Kiem.bang("va dung o tran int", Integer.valueOf(Integer.MAX_VALUE),
                Integer.valueOf(luong));

        // Mo phong tiep: xai het roi xai them nua van khong am.
        int con = luong;
        for (int i = 0; i < 4; i++) {
            con = real.Char.truChan(con, 900000000L);
        }
        con = real.Char.truChan(con, 999999999999L);
        Kiem.bang("xai qua so co: dung o 0 chu khong am", Integer.valueOf(0),
                Integer.valueOf(con));
    }

    private KiemTienAm() {
    }
}
