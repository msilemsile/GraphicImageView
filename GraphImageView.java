import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Graph ImageView
 */
public class GraphImageView extends View {

    public static final int GRAPH_CIRCLE = 1;
    public static final int GRAPH_CORNER = 2;
    public static final int GRAPH_TRIANGLE = 3;
    public static final int GRAPH_PATH = 4;
    private int mGraph;
    private int mCornerRadius;
    private int mLeftTopCorner, mRightTopCorner, mLeftBottomCorner, mRightBottomCorner;

    private Path mPath = new Path();
    private RectF mRectF = new RectF();
    private float[] eightFloats = new float[8];
    private Bitmap mGraphBitmap;
    private int mResId;
    private BitmapFactory.Options mOption = new BitmapFactory.Options();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private BitmapShader mBitmapShader;

    public GraphImageView(Context context) {
        this(context, null);
    }

    public GraphImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mOption.inPurgeable = true;
    }

    public void setImageResource(int resId) {
        if (mGraphBitmap != null) {
            mGraphBitmap.recycle();
            mGraphBitmap = null;
        }
        mResId = resId;
        mOption.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, mOption);
        mOption.inJustDecodeBounds = false;
        postInvalidate();
    }

    /**
     * 圆形
     */
    public void asCircleGraph() {
        mGraph = GRAPH_CIRCLE;
        postInvalidate();
    }

    /**
     * 圆角
     */
    public void asCornerGraph(int cornerRadius) {
        mGraph = GRAPH_CORNER;
        mCornerRadius = cornerRadius;
        postInvalidate();
    }

    /**
     * 圆角
     */
    public void asCornerGraph(int leftTopCorner, int rightTopCorner, int leftBottomCorner, int rightBottomCorner) {
        mGraph = GRAPH_CORNER;
        mLeftTopCorner = leftTopCorner;
        mLeftBottomCorner = leftBottomCorner;
        mRightTopCorner = rightTopCorner;
        mRightBottomCorner = rightBottomCorner;
        postInvalidate();
    }

    /**
     * 三角形
     */
    public void asTriangle() {
        mGraph = GRAPH_TRIANGLE;
        postInvalidate();
    }

    /**
     * 任意闭合图形
     */
    public void asPath(Path path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        mGraph = GRAPH_PATH;
        mPath = path;
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mGraph != 0 && mResId != 0) {
            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
            mOption.inSampleSize = calculateInSampleSize(width, height);
            if (mGraphBitmap == null) {
                mGraphBitmap = BitmapFactory.decodeResource(getResources(), mResId, mOption);
            }
            if (mGraphBitmap == null) {
                return;
            }
            if (mBitmapShader == null) {
                mBitmapShader = new BitmapShader(mGraphBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                mPaint.setShader(mBitmapShader);
            }
            if (mGraph != GRAPH_PATH) {
                mPath.reset();
            }
            switch (mGraph) {
                case GRAPH_CIRCLE:
                    int halfWidth = width / 2;
                    int halfHeight = height / 2;
                    int radius = halfWidth > halfHeight ? halfHeight : halfWidth;
                    mPath.addCircle(halfWidth, halfHeight, radius, Path.Direction.CW);
                    break;
                case GRAPH_CORNER:
                    if (mRectF.isEmpty()) {
                        mRectF.set(0, 0, getWidth(), getHeight());
                    }
                    if (mCornerRadius > 0) {
                        for (int i = 0; i < eightFloats.length; i++) {
                            eightFloats[i] = mCornerRadius;
                        }
                    } else {
                        eightFloats[0] = mLeftTopCorner;
                        eightFloats[1] = mLeftTopCorner;
                        eightFloats[2] = mRightTopCorner;
                        eightFloats[3] = mRightTopCorner;
                        eightFloats[4] = mLeftBottomCorner;
                        eightFloats[5] = mLeftBottomCorner;
                        eightFloats[6] = mRightBottomCorner;
                        eightFloats[7] = mRightBottomCorner;
                    }
                    mPath.addRoundRect(mRectF, eightFloats, Path.Direction.CW);
                    break;
                case GRAPH_TRIANGLE:
                    mPath.moveTo(width / 2, 0);
                    mPath.lineTo(width, height);
                    mPath.lineTo(0, height);
                    mPath.close();
                    break;
                case GRAPH_PATH:
                    break;
            }
            canvas.drawPath(mPath, mPaint);
        }
    }

    private int calculateInSampleSize(int reqWidth, int reqHeight) {
        mOption.inScaled = true;
        final int height = mOption.outHeight;
        final int width = mOption.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
