package au.gov.ga.xmlschemer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Schemer {

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
            Source xsdFile = new StreamSource(line.getOptionValue("xsd"));
            Source xmlFile = new StreamSource(line.getOptionValue("xml"));
            String catalogFile = line.getOptionValue("catalog");

            SchemaValidator schemaValidator = new SchemaValidator(xsdFile, catalogFile);
            List<String> violations = schemaValidator.validate(xmlFile);

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
}

