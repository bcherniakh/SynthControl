package ua.pp.lab101.synthesizercontrol.service.task;

import java.util.Arrays;

/**
 * Task is class that presents the current mConstantFrequency in MHz and time of action in seconds
 * Created by ashram on 4/23/15.
 *
 */

public class Task {
    private TaskType mTaskType;
    private double mConstantFrequency;
    private double[] frequencyArray;
    private int[] timeArray;
    private double[] mStartFrequency;
    private double[] mFinishFrequency;
    private double[] mFrequencyStep;
    private int[] mTimeStep;
    private boolean mIsCycled;


    /**
     * Constructor for creation constant mode task. It takes one parameter - frequency value.
     * @param constantFrequency is constant frequency value in Mhz. Should not be negative
     */
    public Task(double constantFrequency) {
        mTaskType = TaskType.CONSTANT_FREQUENCY;
        if (constantFrequency <= 0) throw new IllegalArgumentException();
        this.mConstantFrequency = constantFrequency;
    }

    /**
     * Constructor for creation schedule mode task. It takes a LinkedHashMap as parameter.
     * @param frequencyArray is a array of frequency values in MHz (as double)
     * @param timeArray is an array of time values in seconds
     * @param isCycled is flag that tells whether the task should be cycled.
     */
    public Task(double[] frequencyArray, int[] timeArray, boolean isCycled) {
        if (frequencyArray == null || timeArray == null) throw new IllegalArgumentException("task could not be null");
        mTaskType = TaskType.SCHEDULE;
        this.frequencyArray = Arrays.copyOf(frequencyArray, frequencyArray.length);
        this.timeArray = Arrays.copyOf(timeArray, timeArray.length);
        this.mIsCycled = isCycled;
    }

    /**
     * Constructor for creation frequency scan task.
     * @param startFrequency is a start frequency array in MHz. Should not be negative.
     * @param finishFrequency is a finish frequency array in MHz. Should not be negative.
     * @param frequencyStep is a frequency step array in MHz. Should be more than zero
     *                      or be more than start || stop frequency.
     * @param timeStep is a time step array in seconds. Should not be more than zero.
     * @param isCycled is flag that tells whether the task should be cycled.
     */
    public Task(double[] startFrequency, double[] finishFrequency, double[] frequencyStep, double[] timeStep, boolean isCycled) {
        mTaskType = TaskType.FREQUENCY_SCAN;
        mStartFrequency =  Arrays.copyOf(startFrequency, startFrequency.length);
        mFinishFrequency = Arrays.copyOf(finishFrequency, finishFrequency.length);
        mFrequencyStep = Arrays.copyOf(frequencyStep, frequencyStep.length);
        mTimeStep = new int[timeStep.length];

        for (int i =0; i < mTimeStep.length; i++) {
            mTimeStep[i] = (int) (timeStep[i]*1000);
        }
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
     * Returns array of frequency values fo Scheduler mode
     * @return frequency values in MHz
     */
    public double[] getFrequencyArray(){
        return frequencyArray;
    }

    /**
     * Returns array of time values for scheduler mode
     * @return
     */
    public int[] getTimeArray() {
        return timeArray;
    }

    /**
     * Returns is cycled value
     * @return
     */
    public boolean getIsCycled() {
        return mIsCycled;
    }
    /**
     * Returns the start frequency value in MHz for frequency scan mode.
     * @return frequency value in MHz
     */
    public double[] getStartFrequency() {
        return mStartFrequency;
    }

    /**
     * Returns the finish frequency value in MHz for frequency scan mode.
     * @return frequency value in MHz
     */
    public double[] getFinishFrequency() {
        return mFinishFrequency;
    }

    /**
     * Returns the value of the step in MHz for frequency scan mode.
     * @return value of the frequency step in MHz
     */
    public double[] getFrequencyStep() {
        return mFrequencyStep;
    }

    /**
     * Returns the cycle flag for both scan and schedule modes.
     * @return cycle flag
     */
    public int[] getTimeStep() {
        return mTimeStep;
    }

}
