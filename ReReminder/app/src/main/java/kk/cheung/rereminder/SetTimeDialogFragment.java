package kk.cheung.rereminder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by Kakit on 02/01/2015.
 * Just a standard time picking dialog
 */
public class SetTimeDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(),(ActivityCreateTask) getActivity(),hour,min,
                DateFormat.is24HourFormat(getActivity()));
    }

}
