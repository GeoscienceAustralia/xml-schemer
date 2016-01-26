package au.gov.ga.xmlschemer;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class Schemer {

    private Validator validator;

    public Schemer(String xsd, String... catalogs) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new Resolver(catalogs));
        Schema schema = factory.newSchema(new File(xsd));
        validator = schema.newValidator();
        if (catalogs.length > 0) {
            validator.setResourceResolver(new Resolver(catalogs));
        }
    }

    public void validate(String xml) throws SAXException, IOException {
        validator.validate(new StreamSource(xml));
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
            LSInput input = resolver.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
            return input;
        }
    }
}
