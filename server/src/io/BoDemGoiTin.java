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

    /** Mac dinh cho may that (qua Internet): 300/5s = 60 goi moi giay. */
    public static final int GOI_THUONG_MAC_DINH     = 300;
    /** Mac dinh cho may that. Rong hon vi vao map la mot chum. */
    public static final int GOI_TAI_NGUYEN_MAC_DINH = 600;

    /**
     * Nguong that su dang dung, dat tu server.ini luc khoi dong (sv.tranGoiThuong /
     * sv.tranGoiTaiNguyen). Mac dinh giu nguyen so cu nen may nao khong dat gi thi hanh vi
     * khong doi mot chut nao.
     *
     * VI SAO PHAI DOI DUOC. Hai nguong nay duoc canh theo do tre Internet. Qua loopback
     * (may nha tu chay server de thu) KHONG CO do tre, nen mot client vua xoa cache tai anh
     * nhanh hon han va cham tran mot cach hoan toan luong thien - da xay ra that: log bao
     * "NGAP GOI TIN: cat ket noi admin ip=127.0.0.1 (601 goi tai nguyen trong 5000ms)" ba
     * lan lien trong mot phien thu, va sau khi nang tran len 6000 thi VAN bi cat o goi
     * 6001. Tuc qua loopback khong co con so "du cao" nao chac chan; may nha nen TAT han
     * phep dem tai nguyen thay vi doan mot con so.
     *
     * 0 (hoac so am) = TAT han phep dem do, dung quy uoc cua sv.socketTimeout va
     * sv.connRateWindow. Chi dat 0 o may nha; tren VPS day la lop chan spam lenh -51
     * (doc anh tu dia, vua ton dia vua ton bang thong).
     */
    public static int GOI_THUONG_TOI_DA     = GOI_THUONG_MAC_DINH;
    public static int GOI_TAI_NGUYEN_TOI_DA = GOI_TAI_NGUYEN_MAC_DINH;

    /**
     * Lenh TRA CUU chi doc (7 = hoi mau/vi tri quai) dem RIENG, mac dinh 600/5s.
     *
     * VI SAO (18/10/2026, do bang tools/pokemon/kiem_tai/BotTai 100 bot): bot GM quet id quai
     * 0..399 bang lenh 7 trong ~2.4 giay de tim quai, cham tran 300 lenh thuong va sinh 67 dong
     * "NGAP GOI TIN" trong 10 phut. Client that chi hoi quai DANG THAY tren man hinh (vai chuc
     * moi giay luc danh nhom), nen tach rieng lenh doc re tien nay ra khoi ngan sach cua lenh
     * doi trang thai (di chuyen, danh, mua ban) - lenh thuong van giu nguyen 300.
     * sv.tranGoiTraCuu; 0 = tat phep dem nay.
     */
    public static final int GOI_TRA_CUU_MAC_DINH = 600;
    public static int GOI_TRA_CUU_TOI_DA = GOI_TRA_CUU_MAC_DINH;

    /**
     * IP mien dem ngap (sv.tranGoiMienIp, phan cach dau phay) - CHI cho may chay bot tai
     * (tools/pokemon/kiem_tai) de do suc chiu ma khong bi bo goi. Mac dinh RONG. Tren VPS de
     * trong: mot IP mien la mot cua spam khong khoa.
     */
    public static volatile java.util.Set<String> MIEN_IP = java.util.Collections.emptySet();

    public static boolean mienIp(final String ip) {
        return ip != null && !MIEN_IP.isEmpty() && MIEN_IP.contains(ip.trim());
    }

    public static void datMienIp(final String ds) {
        final java.util.Set<String> r = new java.util.HashSet<String>();
        if (ds != null) {
            for (final String x : ds.split(",")) {
                if (!x.trim().isEmpty()) {
                    r.add(x.trim());
                }
            }
        }
        MIEN_IP = java.util.Collections.unmodifiableSet(r);
    }

    /** Do dai cua so dem (ms). */
    public static final long CUA_SO_MS             = 5000L;

    /**
     * Vuot tran BAO NHIEU LAN thi moi that su cat ket noi.
     *
     * VI SAO CO CON SO NAY. Ban dau vuot tran la cat ngay, va dieu do cat nham nguoi
     * choi luong thien: khi hanh trang day, moi duong xu ly tren server deu tra ve
     * NGAY (khong ghi CSDL, khong tra goi trang thai) nen mat het "phanh" tu nhien,
     * con vong lap auto ben client thi ban lai moi khung hinh. Nhip goi tin vot len
     * vai chuc lan/giay va cham tran 300/5s trong vai giay - nguoi choi bi van ra
     * khoi game giua luc dang lam do.
     *
     * Cach xu ly dung: vuot tran thi BO BOT goi (server khong xu ly nua, nhip tu ha
     * xuong dung tran), chi cat ket noi khi nhip cao gap BOI nay - luc do khong con
     * la nguoi choi nhanh tay nua. Cat ket noi la hinh phat rat nang vi no keo theo
     * mot vong luu/nap nhan vat.
     */
    public static final int  BOI_CAT_KET_NOI       = 4;

    private long mocCuaSo;
    private int  demThuong;
    private int  demTaiNguyen;
    private int  demTraCuu;
    /** Da ghi log "bo bot goi" trong cua so hien tai chua (chi ghi 1 dong moi cua so). */
    private boolean daBaoTrongCuaSo;

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

    /** Lenh tra cuu chi doc, re tien (xem GOI_TRA_CUU_TOI_DA). */
    public static boolean laLenhTraCuu(final int command) {
        return command == 7;
    }

    private int dem(final int command) {
        return laLenhTaiNguyen(command) ? this.demTaiNguyen
                : laLenhTraCuu(command) ? this.demTraCuu : this.demThuong;
    }

    private static int tran(final int command) {
        return laLenhTaiNguyen(command) ? GOI_TAI_NGUYEN_TOI_DA
                : laLenhTraCuu(command) ? GOI_TRA_CUU_TOI_DA : GOI_THUONG_TOI_DA;
    }

    /** "thuong" / "tai nguyen" / "tra cuu" - cho dong log. */
    public static String tenLoai(final int command) {
        return laLenhTaiNguyen(command) ? "tai nguyen" : laLenhTraCuu(command) ? "tra cuu" : "thuong";
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
            this.demTraCuu = 0;
            this.daBaoTrongCuaSo = false;
        }

        final int dem;
        if (laLenhTaiNguyen(command)) {
            dem = ++this.demTaiNguyen;
        } else if (laLenhTraCuu(command)) {
            dem = ++this.demTraCuu;
        } else {
            dem = ++this.demThuong;
        }
        final int tran = tran(command);
        if (tran <= 0) {
            // 0 = TAT han phep dem nay, dung quy uoc cua sv.socketTimeout va
            // sv.connRateWindow trong ban mau. PHAI co nhanh nay: khong thi tran=0 lam
            // "dem > tran" dung ngay o goi DAU TIEN, tuc ai dat =0 tuong la tat thi hoa ra
            // cat sach moi ket noi. Chi may nha moi nen tat.
            return false;
        }
        return dem > tran;
    }

    /**
     * Nhip goi tin da cao gap BOI_CAT_KET_NOI lan tran chua?
     *
     * Chi khi do moi cat ket noi. Duoi muc do, goi vuot tran chi bi BO (server khong
     * xu ly) - ket noi giu nguyen, nhan vat khong bi mot vong luu/nap nao.
     *
     * Chi co nghia khi vuotNguong() vua tra ve true.
     */
    public boolean vuotXaNguong(final int command) {
        final int dem  = dem(command);
        final int tran = tran(command);
        if (tran <= 0) {
            return false;
        }
        return dem > tran * BOI_CAT_KET_NOI;
    }

    /**
     * Xin phep ghi MOT dong log cho cua so dem hien tai.
     *
     * Can co: khi bo bot goi thay vi cat ket noi, ket noi con song nen moi goi vuot
     * tran deu se sinh ra mot dong log - hang chuc nghin dong moi phut. Chan lai con
     * dung 1 dong moi cua so 5 giay.
     */
    public boolean xinPhepBao() {
        if (this.daBaoTrongCuaSo) {
            return false;
        }
        this.daBaoTrongCuaSo = true;
        return true;
    }

    // ------------------------------------------------------------------ quyet dinh cho mot goi
    /** Xu ly binh thuong. */
    public static final int QUA  = 0;
    /** Lenh thuong vuot tran: BO goi, giu ket noi. */
    public static final int BO   = 1;
    /** Vuot xa tran (BOI_CAT_KET_NOI lan): cat ket noi. */
    public static final int CAT  = 2;
    /** Lenh TAI NGUYEN vuot tran: KHONG bo - cho hoanMs() roi moi xu ly (ha nhip, khong mat goi). */
    public static final int HOAN = 3;

    /**
     * Quyet dinh cho goi tin vua toi.
     *
     * VI SAO LENH TAI NGUYEN KHONG BI BO NUA (19/10/2026, open test "tai du lieu cham, khong vao
     * duoc"): client xin anh / quan ao / hieu ung MOT lan roi cho. Bo goi la mat han anh do -
     * lenh -47 (anh quai) client danh dau "da xin" va KHONG BAO GIO xin lai, -49 chi xin lai khi
     * co hieu ung moi, -48 / -51 cho 15-30 giay moi xin lai. Tuc moi goi bi bo la mot o trong /
     * quai vo hinh hoac mot lan cho 30 giay. Thay vao do: vuot tran thi luong doc CUA RIENG ket
     * noi nay ngu hoanMs() (~8 ms voi tran 600/5 s) truoc khi xu ly - nhip bi ep ve dung tran,
     * goi van duoc tra loi theo dung thu tu, ket noi khac khong anh huong. Ke spam -51 van bi
     * ghim o ~tran/5 s (khong ton dia/bang thong hon truoc), chi la khong lam hai nguoi that.
     */
    public int phanLoai(final int command, final long bayGio) {
        if (!vuotNguong(command, bayGio)) {
            return QUA;
        }
        if (laLenhTaiNguyen(command)) {
            return HOAN;
        }
        return vuotXaNguong(command) ? CAT : BO;
    }

    /** Thoi gian cho moi goi tai nguyen vuot tran: CUA_SO_MS / tran (tran 600 -> 8 ms), toi thieu 1 ms. */
    public static long hoanMs(final int command) {
        final int tran = tran(command);
        return tran <= 0 ? 0L : Math.max(1L, CUA_SO_MS / tran);
    }

    /** So goi da dem duoc trong cua so hien tai, cho dong log khi cat ket noi. */
    public int demHienTai(final int command) {
        return dem(command);
    }

    public int tranCua(final int command) {
        return tran(command);
    }
}
