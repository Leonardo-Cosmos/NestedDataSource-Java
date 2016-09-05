package org.pseudoscript.nesteddatasource.file;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pseudoscript.nesteddatasource.DataSource;
import org.pseudoscript.nesteddatasource.exception.DataNotFoundException;
import org.pseudoscript.nesteddatasource.exception.IllegalKeyException;

public abstract class FileDataSource implements DataSource {

	protected final Map<String, Object> dataMap;
	
	protected final File file;
	
	private boolean isChanged = false;
	
	public FileDataSource(File file) {
		dataMap = new LinkedHashMap<>();
		
		if (!file.isFile()) {
			throw new IllegalArgumentException();
		}
		this.file = file;
	}
	
	@Override
	public Object get(String key) throws DataNotFoundException, IllegalKeyException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		if (key.startsWith(DataSource.SEPARATOR)) {
			throw new IllegalKeyException(key, "Key separator \"" + DataSource.SEPARATOR + 
					"\" cannot be start of key.");
		}
		
		if (key.endsWith(DataSource.SEPARATOR)) {
			throw new IllegalKeyException(key, "Key separator \"" + DataSource.SEPARATOR +
					"\" cannot be end of key.");
		}
		
		Object value = dataMap.get(key);
		if (value == null) {
			throw new DataNotFoundException(key, 
					String.format("Data of \"%s\" is not found.", key));
		}
		return value;
	}

	@Override
	public void set(String key, Object value) {
		isChanged = true;
		dataMap.put(key, value);
	}
	
	public boolean isChanged() {
		return this.isChanged;
	}
}
