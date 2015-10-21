package ua.pp.lab101.synthesizercontrol.activity.accessory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.SchedulerModeFragment;

public class AddItemToScheduleActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String RUN_TYPE_ID = "run_type";
    public static final int ADD_RUN = 0;
    public static final int EDIT_RUN = 1;
    private static final String LOG_TAG = "AddItem";
    private Button mApplyBtn = null;
    private EditText mFrequencyValueET = null;
    private EditText mSecondsValueET = null;
    private EditText mMinutesValueET = null;
    private EditText mHoursValueET = null;
    private double mFrequencyValue = 0;
    private int mTimeInSecondsValue = 0;
    private int mRunType;

    private String mSavedData = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_to_schedule);
        mApplyBtn = (Button) findViewById(R.id.applyBtn);
        mFrequencyValueET = (EditText) findViewById(R.id.frequencyValue);
        mSecondsValueET = (EditText) findViewById(R.id.timeValueSeconds);
        mMinutesValueET = (EditText) findViewById(R.id.timeValueMinutes);
        mHoursValueET = (EditText) findViewById(R.id.timeValueHours);
        mApplyBtn.setOnClickListener(this);
        Intent intent = getIntent();
        mRunType = intent.getIntExtra(RUN_TYPE_ID, ADD_RUN);
        Log.d("Run type is: " + LOG_TAG, String.valueOf(mRunType));
        if (mRunType == EDIT_RUN) {
            int hours = 0;
            int minutes = 0;
            int seconds = 0;
            mFrequencyValue = intent.getDoubleExtra(SchedulerModeFragment.ATTRIBUTE_FREQUENCY, 0.0);
            mTimeInSecondsValue = intent.getIntExtra(SchedulerModeFragment.ATTRIBUTE_TIME, 0);
            hours = mTimeInSecondsValue / 3600;
            minutes = (mTimeInSecondsValue % 3600) / 60;
            seconds = (mTimeInSecondsValue % 3600) % 60;
            mFrequencyValueET.setText(String.valueOf(mFrequencyValue));
            mHoursValueET.setText(String.valueOf(hours));
            mMinutesValueET.setText(String.valueOf(minutes));
            mSecondsValueET.setText(String.valueOf(seconds));
        } else {
            mFrequencyValueET.setText("0");
            mHoursValueET.setText("0");
            mMinutesValueET.setText("0");
            mSecondsValueET.setText("0");
        }

        setFocusListener(mFrequencyValueET);
        setFocusListener(mSecondsValueET);
        setFocusListener(mMinutesValueET);
        setFocusListener(mHoursValueET);

        setOnClickListener(mFrequencyValueET);
        setOnClickListener(mSecondsValueET);
        setOnClickListener(mMinutesValueET);
        setOnClickListener(mHoursValueET);
    }

    private void setFocusListener(final EditText editText) {
        if (editText == null) return;
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (editText.isFocused()) {
                    mSavedData = editText.getText().toString();
                    editText.getText().clear();
                } else {
                    if (editText.getText().toString().isEmpty()) {
                        editText.setText(mSavedData);
                    }
                }
            }
        });
    }

    private void setOnClickListener (final EditText editText) {
        if (editText == null) return;

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.clearFocus();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item_to_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_stop_service) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        String frequencyValueText = mFrequencyValueET.getText().toString();
        String secondsValueText = mSecondsValueET.getText().toString();
        String minutesValueText = mMinutesValueET.getText().toString();
        String hoursValueText = mHoursValueET.getText().toString();
        try {
            int seconds = 0;
            int minutes = 0;
            int hours = 0;
            seconds = Integer.parseInt(secondsValueText);
            minutes = 60 * Integer.parseInt(minutesValueText);
            hours = 3600 * Integer.parseInt(hoursValueText);
            mFrequencyValue = Double.parseDouble(frequencyValueText);
            mTimeInSecondsValue = seconds + minutes + hours;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Parse exception occurred");
            showToast(getString(R.string.additem_msg_parse_error));
            return;
        }

        if (mFrequencyValue < 35 || mFrequencyValue > 4400) {
            showToast(getString(R.string.additem_msg_frequency_incorrect));
            return;
        }

        if (mTimeInSecondsValue < 1 || mTimeInSecondsValue > 6*3600) {
            showToast(getString(R.string.additem_msg_time_incorrect));
            return;
        }

        mFrequencyValue = new BigDecimal(mFrequencyValue).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
        intent.putExtra(AddItemToScheduleActivity.RUN_TYPE_ID, mRunType);
        intent.putExtra(SchedulerModeFragment.ATTRIBUTE_TIME, mTimeInSecondsValue);
        intent.putExtra(SchedulerModeFragment.ATTRIBUTE_FREQUENCY, mFrequencyValue);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
