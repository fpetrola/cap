package com.fpetrola.cap.model.binders.implementations;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.DatabaseConnection;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.Property;

public class DatabaseEntitiesExtractor extends DefaultBinder implements BidirectionalBinder<DatabaseConnection, EntityModel> {

	private List<EntityModel> entities = new ArrayList<EntityModel>();
	private DatabaseConnection dbConnection;

	public DatabaseEntitiesExtractor() {
	}
	
	public List<EntityModel> pull(DatabaseConnection dbConnection) {

		entities.clear();
		this.dbConnection = dbConnection;
		try {

			DatabaseMetaData databaseMetaData = dbConnection.con.getMetaData();
			ResultSet resultSet = databaseMetaData.getTables(dbConnection.con.getCatalog(), null, null, new String[] { "TABLE" });

			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				getTable(tableName);
			}

			Collections.sort(entities, new Comparator<EntityModel>() {

				public int compare(EntityModel arg0, EntityModel arg1) {
					return arg0.name.compareTo(arg1.name);
				}
			});

			return entities;

		} catch (Exception e) {
			return new ArrayList<EntityModel>();
		}
	}

	public void getTable(String tableName) throws SQLException {
		Statement st = dbConnection.con.createStatement();

		String sql = "select * from " + tableName;
		ResultSet rs = st.executeQuery(sql);
		ResultSetMetaData metaData = rs.getMetaData();

		int rowCount = metaData.getColumnCount();
		List<Property> properties = new ArrayList<>();

		for (int i = 0; i < rowCount; i++) {
			String columnName = metaData.getColumnName(i + 1);
			int columnDisplaySize = metaData.getColumnDisplaySize(i + 1);
			String columnTypeName = metaData.getColumnTypeName(i + 1);

			properties.add(new Property(columnName, columnTypeName));
		}
		entities.add(new EntityModel(tableName, properties));
	}

	@Override
	public String toString() {
		return "DatabaseEntitiesExtractor";
	}
	
	public String getParametersProposalMessage() {
		return "I've found some new entities in database. Do you want to add them to your code?";
	}
}