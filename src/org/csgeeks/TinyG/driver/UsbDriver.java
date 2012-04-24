package org.csgeeks.TinyG.driver;

import java.io.InputStream;

public class UsbDriver implements TinygDriver {

	public UsbDriver(String string) {
		// TODO Auto-generated constructor stub
	}

	public RetCode connect() {
		// TODO Auto-generated method stub
		return new RetCode(false, null);
	}

	public RetCode disconnect() {
		// TODO Auto-generated method stub
		return new RetCode(false, null);
	}

	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(String message) {
		// TODO Auto-generated method stub
		
	}

}
