package com.idtechproducts.device.sdkdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.clearent.idtech.android.domain.CardProcessingResponse;
import com.clearent.idtech.android.family.DeviceFactory;
import com.clearent.idtech.android.PublicOnReceiverListener;
import com.clearent.idtech.android.family.device.VP3300;
import com.clearent.idtech.android.token.domain.TransactionToken;
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
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ReaderInfo.DEVICE_TYPE;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.bluetooth.BluetoothLEController;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
            case R.id.action_exit_app:
                mainView.releaseSDK();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("ValidFragment")
    public class SdkDemoFragment extends Fragment implements PublicOnReceiverListener {
        private final long BLE_ScanTimeout = 30000; //in milliseconds

        private VP3300 device;
        private static final int REQUEST_ENABLE_BT = 1;
        private long totalEMVTime;
        private boolean calcLRC = true;

        private BluetoothAdapter mBtAdapter = null;

        private TextView status;
        private TextView infoText;
        private EditText textAmount;
        private View rootView;
        private LayoutInflater layoutInflater;
        private ViewGroup viewGroup;
        private AlertDialog alertSwipe;

        private String info = "";
        private String detail = "";
        private Handler handler = new Handler();

        private StructConfigParameters config = null;
        private EditText edtSelection;

        private Button swipeButton;
        private Button commandBtn;
        private final int emvTimeout = 30;

        private boolean btleDeviceRegistered = false;
        private String btleDeviceAddress = "00:1C:97:14:FD:34";

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

            swipeButton.setEnabled(false);
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
            swipeButton.setEnabled(true);
            commandBtn.setEnabled(true);
            info += "Card reader is ready for use.\n";
            handler.post(doUpdateStatus);
        }

        @Override
        public void successfulTransactionToken(final TransactionToken transactionToken) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    info += "Please remove card\n";
                    info += "Card is now represented by a transaction token: " + transactionToken.getTransactionToken() + "\n";

                    ResDataStruct resData = new ResDataStruct();
                   // completeTransaction(resData);

                    handler.post(doUpdateStatus);
                    if (alertSwipe != null && alertSwipe.isShowing()) {
                        alertSwipe.dismiss();
                    }
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
                }
            });
            runSampleTransaction(transactionToken);
        }

        @Override
        public void handleCardProcessingResponse(CardProcessingResponse cardProcessingResponse) {
            switch (cardProcessingResponse) {
                case TERMINATE:
                case USE_MAGSTRIPE:
                    break;
                case USE_CHIP_READER:
                    info += "Card has chip. Try insert.\n";
                    handler.post(doUpdateStatus);
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
                    if (alertSwipe != null && alertSwipe.isShowing()) {
                        alertSwipe.dismiss();
                    }
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
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
                    if (alertSwipe != null && alertSwipe.isShowing()) {
                        alertSwipe.dismiss();
                    }
            }
        }

        @Override
        public void handleConfigurationErrors(String message) {
            info = "The reader failed to configure. Error - " + message;
            swipeButton.setEnabled(false);
            commandBtn.setEnabled(false);
        }

        private void runSampleTransaction(TransactionToken transactionToken) {
            SampleTransaction sampleTransaction = new SampleTransactionImpl();
            PostTransactionRequest postTransactionRequest = new PostTransactionRequest();
            ;
            postTransactionRequest.setTransactionToken(transactionToken);
            postTransactionRequest.setApiKey("24425c33043244778a188bd19846e860");
            postTransactionRequest.setBaseUrl("https://gateway-sb.clearent.net");
            SaleTransaction saleTransaction;
            if (textAmount == null || textAmount.getText().toString() == null || textAmount.getText().toString().length() == 0) {
                saleTransaction = new SaleTransaction("1.00");
            } else {
                saleTransaction = new SaleTransaction(textAmount.getText().toString());
            }
            postTransactionRequest.setSaleTransaction(saleTransaction);
            sampleTransaction.doSale(postTransactionRequest, this);
        }

        public void initializeReader() {
            if (device != null) {
                releaseSDK();
            }

            //Gather the context needed to get a device object representing the card reader.
            DemoApplicationContext demoApplicationContext = new DemoApplicationContext(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ, this, getActivity(), "https://gateway-sb.clearent.net", "307a301406072a8648ce3d020106092b240303020801010c036200042b0cfb3a1faaca8fb779081717a0bafb03e0cb061a1ef297f75dc5b951aaf163b0c2021e9bb73071bf89c711070e96ab1b63c674be13041d9eb68a456eb6ae63a97a9345c120cd8bff1d5998b2ebbafc198c5c5b26c687bfbeb68b312feb43bf", getIDTechAndroidDeviceConfigurationXmlFile());
            device = DeviceFactory.getVP3300(demoApplicationContext);
            device.device_configurePeripheralAndConnect();
            Toast.makeText(getActivity(), "get started", Toast.LENGTH_LONG).show();
            displaySdkInfo();
        }

        private EditText edtBTLE_Name;
        private Dialog dlgBTLE_Name;

        void openReaderSelectDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Select a device:");
            builder.setCancelable(false);
            builder.setItems(R.array.reader_type, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {

                        case 0:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_AJ))
                                Toast.makeText(getActivity(), "VP3300 Audio Jack (Audio Jack) is selected", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
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
                                btnBTLE_Ok.setOnClickListener(setBTLE_NameOnClick);
                                dlgBTLE_Name.show();
                            } else
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            return;
                        case 3:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT_USB))
                                Toast.makeText(getActivity(), "VP3300 Bluetooth (USB) is selected", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            if (device.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_USB))
                                Toast.makeText(getActivity(), "VP3300 USB is selected", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), "Failed. Please disconnect first.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_BT) {
                        device.registerListen();
                    }
                }
            });

            swipeButton.setEnabled(false);
            commandBtn.setEnabled(false);

            builder.create().show();
        }

        private View.OnClickListener setBTLE_NameOnClick = new View.OnClickListener() {
            public void onClick(View v) {
                dlgBTLE_Name.dismiss();
                //set to device mac address instead of friendly name to see if the the bluetooth works better
                //String deviceId = "00:1C:97:15:B0:43";
                //Common.setBLEDeviceName(deviceId);
                Common.setBLEDeviceName(edtBTLE_Name.getText().toString());

                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
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
                if (!mBtAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    scanLeDevice(true, BLE_ScanTimeout);
                }
            }
        };

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (device.device_getDeviceType() == DEVICE_TYPE.DEVICE_VP3300_BT) {
                // TODO Auto-generated method stub
                if (requestCode == REQUEST_ENABLE_BT) {

                    if (resultCode == Activity.RESULT_OK) {
                        Toast.makeText(getActivity(), "Bluetooth has turned on, now searching for device", Toast.LENGTH_SHORT).show();
                        //start scaning
                        scanLeDevice(true, BLE_ScanTimeout);
                    } else {
                        // User did not enable Bluetooth or an error occurred
                        Toast.makeText(getActivity(), "Problem in Bluetooth Turning ON", Toast.LENGTH_SHORT).show();
                        swipeButton.setEnabled(true);
                        commandBtn.setEnabled(true);
                    }
                }
            }
        }

        private void scanLeDevice(final boolean enable, long timeout) {

            if (enable) {
                handler.postDelayed(new Runnable() {
                    public void run() {
                        mBtAdapter.stopLeScan(mLeScanCallback);
                    }
                }, timeout);
                mBtAdapter.startLeScan(mLeScanCallback);
            } else {
                mBtAdapter.stopLeScan(mLeScanCallback);
            }
        }

        private BluetoothAdapter.LeScanCallback mLeScanCallback =
                new BluetoothAdapter.LeScanCallback() {
                    public void onLeScan(final BluetoothDevice btledevice, int rssi,
                                         byte[] scanRecord) {
                        String BLE_Id = Common.getBLEDeviceName();
                        if (BLE_Id != null) {
                            if (BLE_Id.length() == 17 && BLE_Id.charAt(2) == ':' && BLE_Id.charAt(5) == ':' && BLE_Id.charAt(8) == ':' &&
                                    BLE_Id.charAt(11) == ':' && BLE_Id.charAt(14) == ':')  //search by address
                            {
                                String deviceAddress = btledevice.getAddress();
                                if (deviceAddress != null && deviceAddress.equalsIgnoreCase(BLE_Id))  //found the device by address
                                {
                                    BluetoothLEController.setBluetoothDevice(btledevice);
                                    btleDeviceAddress = deviceAddress;
                                    if (!btleDeviceRegistered) {
                                        device.registerListen();
                                        btleDeviceRegistered = true;
                                    }
                                }
                            } else  //search by name
                            {
                                String deviceName = btledevice.getName();
                                String deviceAddress = btledevice.getAddress();
Log.i("WATCH", "bt device name" + deviceName);
Log.i("WATCH", "bt device address " +  deviceAddress);
                                if (deviceName != null && deviceName.startsWith(BLE_Id))  //found the device by name
                                {
                                    BluetoothLEController.setBluetoothDevice(btledevice);
                                    btleDeviceAddress = btledevice.getAddress();
                                    if (!btleDeviceRegistered) {
                                        device.registerListen();
                                        btleDeviceRegistered = true;
                                    }
                                }
                            }
                        }
                    }
                };


        Dialog dlgMenu;
        Dialog dlgLanguageMenu;
        byte type;
        String[] theLines;
        byte[] Language;
        byte MessageId;
        int finalTimout;

        public void lcdDisplay(int mode, String[] lines, int timeout) {
            if (lines != null && lines.length > 0) {
                //framework notifies both methods. Removing dups.
                if (lines[0].contains("SWIPE OR INSERT") || lines[0].contains("PLEASE WAIT") || lines[0].contains("PROCESSING") || lines[0].contains("GO ONLINE") || lines[0].contains("TERMINATE") || lines[0].contains("USE MAGSTRIPE")) {
                    return;
                }
                info += "\n";
                Log.i("WATCH1", lines[0]);
                info += lines[0] + "\n";
                handler.post(doUpdateStatus);
                String checkReceiptMessage = "Sample Transaction successful. Transaction Id:";
                if (lines[0].contains(checkReceiptMessage)) {
                    runSampleReceipt(lines[0]);
                }
            }

        }

        private void runSampleReceipt(String line) {
            String[] parts = line.split(":");
            ReceiptRequest receiptRequest = new ReceiptRequest();
            receiptRequest.setApiKey("24425c33043244778a188bd19846e860");
            receiptRequest.setBaseUrl("https://gateway-sb.clearent.net");
            ReceiptDetail receiptDetail = new ReceiptDetail();
            receiptDetail.setEmailAddress("dhigginbotham@clearent.com,bguntli@clearent.com");
            receiptDetail.setTransactionId(parts[1]);
            receiptRequest.setReceiptDetail(receiptDetail);

            SampleReceipt sampleReceipt = new SampleReceiptImpl();
            sampleReceipt.doReceipt(receiptRequest, this);
        }

        public void lcdDisplay(int mode, String[] lines, int timeout, byte[] languageCode, byte messageId) {
            type = (byte) mode;
            theLines = lines;

            if (theLines == null || theLines.length == 0) {
                return;
            }

            finalTimout = timeout;
            Language = languageCode;
            MessageId = messageId;
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (type == 0x01) //Application Menu Display
                    {
                        dlgMenu = new Dialog(getActivity());
                        dlgMenu.setTitle("Application Menu");
                        dlgMenu.setCancelable(false);
                        dlgMenu.setContentView(R.layout.emv_menu_display_dialog);
                        TextView tv = new TextView(getActivity());
                        tv = (TextView) dlgMenu.findViewById(R.id.tvApplication);
                        String strApplication = "";
                        for (int x = 0; x < theLines.length; x++) {
                            strApplication = (strApplication + theLines[x] + "\r\n");
                        }
                        tv.setText(strApplication);

                        edtSelection = (EditText) dlgMenu.findViewById(R.id.edtAppSelection);

                        Button btnMenuDisplayOK = (Button) dlgMenu.findViewById(R.id.btnMenuDisplayOK);
                        btnMenuDisplayOK.setOnClickListener(menuDisplayOKOnClick);
                        dlgMenu.show();

                        //wait for timeout
                        dialogId = 3;
                        timerDelayRemoveDialog((long) finalTimout * 1000, dlgMenu);
                    } else if (type == 0x02) //Normal Display Get Function Key
                    {

                    } else if (type == 0x08) //Language Menu Display
                    {
                        //Open a language menu
                        dlgLanguageMenu = new Dialog(getActivity());
                        dlgLanguageMenu.setContentView(R.layout.language_menu);

                        final ListView lv = (ListView) dlgLanguageMenu.findViewById(R.id.lvLanguage);

                        final int[] lineNum = new int[theLines.length];

                        for (int i = 0; i < theLines.length; i++) {
                            lineNum[i] = Integer.valueOf(theLines[i].substring(0, 1));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.single_row_textview,
                                R.id.tvLanguage, theLines);

                        lv.setAdapter(adapter);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                device.emv_lcdControlResponse((byte) 0x08, (byte) (lineNum[position] & 0xFF));
                                dlgLanguageMenu.dismiss();
                            }
                        });

                        dlgLanguageMenu.setCancelable(false);
                        dlgLanguageMenu.setTitle("Select Language");
                        dlgLanguageMenu.show();

                        //wait for timeout
                        dialogId = 2;
                        timerDelayRemoveDialog((long) finalTimout * 1000, dlgLanguageMenu);
                    } else {
                        Log.i("WATCH2", theLines[0]);
                        info += theLines[0] + "\n";
                        handler.post(doUpdateStatus);
                    }
                }
            });
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

        public void displaySdkInfo() {
            info = "Manufacturer: " + android.os.Build.MANUFACTURER + "\n" +
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
        private Runnable doSwipeProgressBar = new Runnable() {

            public void run() {
                if (startSwipe) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Please swipe/tap a card");
                    builder.setView(layoutInflater.inflate(R.layout.frame_swipe, viewGroup, false));
                    builder.setCancelable(false);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            int ret = device.msr_cancelMSRSwipe();
                            if (ret == ErrorCode.SUCCESS) {
                                infoText.setText("Swipe cancelled");
                            } else {
                                infoText.setText("Failed to cancel swipe");
                            }
                            swipeButton.setEnabled(true);
                            commandBtn.setEnabled(true);
                        }
                    });
                    alertSwipe = builder.create();
                    alertSwipe.show();
                }
            }

        };

        private boolean startSwipe = false;

        private class SwipeButtonListener implements OnClickListener {
            public void onClick(View arg0) {
                int ret;
                startSwipe = true;
                totalEMVTime = System.currentTimeMillis();
                textAmount = (EditText) findViewById(R.id.textAmount);

                ret = device.device_startTransaction(1.00, 0.00, 0, 30, null);

                swipeButton.setEnabled(false);
                commandBtn.setEnabled(false);

                if (ret == ErrorCode.SUCCESS) {
                    info = "Please swipe/tap a card\n";
                    detail = "";
                    handler.post(doSwipeProgressBar);
                    handler.post(doUpdateStatus);
                } else if (ret == ErrorCode.RETURN_CODE_OK_NEXT_COMMAND) {
                    info = "Start EMV transaction\n";
                    detail = "";
                    handler.post(doUpdateStatus);
                } else {
                    info = "cannot swipe/tap card\n";
                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    detail = "";
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
                    handler.post(doUpdateStatus);
                }
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
            status.setText("Connected");
            if (!Common.getBootLoaderMode()) {
                String device_name = device.device_getDeviceType().toString();
                info = device_name.replace("DEVICE_", "");
                info += " Reader is connected\r\n";
                if (info.startsWith("VP3300_BT Reader"))
                    info += "Address: " + btleDeviceAddress;
                detail = "";
                handler.post(doUpdateStatus);
            }
            Toast.makeText(getActivity(), "Reader Connected", Toast.LENGTH_LONG).show();
        }

        public void deviceDisconnected() {
            if (alertSwipe != null)
                if (alertSwipe.isShowing())
                    alertSwipe.dismiss();
            status.setText("Disconnected");

            swipeButton.setEnabled(false);
            commandBtn.setEnabled(false);

            if (!Common.getBootLoaderMode()) {
                info = "Reader is disconnected";
                detail = "";
                handler.post(doUpdateStatus);
            }
        }

        public void timeout(int errorCode) {
            if (alertSwipe != null && alertSwipe.isShowing())
                alertSwipe.dismiss();
            info = ErrorCodeInfo.getErrorCodeDescription(errorCode);
            detail = "";
            handler.post(doUpdateStatus);
            swipeButton.setEnabled(true);
            commandBtn.setEnabled(true);
        }

        public void ICCNotifyInfo(byte[] dataNotify, String strMessage) {
            if (strMessage != null && strMessage.length() > 0) {
                String strHexResp = Common.getHexStringFromBytes(dataNotify);
                Log.d("Demo Info >>>>>", "dataNotify=" + strHexResp);

                info = "ICC Notification Info: " + strMessage + "\n" + "Resp: " + strHexResp;
                detail = "";
                handler.post(doUpdateStatus);
            }
        }

        public void msgToConnectDevice() {
            info = "Connecting a reader (when connecting for the first time EMV configuration is applied)...\n";
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void msgAudioVolumeAdjustFailed() {
            info = "SDK could not adjust volume...";
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void LoadXMLConfigFailureInfo(int index, String strMessage) {
            info = "XML loading error...";
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

        public void msgBatteryLow() {
            // TODO Auto-generated method stub

        }
    }

}
