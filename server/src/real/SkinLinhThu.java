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
    public static final int SO_SKIN = 6;

    /** Chi so skin cua Kim Cang Long - chi so DUY NHAT chi so ngau nhien theo vat pham. */
    public static final int KIM_CANG_LONG = 5;

    /** Ten skin, dung cho thong bao va mo ta vat pham. Thu tu khop ID_ITEM va file anh. */
    public static final String[] TEN = {"Phúc Thú", "Gấu Trúc", "Tiên Lộc", "Xích Dương", "Chiến Lang", "Kim Cang Long"};

    /**
     * Id anh con thu tren ban do. Skin thu n dung id ID_ANH_BAN_DO + n cho CA anh atlas
     * lan du lieu frame (client tra ca hai bang cung mot id, xem DataSkillEff.paint*).
     */
    public static final int ID_ANH_BAN_DO = 13000;

    /**
     * Id template cua 6 vat pham skin trong bang data_item, dung thu tu skin 0..5.
     * Tao bang sql/skin_linh_thu.sql. Doi id trong SQL thi phai doi o day cho khop.
     *
     * Skin 5 (Kim Cang Long) KHONG di theo bang hang so noi tai o duoi (xem CONG_HP..
     * BAO_KICH_HOAT) - chi so cua no la NGAU NHIEN THEO TUNG VAT PHAM, sinh luc tao bang
     * Item.createAttributeSkinKimCangLong() va doc lai tu chinh item dang mac (xem
     * timSkinDangMac). Vi vay tat ca bang hang so o duoi deu dat gia tri TRUNG LAP (0/false/
     * -1/DK_KHONG) cho chi so 5, dung theo mau "khong noi tai" da co san cua Chien Lang (4).
     */
    public static final int[] ID_ITEM = {905, 906, 907, 908, 909, 911};

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
     * Dong bang bu toa do nguoi ngoi (Char.dx_horse_tool / dy_horse_tool) cua TUNG SKIN,
     * gui kem idThutool. Dong 0 = {0,0,7,-7} / {-20,-15,-15,-15}, dung dong ma con Poro dang
     * dung: 5 skin dau deu la thu bon chan co xap xi Poro nen ngoi vua.
     *
     * Kim Cang Long (5) dung dong RIENG so 5 (Char.java, khai bao dx_horse_tool): no la rong
     * dai, yen nam dung tam than khi quay ngang nen nguoi khong duoc day sang ben 7px nhu
     * Poro (dx +1/-1); huong xuong nguoi ngoi sau dau rong, nhac -28; huong len nguoi ha
     * xuong 0 de than che phan tren yen (huong len client ve thu truoc nguoi, yen khong the
     * che nguoi). Hai so nay chinh theo anh chup nguoi choi gui: -23/-15 -> -28/-10 -> -28/0.
     * Truoc 11/09/2026 dung chung dong 0: nguoi lech 6px ve phia duoi khi quay ngang, con
     * huong xuong thi noi lo lung tren dau rong (nguoi choi bao kem anh chup).
     *
     * Bang do server GUI CHO CLIENT kem SO DONG trong goi 26, ca ban Unity
     * (MessageHandler.cs:1795) lan ban JAR (MessageHandler.java:1754) deu doc dong theo so
     * do - nen them dong moi la du, KHONG phai dong goi lai client. Gia tri hop le la
     * 0..dx_horse_tool.length-1; -1 = khong bu (nguoi ngoi tut xuong sat dat); gui so lon
     * hon bang thi client nem loi giua luc ve. test/KiemSkinLinhThu.kiemChoNgoiSkin chot
     * moi phan tu o day deu tro vao mot dong co that.
     */
    public static final int[] INDEX_DXY = {0, 0, 0, 0, 0, 5};

    /** Dong bang bu toa do cho skin dang mang; 0 (dong Poro) neu khong xac dinh duoc. */
    public static int indexDxy(final Char p) {
        return (p != null && hopLe(p.idSkinLinhThu)) ? INDEX_DXY[p.idSkinLinhThu] : INDEX_DXY[0];
    }

    /**
     * Cach di chuyen: 0 = di bo, 1 = bay. Ca 5 skin deu la thu bon chan di tren dat.
     *
     * Gia tri nay di vao byte "fly" cua goi thong tin nhan vat; client dung no de quyet
     * dinh tu the ngoi, do cao va co ve bong cham dat hay khong (Client/Char.anmFly).
     */
    public static final int[] BAY = {0, 0, 0, 0, 0, 0};

    // ====================================================================================
    //                              NOI TAI THUONG TRUC
    // ====================================================================================

    /**
     * Chi so skin cong cho CHU NHAN khi dang mang skin (khong ti le, khong hoi chieu).
     * Thu tu moi bang khop thu tu skin 0..4 nhu TEN va ID_ITEM.
     *
     * HAI NHOM KHAC NHAU, dung theo hai nhan "Tac dung chi so" / "Buff bi dong" trong ban
     * mo ta nguoi choi dua (xem hopHe() o duoi, phan "HE NGU HANH"):
     *   - "Buff bi dong" — CONG_HP, CONG_MP, PC_HAP_THU: LUON BAT, khong can dung he.
     *   - "Tac dung chi so" — CONG_CHINH_XAC, PC_XUYEN_GIAP, PC_TIEM_NANG, PC_THU: CHI BAT
     *     khi skin dang mang HOP HE voi mon phai cua chu nhan. Sai he thi noiTaiHopHe tra
     *     0, nguoi choi chi con nhom buff bi dong ben tren — dung y "sai he thi chi con
     *     buff bi dong chu khong co tac dung chu dong" nguoi choi yeu cau.
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

    /** Phuc Thu: cong thang vao maxhp / maxmp cua chu nhan — "Buff bi dong", luon bat. */
    public static final int[] CONG_HP = {2000, 0, 0, 0, 0, 0};
    public static final int[] CONG_MP = {3000, 0, 0, 0, 0, 0};

    /**
     * Gau Truc: cong THANG vao diem chinh xac cuoi cung cua nhan vat — CHI KHI HOP HE (day
     * la dong "Tac dung chi so" cua Gau Truc; sai he thi congChinhXac tra 0, chi con noi
     * tai PST o phan duoi vi no la "Buff bi dong").
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
    public static final int[] CONG_CHINH_XAC = {0, 200, 0, 0, 0, 0};

    /**
     * Xich Duong: xuyen giap, cong vao Char.attNguHanh[3] — CHI KHI HOP HE (dong "Tac dung
     * chi so" cua Xich Duong; sai he thi pcXuyenGiap tra 0, chi con noi tai +50% tan cong
     * khi mau thap o phan duoi, vi do la "Buff bi dong").
     *
     * XUYEN GIAP O SERVER NAY LA TI LE CHU KHONG PHAI TRU GIAP: xem
     * Char.getBuffEffAttack — no quay r.nextInt(100) < attNguHanh[3], trung thi don do
     * BO QUA HOAN TOAN giap muc tieu (attackDam dat dam = att). Nen 5 = "5% so don bo
     * qua giap", khong phai "giam 5% giap moi don".
     */
    public static final int[] PC_XUYEN_GIAP = {0, 0, 0, 5, 0, 0};

    /**
     * Tien Loc: cong phan tram vao BON diem tiem nang cua chu nhan
     * (suc manh / kheo leo / tinh than / suc khoe) — CHI KHI HOP HE (dong "Tac dung chi
     * so" cua Tien Loc; sai he thi pcTiemNang tra 0, chi con noi tai la chan o phan duoi
     * vi do la "Buff bi dong").
     *
     * KHONG ghi vao chinh bon truong do. Bon truong ay duoc GHI XUONG CSDL nguyen si
     * (DatabaseNhanVat gop chuoi "strength,agitity,spirit,health,..."), nen nang chung
     * len roi tra lai la dat cuoc vao viec khong co luong nao luu nhan vat trong dung
     * khoang do — thua mot lan la so phong len nam vinh vien trong CSDL va con phong tiep
     * moi lan luu. Thay vao do, phan cong duoc tinh TAI CHO o tung cong thuc, qua bon ham
     * Char.tnSucManh/tnKheoLeo/tnTinhThan/tnSucKhoe.
     *
     * Cung vi the KHONG di qua newAtb hay upAbility: newAtb la byte[10] (tran 127, ma 20%
     * tiem nang cua nhan vat cap cao vuot xa), con upAbility la byte[5] va khong he duoc
     * dat lai moi lan tinh — cong vao la don vo han roi tran kieu thanh so am.
     */
    public static final int[] PC_TIEM_NANG = {0, 0, 20, 0, 0, 0};

    /**
     * Chien Lang: cong phan tram vao ca thu vat lan thu phep — CHI KHI HOP HE (dong "Tac
     * dung chi so" cua Chien Lang; sai he thi pcThu tra 0, chi con +15% hap thu o duoi vi
     * do la "Buff bi dong").
     */
    public static final int[] PC_THU = {0, 0, 0, 0, 20, 0};

    /**
     * Chien Lang: phan tram sat thuong bi hap thu truoc khi tru mau — "Buff bi dong",
     * luon bat, khong can hop he.
     *
     * CONG DON voi hap thu san co cua linh thu (Animal.att[9]) va voi noi tai 3% cua Lan
     * — xem Char.checkHapthuSatThuong. Khong thay the, khong lay so lon hon.
     */
    public static final int[] PC_HAP_THU = {0, 0, 0, 0, 15, 0};

    // ------------------------------------------------------------------------------------
    //           HE NGU HANH — HOP HE MOI DUOC "TAC DUNG CHI SO", SAI HE CHI CON BUFF
    // ------------------------------------------------------------------------------------

    /**
     * Nam he, XEP THEO VONG TUONG SINH, nen "he nay sinh ra he nao" chi la (i + 1) % 5:
     *
     *      Moc -> Hoa -> Tho -> Kim -> Thuy -> Moc
     *       0      1      2      3      4
     *
     * Xep theo vong thay vi theo thu tu quen thuoc (Kim Moc Thuy Hoa Tho) la co y: quan he
     * tuong sinh thanh mot phep cong, khong con bang tra 5x5 de go nham mot o.
     */
    public static final String[] TEN_HE = {"Mộc", "Hỏa", "Thổ", "Kim", "Thủy"};

    /** He cua tung skin, theo thu tu skin 0..4. */
    public static final int[] HE = {
        3, // Phuc Thu       - Kim
        0, // Gau Truc       - Moc
        4, // Tien Loc       - Thuy
        1, // Xich Duong     - Hoa
        2, // Chien Lang     - Tho
        3, // Kim Cang Long  - Kim (dung ten, khong tao tac dung chi so gi - xem ID_ITEM)
    };

    /**
     * He cua tung mon phai. Lay dung bang anh xa da co san trong game — xem
     * data/Animal.java (khang ngu hanh cua thu cuoi) va Char.getTkKhangTheoClass:
     *
     *      0 Kiem khach = Kim   1 Chien binh = Hoa   2 Phap su = Thuy
     *      3 Dau si     = Tho   4 Cung thu   = Moc
     */
    public static final int[] HE_CUA_MON_PHAI = {
        3, // 0 Kiem khach - Kim
        1, // 1 Chien binh - Hoa
        4, // 2 Phap su    - Thuy
        2, // 3 Dau si     - Tho
        0, // 4 Cung thu   - Moc
    };

    /** He nay sinh ra he nao. Vong tuong sinh: Moc -> Hoa -> Tho -> Kim -> Thuy -> Moc. */
    public static int sinh(final int he) {
        return (he + 1) % TEN_HE.length;
    }

    /**
     * Skin dang mang co HOP HE voi mon phai cua nguoi choi khong.
     *
     * Hop he = he cua SKIN sinh ra he cua MON PHAI. Do la cach ban mo ta ghi:
     * "Phap su he thuy nen trang bi skin Phuc Thu" — Phuc Thu he Kim, ma Kim sinh Thuy.
     * Ca nam dong trong ban mo ta deu theo dung quan he nay.
     *
     * Chieu nguoc lai KHONG tinh: Thuy sinh Moc, nhung Tien Loc (Thuy) chi hop voi Cung
     * thu (Moc) chu khong hop voi Phap su (Thuy) du Kim sinh ra Thuy do.
     *
     * DAY LA CONG TAC CHINH CUA CA TINH NANG: hopHe() == true thi noiTaiHopHe() moi tra so
     * khac 0 (nhom "Tac dung chi so": chinh xac/tiem nang/xuyen giap/thu cua bon skin
     * Gau Truc, Tien Loc, Xich Duong, Chien Lang) VA thoaDieuKien() moi cho phep noi tai co
     * ti le cua Phuc Thu kich hoat (xem NOI_TAI_CAN_HOP_HE). Sai he thi ca hai deu tra 0/
     * false — nguoi choi chi con nhom "Buff bi dong" luon bat.
     */
    public static boolean hopHe(final Char p) {
        if (!dangMang(p)) {
            return false;
        }
        final int mp = p.charClass;
        if (mp < 0 || mp >= HE_CUA_MON_PHAI.length) {
            return false;
        }
        return sinh(HE[p.idSkinLinhThu]) == HE_CUA_MON_PHAI[mp];
    }

    /** Mon phai nao hop voi skin nay, dung cho mo ta vat pham. */
    public static int monPhaiHopVoiSkin(final int skin) {
        if (!hopLe(skin)) {
            return -1;
        }
        final int heCan = sinh(HE[skin]);
        for (int mp = 0; mp < HE_CUA_MON_PHAI.length; mp++) {
            if (HE_CUA_MON_PHAI[mp] == heCan) {
                return mp;
            }
        }
        return -1;
    }

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

    /**
     * Nhu noiTai, nhung chi tra so khi skin dang mang HOP HE voi mon phai cua chu nhan —
     * dung cho nhom "Tac dung chi so" (xem hopHe() o phan "HE NGU HANH" duoi day). hopHe()
     * tu no da goi dangMang() nen khong can kiem lai o day.
     */
    private static int noiTaiHopHe(final int[] bang, final Char p) {
        return hopHe(p) ? bang[p.idSkinLinhThu] : 0;
    }

    public static int congHp(final Char p) {
        return noiTai(CONG_HP, p);
    }

    public static int congMp(final Char p) {
        return noiTai(CONG_MP, p);
    }

    public static int congChinhXac(final Char p) {
        return noiTaiHopHe(CONG_CHINH_XAC, p);
    }

    public static int pcXuyenGiap(final Char p) {
        return noiTaiHopHe(PC_XUYEN_GIAP, p);
    }

    public static int pcTiemNang(final Char p) {
        return noiTaiHopHe(PC_TIEM_NANG, p);
    }

    public static int pcThu(final Char p) {
        return noiTaiHopHe(PC_THU, p);
    }

    public static int pcHapThu(final Char p) {
        return noiTai(PC_HAP_THU, p);
    }

    // ====================================================================================
    //                    NOI TAI CO TI LE KICH HOAT VA HOI CHIEU
    // ====================================================================================
    //
    // Khac han phan chi so o tren: phan kia luon bat, phan nay chi bat trong vai giay sau
    // khi mot dieu kien xay ra, roi phai doi het hoi chieu.
    //
    // MOI SKIN CO DUNG MOT NOI TAI, nen moi bang duoi day tra theo so hieu skin va trang
    // thai chi can hai moc thoi gian tren Char (het hieu luc / het hoi chieu). Khong can
    // cot CSDL nao: hoi chieu 40 giay ngan hon moi phien choi, mat khi dang xuat la dung.
    //
    // TAT CA deu di qua dangMang() nhu phan chi so — xuong thu la tat, khong con hieu luc.
    //
    // RIENG PHUC THU: day la dong "Tac dung chi so" cua no (ban mo ta dua giam 30% ST o
    // day, con +HP/+MP moi la "Buff bi dong") nen noi tai nay CON PHAI HOP HE moi kich
    // hoat duoc — xem NOI_TAI_CAN_HOP_HE va thoaDieuKien(). Bon skin con lai thi nguoc lai:
    // noi tai co ti le cua chung la "Buff bi dong" (luon bat, khong can hop he), con dong
    // "Tac dung chi so" cua chung nam o nhom chi so tren (noiTaiHopHe).

    /** Dieu kien kich hoat, tra theo so hieu skin. */
    public static final int DK_KHONG = 0;
    /** Bi mon phai danh don VAT LY danh (Kiem khach, Chien binh, Dau si). */
    public static final int DK_BI_VAT_LY = 1;
    /** Bi mon phai danh don PHEP danh (Phap su, Cung thu). */
    public static final int DK_BI_PHEP = 2;
    /** Bi bat cu ai danh. */
    public static final int DK_BI_DANH = 3;
    /** Mau tut xuong duoi nguong. */
    public static final int DK_HP_THAP = 4;

    public static final int[] DIEU_KIEN = {
        DK_BI_VAT_LY, // 0 Phuc Thu
        DK_BI_PHEP,   // 1 Gau Truc
        DK_BI_DANH,   // 2 Tien Loc
        DK_HP_THAP,   // 3 Xich Duong
        DK_KHONG,     // 4 Chien Lang       — noi tai cua no la 15% hap thu, luon bat, khong ti le
        DK_KHONG,     // 5 Kim Cang Long    — khong dung he noi tai nay (xem ID_ITEM)
    };

    /**
     * Noi tai co ti le nao CAN HOP HE moi kich hoat duoc — chi Phuc Thu (skin 0): do la
     * dong "Tac dung chi so" cua no theo ban mo ta. Bon skin con lai de false: noi tai co
     * ti le cua chung la "Buff bi dong", luon bat bat ke dung/sai he — xem thoaDieuKien().
     */
    public static final boolean[] NOI_TAI_CAN_HOP_HE = {true, false, false, false, false, false};

    /** Ti le kich hoat (phan tram). 100 = cu du dieu kien la kich hoat. */
    public static final int[] PC_KICH_HOAT = {15, 15, 10, 100, 0, 0};

    /** Thoi gian hieu luc sau khi kich hoat (mili giay). */
    public static final int[] MS_HIEU_LUC = {3000, 3000, 5000, 3000, 0, 0};

    /**
     * Hoi chieu (mili giay).
     *
     * Xich Duong: ban mo ta KHONG ghi hoi chieu cho dong nay, nhung khong co hoi chieu
     * thi "trong 3 giay" thanh vo nghia — cu o duoi 30% mau la no bat lai ngay, tuc buff
     * chay lien tuc. Dat 40 giay cho dong bo voi ba noi tai kia. Doi so o day la du.
     */
    public static final int[] MS_HOI_CHIEU = {40000, 40000, 40000, 40000, 0, 0};

    /**
     * Hieu ung hinh anh bao cho nguoi choi biet noi tai dang chay.
     *
     * Chon bon id KHONG CO CO CHE NAO KEM THEO — da soat ca ma nguon: LIGHT_YELLOW,
     * LIGHT_MAGENTA, BUFF_DAM_5, BUFF_DAM_10 khong duoc doc o bat cu dau ngoai bang hang
     * so, va stune cua chung deu bang 0 nen khong gay choang.
     *
     * DUNG dung EFF_SHIELD_1/EFF_SHIELD_2 du ten nghe hop: Char.getPcGiamSatThuong va
     * getBackDam doc hai id do va tu dong cho 15%/30% giam sat thuong cong 20%/30% phan
     * don. Muon 30% ma an 45% thi khong ai biet la sai.
     */
    public static final int[] ID_HIEU_UNG = {
        EffectBuff.LIGHT_YELLOW,
        EffectBuff.LIGHT_MAGENTA,
        EffectBuff.BUFF_DAM_10,
        EffectBuff.BUFF_DAM_5,
        -1,
        -1,
    };

    /** Phan tram sat thuong bi CHAN khi noi tai dang chay (Phuc Thu, Tien Loc). */
    public static final int[] PC_CHAN_ST_NOI_TAI = {30, 0, 50, 0, 0, 0};

    /** Diem phan tram cong vao PHAN SAT THUONG khi noi tai dang chay (Gau Truc). */
    public static final int[] PC_CONG_PST_NOI_TAI = {0, 50, 0, 0, 0, 0};

    /** Tran phan sat thuong khi noi tai Gau Truc dang cong vao. */
    public static final int PC_TRAN_PST = 80;

    /** Phan tram TAN CONG cong them khi noi tai dang chay (Xich Duong). */
    public static final int[] PC_CONG_TAN_CONG_NOI_TAI = {0, 0, 0, 50, 0, 0};

    /** Nguong mau (phan tram) de noi tai DK_HP_THAP kich hoat. */
    public static final int PC_NGUONG_HP = 30;

    /** Cau bao khi noi tai kich hoat, tra theo so hieu skin. */
    public static final String[] BAO_KICH_HOAT = {
        "Phúc Thú che chở: giảm 30% sát thương trong 3 giây!",
        "Gấu Trúc phản chấn: phản sát thương tăng mạnh trong 3 giây!",
        "Tiên Lộc dựng lá chắn: chặn 50% sát thương trong 5 giây!",
        "Xích Dương cuồng nộ: tăng 50% tấn công trong 3 giây!",
        "",
        "",
    };

    /** Mon phai danh don VAT LY: Kiem khach (0), Chien binh (1), Dau si (3). */
    public static boolean laClassVatLy(final int charClass) {
        return charClass == 0 || charClass == 1 || charClass == 3;
    }

    /**
     * Mon phai danh don PHEP: Phap su (2), Cung thu (4).
     *
     * Khop dung Char.isCharMagic(). Ban mo ta ghi "class tan cong ma (phap su - cung
     * thu)" — dung y het. Rieng dong Phuc Thu ban mo ta ghi "CB - KK - CT", nhung nguoi
     * choi da chot lai la Chien binh, Kiem khach, DAU SI: tren may chu nay Cung thu la
     * class danh don phep, khong the vua o nhom vat ly vua o nhom phep.
     */
    public static boolean laClassPhep(final int charClass) {
        return charClass == 2 || charClass == 4;
    }

    /** Noi tai cua skin dang mang co dang chay khong. */
    public static boolean noiTaiDangChay(final Char p) {
        return dangMang(p)
                && System.currentTimeMillis() < p.skinHetHieuLuc[p.idSkinLinhThu];
    }

    /** Noi tai con bao lau moi het hoi chieu (mili giay), 0 = san sang. */
    public static long conHoiChieu(final Char p) {
        if (!dangMang(p)) {
            return 0L;
        }
        final long con = p.skinHetHoiChieu[p.idSkinLinhThu] - System.currentTimeMillis();
        return con > 0L ? con : 0L;
    }

    /**
     * Don danh vua roi co thoa DIEU KIEN kich hoat khong (chua xet ti le, chua xet hoi
     * chieu). Goi tu Char.checkHapthuSatThuong — cho duy nhat moi duong sat thuong danh
     * vao nhan vat deu di qua (PvP, quai thuong, va toan bo cac boss).
     *
     * @param nguonDanh ke gay sat thuong, co the la quai
     * @param damSapNhan sat thuong sap tru, de biet mau co tut duoi nguong khong
     */
    public static boolean thoaDieuKien(final Char p, final LiveActor nguonDanh,
                                       final int damSapNhan) {
        if (!dangMang(p)) {
            return false;
        }
        // Phuc Thu: noi tai nay la "Tac dung chi so" nen can hop he moi kich hoat. Kiem
        // truoc DIEU_KIEN — sai he thi khong can xet tiep ke danh la ai.
        if (NOI_TAI_CAN_HOP_HE[p.idSkinLinhThu] && !hopHe(p)) {
            return false;
        }
        final int dk = DIEU_KIEN[p.idSkinLinhThu];
        if (dk == DK_BI_DANH) {
            return true;
        }
        if (dk == DK_HP_THAP) {
            // Tinh theo mau CON LAI SAU don nay: "khi HP giam duoi 30%" — tuc don danh
            // lam tut xuong duoi nguong cung phai kich hoat, khong doi den don sau.
            if (p.maxhp <= 0) {
                return false;
            }
            final int conLai = p.hp - (damSapNhan > 0 ? damSapNhan : 0);
            return conLai * 100 < p.maxhp * PC_NGUONG_HP;
        }
        // Hai dieu kien con lai doi ke danh phai la NGUOI CHOI: quai khong co mon phai.
        if (nguonDanh == null || nguonDanh.cat != 0 || !(nguonDanh instanceof Char)) {
            return false;
        }
        final int mp = ((Char) nguonDanh).charClass;
        return (dk == DK_BI_VAT_LY) ? laClassVatLy(mp) : laClassPhep(mp);
    }

    /**
     * Thu kich hoat noi tai. Tra true neu vua kich hoat NGAY LAN NAY (de nguoi goi gui
     * hieu ung va cau bao — goi nay chi chay mot lan cho moi lan kich hoat).
     *
     * Dang chay thi khong kich hoat lai: neu cho phep, moi don danh trung ti le se gia
     * han them, thanh ra buff chay lien tuc va hoi chieu khong con y nghia gi.
     */
    public static boolean thuKichHoat(final Char p, final LiveActor nguonDanh,
                                      final int damSapNhan) {
        if (!thoaDieuKien(p, nguonDanh, damSapNhan)) {
            return false;
        }
        final int s = p.idSkinLinhThu;
        if (PC_KICH_HOAT[s] <= 0) {
            return false;
        }
        final long now = System.currentTimeMillis();
        if (now < p.skinHetHieuLuc[s] || now < p.skinHetHoiChieu[s]) {
            return false;
        }
        if (PC_KICH_HOAT[s] < 100 && Map.r.nextInt(100) >= PC_KICH_HOAT[s]) {
            return false;
        }
        p.skinHetHieuLuc[s] = now + MS_HIEU_LUC[s];
        p.skinHetHoiChieu[s] = now + MS_HOI_CHIEU[s];
        return true;
    }

    /** Phan tram sat thuong bi chan boi noi tai dang chay. */
    public static int pcChanSatThuong(final Char p) {
        return noiTaiDangChay(p) ? PC_CHAN_ST_NOI_TAI[p.idSkinLinhThu] : 0;
    }

    /** Diem phan tram cong vao phan sat thuong boi noi tai dang chay. */
    public static int pcCongPhanSatThuong(final Char p) {
        return noiTaiDangChay(p) ? PC_CONG_PST_NOI_TAI[p.idSkinLinhThu] : 0;
    }

    /** Phan tram tan cong cong them boi noi tai dang chay. */
    public static int pcCongTanCong(final Char p) {
        return noiTaiDangChay(p) ? PC_CONG_TAN_CONG_NOI_TAI[p.idSkinLinhThu] : 0;
    }

    /**
     * Bao cho nguoi choi biet noi tai vua kich hoat: mot hieu ung hinh keo dai dung bang
     * thoi gian hieu luc, va mot dong chu.
     *
     * Nuot moi loi: day chi la phan bao hieu. Hieu luc da duoc ghi vao hai moc thoi gian
     * roi, khong duoc de mot loi gui goi tin lam hong don danh dang xu ly.
     */
    public static void baoKichHoat(final Char p) {
        if (!dangMang(p)) {
            return;
        }
        final int s = p.idSkinLinhThu;
        try {
            if (ID_HIEU_UNG[s] >= 0
                    && p.addEffBuff((short) ID_HIEU_UNG[s], p.skinHetHieuLuc[s],
                            EffectBuff.BY_ACTOR, 0) != null) {
                p.sendEffToChar(p);
                p.sendEffToNearChar();
            }
            p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(BAO_KICH_HOAT[s]));
        } catch (final Exception e) {
            // Bao hieu hong thi noi tai van chay — khong duoc lam hong don danh.
        }
    }

    /**
     * Chuoi day du dat vao cot data_item.name — vua la TEN vua la MO TA.
     *
     * Game nay khong co cot mo ta rieng cho do mac duoc: khong template nao mang mo ta,
     * va Item.getInfoSend2Char() tra ve chuoi rong. Cho duy nhat hien duoc chu tu do la
     * chinh cot name — va game da lam san nhu vay: "Áo Nghê Thường\n- Tăng 50% băng, sét
     * ..." la MOT gia tri name. Nen skin di theo dung loi do.
     *
     * SINH TU CHINH CAC BANG NOI TAI o tren, khong go tay: doi mot con so trong bang thi
     * mo ta nguoi choi doc duoc tu dong doi theo, khong the lech. test/KiemSkinLinhThu doi
     * chieu chuoi nay voi sql/skin_linh_thu.sql nen ban SQL cung khong the lech not.
     *
     * CHI GHI THU DANG CHAY. Cac noi tai co ti le va hoi chieu trong ban mo ta (giam sat
     * thuong, la chan, phan sat thuong...) chua lam nen KHONG duoc ghi vao day — hua voi
     * nguoi choi mot thu khong ton tai thi te hon la khong hua.
     */
    public static String moTaVatPham(final int skin) {
        if (!hopLe(skin)) {
            return "";
        }
        final StringBuilder s = new StringBuilder("Skin linh thú ").append(TEN[skin]);
        s.append("\n[hệ ").append(TEN_HE[HE[skin]]).append(']');
        final int mp = monPhaiHopVoiSkin(skin);
        // Kim Cang Long (5) khong co tac dung chi so theo he - bo dong "hop X" de khoi
        // hua nham mot thu no khong lam (xem ID_ITEM).
        if (mp >= 0 && mp < CharManager.NAME_CLASS.length && skin != KIM_CANG_LONG) {
            s.append(" - hợp ").append(CharManager.NAME_CLASS[mp]);
        }
        if (skin == KIM_CANG_LONG) {
            // Chi so ngau nhien theo tung vat pham (Item.createAttributeSkinKimCangLong: 2-3
            // trong Bao kich 1-5% / Sat thuong chuan 1-5% / Tang cong 3-15%) hien qua tooltip
            // chi so vat pham (otherAtt[4]/[47]/[54]). Ten template la MOT chuoi dung chung
            // cho moi ban, nen KHONG ghi dai ngau nhien vao day: mon da roll roi ma tooltip
            // van ghi "Ngau nhien 2 hoac 3 trong..." la gay hieu nham (nguoi choi bao
            // 12/09/2026, kem anh chup) - dai ngau nhien la dac ta de server roll, chi con
            // trong chu thich ma nguon va docs/skin-linh-thu.md.
            s.append("\n- Khi trang bị Chổi Sét: 5% tỉ lệ mỗi lần Sét Lan phát động sẽ triệu hồi Sét đánh, tăng thêm 3 giây choáng");
            return s.toString();
        }
        if (CONG_HP[skin] > 0) {
            s.append("\n- Cộng ").append(CONG_HP[skin]).append(" HP");
        }
        if (CONG_MP[skin] > 0) {
            s.append("\n- Cộng ").append(CONG_MP[skin]).append(" MP");
        }
        if (CONG_CHINH_XAC[skin] > 0) {
            s.append("\n- Cộng ").append(CONG_CHINH_XAC[skin]).append(" chính xác (khi hợp hệ)");
        }
        if (PC_TIEM_NANG[skin] > 0) {
            s.append("\n- Cộng ").append(PC_TIEM_NANG[skin]).append("% điểm tiềm năng (khi hợp hệ)");
        }
        if (PC_XUYEN_GIAP[skin] > 0) {
            s.append("\n- ").append(PC_XUYEN_GIAP[skin])
             .append("% đòn đánh xuyên toàn bộ giáp (khi hợp hệ)");
        }
        if (PC_THU[skin] > 0) {
            s.append("\n- Cộng ").append(PC_THU[skin]).append("% phòng thủ (khi hợp hệ)");
        }
        if (PC_HAP_THU[skin] > 0) {
            s.append("\n- Hấp thụ ").append(PC_HAP_THU[skin]).append("% sát thương");
        }
        final String noiTai = moTaNoiTai(skin);
        if (noiTai.length() > 0) {
            s.append("\n").append(noiTai);
        }
        return s.toString();
    }

    /** Dong mo ta noi tai co ti le, rong neu skin khong co noi tai loai nay. */
    private static String moTaNoiTai(final int skin) {
        if (PC_KICH_HOAT[skin] <= 0) {
            return "";
        }
        final StringBuilder s = new StringBuilder("- Nội tại");
        if (NOI_TAI_CAN_HOP_HE[skin]) {
            s.append(" (khi hợp hệ)");
        }
        s.append(": ");
        switch (DIEU_KIEN[skin]) {
            case DK_BI_VAT_LY:
                s.append("khi bị Kiếm khách, Chiến binh hoặc Đấu sĩ đánh, ");
                break;
            case DK_BI_PHEP:
                s.append("khi bị Pháp sư hoặc Cung thủ đánh, ");
                break;
            case DK_BI_DANH:
                s.append("khi nhận sát thương, ");
                break;
            case DK_HP_THAP:
                s.append("khi máu xuống dưới ").append(PC_NGUONG_HP).append("%, ");
                break;
            default:
                return "";
        }
        if (PC_CHAN_ST_NOI_TAI[skin] > 0) {
            s.append("chặn ").append(PC_CHAN_ST_NOI_TAI[skin]).append("% sát thương");
        } else if (PC_CONG_PST_NOI_TAI[skin] > 0) {
            s.append("phản sát thương +").append(PC_CONG_PST_NOI_TAI[skin])
             .append("% (tối đa ").append(PC_TRAN_PST).append("%)");
        } else if (PC_CONG_TAN_CONG_NOI_TAI[skin] > 0) {
            s.append("tăng ").append(PC_CONG_TAN_CONG_NOI_TAI[skin]).append("% tấn công");
        }
        s.append(" trong ").append(MS_HIEU_LUC[skin] / 1000).append(" giây");
        if (PC_KICH_HOAT[skin] < 100) {
            s.append(". Tỉ lệ ").append(PC_KICH_HOAT[skin]).append('%');
        }
        s.append(", hồi chiêu ").append(MS_HOI_CHIEU[skin] / 1000).append(" giây");
        return s.toString();
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
     * Co ve hinh skin cho nguoi choi thay khong — dangMang() CONG voi cong tac "bat/tat
     * hien thi skin" o NPC Tho Ren Than Bi (Char.isTatSkinLinhThu). CHI dung cho ba cho
     * ghi hinh anh (idAnhBanDo, bay, va ba cap goi trong MessageCreator zero-hoa userHorse/
     * fly/idThutool) — KHONG dung cho chi so hay noi tai. Cong tac nay chi an con thu, moi
     * chi so/noi tai (noiTai, noiTaiHopHe, thoaDieuKien...) van di qua dangMang() nhu cu va
     * KHONG doc ham nay, dung y "an hien thi" cua nguoi choi: tat hien thi khong duoc mat
     * chi so da mac.
     */
    public static boolean hienThi(final Char p) {
        return dangMang(p) && !p.isTatSkinLinhThu();
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

    /** Id anh con thu tren ban do, -1 = khong ve skin (khong mang, hoac da tat hien thi). */
    public static int idAnhBanDo(final Char p) {
        return hienThi(p) ? ID_ANH_BAN_DO + p.idSkinLinhThu : -1;
    }

    /** Byte "fly" cua skin dang mang, -1 = khong ve skin (khong mang, hoac da tat hien thi). */
    public static int bay(final Char p) {
        return hienThi(p) ? BAY[p.idSkinLinhThu] : -1;
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

    // ====================================================================================
    //           KIM CANG LONG — CHI SO NGAU NHIEN THEO VAT PHAM (khong dung bang hang so)
    // ====================================================================================

    /**
     * Vat pham Kim Cang Long dang mac VA dang mang (dangMang), null neu khong. Dung de doc
     * lai chi so ngau nhien da roll luc tao vat pham (Item.createAttributeSkinKimCangLong,
     * luu trong otherAtt[4]/[47]/[54]) — khac voi 5 skin con lai, khong doc tu bang hang so
     * vi moi vat pham Kim Cang Long mot khac nhau.
     */
    public static Item timSkinDangMac(final Char p) {
        if (p == null || !dangMang(p) || p.idSkinLinhThu != KIM_CANG_LONG || p.awItems == null) {
            return null;
        }
        for (int i = 0; i < p.awItems.size(); i++) {
            final Item it = p.awItems.get(i);
            if (it != null && it.getTemplate() != null && it.getTemplate().id == ID_ITEM[KIM_CANG_LONG]) {
                return it;
            }
        }
        return null;
    }

    /** Bao kich (%) tu Kim Cang Long dang mang, 0 neu khong co - otherAtt[4] "Bạo kích". */
    public static int kimCangLongBaoKich(final Char p) {
        final Item it = timSkinDangMac(p);
        return it != null ? it.otherAtt[4] : 0;
    }

    /** Tang cong (%) tu Kim Cang Long dang mang, 0 neu khong co - otherAtt[47] "Gia tăng tc". */
    public static int kimCangLongTangCong(final Char p) {
        final Item it = timSkinDangMac(p);
        return it != null ? it.otherAtt[47] : 0;
    }

    /**
     * He so sat thuong chuan tu Kim Cang Long dang mang, 0 neu khong co - otherAtt[54]
     * "Sát thương chuẩn". Ap dung cong THANG moi don theo % maxHP MUC TIEU, cung tinh than
     * voi otherAtt[54] cua vu khi thoi trang (CharChiSo.attackDam) nhung doc tu vat pham
     * skin, khong phai self.itemVukhiThoiTrang.
     */
    public static int kimCangLongSatThuongChuan(final Char p) {
        final Item it = timSkinDangMac(p);
        return it != null ? it.otherAtt[54] : 0;
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
        // PHAI tinh lai chi so TRUOC khi dung goi tin.
        //
        // attack / maxhp / defend_physic / accurate deu la TRUONG DUOC LUU SAN, chi tinh
        // lai khi calculateAttrib() chay. Doi skin ma khong goi no thi bon truong do van
        // la so cua truoc luc mac — trong khi bang diem tiem nang lai duoc tinh tuoi ngay
        // luc dung goi tin (MessageCreator cong p.tnSucManh() vao tai cho). Ket qua:
        // nguoi choi THAY tiem nang tang ma tan cong dung im. Dung loi da bi bao.
        //
        // Dat truoc cac lenh gui, khong thi goi tin dau tien van mang so cu.
        p.calculateAttrib();
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
