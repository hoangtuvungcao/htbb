package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.DatTenPet;
import com.htbb.pokemon.PetPokemon;
import data.Database;
import data.Pet;
import io.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Noi com.htbb.pokemon.DatTenPet (lop thuan) vao game: dung "The dat ten Pokemon" (data_vat_pham_lau_dai hieu_ung
 * "dat_ten:1") -&gt; menu chon pet (-30, NPC ao {@link #NPC_CHON_PET}) -&gt; o nhap ten (-31, {@link #INPUT_TEN}) -&gt;
 * server kiem ten, TRU THE TRUOC, quay dong, ghi tob_pet_dat_ten + nhat ky tob_log_dat_ten, tinh lai chi so, gui -94
 * (ten tren dau pet) cho minh va nguoi o gan, hop thoai ket qua (-106 / 37).
 *
 * Bang (sql/them_the_dat_ten_pet.sql, server cung tu tao 2 bang tob_*): data_dong_the_ten (bang dong), data_tu_cam_ten
 * (tu cam trong ten), tob_pet_dat_ten (ten + dong cua tung pet, khoa id_pet = tob_pet.id), tob_log_dat_ten (kiem toan).
 * Thieu bang dong / tu cam = khong dat ten duoc (dong cua, khong mo).
 */
public final class DatTenPetGame {

    /** Menu chon pet (NPC ao, khong trung NapTienGame -540..-549). */
    public static final int NPC_CHON_PET = -561;
    /** O nhap ten (idActor cua goi -31). */
    public static final int INPUT_TEN = -32061;
    /** Ma hieu ung trong data_vat_pham_lau_dai. */
    public static final String HIEU_UNG = "dat_ten";

    /** charDBID -&gt; phien {id item trong tui, id template, danh sach idDB pet, idDB pet da chon (0 = chua)}. */
    private static final ConcurrentHashMap<Integer, Phien> PHIEN = new ConcurrentHashMap<Integer, Phien>();

    static final class Phien {
        final short idItem;
        final int idMau;
        final List<Integer> pet;
        volatile int chon;
        final long luc = System.currentTimeMillis();

        Phien(final short idItem, final int idMau, final List<Integer> pet) {
            this.idItem = idItem;
            this.idMau = idMau;
            this.pet = pet;
        }
    }

    private DatTenPetGame() {
    }

    // ================================================================== cau hinh

    static int so(final String khoa, final int macDinh) {
        return (int) CauHinhTrungTam.so(khoa, macDinh);
    }

    static int[] dsSo(final String khoa, final int[] macDinh) {
        final List<String> ds = CauHinhTrungTam.dsChuoi(khoa);
        if (ds.isEmpty()) {
            return macDinh;
        }
        final int[] ra = new int[ds.size()];
        try {
            for (int i = 0; i < ra.length; i++) {
                ra[i] = Integer.parseInt(ds.get(i));
            }
        } catch (final NumberFormatException e) {
            return macDinh;
        }
        return ra;
    }

    public static boolean bat() {
        return com.htbb.pokemon.LauDai.bat() && so(DatTenPet.K_BAT, 1) != 0;
    }

    static int tranDong() {
        return Math.max(0, Math.min(DatTenPet.TRAN_DONG_MAC_DINH * 2, so(DatTenPet.K_TRAN_DONG, DatTenPet.TRAN_DONG_MAC_DINH)));
    }

    static int tranTrangTri() {
        return Math.max(0, Math.min(DatTenPet.TRAN_TRANG_TRI_MAC_DINH, so(DatTenPet.K_TRAN_TRANG_TRI, DatTenPet.TRAN_TRANG_TRI_MAC_DINH)));
    }

    /** Gia the (luong) ban o Cua hang luong - nguon gia DUY NHAT (cau hinh). */
    public static int giaLuong() {
        return Math.max(com.htbb.pokemon.NapTienLogic.GIA_MON_TOI_THIEU, Math.min(com.htbb.pokemon.NapTienLogic.GIA_MON_TOI_DA,
                so(DatTenPet.K_GIA_LUONG, DatTenPet.GIA_LUONG_MAC_DINH)));
    }

    public static int muaTuanToiDa() {
        return Math.max(0, Math.min(20, so(DatTenPet.K_MUA_TUAN, DatTenPet.MUA_TUAN_MAC_DINH)));
    }

    /**
     * Phan nghin cac o trang tri KHAC dang chiem trong tran tong 5% (thiet_ke_can_bang 4.5: trang phuc 2%, danh hieu 1%,
     * hao quang 1%, skin thu cuoi 1%). He o trang tri theo ngan sach chua chay tren may chu (cac mon cu van qua tran
     * chung chi_so.tran_phan_tram_cong), nen hien la 0; khi he o len, tra tong o day va the ten tu bi kep.
     */
    static int trangTriKhacPhanNghin(final Char p) {
        return 0;
    }

    // ================================================================== trang thai pet

    public static DatTenPet.TrangThai trangThai(final Pet pet) {
        if (pet == null || pet.tenDat == null) {
            return null;
        }
        return new DatTenPet.TrangThai(pet.tenDat, pet.dongMa, pet.dongPhanNghin, pet.datTenSoLan);
    }

    public static boolean daDatTen(final Pet pet) {
        return pet != null && pet.tenDat != null;
    }

    /**
     * Phan nghin chi so {@code ma} tu dong the ten cua pet DANG DI THEO (0 neu khong co). Chi khi gioi han chi so dang
     * bat (moi cong don sau do deu qua tran). Goi trong CharChiSo / ChiSoTrangBiGame / Char.haveNeTranh.
     */
    public static int phanNghin(final Char p, final String ma) {
        return p == null ? 0 : phanNghin(p, p.petUsing, ma);
    }

    public static int phanNghin(final Char p, final Pet pet, final String ma) {
        if (pet == null || pet.dongMa == null || !pet.dongMa.equals(ma) || !ChiSoTrangBiGame.bat()) {
            return 0;
        }
        return DatTenPet.phanNghin(trangThai(pet), ma, tranDong(), tranTrangTri(), trangTriKhacPhanNghin(p));
    }

    /** Ten loai cua pet (Pokemon theo id item, pet kieu cu theo ten mau). */
    static String tenLoai(final Pet pet) {
        if (pet == null) {
            return "";
        }
        if (pet.itemPet != null && pet.itemPet.getTemplate() != null) {
            final int dex = PetPokemon.dex(pet.itemPet.getTemplate().id);
            if (dex > 0) {
                final String t = com.htbb.pokemon.SuuTamSinhThai.tenLoai(dex);
                if (t != null && !t.isEmpty()) {
                    return t;
                }
            }
        }
        try {
            final String t = pet.getPetTemplate() != null ? pet.getPetTemplate().name : pet.name;
            return t == null ? "" : com.htbb.pokemon.ChuPokemon.doi(t);
        } catch (final Exception e) {
            return pet.name == null ? "" : pet.name;
        }
    }

    /** Ten hien tren dau pet: ten rieng, khong co thi ten loai. */
    public static String tenHien(final Pet pet) {
        return daDatTen(pet) ? pet.tenDat : tenLoai(pet);
    }

    /** Dong cho the thong tin pet (Pet.getInfoSendUser): ten rieng + loai + dong chi so. "" neu chua dat ten. */
    public static String dongThongTin(final Pet pet) {
        if (!daDatTen(pet)) {
            return "";
        }
        final String loai = tenLoai(pet);
        final String dong = DatTenPet.moTa(pet.dongMa, pet.dongPhanNghin);
        return "- Tên: " + pet.tenDat + (loai.isEmpty() ? "" : " (" + loai + ")")
                + (dong.isEmpty() ? "" : "\n- Thẻ tên: " + dong + " (chỉ khi đi theo)") + "\n";
    }

    /** Phan mo rong cua -94 cho pet dang di theo (TienHoaPokemon.taoPokePet). */
    public static void ghiMoRong(final java.io.DataOutputStream dos, final Char ch) throws java.io.IOException {
        final Pet pet = ch == null ? null : ch.petUsing;
        DatTenPet.ghiTenPet(dos, pet == null ? "" : tenHien(pet), daDatTen(pet));
    }

    // ================================================================== CSDL

    private static final String[] TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS tob_pet_dat_ten (id_pet INT NOT NULL, id_char INT NOT NULL, ten VARCHAR(40) NOT NULL,"
            + " chi_so VARCHAR(16) NOT NULL DEFAULT '', gia_tri INT NOT NULL DEFAULT 0, so_lan INT NOT NULL DEFAULT 0,"
            + " cap_nhat DATETIME NULL, PRIMARY KEY (id_pet), KEY idx_pet_dat_ten_char (id_char)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_log_dat_ten (id BIGINT NOT NULL AUTO_INCREMENT, luc DATETIME NOT NULL,"
            + " id_char INT NOT NULL, charname VARCHAR(30) NOT NULL DEFAULT '', id_pet INT NOT NULL, dex INT NOT NULL DEFAULT 0,"
            + " ten_cu VARCHAR(40) NOT NULL DEFAULT '', dong_cu VARCHAR(32) NOT NULL DEFAULT '', ten_moi VARCHAR(40) NOT NULL DEFAULT '',"
            + " dong_moi VARCHAR(32) NOT NULL DEFAULT '', id_item INT NOT NULL DEFAULT 0, ket_qua VARCHAR(32) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (id), KEY idx_log_dat_ten_char (id_char), KEY idx_log_dat_ten_luc (luc)) DEFAULT CHARSET=utf8",
    };

    /** Luc khoi dong (TeamServer, cung cho voi DatabaseLauDai.taoBang). */
    public static void taoBang() {
        for (final String sql : TAO_BANG) {
            ghi(sql);
        }
    }

    static int ghi(final String sql, final Object... thamSo) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < thamSo.length; i++) {
                pre.setObject(i + 1, thamSo[i]);
            }
            return pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[DAT TEN PET] LOI SQL: " + e);
            return -1;
        } finally {
            dong(null, pre);
            if (conn != null) {
                try {
                    Database.connPool.free(conn);
                } catch (final Exception ignored) {
                }
            }
        }
    }

    static void dong(final ResultSet rs, final PreparedStatement pre) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (pre != null) {
                pre.close();
            }
        } catch (final Exception ignored) {
        }
    }

    /** Database.getCharPet (cung ket noi): nap ten + dong cho cac pet cua p. Pet doi chu (khong qua duong nay) khong co dong. */
    public static void napChoChar(final Char p, final Connection conn) {
        if (p == null || conn == null || p.pet == null || p.pet.isEmpty()) {
            return;
        }
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            pre = conn.prepareStatement("SELECT id_pet, ten, chi_so, gia_tri, so_lan FROM tob_pet_dat_ten WHERE id_char = ?");
            pre.setInt(1, p.charDBID);
            rs = pre.executeQuery();
            while (rs.next()) {
                final int idPet = rs.getInt(1);
                for (final Pet pet : p.pet) {
                    if (pet != null && pet.idDB == idPet && pet.ownerid == p.charDBID) {
                        pet.tenDat = rs.getString(2);
                        final String ma = rs.getString(3);
                        pet.dongMa = DatTenPet.maHopLe(ma) ? ma : null;
                        pet.dongPhanNghin = Math.max(0, rs.getInt(4));
                        pet.datTenSoLan = rs.getInt(5);
                    }
                }
            }
        } catch (final Exception e) {
            // chua co bang (CSDL cu chua chay migration): pet khong ten, khong dong
        } finally {
            dong(rs, pre);
        }
    }

    /** Bang dong (SQL). Loi / thieu bang -&gt; rong (khong dat ten duoc). */
    static List<DatTenPet.Dong> napBang() {
        final List<DatTenPet.Dong> tho = new ArrayList<DatTenPet.Dong>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id, chi_so, trong_so, he_so FROM data_dong_the_ten WHERE bat = 1 ORDER BY id");
            rs = pre.executeQuery();
            while (rs.next()) {
                tho.add(new DatTenPet.Dong(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
            }
        } catch (final Exception e) {
            System.out.println("[DAT TEN PET] chua co data_dong_the_ten: " + e);
        } finally {
            dong(rs, pre);
            if (conn != null) {
                try {
                    Database.connPool.free(conn);
                } catch (final Exception ignored) {
                }
            }
        }
        return DatTenPet.locBang(tho);
    }

    /** Tu cam (SQL). null = loi doc (dong cua: khong nhan ten nao). */
    static List<DatTenPet.TuCam> napTuCam() {
        final List<DatTenPet.TuCam> ds = new ArrayList<DatTenPet.TuCam>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT tu, kieu FROM data_tu_cam_ten WHERE bat = 1");
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new DatTenPet.TuCam(rs.getString(1), rs.getString(2)));
            }
            return ds;
        } catch (final Exception e) {
            System.out.println("[DAT TEN PET] chua co data_tu_cam_ten: " + e);
            return null;
        } finally {
            dong(rs, pre);
            if (conn != null) {
                try {
                    Database.connPool.free(conn);
                } catch (final Exception ignored) {
                }
            }
        }
    }

    static boolean luu(final Pet pet, final Char p, final DatTenPet.TrangThai t) {
        return ghi("INSERT INTO tob_pet_dat_ten (id_pet, id_char, ten, chi_so, gia_tri, so_lan, cap_nhat) VALUES (?, ?, ?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE id_char = VALUES(id_char), ten = VALUES(ten), chi_so = VALUES(chi_so),"
                + " gia_tri = VALUES(gia_tri), so_lan = VALUES(so_lan), cap_nhat = VALUES(cap_nhat)",
                Integer.valueOf(pet.idDB), Integer.valueOf(p.charDBID), t.ten, t.ma, Integer.valueOf(t.phanNghin),
                Integer.valueOf(t.soLan), new Timestamp(System.currentTimeMillis())) > 0;
    }

    /** Nhat ky kinh te: MOI lan the bi tru (ok / hoan the). Cot theo DatTenPet.COT_NHAT_KY. */
    static void ghiNhatKy(final Char p, final Pet pet, final DatTenPet.TrangThai cu, final DatTenPet.TrangThai moi,
            final int idMau, final String ketQua) {
        final Object[] d = DatTenPet.dongNhatKy(p.charDBID, p.charname, pet == null ? 0 : pet.idDB, dexPet(pet), cu, moi,
                idMau, ketQua);
        final Object[] thamSo = new Object[d.length + 1];
        thamSo[0] = new Timestamp(System.currentTimeMillis());
        System.arraycopy(d, 0, thamSo, 1, d.length);
        ghi("INSERT INTO tob_log_dat_ten (luc, " + String.join(", ", DatTenPet.COT_NHAT_KY) + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                thamSo);
        com.htbb.log.Nhat.thongTin("KINH_TE", "dat ten pet " + p.charname + " pet " + (pet == null ? 0 : pet.idDB) + " '"
                + (moi == null ? "" : moi.ten) + "' " + (moi == null ? "" : moi.ma + ":" + moi.phanNghin) + " " + ketQua);
    }

    static int dexPet(final Pet pet) {
        return pet != null && pet.itemPet != null && pet.itemPet.getTemplate() != null ? PetPokemon.dex(pet.itemPet.getTemplate().id) : 0;
    }

    // ================================================================== luong dung the

    /** Pet dat ten duoc: da luu CSDL, vinh vien, chua het han. */
    static boolean datTenDuoc(final Pet pet) {
        return pet != null && pet.idDB > 0 && pet.totalMinute == 0 && !pet.ispetthoihan && pet.getTimeExpire() > 0;
    }

    /** VatPhamDungLauDai.dung (hieu_ung dat_ten): mo menu chon pet. The CHUA bi tru. */
    public static void batDau(final Char p, final Item item) {
        if (!bat()) {
            DungVatPham.loi(p, "Thẻ đặt tên đang tạm khoá.");
            return;
        }
        final List<Integer> ds = new ArrayList<Integer>();
        final List<String> ten = new ArrayList<String>();
        for (final Pet pet : p.pet == null ? new java.util.Vector<Pet>() : p.pet) {
            if (datTenDuoc(pet) && ds.size() < 100) {
                ds.add(Integer.valueOf(pet.idDB));
                final String loai = tenLoai(pet);
                ten.add((daDatTen(pet) ? pet.tenDat + " (" + loai + ")" : loai) + (pet == p.petUsing ? " - đang đi theo" : ""));
            }
        }
        if (ds.isEmpty()) {
            DungVatPham.loi(p, "Chưa có Pokémon đi theo (vĩnh viễn) nào để đặt tên.");
            return;
        }
        PHIEN.put(Integer.valueOf(p.charDBID), new Phien(item.id, item.getTemplate().id, ds));
        p.sendMessage(MessageCreator.createMsgMenu(ten.toArray(new String[0]), NPC_CHON_PET, 0));
    }

    /** MapDoMenu: true neu la menu chon pet. */
    public static boolean xuLyMenu(final Char p, final int idNpc, final int idMenu, final int chon) {
        if (idNpc != NPC_CHON_PET || p == null) {
            return false;
        }
        final Phien ph = PHIEN.get(Integer.valueOf(p.charDBID));
        if (ph == null || chon < 0 || chon >= ph.pet.size()) {
            return true;
        }
        ph.chon = ph.pet.get(chon).intValue();
        final Pet pet = timPet(p, ph.chon);
        if (pet == null) {
            PHIEN.remove(Integer.valueOf(p.charDBID));
            return true;
        }
        moONhap(p, pet, null);
        return true;
    }

    static void moONhap(final Char p, final Pet pet, final String loi) {
        final int min = so(DatTenPet.K_DAI_MIN, DatTenPet.DAI_MIN_MAC_DINH);
        final int max = so(DatTenPet.K_DAI_MAX, DatTenPet.DAI_MAX_MAC_DINH);
        p.sendMessage(MessageCreator.createMsgInputText(INPUT_TEN, 0, (loi == null ? "" : loi + " ")
                + "Đặt tên cho " + tenLoai(pet) + " (" + min + "-" + max + " ký tự)", 0));
    }

    static Pet timPet(final Char p, final int idDB) {
        if (p.pet == null) {
            return null;
        }
        for (final Pet pet : p.pet) {
            if (pet != null && pet.idDB == idDB && pet.ownerid == p.charDBID) {
                return pet;
            }
        }
        return null;
    }

    static Item timThe(final Char p, final Phien ph) {
        for (final Item it : p.iItems) {
            if (it != null && it.id == ph.idItem && it.getTemplate() != null && it.getTemplate().id == ph.idMau) {
                return it;
            }
        }
        // tui doi (sap xep / dung the khac): lay the bat ky cung mau
        for (final Item it : p.iItems) {
            if (it != null && it.getTemplate() != null && it.getTemplate().id == ph.idMau) {
                return it;
            }
        }
        return null;
    }

    /**
     * MapNhapText (chuoi GOC, chua ha chu thuong): true neu la o nhap ten pet. Ten sai -&gt; mo lai o nhap (the chua tru).
     */
    public static boolean xuLyNhap(final Char p, final int idActor, final String tho) {
        if (idActor != INPUT_TEN || p == null) {
            return false;
        }
        final Phien ph = PHIEN.get(Integer.valueOf(p.charDBID));
        if (ph == null || ph.chon == 0) {
            return true;
        }
        final Pet pet = timPet(p, ph.chon);
        if (pet == null || !datTenDuoc(pet)) {
            PHIEN.remove(Integer.valueOf(p.charDBID));
            baoKetQua(p, false, "Không đặt tên được", "Pokémon này không còn trong túi.");
            return true;
        }
        if (!bat()) {
            PHIEN.remove(Integer.valueOf(p.charDBID));
            baoKetQua(p, false, "Không đặt tên được", "Thẻ đặt tên đang tạm khoá.");
            return true;
        }
        final String ten = DatTenPet.chuanHoa(tho);
        final List<DatTenPet.TuCam> tuCam = napTuCam();
        if (tuCam == null) {
            PHIEN.remove(Integer.valueOf(p.charDBID));
            baoKetQua(p, false, "Không đặt tên được", "Máy chủ chưa sẵn sàng, thẻ vẫn còn trong túi.");
            return true;
        }
        final String loi = DatTenPet.kiemTen(ten, so(DatTenPet.K_DAI_MIN, DatTenPet.DAI_MIN_MAC_DINH),
                so(DatTenPet.K_DAI_MAX, DatTenPet.DAI_MAX_MAC_DINH), tuCam);
        if (loi != null) {
            moONhap(p, pet, loi); // giu phien, cho nhap lai
            return true;
        }
        PHIEN.remove(Integer.valueOf(p.charDBID));
        datTen(p, pet, ph, ten);
        return true;
    }

    /** Tru the TRUOC, quay, luu; luu loi -&gt; hoan the. */
    static void datTen(final Char p, final Pet pet, final Phien ph, final String ten) {
        final List<DatTenPet.Dong> bang = napBang();
        if (bang.isEmpty()) {
            baoKetQua(p, false, "Không đặt tên được", "Thẻ đặt tên đang tạm khoá, thẻ vẫn còn trong túi.");
            return;
        }
        final DatTenPet.TrangThai cu = trangThai(pet);
        synchronized (p) {
            final Item the = timThe(p, ph);
            if (the == null || !p.iItems.remove(the)) {
                baoKetQua(p, false, "Không đặt tên được", "Thẻ đặt tên không còn trong túi.");
                return;
            }
            p.removeIDItem(the.id);
        }
        final int tran = tranDong();
        final DatTenPet.KetQua kq = DatTenPet.quay(bang, dsSo(DatTenPet.K_MUC, DatTenPet.MUC_MAC_DINH),
                dsSo(DatTenPet.K_TRONG_SO, DatTenPet.TRONG_SO_MAC_DINH), tran, ThreadLocalRandom.current());
        final DatTenPet.TrangThai moi = kq == null ? null : DatTenPet.datLai(cu, ten, kq);
        if (moi == null || !luu(pet, p, moi)) {
            Char.createItemSoLuong(ph.idMau, 1, p); // hoan the (khoa nhu luc nhan)
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            ghiNhatKy(p, pet, cu, moi, ph.idMau, "hoan_the");
            baoKetQua(p, false, "Không đặt tên được", "Lỗi lưu dữ liệu, thẻ đã được trả lại.");
            return;
        }
        pet.tenDat = moi.ten;
        pet.dongMa = moi.ma;
        pet.dongPhanNghin = moi.phanNghin;
        pet.datTenSoLan = moi.soLan;
        ghiNhatKy(p, pet, cu, moi, ph.idMau, "ok");
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Exception ignored) {
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        if (pet == p.petUsing) {
            VatPhamDungLauDai.tinhLaiChiSo(p);
            final Message m = TienHoaPokemon.taoPokePet(p);
            if (m != null) {
                p.sendMessage(m);
            }
            TienHoaPokemon.guiPokePetChoNguoiGan(p);
        }
        final int hl = DatTenPet.hieuLuc(moi.phanNghin, tran, tranTrangTri(), trangTriKhacPhanNghin(p));
        final StringBuilder b = new StringBuilder();
        b.append(tenLoai(pet)).append(" giờ tên là \"").append(moi.ten).append("\".\n");
        b.append("Dòng thẻ tên: ").append(DatTenPet.moTa(moi.ma, moi.phanNghin));
        if (hl < moi.phanNghin) {
            b.append(" (đang tính ").append(hl <= 0 ? "0" : DatTenPet.moTa(moi.ma, hl)).append(" vì chạm trần trang trí)");
        }
        if (cu != null && cu.ma != null) {
            b.append("\nThay dòng cũ: ").append(DatTenPet.moTa(cu.ma, cu.phanNghin)).append(" (không cộng dồn).");
        }
        b.append(pet == p.petUsing ? "\nChỉ tính khi Pokémon này đang đi theo." : "\nCho Pokémon này đi theo để dòng có hiệu lực.");
        b.append("\nPokémon đã đặt tên không giao dịch được.");
        baoKetQua(p, true, "Đặt tên thành công", b.toString());
    }

    /** Hop thoai ket qua: -106 (client moi, the trang kieu Pokemon GO) hoac 37 (client cu / Unity). */
    static void baoKetQua(final Char p, final boolean ok, final String tieuDe, final String noiDung) {
        final int ban = so(DatTenPet.K_BAN_POPUP, DatTenPet.BAN_POPUP_MAC_DINH);
        try {
            if (p.session != null && p.session.banClient >= ban) {
                final Message m = new Message(DatTenPet.LENH_KET_QUA);
                DatTenPet.ghiKetQua(m.dos, ok ? DatTenPet.KQ_OK : DatTenPet.KQ_LOI, tieuDe, noiDung);
                p.sendMessage(m);
                return;
            }
            p.sendMessage(MessageCreator.createServerAlertMessage(tieuDe + "\n" + noiDung, ""));
        } catch (final Exception ignored) {
        }
    }
}
