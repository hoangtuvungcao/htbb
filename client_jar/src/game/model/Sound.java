package game.model;

import java.util.Vector;
import javax.microedition.media.Player;

public class Sound {
    public int volume;
    private Player music;
    public static Sound me = new Sound();
    private Vector sound;
    Vector name;
    Vector loopSound;
    public int isPlaying = -1;

    public void onRequestOpenSound(String des, byte id) {
    }

    public int setSound(String na) {
        if (this.name == null) {
            this.name = new Vector();
        }
        for (int i = 0; i < this.name.size(); i++) {
            String str = (String) this.name.elementAt(i);
            if (str.equals(na)) {
                return i;
            }
        }
        return -1;
    }

    public void onSoundData(byte[] dataS, byte id, byte loopS) {
    }

    public void pauseMusic() {
    }

    public void resumeMusic() {
    }

    public void stopMusic() {
    }

    public void play(byte[] data, int index) {
    }

    public void setVolume(int a) {
    }
}
