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
 * Monster.update bao client them lai quai khi loai rieng doi. Dung bang loai that tu khoi them_loai_day_du.sql cua sql/nang_cap.sql.
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
        final String xl = new String(Files.readAllBytes(Paths.get("src/real/MapXuLyLenh.java")), StandardCharsets.UTF_8);
        Kiem.dung("MapXuLyLenh: lenh -126 -> LoaiMayChu", xl.contains("case -126:") && xl.contains("LoaiMayChu.xuLy(player, message)"));
        Kiem.dung("BoDemGoiTin: -126 la lenh tai nguyen", io.BoDemGoiTin.laLenhTaiNguyen(-126));
    }
}
