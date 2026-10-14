package game.render;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.SoundPool;
import com.teamobi.kpah.GameMidlet;
import game.model.Cout;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class Sound {
    public static final byte LONG = 1;
    private static final int MAX_VOLUME = 10;
    public static final int MBClick = 10;
    public static final int MBoss_injure = 17;
    public static final int MBua1 = 3;
    public static final int MBua2 = 4;
    public static final int MChienBinh1 = 5;
    public static final int MChienBinh2 = 6;
    public static final int MCung_1 = 7;
    public static final int MCung_2 = 8;
    public static final int MKiem_1 = 9;
    public static final int MKiem_2 = 10;
    public static final int MLogin = 0;
    public static final int MPs_1 = 11;
    public static final int MPs_2 = 12;
    public static final int MPs_3 = 13;
    public static final int MRun = 14;
    public static final int MRunOnPet = 15;
    public static final int MSelectChar = 16;
    public static final int MSonNam = 1;
    public static final byte SHORT = 0;
    public static Sound gI = null;
    public static boolean isSound = true;
    public static MediaPlayer[] music;
    public static SoundPool[] sound;
    public static int[] soundID;
    public static int volume;

    public static int getSeason() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        int i = calendar.get(5);
        int i2 = calendar.get(2) + 1;
        int i3 = calendar.get(1);
        if (i3 == 2013 && i2 == 12 && i <= 26) {
            return 3;
        }
        if (i3 != 2014) {
            return 0;
        }
        if (i2 == 1) {
            return i >= 25 ? 2 : 0;
        }
        return (i2 != 2 || i > 10) ? 0 : 2;
    }

    public static void init(Context context) {
        music = new MediaPlayer[2];
        getSeason();
        String[] strArr = {"0", "2"};
        int i = 0;
        while (true) {
            MediaPlayer[] mediaPlayerArr = music;
            if (i >= mediaPlayerArr.length) {
                break;
            }
            mediaPlayerArr[i] = getMediaSoundFile(strArr[i] + ".mp3");
            i++;
        }
        sound = new SoundPool[1];
        soundID = new int[1];
        int[] iArr = {10};
        int i2 = 0;
        while (true) {
            SoundPool[] soundPoolArr = sound;
            if (i2 >= soundPoolArr.length) {
                return;
            }
            soundPoolArr[i2] = new SoundPool(4, 3, 0);
            sound[i2].setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i3, int i4) {
                }
            });
            soundID[i2] = getSoundPoolSource(i2, iArr[i2] + ".mp3");
            i2++;
        }
    }

    public static int getSoundPoolSource(int i, String str) {
        try {
            AssetFileDescriptor assetFileDescriptorOpenFd = GameMidlet.asset.openFd("music/" + str);
            return sound[i].load(assetFileDescriptorOpenFd.getFileDescriptor(), assetFileDescriptorOpenFd.getStartOffset(), assetFileDescriptorOpenFd.getLength(), 0);
        } catch (IOException unused) {
            Cout.println("tieng dong loi: " + str);
            return -1;
        }
    }

    public static MediaPlayer getMediaSoundFile(String str) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            try {
                AssetFileDescriptor assetFileDescriptorOpenFd = GameMidlet.asset.openFd("music/" + str);
                mediaPlayer.setDataSource(assetFileDescriptorOpenFd.getFileDescriptor(), assetFileDescriptorOpenFd.getStartOffset(), assetFileDescriptorOpenFd.getLength());
                mediaPlayer.prepare();
            } catch (IOException e) {
                Cout.println("am thanh loi: " + str);
                e.printStackTrace();
            }
            return mediaPlayer;
        } finally {
            System.gc();
        }
    }

    public static void play(int i, float f, byte b) {
        MediaPlayer[] mediaPlayerArr;
        SoundPool[] soundPoolArr;
        try {
            if (isSound && i != -1 && (mediaPlayerArr = music) != null && (soundPoolArr = sound) != null) {
                if (b == 1) {
                    mediaPlayerArr[i].setVolume(f, f);
                    music[i].seekTo(0);
                    music[i].setLooping(true);
                    music[i].start();
                } else {
                    int i2 = i - 10;
                    soundPoolArr[i2].play(soundID[i2], f, f, 0, 0, 1.0f);
                }
            }
        } catch (IllegalStateException unused) {
        }
    }

    public static void play_Music_Long(int i, float f, boolean z) {
        try {
            if (isSound && i != -1) {
                int[] iArr = {0, 0, 0};
                music[iArr[i]].setVolume(f, f);
                music[iArr[i]].setLooping(z);
                music[iArr[i]].seekTo(0);
                music[iArr[i]].start();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void pauseMusic(int i, int i2) {
        MediaPlayer mediaPlayer = music[i];
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            return;
        }
        music[i].pause();
    }

    public static void resumeMusic(int i, int i2) {
        MediaPlayer mediaPlayer = music[i];
        if (mediaPlayer == null || mediaPlayer.getCurrentPosition() == 0 || music[i].getCurrentPosition() >= 33332 || volume <= 0) {
            return;
        }
        music[i].start();
    }

    public static Sound gI() {
        if (gI == null) {
            gI = new Sound();
        }
        return gI;
    }

    public static void resumeAll() {
        for (int i = 0; i < music.length; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                music[i].seekTo(music[i].getCurrentPosition());
                music[i].start();
            }
        }
    }

    public static void resumeById(int i) {
        int i2 = 0;
        while (true) {
            try {
                MediaPlayer[] mediaPlayerArr = music;
                if (i2 >= mediaPlayerArr.length) {
                    return;
                }
                if (i == i2) {
                    music[i2].seekTo(mediaPlayerArr[i2].getCurrentPosition());
                    music[i2].start();
                }
                i2++;
            } catch (Exception unused) {
                return;
            }
        }
    }

    public static void releaseAll() {
        int i = 0;
        while (true) {
            MediaPlayer[] mediaPlayerArr = music;
            if (i < mediaPlayerArr.length) {
                MediaPlayer mediaPlayer = mediaPlayerArr[i];
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    music[i] = null;
                }
                i++;
            } else {
                System.gc();
                return;
            }
        }
    }

    public static void stopAll() {
        int i = 0;
        while (true) {
            try {
                MediaPlayer[] mediaPlayerArr = music;
                if (i < mediaPlayerArr.length) {
                    MediaPlayer mediaPlayer = mediaPlayerArr[i];
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        music[i].pause();
                    }
                    i++;
                } else {
                    System.gc();
                    return;
                }
            } catch (Exception unused) {
                return;
            }
        }
    }

    public static void setVolume(int i, int i2, int i3) {
        float fLog = (float) (1.0d - (Math.log(10 - i3) / Math.log(10.0d)));
        MediaPlayer mediaPlayer = music[i];
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(fLog, fLog);
        }
    }
}
