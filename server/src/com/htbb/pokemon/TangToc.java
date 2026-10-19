package com.htbb.pokemon;

/**
 * VAT PHAM TANG TOC luyen cap (quyet dinh 18/10/2026: KHONG tran cap theo mua; len cap CHAM, nguoi choi
 * dung vat pham de cay / treo nhanh hon). Lop THUAN - test/KiemTangToc.java goi truc tiep; game goi qua
 * real.TangTocGame; mo phong tools/pokemon/MoPhongKinhTe.java (mo-dun TANG TOC) doc chung hang so.
 * Thiet ke: docs/thiet-ke-game.md, docs/van-hanh.md.
 *
 * Nguyen tac:
 *   1. CHI mua THOI GIAN (EXP). Khong xu, khong vat pham, khong da, khong ti le roi, khong chi so.
 *   2. Moi mon co 2 ban CUNG cong dung: cao cap (ban bang luong o Cua hang & Nap) va khoa (kiem bang
 *      choi: nhiem vu tuan, lich dang nhap, ve mua, Kecleon bang luong khoa). Ca hai KHONG giao dich duoc.
 *   3. Ban cao cap va ban khoa dung CHUNG tran: moi mon co "diem tang toc" = so phut EXP o toc do 100% toi
 *      da no cho; tong diem dung moi ngay <= {@link #DIEM_NGAY_TOI_DA}, moi tuan <= {@link #DIEM_TUAN_TOI_DA}.
 *      Tran tuan 210 diem = 3,5 gio EXP/tuan: nguoi choi nhe (1-2 gio/ngay) nhanh hon toi da ~33%, nguoi cay
 *      ~20% (mo phong: nguoi nap toi da / khong nap cung gio choi <= 40%).
 *   4. Cap toi da (100): khong dung duoc (EXP vuot cap thanh tinh thong = suc manh -> khong ban).
 *   5. Bot treo may khong vuot tran: tran tinh theo nhan vat, ngay va tuan, khong theo so vat pham co.
 */
public final class TangToc {

    private TangToc() {
    }

    // ================================================================== mon

    public static final int VE_15 = 0, VE_2 = 1, BUA = 2, UY_THAC = 3, KEO = 4;
    public static final int SO_LOAI = 5;
    /** Ma (cot ma cua nap_cua_hang / data_vat_pham_lau_dai, ban khoa them hau to "_khoa"). */
    public static final String[] MA = {"ve_exp_15", "ve_exp_2", "bua_the_luc", "uy_thac", "keo_hiem"};
    public static final String[] TEN = {"Vé EXP x1,5 (30 phút)", "Vé EXP x2 (30 phút)", "Bùa thể lực (+60 phút)",
        "Phiếu ủy thác luyện tập (1 giờ)", "Kẹo hiếm"};
    /** id data_item ban cao cap (luong) - dai 1250..1257 cua sinh_vat_pham_lau_dai.py. Keo hiem dung 1214 (da co). */
    public static final int[] ID_CAO_CAP = {1250, 1252, 1254, 1256, 1214};
    /** id data_item ban khoa (kiem bang choi). */
    public static final int[] ID_KHOA = {1251, 1253, 1255, 1257, 1214};
    /** Diem tang toc = so phut EXP o toc do 100% ma mon cho TOI DA. */
    public static final int[] DIEM = {15, 30, 45, 12, 30};
    /** Gia ban cao cap (luong; luong bi dot khi mua). */
    public static final int[] GIA_LUONG = {20, 40, 60, 15, 40};
    /** Mua cao cap toi da / tuan / tai khoan (de nguoi choi khong mua thua so voi tran dung). */
    public static final int[] MUA_TUAN_TOI_DA = {14, 7, 4, 17, 2};

    /** Tong diem dung moi ngay / moi tuan (ban cao cap + ban khoa CHUNG). */
    public static final int DIEM_NGAY_TOI_DA = 60;
    public static final int DIEM_TUAN_TOI_DA = 210;

    // ---- ve nhan EXP ----
    public static final int VE_PHUT = 30;
    /** He so EXP (%) cua ve: x1,5 / x2. Nhan SAU nang luong (dung luc met chi duoc 25%/10% x he so). */
    public static final int[] VE_HE_SO = {150, 200};
    /** Tong phut ve kich hoat moi ngay. */
    public static final int VE_PHUT_NGAY_TOI_DA = 60;

    // ---- bua the luc ----
    /** +60 phut toc do 100% cho hom nay (khong tich sang hom sau). */
    public static final int BUA_PHUT = 60;
    public static final int BUA_NGAY_TOI_DA = 1;

    // ---- uy thac luyen tap (treo offline) ----
    /** EXP / gio uy thac = 20% EXP 1 gio danh quai o cap luc thoat. CHI EXP. */
    public static final int UY_THAC_PHAN_TRAM = 20;
    public static final int UY_THAC_GIO_NGAY = 3;
    /** The thang: +1 gio uy thac / ngay (van trong tran diem ngay/tuan). */
    public static final int UY_THAC_GIO_THE_THANG = 1;
    /** Gio uy thac dat truoc (chua chay) toi da. */
    public static final int UY_THAC_GIO_CHO_TOI_DA = 4;

    // ---- keo hiem (da co: VatPhamDungLauDai, 2/tuan) ----
    public static final int KEO_TUAN_TOI_DA = 2;

    static final long PHUT_MS = 60000L;
    static final long GIO_MS = 3600000L;

    /** Loai cua id item (ban cao cap hoac khoa), -1 neu khong phai vat pham tang toc. */
    public static int loaiCuaItem(final int idItem) {
        for (int i = 0; i < SO_LOAI; i++) {
            if (ID_CAO_CAP[i] == idItem || ID_KHOA[i] == idItem) {
                return i;
            }
        }
        return -1;
    }

    /** Loai cua ma cua hang ("ve_exp_15", ...), -1 neu khong co. */
    public static int loaiCuaMa(final String ma) {
        for (int i = 0; i < SO_LOAI; i++) {
            if (MA[i].equals(ma)) {
                return i;
            }
        }
        return -1;
    }

    // ================================================================== tran ngay / tuan

    /** Doi ngay / tuan: dat lai bo dem (goi truoc moi lan doc / ghi). */
    static void lamMoi(final HoSoLauDai h, final int ngay, final int tuan) {
        if (h.tgNgay != ngay) {
            h.tgNgay = ngay;
            h.tgDiemNgay = 0;
            h.tgVePhutNgay = 0;
            h.tgBuaNgay = 0;
            h.tgUtMsNgay = 0L;
            h.ban = true;
        }
        if (h.tgTuan != tuan) {
            h.tgTuan = tuan;
            h.tgDiemTuan = 0;
            h.ban = true;
        }
    }

    /** Diem con dung duoc hom nay (min tran ngay, tran tuan). */
    public static int diemConLai(final HoSoLauDai h, final int ngay, final int tuan) {
        synchronized (h) {
            lamMoi(h, ngay, tuan);
            return Math.max(0, Math.min(DIEM_NGAY_TOI_DA - h.tgDiemNgay, DIEM_TUAN_TOI_DA - h.tgDiemTuan));
        }
    }

    /**
     * Dung duoc mon {@code loai} khong. @return null neu duoc, nguoc lai ly do (tieng Viet cho nguoi choi).
     * {@code cap}/{@code capToiDa}: cap nhan vat va cap cao nhat bang EXP; {@code coTheThang}: the thang con han.
     */
    public static String kiemDung(final HoSoLauDai h, final int loai, final int cap, final int capToiDa,
            final boolean coTheThang, final int ngay, final int tuan) {
        if (loai < 0 || loai >= SO_LOAI) {
            return "Không phải vật phẩm tăng tốc.";
        }
        if (cap >= capToiDa) {
            return "Đã đạt cấp tối đa: vật phẩm tăng tốc không còn tác dụng (EXP vượt cấp không bán bằng tiền).";
        }
        synchronized (h) {
            lamMoi(h, ngay, tuan);
            final int diem = DIEM[loai];
            if (h.tgDiemTuan + diem > DIEM_TUAN_TOI_DA) {
                // Chu "Chua dung duoc" dung dau: cau cu "Da dung 57/60..." doc nhu dung thanh cong (bao 0.9.2)
                return "Chưa dùng được: tuần này đã dùng " + h.tgDiemTuan + "/" + DIEM_TUAN_TOI_DA + " điểm tăng tốc, vật phẩm này cần "
                        + diem + " (mở lại thứ Hai).";
            }
            if (h.tgDiemNgay + diem > DIEM_NGAY_TOI_DA) {
                return "Chưa dùng được: hôm nay đã dùng " + h.tgDiemNgay + "/" + DIEM_NGAY_TOI_DA + " điểm tăng tốc, vật phẩm này cần "
                        + diem + " (mở lại ngày mai).";
            }
            if (loai == VE_15 || loai == VE_2) {
                final int heSo = VE_HE_SO[loai];
                if (h.tgVeMs > 0L && h.tgVeHeSo != heSo) {
                    return "Đang dùng vé EXP x" + (h.tgVeHeSo == 200 ? "2" : "1,5") + " (còn " + (h.tgVeMs / PHUT_MS + 1)
                            + " phút đánh quái). Dùng hết rồi mới dùng loại khác.";
                }
                if (h.tgVePhutNgay + VE_PHUT > VE_PHUT_NGAY_TOI_DA) {
                    return "Mỗi ngày tối đa " + VE_PHUT_NGAY_TOI_DA + " phút vé EXP.";
                }
            } else if (loai == BUA) {
                if (h.tgBuaNgay >= BUA_NGAY_TOI_DA) {
                    return "Mỗi ngày chỉ dùng " + BUA_NGAY_TOI_DA + " Bùa thể lực.";
                }
            } else if (loai == UY_THAC) {
                final long toiDa = (UY_THAC_GIO_NGAY + (coTheThang ? UY_THAC_GIO_THE_THANG : 0)) * GIO_MS;
                if (h.tgUtMs + h.tgUtMsNgay + GIO_MS > toiDa) {
                    return "Mỗi ngày tối đa " + toiDa / GIO_MS + " giờ ủy thác (đang chờ " + h.tgUtMs / PHUT_MS
                            + " phút, đã nhận " + h.tgUtMsNgay / PHUT_MS + " phút).";
                }
                if (h.tgUtMs + GIO_MS > UY_THAC_GIO_CHO_TOI_DA * GIO_MS) {
                    return "Đã đặt trước tối đa " + UY_THAC_GIO_CHO_TOI_DA + " giờ ủy thác.";
                }
            } else if (loai == KEO) {
                if (h.khTuan == tuan && h.khSo >= KEO_TUAN_TOI_DA) {
                    return "Kẹo hiếm: tối đa " + KEO_TUAN_TOI_DA + " viên mỗi tuần.";
                }
            }
            return null;
        }
    }

    /**
     * Ghi nhan da dung mon {@code loai} (SAU kiemDung == null va SAU khi tru vat pham). Ve: cong 30 phut vao
     * ve dang chay; bua: +60 phut 100% hom nay (goi {@link #congBua} cho nang luong); uy thac: +1 gio cho.
     * Keo hiem: chi tru diem (VatPhamDungLauDai tu dem 2/tuan va cong EXP).
     */
    public static void ghiDung(final HoSoLauDai h, final int loai, final int ngay, final int tuan) {
        synchronized (h) {
            lamMoi(h, ngay, tuan);
            h.tgDiemNgay += DIEM[loai];
            h.tgDiemTuan += DIEM[loai];
            if (loai == VE_15 || loai == VE_2) {
                h.tgVeHeSo = VE_HE_SO[loai];
                h.tgVeMs += VE_PHUT * PHUT_MS;
                h.tgVePhutNgay += VE_PHUT;
            } else if (loai == BUA) {
                h.tgBuaNgay++;
                congBua(h, ngay);
            } else if (loai == UY_THAC) {
                h.tgUtMs += GIO_MS;
            }
            h.ban = true;
        }
    }

    /** Bua the luc: +60 phut 100% cho ngay {@code ngay} (yyyyMMdd), khong tich sang hom sau. */
    static void congBua(final HoSoLauDai h, final int ngay) {
        if (h.nlNgay != ngay) {
            // chua danh quai hom nay: luu de NangLuong cong khi doi ngay
            h.tgBuaChoNgay = ngay;
            return;
        }
        h.nlPhut100 += BUA_PHUT;
        h.tgBuaCongNgay = ngay;
    }

    /**
     * NangLuong.ghiNhan goi ngay sau khi doi ngay: bua da dung truoc lan danh quai dau tien cua ngay thi cong
     * vao phut 100% bay gio. Phan bua khong dung het KHONG tich sang hom sau (NangLuong.phut100NgayMoi tru ra).
     */
    public static void sauDoiNgay(final HoSoLauDai h, final int ngay) {
        if (h.tgBuaChoNgay == ngay) {
            h.nlPhut100 += BUA_PHUT;
            h.tgBuaCongNgay = ngay;
            h.tgBuaChoNgay = 0;
        }
    }

    /** Phut 100% "that" (khong tinh bua) cua ngay da qua - de tinh tich luy sang hom sau. */
    public static int phut100KhongBua(final HoSoLauDai h) {
        return h.tgBuaCongNgay == h.nlNgay && h.tgBuaCongNgay != 0 ? h.nlPhut100 - BUA_PHUT : h.nlPhut100;
    }

    // ================================================================== ve EXP khi ha quai

    /**
     * He so ve (%) cho lan ha quai nay va TRU thoi gian ve bang thoi gian danh quai vua tinh
     * ({@code msVuaDanh} = phan NangLuong vua cong, <= 60 giay). Ve chi chay khi DANH QUAI (dung yen / AFK
     * khong mat ve). Het ve -> 100.
     */
    public static int heSoVeVaTru(final HoSoLauDai h, final long msVuaDanh) {
        synchronized (h) {
            if (h.tgVeMs <= 0L) {
                return 100;
            }
            final int heSo = h.tgVeHeSo > 100 ? h.tgVeHeSo : 100;
            h.tgVeMs = Math.max(0L, h.tgVeMs - Math.max(0L, msVuaDanh));
            if (h.tgVeMs == 0L) {
                h.tgVeHeSo = 0;
            }
            h.ban = true;
            return heSo;
        }
    }

    /** Ap he so ve (%) len EXP da tinh (sau nang luong, buff, to doi). Tran Integer.MAX. */
    public static int nhanVe(final int exp, final int heSoVe) {
        if (exp <= 0 || heSoVe <= 100) {
            return exp;
        }
        return (int) Math.min(Integer.MAX_VALUE, (long) exp * heSoVe / 100L);
    }

    // ================================================================== uy thac (offline)

    /**
     * Gio uy thac duoc tra khi dang nhap lai: min(gio da dat truoc, thoi gian offline). Phan khong dung (offline
     * ngan hon) giu lai cho lan sau. @return ms duoc tinh EXP (da tru khoi h.tgUtMs).
     */
    public static long tinhUyThac(final HoSoLauDai h, final long msThoat, final long bayGio, final int ngay, final int tuan) {
        synchronized (h) {
            lamMoi(h, ngay, tuan);
            if (h.tgUtMs <= 0L || msThoat <= 0L || bayGio <= msThoat) {
                return 0L;
            }
            final long ms = Math.min(h.tgUtMs, bayGio - msThoat);
            h.tgUtMs -= ms;
            h.tgUtMsNgay += ms;
            h.ban = true;
            return ms;
        }
    }

    /** EXP cho {@code ms} uy thac o EXP 1 gio {@code expMoiGio}: 20%. */
    public static long expUyThac(final long ms, final long expMoiGio) {
        if (ms <= 0L || expMoiGio <= 0L) {
            return 0L;
        }
        return expMoiGio * UY_THAC_PHAN_TRAM / 100L * ms / GIO_MS;
    }

    // ================================================================== mo ta

    /** Dong trang thai cho So tay: diem ngay/tuan, ve dang chay, uy thac cho. */
    public static String trangThai(final HoSoLauDai h, final int ngay, final int tuan) {
        synchronized (h) {
            lamMoi(h, ngay, tuan);
            final StringBuilder b = new StringBuilder();
            b.append("Tăng tốc: ").append(h.tgDiemNgay).append('/').append(DIEM_NGAY_TOI_DA).append(" điểm hôm nay, ")
                    .append(h.tgDiemTuan).append('/').append(DIEM_TUAN_TOI_DA).append(" tuần này");
            if (h.tgVeMs > 0L) {
                b.append("; vé EXP x").append(h.tgVeHeSo == 200 ? "2" : "1,5").append(" còn ")
                        .append(h.tgVeMs / PHUT_MS + 1).append(" phút đánh quái");
            }
            if (h.tgUtMs > 0L) {
                b.append("; ủy thác chờ ").append(h.tgUtMs / PHUT_MS).append(" phút (chạy khi thoát game)");
            }
            return b.append('.').toString();
        }
    }
}
