(defproject riemann-cqengine-index "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.googlecode.cqengine/cqengine "1.2.6"]]

  :main ^:skip-aot riemann-cqengine-index.core
  :java-source-paths ["src/riemann_cqengine_index/"]
  :java-source-path "src/riemann_cqengine_index/"
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[riemann "0.2.4"]]}
             :uberjar {:aot :all}})
