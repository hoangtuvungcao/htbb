package real;

import io.Message;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Bao loi 24/10/2026 "danh ma khong gay duoc 1 ti sat thuong nao" (J2ME, cap 27, o Pewter): MapDanhNhau.doAttackMonster /
 * doAttackMultiTarget bo qua MOI don danh quai trong lang Pewter / Blackthorn / Cianwood (mapIDLoadMap 0/70/80), ma Thach
 * dau Gym (sua-duong-gym) trieu hoi boss canh nguoi choi o ban do dang dung - thuong la Pewter. Kiem
 * MapDanhNhau.choDanhQuai: ngoai lang moi quai danh duoc; trong lang chi boss Gym cua chinh minh (lenh 9 va 106), khong
 * phai quai khac / boss Gym cua nguoi khac / boss da guc; doc trom khong lam lech goi tin; thi tran van khong PvP.
 * Duong danh day du (J2ME lenh 9 / 106, PC ngam -113) o bot tools/pokemon/kiem_tai/KichBanGymLang.
 */
public final class KiemDanhQuaiTrongLang {

    private KiemDanhQuaiTrongLang() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void dung(final String m, final boolean v) {
        BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
    }

    static void sai(final String m, final boolean v) {
        BAO.bang(m, Boolean.FALSE, Boolean.valueOf(v));
    }

    static Map banDo(final int loadMap, final int id) {
        final Map m = new Map();
        m.initKhoQuaiVaNguoi();
        m.mapId = id;
        m.mapIDLoadMap = loadMap;
        return m;
    }

    static Monster quai(final Map m, final int id) {
        final Monster q = new Monster((byte) 1);
        q.id = (short) id;
        q.maxhp = 49100;
        q.hp = 49100;
        q.isDead = false;
        q.map = m;
        m.monsters.get(0).put(Short.valueOf((short) id), q);
        return q;
    }

    static int idKe = 8800;

    static Char nv(final Map m) {
        final Char p = new Char(null);
        p.id = (short) idKe;
        p.charDBID = idKe++;
        p.charname = "danhlang" + p.charDBID;
        p.inCountry = 0;
        p.myCountry = 0;
        p.region = 0;
        p.map = m;
        p.mapID = m.mapId;
        return p;
    }

    static Message goi9(final int id, final int chieu) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bo);
        d.writeShort(id);
        d.writeByte(chieu);
        return new Message(9, bo.toByteArray());
    }

    static Message goi106(final int chieu, final int... ids) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bo);
        d.writeByte(chieu);
        d.writeByte(ids.length);
        for (final int id : ids) {
            d.writeShort(id);
        }
        return new Message(106, bo.toByteArray());
    }

    public static void chay() throws Exception {
        // ---- Pewter (map 0 / ban sao 301 cung nap map 0), Blackthorn 70, Cianwood 80
        for (final int load : new int[]{0, 70, 80}) {
            final Map lang = banDo(load, load == 0 ? 301 : load);
            final Char p = nv(lang);
            final Char khac = nv(lang);
            final Monster thuong = quai(lang, -31000 - load);
            final Monster boss = quai(lang, -31500 - load);
            BossLauDai.datGym(boss, 1, p, 600000L);
            dung("lang " + load + ": la lang (Map.isMapLang)", Map.isMapLang(lang));
            sai("lang " + load + ": quai thuong van khong danh duoc (lenh 9)", MapDanhNhau.choDanhQuai(lang, p, goi9(thuong.id, 0), false));
            dung("lang " + load + ": boss Gym CUA MINH danh duoc (lenh 9, J2ME / khoa muc tieu)",
                    MapDanhNhau.choDanhQuai(lang, p, goi9(boss.id, 0), false));
            dung("lang " + load + ": boss Gym cua minh danh duoc (lenh 106 dien rong)",
                    MapDanhNhau.choDanhQuai(lang, p, goi106(1, boss.id), true));
            dung("lang " + load + ": lenh 106 nhieu muc tieu, boss Gym o sau",
                    MapDanhNhau.choDanhQuai(lang, p, goi106(1, thuong.id, boss.id), true));
            sai("lang " + load + ": lenh 106 chi quai thuong", MapDanhNhau.choDanhQuai(lang, p, goi106(1, thuong.id), true));
            sai("lang " + load + ": boss Gym cua NGUOI KHAC khong danh duoc (1 dau 1, lang an toan)",
                    MapDanhNhau.choDanhQuai(lang, khac, goi9(boss.id, 0), false));
            // doc trom roi tra lai: duong danh cu doc lai dung id / chieu
            final Message m9 = goi9(boss.id, 3);
            MapDanhNhau.choDanhQuai(lang, p, m9, false);
            BAO.bang("lang " + load + ": lenh 9 doc lai dung id sau khi kiem", Short.valueOf(boss.id), Short.valueOf(m9.dis.readShort()));
            BAO.bang("lang " + load + ": lenh 9 doc lai dung chieu", Integer.valueOf(3), Integer.valueOf(m9.dis.readByte()));
            final Message m106 = goi106(2, thuong.id, boss.id);
            MapDanhNhau.choDanhQuai(lang, p, m106, true);
            BAO.bang("lang " + load + ": lenh 106 doc lai dung chieu", Integer.valueOf(2), Integer.valueOf(m106.dis.readByte()));
            BAO.bang("lang " + load + ": lenh 106 doc lai dung so quai", Integer.valueOf(2), Integer.valueOf(m106.dis.readByte()));
            BAO.bang("lang " + load + ": lenh 106 doc lai dung id dau", Short.valueOf(thuong.id), Short.valueOf(m106.dis.readShort()));
            sai("lang " + load + ": goi hong (rong) khong nem loi, khong cho danh", MapDanhNhau.choDanhQuai(lang, p, new Message(9, new byte[0]), false));
            boss.hp = 0;
            boss.isDead = true;
            sai("lang " + load + ": boss Gym da guc khong con ngoai le", MapDanhNhau.choDanhQuai(lang, p, goi9(boss.id, 0), false));
            // thi tran van an toan: khong PvP (LuatPk.chan vung an toan)
            BAO.bang("lang " + load + ": PvP van bi chan (vung an toan)", Boolean.TRUE,
                    Boolean.valueOf(com.htbb.pokemon.LuatPk.chan(LuatPkGame.vungAnToan(lang), 27, 27, false, 0) != null));
        }
        // ---- ngoai lang: dong Pallet (1), map luyen 2, thi tran Rustboro 45: luat cu - moi quai danh duoc
        for (final int load : new int[]{1, 2, 45, 201}) {
            final Map m = banDo(load, load);
            final Char p = nv(m);
            final Monster thuong = quai(m, -30000 - load);
            sai("map " + load + ": khong phai lang", Map.isMapLang(m));
            dung("map " + load + ": quai hoang da danh duoc (lenh 9)", MapDanhNhau.choDanhQuai(m, p, goi9(thuong.id, 0), false));
            dung("map " + load + ": quai hoang da danh duoc (lenh 106)", MapDanhNhau.choDanhQuai(m, p, goi106(0, thuong.id), true));
            final Message m9 = goi9(thuong.id, 1);
            MapDanhNhau.choDanhQuai(m, p, m9, false);
            BAO.bang("map " + load + ": khong doc goi tin (id con nguyen)", Short.valueOf(thuong.id), Short.valueOf(m9.dis.readShort()));
        }
        // ---- BossLauDai.laGymCua
        final Map pallet = banDo(1, 1);
        final Char chu = nv(pallet);
        final Monster b = quai(pallet, -29999);
        sai("laGymCua: quai chua dang ky Gym", BossLauDai.laGymCua(b, chu));
        BossLauDai.datGym(b, 1, chu, 600000L);
        dung("laGymCua: boss Gym cua chu", BossLauDai.laGymCua(b, chu));
        sai("laGymCua: null an toan", BossLauDai.laGymCua(null, chu) || BossLauDai.laGymCua(b, null));
        b.hp = 0;
        b.isDead = true;
    }
}
