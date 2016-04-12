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

/**
 * XML schema validation utility. Given a schema and optionally an OASIS catalog file,
 * return a list of schema violations. The code is thread-safe and re-entrant,
 * and you are encouraged to cache and re-use SchemaValidator objects to avoid re-parsing
 * the supplied XSD files.
 */
public class SchemaValidator {
    // Schema factories and validators in javax.xml.validation are not thread-safe,
    // so keep their instances local to methods.

    private final static Logger log = LoggerFactory.getLogger(SchemaValidator.class);

    // Schemas, on the other hand, are thread-safe.
    private Schema schema;

    public SchemaValidator(Source xsd) throws SAXException {
        this(xsd, null);
    }

    public SchemaValidator(Source xsd, String catalogFileName) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (catalogFileName != null) { // TODO: check for ""
            factory.setResourceResolver(new Resolver(new String[]{catalogFileName}));
        }
        schema = factory.newSchema(xsd);
    }

    public List<String> validate(Source xml) throws IOException {
        ParseErrorHandler errorHandler = new ParseErrorHandler();
        Validator validator = schema.newValidator();
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

    public static class Resolver implements LSResourceResolver {

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

