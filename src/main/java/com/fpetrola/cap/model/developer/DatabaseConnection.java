package com.fpetrola.cap.model.developer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection implements DeveloperModel {
	public String driver;
	public String connection;
	public String user;
	public String password;
	public Connection con;

	public DatabaseConnection(String driver, String connection, String user, String password) {
		this.driver = driver;
		this.connection = connection;
		this.user = user;
		this.password = password;
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(connection, user, password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public DatabaseConnection() {
	}

	@Override
	public String toString() {
		return "DbConnection\n [" + connection + "]";
	}
}
