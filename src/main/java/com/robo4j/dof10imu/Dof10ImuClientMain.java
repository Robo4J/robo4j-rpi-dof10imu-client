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

package com.robo4j.dof10imu;

import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.db.sql.RoboPointSQLPersistenceUnit;
import com.robo4j.db.sql.SQLDataSourceUnit;
import com.robo4j.db.sql.util.DBSQLConstants;
import com.robo4j.dof10imu.controller.AccDemoController;
import com.robo4j.dof10imu.controller.AccUnitProvider;
import com.robo4j.dof10imu.controller.GPSDemoController;
import com.robo4j.dof10imu.controller.GpsUnitProvider;
import com.robo4j.dof10imu.controller.GyroDemoController;
import com.robo4j.dof10imu.controller.GyroUnitProvider;
import com.robo4j.dof10imu.controller.Utils;
import com.robo4j.units.rpi.accelerometer.AccelerometerLSM303Unit;
import com.robo4j.units.rpi.gps.GPSUnit;
import com.robo4j.units.rpi.gyro.GyroL3GD20Unit;

/**
 * Demo is optimised for PostgreSQL 9.4 Database It's necessary to prepare
 * Database schema by using Flyway. Flyway is available inside robo4j-db-sql
 * module.
 *
 * module allows to play with form of storing element type
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class Dof10ImuClientMain {

	private static final String PERSISTENCE_UNIT_NAME = "dbSqlUnit";
	private static final String TARGET_STORAGE_UNIT = "dof10PersistenceUnit";
	private static final String GPS_DOF10_PROVIDER = "gpsDof10UnitProvider";
	private static final String GPS_CONTROLLER = "gpsController";
	private static final String GYRO_DOF10_PROVIDER = "gyroDof10UnitProvider";
	private static final String GYRO_CONTROLLER = "gyroController";
	private static final String ACC_DOF10_PROVIDER = "accDof10UnitProvider";
	private static final String ACC_CONTROLLER = "accController";

	public static void main(String[] args) throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration sqlConfig = ConfigurationFactory.createEmptyConfiguration();
		SQLDataSourceUnit sqlUnit = new SQLDataSourceUnit(system, PERSISTENCE_UNIT_NAME);
		sqlConfig.setString("sourceType", "postgresql");
		sqlConfig.setString("packages", "com.robo4j.db.sql.model");
		sqlConfig.setInteger("limit", 3);
		sqlConfig.setString("sorted", "asc");
		sqlConfig.setString("hibernate.hbm2ddl.auto", "validate");
		sqlConfig.setString("hibernate.connection.url", "jdbc:postgresql://192.168.178.42:5433/robo4j1");
		sqlConfig.setString("targetUnit", TARGET_STORAGE_UNIT);

		RoboPointSQLPersistenceUnit persistenceUnit = new RoboPointSQLPersistenceUnit(system, TARGET_STORAGE_UNIT);
		Configuration persistenceUnitConfig = ConfigurationFactory.createEmptyConfiguration();
		persistenceUnitConfig.setString("persistenceUnit", PERSISTENCE_UNIT_NAME);
		persistenceUnitConfig.setString("config", "default");

		// GPS
		GpsUnitProvider gpsUnitProvider = new GpsUnitProvider(system, GPS_DOF10_PROVIDER);
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(DBSQLConstants.KEY_PERSISTENCE_UNIT, TARGET_STORAGE_UNIT);
		config.setInteger(Utils.VALID_STORE_POINT, 4);
		gpsUnitProvider.initialize(config);

		GPSDemoController gpsDemoController = new GPSDemoController(system, GPS_CONTROLLER);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(Utils.PROPERTY_UNIT_NAME, Utils.GPS_UNIT);
		config.setString(Utils.PROPERTY_PROCESSOR_NAME, GPS_DOF10_PROVIDER);
		gpsDemoController.initialize(config);

		GPSUnit gpsUnit = new GPSUnit(system, Utils.GPS_UNIT);
		config = ConfigurationFactory.createEmptyConfiguration();
		gpsUnit.initialize(config);

		// GYRO
		GyroUnitProvider gyroUnitProvider = new GyroUnitProvider(system, GYRO_DOF10_PROVIDER);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(DBSQLConstants.KEY_PERSISTENCE_UNIT, TARGET_STORAGE_UNIT);
		config.setInteger(Utils.VALID_STORE_POINT, 10);
		gyroUnitProvider.initialize(config);

		GyroDemoController gyroDemoController = new GyroDemoController(system, GYRO_CONTROLLER);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(Utils.PROPERTY_UNIT_NAME, Utils.GYRO_UNIT);
		config.setString(Utils.PROPERTY_PROCESSOR_NAME, GYRO_DOF10_PROVIDER);
		gyroDemoController.initialize(config);

		// similar setup to gyroexample.xml
		GyroL3GD20Unit gyroUnit = new GyroL3GD20Unit(system, Utils.GYRO_UNIT);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setInteger("bus", 1);
		config.setInteger("address", 0x6b);
		config.setString("sensitivity", "DPS_245");
		config.setBoolean("enableHighPass", true);
		// Periodicity, in ms, to sample the gyro
		config.setInteger("period", 10);
		gyroUnit.initialize(config);

		// Accelerometer
		AccUnitProvider accUnitProvider = new AccUnitProvider(system, ACC_DOF10_PROVIDER);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(DBSQLConstants.KEY_PERSISTENCE_UNIT, TARGET_STORAGE_UNIT);
		config.setInteger(Utils.VALID_STORE_POINT, 20);
		accUnitProvider.initialize(config);

		AccDemoController accDemoController = new AccDemoController(system, ACC_CONTROLLER);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(Utils.PROPERTY_UNIT_NAME, Utils.ACC_UNIT);
		config.setString(Utils.PROPERTY_PROCESSOR_NAME, ACC_DOF10_PROVIDER);
		accDemoController.initialize(config);

		// similar to accelerometer.xml
		AccelerometerLSM303Unit accelerometerUnit = new AccelerometerLSM303Unit(system, Utils.ACC_UNIT);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setInteger("bus", 1);
		config.setInteger("address", 0x19);
		config.setString("rage", "HZ_10");
		//Periodicity, in ms, to sample the gyro
		config.setInteger("axisEnable", 7);
		config.setInteger("period", 200);
		Configuration offsetConfig = config.createChildConfiguration("offsets");
		offsetConfig.setFloat("x", 0.064f);
		offsetConfig.setFloat("y", 0.001f);
		offsetConfig.setFloat("z", 0f);
		Configuration multipliers = config.createChildConfiguration("multipliers");
		multipliers.setFloat("x", -0.969932f);
		multipliers.setFloat("y", 0.917431f);
		multipliers.setFloat("z", 0.943396f);
		accelerometerUnit.initialize(config);

		//system init
		system.addUnits(sqlUnit, persistenceUnit, gpsUnitProvider, gpsUnit, gpsDemoController, gyroUnit,
				gyroUnitProvider, gyroDemoController, accelerometerUnit, accUnitProvider, accDemoController);

		sqlUnit.initialize(sqlConfig);
		persistenceUnit.initialize(persistenceUnitConfig);

		system.start();
		System.out.println("State after start:");
		System.out.println(SystemUtil.printStateReport(system));

		System.out.println("Press enter to quit!");
		System.in.read();
		system.shutdown();
		System.out.println("System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));

	}

}
