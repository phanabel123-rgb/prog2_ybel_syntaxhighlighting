package highlighting;

import highlighting.antlr.*;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.Texts;
import highlighting.regex.*;
import highlighting.ui.EditorUI;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Main {

  public static void main(String... args) {
    // Phase I: RegexHighlighter
    SyntaxHighlighter regex = new RegexHighlighter();

    // Phase II: ScanningHighlighter
    // SyntaxHighlighter scanning = new ScanningHighlighter();

    // Phase III: AntlrTokenCollector (tokenbasiert)
    SyntaxHighlighter antlrToken = new AntlrTokenCollector();

    // and go ...
    // EditorUI.show(Texts.START_TEXT, regex);
    // EditorUI.show(Texts.START_TEXT, scanning);
    EditorUI.show(Texts.START_TEXT, antlrToken);

    var input = CharStreams.fromString(Texts.START_TEXT);
    var lexer = new MiniJavaLexer(input);
    var tokens = new CommonTokenStream(lexer);
    var parser = new MiniJavaParser(tokens);


    MiniJavaParser.CompilationUnitContext parseTree = parser.compilationUnit();


    PrettyPrinterVisitor prettyPrinter = new PrettyPrinterVisitor(4);
    prettyPrinter.visit(parseTree);


    System.out.println("\n--- Pretty Printed Result ---");
    System.out.println(prettyPrinter.result());
  }
}
