package Utils;

import org.apache.commons.cli.*;

public class Parameters {

    static Options options;

    static {
        options = new Options();

        Option inputFile = new Option("i", "input", true, "Source file to analyse");
        Option symbol = new Option("s", "symbol", true, "Symbol to analyse");
        Option outputFile = new Option("o", "output", true, "Output file");

        inputFile.setRequired(true);
        symbol.setRequired(true);
        outputFile.setRequired(false);
        outputFile.setOptionalArg(true);

        options.addOption(inputFile).addOption(symbol).addOption(outputFile);
    }

    String inputFilePath;
    String outputFilePath;
    String symbolName;

    public String getInputFilePath() {
        return inputFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public Parameters(String[] args) {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("JS Symbol analyser", options);
            System.exit(-1);
        }

        inputFilePath = cmd.getOptionValue("input");
        symbolName = cmd.getOptionValue("symbol");
        outputFilePath = cmd.getOptionValue("output");
    }
}
