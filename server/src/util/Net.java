package util;

// SECURITY: outbound HTTP disabled. These helpers previously fetched arbitrary URLs (used to talk to
// third-party teamobi/VTC/mobiplay servers). They now return empty strings and never open a connection,
// so no player data can leave the server through them.
public class Net {
   public static String getHttp(String urlstr) {
      return "";
   }

   public static String getHttpLogin(String urlstr) {
      return "";
   }
}
