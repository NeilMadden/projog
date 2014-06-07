package org.projog.core.function.math;

import org.projog.core.Calculatable;
import org.projog.core.KnowledgeBase;
import org.projog.core.term.DoubleNumber;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.Term;
import org.projog.core.term.TermType;

/**
 * A template for {@code Calculatable}s that accept exactly one argument.
 */
abstract class AbstractOneArgumentCalculatable implements Calculatable {
   @Override
   public final Numeric calculate(KnowledgeBase kb, Term[] args) {
      Numeric n = kb.getNumeric(args[0]);
      if (n.getType() == TermType.DOUBLE) {
         double answer = calculateDouble(n.getDouble());
         return new DoubleNumber(answer);
      } else {
         int answer = calculateInt(n.getInt());
         return new IntegerNumber(answer);
      }
   }

   /** Returns the result of evaluating an arithmetic expression using the specified argument */
   protected abstract double calculateDouble(double d);

   /** Returns the result of evaluating an arithmetic expression using the specified argument */
   protected abstract int calculateInt(int i);
}
