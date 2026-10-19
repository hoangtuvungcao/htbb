package real;

import com.htbb.pokemon.ChiSoKinhTe;
import com.htbb.pokemon.LauDai;
import data.DatabaseLauDai;
import java.util.Date;

/**
 * Viec dinh ky cua thiet ke dai han, goi moi giay tu vong BaoTriDaily ({@link #nhip}). Moi
 * viec chay tren luong rieng de khong chan vong bao tri. Tat co: khong lam gi.
 *
 *   - moi gio (phut 7): tinh lai cap trung vi (bat kip, GD2 3.6);
 *   - moi ngay tu 04:00 (lan dau trong ngay): ghi tob_kinh_te_ngay (GD2 0.4) va hen lan ke
 *     tiep cua cac su kien trong lich 52 tuan (GD2 5.2).
 * Chot bang "da chay hom nay / gio nay" chu khong bang giay == 0: vong 1 giay co the truot giay.
 */
public final class LauDaiJob {

    private static volatile int gioDaChay = -1;
    private static volatile int ngayDaChay = -1;

    private LauDaiJob() {
    }

    private static volatile int bossDaChay = -1;

    public static void nhip(final Date d) {
        if (!LauDai.bat()) {
            return;
        }
        try {
            TrangThaiGame.nhip();
            if (d.getSeconds() % 3 == 0) {
                VatPhamDungLauDai.nhip(); // do giu hoi HP, qua mong tu dung
            }
            if (d.getSeconds() % 5 == 0) {
                BossLauDai.nhip5Giay();
                BossLauDai.donGymHetGio();
            }
        } catch (final Exception e) {
            System.out.println("LauDaiJob: " + e);
        }
        // Boss the gioi hang tuan: thu Bay 20:00; tuan "dot kich" (SuKien moi:dotkich) them 12:30 moi ngay.
        final int phut = d.getHours() * 60 + d.getMinutes();
        final boolean thuBay = d.getDay() == 6 && phut == 20 * 60;
        final boolean dotKich = SuKien.bat("moi:dotkich") && (phut == 12 * 60 + 30 || phut == 20 * 60 + 30);
        final int khoa = d.getDate() * 10000 + phut;
        if ((thuBay || dotKich) && bossDaChay != khoa) {
            bossDaChay = khoa;
            chay("LauDai-boss", new Runnable() {
                public void run() {
                    BossLauDai.trieuHoiTheGioi();
                }
            });
        }
        final int gio = d.getDate() * 100 + d.getHours();
        if (d.getMinutes() >= 7 && gioDaChay != gio) {
            gioDaChay = gio;
            chay("LauDai-trungvi", new Runnable() {
                public void run() {
                    LauDaiNhanVat.capNhatTrungVi();
                }
            });
        }
        final int ngay = (d.getYear() + 1900) * 10000 + (d.getMonth() + 1) * 100 + d.getDate();
        if (d.getHours() >= 4 && ngayDaChay != ngay) {
            ngayDaChay = ngay;
            chay("LauDai-dem", new Runnable() {
                public void run() {
                    final ChiSoKinhTe c = DatabaseLauDai.ghiNgay();
                    if (c != null) {
                        System.out.println("[LAU DAI] kinh te " + ngay + ": " + c.soNguoi + " nguoi, cap trung vi "
                                + c.capTrungVi + ", cung tien thuc " + (c.cungTienThucX100 / 100.0) + " gio, muc " + c.muc);
                    }
                    // Lich su kien: nay do LichSuKien.dongBoLichNam (lich nam theo ngay that) lo, khong hen theo tuan ISO nua.
                }
            });
        }
    }

    private static void chay(final String ten, final Runnable r) {
        final Thread t = new Thread(r, ten);
        t.setDaemon(true);
        t.start();
    }
}
