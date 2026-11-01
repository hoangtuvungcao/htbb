package dev.peerova;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Loads the self-contained JNI library from the classpath (fat-jar desktop apps):
 * {@code /peerova-native/<os>-<arch>/<System.mapLibraryName("peerova_jni")>}, extracted into
 * {@code peerova.extract.dir} (default: java.io.tmpdir/peerova-<user>) and loaded with System.load.
 * os = windows | linux | darwin, arch = x86_64 | aarch64 | x86.
 */
final class NativeLoader {
    private NativeLoader() {}

    static String platform() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        String o = os.startsWith("windows") ? "windows" : (os.contains("mac") || os.contains("darwin")) ? "darwin" : "linux";
        String a = (arch.equals("amd64") || arch.equals("x86_64")) ? "x86_64"
                : (arch.equals("aarch64") || arch.equals("arm64")) ? "aarch64"
                : (arch.equals("x86") || arch.matches("i[3-6]86")) ? "x86" : arch;
        return o + "-" + a;
    }

    static boolean loadFromResources() {
        String name = System.mapLibraryName("peerova_jni");
        String res = "/peerova-native/" + platform() + "/" + name;
        InputStream in = Peerova.class.getResourceAsStream(res);
        if (in == null) return false;
        try {
            String dirProp = System.getProperty("peerova.extract.dir");
            File dir = dirProp != null ? new File(dirProp)
                    : new File(System.getProperty("java.io.tmpdir"), "peerova-" + System.getProperty("user.name", "u"));
            if (!dir.isDirectory() && !dir.mkdirs()) return false;
            byte[] data = readAll(in);
            // content-addressed name: a running process may hold an older copy open (Windows locks DLLs)
            File f = new File(dir, Long.toHexString(fnv(data)) + "-" + name);
            if (!f.isFile() || f.length() != data.length) {
                // not File.createTempFile: it seeds SecureRandom, which crashes some JVMs under wine (NetworkInterface)
                File tmp = new File(dir, f.getName() + "." + System.nanoTime() + ".tmp");
                OutputStream out = new FileOutputStream(tmp);
                try {
                    out.write(data);
                } finally {
                    out.close();
                }
                if (!tmp.renameTo(f)) {
                    if (!f.isFile() || f.length() != data.length) {
                        tmp.delete();
                        return false;
                    }
                    tmp.delete();
                }
            }
            System.load(f.getAbsolutePath());
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static byte[] readAll(InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream(1 << 20);
        byte[] buf = new byte[1 << 16];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        return b.toByteArray();
    }

    private static long fnv(byte[] d) {
        long h = 0xcbf29ce484222325L;
        for (byte x : d) {
            h ^= (x & 0xff);
            h *= 0x100000001b3L;
        }
        return h;
    }
}
