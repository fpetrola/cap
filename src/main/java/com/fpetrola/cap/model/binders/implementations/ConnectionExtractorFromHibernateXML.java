package com.fpetrola.cap.model.binders.implementations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.DatabaseConnection;

public class ConnectionExtractorFromHibernateXML extends DefaultBinder<Void, DatabaseConnection> implements BidirectionalBinder<Void, DatabaseConnection> {

	public String name;
	
	static private List<DatabaseConnection> result= new ArrayList<DatabaseConnection>();

	public ConnectionExtractorFromHibernateXML() {
	}

	public List<DatabaseConnection> pull(Void source) {
		if (result.isEmpty())
			try {

				File dir = new File("/home/fernando/git/cap-tests");
				Optional<Path> findFirst = Files.walk(Paths.get(dir.getPath())).filter(f -> f.getFileName().toString().contains("hibernate.cfg.xml")).findFirst();

				findFirst.ifPresent(p -> {

					try {
						HashMap<String, String> values = new HashMap<String, String>();

						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder;
						builder = factory.newDocumentBuilder();
						Document doc = builder.parse(p.toFile());
						Node first = doc.getFirstChild();
						NodeList childs = first.getChildNodes();
						Element item = (Element) childs.item(1);
						NodeList childNodes = item.getChildNodes();
						String driver = "";
						String connection = "";
						String user = "";
						String password = "";

						for (int i = 0; i < childNodes.getLength(); i++) {
							Node child = childNodes.item(i);

							if (child instanceof Element) {
								Element element = (Element) child;

								String attribute = element.getAttribute("name");

								Node item2 = element.getChildNodes().item(0);
								String textContent = item2.getTextContent();

								if (attribute.equals("connection.driver_class"))
									driver = textContent;
								if (attribute.equals("connection.url"))
									connection = textContent;
								if (attribute.equals("connection.username"))
									user = textContent;
								if (attribute.equals("connection.password"))
									password = textContent;
							}
						}

						DatabaseConnection dbConnection = new DatabaseConnection(driver, connection, user, password);
						result.add(dbConnection);
					} catch (DOMException | ParserConfigurationException | SAXException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		return result;
	}
}