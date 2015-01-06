package kk.cheung.rereminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by Kakit Cheung on 01/01/2015.
 * Dialog for long clicking on a task, presents options to do to that task
 */
public class TaskOptionsDialogFragment extends DialogFragment {

    private TaskOptionsDialogInterface listener;
    public static String POS_KEY = "pos";

    //Interface defines click behavior
    public interface TaskOptionsDialogInterface {
        public void onEditClick(int pos);
        public void onDeleteClick(int pos);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Hope it's just TaskAdapter calling this dialog with ActivityMain, no exception handling
        listener = (TaskOptionsDialogInterface) activity;
    }

    public static TaskOptionsDialogFragment newInstance(int pos) {
        TaskOptionsDialogFragment frag = new TaskOptionsDialogFragment();

        Bundle arg = new Bundle();
        arg.putInt(POS_KEY,pos);
        frag.setArguments(arg);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int pos = getArguments().getInt(POS_KEY);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setItems(R.array.taskOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    listener.onEditClick(pos);
                }
                else if (which == 1) {
                    listener.onDeleteClick(pos);
                }
                else {
                    CharSequence toastText = "Lol what did you press";
                    Toast toast = Toast.makeText(getActivity(), toastText,Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        return builder.create();
    }
}
