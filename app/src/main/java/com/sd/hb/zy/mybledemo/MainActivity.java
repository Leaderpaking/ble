package com.sd.hb.zy.mybledemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /**
     * 蓝牙的基本操作，比如打开等
     */
    private BluetoothAdapter bluetoothAdapter;
    private int REQUEST_ENABLE_BT = 0;
    private long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<>();
    private BleDeviceListAdapter adapter;

    /**
     * 蓝牙设备
     */
    private BluetoothDevice mBluetoothDevice;
    /**
     * 数据的读写操作
     */
    private BluetoothGattCharacteristic characteristic;
    /**
     * 连接蓝牙设备 重连等操作
     */
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGatt> bluetoothGattList;
    public static final int PERMISSIONS_REQUEST_CODE = 101;
    /**
     * 扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            if (bluetoothDevice.getName() != null) {
                if (!bluetoothDeviceArrayList.contains(bluetoothDevice)) {
                    bluetoothDeviceArrayList.add(bluetoothDevice);
                }
                Log.e(TAG, "scan--" + bluetoothDevice.getName());
            }
        }
    };

    private Handler adapterFreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();
        }
    };

    public BluetoothDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerBleListenerReceiver();
        //位置权限
        int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (check == PackageManager.PERMISSION_GRANTED) {
            initBlueTooth();
        } else {
            //获取权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE);
        }

        addListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initBlueTooth();
            } else {
                Toast.makeText(this, "没有获取位置权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addListener() {
        findViewById(R.id.btn_scan_ble).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.notifyDataSetChanged();
                AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                        .setAdapter(adapter, null)
                        .create();
                builder.show();

            }
        });

    }

    /**
     * 初始化蓝牙
     */
    private void initBlueTooth() {
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (manager != null) {
            bluetoothAdapter = manager.getAdapter();
            if (bluetoothAdapter != null) {
                //蓝牙没有打开
                if (!bluetoothAdapter.isEnabled()) {
                    openBle();
                } else {
                    Toast.makeText(MainActivity.this, "蓝牙已打开", Toast.LENGTH_SHORT).show();
                    scanLeDevice(true);
                }
            } else {
                openBle();
            }
        }
        adapter = new BleDeviceListAdapter(bluetoothDeviceArrayList, MainActivity.this);
        bluetoothGattList = new ArrayList<>();
    }


    /**
     * 打开蓝牙
     */
    private void openBle() {
//        boolean enable = bluetoothAdapter.enable();//打开蓝牙'直接打开，用户不知权，用于定制系统'
//        Toast.makeText(MainActivity.this, "正在打开蓝牙", Toast.LENGTH_SHORT).show();
//        if (enable) {
//            Log.e("open",enable+"");
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    scanLeDevice(true);
//                }
//            },2000);
//
//        }
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//提示用户正在打开蓝牙
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


    }


    /**
     * 打开或者停止扫描
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            mScanning = true;
            // 定义一个回调接口供扫描结束处理
            bluetoothAdapter.startLeScan(mLeScanCallback);
            // 预先定义停止蓝牙扫描的时间（因为蓝牙扫描需要消耗较多的电量）
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 连接蓝牙
     */
    public void connectBle(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
        if (bluetoothDevice != null) {
            //第二个参数 是否重连
            mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, bluetoothGattCallback);
            bluetoothGattList.add(mBluetoothGatt);
        }

    }

    /**
     * 断开蓝牙设备
     */
    public void bleDisConnectDevice(BluetoothDevice device) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }


    private String TAG = "MainActivity";
    /**
     * 蓝牙连接成功回调
     */

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        //不要执行耗时操作
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {//连接成功
                Log.e(TAG, "onConnectionStateChange 蓝牙连接");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "onConnectionStateChange 蓝牙断连");
                if (mBluetoothDevice != null) {//重新连接
                    //关闭当前新的连接
                    gatt.close();
                    characteristic = null;
                    adapterFreshHandler.sendEmptyMessage(0);
//                    connectBle(mBluetoothDevice);
                }

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {//以上的方法gatt.discoverServices();才会回调
            super.onServicesDiscovered(gatt, status);
            //回调之后，设备之间才真正通信连接起来
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onServicesDiscovered 蓝牙连接正常");
                BluetoothGattService service = gatt.getService(UUID.fromString(BleConstantValue.serverUuid));
                characteristic = service.getCharacteristic(UUID.fromString(BleConstantValue.charaUuid));
                gatt.readCharacteristic(characteristic);//执行之后，会执行下面的onCharacteristicRead的回调方法
                setCharacteristicNotification(characteristic, true);
                adapterFreshHandler.sendEmptyMessage(0);
            } else {
                Log.e(TAG, "onServicesDiscovered 蓝牙连接失败");
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(TAG, "callback characteristic read status " + status
                    + " in thread " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "read value: " + characteristic.getValue());
            }


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "write value: " + FormatUtil.bytesToHexString(characteristic.getValue()));
        }

        //设备发出通知时会调用到该接口
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG, "接收：" + FormatUtil.bytesToHexString(characteristic.getValue()));//byte[]转为16进制字符串
            bleWriteReceiveCallback();
        }


    };

    /**
     * 设置接收通知 即 另一端发送数据
     *
     * @param characteristic
     * @param enabled
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
//                .fromString(BleConstantValue.charaUuid));
//        if (descriptor != null) {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }

    }


    /**
     * 写入：接受成功指令
     */
    public void bleWriteReceiveCallback() {
        String writeStr = "REVOK\r\n";
        writeCmd(writeStr.getBytes());
    }

    /**
     * 写入命令
     *
     * @param cmd
     */
    private void writeCmd(byte[] cmd) {
        if (characteristic != null) {
            // 发出数据
            characteristic.setValue(cmd);
            if (mBluetoothGatt.writeCharacteristic(characteristic)) {
                Log.e(TAG, "写入成功");
            } else {
                Log.e(TAG, "写入失败");
            }
        } else {
            Toast.makeText(MainActivity.this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开蓝牙的回调，一般打开后开始扫描
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            scanLeDevice(true);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseResource();
        unregisterReceiver(bleListenerReceiver);

    }

    /**
     * 释放资源
     */

    private void releaseResource() {
        Log.e(TAG, "断开蓝牙连接，释放资源");
        for (BluetoothGatt gatt : bluetoothGattList) {
            if (gatt != null) {
                gatt.disconnect();
                gatt.close();

            }
        }
    }


    /**
     * 显示监听状态变化
     *
     * @param intent
     */
    private void showBleStateChange(Intent intent) {
        String action = intent.getAction();
        //连接的设备信息
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.e(TAG, "蓝牙监听广播…………………………" + action);

        if (mBluetoothDevice != null && mBluetoothDevice.equals(device)) {
            Log.e(TAG, "收到广播-->是当前连接的蓝牙设备");

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.e(TAG,"广播 蓝牙已经连接");

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.e(TAG,"广播 蓝牙断开连接");
            }
        } else {
            Log.e(TAG, "收到广播-->不是当前连接的蓝牙设备");
        }

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.e(TAG, "STATE_OFF 蓝牙关闭");
                    adapter.clear();
                    releaseResource();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.e(TAG, "STATE_TURNING_OFF 蓝牙正在关闭");
                    //停止蓝牙扫描
                    scanLeDevice(false);
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "STATE_ON 蓝牙开启");
                    //扫描蓝牙设备
                    scanLeDevice(true);
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.e(TAG, "STATE_TURNING_ON 蓝牙正在开启");
                    break;
            }
        }
    }

    /**
     * 蓝牙监听广播接受者
     */
    private BroadcastReceiver bleListenerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showBleStateChange(intent);
        }
    };

    /**
     * 注册蓝牙监听广播
     */
    private void registerBleListenerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bleListenerReceiver, intentFilter);
    }

}
