package game.model;

import android.graphics.Bitmap;
import java.lang.reflect.Array;

public class WPSplashInfo {
    public Bitmap image;
    public int[][] P0_X = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 8);
    public int[][] P0_Y = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 8);
    public int[][] PF_X = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 8);
    public int[][] PF_Y = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 8);
    public int[][] PF_W = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 8);
    public int[][] PF_H = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 4, 8);
}
