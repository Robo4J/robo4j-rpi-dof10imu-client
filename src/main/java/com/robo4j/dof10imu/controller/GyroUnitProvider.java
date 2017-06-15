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

import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.db.sql.dto.ERoboPointDTO;
import com.robo4j.db.sql.util.DBSQLConstants;
import com.robo4j.units.rpi.gyro.GyroEvent;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class GyroUnitProvider extends RoboUnit<GyroEvent> {

	private static final int INIT_VALUE = 0;
	private volatile AtomicInteger counter = new AtomicInteger(0);
	private String persistenceUnitName;
	private Integer validPointNumber;
	private Gson gson = new Gson();

	public GyroUnitProvider(RoboContext context, String id) {
		super(GyroEvent.class, context, id);
	}

	@Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        persistenceUnitName = configuration.getString(DBSQLConstants.KEY_PERSISTENCE_UNIT, null);
        if(persistenceUnitName == null){
            SimpleLoggingUtil.debug(getClass(), "no available" + DBSQLConstants.KEY_PERSISTENCE_UNIT);
        }

        validPointNumber = configuration.getInteger(Utils.VALID_STORE_POINT, null);
        if(validPointNumber == null){
            throw new ConfigurationException(Utils.VALID_STORE_POINT);
        }
    }

	@Override
	public void onMessage(GyroEvent message) {
		System.out.println(getClass().getSimpleName() + ": onMessage: " + message);
		if (persistenceUnitName != null && validPointNumber == counter.getAndIncrement()) {
			System.out.println(getClass().getSimpleName() + ": STORED onMessage: " + message);
			getContext().getReference(persistenceUnitName).sendMessage(eventToMessage(message));
			counter.set(INIT_VALUE);
		}
	}

	// Private Methods
	private ERoboPointDTO eventToMessage(GyroEvent message) {
		//@formatter:off
		StringBuilder sb = new StringBuilder("{")
				.append(message.getAngles().x).append(",")
				.append(message.getAngles().y).append(",")
				.append(message.getAngles().z).append("}");
		//@formatter:on
		return new ERoboPointDTO(Utils.GYRO_UNIT, gson.toJson(message.getAngles()));
	}
}
