import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Kich ban cua bo hoi quy NGUOI CHOI THAT (HoiQuy). Moi kich ban: tai khoan dang ky bang duong that, khong GM; chuan
 * bi (neu co) qua HoiQuy.datCap / capTien / duTuoi / giftcode, ghi ro trong bao cao.
 * Moi loi nguoi choi bao (24/10/2026) la mot kich ban VINH VIEN (ten trong ngoac).
 */
final class KichBanHoiQuy {

    private KichBanHoiQuy() {
    }

    static void ket(String muc, Boolean ok, String bang) {
        HoiQuy.ket(muc, ok, bang);
    }

    static void dangKy() {
        HoiQuy.dk("dk_dang_ky", "dang ky trong game (#dk2) -> dang nhap -> tao nhan vat -> vao game; trung ten", KichBanHoiQuy::dangKyMoi);
        HoiQuy.dk("dk_han_muc_ip", "(loi 24/10: dang ky bi chan vi chung IP) han muc theo mang: dung cau bao, dung so", KichBanHoiQuy::hanMucIp);
        HoiQuy.dk("chon_nv_da_xoa", "(loi: chon Pokemon da xoa bao 'bao tri') xoa -> chon -> khoi phuc", KichBanHoiQuy::chonDaXoa);
        HoiQuy.dk("hd_lam", "(loi: moi huong dan lap lai) huong dan tan thu nhanh 'Lam': moi mot lan, 10 buoc, dang nhap lai khong moi", KichBanHoiQuy::huongDanLam);
        HoiQuy.dk("hd_bo_qua", "huong dan nhanh 'Bo qua': hop xac nhan, du thuong, diem len cap qua EXP nhiem vu, khong moi lai", KichBanHoiQuy::huongDanBoQua);
        HoiQuy.dk("di_bo", "di bo lang -> Pallet -> lang: khong keo lui, toc do client that duoc nhan, hack x3 bi chan", KichBanHoiQuy::diBo);
        HoiQuy.dk("ve_lang", "Ve lang mien phi (So tay) tu Pallet; Day thoat hiem / hoi sinh ve lang", KichBanHoiQuy::veLang);
        HoiQuy.dk("cua_ai", "(loi: cua ai chua nhan ma ha quai khong tinh) ha truoc khi nhan khong tinh, hop 95 nhan, Quay nhan, dem dung", KichBanHoiQuy::cuaAi);
        HoiQuy.dk("nv_muc_tieu", "(loi: muc tieu ghi sai ban do / so luong 0/4 vs 5) moi nhiem vu nhan duoc o cap 1-40: quai, ban do, so", KichBanHoiQuy::nvMucTieu);
        HoiQuy.dk("gym", "(loi: Gym trong lang 0 sat thuong; boss hien lai / 2 con; duong Gym sai) thach dau, danh, ha, khong hien lai", KichBanHoiQuy::gym);
        HoiQuy.dk("tau_new_bark", "(loi: len tau New Bark khi chua mo cong -> ket nap lai map 107) tu choi len tau, cuu khi luu tren thuyen", KichBanHoiQuy::tauNewBark);
        HoiQuy.dk("shop_xu", "shop NPC (xu): tru dung gia, vat pham vao tui, so sach", KichBanHoiQuy::shopXu);
        HoiQuy.dk("shop_dac_biet", "(loi: mua shop dac biet vao o an; gia luong khoa hien thanh luong) luong / luong khoa / xu", KichBanHoiQuy::shopDacBiet);
        HoiQuy.dk("giftcode", "giftcode: nhan duoc, nhap lai, tui day khong mat qua", KichBanHoiQuy::giftcode);
        HoiQuy.dk("luu_dang_nhap_lai", "cap / xu / luong khoa / vat pham / ban do giu nguyen sau dang nhap lai", KichBanHoiQuy::luuDangNhapLai);
        HoiQuy.dk("hai_phien", "hai phien cung tai khoan: phien cu bi day ra, khong nhan doi tien", KichBanHoiQuy::haiPhien);
        HoiQuy.dkCham("cot_truyen", "cot truyen tu cap 1 (khong GM): nhiem vu chinh, len cap, diem len cap, nhiem vu phu", KichBanHoiQuy::cotTruyen);
        KichBanHoiQuy2.dangKy();
    }

    // ================================================================== tien ich
    static int soTay(BotChucNang.Bot b, String muc) throws IOException {
        KichBanDS4.moSoTay(b);
        int i = b.timMuc(muc);
        int m = b.moc();
        if (i >= 0) b.menu(-527, 0, i);
        b.bom(900);
        return i < 0 ? -1 : m;
    }

    /** Roi san 105, chon "Bỏ qua" huong dan (Dong y) -> o lang, cap ~4. */
    static void boQuaHuongDan(BotChucNang.Bot b) throws IOException {
        KichBanHuongDan.roiSan(b);
        int m = b.moc();
        b.menu(KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 1);
        b.cho(x -> x.coLenh(m, -32), 3000);
        BotChucNang.SuKien p = b.layLenh(m, -32);
        b.gui(-32, (short) (p == null ? b.actor : p.so[0]), (byte) KichBanHuongDan.POPUP_BO_QUA, "", (byte) 1);
        b.bom(2000);
        b.lamMoi();
    }

    /** Ha n quai mau tpl tren ban do hien tai bang di bo + danh (khong GM). */
    static int haMauDiBo(BotChucNang.Bot b, int tpl, int n) throws IOException {
        int ha = 0;
        for (int vong = 0; vong < 30 && ha < n && !b.dut; vong++) {
            List<Integer> song = KichBanDS.timQuai(b, 0, 300);
            KichBanHuongDan.dongBoViTri(b);
            Integer chon = null;
            long gan = Long.MAX_VALUE;
            for (int id : song) {
                int[] q = b.quai.get(id);
                if (q == null || q[5] != tpl) continue;
                long d = (long) (q[0] - b.px) * (q[0] - b.px) + (long) (q[1] - b.py) * (q[1] - b.py);
                if (d < gan) { gan = d; chon = id; }
            }
            if (chon == null) {
                // di toi o sinh gan nhat cua loai (luoi sinh server/map) roi tim lai
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                int[] toi = null;
                double kc = 1e18;
                for (int[] o : BotXuyenSuot.oQuai(b.mapLoad)) {
                    if (o[1] != tpl) continue;
                    double d = Math.hypot(o[2] - b.px / 16.0, o[3] - b.py / 16.0) + (vong % 3) * 7 * Math.random();
                    if (d < kc) { kc = d; toi = o; }
                }
                int[] o = toi == null || bd == null ? null : bd.oGan(toi[2], toi[3], 3);
                if (o != null) b.diDuong(bd, o[0], o[1]); else b.bom(2500);
                continue;
            }
            if (KichBanHoiQuy2.haQuaiDiBo(b, chon, 80) > 0) ha++;
        }
        return ha;
    }

    static int dem(BotChucNang.Bot b, int tu, int cmd) {
        int n = 0;
        for (int i = tu; i < b.moc(); i++) if (b.su.get(i).cmd == cmd) n++;
        return n;
    }

    /** Diem huan luyen / tiem nang chua cong + da cong (tob_char.basic: 5 = tiem nang chua cong, 6 = ky nang). */
    static String basic(int charDb) {
        return BotChucNang.sql1("SELECT basic FROM tob_char WHERE id=" + charDb);
    }

    // ================================================================== dang ky
    static void dangKyMoi() throws Exception {
        String ten = HoiQuy.tenMoi("dk");
        String[] k = HoiQuy.dangKy(ten, "hq-may-" + ten);
        ket("dk.dang_ky", k[1] == null, k[1] == null ? ten : k[1]);
        if (k[1] != null) return;
        String[] k2 = HoiQuy.dangKy(ten, "hq-may-" + ten + "x");
        ket("dk.trung_ten", k2[1] != null && BotChucNang.khongDau(k2[1]).contains("da ton tai"), String.valueOf(k2[1]));
        BotChucNang.Bot b = new BotChucNang.Bot(ten);
        BatBien.gan(b);
        try {
            String l = b.dangNhap(HoiQuy.MAT_KHAU);
            ket("dk.dang_nhap", l == null, String.valueOf(l));
            if (l != null) return;
            l = b.vaoGame(0, 0);
            ket("dk.tao_nhan_vat_vao_game", l == null && b.mapId == 105, l == null ? "map " + b.mapId + " lv " + b.lv + " xu " + b.xu : l);
            ket("dk.khong_tien_tang", b.luong == 0 && b.lk == 0 && b.xu == 5000, "xu " + b.xu + " luong " + b.luong + " lk " + b.lk);
            HoiQuy.napTheGioi(b);
            // che do Diem huan luyen (production): client 0.9.17 xin (-122 k0) -> may chu tra -122 k1 co bat = 1; client
            // hien nut "Huấn luyện" thay "Tiềm năng" (loi anh chup 24/10)
            int mh = b.moc();
            b.gui(-122, (byte) 0);
            b.bom(1500);
            int hl = -1;
            for (int i = mh; i < b.moc(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -122 && e.d != null && e.d.length >= 2 && e.d[0] == 1) hl = e.d[1];
            }
            ket("dk.che_do_huan_luyen", hl == 1, "goi -122 k1 bat=" + hl);
            BotChucNang.Bot sai = new BotChucNang.Bot(ten);
            String l2 = sai.dangNhap("matkhausai");
            sai.close();
            ket("dk.sai_mat_khau", l2 != null, String.valueOf(l2));
        } finally {
            HoiQuy.thoat(b);
        }
    }

    static void hanMucIp() throws Exception {
        if (BotChucNang.DB.isEmpty()) { ket("dk_ip", null, "can -Ddb"); return; }
        // han muc / gio = 2 (cau_hinh dang_ky.toi_da_gio, chinh song tren web). Cung MOT may (ma may) dang ky 3 lan: lan 3
        // bi chan voi cau bao noi ro; khac may cung mang: sau WAF / proxy tin cay (GioiHanIp) khong dem chung IP - loi
        // 24/10 "dang ky bi chan vi chung IP" (mang di dong dung chung).
        HoiQuy.datCauHinh("dang_ky.toi_da_gio", "2");
        Thread.sleep(6500);
        String may = "hq-may-chung-" + HoiQuy.tenMoi("");
        List<String> kq = new ArrayList<>();
        int duoc = 0;
        String chan = null;
        for (int i = 0; i < 3; i++) {
            String ten = HoiQuy.tenMoi("ip");
            String[] k = HoiQuy.dangKy(ten, may);
            kq.add(ten + "=" + (k[1] == null ? "OK" : k[1]));
            if (k[1] == null) duoc++;
            else chan = k[1];
        }
        ket("dk_ip.cung_may_dung_han_muc", duoc == 2 && chan != null, kq.toString());
        ket("dk_ip.cau_bao_ro", chan != null && (BotChucNang.khongDau(chan).contains("thu lai") || BotChucNang.khongDau(chan).contains("lien he")), String.valueOf(chan));
        // khac may: 3 tai khoan cung IP
        kq.clear();
        String chanIp = null;
        int duocIp = 0;
        for (int i = 0; i < 3; i++) {
            String ten = HoiQuy.tenMoi("iq");
            String[] k = HoiQuy.dangKy(ten, "hq-may-" + ten);
            kq.add(ten + "=" + (k[1] == null ? "OK" : k[1]));
            if (k[1] == null) duocIp++; else chanIp = k[1];
        }
        ket("dk_ip.khac_may_cung_mang", duocIp == 3 || chanIp != null && BotChucNang.khongDau(chanIp).contains("dung chung mang"),
                (duocIp == 3 ? "khong dem chung IP (proxy tin cay / WAF): " : "dem theo IP (ket noi thang): ") + kq);
        HoiQuy.datCauHinh("dang_ky.toi_da_gio", "1000");
        Thread.sleep(6000);
        String[] k = null;
        for (int lan = 0; lan < 4; lan++) {
            k = HoiQuy.dangKy(HoiQuy.tenMoi("ip"), may);
            if (k[1] == null) break;
            Thread.sleep(5000);
        }
        ket("dk_ip.mo_lai_khi_nang_han_muc", k[1] == null, String.valueOf(k[1]));
    }

    static void chonDaXoa() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("xo", 0, 0);
        String u = b.user;
        int id = b.charDb;
        b.close();
        Thread.sleep(2500);
        try (BotChucNang.Bot c = new BotChucNang.Bot(u)) {
            BatBien.gan(c);
            c.dangNhap(HoiQuy.MAT_KHAU);
            int m = c.moc();
            c.gui(13, (byte) 2, id); // Xoa
            String t = c.doiChu(m, 4000, "xoá", "xóa", "bảo trì");
            ket("chon_nv_da_xoa.xoa", t != null && !BotChucNang.khongDau(t).contains("bao tri"), String.valueOf(t));
            for (int kieu : new int[]{1, 0}) {
                int m2 = c.moc();
                c.gui(13, (byte) kieu, id);
                c.bom(2500);
                String s = c.chuTu(m2);
                boolean vao = c.coLenh(m2, 12);
                ket("chon_nv_da_xoa.chon_kieu" + kieu, !BotChucNang.khongDau(s).contains("bao tri") && (!vao || kieu == 0) && (vao || s.contains("đã bị xoá")),
                        (vao ? "VAO GAME map " + c.mapId : "khong vao") + " | " + s);
                if (vao) return;
            }
            int m3 = c.moc();
            c.gui(13, (byte) 3, id); // Khoi phuc
            String t3 = c.doiChu(m3, 4000, "khôi phục", "bảo trì");
            ket("chon_nv_da_xoa.khoi_phuc", t3 != null && t3.contains("khôi phục") && !BotChucNang.khongDau(t3).contains("bao tri"), String.valueOf(t3));
            int m4 = c.moc();
            c.gui(13, (byte) 0, id);
            c.cho(x -> x.coLenh(m4, 12), 8000);
            ket("chon_nv_da_xoa.vao_sau_khoi_phuc", c.coLenh(m4, 12), c.chuTu(m4));
        }
    }

    // ================================================================== huong dan
    static void huongDanLam() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("hl", 0, 0);
        String u = b.user;
        try {
            int m = b.moc();
            KichBanHuongDan.roiSan(b);
            boolean moi = b.doiMenu(m, KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 3000) == 2;
            ket("hd_lam.moi", moi && b.timMuc("Bỏ qua") == 1, KichBanHuongDan.chuSau(b, m));
            int m2 = b.moc();
            b.menu(KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 0);
            b.bom(1000);
            KichBanHuongDan.Nv q = KichBanHuongDan.chinh(b, 0, 0);
            int capTruoc = b.lv, ptTruoc = b.pt;
            List<String> buoc = new ArrayList<>();
            for (int k = 0; k < KichBanHuongDan.SO_BUOC && q != null; k++) {
                String ly = KichBanHuongDan.lamBuoc(b, q, k);
                buoc.add(q.ten + (ly == null ? "" : " <" + ly + ">"));
                if (ly != null) { ket("hd_lam.buoc" + (k + 1), false, q + ": " + ly); break; }
                KichBanHuongDan.Nv sau = KichBanHuongDan.chinh(b, 0, 0);
                if (sau == null || sau.id != q.id + 1) break;
                q = sau;
            }
            ket("hd_lam.xong_10_buoc", q != null && q.id == KichBanHuongDan.SO_BUOC, "buoc: " + String.join(" | ", buoc) + " -> tiep " + q);
            // so lan moi trong ca qua trinh: dung 1 (moi lap lai = loi 24/10)
            int moiLai = 0;
            for (int i = m2; i < b.moc(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -30 && e.so != null && e.so[0] == KichBanHuongDan.NPC_SO_TAY && e.so[1] == KichBanHuongDan.MENU_MOI) moiLai++;
            }
            ket("hd_lam.khong_moi_lai_khi_choi", moiLai == 0, "menu moi them " + moiLai + " lan sau khi chon Lam");
            b.lamMoi();
            ket("hd_lam.diem_len_cap", b.lv <= capTruoc || b.pt >= ptTruoc, "cap " + capTruoc + "->" + b.lv + " diem " + ptTruoc + "->" + b.pt);
        } finally {
            HoiQuy.thoat(b);
        }
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            c.bom(3000);
            int moi = 0;
            for (BotChucNang.SuKien e : c.su) if (e.cmd == -30 && e.so != null && e.so[1] == KichBanHuongDan.MENU_MOI && e.so[0] == KichBanHuongDan.NPC_SO_TAY) moi++;
            ket("hd_lam.dang_nhap_lai_khong_moi", moi == 0, "menu moi khi vao lai: " + moi + " | map " + c.mapId);
            HoiQuy.thoat(c);
        }
    }

    static void huongDanBoQua() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("hb", 0, 0);
        String u = b.user;
        try {
            KichBanHuongDan.roiSan(b);
            b.lamMoi();
            int lv0 = b.lv, pt0 = b.pt;
            String bs0 = basic(b.charDb);
            long xu0 = b.xu;
            int m = b.moc();
            b.menu(KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 1);
            b.cho(x -> x.coLenh(m, -32), 3000);
            BotChucNang.SuKien p = b.layLenh(m, -32);
            ket("hd_bo_qua.hop", p != null && p.so[1] == KichBanHuongDan.POPUP_BO_QUA, p == null ? b.chuTu(m) : p.chu);
            int m2 = b.moc();
            b.gui(-32, (short) (p == null ? b.actor : p.so[0]), (byte) KichBanHuongDan.POPUP_BO_QUA, "", (byte) 1);
            b.bom(2500);
            b.lamMoi();
            StringBuilder sbc = new StringBuilder();
            for (int i = m2; i < b.moc(); i++) if (b.su.get(i).chu != null) sbc.append(b.su.get(i).chu).append(" | ");
            String chu = sbc.toString();
            ket("hd_bo_qua.thuong", b.xu - xu0 == 1450, "xu +" + (b.xu - xu0) + " | " + chu);
            // loi 24/10 "len cap qua EXP nhiem vu khong cong diem": moi cap len phai co diem (cau "Lên cấp N! +x Điểm ...")
            // moi cau "Lên cấp N! +X Điểm ..." co the gop nhieu cap: X / (N - cap truoc) phai deu nhau, cap cuoi = cap that
            Matcher mc = Pattern.compile("Lên cấp (\\d+)! \\+(\\d+) (Điểm huấn luyện|điểm tiềm năng)").matcher(chu);
            int tongDiem = 0, truoc = lv0, cuoi = lv0;
            Set<Integer> tiLe = new TreeSet<>();
            List<String> cau = new ArrayList<>();
            while (mc.find()) {
                int n = Integer.parseInt(mc.group(1)), x = Integer.parseInt(mc.group(2));
                cau.add(mc.group(0));
                if (n > truoc) tiLe.add(x / (n - truoc) * 1000 + x % (n - truoc));
                tongDiem += x;
                truoc = n;
                cuoi = n;
            }
            ket("hd_bo_qua.diem_moi_cap", b.lv > lv0 && cuoi == b.lv && tiLe.size() == 1, "cap " + lv0 + " -> " + b.lv + ": " + cau + " ti le/cap " + tiLe);
            ket("hd_bo_qua.diem_vao_nhan_vat", b.pt >= pt0 + tongDiem, "pt goi 3: " + pt0 + " -> " + b.pt + " (cong " + tongDiem + ") basic " + bs0 + " -> " + basic(b.charDb));
        } finally {
            HoiQuy.thoat(b);
        }
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            c.bom(2500);
            int moi = 0;
            for (BotChucNang.SuKien e : c.su) if (e.cmd == -30 && e.so != null && e.so[1] == KichBanHuongDan.MENU_MOI && e.so[0] == KichBanHuongDan.NPC_SO_TAY) moi++;
            ket("hd_bo_qua.khong_moi_lai", moi == 0, "menu moi khi vao lai: " + moi);
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== di bo + toc do
    /** Di thang {@code n} o theo truc, moi o cach {@code ms}; tra ve so lan server tra vi tri (keo lui). */
    static int diThang(BotChucNang.Bot b, BotChucNang.BanDo bd, int n, long ms) throws IOException {
        KichBanHuongDan.dongBoViTri(b);
        int x = b.px / 16, y = b.py / 16;
        int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int[] chon = null;
        int dai = 0;
        for (int[] h : hg) {
            int k = 0;
            while (k < n && bd.diDuoc(x + h[0] * (k + 1), y + h[1] * (k + 1)) && !bd.laCong(x + h[0] * (k + 1), y + h[1] * (k + 1))) k++;
            if (k > dai) { dai = k; chon = h; }
        }
        if (chon == null || dai < 4) return -1;
        int m = b.moc();
        for (int k = 1; k <= dai; k++) {
            b.diToi((x + chon[0] * k) * 16 + 8, (y + chon[1] * k) * 16 + 8);
            b.bom(ms);
        }
        b.bom(600);
        int keo = 0;
        for (int i = m; i < b.moc(); i++) if (b.su.get(i).cmd == 4 && b.su.get(i).chu != null) keo++;
        // quay lai cho cu
        for (int k = dai - 1; k >= 0; k--) {
            b.diToi((x + chon[0] * k) * 16 + 8, (y + chon[1] * k) * 16 + 8);
            b.bom(260);
        }
        b.bom(500);
        return keo;
    }

    static void diBo() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("db", 0, 0);
        try {
            boQuaHuongDan(b);
            HoiQuy.napTheGioi(b);
            int lang = b.mapId;
            boolean ra = KichBanGymLang.raPallet(b);
            ket("di_bo.lang_ra_pallet", ra, "map " + lang + " -> " + b.mapId);
            if (!ra) return;
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            b.bom(1500);
            // toc do client that: 5 px / khung 40 ms (TocDoDiChuyen) = 1 o / 128 ms; client gui moi 250 ms (~2 o / goi)
            // -> ca hai deu phai duoc nhan. Bot di 1 o / 130 ms.
            BatBien v = BatBien.of(b);
            int vp0 = BatBien.VI_PHAM.size();
            int keo = diThang(b, bd, 12, 130);
            ket("di_bo.toc_do_that_khong_keo_lui", keo == 0, "di 1 o / 130 ms: server tra vi tri " + keo + " lan (keo lui " + v.soKeoLui + ")");
            // hack toc do x3 (1 o / 40 ms): server phai chan (tra vi tri). Bat bien keo lui o day la mong doi -> bo khoi danh sach.
            int keoHack = diThang(b, bd, 12, 40);
            synchronized (BatBien.VI_PHAM) { while (BatBien.VI_PHAM.size() > vp0 && keoHack > 0) BatBien.VI_PHAM.remove(BatBien.VI_PHAM.size() - 1); }
            ket("di_bo.hack_toc_do_bi_chan", keoHack > 0, "di 1 o / 40 ms (x3): server tra vi tri " + keoHack + " lan");
            boolean ve = KichBanHuongDan.quaCong(b, 0);
            ket("di_bo.pallet_ve_lang", ve && KichBanDS.laLang(b.mapId), "map " + b.mapId);
        } finally {
            HoiQuy.thoat(b);
        }
    }

    // ================================================================== ve lang
    static void veLang() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("vl", 0, 0);
        try {
            boQuaHuongDan(b);
            if (!KichBanGymLang.raPallet(b)) { ket("ve_lang.ra_pallet", false, "map " + b.mapId); return; }
            int m = soTay(b, "Về làng");
            ket("ve_lang.co_muc_so_tay", m >= 0, String.join(" ; ", b.menuChu));
            if (m >= 0) {
                b.cho(x -> x.coLenh(m, 12), 5000);
                if (b.coLenh(m, 12)) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(1200); }
                ket("ve_lang.mien_phi_ve_lang", KichBanDS.laLang(b.mapId), "map " + b.mapId + " | " + b.chuTu(m));
            }
            // dang o lang roi: bam lai -> bao ro, khong nap lai map
            int m2 = soTay(b, "Về làng");
            b.bom(1500);
            ket("ve_lang.o_lang_khong_nap_lai", m2 < 0 || dem(b, m2, 12) == 0, b.chuTu(Math.max(0, m2)));
        } finally {
            HoiQuy.thoat(b);
        }
    }

    // ================================================================== cua ai
    static void cuaAi() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("ca", 0, 0);
        try {
            boQuaHuongDan(b);
            ket("cua_ai.cap_sau_bo_qua", b.lv >= 3, "lv " + b.lv);
            if (!KichBanGymLang.raPallet(b)) { ket("cua_ai.ra_pallet", false, "map " + b.mapId); return; }
            // ha Torchic (muc tieu CA-01.1) TRUOC khi nhan: khong tinh + hop 95 nhac
            int m = b.moc();
            int ha = haMauDiBo(b, 4, 2);
            b.bom(1200);
            KichBanTienDoNhiemVu.Dong ca0 = KichBanTienDoNhiemVu.tim(KichBanTienDoNhiemVu.ds(b, m, 2), 2);
            BotChucNang.SuKien hop = null;
            int soHop = 0;
            for (int i = m; i < b.moc(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -32 && e.so != null && e.so[1] == 95) { soHop++; hop = e; }
            }
            ket("cua_ai.chua_nhan_khong_tinh", ha > 0 && ca0 == null, "ha " + ha + " Torchic, cua ai trong so: " + ca0);
            ket("cua_ai.hop_95_nhac_mot_lan", soHop == 1, "so hop " + soHop + " " + (hop == null ? b.chuTu(m) : hop.chu));
            if (hop == null) {
                // duong Quay -> Nhiem vu cua toi -> Thu thach cua ai
                int mq = b.moc();
                boolean q = KichBanDS4.chonQuay(b, "Nhiệm vụ của tôi");
                b.doiMenu(mq, -527, 10, 3000);
                int i = b.timMuc("Thử thách cửa ải");
                ket("cua_ai.quay_co_muc", q && i >= 0, b.menuChu.toString());
                if (i < 0) return;
                int m2 = b.moc();
                b.menu(-527, 10, i);
                b.bom(1500);
                ket("cua_ai.quay_nhan", b.chuTu(m2).length() > 0, b.chuTu(m2));
            } else {
                int m2 = b.moc();
                b.gui(-32, (short) hop.so[0], (byte) 95, "", (byte) 1);
                b.bom(1500);
                KichBanTienDoNhiemVu.Dong ca = KichBanTienDoNhiemVu.tim(KichBanTienDoNhiemVu.ds(b, m2, 2), 2);
                ket("cua_ai.dong_y_nhan", ca != null && ca.td.size() == 1 && ca.td.get(0)[0] == 4, "so: " + KichBanTienDoNhiemVu.ds(b, m2, 2) + " | " + b.chuTu(m2));
            }
            int m3 = b.moc();
            int ha2 = haMauDiBo(b, 4, 3);
            b.bom(1200);
            KichBanTienDoNhiemVu.Dong ca2 = KichBanTienDoNhiemVu.tim(KichBanTienDoNhiemVu.ds(b, m3, 2), 2);
            ket("cua_ai.sau_nhan_dem", ca2 != null && ca2.td.get(0)[1] >= Math.min(ha2, 3) && ha2 > 0, "ha " + ha2 + " -> " + ca2);
            // khong co hop 95 nao them khi da nhan
            int hop2 = 0;
            for (int i = m3; i < b.moc(); i++) if (b.su.get(i).cmd == -32 && b.su.get(i).so != null && b.su.get(i).so[1] == 95) hop2++;
            ket("cua_ai.da_nhan_khong_nhac", hop2 == 0, "hop 95 them " + hop2);
        } finally {
            HoiQuy.thoat(b);
        }
    }

    // ================================================================== muc tieu nhiem vu (du lieu)
    /**
     * Moi nhiem vu nhan duoc o cap 1-40 (chinh + phu + ngay/tuan): so luong quai trong chu = so can, ban do trong chu = noi
     * quai sinh, quai co cho sinh. Doc ca bang data_quest_moi (giong cach server dung) + mo ta nguoi choi thay o so.
     */
    static void nvMucTieu() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("mt", 0, 0);
        try {
            KichBanHuongDan.roiSan(b);
            HoiQuy.napTheGioi(b);
            List<String> cot = BotChucNang.sql("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='data_quest_moi'");
            ket("nv_muc_tieu.bang", !cot.isEmpty(), cot.toString());
            List<String> sai = new ArrayList<>();
            int n = 0;
            for (String d : BotChucNang.sql("SELECT id, name, idMonskill, nmonskill, CONCAT(scontent, \" | \", content), lv FROM data_quest_moi WHERE lv <= 40 AND type = 0")) {
                String[] f = d.split("\t", -1);
                if (f.length < 6) continue;
                n++;
                String[] qs = f[2].replaceAll("[\\[\\]\\s]", "").split("[,;|]");
                String[] ts = f[3].replaceAll("[\\[\\]\\s]", "").split("[,;|]");
                for (int i = 0; i < qs.length; i++) {
                    if (qs[i].isEmpty() || !qs[i].matches("-?\\d+")) continue;
                    int tpl = Integer.parseInt(qs[i]);
                    if (tpl <= 0) continue;
                    int can = i < ts.length && ts[i].matches("\\d+") ? Integer.parseInt(ts[i]) : -1;
                    String tq = HoiQuy.TEN_QUAI.get(tpl);
                    Set<Integer> maps = BotXuyenSuot.QUAI_MAP.get(tpl);
                    if (maps == null || maps.isEmpty()) { sai.add(f[0] + " " + f[1] + ": quai " + tpl + " (" + tq + ") khong sinh o dau"); continue; }
                    if (tq == null) continue;
                    Matcher mm = Pattern.compile("(\\d+) " + Pattern.quote(tq) + "(?: ở ([^·,.(|\\n]+))?").matcher(f[4]);
                    if (mm.find()) {
                        if (can > 0 && Integer.parseInt(mm.group(1)) != can) sai.add(f[0] + " " + f[1] + ": chu '" + mm.group(0) + "' nhung can " + can);
                        if (mm.group(2) != null) {
                            String tb = mm.group(2).trim();
                            boolean khop = false;
                            for (int mid : maps) { String t = HoiQuy.TEN_MAP.get(mid); if (t != null && (t.contains(tb) || tb.contains(t))) khop = true; }
                            if (!khop) {
                                List<String> that = new ArrayList<>();
                                for (int mid : maps) that.add(String.valueOf(HoiQuy.TEN_MAP.get(mid)));
                                sai.add(f[0] + " " + f[1] + ": " + tq + " ghi o '" + tb + "' nhung sinh o " + that);
                            }
                        }
                    }
                }
            }
            ket("nv_muc_tieu.du_lieu_cap_1_40", n > 0 && sai.isEmpty(), n + " nhiem vu; " + sai.size() + " sai: " + String.join(" || ", sai.subList(0, Math.min(8, sai.size()))));
        } finally {
            HoiQuy.thoat(b);
        }
    }

    // ================================================================== Gym
    static void gym() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("gy", 0, 0);
        BotChucNang.Bot ban = null;
        try {
            boQuaHuongDan(b);
            HoiQuy.datCap(b, 40);
            // nguoi choi cong diem len cap vao chieu + suc manh nhu that
            b.gui(34, (byte) 0, (short) Math.max(1, b.pt));
            b.bom(400);
            for (int o = 0; o < 7; o++) for (int k = 0; k < 9; k++) { b.gui(36, (byte) o); b.bom(40); }
            b.lamMoi();
            // So tay -> Thach dau Gym: dung nhan "Gym Pewter"
            int m0 = soTay(b, "Thách đấu Gym");
            b.doiMenu(m0, -527, 5, 2500);
            ket("gym.so_tay_ds", m0 >= 0 && b.timMuc("Gym Pewter") >= 0, b.menuChu.toString());
            ban = HoiQuy.nguoiChoiMoi("gb", 0, 0); // nguoi dung canh: chi thay MOT boss
            KichBanHuongDan.roiSan(ban);
            Integer boss = KichBanGymLang.thachDauGym(b);
            ket("gym.trong_lang_hien_boss", boss != null, "map " + b.mapId + " boss " + boss + " | " + b.chuTu(Math.max(0, m0)));
            if (boss == null) return;
            // bam lai 2 lan: van mot boss
            KichBanGymLang.thachDauGym(b);
            b.bom(1500);
            List<Integer> song = KichBanDS.timQuai(b, 0, 400);
            int soBoss = 0;
            for (int id : song) { int[] q = b.quai.get(id); if (q != null && q[4] >= 2000 && q[4] == b.quai.get(boss)[4]) soBoss++; }
            ket("gym.mot_boss", soBoss <= 1, "boss song cung maxhp: " + soBoss);
            int[] h = KichBanGymLang.danh9(b, boss, 4);
            ket("gym.trong_lang_gay_sat_thuong", h[0] > 0 && h[1] < h[0], KichBanGymLang.bang("lenh 9", h, b));
            int m = b.moc();
            int don = KichBanHoiQuy2.haQuaiDiBo(b, boss, 400);
            String kq = b.doiChu(m, 4000, "Chiến thắng", "huy hiệu", "Huy hiệu");
            ket("gym.ha_boss", don > 0, don + " don | " + kq);
            Thread.sleep(9000);
            b.quai.remove(boss);
            b.gui(7, (short) (int) boss);
            b.bom(800);
            int[] q = b.quai.get(boss);
            ket("gym.khong_hien_lai", q == null || q[2] <= 0, "sau 9 giay boss " + (q == null ? "khong co" : "hp " + q[2]));
            int m2 = b.moc();
            KichBanGymLang.thachDauGym(b);
            b.bom(1500);
            ket("gym.thach_dau_lai_bao_ro", b.chuTu(m2).length() > 0, b.chuTu(m2));
        } finally {
            if (ban != null) HoiQuy.thoat(ban);
            HoiQuy.thoat(b);
        }
    }

    // ================================================================== tau New Bark
    static void tauNewBark() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("tn", 0, 0);
        String u = b.user;
        int id = b.charDb;
        boQuaHuongDan(b);
        HoiQuy.datCap(b, 32);
        HoiQuy.thoat(b);
        // CHUAN BI: nhan vat dung o Ben tau 106 co 2 ve tau (= mua ve o ben); chua lam cua ai Duong sang Johto
        HoiQuy.chuanBi("dat " + u + " o Ben tau 106 (20,12) + 2 ve tau (CSDL khi da thoat; = di bo toi ben + mua ve)");
        KichBanKetTau.doiTruong(id, "pInfo", new int[]{8, 9, 10}, new String[]{"106", "" + (20 * 16 + 8), "" + (12 * 16 + 8)});
        KichBanKetTau.doiTruong(id, "potion", new int[]{20}, new String[]{"2"});
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            if (c.mapId != 106) { ket("tau.o_ben", false, "map " + c.mapId); return; }
            int m = c.moc();
            c.gui(82);
            String t = c.doiChu(m, 3000, "Đường sang Johto", "New Bark", "cửa ải");
            c.bom(6500);
            ket("tau.chua_mo_cong_tu_choi", t != null && c.mapId == 106 && dem(c, m, 82) == 0, "bao " + t + " | map " + c.mapId + " | " + c.chuTu(m));
            HoiQuy.thoat(c);
        }
        // nhan vat luu tren thuyen 107 (ban cu ket) -> vao game: ve lang, khong nap lai lien tuc
        HoiQuy.chuanBi("dat " + u + " luu tren thuyen 107 (tai hien nhan vat da ket truoc ban sua)");
        KichBanKetTau.doiTruong(id, "pInfo", new int[]{8, 9, 10}, new String[]{"107", "" + (20 * 16 + 8), "" + (12 * 16 + 8)});
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            int m = c.moc();
            c.bom(10000);
            ket("tau.cuu_107", KichBanKetTau.laLang(c.mapId) && dem(c, m, 12) == 0, "map " + c.mapId + ", nap lai trong 10 giay " + dem(c, m, 12));
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== shop xu
    static void shopXu() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("sx", 0, 0);
        try {
            boQuaHuongDan(b);
            if (!KichBanDS2.vaoNha(b, 103)) { ket("shop_xu.vao_nha", false, "map " + b.mapId); return; }
            b.lamMoi();
            long xu0 = b.xu;
            int t0 = b.soThuoc(1);
            int m = b.moc();
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 5);
            b.bom(1500);
            b.lamMoi();
            long chi = xu0 - b.xu;
            int them = b.soThuoc(1) - t0;
            String gia = BotChucNang.sql1("SELECT price FROM data_potion WHERE id=1");
            ket("shop_xu.mua_5_thuoc", them == 5 && chi > 0, "thuoc +" + them + ", xu -" + chi + " (gia CSDL " + gia + ") | " + b.chuTu(m));
            ket("shop_xu.dung_gia", gia == null || chi == 5L * Long.parseLong(gia.trim()), "xu -" + chi + " = 5 x " + gia);
            // het xu: khong tru oan
            long xu1 = b.xu;
            int m2 = b.moc();
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 30000);
            b.bom(1500);
            b.lamMoi();
            ket("shop_xu.khong_du_xu_khong_tru", b.xu == xu1 || b.soThuoc(1) > t0 + 5, "xu " + xu1 + " -> " + b.xu + " | " + b.chuTu(m2));
            KichBanDS2.raNgoai(b);
        } finally {
            HoiQuy.thoat(b);
        }
    }

    // ================================================================== shop dac biet
    static void shopDacBiet() throws Exception {
        KichBanHoiQuy2.shopDacBiet();
    }

    static void giftcode() throws Exception {
        KichBanHoiQuy2.giftcode();
    }

    // ================================================================== luu khi dang nhap lai
    static void luuDangNhapLai() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("lu", 0, 0);
        String u = b.user;
        boQuaHuongDan(b);
        KichBanGymLang.raPallet(b);
        b.lamMoi();
        Thread.sleep(500);
        String truoc = "lv" + b.lv + " xu" + b.xu + " lk" + b.lk + " luong" + b.luong + " thuoc" + b.soThuoc(1) + " do" + b.doDung.size() + " map" + b.mapId;
        HoiQuy.thoat(b);
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            c.lamMoi();
            String sau = "lv" + c.lv + " xu" + c.xu + " lk" + c.lk + " luong" + c.luong + " thuoc" + c.soThuoc(1) + " do" + c.doDung.size() + " map" + c.mapId;
            ket("luu.dang_nhap_lai", truoc.equals(sau), truoc + " => " + sau);
            String db = BotChucNang.sql1("SELECT CONCAT(gold,'/',luong,'/',luonglock) FROM tob_char WHERE id=" + c.charDb);
            ket("luu.csdl_khop", db != null && db.equals(c.xu + "/" + c.luong + "/" + c.lk), "CSDL " + db + " client " + c.xu + "/" + c.luong + "/" + c.lk);
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== hai phien
    static void haiPhien() throws Exception {
        BotChucNang.Bot a = HoiQuy.nguoiChoiMoi("hp", 0, 0);
        String u = a.user;
        boQuaHuongDan(a);
        a.lamMoi();
        long xu0 = a.xu;
        BotChucNang.Bot c = HoiQuy.vaoLai(u);
        try {
            a.bom(2000);
            ket("hai_phien.phien_cu_bi_day", a.dut || !a.conSong(), "phien cu con song: " + !a.dut);
            c.lamMoi();
            ket("hai_phien.khong_nhan_doi", c.xu == xu0, "xu " + xu0 + " -> " + c.xu);
            // phien cu gui mua thuoc sau khi bi day: khong tru / khong cong
            try { a.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 1); } catch (IOException ignored) { }
            c.bom(1500);
            c.lamMoi();
            ket("hai_phien.phien_cu_khong_tac_dung", c.xu == xu0, "xu " + c.xu);
        } finally {
            a.close();
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== cot truyen
    static void cotTruyen() throws Exception {
        long phut = Long.getLong("phutCotTruyen", 12);
        BotChucNang.Bot b0 = HoiQuy.nguoiChoiMoi("ct", 0, 0);
        String u = b0.user;
        b0.close();
        BotXuyenSuot.Dong d = new BotXuyenSuot.Dong(0, u, System.currentTimeMillis() + phut * 60000L) {
            @Override
            void vao() throws Exception {
                b = HoiQuy.vaoLai(user);
                HoiQuy.napTheGioi(b);
                KichBanHuongDan.roiSan(b);
                b.lamMoi();
                capTruoc = b.lv;
            }
        };
        int vp0 = BatBien.VI_PHAM.size();
        d.run();
        ket("cot_truyen.tien_trien", d.capCao > d.capDau || d.soNvChinh > 0, "cap " + d.capDau + " -> " + d.capCao + ", nv chinh " + d.soNvChinh + ", phu " + d.soNvPhu
                + ", gym " + d.soGym + ", ha " + d.soHaQuai + ", doi map " + d.soDoiMap + ", chet " + d.soChet);
        ket("cot_truyen.khong_ket", d.ket.isEmpty(), String.join(" || ", d.ket));
        ket("cot_truyen.khong_loi", d.loi.isEmpty(), String.join(" || ", d.loi));
        String bs = basic(HoiQuy.CHAR_DB.getOrDefault(u, -1));
        ket("cot_truyen.vi_pham", BatBien.VI_PHAM.size() == vp0, "basic " + bs);
    }
}
