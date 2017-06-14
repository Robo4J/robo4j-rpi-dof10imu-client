package com.robo4j.dof10imu.controller;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.hw.rpi.serial.gps.GPSEvent;
import com.robo4j.units.rpi.gps.GPSRequest;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class GPSDemoController extends RoboUnit<Boolean> {

    private String gpsUnitName;
    private String gpsProcessorName;

    public GPSDemoController(RoboContext context, String id) {
        super(Boolean.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        gpsUnitName = configuration.getString("gpsUnitName", null);
        gpsProcessorName = configuration.getString("gpsProcessorName", null);
    }

    @Override
    public void onMessage(Boolean message) {
        RoboReference<GPSEvent> processor = getContext().getReference(gpsProcessorName);
        if(message){
            System.out.println("START to gps unit: " + gpsUnitName);
            getContext().getReference(gpsUnitName).sendMessage(new GPSRequest(processor, GPSRequest.Operation.REGISTER));
        } else {
            System.out.println("ENDING requesting GPS events: " + gpsUnitName);
            getContext().getReference(gpsUnitName).sendMessage(new GPSRequest(processor, GPSRequest.Operation.UNREGISTER));

        }
    }

    @Override
    public void start() {
        this.setState(LifecycleState.STARTING);
        onMessage(true);
        this.setState(LifecycleState.STARTED);
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        onMessage(false);
        setState(LifecycleState.STOPPED);
    }
}
