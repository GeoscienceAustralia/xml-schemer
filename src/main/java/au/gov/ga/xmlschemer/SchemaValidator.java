package au.gov.ga.xmlschemer;

import java.io.IOException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.xerces.util.XMLCatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class SchemaValidator {

    private final static Logger log = LoggerFactory.getLogger(SchemaValidator.class);

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

    public List<String> validate(Source xml) throws IOException {
        ParseErrorHandler errorHandler = new ParseErrorHandler();
        validator.setErrorHandler(errorHandler);
        try {
            validator.validate(xml);
        }
        catch (SAXException e) {
            // SAXException is thrown in case of a fatal error,
            // which would have already handled in ParseErrorHandler.
            log.warn("Fatal parsing error, already accounted for in " + ParseErrorHandler.class.getName(), e);
        }
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

