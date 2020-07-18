package com.control.sabzak;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private String deviceName = "HMSoft";
    private String deviceAddress = "4C:24:98:5C:D7:F2";
    TextView statusTV;

    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;

    public final static UUID customCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID customService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    //private LeDeviceListAdapter leDeviceListAdapter;

    private Map<String, BluetoothDevice> mScanResults;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private boolean mConnected;
    private ScanCallback mScanCallback;
    private BluetoothGatt mGatt;
    private boolean mInitialized;

    private BluetoothDevice sabzak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get status text
        statusTV = findViewById(R.id.statusView);
        setStatus("Status Unknown!");

        //start setting up bluetooth
        //bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();



        //tryConnect(bluetoothAdapter);

        //open button
        Button openBtn = findViewById(R.id.openBtn);
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Open Clicked!", Toast.LENGTH_SHORT)
                //        .show();
                String messageStr = "B";
                sendMessage(messageStr);

            }
        });


        //close button
        Button closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Close Clicked!", Toast.LENGTH_SHORT)
                //        .show();
                String messageStr = "C";
                sendMessage(messageStr);
            }
        });


        //connect button
        Button connBtn = findViewById(R.id.connectBtn);
        connBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();

            }
        });

    }


    //*****************New Code****************

    @Override
    protected void onResume() {
        super.onResume();

        //check low energy support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            setStatus("No LE cupport!");
            finish();
        }
    }


    // Start scanning for devices
    private void startScan() {
        if (!hasPermissions() || mScanning) {
            return;
        }

        disconnectGattServer();

        mScanResults = new HashMap<>();
        mScanCallback = new BtleScanCallback(mScanResults);

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(customService))
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(scanFilter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        bluetoothLeScanner.startScan(filters, settings, mScanCallback);


        mHandler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                stopScan();
            }
        };
        mScanning = true;
        setStatus("Started Scanning...");
        mHandler.postDelayed(r, SCAN_PERIOD);

    }

    private void sendMessage(String message) {
        if (!mConnected) {
            Log.e(TAG, "mConnect os false");
            return;
        }

        BluetoothGattService service = mGatt.getService(customService);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(customCharacteristic);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        //mInitialized = mGatt.setCharacteristicNotification(characteristic, true);

        byte[] messageBytes = new byte[0];
        try {
            messageBytes = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }
        characteristic.setValue(messageBytes);
        boolean success = mGatt.writeCharacteristic(characteristic);

    }



    private void stopScan() {

        if (mScanning && bluetoothAdapter != null && bluetoothAdapter.isEnabled() && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(mScanCallback);
            setStatus("Stopped scanning.");
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;

    }

    private void scanComplete() {
        if (mScanResults.isEmpty()) {
            setStatus("Could not find Sabzak!");
            Log.d(TAG, "Coud not find Sabzak at "+ deviceAddress);
            return;
        }
        for (String deviceAddress : mScanResults.keySet()) {
            setStatus("Found Sabzak");
            Log.d(TAG, "Found Sabzak -> "+ deviceAddress);
            sabzak = mScanResults.get(deviceAddress);
            connectDevice(sabzak);
            mConnected = true;
        }
    }

    private boolean hasPermissions() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(TAG, "Requested user enables Bluetooth. Try starting the scan again.");
    }
    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }


    //GATT connection

    private void connectDevice(BluetoothDevice device) {
        setStatus("Connecting ...");
        Log.d(TAG," Connecting to "+ device.getAddress());
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = device.connectGatt(this, false, gattClientCallback);
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void disconnectGattServer() {
        //setStatus("Closing Gatt connection");
        Log.d(TAG,"Closing Gatt connection.");
        mConnected = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

    //callbacks

    private class BtleScanCallback extends ScanCallback {

        private Map<String, BluetoothDevice> mScanResults;

        BtleScanCallback(Map<String, BluetoothDevice> scanResults) {
            mScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            setStatus("BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            stopScan();
            BluetoothDevice device = result.getDevice();
            connectDevice(device);
            //String deviceAddress = device.getAddress();
            //mScanResults.put(deviceAddress, device);
        }
    }

    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange newState: " + newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.w(TAG, "Connection Gatt failure status " + status);
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                Log.w(TAG, "Connection not GATT success status " + status);
                disconnectGattServer();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                setStatus("** Connected to Sabzak **");
                Log.d(TAG, "Connected to device " + gatt.getDevice().getAddress());
                setConnected(true);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                setStatus("Disconnected from Sabzak!");
                Log.w(TAG, "Disconnected from device");
                disconnectGattServer();
            }
        }
    }








    // Update text view string on the application window
    private void setStatus(String sstr) {
        statusTV.setText(sstr);
    }





    //****************Old Code*****************
    /*private void scanLeDevice(final boolean enable) {

        if (enable){
       // bleScanner.startScan(
       //         filters,
       //         settings,
       //         scanCallback);
        } else {
        //    bleScanner.stopScan(scanCallBack);
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        //private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            //mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public BluetoothDevice getDevice(String deviceName){
            for (int i=0 ;i < mLeDevices.size(); i++){
                if (mLeDevices.get(i).getName().equals(deviceName))
                    return mLeDevices.get(i);
            }
            return null;
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leDeviceListAdapter.addDevice(device);
                            leDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

   */
}