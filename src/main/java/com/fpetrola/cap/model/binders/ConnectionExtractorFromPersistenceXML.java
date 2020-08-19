package com.fpetrola.cap.model.binders;

import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.helpers.PersistenceUnitInfoImpl;
import com.fpetrola.cap.helpers.PersistenceXmlParser;
import com.fpetrola.cap.model.developer.DatabaseConnection;

public class ConnectionExtractorFromPersistenceXML implements BidirectionalBinder<Object, DatabaseConnection> {

	public String name;
	public ConnectionExtractorFromPersistenceXML() {
	}
	public List<DatabaseConnection> pull(Object source) {
		PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
		persistenceXmlParser.parse();
		PersistenceUnitInfoImpl defaultPersistenceUnit = persistenceXmlParser.getDefaultPersistenceUnit();

		String driver = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.driver");
		String connection = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.url");
		String user = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.user");
		String password = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.password");
		DatabaseConnection dbConnection = new DatabaseConnection(driver, connection, user, password);
		return Arrays.asList(dbConnection);
	}
}