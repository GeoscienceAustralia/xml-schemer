package au.gov.ga.xmlschemer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class Schemer {

    private Validator validator;

    public static void main(String[] args) {
        Option xsdFileOption = Option.builder(null)
            .type(String.class)
            .argName("xsd-file")
            .longOpt("xsd")
            .required()
            .hasArg()
            .desc("XSD file")
            .build();

        Option xmlFileOption = Option.builder(null)
            .type(String.class)
            .argName("xml-file")
            .longOpt("xml")
            .required()
            .hasArg()
            .desc("XML file")
            .build();

        Option catalogFileOption = Option.builder(null)
            .type(String.class)
            .longOpt("catalog")
            .argName("oasis-catalog-file")
            .hasArg()
            .desc("schema catalog file")
            .build();

        Options options = new Options();
        options.addOption(catalogFileOption);
        options.addOption(xsdFileOption);
        options.addOption(xmlFileOption);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);
            String xsdFile = line.getOptionValue("xsd");
            String xmlFile = line.getOptionValue("xml");
            String catalogFile = line.getOptionValue("catalog");

            Schemer schemer = new Schemer(xsdFile, catalogFile);
            List<String> violations = schemer.validate(xmlFile);

            if (!violations.isEmpty()) {
                for (String violation : violations) {
                    System.err.println(violation);
                }
                System.exit(1);
            }
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp(120, "schemer.sh", "", options, "", true);
            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Schemer(String xsd, String catalog) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (catalog != null) {
            factory.setResourceResolver(new Resolver(new String[]{catalog}));
        }
        Schema schema = factory.newSchema(new File(xsd));
        validator = schema.newValidator();
    }

    public List<String> validate(String xml) throws SAXException, IOException {
        ParseErrorHandler errorHandler = new ParseErrorHandler();
        validator.setErrorHandler(errorHandler);
        validator.validate(new StreamSource(xml));
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
            LSInput input = resolver.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
            return input;
        }
    }
}
