import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Kich ban TIEN DO NHIEM VU (nhanh sua-tien-do-nv) - tai hien bao cua nguoi choi 0.9.13 "Trung Nguyễn": dang lam
 * C03.2 "Gom hàng rơi" (Chính tuyến, client hien 0/4) + P-04.0.2 "Chứng tỏ sức mạnh" (Phụ 0/20), danh ~80 con van
 * khong xong, khong biet tra o dau, vao NPC khong thay "Nhiệm vụ của tôi".
 *
 *  tdnv_so            so Nhiem vu (-64 nhom 2) ghi ro muc tieu: client &lt; 915 ten "… · Weedle ở Lavender", mo ta
 *                     "Mục tiêu: Hạ 20 Weedle ở Lavender · Trả: Machamp (Pewter)"; client &gt;= 915 duoi muc/tra; C03.2 can 5;
 *  tdnv_sai_quai      ha Torchic (khong phai muc tieu) o Pallet: tien do khong doi + hop 95 nhac cua ai MOT lan, Đồng ý =
 *                     nhan cua ai -> cua ai hien o khe doi so (main_sub 2, id 30011) va dem tiep;
 *  tdnv_weedle        ha 20 Weedle o Lavender: -64 nhom 2 gui lai moi lan tang (x/20 dung), du -> nhom 1 + "Xong";
 *                     tra o Pewter -> thuong;
 *  tdnv_goi_hang      ha Litwick o Cerulean, nhat Goi hang (-65) toi 5/5 -> xong, tra Kecleon;
 *  tdnv_nhiem_vu_cua_toi quay doi thuong (-90) co muc "Nhiệm vụ của tôi" -> menu So tay nhiem vu (-527/10).
 * Chay: BotChucNang host port kt tdnv_all (-Dban=913 va -Dban=915, -Ddb ...).
 */
class KichBanTienDoNhiemVu {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("tdnv_all", true, KichBanTienDoNhiemVu::tatCa);
    }

    static final boolean MOI = BotChucNang.BAN >= 915;

    /** Dong -64. */
    static final class Dong {
        int id, ms, loai = -1, npc;
        String ten, moTa = "", muc = "", tra = "", khoa = "";
        int co;
        List<int[]> td = new ArrayList<>(); // {mau/vat pham, da, can}

        public String toString() {
            return ten + "#" + id + "/" + ms + " t" + loai + " " + td.stream().map(a -> a[0] + ":" + a[1] + "/" + a[2]).reduce("", (x, y) -> x + y)
                    + (moTa.isEmpty() ? "" : " «" + moTa + "»") + (muc.isEmpty() ? "" : " [" + muc + " | " + tra + " | " + khoa + " | " + co + "]");
        }
    }

    static List<Dong> doc(byte[] d) throws IOException {
        DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
        int nhom = r.readByte();
        int n = r.readByte();
        List<Dong> l = new ArrayList<>();
        for (int k = 0; k < n; k++) {
            Dong q = new Dong();
            q.id = r.readShort();
            q.ms = r.readByte();
            q.ten = r.readUTF();
            if (nhom == 0) {
                q.npc = r.readByte();
                r.readUTF();
                q.loai = r.readByte();
                r.readByte();
            } else if (nhom == 1) {
                q.npc = r.readByte();
                r.readUTF();
                q.moTa = r.readUTF();
                r.readUTF();
            } else {
                q.loai = r.readByte();
                q.moTa = r.readUTF();
                q.npc = r.readByte();
                r.readByte();
                r.readUTF();
                if (q.loai == 2 || q.loai == 0) {
                    int m = r.readByte();
                    for (int j = 0; j < m; j++) q.td.add(new int[]{r.readShort(), r.readShort(), r.readShort()});
                } else if (q.loai == 4) {
                    q.td.add(new int[]{-1, r.readShort(), r.readShort()});
                }
            }
            l.add(q);
        }
        if (r.available() > 0) {
            int m = r.readByte();
            for (int k = 0; k < m && k < l.size(); k++) {
                Dong q = l.get(k);
                q.muc = r.readUTF();
                q.tra = r.readUTF();
                q.khoa = r.readUTF();
                q.co = r.readUnsignedByte();
            }
        }
        return l;
    }

    /** Ds -64 nhom moi nhat tu moc (null = khong co goi). */
    static List<Dong> ds(BotChucNang.Bot b, int tu, int nhom) {
        List<Dong> kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -64 || e.d == null || e.d.length < 2 || e.d[0] != nhom) continue;
            try {
                kq = doc(e.d);
            } catch (IOException ex) {
                kq = null;
            }
        }
        return kq;
    }

    static int soGoi(BotChucNang.Bot b, int tu, int nhom) {
        int n = 0;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -64 && e.d != null && e.d.length > 0 && e.d[0] == nhom) n++;
        }
        return n;
    }

    static Dong tim(List<Dong> l, int ms) {
        if (l != null) for (Dong q : l) if (q.ms == ms) return q;
        return null;
    }

    static int chiSo(int mainsub, int idGoc) {
        String s = BotChucNang.sql1("SELECT COUNT(*) FROM data_quest_moi WHERE mainsub=" + mainsub + " AND id<" + idGoc);
        return s == null ? -1 : Integer.parseInt(s.trim());
    }

    /** Dang nhap lai tai khoan u. */
    static BotChucNang.Bot vaoLai(String u) throws Exception {
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        String l = b.dangNhap("123456");
        if (l == null) l = b.vaoGame(0);
        if (l != null) throw new IllegalStateException(l);
        b.bom(1200);
        return b;
    }

    /** Ha toi da n quai mau tpl tren ban do hien tai (nhat do nhiem vu roi), tra ve so con da ha. */
    static int haMau(BotChucNang.Bot b, int tpl, int n, boolean nhat) throws IOException {
        int ha = 0;
        for (int vong = 0; vong < 40 && ha < n; vong++) {
            List<Integer> song = KichBanDS.timQuai(b, 0, 260);
            List<Integer> dung = new ArrayList<>();
            for (int id : song) if (b.quai.get(id)[5] == tpl) dung.add(id);
            if (dung.isEmpty()) {
                b.bom(3000);
                continue;
            }
            for (int id : dung) {
                if (ha >= n) break;
                int m = b.moc();
                int don = KichBanDS.haQuai(b, id, -1, 60);
                if (don > 0) {
                    ha++;
                    if (nhat) nhatDo(b, m);
                }
                if (b.dut) return ha;
            }
        }
        return ha;
    }

    static void nhatDo(BotChucNang.Bot b, int tu) throws IOException {
        b.bom(500);
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 17 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readShort(); r.readShort(); r.readByte(); r.readInt(); r.readByte();
                int n = r.readByte();
                for (int k = 0; k < n; k++) {
                    int cat = r.readByte(), tpl = r.readShort(), id = r.readShort();
                    r.readShort(); r.readShort();
                    if (cat == 14) b.gui(-65, (short) id);
                }
            } catch (IOException ignored) {
            }
        }
        b.bom(300);
    }

    static void tatCa() throws Exception {
        final String tt = MOI ? "915" : "913";
        BotChucNang.Bot b0 = KichBanDS3.botLang(12);
        String u = b0.user;
        int idc = b0.charDb;
        b0.close();
        Thread.sleep(2500);
        int c = chiSo(0, 1014), p = chiSo(1, 2044);
        // trang thai cua nguoi choi: da nhan C03.2 + P-04.0.2 (dang lam, 0 tien do), chua nhan cua ai nao
        BotChucNang.sql("UPDATE tob_char_quest SET isfinish='" + c + ",2,0|" + p + ",2,1', itemget='0|0', monskilled='0|0' WHERE id_char=" + idc);
        BotChucNang.sql("DELETE FROM tob_nhiem_vu_lau_dai WHERE id_char=" + idc + " AND ma LIKE 'CA-%'");
        BotChucNang.Bot b = vaoLai(u);
        try {
            List<Dong> dang = ds(b, 0, 2);
            Dong chinh = tim(dang, 0), phu = tim(dang, 1);
            ket("tdnv_so." + tt + ".co_hai_dong", chinh != null && phu != null && chinh.id == c && phu.id == p, "dang lam " + dang);
            if (chinh == null || phu == null) return;
            ket("tdnv_so." + tt + ".c032_can_5", chinh.td.size() == 1 && chinh.td.get(0)[2] == 5 && chinh.td.get(0)[0] == 10, chinh.toString());
            ket("tdnv_so." + tt + ".mo_ta_phu", phu.moTa.startsWith("Mục tiêu: Hạ 20 Weedle ở Lavender · Trả: Machamp"), phu.moTa);
            ket("tdnv_so." + tt + ".mo_ta_chinh", chinh.moTa.startsWith("Mục tiêu: Hạ Litwick ở Cerulean, nhặt 5 Gói hàng · Trả: Kecleon"), chinh.moTa);
            if (MOI) {
                ket("tdnv_so.915.ten_goc", "Chứng tỏ sức mạnh".equals(phu.ten) && "Gom hàng rơi".equals(chinh.ten), chinh.ten + " / " + phu.ten);
                ket("tdnv_so.915.duoi", "Hạ 20 Weedle ở Lavender".equals(phu.muc) && phu.tra.startsWith("Machamp"), phu.toString());
            } else {
                ket("tdnv_so.913.ten_kem_muc_tieu", phu.ten.equals("Chứng tỏ sức mạnh · Weedle ở Lavender")
                        && chinh.ten.equals("Gom hàng rơi · Gói hàng (Litwick, Cerulean)"), chinh.ten + " / " + phu.ten);
                ket("tdnv_so.913.khong_duoi", phu.muc.isEmpty(), phu.toString());
            }

            // ---- danh nham Torchic o Pallet (cua ai CA-01.1 chua nhan)
            b.den(1, 40, 50);
            int m = b.moc();
            int ha = haMau(b, 4, 2, false);
            b.bom(800);
            List<Dong> sau = ds(b, m, 2);
            Dong phuSau = tim(sau, 1);
            ket("tdnv_sai_quai." + tt + ".khong_tinh", phuSau == null || phuSau.td.get(0)[1] == 0, "ha " + ha + " Torchic, ds " + sau);
            int soHop = 0;
            BotChucNang.SuKien hop = null;
            for (int i = m; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -32 && e.so != null && e.so[1] == 95) {
                    soHop++;
                    hop = e;
                }
            }
            ket("tdnv_sai_quai." + tt + ".nhac_mot_lan", soHop == 1 && hop.chu.contains("Bạn đang hạ Torchic nhưng chưa nhận cửa ải"),
                    "so hop " + soHop + " " + (hop == null ? "" : hop.chu) + " | " + b.chuTu(m));
            if (hop != null) {
                int m2 = b.moc();
                b.gui(-32, (short) hop.so[0], (byte) 95, "", (byte) 1);
                b.bom(1500);
                Dong ca = tim(ds(b, m2, 2), 2);
                ket("tdnv_sai_quai." + tt + ".cua_ai_trong_so", ca != null && ca.id == 30011 && ca.loai == 0 && ca.td.size() == 1 && ca.td.get(0)[0] == 4
                        && ca.td.get(0)[2] == 20 && (!MOI || (ca.co & 1) == 1), "ds " + ds(b, m2, 2) + " | " + b.chuTu(m2));
                int m3 = b.moc();
                haMau(b, 4, 2, false);
                b.bom(800);
                Dong ca2 = tim(ds(b, m3, 2), 2);
                ket("tdnv_sai_quai." + tt + ".cua_ai_dem", ca2 != null && ca2.td.get(0)[1] >= 2, "ds " + ds(b, m3, 2));
                // huy tu so: khong huy duoc, co ly do
                int m4 = b.moc();
                b.gui(-64, (byte) 2, (short) 30011, (byte) 2);
                b.bom(1000);
                ket("tdnv_sai_quai." + tt + ".khong_huy", b.chuTu(m4).contains("không huỷ") && tim(ds(b, m4, 2), 2) != null, b.chuTu(m4));
            }

            // ---- 20 Weedle o Lavender
            b.den(5, 40, 40);
            int m5 = b.moc();
            int haW = haMau(b, 11, 20, false);
            b.bom(1200);
            int goi2 = soGoi(b, m5, 2);
            List<Dong> xong = ds(b, m5, 1);
            Dong phuXong = tim(xong, 1);
            ket("tdnv_weedle." + tt + ".tien_do_gui_lai", goi2 >= Math.min(haW, 19), "ha " + haW + ", goi -64/2: " + goi2);
            ket("tdnv_weedle." + tt + ".xong", haW >= 20 && phuXong != null && phuXong.id == p && tim(ds(b, m5, 2), 1) == null,
                    "ha " + haW + " xong " + xong + " | " + b.chuTu(m5).replaceAll("(?s).{0,0}", "").substring(0, Math.min(300, b.chuTu(m5).length())));
            b.den(0, 30, 30);
            long xu0 = b.xu;
            int m6 = b.moc();
            b.gui(-64, (byte) 1, (short) p, (byte) 1);
            b.bom(1500);
            b.lamMoi();
            ket("tdnv_weedle." + tt + ".tra", b.xu > xu0 && tim(ds(b, m6, 1), 1) == null, "xu " + xu0 + " -> " + b.xu + " | " + b.chuTu(m6));

            // ---- Goi hang tu Litwick o Cerulean
            b.den(4, 50, 50);
            int m7 = b.moc();
            for (int lan = 0; lan < 14; lan++) {   // roi Goi hang la ngau nhien: may cham thi can nhieu luot hon
                haMau(b, 6, 10, true);
                Dong cx = tim(ds(b, m7, 1), 0);
                if (cx != null) break;
            }
            Dong chinhXong = tim(ds(b, m7, 1), 0);
            ket("tdnv_goi_hang." + tt + ".xong_5", chinhXong != null && chinhXong.id == c, "xong " + ds(b, m7, 1) + " dang " + ds(b, m7, 2));
            b.den(0, 30, 30);
            long xu1 = b.xu;
            int m8 = b.moc();
            b.gui(-64, (byte) 1, (short) c, (byte) 0);
            b.bom(1500);
            b.lamMoi();
            ket("tdnv_goi_hang." + tt + ".tra", b.xu > xu1, "xu " + xu1 + " -> " + b.xu + " | " + b.chuTu(m8));

            // ---- "Nhiệm vụ của tôi" o quay doi thuong
            int m9 = b.moc();
            b.gui(23, (byte) -90);
            b.doiMenu(m9, -90, 0, 3000);
            int vt = b.timMuc("Nhiệm vụ của tôi");
            ket("tdnv_nhiem_vu_cua_toi." + tt + ".co_muc", vt >= 0, "menu -90: " + b.menuChu);
            if (vt >= 0) {
                int m10 = b.moc();
                b.menu(-90, 0, vt);
                ket("tdnv_nhiem_vu_cua_toi." + tt + ".mo_so", b.doiMenu(m10, -527, 10, 3000) > 0 && b.timMuc("Thử thách cửa ải") >= 0,
                        "menu " + b.menuChu);
            }
        } finally {
            b.close();
        }
    }
}
