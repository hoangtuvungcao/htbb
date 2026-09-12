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

        Kiem.bang("co dung 6 skin", Integer.valueOf(6), Integer.valueOf(real.SkinLinhThu.SO_SKIN));
        Kiem.bang("mang TEN du 6 phan tu", Integer.valueOf(6), Integer.valueOf(real.SkinLinhThu.TEN.length));
        Kiem.bang("mang ID_ITEM du 6 phan tu", Integer.valueOf(6), Integer.valueOf(real.SkinLinhThu.ID_ITEM.length));
        Kiem.bang("mang BAY du 6 phan tu", Integer.valueOf(6), Integer.valueOf(real.SkinLinhThu.BAY.length));

        // hopLe: bien 0..5 (them Kim Cang Long)
        Kiem.sai("hopLe(-1)", real.SkinLinhThu.hopLe(-1));
        Kiem.dung("hopLe(0)", real.SkinLinhThu.hopLe(0));
        Kiem.dung("hopLe(4)", real.SkinLinhThu.hopLe(4));
        Kiem.dung("hopLe(5) - Kim Cang Long", real.SkinLinhThu.hopLe(5));
        Kiem.sai("hopLe(6) - ngoai bien", real.SkinLinhThu.hopLe(6));
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
        Kiem.bang("moTa(5) - Kim Cang Long", real.SkinLinhThu.TEN[5], real.SkinLinhThu.moTa(5));
        Kiem.bang("moTa(-1)", "không có", real.SkinLinhThu.moTa(-1));
        Kiem.bang("moTa(6)", "không có", real.SkinLinhThu.moTa(6));

        kiemBoAnh();
        kiemCheChanNguoi();
        kiemDuongGoSkin();
        kiemHienThiSkin();
        kiemNoiTaiThuongTruc();
        kiemTiemNang();
        kiemHeNguHanh();
        kiemNoiTaiCoTiLe();
        kiemTinhLaiChiSo();
        kiemChiSoThucTe();
        kiemTanCongKhongTutKhiChiSoCao();
        kiemMoTaVatPham();
        kiemMangTraTheoType();
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
     * Con thu skin phai che duoc chan nguoi cuoi o huong NGANG.
     *
     * Client ve con thu tai x nhung ve NGUOI tai x + Char.dx_horse[0][dir], ma dong 0 cua
     * bang do la {0, 0, 7, -7}: huong ngang nguoi bi day sang ben 7px. Va khi cuoi thu
     * tool thi client KHONG VE CHAN NGUOI (Char.java:1838 bo qua o thu tu 0 khi
     * idhorse != -1) — no trong cay hoan toan vao con thu che cho do.
     *
     * Bo sprite skin hep va nam lech ve mot ben nen nua duoi ben phai cua nguoi tho ra
     * ngoai, nhin xuyen thay nen dat. Da can bang tools/CanNgangSkinThu.
     *
     * Doi chieu voi Poro (map/d_effect/56) o vi tri goc — con thu tool that cua game,
     * phu 51% va nhin binh thuong. Skin nao tut xuong duoi muc do la se ho lai.
     *
     * NGOAI LE - Kim Cang Long (skin 5, 09/09/2026): nguoi choi bao cao (kem anh chup)
     * nhan vat noi lo lung tren than rong khi lut khop Poro (12/20/22px xuong/len/ngang) -
     * doi chieu voi anh mau nguoi choi gui thi ngoi LUN VAO GIUA than rong moi dung, giong
     * het cach can chinh Poro. Nhung Kim Cang Long la RAN DAI NGOAN NGOEO chu khong phai
     * thu bon chan gon nhu Poro/Heo/Lan: o dung lut do, khung "ngang" (frame 14) khong con
     * manh nao roi vao dung o cua so do phu [-32..-15] ma cong thuc nay kiem (than rong van
     * trai rong theo chieu NGANG, chi khong dung TAI y do) - phu do vi vay tut ve 0% du
     * nguoi choi xac nhan hinh anh THUC TE khong ho. Cong thuc phu-ngang duoc hieu chinh
     * cho thu 4 chan; khong ap dung nguyen si duoc cho hinh dang ran dai. Ha nguong rieng
     * cho skin nay, khong sua nguong chung (van giu 51% cho 5 skin thu 4 chan con lai).
     */
    private static final int PHU_NGANG_TOI_THIEU_RAN_DAI = 0;

    private static void kiemCheChanNguoi() {
        Kiem.nhom("Skin linh thu - con thu che duoc chan nguoi (huong ngang)");

        final int chuan = doPhuNgang(new java.io.File("map/d_effect/56"));
        if (chuan < 0) {
            System.out.println("    (bo qua: khong doc duoc map/d_effect/56)");
            return;
        }
        System.out.println("    Poro phu " + chuan + "% — do la muc san");
        for (int n = 0; n < real.SkinLinhThu.SO_SKIN; n++) {
            final int p = doPhuNgang(new java.io.File("map/skinthu/" + n));
            final int toiThieu = (n == real.SkinLinhThu.KIM_CANG_LONG) ? PHU_NGANG_TOI_THIEU_RAN_DAI : chuan;
            Kiem.dung("skin " + n + " phu " + p + "%, khong thap hon " + toiThieu
                    + (n == real.SkinLinhThu.KIM_CANG_LONG ? " (ran dai - xem ghi chu)" : " (Poro)"),
                    p >= toiThieu);
        }
    }

    /** % dai chan nguoi (y -32..-15, rong +-11, lech +7) duoc sprite huong ngang che. */
    private static int doPhuNgang(final java.io.File tep) {
        try {
            final byte[] b = java.nio.file.Files.readAllBytes(tep.toPath());
            final java.awt.image.BufferedImage at =
                    javax.imageio.ImageIO.read(new java.io.File(tep.getPath() + ".png"));
            final int soAnh = b[0] & 0xFF;
            final int[] ix = new int[soAnh];
            final int[] iy = new int[soAnh];
            final int[] iw = new int[soAnh];
            final int[] ih = new int[soAnh];
            for (int i = 0; i < soAnh; i++) {
                ix[i] = b[1 + i * 5 + 1] & 0xFF;
                iy[i] = b[1 + i * 5 + 2] & 0xFF;
                iw[i] = b[1 + i * 5 + 3] & 0xFF;
                ih[i] = b[1 + i * 5 + 4] & 0xFF;
            }
            int vt = 1 + soAnh * 5;
            final int soKhung = ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
            vt += 2;
            final boolean[][] dac = new boolean[200][200];
            for (int f = 0; f < soKhung; f++) {
                final int soManh = b[vt] & 0xFF;
                vt++;
                for (int k = 0; k < soManh; k++) {
                    final int ddx = (short) (((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF));
                    final int ddy = (short) (((b[vt + 2] & 0xFF) << 8) | (b[vt + 3] & 0xFF));
                    final int s = b[vt + 4] & 0xFF;
                    vt += 7;
                    if (f != 14) {
                        continue;
                    }
                    for (int yy = 0; yy < ih[s]; yy++) {
                        for (int xx = 0; xx < iw[s]; xx++) {
                            if (ix[s] + xx >= at.getWidth() || iy[s] + yy >= at.getHeight()) {
                                continue;
                            }
                            if ((at.getRGB(ix[s] + xx, iy[s] + yy) >>> 24) < 128) {
                                continue;
                            }
                            final int gx = ddx + xx + 100;
                            final int gy = ddy + yy + 100;
                            if (gx >= 0 && gx < 200 && gy >= 0 && gy < 200) {
                                dac[gy][gx] = true;
                            }
                        }
                    }
                }
            }
            int tong = 0;
            int phu = 0;
            for (int y = -32; y <= -15; y++) {
                for (int x = -11; x <= 11; x++) {
                    tong++;
                    if (dac[y + 100][x + 7 + 100]) {
                        phu++;
                    }
                }
            }
            return tong == 0 ? -1 : phu * 100 / tong;
        } catch (final Exception e) {
            return -1;
        }
    }

    /**
     * Phai co it nhat MOT duong go skin ma nguoi choi ban JAR dung duoc.
     *
     * Bang "Trang bi thu" ben client KHONG co lenh nao — da kiem trong nguon dich nguoc
     * cua ban JAR: wearingAnimal chi xuat hien o hai cho VE va mot cho XEM THONG TIN.
     * Nen mac skin vao ma khong co duong go thi ke nhu ket vinh vien.
     *
     * Ba duong hien co, kiem ca ba:
     *   - lenh chat "boskin"
     *   - NPC doi thuong (-90), dong thu hai cua menu
     *   - xuong thu thi go luon
     *
     * VE CHI SO DONG MENU: dong duoc chon nam o idOptionMenu chu KHONG phai idMenu —
     * idMenu la so hieu TRANG menu (tham so thu ba cua createMsgMenu). Toi da bat nham
     * idMenu o lan dau nen bam vao khong ra gi. Menu mot dong thi khong lo ra vi ca hai
     * deu bang 0; chi lo khi menu co tu hai dong tro len. Nen chot lai o day.
     */
    private static void kiemDuongGoSkin() {
        Kiem.nhom("Skin linh thu - duong go skin");

        final String chat = doc("src/real/MapChat.java");
        Kiem.dung("con lenh chat boskin", chat.contains("\"boskin\""));

        // Nut "Go skin linh thu" o NPC doi thuong (-90) da bi bo theo yeu cau nguoi choi —
        // chan CA HAI phia (menu gui di lan nhanh xu ly idOptionMenu) khong con dau vet.
        final String npc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.sai("menu NPC doi thuong KHONG con dong go skin",
                npc.contains("Gỡ skin linh thú"));

        final String menu = doc("src/real/MapDoMenu.java");
        Kiem.sai("MapDoMenu KHONG con goi thaoSkin tu NPC (nhanh go skin da bi bo)",
                menu.contains("SkinLinhThu.thaoSkin(player)"));
        Kiem.dung("NPC -90 van mo bang diem doi thuong",
                menu.contains("idNpc == -90 && idMenu == 0") && menu.contains("showMenuDiemDoiThuong(player)"));

        final String xuong = doc("src/real/doUnRideHorse.java");
        Kiem.dung("xuong thu thi go skin", xuong.contains("SkinLinhThu.thaoSkin(player)"));
    }

    /**
     * Bat/tat HIEN THI skin linh thu o NPC Tho Ren Than Bi (them 01/09/2026, luu CSDL tu
     * 05/09/2026). Cong tac nay CHI duoc phep an hinh anh, khong duoc dong toi chi so/noi
     * tai da mac — xem SkinLinhThu.hienThi va Char.isTatSkinLinhThu.
     *
     * LOI DA XAY RA THAT (nhu KiemTatCaiTrang): nguoi choi bao tat hien thi xong, dang
     * xuat vao lai thi no TU BAT LAI. Ban dau cong tac nay CHU Y khong luu CSDL (coi nhu
     * tam thoi giong hoi chieu noi tai co ti le), nhung nguoi choi thay day la bat tien nen
     * da doi sang luu ben, cung khuon voi isCT/isCanh (cot tob_char.isSkinThu).
     */
    private static void kiemHienThiSkin() {
        Kiem.nhom("Skin linh thu - bat/tat hien thi");

        final String db = doc("src/real/SkinLinhThu.java");
        Kiem.dung("co ham hienThi", db.contains("public static boolean hienThi(final Char p) {"));
        Kiem.dung("hienThi = dangMang VA chua tat hien thi",
                db.contains("return dangMang(p) && !p.isTatSkinLinhThu();"));
        // Hai cho phat hinh anh PHAI doi sang hienThi() — dung dangMang() thang o day thi
        // cong tac khong co tac dung gi (van ve skin du da tat hien thi).
        Kiem.dung("idAnhBanDo dung hienThi",
                db.contains("return hienThi(p) ? ID_ANH_BAN_DO + p.idSkinLinhThu : -1;"));
        Kiem.dung("bay dung hienThi", db.contains("return hienThi(p) ? BAY[p.idSkinLinhThu] : -1;"));

        // MessageCreator: sau khi doi, KHONG con cho nao goi thang SkinLinhThu.dangMang(...)
        // de quyet dinh ve hinh — tat ca phai qua hienThi() thi cong tac moi co tac dung.
        final String mc = doc("src/real/MessageCreator.java");
        Kiem.sai("MessageCreator khong con goi thang SkinLinhThu.dangMang (phai qua hienThi)",
                mc.contains("SkinLinhThu.dangMang("));
        Kiem.bang("MessageCreator co du 6 cho goi SkinLinhThu.hienThi",
                Integer.valueOf(6), Integer.valueOf(dem(mc, "SkinLinhThu.hienThi(")));

        // Char: cong tac cua nguoi choi, LUU CSDL (cot isSkinThu) tu 05/09/2026 - xem chu
        // thich dau ham kiemHienThiSkin().
        final String ch = doc("src/real/Char.java");
        Kiem.dung("Char co truong isTatSkinLinhThu", ch.contains("private boolean isTatSkinLinhThu = false;"));
        Kiem.dung("Char co ham isTatSkinLinhThu()", ch.contains("public boolean isTatSkinLinhThu() {"));
        Kiem.dung("Char co ham setTatSkinLinhThu()", ch.contains("public void setTatSkinLinhThu() {"));
        Kiem.dung("Char co ham setBatSkinLinhThu()", ch.contains("public void setBatSkinLinhThu() {"));

        // ---- Luu xuong CSDL (cung khuon voi isCT/isCanh - xem KiemTatCaiTrang) ----
        final String luu = doc("src/data/DatabaseNhanVat.java");
        Kiem.dung("cau UPDATE cua saveChar co ghi cot isSkinThu", luu.contains("isSkinThu=?"));
        Kiem.dung("saveChar dat gia tri cho cot isSkinThu (1 = da tat)",
                luu.contains("p.isTatSkinLinhThu() ? 1 : 0"));
        Kiem.dung("charDBID chuyen sang tham so 30 (sau khi them isSkinThu)",
                luu.contains("pre.setInt(30, p.charDBID)"));

        // ---- Nap lai luc dang nhap ----
        final String dbLoad = doc("src/data/Database.java");
        Kiem.dung("luc nap co doc cot isSkinThu", dbLoad.contains("rs.getByte(\"isSkinThu\")"));
        Kiem.dung("nap: 0 = dang bat hien thi", dbLoad.contains("p.setBatSkinLinhThu();"));
        Kiem.dung("nap: khac 0 = da tat hien thi", dbLoad.contains("p.setTatSkinLinhThu();"));

        // Phai doc trong try RIENG (cot moi - may chu chua chay themCotCongTacHienThi()
        // thi rs.getByte nem SQLException giua ham nap nhan vat), va lui ve mac dinh BAT
        // trong catch - giong het lo cot isCT/isCanh.
        final int mocDocSkin = dbLoad.indexOf("if (rs.getByte(\"isSkinThu\") == 0) {");
        final int mocTrySkin = dbLoad.lastIndexOf("try {", mocDocSkin);
        final int mocCatchSkin = dbLoad.indexOf("catch", mocDocSkin);
        Kiem.dung("doc isSkinThu nam trong mot try rieng, ngay sat truoc no",
                mocDocSkin > 0 && mocTrySkin > 0 && mocDocSkin - mocTrySkin < 200);
        Kiem.dung("co nhanh catch ngay sau, lui ve mac dinh dang bat",
                mocCatchSkin > mocDocSkin && mocCatchSkin - mocDocSkin < 300);

        // ---- Tu them cot luc khoi dong (khong ai bi khoa nhan vat vi thieu cot) ----
        Kiem.dung("ham tu them cot co lo cot isSkinThu", dbLoad.contains("themCotNeuThieu(\"isSkinThu\""));

        // NPC Tho Ren Than Bi (npcType -8, menu dang co "Than Khi", "Trang bi thu"...):
        // them dong bat/tat ngay trong menu do, giong ba nut phi phong/mat na/canh co san.
        final String npc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        final int mocCaseTam = npc.indexOf("case -8: {");
        final int mocDongToggle = npc.indexOf("hiển thị skin thú");
        Kiem.dung("menu NPC -8 co dong bat/tat hien thi skin thu", mocDongToggle > 0);
        Kiem.dung("dong toggle nam trong menu cua case -8",
                mocCaseTam > 0 && mocDongToggle > mocCaseTam && mocDongToggle - mocCaseTam < 2000);

        // RealController: option moi phai doi ca hai chieu va gui lai goi tin, dung nhu ba
        // nut phi phong/mat na/canh da co san (case 5/6/7). KHONG chot theo so case cu the:
        // menu NPC -8 con bi sap xep lai boi cong viec khac dang chay song song (da thay
        // "Tinh Hoa Trang Phuc" gop vao case 11, day so case cua toggle nay tu 15 xuong
        // 14) — mot chot theo so cu the se do sai moi lan menu doi thu tu. Neo theo chinh
        // loi goi isTatSkinLinhThu() (chi xuat hien dung mot cho trong file) thay vi so case.
        final String rc = doc("src/real/RealController.java");
        final int mocToggle = rc.indexOf("if (player.isTatSkinLinhThu()) {");
        Kiem.dung("doCreateItem co nhanh toggle hien thi skin thu", mocToggle > 0);
        final int mocSetTat = rc.indexOf("player.setTatSkinLinhThu();", mocToggle);
        final int mocSetBat = rc.indexOf("player.setBatSkinLinhThu();", mocToggle);
        Kiem.dung("nhanh toggle co goi setTatSkinLinhThu gan ke",
                mocToggle > 0 && mocSetTat > mocToggle && mocSetTat - mocToggle < 500);
        Kiem.dung("nhanh toggle co goi setBatSkinLinhThu gan ke",
                mocToggle > 0 && mocSetBat > mocToggle && mocSetBat - mocToggle < 500);
    }

    /**
     * NOI TAI THUONG TRUC cua tung skin: so lieu, va CHO MOC vao ma nguon.
     *
     * Kiem hai tang, vi mot minh tang so lieu la vo dung: bang so co the dung y nguyen
     * trong khi cho doc no da bi xoa mat trong mot lan don ma nao do — luc do skin im
     * lang khong cong gi ma bo kiem van bao dat. Nen moi chi so deu duoc chot them mot
     * lan o dung ham tinh chi so an no.
     *
     * Cac cho moc KHONG the goi that duoc tu day: chung deu can mot Char that (co Session
     * + CSDL). Chot bang van ban ma nguon la thu manh nhat lam duoc trong gioi han do —
     * no bat duoc viec xoa nham hoac doi ten ham, tuc dung loi that su de xay ra.
     *
     * TRAT TU cung duoc chot, khong chi su co mat: hai cho ben duoi ma dat sai vi tri thi
     * chay dung cu phap nhung ra so sai (xem chu thich tung dong).
     */
    private static void kiemNoiTaiThuongTruc() {
        Kiem.nhom("Skin linh thu - noi tai thuong truc (chi so)");

        // ---- Tang 1: so lieu ----
        final int[][] bang = {
            real.SkinLinhThu.CONG_HP,
            real.SkinLinhThu.CONG_MP,
            real.SkinLinhThu.CONG_CHINH_XAC,
            real.SkinLinhThu.PC_XUYEN_GIAP,
            real.SkinLinhThu.PC_THU,
            real.SkinLinhThu.PC_HAP_THU,
        };
        final String[] tenBang = {
            "CONG_HP", "CONG_MP", "CONG_CHINH_XAC", "PC_XUYEN_GIAP", "PC_THU", "PC_HAP_THU",
        };
        for (int i = 0; i < bang.length; i++) {
            Kiem.bang("mang " + tenBang[i] + " du " + real.SkinLinhThu.SO_SKIN + " phan tu",
                    Integer.valueOf(real.SkinLinhThu.SO_SKIN), Integer.valueOf(bang[i].length));
        }

        // So lay tu ban mo ta nguoi choi dua. Doi so trong game thi phai doi ca o day —
        // do la co y: khong ai sua duoc chi so ma quen mat ban mo ta da hua voi nguoi choi.
        Kiem.bang("Phuc Thu +2000 HP", Integer.valueOf(2000),
                Integer.valueOf(real.SkinLinhThu.CONG_HP[0]));
        Kiem.bang("Phuc Thu +3000 MP", Integer.valueOf(3000),
                Integer.valueOf(real.SkinLinhThu.CONG_MP[0]));
        Kiem.bang("Gau Truc +200 chinh xac", Integer.valueOf(200),
                Integer.valueOf(real.SkinLinhThu.CONG_CHINH_XAC[1]));
        Kiem.bang("Xich Duong +5% xuyen giap", Integer.valueOf(5),
                Integer.valueOf(real.SkinLinhThu.PC_XUYEN_GIAP[3]));
        Kiem.bang("Chien Lang +20% thu", Integer.valueOf(20),
                Integer.valueOf(real.SkinLinhThu.PC_THU[4]));
        Kiem.bang("Chien Lang +15% hap thu", Integer.valueOf(15),
                Integer.valueOf(real.SkinLinhThu.PC_HAP_THU[4]));

        // Moi dong chi so chi thuoc ve DUNG MOT skin. Chan loi chep de: dan mot dong sang
        // bang khac ma quen doi chi so thi bon skin con lai bong duoc thom lay.
        final int[] chuSoHuu = {0, 0, 1, 3, 4, 4};
        for (int i = 0; i < bang.length; i++) {
            for (int s = 0; s < bang[i].length; s++) {
                if (s == chuSoHuu[i]) {
                    Kiem.dung(tenBang[i] + " co gia tri o skin " + s, bang[i][s] > 0);
                } else {
                    Kiem.bang(tenBang[i] + " phai bang 0 o skin " + s,
                            Integer.valueOf(0), Integer.valueOf(bang[i][s]));
                }
            }
        }

        // ---- Tang 2: cho moc trong ma nguon ----
        final String chiSo = doc("src/real/CharChiSo.java");
        final String ch = doc("src/real/Char.java");

        Kiem.dung("calculateMaxHPMP cong HP skin", chiSo.contains("self.maxhp += SkinLinhThu.congHp(self)"));
        Kiem.dung("calculateMaxHPMP cong MP skin", chiSo.contains("self.maxmp += SkinLinhThu.congMp(self)"));
        Kiem.dung("calculateDefend cong % thu skin", chiSo.contains("SkinLinhThu.pcThu(self)"));
        Kiem.dung("calculateAccurate cong chinh xac skin",
                ch.contains("final int chinhXacSkin = SkinLinhThu.congChinhXac(this);"));
        // Phai ton trong tran cua game. Chinh xac chan cung o 693 (chia 7, chan 99, nhan
        // 700/100); moi nguon chinh xac khac deu nam duoi do. De skin vuot tran thi no la
        // chi so duy nhat di ra ngoai thang do cua game.
        Kiem.dung("chinh xac skin bi chan boi tran cua game",
                ch.contains("Math.min(tran, this.accurate + chinhXacSkin)"));
        Kiem.dung("tran do khong duoc CAT BOT cai da co (ban do chien truong nhan noi luc)",
                ch.contains("Math.max(TRAN_CHINH_XAC, this.accurate)"));
        // Phai cong THANG vao ket qua cuoi, khong duoc cong vao accurate2 giua ham:
        // accurate2 con phai di qua phep chia 7 va cai tran 99, 200 se con ~28 va thanh 0
        // voi nhan vat da cham tran. Nguoi choi doc "+200" thi phai duoc 200.
        Kiem.sai("KHONG cong chinh xac skin vao accurate2 (se bi chia 7 va chan tran)",
                ch.contains("accurate2 += SkinLinhThu.congChinhXac"));
        final int chanTran = ch.indexOf("this.accurate = (short) (this.accurate * 700 / 100)");
        final int congCx = ch.indexOf("final int chinhXacSkin = SkinLinhThu.congChinhXac(this);");
        Kiem.dung("chinh xac skin cong SAU cai tran 99 va phep nhan 700",
                chanTran > 0 && congCx > chanTran);
        Kiem.dung("calculateAttrib cong xuyen giap skin", ch.contains("SkinLinhThu.pcXuyenGiap(this)"));
        Kiem.dung("checkHapthuSatThuong cong % hap thu skin", ch.contains("SkinLinhThu.pcHapThu(this)"));

        // % thu phai vao ca BON bo dem. Thieu upvP/upmP thi nguoi choi thay +20% trong
        // bang nhan vat nhung vao danh nhau voi nguoi choi khac lai khong duoc gi
        // (defendVatPlayer/defendMaPlayer tinh rieng bang hai bo dem do).
        final int moc = chiSo.indexOf("SkinLinhThu.pcThu(self)");
        final String khoiThu = moc < 0 ? "" : chiSo.substring(moc, Math.min(chiSo.length(), moc + 400));
        Kiem.dung("% thu vao upVat", khoiThu.contains("upVat += pcThuSkin"));
        Kiem.dung("% thu vao upMa", khoiThu.contains("upMa += pcThuSkin"));
        Kiem.dung("% thu vao upvP (thu khi PK)", khoiThu.contains("upvP += pcThuSkin"));
        Kiem.dung("% thu vao upmP (thu khi PK)", khoiThu.contains("upmP += pcThuSkin"));

        // HAP THU PHAI CONG DON, khong duoc gan de. Khoi hap thu cua linh thu truoc day
        // viet "a = ..." nen no xoa sach moi thu cong vao truoc — cu the la 3% noi tai
        // cua Lan. Doi sang "a +=" thi ca ba nguon (Lan, linh thu, skin) deu duoc tinh.
        Kiem.sai("khoi hap thu cua linh thu KHONG con gan de",
                ch.contains("a = finalDam * (animalRide.att[9]"));
        Kiem.dung("khoi hap thu cua linh thu cong don",
                ch.contains("a += finalDam * (animalRide.att[9]"));

        // TRAT TU 2 — HP/MP: phai cong SAU cac khoi nhan phan tram (fullset, pet, noi luc,
        // cai tao). Cong truoc thi 2000 mau bi nhan len theo, ra nhieu hon con so da hua.
        final int nhanNoiLuc = chiSo.indexOf("self.maxhp = (int) (self.maxhp * noiluc / 10L)");
        final int congHp = chiSo.indexOf("self.maxhp += SkinLinhThu.congHp(self)");
        Kiem.dung("cong HP skin nam SAU khoi nhan phan tram noi luc",
                nhanNoiLuc > 0 && congHp > nhanNoiLuc);
        // ... va TRUOC hai dong chan, khong thi hp co the vuot maxhp.
        final int chanHp = chiSo.indexOf("if (self.hp > self.maxhp)");
        Kiem.dung("cong HP skin nam TRUOC dong chan hp", chanHp > 0 && congHp < chanHp);

        // ---- Rao chi so cua checkKick5AttAnimal ----
        // groupKickAnimal chi dai 5 (type 14..18) ma skin mang type 30 -> chi so 16.
        // Khong rao thi ArrayIndexOutOfBounds, va no roi vao mot catch RONG nen im lang.
        Kiem.dung("groupKickAnimal van chi phu type 14..18",
                ch.contains("groupKickAnimal = new byte[][]{{18}, {16}, {14}, {15}, {17}}"));
        Kiem.dung("TYPE_ITEM cua skin nam NGOAI dai do (nen phai rao)",
                real.SkinLinhThu.TYPE_ITEM - 14 >= 5);
        Kiem.dung("checkKick5AttAnimal co rao chi so truoc khi tra bang",
                ch.contains("loaiTrangBiThu >= 0 && loaiTrangBiThu < groupKickAnimal.length"));
        // Rao bang "continue" se treo may chu: vong lap tang bien dem o CUOI than vong.
        final int moc2 = ch.indexOf("public void checkKick5AttAnimal()");
        final String khoiRao = moc2 < 0 ? "" : ch.substring(moc2, Math.min(ch.length(), moc2 + 2200));
        Kiem.sai("rao KHONG dung continue (se treo vong lap)", khoiRao.contains("continue;"));
    }

    /**
     * TIEN LOC: +20% diem tiem nang, cong TAI CHO chu khong ghi vao chi so goc.
     *
     * Cho nguy hiem nhat cua ca tinh nang nay: bon truong strength/agitity/spirit/health
     * duoc GHI XUONG CSDL nguyen si (DatabaseNhanVat gop chuoi "strength,agitity,..."),
     * nen cach lam "nang len - tinh - tra lai" se hong vinh vien neu co mot luong khac
     * luu nhan vat dung khoang giua. Phep kiem duoi day chot lai rang KHONG co cho nao
     * ghi vao bon truong do ngoai dung cho dat lai chi so goc.
     *
     * Cai gia cua cach tinh tai cho la moi cong thuc phai cong tay mot lan. Nen o day
     * dem so cho, va dem la de doi: thieu mot cho thi chi so do lang le khong duoc cong,
     * khong loi, khong cach nao thay tru khi ngoi thu tung mon phai.
     */
    private static void kiemTiemNang() {
        Kiem.nhom("Skin linh thu - Tien Loc +20% diem tiem nang");

        Kiem.bang("mang PC_TIEM_NANG du 5 phan tu", Integer.valueOf(real.SkinLinhThu.SO_SKIN),
                Integer.valueOf(real.SkinLinhThu.PC_TIEM_NANG.length));
        Kiem.bang("Tien Loc +20%", Integer.valueOf(20),
                Integer.valueOf(real.SkinLinhThu.PC_TIEM_NANG[2]));
        for (int s = 0; s < real.SkinLinhThu.SO_SKIN; s++) {
            if (s != 2) {
                Kiem.bang("skin " + s + " khong cong tiem nang",
                        Integer.valueOf(0), Integer.valueOf(real.SkinLinhThu.PC_TIEM_NANG[s]));
            }
        }

        final String ch = doc("src/real/Char.java");
        final String chiSo = doc("src/real/CharChiSo.java");
        final String mc = doc("src/real/MessageCreator.java");

        // NAM ham tinh tai cho, khong phai bon.
        //
        // Nhan vat co NAM diem tiem nang: Database.basicPoint[class][0..4] = suc manh,
        // kheo leo, tinh than, suc khoe, MAY MAN. Ban dau toi bo sot may man, nen "+20%
        // diem tiem nang" cong cho bon cai dau ma CHI MANG dung im (crit1 = (luck+..)/5)
        // va bang chi so hien mot dong khong nhuc nhich giua bon dong tang.
        final String[] ham = {"tnSucManh", "tnKheoLeo", "tnTinhThan", "tnSucKhoe", "tnMayMan"};
        for (final String h : ham) {
            Kiem.dung("Char co ham " + h + "()", ch.contains("public int " + h + "()"));
        }

        // KHONG duoc ghi vao chi so goc: chi duy nhat cho dat lai (Database.basicPoint)
        // moi duoc gan. Bat cu phep gan nao khac la duong dan toi hong CSDL.
        Kiem.bang("chi co 1 cho gan this.strength (cho dat lai chi so goc)",
                Integer.valueOf(1), Integer.valueOf(dem(ch, "this.strength = ")));
        Kiem.bang("chi co 1 cho gan this.agitity", Integer.valueOf(1),
                Integer.valueOf(dem(ch, "this.agitity = ")));
        Kiem.bang("chi co 1 cho gan this.spirit", Integer.valueOf(1),
                Integer.valueOf(dem(ch, "this.spirit = ")));
        Kiem.bang("chi co 1 cho gan this.health", Integer.valueOf(1),
                Integer.valueOf(dem(ch, "this.health = ")));
        Kiem.dung("cho gan do la cho dat lai theo Database.basicPoint",
                ch.contains("this.strength = (short) (Database.basicPoint"));

        // Dem so cho da cong. Thieu mot cho = mot cong thuc lang le khong duoc cong.
        // 25 CHO chu khong phai 25 dong: co dong chua hai chi so cung luc, vi du
        // "if (self.strength >= self.agitity)" va cong thuc tan cong cua Phap su.
        final int soChoChiSo = dem(chiSo, "tnSucManh()") + dem(chiSo, "tnKheoLeo()")
                + dem(chiSo, "tnTinhThan()") + dem(chiSo, "tnSucKhoe()");
        Kiem.bang("CharChiSo cong du 25 cho", Integer.valueOf(25), Integer.valueOf(soChoChiSo));
        Kiem.bang("Char cong du 2 cho (ne tranh + chinh xac)",
                Integer.valueOf(2), Integer.valueOf(dem(ch, "this.agitity + this.tnKheoLeo()")));

        // Khong con cho nao doc chi so goc ma QUEN cong. Bon truong nay trong CharChiSo
        // chi duoc phep xuat hien ben trong dau ngoac "(self.X + self.tnY())".
        final String[] truong = {"self.strength", "self.agitity", "self.spirit", "self.health"};
        final String[] kem = {"self.tnSucManh()", "self.tnKheoLeo()", "self.tnTinhThan()", "self.tnSucKhoe()"};
        for (int i = 0; i < truong.length; i++) {
            Kiem.bang("moi cho doc " + truong[i] + " deu co cong tiem nang",
                    Integer.valueOf(dem(chiSo, truong[i])), Integer.valueOf(dem(chiSo, kem[i])));
        }

        // Bang chi so nguoi choi NHIN THAY cung phai cong, khong thi ho thay so cu trong
        // khi sat thuong da tang — khong the tu kiem chung la skin dang chay.
        Kiem.dung("bang chi so gui client cong suc manh", mc.contains("p.strength + p.tnSucManh()"));
        Kiem.dung("bang chi so gui client cong kheo leo", mc.contains("p.agitity + p.tnKheoLeo()"));
        Kiem.dung("bang chi so gui client cong tinh than", mc.contains("p.spirit + p.tnTinhThan()"));
        Kiem.dung("bang chi so gui client cong suc khoe", mc.contains("p.health + p.tnSucKhoe()"));
        Kiem.dung("bang chi so gui client cong may man", mc.contains("p.luck + p.tnMayMan()"));

        // MAY MAN nuoi CHI MANG. Bo sot cho nay thi bon chi so kia tang ma chi mang dung
        // im — nguoi choi nhin bang la thay ngay co gi do sai.
        Kiem.dung("chi mang cong may man tu skin",
                ch.contains("(this.luck + this.tnMayMan())"));
        Kiem.bang("khong con cho nao doc luck ma quen cong tiem nang (trong Char)",
                Integer.valueOf(dem(ch, "this.luck + this.tnMayMan()")),
                Integer.valueOf(dem(ch, "this.luck +")));
    }

    /**
     * HE NGU HANH: cuoi skin HOP HE voi mon phai thi cong sat thuong.
     *
     * Hop he = he cua SKIN sinh ra he cua MON PHAI, theo vong tuong sinh
     * Moc -> Hoa -> Tho -> Kim -> Thuy -> Moc.
     *
     * Nam cap skin/mon phai duoi day duoc chep tu chinh ban mo ta nguoi choi dua ra, va
     * doi chieu doc lap voi phep tinh sinh(). Neu ai do doi mot so trong bang HE hay
     * HE_CUA_MON_PHAI thi mot trong hai ben se lech va phep kiem bao ngay — chu khong doi
     * den luc nguoi choi phat hien skin cua ho khong len sat thuong.
     */
    private static void kiemHeNguHanh() {
        Kiem.nhom("Skin linh thu - he ngu hanh va cong sat thuong khi hop he");

        Kiem.bang("co 5 he", Integer.valueOf(5), Integer.valueOf(real.SkinLinhThu.TEN_HE.length));
        Kiem.bang("mang HE du 5 phan tu", Integer.valueOf(real.SkinLinhThu.SO_SKIN),
                Integer.valueOf(real.SkinLinhThu.HE.length));
        Kiem.bang("mang HE_CUA_MON_PHAI du 5 mon phai", Integer.valueOf(5),
                Integer.valueOf(real.SkinLinhThu.HE_CUA_MON_PHAI.length));

        // Vong tuong sinh phai kin: di 5 buoc tu bat ky he nao phai ve dung cho cu, va
        // khong he nao sinh ra chinh no.
        for (int h = 0; h < real.SkinLinhThu.TEN_HE.length; h++) {
            int di = h;
            for (int b = 0; b < real.SkinLinhThu.TEN_HE.length; b++) {
                di = real.SkinLinhThu.sinh(di);
            }
            Kiem.bang("di het vong tu he " + h + " thi ve cho cu",
                    Integer.valueOf(h), Integer.valueOf(di));
            Kiem.sai("he " + h + " khong sinh ra chinh no", real.SkinLinhThu.sinh(h) == h);
        }

        // Ten he phai dung thu tu vong tuong sinh Moc -> Hoa -> Tho -> Kim -> Thuy.
        final String[] vong = {"Mộc", "Hỏa", "Thổ", "Kim", "Thủy"};
        for (int h = 0; h < vong.length; h++) {
            Kiem.bang("he " + h + " la " + vong[h], vong[h], real.SkinLinhThu.TEN_HE[h]);
        }

        // Nam cap chep tu ban mo ta nguoi choi dua: {skin, mon phai hop}.
        // 0 Kiem khach, 1 Chien binh, 2 Phap su, 3 Dau si, 4 Cung thu.
        final int[][] capTheoBanMoTa = {
            {0, 2}, // Phuc Thu  (Kim)  -> Phap su   (Thuy),  Kim sinh Thuy
            {1, 1}, // Gau Truc  (Moc)  -> Chien binh (Hoa),  Moc sinh Hoa
            {2, 4}, // Tien Loc  (Thuy) -> Cung thu  (Moc),   Thuy sinh Moc
            {3, 3}, // Xich Duong (Hoa) -> Dau si    (Tho),   Hoa sinh Tho
            {4, 0}, // Chien Lang (Tho) -> Kiem khach (Kim),  Tho sinh Kim
        };
        for (final int[] cap : capTheoBanMoTa) {
            Kiem.bang("skin " + cap[0] + " (" + real.SkinLinhThu.TEN[cap[0]] + ") hop mon phai "
                            + cap[1], Integer.valueOf(cap[1]),
                    Integer.valueOf(real.SkinLinhThu.monPhaiHopVoiSkin(cap[0])));
        }

        // Moi mon phai phai co DUNG MOT skin hop. Thieu thi mot mon phai khong co lua
        // chon nao, trung thi mot mon phai co hai con con mon khac khong co con nao.
        final int[] dem = new int[5];
        for (int s = 0; s < real.SkinLinhThu.SO_SKIN; s++) {
            if (s == real.SkinLinhThu.KIM_CANG_LONG) {
                // Kim Cang Long dung lai he Kim cua Phuc Thu (5 he da het cho 5 skin dau,
                // khong con he trong) nhung KHONG co tac dung chi so hopHe nao - chi so cua
                // no ngau nhien theo vat pham, khong doc qua noiTaiHopHe. Khong tinh vao
                // dieu kien "moi mon phai dung 1 skin hop" ben duoi.
                continue;
            }
            final int mp = real.SkinLinhThu.monPhaiHopVoiSkin(s);
            Kiem.dung("skin " + s + " co mon phai hop hop le", mp >= 0 && mp < 5);
            if (mp >= 0 && mp < 5) {
                dem[mp]++;
            }
        }
        for (int mp = 0; mp < 5; mp++) {
            Kiem.bang("mon phai " + mp + " co dung 1 skin hop",
                    Integer.valueOf(1), Integer.valueOf(dem[mp]));
        }

        // Khong con co che +% sat thuong chung khi hop he (nguoi choi yeu cau bo, ban mo ta
        // 5 skin khong nhac dong nay) — chan cho applyThanKhiOnHit KHONG con dong nao goi
        // ham do nua, tranh ai do vo tinh khoi phuc lai mot manh cu.
        final String chiSo = doc("src/real/CharChiSo.java");
        Kiem.sai("applyThanKhiOnHit khong con cong sat thuong rieng khi hop he",
                chiSo.contains("pcSatThuongHopHe"));

        // ---- hopHe() gio la CONG TAC cho nhom "Tac dung chi so": xem SkinLinhThu.java ----
        final String db = doc("src/real/SkinLinhThu.java");
        // Bon chi so tinh: chinh xac/tiem nang/xuyen giap/thu PHAI di qua noiTaiHopHe —
        // sai he thi tra 0, dung y "Tac dung chi so" chi bat khi hop he.
        Kiem.dung("congChinhXac dung noiTaiHopHe (Gau Truc, can hop he)",
                db.contains("public static int congChinhXac(final Char p) {\n        return noiTaiHopHe(CONG_CHINH_XAC, p);"));
        Kiem.dung("pcXuyenGiap dung noiTaiHopHe (Xich Duong, can hop he)",
                db.contains("public static int pcXuyenGiap(final Char p) {\n        return noiTaiHopHe(PC_XUYEN_GIAP, p);"));
        Kiem.dung("pcTiemNang dung noiTaiHopHe (Tien Loc, can hop he)",
                db.contains("public static int pcTiemNang(final Char p) {\n        return noiTaiHopHe(PC_TIEM_NANG, p);"));
        Kiem.dung("pcThu dung noiTaiHopHe (Chien Lang, can hop he)",
                db.contains("public static int pcThu(final Char p) {\n        return noiTaiHopHe(PC_THU, p);"));
        // Ba dong "Buff bi dong" (HP/MP cua Phuc Thu, hap thu cua Chien Lang) PHAI van di
        // qua noiTai thuong (luon bat, khong doi hop he) — khong duoc lay nham sang gated.
        Kiem.dung("congHp van dung noiTai thuong (Buff bi dong, luon bat)",
                db.contains("public static int congHp(final Char p) {\n        return noiTai(CONG_HP, p);"));
        Kiem.dung("congMp van dung noiTai thuong (Buff bi dong, luon bat)",
                db.contains("public static int congMp(final Char p) {\n        return noiTai(CONG_MP, p);"));
        Kiem.dung("pcHapThu van dung noiTai thuong (Buff bi dong, luon bat)",
                db.contains("public static int pcHapThu(final Char p) {\n        return noiTai(PC_HAP_THU, p);"));
    }

    /**
     * NOI TAI CO TI LE KICH HOAT VA HOI CHIEU — bon dong con lai cua ban mo ta.
     *
     * Khong the goi that duoc tu day (can Char that co Session + CSDL), nen kiem hai thu
     * kiem duoc va deu la cho de sai: SO LIEU dung ban mo ta, va CAC BAT BIEN ma neu vo
     * thi khong ai phat hien bang mat thuong.
     */
    private static void kiemNoiTaiCoTiLe() {
        Kiem.nhom("Skin linh thu - noi tai co ti le va hoi chieu");

        final int n = real.SkinLinhThu.SO_SKIN;
        final int[][] bang = {
            real.SkinLinhThu.DIEU_KIEN, real.SkinLinhThu.PC_KICH_HOAT,
            real.SkinLinhThu.MS_HIEU_LUC, real.SkinLinhThu.MS_HOI_CHIEU,
            real.SkinLinhThu.ID_HIEU_UNG, real.SkinLinhThu.PC_CHAN_ST_NOI_TAI,
            real.SkinLinhThu.PC_CONG_PST_NOI_TAI, real.SkinLinhThu.PC_CONG_TAN_CONG_NOI_TAI,
        };
        final String[] tenBang = {
            "DIEU_KIEN", "PC_KICH_HOAT", "MS_HIEU_LUC", "MS_HOI_CHIEU", "ID_HIEU_UNG",
            "PC_CHAN_ST_NOI_TAI", "PC_CONG_PST_NOI_TAI", "PC_CONG_TAN_CONG_NOI_TAI",
        };
        for (int i = 0; i < bang.length; i++) {
            Kiem.bang("mang " + tenBang[i] + " du " + n + " phan tu",
                    Integer.valueOf(n), Integer.valueOf(bang[i].length));
        }
        Kiem.bang("mang BAO_KICH_HOAT du " + n + " phan tu",
                Integer.valueOf(n), Integer.valueOf(real.SkinLinhThu.BAO_KICH_HOAT.length));
        Kiem.bang("mang NOI_TAI_CAN_HOP_HE du " + n + " phan tu",
                Integer.valueOf(n), Integer.valueOf(real.SkinLinhThu.NOI_TAI_CAN_HOP_HE.length));

        // Chi Phuc Thu (skin 0) can hop he moi kich hoat noi tai co ti le — do la dong
        // "Tac dung chi so" cua no theo ban mo ta. Bon skin con lai la "Buff bi dong".
        for (int s = 0; s < n; s++) {
            Kiem.bang("skin " + s + " NOI_TAI_CAN_HOP_HE = " + (s == 0),
                    Boolean.valueOf(s == 0), Boolean.valueOf(real.SkinLinhThu.NOI_TAI_CAN_HOP_HE[s]));
        }

        // ---- So lieu, chep tu ban mo ta nguoi choi dua ----
        Kiem.bang("Phuc Thu: bi class vat ly danh", Integer.valueOf(real.SkinLinhThu.DK_BI_VAT_LY),
                Integer.valueOf(real.SkinLinhThu.DIEU_KIEN[0]));
        Kiem.bang("Phuc Thu: chan 30% ST", Integer.valueOf(30),
                Integer.valueOf(real.SkinLinhThu.PC_CHAN_ST_NOI_TAI[0]));
        Kiem.bang("Phuc Thu: ti le 15%", Integer.valueOf(15),
                Integer.valueOf(real.SkinLinhThu.PC_KICH_HOAT[0]));
        Kiem.bang("Phuc Thu: 3 giay", Integer.valueOf(3000),
                Integer.valueOf(real.SkinLinhThu.MS_HIEU_LUC[0]));

        Kiem.bang("Gau Truc: bi class phep danh", Integer.valueOf(real.SkinLinhThu.DK_BI_PHEP),
                Integer.valueOf(real.SkinLinhThu.DIEU_KIEN[1]));
        Kiem.bang("Gau Truc: PST +50 diem", Integer.valueOf(50),
                Integer.valueOf(real.SkinLinhThu.PC_CONG_PST_NOI_TAI[1]));
        Kiem.bang("Gau Truc: tran PST 80%", Integer.valueOf(80),
                Integer.valueOf(real.SkinLinhThu.PC_TRAN_PST));
        Kiem.bang("Gau Truc: ti le 15%", Integer.valueOf(15),
                Integer.valueOf(real.SkinLinhThu.PC_KICH_HOAT[1]));

        Kiem.bang("Tien Loc: bi bat cu ai danh", Integer.valueOf(real.SkinLinhThu.DK_BI_DANH),
                Integer.valueOf(real.SkinLinhThu.DIEU_KIEN[2]));
        Kiem.bang("Tien Loc: la chan 50%", Integer.valueOf(50),
                Integer.valueOf(real.SkinLinhThu.PC_CHAN_ST_NOI_TAI[2]));
        Kiem.bang("Tien Loc: ti le 10%", Integer.valueOf(10),
                Integer.valueOf(real.SkinLinhThu.PC_KICH_HOAT[2]));
        Kiem.bang("Tien Loc: 5 giay", Integer.valueOf(5000),
                Integer.valueOf(real.SkinLinhThu.MS_HIEU_LUC[2]));

        Kiem.bang("Xich Duong: mau thap", Integer.valueOf(real.SkinLinhThu.DK_HP_THAP),
                Integer.valueOf(real.SkinLinhThu.DIEU_KIEN[3]));
        Kiem.bang("Xich Duong: nguong 30% mau", Integer.valueOf(30),
                Integer.valueOf(real.SkinLinhThu.PC_NGUONG_HP));
        Kiem.bang("Xich Duong: +50% tan cong", Integer.valueOf(50),
                Integer.valueOf(real.SkinLinhThu.PC_CONG_TAN_CONG_NOI_TAI[3]));
        Kiem.bang("Xich Duong: 3 giay", Integer.valueOf(3000),
                Integer.valueOf(real.SkinLinhThu.MS_HIEU_LUC[3]));

        Kiem.bang("Chien Lang khong co noi tai loai nay (15% hap thu luon bat)",
                Integer.valueOf(0), Integer.valueOf(real.SkinLinhThu.PC_KICH_HOAT[4]));
        Kiem.bang("Chien Lang khong co hieu ung hinh", Integer.valueOf(-1),
                Integer.valueOf(real.SkinLinhThu.ID_HIEU_UNG[4]));

        // ---- Bat bien 1: HOI CHIEU PHAI DAI HON THOI GIAN HIEU LUC ----
        // Nguoc lai thi noi tai chay lien tuc va ti le/hoi chieu thanh trang tri.
        for (int s = 0; s < n; s++) {
            if (real.SkinLinhThu.PC_KICH_HOAT[s] > 0) {
                Kiem.dung("skin " + s + ": hoi chieu dai hon thoi gian hieu luc",
                        real.SkinLinhThu.MS_HOI_CHIEU[s] > real.SkinLinhThu.MS_HIEU_LUC[s]);
            }
        }

        // ---- Bat bien 2: MOI SKIN CO DUNG MOT LOAI HIEU LUC ----
        // Hai loai cung luc thi mo ta vat pham chi ghi duoc mot (moTaNoiTai dung if/else),
        // nguoi choi doc thieu mat mot dong.
        for (int s = 0; s < n; s++) {
            int soLoai = 0;
            if (real.SkinLinhThu.PC_CHAN_ST_NOI_TAI[s] > 0) {
                soLoai++;
            }
            if (real.SkinLinhThu.PC_CONG_PST_NOI_TAI[s] > 0) {
                soLoai++;
            }
            if (real.SkinLinhThu.PC_CONG_TAN_CONG_NOI_TAI[s] > 0) {
                soLoai++;
            }
            Kiem.bang("skin " + s + " co dung " + (real.SkinLinhThu.PC_KICH_HOAT[s] > 0 ? 1 : 0)
                            + " loai hieu luc noi tai",
                    Integer.valueOf(real.SkinLinhThu.PC_KICH_HOAT[s] > 0 ? 1 : 0),
                    Integer.valueOf(soLoai));
        }

        // ---- Bat bien 3: HAI NHOM MON PHAI PHU KIN VA KHONG CHONG NHAU ----
        // Ban mo ta ghi "CB - KK - CT" cho Phuc Thu, nhung tren may chu nay Cung thu la
        // class danh don PHEP (Char.isCharMagic tra true cho class 2 va 4). Nguoi choi da
        // chot lai la Chien binh, Kiem khach, DAU SI. Neu ai do sua theo ban mo ta thi
        // Cung thu se nam ca hai nhom va Dau si khong nam nhom nao — chot lai o day.
        for (int mp = 0; mp < 5; mp++) {
            final boolean vat = real.SkinLinhThu.laClassVatLy(mp);
            final boolean phep = real.SkinLinhThu.laClassPhep(mp);
            Kiem.dung("mon phai " + mp + " thuoc dung mot nhom", vat != phep);
        }
        Kiem.dung("Kiem khach danh don vat ly", real.SkinLinhThu.laClassVatLy(0));
        Kiem.dung("Chien binh danh don vat ly", real.SkinLinhThu.laClassVatLy(1));
        Kiem.dung("Dau si danh don vat ly", real.SkinLinhThu.laClassVatLy(3));
        Kiem.dung("Phap su danh don phep", real.SkinLinhThu.laClassPhep(2));
        Kiem.dung("Cung thu danh don phep (KHONG phai vat ly)", real.SkinLinhThu.laClassPhep(4));

        final String ch = doc("src/real/Char.java");
        // Nhom "danh don phep" phai khop dung Char.isCharMagic, khong duoc lech.
        Kiem.dung("isCharMagic van la class 2 va 4",
                ch.contains("return this.charClass == 2 || this.charClass == 4;"));

        // ---- Bat bien 4: HIEU UNG HINH PHAI KHONG CO CO CHE KEM THEO ----
        // EFF_SHIELD_1/2 nghe hop nhung Char.getPcGiamSatThuong va getBackDam doc chung
        // va tu dong cho 15%/30% giam sat thuong + 20%/30% phan don. Dung chung thi muon
        // 30% ma an 45%, khong ai biet la sai.
        for (int s = 0; s < n; s++) {
            final int id = real.SkinLinhThu.ID_HIEU_UNG[s];
            Kiem.sai("hieu ung skin " + s + " khong phai EFF_SHIELD_1",
                    id == real.EffectBuff.EFF_SHIELD_1);
            Kiem.sai("hieu ung skin " + s + " khong phai EFF_SHIELD_2",
                    id == real.EffectBuff.EFF_SHIELD_2);
        }
        final String[] tenHieuUng = {"LIGHT_YELLOW", "LIGHT_MAGENTA", "BUFF_DAM_10", "BUFF_DAM_5"};
        for (final String t : tenHieuUng) {
            Kiem.sai("Char.java khong gan co che nao cho " + t, ch.contains(t));
        }

        // ---- Cho moc trong ma nguon ----
        final String chiSo = doc("src/real/CharChiSo.java");
        Kiem.dung("kich hoat noi tai trong checkHapthuSatThuong",
                ch.contains("SkinLinhThu.thuKichHoat(this, actor, finalDam)"));
        Kiem.dung("chan sat thuong theo noi tai", ch.contains("SkinLinhThu.pcChanSatThuong(this)"));
        Kiem.dung("cong phan sat thuong theo noi tai",
                ch.contains("SkinLinhThu.pcCongPhanSatThuong(temp)"));
        Kiem.dung("cong tan cong theo noi tai", chiSo.contains("SkinLinhThu.pcCongTanCong(self)"));

        // Kich hoat PHAI nam trong checkHapthuSatThuong — cho duy nhat moi duong sat
        // thuong danh vao nhan vat deu di qua (PvP, quai thuong, moi lop Boss). Moc o
        // subDam se sot het duong boss.
        final int mocHam = ch.indexOf("public int checkHapthuSatThuong");
        final int mocKichHoat = ch.indexOf("SkinLinhThu.thuKichHoat(this, actor, finalDam)");
        Kiem.dung("kich hoat nam trong checkHapthuSatThuong",
                mocHam > 0 && mocKichHoat > mocHam && mocKichHoat - mocHam < 4000);

        // Tran 80% chi duoc ap KHI noi tai gop vao. Ap vo dieu kien se am tham cat bot
        // phan sat thuong cua nguoi da co tren 80% tu trang bi, du ho khong mac skin nao.
        Kiem.dung("tran PST chi ap khi noi tai gop vao", ch.contains("if (pcPstSkin > 0) {"));

        // Duong sat thuong danh vao phai con day du ca nam cho goi checkHapthuSatThuong.
        final String[] duong = {
            "src/real/MapDanhNhau.java", "src/real/MapLienDau.java", "src/real/Region.java",
            "src/real/RegionVantienTran.java", "src/real/Monster.java",
        };
        for (final String d : duong) {
            Kiem.dung(d + " van goi checkHapthuSatThuong",
                    doc(d).contains("checkHapthuSatThuong("));
        }

        // thoaDieuKien phai chan theo NOI_TAI_CAN_HOP_HE (sai he thi noi tai co ti le cua
        // Phuc Thu khong kich hoat) TRUOC khi doc DIEU_KIEN.
        final String db = doc("src/real/SkinLinhThu.java");
        final int mocThoaDK = db.indexOf("public static boolean thoaDieuKien(");
        final int mocHopHeGate = db.indexOf("NOI_TAI_CAN_HOP_HE[p.idSkinLinhThu] && !hopHe(p)");
        final int mocDocDieuKien = db.indexOf("final int dk = DIEU_KIEN[p.idSkinLinhThu];");
        Kiem.dung("thoaDieuKien co chan theo NOI_TAI_CAN_HOP_HE",
                mocThoaDK > 0 && mocHopHeGate > mocThoaDK);
        Kiem.dung("chan hop he nam TRUOC khi doc DIEU_KIEN",
                mocHopHeGate > 0 && mocDocDieuKien > mocHopHeGate);
    }

    /**
     * PHAI TINH LAI CHI SO moi khi trang thai skin doi — va tinh DUNG LUC.
     *
     * LOI DA XAY RA THAT, nguoi choi bao: "tang tiem nang nhung tan cong khong tang".
     *
     * Goc: attack / maxhp / defend_physic / accurate deu la TRUONG DUOC LUU SAN, chi doi
     * khi calculateAttrib() chay. Con bang diem tiem nang lai duoc tinh TUOI ngay luc dung
     * goi tin (MessageCreator cong p.tnSucManh() tai cho). Nen khi thieu loi goi
     * calculateAttrib, nguoi choi THAY tiem nang tang ma moi chi so dan xuat dung im —
     * trong nhu skin chi cong mot nua.
     *
     * Ba lo, khong phai mot:
     *   1. dongBoVaGui khong goi calculateAttrib -> mac/thao skin khong an gi.
     *   2. doRideAnimal goi calculateAttrib TRUOC khi dong bo skin -> len thu khong an gi.
     *   3. SelectCharHandler khong goi lai sau khi dong bo -> dang nhap khong an gi.
     *
     * Ly do sau xa: SkinLinhThu.dangMang phu thuoc vao CA animalRide, ma viec tinh lai
     * chi duoc kich khi idSkinLinhThu doi. Len thu thi so hieu skin KHONG doi (da co san
     * tu luc dang nhap) — cai doi la animalRide. Nen o duong cuoi thu, loi goi
     * calculateAttrib phai VO DIEU KIEN, khong duoc gan vao "dongBoTheoTrangBi tra true".
     *
     * Phep kiem duoi day chot THU TU chu khong chi su co mat: dung thu tu thi chay dung
     * cu phap ma van khong an gi, y het luc chua sua.
     */
    private static void kiemTinhLaiChiSo() {
        Kiem.nhom("Skin linh thu - tinh lai chi so khi trang thai skin doi");

        final String db = doc("src/real/SkinLinhThu.java");
        final String cuoi = doc("src/real/doRideAnimal.java");
        final String xuong = doc("src/real/doUnRideHorse.java");
        final String dangNhap = doc("src/real/cmd/SelectCharHandler.java");

        // --- 1. Mac / thao skin ---
        Kiem.dung("dongBoVaGui goi calculateAttrib", db.contains("p.calculateAttrib();"));
        final int mocDongBo = db.indexOf("public static void dongBoVaGui");
        final int mocTinh = db.indexOf("p.calculateAttrib();", mocDongBo);
        final int mocGui = db.indexOf("createMainCharInfoMessage(p)", mocDongBo);
        Kiem.dung("loi goi do nam trong dongBoVaGui", mocDongBo > 0 && mocTinh > mocDongBo);
        Kiem.dung("tinh lai TRUOC khi dung goi tin (khong thi goi dau tien mang so cu)",
                mocTinh > 0 && mocGui > mocTinh);

        // --- 2. Cuoi thu ---
        final int cuoiDongBo = cuoi.indexOf("SkinLinhThu.dongBoTheoTrangBi(player)");
        final int cuoiTinh = cuoi.indexOf("player.calculateAttrib()");
        Kiem.dung("doRideAnimal co dong bo skin", cuoiDongBo > 0);
        Kiem.dung("doRideAnimal co tinh lai chi so", cuoiTinh > 0);
        Kiem.dung("cuoi thu: tinh lai chi so SAU khi dong bo skin (khong phai truoc)",
                cuoiTinh > cuoiDongBo);

        // --- 2b. MAC / THAO skin o bang "Trang bi thu" (Map.exchangeItem) ---
        //
        // DUONG THU TU, BI SOT LUC DAU. Loi da xay ra that (05/09/2026): nguoi choi mac
        // skin vao thi bang Tiem nang tang ma Tan cong dung im. O day calculateAttrib()
        // nam TREN dong dongBoTheoTrangBi, nen no tinh bang idSkinLinhThu CU; con bang
        // tiem nang thi MessageCreator cong tnKheoLeo() tai cho nen doc gia tri MOI ->
        // lo ra dung mot nua. Day la duong nguoi choi dung NHIEU NHAT (mac/thao bang
        // giao dien), nhung ba phep kiem tren khong cham toi no.
        final String doiDo = doc("src/real/Map.java");
        final int mocExDongBo = doiDo.indexOf("SkinLinhThu.dongBoTheoTrangBi(player)");
        Kiem.dung("Map.exchangeItem co dong bo skin", mocExDongBo > 0);
        if (mocExDongBo > 0) {
            // Loi goi calculateAttrib GAN NHAT truoc/sau dong bo, trong cung khoi.
            final int mocExTinh = doiDo.indexOf("player.calculateAttrib();", mocExDongBo);
            final int mocExGui = doiDo.indexOf("createMainCharInfoMessage(player)", mocExDongBo);
            Kiem.dung("mac/thao skin: tinh lai chi so SAU khi dong bo skin (khong phai truoc)",
                    mocExTinh > mocExDongBo);
            Kiem.dung("mac/thao skin: tinh lai TRUOC khi dung goi tin",
                    mocExTinh > 0 && mocExGui > mocExTinh);
        }

        // --- 3. Dang nhap ---
        final int dnDongBo = dangNhap.indexOf("SkinLinhThu.dongBoTheoTrangBi(p)");
        final int dnTinh = dangNhap.indexOf("p.calculateAttrib();", dnDongBo);
        final int dnGui = dangNhap.indexOf("createMainCharInfoMessage(p)", dnDongBo);
        Kiem.dung("dang nhap: co dong bo skin", dnDongBo > 0);
        Kiem.dung("dang nhap: tinh lai chi so sau khi dong bo",
                dnTinh > dnDongBo);
        Kiem.dung("dang nhap: tinh lai TRUOC goi thong tin nhan vat dau tien",
                dnTinh > 0 && dnGui > dnTinh);

        // --- 4. Xuong thu ---
        final int xThao = xuong.indexOf("SkinLinhThu.thaoSkin(player)");
        final int xTinh = xuong.indexOf("player.calculateAttrib()");
        Kiem.dung("xuong thu: co go skin", xThao > 0);
        Kiem.dung("xuong thu: tinh lai chi so SAU khi go skin", xTinh > xThao);
    }

    /**
     * DO THAT SU chi so nhan vat truoc/sau khi mang tung skin.
     *
     * Day la phep kiem manh nhat cua ca tinh nang: no dung mot Char that, chay
     * calculateAttrib() that, roi so SO. Moi phep kiem khac trong tep nay chi do van ban
     * ma nguon — chung bat duoc "quen goi ham" nhung KHONG bat duoc "goi dung ma ra so
     * sai". Chinh loai sau moi la thu nguoi choi nhin thay.
     *
     * Da bat duoc that: bo sot diem tiem nang thu nam lam chi mang dung im, va chinh xac
     * cua Gau Truc vuot ra ngoai tran cua game.
     *
     * DUNG CHI SO KHIEM TON (200) chu khong dung so lon: chinh xac chan tran o 693 va ne
     * tranh chan o 299, nhan vat chi so cao thi cham tran het va moi thay doi bi cai tran
     * nuot mat — phep kiem se "dat" ma khong chung minh duoc gi.
     */
    private static void kiemChiSoThucTe() {
        Kiem.nhom("Skin linh thu - do that su chi so tren Char that");

        if (!buDuLieuAnhSkin()) {
            Kiem.dung("bu duoc du lieu anh skin de dangMang() tra true", false);
            return;
        }
        buGemTemplate();

        for (int mp = 0; mp < 5; mp++) {
            final int[] goc = doChiSo(mp, real.SkinLinhThu.KHONG_CO);
            if (goc == null) {
                Kiem.dung("dung duoc Char that cho mon phai " + mp, false);
                return;
            }
            final int[] phucThu = doChiSo(mp, 0);
            final int[] gauTruc = doChiSo(mp, 1);
            final int[] tienLoc = doChiSo(mp, 2);
            final int[] xichDuong = doChiSo(mp, 3);
            final int[] chienLang = doChiSo(mp, 4);
            final String mpTen = " (mon phai " + mp + ")";

            // --- Phuc Thu: +2000 HP, +3000 MP, KHONG dong gi khac ---
            Kiem.bang("Phuc Thu +2000 maxhp" + mpTen,
                    Integer.valueOf(goc[HP] + 2000), Integer.valueOf(phucThu[HP]));
            Kiem.bang("Phuc Thu +3000 maxmp" + mpTen,
                    Integer.valueOf(goc[MP] + 3000), Integer.valueOf(phucThu[MP]));
            Kiem.bang("Phuc Thu khong doi tan cong" + mpTen,
                    Integer.valueOf(goc[TC]), Integer.valueOf(phucThu[TC]));
            Kiem.bang("Phuc Thu khong doi thu" + mpTen,
                    Integer.valueOf(goc[TV]), Integer.valueOf(phucThu[TV]));

            // --- Gau Truc: "Tac dung chi so" (+200 chinh xac) CHI khi hop he (mp == 1,
            // Chien binh) — nguoi choi yeu cau "sai he thi chi con buff bi dong chu khong
            // co tac dung chu dong", nen o day chinh xac phai giu NGUYEN chi so goc khi
            // mon phai khac 1, va chi tang dung luc mp == 1.
            final boolean hopGauTruc = mp == 1;
            Kiem.bang("Gau Truc chinh xac " + (hopGauTruc ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopGauTruc), Boolean.valueOf(gauTruc[CX] > goc[CX]));
            if (!hopGauTruc) {
                Kiem.bang("Gau Truc sai he: chinh xac bang dung chi so goc" + mpTen,
                        Integer.valueOf(goc[CX]), Integer.valueOf(gauTruc[CX]));
            }
            Kiem.dung("Gau Truc KHONG day chinh xac vuot tran " + real.Char.TRAN_CHINH_XAC
                    + mpTen, gauTruc[CX] <= real.Char.TRAN_CHINH_XAC);
            Kiem.bang("Gau Truc khong doi maxhp" + mpTen,
                    Integer.valueOf(goc[HP]), Integer.valueOf(gauTruc[HP]));
            Kiem.bang("Gau Truc khong doi tan cong" + mpTen,
                    Integer.valueOf(goc[TC]), Integer.valueOf(gauTruc[TC]));

            // --- Tien Loc: "Tac dung chi so" (+20% tiem nang) CHI khi hop he (mp == 4,
            // Cung thu) — sai he thi ca nam chi so keo theo phai giu nguyen het, khong duoc
            // ho ri mot chut nao.
            final boolean hopTienLoc = mp == 4;
            Kiem.bang("Tien Loc tan cong " + (hopTienLoc ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopTienLoc), Boolean.valueOf(tienLoc[TC] > goc[TC]));
            Kiem.bang("Tien Loc maxhp " + (hopTienLoc ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopTienLoc), Boolean.valueOf(tienLoc[HP] > goc[HP]));
            Kiem.bang("Tien Loc maxmp " + (hopTienLoc ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopTienLoc), Boolean.valueOf(tienLoc[MP] > goc[MP]));
            Kiem.bang("Tien Loc thu " + (hopTienLoc ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopTienLoc), Boolean.valueOf(tienLoc[TV] > goc[TV]));
            Kiem.bang("Tien Loc ne tranh " + (hopTienLoc ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopTienLoc), Boolean.valueOf(tienLoc[NT] > goc[NT]));
            if (!hopTienLoc) {
                Kiem.bang("Tien Loc sai he: tan cong bang dung chi so goc" + mpTen,
                        Integer.valueOf(goc[TC]), Integer.valueOf(tienLoc[TC]));
            }
            // May man la diem tiem nang THU NAM, no nuoi chi mang. Chi mang chi duoc
            // tinh cho Cung thu (crit1 chi chay o charClass == 4) — dung luc do cung la
            // mon phai hop he cua Tien Loc, nen chi kiem tang o day.
            if (mp == 4) {
                Kiem.dung("Tien Loc lam chi mang tang (diem tiem nang thu nam, hop he)" + mpTen,
                        tienLoc[CM] > goc[CM]);
            }

            // --- Xich Duong: "Tac dung chi so" (+5% xuyen giap) CHI khi hop he (mp == 3,
            // Dau si) — sai he thi xuyen giap phai bang dung chi so goc.
            final boolean hopXichDuong = mp == 3;
            Kiem.bang("Xich Duong xuyen giap" + (hopXichDuong ? " +5 (hop he)" : " giu nguyen (sai he)") + mpTen,
                    Integer.valueOf(hopXichDuong ? goc[XG] + 5 : goc[XG]), Integer.valueOf(xichDuong[XG]));
            Kiem.bang("Xich Duong khong doi tan cong" + mpTen,
                    Integer.valueOf(goc[TC]), Integer.valueOf(xichDuong[TC]));
            Kiem.bang("Xich Duong khong doi maxhp" + mpTen,
                    Integer.valueOf(goc[HP]), Integer.valueOf(xichDuong[HP]));

            // --- Chien Lang: "Tac dung chi so" (+20% thu) CHI khi hop he (mp == 0, Kiem
            // khach) — sai he thi thu vat/thu phep phai bang dung chi so goc.
            final boolean hopChienLang = mp == 0;
            Kiem.bang("Chien Lang thu vat " + (hopChienLang ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopChienLang), Boolean.valueOf(chienLang[TV] > goc[TV]));
            Kiem.bang("Chien Lang thu phep " + (hopChienLang ? "co tang (hop he)" : "giu nguyen (sai he)") + mpTen,
                    Boolean.valueOf(hopChienLang), Boolean.valueOf(chienLang[TP] > goc[TP]));
            if (!hopChienLang) {
                Kiem.bang("Chien Lang sai he: thu vat bang dung chi so goc" + mpTen,
                        Integer.valueOf(goc[TV]), Integer.valueOf(chienLang[TV]));
            }
            Kiem.bang("Chien Lang khong doi maxhp" + mpTen,
                    Integer.valueOf(goc[HP]), Integer.valueOf(chienLang[HP]));
            Kiem.bang("Chien Lang khong doi tan cong" + mpTen,
                    Integer.valueOf(goc[TC]), Integer.valueOf(chienLang[TC]));

            // --- Khong skin nao duoc dong vao xuyen giap ngoai Xich Duong ---
            Kiem.bang("Phuc Thu khong dong xuyen giap" + mpTen,
                    Integer.valueOf(goc[XG]), Integer.valueOf(phucThu[XG]));
            Kiem.bang("Chien Lang khong dong xuyen giap" + mpTen,
                    Integer.valueOf(goc[XG]), Integer.valueOf(chienLang[XG]));
        }

        // --- Nhan vat CHI SO CAO: kiem chinh cai tran, bang so chu khong bang van ban ---
        //
        // Voi chi so 200 o tren, chinh xac chua cham tran nen bo cai chan tran di van ra
        // dung ket qua — phep kiem se khong chung minh duoc gi. Phai dung mot nhan vat da
        // cham tran moi thay. Day dung la loi da tim ra: Gau Truc day chinh xac len 893
        // trong khi tran cua game la 693.
        final int[] caoGoc = doChiSoCao(real.SkinLinhThu.KHONG_CO);
        final int[] caoGauTruc = doChiSoCao(1);
        final int[] caoTienLoc = doChiSoCao(2);
        if (caoGoc == null || caoGauTruc == null || caoTienLoc == null) {
            Kiem.dung("dung duoc Char chi so cao", false);
            return;
        }
        Kiem.bang("nhan vat chi so cao da cham tran chinh xac",
                Integer.valueOf(real.Char.TRAN_CHINH_XAC), Integer.valueOf(caoGoc[CX]));
        Kiem.bang("Gau Truc KHONG day chinh xac vuot tran cua game",
                Integer.valueOf(real.Char.TRAN_CHINH_XAC), Integer.valueOf(caoGauTruc[CX]));
        // Ne tranh cung co tran (299) va Tien Loc phai ton trong no y het.
        Kiem.dung("ne tranh cua nhan vat chi so cao khong vuot tran",
                caoTienLoc[NT] <= 299);
    }

    /**
     * MAC SKIN KHONG BAO GIO DUOC LAM TUT TAN CONG — o MOI muc chi so.
     *
     * LOI DA XAY RA THAT (05/09/2026, nguoi choi bao): Cung thu 49k tan cong, mac Tien Loc
     * (+20% diem tiem nang) vao thi tan cong TUT XUONG 47k thay vi tang.
     *
     * Goc khong nam trong SkinLinhThu ma o CharChiSo.calculateAttack: bay cong thuc tan cong
     * (ca nam mon phai) ep ket qua ve (short) — tran 32767 — trong khi attack1 va moi so hang
     * deu la int. Kheo leo/suc manh la short (toi da 32767) nen sau khi nhan 5/3 hay 4/3 la
     * vuot tran va QUAY VONG SANG AM. Hau qua: nhan vat cang manh tan cong cang tut, va cong
     * them chi so (skin, buff, do moi) co the LAM GIAM tan cong dung luc vua vuot moc 32767.
     *
     * VI SAO BO KIEM CU KHONG BAT DUOC: doChiSo() dung chi so 200 va doChiSoCao() dung 5000 —
     * ca hai deu nam DUOI moc tran, nen bay cong thuc chay dung va phep kiem "Tien Loc lam
     * tang tan cong" van dat. Phep kiem duoi day quet dung KHOANG DA VO (16000..21000).
     *
     * Quet bang Char THAT + calculateAttrib() THAT, khong chep lai cong thuc: chep lai thi
     * sua cong thuc that ma quen sua ban chep, phep kiem van dat.
     */
    private static void kiemTanCongKhongTutKhiChiSoCao() {
        Kiem.nhom("Skin linh thu - mac skin khong duoc lam TUT tan cong (tran short cu)");

        if (!buDuLieuAnhSkin()) {
            Kiem.dung("bu duoc du lieu anh skin", false);
            return;
        }
        buGemTemplate();

        // Cung thu (mon phai 4) hop he voi Tien Loc (skin 2) — dung cap dang bi bao loi.
        final int[] mucKheoLeo = {5000, 15000, 16000, 17000, 18000, 19000, 19600, 20000, 21000};
        int soMucTut = 0;
        int soMucAm = 0;
        for (final int kl : mucKheoLeo) {
            final Integer khongSkin = tanCongCungThu(kl, real.SkinLinhThu.KHONG_CO);
            final Integer coTienLoc = tanCongCungThu(kl, 2);
            if (khongSkin == null || coTienLoc == null) {
                Kiem.dung("dung duoc Char that o muc kheo leo " + kl, false);
                return;
            }
            if (coTienLoc.intValue() < khongSkin.intValue()) {
                soMucTut++;
            }
            if (khongSkin.intValue() < 0 || coTienLoc.intValue() < 0) {
                soMucAm++;
            }
        }
        Kiem.bang("khong muc kheo leo nao ma mac Tien Loc lam TUT tan cong",
                Integer.valueOf(0), Integer.valueOf(soMucTut));
        Kiem.bang("khong muc kheo leo nao cho ra tan cong AM",
                Integer.valueOf(0), Integer.valueOf(soMucAm));

        // Chot thang o ma nguon: bay cong thuc tan cong khong duoc ep (short) nua.
        final String chiSo = doc("src/real/CharChiSo.java");
        final int mocHam = chiSo.indexOf("static void calculateAttack(Char self)");
        Kiem.dung("tim thay calculateAttack", mocHam > 0);
        if (mocHam > 0) {
            final int mocKet = chiSo.indexOf("int attack2 = 0;", mocHam);
            final String than = chiSo.substring(mocHam, mocKet > mocHam ? mocKet : chiSo.length());
            Kiem.sai("khoi cong thuc tan cong theo mon phai KHONG con ep (short) (tran 32767)",
                    than.contains("attack1 += (short)"));
        }
    }

    /** Tan cong THAT su cua mot Cung thu co kheo leo = kl, mang skin nay. null neu dung hong. */
    private static Integer tanCongCungThu(final int kheoLeo, final int skin) {
        try {
            final real.Char c = new real.Char(null);
            c.charname = "KIEMTHUTC";
            c.charClass = 4; // Cung thu — hop he voi Tien Loc
            c.lastLV = 100;
            c.strength = (short) kheoLeo;
            c.agitity = (short) kheoLeo;
            c.spirit = (short) kheoLeo;
            c.health = (short) kheoLeo;
            c.luck = (short) kheoLeo;
            c.animalRide = taoThuCuoi();
            if (c.animalRide == null) {
                return null;
            }
            c.idSkinLinhThu = skin;
            c.calculateAttrib();
            return Integer.valueOf(c.getAttack());
        } catch (final Throwable t) {
            return null;
        }
    }

    /** Nhu doChiSo nhung chi so du cao de cham tran chinh xac va ne tranh. */
    private static int[] doChiSoCao(final int skin) {
        try {
            final real.Char c = new real.Char(null);
            c.charname = "KIEMTHUCAO";
            // Chi so tinh (chinh xac, ne tranh...) cua Gau Truc/Tien Loc gio CHI cong khi
            // hop he — phai dung DUNG mon phai hop he voi skin dang kiem, khong thi bai
            // kiem tran cua game duoi day khong con cong gi de ma chan tran nua. Khong co
            // skin (KHONG_CO) thi mon phai nao cung nhu nhau, giu class 0.
            final int mpHopHe = real.SkinLinhThu.hopLe(skin) ? real.SkinLinhThu.monPhaiHopVoiSkin(skin) : 0;
            c.charClass = (byte) (mpHopHe >= 0 ? mpHopHe : 0);
            c.lastLV = 100;
            c.strength = 5000;
            c.agitity = 5000;
            c.spirit = 5000;
            c.health = 5000;
            c.luck = 5000;
            c.animalRide = taoThuCuoi();
            if (c.animalRide == null) {
                return null;
            }
            c.idSkinLinhThu = skin;
            c.calculateAttrib();
            final java.lang.reflect.Field f = real.Char.class.getField("attNguHanh");
            final byte[] nh = (byte[]) f.get(c);
            return new int[]{c.getAttack(), c.defend_physic, c.defend_magic, c.maxhp,
                             c.maxmp, c.accurate, c.dodge, c.critical, nh[3]};
        } catch (final Throwable t) {
            return null;
        }
    }

    private static final int TC = 0;
    private static final int TV = 1;
    private static final int TP = 2;
    private static final int HP = 3;
    private static final int MP = 4;
    private static final int CX = 5;
    private static final int NT = 6;
    private static final int CM = 7;
    private static final int XG = 8;

    /** Dung mot Char that, chay calculateAttrib, tra ve day chi so. null neu khong dung duoc. */
    private static int[] doChiSo(final int monPhai, final int skin) {
        try {
            final real.Char c = new real.Char(null);
            c.charname = "KIEMTHU";
            c.charClass = (byte) monPhai;
            c.lastLV = 50;
            // Chi so khiem ton de khong cham tran chinh xac (693) va ne tranh (299).
            c.strength = 200;
            c.agitity = 200;
            c.spirit = 200;
            c.health = 200;
            c.luck = 200;
            c.animalRide = taoThuCuoi();
            if (c.animalRide == null) {
                return null;
            }
            c.idSkinLinhThu = skin;
            c.calculateAttrib();
            final java.lang.reflect.Field f = real.Char.class.getField("attNguHanh");
            final byte[] nh = (byte[]) f.get(c);
            return new int[]{c.getAttack(), c.defend_physic, c.defend_magic, c.maxhp,
                             c.maxmp, c.accurate, c.dodge, c.critical, nh[3]};
        } catch (final Throwable t) {
            return null;
        }
    }

    private static data.Animal taoThuCuoi() {
        try {
            final java.lang.reflect.Constructor<data.Animal> ct =
                    data.Animal.class.getDeclaredConstructor();
            ct.setAccessible(true);
            return ct.newInstance();
        } catch (final Throwable t) {
            return null;
        }
    }

    /**
     * SkinLinhThu.dangMang() doi co du hai tep anh cua skin. Bo kiem thu khong nap
     * DataGame that, nen bu hai mang do bang du lieu gia — chi can KHAC NULL.
     */
    private static boolean buDuLieuAnhSkin() {
        final int n = real.SkinLinhThu.SO_SKIN;
        return datTinh("dataSkinThu", new byte[n][1]) && datTinh("imgSkinThu", new byte[n][1]);
    }

    private static boolean datTinh(final String ten, final Object giaTri) {
        try {
            final java.lang.reflect.Field f = data.DataGame.class.getDeclaredField(ten);
            f.setAccessible(true);
            f.set(null, giaTri);
            return true;
        } catch (final Throwable t) {
            return false;
        }
    }

    private static void buGemTemplate() {
        try {
            if (real.Map.gemTemplate == null) {
                final real.GemTemplate[] g = new real.GemTemplate[400];
                for (int i = 0; i < g.length; i++) {
                    g[i] = new real.GemTemplate();
                }
                real.Map.gemTemplate = g;
            }
        } catch (final Throwable ignored) {
            // Khong bu duoc thi doChiSo() se tra null va phep kiem tren bao hong.
        }
    }

    /**
     * MO TA VAT PHAM: chuoi trong CSDL phai khop tuyet doi voi bang so trong ma nguon.
     *
     * Game nay khong co cot mo ta rieng cho do mac duoc — no nhet ca bang mo ta vao cot
     * `name` va xuong dong bang ky tu xuong dong that (xem "Áo Nghê Thường" trong ban
     * dump). Nghia la con so NGUOI CHOI DOC nam o CSDL con con so GAME DUNG nam o ma
     * nguon. Hai cho, mot su that — khong chot lai thi som muon lech, va lech kieu do
     * khong ai phat hien tru khi ngoi cong tay.
     */
    private static void kiemMoTaVatPham() {
        Kiem.nhom("Skin linh thu - mo ta vat pham khop voi bang so");

        final String sql = doc("sql/ten_mo_ta_skin_linh_thu.sql");
        Kiem.dung("doc duoc sql/ten_mo_ta_skin_linh_thu.sql", sql.length() > 100);
        Kiem.dung("tep sql ghi ro la duoc sinh ra", sql.contains("DUNG SUA TAY"));

        for (int s = 0; s < real.SkinLinhThu.SO_SKIN; s++) {
            final String moTa = real.SkinLinhThu.moTaVatPham(s);
            Kiem.dung("mo ta skin " + s + " co ten skin", moTa.contains(real.SkinLinhThu.TEN[s]));
            Kiem.dung("mo ta skin " + s + " co dong he",
                    moTa.contains("[hệ " + real.SkinLinhThu.TEN_HE[real.SkinLinhThu.HE[s]] + "]"));
            // Chuoi trong sql phai la CHINH chuoi ma ma nguon sinh ra, tung ky tu.
            Kiem.dung("sql chua dung mo ta skin " + s + " (khong lech mot ky tu nao)",
                    sql.contains(moTa.replace("'", "''")));
            Kiem.dung("sql cap nhat dung id " + real.SkinLinhThu.ID_ITEM[s],
                    sql.contains("WHERE `id` = " + real.SkinLinhThu.ID_ITEM[s]));
        }
        Kiem.bang("mo ta cua skin khong hop le la chuoi rong", "", real.SkinLinhThu.moTaVatPham(9));

        // Tep cai dat goc phai chi sang tep nay, khong thi nguoi cai chi chay mot tep va
        // vat pham mang ten ngan khong co mo ta.
        final String goc = doc("sql/skin_linh_thu.sql");
        Kiem.dung("skin_linh_thu.sql chi sang tep ten/mo ta",
                goc.contains("ten_mo_ta_skin_linh_thu.sql"));
    }

    /** Dem so lan mot chuoi xuat hien. */
    private static int dem(final String trong, final String tim) {
        int n = 0;
        int i = trong.indexOf(tim);
        while (i >= 0) {
            n++;
            i = trong.indexOf(tim, i + tim.length());
        }
        return n;
    }

    /**
     * Mang tra theo TYPE phia SERVER phai phu het dai type dang dung.
     *
     * Day la cho da tung vo that: Item.attribShowedForType dai 30, ma skin mang type 30,
     * nen Item.getInfoAtbItem nem ArrayIndexOutOfBoundsException NGAY giua luc dung goi
     * tin. Hong ca createAnimalWearingMessage lan createCharInventoryMessage: nguoi choi
     * thay bang "Trang bi thu" RONG va vat pham bien mat khoi hanh trang, du du lieu van
     * con nguyen. Loi bi try/catch o ngoai nuot nen chi thay hau qua, khong thay loi.
     *
     * Lan do toi da soat rat ky cac mang tra theo type phia CLIENT ma quen mat phia
     * SERVER. Kiem tra nay chan dung viec do lap lai.
     */
    private static void kiemMangTraTheoType() {
        Kiem.nhom("Skin linh thu - mang tra theo type phia server");

        final int type = real.SkinLinhThu.TYPE_ITEM;
        Kiem.dung("Item.attribShowedForType phu het type " + type,
                real.Item.attribShowedForType != null
                        && real.Item.attribShowedForType.length > type);
        if (real.Item.attribShowedForType != null
                && real.Item.attribShowedForType.length > type) {
            Kiem.dung("hang " + type + " khong rong (null la NPE luc doc)",
                    real.Item.attribShowedForType[type] != null);
            Kiem.dung("hang " + type + " du 10 cot",
                    real.Item.attribShowedForType[type].length >= 10);
            // Hang 30 phai de trong. Skin CO noi tai (xem kiemNoiTaiThuongTruc) nhung so
            // cua no nam trong hang so cua real.SkinLinhThu chu KHONG nam trong atb cua
            // vat pham — nen khong co dong atb nao de hien, va bat co len la hien so 0.
            boolean coHien = false;
            for (final boolean b : real.Item.attribShowedForType[type]) {
                coHien = coHien || b;
            }
            Kiem.sai("skin khong hien dong atb nao (noi tai khong nam trong atb)", coHien);
        }
        // Ca dai cot phai 30..34 deu phai an toan, khong rieng gi o dang dung.
        for (int t = 30; t <= 34; t++) {
            Kiem.dung("type " + t + " tra duoc khong nem loi",
                    real.Item.attribShowedForType.length > t
                            && real.Item.attribShowedForType[t] != null);
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
            if (n == real.SkinLinhThu.KIM_CANG_LONG) {
                // Kim Cang Long: "tren" cua khoi xuong la DUOI/TIA NUOC bay cao han han THAN
                // rong - khac han Poro/Lan/Heo la thu 4 chan gon, noi "tren" chinh la LUNG
                // con vat, nen "lut" (tinh tu "tren" toi diem nguoi ngoi) cua rong tat yeu
                // lon hon nhieu du nguoi ngoi dung cho. Con so nay KHONG chot duoc bang cong
                // thuc doi chieu voi thu tool (do la cho thu 4 chan) - cho ngoi cua no duoc
                // chot rieng o kiemChoNgoiKimCangLong() ben duoi. O day chi kiem con so
                // khong vo ly (duong, va khong qua vai tram px).
                Kiem.dung("skin " + n + " lut " + l + "px, ran dai nen vuot khoang thu 4 chan (xem ghi chu)",
                        l != Integer.MIN_VALUE && l > 0 && l < 300);
                continue;
            }
            Kiem.dung("skin " + n + " lut " + l + "px, nam trong khoang thu tool cua game",
                    l != Integer.MIN_VALUE && l >= thap - bien && l <= cao + bien);
        }

        kiemChoNgoiKimCangLong();
    }

    /**
     * Cho ngoi Kim Cang Long (skin 5) - 11/09/2026.
     *
     * LICH SU: ba lan chinh "lut" cua khoi xuong (khop Poro 12px -> nguoi noi lo lung; can
     * day ve 0..7px -> nguoi chui han vao dau rong; muc giua 47px) deu bi nguoi choi bao loi
     * kem anh chup, vi ca ba chi doi CON RONG len xuong ma van dung DONG 0 cua bang
     * dx/dy_horse_tool (dong cua Poro): huong ngang nguoi bi day +7px ve phia duoi trong khi
     * yen nam dung tam than; huong xuong thi dat rong cham dat la nguoi (nhac -20) chui vao
     * dau rong, nang rong len thi chan rong roi mat dat.
     *
     * CACH SUA: hai viec tach nhau.
     *   1. Con rong DUNG CHAM DAT o ca ba khoi nhu moi thu cuoi khac (khoi xuong truoc do
     *      lot xuong duoi vach chan +27px, da nang len 24px bang tools/DichSkinThu).
     *   2. NGUOI di theo DONG RIENG so 5 cua bang (SkinLinhThu.INDEX_DXY[5]): ngang dx +1/-1
     *      (dung tam yen, thay cho +7/-7), xuong dy -23 - dung quan he cua Rong Lon (thu tool
     *      rong to co san cua game, dong 3: nguoi ngoi sau dau rong, mong thap hon dinh dau
     *      ~14px). Bang nay server gui kem SO DONG, ca hai client doc theo do (Unity
     *      MessageHandler.cs:1795, JAR MessageHandler.java:1754) nen them dong khong can
     *      dong goi lai client.
     *   3. (DA THU ROI BO) Rieng khoi xuong, sợi duoi cuon len (anh con 2, 5, 10) tung doi
     *      sang phai 20px ngay 11/09/2026: client luon ve con thu SAU than nguoi (Char.cs
     *      paint, orderPaint 1) nen manh nao nam trong cot nguoi la ve DE LEN nguoi - duoi
     *      moc giua lung che 49% than. Nhung nguoi choi (12/09/2026) hoi "sao doi duoi chu
     *      khong phai vi tri nguoi" va chon TRA DUOI VE CHO GOC, chap nhan duoi cat ngang
     *      than (~65-74% voi nguoi o -28). Du lieu duoi vi the phai GIU NGUYEN nhu goc -
     *      muon dời lai thi hoi nguoi choi truoc, dung tu y.
     *
     * Do bang tools/ThuChoNgoiSkinThu (ve dung thu tu ve cua client + hinh que nguoi ngoi),
     * xem docs/skin-linh-thu.md muc 9.
     */
    private static void kiemChoNgoiKimCangLong() {
        Kiem.nhom("Skin linh thu - cho ngoi Kim Cang Long (dong bang rieng + du lieu khoi xuong)");

        final int s = real.SkinLinhThu.KIM_CANG_LONG;
        final byte[][] dx = real.Char.dx_horse_tool;
        final byte[][] dy = real.Char.dy_horse_tool;
        Kiem.bang("INDEX_DXY du " + real.SkinLinhThu.SO_SKIN + " phan tu",
                Integer.valueOf(real.SkinLinhThu.SO_SKIN), Integer.valueOf(real.SkinLinhThu.INDEX_DXY.length));
        Kiem.bang("dx_horse_tool va dy_horse_tool cung so dong",
                Integer.valueOf(dx.length), Integer.valueOf(dy.length));
        for (int r = 0; r < dx.length; r++) {
            Kiem.dung("dong " + r + " du 4 huong (xuong, len, trai, phai)",
                    dx[r].length == 4 && dy[r].length == 4);
        }
        // Moi skin phai tro vao mot dong CO THAT: gui so lon hon bang la client nem loi giua
        // luc ve, gui -1 la nguoi ngoi tut xuong sat dat.
        for (int i = 0; i < real.SkinLinhThu.SO_SKIN; i++) {
            final int d = real.SkinLinhThu.INDEX_DXY[i];
            Kiem.dung("INDEX_DXY[" + i + "] = " + d + " tro vao mot dong co that",
                    d >= 0 && d < dx.length);
        }
        // 5 skin thu bon chan van dung dong 0 (Poro) - doi chung la doi cho ngoi ca 5 con.
        for (int i = 0; i < s; i++) {
            Kiem.bang("skin " + i + " (thu bon chan) van dung dong 0", Integer.valueOf(0),
                    Integer.valueOf(real.SkinLinhThu.INDEX_DXY[i]));
        }

        final int d5 = real.SkinLinhThu.INDEX_DXY[s];
        Kiem.dung("Kim Cang Long dung dong rieng, khong phai dong 0 cua Poro", d5 != 0);
        if (d5 < 0 || d5 >= dx.length) {
            return;
        }
        Kiem.bang("dong KCL: xuong dx 0 (nguoi giua dau rong)", Integer.valueOf(0), Integer.valueOf(dx[d5][0]));
        Kiem.bang("dong KCL: len dx 0", Integer.valueOf(0), Integer.valueOf(dx[d5][1]));
        Kiem.bang("dong KCL: trai dx +1 (yen o tam than, khong day 7px nhu Poro)",
                Integer.valueOf(1), Integer.valueOf(dx[d5][2]));
        Kiem.bang("dong KCL: phai dx -1 (doi dau cua trai - client lat guong)",
                Integer.valueOf(-1), Integer.valueOf(dx[d5][3]));
        // Hai so duoi day chinh theo anh chup nguoi choi gui: 11/09 -23 (khop Rong Lon -22) /
        // -15 -> "xuong cho cao len chut, len dich xuong chut nua" -> -28 / -10; 12/09 "than
        // van chua gan yen nhu hinh ngang" -> len ha tiep xuong 0 (huong len client ve thu
        // truoc nguoi nen yen khong che duoc nguoi, chi con cach ha than xuong che phan tren yen).
        Kiem.bang("dong KCL: xuong dy -28 (nguoi ngoi sau dau rong, cao hon dinh horn)",
                Integer.valueOf(-28), Integer.valueOf(dy[d5][0]));
        Kiem.bang("dong KCL: len dy 0 (than che phan tren yen do khi nhin tu lung, con mep sau yen)",
                Integer.valueOf(0), Integer.valueOf(dy[d5][1]));
        // paintName cua client (Char.cs:1520 "num = dy_horse[0][dir]", Char.java:2081) LUON dat
        // ten theo dong 0, bat ke dong nao dang dung. Rang buoc vi the LECH PHIA:
        //   - dong moi CAO hon dong 0 (dy am hon) la ten sat dau: anh chup thuc te o dy -23
        //     (cao hon 3) ten con cach dinh dau ~14px, -28 (cao hon 8) con ~9px; qua 10 la cham.
        //   - dong moi THAP hon dong 0 (dy duong hon) chi lam ten cach dau xa them, vo hai;
        //     huong len o 0 thap hon dong 0 (-15) 15px la co y. Chan 15 de khong ai ha vo tan.
        for (int h = 0; h < 4; h++) {
            final int lech = dy[d5][h] - dy[0][h];
            Kiem.dung("dong KCL: dy huong " + h + " khong cao hon dong 0 qua 10px (lech " + lech + "; ten dat theo dong 0)",
                    lech >= -10);
            Kiem.dung("dong KCL: dy huong " + h + " khong thap hon dong 0 qua 15px (lech " + lech + ")",
                    lech <= 15);
        }
        // Dong 5 phai dung la dong CUOI: khong ai chen dong vao giua ma quen doi INDEX_DXY.
        Kiem.bang("dong KCL la dong cuoi cua bang", Integer.valueOf(dx.length - 1), Integer.valueOf(d5));

        // ---- Du lieu khung khoi xuong ----
        final java.io.File tep = new java.io.File("map/skinthu/" + s);
        final int[] doDuoi = doKhoiXuong(tep);
        if (doDuoi == null) {
            System.out.println("    (bo qua: khong doc duoc map/skinthu/" + s + ")");
            return;
        }
        // [0] = diem mo duc thap nhat (day con rong, alpha >= 128) cua khoi xuong. Truoc
        // 11/09/2026 la +27 (ca dau rong nam duoi vach chan); Poro +5, Rong Lon +15 (bong
        // dam). Rong phai dung tren dat: day trong khoang 0..8.
        Kiem.dung("khoi xuong: day con rong o " + doDuoi[0] + "px, cham dat (0..8)",
                doDuoi[0] >= 0 && doDuoi[0] <= 8);
        // [1] = dx nho nhat cua cac manh DUOI (anh con 2, 5, 10 - tia nuoc cuon len, ve TREN
        // cung). Cot than nguoi la x -6..6 (+-6 quanh dx 0 cua dong KCL). Duoi GOC bat dau
        // trong cot do (ve de len than nguoi); ban 11/09 tung doi ra x >= 7 roi nguoi choi
        // chon tra lai 12/09/2026. Chot o day de khong ai "sua giup" lan nua ma khong hoi.
        Kiem.dung("khoi xuong: duoi cuon (anh 2/5/10) o vi tri goc, bat dau tu x " + doDuoi[1] + " < 7 (nguoi choi chon giu)",
                doDuoi[1] < 7);
        Kiem.dung("khoi xuong: co manh duoi de kiem (atlas chua doi)", doDuoi[2] > 0);
    }

    /**
     * Do khoi xuong (khung 0..6) cua mot tep thu cuoi: {day mo duc thap nhat (alpha >= 128),
     * dx nho nhat cua cac manh dung anh con 2/5/10, so manh nhu vay}. null neu khong doc duoc.
     */
    private static int[] doKhoiXuong(final java.io.File tep) {
        try {
            final byte[] b = java.nio.file.Files.readAllBytes(tep.toPath());
            final java.awt.image.BufferedImage at =
                    javax.imageio.ImageIO.read(new java.io.File(tep.getPath() + ".png"));
            final int soAnh = b[0] & 0xFF;
            final int[] ix = new int[soAnh];
            final int[] iy = new int[soAnh];
            final int[] iw = new int[soAnh];
            final int[] ih = new int[soAnh];
            for (int i = 0; i < soAnh; i++) {
                ix[i] = b[1 + i * 5 + 1] & 0xFF;
                iy[i] = b[1 + i * 5 + 2] & 0xFF;
                iw[i] = b[1 + i * 5 + 3] & 0xFF;
                ih[i] = b[1 + i * 5 + 4] & 0xFF;
            }
            int vt = 1 + soAnh * 5;
            final int soKhung = ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
            vt += 2;
            int day = Integer.MIN_VALUE;
            int dxDuoi = Integer.MAX_VALUE;
            int soManhDuoi = 0;
            for (int f = 0; f < soKhung; f++) {
                final int soManh = b[vt] & 0xFF;
                vt++;
                for (int k = 0; k < soManh; k++) {
                    final int ddx = (short) (((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF));
                    final int ddy = (short) (((b[vt + 2] & 0xFF) << 8) | (b[vt + 3] & 0xFF));
                    final int a = b[vt + 4] & 0xFF;
                    vt += 7;
                    if (f >= 7) {
                        continue;
                    }
                    if (a == 2 || a == 5 || a == 10) {
                        soManhDuoi++;
                        dxDuoi = Math.min(dxDuoi, ddx);
                    }
                    for (int yy = ih[a] - 1; yy >= 0; yy--) {
                        boolean coDiem = false;
                        for (int xx = 0; xx < iw[a] && !coDiem; xx++) {
                            if (ix[a] + xx < at.getWidth() && iy[a] + yy < at.getHeight()
                                    && (at.getRGB(ix[a] + xx, iy[a] + yy) >>> 24) >= 128) {
                                coDiem = true;
                            }
                        }
                        if (coDiem) {
                            day = Math.max(day, ddy + yy);
                            break;
                        }
                    }
                }
            }
            return new int[] { day, dxDuoi == Integer.MAX_VALUE ? 0 : dxDuoi, soManhDuoi };
        } catch (final Exception e) {
            return null;
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
