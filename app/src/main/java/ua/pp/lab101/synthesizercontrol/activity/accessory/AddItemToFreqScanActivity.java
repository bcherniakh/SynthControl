package ua.pp.lab101.synthesizercontrol.activity.accessory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.FrequencyScanModeFragment;

public class AddItemToFreqScanActivity extends Activity {
    private static final String LOG_TAG = "Add Item to fs list";

    public static final String RUN_TYPE_ID = "run_type";
    public static final int ADD_RUN = 0;
    public static final int EDIT_RUN = 1;

    private EditText mFrequencyFromET;
    private EditText mFrequencyToTE;
    private EditText mFrequencyStepET;
    private EditText mTimeStepET;
    private Button mApplyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_to_freq_scan);
        mFrequencyFromET = (EditText) findViewById(R.id.frequencyFromEditText);
        mFrequencyToTE = (EditText) findViewById(R.id.frequencyToEditText);
        mFrequencyStepET = (EditText) findViewById(R.id.frequencySrepEditText);
        mTimeStepET = (EditText) findViewById(R.id.timeStepEditText);
        mApplyBtn = (Button) findViewById(R.id.addFreqScanApply);

        if (mApplyBtn != null) {
            mApplyBtn.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 buttonApplyPressed();
                                             }
                                         }
            );
        }
    }

    private void buttonApplyPressed() {
        double fromFrequency;
        double toFrequency;
        double frequencyStep;
        double timeStep;
        try {
            fromFrequency = Double.parseDouble(mFrequencyFromET.getText().toString());
            toFrequency = Double.parseDouble(mFrequencyToTE.getText().toString());
            frequencyStep = Double.parseDouble(mFrequencyStepET.getText().toString());
            timeStep = Double.parseDouble(mTimeStepET.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Parse exception occurred");
            showToast(getString(R.string.additem_freq_scan_msg_parse_error));
            return;
        }

        if (!checkAllValues(fromFrequency, toFrequency, frequencyStep, timeStep)) {
            Log.e(LOG_TAG, "Error in entered values");
            return;
        }

        Log.d(LOG_TAG, "All values are ok");
        fromFrequency = roundValue(fromFrequency);
        toFrequency = roundValue(toFrequency);
        frequencyStep = roundValue(frequencyStep);
        timeStep = roundValue(timeStep);
        int roundedTime = (int) (timeStep * 1000);

        Intent intent = new Intent();
        intent.putExtra(AddItemToFreqScanActivity.RUN_TYPE_ID, ADD_RUN);
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_FROM_FREQUENCY, fromFrequency);
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_TO_FREQUENCY, toFrequency);
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_FREQUENCY_STEP, frequencyStep);
        intent.putExtra(FrequencyScanModeFragment.ATTRIBUTE_TIME_STEP, roundedTime);
        setResult(RESULT_OK, intent);
        finish();
    }

    private double roundValue(double value) {
        return new BigDecimal(value).setScale(3, RoundingMode.DOWN).doubleValue();
    }

    private boolean checkAllValues(double fromFrequency, double toFrequency, double frequencyStep,
                                   double timeStep) {

        if (!checkFrequencyBoundaries(fromFrequency)) {
            showToast(getString(R.string.additem_freq_scan_toast_frequency_range));
            return false;
        }

        if (!checkFrequencyBoundaries(toFrequency)) {
            showToast(getString(R.string.additem_freq_scan_toast_frequency_range));
            return false;
        }

        if (!checkStepValue(frequencyStep)) {
            showToast(getString(R.string.additem_freq_scan_toast_step_incorrect));
            return false;
        }

        if (!checkTimeStepValue(timeStep)) {
            showToast(getString(R.string.additem_freq_scan_toast_time_incorrect));
            return false;
        }

//        if (!checkFromTo(fromFrequency, toFrequency)) {
//            showToast(getString(R.string.additem_freq_scan_toast_from_to));
//            return false;
//        }

        return true;
    }

    private boolean checkFromTo(double fromFrequency, double toFrequency) {
        if (fromFrequency >= toFrequency) return false;
        return true;
    }

    private boolean checkTimeStepValue(double timeStep) {
        if ((timeStep >= 0.01) && (timeStep <= 3600)) return true;
        return false;
    }

    private boolean checkStepValue(double frequencyStep) {
        if ((frequencyStep >= 0.001) && (frequencyStep <= 1000)) return true;
        return false;
    }

    private boolean checkFrequencyBoundaries(double frequency) {
        if ((frequency < 35) || (frequency > 4400)) return false;
        return true;
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}