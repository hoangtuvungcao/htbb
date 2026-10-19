import java.io.IOException;
import java.util.*;

/**
 * Kich ban "dung vat pham" (0.9.2, chu bao "dung vat pham khong co tac dung"): mua (Cua hang trong So tay, shop luong,
 * shop NPC) hoac nhan (GM ait / spt khi khong ban) MOT vat pham moi loai roi bam dung. Moi lan bam phai co phan hoi
 * (thong bao nhanh -104 voi -Dban=902, hop thoai 37 voi client cu) va dung tac dung / so luong.
 *
 *   java -cp build/kiem_tai -Dban=902 -Ddb=htbb_db -Ddbport=3601 BotChucNang 127.0.0.1 19241 vp dung_vat_pham
 *
 * Dong cuoi "IM LANG n/m" = so lan bam khong co phan hoi nao (truoc ban sua: phan lon cac loai).
 */
class KichBanDungVatPham {

    static void dangKy() {
        BotChucNang.dk("dung_vat_pham", true, KichBanDungVatPham::chay);
    }

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static int soBam, soImLang;

    /** Phan hoi nhin thay duoc sau moc (37 hop thoai, -104 thong bao nhanh, -30 menu, 27 chat minh). */
    static String phanHoi(BotChucNang.Bot b, int moc, long ms) {
        b.cho(x -> {
            for (int i = moc; i < x.su.size(); i++) {
                int c = x.su.get(i).cmd;
                if (c == 37 || c == -104 || c == -30 || c == 27) return true;
            }
            return false;
        }, ms);
        StringBuilder sb = new StringBuilder();
        for (int i = moc; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if ((e.cmd == 37 || e.cmd == -104 || e.cmd == -30 || e.cmd == 27) && e.chu != null) sb.append(e.chu.replace('\n', ' ')).append(" | ");
        }
        return sb.toString();
    }

    static String bamDung(BotChucNang.Bot b, int id) throws IOException {
        int m = b.moc();
        b.gui(29, (short) id);
        String s = phanHoi(b, m, 2500);
        b.bom(300);
        soBam++;
        if (s.isEmpty()) soImLang++;
        return s;
    }

    static String bamThuoc(BotChucNang.Bot b, int loai) throws IOException {
        int m = b.moc();
        b.gui(22, (byte) loai);
        String s = phanHoi(b, m, 2500);
        b.bom(300);
        soBam++;
        if (s.isEmpty()) soImLang++;
        return s;
    }

    static int dem(BotChucNang.Bot b, int tpl) {
        int n = 0;
        for (int[] d : b.doDung) if (d[1] == tpl) n++;
        return n;
    }

    static int idMon(BotChucNang.Bot b, int tpl) {
        int[] d = b.timDo(x -> x[1] == tpl);
        return d == null ? KichBanDS3.KHONG : d[0];
    }

    /** So tay -> Cua hang -> nhom co chu {@code nhom} -> dong co chu {@code dong}. Tra ve phan hoi mua. */
    static String muaCuaHang(BotChucNang.Bot b, String nhom, String dong) throws IOException {
        int m = KichBanDS4.chonSoTay(b, 9);
        if (b.doiMenu(m, -527, 20, 3000) <= 0) return "(khong mo duoc Cua hang)";
        int i = b.timMuc(nhom);
        if (i < 0) return "(khong co nhom " + nhom + ": " + b.menuChu + ")";
        int m2 = b.moc();
        b.menu(-527, 20, i);
        if (b.doiMenu(m2, -527, 21, 3000) <= 0) return "(khong mo duoc nhom " + nhom + ")";
        int j = b.timMuc(dong);
        if (j < 0) return "(khong co dong " + dong + ")";
        int m3 = b.moc();
        b.menu(-527, 21, j);
        String s = phanHoi(b, m3, 3000);
        b.bom(600);
        b.lamMoi();
        return s;
    }

    static void chay() throws Exception {
        soBam = 0;
        soImLang = 0;
        try (BotChucNang.Bot b = KichBanDS3.botLang(45)) {
            b.gm("smn " + b.ten + " 0 0 500", 600); // luong khoa cho Kecleon
            b.lamMoi();

            // 1. Binh HP: mua o shop luong (85 muc 2 = 200 binh loai 21 "Sieu thuoc hoi mau dac biet"),
            //    day mau -> bao, khong tru; thieu mau -> hoi
            final int BINH = 21;
            int h0 = b.soThuoc(BINH);
            b.gui(85, (byte) 2);
            b.bom(1500);
            b.lamMoi();
            ket("dung.mua_binh_hp", b.soThuoc(BINH) > h0, "binh " + BINH + ": " + h0 + " -> " + b.soThuoc(BINH));
            String s;
            if (b.hp >= b.maxhp) {
                s = bamThuoc(b, BINH);
                b.lamMoi();
                ket("dung.binh_hp_day_mau", s.contains("đầy") && b.soThuoc(BINH) > h0 && b.soThuoc(BINH) == 200 + h0, "day mau: " + s + " con " + b.soThuoc(BINH));
            } else {
                ket("dung.binh_hp_day_mau", null, "bot chua day mau (" + b.hp + "/" + b.maxhp + ")");
            }
            // mat mau: dung yen o map quai cho quai danh (toi da 20 giay), xong ve lang
            b.den(2, 20, 20);
            for (int k = 0; k < 20 && b.hp >= b.maxhp; k++) {
                b.bom(1000);
                b.lamMoi();
            }
            int n2 = b.soThuoc(BINH), hp0 = b.hp;
            b.bom(400);
            int mT = b.moc();
            b.gui(22, (byte) BINH);
            b.bom(1200);
            b.lamMoi();
            // binh HP dung thanh cong: khong thong bao (thanh mau len) - khong tinh vao "im lang"
            if (hp0 >= b.maxhp) ket("dung.binh_hp", null, "khong mat mau duoc o map 2");
            else ket("dung.binh_hp", b.soThuoc(BINH) == n2 - 1 && b.hp > hp0, "hp " + hp0 + " -> " + b.hp + "/" + b.maxhp + ", binh " + n2 + " -> " + b.soThuoc(BINH) + " " + b.chuTu(mT));

            KichBanDS.veLang(b);
            // 2. Qua mong (he sinh thai): mua 5 Qua Oran bang xu
            s = muaCuaHang(b, "Thuốc và quả mọng", "Quả Oran");
            ket("dung.mua_qua", dem(b, 1215) >= 5, "mua: " + s + " Oran=" + dem(b, 1215));
            int nO = dem(b, 1215);
            s = bamDung(b, idMon(b, 1215));
            b.lamMoi();
            boolean thieu = b.hp < b.maxhp;
            ket("dung.qua_oran", thieu ? dem(b, 1215) == nO - 1 : (dem(b, 1215) == nO && s.contains("đầy")), "hp " + b.hp + "/" + b.maxhp + ": " + s + " Oran " + nO + " -> " + dem(b, 1215));

            // 3. Tang cuong: 5 X Toc do
            s = muaCuaHang(b, "Thuốc và quả mọng", "X Tốc độ");
            int nX = dem(b, 1208);
            s = bamDung(b, idMon(b, 1208));
            b.lamMoi();
            ket("dung.x_toc_do", nX > 0 && dem(b, 1208) == nX - 1 && s.contains("tốc đánh"), s);

            // 4. Ve EXP (khoa, Kecleon luong khoa) = vat pham khoa + tang toc
            s = muaCuaHang(b, "Lượng khoá", "Vé EXP");
            int nV = dem(b, 1251);
            ket("dung.mua_ve_exp_khoa", nV > 0, s);
            if (nV > 0) {
                s = bamDung(b, idMon(b, 1251));
                b.lamMoi();
                ket("dung.ve_exp_khoa", !s.isEmpty() && (dem(b, 1251) == nV - 1) == s.contains("Đã dùng"), s);
            }

            // 5. Treo may: Phieu uy thac (khoa)
            s = muaCuaHang(b, "Lượng khoá", "ủy thác");
            int nU = dem(b, 1257);
            ket("dung.mua_uy_thac", nU > 0, s);
            if (nU > 0) {
                s = bamDung(b, idMon(b, 1257));
                b.lamMoi();
                ket("dung.uy_thac", !s.isEmpty() && (dem(b, 1257) == nU - 1) == s.contains("Đã dùng"), s);
            }

            // 6. Bua the luc: khong ban o cua hang -> GM
            int idB = KichBanDS3.aitMon(b, 1254, 0);
            s = bamDung(b, idB);
            b.lamMoi();
            ket("dung.bua_the_luc", !s.isEmpty(), s);

            // 7. Hoi sinh: Kecleon "Thuoc hoi sinh toi da" (cap 40); con song -> bao chi dung khi guc, khong mat
            s = muaCuaHang(b, "Lượng khoá", "Thuốc hồi sinh tối đa");
            int nH = dem(b, 1207);
            ket("dung.mua_hoi_sinh", nH > 0, s);
            if (nH > 0) {
                s = bamDung(b, idMon(b, 1207));
                b.lamMoi();
                ket("dung.hoi_sinh_con_song", s.contains("gục") && dem(b, 1207) == nH, s);
            }

            // 8. Da tien hoa: doi 10 Huy hieu ngay cong dong lay Da Nuoc
            for (int k = 0; k < 10; k++) KichBanDS3.aitMon(b, 1053, 0);
            s = muaCuaHang(b, "Huy hiệu ngày cộng đồng", "Đá Nước");
            int nD = dem(b, 960);
            ket("dung.mua_da_tien_hoa", nD > 0, s);
            if (nD > 0) {
                s = bamDung(b, idMon(b, 960));
                b.lamMoi();
                ket("dung.da_tien_hoa", !s.isEmpty(), s + " (con " + dem(b, 960) + "/" + nD + ")");
            }

            // 9. Hop qua: Hop tham gia dot kich (GM) + Qua le 30/4 (mo thang tu tui)
            int idHop = KichBanDS3.aitMon(b, 1050, 0);
            s = bamDung(b, idHop);
            b.lamMoi();
            ket("dung.hop_dot_kich", s.contains("Mở") || s.contains("ô trống"), s);
            int idQua = KichBanDS3.aitMon(b, 1057, 0);
            int oran = dem(b, 1215);
            s = bamDung(b, idQua);
            b.lamMoi();
            ket("dung.qua_le", s.contains("Mở") && dem(b, 1057) == 0 && dem(b, 1215) > oran, s + " Oran " + oran + " -> " + dem(b, 1215));

            // 10. Trang bi: mac roi thao (vong co so cap)
            int idVc = KichBanDS3.aitMon(b, 126, 0);
            s = bamDung(b, idVc);
            b.lamMoi();
            boolean mac = b.timDo(d -> d[0] == idVc) == null;
            ket("dung.mac_trang_bi", mac && !s.isEmpty(), s);
            s = bamDung(b, idVc);
            b.lamMoi();
            ket("dung.thao_trang_bi", b.timDo(d -> d[0] == idVc) != null && s.contains("tháo"), s);

            // 11. Ngoai hinh: ruy bang (type 19) mac duoc; skin thu cuoi khi khong cuoi -> bao ro
            int idRb = KichBanDS3.aitMon(b, 584, 0);
            s = bamDung(b, idRb);
            b.lamMoi();
            ket("dung.ruy_bang", b.timDo(d -> d[0] == idRb) == null, s);
            int idSk = KichBanDS3.aitMon(b, 905, 0);
            s = bamDung(b, idSk);
            ket("dung.skin_chua_cuoi", s.contains("cưỡi"), s);

            // 12. Nguyen lieu / vat pham khong dung truc tiep: bao huong dan, khong mat
            int idNl = KichBanDS3.aitMon(b, 1034, 0);
            s = bamDung(b, idNl);
            b.lamMoi();
            ket("dung.nguyen_lieu", !s.isEmpty() && b.timDo(d -> d[0] == idNl) != null, s);

            // 13. Hop qua dang binh (Hop than ky, GM spt)
            b.gm("spt " + b.ten + " 79 1", 600);
            b.lamMoi();
            int n79 = b.soThuoc(79);
            s = bamThuoc(b, 79);
            b.lamMoi();
            ket("dung.hop_than_ky", !s.isEmpty() && b.soThuoc(79) == n79 - 1, s);

            // 14. Tui cu o client: id khong con
            s = bamDung(b, 31000);
            ket("dung.id_khong_con", s.contains("không còn"), s);

            ket("dung.ket_noi", b.conSong(), "");
        }
        System.out.println("IM LANG " + soImLang + "/" + soBam + " lan bam dung (khong co phan hoi)");
        ket("dung.khong_im_lang", soImLang == 0, "im lang " + soImLang + "/" + soBam);
    }
}
