package ticketsys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dao {
	// instance fields
	static Connection connect = null;
	Statement statement = null;

	// constructor
	public Dao() {
	  
	}

	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false" + "&user=fp411&password=411");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
		// variables for SQL Query table creations
		final String createTicketsTable = "CREATE TABLE zwats2_tickets("
				+ "ticket_id INT AUTO_INCREMENT PRIMARY KEY,"
				+ " ticket_opener VARCHAR(30), ticket_description VARCHAR(200),"
				+ " start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
				+ " close_date TIMESTAMP NULL)";
		final String createUsersTable = "CREATE TABLE zwats2_users("
				+ "uid INT AUTO_INCREMENT PRIMARY KEY,"
				+ " username VARCHAR(30),"
				+ " password VARCHAR(30),"
				+ " admin int)";

		try {

			// execute queries to create tables

			statement = getConnection().createStatement();

			statement.executeUpdate(createTicketsTable);
			statement.executeUpdate(createUsersTable);
			System.out.println("Created tables in given database...");

			// end create table
			// close connection/statement object
			//statement.close();
			connect.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// add users to user table
		addUsers();
	}

	public void addUsers() {
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		String sql;

		Statement statement;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)
		
		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("./userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB

			statement = getConnection().createStatement();

			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {

				sql = "insert into zwats2_users(username,password,admin) " + "values('" + rowData.get(0) + "'," + " '"
						+ rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			//statement.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public int insertRecords(String ticketName, String ticketDesc) {
		int id = 0;
		try {
			statement = getConnection().createStatement();
			statement.executeUpdate("Insert into zwats2_tickets" + "(ticket_opener, ticket_description) values(" + " '"
					+ ticketName + "','" + ticketDesc + "')", Statement.RETURN_GENERATED_KEYS);

			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next()) {
				// retrieve first field in table
				id = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;

	}

	public ResultSet readRecords() {

		ResultSet results = null;
		try {
			statement = connect.createStatement();
			if (Tickets.chkIfAdmin) {
				results = statement.executeQuery("SELECT * FROM zwats2_tickets");
			} else {
				results = statement.executeQuery("SELECT * FROM zwats2_tickets WHERE ticket_opener = '" + Login.username + "'");
			}
			
			//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	// continue coding for updateRecords implementation
	public void updateRecords(String item, String desc, int id) {
		try {
			statement = connect.createStatement();
			if (item == "desc") {
				// catch exceptions
				try {
					// for changing description
					if (Tickets.chkIfAdmin) {
						statement.executeUpdate("UPDATE zwats2_tickets SET ticket_description = '" + desc + "' WHERE ticket_id = " + id);

					} else {
						
						
						statement.executeUpdate("UPDATE zwats2_tickets "
								+ "SET ticket_description = '" + desc + "'" + 
								"WHERE ticket_opener = '" + Login.username + "' AND ticket_id = " + id);
						
					}
				}	catch (SQLException e) {
					System.out.println("An error has occured");
					System.out.println(e.getMessage());
				}
				
			}	// to reopen tickets
				else if ( item == "reopen")	{
				try {
					if (Tickets.chkIfAdmin) {
						statement.executeUpdate("UPDATE zwats2_tickets SET close_date = NULL WHERE ticket_id = " + id);
					} else {
						
						
						statement.executeUpdate("UPDATE zwats2_tickets "
								+ "SET close_date = NULL" + 
								"WHERE ticket_opener = '" + Login.username + "' AND ticket_id = " + id);
						
					}
				}	catch (SQLException e) {
					System.out.println("An error has occured");
					System.out.println(e.getMessage());
				}
			}
			
			
			if (Tickets.chkIfAdmin) {
				statement.executeUpdate("UPDATE zwats2_tickets SET close_date = NULL");
			} else {
				statement.executeUpdate("UPDATE zwats2_tickets "
						+ "SET WHERE ticket_opener = '" + Login.username + "' AND ticket_id = " + id);
			}
			//statement.close();
			//connect.close();
		} catch (SQLException e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}
		
	}
	// continue coding for deleteRecords implementation
	public void deleteRecords(int ticketNum) {
		try {
			statement = connect.createStatement();
			statement.executeUpdate("DELETE FROM zwats2_tickets WHERE ticket_id = " + ticketNum);
			System.out.println("Ticket ID: " + ticketNum + " has been deleted successfully.");
			// close connection/statement object
			//statement.close();
			//connect.close();
		} 
		catch  (SQLException e) {
			System.out.println("An error has occured");
			e.printStackTrace();
			
		}
		
	}
	public void closeRecords(int ticketNum) {
		try {
			statement = connect.createStatement();
			statement.executeUpdate("UPDATE zwats2_tickets SET close_date = CURRENT_TIMESTAMP WHERE ticket_id = " + ticketNum);
			System.out.println("Ticket ID " + ticketNum + " has been closed successfully");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
