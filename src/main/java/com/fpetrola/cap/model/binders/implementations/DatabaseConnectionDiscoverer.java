package com.fpetrola.cap.model.binders.implementations;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.DatabaseConnection;

public class DatabaseConnectionDiscoverer extends DefaultBinder<Void, DatabaseConnection> implements BidirectionalBinder<Void, DatabaseConnection> {

	public String name;
	private DatabaseConnection dbConnection;
	static private List<DatabaseConnection> result = new ArrayList<DatabaseConnection>();

	public DatabaseConnectionDiscoverer() {
	}

	public List<DatabaseConnection> pull(Void source) {
		if (result.isEmpty()) {
			try {
				var driver = "com.mysql.jdbc.Driver";
				var connectionPrefix = "jdbc:mysql://localhost:3306/";
				var user = "root";
				var password = "test";

				Class.forName(driver);

				var con = DriverManager.getConnection(connectionPrefix, user, password);
				var rs = con.getMetaData().getCatalogs();

				var connection = connectionPrefix;
				while (rs.next()) {
					connection = connectionPrefix + rs.getString("TABLE_CAT");
				}
				con.close();

				dbConnection = new DatabaseConnection(driver, connection, user, password);
				result.add(dbConnection);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}