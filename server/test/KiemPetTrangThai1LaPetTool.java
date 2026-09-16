import data.Pet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Item;
import real.ItemTemplates;

/**
 * Pet trung (Cu/Rong/Yeu tinh/Doi/Dai bang) o TRANG THAI 1 ve thanh MOT con pet-tool - sua 16/09/2026.
 *
 * Loi: nguoi mang "Yeu Tinh cap 1" thay 2 con: yeu tinh vang (buff hieu ung 26 dan vao nhan vat)
 * + con de trang (actor pet, vi mau 25 muon bo anh mau 14 tu 05/09). Goc: data_item 699 co level=0
 * ("cap 1" trong ten la atb0), Pet.getType() cu chi coi level > 0 la pet-tool.
 *
 * Sua: Pet.getType() = TYPE_PET_TOOL khi co itemPet (bat ke level). Server gui type=5 + imgID =
 * getIdEffPet() = atb1 = 24..28, client ve bang lop Pet voi bang khung rieng map/d_effect/24..28.pet
 * (tools/TaoPetTrangThai1.java, cau truc nhu bang pet 49, bay cao 30). Moi cho cong buff pet deu co
 * dieu kien !isPetTool() nen tu dong tat. Kem sua createCharWearingCharInfo ghi imgID theo do rong
 * dung phien ban client (truoc luon 1 byte, client >= 2.2.5 doc 2 byte -> lech goi).
 *
 * Test khoa: (1) phan loai theo level/itemPet; (2) canLearnSkill van doi level > 0; (3) 24-28 nam
 * trong id_eff_pet_tool; (4) 5 file .pet dung cau truc va nam trong anh; (5) imgID theo phien ban.
 */
public final class KiemPetTrangThai1LaPetTool {

    private static final int DO_CAO_BAY = 30;

    private KiemPetTrangThai1LaPetTool() {
    }

    public static void chay() {
        Kiem.nhom("Pet trung trang thai 1 = pet-tool: 1 con, bang khung 24-28, imgID dung do rong");

        kiemPhanLoai();
        kiemHocKyNang();
        kiemDanhSachGoi26();
        kiemBangKhungPet();
        kiemDoRongImgId();
    }

    /** Pet mau 25 (Yeu tinh), info 13 truong: idTemplate,timeBuy,totalMinute,type,exp,powerEat,thoihan,phamchat,maxSkill,skill1,skill2,canTrade,maxPhamChat. */
    private static Pet petYeuTinh(final int levelVatPham, final int phamchat, final int maxSkill) {
        final Pet pet = new Pet(0);
        pet.initDbInfo("25,0,0,1,0,0,0," + phamchat + "," + maxSkill + ",-1,-1,0,3");
        pet.itemPet = new VatPhamPet(1, 26, 0);
        pet.itemPet.level = (short) levelVatPham;
        return pet;
    }

    private static void kiemPhanLoai() {
        final Pet trangThai1 = petYeuTinh(0, -1, 0);
        Kiem.bang("Yeu tinh level 0: getType() = TYPE_PET_TOOL", Short.valueOf(Pet.TYPE_PET_TOOL), Short.valueOf(trangThai1.getType()));
        Kiem.dung("... isPetTool() (moi cho cong buff 24-28 deu bi chan boi !isPetTool())", trangThai1.isPetTool());
        Kiem.bang("... imgID gui client = 26 (hinh yeu tinh, nay la anh pet)", Integer.valueOf(26), Integer.valueOf(trangThai1.getIdEffPet()));
        Kiem.bang("... getTotalFrame() = 12 (khung an toan cho AnimalMove du phong tren J2ME)", Byte.valueOf((byte) 12), Byte.valueOf(trangThai1.getTotalFrame()));

        final Pet trangThai2 = petYeuTinh(1, -1, 0);
        Kiem.dung("Yeu tinh level 1: van pet-tool", trangThai2.isPetTool());
        Kiem.bang("... anh 39 nhu cu", Integer.valueOf(39), Integer.valueOf(trangThai2.getIdEffPet()));
        Kiem.bang("Yeu tinh level 2: anh 40 nhu cu", Integer.valueOf(40), Integer.valueOf(petYeuTinh(2, -1, 0).getIdEffPet()));

        // Pet mau 25 KHONG co vat pham (createPet(id, time) tu giftcode/su kien): van la pet thuong.
        final Pet petThuong = new Pet(0);
        petThuong.initDbInfo("25,0,0,1,0,0,0,-1,0,-1,-1,0,0");
        Kiem.bang("mau 25 khong itemPet: getType() = type_move cua mau", Short.valueOf(Pet.TYPE_WALK), Short.valueOf(petThuong.getType()));
        Kiem.sai("... khong phai pet-tool", petThuong.isPetTool());
        Kiem.bang("... khong co ma anh hieu ung", Integer.valueOf(-1), Integer.valueOf(petThuong.getIdEffPet()));

        final Pet longDen = new Pet(0);
        longDen.initDbInfo("0,0,0,0,0,0,0,-1,0,-1,-1,0,0");
        Kiem.sai("pet long den: khong phai pet-tool", longDen.isPetTool());

        final Pet bachThu = new Pet(0);
        bachThu.initDbInfo("29,0,0,1,0,0,0,-1,0,-1,-1,0,0");
        bachThu.itemPet = new VatPhamPet(5, 60, 5);
        bachThu.itemPet.level = 5;
        Kiem.dung("Bach Thu level 5: pet-tool", bachThu.isPetTool());
        Kiem.bang("... anh 60", Integer.valueOf(60), Integer.valueOf(bachThu.getIdEffPet()));

        // Khoa van ban: khong ai quay lai dieu kien level > 0 trong getType().
        final String ma = doc("src/data/Pet.java");
        final String getType = doan(ma, "public short getType()", "}");
        Kiem.dung("Pet.getType(): pet-tool khi co itemPet, khong con doi level > 0",
                getType.contains("this.itemPet != null ? TYPE_PET_TOOL") && !getType.contains("level > 0"));
    }

    /** Luat cu: chi tu trang thai 2 moi hoc ky nang - giu nguyen du isPetTool() nay dung cho ca trang thai 1. */
    private static void kiemHocKyNang() {
        final Pet trangThai1 = petYeuTinh(0, 1, 1);
        Kiem.sai("level 0 co pham chat: KHONG hoc ky nang (nhu truoc 16/09)", trangThai1.canLearnSkill());
        final Pet trangThai2 = petYeuTinh(1, 1, 1);
        Kiem.dung("level 1 co pham chat, chua co ky nang: hoc duoc", trangThai2.canLearnSkill());
        final Pet chuaPhamChat = petYeuTinh(1, -1, 0);
        Kiem.sai("level 1 chua doi pham chat: chua hoc duoc (nhu cu)", chuaPhamChat.canLearnSkill());
    }

    private static void kiemDanhSachGoi26() {
        final String mc = doc("src/real/MessageCreator.java");
        final String dongMang = doan(mc, "id_eff_pet_tool = new int[]{", "};");
        Kiem.dung("doc duoc dong id_eff_pet_tool", !dongMang.isEmpty());
        for (int id = 24; id <= 28; id++) {
            Kiem.dung("id_eff_pet_tool co " + id + " (khong co thi client khong co bang khung -> pet trang thai 1 vo hinh)", chuaSo(dongMang, id));
        }
        for (final int id : new int[]{35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 60, 67, 68, 69}) {
            Kiem.dung("id_eff_pet_tool van con " + id, chuaSo(dongMang, id));
        }
    }

    /** Moi <id>.pet: 3 o = 3 o dau cua bang hieu ung <id>, nam trong anh, 3 khung 1 part, bay cao DO_CAO_BAY, duoi = bang 49. */
    private static void kiemBangKhungPet() {
        final byte[] b49 = docByte("map/d_effect/49");
        Kiem.dung("bang pet mau 49 con (3 o, du dai)", b49.length > 42 && (b49[0] & 0xFF) == 3);
        for (int id = 24; id <= 28; id++) {
            final byte[] bHieuUng = docByte("map/d_effect/" + id);
            final byte[] bPet = docByte("map/d_effect/" + id + ".pet");
            final int[] kichCoAnh = kichCoPng("map/d_effect/" + id + ".png");
            Kiem.dung(id + ": co bang hieu ung goc, anh png, va bang rieng .pet",
                    bHieuUng.length > 16 && bPet.length > 42 && kichCoAnh != null);
            if (bHieuUng.length <= 16 || bPet.length <= 42 || kichCoAnh == null || b49.length <= 42) {
                continue;
            }
            Kiem.dung(id + ": bang hieu ung goc co it nhat 3 o", (bHieuUng[0] & 0xFF) >= 3);
            Kiem.bang(id + ".pet: 3 o nho", Integer.valueOf(3), Integer.valueOf(bPet[0] & 0xFF));
            for (int i = 0; i < 3; i++) {
                final int goc = 1 + i * 5;
                Kiem.bang(id + ".pet o " + i + " = o " + i + " cua bang hieu ung (x,y,w,h)",
                        (bHieuUng[goc + 1] & 0xFF) + "," + (bHieuUng[goc + 2] & 0xFF) + "," + (bHieuUng[goc + 3] & 0xFF) + "," + (bHieuUng[goc + 4] & 0xFF),
                        (bPet[goc + 1] & 0xFF) + "," + (bPet[goc + 2] & 0xFF) + "," + (bPet[goc + 3] & 0xFF) + "," + (bPet[goc + 4] & 0xFF));
                Kiem.dung(id + ".pet o " + i + " nam trong anh " + kichCoAnh[0] + "x" + kichCoAnh[1],
                        (bPet[goc + 1] & 0xFF) + (bPet[goc + 3] & 0xFF) <= kichCoAnh[0]
                        && (bPet[goc + 2] & 0xFF) + (bPet[goc + 4] & 0xFF) <= kichCoAnh[1]);
                Kiem.dung(id + ".pet o " + i + " co kich co that", (bPet[goc + 3] & 0xFF) > 0 && (bPet[goc + 4] & 0xFF) > 0);
            }
            Kiem.bang(id + ".pet: 3 khung", Integer.valueOf(3), Integer.valueOf(docShort(bPet, 16)));
            for (int i = 0; i < 3; i++) {
                final int k = 18 + i * 8;
                final int w = bPet[1 + i * 5 + 3] & 0xFF;
                final int h = bPet[1 + i * 5 + 4] & 0xFF;
                Kiem.bang(id + ".pet khung " + i + ": 1 part", Integer.valueOf(1), Integer.valueOf(bPet[k] & 0xFF));
                Kiem.bang(id + ".pet khung " + i + ": dx = -w/2 (can giua)", Integer.valueOf(-(w / 2)), Integer.valueOf(docShort(bPet, k + 1)));
                Kiem.bang(id + ".pet khung " + i + ": dy = -h - " + DO_CAO_BAY + " (bay)", Integer.valueOf(-h - DO_CAO_BAY), Integer.valueOf(docShort(bPet, k + 3)));
                Kiem.bang(id + ".pet khung " + i + ": tro o nho " + i, Integer.valueOf(i), Integer.valueOf(bPet[k + 5] & 0xFF));
            }
            // Client: khung 0 co dy + h <= -5 -> isFly (bong o chan, con thu lo lung), khop dai -25..-33 cua 35-44.
            final int h0 = bPet[1 + 4] & 0xFF;
            final int dy0 = docShort(bPet, 18 + 3);
            Kiem.dung(id + ".pet: isFly (dy0 + h0 = " + (dy0 + h0) + " <= -5)", dy0 + h0 <= -5);
            boolean duoiKhop = bPet.length == b49.length;
            for (int i = 42; duoiKhop && i < b49.length; i++) {
                duoiKhop = bPet[i] == b49[i];
            }
            Kiem.dung(id + ".pet: sequence/anim/bong trung tung byte voi bang pet 49", duoiKhop);
        }
    }

    /** Hai ham dung goi 15 phai ghi imgID cung do rong: >= 2.2.5 -> 2 byte, cu hon -> 1 byte. */
    private static void kiemDoRongImgId() {
        final String mc = doc("src/real/MessageCreator.java");
        final int viTriInfo = mc.indexOf("public static Message createCharWearingCharInfo(");
        final int viTriWearing = mc.indexOf("public static Message createCharWearingMessage(");
        Kiem.dung("con ca hai ham dung goi 15", viTriInfo > -1 && viTriWearing > viTriInfo);
        if (viTriInfo < 0 || viTriWearing < 0) {
            return;
        }
        final String doanInfo = mc.substring(viTriInfo, viTriWearing);
        final String doanWearing = mc.substring(viTriWearing);
        Kiem.dung("createCharWearingCharInfo: imgID phan nhanh theo version 2.2.5 (khong con luon 1 byte)",
                doanInfo.contains("version.compareTo(\"2.2.5\")") && doanInfo.contains("m.dos.writeShort(imgIdPet)"));
        Kiem.dung("createCharWearingMessage: van phan nhanh theo version 2.2.5",
                doanWearing.contains("version.compareTo(\"2.2.5\") < 0"));
    }

    private static boolean chuaSo(final String dong, final int so) {
        final String[] phan = dong.split("[^0-9]+");
        for (final String p : phan) {
            if (!p.isEmpty() && Integer.parseInt(p) == so) {
                return true;
            }
        }
        return false;
    }

    private static int docShort(final byte[] b, final int i) {
        return (short) (((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF));
    }

    /** Doc rong/cao tu header PNG (IHDR o byte 16-23), khong can ImageIO. */
    private static int[] kichCoPng(final String duong) {
        final byte[] b = docByte(duong);
        if (b.length < 24) {
            return null;
        }
        final int w = ((b[16] & 0xFF) << 24) | ((b[17] & 0xFF) << 16) | ((b[18] & 0xFF) << 8) | (b[19] & 0xFF);
        final int h = ((b[20] & 0xFF) << 24) | ((b[21] & 0xFF) << 16) | ((b[22] & 0xFF) << 8) | (b[23] & 0xFF);
        return new int[]{w, h};
    }

    private static String doan(final String ma, final String tu, final String den) {
        final int bd = ma.indexOf(tu);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf(den, bd + tu.length());
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt + den.length());
    }

    private static byte[] docByte(final String duong) {
        try {
            return Files.readAllBytes(Paths.get(duong));
        } catch (final IOException e) {
            return new byte[0];
        }
    }

    private static String doc(final String duongDan) {
        try {
            return new String(Files.readAllBytes(Paths.get(duongDan)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    /** Vat pham pet gia: khong can CSDL, chi can atb0 (cap), atb1 (anh) va level cua mau. */
    private static final class VatPhamPet extends Item {
        private final ItemTemplates template = new ItemTemplates();

        VatPhamPet(final int cap, final int atb1, final int levelMau) {
            this.template.atb = new short[33];
            this.template.atb[0] = (short) cap;
            this.template.atb[1] = (short) atb1;
            this.template.level = (byte) levelMau;
            this.template.name = "Yeu Tinh cap " + cap;
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
