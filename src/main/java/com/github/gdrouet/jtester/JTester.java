package com.github.gdrouet.jtester;

import com.github.wuic.util.IOUtils;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is able to run tests. Steps are:
 * <ul>
 * <li>register tests for a particular set of .java file containing the actual file to test</li>
 * <li>compile the sources with some transformations to make it work</li>
 * <li>execute a compiled an identified 'main' method</li>
 * <li>compare output generated while test execution with an expected result contained in a separate file</li>
 * <li>report all tests results in a separate file</li>
 * </ul>
 */
public class JTester {

    /**
     * <p>
     * A registration provides a state for testing purpose. The test is identified with a step name. This name must
     * be the end of the directory containing the file to test. An environment is provided as a list of files to compile
     * with an identified test file containing a 'main' method to execute. The result of the execution must be
     * compared an expected value contained in a dedicated file. This file must be named with the name of the class to test.
     * </p>
     */
    private class Registration {

        /**
         * The file to test.
         */
        private final File testFile;

        /**
         * The file containing the main method to execute the test.
         */
        private final File testExecutorFile;

        /**
         * The file containing the result expected by the output of main method.
         */
        private final File expectationFile;

        /**
         * All the files containing the classes required by both test and executor files.
         */
        private final File[] environmentFiles;

        /**
         * The test step.
         */
        private final String step;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param test        the file to test
         * @param executor    the tester file
         * @param s           the test identifier
         * @param expectation the expected result
         * @param environment the file set to compile
         */
        private Registration(final File test,
                             final File executor,
                             final String s,
                             final File expectation,
                             final File[] environment) {
            testFile = test;
            step = s;
            testExecutorFile = executor;
            expectationFile = expectation;
            environmentFiles = environment;
        }
    }

    /**
     * Pattern for files to test.
     */
    public static final Pattern FILE_TO_TEST_PATTERN =
            Pattern.compile("([a-zA-Z]*).?([a-zA-Z]*).?(\\d*?).?([a-zA-Z]*).?([a-zA-Z]*).?(([a-zA-Z0-9]*)(\\.java))");

    /**
     * Directory containing files to test.
     */
    private final File testDirectory;

    /**
     * Directory containing expectations files.
     */
    private final File expectationDirectory;

    /**
     * Directory containing additional sources to execute test.
     */
    private final File environmentDirectory;

    /**
     * List of registered tests.
     */
    private final List<Registration> registrations;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param test        test directory
     * @param expectation expectation directory
     * @param environment environment directory
     */
    public JTester(final File test, final File expectation, final File environment) {
        testDirectory = test;
        expectationDirectory = expectation;
        environmentDirectory = environment;
        registrations = new ArrayList<>();

        if (testDirectory.isFile()) {
            throw new IllegalArgumentException(test.toString() + " must be a directory");
        }

        // TODO test if files are directories and if they exist
    }

    /**
     * <p>
     * Creates a new test registration.
     * </p>
     *
     * @param endDirectory    the end of directory name containing the files to test with the specified executor
     * @param executorFile    the source file with main method
     * @param expectationFile the file with expected result
     * @param testClass       some additional classes to satisfy executor/test file dependencies
     */
    public void addRegistration(final String endDirectory,
                                final String executorFile,
                                final String expectationFile,
                                final String... testClass) {
        // Look for directory with all files related to a particular test
        for (final File file : testDirectory.listFiles()) {

            // Directory found: it ends with the specified string
            if (file.getName().endsWith(endDirectory)) {

                // Collect absolute path for files to compile
                final File[] envFiles = new File[testClass.length];

                for (int i = 0; i < testClass.length; i++) {
                    envFiles[i] = new File(environmentDirectory, testClass[i]);
                }

                registrations.add(new Registration(
                        file,
                        new File(environmentDirectory, executorFile),
                        endDirectory,
                        new File(expectationDirectory, expectationFile),
                        envFiles));
                return;
            }
        }

        throw new IllegalArgumentException();
    }

    public void scanTest(final String[] args) throws IOException {
        // Execute all registered test for all discovered files to test
        for (final Registration registration : registrations) {
            final File step = new File(registration.testFile.getParentFile(), registration.step);
            File env;

            for (final File file : registration.testFile.listFiles()) {
                final Matcher matcher = FILE_TO_TEST_PATTERN.matcher(file.getName());
                if (matcher.find()) {
                    // The part of file name which represents the class name to test
                    final String fileTest = matcher.group(7);

                    // The student name
                    final String student = matcher.group(2);

                    // Create the directory containing compilation elements for current registration's test and current student
                    env = new File(step, student);
                    env.mkdirs();

                    // Will append all the source code to compile here
                    final StringBuilder compile = new StringBuilder();

                    // Build the list of files to compile: environment + executor file
                    final File[] compileFile = new File[registration.environmentFiles.length + 1];
                    System.arraycopy(registration.environmentFiles, 0, compileFile, 0, compileFile.length - 1);
                    compileFile[compileFile.length - 1] = registration.testExecutorFile;

                    // Load source code from file to compile and make some changes
                    for (final File f : compileFile) {
                        String content;

                        // Rename the class to test according to the name found in file to compile
                        final boolean isFileToTest = fileTest.startsWith(f.getName().substring(0, f.getName().lastIndexOf('.')));

                        // Read environment files except for the file to test which is substituted
                        try (final InputStream src = new FileInputStream(isFileToTest ? file : f)) {
                            content = IOUtils.readString(new InputStreamReader(src));
                            int index;

                            // Remove any package declaration, we work in default package
                            while ((index = content.indexOf("package")) != -1) {
                                content = content.substring(0, index) + content.substring(content.indexOf(';', index) + 1);
                            }

                            // No import is necessary in file to test, put others in top of file
                            while ((index = content.indexOf("import")) != -1) {
                                String importStatement = content.substring(index, content.indexOf(';', index) + 1);

                                if (!isFileToTest) {
                                    compile.insert(0, importStatement);
                                }

                                content = content.substring(0, index) + content.substring(content.indexOf(';', index) + 1);
                            }

                            index = -fileTest.length();

                            // Rename class to test declaration/references with name in environment
                            while ((index = content.indexOf(fileTest, index + fileTest.length())) != -1) {
                                // TODO : param replacement
                                content = content.substring(0, index) + registration.expectationFile.getName() + content.substring(index + fileTest.length());
                            }

                            // We put all classes in one file so we keep only the test executor class public
                            if (!registration.testExecutorFile.getName().equals(f.getName())) {
                                final String keyWord = "public class ";

                                while ((index = content.indexOf(keyWord)) != -1) {
                                    content = content.substring(0, index) + "class " + content.substring(index + keyWord.length());
                                }
                            }

                            compile.append(content);
                        }
                    }

                    // Copy source code to compile into a file with test executor name
                    final String executor = registration.testExecutorFile.getName();
                    final File path = new File(env, executor);

                    try (final OutputStream os = new FileOutputStream(path)) {
                        IOUtils.copyStreamIoe(new ByteArrayInputStream(compile.toString().getBytes()), os);
                    }

                    // Compile source code into student's directory for current registration's test
                    final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                    final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
                    final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(path.getAbsolutePath()));
                    final JavaCompiler.CompilationTask task = compiler.getTask(null,
                            fileManager,
                            diagnostics,
                            Arrays.asList("-d", env.getAbsolutePath()),
                            null,
                            compilationUnits);
                    final boolean success = task.call();

                    // compilation fails
                    if (!success) {
                        for (Diagnostic diagnostic : diagnostics.getDiagnostics())
                            System.out.format("Error on line %d in %s%n",
                                    diagnostic.getLineNumber(),
                                    diagnostic.getSource());
                    }

                    fileManager.close();

                    // Execute compiled code
                    if (success) {
                        try {
                            // Create a new class loader with the directory
                            final ClassLoader loader = new URLClassLoader(new URL[] { env.toURL() });

                            // Load in the classes
                            final Class clazz = loader.loadClass(executor.substring(0, executor.lastIndexOf('.')));
                            final  Class[] argTypes = {args.getClass(),};
                            final Object[] passedArgs = {args};

                            // Execute 'main' method
                            final Method main = clazz.getMethod("main", argTypes);
                            main.invoke(null, passedArgs);

                            // TODO: interceptor System.out to compare result with expectation file content
                            // TODO: make report file
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}