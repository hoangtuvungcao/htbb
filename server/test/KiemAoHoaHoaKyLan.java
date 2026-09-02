import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * "ẢO HÓA HỎA KỲ LÂN" - them ngay 02/09/2026 theo yeu cau nguoi dung.
 *
 * BOI CANH. Hoa Ky Lan (Char.itemHoaKyLan, item template 681/682/683) mang 7 chi so:
 * bon khang lan (otherAtt 24/25/26/31, ap dung trong CharChiSo) va ba chi so cua Than
 * Thu di kem - sat thuong % tan cong chu (otherAtt 38), sat thuong % maxhp doi thu
 * (otherAtt 40, chi cap 3), suc khoe Than Thu (otherAtt 39). BA chi so sau TUNG duoc
 * ap dung qua CAC NHANH RIENG THEO CAP (if getIdCharThanThu()==1/==2 ...) voi so
 * hardcode trung khop otherAtt, chu KHONG DOC TRUC TIEP otherAtt - nen khong the
 * "+10%" len duoc neu khong refactor truoc. Cum sua nay lam hai viec:
 *
 *   1. Doc THANG tu otherAtt cua item dang mac (qua Char.chiSoPhanTramHoaKyLan /
 *      chiSoTuyetDoiHoaKyLan) thay vi nhanh rieng theo cap - tu dong dung voi moi cap
 *      hien co, khong can sua them neu sau nay them cap 4+.
 *   2. Them "Ảo hóa": Hoa Ky Lan VINH VIEN + 100 luong -> +10 (diem phan tram, voi 6
 *      chi so kieu %) hoac +10% (voi suc khoe, kieu tuyet doi) trong 7 ngay, roi tu
 *      dong tro ve chi so goc, khong can dang nhap lai.
 *
 * Nhu cac test khac trong cum nay, day la kiem tren VAN BAN MA NGUON: cac ham lien
 * quan can CSDL/mot Char song moi chay duoc, khong dung trong bo kiem thu doc lap nay.
 */
public final class KiemAoHoaHoaKyLan {

    public static void chay() {
        Kiem.nhom("Ao hoa Hoa Ky Lan");

        hangSoGiaVaHan();
        chiSoPhanTramCongThang10();
        chiSoTuyetDoiCong10PhanTram();
        doAoHoaKiemVinhVienVaGia();
        doTrangBiDungChungChoBaCho();
        thuTuGanItemHoaKyLanTruocKhiTaoThanThu();
        charChiSoDocQuaHelper();
        charThanthuKhongConHardcodeTheoCap();
        khongConHieuUngHienSaiSo5PhanTram();
        sucKhoeThanThuDocTuHelper();
        luuVaNapLaiTimeAoHoa();
        kiemTraHetHanTrongVongLapDinhKy();
        menuLuonCoBaLuaChon();
        xuLyMenuBaLuaChonDungIdOptionMenu();
    }

    private static void hangSoGiaVaHan() {
        final String s = doc("src/real/Char.java");
        Kiem.dung("gia 100 luong", s.contains("public static final int GIA_LUONG_AO_HOA_HOA_KY_LAN = 100;"));
        Kiem.dung("han 7 ngay tinh bang ms", s.contains("public static final long HAN_AO_HOA_HOA_KY_LAN_MS = 7L * 24 * 60 * 60 * 1000L;"));
    }

    /**
     * "kháng độc lan 5% > 15%" trong yeu cau goc la CONG THANG 10, khong phai nhan 1.1
     * (5*1.1=5.5, khong phai 15). Chot nay la chot de sai nhat trong ca tinh nang.
     */
    private static void chiSoPhanTramCongThang10() {
        final String s = doc("src/real/Char.java");
        final int vt = s.indexOf("public int chiSoPhanTramHoaKyLan(final int idxOtherAtt) {");
        Kiem.dung("tim thay chiSoPhanTramHoaKyLan", vt > 0);
        final String than = s.substring(vt, Math.min(s.length(), vt + 300));
        Kiem.dung("cong THANG 10, khong nhan 1.1", than.contains("+ (this.dangAoHoaHoaKyLan() ? 10 : 0)"));
        Kiem.sai("khong nhan 110/100 hay 1.1 o day", than.contains("110") || than.contains("1.1"));
        Kiem.dung("tra ve 0 khi khong mac Hoa Ky Lan nao", than.contains("if (this.itemHoaKyLan == null) {\n            return 0;\n        }"));
    }

    /** "sức khỏe 1000 > 1100" la +10% (tuong doi), khac han quy tac cong thang cua cac chi so %. */
    private static void chiSoTuyetDoiCong10PhanTram() {
        final String s = doc("src/real/Char.java");
        final int vt = s.indexOf("public int chiSoTuyetDoiHoaKyLan(final int idxOtherAtt) {");
        Kiem.dung("tim thay chiSoTuyetDoiHoaKyLan", vt > 0);
        final String than = s.substring(vt, Math.min(s.length(), vt + 300));
        Kiem.dung("cong 10% (co + co/10), khong phai cong thang 10", than.contains("co + co / 10"));
    }

    /** doAoHoaHoaKyLan: chi Hoa Ky Lan VINH VIEN, va phai du 100 luong. */
    private static void doAoHoaKiemVinhVienVaGia() {
        final String s = doc("src/real/Char.java");
        final int vt = s.indexOf("public void doAoHoaHoaKyLan(final Item item) {");
        Kiem.dung("tim thay doAoHoaHoaKyLan", vt > 0);
        final String than = s.substring(vt, Math.min(s.length(), vt + 1500));
        Kiem.dung("tu choi item co thoi han (minuteExist > 0)", than.contains("if (item.minuteExist > 0) {"));
        Kiem.dung("kiem du GIA_LUONG_AO_HOA_HOA_KY_LAN luong", than.contains("this.getLuong() < GIA_LUONG_AO_HOA_HOA_KY_LAN"));
        Kiem.dung("tru dung GIA_LUONG_AO_HOA_HOA_KY_LAN luong", than.contains("this.subLuong((long) GIA_LUONG_AO_HOA_HOA_KY_LAN);"));
        Kiem.dung("dat moc het han = now + HAN_AO_HOA_HOA_KY_LAN_MS", than.contains("this.timeAoHoaHoaKyLan = System.currentTimeMillis() + HAN_AO_HOA_HOA_KY_LAN_MS;"));
        Kiem.dung("ghi log aohoahkl", than.contains("\"aohoahkl\""));
        // Thu tu: kiem vinh vien va du luong PHAI truoc khi tru tien / mac len nguoi.
        final int viTriKiemThoiHan = than.indexOf("if (item.minuteExist > 0)");
        final int viTriTruTien = than.indexOf("this.subLuong(");
        Kiem.dung("kiem thoi han truoc khi tru tien", viTriKiemThoiHan >= 0 && viTriTruTien > viTriKiemThoiHan);

        // Chot de sai: PHAI dat timeAoHoaHoaKyLan TRUOC khi goi doTrangBiHoaKyLan. Neu
        // dao nguoc, doCreateCharThanThu (goi ben trong doTrangBiHoaKyLan) doc
        // chiSoTuyetDoiHoaKyLan(39) luc timeAoHoaHoaKyLan CHUA duoc cap nhat, nen Than
        // Thu vua tao THIEU +10% suc khoe cho toi lan mac lai ke tiep.
        final int viTriDatMoc = than.indexOf("this.timeAoHoaHoaKyLan = System.currentTimeMillis()");
        final int viTriGoiTrangBi = than.indexOf("this.doTrangBiHoaKyLan(item);");
        Kiem.dung("dat moc het han TRUOC khi goi doTrangBiHoaKyLan (de Than Thu vua tao nhan dung suc khoe da ao hoa)",
                viTriDatMoc >= 0 && viTriGoiTrangBi > viTriDatMoc);
    }

    /** doTrangBiHoaKyLan phai la noi DUY NHAT lam viec "mac len nguoi", dung chung cho Su dung va Ao hoa. */
    private static void doTrangBiDungChungChoBaCho() {
        final String charJava = doc("src/real/Char.java");
        Kiem.dung("co ham doTrangBiHoaKyLan", charJava.contains("public void doTrangBiHoaKyLan(final Item item) {"));
        Kiem.dung("doAoHoaHoaKyLan goi doTrangBiHoaKyLan khi item chua duoc mac",
                charJava.contains("if (this.itemHoaKyLan != item) {\n            this.doTrangBiHoaKyLan(item);\n        } else {"));

        final String mapDoMenu = doc("src/real/MapDoMenu.java");
        Kiem.dung("MapDoMenu (nut Su dung) goi doTrangBiHoaKyLan thay vi tu lam lai",
                mapDoMenu.contains("player.doTrangBiHoaKyLan(player.itemTam);"));
        Kiem.sai("MapDoMenu khong con tu goi charThanThuDissapear/doCreateCharThanThu lap lai",
                mapDoMenu.contains("player.charThanThuDissapear();\n                            player.iItems.remove(player.itemTam);"));

        final String mapJava = doc("src/real/Map.java");
        Kiem.sai("Map.java (doUseItem) khong con nhanh tu trang bi rieng nua",
                mapJava.contains("player.doCreateCharThanThu(item.getTemplate().atb[0]);"));
    }

    /**
     * doCreateCharThanThu doc chiSoTuyetDoiHoaKyLan(39) qua itemHoaKyLan cua CHINH
     * minh, nen itemHoaKyLan PHAI duoc gan xong TRUOC khi goi ham nay. Chi con DUY
     * NHAT mot noi goi doCreateCharThanThu (ben trong doTrangBiHoaKyLan) - neu co noi
     * khac goi lai truc tiep se lay nham suc khoe cua item CU (hoac 0 neu chua tung
     * mac gi).
     */
    private static void thuTuGanItemHoaKyLanTruocKhiTaoThanThu() {
        final String s = doc("src/real/Char.java");
        final int soLanGoi = dem(s, "this.doCreateCharThanThu(item.getTemplate().atb[0]);");
        Kiem.bang("doCreateCharThanThu(...) chi duoc goi DUNG MOT lan trong toan bo Char.java",
                Integer.valueOf(1), Integer.valueOf(soLanGoi));
        final int viTriGanItem = s.indexOf("this.itemHoaKyLan = item;");
        final int viTriGoiTao = s.indexOf("this.doCreateCharThanThu(item.getTemplate().atb[0]);");
        Kiem.dung("gan itemHoaKyLan = item TRUOC khi goi doCreateCharThanThu",
                viTriGanItem >= 0 && viTriGoiTao > viTriGanItem);
    }

    /** CharChiSo phai doc qua helper (co cong ao hoa) thay vi doc thang otherAtt. */
    private static void charChiSoDocQuaHelper() {
        final String s = doc("src/real/CharChiSo.java");
        Kiem.dung("khangbanglan qua chiSoPhanTramHoaKyLan(24)", s.contains("self.khangbanglan += self.chiSoPhanTramHoaKyLan(24);"));
        Kiem.dung("khangsetlan qua chiSoPhanTramHoaKyLan(25)", s.contains("self.khangsetlan += self.chiSoPhanTramHoaKyLan(25);"));
        Kiem.dung("khangdoclan qua chiSoPhanTramHoaKyLan(26)", s.contains("self.khangdoclan += self.chiSoPhanTramHoaKyLan(26);"));
        Kiem.dung("khanglualan qua chiSoPhanTramHoaKyLan(31)", s.contains("self.khanglualan += self.chiSoPhanTramHoaKyLan(31);"));
        Kiem.sai("khong con doc thang otherAtt[24] o day nua", s.contains("self.khangbanglan += self.itemHoaKyLan.otherAtt[24];"));
    }

    /**
     * data/CharThanthu.java KHONG con ba nhanh rieng theo cap (getIdCharThanThu()==1/==2)
     * cho sat thuong - phai doc thang tu Char.chiSoPhanTramHoaKyLan(38/40) de "ao hoa"
     * ap dung duoc ma khong can sua rieng tung cap.
     */
    private static void charThanthuKhongConHardcodeTheoCap() {
        final String s = doc("src/data/CharThanthu.java");
        // Chi kiem CAC NHANH TINH SAT THUONG cu - "getIdCharThanThu() == 1/2" van con o
        // doBuffOwner (chon loai khien EFF_SHIELD_1/2 theo cap, khong lien quan sat
        // thuong) nen KHONG cam blanket ca chuoi do trong toan file.
        Kiem.sai("khong con return attack*20/100 hardcode", s.contains("return this.owner.attack * 20 / 100;"));
        Kiem.sai("khong con return attack*30/100 hardcode", s.contains("return this.owner.attack * 30 / 100;"));
        Kiem.sai("khong con return actor.maxhp*20/100 hardcode", s.contains("return actor.maxhp * 20 / 100;"));
        Kiem.sai("khong con checkGiamSatThuongTong(att*20/100) hardcode", s.contains("actor.checkGiamSatThuongTong(att * 20 / 100)"));
        Kiem.sai("khong con checkGiamSatThuongTong(att*30/100) hardcode", s.contains("actor.checkGiamSatThuongTong(att * 30 / 100)"));
        Kiem.bang("doc chiSoPhanTramHoaKyLan(38) (% tan cong chu) it nhat 3 lan (attackDamAuto, attackDam x2)",
                Integer.valueOf(3), Integer.valueOf(dem(s, "this.owner.chiSoPhanTramHoaKyLan(38)")));
        Kiem.dung("doc chiSoPhanTramHoaKyLan(40) (% maxhp doi thu, chi trong attackDamAuto)",
                s.contains("final int pcMaxHp = this.owner.chiSoPhanTramHoaKyLan(40);"));
    }

    /** Hieu ung hien thi truoc day hardcode "attack*5/100" (dung cho cap 1), sai voi cap 2/3. */
    private static void khongConHieuUngHienSaiSo5PhanTram() {
        final String s = doc("src/data/CharThanthu.java");
        Kiem.sai("khong con hardcode attack*5/100 trong hieu ung hien thi",
                s.contains("this.owner.attack * 5 / 100"));
    }

    /** Suc khoe Than Thu doc tu chiSoTuyetDoiHoaKyLan(39), khong con hardcode 1000. */
    private static void sucKhoeThanThuDocTuHelper() {
        final String s = doc("src/real/Char.java");
        final int vt = s.indexOf("public void doCreateCharThanThu(final int idThanthu) {");
        Kiem.dung("tim thay doCreateCharThanThu", vt > 0);
        final String than = s.substring(vt, Math.min(s.length(), vt + 1700));
        Kiem.dung("doc chiSoTuyetDoiHoaKyLan(39) cho suc khoe", than.contains("this.chiSoTuyetDoiHoaKyLan(39)"));
        Kiem.dung("van co gia tri du phong 1000 khi khong doc duoc (vi du item hong)",
                than.contains("(sucKhoe > 0) ? sucKhoe : 1000"));
        Kiem.dung("hp dat bang dung maxhp vua tinh (khong con hardcode 1000 rieng)",
                than.contains("this.charthanthu.hp = this.charthanthu.maxhp;"));
    }

    /** timeAoHoaHoaKyLan phai duoc luu xuong CSDL va doc lai duoc, tuong thich nguoc voi du lieu cu. */
    private static void luuVaNapLaiTimeAoHoa() {
        final String vongLap = doc("src/real/CharVongLap.java");
        Kiem.dung("getDBInfo ghi them timeAoHoaHoaKyLan o CUOI chuoi",
                vongLap.contains("pInfo = String.valueOf(pInfo) + \",\" + self.timeAoHoaHoaKyLan;"));
        final int viTriSoQuai = vongLap.indexOf("self.soQuaiHomNay;");
        final int viTriAoHoa = vongLap.indexOf("self.timeAoHoaHoaKyLan;");
        Kiem.dung("nam SAU truong cuoi cung cu (soQuaiHomNay) - dung quy uoc them cuoi chuoi",
                viTriSoQuai >= 0 && viTriAoHoa > viTriSoQuai);

        final String napLieu = doc("src/real/CharNapDuLieu.java");
        Kiem.dung("doc lai data[115] trong try rieng (du lieu cu thieu truong nay se bi bat, khong lam hong ca khoi)",
                napLieu.contains("self.timeAoHoaHoaKyLan = Long.parseLong(data[115]);"));

        final String khoiTao = doc("src/real/CharKhoiTao.java");
        Kiem.dung("nhan vat moi khoi tao timeAoHoaHoaKyLan = 0", khoiTao.contains("self.timeAoHoaHoaKyLan = 0L;"));
    }

    /** checkExpireAoHoaHoaKyLan phai duoc goi dinh ky, cung nhom voi checkExpirePet. */
    private static void kiemTraHetHanTrongVongLapDinhKy() {
        final String s = doc("src/real/CharVongLap.java");
        Kiem.dung("goi self.checkExpireAoHoaHoaKyLan() cung nhom voi checkExpirePet",
                s.contains("self.checkExpirePet();\n                self.checkExpireItemInentry();\n                self.checkExpireAoHoaHoaKyLan();"));

        final String charJava = doc("src/real/Char.java");
        final int vt = charJava.indexOf("public void checkExpireAoHoaHoaKyLan() {");
        Kiem.dung("tim thay checkExpireAoHoaHoaKyLan", vt > 0);
        final String than = charJava.substring(vt, Math.min(charJava.length(), vt + 700));
        Kiem.dung("dat lai ve 0 khi het han", than.contains("this.timeAoHoaHoaKyLan = 0L;"));
        Kiem.dung("tinh lai chi so ngay khi het han (khong doi dang nhap lai)", than.contains("this.calculateAttrib();"));
    }

    /** Menu chon Hoa Ky Lan tu tui do phai LUON co 3 lua chon, ke ca item da cap toi da. */
    private static void menuLuonCoBaLuaChon() {
        final String s = doc("src/real/Map.java");
        Kiem.dung("menu 3 lua chon: Su dung / Nang cap / Ao hoa",
                s.contains("new String[]{\"Sử dụng\", \"Nâng cấp\", \"Ảo hóa\"}, -513, 0"));
        Kiem.sai("khong con re nhanh rieng theo idItemUpLevel (truoc day item max tu dong mac, khong hoi gi)",
                s.contains("if (item.getTemplate().idItemUpLevel > -1) {\n                            player.itemTam = item;"));
    }

    /** idOptionMenu 0/1/2 phai ung DUNG voi Su dung / Nang cap / Ao hoa, khong dam vao nhau. */
    private static void xuLyMenuBaLuaChonDungIdOptionMenu() {
        final String s = doc("src/real/MapDoMenu.java");
        final int vtCase = s.indexOf("case -513:");
        Kiem.dung("tim thay case -513", vtCase > 0);
        final String than = s.substring(vtCase, Math.min(s.length(), vtCase + 2500));
        Kiem.dung("idOptionMenu == 1 la Nang cap", than.contains("} else if (idOptionMenu == 1) {"));
        Kiem.dung("idOptionMenu == 2 la Ao hoa, goi doAoHoaHoaKyLan", than.contains("} else if (idOptionMenu == 2) {\n                            // "));
        Kiem.dung("nhanh Ao hoa goi dung ham", than.contains("player.doAoHoaHoaKyLan(player.itemTam);"));
        Kiem.sai("khong con dieu kien mo rong 'idOptionMenu != 0' (se lam Ao hoa roi nham vao Nang cap)",
                than.contains("} else if (idOptionMenu != 0) {"));
    }

    private static int dem(final String trong, final String mau) {
        int n = 0;
        int i = trong.indexOf(mau);
        while (i >= 0) {
            n++;
            i = trong.indexOf(mau, i + mau.length());
        }
        return n;
    }

    private static String doc(final String duongDan) {
        try {
            return new String(Files.readAllBytes(Paths.get(duongDan)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Kiem.dung("doc duoc " + duongDan, false);
            return "";
        }
    }
}
