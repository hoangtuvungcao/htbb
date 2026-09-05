import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): chot lai HANH VI HIEN TAI cua
 * le thuong "Phi Phong Huyen Vu" (id 750, xem Item.isPhiPhongHuyenVu()). Phat hien
 * 05/09/2026 khi nguoi choi hoi "chua co huong dan nhap phi phong huyen vu nhi?" - doi
 * chieu code thi lo ra ba dieu de nham trong bang loaiThuong() (web/admin/inc/thuong.php):
 *
 * 1. Giftcode "PP"/"PP8X" (phi phong max bao kich / max 8x) CHI tu chon trong 15 id co
 * dinh {584..597,840} (xem Char.createPhiPhongMaxBaoKich, PP8X ep cung ve id 593) -
 * KHONG BAO GIO ra id 750. Duong DUY NHAT tang dung Phi Phong Huyen Vu la PPR/PPV/
 * PPRK/PPVK voi id=750 ghi ro trong chuoi.
 *
 * 2. Ten goi "PPV" ("vinh vien" - doan tu ten cu) gay hieu sai: PPR va PPV goi CUNG mot
 * ham Char.createtemCoat(), khac nhau DUY NHAT o co "ismax" (chi so max hay ngau nhien).
 * Vinh vien/co han do RIENG tham so thoiHan (tachTime[3]) quyet dinh, GIONG HET nhau o
 * ca hai loai - "PPR:...:0:..." cung vinh vien y het "PPV:...:0:...".
 *
 * 3. Tham so "khang" cua PPRK/PPVK (chon he khang) CHI co tac dung that voi id=750:
 * Item.createAttributeItemCoat() chi ghi otherAtt[59..63]=300 (dong khang) BEN TRONG
 * nhanh "if (this.getTemplate().id == 750)" - id phi phong nao khac thi tham so khang
 * bi doc nhung khong dong nao dung toi, im lang khong co gi xay ra.
 *
 * Khong sua code o day - day chi la chot lai hanh vi hien tai + kiem lai ghi chu moi
 * them trong web/admin/inc/thuong.php dung voi code that. Neu sau nay ai them id 750
 * vao bang PP/PP8X, hoac lam cho khang co tac dung voi id khac, test nay se do - dung
 * nhu thiet ke cua characterization test (nhac cap nhat lai huong dan trong thuong.php).
 */
public final class KiemGiftcodePhiPhongHuyenVu {

    private static final String F_ITEM = "src/real/Item.java";
    private static final String F_GIFTCODE = "src/data/DatabaseGiftCode.java";
    private static final String F_CHAR = "src/real/Char.java";
    private static final String F_THUONG = "web/admin/inc/thuong.php";

    public static void chay() {
        Kiem.nhom("Giftcode PPR/PPV/PPRK/PPVK - Phi Phong Huyen Vu (id 750)");

        final String item = doc(F_ITEM);
        final String gc = doc(F_GIFTCODE);
        final String ch = doc(F_CHAR);
        final String thuong = doc(F_THUONG);
        Kiem.dung("doc duoc " + F_ITEM, !item.isEmpty());
        Kiem.dung("doc duoc " + F_GIFTCODE, !gc.isEmpty());
        Kiem.dung("doc duoc " + F_CHAR, !ch.isEmpty());
        Kiem.dung("doc duoc " + F_THUONG, !thuong.isEmpty());
        if (item.isEmpty() || gc.isEmpty() || ch.isEmpty() || thuong.isEmpty()) {
            return;
        }

        // ---- 1. isPhiPhongHuyenVu() dung id 750. ----
        final int mocIsHuyenVu = item.indexOf("public boolean isPhiPhongHuyenVu()");
        Kiem.dung("tim thay isPhiPhongHuyenVu()", mocIsHuyenVu > 0);
        if (mocIsHuyenVu > 0) {
            final int mocXuongDong2 = item.indexOf('\n', item.indexOf('\n', mocIsHuyenVu) + 1);
            final String than = item.substring(mocIsHuyenVu, mocXuongDong2);
            Kiem.dung("isPhiPhongHuyenVu() so sanh id == 750", than.contains("id == 750"));
        }

        // ---- 2. PP/PP8X: bang id co dinh {584..597,840} KHONG chua 750, PP8X ep ve 593. ----
        final int mocPP = ch.indexOf("public static Item createPhiPhongMaxBaoKich(");
        Kiem.dung("tim thay createPhiPhongMaxBaoKich", mocPP > 0);
        if (mocPP > 0) {
            final int mocKet = ch.indexOf("public static void createNguyetLinhTruong(", mocPP);
            final String than = ch.substring(mocPP, mocKet > mocPP ? mocKet : ch.length());
            Kiem.sai("bang id co dinh {584..597,840} khong chua 750", than.contains("750"));
            Kiem.dung("is8x ep ve id 593 (khong phai 750)", than.contains("idtemp = 593"));
        }

        // ---- 3. createAttributeItemCoat: cong thuc chon khang CHI nam trong nhanh id==750. ----
        final int mocHam = item.indexOf("public void createAttributeItemCoat(");
        Kiem.dung("tim thay createAttributeItemCoat", mocHam > 0);
        if (mocHam > 0) {
            final int mocKetHam = item.indexOf("public Vector<String> getInfoLucky()", mocHam);
            final String than = item.substring(mocHam, mocKetHam > mocHam ? mocKetHam : item.length());
            final int moc750 = than.indexOf("this.getTemplate().id == 750");
            Kiem.dung("co nhanh rieng cho id == 750 trong createAttributeItemCoat", moc750 > 0);
            if (moc750 > 0) {
                final String truoc750 = than.substring(0, moc750);
                final String tu750 = than.substring(moc750);
                // Chu y: chu ky ham co tham so ten "khang" nen KHONG the kiem tra
                // truoc750.contains("khang") - se tu khop vao chinh khai bao tham so.
                // Phai kiem CONG THUC that (khang + 59) thay vi ten bien suong.
                Kiem.sai("truoc nhanh id==750, chua co cong thuc chon o theo khang (khang + 59)",
                        truoc750.contains("khang + 59"));
                Kiem.dung("trong nhanh id==750 co doc khang de chon o (khang == -1 va khang + 59)",
                        tu750.contains("khang == -1") && tu750.contains("khang + 59"));
            }
        }

        // ---- 4. PPR/PPV: cung goi createtemCoat, khac DUY NHAT o ismax; CUNG doc
        // tachTime[3] lam thoiHan (khong phai rieng PPV moi "vinh vien"). ----
        Kiem.dung("giftcode PPR goi createtemCoat voi ismax=false, tachTime[3]=thoiHan",
                gc.contains("player.createtemCoat(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), false, Integer.parseInt(tachTime[3]));"));
        Kiem.dung("giftcode PPV goi createtemCoat voi ismax=true, CUNG tachTime[3]=thoiHan",
                gc.contains("player.createtemCoat(Integer.parseInt(tachTime[1]), player, Integer.parseInt(tachTime[4]), true, Integer.parseInt(tachTime[3]));"));

        // ---- 5. thuong.php da ghi ro vi du id 750 + canh bao trong bang loaiThuong(). ----
        Kiem.dung("thuong.php: PPR co vi du id 750 (Huyen Vu)", thuong.contains("'PPR:750:1:0:0'"));
        Kiem.dung("thuong.php: PPV co vi du id 750 (Huyen Vu)", thuong.contains("'PPV:750:1:0:0'"));
        Kiem.dung("thuong.php: PPRK co vi du id 750 (Huyen Vu)", thuong.contains("'PPRK:750:1:0:0:2'"));
        Kiem.dung("thuong.php: PPVK co vi du id 750 (Huyen Vu)", thuong.contains("'PPVK:750:1:0:0:2'"));
        Kiem.dung("thuong.php: canh bao khang chi dung that voi id=750",
                thuong.contains("CHỈ có tác dụng thật với"));
        Kiem.dung("thuong.php: canh bao PP/PP8X khong bao gio ra Huyen Vu",
                thuong.contains("KHÔNG BAO GIỜ ra Phi Phong Huyền Vũ"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemGiftcodePhiPhongHuyenVu() {
    }
}
