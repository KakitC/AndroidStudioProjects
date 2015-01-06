package kk.cheung.rereminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.StringTokenizer;

/*
* Created by Kakit Cheung 12/18/2014
*
* Main activity for ReRepeater app
 */
public class ActivityMain extends Activity
        implements TaskOptionsDialogFragment.TaskOptionsDialogInterface {
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    protected final static int NEW_TASK_ACTIVITY_CODE = 1;
    protected final static int EDIT_TASK_ACTIVITY_CODE = 2;
    protected final static String SAVE_FILENAME = "rereminder_tasks.dat";

    protected final static String EDIT_BUNDLE_DATA = "kk.cheung.rereminder.EDIT_BUNDLE_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView)findViewById(R.id.taskList);
        mLayoutManager = new LinearLayoutManager(this); //vertical layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //Maybe file reading goes in onResume()
        //Log.d("ActivityMain","Starting activity");
        try {
            String savedString = getStringFromFile(SAVE_FILENAME);
            mAdapter = new TaskAdapter(this, savedString.split("\\\\"));
        } catch (FileNotFoundException e) {
            Log.d("ActivityMain", "No save file exists");
            //load default tasks
            mAdapter = new TaskAdapter(this, getResources().getStringArray(R.array.defaultTasks));
        } catch (Exception e) {
            Context context = getApplicationContext();
            CharSequence toastText = "There was an error in retrieving your data";
            Toast toast = Toast.makeText(context, toastText,Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
            Log.d("ActivityMain", "Error reading from saveFile, what did you dooooo");
            mAdapter = new TaskAdapter(this, getResources().getStringArray(R.array.defaultTasks));
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save data, maybe supposed to be in onStop()

        OutputStream saveFile = null;

        try {
            saveFile = new BufferedOutputStream(this.openFileOutput(SAVE_FILENAME,MODE_PRIVATE));
            String saveText = mAdapter.dataSave();
            //Log.d("ActivityMain","saveText: " + saveText);

            saveFile.write(saveText.getBytes());
            //Log.d("ActivityMain","Writing to file");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("ActivityMain", "Isn't it supposed to create a file if not found?");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ActivityMain", "Error writing to file?");
        } finally {
            try {
                if (saveFile != null) { saveFile.close(); }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ActivityMain","Seriously, it won't even close?");
            }
        }
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.newTaskButt:
                createNewTask();
                return true;
            case R.id.refreshButt:
                refreshView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Menu option Create Task
    //Starts new activity ActivityCreateTask, for getting results to create a new task
    public void createNewTask() {
        Intent newTaskIntent = new Intent(this, ActivityCreateTask.class);
        startActivityForResult(newTaskIntent,NEW_TASK_ACTIVITY_CODE);
    }


    public void refreshView() {
        //TODO animate refresh (flash white? load cards by animation?)
        mAdapter.notifyDataSetChanged();
    }

    //Delete a task at that position, refresh
    public void onDeleteClick(int pos) {
        mAdapter.deleteTask(pos);

        Context context = getApplicationContext();
        CharSequence toastText = "Task deleted";
        Toast toast = Toast.makeText(context, toastText,Toast.LENGTH_SHORT);
        toast.show();

        refreshView();
    }

    //Gets task data from position in the task list, and sends to ActivityCreateTask
    //to load into input fields
    public void onEditClick(int pos) {
        Task taskToEdit= mAdapter.getTask(pos);
        Intent editTaskIntent = new Intent(this, ActivityCreateTask.class);
        Bundle taskData = new Bundle();
        taskData.putString(ActivityCreateTask.NAME,taskToEdit.getName());
        taskData.putLong(ActivityCreateTask.REPEAT_TIME,taskToEdit.getRepeatTime());
        taskData.putLong(ActivityCreateTask.SET_TIME,taskToEdit.getSetTime());
        taskData.putInt(ActivityCreateTask.COLOR,taskToEdit.getColor());
        taskData.putInt(TaskOptionsDialogFragment.POS_KEY, pos);

        editTaskIntent.putExtra(EDIT_BUNDLE_DATA,taskData);
        startActivityForResult(editTaskIntent,EDIT_TASK_ACTIVITY_CODE);
    }

    //Handle return information from ActivityCreateTask, and add to taskAdapter dataset
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Edit existing task
        //Probably can edit in place, everything's by reference
        if (requestCode == EDIT_TASK_ACTIVITY_CODE && resultCode == RESULT_OK) {
            Bundle returnData = data.getBundleExtra(ActivityCreateTask.NEW_BUNDLE_DATA);
            Task oldTask = mAdapter.getTask(returnData.getInt(TaskOptionsDialogFragment.POS_KEY));

            oldTask.setName(returnData.getString(ActivityCreateTask.NAME));
            oldTask.setRepeatTime(returnData.getLong(ActivityCreateTask.REPEAT_TIME));
            oldTask.setSetTime(returnData.getLong(ActivityCreateTask.SET_TIME));
            oldTask.setColor(returnData.getInt(ActivityCreateTask.COLOR));

            //mAdapter.addTask(oldTask);
            mAdapter.notifyDataSetChanged();
        }

        //Create a new task, add it to the list
        if (requestCode == NEW_TASK_ACTIVITY_CODE && resultCode == RESULT_OK) {
            Task newTask = new Task();
            Bundle returnData = data.getBundleExtra(ActivityCreateTask.NEW_BUNDLE_DATA);
            newTask.setName(returnData.getString(ActivityCreateTask.NAME));
            newTask.setRepeatTime(returnData.getLong(ActivityCreateTask.REPEAT_TIME));
            newTask.setSetTime(returnData.getLong(ActivityCreateTask.SET_TIME));
            newTask.setColor(returnData.getInt(ActivityCreateTask.COLOR));

            mAdapter.addTask(newTask);
            refreshView();
        }
    }

    //Turn longass save file text string into an array of task strings
    //<taskdata>\<taskdata>\<taskdata>
    private String[] saveStringToArray(String saveString) {
        StringTokenizer st = new StringTokenizer(saveString,"\\");
        String[] saveData = new String[st.countTokens()];

        for (int i = 0; st.hasMoreTokens(); i++) {
            saveData[i] = st.nextToken();
            //Log.d("ActivityMain", saveData[i]);
        }

        return saveData;
    }

    //From http://stackoverflow.com/questions/12910503/read-file-as-string
    //Reads a whole file stream into a string
    public String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    //Reads from a saved file to a single string
    public String getStringFromFile (String filePath) throws Exception {
        FileInputStream fin = openFileInput(filePath);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
}

