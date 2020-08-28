package com.fpetrola.cap.model.binders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fpetrola.cap.helpers.PersistenceUnitInfoImpl;
import com.fpetrola.cap.helpers.PersistenceXmlParser;
import com.fpetrola.cap.model.developer.DatabaseConnection;

public class ConnectionExtractorFromPersistenceXML extends DefaultBinder implements BidirectionalBinder<Object, DatabaseConnection> {

	public String name;

	public ConnectionExtractorFromPersistenceXML() {
	}

	public List<DatabaseConnection> pull(Object source) {
		List<DatabaseConnection> result= new ArrayList<DatabaseConnection>();
		try {

			File dir = new File("/home/fernando/git/cap-tests");
			Optional<Path> findFirst = Files.walk(Paths.get(dir.getPath())).filter(f -> f.getFileName().toString().contains("persistence.xml")).findFirst();
			System.out.println(findFirst);

			findFirst.ifPresent(p -> {

				PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
				persistenceXmlParser.parse();
				PersistenceUnitInfoImpl defaultPersistenceUnit = persistenceXmlParser.getDefaultPersistenceUnit();

				String driver = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.driver");
				String connection = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.url");
				String user = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.user");
				String password = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.password");
				DatabaseConnection dbConnection = new DatabaseConnection(driver, connection, user, password);
				result.add(dbConnection);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}