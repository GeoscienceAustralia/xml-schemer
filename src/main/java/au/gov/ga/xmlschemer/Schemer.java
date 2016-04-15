package au.gov.ga.xmlschemer;

import java.io.File;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

public class Schemer {
    private static final Logger log = LoggerFactory.getLogger(Schemer.class);

    private static final String schemaCommandName = "schema";
    private static final String schematronCommandName = "schematron";

    private static class GlobalOptions {
        @Parameter(names = {"-h", "--help"}, description = "Print usage", help = true)
        public boolean help;
    }

    private static class CommandOptions {
        @Parameter(names = "--xml", description = "XML file to validate", arity = 1, required = true, validateWith = FileValidator.class)
        public String xmlFileName;

        @Parameter(names = "--catalog", description = "OASIS catalog file", arity = 1, validateWith = FileValidator.class)
        public String catalogFileName;
    }

    @Parameters(commandDescription = "Run schema validation")
    private static class SchemaCommandOptions extends CommandOptions {
        @Parameter(names = "--xsd", description = "XSD schema file", arity = 1, required = true, validateWith = FileValidator.class)
        public String xsdFileName;
    }

    @Parameters(commandDescription = "Run schematron validation")
    private static class SchematronCommandOptions extends CommandOptions {
        @Parameter(names = "--xslt", description = "Schematron XSLT file", arity = 1, required = true, validateWith = FileValidator.class)
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
            log.error(e.getMessage());
            if (!(e instanceof ParameterValidationException)) { 
                commander.usage();
            }
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
                List<Violation> violations = schemaValidator.validate(xmlFile);

                if (!violations.isEmpty()) {
                    for (Violation violation : violations) {
                        System.err.println(violation);
                    }
                    System.exit(1);
                }
            } else if (schematronCommandName.equals(commander.getParsedCommand())) {
                Source xsltFile = new StreamSource(schematronCommandOptions.xsltFileName);
                Source xmlFile = new StreamSource(schematronCommandOptions.xmlFileName);
                if (!new File(schematronCommandOptions.catalogFileName).exists()) {
                    abort("Catalog file " + schematronCommandOptions.catalogFileName + " does not exist.");
                }
                SchematronValidator schematronValidator = new SchematronValidator(xsltFile, schematronCommandOptions.catalogFileName);
                List<Violation> violations = schematronValidator.validate(xmlFile);

                if (!violations.isEmpty()) {
                    for (Violation violation : violations) {
                        System.err.println(violation);
                    }
                    System.exit(1);
                }
            } else {
                commander.usage();
            }
        }
        catch (Throwable e) {
            log.error("Unexpected error", e);
            System.exit(1);
        }
    }

    public static class FileValidator implements IParameterValidator {
        public void validate(String name, String value) throws ParameterException {
            if (!new File(value).exists()) {
                throw new ParameterValidationException("File " + value + " does not exist.");
            }
        }
    }

    public static class ParameterValidationException extends ParameterException {
        public ParameterValidationException(String message) {
            super(message);
        }
    }

    private static void abort(String message) {
        log.error(message);
        System.exit(1);
    }
}
