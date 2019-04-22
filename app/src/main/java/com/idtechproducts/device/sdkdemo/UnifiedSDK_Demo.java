package com.idtechproducts.device.sdkdemo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class UnifiedSDK_Demo extends ActionBarActivity {

    private SdkDemoFragment mainView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        if (savedInstanceState == null) {
            mainView = new SdkDemoFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainView).commit();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
    }

    // Inflate the menu items to the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch_reader_type:
                mainView.openReaderSelectDialog();
                break;
            case R.id.action_enable_log:
                boolean toEnable = !item.isChecked();
                item.setChecked(toEnable);
                mainView.enableLogFeature(toEnable);
                break;
            case R.id.action_delete_log:
                mainView.deleteLogs();
                break;
            case R.id.action_firmware_update_init:
                mainView.openReaderSelectDialogForFwUpdate();
                break;
            case R.id.action_firmware_update:
                mainView.continueFirmwareUpdate();
                break;
            case R.id.action_exit_app:
                mainView.releaseSDK();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
