package com.example.rfid_c72_plugin_example.Activities;

import android.util.Log;
import android.os.Handler;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;
import android.app.Activity;

import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.interfaces.ScanBTCallback;
import com.rscja.deviceapi.interfaces.ConnectionStatus;

import io.flutter.plugin.common.MethodChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback;

/*
Creator: NMC97
Date: 12/2024
Description: R5 Devices List
*/
public class DeviceListActivity {
    private RFIDWithUHFBLE uhfble;
    BTStatus btStatus = new BTStatus();
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private List<MyDevice> deviceList = new ArrayList<>();
    private Map<String, Integer> devRssiValues = new HashMap<>();
    private Activity _activity;
    private MethodChannel methodChannel; // MethodChannel to communicate with Flutter


    public DeviceListActivity(Activity activity, RFIDWithUHFBLE uhfble, MethodChannel channel) {
        this.uhfble = uhfble;
        this._activity = activity;
        this.methodChannel = channel; // Assign MethodChannel
    }
    private void showToast(String message) {
        _activity.runOnUiThread(() -> Toast.makeText(_activity, message, Toast.LENGTH_SHORT).show());
    }
    public void scanBLEDevice(final boolean enable) {
        if (enable) {
            Log.i("MINHCHAULOG", "Start scan...");

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    uhfble.stopScanBTDevices();
                    Log.i("MINHCHAULOG", "Stop scan");
                    sendDeviceListToFlutter(); //Returns the device list to Flutter

                    //print device list
                    for (MyDevice d : deviceList) {
                        Log.i("MINHCHAULOG", "Device: " + "Name: " + d.getName() + "|" + " Address: " + d.getAddress());
                    }
                }
            }, SCAN_PERIOD); // stop scan after 10s

            mScanning = true;
            if (uhfble == null) {
                return;
            }
            uhfble.startScanBTDevices(new ScanBTCallback() {

                @Override
                public void getDevices(final BluetoothDevice bluetoothDevice, final int rssi, byte[] bytes) {
                    MyDevice myDevice = new MyDevice(bluetoothDevice.getAddress(), bluetoothDevice.getName());
                    addDevice(myDevice, rssi);
                }
            });

        } else {
            mScanning = false;
            uhfble.stopScanBTDevices();
        }
    }
    private void addDevice(MyDevice device, int rssi) {

        try {
            boolean deviceFound = false;
            if (device.getName() == null || device.getName().equals("")) return;
            for (MyDevice listDev : deviceList) {
                if (listDev.getAddress().equals(device.getAddress())) {
                    deviceFound = true;
                    break;
                }
            }
            devRssiValues.put(device.getAddress(), rssi);
            if (!deviceFound) {
                deviceList.add(device);
            }

            // Reorder based on signal strength
            Collections.sort(deviceList, new Comparator<MyDevice>() {
                @Override
                public int compare(MyDevice device1, MyDevice device2) {
                    String key1 = device1.getAddress();
                    String key2 = device2.getAddress();
                    int v1 = devRssiValues.get(key1);
                    int v2 = devRssiValues.get(key2);
                    if (v1 > v2) {
                        return -1;
                    } else if (v1 < v2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        } catch (Exception e) {
            Log.d("MINCHAULOG", "Scanning Fail" + e.getMessage());
        }
    }
    private void sendDeviceListToFlutter() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (MyDevice device : deviceList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", device.getName() == null ? "Unknown" : device.getName());
                jsonObject.put("address", device.getAddress());
                jsonArray.put(jsonObject);
            }

            // Send device list to Flutter via MethodChannel
            methodChannel.invokeMethod("onDeviceListReceived", jsonArray.toString());
        } catch (Exception e) {
            Log.e("MINHCHAULOG", "Error sending device list to Flutter: " + e.getMessage());
        }
    }

    public void connect(String deviceAddress) {
        if (uhfble.getConnectStatus() == ConnectionStatus.CONNECTING) {
            showToast("Connecting...");
        } else {
            uhfble.connect(deviceAddress, btStatus);
            Log.e("MINHCHAULOG","Trang Thai Ket Noi"+ uhfble.getConnectStatus());
        }
    }
    class BTStatus implements ConnectionStatusCallback<Object> {
        @Override
        public void getStatus(final ConnectionStatus connectionStatus, final Object device1) {

            BluetoothDevice device = (BluetoothDevice) device1;
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                //  remoteBTName = device.getName();
                //   remoteBTAdd = device.getAddress();
                showToast("Connected to " + device.getName());
                android.util.Log.d("MINHCHAULOG", "Connected device: " + device.getName());

            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                android.util.Log.d("MINHCHAULOG", "DisConnected device: " + device.getName());
            }
        }
    }
}