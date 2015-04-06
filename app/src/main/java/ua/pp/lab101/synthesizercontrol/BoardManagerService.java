package ua.pp.lab101.synthesizercontrol;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import ua.pp.lab101.synthesizercontrol.ADRegisters.ADBoardController;

public class BoardManagerService extends Service {

    private static final String LOG_TAG = "BoardService";
    private ExecutorService es;

    /*System elements. Context and usbdevice*/
    private D2xxManager mFtdid2xx = null;
    private FT_Device mFtDev = null;
    private int mDevCount = -1;

    /**/
    private ADBoardController adf;
    /*Logic workflow variables*/

    public BoardManagerService() {
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "MyService onCreate");
        es = Executors.newFixedThreadPool(1);
        adf = ADBoardController.getInstance();
        if (mFtdid2xx == null) {
            try {
                mFtdid2xx = D2xxManager.getInstance(this);
            } catch (D2xxManager.D2xxException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed open FT245 device.");
            }
        }
        connectTheDevice();
    }

    public void connectTheDevice() {
        int openIndex = 0;
        if (mDevCount > 0)
            return;

        mDevCount = mFtdid2xx.createDeviceInfoList(this);
        if (mDevCount > 0) {
            mFtDev = mFtdid2xx.openByIndex(this, openIndex);

            if (mFtDev == null) {
                return;
            }

            if (mFtDev.isOpen()) {
                mFtDev.resetDevice();
                mFtDev.setBaudRate(9600);
                mFtDev.setLatencyTimer((byte) 16);
                mFtDev.setBitMode((byte) 0x0f, D2xxManager.FT_BITMODE_ASYNC_BITBANG);
                writeData(adf.geiInitianCommanSequence());
                Log.d(LOG_TAG, "Device vas found");
            } else {
                Log.d(LOG_TAG, "permission error");
            }
        } else {
            Log.e(LOG_TAG, "Could not open the device");
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "MyService onDestroy");
        if (mFtDev != null && mFtDev.isOpen()) {
            writeData(adf.turnOffTheDevice());
            mFtDev.close();
            mFtDev = null;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "MyService onStartCommand");
        double time = intent.getDoubleExtra("frequency", 1);
        MyRun mr = new MyRun(time, startId);
        es.execute(mr);
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    class MyRun implements Runnable {
        double frequencyValue;
        int startId;


        public MyRun(double frequencyValue, int startId) {
            this.frequencyValue = frequencyValue;
            this.startId = startId;
            Log.d(LOG_TAG, "Write run" + startId + " create");
        }

        public void run() {
            Log.d(LOG_TAG, "MyRun#" + startId + " start");
            writeData(adf.setFrequency(frequencyValue));
            writeData(adf.turnOnDevice());
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeData(adf.setFrequency(35));
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stop();
        }

        void stop() {
            Log.d(LOG_TAG, "MyRun#" + startId + " end, stopSelf(" + startId + ")");
            stopSelf(startId);
        }
    }

    private void writeData(byte[][] commands) {
        for (int i = 0; i < commands.length; i++) {
            int result = writeDataToRegister(commands[i]);
            Log.i(LOG_TAG, Integer.toString(result) + " bytes wrote to reg" + Integer.toString(i));
        }
    }

    private int writeDataToRegister(byte[] data) {
        return mFtDev.write(data);
    }
}
