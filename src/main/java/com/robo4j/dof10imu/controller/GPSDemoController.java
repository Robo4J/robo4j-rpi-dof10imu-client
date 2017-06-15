/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

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
 * Controller for GPS Unit
 * functionality: turn on/off
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class GPSDemoController extends RoboUnit<Boolean> {

    private String unitName;
    private String processorName;

    public GPSDemoController(RoboContext context, String id) {
        super(Boolean.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        unitName = configuration.getString(Utils.PROPERTY_UNIT_NAME, null);
        if(unitName == null){
            throw new ConfigurationException(Utils.PROPERTY_UNIT_NAME);
        }
        processorName = configuration.getString(Utils.PROPERTY_PROCESSOR_NAME, null);
        if(processorName == null){
            throw new ConfigurationException(Utils.PROPERTY_PROCESSOR_NAME);
        }
    }

    @Override
    public void onMessage(Boolean message) {
        RoboReference<GPSEvent> processor = getContext().getReference(processorName);
        if(message){
            System.out.println("START to gps unit: " + unitName);
            getContext().getReference(unitName).sendMessage(new GPSRequest(processor, GPSRequest.Operation.REGISTER));
        } else {
            System.out.println("ENDING requesting GPS events: " + unitName);
            getContext().getReference(unitName).sendMessage(new GPSRequest(processor, GPSRequest.Operation.UNREGISTER));

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
