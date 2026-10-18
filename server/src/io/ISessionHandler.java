/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package io;

import java.io.IOException;

public interface ISessionHandler {
    void processMessage(Session var1, Message var2) throws IOException;

    void onDisconnected(Session var1);

    void onDisconnectedFromLocalPlace(Session var1);
}
