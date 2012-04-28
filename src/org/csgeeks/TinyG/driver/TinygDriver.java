package org.csgeeks.TinyG.driver;

import java.io.InputStream;

public interface TinygDriver {
	public RetCode connect();
	public RetCode disconnect();
	public void write(String message);
	public InputStream getInputStream();
	public boolean isReady();
}
