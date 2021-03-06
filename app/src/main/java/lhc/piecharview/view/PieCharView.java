package lhc.piecharview.view;

import android.content.Context;
import android.content.res.TypedArray;
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

import lhc.piecharview.R;
import lhc.piecharview.bean.Pie;

/**
 * 作者：LHC on 2017/2/7 09:51
 * 描述：饼图控件
 */
public class PieCharView extends View {
    private final static float TOUCH_OFF_SET = 1.1f;
    private final static int BREAKDOWN_MARGIN = 15;
    private final static float DEFAULT_RADIUS = 55;
    private final static float DEFAULT_ROUND_WIDTH = 20;
    private final static float DEFAULT_RECT_WIDTH = 8;
    private final static float DEFAULT_RISK_TXT_SIZE = 70;
    private final static float DEFAULT_BREAK_DOWN_SIZE = 30;
    private int mWidth;
    private int mHeight;
    /**
     * 圆环宽度
     */
    private float mRoundWidth;
    /**
     * 风险类型的字体大小
     */
    private float mRiskTxtSize;
    /**
     * 类目明细文字大小
     */
    private float mBreakdownTxtSize;
    /**
     * 类目明细方块宽度
     */
    private float mRectWidth;
    /**
     * 圆环半径
     */
    private float mStillRadius;
    /**
     * 点击时的圆环半径
     */
    private float mTouchRadius;
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
        initAttrs(context, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieCharView);
        if (typedArray != null) {
            mRoundWidth = typedArray.getDimension(R.styleable.PieCharView_roundWidth, DEFAULT_ROUND_WIDTH);
            mStillRadius = typedArray.getDimension(R.styleable.PieCharView_roundStillRadius, DEFAULT_RADIUS);
            mTouchRadius = typedArray.getDimension(R.styleable.PieCharView_roundTouchRadius, DEFAULT_RADIUS);
            mRectWidth = typedArray.getDimension(R.styleable.PieCharView_typeRectWidth, DEFAULT_RECT_WIDTH);
            mRiskTxtSize = typedArray.getDimension(R.styleable.PieCharView_riskTextSize, DEFAULT_RISK_TXT_SIZE);
            mBreakdownTxtSize = typedArray.getDimension(R.styleable.PieCharView_beakDownTextSize, DEFAULT_BREAK_DOWN_SIZE);
            typedArray.recycle();
        }
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

        int margin = (int) mRoundWidth;//圆环外间距为圆环宽度，防止点击滑动后滑出屏幕

        //设置绘制内圆的矩形
        pieInRectF.left = getPaddingLeft() + mRoundWidth + margin;
        pieInRectF.top = getPaddingTop() + mHeight / 2 - mRoundWidth / 2;
        pieInRectF.right = pieInRectF.left + mRoundWidth + mStillRadius * 2;
        pieInRectF.bottom = pieInRectF.top + mRoundWidth + mStillRadius * 2;
        //设置绘制外圆的矩形
        pieOutRectF.left = pieInRectF.left - mRoundWidth;
        pieOutRectF.top = pieInRectF.top - mRoundWidth;
        pieOutRectF.right = pieInRectF.right + mRoundWidth;
        pieOutRectF.bottom = pieInRectF.bottom + mRoundWidth;

        float gap = (float) ((TOUCH_OFF_SET - 1) * (mRoundWidth / 2) / Math.sqrt(2));
        //设置绘制点击后内圆的矩形
        pieInTouchRectF.left = getPaddingLeft() + margin + mRoundWidth - gap;
        pieInTouchRectF.top = getPaddingTop() + mHeight / 2 - mRoundWidth / 2 - gap;
        pieInTouchRectF.right = pieInTouchRectF.left + gap * 2 + mRoundWidth;
        pieInTouchRectF.bottom = pieInTouchRectF.top + gap * 2 + mRoundWidth;
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
            txtPaint.setTextSize(mRiskTxtSize);
            Paint.FontMetrics fontMetrics = txtPaint.getFontMetrics();
            canvas.save();
            canvas.translate(pieInRectF.centerX(), pieInRectF.centerY());
            canvas.drawText(riskType, 0, (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom, txtPaint);//-20为调整偏移量，由于ascent距离文字顶部有距离，会造成中心点偏差
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
            Paint.FontMetrics fm = txtPaint.getFontMetrics();
            float txtHeight = fm.bottom - fm.top;
            int margin = BREAKDOWN_MARGIN;
            Pie pie;
            for (int i = 0; i < mList.size(); i++) {
                pie = mList.get(i);
                canvas.save();
                float translateX = getPaddingLeft() + 3f / 4 * mWidth;//宽度3/4的位置
                float translateY = getPaddingTop() + mHeight / 2 - txtHeight * mList.size() / 2 + i * (txtHeight + margin);
                int txtY = (int) (mRectWidth / 2 + (fm.bottom - fm.top) / 2 - fm.bottom);
                canvas.translate(translateX, translateY);
                piePaint.setColor(pie.getColor());
                //绘制类目方块
                canvas.drawRect(0, 0, mRectWidth, mRectWidth, piePaint);
                //绘制类目名称
                canvas.drawText(pie.getName(), mRectWidth + margin, txtY, txtPaint);
                //绘制类目百分比
                canvas.drawText(pie.getPer() + "%", mRectWidth + txtPaint.measureText(pie.getName()) + margin, txtY, piePaint);
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
