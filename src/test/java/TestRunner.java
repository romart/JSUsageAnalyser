import java.io.File;
import java.io.IOException;

public class TestRunner {


    private static class TestCase {
        String source;
        String output;
        String symbol;

        TestCase(String src, String out, String smb) {
            this.source = src;
            this.output = out;
            this.symbol = smb;
        }

        public String getSource() {
            return source;
        }

        public String getOutput() {
            return output;
        }

        public String getSymbol() {
            return symbol;
        }
    }




    public static void main(String[] args) throws IOException {
        TestCase[] testCases = {
                new TestCase("ex01.js", "output01.txt", "foo"),
                new TestCase("ex02.js", "output02.txt", "A.prototype.bar"),
                new TestCase("ex03.js", "output03.txt", "A.prototype.foo"),
                new TestCase("ex04.js", "output04.txt", "B.prototype.foo"),
                new TestCase("ex05.js", "output05.txt", "A.prototype.bar"),

                new TestCase("ex1.js", "output1.txt", "foo"),
                new TestCase("ex2.js", "output2.txt", "A.prototype.bar"),
                new TestCase("ex3.js", "output3.txt", "A.prototype.foo"),
                new TestCase("ex4.js", "output4.txt", "B.prototype.foo"),
                new TestCase("ex5.js", "output5.txt", "A.prototype.bar"),
                new TestCase("ex6.js", "output6.txt", "A.prototype.foo"),
                new TestCase("ex7.js", "output7.txt", "A.prototype.foo"),
                new TestCase("ex8.js", "output8.txt", "A.prototype.foo"),
                new TestCase("ex9.js", "output9.txt", "A.prototype.foo"),
        };

        runTests(testCases);
    }

    private static void runTests(TestCase[] testCases) {

        for (TestCase test : testCases) {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome +
                    File.separator + "bin" +
                    File.separator + "java";
            String sourceDir = "src" + File.separator + "test" + File.separator + "Sources" + File.separator;
            String classpath = System.getProperty("java.class.path");
            String className = Main.class.getCanonicalName();

            ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, "-i", sourceDir + test.getSource(), "-s", test.getSymbol(), "-o", test.getOutput());

            try {
                Process process = builder.inheritIO().start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
