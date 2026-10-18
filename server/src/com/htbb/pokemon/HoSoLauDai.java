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
    /** Gym: bit huy hieu da co (16 bit) va tuan danh lai gan nhat moi Gym bit. */
    public int gymHuyHieu;
    public int gymTuan;
    public int gymTuanBit;

    /** Vi tri nhiem vu chinh CU (chuoi data_quest) + 1 truoc khi chuyen; 0 = khong co. */
    public int nvCu;

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
        dat(b, "kt", khTuan);
        dat(b, "ks", khSo);
        dat(b, "dg", doGiu);
        dat(b, "gh", gymHuyHieu);
        dat(b, "gw", gymTuan);
        dat(b, "gb", gymTuanBit);
        for (final Map.Entry<Integer, Integer> e : truot.entrySet()) {
            if (e.getValue().intValue() > 0) {
                dat(b, "t" + e.getKey(), e.getValue().intValue());
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
            } else if (k.equals("kt")) {
                h.khTuan = iv;
            } else if (k.equals("ks")) {
                h.khSo = iv;
            } else if (k.equals("dg")) {
                h.doGiu = iv;
            } else if (k.equals("gh")) {
                h.gymHuyHieu = iv;
            } else if (k.equals("gw")) {
                h.gymTuan = iv;
            } else if (k.equals("gb")) {
                h.gymTuanBit = iv;
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
