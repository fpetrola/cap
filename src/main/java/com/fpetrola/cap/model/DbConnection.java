package com.fpetrola.cap.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection implements DeveloperModel {
	public final String driver = "com.mysql.jdbc.Driver";

	@Override
	public String toString() {
		return "DbConnection\n [" + connection + "]";
	}

	public final String connection = "jdbc:mysql://localhost:3306/testdb";
	public final String user = "root";
	public final String password = "test";

	public Connection con;

	public DbConnection() {
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(connection, user, password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

}
