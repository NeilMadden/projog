/*
 * Copyright 2013 S. Webber
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
package org.projog.core;

import static org.projog.core.KnowledgeBaseUtils.getProjogEventsObservable;
import static org.projog.core.KnowledgeBaseUtils.getTermFormatter;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.projog.core.event.ProjogEvent;
import org.projog.core.event.ProjogEventType;
import org.projog.core.event.ProjogEventsObservable;
import org.projog.core.term.Structure;
import org.projog.core.term.Term;
import org.projog.core.term.TermFormatter;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Variable;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.UserDefinedPredicateFactory;

/**
 * Collection of spy points.
 * <p>
 * Spy points are useful in the debugging of Prolog programs. When a spy point is set on a predicate a
 * {@link ProjogEventType} is generated every time the predicate is executed, fails or succeeds.
 * </p>
 * <p>
 * Each {@link org.projog.core.KnowledgeBase} has a single unique {@code SpyPoints} instance.
 * </p>
 *
 * @see KnowledgeBaseUtils#getSpyPoints(KnowledgeBase)
 */
public final class SpyPoints {
   private final Object lock = new Object();
   private final Map<PredicateKey, SpyPoint> spyPoints = new TreeMap<>(); // TODO make concurrent?
   private final KnowledgeBase kb;
   private final ProjogEventsObservable projogEventsObservable;
   private final TermFormatter termFormatter;
   private boolean traceEnabled;

   public SpyPoints(KnowledgeBase kb) {
      this.kb = kb;
      this.projogEventsObservable = getProjogEventsObservable(kb);
      this.termFormatter = getTermFormatter(kb);
   }

   public SpyPoints(ProjogEventsObservable observable, TermFormatter termFormatter) { // TODO only used by tests - remove
      this.kb = null;
      this.projogEventsObservable = observable;
      this.termFormatter = termFormatter;
   }

   public void setTraceEnabled(boolean traceEnabled) {
      synchronized (lock) {
         this.traceEnabled = traceEnabled;
         for (SpyPoints.SpyPoint sp : spyPoints.values()) {
            if (traceEnabled) {
               sp.enabled = true;
            } else {
               sp.enabled = sp.set;
            }
         }
      }
   }

   public void setSpyPoint(PredicateKey key, boolean set) {
      synchronized (lock) {
         SpyPoint sp = getSpyPoint(key);
         sp.set = set;
         sp.enabled = traceEnabled || sp.set;
      }
   }

   public SpyPoint getSpyPoint(PredicateKey key) {
      SpyPoint spyPoint = spyPoints.get(key);
      if (spyPoint == null) {
         spyPoint = createNewSpyPoint(key);
      }
      return spyPoint;
   }

   private SpyPoint createNewSpyPoint(PredicateKey key) {
      synchronized (lock) {
         SpyPoint spyPoint = spyPoints.get(key);
         if (spyPoint == null) {
            spyPoint = new SpyPoint(key);
            spyPoint.enabled = traceEnabled;
            spyPoints.put(key, spyPoint);
         }
         return spyPoint;
      }
   }

   public Map<PredicateKey, SpyPoint> getSpyPoints() {
      return Collections.unmodifiableMap(spyPoints);
   }

   public class SpyPoint {
      private final PredicateKey key;
      private boolean enabled;
      private boolean set;

      private SpyPoint(PredicateKey key) {
         this.key = key;
      }

      public PredicateKey getPredicateKey() {
         return key;
      }

      public boolean isSet() {
         return set;
      }

      public boolean isEnabled() {
         return enabled;
      }

      /** Generates an event of type {@link ProjogEventType#CALL} */
      public void logCall(Object source, Term[] args) {
         log(ProjogEventType.CALL, source, args, null);
      }

      /** Generates an event of type {@link ProjogEventType#REDO} */
      public void logRedo(Object source, Term[] args) {
         log(ProjogEventType.REDO, source, args, null);
      }

      /** Generates an event of type {@link ProjogEventType#EXIT} */
      public void logExit(Object source, Term[] args, int clauseNumber) {
         ClauseModel clauseModel;
         if (clauseNumber != -1) {
            Map<PredicateKey, UserDefinedPredicateFactory> userDefinedPredicates = kb.getUserDefinedPredicates();
            UserDefinedPredicateFactory userDefinedPredicate = userDefinedPredicates.get(getPredicateKey());
            // clauseNumber starts at 1 / getClauseModel starts at 0
            clauseModel = userDefinedPredicate.getClauseModel(clauseNumber - 1);
         } else {
            clauseModel = null;
         }
         log(ProjogEventType.EXIT, source, args, clauseModel);
      }

      /** Generates an event of type {@link ProjogEventType#EXIT} */
      public void logExit(Object source, Term[] args, ClauseModel clause) {
         log(ProjogEventType.EXIT, source, args, clause);
      }

      /** Generates an event of type {@link ProjogEventType#FAIL} */
      public void logFail(Object source, Term[] args) {
         log(ProjogEventType.FAIL, source, args, null);
      }

      private void log(ProjogEventType type, Object source, Term[] args, ClauseModel clauseModel) {
         if (isEnabled() == false) {
            return;
         }

         SpyPointEvent spe = new SpyPointEvent(key, args, clauseModel);
         ProjogEvent pe = new ProjogEvent(type, spe, source);
         projogEventsObservable.notifyObservers(pe);
      }
   }

   public class SpyPointEvent {
      private final PredicateKey key;
      private final Term[] args;
      private final ClauseModel clauseModel;

      private SpyPointEvent(PredicateKey key, Term[] args, ClauseModel clauseModel) {
         this.key = key;
         for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
               args[i] = new Variable("Variable");
            }
         }
         this.args = TermUtils.copy(args);
         this.clauseModel = clauseModel;
      }

      public PredicateKey getPredicateKey() {
         return key;
      }

      public String getFormattedTerm() {
         if (args.length == 0) {
            return key.getName();
         } else {
            Term term = Structure.createStructure(key.getName(), args);
            return termFormatter.toString(term);
         }
      }

      public ClauseModel getClauseModel() {
         if (clauseModel == null) {
            throw new IllegalStateException("No clause specified for event");
         }
         return clauseModel;
      }

      public String getFormattedClause() {
         return termFormatter.toString(getClauseModel().getOriginal());
      }

      @Override
      public String toString() {
         return getFormattedTerm();
      }
   }
}
