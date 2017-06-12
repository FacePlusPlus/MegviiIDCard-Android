package com.megvii.faceppidcardui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.megvii.faceppidcardui.util.Screen;
import com.megvii.idcard.sdk.IDCard.Card;
import com.megvii.idcard.sdk.IDCard.Faculae;
import com.megvii.idcard.sdk.IDCard.IDCardQuality;
import com.megvii.idcard.sdk.IDCard.PointF;
import com.megvii.idcard.sdk.IDCard.Shadow;

public class MyView extends View {

	private IDCardQuality iCardQuality;
	private Bitmap bitmap;
	float[] points;
	private Paint paint;
	private int width, height;

	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setiCardQuality(Bitmap bitmap, IDCardQuality iCardQuality) {
		this.iCardQuality = iCardQuality;
		width = Screen.mWidth;
		float scale = bitmap.getWidth() * 1.0f / width;
		height = (int) (bitmap.getHeight() / scale);
		this.bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
		paint = new Paint();
		paint.setStrokeWidth(5);

		this.postInvalidate();

	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (bitmap == null || iCardQuality == null) {
			super.onDraw(canvas);
			return;
		}
		canvas.drawBitmap(bitmap, 0, 20, null);
		Shadow[] Shadows = iCardQuality.Shadows;
		Faculae[] faculaes = iCardQuality.faculaes;
		Card[] cards = iCardQuality.cards;
		paint.setColor(0xffaa0000);
		Log.e("ceshi", "Shadows.length===" + Shadows.length);
		for (int i = 0; i < Shadows.length; i++) {
			PointF[] vertex = Shadows[i].vertex;
			Log.w("ceshi", "Shadows[" + i + "].average" + Shadows[i].average[0] + "," + Shadows[i].average[1] + ","
					+ Shadows[i].average[2]);
			Log.w("ceshi", "Shadows[" + i + "].variance" + Shadows[i].variance[0] + "," + Shadows[i].variance[1] + ","
					+ Shadows[i].variance[2]);
			sqrt(Shadows[i].variance[0], Shadows[i].variance[1], Shadows[i].variance[2], "Shadows");
//			if(!isContinueShadowDraw(cards[0].average, cards[0].variance, Shadows[i].average, Shadows[i].variance))
//				continue;
			for (int j = 0; j < vertex.length; j++) {
				float startx = vertex[j].x * width;
				float starty = vertex[j].y * height;
				float endx = vertex[(j + 1) % vertex.length].x * width;
				float endy = vertex[(j + 1) % vertex.length].y * height;
				canvas.drawLine(startx, starty, endx, endy, paint);
			}
		}
		Log.e("ceshi", "faculaes.length===" + faculaes.length);
		paint.setColor(0xff00aa00);
		for (int i = 0; i < faculaes.length; i++) {
			PointF[] vertex = faculaes[i].vertex;
			Log.w("ceshi", "faculaes[" + i + "].average" + faculaes[i].average[0] + "," + faculaes[i].average[1] + ","
					+ faculaes[i].average[2]);
			Log.w("ceshi", "faculaes[" + i + "].variance" + faculaes[i].variance[0] + "," + faculaes[i].variance[1]
					+ "," + faculaes[i].variance[2]);
			sqrt(faculaes[i].variance[0], faculaes[i].variance[1], faculaes[i].variance[2], "faculaes");
//			if(!isContinueDraw(cards[0].average, cards[0].variance, faculaes[i].average, faculaes[i].variance))
//				continue;
			for (int j = 0; j < vertex.length; j++) {
				float startx = vertex[j].x * width;
				float starty = vertex[j].y * height;
				float endx = vertex[(j + 1) % vertex.length].x * width;
				float endy = vertex[(j + 1) % vertex.length].y * height;
				canvas.drawLine(startx, starty, endx, endy, paint);
			}
		}
		
		paint.setColor(0xff0000aa);
		for (int i = 0; i < cards.length; i++) {
			PointF[] vertex = cards[i].vertex;
			Log.w("ceshi", "cards[" + i + "].average" + cards[i].average[0] + "," + cards[i].average[1] + ","
					+ cards[i].average[2]);
			Log.w("ceshi", "cards[" + i + "].variance" + cards[i].variance[0] + "," + cards[i].variance[1] + ","
					+ cards[i].variance[2]);
			sqrt(cards[i].variance[0], cards[i].variance[1], cards[i].variance[2], "card");
			for (int j = 0; j < vertex.length; j++) {
				float startx = vertex[j].x * width;
				float starty = vertex[j].y * height;
				float endx = vertex[(j + 1) % vertex.length].x * width;
				float endy = vertex[(j + 1) % vertex.length].y * height;
				canvas.drawLine(startx, starty, endx, endy, paint);
			}
		}
	}
	
	private boolean isContinueDraw(float[] cardAverage, float[] cardVariance, float[] valueAverage, float[] valueVariance){
		for (int i = 0; i < 3; i++) {
			Log.w("ceshi", "isContinueDraw:::" + cardAverage[i] + ", " + (float) Math.sqrt(cardVariance[i]) * 0.5 + "," + valueAverage[i]);
			if ((cardAverage[i] + (float) (Math.sqrt(cardVariance[i]) * 0.5)) < valueAverage[i])
				return true;
			if (cardVariance[i] < valueVariance[i])
				return true;
			
		}
		return false;
	}
	private boolean isContinueShadowDraw(float[] cardAverage, float[] cardVariance, float[] valueAverage, float[] valueVariance){
		for (int i = 0; i < 3; i++) {
			Log.w("ceshi", "isContinueDraw:::" + cardAverage[i] + ", " + (float) Math.sqrt(cardVariance[i]) * 0.8 + "," + valueAverage[i]);
			if ((cardAverage[i] - (float) (Math.sqrt(cardVariance[i]) *0.8 )) > valueAverage[i])
				return true;
//			if (cardVariance[i] < valueVariance[i])
//				return true;
			
		}
		return false;
	}

	private void sqrt(float value0, float value1, float value2, String tag) {
		float result0 = (float) Math.sqrt(value0);
		float result1 = (float) Math.sqrt(value1);
		float result2 = (float) Math.sqrt(value2);
		float mean = (result0 + result1 + result2) / 3.0f;
		Log.w("ceshi", tag + "+++mean===" + mean);
	}
}