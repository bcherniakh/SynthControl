package ua.pp.lab101.synthesizercontrol.activity.main;

import java.io.Serializable;

import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;

/**
 * Created by ashram on 4/15/15.
 */
public class ReferenceContainer implements Serializable {
    private BoardManagerService mService;
    private BoardManagerService.BoardManagerBinder mBinder;

    public ReferenceContainer(BoardManagerService service, BoardManagerService.BoardManagerBinder binder) {
        mBinder = binder;
        mService = service;
    }

    public BoardManagerService getService() {
        return mService;
    }

    public BoardManagerService.BoardManagerBinder getBinder() {
        return mBinder;
    }
}
