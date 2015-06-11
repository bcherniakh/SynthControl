package ua.pp.lab101.synthesizercontrol.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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

import ua.pp.lab101.synthesizercontrol.activity.main.MainActivity;
import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.adregisters.ADBoardController;
import ua.pp.lab101.synthesizercontrol.service.task.Task;
import ua.pp.lab101.synthesizercontrol.service.task.TaskType;

public class BoardManagerService extends Service {
    //IBinder realization that will be returned in binding process
    private IBinder mLocalBinder = new BoardManagerBinder();

    //Broadcast messages
    public static final String INTENT_TASK_DONE = "ua.pp.lab101.synthesizercontrol.taskdone";
    public static final String INTENT_DEVICE_UNPLUGGED = "ua.pp.lab101.synthesizercontrol.deviceunplugged";
    public static final String INTENT_DEVICE_PLUGGED = "ua.pp.lab101.synthesizercontrol.deviceplugged";
    public static final String TASK_TYPE = "ua.pp.lab101.synthesizercontrol.tasktype";
    public static final int TASK_TYPE_SCHEDULE = 1;
    public static final int TASK_TYPE_CONSTANT = 2;
    public static final int TASK_TYPE_FREQUENCY_SCAN = 3;

    private static final int ONGOING_NOTIFICATION_ID = 1488;
    private static final String LOG_TAG = "BoardManager";
    private Thread currentThreadTask;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    /*System elements. Context and usbdevice*/
    private D2xxManager mFtdid2xx = null;
    private FT_Device mFtDev = null;
    private int mDevCount = -1;

    /**/
    private ADBoardController adf;

    /*workflow variables*/
    private boolean mDeviceConnected;
    private double mCurrentFrequency;
    private Task mCurrentTask;
    private ServiceStatus mServiceStatus;

    public BoardManagerService() {
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate method");

        //getting an instance of Board controller
        adf = ADBoardController.getInstance();

        //if the service starts for the firs time it's status is idle.
        //Creating notification and declaring the service as foreground
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.bms_notif_title));
        Intent tapResultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(tapResultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
        );
        mNotificationBuilder.setContentIntent(resultPendingIntent);
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

        /*first run initialization*/
        mCurrentFrequency = 0;
        changeServiceStatus(ServiceStatus.IDLE);
    }

    private void connectDevice() {
        int openIndex = 0;
        if (mDevCount > 0) {
            Log.d(LOG_TAG, "There are opened devices");
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
                setupTheDevice();
                Log.d(LOG_TAG, "Device vas found and configured");
                mDeviceConnected = true;
            } else {
                Log.e(LOG_TAG, "Permission error");
            }
        } else {
            Log.e(LOG_TAG, "Failed to create the device info list");
        }
    }

    private void setupTheDevice() {
        mFtDev.resetDevice();
        mFtDev.setBaudRate(9600);
        mFtDev.setLatencyTimer((byte) 16);
        mFtDev.setBitMode((byte) 0x0f, D2xxManager.FT_BITMODE_ASYNC_BITBANG);
        writeData(adf.geiInitianCommanSequence());
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
        if (!mDeviceConnected) {
            connectDevice();
            changeServiceStatus(ServiceStatus.IDLE);
        }
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        return mLocalBinder;
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

    private void shutdownDevice() {
        if (mDeviceConnected) {
            changeServiceStatus(ServiceStatus.IDLE);
            writeData(adf.turnOffTheDevice());
        } else {
            Log.e(LOG_TAG, "Turn off attempt. Device disconnected.");
        }
    }

    public void stopAnyWorkingTask() {
        if (mServiceStatus.equals(ServiceStatus.SCHEDULE_MODE) || mServiceStatus.equals(ServiceStatus.FREQUENCY_SCAN_MODE)) {
            currentThreadTask.interrupt();
        }
        shutdownDevice();
    }

    public boolean isDeviceConnected() {
        return mDeviceConnected;
    }


    public void performTask(Task task) {
        if (mDeviceConnected == false) return;
        TaskType currentTaskType = task.getTaskType();
        mCurrentTask = task;
        switch (currentTaskType) {
            case CONSTANT_FREQUENCY_MODE:
                performConstantModeTask(task.getConstantFrequency());
                break;
            case SCHEDULE_MODE:
                performScheduleTask(task.getFrequencyArray(), task.getTimeArray(), task.getIsCycled());
                break;
            case FREQUENCY_SCAN_MODE:
                performFrequencyScanTask(task.getStartFrequency(), task.getFinishFrequency(), task.getFrequencyStep(),
                        task.getTimeStep(), task.getIsCycled());
                break;
        }
    }



    public ServiceStatus getCurrentStatus() {
        return mServiceStatus;
    }

    public double getCurrentFrequency() {
        return mCurrentFrequency;
    }

    public Task getCurrentTask() {
        return mCurrentTask;
    }

    private void performConstantModeTask(double frequencyValue) {
        setFrequencyOnTheDevice(frequencyValue);
        changeServiceStatus(ServiceStatus.CONSTANT_MODE);
    }

    private void performScheduleTask(double[] frequency, int[] time, boolean isCycled) {
        if (frequency.length != time.length) {
            Log.e(LOG_TAG, "Array length mismatch");
            return;
        }

        for (int i = 0; i < time.length; i++) {
            time[i] = time[i] * 1000;
        }
        TaskPerformer performer = new TaskPerformer(frequency, time, isCycled);
        currentThreadTask = new Thread(performer);
        currentThreadTask.start();
        changeServiceStatus(ServiceStatus.SCHEDULE_MODE);
    }

    private void performFrequencyScanTask(double startFrequency, double finishFrequency, double frequencyStep, int timeStep, boolean isCycled) {
        double[] frequency = getFrequencyArray(startFrequency, finishFrequency, frequencyStep);
        int[] time = getTimeArray(startFrequency, finishFrequency, frequencyStep, timeStep);
        TaskPerformer performer = new TaskPerformer(frequency, time, isCycled);
        currentThreadTask = new Thread(performer);
        currentThreadTask.start();
        changeServiceStatus(ServiceStatus.FREQUENCY_SCAN_MODE);
    }

    private int[] getTimeArray(double startFrequency, double finishFrequency, double frequencyStep, int timeStep) {
        double range = finishFrequency - startFrequency;
        int entries = (int) (range / frequencyStep) + 1;
        int[] time = new int[entries];
        for (int i = 0; i < entries; i++) {
            time[i] = timeStep;
        }
        return time;
    }

    private double[] getFrequencyArray(double startFrequency, double finishFrequency, double frequencyStep) {
        double range = finishFrequency - startFrequency;
        int entries = (int) (range / frequencyStep) + 1;
        double[] frequency = new double[entries];
        frequency[0] = startFrequency;
        for (int i = 1; i < entries; i++) {
            frequency[i] = frequency[i - 1] + frequencyStep;
        }
        return frequency;
    }

    private void setFrequencyOnTheDevice(double frequencyValue) {
        if (mDeviceConnected) {
            mCurrentFrequency = frequencyValue;
            writeData(adf.setFrequency(frequencyValue));
            writeData(adf.turnOnDevice());
        } else {
            Log.e(LOG_TAG, "Set frequency attempt. Device disconnected.");
        }
    }

    class TaskPerformer implements Runnable {
        private double[] frequency;
        private int[] time;
        private boolean isCycled;
        private boolean interrupted;

        public TaskPerformer(double[] frequency, int[] time, boolean isCycled) {
            this.frequency = Arrays.copyOf(frequency, frequency.length);
            this.time = Arrays.copyOf(time, time.length);
            this.isCycled = isCycled;
            Log.d(LOG_TAG, "Task created");
            this.interrupted = false;
        }

        public void run() {
            Log.d(LOG_TAG, "Task started");
            do {
                for (int i = 0; i < frequency.length; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        interrupted = true;
                        break;
                    }
                    mCurrentFrequency = frequency[i];
                    changeFrequencyVisualization();
                    writeData(adf.setFrequency(frequency[i]));
                    writeData(adf.turnOnDevice());
                    try {
                        TimeUnit.MILLISECONDS.sleep(time[i]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        interrupted = true;
                        Log.d(LOG_TAG, "Pause was interrupted");
                        break;
                    }
                }
            }
            while (isCycled && !interrupted);
            stop();
        }
        void stop() {
            Log.d(LOG_TAG, "Task ends");
            shutdownDevice();
            Intent intent = new Intent(INTENT_TASK_DONE);
            intent.putExtra(TASK_TYPE, TASK_TYPE_SCHEDULE);
            sendBroadcast(intent);
        }
    }

    private void changeServiceStatus(ServiceStatus newStatus) {
        String serviceStatusContent = "";
        String serviceStatusSubtext = "";
        switch (newStatus) {
            case IDLE:
                mServiceStatus = newStatus;
                serviceStatusContent = (mDeviceConnected) ? getString(R.string.bms_notif_content_status_idle)
                        : getString(R.string.bms_notif_content_status_disconnected);
                serviceStatusSubtext = "0";
                changeNotificationAll(serviceStatusContent, serviceStatusSubtext);
                break;
            case CONSTANT_MODE:
                mServiceStatus = newStatus;
                serviceStatusContent = getString(R.string.bms_notif_content_status_constant);
                serviceStatusSubtext = String.valueOf(mCurrentFrequency);
                changeNotificationAll(serviceStatusContent, serviceStatusSubtext);
                break;
            case SCHEDULE_MODE:
                mServiceStatus = newStatus;
                serviceStatusContent = getString(R.string.bms_notif_content_status_schedule);
                serviceStatusSubtext = String.valueOf(mCurrentFrequency);
                changeNotificationAll(serviceStatusContent, serviceStatusSubtext);
                break;
            case FREQUENCY_SCAN_MODE:
                mServiceStatus = newStatus;
                serviceStatusContent = getString(R.string.bms_notif_content_status_freqscan);
                serviceStatusSubtext = String.valueOf(mCurrentFrequency);
                changeNotificationAll(serviceStatusContent, serviceStatusSubtext);
                break;
            case DEVICE_DISCONNECTED:
                mServiceStatus = newStatus;
                mDeviceConnected = false;
                mDevCount = -1;
                serviceStatusContent = getString(R.string.bms_notif_content_status_disconnected);
                changeNotificationAll(serviceStatusContent, "0");
                break;
        }

    }

    private void changeFrequencyVisualization() {
        String frequencyString = String.valueOf(mCurrentFrequency);
        changeNotificationSubtext(frequencyString);
    }

    private void changeNotificationAll(String content, String subtext) {
        Log.d(LOG_TAG, "method called. Text: " + content);
        if (content != null) {
            String information = getString(R.string.bms_notif_content_title) + " "
                    + content;
            mNotificationBuilder.setContentText(information);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
        }

        if (subtext != null) {
            String information = getString(R.string.bms_notif_subcont_title) + " "
                    + subtext;
            mNotificationBuilder.setSubText(information);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
        }
    }

    private void changeNotificationContent(String content) {
        Log.d(LOG_TAG, "method called. Text: " + content);
        if (content != null) {
            String information = getString(R.string.bms_notif_content_title) + " "
                    + content;
            mNotificationBuilder.setContentText(information);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
        }
    }

    private void changeNotificationSubtext(String subtext) {
        Log.d(LOG_TAG, "method called. Text: " + subtext);
        if (subtext != null) {
            String information = getString(R.string.bms_notif_subcont_title) + " "
                    + subtext;
            mNotificationBuilder.setSubText(information);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mNotificationBuilder.build());
        }
    }


    public class BoardManagerBinder extends Binder {
        public BoardManagerService getService() {
            return BoardManagerService.this;
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
                Intent deviceUnpluggedIntent = new Intent(INTENT_DEVICE_UNPLUGGED);
                sendBroadcast(deviceUnpluggedIntent);
                changeServiceStatus(ServiceStatus.DEVICE_DISCONNECTED);
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d(LOG_TAG, "Device attached!");
                Intent devicePluggedIntent = new Intent(INTENT_DEVICE_PLUGGED);
                sendBroadcast(devicePluggedIntent);
                changeNotificationAll(getString(R.string.bms_notif_content_staus_found),
                        getString(R.string.bms_notif_subcont_plase_tap));
            }
        }
    };
}