/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Vector;

public class SMSProvider {
    public Vector<String> decript = new Vector<>();
    public Vector<String> syntax = new Vector<>();
    public Vector<String> center = new Vector<>();
    public Vector<String> agent = new Vector<>();
    public Vector<String> provider = new Vector<>();

    public SMSProvider(int type) {
        this.decript.add("sms 15K: 10.000 xu");
        this.decript.add("sms 10K: 6.000 xu");
        this.decript.add("sms 15K: 10 lượng");
        this.syntax.add("TEAM NAP9 ");
        this.syntax.add("TEAM NAP9 ");
        this.syntax.add("TEAM LUONG9 ");
        this.center.add("8733");
        this.center.add("8633");
        this.center.add("8733");
        this.agent.add("0");
        this.agent.add("0");
        this.agent.add("0");
    }

    public void reset() {
        this.decript.removeAll(this.decript);
        this.syntax.removeAll(this.syntax);
        this.center.removeAll(this.center);
        this.agent.removeAll(this.agent);
    }

    public void setData(Vector<String> dec, Vector<String> syn, Vector<String> center, Vector<String> agent, Vector<String> provider) {
        this.decript = dec;
        this.syntax = syn;
        this.center = center;
        this.agent = agent;
        this.provider = provider;
    }
}
