/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package util;

import data.Database;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import server.TeamServer;

public class Logger implements Runnable {
    public static int TYPE_LOGIN = 1;
    public static int TYPE_LOGOUT = 2;
    public static int TYPE_CHAT = 3;
    public static int TYPE_TRANSFER_MONEY = 4;
    public static int TYPE_BUY_AVATAR = 5;
    public static int TYPE_UPDATE_MONEY = 6;
    public static int TYPE_NAP_MONEY = 7;
    private static String[] TYPE_NAMES = new String[]{"", "[LOGIN]", "[LOGOUT]", "[CHAT]", "[TRANSFER MONEY]", "[BUY AVATAR]", "[UPDATE MONEY]", "[NAP MONEY]"};
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    private static SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
    private static Logger hnd;
    private static List<Logger.LogItem> list;
    public static boolean exit;

    private Logger() {
        hnd = this;
        list = new Vector<>(1000);
        new Thread(this).start();
    }

    public static Logger getInstance() {
        if (hnd == null) {
            new Logger();
        }

        return hnd;
    }

    public static String toDateString(long datetime) {
        return dateFormat.format(new Date(datetime));
    }

    public static void log(int type, String username, String value, String text) {
        if (hnd == null) {
            new Logger();
        }

        Logger.LogItem log = hnd.new LogItem(type, username, value, text);
        list.add(log);
    }

    public static void writeToDB() {
        if (list != null && list.size() > 0) {
            try {
                Connection conn = Database.instance.getConnection();

                while (list.size() > 0) {
                    Logger.LogItem log = list.remove(0);

                    try {
                        Statement stmt = conn.createStatement();
                        String sql = "INSERT INTO board_log(logtime,type,username,value,text) VALUES('"
                            + dateFormat.format(new Date(log.time))
                            + "',"
                            + log.type
                            + ",'"
                            + log.username
                            + "','"
                            + log.value
                            + "','"
                            + log.text
                            + "')";
                        stmt.execute(sql);
                        stmt.close();

                        try {
                            stmt.close();
                        } catch (Exception var15) {
                        }
                    } catch (SQLException var16) {
                        var16.printStackTrace();
                    } finally {
                        try {
                            conn.close();
                        } catch (Exception var14) {
                        }
                    }
                }
            } catch (SQLException var18) {
                var18.printStackTrace();
            }
        }
    }

    public static void writeToFile() {
        String st = "\t";
        if (list != null && list.size() > 0) {
            try {
                String filename = dateFormat1.format(new Date(list.get(0).time)) + ".txt";
                FileWriter outFile = new FileWriter("serverlog/" + filename, true);
                PrintWriter out = new PrintWriter(outFile);

                while (list.size() > 0) {
                    Logger.LogItem log = list.remove(0);

                    try {
                        out.print(TYPE_NAMES[log.type]);
                        out.print(st);
                        out.print(dateFormat.format(new Date(log.time)));
                        out.print(st);
                        out.print(log.username);
                        out.print(st);
                        out.print(log.value);
                        out.print(st);
                        out.print(log.text);
                        out.println();
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }
                }

                out.flush();
                out.close();
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }
    }

    public static void logError(Exception e) {
        try {
            FileWriter outFile = new FileWriter("errorlog.txt", true);
            PrintWriter out = new PrintWriter(outFile);
            out.println(dateFormat.format(new Date()));
            e.printStackTrace(out);
            out.println();
            out.flush();
            out.close();
        } catch (IOException var3) {
            var3.printStackTrace();
        }
    }

    public static void logError(Exception e, String fileName) {
        try {
            FileWriter outFile = new FileWriter(fileName, true);
            PrintWriter out = new PrintWriter(outFile);
            out.println(dateFormat.format(new Date()));
            e.printStackTrace(out);
            out.println();
            out.flush();
            out.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }
    }

    public static void logStringWithDate(String st, String fileName) {
        try {
            FileWriter outFile = new FileWriter(fileName, true);
            PrintWriter out = new PrintWriter(outFile);
            out.println(dateFormat.format(new Date()));
            out.print(st);
            out.println();
            out.flush();
            out.close();
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    public static void logString(String st, String fileName) {
        try {
            FileWriter outFile = new FileWriter(fileName, true);
            PrintWriter out = new PrintWriter(outFile);
            out.print(st);
            out.println();
            out.flush();
            out.close();
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (TeamServer.running) {
            writeToFile();

            try {
                Thread.sleep(60000L);
            } catch (InterruptedException var2) {
            }
        }
    }

    private class LogItem {
        public long time = System.currentTimeMillis();
        public int type;
        public String username;
        public String value;
        public String text;

        public LogItem(int type, String username, String value, String text) {
            this.type = type;
            this.username = username;
            this.value = value;
            this.text = text;
        }
    }
}
