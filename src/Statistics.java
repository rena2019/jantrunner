import java.io.File;
import java.sql.*;

/**
 * Class to access SQLite database
 * @author ReNa2019 http://code.google.com/p/jantrunner/
 *
 */
public class Statistics {
	private static final String TABLE_DURATION = "execution";
	private String dbFilename;

	public Statistics(String dbFilename) throws Exception {
		this.dbFilename = dbFilename;
		if (!new File(dbFilename).exists())
			createTable();
	}
	
	/**
	 * main method to use the class from command line
	 * @param args db file name
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("please enter db file name");
			System.exit(1);
			return;
		}
		Statistics stat = new Statistics(args[0]);
		String sqlQuery = "";
		if (args.length >= 2) {
			sqlQuery = args[1];
		}
		stat.dumpTable(sqlQuery);
		/*
		// stat.insertSomeValues();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    //Date date = (Date) formatter.parseObject("2011-09-15 19:01:02");
	    
		stat.addValues(formatter.format(new Date()), "javascript.xml", "default", 3.5);
		Thread.sleep(2000);
		stat.addValues(formatter.format(new Date()), "javascript.xml", "default", 6.1);
		System.out.println("duration="
				+ stat.getLastDuration("javascript.xml", "default"));
		System.out.println("avg="
				+ stat.getAvarage("javascript.xml", "default"));
		*/
	}

	/**
	 * Get last execution duration for the given ant file/target
	 * @param antFilename
	 * @param target
	 * @return
	 * @throws Exception
	 */
	public double getLastDuration(String antFilename, String target)
			throws Exception {
		double duration = 0;
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
				+ this.dbFilename);
		Statement stat = conn.createStatement();

		ResultSet rs = stat.executeQuery("select * from " + TABLE_DURATION
				+ " where (file LIKE '" + antFilename + "' AND target LIKE '"
				+ target + "') ORDER by date DESC;");
		/*while (rs.next()) {
			System.out.println("date = " + rs.getString("date") + ", target = " + rs.getString("target")
					+ ", file = " + rs.getString("file") + "duration = "
					+ rs.getDouble("duration"));
			duration = rs.getDouble("duration");
		}*/
		duration = rs.getDouble("duration");
		rs.close();
		conn.close();
		return duration;
	}
	
	/**
	 * Dump the SQL table to the console.
	 * @throws Exception
	 */
	public void dumpTable(String query) throws Exception {
		Class.forName("org.sqlite.JDBC");
		String SEPARATOR = ";";
		String sqlQuery = "select * from " + TABLE_DURATION + " ORDER by date;";
		//use the given query if existing
		if (!query.equals("")) {
			sqlQuery = query;
		}
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
				+ this.dbFilename);
		Statement stat = conn.createStatement();

		ResultSet rs = stat.executeQuery(sqlQuery);
		System.out.println("date;file;target;duration");
		while (rs.next()) {
			System.out.println(rs.getString("date") + SEPARATOR + rs.getString("file")
					+ SEPARATOR + rs.getString("target") + SEPARATOR
					+ rs.getDouble("duration"));
		}
	}

	public void createTable() throws Exception {
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
				+ this.dbFilename);
		Statement stat = conn.createStatement();
		stat.executeUpdate("drop table if exists " + TABLE_DURATION + ";");
		stat.executeUpdate("create table " + TABLE_DURATION
				+ " (date, file, target, duration);");
	}

	public void addValues(String date, String antFilename, String target, double duration)
			throws Exception {
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
				+ this.dbFilename);
		Statement stat = conn.createStatement();
		PreparedStatement prep = conn
				.prepareStatement("insert into execution values (?, ?, ?, ?);");

		prep.setString(1, date);
		prep.setString(2, antFilename);
		prep.setString(3, target);
		prep.setDouble(4, duration);
		prep.addBatch();

		// conn.setAutoCommit(false);
		prep.executeBatch();
		conn.close();
	}

	/**
	 * Get the average execution time for the given ant file/task
	 * @param antFilename
	 * @param target
	 * @return duration in seconds
	 * @throws Exception
	 */
	private double getAverage(String antFilename, String target)
			throws Exception {
		int duration = 0;
		// sqlite Aggregate Functions
		// http://www.sqlite.org/lang_aggfunc.html#avg
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
				+ this.dbFilename);
		Statement stat = conn.createStatement();

		String select = " where (target='" + target + "' AND file = '"
				+ antFilename + "')";
		select = "";
		String func = "avg";
		ResultSet rs = stat.executeQuery("select " + func + "(duration) as '"
				+ func + "' from " + TABLE_DURATION + " " + select + ";");
		double ret = rs.getDouble(1);
		//System.out.println(func + " = " + ret);
		rs.close();
		conn.close();
		return ret;
	}
}
