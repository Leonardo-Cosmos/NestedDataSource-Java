package org.pseudoscript.nesteddatasource;

import java.io.IOException;

import org.pseudoscript.nesteddatasource.exception.DataNotFoundException;
import org.pseudoscript.nesteddatasource.exception.IllegalKeyException;

public interface DataSource {
	
	String SEPARATOR = ".";
	
	Object get(String key) throws DataNotFoundException, IllegalKeyException;
	
	void set(String key, Object value) throws IllegalKeyException;
	
	void load() throws IOException;
	
	void save() throws IOException;
	
}
