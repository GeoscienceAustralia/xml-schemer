package au.gov.ga.xmlschemer;

import org.junit.Test;

public class SchemerTest {

    private String getFile(String filename) {
        return Thread.currentThread().getContextClassLoader().getResource(filename).getFile();
    }

    @Test
    public void testValidate() throws Exception {
        String xsd = getFile("iso-19139-20070417/gmx/gmx.xsd");
        String xml = getFile("antenna-receiver-codelists.xml");
        String cat = getFile("catalog.xml");

        Schemer validator = new Schemer(xsd, cat);
        validator.validate(xml);
    }
}
