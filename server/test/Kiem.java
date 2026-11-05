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

        // P1: so lieu thiet ke (bang EXP, nang cap, kinh te, lich su kien, diem cau, trang bi giftcode, danh muc cau
        // hinh) NAP TU sql/htbb.sql TRUOC moi nhom - giong may chu nap tu CSDL (data.DatabaseDuLieuSql).
        try {
            KiemDuLieuSql.napKiem = com.htbb.dulieu.DuLieuSql.napTuDumpDayDu();
        } catch (final RuntimeException e) {
            System.out.println("!!! KHONG NAP DUOC DU LIEU SQL: " + e.getMessage());
        }
        KiemDuLieuSql.chay();
        KiemChuoi.chay();
        KiemVatPham.chay();
        KiemNhanhHoaDao.chay();
        KiemDucLoKham.chay();
        KiemSkinThuCuoi.chay();
        KiemPokemon.chay();
        KiemThuPokemon.chay();
        KiemIconPokemon.chay();
        KiemIconThayCu.chay();
        KiemQuaiPokemon.chay(); // moi quai/boss ve bang Pokemon o jar + Unity, het anh quai cu (P2)
        KiemPetPokemon.chay();
        KiemTaiNguyenCu.chay(); // khong con hinh may chu cu trong tai nguyen phat hanh (du_lieu/tai_nguyen_cu*.{txt,csv}, P2)
        KiemTenCu.chay(); // khong con ten game cu trong server/src, client_jar/src, web/, client_pc_ios, client_apk, client_pc (ngoai le: du_lieu/ten_cu_cho_phep.csv)
        KiemThuPetP2.chay(); // thu cuoi / pet / skin / hoa hinh -> Pokemon: SQL data_thu_pokemon = Java = CSV = client (P2)
        KiemLoiChoiPokemon.chay();
        KiemPokemonServer.chay();
        KiemPokemonClient.chay();
        KiemChuPokemon.chay();
        KiemThuatNgu.chay();
        KiemDangNhapKhac.chay();
        KiemHieuUngPokemon.chay();
        KiemTilePokemon.chay();
        KiemMapPokemon.chay();
        KiemVungHoenn.chay();
        KiemTanThu.chay();
        KiemHinhPokemon.chay();
        KiemNpcSprite.chay(); // anh NPC = Pokemon: SQL pokemon_npc_sprite.sql = jar = Unity = csv, npc.sh khong con anh cu (P2)
        KiemLauDai.chay();
        KiemNhiemVu.chay();
        KiemHienThiChu.chay(); // ra soat hien thi: chu nguoi choi thay (ten lop lot, thieu dau, ma loi) + chu nhiem vu khop du lieu
        KiemNovaDB.chay();
        KiemPhienBan.chay();
        KiemGoi26Test.chay();
        KiemGoi3Test.chay();
        KiemGoi5Test.chay();
        KiemBoDemGoiTin.chay();
        KiemNhatKyChongSpam.chay();
        KiemTheoDoiBatThuong.chay();
        KiemMapClanVao.chay(); // vao ban do bang khi chua co danh sach: khong NPE (cong kiem 24/10)
        KiemSuaDeXuatBot.chay(); // 3 de xuat bot hoi quy 25/10: chiem Gym chi trong Gym, cong sai o khong kick ngay, dang ky chan theo may
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
        KiemLoaiTienCuaHang.chay();
        KiemIconSkinThu.chay();
        KiemDanhMucNgoc.chay();
        KiemDanhMucThu.chay();
        KiemDanhMucDanhHieu.chay();
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
        KiemToiUuLag.chay();
        KiemKhoGoi.chay();
        KiemCaiTaoCaiTrang.chay();
        KiemBangMaNguon.chay();
        KiemNpcHoaTieuNhieuKhu.chay();
        KiemNearCharBoQuaLoiRiengLe.chay();
        KiemCharManagerBotTrungTen.chay();
        KiemGiaoDichThuoc.chay();
        KiemChiemVatRoi.chay();
        KiemDocThu.chay();
        KiemGiaoDichRutDo.chay();
        KiemGiaoDichXu.chay(); // 0.9.27 giao dich xu 1-1 (goi 66/2/3): nguyen tu, khong nhan doi, tran ngay, luong khong giao dich
        KiemTruTruocTrao.chay(); // 29/10/2026 ma-tran-bot: cho / quy doi / ghep ngoc / che tao / nang cap cu tru TRUOC trao; ban thu NPC; kho do
        KiemKetBan.chay();
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
        KiemLoiBotChucNang.chay();
        KiemCuocKhuMoAttackDam.chay();
        KiemMaterialMapNhatDo.chay();
        KiemPetTrangThai1LaPetTool.chay();
        KiemLauDaiGd2.chay();
        KiemNoiDung.chay();
        KiemHeSinhThai.chay();
        KiemItemMoCoi.chay(); // luat khong item mo coi (kiem tren sql/htbb.sql)
        KiemNapTien.chay();
        KiemTangToc.chay();
        KiemTuDong.chay();
        KiemTuDongKeHoach.chay(); // bo nao Tu dong danh (com.htbb.pokemon.TuDongKeHoach)
        KiemDiChuyen.chay(); // lenh 4: quy toc do (real.TocDoDiChuyen) + duoi quai / tim duong client (DuongDi)
        KiemQuaiDiChuyen.chay(); // quai di chuyen theo va cham nguoi choi (DiChuyenQuai, moi map cMap, Monster client)
        KiemHoiSinh.chay(); // guc / hoi sinh (com.htbb.pokemon.HoiSinh)
        KiemDungVatPham.chay(); // lenh 29 / 22: moi loai vat pham co tac dung + phan hoi (real.DungVatPham, -104)
        KiemDoiCuocTrangBiThu.chay(); // 0.9.30: thay cuoc (khong con "2 loai cuoc"), thao / thay do thu, "Cần cấp N để dùng"
        KiemChongVatPham.chay(); // 30/10/2026: vat pham type 28 giong het chong mot o (ChongVatPham: gop / nhan / dung / ban / kho / luu)
        KiemPhuQuaNgay.chay(); // 30/10/2026: nhiem vu phu lau dai (P-*) giu tien do qua ngay (truoc day xoa im lang luc 0h)
        KiemPhuMoRong.chay(); // 30/10/2026: phu moi khi cap nv <= cap + 10 (khong chan duoi), xep gan cap, trang So tay
        KiemPhuNpcMotNguon.chay(); // chu game 31/10/2026: NPC phu cap -15..+3 / 30 muc; buoc nhan o So tay hien o NPC
        KiemQuaiNhiemVuToiDuoc.chay(); // 30/10/2026: quai nhiem vu sinh o ban do toi duoc (Dong co phia tay 21 / Tau S.S. Anne 22 qua Arcanine)
        KiemVeTreo.chay(); // Ve treo 10 gio: kho gio Tu dong danh + giftcode TREO400 (XU + ve xep chong)
        KiemNangLuongThanhVien.chay(); // Bua the luc o cua hang + thuong nang luong Thanh vien / The thang / Ve mua + Tu dong luc 0%
        KiemLuongKhoaShop.chay(); // cua hang dac biet: dong tra bang luong khoa ghi ro / goi 25 noi them / khong tru luong thay
        KiemMoTaVatPham.chay(); // 0.9.14 ra soat vat pham: loai / mo ta / dung tu tui / sao goi thu cuoi / shop
        KiemQuanLyThu.chay(); // o thu chung, tha ve tu nhien (xu), khoa, ve mo o thu, goi 16 noi them + -119
        KiemMoRongTui.chay(); // mo rong hanh trang (ve / xu, -105 noi them, -107) + vut / ban co so luong (-21 / -23 / 78)
        KiemTrangThaiVatPham.chay(); // 0.9.3: trang thai -105 ngay sau mua / ban / dung / mac / thao, -104 co kieu
        KiemNgoaiHinh.chay(); // ngoai hinh trang bi tren than Pokemon (docs/thiet-ke-game.md)
        KiemThanhVien.chay();
        KiemVongLapChung.chay(); // vong lap chung thay thread-moi-khu (docs/kien-truc.md)

        KiemChiSo.chay(); // chi so trang bi + chong lam phat (docs/thiet-ke-game.md)
        KiemAdminPanel.chay();
        KiemKhongLoiCu.chay(); // P3: khong con lib/htbb-core.jar (docs/giay-phep.md)
        KiemKinhTeKhoiDau.chay(); // qua tao nhan vat da bo, he so nap = 1, ghi client dang nhap
        KiemChongClone.chay();
        KiemVongQuay.chay(); // vong quay gop hu: muc cuoc, phi, bao toan hu, nut Dong y (chu game 19/10/2026)
        KiemPhoBanCup.chay(); // 0.9.7 Pho ban Ham nguc + Cup PvP tuan: xoay tuan, Thuy Si, chong clone, vong doi phong, thuong mot lan
        KiemThachDauCuoc.chay(); // thach dau cuoc xu: ky quy, phi, muc theo cap (19/10/2026)
        KiemLiXi.chay(); // li xi Tet: tran, xu theo gio, ma luong khoa theo tai khoan (19/10/2026)
        KiemLichNam.chay(); // lich su kien nam theo ngay that + am lich (chu game 19/10/2026)
        KiemVungNhinCoDinh.chay(); // camera co dinh: kep be rong man client (com.htbb.mang.VungNhinCoDinh)
        KiemTenHien.chay(); // ten NPC + ten ban do tu SQL data_npc / data_map_ten, co dau, khong tu cam (P6)
        KiemHeHieuQua.chay(); // 0.9.5: he so khac he (server quyet), Normal vs Water/Ground x1, Grass x4, Electric vs Ground x0
        KiemKhoaBanDo.chay(); // khoa ban do theo cap + cua ai, moi duong vao, mien tru, Tu dong theo ban do (khoa-map)
        KiemThongBaoNguoiChoi.chay(); // 0.9.6.1: thong bao nguoi choi ro rang (cau ca, boss ai/o dau), goi -104 co hinh
        KiemSuaLogic096.chay(); // 0.9.6: sua logic server - ban ngoc NPC, ky gui, EXP doi / ha quai, nhiem vu, nhom
        KiemNhiemVuOn.chay(); // 0.9.7.1: nhan / tra / huy nhiem vu on dinh, dong bo danh sach client
        KiemKetDoi.chay(); // Le ket doi: nhom trung (Egg Group), ma tran 21 x 21 loai, ly do tu choi, cap cu giu nguyen
        KiemHuongDanTanThu.chay(); // huong dan tan thu: 10 buoc dau chuoi chinh, bo qua, NPC lang, chuoi chinh sau huong dan
        KiemSuaTuyen.chay(); // sua-tuyen: lang theo vung, buoc dia diem co vung dich di duoc, chu phan thuong = dong thuong
        KiemNpc.chay(); // moi NPC + moi muc menu NPC dung duoc: NPC tinh ngoai troi, su kien -49, muc da go (npcfix)
        KiemTaiDuLieu096.chay(); // 0.9.6.1 loi tai: Bang vang khong treo, ban do / tai nguyen / id map SQL tai duoc
        KiemMenu096.chay(); // 0.9.6.1 menu He sinh thai: khong muc cau ca (chi nut ngu canh), chi so menu goc khop xu ly
        KiemGiapVoHan.chay(); // 0.9.6.1: giap / khien khong vo han - het han dung luc, hoi chieu > thoi luong (real.ChieuHoTro)
        KiemCauCaNhip.chay(); // 0.9.6.1 cau ca: cho ngau nhien, khung keo can, 30% khong duoc gi, nghi + giam theo gio, phao xuong nuoc
        KiemKhoaKhiMac.chay(); // 0.9.6.1: do da mac bi khoa (khong giao dich), pha khoa o Machamp (ho hut xu), Da pha khoa 1295
        KiemThongBaoNguoiChoi.chay(); // 0.9.6.1: thong bao nguoi choi ro rang (cau ca, boss ai/o dau), goi -104 co hinh
        KiemPvpPk.chay(); // 0.9.7 PvP xep hang (Elo chi trong tran ghep, chong nick phu), thuong mua, luat PK, diem moc nhiem vu
        KiemKenhChat.chay(); // 0.9.8 kenh chat: luat kenh, the gioi / bang / doi / rieng, chan, hop thu offline, client cu
        KiemDanhHieu.chay(); // 0.9.7 danh hieu: mo khoa, an "???", het han, mot danh hieu dang deo, tran chi so, sql/them_danh_hieu.sql
        KiemDoHiem.chay(); // 0.9.7 de xuat 4: mau do hiem MOT nguon (do_hiem.json -> server / client / web / SQL), tuong phan
        KiemThoiTiet.chay(); // 0.9.7 de xuat 8: thoi tiet 4 khung gio / vung, anh huong co tran, phu mau mua, goi -118
        KiemNgayCongDong.chay(); // 0.9.7 de xuat 8: ngay cong dong hang thang (lich nam), gio vang, tran ti le / EXP / shiny
        KiemCapNhatClient.chay(); // 0.9.7: cap nhat client - hai nguong, goi -115, hop 37 client cu co link
        KiemHocChieu.chay(); // de xuat 1: hoc chieu theo loai (data_learnset) + Diem huan luyen thay diem tiem nang
        KiemBoNgocCheTao.chay(); // bo trang bi cung bac, ngoc he, che tao, moc neo nang cap (bo-ngoc-bat)
        KiemBanDoKhoa.chay(); // 0.9.8: do khoa ban NPC gia thap + Huy
        KiemBatPokemon.chay(); // thu phuc Pokemon, Hop PC, Keo loai, Pokedex "Da bat" (bo-ngoc-bat)
        KiemTraoDoiPokemon.chay(); // ca the IV / tinh cach, chuyen doi, trao doi Pokemon nguyen tu (bat-trade)
        KiemNgamTay.chay(); // 0.9.8 ngam tay PvP: khong tu dong / khong tu nham, ban theo huong (hinh hoc, tuong, bu tre, hoi chieu / PP, client cu)
        KiemChoPokemon.chay(); // cho Pokemon: khung gia, thue, ky quy, mua nguyen tu (bat-trade)
        KiemDatTenPet.chay(); // 0.9.6 the dat ten Pokemon: nhan pet, ten, mot dong chi so co tran, khong cong don, nhat ky
        KiemLoaiDayDu.chay();
        KiemLoaiBoSung.chay(); // loai-bo-sung: mo ta 1.025 loai, shiny hoang da, neo phu kien goi tai, goi -125/-126 tuong thich
        KiemLoaiQuaiGame.chay(); // loai rieng tung con quai: hoi sinh, -125 sau lenh 4 chi client >= 908, fallback client cu // loai day du: 1.025 loai, chuoi tien hoa, chieu, noi song, sprite, chon loai hoi sinh, goi -125/-126
        KiemDoTrangPhuc.chay(); // do ho moi + tu do trang tri (do-trang-phuc)
        KiemParity.chay(); // 0.9.8: doi chieu 0.9.7 (du_lieu/parity_097.txt) - khong mat goi tin / menu / lenh GM / khoa / bang / HUD
        KiemDiaDiemNpc.chay(); // 0.9.8: buoc dia diem toi duoc (Arcanine gac cong), Dia diem / NPC chuoi chinh dung ban do
        KiemMucTieuNhiemVu.chay(); // sua-tien-do-nv: quai muc tieu sinh dung ban do, so dem khop chu (khoi sua_muc_tieu_nhiem_vu.sql)
        KiemTienDoNhiemVu.chay(); // sua-tien-do-nv: so Nhiem vu ghi ro muc tieu / ban do quai that / tra o dau, cua ai, Nhiem vu cua toi
        KiemDuongMenu.chay(); // sua-duong-gym: moi duong menu trong chu = nhan menu that; duong tat Thach dau Gym
        KiemDauTruong.chay(); // Dau truong Bao Boi 3v3 (D12): sat thuong, tat dinh, AI, diem, ghep, thuong, goi -109, SQL
        KiemChuyenLuyen.chay(); // Chuyen luyen (chu game 05/11/2026): dieu kien, bua, gia xu / da, khoa, du lieu SQL / web / docs
        KiemPk3v3.chay(); // PK 3 vs 3 buoi toi (chu game 05/11/2026): gio mo, bac cap, ghep + chong clone, thuong / chong cay, du lieu
        KiemQaToanGame.chay(); // vong QA toan game 23/10/2026: loi tim thay khi choi dau-cuoi bang bot (KichBanQaToanGame)
        KiemThuongLenCap.chay(); // 23/10/2026 bao loi: len cap qua nhiem vu / goi qua / nhay nhieu cap khong co diem tiem nang
        KiemLocTuCam.chay(); // de xuat QA #7: tu cam ngan chi khop ca tu (loc_tu_cam_ca_tu.sql)
        KiemDoiTenTrangBi.chay(); // de xuat QA #8: trang bi trung ten -> "<Ten> · Cap N" (doi_ten_trang_bi_cap.sql)
        KiemIconTrangBi.chay(); // icon trang bi dung do vat: moi o chi dung icon tap duyet (doi_icon_trang_bi.sql, IconTrangBi)
        Kiem.nhom("Danh quai trong lang: boss Gym cua minh (bao loi 24/10/2026 danh khong gay sat thuong)");
        real.KiemDanhQuaiTrongLang.BAO = new real.KiemDanhQuaiTrongLang.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        try {
            real.KiemDanhQuaiTrongLang.chay();
        } catch (final Exception e) {
            Kiem.bang("KiemDanhQuaiTrongLang khong nem loi", "", String.valueOf(e));
        }
        Kiem.nhom("Che tao nguyen tu (bao loi 03/11/2026 'che xong khong thay do ma xu da tru'): tao mon truoc, tru sau");
        real.KiemCheTaoNguyenTu.BAO = new real.KiemCheTaoNguyenTu.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemCheTaoNguyenTu.chay();
        Kiem.nhom("Chuyen luyen - luong game: tru nguyen tu, cap co / khong bua, chi so tinh lai, khoa lan, pity, tu choi");
        real.KiemChuyenLuyenGame.BAO = new real.KiemChuyenLuyenGame.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemChuyenLuyenGame.chay();
        Kiem.nhom("Quai thuong khong la boss (bao loi Violet 05/11/2026: Arcanine hoang da hien 'P3 4:58' nhu truong Gym)");
        real.KiemQuaiThuongKhongLaBoss.BAO = new real.KiemQuaiThuongKhongLaBoss.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemQuaiThuongKhongLaBoss.chay();
        Kiem.nhom("Khu mo huong dan (bao loi 05/11/2026 'vao map nay khong lam duoc gi'): nhac mac Cuoc, huong dan khi vao");
        real.KiemKhuMoHuongDan.BAO = new real.KiemKhuMoHuongDan.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemKhuMoHuongDan.chay();
        Kiem.nhom("Ve dich chuyen (Kecleon xu theo cap, Su dung -> lang / thanh pho da mo, dung 1 lan, cam Gym/PvP/pho ban/PK)");
        real.KiemVeDichChuyen.BAO = new real.KiemVeDichChuyen.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemVeDichChuyen.chay();
        Kiem.nhom("Thuoc cap cao bang xu (Chansey / Kecleon, mo cap 30 / 50 / 70, gia 0,05-5% thu nhap 1 gio, chi canh NPC ban thuoc)");
        real.KiemThuocCapCao.BAO = new real.KiemThuocCapCao.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemThuocCapCao.chay();
        Kiem.nhom("Ve lang mien phi (So tay, luot ngay + thang Gym, hoi chieu chung ve) + qua huy hieu Gym dau tien (1 Ve dich chuyen)");
        real.KiemVeLangMienPhi.BAO = new real.KiemVeLangMienPhi.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemVeLangMienPhi.chay();
        Kiem.nhom("PK 3 vs 3 buoi toi - luong game: hang cho / to doi, tran 3v3 tren san cup, bo cuoc / rot mang, luot / tai khoan, thuong + chong cay, bot, chot ngay / tuan");
        real.KiemPk3v3Game.BAO = new real.KiemPk3v3Game.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        real.KiemPk3v3Game.chay();
        Kiem.nhom("Gym mot tran / nhan vat: boss go khi thang / het gio / roi ban do / thoat, khong hoi sinh (bao loi 24/10/2026)");
        real.KiemGymMotTran.BAO = new real.KiemGymMotTran.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        try {
            real.KiemGymMotTran.chay();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.bang("KiemGymMotTran khong nem loi", "", String.valueOf(e));
        }
        Kiem.nhom("Ket tren thuyen 107 (ben kia khoa): ve ben mot lan, len tau bi tu choi, khong nap map lien tuc, cuu luc nap (bao loi 24/10/2026)");
        real.KiemKetTau.BAO = new real.KiemKetTau.Bao() {
            @Override
            public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                Kiem.bang(moTa, mongDoi, thucTe);
            }
        };
        try {
            real.KiemKetTau.chay();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.bang("KiemKetTau khong nem loi", "", String.valueOf(e));
        }
        KiemCap140.chay();
        KiemTinhNangThuong.chay();
        KiemTimDiemNhiemVu.chay(); // bao loi 29/10/2026: diem nhiem vu (dau sao, vi tri bang loi), nha Pewter, vuon - trung, ve tau
        KiemVuonBerry.chay(); // bat lai vuon 29/10/2026: o dat gieo / lon / thu hoach, luu ho so, buoc So tay lam_vuon, ngan sach
        KiemBuocHanhDong.chay(); // sua-buoc-hanh-dong: buoc noi chuyen hua hanh dong -> kiem that hoac chu that (khoi sua_buoc_hanh_dong.sql)
        KiemGiaVeTau.chay(); // bao loi 30/10/2026: ve tau ghi 15k mua 5k - gia hien o goi dang nhap = data_potion 20
        KiemVaoNhaNhiemVu.chay(); // bao loi 30/10/2026: vao nha (C05.2 Bao tang Pewter) xong ngay - client khong gui goi di chuyen trong nha; chu Nha tre
        KiemCanBangCap.chay(); // can bang cap 15-45: duong quai / gia do cua hang, #29 luon lam duoc, EXP thang Gym theo cap
        KiemPetDongHanh.chay(); // chu game 30/10/2026 "có": pet dong hanh mien phi 1 lan + mua bang xu (Nha tre dung duoc)
        KiemCanBangDong.chay(); // chu game 31/10/2026: chi so 5 dong theo bang thiet ke, hoi HP ngoai tran, sua do theo do ben mat, vu khi hong 30%
        KiemCmeTai.chay(); // tai 315 bot cb2 31/10/2026: ConcurrentModificationException hieu ung nhan vat / vong quai
        KiemTtkQuai.chay(); // chu game 31/10/2026 "fix" TTK: mo hinh TTK do cua hang +0/+6/+10 moi dong cap 10..100, Gym, tinh anh, khac he
        KiemDoiLoaiQuai.chay(); // do suc 31/10/2026: quai doi loai khi hoi sinh (he sinh thai) giu cap + chi so cua bai (Golem -> Krokorok 60 o CA-09.1)
        KiemQuaiNam2.chay(); // cong kiem bot 31/10/2026: quai nam 2 chi lech cap goc <= 2 (Mahogany 84 moc Wailord / Banette 91-92)
        KiemMuaDungNpc.chay(); // bot phu kin 31/10/2026: goi 24 chi mua mon cua NPC dang dung canh (giap Indigo khong mua o Pewter)
        KiemChanKhiGuc.chay(); // chu game 31/10/2026: dang guc khong dung NPC / di chuyen bang NPC / mua ban / giao dich / qua cong
        KiemGucLapLai.chay(); // cong kiem bot 31/10/2026: client guc van gui di chuyen / danh -> khong tinh them lan guc
        KiemBossLangThang.chay(); // chu game 31/10/2026: boss lang thang cu bi dong - chi danh nguoi da danh no
        KiemHoTong.chay(); // bao loi kazuhas4 31/10/2026 N-3.36: nhan hang o Tong tieu dau (bo dieu kien nuoc / luot), giao Kecleon, dem buoc, chi dan
        KiemHoTongThuongMienPhi.chay(); // chu game 31/10/2026: xe tu luot mien phi nhiem vu chi tra thuong nhiem vu
        KiemXeHangDiTheo.chay(); // bot cb2 31/10/2026: xe hang ket o goc nha (Pewter) - di theo vet nguoi choi
        KiemBaoLoi0411.chay(); // bao loi Zalo 02-04/11/2026: xoa nham nhiem vu, giao hang Kecleon, Ponyta quan, het ve chuong
        KiemDangNhapServer.chay(); // chu game 31/10/2026: login server HTBB thay CheckLoginSocket.jar (giao thuc, han, tat em, quay lui)
        KiemHlvAi.chay(); // chu game 31/10/2026: huan luyen vien AI (bot lap may chu) - tien to ten bot, luat chong lam phat, danh muc hlv_ai
        KiemDoiLuong.chay(); // chu game 31/10/2026: doi luong -> luong khoa / xu (ti gia chung), san luong thuong <-> xu, giao dich khong co luong
        final int ma = tongKet();
        // Khoi static cua Map tao thread nen JVM khong tu thoat - phai ep thoat.
        System.exit(ma);
    }

    private Kiem() {
    }
}
