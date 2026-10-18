package game.core.j2me;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import java.util.HashMap;
import java.util.Vector;

public class SoundManager {
    public static MediaPlayer bgMusic = null;
    public static SoundManager instance = null;
    public static AudioManager mAudioManager = null;
    public static Context mContext = null;
    public static final int snd_ava_angry_b = 30;
    public static final int snd_ava_angry_g = 31;
    public static final int snd_ava_crow = 80;
    public static final int snd_ava_cry_b = 32;
    public static final int snd_ava_cry_b_2 = 33;
    public static final int snd_ava_cry_g = 34;
    public static final int snd_ava_cry_g_2 = 35;
    public static final int snd_ava_fight = 36;
    public static final int snd_ava_fight_2 = 37;
    public static final int snd_ava_fight_3 = 38;
    public static final int snd_ava_fight_4 = 39;
    public static final int snd_ava_jump_b = 40;
    public static final int snd_ava_jump_b_2 = 41;
    public static final int snd_ava_jump_g = 42;
    public static final int snd_ava_jump_g_2 = 43;
    public static final int snd_ava_kiss = 44;
    public static final int snd_ava_kiss_2 = 45;
    public static final int snd_ava_kiss_3 = 46;
    public static final int snd_ava_kiss_b = 47;
    public static final int snd_ava_laugh_b = 48;
    public static final int snd_ava_laugh_b_2 = 49;
    public static final int snd_ava_laugh_b_3 = 50;
    public static final int snd_ava_laugh_g = 51;
    public static final int snd_ava_laugh_g_2 = 52;
    public static final int snd_ava_laugh_g_3 = 53;
    public static final int snd_ava_leuleu_b = 54;
    public static final int snd_ava_leuleu_b_2 = 55;
    public static final int snd_ava_leuleu_g = 56;
    public static final int snd_ava_leuleu_g_2 = 57;
    public static final int snd_bg_fishing = 81;
    public static final int snd_bg_house = 83;
    public static final int snd_bg_shop1 = 84;
    public static final int snd_bg_shop2 = 85;
    public static final int snd_bg_shop3 = 86;
    public static final int snd_bg_wedding = 82;
    public static final int snd_effect_buy = 2;
    public static final int snd_effect_dao_dat = 4;
    public static final int snd_effect_earned_money = 3;
    public static final int snd_effect_fish = 1;
    public static final int snd_effect_fishing_reel = 0;
    public static final int snd_effect_thu_hoach = 6;
    public static final int snd_effect_tuoi_nuoc = 5;
    public static SoundPool soundPool;
    public static HashMap<Integer, Integer> soundPoolMap;
    private static Vector<Integer> mAvailibleSounds = new Vector<>();
    private static Vector<Integer> mKillSoundQueue = new Vector<>();
    private static Handler mHandler = new Handler();
    public static int currentBGMusic = 0;
    public static boolean isBGplay = false;

    public static void loadSounds() {
    }

    public static void loadSoundsEffect() {
    }

    public static void playBGSound(int i) {
    }

    public static void playLoopedSound(int i) {
    }

    public static void playSound(int i) {
    }

    public static void stopBGSound() {
    }

    public static void stopSound(int i) {
    }

    public static SoundManager gI() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public static void init(Context context) {
        mContext = context;
        soundPool = new SoundPool(4, 3, 100);
        soundPoolMap = new HashMap<>();
        mAudioManager = (AudioManager) mContext.getSystemService("audio");
        loadSoundsEffect();
    }

    public void addSound(int i, int i2) {
        mAvailibleSounds.add(Integer.valueOf(i));
        soundPoolMap.put(Integer.valueOf(i), Integer.valueOf(soundPool.load(mContext, i2, 1)));
    }

    public static void cleanup() {
        soundPool.release();
        soundPool = null;
        soundPoolMap.clear();
        mAudioManager.unloadSoundEffects();
        instance = null;
    }
}
