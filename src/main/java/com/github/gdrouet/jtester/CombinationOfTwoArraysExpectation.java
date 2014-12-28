package com.github.gdrouet.jtester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that an array's string representation is the combination of two others.
 */
public class CombinationOfTwoArraysExpectation extends Expectation {

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     */
    public CombinationOfTwoArraysExpectation(final String file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResultExpected(final String expected, final String ... result) {
        final Matcher matcher = Pattern.compile(expected).matcher(result[0]);
        int it = 0;

        while (matcher.find()) {
            it++;
            final String[] first = matcher.group(1).split(", ");
            final String[] second = matcher.group(2).split(", ");
            final String[] combine = matcher.group(3).split(", ");

            if ((first.length + second.length) != combine.length) {
                return false;
            }

            for (final String c : combine) {
                boolean find = false;

                for (final String f : first) {
                    if (c.equals(f)) {
                        find = true;
                    }
                }

                for (final String s : second) {
                    if (c.endsWith(s)) {
                        find = true;
                    }
                }

                if (!find) {
                    return false;
                }
            }

            return true;
        }

        return it != 0;
    }
}
