package au.gov.ga.xmlschemer;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class ParseErrorHandler implements ErrorHandler {

    private List<String> violations = new ArrayList<String>();

    public void error(SAXParseException e) {
        violations.add(prepareMessage(e));
    }

    public void fatalError(SAXParseException e) {
        System.out.println("FATAL");
        violations.add(prepareMessage(e));
    }

    public void warning(SAXParseException e) {
        System.out.println(prepareMessage(e));
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
