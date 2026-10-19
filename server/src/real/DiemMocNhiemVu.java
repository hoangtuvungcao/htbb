package real;

/**
 * DIEM MOC NHIEM VU (0.9.7): MOT cho cho cac kieu muc tieu (data_nhiem_vu_kieu) truoc day khong co diem goi - moi ham la
 * MOT su kien game, goi NhiemVuEngine.suKien / datMuc (kieu chung, khong gan voi nhiem vu cu the nao; ca chuoi chinh moi
 * cua nhanh khoa-map cung chi doc cac su kien nay). Danh hieu (DanhHieuGame) nghe cung cac su kien.
 *
 * <pre>
 *  kieu              noi goi (mot cho)                                tham so
 *  cau_ca            com.htbb.pokemon.HeSinhThaiMenu.cauCa (KHONG o day: diem moc duy nhat cua cau ca nam o do;
 *                    nhanh khac dung them diem moc cau_ca la dem hai lan)   dex gap (0 = khong)
 *  dao_dat           HeSinhThaiMenu.dao                    {@link #khiDao}        ma vat lieu
 *  nau_an            HeSinhThaiMenu.lamCongThuc (ky nang Nau an) {@link #khiNau}  id cong thuc
 *  lam_vuon          HeSinhThaiGame.khiThuHoach            {@link #khiThuHoach}   id cay
 *  ap_trung          HeSinhThaiMenu.hanhDongTrung (no)     {@link #khiNoTrung}    dex
 *  do_vui            HeSinhThaiGame.DoVui.traLoi (dung)     {@link #khiDoVui}      ""
 *  dua_pokemon       HeSinhThaiGame.Dua.ketThuc (ve dich)   {@link #khiDua}        hang (1..)
 *  cho_ban           HeSinhThaiMenu.banNpc / traTienNguoiBan (nguoi ban online) {@link #khiBan}  ""
 *  nang_cap          Map.doUseItemGoc (mac do nhan vat)     {@link #khiMacDo}      id template
 *  bo_trang_bi       cung cho (datMuc so mon cung bac cao nhat dang mac)       -
 *  pokedex_vung      PokedexPokemon.khiHaQuai (loai moi)    {@link #khiLoaiMoi}    vung 0 Kanto / 1 Johto / 2 Hoenn
 *  su_kien_vat_pham  NhiemVuEngine.khiHaQuai khi co su kien dang bat (roi vat pham su kien) {@link #khiHaQuaiSuKien}  ""
 *  giao_hang/dua_thu Char.OnQuest tra nhiem vu chinh cu giao vat pham (type 1) {@link #khiGiaoVatPham}  id vat pham NV / ""
 *  ho_tong           Char.doResponseTieu (ve dich, khong bi cuop)       {@link #khiHoTong}  ""
 * </pre>
 * Kiem thu: test/KiemDiemMocNhiemVu (moi kieu trong data_nhiem_vu_kieu co diem goi; tham so dung).
 */
public final class DiemMocNhiemVu {

    private DiemMocNhiemVu() {
    }

    static void ban(final Char p, final String kieu, final String thamSo, final int n) {
        try {
            if (p != null && p.isBot == -1 && p.charDBID > 0) {
                NhiemVuEngine.suKien(p, kieu, thamSo == null ? "" : thamSo, n);
            }
        } catch (final Exception e) {
            System.out.println("DiemMocNhiemVu " + kieu + ": " + e);
        }
    }

    public static void khiDao(final Char p, final int maVatLieu) {
        ban(p, "dao_dat", String.valueOf(maVatLieu), 1);
    }

    /** Lam xong mot cong thuc; chi ky nang Nau an tinh "nau_an". */
    public static void khiNau(final Char p, final int idCongThuc, final boolean laNauAn) {
        if (laNauAn) {
            ban(p, "nau_an", String.valueOf(idCongThuc), 1);
        }
    }

    public static void khiThuHoach(final Char p, final int idCay) {
        ban(p, "lam_vuon", String.valueOf(idCay), 1);
    }

    public static void khiNoTrung(final Char p, final int dex) {
        ban(p, "ap_trung", String.valueOf(dex), 1);
    }

    public static void khiDoVui(final Char p) {
        ban(p, "do_vui", "", 1);
    }

    /** Ve dich Dua Pokemon (moi nguoi ve dich, hang 1..). */
    public static void khiDua(final Char p, final int hang) {
        ban(p, "dua_pokemon", String.valueOf(hang), 1);
    }

    /** Ban duoc hang (cho sinh thai cho nguoi khac hoac cho NPC). */
    public static void khiBan(final Char p) {
        ban(p, "cho_ban", "", 1);
    }

    /** Mac mot mon trang bi (khong phai do thu): "nang_cap" + "bo_trang_bi" = so mon cung bac cao nhat. */
    public static void khiMacDo(final Char p, final int idTemplate) {
        ban(p, "nang_cap", String.valueOf(idTemplate), 1);
        try {
            if (p != null && p.isBot == -1 && p.charDBID > 0) {
                NhiemVuEngine.datMuc(p, "bo_trang_bi", soMonCungBac(p));
            }
        } catch (final Exception e) {
            System.out.println("DiemMocNhiemVu bo_trang_bi: " + e);
        }
    }

    /** So mon dang mac cung "bac" (cap yeu cau / 10) nhieu nhat. */
    static int soMonCungBac(final Char p) {
        final java.util.Vector<Item> ds = p.getCharWearing();
        if (ds == null) {
            return 0;
        }
        final java.util.HashMap<Integer, Integer> dem = new java.util.HashMap<Integer, Integer>();
        int max = 0;
        for (final Item it : ds) {
            if (it == null || it.getTemplate() == null) {
                continue;
            }
            final int bac = it.getTemplate().level / 10;
            final int n = dem.merge(Integer.valueOf(bac), 1, Integer::sum);
            max = Math.max(max, n);
        }
        return max;
    }

    /** Vung cua mot loai theo so Pokedex quoc gia: 0 Kanto (1..151), 1 Johto (152..251), 2 Hoenn (252..386), -1 khac. */
    public static int vungLoai(final int dex) {
        if (dex >= 1 && dex <= 151) {
            return 0;
        }
        if (dex <= 251 && dex > 151) {
            return 1;
        }
        if (dex <= 386 && dex > 251) {
            return 2;
        }
        return -1;
    }

    /** Lan dau ha mot loai (Pokedex "da ha" moi) -&gt; "pokedex_vung" tham so vung. */
    public static void khiLoaiMoi(final Char p, final int dex) {
        final int v = vungLoai(dex);
        if (v >= 0) {
            ban(p, "pokedex_vung", String.valueOf(v), 1);
        }
    }

    /** Ti le (phan tram) mot lan ha quai trong luc co su kien bat tinh la nhat duoc vat pham su kien. */
    static final int TI_LE_SU_KIEN = 35;

    /** NhiemVuEngine.khiHaQuai: neu co su kien dang bat, 35% lan ha tinh "su_kien_vat_pham". */
    public static void khiHaQuaiSuKien(final Char p, final java.util.Random r) {
        if (p == null || !coSuKien()) {
            return;
        }
        if ((r == null ? Map.random(100) : r.nextInt(100)) < TI_LE_SU_KIEN) {
            ban(p, "su_kien_vat_pham", "", 1);
        }
    }

    /** Co su kien dang bat (lich su kien nam / su kien dai han). */
    static boolean coSuKien() {
        try {
            for (final String ma : com.htbb.pokemon.SuKienLauDai.MA_MOI) {
                if (SuKien.bat("moi:" + ma)) {
                    return true;
                }
            }
            return SuKien.bat(SuKien.HALLOWEEN) || SuKien.bat(SuKien.NOEL) || SuKien.bat(SuKien.TRUNG_THU)
                    || SuKien.bat(SuKien.TET_DUONG_LICH);
        } catch (final Exception e) {
            return false;
        }
    }

    /** Tra nhiem vu giao vat pham (chuoi chinh cu, type 1): "giao_hang" tham so id vat pham NV + "dua_thu". */
    public static void khiGiaoVatPham(final Char p, final short[] idVatPham) {
        if (idVatPham == null || idVatPham.length == 0) {
            ban(p, "giao_hang", "", 1);
        } else {
            for (final short id : idVatPham) {
                ban(p, "giao_hang", String.valueOf(id), 1);
            }
        }
        ban(p, "dua_thu", "", 1);
    }

    /** Ho tong (van tieu) ve dich, khong bi cuop. */
    public static void khiHoTong(final Char p) {
        ban(p, "ho_tong", "", 1);
    }
}
