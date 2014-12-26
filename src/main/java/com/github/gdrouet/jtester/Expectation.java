package com.github.gdrouet.jtester;

/**
 * An expectation checks that an expected result matches a specified value.
 */
public abstract class Expectation {

    /**
     * File containing the expected result.
     */
    private final String fileName;

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     */
    public Expectation(final String file) {
        fileName = file;
    }

    /**
     * Indicates if the given value matches the expected result.
     *
     * @param expected the expected result
     * @param result the result
     * @return {@code true} in case of match, {@code false} otherwise
     */
    public abstract boolean isResultExpected(String expected, String result);

    /**
     * Returns the file containing the expected result.
     *
     * @return the file
     */
    public String getFile() {
        return fileName;
    }
}
