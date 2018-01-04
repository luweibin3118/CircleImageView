package com.lwb.circleimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lwb.circleimageview.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/12/27.
 */
public class CircleImageView extends ImageView {

    /**
     * 圆形
     */
    private static final int SHAPE_CIRCLE_NORMAL = 0;

    /**
     * 多角形
     */
    private static final int SHAPE_MULTI_CORNER = 1;

    /**
     * 菱形
     */
    private static final int SHAPE_DIAMOND = 2;

    private int shape = SHAPE_CIRCLE_NORMAL;

    private int cornCount = 5;

    private Path mPath;

    private Paint mPaint;

    private boolean bezier = true;

    private int borderSize = 0;

    private int borderColor;

    public CircleImageView(Context context) {
        super(context);
        init(null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPath = new Path();
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.circle_view);
            shape = typedArray.getInteger(R.styleable.circle_view_image_type, 0);
            cornCount = typedArray.getInteger(R.styleable.circle_view_corner_number, 5);
            bezier = typedArray.getBoolean(R.styleable.circle_view_smooth_line, false);
            borderSize = typedArray.getInteger(R.styleable.circle_view_border_size, 0);
            borderColor = typedArray.getInteger(R.styleable.circle_view_border_color, 0xff000000);
            if (cornCount <= 4 && cornCount != 3) {
                shape = SHAPE_CIRCLE_NORMAL;
            }
            if (cornCount > 30) {
                cornCount = 30;
            }
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (null != drawable) {
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(borderColor);
            mPaint.setStrokeWidth(borderSize);
            drawBackground(canvas);

            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
                super.onDraw(canvas);
                return;
            }
            final Rect rectSrc = getBitmapCenterRect(bitmap);
            Rect rectDst = new Rect(0 + borderSize, 0 + borderSize, getWidth() - borderSize, getHeight() - borderSize);
            int sc;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            } else {
                sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), mPaint);
            }
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            drawBackground(canvas);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rectSrc, rectDst, mPaint);
            mPaint.setXfermode(null);
            canvas.restoreToCount(sc);
        } else {
            super.onDraw(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        switch (shape) {
            case SHAPE_CIRCLE_NORMAL:
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - borderSize, mPaint);
                break;
            case SHAPE_DIAMOND:
                mPath.moveTo(getWidth() / 2, 0);
                mPath.lineTo(0, getWidth() / 2);
                mPath.lineTo(getWidth() / 2, getWidth());
                mPath.lineTo(getWidth(), getWidth() / 2);
                mPath.close();
                canvas.drawPath(mPath, mPaint);
                break;
            case SHAPE_MULTI_CORNER:
                int a = 360 / cornCount;
                Point centerPoint = new Point(getWidth() / 2, getHeight() / 2);
                int r = (getWidth() / 2) - borderSize;
                Point firstPoint = new Point(getWidth() / 2, 0 + borderSize);
                List<Point> points = new ArrayList<>();
                points.add(firstPoint);

                for (int i = 1; i <= cornCount - 1; i++) {
                    int x = (int) (r * sin(a * i + 180)) + centerPoint.x;
                    int y = (int) (r * cos(a * i + 180)) + centerPoint.y;
                    Point point = new Point(x, y);
                    points.add(point);
                }

                if (points.size() % 2 == 1) {
                    if (bezier) {
                        lineSingleBezier(canvas, points);
                        if (borderSize > 0) {
                            lineSingleBezier(canvas, turnPoints(points));
                        }
                    } else {
                        lineSingleNoBezier(canvas, points);
                        if (borderSize > 0) {
                            lineSingleNoBezier(canvas, turnPoints(points));
                        }
                    }
                } else {
                    if (bezier) {
                        lineDoubleBezier(canvas, points);
                        if (borderSize > 0) {
                            lineDoubleBezier(canvas, turnPoints(points));
                            lineDoubleBezier(canvas, turnPoints(turnPoints(points)));
                        }
                    } else {
                        lineDoubleNoBezier(canvas, points);
                        if (borderSize > 0) {
                            lineDoubleNoBezier(canvas, turnPoints(points));
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private List<Point> turnPoints(List<Point> points) {
        List<Point> copyPoints = new ArrayList<>();
        copyPoints.addAll(points);
        Point first = copyPoints.get(0);
        copyPoints.remove(0);
        copyPoints.add(first);
        return copyPoints;
    }

    private void lineDoubleNoBezier(Canvas canvas, List<Point> points) {
        mPath.reset();
        for (int i = 0; i < points.size(); i = i + 2) {
            if (i == 0) {
                mPath.moveTo(points.get(i).x, points.get(i).y);
            } else {
                mPath.lineTo(points.get(i).x, points.get(i).y);
            }
        }
        mPath.close();
        canvas.drawPath(mPath, mPaint);
        mPath.reset();
        for (int i = 1; i < points.size(); i = i + 2) {
            if (i == 1) {
                mPath.moveTo(points.get(i).x, points.get(i).y);
            } else {
                mPath.lineTo(points.get(i).x, points.get(i).y);
            }
        }
        mPath.close();
        canvas.drawPath(mPath, mPaint);
    }

    private void lineDoubleBezier(Canvas canvas, List<Point> points) {
        mPath.reset();
        for (int i = 0; i < points.size(); i = i + 2) {
            if (i == 0) {
                mPath.moveTo(points.get(i).x, points.get(i).y);
            } else {
                mPath.quadTo(points.get(i - 1).x, points.get(i - 1).y, points.get(i).x, points.get(i).y);
            }
        }
        mPath.quadTo(points.get(points.size() - 1).x, points.get(points.size() - 1).y, points.get(0).x, points.get(0).y);
        canvas.drawPath(mPath, mPaint);
        mPath.reset();
        for (int i = 1; i < points.size(); i = i + 2) {
            if (i == 1) {
                mPath.moveTo(points.get(i).x, points.get(i).y);
            } else {
                mPath.quadTo(points.get(i - 1).x, points.get(i - 1).y, points.get(i).x, points.get(i).y);
            }
        }
        mPath.quadTo(points.get(0).x, points.get(0).y, points.get(1).x, points.get(1).y);
        canvas.drawPath(mPath, mPaint);
    }

    private void lineSingleNoBezier(Canvas canvas, List<Point> points) {
        mPath.reset();
        for (int i = 0; i < points.size(); i = i + 2) {
            if (i == 0) {
                mPath.moveTo(points.get(i).x, points.get(i).y);
            } else {
                mPath.lineTo(points.get(i).x, points.get(i).y);
            }
        }
        for (int i = 1; i < points.size(); i = i + 2) {
            mPath.lineTo(points.get(i).x, points.get(i).y);
        }
        mPath.close();
        canvas.drawPath(mPath, mPaint);
    }

    private void lineSingleBezier(Canvas canvas, List<Point> points) {
        mPath.reset();
        for (int i = 0; i < points.size(); i = i + 2) {
            if (i == 0) {
                mPath.moveTo(points.get(i).x, points.get(i).y);
            } else {
                mPath.quadTo(points.get(i - 1).x, points.get(i - 1).y, points.get(i).x, points.get(i).y);
            }
        }
        for (int i = 1; i < points.size(); i = i + 2) {
            mPath.quadTo(points.get(i - 1).x, points.get(i - 1).y, points.get(i).x, points.get(i).y);
        }
        mPath.quadTo(points.get(points.size() - 1).x, points.get(points.size() - 1).y, points.get(0).x, points.get(0).y);
        canvas.drawPath(mPath, mPaint);
    }

    private float cos(int corner) {
        return (float) Math.cos(corner * Math.PI / 180);
    }

    private float sin(int corner) {
        return (float) Math.sin(corner * Math.PI / 180);
    }

    private Rect getBitmapCenterRect(Bitmap bitmap) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int bitmapSide = Math.min(bitmapWidth, bitmapHeight);
        final Rect rect = new Rect(
                bitmapWidth / 2 - bitmapSide / 2,
                bitmapHeight / 2 - bitmapSide / 2,
                bitmapWidth / 2 + bitmapSide / 2,
                bitmapHeight / 2 + bitmapSide / 2
        );
        return rect;
    }
}
