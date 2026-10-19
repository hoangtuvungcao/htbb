/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class UserLogger {
    public static String PATH = "E:/workspaceNew/log/";
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    public static DateFormat dateYearFormat = new SimpleDateFormat("yyyy");
    public static DateFormat dateMonthFormat = new SimpleDateFormat("MM");
    public static DateFormat dateDayFormat = new SimpleDateFormat("dd");
    private static Hashtable<String, Vector<UserLogger.LogItem>> playerLogs = new Hashtable<>();
    static UserLogger instance = new UserLogger();
    public static String[][] temp = new String[][]{
        {"aáàảãạăắằẳẵặâấầẩẫậ", "éèẻẽẹêếềểễệe", "íìỉĩịi", "óòỏõọôốồổỗộơớờởỡợo", "úùủũụưứừửữựu", "ýỳỷỹỵ", "đ"}, {"a", "e", "i", "o", "u", "y", "d"}
    };

    public static synchronized void addLog(String username, String text, String aclog) {
    }

    public static synchronized void flushToFile(String username) {
    }

    public static String switch2UnUTF(String source) {
        source = source.toLowerCase();
        String result = "";

        for (int i = 0; i < source.length(); i++) {
            String st = String.valueOf(source.charAt(i));

            for (int j = 0; j < temp[0].length; j++) {
                if (temp[0][j].indexOf(st) != -1) {
                    st = temp[1][j];
                    break;
                }
            }

            result = result + st;
        }

        return result;
    }

    public class LogItem {
        Date logTime;
        String text;
        String charname = "";
        String aclog = "";

        public LogItem(String text, String charname, String aclog) {
            this.text = UserLogger.switch2UnUTF(text);
            this.charname = charname;
            this.aclog = UserLogger.switch2UnUTF(aclog);
            this.logTime = new Date();
        }
    }
}
