package javax.microedition.media;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.midlet.MIDlet;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Manager (MMAPI) bản PC: WAV -> javax.sound.sampled.Clip, MIDI -> javax.sound.midi (Sequencer + bộ tổng hợp
 * mềm Gervill của JRE). Máy không có thiết bị âm thanh (máy chủ, Xvfb): realize ném MediaException như máy
 * J2ME không hỗ trợ - game (SoundMm) tự tắt tiếng, không lỗi.
 * Âm lượng tổng (cài đặt PC): nhạc (MIDI) và hiệu ứng (WAV) nhân thêm mucNhac / mucHieuUng (0..100).
 */
public final class Manager {
    public static final String TONE_DEVICE_LOCATOR = "device://tone";
    public static final String MIDI_DEVICE_LOCATOR = "device://midi";

    /** âm lượng tổng của cài đặt PC (0..100) */
    public static volatile int mucNhac = 80;
    public static volatile int mucHieuUng = 90;
    /** đếm lỗi thiết bị (để lớp PC báo "không có âm thanh") */
    public static volatile int soLoiThietBi;
    public static volatile String loiCuoi;

    private static final Vector DANG_MO = new Vector();

    private Manager() {
    }

    public static String[] getSupportedContentTypes(String protocol) {
        return new String[]{"audio/x-wav", "audio/wav", "audio/midi", "audio/x-midi"};
    }

    public static String[] getSupportedProtocols(String type) {
        return new String[]{"resource"};
    }

    public static Player createPlayer(InputStream in, String type) throws IOException, MediaException {
        if (in == null) throw new IllegalArgumentException();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) > 0) bo.write(b, 0, n);
        byte[] d = bo.toByteArray();
        boolean midi = (type != null && type.indexOf("mid") >= 0)
                || (d.length > 4 && d[0] == 'M' && d[1] == 'T' && d[2] == 'h' && d[3] == 'd');
        return midi ? (Player) new MayMidi(d, type) : new MayWav(d, type);
    }

    public static Player createPlayer(String locator) throws IOException, MediaException {
        if (locator == null) throw new IllegalArgumentException();
        if (locator.startsWith("resource:")) {
            InputStream in = MIDlet.moTaiNguyen(locator.substring(9));
            if (in == null) throw new MediaException(locator);
            return createPlayer(in, locator.endsWith(".mid") ? "audio/midi" : null);
        }
        throw new MediaException("Khong ho tro " + locator);
    }

    public static void playTone(int note, int duration, int volume) throws MediaException {
    }

    /** Áp lại âm lượng tổng cho mọi Player đang mở (đổi trong cài đặt). */
    public static void apAmLuongTong() {
        Object[] a = DANG_MO.toArray();
        for (int i = 0; i < a.length; i++) {
            try {
                ((Goc) a[i]).apMuc();
            } catch (Throwable e) {
                // bỏ qua
            }
        }
    }

    /** Đóng mọi Player (thoát game). */
    public static void dongHet() {
        Object[] a = DANG_MO.toArray();
        for (int i = 0; i < a.length; i++) {
            try {
                ((Player) a[i]).close();
            } catch (Throwable e) {
                // bỏ qua
            }
        }
    }

    private static void baoLoi(Throwable e) {
        soLoiThietBi++;
        loiCuoi = String.valueOf(e);
    }

    /** Phần chung: trạng thái, lặp, nghe, âm lượng. */
    abstract static class Goc implements Player, VolumeControl {
        final byte[] duLieu;
        final String kieu;
        int trangThai = UNREALIZED;
        int lap = 1;
        int daLap;
        int muc = 100;
        boolean tat;
        private final Vector nghe = new Vector();

        Goc(byte[] d, String kieu) {
            this.duLieu = d;
            this.kieu = kieu;
        }

        abstract void moThietBi() throws Exception;

        abstract void batDauPhat();

        abstract void dungPhat();

        abstract void dongThietBi();

        abstract void apMuc();

        public synchronized void realize() throws MediaException {
            if (trangThai == CLOSED) throw new IllegalStateException();
            if (trangThai >= REALIZED) return;
            try {
                moThietBi();
                trangThai = PREFETCHED;
                DANG_MO.addElement(this);
                apMuc();
            } catch (Throwable e) {
                baoLoi(e);
                dongThietBi();
                throw new MediaException(String.valueOf(e));
            }
        }

        public void prefetch() throws MediaException {
            realize();
        }

        public void start() throws MediaException {
            realize();
            synchronized (this) {
                if (trangThai == STARTED) return;
                daLap = 0;
                batDauPhat();
                trangThai = STARTED;
            }
            bao(PlayerListener.STARTED, null);
        }

        public void stop() {
            synchronized (this) {
                if (trangThai != STARTED) return;
                try {
                    dungPhat();
                } catch (Throwable e) {
                    // bỏ qua
                }
                trangThai = PREFETCHED;
            }
            bao(PlayerListener.STOPPED, null);
        }

        public synchronized void deallocate() {
        }

        public void close() {
            synchronized (this) {
                if (trangThai == CLOSED) return;
                try {
                    dongThietBi();
                } catch (Throwable e) {
                    // bỏ qua
                }
                trangThai = CLOSED;
                DANG_MO.removeElement(this);
            }
            bao(PlayerListener.CLOSED, null);
        }

        /** Phát hết một lượt: lặp lại hoặc báo END_OF_MEDIA. */
        void hetLuot() {
            boolean lai;
            synchronized (this) {
                if (trangThai != STARTED) return;
                daLap++;
                lai = lap < 0 || daLap < lap;
                if (!lai) trangThai = PREFETCHED;
            }
            if (lai) {
                try {
                    batDauPhat();
                } catch (Throwable e) {
                    // bỏ qua
                }
            } else {
                bao(PlayerListener.END_OF_MEDIA, null);
            }
        }

        public synchronized int getState() {
            return trangThai;
        }

        public String getContentType() {
            return kieu;
        }

        public synchronized void setLoopCount(int c) {
            if (c == 0) throw new IllegalArgumentException();
            lap = c;
        }

        public void addPlayerListener(PlayerListener l) {
            if (l != null) nghe.addElement(l);
        }

        public void removePlayerListener(PlayerListener l) {
            nghe.removeElement(l);
        }

        void bao(String ev, Object d) {
            for (int i = 0; i < nghe.size(); i++) {
                try {
                    ((PlayerListener) nghe.elementAt(i)).playerUpdate(this, ev, d);
                } catch (Throwable e) {
                    // bỏ qua
                }
            }
        }

        public Control[] getControls() {
            return new Control[]{this};
        }

        public Control getControl(String t) {
            if (t != null && (t.endsWith("VolumeControl"))) return this;
            return null;
        }

        public synchronized void setMute(boolean m) {
            tat = m;
            apMuc();
        }

        public synchronized boolean isMuted() {
            return tat;
        }

        public synchronized int setLevel(int l) {
            muc = Math.max(0, Math.min(100, l));
            apMuc();
            bao(PlayerListener.VOLUME_CHANGED, this);
            return muc;
        }

        public synchronized int getLevel() {
            return muc;
        }
    }

    /** WAV = Clip (nạp cả tiếng vào bộ nhớ, phát lại nhanh). */
    static final class MayWav extends Goc {
        private Clip clip;

        MayWav(byte[] d, String kieu) {
            super(d, kieu);
        }

        void moThietBi() throws Exception {
            AudioInputStream ai = AudioSystem.getAudioInputStream(new ByteArrayInputStream(duLieu));
            Clip c = AudioSystem.getClip();
            c.open(ai);
            c.addLineListener(new LineListener() {
                public void update(LineEvent ev) {
                    if (ev.getType() == LineEvent.Type.STOP && clip != null
                            && clip.getFramePosition() >= clip.getFrameLength() - 1) {
                        hetLuot();
                    }
                }
            });
            clip = c;
        }

        void batDauPhat() {
            if (clip == null) return;
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }

        void dungPhat() {
            if (clip != null) clip.stop();
        }

        void dongThietBi() {
            if (clip != null) {
                try {
                    clip.close();
                } catch (Throwable e) {
                    // bỏ qua
                }
            }
            clip = null;
        }

        void apMuc() {
            Clip c = clip;
            if (c == null) return;
            float v = tat ? 0f : muc / 100f * mucHieuUng / 100f;
            try {
                FloatControl fc = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                float db = v <= 0.0001f ? fc.getMinimum() : (float) (20.0 * Math.log10(v));
                fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), db)));
            } catch (Throwable e) {
                // thiết bị không có điều khiển âm lượng
            }
        }

        public synchronized long setMediaTime(long t) {
            if (clip != null) clip.setMicrosecondPosition(t);
            return t;
        }

        public synchronized long getMediaTime() {
            return clip == null ? TIME_UNKNOWN : clip.getMicrosecondPosition();
        }

        public synchronized long getDuration() {
            return clip == null ? TIME_UNKNOWN : clip.getMicrosecondLength();
        }
    }

    // ------------------------------------------------------------------ MIDI: 2 "làn" tổng hợp + chuyển bài mượt
    /**
     * Một bộ tổng hợp mềm (Gervill) đã nạp soundbank chất lượng (TimGM6mb.sf2 đi kèm bản PC nếu có, không thì
     * soundbank mặc định của JRE). Có 2 làn để bài mới nhỏ dần vào trong lúc bài cũ nhỏ dần ra (crossfade).
     */
    static final class Lan {
        Synthesizer synth;
        javax.sound.midi.Receiver nhan;
        MayMidi chu;
        float muc;           // 0..1 hiện tại (đang mờ dần)
    }

    private static final Lan[] LAN = new Lan[2];
    private static boolean daMoLan;

    private static javax.sound.midi.Soundbank soundbank() {
        String[] thu = {System.getProperty("htbb.soundbank"), null};
        try {
            java.io.File jar = new java.io.File(Manager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            thu[1] = new java.io.File(jar.getParentFile(), "am/TimGM6mb.sf2").getPath();
        } catch (Throwable e) {
            // bỏ qua
        }
        for (int i = 0; i < thu.length; i++) {
            if (thu[i] == null) continue;
            java.io.File f = new java.io.File(thu[i]);
            if (!f.isFile()) continue;
            try {
                return MidiSystem.getSoundbank(f);
            } catch (Throwable e) {
                // thử tiếp
            }
        }
        return null;
    }

    private static synchronized void moLan() throws Exception {
        if (daMoLan) {
            if (LAN[0] == null) throw new MediaException("khong co bo tong hop MIDI");
            return;
        }
        daMoLan = true;
        javax.sound.midi.Soundbank sb = soundbank();
        for (int i = 0; i < LAN.length; i++) {
            Synthesizer sy = MidiSystem.getSynthesizer();
            sy.open();
            if (sb != null && sy.isSoundbankSupported(sb)) {
                sy.unloadAllInstruments(sy.getDefaultSoundbank());
                sy.loadAllInstruments(sb);
            }
            Lan l = new Lan();
            l.synth = sy;
            l.nhan = sy.getReceiver();
            LAN[i] = l;
        }
        System.err.println("HTBB: MIDI " + (sb != null ? "soundbank " + sb.getName() : "soundbank mac dinh JRE"));
    }

    /** Âm lượng tổng (Universal SysEx master volume) của một làn, 0..1. */
    static void datMucLan(Lan l, float v) {
        l.muc = v;
        int m = Math.max(0, Math.min(16383, (int) (16383 * v)));
        try {
            byte[] d = {(byte) 0xF0, 0x7F, 0x7F, 0x04, 0x01, (byte) (m & 0x7F), (byte) ((m >> 7) & 0x7F), (byte) 0xF7};
            javax.sound.midi.SysexMessage sx = new javax.sound.midi.SysexMessage();
            sx.setMessage(d, d.length);
            l.nhan.send(sx, -1);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    /** Mờ dần âm lượng làn từ hiện tại -> dich trong ms; xong gọi sau (có thể null). */
    static void moDan(final Lan l, final float dich, final int ms, final Runnable sau) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                float tu = l.muc;
                long t0 = System.currentTimeMillis();
                while (true) {
                    float k = Math.min(1f, (System.currentTimeMillis() - t0) / (float) ms);
                    datMucLan(l, tu + (dich - tu) * k);
                    if (k >= 1f) break;
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (sau != null) sau.run();
            }
        }, "HTBB-mo-dan");
        t.setDaemon(true);
        t.start();
    }

    /** MIDI = Sequencer -> làn tổng hợp; bài mới vào nhỏ dần lên, bài cũ dừng thì nhỏ dần ra (chuyển bài mượt). */
    static final class MayMidi extends Goc {
        private Sequencer seq;
        private Lan lan;

        MayMidi(byte[] d, String kieu) {
            super(d, kieu);
        }

        void moThietBi() throws Exception {
            moLan();
            Sequence s = MidiSystem.getSequence(new ByteArrayInputStream(duLieu));
            Sequencer q = MidiSystem.getSequencer(false);
            q.open();
            q.setSequence(s);
            q.addMetaEventListener(new MetaEventListener() {
                public void meta(MetaMessage m) {
                    if (m.getType() == 47) { // hết bài
                        hetLuot();
                    }
                }
            });
            seq = q;
        }

        private float mucDich() {
            return tat ? 0f : muc / 100f * mucNhac / 100f;
        }

        void batDauPhat() {
            if (seq == null) return;
            if (lan == null) {
                synchronized (Manager.class) {
                    Lan chon = null;
                    for (int i = 0; i < LAN.length; i++) {
                        if (LAN[i] != null && LAN[i].chu == null) {
                            chon = LAN[i];
                            break;
                        }
                    }
                    if (chon == null) chon = LAN[0];
                    if (chon.chu != null && chon.chu != this) chon.chu.nhaLan();
                    chon.chu = this;
                    lan = chon;
                }
                try {
                    seq.getTransmitter().setReceiver(lan.nhan);
                } catch (Throwable e) {
                    return;
                }
                datMucLan(lan, 0f);
                moDan(lan, mucDich(), 900, null);
            }
            seq.setTickPosition(0);
            seq.start();
        }

        /** Làn bị bài khác lấy: dừng ngay. */
        synchronized void nhaLan() {
            try {
                if (seq != null) seq.stop();
            } catch (Throwable e) {
                // bỏ qua
            }
            lan = null;
        }

        void dungPhat() {
            final Lan l = lan;
            final Sequencer q = seq;
            if (q == null) return;
            if (l == null) {
                q.stop();
                return;
            }
            lan = null;
            moDan(l, 0f, 650, new Runnable() {
                public void run() {
                    try {
                        q.stop();
                        // ngắt nốt đang ngân
                        javax.sound.midi.ShortMessage m = new javax.sound.midi.ShortMessage();
                        for (int c = 0; c < 16; c++) {
                            m.setMessage(javax.sound.midi.ShortMessage.CONTROL_CHANGE, c, 123, 0);
                            l.nhan.send(m, -1);
                        }
                    } catch (Throwable e) {
                        // bỏ qua
                    }
                    synchronized (Manager.class) {
                        if (l.chu == MayMidi.this) l.chu = null;
                    }
                }
            });
        }

        void dongThietBi() {
            if (seq != null) {
                if (lan != null) dungPhat();
                final Sequencer q = seq;
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(800);
                            q.close();
                        } catch (Throwable e) {
                            // bỏ qua
                        }
                    }
                }, "HTBB-dong-midi");
                t.setDaemon(true);
                t.start();
            }
            seq = null;
        }

        void apMuc() {
            Lan l = lan;
            if (l != null) datMucLan(l, mucDich());
        }

        public synchronized long setMediaTime(long t) {
            if (seq != null) seq.setMicrosecondPosition(t);
            return t;
        }

        public synchronized long getMediaTime() {
            return seq == null ? TIME_UNKNOWN : seq.getMicrosecondPosition();
        }

        public synchronized long getDuration() {
            return seq == null ? TIME_UNKNOWN : seq.getMicrosecondLength();
        }
    }
}
