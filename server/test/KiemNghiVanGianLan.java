import data.NghiVanGianLan;

/**
 * Kiem thu bo ghi NGHI VAN GIAN LAN (data.NghiVanGianLan).
 *
 * Thoi gian truyen tu ngoai vao nen khong phai cho het mot gio nao — chay xong
 * trong vai mili giay.
 *
 * Hai dieu quan trong nhat can chac:
 *   - mot ke gian lan that VAN duoc ghi lai (khong bi cat oan ngay tu dau),
 *   - mot phep kiem bao dong gia lien tuc KHONG the lam day o dia.
 *
 * Dieu thu hai la ly do bo dem nay ton tai. Ngay 29/08/2026 log/game.out tren
 * VPS len 146 MB vi 326.826 lan in cung mot vet goi tu mot khoi catch nam trong
 * vong lap chay moi 300ms. Ghi log khong han muc trong duong chay lien tuc thi
 * som muon cung day o dia.
 */
public final class KiemNghiVanGianLan {

    public static void chay() {
        Kiem.nhom("data.NghiVanGianLan - ghi nghi van gian lan");

        dinhDangMotDong();
        khongDayNoiDungSangDongKhac();
        catChiTietQuaDai();
        keGianLanThatVanDuocGhi();
        motNhanVatSpamThiBiCat();
        cuaSoGioTruotThiDemLai();
        tranToanServer();
    }

    // ------------------------------------------------------------- dinh dang

    private static void dinhDangMotDong() {
        NghiVanGianLan.datLai();
        final String d = NghiVanGianLan.taoDong(
                "2026-08-29 12:00:00", "AnhHung", "1.2.3.4", "hackbitem", "mua item id: -5");
        Kiem.bang("mot su kien la mot dong, ngan bang ' | '",
                "2026-08-29 12:00:00 | AnhHung | 1.2.3.4 | hackbitem | mua item id: -5", d);
        Kiem.sai("khong co ky tu xuong dong", d.indexOf('\n') >= 0);
    }

    /**
     * Ten nhan vat va chi tiet la du lieu tu nguoi choi. Neu chung day duoc ky tu
     * xuong dong hay dau ngan cot vao file thi ke gian lan tu che duoc dong log
     * cua chinh minh, va "grep" theo ten se dem sai.
     */
    private static void khongDayNoiDungSangDongKhac() {
        NghiVanGianLan.datLai();
        final String d = NghiVanGianLan.taoDong(
                "2026-08-29 12:00:00", "Ke\nGian", "1.2.3.4", "hackc", "a\r\nb\tc | d");
        Kiem.sai("ten co xuong dong: da bi loc", d.indexOf('\n') >= 0);
        Kiem.sai("ten co xuong dong: khong con \\r", d.indexOf('\r') >= 0);
        // Dong phai co dung 5 cot. Neu du lieu nguoi choi day them dau | vao thi
        // se thanh 6 cot tro len va cot cuoi bi tach doi.
        final String[] cot = d.split(" \\| ");
        Kiem.bang("dong luon co dung 5 cot", Integer.valueOf(5), Integer.valueOf(cot.length));
        Kiem.sai("cot chi tiet khong con dau |", cot[4].indexOf('|') >= 0);
        Kiem.dung("van con doc duoc noi dung", d.indexOf("Ke Gian") > 0);
    }

    private static void catChiTietQuaDai() {
        NghiVanGianLan.datLai();
        final StringBuilder dai = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            dai.append('x');
        }
        final String d = NghiVanGianLan.taoDong(
                "2026-08-29 12:00:00", "A", "1.2.3.4", "hackc", dai.toString());
        Kiem.dung("chi tiet 1000 ky tu bi cat ngan",
                d.length() < 100 + NghiVanGianLan.CHI_TIET_TOI_DA + 10);
        Kiem.dung("co dau ... bao la da cat", d.endsWith("..."));
    }

    // -------------------------------------------------------------- han muc

    /** Dieu quan trong nhat: ke gian lan that khong duoc bi bo qua. */
    private static void keGianLanThatVanDuocGhi() {
        NghiVanGianLan.datLai();
        boolean ghiHet = true;
        for (int i = 0; i < NghiVanGianLan.TOI_DA_MOI_NHAN_VAT; i++) {
            if (NghiVanGianLan.xinPhepGhi("KeGian", 0L) != NghiVanGianLan.CHO) {
                ghiHet = false;
            }
        }
        Kiem.dung("20 su kien dau cua mot nhan vat deu duoc ghi", ghiHet);
    }

    private static void motNhanVatSpamThiBiCat() {
        NghiVanGianLan.datLai();
        for (int i = 0; i < NghiVanGianLan.TOI_DA_MOI_NHAN_VAT; i++) {
            NghiVanGianLan.xinPhepGhi("Spam", 0L);
        }
        Kiem.bang("su kien thu 21 ghi mot dong bao cham tran",
                Integer.valueOf(NghiVanGianLan.CHO_VA_BAO_CHAM_TRAN),
                Integer.valueOf(NghiVanGianLan.xinPhepGhi("Spam", 0L)));
        Kiem.bang("su kien thu 22 im hoan toan",
                Integer.valueOf(NghiVanGianLan.KHONG_CHO),
                Integer.valueOf(NghiVanGianLan.xinPhepGhi("Spam", 0L)));

        // 100.000 lan lien tuc trong cung mot mili giay - gia lap dung canh da lam
        // game.out phinh len 146 MB.
        int soDongThucSuGhi = 0;
        for (int i = 0; i < 100000; i++) {
            if (NghiVanGianLan.xinPhepGhi("Spam", 0L) != NghiVanGianLan.KHONG_CHO) {
                soDongThucSuGhi++;
            }
        }
        Kiem.bang("100.000 lan bao dong lien tuc: khong ghi them dong nao",
                Integer.valueOf(0), Integer.valueOf(soDongThucSuGhi));
    }

    private static void cuaSoGioTruotThiDemLai() {
        NghiVanGianLan.datLai();
        for (int i = 0; i < NghiVanGianLan.TOI_DA_MOI_NHAN_VAT + 5; i++) {
            NghiVanGianLan.xinPhepGhi("A", 0L);
        }
        Kiem.bang("van bi cat khi chua het gio",
                Integer.valueOf(NghiVanGianLan.KHONG_CHO),
                Integer.valueOf(NghiVanGianLan.xinPhepGhi("A", NghiVanGianLan.CUA_SO_MS - 1)));
        Kiem.bang("sang gio moi thi ghi lai binh thuong",
                Integer.valueOf(NghiVanGianLan.CHO),
                Integer.valueOf(NghiVanGianLan.xinPhepGhi("A", NghiVanGianLan.CUA_SO_MS)));
    }

    /**
     * Han muc theo nhan vat khong du: mot phep kiem bao dong gia se bao dong tren
     * MOI nguoi dang online cung luc. Phai co them tran toan server.
     */
    private static void tranToanServer() {
        NghiVanGianLan.datLai();
        int ghiDuoc = 0;
        // 200 nhan vat khac nhau, moi nguoi 20 su kien = 4000 luot xin ghi
        for (int nguoi = 0; nguoi < 200; nguoi++) {
            for (int i = 0; i < NghiVanGianLan.TOI_DA_MOI_NHAN_VAT; i++) {
                if (NghiVanGianLan.xinPhepGhi("nguoi" + nguoi, 0L) != NghiVanGianLan.KHONG_CHO) {
                    ghiDuoc++;
                }
            }
        }
        Kiem.dung("4000 luot bao dong tren 200 nhan vat: khong vuot tran server",
                ghiDuoc <= NghiVanGianLan.TOI_DA_TOAN_SERVER + 1);
        Kiem.dung("nhung van ghi duoc kha nhieu de con dieu tra",
                ghiDuoc >= NghiVanGianLan.TOI_DA_TOAN_SERVER / 2);
    }
}
