package com.xmg.bluetoothearphone;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.xmg.bluetoothearphone.adapter.BlueAdapter;
import com.xmg.bluetoothearphone.base.BaseActivity;
import com.xmg.bluetoothearphone.bean.BlueDevice;
import com.xmg.bluetoothearphone.utlis.AudioUtils;
import com.xmg.bluetoothearphone.utlis.BlueUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int ENABLE_BLUE = 100;
    private static final String MAINACTIVITY = "MainActivity";
    /**自定义返回码*/
    private static final int MY_PERMISSION_REQUEST_CONSTANT = 123456;
    @Bind(R.id.btn_start)
    Button btnStart;
    @Bind(R.id.btn_public)
    Button btnPublic;
    @Bind(R.id.btn_search)
    Button btnSearch;
    @Bind(R.id.ls_blue)
    ListView lsBlue;
    @Bind(R.id.btn_stop)
    Button btnStop;
    @Bind(R.id.btn_play)
    Button btnPlay;
    @Bind(R.id.btn_stop_play)
    Button btnStopPlay;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> devices;
    private Set<BlueDevice> setDevices = new HashSet<>();
    private BlueAdapter blueAdapter;
    private IntentFilter mFilter;
    /**蓝牙音频传输协议*/
    private BluetoothA2dp a2dp;
    /**需要连接的蓝牙设备*/
    private BluetoothDevice currentBluetoothDevice;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        /**判断手机系统的版本*/
        if (Build.VERSION.SDK_INT >= 6.0) {//Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                /**动态添加权限：ACCESS_FINE_LOCATION*/
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_CONSTANT);
            }
        }
        initDate();
        setListener();

    }

    /**请求权限的回调：这里判断权限是否添加成功*/
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CONSTANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("main","添加权限成功");
                }
                return;
            }
        }
    }

    private void initDate() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /**注册搜索蓝牙receiver*/
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, mFilter);
        /**注册配对状态改变监听器*/
//        IntentFilter mFilter = new IntentFilter();
//        mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        mFilter.addAction(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, mFilter);

        getBondedDevices();
    }

    private void setListener() {
        btnStart.setOnClickListener(this);
        btnPublic.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnStopPlay.setOnClickListener(this);
        lsBlue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<BlueDevice> listDevices = blueAdapter.getListDevices();
                final BlueDevice blueDevice = listDevices.get(i);
                String msg="";
                /**还没有配对*/
                if (blueDevice.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
                    msg="是否与设备" + blueDevice.getName() + "配对并连接？";
                }else{
                    msg= "是否与设备" + blueDevice.getName() + "连接？";
                }
                showDailog(msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /**当前需要配对的蓝牙设备*/
                        currentBluetoothDevice = blueDevice.getDevice();
                        /**还没有配对*/
                        if (blueDevice.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
                            startPariBlue(blueDevice);
                        } else {
                            /**完成配对的,直接连接*/
                            contectBuleDevices();
                        }
                        showProgressDailog();
                    }
                });
            }
        });

        /**长按取消配对*/
        lsBlue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<BlueDevice> listDevices = blueAdapter.getListDevices();
                final BlueDevice blueDevices = listDevices.get(i);
                /**还没有配对*/
                if (blueDevices.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
                    return false;
                    /**完成配对的*/
                } else {
                    showDailog("是否取消" + blueDevices.getName() + "配对？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            BlueUtils.unpairDevice(blueDevices.getDevice());
                        }
                    });
                }
                return false;
            }
        });
    }


    @Override
    public void onClick(View view) {
        if (mBluetoothAdapter == null)
            return;
        switch (view.getId()) {
            case R.id.btn_start:
                /**如果本地蓝牙没有开启，则开启*/
                if (!mBluetoothAdapter.isEnabled()) {
                    // 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，
                    // 那么将会收到RESULT_OK的结果，
                    // 如果RESULT_CANCELED则代表用户不愿意开启蓝牙
                    Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(mIntent, ENABLE_BLUE);
                } else {
                    Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                    getBondedDevices();
                }
                break;
            case R.id.btn_public:
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180);//180可见时间
                startActivity(intent);
                break;
            case R.id.btn_search:

                showProgressDailog();
                // 如果正在搜索，就先取消搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                // 开始搜索蓝牙设备,搜索到的蓝牙设备通过广播返回
                mBluetoothAdapter.startDiscovery();
                break;
            case R.id.btn_stop:
                /**关闭蓝牙*/
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }
                break;
            case R.id.btn_play:
                AudioUtils.INSTANCE.playMedai(MainActivity.this);
                break;
            case R.id.btn_stop_play:
                AudioUtils.INSTANCE.stopPlay();
                break;
            default:
                break;
        }
    }

    /**
     * 开始配对蓝牙设备
     *
     * @param blueDevice
     */
    private void startPariBlue(BlueDevice blueDevice) {
        BlueUtils blueUtils = new BlueUtils(blueDevice);
        blueUtils.doPair();
    }

    /**
     * 取消配对蓝牙设备
     *
     * @param blueDevice
     */
    private void stopPariBlue(BlueDevice blueDevice) {
        BlueUtils.unpairDevice(blueDevice.getDevice());
    }

    /**
     * 开始连接蓝牙设备
     */
    private void contectBuleDevices() {
        /**使用A2DP协议连接设备*/
        mBluetoothAdapter.getProfileProxy(this, mProfileServiceListener, BluetoothProfile.A2DP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙开启成功", Toast.LENGTH_SHORT).show();
                getBondedDevices();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "蓝牙开始失败", Toast.LENGTH_SHORT).show();
            }
        } else {

        }
    }

    /**
     * 获取所有已经绑定的蓝牙设备并显示
     */
    private void getBondedDevices() {
        if(!setDevices.isEmpty())
            setDevices.clear();
        devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : devices) {
            BlueDevice blueDevice = new BlueDevice();
            blueDevice.setName(bluetoothDevice.getName());
            blueDevice.setAddress(bluetoothDevice.getAddress());
            blueDevice.setDevice(bluetoothDevice);
            blueDevice.setStatus("已配对");
            setDevices.add(blueDevice);
        }
        if (blueAdapter == null) {
            blueAdapter = new BlueAdapter(this, setDevices);
            lsBlue.setAdapter(blueAdapter);
        } else {
            blueAdapter.setSetDevices(setDevices);
            blueAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        ButterKnife.unbind(this);

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /** 搜索到的蓝牙设备*/
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 搜索到的不是已经配对的蓝牙设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    BlueDevice blueDevice = new BlueDevice();
                    blueDevice.setName(device.getName());
                    blueDevice.setAddress(device.getAddress());
                    blueDevice.setDevice(device);
                    setDevices.add(blueDevice);
                    blueAdapter.setSetDevices(setDevices);
                    blueAdapter.notifyDataSetChanged();
                    Log.d(MAINACTIVITY, "搜索结果......"+device.getName());
                }

                /**当绑定的状态改变时*/
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(MAINACTIVITY, "正在配对......");

                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(MAINACTIVITY, "完成配对");
                        hideProgressDailog();
                        /**开始连接*/
                        contectBuleDevices();
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(MAINACTIVITY, "取消配对");
                        Toast.makeText(MainActivity.this,"成功取消配对",Toast.LENGTH_SHORT).show();
                        getBondedDevices();
                        break;
                    default:
                        break;
                }

                /**搜索完成*/
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                setProgressBarIndeterminateVisibility(false);
                Log.d(MAINACTIVITY, "搜索完成......");
                hideProgressDailog();
            }
        }
    };

    /**
     * 连接蓝牙设备（通过监听蓝牙协议的服务，在连接服务的时候使用BluetoothA2dp协议）
     */
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {

        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            try {
                if (profile == BluetoothProfile.HEADSET) {
//                    bh = (BluetoothHeadset) proxy;
//                    if (bh.getConnectionState(mTouchObject.bluetoothDevice) != BluetoothProfile.STATE_CONNECTED){
//                        bh.getClass()
//                                .getMethod("connect", BluetoothDevice.class)
//                                .invoke(bh, mTouchObject.bluetoothDevice);
//                    }

                } else if (profile == BluetoothProfile.A2DP) {
                    /**使用A2DP的协议连接蓝牙设备（使用了反射技术调用连接的方法）*/
                    a2dp = (BluetoothA2dp) proxy;
                    if (a2dp.getConnectionState(currentBluetoothDevice) != BluetoothProfile.STATE_CONNECTED) {
                        a2dp.getClass()
                                .getMethod("connect", BluetoothDevice.class)
                                .invoke(a2dp, currentBluetoothDevice);
                        Toast.makeText(MainActivity.this,"请播放音乐",Toast.LENGTH_SHORT).show();
                        getBondedDevices();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


}
