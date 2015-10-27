package ua.pp.lab101.synthesizercontrol.service;

/**
 * Created by ashram on 4/17/15.
 */
public enum ServiceStatus {
    IDLE,
    CONSTANT_MODE,
    SCHEDULE_MODE,
    FREQUENCY_SCAN_MODE,
    DEVICE_FOUND,
    DEVICE_DISCONNECTED;
}
