package org.pseudoscript.nesteddatasource.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pseudoscript.nesteddatasource.DataSource;
import org.pseudoscript.nesteddatasource.exception.DataNotFoundException;
import org.pseudoscript.nesteddatasource.exception.IllegalKeyException;

class DirDataSource implements DataSource {

	private static final Pattern FILE_DATA_SOURCE_NAME_PATTERN = Pattern.compile("([^\\.]+)(\\.[^\\.]+)?");
	
	private final Map<String, DirDataSource> dirDataSourceMap;
	private final Map<String, FileDataSource> fileDataSourceMap;

	protected final File file;
	
	public DirDataSource(File file) {
		dirDataSourceMap = new HashMap<>();
		fileDataSourceMap = new HashMap<>();

		if (!file.isDirectory()) {
			throw new IllegalArgumentException();
		}
		this.file = file;
	}

	@Override
	public Object get(String key) throws DataNotFoundException, IllegalKeyException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		SeparatedKey separatedKey = separateKey(key);		
		DataSource dataSource = findDataSource(separatedKey);
		return dataSource.get(separatedKey.innerKey);
	}

	@Override
	public void set(String key, Object value) throws IllegalKeyException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		SeparatedKey separatedKey = separateKey(key);
		DataSource dataSource = findDataSource(separatedKey);
		dataSource.set(separatedKey.innerKey, value);
	}

	@Override
	public void load() throws IOException {
		dirDataSourceMap.clear();
		fileDataSourceMap.clear();
		
		File[] files = file.listFiles();
		DataSource dataSource = null;
		for (File file : files) {
			try {
				dataSource = FileDataSourceFactory.newDataSource(file);
			} catch (FileNotFoundException ex) {
				
			}
			if (dataSource instanceof DirDataSource) {
				dirDataSourceMap.put(file.getName(), (DirDataSource) dataSource);
			} else {
				String fileName = file.getName();
				Matcher matcher = FILE_DATA_SOURCE_NAME_PATTERN.matcher(fileName);
				if (matcher.matches()) {
					fileDataSourceMap.put(matcher.group(1), (FileDataSource) dataSource);
				}
			}
		}
		
		for (Entry<String, FileDataSource> entry : fileDataSourceMap.entrySet()) {
			entry.getValue().load();
		}
		
		for (Entry<String, DirDataSource> entry : dirDataSourceMap.entrySet()) {
			entry.getValue().load();
		}
	}

	@Override
	public void save() throws IOException {
		for (Entry<String, FileDataSource> entry : fileDataSourceMap.entrySet()) {
			entry.getValue().save();
		}
		
		for (Entry<String, DirDataSource> entry : dirDataSourceMap.entrySet()) {
			entry.getValue().save();
		}
	}

	protected SeparatedKey separateKey(String key) throws IllegalKeyException {
		int index = key.indexOf(DataSource.SEPARATOR);
		
		SeparatedKey separatedKey = null;
		if (index == -1) {
			throw new IllegalKeyException(key, key + " is last level in directory.");
		} else if (index == 0) {
			throw new IllegalKeyException(key, "Key separator \"" + DataSource.SEPARATOR + 
					"\" cannot be start of key.");
		} else if (index == (key.length() - 1)) {
			throw new IllegalKeyException(key, "Key separator \"" + DataSource.SEPARATOR +
					"\" cannot be end of key.");	
		} else {
			separatedKey = new SeparatedKey();
			separatedKey.dataSourceKey = key.substring(0, index);
			separatedKey.innerKey = key.substring(index + 1);
			return separatedKey;
		}
	}
	
	protected DataSource findDataSource(SeparatedKey separatedKey) throws IllegalKeyException {
		String dataSourceKey = separatedKey.dataSourceKey;
		
		DirDataSource dirDataSource = dirDataSourceMap.get(dataSourceKey);
		if (dirDataSource != null) {
			if (!separatedKey.innerKey.contains(DataSource.SEPARATOR)) {
				throw new IllegalKeyException(separatedKey.innerKey, 
						String.format("\"%s\" exists, but directory cannot be the last level of key."));
			}
			return dirDataSource;
		}
		
		FileDataSource fileDataSource = fileDataSourceMap.get(dataSourceKey);
		if (fileDataSource != null) {
			return fileDataSource;
		}
		
		throw new IllegalKeyException(dataSourceKey, 
				String.format("Cannot find directory or file whose name is \"%s\"", dataSourceKey));
	}

	protected class SeparatedKey {
		public String dataSourceKey;
		public String innerKey;
	}
}
