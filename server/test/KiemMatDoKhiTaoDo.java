import io.BoDemGoiTin;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Kiem thu cum sua ngay 02/09/2026: "de auto lam do, hanh trang day thi bi out va
 * vao lai bi lui ve trang thai cu, mat het do da lam".
 *
 * Cum nay gom hai nhom sua tach biet nhau:
 *
 *   VE A - BI OUT. Ngap goi tin khong con cat ket noi ngay khi vuot tran; chi bo bot
 *   goi, va chi cat khi nhip cao gap BOI_CAT_KET_NOI lan. Kem theo la ba cho lam nhip
 *   goi tin vot len mot cach vo ich: tru xu khi tao do that bai, chan mua nguyen lieu
 *   sai loai o, va anh thieu lam client hoi lai mai.
 *
 *   VE B - MAT DU LIEU. Bon duong co the lam mot doi tuong Char cu de len ban ghi
 *   moi hon, hoac lam nhan vat bien mat ma khong duoc luu.
 *
 * Phan lon la kiem tren VAN BAN MA NGUON. Ly do: nhung ham lien quan (kickPlayer,
 * saveChar, doGetItem...) deu can CSDL that va mot phien socket that moi chay duoc,
 * khong dung duoc trong bo kiem thu khong phu thuoc gi nay. Kiem van ban van bat duoc
 * dieu quan trong nhat: co ai vo tinh dua lai khuon mau cu vao khong.
 */
public final class KiemMatDoKhiTaoDo {

    private static final int LENH_THUONG = 4;

    public static void chay() {
        Kiem.nhom("Mat do khi de auto lam do - cum sua 02/09/2026");

        ngapGoiThiBoBotChuKhongCatNgay();
        vuotXaNguongThiMoiCat();
        logNgapGoiKhongDuocNgap();
        khongTruXuKhiTaoDoThatBai();
        chanMuaNguyenLieuTheoONgoc();
        goNhanVatPhaiSoTheoThamChieu();
        phienCuKhongDuocKickNhanVatMoi();
        khongGoNhanVatMaKhongLuu();
        khongGhiChuoiHanhTrangDoDang();
        luuNhanVatPhaiCoKhoaRieng();
        phucVuAnhPhaiKiemBien();
        dapDoTuDongPhaiLuuKhiXong();
    }

    // ---------------------------------------------------------------- VE A

    /**
     * Vuot tran o muc "nhanh tay" thi bo goi, KHONG cat ket noi.
     *
     * Day la thay doi hanh vi quan trong nhat cua ve A. Ban cu cat ket noi ngay o goi
     * thu 301, va moi lan cat la mot vong luu/nap nhan vat - dung cho co hoi mat du
     * lieu. Nguoi choi luong thien de auto voi hanh trang day bi cat lien tuc: log
     * hack.txt ngay 01-02/09/2026 co 8 lan cat trong 37 giay cho cung mot nguoi.
     */
    private static void ngapGoiThiBoBotChuKhongCatNgay() {
        final BoDemGoiTin dem = new BoDemGoiTin(0L);
        for (int i = 0; i < BoDemGoiTin.GOI_THUONG_TOI_DA; i++) {
            Kiem.sai("goi thu " + (i + 1) + " chua vuot tran", dem.vuotNguong(LENH_THUONG, 0L));
        }
        Kiem.dung("goi thu " + (BoDemGoiTin.GOI_THUONG_TOI_DA + 1) + " vuot tran",
                dem.vuotNguong(LENH_THUONG, 0L));
        Kiem.sai("nhung CHUA vuot xa -> chi bo goi, khong cat ket noi",
                dem.vuotXaNguong(LENH_THUONG));

        final String session = doc("src/io/Session.java");
        // 19/10/2026: Session hoi BoDemGoiTin.phanLoai (QUA / BO / CAT / HOAN), phanLoai hoi vuotXaNguong.
        final String bo = doc("src/io/BoDemGoiTin.java");
        Kiem.dung("Session chi cat khi phanLoai tra CAT (phanLoai hoi vuotXaNguong)",
                session.contains("quyet == BoDemGoiTin.CAT") && session.contains("this.boDemGoiTin.phanLoai(command")
                        && bo.contains("return vuotXaNguong(command) ? CAT : BO;"));
        Kiem.dung("nhanh khong vuot xa thi continue (bo goi), khong break",
                session.contains("// Chi vuot tran o muc \"nhanh tay\": BO goi nay, GIU ket noi."));
    }

    /** Chi cat khi nhip cao gap BOI_CAT_KET_NOI lan tran. */
    private static void vuotXaNguongThiMoiCat() {
        Kiem.dung("boi cat ket noi phai lon hon 1 (khong thi lai thanh cat ngay)",
                BoDemGoiTin.BOI_CAT_KET_NOI > 1);

        final BoDemGoiTin dem = new BoDemGoiTin(0L);
        final int tran = BoDemGoiTin.GOI_THUONG_TOI_DA;
        final int nguongCat = tran * BoDemGoiTin.BOI_CAT_KET_NOI;
        for (int i = 0; i < nguongCat; i++) {
            dem.vuotNguong(LENH_THUONG, 0L);
        }
        Kiem.sai("dung o moc gap " + BoDemGoiTin.BOI_CAT_KET_NOI + " lan van chua cat",
                dem.vuotXaNguong(LENH_THUONG));
        dem.vuotNguong(LENH_THUONG, 0L);
        Kiem.dung("qua moc do mot goi thi moi cat", dem.vuotXaNguong(LENH_THUONG));

        // Cua so moi dat lai het, ke ca co da bao.
        dem.vuotNguong(LENH_THUONG, BoDemGoiTin.CUA_SO_MS);
        Kiem.sai("sang cua so moi thi khong con o trang thai cat",
                dem.vuotXaNguong(LENH_THUONG));
    }

    /**
     * Vi ket noi khong bi cat nua, moi goi vuot tran deu se sinh mot dong log neu
     * khong chan. Chan lai con 1 dong moi cua so 5 giay.
     */
    private static void logNgapGoiKhongDuocNgap() {
        final BoDemGoiTin dem = new BoDemGoiTin(0L);
        Kiem.dung("cua so dau: duoc ghi log", dem.xinPhepBao());
        Kiem.sai("van cua so do: khong ghi nua", dem.xinPhepBao());
        Kiem.sai("van cua so do: van khong ghi", dem.xinPhepBao());
        dem.vuotNguong(LENH_THUONG, BoDemGoiTin.CUA_SO_MS);
        Kiem.dung("sang cua so moi: duoc ghi lai", dem.xinPhepBao());

        final String session = doc("src/io/Session.java");
        Kiem.dung("Session chan log bang xinPhepBao", session.contains("this.boDemGoiTin.xinPhepBao()"));
        Kiem.dung("van luon ghi khi that su cat ket noi", session.contains("if (cat || this.boDemGoiTin.xinPhepBao())"));
    }

    /**
     * Tao do that bai thi KHONG duoc tru xu.
     *
     * doCreateNewItem tra ve null khi hanh trang day, thieu nguyen lieu, hoac goi tin
     * khong hop le. Ban cu tru 5000 xu o ca ba truong hop - nguoi de auto voi hanh
     * trang day bi bao mon xu lien tuc ma khong nhan duoc gi.
     */
    private static void khongTruXuKhiTaoDoThatBai() {
        final String s = doc("src/real/MapCheTao.java");
        Kiem.dung("giu lai ket qua cua doCreateNewItem", s.contains("final Item vuaTao = map.doCreateNewItem("));
        Kiem.dung("that bai thi ra som, khong tru xu", s.contains("if (vuaTao == null) {"));
        final int viTriKiem = s.indexOf("if (vuaTao == null) {");
        final int viTriTruXu = s.indexOf("player.subXu(5000L, true, \"map 23\")");
        Kiem.dung("cho kiem null nam TRUOC cho tru xu", viTriKiem > 0 && viTriTruXu > viTriKiem);
    }

    /**
     * Hanh trang day: nguyen lieu che tao phai kiem o NGOC, khong phai o THUOC.
     *
     * Nguyen lieu (shop id 116..125, 176) di vao o ngoc qua doAddGemItem, nen cong
     * thuc cu potions[item.id + 19] luon bang 0 va lenh mua luon bi tu choi ke ca khi
     * nguoi choi da co san dung loai do. Lenh 85 khong co truong so luong nen mot don
     * mua 300 don vi la 300 goi tin - day dung la nguon ngap goi thay trong log.
     */
    private static void chanMuaNguyenLieuTheoONgoc() {
        final String s = doc("src/real/doBuySpecialItem.java");
        Kiem.dung("co bang tra cuu shop id -> id ngoc", s.contains("private static int idNgocCuaShopItem(final int idShop)"));
        Kiem.dung("nhanh ngoc kiem listGemitem/listGemitemLock",
                s.contains("player.listGemitem[idNgoc] > 0 || player.listGemitemLock[idNgoc] > 0"));
        Kiem.dung("bang tra cuu khop voi mang idgem cua chinh cac nhanh mua",
                s.contains("{71, 72, 78, 79, 85, 86, 92, 93, 99, 100}")
                && s.contains("int[] idgem = new int[]{71, 72, 78, 79, 85, 86, 92, 93, 99, 100}"));
        Kiem.sai("khong con kiem thang potions[item.id + 19] == 0",
                s.contains("player.potions[item.id + 19] == 0"));
    }

    // ---------------------------------------------------------------- VE B

    /**
     * Go nhan vat khoi so dang ky phai so theo THAM CHIEU doi tuong.
     *
     * Khi bi cat roi vao lai ngay, hai doi tuong Char cua cung mot nhan vat trung ca
     * ba khoa (id, userID, ten). Ban cu quet vChars bang "trung ten HOAC trung
     * charDBID" nen loi goi remove(p_moi) o SelectCharHandler/LoginHandler go dang ky
     * chinh nhan vat DANG SONG.
     */
    private static void goNhanVatPhaiSoTheoThamChieu() {
        final String s = doc("src/real/CharManager.java");
        Kiem.sai("khong con quet vChars theo ten hoac charDBID",
                s.contains("charDBID == p.charDBID"));
        Kiem.dung("chi xoa khoa khi khoa do dang tro dung doi tuong nay",
                s.contains("if (this.charID.get(p.id) == p)")
                && s.contains("if (this.userID.get(p.userID) == p)")
                && s.contains("if (this.charName.get(p.getName()) == p)"));
        Kiem.dung("xoa khoi vChars theo tham chieu", s.contains("this.vChars.remove(p);"));
        // 02/09/2026: put() khong con tu choi ca cum theo ten cho BOT (xem
        // KiemCharManagerBotTrungTen) - nhung bat bien cua fix "mat do" van phai giu: moi phep
        // kiem (id/userID, va ten cho nguoi choi that) deu nam TRUOC dong ghi dau tien, nen
        // that bai thi khong ro ri khoa nao.
        final String thanPut = s.substring(s.indexOf("public boolean put(Char p) {"));
        final int viTriGhiDau = thanPut.indexOf("this.userID.put(p.userID, p);");
        final int viTriKiemId = thanPut.indexOf("if (this.userID.containsKey(p.userID) || this.charID.containsKey(p.id)) {");
        final int viTriKiemTen = thanPut.indexOf("if (tenDaCo && p.isBot == -1) {");
        Kiem.dung("put ghi ca cum: kiem het dieu kien truoc roi moi ghi",
                viTriGhiDau > 0 && viTriKiemId >= 0 && viTriKiemTen >= 0
                && viTriKiemId < viTriGhiDau && viTriKiemTen < viTriGhiDau);

        final String map = doc("src/real/Map.java");
        Kiem.dung("Map.removePLayer cung go theo tham chieu", map.contains("players.remove(player);"));
        Kiem.sai("Map.removePLayer khong con doi chieu theo charname",
                map.contains("((Char) players.get(i)).charname.toLowerCase().equals(player.charname.toLowerCase())"));
    }

    /** Mot phien vua dut chi duoc dong den nhan vat cua DUNG phien do. */
    private static void phienCuKhongDuocKickNhanVatMoi() {
        final String s = doc("src/real/RealController.java");
        Kiem.dung("onDisconnected doi chieu p.getSession() == conn",
                s.contains("if (p != null && p.getSession() == conn) {"));

        // Chot an toan cuoi cung: kickPlayer khong ghi xuong mot ban Char da het hieu
        // luc. CharVongLap cu 5 giay lai goi lai kickPlayer cho nhan vat co timeExit
        // da qua, nen mot ban cu con sot lai se tu ghi de ban moi neu khong co chot nay.
        final String cm = doc("src/real/CharManager.java");
        Kiem.dung("kickPlayer chi luu khi day van la ban dang dang ky",
                cm.contains("if (this.getByUserID(p.userID) == p) {"));
        final int viTriChot = cm.indexOf("if (this.getByUserID(p.userID) == p) {");
        final int viTriLuu = cm.indexOf("Database.instance.saveChar(p, false);");
        Kiem.dung("chot nam TRUOC cho luu", viTriChot > 0 && viTriLuu > viTriChot);
    }

    /**
     * Khong duoc phep co duong nao go nhan vat ma khong luu - VA khong duoc phep luu
     * DONG BO ngay trong vong lap ban do.
     *
     * Sua lan dau (chi luu, khong doi cho luu) tung tao ra mot regression nghiem trong
     * hon ca loi dang sua: Database.instance.saveChar(p, false) goi TRUC TIEP, dong bo,
     * ngay trong vong lap cap nhat cua CHINH LUONG "MAP <id>" (Map implements Runnable,
     * moi map mot luong rieng, ca quai lan nguoi choi cua ban do do deu cap nhat qua
     * dung luong nay). saveChar la nhieu cau lenh SQL noi tiep tren bang tob_char
     * (MyISAM, khoa CA BANG khi ghi) - mot lan goi cham (khoa bang, cho ket noi) la
     * luong map do dung hinh, va quai tren ban do do (roi ca ban do khac cung theo
     * kieu) ngung cap nhat/hien thi. Da xay ra that ~03/09/2026 sau mot lan khoi dong
     * lai lam nhieu nguoi reconnect gan nhu cung luc.
     *
     * Sua dung: giu nguyen phep kiem "dung la ban dang dang ky" (re, chi bo nho, lam
     * NGAY tren luong map), nhung day CHINH VIEC GHI xuong CSDL qua ThreadPool -
     * dung khuon mau CharVongLap dang dung cho autosave dinh ky.
     */
    private static void khongGoNhanVatMaKhongLuu() {
        final String s = doc("src/real/MapVongLap.java");
        Kiem.dung("co import io.ThreadPool", s.contains("import io.ThreadPool;"));
        Kiem.dung("phep kiem dung la ban dang dang ky nam NGOAI Runnable (lam tren luong map)",
                s.contains("if (CharManager.instance.getByUserID(p.userID) == p) {\n"
                        + "                            final Char pCanLuu = p;\n"
                        + "                            ThreadPool.instant.workList.add(new Runnable() {"));
        Kiem.dung("viec GHI CSDL nam trong Runnable, day qua ThreadPool",
                s.contains("ThreadPool.instant.workList.add(new Runnable() {")
                        && s.contains("Database.instance.saveChar(pCanLuu, false);"));
        // Chot quan trong nhat: khong con goi Database.instance.saveChar DONG BO
        // (ngoai Runnable) trong ham nay - day chinh la thu gay dung hinh quai.
        final int viTriHam = s.indexOf("static void updateSand(Map map)");
        final int viTriKetThucVongLap = s.indexOf("boolean exitBoard = false;");
        Kiem.dung("tim thay vi tri de kiem", viTriHam >= 0);
        final int viTriRunnableMo = s.indexOf("ThreadPool.instant.workList.add(new Runnable() {");
        final int viTriRunnableDong = s.indexOf("});", viTriRunnableMo);
        final String truocRunnable = s.substring(Math.max(0, viTriRunnableMo - 600), viTriRunnableMo);
        final String trongRunnable = s.substring(viTriRunnableMo, viTriRunnableDong);
        Kiem.sai("khong goi saveChar dong bo NGAY TRUOC Runnable (ngoai luong)",
                truocRunnable.contains("Database.instance.saveChar("));
        Kiem.dung("saveChar nam BEN TRONG Runnable (dung luong ThreadPool)",
                trongRunnable.contains("Database.instance.saveChar(pCanLuu, false);"));
    }

    /**
     * Chuoi hanh trang do dang thi HUY ca lan luu, khong duoc ghi de.
     *
     * Ghi mot chuoi bi cat giua chung la xoa vinh vien moi mon nam sau diem dut.
     */
    private static void khongGhiChuoiHanhTrangDoDang() {
        final String s = doc("src/data/DatabaseNhanVat.java");
        Kiem.dung("co ham ghep chuoi dung chung", s.contains("private static ChuoiVatPham ghepChuoiVatPham(final Char p)"));
        Kiem.dung("duyet tren ban chup (toArray) chu khong duyet thang Vector",
                s.contains("return v.toArray(new Item[0]);"));
        Kiem.dung("danh sach null thi nem loi, khong ghi de bang danh sach rong",
                s.contains("throw new IllegalStateException(\"danh sach vat pham null"));
        // Khong con BAT KY duong luu nao duyet thang p.iItems: bon ham ghi vao tob_char
        // (saveChar, saveCharAuto, saveCharLienDau, saveCharBackup) deu di qua ban chup.
        Kiem.sai("khong con for-each thang tren p.iItems trong duong luu",
                s.contains("for (Item item2 : p.iItems) {"));
        Kiem.bang("ca bon ham ghi tob_char deu dung ghepChuoiVatPham",
                Integer.valueOf(4), Integer.valueOf(dem(s, "final ChuoiVatPham chuoi = ghepChuoiVatPham(p);")));
        Kiem.dung("saveChar huy lan luu khi ghep chuoi loi",
                s.contains("huy luu vi ghep chuoi vat pham loi - "));
        Kiem.dung("saveCharAuto huy lan luu khi ghep chuoi loi",
                s.contains("huy luu tu dong vi ghep chuoi vat pham loi - "));
        Kiem.dung("van giu dung dinh dang cu: phan model noi o cuoi chuoi inven",
                s.contains("sInven = sInven + \">\" + infomodel;"));

        // Mot lan luu that bai phai de lai dau vet. Cho xin ket noi nem
        // NoSuchElementException (RuntimeException) khi pool can, ma "catch (SQLException)"
        // khong bat duoc: no xuyen qua roi bi nuot o ben goi, khong dong log nao.
        Kiem.sai("saveCharAuto khong con chi bat rieng SQLException",
                s.contains("} catch (SQLException ex) {\n            ex.printStackTrace();\n            System.out.println(\" LOI SAVE CHAR \""));
        Kiem.dung("luu tu dong that bai thi ghi log loisave",
                s.contains("\"luu tu dong that bai - \" + ex, \"loisave\""));
    }

    /** Hai lan luu cua CUNG mot nhan vat khong duoc chong nhau. */
    private static void luuNhanVatPhaiCoKhoaRieng() {
        final String c = doc("src/real/Char.java");
        Kiem.dung("Char co khoa luu rieng", c.contains("public final transient Object KHOA_LUU = new Object();"));

        final String d = doc("src/data/Database.java");
        Kiem.dung("saveChar giu khoa do", d.contains("synchronized (p.KHOA_LUU) {\n            return DatabaseNhanVat.saveChar(this, p, admindExit);"));
        Kiem.dung("saveCharAuto giu khoa do", d.contains("synchronized (p.KHOA_LUU) {\n            return DatabaseNhanVat.saveCharAuto(this, p);"));
    }

    /**
     * Phuc vu anh phai kiem bien: mot chi so ngoai bien lam ca lenh gui bi bo qua,
     * client khong nhan duoc tra loi nao va hoi lai mai (2.947 lan trong log VPS).
     */
    private static void phucVuAnhPhaiKiemBien() {
        final String s = doc("src/data/DataGame.java");
        Kiem.dung("co ham ghi anh co kiem bien", s.contains("private static void ghiAnh(final Message m, final byte[][] kho, final int chiSo, final int idImg)"));
        Kiem.dung("kiem ca null lan bien", s.contains("if (kho != null && chiSo >= 0 && chiSo < kho.length && kho[chiSo] != null) {"));
        Kiem.dung("dai anh pet cap du 300 o (dai id 5200..5499)", s.contains("final int nPet = 300;"));
        Kiem.sai("khong con nPet = 20", s.contains("byte nPet = 20;"));

        // Khong con cho nao ghi thang mang anh vao goi (tru skin linh thu da co san
        // kiem null). Doi chieu dung tung cau lenh cu de khong bat nham chu thich -
        // dong 109 co nhac ten m.dos.write(imgEffectSkill[...]) trong mot chu thich.
        Kiem.sai("khong con ghi thang anh icon", s.contains("m.dos.write(allImg[idImg]);"));
        Kiem.sai("khong con ghi thang anh pet", s.contains("m.dos.write(imgPet[idImg - 5200]);"));
        Kiem.sai("khong con ghi thang anh thuoc", s.contains("m.dos.write(imgPotion[idImg - 5500]);"));
        Kiem.sai("khong con ghi thang anh hieu ung skill", s.contains("m.dos.write(imgEffectSkill[idImg - 9000]);"));
        Kiem.sai("khong con ghi thang anh bo phan dong", s.contains("m.dos.write(imgDynamicEffect[idImg - 8700]);"));
    }

    /** Phien dap do tu dong ket thuc thi phai luu, khong doi lan luu tu dong sau. */
    private static void dapDoTuDongPhaiLuuKhiXong() {
        final String s = doc("src/real/CharCapNhat.java");
        Kiem.dung("co ham luu sau khi luyen", s.contains("private static void luuSauKhiLuyen(final Char self)"));
        Kiem.dung("luu qua saveCharAuto", s.contains("Database.instance.saveCharAuto(self);"));
        // 3 duong tu 03-04/09/2026: hoan thanh, het xu/nguyen lieu phat hien SAU khi da roll
        // (ban cu), va het xu/nguyen lieu phat hien TRUOC khi roll (vá them, tranh roll/tru
        // gem mien phi mot nhip khi vua het xu/nguyen lieu dung luc do - xem Char.enoughtLkd).
        Kiem.bang("goi o ca ba duong ket thuc phien", Integer.valueOf(3), Integer.valueOf(dem(s, "luuSauKhiLuyen(self);")));
    }

    // ---------------------------------------------------------------- tien ich

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
