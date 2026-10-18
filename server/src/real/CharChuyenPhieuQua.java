/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class CharChuyenPhieuQua {
    public String nameChuyen = "";
    public int soluong = 0;
    public long time = System.currentTimeMillis();

    public CharChuyenPhieuQua() {
        this.time = System.currentTimeMillis() + 90000L;
    }

    public boolean isTimeOver() {
        return System.currentTimeMillis() - this.time >= 0L;
    }
}
