/*
 * Copyright 2013-2014 S. Webber
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
package org.projog.core.function.math;

import static org.projog.core.KnowledgeBaseUtils.getArithmeticOperators;

import java.util.Arrays;

import org.projog.core.ArithmeticOperator;
import org.projog.core.ArithmeticOperators;
import org.projog.core.KnowledgeBase;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Numeric;
import org.projog.core.term.Term;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Variable;

/* TEST
 squared(X,Y) :- Y is X * X.

 %QUERY squared(3,X)
 %ANSWER X=9

 %QUERY X is squared(3)
 %ERROR Cannot find arithmetic operator: squared/1

 %TRUE arithmetic_function(squared/1)

 %QUERY X is squared(3)
 %ANSWER X=9
 */
/**
 * <code>arithmetic_function(X)</code> - defines a predicate as an arithmetic function.
 * <p>
 * Allows the predicate defined by <code>X</code> to be used as an arithmetic function.
 */
public final class AddUserDefinedArithmeticOperator extends AbstractSingletonPredicate {
   private ArithmeticOperators operators;

   @Override
   public void init() {
      operators = getArithmeticOperators(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(Term arg) {
      final PredicateKey key = PredicateKey.createFromNameAndArity(arg);
      final UserDefinedArithmeticOperator arithmeticOperator = new UserDefinedArithmeticOperator(getKnowledgeBase(), key);
      operators.addArithmeticOperator(key, arithmeticOperator);
      return true;
   }

   private static final class UserDefinedArithmeticOperator implements ArithmeticOperator {
      final int numArgs;
      final PredicateKey key;
      final PredicateFactory pf;

      UserDefinedArithmeticOperator(KnowledgeBase kb, PredicateKey originalKey) {
         this.numArgs = originalKey.getNumArgs();
         this.key = new PredicateKey(originalKey.getName(), numArgs + 1);
         this.pf = kb.getPredicateFactory(key);
      }

      @Override
      public Numeric calculate(Term... args) {
         final Variable result = new Variable("result");
         final Term[] argsPlusResult = createArgumentsIncludingResult(args, result);

         if (pf.getPredicate(argsPlusResult).evaluate()) {
            return TermUtils.castToNumeric(result);
         } else {
            throw new ProjogException("Could not evaluate: " + key + " with arguments: " + Arrays.toString(args));
         }
      }

      private Term[] createArgumentsIncludingResult(Term[] args, final Variable result) {
         final Term[] argsPlusResult = new Term[numArgs + 1];
         for (int i = 0; i < numArgs; i++) {
            argsPlusResult[i] = args[i].getTerm();
         }
         argsPlusResult[numArgs] = result;
         return argsPlusResult;
      }

      @Override
      public void setKnowledgeBase(KnowledgeBase kb) {
         throw new UnsupportedOperationException();
      }
   }
}
