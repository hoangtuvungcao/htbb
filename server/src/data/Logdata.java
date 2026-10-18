/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Hashtable;
import java.util.Vector;
import real.AdminHandler;
import real.Char;
import real.Map;
import server.TeamServer;

public class Logdata {
    public static Vector<String> query = new Vector<>();
    public static Hashtable<String, Integer> topRich = new Hashtable<>();
    public static Hashtable<String, Integer> topLv = new Hashtable<>();
    public static Vector<CharInfo> charTopRich = new Vector<>();
    public static Vector<CharInfo> charTopLv = new Vector<>();
    public static byte idAdv = 0;
    public static byte idAdv2 = 0;

    public static void addChar(Char p, int type) {
        CharInfo cc = Map.createCharInfo(p);
        cc.money = p.getxu();
        cc.luong = p.getLuong();
        charTopRich.add(cc);
        charTopLv.add(cc);
    }

    public static void checkAddCharTop(CharInfo cc, int typetop) {
        try {
            if (typetop == 0) {
                CharInfo me = null;
                int pos = -1;

                for (int i = 0; i < Map.topRich.size(); i++) {
                    CharInfo c = Map.topRich.get(i);
                    if (c.name.equals(cc.name)) {
                        me = c;
                    } else if (c.money < cc.money && pos == -1) {
                        pos = i;
                    }
                }

                if (me != null) {
                    if (pos == -1) {
                        me.money = cc.money;
                        me.luong = cc.luong;
                        me.wearingItem = cc.wearingItem;
                    } else {
                        Map.topRich.remove(me);
                    }
                }

                if (pos != -1) {
                    cc.money = cc.money;
                    cc.luong = cc.luong;
                    if (me == null) {
                        Map.topRich.remove(Map.topRich.size() - 1);
                    }

                    Map.topRich.insertElementAt(cc, pos);
                }
            } else if (typetop == 1) {
                CharInfo me = null;
                int pos = -1;

                for (int ix = 0; ix < Map.topLv.size(); ix++) {
                    CharInfo c = Map.topLv.get(ix);
                    if (c.name.equals(cc.name)) {
                        me = c;
                    } else if (c.money < cc.money && pos == -1) {
                        pos = ix;
                    }
                }

                if (me != null) {
                    if (pos == -1) {
                        me.money = cc.money;
                        me.luong = cc.luong;
                        me.wearingItem = cc.wearingItem;
                    } else {
                        Map.topLv.remove(me);
                    }
                }

                if (pos != -1) {
                    cc.money = cc.money;
                    cc.luong = cc.luong;
                    if (me == null) {
                        Map.topLv.remove(Map.topLv.size() - 1);
                    }

                    Map.topLv.insertElementAt(cc, pos);
                }
            }
        } catch (Exception var6) {
        }
    }

    public Logdata() {
        (new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("LOG DATA");

                while (TeamServer.running && !AdminHandler.isStopServer) {
                    Vector<String> data = Database.cloneVectorString(Logdata.query);
                    Logdata.query.removeAllElements();
                    if (data.size() > 0) {
                        Database.instance.saveAllLog(data);
                    }

                    if (System.currentTimeMillis() - Map.timeGettop > 900000L && !Map.getTop) {
                        Map.getTop = true;
                        Map.timeGettop = System.currentTimeMillis();
                        if (Logdata.topRich.size() == 0) {
                            Map.topRich = Database.instance.getTopRich();
                        }

                        if (Logdata.topLv.size() == 0) {
                            Map.topLv = Database.instance.getTopLV();
                        }

                        Map.topClan = Database.instance.getTopClan();

                        for (int i = 0; i < 3; i++) {
                            Map.topLvNation.set(i, Database.instance.getTopCountry(i, 0));
                            Map.topHonor.set(i, Database.instance.getTopCountry(i, 1));
                        }

                        Map.getTop = false;
                    }

                    if (Logdata.topRich.size() > 0) {
                        while (Logdata.charTopLv.size() > 0) {
                            CharInfo cc = Logdata.charTopLv.remove(0);
                            Logdata.checkAddCharTop(cc, 1);
                        }

                        while (Logdata.charTopRich.size() > 0) {
                            CharInfo cc = Logdata.charTopRich.remove(0);
                            Logdata.checkAddCharTop(cc, 0);
                        }
                    }

                    try {
                        Thread.sleep(100L);
                    } catch (Exception var3) {
                        var3.printStackTrace();
                    }
                }
            }
        }).start();
        if (Map.openLog) {
            (new Thread() {
                @Override
                public void run() {
                    try {
                        for (; !AdminHandler.isStopServer; Thread.sleep(900000L)) {
                            try {
                                String info = Net.getHttpADV("http://27.0.14.67/adsinkpah.txt");
                                System.out.println(info);
                                String[] dataAdv = Char.split(info, "\n");
                                Char.infoAdv.removeAllElements();

                                for (int i = 0; i < dataAdv.length; i++) {
                                    String[] data = Char.split(dataAdv[i], ";");
                                    int idGame = Integer.parseInt(Map.getNumberFromString(data[0]).trim());
                                    if (idGame != 9) {
                                        AdvInfo av = new AdvInfo();
                                        av.linkDown = data[1] + "&from=kpah";
                                        av.infoDownload = "Bạn thật sự muốn tải game " + data[2] + " ?";

                                        for (int j = 3; j < data.length; j++) {
                                            av.info.add(data[2] + ":" + data[j]);
                                        }

                                        Char.infoAdv.add(av);
                                    }
                                }

                                if (Char.infoAdv.size() > 0) {
                                    if (Char.infoAdv.size() == 1) {
                                        Logdata.idAdv = 0;
                                    } else {
                                        int id = Map.r.nextInt(Char.infoAdv.size());

                                        while (id == Logdata.idAdv) {
                                            id = Map.r.nextInt(Char.infoAdv.size());
                                        }

                                        Logdata.idAdv = (byte)id;
                                    }
                                }
                            } catch (Exception var8) {
                            }
                        }
                    } catch (Exception var9) {
                    }
                }
            }).start();
            (new Thread() {
                @Override
                public void run() {
                    try {
                        for (; !AdminHandler.isStopServer; Thread.sleep(900000L)) {
                            try {
                                String info = Net.getHttpADV("http://27.0.14.67/ads/adsinkpah.txt");
                                String[] dataAdv = Char.split(info, "\n");
                                Char.infoAdv2.removeAllElements();

                                for (int i = 0; i < dataAdv.length; i++) {
                                    String[] data = Char.split(dataAdv[i], ";");
                                    int idGame = Integer.parseInt(Map.getNumberFromString(data[0]).trim());
                                    if (idGame != 9) {
                                        AdvInfo av = new AdvInfo();
                                        av.linkDown = data[1] + "&from=kpah";
                                        av.infoDownload = "Bạn thật sự muốn tải game " + data[2] + " ?";

                                        for (int j = 3; j < data.length; j++) {
                                            av.info.add(data[2] + ":" + data[j]);
                                        }

                                        Char.infoAdv2.add(av);
                                    }
                                }

                                if (Char.infoAdv2.size() > 0) {
                                    if (Char.infoAdv2.size() == 1) {
                                        Logdata.idAdv2 = 0;
                                    } else {
                                        int id = Map.r.nextInt(Char.infoAdv2.size());

                                        while (id == Logdata.idAdv2) {
                                            id = Map.r.nextInt(Char.infoAdv2.size());
                                        }

                                        Logdata.idAdv2 = (byte)id;
                                    }
                                }
                            } catch (Exception var8) {
                            }
                        }
                    } catch (Exception var9) {
                    }
                }
            }).start();
        }
    }
}
