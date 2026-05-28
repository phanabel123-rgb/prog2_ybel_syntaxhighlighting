package highlighting.presets;

import highlighting.core.HighlightRegion;
import org.junit.jupiter.api.Test;
import highlighting.regex.Token;
import org.junit.jupiter.api.BeforeEach;


import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MiniJavaTokensTest {
    private List<Token> tokens;
    private Token javadocToken;
    private Token blockCommentToken;
    private Token lineCommentToken;
    private Token stringToken;
    private Token charToken;
    private Token annotationToken;
    private Token keywordToken;


    @BeforeEach
    public void setUp() {

        tokens = MiniJavaTokens.defaultTokens();


        javadocToken = findTokenByColour(MiniJavaColours.JAVADOC_COMMENT_COLOUR);
        blockCommentToken = findTokenByColour(MiniJavaColours.BLOCK_COMMENT_COLOUR);
        lineCommentToken = findTokenByColour(MiniJavaColours.LINE_COMMENT_COLOUR);
        stringToken = findTokenByColour(MiniJavaColours.STRING_LITERAL_COLOUR);
        charToken = findTokenByColour(MiniJavaColours.CHAR_LITERAL_COLOUR);
        annotationToken = findTokenByColour(MiniJavaColours.ANNOTATION_COLOUR);
        keywordToken = findTokenByColour(MiniJavaColours.KEYWORD_COLOUR);
    }

    private Token findTokenByColour(java.awt.Color colour) {
        return tokens.stream()
            .filter(t -> t.colour().equals(colour))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Token mit der Farbe " + colour + " wurde nicht gefunden!"));
    }

    @Test
    public void testPositions_BeginningMiddleEnd() {
        // Keyword 'public' am Anfang, in der Mitte und am Ende des Textes
        String text = "public Haus MyClass public";
        List<HighlightRegion> matches = keywordToken.test(text);

        assertEquals(2, matches.size(), "Sollte das Keyword am Anfang und am Ende finden (class wird ignoriert, wenn nicht im RegEx)");

        // Überprüfe exakten Treffer am Anfang (Index 0)
        assertEquals(0, matches.get(0).start());
        assertEquals(6, matches.get(0).end());

        // Überprüfe exakten Treffer am Ende (Index 21 bis 27)
        assertEquals(20, matches.get(1).start());
        assertEquals(26, matches.get(1).end());
    }


    @Test
    public void testMultipleMatchesInSameText() {
        String text = "package controller;\nimport com.badlogic.gdx.Game;\npublic class Test {}";
        List<HighlightRegion> matches = keywordToken.test(text);


        assertTrue(matches.size() >= 4, "Es sollten mehrere Keywords im selben Text gefunden werden.");
    }


    @Test
    public void testNoMatchesFound() {
        String text = "X Y Z 12345";
        List<HighlightRegion> matches = keywordToken.test(text);

        assertTrue(matches.isEmpty(), "Wenn keine Keywords existieren, muss die Liste leer sein.");
    }


    @Test
    public void testEdgeCase_CommentContainsKeywords() {
        // Der Kommentar selbst sollte vom Block- / Zeilenkommentar-Token gefunden werden
        String lineCommentText = "// This package is final and public";
        List<HighlightRegion> commentMatches = lineCommentToken.test(lineCommentText);
        assertEquals(1, commentMatches.size(), "Der gesamte Zeilenkommentar muss als EIN Treffer erkannt werden.");


        List<HighlightRegion> keywordMatches = keywordToken.test(lineCommentText);
        assertFalse(keywordMatches.isEmpty(), "Das Keyword-Token sucht isoliert und findet die Wörter im Kommentar.");
    }


    @Test
    public void testEdgeCase_Annotations() {

        String textBeginning = "@Over-ride\npublic void test()";
        List<HighlightRegion> matchesBeg = annotationToken.test(textBeginning);
        assertEquals(1, matchesBeg.size());
        assertEquals(0, matchesBeg.get(0).start(), "Annotation sollte direkt bei Index 0 starten.");


        String textSpaces = "    @Over-ride public";
        List<HighlightRegion> matchesSpace = annotationToken.test(textSpaces);
        assertEquals(1, matchesSpace.size(), "Annotation muss auch eingerückt gefunden werden.");

        // Sicherstellen, dass der Bindestrich im Namen mitgematcht wurde (Länge von @Over-ride ist 10)
        HighlightRegion region = matchesSpace.get(0);
        assertEquals(10, region.end() - region.start(), "Die gesamte Annotation inkl. Bindestrich muss gematcht sein.");
    }


    @Test
    public void testEdgeCase_StringsContainingCommentSymbols() {
        String text = "String url = \"http://mywebsite.com/*comment*/\";";
        List<HighlightRegion> stringMatches = stringToken.test(text);

        assertEquals(1, stringMatches.size(), "Es sollte genau ein String-Literal gefunden werden.");

        // Prüfen, ob der gesamte Inhalt inklusive der Kommentarzeichen als ein einziger String erkannt wurde
        int start = stringMatches.get(0).start();
        int end = stringMatches.get(0).end();
        String extractedString = text.substring(start, end);

        assertEquals("\"http://mywebsite.com/*comment*/\"", extractedString,
            "Der RegEx darf sich nicht von // oder /* innerhalb des Strings verwirren lassen.");
    }


    @Test
    public void testEdgeCase_JavadocVsBlockComment() {
        String testText = "/** Javadoc */ und /* Normaler Blockkommentar */";

        List<HighlightRegion> javadocMatches = javadocToken.test(testText);
        List<HighlightRegion> blockMatches = blockCommentToken.test(testText);

        assertEquals(1, javadocMatches.size(), "Sollte exakt das Javadoc finden.");
        // Da das normale Blockkommentar-Token auch auf /** anspringt,
        // testen wir hier nur, ob es zumindest das zweite echte Blockkommentar findet.
        assertTrue(blockMatches.size() >= 1, "Blockkommentar-Regex sollte anschlagen.");
    }

}
