/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import real.Char;
import real.Map;
import real.MessageCreator;

public class Cooking {
    public byte type = 0;
    public long timeStart = 0L;
    public byte timeCook = 60;
    public byte timeChamnc = 0;
    public byte water = 0;
    public Hashtable<String, CharCooking> listCharCooking = new Hashtable<>();
    public Hashtable<String, CharCooking> listCharGift = new Hashtable<>();
    public Vector<CharCooking> vCharCook = new Vector<>();
    public Vector<CharCooking> vCharGift = new Vector<>();
    boolean isStart = false;
    public boolean isAdminEnd = false;
    public boolean isAdminStart = false;
    public static byte timeNauBanh = 18;
    public static String[] namenoibanh = new String[]{"", "Nồi bánh chưng", "Nối bánh tét"};

    public synchronized boolean ChamNuoc() {
        if (Char.getDayOpen(0L).equals("2017-01-24")) {
            return false;
        } else if (this.isChamNc()) {
            this.timeChamnc++;
            return true;
        } else {
            return false;
        }
    }

    public boolean doGopNguyenLieu(Char p, int nbanh) {
        CharCooking c = this.listCharCooking.get(p.charname);
        if (c == null) {
            return false;
        } else {
            c.gop += nbanh;
            return true;
        }
    }

    public void setListCharCooking(String info) {
        if (info != null && !info.equals("")) {
            try {
                String[] data = Char.split(info, ",");

                for (int i = 0; i < data.length; i++) {
                    String[] data1 = Char.split(data[i], ":");
                    CharCooking c = new CharCooking();
                    c.name = data1[0].toLowerCase();
                    c.gop = Integer.parseInt(data1[1]);
                    this.listCharCooking.put(c.name, c);
                    this.vCharCook.add(c);
                }
            } catch (Exception var6) {
            }
        }
    }

    public void setListCharGift(String info) {
        if (info != null && !info.equals("")) {
            try {
                String[] data = Char.split(info, ",");

                for (int i = 0; i < data.length; i++) {
                    String[] data1 = Char.split(data[i], ":");
                    CharCooking c = new CharCooking();
                    c.name = data1[0].toLowerCase();
                    c.nbanh = Integer.parseInt(data1[1]);
                    this.listCharGift.put(c.name, c);
                    this.vCharGift.add(c);
                }
            } catch (Exception var6) {
            }
        }
    }

    public void startCook() {
        if (!Char.getDayOpen(0L).equals("2017-01-24")) {
            Calendar cl = Calendar.getInstance();
            int ihour = cl.get(11);
            int minute = cl.get(12);
            if ((this.isAdminStart || ihour == timeNauBanh) && !this.isStart) {
                this.isStart = true;
                this.timeChamnc = 0;
                this.timeCook = 60;
                this.timeStart = System.currentTimeMillis();
                this.water = 0;

                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Bắt đầu nấu bánh " + (this.type == 1 ? "bánh chưng" : "bánh tét")));
                } catch (Exception var5) {
                }
            }
        }
    }

    public void isEnd() {
        Calendar cl = Calendar.getInstance();
        int ihour = cl.get(11);
        int minute = cl.get(12);
        if (this.isStart && (ihour > timeNauBanh || this.isAdminEnd)) {
            this.isStart = false;
            this.isAdminEnd = false;
            this.timeStart = 0L;
            quickSort(this.vCharCook, 0);
            System.out.println("KET THUC NAU BANH: " + this.vCharCook.size());

            for (int i = 0; i < this.vCharCook.size(); i++) {
                CharCooking c = this.vCharCook.get(i);
                CharCooking cg = this.listCharGift.get(c.name);
                if (cg == null) {
                    cg = new CharCooking();
                    cg.name = c.name;
                    this.listCharGift.put(c.name, cg);
                    this.vCharGift.add(cg);
                }

                int nbanh = c.gop;
                c.gop = 0;
                if (nbanh > 30) {
                    nbanh = 30;
                }

                if (this.water == 1) {
                    nbanh /= 2;
                } else if (this.water == 2) {
                    nbanh = 0;
                }

                cg.nbanh += nbanh;
                if (i < 10) {
                    if (this.water == 0) {
                        cg.nbanh += 30;
                    } else if (this.water == 0) {
                        cg.nbanh += 15;
                    }
                }
            }

            this.vCharCook.removeAllElements();
            this.listCharCooking.clear();
            Database.instance.saveCooking(this, this.type);
            Database.instance.saveOrtherLog("", namenoibanh[this.type], "so luong banh khi ket thuc: " + this.water, "ecook");

            try {
                Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Đã kết thúc nấu bánh"));
            } catch (Exception var8) {
            }
        }
    }

    public String getListChar() {
        String info = "";

        for (int i = 0; i < this.vCharCook.size(); i++) {
            if (!info.equals("")) {
                info = info + ",";
            }

            CharCooking c = this.vCharCook.get(i);
            info = info + c.name + ":" + c.gop;
        }

        return info;
    }

    public String getListCharGift() {
        String info = "";

        for (int i = 0; i < this.vCharGift.size(); i++) {
            if (!info.equals("")) {
                info = info + ",";
            }

            CharCooking c = this.vCharGift.get(i);
            info = info + c.name + ":" + c.nbanh;
        }

        return info;
    }

    public CharCooking addCharCook(Char p) {
        CharCooking c = this.listCharCooking.get(p.charname.toLowerCase());
        if (c == null) {
            c = new CharCooking();
            c.name = p.charname;
            c.gop = 0;
            this.listCharCooking.put(p.charname, c);
            this.vCharCook.add(c);
        }

        return c;
    }

    public String getNameOption() {
        Calendar cl = Calendar.getInstance();
        int ihour = cl.get(11);
        if (!this.isChamNc() && ihour != timeNauBanh) {
            return this.type == 1 ? "Góp bánh chưng" : "Góp bánh tét";
        } else {
            return "Châm nước";
        }
    }

    public synchronized boolean isChamNc() {
        Calendar cl = Calendar.getInstance();
        int ihour = cl.get(11);
        if (ihour != timeNauBanh) {
            return false;
        } else {
            return this.timeChamnc == 0 && this.timeCook <= 40 ? true : this.timeChamnc == 1 && this.timeCook <= 20;
        }
    }

    public int getBanhNhan(CharCooking c) {
        if (this.water == 0) {
            return c.nbanh;
        } else if (this.water == 1) {
            return c.nbanh == 1 ? 1 : c.nbanh / 2;
        } else {
            return 0;
        }
    }

    public void update() {
        long t = System.currentTimeMillis();
        if (this.timeStart > 0L && this.timeCook > 0) {
            if (t - this.timeStart >= 60000L) {
                this.timeStart = System.currentTimeMillis();
                this.timeCook--;
                if (this.timeCook == 20) {
                    if (this.timeChamnc == 0) {
                        this.water = 1;
                    }
                } else if (this.timeCook == 0) {
                    if (this.timeChamnc == 0) {
                        this.water = 2;
                    } else if (this.timeChamnc == 1) {
                        this.water = 1;
                    }
                }

                try {
                    if (this.isChamNc()) {
                        Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(namenoibanh[this.type] + " chưa được châm nước"));
                    }
                } catch (Exception var4) {
                }
            }

            this.isEnd();
        } else {
            this.startCook();
        }
    }

    public void doGetBanh(Char p) {
        if (this.water != 2) {
            CharCooking c = this.listCharCooking.get(p.charname);
        }
    }

    public static void quickSort(Vector<CharCooking> actors, int type) {
        recQuickSort(actors, 0, actors.size() - 1, type);
    }

    private static void recQuickSort(Vector<CharCooking> actors, int left, int right, int type) {
        try {
            if (right - left <= 0) {
                return;
            }

            long pivot = actors.elementAt(right).gop;
            int partition = partitionIt(actors, left, right, pivot, type);
            recQuickSort(actors, left, partition - 1, type);
            recQuickSort(actors, partition + 1, right, type);
        } catch (Exception var7) {
        }
    }

    private static int partitionIt(Vector<CharCooking> actors, int left, int right, long pivot, int type) {
        int leftPtr = left - 1;
        int rightPtr = right;

        try {
            while (true) {
                while (actors.elementAt(++leftPtr).gop > pivot) {
                }

                while (rightPtr > 0) {
                    if (actors.elementAt(--rightPtr).gop >= pivot) {
                        break;
                    }
                }

                if (leftPtr >= rightPtr) {
                    swap(actors, leftPtr, right, type);
                    break;
                }

                swap(actors, leftPtr, rightPtr, type);
            }
        } catch (Exception var9) {
        }

        return leftPtr;
    }

    private static void swap(Vector<CharCooking> actors, int dex1, int dex2, int type) {
        CharCooking temp = actors.elementAt(dex2);
        if (actors.elementAt(dex2).gop != actors.elementAt(dex1).gop) {
            actors.setElementAt(actors.elementAt(dex1), dex2);
            actors.setElementAt(temp, dex1);
        }
    }
}
