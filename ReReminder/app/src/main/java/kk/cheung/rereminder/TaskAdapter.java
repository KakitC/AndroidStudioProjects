package kk.cheung.rereminder;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/*
* Created by Kakit Cheung, 12/23/2014
*
* Acts as a bridge between the task dataset (a list), and the view renderer.
* Has methods for interacting with task list (get, del, sort, add) and saving/loading data
* Contains ViewHolder class, which acts as a handle for the various views
* that are in each task card.
*/
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private static List<Task> taskList;
    private static Context context;

    /*
    * Provides a reference to the views for each data item
    * Complex data items may need more than one view per item, and
    * you provide access to all the views for a data item in a view holder
    */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        public CardView taskCard;
        public TextView taskName;
        public TextView taskTimeDiff;
        public TextView taskRepeatTime;
        public TaskAdapter parentAdapter;

        //Constructor
        public ViewHolder(View v) {
            super(v);
            taskCard = (CardView) v;
            taskName = (TextView) v.findViewById(R.id.taskName);
            taskTimeDiff = (TextView) v.findViewById(R.id.taskTimeDiff);
            taskRepeatTime = (TextView) v.findViewById(R.id.taskRepeatTime);
        }

        //Refresh task, change setTime to now
        @Override
        public void onClick(View view) {
            taskList.get(getPosition()).setSetTime(Calendar.getInstance().getTimeInMillis());
            ((ActivityMain) parentAdapter.context).refreshView();
        }

        //Create a dialog
        @Override
        public boolean onLongClick(View view) {
            DialogFragment dialog = TaskOptionsDialogFragment.newInstance(
                    getPosition());
            dialog.show(((Activity) context).getFragmentManager(),"Task Options");
            return true;
        }
    } //end TaskAdapter.ViewHolder

    //CONSTRUCTOR
    public TaskAdapter(Context context, String[] taskDataArray) {
        List<Task> taskList = new LinkedList<>();
        // Save data string parser
        for (String td : taskDataArray) {
            String[] tokens = td.split("\\|");
            if (tokens.length == 4) { taskList.add(new Task(tokens)); }
        }

        TaskAdapter.context = context;
        TaskAdapter.taskList = taskList;
        sortTasks();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_card, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Rebuild the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Task thisTask = taskList.get(position);
        holder.parentAdapter = this;
        holder.taskCard.setOnClickListener(holder);
        holder.taskCard.setOnLongClickListener(holder);
        //Set task name
        holder.taskName.setText(thisTask.getName());
        //Set card color
        holder.taskCard.setCardBackgroundColor(thisTask.getColor());
        //Set repeat time
        holder.taskRepeatTime.setText(getFriendlyRepeatTime(thisTask.getRepeatTime()));

        //Set time difference text and color
        String timeDiffText;
        Date taskDueDate = new Date(thisTask.getSetTime()+thisTask.getRepeatTime());
        Date now = new Date();
        Date soon = new Date(now.getTime() + DateUtils.DAY_IN_MILLIS);
        timeDiffText = DateUtils.getRelativeTimeSpanString(
                taskDueDate.getTime()).toString();
        holder.taskTimeDiff.setText(timeDiffText);

        if (now.after(taskDueDate)) {
            holder.taskTimeDiff.setTextColor(context.getResources().
                    getColor(R.color.lateText));
        }
        else if (soon.after(taskDueDate)) {
            holder.taskTimeDiff.setTextColor(context.getResources().
                    getColor(R.color.soonText));
        }
        else {
            holder.taskTimeDiff.setTextColor(context.getResources().
                    getColor(R.color.lightText));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    public int getItemCount() {
        return taskList.size();
    }

    // Add a premade task to this adapter's list
    public void addTask(Task newTask) {
        taskList.add(newTask);
    }

    // Remove a task at pos from the list
    public void deleteTask(int pos) {
        taskList.remove(pos);
    }

    // Resort the task list
    public void sortTasks() {
        Collections.sort(taskList);
    }

    // get() a task at the position
    public Task getTask(int pos) {
        return taskList.get(pos);
    }

    //Creates a \ separated list of all tasks for saving data "<task>\<task>"
    //Basically a toString(), but only just the task list dataset
    //<taskdata>\<taskdata>\<taskdata>
    public String dataSave() {
        String saveString = "";

        if (!taskList.isEmpty()) {
            for (Task task : taskList) {
                saveString = saveString + task.toString() + "\\";
            }
            saveString = saveString.substring(0, saveString.length() - 1);
        }

        return saveString;
    }

    //From StackExchange http://stackoverflow.com/questions/635935/
    //      how-can-i-calculate-a-time-span-in-java-and-format-the-output
    //Prints millis time in normal words, grammar for taskRepeatTime
    //("Every x hours and y mins")
    //Minutes resolution output, second resolution input
    private static String getFriendlyRepeatTime(long timeDiff) {
        StringBuilder sb = new StringBuilder();
        sb.append("Every ");
        long diffInSeconds = timeDiff / 1000;
        long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long months = (diffInSeconds = (long) (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = diffInSeconds / 12;

        if (years > 0) {
            if (years == 1) {
                sb.append("year");
            } else {
                sb.append(years).append(" years");
            }
            if (years <= 6 && months > 0) {
                if (months == 1) {
                    sb.append(" and a month");
                } else {
                    sb.append(" and ").append(months).append(" months");
                }
            }
        } else if (months > 0) {
            if (months == 1) {
                sb.append("month");
            } else {
                sb.append(months).append(" months");
            }
            if (months <= 6 && days > 0) {
                if (days == 1) {
                    sb.append(" and a day");
                } else {
                    sb.append(" and ").append(days).append(" days");
                }
            }
        } else if (days > 0) {
            if (days == 1) {
                sb.append("day");
            } else {
                sb.append(days).append(" days");
            }
            if (days <= 3 && hrs > 0) {
                if (hrs == 1) {
                    sb.append(" and an hour");
                } else {
                    sb.append(" and ").append(hrs).append(" hours");
                }
            }
        } else if (hrs > 0) {
            if (hrs == 1) {
                sb.append("hour");
            } else {
                sb.append(hrs).append(" hours");
            }
            if (min > 1) {
                sb.append(" and ").append(min).append(" minutes");
            }
        } else {
            if (min == 1) {
                sb.append("minute");
            } else {
                sb.append(min).append(" minutes");
            }
        }
        return sb.toString();
    }
}

