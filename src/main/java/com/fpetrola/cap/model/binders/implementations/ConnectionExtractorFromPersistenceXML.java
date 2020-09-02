package com.fpetrola.cap.model.binders.implementations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.fpetrola.cap.helpers.PersistenceXmlParser;
import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.DatabaseConnection;

public class ConnectionExtractorFromPersistenceXML extends DefaultBinder<Void, DatabaseConnection> implements BidirectionalBinder<Void, DatabaseConnection> {

	public String name;
	static private List<DatabaseConnection> result = new ArrayList<DatabaseConnection>();

	public ConnectionExtractorFromPersistenceXML() {
	}

	public List<DatabaseConnection> pull(Void source) {
		if (result.isEmpty())
			try {

				var dir = new File("/home/fernando/git/cap-tests");
				var findFirst = Files.walk(Paths.get(dir.getPath())).filter(f -> f.getFileName().toString().contains("persistence.xml")).findFirst();

				findFirst.ifPresent(p -> {

					try {
						var persistenceXmlParser = new PersistenceXmlParser();
						persistenceXmlParser.parse(p.toUri().toURL());
						
						var defaultPersistenceUnit = persistenceXmlParser.getDefaultPersistenceUnit();
						var driver = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.driver");
						var connection = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.url");
						var user = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.user");
						var password = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.password");
						
						var dbConnection = new DatabaseConnection(driver, connection, user, password);
						
						result.add(dbConnection);
					} catch (IOException | ParserConfigurationException | SAXException e) {
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		return result;
	}
}