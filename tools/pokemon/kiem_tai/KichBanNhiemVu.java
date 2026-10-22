import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Kich ban NHIEM VU CHINH / PHU qua goi -64 dung nhu client that gui (Quest.capNhat / hoiNhan / ManNhiemVu):
 *  nv_noi_chuyen   nhiem vu noi chuyen (type 3) client gui THANG "tra" (-64 type 1) khi nghe het loi, KHONG gui "nhan"
 *                  truoc -> server phai hoan thanh + sang buoc tiep (truoc day 0.9.6 bo qua im lang: chuoi chinh ket
 *                  ngay buoc 1 "Thuc giac o Pallet");
 *  nv_nhan_kep     nhan 2 lan (bam dup) / nhan id cu -> khong tru diem 2 lan, khong dat lai tien do, co cau tra loi;
 *  nv_danh_quai    nhan nhiem vu danh quai -> ds Dang lam (-64/2) ve ngay; tra khi chua xong -> bi tu choi CO LY DO;
 *  nv_huy          nut "Huy nhiem vu" cua client 0.9.7 (goi 52 type 3) -> server huy that, tra ds moi;
 *  nv_phu          nhiem vu phu noi chuyen tra thang; nhan nhiem vu phu thu hai khi dang lam -> ly do;
 *  nv_dang_nhap_lai thoat giua nhiem vu -> vao lai thay dung nhiem vu dang lam (-64/2) va tien do.
 */
class KichBanNhiemVu {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("nv_noi_chuyen", true, KichBanNhiemVu::noiChuyen);
        BotChucNang.dk("nv_nhan_kep", true, KichBanNhiemVu::nhanKep);
        BotChucNang.dk("nv_danh_quai", true, KichBanNhiemVu::danhQuai);
        BotChucNang.dk("nv_huy", true, KichBanNhiemVu::huy);
        BotChucNang.dk("nv_phu", true, KichBanNhiemVu::phu);
        BotChucNang.dk("nv_dang_nhap_lai", true, KichBanNhiemVu::dangNhapLai);
    }

    /** Mot dong nhiem vu trong goi -64: {id, main_sub, type(-1 neu khong co), ten}. */
    static final class Nv {
        int id, ms, loai = -1;
        String ten;

        public String toString() {
            return ten + "#" + id + "/" + ms + (loai >= 0 ? " t" + loai : "");
        }
    }

    /** Danh sach -64 nhom {@code nhom} (0 co the nhan, 1 xong, 2 dang lam) MOI NHAT sau moc; null neu khong co goi. */
    static List<Nv> ds(BotChucNang.Bot b, int tu, int nhom) {
        List<Nv> kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -64 || e.d == null || e.d.length < 2 || e.d[0] != nhom) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                int n = r.readByte();
                List<Nv> l = new ArrayList<>();
                for (int k = 0; k < n; k++) {
                    Nv q = new Nv();
                    q.id = r.readShort();
                    q.ms = r.readByte();
                    q.ten = r.readUTF();
                    if (nhom == 0) {
                        r.readByte();
                        r.readUTF();
                        q.loai = r.readByte();
                        r.readByte();
                    } else if (nhom == 1) {
                        r.readByte();
                        r.readUTF();
                        r.readUTF();
                        r.readUTF();
                    } else {
                        q.loai = r.readByte();
                        r.readUTF();
                        r.readByte();
                        r.readByte();
                        r.readUTF();
                        if (q.loai == 2 || q.loai == 0) {
                            int m = r.readByte();
                            r.skipBytes(6 * m);
                        } else if (q.loai == 4) {
                            r.skipBytes(4);
                        }
                    }
                    l.add(q);
                }
                kq = l;
            } catch (IOException ex) {
                // goi cat ngan: bo
            }
        }
        return kq;
    }

    static Nv tim(List<Nv> l, int ms) {
        if (l != null) for (Nv q : l) if (q.ms == ms) return q;
        return null;
    }

    static void gui(BotChucNang.Bot b, int type, int id, int ms) throws IOException {
        b.gui(-64, (byte) type, (short) id, (byte) ms);
    }

    /** Ds hien tai: dang nhap lai la cach chac nhat de lay ca 3 nhom (sendInfoQuest) - o day doc goi tu luc vao game. */
    static Nv chinhCoTheNhan(BotChucNang.Bot b) {
        return tim(ds(b, 0, 0), 0);
    }

    static String idChinh(BotChucNang.Bot b) {
        return BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + b.charDb);
    }

    // ------------------------------------------------------------------ kich ban

    static void noiChuyen() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(1)) {
            Nv q = chinhCoTheNhan(b);
            if (q == null || q.loai != 3) {
                ket("nv_noi_chuyen", null, "nhiem vu chinh dau khong phai noi chuyen: " + q);
                return;
            }
            int m = b.moc();
            gui(b, 1, q.id, 0); // client: nghe het loi thoai -> "tra" thang
            b.bom(1500);
            Nv sau = tim(ds(b, m, 0), 0);
            ket("nv_noi_chuyen.sang_buoc_sau", sau != null && sau.id == q.id + 1, "truoc " + q + " -> ds co the nhan moi: " + ds(b, m, 0) + " | " + b.chuTu(m));
            // bam dup: goi tra lan 2 voi id cu -> khong nhay them buoc
            int m2 = b.moc();
            gui(b, 1, q.id, 0);
            b.bom(1200);
            Nv sau2 = tim(ds(b, m2, 0), 0);
            ket("nv_noi_chuyen.tra_kep", sau2 == null || sau2.id == q.id + 1, "lan 2: " + ds(b, m2, 0) + " | " + b.chuTu(m2));
            // ca chuoi: 2 buoc noi chuyen dau (901, 902) di lien
            if (sau != null && sau.loai == 3) {
                int m3 = b.moc();
                gui(b, 1, sau.id, 0);
                b.bom(1500);
                Nv s3 = tim(ds(b, m3, 0), 0);
                ket("nv_noi_chuyen.buoc_2", s3 != null && s3.id == sau.id + 1, sau + " -> " + s3 + " | " + b.chuTu(m3));
            }
        }
    }

    /** Dua nhan vat toi nhiem vu chinh danh quai dau tien (tra thang cac buoc noi chuyen). */
    static Nv toiDanhQuai(BotChucNang.Bot b) throws IOException {
        Nv q = chinhCoTheNhan(b);
        Nv dang = tim(ds(b, 0, 2), 0);
        if (q == null && dang != null && dang.loai == 0) {
            gui(b, 2, dang.id, 0); // tai khoan dung lai dang lam: huy de bat dau lai tu "co the nhan"
            int m = b.moc();
            b.bom(1000);
            q = tim(ds(b, m, 0), 0);
        }
        for (int i = 0; i < 6 && q != null && q.loai == 3; i++) {
            int m = b.moc();
            gui(b, 1, q.id, 0);
            b.bom(1200);
            Nv n = tim(ds(b, m, 0), 0);
            if (n == null) return null;
            q = n;
        }
        return q;
    }

    static void nhanKep() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(5)) {
            Nv q = toiDanhQuai(b);
            if (q == null || q.loai != 0) {
                ket("nv_nhan_kep", false, "khong toi duoc nhiem vu danh quai: " + q);
                return;
            }
            int m = b.moc();
            gui(b, 0, q.id, 0);
            gui(b, 0, q.id, 0); // bam dup
            b.bom(1500);
            List<Nv> dang = ds(b, m, 2);
            String chu = b.chuTu(m);
            int soNhan = chu.split("Đã nhận nhiệm vụ", -1).length - 1;
            ket("nv_nhan_kep.mot_lan", tim(dang, 0) != null && soNhan == 1, "dang lam " + dang + ", 'Đã nhận' x" + soNhan + " | " + chu);
            // nhan id SAI (ds cu cua client): co ly do, khong doi nhiem vu dang lam
            int m2 = b.moc();
            gui(b, 0, q.id + 5, 0);
            b.bom(1000);
            String t = b.chuTu(m2);
            ket("nv_nhan_kep.id_cu_co_ly_do", t.contains("nhiệm vụ") && !t.contains("Đã nhận nhiệm vụ"), t);
        }
    }

    static void danhQuai() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(5)) {
            Nv q = toiDanhQuai(b);
            if (q == null || q.loai != 0) {
                ket("nv_danh_quai", false, "khong toi duoc nhiem vu danh quai: " + q);
                return;
            }
            int m = b.moc();
            gui(b, 0, q.id, 0);
            b.bom(1200);
            ket("nv_danh_quai.nhan", tim(ds(b, m, 2), 0) != null && tim(ds(b, m, 0), 0) == null,
                    "dang lam " + ds(b, m, 2) + " co the nhan " + ds(b, m, 0) + " | " + b.chuTu(m));
            int m2 = b.moc();
            gui(b, 1, q.id, 0); // tra khi chua xong
            b.bom(1200);
            String t = b.chuTu(m2);
            ket("nv_danh_quai.tra_som_co_ly_do", t.contains("chưa") || t.contains("Chưa"), t);
            ket("nv_danh_quai.tra_som_van_dang_lam", tim(ds(b, m2, 2), 0) != null || ds(b, m2, 2) == null, "ds " + ds(b, m2, 2));
        }
    }

    static void huy() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(5)) {
            Nv q = toiDanhQuai(b);
            if (q == null || q.loai != 0) {
                ket("nv_huy", false, "khong toi duoc nhiem vu danh quai: " + q);
                return;
            }
            gui(b, 0, q.id, 0);
            b.bom(1000);
            int m = b.moc();
            b.gui(52, (byte) 3, (byte) 0, (short) 0); // nut "Huy nhiem vu" cua ManNhiemVu 0.9.7 (GameService.receiveQuest)
            b.bom(1200);
            String t = b.chuTu(m);
            Nv lai = tim(ds(b, m, 0), 0);
            ket("nv_huy.client_097", lai != null && lai.id == q.id && t.contains("hủy"), "co the nhan lai: " + lai + " | " + t);
            gui(b, 0, q.id, 0);
            b.bom(800);
            int m2 = b.moc();
            gui(b, 2, q.id, 0); // client moi: -64 type 2
            b.bom(1200);
            Nv lai2 = tim(ds(b, m2, 0), 0);
            ket("nv_huy.client_moi", lai2 != null && lai2.id == q.id, "co the nhan lai: " + lai2 + " | " + b.chuTu(m2));
        }
    }

    static void phu() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(1)) {
            List<Nv> l = ds(b, 0, 0);
            Nv noi = null, khac = null;
            if (l != null) for (Nv q : l) {
                if (q.ms != 1) continue;
                if (q.loai == 3 && noi == null) noi = q;
                else if (q.loai != 3 && q.loai != 1 && khac == null) khac = q;
            }
            if (noi == null) {
                ket("nv_phu.noi_chuyen", null, "khong co nhiem vu phu noi chuyen: " + l);
            } else {
                int m = b.moc();
                long xu0 = b.xu;
                gui(b, 1, noi.id, 1); // client: tra thang
                b.bom(1500);
                String t = b.chuTu(m);
                ket("nv_phu.noi_chuyen", ds(b, m, 0) != null && t.indexOf("Exception") < 0, "tra " + noi + " -> ds " + ds(b, m, 0) + " | " + t);
                boolean lai = false;
                final Nv daTra = noi;
                if (ds(b, m, 0) != null) for (Nv q : ds(b, m, 0)) if (q.ms == 1 && q.id == daTra.id) lai = true;
                ket("nv_phu.khong_nhan_lai", ds(b, m, 0) != null && !lai, "sau khi tra, ds co the nhan con " + noi + ": " + lai);
                // tra lan 2 (bam dup / goi lai) -> khong lanh thuong lan nua
                long xu1 = b.xu;
                int m3 = b.moc();
                gui(b, 1, noi.id, 1);
                b.bom(1000);
                ket("nv_phu.tra_kep", !b.chuTu(m3).contains("hoàn thành"), b.chuTu(m3));
            }
            l = ds(b, 0, 0); // ds moi nhat (sau khi tra nhiem vu noi chuyen, danh sach phu mo them)
            if (l != null) for (Nv q : l) if (q.ms == 1 && q.loai != 3 && q.loai != 1 && khac == null) khac = q;
            if (khac != null) {
                int m = b.moc();
                gui(b, 0, khac.id, 1);
                b.bom(1000);
                Nv dang = tim(ds(b, m, 2), 1);
                ket("nv_phu.nhan", dang != null && dang.id == khac.id, "dang lam " + ds(b, m, 2) + " | " + b.chuTu(m));
                Nv thu2 = null;
                for (Nv q : l) if (q.ms == 1 && q.id != khac.id && q.loai != 3) { thu2 = q; break; }
                if (thu2 != null) {
                    int m2 = b.moc();
                    gui(b, 0, thu2.id, 1);
                    b.bom(1000);
                    String t2 = b.chuTu(m2);
                    ket("nv_phu.nhan_thu_hai_co_ly_do", t2.contains("nhiệm vụ phụ"), t2);
                }
            } else ket("nv_phu.nhan", null, "khong co nhiem vu phu danh quai / nhat do: " + l);
        }
    }

    static void dangNhapLai() throws Exception {
        BotChucNang.Bot b = KichBanDS3.botLang(5);
        try {
            Nv q = toiDanhQuai(b);
            if (q == null || q.loai != 0) {
                ket("nv_dang_nhap_lai", false, "khong toi duoc nhiem vu danh quai: " + q);
                return;
            }
            gui(b, 0, q.id, 0);
            b.bom(1200);
            String u = b.user;
            b.close();
            Thread.sleep(2500);
            b = new BotChucNang.Bot(u);
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0);
            if (l != null) {
                ket("nv_dang_nhap_lai", false, l);
                return;
            }
            b.bom(1000);
            Nv dang = tim(ds(b, 0, 2), 0);
            ket("nv_dang_nhap_lai.van_dang_lam", dang != null && dang.id == q.id, "ds dang lam sau khi vao lai: " + ds(b, 0, 2) + " (co the nhan " + ds(b, 0, 0) + ")");
        } finally {
            b.close();
        }
    }
}
