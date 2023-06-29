package dto;

import java.net.InetAddress;
import java.util.Objects;

public class User {
	private String name;

	private String password;

	private int id;

	private InetAddress ip;
	private boolean isOnline ;


	public User (String name,String password) {
			this.name = name;
			this.password = password;
	}

    public User (int id, String name,String password) {
		this.id = id;
		this.name = name;
		this.password = password;
    }

	public User(int id, String name,InetAddress ip ,boolean isOnline) {
		this.id = id;
		this.name = name;
		this.ip = ip;
		this.isOnline = isOnline;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User) o;
		return id == user.id && Objects.equals(name, user.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, id);
	}

	@Override
	public String toString() {
		if (ip != null) {
			String ipStr = ip.toString().substring(1); // 去掉第一个 /
			return  id + "," + name + "," + isOnline + "," + ipStr;
		} else {
			return  id + "," + name + "," + isOnline + "," + ip;
		}
	}

	public String loginOrRegisterStr() {
		return name + "," + password;
	}

}
