/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop va hanh vi; package da doi sang com.htbb.log.nguoidung (bo ten game cu). Nguon goc:
 * docs/giay-phep.md.
 */
package com.htbb.log.nguoidung;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Net {
    public static String getHttp(String urlstr) {
        String text = null;

        try {
            URL url = new URL(urlstr);
            InputStream inputstream = url.openStream();
            ByteArrayOutputStream ba = new ByteArrayOutputStream();

            while (true) {
                byte[] b = new byte[1];
                int x = inputstream.read(b);
                if (x == -1) {
                    text = new String(ba.toByteArray(), "UTF-8");
                    break;
                }

                ba.write(b);
            }
        } catch (Exception var7) {
        }

        return text;
    }

    public static int getHttpAsync(String urlstr) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection)new URL(urlstr).openConnection();
            urlConnection.getResponseCode();
            Thread.sleep(100L);
            urlConnection.disconnect();
            return 0;
        } catch (Exception var2) {
            return -1;
        }
    }
}
