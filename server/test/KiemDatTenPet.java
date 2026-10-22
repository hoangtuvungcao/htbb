import com.htbb.pokemon.DatTenPet;
import com.htbb.pokemon.GoiPokemon;
import com.htbb.pokemon.NapTienLogic;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The dat ten Pokemon (0.9.6, yeu cau chu game 19/10/2026): nhan tren dau pet + the dat ten cho MOT dong chi so nho, co
 * kiem soat (com.htbb.pokemon.DatTenPet, real.DatTenPetGame). Kiem: goi -94 mo rong (client cu van doc duoc), ten (do dai,
 * co dau, ky tu, tu cam + lach luat), quay dong (bien, phan bo nghieng ve thap, cau hinh sai -> mac dinh), tran (o the ten +
 * tran tong trang tri 5%), dat lai khong cong don, nhat ky kinh te, noi day trong game (truoc kep tran, pet khoa giao dich),
 * cau hinh trung tam, SQL migration (idempotent, khong mau hong MariaDB 10.3) + seed day du, icon, cua hang luong.
 */
public final class KiemDatTenPet {

    private KiemDatTenPet() {
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    public static void chay() {
        Kiem.nhom("The dat ten Pokemon: nhan pet, ten, dong chi so co tran (DatTenPet / DatTenPetGame)");
        goi();
        ten();
        quay();
        tran();
        datLai();
        nhatKy();
        noiDay();
        cauHinh();
        sql();
        cuaHang();
    }

    // ------------------------------------------------------------------ goi -94 / -106

    static void goi() {
        try {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final DataOutputStream d = new DataOutputStream(b);
            GoiPokemon.ghiPet(d, 7, 25, false);
            DatTenPet.ghiTenPet(d, "Sấm Sét", true);
            final byte[] goi = b.toByteArray();
            // client CU (0.9.5 / Unity): doc dung 5 byte dau, phan thua bo qua
            final DataInputStream cu = new DataInputStream(new ByteArrayInputStream(goi));
            Kiem.bang("-94 client cu: charId", Integer.valueOf(7), Integer.valueOf(cu.readShort()));
            Kiem.bang("-94 client cu: dex", Integer.valueOf(25), Integer.valueOf(cu.readShort()));
            Kiem.bang("-94 client cu: shiny", Integer.valueOf(0), Integer.valueOf(cu.readUnsignedByte()));
            // client MOI: doc tiep phan mo rong khi con byte
            Kiem.dung("-94 con phan mo rong", cu.available() > 0);
            Kiem.bang("-94 phien ban ten", Integer.valueOf(DatTenPet.PHIEN_BAN_TEN), Integer.valueOf(cu.readUnsignedByte()));
            Kiem.bang("-94 ten hien (co dau)", "Sấm Sét", cu.readUTF());
            Kiem.bang("-94 co ten rieng", Integer.valueOf(1), Integer.valueOf(cu.readUnsignedByte()));
            Kiem.bang("-94 het goi", Integer.valueOf(0), Integer.valueOf(cu.available()));

            final ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            DatTenPet.ghiKetQua(new DataOutputStream(b2), DatTenPet.KQ_OK, "Đặt tên thành công", "Dòng thẻ tên: +1,5% HP");
            final DataInputStream k = new DataInputStream(new ByteArrayInputStream(b2.toByteArray()));
            Kiem.bang("-106 kieu", Integer.valueOf(0), Integer.valueOf(k.readUnsignedByte()));
            Kiem.bang("-106 tieu de", "Đặt tên thành công", k.readUTF());
            Kiem.bang("-106 noi dung", "Dòng thẻ tên: +1,5% HP", k.readUTF());
        } catch (final Exception e) {
            Kiem.bang("goi -94/-106 khong loi", "", String.valueOf(e));
        }
        Kiem.bang("-106 so lenh", Integer.valueOf(-106), Integer.valueOf(DatTenPet.LENH_KET_QUA));
        final String th = doc("src/real/TienHoaPokemon.java");
        final int iPet = th.indexOf("GoiPokemon.ghiPet(m.dos, ch.id, dex");
        Kiem.dung("TienHoaPokemon.taoPokePet ghi phan mo rong NGAY SAU ghiPet (gui cho minh + nguoi gan qua chuoi -90)",
                iPet > 0 && th.indexOf("DatTenPetGame.ghiMoRong(m.dos, ch);", iPet) > iPet
                        && th.indexOf("DatTenPetGame.ghiMoRong(m.dos, ch);", iPet) - iPet < 300);
        final String c = doc("../client_jar/src/game/pokemon/PokeMang.java");
        Kiem.dung("client doc phan mo rong -94 chi khi con byte (server cu khong gui)", c.contains("r.available() > 0")
                && c.contains("c.pokePetTen = tenPet;"));
        Kiem.dung("client -106 = HopThongBao (the trang kieu Pokemon GO)", c.contains("DAT_TEN_KET_QUA = -106") && c.contains("HopThongBao.mo(tieuDe, noiDung"));
        Kiem.dung("server -106 chi cho client so ban >= dat_ten_pet.ban_client_popup, con lai hop thoai 37",
                doc("src/real/DatTenPetGame.java").contains("p.session.banClient >= ban")
                        && doc("src/real/DatTenPetGame.java").contains("createServerAlertMessage(tieuDe + \"\\n\" + noiDung"));
        Kiem.dung("-106 chua dung o server (ngoai DatTenPet)", !doc("src/real/HanhDongVatPham.java").contains("-106")
                && !doc("src/real/DungVatPham.java").contains("-106"));
        final String nhan = doc("../client_jar/src/game/pokemon/NhanPet.java");
        Kiem.dung("client nhan pet = 'ten · chu', qua NhanTheGioi.pet", nhan.contains("append(\" · \")") && nhan.contains("NhanTheGioi.pet("));
        final String ntg = doc("../client_jar/src/game/pokemon/NhanTheGioi.java");
        Kiem.dung("client NhanTheGioi: nhan pet nho hon (coPet < coNhan), mau diu, chong thi BO (khong ve mo)",
                ntg.contains("static float coPet()") && ntg.contains("return ChuDe.dt() ? 6.2f : 5.6f;")
                        && ntg.contains("if (LOAI[i] == PET || laQuai && p == 5) {"));
        Kiem.dung("client AnimalMove.paintName -> NhanPet (moi pet: minh + nguoi khac)",
                doc("../client_jar/src/game/model/AnimalMove.java").contains("game.pokemon.NhanPet.ve(this);"));
    }

    // ------------------------------------------------------------------ ten

    static List<DatTenPet.TuCam> tuCamTuSql() {
        final List<DatTenPet.TuCam> ds = new ArrayList<DatTenPet.TuCam>();
        final String s = DayDu.phanNangCap("them_the_dat_ten_pet.sql");
        final int i = s.indexOf("INSERT IGNORE INTO `data_tu_cam_ten`");
        final int j = s.indexOf(";", i);
        if (i < 0 || j < 0) {
            return ds;
        }
        final Matcher m = Pattern.compile("\\('([^']*)','(tu|chuoi)',(\\d)\\)").matcher(s.substring(i, j));
        while (m.find()) {
            if ("1".equals(m.group(3))) {
                ds.add(new DatTenPet.TuCam(m.group(1), m.group(2)));
            }
        }
        return ds;
    }

    static String kiem(final String tho, final List<DatTenPet.TuCam> tc) {
        return DatTenPet.kiemTen(DatTenPet.chuanHoa(tho), 2, 12, tc);
    }

    static void ten() {
        final List<DatTenPet.TuCam> tc = tuCamTuSql();
        Kiem.dung("doc duoc tu cam tu SQL (" + tc.size() + ")", tc.size() >= 30);
        final String[] dung = {"Sấm Sét", "Pikachu", "Đỏ", "Bé Na 2", "  Mây   Trắng  ", "Ỷ Thiên", "Lồng Đèn", "Badminton",
            "Phượng Hoàng", "ab"};
        for (final String t : dung) {
            Kiem.bang("ten hop le: '" + t + "'", null, kiem(t, tc));
        }
        Kiem.bang("chuan hoa: gop dau cach, bo dau cuoi", "Mây Trắng", DatTenPet.chuanHoa("  Mây   Trắng  "));
        final String nfd = Normalizer.normalize("Sấm Sét", Normalizer.Form.NFD);
        Kiem.dung("chuan hoa: NFD (go tren may khac) -> NFC", "Sấm Sét".equals(DatTenPet.chuanHoa(nfd)) && nfd.length() > 7);
        Kiem.bang("do dai dem ky tu (khong dem byte)", Integer.valueOf(13), Integer.valueOf(DatTenPet.doDai("Ỷ Thiên Đồ Ký")));
        final String[] sai = {"A", "", "   ", "Mười Ba Chữ Cáii", "abc!", "Pika·chu", "Pika_chu", "1234", "Пикачу", "ピカチュウ",
            "Pika\uD83D\uDE00"};
        for (final String t : sai) {
            Kiem.dung("ten bi tu choi: '" + t + "'", kiem(t, tc) != null);
        }
        final String[] tuc = {"Lồn", "LỒN", "con đĩ", "Địt", "cứt", "D m m", "đ m m", "dmm", "VCL", "v c l", "Fuck", "F u c k", 
            "Admin", "GM", "Đù", "Đụ", "Vãi Dmm", "ditme", "d1tme", "5hit"};
        for (final String t : tuc) {
            Kiem.dung("tu cam bi chan: '" + t + "'", kiem(t, tc) != null);
        }
        Kiem.bang("tu cam: khong noi tu nao", "Tên có từ không phù hợp, hãy chọn tên khác.", kiem("Lồn", tc));
        Kiem.dung("tu cam 'tu' chi khop CA TU ('Lồng' khong bi chan vi 'lồn')", kiem("Lồng", tc) == null);
        Kiem.dung("tu cam: loi doc bang (null) = khong co tu (game dong cua rieng)", DatTenPet.kiemTen("Pikachu", 2, 12, null) == null);
        Kiem.dung("khung: bo dau + so giong chu", "ditme".equals(DatTenPet.khung("Đ1t M3")));
        final String g = doc("src/real/DatTenPetGame.java");
        Kiem.dung("game: loi doc tu cam -> KHONG dat ten (dong cua)", g.contains("if (tuCam == null) {"));
        Kiem.dung("game: ten sai -> mo lai o nhap, the chua tru", g.contains("moONhap(p, pet, loi); // giu phien, cho nhap lai"));
        final String mt = doc("src/real/MapNhapText.java");
        final int hook = mt.indexOf("DatTenPetGame.xuLyNhap(player, idActor, textGoc)");
        Kiem.dung("MapNhapText: o nhap ten pet nhan chuoi GOC (truoc toLowerCase)", hook > 0 && hook < mt.indexOf("textGoc.trim().toLowerCase()"));
        // RealController dong goi lai -31 voi chuoi DA ha chu thuong truoc khi toi Map (loi bot bat duoc 19/10/2026: "sấm sét")
        final String rc = doc("src/real/RealController.java");
        final int hookRc = rc.indexOf("DatTenPetGame.xuLyNhap(p, idActor, textGoc)");
        Kiem.dung("RealController: o nhap ten pet nhan chuoi GOC truoc khi dong goi lai chu thuong", hookRc > 0
                && hookRc < rc.indexOf("String text = textGoc.trim().toLowerCase();"));
    }

    // ------------------------------------------------------------------ quay

    static List<DatTenPet.Dong> bangSql() {
        final List<DatTenPet.Dong> ds = new ArrayList<DatTenPet.Dong>();
        final String s = DayDu.phanNangCap("them_the_dat_ten_pet.sql");
        final Matcher m = Pattern.compile("\\((\\d+),'(\\w+)','[^']*',(\\d+),(\\d+),(\\d),'[^']*'\\)").matcher(s);
        while (m.find()) {
            if ("1".equals(m.group(5))) {
                ds.add(new DatTenPet.Dong(Integer.parseInt(m.group(1)), m.group(2), Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4))));
            }
        }
        return ds;
    }

    static void quay() {
        final List<DatTenPet.Dong> bang = DatTenPet.locBang(bangSql());
        Kiem.bang("bang dong SQL: 5 dong hop le (hp, tan cong, phong thu, chi mang, ne)", Integer.valueOf(5), Integer.valueOf(bang.size()));
        final List<DatTenPet.Dong> tho = new ArrayList<DatTenPet.Dong>(bang);
        tho.add(new DatTenPet.Dong(90, "exp", 50, 100));
        tho.add(new DatTenPet.Dong(91, "hp", 0, 100));
        tho.add(new DatTenPet.Dong(92, "hp", 10, 300));
        Kiem.bang("locBang bo ma la / trong so 0 / he so > 100", Integer.valueOf(5), Integer.valueOf(DatTenPet.locBang(tho).size()));
        Kiem.dung("bang rong -> khong quay", DatTenPet.quay(new ArrayList<DatTenPet.Dong>(), DatTenPet.MUC_MAC_DINH,
                DatTenPet.TRONG_SO_MAC_DINH, 30, new Random(1)) == null);

        final Random r = new Random(20261019L);
        final int N = 200000;
        final int[] demMuc = new int[DatTenPet.MUC_MAC_DINH.length];
        final java.util.Map<String, Integer> demMa = new java.util.HashMap<String, Integer>();
        long tong = 0L;
        int max = 0;
        int min = Integer.MAX_VALUE;
        boolean ngoai = false;
        for (int i = 0; i < N; i++) {
            final DatTenPet.KetQua k = DatTenPet.quay(bang, DatTenPet.MUC_MAC_DINH, DatTenPet.TRONG_SO_MAC_DINH,
                    DatTenPet.TRAN_DONG_MAC_DINH, r);
            demMuc[k.muc]++;
            demMa.merge(k.ma, 1, Integer::sum);
            tong += k.phanNghin;
            max = Math.max(max, k.phanNghin);
            min = Math.min(min, k.phanNghin);
            final int ky = DatTenPet.MUC_MAC_DINH[k.muc] * ("chi_mang".equals(k.ma) || "ne_tranh".equals(k.ma) ? 50 : 100) / 100;
            ngoai |= k.phanNghin != ky || !DatTenPet.maHopLe(k.ma);
        }
        Kiem.dung("200k lan quay: moi dong = muc x he so, ma hop le", !ngoai);
        Kiem.dung("200k lan quay: <= tran dong 30‰ (max " + max + ")", max <= DatTenPet.TRAN_DONG_MAC_DINH);
        Kiem.dung("200k lan quay: >= 5‰ (min " + min + ")", min >= 5);
        for (int i = 1; i < demMuc.length; i++) {
            Kiem.dung("phan bo nghieng ve thap: muc " + i + " (" + demMuc[i] + ") < muc " + (i - 1) + " (" + demMuc[i - 1] + ")",
                    demMuc[i] < demMuc[i - 1]);
        }
        final double moc0 = demMuc[0] / (double) N;
        Kiem.dung("muc thap nhat ~55% (" + String.format("%.3f", moc0) + ")", moc0 > 0.53 && moc0 < 0.57);
        final double moc3 = demMuc[3] / (double) N;
        Kiem.dung("muc cao nhat ~3% (" + String.format("%.3f", moc3) + ")", moc3 > 0.025 && moc3 < 0.035);
        final double tb = tong / (double) N;
        Kiem.dung("gia tri trung binh <= 14‰ (+1,4%) (" + String.format("%.2f", tb) + ")", tb <= 14.0);
        final double hp = demMa.getOrDefault("hp", 0) / (double) N;
        Kiem.dung("chi so theo trong so bang (hp ~30%: " + String.format("%.3f", hp) + ")", hp > 0.28 && hp < 0.32);

        Kiem.sai("cau hinh muc tang trong so (muc cao ra nhieu hon) = sai", DatTenPet.mucHopLe(new int[]{10, 20}, new int[]{10, 20}, 30));
        Kiem.sai("cau hinh muc vuot tran dong = sai", DatTenPet.mucHopLe(new int[]{10, 40}, new int[]{50, 10}, 30));
        Kiem.sai("cau hinh muc khong tang dan = sai", DatTenPet.mucHopLe(new int[]{20, 10}, new int[]{50, 10}, 30));
        Kiem.sai("cau hinh muc lech do dai = sai", DatTenPet.mucHopLe(new int[]{10, 20}, new int[]{50}, 30));
        Kiem.dung("cau hinh mac dinh hop le", DatTenPet.mucHopLe(DatTenPet.MUC_MAC_DINH, DatTenPet.TRONG_SO_MAC_DINH, 30));
        final Random r2 = new Random(5);
        boolean duoiTran = true;
        for (int i = 0; i < 5000; i++) {
            final DatTenPet.KetQua k = DatTenPet.quay(bang, new int[]{10, 60}, new int[]{1, 99}, 30, r2);
            duoiTran &= k.phanNghin <= 30 && k.phanNghin <= 15 * 2;
        }
        Kiem.dung("cau hinh sai (muc 60‰, trong so tang) -> dung mac dinh, van <= tran", duoiTran);
        final Random r3 = new Random(9);
        boolean hepTran = true;
        for (int i = 0; i < 5000; i++) {
            hepTran &= DatTenPet.quay(bang, DatTenPet.MUC_MAC_DINH, DatTenPet.TRONG_SO_MAC_DINH, 12, r3).phanNghin <= 12;
        }
        Kiem.dung("ha tran dong (12‰) -> moi dong <= 12‰ du muc mac dinh lon hon", hepTran);
    }

    // ------------------------------------------------------------------ tran

    static void tran() {
        Kiem.bang("hieu luc: dong 30 trong o 30, chua co trang tri khac", Integer.valueOf(30), Integer.valueOf(DatTenPet.hieuLuc(30, 30, 50, 0)));
        Kiem.bang("hieu luc: kep o the ten 20", Integer.valueOf(20), Integer.valueOf(DatTenPet.hieuLuc(30, 20, 50, 0)));
        Kiem.bang("hieu luc: con 10‰ trong tran trang tri 5%", Integer.valueOf(10), Integer.valueOf(DatTenPet.hieuLuc(30, 30, 50, 40)));
        Kiem.bang("hieu luc: 4 o trang tri du 5% (2+1+1+1) -> the ten 0", Integer.valueOf(0),
                Integer.valueOf(DatTenPet.hieuLuc(30, 30, 50, 20 + 10 + 10 + 10)));
        Kiem.bang("hieu luc: trang tri khac vuot tran -> 0 (khong am)", Integer.valueOf(0), Integer.valueOf(DatTenPet.hieuLuc(30, 30, 50, 70)));
        Kiem.bang("hieu luc: dong am -> 0", Integer.valueOf(0), Integer.valueOf(DatTenPet.hieuLuc(-5, 30, 50, 0)));
        final DatTenPet.TrangThai t = new DatTenPet.TrangThai("Sấm Sét", "hp", 30, 1);
        Kiem.bang("chi so khac dong -> 0", Integer.valueOf(0), Integer.valueOf(DatTenPet.phanNghin(t, "tan_cong", 30, 50, 0)));
        Kiem.bang("chi so dung dong -> qua tran", Integer.valueOf(20), Integer.valueOf(DatTenPet.phanNghin(t, "hp", 20, 50, 0)));
        Kiem.bang("pet chua dat ten -> 0", Integer.valueOf(0), Integer.valueOf(DatTenPet.phanNghin(null, "hp", 30, 50, 0)));
        // ngan sach 5.12 thiet ke can bang: luyen chieu 15 + bo 5 + TRANG TRI (gom the ten) 5 + dong hanh 4 + dong hiem 20 = 49 <= 50
        int xauNhat = 0;
        for (int khac = 0; khac <= 50; khac += 5) {
            xauNhat = Math.max(xauNhat, khac + DatTenPet.hieuLuc(30, 30, 50, khac));
        }
        Kiem.bang("trang tri + the ten xau nhat = 5% (khong vuot tran trang tri)", Integer.valueOf(50), Integer.valueOf(xauNhat));
        Kiem.dung("ngan sach % cong them xau nhat 15+5+5+4+20 = 49% <= chi_so.tran_phan_tram_cong 50%",
                150 + 50 + xauNhat + 40 + 200 <= com.htbb.pokemon.GioiHanChiSo.PHAN_TRAM_CONG_TRAN);
        final String g = doc("src/real/DatTenPetGame.java");
        Kiem.dung("game: chi pet DANG DI THEO (petUsing) duoc tinh", g.contains("return p == null ? 0 : phanNghin(p, p.petUsing, ma);"));
        Kiem.dung("game: chi khi gioi han chi so bat (moi cong don sau do qua tran)", g.contains("|| !ChiSoTrangBiGame.bat()) {"));
        Kiem.dung("game: tran o / tran trang tri doc cau hinh nhung kep cung (khong mo rong qua mac dinh)",
                g.contains("Math.min(DatTenPet.TRAN_TRANG_TRI_MAC_DINH, so(DatTenPet.K_TRAN_TRANG_TRI")
                        && g.contains("Math.min(DatTenPet.TRAN_DONG_MAC_DINH * 2, so(DatTenPet.K_TRAN_DONG"));
    }

    // ------------------------------------------------------------------ dat lai

    static void datLai() {
        final Random r = new Random(77);
        final List<DatTenPet.Dong> bang = DatTenPet.locBang(bangSql());
        DatTenPet.TrangThai t = null;
        boolean khongCongDon = true;
        for (int lan = 1; lan <= 50; lan++) {
            final DatTenPet.KetQua k = DatTenPet.quay(bang, DatTenPet.MUC_MAC_DINH, DatTenPet.TRONG_SO_MAC_DINH, 30, r);
            t = DatTenPet.datLai(t, "Ten" + lan, k);
            int tong = 0;
            for (final String ma : DatTenPet.MA_HOP_LE) {
                tong += DatTenPet.phanNghin(t, ma, 30, 50, 0);
            }
            khongCongDon &= tong == k.phanNghin && t.soLan == lan && ("Ten" + lan).equals(t.ten);
        }
        Kiem.dung("50 lan dat lai: tong moi chi so = CHI dong cuoi (khong cong don), so lan tang", khongCongDon);
        Kiem.dung("50 lan dat lai: van <= tran dong", t.phanNghin <= 30);
        final String g = doc("src/real/DatTenPetGame.java");
        Kiem.dung("game: luu = UPSERT theo id_pet (moi pet 1 dong)", g.contains("INSERT INTO tob_pet_dat_ten") && g.contains("PRIMARY KEY (id_pet)")
                && g.contains("ON DUPLICATE KEY UPDATE id_char = VALUES(id_char), ten = VALUES(ten), chi_so = VALUES(chi_so)"));
        Kiem.dung("game: tru the TRUOC khi quay, luu loi thi hoan the", g.indexOf("p.iItems.remove(the)") < g.indexOf("DatTenPet.quay(bang")
                && g.contains("Char.createItemSoLuong(ph.idMau, 1, p); // hoan the"));
    }

    // ------------------------------------------------------------------ nhat ky

    static void nhatKy() {
        final DatTenPet.TrangThai cu = new DatTenPet.TrangThai("Cũ", "hp", 10, 1);
        final DatTenPet.TrangThai moi = new DatTenPet.TrangThai("Sấm Sét", "tan_cong", 15, 2);
        final Object[] d = DatTenPet.dongNhatKy(11, "vantron", 345, 25, cu, moi, 1261, "ok");
        Kiem.bang("nhat ky: so cot = COT_NHAT_KY", Integer.valueOf(DatTenPet.COT_NHAT_KY.length), Integer.valueOf(d.length));
        Kiem.bang("nhat ky: ten cu / dong cu / ten moi / dong moi / item / ket qua", "Cũ|hp:10|Sấm Sét|tan_cong:15|1261|ok",
                d[4] + "|" + d[5] + "|" + d[6] + "|" + d[7] + "|" + d[8] + "|" + d[9]);
        final Object[] d0 = DatTenPet.dongNhatKy(11, null, 345, 0, null, moi, 1261, "ok");
        Kiem.bang("nhat ky: lan dau (chua co ten cu)", "|", d0[4] + "|" + d0[5]);
        final String g = doc("src/real/DatTenPetGame.java");
        Kiem.dung("nhat ky ghi o ca nhanh ok va hoan the", g.contains("ghiNhatKy(p, pet, cu, moi, ph.idMau, \"ok\");")
                && g.contains("ghiNhatKy(p, pet, cu, moi, ph.idMau, \"hoan_the\");"));
        final int i = g.indexOf("INSERT INTO tob_log_dat_ten");
        final String cauLenh = i < 0 ? "" : g.substring(i, g.indexOf(";", i));
        int dau = 0;
        for (final char c : cauLenh.toCharArray()) {
            dau += c == '?' ? 1 : 0;
        }
        Kiem.bang("nhat ky: so tham so INSERT = 1 (luc) + COT_NHAT_KY", Integer.valueOf(DatTenPet.COT_NHAT_KY.length + 1), Integer.valueOf(dau));
        Kiem.dung("nhat ky: con ghi log KINH_TE (tep kiem toan)", g.contains("com.htbb.log.Nhat.thongTin(\"KINH_TE\""));
        Kiem.dung("bang nhat ky co du cot COT_NHAT_KY", cotCo(g, "tob_log_dat_ten"));
    }

    static boolean cotCo(final String g, final String bang) {
        final int i = g.indexOf("CREATE TABLE IF NOT EXISTS " + bang);
        if (i < 0) {
            return false;
        }
        final String tao = g.substring(i, g.indexOf("DEFAULT CHARSET", i));
        for (final String c : DatTenPet.COT_NHAT_KY) {
            if (!tao.contains(" " + c + " ")) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ noi day trong game

    static void noiDay() {
        final String cs = doc("src/real/CharChiSo.java");
        final int hp = cs.indexOf("DatTenPetGame.phanNghin(self, com.htbb.pokemon.DatTenPet.HP)");
        Kiem.dung("HP: cong dong the ten TRUOC kepPhanTram (trong chi_so.tran_phan_tram_cong)", hp > 0
                && hp < cs.indexOf("self.maxhp = ChiSoTrangBiGame.kepPhanTram(self.maxhp, hpGoc * them / 1000L);"));
        final int pt = cs.indexOf("DatTenPetGame.phanNghin(self, com.htbb.pokemon.DatTenPet.PHONG_THU)");
        Kiem.dung("Phong thu: TRUOC kepPhanTram", pt > 0 && pt < cs.indexOf("self.defend_physic = ChiSoTrangBiGame.kepPhanTram(self.defend_physic, gocVat);"));
        final int tc = cs.indexOf("DatTenPetGame.phanNghin(self, com.htbb.pokemon.DatTenPet.TAN_CONG)");
        Kiem.dung("Tan cong: TRUOC kepPhanTram", tc > 0 && tc < cs.indexOf("self.attack = ChiSoTrangBiGame.kepPhanTram(self.attack, gocTc);"));
        Kiem.dung("Chi mang: vao chiMangTho (giam dan / tran 40%)", doc("src/real/ChiSoTrangBiGame.java")
                .contains("+ DatTenPetGame.phanNghin(p, com.htbb.pokemon.DatTenPet.CHI_MANG))")); // cung tong voi danh hieu, truoc tran
        final String ch = doc("src/real/Char.java");
        final int ne = ch.indexOf("DatTenPetGame.phanNghin(this, petUsing, com.htbb.pokemon.DatTenPet.NE_TRANH)");
        Kiem.dung("Ne: trong haveNeTranh (pet bi tat o Moba thi khong tinh), truoc tran 900", ne > ch.indexOf("public int haveNeTranh()")
                && ne < ch.indexOf("if (ne > 900) {", ne));
        Kiem.dung("Ne bo qua pet (hoa giai ky nang pet) KHONG tinh dong the ten",
                !ch.substring(ch.indexOf("public int haveNeTranhBoQuaPet()"), ch.indexOf("public int haveNeTranh()")).contains("DatTenPetGame"));
        final String pet = doc("src/data/Pet.java");
        final int ct = pet.indexOf("public boolean canTrade() {");
        Kiem.dung("pet da dat ten KHONG giao dich (khoa khi mac, chan nuoi pet co dong roi ban / clone)", ct > 0
                && pet.indexOf("real.DatTenPetGame.daDatTen(this)", ct) > ct && pet.indexOf("real.DatTenPetGame.daDatTen(this)", ct) - ct < 300);
        Kiem.dung("the thong tin pet: ten rieng + loai + dong", pet.contains("return real.DatTenPetGame.dongThongTin(this) + this.getInfoSendUserGoc();"));
        Kiem.dung("ten + dong KHONG luu vao tob_pet.name (khong vao chuoi SQL ghep cua savePet)", !doc("src/data/Database.java").contains("tenDat"));
        Kiem.dung("nap ten + dong cung ket noi getCharPet, khop id_pet + chu", doc("src/data/Database.java").contains("real.DatTenPetGame.napChoChar(p, conn);")
                && doc("src/real/DatTenPetGame.java").contains("pet.idDB == idPet && pet.ownerid == p.charDBID"));
        Kiem.dung("dung the: hieu ung 'dat_ten' trong data_vat_pham_lau_dai (du lieu, khong ghi cung id)",
                doc("src/real/VatPhamDungLauDai.java").contains("if (hu.containsKey(DatTenPetGame.HIEU_UNG)) {"));
        Kiem.dung("menu chon pet noi o MapDoMenu", doc("src/real/MapDoMenu.java").contains("DatTenPetGame.xuLyMenu(player, idNpc, idMenu, idOptionMenu)"));
        Kiem.dung("TeamServer tao bang luc khoi dong", doc("src/server/TeamServer.java").contains("real.DatTenPetGame.taoBang();"));
        Kiem.dung("chi pet vinh vien, da luu CSDL moi dat ten duoc", doc("src/real/DatTenPetGame.java")
                .contains("pet.idDB > 0 && pet.totalMinute == 0 && !pet.ispetthoihan && pet.getTimeExpire() > 0"));
        Kiem.dung("NPC ao -561 / o nhap -32061 khong trung NapTienGame", real.DatTenPetGame.NPC_CHON_PET < com.htbb.pokemon.NapTienGame.NPC_RAO_GIA
                && real.DatTenPetGame.INPUT_TEN != real.CharDoiTenNhanVat.INPUT_DOI_TEN);
        Kiem.bang("mo ta dong", "+1,5% HP", DatTenPet.moTa("hp", 15));
        Kiem.bang("mo ta dong nguyen", "+3% Tấn công", DatTenPet.moTa("tan_cong", 30));
    }

    // ------------------------------------------------------------------ cau hinh

    static void cauHinh() {
        final String j = DayDu.danhMucJson(); // danh muc trong CSDL
        // 0.9.8 (ban-098): gia / muc / trong so chuyen lop co_dinh (giu <= 70 khoa chinh tren web)
        final String[][] chinh = {};
        Kiem.dung("cau hinh luon bat (cong tac khan cap, server.ini) " + DatTenPet.K_BAT, dongJson(j, DatTenPet.K_BAT).contains("\"lop\": \"luon_bat\""));
        final String[][] coDinh = {{DatTenPet.K_MUA_TUAN, String.valueOf(DatTenPet.MUA_TUAN_MAC_DINH)},
            {DatTenPet.K_TRAN_DONG, String.valueOf(DatTenPet.TRAN_DONG_MAC_DINH)},
            {DatTenPet.K_TRAN_TRANG_TRI, String.valueOf(DatTenPet.TRAN_TRANG_TRI_MAC_DINH)},
            {DatTenPet.K_DAI_MIN, String.valueOf(DatTenPet.DAI_MIN_MAC_DINH)}, {DatTenPet.K_DAI_MAX, String.valueOf(DatTenPet.DAI_MAX_MAC_DINH)},
            {DatTenPet.K_BAN_POPUP, String.valueOf(DatTenPet.BAN_POPUP_MAC_DINH)},
            {DatTenPet.K_GIA_LUONG, String.valueOf(DatTenPet.GIA_LUONG_MAC_DINH)}, {DatTenPet.K_MUC, "10,15,20,30"}, {DatTenPet.K_TRONG_SO, "55,30,12,3"}};
        for (final String[] k : chinh) {
            final String dong = dongJson(j, k[0]);
            Kiem.dung("cau hinh chinh (tunable) " + k[0] + " = " + k[1], dong.contains("\"macDinh\": \"" + k[1] + "\"") && !dong.contains("\"lop\""));
        }
        for (final String[] k : coDinh) {
            final String dong = dongJson(j, k[0]);
            Kiem.dung("cau hinh co dinh (fixed) " + k[0] + " = " + k[1], dong.contains("\"macDinh\": \"" + k[1] + "\"")
                    && dong.contains("\"lop\": \"co_dinh\""));
        }
        Kiem.bang("mac dinh muc = DatTenPet", "10,15,20,30", join(DatTenPet.MUC_MAC_DINH));
        Kiem.bang("mac dinh trong so = DatTenPet", "55,30,12,3", join(DatTenPet.TRONG_SO_MAC_DINH));
        final String nc = DayDu.nangCap();
        Kiem.dung("nang_cap.sql phan 2 co dat_ten_pet.* + trang_tri.tran_tong_phan_nghin", nc.contains("VALUES ('dat_ten_pet.trong_so_muc'")
                && nc.contains("VALUES ('trang_tri.tran_tong_phan_nghin'"));
    }

    static String join(final int[] a) {
        final StringBuilder b = new StringBuilder();
        for (final int x : a) {
            b.append(b.length() > 0 ? "," : "").append(x);
        }
        return b.toString();
    }

    static String dongJson(final String j, final String khoa) {
        final int i = j.indexOf("\"khoa\": \"" + khoa + "\"");
        if (i < 0) {
            return "";
        }
        return j.substring(i, j.indexOf('\n', i));
    }

    // ------------------------------------------------------------------ SQL + icon

    static void sql() {
        final String m = DayDu.phanNangCap("them_the_dat_ten_pet.sql").replaceAll("(?m)--.*$", ""); // bo chu thich
        Kiem.dung("migration co", m.contains("CREATE TABLE"));
        Kiem.dung("migration KHONG dung INSERT ... SELECT ... ON DUPLICATE KEY UPDATE (hong MariaDB 10.3)",
                !Pattern.compile("(?is)INSERT[^;]*SELECT[^;]*ON DUPLICATE KEY UPDATE").matcher(m).find()
                        && !Pattern.compile("(?i)ON DUPLICATE KEY UPDATE\\s+`?(\\w+)`?\\s*=\\s*`?\\1`?").matcher(m).find());
        int insert = 0;
        int ignore = 0;
        final Matcher mi = Pattern.compile("(?m)^INSERT( IGNORE)? INTO").matcher(m);
        while (mi.find()) {
            insert++;
            ignore += mi.group(1) != null ? 1 : 0;
        }
        Kiem.dung("migration idempotent: moi INSERT la INSERT IGNORE (" + ignore + "/" + insert + ")", insert > 0 && insert == ignore);
        Kiem.dung("migration: moi CREATE TABLE la IF NOT EXISTS", !Pattern.compile("CREATE TABLE (?!IF NOT EXISTS)").matcher(m).find());
        Kiem.dung("migration: khong DROP / DELETE", !m.contains("DROP ") && !m.contains("DELETE "));
        Kiem.dung("tu cam: cot tu utf8_bin (đụ / đù / đĩ / đỉ khac nhau, INSERT IGNORE khong nuot dong)", m.contains("COLLATE utf8_bin"));
        final String d = doc("../sql/htbb_day_du.sql");
        Kiem.dung("seed day du: data_item 1261 icon 14502", d.contains("(1261,'Thẻ đặt tên Pokémon',28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14502,"));
        Kiem.dung("seed day du: cong dung + 2 nguon kiem duoc + mon cua hang luong", d.contains("(1261,'the_dat_ten_pet','Thẻ đặt tên Pokémon','tien_ich','dat_ten:1'")
                && d.contains("(128,'Đổi Tem nghiên cứu','Thẻ đặt tên Pokémon','item:1054x8','ITEM:1261:1',10,0,-1,1)")
                && d.contains("(129,'Lượng khoá (Kecleon)','Thẻ đặt tên Pokémon','luong_khoa:40','ITEM:1261:1',10,0,-1,1)")
                && d.contains("('the_dat_ten_pet','Thẻ đặt tên Pokémon','tien_ich','dat_ten_pet',100,0,0,0,0,0,1,24,'')"));
        for (final String b : new String[]{"data_dong_the_ten", "data_tu_cam_ten", "tob_pet_dat_ten", "tob_log_dat_ten"}) {
            Kiem.dung("seed day du + migration co bang " + b, DayDu.coBang(b) && m.contains("CREATE TABLE IF NOT EXISTS `" + b + "`"));
        }
        Kiem.dung("seed day du: danh muc cau hinh co dat_ten_pet.*", d.contains("('dat_ten_pet.gia_luong','dat_ten_pet',"));
        final File icon = new File("map/icon_pokemon/502.png");
        boolean png = false;
        try {
            final java.awt.image.BufferedImage a = javax.imageio.ImageIO.read(icon);
            png = a != null && a.getWidth() == 16 && a.getHeight() == 16;
        } catch (final Exception e) {
            png = false;
        }
        Kiem.dung("icon PNG that 16x16 map/icon_pokemon/502.png (= 14502)", png);
        Kiem.dung("bang_icon.csv co dong 502", doc("../du_lieu/bang_icon.csv").contains("\n502,14502,"));
        Kiem.dung("README sql ghi khoi them_the_dat_ten_pet.sql (nang_cap.sql phan 11)", doc("../sql/README.md").contains("them_the_dat_ten_pet.sql"));
    }

    // ------------------------------------------------------------------ cua hang luong

    static void cuaHang() {
        NapTienLogic.Mon the = null;
        for (final NapTienLogic.Mon m : NapTienLogic.MON_MAC_DINH) {
            if (NapTienLogic.MA_THE_DAT_TEN.equals(m.ma)) {
                the = m;
            }
        }
        Kiem.dung("MON_MAC_DINH co the dat ten", the != null);
        if (the == null) {
            return;
        }
        Kiem.bang("mon the dat ten hop le", null, NapTienLogic.kiemMon(the));
        Kiem.dung("trao dat_ten_pet trong whitelist", java.util.Arrays.asList(NapTienLogic.TRAO_HOP_LE).contains(NapTienLogic.TRAO_DAT_TEN_PET));
        Kiem.dung("the dat ten: khong giao dich, khong chi so tren mon", !the.giaoDichDuoc && !the.coChiSo);
        Kiem.dung("ma khac voi trao dat_ten_pet bi tu choi", NapTienLogic.kiemMon(new NapTienLogic.Mon("the_khac", "Thẻ", "tien_ich",
                NapTienLogic.TRAO_DAT_TEN_PET, 100, 0, 0, 0, false)) != null);
        Kiem.dung("the dat ten giao dich duoc bi tu choi", NapTienLogic.kiemMon(new NapTienLogic.Mon(NapTienLogic.MA_THE_DAT_TEN, "Thẻ",
                "tien_ich", NapTienLogic.TRAO_DAT_TEN_PET, 100, 0, 0, 0, true)) != null);
        Kiem.dung("the dat ten gioi_han dong != 0 bi tu choi (cau hinh quyet)", NapTienLogic.kiemMon(new NapTienLogic.Mon(
                NapTienLogic.MA_THE_DAT_TEN, "Thẻ", "tien_ich", NapTienLogic.TRAO_DAT_TEN_PET, 100, 0, 5, 0, false)) != null);
        Kiem.dung("mon co chi so van bi cam", NapTienLogic.kiemMon(new NapTienLogic.Mon(NapTienLogic.MA_THE_DAT_TEN, "Thẻ", "tien_ich",
                NapTienLogic.TRAO_DAT_TEN_PET, 100, 0, 0, 0, false, true)) != null);
        final String g = doc("src/com/htbb/pokemon/NapTienGame.java");
        Kiem.dung("mua the: gia + gioi han tuan tu cau hinh, dem theo tuan, vao tui KHOA", g.contains("final int gia = giaThat(m);")
                && g.contains("real.DatTenPetGame.muaTuanToiDa()") && g.contains("tangToc || datTen ? m.ma + \"@\"")
                && g.contains("real.VatPhamDungLauDai.idTheoHieuUng(real.DatTenPetGame.HIEU_UNG)"));
        Kiem.dung("mua the: so cai ghi dung gia that", g.contains("NapTienDb.ghiSoCai(p.userID, p.charDBID, \"tieu\", -gia,"));
        Kiem.dung("web: kiem mon dat_ten_pet giong server", doc("../web/server/utils/napTienThuan.ts").contains("function kiemDatTen(m: MonCuaHang)"));
    }
}
