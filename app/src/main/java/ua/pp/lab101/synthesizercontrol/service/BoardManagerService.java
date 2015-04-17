package ua.pp.lab101.synthesizercontrol.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
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
    private IBinder mLocalBinder = new BoardManagerBinder();

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

    /*workflow variables*/
    private boolean mDeviceConnected;
    private double mCurrentFrequency;
    CurrentStatus mServiceStatus;

    public BoardManagerService() {
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate method");
        es = Executors.newFixedThreadPool(1);

        //getting an instance of Board controller
        adf = ADBoardController.getInstance();


        //if the service starts for the firs time it's status is idle.
        //Creating notification and declaring the service as foreground
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new Notification.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.bms_notif_title))
                .setContentText(getString(R.string.bms_notif_content_title)
                        + getString(R.string.bms_notif_content_not_connected));
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
        );
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        mNotificationBuilder.setSubText(getString(R.string.bms_notif_subcont_title)
                + getString(R.string.bms_notif_subcont_none));
        startForeground(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);

        //Opening the mFtdid2xx device
        if (mFtdid2xx == null) {
            try {
                mFtdid2xx = D2xxManager.getInstance(this);
            } catch (D2xxManager.D2xxException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed open FT245 device.");
            }
        }
        connectDevice();
    }

    public void connectDevice() {
        int openIndex = 0;
        if (mDevCount > 0) {
            Log.d(LOG_TAG, "There are openned devices");
            return;
        }
        mDevCount = mFtdid2xx.createDeviceInfoList(this);

        if (mDevCount > 0) {
            mFtDev = mFtdid2xx.openByIndex(this, openIndex);
            if (mFtDev == null) {
                Log.e(LOG_TAG, "Failed to open the device by index 0");
                return;
            }

            if (mFtDev.isOpen()) {
                mDeviceConnected = true;
                mFtDev.resetDevice();
                mFtDev.setBaudRate(9600);
                mFtDev.setLatencyTimer((byte) 16);
                mFtDev.setBitMode((byte) 0x0f, D2xxManager.FT_BITMODE_ASYNC_BITBANG);
                writeData(adf.geiInitianCommanSequence());
                Log.d(LOG_TAG, "Device vas found and configured");
                changeNotificationText(getString(R.string.bms_notif_content_title)
                        + getString(R.string.bms_notif_content_connected), getString(R.string.bms_notif_subcont_title));
            } else {
                Log.e(LOG_TAG, "Permission error");
            }
        } else {
            Log.e(LOG_TAG, "Failed to create the device info list");
        }
    }

    public void configureTheDevice() {

    }

    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy method");
        if (mFtDev != null && mFtDev.isOpen()) {
            writeData(adf.turnOffTheDevice());
            mFtDev.close();
            mFtDev = null;
        }
        this.unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStart method called");
        connectDevice();
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        return mLocalBinder;
    }

    class ADFWriterRun implements Runnable {
        double frequencyValue;
        int startId;


        public ADFWriterRun(double frequencyValue) {
            this.frequencyValue = frequencyValue;
            this.startId = 0;
            Log.d(LOG_TAG, "Write run created");
        }

        public void run() {
            Log.d(LOG_TAG, "Write run started");
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
            //stopSelf(startId);
        }
    }

    private synchronized void writeData(byte[][] commands) {
        for (int i = 0; i < commands.length; i++) {
            int result = writeDataToRegister(commands[i]);
            Log.i(LOG_TAG, Integer.toString(result) + " bytes wrote to reg" + Integer.toString(i));
        }
    }

    private int writeDataToRegister(byte[] data) {
        return mFtDev.write(data);
    }

    public class BoardManagerBinder extends Binder {
        public BoardManagerService getService() {
            return BoardManagerService.this;
        }
    }

    /*public API classes*/
    public void changeNotificationText(String content, String subtext) {
        Log.d(LOG_TAG, "method called. Text: " + content);
        if (content != null) {
            mNotificationBuilder.setContentText(content);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
        }

        if (subtext != null) {
            mNotificationBuilder.setSubText(subtext);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
        }
    }

    public void setFrequency(double frequency) {
        if (frequency > 0) {
            ADFWriterRun mr = new ADFWriterRun(frequency);
            es.execute(mr);
        }
    }

    /*Lol shitcode style test programming YOBA*/
    /*this BroadcastReceiver receives state change of the device connection and manage all
     *the changes in the Board service logic and enforces the control activity to change it
     *it state and visualize current situation.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.d(LOG_TAG, "Device detached!");
                /*TODO create
                 * 1)realization of operation control activity notification
                 * 2)realization of current operation state stop&save
                 */
                mDeviceConnected = false;
                mDevCount = -1;
                changeNotificationText(getString(R.string.bms_notif_content_title)
                        + getString(R.string.bms_notif_content_not_connected), null);

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d(LOG_TAG, "Device attached!");
                /*TODO create
                 * 1)realization of operation control activity notification
                 * 2)realization of current operation state stop&save
                 */
                changeNotificationText(getString(R.string.bms_notif_title)
                        + getString(R.string.bms_notif_content_found),
                        getString(R.string.bms_notif_subcont_plase_tap));

            }
        }
    };
}