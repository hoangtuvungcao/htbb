/** Kiem thu cac ham xu ly chuoi thuan trong real.Map. */
public final class KiemChuoi {

    public static void chay() {
        Kiem.nhom("real.Map - ham chuoi");

        // getNumberFromString: giu lai dung cac ky tu '0'-'9', bo het phan con lai.
        Kiem.bang("getNumberFromString giu chu so", "1234", real.Map.getNumberFromString("abc123x4"));
        Kiem.bang("getNumberFromString chuoi rong", "", real.Map.getNumberFromString(""));
        Kiem.bang("getNumberFromString khong co chu so", "", real.Map.getNumberFromString("abc"));
        Kiem.bang("getNumberFromString toan chu so", "907", real.Map.getNumberFromString("907"));
        Kiem.bang("getNumberFromString bo dau am", "5", real.Map.getNumberFromString("-5"));
        Kiem.bang("getNumberFromString bo dau cham", "312", real.Map.getNumberFromString("3.12"));
        // Ghi nhan mot dac diem de gay bat ngo: so am va so thap phan bi mat dau,
        // nen KHONG dung ham nay de doc gia tien co dau phan cach.
        Kiem.bang("getNumberFromString '49.000d' -> '49000'", "49000", real.Map.getNumberFromString("49.000d"));

        // replaceStringWithSpace (0.9.8): giu moi chu tieng Viet + dau cau; chi bo ky tu la / dau nhay / < > / gach cheo
        Kiem.bang("replaceStringWithSpace giu chu Viet",
                "Nguyễn Văn A", real.Map.replaceStringWithSpace("Nguyễn Văn A"));
        Kiem.bang("giu đ Đ í ì ỉ ĩ ị va dau cau", "Đi đâu? Ít thì ỉ ĩ ị, vui!",
                real.Map.replaceStringWithSpace("Đi đâu? Ít thì ỉ ĩ ị, vui!"));
        Kiem.bang("replaceStringWithSpace bo ky tu la",
                "abc", real.Map.replaceStringWithSpace("a<b>c"));
        Kiem.bang("replaceStringWithSpace giu email",
                "a.b@c-d_e+f", real.Map.replaceStringWithSpace("a.b@c-d_e+f"));
        Kiem.bang("bo dau nhay, GIU khoang trang", " OR 1=1", real.Map.replaceStringWithSpace("' OR 1=1"));
        real.Map.extend = new String[]{".com", ".vn"};
        Kiem.bang("replaceExtendDomain giu hoa thuong, bo ten mien / so dien thoai / lien ket",
                "Chào Bạn, ghé shopabc nhé *** hoặc ***", real.Map.replaceExtendDomain("Chào Bạn, ghé shopabc.COM nhé 0912 345 678 hoặc https://x.vn/a"));
        Kiem.bang("replaceExtendDomain khong dung so tien", "Bán 100000 xu", real.Map.replaceExtendDomain("Bán 100000 xu"));
        Kiem.bang("checkBadWord2 giu hoa thuong", "Xin Chào ĐỘI", real.BadWord.checkBadWord2("Xin  Chào ĐỘI"));

        Kiem.nhom("Constant.Checker");
        // resetMapId hien la ham dong nhat - toan bo switch da bi comment lai.
        // Test nay ghi lai dieu do: neu ai mo lai switch, test se do va bat phai xem lai.
        Kiem.bang("resetMapId(28) tra ve chinh no", Integer.valueOf(28), Integer.valueOf(Constant.Checker.resetMapId(28)));
        Kiem.bang("resetMapId(0) tra ve chinh no", Integer.valueOf(0), Integer.valueOf(Constant.Checker.resetMapId(0)));
        Kiem.bang("resetMapId(861) tra ve chinh no", Integer.valueOf(861), Integer.valueOf(Constant.Checker.resetMapId(861)));
    }

    private KiemChuoi() {
    }
}
