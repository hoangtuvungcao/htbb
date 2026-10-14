package game.core.j2me;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import com.teamobi.kpah.GameMidlet;
import game.model.Cout;
import game.render.GCanvas;

public class Graphics {
    public static int BASELINE = 64;
    public static int BOTTOM = 32;
    public static int DOTTED = 1;
    public static int HCENTER = 1;
    public static int LEFT = 4;
    public static int RIGHT = 8;
    public static int SOLID = 0;
    public static int TOP = 16;
    public static final int TRANS_MIRROR = 2;
    public static final int TRANS_MIRROR_ROT180 = 1;
    public static final int TRANS_MIRROR_ROT270 = 4;
    public static final int TRANS_MIRROR_ROT90 = 7;
    public static final int TRANS_NONE = 0;
    public static final int TRANS_ROT180 = 3;
    public static final int TRANS_ROT270 = 6;
    public static final int TRANS_ROT90 = 5;
    public static int VCENTER = 2;
    public static Canvas canvas;
    private Images2 img;
    public Paint paint;
    public int translateX;
    public int translateY;

    public int getDisplayColor(int i) {
        return i;
    }

    public int getHeight() {
        return 12;
    }

    public void setHeight(int i) {
    }

    public Graphics() {
        this.translateX = 0;
        this.translateY = 0;
        this.paint = new Paint();
    }

    public Graphics(Images2 images2) {
        this(new Canvas(images2.bitmap), images2);
        this.paint = new Paint();
    }

    public Graphics(Canvas canvas2, Images2 images2) {
        this.translateX = 0;
        this.translateY = 0;
        canvas = canvas2;
        this.img = images2;
        canvas2.save();
    }

    public void setCanvas(Canvas canvas2) {
        canvas = canvas2;
    }

    public void drawRegion(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6) {
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(Bitmap.createBitmap(bitmap, i, i2, i3, i4), i5, i6, (Paint) null);
    }

    public void drawRegion(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        drawRegion(bitmap, i, i2, i3, i4, i5, i6, i7, 20);
    }

    public void drawRegion(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9;
        int i10;
        int i11;
        if (bitmap == null) {
            return;
        }
        canvas.save();
        int i12 = 0;
        if (i8 != 3) {
            if (i8 == 6) {
                i10 = i4 / 2;
            } else if (i8 != 10) {
                if (i8 == 17) {
                    i9 = i3 / 2;
                } else if (i8 == 20) {
                    i9 = 0;
                    i10 = 0;
                } else if (i8 != 24) {
                    if (i8 == 33) {
                        i9 = i3 / 2;
                    } else if (i8 == 36) {
                        i10 = i4;
                    } else if (i8 != 40) {
                        i9 = 0;
                        i10 = 0;
                    } else {
                        i9 = i3;
                    }
                    i10 = i4;
                } else {
                    i9 = i3;
                }
                i10 = 0;
            } else {
                i10 = i4 / 2;
                i9 = i3;
            }
            i9 = 0;
        } else {
            i9 = i3 / 2;
            i10 = i4 / 2;
        }
        int i13 = i6 - i9;
        int i14 = i7 - i10;
        switch (i5) {
            case 1:
                float f = i13;
                float f2 = i14;
                canvas.scale(-1.0f, 1.0f, f, f2);
                canvas.rotate(180.0f, f, f2);
                i11 = i4;
                break;
            case 2:
                canvas.scale(-1.0f, 1.0f, i13, i14);
                i11 = 0;
                i12 = i3;
                break;
            case 3:
                canvas.rotate(180.0f, i13, i14);
                i12 = i3;
                i11 = i4;
                break;
            case 4:
                float f3 = i13;
                float f4 = i14;
                canvas.scale(-1.0f, 1.0f, f3, f4);
                canvas.rotate(90.0f, f3, f4);
                i11 = 0;
                break;
            case 5:
                canvas.rotate(90.0f, i13, i14);
                i11 = i4;
                break;
            case 6:
                canvas.rotate(270.0f, i13, i14);
                i11 = 0;
                i12 = i3;
                break;
            case 7:
                float f5 = i13;
                float f6 = i14;
                canvas.scale(-1.0f, 1.0f, f5, f6);
                canvas.rotate(270.0f, f5, f6);
                i12 = i3;
                i11 = i4;
                break;
            default:
                i11 = 0;
                break;
        }
        int i15 = i13 - i12;
        int i16 = i14 - i11;
        canvas.clipRect(i15, i16, i3 + i15, i4 + i16);
        canvas.drawBitmap(bitmap, i15 - i, i16 - i2, (Paint) null);
        canvas.restore();
    }

    public void drawRegionGraphics(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9;
        int i10;
        int i11;
        if (bitmap == null) {
            return;
        }
        int i12 = 0;
        if (i8 != 3) {
            if (i8 == 6) {
                i10 = i4 / 2;
            } else if (i8 != 10) {
                if (i8 == 17) {
                    i9 = i3 / 2;
                } else if (i8 == 20) {
                    i9 = 0;
                    i10 = 0;
                } else if (i8 != 24) {
                    if (i8 == 33) {
                        i9 = i3 / 2;
                    } else if (i8 == 36) {
                        i10 = i4;
                    } else if (i8 != 40) {
                        i9 = 0;
                        i10 = 0;
                    } else {
                        i9 = i3;
                    }
                    i10 = i4;
                } else {
                    i9 = i3;
                }
                i10 = 0;
            } else {
                i10 = i4 / 2;
                i9 = i3;
            }
            i9 = 0;
        } else {
            i9 = i3 / 2;
            i10 = i4 / 2;
        }
        int i13 = i6 - i9;
        int i14 = i7 - i10;
        switch (i5) {
            case 1:
                float f = i13;
                float f2 = i14;
                canvas.scale(-1.0f, 1.0f, f, f2);
                canvas.rotate(180.0f, f, f2);
                i11 = i4;
                break;
            case 2:
                canvas.scale(-1.0f, 1.0f, i13, i14);
                i11 = 0;
                i12 = i3;
                break;
            case 3:
                canvas.rotate(180.0f, i13, i14);
                i12 = i3;
                i11 = i4;
                break;
            case 4:
                float f3 = i13;
                float f4 = i14;
                canvas.scale(-1.0f, 1.0f, f3, f4);
                canvas.rotate(90.0f, f3, f4);
                i11 = 0;
                break;
            case 5:
                canvas.rotate(90.0f, i13, i14);
                i11 = i4;
                break;
            case 6:
                canvas.rotate(270.0f, i13, i14);
                i11 = 0;
                i12 = i3;
                break;
            case 7:
                float f5 = i13;
                float f6 = i14;
                canvas.scale(-1.0f, 1.0f, f5, f6);
                canvas.rotate(270.0f, f5, f6);
                i12 = i3;
                i11 = i4;
                break;
            default:
                i11 = 0;
                break;
        }
        int i15 = i13 - i12;
        int i16 = i14 - i11;
        canvas.clipRect(i15, i16, i3 + i15, i4 + i16);
        canvas.drawBitmap(bitmap, i15 - i, i16 - i2, (Paint) null);
    }

    public void drawRegionAlpha(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9;
        int i10;
        int i11;
        if (bitmap == null) {
            return;
        }
        canvas.save();
        int i12 = 0;
        if (i8 != 3) {
            if (i8 == 6) {
                i10 = i4 / 2;
            } else if (i8 != 10) {
                if (i8 == 17) {
                    i9 = i3 / 2;
                } else if (i8 == 20) {
                    i9 = 0;
                    i10 = 0;
                } else if (i8 != 24) {
                    if (i8 == 33) {
                        i9 = i3 / 2;
                    } else if (i8 == 36) {
                        i10 = i4;
                    } else if (i8 != 40) {
                        i9 = 0;
                        i10 = 0;
                    } else {
                        i9 = i3;
                    }
                    i10 = i4;
                } else {
                    i9 = i3;
                }
                i10 = 0;
            } else {
                i10 = i4 / 2;
                i9 = i3;
            }
            i9 = 0;
        } else {
            i9 = i3 / 2;
            i10 = i4 / 2;
        }
        int i13 = i6 - i9;
        int i14 = i7 - i10;
        switch (i5) {
            case 1:
                float f = i13;
                float f2 = i14;
                canvas.scale(-1.0f, 1.0f, f, f2);
                canvas.rotate(180.0f, f, f2);
                i11 = i4;
                break;
            case 2:
                canvas.scale(-1.0f, 1.0f, i13, i14);
                i11 = 0;
                i12 = i3;
                break;
            case 3:
                canvas.rotate(180.0f, i13, i14);
                i12 = i3;
                i11 = i4;
                break;
            case 4:
                float f3 = i13;
                float f4 = i14;
                canvas.scale(-1.0f, 1.0f, f3, f4);
                canvas.rotate(90.0f, f3, f4);
                i11 = 0;
                break;
            case 5:
                canvas.rotate(90.0f, i13, i14);
                i11 = i4;
                break;
            case 6:
                canvas.rotate(270.0f, i13, i14);
                i11 = 0;
                i12 = i3;
                break;
            case 7:
                float f5 = i13;
                float f6 = i14;
                canvas.scale(-1.0f, 1.0f, f5, f6);
                canvas.rotate(270.0f, f5, f6);
                i12 = i3;
                i11 = i4;
                break;
            default:
                i11 = 0;
                break;
        }
        int i15 = i13 - i12;
        int i16 = i14 - i11;
        canvas.clipRect(i15, i16, i3 + i15, i4 + i16);
        canvas.drawBitmap(bitmap, i15 - i, i16 - i2, this.paint);
        canvas.restore();
    }

    public void drawImage(Bitmap bitmap, int i, int i2) {
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, i, i2, (Paint) null);
    }

    public void drawImage(Bitmap bitmap, int i, int i2, int i3) {
        if (bitmap == null) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (i3 != 3) {
            if (i3 == 6) {
                height /= 2;
            } else if (i3 != 10) {
                if (i3 == 17) {
                    width /= 2;
                } else if (i3 == 20) {
                    width = 0;
                    height = 0;
                } else if (i3 != 24) {
                    if (i3 == 33) {
                        width /= 2;
                    } else if (i3 != 36) {
                        if (i3 != 40) {
                            width = 0;
                            height = 0;
                        }
                    }
                }
                height = 0;
            } else {
                height /= 2;
            }
            width = 0;
        } else {
            width /= 2;
            height /= 2;
        }
        canvas.drawBitmap(bitmap, i - width, i2 - height, (Paint) null);
    }

    public void drawImageAlpha(Bitmap bitmap, int i, int i2, int i3) {
        if (bitmap == null) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (i3 != 3) {
            if (i3 == 6) {
                height /= 2;
            } else if (i3 != 10) {
                if (i3 == 17) {
                    width /= 2;
                } else if (i3 == 20) {
                    width = 0;
                    height = 0;
                } else if (i3 != 24) {
                    if (i3 == 33) {
                        width /= 2;
                    } else if (i3 != 36) {
                        if (i3 != 40) {
                            width = 0;
                            height = 0;
                        }
                    }
                }
                height = 0;
            } else {
                height /= 2;
            }
            width = 0;
        } else {
            width /= 2;
            height /= 2;
        }
        canvas.drawBitmap(bitmap, i - width, i2 - height, this.paint);
    }

    public void drawImage(Bitmap bitmap, int i, int i2, int i3, int i4) {
        if (bitmap == null) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(i3, (bitmap.getWidth() / 2) + i, (bitmap.getHeight() / 2) + i2);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true), i, i2, (Paint) null);
    }

    public void drawChar(char c, int i, int i2) {
        char[] cArr = {'0'};
        cArr[0] = c;
        canvas.drawText(cArr, 0, 1, i, i2, this.paint);
    }

    public void drawChar(char[] cArr, int i, int i2, int i3, int i4) {
        canvas.drawText(cArr, i, i2, i3, i4, this.paint);
    }

    public void drawString(String str, int i, int i2) {
        canvas.drawText(str, i, i2, this.paint);
    }

    public void drawString(String str, int i, int i2, int i3, Paint paint) {
        str.trim();
        if (str.endsWith("\n")) {
            str = str.substring(0, str.length() - 1);
        }
        if (i3 == 0) {
            paint.setTextAlign(Paint.Align.LEFT);
        } else if (i3 == 1) {
            paint.setTextAlign(Paint.Align.RIGHT);
        } else if (i3 == 2) {
            paint.setTextAlign(Paint.Align.CENTER);
        } else {
            paint.setTextAlign(Paint.Align.LEFT);
        }
        paint.setAntiAlias(true);
        canvas.drawText(str, i, i2, paint);
    }

    public void drawStringCell(String str, int i, int i2, int i3, Paint paint, int i4, int i5) {
        str.trim();
        new Graphics();
        int width = getWidth(str);
        int height = getHeight() / 2;
        if (str.endsWith("\n")) {
            str = str.substring(0, str.length() - 1);
        }
        if (i3 == 0) {
            fillRect(i - 1, i2 - 1, width + 2, height);
            paint.setTextAlign(Paint.Align.LEFT);
        } else if (i3 == 1) {
            setColor(i5);
            int i6 = (i - 1) - width;
            int i7 = (i2 - 2) - height;
            int i8 = width + 2;
            int i9 = height + 3;
            fillRect(i6, i7, i8, i9);
            setColor(i4);
            drawRect(i6, i7, i8, i9);
            paint.setTextAlign(Paint.Align.RIGHT);
        } else if (i3 == 2) {
            paint.setTextAlign(Paint.Align.CENTER);
        } else {
            paint.setTextAlign(Paint.Align.LEFT);
        }
        paint.setAntiAlias(true);
        canvas.drawText(str, i, i2, paint);
    }

    public void drawString(String str, int i, int i2, int i3) {
        str.trim();
        this.paint.setStyle(Paint.Style.FILL);
        if (str.endsWith("\n")) {
            str = str.substring(0, str.length() - 1);
        }
        if (i3 == 0) {
            this.paint.setTextAlign(Paint.Align.LEFT);
        } else if (i3 == 1) {
            this.paint.setTextAlign(Paint.Align.RIGHT);
        } else if (i3 == 2) {
            this.paint.setTextAlign(Paint.Align.CENTER);
        } else {
            this.paint.setTextAlign(Paint.Align.LEFT);
        }
        this.paint.setAntiAlias(true);
        canvas.drawText(str, i, i2, this.paint);
    }

    public void fillRect(int i, int i2, int i3, int i4) {
        this.paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(i, i2, i3 + i, i4 + i2, this.paint);
    }

    public int getWidth(String str) {
        str.trim();
        this.paint.setTextSize(9.5f);
        return (int) this.paint.measureText(str);
    }

    public void drawRect(int i, int i2, int i3, int i4) {
        this.paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(i, i2, i3 + i, i4 + i2, this.paint);
    }

    public void setColor(int i) {
        if (i == 0) {
            i = -16777216;
        } else if (i == 1) {
            i = -1;
        }
        this.paint.setColor(i);
    }

    public void setColor(int i, float f) {
        int i2 = ((int) f) * 10;
        if (i == 0) {
            i = -16777216;
        } else if (i == 1) {
            i = -1;
        }
        this.paint.setColor(i);
        this.paint.setAlpha(i2);
    }

    public void setColor(int i, int i2, int i3) {
        this.paint.setARGB(0, i, i2, i3);
    }

    public void setAlpha(int i) {
        this.paint.setAlpha(i);
    }

    public void drawLine(int i, int i2, int i3, int i4) {
        canvas.drawLine(i, i2, i3, i4, this.paint);
    }

    public void drawArc(int i, int i2, int i3, int i4, int i5, int i6) {
        this.paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(i, i2, i + i3, i2 + i4), i5, i6, false, this.paint);
    }

    public void fillArc(int i, int i2, int i3, int i4, int i5, int i6) {
        RectF rectF;
        this.paint.setStyle(Paint.Style.FILL);
        if (GCanvas.screenlevel == 2) {
            rectF = new RectF(i + 1, i2, i + i3, i2 + i4);
        } else {
            rectF = new RectF(i, i2, i + i3, i2 + i4);
        }
        canvas.drawArc(rectF, i5, i6, false, this.paint);
    }

    public void translate(int i, int i2) {
        canvas.translate(i, i2);
        this.translateX += i;
        this.translateY += i2;
    }

    public void translateAndroid9(int i, int i2) {
        if (GameMidlet.IS_ANDROID_9) {
            canvas.translate(i, i2);
            this.translateX += i;
            this.translateY += i2;
        }
    }

    public int getTranslateX() {
        return this.translateX;
    }

    public int getTranslateY() {
        return this.translateY;
    }

    public static class gFont {
        public static int BOLD = 3;
        public static int BOLD_ITALIC = 1;
        public static int ITALIC = 2;
        public static int NORMAL = 0;
        public static float SIZE_MEDIUM = 10.0f;
        public static int STYLE_BOLD = 3;
        public static int STYLE_PLAIN;
        public float mySize;
        public int myStyle;

        public gFont(int i, float f) {
            this.myStyle = i;
            this.mySize = f;
        }
    }

    public void setFont(gFont gfont) {
        this.paint.setTextSize(gfont.mySize);
    }

    public void setClip(int i, int i2, int i3, int i4) {
        try {
            if (GameMidlet.IS_ANDROID_9) {
                return;
            }
            canvas.clipRect(i, i2, i + i3, i2 + i4, Region.Op.REPLACE);
        } catch (Exception unused) {
            Cout.println("Loi setclip graphics");
        }
    }

    public int getClipX() {
        Rect clipBounds = canvas.getClipBounds();
        if (clipBounds != null) {
            return clipBounds.left;
        }
        return 0;
    }

    public int getClipY() {
        Rect clipBounds = canvas.getClipBounds();
        if (clipBounds != null) {
            return clipBounds.top;
        }
        return 0;
    }

    public int getClipWidth() {
        Rect clipBounds = canvas.getClipBounds();
        if (clipBounds != null) {
            return clipBounds.width();
        }
        return 0;
    }

    public int getClipHeight() {
        Rect clipBounds = canvas.getClipBounds();
        if (clipBounds != null) {
            return clipBounds.height();
        }
        return 0;
    }

    public void drawRoundRect(int i, int i2, int i3, int i4, int i5, int i6) {
        this.paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(new RectF(i, i2, i + i3, i2 + i4), i5, i6, this.paint);
    }

    public void fillRoundRect(int i, int i2, int i3, int i4, int i5, int i6) {
        this.paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(i, i2, i + i3, i2 + i4), i5, i6, this.paint);
    }

    public void drawRGB(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6, boolean z) {
        canvas.drawBitmap(iArr, i, i5, i3 + this.translateX, i4 + this.translateY, i5, i6, z, (Paint) null);
    }

    public void fillTriangle(int i, int i2, int i3, int i4, int i5, int i6) {
        Path path = new Path();
        path.moveTo(i + this.translateX, i2 + this.translateY);
        path.lineTo(i3 + this.translateX, i4 + this.translateY);
        path.lineTo(i5 + this.translateX, i6 + this.translateY);
        path.close();
        this.paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, this.paint);
    }

    public void drawTriangle(int i, int i2, int i3, int i4, int i5, int i6) {
        Path path = new Path();
        path.moveTo(i + this.translateX, i2 + this.translateY);
        path.lineTo(i3 + this.translateX, i4 + this.translateY);
        path.lineTo(i5 + this.translateX, i6 + this.translateY);
        path.close();
        this.paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, this.paint);
    }

    public Bitmap createImage(Bitmap bitmap, int i, int i2, int i3, int i4) {
        return Bitmap.createBitmap(bitmap, i, i2, i3, i4);
    }

    public void ClipRec(int i, int i2, int i3, int i4) {
        if (GameMidlet.IS_ANDROID_9) {
            saveCanvas();
            canvas.clipRect(i, i2, i3 + i, i4 + i2);
        }
    }

    public void ClipRec2(int i, int i2, int i3, int i4) {
        if (GameMidlet.IS_ANDROID_9) {
            canvas.clipRect(i, i2, i3 + i, i4 + i2);
        }
    }

    public void translateAndroid(int i, int i2) {
        if (GameMidlet.IS_ANDROID_9) {
            translate(i, i2);
        }
    }

    public static void resetTransAndroid(Graphics graphics) {
        if (GameMidlet.IS_ANDROID_9) {
            graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
            graphics.translate(0, 0);
        }
    }

    public void saveCanvas() {
        if (GameMidlet.IS_ANDROID_9) {
            canvas.save();
        }
    }

    public void restoreCanvas() {
        if (GameMidlet.IS_ANDROID_9) {
            canvas.restore();
        }
    }
}
