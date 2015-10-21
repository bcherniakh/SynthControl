package ua.pp.lab101.synthesizercontrol.activity.accessory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.FrequencyScanModeFragment;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.SchedulerModeFragment;

public class WriteDataToCSVFileActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String WRITE_FILE_TYPE_ID = "write_file_type";
    public static final int WRITE_SCHEDULE_FILE = 0;
    public static final int WRITE_FREQUENCY_SCAN_FILE = 1;

    private static final String LOG_TAG = "WriteAct";

    private Button mWriteBtn = null;
    private EditText mFileName = null;
    private TextView mExplanationMainnTextView = null;
    private TextView mExplanationDirectoryTextView = null;

    private int mWriteTypeID;
    private String mDirectoryPath = null;
    private ArrayList<String> mFileNames = new ArrayList<>();
    private final String mDirectoryName = "SynthControl";
    private final String mScheduleDirectoryName = "Schedule";
    private final String mFrequencyScanDirectoryName = "FrequencyScan";

    private boolean mErrorOccurred = false;
    private String[] mFrequencyValues = null;
    private String[] mTimeValues = null;

    private String[] mFromFrequency = null;
    private String[] mToFrequency = null;
    private String[] mFrequencyStep = null;
    private String[] mTimeStep = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_csv_file);
        mWriteBtn = (Button) findViewById(R.id.writeFileApplyBtn);
        mWriteBtn.setOnClickListener(this);
        mFileName = (EditText) findViewById(R.id.writeFileFileNameEt);
        mExplanationMainnTextView = (TextView) findViewById(R.id.writeFileMainTitleText);
        mExplanationDirectoryTextView = (TextView) findViewById(R.id.writeFileExplanationText);
        Intent intent = getIntent();
        mWriteTypeID = intent.getIntExtra(WRITE_FILE_TYPE_ID, WRITE_SCHEDULE_FILE);

        if (mWriteTypeID == WRITE_SCHEDULE_FILE) {
            mExplanationMainnTextView.setText(R.string.write_schedule_explanation_title);
            mExplanationDirectoryTextView.setText(R.string.write_schedule_explanation_directory);
        } else if (mWriteTypeID == WRITE_FREQUENCY_SCAN_FILE){
            mExplanationMainnTextView.setText(R.string.write_freq_scan_explanation_title);
            mExplanationDirectoryTextView.setText(R.string.write_freq_scan_explanation_directory);
        }

        getDataFromIntent(intent);
        }



    @Override
    public void onStart() {
        super.onStart();
        initiateDirectory();
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
        writeData(false);
    }

    private void getDataFromIntent(Intent intent) {
        if (mWriteTypeID == WRITE_SCHEDULE_FILE) {
            mFrequencyValues = intent.getStringArrayExtra(SchedulerModeFragment.ATTRIBUTE_FREQUENCY_ARRAY);
            mTimeValues = intent.getStringArrayExtra(SchedulerModeFragment.ATTRIBUTE_TIME_ARRAY);

            if (mFrequencyValues == null || mTimeValues == null) {
                Log.d(LOG_TAG, "An error occurred. No data in array");
                notifyAboutError(getString(R.string.write_dialog_error_zero_array), true);
            }

            if (mFrequencyValues.length == 0 || mTimeValues.length == 0) {
                Log.d(LOG_TAG, "An error occurred. No data in array");
                notifyAboutError(getString(R.string.write_dialog_error_zero_array), true);
            }
        } else if (mWriteTypeID == WRITE_FREQUENCY_SCAN_FILE) {
            mFromFrequency = intent.getStringArrayExtra(FrequencyScanModeFragment.ATTRIBUTE_FROM_FREQUENCY_ARRAY);
            mToFrequency = intent.getStringArrayExtra(FrequencyScanModeFragment.ATTRIBUTE_TO_FREQUENCY_ARRAY);
            mFrequencyStep = intent.getStringArrayExtra(FrequencyScanModeFragment.ATTRIBUTE_FREQUENCY_STEP_ARRAY);
            mTimeStep = intent.getStringArrayExtra(FrequencyScanModeFragment.ATTRIBUTE_TIME_STEP_ARRAY);

            if (mFromFrequency == null ||mToFrequency == null || mFrequencyStep == null ||
                    mTimeStep == null) {
                Log.d(LOG_TAG, "An error occurred. No data in array");
                notifyAboutError(getString(R.string.write_dialog_error_zero_array), true);
            }

            if (mFromFrequency.length == 0 ||mToFrequency.length == 0 || mFrequencyStep.length ==0 ||
                    mTimeStep.length == 0) {
                Log.d(LOG_TAG, "An error occurred. No data in array");
                notifyAboutError(getString(R.string.write_dialog_error_zero_array), true);
            }
         }
    }

    private void writeData(boolean overwrite) {
        String fileName = mFileName.getText().toString();
        if (fileName.isEmpty()) {
            showToast(getString(R.string.write_toast_empty_file_name));
            return;
        }

        fileName = fileName.concat(".csv");

        if(checkIsFileExists(fileName)) {
            if (!overwrite) {
                showOverwriteFileDialog(fileName);
                return;
            } else {
                showToast(getString(R.string.write_toast_file_overwritten));
            }
        }

        String filePath = makeFullPath(fileName);
        boolean result = writeFile(filePath);
        if (result) {
            String message = getString(R.string.write_toast_file_wrote_begin) + " " +
                    fileName + " " + getString(R.string.write_toast_file_wrote_end);
            showToast(message);
            finish();
        }
    }

    private boolean writeFile(String filePath) {
        List<String[]> data = getData();
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath), ';');
            writer.writeAll(data, false);
            writer.close();
            Log.d(LOG_TAG, "Wrote successfully");
            return true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Write error occurred");
            notifyAboutError(getString(R.string.write_dialog_error_io_error), true);
            e.printStackTrace();
        }
        return false;
    }

    private List<String[]> getData() {

        if (mWriteTypeID == WRITE_SCHEDULE_FILE) {
            return getScheduleData();
        } else if (mWriteTypeID == WRITE_FREQUENCY_SCAN_FILE) {
            return getFrequencyScanData();
        }
        return null;
    }

    private List<String[]> getScheduleData() {
        List<String[]> data = new LinkedList<>();
        for (int i = 0; i < mFrequencyValues.length; i++) {
            data.add(createNewScheduleLine(i));
        }
        return data;
    }

    private List<String[]> getFrequencyScanData() {
        List<String[]> data = new LinkedList<>();
        for (int i = 0; i < mFromFrequency.length; i++) {
            data.add(createNewFrequencyScanLine(i));
        }
        return data;
    }

    private String[] createNewScheduleLine(int i) {
        String[] newLine = new String[4];
        newLine[0] = mFrequencyValues[i];
        newLine[1] = getHoursFromTime(i);
        newLine[2] = getMinutesFromTime(i);
        newLine[3] = getSecondsFromTime(i);
        return  newLine;
    }

    private String[] createNewFrequencyScanLine(int i) {
        String[] newLine = new String[4];
        newLine[0] = mFromFrequency[i];
        newLine[1] = mToFrequency[i];
        newLine[2] = mFrequencyStep[i];
        newLine[3] = mTimeStep[i];
        return  newLine;
    }

    private String getSecondsFromTime(int i) {
        int seconds = 0;
        int timeInSeconds = Integer.parseInt(mTimeValues[i]);
        seconds = (timeInSeconds % 3600) % 60;
        return String.valueOf(seconds);
    }

    private String getMinutesFromTime(int i) {
        int minutes = 0;
        int timeInSeconds = Integer.parseInt(mTimeValues[i]);
        minutes = (timeInSeconds % 3600) / 60;
        return String.valueOf(minutes);
    }

    private String getHoursFromTime(int i) {
        int hours = 0;
        int timeInSeconds = Integer.parseInt(mTimeValues[i]);
        hours = timeInSeconds / 3600;
        return String.valueOf(hours);
    }

    private boolean checkIsFileExists(String fileName) {
        for (String fileInDirectory : mFileNames) {
            if (fileName.equals(fileInDirectory)) {
                return true;
            }
        }
        return false;
    }

    private String makeFullPath(String fileName) {
        StringBuilder builder = new StringBuilder();
        builder.append(mDirectoryPath);
        builder.append(File.separator);
        builder.append(fileName);
        return builder.toString();
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void notifyAboutError(String message, final boolean closeActivity) {
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

    private void showOverwriteFileDialog(String filename) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.write_dialog_over_title);
        builder.setMessage(getString(R.string.write_dialog_over_message_begin) + " " +
                filename + " " + getString(R.string.write_dialog_over_message_end));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.write_dialog_over_btn_positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        writeData(true);
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(R.string.write_dialog_over_btn_negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initiateDirectory() {
        boolean isStorageWritable = isExternalStorageWritable();
        if (isStorageWritable) {
            Log.d(LOG_TAG, "External storage writable");
        } else {
            Log.d(LOG_TAG, "External storage is not writable");
            notifyAboutError(getString(R.string.write_dialog_error_no_storage), true);
            return;
        }

        if (mWriteTypeID == WRITE_SCHEDULE_FILE) {
            mDirectoryPath = Environment.getExternalStorageDirectory() + File.separator +
                    mDirectoryName + File.separator + mScheduleDirectoryName;
        } else if (mWriteTypeID == WRITE_FREQUENCY_SCAN_FILE) {
            mDirectoryPath = Environment.getExternalStorageDirectory() + File.separator +
                    mDirectoryName + File.separator + mFrequencyScanDirectoryName;
        }

        File taskFileDirectory = new File(mDirectoryPath);
        boolean directoryAvailable = taskFileDirectory.exists() && taskFileDirectory.isDirectory();
        if (directoryAvailable) {
            Log.d(LOG_TAG, "Directory exists");
        } else {
            Log.d(LOG_TAG, "No directory found");
            File directory = new File(Environment.getExternalStorageDirectory()+File.separator+mDirectoryName);
            directory.mkdirs();
            if (mWriteTypeID == WRITE_SCHEDULE_FILE) {
                File schedulerDirectory = new File(Environment.getExternalStorageDirectory()+File                           .separator+mDirectoryName + File.separator + mScheduleDirectoryName);
                schedulerDirectory.mkdirs();
            } else if (mWriteTypeID == WRITE_FREQUENCY_SCAN_FILE) {
                File frequencyScanDirectory = new File(Environment.getExternalStorageDirectory()+File                           .separator+mDirectoryName+ File.separator + mFrequencyScanDirectoryName);
                frequencyScanDirectory.mkdirs();
            }
            notifyAboutError(getString(R.string.write_dialog_error_no_folder), false);
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

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}