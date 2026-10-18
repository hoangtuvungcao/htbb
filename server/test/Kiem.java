/**
 * Bo kiem thu toi gian - KHONG phu thuoc thu vien ngoai (khong JUnit).
 *
 * Vi sao khong dung JUnit: thu muc lib/ nam tren Class-Path cua htbb-server.jar
 * (xem MANIFEST), va build.bat gom TOAN BO src/. Them junit.jar vao lib/ hay them
 * test vao src/ la day ca hai len server that. Test nam o test/ rieng, build.bat
 * khong dung toi, nen jar deploy khong doi mot byte nao.
 *
 * Day la CHARACTERIZATION TEST: no ghi lai hanh vi HIEN TAI cua code, khong phai
 * hanh vi "dung". Muc dich la phat hien thay doi ngoai y muon khi refactor tiep -
 * neu mot phep tach lam doi ket qua, test do se do.
 */
public final class Kiem {

    private static int tong = 0;
    private static int hong = 0;
    private static String nhom = "";

    public static void nhom(final String ten) {
        nhom = ten;
        System.out.println();
        System.out.println("== " + ten);
    }

    public static void bang(final String moTa, final Object mongDoi, final Object thucTe) {
        tong++;
        final boolean ok = mongDoi == null ? thucTe == null : mongDoi.equals(thucTe);
        if (ok) {
            System.out.println("   ok   " + moTa);
        } else {
            hong++;
            System.out.println("   HONG " + moTa);
            System.out.println("        mong doi: [" + mongDoi + "]");
            System.out.println("        thuc te : [" + thucTe + "]");
        }
    }

    public static void dung(final String moTa, final boolean thucTe) {
        bang(moTa, Boolean.TRUE, Boolean.valueOf(thucTe));
    }

    public static void sai(final String moTa, final boolean thucTe) {
        bang(moTa, Boolean.FALSE, Boolean.valueOf(thucTe));
    }

    /** In tong ket va tra ve ma thoat: 0 = tat ca dat, 1 = co test hong. */
    public static int tongKet() {
        System.out.println();
        System.out.println("============================================================");
        if (hong == 0) {
            System.out.println("  DAT: " + tong + "/" + tong + " kiem tra");
        } else {
            System.out.println("  HONG: " + hong + "/" + tong + " kiem tra that bai");
        }
        System.out.println("============================================================");
        return hong == 0 ? 0 : 1;
    }

    public static void main(final String[] args) {
        System.out.println("============================================================");
        System.out.println("  KIEM THU HTBB");
        System.out.println("============================================================");
        System.out.println("(Khoi static cua real.Map co goi CSDL nen se in NullPointer-");
        System.out.println(" Exception o duoi - do la BINH THUONG khi chay ngoai server,");
        System.out.println(" Map van nap duoc va cac ham thuan van chay dung.)");

        KiemChuoi.chay();
        KiemVatPham.chay();
        KiemNhanhHoaDao.chay();
        KiemDucLoKham.chay();
        KiemSkinThuCuoi.chay();
        KiemPokemon.chay();
        KiemThuPokemon.chay();
        KiemIconPokemon.chay();
        KiemPetPokemon.chay();
        KiemLoiChoiPokemon.chay();
        KiemPokemonServer.chay();
        KiemPokemonClient.chay();
        KiemChuPokemon.chay();
        KiemThuatNgu.chay();
        KiemHieuUngPokemon.chay();
        KiemTilePokemon.chay();
        KiemMapPokemon.chay();
        KiemVungHoenn.chay();
        KiemTanThu.chay();
        KiemHinhPokemon.chay();
        KiemLauDai.chay();
        KiemNhiemVu.chay();
        KiemNovaDB.chay();
        KiemPhienBan.chay();
        KiemGoi26Test.chay();
        KiemGoi3Test.chay();
        KiemGoi5Test.chay();
        KiemBoDemGoiTin.chay();
        KiemNhatKyChongSpam.chay();
        KiemMatDoKhiTaoDo.chay();
        KiemCuaHangSuKien.chay();
        KiemAoHoaPonyta.chay();
        KiemNghiVanGianLan.chay();
        KiemTienAm.chay();
        KiemMenuDauTruong.chay();
        KiemNpcAn.chay();
        KiemLogMatVatPham.chay();
        KiemLocLogVatPham.chay();
        KiemBanDoOffline.chay();
        KiemCamTheoMay.chay();
        KiemDangKyTrongGame.chay();
        KiemProxyTinCay.chay();
        KiemBanMauVps.chay();
        KiemClientUnity.chay();
        KiemTatCaiTrang.chay();
        KiemTaiLieuCauHinh.chay();
        KiemChotCSDL.chay();
        KiemMayDevCSDL.chay();
        KiemCauHinhPipeline.chay();
        KiemCauHinhTrungTam.chay();
        KiemLocBiMat.chay();
        KiemChiTietThuoc.chay();
        KiemIconSkinThu.chay();
        KiemDanhMucNgoc.chay();
        KiemDanhMucThu.chay();
        KiemDanhMucDanhHieu.chay();
        KiemDanhMucItem.chay();
        KiemBangThuocTinh.chay();
        KiemLienKetTaiLieu.chay();
        KiemAnhHieuUng.chay();
        KiemLenhWeb.chay();
        KiemQuaVuonCapCao.chay();
        KiemAnhThieuTraTrongSuot.chay();
        KiemBangCapDo.chay();
        KiemSuKien.chay();
        KiemLichSuKien.chay();
        KiemSuKienWorldCup.chay();
        KiemVongQuayThanTai.chay();
        KiemHoaHinhPet.chay();
        KiemChoPetAn.chay();
        KiemHoaHinhThu.chay();
        KiemSoLuongItemGiftcode.chay();
        KiemTranXuRoi.chay();
        KiemTinhLuyenBaoVat.chay();
        KiemDonLog.chay();
        KiemCaiTaoCaiTrang.chay();
        KiemBangMaNguon.chay();
        KiemNpcHoaTieuNhieuKhu.chay();
        KiemNearCharBoQuaLoiRiengLe.chay();
        KiemCharManagerBotTrungTen.chay();
        KiemGiaoDichThuoc.chay();
        KiemChiemVatRoi.chay();
        KiemGiftcodeDoCamTayBaoVat.chay();
        KiemGiftcodeRuyBangHoenn.chay();
        KiemNangCapDoCamTayLv86.chay();
        KiemXoaVinhVien.chay();
        KiemGuiGoiNguoiGan.chay();
        KiemGoiViTriActor.chay();
        KiemDemGoiTin.chay();
        KiemMauPetCoAnh.chay();
        KiemChuHoaCoDau.chay();
        KiemGiamExpQuaiLv60.chay();
        KiemBaoTriDaily.chay();
        KiemGiaiTanBangMoiTao.chay();
        KiemGoi26SetDanh.chay();
        KiemSaveCharAutoDuBangPhu.chay();
        KiemDongBoMyCountryKhiVaoBang.chay();
        KiemThuongTanThuVaLenCap35.chay();
        KiemChiemGymDoiDiemGiaoHang.chay();
        KiemCuocKhuMoAttackDam.chay();
        KiemMaterialMapNhatDo.chay();
        KiemPetTrangThai1LaPetTool.chay();
        KiemLauDaiGd2.chay();
        KiemNoiDung.chay();
        KiemHeSinhThai.chay();
        KiemItemMoCoi.chay(); // luat khong item mo coi (tools/pokemon/kiem_item_mo_coi.py)
        KiemNapTien.chay();
        KiemTangToc.chay();
        KiemTuDong.chay();
        KiemHoiSinh.chay(); // guc / hoi sinh (com.htbb.pokemon.HoiSinh)
        KiemNgoaiHinh.chay(); // ngoai hinh trang bi tren than Pokemon (docs/thiet-ke-game.md)
        KiemThanhVien.chay();
        KiemVongLapChung.chay(); // vong lap chung thay thread-moi-khu (docs/kien-truc.md)

        KiemChiSo.chay(); // chi so trang bi + chong lam phat (docs/thiet-ke-game.md)
        KiemAdminPanel.chay();
        KiemKhongLoiCu.chay(); // P3: khong con lib/htbb-core.jar (docs/giay-phep.md)
        final int ma = tongKet();
        // Khoi static cua Map tao thread nen JVM khong tu thoat - phai ep thoat.
        System.exit(ma);
    }

    private Kiem() {
    }
}
