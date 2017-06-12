package com.megvii.faceppidcardui.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class DialogUtil {

	private Activity activity;

	public DialogUtil(Activity activity) {
		this.activity = activity;
	}

	public void showUpdate(final String apkURL) {
		Builder builder = new Builder(activity);
		builder.setTitle("版本更新");
		builder.setMessage("发现新版本，是否需要更新？");
		builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(activity, DownloadService.class);
				intent.putExtra("url", apkURL);
				intent.putExtra("contentName", "MegviiCloud");
				activity.startService(intent);
			}
		}).setNegativeButton("取消", null);
		final AlertDialog dilaog = builder.create();
		dilaog.show();
	}

	public void showEditText(final TextView text, final int index) {
		AlertDialog.Builder builder = new Builder(activity);
		builder.setTitle(getTitle(index)); // 设置对话框标题
		builder.setIcon(android.R.drawable.btn_star); // 设置对话框标题前的图标
		LayoutParams tvLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		final EditText edit = new EditText(activity);
		edit.setLayoutParams(tvLp);
		edit.setText(text.getText().toString());
		edit.setSelection(text.getText().toString().length());

		final InputMethodManager imm = (InputMethodManager) edit.getContext()
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);

		builder.setView(edit);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String str = edit.getText().toString();
				if (isNum(str)) {
					ConUtil.showToast(activity, "请输入数字！");
					return;
				} else {
					try {
						String value = getContent(str, index);
						setTextSzie(text, value.length());
						text.setText(value);
					} catch (Exception e) {
						ConUtil.showToast(activity, "请输入数字！");
					}
				}

				// 取消重命名时候隐藏软键盘
				imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 取消重命名时候隐藏软键盘
				imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
			}
		});

		builder.setCancelable(true); // 设置按钮是否可以按返回键取消,false则不可以取消
		AlertDialog dialog = builder.create(); // 创建对话框
		dialog.setCanceledOnTouchOutside(true); // 设置弹出框失去焦点是否隐藏,即点击屏蔽其它地方是否隐藏
		dialog.show();
	}

	class ChoiceOnClickListener implements DialogInterface.OnClickListener {

		private int which = 0;

		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			this.which = which;
		}

		public int getWhich() {
			return which;
		}
	}

	public void showDialog(String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(activity).setTitle(message)
				.setNegativeButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
					}
				}).setCancelable(false).create();
		alertDialog.show();
	}

	public void onDestory() {
		activity = null;
	}

	public DoialogCameraListener mDoialogUtilListener;

	public void setDoialogCameraListener(DoialogCameraListener mDoialogUtilListener) {
		this.mDoialogUtilListener = mDoialogUtilListener;
	}

	public interface DoialogCameraListener {
		public void intoCamera(int index);

		public void intoPicture(int index);
	}

	public static void setTextSzie(TextView text, int num) {
		if (num < 3)
			text.setTextSize(20);
		else if (num < 5)
			text.setTextSize(18);
		else if (num < 7)
			text.setTextSize(16);
		else if (num < 9)
			text.setTextSize(14);
		else if (num >= 9)
			text.setTextSize(12);
	}

	public String getTitle(int index) {
		String title = "请输入";
		switch (index) {
		case 0:
			title = "最小值是33\n最大值是2147483647";
			break;
		case 1:
			title = "最小值是1\n最大值是2147483647";
			break;
		case 2:
			title = "最小值是0\n最大值是1          ";
			break;
		case 3:
			title = "最小值是0\n最大值是1          ";
			break;
		case 4:
			title = "最小值是0\n最大值是1          ";
			break;
		}
		return title;
	}

	public String getContent(String str, int index) {
		String content = str;
		switch (index) {
		case 0:
			long faceSize = (long) Float.parseFloat(content);
			if (faceSize < 33)
				faceSize = 33;
			else if (faceSize > 2147483647)
				faceSize = 2147483647;

			content = faceSize + "";
			break;
		case 1:
			long interval = (long) Float.parseFloat(content);
			if (interval < 1)
				interval = 1;
			else if (interval > 2147483647)
				interval = 2147483647;

			content = interval + "";
			break;
		case 2:
		case 3:
		case 4:
			float vlaue = Float.parseFloat(content);
			if (vlaue < 0)
				vlaue = 0;
			else if (vlaue > 1)
				vlaue = 1;

			content = vlaue + "";
			break;
		}
		return content;
	}

	public boolean isNum(String str) {
		String reg = "[a-zA-Z]+";
		return str.matches(reg);
	}
}
