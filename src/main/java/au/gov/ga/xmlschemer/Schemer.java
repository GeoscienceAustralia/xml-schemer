package au.gov.ga.xmlschemer;

import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

public class Schemer {

    private static final String schemaCommandName = "schema";
    private static final String schematronCommandName = "schematron";

    private static class GlobalOptions {
        @Parameter(names = {"-h", "--help"}, description = "Print usage", help = true)
        public boolean help;
    }

    private static class CommandOptions {
        @Parameter(names = "--xml", description = "XML file to validate", arity = 1, required = true)
        public String xmlFileName;

        @Parameter(names = "--catalog", description = "OASIS catalog file", arity = 1)
        public String catalogFileName;
    }

    @Parameters(commandDescription = "Run schema validation")
    private static class SchemaCommandOptions extends CommandOptions {
        @Parameter(names = "--xsd", description = "XSD schema file", arity = 1, required = true)
        public String xsdFileName;
    }

    @Parameters(commandDescription = "Run schematron validation")
    private static class SchematronCommandOptions extends CommandOptions {
        @Parameter(names = "--xslt", description = "Schematron XSLT file", arity = 1, required = true)
        public String xsltFileName;
    }

    public static void main(String[] args) {

        GlobalOptions globalOptions = new GlobalOptions();
        JCommander commander = new JCommander(globalOptions);
        commander.setProgramName("schemer.sh");
        SchemaCommandOptions schemaCommandOptions = new SchemaCommandOptions();
        commander.addCommand(schemaCommandName, schemaCommandOptions);
        SchematronCommandOptions schematronCommandOptions = new SchematronCommandOptions();
        commander.addCommand(schematronCommandName, schematronCommandOptions);
        try {
            commander.parse(args);
        }
        catch (ParameterException e) {
            System.err.println(e.getMessage());
            commander.usage();
            System.exit(1);
        }
        try {
            if (globalOptions.help) {
                commander.usage();
            } else if (schemaCommandName.equals(commander.getParsedCommand())) {
                Source xsdFile = new StreamSource(schemaCommandOptions.xsdFileName);
                Source xmlFile = new StreamSource(schemaCommandOptions.xmlFileName);
                String catalogFile = schemaCommandOptions.catalogFileName;

                SchemaValidator schemaValidator = new SchemaValidator(xsdFile, catalogFile);
                List<String> violations = schemaValidator.validate(xmlFile);

                if (!violations.isEmpty()) {
                    for (String violation : violations) {
                        System.err.println(violation);
                    }
                    System.exit(1);
                }
            } else if (schematronCommandName.equals(commander.getParsedCommand())) {
                Source xsltFile = new StreamSource(schematronCommandOptions.xsltFileName);
                Source xmlFile = new StreamSource(schematronCommandOptions.xmlFileName);
                String catalogFile = schematronCommandOptions.catalogFileName;

                SchematronValidator schematronValidator = new SchematronValidator(xsltFile, catalogFile);
                List<String> violations = schematronValidator.validate(xmlFile);

                if (!violations.isEmpty()) {
                    for (String violation : violations) {
                        System.err.println(violation);
                    }
                    System.exit(1);
                }
            } else {
                commander.usage();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

