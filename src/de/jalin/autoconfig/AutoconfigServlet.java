package de.jalin.autoconfig;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AutoconfigServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private Properties addresses;

	@Override
	public void init() throws ServletException {
		addresses = new Properties();
		try {
			addresses.load(getClass().getClassLoader().getResourceAsStream("emailaddress.properties"));
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	// <clientConfig version="1.1">
	// <emailProvider id="hostsharing.net">
	//   <domain>example.org</domain>
	//   <incomingServer type="imap">
	//     <hostname>xyz00.hostsharing.net</hostname>
	//     <port>993</port>
	//     <socketType>SSL</socketType>
	//     <username>xyz00-hugo</username>
	//     <authentication>password-cleartext</authentication>
	//   </incomingServer>
	//   <outgoingServer type="smtp">
	//     <hostname>xyz00.hostsharing.net</hostname>
	//     <port>465</port>
	//     <socketType>SSL</socketType>
	//     <username>xyz00-hugo</username>
	//     <authentication>password-cleartext</authentication>
	//     <addThisServer>true</addThisServer>
	//     <useGlobalPreferredServer>true</useGlobalPreferredServer>
	//   </outgoingServer>
	// </emailProvider>
	// </clientConfig>
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String emailAddress = req.getParameter("emailaddress");
		if (emailAddress == null) {
			throw new ServletException("emailaddress required");
		}
		int atCharacterPosition = emailAddress.indexOf('@');
		String requestURI = req.getRequestURI();
		boolean requestURIOk = "/mail/config-v1.1.xml".equals(requestURI);
		String userName = addresses.getProperty(emailAddress);
		if (requestURIOk && userName != null && userName.length() >= 5 
				&& atCharacterPosition > 0 && atCharacterPosition < emailAddress.length()) {
			String pacServer = userName.substring(0, 5) + ".hostsharing.net";
			String domainpart = emailAddress.substring(atCharacterPosition + 1);
			try {
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				Document document = builder.newDocument();
				Element clientConfig = document.createElement("clientConfig");
				clientConfig.setAttribute("version", "1.1");
				document.appendChild(clientConfig);
				Element emailProvider = appendElementWithAttribute(document, clientConfig, "emailProvider", "id", "hostsharing.net");
				appendElementWithText(document, emailProvider, "domain", domainpart);
				Element incomingServer = appendElementWithAttribute(document, emailProvider, "incomingServer", "type", "imap");
				appendElementWithText(document, incomingServer, "hostname", pacServer);
				appendElementWithText(document, incomingServer, "port", "993");
				appendElementWithText(document, incomingServer, "socketType", "SSL");
				appendElementWithText(document, incomingServer, "username", userName);
				appendElementWithText(document, incomingServer, "authentication", "password-cleartext");
				Element outgoingServer = appendElementWithAttribute(document, emailProvider, "outgoingServer", "type", "smtp");
				appendElementWithText(document, outgoingServer, "hostname", pacServer);
				appendElementWithText(document, outgoingServer, "port", "465");
				appendElementWithText(document, outgoingServer, "socketType", "SSL");
				appendElementWithText(document, outgoingServer, "username", userName);
				appendElementWithText(document, outgoingServer, "authentication", "password-cleartext");
				appendElementWithText(document, outgoingServer, "addThisServer", "true");
				appendElementWithText(document, outgoingServer, "useGlobalPreferredServer", "true");
				serializeDocument(document, resp);
			} catch (ParserConfigurationException e) {
				throw new ServletException(e);
			}
		} else {
			resp.sendError(404);
		}
	}

	private Element appendElementWithAttribute(Document document, Element parent,
			String childElementName, String attributeName, String attributeValue) {
		Element child = document.createElement(childElementName);
		child.setAttribute(attributeName, attributeValue);
		parent.appendChild(child);
		return child;
	}

	private Element appendElementWithText(Document document, Element parent, 
			String childElementName, String text) {
		Element child = document.createElement(childElementName);
		parent.appendChild(child);
		child.appendChild(document.createTextNode(text));
		return child;
	}

	private void serializeDocument(Document document, HttpServletResponse resp)
			throws ServletException {
		resp.setContentType("text/xml");
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(resp.getOutputStream()));
		} catch (TransformerException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
