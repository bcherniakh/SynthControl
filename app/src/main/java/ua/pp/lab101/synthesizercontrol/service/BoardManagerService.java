package ua.pp.lab101.synthesizercontrol.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import ua.pp.lab101.synthesizercontrol.MainActivity;
import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.adregisters.ADBoardController;

public class BoardManagerService extends Service {
    //IBinder realization that will be returned in binding process
    private IBinder mLocalBinder = new LocalBinder();

    private static final int ONGOING_NOTIFICATION_ID = 1488;
    private static final String LOG_TAG = "BoardService";
    private ExecutorService es;

    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder;

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
        Log.d(LOG_TAG, "onCreate method");
        es = Executors.newFixedThreadPool(1);
        adf = ADBoardController.getInstance();

        //Opening the mFtdid2xx device
        if (mFtdid2xx == null) {
            try {
                mFtdid2xx = D2xxManager.getInstance(this);
            } catch (D2xxManager.D2xxException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed open FT245 device.");
            }
        }
        connectTheDevice();

        //Creating notification and declaring the service as foreground
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new Notification.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Board manager service")
                .setContentText("No device present");
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                );
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        startForeground (ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
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
                Log.d(LOG_TAG, "Device vas found and configured");
                changeNotificationText("Device connected");
            } else {
                Log.d(LOG_TAG, "permission error");
            }
        } else {
            Log.e(LOG_TAG, "Could not open the device");
        }
    }

    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy method");
        if (mFtDev != null && mFtDev.isOpen()) {
            writeData(adf.turnOffTheDevice());
            mFtDev.close();
            mFtDev = null;
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStart method called");
        //double frequency = intent.getDoubleExtra("frequency", 1);
//        if (frequency > 0) {
//            ADFWriterRun mr = new ADFWriterRun(frequency, startId);
//            es.execute(mr);
//        }
        String text = intent.getStringExtra("Text");
        mNotificationBuilder.setContentText(text);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        return mLocalBinder;
    }

    class ADFWriterRun implements Runnable {
        double frequencyValue;
        int startId;


        public ADFWriterRun(double frequencyValue, int startId) {
            this.frequencyValue = frequencyValue;
            this.startId = startId;
            Log.d(LOG_TAG, "Write run" + startId + " created");
        }

        public void run() {
            Log.d(LOG_TAG, "Write run" + startId + " started");
            writeData(adf.setFrequency(frequencyValue));
            writeData(adf.turnOnDevice());
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "Pause was interrupted");
            }
            stop();
        }

        void stop() {
            Log.d(LOG_TAG, "service stops" + startId + " end, stopSelf(" + startId + ")");
            stopSelf(startId);
        }
    }

    private synchronized void  writeData(byte[][] commands) {
        for (int i = 0; i < commands.length; i++) {
            int result = writeDataToRegister(commands[i]);
            Log.i(LOG_TAG, Integer.toString(result) + " bytes wrote to reg" + Integer.toString(i));
        }
    }

    private int writeDataToRegister(byte[] data) {
        return mFtDev.write(data);
    }

    public class LocalBinder extends Binder {
        public BoardManagerService getService() {
            return BoardManagerService.this;
        }
    }

    /*public API classes*/
    public void changeNotificationText(String text) {
        Log.d(LOG_TAG, "methos called. Text: " + text);
        if (text == null) return;
        mNotificationBuilder.setContentText(text);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
    }
}
