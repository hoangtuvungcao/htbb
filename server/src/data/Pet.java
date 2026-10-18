package data;

import io.Message;
import java.util.Hashtable;
import java.util.Vector;
import real.Actor;
import real.Char;
import real.EffectBuff;
import real.GemTemplate;
import real.Item;
import real.ItemTemplates;
import real.LevelPet;
import real.LiveActor;
import real.Map;
import real.MessageCreator;
import server.TeamServer;

public class Pet extends Actor {
    public static int MAX_POINT_EAT = 20000;
    public static int MAX_POWER_EAT = 300;
    public static byte TYPE_FLY_1_FRAME = 0;
    public static byte TYPE_WALK = 1;
    public static byte TYPE_LANTERNS = 2;
    public static byte TYPE_FLY_2_FRAME = 3;
    public static byte TYPE_FLY_IMAGE = 4;
    public static byte TYPE_PET_TOOL = 5;
    public short powerEat = 0;
    public String name = "";
    public byte idImg = 0;
    public byte place;
    public byte type;
    public int idDB = 0;
    public int ownerid = 0;
    public int idDbliendau = 0;
    public long timeBuy = 0L;
    public long lastEat;
    public int totalMinute = 0;
    public byte idTemplate;
    public LevelPet lvDetail = new LevelPet();
    public Item itemPet = null;
    static String[] decript = new String[]{"Tăng công 20%", "Tăng 10% tấn công, 10% phòng thủ", "Tăng 20% tấn công, 20% phòng thủ", "Tăng công 30%", "Tăng 10%exp,10% tấn công,10% phòng thủ", "Tăng 10%exp,10% tấn công", "Tăng 10%exp,10% tấn công,10% phòng thủ, tặng 100% thuộc tính băng lan, sét lan, độc lan, lửa lan", "Tăng 10%exp,10% tấn công,10% phòng thủ", "Tăng 10%exp,10% tấn công,10% phòng thủ", "Tăng 10%exp,10% tấn công,10% phòng thủ, x3 thuộc tính băng lan", "Tăng 10%exp,10% tấn công,10% phòng thủ, x3 thuộc tính sét lan", "Tăng 10%exp,10% tấn công,10% phòng thủ, x3 thuộc tính độc lan", "Tăng 5% tấn công", "Tăng 10%exp,10% tấn công,10% phòng thủ, tăng 100% thuộc tính băng lan, sét lan, độc lan, lửa lan, 2% bạo kích", "Tăng 10%exp,10% tấn công,10% phòng thủ, tăng 100% thuộc tính băng lan, sét lan, độc lan, lửa lan, 2% bạo kích", "Tăng 10%exp,10% tấn công,10% phòng thủ", "", "", "Tăng 10%exp,10% tấn công,10% phòng thủ", "Tăng 10% exp", "Tăng 5% phòng thủ", "Tăng 10%exp, tặng 100% thuộc tính băng lan, sét lan, độc lan, lửa lan", "Tăng 10%exp,10% tấn công,10% phòng thủ. Mỗi 5s hồi phục 5k hp cho chủ. 50% biến chủ nhân thành Tề thiên đại thánh", "", "", "", "", "", "Tăng 25%exp,10% tấn công", ""};
    public static int[][] percent = new int[][]{{20, 0, 0, 0, 0, 0, 0, 0}, {10, 10, 0, 0, 0, 0, 0, 0}, {20, 20, 0, 0, 0, 0, 0, 0}, {30, 0, 0, 0, 0, 0, 0, 0}, {10, 10, 10, 0, 0, 0, 0, 0}, {10, 0, 10, 0, 0, 0, 0, 0}, {10, 10, 10, 1, 1, 1, 0, 1}, {10, 10, 10, 0, 0, 0, 0, 0}, {10, 10, 10, 0, 0, 0, 0, 0}, {10, 10, 10, 3, 0, 0, 0, 0}, {10, 10, 10, 0, 3, 0, 0, 0}, {10, 10, 10, 0, 0, 3, 0, 0}, {5, 0, 0, 0, 0, 0, 0, 0}, {10, 10, 10, 1, 1, 1, 20, 1}, {10, 10, 10, 1, 1, 1, 20, 1}, {10, 10, 10, 0, 0, 0, 0, 0}, {10, 10, 10, 0, 0, 0, 0, 0}, {10, 10, 10, 0, 0, 0, 0, 0}, {10, 10, 10, 0, 0, 0, 0, 0}, {0, 0, 10, 0, 0, 0, 0, 0}, {0, 5, 0, 0, 0, 0, 0, 0}, {10, 0, 10, 1, 1, 1, 0, 1}, {10, 10, 10, 0, 0, 0, 0, 0}, new int[8], new int[8], new int[8], new int[8], new int[8], {10, 0, 25, 0, 0, 0, 0, 0}, new int[8], new int[8], new int[8], new int[8], new int[8], new int[8]};
    static int[][] pc_chang_thong_thao = new int[][]{{983, 14, 3, 0}, {996, 498, 3, 0}, {999, 666, 333, 0}, {1000, 750, 500, 250}};
    long timeBienHinh = 0L;
    // Hoa hinh Pet (them 31/08/2026, port tu ban goc khac - xem README).
    // idHoaHinhPet: -1 = chua hoa hinh, khac -1 = da hoa hinh (danh dau CO/KHONG,
    // gan = 67 luc kich hoat cho ro rang - gia tri cu the KHONG dung de chon anh
    // hien thi). Anh hien thi thuc te chon theo capHoaHinh, xem getIdEffPet().
    public short idHoaHinhPet = -1;
    public long timeEndHoaHinhPet = 0L;
    // 0..5, luon dong bo bang capHoaHinh qua initKhangXuyenGiapWhenHoaHinh() -
    // tach rieng (khong dung thang capHoaHinh) de khop dinh dang luu CSDL cu.
    public byte khangXuyenGiapCap = 0;
    public byte capHoaHinh = 0;
    // Cooldown RIENG cho khang xuyen giap - KHONG dung chung timeBienHinh, vi
    // truong do da la cooldown cua 5 noi tai theo loai thu (checkBienHinh/
    // checkVetThuongSau/checkHoangSo/checkBatTu/checkYemBua). Neu dung chung,
    // hoa hinh se boi tranh cooldown voi chinh noi tai cua loai thu đó.
    private long timeKhangXuyenGiap = 0L;
    // Moc het hieu luc cua lan kich hoat hien tai (cua so duy tri 3-8s, xem
    // applyKhangXuyenGiap): trong cua so nay MOI don nhan vao deu duoc giam. Khong luu CSDL.
    private long timeKhangXuyenGiapHetHieuLuc = 0L;
    /** Han hoa hinh Lan: 10 ngay tinh tu luc hoa hinh; hoa hinh lai thi tinh lai tu dau. */
    public static final long HAN_HOA_HINH_MS = 864_000_000L;
    // Index 5 noi dai theo dung buoc tang +3 cua 4 buoc truoc (2,5,8,11) - PC_UPDGRADE_OK
    // co san entry [5]=7% cho tang nay (pet o tier 5 la tier co that, dat duoc binh
    // thuong) nhung MATERIAL_UPDGRADE thieu hang tuong ung nen doUpdrage() nem
    // ArrayIndexOutOfBoundsException ngay dong dau tien khi mot pet da o tier 5 bam nang cap.
    static int[] MATERIAL_UPDGRADE = new int[]{0, 2, 5, 8, 11, 14}; // GD2 1.3: them [5]=14
    static int[] PC_UPDGRADE_OK = new int[]{0, 90, 45, 20, 15, 7};
    byte phamchat = -1;
    byte maxSkill = 0;
    byte max_pham_chat = 0;
    short id_skill1 = -1;
    short id_skill2 = -1;
    static String[] NAME_PHAM_CHAT = new String[]{"Ngu đần", "Bình thường", "Nhanh nhẹn", "Thông minh"};
    public static short ID_SKILL_CHUA_TRI = 0;
    public static short ID_SKILL_PHA_TRIEN = 1;
    public static short ID_SKILL_DOAT_MENH = 2;
    public static short ID_SKILL_THIEN_NHAN = 3;
    public static short ID_SKILL_TIEN_KHI = 4;
    public static short ID_SKILL_KIM_CANG = 5;
    public static short ID_SKILL_CAN_KHON = 6;
    public static short ID_SKILL_CUONG_TRANG = 7;
    public static short ID_SKILL_CUONG_THAN = 8;
    public static short ID_SKILL_KIEN_CO = 9;
    public static short ID_SKILL_HO_THE = 10;
    public static short ID_SKILL_LIEN_KICH = 11;
    public static short ID_SKILL_TANG_KICH = 12;
    public static short ID_SKILL_THUAN_ANH = 13;
    public static short ID_SKILL_PHI_HUYET = 14;
    public static SkillPet[] ALL_SKILL = new SkillPet[]{new SkillPet("Chữa trị", "Hồi #% hp mỗi 5s", 0, 1, 3), new SkillPet("Phá triền", "Giảm 50% sát thương. Tỷ lệ xuất hiện #%", 1, 5, 10, 5, 10), new SkillPet("Đoạt mệnh", "Tăng #% sát thương", 2, 5, 10), new SkillPet("Thiên nhãn", "Né đòn. Tỷ lệ xuất hiện #%", 3, 10, 20), new SkillPet("Tiên khí", "Chủ nhân không bị hiệu ứng bất lợi(ko loại bỏ sát thương từ hiệu ứng). Tỷ lệ xuất hiện #%, tồn tại trong @s", 4, 5, 10, 5, 10), new SkillPet("Kim cang", "Tăng #% thủ vật lý", 5, 5, 10), new SkillPet("Càn khôn", "Tăng #% thủ ma pháp", 6, 5, 10), new SkillPet("Cường tráng", "Tăng #% hp", 7, 5, 15), new SkillPet("Cường thân", "Tăng #% mp", 8, 5, 10), new SkillPet("Kiên cố", "Tăng #% phòng thủ", 9, 5, 10), new SkillPet("Hộ thể", "Tăng #% phòng thủ trang bị", 10, 15, 30), new SkillPet("Liên kích", "Tăng #% sát thương chí mạng", 11, 5, 15), new SkillPet("Tăng kích", "Tăng #% bạo kích", 12, 1, 3), new SkillPet("Thuấn ảnh", "Hồi # mp sau mỗi 5s", 13, 1000, 2000), new SkillPet("Phí huyết", "Hồi # hp sau mỗi 5s", 14, 500, 1000)};
    public static short[][] ID_SKILL_PET;
    static int[] PC_LEAR_SKILL_1;
    static int[] PC_LEAR_SKILL_2;
    byte canTrade = 0;
    public static int[] limitLevel;
    public short lastlevel = 0;
    public boolean ispetthoihan = false;

    static {
        ID_SKILL_PET = new short[][]{{ID_SKILL_CHUA_TRI, ID_SKILL_DOAT_MENH, ID_SKILL_TIEN_KHI, ID_SKILL_CUONG_TRANG, ID_SKILL_KIEN_CO, ID_SKILL_HO_THE}, {ID_SKILL_PHA_TRIEN, ID_SKILL_THIEN_NHAN, ID_SKILL_KIM_CANG, ID_SKILL_CAN_KHON, ID_SKILL_LIEN_KICH, ID_SKILL_TANG_KICH}, {ID_SKILL_CUONG_THAN, ID_SKILL_THUAN_ANH, ID_SKILL_PHI_HUYET}};
        PC_LEAR_SKILL_1 = new int[]{0, 1, 10, 25};
        PC_LEAR_SKILL_2 = new int[]{0, 0, 5, 10};
        limitLevel = new int[]{40, 50, 50};
    }

    public Pet(int idBuyShop) {
        this.id = (short)idBuyShop;
        this.cat = 12;
    }

    public Pet(String name, byte idImg, int owner, String info, String stitem, String lastlog) {
        this.name = name;

        try {
            if (name != null && name.indexOf("hh:") > -1) {
                this.name = Char.split(name, "hh:")[0];
            }
        } catch (Exception var15) {
        }

        this.idImg = idImg;
        this.ownerid = owner;
        this.initDbInfo(info);
        if (this.idTemplate == 19 && this.idImg != 18) {
            this.idImg = 18;
        }

        if (this.idTemplate == 20 && this.idImg != 19) {
            this.idImg = 19;
        }

        if (this.idTemplate == 21 && this.idImg != 17) {
            this.idImg = 17;
        }
        this.apDungAnhTheoMau();

        name = this.getPetTemplate().name;
        if (stitem != null && !stitem.equals("")) {
            String lastlogout = Char.split(lastlog.trim(), ".")[0].trim();
            String[] dtitem = Char.split(stitem, "|");
            Item item = new Item();
            item.dbInfo = dtitem[0];
            item.initInfoFromDB();
            item.setTemplate(item.tempateID, item.clazz, item.clazz, item.tempateID);
            String[] data = Char.split(dtitem[1], ",");
            String countnguhanh = "";

            for(int j = 0; j < data.length; ++j) {
                try {
                    if (j < 33) {
                        item.atb[j] = Short.parseShort(data[j].trim());
                        if (j >= 28 && item.atb[j] > 0 && j != item.idNguHanh2) {
                            countnguhanh = countnguhanh + j + ",";
                        }
                    } else if (j < 43) {
                        item.newAtb[j - 33] = Byte.parseByte(data[j].trim());
                    } else if (j < 58) {
                        item.addMoreLevelSkill[j - 43] = Byte.parseByte(data[j].trim());
                    } else if (j < 61) {
                        item.lockAtb[j - 58] = Byte.parseByte(data[j].trim());
                    } else {
                        item.otherAtt[j - 61] = Short.parseShort(data[j].trim());
                    }
                } catch (Exception var14) {
                }
            }

            item.id = 0;
            this.itemPet = item;
            if (this.itemPet.getTemplate().atb[0] > 1) {
                this.itemPet.doAddOptionRandomPet(this.itemPet.getTemplate().atb[0], false);
            }

            if (lastlogout.compareTo("2017-12-20 15:15:00") < 0) {
                if (this.itemPet.isCu()) {
                    this.itemPet.doAddOptionPetCu(this.itemPet.getTemplate().atb[0]);
                } else if (this.itemPet.isRong()) {
                    this.itemPet.doAddOptionPetRong(this.itemPet.getTemplate().atb[0]);
                } else if (this.itemPet.isDoi()) {
                    this.itemPet.doAddOptionPetDoi(this.itemPet.getTemplate().atb[0]);
                } else if (this.itemPet.isYeuTinh()) {
                    this.itemPet.doAddOptionPetYeuTinh(this.itemPet.getTemplate().atb[0]);
                } else if (this.itemPet.isDaiBang()) {
                    this.itemPet.doAddOptionPetDaiBang(this.itemPet.getTemplate().atb[0]);
                }
            }
        }

        if (this.itemPet != null) {
            if (this.lvDetail.lv < limitLevel[0] && this.itemPet.level > 0) {
                this.lvDetail.setExp(this.lvDetail.getExp() + 1L);
            }

            if (this.lvDetail.canUplevel() && this.itemPet.getTemplate().atb[0] == 5 && this.itemPet.level == 0 && this.lvDetail.lv < 40) {
                this.lvDetail.setExp(this.lvDetail.getExp() + 1L);
                this.lastlevel = (short)this.lvDetail.lv;
            }
        }

    }

    public Pet(byte idtemplate, byte idimg, short id, int idDB, int ownerID, String name) {
        this.name = name;
        this.id = id;
        this.idImg = idimg;
        this.idDB = idDB;
        this.ownerid = ownerID;
        this.idTemplate = idtemplate;
        this.apDungAnhTheoMau();
        name = this.getPetTemplate().name;
    }

    public Pet(byte idtemplate, byte idimg, short id, int idDB, int ownerID, String name, Item itemP) {
        this.name = name;
        this.id = id;
        this.idImg = idimg;
        this.idDB = idDB;
        this.ownerid = ownerID;
        this.idTemplate = idtemplate;
        this.apDungAnhTheoMau();
        name = this.getPetTemplate().name;
        this.itemPet = itemP;
    }

    /**
     * Sau mau 23 Cú, 24 Rồng, 25 Thỏ bay, 26 Dơi, 27 Đại bàng, 29 Bạch Thử chua bao gio co anh
     * rieng - Map.loadPetTemplate cho chung MUON bo anh/khung/kieu di chuyen cua mot mau co san.
     */
    static boolean laMauMuonAnh(final int idTemplate) {
        return idTemplate >= 23 && idTemplate <= 29 && idTemplate != 28;
    }

    /**
     * Voi sau mau muon anh, idImg va type LUON lay theo bang mau, bo qua gia tri trong DB.
     *
     * VI SAO. 338 dong tob_pet (33 nguoi choi, tinh den 05/09/2026) con luu idImg=21 va
     * type=1 (TYPE_WALK) trong chuoi info - bo gia tri giu cho cua ban mau cu. map/pet/21.png
     * khong ton tai nen server tra anh trong suot 32x96; TYPE_WALK lai bao client dung bang
     * khung 12 khung (chi so toi 9) tren mot anh khai bao 1 khung -> vung ve NGOAI BIEN ->
     * client J2ME nem IllegalArgumentException giua vong ve actor -> moi actor ve sau pet bien
     * mat trong khung hinh do, khung sau hien lai. Do chinh la "nhan vat luc an luc hien ~1
     * lan/giay, o moi ban do" nguoi choi bao ngay 05/09/2026 - bat dau dung luc ho nhan pet
     * loai nay (13:50), khong lien quan ban deploy 02:29 cung ngay.
     *
     * Chi doi bang mau la KHONG DU: Pet.type doc tu info (initDbInfo, truong thu 4), idImg doc
     * tu cot idImg - ca hai deu la du lieu cu. Nen ghi de ngay khi nap, dat sau cac phep ghi de
     * 19/20/21 co san (cung y do, cung cach lam). getDBInfo() va UPDATE tob_pet ghi lai ca type
     * va idImg, nen sau lan luu ke tiep DB tu lanh - khong can chay SQL tay.
     *
     * Goi ca o hai constructor tao pet MOI: chung khong gan type (mac dinh 0 = TYPE_FLY_1_FRAME,
     * bang khung 9 chi so) va mot vai cho goi (Char.addPet) gan type cung sau do; voi sau mau nay
     * gia tri theo mau la dung nhat.
     */
    private void apDungAnhTheoMau() {
        if (!laMauMuonAnh(this.idTemplate)) {
            return;
        }
        final PetTemplate mau = (Map.petTemplate != null && this.idTemplate < Map.petTemplate.length)
                ? Map.petTemplate[this.idTemplate] : null;
        if (mau == null) {
            return;
        }
        this.idImg = mau.idImg;
        this.type = mau.type_move;
    }

    /**
     * Anh hoa hinh Pet = pet Lan: map/d_effect/67.png (cap 1-3), 68.png (cap 4), 69.png (cap 5) -
     * ca ba la luoi sprite 256x256, dong thoi la anh danh hieu 8767/8768/8769. 69 la anh Lan that
     * (yeu cau 02/09/2026); 67/68 la hai o hieu ung tam thoi cho toi khi co anh dang di dung cau
     * truc nhu 69 (xem tools/TaoPetLanCapThap.java) - nguoi yeu cau da xac nhan chap nhan pet
     * dung yen nhap nhay hieu ung o hai cap nay thay vi di that.
     *
     * Duong ve pet tren client (Pet.paint) lay anh GameData.getImgIcon(8700 + id) va bang khung
     * Pet.ALL_PET_DATA[id]; bang do do goi 26 gui trong khoi id_eff_pet_tool (MessageCreator).
     * Bang khung goc "67"/"68"/"69" viet cho hieu ung / danh hieu (anim dai 48/1/0 khung) - pet
     * doc se chia cho 0 va bien mat (su co 01/09/2026 voi rieng 69, luc do doan nham la do anh
     * vuong). Vi vay moi anh nhan bang RIENG <id>.pet (DataGame.moTaPet uu tien <id>.pet), cau
     * truc nhu bang pet 49, sinh bang tools/TaoPetLan.java (69) va TaoPetLanCapThap.java (67, 68).
     *
     * Id 49 cu (anh cat/resize tu 69.png) chua tung nam trong id_eff_pet_tool nen client khong
     * co bang khung cho no - da thay bang 69 + 69.pet. test/KiemHoaHinhPet.java khoa ca ba:
     * cac hang so nay, 67/68/69 co trong id_eff_pet_tool, va cac file .pet dung cau truc.
     */
    public static final int ID_HIEU_UNG_HOA_HINH_PET = 69;
    /** Anh Lan cap 4 (xem idHieuUngHoaHinhTheoCap). */
    public static final int ID_HIEU_UNG_HOA_HINH_PET_CAP_4 = 68;
    /** Anh Lan cap 1-3 (xem idHieuUngHoaHinhTheoCap). */
    public static final int ID_HIEU_UNG_HOA_HINH_PET_CAP_THAP = 67;

    /**
     * Chon anh hoa hinh theo capHoaHinh: cap 1-3 dung chung mot anh (67), cap 4 rieng (68),
     * cap 5 dung anh Lan that (69, ID_HIEU_UNG_HOA_HINH_PET). Quy uoc theo yeu cau 02/09/2026:
     * "pet cấp 1 = Lân cấp 1 (67)", "pet cấp 4 = Lân cấp 2 (68)", "pet cấp 5 = Lân cấp 5 (69,
     * đã có sẵn)" - cap 2, 3 (khong duoc neu ten rieng) dung chung anh voi cap 1.
     */
    private int idHieuUngHoaHinhTheoCap() {
        if (this.capHoaHinh >= 5) {
            return ID_HIEU_UNG_HOA_HINH_PET;
        }
        if (this.capHoaHinh == 4) {
            return ID_HIEU_UNG_HOA_HINH_PET_CAP_4;
        }
        return ID_HIEU_UNG_HOA_HINH_PET_CAP_THAP;
    }

    public int getIdEffPet() {
        if (this.itemPet != null) {
            if (this.idHoaHinhPet != -1 && !this.endHoaHinhPet()) {
                return idHieuUngHoaHinhTheoCap();
            }
            // Level >= 2 dung chung ma cap 2 (truoc day la "== 2"): tool cap 3-5 roi xuong dong
            // "return atb[1]" ben duoi = 24..28, ma client KHONG co bang khung (khong nam trong
            // MessageCreator.id_eff_pet_tool, va map/d_effect/24-28 chi co bang dang hieu ung -
            // dua vao goi 26 la client chia cho 0, xem sự cố 01/09/2026 voi id 69). Pet.paint tren
            // client co kiem null nen pet chi VO HINH chu khong crash - log VPS 05/09/2026 thay
            // client xin 8725/8728 (Rong/Dai bang cap 3-5) 15 lan. Cho toi khi co bang khung
            // rieng cho 24-28, hien cap 3-5 bang anh cap 2 con hon vo hinh. Voi pet-tool,
            // getIdEffPet() chi dung de hien thi (moi cho buff deu co !isPetTool()).
            if (this.isPetCu()) {
                if (this.itemPet.level == 1) {
                    return 37;
                }

                if (this.itemPet.level >= 2) {
                    return 38;
                }
            } else if (this.isPetDaiBang()) {
                if (this.itemPet.level == 1) {
                    return 43;
                }

                if (this.itemPet.level >= 2) {
                    return 44;
                }
            } else if (this.isPetDoi()) {
                if (this.itemPet.level == 1) {
                    return 41;
                }

                if (this.itemPet.level >= 2) {
                    return 42;
                }
            } else if (this.isPetYeuTinh()) {
                if (this.itemPet.level == 1) {
                    return 39;
                }

                if (this.itemPet.level >= 2) {
                    return 40;
                }
            } else if (this.isPetRong()) {
                if (this.itemPet.level == 1) {
                    return 36;
                }

                if (this.itemPet.level >= 2) {
                    return 35;
                }
            }

            // Level 0 (trang thai 1) roi xuong day: atb1 = 24..28 (Cu/Rong/Yeu tinh/Doi/Dai bang).
            // Tu 16/09/2026 day la MA ANH PET-TOOL cua trang thai 1 (ve bang bang khung <id>.pet),
            // khong con la buff dan vao nhan vat - xem getType(). Bach Thu (727, level 5) cung roi
            // xuong day: atb1 = 60.
            return this.itemPet.getTemplate().atb[1];
        } else {
            return -1;
        }
    }

    public void petAttack(Vector<LiveActor> target, Char p) {
    }

    public void doPetAttack(Vector<LiveActor> target, Vector<Integer> dam, Char owner) {
        try {
            int idEff = 0;
            Message msg = new Message(-73);
            msg.dos.writeShort(this.ownerid);
            msg.dos.writeByte(0);
            msg.dos.writeShort(idEff);
            msg.dos.writeByte(target.size());

            for(int i = 0; i < target.size(); ++i) {
                LiveActor tg = (LiveActor)target.get(i);
                msg.dos.writeByte(tg.cat);
                msg.dos.writeShort(tg.id);
                msg.dos.writeInt((Integer)dam.get(i));
                msg.dos.writeInt(tg.getHP(0));
            }

            owner.sendMessage(msg);
            owner.sendToNearPlayer(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void doPetBuff() {
        try {
            int idEff = 0;
            Message msg = new Message(-73);
            msg.dos.writeShort(this.ownerid);
            msg.dos.writeByte(1);
            msg.dos.writeShort(idEff);
            msg.dos.writeLong(0L);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean checkCanTienHoa() {
        if (this.itemPet != null) {
            if (this.itemPet.level == 0 && this.lvDetail.canTienHoa(0) && this.lvDetail.lv >= 40) {
                return true;
            }

            if (this.itemPet.level == 1 && this.lvDetail.canTienHoa(1) && this.lvDetail.lv >= 40) {
                return true;
            }
        }

        return false;
    }

    public boolean checkCanLearSkill() {
        return this.itemPet != null && this.itemPet.level > 0;
    }

    public boolean checkCanChangeThongThao() {
        return this.itemPet != null && this.itemPet.level > 0;
    }

    public String getInfoItem() {
        return this.itemPet != null ? this.itemPet.getInfoSave() : "";
    }

    public int getTimeExpire() {
        if (this.totalMinute == 0 && !this.ispetthoihan) {
            return 32000;
        } else {
            int time = this.totalMinute - (int)((System.currentTimeMillis() - this.timeBuy) / 60000L);
            return time > 0 ? time : -1;
        }
    }
    

    public String getNameSave() {
        return this.itemPet != null ? this.itemPet.getName() : this.name;
    }

    public String getName() {
        String hh = null;
        if (this.totalMinute > 0 && this.timeBuy > 0L) {
            long time = (long)this.totalMinute;
            String[] info = Char.split(Char.getDayTimeByTime(this.timeBuy + time * 60000L), " ");
            hh = "\nhh: " + info[0] + "\n-" + info[1];
        }

        String idInfo = "\nID: " + this.idDB;
        // Dang hoa hinh Lan thi ten hien "Lân cấp n - <ten goc>" de nguoi choi biet ngay (luat 02/09/2026).
        final String tienTo = this.dangHoaHinh() ? "Lân cấp " + this.capHoaHinh + " - " : "";
        return tienTo + (this.itemPet != null ? this.itemPet.getName() + idInfo + (hh != null ? hh : "") : this.name + idInfo + (hh != null ? hh : ""));
    }

    public int getbaoKich() {
        return this.itemPet != null ? this.itemPet.otherAtt[4] + this.getTangKich() : percent[this.idTemplate][6];
    }

    /**
     * Ten pham chat, an toan voi moi gia tri.
     *
     * phamchat = -1 la gia tri MAC DINH cua moi thu nuoi chua tung doi pham chat
     * (xem khai bao o dau lop), va getInfo() cung in no ra thanh dau "?". Tra
     * cuu thang NAME_PHAM_CHAT[-1] la ArrayIndexOutOfBoundsException.
     */
    private static String tenPhamChat(final int pc) {
        if (pc < 0) {
            return "chua co";
        }
        if (pc >= NAME_PHAM_CHAT.length) {
            return "khong ro (" + pc + ")";
        }
        return NAME_PHAM_CHAT[pc];
    }

    public void doChangeThongThao(Char p) {
        int pc = Map.r.nextInt(1000);
        int oldPhamChat = this.phamchat;

        // Bang ti le chi co 4 dong. max_pham_chat doc tu chuoi luu trong database
        // (dong "Byte.parseByte(data[12])" o initFromDB) va chi duoc chan can duoi
        // <0, khong chan can tren - du lieu hong la vuot mang ngay dong duoi.
        if (this.max_pham_chat < 0) {
            this.max_pham_chat = 0;
        } else if (this.max_pham_chat >= pc_chang_thong_thao.length) {
            this.max_pham_chat = (byte) (pc_chang_thong_thao.length - 1);
        }
        int old_skill1 = this.id_skill1;
        int old_skill2 = this.id_skill2;
        int old_maxskill = this.maxSkill;
        this.id_skill1 = -1;
        this.id_skill2 = -1;
        this.maxSkill = 0;
        if (pc <= pc_chang_thong_thao[this.max_pham_chat][0]) {
            this.phamchat = 3;
            this.maxSkill = 2;
            this.max_pham_chat = 3;
        } else if (pc <= pc_chang_thong_thao[this.max_pham_chat][1]) {
            this.phamchat = 2;
            if (Map.r.nextInt(100) < 20) {
                this.maxSkill = 2;
            } else {
                this.maxSkill = 1;
            }

            if (this.max_pham_chat < 2) {
                this.max_pham_chat = 2;
            }
        } else if (pc < pc_chang_thong_thao[this.max_pham_chat][2]) {
            this.phamchat = 1;
            this.maxSkill = 1;
            if (this.max_pham_chat < 1) {
                this.max_pham_chat = 1;
            }
        } else {
            this.phamchat = 0;
            this.maxSkill = 1;
        }

        p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
        p.sendMessage(MessageCreator.createServerAlertMessage("Thay đổi phẩm chất từ " + tenPhamChat(oldPhamChat) + " qua " + tenPhamChat(this.phamchat), ""));
        Database.instance.saveOrtherLog("", p.charname, "pham chat " + oldPhamChat + "_" + this.phamchat + ", maxskill " + old_maxskill + "_" + this.maxSkill + ",skill1 " + old_skill1 + ", skill2 " + old_skill2 + ". Thay đổi phẩm chất từ " + tenPhamChat(oldPhamChat) + " qua " + tenPhamChat(this.phamchat) + " " + this.itemPet.level + " >> " + this.itemPet.getInfoSave(), "doiphamchat");
    }

    public boolean doTienHoa(Char p) {
        if (this.itemPet != null && this.itemPet.level < 2) {
            if (!this.isPetTool() && this.getIdEffPet() > -1) {
                p.huyEff(p, this.getIdEffPet());
            }

            ++this.itemPet.level;
            this.lvDetail.setExp(this.lvDetail.getExp() + 1L);
            this.lastlevel = (short)this.lvDetail.lv;
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendMessage(MessageCreator.createServerAlertMessage("Tiến hoá thành công lên trạng thái " + this.itemPet.level, ""));
            Database.instance.saveOrtherLog("", p.charname, "Tiến hoá thành công lên trạng thái " + this.itemPet.level + " >> " + this.itemPet.getInfoSave(), "tienhoa");
            return true;
        } else {
            return false;
        }
    }

    public boolean endHoaHinhPet() {
        return this.idHoaHinhPet != -1 && this.timeEndHoaHinhPet - System.currentTimeMillis() <= 0L;
    }

    public void initKhangXuyenGiapWhenHoaHinh() {
        this.khangXuyenGiapCap = this.capHoaHinh;
    }

    /**
     * Bang "Khang xuyen giap" theo cap Lan 1..5 (yeu cau ngay 02/09/2026):
     * {giam % sat thuong, ti le kich hoat %, thoi gian duy tri (giay), hoi chieu (giay)}.
     *   cap 1:  +5%  / 10% / 3s / 100s
     *   cap 2:  +10% / 10% / 3s / 100s
     *   cap 3:  +15% / 20% / 5s / 80s
     *   cap 4:  +20% / 20% / 5s / 80s
     *   cap 5:  +25% / 30% / 8s / 60s
     * Dung CHUNG giua applyKhangXuyenGiap() (thuc thi), getInfoSendUser() (hien cho nguoi
     * choi) va RealController case 13 (thong bao luc hoa hinh). Cap < 1 tra ve toan 0, cap > 5
     * tinh nhu 5.
     */
    public static int[] thongSoKhangXuyenGiap(final int cap) {
        switch (cap) {
            case 1: return new int[]{5, 10, 3, 100};
            case 2: return new int[]{10, 10, 3, 100};
            case 3: return new int[]{15, 20, 5, 80};
            case 4: return new int[]{20, 20, 5, 80};
            default: return cap < 1 ? new int[]{0, 0, 0, 0} : new int[]{25, 30, 8, 60};
        }
    }

    /**
     * Giam sat thuong nhan vao theo "Khang xuyen giap" cua pet Lan. Goi tu
     * CharChiSo.applyThanKhiOnHit() moi khi chu nhan bi danh va dang dung pet nay.
     *
     * Co che (khac ban 31/08 chi giam MOT don roi vao hoi chieu): kich hoat mo mot CUA SO DUY
     * TRI 3-8s, moi don trong cua so deu bi giam, khong quay lai ti le; hoi chieu tinh tu luc
     * kich hoat. Ngoai cua so va da het hoi chieu thi moi quay ti le lan nua.
     */
    public int applyKhangXuyenGiap(final int finalDame) {
        return applyKhangXuyenGiap(finalDame, System.currentTimeMillis());
    }

    /** Ban nhan moc thoi gian, de test dieu khien duoc duy tri / hoi chieu. */
    public int applyKhangXuyenGiap(final int finalDame, final long now) {
        if (this.khangXuyenGiapCap <= 0 || this.endHoaHinhPet()) {
            return finalDame;
        }
        final int[] thongSo = thongSoKhangXuyenGiap(this.khangXuyenGiapCap);
        if (now < this.timeKhangXuyenGiapHetHieuLuc) {
            return finalDame - finalDame * thongSo[0] / 100;
        }
        if (now < this.timeKhangXuyenGiap) {
            return finalDame;
        }
        if (Map.r.nextInt(100) >= thongSo[1]) {
            return finalDame;
        }
        this.timeKhangXuyenGiapHetHieuLuc = now + thongSo[2] * 1000L;
        this.timeKhangXuyenGiap = now + thongSo[3] * 1000L;
        return finalDame - finalDame * thongSo[0] / 100;
    }

    /** Dang mang hinh Lan: da hoa hinh va con han. */
    public boolean dangHoaHinh() {
        return this.idHoaHinhPet != -1 && !this.endHoaHinhPet();
    }

    /**
     * Cap cua pet nuoi = cot atb0 cua template item pet ("Cú cấp 3" co atb0 = 3), 0 neu chua
     * co cap. Day la "n" trong cong thuc: pet cap n + n x 10 binh hoa hinh = pet Lan cap n.
     */
    public int capPet() {
        if (this.itemPet == null || this.itemPet.getTemplate() == null) {
            return 0;
        }
        return Math.max(0, Math.min(5, this.itemPet.getTemplate().atb[0]));
    }

    /** So binh hoa hinh (item 268) de hoa hinh pet cap n thanh Lan cap n: n x 10; -1 neu chua co cap. */
    public static int soBinhHoaHinhCan(final int capPet) {
        return capPet < 1 ? -1 : capPet * 10;
    }

    /**
     * Neu hoa hinh da het han thi tra pet ve con cu (xoa moi dau vet Lan) va tra ve true.
     * Goi tu Char.checkExpirePet() (moi 5 phut, khi dang choi) va Database.getCharPet() (luc nap).
     */
    public boolean donHoaHinhHetHan() {
        if (!this.endHoaHinhPet()) {
            return false;
        }
        this.idHoaHinhPet = -1;
        this.timeEndHoaHinhPet = 0L;
        this.khangXuyenGiapCap = 0;
        this.capHoaHinh = 0;
        this.timeKhangXuyenGiapHetHieuLuc = 0L;
        return true;
    }

    public void doUpdrage2MaxByAdmin(Char p, int nUp) {
        try {
            for(int k = 0; k < nUp; ++k) {
                System.out.println("nang cap pet by admin " + this.itemPet.getTemplate().idItemUpLevel);
                this.lvDetail.setExp(LevelPet.getXpFromLevel(50));
                this.lastlevel = (short)this.lvDetail.lv;
                ItemTemplates template = (ItemTemplates)((Hashtable)Map.itemTemplates.get(0)).get(Integer.valueOf(this.itemPet.getTemplate().idItemUpLevel));
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = 0;
                newItem.owner = 0;
                if (this.itemPet.minuteExist > 0) {
                    newItem.minuteExist = this.itemPet.minuteExist;
                    newItem.timeLoan = this.itemPet.timeLoan;
                }

                newItem.level = newItem.getTemplate().level;
                newItem.identify = 0;
                newItem.clazz = 0;
                newItem.doAddOptionPet(template.atb[0]);

                for(int i = 0; i < Item.OPTION_RANDOM.length; ++i) {
                    for(int j = 0; j < Item.OPTION_RANDOM[i].length; ++j) {
                        if (i != 0 && i != 1 && i != 4 && i != 5) {
                            newItem.lockAtb[Item.OPTION_RANDOM[i][j]] = this.itemPet.lockAtb[Item.OPTION_RANDOM[i][j]];
                        } else {
                            newItem.otherAtt[Item.OPTION_RANDOM[i][j]] = this.itemPet.otherAtt[Item.OPTION_RANDOM[i][j]];
                        }
                    }
                }

                newItem.doAddOptionRandomPet(template.atb[0], false);
                this.itemPet = newItem;
                this.itemPet.level = 2;
                newItem.dateCreate = Char.getDayTime(0L);
                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
                // Che do Pokemon: item pet doi -> loai Pokemon co the doi; nguoi o gan cung can -94.
                real.TienHoaPokemon.guiPokePetChoNguoiGan(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void doUpdrage(Char p, int idLkd) {
        int soluongDel = MATERIAL_UPDGRADE[this.itemPet.getTemplate().atb[0]];

        for(int i = 0; i < GemTemplate.ID_MATERIAL_HIGHT[5].length; ++i) {
            if (p.listGemitem[GemTemplate.ID_MATERIAL_HIGHT[5][i]] < soluongDel && p.listGemitemLock[GemTemplate.ID_MATERIAL_HIGHT[5][i]] < soluongDel) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Ngươi cần có " + soluongDel + " nguyên liệu cao cấp mỗi loại", ""));
                return;
            }

            if (p.listGemitem[GemTemplate.ID_MATERIAL_LOW[5][i]] < soluongDel && p.listGemitemLock[GemTemplate.ID_MATERIAL_LOW[5][i]] < soluongDel) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Ngươi cần có " + soluongDel + " nguyên liệu sơ cấp mỗi loại", ""));
                return;
            }
        }

        int pcLKD = 0;
        if (idLkd == GemTemplate.LKD_30 || idLkd == GemTemplate.LKD_35 || idLkd == GemTemplate.LKD_40) {
            if (idLkd == GemTemplate.LKD_30) {
                pcLKD = 30;
            } else if (idLkd == GemTemplate.LKD_35) {
                pcLKD = 35;
            } else if (idLkd == GemTemplate.LKD_40) {
                pcLKD = 40;
            }

            if (p.listGemitem[idLkd] < 1 && p.listGemitemLock[idLkd] < 1) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Không đủ Luyện kim dược", ""));
                return;
            }
        }

        String info = "nâng cấp " + this.getName() + " " + (idLkd > -1 ? " su dung lkd " + idLkd : " ko su dung lkd") + " lên cấp " + (this.itemPet.getTemplate().atb[0] + 1);
        int pc = PC_UPDGRADE_OK[this.itemPet.getTemplate().atb[0]] + pcLKD;
        if (Map.r.nextInt(100) < pc) {
            this.lvDetail.setExp(this.lvDetail.getExp() + 1L);
            this.lastlevel = (short)this.lvDetail.lv;
            ItemTemplates template = (ItemTemplates)((Hashtable)Map.itemTemplates.get(0)).get(Integer.valueOf(this.itemPet.getTemplate().idItemUpLevel));
            Item newItem = null;
            newItem = new Item(template, false, 0, 0, template.id);
            newItem.id = 0;
            newItem.owner = 0;
            newItem.level = newItem.getTemplate().level;
            newItem.identify = 0;
            newItem.clazz = 0;
            newItem.doAddOptionPet(template.atb[0]);
            info = info + " thành công";

            for(int i = 0; i < Item.OPTION_RANDOM.length; ++i) {
                for(int j = 0; j < Item.OPTION_RANDOM[i].length; ++j) {
                    if (i != 0 && i != 1 && i != 4 && i != 5) {
                        newItem.lockAtb[Item.OPTION_RANDOM[i][j]] = this.itemPet.lockAtb[Item.OPTION_RANDOM[i][j]];
                    } else {
                        newItem.otherAtt[Item.OPTION_RANDOM[i][j]] = this.itemPet.otherAtt[Item.OPTION_RANDOM[i][j]];
                    }
                }
            }

            newItem.doAddOptionRandomPet(template.atb[0], false);
            this.itemPet = newItem;
            newItem.dateCreate = Char.getDayTime(0L);
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            // Che do Pokemon: nang cap doi item pet (vd Zubat cap 2 -> Golbat cap 3); lenh 3 o tren chi
            // gui cho minh (kem -94), nguoi o gan nhan -94 rieng.
            real.TienHoaPokemon.guiPokePetChoNguoiGan(p);
            p.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thành công lên " + this.getName(), ""));
        } else {
            info = info + " thất bại";
            p.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thất bại", ""));
        }

        info = info + " và sử dụng:";

        for(int i = 0; i < GemTemplate.ID_MATERIAL_HIGHT[5].length; ++i) {
            if (p.listGemitemLock[GemTemplate.ID_MATERIAL_HIGHT[5][i]] >= soluongDel) {
                p.delGem(GemTemplate.ID_MATERIAL_HIGHT[5][i], soluongDel, true);
                info = info + "," + Map.gemTemplate[GemTemplate.ID_MATERIAL_HIGHT[5][i]].name + " khoa " + soluongDel;
            } else {
                p.delGem(GemTemplate.ID_MATERIAL_HIGHT[5][i], soluongDel, false);
                info = info + "," + Map.gemTemplate[GemTemplate.ID_MATERIAL_HIGHT[5][i]].name + " " + soluongDel;
            }

            if (p.listGemitemLock[GemTemplate.ID_MATERIAL_LOW[5][i]] >= soluongDel) {
                p.delGem(GemTemplate.ID_MATERIAL_LOW[5][i], soluongDel, true);
                info = info + "," + Map.gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khoa " + soluongDel;
            } else {
                p.delGem(GemTemplate.ID_MATERIAL_LOW[5][i], soluongDel, false);
                info = info + "," + Map.gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " " + soluongDel;
            }
        }

        if (idLkd > -1) {
            if (p.listGemitemLock[idLkd] > 0) {
                p.delGem(idLkd, 1, true);
            } else {
                p.delGem(idLkd, 1, false);
            }
        }

        p.sendMessage(MessageCreator.createCharGemItem(p));
        Database.instance.saveOrtherLog("", p.charname, info, "nangcappet");
    }

    public int[] checkBienHinh() {
        if (this.isPetCu() && this.CanUseNoiTai()) {
            if (this.itemPet != null && this.itemPet.level != 0) {
                if (Map.r.nextInt(100) < (this.itemPet.level > 1 ? 20 : 10) && System.currentTimeMillis() - this.timeBienHinh >= 0L) {
                    if (this.itemPet.level == 1) {
                        this.timeBienHinh = System.currentTimeMillis() + 120000L;
                        return new int[]{5000, 30};
                    } else {
                        this.timeBienHinh = System.currentTimeMillis() + 60000L;
                        return new int[]{5000, 90};
                    }
                } else {
                    return new int[]{-1, -1};
                }
            } else {
                return new int[]{-1, -1};
            }
        } else {
            return new int[]{-1, -1};
        }
    }

    public int[] checkVetThuongSau() {
        if (this.isPetDoi() && this.CanUseNoiTai()) {
            if (this.itemPet != null && this.itemPet.level != 0) {
                if (Map.r.nextInt(100) < (this.itemPet.level > 1 ? 20 : 10) && System.currentTimeMillis() - this.timeBienHinh >= 0L) {
                    if (this.itemPet.level == 1) {
                        this.timeBienHinh = System.currentTimeMillis() + 60000L;
                        return new int[]{EffectBuff.VET_THUONG_SAU, 5000};
                    } else {
                        this.timeBienHinh = System.currentTimeMillis() + 30000L;
                        return new int[]{EffectBuff.VET_THUONG_SAU, 10000};
                    }
                } else {
                    return new int[]{-1};
                }
            } else {
                return new int[]{-1, 0};
            }
        } else {
            return new int[]{-1, -1};
        }
    }

    public int[] checkHoangSo() {
        if (this.isPetRong() && this.CanUseNoiTai()) {
            if (this.itemPet != null && this.itemPet.level != 0) {
                if (Map.r.nextInt(100) < (this.itemPet.level > 1 ? 20 : 10) && System.currentTimeMillis() - this.timeBienHinh >= 0L) {
                    if (this.itemPet.level == 1) {
                        this.timeBienHinh = System.currentTimeMillis() + 60000L;
                        return new int[]{EffectBuff.HOANG_LOAN, 5000};
                    } else {
                        this.timeBienHinh = System.currentTimeMillis() + 30000L;
                        return new int[]{EffectBuff.HOANG_LOAN, 10000};
                    }
                } else {
                    return new int[]{-1};
                }
            } else {
                return new int[]{-1};
            }
        } else {
            return new int[]{-1};
        }
    }

    public int[] checkBatTu(Char p) {
        if (this.isPetDaiBang() && this.CanUseNoiTai()) {
            if (this.itemPet != null && this.itemPet.level != 0) {
                if (Map.r.nextInt(100) < (this.itemPet.level > 1 ? 30 : 10) && System.currentTimeMillis() - this.timeBienHinh >= 0L) {
                    int pcHp = p.hp * 100 / p.maxhp;
                    if (this.itemPet.level == 1) {
                        if (pcHp < 50) {
                            this.timeBienHinh = System.currentTimeMillis() + 60000L;
                            return new int[]{EffectBuff.BAT_TU, 5000};
                        }
                    } else if (pcHp < 70) {
                        this.timeBienHinh = System.currentTimeMillis() + 60000L;
                        return new int[]{EffectBuff.BAT_TU, 5000};
                    }
                }

                return new int[]{-1};
            } else {
                return new int[]{-1};
            }
        } else {
            return new int[]{-1};
        }
    }

    public int[] checkYemBua() {
        if (this.isPetYeuTinh() && this.CanUseNoiTai()) {
            if (this.itemPet != null && this.itemPet.level != 0) {
                if (Map.r.nextInt(100) < (this.itemPet.level > 1 ? 20 : 10) && System.currentTimeMillis() - this.timeBienHinh >= 0L) {
                    if (this.itemPet.level == 1) {
                        this.timeBienHinh = System.currentTimeMillis() + 60000L;
                        return new int[]{EffectBuff.YEM_BUA, 5000, 95};
                    } else {
                        this.timeBienHinh = System.currentTimeMillis() + 30000L;
                        return new int[]{EffectBuff.YEM_BUA, 10000, 99};
                    }
                } else {
                    return new int[]{-1};
                }
            } else {
                return new int[]{-1};
            }
        } else {
            return new int[]{-1};
        }
    }

    public String getInfoSendUser() {
        if (this.itemPet != null) {
            String info = "";
            if (!this.isPetBachThu()) {
                info = info + "- Sức ăn : " + this.powerEat + "/" + MAX_POWER_EAT;
                info = info + "\n- Trạng thái: " + (this.itemPet.level + 1) + "/3";
                info = info + "\n- Phẩm chất: " + (this.phamchat == -1 ? "?" : tenPhamChat(this.phamchat));
                // Nhan phai la "Cap do": so in ra la lvDetail.lv (1..LevelPet.expMain.length),
                // con TIEN HOA la dong "Trang thai: x/3" ngay tren (itemPet.level + 1) - dung
                // thuat ngu ma chinh game dung luc tien hoa xong ("...lên trạng thái N").
                // De nhan "Tien hoa" o day thi bang thong tin pet KHONG CON dong nao ten "Cap
                // do", ma ten pet lai la "Rồng cấp 1..5" (bac template) - nguoi choi doc con so
                // "cap" trong ten roi tuong do la cap do. Da bao nham that ngay 10/09/2026:
                // pet lv50 bi tu choi cho an, nguoi choi khang dinh "chua he len lv50".
                info = info + "\n- Cấp độ: " + this.lvDetail.lv + "+" + this.lvDetail.percent / 10 + "." + this.lvDetail.percent % 10 + "%";
                info = info + "\n- Kỹ năng có thể học: " + (this.phamchat > -1 ? this.maxSkill : 0);
                if (this.id_skill1 > -1) {
                    info = info + "\n- Kỹ năng: " + ALL_SKILL[this.id_skill1].name;
                    String decript = ALL_SKILL[this.id_skill1].getDecript(this.itemPet.level);
                    if (decript != null) {
                        info = info + decript;
                    }
                }

                if (this.id_skill2 > -1) {
                    info = info + "\n- Kỹ năng: " + ALL_SKILL[this.id_skill2].name;
                    String decript = ALL_SKILL[this.id_skill2].getDecript(this.itemPet.level);
                    if (decript != null) {
                        info = info + decript;
                    }
                }
            }

            if (this.isPetBachThu()) {
                info = info + "\nChống đồ sát.";
            } else if (this.isPetCu()) {
                info = info + "\n- Ru ngủ mục tiêu trong 10s. Khi bị ru ngủ mục tiêu sẽ bất động và sẽ tỉnh lại khi nhận sát thương hoặc hết 10s";
            } else if (this.isPetRong()) {
                info = info + "\n- Gây choáng mục tiêu trong 5s. Khi choáng mục tiêu không thể di chuyển, không thể trình diễn kỹ năng, không thể uống hp";
            } else if (this.isPetYeuTinh()) {
                info = info + "\n- Gây bỏng mục tiêu trong 5s. Khi bỏng mục tiêu sẽ bị nhận thêm " + this.getPcUpSatThuongBong() + "% sát thương.";
            } else if (this.isPetDoi()) {
                info = info + "\n- Gây độc mục tiêu trong 5s. Mỗi giây người chơi sẽ mất " + this.getDoc() + " hp do độc gây ra.";
            } else if (this.isPetDaiBang()) {
                info = info + "\n- Làm suy yếu mục tiêu trong 5s. Khi suy yếu sức tấn công và phòng thủ sẽ bị giảm " + this.getPcTacDongSuyYeu() + "%.";
            }

            if (this.itemPet.level == 1) {
                if (this.isPetCu()) {
                    info = info + "\n- Biến hình,gst 30% trong 5s, 10% xuất hiện, chờ 120s";
                } else if (this.isPetDoi()) {
                    info = info + "\n- Vết thương sâu, ko hồi hp trong 5s, 10% xuất hiện, Chờ 60s";
                } else if (this.isPetRong()) {
                    info = info + "\n- Hoảng sợ trong 5s, 10% xuất hiện";
                } else if (this.isPetDaiBang()) {
                    info = info + "\n- Bất tử trong 5s( 10% xuất hiện khi hp<50%. Chờ 60s)";
                } else if (this.isPetYeuTinh()) {
                    info = info + "\n- Yểm bùa(mất 95%mp, không hồi mp) trong 5s, 10% xuất hiện";
                }
            } else if (this.itemPet.level == 2) {
                if (this.isPetCu()) {
                    info = info + "\n- Biến hình,gst 90% trong 5s, 20% xuất hiện, chờ 60s";
                } else if (this.isPetDoi()) {
                    info = info + "\nVết thương sâu, ko hồi hp trong 10s, 20% xuất hiện, chờ 30s";
                } else if (this.isPetRong()) {
                    info = info + "\n- Hoảng sợ trong 10s, 20% xuất hiện";
                } else if (this.isPetDaiBang()) {
                    info = info + "\n- Bất tử trong 5s( 30% xuất hiện khi hp<70%. Chờ 60s)";
                } else if (this.isPetYeuTinh()) {
                    info = info + "\n- Yểm bùa(mất 99%mp, không hồi mp) trong 10s, 20% xuất hiện";
                }
            }

            info = info + "\n" + this.itemPet.getInfoOptionPet();
            if (this.itemPet.getTemplate().atb[0] < 5) {
                info = info + "\n- Tiến hoá cần:";
                info = info + "\n- Cần " + MATERIAL_UPDGRADE[this.itemPet.getTemplate().atb[0]] + " nguyên liệu cao cấp cấp 6";
                info = info + "\n- Cần " + MATERIAL_UPDGRADE[this.itemPet.getTemplate().atb[0]] + " nguyên liệu sơ cấp cấp 6";
                info = info + "\n- Tỷ lệ thành công: " + PC_UPDGRADE_OK[this.itemPet.getTemplate().atb[0]] + "%";
            }

            // Hoa hinh Lan (luat 02/09/2026): day la cho nguoi choi doc duoc cap Lan,
            // con han bao lau va khang xuyen giap dang co gi. Dung CHUNG bang
            // thongSoKhangXuyenGiap() voi applyKhangXuyenGiap() de hien dung so dang chay.
            if (this.capHoaHinh > 0) {
                if (this.endHoaHinhPet()) {
                    info = info + "\n- Hoá hình Lân cấp " + this.capHoaHinh + " đã hết hạn, pet trở về hình cũ";
                } else {
                    final int[] thongSo = thongSoKhangXuyenGiap(this.khangXuyenGiapCap);
                    final long conLaiNgay = (this.timeEndHoaHinhPet - System.currentTimeMillis()) / (24L * 60L * 60000L);
                    info = info + "\n- Pet Lân cấp " + this.capHoaHinh + ", còn " + conLaiNgay + " ngày"
                            + "\n- Kháng xuyên giáp +" + thongSo[0] + "%: khi bị đánh có " + thongSo[1]
                            + "% kích hoạt, giảm " + thongSo[0] + "% sát thương trong " + thongSo[2]
                            + "s, hồi chiêu " + thongSo[3] + "s";
                }
            }

            return info;
        } else {
            return this.canTrade == 1 ? "- " + Pet.decript[this.idTemplate] + "\n- Có thể giao dịch cho người khác." : "- " + Pet.decript[this.idTemplate];
        }
    }

    public boolean canLearnSkill() {
        // level > 0: hoc ky nang chi mo tu trang thai 2 - luat cu. Giu rieng o day vi tu 16/09/2026
        // isPetTool() da dung cho ca trang thai 1 (xem getType()).
        if (!this.isPetBachThu() && this.isPetTool() && this.itemPet.level > 0) {
            if (this.phamchat != -1 && this.phamchat < NAME_PHAM_CHAT.length) {
                if (this.maxSkill == 1 && this.id_skill1 > -1) {
                    return false;
                } else {
                    return this.maxSkill <= 1 || this.id_skill1 <= -1 || this.id_skill2 <= -1;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void doLearSkillTest(int idskill) {
        if (idskill > 14) {
            idskill = 14;
        }

        this.id_skill1 = (short)idskill;
    }

    public int doLearSkill() {
        int[] pc = new int[]{10, 100, 500};
        int index = Char.getIndexRandom(pc);
        if (this.id_skill1 == -1) {
            if (Map.r.nextInt(100) >= PC_LEAR_SKILL_1[this.phamchat]) {
                return -1;
            } else {
                while(this.id_skill1 == this.id_skill2) {
                    this.id_skill1 = ID_SKILL_PET[index][Map.r.nextInt(ID_SKILL_PET[index].length)];
                }

                return this.id_skill1;
            }
        } else {
            if (this.maxSkill > 1 && this.id_skill1 > -1) {
                index = Char.getIndexRandom(pc);
                if (this.id_skill2 == -1) {
                    if (Map.r.nextInt(100) >= PC_LEAR_SKILL_2[this.phamchat]) {
                        return -1;
                    }

                    while(this.id_skill2 == this.id_skill1) {
                        this.id_skill2 = ID_SKILL_PET[index][Map.r.nextInt(ID_SKILL_PET[index].length)];
                    }

                    return this.id_skill2;
                }
            }

            return -1;
        }
    }

    public int getDoc() {
        return this.itemPet != null && this.isPetDoi() ? this.itemPet.otherAtt[49] : 0;
    }

    public int getTimeTrungDoc() {
        return this.itemPet != null && this.isPetDoi() ? 5000 : 0;
    }

    public int getHpHutByHit() {
        return this.itemPet != null && this.isPetDoi() ? this.itemPet.otherAtt[48] : 0;
    }

    public int getpcHutHpByHit() {
        return this.itemPet != null && this.isPetDoi() ? this.itemPet.otherAtt[52] : 0;
    }

    public int getpcTrungDoc() {
        return this.itemPet != null && this.isPetDoi() ? this.itemPet.otherAtt[44] : 0;
    }

    public int getpcSuyYeu() {
        return this.itemPet != null && this.isPetDaiBang() ? this.itemPet.otherAtt[45] : 0;
    }

    public int getNeTranh() {
        return this.itemPet != null && this.isPetDaiBang() ? this.itemPet.atb[2] : 0;
    }

    public int getPcTacDongSuyYeu() {
        return this.itemPet != null && this.isPetDaiBang() ? this.itemPet.otherAtt[51] : 0;
    }

    public int getPcBong() {
        return this.itemPet != null && this.isPetYeuTinh() ? this.itemPet.otherAtt[43] : 0;
    }

    public int getPcBaoKich() {
        return this.itemPet != null && this.isPetYeuTinh() ? this.itemPet.otherAtt[4] : 0;
    }

    public int getPcUpSatThuongBong() {
        return this.itemPet != null && this.isPetYeuTinh() ? this.itemPet.otherAtt[53] : 0;
    }

    public int getPcChoang() {
        return this.itemPet != null && this.isPetRong() ? this.itemPet.otherAtt[42] : 0;
    }

    public int getSatThuong() {
        return this.itemPet != null && this.isPetRong() ? this.itemPet.otherAtt[47] : 0;
    }

    public int getPcRuNgu() {
        return this.itemPet != null && this.isPetCu() ? this.itemPet.otherAtt[41] : 0;
    }

    public int getChuyenSatThuong() {
        return this.itemPet != null && this.isPetCu() ? this.itemPet.otherAtt[46] : 0;
    }

    public int getBangLan() {
        return this.itemPet != null ? this.itemPet.otherAtt[7] : 0;
    }

    public int getLuaLan() {
        return this.itemPet != null ? this.itemPet.otherAtt[28] : 0;
    }

    public int getSetLan() {
        return this.itemPet != null ? this.itemPet.otherAtt[5] : 0;
    }

    public int getStrength() {
        return this.itemPet != null ? this.itemPet.newAtb[4] : 0;
    }

    public int getspirit() {
        return this.itemPet != null ? this.itemPet.newAtb[6] : 0;
    }

    public int getagitity() {
        return this.itemPet != null ? this.itemPet.newAtb[5] : 0;
    }

    public int getHealth() {
        return this.itemPet != null ? this.itemPet.newAtb[7] : 0;
    }

    public int getDocLan() {
        return this.itemPet != null ? this.itemPet.otherAtt[6] : 0;
    }

    public int getHpRegen() {
        if (this.isPetKhi()) {
            return 2000;
        } else if (this.itemPet != null) {
            int value = this.itemPet.otherAtt[17];
            return value;
        } else {
            return 0;
        }
    }

    public int getMpRegen() {
        if (this.isPetKhi()) {
            return 2000;
        } else {
            return this.itemPet != null ? this.itemPet.otherAtt[18] : 0;
        }
    }

    public int getPcUpExp() {
        return this.itemPet != null ? this.itemPet.otherAtt[50] : percent[this.idTemplate][2];
    }

    public int getPcUpDam() {
        return this.itemPet != null ? this.itemPet.lockAtb[0] : percent[this.idTemplate][0];
    }

    public int getPcUpDef() {
        return this.itemPet != null ? this.itemPet.lockAtb[1] : percent[this.idTemplate][1];
    }

    public int getXLuaLan() {
        return percent[this.idTemplate][7];
    }

    public int getXBangLan() {
        return percent[this.idTemplate][3];
    }

    public int getXSetLan() {
        return percent[this.idTemplate][4];
    }

    public int getXDocLan() {
        return percent[this.idTemplate][5];
    }

    public boolean isPetBachThu() {
        return this.idTemplate == 29;
    }

    public boolean isPetKhi() {
        return this.idTemplate == 22;
    }

    public boolean isPetCu() {
        return this.idTemplate == 23;
    }

    public boolean isPetRong() {
        return this.idTemplate == 24;
    }

    public boolean isPetYeuTinh() {
        return this.idTemplate == 25;
    }

    public boolean isPetDoi() {
        return this.idTemplate == 26;
    }

    public boolean isPetDaiBang() {
        return this.idTemplate == 27;
    }

    /**
     * So khung client dung de cat anh pet trong AnimalMove.paint (hImg = anh.cao / sumFrame).
     * Chi duoc ghi vao goi cmd 15 (MessageCreator, hai cho).
     */
    public byte getTotalFrame() {
        if (this.getType() == TYPE_PET_TOOL) {
            return SO_KHUNG_GUI_CHO_PET_TOOL;
        }
        return Map.petTemplate[this.idTemplate].nFrame;
    }

    /**
     * Pet co tool (getType() == TYPE_PET_TOOL) duoc gui type=5 va imgID = getIdEffPet(). Client ve
     * no bang lop Pet (anh 8700+idEff, bang khung rieng tu goi 26) - nhung van dung them mot
     * AnimalMove cung imgID do, va AnimalMove.paint xin anh 5200+idEff (log VPS 05/09/2026: 5235,
     * 5236, 5243, 5260, 5269 = dung cac ma hieu ung). map/pet/<idEff>.png khong ton tai nen server
     * tra anh trong suot 32x96; AnimalMove cat anh do theo bang khung type 5 (chi so toi 11). Neu
     * sumFrame la nFrame cua mau (4-12) thi khung >= nFrame roi ra NGOAI BIEN -> client J2ME nem
     * IllegalArgumentException giua vong ve. Gui 12 (= chi so toi da + 1) thi hImg = 96/12 = 8 va
     * moi khung 0..11 deu nam trong 96 diem cao. Lop Pet khong doc sumFrame nen khong anh huong.
     */
    static final byte SO_KHUNG_GUI_CHO_PET_TOOL = 12;

    public boolean CanUseNoiTai() {
        if (this.itemPet == null) {
            return false;
        } else {
            int pcEat = this.powerEat * 100 / MAX_POWER_EAT;
            return pcEat >= 20 || TeamServer.isServerLocal();
        }
    }

    public int[] getChuaTri() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_CHUA_TRI || this.id_skill2 == ID_SKILL_CHUA_TRI)) {
            if (this.itemPet.level == 1) {
                return new int[]{1, 5000};
            }

            if (this.itemPet.level == 2) {
                return new int[]{3, 5000};
            }
        }

        return new int[]{0, 5000};
    }

    public int[] getPhatTrien() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_PHA_TRIEN || this.id_skill2 == ID_SKILL_PHA_TRIEN)) {
            if (this.itemPet.level == 1) {
                return new int[]{50, 5, 5000};
            }

            if (this.itemPet.level == 2) {
                return new int[]{50, 10, 10000};
            }
        }

        return new int[3];
    }

    public int getDoatMenh() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_DOAT_MENH || this.id_skill2 == ID_SKILL_DOAT_MENH)) {
            if (this.itemPet.level == 1) {
                return 5;
            }

            if (this.itemPet.level == 2) {
                return 10;
            }
        }

        return 0;
    }

    public int getNeDon() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_THIEN_NHAN || this.id_skill2 == ID_SKILL_THIEN_NHAN)) {
            if (this.itemPet.level == 1) {
                return 10;
            }

            if (this.itemPet.level == 2) {
                return 20;
            }
        }

        return 0;
    }

    public int[] getTienKhi() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_TIEN_KHI || this.id_skill2 == ID_SKILL_TIEN_KHI)) {
            if (this.itemPet.level == 1) {
                return new int[]{5, 5000};
            }

            if (this.itemPet.level == 2) {
                return new int[]{10, 10000};
            }
        }

        return new int[2];
    }

    public int[] getCanKhon() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_CAN_KHON || this.id_skill2 == ID_SKILL_CAN_KHON)) {
            if (this.itemPet.level == 1) {
                return new int[]{3, 5};
            }

            if (this.itemPet.level == 2) {
                return new int[]{6, 10};
            }
        }

        return new int[2];
    }

    public int[] getKimCang() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_KIM_CANG || this.id_skill2 == ID_SKILL_KIM_CANG)) {
            if (this.itemPet.level == 1) {
                return new int[]{3, 5};
            }

            if (this.itemPet.level == 2) {
                return new int[]{6, 10};
            }
        }

        return new int[2];
    }

    public int getCuongTrang() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_CUONG_TRANG || this.id_skill2 == ID_SKILL_CUONG_TRANG)) {
            if (this.itemPet.level == 1) {
                return 5;
            }

            if (this.itemPet.level == 2) {
                return 15;
            }
        }

        return 0;
    }

    public int getCuongThan() {
        if (this.CanUseNoiTai() && this.itemPet != null && (this.id_skill1 == ID_SKILL_CUONG_THAN || this.id_skill2 == ID_SKILL_CUONG_THAN)) {
            if (this.itemPet.level == 1) {
                return 5;
            }

            if (this.itemPet.level == 2) {
                return 10;
            }
        }

        return 0;
    }

    public int getKienCo() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_KIEN_CO || this.id_skill2 == ID_SKILL_KIEN_CO)) {
            if (this.itemPet.level == 1) {
                return 5;
            }

            if (this.itemPet.level == 2) {
                return 10;
            }
        }

        return 0;
    }

    public int getHoThe() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_HO_THE || this.id_skill2 == ID_SKILL_HO_THE)) {
            if (this.itemPet.level == 1) {
                return 15;
            }

            if (this.itemPet.level == 2) {
                return 30;
            }
        }

        return 0;
    }

    public int getLienKich() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_LIEN_KICH || this.id_skill2 == ID_SKILL_LIEN_KICH)) {
            if (this.itemPet.level == 1) {
                return 5;
            }

            if (this.itemPet.level == 2) {
                return 15;
            }
        }

        return 0;
    }

    public int getTangKich() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_TANG_KICH || this.id_skill2 == ID_SKILL_TANG_KICH)) {
            if (this.itemPet.level == 1) {
                return 10;
            }

            if (this.itemPet.level == 2) {
                return 30;
            }
        }

        return 0;
    }

    public int getThuanAnh() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_THUAN_ANH || this.id_skill2 == ID_SKILL_THUAN_ANH)) {
            if (this.itemPet.level == 1) {
                return 1000;
            }

            if (this.itemPet.level == 2) {
                return 2000;
            }
        }

        return 0;
    }

    public int getPhiHuyet() {
        if (this.CanUseNoiTai() && (this.id_skill1 == ID_SKILL_PHI_HUYET || this.id_skill2 == ID_SKILL_PHI_HUYET)) {
            if (this.itemPet.level == 1) {
                return 500;
            }

            if (this.itemPet.level == 2) {
                return 1000;
            }
        }

        return 0;
    }

    public void initDbInfo(String info) {
        try {
            String[] data = Char.split(info, ",");
            this.idTemplate = Byte.parseByte(data[0]);
            this.timeBuy = Long.parseLong(data[1]);
            this.totalMinute = Integer.parseInt(data[2]);
            this.type = Byte.parseByte(data[3]);
            this.lvDetail.setExp(Long.parseLong(data[4]));
            this.powerEat = Short.parseShort(data[5]);
            this.ispetthoihan = Byte.parseByte(data[6]) == 1;
            this.phamchat = Byte.parseByte(data[7]);
            this.maxSkill = Byte.parseByte(data[8]);
            this.id_skill1 = Byte.parseByte(data[9]);
            this.id_skill2 = Byte.parseByte(data[10]);
            this.canTrade = Byte.parseByte(data[11]);
            this.max_pham_chat = Byte.parseByte(data[12]);
            if (this.max_pham_chat < 0) {
                this.max_pham_chat = 0;
            }
            // Hoa hinh Pet (them 31/08/2026) - du lieu cu (data.length<=13) khong
            // co may truong nay, giu nguyen gia tri mac dinh khai bao tren field
            // (-1/0/0/0). Kiem tra do dai TUNG truong mot, khong dung try/catch
            // chung o tren, de mot ban ghi cu khong lam mat luon max_pham_chat.
            if (data.length > 13) {
                this.idHoaHinhPet = Short.parseShort(data[13]);
            }
            if (data.length > 14) {
                this.timeEndHoaHinhPet = Long.parseLong(data[14]);
            }
            if (data.length > 15) {
                this.khangXuyenGiapCap = Byte.parseByte(data[15]);
            }
            if (data.length > 16) {
                this.capHoaHinh = Byte.parseByte(data[16]);
            }
        } catch (Exception e) {
            Logger.logErrorPet(e);
        }

    }

    public String getDBInfo() {
        String info = String.valueOf(this.idTemplate);
        info = info + "," + this.timeBuy;
        info = info + "," + this.totalMinute;
        info = info + "," + this.type;
        info = info + "," + this.lvDetail.getExp();
        info = info + "," + this.powerEat;
        info = info + "," + (this.ispetthoihan ? 1 : 0);
        info = info + "," + this.phamchat;
        info = info + "," + this.maxSkill;
        info = info + "," + this.id_skill1;
        info = info + "," + this.id_skill2;
        info = info + "," + this.canTrade;
        info = info + "," + this.max_pham_chat;
        info = info + "," + this.idHoaHinhPet;
        info = info + "," + this.timeEndHoaHinhPet;
        info = info + "," + this.khangXuyenGiapCap;
        info = info + "," + this.capHoaHinh;
        return info;
    }

    public boolean isPetTool() {
        return this.getType() == TYPE_PET_TOOL;
    }

    /**
     * Pet co vat pham (no tu trung: Cu/Rong/Yeu tinh/Doi/Dai bang; Bach Thu 727) LUON di duong
     * pet-tool: goi 15 gui type=5 + imgID = getIdEffPet(), client ve bang lop Pet voi bang khung
     * lay tu goi 26 (khoi id_eff_pet_tool).
     *
     * TRUOC 16/09/2026 dieu kien la "itemPet.level > 0". Pet trung o TRANG THAI 1 (level 0 - "cap n"
     * trong ten vat pham la atb0, KHONG phai level nay; ca 25 dong 689-713 deu level 0) bi coi la pet
     * THUONG: server gui type/idImg cua MAU - mau 23-27 tu 05/09 (af1f096) muon bo anh mau co san
     * (con de, canh cut, Giap Ngo, Doi quy, Thanh tuoc) - VA cong buff hieu ung atb1 = 24..28
     * (EffectBuff.PET_1..5) dan vao nhan vat -> nguoi choi thay 2 con (bao 16/09/2026, VPS luc do
     * 118 nhan vat dang mang). Nay level 0 cung la pet-tool: hinh 24..28 (dung con cu/rong/yeu
     * tinh/doi/dai bang cua hieu ung cu) duoc ve nhu MOT con pet bay theo, qua bang khung rieng
     * map/d_effect/24..28.pet (tools/TaoPetTrangThai1.java), va moi cho cong buff pet (deu co dieu
     * kien !isPetTool()) tu dong khong chay nua -> dung 1 con. Nut "Thu cung" van co thong tin vi
     * khoi pet van duoc gui. Luat "chi tu trang thai 2 moi hoc ky nang" giu rieng o canLearnSkill
     * bang level > 0. Test: test/KiemPetTrangThai1LaPetTool.java.
     */
    public short getType() {
        return this.itemPet != null ? TYPE_PET_TOOL : this.type;
    }

    public Pet clone(int charid) {
        Pet pet = new Pet(this.name, this.idImg, charid, this.getDBInfo(), this.itemPet != null ? this.itemPet.getInfoSave() : null, "2017-12-21");
        pet.place = this.place;
        return pet;
    }

    public byte getWframe() {
        return Map.petTemplate[this.idTemplate].wFrame;
    }

    public byte getHframe() {
        return Map.petTemplate[this.idTemplate].hFrame;
    }

    public PetTemplate getPetTemplate() {
        return Map.petTemplate[this.idTemplate];
    }

    public int getIdIconPaint() {
        if (this.itemPet != null) {
            if (this.itemPet.level == 1) {
                if (this.isPetCu()) {
                    return 37;
                }

                if (this.isPetDoi()) {
                    return 41;
                }

                if (this.isPetRong()) {
                    return 36;
                }

                if (this.isPetDaiBang()) {
                    return 43;
                }

                if (this.isPetYeuTinh()) {
                    return 39;
                }
            } else if (this.itemPet.level == 2) {
                if (this.isPetCu()) {
                    return 38;
                }

                if (this.isPetDoi()) {
                    return 42;
                }

                if (this.isPetRong()) {
                    return 35;
                }

                if (this.isPetDaiBang()) {
                    return 44;
                }

                if (this.isPetYeuTinh()) {
                    return 40;
                }
            }
        }

        return Map.petTemplate[this.idTemplate].idImgPaint;
    }

    public boolean addXP(long[] dxp, Char p) {
        if (this.lvDetail.canUplevel()) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Không thể cho ăn khi chưa nâng cấp pet", ""));
            return false;
        } else {
            // Pet da kich tran CA cap do (lv = LevelPet.expMain.length) LAN tien hoa
            // (itemPet.level = 2): phan EXP cua mieng an chac chan bo di.
            //
            // NHUNG KHONG DUOC CHAN CHO AN. Cho an la duong DUY NHAT nap powerEat (suc an,
            // chi tang o dong "powerEat + dxp[1]" ben duoi), ma powerEat tut 5 diem moi 30
            // phut trong doEat, va CanUseNoiTai() doi powerEat >= 20% MAX_POWER_EAT (>= 60)
            // thi noi tai cua pet - Chua Tri, Phat Trien, Doat Menh... - moi chay. Chan cho
            // an nghia la sau khoang mot ngay, dung nhung con pet dau tu nhat cua nguoi choi
            // MAT HAN noi tai va khong con cach nao hoi lai.
            //
            // Ban 09/09/2026 tung chan han o day de chua loi "cho an mat do, thu nuoi khong
            // doi". Do CSDL that ngay 10/09/2026: 53 pet dang mang dinh chan, 13 con da tut
            // duoi nguong noi tai. Nay cho an binh thuong, KHONG chan va cung KHONG bao gi
            // them (nguoi choi phai cho an deu de giu suc an, bao moi mieng an la phien).
            //
            // Truong hop cho an that su vo ich (suc an da day) van bi doEat tu choi truoc khi
            // toi day, kem thong bao "Vat pham cho an vuot qua suc an cua pet".
            this.lvDetail.addExpUpLevel(this.lvDetail.getExp() + dxp[0]);
            int newlevel = this.lvDetail.lv;
            // limitLevel chi co 3 o (bac tien hoa 0/1/2), nhung itemPet.level KHONG phai luc nao
            // cung nam trong 0..2: Char.doCreatePetMax dat thang level = 5, va Item.initInfoFromDB
            // thay level == 0 thi lay getTemplate().level - ma data_item 727 Bach Thu, 790 Bong
            // vang, 802 Kim ho bao bao, 873 Capybara deu ghi level = 5. Nhung con pet do vao day
            // la limitLevel[5] -> ArrayIndexOutOfBounds, bi MapDoMenu nuot mat trong catch rong
            // (~dong 2769) nen nguoi choi thay "cho an khong co gi xay ra" ma khong co bao loi.
            // Kep ve o cuoi = coi nhu da tien hoa het, dung y voi loai pet khong co duong tien hoa.
            final int bacTienHoa = Math.max(0, Math.min(this.itemPet.level, limitLevel.length - 1));
            if (newlevel >= limitLevel[bacTienHoa] && limitLevel[bacTienHoa] > -1 && (this.itemPet.getTemplate().atb[0] < 5 || this.itemPet.getTemplate().atb[0] == 5 && this.itemPet.level == 1)) {
                long[] data = LevelPet.getArrXpFromLevel(limitLevel[bacTienHoa] - 1);
                this.lvDetail.setExp(data[0] + data[1] - 1L);
                newlevel = this.lvDetail.lv;
            }

            this.powerEat = (short)((int)((long)this.powerEat + dxp[1]));
            this.lastlevel = (short)newlevel;
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            if (this.lvDetail.canUplevel() && p != null) {
                if (this.itemPet.getTemplate().atb[0] < 5) {
                    p.sendMessage(MessageCreator.createServerAlertMessage(this.getName() + " đã có thể nâng cấp lên " + ((this.lastlevel + 1) / 10 + 1), ""));
                } else if (this.itemPet.level < 2) {
                    p.sendMessage(MessageCreator.createServerAlertMessage(this.getName() + " đã có thể tiến hoá lên trạng thái" + (this.itemPet.level + 2), ""));
                }
            }

            return true;
        }
    }

    public boolean doEat(long[] exp_powerEat, Char p) {
        int pcEat = this.powerEat * 100 / MAX_POWER_EAT;

        try {
            if (this.itemPet == null) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm cho ăn vượt quá sức ăn của pet", ""));
                return false;
            }

            long min = (System.currentTimeMillis() - this.lastEat) / 60000L;
            int temp = (int)(min / 30L * 5L);
            if (temp > this.powerEat) {
                this.powerEat = 0;
            } else if (temp > 0) {
                this.powerEat = (short)(this.powerEat - temp);
                if (this.powerEat < 0) {
                    this.powerEat = 0;
                }
            }
        } catch (Exception var8) {
            return false;
        }

        if ((long)this.powerEat + exp_powerEat[1] > (long)MAX_POWER_EAT) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm cho ăn vượt quá sức ăn của pet", ""));
            return false;
        } else {
            this.lastEat = System.currentTimeMillis();
            boolean a = this.addXP(exp_powerEat, p);

            try {
                if (pcEat < 20 && this.powerEat * 100 / MAX_POWER_EAT >= 20) {
                    p.calculateAttrib();
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                }
            } catch (Exception var7) {
            }

            return a;
        }
    }

    public boolean isPetLongDenvinhVien() {
        return this.idTemplate == 28;
    }

    /** Trung Thu 2026 (moi 19/09/2026): co "diem tieu xai" doi lay long den TAI NPC Hang
     * Nga (MapSuKien.java) phai KHONG THE giao dich - khac voi quy tac chung "moi long den
     * deu giao dich duoc" ben duoi. Danh dau rieng luc tao thay vi tach idTemplate, vi cung
     * dung chung id 12/19/20 voi duong "Nhan long den" mien phi (idMenu==8, CO giao dich). */
    public boolean khongTheGiaoDichDuDaLaLongDen = false;

    public boolean canTrade() {
        if (this.khongTheGiaoDichDuDaLaLongDen) {
            return false;
        }
        // Trung Thu 2026 (sua 19/09/2026, yeu cau nguoi dung "tat ca long den deu co the
        // giao dich"): moi long den (type_move == TYPE_LANTERNS, ke ca ban vinh vien id 28)
        // deu giao dich duoc - truoc day chi 5 loai thu whitelist ben duoi (Dai Bang/Cu/
        // Doi/Rong/Yeu Tinh) hoac co dung field canTrade=1 (khong noi nao gan gia tri nay
        // cho long den) moi giao dich duoc, nen moi long den bi khoa cung.
        if (this.type == Pet.TYPE_LANTERNS) {
            return true;
        }
        // Trung Thu 2026 (moi 19/09/2026, yeu cau nguoi dung "pet Capybara... co the cho
        // giao dich"): idTemplate 30 = Capybara (xem Map.java).
        if (this.idTemplate == 30) {
            return true;
        }
        if ((this.isPetDaiBang() || this.isPetCu() || this.isPetDoi() || this.isPetRong() || this.isPetYeuTinh()) && this.ispetthoihan) {
            return true;
        } else {
            return this.canTrade == 1;
        }
    }

    public boolean isPetTradeThoiHan() {
        return (this.isPetDaiBang() || this.isPetCu() || this.isPetDoi() || this.isPetRong() || this.isPetYeuTinh()) && this.ispetthoihan;
    }

    public String getNamePet() {
        return this.name;
    }

    public void setTimeExpire(int seconds) {
        if (seconds <= 0) {
            this.totalMinute = 0;
            this.timeBuy = 0L;
            this.ispetthoihan = false;
        } else {
            this.totalMinute = seconds / 60;
            this.timeBuy = System.currentTimeMillis();
            this.ispetthoihan = true;
        }
    }

    

}
