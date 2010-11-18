import java.sql.Connection;


public class PluginProperties {
	
	private String directory;
	private String name;
	private boolean useMysql;
	private PropertiesFile globalProperties = new PropertiesFile("server.properties");
	private Connection sqlConnection;

	public PluginProperties(String pluginName) {
		this(pluginName, true);
	}

	public PluginProperties(String name, boolean useMysql) {
		super();
		this.name = name;
		this.useMysql = useMysql?checkMysql():false;
		directory = "plugins/";
	}
	
	protected boolean checkMysql(){
		String dataSource = globalProperties.getString("data-source");
		boolean usingMysql = dataSource.contentEquals("mysql");
		if(usingMysql){
			sqlConnection = etc.getSQLConnection();
		}
		return usingMysql && (sqlConnection != null);
	}

}
