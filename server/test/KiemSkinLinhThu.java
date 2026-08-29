/**
 * Kiem thu tinh nang skin linh thu (real.SkinLinhThu).
 *
 * Chi kiem cac ham THUAN - khong can Char, khong can DataGame da nap anh.
 * Cac ham can Char (dangMang, idAnhBanDo, bay, ghiAnhTrangBiThu...) khong kiem
 * duoc o day vi chung can mot doi tuong Char that, ma tao Char can Session +
 * CSDL. Do la gioi han that su cua bo test nay.
 */
public final class KiemSkinLinhThu {

    public static void chay() {
        Kiem.nhom("real.SkinLinhThu");

        Kiem.bang("co dung 5 skin", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.SO_SKIN));
        Kiem.bang("mang TEN du 5 phan tu", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.TEN.length));
        Kiem.bang("mang ID_ITEM du 5 phan tu", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.ID_ITEM.length));
        Kiem.bang("mang BAY du 5 phan tu", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.BAY.length));

        // hopLe: bien 0..4
        Kiem.sai("hopLe(-1)", real.SkinLinhThu.hopLe(-1));
        Kiem.dung("hopLe(0)", real.SkinLinhThu.hopLe(0));
        Kiem.dung("hopLe(4)", real.SkinLinhThu.hopLe(4));
        Kiem.sai("hopLe(5) - ngoai bien", real.SkinLinhThu.hopLe(5));
        Kiem.sai("hopLe(KHONG_CO)", real.SkinLinhThu.hopLe(real.SkinLinhThu.KHONG_CO));

        // skinCuaItem / laItemSkin: anh xa id vat pham -> so hieu skin
        for (int i = 0; i < real.SkinLinhThu.SO_SKIN; i++) {
            final int idItem = real.SkinLinhThu.ID_ITEM[i];
            Kiem.bang("skinCuaItem(" + idItem + ") = " + i,
                    Integer.valueOf(i), Integer.valueOf(real.SkinLinhThu.skinCuaItem(idItem)));
            Kiem.dung("laItemSkin(" + idItem + ")", real.SkinLinhThu.laItemSkin(idItem));
        }
        Kiem.bang("skinCuaItem(1) = KHONG_CO",
                Integer.valueOf(real.SkinLinhThu.KHONG_CO), Integer.valueOf(real.SkinLinhThu.skinCuaItem(1)));
        Kiem.sai("laItemSkin(1)", real.SkinLinhThu.laItemSkin(1));

        // ID_ITEM khong duoc trung nhau, neu trung thi skinCuaItem tra ve sai skin
        for (int i = 0; i < real.SkinLinhThu.ID_ITEM.length; i++) {
            for (int j = i + 1; j < real.SkinLinhThu.ID_ITEM.length; j++) {
                Kiem.sai("ID_ITEM[" + i + "] khac ID_ITEM[" + j + "]",
                        real.SkinLinhThu.ID_ITEM[i] == real.SkinLinhThu.ID_ITEM[j]);
            }
        }

        // moTa: skin hop le tra ve ten, khong hop le tra ve "khong co"
        Kiem.bang("moTa(0)", real.SkinLinhThu.TEN[0], real.SkinLinhThu.moTa(0));
        Kiem.bang("moTa(4)", real.SkinLinhThu.TEN[4], real.SkinLinhThu.moTa(4));
        Kiem.bang("moTa(-1)", "không có", real.SkinLinhThu.moTa(-1));
        Kiem.bang("moTa(5)", "không có", real.SkinLinhThu.moTa(5));

        kiemBoAnh();
        kiemDaiIcon();
        kiemLenhChat();
        kiemOGiuaVeThuThat();
        kiemViTriIconTrangBiThu();
        kiemChoNgoiSkin();
    }

    /**
     * Bo anh phai DU va DUNG DINH DANG.
     *
     * Thieu mot trong hai tep la DataGame.napSkinThu tat han skin do — im lang, khong loi.
     * Va so khung phai DUNG 21: client khong doc so khung tu tep ma tinh chi so bang cong
     * thuc co dinh (Char.getFrameHorse: xuong +0, len +7, ngang +14), nen it hon 21 la
     * paintTopHorse thoat som va con thu bien mat dung tick do.
     */
    private static void kiemBoAnh() {
        Kiem.nhom("Skin linh thu - bo anh");

        for (int i = 0; i < real.SkinLinhThu.SO_SKIN; i++) {
            final java.io.File du = new java.io.File("map/skinthu/" + i);
            final java.io.File atlas = new java.io.File("map/skinthu/" + i + ".png");
            Kiem.dung("co map/skinthu/" + i, du.isFile());
            Kiem.dung("co map/skinthu/" + i + ".png", atlas.isFile());
            Kiem.bang("skin " + i + " co dung 21 khung", Integer.valueOf(21),
                    Integer.valueOf(soKhung(du)));
        }
    }

    /**
     * O giua bang "Trang bi thu" phai ve CON THU THAT, khong phai skin.
     *
     * Ban skin dau tien ghi de cho do bang dai 3 khung cua skin, nen nguoi choi mo bang ra
     * chi thay con skin va mat luon cho xem con linh thu cua minh. Neu ai do dat lai cho
     * ghi de nay thi bai kiem duoi day hong.
     */
    private static void kiemOGiuaVeThuThat() {
        Kiem.nhom("Skin linh thu - o giua ve con thu that");

        for (final String tep : new String[] {
                "src/real/MessageCreator.java", "src/real/MapXuLyLenh.java" }) {
            final String ma = doc(tep);
            Kiem.dung("doc duoc " + tep, ma.length() > 0);
            Kiem.sai("KHONG ghi de anh bang Trang bi thu bang skin (" + tep + ")",
                    ma.contains("ghiAnhTrangBiThu"));
            Kiem.dung("van ghi anh con thu that (" + tep + ")",
                    ma.contains("DataGame.imgAvtAnimal[p.animalRide.idImg]"));
        }
        Kiem.sai("SkinLinhThu khong con ham ghi de do",
                doc("src/real/SkinLinhThu.java").contains("ghiAnhTrangBiThu"));
    }

    /**
     * Icon trang bi thu phai lay o theo idWearAnimal, KHONG theo posPaintWearing.
     *
     * posPaintWearing la mang cua bang NHAN VAT va chi dai 20 phan tu: type 19 (o dau tien
     * ben phai bang thu) tra ra o Phi phong cua bang nhan vat nen icon skin roi xuong goc
     * duoi trai, con type 20..23 thi vuot bien mang - nem IndexOutOfRange giua vong ve ma
     * vong ve khong co try/catch.
     *
     * Chi sua duoc o client Unity; ban J2ME cu khong build lai duoc nen van lech.
     */
    private static void kiemViTriIconTrangBiThu() {
        Kiem.nhom("Skin linh thu - vi tri icon trong bang Trang bi thu");

        for (final String ten : new String[] { "MenuWindows", "WindowInfoScr", "ShopNew" }) {
            final String tep = "ClientUnity/Assets/Scripts/Game/" + ten + ".cs";
            final String ma = doc(tep);
            Kiem.dung("doc duoc " + ten + ".cs", ma.length() > 0);
            Kiem.dung(ten + " co ham dat icon theo o trang bi thu",
                    ma.contains("private void paintIconTrangBiThu(MyGraphics g, ItemInInventory item, int type)"));
            Kiem.dung(ten + " tra o bang idWearAnimal", ma.contains("if (idWearAnimal[i] == type)"));
            // Vong ve trang bi thu phai goi ham moi, khong goi thang mang cu nua.
            final int vt = ma.indexOf("private void paintAnimalWearing(MyGraphics g)");
            final int het = (vt < 0) ? -1 : ma.indexOf("\n\tprivate void paintItemIcon", vt);
            final String than = (vt < 0 || het < 0) ? "" : ma.substring(vt, het);
            Kiem.dung(ten + ".paintAnimalWearing goi paintIconTrangBiThu",
                    than.contains("paintIconTrangBiThu(g, itemInInventory, itemTemplate.type)"));
            Kiem.sai(ten + ".paintAnimalWearing KHONG con dung posPaintWearing truc tiep",
                    than.contains("posPaintWearingX[itemTemplate.type]"));
        }
    }

    /**
     * Doc so khung tu tep du lieu: 1 byte so anh con, moi anh con 5 byte, roi short so
     * khung. Tra -1 neu doc khong duoc.
     */
    private static int soKhung(final java.io.File tep) {
        try {
            final byte[] b = java.nio.file.Files.readAllBytes(tep.toPath());
            final int soAnh = b[0] & 0xFF;
            final int vt = 1 + soAnh * 5;
            if (vt + 1 >= b.length) {
                return -1;
            }
            return ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
        } catch (final Exception e) {
            return -1;
        }
    }

    /**
     * Icon vat pham KHONG duoc dam vao dai 855..859.
     *
     * Ban skin dau tien dung dung dai do. Sau khi tinh nang bi go, nam id ay da duoc giao
     * cho CANH (commit 2c651ce) lam anh o xem truoc cua so Trang bi — dat de len la o xem
     * truoc cua canh hien ra con thu. Va nIconItem phai phu het dai icon moi: id nao
     * < nIconItem ma thieu file thi sendByteImg nem NPE roi bo qua ca lenh gui, client
     * hoi mai khong bao gio duoc tra loi.
     */
    private static void kiemDaiIcon() {
        Kiem.nhom("Skin linh thu - dai icon khong dam vao Canh");

        final String sql = doc("sql/skin_linh_thu.sql");
        Kiem.dung("doc duoc sql/skin_linh_thu.sql", sql.length() > 0);

        int lonNhat = -1;
        for (int i = 0; i < real.SkinLinhThu.SO_SKIN; i++) {
            final int idIcon = 860 + i;
            Kiem.dung("co map/icon/" + idIcon + ".png",
                    new java.io.File("map/icon/" + idIcon + ".png").isFile());
            Kiem.dung("SQL dat icon " + idIcon + " cho vat pham " + real.SkinLinhThu.ID_ITEM[i],
                    sql.contains("-1, " + idIcon + ", " + idIcon + ","));
            lonNhat = Math.max(lonNhat, idIcon);
        }
        for (int idCanh = 855; idCanh <= 859; idCanh++) {
            Kiem.sai("KHONG lay id " + idCanh + " cua canh lam icon skin",
                    sql.contains("-1, " + idCanh + ", " + idCanh + ","));
        }

        final String dg = doc("src/data/DataGame.java");
        Kiem.dung("nIconItem phu het dai icon skin (" + (lonNhat + 1) + ")",
                dg.contains("nIconItem = " + (lonNhat + 1) + ";"));
    }

    /**
     * Ban nay CHI co hai nguon phat skin: NPC doi thuong va giftcode. Lenh admin
     * "tangskin" cua ban dau tien da duoc bo theo yeu cau, nhung "boskin" thi PHAI con —
     * khong co no thi nguoi choi mac skin vao roi khong go ra duoc.
     */
    private static void kiemLenhChat() {
        Kiem.nhom("Skin linh thu - lenh chat");

        final String chat = doc("src/real/MapChat.java");
        Kiem.dung("doc duoc src/real/MapChat.java", chat.length() > 0);
        Kiem.dung("con lenh boskin de thao skin", chat.contains("\"boskin\""));
        Kiem.sai("KHONG con lenh admin tangskin", chat.contains("tangskin"));
    }

    /**
     * Cho ngoi cua skin: bang bu phai co du 5 muc va phai duoc CONG vao dy_horse.
     *
     * Client ve con thu ngay tai chan nhan vat roi nhac rieng nhan vat len bang mot bang
     * CO DINH (Char.dy_horse), khong theo con thu. Bo sprite skin thap hon con thu chuan
     * nen chan nguoi cao hon lung thu. Khong nhac con thu len duoc (nhac bao nhieu thi
     * chan no roi mat dat bay nhieu), nen phai ha nguoi ngoi xuong.
     *
     * Dau CONG la quan trong: dy_horse am la nhac len, cong so duong vao la bot nhac ->
     * ngoi thap xuong. Tru di la nguoi bay cao hon nua, ho to gap doi.
     */
    private static void kiemChoNgoiSkin() {
        Kiem.nhom("Skin linh thu - cho nguoi ngoi");

        final String ma = doc("ClientUnity/Assets/Scripts/Game/Char.cs");
        Kiem.dung("doc duoc Char.cs", ma.length() > 0);
        Kiem.dung("co bang bu cho ngoi", ma.contains("public static int[] BU_NGOI_SKIN = new int[5]"));
        Kiem.dung("moc dai id skin khop voi server",
                ma.contains("ID_ANH_SKIN_DAU = " + real.SkinLinhThu.ID_ANH_BAN_DO));
        Kiem.dung("bu duoc CONG vao dy_horse, khong phai tru",
                ma.contains("return dy_horse[typehorse][dir] + ((n >= 0) ? BU_NGOI_SKIN[n] : 0);"));

        final String gs = doc("ClientUnity/Assets/Scripts/Game/GameScr.cs");
        Kiem.dung("co phim do cho ngoi ngay trong game", gs.contains("doChinhChoNgoiSkin()"));
        Kiem.dung("so dang do duoc ve len man hinh", gs.contains("veSoBuNgoiSkin(g)"));
    }

    private static String doc(final String duongDan) {
        try {
            return new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(duongDan)), "UTF-8");
        } catch (final Exception e) {
            return "";
        }
    }

    private KiemSkinLinhThu() {
    }
}
