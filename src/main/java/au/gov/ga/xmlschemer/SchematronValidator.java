package au.gov.ga.xmlschemer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltTransformer;

public class SchematronValidator {

    private Source schematron;
    private String catalogFileName;

    public SchematronValidator(Source schematron, String catalogFileName) {
        this.schematron = schematron;
        this.catalogFileName = catalogFileName;
    }

    public List<String> validate(Source xml) throws Exception {
        XsltTransformer transformer = new Processor(false).newXsltCompiler()
            .compile(schematron)
            .load();

        // TODO: consolidate with catalog loading in SchemaValidator
        CatalogManager catalogManager = new CatalogManager();
        catalogManager.setIgnoreMissingProperties(true);
        catalogManager.setCatalogFiles(catalogFileName);
        transformer.setURIResolver(new CatalogResolver(catalogManager));

        transformer.setSource(xml);
        Document document = new DocumentImpl();
        transformer.setDestination(new DOMDestination(document));

        transformer.transform();

        XPath path = XPathFactory.newInstance().newXPath();
        NodeList failedAsserts = (NodeList) path.evaluate("//*[local-name()='failed-assert']", document, XPathConstants.NODESET);

        List<String> violations = new ArrayList<>();

        for (int i = 0; i < failedAsserts.getLength(); i++) {
            Node failedAssert = failedAsserts.item(i);
            violations.add(
                    "location: " + path.evaluate("@location", failedAssert) + " " +
                    "message: "  + path.evaluate("text()",    failedAssert)
            );
        }
        return violations;
    }
}
