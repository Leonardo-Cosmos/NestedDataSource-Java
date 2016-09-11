package org.pseudoscript.nesteddatasource.file;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.pseudoscript.nesteddatasource.DataSource;

class JsonDataSource extends FileDataSource {

	private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(?<name>[^\\.]+)\\.(?<extension>.+)");
	private static final String FILE_NAME_GROUP_NAME = "name";
	
	protected String rootObjectName;
	protected Object rootData;
	
	public JsonDataSource(File file) {
		super(file);
		
		Matcher matcher = FILE_NAME_PATTERN.matcher(file.getName());
		if (matcher.matches()) {
			rootObjectName = matcher.group(FILE_NAME_GROUP_NAME);
		}
	}

	@Override
	public void load() throws IOException {
		StringBuilder jsonBuilder = new StringBuilder();
		CharBuffer charBuffer = CharBuffer.allocate(0x400);
		Reader reader = null;
		try {
			reader = new FileReader(file);
			while (reader.read(charBuffer) != -1) {
				charBuffer.flip();
				jsonBuilder.append(charBuffer);
				charBuffer.rewind();
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
		loadData(jsonObject, "");
	}

	private void loadData(JSONObject jsonObject, String keyBase) {
		Iterator<String> keyIterator = jsonObject.keys();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			Object child = jsonObject.get(key);
			if (child instanceof JSONObject) {
				JSONObject childObject = jsonObject.getJSONObject(key);
				if (keyBase.isEmpty()) {
					loadData(childObject, key);
				} else {
					String childKeyBase = keyBase + DataSource.SEPARATOR + key;
					loadData(childObject, childKeyBase);
				}
			} else if (child instanceof String) {
				dataMap.put(keyBase, (String) child);
			}
		}
	}
	
	@Override
	public void save() throws IOException {
		JSONObject rootObject = new JSONObject();
		for (Entry<String, Object> entry : dataMap.entrySet()) {
			saveData(rootObject, entry.getKey(), entry.getValue().toString());
		}
		String jsonString = rootObject.toString();
		
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(jsonString);
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	private void saveData(JSONObject rootObject, String key, String value) {
		String[] parentObjectKeys = key.split(DataSource.SEPARATOR);
		JSONObject currentObject = rootObject;
		for (int i = 0; i < parentObjectKeys.length - 1; i++) {
			String parentObjectKey = parentObjectKeys[i];
			
			// Search matched object in child objects.
			JSONObject parentObject = null;
			if (currentObject.keySet().contains(parentObjectKey)) {
				parentObject = currentObject.getJSONObject(parentObjectKey);
			}
			
			// Create object for current level if it is not found.
			if (parentObject == null) {
				parentObject = new JSONObject();
				currentObject.put(parentObjectKey, parentObject);
			}
			
			// Enter next level.
			currentObject = parentObject;
		}
		
		String lastKey = parentObjectKeys[parentObjectKeys.length - 1];
		currentObject.put(lastKey, value);
	}
	
}
