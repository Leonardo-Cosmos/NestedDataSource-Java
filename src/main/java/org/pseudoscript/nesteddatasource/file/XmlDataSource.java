package org.pseudoscript.nesteddatasource.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pseudoscript.nesteddatasource.DataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class XmlDataSource extends FileDataSource {
	
	private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(?<name>[^\\.]+)\\.(?<extension>.+)");
	private static final String FILE_NAME_GROUP_NAME = "name";
	
	protected String rootELementName;
	
	public XmlDataSource(File file) {
		super(file);
		
		Matcher matcher = FILE_NAME_PATTERN.matcher(file.getName());
		if (matcher.matches()) {
			rootELementName = matcher.group(FILE_NAME_GROUP_NAME);
		}
	}

	@Override
	public void save() throws IOException {
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException ex) {
			throw new IOException("Fail to create document.", ex);
		}
		
		Element rootElement = document.createElement(rootELementName);
		document.appendChild(rootElement);
		
		for (Entry<String, Object> entry : dataMap.entrySet()) {
			saveData(document, rootElement, entry.getKey(), entry.getValue().toString());
		}
		
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			Transformer transformer = tf.newTransformer();
			DOMSource source = new DOMSource(document);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			PrintWriter pw = new PrintWriter(new FileOutputStream(this.file));
			StreamResult result = new StreamResult(pw);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException ex) {
			throw new IOException("Fail to write file.", ex);
		} catch (IllegalArgumentException ex) {
			throw new IOException("Fail to write file.", ex);
		} catch (FileNotFoundException ex) {
			throw new IOException("Fail to write file.", ex);
		} catch (TransformerException ex) {
			throw new IOException("Fail to write file.", ex);
		}
	}
	
	private void saveData(Document document, Element rootElement, String key, String value) {
		String[] parentELementNames = key.split(DataSource.SEPARATOR);
		Element currentElement = rootElement;
		for (String parentELementName : parentELementNames) {
			// Search matched element in child node list.
			NodeList nodeList = currentElement.getElementsByTagName(parentELementName);
			Element parentElement = null;
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (nodeList.item(i) instanceof Element) {
					parentElement = (Element) nodeList.item(i);
					break;
				}
			}
			
			// Create element for current level if it is not found.
			if (parentElement == null) {
				parentElement = document.createElement(parentELementName);
				currentElement.appendChild(parentElement);
			}
			
			currentElement = parentElement;
		}
		
		currentElement.appendChild(document.createTextNode(value));
	}
	
	@Override
	public void load() throws IOException {
		Document document = null;
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			document = builder.parse(this.file);			
		} catch (FileNotFoundException ex) {
			throw new IOException("Fail to read file.", ex);
		} catch (ParserConfigurationException ex) {
			throw new IOException("Fail to read file.", ex);
		} catch (SAXException ex) {
			throw new IOException("Fail to read file.", ex);
		} catch (IOException ex) {
			throw new IOException("Fail to read file.", ex);
		}
		
		Element rootElement = null;
		NodeList documentChildNodes = document.getChildNodes();
		for (int i = 0; i < documentChildNodes.getLength(); i++) {
			Node node = documentChildNodes.item(i);
			if (rootELementName.equals(node.getNodeName())) {
				rootElement = (Element) node;
				break;
			}
		}
		
		if (rootElement != null) {
			loadData(rootElement, "");
		}
	}
	
	private void loadData(Element dataElement, String keyBase) {
		NodeList childElementList = dataElement.getElementsByTagName("*");
		if (childElementList.getLength() > 0) {
			for (int i = 0; i < childElementList.getLength(); i++) {
				Node node = childElementList.item(i);
				Element element = (Element) node;
				if (keyBase.isEmpty()) {
					loadData(element, element.getNodeName());
				} else {
					String childKeyBase = keyBase + DataSource.SEPARATOR + element.getNodeName();
					loadData(element, childKeyBase);
				}
			}
		} else {
			dataMap.put(keyBase, dataElement.getTextContent());
		}
	}
}
