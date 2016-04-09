package au.gov.ga.xmlschemer;

import java.util.List;

import org.junit.Assert;

public class Asserts {

    public static void assertNoViolations(List<String> violations) throws AssertionError {
        assertViolations(violations, 0);
    }

    public static void assertViolations(List<String> violations, int n) throws AssertionError {
        if (violations.size() != n) {
            violations.forEach(v -> { System.out.println(v); });
        }
        Assert.assertEquals(n, violations.size());
    }
}
