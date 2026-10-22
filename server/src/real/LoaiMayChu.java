package real;

import com.htbb.pokemon.GoiLoai;
import com.htbb.pokemon.LoaiDuLieu;
import com.htbb.pokemon.Pokedex;
import io.Message;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lenh -126 POKE_LOAI (com.htbb.pokemon.GoiLoai): sprite tai theo nhu cau, bang loai day du, co Pokedex 1.025 loai.
 *
 * Sprite: goi server/map/loai/NNNN.goi (ban sac mau NNNNs.goi) (thu muc lam viec cua server: map/loai) gui theo khuc 4 KB, moi client toi da
 * loai.sprite_kb_giay KB/giay (thung token, mot luong nen chung), hang doi moi client toi da {@link #TOI_DA_HANG} loai
 * (trung loai bo qua). Lenh la "tai nguyen" cho bo dem chong spam (io.BoDemGoiTin). Kieu la thi bo qua, khong ngat.
 */
public final class LoaiMayChu {

    static final int TOI_DA_HANG = 48;
    static final int TOI_DA_GOI_BYTE = 512 * 1024;
    /** dex*2+shiny -> goi (byte[0] = khong co tep). */
    static final ConcurrentHashMap<Integer, byte[]> GOI = new ConcurrentHashMap<Integer, byte[]>();
    static final ConcurrentHashMap<Char, HangGui> HANG = new ConcurrentHashMap<Char, HangGui>();
    private static volatile ScheduledExecutorService lich;

    private LoaiMayChu() {
    }

    /** Hang gui sprite cua mot client. */
    static final class HangGui {
        final ArrayDeque<int[]> viec = new ArrayDeque<int[]>(); // {dex, shiny, viTri}
        GoiLoai.ThungToken thung;
    }

    public static void xuLy(final Char p, final Message msg) {
        if (p == null || msg == null) {
            return;
        }
        try {
            final int kieu = msg.dis.readUnsignedByte();
            switch (kieu) {
                case GoiLoai.XIN_SPRITE:
                    xinSprite(p, msg.dis.readShort(), msg.dis.readUnsignedByte());
                    break;
                case GoiLoai.XIN_BANG_LOAI:
                    guiBangLoai(p, msg.dis.available() >= 4 ? msg.dis.readInt() : 0);
                    break;
                case GoiLoai.XIN_POKEDEX:
                    guiPokedex(p);
                    break;
                default:
                    break; // kieu la: bo qua
            }
        } catch (final Exception e) {
            // goi hong: bo qua
        }
    }

    // ------------------------------------------------------------------ sprite

    static File thuMucGoi() {
        final String[] thu = {"map/loai", "server/map/loai", "../server/map/loai"};
        for (final String s : thu) {
            final File f = new File(s);
            if (f.isDirectory()) {
                return f;
            }
        }
        return new File("map/loai");
    }

    /** Goi sprite cua loai (null = khong co). */
    static byte[] goi(final int dex, final int shiny) {
        if (dex <= 0 || dex > LoaiDuLieu.SO_LOAI) {
            return null;
        }
        final Integer k = Integer.valueOf(dex * 2 + (shiny != 0 ? 1 : 0));
        byte[] b = GOI.get(k);
        if (b == null) {
            b = new byte[0];
            try {
                // ban sac mau: <NNNN>s.goi (PMDCollab shiny); loai "chua co shiny" -> tong 0, client ve ban thuong
                final File f = new File(thuMucGoi(), GoiLoai.tenGoi(dex, shiny != 0));
                if (f.isFile() && f.length() > 0 && f.length() <= TOI_DA_GOI_BYTE) {
                    final byte[] du = Files.readAllBytes(f.toPath());
                    if (GoiLoai.docGoi(du, dex) != null && GoiLoai.shinyCuaGoi(du) == (shiny != 0 ? 1 : 0)) {
                        b = du;
                    } else {
                        System.out.println("[LOAI] goi hong: " + f);
                    }
                }
            } catch (final Exception e) {
                System.out.println("[LOAI] doc goi " + dex + ": " + e);
            }
            GOI.put(k, b);
        }
        return b.length == 0 ? null : b;
    }

    static void xinSprite(final Char p, final int dex, final int shiny) {
        final byte[] b = goi(dex, shiny);
        if (b == null) {
            gui(p, dex, shiny, null, 0); // tong = 0: client ve bong, khong hoi lai trong phien
            return;
        }
        HangGui h = HANG.get(p);
        if (h == null) {
            h = new HangGui();
            final HangGui cu = HANG.putIfAbsent(p, h);
            if (cu != null) {
                h = cu;
            }
        }
        synchronized (h) {
            for (final int[] v : h.viec) {
                if (v[0] == dex && v[1] == shiny) {
                    return;
                }
            }
            if (h.viec.size() >= TOI_DA_HANG) {
                return;
            }
            h.viec.add(new int[]{dex, shiny, 0});
        }
        damBaoLich();
    }

    static void gui(final Char p, final int dex, final int shiny, final byte[] b, final int viTri) {
        final Message m = new Message(GoiLoai.POKE_LOAI);
        try {
            GoiLoai.ghiKhucSprite(m.dos, dex, shiny, b, viTri);
        } catch (final Exception e) {
            return;
        }
        p.sendMessage(m);
    }

    static long byteGiay() {
        return Math.max(4, LoaiQuaiGame.so("loai.sprite_kb_giay", 64)) * 1024L;
    }

    static void damBaoLich() {
        if (lich != null) {
            return;
        }
        synchronized (LoaiMayChu.class) {
            if (lich != null) {
                return;
            }
            final ScheduledExecutorService l = Executors.newSingleThreadScheduledExecutor((r) -> {
                final Thread t = new Thread(r, "loai-sprite");
                t.setDaemon(true);
                return t;
            });
            l.scheduleWithFixedDelay(LoaiMayChu::nhip, 50, 50, TimeUnit.MILLISECONDS);
            lich = l;
        }
    }

    /** Mot nhip: moi client gui duoc bao nhieu khuc thi gui (trong han muc bang thong). */
    static void nhip() {
        try {
            final long now = System.currentTimeMillis();
            final long bg = byteGiay();
            for (final Iterator<java.util.Map.Entry<Char, HangGui>> it = HANG.entrySet().iterator(); it.hasNext(); ) {
                final java.util.Map.Entry<Char, HangGui> e = it.next();
                final Char p = e.getKey();
                final HangGui h = e.getValue();
                if (p == null || p.exit || p.session == null) {
                    it.remove();
                    continue;
                }
                synchronized (h) {
                    if (h.thung == null) {
                        h.thung = new GoiLoai.ThungToken(bg, now);
                    }
                    int lan = 0;
                    while (!h.viec.isEmpty() && lan++ < 8) {
                        final int[] v = h.viec.peek();
                        final byte[] b = goi(v[0], v[1]);
                        if (b == null) {
                            h.viec.poll();
                            continue;
                        }
                        final int len = Math.min(GoiLoai.KHUC, b.length - v[2]);
                        if (!h.thung.lay((int) Math.min(bg, len + 16), now)) {
                            break;
                        }
                        gui(p, v[0], v[1], b, v[2]);
                        v[2] += len;
                        if (v[2] >= b.length) {
                            h.viec.poll();
                        }
                    }
                    if (h.viec.isEmpty()) {
                        it.remove();
                    }
                }
            }
        } catch (final Throwable e) {
            System.out.println("[LOAI] nhip sprite: " + e);
        }
    }

    // ------------------------------------------------------------------ bang loai + Pokedex

    static void guiBangLoai(final Char p, final int bamClient) {
        final LoaiDuLieu d = LoaiQuaiGame.bang();
        if (d.rong()) {
            return; // may chu cu / CSDL chua co bang: client giu cach cu (-91)
        }
        final Message m = new Message(GoiLoai.POKE_LOAI);
        try {
            if (bamClient != 0 && bamClient == d.bam()) {
                GoiLoai.ghiBangKhongDoi(m.dos, d.bam());
            } else {
                GoiLoai.ghiBangLoai(m.dos, d);
            }
        } catch (final Exception e) {
            return;
        }
        p.sendMessage(m);
    }

    /** Co Pokedex 1.025 loai (chi loai da gap): {dex, co} (com.htbb.pokemon.PokedexDayDu.co). */
    static List<int[]> coPokedex(final Pokedex dx) {
        final List<int[]> ds = new ArrayList<int[]>();
        for (int dex = 1; dex <= LoaiDuLieu.SO_LOAI; dex++) {
            final int co = com.htbb.pokemon.PokedexDayDu.co(dx, dex);
            if (co != 0) {
                ds.add(new int[]{dex, co});
            }
        }
        return ds;
    }

    static void guiPokedex(final Char p) {
        final Pokedex dx = PokedexPokemon.lay(p);
        if (dx == null) {
            return;
        }
        final Message m = new Message(GoiLoai.POKE_LOAI);
        try {
            GoiLoai.ghiPokedex(m.dos, coPokedex(dx));
        } catch (final Exception e) {
            return;
        }
        p.sendMessage(m);
    }
}
