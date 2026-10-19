import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Kich ban bot THE DAT TEN POKEMON (0.9.6, com.htbb.pokemon.DatTenPet / real.DatTenPetGame) tren MAY CHU THU RIENG (can GM
 * slv / smn, -Ddb de chen pet + doi chieu CSDL):
 *   1. chen MOT pet vinh vien dang di theo (tob_pet place 1) cho nhan vat bot, dang nhap lai -> -94 co duoi ten (ten loai);
 *   2. doi the o So tay -> Cua hang & doi -> "Lượng khoá (Kecleon)" (ban KIEM DUOC, 40 luong khoa);
 *   3. bam Su dung the -> menu chon pet (NPC ao -561) -> o nhap ten (-32061);
 *   4. ten tuc ("d m m") -> mo lai o nhap, the CHUA bi tru;
 *   5. ten dung ("Sấm Sét") -> the bi tru, ket qua (37 hoac -106 voi -Dban=906), -94 mang "Sấm Sét", CSDL tob_pet_dat_ten
 *      (1 dong, so_lan 1, gia tri trong tran) + tob_log_dat_ten (ok);
 *   6. dat lai ("Mây") -> van 1 dong, so_lan 2 (khong cong don), nhat ky 2 dong;
 *   7. dang nhap lai -> -94 luc vao game mang "Mây" (nap tu CSDL).
 *
 *   java -cp /tmp/bot -Ddb=htbb_db -Ddbport=3389 [-Dban=906] BotChucNang 127.0.0.1 29429 kt dat_ten_pet
 */
final class KichBanDatTenPet {

    private KichBanDatTenPet() {
    }

    static void dangKy() {
        BotChucNang.dk("dat_ten_pet", true, KichBanDatTenPet::chay);
    }

    static final int NPC = -561, INPUT = -32061, THE = 1261;

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    /** Ten hien trong duoi -94 cuoi cung (cua charId) tu moc; null = khong co / khong duoi. */
    static String tenPet94(BotChucNang.Bot b, int tu, int charId) {
        String ten = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -94 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                int id = r.readShort();
                r.readShort();
                r.readUnsignedByte();
                if (id != charId || r.available() == 0) continue;
                if (r.readUnsignedByte() < 1) continue;
                ten = r.readUTF();
            } catch (Exception ignored) {
            }
        }
        return ten;
    }

    /** Ket qua dat ten: -106 (tieu de | noi dung) hoac 37 tu moc. */
    static String ketQua(BotChucNang.Bot b, int tu) {
        StringBuilder s = new StringBuilder();
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -106 && e.d != null) {
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    int k = r.readUnsignedByte();
                    s.append("[-106 kieu ").append(k).append("] ").append(r.readUTF()).append(" | ").append(r.readUTF()).append(" ; ");
                } catch (Exception ignored) {
                }
            } else if ((e.cmd == 37 || e.cmd == -104) && e.chu != null) {
                s.append("[").append(e.cmd).append("] ").append(e.chu).append(" ; ");
            }
        }
        return s.toString().replace('\n', ' ');
    }

    static boolean coNhap(BotChucNang.Bot b, int tu) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -31 && e.so != null && e.so[0] == INPUT) return true;
        }
        return false;
    }

    static String chuNhap(BotChucNang.Bot b, int tu) {
        String c = "";
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -31 && e.so != null && e.so[0] == INPUT) c = e.chu;
        }
        return c;
    }

    static BotChucNang.Bot vaoLai(BotChucNang.Bot b) throws Exception {
        String u = b.user;
        b.close();
        Thread.sleep(2500);
        BotChucNang.Bot n = new BotChucNang.Bot(u);
        String l = n.dangNhap("123456");
        if (l != null) {
            Thread.sleep(3000);
            n.close();
            n = new BotChucNang.Bot(u);
            l = n.dangNhap("123456");
        }
        if (l != null) throw new IllegalStateException(u + " dang nhap lai: " + l);
        l = n.vaoGame(0);
        if (l != null) throw new IllegalStateException(u + " " + l);
        return n;
    }

    /** Doi 1 the o Kecleon (luong khoa). Tra ve so the trong tui sau khi doi. */
    static int doiThe(BotChucNang.Bot b) throws IOException {
        BotChucNang.sql("DELETE FROM tob_mua_tuan WHERE id_char=" + b.charDb); // gioi han 1 lan / tuan: kich ban doi nhieu lan
        String s = KichBanDungVatPham.muaCuaHang(b, "Kecleon", "Thẻ đặt tên");
        b.lamMoi();
        int n = KichBanDungVatPham.dem(b, THE);
        System.out.println("  doi the: " + s + " -> " + n + " the");
        return n;
    }

    /** Bam dung the -> chon pet 0 -> o nhap. true neu toi o nhap. */
    static boolean moNhap(BotChucNang.Bot b) throws IOException {
        int id = KichBanDungVatPham.idMon(b, THE);
        if (id == KichBanDS3.KHONG) {
            System.out.println("  khong thay the trong tui");
            return false;
        }
        int m = b.moc();
        b.gui(29, (short) id);
        if (b.doiMenu(m, NPC, 0, 3000) <= 0) {
            System.out.println("  khong co menu chon pet: " + b.chuTu(m));
            return false;
        }
        System.out.println("  menu: " + b.menuChu);
        int m2 = b.moc();
        b.menu(NPC, 0, 0);
        b.cho(x -> coNhap(x, m2), 3000);
        if (!coNhap(b, m2)) System.out.println("  khong co o nhap: " + b.chuTu(m2));
        return coNhap(b, m2);
    }

    static void chay() throws Exception {
        BotChucNang.Bot b = KichBanDS3.botLang(25);
        try {
            if (BotChucNang.DB.isEmpty()) {
                ket("dat_ten.csdl", null, "can -Ddb");
                return;
            }
            int cid = b.charDb;
            BotChucNang.sql("DELETE FROM tob_pet WHERE owner=" + cid);
            BotChucNang.sql("DELETE FROM tob_pet_dat_ten WHERE id_char=" + cid);
            BotChucNang.sql("DELETE FROM tob_log_dat_ten WHERE id_char=" + cid);
            b.gm("smn " + b.ten + " 0 0 500", 600); // luong khoa cho Kecleon
            b = vaoLai(b);
            // pet kieu cu vinh vien (mau 0), dang di theo (place 1)
            BotChucNang.sql("INSERT INTO tob_pet(owner,place,info,idImg,name,itPet) VALUES (" + cid + ",1,'0,"
                    + System.currentTimeMillis() + ",0,1,0,0,0,-1,0,-1,-1,0,0,-1,0,0,0',0,'pet','')");
            int m0 = b.moc();
            b = vaoLai(b);
            String t0 = tenPet94(b, 0, b.actor);
            ket("dat_ten.94_luc_vao", t0 != null && !t0.isEmpty(), "ten hien luc vao game (chua dat ten) = '" + t0 + "'");

            int n0 = doiThe(b);
            ket("dat_ten.doi_the_kecleon", n0 >= 1, "the trong tui: " + n0);
            if (n0 < 1) return;

            boolean nhap = moNhap(b);
            ket("dat_ten.menu_o_nhap", nhap, "menu -561 -> o nhap -32061");
            if (!nhap) return;
            int m = b.moc();
            b.gui(-31, (short) INPUT, (byte) 0, "d m m");
            b.cho(x -> coNhap(x, m), 3000);
            b.lamMoi();
            ket("dat_ten.ten_tuc_mo_lai", coNhap(b, m) && chuNhap(b, m).contains("không phù hợp") && KichBanDungVatPham.dem(b, THE) == n0,
                    "nhap lai: " + chuNhap(b, m) + ", the " + KichBanDungVatPham.dem(b, THE));

            int m2 = b.moc();
            b.gui(-31, (short) INPUT, (byte) 0, "Sấm Sét");
            b.bom(2000);
            b.lamMoi();
            String kq = ketQua(b, m2);
            ket("dat_ten.thanh_cong", kq.contains("Đặt tên thành công") && kq.contains("Sấm Sét"), kq);
            ket("dat_ten.tru_the", KichBanDungVatPham.dem(b, THE) == n0 - 1, "the " + n0 + " -> " + KichBanDungVatPham.dem(b, THE));
            ket("dat_ten.94_ten_moi", "Sấm Sét".equals(tenPet94(b, m2, b.actor)), "-94 sau dat ten: '" + tenPet94(b, m2, b.actor) + "'");
            String dong = BotChucNang.sql1("SELECT CONCAT(ten,'|',chi_so,'|',gia_tri,'|',so_lan) FROM tob_pet_dat_ten WHERE id_char=" + cid);
            boolean trongTran = false;
            if (dong != null && dong.split("\\|").length == 4) {
                int v = Integer.parseInt(dong.split("\\|")[2]);
                trongTran = v >= 1 && v <= 30;
            }
            ket("dat_ten.csdl_dong", dong != null && dong.startsWith("Sấm Sét|") && dong.endsWith("|1") && trongTran, "tob_pet_dat_ten: " + dong);
            ket("dat_ten.nhat_ky", "1".equals(BotChucNang.sql1("SELECT COUNT(*) FROM tob_log_dat_ten WHERE id_char=" + cid + " AND ket_qua='ok' AND ten_moi='Sấm Sét'")),
                    "tob_log_dat_ten ok: " + BotChucNang.sql1("SELECT COUNT(*) FROM tob_log_dat_ten WHERE id_char=" + cid));

            // dat lai: quay lai, khong cong don
            int n1 = doiThe(b);
            if (n1 >= 1 && moNhap(b)) {
                int m3 = b.moc();
                b.gui(-31, (short) INPUT, (byte) 0, "Mây");
                b.bom(2000);
                String kq2 = ketQua(b, m3);
                String d2 = BotChucNang.sql1("SELECT CONCAT(COUNT(*),'|',MAX(ten),'|',MAX(so_lan)) FROM tob_pet_dat_ten WHERE id_char=" + cid);
                ket("dat_ten.dat_lai", kq2.contains("Mây") && "1|Mây|2".equals(d2), "ket qua: " + kq2 + " csdl: " + d2);
                ket("dat_ten.nhat_ky_2", "2".equals(BotChucNang.sql1("SELECT COUNT(*) FROM tob_log_dat_ten WHERE id_char=" + cid + " AND ket_qua='ok'")),
                        "dong nhat ky ok: " + BotChucNang.sql1("SELECT COUNT(*) FROM tob_log_dat_ten WHERE id_char=" + cid));
            } else {
                ket("dat_ten.dat_lai", false, "khong doi / mo duoc the thu 2 (" + n1 + ")");
            }
            b = vaoLai(b);
            ket("dat_ten.nap_lai_tu_csdl", "Mây".equals(tenPet94(b, 0, b.actor)), "-94 luc vao lai: '" + tenPet94(b, 0, b.actor) + "'");
        } finally {
            b.close();
        }
    }
}
