/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.io.Serializable;

class AntTarget implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String file;
	public String target;
	public String name;
	public String description;
	
	AntTarget(String file, String target) {
		this.file = file;
		this.target = target;
		//TODO parse description
	}
	
	AntTarget(String name, String file, String target, String description) {
		this.name = name;
		this.file = file;
		this.target = target;
		this.description = description;
	}
	public String toString() {
		if (name != null && !name.equals(""))
			return name;
		return target;
	}
}