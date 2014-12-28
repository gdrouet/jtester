package com.github.gdrouet.jtester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This expectation checks that each possible result occurs at least once in 10 executions.
 */
public class AtLeastOneOccurrenceExpectation extends Expectation {

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param file the file with expected result
     */
    public AtLeastOneOccurrenceExpectation(final String file) {
        super(file, 10);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResultExpected(final String expected, final String... result) {
        final Matcher matcher = Pattern.compile(expected).matcher(String.join("", result));
        boolean match = false;

        while (matcher.find()) {
            match = true;

            for (int i = 0; i < matcher.groupCount(); i++) {
                if (matcher.group(i) == null) {
                    return false;
                }
            }
        }

        return match;
    }
}
