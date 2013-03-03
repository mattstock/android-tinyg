package org.csgeeks.TinyG.Support;

import java.util.HashSet;
import java.util.Set;

public class Config {
	private static final Set<TinyGType> axisVals = new HashSet<TinyGType>();
	private static final Set<TinyGType> motorVals = new HashSet<TinyGType>();
	private static final Set<TinyGType> sysVals = new HashSet<TinyGType>();
	
	// Eventually pull this from resources?  Multiple column implementation in android is a bit hackish.
	public Config() {
		motorVals.add(new TinyGType("tr", "float"));
		motorVals.add(new TinyGType("sa", "float"));
		motorVals.add(new TinyGType("mi", "int"));
		motorVals.add(new TinyGType("po", "boolean"));
		motorVals.add(new TinyGType("pm", "boolean"));
		motorVals.add(new TinyGType("ma", "int"));
	}
	
	public Set<TinyGType> getMotor() {
		return motorVals;
	}
	
	public Set<TinyGType> getAxis() {
		return axisVals;
	}
	
	public Set<TinyGType> getSys() {
		return sysVals;
	}
	
	public class TinyGType {
		public String name;
		public String type;
		
		private TinyGType(String name, String type) {
			this.name = name;
			this.type = type;
		}
	}
}
