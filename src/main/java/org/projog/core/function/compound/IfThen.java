/*
 * Copyright 2018 S. Webber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projog.core.function.compound;

import org.projog.core.KnowledgeBase;
import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.function.AbstractPredicate;
import org.projog.core.term.Term;
import org.projog.core.term.TermUtils;

/* TEST
 if_then_else_test(1).
 if_then_else_test(2).
 if_then_else_test(3).

 %TRUE 2>1 -> true
 %FALSE 2<1 -> true
 %FALSE 2>1 -> fail

 %QUERY if_then_else_test(X) -> if_then_else_test(X)
 %ANSWER X=1
 %NO

 %QUERY if_then_else_test(X) -> if_then_else_test(Y)
 %ANSWER
 % X=1
 % Y=1
 %ANSWER
 %ANSWER
 % X=1
 % Y=2
 %ANSWER
 %ANSWER
 % X=1
 % Y=3
 %ANSWER

 %QUERY true -> X=a ; X=b
 %ANSWER X=a

 %QUERY fail -> X=a ; X=b
 %ANSWER X=b

 %QUERY (X=a, 1<2) -> Y=b; Y=c
 %ANSWER
 % X=a
 % Y=b
 %ANSWER

 %QUERY (X=a, 1>2) -> Y=b; Y=c
 %ANSWER
 % X=UNINSTANTIATED VARIABLE
 % Y=c
 %ANSWER

 %QUERY if_then_else_test(X) -> if_then_else_test(X) ; if_then_else_test(X)
 %ANSWER X=1
 %NO

 %QUERY (if_then_else_test(X), fail) -> if_then_else_test(X) ; if_then_else_test(X)
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3

 %QUERY if_then_else_test(X) -> if_then_else_test(Y) ; Y=b
 %ANSWER
 % X=1
 % Y=1
 %ANSWER
 %ANSWER
 % X=1
 % Y=2
 %ANSWER
 %ANSWER
 % X=1
 % Y=3
 %ANSWER
 */
/**
 * <code>X-&gt;Y</code> - if <code>X</code> succeeds then <code>Y</code> is evaluated.
 * <p>
 * <b>Note:</b> The behaviour of this predicate changes when it is specified as the first argument of a structure of the
 * form <code>;/2</code>, i.e. the <i>"disjunction"</i> predicate. When a <code>-&gt;/2</code> predicate is the first
 * argument of a <code>;/2</code> predicate then the resulting behaviour is a <i>"if/then/else"</i> statement of the
 * form <code>((if-&gt;then);else)</code>.
 * </p>
 *
 * @See {@link Disjunction}
 */
public final class IfThen implements PredicateFactory {
   private KnowledgeBase kb;

   @Override
   public IfThenPredicate getPredicate(Term... args) {
      if (args.length == 2) {
         return getPredicate(args[0], args[1]);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public IfThenPredicate getPredicate(Term conditionTerm, Term thenTerm) {
      return new IfThenPredicate(KnowledgeBaseUtils.getPredicate(kb, conditionTerm), KnowledgeBaseUtils.getPredicate(kb, thenTerm));
   }

   @Override
   public void setKnowledgeBase(KnowledgeBase kb) {
      this.kb = kb;
   }

   public static final class IfThenPredicate extends AbstractPredicate {
      private final Predicate conditionPredicate;
      private final Predicate thenPredicate;
      private Term[] actualArgs;

      private IfThenPredicate(Predicate conditionPredicate, Predicate thenPredicate) {
         this.conditionPredicate = conditionPredicate;
         this.thenPredicate = thenPredicate;
      }

      @Override
      public boolean evaluate(Term conditionTerm, Term thenTerm) {
         if (actualArgs == null) {
            if (conditionPredicate.evaluate(conditionTerm.getArgs())) {
               actualArgs = thenTerm.getTerm().getArgs();
            } else {
               return false;
            }
         } else {
            TermUtils.backtrack(actualArgs);
         }

         return thenPredicate.evaluate(actualArgs);
      }

      @Override
      public boolean isRetryable() {
         return thenPredicate.isRetryable();
      }

      @Override
      public boolean couldReEvaluationSucceed() {
         return isRetryable() && thenPredicate.couldReEvaluationSucceed();
      }
   }
}