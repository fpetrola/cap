package com.fpetrola.cap.helpers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.persistence.spi.PersistenceUnitTransactionType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PersistenceXmlParser {

	public static final String DEFAULT_PERSISTENCE_XML_LOCATION = "META-INF/persistence.xml";
	public static final String VERSION_ATTRIBUTE = "version";
	public static final String PERSISTENCE_TAG = "persistence";
	public static final String PERSISTENCE_UNIT_TAG = "persistence-unit";
	public static final String PROVIDER_TAG = "provider";
	public static final String PROPERTIES_TAG = "properties";
	public static final String PROPERTY_TAG = "property";
	public static final String JTA_DATA_SOURCE_TAG = "jta-data-source";
	public static final String CLASS_TAG = "class";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String VALUE_ATTRIBUTE = "value";
	public static final String TRANSACTION_TYPE_ATTR = "transaction-type";

	String jpaVersion;

	List<PersistenceUnitInfoImpl> puList = new LinkedList<>();

	public PersistenceUnitInfoImpl getDefaultPersistenceUnit() {
		return puList.get(0);
	}

	public List<PersistenceUnitInfoImpl> getPersistenceUnits() {
		return puList;
	}

	public void parse(){
		try {
			parse(Thread.currentThread().getContextClassLoader());
		} catch (IOException | ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parse(ClassLoader classLoader) throws IOException, ParserConfigurationException, SAXException {
		parse(classLoader.getResource(DEFAULT_PERSISTENCE_XML_LOCATION));
	}

	public void parse(URL persistenceXmlFile) throws IOException, ParserConfigurationException, SAXException {
		URLConnection conn = persistenceXmlFile.openConnection();
		conn.setUseCaches(false);
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(conn.getInputStream());

		Element persistenceElement = doc.getDocumentElement();
		jpaVersion = persistenceElement.getAttribute(VERSION_ATTRIBUTE);

		NodeList nList = persistenceElement.getElementsByTagName(PERSISTENCE_UNIT_TAG);
		for (int i = 0; i < nList.getLength(); ++i) {
			Node n = nList.item(i);
			if (n instanceof Element) {
				parsePersistenceUnit((Element) n);
			}
		}
	}

	public String getJpaVersion() {
		return jpaVersion;
	}

	private void parsePersistenceUnit(Element element) {

		PersistenceUnitInfoImpl persistenceUnitInfoImpl = new PersistenceUnitInfoImpl();

		persistenceUnitInfoImpl.setPersistenceXMLSchemaVersion(getJpaVersion());
		persistenceUnitInfoImpl.setPersistenceUnitName(element.getAttribute(NAME_ATTRIBUTE));

		if (element.hasAttribute(TRANSACTION_TYPE_ATTR)) {
			persistenceUnitInfoImpl.setTransactionType(PersistenceUnitTransactionType.valueOf(element.getAttribute(TRANSACTION_TYPE_ATTR)));
		}

		NodeList jtaDataSourceTagList = element.getElementsByTagName(JTA_DATA_SOURCE_TAG);
		if (jtaDataSourceTagList.getLength() > 0) {
			persistenceUnitInfoImpl.setJtaDataSourceUrl(((Element) jtaDataSourceTagList.item(0)).getTextContent());
		}

		NodeList providerTagList = element.getElementsByTagName(PROVIDER_TAG);
		if (providerTagList.getLength() > 0) {
			persistenceUnitInfoImpl.setPersistenceProviderClassName(providerTagList.item(0).getTextContent());
		}

		NodeList managedClassList = element.getElementsByTagName(CLASS_TAG);
		if (providerTagList.getLength() > 0) {
			persistenceUnitInfoImpl.setManagedClassNames(getManagedClassNames(managedClassList));
		}

		NodeList propertiesTagList = element.getElementsByTagName(PROPERTIES_TAG);
		if (propertiesTagList.getLength() > 0) {
			NodeList nList = ((Element) propertiesTagList.item(0)).getElementsByTagName(PROPERTY_TAG);
			Properties properties = new Properties();
			for (int i = 0; i < nList.getLength(); ++i) {
				Element property = (Element) nList.item(i);
				properties.setProperty(property.getAttribute(NAME_ATTRIBUTE), property.getAttribute(VALUE_ATTRIBUTE));
			}
			persistenceUnitInfoImpl.setProperties(properties);
		}
		puList.add(persistenceUnitInfoImpl);
	}

	private List<String> getManagedClassNames(NodeList managedClassList) {
		List<String> managedClassNames = new LinkedList<>();
		for (int i = 0; i < managedClassList.getLength(); ++i) {
			Element cn = (Element) managedClassList.item(i);
			managedClassNames.add(cn.getTextContent());
		}
		return managedClassNames;
	}
}