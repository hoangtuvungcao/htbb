/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package common;

public final class Assert {
    public static void verify(boolean condition) {
        if (!condition) {
            throw new AssertionFailed();
        }
    }

    public static void verify(boolean condition, String message) {
        if (!condition) {
            throw new AssertionFailed(message);
        }
    }
}
