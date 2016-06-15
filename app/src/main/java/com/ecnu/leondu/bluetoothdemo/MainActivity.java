package com.ecnu.leondu.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    // ListAdapter
    private LeDeviceListAdapter mLeDeviceListAdapter;
    public static BluetoothAdapter bluetoothAdapter;
    public static BluetoothLeClass mBLE;
    private Boolean isScanning;

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothSocket bluetoothSocket;

    public RecyclerView recyclerView;
    public ArrayAdapter<String> arrayAdapter;
    DeviceRecyclerViewAdapter deviceRecyclerViewAdapter;
    BluetoothLeScanner bluetoothLeScanner;


    private boolean mScanning = true;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public ArrayList<BluetoothDevice> pairedDevices;

    public static final String BLUETOOTH_DEVICE_NAME = "bluetooth.recipe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pairedDevices = new ArrayList<BluetoothDevice>();
        recyclerView = (RecyclerView) findViewById(R.id.Recycler_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        mHandler = new Handler();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "BluetoothAdapter 未获取", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mBLE = new BluetoothLeClass(this);
        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        //发现BLE终端的Service时回调

        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        //收到BLE终端数据交互的事件
        mBLE.setOnDataAvailableListener(mOnDataAvailable);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLeDevice(true);
//                scanBLE();
            }
        });
    }


    private void scanBLE() {
        int apiVersion = android.os.Build.VERSION.SDK_INT;
        if (apiVersion > android.os.Build.VERSION_CODES.KITKAT) {
            Toast.makeText(getBaseContext(), "版本较新", Toast.LENGTH_SHORT).show();
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            // scan for devices
            scanner.startScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    // get the discovered device as you wish
                    // this will trigger each time a new device is found


                    BluetoothDevice device = result.getDevice();
                    Toast.makeText(getBaseContext(), "发现设备", Toast.LENGTH_SHORT).show();
                    pairedDevices.add(device);
                    deviceRecyclerViewAdapter.notifyDataSetChanged();
                }
            });
        } else {
            // targetting kitkat or bellow
            bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    // get the discovered device as you wish
                    Toast.makeText(getBaseContext(), "发现设备", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                    Toast.makeText(getBaseContext(), "扫描停止", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothLeScanner.startScan(scanCallback);
            Toast.makeText(getBaseContext(), "开始扫描", Toast.LENGTH_SHORT).show();
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
            Toast.makeText(getBaseContext(), "扫描停止", Toast.LENGTH_SHORT).show();
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            pairedDevices.add(device);
            Toast.makeText(getBaseContext(), "发现设备", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Device name: " + device.getName());
            Log.i(TAG, "Device address: " + device.getAddress());
            Log.i(TAG, "Device service UUIDs: " + device.getUuids());

            ScanRecord record = result.getScanRecord();
            Log.i(TAG, "Record advertise flags: 0x" + Integer.toHexString(record.getAdvertiseFlags()));
            Log.i(TAG, "Record Tx power level: " + record.getTxPowerLevel());
            Log.i(TAG, "Record device name: " + record.getDeviceName());
            Log.i(TAG, "Record service UUIDs: " + record.getServiceUuids());
            Log.i(TAG, "Record service data: " + record.getServiceData());
            deviceRecyclerViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(getBaseContext(), "扫描失败", Toast.LENGTH_SHORT).show();
        }
    };


    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLE.getSupportedGattServices());
        }

    };

    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener() {

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + Utils.bytesToHexString(characteristic.getValue()));
        }

        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + new String(characteristic.getValue()));
        }
    };


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "发现设备", Toast.LENGTH_SHORT).show();
                            pairedDevices.add(device);
                            deviceRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            //-----Service的字段信息-----//
            int type = gattService.getType();
            Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
            Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());

            //-----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG, "---->char value:" + new String(data));
                }

                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBLE.readCharacteristic(gattCharacteristic);
                        }
                    }, 500);

                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                    //设置数据内容
                    gattCharacteristic.setValue("send data->");
                    //往蓝牙模块写入数据
                    mBLE.writeCharacteristic(gattCharacteristic);
                }

                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:" + new String(desData));
                    }
                }
            }
        }//

    }


    private class DeviceRecyclerViewAdapter extends RecyclerView.Adapter {
        class deviceHolder extends RecyclerView.ViewHolder {

            private TextView deviceNameTextView;
            private TextView deviceAddressTextView;
            private View itemView;

            public View getItemView() {
                return itemView;
            }

            public deviceHolder(View itemView) {
                super(itemView);
                deviceNameTextView = (TextView) itemView.findViewById(R.id.txt_deviceName);
                deviceAddressTextView = (TextView) itemView.findViewById(R.id.txt_deviceAddress);
                this.itemView = itemView;
            }

            public TextView getDeviceNameTextView() {

                return deviceNameTextView;
            }

            public TextView getDeviceAddressTextView() {
                return deviceAddressTextView;
            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            return new deviceHolder(LayoutInflater.from(getBaseContext()).inflate(R.layout.item_device, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            final deviceHolder viewHolder = (deviceHolder) holder;
            viewHolder.getDeviceNameTextView().setText(pairedDevices.get(position).getName());
            viewHolder.getDeviceAddressTextView().setText(pairedDevices.get(position).getAddress());

            viewHolder.getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pairedDevices.get(position).getAddress() != null) {
                        Intent intent = new Intent(MainActivity.this, BleGattActivity.class);
                        intent.putExtra("Device_Address", pairedDevices.get(position).getAddress());
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getBaseContext(), "地址为空", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return pairedDevices.size();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter();
        recyclerView.setAdapter(deviceRecyclerViewAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mBLE.disconnect();
//        mBLE.close();
    }
}
