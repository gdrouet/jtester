package com.github.gdrouet.jtester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that each array's string representation shows an ordered integers range.
 */
public class IncrementalArrayExpectation extends Expectation {

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     */
    public IncrementalArrayExpectation(final String file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResultExpected(final String expected, final String ... result) {
        final Matcher matcher = Pattern.compile(expected).matcher(result[0]);

        while (matcher.find()) {
            final String[] array = matcher.group(1).split(", ");

            for (int i = 1; i < array.length; i++) {
                if (Integer.parseInt(array[i]) < Integer.parseInt(array[i - 1])) {
                    return false;
                }
            }
        }

        return true;
    }
}
