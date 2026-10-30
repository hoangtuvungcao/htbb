import com.htbb.pokemon.KhuVucNhiemVu;
import data.InfoQuestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import real.Char;
import real.KhuVucNhiemVuGame;
import real.NewCharQuest;

/**
 * BAO LOI NGUOI CHOI 30/10/2026 (J2ME tren gia lap Android, ban 922, nhan vat cap 47 ket buoc chinh C05.2 "Bảo tàng thiên
 * thạch" tu 23/10): "vẫn ko làm đc nv nhà pewter" sau khi khoi sua_tim_nhiem_vu.sql (nha 102 / 72 / 82 cung tinh) da len.
 * <p>
 * Goc: buoc dia diem chi kiem trong CharCapNhat.doPlayerMove (goi 4). Client (client_jar / client_pc_ios
 * GameService.moveChar) KHONG gui goi 4 khi dang trong nha (Tilemap.isOfflineMap) -> vung dich la ca ngoi nha khong bao gio
 * duoc kiem voi nguoi choi that (bot kiem_tai st_tim_diem tu gui goi 4 trong nha nen qua). Sua: kiem ngay khi vao ban do
 * (MapXuLyLenh.doChangeMap qua cua, Map.move2Map - Arcanine gac cong) qua {@link KhuVucNhiemVuGame#sauKhiVaoBanDo}.
 * <p>
 * Kem: Nha tre "Trả phí hôm nay" khi chua gui pet chi bao "Chưa gửi pet nào." - nay noi ro pet dong hanh la gi / cach co.
 */
public final class KiemVaoNhaNhiemVu {

    private KiemVaoNhaNhiemVu() {
    }

    public static void chay() {
        Kiem.nhom("Vao nha xong buoc dia diem khong can di chuyen + chu Nha tre (bao loi 30/10/2026)");
        try {
            hanhVi();
            ma();
            nhaTre();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    /** Nhan vat vua vao nha 102 (ban do offline), KHONG di buoc nao: sauKhiVaoBanDo phai ghi nhan da toi. */
    static void hanhVi() {
        final HashMap<String, List<KhuVucNhiemVu.Vung>> m = new HashMap<String, List<KhuVucNhiemVu.Vung>>();
        // noi_chuyen_sau = true: toi -> trang thai du (khong goi OnQuest / CSDL trong bo kiem khong CSDL)
        m.put("NHA.1", new ArrayList<KhuVucNhiemVu.Vung>(Arrays.asList(
                new KhuVucNhiemVu.Vung("NHA.1", 100, 0, 0, 255, 255, true, "Bảo tàng Pewter"),
                new KhuVucNhiemVu.Vung("NHA.1", 102, 0, 0, 255, 255, true, "Nhà lớn Pewter"))));
        KhuVucNhiemVuGame.dat(m);
        final short id = (short) 31980;
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            final InfoQuestTemplate a = KiemSuaTuyen.mau("NHA.1", id);
            a.main_sub = 0;
            a.monsterKill = new java.util.Vector<Short>();
            a.totalMonsKilled = new java.util.Vector<Short>();
            a.itemget = new java.util.Vector<Short>();
            a.totalitemget = new java.util.Vector<Short>();
            real.Map.allMainQuest.put(Short.valueOf(id), a);
            final Char p = new Char(null);
            p.setSession(new io.Session(null, null, true));
            p.getSession().banClient = 922;
            final NewCharQuest q = new NewCharQuest(true);
            q.id_quest = id;
            q.main_sub = 0;
            q.state_quest = NewCharQuest.WORKING;
            p.mainQuest = q;
            p.subQuest = new NewCharQuest(true);
            // ngoai lang Pewter (ban sao khu 301 -> nap 0): chua toi
            final real.Map lang = new real.Map();
            lang.mapIDLoadMap = 0;
            p.map = lang;
            p.mapID = 301;
            p.x = 34 * 16 + 8;
            p.y = 12 * 16 + 8;
            KhuVucNhiemVuGame.sauKhiVaoBanDo(p);
            Kiem.bang("o lang Pewter: van dang lam", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q.state_quest));
            // qua cua vao nha 102: ban do offline, mapID = id nha, dung o cua (chua buoc nao)
            final real.Map nha = new real.Map();
            nha.isMapOffline = true;
            nha.mapIDLoadMap = -1;
            p.map = nha;
            p.mapID = 102;
            p.x = 9 * 16 + 8;
            p.y = 14 * 16 + 8;
            KhuVucNhiemVuGame.sauKhiVaoBanDo(p);
            Kiem.bang("vua vao nha 102 (khong goi di chuyen): da toi", Integer.valueOf(NewCharQuest.FINISH), Integer.valueOf(q.state_quest));
            // Arcanine dua thang vao nha 100
            q.state_quest = NewCharQuest.WORKING;
            p.mapID = 100;
            KhuVucNhiemVuGame.sauKhiVaoBanDo(p);
            Kiem.bang("Arcanine dua vao nha 100: da toi", Integer.valueOf(NewCharQuest.FINISH), Integer.valueOf(q.state_quest));
            // nha khac (103 Trung tam Pokemon): khong tinh
            q.state_quest = NewCharQuest.WORKING;
            p.mapID = 103;
            KhuVucNhiemVuGame.sauKhiVaoBanDo(p);
            Kiem.bang("nha 103: khong tinh", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q.state_quest));
            // khong co nhan vat / ban do: khong nem
            KhuVucNhiemVuGame.sauKhiVaoBanDo(null);
            p.map = null;
            KhuVucNhiemVuGame.sauKhiVaoBanDo(p);
            Kiem.dung("null / chua co ban do: khong nem", true);
        } finally {
            real.Map.allMainQuest.remove(Short.valueOf(id));
            real.Map.gemTemplate = gemCu;
            KhuVucNhiemVuGame.dat(null);
        }
    }

    /** Hai duong vao ban do deu goi kiem; client van khong gui goi 4 trong nha (ly do phai kiem luc vao). */
    static void ma() throws Exception {
        final String xl = KiemSuaTuyen.doc("src/real/MapXuLyLenh.java");
        final int dc = xl.indexOf("static void doChangeMap(Map map, Char player, Message message)");
        final int hetDc = xl.indexOf("\n    static ", dc + 10);
        final String than = dc < 0 ? "" : hetDc < 0 ? xl.substring(dc) : xl.substring(dc, hetDc);
        final int doi = than.indexOf("player.mapID = toMapID;"); // nhanh doi ban do that (khong phai nap lai cung ban do)
        final int kiem = than.indexOf("KhuVucNhiemVuGame.sauKhiVaoBanDo(player)", Math.max(doi, 0));
        final int gui = kiem < 0 ? -1 : than.lastIndexOf("MessageCreator.createMapMessage(player)", kiem);
        Kiem.dung("doChangeMap (qua cua): kiem buoc dia diem SAU khi doi + gui ban do moi", doi > 0 && gui > doi && kiem > gui);
        final String mp = KiemSuaTuyen.doc("src/real/Map.java");
        final int mv = mp.indexOf("public void move2Map(Char player, int x, int y, int mapID, int country)");
        final int hetMv = mp.indexOf("private void sendInfoMap(", mv);
        final String thanMv = mv < 0 || hetMv < 0 ? "" : mp.substring(mv, hetMv);
        final int guiMv = thanMv.indexOf("MessageCreator.createMapMessage(player)");
        final int kiemMv = thanMv.indexOf("KhuVucNhiemVuGame.sauKhiVaoBanDo(player)");
        Kiem.dung("move2Map (Arcanine / dich chuyen): kiem buoc dia diem SAU khi gui ban do moi", guiMv > 0 && kiemMv > guiMv);
        final String gs = KiemSuaTuyen.doc("../client_jar/src/game/networklogic/GameService.java");
        final int mc = gs.indexOf("public void moveChar(int x, int y)");
        Kiem.dung("client_jar: moveChar khong gui trong nha (Tilemap.isOfflineMap) - may chu phai kiem luc vao",
                mc > 0 && gs.indexOf("Tilemap.isOfflineMap", mc) > mc && gs.indexOf("Tilemap.isOfflineMap", mc) < mc + 200);
    }

    static void nhaTre() throws Exception {
        final String chu = com.htbb.pokemon.HeSinhThaiMenu.CHUA_CO_PET;
        Kiem.dung("chua co pet: noi pet dong hanh, khong phai Pokemon chien dau", chu.contains("pet đồng hành") && chu.contains("không phải Pokémon chiến đấu"));
        Kiem.dung("chua co pet: chi cach co (Poke Ball / Cho sinh thai)", chu.contains("Poké Ball") && chu.contains("Chợ sinh thái"));
        final String hm = KiemSuaTuyen.doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.sai("tra phi khi chua gui: khong con chi 'Chưa gửi pet nào.'", hm.contains("bao(p, \"Chưa gửi pet nào.\");"));
        Kiem.dung("menu: chua gui thi nhan 'Trả phí hôm nay (gửi pet trước)'", hm.contains("\"Trả phí hôm nay (gửi pet trước)\""));
        final String khoi = DayDu.phanNangCap("sua_nha_tre_pet.sql");
        Kiem.dung("co khoi sua_nha_tre_pet.sql", khoi.contains("UPDATE `data_huong_dan_tan_thu`"));
        final int set = khoi.indexOf("SET `noi_dung` = '");
        final int where = khoi.indexOf("WHERE `ma` = 'npc.19_trung'");
        final String moi = set < 0 || where < 0 ? "" : khoi.substring(set, where);
        Kiem.dung("huong dan trung: noi pet dong hanh, bo 'gửi 1-2 Pokémon'", moi.contains("pet đồng hành") && !moi.contains("gửi 1-2 Pokémon"));
        Kiem.dung("chi sua khi admin chua sua (sua_boi = '')", khoi.contains("AND `sua_boi` = ''"));
    }
}
