package ua.pp.lab101.synthesizercontrol.service.task;

/**
 * Presents supported types of task that could be created and performed.
 */
public enum TaskType {
    /**
     * Constant frequency type. Frequency could be set ones. The task could be stopped manually.
     */
    CONSTANT_FREQUENCY_MODE,

    /**
     * Schedule mode. In this mode frequency sets for some value of time. Could be cycled.
     */
    SCHEDULE_MODE,

    /**
     * Frequency scan mode. In this mode frequency increases (or decreases) from some start value to
     * some finish value by some step.
     */
    FREQUENCY_SCAN_MODE;
}
