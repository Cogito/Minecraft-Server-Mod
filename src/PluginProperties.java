import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class PluginProperties {
	
	private String directory;
	private String name;
	private String tablePrefix;
	private boolean useMysql;
	private PropertiesFile globalProperties = new PropertiesFile("server.properties");
	private PropertiesFile pluginProperties;
	private Connection sqlConnection;

	public PluginProperties(String pluginName) {
		this(pluginName, true);
	}

	public PluginProperties(String name, boolean useMysql) {
		super();
		this.name = name;
		this.useMysql = useMysql?checkMysql():false;
		directory = "plugins/";
		tablePrefix = "plugin_";
	}
	
	protected boolean checkMysql(){
		String dataSource = globalProperties.getString("data-source");
		boolean usingMysql = dataSource.contentEquals("mysql");
		if(usingMysql){
			sqlConnection = etc.getSQLConnection();
		}
		return usingMysql && (sqlConnection != null);
	}
	
	protected void initialiseDataSource(){
		if(this.useMysql){
			// check mysql connection is still open
			if(!(this.useMysql = checkMysql())){
				// have to be careful here - don't want to loop indefinitely
				initialiseDataSource();
			} else {
				String[] field1 = {"user"};
				LinkedList<String[]> fields = new LinkedList<String[]>();
				fields.add(field1);
				createTable(this.sqlConnection, this.tablePrefix+this.name , fields);
			}	
		} else { //flatfile
			// might need seperate files for different tables
			this.pluginProperties = new PropertiesFile(this.directory+this.name+".txt");
		}
		
		

	}

	protected void createTable(Connection connection, String pluginTableName, List<String[]> tableFields) {
		try {
		    if(!tableExists(pluginTableName, connection)){
		    	// create the table
		    	Statement stmt = connection.createStatement();

		    	// Create table called my_table
		    	String sql = "CREATE TABLE "+pluginTableName+" "+sqlFieldConstructor(tableFields);
		    	stmt.executeUpdate(sql);

		    }
		} catch (SQLException e) {
		}
	}

	/**
	 * Generates a string of the form <code>(field1 field1param, field2 field2param)</code>
	 * that can be used for constructing sql tables
	 * 
	 * @param tableFields A list of table fields, where each field is a String array with
	 * 						at least the field name and data type. Must be not empty.
	 * @return
	 */
	private String sqlFieldConstructor(List<String[]> tableFields) {
		assert(!tableFields.isEmpty());
		Iterator<String[]> fields = tableFields.iterator();
		String[] field;
		String fieldConstructor = "(";
		while(fields.hasNext()){
			 field = fields.next();
			 assert(field.length >= 2); // need to do full error checking later
			 for(String fieldval : field)
				 fieldConstructor += fieldval + " ";
			 
			 if(fields.hasNext())
				 fieldConstructor += ", ";
		}
		fieldConstructor += ")";
		return fieldConstructor;
	}

	protected boolean tableExists(String pluginName, Connection connection) throws SQLException {
		// Gets the database metadata
		DatabaseMetaData dbmd = connection.getMetaData();

		// Specify the type of object; in this case we want tables
		String[] types = {"TABLE"};
		ResultSet resultSet = dbmd.getTables(null, null, "%", types);
		
		// Search for the table
		boolean tableFound = false;
		while (!tableFound && resultSet.next()) {
		    // Get the table name
		    String tableName = resultSet.getString(3);
		    if (tableName.equals(pluginName)) {
		    	tableFound = true;
		    }
		}
		return tableFound;
	}
	
	

}
