import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import real.Char;
import real.MessageCreator;
import real.NpcTinhBanDo;

/**
 * Goi 24 (mua vat pham loai 3) chi mua duoc mon trong danh sach ban cua NPC dang dung canh (NpcTinhBanDo.banTaiCho).
 * Bot phu kin 31/10/2026: may chu chi kiem danh sach trang chung (MessageCreator.ALL_ID_ITEM_NPC_SELL) - dung canh
 * Machamp / Bronzong Pewter (hoac trong nha 103 chi co Machamp) mua duoc giap Indigo 467..506 cua NPC 27. Kiem:
 * danh sach dsBan khop dung goi 23 ma NPC gui (khong lam hong cua hang that), va dung cho / sai cho tren tep cMap that.
 */
public final class KiemMuaDungNpc {

    private KiemMuaDungNpc() {
    }

    public static void chay() {
        Kiem.nhom("Goi 24: chi mua duoc mon NPC dang dung canh ban (giap Indigo khong mua o Pewter)");
        try {
            khopGoi23();
            dungCho();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static Char nv(final int mapID, final boolean nha, final int load, final int ox, final int oy) {
        final Char p = new Char(null);
        final real.Map m = new real.Map();
        m.isMapOffline = nha;
        m.mapIDLoadMap = nha ? -1 : load;
        p.map = m;
        p.mapID = mapID;
        p.x = ox * 16 + 8;
        p.y = oy * 16 + 8;
        return p;
    }

    /** id trong goi 23 (bo khung [cmd][len 2], byte 1, byte lop, short n, n short). */
    static int[] idGoi(final io.Message m) throws Exception {
        final byte[] b = m.toByteArray();
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
        in.readByte();
        in.readByte();
        final int n = in.readShort();
        final int[] kq = new int[n];
        for (int i = 0; i < n; i++) {
            kq[i] = in.readShort();
        }
        return kq;
    }

    /** Moi id NPC gui deu nam trong dsBan, va dsBan khong rong hon (dem so id trong cac doan == so id goi). */
    static void khop(final String ten, final int[] goi, final int[] doan) {
        boolean du = true;
        for (final int id : goi) {
            boolean co = false;
            for (int k = 0; k + 1 < doan.length; k += 2) {
                co |= id >= doan[k] && id <= doan[k + 1];
            }
            du &= co;
        }
        int dem = 0;
        for (int k = 0; k + 1 < doan.length; k += 2) {
            dem += doan[k + 1] - doan[k] + 1;
        }
        Kiem.dung(ten + ": " + goi.length + " mon trong goi 23 deu trong dsBan, dsBan " + dem + " mon", du && dem == goi.length);
    }

    static void khopGoi23() throws Exception {
        khop("Bronzong 3 nha 104", idGoi(MessageCreator.createMsgSellArmoOfNPC(nv(104, true, -1, 7, 4), 0, 3)), NpcTinhBanDo.dsBan(3, 104));
        khop("Bronzong 3 ban do > 200", idGoi(MessageCreator.createMsgSellArmoOfNPC(nv(260, true, -1, 7, 4), 0, 3)), NpcTinhBanDo.dsBan(3, 260));
        khop("Bronzong 27 Indigo", idGoi(MessageCreator.createMsgSellArmoOfNPC(nv(260, true, -1, 7, 4), 0, 27)), NpcTinhBanDo.dsBan(27, 260));
        // Machamp 2 (< 200): do cam tay (79..113, 465, 466, the mua ban 675) + trang bi thu (509..568) - hai goi
        final int[] vk = idGoi(MessageCreator.createMsgNPCSellWeapone(nv(103, true, -1, 8, 4), true));
        final int[] thu = idGoi(MessageCreator.createMsgNPCSellWeapone(nv(103, true, -1, 8, 4), false));
        final int[] ca = new int[vk.length + thu.length];
        System.arraycopy(vk, 0, ca, 0, vk.length);
        System.arraycopy(thu, 0, ca, vk.length, thu.length);
        khop("Machamp 2 nha 103 (cam tay + thu)", ca, NpcTinhBanDo.dsBan(2, 103));
        khop("Machamp 2 ban do > 200", idGoi(MessageCreator.createMsgNPCSellWeapone(nv(261, true, -1, 7, 5), true)), NpcTinhBanDo.dsBan(2, 261));
        // moi doan cua dsBan nam trong danh sach trang chung (khong mo them mon nao)
        MessageCreator.doInitItemNpcSell();
        boolean trongTrang = true;
        for (final int loai : new int[]{2, 3, 24, 27, 28}) {
            for (final int map : new int[]{103, 260}) {
                final int[] d = NpcTinhBanDo.dsBan(loai, map);
                for (int k = 0; k + 1 < d.length; k += 2) {
                    for (int id = d[k]; id <= d[k + 1]; id++) {
                        boolean co = false;
                        for (int j = 0; j + 1 < MessageCreator.ALL_ID_ITEM_NPC_SELL.length; j += 2) {
                            co |= id >= MessageCreator.ALL_ID_ITEM_NPC_SELL[j] && id <= MessageCreator.ALL_ID_ITEM_NPC_SELL[j + 1];
                        }
                        trongTrang &= co;
                    }
                }
            }
        }
        Kiem.dung("dsBan khong vuot danh sach trang ALL_ID_ITEM_NPC_SELL", trongTrang);
    }

    static void dungCho() {
        // tep cMap that: nha 103 = Machamp (2), nha 104 = Bronzong (3), nha 260 = Bronzong Indigo (27); lang 0: Machamp 32,45 / Bronzong 34,10
        Kiem.dung("nha 103 (Machamp): mua vu khi 79", NpcTinhBanDo.banTaiCho(nv(103, true, -1, 8, 4), 79));
        Kiem.dung("nha 103 (Machamp): KHONG mua giap Indigo 467", !NpcTinhBanDo.banTaiCho(nv(103, true, -1, 8, 4), 467));
        Kiem.dung("nha 103 (Machamp): KHONG mua giap 3 (Bronzong o nha 104)", !NpcTinhBanDo.banTaiCho(nv(103, true, -1, 8, 4), 3));
        Kiem.dung("nha 104 (Bronzong): mua giap 3 / 170", NpcTinhBanDo.banTaiCho(nv(104, true, -1, 7, 4), 3)
                && NpcTinhBanDo.banTaiCho(nv(104, true, -1, 7, 4), 170));
        Kiem.dung("nha 104 (Bronzong Pewter): KHONG mua giap Indigo 467", !NpcTinhBanDo.banTaiCho(nv(104, true, -1, 7, 4), 467));
        Kiem.dung("nha 260 (Bronzong Indigo): mua giap Indigo 467 / 506 / phien da 569", NpcTinhBanDo.banTaiCho(nv(260, true, -1, 7, 4), 467)
                && NpcTinhBanDo.banTaiCho(nv(260, true, -1, 7, 4), 506) && NpcTinhBanDo.banTaiCho(nv(260, true, -1, 7, 4), 569));
        Kiem.dung("lang 0 canh Bronzong 34,10: mua giap 3, KHONG 467", NpcTinhBanDo.banTaiCho(nv(0, false, 0, 34, 12), 3)
                && !NpcTinhBanDo.banTaiCho(nv(0, false, 0, 34, 12), 467));
        Kiem.dung("lang 0 xa moi NPC (60,60): khong mua duoc", !NpcTinhBanDo.banTaiCho(nv(0, false, 0, 60, 60), 79));
        Kiem.dung("Indigo 201 canh Bronzong 27 (22,27): mua 467", NpcTinhBanDo.banTaiCho(nv(201, false, 201, 22, 29), 467));
        Kiem.dung("Indigo 201 canh NPC 28 (9,38): mua vu khi 174, KHONG 467", NpcTinhBanDo.banTaiCho(nv(201, false, 201, 9, 40), 174)
                && !NpcTinhBanDo.banTaiCho(nv(201, false, 201, 9, 40), 467));
    }
}
