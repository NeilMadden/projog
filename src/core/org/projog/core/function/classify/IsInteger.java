package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Term;
import org.projog.core.term.TermType;

/* SYSTEM TEST
 % %TRUE% integer(1)
 % %TRUE% integer(-1)
 % %TRUE% integer(0)
 % %FALSE% integer(1.0)
 % %FALSE% integer(-1.0)
 % %FALSE% integer(0.0)
 % %FALSE% float('1')
 % %FALSE% float('1.0')
 % %FALSE% integer(a)
 % %FALSE% integer(p(1,2,3))
 % %FALSE% integer([1,2,3])
 % %FALSE% integer([])
 % %FALSE% integer(X)
 % %FALSE% integer(_)
*/
/**
 * <code>integer(X)</code> - checks that a term is an integer.
 * <p>
 * <code>integer(X)</code> succeeds if <code>X</code> currently stands for an integer.
 * </p>
 */
public final class IsInteger extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term... args) {
      return evaluate(args[0]);
   }

   /**
    * Overloaded version of {@link #evaluate(Term...)} that avoids the overhead of creating a new {@code Term} array.
    * 
    * @see org.projog.core.Predicate#evaluate(Term...)
    */
   public boolean evaluate(Term arg) {
      return arg.getType() == TermType.INTEGER;
   }
}