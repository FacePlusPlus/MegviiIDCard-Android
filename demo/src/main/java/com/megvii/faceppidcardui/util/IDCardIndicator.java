package com.megvii.faceppidcardui.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.megvii.idcard.sdk.IDCard;
import com.megvii.idcard.sdk.IDCard.Card;
import com.megvii.idcard.sdk.IDCard.Faculae;
import com.megvii.idcard.sdk.IDCard.IDCardQuality;
import com.megvii.idcard.sdk.IDCard.Shadow;


public class IDCardIndicator extends View {
	private Rect mShowRect = null;
	private Rect mDrawRect = null;
	private Paint mDrawPaint = null;
	private float IDCARD_RATIO = 856.0f / 540.0f;
	private float CONTENT_RATIO = 1f;
	private float SHOW_CONTENT_RATIO = CONTENT_RATIO * 13.0f / 16.0f;
	private Rect mTmpRect = null;

	private void init() {
		mShowRect = new Rect();
		mDrawRect = new Rect();
		mTmpRect = new Rect();
		mDrawPaint = new Paint();
		mDrawPaint.setDither(true);
		mDrawPaint.setAntiAlias(true);
		mDrawPaint.setStrokeWidth(10);
		mDrawPaint.setStyle(Paint.Style.STROKE);
		mDrawPaint.setColor(0xff0000ff);
	}

	public IDCardIndicator(Context context) {
		super(context);
		init();
	}

	public IDCardIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public IDCardIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int width = right - left;
		int height = bottom - top;

		int centerX = width >> 1;
		int centerY = height >> 1;
		int content_width = 0;
		int content_height = 0;
		if (width / (float) (height) < IDCARD_RATIO) // the view is to high
		{
			content_width = (int) (width * SHOW_CONTENT_RATIO);
			content_height = (int) (content_width / IDCARD_RATIO);
		} else { // the view is too wide
			content_height = (int) (height * SHOW_CONTENT_RATIO);
			content_width = (int) (content_height * IDCARD_RATIO);
		}

		mShowRect.left = centerX - content_width / 2;
		mShowRect.top = centerY - content_height / 2;
		mShowRect.right = centerX + content_width / 2;
		mShowRect.bottom = centerY + content_height / 2;
		getActualRect(width, height);
	
	}

	/**
	 *实际给算法的大小 
	 */
	private void getActualRect(int width, int height){
		
		int content_width = 0;
		int content_height = 0;
		int centerX = width >> 1;
		int centerY = height >> 1;
		
		if (width / (float) (height) < IDCARD_RATIO) // the view is to high
		{
			content_width = (int) (width * CONTENT_RATIO);
			content_height = (int) (content_width / IDCARD_RATIO);
		} else { // the view is too wide
			content_height = (int) (height * CONTENT_RATIO);
			content_width = (int) (content_height * IDCARD_RATIO);
		}
		
		mDrawRect.left = centerX - content_width / 2;
		mDrawRect.top = centerY - content_height / 2;
		mDrawRect.right = centerX + content_width / 2;
		mDrawRect.bottom = centerY + content_height / 2;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		// background
		mDrawPaint.setStyle(Paint.Style.FILL);
		mDrawPaint.setColor(0xa0000000);

		// top
		mTmpRect.set(0, 0, getWidth(), mShowRect.top);
		canvas.drawRect(mTmpRect, mDrawPaint);
		// bottom
		mTmpRect.set(0, mShowRect.bottom, getWidth(), getHeight());
		canvas.drawRect(mTmpRect, mDrawPaint);
		// left
		mTmpRect.set(0, mShowRect.top, mShowRect.left, mShowRect.bottom);
		canvas.drawRect(mTmpRect, mDrawPaint);
		// right
		mTmpRect.set(mShowRect.right, mShowRect.top, getWidth(),
				mShowRect.bottom);
		canvas.drawRect(mTmpRect, mDrawPaint);

		drawViewfinder(canvas);
		
		drawFaculae(canvas);
		super.onDraw(canvas);
	}
	
	public void setiCardQuality(IDCardQuality iCardQuality) {
		this.iCardQuality = iCardQuality;
		faculaeWidth = mDrawRect.width();
		faculaeHeight = mDrawRect.height();
		
		faculaePaint = new Paint();
		faculaePaint.setStrokeWidth(5);

		this.postInvalidate();

	}
	
	private IDCardQuality iCardQuality;
	private Paint faculaePaint;
	private int faculaeWidth, faculaeHeight;
	private void drawFaculae(Canvas canvas){
		if (faculaePaint == null || iCardQuality == null) {
			return;
		}
		Shadow[] Shadows = iCardQuality.Shadows;
		Faculae[] faculaes = iCardQuality.faculaes;
		Card[] cards = iCardQuality.cards;
		faculaePaint.setColor(0xffaa0000);
		Log.e("ceshi", "Shadows.length===" + Shadows.length);
		for (int i = 0; i < Shadows.length; i++) {
			IDCard.PointF[] vertex = Shadows[i].vertex;
			for (int j = 0; j < vertex.length; j++) {
				float startx = vertex[j].x * faculaeWidth;
				float starty = vertex[j].y * faculaeHeight;
				float endx = vertex[(j + 1) % vertex.length].x * faculaeWidth;
				float endy = vertex[(j + 1) % vertex.length].y * faculaeHeight;
				canvas.drawLine(startx, starty + mDrawRect.top, endx, endy + mDrawRect.top, faculaePaint);
			}
		}
		faculaePaint.setColor(0xff00aa00);
		for (int i = 0; i < faculaes.length; i++) {
			IDCard.PointF[] vertex = faculaes[i].vertex;
			for (int j = 0; j < vertex.length; j++) {
				float startx = vertex[j].x * faculaeWidth;
				float starty = vertex[j].y * faculaeHeight;
				float endx = vertex[(j + 1) % vertex.length].x * faculaeWidth;
				float endy = vertex[(j + 1) % vertex.length].y * faculaeHeight;
				canvas.drawLine(startx, starty + mDrawRect.top, endx, endy + mDrawRect.top, faculaePaint);
			}
		}
		
		faculaePaint.setColor(0xff0000aa);
		for (int i = 0; i < cards.length; i++) {
			IDCard.PointF[] vertex = cards[i].vertex;
			for (int j = 0; j < vertex.length; j++) {
				float startx = vertex[j].x * faculaeWidth;
				float starty = vertex[j].y * faculaeHeight;
				float endx = vertex[(j + 1) % vertex.length].x * faculaeWidth;
				float endy = vertex[(j + 1) % vertex.length].y * faculaeHeight;
				canvas.drawLine(startx, starty + mDrawRect.top, endx, endy + mDrawRect.top, faculaePaint);
			}
		}
	}

	private void drawViewfinder(Canvas canvas) {
		int finderColor = 0XFF00D3FF;
		mDrawPaint.setStyle(Paint.Style.STROKE);
		mDrawPaint.setColor(finderColor);
		mDrawPaint.setStrokeWidth(2);
		int length = mShowRect.height() / 16;
		// left top
		canvas.drawLine(mShowRect.left, mShowRect.top, mShowRect.left + length,
				mShowRect.top, mDrawPaint);
		canvas.drawLine(mShowRect.left, mShowRect.top, mShowRect.left,
				mShowRect.top + length, mDrawPaint);

		// right top
		canvas.drawLine(mShowRect.right, mShowRect.top, mShowRect.right
				- length, mShowRect.top, mDrawPaint);
		canvas.drawLine(mShowRect.right, mShowRect.top, mShowRect.right,
				mShowRect.top + length, mDrawPaint);

		// left bottom
		canvas.drawLine(mShowRect.left, mShowRect.bottom, mShowRect.left
				+ length, mShowRect.bottom, mDrawPaint);
		canvas.drawLine(mShowRect.left, mShowRect.bottom, mShowRect.left,
				mShowRect.bottom - length, mDrawPaint);

		// right bottom
		canvas.drawLine(mShowRect.right, mShowRect.bottom, mShowRect.right
				- length, mShowRect.bottom, mDrawPaint);
		canvas.drawLine(mShowRect.right, mShowRect.bottom, mShowRect.right,
				mShowRect.bottom - length, mDrawPaint);

		// 两个角中间的线
		mDrawPaint.setColor(0X20FFFFFF);
		// top
		canvas.drawLine(mShowRect.left + length, mShowRect.top, mShowRect.right
				- length, mShowRect.top, mDrawPaint);
		// left
		canvas.drawLine(mShowRect.left, mShowRect.top + length, mShowRect.left,
				mShowRect.bottom - length, mDrawPaint);
		// right
		canvas.drawLine(mShowRect.right, mShowRect.top + length,
				mShowRect.right, mShowRect.bottom - length, mDrawPaint);
		// bottom
		canvas.drawLine(mShowRect.left + length, mShowRect.bottom,
				mShowRect.right - length, mShowRect.bottom, mDrawPaint);

	}

	public RectF getShowPosition() {
		RectF rectF = new RectF();
		rectF.left = mShowRect.left / (float) getWidth();
		rectF.top = mShowRect.top / (float) getHeight();
		rectF.right = mShowRect.right / (float) getWidth();
		rectF.bottom = mShowRect.bottom / (float) getHeight();
		return rectF;
	}
	public RectF getPosition() {
		RectF rectF = new RectF();
		rectF.left = mDrawRect.left / (float) getWidth();
		rectF.top = mDrawRect.top / (float) getHeight();
		rectF.right = mDrawRect.right / (float) getWidth();
		rectF.bottom = mDrawRect.bottom / (float) getHeight();
		return rectF;
	}
}
