import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot THU PHUC + HOP PC + CHO POKEMON (0.9.8, goi -121 cua real.BatPokemonGame / ChoPokemonGame), client 908:
 *   tp_bat  mua 10 Poke Ball o Y ta Chansey (menu "Poké Ball & Hộp PC"), ra dong Pallet; nem vao quai THUONG bi tu
 *           choi ("đã thuần hoá"); GM "hoangda" bien mot con thanh Pokemon hoang da (-125 dex &gt; 0), danh cho yeu
 *           (HP <= 35%) roi nem bong (-121 loai 1) toi khi bat duoc (toi da 3 con quai) -> goi ket qua, Hop PC (-121 loai 3)
 *           co con moi, CSDL tob_pokedex.so_bat tang, bong bi tru.
 *   tp_cho  A (con vua bat) dang ban o Cho Pokemon (khung gia may chu, -121 loai 7 / 1 -> 2), B mua (7 / 0 -> 3):
 *           con sang Hop PC cua B, B mat dung gia, A nhan gia - thue 8% - phi dang 1% khi vao lai Hop PC.
 * Chay: java -cp build -Ddb=htbb_db -Ddbport=3410 -Dban=908 KichBanThuPhuc 127.0.0.1 19410 kt [tp_bat tp_cho]
 */
public class KichBanThuPhuc {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    /** Goi -121 loai {@code kieu} sau moc (moi nhat); null = khong co. */
    static byte[] goi121(BotChucNang.Bot b, int tu, int kieu) {
        byte[] r = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -121 && e.d != null && e.d.length > 0 && e.d[0] == kieu) r = e.d;
        }
        return r;
    }

    static DataInputStream doc(byte[] d) {
        DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
        try {
            r.readByte();
        } catch (Exception e) {
            // rong
        }
        return r;
    }

    /** Danh sach con trong Hop PC (goi -121 loai 4): {id, dex}. */
    static List<int[]> hop(BotChucNang.Bot b) throws Exception {
        int m = b.moc();
        b.gui(-121, (byte) 3);
        b.cho(x -> goi121(x, m, 4) != null, 3000);
        byte[] d = goi121(b, m, 4);
        List<int[]> ds = new ArrayList<>();
        if (d == null) return null;
        DataInputStream r = doc(d);
        r.readShort();
        r.readShort();
        r.readInt();
        r.readShort();
        int n = r.readShort();
        for (int i = 0; i < n && i < 1; i++) {
            int id = r.readInt();
            int dex = r.readShort();
            ds.add(new int[]{id, dex});
        }
        return ds;
    }

    static int soCon(BotChucNang.Bot b) throws Exception {
        int m = b.moc();
        b.gui(-121, (byte) 3);
        b.cho(x -> goi121(x, m, 4) != null, 3000);
        byte[] d = goi121(b, m, 4);
        return d == null ? -1 : doc(d).readShort();
    }

    /** Mua 10 Poke Ball o Chansey (Rustboro 45, ngoai troi). */
    static String muaBong(BotChucNang.Bot b) throws Exception {
        b.den(45, 30, 30);
        int m = b.moc();
        b.gui(23, (byte) 0);
        int n = b.doiMenu(m, 0, 0, 3000);
        int i = b.timMuc("Hộp PC");
        if (n <= 0 || i < 0) return "Chansey khong co muc Poke Ball & Hop PC: " + b.chuTu(m);
        m = b.moc();
        b.menu(0, 0, i);
        n = b.doiMenu(m, -581, 0, 3000);
        if (n <= 0) return "khong mo menu bong: " + b.chuTu(m);
        m = b.moc();
        b.menu(-581, 0, 0);
        b.bom(1200);
        return b.chuTu(m).contains("Mua") ? null : "mua bong khong co phan hoi: " + b.chuTu(m);
    }

    /** Danh quai cho yeu roi nem; true = bat duoc. */
    static boolean batMot(BotChucNang.Bot b, int id, StringBuilder ghi) throws Exception {
        int[] q = b.quai.get(id);
        if (q == null) return false;
        b.den(b.mapId, q[0] / 16, q[1] / 16);
        for (int lan = 0; lan < 40; lan++) {   // bot cap thap ~5% HP / don: toi 40 don moi xuong 35%
            b.gui(7, (short) id);
            b.bom(200);
            q = b.quai.get(id);
            if (q == null || q[2] == 0) return false;
            if (q[2] > 0 && q[4] > 0 && q[2] * 100 <= q[4] * 35) break;
            b.gui(9, (short) id, (byte) 0);
            b.bom(500);
        }
        q = b.quai.get(id);
        if (q == null || q[2] <= 0) return false;
        for (int nem = 0; nem < 4; nem++) {
            int m = b.moc();
            b.gui(-121, (byte) 1, (short) id, (byte) 1);
            b.cho(x -> goi121(x, m, 1) != null || x.doiChu(m, 0, "") != null, 2500);
            byte[] d = goi121(b, m, 1);
            if (d == null) {
                ghi.append("[nem ").append(id).append(": ").append(b.chuTu(m)).append("] ");
                return false;
            }
            DataInputStream r = doc(d);
            r.readShort();
            r.readShort();
            r.readByte();
            int kq = r.readByte();
            r.readByte();
            int dex = r.readShort();
            r.readByte();
            int tl = r.readShort();
            String chu = r.readUTF();
            ghi.append("[").append(dex).append(" ").append(tl).append("‰ ").append(chu).append("] ");
            if (kq == 1) return true;
            if (chu.contains("bỏ chạy")) return false;
            b.bom(1600);
        }
        return false;
    }

    /** Bien quai id thanh Pokemon hoang da (GM "hoangda", may thu) - cho -125 ve. */
    static boolean hoangDa(BotChucNang.Bot b, int id) throws Exception {
        b.gm("hoangda " + id + " 16 1", 900);   // Pidgey, bac Pho bien
        b.cho(x -> x.hoangDa.containsKey(id), 3000);
        return b.hoangDa.containsKey(id);
    }

    static void bat() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(3)) {
            String l = muaBong(b);
            ket("tp.mua_bong", l == null, l == null ? "mua 10 Poke Ball" : l);
            // bat la ngau nhien (Pidgey thoat 4 lan roi bo chay = het 10 bong co the xay ra ~1/5 lan chay): mua du du
            // phong de ket qua khong phu thuoc van may - luat bat/thoat khong doi
            for (int k = 0; k < 2 && l == null; k++) l = muaBong(b);
            int con0 = soCon(b);
            ket("tp.hop_pc_mo", con0 >= 0, "Hop PC " + con0 + " con");
            String dex0 = BotChucNang.sql1("SELECT COALESCE(SUM(so_bat),0) FROM tob_pokedex WHERE id_char=" + b.charDb);
            if (!b.den(1, 30, 30)) { ket("tp.bat", false, "khong vao duoc dong Pallet"); return; }
            List<Integer> ds = KichBanDS.timQuai(b, 0, 200);
            // 0.9.8: quai thuong KHONG bat duoc - nem vao con khong phai hoang da phai bi tu choi ro rang
            Integer thuong = null;
            for (int id : ds) if (!b.hoangDa.containsKey(id)) { thuong = id; break; }
            if (thuong != null) {
                int[] qt = b.quai.get(thuong);
                if (qt != null) b.den(b.mapId, qt[0] / 16, qt[1] / 16);   // lai gan (khong thi may chu bao "qua xa" truoc)
                int m = b.moc();
                b.gui(-121, (byte) 1, (short) (int) thuong, (byte) 1);
                String t = b.doiChu(m, 2500, "thuần hoá");
                ket("tp.quai_thuong_tu_choi", t != null, t == null ? b.chuTu(m) : t);
            }
            StringBuilder ghi = new StringBuilder();
            boolean duoc = false;
            for (int i = 0; i < ds.size() && i < 10 && !duoc; i++) {
                int id = ds.get(i);
                if (!b.hoangDa.containsKey(id) && !hoangDa(b, id)) { ghi.append("[khong bien duoc ").append(id).append(" thanh hoang da] "); continue; }
                duoc = batMot(b, id, ghi);
            }
            ket("tp.bat", duoc, ghi.toString());
            int con1 = soCon(b);
            ket("tp.hop_pc_them", duoc && con1 == con0 + 1, "Hop PC " + con0 + " -> " + con1);
            b.close();
            Thread.sleep(3000);
            String dex1 = BotChucNang.sql1("SELECT COALESCE(SUM(so_bat),0) FROM tob_pokedex WHERE id_char=" + b.charDb);
            ket("tp.pokedex_da_bat", dex1 != null && dex0 != null && Long.parseLong(dex1.trim()) > Long.parseLong(dex0.trim()),
                    "tob_pokedex.so_bat " + dex0 + " -> " + dex1);
        }
    }

    static void cho() throws Exception {
        try (BotChucNang.Bot a = KichBanDS3.botLang(25); BotChucNang.Bot c = KichBanDS4.botCungQuoc(25, a)) {
            String lb = muaBong(a);
            if (lb != null) { ket("tp.cho", false, "A khong mua duoc bong: " + lb); return; }
            // bat ngau nhien nhu tp.bat (cong kiem 31/10/2026: "Bạn không còn Poké Ball" sau 10 bong): mua 30 bong, thu toi
            // 10 con - chi de buoc chuan bi (co mot con trong Hop PC) khong phu thuoc van may; luat cho khong doi
            for (int k = 0; k < 2 && lb == null; k++) lb = muaBong(a);
            a.den(1, 30, 30);
            a.gm("slv " + a.ten + " 3 0", 800); // quai Pallet cap 1-5: can cap thap de khong ha mot don
            StringBuilder ghi = new StringBuilder();
            boolean duoc = false;
            List<Integer> ds = KichBanDS.timQuai(a, 0, 200);
            for (int i = 0; i < ds.size() && i < 10 && !duoc; i++) {
                if (!a.hoangDa.containsKey(ds.get(i)) && !hoangDa(a, ds.get(i))) continue;
                duoc = batMot(a, ds.get(i), ghi);
            }
            a.gm("slv " + a.ten + " 25 0", 800);
            if (!duoc) { ket("tp.cho", false, "A khong bat duoc con nao: " + ghi); return; }
            List<int[]> h = hop(a);
            if (h == null || h.isEmpty()) { ket("tp.cho", false, "Hop PC A rong"); return; }
            int idCon = h.get(0)[0];
            int m = a.moc();
            a.gui(-121, (byte) 7, (byte) 1, idCon);
            a.cho(x -> goi121(x, m, 7) != null, 3000);
            byte[] k = goi121(a, m, 7);
            int giaMin = -1;
            if (k != null && k.length > 1 && k[1] == 1) {
                DataInputStream r = doc(k);
                r.readByte();
                r.readInt();
                r.readUTF();
                r.readShort();
                r.readByte();
                r.readByte();
                r.readByte();
                giaMin = r.readInt();
            }
            ket("tp.cho.khung_gia", giaMin > 0, "khung gia min=" + giaMin + " " + a.chuTu(m));
            if (giaMin <= 0) return;
            a.lamMoi();
            long xuA0 = a.xu;
            int m2 = a.moc();
            a.gui(-121, (byte) 7, (byte) 2, idCon, giaMin);
            a.bom(1500);
            String id = BotChucNang.sql1("SELECT id FROM tob_cho_pokemon WHERE id_ban=" + a.charDb + " ORDER BY id DESC LIMIT 1");
            ket("tp.cho.dang", id != null && !id.trim().isEmpty(), "tin dang " + id + " " + a.chuTu(m2));
            if (id == null || id.trim().isEmpty()) return;
            c.lamMoi();
            long xuC0 = c.xu;
            int m3 = c.moc();
            c.gui(-121, (byte) 7, (byte) 3, Long.parseLong(id.trim()));
            c.bom(2000);
            c.lamMoi();
            int conC = soCon(c);
            ket("tp.cho.mua", xuC0 - c.xu == giaMin && conC >= 1, "B xu " + xuC0 + " -> " + c.xu + " (gia " + giaMin + "), Hop PC B " + conC
                    + " " + c.chuTu(m3));
            a.bom(1500);
            soCon(a); // vao Hop PC: nhan tien ban
            a.bom(1500);
            a.lamMoi();
            long nhan = a.xu - xuA0;
            // xuA0 do TRUOC khi dang: nguoi ban da tra phi dang 1% (dot) luc dang, luc ban nhan gia - thue 8%
            long mong = giaMin - giaMin * 8L / 100L - Math.max(1L, giaMin / 100L);
            ket("tp.cho.thue", nhan > 0 && nhan <= mong && nhan >= mong - 2, "A nhan " + nhan + " (gia " + giaMin + " - thue 8% - phi dang 1% = " + mong
                    + ", phi dang 1% tru luc dang)");
        }
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("tp_bat", true, KichBanThuPhuc::bat);
        BotChucNang.dk("tp_cho", true, KichBanThuPhuc::cho);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(List.of("tp_bat", "tp_cho"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
