package com.robo4j.dof10imu;

import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.db.sql.RoboPointSQLPersistenceUnit;
import com.robo4j.db.sql.SQLDataSourceUnit;
import com.robo4j.db.sql.util.DBSQLConstants;
import com.robo4j.dof10imu.controller.DOF10UnitProvider;
import com.robo4j.dof10imu.controller.GPSDemoController;
import com.robo4j.units.rpi.gps.GPSUnit;

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
	private static final String DOF10_PROVIDER = "dof10UnitProvider";
    private static final String GPS_CONTROLLER = "gpsController";
    private static final String GPS_UNIT = "gps";

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

		DOF10UnitProvider dof10UnitProvider = new DOF10UnitProvider(system, DOF10_PROVIDER);
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(DBSQLConstants.KEY_PERSISTENCE_UNIT, TARGET_STORAGE_UNIT);
		dof10UnitProvider.initialize(config);

        GPSDemoController gpsDemoController = new GPSDemoController(system, GPS_CONTROLLER);
        config = ConfigurationFactory.createEmptyConfiguration();
        config.setString("gpsUnitName", GPS_UNIT);
        config.setString("gpsProcessorName", DOF10_PROVIDER);
        gpsDemoController.initialize(config);

        GPSUnit gpsUnit = new GPSUnit(system, GPS_UNIT);
        config = ConfigurationFactory.createEmptyConfiguration();
        gpsUnit.initialize(config);


		system.addUnits(sqlUnit, persistenceUnit, dof10UnitProvider, gpsUnit, gpsDemoController);
        sqlUnit.initialize(sqlConfig);
        persistenceUnit.initialize(persistenceUnitConfig);

        system.start();

        System.out.println("System: State after start:");
        System.out.println(SystemUtil.printStateReport(system));

        System.out.println("State after start:");
        System.out.println(SystemUtil.printStateReport(system));

        System.out.println("Press enter to quit!");
        System.in.read();
        system.shutdown();
        System.out.println("System: State after shutdown:");
        System.out.println(SystemUtil.printStateReport(system));

	}
}
