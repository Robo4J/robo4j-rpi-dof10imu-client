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
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.db.sql.dto.ERoboPointDTO;
import com.robo4j.db.sql.util.DBSQLConstants;
import com.robo4j.hw.rpi.serial.gps.GPSEvent;
import com.robo4j.hw.rpi.serial.gps.GPSVisitor;
import com.robo4j.hw.rpi.serial.gps.PositionEvent;
import com.robo4j.hw.rpi.serial.gps.VelocityEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class DOF10UnitProvider extends RoboUnit<GPSEvent> {

    private static final int INIT_VALUE = 0;
    private static final String ROBO_POINT_TYPE = "gps";
    private static final int MAX_DELAY_POINTS = 10;
    private String persistenceUnitName;
    private final  GpsResultVisitor visitor = new GpsResultVisitor();
    private volatile AtomicInteger counter = new AtomicInteger(0);

    public DOF10UnitProvider(RoboContext context, String id) {
        super(GPSEvent.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        persistenceUnitName = configuration.getString(DBSQLConstants.KEY_PERSISTENCE_UNIT, null);
    }

    @Override
    public void onMessage(GPSEvent result) {
        if(persistenceUnitName != null && MAX_DELAY_POINTS == counter.getAndIncrement()){
            getContext().getReference(persistenceUnitName).sendMessage(result.visit(visitor));
            counter.set(INIT_VALUE);
        }
    }

    //Private Method
    //FIXME
    private class GpsResultVisitor implements GPSVisitor<ERoboPointDTO> {
        @Override
        public ERoboPointDTO visit(VelocityEvent event) {
            StringBuilder sb = new StringBuilder("{")
                    .append(event.getGroundSpeed())
                    .append(",")
                    .append(event.getMagneticTrackMadeGood())
                    .append(",")
                    .append(event.getTrueTrackMadeGood())
                    .append("}");
            return new ERoboPointDTO(ROBO_POINT_TYPE, sb.toString());
        }

        @Override
        public ERoboPointDTO visit(PositionEvent event) {
            StringBuilder sb = new StringBuilder("{")
                    .append(event.getAltitude())
                    .append(",")
                    .append(event.getElipsoidAltitude())
                    .append(",")
                    .append(event.getFixQuality())
                    .append(",")
                    .append(event.getAccuracyCategory())
                    .append(",")
                    .append(event.getLocation().getLatitude())
                    .append(":")
                    .append(event.getLocation().getLongitude())
                    .append("}");
            return new ERoboPointDTO(ROBO_POINT_TYPE, sb.toString());
        }
    }

}
