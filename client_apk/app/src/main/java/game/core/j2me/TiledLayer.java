package game.core.j2me;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.lang.reflect.Array;

public class TiledLayer extends Layer {
    private int[] anim_to_static;
    private int cellHeight;
    private int[][] cellMatrix;
    private int cellWidth;
    private int columns;
    private Rect mDestRect;
    private Paint mPaint;
    private Rect mSrcRect;
    private int numOfAnimTiles;
    private int numberOfTiles;
    private int rows;
    Bitmap sourceImage;
    int[] tileSetX;
    int[] tileSetY;

    public TiledLayer(int i, int i2, Bitmap bitmap, int i3, int i4) {
        super((i < 1 || i3 < 1) ? -1 : i * i3, (i2 < 1 || i4 < 1) ? -1 : i2 * i4);
        this.mPaint = new Paint();
        this.mSrcRect = new Rect();
        this.mDestRect = new Rect();
        if (bitmap.getWidth() % i3 != 0 || bitmap.getHeight() % i4 != 0) {
            throw new IllegalArgumentException();
        }
        this.columns = i;
        this.rows = i2;
        this.cellMatrix = new int[i2][i];
        createStaticSet(bitmap, ((bitmap.getWidth() / i3) * (bitmap.getHeight() / i4)) + 1, i3, i4, true);
    }

    public int createAnimatedTile(int i) {
        if (i < 0 || i >= this.numberOfTiles) {
            throw new IndexOutOfBoundsException();
        }
        int[] iArr = this.anim_to_static;
        if (iArr == null) {
            this.anim_to_static = new int[4];
            this.numOfAnimTiles = 1;
        } else if (this.numOfAnimTiles == iArr.length) {
            int[] iArr2 = new int[iArr.length * 2];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.anim_to_static = iArr2;
        }
        int[] iArr3 = this.anim_to_static;
        int i2 = this.numOfAnimTiles;
        iArr3[i2] = i;
        this.numOfAnimTiles = i2 + 1;
        return -i2;
    }

    public void setAnimatedTile(int i, int i2) {
        if (i2 < 0 || i2 >= this.numberOfTiles) {
            throw new IndexOutOfBoundsException();
        }
        int i3 = -i;
        int[] iArr = this.anim_to_static;
        if (iArr == null || i3 <= 0 || i3 >= this.numOfAnimTiles) {
            throw new IndexOutOfBoundsException();
        }
        iArr[i3] = i2;
    }

    public int getAnimatedTile(int i) {
        int i2 = -i;
        int[] iArr = this.anim_to_static;
        if (iArr == null || i2 <= 0 || i2 >= this.numOfAnimTiles) {
            throw new IndexOutOfBoundsException();
        }
        return iArr[i2];
    }

    public void setCell(int i, int i2, int i3) {
        if (i < 0 || i >= this.columns || i2 < 0 || i2 >= this.rows) {
            throw new IndexOutOfBoundsException();
        }
        if (i3 > 0) {
            if (i3 >= this.numberOfTiles) {
                throw new IndexOutOfBoundsException();
            }
        } else if (i3 < 0 && (this.anim_to_static == null || (-i3) >= this.numOfAnimTiles)) {
            throw new IndexOutOfBoundsException();
        }
        this.cellMatrix[i2][i] = i3;
    }

    public int getCell(int i, int i2) {
        if (i < 0 || i >= this.columns || i2 < 0 || i2 >= this.rows) {
            throw new IndexOutOfBoundsException();
        }
        return this.cellMatrix[i2][i];
    }

    public void fillCells(int i, int i2, int i3, int i4, int i5) {
        int i6;
        int i7;
        int i8;
        int i9;
        if (i3 < 0 || i4 < 0) {
            throw new IllegalArgumentException();
        }
        if (i < 0 || i >= (i6 = this.columns) || i2 < 0 || i2 >= (i7 = this.rows) || (i8 = i3 + i) > i6 || (i9 = i4 + i2) > i7) {
            throw new IndexOutOfBoundsException();
        }
        if (i5 > 0) {
            if (i5 >= this.numberOfTiles) {
                throw new IndexOutOfBoundsException();
            }
        } else if (i5 < 0 && (this.anim_to_static == null || (-i5) >= this.numOfAnimTiles)) {
            throw new IndexOutOfBoundsException();
        }
        while (i2 < i9) {
            for (int i10 = i; i10 < i8; i10++) {
                this.cellMatrix[i2][i10] = i5;
            }
            i2++;
        }
    }

    public final int getCellWidth() {
        return this.cellWidth;
    }

    public final int getCellHeight() {
        return this.cellHeight;
    }

    public final int getColumns() {
        return this.columns;
    }

    public final int getRows() {
        return this.rows;
    }

    public void setStaticTileSet(Bitmap bitmap, int i, int i2) {
        if (i < 1 || i2 < 1 || bitmap.getWidth() % i != 0 || bitmap.getHeight() % i2 != 0) {
            throw new IllegalArgumentException();
        }
        setWidthImpl(this.columns * i);
        setHeightImpl(this.rows * i2);
        int width = (bitmap.getWidth() / i) * (bitmap.getHeight() / i2);
        if (width >= this.numberOfTiles - 1) {
            createStaticSet(bitmap, width + 1, i, i2, true);
        } else {
            createStaticSet(bitmap, width + 1, i, i2, false);
        }
    }

    @Override
    public final void paint(Canvas canvas) {
        canvas.getClass();
        if (!this.visible) {
            return;
        }
        int i = this.columns;
        int i2 = this.rows;
        Rect clipBounds = canvas.getClipBounds();
        int i3 = (clipBounds.left - this.x) / this.cellWidth;
        if (i3 <= 0) {
            i3 = 0;
        }
        int i4 = ((this.x + (this.columns * this.cellWidth)) - clipBounds.right) / this.cellWidth;
        if (i4 > 0) {
            i -= i4;
        }
        int i5 = (clipBounds.top - this.y) / this.cellHeight;
        int i6 = i5 > 0 ? i5 : 0;
        int i7 = ((this.y + (this.rows * this.cellHeight)) - clipBounds.bottom) / this.cellHeight;
        if (i7 > 0) {
            i2 -= i7;
        }
        int i8 = this.y;
        int i9 = this.cellHeight * i6;
        while (true) {
            i8 += i9;
            if (i6 >= i2) {
                return;
            }
            int i10 = this.x + (this.cellWidth * i3);
            int i11 = i3;
            while (i11 < i) {
                int animatedTile = this.cellMatrix[i6][i11];
                if (animatedTile != 0) {
                    if (animatedTile < 0) {
                        animatedTile = getAnimatedTile(animatedTile);
                    }
                    Rect rect = this.mSrcRect;
                    int i12 = this.tileSetX[animatedTile];
                    int i13 = this.tileSetY[animatedTile];
                    rect.set(i12, i13, this.cellWidth + i12, this.cellHeight + i13);
                    this.mDestRect.set(i10, i8, this.cellWidth + i10, this.cellHeight + i8);
                    canvas.drawBitmap(this.sourceImage, this.mSrcRect, this.mDestRect, this.mPaint);
                }
                i11++;
                i10 += this.cellWidth;
            }
            i6++;
            i9 = this.cellHeight;
        }
    }

    private void createStaticSet(Bitmap bitmap, int i, int i2, int i3, boolean z) {
        this.cellWidth = i2;
        this.cellHeight = i3;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        this.sourceImage = bitmap;
        this.numberOfTiles = i;
        this.tileSetX = new int[i];
        this.tileSetY = new int[i];
        if (!z) {
            this.rows = 0;
            while (this.rows < this.cellMatrix.length) {
                int length = this.cellMatrix[this.rows].length;
                this.columns = 0;
                while (this.columns < length) {
                    this.cellMatrix[this.rows][this.columns] = 0;
                    this.columns++;
                }
                this.rows++;
            }
            this.anim_to_static = null;
        }
        int tile = 1;
        for (int y = 0; y < height; y += i3) {
            for (int x = 0; x < width; x += i2) {
                this.tileSetX[tile] = x;
                this.tileSetY[tile] = y;
                tile++;
            }
        }
    }
}
