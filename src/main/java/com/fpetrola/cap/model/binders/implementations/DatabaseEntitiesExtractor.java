package com.fpetrola.cap.model.binders.implementations;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.DatabaseConnection;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.Property;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.fpetrola.cap.model.source.SourceCodeModification;

public class DatabaseEntitiesExtractor extends DefaultBinder<DatabaseConnection, EntityModel> implements Binder<DatabaseConnection, EntityModel> {

	public List<EntityModel> pull(DatabaseConnection dbConnection) {
		var entities = new ArrayList<EntityModel>();
		try {
			SourceChangesListener sourceChangesListener = getSourceChangesListener();
			String uri = getWorkspacePath() != null ? "file://" + getWorkspacePath() + "/database.sql" : null;
//			String content1 = sourceChangesListener.getFileContent(uri);
//			String[] split = content1.split("\n");
//
//			for (String string : split) {
//				Statement stmt = dbConnection.con.createStatement();
//				stmt.executeUpdate(string);
//			}

			var resultSet = dbConnection.con.getMetaData().getTables(dbConnection.con.getCatalog(), null, null, new String[] { "TABLE" });

			while (resultSet.next()) {
				var tableName = resultSet.getString("TABLE_NAME");
				var metaData = dbConnection.con.createStatement().executeQuery("select * from " + tableName).getMetaData();

				var properties = IntStream.range(0, metaData.getColumnCount()).mapToObj(i -> {
					try {
						return new Property(metaData.getColumnName(i + 1), metaData.getColumnTypeName(i + 1));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}).collect(Collectors.toList());

				EntityModel entityModel = new EntityModel(tableName, properties);
				entityModel.setEntityModelListener((model, property, pm) -> {
					try {
						Statement stmt = dbConnection.con.createStatement();
						String sql = "ALTER TABLE " + tableName + " ADD " + property.name + " " + property.typeName + " NOT NULL";
//						stmt.executeUpdate(sql);

						if (uri != null) {
							String content = sourceChangesListener.getFileContent(uri);
							String newContent = content + "\n" + sql;

							SourceChange sourceChange = new SourceChange(uri);
							sourceChange.getInsertions().addAll(JavaSourceChangesHandler.createModificationsForUri(content, newContent, uri));
							getChangesLinker().addSourceChangeFor(pm, sourceChange);
						}
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				});

				entities.add(entityModel);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return entities;
	}

	public String getParametersProposalMessage() {
		return "I've found some new entities in database. Do you want to add them to your code?";
	}
}