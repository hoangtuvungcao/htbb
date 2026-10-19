/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package io;

import java.util.Vector;

public class SessionManager {
    public static SessionManager instance = new SessionManager();
    public Vector<Session> sessionList = new Vector<>();

    private SessionManager() {
    }

    public int size() {
        return this.sessionList.size();
    }

    public Session get(int index) {
        try {
            return this.sessionList.get(index);
        } catch (IndexOutOfBoundsException var3) {
            return null;
        }
    }

    public void add(Session s) {
        this.sessionList.add(s);
    }

    public void remove(Session s) {
        this.sessionList.remove(s);
    }

    public void removeSession(Session s) {
        int size = this.sessionList.size();

        for (int i = 0; i < size; i++) {
            try {
                if (this.sessionList.get(i).userID == s.userID) {
                    this.sessionList.remove(i);
                    return;
                }
            } catch (Exception var5) {
            }
        }
    }
}
