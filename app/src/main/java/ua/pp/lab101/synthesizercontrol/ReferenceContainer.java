package ua.pp.lab101.synthesizercontrol;

import java.io.Serializable;

import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;

/**
 * Created by ashram on 4/15/15.
 */
public class ReferenceContainer implements Serializable {
    private BoardManagerService mService;
    private BoardManagerService.LocalBinder mBinder;

    public ReferenceContainer(BoardManagerService service, BoardManagerService.LocalBinder binder) {
        mBinder = binder;
        mService = service;
    }

    public BoardManagerService getService() {
        return mService;
    }

    public BoardManagerService.LocalBinder getBinder() {
        return mBinder;
    }
}
