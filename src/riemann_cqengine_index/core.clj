(ns riemann-cqengine-index.core
  (:import com.googlecode.cqengine.CQEngine
           com.googlecode.cqengine.query.QueryFactory)
  (:use [riemann.time :only [unix-time]])
  (:gen-class))



(defn cqengine-index
  "Create a new nonblockinghashmap backed index"
  []
  (let [hm (. CQEngine (newInstance))]
    (reify
      Index
      (clear [this]
             (.clear hm))

      (delete [this event]
              (.remove hm event))

      (delete-exactly [this event]
                      (.remove hm event))

      (expire [this]
        (let [expired (.retrieve this (QueryFactory/lessThan EXPIRES_AT (unix-time)))]
          (map #(delete this %) expired)
          expired))


      (search [this query-ast]
              "O(n), sadly."
              (let [matching (query/fun query-ast)]
                (filter matching (.values hm))))

      (update [this event]
        (if (= "expired" (:state event))
          (delete this event)
          (.add hm event)))

      (lookup [this host service]
        (first (.retrieve hm (QueryFactory/equal ID [host service]))))

      clojure.lang.Seqable
      (seq [this]
           (seq (.iterator hm)))

      ServiceEquiv
      (equiv? [this other] (= (class this) (class other)))

      Service
      (conflict? [this other] false)
      (reload! [this new-core])
      (start! [this])
      (stop! [this]))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (println "Hello, World!"))
