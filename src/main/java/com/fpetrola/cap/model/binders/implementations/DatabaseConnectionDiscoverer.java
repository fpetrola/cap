package com.fpetrola.cap.model.binders.implementations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.DatabaseConnection;

public class DatabaseConnectionDiscoverer extends DefaultBinder<Object, DatabaseConnection, Object> implements BidirectionalBinder<Object, DatabaseConnection> {

	public String name;

	public DatabaseConnectionDiscoverer() {
	}

	public List<DatabaseConnection> pull(Object source) {
		List<DatabaseConnection> result = new ArrayList<DatabaseConnection>();
		try {
			String driver = "com.mysql.jdbc.Driver";
			String connectionPrefix = "jdbc:mysql://localhost:3306/";
			String user = "root";
			String password = "test";

			Class.forName(driver);

			Connection con = DriverManager.getConnection(connectionPrefix, user, password);
			ResultSet rs = con.getMetaData().getCatalogs();

			String connection = connectionPrefix;
			while (rs.next()) {
				connection = connectionPrefix + rs.getString("TABLE_CAT");
			}
			con.close();
			DatabaseConnection dbConnection = new DatabaseConnection(driver, connection, user, password);
			result.add(dbConnection);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}
}