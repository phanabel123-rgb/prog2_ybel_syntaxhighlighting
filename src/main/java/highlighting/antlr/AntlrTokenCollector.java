package highlighting.antlr;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaColours;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.*;

// TODO Phase III — AntlrTokenCollector (token-based syntax highlighting).

// This highlighter uses the ANTLR-generated MiniJavaLexer to turn the input text into a token
// stream. {@code collectMatches(String)} is the only method you need to implement: extract tokens
// of interest and map them to {@code HighlightRegions} using the colours from {@code
// MiniJavaColours}. Sorting, filtering of invalid regions, and conflict handling are performed by
// the base class {@code SyntaxHighlighter} via the template method {@code computeRegions(...)}.
public class AntlrTokenCollector extends SyntaxHighlighter {

  // TODO (Phase III — implement this method): Use the token stream produced by the ANTLR-generated
  // {@code MiniJavaLexer} to collect highlight regions.
  //
  // Requirements / hints:
  // - Iterate over the lexer tokens (typically via {@code CommonTokenStream}); ignore the EOF
  // token.
  // - For each token type that should be coloured (e.g., keywords, string/char literals, comments),
  // create a {@code HighlightRegion} with the corresponding colour from {@code MiniJavaColours}.
  // - Use {@code Token#getStartIndex()} and {@code Token#getStopIndex()} (inclusive) to compute
  // {@code [start, end)} ranges: {@code start = startIndex, end = stopIndex + 1}.
  // - Do not sort, merge, or resolve overlaps here; return all candidates as you find them.
  // Normalisation and conflict resolution are handled later by the template method.
  // - Annotation highlighting: colour '@' and the immediately following IDENTIFIER token (if
  // present).
  @Override
  public List<HighlightRegion> collectMatches(String text) {
    List<HighlightRegion> regions = new ArrayList<>();

    var input = CharStreams.fromString(text);
    var lexer = new MiniJavaLexer(input);
    var tokens = new CommonTokenStream(lexer);

    tokens.fill();
    List<Token> tokenList = tokens.getTokens();

    for (int i = 0; i < tokenList.size(); i++) {
      Token t = tokenList.get(i);

      if (t.getType() == Token.EOF) {
        continue;
      }

      if (t.getType() == MiniJavaLexer.AT) {
        if (i + 1 < tokenList.size()
            && tokenList.get(i + 1).getType() == MiniJavaLexer.IDENTIFIER) {
          regions.add(
              new HighlightRegion(
                  t.getStartIndex(),
                  tokenList.get(i + 1).getStopIndex() + 1,
                  MiniJavaColours.ANNOTATION_COLOUR));
          i++;
        }

        continue;
      }



      Color colour = null;

      switch (t.getType()) {
        case MiniJavaLexer.PACKAGE:
        case MiniJavaLexer.IMPORT:
        case MiniJavaLexer.CLASS:
        case MiniJavaLexer.PUBLIC:
        case MiniJavaLexer.PRIVATE:
        case MiniJavaLexer.FINAL:
        case MiniJavaLexer.RETURN:
        case MiniJavaLexer.NULL:
        case MiniJavaLexer.NEW:
        case MiniJavaLexer.IF:
        case MiniJavaLexer.ELSE:
        case MiniJavaLexer.WHILE:
        case MiniJavaLexer.EXTENDS:
        case MiniJavaLexer.IMPLEMENTS:
          colour = MiniJavaColours.KEYWORD_COLOUR;
          break;
        case MiniJavaLexer.STRING_LITERAL:
          colour = MiniJavaColours.STRING_LITERAL_COLOUR;
          break;
        case MiniJavaLexer.CHAR_LITERAL:
          colour = MiniJavaColours.CHAR_LITERAL_COLOUR;
          break;
        case MiniJavaLexer.JAVADOC_COMMENT:
          colour = MiniJavaColours.JAVADOC_COMMENT_COLOUR;
          break;
        case MiniJavaLexer.BLOCK_COMMENT:
          colour = MiniJavaColours.BLOCK_COMMENT_COLOUR;
          break;
        case MiniJavaLexer.LINE_COMMENT:
          colour = MiniJavaColours.LINE_COMMENT_COLOUR;
          break;
      }
      if (colour != null) {
        regions.add(new HighlightRegion(t.getStartIndex(), t.getStopIndex() + 1, colour));
      }
    }

    return regions;
  }
}
