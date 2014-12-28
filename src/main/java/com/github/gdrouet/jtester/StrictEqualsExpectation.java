package com.github.gdrouet.jtester;

/**
 * Expectation based on {@link String#equals(Object)}.
 */
public class StrictEqualsExpectation extends Expectation {

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     */
    public StrictEqualsExpectation(final String file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResultExpected(final String e, final String ... s) {
        return e.equals(s[0]);
    }
}
