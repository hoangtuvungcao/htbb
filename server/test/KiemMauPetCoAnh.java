import java.io.File;

import data.Pet;
import data.PetTemplate;
import real.Item;

/**
 * Moi mau pet trong real.Map.petTemplate phai tro toi mot tam anh CO THAT (map/pet/<idImg>.png)
 * va khai bao so khung/kich co that, khong duoc de gia tri giu cho.
 *
 * LOI DA XAY RA THAT (05/09/2026): sau mau 23 Cú, 24 Rồng, 25 Thỏ bay, 26 Dơi, 27 Đại bàng,
 * 29 Bạch Thử ghi idImg=21, nFrame=1, w=1, h=1, TYPE_WALK - toan gia tri giu cho, map/pet/21.png
 * chua bao gio ton tai. Server tra anh trong suot 32x96 (ban va 02/09) nen client khong con NPE,
 * nhung TYPE_WALK dung bang khung cua pet 12 khung: client ve khung 1..11 tren anh khai bao 1
 * khung -> vung ve ngoai bien anh -> client J2ME nem IllegalArgumentException ngay trong vong ve
 * actor (khong try/catch tung actor) -> moi actor ve sau pet bien mat trong khung hinh do, khung
 * sau (idFrame ve 0) hien lai. Nguoi choi thay "nhan vat luc an luc hien ~1 lan/giay, o moi ban
 * do, ke ca nhan vat cua chinh minh". 338 pet cua 33 nguoi choi dinh, 233 con phat trong dung
 * ngay do. Toan bo nguoi choi tren server dung client J2ME (log [CLIENT]: Nokia 6233 /
 * MicroEmulator, 2.0.9).
 *
 * SUA: sau mau muon y nguyen bo (idImg, nFrame, wFrame, hFrame, type_move) cua mot mau co san da
 * chay dung; Pet.apDungAnhTheoMau() ghi de idImg/type theo mau khi nap tu DB (338 dong cu con luu
 * idImg=21, type=WALK) va khi tao moi. getDBInfo()/UPDATE tob_pet ghi ca hai nen DB tu lanh.
 *
 * Duong thu hai (pet CO TOOL): client dung them AnimalMove voi imgID = getIdEffPet() va cat anh
 * trong suot theo bang khung type 5 (chi so toi 11) -> Pet.getTotalFrame() gui 12 cho pet-tool.
 */
public final class KiemMauPetCoAnh {

    /** {mau muon, mau nguon} - phai khop voi chu thich trong Map.loadPetTemplate. */
    private static final int[][] CAP_MUON = {
        {23, 13},  // Cú        <- Chim cánh cụt
        {24, 8},   // Rồng      <- Pet Giáp Ngọ
        {25, 14},  // Thỏ bay   <- Pet ất mùi
        {26, 3},   // Dơi       <- Dơi quỷ
        {27, 1},   // Đại bàng  <- Thanh tước
        {29, 9},   // Bạch Thử  <- Pet Armadillo băng
        {30, 9},   // Capybara  <- Pet Armadillo băng (them 19/09/2026, chua co anh rieng)
    };

    /**
     * Chi so khung LON NHAT ma bang arrFrameStand/arrFrameMove cua client (AnimalMove.cs) co the
     * yeu cau theo type_move, tinh CA BON HUONG. Chep tu ClientUnity/Assets/Scripts/Game/AnimalMove.cs
     * (ban port trung khop client J2ME). Chi so khung >= nFrame la ve ngoai bien anh.
     */
    private static final int[] KHUNG_TOI_DA_THEO_TYPE = {
        8,   // 0 TYPE_FLY_1_FRAME
        11,  // 1 TYPE_WALK
        1,   // 2 TYPE_LANTERNS
        5,   // 3 TYPE_FLY_2_FRAME
        11,  // 4 TYPE_FLY_IMAGE
        11,  // 5 TYPE_PET_TOOL
    };

    private KiemMauPetCoAnh() {
    }

    public static void chay() {
        Kiem.nhom("Mau pet phai co anh that (map/pet/<idImg>.png) va so khung hop le");

        real.Map.loadPetTemplate();
        final PetTemplate[] ds = real.Map.petTemplate;
        // 31 mau tu 19/09/2026 (mau 30 Capybara muon anh mau 9 - xem Map.loadPetTemplate).
        Kiem.dung("nap duoc Map.petTemplate (31 mau)", ds != null && ds.length == 31);
        if (ds == null || ds.length == 0) {
            return;
        }

        // 1. Moi mau -> file anh ton tai. Day la phep kiem BAO QUAT: bat ca mau moi them sau nay.
        int thieu = 0;
        String viDu = "";
        for (final PetTemplate t : ds) {
            if (t == null) {
                continue;
            }
            final File f = new File("map/pet/" + t.idImg + ".png");
            if (!f.isFile()) {
                thieu++;
                if (viDu.isEmpty()) {
                    viDu = "mau " + t.idTemplate + " '" + t.name + "' -> map/pet/" + t.idImg + ".png";
                }
            }
        }
        Kiem.bang("moi mau pet deu tro toi file anh co that" + (viDu.isEmpty() ? "" : " (thieu: " + viDu + ")"),
                Integer.valueOf(0), Integer.valueOf(thieu));

        // 2. Khong con bo gia tri giu cho.
        int giuCho = 0;
        for (final PetTemplate t : ds) {
            if (t != null && t.nFrame <= 1 && t.wFrame <= 1 && t.hFrame <= 1) {
                giuCho++;
            }
        }
        Kiem.bang("khong mau nao con nFrame=1, w=1, h=1 (gia tri giu cho)", Integer.valueOf(0), Integer.valueOf(giuCho));
        int mot = 0;
        for (final PetTemplate t : ds) {
            if (t != null && t.nFrame < 2) {
                mot++;
            }
        }
        Kiem.bang("moi mau deu >= 2 khung (bang khung client luon dung >= 2 chi so)", Integer.valueOf(0), Integer.valueOf(mot));

        // 3. Sau mau muon anh phai GIONG HET mau nguon o 5 truong lien quan ve, va mau nguon phai
        //    du khung cho bang khung client o MOI huong (do la ly do khong muon phuong hoang 4 khung).
        for (final int[] c : CAP_MUON) {
            final PetTemplate a = ds[c[0]];
            final PetTemplate b = ds[c[1]];
            Kiem.dung("mau " + c[0] + " '" + a.name + "' dung dung bo anh/khung/kieu di chuyen cua mau " + c[1] + " '" + b.name + "'",
                    a.idImg == b.idImg && a.nFrame == b.nFrame && a.wFrame == b.wFrame
                    && a.hFrame == b.hFrame && a.type_move == b.type_move);
            Kiem.dung("mau " + c[0] + " giu icon rieng (idImgPaint khac mau nguon)", a.idImgPaint != b.idImgPaint);
            final int type = a.type_move;
            final boolean typeBiet = type >= 0 && type < KHUNG_TOI_DA_THEO_TYPE.length;
            Kiem.dung("mau " + c[0] + ": type_move " + type + " co trong bang khung client", typeBiet);
            if (typeBiet) {
                Kiem.dung("mau " + c[0] + ": nFrame " + a.nFrame + " > chi so khung toi da " + KHUNG_TOI_DA_THEO_TYPE[type]
                        + " cua type " + type + " (moi huong deu trong bien)",
                        a.nFrame > KHUNG_TOI_DA_THEO_TYPE[type]);
            }
            Kiem.sai("mau " + c[0] + " KHONG muon phuong hoang 16/17 (4 khung, FLY_2_FRAME can 6)",
                    a.idImg == 16 || a.idImg == 17);
        }

        // 4. Pet nap tu DB voi du lieu CU (idImg=21, type=WALK) -> ghi de theo mau.
        try {
            // Dung chuoi info that cua pet #758 "Rồng cấp 5" (huydiet) trong tob_pet, 13 truong.
            final String infoCu = "24,1788591044873,0,1,2082500000,300,0,3,2,4,-1,0,3";
            final Pet rong = new Pet("Rồng cấp 5", (byte) 21, 44, infoCu, "", "");
            Kiem.bang("pet DB idImg=21 mau 24 Rồng -> idImg theo mau",
                    Byte.valueOf(ds[24].idImg), Byte.valueOf(rong.idImg));
            Kiem.bang("pet DB type=1 (WALK) mau 24 -> type theo mau",
                    Byte.valueOf(ds[24].type_move), Byte.valueOf(rong.type));
            Kiem.dung("getDBInfo() ghi type MOI (truong thu 4) -> DB tu lanh o lan luu sau",
                    rong.getDBInfo().startsWith("24,1788591044873,0," + ds[24].type_move + ","));
            Kiem.bang("pet KHONG tool: getTotalFrame = nFrame cua mau",
                    Byte.valueOf(ds[24].nFrame), Byte.valueOf(rong.getTotalFrame()));

            final Pet daiBang = new Pet("Đại Bàng cấp 5", (byte) 21, 44, "27,1788593616237,0,1,2082500000,300,0,3,2,4,-1,0,3", "", "");
            Kiem.bang("pet DB mau 27 Đại bàng -> idImg theo mau", Byte.valueOf(ds[27].idImg), Byte.valueOf(daiBang.idImg));

            // Mau NGOAI nhom muon anh giu nguyen idImg cua DB (khong dong den pet khac).
            final Pet canhCut = new Pet("Chim", (byte) 13, 44, "13,0,0,1,0,0,0,0,0,0,0,0,0", "", "");
            Kiem.bang("mau ngoai nhom (13 Chim cánh cụt) giu nguyen idImg DB", Byte.valueOf((byte) 13), Byte.valueOf(canhCut.idImg));
            Kiem.bang("mau ngoai nhom giu nguyen type DB", Byte.valueOf((byte) 1), Byte.valueOf(canhCut.type));

            // Pet TAO MOI tu mau (duong Char.createPet / ItemLuckyDraw) cung phai co type dung,
            // khong de mac dinh 0 (TYPE_FLY_1_FRAME dung bang khung 9 chi so).
            final Pet moi = new Pet(ds[24].idTemplate, ds[24].idImg, (short) 1, 0, 44, "Rồng");
            Kiem.bang("pet moi mau 24 -> type theo mau", Byte.valueOf(ds[24].type_move), Byte.valueOf(moi.type));
            Kiem.bang("pet moi mau 24 -> idImg theo mau", Byte.valueOf(ds[24].idImg), Byte.valueOf(moi.idImg));

            // 5. Pet CO TOOL: gui 12 khung (bang khung type 5 co chi so toi 11) - xem
            //    Pet.SO_KHUNG_GUI_CHO_PET_TOOL. Gan tool bang tay de khong phai nap Item template.
            final Pet coTool = new Pet("Rồng cấp 5", (byte) 21, 44, infoCu, "", "");
            coTool.itemPet = new Item();
            coTool.itemPet.level = 2;
            Kiem.bang("pet co tool -> getType() = TYPE_PET_TOOL", Short.valueOf(Pet.TYPE_PET_TOOL), Short.valueOf(coTool.getType()));
            Kiem.bang("pet co tool -> getTotalFrame() = 12 (= chi so khung toi da type 5 + 1)",
                    Byte.valueOf((byte) 12), Byte.valueOf(coTool.getTotalFrame()));
            Kiem.dung("12 > chi so khung toi da cua type 5", 12 > KHUNG_TOI_DA_THEO_TYPE[5]);
            Kiem.bang("Rồng tool level 2 -> ma hieu ung 35 (client xin 8735 va 5235)",
                    Integer.valueOf(35), Integer.valueOf(coTool.getIdEffPet()));
            coTool.itemPet.level = 5;
            Kiem.bang("Rồng tool level 5 -> KEP ve ma cap 2 (35), khong roi xuong atb[1]=25 (client khong co bang khung)",
                    Integer.valueOf(35), Integer.valueOf(coTool.getIdEffPet()));
        } catch (final Throwable e) {
            Kiem.dung("tao Pet thu nghiem khong nem loi (" + e + ")", false);
        }
    }
}
