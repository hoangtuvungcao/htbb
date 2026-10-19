package com.htbb.cauhinh;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tinh cau hinh hieu luc - HAM THUAN (khong CSDL, khong luong), test/KiemCauHinhTrungTam goi truc tiep.
 *
 * THU TU UU TIEN (moi khoa trong danh muc, bang cau_hinh):
 *   1. CSDL (bang cau_hinh)  - hop le thi dung; SAI (kieu/khoang/lua chon) thi TU CHOI: giu gia tri tot gan
 *                               nhat (lan nap truoc), chua co lan nao thi roi xuong server.ini.
 *   2. server.ini            - gia tri cua nguoi van hanh tren may. Sai luat van GIU (hanh vi cu - ma doc
 *                               rieng cua tung tinh nang tu kep/bo qua), chi ghi canh bao.
 *   3. mac dinh trong danh muc.
 *
 * Khoa "khoaProd" (thu nghiem): gia tri tu CSDL khac mac dinh bi TU CHOI tren may that (choPhepThuNghiem
 * = false, tuc server.ini khong co sv.cho_phep_thu_nghiem=1).
 *
 * Khoa AN (lop co_dinh / luon_bat, docs/cau-hinh.md): may that BO QUA dong CSDL (ghi loi de trang thay), lay
 * server.ini (khan cap) roi gia tri co dinh trong danh muc, nguon {@link #NGUON_CO_DINH}. Gia tri co dinh LUON
 * vao tatCa (ke ca khi bang macDinh) de ma doc cu (Properties) chay dung gia tri san xuat thay vi mac dinh rieng.
 * May thu (choPhepThuNghiem) van doc CSDL nhu khoa thuong.
 *
 * Khoa "khoi dong lai" (apDungNgay = false): sau lan tinh dau tien (luc khoi dong, cu == null), gia tri
 * hieu luc DONG BANG; gia tri moi trong CSDL/ini chi ghi vao choKhoiDong de trang quan tri hien
 * "cho khoi dong lai".
 *
 * Khoa KHONG co trong danh muc (vd khoa de so lieu dai han data_kinh_te_lau_dai, lau_dai.* do game ghi):
 * di thang tu CSDL vao tatCa (nhu CauHinh.napLai cu), khong kiem.
 */
public final class TinhCauHinh {

    public static final String NGUON_DB = "db";
    public static final String NGUON_INI = "ini";
    public static final String NGUON_MAC_DINH = "mac_dinh";
    public static final String NGUON_GIU_CU = "giu_cu";
    /** Khoa an (co dinh / luon bat), khong co dong server.ini: gia tri co dinh cua danh muc. */
    public static final String NGUON_CO_DINH = "co_dinh";

    private TinhCauHinh() {
    }

    /** Ket qua bat bien. */
    public static final class KetQua {
        /** Khoa trong danh muc (bang cau_hinh) -> gia tri hieu luc (luon co, toi thieu la mac dinh). */
        public final Map<String, String> hieuLuc;
        /** Khoa trong danh muc -> db | ini | mac_dinh | giu_cu. */
        public final Map<String, String> nguon;
        /** Khoa -> ly do gia tri bi tu choi / canh bao (rong = khong loi). */
        public final Map<String, String> loi;
        /** Khoa chi doi khi khoi dong lai -> gia tri dang cho (khac gia tri dang chay). */
        public final Map<String, String> choKhoiDong;
        /**
         * Toan bo khoa cho ma doc cu (Properties): server.ini + dong CSDL ngoai danh muc + khoa danh muc
         * co nguon khac mac_dinh. Khoa danh muc khong co o dau ca thi VANG MAT (ma cu dung mac dinh rieng).
         */
        public final Map<String, String> tatCa;

        KetQua(final Map<String, String> h, final Map<String, String> n, final Map<String, String> l,
                final Map<String, String> c, final Map<String, String> t) {
            this.hieuLuc = Collections.unmodifiableMap(h);
            this.nguon = Collections.unmodifiableMap(n);
            this.loi = Collections.unmodifiableMap(l);
            this.choKhoiDong = Collections.unmodifiableMap(c);
            this.tatCa = Collections.unmodifiableMap(t);
        }
    }

    public static KetQua tinh(final DinhNghiaKhoa[] dm, final Map<String, String> ini, final Map<String, String> db,
            final KetQua cu, final boolean choPhepThuNghiem) {
        final Map<String, String> iniM = ini == null ? Collections.<String, String>emptyMap() : ini;
        final Map<String, String> dbM = db == null ? Collections.<String, String>emptyMap() : db;
        final Map<String, String> hieuLuc = new LinkedHashMap<String, String>();
        final Map<String, String> nguon = new LinkedHashMap<String, String>();
        final Map<String, String> loi = new LinkedHashMap<String, String>();
        final Map<String, String> cho = new LinkedHashMap<String, String>();
        final Map<String, String> tatCa = new HashMap<String, String>(iniM);
        final Map<String, DinhNghiaKhoa> theoKhoa = new HashMap<String, DinhNghiaKhoa>();
        for (final DinhNghiaKhoa d : dm) {
            if (d.trongCauHinh()) {
                theoKhoa.put(d.khoa, d);
            }
        }
        for (final Map.Entry<String, String> e : dbM.entrySet()) {
            if (!theoKhoa.containsKey(e.getKey()) && e.getValue() != null) {
                tatCa.put(e.getKey(), e.getValue().trim());
            }
        }
        for (final DinhNghiaKhoa d : dm) {
            if (!d.trongCauHinh()) {
                continue;
            }
            final String k = d.khoa;
            String ungVien = null;
            String tu = null;
            final boolean anTrenMayThat = d.laAn() && !choPhepThuNghiem;
            final String vDb = dbM.get(k);
            if (vDb != null && anTrenMayThat) {
                if (!vDb.trim().equals(d.macDinh)) {
                    loi.put(k, "khoá cố định — máy thật bỏ qua CSDL \"" + catNgan(vDb) + "\", dùng \"" + d.macDinh
                            + "\" (đổi khẩn cấp: server.ini)");
                }
            } else if (vDb != null) {
                final KiemGiaTri.KetQua r = KiemGiaTri.kiem(d, vDb);
                if (!r.dung()) {
                    loi.put(k, "CSDL \"" + catNgan(vDb) + "\" bị từ chối: " + r.loi);
                } else if (d.laKhoaProd() && !choPhepThuNghiem && !r.giaTri.equals(d.macDinh)) {
                    loi.put(k, "khoá thử nghiệm — máy thật chỉ nhận mặc định \"" + d.macDinh
                            + "\" (cần sv.cho_phep_thu_nghiem=1 trong server.ini)");
                } else {
                    ungVien = r.giaTri;
                    tu = NGUON_DB;
                }
                if (ungVien == null && cu != null && cu.hieuLuc.containsKey(k) && d.apDungNgay) {
                    // Gia tri CSDL sai: giu gia tri tot gan nhat (khong roi ve ini giua chung -> khong giat).
                    ungVien = cu.hieuLuc.get(k);
                    tu = NGUON_GIU_CU;
                }
            }
            if (ungVien == null) {
                final String vIni = iniM.get(k);
                if (vIni != null) {
                    final KiemGiaTri.KetQua r = KiemGiaTri.kiem(d, vIni);
                    if (r.dung()) {
                        ungVien = r.giaTri;
                    } else {
                        ungVien = vIni.trim();
                        if (!loi.containsKey(k)) {
                            loi.put(k, "server.ini \"" + catNgan(vIni) + "\" sai luật (" + r.loi + ") — vẫn dùng như cũ");
                        }
                    }
                    tu = NGUON_INI;
                } else {
                    ungVien = d.macDinh;
                    tu = anTrenMayThat ? NGUON_CO_DINH : NGUON_MAC_DINH;
                }
            }
            String giaTri = ungVien;
            String nguonCuoi = tu;
            if (!d.apDungNgay && cu != null && cu.hieuLuc.containsKey(k)) {
                giaTri = cu.hieuLuc.get(k);
                nguonCuoi = cu.nguon.containsKey(k) ? cu.nguon.get(k) : tu;
                if (!giaTri.equals(ungVien)) {
                    cho.put(k, ungVien);
                }
            }
            hieuLuc.put(k, giaTri);
            nguon.put(k, nguonCuoi);
            if (!NGUON_MAC_DINH.equals(nguonCuoi)) {
                tatCa.put(k, giaTri);
            } else {
                tatCa.remove(k);
            }
        }
        return new KetQua(hieuLuc, nguon, loi, cho, tatCa);
    }

    static String catNgan(final String s) {
        final String t = s.trim();
        return t.length() > 60 ? t.substring(0, 57) + "..." : t;
    }
}
