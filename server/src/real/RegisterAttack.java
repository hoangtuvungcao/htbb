/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.CharInfo;
import data.Database;
import data.NewClan;
import java.util.Vector;

public class RegisterAttack {
    public String dayAttack = "";
    public byte idMyAttack = -1;
    public byte idNationAttackMe = -1;
    public long time = System.currentTimeMillis();
    public byte win = 0;
    public byte lose = 0;
    public byte timeX2 = 0;

    public void settime() {
    }

    public void setMyAttack(int idNationMyAttack) {
        this.idMyAttack = (byte)idNationMyAttack;
    }

    public void setNationAttackMe(int idNationAttackMe) {
        this.idNationAttackMe = (byte)idNationAttackMe;
    }

    public void setDayAttack(String dayAttack) {
        this.dayAttack = dayAttack;
    }

    public void update(int country) {
    }

    public void reset() {
        this.dayAttack = "";
        this.idMyAttack = -1;
        this.idNationAttackMe = -1;
    }

    public boolean openX2(int country, int hour) {
        if (this.timeX2 > 0) {
            if (hour == 9) {
                Map.x2Country[country] = 3;
                Map.minuteX2Country[country] = System.currentTimeMillis() + this.timeX2 * 60 * 60 * 1000;
                Database.instance.saveEvent(Map.event.getInfo());
                Database.instance.saveOrtherLog("", Map.nameCountry[country], "Bat 150 " + this.timeX2, "x150");
                this.timeX2 = 0;
            }

            return true;
        } else {
            return false;
        }
    }

    public void addGif(int country) {
        int soluong = 0;
        boolean openx150 = false;
        if (this.win != 0 || this.lose != 0) {
            if (this.win > 0) {
                soluong = 3;
                this.timeX2 = 6;
            }

            if (openx150) {
                Map.x2Country[country] = 3;
                Map.minuteX2Country[country] = System.currentTimeMillis() + 3600000L;
                this.win = 0;
                this.lose = 0;
                Database.instance.saveEvent(Map.event.getInfo());
                Database.instance.saveOrtherLog("", Map.nameCountry[country], "Bat 150 " + this.timeX2, "x150");
                this.timeX2 = 0;
            }

            if (soluong > 0) {
                NewClan clan = NewClan.getClan(Map.idClanTown[country]);
                if (clan != null) {
                    Char p = CharManager.instance.getCharByCharName(clan.master.toLowerCase());
                    if (p != null) {
                        for (byte i = 0; i < 5; i++) {
                            p.doAddGemItem(InfoGifLucky.idMaterial[2][i], soluong, false);
                            p.doAddGemItem(InfoGifLucky.idMaterial[5][i], soluong, false);
                        }

                        p.sendMessage(MessageCreator.createCharGemItem(p));
                        Database.instance
                            .saveOrtherLog("", p.charname, this.timeX2 + "_" + this.win + "_" + this.lose + "_Nhận ng liệu 6 mỗi loại " + soluong + " viên", "gifwar");
                    } else {
                        Database.instance.saveOrtherLog("", clan.master, this.timeX2 + "_" + this.win + "_" + this.lose + "_không online", "gifwarFail");
                    }
                }
            }

            Vector<CharInfo> topLieTram = Map.topPK.get(country);
            int len = topLieTram.size();
            if (len > 3) {
                len = 3;
            }

            int[] ngemlientram = new int[]{5, 3, 2};

            for (int i = 0; i < len; i++) {
                CharInfo cinfo = topLieTram.get(i);
                if (cinfo.pk <= 0) {
                    Database.instance.saveOrtherLog("", topLieTram.get(i).name, "ko co diem lien tram " + cinfo.pk, "gifPKFail");
                } else {
                    Char p = CharManager.instance.getCharByCharName(cinfo.name.toLowerCase());
                    if (p == null) {
                        Database.instance.saveOrtherLog("", topLieTram.get(i).name, "khong online", "gifPKFail");
                    } else {
                        String inf = "";
                        byte[] sl = new byte[5];

                        for (byte j = 0; j < ngemlientram[i]; j++) {
                            int r = Map.r.nextInt(5);
                            sl[r]++;
                            p.doAddGemItem(InfoGifLucky.idMaterial[2][r], 1, true);
                        }

                        p.doAddGemItem(154, ngemlientram[i], true);

                        for (int j = 0; j < 5; j++) {
                            if (sl[j] > 0) {
                                inf = inf + Map.gemTemplate[InfoGifLucky.idMaterial[2][j]].name + "_" + sl[j] + ",";
                            }
                        }

                        inf = inf + Map.gemTemplate[154].name + "_" + ngemlientram[i];
                        Database.instance.saveOrtherLog("", p.charname, inf, "gifPK");
                    }
                }
            }

            this.win = 0;
            this.lose = 0;
        }
    }
}
