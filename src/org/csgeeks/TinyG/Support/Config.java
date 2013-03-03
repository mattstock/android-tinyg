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
		axisVals.add(new TinyGType("tm", "float"));
		axisVals.add(new TinyGType("vm", "float"));
		axisVals.add(new TinyGType("jm", "float"));
		axisVals.add(new TinyGType("jd", "float"));
		axisVals.add(new TinyGType("ra", "float"));
		axisVals.add(new TinyGType("fr", "float"));
		axisVals.add(new TinyGType("am", "int"));
		axisVals.add(new TinyGType("sv", "float"));
		axisVals.add(new TinyGType("lv", "float"));
		axisVals.add(new TinyGType("sn", "int"));
		axisVals.add(new TinyGType("sx", "int"));
		axisVals.add(new TinyGType("zb", "float"));
		sysVals.add(new TinyGType("fb", "float"));
		sysVals.add(new TinyGType("fv", "float"));
		sysVals.add(new TinyGType("hv", "int"));
		sysVals.add(new TinyGType("id", "string"));
		sysVals.add(new TinyGType("ja", "float"));
		sysVals.add(new TinyGType("ct", "float"));
		sysVals.add(new TinyGType("st", "int"));
		sysVals.add(new TinyGType("ej", "boolean"));
		sysVals.add(new TinyGType("jv", "int"));
		sysVals.add(new TinyGType("tv", "int"));
		sysVals.add(new TinyGType("qv", "int"));
		sysVals.add(new TinyGType("sv", "int"));
		sysVals.add(new TinyGType("si", "int"));
		sysVals.add(new TinyGType("ic", "boolean"));
		sysVals.add(new TinyGType("ec", "boolean"));
		sysVals.add(new TinyGType("ee", "boolean"));
		sysVals.add(new TinyGType("ex", "boolean"));
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
