/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class CCUProvider {
    public int ccu = 0;
    public int max = 0;
    public int min = 100000;
    public String provider = "0";
    public int tempCCU;
    public int[] ccuFirmWareTemp = new int[6];
    public int[] ccuFirmWare = new int[6];

    public CCUProvider(String pro) {
        this.provider = pro;
        this.ccu = 0;
    }

    public void upCCU() {
        if (this.ccu > this.max) {
            this.max = this.ccu;
        }
    }

    public int getMinCCU() {
        return 0;
    }

    public String getInfoLog() {
        String[] firm = new String[]{"CCU: ", "Java: ", "Andr: ", "Ip: ", "3D: ", "PC: ", "WP: "};
        String info = firm[0] + this.ccu;

        for (int i = 1; i < firm.length; i++) {
            info = info + "_" + firm[i] + this.ccuFirmWare[i - 1];
        }

        return info;
    }
}
