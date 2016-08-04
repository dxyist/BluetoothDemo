package com.ecnu.leondu.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    private EditText showTextView;
    private Button testButton;

    private final static String TAG = SheetMusicActivity.class.getSimpleName();
    private final static String UUID_KEY_DATA = "f0001132-0451-4000-b000-000000000000";
    private final static String UUID_KEY_DATA_SEND = "f0001131-0451-4000-b000-000000000000";

    public BluetoothLeClass mBLE;
    public BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    private String deviceAddress;

    String bufString = "";

    BluetoothGattCharacteristic testGattCharacteristic;


    List<BluetoothGattService> gattServices;

    BluetoothGattService dataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        showTextView = (EditText) findViewById(R.id.edt_Test);
        testButton = (Button) findViewById(R.id.btn_Test);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                    设置数据内容
                testGattCharacteristic.setValue("#" + (new GregorianCalendar()).get(GregorianCalendar.MILLISECOND)+" ");
                //往蓝牙模块写入数据
                mBLE.writeCharacteristic(testGattCharacteristic);
            }
        });

        //---------蓝牙部分--------
        mBLE = MainActivity.mBLE;
        this.gattServices = new ArrayList<BluetoothGattService>();
        //发现BLE终端的Service时回调
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        //收到BLE终端数据交互的事件
        mBLE.setOnDataAvailableListener(mOnDataAvailable);

        deviceAddress = getIntent().getStringExtra("Device_Address");
        mBLE.connect(deviceAddress);

        bluetoothAdapter = MainActivity.bluetoothAdapter;
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        mBLE.setOnConnectListener(new BluetoothLeClass.OnConnectListener() {

            @Override
            public void onConnect(BluetoothGatt gatt) {

                Log.e(TAG, "OnConnectListener!!!");
                Message message = mainHandler.obtainMessage();


                mainHandler.sendMessage(message);
            }
        });

        mBLE.setOnDisconnectListener(new BluetoothLeClass.OnDisconnectListener() {

            @Override
            public void onDisconnect(BluetoothGatt gatt) {
                Log.e(TAG, "onDisconnect!!!");
                Message message = mainHandler.obtainMessage();
                mainHandler.sendMessage(message);

            }
        });
        mBLE.setOnDataAvailableListener(mOnDataAvailable);

    }


    Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            int age = b.getInt("age");
            String name = b.getString("name");
            System.out.println("age is " + age + ", name is" + name);
            System.out.println("Handler--->" + Thread.currentThread().getId());
            System.out.println("handlerMessage");

        }
    };


    private Handler keyEventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle b = msg.getData();
            String keyString = b.getString("key");

            int nowMill = (new GregorianCalendar()).get(GregorianCalendar.MILLISECOND);
            int passedMill = Integer.valueOf(keyString.substring(1).trim());
            int value = nowMill - passedMill;
            if (value <0)
            {
                value +=1000;
            }



                showTextView.setText("数据延迟为" +value+ "毫秒");



            Toast.makeText(getBaseContext(),"接收到数据",Toast.LENGTH_SHORT).show();

        }
    };




    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {

            gattServices = mBLE.getSupportedGattServices();

            displayGattServices(gattServices);
            Message message = mainHandler.obtainMessage();
            mainHandler.sendMessage(message);


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
            Log.e(TAG, "BLE终端数据被读的事件!!!");
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
            Log.e(TAG, "收到BLE终端写入数据回调!!!");
            Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + new String(characteristic.getValue()));

            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("key", new String(characteristic.getValue()));
            message.setData(bundle);//bundle传值，耗时，效率低
            keyEventHandler.sendMessage(message);//发送message信息
            message.what = 1;//标志是哪个线程传数据

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
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mBLE.readCharacteristic(gattCharacteristic);
//                        }
//                    }, 500);
                    Log.e(TAG, "发现所需服务!!!");

//                    接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);


//                    设置数据内容
//                    gattCharacteristic.setValue("send data->");
//                    //往蓝牙模块写入数据
//                    mBLE.writeCharacteristic(gattCharacteristic);
                }

                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA_SEND)) {
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mBLE.readCharacteristic(gattCharacteristic);
//                        }
//                    }, 500);
                    Log.e(TAG, "写入数据!!!");
                    Log.e(TAG, "发现可连接服务!!!");

                    testGattCharacteristic = gattCharacteristic;


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
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBLE.disconnect();
        mBLE.close();
    }

}
