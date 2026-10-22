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
 *  cho_ban           HeSinhThaiMenu.banNpc / traTienNguoiBan {@link #khiBan}; nguoi ban OFFLINE: ghi
 *                    tob_nhiem_vu_cho ({@link #ghiChoNguoiBan}), ap luc vao ban do lan dau ({@link #apChoCho})  ""
 *  nang_cap          Map.doUseItemGoc (mac do nhan vat)     {@link #khiMacDo}      id template
 *  bo_trang_bi       cung cho (datMuc so mon cung bac cao nhat dang mac)       -
 *  pokedex_vung      PokedexPokemon.khiHaQuai (loai moi)    {@link #khiLoaiMoi}    vung 0 Kanto / 1 Johto / 2 Hoenn
 *  su_kien_vat_pham  vat pham cua su kien DANG BAT vao tui (Map.doGetPotion nhat, NPC su kien, giao hang...)
 *                    {@link #khiNhanVatPhamSuKien}                                id vat pham (data_su_kien_vat_pham)
 *  giao_hang/dua_thu Char.OnQuest tra nhiem vu chinh cu giao vat pham (type 1) {@link #khiGiaoVatPham}  id vat pham NV / ""
 *  ho_tong           Char.doResponseTieu (ve dich, khong bi cuop)       {@link #khiHoTong}  ""
 *  dung_thuoc        DungVatPham.dungThuoc (binh da tru khoi tui)       {@link #khiDungThuoc}  id thuoc
 *  hoc_chieu         Map.doAddSkillPoint (cong 1 diem) / Map.doLearnSkil (chieu moi o Blaziken) {@link #khiHocChieu}  id chieu
 *  tha_cau           HeSinhThaiMenu.thaCau (da tru the luc + moi, chua biet ket qua) {@link #khiThaCau}  ban do
 *                    (ba kieu tren: buoc hanh dong cua huong dan tan thu - HuongDanTanThuGame, khong co nhiem vu engine)
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

    /** Dung mot binh thuoc (da tru khoi tui). */
    public static void khiDungThuoc(final Char p, final int idThuoc) {
        ban(p, "dung_thuoc", String.valueOf(idThuoc), 1);
    }

    /** Cong mot diem ky nang / hoc mot chieu moi. */
    public static void khiHocChieu(final Char p, final int idChieu) {
        ban(p, "hoc_chieu", String.valueOf(idChieu), 1);
    }

    /** Tha cau thanh cong o ho / ao (chua biet ca can hay khong - "cau_ca" moi la cau duoc). */
    public static void khiThaCau(final Char p) {
        ban(p, "tha_cau", p == null ? "" : String.valueOf(p.mapID), 1);
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

    /**
     * Vat pham su kien VAO TUI (nhat tu quai roi Map.doGetPotion, NPC su kien, giao hang, che do / tien giai Vu Lan):
     * chi tinh khi vat pham thuoc mot su kien DANG BAT (bang data_su_kien_vat_pham, real.SuKienVatPham). Tham so = id vat
     * pham: nhiem vu dat muc_tieu_id = id de nham mot vat pham, de trong = bat ky vat pham cua su kien dang dien ra.
     */
    public static void khiNhanVatPhamSuKien(final Char p, final int idVatPham, final int soLuong) {
        if (p == null || soLuong <= 0) {
            return;
        }
        if (SuKienVatPham.suKienDangBat(idVatPham, SuKienVatPham::dangBat) != null) {
            ban(p, "su_kien_vat_pham", String.valueOf(idVatPham), soLuong);
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

    // ================================================================== cho_ban cho nguoi ban offline

    public static void taoBang() {
        data.DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_nhiem_vu_cho (id BIGINT NOT NULL AUTO_INCREMENT,"
                + " id_char INT NOT NULL, so INT NOT NULL DEFAULT 1, ghi_chu VARCHAR(128) NOT NULL DEFAULT '',"
                + " da_ap TINYINT NOT NULL DEFAULT 0, luc DATETIME NOT NULL, ap_luc DATETIME NULL, PRIMARY KEY (id),"
                + " KEY idx_nvc_char (id_char, da_ap)) DEFAULT CHARSET=utf8");
    }

    /** Nguoi ban OFFLINE ban duoc hang tren cho: ghi tien do cho (ap o lan vao ban do ke tiep). */
    public static void ghiChoNguoiBan(final int idChar, final String ghiChu) {
        if (idChar <= 0) {
            return;
        }
        data.DatabaseLauDai.ghiCong("INSERT INTO tob_nhiem_vu_cho (id_char, so, ghi_chu, da_ap, luc) VALUES (?, 1, ?, 0, NOW())",
                Integer.valueOf(idChar), ghiChu == null ? "" : (ghiChu.length() > 120 ? ghiChu.substring(0, 120) : ghiChu));
    }

    /**
     * Vao ban do lan dau trong phien (DanhHieuGame.khiVaoBanDo): ap moi dong tob_nhiem_vu_cho chua ap. Moi dong chi ap
     * khi UPDATE da_ap 0 -&gt; 1 doi dung 1 dong (hai phien / khoi dong lai khong ap hai lan).
     */
    public static int apChoCho(final Char p) {
        if (p == null || p.isBot != -1 || p.charDBID <= 0) {
            return 0;
        }
        final java.util.List<long[]> ds = new java.util.ArrayList<long[]>();
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pre = null;
        java.sql.ResultSet rs = null;
        try {
            conn = data.Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id, so FROM tob_nhiem_vu_cho WHERE id_char = ? AND da_ap = 0 ORDER BY id LIMIT 200");
            pre.setInt(1, p.charDBID);
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new long[]{rs.getLong(1), rs.getInt(2)});
            }
        } catch (final Exception e) {
            return 0;
        } finally {
            XepHangPvpGame.dong(rs, pre, conn);
        }
        int n = 0;
        for (final long[] d : ds) {
            if (data.DatabaseLauDai.ghiCong("UPDATE tob_nhiem_vu_cho SET da_ap = 1, ap_luc = NOW() WHERE id = ? AND da_ap = 0",
                    Long.valueOf(d[0])) == 1) {
                ban(p, "cho_ban", "", (int) Math.max(1L, d[1]));
                n++;
            }
        }
        return n;
    }
}
