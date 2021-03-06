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
package org.projog.core.function.math;

/* TEST
 %QUERY X is 3 \/ 3
 %ANSWER X=3

 %QUERY X is 3 \/ 7
 %ANSWER X=7

 %QUERY X is 3 \/ 6
 %ANSWER X=7

 %QUERY X is 3 \/ 8
 %ANSWER X=11

 %QUERY X is 43 \/ 27
 %ANSWER X=59

 %QUERY X is 27 \/ 43
 %ANSWER X=59

 %QUERY X is 43 \/ 0
 %ANSWER X=43

 %QUERY X is 0 \/ 0
 %ANSWER X=0
 */
/**
 * <code>\/</code> - bitwise 'or'.
 */
public final class BitwiseOr extends AbstractBinaryIntegerArithmeticOperator {
   @Override
   protected long calculateLong(long n1, long n2) {
      return n1 | n2;
   }
}
