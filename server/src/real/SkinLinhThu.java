package real;

import data.DataGame;

/**
 * SKIN LINH THU — doi hinh con linh thu (thu cuoi) dang cuoi, GIU NGUYEN chi so.
 *
 * Mac vat pham skin vao o dau tien ben phai bang "Trang bi thu" thi con linh thu dang
 * cuoi doi hinh, doi hoat anh va doi cach di chuyen theo skin. Chi so cua con linh thu
 * (att, otherAtt, cap, khang, thuc an...) khong bi dong toi mot chut nao: skin chi ghi
 * de o tang DONG GOI TIN, khong ghi vao con thu.
 *
 * ------------------------------------------------------------------------------------
 * VI SAO PHAI TACH O TANG GOI TIN
 * ------------------------------------------------------------------------------------
 * data.Animal.idImg nghe nhu "id hinh anh" nhung thuc ra la KHOA LOAI THU. No vua tra
 * cuu bang hinh anh (Animal.fr, idImgPaint, rideHorse, idImgIcon, typeAnimal) vua tra
 * cuu bang CHI SO (Animal.pcTN, addMoreSP, addMoreBasic va gan 50 nhanh switch trong
 * upLevel / upAttribute / getGiamKhang...). No lai duoc luu xuong cot tob_animal.id_img.
 *
 * Nen ghi idImg de doi hinh la mat han loai thu that — dung nghia "mat chi so". Vi vay
 * o day KHONG BAO GIO cham vao idImg. Skin chi song trong Char.idSkinLinhThu va chi
 * duoc doc o dung ba cho dong goi tin (xem MessageCreator + Map.doSendDetailChar).
 *
 * ------------------------------------------------------------------------------------
 * CLIENT VE CON THU BANG DUONG NAO
 * ------------------------------------------------------------------------------------
 * Client co hai duong ve thu cuoi:
 *
 *   1. Duong "ngua co dien": server gui userHorse, client lam useHorse = userHorse - 1
 *      roi tra bang HARD-CODE 8 phan tu (Char.fHorse, sizeHorse, hHorse, dxh, dyh).
 *      Duong nay KHONG them hinh moi duoc — bang nam trong client, ma client la ban
 *      decompile khong build lai duoc. Gui so >= 9 con lam client nem IndexOutOfRange.
 *
 *   2. Duong "thu cuoi tool": server gui idThutool, client goi Char.getPartEff(id) ->
 *      DataSkillEff, ve theo du lieu frame nhan tu server. Rong Bang, Poro, Lan, Su Tu
 *      va hoa hinh deu di duong nay. Duong nay HOAN TOAN do du lieu quyet dinh.
 *
 * Skin di duong 2, va PHAI TAT duong 1 cung luc (gui userHorse = 0) — khong thi client
 * ve ca hai, con thu that va con skin chong len nhau.
 *
 * ------------------------------------------------------------------------------------
 * ID ANH: DAI MOI HOAN TOAN
 * ------------------------------------------------------------------------------------
 * Client giu anh theo id trong GameData.listImgIcon va khong he co so phien ban; muc
 * chi bi bo khi bang qua 100 phan tu VA ranh qua 120s, nen thuc te no song den luc tat
 * han app. Dung lai mot id da tung phat anh khac la nguoi choi cu ghep anh cu voi du
 * lieu frame moi -> hien ra mot khoi vun.
 *
 * Vi vay skin dung DAI ID MOI TINH 13000..13004, chua tung duoc dung: dai id anh cua
 * DataGame.sendByteImg chi chay den 12099, va idImg la short nen 13000 van hop le. Nho
 * vay khong can co che day anh ep buoc nao ca, va cung khong dung chung id voi bat cu
 * hieu ung nao san co.
 *
 * File anh nam o thu muc RIENG map/skinthu/, khong chen vao map/d_effect/ (0..299 da
 * kin cho). Xem tools/SkinThu.java.
 */
public final class SkinLinhThu {

    private SkinLinhThu() {
    }

    public static final int KHONG_CO = -1;
    public static final int SO_SKIN = 5;

    /** Ten skin, dung cho thong bao va mo ta vat pham. Thu tu khop ID_ITEM va file anh. */
    public static final String[] TEN = {"Phúc Thú", "Gấu Trúc", "Tiên Lộc", "Xích Dương", "Chiến Lang"};

    /**
     * Id anh con thu tren ban do. Skin thu n dung id ID_ANH_BAN_DO + n cho CA anh atlas
     * lan du lieu frame (client tra ca hai bang cung mot id, xem DataSkillEff.paint*).
     */
    public static final int ID_ANH_BAN_DO = 13000;

    /**
     * Id template cua 5 vat pham skin trong bang data_item, dung thu tu skin 0..4.
     * Tao bang sql/skin_linh_thu.sql. Doi id trong SQL thi phai doi o day cho khop.
     */
    public static final int[] ID_ITEM = {905, 906, 907, 908, 909};

    /**
     * Vat pham skin mang type 30.
     *
     * Bang "Trang bi thu" cua client co 10 o, thu tu type do client quy dinh san trong
     * idWearAnimal:
     *
     *     idWearAnimal = {18, 30, 14, 31, 15, 32, 16, 33, 17, 34}
     *      o chan (0,2,4,6,8) = cot trai   |   o le (1,3,5,7,9) = cot phai
     *
     * O DAU TIEN BEN PHAI ung voi type 30, nen vat pham skin phai mang type 30.
     *
     * VI SAO KHONG PHAI 19 (ban dau dat nham): hai client danh so cot phai KHAC NHAU.
     *   - ban JAR  (build/c276/.../WindowInfoScr.java:181): cot phai = 30..34, va mang
     *     posPaintWearing cua no dai 36 nen co san o 30..34 tai x=107.
     *   - ban Unity: port tu mot ban jar cu hon, cot phai con la 19..23, mang dai 20.
     * Gui type 19 thi ban Unity ve dung nhung ban JAR tra posPaintWearing[19] = (34, 92)
     * — tuc o PHI PHONG cua bang nhan vat — nen icon skin roi xuong goc duoi giua hai cot.
     * Da thong nhat ve cach danh so cua ban JAR: server gui 30, ban Unity sua idWearAnimal
     * cho khop. Nho vay ban JAR chay dung ma KHONG phai dong goi lai jar client.
     *
     * Loi thu hai duoc go luon: type 19 cung la type cua "Phi phong" nhan vat
     * (Map.isPhiPhong). Dung chung mot so cho hai thu khac nhau khien moi cho phai phan
     * biet bang ID TEMPLATE. Type 30 khong dung voi bat ky do mac nao: trong data_item
     * chi co dong id=30 "ech quy" — do la ban ghi LOAI THU, khong phai vat pham mac duoc.
     *
     * Van giu loc theo ID TEMPLATE (SkinLinhThu.laItemSkin) o moi cho, vi do la thu chac
     * chan nhat va da chay on:
     *   - Map.exchangeItem       : day sang duong trang bi thu.
     *   - CharVongLap.getAnimalWearing : nhan skin bat ke idAnimal.
     *   - CharTrangBi (nap lai)  : chan skin roi vao nhanh do nhan vat.
     *
     * Doi so nay phai doi kem trong data_item — xem sql/sua_type_skin_linh_thu.sql.
     */
    public static final int TYPE_ITEM = 30;

    /**
     * Dong bang bu toa do nguoi ngoi (Char.dx_horse_tool / dy_horse_tool), gui kem
     * idThutool. Dong 0 = {0,0,7,-7} / {-20,-15,-15,-15}, dung dong ma con Poro dang
     * dung: cac skin deu la thu bon chan co xap xi Poro nen ngoi vua.
     *
     * Bang chi co 5 dong (Char.java:764) nen gia tri hop le la 0..4; -1 = khong bu.
     * Gui -1 thi nguoi ngoi tut xuong sat dat, gui >= 5 thi client nem loi giua luc ve.
     */
    public static final int INDEX_DXY = 0;

    /**
     * Cach di chuyen: 0 = di bo, 1 = bay. Ca 5 skin deu la thu bon chan di tren dat.
     *
     * Gia tri nay di vao byte "fly" cua goi thong tin nhan vat; client dung no de quyet
     * dinh tu the ngoi, do cao va co ve bong cham dat hay khong (Client/Char.anmFly).
     */
    public static final int[] BAY = {0, 0, 0, 0, 0};

    // ====================================================================================
    //                              NOI TAI THUONG TRUC
    // ====================================================================================

    /**
     * Chi so skin cong cho CHU NHAN, luon bat khi dang mang skin (khong ti le, khong
     * hoi chieu). Thu tu moi bang khop thu tu skin 0..4 nhu TEN va ID_ITEM.
     *
     * VI SAO DE SO O DAY MA KHONG DE TRONG data_item.atb:
     *   - So la CO DINH theo tung skin, khong quay ngau nhien theo tung mon do, nen no la
     *     hang so cua tinh nang chu khong phai thuoc tinh cua vat pham.
     *   - Duong atb cua trang bi thu bi loc theo TYPE o moi cho tinh chi so
     *     (calculateAttack nhan 15/17, calculateDefend nhan 14/16/18) — type 30 roi ra
     *     ngoai het, nen di duong do van phai sua tung cho. Sua thang o tung cho thi ro
     *     rang hon la nhoi so vao DB roi van phai sua.
     *   - De o day thi phep kiem doc duoc, va doi so khong phai chay SQL tren may chu.
     *
     * Hien thi cho nguoi choi di duong khac: xem SkinLinhThuMoTa (bang data_attribute da
     * co san ten cho moi dong nay — 3 Chinh xac, 31 Xuyen giap, 33 Tang HP, 34 Tang MP,
     * 81 Hap thu sat thuong).
     */

    /** Phuc Thu: cong thang vao maxhp / maxmp cua chu nhan. */
    public static final int[] CONG_HP = {2000, 0, 0, 0, 0};
    public static final int[] CONG_MP = {3000, 0, 0, 0, 0};

    /**
     * Gau Truc: cong THANG vao diem chinh xac cuoi cung cua nhan vat.
     *
     * Cong o CUOI Char.calculateAccurate, sau ca phep chia 7 lan cai tran 99, nen 200 la
     * 200 that. Cong vao accurate2 giua ham thi no bi chia 7 con khoang 28, va thanh 0
     * han voi nhan vat da cham tran — khong dung y "cong thang vao nhan vat".
     *
     * Do lon thuc te: cong thuc trung don la random(accurate + 10) < accurate, tuc ti le
     * accurate/(accurate+10). Duong cong nay bao hoa rat nhanh, nen +200 dang ke voi nhan
     * vat chinh xac thap va gan nhu khong doi gi voi nhan vat da cao. Do la cach game
     * dung chinh xac tu truoc, khong phai rieng skin.
     */
    public static final int[] CONG_CHINH_XAC = {0, 200, 0, 0, 0};

    /**
     * Xich Duong: xuyen giap, cong vao Char.attNguHanh[3].
     *
     * XUYEN GIAP O SERVER NAY LA TI LE CHU KHONG PHAI TRU GIAP: xem
     * Char.getBuffEffAttack — no quay r.nextInt(100) < attNguHanh[3], trung thi don do
     * BO QUA HOAN TOAN giap muc tieu (attackDam dat dam = att). Nen 5 = "5% so don bo
     * qua giap", khong phai "giam 5% giap moi don".
     */
    public static final int[] PC_XUYEN_GIAP = {0, 0, 0, 5, 0};

    /** Chien Lang: cong phan tram vao ca thu vat lan thu phep. */
    public static final int[] PC_THU = {0, 0, 0, 0, 20};

    /**
     * Chien Lang: phan tram sat thuong bi hap thu truoc khi tru mau.
     *
     * CONG DON voi hap thu san co cua linh thu (Animal.att[9]) va voi noi tai 3% cua Lan
     * — xem Char.checkHapthuSatThuong. Khong thay the, khong lay so lon hon.
     */
    public static final int[] PC_HAP_THU = {0, 0, 0, 0, 15};

    /**
     * Doc mot bang noi tai cho nhan vat nay. Tra 0 khi khong dang hien skin.
     *
     * Di qua dangMang() chu khong qua hopLe(idSkinLinhThu): noi tai phai tat cung luc voi
     * hinh anh. Nguoi xuong thu, bang chu dang giu thanh, hay may chu thieu file anh thi
     * khong thay con skin dau ca — cho ho chi so trong luc do la thuong cho thu ho khong
     * he co.
     */
    private static int noiTai(final int[] bang, final Char p) {
        return dangMang(p) ? bang[p.idSkinLinhThu] : 0;
    }

    public static int congHp(final Char p) {
        return noiTai(CONG_HP, p);
    }

    public static int congMp(final Char p) {
        return noiTai(CONG_MP, p);
    }

    public static int congChinhXac(final Char p) {
        return noiTai(CONG_CHINH_XAC, p);
    }

    public static int pcXuyenGiap(final Char p) {
        return noiTai(PC_XUYEN_GIAP, p);
    }

    public static int pcThu(final Char p) {
        return noiTai(PC_THU, p);
    }

    public static int pcHapThu(final Char p) {
        return noiTai(PC_HAP_THU, p);
    }

    public static boolean hopLe(final int skin) {
        return skin >= 0 && skin < SO_SKIN;
    }

    /** Template nay co phai vat pham skin khong? */
    public static boolean laItemSkin(final int idTemplate) {
        return skinCuaItem(idTemplate) != KHONG_CO;
    }

    /** Skin ma vat pham nay mang lai, KHONG_CO neu khong phai vat pham skin. */
    public static int skinCuaItem(final int idTemplate) {
        for (int i = 0; i < ID_ITEM.length; i++) {
            if (ID_ITEM[i] == idTemplate) {
                return i;
            }
        }
        return KHONG_CO;
    }

    public static String moTa(final int skin) {
        return hopLe(skin) ? TEN[skin] : "không có";
    }

    /**
     * Nhan vat co dang hien hinh skin khong. Day la CONG DUY NHAT: moi cho ghi de goi
     * tin deu hoi qua ham nay, nen chi can chan o day la chan het.
     *
     * Ba dieu kien:
     *   - Dang cuoi thu: skin doi hinh CON THU, khong co con thu thi khong co gi de doi.
     *     Xuong thu la ve lai nhan vat di bo binh thuong.
     *   - Co skin hop le.
     *   - KHONG phai bang chu dang giu thanh. Voi nguoi do, ca hai cho ghi idThutool
     *     (MessageCreator.createCharInfo va createMainCharInfoMessage) deu nam trong
     *     "if (animalRide != null && !isMasterClanAndHaveTown())" nen id anh skin khong
     *     bao gio duoc gui. Neu van tat duong ngua co dien (userHorse = 0) thi ho MAT
     *     HAN con thu — khong ve gi ca. Nen skin phai im lang han voi truong hop nay.
     *   - CO DU CA BA FILE anh cua skin. Cung mot ly do: bat skin len la tat duong ngua
     *     co dien, ma thieu du lieu frame thi duong thu cuoi tool cung khong ve duoc gi
     *     (client van tao DataSkillEff nhung listFrame rong nen paint*Horse thoat ngay)
     *     -> nguoi choi cuoi hu khong. Thieu file thi tha khong co skin con hon.
     */
    public static boolean dangMang(final Char p) {
        return p != null && p.animalRide != null && hopLe(p.idSkinLinhThu)
                && !p.isMasterClanAndHaveTown()
                && coDuAnh(p.idSkinLinhThu);
    }

    /**
     * Da nap du ca hai file cua skin nay chua (atlas ban do + du lieu frame).
     *
     * Truoc day con doi file thu ba la dai 3 frame cho bang "Trang bi thu". Bang do nay
     * ve CON THU THAT chu khong ve skin nua, nen dai do khong con duoc gui di dau ca.
     */
    public static boolean coDuAnh(final int skin) {
        return hopLe(skin)
                && DataGame.duLieuSkinThu(skin) != null
                && DataGame.anhBanDoSkinThu(skin) != null;
    }

    /** Id anh con thu tren ban do, -1 = khong mang skin. */
    public static int idAnhBanDo(final Char p) {
        return dangMang(p) ? ID_ANH_BAN_DO + p.idSkinLinhThu : -1;
    }

    /** Byte "fly" cua skin dang mang, -1 = khong mang skin. */
    public static int bay(final Char p) {
        return dangMang(p) ? BAY[p.idSkinLinhThu] : -1;
    }

    // ====================================================================================
    //                      SKIN DUOC QUYET DINH BOI VAT PHAM DANG MAC
    // ====================================================================================

    /**
     * Dat lai Char.idSkinLinhThu theo VAT PHAM skin dang mac o bang Trang bi thu.
     * Tra ve true neu skin vua doi (luc do nguoi goi gui lai goi tin hinh anh).
     *
     * Vat pham la NGUON DUY NHAT quyet dinh skin, khong co cot DB rieng: thao ra, ban
     * di, dang nhap lai deu chay qua day nen khong bao gio lech. Vat pham thi da duoc
     * luu san trong danh sach trang bi thu (Char.awItems -> getAllItemInfoSave) nen
     * skin tu dong ben qua cac lan dang nhap ma khong phai them cot nao.
     *
     * KHONG goi getAnimalWearing() trong ham nay: ham do duoc goi tu chinh
     * createAnimalWearingMessage, ma nguoi goi ham nay lai gui goi do -> goi vong tron.
     * Duyet thang awItems.
     */
    public static boolean dongBoTheoTrangBi(final Char p) {
        if (p == null) {
            return false;
        }
        int moi = KHONG_CO;
        if (p.awItems != null) {
            for (int i = 0; i < p.awItems.size(); i++) {
                final Item it = p.awItems.get(i);
                if (it == null || it.getTemplate() == null) {
                    continue;
                }
                final int s = skinCuaItem(it.getTemplate().id);
                if (hopLe(s)) {
                    moi = s;
                    break;
                }
            }
        }
        if (moi == p.idSkinLinhThu) {
            return false;
        }
        p.idSkinLinhThu = moi;
        return true;
    }

    /**
     * Thao skin dang mac, tra vat pham ve hanh trang. Goi tu lenh chat "boskin".
     *
     * Can co ham nay vi bang "Trang bi thu" ben client khong co nut thao do — xem chu
     * thich cho goi lenh trong Map.doChat.
     */
    public static void thaoSkin(final Char p) {
        if (p == null) {
            return;
        }
        Item item = null;
        if (p.awItems != null) {
            for (int i = 0; i < p.awItems.size(); i++) {
                final Item it = p.awItems.get(i);
                if (it != null && it.getTemplate() != null && laItemSkin(it.getTemplate().id)) {
                    item = it;
                    break;
                }
            }
        }
        if (item == null) {
            p.sendMessage(MessageCreator.createServerAlertMessage(
                    "Bạn không mang skin linh thú nào.", ""));
            return;
        }
        if (p.isFullInventory()) {
            p.sendMessage(MessageCreator.createServerAlertMessage(
                    "Hành trang đầy, không tháo skin ra được.", ""));
            return;
        }
        p.awItems.remove(item);
        p.iItems.add(item);
        item.place = 0;
        item.idAnimal = -1;
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        dongBoVaGui(p);
    }

    /**
     * Dong bo skin theo trang bi roi gui lai moi thu client can de ve dung ngay: goi
     * thong tin nhan vat cho minh va cho nguoi ben canh, goi hinh thu cuoi.
     *
     * Goi sau khi mac / thao vat pham, khi cuoi thu, khi xuong thu, khi dang nhap.
     */
    public static void dongBoVaGui(final Char p) {
        if (!dongBoTheoTrangBi(p)) {
            return;
        }
        try {
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            // Nguoi xung quanh chi can createCharInfo: do la goi mang idThutool, tuc hinh
            // con thu tren ban do. KHONG gui createAnimalWearingMessage cho ho — goi do
            // lam client cua ho nhay sang bang "Trang bi thu" cua minh (GameScr
            // .onAnimalWearingInfo -> onViewAnimalInfo -> switchToMe), va no cung khong
            // mang du lieu nao dung de ve tren ban do.
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            if (p.animalRide != null) {
                p.sendMessage(MessageCreator.createAnimalWearingMessage(p, p));
            }
            p.sendMessage(MessageCreator.createServerAlertMessage(thongBaoDoiSkin(p), ""));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cau thong bao sau khi skin vua doi. Ba truong hop chu khong phai hai: co truong hop
     * nguoi choi MAC VAO duoc vat pham nhung skin van khong hien (bang chu dang giu thanh,
     * hoac may chu thieu file anh). Bao "da bo skin" luc do la noi doi, con bao "da trang
     * bi" thi ho se di tim xem hinh doi o dau.
     */
    public static String thongBaoDoiSkin(final Char p) {
        if (p == null || !hopLe(p.idSkinLinhThu)) {
            return "Đã bỏ skin linh thú";
        }
        if (dangMang(p)) {
            return "Đã trang bị skin linh thú " + moTa(p.idSkinLinhThu);
        }
        if (!coDuAnh(p.idSkinLinhThu)) {
            return "Đã mặc skin " + moTa(p.idSkinLinhThu)
                    + " nhưng máy chủ thiếu ảnh nên chưa hiện được.";
        }
        return "Đã mặc skin " + moTa(p.idSkinLinhThu)
                + " nhưng đang giữ thành nên chưa hiện được.";
    }
}
