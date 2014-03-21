(ns riemann-cqengine-index.query
  (:use riemann.common
        riemann-cqengine-index.event-attributes
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clojure.core.cache :as cache]
            [riemann.string :as s])
  (:import com.googlecode.cqengine.query.QueryFactory
           (org.antlr.runtime.tree BaseTree)))


(defn make-regexy-query [field s]
  (if (not-any? #(= \% %) s)
    (list 'QueryFactory/contains field s)
    (let [[start & kids] (split-regexy-string s)
          [end kids] [(last kids) (filter #(not= "%" %) (butlast kids))]
          start (if (not= "%" start) (list 'QueryFactory/startsWith field start))
          end (if (not= "%" end) (list 'QueryFactory/endsWith field end))
          kids (if kids (map #(list 'QueryFactory/contains field %) kids))
          result (filter (complement nil?) (apply list 'QueryFactory/and start end kids))]
      (if (= 2 (count result))
        (second result)
        result))))

(defn- split-regexy-string [string]
  (filter (complement empty?)
          (s/split-lines
            (s/replace string "%" "\n%\n"))))


(defn node-ast [^BaseTree node]
  "The AST for a given parse node"
  (let [n    (.getText node)
        kids (remove (fn [x] (= x :useless))
                     (map node-ast (.getChildren node)))]
    (case n
      "or"  (apply list 'QueryFactory/or kids)
      "and" (apply list 'QueryFactory/and kids)
      "not" (apply list 'QueryFactory/not kids)
      "="   (apply list 'QueryFactory/equal kids)
      ">"   (apply list 'QueryFactory/greaterThan kids)
      ">="  (apply list 'QueryFactory/greaterThanOrEqualTo kids)
      "<"   (apply list 'QueryFactory/lessThan kids)
      "<="  (apply list 'QueryFactory/lessThanOrEqualTo kids)

      "=~"  (make-regexy-query (first kids) (last kids))
      "~="  (make-regexy-query (first kids) (last kids))


      "!="  (list 'QueryFactory/not (apply list 'QueryFactory/equal kids))
      "tagged"      (apply list 'QueryFactory/equal 'tags kids)
      "("           :useless
      ")"           :useless
      "nil"         nil
      "null"        nil
      "true"        true
      "false"       false
      "host"        'HOST
      "service"     'SERVICE
      "state"       'STATE
      "description" 'DESCRIPTION
      ;"metric_f"    'metric_f
      "metric"      'METRIC
      "time"        'TIME
      "ttl"         'TTL
      (when n (let [term (read-string n)]
                (if (or (number? term)
                        (string? term))
                  term
                  (throw+ {:type ::parse-error
                           :message (str "invalid term \"" n "\"")})))))))



(defn ast
  "The expression AST for a given string"
  [string]
  (node-ast (parse-string string)))

(def fun-cache
  "Speeds up the compilation of queries by caching map of ASTs to corresponding
  functions."
  (atom (cache/lru-cache-factory {} :threshold 64)))

(defn fun
  "Transforms an AST into a fn [event] which returns true if the query matches
  that event. Example:

  (def q (fun (ast \"metric > 2\")))
  (q {:metric 1}) => false
  (q {:metric 3}) => true"
  [ast]
  (if-let [fun (cache/lookup @fun-cache ast)]
    ; Cache hit
    (do (swap! fun-cache cache/hit ast)
        fun)
    ; Cache miss
    (let [fun (eval ast)]
      (swap! fun-cache cache/miss ast fun)
      fun)))


