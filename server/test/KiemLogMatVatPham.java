/**
 * Kiem thu phan ghi log "mat vat pham".
 *
 * VI SAO CAN: cac cau log KHONG dung PreparedStatement — chung duoc GHEP CHUOI roi day
 * vao Logdata.query, va Logdata chay CA LO mot luot. Mot dau nhay don lot vao la hong
 * ca lo, tuc mat luon nhung dong log khac cung nhip. Ma ten vat pham lay tu bang
 * data_item do admin nhap tay nen khong the tin la sach.
 *
 * Dau "\" con nguy hon dau nhay: trong MySQL no la ky tu thoat, nen mot ten ket thuc
 * bang "\" se NUOT dau nhay dong chuoi va lam lech toan bo phan con lai cau lenh.
 */
public final class KiemLogMatVatPham {

    public static void chay() {
        Kiem.nhom("Database.lamSachChoLog - lam sach truoc khi ghep cau log");

        Kiem.bang("null -> chuoi rong", "", data.Database.lamSachChoLog(null));
        Kiem.bang("chuoi thuong giu nguyen", "Kiem Sat Than",
                data.Database.lamSachChoLog("Kiem Sat Than"));

        Kiem.bang("bo dau nhay don", "Kiem  Than", data.Database.lamSachChoLog("Kiem 'Than"));
        Kiem.bang("bo dau gach nguoc", "Kiem  Than", data.Database.lamSachChoLog("Kiem \\Than"));
        Kiem.bang("bo xuong dong", "Kiem  Than", data.Database.lamSachChoLog("Kiem \nThan"));
        Kiem.bang("bo ca \\r", "Kiem  Than", data.Database.lamSachChoLog("Kiem \rThan"));

        // Ca hiem nhung chet nguoi: "\'" — bo dau nhay ma khong bo dau gach nguoc thi
        // van con "\" o cuoi, va no se nuot dau nhay dong chuoi cua cau INSERT.
        Kiem.sai("khong con dau gach nguoc nao sot lai",
                data.Database.lamSachChoLog("abc\\'def").contains("\\"));
        Kiem.sai("va cung khong con dau nhay",
                data.Database.lamSachChoLog("abc\\'def").contains("'"));

        // Chuoi tan cong dien hinh: dong cau lenh roi noi them lenh khac.
        final String doc = "x','y',Now(),'z'); DROP TABLE tob_char; -- ";
        final String sach = data.Database.lamSachChoLog(doc);
        Kiem.sai("chuoi tan cong khong con dau nhay de thoat ra", sach.contains("'"));
        Kiem.dung("nhung van doc duoc de con dieu tra", sach.contains("DROP TABLE"));

        Kiem.bang("do dai khong doi (chi thay ky tu, khong xoa)",
                Integer.valueOf(doc.length()), Integer.valueOf(sach.length()));

        Kiem.nhom("Cau log mat vat pham - dung cho ghi log");

        // Ghep dung nhu ham that lam, roi kiem cau ket qua.
        final String ten = data.Database.lamSachChoLog("Kiem 'Rong\\ Than");
        final String cau = "insert into tob_log_all_item (idchar,charname,info,timelog,aclog) values ("
                + 123 + ",'" + ten + "','" + data.Database.lamSachChoLog("nghien do | abc") + "',Now(),'matdo')";

        // Dung 6: hai cho charname, hai cho info, hai cho 'matdo'. Le mot cai la chuoi
        // nao do da thoat ra ngoai va cau lenh hong.
        Kiem.bang("cau chi co dung 6 dau nhay don (khong co cai nao thoat ra)",
                Integer.valueOf(6), Integer.valueOf(demKyTu(cau, '\'')));
        Kiem.dung("cau ket thuc dung", cau.endsWith("'matdo')"));
        Kiem.dung("co cot idchar de tra theo tung nhan vat", cau.contains("idchar"));
        Kiem.dung("dung chung aclog 'matdo' de tim mot lan ra het",
                cau.contains("'matdo'"));

        Kiem.nhom("Cac diem ghi log da duoc noi vao chua");

        // Loi nay tung xay ra that: viet ham ghi log roi quen goi. Doc thang ma nguon.
        final String[][] can = {
            {"src/real/MapXuLyLenh.java", "nghien do tu dong"},
            {"src/real/MapXuLyLenh.java", "ban tu dong"},
            {"src/real/CharNapDuLieu.java", "server xoa luc nap nhan vat"},
            {"src/server/Panel/ButtonEditor.java", "admin xoa tay"},
        };
        for (final String[] c : can) {
            final String ma = doc(c[0]);
            Kiem.dung(c[0] + " co goi ghi log: \"" + c[1] + "\"",
                    ma != null && ma.contains("saveLogMatVatPham") && ma.contains(c[1]));
        }

        // deleteItem() KHONG phai cho dat log: than ham rong, moi loi goi deu vo nghia.
        final String db = doc("src/data/Database.java");
        Kiem.dung("van ghi nho deleteItem la ham rong (khong dat log o do)",
                db != null && db.contains("than ham do RONG"));
    }

    /** Doc ca file ma nguon; tra null neu khong doc duoc (chay ngoai thu muc goc). */
    private static String doc(final String duongDan) {
        java.io.Reader r = null;
        try {
            final java.io.File f = new java.io.File(duongDan);
            if (!f.isFile()) {
                return null;
            }
            r = new java.io.InputStreamReader(new java.io.FileInputStream(f), "UTF-8");
            final StringBuilder sb = new StringBuilder();
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
            return sb.toString();
        } catch (final Exception e) {
            return null;
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
    }

    private static int demKyTu(final String s, final char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    private KiemLogMatVatPham() {
    }
}
