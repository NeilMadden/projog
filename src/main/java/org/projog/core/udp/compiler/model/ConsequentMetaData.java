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
package org.projog.core.udp.compiler.model;

import java.util.Collection;
import java.util.Map;

import org.projog.core.term.Term;

/** Contains meta-data for the consequent (i.e. head) of a clause. */
public final class ConsequentMetaData implements ClauseElement {
   private final Term term;
   private final Map<String, ClauseVariableMetaData> variables;

   ConsequentMetaData(Term term, Map<String, ClauseVariableMetaData> variables) {
      this.term = term;
      this.variables = variables;
   }

   public Term getTerm() {
      return term;
   }

   @Override
   public Collection<ClauseVariableMetaData> getVariables() {
      // TODO Return a unmodifiable copy instead? (Also review for other classes of this package.)
      return variables.values();
   }
}
