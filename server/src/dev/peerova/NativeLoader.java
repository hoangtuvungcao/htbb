package dev.peerova;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Properties;

/**
 * Loads the native code behind {@link Peerova}.
 *
 * <ol>
 * <li>System properties {@code peerova.lib.path} / {@code peerova.jni.path} (absolute paths) win.</li>
 * <li>Desktop {@code peerova.jar}: if it embeds a native library for this OS/arch
 *     ({@code dev/peerova/native/<platform>/}, listed with its SHA-256 in {@code natives.properties}), the file
 *     is extracted to a per-user cache directory, versioned by release and hash
 *     ({@code <cache>/<version>-<sha256 prefix>/<platform>/}), verified, then loaded with {@link System#load}.
 *     Nothing is written to the working directory. Disable with {@code -Dpeerova.native.extract=false}.</li>
 * <li>Otherwise (Android, other platforms, extraction failed): {@link System#loadLibrary} of {@code peerova}
 *     and {@code peerova_jni} from {@code java.library.path} / the APK.</li>
 * </ol>
 *
 * Cache directory: {@code -Dpeerova.cache.dir=...}, else {@code %LOCALAPPDATA%\Peerova\java} (Windows),
 * {@code ~/Library/Caches/Peerova/java} (macOS), {@code $XDG_CACHE_HOME/peerova/java} or
 * {@code ~/.cache/peerova/java} (Linux); if that is not writable, {@code <java.io.tmpdir>/peerova-<user>/java}
 * (only used when the directory is owned by the current user). Windows 7 / 8.1 / Server 2012 get the legacy build
 * automatically; force it with {@code -Dpeerova.windows.legacy=true|false}.
 */
final class NativeLoader {
    private static final String RES = "/dev/peerova/native/";

    private NativeLoader() {}

    static void load() {
        String lib = System.getProperty("peerova.lib.path");
        String jni = System.getProperty("peerova.jni.path");
        if (lib != null || jni != null) {
            loadFromPath(lib, jni);
            return;
        }
        Throwable embedded = null;
        if (!isAndroid() && !"false".equalsIgnoreCase(System.getProperty("peerova.native.extract"))) {
            try {
                if (loadEmbedded()) return;
            } catch (Throwable t) {
                embedded = t; // fall back to java.library.path below
            }
        }
        try {
            loadFromPath(null, null);
        } catch (UnsatisfiedLinkError e) {
            if (embedded == null) throw e;
            UnsatisfiedLinkError u = new UnsatisfiedLinkError(e.getMessage() + " (and the native library embedded in peerova.jar could not be used: " + embedded + ")");
            u.initCause(embedded);
            throw u;
        }
    }

    /** Two-library layout: libpeerova (core) + libpeerova_jni (glue). */
    private static void loadFromPath(String lib, String jni) {
        // Load libpeerova first: old Android linkers do not resolve dependencies from the app's lib dir.
        try {
            if (lib != null) System.load(lib); else System.loadLibrary("peerova");
        } catch (UnsatisfiedLinkError ignored) {
            // Fine on desktop: libpeerova_jni then finds libpeerova via rpath / LD_LIBRARY_PATH / PATH.
        }
        if (jni != null) System.load(jni); else System.loadLibrary("peerova_jni");
    }

    private static boolean isAndroid() {
        String vm = System.getProperty("java.vm.name", "");
        String vendor = System.getProperty("java.vendor", "");
        return vm.toLowerCase(Locale.ROOT).contains("dalvik") || vendor.toLowerCase(Locale.ROOT).contains("android");
    }

    /** e.g. "linux-x86_64", "windows-x86_64"; null if unknown. */
    static String platform() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        String o = os.startsWith("windows") ? "windows" : os.startsWith("linux") ? "linux"
                : os.startsWith("mac") || os.startsWith("darwin") ? "macos" : null;
        String a = arch.equals("amd64") || arch.equals("x86_64") || arch.equals("x64") ? "x86_64"
                : arch.equals("aarch64") || arch.equals("arm64") ? "aarch64" : null;
        return o == null || a == null ? null : o + "-" + a;
    }

    /** Windows before 10 (7, 8, 8.1, Server 2008 R2 / 2012 / 2012 R2) needs the legacy build. */
    static boolean windowsLegacy() {
        String forced = System.getProperty("peerova.windows.legacy");
        if (forced != null) return Boolean.parseBoolean(forced);
        String v = System.getProperty("os.version", "");
        try {
            int dot = v.indexOf('.');
            return Integer.parseInt(dot < 0 ? v : v.substring(0, dot)) < 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean loadEmbedded() throws IOException {
        Properties idx = new Properties();
        InputStream in = NativeLoader.class.getResourceAsStream(RES + "natives.properties");
        if (in == null) return false; // jar without natives (e.g. Android AAR build)
        try { idx.load(in); } finally { in.close(); }
        String plat = platform();
        if (plat == null) return false;
        if (plat.startsWith("windows-") && windowsLegacy() && idx.getProperty(plat + "-legacy.file") != null) plat += "-legacy";
        String file = idx.getProperty(plat + ".file");
        String sha = idx.getProperty(plat + ".sha256");
        if (file == null || sha == null || sha.length() != 64 || file.contains("/") || file.contains("\\")) return false;
        sha = sha.toLowerCase(Locale.ROOT);
        String version = idx.getProperty("version", "0").replaceAll("[^A-Za-z0-9._-]", "_");
        String sub = version + "-" + sha.substring(0, 16) + File.separator + plat;

        IOException last = null;
        for (File root : cacheRoots()) {
            try {
                File dir = new File(root, sub);
                File target = new File(dir, file);
                mkdirsPrivate(root, dir);
                if (!(target.isFile() && sha.equals(sha256(target)))) extract(RES + plat + "/" + file, dir, target, sha);
                // Re-check right before loading: never load bytes that do not match the index.
                if (!sha.equals(sha256(target))) throw new IOException("SHA-256 mismatch for " + target);
                System.load(target.getAbsolutePath());
                return true;
            } catch (IOException e) {
                last = e;
            }
        }
        throw last != null ? last : new IOException("no usable cache directory");
    }

    private static File[] cacheRoots() {
        String forced = System.getProperty("peerova.cache.dir");
        if (forced != null && !forced.isEmpty()) return new File[] {new File(forced)};
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home", "");
        File primary;
        if (os.startsWith("windows")) {
            String local = System.getenv("LOCALAPPDATA");
            primary = local != null && !local.isEmpty() ? new File(local, "Peerova" + File.separator + "java")
                    : new File(home, "AppData" + File.separator + "Local" + File.separator + "Peerova" + File.separator + "java");
        } else if (os.startsWith("mac")) {
            primary = new File(home, "Library/Caches/Peerova/java");
        } else {
            String xdg = System.getenv("XDG_CACHE_HOME");
            primary = xdg != null && xdg.startsWith("/") ? new File(xdg, "peerova/java") : new File(home, ".cache/peerova/java");
        }
        String user = System.getProperty("user.name", "user").replaceAll("[^A-Za-z0-9._-]", "_");
        File tmp = new File(System.getProperty("java.io.tmpdir"), "peerova-" + user);
        return new File[] {primary, tmp};
    }

    /** Writes the resource to a temp file in {@code dir}, verifies it, then renames it to {@code target}. */
    private static void extract(String res, File dir, File target, String sha) throws IOException {
        InputStream in = NativeLoader.class.getResourceAsStream(res);
        if (in == null) throw new IOException("missing resource " + res);
        File tmp = newFile(dir, ".tmp");
        try {
            MessageDigest md = sha256();
            OutputStream out = new FileOutputStream(tmp);
            try {
                byte[] buf = new byte[1 << 16];
                for (int n; (n = in.read(buf)) > 0; ) {
                    md.update(buf, 0, n);
                    out.write(buf, 0, n);
                }
            } finally {
                out.close();
                in.close();
            }
            if (!sha.equals(hex(md.digest()))) throw new IOException("embedded native library is corrupted (SHA-256 mismatch)");
            tmp.setReadable(false, false);
            tmp.setReadable(true, true);
            tmp.setWritable(false, false);
            tmp.setWritable(true, true);
            tmp.setExecutable(false, false);
            tmp.setExecutable(true, true);
            if (target.exists() && !sha.equals(sha256(target))) target.delete(); // stale / damaged copy
            if (!tmp.renameTo(target) && !(target.isFile() && sha.equals(sha256(target))))
                throw new IOException("cannot create " + target); // a lost race is fine when the winner's copy is good
        } finally {
            if (tmp.exists()) tmp.delete();
        }
    }

    /**
     * Creates {@code dir} below {@code root}. Every directory from {@code root} down to {@code dir} must be owned
     * by the current user and is made owner-only (0700 on POSIX), so nobody else can swap the file between the
     * hash check and the load.
     */
    private static void mkdirsPrivate(File root, File dir) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs() && !dir.isDirectory()) throw new IOException("cannot create " + dir);
        File probe = newFile(dir, ".probe"); // a file we certainly own
        try {
            for (File d = dir; d != null; d = d.getParentFile()) {
                d.setReadable(false, false);
                d.setReadable(true, true);
                d.setWritable(false, false);
                d.setWritable(true, true);
                d.setExecutable(false, false);
                d.setExecutable(true, true);
                Owner.check(d, probe);
                if (d.equals(root)) break;
            }
        } finally {
            probe.delete();
        }
    }

    /** Refuses directories owned by another user (e.g. pre-created in a shared temp dir). */
    private static final class Owner {
        static void check(File dir, File mine) throws IOException {
            // Windows: %LOCALAPPDATA% and %TEMP% are per-user (ACLs); elevated processes create files owned by
            // Administrators, so an owner comparison would give false alarms there.
            if (File.separatorChar == '\\') return;
            Object me, owner;
            try {
                me = java.nio.file.Files.getOwner(mine.toPath());
                owner = java.nio.file.Files.getOwner(dir.toPath());
            } catch (UnsupportedOperationException | NoClassDefFoundError | SecurityException e) {
                return; // no owner information on this platform
            }
            if (!me.equals(owner)) throw new IOException("cache directory " + dir + " is owned by " + owner + ", not " + me);
        }
    }

    private static int seq;

    /**
     * Creates a new, unique empty file in {@code dir} (exclusive create, never reuses or follows an existing
     * entry). Unlike {@link File#createTempFile} it needs no SecureRandom, so class loading never waits for
     * entropy seeding.
     */
    private static File newFile(File dir, String suffix) throws IOException {
        for (int i = 0; i < 100; i++) {
            int n;
            synchronized (NativeLoader.class) { n = ++seq; }
            File f = new File(dir, "peerova-" + Long.toHexString(System.nanoTime()) + "-"
                    + Integer.toHexString(System.identityHashCode(new Object())) + "-" + n + suffix);
            if (f.createNewFile()) return f;
        }
        throw new IOException("cannot create a file in " + dir);
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String sha256(File f) throws IOException {
        MessageDigest md = sha256();
        InputStream in = new FileInputStream(f);
        try {
            byte[] buf = new byte[1 << 16];
            for (int n; (n = in.read(buf)) > 0; ) md.update(buf, 0, n);
        } finally {
            in.close();
        }
        return hex(md.digest());
    }

    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(Character.forDigit((x >> 4) & 15, 16)).append(Character.forDigit(x & 15, 16));
        return sb.toString();
    }
}
