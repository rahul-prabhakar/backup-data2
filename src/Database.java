
public class Database {
	int id  =0;
	String dbName;
	String ip;
	String userName;
	String password;
	String schema;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserName() {
		return userName;
	}

	public Database(int id, String dbName, String ip, String schema, String userName, String password) {
		super();
		this.id = id;
		this.dbName = dbName;
		this.ip = ip;
		this.userName = userName;
		this.password = password;
		this.schema = schema;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
