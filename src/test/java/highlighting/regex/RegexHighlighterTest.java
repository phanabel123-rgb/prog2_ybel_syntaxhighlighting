package highlighting.regex;

import static org.junit.jupiter.api.Assertions.*;

import highlighting.core.HighlightRegion;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegexHighlighterTest {

  private RegexHighlighter highlighter;

  @BeforeEach
  public void setUp() {
    highlighter = new RegexHighlighter();
  }

  @Test
  public void testSimpleCasesWithoutOverlaps() {
    String text = "package class"; // Zwei separate Keywords hintereinander

    List<HighlightRegion> rawMatches = highlighter.collectMatches(text);
    List<HighlightRegion> resolvedMatches = highlighter.resolveConflicts(rawMatches);

    // Es müssen beide erhalten bleiben
    assertEquals(
        2, resolvedMatches.size(), "Beide konfliktfreien Keywords müssen erhalten bleiben.");

    // Erste Region prüfen ('package' geht von Index 0 bis 7)
    assertEquals(0, resolvedMatches.get(0).start());
    assertEquals(7, resolvedMatches.get(0).end());

    // Zweite Region prüfen ('class' geht von Index 8 bis 13)
    assertEquals(8, resolvedMatches.get(1).start());
    assertEquals(13, resolvedMatches.get(1).end());
  }

  @Test
  public void testOverlappingKeywordInsideComment() {
    String text = "// public class Test"; // Ein Zeilenkommentar, der Keywords enthält

    List<HighlightRegion> rawMatches = highlighter.collectMatches(text);
    List<HighlightRegion> resolvedMatches = highlighter.resolveConflicts(rawMatches);

    // Nur der Zeilenkommentar (von Index 0 bis 20) darf erhalten bleiben
    assertEquals(
        1,
        resolvedMatches.size(),
        "Keywords im Kommentar müssen komplett entfernt werden. Nur der Kommentar darf bleiben.");

    assertEquals(0, resolvedMatches.get(0).start());
    assertEquals(20, resolvedMatches.get(0).end());
  }

  @Test
  public void testJavadocVsBlockComment() {
    String text = "/** Javadoc */"; // Matched Javadoc und normalen Blockkommentar

    List<HighlightRegion> rawMatches = highlighter.collectMatches(text);
    List<HighlightRegion> resolvedMatches = highlighter.resolveConflicts(rawMatches);

    assertEquals(
        1,
        resolvedMatches.size(),
        "Bei Javadoc darf der normale Blockkommentar-Regex die Region nicht überschreiben.");

    assertEquals(0, resolvedMatches.get(0).start());
    assertEquals(14, resolvedMatches.get(0).end());
  }

  @Test
  public void testAdjacentRegions() {
    // Wir erstellen manuell zwei direkt angrenzende Regionen
    List<HighlightRegion> adjacentRegions = new ArrayList<>();
    adjacentRegions.add(new HighlightRegion(0, 5, Color.BLUE));
    adjacentRegions.add(new HighlightRegion(5, 10, Color.ORANGE));

    List<HighlightRegion> resolved = highlighter.resolveConflicts(adjacentRegions);

    // Beide müssen erhalten bleiben
    assertEquals(
        2,
        resolved.size(),
        "Direkt aufeinanderfolgende Regionen (Grenzberührung) überlappen nicht und müssen beide"
            + " bleiben.");

    assertEquals(0, resolved.get(0).start());
    assertEquals(5, resolved.get(0).end());
    assertEquals(5, resolved.get(1).start());
    assertEquals(10, resolved.get(1).end());
  }

  @Test
  public void testEmptyStringAndNoMatches() {
    // Fall A: Komplett leerer String
    List<HighlightRegion> emptyMatches = highlighter.collectMatches("");
    List<HighlightRegion> emptyResolved = highlighter.resolveConflicts(emptyMatches);

    assertTrue(emptyMatches.isEmpty(), "Bei leerem Text darf collectMatches nichts finden.");
    assertTrue(emptyResolved.isEmpty(), "Bei leerem Text muss resolveConflicts leer sein.");

    // Fall B: Text ohne jegliche Übereinstimmungen (z.B. nur Rauten)
    List<HighlightRegion> noMatches = highlighter.collectMatches("###");
    List<HighlightRegion> noResolved = highlighter.resolveConflicts(noMatches);

    assertTrue(noMatches.isEmpty(), "Unbekannte Zeichen dürfen keine Matches erzeugen.");
    assertTrue(noResolved.isEmpty(), "Ohne Matches gibt es keine Konflikte zu lösen.");
  }
}
