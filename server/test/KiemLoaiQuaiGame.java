import com.htbb.pokemon.GoiLoai;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LoaiDuLieu;
import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import real.Char;
import real.LoaiQuaiGame;
import real.Monster;
import real.MonsterTemplate;

/**
 * NOI loai rieng tung con quai vao game (real.LoaiQuaiGame): hoi sinh doi loai theo vung + dai cap (mau giu nguyen),
 * goi -125 sau lenh 4 chi cho client >= 908, client cu thay loai cua mau (fallback), Pokedex ghi loai nguoi do thay,
 * Monster.update bao client them lai quai khi loai rieng doi. Dung bang loai that tu khoi them_loai_day_du.sql cua sql/htbb.sql.
 */
public final class KiemLoaiQuaiGame {

    static final List<Message> GOI = new ArrayList<Message>();

    static Char nguoi(final int ban) {
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        final Char p = new Char(null);
        final Session s = new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        };
        s.banClient = ban;
        p.setSession(s);
        p.charClass = 1;
        p.isBot = -1;
        return p;
    }

    public static void chay() {
        Kiem.nhom("KiemLoaiQuaiGame: loai rieng tung con quai (hoi sinh, -125, fallback client cu)");
        try {
            chayHet();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("KiemLoaiQuaiGame chay het khong loi: " + e, false);
        }
    }

    static void chayHet() throws Exception {
        final String sql = DayDu.phanNangCap(KiemLoaiDayDu.SQL);
        final LoaiDuLieu d = KiemLoaiDayDu.dung(KiemLoaiDayDu.docSql(sql));
        final LoaiDuLieu cu = LoaiDuLieu.hien();
        LoaiDuLieu.dat(d);
        final MonsterTemplate t = new MonsterTemplate();
        t.id = 251;
        t.dex = 19; // Rattata
        t.maxhp = 500;
        t.level = 20;
        final MonsterTemplate tCu = real.Map.monsterTemplates.put(Integer.valueOf(t.id), t);
        try {
            final real.Map map = new real.Map();
            map.initKhoQuaiVaNguoi();
            map.mapId = 1; // Pallet - dong co (Kanto)
            final Monster m = new Monster((byte) 1);
            m.id = (short) 77;
            m.idTemplate = t.id;
            m.level = 20;
            m.map = map;
            m.inCountry = 0;
            m.hePokemon = (byte) HePokemon.THUONG;
            int doi = 0, sai = 0, coRieng = 0;
            for (int i = 0; i < 3000; i++) {
                final boolean x = LoaiQuaiGame.khiHoiSinh(m);
                if (x) {
                    doi++;
                }
                if (m.dexRieng > 0) {
                    coRieng++;
                    final LoaiDuLieu.Loai l = d.lay(m.dexRieng);
                    if (l == null || !l.daMo || l.bac >= 6 || m.dexRieng == 19) {
                        sai++;
                    }
                }
            }
            Kiem.dung("hoi sinh: co Pokemon hoang da (~5% + T3 1%: " + coRieng + "/3000)", coRieng > 60 && coRieng < 400);
            Kiem.bang("hoi sinh: loai rieng luon da mo, khong T6, khac loai mau", 0, sai);
            Kiem.dung("hoi sinh: bao doi loai khi loai rieng doi", doi > 0);
            Kiem.bang("hoi sinh: mau quai giu nguyen (cap, HP, TTK theo mau)", 251, m.idTemplate);

            // 0.9.8: toi da 2 Pokemon hoang da cung ban do / khu - hai con khac dang hoang da thi con nay hoi sinh thuong
            final Monster h1 = new Monster((byte) 1), h2 = new Monster((byte) 1);
            for (final Monster h : new Monster[]{h1, h2}) {
                h.map = map;
                h.inCountry = 0;
                h.dexRieng = 16;
                h.bacRieng = 1;
                LoaiQuaiGame.HOANG_DA.put(h, Long.valueOf(System.currentTimeMillis()));
            }
            int vuot = 0;
            for (int i = 0; i < 1000; i++) {
                LoaiQuaiGame.khiHoiSinh(m);
                vuot += m.dexRieng > 0 ? 1 : 0;
            }
            Kiem.bang("toi da 2 Pokemon hoang da / ban do: con thu ba khong hien", 0, vuot);
            Kiem.dung("laHoangDa: chi quai co loai rieng", LoaiQuaiGame.laHoangDa(h1) && !LoaiQuaiGame.laHoangDa(m));
            h1.isDead = true;
            int lai = 0;
            for (int i = 0; i < 3000; i++) {
                LoaiQuaiGame.khiHoiSinh(m);
                lai += m.dexRieng > 0 ? 1 : 0;
            }
            Kiem.dung("mot con hoang da chet -> lai co cho (" + lai + "/3000)", lai > 0);
            LoaiQuaiGame.HOANG_DA.clear();
            final String bg = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/BatPokemonGame.java")),
                    java.nio.charset.StandardCharsets.UTF_8);
            Kiem.dung("nem bong: quai thuong (khong hoang da) bi tu choi ro rang", bg.contains("if (!LoaiQuaiGame.laHoangDa(m)) {")
                    && bg.contains("đã thuần hoá, không thể bắt"));

            // 0.9.8: bong TANG co tran chung 10 Poke Ball quy doi / ngay (Great 2,5, Ultra 6)
            final com.htbb.pokemon.BatPokemon.Bong[] bb = com.htbb.pokemon.BatPokemon.bongMacDinh().toArray(new com.htbb.pokemon.BatPokemon.Bong[0]);
            Kiem.bang("quy doi Poke / Great / Ultra (1/10)", "10/25/60", com.htbb.pokemon.BatPokemon.quyDoiPhanMuoi(bb[0], bb[0]) + "/"
                    + com.htbb.pokemon.BatPokemon.quyDoiPhanMuoi(bb[1], bb[0]) + "/" + com.htbb.pokemon.BatPokemon.quyDoiPhanMuoi(bb[2], bb[0]));
            Kiem.bang("tang 5 Poke dau ngay: du 5", 5, com.htbb.pokemon.BatPokemon.duocTang(5, 10, 0));
            Kiem.bang("da tang 5 Poke + 1 Ultra (110): het tran, khong them", 0, com.htbb.pokemon.BatPokemon.duocTang(3, 10, 110));
            Kiem.bang("da tang 50: Ultra x2 chi duoc 0 (con 50 < 60)", 0, com.htbb.pokemon.BatPokemon.duocTang(2, 60, 50));
            Kiem.bang("da tang 40: Great x3 duoc 2 (con 60)", 2, com.htbb.pokemon.BatPokemon.duocTang(3, 25, 40));
            final com.htbb.pokemon.HopPc hp = new com.htbb.pokemon.HopPc();
            hp.bongTangNgay = 35;
            Kiem.bang("Hop PC luu bo dem bong tang", 35, com.htbb.pokemon.HopPc.giaiMa(hp.maHoa()).bongTangNgay);
            Kiem.dung("chu thuong: bong:pokex5 -> '5 Poké Ball'", com.htbb.pokemon.ChuThuongNhiemVu.dong(0, 0, 0, 0, "32000", 0, "32000", "32000",
                    "bong:pokex5", null, null).contains("5 Poké Ball"));

            // 0.9.8 trang chieu: ho so luu 3 trang + so trang mo them
            final com.htbb.pokemon.HoSoLauDai hs = new com.htbb.pokemon.HoSoLauDai();
            hs.oNhanh1 = 0x123456789AL;
            hs.oNhanh3 = -5L;
            hs.trangChieu = 2;
            hs.trangChieuThem = 1;
            final com.htbb.pokemon.HoSoLauDai hs2 = com.htbb.pokemon.HoSoLauDai.giaiMa(hs.maHoa());
            Kiem.bang("ho so: trang chieu luu / doc", "78187493530/0/-5/2/1", hs2.oNhanh1 + "/" + hs2.oNhanh2 + "/" + hs2.oNhanh3 + "/"
                    + hs2.trangChieu + "/" + hs2.trangChieuThem);
            final String ng = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/NgoaiHinhGame.java")),
                    java.nio.charset.StandardCharsets.UTF_8);
            Kiem.dung("trang chieu: -103 loai 1 (dong bo) / 2 (mua xu), trang 3 mot lan, chi client 908",
                    ng.contains("if (loai == 1) {") && ng.contains("if (loai == 2) {") && ng.contains("if (h.trangChieuThem > 0) {")
                            && ng.contains("if (p == null || !BoNgocGame.coClient(p)) {"));

            // ep mot loai rieng de kiem goi
            m.dexRieng = 16;
            m.bacRieng = 1;
            m.daCoLoaiRieng = true;
            final Char moi = nguoi(908), cuCl = nguoi(907), unity = nguoi(-1);
            GOI.clear();
            LoaiQuaiGame.sauThemQuai(moi, m);
            Kiem.bang("-125: client 908 nhan 1 goi", 1, GOI.size());
            if (!GOI.isEmpty()) {
                final byte[] b = GOI.get(0).toByteArray();
                final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                Kiem.bang("-125: lenh", (byte) GoiLoai.POKE_QUAI_CON, b[0]);
                Kiem.bang("-125: noi dung n=1, id 77, dex 16, shiny 0, bac 1", "1,77,16,0,1", in.readUnsignedByte() + ","
                        + in.readShort() + "," + in.readShort() + "," + in.readUnsignedByte() + "," + in.readUnsignedByte());
            }
            GOI.clear();
            LoaiQuaiGame.sauThemQuai(cuCl, m);
            LoaiQuaiGame.sauThemQuai(unity, m);
            Kiem.bang("-125: client 907 / Unity khong nhan (fallback loai mau)", 0, GOI.size());
            Kiem.bang("Pokedex: client 908 ghi loai rieng", 16, LoaiQuaiGame.dexChoNguoi(moi, m));
            Kiem.bang("Pokedex: client 907 ghi loai mau", 19, LoaiQuaiGame.dexChoNguoi(cuCl, m));
            Kiem.bang("loai that cua quai", 16, LoaiQuaiGame.dex(m));
            m.dexRieng = 0;
            GOI.clear();
            LoaiQuaiGame.sauThemQuai(moi, m);
            Kiem.bang("-125: ve lai loai mau van gui dex 0 (client xoa ghi de)", 1, GOI.size());
            final Monster sach = new Monster((byte) 1);
            sach.idTemplate = t.id;
            GOI.clear();
            LoaiQuaiGame.sauThemQuai(moi, sach);
            Kiem.bang("-125: quai chua tung doi loai -> khong gui (khong ton bang thong)", 0, GOI.size());
            // boss / map khac Map.class: khong doi
            m.isBoss = true;
            LoaiQuaiGame.khiHoiSinh(m);
            Kiem.bang("boss khong doi loai", 0, m.dexRieng);
            m.isBoss = false;
        } finally {
            if (tCu != null) {
                real.Map.monsterTemplates.put(Integer.valueOf(tCu.id), tCu);
            } else {
                real.Map.monsterTemplates.remove(Integer.valueOf(251));
            }
            LoaiDuLieu.dat(cu);
        }
        final String nguon = new String(Files.readAllBytes(Paths.get("src/real/Region.java")), StandardCharsets.UTF_8);
        Kiem.dung("Region: -125 gui NGAY SAU lenh 4 them quai", nguon.matches(
                "(?s).*writeActorPos\\(new Message\\(4\\), \\(Actor\\)mt\\)\\);\\s*LoaiQuaiGame\\.sauThemQuai\\(p, mt\\);.*"));
        // 0.9.8: ban do KHONG chia khu (vong lap MapVongLap) cung gui -125 sau lenh 4 - truoc chi Region gui nen dau hoang da
        // khong hien / khong bat duoc o cac ban do nay
        final String vl = new String(Files.readAllBytes(Paths.get("src/real/MapVongLap.java")), StandardCharsets.UTF_8);
        int them = 0, sau = 0;
        for (int i = vl.indexOf("p.writeActorPos(new Message(4), mt));"); i >= 0; i = vl.indexOf("p.writeActorPos(new Message(4), mt));", i + 1)) {
            them++;
            final int j = vl.indexOf('\n', i);
            if (vl.startsWith("LoaiQuaiGame.sauThemQuai(p, mt);", vl.indexOf("L", j))) {
                sau++;
            }
        }
        Kiem.dung("MapVongLap: moi lenh 4 them quai co -125 ngay sau (" + sau + "/" + them + ")", them > 0 && sau == them);
        final String xl = new String(Files.readAllBytes(Paths.get("src/real/MapXuLyLenh.java")), StandardCharsets.UTF_8);
        Kiem.dung("MapXuLyLenh: lenh -126 -> LoaiMayChu", xl.contains("case -126:") && xl.contains("LoaiMayChu.xuLy(player, message)"));
        Kiem.dung("BoDemGoiTin: -126 la lenh tai nguyen", io.BoDemGoiTin.laLenhTaiNguyen(-126));
        // 0.9.8 + loai-bo-sung: shiny CHI tung tren con hoang da (co dau, da qua gioi han khu) - quai thuong khong shiny
        final String lq = new String(Files.readAllBytes(Paths.get("src/real/LoaiQuaiGame.java")), StandardCharsets.UTF_8);
        final int iGioiHan = lq.indexOf("kq = ChonLoaiHoangDa.GIU;"), iShiny = lq.indexOf("m.shinyRieng = kq.dex > 0 && tungShiny(d, kq.dex);");
        Kiem.dung("shiny chi tung sau gioi han khu va chi khi la hoang da (kq.dex > 0)", iGioiHan > 0 && iShiny > iGioiHan);
        final String bp = new String(Files.readAllBytes(Paths.get("src/real/BatPokemonGame.java")), StandardCharsets.UTF_8);
        Kiem.dung("bat: tu choi quai khong hoang da TRUOC khi xet shiny", bp.indexOf("tuChoi(p, idQuai, thuTu, KHONG_HOANG_DA)") > 0
                && bp.indexOf("tuChoi(p, idQuai, thuTu, KHONG_HOANG_DA)") < bp.indexOf("LoaiQuaiGame.shinyChoNguoi(p, m)"));
    }
}
