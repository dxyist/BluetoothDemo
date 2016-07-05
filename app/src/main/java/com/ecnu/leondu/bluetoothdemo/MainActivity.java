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
import android.content.pm.PackageManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String UUID_KEY_DATA = "f0001132-0451-4000-b000-000000000000";
    // ListAdapter
    private ListView listView;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    public static BluetoothAdapter bluetoothAdapter;
    public static BluetoothLeClass mBLE;
    private Boolean isScanning;
    public static List<BluetoothGattService> gattServices;
    BluetoothDevice device;

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothSocket bluetoothSocket;



    public RecyclerView recyclerView;
    public ArrayAdapter<String> arrayAdapter;
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
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device != null) {

//                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    device = mLeDeviceListAdapter.getDevice(position);

//                    mBLE.connect(device.getAddress());
                    Intent intent = new Intent(MainActivity.this, BleGattActivity.class);
                    intent.putExtra("Device_Address", device.getAddress());
                    startActivity(intent);


                } else {
                    Toast.makeText(getBaseContext(), "地址为空", Toast.LENGTH_SHORT).show();
                }
                ;
            }
        });
      listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

              BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
              if (device != null) {

//                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                  device = mLeDeviceListAdapter.getDevice(position);

//                    mBLE.connect(device.getAddress());
                  Intent intent = new Intent(MainActivity.this, BleServiceActivity.class);
                  intent.putExtra("Device_Address", device.getAddress());
                  startActivity(intent);


              } else {
                  Toast.makeText(getBaseContext(), "地址为空", Toast.LENGTH_SHORT).show();
              }
              ;
              return false;
          }
      });


        // 确认设备是否支持蓝牙BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // 获取当前设备的BluetoothAdapter
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // 初始化一个Handler
        mHandler = new Handler();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "BluetoothAdapter 未获取", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // 不太确定这句话有什么用
        bluetoothAdapter.enable();
        mBLE = new BluetoothLeClass(this);


        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        //发现BLE终端的Service时回调

//        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
//        //收到BLE终端数据交互的事件
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
//                    Toast.makeText(getBaseContext(), "发现设备", Toast.LENGTH_SHORT).show();
                    pairedDevices.add(device);
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();

                }
            });
        } else {
            // targetting kitkat or bellow
            bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    // get the discovered device as you wish
//                    Toast.makeText(getBaseContext(), "发现设备", Toast.LENGTH_SHORT).show();
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
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
//                    Toast.makeText(getBaseContext(), "扫描停止", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
//            Toast.makeText(getBaseContext(), "开始扫描ing", Toast.LENGTH_SHORT).show();
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            Toast.makeText(getBaseContext(), "扫描停止", Toast.LENGTH_SHORT).show();
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            pairedDevices.add(device);
            mLeDeviceListAdapter.addDevice(device);
            mLeDeviceListAdapter.notifyDataSetChanged();

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

//            displayGattServices(mBLE.getSupportedGattServices());
//            MainActivity.gattServices = gatt.getServices();
//            MainActivity.gattServices = gatt.getServices();

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
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
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
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        listView.setAdapter(mLeDeviceListAdapter);
//        scanLeDevice(true);
        scanLeDevice(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
//        scanLeDevice(false);

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLeDeviceListAdapter.clear();
        mBLE.disconnect();
        mBLE.close();
    }
}
