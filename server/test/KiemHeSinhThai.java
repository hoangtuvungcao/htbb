import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.htbb.pokemon.BanDoSinhThai;
import com.htbb.pokemon.ChoSinhThai;
import com.htbb.pokemon.DaTienHoa;
import com.htbb.pokemon.DinhDangBanDo;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.HeSinhThai;
import com.htbb.pokemon.HeSinhThaiGame;
import com.htbb.pokemon.HoSoSinhThai;
import com.htbb.pokemon.KyNangSong;
import com.htbb.pokemon.MiniGameSinhThai;
import com.htbb.pokemon.NhaTre;
import com.htbb.pokemon.PetPokemon;
import com.htbb.pokemon.SinhThaiTheGioi;
import com.htbb.pokemon.SuuTamSinhThai;
import com.htbb.pokemon.ThongSoLauDai;
import com.htbb.pokemon.VatLieuSinhThai;

/**
 * He sinh thai game (docs/thiet-ke-game.md): co sv.he_sinh_thai, the gioi (thoi tiet / ngay
 * dem / mua / thay loai / bung phat), ky nang song, nha tre, cho, mini game, suu tam, ho so, ban do,
 * du lieu SQL sinh ra, diem moc trong src/real va ngan sach kinh te.
 */
public final class KiemHeSinhThai {

    private KiemHeSinhThai() {
    }

    public static void chay() {
        co();
        theGioi();
        thayLoai();
        kyNang();
        nganSach();
        nhaTre();
        cho();
        miniGame();
        suuTam();
        hoSo();
        banDo();
        hoCauCa();
        duLieuSql();
        diemMoc();
    }

    // ------------------------------------------------------------------ co

    static void co() {
        Kiem.nhom("He sinh thai - co sv.he_sinh_thai (mac dinh tat = hanh vi cu)");
        Kiem.bang("ten khoa", "sv.he_sinh_thai", HeSinhThai.KHOA);
        Kiem.dung("\"1\" = bat", HeSinhThai.docGiaTri(" 1 "));
        Kiem.sai("\"0\" = tat", HeSinhThai.docGiaTri("0"));
        Kiem.sai("null = tat", HeSinhThai.docGiaTri(null));
        Kiem.sai("server.ini cua repo khong bat (thieu dong = tat)", HeSinhThai.bat());
        final boolean cu = HeSinhThai.bat();
        HeSinhThai.dat(false);
        Kiem.sai("tat co: khiChat khong nuot chat", HeSinhThaiGame.khiChat(null, "hst"));
        Kiem.bang("tat co: sat thuong giu nguyen", Integer.valueOf(1234),
                Integer.valueOf(HeSinhThaiGame.satThuongThoiTiet(null, 0, 1234)));
        HeSinhThaiGame.khiHoiSinh(null);
        HeSinhThaiGame.khiHaQuai(null, null);
        HeSinhThaiGame.khiThuHoach(null, 0, 0, "Đã thu hoạch");
        Kiem.dung("tat co: cac diem moc khong nem loi", true);
        HeSinhThai.dat(cu);
        try {
            final String tmpl = new String(Files.readAllBytes(new File("../vps/server.ini.tmpl").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("vps/server.ini.tmpl co sv.he_sinh_thai=1 (production)", tmpl.contains("\nsv.he_sinh_thai=1\n"));
        } catch (final Exception e) {
            Kiem.dung("doc server.ini.tmpl: " + e, false);
        }
    }

    // ------------------------------------------------------------------ the gioi

    static void theGioi() {
        Kiem.nhom("He sinh thai - thoi tiet / ngay dem / mua (tat dinh)");
        Kiem.bang("cung ngay cung vung = cung thoi tiet", Integer.valueOf(SinhThaiTheGioi.thoiTiet(20261017, 4)),
                Integer.valueOf(SinhThaiTheGioi.thoiTiet(20261017, 4)));
        Kiem.bang("ma vung 'ho'", Integer.valueOf(SinhThaiTheGioi.V_HO), Integer.valueOf(SinhThaiTheGioi.maVung("ho")));
        Kiem.bang("ma vung la -> -1", Integer.valueOf(-1), Integer.valueOf(SinhThaiTheGioi.maVung("xyz")));
        final int[][] dem = new int[SinhThaiTheGioi.MA_VUNG.length][6];
        int doi = 0;
        for (int d = 0; d < 365; d++) {
            final java.util.Calendar c = java.util.Calendar.getInstance();
            c.clear();
            c.set(2027, 0, 1);
            c.add(java.util.Calendar.DAY_OF_YEAR, d);
            final int ngay = c.get(java.util.Calendar.YEAR) * 10000 + (c.get(java.util.Calendar.MONTH) + 1) * 100
                    + c.get(java.util.Calendar.DAY_OF_MONTH);
            for (int v = 0; v < dem.length; v++) {
                dem[v][SinhThaiTheGioi.thoiTiet(ngay, v)]++;
            }
            if (d > 0 && SinhThaiTheGioi.thoiTiet(ngay, SinhThaiTheGioi.V_HO) != SinhThaiTheGioi.thoiTiet(ngay - 1, SinhThaiTheGioi.V_HO)) {
                doi++;
            }
        }
        Kiem.bang("hang dong: luon quang (khong thoi tiet trong hang)", Integer.valueOf(365), Integer.valueOf(dem[SinhThaiTheGioi.V_HANG][0]));
        Kiem.bang("trong nha: luon quang", Integer.valueOf(365), Integer.valueOf(dem[SinhThaiTheGioi.V_TRONG_NHA][0]));
        Kiem.dung("vung tuyet: tuyet > 50% so ngay (" + dem[SinhThaiTheGioi.V_TUYET][SinhThaiTheGioi.TUYET] + ")",
                dem[SinhThaiTheGioi.V_TUYET][SinhThaiTheGioi.TUYET] > 182);
        Kiem.dung("sa mac: co bao cat (" + dem[SinhThaiTheGioi.V_SA_MAC][SinhThaiTheGioi.BAO_CAT] + " ngay)",
                dem[SinhThaiTheGioi.V_SA_MAC][SinhThaiTheGioi.BAO_CAT] > 60);
        Kiem.dung("ho: mua nhieu nhat sau quang (" + dem[SinhThaiTheGioi.V_HO][SinhThaiTheGioi.MUA] + " ngay)",
                dem[SinhThaiTheGioi.V_HO][SinhThaiTheGioi.MUA] > 90);
        Kiem.bang("ho: khong bao gio bao cat", Integer.valueOf(0), Integer.valueOf(dem[SinhThaiTheGioi.V_HO][SinhThaiTheGioi.BAO_CAT]));
        Kiem.dung("thoi tiet ho doi qua ngay (" + doi + " lan/nam)", doi > 100);
        Kiem.dung("dong: ho co tuyet", SinhThaiTheGioi.trongSoThoiTiet(SinhThaiTheGioi.V_HO, 12)[SinhThaiTheGioi.TUYET] > 0);
        Kiem.bang("ha: khong tuyet o ho", Integer.valueOf(0),
                Integer.valueOf(SinhThaiTheGioi.trongSoThoiTiet(SinhThaiTheGioi.V_HO, 6)[SinhThaiTheGioi.TUYET]));
        Kiem.dung("19h la dem", SinhThaiTheGioi.laDem(19));
        Kiem.dung("5h la dem", SinhThaiTheGioi.laDem(5));
        Kiem.sai("6h la ngay", SinhThaiTheGioi.laDem(6));
        Kiem.sai("18h la ngay", SinhThaiTheGioi.laDem(18));
        Kiem.bang("mua thang 1 = dong", Integer.valueOf(SinhThaiTheGioi.DONG), Integer.valueOf(SinhThaiTheGioi.mua(1)));
        Kiem.bang("mua thang 6 = ha", Integer.valueOf(SinhThaiTheGioi.HA), Integer.valueOf(SinhThaiTheGioi.mua(6)));
        Kiem.bang("mua: chieu Nuoc +10%", Integer.valueOf(110), Integer.valueOf(SinhThaiTheGioi.satThuongPhanTram(HePokemon.NUOC, SinhThaiTheGioi.MUA)));
        Kiem.bang("mua: chieu Lua -10%", Integer.valueOf(90), Integer.valueOf(SinhThaiTheGioi.satThuongPhanTram(HePokemon.LUA, SinhThaiTheGioi.MUA)));
        Kiem.bang("nang: chieu Lua +10%", Integer.valueOf(110), Integer.valueOf(SinhThaiTheGioi.satThuongPhanTram(HePokemon.LUA, SinhThaiTheGioi.NANG)));
        Kiem.bang("tuyet: chieu Bang +10%", Integer.valueOf(110), Integer.valueOf(SinhThaiTheGioi.satThuongPhanTram(HePokemon.BANG, SinhThaiTheGioi.TUYET)));
        Kiem.bang("quang: khong doi", Integer.valueOf(100), Integer.valueOf(SinhThaiTheGioi.satThuongPhanTram(HePokemon.NUOC, SinhThaiTheGioi.QUANG)));
        int lech = 0;
        for (int he = 0; he < HePokemon.SO_HE; he++) {
            for (int tt = 0; tt < 6; tt++) {
                lech = Math.max(lech, Math.abs(SinhThaiTheGioi.satThuongPhanTram(he, tt) - 100));
            }
        }
        Kiem.dung("moi he so thoi tiet trong +-" + HeSinhThai.THOI_TIET_SAT_THUONG_PHAN_TRAM + "% (nho)",
                lech <= HeSinhThai.THOI_TIET_SAT_THUONG_PHAN_TRAM);
        Kiem.bang("hieu ung client: mua = 0 (AnimateEffect.RAIN)", Integer.valueOf(0), Integer.valueOf(SinhThaiTheGioi.HIEU_UNG_CLIENT[SinhThaiTheGioi.MUA]));
        Kiem.bang("hieu ung client: tuyet = 2 (AnimateEffect.SNOW)", Integer.valueOf(2), Integer.valueOf(SinhThaiTheGioi.HIEU_UNG_CLIENT[SinhThaiTheGioi.TUYET]));
        Kiem.bang("hieu ung client: suong mu = 6 (moc client moi)", Integer.valueOf(6), Integer.valueOf(SinhThaiTheGioi.HIEU_UNG_CLIENT[SinhThaiTheGioi.SUONG_MU]));
        Kiem.bang("hieu ung client: nang gat = 7 (moc client moi)", Integer.valueOf(7), Integer.valueOf(SinhThaiTheGioi.HIEU_UNG_CLIENT[SinhThaiTheGioi.NANG]));
        Kiem.bang("hieu ung client: quang = -1 (tat)", Integer.valueOf(-1), Integer.valueOf(SinhThaiTheGioi.HIEU_UNG_CLIENT[SinhThaiTheGioi.QUANG]));
        Kiem.dung("id hieu ung danh hieu sinh thai 9600.. khong dung dai 8700..8999 / skin 13000..",
                HeSinhThai.ID_EFF_DANH_HIEU_DAU > 8999 && HeSinhThai.ID_EFF_DANH_HIEU_DAU + HoSoSinhThai.DANH_HIEU.length <= 9616);

        Kiem.nhom("He sinh thai - bung phat");
        final List<int[]> ds = new ArrayList<int[]>();
        ds.add(new int[]{1, 2});
        ds.add(new int[]{3, 4});
        ds.add(new int[]{5, 6});
        Kiem.dung("cung (ngay, khung) = cung cap", SinhThaiTheGioi.bungPhat(20261017, 1, ds) == SinhThaiTheGioi.bungPhat(20261017, 1, ds));
        Kiem.dung("danh sach rong -> null", SinhThaiTheGioi.bungPhat(20261017, 1, new ArrayList<int[]>()) == null);
        Kiem.bang("khung 17h", Integer.valueOf(1), Integer.valueOf(SinhThaiTheGioi.khungBungPhat(17)));
        Kiem.bang("13h khong co khung", Integer.valueOf(-1), Integer.valueOf(SinhThaiTheGioi.khungBungPhat(13)));
        final Set<Integer> chon = new HashSet<Integer>();
        for (int n = 20261001; n <= 20261028; n++) {
            chon.add(SinhThaiTheGioi.bungPhat(n, 0, ds)[0]);
        }
        Kiem.dung("bung phat xoay vong qua cac tuyen", chon.size() == 3);
        final Random r = new Random(7);
        int doiBp = 0;
        for (int i = 0; i < 10000; i++) {
            doiBp += SinhThaiTheGioi.doiThanhBungPhat(1, 2, r) ? 1 : 0;
        }
        Kiem.dung("ti le doi thanh loai bung phat ~" + HeSinhThai.TI_LE_BUNG_PHAT_PHAN_TRAM + "% (" + doiBp / 100.0 + "%)",
                Math.abs(doiBp / 100.0 - HeSinhThai.TI_LE_BUNG_PHAT_PHAN_TRAM) < 2);
        Kiem.sai("loai goc = loai bung phat: khong doi", SinhThaiTheGioi.doiThanhBungPhat(2, 2, r));
    }

    static void thayLoai() {
        Kiem.nhom("He sinh thai - thay loai khi hoi sinh (khong doi so luong / vi tri)");
        final List<SinhThaiTheGioi.UngVien> ds = new ArrayList<SinhThaiTheGioi.UngVien>();
        ds.add(new SinhThaiTheGioi.UngVien(10, HePokemon.NUOC, -1, 20, false, -1, -1, 0xFFF));
        ds.add(new SinhThaiTheGioi.UngVien(11, HePokemon.LUA, -1, 20, false, -1, -1, 0xFFF));
        ds.add(new SinhThaiTheGioi.UngVien(12, HePokemon.THUONG, -1, 20, false, -1, -1, 1)); // chi thang 1
        ds.add(new SinhThaiTheGioi.UngVien(13, HePokemon.MA, -1, 2, true, SinhThaiTheGioi.SUONG_MU, 1, 0xFFF));
        final Random r = new Random(42);
        final Map<Integer, Integer> dem = new HashMap<Integer, Integer>();
        final int n = 200000;
        for (int i = 0; i < n; i++) {
            dem.merge(SinhThaiTheGioi.chonThayThe(ds, 0, SinhThaiTheGioi.MUA, 12, 6, r), 1, Integer::sum);
        }
        final double thay = 100.0 * (n - dem.getOrDefault(-1, 0)) / n;
        Kiem.dung("ti le thay ~" + HeSinhThai.TI_LE_THAY_PHAN_TRAM + "% (" + String.format("%.2f", thay) + "%)",
                Math.abs(thay - HeSinhThai.TI_LE_THAY_PHAN_TRAM) < 0.5);
        Kiem.dung("mua: loai Nuoc ra nhieu hon loai Lua (x" + HeSinhThai.HE_SO_SINH_HOP_THOI_TIET + ")",
                dem.getOrDefault(10, 0) > 2.5 * dem.getOrDefault(11, 1));
        Kiem.bang("loai di cu vang mat thang 6", Integer.valueOf(0), dem.getOrDefault(12, 0));
        Kiem.bang("quai hiem chi trong suong mu ban dem: troi mua -> 0", Integer.valueOf(0), dem.getOrDefault(13, 0));
        int hiem = 0;
        for (int i = 0; i < n; i++) {
            if (SinhThaiTheGioi.chonThayThe(ds, 0, SinhThaiTheGioi.SUONG_MU, 23, 6, r) == 13) {
                hiem++;
            }
        }
        Kiem.dung("quai hiem trong dung dieu kien: hiem (" + hiem + "/" + n + ", < 1%)", hiem > 0 && hiem < n / 100);
        Kiem.bang("danh sach rong -> giu loai goc", Integer.valueOf(-1),
                Integer.valueOf(SinhThaiTheGioi.chonThayThe(new ArrayList<SinhThaiTheGioi.UngVien>(), 0, 0, 12, 6, r)));
        Kiem.bang("trong so hieu dung: dem nhan he Ma", Integer.valueOf(20 * HeSinhThai.HE_SO_SINH_HOP_THOI_TIET),
                Integer.valueOf(SinhThaiTheGioi.trongSoHieuDung(new SinhThaiTheGioi.UngVien(1, HePokemon.MA, -1, 20, false, -1, -1, 0xFFF),
                        SinhThaiTheGioi.QUANG, 22, 6)));
    }

    // ------------------------------------------------------------------ ky nang song

    static void kyNang() {
        Kiem.nhom("He sinh thai - ky nang song (cap 1..50, the luc)");
        boolean tang = true;
        for (int c = 1; c < HeSinhThai.CAP_KY_NANG_TOI_DA - 1; c++) {
            tang &= KyNangSong.expLenCap(c + 1) > KyNangSong.expLenCap(c);
        }
        Kiem.dung("EXP len cap tang dan", tang);
        final long tong = KyNangSong.tongExpDenCap(HeSinhThai.CAP_KY_NANG_TOI_DA);
        final double ngay = tong / (HeSinhThai.THE_LUC_NGAY / (double) HeSinhThai.TL_CAU_CA * HeSinhThai.EXP_MOI_LAN * 1.3);
        Kiem.dung("tong EXP 1->50 = " + tong + " (muc tieu dai han: 60-400 ngay choi het the luc, ~" + Math.round(ngay) + ")",
                ngay >= 60 && ngay <= 400);
        Kiem.bang("cap tu EXP 0", Integer.valueOf(1), Integer.valueOf(KyNangSong.capTuExp(0)));
        Kiem.bang("cap tu EXP du cap 10", Integer.valueOf(10), Integer.valueOf(KyNangSong.capTuExp(KyNangSong.tongExpDenCap(10))));
        Kiem.bang("cap tran 50", Integer.valueOf(50), Integer.valueOf(KyNangSong.capTuExp(Long.MAX_VALUE / 4)));
        Kiem.bang("the luc: cung ngay giu nguyen", Integer.valueOf(37), Integer.valueOf(KyNangSong.theLucNgayMoi(37, 0)));
        Kiem.bang("the luc: ngay ke tiep +100", Integer.valueOf(137), Integer.valueOf(KyNangSong.theLucNgayMoi(37, 1)));
        Kiem.bang("the luc: tran tich luy", Integer.valueOf(HeSinhThai.THE_LUC_TOI_DA), Integer.valueOf(KyNangSong.theLucNgayMoi(190, 1)));
        Kiem.bang("the luc: nghi 2 ngay = day", Integer.valueOf(HeSinhThai.THE_LUC_TOI_DA), Integer.valueOf(KyNangSong.theLucNgayMoi(0, 3)));
        for (final int[] d : KyNangSong.BANG_CAU_CA) {
            Kiem.dung("bang cau ca: vat lieu " + d[0] + " co trong danh muc", VatLieuSinhThai.coMa(d[0]));
        }
        for (final int[] d : KyNangSong.BANG_DAO) {
            Kiem.dung("bang dao: vat lieu " + d[0] + " co trong danh muc", VatLieuSinhThai.coMa(d[0]));
        }
        final Set<Integer> idCt = new HashSet<Integer>();
        for (int i = 0; i < KyNangSong.CONG_THUC.length; i++) {
            final KyNangSong.CongThuc ct = KyNangSong.CONG_THUC[i];
            Kiem.bang("cong thuc " + ct.ten + ": id = chi so", Integer.valueOf(i), Integer.valueOf(ct.id));
            idCt.add(ct.id);
            for (final int[] nl : ct.nguyenLieu) {
                Kiem.dung("cong thuc " + ct.ten + ": nguyen lieu " + nl[0] + " ton tai", VatLieuSinhThai.coMa(nl[0]) && nl[1] > 0);
            }
            Kiem.dung("cong thuc " + ct.ten + ": co dau ra", ct.ra > 0 && VatLieuSinhThai.coMa(ct.ra)
                    || ct.ra == 0 && ct.thuong != null && ct.thuong.startsWith("ITEM:"));
        }
        final Set<Integer> daTh = new HashSet<Integer>();
        for (final int id : DaTienHoa.ID_ITEM) {
            daTh.add(id);
        }
        for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
            if (ct.thuong != null) {
                final int id = Integer.parseInt(ct.thuong.split(":")[1]);
                Kiem.dung("thuong " + ct.ten + " (" + id + ") la da tien hoa that hoac item pet Pokemon",
                        daTh.contains(id) || PetPokemon.dex(id) > 0);
            }
        }
        for (int ma = 51; ma <= 57; ma++) {
            Kiem.bang("manh " + ma + " -> da tien hoa", Integer.valueOf(DaTienHoa.ID_ITEM[ma - 51]), Integer.valueOf(VatLieuSinhThai.daTuManh(ma)));
        }
        for (int ma = 90; ma <= 94; ma++) {
            Kiem.dung("thuoc " + ma + " doi ra binh thuoc that (PT:)", KyNangSong.thuongDungThuoc(ma) != null
                    && KyNangSong.thuongDungThuoc(ma).startsWith("PT:"));
        }
        Kiem.bang("thanh pham lay bac THAP nhat", Integer.valueOf(2), Integer.valueOf(KyNangSong.bacThanhPham(new int[]{4, 2, 5})));
        final Random r = new Random(1);
        final Set<Integer> raCa = new HashSet<Integer>();
        for (int i = 0; i < 20000; i++) {
            final KyNangSong.KetQua k = KyNangSong.cauCa(KyNangSong.NUOC_NGOT, 1, false, r);
            raCa.add(k.ma);
            if (k.ma == 9 || k.ma == 13) {
                Kiem.dung("nuoc ngot khong ra ca bien", false);
                break;
            }
        }
        Kiem.dung("cap 1 khong cau duoc Dratini (can cap 40)", !raCa.contains(19));
        int dra = 0;
        for (int i = 0; i < 200000; i++) {
            dra += KyNangSong.cauCa(KyNangSong.NUOC_NGOT, 50, true, r).ma == 19 ? 1 : 0;
        }
        Kiem.dung("cap 50 + moi thom: Dratini hiem nhung co (" + dra + "/200000)", dra > 0 && dra < 4000);
    }

    // ------------------------------------------------------------------ ngan sach kinh te

    static void nganSach() {
        Kiem.nhom("He sinh thai - ngan sach (THIET_KE_LAU_DAI muc 2: khong lam phat)");
        final double evCau1 = KyNangSong.kyVongCauCa(KyNangSong.NUOC_NGOT, 1, false);
        final double evCau50 = KyNangSong.kyVongCauCa(KyNangSong.NUOC_MAN, 50, true);
        final double evDao = KyNangSong.kyVongDao(50);
        Kiem.dung("ky vong 1 lan cau cap 1 = " + Math.round(evCau1 * 10) / 10.0 + "‰ gio (5..20)", evCau1 >= 5 && evCau1 <= 20);
        Kiem.dung("ky vong 1 lan cau cap 50 + moi thom = " + Math.round(evCau50) + "‰ gio (<= 40)", evCau50 <= 40);
        final double moiTheLuc = Math.max(evCau50 / HeSinhThai.TL_CAU_CA, evDao / HeSinhThai.TL_DAO);
        final double gioHang = moiTheLuc * HeSinhThai.THE_LUC_TOI_DA / 1000.0;
        Kiem.dung("gia tri hang toi da / ngay (the luc tich 200, cap 50) = " + Math.round(gioHang * 100) / 100.0
                + " gio thu nhap (<= 4)", gioHang <= 4.0);
        final double phiCau = HeSinhThai.THE_LUC_NGAY / (double) HeSinhThai.TL_CAU_CA * HeSinhThai.PHI_MOI_CAU_PHAN_NGHIN;
        final double phiDao = HeSinhThai.THE_LUC_NGAY / (double) HeSinhThai.TL_DAO * HeSinhThai.PHI_XENG_PHAN_NGHIN;
        Kiem.dung("phi moi cau 1 ngay (" + phiCau + "‰) >= tran NPC mua lai (" + HeSinhThai.TRAN_BAN_NPC_NGAY_PHAN_NGHIN
                + "‰): cau roi ban NPC khong sinh xu rong", phiCau >= HeSinhThai.TRAN_BAN_NPC_NGAY_PHAN_NGHIN);
        Kiem.dung("phi xeng 1 ngay (" + phiDao + "‰) >= tran NPC mua lai", phiDao >= HeSinhThai.TRAN_BAN_NPC_NGAY_PHAN_NGHIN);
        Kiem.dung("tran NPC mua lai <= 0,1 gio thu nhap / ngay", HeSinhThai.TRAN_BAN_NPC_NGAY_PHAN_NGHIN <= 100);
        Kiem.bang("thue + phi dang = thue cho thiet ke dai han (6% + 1%)",
                Integer.valueOf(ThongSoLauDai.THUE_CHO_PHAN_TRAM + ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM),
                Integer.valueOf(HeSinhThai.THUE_CHO_PHAN_TRAM + HeSinhThai.PHI_DANG_PHAN_TRAM));
        Kiem.dung("NPC khong mua trang tri / trung", VatLieuSinhThai.giaNpc(110, 3) == 0 && VatLieuSinhThai.giaNpc(VatLieuSinhThai.TRUNG, 3) == 0);
        Kiem.bang("NPC mua = 40% gia tham chieu", Long.valueOf(VatLieuSinhThai.giaXu(36, 2) * 40 / 100), Long.valueOf(VatLieuSinhThai.giaNpc(36, 2)));
        Kiem.dung("gia tham chieu tang theo bac (gio choi, khong so co dinh)", VatLieuSinhThai.giaXu(19, 5) > VatLieuSinhThai.giaXu(19, 1));
        Kiem.bang("bac theo cap 1 / 20 / 85", "1/2/5", VatLieuSinhThai.bacTheoCap(1) + "/" + VatLieuSinhThai.bacTheoCap(20) + "/"
                + VatLieuSinhThai.bacTheoCap(85));
        final int[] ma = VatLieuSinhThai.tatCa();
        final Set<String> ten = new HashSet<String>();
        for (final int m : ma) {
            ten.add(VatLieuSinhThai.ten(m));
        }
        Kiem.bang("ten vat lieu khong trung", Integer.valueOf(ma.length), Integer.valueOf(ten.size()));
        Kiem.dung("khoa tui: ma*10+bac", VatLieuSinhThai.khoa(36, 3) == 363 && VatLieuSinhThai.maCuaKhoa(363) == 36
                && VatLieuSinhThai.bacCuaKhoa(363) == 3);
        Kiem.dung("che thuoc co tran me/ngay (khong thay het ho hut thuoc NPC)", KyNangSong.ME_THUOC_TOI_DA_NGAY <= 5);
    }

    // ------------------------------------------------------------------ nha tre

    static void nhaTre() {
        Kiem.nhom("He sinh thai - nha tre & trung");
        final NhaTre.Trung t = new NhaTre.Trung(147, 31, 30, 29, true, 123);
        final NhaTre.Trung t2 = NhaTre.Trung.doc(t.chuoi());
        Kiem.bang("trung: chuoi hoa / doc lai", t.chuoi(), t2 == null ? null : t2.chuoi());
        Kiem.dung("trung: chuoi hong -> null", NhaTre.Trung.doc("abc") == null && NhaTre.Trung.doc("9999.1.1.1.0.0") == null);
        Kiem.bang("IV ngoai khoang bi kep", Integer.valueOf(31), Integer.valueOf(NhaTre.Trung.doc("147.99.0.0.0.0").ivHp));
        Kiem.bang("pham chat S", "S", NhaTre.phamChat(84));
        Kiem.bang("pham chat C", "C", NhaTre.phamChat(49));
        Kiem.bang("trung S Dratini -> pet cap 2 (695)", "ITEM:695:1", NhaTre.thuongNo(t));
        Kiem.bang("trung thuong Dratini -> pet cap 1 (694)", "ITEM:694:1", NhaTre.thuongNo(new NhaTre.Trung(147, 1, 1, 1, false, 0)));
        Kiem.bang("Dragonite (149) -> trung Dratini", Integer.valueOf(147), Integer.valueOf(NhaTre.loaiGoc(149)));
        Kiem.bang("Crobat (169) -> trung Zubat", Integer.valueOf(41), Integer.valueOf(NhaTre.loaiGoc(169)));
        Kiem.bang("loai khong co trung -> 0", Integer.valueOf(0), Integer.valueOf(NhaTre.loaiGoc(25)));
        for (final int[] l : NhaTre.LOAI_TRUNG) {
            Kiem.bang("trung " + SuuTamSinhThai.tenLoai(l[0]) + ": item pet cap 1 dung loai", Integer.valueOf(l[0]), Integer.valueOf(PetPokemon.dex(l[1])));
            if (l[2] > 0) {
                Kiem.bang("trung " + SuuTamSinhThai.tenLoai(l[0]) + ": item cap 2 cung dong", Integer.valueOf(NhaTre.loaiGoc(l[0])),
                        Integer.valueOf(NhaTre.loaiGoc(PetPokemon.dex(l[2]))));
            }
            Kiem.dung("trung " + SuuTamSinhThai.tenLoai(l[0]) + ": buoc no 200..1000", l[3] >= 200 && l[3] <= 1000);
        }
        final Random r = new Random(3);
        boolean trongKhoang = true;
        int lonHon = 0;
        for (int i = 0; i < 5000; i++) {
            final int[] con = NhaTre.diTruyen(new int[]{31, 31, 31}, new int[]{0, 0, 0}, r);
            for (final int v : con) {
                trongKhoang &= v >= 0 && v <= 31;
            }
            lonHon += con[0] >= 28 ? 1 : 0;
        }
        Kiem.dung("IV con trong 0..31", trongKhoang);
        Kiem.dung("IV di truyen tu cha/me (~50% lay ben cao)", lonHon > 2000 && lonHon < 3000);
        Kiem.dung("pity: trung thu " + HeSinhThai.SHINY_TRUNG_PITY + " chac chan shiny",
                NhaTre.laShiny(HeSinhThai.SHINY_TRUNG_PITY - 1, new Random(9)));
        int sh = 0;
        for (int i = 0; i < 100000; i++) {
            sh += NhaTre.laShiny(0, r) ? 1 : 0;
        }
        Kiem.dung("shiny ~1/" + HeSinhThai.SHINY_TRUNG_MAU + " (" + sh + "/100000)", sh > 100 && sh < 300);
        Kiem.bang("de trung: 300 buoc -> 1", Integer.valueOf(1), Integer.valueOf(NhaTre.soTrungMoi(299, 1, 0)));
        Kiem.bang("de trung: tran ngay", Integer.valueOf(0), Integer.valueOf(NhaTre.soTrungMoi(299, 1, HeSinhThai.TRUNG_TOI_DA_NGAY)));
        final int[] a = NhaTre.ivHoangDa(5, 147);
        Kiem.dung("IV hoang da tat dinh, 0..15", a[0] == NhaTre.ivHoangDa(5, 147)[0] && a[0] <= 15 && a[1] <= 15 && a[2] <= 15);
    }

    // ------------------------------------------------------------------ cho

    static void cho() {
        Kiem.nhom("He sinh thai - cho sinh thai / don dat hang");
        Kiem.bang("thue 6% cua 10000", Long.valueOf(600), Long.valueOf(ChoSinhThai.thue(10000)));
        Kiem.bang("phi dang 1% (toi thieu 1)", Long.valueOf(1), Long.valueOf(ChoSinhThai.phiDang(5)));
        Kiem.bang("nguoi ban nhan 94%", Long.valueOf(9400), Long.valueOf(ChoSinhThai.nguoiBanNhan(10000)));
        Kiem.bang("ky quy = gia x so + 1%", Long.valueOf(1010), Long.valueOf(ChoSinhThai.kyQuy(100, 10)));
        Kiem.bang("huy don hoan phan chua giao", Long.valueOf(700), Long.valueOf(ChoSinhThai.hoanHuyDon(100, 10, 3)));
        final long tran = ChoSinhThai.tranBanNpcNgay(30);
        Kiem.bang("tran NPC = 0,1 gio thu nhap", Long.valueOf(ThongSoLauDai.xuMoiGio(30) / 10), Long.valueOf(tran));
        final long[] kq = ChoSinhThai.npcMua(36, 2, 100000, 0, 30);
        Kiem.dung("NPC mua khong vuot tran ngay", kq[1] <= tran && kq[0] > 0);
        Kiem.bang("da het tran -> 0", Long.valueOf(0), Long.valueOf(ChoSinhThai.npcMua(36, 2, 5, tran, 30)[0]));
        Kiem.dung("gia muc x3 hop le, x5+ khong", ChoSinhThai.giaHopLe(36, 2, ChoSinhThai.giaTheoMuc(36, 2, 3))
                && !ChoSinhThai.giaHopLe(36, 2, VatLieuSinhThai.giaXu(36, 2) * 6));
        Kiem.dung("muc gia toi da <= tran x" + HeSinhThai.GIA_TOI_DA_LAN,
                ChoSinhThai.MUC_GIA_PHAN_TRAM[ChoSinhThai.MUC_GIA_PHAN_TRAM.length - 1] <= HeSinhThai.GIA_TOI_DA_LAN * 100);
    }

    // ------------------------------------------------------------------ mini game

    static void miniGame() {
        Kiem.nhom("He sinh thai - do vui, dua, giai dau");
        Kiem.bang("chuan hoa", "mrmime", MiniGameSinhThai.chuanHoa(" Mr. Mime "));
        Kiem.bang("chuan hoa bo dau", "dalua", MiniGameSinhThai.chuanHoa("Đá Lửa"));
        Kiem.dung("tra loi dung khong phan biet hoa/dau cach", MiniGameSinhThai.dung("pi ka chu", "Pikachu"));
        Kiem.sai("tra loi rong khong dung", MiniGameSinhThai.dung("   ", "Pikachu"));
        Kiem.sai("tra loi sai", MiniGameSinhThai.dung("Raichu", "Pikachu"));
        final String g = MiniGameSinhThai.goiY("Pikachu", HePokemon.DIEN, -1);
        Kiem.dung("goi y co he, so chu, chu dau: " + g, g.contains("7 chữ") && g.contains("chữ P") && g.contains(HePokemon.ten(HePokemon.DIEN)));
        Kiem.dung("cau hoi tat dinh", MiniGameSinhThai.chonCauHoi(20261017, 1, 50) == MiniGameSinhThai.chonCauHoi(20261017, 1, 50));
        final int[][] cap = MiniGameSinhThai.capDauVong1(new int[]{1, 2, 3, 4, 5});
        Kiem.bang("5 nguoi -> 4 cap (3 mien dau)", Integer.valueOf(4), Integer.valueOf(cap.length));
        Kiem.bang("hat giong 1 gap mien dau", Integer.valueOf(-1), Integer.valueOf(cap[0][1]));
        Kiem.bang("vong sau: 3 nguoi thang -> 2 cap", Integer.valueOf(2), Integer.valueOf(MiniGameSinhThai.vongSau(new int[]{1, 2, 3}).length));
        final boolean[] di = new boolean[40 * 20];
        for (int y = 5; y < 15; y++) {
            for (int x = 0; x < 40; x++) {
                di[y * 40 + x] = true;
            }
        }
        final int[][] moc = MiniGameSinhThai.chonMocDua(di, 40, 20, 5, 1);
        Kiem.bang("dua: 5 moc", Integer.valueOf(5), Integer.valueOf(moc.length));
        boolean tren = true, traiPhai = true;
        for (int i = 0; i < moc.length; i++) {
            tren &= di[moc[i][1] * 40 + moc[i][0]];
            if (i > 0) {
                traiPhai &= moc[i][0] > moc[i - 1][0];
            }
        }
        Kiem.dung("dua: moc nam tren o di duoc", tren);
        Kiem.dung("dua: moc trai -> phai", traiPhai);
        Kiem.bang("dua: toi moc -> moc ke", Integer.valueOf(1), Integer.valueOf(MiniGameSinhThai.capNhatMoc(moc, 0, moc[0][0] + 2, moc[0][1])));
        Kiem.bang("dua: xa moc -> giu", Integer.valueOf(0), Integer.valueOf(MiniGameSinhThai.capNhatMoc(moc, 0, moc[0][0] + 9, moc[0][1])));
    }

    // ------------------------------------------------------------------ suu tam

    static void suuTam() {
        Kiem.nhom("He sinh thai - nghien cuu, album, vung");
        final int ad = SuuTamSinhThai.apDung(false, false, true, HePokemon.NUOC, -1, true);
        Kiem.dung("quai Nuoc: dem + thoi tiet + hiem", (ad & SuuTamSinhThai.NV_DEM) != 0 && (ad & SuuTamSinhThai.NV_THOI_TIET) != 0
                && (ad & SuuTamSinhThai.NV_HIEM) != 0);
        Kiem.bang("quai Giac dau: khong co nhiem vu thoi tiet", Integer.valueOf(0),
                Integer.valueOf(SuuTamSinhThai.apDung(false, false, true, HePokemon.GIAC_DAU, -1, false) & SuuTamSinhThai.NV_THOI_TIET));
        Kiem.dung("ca: chi can cau duoc", SuuTamSinhThai.xongLoai(SuuTamSinhThai.NV_CAU, SuuTamSinhThai.NV_CAU));
        Kiem.sai("quai: 1 nhiem vu chua xong", SuuTamSinhThai.xongLoai(SuuTamSinhThai.NV_DEM, ad));
        Kiem.dung("quai: 2 nhiem vu la xong", SuuTamSinhThai.xongLoai(SuuTamSinhThai.NV_DEM | SuuTamSinhThai.NV_HIEM, ad));
        Kiem.sai("khong ap dung -> khong xong", SuuTamSinhThai.xongLoai(63, 0));
        Kiem.bang("moc album 9 -> 10", Integer.valueOf(0), Integer.valueOf(SuuTamSinhThai.mocVuaDat(9, 10)));
        Kiem.bang("moc album 11 -> 12: khong", Integer.valueOf(-1), Integer.valueOf(SuuTamSinhThai.mocVuaDat(11, 12)));
        Kiem.bang("chi so vung co tran " + HeSinhThai.CHI_SO_VUNG_TRAN_PHAN_NGHIN + "‰", Integer.valueOf(HeSinhThai.CHI_SO_VUNG_TRAN_PHAN_NGHIN),
                Integer.valueOf(SuuTamSinhThai.chiSoVinhVien(14)));
        Kiem.dung("chi so vinh vien tran <= 3% (trong ngan sach suc manh, tinh thong +10%)", HeSinhThai.CHI_SO_VUNG_TRAN_PHAN_NGHIN <= 30);
        Kiem.bang("ten ca", "Magikarp", SuuTamSinhThai.tenLoai(129));
        Kiem.dung("mo ta anh", SuuTamSinhThai.moTaAnh(SuuTamSinhThai.anh(129, 2, 2, 20261017)).contains("Magikarp"));
        Kiem.dung("danh hieu album nam trong danh muc ho so", HoSoSinhThai.DANH_HIEU.length >= SuuTamSinhThai.DANH_HIEU_ALBUM.length
                && java.util.Arrays.asList(HoSoSinhThai.DANH_HIEU).containsAll(java.util.Arrays.asList(SuuTamSinhThai.DANH_HIEU_ALBUM)));
        Kiem.dung("so vung <= 31 (bit vungXong)", SinhThaiTheGioi.MA_VUNG.length <= 31);
    }

    // ------------------------------------------------------------------ ho so

    static void hoSo() {
        Kiem.nhom("He sinh thai - ho so nhan vat (chuoi hoa)");
        final HoSoSinhThai h = new HoSoSinhThai();
        h.ngay = 20261017;
        h.theLuc = 77;
        h.expKyNang[2] = 12345;
        h.them(VatLieuSinhThai.khoa(36, 2), 5);
        h.them(VatLieuSinhThai.khoa(36, 1), 3);
        h.trung.add(new NhaTre.Trung(41, 1, 2, 3, false, 10));
        h.nhaTre1 = 42;
        h.buocNhaTre = 99;
        h.giong.put(41, new int[]{10, 20, 30});
        h.ghiNghienCuu(129, SuuTamSinhThai.NV_CAU);
        h.themAnh(SuuTamSinhThai.anh(129, 2, 2, 20261017));
        h.vungXong = 5;
        h.trangTri.put(110, 2);
        h.themDanhHieu("Nhà khảo cổ");
        h.themDanhHieu("khong co trong danh muc");
        h.buffMoiThom = 4;
        h.doVuiDung = 3;
        final String s = h.chuoi();
        final HoSoSinhThai d = HoSoSinhThai.doc(s);
        Kiem.bang("chuoi hoa -> doc -> chuoi hoa giong het", s, d.chuoi());
        Kiem.dung("chuoi khong co ky tu nguoi choi (chi so va , : . ~ | =)", s.substring(2).matches("[0-9,:.~|=a-z]*"));
        Kiem.bang("the luc", Integer.valueOf(77), Integer.valueOf(d.theLuc));
        Kiem.bang("danh hieu la bi bo", Integer.valueOf(1), Integer.valueOf(d.danhHieu.size()));
        Kiem.bang("chuoi hong -> ho so moi", Integer.valueOf(HeSinhThai.THE_LUC_TOI_DA), Integer.valueOf(HoSoSinhThai.doc("rac|||").theLuc));
        Kiem.bang("null -> ho so moi", Integer.valueOf(0), Integer.valueOf(HoSoSinhThai.doc(null).tui.size()));
        Kiem.bang("lay theo ma: uu tien bac thap", Integer.valueOf(1), Integer.valueOf(d.layTheoMa(36, 4)));
        Kiem.bang("con lai 4 cai bac 2", Integer.valueOf(4), Integer.valueOf(d.so(VatLieuSinhThai.khoa(36, 2))));
        Kiem.bang("khong du -> khong lay", Integer.valueOf(0), Integer.valueOf(d.layTheoMa(36, 50)));
        Kiem.sai("bot qua so -> false", d.bot(VatLieuSinhThai.khoa(36, 2), 99));
        Kiem.dung("sang ngay moi: dat lai dem", d.sangNgay(20261018) && d.theLuc == 177 && d.banNpcHomNay == 0);
        Kiem.sai("cung ngay: khong dat lai", d.sangNgay(20261018));
        Kiem.sai("ghi nghien cuu lan 2 -> false", d.ghiNghienCuu(129, SuuTamSinhThai.NV_CAU));
        final HoSoSinhThai e = new HoSoSinhThai();
        e.buffSup = 1;
        Kiem.bang("buff sup: +50% exp", Long.valueOf(15), Long.valueOf(e.congExp(0, 10) >= 0 ? e.expKyNang[0] : -1));
    }

    // ------------------------------------------------------------------ ban do

    static void banDo() {
        Kiem.nhom("He sinh thai - mep nuoc / diem dao (bo tile moi)");
        try {
            final Map<String, Set<Integer>> nuoc = new HashMap<String, Set<Integer>>();
            for (final String d : Files.readAllLines(new File("../du_lieu/bang_tile_moi.csv").toPath(), StandardCharsets.UTF_8)) {
                final String[] c = d.split(",");
                if (c.length < 4 || "bo_tile".equals(c[0])) {
                    continue;
                }
                if ((c[2].startsWith("W") || "sen".equals(c[2])) && "2".equals(c[3])) {
                    nuoc.computeIfAbsent(c[0], (k) -> new HashSet<Integer>()).add(Integer.parseInt(c[1]));
                }
            }
            for (final String bo : new String[]{"t", "t_hang", "t_thanh"}) {
                final Set<Integer> java = new HashSet<Integer>();
                for (final int i : BanDoSinhThai.tileNuoc(bo)) {
                    java.add(i);
                }
                Kiem.bang("tile nuoc bo " + bo + " khop bang_tile_moi.csv", nuoc.getOrDefault(bo, new HashSet<Integer>()), java);
            }
        } catch (final Exception e) {
            Kiem.dung("doc bang_tile_moi.csv: " + e, false);
        }
        try {
            final File f = new File("cMap/3");
            final DinhDangBanDo bd = DinhDangBanDo.doc(Files.readAllBytes(f.toPath()));
            final boolean[] n = BanDoSinhThai.luoiNuoc(bd, DinhDangBanDo.boTile(3));
            Kiem.dung("map 3 (Vermilion - ho lon) co nhieu o nuoc (" + BanDoSinhThai.demNuoc(n) + ")", BanDoSinhThai.demNuoc(n) > 50);
            final byte[] kieu = Files.readAllBytes(new File("cMap/t.type").toPath());
            final boolean[] di = BanDoSinhThai.luoiDiDuoc(bd, kieu, n);
            int x = -1, y = -1;
            for (int i = 0; i < n.length && x < 0; i++) {
                if (n[i]) {
                    x = i % bd.w;
                    y = i / bd.w;
                }
            }
            Kiem.dung("ngay canh o nuoc la gan nuoc", BanDoSinhThai.ganNuoc(n, bd.w, bd.h, x, y, 2));
            boolean nuocDiDuoc = false;
            for (int i = 0; i < n.length; i++) {
                nuocDiDuoc |= n[i] && di[i];
            }
            Kiem.sai("o nuoc khong di duoc", nuocDiDuoc);
            final int[][] dd = BanDoSinhThai.diemDao(di, bd.w, bd.h, 20261017, 3, 6);
            Kiem.bang("6 diem dao", Integer.valueOf(6), Integer.valueOf(dd.length));
            boolean tot = true;
            for (final int[] p : dd) {
                tot &= di[p[1] * bd.w + p[0]];
            }
            Kiem.dung("diem dao nam tren o di duoc", tot);
            Kiem.dung("diem dao tat dinh theo ngay", BanDoSinhThai.diemDao(di, bd.w, bd.h, 20261017, 3, 6)[0][0] == dd[0][0]);
            Kiem.bang("dung tren diem -> tim thay", Integer.valueOf(0), Integer.valueOf(BanDoSinhThai.diemTaiCho(dd, dd[0][0], dd[0][1], 1)));
            Kiem.dung("goi y huong", BanDoSinhThai.goiYDiemGanNhat(dd, null, dd[0][0] + 5, dd[0][1]).contains("Tây"));
        } catch (final Exception e) {
            Kiem.dung("doc cMap/3: " + e, false);
        }
    }

    // ------------------------------------------------------------------ ho cau ca

    static void hoCauCa() {
        Kiem.nhom("He sinh thai - CHI cau ca o ho (vung nuoc kin, khong toi mep ban do)");
        final int w = 30, h = 20;
        final boolean[] n = new boolean[w * h];
        for (int y = 6; y < 12; y++) {
            for (int x = 8; x < 16; x++) {
                n[y * w + x] = true; // ho 48 o giua ban do
            }
        }
        for (int x = 0; x < w; x++) {
            n[18 * w + x] = true; // bien sat mep duoi
            n[17 * w + x] = true;
        }
        n[3 * w + 25] = n[3 * w + 26] = true; // vung nuoc 2 o: qua nho
        final int[] nh = BanDoSinhThai.nhanHo(n, w, h);
        Kiem.bang("ho giua ban do = ho 0", Integer.valueOf(0), Integer.valueOf(nh[8 * w + 10]));
        Kiem.bang("bien cham mep -> khong phai ho", Integer.valueOf(-1), Integer.valueOf(nh[18 * w + 5]));
        Kiem.bang("vung nuoc 2 o -> khong phai ho", Integer.valueOf(-1), Integer.valueOf(nh[3 * w + 25]));
        Kiem.bang("1 ho, 48 o", "[48]", java.util.Arrays.toString(BanDoSinhThai.kichThuocHo(nh)));
        Kiem.bang("dung sat ho (cach 2 o) -> ho 0", Integer.valueOf(0), Integer.valueOf(BanDoSinhThai.hoGan(nh, w, h, 6, 8, 2)));
        Kiem.bang("dung sat bien -> khong cau duoc", Integer.valueOf(-1), Integer.valueOf(BanDoSinhThai.hoGan(nh, w, h, 5, 15, 2)));
        Kiem.sai("ao nho (< " + BanDoSinhThai.HO_LON + " o): khong co ca hiem (Dratini)", coCa(false, 19));
        Kiem.dung("ho lon: co Dratini (cap 50)", coCa(true, 19));
        // Diem cau ca nap tu data_hst_ho_cau (P1: bang la nguon duy nhat, bo thuat toan chep trong Java). Bat bien tren
        // ban do that: diem di duoc, khong nam trong ho, sat (4 huong) o cua dung ho, so o khop nhan ho luc chay, <= 4
        // diem / ho, moi ho co bo di duoc deu co diem. Va bang SQL == du_lieu/diem_cau_ca.csv (cung bo sinh).
        try {
            final List<String> csv = new ArrayList<String>();
            for (final String d : Files.readAllLines(new File("../du_lieu/diem_cau_ca.csv").toPath(), StandardCharsets.UTF_8)) {
                if (d.startsWith("#") || d.startsWith("map_id")) {
                    continue;
                }
                final String[] c = d.split(",");
                csv.add(c[0] + ":" + c[3] + ":" + c[4] + ":" + c[6] + ":" + c[7]);
            }
            final List<String> sql = new ArrayList<String>();
            int lech = 0, soMap = 0, soDiem = 0;
            for (final String d : Files.readAllLines(new File("../du_lieu/thiet_ke_map.csv").toPath(), StandardCharsets.UTF_8)) {
                final String[] c = d.split(";");
                if (c.length < 3 || !c[0].matches("\\d+")) {
                    continue;
                }
                final int id = Integer.parseInt(c[0]);
                final File f = new File("cMap/" + id);
                if (!f.isFile()) {
                    continue;
                }
                final List<int[]> ds = BanDoSinhThai.diemCauCua(id);
                for (final int[] p : ds) {
                    sql.add(id + ":" + p[0] + ":" + p[3] + ":" + p[1] + ":" + p[2]);
                }
                if ("40".equals(c[0]) || "42".equals(c[0])) {
                    if (!ds.isEmpty()) {
                        lech++;
                        System.out.println("        map " + id + " (khong cau) co diem cau");
                    }
                    continue;
                }
                soMap++;
                final DinhDangBanDo bd = DinhDangBanDo.doc(Files.readAllBytes(f.toPath()));
                final String bo = DinhDangBanDo.boTile(id);
                final boolean[] nuoc = BanDoSinhThai.luoiNuoc(bd, bo);
                final boolean[] dd = BanDoSinhThai.luoiDiDuoc(bd, Files.readAllBytes(new File("cMap/" + bo + ".type").toPath()), nuoc);
                final int[] nhan = BanDoSinhThai.nhanHo(nuoc, bd.w, bd.h);
                final int[] kt = BanDoSinhThai.kichThuocHo(nhan);
                final int[] demHo = new int[kt.length];
                for (final int[] p : ds) {
                    soDiem++;
                    final int i = p[2] * bd.w + p[1];
                    final boolean ok = p[0] >= 0 && p[0] < kt.length && kt[p[0]] == p[3] && p[1] > 0 && p[2] > 0
                            && p[1] < bd.w - 1 && p[2] < bd.h - 1 && dd[i] && nhan[i] < 0
                            && (nhan[i - 1] == p[0] || nhan[i + 1] == p[0] || nhan[i - bd.w] == p[0] || nhan[i + bd.w] == p[0]);
                    if (!ok) {
                        lech++;
                        System.out.println("        map " + id + ": diem cau sai " + java.util.Arrays.toString(p));
                    } else {
                        demHo[p[0]]++;
                    }
                }
                for (int ho = 0; ho < kt.length; ho++) {
                    boolean coBo = false;
                    for (int y = 1; y < bd.h - 1 && !coBo; y++) {
                        for (int x = 1; x < bd.w - 1 && !coBo; x++) {
                            final int i = y * bd.w + x;
                            coBo = dd[i] && nhan[i] < 0 && (nhan[i - 1] == ho || nhan[i + 1] == ho || nhan[i - bd.w] == ho
                                    || nhan[i + bd.w] == ho);
                        }
                    }
                    if (demHo[ho] > 4 || (coBo && demHo[ho] == 0)) {
                        lech++;
                        System.out.println("        map " + id + " ho " + ho + ": " + demHo[ho] + " diem cau (bo di duoc: " + coBo + ")");
                    }
                }
            }
            Kiem.dung("diem cau (data_hst_ho_cau) co tren >= 10 map, >= 40 diem", sql.size() >= 40 && soMap >= 10);
            Kiem.bang("diem cau tu SQL dung bat bien tren " + soMap + " map (" + soDiem + " diem)", Integer.valueOf(0), Integer.valueOf(lech));
            java.util.Collections.sort(csv);
            java.util.Collections.sort(sql);
            Kiem.bang("data_hst_ho_cau == du_lieu/diem_cau_ca.csv", csv, sql);
        } catch (final Exception e) {
            Kiem.dung("doi chieu diem cau ca: " + e, false);
        }
    }

    static boolean coCa(final boolean hoLon, final int ma) {
        final Random r = new Random(5);
        for (int i = 0; i < 300000; i++) {
            if (KyNangSong.cauCa(KyNangSong.NUOC_NGOT, 50, true, hoLon, r).ma == ma) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ SQL sinh ra

    static void duLieuSql() {
        Kiem.nhom("He sinh thai - du_lieu/sql_nguon/pokemon_he_sinh_thai.sql (sinh tu tools/pokemon/sinh_sql_he_sinh_thai.py)");
        try {
            final String sql = new String(Files.readAllBytes(new File("../du_lieu/sql_nguon/pokemon_he_sinh_thai.sql").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("co dau SINH RA / KHONG SUA TAY", sql.startsWith("-- SINH RA boi tools/pokemon/sinh_sql_he_sinh_thai.py"));
            for (final String b : new String[]{"data_hst_vung", "data_hst_thay_quai", "data_hst_bung_phat", "data_hst_vung_loai", "data_hst_ho_cau",
                "tob_hst_nhan_vat", "tob_hst_cho", "tob_hst_don", "tob_hst_hop_thu"}) {
                Kiem.dung("co bang " + b, sql.contains("`" + b + "`"));
            }
            Kiem.sai("khong dung CREATE TABLE ... LIKE (NovaDB)", sql.contains(" LIKE "));
            Kiem.sai("bang nguoi choi khong bi DROP", sql.contains("DROP TABLE IF EXISTS `tob_hst"));
            // Quai: id -> cap, phan loai
            final Map<Integer, int[]> quai = new HashMap<Integer, int[]>();
            for (final String d : Files.readAllLines(new File("../du_lieu/bang_quai.csv").toPath(), StandardCharsets.UTF_8)) {
                final String[] c = d.split(",");
                if (c.length > 5 && c[0].matches("\\d+")) {
                    quai.put(Integer.parseInt(c[0]), new int[]{Integer.parseInt(c[2]), "quai".equals(c[4]) ? 1 : 0});
                }
            }
            final String khoi = sql.substring(sql.indexOf("INSERT INTO `data_hst_thay_quai`"), sql.indexOf("DROP TABLE IF EXISTS `data_hst_bung_phat`"));
            final java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d+),(\\d+),(\\d+),(\\d+),(-?\\d+),(-?\\d+),(\\d+)\\)").matcher(khoi);
            int n = 0, sai = 0, hiem = 0;
            final Set<String> khoa = new HashSet<String>();
            int trung = 0;
            while (m.find()) {
                n++;
                if (!khoa.add(m.group(1) + "-" + m.group(2))) {
                    trung++;
                }
                final int[] g = quai.get(Integer.parseInt(m.group(1)));
                final int[] t = quai.get(Integer.parseInt(m.group(2)));
                final boolean laHiem = "1".equals(m.group(4));
                hiem += laHiem ? 1 : 0;
                if (g == null || t == null || g[1] != 1 || t[1] != 1) {
                    sai++;
                    continue;
                }
                final int dc = t[0] - g[0];
                if (laHiem ? (dc < 3 || dc > 8 || Integer.parseInt(m.group(3)) > 5) : Math.abs(dc) > 4) {
                    sai++;
                }
            }
            Kiem.bang("khoa chinh (id_goc, id_thay) khong trung", Integer.valueOf(0), Integer.valueOf(trung));
            Kiem.dung("co dong thay loai (" + n + ", " + hiem + " hiem)", n > 100 && hiem > 0);
            Kiem.bang("moi dong thay loai: ca hai la quai thuong, cung dai cap (+-4; hiem +3..+8, trong so nho)", Integer.valueOf(0),
                    Integer.valueOf(sai));
            final String bp = sql.substring(sql.indexOf("INSERT INTO `data_hst_bung_phat`"), sql.indexOf("DROP TABLE IF EXISTS `data_hst_vung_loai`"));
            Kiem.sai("bung phat khong o map 40 (MOBA) / 42", bp.contains("\n(40,") || bp.contains("\n(42,"));
        } catch (final Exception e) {
            Kiem.dung("doc du_lieu/sql_nguon/pokemon_he_sinh_thai.sql: " + e, false);
        }
        try {
            final String sh = new String(Files.readAllBytes(new File("../tools/pokemon/xuat_sql_day_du.sh").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("xuat_sql_day_du.sh gom pokemon_he_sinh_thai", sh.contains("pokemon_he_sinh_thai"));
        } catch (final Exception e) {
            Kiem.dung("doc xuat_sql_day_du.sh: " + e, false);
        }
    }

    // ------------------------------------------------------------------ diem moc

    static void diemMoc() {
        Kiem.nhom("He sinh thai - diem moc trong src/real (nho, danh dau '// he sinh thai:')");
        final String[][] moc = {
            {"src/real/Monster.java", "HeSinhThaiGame.khiHoiSinh(this)"},
            {"src/real/Monster.java", "HeSinhThaiGame.khiHaQuai(p, this)"},
            {"src/real/CharChiSo.java", "HeSinhThaiGame.satThuongThoiTiet(self, idSkill, finalDame)"},
            {"src/real/MapChat.java", "HeSinhThaiGame.khiChat(player, msgChat)"},
            {"src/real/Char.java", "HeSinhThaiGame.khiThuHoach(this, cayTruoc, hangTruoc, kq)"},
            {"src/real/MapDoMenu.java", "HeSinhThaiMenu.doMenu(player, idMenu, idOptionMenu)"},
            {"src/real/cmd/RequestNPCInfoHandler.java", "HeSinhThaiGame.TEN_MUC"},
            {"src/real/MapLienDau.java", "HeSinhThaiGame.ketQuaTranPvp(p, c)"},
            {"src/server/TeamServer.java", "p.getProperty(\"sv.he_sinh_thai\")"},
        };
        for (final String[] c : moc) {
            try {
                final String s = new String(Files.readAllBytes(new File(c[0]).toPath()), StandardCharsets.UTF_8);
                Kiem.bang(c[0] + ": goi " + c[1] + " dung 1 lan", Integer.valueOf(1), Integer.valueOf(s.split(java.util.regex.Pattern.quote(c[1]), -1).length - 1));
                Kiem.dung(c[0] + ": co danh dau '// he sinh thai:'", s.contains("// he sinh thai:"));
            } catch (final Exception e) {
                Kiem.dung("doc " + c[0] + ": " + e, false);
            }
        }
        Kiem.bang("npc ao -528", Integer.valueOf(-528), Integer.valueOf(HeSinhThaiGame.NPC_ID));
    }
}
