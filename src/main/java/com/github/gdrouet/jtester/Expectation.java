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
     * Number of test execution.
     */
    private final int numberOfExecutions;

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     * @param noe the number of test executions
     */
    public Expectation(final String file, final int noe) {
        fileName = file;
        numberOfExecutions = noe;
    }

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     */
    public Expectation(final String file) {
        this(file, 1);
    }

    /**
     * Indicates if the given value matches the expected result.
     *
     * @param expected the expected result
     * @param result the result (one string per execution)
     * @return {@code true} in case of match, {@code false} otherwise
     */
    public abstract boolean isResultExpected(String expected, String ... result);

    /**
     * Returns the file containing the expected result.
     *
     * @return the file
     */
    public String getFile() {
        return fileName;
    }

    /**
     * <p>
     * Gets the number of required executions.
     * </p>
     *
     * @return the number of test execution
     */
    public int getNumberOfExecutions() {
        return numberOfExecutions;
    }
}
