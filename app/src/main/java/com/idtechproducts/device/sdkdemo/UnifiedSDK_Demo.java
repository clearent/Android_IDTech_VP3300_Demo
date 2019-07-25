package com.idtechproducts.device.sdkdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.clearent.idtech.android.HasManualTokenizingSupport;
import com.clearent.idtech.android.domain.CardProcessingResponse;
import com.clearent.idtech.android.family.DeviceFactory;
import com.clearent.idtech.android.PublicOnReceiverListener;
import com.clearent.idtech.android.family.device.VP3300;
import com.clearent.idtech.android.token.domain.TransactionToken;
import com.clearent.idtech.android.token.manual.ManualCardTokenizer;
import com.clearent.idtech.android.token.manual.ManualCardTokenizerImpl;
import com.clearent.sample.CreditCard;
import com.clearent.sample.PostTransactionRequest;
import com.clearent.sample.ReceiptDetail;
import com.clearent.sample.ReceiptRequest;
import com.clearent.sample.SaleTransaction;
import com.clearent.sample.SampleReceipt;
import com.clearent.sample.SampleReceiptImpl;
import com.clearent.sample.SampleTransaction;
import com.clearent.sample.SampleTransactionImpl;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ErrorCodeInfo;
import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ReaderInfo.DEVICE_TYPE;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateToolMsg;
import com.idtechproducts.device.bluetooth.BluetoothLEController;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    @SuppressLint("ValidFragment")
    public class SdkDemoFragment extends Fragment implements PublicOnReceiverListener, HasManualTokenizingSupport, FirmwareUpdateToolMsg {
        private final long BLE_ScanTimeout = 10000; //in milliseconds
        private final long BLE_Max_Scan_Retries = 3; //in milliseconds

        private boolean isBluetoothScanning = false;

        private VP3300 device;
        private FirmwareUpdateTool fwTool;
        private ManualCardTokenizer manualCardTokenizer;
        private static final int REQUEST_ENABLE_BT = 1;
        private long totalEMVTime;
        private boolean calcLRC = true;

        private BluetoothAdapter mBtAdapter = null;

        private TextView status;
        private TextView infoText;
        private EditText textAmount;
        private CheckBox checkboxAutoConfigure;
        private EditText textCard;
        private EditText textExpirationDate;
        private EditText textCsc;

        private View rootView;
        private LayoutInflater layoutInflater;
        private ViewGroup viewGroup;
        private AlertDialog transactionAlertDialog;

        private String lastTransactionToken;
        private String info = "";
        private String detail = "";
        private Handler handler = new Handler();

        private StructConfigParameters config = null;
        private EditText edtSelection;

        private boolean isFwInitDone = false;

        private Button swipeButton;
        private Button manualButton;
        private Button commandBtn;
        private final int emvTimeout = 30;

        private int bleRetryCount = 0;

        private boolean isReady = false;
        private BluetoothDevice currentBluetoothDevice = null;

        private String currentBluetoothUUID = null;
        private boolean startTransaction = false;

        private int numTimesAttemptedToConfigureReader = 0;
        private int maxTimesToConfigureReader = 5;

        private ProgressDialog waitForReaderIsReadyProgressDialog;

        private DemoApplicationContext demoApplicationContext;

        //To take control of configuration (default is to auto configure the reader
        //1 - set disableAutoConfiguration to false in ApplicationContext
        //2 - set applyClearentConfiguration to true
        private boolean applyClearentConfiguration = false;

        private boolean btleDeviceRegistered = false;
        private String btleDeviceAddress = null;

        private byte[] tag8A = new byte[]{0x30, 0x30};

        private String[] commands = {
                "Get Firmware Version",            // 0
                "Get Serial Number",        // 1
                "Send Command",                //2
                "Get Global Configuration",                // 3
                "Burst Mode On",        // 4
                "Burst Mode Off",        // 5
                "Auto Poll On",        // 6
                "Auto Poll Off",        // 7
                "Get Transaction Result",                // 8
                "ICC - Power On",            // 9
                "ICC - Power Off",            // 10
                "ICC - Get Reader Status",    // 11
                "ICC - Passthrough Mode On", //12
                "ICC - Passthrough Mode Off", //13
                "ICC - Exchange APDU Plaintext",    // 14
                "EMV - Start Transaction",    // 15
                "EMV - AID",    // 16
                "EMV - CAPK", //17
                "EMV - CRL", //18
                "EMV - Terminal" //19
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            layoutInflater = inflater;
            viewGroup = container;
            rootView = inflater.inflate(R.layout.fragment_main, container, false);

            status = (TextView) rootView.findViewById(R.id.status_text);
            status.setText("Disconnected");

            infoText = (TextView) rootView.findViewById(R.id.text_area_top);
            infoText.setVerticalScrollBarEnabled(true);

            swipeButton = (Button) rootView.findViewById(R.id.btn_swipeCard);
            swipeButton.setOnClickListener(new SwipeButtonListener());

            manualButton = (Button) rootView.findViewById(R.id.btn_manualCard);
            manualButton.setOnClickListener(new ManualButtonListener());
            manualButton.setEnabled(true);

            swipeButton.setEnabled(true);
            commandBtn = (Button) rootView.findViewById(R.id.btn_command);
            commandBtn.setOnClickListener(new CommandButtonListener());
            commandBtn.setEnabled(false);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            if (device != null) {
                device.unregisterListen();
            }

            initializeReader();
            openReaderSelectDialog();

            super.onActivityCreated(savedInstanceState);
        }


        @Override
        public void onDestroy() {
            if (device != null) {
                device.unregisterListen();
            }

            super.onDestroy();
        }

        @Override
        public void isReady() {
            applyClearentConfiguration = false;
//            getActivity().runOnUiThread(new Runnable() {
//                public void run() {
//                    swipeButton.setEnabled(true);
//                    commandBtn.setEnabled(true);
//                }
//            });
            info += "\nClearent Framework communicated back (isReady) at Time " + new Date().toString() + "\n";
            info += "\nCard reader is ready for use.\n";
            handler.post(doUpdateStatus);

            if (waitForReaderIsReadyProgressDialog != null && waitForReaderIsReadyProgressDialog.isShowing()) {
                waitForReaderIsReadyProgressDialog.dismiss();
                handler.post(doEnableButtons);
                handler.post(doTransactionAlert);
                handler.post(doStartTransaction);
            }

            isReady = true;
        }

        @Override
        public void successfulTransactionToken(final TransactionToken transactionToken) {
            lastTransactionToken = transactionToken.getTransactionToken();
//            getActivity().runOnUiThread(new Runnable() {
//                public void run() {
//                    info += "Please remove card\n";
//                    transactionAlertDialog.setMessage("Please remove card");
//                    info += "Card is now represented by a transaction token: " + transactionToken.getTransactionToken() + "\n";
//                    handler.post(doUpdateStatus);
//                    manualButton.setEnabled(true);
//                    swipeButton.setEnabled(true);
//                    commandBtn.setEnabled(true);
//                }
//            });
            handler.post(doSuccessUpdates);
            runSampleTransaction(transactionToken);
        }

        private Runnable doSuccessUpdates = new Runnable() {
            public void run() {
                info += "Please remove card\n";
                transactionAlertDialog.setMessage("Please remove card");
                info += "Card is now represented by a transaction token: " + lastTransactionToken + "\n";
                handler.post(doUpdateStatus);
                manualButton.setEnabled(true);
                swipeButton.setEnabled(true);
                commandBtn.setEnabled(true);
            }
        };


        @Override
        public void handleCardProcessingResponse(CardProcessingResponse cardProcessingResponse) {
            switch (cardProcessingResponse) {
                case TERMINATE:
                case USE_MAGSTRIPE:
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            swipeButton.setEnabled(true);
                            commandBtn.setEnabled(true);
                            if (transactionAlertDialog != null && transactionAlertDialog.isShowing()) {
                                transactionAlertDialog.dismiss();
                            }
                        }
                    });
                    break;
                case USE_CHIP_READER:
                    info += "Card has chip. Try insert.\n";
                    handler.post(doUpdateStatus);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            swipeButton.setEnabled(true);
                            commandBtn.setEnabled(true);
                            if (transactionAlertDialog != null && transactionAlertDialog.isShowing()) {
                                transactionAlertDialog.dismiss();
                            }
                        }
                    });
                    break;
                case REMOVE_CARD_AND_TRY_SWIPE:
                    info += "Remove card\n";
                    handler.post(doUpdateStatus);
                    break;
                case NONTECHNICAL_FALLBACK_SWIPE_CARD:
                    info += "Please swipe card\n";
                    handler.post(doUpdateStatus);
                    break;
                default:
                    info += "Card processing error: " + cardProcessingResponse.getDisplayMessage() + "\n";
                    handler.post(doUpdateStatus);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            swipeButton.setEnabled(true);
                            commandBtn.setEnabled(true);
                            if (transactionAlertDialog != null && transactionAlertDialog.isShowing()) {
                                transactionAlertDialog.dismiss();
                            }
                        }
                    });
            }
        }

        @Override
        public void handleManualEntryError(String message) {
            info += "\nFailed to get a transaction token from a manually entered card. Error - " + message;
            handler.post(doUpdateStatus);
            manualButton.setEnabled(true);
            swipeButton.setEnabled(true);
            commandBtn.setEnabled(true);
        }

        @Override
        public void handleConfigurationErrors(String message) {
            if (device.device_getDeviceType() == DEVICE_TYPE.DEVICE_VP3300_BT && applyClearentConfiguration && numTimesAttemptedToConfigureReader < maxTimesToConfigureReader) {
                info += "\nThe reader failed to configure. Error - " + message + ". Attempt " + numTimesAttemptedToConfigureReader + "\n";
                handler.post(doUpdateStatus);
                numTimesAttemptedToConfigureReader++;
                device.applyClearentConfiguration();
            } else {
                info += "\nThe reader failed to configure. Error - " + message;
                handler.post(doUpdateStatus);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        swipeButton.setEnabled(true);
                        commandBtn.setEnabled(true);
                    }
                });
            }
            //  releaseSDK();
        }

        private void runSampleTransaction(TransactionToken transactionToken) {
            SampleTransaction sampleTransaction = new SampleTransactionImpl();
            PostTransactionRequest postTransactionRequest = new PostTransactionRequest();
            postTransactionRequest.setTransactionToken(transactionToken);
            //sandbox
            postTransactionRequest.setApiKey(getTestApiKey());
            postTransactionRequest.setBaseUrl(getPaymentsBaseUrl());
            SaleTransaction saleTransaction;
            if (textAmount == null || textAmount.getText().toString() == null || textAmount.getText().toString().length() == 0) {
                saleTransaction = new SaleTransaction("1.00");
            } else {
                saleTransaction = new SaleTransaction(textAmount.getText().toString());
            }
            saleTransaction.setSoftwareTypeVersion("v1.0");
            saleTransaction.setSoftwareType("android-idtech-vp3300-demo-1.0");
            postTransactionRequest.setSaleTransaction(saleTransaction);
            sampleTransaction.doSale(postTransactionRequest, this);
        }

        public void initializeReader() {

            if (device != null) {
                releaseSDK();
            }
            isReady = false;
            applyClearentConfiguration = false;
            btleDeviceRegistered = false;
            isBluetoothScanning = false;
            manualCardTokenizer = new ManualCardTokenizerImpl(this);

            //Gather the context needed to get a device object representing the card reader.
            demoApplicationContext = new DemoApplicationContext(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ, this, getActivity(), getPaymentsBaseUrl(), getPaymentsPublicKey(), null);
            //demoApplicationContext = new DemoApplicationContext(DEVICE_TYPE.DEVICE_VP3300_USB, this, getActivity(), getPaymentsBaseUrl(), getPaymentsPublicKey(), null);
            device = DeviceFactory.getVP3300(demoApplicationContext);

            //Enable verbose logging only when instructed to by support.
            device.log_setVerboseLoggingEnable(true);
            device.log_setSaveLogEnable(true);

            //reset the shared preference so we can test applying the configuration again.
            //device.setReaderConfiguredSharedPreference(false);

            fwTool = new FirmwareUpdateTool(this, getActivity());

            Toast.makeText(getActivity(), "get started", Toast.LENGTH_LONG).show();
            displaySdkInfo();

            device.addRemoteLogRequest("Android_IDTech_VP3300_Demo", "Initialized the VP3300");
        }

        private EditText edtBTLE_Name;
        private Dialog dlgBTLE_Name;

        void openReaderSelectDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            device.setAutoConfiguration(false);
            View checkBoxView = View.inflate(getActivity(), R.layout.auto_configure_checkbox, null);
            CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkboxAutoConfigure);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    device.setAutoConfiguration(isChecked ? true : false);
                }
            });
            CheckBox clearReaderCacheCheckBox = (CheckBox) checkBoxView.findViewById(R.id.clearReaderCache);
            clearReaderCacheCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    device.setReaderConfiguredSharedPreference(isChecked ? false : true);
                }
            });
            builder.setTitle("Select a device:");
            builder.setView(checkBoxView);
            builder.setCancelable(false);
            builder.setItems(R.array.reader_type, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {

                        case 0:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_AJ)) {
                                Toast.makeText(getActivity(), "VP3300 Audio Jack (Audio Jack) is selected", Toast.LENGTH_SHORT).show();
                                applyClearentConfiguration = true;
                                btleDeviceRegistered = false;
                                isBluetoothScanning = false;
                            } else {
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 1:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_AJ_USB))
                                Toast.makeText(getActivity(), "VP3300 Audio Jack (USB) is selected", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT)) {
                                Toast.makeText(getActivity(), "VP3300 Bluetooth (Bluetooth) is selected", Toast.LENGTH_SHORT).show();
                                dlgBTLE_Name = new Dialog(getActivity());
                                dlgBTLE_Name.setTitle("Enter Name or Address");
                                dlgBTLE_Name.setCancelable(false);
                                dlgBTLE_Name.setContentView(R.layout.btle_device_name_dialog);
                                Button btnBTLE_Ok = (Button) dlgBTLE_Name.findViewById(R.id.btnSetBTLE_Name_Ok);
                                edtBTLE_Name = (EditText) dlgBTLE_Name.findViewById(R.id.edtBTLE_Name);
                                String bleId = "14958";
                                try {
                                    InputStream inputStream = getActivity().openFileInput("bleId.txt");

                                    if (inputStream != null) {
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        String receiveString = "";
                                        StringBuilder stringBuilder = new StringBuilder();

                                        while ((receiveString = bufferedReader.readLine()) != null) {
                                            stringBuilder.append(receiveString);
                                        }

                                        inputStream.close();
                                        bleId = stringBuilder.toString();
                                    }
                                } catch (FileNotFoundException e) {

                                } catch (IOException e) {

                                }
                                edtBTLE_Name.setText(bleId);

                                btnBTLE_Ok.setOnClickListener(setBTLE_NameOnClick);
                                dlgBTLE_Name.show();
                                btleDeviceRegistered = false;
                                isBluetoothScanning = false;
                            } else
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT_USB))
                                Toast.makeText(getActivity(), "VP3300 Bluetooth (USB) is selected", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_USB)) {
                                applyClearentConfiguration = false;
                                btleDeviceRegistered = false;
                                isBluetoothScanning = false;
                                Toast.makeText(getActivity(), "VP3300 USB is selected", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }

                    if (device.device_getDeviceType() == DEVICE_TYPE.DEVICE_VP3300_USB) {
                        handler.post(doEnableButtons);
                        handler.post(doRegisterListen);
                    } else if (device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_BT) {
                        handler.post(doRegisterListen);
                        device.device_configurePeripheralAndConnect();
                    }
                }
            });

            builder.create().show();
        }

        private View.OnClickListener setBTLE_NameOnClick = new View.OnClickListener() {
            public void onClick(View v) {
                dlgBTLE_Name.dismiss();
                Common.setBLEDeviceName(edtBTLE_Name.getText().toString());

                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput("bleId.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(edtBTLE_Name.getText().toString());
                    outputStreamWriter.close();
                } catch (IOException e) {

                }

                device.setIDT_Device(fwTool);

                if (!isBleSupported(getActivity())) {
                    Toast.makeText(getActivity(), "Bluetooth LE is not supported\r\n", Toast.LENGTH_LONG).show();
                    return;
                }

                final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBtAdapter = bluetoothManager.getAdapter();

                if (mBtAdapter == null) {
                    Toast.makeText(getActivity(), "Bluetooth LE is not available\r\n", Toast.LENGTH_LONG).show();
                    return;
                }

                btleDeviceRegistered = false;
                isBluetoothScanning = false;

                if (!mBtAdapter.isEnabled()) {
                    Log.i("CLEARENT", "Adapter");
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    scanforDevice(true, BLE_ScanTimeout);
                }
            }
        };

        boolean isBleSupported(Context context) {
            return BluetoothAdapter.getDefaultAdapter() != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (device.device_getDeviceType() == DEVICE_TYPE.DEVICE_VP3300_BT) {
                if (requestCode == REQUEST_ENABLE_BT) {
                    if (resultCode == Activity.RESULT_OK) {
                        Toast.makeText(getActivity(), "Bluetooth has turned on, now searching for device", Toast.LENGTH_SHORT).show();
                        scanforDevice(true, BLE_ScanTimeout);
                    } else {
                        Toast.makeText(getActivity(), "Problem in Bluetooth Turning ON", Toast.LENGTH_SHORT).show();
                        handler.post(doEnableButtons);
                    }
                }
            }
        }

        private void scanforDevice(final boolean enable, final long timeout) {
            handler.post(doConnectReaderProgressBar);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentBluetoothDevice != null) {
                handler.post(doRegisterListen);
            } else {
                btleDeviceRegistered = false;
                bleRetryCount = 0;
                scanLeDevice(true, timeout);
            }
        }

        Handler scanProgressBarHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                waitForReaderIsReadyProgressDialog.incrementProgressBy(1);
            }
        };

        private void scanLeDevice(final boolean enable, final long timeout) {
            if (enable) {
                handler.postDelayed(new Runnable() {
                    public void run() {
                        info += "\nStopping Scan Time " + new Date().toString() + "\n";
                        mBtAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                        isBluetoothScanning = false;
                        if (!device.device_isConnected()) {
                            info += "\nTimed out trying to find bluetooth device";
                            handler.post(doUpdateStatus);
                            btleDeviceRegistered = false;
                            bleRetryCount++;
                            if (bleRetryCount <= BLE_Max_Scan_Retries) {
                                if (waitForReaderIsReadyProgressDialog.isShowing()) {
                                    waitForReaderIsReadyProgressDialog.setMessage("Still looking... " + bleRetryCount);
                                }
                                info += "\nTrying again ";
                                handler.post(doUpdateStatus);
                                scanLeDevice(true, timeout);
                            } else {
                                info += "\nFailed to connect to bluetooth device.";
                                info += "\nPossible issues:";
                                info += "\nMore than one IdTech bluetooth device in range. When prompted for search criteria, enter the last 5 digits of device serial number (bottom of reader, ex = 737T003758).";
                                info += "\nSecond led of reader is amber color. Battery is low. Charge the reader. ";
                                info += "\nWifi signal interference. If your tablet connects to wifi with 2.4ghz it could interfere with the bluetooth device (which is 2.4ghz). If possible connect your tablet/phone to wifi over 5ghz.";
                                info += "\nBluetooth signal interference. Sometimes you can have too many bluetooth devices in range. When this happens, try moving your reader close to the tablet or phone.";
                                handler.post(doUpdateStatus);
                                if (waitForReaderIsReadyProgressDialog.isShowing()) {
                                    waitForReaderIsReadyProgressDialog.dismiss();
                                }
                            }
                        }
                    }
                }, timeout);

                List<ScanFilter> scanFilters = createScanFilter();
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setReportDelay(0)
                        .build();
                mBtAdapter.getBluetoothLeScanner().startScan(scanFilters, settings, scanCallback);
            } else {
                mBtAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            }
        }

        private List<ScanFilter> createScanFilter() {
            List<ScanFilter> scanFilters = new ArrayList<>();
            ScanFilter scanFilter = null;
            String last5 = Common.getBLEDeviceName();
            if (btleDeviceAddress != null) {
                scanFilter = new ScanFilter.Builder().setDeviceAddress(btleDeviceAddress).build();
            } else if (last5 != null && !"".equals(last5)) {
                scanFilter = new ScanFilter.Builder().setDeviceName("IDTECH-VP3300-" + last5).build();
            } else {
                scanFilter = new ScanFilter.Builder().build();
            }
            scanFilters.add(scanFilter);
            return scanFilters;
        }

        private ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                String last5 = Common.getBLEDeviceName();
                String searchString = last5 != null && !"".equals(last5) ? "IDTECH-VP3300-" + last5 : "IDTECH";
                if (result == null || result.getDevice() == null || result.getDevice().getName() == null) {
                    Log.i("SCAN", "Skipping weirdness ");
                } else if (!btleDeviceRegistered && result.getDevice().getName().contains(searchString)) {
                    info += "\nDevice found during scan at Time " + new Date().toString() + ". Pass device to idtech framework to register a listener.\n";
                    Log.i("SCAN", "Scan success " + result.getDevice().getName());
                    currentBluetoothDevice = result.getDevice();
                    BluetoothLEController.setBluetoothDevice(result.getDevice());
                    btleDeviceAddress = result.getDevice().getAddress();

                    String storedDeviceSerialNumberOfConfiguredReader = device.getStoredDeviceSerialNumberOfConfiguredReader();
                    if (storedDeviceSerialNumberOfConfiguredReader.contains(last5)) {
                        Log.i("WATCH", "The device we found during scanning has been configured from this device");
                    }
                    handler.post(doRegisterListen);
                    btleDeviceRegistered = true;
                } else {
                    Log.i("SCAN", "Skip " + result.getDevice().getName());
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                System.out.println("BLE// onBatchScanResults");
                for (ScanResult sr : results) {
                    Log.i("ScanResult - Results", sr.toString());
                    info += "\nScanResult - Results" + sr.toString() + "\n";
                    handler.post(doUpdateStatus);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                System.out.println("BLE// onScanFailed");
                Log.e("Scan Failed", "Error Code: " + errorCode);
                info += "\nScan Failed. Error Code: " + errorCode + "\n";
                handler.post(doUpdateStatus);
            }
        };

        private String[] fw_commands = null;

        void openReaderSelectDialogForFwUpdate() {
            if (device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_AJ_USB && device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_USB &&
                    device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_BT_USB && device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_COM) {
                Toast.makeText(getActivity(), "Only support VP3300 through USB and RS232", Toast.LENGTH_LONG).show();
                return;
            }

            if (isFwInitDone) {
                Toast.makeText(getActivity(), "FW update initialization was done, please update FW now", Toast.LENGTH_LONG).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select a device:");
            builder.setCancelable(true);

            builder.setItems(R.array.fw_reader_type, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    String fileNames[] = {"vp3300_aj_firmware_064.txt", "vp3300_bt_firmware_090.txt", "IDT_Firmware_VP3300_USB.txt", "IDT_Firmware_VP3300_COM.txt"};

                    // to refer to the application path
                    File fileDir = getActivity().getFilesDir();
                    String fileName = fileDir.getParent() + java.io.File.separator + fileDir.getName();
                    // String path = Environment.getExternalStorageDirectory().toString();
                    String fileNameWithPath = fileName + java.io.File.separator + fileNames[which];
                    info += "fileNameWithPath: " + fileNameWithPath + "";
                    fw_commands = getStringArrayFromFirmwareTXTFile(fileNameWithPath);

                    if (fw_commands == null || fw_commands.length == 0) {
                        Toast.makeText(getActivity(), "Please check if \"" + fileNames[which] + "\" is located in the root directory.", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        int i;
                        for (i = 0; i < fw_commands.length; i++) {
                            if (fw_commands[i] != null)
                                break;
                        }
                        if (i == fw_commands.length) {
                            Toast.makeText(getActivity(), "Please check if \"" + fileNames[which] + "\" contains correct data.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    int ret = device.device_updateFirmware(fw_commands);
                    if (ret == ErrorCode.SUCCESS) {
                        info = "Initialize firmware update...";
                        swipeButton.setEnabled(false);
                        commandBtn.setEnabled(false);
                        Toast.makeText(getActivity(), "FW update started initialization.", Toast.LENGTH_SHORT).show();
                    } else {
                        info += "updatefirmware failed: " + device.device_getResponseCodeString(ret) + "";
                        Toast.makeText(getActivity(), "FW update initialization cannot be started.", Toast.LENGTH_LONG).show();
                    }
                    handler.post(doUpdateStatus);
                }
            });
            builder.create().show();
        }

        private String[] getStringArrayFromFirmwareTXTFile(String strFilePathName) {

            File file = new File(strFilePathName);
            if (file.exists() == false)
                return null;

            String[] cmds;
            int count = 0;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String line = "";

                while ((line = br.readLine()) != null && !line.equalsIgnoreCase("END>")) {
                    count++;
                }
                count++;
                br.close();
                br = null;

                br = new BufferedReader(new FileReader(file));
                line = "";

                cmds = new String[count];

                for (int i = 0; i < count; i++) {
                    line = br.readLine();
                    if (line != null)
                        cmds[i] = new String(line);
                }
            } catch (IOException e) {
                return null;
            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (Exception ex) {
                    return null;
                }
            }
            return cmds;
        }

        void continueFirmwareUpdate() {
            if (!Common.getBootLoaderMode()) {
                Toast.makeText(getActivity(), "Please initialize firmware update first.", Toast.LENGTH_LONG).show();
                return;
            }

            int ret = device.device_updateFirmware(fw_commands);
            if (ret == ErrorCode.SUCCESS) {
                swipeButton.setEnabled(false);
                commandBtn.setEnabled(false);
                Toast.makeText(getActivity(), "FW update started.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "FW update cannot be started.", Toast.LENGTH_LONG).show();
                detail = "";
                handler.post(doUpdateStatus);
            }
        }

        Dialog dlgMenu;
        Dialog dlgLanguageMenu;
        byte type;
        String[] theLines;
        byte[] Language;
        byte MessageId;
        int finalTimout;

        public void lcdDisplay(int mode, final String[] lines, int timeout) {
            if (lines != null && lines.length > 0) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (lines[0].contains("TIME OUT")) {
                            handler.post(doEnableButtons);
                        }
                        info += "\n";
                        Log.i("WATCH1", lines[0]);
                        info += lines[0] + "\n";
                        handler.post(doUpdateStatus);
                        if (waitForReaderIsReadyProgressDialog != null && waitForReaderIsReadyProgressDialog.isShowing()) {
                            waitForReaderIsReadyProgressDialog.setMessage(lines[0]);
                        } else if (transactionAlertDialog != null && transactionAlertDialog.isShowing()) {
                            transactionAlertDialog.setMessage(lines[0]);
                            String checkTransactionMessage = "Sample Transaction successful. Transaction Id:";
                            String checkReceiptMessage = "Sample receipt sent successfully";
                            String checkFailedTransactionFailed = "Sample transaction failed";
                            if (lines[0].contains(checkFailedTransactionFailed)) {
                                if (transactionAlertDialog != null && transactionAlertDialog.isShowing()) {
                                    transactionAlertDialog.dismiss();
                                }
                            } else if (lines[0].contains(checkTransactionMessage)) {
                                runSampleReceipt(lines[0]);
                            } else if (lines[0].contains(checkReceiptMessage)) {
                                if (transactionAlertDialog != null && transactionAlertDialog.isShowing()) {
                                    transactionAlertDialog.dismiss();
                                }
                            }
                        }
                    }
                });
            }
        }

        private void runSampleReceipt(String line) {
            String[] parts = line.split(":");
            ReceiptRequest receiptRequest = new ReceiptRequest();
            receiptRequest.setApiKey(getTestApiKey());
            receiptRequest.setBaseUrl(getPaymentsBaseUrl());
            ReceiptDetail receiptDetail = new ReceiptDetail();
            receiptDetail.setEmailAddress("dhigginbotham@clearent.com,lwalden@clearent.com");
            receiptDetail.setTransactionId(parts[1]);
            receiptRequest.setReceiptDetail(receiptDetail);

            SampleReceipt sampleReceipt = new SampleReceiptImpl();
            sampleReceipt.doReceipt(receiptRequest, this);
        }

        public void lcdDisplay(int mode, String[] lines, int timeout, byte[] languageCode, byte messageId) {
            //Clearent runs with a terminal major configuration of 5C, so no prompts. We should be able to
            //monitor all messaging using the other lcdDisplay method as well as the response and error handlers.
        }

        private View.OnClickListener menuDisplayOKOnClick = new View.OnClickListener() {
            public void onClick(View v) {
                byte bSelection = Byte.parseByte(edtSelection.getText().toString());
                if (bSelection == 0x00) {
                    Toast.makeText(getActivity(), "Selection Error: Cannot be 0", Toast.LENGTH_LONG).show();
                    return;
                }
                device.emv_lcdControlResponse((byte) 0x01, (byte) bSelection);
                dlgMenu.dismiss();
            }
        };

        private int dialogId = 0;  //authenticate_dialog: 0 complete_emv_dialog: 1 language selection: 2 menu_display: 3

        public void timerDelayRemoveDialog(long time, final Dialog d) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (d.isShowing()) {
                        d.dismiss();
                        switch (dialogId) {
                            case 0:
                                info = "EMV Transaction Declined.  Authentication Time Out.\n";
                                break;
                            case 1:
                                info = "EMV Transaction Declined.  Complete EMV Time Out.\n";
                                break;
                            case 2:
                                info = "EMV Transaction Language Selection Time Out.\n";
                                break;
                            case 3:
                                info = "EMV Transaction Menu Selection Time Out.\n";
                                break;
                        }
                        handler.post(doUpdateStatus);
                        ResDataStruct resData = new ResDataStruct();
                        device.emv_cancelTransaction(resData);
                        swipeButton.setEnabled(true);
                        commandBtn.setEnabled(true);
                    }
                }
            }, time);
        }

        public void releaseSDK() {
            if (device != null) {
                device.unregisterListen();
                device.release();
            }
        }

        public void enableLogFeature(boolean enable) {
            if (device != null) {
                device.log_setSaveLogEnable(enable);
            }

            if (enable)
                Toast.makeText(getActivity(), "Log feature enabled", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "Log feature disabled", Toast.LENGTH_SHORT).show();
        }

        public void deleteLogs() {
            if (device != null) {
                int fileDeleted = device.log_deleteLogs();
                Toast.makeText(getActivity(), "Total of " + fileDeleted + " files are deleted.", Toast.LENGTH_SHORT).show();
            }
        }

        public void displaySdkInfo() {
            info += "Manufacturer: " + android.os.Build.MANUFACTURER + "\n" +
                    "Model: " + android.os.Build.MODEL + "\n" +
                    "OS Version: " + android.os.Build.VERSION.RELEASE + " \n" +
                    "SDK Version: \n" + device.config_getSDKVersion() + "\n";

            detail = "";

            handler.post(doUpdateStatus);
        }

        private Runnable doUpdateStatus = new Runnable() {
            public void run() {
                infoText.setText(info);
            }
        };

        private Runnable doEnableButtons = new Runnable() {
            public void run() {
                swipeButton.setEnabled(true);
                commandBtn.setEnabled(true);
            }
        };

        private Runnable doSwipeProgressBar = new Runnable() {
            public void run() {
                handler.post(doEnableButtons);
                if (device.device_isConnected()) {
                    handler.post(doTransactionAlert);
                    handler.post(doStartTransaction);
                } else {
                    scanforDevice(true, BLE_ScanTimeout);
                }
            }
        };

        private Runnable doRegisterListen = new Runnable() {
            public void run() {
                device.registerListen();
            }
        };

        private Runnable doStartTransaction = new Runnable() {
            @Override
            public void run() {
                byte tags[] = {(byte) 0xDF, (byte) 0xEF, 0x1F, 0x02, 0x01, 0x00};
                int ret = device.device_startTransaction(1.00, 0.00, 0, 60, null);
                //int ret = device.emv_startTransaction(1.00, 0.00, 0, emvTimeout, tags, false);
                if (ret == ErrorCode.SUCCESS || ret == ErrorCode.RETURN_CODE_OK_NEXT_COMMAND) {
                    transactionAlertDialog.setMessage("Insert card or try swipe...");
                } else if (!device.device_isConnected() && device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT)) {
                    transactionAlertDialog.setMessage("Card reader is not connecting. Check for low battery amber light or press button to try again");
                    info = "Card reader is not connected. Press button,\n";
                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    handler.post(doEnableButtons);
                    handler.post(doUpdateStatus);
                    btleDeviceRegistered = false;
                    scanforDevice(true, BLE_ScanTimeout);
                } else {
                    transactionAlertDialog.setMessage("Card reader failed to process. Cancel and try again");
                    info = "cannot swipe/tap card\n";
                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    handler.post(doEnableButtons);
                    handler.post(doUpdateStatus);
                }
            }
        };

        private Runnable doTransactionAlert = new Runnable() {
            public void run() {
                infoText.setText(info);
                if (transactionAlertDialog != null) {
                    transactionAlertDialog.setTitle("Processing payment");
                    transactionAlertDialog.setMessage("Wait for instructions...");
                    transactionAlertDialog.show();
                } else {
                    AlertDialog.Builder transactionViewBuilder = new AlertDialog.Builder(getActivity());

                    transactionViewBuilder.setTitle("Processing payment");
                    transactionViewBuilder.setMessage("Wait for instructions...");
                    transactionViewBuilder.setView(layoutInflater.inflate(R.layout.frame_swipe, viewGroup, false));
                    transactionViewBuilder.setCancelable(false);
                    transactionViewBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            int ret = device.device_cancelTransaction();
                            if (ret == ErrorCode.SUCCESS) {
                                infoText.setText("Transaction cancelled");
                            } else {
                                infoText.setText("Failed to cancel transaction");
                            }
                            handler.post(doEnableButtons);
                        }
                    });
                    transactionAlertDialog = transactionViewBuilder.create();
                    transactionAlertDialog.show();
                }
            }
        };

        private Runnable doConnectReaderProgressBar = new Runnable() {
            public void run() {
                if (waitForReaderIsReadyProgressDialog != null) {
                    waitForReaderIsReadyProgressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (waitForReaderIsReadyProgressDialog.getProgress() <= waitForReaderIsReadyProgressDialog
                                        .getMax()) {
                                    Thread.sleep(100);
                                    scanProgressBarHandle.sendMessage(scanProgressBarHandle.obtainMessage());
                                    if (waitForReaderIsReadyProgressDialog.getProgress() == waitForReaderIsReadyProgressDialog
                                            .getMax()) {
                                        waitForReaderIsReadyProgressDialog.dismiss();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    waitForReaderIsReadyProgressDialog = new ProgressDialog(getActivity());

                    waitForReaderIsReadyProgressDialog.setMax(120);
                    waitForReaderIsReadyProgressDialog.setTitle("Connecting reader");
                    waitForReaderIsReadyProgressDialog.setMessage("Scanning (press button)...");
                    waitForReaderIsReadyProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    waitForReaderIsReadyProgressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (waitForReaderIsReadyProgressDialog.getProgress() <= waitForReaderIsReadyProgressDialog
                                        .getMax()) {
                                    Thread.sleep(100);
                                    scanProgressBarHandle.sendMessage(scanProgressBarHandle.obtainMessage());
                                    if (waitForReaderIsReadyProgressDialog.getProgress() == waitForReaderIsReadyProgressDialog
                                            .getMax()) {
                                        waitForReaderIsReadyProgressDialog.dismiss();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        };

        //Do NOT store this in your app (secret key)
        public String getTestApiKey() {
            return "24425c33043244778a188bd19846e860";
        }

        @Override
        public String getPaymentsBaseUrl() {
            return "https://gateway-qa.clearent.net";
        }

        @Override
        public String getPaymentsPublicKey() {
            return "307a301406072a8648ce3d020106092b240303020801010c036200042b0cfb3a1faaca8fb779081717a0bafb03e0cb061a1ef297f75dc5b951aaf163b0c2021e9bb73071bf89c711070e96ab1b63c674be13041d9eb68a456eb6ae63a97a9345c120cd8bff1d5998b2ebbafc198c5c5b26c687bfbeb68b312feb43bf";
        }

        public class SwipeButtonListener implements OnClickListener {
            public void onClick(View arg0) {
                int ret;
                startTransaction = true;
                totalEMVTime = System.currentTimeMillis();
                textAmount = (EditText) findViewById(R.id.textAmount);
                handler.post(doSwipeProgressBar);
            }
        }

        private class ManualButtonListener implements OnClickListener {
            public void onClick(View arg0) {
                textCard = (EditText) findViewById(R.id.textCard);
                textExpirationDate = (EditText) findViewById(R.id.textExpirationDate);
                textCsc = (EditText) findViewById(R.id.textCsc);

                CreditCard creditCard = new CreditCard();
                creditCard.setCard(textCard.getText().toString());
                creditCard.setExpirationDateMMYY(textExpirationDate.getText().toString());
                creditCard.setCsc(textCsc.getText().toString());

                manualCardTokenizer.createTransactionToken(creditCard);

                manualButton.setEnabled(false);
                commandBtn.setEnabled(false);
                swipeButton.setEnabled(false);

                info = "Manual tokenization requested\n";
                detail = "";
                handler.post(doUpdateStatus);
            }
        }

        private class CommandButtonListener implements OnClickListener {

            private Dialog dlgStartEMV;
            private EditText edtAmount;
            private EditText edt8A;
            private Dialog dlgAID;
            private Dialog dlgCAPK;
            private Dialog dlgCRL;
            private Dialog dlgTerminal;
            private Dialog dlgSendCAPDU;
            private Dialog dlgSendCmd;
            private EditText edtCAPDU;
            private EditText edtCmd;
            private EditText edtSubCmd;
            private EditText edtCmdData;
            private String strAPDU;
            private String _first_strAPDU = "00A404000E315041592E5359532E444446303100";

            public void onClick(View v) {
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        getActivity(), R.layout.command_dialog, commands);
                AlertDialog.Builder commandBuilder = new AlertDialog.Builder(getActivity());
                commandBuilder.setTitle("Select a Command");
                commandBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (device == null)
                            return;

                        StringBuilder sb = new StringBuilder();
                        int ret;
                        ResDataStruct resData = new ResDataStruct();

                        switch (which) {
                            case 0:
                                ret = device.device_getFirmwareVersion(sb);
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Firmware Version: " + sb.toString();
                                } else {
                                    info = "GetFirmwareVersion: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 1:
                                ret = device.config_getSerialNumber(sb);
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Serial Number: " + sb.toString();
                                } else {
                                    info = "GetSerialNumber: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 2:
                                dlgSendCmd = new Dialog(getActivity());
                                dlgSendCmd.setTitle("Please Enter Command");
                                dlgSendCmd.setCancelable(false);

                                Button btnSendCmd;
                                Button btnCancelCmd;
                                dlgSendCmd.setContentView(R.layout.idg_send_command_dialog);
                                btnSendCmd = (Button) dlgSendCmd.findViewById(R.id.btnIdgSendCmd);
                                btnCancelCmd = (Button) dlgSendCmd.findViewById(R.id.btnIdgCancelCmd);
                                edtCmd = (EditText) dlgSendCmd.findViewById(R.id.edtIdgCmd);
                                edtSubCmd = (EditText) dlgSendCmd.findViewById(R.id.edtIdgSubCmd);
                                edtCmdData = (EditText) dlgSendCmd.findViewById(R.id.edtIdgCmdData);
                                btnSendCmd.setOnClickListener(sendCmdOnClick);
                                btnCancelCmd.setOnClickListener(cancelCmdOnClick);
                                dlgSendCmd.show();
                                break;

                            case 4:
                                ret = device.device_setBurstMode((byte) 0x01);  //Burst Mode On
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Burst Mode On: Successful\n";
                                } else {
                                    info = "Burst Mode On: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 5:
                                ret = device.device_setBurstMode((byte) 0x00);  //Burst Mode Off
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Burst Mode Off: Successful\n";
                                } else {
                                    info = "Burst Mode Off: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 9:
                                ret = device.icc_powerOnICC(resData);
                                if (ret == ErrorCode.SUCCESS) {
                                    if (resData.resData != null)
                                        info = "Power On ICC: Successful <" + Common.base16Encode(resData.resData) + ">\n";
                                    else
                                        info = "Power On ICC: Successful\n";
                                } else {
                                    info = "Power On ICC: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                handler.post(doUpdateStatus);
                                break;
                            case 10:
                                ret = device.icc_powerOffICC(resData);
                                if (ret == ErrorCode.SUCCESS) {
                                    if (resData.resData != null)
                                        info = "Power Off ICC: Successful <" + Common.base16Encode(resData.resData) + ">";
                                    else
                                        info = "Power Off ICC: Successful";
                                } else {
                                    info = "Power Off ICC: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 11:
                                ICCReaderStatusStruct iccStatus = new ICCReaderStatusStruct();
                                ret = device.icc_getICCReaderStatus(iccStatus);
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "ICC Reader Status: \n";
                                    info += " - ICC Power On: " + iccStatus.iccPower + "\n";
                                    info += " - Card Seated: " + iccStatus.cardSeated + "\n";
                                } else {
                                    info = "Get ICC Reader Status: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 12:
                                ret = device.icc_passthroughOnICC();
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Passthrough On ICC: Successful";
                                } else {
                                    info = "Passthrough On ICC: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 13:
                                ret = device.icc_passthroughOffICC();
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Passthrough Off ICC: Successful";
                                } else {
                                    info = "Passthrough Off ICC: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 20:
                                ret = device.emv_getEMVKernelVersion(sb);
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Kernel Version: " + sb.toString();
                                } else {
                                    info = "Get Kernel Version: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            default:
                                info = "Feature not implemented yet";
                                detail = "";
                                handler.post(doUpdateStatus);
                        }
                    }

                });

                commandBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                commandBuilder.create().show();
            }

            private View.OnClickListener sendCmdOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    String strCmd = edtCmd.getText().toString();
                    String strSubCmd = "";
                    String strCmdData = "";
                    strSubCmd = edtSubCmd.getText().toString();
                    strCmdData = edtCmdData.getText().toString();
                    ResDataStruct resData = new ResDataStruct();

                    int ret;
                    if (strCmd.length() <= 0 || strCmd.length() % 2 == 1 || strSubCmd.length() <= 0 || strSubCmd.length() % 2 == 1) {
                        Toast.makeText(getActivity(), "Command could not be sent", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (strCmdData != null && strCmdData.length() % 2 == 1) {
                        Toast.makeText(getActivity(), "Command could not be sent", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dlgSendCmd.dismiss();
                    ret = device.device_sendDataCommand(strCmd + strSubCmd, calcLRC, strCmdData, resData);
                    if (ret == ErrorCode.SUCCESS) {
                        info = "Send Command " + strCmd + ((strSubCmd == "") ? "" : " ") + strSubCmd + " Succeeded\n";
                        detail = "Send Command " + strCmd + ((strSubCmd == "") ? "" : " ") + strSubCmd + "\nResult: <" + Common.base16Encode(resData.resData) + ">\n\n";

                        if (resData.resData != null && resData.resData.length > 0) {
                            detail += "Result data in Hex: <" + Common.base16Encode(resData.resData) + ">\n\n";
                            detail += "Result data in ASCII: <" + Common.getAsciiFromByte(resData.resData) + ">";
                        }
                    } else {
                        info = "Send Command " + strCmd + ((strSubCmd == "") ? "" : " ") + strSubCmd + " Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                        detail = "";
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener cancelCmdOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    dlgSendCmd.cancel();
                }

            };

        }

        ////////////// CALLBACKS /////////////
        public void deviceConnected() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    status.setText("Connected to Sandbox");
                }
            });
            info += "\nIdTech Framework communicated back (deviceConnected) at Time " + new Date().toString() + "\n";
            if (!Common.getBootLoaderMode()) {
                String device_name = device.device_getDeviceType().toString();
                info += device_name.replace("DEVICE_", "");
                if (!isReady) {
                    info += "Reader is paired and is being upgraded\r\n";
                    if (waitForReaderIsReadyProgressDialog != null && waitForReaderIsReadyProgressDialog.isShowing()) {
                        waitForReaderIsReadyProgressDialog.setMessage("Reader connected. Checking for updates...");
                    }
                }
                info += "Address: " + btleDeviceAddress;
                detail = "";
                handler.post(doUpdateStatus);

            }

            if (applyClearentConfiguration) {
                long waitBeforeAttemptingConfiguration = 100;
                if (isBluetoothScanning) {
                    waitBeforeAttemptingConfiguration = 8000;
                }
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (applyClearentConfiguration) {
                            numTimesAttemptedToConfigureReader++;
                            device.applyClearentConfiguration();
                        }
                    }
                }, waitBeforeAttemptingConfiguration);
            }
        }


        public void deviceDisconnected() {
            if (transactionAlertDialog != null)
                if (transactionAlertDialog.isShowing())
                    transactionAlertDialog.dismiss();
            if (!Common.getBootLoaderMode()) {
                info += "Reader is disconnected";
                detail = "";
                handler.post(doUpdateStatus);
            }
            isReady = false;
        }

        public void timeout(int errorCode) {
            info += ErrorCodeInfo.getErrorCodeDescription(errorCode);
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void ICCNotifyInfo(byte[] dataNotify, String strMessage) {
            if (strMessage != null && strMessage.length() > 0) {
                String strHexResp = Common.getHexStringFromBytes(dataNotify);
                Log.d("Demo Info >>>>>", "dataNotify=" + strHexResp);

                info += "ICC Notification Info: " + strMessage + "\n" + "Resp: " + strHexResp;
                detail = "";
                handler.post(doUpdateStatus);
            }
        }

        public void msgToConnectDevice() {
            info += "\nConnect device...\n";
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void msgAudioVolumeAdjustFailed() {
            info += "SDK could not adjust volume...";
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void onReceiveMsgChallengeResult(int returnCode, byte[] data) {
            // Not called for UniPay Firmware update
        }


        public void LoadXMLConfigFailureInfo(int index, String strMessage) {
            info += "XML loading error...";
            detail = "";
            handler.post(doUpdateStatus);
        }

        private String getIDTechAndroidDeviceConfigurationXmlFile() {
            //the target filename in the application path
            String fileName = "idt_unimagcfg_default.xml";

            try {
                InputStream in = getResources().openRawResource(R.raw.idt_unimagcfg_default);
                int length = in.available();
                byte[] buffer = new byte[length];
                in.read(buffer);
                in.close();
                getActivity().deleteFile(fileName);
                FileOutputStream fout = getActivity().openFileOutput(fileName, MODE_PRIVATE);
                fout.write(buffer);
                fout.close();

                // to refer to the application path
                File fileDir = getActivity().getFilesDir();
                fileName = fileDir.getParent() + java.io.File.separator + fileDir.getName();
                fileName = fileName + java.io.File.separator + "idt_unimagcfg_default.xml";

            } catch (Exception e) {
                e.printStackTrace();
                fileName = null;
            }
            return fileName;
        }

        public void dataInOutMonitor(byte[] data, boolean isIncoming) {
            //monitor for debugging and support purposes only.
        }

        @Override
        public void autoConfigProgress(int i) {
            Log.i("WATCH", "autoConfigProgress");
        }

        @Override
        public void autoConfigCompleted(StructConfigParameters structConfigParameters) {
            Log.i("WATCH", "autoConfigCompleted");
        }

        @Override
        public void deviceConfigured() {
            Log.i("WATCH", "device is configured");

        }


        public void onReceiveMsgUpdateFirmwareProgress(int nProgressValue) {
            if (Common.getBootLoaderMode())
                info = "Firmware update is in process... (" + nProgressValue + "%)";
            else
                info = "Firmware update initialization is in process...";
            handler.post(doUpdateStatus);
        }

        public void onReceiveMsgUpdateFirmwareResult(final int result) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    switch (result) {
                        case FirmwareUpdateToolMsg.cmdUpdateFirmware_Succeed:
                            info = "Firmware update is done successfully...";
                            detail = "";
                            isFwInitDone = false;
                            break;
                        case FirmwareUpdateToolMsg.cmdInitializeUpdateFirmware_Succeed:
                            if (device.device_getDeviceType() == DEVICE_TYPE.DEVICE_KIOSK_III)
                                info = "Firmware update initialization is done successfully, please wait for device reconnection and do firmware update.";
                            else
                                info = "Firmware update initialization is done successfully, please do firmware update now.";
                            isFwInitDone = true;
                            break;
                        case FirmwareUpdateToolMsg.cmdUpdateFirmware_Timeout:
                            if (Common.getBootLoaderMode()) {
                                info = "Firmware update timeout... Please try firmware update again...";
                                detail = "";
                            } else {
                                info = "Firmware update initialization timeout... Please try again...";
                            }
                            break;
                        case FirmwareUpdateToolMsg.cmdUpdateFirmware_DownloadBlockFailed:
                            info = "Firmware update failed... Please try again...";
                            detail = "";

                            break;
                    }
                    handler.post(doEnableButtons);
                    handler.post(doUpdateStatus);

                }

            });
        }

        private String emvErrorCodes(int val) {
            if (val == IDTEMVData.APPROVED_OFFLINE) return "APPROVED_OFFLINE";
            if (val == IDTEMVData.DECLINED_OFFLINE) return "DECLINED_OFFLINE";
            if (val == IDTEMVData.APPROVED) return "APPROVED";
            if (val == IDTEMVData.DECLINED) return "DECLINED";
            if (val == IDTEMVData.GO_ONLINE) return "GO_ONLINE";
            if (val == IDTEMVData.CALL_YOUR_BANK) return "CALL_YOUR_BANK";
            if (val == IDTEMVData.NOT_ACCEPTED) return "NOT_ACCEPTED";
            if (val == IDTEMVData.USE_MAGSTRIPE) return "USE_MAGSTRIPE";
            if (val == IDTEMVData.TIME_OUT) return "TIME_OUT";
            if (val == IDTEMVData.START_TRANS_SUCCESS) return "START_TRANS_SUCCESS";
            if (val == IDTEMVData.MSR_SUCCESS) return "MSR_SUCCESS";
            if (val == IDTEMVData.TRANSACTION_CANCELED) return "TRANSACTION_CANCELED";
            if (val == IDTEMVData.CTLS_TWO_CARDS) return "CTLS_TWO_CARDS";
            if (val == IDTEMVData.CTLS_TERMINATE) return "CTLS_TERMINATE";
            if (val == IDTEMVData.CTLS_TERMINATE_TRY_ANOTHER) return "CTLS_TERMINATE_TRY_ANOTHER";
            if (val == IDTEMVData.MSR_SWIPE_CAPTURED) return "MSR_SWIPE_CAPTURED";
            if (val == IDTEMVData.REQUEST_ONLINE_PIN) return "REQUEST_ONLINE_PIN";
            if (val == IDTEMVData.REQUEST_SIGNATURE) return "REQUEST_SIGNATURE";
            if (val == IDTEMVData.FALLBACK_TO_CONTACT) return "FALLBACK_TO_CONTACT";
            if (val == IDTEMVData.FALLBACK_TO_OTHER) return "FALLBACK_TO_OTHER";
            if (val == IDTEMVData.REVERSAL_REQUIRED) return "REVERSAL_REQUIRED";
            if (val == IDTEMVData.ADVISE_REQUIRED) return "ADVISE_REQUIRED";
            if (val == IDTEMVData.NO_ADVISE_REVERSAL_REQUIRED) return "NO_ADVISE_REVERSAL_REQUIRED";
            if (val == IDTEMVData.UNABLE_TO_REACH_HOST) return "UNABLE_TO_REACH_HOST";
            if (val == IDTEMVData.FILE_ARG_INVALID) return "FILE_ARG_INVALID";
            if (val == IDTEMVData.FILE_OPEN_FAILED) return "FILE_OPEN_FAILED";
            if (val == IDTEMVData.FILE_OPERATION_FAILED) return "FILE_OPERATION_FAILED";
            if (val == IDTEMVData.MEMORY_NOT_ENOUGH) return "MEMORY_NOT_ENOUGH";
            if (val == IDTEMVData.SMARTCARD_OK) return "SMARTCARD_OK";
            if (val == IDTEMVData.SMARTCARD_FAIL) return "SMARTCARD_FAIL";
            if (val == IDTEMVData.SMARTCARD_INIT_FAILED) return "SMARTCARD_INIT_FAILED";
            if (val == IDTEMVData.FALLBACK_SITUATION) return "FALLBACK_SITUATION";
            if (val == IDTEMVData.SMARTCARD_ABSENT) return "SMARTCARD_ABSENT";
            if (val == IDTEMVData.SMARTCARD_TIMEOUT) return "SMARTCARD_TIMEOUT";
            if (val == IDTEMVData.MSR_CARD_ERROR) return "MSR_CARD_ERROR";
            if (val == IDTEMVData.PARSING_TAGS_FAILED) return "PARSING_TAGS_FAILED";
            if (val == IDTEMVData.CARD_DATA_ELEMENT_DUPLICATE) return "CARD_DATA_ELEMENT_DUPLICATE";
            if (val == IDTEMVData.DATA_FORMAT_INCORRECT) return "DATA_FORMAT_INCORRECT";
            if (val == IDTEMVData.APP_NO_TERM) return "APP_NO_TERM";
            if (val == IDTEMVData.APP_NO_MATCHING) return "APP_NO_MATCHING";
            if (val == IDTEMVData.AMANDATORY_OBJECT_MISSING) return "AMANDATORY_OBJECT_MISSING";
            if (val == IDTEMVData.APP_SELECTION_RETRY) return "APP_SELECTION_RETRY";
            if (val == IDTEMVData.AMOUNT_ERROR_GET) return "AMOUNT_ERROR_GET";
            if (val == IDTEMVData.CARD_REJECTED) return "CARD_REJECTED";
            if (val == IDTEMVData.AIP_NOT_RECEIVED) return "AIP_NOT_RECEIVED";
            if (val == IDTEMVData.AFL_NOT_RECEIVEDE) return "AFL_NOT_RECEIVEDE";
            if (val == IDTEMVData.AFL_LEN_OUT_OF_RANGE) return "AFL_LEN_OUT_OF_RANGE";
            if (val == IDTEMVData.SFI_OUT_OF_RANGE) return "SFI_OUT_OF_RANGE";
            if (val == IDTEMVData.AFL_INCORRECT) return "AFL_INCORRECT";
            if (val == IDTEMVData.EXP_DATE_INCORRECT) return "EXP_DATE_INCORRECT";
            if (val == IDTEMVData.EFF_DATE_INCORRECT) return "EFF_DATE_INCORRECT";
            if (val == IDTEMVData.ISS_COD_TBL_OUT_OF_RANGE) return "ISS_COD_TBL_OUT_OF_RANGE";
            if (val == IDTEMVData.CRYPTOGRAM_TYPE_INCORRECT) return "CRYPTOGRAM_TYPE_INCORRECT";
            if (val == IDTEMVData.PSE_BY_CARD_NOT_SUPPORTED) return "PSE_BY_CARD_NOT_SUPPORTED";
            if (val == IDTEMVData.USER_LANGUAGE_SELECTED) return "USER_LANGUAGE_SELECTED";
            if (val == IDTEMVData.SERVICE_NOT_ALLOWED) return "SERVICE_NOT_ALLOWED";
            if (val == IDTEMVData.NO_TAG_FOUND) return "NO_TAG_FOUND";
            if (val == IDTEMVData.CARD_BLOCKED) return "CARD_BLOCKED";
            if (val == IDTEMVData.LEN_INCORRECT) return "LEN_INCORRECT";
            if (val == IDTEMVData.CARD_COM_ERROR) return "CARD_COM_ERROR";
            if (val == IDTEMVData.TSC_NOT_INCREASED) return "TSC_NOT_INCREASED";
            if (val == IDTEMVData.HASH_INCORRECT) return "HASH_INCORRECT";
            if (val == IDTEMVData.ARC_NOT_PRESENCED) return "ARC_NOT_PRESENCED";
            if (val == IDTEMVData.ARC_INVALID) return "ARC_INVALID";
            if (val == IDTEMVData.COMM_NO_ONLINE) return "COMM_NO_ONLINE";
            if (val == IDTEMVData.TRAN_TYPE_INCORRECT) return "TRAN_TYPE_INCORRECT";
            if (val == IDTEMVData.APP_NO_SUPPORT) return "APP_NO_SUPPORT";
            if (val == IDTEMVData.APP_NOT_SELECT) return "APP_NOT_SELECT";
            if (val == IDTEMVData.LANG_NOT_SELECT) return "LANG_NOT_SELECT";
            if (val == IDTEMVData.TERM_DATA_NOT_PRESENCED) return "TERM_DATA_NOT_PRESENCED";
            if (val == IDTEMVData.CVM_TYPE_UNKNOWN) return "CVM_TYPE_UNKNOWN";
            if (val == IDTEMVData.CVM_AIP_NOT_SUPPORTED) return "CVM_AIP_NOT_SUPPORTED";
            if (val == IDTEMVData.CVM_TAG_8E_MISSING) return "CVM_TAG_8E_MISSING";
            if (val == IDTEMVData.CVM_TAG_8E_FORMAT_ERROR) return "CVM_TAG_8E_FORMAT_ERROR";
            if (val == IDTEMVData.CVM_CODE_IS_NOT_SUPPORTED) return "CVM_CODE_IS_NOT_SUPPORTED";
            if (val == IDTEMVData.CVM_COND_CODE_IS_NOT_SUPPORTED)
                return "CVM_COND_CODE_IS_NOT_SUPPORTED";
            if (val == IDTEMVData.CVM_NO_MORE) return "CVM_NO_MORE";
            if (val == IDTEMVData.PIN_BYPASSED_BEFORE) return "PIN_BYPASSED_BEFORE";
            if (val == IDTEMVData.UNKONWN) return "UNKONWN";
            return "";
        }

        public void msgBatteryLow() {
            Log.i("WATCH", "msgBatteryLow");
        }
    }

}
