import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Sua loi: bam "Tinh Luyện Thần Binh" o menu NPC -8 nhay THANG vao o chon
 * nguyen lieu, khong cho xem vu khi than binh dang mac (chi so tinh luyen
 * hien tai) truoc. Fix them mot buoc trung gian: menu gui shop chi co 1 item
 * (vu khi dang mac, type 7) de nguoi choi xem, bam "Tiep tuc" moi mo danh
 * sach nguyen lieu that (Map.doBuyItemShopNew, nhanh type == 7 goi
 * showTinhLuyenMaterialList()).
 *
 * Nhan tien sua mot cho LECH du lieu phat hien trong luc sua: nhanh type == 6
 * (dung de dung popup xac nhan "Ban co muon dung X de tinh luyen Y") tu loc
 * nguyen lieu rieng bang mang validMaterialIDs cung, KHONG co truong hop moc
 * 99% (locNguyenLieuTinhLuyen doi nguyen lieu phai CAO HON trang bi), va hien
 * gia SAI (500 luong, khong tru xu) so voi luc THUC THI that (idpopup 124:
 * 1 luong + 3 trieu xu) - khien shopIndex nguoi choi gui ve co the tro sang
 * MON KHAC trong danh sach dung de thuc thi. Da doi sang dung chung
 * locNguyenLieuTinhLuyen() va cung gia voi luc thuc thi.
 *
 * RealController/Map qua nang de dung truc tiep trong test (can Char/Map
 * that voi CSDL) nen quet van ban nguon de xac nhan cau truc sua dung, giong
 * cach cac Kiem* khac lam.
 */
public final class KiemTinhLuyenThanBinh {

    private static final String TEP_REAL_CONTROLLER = "src/real/RealController.java";
    private static final String TEP_MAP = "src/real/Map.java";

    private KiemTinhLuyenThanBinh() {
    }

    public static void chay() {
        Kiem.nhom("Tinh Luyện Thần Binh - phai xem vu khi truoc khi chon nguyen lieu");

        kiemRealController();
        kiemMap();
        kiemCongDonKhiDat100();
    }

    /**
     * Chot pham vi cua phep NHAN chi so luc tinh luyen cham 100% (Map, idpopup 124,
     * nhanh "TinhLuyen == 100", vu khi 758..772).
     *
     * LUAT: he so nhan la "Chi so vu khi tang" = otherAtt[55] cua chinh cay than binh,
     * don vi PHAN NGHIN (501 -> x1,501). No CHI duoc nhan len mang atb[]; ba chi so
     * otherAtt[54]/[55]/[56] phai GIU NGUYEN.
     *
     * LOI DA XAY RA THAT (05/09/2026, nguoi choi bao "sau khi tinh luyen lai tang hon
     * nhieu" va tuong server dang nhan cung 1,5): commit 2480c47 doi khoi khoi phuc
     * otherAtt[54/55/56] len TRUOC ba dong cong don, khien ba dong do - von la code
     * chet o ban goc bb83665 vi bi ghi de ngay sau - bat dau chay that. Hau qua nang
     * nhat nam o [55]: no khong phai chi so thuong ma la CHINH HE SO NHAN, duoc ap len
     * vu khi that dang mac (Item.getValueUpByVuKhiThoiTrang, getInfoAtbItemWearing).
     * Cho no tu nhan chinh no la an hai lan: 50,1% -> 75,2%.
     *
     * So do do nguoi choi gui ve khop tuyet doi voi cong thuc (chia so nguyen, cat cut):
     *     [54] 241 + 241*501/1000 = 241 + 120 = 361  (24,1% -> 36,1%)
     *     [55] 501 + 501*501/1000 = 501 + 251 = 752  (50,1% -> 75,2%)
     *     [56] 290 + 290*501/1000 = 290 + 145 = 435  (29,0% -> 43,5%)
     */
    private static void kiemCongDonKhiDat100() {
        final String noiDung = doc(TEP_MAP);
        if (noiDung.isEmpty()) {
            return;
        }

        // Gioi han vung quet vao dung nhanh dat 100% cua vu khi than binh, khong quet
        // ca tep (chuoi "att55" chi xuat hien o day nhung cu chan cho chac).
        final int dauKhoi = noiDung.indexOf("if (oldId >= 758 && oldId <= 772) {");
        Kiem.dung("tim thay nhanh xu ly dat 100% (oldId 758..772)", dauKhoi >= 0);
        if (dauKhoi < 0) {
            return;
        }
        final int cuoiKhoi = noiDung.indexOf("// Cập nhật allAtb", dauKhoi);
        Kiem.dung("tim thay diem ket thuc khoi cong don", cuoiKhoi > dauKhoi);
        if (cuoiKhoi <= dauKhoi) {
            return;
        }
        final String khoi = noiDung.substring(dauKhoi, cuoiKhoi);

        // Ba mang chi so PHAI duoc nhan, va deu phai di qua ham co chan tran.
        Kiem.dung("nhan atb[] qua ham chan tran (tran short)",
                khoi.contains("atb[k] = (short) nhanTheoChiSoVuKhiTang(")
                && khoi.contains("att55, Short.MAX_VALUE)"));
        Kiem.dung("nhan newAtb[] qua ham chan tran (tran byte)",
                khoi.contains("newAtb[k] = (byte) nhanTheoChiSoVuKhiTang("));
        Kiem.dung("nhan lockAtb[] qua ham chan tran (tran byte)",
                khoi.contains("lockAtb[k] = (byte) nhanTheoChiSoVuKhiTang("));
        Kiem.dung("hai mang byte deu chan tran o Byte.MAX_VALUE",
                khoi.contains("att55, Byte.MAX_VALUE)"));

        // KHONG duoc nhan thang kieu cu: byte[] quay vong am ma khong nem loi.
        Kiem.sai("KHONG con nhan thang atb[] (bo qua chan tran)",
                khoi.contains("atb[k] +=  player.itemVukhiThoiTrang.atb[k] * att55 / 1000"));

        // Cap ky nang KHONG phai chi so -> khong nhan.
        Kiem.sai("KHONG nhan addMoreLevelSkill[] (la cap ky nang, vd \"Hộ độc tiễn: 1\")",
                khoi.contains("addMoreLevelSkill[k] = (byte) nhanTheoChiSoVuKhiTang("));
        // Do ben khong phai chi so -> copy thang tu nguyen lieu, khong nhan.
        Kiem.dung("durable copy thang tu nguyen lieu",
                noiDung.indexOf("itemVukhiThoiTrang.durable = selectedItem.durable;", dauKhoi) > dauKhoi);
        Kiem.sai("KHONG nhan durable theo att55",
                khoi.contains("durable = (short) nhanTheoChiSoVuKhiTang(")
                || khoi.contains("durable * att55"));

        // Ham nhan phai chan tran THAT (khong chi doi ten roi van tran).
        final int dauHam = noiDung.indexOf("private static int nhanTheoChiSoVuKhiTang(");
        Kiem.dung("tim thay ham nhanTheoChiSoVuKhiTang", dauHam >= 0);
        if (dauHam >= 0) {
            final String thanHam = noiDung.substring(dauHam,
                    Math.min(noiDung.length(), dauHam + 400));
            Kiem.dung("ham tinh bang long (khong tran giua chung)",
                    thanHam.contains("(long) giaTri * att55 / 1000L"));
            Kiem.dung("ham cat ve tran khi vuot",
                    thanHam.contains("(ketQua > tran) ? tran : (int) ketQua"));
            Kiem.dung("ham tra ve nguyen ven khi <= 0",
                    thanHam.contains("if (giaTri <= 0)"));
        }

        // Ba chi so dac biet phai duoc khoi phuc NGUYEN VEN tu ban luu.
        Kiem.dung("khoi phuc nguyen ven otherAtt[54] = att54",
                khoi.contains("otherAtt[54] = att54;"));
        Kiem.dung("khoi phuc nguyen ven otherAtt[55] = att55",
                khoi.contains("otherAtt[55] = att55;"));
        Kiem.dung("khoi phuc nguyen ven otherAtt[56] = att56",
                khoi.contains("otherAtt[56] = att56;"));

        // ...va KHONG duoc nhan them lan nao nua.
        Kiem.sai("KHONG cong don otherAtt[54] (Sát thương chuẩn)",
                khoi.contains("other[54] += other[54] * att55 / 1000"));
        Kiem.sai("KHONG cong don otherAtt[55] (Chỉ số vũ khí tăng - chinh la he so nhan, an hai lan)",
                khoi.contains("other[55] += other[55] * att55 / 1000"));
        Kiem.sai("KHONG cong don otherAtt[56] (Tỷ lệ gây sát thương chuẩn)",
                khoi.contains("other[56] += other[56] * att55 / 1000"));
        Kiem.sai("KHONG con bien tam short[] other tro vao otherAtt de cong don",
                khoi.contains("short[] other = player.itemVukhiThoiTrang.otherAtt"));

        kiemSoHocChanTran();
    }

    /**
     * Kiem SO HOC that cua phep nhan, khong chi doc van ban nguon. Chep dung cong thuc
     * o Map.nhanTheoChiSoVuKhiTang (khong goi truc tiep duoc: Map co khoi static noi CSDL).
     * Neu ai sua cong thuc ben Map ma khong sua o day thi cac phep quet van ban ben tren
     * se do truoc, nen hai lop bo tro cho nhau.
     */
    private static void kiemSoHocChanTran() {
        final int att55 = 501; // = 50,1%, dung con so that cua nguoi choi bao loi

        // So lieu that trong anh nguoi choi gui (05/09/2026).
        Kiem.bang("atb Tấn công 1798 -> 2698", Integer.valueOf(2698),
                Integer.valueOf(nhan(1798, att55, Short.MAX_VALUE)));
        Kiem.bang("newAtb Tăng HP 9 (hien 9000) -> 13 (hien 13000)", Integer.valueOf(13),
                Integer.valueOf(nhan(9, att55, Byte.MAX_VALUE)));
        Kiem.bang("newAtb Cộng khéo léo 4 -> 6", Integer.valueOf(6),
                Integer.valueOf(nhan(4, att55, Byte.MAX_VALUE)));
        Kiem.bang("lockAtb Tăng công 21 -> 31", Integer.valueOf(31),
                Integer.valueOf(nhan(21, att55, Byte.MAX_VALUE)));

        // Chan tran byte: day chinh la cho quay vong am neu khong chan.
        Kiem.bang("lockAtb Tăng công 90 -> chan o 127 (khong quay vong)", Integer.valueOf(127),
                Integer.valueOf(nhan(90, att55, Byte.MAX_VALUE)));
        Kiem.bang("byte 127 da toi tran -> giu 127", Integer.valueOf(127),
                Integer.valueOf(nhan(127, att55, Byte.MAX_VALUE)));
        // Chung minh moi nguy: neu ep byte KHONG chan tran thi 90 hoa thanh so AM.
        Kiem.dung("neu khong chan tran thi 90 ep byte ra so am (ly do phai chan)",
                (byte) (90 + 90 * att55 / 1000) < 0);

        // Chan tran short cho atb.
        Kiem.bang("atb 30000 -> chan o 32767", Integer.valueOf(32767),
                Integer.valueOf(nhan(30000, att55, Short.MAX_VALUE)));

        // Gia tri <= 0 giu nguyen.
        Kiem.bang("0 giu nguyen 0", Integer.valueOf(0),
                Integer.valueOf(nhan(0, att55, Byte.MAX_VALUE)));
        Kiem.bang("so am giu nguyen (khong nhan cho am them)", Integer.valueOf(-5),
                Integer.valueOf(nhan(-5, att55, Byte.MAX_VALUE)));

        // Cat cut phan le, giong cach tinh cu cua atb[].
        Kiem.bang("1 -> 1 (0,501 bi cat cut)", Integer.valueOf(1),
                Integer.valueOf(nhan(1, att55, Byte.MAX_VALUE)));
        Kiem.bang("3 -> 4 (1,503 cat cut con 1)", Integer.valueOf(4),
                Integer.valueOf(nhan(3, att55, Byte.MAX_VALUE)));

        // att55 = 0 (than binh khong co chi so nay) -> khong doi gi.
        Kiem.bang("att55 = 0 thi giu nguyen", Integer.valueOf(1798),
                Integer.valueOf(nhan(1798, 0, Short.MAX_VALUE)));
    }

    /** Bản sao công thức của Map.nhanTheoChiSoVuKhiTang. */
    private static int nhan(final int giaTri, final int att55, final int tran) {
        if (giaTri <= 0) {
            return giaTri;
        }
        final long ketQua = giaTri + (long) giaTri * att55 / 1000L;
        return (ketQua > tran) ? tran : (int) ketQua;
    }

    private static void kiemRealController() {
        final String noiDung = doc(TEP_REAL_CONTROLLER);
        Kiem.dung("doc duoc " + TEP_REAL_CONTROLLER, !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        final int dauCase8 = noiDung.indexOf("case 8:");
        final int dauCase9 = noiDung.indexOf("case 9:");
        Kiem.dung("tim thay case 8 (Tinh Luyện Thần Binh) va case 9 sau do", dauCase8 >= 0 && dauCase9 > dauCase8);
        if (dauCase8 < 0 || dauCase9 <= dauCase8) {
            return;
        }
        final String than = noiDung.substring(dauCase8, dauCase9);

        Kiem.dung("van giu kiem tra CheckNguyenLieuTL truoc khi hien gi ca",
                than.contains("player.CheckNguyenLieuTL()"));
        Kiem.dung("gui shop 'Vũ Khí Thần Binh' voi type 7 (buoc xem truoc)",
                than.contains("\"Vũ Khí Thần Binh\", 7"));
        Kiem.dung("shop nay chi chua DUNG vu khi dang mac (player.itemVukhiThoiTrang), khong phai danh sach nguyen lieu",
                than.contains("vuKhiDangMac.addElement(player.itemVukhiThoiTrang)"));
        Kiem.dung("KHONG con tu xay danh sach nguyen lieu rieng o day (da chuyen sang Map, nhanh type == 7)",
                !than.contains("validMaterialIDs"));
        Kiem.dung("KHONG con goi thang shop 'Nguyên liệu' voi type 6 tu day nua",
                !than.contains("\"Chọn\", \"Nguyên liệu\", 6"));
    }

    private static void kiemMap() {
        final String noiDung = doc(TEP_MAP);
        Kiem.dung("doc duoc " + TEP_MAP, !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        final int dauType6 = noiDung.indexOf("else if (type == 6)");
        final int dauType7 = noiDung.indexOf("else if (type == 7)");
        final int dauType10000 = noiDung.indexOf("else if (type == -10000)");
        Kiem.dung("tim thay nhanh type == 6, type == 7 (moi), va type == -10000 sau do, dung thu tu",
                dauType6 >= 0 && dauType7 > dauType6 && dauType10000 > dauType7);
        if (dauType6 < 0 || dauType7 <= dauType6 || dauType10000 <= dauType7) {
            return;
        }

        final String thanType6 = noiDung.substring(dauType6, dauType7);
        Kiem.dung("nhanh type == 6 dung chung locNguyenLieuTinhLuyen() thay vi tu loc rieng",
                thanType6.contains("locNguyenLieuTinhLuyen(player)"));
        Kiem.dung("nhanh type == 6 KHONG con tu khai bao mang validMaterialIDs rieng (nguon lech voi luc thuc thi)",
                !thanType6.contains("int[] validMaterialIDs ="));
        Kiem.dung("nhanh type == 6 kiem tra dung 1 luong (khop luc thuc thi idpopup 124)",
                thanType6.contains("getLuong() < 1"));
        Kiem.dung("nhanh type == 6 co kiem tra 3.000.000 xu (truoc day thieu hoan toan)",
                thanType6.contains("getxu() < 3000000"));

        final String thanType7 = noiDung.substring(dauType7, dauType10000);
        Kiem.dung("nhanh type == 7 (buoc xem vu khi) mo tiep danh sach nguyen lieu that",
                thanType7.contains("showTinhLuyenMaterialList(player)"));
        Kiem.dung("nhanh type == 7 kiem tra lai checkVuKhiThanBinh() (phong trang bi da doi giua luc mo 2 shop)",
                thanType7.contains("player.checkVuKhiThanBinh()"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
