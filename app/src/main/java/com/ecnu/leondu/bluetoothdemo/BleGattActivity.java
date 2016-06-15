package com.ecnu.leondu.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Make Some Change

public class BleGattActivity extends AppCompatActivity {

    private final static String TAG = BleGattActivity.class.getSimpleName();
    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public BluetoothLeClass mBLE;
    public BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    private Boolean isConnected;
    private MenuItem connectItem;
    private String deviceAddress;
    ServiceRecyclerViewAdapter serviceRecyclerViewAdapter;

    List<BluetoothGattService> gattServices;

    private TextView projectNameTextView;
    private TextView rssiTextView;
    private TextView addressTextView;
    private TextView statusTextView;

    private RecyclerView recyclerView;

    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_gatt);
        projectNameTextView = (TextView) findViewById(R.id.txt_Peripheral);
        rssiTextView = (TextView) findViewById(R.id.txt_RSSI);
        addressTextView = (TextView) findViewById(R.id.txt_Address);
        statusTextView = (TextView) findViewById(R.id.txt_Status);
        // RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.Recycler_Services);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));

        mBLE = MainActivity.mBLE;
        deviceAddress = getIntent().getStringExtra("Device_Address");
        mBLE.connect(deviceAddress);

        bluetoothAdapter = MainActivity.bluetoothAdapter;
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        projectNameTextView.setText(bluetoothDevice.getName());
        addressTextView.setText(bluetoothDevice.getAddress());

        String stateString = "";
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            stateString = "Connected";
        } else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
            stateString = "Connecting";
        } else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            stateString = "Disconnected";
        }
        statusTextView.setText(stateString);
        gattServices = new ArrayList<BluetoothGattService>();

        mHandler = new android.os.Handler();

        //发现BLE终端的Service时回调
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        //收到BLE终端数据交互的事件
        mBLE.setOnDataAvailableListener(mOnDataAvailable);

        mBLE.setOnConnectListener(new BluetoothLeClass.OnConnectListener() {

            @Override
            public void onConnect(BluetoothGatt gatt) {
                Toast.makeText(getApplicationContext(), "蓝牙已连接", Toast.LENGTH_SHORT).show();
                isConnected = true;
                connectItem.setTitle("connected");
            }
        });

        mBLE.setOnDisconnectListener(new BluetoothLeClass.OnDisconnectListener() {

            @Override
            public void onDisconnect(BluetoothGatt gatt) {
                Toast.makeText(getBaseContext(), "蓝牙连接已断开", Toast.LENGTH_SHORT).show();
                isConnected = false;
                connectItem.setTitle("disconnected");
            }
        });
    }


    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
//            displayGattServices(mBLE.getSupportedGattServices());
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

//
//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) return;
//
//        for (BluetoothGattService gattService : gattServices) {
//            //-----Service的字段信息-----//
//            int type = gattService.getType();
//            Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
//            Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
//            Log.e(TAG, "-->service uuid:" + gattService.getUuid());
//
//            //-----Characteristics的字段信息-----//
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
//
//                int permission = gattCharacteristic.getPermissions();
//                Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));
//
//                int property = gattCharacteristic.getProperties();
//                Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));
//
//                byte[] data = gattCharacteristic.getValue();
//                if (data != null && data.length > 0) {
//                    Log.e(TAG, "---->char value:" + new String(data));
//                }
//
//                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
//                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
//                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mBLE.readCharacteristic(gattCharacteristic);
//                        }
//                    }, 500);
//
//                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
//                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
//                    //设置数据内容
//                    gattCharacteristic.setValue("send data->");
//                    //往蓝牙模块写入数据
//                    mBLE.writeCharacteristic(gattCharacteristic);
//                }
//
//                //-----Descriptors的字段信息-----//
//                gattDescriptors = gattCharacteristic.getDescriptors();
//                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
//                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
//                    int descPermission = gattDescriptor.getPermissions();
//                    Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));
//
//                    byte[] desData = gattDescriptor.getValue();
//                    if (desData != null && desData.length > 0) {
//                        Log.e(TAG, "-------->desc value:" + new String(desData));
//                    }
//                }
//            }
//        }
//    }


    private class ServiceRecyclerViewAdapter extends RecyclerView.Adapter {
        class deviceHolder extends RecyclerView.ViewHolder {

            private TextView serviceNameTextView;
            private TextView serviceUUIDTextView;
            private TextView servicePermissionTextView;
            private View itemView;


            public deviceHolder(View itemView) {
                super(itemView);
                serviceNameTextView = (TextView) itemView.findViewById(R.id.txt_serviceName);
                serviceUUIDTextView = (TextView) itemView.findViewById(R.id.txt_serviceName);
                servicePermissionTextView = (TextView) itemView.findViewById(R.id.txt_servicePermission);
                this.itemView = itemView;
            }

            public TextView getServiceNameTextView() {
                return serviceNameTextView;
            }

            public TextView getServiceUUIDTextView() {
                return serviceUUIDTextView;
            }

            public TextView getServicePermissionTextView() {
                return servicePermissionTextView;
            }

            public View getItemView() {
                return itemView;
            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            return new deviceHolder(LayoutInflater.from(getBaseContext()).inflate(R.layout.item_service, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            final deviceHolder viewHolder = (deviceHolder) holder;
            viewHolder.getServiceNameTextView().setText(gattServices.get(position).getClass().getName());
            viewHolder.getServiceUUIDTextView().setText(gattServices.get(position).getUuid()+"");
            viewHolder.getServicePermissionTextView().setText((gattServices.get(position).getType() == 1 )?"primary":"secondary");


        }

        @Override
        public int getItemCount() {
            return gattServices.size();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gatt, menu);
        connectItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            mBLE.connect(deviceAddress);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gattServices = mBLE.getSupportedGattServices();

        serviceRecyclerViewAdapter = new ServiceRecyclerViewAdapter();
        recyclerView.setAdapter(serviceRecyclerViewAdapter);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBLE.disconnect();
    }

    ;
}
