/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import io.Message;
import java.util.Vector;
import real.Char;
import real.Map;
import real.MessageCreator;

public class Xoso {
    public static int max = 0;
    public static int min = 9;
    public static byte[] numberBuy = new byte[100];

    public static void reset() {
        numberBuy = new byte[100];
    }

    public static String getTotalNumber() {
        String info = String.valueOf(numberBuy[0]);

        for (byte i = 1; i < 100; i++) {
            info = info + "," + numberBuy[i];
        }

        return info + "," + Map.winNumber;
    }

    public static boolean canBuy(byte type, byte number) {
        return numberBuy[max] != numberBuy[number] || numberBuy[max] == numberBuy[min] || numberBuy[max] - numberBuy[min] < 10;
    }

    public static synchronized String buyNumber(Char p, byte number, byte type) {
        if (numberBuy[max] == numberBuy[number] && numberBuy[max] != numberBuy[min] && numberBuy[max] - numberBuy[min] >= 10) {
            return "Số này tạm thời đã hết. Xin chọn số khác.";
        } else {
            numberBuy[number]++;
            if (numberBuy[number] > numberBuy[max]) {
                max = number;
            }

            getMin();
            p.doBuyNumber(type, number);
            Database.instance.saveEvent(Map.event.getInfo());
            return "Chúc mừng bạn đã mua được vé " + number;
        }
    }

    public static void getMin() {
        for (byte i = 0; i < numberBuy.length; i++) {
            if (numberBuy[min] > numberBuy[i]) {
                min = i;
            }
        }
    }

    public static synchronized void doRequestBuyNumber(Char p, byte idBuy) {
        try {
            Message m = new Message(-63);
            Vector<Byte> number = getAvaliableumber((byte)0);
            if (number.size() == 0) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Hết vé.", ""));
                return;
            }

            m.dos.writeByte(number.size());

            for (byte i = 0; i < number.size(); i++) {
                m.dos.writeByte(number.get(i));
            }

            p.sendMessage(m);
        } catch (Exception var5) {
        }
    }

    public static Vector<Byte> getAvaliableumber(byte type) {
        Vector<Byte> number = new Vector<>();

        for (byte i = 0; i < 100; i++) {
            if (canBuy(type, i)) {
                number.add(i);
            }
        }

        return number;
    }

    public static void selectWinNumber() {
        int count = 0;
        Vector<Byte> numberWin = new Vector<>();

        for (byte i = 0; i < numberBuy.length; i++) {
            count += numberBuy[i];
        }

        if (count > 700) {
            Map.winNumber = (byte)Map.r.nextInt(100);
        } else {
            if (count > 80 && count <= 100) {
                for (byte i = 0; i < numberBuy.length; i++) {
                    if (numberBuy[i] == 0) {
                        numberWin.add(i);
                    }
                }
            } else if (count > 100 && count <= 140) {
                for (byte ix = 0; ix < numberBuy.length; ix++) {
                    if (numberBuy[ix] <= 1) {
                        numberWin.add(ix);
                    }
                }
            } else if (count > 140 && count <= 210) {
                for (byte ixx = 0; ixx < numberBuy.length; ixx++) {
                    if (numberBuy[ixx] <= 2) {
                        numberWin.add(ixx);
                    }
                }
            } else if (count > 210 && count <= 280) {
                for (byte ixxx = 0; ixxx < numberBuy.length; ixxx++) {
                    if (numberBuy[ixxx] <= 3) {
                        numberWin.add(ixxx);
                    }
                }
            } else if (count > 280 && count <= 350) {
                for (byte ixxxx = 0; ixxxx < numberBuy.length; ixxxx++) {
                    if (numberBuy[ixxxx] <= 4) {
                        numberWin.add(ixxxx);
                    }
                }
            } else if (count > 350 && count <= 420) {
                for (byte ixxxxx = 0; ixxxxx < numberBuy.length; ixxxxx++) {
                    if (numberBuy[ixxxxx] <= 5) {
                        numberWin.add(ixxxxx);
                    }
                }
            } else if (count > 420 && count <= 490) {
                for (byte ixxxxxx = 0; ixxxxxx < numberBuy.length; ixxxxxx++) {
                    if (numberBuy[ixxxxxx] <= 6) {
                        numberWin.add(ixxxxxx);
                    }
                }
            } else if (count > 490 && count <= 560) {
                for (byte ixxxxxxx = 0; ixxxxxxx < numberBuy.length; ixxxxxxx++) {
                    if (numberBuy[ixxxxxxx] <= 7) {
                        numberWin.add(ixxxxxxx);
                    }
                }
            } else if (count > 560 && count <= 630) {
                for (byte ixxxxxxxx = 0; ixxxxxxxx < numberBuy.length; ixxxxxxxx++) {
                    if (numberBuy[ixxxxxxxx] <= 8) {
                        numberWin.add(ixxxxxxxx);
                    }
                }
            } else if (count > 630 && count <= 700) {
                for (byte ixxxxxxxxx = 0; ixxxxxxxxx < numberBuy.length; ixxxxxxxxx++) {
                    if (numberBuy[ixxxxxxxxx] <= 9) {
                        numberWin.add(ixxxxxxxxx);
                    }
                }
            }

            if (numberWin.size() > 0) {
                Map.winNumber = numberWin.get(Map.r.nextInt(numberWin.size()));
            } else {
                Map.winNumber = (byte)Map.r.nextInt(100);
            }
        }

        Database.instance.saveEvent(Map.event.getInfo());
        Database.instance.saveLogXoso();
    }
}
