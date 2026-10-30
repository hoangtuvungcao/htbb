import com.htbb.pokemon.ChuThuongNhiemVu;
import com.htbb.pokemon.HeSinhThaiMenu;
import com.htbb.pokemon.NhaTre;
import com.htbb.pokemon.PetDongHanh;
import com.htbb.pokemon.PetPokemon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import real.Char;
import real.Item;
import real.ItemTemplates;
import real.Map;

/**
 * Pet dong hanh de co hon (chu game 30/10/2026: "có"): moi nhan vat nhan MOT pet co ban mien phi (muc Nha tre hoac thuong
 * buoc chinh C07.3), mua them bang xu (1.500‰H) - com.htbb.pokemon.PetDongHanh, HeSinhThaiMenu, khoi them_pet_dong_hanh.sql.
 */
public final class KiemPetDongHanh {

    private KiemPetDongHanh() {
    }

    public static void chay() {
        Kiem.nhom("Pet dong hanh: nhan mien phi 1 lan, mua bang xu, thuong buoc C07.3, nha tre nhan pet (30/10/2026)");
        try {
            loai();
            nhanMotLan();
            mua();
            nhaTreNhan();
            thuongNhiemVu();
            sqlVaChu();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemPetDongHanh khong nem loi: " + e, false);
        }
    }

    /** 3 loai co ban: item pet cap 1 trong PetPokemon.BANG va bang data_item (type 27, atb0 = cap 1). */
    static void loai() {
        Kiem.bang("3 loai de chon", Integer.valueOf(3), Integer.valueOf(PetDongHanh.LOAI.length));
        final List<List<String>> item = KiemSuaTuyen.dong(DayDu.sql(), "data_item");
        for (final int id : PetDongHanh.LOAI) {
            Kiem.dung("item " + id + " la pet trong PetPokemon.BANG", PetPokemon.dex(id) > 0);
            List<String> r = null;
            for (final List<String> x : item) {
                if (x.get(0).trim().equals(String.valueOf(id))) {
                    r = x;
                }
            }
            Kiem.dung("item " + id + " co trong data_item, type 27, cap 1, gia NPC 0 (khong doi ra xu)",
                    r != null && "27".equals(r.get(2).trim()) && "1".equals(r.get(8).trim()) && "0".equals(r.get(18).trim()));
            Kiem.dung("item " + id + " khong phai dong hiem Dratini", PetPokemon.dex(id) != 147);
        }
        Kiem.bang("chon ngoai danh sach", Integer.valueOf(-1), Integer.valueOf(PetDongHanh.idTheoChon(3)));
        Kiem.bang("chon am", Integer.valueOf(-1), Integer.valueOf(PetDongHanh.idTheoChon(-1)));
    }

    /** Co CSDL gia (INSERT IGNORE theo id_char): lan 2, bam dup, dang nhap lai (doi tuong moi, cung CSDL) khong ra pet thu 2. */
    static void nhanMotLan() {
        final Set<Integer> csdl = new HashSet<Integer>();
        final List<Integer> phat = new ArrayList<Integer>();
        final int idChar = 4242;
        final int k1 = PetDongHanh.nhanMienPhi(1, () -> csdl.add(Integer.valueOf(idChar)), id -> phat.add(Integer.valueOf(id)));
        Kiem.bang("lan 1: OK, phat Zubat", "0:[704]", k1 + ":" + phat);
        final int k2 = PetDongHanh.nhanMienPhi(0, () -> csdl.add(Integer.valueOf(idChar)), id -> phat.add(Integer.valueOf(id)));
        Kiem.bang("lan 2 (loai khac): DA_NHAN, khong phat them", PetDongHanh.DA_NHAN + ":[704]", k2 + ":" + phat);
        // dang nhap lai: bo nho dem menu mat, CSDL con -> van tu choi
        final Set<Integer> csdlSauVaoLai = csdl; // cung bang tob_pet_dong_hanh
        final int k3 = PetDongHanh.nhanMienPhi(2, () -> csdlSauVaoLai.add(Integer.valueOf(idChar)), id -> phat.add(Integer.valueOf(id)));
        Kiem.bang("sau dang nhap lai: DA_NHAN", PetDongHanh.DA_NHAN + ":1", k3 + ":" + phat.size());
        // nhan vat khac (clone cung tai khoan) nhan rieng 1 lan - pet khoa nen khong chuyen duoc
        final int k4 = PetDongHanh.nhanMienPhi(2, () -> csdl.add(Integer.valueOf(idChar + 1)), id -> phat.add(Integer.valueOf(id)));
        Kiem.bang("nhan vat khac: 1 lan cua rieng no", "0:2", k4 + ":" + phat.size());
        // lua chon sai: khong danh dau (khong mat luot)
        final Set<Integer> csdl2 = new HashSet<Integer>();
        final int k5 = PetDongHanh.nhanMienPhi(7, () -> csdl2.add(Integer.valueOf(1)), id -> true);
        Kiem.bang("chon sai: SAI_LOAI, khong ghi co", PetDongHanh.SAI_LOAI + ":0", k5 + ":" + csdl2.size());
        // thu tu trong game: danh dau CSDL TRUOC roi moi phat
        final String hm = docMa("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        final String than = doan(hm, "static void nhanPetMienPhi(", "static void moMuaPet(");
        Kiem.dung("game: danhDauPetDongHanh (INSERT IGNORE) trong nhanMienPhi", than.contains("data.DatabaseLauDai.danhDauPetDongHanh(p.charDBID"));
        Kiem.dung("game: kiem cho trong hanh trang TRUOC khi danh dau", than.indexOf("isFullInventory") >= 0
                && than.indexOf("isFullInventory") < than.indexOf("PetDongHanh.nhanMienPhi"));
        final String db = docMa("src/data/DatabaseLauDai.java");
        Kiem.dung("CSDL: INSERT IGNORE theo khoa id_char, true chi khi chen duoc 1 dong",
                db.contains("INSERT IGNORE INTO tob_pet_dong_hanh (id_char, id_item, nguon, luc)") && db.contains("NOW())\",")
                        && db.contains("PRIMARY KEY (id_char)) DEFAULT CHARSET=utf8\"") && db.contains("nguon) == 1;"));
        Kiem.dung("CSDL: doc loi = coi nhu da nhan", db.contains("return r.isEmpty() || !(r.get(0)[0] instanceof Number)"));
    }

    /** Gia 1.500‰H: cap 20 nam trong 1-2 gio thu nhap; tru TRUOC, khong du thi khong phat. */
    static void mua() {
        long gio20 = 0;
        long gio10 = 0;
        long gio50 = 0;
        for (final List<String> r : KiemSuaTuyen.dong(DayDu.sql(), "data_cap_do_lau_dai")) {
            final int cap = Integer.parseInt(r.get(0).trim());
            final long g = Long.parseLong(r.get(4).trim()) * Long.parseLong(r.get(5).trim()); // quai_moi_gio x xu_moi_quai
            if (cap == 20) {
                gio20 = g;
            } else if (cap == 10) {
                gio10 = g;
            } else if (cap == 50) {
                gio50 = g;
            }
        }
        final long g20 = PetDongHanh.gia(gio20);
        System.out.println("        thu nhap / gio: cap 10 " + gio10 + ", cap 20 " + gio20 + ", cap 50 " + gio50 + " -> gia pet "
                + PetDongHanh.gia(gio10) + " / " + g20 + " / " + PetDongHanh.gia(gio50) + " xu");
        Kiem.dung("gia cap 20 (" + g20 + " xu) trong 1..2 gio thu nhap (" + gio20 + " xu / gio)", gio20 > 0 && g20 >= gio20 && g20 <= 2 * gio20);
        Kiem.bang("gia toi thieu 1 xu", Long.valueOf(1L), Long.valueOf(PetDongHanh.gia(0)));
        final List<String> thuTu = new ArrayList<String>();
        final int k1 = PetDongHanh.mua(0, () -> {
            thuTu.add("tru");
            return true;
        }, id -> thuTu.add("phat " + id));
        Kiem.bang("mua du tien: tru roi moi phat", "0:[tru, phat 689]", k1 + ":" + thuTu);
        thuTu.clear();
        final int k2 = PetDongHanh.mua(2, () -> {
            thuTu.add("tru");
            return false;
        }, id -> thuTu.add("phat " + id));
        Kiem.bang("thieu tien: khong phat", PetDongHanh.THIEU_TIEN + ":[tru]", k2 + ":" + thuTu);
        thuTu.clear();
        final int k3 = PetDongHanh.mua(9, () -> thuTu.add("tru"), id -> thuTu.add("phat"));
        Kiem.bang("chon sai: khong tru", PetDongHanh.SAI_LOAI + ":[]", k3 + ":" + thuTu);
        final String hm = docMa("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        final String than = doan(hm, "static void muaPet(", "static void moTrung(");
        Kiem.dung("game: tru bang HeSinhThaiGame.truXu (truXuNeuDu, xu_ra) truoc khi phat",
                than.contains("HeSinhThaiGame.truXu(p, gia, \"mua pet dong hanh\")") && than.indexOf("truXu") < than.indexOf("createItemSoLuong"));
        Kiem.dung("game: gia menu = gia tru (cung PetDongHanh.gia(HeSinhThaiGame.xuGio(p)))",
                hm.contains("\"Mua pet đồng hành (\" + PetDongHanh.gia(HeSinhThaiGame.xuGio(p)) + \" xu)\"")
                        && than.contains("PetDongHanh.gia(HeSinhThaiGame.xuGio(p))"));
    }

    /** Pet nhan duoc (Char.createItemSoLuong, cung duong no trung) vao tui KHOA va Nha tre nhan gui. */
    static void nhaTreNhan() {
        final Vector<Hashtable<Integer, ItemTemplates>> cu = Map.itemTemplates;
        final real.GemTemplate[] gemCu = Map.gemTemplate;
        try {
            Map.loadGemTemplate();
            Map.itemTemplates = new Vector<Hashtable<Integer, ItemTemplates>>();
            for (int i = 0; i < 6; i++) {
                Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
            }
            for (final int id : PetDongHanh.LOAI) {
                final ItemTemplates t = new ItemTemplates();
                t.id = id;
                t.type = (byte) 27;
                t.name = "Pet " + id;
                t.atb = new short[10];
                for (final Hashtable<Integer, ItemTemplates> b : Map.itemTemplates) {
                    b.put(Integer.valueOf(id), t);
                }
            }
            for (final int id : PetDongHanh.LOAI) {
                final Char p = new Char(null);
                Kiem.dung("chua co pet: nha tre khong co gi de gui", HeSinhThaiMenu.petSoHuu(p).isEmpty());
                final Item it = Char.createItemSoLuong(id, 1, p);
                Kiem.dung("pet " + id + " vao tui, khoa (khong giao dich)", it != null && it.lock == 1 && p.iItems.size() == 1);
                final int dex = PetPokemon.dex(id);
                Kiem.dung("nha tre nhan pet " + PetDongHanh.ten(id) + " (petSoHuu)", HeSinhThaiMenu.petSoHuu(p).contains(Integer.valueOf(dex)));
                Kiem.dung("pet " + id + " de ra trung loai co trong NhaTre.LOAI_TRUNG", NhaTre.chiSo(NhaTre.loaiGoc(dex)) >= 0);
            }
        } finally {
            Map.itemTemplates = cu;
            Map.gemTemplate = gemCu;
        }
    }

    /** Nhan vat moi: thuong buoc chinh C07.3 (cap 14) mo menu chon; chu thuong hien pet. */
    static void thuongNhiemVu() throws Exception {
        List<String> buoc = null;
        for (final List<String> r : KiemSuaTuyen.dong(DayDu.sql(), "data_quest_lau_dai")) {
            if (PetDongHanh.BUOC_THUONG.equals(r.get(0))) {
                buoc = r;
            }
        }
        Kiem.dung("buoc C07.3 la chuoi chinh, cap <= 15, chay engine cu (dong_co = 1 -> khiXongChuoiCu phat thuong_khac)",
                buoc != null && "chinh".equals(buoc.get(2)) && Integer.parseInt(buoc.get(12).trim()) <= 15 && "1".equals(buoc.get(28).trim()));
        final String khoi = DayDu.phanNangCap("them_pet_dong_hanh.sql");
        Kiem.dung("khoi dat thuong_khac pet_dong_hanh:1 cho C07.3 (theo gia tri cu)",
                khoi.contains("SET `thuong_khac` = 'pet_dong_hanh:1'") && khoi.contains("WHERE `ma` = 'C07.3' AND `thuong_khac` = ''"));
        Kiem.dung("PetDongHanh.coThuong", PetDongHanh.coThuong("danh_hieu:X;pet_dong_hanh:1") && !PetDongHanh.coThuong("item:1x1"));
        final String nv = docMa("src/real/NhiemVuEngine.java");
        Kiem.dung("NhiemVuEngine.thuongKhac: pet_dong_hanh -> moNhanPetDongHanh(p, true)",
                nv.contains("com.htbb.pokemon.PetDongHanh.THUONG_KHAC.equals(k)") && nv.contains("HeSinhThaiMenu.moNhanPetDongHanh(p, true)"));
        final String chu = ChuThuongNhiemVu.dong(89646, 1923, 0, 2, "32000", -1, "32000", "32000", "pet_dong_hanh:1", null, null);
        Kiem.bang("chu Phan thuong cua buoc", "89.646 EXP, 1.923 xu, 2 lượng khoá, 1 pet đồng hành (tự chọn, nếu chưa nhận)", chu);
        final String hm = docMa("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        final String mo = doan(hm, "public static void moNhanPetDongHanh(", "static void nhanPetMienPhi(");
        Kiem.dung("thuong nhiem vu dung CUNG co (daNhanPet) voi muc Nha tre", mo.contains("if (daNhanPet(p))"));
    }

    static void sqlVaChu() throws Exception {
        final String khoi = DayDu.phanNangCap("them_pet_dong_hanh.sql");
        Kiem.dung("khoi tao bang tob_pet_dong_hanh (CREATE IF NOT EXISTS, khoa id_char)",
                khoi.contains("CREATE TABLE IF NOT EXISTS `tob_pet_dong_hanh`") && khoi.contains("PRIMARY KEY (`id_char`)"));
        Kiem.sai("khoi khong xoa / chen de du lieu nguoi choi", khoi.contains("DELETE") || khoi.contains("DROP") || khoi.contains("INSERT INTO `tob"));
        final String cu = DayDu.phanNangCap("sua_nha_tre_pet.sql");
        final int a = cu.indexOf("SET `noi_dung` = '");
        final int b = cu.indexOf("'\n  WHERE `ma` = 'npc.19_trung'");
        final String giaTriTruoc = a < 0 || b < 0 ? "?" : cu.substring(a + 18, b);
        Kiem.dung("huong dan npc.19_trung: UPDATE theo gia tri cua khoi truoc + sua_boi = ''",
                khoi.contains("AND `sua_boi` = '' AND `noi_dung` = '" + giaTriTruoc + "';"));
        Kiem.dung("huong dan noi cach nhan pet mien phi / mua", khoi.contains("Nhận pet đồng hành đầu tiên (miễn phí, 1 lần)")
                && khoi.contains("Mua pet đồng hành"));
        final String c = HeSinhThaiMenu.CHUA_CO_PET;
        Kiem.dung("chu Nha tre chua co pet: chi muc nhan mien phi + mua", c.contains(HeSinhThaiMenu.NHAN_PET_MIEN_PHI) && c.contains("Mua pet đồng hành"));
        final String hm = docMa("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        final String mo = doan(hm, "static void moNhaTre(", "public static final String NHAN_PET_MIEN_PHI");
        Kiem.dung("menu Nha tre: muc moi o CUOI (sau Xem trung - chi so 0..4 cu khong doi)",
                mo.indexOf("\"Xem trứng (\"") > 0 && mo.indexOf("NHAN_PET_MIEN_PHI") > mo.indexOf("\"Xem trứng (\""));
        final String tl = docMa("../docs/thiet-ke-game.md");
        Kiem.dung("thiet ke 7.4 ghi pet dong hanh + gia", tl.contains("Pet đồng hành") && tl.contains("1.500‰H"));
    }

    static String docMa(final String duong) {
        try {
            return KiemSuaTuyen.doc(duong);
        } catch (final Exception e) {
            return "";
        }
    }

    static String doan(final String s, final String dau, final String cuoi) {
        final int a = s.indexOf(dau);
        final int b = a < 0 ? -1 : s.indexOf(cuoi, a);
        return a < 0 ? "" : b < 0 ? s.substring(a) : s.substring(a, b);
    }
}
