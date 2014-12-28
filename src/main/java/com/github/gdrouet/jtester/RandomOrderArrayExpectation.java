package com.github.gdrouet.jtester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Expects an array does not contain an ordered suite of integers.
 */
public class RandomOrderArrayExpectation extends Expectation {

    /**
     * Builds a new instance.
     *
     * @param file the file with expected result
     */
    public RandomOrderArrayExpectation(final String file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResultExpected(final String expected, final String ... result) {
        final Matcher matcher = Pattern.compile(expected).matcher(result[0]);
        boolean match = false;

        while (matcher.find()) {
            match = true;
            final String[] array = matcher.group(1).split(", ");
            final int maxSequence = array.length / 10;
            int diff = 0;
            int seq = 0;

            for (int i = 1; i < array.length && seq < maxSequence; i++) {
                int it = Integer.parseInt(array[i]) - Integer.parseInt(array[i - 1]);

                if (it == diff) {
                    seq++;
                } else {
                    diff = it;
                    seq = 0;
                }
            }

            if (seq >= maxSequence) {
                return false;
            }
        }

        return match;
    }
}
