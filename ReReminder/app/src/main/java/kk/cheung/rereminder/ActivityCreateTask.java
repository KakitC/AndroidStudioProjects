package kk.cheung.rereminder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/*
* Created by Kakit Cheung 12/23/2014
*
* Activity for inputting information to create a new Task, or edit an existing Task
*/
public class ActivityCreateTask extends Activity implements AdapterView.OnItemSelectedListener,
                                                            TimePickerDialog.OnTimeSetListener,
                                                            DatePickerDialog.OnDateSetListener {
    private String name;
    private long repeatTime;
    private long setTime = 0;
    private int color;
    private int pos = 0;

    private Calendar setTimeCal;

    private static int[] repeatTimeIds = {
            R.id.yearField,
            R.id.monthField,
            R.id.dayField,
            R.id.hourField,
            R.id.minField };
    private EditText[] repeatTimeFields = new EditText[repeatTimeIds.length];
    private static int[] colorIds = {
            R.color.cardWhite,
            R.color.cardGrey,
            R.color.cardRed,
            R.color.cardYellow,
            R.color.cardGreen,
            R.color.cardTeal,
            R.color.cardBlue,
            R.color.cardMagenta };
    private int[] colors = new int[colorIds.length];

    //return bundle string keys
    protected final static String NEW_BUNDLE_DATA = "kk.cheung.rereminder.NEW_BUNDLE_DATA";
    protected final static String NAME = "name";
    protected final static String REPEAT_TIME = "repeatTime";
    protected final static String SET_TIME = "setTime";
    protected final static String COLOR = "color";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        for (int i = 0; i < 5; i++) {
            repeatTimeFields[i] = (EditText) findViewById(repeatTimeIds[i]);
        }

        Spinner colorSpinner = (Spinner) findViewById(R.id.TaskColorSpinner);
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,
                R.array.TaskSpinnerColors, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
        colorSpinner.setOnItemSelectedListener(this);


        Bundle editData = getIntent().getBundleExtra(ActivityMain.EDIT_BUNDLE_DATA);
        if (editData != null) {
            name = editData.getString(NAME);
            repeatTime = editData.getLong(REPEAT_TIME);
            setTime = editData.getLong(SET_TIME);
            color = editData.getInt(COLOR);
            pos = editData.getInt(TaskOptionsDialogFragment.POS_KEY);

            ((EditText) findViewById(R.id.TaskNameField)).setText(name);

            //load repeatTime
            long diffInSeconds = repeatTime / 1000;
            long[] times = new long[repeatTimeFields.length];
            times[4] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
            times[3] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
            times[2] = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
            times[1] = (diffInSeconds = (long)(diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
            times[0] = diffInSeconds / 12;

            for (int i = 0; i < repeatTimeFields.length; i++) {
                repeatTimeFields[i].setText(Long.toString(times[i]));
            }

            //load setTime
            setTimeCal = GregorianCalendar.getInstance();
            setTimeCal.setTimeInMillis(setTime + repeatTime); //First reminder time, not when set
            setSetDateText(setTimeCal);
            setSetTimeText(setTimeCal);

            //Load color
            for (int i = 0; i < colorIds.length ; i++) {
                colors[i] =  getResources().getColor(colorIds[i]);
                if (color == colors[i]) {
                    colorSpinner.setSelection(i);
                    findViewById(R.id.editTaskActivity).setBackgroundColor(color);
                }
            }
        } //end EDIT_BUNDLE_DATA case, finished loading existing task data
        else { //Default new task data
            //Every day default
            for (EditText field : repeatTimeFields) {
                field.setText("0");
            }
            /*for (int i = 0; i < repeatTimeFields.length; i++) {
                repeatTimeFields[i].setText("0");
            }*/
            repeatTimeFields[2].setText("1");

            setTimeCal = GregorianCalendar.getInstance();
            setSetTimeText(setTimeCal);
            setSetDateText(setTimeCal);

            color = getResources().getColor(R.color.cardWhite); //default to white
            findViewById(R.id.editTaskActivity).setBackgroundColor(color);
        }
    }

    //Save input fields to a bundle and send it back to main activity
    public void createTask(View view) {

        name = ((TextView) findViewById(R.id.TaskNameField)).getText().toString();

        //Calculate repeat time
        try {
            int[] times = new int[repeatTimeFields.length];

            for (int i = 0; i < repeatTimeFields.length; i++) {
                String field = repeatTimeFields[i].getText().toString();
                if (field.equals("")) { //Input field left empty check
                    field = "0";
                }
                times[i] = Integer.valueOf(field);
            }

            //TODO Known bug: Anything above 1 year adds 5 days every time the task is saved because years are not 12 30day months
            repeatTime = times[0]*365*DateUtils.DAY_IN_MILLIS + (long) times[1]*30*DateUtils.DAY_IN_MILLIS +
                    times[2]*DateUtils.DAY_IN_MILLIS + times[3]*DateUtils.HOUR_IN_MILLIS +
                    times[4]*60*1000 + 1000;
            if (repeatTime == 1000) {
                Context context = getApplicationContext();
                CharSequence toastText = "Please put a repeating time period";
                Toast toast = Toast.makeText(context, toastText,Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        } catch (NumberFormatException e) {
            Context context = getApplicationContext();
            CharSequence toastText = "Lol what did you do, put in real numbers";
            Toast toast = Toast.makeText(context, toastText,Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        setTime = setTimeCal.getTimeInMillis()-repeatTime;

        //Color handled on creation or declared by spinner

        //Return intent with task creation data
        Intent returnIntent = new Intent();
        Bundle returnData = new Bundle();
        returnData.putString(NAME, name);
        returnData.putLong(REPEAT_TIME, repeatTime);
        returnData.putLong(SET_TIME, setTime);
        returnData.putInt(COLOR, color);
        returnData.putInt(TaskOptionsDialogFragment.POS_KEY, pos);
        returnIntent.putExtra(NEW_BUNDLE_DATA,returnData);

        setResult(RESULT_OK, returnIntent);
        finish();
    }

    //Set task color to selected, and change activity background to match
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String selection = parent.getItemAtPosition(pos).toString();
        switch (selection) {
            case "White":
                color = getResources().getColor(R.color.cardWhite);
                break;
            case "Grey":
                color = getResources().getColor(R.color.cardGrey);
                break;
            case "Red":
                color = getResources().getColor(R.color.cardRed);
                break;
            case "Yellow":
                color = getResources().getColor(R.color.cardYellow);
                break;
            case "Green":
                color = getResources().getColor(R.color.cardGreen);
                break;
            case "Teal":
                color = getResources().getColor(R.color.cardTeal);
                break;
            case "Blue":
                color = getResources().getColor(R.color.cardBlue);
                break;
            case "Magenta":
                color = getResources().getColor(R.color.cardMagenta);
                break;
            default:
                Context context = getApplicationContext();
                CharSequence toastText = "lol what did you press";
                Toast toast = Toast.makeText(context, toastText,Toast.LENGTH_SHORT);
                toast.show();
                Log.d("ActivityCreateTask","Wrong color spinner switch case?");
                break;
        }
        findViewById(R.id.editTaskActivity).setBackgroundColor(color);
    }

    //lol do nothing
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    //No options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    //TODO implement time and date picker dialog fragments showing existing setTime
    //send info through a bundle?
    public void showSetTimePickerDialog(View v) {
        DialogFragment setTimeFrag = new SetTimeDialogFragment();
        setTimeFrag.show(getFragmentManager(),"setTimePicker");
    }
    public void showSetDatePickerDialog(View v) {
        DialogFragment setDateFrag = new SetDateDialogFragment();
        setDateFrag.show(getFragmentManager(),"setDatePicker");
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        setTimeCal.set(Calendar.HOUR_OF_DAY,hour);
        setTimeCal.set(Calendar.MINUTE,minute);

        setSetTimeText(setTimeCal);
    }

    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        setTimeCal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        setTimeCal.set(Calendar.MONTH,monthOfYear);
        setTimeCal.set(Calendar.YEAR,year);

        setSetDateText(setTimeCal);
    }

    public void setSetDateText(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        //var = if ? then : else;
        String dayString =  day < 10 ? "0" + String.valueOf(day) : String.valueOf(day);
        String monthString = month < 10 ? "0" + String.valueOf(month) : String.valueOf(month);

        String setDateText = dayString + "/" + monthString + "/" + String.valueOf(year);
        ((TextView) findViewById(R.id.setSetDateButt)).setText(setDateText);
    }

    public void setSetTimeText(Calendar cal) {
        int min = cal.get(Calendar.MINUTE);
        String minString =  min < 10 ? "0" + String.valueOf(min) : String.valueOf(min);

        //24Hr format
        if (DateFormat.is24HourFormat(this)) {
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            String hourString = hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour);
            String timeText = hourString + ":" + minString;
            ((TextView) findViewById(R.id.setSetTimeButt)).setText(timeText);
        }
        //AM PM format
        else {
            int hour = cal.get(Calendar.HOUR);
            String timeText;
            if (cal.get(Calendar.AM_PM) == Calendar.AM) {
                timeText = String.valueOf(hour) + ":" + minString + " AM";
            }
            else {
                timeText = String.valueOf(hour) + ":" + minString + " PM";
            }
            ((TextView) findViewById(R.id.setSetTimeButt)).setText(timeText);
        }
    }
}
