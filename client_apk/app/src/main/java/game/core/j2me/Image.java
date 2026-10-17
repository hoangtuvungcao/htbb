package game.core.j2me;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import com.htbb.client.GameMidlet;
import com.htbb.client.R;
import game.model.Cout;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Image {
    public static Class res = R.drawable.class;

    public static Bitmap createImage(String str) {
        InputStream inputStreamOpen;
        try {
            inputStreamOpen = GameMidlet.asset.open(str);
        } catch (IOException e) {
            Cout.println("!!!!!!!    error loadImage:" + str);
            e.printStackTrace();
            inputStreamOpen = null;
        }
        return BitmapFactory.decodeStream(inputStreamOpen);
    }

    public static String convert(String str) {
        if (str.charAt(0) == '/') {
            str = str.substring(1);
        }
        String strReplaceAll = str.replaceAll("/", "_");
        if (strReplaceAll.indexOf(46) > -1) {
            strReplaceAll = strReplaceAll.substring(0, strReplaceAll.indexOf(46));
        }
        return strReplaceAll.toLowerCase();
    }

    public static int getDrawableID(String str) {
        try {
            return res.getField(str).getInt(null);
        } catch (Exception e) {
            System.out.println("Loi tai getDrawableID . path: " + str + " ly do: " + e.toString());
            return 0;
        }
    }

    public static Bitmap createImage(byte[] bArr, int i, int i2) {
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
    }

    public static Bitmap loadImageFromAsset(String str) {
        InputStream inputStreamOpen;
        try {
            inputStreamOpen = GameMidlet.asset.open(str);
        } catch (IOException e) {
            Cout.println("!!!!!!!    error loadImage:" + str);
            e.printStackTrace();
            inputStreamOpen = null;
        }
        return BitmapFactory.decodeStream(inputStreamOpen);
    }

    public static String getLink(String str) {
        return str.charAt(0) == '/' ? str.substring(1) : str;
    }

    public static Bitmap createImageAsset(String str) throws IOException {
        return BitmapFactory.decodeStream(GameMidlet.asset.open(getLink(str)));
    }

    public static Bitmap createImage(int i, int i2) {
        return Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap creatBitMapAngle(Bitmap bitmap, int i) {
        if (bitmap == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(360 - i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap CreateScaledImage(Bitmap bitmap, int i, int i2) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[width * height];
        bitmap.getPixels(iArr, 0, width, 0, 0, width, height);
        int[] iArr2 = new int[i * i2];
        for (int i3 = 0; i3 < i2; i3++) {
            int i4 = (i3 * height) / i2;
            for (int i5 = 0; i5 < i; i5++) {
                iArr2[(i * i3) + i5] = iArr[(width * i4) + ((i5 * width) / i)];
            }
        }
        return Bitmap.createBitmap(iArr2, i, i2, Bitmap.Config.ARGB_8888);
    }

    public static DataInputStream openFile(String str) {
        try {
            return new DataInputStream(GameMidlet.asset.open(getLink(str)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
