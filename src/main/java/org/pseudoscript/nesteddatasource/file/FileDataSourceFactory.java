package org.pseudoscript.nesteddatasource.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pseudoscript.nesteddatasource.DataSource;

public class FileDataSourceFactory {

	private static final Pattern xmlPattern = Pattern.compile(".+\\.[xX][mM][lL]");
	private static final Pattern jsonPattern = Pattern.compile(".+\\.[jJ][sS][oO][nN]");
	private static final Pattern csvPattern = Pattern.compile(".+\\.[cC][sS][vV]");
	private static final Pattern propertiesPattern = Pattern.compile(".+\\.[pP][rR][oO][pP][eE][rR][tT][iI][eE][sS]");

	public static boolean isXml(String fileName) {
		Matcher matcher = xmlPattern.matcher(fileName);
		return matcher.matches();
	}

	public static boolean isJson(String fileName) {
		Matcher matcher = jsonPattern.matcher(fileName);
		return matcher.matches();
	}
	
	public static boolean isCsv(String fileName) {
		Matcher matcher = csvPattern.matcher(fileName);
		return matcher.matches();
	}
	
	public static boolean isProperties(String fileName) {
		Matcher matcher = propertiesPattern.matcher(fileName);
		return matcher.matches();
	}

	public static DataSource newDataSource(String path) throws FileNotFoundException {
		File file = new File(path);
		return newDataSource(file);
	}
	
	public static DataSource newDataSource(File file) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException();
		}

		DataSource dataSource = null;
		String fileName = file.getName();
		if (file.isFile()) {
			if (isXml(fileName)) {
				dataSource = new XmlDataSource(file);
			} else if (isJson(fileName)) {
				dataSource = new JsonDataSource(file);
			} else if (isCsv(fileName)) {
				dataSource = new CsvDataSource(file);
			} else if (isProperties(fileName)) {
				dataSource = new PropertiesDataSource(file);
			} else {
				dataSource = new XmlDataSource(file);
			}
		} else {
			dataSource = new DirDataSource(file);
		}

		return dataSource;
	}

}
