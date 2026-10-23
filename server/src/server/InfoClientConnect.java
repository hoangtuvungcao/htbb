/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package server;

import java.util.Hashtable;
import java.util.Vector;
import util.Logger;

public class InfoClientConnect {
    public String ip;
    public int count;
    public int count1;
    public int count2;
    public int maxConnectPerSecond = 0;
    public int maxLogin = 0;
    public int maxTaoChar = 0;
    public long timeConnect = System.currentTimeMillis();
    public long timeLogin = System.currentTimeMillis();
    public long timetaoChar = System.currentTimeMillis();
    // Kiem lag 10/2026: truoc day = luc TAO muc IP (lan ket noi dau) -> lan chon nhan vat DAU TIEN trong 5 s
    // sau khi IP do ket noi (dang nhap binh thuong: ~0.1-3 s) bi SelectCharHandler cho 2 giay. Luat
    // "chon 2 lan trong 5 s thi cho 2 s" giu nguyen; chi lan dau khong con bi phat.
    public long timeSelectChar = 0L;
    public long timeWriteLog = System.currentTimeMillis();
    public Vector<String> charname = new Vector<>();
    public Hashtable<String, String> hcharname = new Hashtable<>();

    public void addUser(String uname) {
        if (this.hcharname.get(uname) == null) {
            this.hcharname.put(uname, uname);
            this.charname.add(uname);
        }
    }

    public boolean canSelectChar() {
        if (System.currentTimeMillis() - this.timeSelectChar >= 5000L) {
            this.timeSelectChar = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    public void countConnect() {
        if (System.currentTimeMillis() - this.timeConnect < 1000L) {
            this.count++;
            this.maxConnectPerSecond = this.maxConnectPerSecond > this.count ? this.maxConnectPerSecond : this.count;
        } else {
            this.count++;
            this.timeConnect = System.currentTimeMillis();
            this.maxConnectPerSecond = this.maxConnectPerSecond > this.count ? this.maxConnectPerSecond : this.count;
            this.count = 0;
        }
    }

    public void countLogin() {
        if (System.currentTimeMillis() - this.timeLogin < 1000L) {
            this.count1++;
            this.maxLogin = this.maxLogin > this.count1 ? this.maxLogin : this.count1;
        } else {
            this.count1++;
            this.timeLogin = System.currentTimeMillis();
            this.maxLogin = this.maxLogin > this.count1 ? this.maxLogin : this.count1;
            this.count1 = 0;
        }
    }

    public void countTaoChar() {
        if (System.currentTimeMillis() - this.timetaoChar < 1000L) {
            this.count2++;
            this.maxTaoChar = this.maxTaoChar > this.count2 ? this.maxTaoChar : this.count2;
        } else {
            this.count2++;
            this.timetaoChar = System.currentTimeMillis();
            this.maxTaoChar = this.maxTaoChar > this.count2 ? this.maxTaoChar : this.count2;
            this.count2 = 0;
        }
    }

    public void writeLog() {
        if (System.currentTimeMillis() - this.timeWriteLog >= 300000L) {
            this.timeWriteLog = System.currentTimeMillis();
            String info = "";

            for (int i = 0; i < this.charname.size(); i++) {
                info = info + this.charname.get(i) + ",";
            }

            Logger.logStringWithDate(info, this.ip + ".txt");
        }
    }
}
