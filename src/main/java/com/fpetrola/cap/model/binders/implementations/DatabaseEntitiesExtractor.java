package com.fpetrola.cap.model.binders.implementations;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

public class DatabaseEntitiesExtractor extends DefaultBinder<DatabaseConnection, EntityModel> implements Binder<DatabaseConnection, EntityModel> {

	public List<EntityModel> pull(DatabaseConnection dbConnection) {
		var entities = new ArrayList<EntityModel>();
		try {
			SourceChangesListener sourceChangesListener = getSourceChangesListener();
			var uri = getWorkspacePath() != null ? "file://" + getWorkspacePath() + "/database.sql" : null;

			if (uri != null) {

				executeActiveQueries(dbConnection, sourceChangesListener, uri);

				var resultSet = dbConnection.con.getMetaData().getTables(dbConnection.con.getCatalog(), null, null, new String[] { "TABLE" });

				while (resultSet.next()) {
					var tableName = resultSet.getString("TABLE_NAME");
					var properties = createProperties(dbConnection, tableName);

					EntityModel entityModel = new EntityModel(tableName, properties);
					entityModel.setEntityModelListener((model, property, pm) -> {
						try {

							if (uri != null) {
								var properties2 = createProperties(dbConnection, tableName);
								boolean notExists = properties2.stream().noneMatch(p -> p.name.equals(property.name));

								if (notExists) {
									String content = sourceChangesListener.getFileContent(uri);
									String sql = "ALTER TABLE " + tableName + " ADD " + property.name + " " + property.typeName + " NOT NULL";
									String newContent = content + "\n" + sql;

									SourceChange sourceChange = new SourceChange(uri, "add column '" + property.name + "' to table '" + tableName + "'");
									sourceChange.getInsertions().addAll(JavaSourceChangesHandler.createModificationsForUri(content, newContent, uri));
									getChangesLinker().addSourceChangeFor(pm, sourceChange);
								}
							}
						} catch (SQLException e) {
							throw new RuntimeException(e);
						}
					});

					entities.add(entityModel);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return entities;
	}

	private List<Property> createProperties(DatabaseConnection dbConnection, String tableName) throws SQLException {
		var metaData = dbConnection.con.createStatement().executeQuery("select * from " + tableName).getMetaData();

		var properties = IntStream.range(0, metaData.getColumnCount()).mapToObj(i -> {
			try {
				return new Property(metaData.getColumnName(i + 1), metaData.getColumnTypeName(i + 1));
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		return properties;
	}

	private void executeActiveQueries(DatabaseConnection dbConnection, SourceChangesListener sourceChangesListener, String uri) throws SQLException {
		var content1 = sourceChangesListener.getFileContent(uri);
		var split = content1.split("\n");

		var modifiedLines = new ArrayList<String>();

		for (var string : split) {
			String sql = "";
			var stmt = dbConnection.con.createStatement();
			sql = string.trim();
			if (!sql.equals("") && !sql.startsWith("#")) {
				try {
					stmt.executeUpdate(sql);
				} catch (SQLException e) {
				}
				sql = "#" + sql;
			}

			modifiedLines.add(sql);
		}

		String content = modifiedLines.stream().collect(Collectors.joining("\n"));

		sourceChangesListener.fileCreation(uri, content);
	}

	public String getParametersProposalMessage() {
		return "I've found some new entities in database. Do you want to add them to your code?";
	}
}