package com.github.gdrouet.jtester;

import java.util.regex.Pattern;

/**
 * Expectation based on pattern matching.
 */
public class RegexMatchExpectation extends Expectation {

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     */
    public RegexMatchExpectation(final String file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResultExpected(final String e, final String s) {
        return Pattern.matches(e, s);
    }
}
