package game.core.j2me;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class Sprite extends Layer {
    private static final int ALPHA_BITMASK = -16777216;
    private static final int FULLY_OPAQUE_ALPHA = -16777216;
    private static final int INVERTED_AXES = 4;
    public static final int TRANS_MIRROR = 2;
    public static final int TRANS_MIRROR_ROT180 = 1;
    public static final int TRANS_MIRROR_ROT270 = 4;
    public static final int TRANS_MIRROR_ROT90 = 7;
    public static final int TRANS_NONE = 0;
    public static final int TRANS_ROT180 = 3;
    public static final int TRANS_ROT270 = 6;
    public static final int TRANS_ROT90 = 5;
    private static final int X_FLIP = 2;
    private static final int Y_FLIP = 1;
    int collisionRectHeight;
    int collisionRectWidth;
    int collisionRectX;
    int collisionRectY;
    private boolean customSequenceDefined;
    int dRefX;
    int dRefY;
    int[] frameCoordsX;
    int[] frameCoordsY;
    int[] frameSequence;
    Rect mDestRect;
    Paint mPaint;
    Rect mSrcRect;
    int numberFrames;
    private int sequenceIndex;
    Bitmap sourceImage;
    int srcFrameHeight;
    int srcFrameWidth;
    int t_collisionRectHeight;
    int t_collisionRectWidth;
    int t_collisionRectX;
    int t_collisionRectY;
    int t_currentTransformation;

    private boolean intersectRect(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        return i5 < i3 && i6 < i4 && i7 > i && i8 > i2;
    }

    public Sprite(Bitmap bitmap) {
        super(bitmap.getWidth(), bitmap.getHeight());
        this.mPaint = new Paint();
        this.mSrcRect = new Rect();
        this.mDestRect = new Rect();
        initializeFrames(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        initCollisionRectBounds();
        setTransformImpl(0);
    }

    public Sprite(Bitmap bitmap, int i, int i2) {
        super(i, i2);
        this.mPaint = new Paint();
        this.mSrcRect = new Rect();
        this.mDestRect = new Rect();
        if (i < 1 || i2 < 1 || bitmap.getWidth() % i != 0 || bitmap.getHeight() % i2 != 0) {
            throw new IllegalArgumentException();
        }
        initializeFrames(bitmap, i, i2, false);
        initCollisionRectBounds();
        setTransformImpl(0);
    }

    public Sprite(Sprite sprite) {
        super(sprite != null ? sprite.getWidth() : 0, sprite != null ? sprite.getHeight() : 0);
        sprite.getClass();
        this.sourceImage = Bitmap.createBitmap(sprite.sourceImage);
        int i = sprite.numberFrames;
        this.numberFrames = i;
        int[] iArr = new int[i];
        this.frameCoordsX = iArr;
        this.frameCoordsY = new int[i];
        System.arraycopy(sprite.frameCoordsX, 0, iArr, 0, sprite.getRawFrameCount());
        System.arraycopy(sprite.frameCoordsY, 0, this.frameCoordsY, 0, sprite.getRawFrameCount());
        this.x = sprite.getX();
        this.y = sprite.getY();
        this.dRefX = sprite.dRefX;
        this.dRefY = sprite.dRefY;
        this.collisionRectX = sprite.collisionRectX;
        this.collisionRectY = sprite.collisionRectY;
        this.collisionRectWidth = sprite.collisionRectWidth;
        this.collisionRectHeight = sprite.collisionRectHeight;
        this.srcFrameWidth = sprite.srcFrameWidth;
        this.srcFrameHeight = sprite.srcFrameHeight;
        setTransformImpl(sprite.t_currentTransformation);
        setVisible(sprite.isVisible());
        this.frameSequence = new int[sprite.getFrameSequenceLength()];
        setFrameSequence(sprite.frameSequence);
        setFrame(sprite.getFrame());
        setRefPixelPosition(sprite.getRefPixelX(), sprite.getRefPixelY());
    }

    public void defineReferencePixel(int i, int i2) {
        this.dRefX = i;
        this.dRefY = i2;
    }

    public void setRefPixelPosition(int i, int i2) {
        this.x = i - getTransformedPtX(this.dRefX, this.dRefY, this.t_currentTransformation);
        this.y = i2 - getTransformedPtY(this.dRefX, this.dRefY, this.t_currentTransformation);
    }

    public int getRefPixelX() {
        return this.x + getTransformedPtX(this.dRefX, this.dRefY, this.t_currentTransformation);
    }

    public int getRefPixelY() {
        return this.y + getTransformedPtY(this.dRefX, this.dRefY, this.t_currentTransformation);
    }

    public void setFrame(int i) {
        if (i < 0 || i >= this.frameSequence.length) {
            throw new IndexOutOfBoundsException();
        }
        this.sequenceIndex = i;
    }

    public final int getFrame() {
        return this.sequenceIndex;
    }

    public int getRawFrameCount() {
        return this.numberFrames;
    }

    public int getFrameSequenceLength() {
        return this.frameSequence.length;
    }

    public void nextFrame() {
        this.sequenceIndex = (this.sequenceIndex + 1) % this.frameSequence.length;
    }

    public void prevFrame() {
        int i = this.sequenceIndex;
        if (i == 0) {
            this.sequenceIndex = this.frameSequence.length - 1;
        } else {
            this.sequenceIndex = i - 1;
        }
    }

    @Override
    public final void paint(Canvas canvas) {
        canvas.getClass();
        if (this.visible) {
            canvas.save();
            int i = this.t_currentTransformation;
            if (i == 2 || i == 7 || i == 1 || i == 4) {
                canvas.scale(-1.0f, 1.0f, this.x + this.dRefX, this.y + this.dRefY);
            }
            int i2 = this.t_currentTransformation;
            if (i2 == 1 || i2 == 3) {
                canvas.rotate(180.0f, this.x + this.dRefX, this.y + this.dRefY);
            } else if (i2 == 4) {
                canvas.rotate(270.0f, this.x + this.dRefX, this.y + this.dRefY);
            } else if (i2 == 5) {
                canvas.rotate(90.0f, this.x + this.dRefX, this.y + this.dRefY);
            } else if (i2 == 6) {
                canvas.rotate(270.0f, this.x + this.dRefX, this.y + this.dRefY);
            } else if (i2 == 7) {
                canvas.rotate(90.0f, this.x + this.dRefX, this.y + this.dRefY);
            }
            Rect rect = this.mSrcRect;
            int[] iArr = this.frameCoordsX;
            int i3 = this.frameSequence[this.sequenceIndex];
            int i4 = iArr[i3];
            int i5 = this.frameCoordsY[i3];
            rect.set(i4, i5, this.srcFrameWidth + i4, this.srcFrameHeight + i5);
            this.mDestRect.set(this.x, this.y, this.x + this.srcFrameWidth, this.y + this.srcFrameHeight);
            canvas.drawBitmap(this.sourceImage, this.mSrcRect, this.mDestRect, this.mPaint);
            canvas.restore();
        }
    }

    public void setFrameSequence(int[] iArr) {
        if (iArr == null) {
            this.sequenceIndex = 0;
            this.customSequenceDefined = false;
            this.frameSequence = new int[this.numberFrames];
            for (int i = 0; i < this.numberFrames; i++) {
                this.frameSequence[i] = i;
            }
            return;
        }
        if (iArr.length < 1) {
            throw new IllegalArgumentException();
        }
        for (int i2 : iArr) {
            if (i2 < 0 || i2 >= this.numberFrames) {
                throw new ArrayIndexOutOfBoundsException();
            }
        }
        this.customSequenceDefined = true;
        int[] iArr2 = new int[iArr.length];
        this.frameSequence = iArr2;
        System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
        this.sequenceIndex = 0;
    }

    public void setImage(Bitmap bitmap, int i, int i2) {
        boolean z = true;
        if (i < 1 || i2 < 1 || bitmap.getWidth() % i != 0 || bitmap.getHeight() % i2 != 0) {
            throw new IllegalArgumentException();
        }
        if ((bitmap.getWidth() / i) * (bitmap.getHeight() / i2) < this.numberFrames) {
            z = false;
            this.customSequenceDefined = false;
        }
        if (this.srcFrameWidth != i || this.srcFrameHeight != i2) {
            int transformedPtX = this.x + getTransformedPtX(this.dRefX, this.dRefY, this.t_currentTransformation);
            int transformedPtY = this.y + getTransformedPtY(this.dRefX, this.dRefY, this.t_currentTransformation);
            setWidthImpl(i);
            setHeightImpl(i2);
            initializeFrames(bitmap, i, i2, z);
            initCollisionRectBounds();
            this.x = transformedPtX - getTransformedPtX(this.dRefX, this.dRefY, this.t_currentTransformation);
            this.y = transformedPtY - getTransformedPtY(this.dRefX, this.dRefY, this.t_currentTransformation);
            computeTransformedBounds(this.t_currentTransformation);
            return;
        }
        initializeFrames(bitmap, i, i2, z);
    }

    public void defineCollisionRectangle(int i, int i2, int i3, int i4) {
        if (i3 < 0 || i4 < 0) {
            throw new IllegalArgumentException();
        }
        this.collisionRectX = i;
        this.collisionRectY = i2;
        this.collisionRectWidth = i3;
        this.collisionRectHeight = i4;
        setTransformImpl(this.t_currentTransformation);
    }

    public void setTransform(int i) {
        setTransformImpl(i);
    }

    public final boolean collidesWith(Sprite sprite, boolean z) {
        if (!sprite.visible || !this.visible) {
            return false;
        }
        int otherLeft = sprite.x + sprite.t_collisionRectX;
        int otherTop = sprite.y + sprite.t_collisionRectY;
        int otherRight = otherLeft + sprite.t_collisionRectWidth;
        int otherBottom = otherTop + sprite.t_collisionRectHeight;
        int left = this.x + this.t_collisionRectX;
        int top = this.y + this.t_collisionRectY;
        int right = left + this.t_collisionRectWidth;
        int bottom = top + this.t_collisionRectHeight;
        if (!intersectRect(otherLeft, otherTop, otherRight, otherBottom, left, top, right, bottom)) {
            return false;
        }
        if (!z) {
            return true;
        }
        if (this.t_collisionRectX < 0) {
            left = this.x;
        }
        if (this.t_collisionRectY < 0) {
            top = this.y;
        }
        if (this.t_collisionRectX + this.t_collisionRectWidth > this.width) {
            right = this.x + this.width;
        }
        if (this.t_collisionRectY + this.t_collisionRectHeight > this.height) {
            bottom = this.y + this.height;
        }
        if (sprite.t_collisionRectX < 0) {
            otherLeft = sprite.x;
        }
        if (sprite.t_collisionRectY < 0) {
            otherTop = sprite.y;
        }
        if (sprite.t_collisionRectX + sprite.t_collisionRectWidth > sprite.width) {
            otherRight = sprite.x + sprite.width;
        }
        if (sprite.t_collisionRectY + sprite.t_collisionRectHeight > sprite.height) {
            otherBottom = sprite.y + sprite.height;
        }
        if (!intersectRect(otherLeft, otherTop, otherRight, otherBottom, left, top, right, bottom)) {
            return false;
        }
        int x1 = left < otherLeft ? otherLeft : left;
        int y1 = top < otherTop ? otherTop : top;
        int x2 = right < otherRight ? right : otherRight;
        int y2 = bottom < otherBottom ? bottom : otherBottom;
        int w = Math.abs(x2 - x1);
        int h = Math.abs(y2 - y1);
        return doPixelCollision(getImageTopLeftX(x1, y1, x2, y2), getImageTopLeftY(x1, y1, x2, y2), sprite.getImageTopLeftX(x1, y1, x2, y2), sprite.getImageTopLeftY(x1, y1, x2, y2), this.sourceImage, this.t_currentTransformation, sprite.sourceImage, sprite.t_currentTransformation, w, h);
    }

    public final boolean collidesWith(TiledLayer tiledLayer, boolean z) {
        boolean z2 = false;
        if (!tiledLayer.visible || !this.visible) {
            return false;
        }
        int i = tiledLayer.x;
        int i2 = tiledLayer.y;
        int i3 = i + tiledLayer.width;
        int i4 = i2 + tiledLayer.height;
        int cellWidth = tiledLayer.getCellWidth();
        int cellHeight = tiledLayer.getCellHeight();
        int i5 = this.x + this.t_collisionRectX;
        int i6 = this.y + this.t_collisionRectY;
        int i7 = i5 + this.t_collisionRectWidth;
        int i8 = i6 + this.t_collisionRectHeight;
        int columns = tiledLayer.getColumns();
        int rows = tiledLayer.getRows();
        if (!intersectRect(i, i2, i3, i4, i5, i6, i7, i8)) {
            return false;
        }
        int i9 = i5 <= i ? 0 : (i5 - i) / cellWidth;
        int i10 = i7 < i3 ? ((i7 - 1) - i) / cellWidth : columns - 1;
        int i11 = i8 < i4 ? ((i8 - 1) - i2) / cellHeight : rows - 1;
        if (!z) {
            for (int i12 = i6 <= i2 ? 0 : (i6 - i2) / cellHeight; i12 <= i11; i12++) {
                for (int i13 = i9; i13 <= i10; i13++) {
                    if (tiledLayer.getCell(i13, i12) != 0) {
                        return true;
                    }
                }
            }
            return false;
        }
        int i14 = this.t_collisionRectX < 0 ? this.x : i5;
        int i15 = this.t_collisionRectY < 0 ? this.y : i6;
        int i16 = this.t_collisionRectX + this.t_collisionRectWidth > this.width ? this.x + this.width : i7;
        int i17 = this.t_collisionRectY + this.t_collisionRectHeight > this.height ? this.y + this.height : i8;
        int i18 = i16;
        int i19 = i15;
        int i20 = i14;
        if (!intersectRect(i, i2, i3, i4, i14, i15, i18, i17)) {
            return false;
        }
        int i21 = i20;
        int i22 = i21 <= i ? 0 : (i21 - i) / cellWidth;
        int i23 = i19;
        int i24 = i23 <= i2 ? 0 : (i23 - i2) / cellHeight;
        int i25 = i18;
        int i26 = i25 < i3 ? ((i25 - 1) - i) / cellWidth : columns - 1;
        int i27 = i17;
        int i28 = i27 < i4 ? ((i27 - 1) - i2) / cellHeight : rows - 1;
        int i29 = (i24 * cellHeight) + i2;
        int i30 = i29 + cellHeight;
        while (i24 <= i28) {
            int i31 = (i22 * cellWidth) + i;
            int i32 = i31 + cellWidth;
            int i33 = i22;
            while (i33 <= i26) {
                int cell = tiledLayer.getCell(i33, i24);
                int i34 = i21;
                if (cell != 0) {
                    if (i21 < i31) {
                        i21 = i31;
                    }
                    if (i23 < i29) {
                        i23 = i29;
                    }
                    if (i25 >= i32) {
                        i25 = i32;
                    }
                    if (i27 >= i30) {
                        i27 = i30;
                    }
                    if (i21 > i25) {
                        int i35 = i25;
                        i25 = i21;
                        i21 = i35;
                    }
                    if (i23 <= i27) {
                        int i36 = i27;
                        i27 = i23;
                        i23 = i36;
                    }
                    if (doPixelCollision(getImageTopLeftX(i21, i27, i25, i23), getImageTopLeftY(i21, i27, i25, i23), tiledLayer.tileSetX[cell] + (i21 - i31), tiledLayer.tileSetY[cell] + (i27 - i29), this.sourceImage, this.t_currentTransformation, tiledLayer.sourceImage, 0, i25 - i21, i23 - i27)) {
                        return true;
                    }
                } else {
                    i25 = i25;
                    i27 = i27;
                }
                i33++;
                i31 += cellWidth;
                i32 += cellWidth;
                i27 = i27;
                i25 = i25;
                i23 = i23;
                i21 = i34;
            }
            i24++;
            i29 += cellHeight;
            i30 += cellHeight;
            z2 = false;
        }
        return z2;
    }

    public final boolean collidesWith(Bitmap bitmap, int i, int i2, boolean z) {
        if (!this.visible) {
            return false;
        }
        int imgRight = i + bitmap.getWidth();
        int imgBottom = i2 + bitmap.getHeight();
        int left = this.x + this.t_collisionRectX;
        int top = this.y + this.t_collisionRectY;
        int right = left + this.t_collisionRectWidth;
        int bottom = top + this.t_collisionRectHeight;
        if (!intersectRect(i, i2, imgRight, imgBottom, left, top, right, bottom)) {
            return false;
        }
        if (!z) {
            return true;
        }
        if (this.t_collisionRectX < 0) {
            left = this.x;
        }
        if (this.t_collisionRectY < 0) {
            top = this.y;
        }
        if (this.t_collisionRectX + this.t_collisionRectWidth > this.width) {
            right = this.x + this.width;
        }
        if (this.t_collisionRectY + this.t_collisionRectHeight > this.height) {
            bottom = this.y + this.height;
        }
        if (!intersectRect(i, i2, imgRight, imgBottom, left, top, right, bottom)) {
            return false;
        }
        int x1 = left < i ? i : left;
        int y1 = top < i2 ? i2 : top;
        int x2 = right < imgRight ? right : imgRight;
        int y2 = bottom < imgBottom ? bottom : imgBottom;
        int w = Math.abs(x2 - x1);
        int h = Math.abs(y2 - y1);
        return doPixelCollision(getImageTopLeftX(x1, y1, x2, y2), getImageTopLeftY(x1, y1, x2, y2), x1 - i, y1 - i2, this.sourceImage, this.t_currentTransformation, bitmap, 0, w, h);
    }

    private void initializeFrames(Bitmap bitmap, int i, int i2, boolean z) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        this.sourceImage = bitmap;
        this.srcFrameWidth = i;
        this.srcFrameHeight = i2;
        int i3 = (width / i) * (height / i2);
        this.numberFrames = i3;
        this.frameCoordsX = new int[i3];
        this.frameCoordsY = new int[i3];
        if (!z) {
            this.sequenceIndex = 0;
        }
        if (!this.customSequenceDefined) {
            this.frameSequence = new int[i3];
        }
        int i4 = 0;
        int i5 = 0;
        while (i4 < height) {
            int i6 = 0;
            while (i6 < width) {
                this.frameCoordsX[i5] = i6;
                this.frameCoordsY[i5] = i4;
                if (!this.customSequenceDefined) {
                    this.frameSequence[i5] = i5;
                }
                i5++;
                i6 += i;
            }
            i4 += i2;
        }
    }

    private void initCollisionRectBounds() {
        this.collisionRectX = 0;
        this.collisionRectY = 0;
        this.collisionRectWidth = this.width;
        this.collisionRectHeight = this.height;
    }

    private static boolean doPixelCollision(int i, int i2, int i3, int i4, Bitmap bitmap, int i5, Bitmap bitmap2, int i6, int i7, int i8) {
        int i9;
        int i10;
        int i11;
        int i12;
        int i13;
        int i14;
        int i15;
        int i16;
        int i17;
        int i18;
        int i19 = i8 * i7;
        int[] iArr = new int[i19];
        int[] iArr2 = new int[i19];
        if ((i5 & 4) != 0) {
            if ((i5 & 1) != 0) {
                i18 = i19 - i8;
                i12 = -i8;
            } else {
                i12 = i8;
                i18 = 0;
            }
            if ((i5 & 2) != 0) {
                i11 = i18 + (i8 - 1);
                i9 = -1;
            } else {
                i11 = i18;
                i9 = 1;
            }
            bitmap.getPixels(iArr, 0, i8, i, i2, i8, i7);
        } else {
            if ((i5 & 1) != 0) {
                i10 = i19 - i7;
                i9 = -i7;
            } else {
                i9 = i7;
                i10 = 0;
            }
            if ((i5 & 2) != 0) {
                i11 = i10 + (i7 - 1);
                i12 = -1;
            } else {
                i11 = i10;
                i12 = 1;
            }
            bitmap.getPixels(iArr, 0, i7, i, i2, i7, i8);
        }
        if ((i6 & 4) != 0) {
            if ((i6 & 1) != 0) {
                int i20 = i19 - i8;
                i16 = -i8;
                i17 = i20;
            } else {
                i16 = i8;
                i17 = 0;
            }
            if ((i6 & 2) != 0) {
                i13 = -1;
                i15 = i17 + (i8 - 1);
            } else {
                i15 = i17;
                i13 = 1;
            }
            bitmap2.getPixels(iArr2, 0, i8, i3, i4, i8, i7);
        } else {
            if ((i6 & 1) != 0) {
                i14 = i19 - i7;
                i13 = -i7;
            } else {
                i13 = i7;
                i14 = 0;
            }
            if ((i6 & 2) != 0) {
                i15 = i14 + (i7 - 1);
                i16 = -1;
            } else {
                i15 = i14;
                i16 = 1;
            }
            bitmap2.getPixels(iArr2, 0, i7, i3, i4, i7, i8);
        }
        for (int i21 = 0; i21 < i8; i21++) {
            int i22 = i15;
            int i23 = i11;
            for (int i24 = 0; i24 < i7; i24++) {
                if ((iArr[i23] & (-16777216)) == -16777216 && (iArr2[i22] & (-16777216)) == -16777216) {
                    return true;
                }
                i23 += i12;
                i22 += i16;
            }
            i11 += i9;
            i15 += i13;
        }
        return false;
    }

    private int getImageTopLeftX(int i, int i2, int i3, int i4) {
        int i5;
        switch (this.t_currentTransformation) {
            case 0:
            case 1:
                i5 = i - this.x;
                break;
            case 2:
            case 3:
                i5 = (this.x + this.width) - i3;
                break;
            case 4:
            case 5:
                i5 = i2 - this.y;
                break;
            case 6:
            case 7:
                i5 = (this.y + this.height) - i4;
                break;
            default:
                Log.e(getClass().getName(), "Sprite: t_currentTransformation=" + this.t_currentTransformation);
                return 0;
        }
        return i5 + this.frameCoordsX[this.frameSequence[this.sequenceIndex]];
    }

    private int getImageTopLeftY(int i, int i2, int i3, int i4) {
        int i5;
        switch (this.t_currentTransformation) {
            case 0:
            case 2:
                i5 = i2 - this.y;
                break;
            case 1:
            case 3:
                i5 = (this.y + this.height) - i4;
                break;
            case 4:
            case 6:
                i5 = i - this.x;
                break;
            case 5:
            case 7:
                i5 = (this.x + this.width) - i3;
                break;
            default:
                Log.e(getClass().getName(), "Sprite: t_currentTransformation=" + this.t_currentTransformation);
                return 0;
        }
        return i5 + this.frameCoordsY[this.frameSequence[this.sequenceIndex]];
    }

    private void setTransformImpl(int i) {
        this.x = (this.x + getTransformedPtX(this.dRefX, this.dRefY, this.t_currentTransformation)) - getTransformedPtX(this.dRefX, this.dRefY, i);
        this.y = (this.y + getTransformedPtY(this.dRefX, this.dRefY, this.t_currentTransformation)) - getTransformedPtY(this.dRefX, this.dRefY, i);
        computeTransformedBounds(i);
        this.t_currentTransformation = i;
    }

    private void computeTransformedBounds(int i) {
        switch (i) {
            case 0:
                this.t_collisionRectX = this.collisionRectX;
                this.t_collisionRectY = this.collisionRectY;
                this.t_collisionRectWidth = this.collisionRectWidth;
                this.t_collisionRectHeight = this.collisionRectHeight;
                this.width = this.srcFrameWidth;
                this.height = this.srcFrameHeight;
                return;
            case 1:
                int i2 = this.srcFrameHeight;
                int i3 = this.collisionRectY;
                int i4 = this.collisionRectHeight;
                this.t_collisionRectY = i2 - (i3 + i4);
                this.t_collisionRectX = this.collisionRectX;
                this.t_collisionRectWidth = this.collisionRectWidth;
                this.t_collisionRectHeight = i4;
                this.width = this.srcFrameWidth;
                this.height = this.srcFrameHeight;
                return;
            case 2:
                int i5 = this.srcFrameWidth;
                int i6 = this.collisionRectX;
                int i7 = this.collisionRectWidth;
                this.t_collisionRectX = i5 - (i6 + i7);
                this.t_collisionRectY = this.collisionRectY;
                this.t_collisionRectWidth = i7;
                this.t_collisionRectHeight = this.collisionRectHeight;
                this.width = i5;
                this.height = this.srcFrameHeight;
                return;
            case 3:
                int i8 = this.srcFrameWidth;
                int i9 = this.collisionRectWidth;
                this.t_collisionRectX = i8 - (this.collisionRectX + i9);
                int i10 = this.srcFrameHeight;
                int i11 = this.collisionRectHeight;
                this.t_collisionRectY = i10 - (this.collisionRectY + i11);
                this.t_collisionRectWidth = i9;
                this.t_collisionRectHeight = i11;
                this.width = i8;
                this.height = this.srcFrameHeight;
                return;
            case 4:
                this.t_collisionRectY = this.collisionRectX;
                this.t_collisionRectX = this.collisionRectY;
                this.t_collisionRectHeight = this.collisionRectWidth;
                this.t_collisionRectWidth = this.collisionRectHeight;
                this.width = this.srcFrameHeight;
                this.height = this.srcFrameWidth;
                return;
            case 5:
                int i12 = this.srcFrameHeight;
                int i13 = this.collisionRectHeight;
                this.t_collisionRectX = i12 - (this.collisionRectY + i13);
                this.t_collisionRectY = this.collisionRectX;
                this.t_collisionRectHeight = this.collisionRectWidth;
                this.t_collisionRectWidth = i13;
                this.width = i12;
                this.height = this.srcFrameWidth;
                return;
            case 6:
                this.t_collisionRectX = this.collisionRectY;
                int i14 = this.srcFrameWidth;
                int i15 = this.collisionRectWidth;
                this.t_collisionRectY = i14 - (this.collisionRectX + i15);
                this.t_collisionRectHeight = i15;
                this.t_collisionRectWidth = this.collisionRectHeight;
                this.width = this.srcFrameHeight;
                this.height = this.srcFrameWidth;
                return;
            case 7:
                int i16 = this.srcFrameHeight;
                int i17 = this.collisionRectHeight;
                this.t_collisionRectX = i16 - (this.collisionRectY + i17);
                int i18 = this.srcFrameWidth;
                int i19 = this.collisionRectWidth;
                this.t_collisionRectY = i18 - (this.collisionRectX + i19);
                this.t_collisionRectHeight = i19;
                this.t_collisionRectWidth = i17;
                this.width = i16;
                this.height = this.srcFrameWidth;
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    int getTransformedPtX(int i, int i2, int i3) {
        int i4;
        int i5;
        switch (i3) {
            case 0:
            case 1:
                return i;
            case 2:
                i4 = this.srcFrameWidth;
                return (i4 - i) - 1;
            case 3:
                i4 = this.srcFrameWidth;
                return (i4 - i) - 1;
            case 4:
            case 6:
                return i2;
            case 5:
                i5 = this.srcFrameHeight;
                return (i5 - i2) - 1;
            case 7:
                i5 = this.srcFrameHeight;
                return (i5 - i2) - 1;
            default:
                Log.e(getClass().getName(), "Sprite: transform=" + i3);
                return 0;
        }
    }

    int getTransformedPtY(int i, int i2, int i3) {
        int i4;
        int i5;
        switch (i3) {
            case 0:
            case 2:
                return i2;
            case 1:
                i4 = this.srcFrameHeight;
                return (i4 - i2) - 1;
            case 3:
                i4 = this.srcFrameHeight;
                return (i4 - i2) - 1;
            case 4:
            case 5:
                return i;
            case 6:
                i5 = this.srcFrameWidth;
                return (i5 - i) - 1;
            case 7:
                i5 = this.srcFrameWidth;
                return (i5 - i) - 1;
            default:
                Log.e(getClass().getName(), "Sprite: transform=" + i3);
                return 0;
        }
    }
}
