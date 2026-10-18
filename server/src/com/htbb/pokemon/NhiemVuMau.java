package com.htbb.pokemon;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mau nhiem vu cho ENGINE nhiem vu tong quat (GD2b, du_lieu/NHIEM_VU_THIET_KE.md muc 5). Lop
 * THUAN: du lieu mot dong (data_quest_lau_dai + data_nhiem_vu_noi_dung) va cac luat chung (gating,
 * bang ngay, gioi han tuan, khop su kien, hoi thoai). Engine choi o real.NhiemVuEngine.
 *
 * Chuoi chinh (chinh / huong_dan) KHONG o day: van choi tren engine cu (data_quest_moi, cot du_phong).
 */
public final class NhiemVuMau {

    public static final int KHE_PHU = 3;
    public static final int NGAY_RUT = 5;
    public static final int NGAY_THUONG = ThongSoLauDai.GIOI_HAN_NGAY;
    public static final int TUAN_TOI_DA = ThongSoLauDai.GIOI_HAN_TUAN;
    public static final int DS_TOI_DA = 20;

    public String ma = "";
    public String loai = "";
    public String ten = "";
    public String tenNpc = "";
    public int cap;
    public int ngayMo;
    public String truoc = "";
    public String kieu = "";
    public String mucTieuId = "";
    public int soLuong = 1;
    public String lapLai = "";
    public long exp;
    public long xu;
    public int luongKhoa;
    public int luong;
    public String vatPham = "";
    public boolean an;
    public String dieuKienMo = "";
    public Map<String, String> dieuKien = new HashMap<String, String>();
    public int mapId = -1;
    public int toaX = -1;
    public int toaY = -1;
    public String dapAn = "";
    public int phut;
    public String nhanh = "";
    public String thuongKhac = "";
    public String hoiThoaiBatDau = "";
    public String hoiThoaiDangLam = "";
    public String hoiThoaiXong = "";
    public String mucTieuText = "";
    public String quaiRoi = "";

    // ------------------------------------------------------------------ phan loai

    /** Nhom do engine moi quan ly (khong phai chuoi chinh). */
    public static boolean engineQuanLy(final String loai) {
        return !"chinh".equals(loai) && !"huong_dan".equals(loai);
    }

    public boolean laNgay() {
        return ma.startsWith("N-");
    }

    /** T- / TV- / TD-: dung chung gioi han 2/tuan; chuoi TV tinh mot lan. */
    public boolean laTuan() {
        return ma.startsWith("T-") || ma.startsWith("TV-") || ma.startsWith("TD-");
    }

    /** Khoa tinh vao gioi han tuan: TV-07.2 -> "TV-07" (ca chuoi 1 lan), con lai = ma. */
    public String khoaTuan() {
        if (ma.startsWith("TV-")) {
            final int i = ma.indexOf('.');
            return i > 0 ? ma.substring(0, i) : ma;
        }
        return ma;
    }

    public boolean laThanhTuu() {
        return kieu.startsWith("thanh_tuu");
    }

    public boolean laSuKien() {
        return ma.startsWith("SK-");
    }

    /** Tuan cua SK-<tuan>.x; -1 neu khong phai. */
    public int tuanSuKien() {
        if (!laSuKien()) {
            return -1;
        }
        try {
            final String s = ma.substring(3);
            final int i = s.indexOf('.');
            return Integer.parseInt(i > 0 ? s.substring(0, i) : s);
        } catch (final RuntimeException e) {
            return -1;
        }
    }

    /** Nhiem vu phu "chiem khe" (toi da 3 cung luc). */
    public boolean chiemKhe() {
        return !chuoiNam2 && !laNgay() && !laTuan() && !laThanhTuu() && !laSuKien();
    }

    // ------------------------------------------------------------------ chuoi chinh nam 2 (Hoenn)

    /**
     * Chuoi chinh nam 2 (chinh / huong_dan co dong_co_tuong_thich = 0: khong bieu dien duoc bang data_quest cu)
     * do ENGINE chay - nhung CHI khi da co ban do (map_id cua nhiem vu ton tai tren may chu; doi ban do Hoenn noi
     * map_id) va tran cap mua >= {@link #CAP_MUA_NAM_2}. Khong tu tao ban do. Tran cap theo mua TAT (mac dinh)
     * thi CapDoLauDai.tranCap = 100 (dieu kien mua luon qua): chuoi nam 2 mo theo CAP (cap_toi_thieu >= 91) +
     * COT TRUYEN (C53.1 truoc = C52.7, ket nam 1 - sau Nha vo dich C50.7).
     */
    public boolean chuoiNam2;
    public static final int CAP_MUA_NAM_2 = 91;

    public static boolean laChuoiNam2(final String loai, final int dongCoTuongThich) {
        return ("chinh".equals(loai) || "huong_dan".equals(loai)) && dongCoTuongThich == 0;
    }

    /** null = chay duoc; nguoc lai ly do. {@code coMap}: map_id >= 0 va ban do co tren may chu. */
    public String kiemNam2(final int tranCapMua, final boolean coMap) {
        if (!chuoiNam2) {
            return null;
        }
        if (tranCapMua < CAP_MUA_NAM_2) {
            return "Cốt truyện năm 2 mở khi trần cấp mùa đạt " + CAP_MUA_NAM_2;
        }
        if ("dan_y".equals(kieu) || mapId < 0 || !coMap) {
            return "Bản đồ Hoenn của nhiệm vụ này chưa mở";
        }
        return null;
    }

    /** Bac bang ngay theo cap: 1-19 -> 1, 20-39 -> 2, 40-59 -> 3, 60-79 -> 4, 80+ -> 5. */
    public static int bacNgay(final int cap) {
        if (cap < 20) {
            return 1;
        }
        if (cap < 40) {
            return 2;
        }
        if (cap < 60) {
            return 3;
        }
        return cap < 80 ? 4 : 5;
    }

    // ------------------------------------------------------------------ gating

    /**
     * Nhan duoc khong (cap, ngay mo, nhiem vu truoc, an). {@code daXong}: ma da hoan thanh.
     * @return null neu duoc, nguoc lai ly do
     */
    public String kiemNhan(final int capNguoi, final int ngayHienTai, final java.util.Set<String> daXong, final boolean moAn) {
        if (capNguoi < cap) {
            return "Cần đạt cấp " + cap;
        }
        if (ngayMo > ngayHienTai) {
            return "Mở sau " + (ngayMo - ngayHienTai) + " ngày";
        }
        if (truoc != null && !truoc.isEmpty()) {
            for (final String t : truoc.split("[,;]")) {
                if (!t.trim().isEmpty() && !daXong.contains(t.trim())) {
                    return "Cần hoàn thành nhiệm vụ trước đó";
                }
            }
        }
        if (an && !moAn) {
            return "Nhiệm vụ này chưa mở";
        }
        return null;
    }

    /**
     * Dieu kien mo cua nhiem vu an (chu tu do). Hieu duoc: "dem" / "ban đêm", "mưa", "tuyết", "nắng";
     * khong hieu thi KHONG mo (an tiep) - engine se bo sung khi co dang du lieu chuan.
     */
    public static boolean moAn(final String dieuKienMo, final int gio, final String thoiTiet) {
        if (dieuKienMo == null || dieuKienMo.trim().isEmpty()) {
            return true;
        }
        final String d = chuanHoa(dieuKienMo);
        boolean hieu = false;
        if (d.contains("dem")) {
            hieu = true;
            if (!(gio >= 19 || gio < 6)) {
                return false;
            }
        }
        for (final String tt : new String[]{"mua", "tuyet", "nang", "bao cat", "suong mu"}) {
            if (d.contains(tt)) {
                hieu = true;
                if (thoiTiet == null || !chuanHoa(thoiTiet).contains(tt)) {
                    return false;
                }
            }
        }
        return hieu;
    }

    // ------------------------------------------------------------------ bang ngay

    /** Rut {@code n} ma tu danh sach theo hat giong (id nhan vat + ngay). Tat dinh. */
    public static List<String> rutNgay(final List<String> ds, final long hatGiong, final int n) {
        final List<String> tron = new ArrayList<String>(ds);
        Collections.sort(tron);
        long s = hatGiong;
        for (int i = tron.size() - 1; i > 0; i--) {
            s = CuonHut.tiep(s);
            final int j = (int) Math.floorMod(s, (long) (i + 1));
            Collections.swap(tron, i, j);
        }
        return tron.subList(0, Math.min(n, tron.size()));
    }

    // ------------------------------------------------------------------ khop su kien

    /**
     * Su kien (kieu, tham so) co tinh cho mau nay khong. muc_tieu_id rong = bat ky (tru pokedex_shiny: bat buoc
     * so dex). Kieu Pokedex (gap/ha/shiny/quan_sat) so theo SO DEX cua loai. thanh_tuu_<nhom>
     * khop su kien "thanh_tuu_<nhom>".
     */
    public boolean khop(final String kieuSuKien, final String thamSo) {
        if (!kieu.equals(kieuSuKien)) {
            return false;
        }
        final boolean rong = mucTieuId == null || mucTieuId.isEmpty();
        if (rong && "pokedex_shiny".equals(kieu)) {
            return false; // shiny phai dung loai muc tieu (so dex); du lieu thieu loai thi khong tinh
        }
        if (rong || "0".equals(mucTieuId) && laThanhTuu()) {
            return true;
        }
        if (thamSo == null) {
            return false;
        }
        for (final String id : mucTieuId.split("[,|]")) {
            if (id.trim().equals(thamSo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Toa do o "x;y" (dang chuan, sinh boi tools/pokemon/sinh_nhiem_vu.py) hoac "x,y".
     * @return {x, y} (>= 0) hoac null neu rong / hong
     */
    public static int[] docToaDo(final String s) {
        if (s == null) {
            return null;
        }
        final String[] p = s.trim().split("\\s*[;,]\\s*");
        if (p.length != 2) {
            return null;
        }
        try {
            final int x = Integer.parseInt(p[0].trim());
            final int y = Integer.parseInt(p[1].trim());
            return x >= 0 && y >= 0 ? new int[]{x, y} : null;
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    /** Dang dung trong ban kinh (o, mac dinh 3) cua toa do mau (x, y theo o). */
    public boolean trongToaDo(final int mapNguoi, final int oX, final int oY) {
        if (mapId >= 0 && mapNguoi != mapId) {
            return false;
        }
        if (toaX < 0 || toaY < 0) {
            return mapId >= 0;
        }
        int r = 3;
        try {
            r = Integer.parseInt(dieuKien.getOrDefault("ban_kinh_o", "3"));
        } catch (final NumberFormatException ignored) {
        }
        return Math.abs(oX - toaX) <= r && Math.abs(oY - toaY) <= r;
    }

    // ------------------------------------------------------------------ hoi thoai / cau do

    /** Thay {da} bang tien do, cat theo "|" thanh dong, "Bạn:" giu nguyen. */
    public static String hoiThoai(final String s, final int tienDo, final int tong) {
        if (s == null) {
            return "";
        }
        return s.replace("{da}", String.valueOf(tienDo)).replace("{can}", String.valueOf(tong)).replace("|", "\n");
    }

    /** Bo dau tieng Viet, chu thuong, gop khoang trang - de so dap an cau do. */
    public static String chuanHoa(final String s) {
        if (s == null) {
            return "";
        }
        String t = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        t = t.replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
        return t.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    /** Dap an khop (mot trong cac dap an ngan boi "|"). */
    public boolean dungDapAn(final String chat) {
        if (dapAn == null || dapAn.isEmpty()) {
            return false;
        }
        final String c = chuanHoa(chat);
        for (final String d : dapAn.split("\\|")) {
            if (!d.trim().isEmpty() && c.equals(chuanHoa(d))) {
                return true;
            }
        }
        return false;
    }

    /** Lua chon: "A:<chu>=>danh_hieu:<ten>|B:..." -> {{nhan, thuong}, ...}. */
    public static List<String[]> luaChon(final String s) {
        final List<String[]> kq = new ArrayList<String[]>();
        if (s == null || s.isEmpty()) {
            return kq;
        }
        for (final String phan : s.split("\\|")) {
            final int i = phan.indexOf("=>");
            final String nhan = i > 0 ? phan.substring(0, i) : phan;
            final String thuong = i > 0 ? phan.substring(i + 2) : "";
            final int c = nhan.indexOf(':');
            kq.add(new String[]{c > 0 ? nhan.substring(c + 1).trim() : nhan.trim(), thuong.trim()});
        }
        return kq;
    }

    /** "k=v;k2=v2" hoac "k=v" -> map; "dieu_kien_quan_sat=thoi_tiet=Mưa" giu phan sau dau = dau tien. */
    public static Map<String, String> docDieuKien(final String s) {
        final Map<String, String> m = new HashMap<String, String>();
        if (s == null) {
            return m;
        }
        for (final String phan : s.split(";")) {
            final int i = phan.indexOf('=');
            if (i > 0) {
                m.put(phan.substring(0, i).trim(), phan.substring(i + 1).trim());
            }
        }
        return m;
    }
}
