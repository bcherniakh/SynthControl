package ua.pp.lab101.synthesizercontrol.service.task;

import java.util.LinkedHashMap;

/**
 * Task is class that presents the current mConstantFrequency in MHz and time of action in seconds
 * Created by ashram on 4/23/15.
 *
 */

public class Task {
    private TaskType mTaskType;
    private double mConstantFrequency;
    private LinkedHashMap<Integer, Double> mScheduleTask;
    private double mStartFrequency;
    private double mFinishFrequency;
    private int mFrequencyStep;
    private int mTimeStep;
    private boolean mIsCycled;


    /**
     * Constructor for creation constant mode task. It takes one parameter - frequency value.
     * @param constantFrequency is constant frequency value in Mhz. Should not be negative
     */
    public Task(double constantFrequency) {
        mTaskType = TaskType.CONSTANT_FREQUENCY_MODE;
        if (constantFrequency <= 0) throw new IllegalArgumentException();
        this.mConstantFrequency = constantFrequency;
    }

    /**
     * Constructor for creation schedule mode task. It takes a LinkedHashMap as parameter.
     * @param scheduleTask is LinkedHashMap contains the time in seconds
     *                     (as integer) and frequency in MHz (as double). Could not be null.
     * @param isCycled is flag that tells whether the task should be cycled.
     */
    public Task(LinkedHashMap<Integer, Double> scheduleTask, boolean isCycled) {
        if (scheduleTask == null) throw new IllegalArgumentException("task could not be null");
        mTaskType = TaskType.SCHEDULE_MODE;
        this.mScheduleTask = scheduleTask;
        this.mIsCycled = isCycled;
    }

    /**
     * Constructor for creation frequency scan task.
     * @param startFrequency is a start frequency in MHz. Should not be negative.
     * @param finishFrequency is a finish frequency in MHz. Should not be negative.
     * @param frequencyStep is a frequency step in MHz. Should be more than zero
     *                      or be more than start || stop frequency.
     * @param timeStep is a time step in seconds. Should not be more than zero.
     * @param isCycled is flag that tells whether the task should be cycled.
     */
    public Task(double startFrequency, double finishFrequency, int frequencyStep, int timeStep, boolean isCycled) {
        if ((startFrequency < 0) || (finishFrequency < 0) ||
                (frequencyStep <= 0) || (timeStep <=0) )
            throw new IllegalArgumentException("Incorrect parameters");
        mTaskType = TaskType.FREQUENCY_SCAN_MODE;
        mStartFrequency = startFrequency;
        mFinishFrequency = finishFrequency;
        mFrequencyStep = frequencyStep;
        mTimeStep = timeStep;
        mIsCycled = isCycled;
    }

    /**
     * Returns the type of current task.
     * @see ua.pp.lab101.synthesizercontrol.service.task.TaskType
     * @return
     */
    public TaskType getTaskType() {
        return mTaskType;
    }

    /**
     * Returns the frequency in MHz for constant mode.
     * @return frequency in MHz
     */
    public double getConstantFrequency() {
        return mConstantFrequency;
    }

    /**
     * Returns the map of time in seconds and frequency in MHz for schedule mode.
     * @return LinkedHashMap of K = time, V = frequency.
     */
    public LinkedHashMap<Integer, Double> getScheduleTask() {
        return mScheduleTask;
    }

    /**
     * Returns the start frequency value in MHz for frequency scan mode.
     * @return frequency value in MHz
     */
    public double getStartFrequency() {
        return mStartFrequency;
    }

    /**
     * Returns the finish frequency value in MHz for frequency scan mode.
     * @return frequency value in MHz
     */
    public double getFinishFrequency() {
        return mFinishFrequency;
    }

    /**
     * Returns the value of the step in MHz for frequency scan mode.
     * @return value of the frequency step in MHz
     */
    public int getFrequencyStep() {
        return mFrequencyStep;
    }

    /**
     * Returns the cycle flag for both scan and schedule modes.
     * @return cycle flag
     */
    public int getTimeStep() {
        return mTimeStep;
    }


}
