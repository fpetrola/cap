package com.fpetrola.cap.helpers;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

	private String persistenceUnitName;

	public PersistenceUnitInfoImpl(String persistenceUnitName, String persistenceProviderClassName, PersistenceUnitTransactionType transactionType, DataSource jtaDataSource, String jtaDataSourceUrl, DataSource nonJtaDataSource, String nonJtaDataSourceUrl, List<String> mappingFileNames, URL persistenceUnitRootUrl, List<String> managedClassNames, boolean excludeUnlistedClasses, Properties properties, String persistenceXMLSchemaVersion) {
		super();
		this.persistenceUnitName = persistenceUnitName;
		this.persistenceProviderClassName = persistenceProviderClassName;
		this.transactionType = transactionType;
		this.jtaDataSource = jtaDataSource;
		this.jtaDataSourceUrl = jtaDataSourceUrl;
		this.nonJtaDataSource = nonJtaDataSource;
		this.nonJtaDataSourceUrl = nonJtaDataSourceUrl;
		this.mappingFileNames = mappingFileNames;
		this.persistenceUnitRootUrl = persistenceUnitRootUrl;
		this.managedClassNames = managedClassNames;
		this.excludeUnlistedClasses = excludeUnlistedClasses;
		this.properties = properties;
		this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
	}

	private String persistenceProviderClassName;
	private PersistenceUnitTransactionType transactionType;
	private DataSource jtaDataSource;
	private String jtaDataSourceUrl;
	private DataSource nonJtaDataSource;
	private String nonJtaDataSourceUrl;
	private List<String> mappingFileNames;
	private URL persistenceUnitRootUrl;
	private List<String> managedClassNames;
	private boolean excludeUnlistedClasses;
	private Properties properties;
	private String persistenceXMLSchemaVersion;

	public PersistenceUnitInfoImpl() {
	}
	
	public boolean isExcludeUnlistedClasses() {
		return excludeUnlistedClasses;
	}

	public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
		this.excludeUnlistedClasses = excludeUnlistedClasses;
	}

	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public void setPersistenceProviderClassName(String persistenceProviderClassName) {
		this.persistenceProviderClassName = persistenceProviderClassName;
	}

	public void setTransactionType(PersistenceUnitTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public void setJtaDataSource(DataSource jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	public void setJtaDataSourceUrl(String jtaDataSourceUrl) {
		this.jtaDataSourceUrl = jtaDataSourceUrl;
	}

	public void setNonJtaDataSource(DataSource nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public void setNonJtaDataSourceUrl(String nonJtaDataSourceUrl) {
		this.nonJtaDataSourceUrl = nonJtaDataSourceUrl;
	}

	public void setMappingFileNames(List<String> mappingFileNames) {
		this.mappingFileNames = mappingFileNames;
	}

	public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
		this.persistenceUnitRootUrl = persistenceUnitRootUrl;
	}

	public void setManagedClassNames(List<String> managedClassNames) {
		this.managedClassNames = managedClassNames;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion) {
		this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
	}

	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	public String getPersistenceProviderClassName() {
		return persistenceProviderClassName;
	}

	public PersistenceUnitTransactionType getTransactionType() {
		if (transactionType == null) {
			return PersistenceUnitTransactionType.RESOURCE_LOCAL;
		}
		return transactionType;
	}

	public DataSource getJtaDataSource() {
		return jtaDataSource;
	}

	public String getJtaDataSourceUrl() {
		return jtaDataSourceUrl;
	}

	public DataSource getNonJtaDataSource() {
		return nonJtaDataSource;
	}

	public String getNonJtaDataSourceUrl() {
		return nonJtaDataSourceUrl;
	}

	public List<String> getMappingFileNames() {
		return mappingFileNames;
	}

	public List<URL> getJarFileUrls() {
		return Collections.<URL>emptyList();
	}

	public URL getPersistenceUnitRootUrl() {
		return persistenceUnitRootUrl;
	}

	public List<String> getManagedClassNames() {
		return managedClassNames;
	}

	public boolean excludeUnlistedClasses() {
		return excludeUnlistedClasses;
	}

	public Properties getProperties() {
		return properties;
	}

	public String getPersistenceXMLSchemaVersion() {
		return persistenceXMLSchemaVersion;
	}

	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public void addTransformer(ClassTransformer transformer) {
	}

	public ClassLoader getNewTempClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
}