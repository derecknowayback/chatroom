package dto;


public class User {
	private String name;

	private String password;

	private int id;

	private boolean isOnline ;


	public User(String name, String ip, int id) {
		this.name = name;
		this.id = id;
	}

	public User (String name,String password) {
			this.name = name;
			this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}



	public boolean insertUser(){
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean online) {
		isOnline = online;
	}

	@Override
	public String toString() {
		return id + "," + name;
	}
}
