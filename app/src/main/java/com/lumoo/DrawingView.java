package com.lumoo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Paint drawPaint, canvasPaint;
    private int paintColor = Color.WHITE;
    private float brushSize = 10f;
    private float lastBrushSize = 10f;
    private boolean erase = false;

    private List<DrawPath> paths = new ArrayList<>();
    private Path drawPath;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public DrawingView(Context context) {
        super(context);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        // TRANSPARENT BACKGROUND için
        setBackgroundColor(Color.TRANSPARENT);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            // TRANSPARENT bitmap oluştur
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            // Canvas'ı transparent yap
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvasBitmap != null) {
            // Önce arka planı temizle (transparent kalması için)
            canvas.drawColor(Color.TRANSPARENT);
            // Sonra çizimleri çiz
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                if (drawCanvas != null) {
                    drawCanvas.drawPath(drawPath, drawPaint);
                    paths.add(new DrawPath(new Path(drawPath), new Paint(drawPaint)));
                }
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setBrushSize(float newSize) {
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize = lastSize;
    }

    public float getLastBrushSize() {
        return lastBrushSize;
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
        }
    }

    public void setPaintColor(int newColor) {
        invalidate();
        paintColor = newColor;
        drawPaint.setColor(paintColor);
    }

    public void clearCanvas() {
        if (drawCanvas != null) {
            // Canvas'ı tamamen temizle (transparent yap)
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        paths.clear();
        invalidate();
    }

    public void undoLastPath() {
        if (paths.size() > 0) {
            paths.remove(paths.size() - 1);
            redrawCanvas();
        }
    }

    private void redrawCanvas() {
        if (drawCanvas != null) {
            // Önce canvas'ı temizle
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            // Sonra tüm yolları yeniden çiz
            for (DrawPath path : paths) {
                drawCanvas.drawPath(path.path, path.paint);
            }
            invalidate();
        }
    }

    public Bitmap getDrawingBitmap() {
        return canvasBitmap;
    }

    public boolean hasDrawing() {
        return paths.size() > 0;
    }

    private static class DrawPath {
        public Path path;
        public Paint paint;

        public DrawPath(Path path, Paint paint) {
            this.path = new Path(path);
            this.paint = new Paint(paint);
        }
    }
}