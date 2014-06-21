package org.projog.core.function.compare;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Term;
import org.projog.core.term.TermComparator;

/* TEST
 %TRUE b@>a
 %FALSE b@>b
 %FALSE b@>c
 %TRUE b@>1
 %FALSE b@>b(a)
 */
/**
 * <code>X@&gt;Y</code> - term "greater than" test.
 * <p>
 * Succeeds when the term argument <code>X</code> is greater than the term argument <code>Y</code>.
 * </p>
 */
public final class TermGreaterThan extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term arg1, Term arg2) {
      return TermComparator.TERM_COMPARATOR.compare(arg1, arg2) == 1;
   }
}