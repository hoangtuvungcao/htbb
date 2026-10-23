package com.htbb.pokemon;

import java.util.Map;
import java.util.TreeMap;

/**
 * Trang thai dai han cua MOT nhan vat (khi {@link LauDai#bat()}): nang luong huan luyen,
 * tinh thong, pity nang cap, lich dang nhap, ve chuong, pity shiny, giftcode thang, Ham
 * nguc, Elo PvP... Luu thanh MOT chuoi "k=v;k=v" trong tob_char_lau_dai.du_lieu (bang moi,
 * khong dung pInfo cua tob_char - pInfo la varchar(2000) da gan day va do nhieu ham cu ghi).
 *
 * Lop THUAN, co test (test/KiemLauDaiGd2.java). Moi truy cap tu nhieu luong di qua
 * synchronized tren chinh doi tuong.
 */
public final class HoSoLauDai {

    // ---- nang luong huan luyen (THIET_KE 1.3) ----
    /** Ngay (yyyyMMdd) cua bo dem ben duoi. */
    public int nlNgay;
    /** Mili-giay danh quai da dung trong ngay nlNgay. */
    public long nlMsDung;
    /** Phut 100% cua ngay nlNgay (180 + tich luy). */
    public int nlPhut100 = ThongSoLauDai.NANG_LUONG_PHUT_NGAY;
    /** Lan cuoi ghi nhan danh quai (ms) - de cong thoi gian lien tuc. */
    public long nlLanCuoi;

    // ---- tinh thong (1.6) ----
    public int ttDiem;
    public long ttQuy;
    /** 0 = cong tan cong, 1 = cong HP. */
    public int ttLoai;

    // ---- pity nang cap: id item -> so lan truot lien tiep o muc hien tai ----
    public final Map<Integer, Integer> truot = new TreeMap<Integer, Integer>();

    // ---- thuoc KHOA (chong clone 19/10/2026): id thuoc -> so binh mua bang luong khoa, KHONG giao dich duoc ----
    // Luu "x<id>=<so>". Xem real/ThuocKhoa.
    public final Map<Integer, Integer> thuocKhoa = new TreeMap<Integer, Integer>();

    // ---- lich dang nhap 7 ngay (7.1) ----
    public int dnBuoc;
    public int dnNgayCuoi;

    // ---- ve chuong (5.1) ----
    public int veChuong = ThongSoLauDai.VE_CHUONG_BAN_DAU;
    public int veTuanCuoi;

    // ---- shiny (7.3) ----
    public int shinyDem;
    public int shinySo;

    // ---- giftcode (5.4) ----
    public int gcThang;
    public int gcDem;

    // ---- ham ngoc bi an (7.4) ----
    public int hnNgay;
    public int hnTangCao;
    public int hnDaThuong;

    // ---- PvP mua (7.2) ----
    public int pvpMua;
    public int pvpElo = ThangPvp.ELO_BAN_DAU;
    public int pvpThang;
    public int pvpThua;

    // ---- bat kip / quay lai (1.7) ----
    public int ngayDauTien;
    public int ngayGanNhat;
    public int quayLaiDenNgay;

    // ---- su pham (7.5) ----
    public int thayId;
    public int moSuPham;

    /** Da chuyen nhiem vu chinh sang chuoi data_quest_moi (1) chua. */
    public int nvChuyen;
    /** Keo Hiem: tuan va so vien da dung (2/tuan). */
    public int khTuan;
    public int khSo;
    /** Do giu dang mang (id template item, 0 = khong). */
    public int doGiu;
    /** Ngoai hinh trang bi: bit o phu kien an + bit 7 an trang phuc (com.htbb.pokemon.NgoaiHinh). */
    public int ngoaiHinhAn;
    /**
     * 0.9.8 trang chieu (o nhanh) tung nhan vat - client 908 dong bo (goi -103 loai 1): 3 trang x 5 o, moi o 12 bit
     * ((kieu + 1) 3 bit | gia tri 8 bit | buff 1 bit), trang dang dung, so trang mo them (0 = 2 trang mac dinh, 1 = 3 trang).
     */
    public long oNhanh1, oNhanh2, oNhanh3;
    public int trangChieu, trangChieuThem;
    /** Gym: bit huy hieu da co (16 bit) va tuan danh lai gan nhat moi Gym bit. */
    public int gymHuyHieu;
    public int gymTuan;
    public int gymTuanBit;

    /** Vi tri nhiem vu chinh CU (chuoi data_quest) + 1 truoc khi chuyen; 0 = khong co. */
    public int nvCu;
    /**
     * Huong dan tan thu: 1 = da hien menu moi Lam / Bo qua (hoac nhan vat cu da vao game truoc ban sua) - khong bao gio
     * moi lai, ke ca sau khi dang nhap lai / khoi dong lai may chu (real.HuongDanTanThuGame.moiNeuCan).
     */
    public int hdMoi;

    // ---- tang toc (TangToc, THIET_KE 1.8): tran diem ngay/tuan chung ban cao cap + ban khoa ----
    public int tgNgay;
    public int tgDiemNgay;
    public int tgTuan;
    public int tgDiemTuan;
    /** Ve EXP dang chay: he so (150/200) va ms danh quai con lai. */
    public int tgVeHeSo;
    public long tgVeMs;
    public int tgVePhutNgay;
    /** Bua the luc: so lan hom nay, ngay cho cong (dung truoc lan danh quai dau), ngay da cong vao nlPhut100. */
    public int tgBuaNgay;
    public int tgBuaChoNgay;
    public int tgBuaCongNgay;
    /** Uy thac: ms da dat truoc (chua chay), ms da nhan hom nay, luc thoat game gan nhat (ms). */
    public long tgUtMs;
    public long tgUtMsNgay;
    public long tgThoat;

    // ---- tu dong danh (TuDong): ngay va ms da dung hom do (tran gio/ngay, chi tinh luc online) ----
    public int tdNgay;
    public long tdMsDung;

    /** Ban sao da doi so voi lan luu truoc (de khong ghi CSDL khi khong doi gi). */
    public boolean ban;

    public synchronized String maHoa() {
        final StringBuilder b = new StringBuilder();
        dat(b, "nn", nlNgay);
        dat(b, "nm", nlMsDung);
        dat(b, "np", nlPhut100);
        dat(b, "td", ttDiem);
        dat(b, "tq", ttQuy);
        dat(b, "tl", ttLoai);
        dat(b, "db", dnBuoc);
        dat(b, "dn", dnNgayCuoi);
        dat(b, "vc", veChuong);
        dat(b, "vt", veTuanCuoi);
        dat(b, "sd", shinyDem);
        dat(b, "ss", shinySo);
        dat(b, "gt", gcThang);
        dat(b, "gd", gcDem);
        dat(b, "hn", hnNgay);
        dat(b, "ht", hnTangCao);
        dat(b, "hd", hnDaThuong);
        dat(b, "pm", pvpMua);
        dat(b, "pe", pvpElo);
        dat(b, "pw", pvpThang);
        dat(b, "pl", pvpThua);
        dat(b, "d1", ngayDauTien);
        dat(b, "d2", ngayGanNhat);
        dat(b, "ql", quayLaiDenNgay);
        dat(b, "th", thayId);
        dat(b, "sp", moSuPham);
        dat(b, "nc", nvChuyen);
        dat(b, "nq", nvCu);
        dat(b, "hm", hdMoi);
        dat(b, "kt", khTuan);
        dat(b, "ks", khSo);
        dat(b, "dg", doGiu);
        dat(b, "nh", ngoaiHinhAn);
        dat(b, "o1", oNhanh1);
        dat(b, "o2", oNhanh2);
        dat(b, "o3", oNhanh3);
        dat(b, "ot", trangChieu);
        dat(b, "tc", trangChieuThem);
        dat(b, "gh", gymHuyHieu);
        dat(b, "gw", gymTuan);
        dat(b, "gb", gymTuanBit);
        dat(b, "z1", tgNgay);
        dat(b, "z2", tgDiemNgay);
        dat(b, "z3", tgTuan);
        dat(b, "z4", tgDiemTuan);
        dat(b, "z5", tgVeHeSo);
        dat(b, "z6", tgVeMs);
        dat(b, "z7", tgVePhutNgay);
        dat(b, "z8", tgBuaNgay);
        dat(b, "z9", tgBuaChoNgay);
        dat(b, "za", tgBuaCongNgay);
        dat(b, "zb", tgUtMs);
        dat(b, "zc", tgUtMsNgay);
        dat(b, "zd", tgThoat);
        dat(b, "a1", tdNgay);
        dat(b, "a2", tdMsDung);
        for (final Map.Entry<Integer, Integer> e : truot.entrySet()) {
            if (e.getValue().intValue() > 0) {
                dat(b, "t" + e.getKey(), e.getValue().intValue());
            }
        }
        for (final Map.Entry<Integer, Integer> e : thuocKhoa.entrySet()) {
            if (e.getValue().intValue() > 0) {
                dat(b, "x" + e.getKey(), e.getValue().intValue());
            }
        }
        return b.toString();
    }

    private static void dat(final StringBuilder b, final String k, final long v) {
        if (v == 0L) {
            return;
        }
        if (b.length() > 0) {
            b.append(';');
        }
        b.append(k).append('=').append(v);
    }

    /** Doc chuoi da luu; khoa la/so hong bi bo qua (khong nem loi). */
    public static HoSoLauDai giaiMa(final String s) {
        final HoSoLauDai h = new HoSoLauDai();
        if (s == null || s.isEmpty()) {
            return h;
        }
        // Gia tri mac dinh khac 0 phai dat ve 0 neu chuoi co ghi (chuoi chi ghi khac 0, nen
        // thieu khoa = 0). Ngoai le: cac khoa mac dinh khac 0 khi CHUA bao gio luu.
        h.nlPhut100 = ThongSoLauDai.NANG_LUONG_PHUT_NGAY;
        h.veChuong = 0;
        h.pvpElo = ThangPvp.ELO_BAN_DAU;
        boolean coVe = false;
        for (final String cap : s.split(";")) {
            final int i = cap.indexOf('=');
            if (i <= 0) {
                continue;
            }
            final String k = cap.substring(0, i);
            final long v;
            try {
                v = Long.parseLong(cap.substring(i + 1).trim());
            } catch (final NumberFormatException e) {
                continue;
            }
            final int iv = (int) v;
            if (k.equals("nn")) {
                h.nlNgay = iv;
            } else if (k.equals("nm")) {
                h.nlMsDung = v;
            } else if (k.equals("np")) {
                h.nlPhut100 = iv;
            } else if (k.equals("td")) {
                h.ttDiem = iv;
            } else if (k.equals("tq")) {
                h.ttQuy = v;
            } else if (k.equals("tl")) {
                h.ttLoai = iv;
            } else if (k.equals("db")) {
                h.dnBuoc = iv;
            } else if (k.equals("dn")) {
                h.dnNgayCuoi = iv;
            } else if (k.equals("vc")) {
                h.veChuong = iv;
                coVe = true;
            } else if (k.equals("vt")) {
                h.veTuanCuoi = iv;
            } else if (k.equals("sd")) {
                h.shinyDem = iv;
            } else if (k.equals("ss")) {
                h.shinySo = iv;
            } else if (k.equals("gt")) {
                h.gcThang = iv;
            } else if (k.equals("gd")) {
                h.gcDem = iv;
            } else if (k.equals("hn")) {
                h.hnNgay = iv;
            } else if (k.equals("ht")) {
                h.hnTangCao = iv;
            } else if (k.equals("hd")) {
                h.hnDaThuong = iv;
            } else if (k.equals("pm")) {
                h.pvpMua = iv;
            } else if (k.equals("pe")) {
                h.pvpElo = iv;
            } else if (k.equals("pw")) {
                h.pvpThang = iv;
            } else if (k.equals("pl")) {
                h.pvpThua = iv;
            } else if (k.equals("d1")) {
                h.ngayDauTien = iv;
            } else if (k.equals("d2")) {
                h.ngayGanNhat = iv;
            } else if (k.equals("ql")) {
                h.quayLaiDenNgay = iv;
            } else if (k.equals("th")) {
                h.thayId = iv;
            } else if (k.equals("sp")) {
                h.moSuPham = iv;
            } else if (k.equals("nc")) {
                h.nvChuyen = iv;
            } else if (k.equals("nq")) {
                h.nvCu = iv;
            } else if (k.equals("hm")) {
                h.hdMoi = iv;
            } else if (k.equals("kt")) {
                h.khTuan = iv;
            } else if (k.equals("ks")) {
                h.khSo = iv;
            } else if (k.equals("dg")) {
                h.doGiu = iv;
            } else if (k.equals("nh")) {
                h.ngoaiHinhAn = iv;
            } else if (k.equals("o1")) {
                h.oNhanh1 = v;
            } else if (k.equals("o2")) {
                h.oNhanh2 = v;
            } else if (k.equals("o3")) {
                h.oNhanh3 = v;
            } else if (k.equals("ot")) {
                h.trangChieu = iv;
            } else if (k.equals("tc")) {
                h.trangChieuThem = iv;
            } else if (k.equals("gh")) {
                h.gymHuyHieu = iv;
            } else if (k.equals("gw")) {
                h.gymTuan = iv;
            } else if (k.equals("gb")) {
                h.gymTuanBit = iv;
            } else if (k.equals("z1")) {
                h.tgNgay = iv;
            } else if (k.equals("z2")) {
                h.tgDiemNgay = iv;
            } else if (k.equals("z3")) {
                h.tgTuan = iv;
            } else if (k.equals("z4")) {
                h.tgDiemTuan = iv;
            } else if (k.equals("z5")) {
                h.tgVeHeSo = iv;
            } else if (k.equals("z6")) {
                h.tgVeMs = v;
            } else if (k.equals("z7")) {
                h.tgVePhutNgay = iv;
            } else if (k.equals("z8")) {
                h.tgBuaNgay = iv;
            } else if (k.equals("z9")) {
                h.tgBuaChoNgay = iv;
            } else if (k.equals("za")) {
                h.tgBuaCongNgay = iv;
            } else if (k.equals("zb")) {
                h.tgUtMs = v;
            } else if (k.equals("zc")) {
                h.tgUtMsNgay = v;
            } else if (k.equals("zd")) {
                h.tgThoat = v;
            } else if (k.equals("a1")) {
                h.tdNgay = iv;
            } else if (k.equals("a2")) {
                h.tdMsDung = v;
            } else if (k.length() > 1 && k.charAt(0) == 'x') {
                try {
                    h.thuocKhoa.put(Integer.valueOf(k.substring(1)), Integer.valueOf(iv));
                } catch (final NumberFormatException e) {
                    // bo qua
                }
            } else if (k.length() > 1 && k.charAt(0) == 't') {
                try {
                    h.truot.put(Integer.valueOf(k.substring(1)), Integer.valueOf(iv));
                } catch (final NumberFormatException e) {
                    // bo qua
                }
            }
        }
        if (!coVe) {
            // chuoi cu khong co "vc" nghia la ve = 0 (da dung het) - maHoa bo qua so 0.
            h.veChuong = 0;
        }
        return h;
    }

    // ------------------------------------------------------------------ thuoc khoa

    /** So binh thuoc {@code id} dang khoa (khong giao dich duoc). */
    public synchronized int thuocKhoa(final int id) {
        final Integer v = thuocKhoa.get(Integer.valueOf(id));
        return v == null ? 0 : v.intValue();
    }

    /** Dat so binh khoa (<= 0 thi xoa). */
    public synchronized void datThuocKhoa(final int id, final int n) {
        if (n <= 0) {
            if (thuocKhoa.remove(Integer.valueOf(id)) != null) {
                ban = true;
            }
            return;
        }
        final Integer cu = thuocKhoa.put(Integer.valueOf(id), Integer.valueOf(n));
        if (cu == null || cu.intValue() != n) {
            ban = true;
        }
    }

    // ------------------------------------------------------------------ pity nang cap

    public synchronized int truot(final int idItem) {
        final Integer v = truot.get(Integer.valueOf(idItem));
        return v == null ? 0 : v.intValue();
    }

    public synchronized void datTruot(final int idItem, final int n) {
        if (n <= 0) {
            truot.remove(Integer.valueOf(idItem));
        } else {
            truot.put(Integer.valueOf(idItem), Integer.valueOf(n));
        }
        // giu chuoi luu gon: toi da 40 mon dang nang do dang
        while (truot.size() > 40) {
            truot.remove(((TreeMap<Integer, Integer>) truot).firstKey());
        }
        ban = true;
    }
}
