package real;

/**
 * SKIN THU CUOI THEO NGU HANH (ten cu: skin thu nuoi)
 *
 * 5 skin, moi skin cho chu nhan mot bo chi so cong thang + mot buff bi dong
 * co ti le kich hoat va thoi gian hoi (cooldown):
 *
 *   0 Phuc Thu  (he Kim)  : +2000 HP, +3000 MP
 *                           bi danh boi class vat ly (Kiem khach / Chien binh / Dau si)
 *                           -> giam 30% sat thuong nhan vao trong 3s. Ti le 15%, cd 40s.
 *   1 Gau Truc  (he Moc)  : +200 chinh xac (nhanh nhen)
 *                           bi danh boi class phep (Phap su / Cung thu)
 *                           -> +50% phan sat thuong trong 3s (toi da 80%). Ti le 15%, cd 40s.
 *   2 Tien Loc  (he Thuy) : +20% diem tiem nang
 *                           bi danh don thuong -> la chan chan 50% sat thuong trong 5s.
 *                           Ti le 10%, cd 40s.
 *   3 Xich Duong(he Hoa)  : +5% xuyen giap
 *                           HP tut duoi 30% -> +50% tan cong trong 3s. Cd 40s (ti le 100%).
 *   4 Chien Lang(he Tho)  : +20% thu (vat + ma)
 *                           +15% hap thu sat thuong (thuong truc, khong can kich hoat).
 *
 * CACH MANG SKIN: mac VAT PHAM SKIN vao o dau tien ben phai bang "Trang bi thu"
 * (5 vat pham type 19, id template trong ID_ITEM, tao bang sql/skin_thu_item.sql).
 *   - Phai dang cuoi thu moi mac duoc: vat pham gan theo tung con thu cuoi (Item.idAnimal),
 *     giong het cac trang bi thu khac.
 *   - Thao ra / ban di / doi con thu / xuong thu -> mat skin, tu dong dong bo
 *     (SkinThuNuoi.dongBoTheoTrangBi goi tu Map.doUseItem, doRideAnimal, doUnRideHorse,
 *     SelectCharHandler).
 *   - Nguoi choi go "skinthu" chi de xem dang mang skin gi.
 *
 * Cach phat vat pham: giftcode "DB:<idTemplate>:1:0" (950..954), NPC shop, hoac su kien.
 * Duong cu (co "so huu skin" ghi bang bit trong tob_skin_thu, lenh "giveskin", menu doi
 * skin) van con nhung chi de ADMIN kiem tra: dat skin bang tay se bi dong bo lai theo
 * vat pham dang mac ngay lan mac do / doi thu / dang nhap sau.
 *
 * CACH DUNG (4 diem noi vao code goc):
 *   1) Char.calculateAttrib() - CUOI HAM (sau calculateCritical):
 *          SkinThuNuoi.apDungChiSo(this);
 *   2) Char.checkHapthuSatThuong(dam, actor) - dau ham:
 *          finalDam = SkinThuNuoi.khiNhanSatThuong(this, finalDam, actor);
 *   3) Char.getAttack() - buff no cua Xich Duong:
 *          atk += atk * SkinThuNuoi.pcTangSatThuong(this) / 100;
 *   4) Char.attackDam() - xuyen giap, ngay canh "Than khi: xuyen thu":
 *          def -= def * SkinThuNuoi.pcXuyenGiap(this) / 100;
 *
 * Cach hien thuc hoa tung chi so trong engine nay:
 *   - "chinh xac"      -> field accurate (dung field chinh xac cua engine)
 *   - "diem tiem nang" -> cong % vao TAT CA chi so dan xuat (tan cong, HP, MP,
 *                         thu vat, thu ma, chinh xac, ne, bao kich). Khong ghi
 *                         vao chi so goc vi chi so goc duoc luu DB (xem canh
 *                         bao o apDungChiSo).
 *   - "xuyen giap"     -> tru % giap cua muc tieu trong vong tinh sat thuong
 *                         (PvP + PvE), giong co che "xuyen thu" cua Than khi.
 *   - "phan sat thuong"-> tru hp ke tan cong + goi tin 89 (giong buffAttackSkill).
 *                         Chan o 1 HP: phan don khong bao gio giet.
 */
public class SkinThuNuoi {

    public static final int KHONG_CO = -1;
    public static final int PHUC_THU = 0;
    public static final int GAU_TRUC = 1;
    public static final int TIEN_LOC = 2;
    public static final int XICH_DUONG = 3;
    public static final int CHIEN_LANG = 4;

    public static final String[] TEN = {"Phúc Thú", "Gấu Trúc", "Tiên Lộc", "Xích Dương", "Chiến Lang"};
    public static final String[] TEN_HE = {"Kim", "Mộc", "Thủy", "Hỏa", "Thổ"};
    /** He cua tung skin: Phuc Thu-Kim, Gau Truc-Moc, Tien Loc-Thuy, Xich Duong-Hoa, Chien Lang-Tho */
    public static final int[] HE_SKIN = {0, 1, 2, 3, 4};

    // ===================== CHI SO CONG THANG =====================
    public static final int[] ADD_HP = {2000, 0, 0, 0, 0};
    public static final int[] ADD_MP = {3000, 0, 0, 0, 0};
    public static final int[] ADD_CHINH_XAC = {0, 200, 0, 0, 0};
    /**
     * "+% diem tiem nang" = cong % vao TAT CA chi so: tan cong, HP, MP, thu vat,
     * thu ma, chinh xac, ne, bao kich.
     */
    public static final int[] PC_TIEM_NANG = {0, 0, 20, 0, 0};
    /**
     * "+% xuyen giap" = BO QUA % giap cua muc tieu khi tinh sat thuong
     * (khong phai cong % sat thuong). Cam o Char.attackDam, ngay canh cho
     * "Than khi: xuyen thu" - ap cho ca PvP va PvE.
     */
    public static final int[] PC_XUYEN_GIAP = {0, 0, 0, 5, 0};
    /** % cong vao phong thu vat + ma */
    public static final int[] PC_THU = {0, 0, 0, 0, 20};
    /** % giam sat thuong nhan vao, thuong truc */
    public static final int[] PC_HAP_THU = {0, 0, 0, 0, 15};

    // ===================== BUFF BI DONG =====================
    /** Ti le kich hoat (%) khi trung dieu kien. 100 = luon kich hoat khi du dieu kien. */
    public static final int[] TI_LE_XH = {15, 15, 10, 100, 0};
    /** Thoi gian hoi giua 2 lan kich hoat (ms) */
    public static final long[] COOLDOWN = {40000L, 40000L, 40000L, 40000L, 0L};
    /** Thoi luong buff (ms) */
    public static final long[] THOI_LUONG = {3000L, 3000L, 5000L, 3000L, 0L};
    /** Muc do buff (%): giam sat thuong / phan sat thuong / chan sat thuong / tang tan cong */
    public static final int[] MUC_BUFF = {30, 50, 50, 50, 0};
    /** Tran cua buff (%) - 0 = khong gioi han. Gau Truc toi da 80%. */
    public static final int[] TRAN_BUFF = {0, 80, 0, 0, 0};

    /** HP con duoi bao nhieu % thi Xich Duong kich hoat */
    public static final int NGUONG_HP_XICH_DUONG = 30;

    // ============================================================
    //                    HINH ANH CUA SKIN
    // ============================================================
    // Skin doi hinh THU CUOI (thu dang cuoi), khong phai thu nuoi/pet.
    // Client ve con thu cuoi hoan toan theo hai thu server gui trong
    // MessageCreator.createAnimalWearingMessage: mot byte so frame + toan bo byte
    // anh png. No khong dung id thu cuoi de tra cuu gi ca, nen chi can thay hai
    // thu do la doi duoc hinh — khong phai sua client.
    //
    // Hinh cua 5 skin cat tu 5 bang sprite goc map/sprite_goc/bang_*.png bang chuoi
    // tool trong tools/tao_skin_thu.sh, dat o map/avtathu/21..25.png (0..20 la hinh
    // thu cuoi goc cua game nen 21..25 con trong).
    //
    //   0 Phuc Thu -> avtathu/21.png   1 Gau Truc -> 22.png   2 Tien Loc -> 23.png
    //   3 Xich Duong -> 24.png         4 Chien Lang -> 25.png
    //
    // MOI DAI PHAI DUNG 3 FRAME. Bang "Trang bi thu" ben client chay hoat anh bang
    // mot bang frame co dinh (WindowInfoScr.cs:43, MenuWindows.cs, ShopNew.cs):
    //     arrFrame = { {0,1,2}, {0,1} }
    //     idFrame  = arrFrame[(numFrame != 3) ? 1 : 0][countFrame]
    //     nhip     = (numFrame != 3) ? 6 : 3 tick
    // Gui 5/6/8 frame (nhu ban dau) thi client CHI ve frame 0 va 1 voi nhip 6 tick —
    // con thu gan nhu dung im, cac frame con lai khong bao gio duoc ve. Gui dung 3
    // frame thi duoc ca ba tu the voi nhip nhanh gap doi. Cat lai dai bang
    // tools/TaoAvtThuTuBang.java (chay qua tools/tao_skin_thu.sh). Client la ban
    // decompile khong build duoc nen phai chinh o server.
    //
    // Phai di kem DataGame nap map/avtathu/0..25 (truoc day chi nap 0..20), khong
    // thi imgAvtAnimal khong co phan tu 21..25 va server nem loi khi gui.
    public static final int[] ID_IMG = {21, 22, 23, 24, 25};
    /** So frame cua sprite tuong ung. -1 = giu so frame goc cua thu cuoi. */
    public static final int[] TONG_FRAME = {3, 3, 3, 3, 3};

    // ---- Anh con thu CHAY TREN BAN DO (duong dung) ----
    // Rong Bang, Poro, Lan, Su Tu... va ca hoa hinh deu duoc ve bang he "thu cuoi
    // tool": Animal.getIdImageTool() tra ve id 8700+N, server gui trong goi thong tin
    // nhan vat, client goi Char.getPartEff(id) -> DataSkillEff. Du lieu frame da duoc
    // gui san o goi login (message 26, danh sach MessageCreator.id_eff_tool_ngu_long)
    // con anh thi client xin theo id luc can. Vi vay he nay KHONG phu thuoc bo anh
    // cache — khac han duong imgAvtAnimal ben tren.
    //
    // Sprite dung bang tools/TaoEffThuCuoi.java (chay qua tools/tao_skin_thu.sh), ghi
    // vao map/d_effect/159, 197, 198, 199, 212. Xem PHAN J trong HUONG_DAN_BUILD_VPS.md:
    // co 5 cai bay o day (21 frame chu khong phai 18, anh phai nhin sang trai, nen bang
    // sprite trong suot chu khong den, bo dem anh cua client, va bong do + nhip nhun).
    //
    // Doi id moi khi lam lai sprite la viec BAT BUOC, khong phai cho gon: client giu
    // anh trong GameData.listImgIcon theo id, khong he co so phien ban, va chi bo khi
    // TAT HAN app (dang cuoi thu thi timeRemove lien tuc duoc lam moi nen khong bao gio
    // het han). Dang nhap lai chi lam moi phan DU LIEU frame (ALL_DATA_EFF.put trong
    // goi 26) — du lieu moi ghep vao anh cu thi ra mot khoi vun cua nhieu frame. Vi vay
    // moi co guiLaiAnhSkin() ben duoi.
    public static final int[] ID_EFF_THU_CUOI = {8859, 8897, 8898, 8899, 8912};

    /**
     * CONG TAT phan doi sprite con thu tren BAN DO. Dat false la tro lai y nguyen trang
     * thai truoc khi co tinh nang: 5 id 159/197/198/199/212 khong duoc them vao
     * MessageCreator.id_eff_tool_ngu_long va con thu cuoi ve lai hinh that. Cac phan con
     * lai cua skin (anh trong bang Trang bi thu, icon, vat pham, chi so) van chay.
     *
     * De o day de doi mot dong roi build lai la xong, vi day la phan duy nhat cua skin
     * co anh huong TOAN MAY CHU: 5 id do di trong goi 26 — goi dung chung cho moi nguoi
     * choi, va la goi mang theo ca bang toa do ve nhan vat / vu khi / phi phong / thu
     * cuoi. Sai o day la ca may chu ve sai, khong rieng ai. Xem PHAN J trong
     * HUONG_DAN_BUILD_VPS.md.
     *
     * Luu y khi bat/tat: client giu ANH theo id trong GameData.listImgIcon toi luc tat
     * han app, nen doi cong tat nay xong nguoi choi phai TAT HAN APP roi mo lai, khong
     * thi con thu (hoac hieu ung mang id do) ve bang anh cu ghep du lieu moi.
     */
    public static final boolean SPRITE_BAN_DO = true;

    /**
     * Day anh sprite cua skin dang mang xuong client, khong cho client tu xin.
     *
     * Client giu anh theo id trong GameData.listImgIcon va khong co so phien ban; muc chi
     * bi bo khi bang co hon 100 phan tu VA ranh qua 120s, nen thuc te no song den luc tat
     * han app. Trong khi do du lieu frame lai duoc ghi lai moi lan dang nhap. Sua sprite
     * cua id dang dung ma khong day anh moi thi client ghep du lieu moi voi anh cu ->
     * hien ra mot khoi vun cua nhieu frame.
     */
    public static void guiLaiAnhSkin(final Char p) {
        if (p == null || !hopLe(p.idSkinThuNuoi)) {
            return;
        }
        final io.Message m = data.DataGame.taoGoiAnhDynamicEffect(ID_EFF_THU_CUOI[p.idSkinThuNuoi]);
        if (m != null) {
            p.sendMessage(m);
        }
    }

    /** Id sprite thu cuoi cua skin dang mang, -1 = khong mang skin (hoac da tat cong tat). */
    public static int idEffThuCuoi(final Char p) {
        if (!SPRITE_BAN_DO || p == null || !hopLe(p.idSkinThuNuoi)) {
            return -1;
        }
        return ID_EFF_THU_CUOI[p.idSkinThuNuoi];
    }

    // ============================================================
    //        VAT PHAM SKIN — O DAU TIEN BEN PHAI BANG TRANG BI THU
    // ============================================================
    // Bang "Trang bi thu" cua client co 10 o: 5 o BEN TRAI la trang bi thu that
    // (yen 18, giap 14, ho uyen 15, non 16, ban dap 17) va 5 o BEN PHAI. Thu tu o do
    // client quy dinh san trong idWearAnimal (WindowInfoScr.cs:361, MenuWindows.cs:229,
    // ShopNew.cs:371, ba cho giong nhau):
    //
    //     idWearAnimal = {18, 19, 14, 20, 15, 21, 16, 22, 17, 23}
    //      o chan (0,2,4,6,8) = cot trai   |   o le (1,3,5,7,9) = cot phai
    //
    // Nghia la O DAU TIEN BEN PHAI (selected = 1) ung voi type 19. Vi vay vat pham skin
    // dung TYPE 19: chon o do trong bang thi client tim trong danh sach trang bi thu mot
    // vat pham type 19 de hien mo ta (showInfoWearingAnimal).
    //
    // LUU Y type 19 cung la type cua "Phi phong" (do nhan vat). Hai ben khong lan nhau
    // vi phan biet bang ID TEMPLATE, khong bang type:
    //   - Map.exchangeItem: template nam trong ID_ITEM thi day sang duong trang bi thu
    //     (awItems) chu khong vao o phi phong cua nhan vat.
    //   - Char.getAnimalWearing: chi nhan type 19 neu template nam trong ID_ITEM.
    //   - Phi phong that (template khac) van di duong cu, khong doi gi.
    public static final int TYPE_ITEM = 19;

    /**
     * Id template cua 5 vat pham skin, theo dung thu tu skin 0..4.
     * Tao bang sql/skin_thu_item.sql (id 950..954, cot xstart = id icon 815..819).
     * Doi id trong SQL thi phai doi o day cho khop.
     */
    public static final int[] ID_ITEM = {950, 951, 952, 953, 954};

    /** Template nay co phai vat pham skin khong? */
    public static boolean laItemSkin(final int idTemplate) {
        for (int i = 0; i < ID_ITEM.length; i++) {
            if (ID_ITEM[i] == idTemplate) {
                return true;
            }
        }
        return false;
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

    /**
     * Dat lai idSkinThuNuoi theo VAT PHAM dang mac o o Trang bi thu. Tra ve true neu
     * skin thay doi (luc do nguoi goi gui lai goi tin hinh anh + chi so).
     *
     * Vat pham skin la NGUON DUY NHAT quyet dinh skin dang mang: thao ra, ban di, doi
     * thu cuoi khac hay dang nhap lai deu chay qua day. Cot idskin trong tob_skin_thu
     * chi con la ban luu tam de khong phai doc lai vat pham o moi cho.
     *
     * KHONG gui goi tin va KHONG goi getAnimalWearing() trong ham nay: getAnimalWearing
     * duoc goi tu createAnimalWearingMessage, ma nguoi goi ham nay lai gui chinh goi do
     * -> goi lai nhau vong tron. Duyet thang p.awItems va tu doi chieu idAnimal.
     */
    public static boolean dongBoTheoTrangBi(final Char p) {
        if (p == null) {
            return false;
        }
        int moi = KHONG_CO;
        if (p.animalRide != null && p.awItems != null) {
            for (int i = 0; i < p.awItems.size(); i++) {
                final Item it = p.awItems.get(i);
                if (it == null || it.idAnimal != p.animalRide.dbid) {
                    continue;
                }
                final int s = skinCuaItem(it.getTemplate().id);
                if (hopLe(s)) {
                    moi = s;
                    break;
                }
            }
        }
        if (moi == p.idSkinThuNuoi) {
            return false;
        }
        p.idSkinThuNuoi = moi;
        p.timeHetBuffSkinThuNuoi = 0L;
        p.timeCdSkinThuNuoi = 0L;
        try {
            data.Database.instance.saveSkinThuNuoi(p);
        } catch (final Exception e) {
        }
        return true;
    }

    /**
     * Dong bo skin theo trang bi roi gui lai moi thu client can de ve dung ngay:
     * chi so, anh sprite thu cuoi, goi thong tin thu cuoi cho minh va cho nguoi ben canh.
     * Goi sau khi mac/thao vat pham, khi cuoi thu, khi xuong thu.
     */
    public static void dongBoVaGui(final Char p) {
        if (!dongBoTheoTrangBi(p)) {
            return;
        }
        try {
            p.calculateAttrib();
            // Day anh truoc khi gui goi thong tin thu cuoi: client giu anh theo id va khong
            // co so phien ban nen neu con anh cu cua id nay thi phai ghi de ngay.
            guiLaiAnhSkin(p);
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendMessage(MessageCreator.createAnimalWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createAnimalWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            p.sendMessage(MessageCreator.createServerAlertMessage(hopLe(p.idSkinThuNuoi)
                    ? ("Đã trang bị skin " + moTa(p.idSkinThuNuoi))
                    : "Đã bỏ skin thú cưỡi", ""));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    //            MO TA VAT PHAM SKIN (dong chi so trong tooltip)
    // ============================================================
    // Client ghep tung dong mo ta la "<ten>: <gia tri>[%]" (InfoAttributeItem.getName +
    // getValue), trong do <ten> tra tu bang ten thuoc tinh nhan duoc luc dang nhap
    // (goi 25). CHI SO DUNG DE TRA CUU KHONG PHAI cot id trong data_attribute ma la
    // THU TU DONG khi doc "select * from data_attribute order by id"
    // (Database.loadNameAttributeItem dem tang dan, client cung luu theo thu tu nhan).
    //
    // Vi vay khong hardcode duoc chi so: chi can them mot dong data_attribute o giua la
    // moi thu lech. Thay vao do tim theo TEN, va tim TU CUOI LEN de neu ten bi trung voi
    // dong san co cua game thi lay dong minh them (dong minh id lon hon nen nam sau).
    // Khong tim thay thi dong do bi bo, vat pham chi mat mo ta chu khong loi.
    //
    // Cac ten nay PHAI trung tung chu voi sql/skin_thu_item.sql.
    public static final String[][] TEN_DONG_MOTA = {
        {"Sinh lực", "Nội lực", "Giảm sát thương khi bị đánh"},
        {"Chính xác cộng thêm", "Phản sát thương"},
        {"Tiềm năng cộng thêm", "Lá chắn chặn sát thương"},
        {"Xuyên giáp cộng thêm", "Nộ khí tăng tấn công"},
        {"Phòng thủ cộng thêm", "Hấp thụ sát thương"},
    };
    /** Gia tri hien o tung dong, khop 1-1 voi TEN_DONG_MOTA. */
    public static final int[][] GIA_TRI_DONG_MOTA = {
        {ADD_HP[0], ADD_MP[0], MUC_BUFF[0]},
        {ADD_CHINH_XAC[1], MUC_BUFF[1]},
        {PC_TIEM_NANG[2], MUC_BUFF[2]},
        {PC_XUYEN_GIAP[3], MUC_BUFF[3]},
        {PC_THU[4], PC_HAP_THU[4]},
    };
    /** Chi so dong trong bang ten thuoc tinh; -1 = chua tim thay. */
    private static final int[][] chiSoDong = new int[TEN_DONG_MOTA.length][];

    /**
     * Tim chi so dong cho tung ten trong TEN_DONG_MOTA. Goi mot lan sau khi
     * Database.loadNameAttributeItem() nap xong bang ten thuoc tinh.
     */
    public static void ganChiSoDongMoTa() {
        final java.util.Vector<NameAttributeItem> bang = ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM;
        int thieu = 0;
        for (int s = 0; s < TEN_DONG_MOTA.length; s++) {
            chiSoDong[s] = new int[TEN_DONG_MOTA[s].length];
            for (int d = 0; d < TEN_DONG_MOTA[s].length; d++) {
                chiSoDong[s][d] = -1;
                if (bang == null) {
                    continue;
                }
                // Tim tu cuoi len: dong minh them co id lon hon nen nam sau dong san co
                for (int i = bang.size() - 1; i >= 0; i--) {
                    final NameAttributeItem n = bang.get(i);
                    if (n != null && TEN_DONG_MOTA[s][d].equals(n.name)) {
                        chiSoDong[s][d] = i;
                        break;
                    }
                }
                if (chiSoDong[s][d] < 0) {
                    thieu++;
                }
            }
        }
        if (thieu > 0) {
            System.out.println("SkinThuNuoi: thieu " + thieu + " dong ten thuoc tinh trong"
                    + " data_attribute -> vat pham skin se thieu mo ta. Chay sql/skin_thu_item.sql.");
        }
    }

    /**
     * Cac dong mo ta gui xuong client cho vat pham skin. Goi tu Item.getInfoAtbItem va
     * Item.getInfoAtbItemWearing: vat pham skin khong dung cot atb0..9 nao (cac dong nay
     * la mo ta co dinh cua skin, khong phai chi so ngau nhien cua vat pham).
     */
    public static java.util.Vector<InfoItemAttribute> dongMoTaVatPham(final int idSkin) {
        final java.util.Vector<InfoItemAttribute> ds = new java.util.Vector<InfoItemAttribute>();
        if (!hopLe(idSkin) || chiSoDong[idSkin] == null) {
            return ds;
        }
        for (int d = 0; d < chiSoDong[idSkin].length; d++) {
            final int chiSo = chiSoDong[idSkin][d];
            if (chiSo >= 0) {
                ds.add(new InfoItemAttribute((short) chiSo, GIA_TRI_DONG_MOTA[idSkin][d]));
            }
        }
        return ds;
    }

    /** Id anh can gui xuong client cho nhan vat nay, -1 = giu nguyen. */
    public static int idImgHienThi(final Char p) {
        if (p == null || !hopLe(p.idSkinThuNuoi)) {
            return -1;
        }
        return ID_IMG[p.idSkinThuNuoi];
    }

    /** So frame can gui xuong client, -1 = giu nguyen. */
    public static int tongFrameHienThi(final Char p) {
        if (p == null || !hopLe(p.idSkinThuNuoi)) {
            return -1;
        }
        return TONG_FRAME[p.idSkinThuNuoi];
    }

    private SkinThuNuoi() {
    }

    public static boolean hopLe(final int idSkin) {
        return idSkin >= 0 && idSkin < TEN.length;
    }

    public static String moTa(final int idSkin) {
        if (!hopLe(idSkin)) {
            return "Không có skin";
        }
        return TEN[idSkin] + " [hệ " + TEN_HE[HE_SKIN[idSkin]] + "]";
    }

    /** Class tan cong vat ly: Kiem khach (0), Chien binh (1), Dau si (3). */
    public static boolean laClassVatLy(final LiveActor actor) {
        if (!(actor instanceof Char)) {
            return false;
        }
        final int cl = ((Char) actor).charClass;
        return cl == 0 || cl == 1 || cl == 3;
    }

    /** Class tan cong phep: Phap su (2), Cung thu (4). Dung lai isCharMagic() cua engine. */
    public static boolean laClassPhep(final LiveActor actor) {
        return (actor instanceof Char) && ((Char) actor).isCharMagic();
    }

    // ============================================================
    //                     CHI SO CONG THANG
    // ============================================================

    /**
     * Goi o CUOI Char.calculateAttrib() (sau ca 6 ham calculate*). Cong chi so
     * cua skin dang mang.
     *
     * QUAN TRONG - CHI DUOC SUA CHI SO DAN XUAT:
     *   calculateAttrib() tinh lai attack / maxhp / maxmp / accurate / dodge /
     *   defend_* / baokich tu dau moi lan goi, nen cong vao day la an toan
     *   (khong bi cong don).
     *   Nguoc lai strength / agitity / spirit / health / luck la chi so GOC, KHONG duoc
     *   tinh lai trong calculateAttrib va con duoc ghi thang vao DB o saveChar
     *   (cot "basic"). Cong vao chung se bi cong don moi lan goi calculateAttrib VA
     *   duoc luu vinh vien -> chi so phinh vo han. Tuyet doi khong sua chung o day.
     *
     *   Vi vay "+20% diem tiem nang" duoc lam bang cach cong 20% vao TAT CA chi so
     *   dan xuat ma diem tiem nang sinh ra (tan cong, HP, MP, thu, chinh xac, ne,
     *   bao kich) - ket qua nguoi choi thay giong het cong 20% vao diem tiem nang,
     *   nhung khong ghi de len chi so goc trong DB.
     */
    public static void apDungChiSo(final Char p) {
        if (p == null || !hopLe(p.idSkinThuNuoi)) {
            return;
        }
        final int s = p.idSkinThuNuoi;

        if (ADD_HP[s] > 0) {
            p.maxhp += ADD_HP[s];
        }
        if (ADD_MP[s] > 0) {
            p.maxmp += ADD_MP[s];
        }
        // "chinh xac" -> field dan xuat accurate
        if (ADD_CHINH_XAC[s] > 0) {
            p.accurate += ADD_CHINH_XAC[s];
        }
        // "+20% diem tiem nang" -> cong 20% vao tat ca chi so (xem giai thich tren)
        if (PC_TIEM_NANG[s] > 0) {
            final int pc = PC_TIEM_NANG[s];
            p.attack += p.attack * pc / 100;
            p.attackPlayer += p.attackPlayer * pc / 100;
            p.maxhp += p.maxhp * pc / 100;
            p.maxmp += p.maxmp * pc / 100;
            p.accurate += p.accurate * pc / 100;
            p.dodge += p.dodge * pc / 100;
            p.baokich += p.baokich * pc / 100;
            p.defend_physic += p.defend_physic * pc / 100;
            p.defend_magic += p.defend_magic * pc / 100;
            p.defendVatPlayer += p.defendVatPlayer * pc / 100;
            p.defendMaPlayer += p.defendMaPlayer * pc / 100;
            // dodge co tran 299 trong engine, giu nguyen tran do
            if (p.dodge > 299) {
                p.dodge = 299;
            }
        }
        if (PC_THU[s] > 0) {
            p.defend_physic += p.defend_physic * PC_THU[s] / 100;
            p.defend_magic += p.defend_magic * PC_THU[s] / 100;
            p.defendVatPlayer += p.defendVatPlayer * PC_THU[s] / 100;
            p.defendMaPlayer += p.defendMaPlayer * PC_THU[s] / 100;
        }
        // Chan HP hien tai khong vuot maxhp sau khi doi chi so
        if (p.hp > p.maxhp) {
            p.hp = p.maxhp;
        }
    }

    // ============================================================
    //                  SAT THUONG GAY RA (tan cong)
    // ============================================================

    /**
     * % cong them vao TAN CONG: chi la buff "+50% tan cong khi HP < 30%" cua
     * Xich Duong (khi dang bat). Xuyen giap KHONG tinh o day - no tru vao giap
     * cua muc tieu, xem pcXuyenGiap().
     */
    public static int pcTangSatThuong(final Char p) {
        if (p == null || p.idSkinThuNuoi != XICH_DUONG) {
            return 0;
        }
        kiemTraXichDuong(p);
        return dangBatBuff(p) ? MUC_BUFF[XICH_DUONG] : 0;
    }

    /**
     * % giap cua muc tieu bi bo qua (xuyen giap). Goi trong Char.attackDam,
     * ap cho ca PvP va PvE.
     */
    public static int pcXuyenGiap(final Char p) {
        if (p == null || !hopLe(p.idSkinThuNuoi)) {
            return 0;
        }
        return PC_XUYEN_GIAP[p.idSkinThuNuoi];
    }

    // ============================================================
    //                  SAT THUONG NHAN VAO (bi danh)
    // ============================================================

    /**
     * Goi o DAU Char.checkHapthuSatThuong(). Tra ve sat thuong sau khi tru.
     * Dong thoi thu kich hoat buff bi dong cua skin.
     *
     * @param p     nhan vat bi danh
     * @param dam   sat thuong goc
     * @param actor nguon gay sat thuong (co the la Char hoac Monster/Boss)
     */
    public static int khiNhanSatThuong(final Char p, final int dam, final LiveActor actor) {
        if (p == null || dam <= 0 || !hopLe(p.idSkinThuNuoi)) {
            return dam;
        }
        final int s = p.idSkinThuNuoi;
        int ketQua = dam;

        // 1) Hap thu thuong truc (Chien Lang)
        if (PC_HAP_THU[s] > 0) {
            ketQua -= ketQua * PC_HAP_THU[s] / 100;
        }

        // 2) Thu kich hoat buff bi dong neu chua bat va da het cooldown
        thuKichHoat(p, s, actor);

        // 3) Neu buff dang bat thi xu ly theo tung loai skin
        if (dangBatBuff(p)) {
            switch (s) {
                case PHUC_THU:   // giam 30% sat thuong nhan vao
                case TIEN_LOC: { // la chan chan 50% sat thuong
                    int pcGiam = MUC_BUFF[s];
                    if (TRAN_BUFF[s] > 0 && pcGiam > TRAN_BUFF[s]) {
                        pcGiam = TRAN_BUFF[s];
                    }
                    ketQua -= ketQua * pcGiam / 100;
                    break;
                }
                case GAU_TRUC: {
                    // Phan sat thuong that: tra % sat thuong vua nhan ve ke tan cong.
                    int pcPhan = MUC_BUFF[s];
                    if (TRAN_BUFF[s] > 0 && pcPhan > TRAN_BUFF[s]) {
                        pcPhan = TRAN_BUFF[s];
                    }
                    phanSatThuong(p, ketQua * pcPhan / 100, actor);
                    break;
                }
                default:
                    break;
            }
        }

        return (ketQua > 0) ? ketQua : 1;
    }

    /**
     * Tra sat thuong ve ke tan cong (Gau Truc).
     * Lam theo dung cach engine dang lam o Char.buffAttackSkill: tru hp truc tiep roi
     * gui goi tin 89 de client hien so sat thuong.
     *
     * An toan: KHONG bao gio giet ke tan cong bang phan don (chan o 1 HP). Ly do la
     * engine xu ly cai chet o nhieu nhanh rieng (roi do, tru diem, hoi sinh...), tru
     * hp thang xuong 0 tu day se tao nhan vat "chet nhung chua chet" -> loi kho lan.
     */
    public static void phanSatThuong(final Char p, final int dam, final LiveActor actor) {
        if (p == null || actor == null || dam <= 0 || actor == p) {
            return;
        }
        try {
            int damPhan = dam;
            // Noi tai "Tra dua" cua Rong Lon + Hoa Ky Lan: ke bi phan don chi an mot nua.
            if (actor instanceof Char) {
                damPhan = ((Char) actor).giamSatThuongPhanDon(damPhan);
                if (damPhan <= 0) {
                    return;
                }
            }
            if (damPhan >= actor.hp) {
                damPhan = actor.hp - 1;
            }
            if (damPhan <= 0) {
                return;
            }
            actor.hp -= damPhan;
            final io.Message m = new io.Message(89);
            m.dos.writeShort(actor.id);
            m.dos.writeByte(actor.cat);
            m.dos.writeByte(0);
            m.dos.writeShort(damPhan > Short.MAX_VALUE ? Short.MAX_VALUE : damPhan);
            m.dos.writeByte(5);
            m.dos.writeByte(-1);
            m.dos.writeByte(0);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        } catch (final Exception e) {
        }
    }

    private static void thuKichHoat(final Char p, final int s, final LiveActor actor) {
        if (TI_LE_XH[s] <= 0 || THOI_LUONG[s] <= 0L) {
            return; // skin khong co buff bi dong (Chien Lang)
        }
        final long now = System.currentTimeMillis();
        if (dangBatBuff(p) || now < p.timeCdSkinThuNuoi) {
            return; // dang bat hoac chua het cooldown
        }
        boolean duDieuKien;
        switch (s) {
            case PHUC_THU:
                duDieuKien = laClassVatLy(actor);
                break;
            case GAU_TRUC:
                duDieuKien = laClassPhep(actor);
                break;
            case TIEN_LOC:
                duDieuKien = true; // don thuong bat ky
                break;
            case XICH_DUONG:
                duDieuKien = false; // xu ly rieng theo nguong HP
                break;
            default:
                duDieuKien = false;
                break;
        }
        if (!duDieuKien) {
            return;
        }
        if (Map.r.nextInt(100) >= TI_LE_XH[s]) {
            return;
        }
        batBuff(p, s, now);
    }

    /** Xich Duong: HP tut duoi nguong thi tu bat buff tang tan cong. */
    public static void kiemTraXichDuong(final Char p) {
        if (p == null || p.idSkinThuNuoi != XICH_DUONG || p.maxhp <= 0) {
            return;
        }
        final long now = System.currentTimeMillis();
        if (dangBatBuff(p) || now < p.timeCdSkinThuNuoi) {
            return;
        }
        if (p.hp * 100 / p.maxhp >= NGUONG_HP_XICH_DUONG) {
            return;
        }
        batBuff(p, XICH_DUONG, now);
    }

    private static void batBuff(final Char p, final int s, final long now) {
        p.timeHetBuffSkinThuNuoi = now + THOI_LUONG[s];
        p.timeCdSkinThuNuoi = now + COOLDOWN[s];
        try {
            p.sendMessage(MessageCreator.createMsgChat(p.id, TEN[s] + " kích hoạt!"));
        } catch (final Exception e) {
        }
    }

    public static boolean dangBatBuff(final Char p) {
        return p != null && System.currentTimeMillis() < p.timeHetBuffSkinThuNuoi;
    }

    // ============================================================
    //     CACH NGUOI CHOI CO SKIN: SU KIEN / GIFTCODE (khong ban)
    // ============================================================
    // KHONG co shop mua skin. Nguoi choi chi so huu skin qua:
    //   1) Giftcode: them "SKIN:<idSkin>" vao danh sach vat pham cua code.
    //      VD listItem = "SKIN:2"  hoac  "DB:1234:1:0,SKIN:0"
    //      (xu ly o Char.doAddGiftEventOffline + Char.doAddQuaTichLuy)
    //   2) Su kien: admin go "giveskin <tên nhân vật> <0-4>" (nguoi nhan phai online).
    //   3) Trong code su kien khac: goi SkinThuNuoi.tangSkin(char, idSkin).
    //
    // Skin da so huu duoc luu bang dang bit trong Char.skinDaSoHuu (bang tob_skin_thu,
    // cot sohuu) nen giu vinh vien. Nguoi choi so huu nhieu skin thi go "skinthu"
    // de doi qua lai giua CAC SKIN DA CO - khong the chon skin chua so huu.

    /** Nhan vat da so huu skin nay chua? */
    public static boolean daSoHuu(final Char p, final int idSkin) {
        return p != null && hopLe(idSkin) && (p.skinDaSoHuu & (1 << idSkin)) != 0;
    }

    /** So skin nhan vat dang so huu. */
    public static int soSkinDangCo(final Char p) {
        int n = 0;
        for (int i = 0; i < TEN.length; i++) {
            if (daSoHuu(p, i)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Tang skin cho nhan vat (su kien / giftcode). Tu dong trang bi luon neu nhan
     * vat chua mang skin nao. Tra ve false neu idSkin sai hoac da so huu tu truoc.
     */
    public static boolean tangSkin(final Char p, final int idSkin) {
        if (p == null || !hopLe(idSkin)) {
            return false;
        }
        if (daSoHuu(p, idSkin)) {
            try {
                p.sendMessage(MessageCreator.createServerAlertMessage(
                        "Bạn đã có skin " + moTa(idSkin) + " rồi.", ""));
            } catch (final Exception e) {
            }
            return false;
        }
        p.skinDaSoHuu |= (1 << idSkin);
        try {
            data.Database.instance.saveOrtherLog("", p.charname,
                    "nhan skin thu nuoi " + TEN[idSkin] + " (su kien/giftcode)", "skinthu");
        } catch (final Exception e) {
        }
        // Chua mang skin nao thi trang bi ngay cho nguoi choi thay lien;
        // dang mang skin khac thi giu nguyen, ho tu go "skinthu" de doi.
        if (!hopLe(p.idSkinThuNuoi)) {
            datSkin(p, idSkin);
        } else {
            luuVaThongBao(p, "Bạn nhận được skin thú nuôi " + moTa(idSkin)
                    + ".\nGõ \"skinthu\" để đổi skin đang mang.");
        }
        return true;
    }

    /** Thu hoi mot skin (admin go "giveskin <ten> -1" de xoa het). */
    public static void thuHoiHetSkin(final Char p) {
        if (p == null) {
            return;
        }
        p.skinDaSoHuu = 0;
        datSkin(p, KHONG_CO);
    }

    // ============================================================
    //      MENU DOI SKIN (chi liet ke skin DA SO HUU, mien phi)
    // ============================================================
    /** Id npc ao cho menu doi skin. Khong trung npc that nao. */
    public static final int ID_NPC_DOI_SKIN = -1001;

    /**
     * Mo menu doi skin. Chi hien cac skin nhan vat DA SO HUU. Khong so huu skin
     * nao thi bao khong co gi de doi.
     */
    public static void moMenuDoiSkin(final Char player) {
        if (player == null) {
            return;
        }
        if (soSkinDangCo(player) == 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Bạn chưa có skin thú nuôi nào.\nSkin chỉ nhận được qua sự kiện hoặc giftcode.", ""));
            return;
        }
        final java.util.ArrayList<String> menu = new java.util.ArrayList<String>();
        for (int i = 0; i < TEN.length; i++) {
            if (daSoHuu(player, i)) {
                menu.add(moTa(i) + (player.idSkinThuNuoi == i ? " (đang mang)" : ""));
            }
        }
        menu.add("Bỏ skin đang mang");
        player.sendMessage(MessageCreator.createMsgMenu(
                menu.toArray(new String[menu.size()]), ID_NPC_DOI_SKIN, 0));
    }

    /**
     * Goi tu Map.doMenu. Tra ve true neu lua chon nay thuoc menu doi skin.
     *
     * Luu y: idNpc trong goi tin do CLIENT gui len nen khong the tin. Vi vay o day
     * van kiem tra quyen so huu: option duoc doi chieu lai voi danh sach skin that
     * su cua nhan vat, client sua goi tin cung khong lay duoc skin chua co.
     */
    public static boolean xuLyDoiSkin(final Char player, final int idNpc, final int idOptionMenu) {
        if (idNpc != ID_NPC_DOI_SKIN) {
            return false;
        }
        if (player == null) {
            return true;
        }
        // Dung lai dung thu tu da gui o moMenuDoiSkin: option thu n = skin so huu thu n.
        int dem = 0;
        for (int i = 0; i < TEN.length; i++) {
            if (!daSoHuu(player, i)) {
                continue;
            }
            if (dem == idOptionMenu) {
                if (player.idSkinThuNuoi == i) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đang mang skin này rồi.", ""));
                } else {
                    datSkin(player, i);
                }
                return true;
            }
            dem++;
        }
        // Option ngoai danh sach = dong cuoi "Bo skin dang mang"
        datSkin(player, KHONG_CO);
        return true;
    }

    /** Doi skin cho nhan vat, tinh lai chi so va luu DB. */
    public static void datSkin(final Char p, final int idSkin) {
        if (p == null) {
            return;
        }
        p.idSkinThuNuoi = hopLe(idSkin) ? idSkin : KHONG_CO;
        p.timeHetBuffSkinThuNuoi = 0L;
        p.timeCdSkinThuNuoi = 0L;
        luuVaThongBao(p, hopLe(idSkin) ? ("Đã trang bị skin " + moTa(idSkin)) : "Đã bỏ skin thú nuôi");
    }

    private static void luuVaThongBao(final Char p, final String thongBao) {
        try {
            data.Database.instance.saveSkinThuNuoi(p);
            p.calculateAttrib();
            // Day anh truoc khi gui thong tin thu cuoi: neu client con giu anh cu cua
            // id nay thi ghi de ngay, khong thi no ve bang anh cu.
            guiLaiAnhSkin(p);
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            // Hinh con thu cuoi nam trong goi createAnimalWearingMessage chu khong
            // phai main char info, nen doi skin ma chi gui main char info thi client
            // van ve hinh cu cho toi khi thoat vao lai.
            p.sendMessage(MessageCreator.createAnimalWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createAnimalWearingMessage(p, p));
            p.sendMessage(MessageCreator.createServerAlertMessage(thongBao, ""));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
