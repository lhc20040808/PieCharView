package lhc.piecharview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import lhc.piecharview.bean.Pie;

/**
 * 作者：LHC on 2017/2/7 09:51
 * 描述：饼图控件
 */
public class PieCharView extends View {
    private final static int BREAKDOWN_MARGIN = 15;
    private final static float TOUCH_OFF_SET = 1.1f;
    private int mWidth;
    private int mHeight;
    private int mRoundWidth = 60;//圆环宽度
    private int mRiskTypeTxtSize = 70;//风险类型字体大小
    private int mBreakdownTxtSize = 45;//类目明细文字大小
    private int mRectWidth = 30;//类目明细方块宽度
    private Paint piePaint, txtPaint;
    private RectF pieInRectF, pieOutRectF, pieInTouchRectF, pieOutTouchRectF;
    private List<Pie> mList;
    private int[] basePieColors = new int[]{Color.parseColor("#FDA890"), Color.parseColor("#ABD1FD")
            , Color.parseColor("#FEC976"), Color.parseColor("#4ab3fd"), Color.parseColor("#ffc100")};
    private String riskType;
    private float downX, downY;
    private boolean touchFlag;//触摸响应标志位

    public PieCharView(Context context) {
        this(context, null);
    }

    public PieCharView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieCharView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setStyle(Paint.Style.STROKE);
        txtPaint.setTextAlign(Paint.Align.CENTER);

        pieInRectF = new RectF();
        pieOutRectF = new RectF();
        pieOutTouchRectF = new RectF();
        pieInTouchRectF = new RectF();

        mList = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - getPaddingLeft() - getPaddingRight();
        mHeight = h - getPaddingTop() - getPaddingBottom();

        int diameter = Math.min(mWidth / 2 - mRoundWidth * 2, mHeight - mRoundWidth * 2);
        int margin = mRoundWidth;//圆环外间距为圆环宽度，防止点击滑动后滑出屏幕

        //设置绘制内圆的矩形
        pieInRectF.left = getPaddingLeft() + mRoundWidth + margin;
        pieInRectF.top = getPaddingTop() + mHeight / 2 - diameter / 2;
        pieInRectF.right = pieInRectF.left + diameter;
        pieInRectF.bottom = pieInRectF.top + diameter;
        //设置绘制外圆的矩形
        pieOutRectF.left = pieInRectF.left - mRoundWidth;
        pieOutRectF.top = pieInRectF.top - mRoundWidth;
        pieOutRectF.right = pieInRectF.right + mRoundWidth;
        pieOutRectF.bottom = pieInRectF.bottom + mRoundWidth;

        float gap = (float) ((TOUCH_OFF_SET - 1) * (diameter / 2) / Math.sqrt(2));
        //设置绘制点击后内圆的矩形
        pieInTouchRectF.left = getPaddingLeft() + margin + mRoundWidth - gap;
        pieInTouchRectF.top = getPaddingTop() + mHeight / 2 - diameter / 2 - gap;
        pieInTouchRectF.right = pieInTouchRectF.left + gap * 2 + diameter;
        pieInTouchRectF.bottom = pieInTouchRectF.top + gap * 2 + diameter;
        //设置绘制点击后圆的矩形
        pieOutTouchRectF.left = pieInTouchRectF.left - mRoundWidth;
        pieOutTouchRectF.top = pieInTouchRectF.top - mRoundWidth;
        pieOutTouchRectF.right = pieInTouchRectF.right + mRoundWidth;
        pieOutTouchRectF.bottom = pieInTouchRectF.bottom + mRoundWidth;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPie(canvas);
        drawRiskType(canvas);
        drawBreakdown(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                for (Pie pie : mList) {
                    pie.setTouch(false);
                    if (pie.isInRegion(downX, downY)) {
                        pie.setTouch(!pie.isTouch());
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 绘制饼图
     */
    private void drawPie(Canvas canvas) {
        int startAngle = -90;//起始角度
        Pie item;
        //根据不同SDK版本采用不同的绘制方案，需要调整paint
        if (Build.VERSION.SDK_INT >= 19) {
            piePaint.setStrokeWidth(1);
            piePaint.setStyle(Paint.Style.FILL);
        } else {
            piePaint.setStrokeWidth(mRoundWidth);
            piePaint.setStyle(Paint.Style.STROKE);
        }

        for (int i = 0; i < mList.size(); i++) {
            item = mList.get(i);
            //获取单个类目所占比例
            int per = item.getPer();
            //计算所占比例对应的角度
            int sweepAngle = (int) ((float) per / (float) getSum() * 360);
            //设置颜色，之后绘制类目明细还要使用
            item.setColor(basePieColors[i % basePieColors.length]);
            piePaint.setColor(item.getColor());
            //绘制弧形
            if (Build.VERSION.SDK_INT >= 19) {
                Path path = null;
                if (item.isTouch()) {
                    path = getArcPath(pieInTouchRectF, pieOutTouchRectF, startAngle, sweepAngle);
                } else {
                    path = getArcPath(pieInRectF, pieOutRectF, startAngle, sweepAngle);
                }
                canvas.drawPath(path, piePaint);
                item.setRegion(path);
            } else {
                Path path = new Path();
                path.addArc(pieInRectF, startAngle, sweepAngle);
                canvas.drawPath(path, piePaint);
            }
            //计算起始角度
            startAngle += sweepAngle;
        }
    }

    /**
     * 获取绘制弧度所需要的path
     *
     * @param in
     * @param out
     * @param startAngle
     * @param angle
     * @return
     */
    private Path getArcPath(RectF in, RectF out, int startAngle, int angle) {
        Path path1 = new Path();
        path1.moveTo(in.centerX(), in.centerY());
        path1.arcTo(in, startAngle, angle);
        Path path2 = new Path();
        path2.moveTo(out.centerX(), out.centerY());
        path2.arcTo(out, startAngle, angle);
        Path path = new Path();
        path.op(path2, path1, Path.Op.DIFFERENCE);
        return path;
    }

    /**
     * 绘制风险类型
     */
    private void drawRiskType(Canvas canvas) {
        if (riskType != null && !"".equals(riskType)) {
            txtPaint.setTextAlign(Paint.Align.CENTER);
            txtPaint.setTextSize(mRiskTypeTxtSize);
            Paint.FontMetrics fontMetrics = txtPaint.getFontMetrics();
            canvas.save();
            canvas.translate(pieInRectF.centerX(), pieInRectF.centerY());
            canvas.drawText(riskType, 0, (fontMetrics.descent - fontMetrics.ascent - 20) / 2, txtPaint);//-20为调整偏移量，由于ascent距离文字顶部有距离，会造成中心点偏差
            canvas.restore();
        }
    }

    /**
     * 绘制细目列表
     */
    private void drawBreakdown(Canvas canvas) {
        if (mList != null && mList.size() > 0) {
            piePaint.setStyle(Paint.Style.FILL);
            piePaint.setTextSize(mBreakdownTxtSize);

            txtPaint.setTextAlign(Paint.Align.LEFT);
            txtPaint.setTextSize(mBreakdownTxtSize);
            Paint.FontMetrics fontMetrics = txtPaint.getFontMetrics();
            float txtHeight = fontMetrics.descent - fontMetrics.ascent;
            int margin = BREAKDOWN_MARGIN;

            Pie pie;
            for (int i = 0; i < mList.size(); i++) {
                pie = mList.get(i);
                canvas.save();
                float translateX = getPaddingLeft() + 3f / 4 * mWidth;//宽度3/4的位置
                float translateY = getPaddingTop() + mHeight / 2 - txtHeight * mList.size() / 2 + i * (txtHeight + margin);
                canvas.translate(translateX, translateY);
                piePaint.setColor(pie.getColor());
                //绘制类目方块
                canvas.drawRect(0, 0, mRectWidth, mRectWidth, piePaint);
                //绘制类目名称
                canvas.drawText(pie.getName(), mRectWidth, txtHeight - 20, txtPaint);//-20为文字高度偏移量
                //绘制类目百分比
                canvas.drawText(pie.getPer() + "%", mRectWidth + txtPaint.measureText(pie.getName()), txtHeight - 20, piePaint);
                canvas.restore();
            }

        }
    }

    /**
     * 获取总数
     *
     * @return
     */
    private int getSum() {
        int sum = 0;
        for (Pie pie : mList) {
            sum += pie.getPer();
        }
        return sum;
    }

    public PieCharView setRiskType(String riskType) {
        this.riskType = riskType;
        return this;
    }

    public PieCharView addPie(Pie pie) {
        if (pie != null) {
            mList.add(pie);
        }
        return this;
    }

    public PieCharView addPie(String name, int per) {
        Pie pie = new Pie();
        pie.setName(name);
        pie.setPer(per);
        mList.add(pie);
        return this;
    }

    public void draw() {
        invalidate();
    }

}
