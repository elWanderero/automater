import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String winTestsDirPath = "./Lab2/tests";
    private static final String linuxTestsDirPath = "../tests";
    private static final String winGraphsDirPath = "./Lab2/graphs";
    private static final String linuxGraphsDirPath = "../graphs";
    private static final String winDotCommand = "dot"; //"\"C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot\"";
    private static final String linuxDotCommand = "dot";
    private static int fileCounter = 1;

    /*--------------------------------------------------------*
     |                    GRAPHVIZ STUFF                      |
     *--------------------------------------------------------*/

    // Takes a graphviz dot-compatible string, generates a .gv and then runs dot on it.
    @SuppressWarnings("Duplicates")
    public static void gvToFile(String gvStr, String filenameRoot) throws IOException {
        // Count lines in gvStr as a measure of size.
        Matcher matcher = Pattern.compile("\r\n|\r|\n").matcher(gvStr);
        int lineCount = 0;
        while (matcher.find()) lineCount++;
        // Dot runs fine up to a few thousand edges, but too big a graph is pointless
        if ( lineCount>500 ) {
            System.err.println("Graphviz file too big, dot aborted.");
            return;
        }

        if ( filenameRoot == null || filenameRoot.isEmpty() )
            filenameRoot = "graph_" + String.valueOf(fileCounter++);
        String gvFilename = filenameRoot + ".gv";
        String svgFilename = filenameRoot + ".svg";

        Writer writer = newGraphFileWriter(gvFilename);
        writer.write(gvStr);
        writer.close();
        runDot(gvFilename, svgFilename);
    }

    public static void gvToFile(String gvStr) throws IOException {
        gvToFile(gvStr, "");
    }

    private static Writer newGraphFileWriter(String graphName) throws FileNotFoundException, UnsupportedEncodingException {
        File dir = thisIsWindows() ? new File(winGraphsDirPath) : new File(linuxGraphsDirPath);
        File graphFile = new File(dir, graphName);
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(graphFile),
                        "utf-8"
                )
        );
    }

    private static void runDot(String gvFilename, String svgFilename) throws IOException {
        String graphsDir = thisIsWindows() ? winGraphsDirPath +"/" : linuxGraphsDirPath +"/";
        String dotCommand = thisIsWindows() ? winDotCommand : linuxDotCommand;
        String cmd = dotCommand + " -T svg -o " + graphsDir + svgFilename + " " + graphsDir + gvFilename;
        // Run cmd an bind process to variable to get output.
        Process proc = Runtime.getRuntime().exec(cmd);
        (new BufferedReader(new InputStreamReader(proc.getInputStream()))).lines().forEach(System.out::println);
        (new BufferedReader(new InputStreamReader(proc.getErrorStream()))).lines().forEach(System.err::println);
    }

    /*--------------------------------------------------------*
     |                     PREPROCESSING                      |
     *--------------------------------------------------------*/

    static String fixWeirdAssFlowGraphs(String weirdAssFG, String prefix) {
        // Remove internal method prefixes
        String ret = weirdAssFG.replaceAll(prefix+"-", "");
        return ret.replaceAll("(edge\\s+[^\\s]+\\s+[^\\s]+)\\s+[^\\s]+-[^\\s]+", "$1 eps" );
    }

    static String fixWeirdAssSpecs(String weirdAssFG, String prefix) {
        // Remove internal method prefixes
        String ret = weirdAssFG.replaceAll(prefix+"-", "");
        // Replace external calls with eps
        return ret.replaceAll("-java(-[^\\n]+)*->", "-eps->" );
    }

    /*--------------------------------------------------------*
     |                        OS STUFF                        |
     *--------------------------------------------------------*/

    static BufferedReader getTestReader(String testFileName) throws FileNotFoundException {
        File dir = thisIsWindows() ? new File(winTestsDirPath) : new File(linuxTestsDirPath);
        File testFile = new File(dir, testFileName);
        return new BufferedReader(new FileReader(testFile));
    }

    static File[] getFilesWithEnding(String dirName, String fileEnding) {
        File testsRoot = thisIsWindows() ? new File(winTestsDirPath) : new File(linuxTestsDirPath);
        File dir = new File(testsRoot, dirName);
        return dir.listFiles((dir1, filename) -> filename.endsWith(fileEnding));
    }

    private static boolean thisIsWindows() {
        return System.getProperty("os.name").equals("Windows 10");
    }

}
