package kk.cheung.rereminder;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by Kakit on 26/12/2014.
 * Individual task object
 * - sortable in a list
 * - has a bunch of fields for ReRepeater tasks, all available to get/set
 *
 */
public class Task implements Comparable<Task> {
    private String name;
    private long setTime;
    private long repeatTime;
    private int color;

    //CONSTRUCTORS
    //Task with default information, never really used
    public Task() {
        name = "New Task";
        setTime = (new Date()).getTime();
        repeatTime = DateUtils.DAY_IN_MILLIS;
        color = Color.WHITE;
    }

    //Clone constructor?
    public Task(Task task) {
        name = task.getName();
        setTime = task.getSetTime();
        repeatTime = task.getRepeatTime();
        color = task.getColor();
    }

    //Constructor parse from a string
    //"Task Name|<setTime in millis>|<repeatTime in millis>|<Background color int>"
    public Task(String[] taskData) {

        name = taskData[0];
        setTime = Long.valueOf(taskData[1]);
        repeatTime = Long.valueOf(taskData[2]);
        color = Integer.valueOf(taskData[3]);
    }

    //Returns negative int if this < another, positive int if this > another
    //Greater defined by when setTime+repeatTime is (lower/older is more urgent/higher)
    @Override
    public int compareTo(Task other) {
        if (setTime+repeatTime > other.getSetTime() +
                other.getRepeatTime()) {
            return 1;
        }
        else if (setTime+repeatTime < other.getSetTime() +
                other.getRepeatTime()) {
            return -1;
        }
        else return 0;
    }

    // Turn task into string for saving
    //"Task Name|<setTime in millis>|<repeatTime in millis>|<Background color int>"
    @Override
    public String toString() {
        String l = name + "|" + Long.toString(setTime) + "|" + Long.toString(repeatTime)
                + "|" + Integer.toString(color);
        return l;
    }

    //GETTER AND SETTER FUNCTIONS FOR EVERYONE!!!
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getSetTime() {
        return setTime;
    }
    public void setSetTime(Long setTime) {
        this.setTime = setTime;
    }
    public Long getRepeatTime() {
        return repeatTime;
    }
    public void setRepeatTime(Long repeatTime) {
        this.repeatTime = repeatTime;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
}
