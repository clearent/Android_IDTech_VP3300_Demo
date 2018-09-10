package com.idtechproducts.device.sdkdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.clearent.device.Clearent_VP3300;
import com.clearent.device.PublicOnReceiverListener;
import com.dbconnection.dblibrarybeta.ProfileManager;
import com.dbconnection.dblibrarybeta.ProfileUtility;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ErrorCodeInfo;
import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.IDT_VP3300;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.OnReceiverListenerPINRequest;
import com.idtechproducts.device.ReaderInfo.DEVICE_TYPE;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.audiojack.UMLog;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateToolMsg;
import com.idtechproducts.device.bluetooth.BluetoothLEController;

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
import android.os.Environment;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dbconnection.dblibrarybeta.*;

public class UnifiedSDK_Demo extends ActionBarActivity {

    private SdkDemoFragment mainView = null;
    public StructConfigParameters debugProfile;

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
            case R.id.action_start_autoconfig:
                mainView.runAutoConfig();
                break;
            case R.id.action_start_rki:
                mainView.startRemoteKeyInjection();
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


    public class SdkDemoFragment extends Fragment implements PublicOnReceiverListener, OnReceiverListenerPINRequest, FirmwareUpdateToolMsg, RESTResponse {
        private final long BLE_ScanTimeout = 5000; //in milliseconds

        private Clearent_VP3300 device;
        private FirmwareUpdateTool fwTool;
        private static final int REQUEST_ENABLE_BT = 1;
        private long totalEMVTime;
        private boolean calcLRC = true;

        private BluetoothAdapter mBtAdapter = null;

        private TextView status;
        private TextView infoText;
        private TextView dataText;
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

        private boolean isFwInitDone = false;
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

        private ProfileManager profileManager;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            layoutInflater = inflater;
            viewGroup = container;
            rootView = inflater.inflate(R.layout.fragment_main, container, false);

            status = (TextView) rootView.findViewById(R.id.status_text);
            status.setText("Disconnected");

            infoText = (TextView) rootView.findViewById(R.id.text_area_top);
            dataText = (TextView) rootView.findViewById(R.id.text_area_bottom);
            dataText.setVerticalScrollBarEnabled(true);

            swipeButton = (Button) rootView.findViewById(R.id.btn_swipeCard);
            swipeButton.setOnClickListener(new SwipeButtonListener());

            commandBtn = (Button) rootView.findViewById(R.id.btn_command);
            commandBtn.setOnClickListener(new CommandButtonListener());
            profileManager = new ProfileManager(this);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            if (device != null)
                device.unregisterListen();

            initializeReader();
            openReaderSelectDialog();

            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onDestroy() {
            if (device != null)
                device.unregisterListen();
            super.onDestroy();
        }

        @Override
        public void successfulTransactionToken(String transactionToken) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    handler.post(doUpdateStatus);
                    dlgCompleteEMV = new Dialog(getActivity());
                    dlgCompleteEMV.setTitle("Successful transaction token found");
                    dlgCompleteEMV.setCancelable(false);
                    dlgCompleteEMV.setContentView(R.layout.complete_emv_one_option_dialog);
                    Button btnCompleteEMV = (Button) dlgCompleteEMV.findViewById(R.id.btnCompleteEMV);
                    btnCompleteEMV.setOnClickListener(onlineApprovedOnClick);
                    Button btnCompCancel = (Button) dlgCompleteEMV.findViewById(R.id.btnCompEMVOneCancel);
                    btnCompCancel.setOnClickListener(authCompCancelOnClick);
                    dialogId = 1;
                    dlgCompleteEMV.show();
                }
            });
        }

        public void initializeReader() {
            if (device != null) {
                releaseSDK();
            }
            device = new Clearent_VP3300(this, this, getActivity(), "https://mobile-devices-qa.clearent.net", "307a301406072a8648ce3d020106092b240303020801010c036200042b0cfb3a1faaca8fb779081717a0bafb03e0cb061a1ef297f75dc5b951aaf163b0c2021e9bb73071bf89c711070e96ab1b63c674be13041d9eb68a456eb6ae63a97a9345c120cd8bff1d5998b2ebbafc198c5c5b26c687bfbeb68b312feb43bf");
            profileManager.doGet();
            Toast.makeText(getActivity(), "get started", Toast.LENGTH_LONG).show();
            device.log_setVerboseLoggingEnable(true);
            fwTool = new FirmwareUpdateTool(this, getActivity());
            displaySdkInfo();
        }

        void runAutoConfig() {
            config = null;
            String filepath = getXMLFileFromRaw();
            if (!isFileExist(filepath)) {
                filepath = null;
            }
            if (ErrorCode.SUCCESS == device.autoConfig_start(filepath))
                Toast.makeText(getActivity(), "AutoConfig started", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "AutoConfig not started", Toast.LENGTH_SHORT).show();
        }

        void startRemoteKeyInjection() {
            int ret = device.device_startRKI();
            if (ret != ErrorCode.SUCCESS) {
                info = "SDK could not start RKI: " + ErrorCodeInfo.getErrorCodeDescription(ret);
                detail = "";
            } else {
                info = "Remote key injection process started...\nPlease wait...";
                detail = "";
            }
            handler.post(doUpdateStatus);

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
                    device.setIDT_Device(fwTool);
                    if (device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_BT)
                        device.registerListen();
                }
            });
            builder.create().show();
        }

        private View.OnClickListener setBTLE_NameOnClick = new View.OnClickListener() {
            public void onClick(View v) {
                dlgBTLE_Name.dismiss();
                Common.setBLEDeviceName(edtBTLE_Name.getText().toString());
                device.setIDT_Device(fwTool);
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

        private String[] fw_commands = null;

        void openReaderSelectDialogForFwUpdate() {
            if (device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_AJ_USB && device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_USB &&
                    device.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_BT_USB) {
                Toast.makeText(getActivity(), "Only support VP3300 through USB", Toast.LENGTH_LONG).show();
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

                    String fileNames[] = {"IDT_Firmware_VP3300_AJ.txt", "IDT_Firmware_VP3300_BT.txt", "IDT_Firmware_VP3300_USB.txt"};

                    String path = Environment.getExternalStorageDirectory().toString();
                    String fileNameWithPath = path + java.io.File.separator + fileNames[which];

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
                        detail = "Please do not unplug the reader.";
                        swipeButton.setEnabled(false);
                        commandBtn.setEnabled(false);
                        Toast.makeText(getActivity(), "FW update started initialization.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "FW update initialization cannot be started.", Toast.LENGTH_LONG).show();
                        detail = "";
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

        Dialog dlgOnlineAuth = null;
        Dialog dlgCompleteEMV = null;
        Dialog dlgMSRFallback = null;

        /////////Callback functions for EMV/////////
        private View.OnClickListener authenticateOnClick = new View.OnClickListener() {
            public void onClick(View v) {
                //next step
                int ret = startAuthentication(_resData);
                dlgOnlineAuth.dismiss();
                if (ret == ErrorCode.RETURN_CODE_OK_NEXT_COMMAND) {
                    swipeButton.setEnabled(false);
                    commandBtn.setEnabled(false);
                } else {
                    info = "EMV Transaction Failed\n";
                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
                }
                handler.post(doUpdateStatus);
            }
        };

        private View.OnClickListener onlineApprovedOnClick = new View.OnClickListener() {
            public void onClick(View v) {
                //next step
                ResDataStruct resData = new ResDataStruct();
                int ret = completeTransaction(resData);
                if (ret == ErrorCode.RETURN_CODE_OK_NEXT_COMMAND) {
                    swipeButton.setEnabled(false);
                    commandBtn.setEnabled(false);
                } else {
                    info = "EMV Transaction Failed\n";
                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
                }
                dlgCompleteEMV.dismiss();
                handler.post(doUpdateStatus);
            }

        };

        private View.OnClickListener authCompCancelOnClick = new View.OnClickListener() {
            public void onClick(View v) {
                ResDataStruct resData = new ResDataStruct();
                device.emv_cancelTransaction(resData);
                if (dialogId == 0)
                    dlgOnlineAuth.dismiss();
                else if (dialogId == 1)
                    dlgCompleteEMV.dismiss();

                info = "EMV Transaction Cancelled";
                handler.post(doUpdateStatus);
                swipeButton.setEnabled(true);
                commandBtn.setEnabled(true);
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
            info = lines[0];
            handler.post(doUpdateStatus);
//TODO send messages here. mode 0 ? timeout 0 ?
        }

        public void lcdDisplay(int mode, String[] lines, int timeout, byte[] languageCode, byte messageId) {
            type = (byte) mode;
            theLines = lines;
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
                        info = theLines[0];
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

        public int startAuthentication(ResDataStruct resData) {
            byte[] tlvElement;
            tlvElement = null;
            return device.emv_authenticateTransaction(tlvElement);
        }

        public int completeTransaction(ResDataStruct resData) {
//			byte[] authResponseCode = new byte[]{(byte)0x30, 0x30};
            byte[] authResponseCode = new byte[2];
            System.arraycopy(tag8A, 0, authResponseCode, 0, 2);
            byte[] issuerAuthData = new byte[]{(byte) 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, 0x30, 0x30};
            byte[] tlvScripts = null;
            byte[] value = null;
            return device.emv_completeTransaction(false, authResponseCode, issuerAuthData, tlvScripts, value);
        }

        private ResDataStruct _resData;
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
                dataText.setText(detail);
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

//				ret = device.msr_startMSRSwipe();
                ret = device.device_startTransaction(1.00, 0.00, 0, 30, null);

                swipeButton.setEnabled(false);
                commandBtn.setEnabled(false);

                if (ret == ErrorCode.SUCCESS) {
                    info = "Please swipe/tap a card";
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
                            case 3:
                                ResDataStruct configData = new ResDataStruct();
                                ret = device.device_reviewAllSetting(configData);
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Global Configuration: \n";
                                    detail = "";
                                    byte[][] tlvGroups = Common.processTLVGroups(configData.resData);

                                    if (tlvGroups != null) {
                                        for (int i = 0; i < tlvGroups.length; i++) {
                                            detail += "Group (FFE4) " + i + ":\r\n";
                                            detail += "TLV data:\r\n";
                                            detail += Common.getHexStringFromBytes(tlvGroups[i]);

                                            Map<String, Map<String, byte[]>> TLVDict = Common.processTLV(tlvGroups[i]);
                                            Map<String, byte[]> tags = TLVDict.get("tags");
                                            Map<String, byte[]> maskedTags = TLVDict.get("masked");
                                            Map<String, byte[]> encryptedTags = TLVDict.get("encrypted");

                                            if (!tags.isEmpty()) {
                                                detail += "\r\n\r\nUnencrypted Tags:\r\n";
                                                Set<String> keys = tags.keySet();
                                                for (String key : keys) {
                                                    detail += key + ": ";
                                                    byte[] data = tags.get(key);
                                                    detail += Common.getHexStringFromBytes(data) + "\r\n";
                                                }
                                            }
                                            if (!maskedTags.isEmpty()) {
                                                detail += "Masked Tags:\r\n";
                                                Set<String> keys = maskedTags.keySet();
                                                for (String key : keys) {
                                                    detail += key + ": ";
                                                    byte[] data = maskedTags.get(key);
                                                    detail += Common.getHexStringFromBytes(data) + "\r\n";
                                                }
                                            }
                                            if (!encryptedTags.isEmpty()) {
                                                detail += "Encrypted Tags:\r\n";
                                                Set<String> keys = encryptedTags.keySet();
                                                for (String key : keys) {
                                                    detail += key + ": ";
                                                    byte[] data = encryptedTags.get(key);
                                                    detail += Common.getHexStringFromBytes(data) + "\r\n";
                                                }
                                            }
                                            detail += "\r\n";
                                        }
                                    } else
                                        detail += "data == null\r\n";
                                } else {
                                    info = "Global Configuration: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                handler.post(doUpdateStatus);
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
                            case 6:
                                ret = device.device_setPollMode((byte) 0x00);  //Auto Poll On
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Auto Poll On: Successful\n";
                                } else {
                                    info = "Auto Poll On: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 7:
                                ret = device.device_setPollMode((byte) 0x01);  //Auto Poll Off
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Auto Poll Off: Successful\n";
                                } else {
                                    info = "Auto Poll Off: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                }
                                detail = "";
                                handler.post(doUpdateStatus);
                                break;
                            case 8:
                                IDTMSRData cardData = new IDTMSRData();
                                ret = device.device_getTransactionResults(cardData);
                                if (ret == ErrorCode.SUCCESS) {
                                    info = "Get Transaction Result: ";
                                    detail = Common.parse_MSRData(device.device_getDeviceType(), cardData);
                                    if (detail.isEmpty())
                                        info += "No data was returned\n";
                                    else
                                        info += "Successful\n";
                                } else {
                                    info = "Get Transaction Result: Failed\n";
                                    info += "Status: " + device.device_getResponseCodeString(ret) + "";
                                    detail = "";
                                }
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
                            case 14:
                                dlgSendCAPDU = new Dialog(getActivity());
                                dlgSendCAPDU.setTitle("Please Enter C-APDU");
                                dlgSendCAPDU.setCancelable(false);
                                dlgSendCAPDU.setContentView(R.layout.apdu_dialog);
                                Button btnSendCAPDU = (Button) dlgSendCAPDU.findViewById(R.id.btnSendCAPDU);
                                Button btnCancelCAPDU = (Button) dlgSendCAPDU.findViewById(R.id.btnCancelAPDU);
                                edtCAPDU = (EditText) dlgSendCAPDU.findViewById(R.id.edtCAPDU);
                                if (_first_strAPDU != null) {
                                    if (_first_strAPDU.length() > 0)
                                        edtCAPDU.setText(_first_strAPDU);
                                }
                                btnSendCAPDU.setOnClickListener(sendCAPDUOnClick);
                                btnCancelCAPDU.setOnClickListener(cancelCAPDUOnClick);
                                dlgSendCAPDU.show();
                                break;
                            case 15:
                                dlgStartEMV = new Dialog(getActivity());
                                dlgStartEMV.setTitle("Start EMV");
                                dlgStartEMV.setCancelable(false);
                                dlgStartEMV.setContentView(R.layout.start_emv_dialog);
                                CheckBox checkbox_auth = (CheckBox) dlgStartEMV.findViewById(R.id.checkbox_auth);
                                CheckBox checkbox_comp = (CheckBox) dlgStartEMV.findViewById(R.id.checkbox_comp);
                                checkbox_auth.setOnClickListener(onAuthCheckBoxClick);
                                checkbox_auth.setChecked(true);
                                Clearent_VP3300.emv_setAutoAuthenticateTransaction(true);
                                checkbox_comp.setOnClickListener(onCompCheckBoxClick);
                                checkbox_comp.setChecked(false);
                                Clearent_VP3300.emv_setAutoCompleteTransaction(false);
                                Button btnStartEMV = (Button) dlgStartEMV.findViewById(R.id.btnStartEMV);
                                Button btnCancel = (Button) dlgStartEMV.findViewById(R.id.btnCancel);
                                edtAmount = (EditText) dlgStartEMV.findViewById(R.id.edtAmount);
                                edt8A = (EditText) dlgStartEMV.findViewById(R.id.edt8A);
                                edt8A.setText(Common.base16Encode(tag8A).toCharArray(), 0, 4);
                                btnStartEMV.setOnClickListener(startEMVOnClick);
                                btnCancel.setOnClickListener(cancelOnClick);
                                dlgStartEMV.show();
                                break;
                            case 16:
                                dlgAID = new Dialog(getActivity());
                                dlgAID.setTitle("Select AID Function");
                                dlgAID.setCancelable(false);
                                dlgAID.setContentView(R.layout.aid_dialog);
                                Button btnShowAID = (Button) dlgAID.findViewById(R.id.btnShowAID);
                                Button btnRemoveAID = (Button) dlgAID.findViewById(R.id.btnRemoveAID);
                                Button btnCreateAID = (Button) dlgAID.findViewById(R.id.btnCreateAID);
                                Button btnShowAIDList = (Button) dlgAID.findViewById(R.id.btnShowAIDList);
                                Button btnAIDCancel = (Button) dlgAID.findViewById(R.id.btnAIDCancel);
                                btnShowAID.setOnClickListener(showAIDOnClick);
                                btnRemoveAID.setOnClickListener(removeAIDOnClick);
                                btnCreateAID.setOnClickListener(createAIDOnClick);
                                btnShowAIDList.setOnClickListener(showAIDListOnClick);
                                btnAIDCancel.setOnClickListener(AIDCancelOnClick);
                                dlgAID.show();
                                break;
                            case 17:
                                dlgCAPK = new Dialog(getActivity());
                                dlgCAPK.setTitle("Select CAPK Function");
                                dlgCAPK.setCancelable(false);
                                dlgCAPK.setContentView(R.layout.capk_dialog);
                                Button btnShowCAPK = (Button) dlgCAPK.findViewById(R.id.btnShowCAPK);
                                Button btnRemoveCAPK = (Button) dlgCAPK.findViewById(R.id.btnRemoveCAPK);
                                Button btnCreateCAPK = (Button) dlgCAPK.findViewById(R.id.btnCreateCAPK);
                                Button btnShowCAPKList = (Button) dlgCAPK.findViewById(R.id.btnShowCAPKList);
                                Button btnCAPKCancel = (Button) dlgCAPK.findViewById(R.id.btnCAPKCancel);
                                btnShowCAPK.setOnClickListener(showCAPKOnClick);
                                btnRemoveCAPK.setOnClickListener(removeCAPKOnClick);
                                btnCreateCAPK.setOnClickListener(createCAPKOnClick);
                                btnShowCAPKList.setOnClickListener(showCAPKListOnClick);
                                btnCAPKCancel.setOnClickListener(CAPKCancelOnClick);
                                dlgCAPK.show();
                                break;
                            case 18:
                                dlgCRL = new Dialog(getActivity());
                                dlgCRL.setTitle("Select CRL Function");
                                dlgCRL.setCancelable(false);
                                dlgCRL.setContentView(R.layout.crl_dialog);
                                Button btnRemoveCRL = (Button) dlgCRL.findViewById(R.id.btnRemoveCRL);
                                Button btnCreateCRL = (Button) dlgCRL.findViewById(R.id.btnCreateCRL);
                                Button btnShowCRLList = (Button) dlgCRL.findViewById(R.id.btnShowCRLList);
                                Button btnCRLCancel = (Button) dlgCRL.findViewById(R.id.btnCRLCancel);
                                btnRemoveCRL.setOnClickListener(removeCRLOnClick);
                                btnCreateCRL.setOnClickListener(createCRLOnClick);
                                btnShowCRLList.setOnClickListener(showCRLListOnClick);
                                btnCRLCancel.setOnClickListener(CRLCancelOnClick);
                                dlgCRL.show();
                                break;
                            case 19:
                                dlgTerminal = new Dialog(getActivity());
                                dlgTerminal.setTitle("Terminal Settings");
                                dlgTerminal.setCancelable(false);
                                dlgTerminal.setContentView(R.layout.terminal_dialog);
                                Button btnRemoveTML = (Button) dlgTerminal.findViewById(R.id.btnRemoveTML);
                                Button btnCreateTML = (Button) dlgTerminal.findViewById(R.id.btnCreateTML);
                                Button btnShowTML = (Button) dlgTerminal.findViewById(R.id.btnShowTML);
                                Button btnTMLCancel = (Button) dlgTerminal.findViewById(R.id.btnTMLCancel);
                                btnRemoveTML.setOnClickListener(removeTMLOnClick);
                                btnCreateTML.setOnClickListener(createTMLOnClick);
                                btnShowTML.setOnClickListener(showTMLOnClick);
                                btnTMLCancel.setOnClickListener(TMLCancelOnClick);
                                dlgTerminal.show();
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

            public int startEMVTransaction(ResDataStruct resData) {
                totalEMVTime = System.currentTimeMillis();
                if (edtAmount == null || edtAmount.getText().toString() == null || edtAmount.getText().toString().length() == 0)
                    return ErrorCode.INVALID_PARAMETER;
                double dAmount = Double.parseDouble(edtAmount.getText().toString());

                if (edt8A == null || edt8A.getText().toString() == null || edt8A.getText().toString().length() != 4)
                    return ErrorCode.INVALID_PARAMETER;
                tag8A = Common.getBytesFromHexString(edt8A.getText().toString());

                byte tags[] = {(byte) 0xDF, (byte) 0xEF, 0x1F, 0x02, 0x01, 0x00};
                Clearent_VP3300.emv_allowFallback(true);
                if (Clearent_VP3300.emv_getAutoAuthenticateTransaction())
                    return device.emv_startTransaction(dAmount, 0.00, 0, emvTimeout, tags, false);
                else
                    return device.emv_startTransaction(dAmount, 0.00, 0, emvTimeout, null, false);
            }

            private View.OnClickListener onAuthCheckBoxClick = new View.OnClickListener() {
                public void onClick(View v) {
                    // Is the view now checked?
                    boolean checked = ((CheckBox) v).isChecked();

                    switch (v.getId()) {
                        case R.id.checkbox_auth:
                            Clearent_VP3300.emv_setAutoAuthenticateTransaction(checked);
                            break;
                    }
                }
            };

            private View.OnClickListener onCompCheckBoxClick = new View.OnClickListener() {
                public void onClick(View v) {
                    // Is the view now checked?
                    boolean checked = ((CheckBox) v).isChecked();

                    switch (v.getId()) {
                        case R.id.checkbox_comp:
                            Clearent_VP3300.emv_setAutoCompleteTransaction(checked);
                            break;
                    }
                }
            };

            private View.OnClickListener startEMVOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    ResDataStruct resData = new ResDataStruct();
                    info = "Processing EMV Transaction.  Please wait...\n";
                    detail = "";
                    handler.post(doUpdateStatus);

                    int ret = startEMVTransaction(resData);
                    dlgStartEMV.dismiss();
                    if (ret == ErrorCode.RETURN_CODE_OK_NEXT_COMMAND) {
                        swipeButton.setEnabled(false);
                        commandBtn.setEnabled(false);
                        Toast.makeText(getActivity(), "Processing EMV Transaction...", Toast.LENGTH_LONG).show();
                    } else {
                        info = "EMV Transaction Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                }

            };

            private View.OnClickListener cancelOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    dlgStartEMV.cancel();
                }

            };

            private View.OnClickListener sendCAPDUOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    _first_strAPDU = edtCAPDU.getText().toString();
                    strAPDU = edtCAPDU.getText().toString();

                    if (strAPDU.length() <= 0) {
                        Toast.makeText(getActivity(), "Command could not be sent", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    infoText.setText("Process exchange APDU command(s)...");
                    dataText.setText("");
                    dlgSendCAPDU.dismiss();
                    swipeButton.setEnabled(false);
                    commandBtn.setEnabled(false);
                    handler.post(runExchangeAPDU);
                }
            };

            private Runnable runExchangeAPDU = new Runnable() {

                public void run() {
                    ResDataStruct resData = new ResDataStruct();
                    int ret;
                    String[] strArray = strAPDU.split("\n");
                    info = "";
                    detail = "";
                    for (int i = 0; i < strArray.length; i++) {
                        byte[] apduPlaintext = Common.getBytesFromHexString(strArray[i]);
                        if (apduPlaintext == null) {
                            String error = "Invalid APDU for command " + (i + 1) + ", please input hex data";
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                            info += "Invalid APDU command " + (i + 1) + " <" + strArray[i] + ">\r\n";
                            break;
                        }
                        ret = device.icc_exchangeAPDU(apduPlaintext, resData);
                        if (ret == ErrorCode.SUCCESS) {
                            info += "C-APDU: <" + strArray[i] + ">\r\n";
                            detail += "APDU Result: <" + Common.base16Encode(resData.resData) + ">\r\n";
                        } else {
                            info += "Exchange APDU Plaintext Failed\n";
                            info += "Status: " + device.device_getResponseCodeString(ret) + "";
                        }
                        handler.post(doUpdateStatus);
                    }
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
                }
            };

            private View.OnClickListener cancelCAPDUOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    dlgSendCAPDU.cancel();
                }

            };

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

            private View.OnClickListener showAIDOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    String aid = "A0000000031010";
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_retrieveApplicationData(aid, resData);
                    dlgAID.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        info = "EMV Retrieve AID  " + aid + " Data Succeeded\n";
                        byte[] tlvData;
                        if (device.device_getDeviceType() == DEVICE_TYPE.DEVICE_MINISMART_II || device.device_getDeviceType() == DEVICE_TYPE.DEVICE_AUGUSTA) {
                            if (resData.resData.length > 0) {
                                tlvData = resData.resData;
                            } else {
                                info = "EMV Retrieve AID  " + aid + " Data Failed\n";
                                info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                                handler.post(doUpdateStatus);
                                return;
                            }
                        } else {
                            if (resData.resData.length > 0) {
                                tlvData = resData.resData;
                            } else {
                                info = "EMV Retrieve AID  " + aid + " Data Failed\n";
                                info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                                handler.post(doUpdateStatus);
                                return;
                            }
                        }

                        if (device.device_getDeviceType() == DEVICE_TYPE.DEVICE_KIOSK_III || device.device_getDeviceType() == DEVICE_TYPE.DEVICE_VENDI) {
                            detail += Common.retrieveAIDTags(tlvData, aid);
                            handler.post(doUpdateStatus);
                            return;
                        }

                        Map<String, Map<String, byte[]>> TLVDict = Common.processTLV(tlvData);
                        Map<String, byte[]> tags = TLVDict.get("tags");
                        Map<String, byte[]> maskedTags = TLVDict.get("masked");
                        Map<String, byte[]> encryptedTags = TLVDict.get("encrypted");

                        if (!tags.isEmpty()) {
                            detail += "Unencrypted Tags:\r\n";
                            Set<String> keys = tags.keySet();
                            for (String key : keys) {
                                detail += key + ": ";
                                byte[] data = tags.get(key);
                                detail += Common.getHexStringFromBytes(data) + "\r\n";
                            }
                        }
                        if (!maskedTags.isEmpty()) {
                            detail += "Masked Tags:\r\n";
                            Set<String> keys = maskedTags.keySet();
                            for (String key : keys) {
                                detail += key + ": ";
                                byte[] data = maskedTags.get(key);
                                detail += Common.getHexStringFromBytes(data) + "\r\n";
                            }
                        }
                        if (!encryptedTags.isEmpty()) {
                            detail += "Encrypted Tags:\r\n";
                            Set<String> keys = encryptedTags.keySet();
                            for (String key : keys) {
                                detail += key + ": ";
                                byte[] data = encryptedTags.get(key);
                                detail += Common.getHexStringFromBytes(data) + "\r\n";
                            }
                        }
                    } else {
                        info = "EMV Retrieve AID  " + aid + " Data Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener removeAIDOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    String aid = "A0000000031010";
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_removeApplicationData(aid, resData);
                    dlgAID.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Remove AID  " + aid + " Succeeded\n";
                        } else {
                            info = "EMV Remove AID  " + aid + " Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Remove AID  " + aid + " Failed\n";
                        info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    }
                    handler.post(doUpdateStatus);
                }

            };

            private View.OnClickListener createAIDOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    String aid = "A0000000031010";
                    byte[] data;
                    data = new byte[]{(byte) 0x9f, 0x01, 0x06, 0x56, 0x49, 0x53, 0x41, 0x30, 0x30, 0x5f, 0x57, 0x01, 0x00, 0x5f, 0x2a,
                            0x02, 0x08, 0x40, (byte) 0x9f, 0x09, 0x02, 0x00, (byte) 0x96, 0x5f, 0x36, 0x01, 0x02, (byte) 0x9f, 0x1b, 0x04,
                            0x00, 0x00, 0x3a, (byte) 0x98, (byte) 0xdf, 0x25, 0x03, (byte) 0x9f, 0x37, 0x04, (byte) 0xdf, 0x28, 0x03, (byte) 0x9f,
                            0x08, 0x02, (byte) 0xdf, (byte) 0xee, 0x15, 0x01, 0x01, (byte) 0xdf, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00,
                            (byte) 0xdf, 0x14, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xdf, 0x15, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00,
                            (byte) 0xdf, 0x18, 0x01, 0x00, (byte) 0xdf, 0x17, 0x04, 0x00, 0x00, 0x27, 0x10, (byte) 0xdf, 0x19, 0x01, 0x00};
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_setApplicationData(aid, data, resData);
                    dlgAID.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV create AID " + aid + " Succeeded\n";
                        } else {
                            info = "EMV create AID " + aid + " Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV create AID " + aid + " Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }

            };

            private View.OnClickListener showAIDListOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_retrieveAidList(resData);
                    dlgAID.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00 && resData.stringArray != null) {
                            info = "EMV Retrieve AID List Succeeded\n";
                            detail = "AID List:\n";
                            for (int i = 0; i < resData.stringArray.length; i++)
                                detail += "<" + resData.stringArray[i] + ">\n";
                        } else {
                            info = "EMV Retrieve AID List Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Retrieve AID List Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }

            };

            private View.OnClickListener AIDCancelOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    dlgAID.dismiss();
                }

            };

            private View.OnClickListener showCAPKOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    int idx = 0;
                    byte[] data;
                    data = new byte[]{(byte) 0xA0, 0x00, 0x00, (byte) 0x99, (byte) 0x99, (byte) 0xE1};
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_retrieveCAPK(data, resData);
                    dlgCAPK.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        byte[] Rid = new byte[5];
                        byte[] temp = new byte[1];

                        if (resData.resData.length >= idx + 30) {
                            detail = "CAPK Data:\n";
                            info = "EMV Retrieve CAPK Succeeded\nRetrieved CAPK A000009999, Index 0xE1";
                            System.arraycopy(resData.resData, idx, Rid, 0, 5);
                            idx += 5;
                            temp[0] = resData.resData[idx++];
                            detail += "RID: <" + Common.base16Encode(Rid) + "> Index: <" + Common.base16Encode(temp) + ">\n";
                            temp[0] = resData.resData[idx++]; //hash algorithm
                            detail += "Hash Algorithm: <" + Common.base16Encode(temp) + ">\n";
                            temp[0] = resData.resData[idx++]; //encryption algorithm
                            detail += "Encryption Algorithm: <" + Common.base16Encode(temp) + ">\n";
                            byte[] hash_value = new byte[20];
                            System.arraycopy(resData.resData, idx, hash_value, 0, 20);
                            detail += "Hash Value: <" + Common.base16Encode(hash_value) + ">\n";
                            idx += 20;
                            byte[] exp = new byte[4];
                            System.arraycopy(resData.resData, idx, exp, 0, 4);
                            detail += "Public Key Exponent: <" + Common.base16Encode(exp) + ">\n";
                            idx += 4;
                            int modLen = (resData.resData[idx] & 0xFF) | ((resData.resData[idx + 1] & 0xFF) << 8);
                            idx += 2;
                            byte[] modulus = new byte[modLen];
                            System.arraycopy(resData.resData, idx, modulus, 0, modLen);
                            detail += "Modulus: <" + Common.base16Encode(modulus) + ">\n";
                        } else {
                            info = "EMV Retrieve CAPK Failed\n";
                            info += "Status: " + device.device_getResponseCodeString(ret) + "\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Retrieve CAPK Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "\n";
                        info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener removeCAPKOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    byte[] data;
                    data = new byte[]{(byte) 0xA0, 0x00, 0x00, (byte) 0x99, (byte) 0x99, (byte) 0xE1};
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_removeCAPK(data, resData);
                    dlgCAPK.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Remove RID A000009999 Index 0xE1 Succeeded\n";
                        } else {
                            info = "EMV Remove RID A00009999 Index 0xE1 Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Remove RID A000009999 Index 0xE1 Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                        info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    }
                    handler.post(doUpdateStatus);
                }

            };

            private View.OnClickListener createCAPKOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    byte[] data = Common.getByteArray("a000009999e10101f8707b9bedf031e58a9f843631b90c90d80ed69500000003700099c5b70aa61b4f4c51b6f90b0e3bfb7a3ee0e7db41bc466888b3ec8e9977c762407ef1d79e0afb2823100a020c3e8020593db50e90dbeac18b78d13f96bb2f57eeddc30f256592417cdf739ca6804a10a29d2806e774bfa751f22cf3b65b38f37f91b4daf8aec9b803f7610e06ac9e6b0000");
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_setCAPK(data, resData);
                    dlgCAPK.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Create RID A000009999 Index E1 Succeeded\n";
                        } else {
                            info = "EMV Create AID A000009999 Index E1 Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Create AID A000009999 Index E1 Failed\n";
                        info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener showCAPKListOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_retrieveCAPKList(resData);
                    dlgCAPK.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Retrieve CAPK List Succeeded\n";
                            detail = "CAPK List:\n";
                            byte[] Rid = new byte[5];
                            byte[] Index = new byte[1];
                            for (int i = 0; i < resData.resData.length; i = i + 6) {
                                System.arraycopy(resData.resData, i, Rid, 0, 5);
                                System.arraycopy(resData.resData, i + 5, Index, 0, 1);
                                detail += "RID <" + Common.base16Encode(Rid) + "> Index <" + Common.base16Encode(Index) + ">\n";
                            }
                        } else {
                            info = "EMV Retrieve CAPK List Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Retrieve CAPK List Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener CAPKCancelOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    dlgCAPK.dismiss();
                }
            };

            private View.OnClickListener removeCRLOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    byte[] data;
                    data = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x50, 0x00, 0x00, 0x10};
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_removeCRL(data, resData);
                    dlgCRL.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Remove CRL A000000003, Index 50, Serial Number 000010 Succeeded\n";
                        } else {
                            info = "EMV Remove CRL A000000003, Index 50, Serial Number 000010 Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Remove CRL A000000003, Index 50, Serial Number 001000 Failed\n";
                        info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener createCRLOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    byte[] data;
                    data = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x50, 0x00, 0x00, 0x10};
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_setCRL(data, resData);
                    dlgCRL.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Create CRL A000000003, Index 50, Serial Number 000010 Succeeded\n";
                        } else {
                            info = "EMV Create CRL A000000003, Index 50, Serial Number 000010 Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Create CRL A000000003, Index 50, Serial Number 000010 Failed\n";
                        info = "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    }
                    handler.post(doUpdateStatus);
                }

            };

            private View.OnClickListener showCRLListOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_retrieveCRL(resData);
                    dlgCRL.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Retrieve CRL List Succeeded\n";

                            if (resData.resData.length > 0) {
                                detail = "CRL List:\n";
                                byte[] Rid = new byte[5];
                                byte[] Index = new byte[1];
                                byte[] Serial = new byte[3];
                                for (int i = 0; i < resData.resData.length; i += 9) {
                                    System.arraycopy(resData.resData, i, Rid, 0, 5);
                                    System.arraycopy(resData.resData, i + 5, Index, 0, 1);
                                    System.arraycopy(resData.resData, i + 6, Serial, 0, 3);
                                    detail += "RID <" + Common.base16Encode(Rid) + "> Index <" + Common.base16Encode(Index) + "> Serial <" + Common.base16Encode(Serial) + ">\n";
                                }
                            } else {
                                info = "EMV CRL List Not Available\n";
                            }
                        } else {
                            info = "EMV Retrieve CRL List Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Retrieve CRL List Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener CRLCancelOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    dlgCRL.dismiss();
                }
            };

            private View.OnClickListener createTMLOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    byte[] data;
                    data = new byte[]{0x5F, 0x36, 0x01, 0x02, (byte) 0x9F, 0x1A, 0x02, 0x08, 0x40, (byte) 0x9F, 0x35, 0x01, 0x21, (byte) 0x9F, 0x33,
                            0x03, 0x60, 0x28, (byte) 0xC8, (byte) 0x9F, 0x40, 0x05, (byte) 0xF0, 0x00, (byte) 0xF0, (byte) 0xA0, 0x01, (byte) 0x9F, 0x1E,
                            0x08, 0x54, 0x65, 0x72, 0x6D, 0x69, 0x6E, 0x61, 0x6C, (byte) 0x9F, 0x15, 0x02, 0x12, 0x34, (byte) 0x9F, 0x16, 0x0F, 0x30,
                            0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, (byte) 0x9F, 0x1C, 0x08, 0x38, 0x37,
                            0x36, 0x35, 0x34, 0x33, 0x32, 0x31, (byte) 0x9F, 0x4E, 0x22, 0x31, 0x30, 0x37, 0x32, 0x31, 0x20, 0x57, 0x61, 0x6C, 0x6B,
                            0x65, 0x72, 0x20, 0x53, 0x74, 0x2E, 0x20, 0x43, 0x79, 0x70, 0x72, 0x65, 0x73, 0x73, 0x2C, 0x20, 0x43, 0x41, 0x20, 0x2C,
                            0x55, 0x53, 0x41, 0x2E, (byte) 0xDF, 0x26, 0x01, 0x01, (byte) 0xDF, 0x10, 0x08, 0x65, 0x6E, 0x66, 0x72, 0x65, 0x73, 0x7A,
                            0x68, (byte) 0xDF, 0x11, 0x01, 0x00, (byte) 0xDF, 0x27, 0x01, 0x00, (byte) 0xDF, (byte) 0xEE, 0x15, 0x01, 0x01, (byte) 0xDF,
                            (byte) 0xEE, 0x16, 0x01, 0x00, (byte) 0xDF, (byte) 0xEE, 0x17, 0x01, 0x07, (byte) 0xDF, (byte) 0xEE, 0x18, 0x01, (byte) 0x80,
                            (byte) 0xDF, (byte) 0xEE, 0x1E, 0x08, (byte) 0xD0, (byte) 0xDC, 0x20, (byte) 0xD0, (byte) 0xC4, 0x1E, 0x14, 0x00, (byte) 0xDF,
                            (byte) 0xEE, 0x1F, 0x01, (byte) 0x80, (byte) 0xDF, (byte) 0xEE, 0x1B, 0x08, 0x30, 0x30, 0x30, 0x31, 0x35, 0x31, 0x30, 0x30,
                            (byte) 0xDF, (byte) 0xEE, 0x20, 0x01, 0x3C, (byte) 0xDF, (byte) 0xEE, 0x21, 0x01, 0x0A, (byte) 0xDF, (byte) 0xEE, 0x22, 0x03,
                            0x32, 0x3C, 0x3C};
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_setTerminalData(data, resData);
                    dlgTerminal.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Create Terminal File Succeeded\n";
                        } else {
                            info = "EMV Create Terminal File Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Create Terminal File Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener removeTMLOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_removeTerminalData(resData);
                    dlgTerminal.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {
                            info = "EMV Remove Terminal Data Succeeded\n";
                        } else {
                            info = "EMV Remove Terminal Data Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Remove Terminal Data Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener showTMLOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    int ret;
                    ResDataStruct resData = new ResDataStruct();
                    ret = device.emv_retrieveTerminalData(resData);
                    dlgTerminal.dismiss();
                    detail = "";
                    if (ret == ErrorCode.SUCCESS) {
                        if (resData.statusCode == 0x00) {

                            if (resData.resData.length > 0) {
                                info = "EMV Retrieve Terminal Data Succeeded\n";
                                Map<String, Map<String, byte[]>> TLVDict = Common.processTLV(resData.resData);
                                Map<String, byte[]> tags = TLVDict.get("tags");
                                Map<String, byte[]> maskedTags = TLVDict.get("masked");
                                Map<String, byte[]> encryptedTags = TLVDict.get("encrypted");

                                if (!tags.isEmpty()) {
                                    detail += "Unencrypted Tags:\r\n";
                                    Set<String> keys = tags.keySet();
                                    for (String key : keys) {
                                        detail += key + ": ";
                                        byte[] data = tags.get(key);
                                        detail += Common.getHexStringFromBytes(data) + "\r\n";
                                    }
                                }
                                if (!maskedTags.isEmpty()) {
                                    detail += "Masked Tags:\r\n";
                                    Set<String> keys = maskedTags.keySet();
                                    for (String key : keys) {
                                        detail += key + ": ";
                                        byte[] data = maskedTags.get(key);
                                        detail += Common.getHexStringFromBytes(data) + "\r\n";
                                    }
                                }
                                if (!encryptedTags.isEmpty()) {
                                    detail += "Encrypted Tags:\r\n";
                                    Set<String> keys = encryptedTags.keySet();
                                    for (String key : keys) {
                                        detail += key + ": ";
                                        byte[] data = encryptedTags.get(key);
                                        detail += Common.getHexStringFromBytes(data) + "\r\n";
                                    }
                                }
                            }
                        } else {
                            info = "EMV Retrieve Terminal Data Failed\n";
                            info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                        }
                    } else {
                        info = "EMV Retrieve Terminal Data Failed\n";
                        info += "Status: " + device.device_getResponseCodeString(ret) + "";
                    }
                    handler.post(doUpdateStatus);
                }
            };

            private View.OnClickListener TMLCancelOnClick = new View.OnClickListener() {
                public void onClick(View v) {
                    dlgTerminal.dismiss();
                }
            };
        }

//        private void printTags(IDTEMVData emvData) {
//            if (emvData.result == IDTEMVData.START_TRANS_SUCCESS)
//                detail = "Start transaction response:\r\n";
//            else if (emvData.result == IDTEMVData.GO_ONLINE)
//                detail += "\r\nAuthentication response:\r\n";
//            else if (emvData.result == IDTEMVData.USE_MAGSTRIPE || emvData.result == IDTEMVData.MSR_SUCCESS) {
//                swipeMSRData(emvData.msr_cardData);
//                detail += "\r\n\r\n";
//                detail += this.emvErrorCodes(emvData.result) + "\r\n";
//                return;
//            } else
//                detail += "\r\nComplete Transaction response:\r\n";
//            if (emvData.unencryptedTags != null && !emvData.unencryptedTags.isEmpty()) {
//                detail += "Unencrypted Tags:\r\n";
//                Set<String> keys = emvData.unencryptedTags.keySet();
//                for (String key : keys) {
//                    detail += key + ": ";
//                    byte[] data = emvData.unencryptedTags.get(key);
//                    detail += Common.getHexStringFromBytes(data) + "\r\n";
//                }
//            }
//            if (emvData.maskedTags != null && !emvData.maskedTags.isEmpty()) {
//                detail += "Masked Tags:\r\n";
//                Set<String> keys = emvData.maskedTags.keySet();
//                for (String key : keys) {
//                    detail += key + ": ";
//                    byte[] data = emvData.maskedTags.get(key);
//                    detail += Common.getHexStringFromBytes(data) + "\r\n";
//                }
//            }
//            if (emvData.encryptedTags != null && !emvData.encryptedTags.isEmpty()) {
//                detail += "Encrypted Tags:\r\n";
//                Set<String> keys = emvData.encryptedTags.keySet();
//                for (String key : keys) {
//                    detail += key + ": ";
//                    byte[] data = emvData.encryptedTags.get(key);
//                    detail += Common.getHexStringFromBytes(data) + "\r\n";
//                }
//            }
//            detail += this.emvErrorCodes(emvData.result) + "\r\n";
//        }

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
            swipeButton.setEnabled(true);
            commandBtn.setEnabled(true);
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

        public void autoConfigCompleted(StructConfigParameters profile) {
            Toast.makeText(getActivity(), "AutoConfig found a working profile.", Toast.LENGTH_LONG).show();
            profileManager.doPost(profile);
            if (device.device_connectWithProfile(profile)) {
                Toast.makeText(getActivity(), "worked", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "didnt work", Toast.LENGTH_SHORT).show();
            }
        }

        public void autoConfigProgress(int progressValue) {
            info = "AutoConfig is running: " + progressValue + "%";
            detail = "";
            handler.post(doUpdateStatus);
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
            info = "Connecting a reader...";
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void msgAudioVolumeAjustFailed() {
            info = "SDK could not adjust volume...";
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void LoadXMLConfigFailureInfo(int index, String strMessage) {
            info = "XML loading error...";
            detail = "";
            handler.post(doUpdateStatus);

        }

        private boolean isFileExist(String path) {
            if (path == null)
                return false;
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            return true;
        }

        private String getXMLFileFromRaw() {
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

        public void onReceiveMsgChallengeResult(int returnCode, byte[] data) {
            // Not called for UniPay Firmware update
        }

        public void msgRKICompleted(String MACResult) {
            info = "Remote Key Injection Succeeded\nMac result: 0x" + MACResult;
            detail = "";
            handler.post(doUpdateStatus);
        }

        public void dataInOutMonitor(byte[] data, boolean isIncoming) {

        }

//        IDTEMVData _card;

//        public void emvTransactionData(IDTEMVData emvData) {
//            _card = emvData;
//            getActivity().runOnUiThread(new Runnable() {
//                public void run() {
//                    printTags(_card);
//                    handler.post(doUpdateStatus);
//                    if (_card.result == IDTEMVData.GO_ONLINE) {
//                        if (Clearent_VP3300.emv_getAutoCompleteTransaction()) {
//                            ResDataStruct resData = new ResDataStruct();
//                            int ret = completeTransaction(resData);
//                            if (ret == ErrorCode.RETURN_CODE_OK_NEXT_COMMAND) {
//                                swipeButton.setEnabled(false);
//                                commandBtn.setEnabled(false);
//                            } else {
//                                info = "EMV Transaction Failed\n";
//                                info += "Status: " + device.device_getResponseCodeString(ret) + "";
//                                swipeButton.setEnabled(true);
//                                commandBtn.setEnabled(true);
//                            }
//                        } else {
//                            dlgCompleteEMV = new Dialog(getActivity());
//                            dlgCompleteEMV.setTitle("Complete EMV transaction");
//                            dlgCompleteEMV.setCancelable(false);
//                            dlgCompleteEMV.setContentView(R.layout.complete_emv_one_option_dialog);
//                            Button btnCompleteEMV = (Button) dlgCompleteEMV.findViewById(R.id.btnCompleteEMV);
//                            btnCompleteEMV.setOnClickListener(onlineApprovedOnClick);
//                            Button btnCompCancel = (Button) dlgCompleteEMV.findViewById(R.id.btnCompEMVOneCancel);
//                            btnCompCancel.setOnClickListener(authCompCancelOnClick);
//                            dialogId = 1;
//                            dlgCompleteEMV.show();
//                        }
//
//                    } else if (_card.result == IDTEMVData.START_TRANS_SUCCESS) {
//                        if (!Clearent_VP3300.emv_getAutoAuthenticateTransaction()) {
//                            //show authentication dialog
//                            dlgOnlineAuth = new Dialog(getActivity());
//                            dlgOnlineAuth.setTitle("Request to authenticate");
//                            dlgOnlineAuth.setCancelable(false);
//                            dlgOnlineAuth.setContentView(R.layout.authenticate_dialog);
//                            Button btnAuthencate = (Button) dlgOnlineAuth.findViewById(R.id.btnAuthenticate);
//                            btnAuthencate.setOnClickListener(authenticateOnClick);
//                            Button btnAuthCancel = (Button) dlgOnlineAuth.findViewById(R.id.btnAuthCancel);
//                            btnAuthCancel.setOnClickListener(authCompCancelOnClick);
//                            dialogId = 0;
//                            dlgOnlineAuth.show();
//                        }
//                    } else {
//                        if (_card.result == IDTEMVData.TIME_OUT)
//                            info = "EMV transaction failed: TIME OUT.";
//                        ResDataStruct resData = new ResDataStruct();
//                        swipeButton.setEnabled(true);
//                        commandBtn.setEnabled(true);
//                        handler.post(doUpdateStatus);
//                        device.emv_cancelTransaction(resData);
//                        totalEMVTime = System.currentTimeMillis() - totalEMVTime;
////							detail += "\r\nTotal time for EMV transaction: "+totalEMVTime+" ms\r\n";
//                    }
//                }
//
//            });
//        }

//        IDTMSRData msr_card;

        //Callback function for MSR swipe
//        public void swipeMSRData(IDTMSRData card) {
//            msr_card = card;
//
//            getActivity().runOnUiThread(new Runnable() {
//                public void run() {
//                    startSwipe = false;
//                    if (alertSwipe != null)
//                        alertSwipe.dismiss();
//
//                    if (msr_card.result != ErrorCode.SUCCESS) {
//                        info = "MSR card data didn't read correctly\n";
//                        info += ErrorCodeInfo.getErrorCodeDescription(msr_card.result);
//                        if (msr_card.result != ErrorCode.FAILED_NACK) {
//                            detail = "";
//                            swipeButton.setEnabled(true);
//                            commandBtn.setEnabled(true);
//                            handler.post(doUpdateStatus);
//                            return;
//                        }
//                    } else {
//                        info = "MSR Card tapped/Swiped Successfully";
//                    }
//                    detail = Common.parse_MSRData(device.device_getDeviceType(), msr_card);
//                    swipeButton.setEnabled(true);
//                    commandBtn.setEnabled(true);
//                    handler.post(doUpdateStatus);
//                }
//
//            });
//        }

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
                    swipeButton.setEnabled(true);
                    commandBtn.setEnabled(true);
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
            // TODO Auto-generated method stub

        }

        public void getProfileResult(String output) {
            if (output.equals("404") || output.contains("failed to connect")) {
                Toast.makeText(getActivity(), "Profile not found. trying xml", Toast.LENGTH_SHORT).show();
                String filepath = getXMLFileFromRaw();
                if (!isFileExist(filepath)) {
                    filepath = null;
                }
                device.config_setXMLFileNameWithPath(filepath);
                device.config_loadingConfigurationXMLFile(false);

            } else {
                device.device_connectWithProfile(ProfileUtility.JSONtoProfile(output));
                Toast.makeText(getActivity(), "Profile Found", Toast.LENGTH_LONG).show();
            }

        }

        public void postProfileResult(String s) {
            Toast.makeText(getActivity(), "Post: " + s, Toast.LENGTH_SHORT).show();

        }

        int _mode = 0;
        byte[] _key = null;
        byte[] _PAN = null;
        int _startTO = 0;
        int _intervalTO = 0;
        String _language = "";

        @Override
        public void pinRequest(final int mode, byte[] key, byte[] PAN, int startTO,
                               int intervalTO, String language) {


            _mode = mode;
            _key = key;
            _PAN = PAN;
            _startTO = startTO;
            _intervalTO = intervalTO;
            _language = language;


            getActivity().runOnUiThread(new Runnable() {
                public void run() {

                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(getActivity());
                    View promptsView = li.inflate(R.layout.prompts, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getActivity());

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.editTextDialogUserInput);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // get user input and set it to result
                                            // edit text
                                            String thepin = userInput.getText().toString();
                                            byte[] pin2 = null;
                                            try {
                                                pin2 = thepin.getBytes("UTF-8");
                                            } catch (UnsupportedEncodingException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                            device.emv_callbackResponsePIN(mode, null, pin2);
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            device.emv_callbackResponsePIN(0, null, null);
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();


                }
            });


        }

    }

}
