package ua.pp.lab101.synthesizercontrol.activity.accessory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;


import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.FrequencyScanModeFragment;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.SchedulerModeFragment;

public class ReadDataFromCSVFileActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String READ_FILE_TYPE_ID = "read_file_type";
    public static final int READ_SCHEDULE_FILE = 0;
    public static final int READ_FREQUENCY_SCAN_FILE = 1;

    private static final String LOG_TAG = "ReadAct";
    private Button mOpenBtn = null;
    private ListView mFilesListView;
    private TextView mTitleTextView;
    private boolean mIsStorageReadable = false;
    private boolean mErrorOccurred = false;
    private int mReadFileTypeID;

    private boolean mDirectoryAvailable = false;
    private static final String mDirectoryName = "SynthControl";
    private static final String mScheduleDirectoryName = "Schedule";
    private static final String mFrequencyScanDirectoryName = "FrequencyScan";
    private String mDirectoryPath;
    private ArrayList<String> mFileNames = new ArrayList<>();

    private double[] mFrequency;
    private int[] mTime;

    private double[] mFromFrequency;
    private double[] mToFrequency;
    private double[] mFrequencyStep;
    private double[] mTimeStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_csv_file);
        mOpenBtn = (Button) findViewById(R.id.readFileOpenBtn);
        mOpenBtn.setOnClickListener(this);
        mFilesListView = (ListView) findViewById(R.id.readFileFileNameLv);
        mTitleTextView = (TextView) findViewById(R.id.readFileTitleTv);

        Intent intent = getIntent();
        mReadFileTypeID = intent.getIntExtra(READ_FILE_TYPE_ID, READ_SCHEDULE_FILE);
        if (mReadFileTypeID == READ_SCHEDULE_FILE) {
            mTitleTextView.setText(R.string.read_title_schedule);
        } else if (mReadFileTypeID == READ_FREQUENCY_SCAN_FILE) {
            mTitleTextView.setText(R.string.read_title_freq_scan);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initiateDirectory();
        mFilesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,         mFileNames);
        mFilesListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        mErrorOccurred = false;
        int selected = mFilesListView.getCheckedItemPosition();
        if (selected < 0 ) {
            showToast(getString(R.string.read_toast_error_select_entry));
            mErrorOccurred = true;
            return;
        }
        String fileName = mFileNames.get(mFilesListView.getCheckedItemPosition());
        ArrayList<String[]> fileContent = getContentFromFile(fileName);
        parseContent(fileContent);
        if (mErrorOccurred) {
            return;
        }

        Intent intent = prepareIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private Intent prepareIntent() {
        if (mReadFileTypeID == READ_SCHEDULE_FILE) {
            return prepareScheduleIntent();
        } else if (mReadFileTypeID == READ_FREQUENCY_SCAN_FILE) {
            return prepareFrequencyScanIntent();
        } else {
            return prepareEmptyIntent();
        }
    }

    private Intent prepareFrequencyScanIntent() {
        Intent intent = new Intent();
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_FROM_FREQUENCY_ARRAY, mFromFrequency);
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_TO_FREQUENCY_ARRAY, mToFrequency);
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_FREQUENCY_STEP_ARRAY, mFrequencyStep);
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_TIME_STEP_ARRAY, mTimeStep);
        return intent;
    }

    private Intent prepareScheduleIntent() {
        Intent intent = new Intent();
        intent.putExtra(SchedulerModeFragment.ATTRIBUTE_TIME_ARRAY, mTime);
        intent.putExtra(SchedulerModeFragment.ATTRIBUTE_FREQUENCY_ARRAY, mFrequency);
        return intent;
    }
    private Intent prepareEmptyIntent() {
        return new Intent();
    }

    private void parseContent(ArrayList<String[]> fileContent) {
        if (fileContent.isEmpty()) {
            Log.d(LOG_TAG, "File content is empty");
            handleOccurredError(getString(R.string.read_dialog_error_file_empty), false);
            return;
        }

        switch (mReadFileTypeID) {
            case READ_SCHEDULE_FILE : parseScheduleContent(fileContent);
                break;
            case READ_FREQUENCY_SCAN_FILE: parseFrequencyScanContent(fileContent);
                break;
        }
    }

    private void parseScheduleContent(ArrayList<String[]> fileContent) {
        mFrequency = new double[fileContent.size()];
        mTime = new int[fileContent.size()];

        double frequency;
        int hours;
        int minutes;
        int seconds;

        for (int i = 0; i < fileContent.size(); i++) {
            String[] nextLine = fileContent.get(i);
            if (nextLine.length != 4) {
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_parse_error), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            try {
                frequency = Double.parseDouble(nextLine[0]);
                hours = Integer.parseInt(nextLine[1]);
                minutes = Integer.parseInt(nextLine[2]);
                seconds = Integer.parseInt(nextLine[3]);
            } catch (NumberFormatException exception) {
                exception.printStackTrace();
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_parse_error), i);
                handleOccurredError(errorMessage, false);
                return;
            }
            if (!checkFrequency(frequency)) {
                Log.d(LOG_TAG, "Frequency incorrect");
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_frequency_range), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            if (!checkTime(hours, minutes, seconds)) {
                Log.d(LOG_TAG, "Time is incorrect");
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_time_range), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            mFrequency[i] = getRoundedValue(frequency);
            mTime[i] = getTimeInSeconds(hours, minutes, seconds);
        }
    }

    private void parseFrequencyScanContent(ArrayList<String[]> fileContent) {
        mFromFrequency = new double[fileContent.size()];
        mToFrequency = new double[fileContent.size()];
        mFrequencyStep = new double[fileContent.size()];
        mTimeStep = new double[fileContent.size()];

        double fromFrequency;
        double toFrequency;
        double frequencyStep;
        double timeStep;

        for (int i = 0; i < fileContent.size(); i++) {
            String[] nextLine = fileContent.get(i);
            if (nextLine.length != 4) {
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_parse_error), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            try {
                fromFrequency = Double.parseDouble(nextLine[0]);
                toFrequency = Double.parseDouble(nextLine[1]);
                frequencyStep = Double.parseDouble(nextLine[2]);
                timeStep = Double.parseDouble(nextLine[3]);
            } catch (NumberFormatException exception) {
                exception.printStackTrace();
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_parse_error), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            if (!(checkFrequency(fromFrequency) && checkFrequency(toFrequency))) {
                Log.e(LOG_TAG, "Frequency incorrect");
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_frequency_range), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            if (!checkFrequencyStepValue(frequencyStep)) {
                Log.d(LOG_TAG, "Frequency step is incorrect");
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_frequency_step_range), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            if(!checkTimeStepValue(timeStep)) {
                Log.e(LOG_TAG, "Time step is incorrect");
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_time_step_range), i);
                handleOccurredError(errorMessage, false);
                return;
            }

            mFromFrequency[i] = getRoundedValue(fromFrequency);
            mToFrequency[i] = getRoundedValue(toFrequency);
            mFrequencyStep[i] = getRoundedValue(frequencyStep);
            mTimeStep[i] = getRoundedValue(timeStep);
        }
    }

    private double getRoundedValue(double frequency) {
        return new BigDecimal(frequency).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
    }

    private boolean checkTime(int hours, int minutes, int seconds) {
        if (hours < 0 || minutes < 0 || seconds < 0) return  false;
        int timeInSeconds = getTimeInSeconds(hours, minutes, seconds);
        if (timeInSeconds > 6 * 3600) return false;
        return true;
    }

    private boolean checkTimeStepValue(double timeStep) {
        if ((timeStep >= 0.001) && (timeStep <= 3600)) return true;
        return false;
    }

    private boolean checkFrequencyStepValue(double frequencyStep) {
        if ((frequencyStep >= 0.001) && (frequencyStep <= 1000)) return true;
        return false;
    }

    private int getTimeInSeconds(int hours, int minutes, int seconds) {
        return hours * 3600 + minutes * 60 + seconds;
    }

    private boolean checkFrequency(double frequency) {
        return (frequency >= 35 && frequency <= 4400);
    }

    private String builtDataErrorMessage(String message, int lineNumber) {
        StringBuilder builder = new StringBuilder();
        builder.append(message);
        builder.append(" ");
        builder.append(lineNumber+1);
        return builder.toString();
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void handleOccurredError(String message, final boolean closeActivity) {
        mErrorOccurred = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.read_dialog_error_title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.read_dialog_error_no_storage_okbutton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (closeActivity) finish();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initiateDirectory() {
        mIsStorageReadable = isExternalStorageReadable();
        if (mIsStorageReadable) {
            Log.d(LOG_TAG, "External storage readable");
        } else {
            Log.e(LOG_TAG, "External storage is not available");
            handleOccurredError(getString(R.string.read_dialog_error_no_storage), true);
            return;
        }

        if (mReadFileTypeID == READ_SCHEDULE_FILE) {
            mDirectoryPath = Environment.getExternalStorageDirectory() + File.separator +
                    mDirectoryName + File.separator + mScheduleDirectoryName;
        } else if (mReadFileTypeID == READ_FREQUENCY_SCAN_FILE) {
            mDirectoryPath = Environment.getExternalStorageDirectory() + File.separator +
                    mDirectoryName + File.separator + mFrequencyScanDirectoryName;
        }

        File taskFileDirectory = new File(mDirectoryPath);
        mDirectoryAvailable = taskFileDirectory.exists() && taskFileDirectory.isDirectory();
        if (mDirectoryAvailable) {
            Log.d(LOG_TAG, "Directory exists");
        } else {
            Log.e(LOG_TAG, "No directory found");
            handleOccurredError(getString(R.string.read_dialog_error_no_folder), true);
            return;
        }

        mFileNames = getFileListInDirectory(mDirectoryPath);
    }

    private ArrayList<String> getFileListInDirectory(String directoryPath) {
        ArrayList<String> tFileList = new ArrayList<String>();
        File directory = new File(directoryPath);
        File[] filesInDirectory = directory.listFiles();

        for (int i = 0; i < filesInDirectory.length; i++) {
            File file = filesInDirectory[i];
            String filePath = file.getName();
            if (filePath.endsWith(".csv"))
                tFileList.add(filePath);
        }
        return tFileList;
    }

    private ArrayList<String[]> getContentFromFile(String fileName) {
        ArrayList<String[]> dataFromFile = new ArrayList<String[]>();
        String chosenFilePath = buildFullPath(fileName);
        CSVReader reader = null;

        try {
            reader = new CSVReader(new FileReader(chosenFilePath), ';');
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                dataFromFile.add(nextLine);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "File reader error occurred");
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "File close exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Null pointer exception occured");
                e.printStackTrace();
            }
        }
        return dataFromFile;
    }

    private String buildFullPath(String fileName) {
        StringBuilder filePathBuilder = new StringBuilder(mDirectoryPath);
        filePathBuilder.append("/");
        filePathBuilder.append(fileName);
        return filePathBuilder.toString();
    }
}