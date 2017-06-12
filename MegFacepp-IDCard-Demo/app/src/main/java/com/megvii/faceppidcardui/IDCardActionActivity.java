package com.megvii.faceppidcardui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.megvii.faceppidcardui.util.ConUtil;
import com.megvii.faceppidcardui.util.DialogUtil;
import com.megvii.faceppidcardui.util.Screen;
import com.megvii.faceppidcardui.util.Util;

import static android.os.Build.VERSION_CODES.M;

public class IDCardActionActivity extends Activity implements View.OnClickListener {

	private boolean isHorizontal, isTextDetect, isDebug, isClearShadow;
	private TextView[] editItemTexts;
	private float[] editValues = { 0.8f, 0.1f, 0.8f };
	private String[] imageItemTexts = { "文字识别", "阴影过滤", "调试信息", "竖屏" };
	private String[] editItemStrs = { "InBound", "IsCard", "Clear" };
	private int[] imageItemImages_gray = { com.megvii.faceppidcard.R.drawable.textrec_gray, com.megvii.faceppidcard.R.drawable.shader_gray, com.megvii.faceppidcard.R.drawable.debug_gray,
			com.megvii.faceppidcard.R.drawable.verticalscreen };
	private int[] imageItemImages_blue = { com.megvii.faceppidcard.R.drawable.textrec_blue, com.megvii.faceppidcard.R.drawable.shader_blue, com.megvii.faceppidcard.R.drawable.debug_blue,
			com.megvii.faceppidcard.R.drawable.horizontalscreen };
	private RelativeLayout[] imageItem_Rels;
	private RelativeLayout[] textItem_Rels;
	private DialogUtil mDialogUtil;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.megvii.faceppidcard.R.layout.activity_idcardaction);
		Screen.initialize(this);
		init();
		initData();
		requestCameraPerm();
	}

	private void init() {
		Util.API_KEY = getIntent().getStringExtra("key");
		Util.API_SECRET = getIntent().getStringExtra("secret");
		mDialogUtil = new DialogUtil(this);
		TextView title = (TextView) findViewById(com.megvii.faceppidcard.R.id.title_layout_titleText);
		title.setText("IDCard 功能演示");
		findViewById(com.megvii.faceppidcard.R.id.title_layout_returnRel).setOnClickListener(this);
		findViewById(com.megvii.faceppidcard.R.id.activity_rootRel).setOnClickListener(this);
		findViewById(com.megvii.faceppidcard.R.id.idcardAction_enterBtn).setOnClickListener(this);
		RelativeLayout rel0 = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcardAction_imageitem_0);
		RelativeLayout rel1 = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcardAction_imageitem_1);
		RelativeLayout rel2 = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcardAction_imageitem_2);
		RelativeLayout rel3 = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcardAction_imageitem_3);
		imageItem_Rels = new RelativeLayout[] { rel0, rel1, rel2, rel3 };
		RelativeLayout textRel0 = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcardAction_edititem_0);
		RelativeLayout textRel1 = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcardAction_edititem_1);
		RelativeLayout textRel2 = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcardAction_edititem_2);
		textItem_Rels = new RelativeLayout[] { textRel0, textRel1, textRel2 };
	}

	private void initData() {
		for (int i = 0; i < imageItem_Rels.length; i++) {
			imageItem_Rels[i].setOnClickListener(this);
			ImageView image = (ImageView) imageItem_Rels[i].findViewById(com.megvii.faceppidcard.R.id.image_item_image);
			image.setImageResource(imageItemImages_gray[i]);
			TextView text = (TextView) imageItem_Rels[i].findViewById(com.megvii.faceppidcard.R.id.image_item_text);
			text.setText(imageItemTexts[i]);
			text.setTextColor(0XFFD0D0D0);
			if (i == 3)
				text.setTextColor(0XFF30364C);
		}

		editItemTexts = new TextView[3];
		for (int i = 0; i < textItem_Rels.length; i++) {
			textItem_Rels[i].setOnClickListener(this);
			TextView text = (TextView) textItem_Rels[i].findViewById(com.megvii.faceppidcard.R.id.edit_item_text);
			text.setText(editItemStrs[i]);
			editItemTexts[i] = (TextView) textItem_Rels[i].findViewById(com.megvii.faceppidcard.R.id.edit_item_edit);
			String str = editValues[i] + "";
			editItemTexts[i].setText(str);
			mDialogUtil.setTextSzie(editItemTexts[i], str.length());
		}
	}

	private void requestCameraPerm() {
		if (android.os.Build.VERSION.SDK_INT >= M) {
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.CAMERA)
					!= PackageManager.PERMISSION_GRANTED) {
				//进行权限请求
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.CAMERA},
						EXTERNAL_STORAGE_REQ_CAMERA_CODE);
			}
		}
	}

	public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;


	private void onclickImageItem(int index, boolean isSelect) {
		ImageView image = (ImageView) imageItem_Rels[index].findViewById(com.megvii.faceppidcard.R.id.image_item_image);
		TextView text = (TextView) imageItem_Rels[index].findViewById(com.megvii.faceppidcard.R.id.image_item_text);
		if (isSelect) {
			image.setImageResource(imageItemImages_blue[index]);
			text.setTextColor(0XFF30364C);
		} else {
			image.setImageResource(imageItemImages_gray[index]);
			text.setTextColor(0XFFD0D0D0);
		}

		if (index == 3) {
			text.setTextColor(0XFF30364C);
			if (isSelect)
				text.setText("横屏");
			else
				text.setText("竖屏");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		int ID = v.getId();
		if(ID == com.megvii.faceppidcard.R.id.activity_rootRel){
			ConUtil.isGoneKeyBoard(this);
		} else if(ID == com.megvii.faceppidcard.R.id.title_layout_returnRel){
			finish();
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_edititem_0){
			mDialogUtil.showEditText(editItemTexts[0], 2);
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_edititem_1){
			mDialogUtil.showEditText(editItemTexts[1], 3);
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_edititem_2){
			mDialogUtil.showEditText(editItemTexts[2], 4);
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_imageitem_0){
			isTextDetect = !isTextDetect;
			onclickImageItem(0, isTextDetect);
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_imageitem_1){
			isClearShadow = !isClearShadow;
			onclickImageItem(1, isClearShadow);
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_imageitem_2){
			isDebug = !isDebug;
			onclickImageItem(2, isDebug);
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_imageitem_3){
			isHorizontal = !isHorizontal;
			onclickImageItem(3, isHorizontal);
		} else if(ID == com.megvii.faceppidcard.R.id.idcardAction_enterBtn){
			Intent intent = new Intent(this, IDCardScanActivity.class);
			float[] editValue = new float[3];
			for (int i = 0; i < editItemTexts.length; i++) {
				editValue[i] = Float.parseFloat(editItemTexts[i].getText().toString());
			}
			intent.putExtra("isvertical", !isHorizontal);
			intent.putExtra("isClearShadow", isClearShadow);
			intent.putExtra("isTextDetect", isTextDetect);
			intent.putExtra("isDebug", isDebug);
			intent.putExtra("bound", editValue[0]);
			intent.putExtra("idcard", editValue[1]);
			intent.putExtra("clear", editValue[2]);
			startActivityForResult(intent, INTO_IDCARDSCAN_PAGE);
		}
	}

	private static final int INTO_IDCARDSCAN_PAGE = 100;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == INTO_IDCARDSCAN_PAGE && resultCode == RESULT_OK) {
//			Intent intent = new Intent(this, IDCardResultActivity.class);
//			intent.putExtra("points", data.getFloatArrayExtra("points"));
//			intent.putExtra("path", data.getStringExtra("path"));
//			intent.putExtra("iCardQuality", data.getSerializableExtra("iCardQuality"));
//			intent.putExtra("isClearShadow", data.getSerializableExtra("isClearShadow"));
//			intent.putExtra("isTextDetect", data.getSerializableExtra("isTextDetect"));
//			intent.putExtra("in_bound", data.getFloatExtra("in_bound", 0.8f));
//			intent.putExtra("is_idcard", data.getFloatExtra("is_idcard", 0.5f));
//			intent.putExtra("clear", data.getFloatExtra("clear", 0.8f));
//
//			startActivity(intent);
		}
	}
}
