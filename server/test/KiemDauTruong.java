import com.htbb.pokemon.CaThePokemon;
import com.htbb.pokemon.DauTruongDuLieu;
import com.htbb.pokemon.DauTruongLogic;
import com.htbb.pokemon.DauTruongTran;
import com.htbb.pokemon.GoiDauTruong;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.LoaiDuLieu;
import com.htbb.pokemon.XepHangPvp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAU TRUONG BAO BOI 3v3 (D12): cong thuc sat thuong (ca tham chieu tinh tay), chi so cap 50 (IV / tinh cach), bang
 * khac he, tat dinh theo hat + phat lai, AI luon hop le (500 tran ngau nhien), trang thai, diem mua, luot ngay, chong nick
 * phu / chong cay, tran luong khoa, danh hieu mua, doi hinh, bo cuc goi -109, khoi SQL them_dau_truong.sql. Du lieu that:
 * khoi them_loai_day_du.sql cua sql/nang_cap.sql (1.025 loai, data_chieu, data_loai_chieu).
 */
public final class KiemDauTruong {

    private KiemDauTruong() {
    }

    static DauTruongDuLieu d;

    public static void chay() {
        Kiem.nhom("KiemDauTruong: Dau truong Bao Boi 3v3 (mo phong, diem, ghep, thuong, goi -109, SQL)");
        try {
            napDuLieu();
            kiemChiSo();
            kiemSatThuong();
            kiemKhacHe();
            kiemDuLieuChieu();
            kiemTatDinh();
            kiemAi();
            kiemTrangThai();
            kiemIvTinhCach();
            kiemDiem();
            kiemLuotThuong();
            kiemGhep();
            kiemDoiHinh();
            kiemGoi();
            kiemSql();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("KiemDauTruong chay het khong loi: " + e, false);
        }
    }

    // ------------------------------------------------------------------ du lieu

    static void napDuLieu() {
        final Map<String, List<Map<String, Object>>> b = KiemLoaiDayDu.docSql(DayDu.phanNangCap(KiemLoaiDayDu.SQL));
        final LoaiDuLieu loai = KiemLoaiDayDu.dung(b);
        final Map<String, DauTruongDuLieu.Chieu> chieu = new HashMap<String, DauTruongDuLieu.Chieu>();
        for (final Map<String, Object> r : b.get("data_chieu")) {
            chieu.put(KiemLoaiDayDu.chu(r, "ma"), new DauTruongDuLieu.Chieu(KiemLoaiDayDu.chu(r, "ma"), KiemLoaiDayDu.chu(r, "ten"),
                    KiemLoaiDayDu.so(r, "he"), KiemLoaiDayDu.chu(r, "kieu"), KiemLoaiDayDu.chu(r, "vai_tro"), KiemLoaiDayDu.so(r, "so_don"),
                    KiemLoaiDayDu.chu(r, "trang_thai"), KiemLoaiDayDu.so(r, "ti_le_tt_phan_nghin")));
        }
        final Map<Integer, String[]> cl = new HashMap<Integer, String[]>();
        for (final Map<String, Object> r : b.get("data_loai_chieu")) {
            cl.computeIfAbsent(Integer.valueOf(KiemLoaiDayDu.so(r, "dex")), k -> new String[4])[KiemLoaiDayDu.so(r, "o")] =
                    KiemLoaiDayDu.chu(r, "ma_chieu");
        }
        d = new DauTruongDuLieu(loai, chieu, cl);
        Kiem.dung("du lieu: 1.025 loai + chieu + 4 chieu moi loai", !d.rong() && loai.soLoai() == 1025 && cl.size() == 1025);
    }

    static DauTruongTran.Con con(final int dex, final int ivDeu, final int tc) {
        final int[] iv = new int[6];
        for (int i = 0; i < 6; i++) {
            iv[i] = ivDeu;
        }
        return DauTruongTran.taoCon(d, dex, false, CaThePokemon.dongGoi(iv), tc);
    }

    // ------------------------------------------------------------------ chi so

    static void kiemChiSo() {
        final DauTruongTran.Con pika = con(25, 31, 0); // Hardy trung tinh
        Kiem.bang("Pikachu cap 50 IV 31 trung tinh = chinh ban {110,75,60,70,70,110}", "[110, 75, 60, 70, 70, 110]",
                java.util.Arrays.toString(pika.chiSo));
        final DauTruongTran.Con bulba = con(1, 31, 0);
        Kiem.bang("Bulbasaur cap 50 IV 31 = {120,69,69,85,85,65}", "[120, 69, 69, 85, 85, 65]", java.util.Arrays.toString(bulba.chiSo));
        final DauTruongTran.Con pikaKq = con(25, 31, 3); // Adamant: +Tan cong -Tan cong DB
        Kiem.bang("Kiên quyết (Adamant): Tan cong 75 x 1,1 = 82", Integer.valueOf(82), Integer.valueOf(pikaKq.chiSo[1]));
        Kiem.bang("Kiên quyết: Tan cong DB 70 x 0,9 = 63", Integer.valueOf(63), Integer.valueOf(pikaKq.chiSo[3]));
        final DauTruongTran.Con pika0 = con(25, 0, 0);
        Kiem.bang("IV 0: Tan cong 60 (IV 31: 75)", Integer.valueOf(60), Integer.valueOf(pika0.chiSo[1]));
        Kiem.bang("IV 0: HP 95 (IV 31: 110)", Integer.valueOf(95), Integer.valueOf(pika0.hpMax));
        Kiem.bang("PP Dau truong theo vai tro: xa 15", Integer.valueOf(15), Integer.valueOf(DauTruongDuLieu.PP[DauTruongDuLieu.V_XA]));
    }

    // ------------------------------------------------------------------ sat thuong

    static void kiemSatThuong() {
        // floor(floor(floor(2*50/5+2) * 80 * 100 / 100) / 50) + 2 = floor(1760 / 50) + 2 = 37
        Kiem.bang("tham chieu: L50 P80 A100 D100 ngau nhien 100 = 37", Integer.valueOf(37),
                Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, false, 100, false, 100, false)));
        Kiem.bang("ngau nhien 85: 37 x 0,85 = 31", Integer.valueOf(31),
                Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, false, 100, false, 85, false)));
        Kiem.bang("STAB x1,5: 55", Integer.valueOf(55), Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, true, 100, false, 100, false)));
        Kiem.bang("STAB + x2: 110", Integer.valueOf(110), Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, true, 200, false, 100, false)));
        Kiem.bang("STAB + x4: 220", Integer.valueOf(220), Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, true, 400, false, 100, false)));
        Kiem.bang("x0,5: 18", Integer.valueOf(18), Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, false, 50, false, 100, false)));
        Kiem.bang("chi mang x1,5 truoc ngau nhien: 55, + STAB 82", Integer.valueOf(82),
                Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, true, 100, true, 100, false)));
        Kiem.bang("bong + vat ly x0,5: 18", Integer.valueOf(18), Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, false, 100, false, 100, true)));
        Kiem.bang("mien nhiem x0 = 0", Integer.valueOf(0), Integer.valueOf(DauTruongTran.satThuong(50, 80, 100, 100, true, 0, false, 100, false)));
        Kiem.bang("toi thieu 1 (x0,25 cua 2)", Integer.valueOf(1), Integer.valueOf(DauTruongTran.satThuong(50, 20, 10, 300, false, 25, false, 100, false)));
        // Pikachu (TC DB 70) Tia dien thuong (xa 80, STAB) vao Gyarados (Nuoc / Bay x4, PT DB (2x100+31)/2+5 = 120)
        // floor(22*80*70/120) = 1026 -> 1026/50 = 20 + 2 = 22 -> x1 -> STAB 33 -> x4 132
        Kiem.bang("tham chieu: Pikachu TC DB 70, uy luc 80 STAB, vao PT DB 120, x4 = 132", Integer.valueOf(132),
                Integer.valueOf(DauTruongTran.satThuong(50, 80, 70, 120, true, 400, false, 100, false)));
        Kiem.bang("bac +2 = x2, -2 = x0,5, +6 = x4", "200,50,400", DauTruongTran.heSoBac(2) + "," + DauTruongTran.heSoBac(-2) + ","
                + DauTruongTran.heSoBac(6));
        Kiem.bang("bac chinh xac -1 = 75%, +1 = 133%", "75,133", DauTruongTran.heSoBacChinhXac(-1) + "," + DauTruongTran.heSoBacChinhXac(1));
    }

    static void kiemKhacHe() {
        Kiem.bang("Dien vao Dat = x0", Integer.valueOf(0), Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.DIEN, HePokemon.DAT, -1)));
        Kiem.bang("Nuoc vao Lua/Da = x4", Integer.valueOf(400), Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.NUOC, HePokemon.LUA, HePokemon.DA)));
        Kiem.bang("Co vao Lua/Bay = x0,25", Integer.valueOf(25), Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.CO, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("Ma vao Thuong = x0", Integer.valueOf(0), Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.MA, HePokemon.THUONG, -1)));
        Kiem.bang("Rong vao Tien = x0", Integer.valueOf(0), Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.RONG, HePokemon.TIEN, -1)));
        Kiem.bang("Bang vao Rong/Bay = x4", Integer.valueOf(400), Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.BANG, HePokemon.RONG, HePokemon.BAY)));
        // Dau truong dung he so CHINH XAC (khong lam diu MMO): Geodude (Da/Dat) khong mat mau vi chieu Dien
        final DauTruongTran.Con pika = con(25, 31, 0), geo = con(74, 31, 0);
        int dien = -1;
        for (int i = 0; i < 4; i++) {
            if (pika.chieu[i].he == HePokemon.DIEN && pika.chieu[i].danh()) {
                dien = i;
            }
        }
        final DauTruongTran t = new DauTruongTran(7L, new DauTruongTran.Con[]{pika, con(4, 31, 0), con(7, 31, 0)},
                new DauTruongTran.Con[]{geo, con(16, 31, 0), con(19, 31, 0)});
        t.aiDoi[DauTruongTran.THU] = false;
        final int hp = geo.hp;
        final List<String> l = t.choi(DauTruongTran.HanhDong.chieu(dien));
        Kiem.dung("Pikachu co chieu he Dien", dien >= 0);
        boolean khongTacDung = false;
        for (final String s : l) {
            khongTacDung |= s.contains("Không có tác dụng") || s.contains("trượt");
        }
        Kiem.dung("chieu Dien vao Geodude: khong tac dung (x0 that) - Geodude khong mat HP vi chieu do", khongTacDung && geo.hp == hp
                || geo.hp == hp);
    }

    static void kiemDuLieuChieu() {
        int thieu = 0, hoTro = 0, danh = 0;
        for (int dex = 1; dex <= 251; dex++) {
            for (final DauTruongDuLieu.Chieu c : d.bonChieu(dex)) {
                if (c == DauTruongDuLieu.MAC_DINH && !"normal.thuong".equals(c.ma)) {
                    thieu++;
                }
                if (c.danh()) {
                    danh++;
                } else {
                    hoTro++;
                }
            }
        }
        Kiem.bang("Gen 1-2: moi loai du 4 chieu that trong data_chieu", Integer.valueOf(0), Integer.valueOf(thieu));
        Kiem.dung("Gen 1-2: moi loai co it nhat 1 chieu danh (" + danh + " danh, " + hoTro + " ho tro)", danh >= 251 * 2);
        boolean coTrangThai = false;
        for (final DauTruongDuLieu.Chieu c : d.chieu().values()) {
            coTrangThai |= c.tiLeTrangThai > 0 && c.danh();
        }
        Kiem.dung("chieu danh co trang thai phu + ti le tu data_chieu.ti_le_tt_phan_nghin", coTrangThai);
    }

    // ------------------------------------------------------------------ tat dinh / AI

    static DauTruongTran.Con[] doiNgau(final java.util.Random r) {
        final DauTruongTran.Con[] k = new DauTruongTran.Con[3];
        for (int i = 0; i < 3; i++) {
            final int dex = 1 + r.nextInt(251);
            k[i] = DauTruongTran.taoCon(d, dex, r.nextInt(20) == 0, CaThePokemon.ivNgauNhien(r), r.nextInt(25));
        }
        return k;
    }

    static DauTruongTran tran(final long hat, final long hatDoi) {
        final java.util.Random r = new java.util.Random(hatDoi);
        return new DauTruongTran(hat, doiNgau(r), doiNgau(r));
    }

    static void kiemTatDinh() {
        final DauTruongTran a = tran(42L, 9L), b = tran(42L, 9L);
        a.chayHet();
        b.chayHet();
        Kiem.dung("cung hat + cung doi: cung nhat ky tung dong", a.nhatKy.equals(b.nhatKy) && a.thang == b.thang && a.luot == b.luot);
        // phat lai: hanh dong ben tan cong ghi lai -> choi lai dung tran
        final DauTruongTran c = tran(42L, 9L);
        for (final String m : a.hanhDongCong) {
            c.choi(DauTruongTran.HanhDong.tuMa(m));
        }
        Kiem.dung("phat lai tu hat + chuoi hanh dong = cung tran", a.nhatKy.equals(c.nhatKy) && a.thang == c.thang);
        // nguoi choi chon chieu khac AI: van tat dinh
        final DauTruongTran x = tran(5L, 3L), y = tran(5L, 3L);
        for (int i = 0; i < 60 && !x.xong(); i++) {
            final DauTruongTran.HanhDong h = DauTruongTran.HanhDong.chieu(i % 4);
            x.choi(h);
            y.choi(h);
        }
        Kiem.dung("hanh dong nguoi choi (chieu 0,1,2,3...): cung hat = cung tran", x.nhatKy.equals(y.nhatKy));
        int khac = 0;
        for (long h = 1; h <= 10; h++) {
            final DauTruongTran p = tran(h, 9L);
            p.chayHet();
            khac += p.nhatKy.equals(a.nhatKy) ? 0 : 1;
        }
        Kiem.dung("hat khac -> tran khac (" + khac + "/10)", khac >= 9);
        Kiem.bang("ma hanh dong c2 / d1 hai chieu", "c2,d1", DauTruongTran.HanhDong.tuMa("c2").ma() + "," + DauTruongTran.HanhDong.tuMa("d1").ma());
    }

    static void kiemAi() {
        final java.util.Random r = new java.util.Random(2026);
        int saiAi = 0, saiHp = 0, saiPp = 0, khongXong = 0, doi = 0, thangCong = 0, luotMax = 0;
        for (int n = 0; n < 500; n++) {
            final DauTruongTran t = new DauTruongTran(r.nextLong(), doiNgau(r), doiNgau(r));
            int an = 0;
            while (!t.xong() && an++ < 80) {
                for (int ben = 0; ben < 2; ben++) {
                    final DauTruongTran.HanhDong h = t.ai(ben);
                    if (!t.hopLe(ben, h)) {
                        saiAi++;
                    }
                    if (ben == DauTruongTran.THU && h.loai == DauTruongTran.HanhDong.DOI) {
                        doi++;
                    }
                }
                // ben tan cong: dua hanh dong ngau nhien (ke ca sai) - may phai thay bang hanh dong hop le
                t.choi(new DauTruongTran.HanhDong(r.nextInt(2), r.nextInt(5) - 1));
                for (int ben = 0; ben < 2; ben++) {
                    for (final DauTruongTran.Con c : t.doi[ben]) {
                        if (c.hp < 0 || c.hp > c.hpMax) {
                            saiHp++;
                        }
                        for (final int p : c.pp) {
                            if (p < 0 || p > 30) {
                                saiPp++;
                            }
                        }
                    }
                }
            }
            if (!t.xong()) {
                khongXong++;
            }
            luotMax = Math.max(luotMax, t.luot);
            thangCong += t.thang == DauTruongTran.CONG ? 1 : 0;
        }
        Kiem.bang("AI: 500 tran ngau nhien, moi hanh dong AI hop le", Integer.valueOf(0), Integer.valueOf(saiAi));
        Kiem.bang("HP luon trong [0, HP toi da]", Integer.valueOf(0), Integer.valueOf(saiHp));
        Kiem.bang("PP khong am", Integer.valueOf(0), Integer.valueOf(saiPp));
        Kiem.bang("moi tran ket thuc trong " + DauTruongTran.LUOT_TOI_DA + " luot (dai nhat " + luotMax + ")", Integer.valueOf(0),
                Integer.valueOf(khongXong));
        Kiem.dung("AI phong thu co doi Pokemon (" + doi + " lan)", doi > 0);
        Kiem.dung("ca hai ben deu thang duoc (cong thang " + thangCong + "/500)", thangCong > 50 && thangCong < 450);
        // het PP: Vung vay, van hop le
        final DauTruongTran.Con a = con(25, 31, 0);
        for (int i = 0; i < 4; i++) {
            a.pp[i] = 0;
        }
        final DauTruongTran t = new DauTruongTran(3L, new DauTruongTran.Con[]{a, con(4, 31, 0), con(7, 31, 0)},
                new DauTruongTran.Con[]{con(1, 31, 0), con(16, 31, 0), con(19, 31, 0)});
        t.aiDoi[DauTruongTran.CONG] = false;
        final DauTruongTran.HanhDong h = t.ai(DauTruongTran.CONG);
        Kiem.dung("het PP: AI tra hanh dong hop le", t.hopLe(DauTruongTran.CONG, h));
        boolean vungVay = false;
        for (final String s : t.choi(h)) {
            vungVay |= s.contains("Vùng vẫy");
        }
        Kiem.dung("het PP: dung Vung vay (giat lui)", vungVay);
        Kiem.sai("doi sang con dang ra = khong hop le", t.hopLe(DauTruongTran.CONG, DauTruongTran.HanhDong.doi(t.dangRa[0])));
        Kiem.sai("chieu o 4 = khong hop le", t.hopLe(DauTruongTran.CONG, DauTruongTran.HanhDong.chieu(4)));
    }

    static void kiemTrangThai() {
        Kiem.dung("Lua mien bong", DauTruongTran.mienNhiem(con(4, 31, 0), DauTruongTran.TT_BONG));
        Kiem.dung("Doc / Thep mien doc", DauTruongTran.mienNhiem(con(1, 31, 0), DauTruongTran.TT_DOC)
                && DauTruongTran.mienNhiem(con(208, 31, 0), DauTruongTran.TT_DOC));
        Kiem.dung("Dien mien te liet", DauTruongTran.mienNhiem(con(25, 31, 0), DauTruongTran.TT_TE));
        Kiem.sai("Thuong khong mien te liet", DauTruongTran.mienNhiem(con(19, 31, 0), DauTruongTran.TT_TE));
        final DauTruongTran t = new DauTruongTran(1L, new DauTruongTran.Con[]{con(25, 31, 0), con(4, 31, 0), con(7, 31, 0)},
                new DauTruongTran.Con[]{con(19, 31, 0), con(16, 31, 0), con(1, 31, 0)});
        final int v = t.tocDo(DauTruongTran.CONG);
        t.dangRa(DauTruongTran.CONG).trangThai = DauTruongTran.TT_TE;
        Kiem.bang("te liet: toc do x0,5", Integer.valueOf(v / 2), Integer.valueOf(t.tocDo(DauTruongTran.CONG)));
        // bong / doc mat mau cuoi luot
        final DauTruongTran u = new DauTruongTran(1L, new DauTruongTran.Con[]{con(143, 31, 0), con(4, 31, 0), con(7, 31, 0)},
                new DauTruongTran.Con[]{con(143, 31, 0), con(16, 31, 0), con(1, 31, 0)});
        u.aiDoi[DauTruongTran.THU] = false;
        u.dangRa(DauTruongTran.THU).trangThai = DauTruongTran.TT_DOC;
        boolean docNgam = false;
        for (final String s : u.choi(null)) {
            docNgam |= s.contains("độc ngấm");
        }
        Kiem.dung("trung doc: mat 1/8 HP cuoi luot", docNgam);
        Kiem.bang("ma trang thai data_chieu -> trang thai chinh", "1,2,3,4,5,0", DauTruongTran.maTrangThai("bong") + ","
                + DauTruongTran.maTrangThai("doc") + "," + DauTruongTran.maTrangThai("te") + "," + DauTruongTran.maTrangThai("ngu") + ","
                + DauTruongTran.maTrangThai("dong_bang") + "," + DauTruongTran.maTrangThai("lu_lan"));
    }

    static void kiemIvTinhCach() {
        // guong: cung loai, IV 31 vs IV 0 -> ben IV cao thang nhieu hon ro
        int thangCao = 0;
        final int[] loai = {6, 9, 3, 65, 68, 94, 130, 149};
        for (int n = 0; n < 160; n++) {
            final int x = loai[n % loai.length], y = loai[(n / 8) % loai.length], z = loai[(n / 3) % loai.length];
            final DauTruongTran t = new DauTruongTran(1000L + n, new DauTruongTran.Con[]{con(x, 31, 0), con(y, 31, 0), con(z, 31, 0)},
                    new DauTruongTran.Con[]{con(x, 0, 0), con(y, 0, 0), con(z, 0, 0)});
            t.chayHet();
            thangCao += t.thang == DauTruongTran.CONG ? 1 : 0;
        }
        Kiem.dung("IV 31 thang IV 0 da so tran guong (" + thangCao + "/160)", thangCao >= 110);
    }

    // ------------------------------------------------------------------ diem / luot / thuong

    static void kiemDiem() {
        Kiem.bang("bang diem thang: 1000 -> 1016", Integer.valueOf(1016), Integer.valueOf(DauTruongLogic.diemCong(1000, 1000, 1, 1000)));
        Kiem.bang("bang diem thua: 1000 -> 984", Integer.valueOf(984), Integer.valueOf(DauTruongLogic.diemCong(1000, 1000, 0, 1000)));
        Kiem.bang("gap lai lan 2 (x1/2): +8", Integer.valueOf(1008), Integer.valueOf(DauTruongLogic.diemCong(1000, 1000, 1, 500)));
        Kiem.bang("lan 3 (x1/4): +4", Integer.valueOf(1004), Integer.valueOf(DauTruongLogic.diemCong(1000, 1000, 1, 250)));
        Kiem.bang("lan 4 (x0): +0", Integer.valueOf(1000), Integer.valueOf(DauTruongLogic.diemCong(1000, 1000, 1, 0)));
        Kiem.bang("thang doi thu manh hon 400: +29", Integer.valueOf(1029), Integer.valueOf(DauTruongLogic.diemCong(1000, 1400, 1, 1000)));
        Kiem.bang("san diem 100", Integer.valueOf(100), Integer.valueOf(DauTruongLogic.diemCong(110, 110, 0, 1000)));
        Kiem.bang("phong thu bi thang: doi 1/2 muc (1000 -> 992)", Integer.valueOf(992), Integer.valueOf(DauTruongLogic.diemThu(1000, 1000, 1, 1000)));
        Kiem.bang("phong thu giu duoc: 1000 -> 1008", Integer.valueOf(1008), Integer.valueOf(DauTruongLogic.diemThu(1000, 1000, 0, 1000)));
        Kiem.bang("he so lap 1000 / 500 / 250 / 0", "1000,500,250,0", DauTruongLogic.heSoLap(0) + "," + DauTruongLogic.heSoLap(1) + ","
                + DauTruongLogic.heSoLap(2) + "," + DauTruongLogic.heSoLap(3));
        Kiem.bang("bac theo diem", "0,1,2,3,4,5", DauTruongLogic.bac(800) + "," + DauTruongLogic.bac(950) + "," + DauTruongLogic.bac(1150) + ","
                + DauTruongLogic.bac(1300) + "," + DauTruongLogic.bac(1450) + "," + DauTruongLogic.bac(1600));
        // 30 tran thang lien voi cung doi thu trong 24 gio chi cong 16 + 8 + 4 (khong cay duoc)
        int dm = 1000;
        for (int i = 0; i < 30; i++) {
            dm = DauTruongLogic.diemCong(dm, 1000, 1, DauTruongLogic.heSoLap(i));
        }
        Kiem.dung("cay mot doi thu 30 tran: chi +28 diem (" + dm + ")", dm <= 1028);
    }

    static void kiemLuotThuong() {
        Kiem.bang("luot ngay = 5", Integer.valueOf(5), Integer.valueOf(DauTruongLogic.LUOT_NGAY));
        Kiem.bang("luot con: 0 da danh = 5, 4 = 1, 5 = 0, 9 = 0", "5,1,0,0", DauTruongLogic.luotConLai(0) + "," + DauTruongLogic.luotConLai(4) + ","
                + DauTruongLogic.luotConLai(5) + "," + DauTruongLogic.luotConLai(9));
        Kiem.bang("luong thang = 3", Integer.valueOf(3), Integer.valueOf(DauTruongLogic.luongTran(true, 1000, 0)));
        Kiem.bang("luong thua = 0", Integer.valueOf(0), Integer.valueOf(DauTruongLogic.luongTran(false, 1000, 0)));
        Kiem.bang("luong gap lai x1/2 = 1, x1/4 = 0", "1,0", DauTruongLogic.luongTran(true, 500, 0) + "," + DauTruongLogic.luongTran(true, 250, 0));
        Kiem.bang("tran tuan: da nhan 24 -> chi 1", Integer.valueOf(1), Integer.valueOf(DauTruongLogic.luongTran(true, 1000, 24)));
        int tuan = 0;
        for (int ngay = 0; ngay < 7; ngay++) {
            for (int l = 0; l < DauTruongLogic.LUOT_NGAY; l++) {
                tuan += DauTruongLogic.luongTran(true, 1000, tuan);
            }
        }
        Kiem.bang("thang het 35 luot / tuan: luong khoa dung tran 25", Integer.valueOf(25), Integer.valueOf(tuan));
        Kiem.bang("danh hieu mua: hang 1 >= 10 tran = Quan quan 9870", Integer.valueOf(9870), Integer.valueOf(DauTruongLogic.danhHieuMua(1, 1500, 10)));
        Kiem.bang("hang 1 nhung 9 tran = khong", Integer.valueOf(0), Integer.valueOf(DauTruongLogic.danhHieuMua(1, 1500, 9)));
        Kiem.bang("hang 2..10 = Tinh anh", Integer.valueOf(9871), Integer.valueOf(DauTruongLogic.danhHieuMua(10, 1300, 20)));
        Kiem.bang("hang 11..100 = Chien binh", Integer.valueOf(9872), Integer.valueOf(DauTruongLogic.danhHieuMua(100, 1000, 20)));
        Kiem.bang("hang 150 du 1300 diem = Chien binh", Integer.valueOf(9872), Integer.valueOf(DauTruongLogic.danhHieuMua(150, 1300, 20)));
        Kiem.bang("hang 150 duoi 1300 = khong", Integer.valueOf(0), Integer.valueOf(DauTruongLogic.danhHieuMua(150, 1250, 20)));
        Kiem.bang("ma danh hieu", "dau_truong_quan_quan", DauTruongLogic.maDanhHieu(9870));
    }

    static void kiemGhep() {
        final XepHangPvp.Cum toi = new XepHangPvp.Cum(1, "may-a", "1.2.3.4");
        final DauTruongLogic.UngVien ok = new DauTruongLogic.UngVien(20, 1050, 1000, new XepHangPvp.Cum(2, "may-b", "5.6.7.8"), 0);
        Kiem.dung("ghep: khac tai khoan / may / IP, diem +-100, suc manh +-15%", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi, ok, 0));
        Kiem.sai("chong nick phu: cung tai khoan", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(21, 1000, 1000, new XepHangPvp.Cum(1, "x", "9.9.9.9"), 0), 0));
        Kiem.sai("chong nick phu: cung thiet bi", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(21, 1000, 1000, new XepHangPvp.Cum(3, "may-a", "9.9.9.9"), 0), 0));
        Kiem.sai("chong nick phu: cung IP", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(21, 1000, 1000, new XepHangPvp.Cum(3, "z", "1.2.3.4"), 0), 0));
        Kiem.dung("IP rong (proxy tin cay) khong tinh trung", DauTruongLogic.ghepDuoc(10, 1000, 1000, new XepHangPvp.Cum(1, "a", ""),
                new DauTruongLogic.UngVien(21, 1000, 1000, new XepHangPvp.Cum(3, "b", ""), 0), 0));
        Kiem.sai("khong ghep chinh minh", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(10, 1000, 1000, new XepHangPvp.Cum(9, "q", "8.8.8.8"), 0), 0));
        Kiem.sai("chong cay: da gap 3 lan trong 24 gio thi khong ghep", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(21, 1000, 1000, new XepHangPvp.Cum(3, "b", "8.8.8.8"), 3), 0));
        Kiem.sai("lech diem 150 > cua so 100", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(21, 1150, 1000, new XepHangPvp.Cum(3, "b", "8.8.8.8"), 0), 0));
        Kiem.dung("lam moi 1 lan: cua so 200", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(21, 1150, 1000, new XepHangPvp.Cum(3, "b", "8.8.8.8"), 0), 1));
        Kiem.sai("suc manh lech 20% > 15%", DauTruongLogic.ghepDuoc(10, 1000, 1000, toi,
                new DauTruongLogic.UngVien(21, 1000, 1200, new XepHangPvp.Cum(3, "b", "8.8.8.8"), 0), 0));
        Kiem.bang("cua so / dai toi da 400 / 50%", "400,50", DauTruongLogic.cuaSoDiem(99) + "," + DauTruongLogic.daiSucManh(99));
        final List<DauTruongLogic.UngVien> ds = new ArrayList<DauTruongLogic.UngVien>();
        for (int i = 0; i < 12; i++) {
            ds.add(new DauTruongLogic.UngVien(100 + i, 950 + i * 10, 1000, new XepHangPvp.Cum(100 + i, "m" + i, "10.0.0." + i), i % 3));
        }
        ds.add(new DauTruongLogic.UngVien(500, 1000, 1000, new XepHangPvp.Cum(1, "khac", "11.0.0.1"), 0)); // nick phu
        final List<DauTruongLogic.UngVien> chon = DauTruongLogic.deXuat(10, 1000, 1000, toi, ds, 0, 77L);
        Kiem.bang("de xuat toi da 3", Integer.valueOf(3), Integer.valueOf(chon.size()));
        boolean coPhu = false;
        int gapMax = 0;
        for (final DauTruongLogic.UngVien u : chon) {
            coPhu |= u.id == 500;
            gapMax = Math.max(gapMax, u.tran24h);
        }
        Kiem.sai("de xuat khong co nick phu", coPhu);
        Kiem.bang("de xuat uu tien doi thu chua gap", Integer.valueOf(0), Integer.valueOf(gapMax));
        Kiem.bang("de xuat tat dinh theo hat", DauTruongLogic.deXuat(10, 1000, 1000, toi, ds, 0, 77L).get(0).id + "",
                chon.get(0).id + "");
    }

    static void kiemDoiHinh() {
        final HopPc h = new HopPc();
        final HopPc.Con a = h.them(25, 30, false, 20261001, "poke");
        final HopPc.Con b = h.them(4, 30, false, 20261001, "poke");
        final HopPc.Con c = h.them(7, 30, false, 20261001, "poke");
        final HopPc.Con a2 = h.them(25, 12, false, 20261001, "poke");
        final HopPc.Con mew = h.them(150, 70, false, 20261001, "poke");
        Kiem.dung("doi 3 con khac loai trong Hop PC: hop le", DauTruongLogic.loiDoi(new int[]{a.id, b.id, c.id}, h, d) == null);
        Kiem.dung("thieu con: loi", DauTruongLogic.loiDoi(new int[]{a.id, b.id}, h, d) != null);
        Kiem.dung("mot con hai o: loi", DauTruongLogic.loiDoi(new int[]{a.id, a.id, c.id}, h, d) != null);
        Kiem.dung("hai con cung loai: loi", DauTruongLogic.loiDoi(new int[]{a.id, a2.id, c.id}, h, d) != null);
        Kiem.dung("Huyen thoai T6 (Mewtwo): loi", DauTruongLogic.loiDoi(new int[]{a.id, b.id, mew.id}, h, d) != null);
        Kiem.dung("id khong co trong Hop PC (vd nhan vat nguoi choi): loi", DauTruongLogic.loiDoi(new int[]{a.id, b.id, 9999}, h, d) != null);
        Kiem.bang("ma hoa doi \"1,2,3\" hai chieu", "1,2,3", DauTruongLogic.ghiIds(DauTruongLogic.docIds("1,2,3")));
        Kiem.dung("ma hoa hong = null", DauTruongLogic.docIds("1,x,3") == null && DauTruongLogic.docIds("1,2") == null);
        final int suc = DauTruongLogic.sucManhDoi(new int[]{a.id, b.id, c.id}, h, d);
        Kiem.bang("suc manh doi = tong suc manh cap 50 (khong theo cap trong Hop)", Integer.valueOf(d.sucManh50(25, a.iv, a.tinhCach)
                + d.sucManh50(4, b.iv, b.tinhCach) + d.sucManh50(7, c.iv, c.tinhCach)), Integer.valueOf(suc));
        a.cap = 100;
        Kiem.bang("chuan hoa cap 50: cap trong Hop khong doi suc manh", Integer.valueOf(suc),
                Integer.valueOf(DauTruongLogic.sucManhDoi(new int[]{a.id, b.id, c.id}, h, d)));
    }

    // ------------------------------------------------------------------ goi -109

    static byte[] ghi(final GhiGoi g) throws Exception {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(b);
        g.ghi(o);
        o.flush();
        return b.toByteArray();
    }

    interface GhiGoi {
        void ghi(DataOutputStream o) throws Exception;
    }

    static void kiemGoi() throws Exception {
        Kiem.bang("lenh -109, client >= 908", "-109,908", GoiDauTruong.LENH + "," + GoiDauTruong.BAN_CLIENT);
        final GoiDauTruong.TrangThai t = new GoiDauTruong.TrangThai();
        t.mo = true;
        t.mua = 2;
        t.ngayConLai = 40;
        t.diem = 1016;
        t.bac = 1;
        t.tenBac = "Đồng";
        t.hang = 7;
        t.thang = 3;
        t.thua = 1;
        t.luotCon = 4;
        t.luotNgay = 5;
        t.luongTuan = 9;
        t.tranLuongTuan = 25;
        t.lamMoiCon = 10;
        t.cong = new int[]{1, 2, 3};
        t.thu = new int[]{3, 2, 1};
        t.sucCong = 2100;
        t.sucThu = 2100;
        final GoiDauTruong.KhoCon k = new GoiDauTruong.KhoCon();
        k.id = 1;
        k.dex = 25;
        k.cap = 30;
        k.sao = 2;
        k.ivPt = 70;
        k.suc50 = 495;
        k.he1 = 3;
        k.he2 = -1;
        k.ten = "Pikachu";
        t.kho = new GoiDauTruong.KhoCon[]{k};
        t.ghiChu = "Đã lưu";
        final DataInputStream r = new DataInputStream(new ByteArrayInputStream(ghi(o -> GoiDauTruong.trangThai(o, t))));
        final StringBuilder s = new StringBuilder();
        s.append(r.readByte()).append('|').append(r.readByte()).append('|').append(r.readShort()).append('|').append(r.readShort()).append('|')
                .append(r.readShort()).append('|').append(r.readByte()).append('|').append(r.readUTF()).append('|').append(r.readInt()).append('|')
                .append(r.readShort()).append('|').append(r.readShort()).append('|').append(r.readByte()).append('|').append(r.readByte())
                .append('|').append(r.readShort()).append('|').append(r.readShort()).append('|').append(r.readByte()).append('|')
                .append(r.readByte()).append('|');
        for (int ben = 0; ben < 2; ben++) {
            final int n = r.readByte();
            for (int i = 0; i < n; i++) {
                s.append(r.readInt()).append(',');
            }
            s.append('|');
        }
        s.append(r.readInt()).append('|').append(r.readInt()).append('|');
        final int nk = r.readShort();
        for (int i = 0; i < nk; i++) {
            s.append(r.readInt()).append(',').append(r.readShort()).append(',').append(r.readByte()).append(',').append(r.readByte()).append(',')
                    .append(r.readByte()).append(',').append(r.readByte()).append(',').append(r.readShort()).append(',').append(r.readByte())
                    .append(',').append(r.readByte()).append(',').append(r.readByte()).append(',').append(r.readUTF());
        }
        s.append('|').append(r.readUTF());
        Kiem.bang("goi trang thai: dung thu tu truong", "0|1|2|40|1016|1|Đồng|7|3|1|4|5|9|25|10|20|1,2,3,|3,2,1,|2100|2100|1,25,30,0,2,70,495,3,-1,0,Pikachu|Đã lưu",
                s.toString());
        Kiem.bang("goi trang thai: doc het, khong du byte", Integer.valueOf(0), Integer.valueOf(r.available()));
        // bat dau + luot + ket qua
        final DauTruongTran tr = tran(11L, 12L);
        final byte[] bd = ghi(o -> GoiDauTruong.batDau(o, 77, "Mây Xanh", 1040, 20, tr, 20, false));
        final DataInputStream q = new DataInputStream(new ByteArrayInputStream(bd));
        Kiem.bang("bat dau: kieu 2 + id tran", "2,77,Mây Xanh,1040,20,50", q.readByte() + "," + q.readInt() + "," + q.readUTF() + "," + q.readShort()
                + "," + q.readByte() + "," + q.readByte());
        int dexDung = 0;
        for (int ben = 0; ben < 2; ben++) {
            for (int i = 0; i < 3; i++) {
                final int dex = q.readShort();
                q.readByte();
                final String ten = q.readUTF();
                q.readByte();
                q.readByte();
                final int hp = q.readShort();
                dexDung += dex == tr.doi[ben][i].dex && hp == tr.doi[ben][i].hpMax && ten.equals(tr.doi[ben][i].ten) ? 1 : 0;
            }
        }
        Kiem.bang("bat dau: 2 ben x 3 con (dex, ten, HP toi da)", Integer.valueOf(6), Integer.valueOf(dexDung));
        int ppDung = 0;
        for (int i = 0; i < 3; i++) {
            for (int m = 0; m < 4; m++) {
                final String ten = q.readUTF();
                q.readByte();
                q.readByte();
                q.readByte();
                ppDung += q.readByte() == tr.doi[0][i].chieu[m].ppToiDa && ten.equals(tr.doi[0][i].chieu[m].ten) ? 1 : 0;
            }
        }
        Kiem.bang("bat dau: 3 x 4 chieu cua minh (ten, PP toi da)", Integer.valueOf(12), Integer.valueOf(ppDung));
        docKhoi(q, tr);
        Kiem.bang("bat dau: doc het", Integer.valueOf(0), Integer.valueOf(q.available()));
        final List<String> dong = tr.choi(null);
        final DataInputStream l = new DataInputStream(new ByteArrayInputStream(ghi(o -> GoiDauTruong.luot(o, 77, tr, dong, 20, true))));
        Kiem.bang("luot: kieu 3, id, so luot", "3,77,1", l.readByte() + "," + l.readInt() + "," + l.readShort());
        final int nd = l.readByte();
        final List<String> doc = new ArrayList<String>();
        for (int i = 0; i < nd; i++) {
            doc.add(l.readUTF());
        }
        Kiem.dung("luot: cac dong nhat ky dung (" + nd + ")", doc.equals(dong));
        docKhoi(l, tr);
        Kiem.bang("luot: doc het", Integer.valueOf(0), Integer.valueOf(l.available()));
        final DataInputStream kq = new DataInputStream(new ByteArrayInputStream(ghi(o -> GoiDauTruong.ketQua(o, 77, true, 1000, 1016, -8, 3,
                1000, 4, "Thắng"))));
        Kiem.bang("ket qua: thu tu truong", "4,77,1,1000,1016,-8,3,1000,4,Thắng", kq.readByte() + "," + kq.readInt() + "," + kq.readByte() + ","
                + kq.readShort() + "," + kq.readShort() + "," + kq.readShort() + "," + kq.readShort() + "," + kq.readShort() + "," + kq.readByte()
                + "," + kq.readUTF());
        final List<GoiDauTruong.DoiThu> ds = new ArrayList<GoiDauTruong.DoiThu>();
        final GoiDauTruong.DoiThu x = new GoiDauTruong.DoiThu();
        x.id = 9;
        x.ten = "Lá";
        x.diem = 990;
        x.suc = 2050;
        x.dex = new int[]{1, 4, 7};
        ds.add(x);
        final DataInputStream dt = new DataInputStream(new ByteArrayInputStream(ghi(o -> GoiDauTruong.doiThu(o, ds, 8, ""))));
        final StringBuilder sd = new StringBuilder();
        sd.append(dt.readByte()).append(',').append(dt.readByte()).append(',').append(dt.readInt()).append(',').append(dt.readUTF()).append(',')
                .append(dt.readShort()).append(',').append(dt.readByte()).append(',').append(dt.readInt()).append(',').append(dt.readByte());
        for (int i = 0; i < 3; i++) {
            sd.append(',').append(dt.readShort()).append('.').append(dt.readByte()).append('.').append(dt.readByte());
        }
        sd.append(',').append(dt.readByte()).append(',').append(dt.readUTF());
        Kiem.bang("doi thu: thu tu truong", "1,1,9,Lá,990,0,2050,0,1.0.0,4.0.0,7.0.0,8,", sd.toString());
        Kiem.bang("doi thu: doc het", Integer.valueOf(0), Integer.valueOf(dt.available()));
        // -109 chi mot noi xu ly o may chu, client co lop doc
        final String xl = new String(Files.readAllBytes(Paths.get("src/real/MapXuLyLenh.java")), StandardCharsets.UTF_8);
        Kiem.bang("MapXuLyLenh: dung mot case -109", Integer.valueOf(1), Integer.valueOf(xl.split("case -109:", -1).length - 1));
        final String cl = new String(Files.readAllBytes(Paths.get("../client_jar/src/game/pokemon/DauTruong.java")), StandardCharsets.UTF_8);
        Kiem.dung("client DauTruong.LENH = -109", cl.contains("LENH = -109"));
        final String mh = new String(Files.readAllBytes(Paths.get("../client_jar/src/game/networklogic/MessageHandler.java")), StandardCharsets.UTF_8);
        Kiem.dung("client MessageHandler giao goi -109 cho DauTruong", mh.contains("game.pokemon.DauTruong.nhan(msg)"));
    }

    static void docKhoi(final DataInputStream q, final DauTruongTran tr) throws Exception {
        int ok = 0;
        for (int ben = 0; ben < 2; ben++) {
            ok += q.readByte() == tr.dangRa[ben] ? 1 : 0;
            for (int i = 0; i < 3; i++) {
                ok += q.readShort() == tr.doi[ben][i].hp ? 1 : 0;
                ok += q.readByte() == tr.doi[ben][i].trangThai ? 1 : 0;
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int m = 0; m < 4; m++) {
                ok += q.readByte() == tr.doi[0][i].pp[m] ? 1 : 0;
            }
        }
        q.readByte();
        q.readByte();
        q.readByte();
        Kiem.bang("khoi trang thai: dang ra, HP, trang thai, PP", Integer.valueOf(2 + 12 + 12), Integer.valueOf(ok));
    }

    // ------------------------------------------------------------------ SQL

    static void kiemSql() throws Exception {
        final String s = DayDu.phanNangCap("them_dau_truong.sql");
        Kiem.dung("sql/nang_cap.sql co khoi them_dau_truong.sql", s.length() > 1000);
        for (final String b : new String[]{"tob_dau_truong_doi", "tob_dau_truong_mua", "tob_dau_truong_tran", "tob_dau_truong_thuong"}) {
            Kiem.dung("khoi dau truong: CREATE TABLE IF NOT EXISTS `" + b + "`", s.contains("CREATE TABLE IF NOT EXISTS `" + b + "`"));
        }
        Kiem.sai("khoi dau truong: khong DROP / DELETE / TRUNCATE", s.contains("DROP ") || s.contains("DELETE ") || s.contains("TRUNCATE"));
        Kiem.sai("khoi dau truong: khong INSERT ... SELECT", s.matches("(?s).*INSERT[^;]*SELECT.*"));
        Kiem.dung("khoi dau truong: luat bang nguoi choi (KiemDuLieuSql.khongPhaNguoiChoi)", KiemDuLieuSql.khongPhaNguoiChoi(s));
        final Map<String, List<Map<String, Object>>> b = KiemLoaiDayDu.docSql(s);
        final List<Map<String, Object>> dh = b.get("data_danh_hieu");
        Kiem.bang("3 danh hieu mua 9870..9872", Integer.valueOf(3), Integer.valueOf(dh == null ? 0 : dh.size()));
        int ok = 0;
        for (final Map<String, Object> r : dh) {
            final int id = KiemLoaiDayDu.so(r, "id");
            ok += KiemLoaiDayDu.chu(r, "ma").equals(DauTruongLogic.maDanhHieu(id)) && KiemLoaiDayDu.so(r, "thoi_han_phut") == 131040
                    && KiemLoaiDayDu.so(r, "phan_nghin") <= 10 && "trao".equals(KiemLoaiDayDu.chu(r, "dieu_kien")) ? 1 : 0;
        }
        Kiem.bang("danh hieu mua: ma khop DauTruongLogic, co han 1 mua, chi so <= 1% (ngan sach o), dieu kien trao", Integer.valueOf(3),
                Integer.valueOf(ok));
        // DDL may chu tao (DauTruongGame.taoBang) cung cot voi SQL
        final String g = new String(Files.readAllBytes(Paths.get("src/real/DauTruongGame.java")), StandardCharsets.UTF_8);
        for (final String bang : new String[]{"tob_dau_truong_doi", "tob_dau_truong_mua", "tob_dau_truong_tran", "tob_dau_truong_thuong"}) {
            final int a = s.indexOf("CREATE TABLE IF NOT EXISTS `" + bang + "`");
            final String ddl = s.substring(a, s.indexOf(") ENGINE", a));
            final java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^  `(\\w+)` ").matcher(ddl);
            final List<String> cot = new ArrayList<String>();
            while (m.find()) {
                cot.add(m.group(1));
            }
            final int ga = g.indexOf("CREATE TABLE IF NOT EXISTS " + bang + " (");
            final String gd = ga < 0 ? "" : g.substring(ga, g.indexOf("DEFAULT CHARSET", ga));
            int thieu = 0;
            for (final String c : cot) {
                thieu += gd.contains(" " + c + " ") || gd.contains("(" + c + " ") ? 0 : 1;
            }
            Kiem.bang(bang + ": DauTruongGame.taoBang co du cot SQL (" + cot.size() + ")", Integer.valueOf(0), Integer.valueOf(thieu));
        }
    }
}
