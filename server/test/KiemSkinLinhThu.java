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
        kiemSoTypeTrangBiThu();
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
     * posPaintWearing la mang cua bang NHAN VAT va o ban Unity chi dai 20 phan tu, trong
     * khi cot phai bang thu mang type 30..34 — tra thang vao mang la vuot bien, nem
     * IndexOutOfRange giua vong ve ma vong ve khong co try/catch.
     *
     * Ban JAR khong can sua: mang cua no dai 36 va co san o 30..34. Xem
     * kiemSoTypeTrangBiThu de biet vi sao hai ban lech nhau.
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
     * SO TYPE cua vat pham skin phai dong bo o BON noi.
     *
     * Bang "Trang bi thu" co 10 o; client tra thu tu o trong mang idWearAnimal, va HAI BAN
     * CLIENT DANH SO COT PHAI KHAC NHAU:
     *
     *     ban JAR   : {18, 30, 14, 31, 15, 32, 16, 33, 17, 34}   mang pos dai 36
     *     ban Unity : {18, 19, 14, 20, 15, 21, 16, 22, 17, 23}   mang pos dai 20  (ban cu)
     *
     * Ban Unity duoc port tu mot ban jar cu hon nen giu cach danh so cu. Dat type 19 thi
     * ban Unity ve dung con ban JAR tra posPaintWearing[19] = (34, 92) — o PHI PHONG cua
     * bang NHAN VAT — nen icon skin roi xuong goc duoi. Da thong nhat theo ban JAR (30),
     * vi nhu vay ban JAR chay dung ma KHONG phai dong goi lai jar client.
     *
     * Bon noi phai khop: SkinLinhThu.TYPE_ITEM, cot type trong sql, idWearAnimal cua ba
     * man hinh Unity, va idWearAnimal cua ban JAR. Lech mot cai la icon lai nhay cho.
     */
    private static void kiemSoTypeTrangBiThu() {
        Kiem.nhom("Skin linh thu - so type dong bo giua server, sql va hai client");

        final int TYPE = 30;
        final String MANG = "{ 18, 30, 14, 31, 15, 32, 16, 33, 17, 34 }";

        Kiem.bang("SkinLinhThu.TYPE_ITEM", TYPE, real.SkinLinhThu.TYPE_ITEM);
        Kiem.sai("TYPE_ITEM khong duoc trung type Phi phong (Map.isPhiPhong = 19)",
                real.SkinLinhThu.TYPE_ITEM == 19);
        Kiem.sai("TYPE_ITEM khong duoc nam trong dai isWearingAnimal (14..18)",
                real.SkinLinhThu.TYPE_ITEM >= 14 && real.SkinLinhThu.TYPE_ITEM <= 18);

        // sql: ca 5 dong INSERT phai mang dung so type do.
        final String sql = doc("sql/skin_linh_thu.sql");
        Kiem.dung("doc duoc sql/skin_linh_thu.sql", sql.length() > 0);
        int soDong = 0;
        for (final String dong : sql.split("\n")) {
            if (!dong.contains("'Skin linh thú")) {
                continue;
            }
            soDong++;
            // cot type la so dau tien sau ten trong ngoac nhay
            final String sau = dong.substring(dong.lastIndexOf('\'') + 1).trim();
            final String so = sau.startsWith(",") ? sau.substring(1).trim().split(",")[0].trim() : "?";
            Kiem.bang("sql dong " + soDong + " co type " + TYPE, String.valueOf(TYPE), so);
        }
        Kiem.bang("sql co du 5 dong INSERT", real.SkinLinhThu.SO_SKIN, soDong);

        // Phai co san file chuyen doi cho may da chay ban sql cu.
        final String vaSql = doc("sql/sua_type_skin_linh_thu.sql");
        Kiem.dung("co sql/sua_type_skin_linh_thu.sql", vaSql.length() > 0);
        Kiem.dung("file chuyen doi dat type ve " + TYPE,
                vaSql.contains("SET `type` = " + TYPE));

        // Ba man hinh Unity phai dung cach danh so cua ban JAR.
        for (final String ten : new String[] { "MenuWindows", "WindowInfoScr", "ShopNew" }) {
            final String ma = doc("ClientUnity/Assets/Scripts/Game/" + ten + ".cs");
            Kiem.dung(ten + ".cs dung idWearAnimal theo ban JAR",
                    ma.contains("idWearAnimal = new sbyte[10] " + MANG));
            Kiem.sai(ten + ".cs khong con cach danh so cu 19..23",
                    ma.contains("{ 18, 19, 14, 20, 15, 21, 16, 22, 17, 23 }"));
        }

        // Doi chieu voi chinh nguon ban JAR neu co san (build/c276 khong nam trong git).
        final java.io.File jar = new java.io.File("build/c276/game/render/screen/WindowInfoScr.java");
        if (jar.isFile()) {
            final String ma = doc(jar.getPath());
            Kiem.dung("ban JAR van danh so cot phai la 30..34",
                    ma.contains("idWearAnimal = new byte[]{18, 30, 14, 31, 15, 32, 16, 33, 17, 34}"));
            // O thu TYPE trong mang toa do phai la cot phai (x = 107) hang dau (y = 11).
            final int[] x = mangSo(ma, "posPaintWearingX = new int[]{");
            final int[] y = mangSo(ma, "posPaintWearingY = new int[]{");
            Kiem.dung("ban JAR co o " + TYPE + " trong posPaintWearing",
                    x.length > TYPE && y.length > TYPE);
            if (x.length > TYPE && y.length > TYPE) {
                Kiem.bang("ban JAR: x cua o " + TYPE + " la cot phai", 107, x[TYPE]);
                Kiem.bang("ban JAR: y cua o " + TYPE + " la hang dau", 11, y[TYPE]);
            }
        } else {
            System.out.println("    (bo qua doi chieu ban JAR: khong co " + jar.getPath() + ")");
        }
    }

    /** Doc mot mang so nguyen viet lien trong nguon Java, vd "new int[]{1, 2, 3}". */
    private static int[] mangSo(final String ma, final String moc) {
        final int vt = ma.indexOf(moc);
        if (vt < 0) {
            return new int[0];
        }
        final int het = ma.indexOf('}', vt);
        if (het < 0) {
            return new int[0];
        }
        final String[] phan = ma.substring(vt + moc.length(), het).split(",");
        final int[] ra = new int[phan.length];
        for (int i = 0; i < phan.length; i++) {
            try {
                ra[i] = Integer.parseInt(phan[i].trim());
            } catch (final NumberFormatException e) {
                return new int[0];
            }
        }
        return ra;
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
     * Cho nguoi ngoi phai CHOT TRONG DU LIEU, khong co duong tinh chinh rieng nao.
     *
     * Client ve con thu tai CHAN nhan vat roi nhac RIENG nhan vat len bang dy_horse — mot
     * bang co dinh chi co MOT dong, o ca ban Unity lan ban JAR. Nen cho ngoi chi can duoc
     * bang cach doi toa do cac khung cua con thu (tools/CanChoNgoiSkinThu), va lam vay thi
     * chay tren moi client.
     *
     * TRUOC DAY con them mang Char.BU_NGOI_SKIN + phim P de do tay so bu ngay trong game.
     * Da bo han: mang do luon bang 0 sau khi da can trong du lieu, ma de lai thi phim P
     * van cong don duoc tung pixel — bam nham la nguoi lun vao than thu, nhin nhu mat nua
     * nguoi. Hai kiem tra dau chan dung viec do quay lai.
     *
     * Kiem tra thu ba do do "lut" (chan nguoi lun vao than thu) roi doi chieu voi CHINH
     * CAC CON THU TOOL CUA GAME — Poro (map/d_effect/56), Lan (62), Heo (64) — chu khong
     * chot mot con so tu nghi ra. Ra ngoai khoang do la bo sprite da bi keo lech.
     */
    private static void kiemChoNgoiSkin() {
        Kiem.nhom("Skin linh thu - cho nguoi ngoi");

        final String ma = doc("ClientUnity/Assets/Scripts/Game/Char.cs");
        Kiem.dung("doc duoc Char.cs", ma.length() > 0);
        Kiem.dung("nhac nguoi lay THANG tu dy_horse, khong cong them gi",
                ma.contains("return dy_horse[typehorse][dir];"));
        Kiem.sai("KHONG con mang bu cho ngoi rieng cho ban Unity",
                ma.contains("int[] BU_NGOI_SKIN"));

        final String gs = doc("ClientUnity/Assets/Scripts/Game/GameScr.cs");
        Kiem.dung("doc duoc GameScr.cs", gs.length() > 0);
        Kiem.sai("KHONG con phim do cho ngoi trong ban phat hanh",
                gs.contains("doChinhChoNgoiSkin"));

        // Khoang binh thuong lay tu chinh cac con thu tool cua game.
        int thap = Integer.MAX_VALUE;
        int cao = Integer.MIN_VALUE;
        for (final String mocThu : new String[] { "56", "62", "64" }) {
            final int l = lutHuongXuong(new java.io.File("map/d_effect/" + mocThu));
            if (l == Integer.MIN_VALUE) {
                continue;
            }
            thap = Math.min(thap, l);
            cao = Math.max(cao, l);
        }
        if (thap == Integer.MAX_VALUE) {
            System.out.println("    (bo qua doi chieu: khong doc duoc map/d_effect/56,62,64)");
            return;
        }
        final int bien = 2;
        System.out.println("    thu tool cua game lut " + thap + ".." + cao
                + "px, cho phep " + (thap - bien) + ".." + (cao + bien));
        for (int n = 0; n < real.SkinLinhThu.SO_SKIN; n++) {
            final int l = lutHuongXuong(new java.io.File("map/skinthu/" + n));
            Kiem.dung("skin " + n + " lut " + l + "px, nam trong khoang thu tool cua game",
                    l != Integer.MIN_VALUE && l >= thap - bien && l <= cao + bien);
        }
    }

    /**
     * Do "lut": chan nhan vat lun vao than con thu bao nhieu pixel, huong XUONG.
     *
     * = Char.dy_horse[0][DOWN] tru di dinh cao nhat cua con thu o khoi khung huong xuong.
     * Tra Integer.MIN_VALUE neu khong doc duoc tep.
     */
    private static int lutHuongXuong(final java.io.File tep) {
        try {
            final byte[] b = java.nio.file.Files.readAllBytes(tep.toPath());
            final int soAnh = b[0] & 0xFF;
            int vt = 1 + soAnh * 5;
            final int soKhung = ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
            vt += 2;
            int tren = Integer.MAX_VALUE;
            for (int f = 0; f < soKhung; f++) {
                final int soManh = b[vt] & 0xFF;
                vt++;
                for (int k = 0; k < soManh; k++) {
                    if (f < 7) {                       // khoi huong xuong = khung 0..6
                        // dy nam o vt+2..vt+3; vt..vt+1 la dx.
                        tren = Math.min(tren, (short) (((b[vt + 2] & 0xFF) << 8) | (b[vt + 3] & 0xFF)));
                    }
                    vt += 7;
                }
            }
            return (tren == Integer.MAX_VALUE) ? Integer.MIN_VALUE : -20 - tren;
        } catch (final Exception e) {
            return Integer.MIN_VALUE;
        }
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
