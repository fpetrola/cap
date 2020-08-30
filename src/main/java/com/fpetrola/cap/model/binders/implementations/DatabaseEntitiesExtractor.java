package com.fpetrola.cap.model.binders.implementations;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.DatabaseConnection;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.Property;

public class DatabaseEntitiesExtractor extends DefaultBinder<DatabaseConnection, EntityModel> implements BidirectionalBinder<DatabaseConnection, EntityModel> {

	public List<EntityModel> pull(DatabaseConnection dbConnection) {
		List<EntityModel> entities = new ArrayList<>();
		try {

			ResultSet resultSet = dbConnection.con.getMetaData().getTables(dbConnection.con.getCatalog(), null, null, new String[] { "TABLE" });

			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");

				ResultSetMetaData metaData = dbConnection.con.createStatement().executeQuery("select * from " + tableName).getMetaData();

				List<Property> properties = new ArrayList<>();
				for (int i = 0; i < metaData.getColumnCount(); i++)
					properties.add(new Property(metaData.getColumnName(i + 1), metaData.getColumnTypeName(i + 1)));

				entities.add(new EntityModel(tableName, properties));
			}
		} catch (Exception e) {
		}
		return entities;
	}

	public String getParametersProposalMessage() {
		return "I've found some new entities in database. Do you want to add them to your code?";
	}
}