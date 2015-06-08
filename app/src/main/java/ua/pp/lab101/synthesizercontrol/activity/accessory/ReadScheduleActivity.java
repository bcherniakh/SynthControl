package ua.pp.lab101.synthesizercontrol.activity.accessory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;


import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.SchedulerModeFragment;

public class ReadScheduleActivity extends Activity implements View.OnClickListener {
    private static final String LOG_TAG = "ReadAct";
    private Button mOpenBtn = null;
    private ListView mFilesListView;
    private boolean mIsStorageReadable = false;
    private boolean mErrorOccurred = false;

    private boolean mDirectoryAvailable = false;
    private static final String mDirectoryName = "SynthControl";
    private String mDirectoryPath;
    private ArrayList<String> mFileNames = new ArrayList<>();

    private double[] mFrequency;
    private int[] mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_schedule);
        mOpenBtn = (Button) findViewById(R.id.openFileBtn);
        mOpenBtn.setOnClickListener(this);
        mFilesListView = (ListView) findViewById(R.id.FileNameslistView);
    }

    @Override
    public void onStart() {
        super.onStart();
        initiateDirectory();
        mFilesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, mFileNames);
        mFilesListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onResume();
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

        Intent intent = new Intent();
        intent.putExtra(SchedulerModeFragment.ATTRIBUTE_TIME_ARRAY, mTime);
        intent.putExtra(SchedulerModeFragment.ATTRIBUTE_FREQUENCY_ARRAY, mFrequency);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void parseContent(ArrayList<String[]> fileContent) {
        if (fileContent.isEmpty()) {
            Log.d(LOG_TAG, "File content is empty");
            notifyAboutError(getString(R.string.read_dialog_error_file_empty), false);
            return;
        }

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
                notifyAboutError(errorMessage, false);
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
                notifyAboutError(errorMessage, false);
                return;
            }
            if (!checkFrequency(frequency)) {
                Log.d(LOG_TAG, "Frequency incorrect");
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_frequency_range), i);
                notifyAboutError(errorMessage, false);
                return;
            }

            if (!checkTime(hours, minutes, seconds)) {
                Log.d(LOG_TAG, "Time is incorrect");
                String errorMessage = builtDataErrorMessage(getString(R.string.read_dialog_error_time_range), i);
                notifyAboutError(errorMessage, false);
                return;
            }

            mFrequency[i] = new BigDecimal(frequency).setScale(3, RoundingMode.UP).doubleValue();
            mTime[i] = getTimeInSeconds(hours, minutes, seconds);
        }
    }

    private boolean checkTime(int hours, int minutes, int seconds) {
        if (hours < 0 || minutes < 0 || seconds < 0) return  false;
        int timeInSeconds = getTimeInSeconds(hours, minutes, seconds);
        if (timeInSeconds > 6 * 3600) return false;
        return true;
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

    private void initiateDirectory() {
        mIsStorageReadable = isExternalStorageReadable();
        if (mIsStorageReadable) {
            Log.d(LOG_TAG, "External storage readable");
        } else {
            Log.d(LOG_TAG, "External storage is not available");
            notifyAboutError(getString(R.string.read_dialog_error_no_storage), true);
            return;
        }

        mDirectoryPath = Environment.getExternalStorageDirectory() + File.separator + mDirectoryName;
        File taskFileDirectory = new File(mDirectoryPath);

        mDirectoryAvailable = taskFileDirectory.exists() && taskFileDirectory.isDirectory();
        if (mDirectoryAvailable) {
            Log.d(LOG_TAG, "Directory exists");
        } else {
            Log.d(LOG_TAG, "No directory found");
            notifyAboutError(getString(R.string.read_dialog_error_no_folder), true);
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