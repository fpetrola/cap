package com.fpetrola.cap.model.binders.implementations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.fpetrola.cap.helpers.PersistenceUnitInfoImpl;
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

				File dir = new File("/home/fernando/git/cap-tests");
				Optional<Path> findFirst = Files.walk(Paths.get(dir.getPath())).filter(f -> f.getFileName().toString().contains("persistence.xml")).findFirst();

				findFirst.ifPresent(p -> {

					try {
						PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
						persistenceXmlParser.parse(p.toUri().toURL());
						PersistenceUnitInfoImpl defaultPersistenceUnit = persistenceXmlParser.getDefaultPersistenceUnit();

						String driver = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.driver");
						String connection = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.url");
						String user = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.user");
						String password = defaultPersistenceUnit.getProperties().getProperty("javax.persistence.jdbc.password");
						DatabaseConnection dbConnection = new DatabaseConnection(driver, connection, user, password);
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