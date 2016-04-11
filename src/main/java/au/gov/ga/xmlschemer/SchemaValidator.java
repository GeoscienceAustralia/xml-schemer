package au.gov.ga.xmlschemer;

import java.io.IOException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class SchemaValidator {

    private Validator validator;

    public SchemaValidator(Source xsd) throws SAXException {
        this(xsd, null);
    }

    public SchemaValidator(Source xsd, String catalogFileName) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (catalogFileName != null) { // TODO: check for ""
            factory.setResourceResolver(new Resolver(new String[]{catalogFileName}));
        }
        Schema schema = factory.newSchema(xsd);
        validator = schema.newValidator();
    }
    public List<String> validate(Source xml) throws SAXException, IOException {
        ParseErrorHandler errorHandler = new ParseErrorHandler();
        validator.setErrorHandler(errorHandler);
        validator.validate(xml);
        return errorHandler.getViolations();
    }

    public class Resolver implements LSResourceResolver {

        private XMLCatalogResolver resolver;

        public Resolver(String[] catalogs) {
            resolver = new XMLCatalogResolver(catalogs);
        }

        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            if (publicId == null && !systemId.startsWith("http://")) { // handle local schema includes
                systemId = namespaceURI + "/" + systemId;
            }
            return resolver.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
        }
    }
}
