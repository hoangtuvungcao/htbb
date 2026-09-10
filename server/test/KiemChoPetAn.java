import data.Pet;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Char;
import real.Item;
import real.ItemTemplates;
import real.LevelPet;

/**
 * Cho pet an (Pet.addXP).
 *
 * CAI BAY DA SAP MOT LAN, DUNG CHAN CHO AN LAI: ban 09/09/2026 them mot nhanh tu choi cho an
 * khi pet kich tran ca cap do (lv = LevelPet.expMain.length = 50) lan tien hoa
 * (itemPet.level = 2), y tot la de nguoi choi khong mat vat pham vo ich. Nhung cho an la
 * duong DUY NHAT nap powerEat (suc an) - Pet.addXP la cho duy nhat cong vao no - trong khi
 * Pet.doEat tru 5 diem moi 30 phut, va Pet.CanUseNoiTai() doi
 * powerEat * 100 / MAX_POWER_EAT (= 300) phai >= 20, tuc powerEat >= 60. Chan cho an nghia la
 * sau khoang mot ngay moi con pet manh nhat deu MAT HAN noi tai (Chua Tri / Phat Trien /
 * Doat Menh...) va khong con duong hoi lai.
 *
 * Do CSDL that ngay 10/09/2026 luc phat hien: 53 pet dang mang dinh chan, 13 con da tut duoi
 * nguong noi tai. Nguoi choi bao dung mot ngay sau khi ban chan len may chu.
 *
 * Cho an that su vo ich (suc an da day) van bi Pet.doEat tu choi truoc, kem thong bao
 * "Vat pham cho an vuot qua suc an cua pet" - do moi la cho chan dung.
 *
 * TIENG ON BINH THUONG khi chay: moi lan addXP se in mot ArrayIndexOutOfBounds tu
 * MessageCreator.createCharWearingMessage kem dong "ECWR null". Do la vi Char(null) trong test
 * khong co bo trang bi that; MessageCreator tu bat loi do va tra null, Char.sendMessage(null)
 * voi session null lai khong lam gi - khong anh huong ket qua kiem tra.
 */
public final class KiemChoPetAn {

    /** Pet.MAX_POWER_EAT * 20 / 100 - nguong CanUseNoiTai(). */
    private static final int NGUONG_NOI_TAI = 60;

    private KiemChoPetAn() {
    }

    public static void chay() {
        Kiem.nhom("Cho pet an - pet kich tran van phai nap duoc suc an");

        kiemPetKichTranVanAnDuoc();
        kiemAnDuNguongThiNoiTaiChayLai();
        kiemChuaNangCapVanBiChan();
        kiemPetCoLevelNgoaiBacTienHoa();
        kiemNhanCapDoTrongBangThongTin();
        quetVanBanNguon();
    }

    /**
     * itemPet.level KHONG phai luc nao cung la bac tien hoa 0/1/2: Char.doCreatePetMax dat thang
     * 5, va Item.initInfoFromDB thay level == 0 thi lay getTemplate().level - data_item 727 Bach
     * Thu / 790 Bong vang / 802 Kim ho bao bao / 873 Capybara deu ghi level = 5. Truoc day
     * limitLevel[5] tren mang 3 o nem ArrayIndexOutOfBounds, bi catch rong cua MapDoMenu nuot,
     * nguoi choi thay cho an "khong co gi xay ra".
     */
    private static void kiemPetCoLevelNgoaiBacTienHoa() {
        final Pet pet = new Pet(0);
        pet.itemPet = new VatPhamPet();
        pet.itemPet.level = 5;
        pet.initDbInfo("29,0,0,0,1000,0,0,3,2," + Pet.ID_SKILL_CHUA_TRI + ",-1,0,3");

        boolean nem = false;
        boolean nhan = false;
        try {
            nhan = pet.addXP(new long[]{1000L, 20L}, new Char(null));
        } catch (final RuntimeException e) {
            nem = true;
        }

        Kiem.sai("pet co itemPet.level = 5 KHONG duoc nem ngoai le khi cho an", nem);
        Kiem.dung("... va van nhan duoc mieng an", nhan);
        Kiem.bang("... va van nap duoc suc an", Integer.valueOf(20), Integer.valueOf(pet.powerEat));
    }

    /**
     * Bang thong tin pet phai goi so cap do la "Cap do". De nhan "Tien hoa" (nhu ban cu) thi
     * khong con dong nao ten "Cap do", ma ten pet lai la "Rong cap 1..5" (bac template) - nguoi
     * choi doc so trong ten roi tuong pet chua len cap. Da bao nham that ngay 10/09/2026.
     */
    private static void kiemNhanCapDoTrongBangThongTin() {
        final Pet pet = petKichTran();
        final String bang = pet.getInfoSendUser();

        Kiem.dung("bang thong tin co dong \"Cấp độ\"", bang.contains("- Cấp độ: "));
        Kiem.dung("... va in dung so cap do", bang.contains("- Cấp độ: " + LevelPet.expMain.length + "+"));
        Kiem.dung("bac tien hoa van nam o dong \"Trạng thái\"", bang.contains("- Trạng thái: 3/3"));
        Kiem.sai("KHONG con dan nhan \"Tiến hoá\" len con so cap do", bang.contains("- Tiến hoá: "));
    }

    /**
     * Pet lv50 + tien hoa 2: exp bo di la dung, nhung mieng an phai duoc nhan de nap suc an.
     */
    private static void kiemPetKichTranVanAnDuoc() {
        final Pet pet = petKichTran();
        pet.powerEat = 0;
        final int lvTruoc = pet.lvDetail.lv;

        final boolean nhan = pet.addXP(new long[]{1000000L, 20L}, new Char(null));

        Kiem.dung("pet kich tran VAN nhan mieng an (khong duoc tra false)", nhan);
        Kiem.bang("suc an tang dung phan cua mieng an", Integer.valueOf(20),
                Integer.valueOf(pet.powerEat));
        Kiem.bang("cap do khong doi (van kich tran)", Integer.valueOf(lvTruoc),
                Integer.valueOf(pet.lvDetail.lv));
        Kiem.bang("cap do dung bang so bac thang exp cua pet", Integer.valueOf(LevelPet.expMain.length),
                Integer.valueOf(pet.lvDetail.lv));
    }

    /** Cho an du nhieu thi vuot lai nguong noi tai - day chinh la thu ban chan da lam mat. */
    private static void kiemAnDuNguongThiNoiTaiChayLai() {
        final Pet pet = petKichTran();
        pet.powerEat = 0;

        Kiem.sai("suc an 0: noi tai dang TAT", pet.CanUseNoiTai());
        for (int lan = 0; lan * 20 < NGUONG_NOI_TAI; lan++) {
            pet.addXP(new long[]{1000000L, 20L}, new Char(null));
        }
        Kiem.dung("cho an du " + NGUONG_NOI_TAI + " suc an: noi tai chay lai",
                pet.CanUseNoiTai());
        Kiem.dung("... va ky nang Chua Tri co tac dung tro lai", pet.getChuaTri()[0] > 0);
    }

    /** Nhanh chan DUY NHAT con lai: dang cho nang cap thi chua duoc an tiep. */
    private static void kiemChuaNangCapVanBiChan() {
        final Pet pet = petKichTran();
        // Lui ve dung moc "cho nang cap": lv 49, exp = expMain[49] - 1.
        pet.lvDetail.setExp(LevelPet.getXpFromLevel(LevelPet.expMain.length) - 1L);
        pet.powerEat = 0;
        Kiem.dung("dung moc cho nang cap (canUplevel)", pet.lvDetail.canUplevel());

        final boolean nhan = pet.addXP(new long[]{1000000L, 20L}, new Char(null));

        Kiem.sai("chua nang cap thi van tu choi cho an", nhan);
        Kiem.bang("... va khong tru/cong suc an", Integer.valueOf(0),
                Integer.valueOf(pet.powerEat));
    }

    /** Chan tai pham: khong duoc dat lai nhanh tu choi cho an vi ly do kich tran. */
    private static void quetVanBanNguon() {
        final String ma = doc("src/data/Pet.java");
        Kiem.dung("doc duoc src/data/Pet.java", ma.length() > 0);
        Kiem.sai("KHONG con nhanh tu choi cho an vi pet da kich tran",
                ma.contains("không thể cho ăn thêm"));
        // Yeu cau 10/09/2026: cho an pet da kich tran thi im lang, khong bao gi them.
        Kiem.sai("KHONG bao gi khi cho an pet da kich tran",
                ma.contains("chỉ còn hồi sức ăn"));
        Kiem.dung("van con nhanh chan khi chua nang cap",
                ma.contains("Không thể cho ăn khi chưa nâng cấp pet"));
    }

    /**
     * Pet lv50, tien hoa 2, loai atb0 = 5 (Rong/Cu/Doi/Yeu Tinh/Dai Bang), co ky nang Chua Tri.
     * Nap qua initDbInfo cho giong duong nap that tu tob_pet.info:
     * idTemplate,timeBuy,totalMinute,type,exp,powerEat,thoihan,phamchat,maxSkill,skill1,skill2,
     * canTrade,maxPhamChat
     */
    private static Pet petKichTran() {
        final Pet pet = new Pet(0);
        pet.itemPet = new VatPhamPet();
        pet.itemPet.level = 2;
        pet.initDbInfo("24,0,0,0," + LevelPet.getXpFromLevel(LevelPet.expMain.length)
                + ",0,0,3,2," + Pet.ID_SKILL_CHUA_TRI + ",-1,0,3");
        return pet;
    }

    private static String doc(final String duongDan) {
        try {
            return new String(Files.readAllBytes(Paths.get(duongDan)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    private static final class VatPhamPet extends Item {
        private final ItemTemplates template = new ItemTemplates();

        VatPhamPet() {
            this.template.atb = new short[33];
            this.template.atb[0] = 5;
            this.template.name = "Rong cap 5";
        }

        @Override
        public ItemTemplates getTemplate() {
            return this.template;
        }
    }

    public static void main(final String[] args) {
        chay();
        System.exit(Kiem.tongKet());
    }
}
