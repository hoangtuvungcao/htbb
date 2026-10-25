/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class CharInboxMessage {
    public String sender = "";
    public String info = "";
    public int idMsg = 0;

    /**
     * Doc thu (goi 110, byte chi so trong hop thu): id DONG tob_inbox can xoa = idMsg cua thu o chi so do, -1 neu chi so
     * khong hop le. Truoc 25/10/2026 Map.doReadMessage xoa "where id=" + CHI SO (0, 1, 2...) - tuc la xoa dong co id
     * trung chi so, thuong la thu dang cho cua NGUOI CHOI KHAC (bot bat_thuong / bt_thu phat hien).
     */
    public static int idCanXoa(final java.util.List<CharInboxMessage> hop, final int chiSo) {
        if (hop == null || chiSo < 0 || chiSo >= hop.size()) {
            return -1;
        }
        final CharInboxMessage ib = hop.get(chiSo);
        return ib == null || ib.idMsg <= 0 ? -1 : ib.idMsg;
    }
}
