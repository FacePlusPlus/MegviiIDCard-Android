package com.megvii.faceppidcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.megvii.faceppidcard.util.KeyUtil;
import com.megvii.faceppidcardui.IDCardActionActivity;
import com.megvii.faceppidcardui.util.ConUtil;
import com.megvii.faceppidcardui.util.DialogUtil;
import com.megvii.idcard.sdk.IDCard;
import com.megvii.licensemanager.sdk.LicenseManager;

public class LoadingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoadingActivity";
    private LinearLayout barLinear;
    private TextView WarrantyText;
    private ProgressBar WarrantyBar;
    private Button againWarrantyBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        init();
        initData();
        network();
    }

    private void init() {
        findViewById(R.id.load_rootRel).setOnClickListener(this);
        barLinear = (LinearLayout) findViewById(R.id.loading_layout_barLinear);
        WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
        WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
        againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
        againWarrantyBtn.setOnClickListener(this);
    }

    private void initData() {
        if (KeyUtil.API_KEY == null || KeyUtil.API_SECRET == null) {
            if (!KeyUtil.isReadKey(this)) {
                DialogUtil mDialogUtil = new DialogUtil(this);
                mDialogUtil.showDialog("请填写API_KEY和API_SECRET");
            }
        }
    }

    private void network() {
        barLinear.setVisibility(View.VISIBLE);
        againWarrantyBtn.setVisibility(View.GONE);
        WarrantyText.setText("正在联网授权中...");
        WarrantyBar.setVisibility(View.VISIBLE);


        final LicenseManager licenseManager = new LicenseManager(this);
        licenseManager.setExpirationMillis(IDCard.getApiExpication(this) * 1000);
        // licenseManager.setAgainRequestTime(againRequestTime);

        String uuid = ConUtil.getUUIDString(LoadingActivity.this);
        long apiName = IDCard.getApiName();
        String content = licenseManager.getContext(uuid, LicenseManager.DURATION_365DAYS, apiName);

        String errorStr = licenseManager.getLastError();
        Log.w("ceshi", "getContent++++errorStr===" + errorStr);

        licenseManager.takeLicenseFromNetwork(uuid, KeyUtil.API_KEY, KeyUtil.API_SECRET, apiName,
                LicenseManager.DURATION_30DAYS, "IDCard", "1", false, new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        authState(true);
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        Log.e(TAG, "failed: " + i + new String(bytes));
                        authState(false);
                    }
                });


    }

    private void authState(boolean isSuccess) {
        if (isSuccess) {
            barLinear.setVisibility(View.GONE);
            WarrantyBar.setVisibility(View.GONE);
            againWarrantyBtn.setVisibility(View.GONE);
            startActivity(new Intent(this, IDCardActionActivity.class).putExtra("key", KeyUtil.API_KEY).putExtra("secret", KeyUtil.API_SECRET));
            finish();
        } else {
            barLinear.setVisibility(View.VISIBLE);
            WarrantyBar.setVisibility(View.GONE);
            againWarrantyBtn.setVisibility(View.VISIBLE);
            WarrantyText.setText("联网授权失败！请检查网络或找服务商");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.load_rootRel) {
            ConUtil.isGoneKeyBoard(this);
        } else if (id == R.id.loading_layout_againWarrantyBtn) {
            network();
        }
    }
}