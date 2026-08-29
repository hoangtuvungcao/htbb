package io;

/**
 * Bo dem chan NGAP GOI TIN cho mot ket noi.
 *
 * Tach rieng khoi Session vi hai ly do: Session phai co socket that moi dung
 * duoc nen khong kiem thu duoc, va logic cua so thoi gian la thu de sai am tham
 * — sai thi hoac chan nham nguoi choi that, hoac khong chan gi ca.
 *
 * BAN CU nam thang trong Session.run() va hong o ba cho:
 *
 *   1. Dem SAU khi da goi processMessage(), nen tran ngap duoc xu ly tron ven
 *      roi moi bi phat hien. Bo dem chi bao cao chu chua bao gio chan.
 *   2. Bo dem chi duoc dat lai BEN TRONG nhanh "vuot 300", nen no cong don suot
 *      ca phien — nguoi choi ngoi vai tieng cung cham 300 mot cach binh thuong.
 *   3. Bon lenh tai nguyen (-47 anh nhan vat, -48 quan ao, -49 hieu ung,
 *      -51 anh) bi loai khoi bo dem HOAN TOAN. Rieng -51 (sendByteImg) doc file
 *      tu dia va gui anh ve — spam no la vua ton dia vua ton bang thong, ma
 *      truoc gio khong co gioi han nao.
 *
 * Ban nay dung CUA SO TRUOT that su, va tach han muc rieng cho lenh tai nguyen
 * (luc vao ban do moi chung tra ve hang loat mot cach hoan toan binh thuong).
 *
 * Nguong lenh thuong giu nguyen 300 trong 5 giay nhu ban cu, de khong chan nham
 * ai — thay doi that su nam o cho no duoc kiem DUNG LUC.
 */
public final class BoDemGoiTin {

    /** So lenh thuong toi da trong mot cua so. 300/5s = 60 goi moi giay. */
    public static final int  GOI_THUONG_TOI_DA     = 300;
    /** So lenh tai nguyen toi da trong mot cua so. Rong hon vi vao map la mot chum. */
    public static final int  GOI_TAI_NGUYEN_TOI_DA = 600;
    /** Do dai cua so dem (ms). */
    public static final long CUA_SO_MS             = 5000L;

    private long mocCuaSo;
    private int  demThuong;
    private int  demTaiNguyen;

    public BoDemGoiTin(final long bayGio) {
        this.mocCuaSo = bayGio;
    }

    /**
     * Lenh tai nguyen: client tai anh / quan ao / hieu ung ve de ve nhan vat.
     * Chung khong doi trang thai game nen dem rieng, han muc rong hon.
     */
    public static boolean laLenhTaiNguyen(final int command) {
        return command == -47 || command == -48 || command == -49 || command == -51;
    }

    /**
     * Ghi nhan mot goi tin vua toi, va cho biet ket noi nay da vuot nguong chua.
     *
     * @param bayGio truyen tu ngoai vao (thay vi goi System.currentTimeMillis()
     *               ben trong) de kiem thu dieu khien duoc thoi gian.
     * @return true = vuot nguong, nen cat ket noi
     */
    public boolean vuotNguong(final int command, final long bayGio) {
        if (bayGio - this.mocCuaSo >= CUA_SO_MS) {
            this.mocCuaSo = bayGio;
            this.demThuong = 0;
            this.demTaiNguyen = 0;
        }

        final boolean taiNguyen = laLenhTaiNguyen(command);
        final int dem  = taiNguyen ? ++this.demTaiNguyen : ++this.demThuong;
        final int tran = taiNguyen ? GOI_TAI_NGUYEN_TOI_DA : GOI_THUONG_TOI_DA;
        return dem > tran;
    }

    /** So goi da dem duoc trong cua so hien tai, cho dong log khi cat ket noi. */
    public int demHienTai(final int command) {
        return laLenhTaiNguyen(command) ? this.demTaiNguyen : this.demThuong;
    }

    public int tranCua(final int command) {
        return laLenhTaiNguyen(command) ? GOI_TAI_NGUYEN_TOI_DA : GOI_THUONG_TOI_DA;
    }
}
