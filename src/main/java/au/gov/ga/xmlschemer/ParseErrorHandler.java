package au.gov.ga.xmlschemer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class ParseErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ParseErrorHandler.class);

    private List<String> violations = new ArrayList<String>();

    public void error(SAXParseException e) {
        violations.add(prepareMessage(e));
    }

    public void fatalError(SAXParseException e) {
        violations.add(prepareMessage(e));
    }

    public void warning(SAXParseException e) {
        log.warn(prepareMessage(e));
    }

    public boolean hasErrors() {
        return !violations.isEmpty();
    }

    public List<String> getViolations() {
        return violations;
    }

    private String prepareMessage(SAXParseException e) {
        return e.getSystemId() + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage();
    }
}
