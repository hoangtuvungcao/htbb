package javax.microedition.media;

import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.NenAndroid;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.midlet.MIDlet;

/** Manager: Player phát bằng android.media.MediaPlayer (wav, mp3, ogg, midi...) từ bộ nhớ. */
public final class Manager {
    public static final String TONE_DEVICE_LOCATOR = "device://tone";
    public static final String MIDI_DEVICE_LOCATOR = "device://midi";

    private Manager() {
    }

    public static String[] getSupportedContentTypes(String protocol) {
        return new String[]{"audio/x-wav", "audio/wav", "audio/mpeg", "audio/midi", "audio/x-midi", "audio/ogg", "audio/amr"};
    }

    public static String[] getSupportedProtocols(String type) {
        return new String[]{"file", "resource"};
    }

    public static Player createPlayer(InputStream in, String type) throws IOException, MediaException {
        if (in == null) throw new IllegalArgumentException();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) > 0) bo.write(b, 0, n);
        return new MayPhat(bo.toByteArray(), type);
    }

    public static Player createPlayer(String locator) throws IOException, MediaException {
        if (locator == null) throw new IllegalArgumentException();
        if (locator.startsWith("resource:")) {
            InputStream in = MIDlet.moTaiNguyen(locator.substring(9));
            if (in == null) throw new MediaException(locator);
            return createPlayer(in, null);
        }
        throw new MediaException("Khong ho tro " + locator);
    }

    public static void playTone(int note, int duration, int volume) throws MediaException {
    }

    static final class MayPhat implements Player, VolumeControl {
        private final byte[] duLieu;
        private final String kieu;
        private MediaPlayer mp;
        private int trangThai = UNREALIZED;
        private int lap = 1;
        private int daLap;
        private int muc = 100;
        private boolean tat;
        private final Vector nghe = new Vector();

        MayPhat(byte[] d, String kieu) {
            this.duLieu = d;
            this.kieu = kieu;
        }

        public synchronized void realize() throws MediaException {
            if (trangThai == CLOSED) throw new IllegalStateException();
            if (trangThai >= REALIZED) return;
            try {
                mp = new MediaPlayer();
                mp.setDataSource(new MediaDataSource() {
                    public int readAt(long pos, byte[] buf, int off, int len) {
                        if (pos >= duLieu.length) return -1;
                        int n = (int) Math.min(len, duLieu.length - pos);
                        System.arraycopy(duLieu, (int) pos, buf, off, n);
                        return n;
                    }

                    public long getSize() {
                        return duLieu.length;
                    }

                    public void close() {
                    }
                });
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer m) {
                        boolean lai;
                        synchronized (MayPhat.this) {
                            daLap++;
                            lai = lap < 0 || daLap < lap;
                            if (!lai) trangThai = PREFETCHED;
                        }
                        if (lai) {
                            try {
                                m.seekTo(0);
                                m.start();
                            } catch (Throwable e) {
                                // bỏ qua
                            }
                        } else {
                            bao(PlayerListener.END_OF_MEDIA, null);
                        }
                    }
                });
                mp.prepare();
                apMuc();
                trangThai = PREFETCHED;
            } catch (Throwable e) {
                Log.w(NenAndroid.TAG, "am thanh " + kieu + ": " + e);
                if (mp != null) mp.release();
                mp = null;
                throw new MediaException(e.toString());
            }
        }

        public void prefetch() throws MediaException {
            realize();
        }

        public void start() throws MediaException {
            realize();
            synchronized (this) {
                if (mp == null || trangThai == STARTED) return;
                daLap = 0;
                mp.start();
                trangThai = STARTED;
            }
            bao(PlayerListener.STARTED, null);
        }

        public void stop() {
            synchronized (this) {
                if (mp == null || trangThai != STARTED) return;
                try {
                    mp.pause();
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
                if (mp != null) {
                    try {
                        mp.release();
                    } catch (Throwable e) {
                        // bỏ qua
                    }
                }
                mp = null;
                trangThai = CLOSED;
            }
            bao(PlayerListener.CLOSED, null);
        }

        public synchronized long setMediaTime(long t) {
            if (mp != null) mp.seekTo((int) (t / 1000));
            return t;
        }

        public synchronized long getMediaTime() {
            return mp == null ? TIME_UNKNOWN : mp.getCurrentPosition() * 1000L;
        }

        public synchronized int getState() {
            return trangThai;
        }

        public synchronized long getDuration() {
            return mp == null ? TIME_UNKNOWN : mp.getDuration() * 1000L;
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

        private void bao(String ev, Object d) {
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

        private void apMuc() {
            if (mp == null) return;
            float v = tat ? 0f : muc / 100f;
            mp.setVolume(v, v);
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
            return muc;
        }

        public synchronized int getLevel() {
            return muc;
        }
    }
}
