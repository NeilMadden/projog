package org.projog.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.createKnowledgeBase;
import static org.projog.core.KnowledgeBaseUtils.getOperands;
import static org.projog.core.parser.WordType.ANONYMOUS_VARIABLE;
import static org.projog.core.parser.WordType.ATOM;
import static org.projog.core.parser.WordType.FLOAT;
import static org.projog.core.parser.WordType.INTEGER;
import static org.projog.core.parser.WordType.QUOTED_ATOM;
import static org.projog.core.parser.WordType.VARIABLE;

import java.io.StringReader;

import org.junit.Test;
import org.projog.core.Operands;

public class WordParserTest {
   private final Operands operands = getOperands(createKnowledgeBase());

   @Test
   public void testAtom() {
      assertWordType("a", ATOM);
      assertWordType("ab", ATOM);
      assertWordType("aB", ATOM);
      assertWordType("a1", ATOM);
      assertWordType("a_", ATOM);
      assertWordType("a_2bY", ATOM);
   }

   @Test
   public void testQuotedAtom() {
      assertWordType("'abcdefg'", "abcdefg", QUOTED_ATOM);
      assertWordType("''", "", QUOTED_ATOM);
      assertWordType("''''", "'", QUOTED_ATOM);
      assertWordType("''''''''''", "''''", QUOTED_ATOM);
      assertWordType("'q 1 \" 0.5 | '' @#~'", "q 1 \" 0.5 | ' @#~", QUOTED_ATOM);
   }

   @Test
   public void testVariable() {
      assertWordType("X", VARIABLE);
      assertWordType("XY", VARIABLE);
      assertWordType("Xy", VARIABLE);
      assertWordType("X1", VARIABLE);
      assertWordType("X_", VARIABLE);
      assertWordType("X_7hU", VARIABLE);
   }

   @Test
   public void testAnonymousVariable() {
      assertWordType("_", ANONYMOUS_VARIABLE);
      assertWordType("__", ANONYMOUS_VARIABLE);
      assertWordType("_X", ANONYMOUS_VARIABLE);
      assertWordType("_x", ANONYMOUS_VARIABLE);
      assertWordType("_2", ANONYMOUS_VARIABLE);
      assertWordType("_X_2a", ANONYMOUS_VARIABLE);
   }

   @Test
   public void testInteger() {
      assertWordType("0", INTEGER);
      assertWordType("1", INTEGER);
      assertWordType("6465456456", INTEGER);
   }

   @Test
   public void testFloat() {
      assertWordType("0.0", FLOAT);
      assertWordType("0.1", FLOAT);
      assertWordType("768.567567", FLOAT);
      assertWordType("3.4028235E38", FLOAT);
      assertWordType("3.4028235e38", FLOAT);
   }

   @Test
   public void testEmptyInput() {
      assertFalse(create("").hasNext());
      assertFalse(create("\t \r\n   ").hasNext());
      assertFalse(create("%abcde").hasNext()); // single line comment
      assertFalse(create("/* hgjh\nghj*/").hasNext()); // multi line comment
   }

   @Test
   public void testSequence() {
      assertParse("Abc12.5@>=-0_2_jgkj a-2hUY_ty\nu\n% kghjgkj\na/*b*/c", "Abc12", ".", "5", "@>=", "-", "0", "_2_jgkj", "a", "-", "2", "hUY_ty", "u", "a", "c");
   }

   @Test
   public void testSentence() {
      assertParse("X is ~( 'Y', 1 ,a).", "X", "is", "~", "(", "Y", ",", "1", ",", "a", ")", ".");
   }

   @Test
   public void testNonAlphanumericCharacterFollowedByPeriod() {
      assertParse("!.", "!", ".");
   }

   /** Test that "!" and ";" get parsed separately, rather than as single combined "!;" element. */
   @Test
   public void testCutFollowedByDisjunction() {
      assertParse("!;true", "!", ";", "true");
   }

   /** Test that "(", "!", ")" and "." get parsed separately, rather than as single combined "(!)." element. */
   @Test
   public void testCutInBrackets() {
      assertParse("(!).", "(", "!", ")", ".");
   }

   @Test
   public void testWhitespaceAndComments() {
      WordParser p = create("/* comment */\t % comment\n % comment\r\n\n");
      assertFalse(p.hasNext());
   }

   @Test
   public void testMultiLineComments() {
      assertParse("/*\n\n*\n/\n*/a/*/b*c/d/*e*/f", "a", "f");
   }

   @Test
   public void testFollowedByTerm() {
      WordParser wp = create("?- , [ abc )");
      wp.next();
      assertFalse(wp.isFollowedByTerm());
      wp.next();
      assertTrue(wp.isFollowedByTerm());
      wp.next();
      assertTrue(wp.isFollowedByTerm());
      wp.next();
      assertFalse(wp.isFollowedByTerm());
   }

   /** @see {@link WordParser#rewind(String)} */
   @Test
   public void testRewindException() {
      WordParser wp = create("a b c");
      assertEquals("a", wp.next().value);
      Word b = wp.next();
      assertEquals("b", b.value);
      wp.rewind(b);
      assertSame(b, wp.next());
      wp.rewind(b);

      // check that can only rewind one word
      assertRewindException(wp, "b");
      assertRewindException(wp, "a");

      assertEquals("b", wp.next().value);
      Word c = wp.next();
      assertEquals("c", c.value);

      // check that the value specified in call to rewind has to be the last value parsed
      assertRewindException(wp, "b");
      assertRewindException(wp, null);
      assertRewindException(wp, "z");

      wp.rewind(c);
      assertSame(c, wp.next());
      assertFalse(wp.hasNext());
      wp.rewind(c);
      assertTrue(wp.hasNext());

      // check that can only rewind one word
      assertRewindException(wp, "c");
   }

   private void assertRewindException(WordParser wp, String value) {
      try {
         wp.rewind(new Word(value, WordType.ATOM));
         fail();
      } catch (IllegalArgumentException e) {
         // expected
      }
   }

   private void assertWordType(String syntax, WordType type) {
      assertWordType(syntax, syntax, type);
   }

   private void assertWordType(String syntax, String value, WordType type) {
      WordParser p = create(syntax);
      assertTrue(p.hasNext());
      Word word = p.next();
      assertEquals(value, word.value);
      assertSame(type, word.type);
      assertFalse(p.hasNext());
   }

   private void assertParse(String sentence, String... words) {
      WordParser p = create(sentence);
      for (String w : words) {
         Word next = p.next();
         assertEquals(w, next.value);
         p.rewind(next);
         assertSame(next, p.next());
      }
      assertFalse(p.hasNext());
      try {
         p.next();
         fail();
      } catch (ParserException e) {
         assertEquals("Unexpected end of stream Line: " + e.getLine(), e.getMessage());
      }
   }

   private WordParser create(String syntax) {
      StringReader sr = new StringReader(syntax);
      return new WordParser(sr, operands);
   }
}