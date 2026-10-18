package com.htbb.pokemon;

/**
 * Noi chi so cua he sinh thai (HoSoSinhThai) vao chi so nhan vat (CharChiSo / Char.getAttack). Lop
 * nho, chi doc - logic so o SuuTamSinhThai / NhaTre (doi he sinh thai). Tat sv.he_sinh_thai: 0.
 *
 *   - hoan thanh vung: +0,5%/vung, tran 3% (SuuTamSinhThai.chiSoVinhVien) cho HP va tan cong;
 *   - IV cua pet dang mang (dong giong tot nhat cua loai goc pet trong ho so): moi chi so 0..31
 *     cho toi da +1% (HP / tan cong / thu) - nho, co tran, trong ngan sach suc manh (THIET_KE 3.6).
 */
public final class ChiSoSinhThai {

    /** Tran % tu IV pet moi chi so (phan nghin). */
    public static final int IV_TRAN_PHAN_NGHIN = 10;

    private ChiSoSinhThai() {
    }

    /** Phan nghin vinh vien tu so vung da hoan thanh. */
    public static int vungPhanNghin(final real.Char p) {
        try {
            final HoSoSinhThai h = HeSinhThaiGame.hoSo(p);
            return h == null ? 0 : SuuTamSinhThai.chiSoVinhVien(Integer.bitCount(h.vungXong));
        } catch (final Throwable e) {
            return 0;
        }
    }

    /** Phan nghin tu IV chi so {@code i} (0 HP, 1 cong, 2 thu) cua pet dang mang. */
    public static int ivPetPhanNghin(final real.Char p, final int i) {
        try {
            if (p == null || p.petUsing == null || p.petUsing.itemPet == null || p.petUsing.itemPet.getTemplate() == null) {
                return 0;
            }
            final int dex = PetPokemon.dex(p.petUsing.itemPet.getTemplate().id);
            if (dex <= 0) {
                return 0;
            }
            final HoSoSinhThai h = HeSinhThaiGame.hoSo(p);
            if (h == null) {
                return 0;
            }
            final int[] iv = h.giong.get(NhaTre.loaiGoc(dex));
            return iv == null || i < 0 || i >= iv.length ? 0 : ivPhanNghin(iv[i]);
        } catch (final Throwable e) {
            return 0;
        }
    }

    /** IV 0..31 -> 0..10 phan nghin. */
    public static int ivPhanNghin(final int iv) {
        return Math.max(0, Math.min(HeSinhThai.IV_TOI_DA, iv)) * IV_TRAN_PHAN_NGHIN / HeSinhThai.IV_TOI_DA;
    }
}
