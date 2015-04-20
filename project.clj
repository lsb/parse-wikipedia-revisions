(defproject parse-wikipedia-revisions "0.2.0-SNAPSHOT"
  :description "Parse the Wikipedia stub-meta-history"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :aot :all
  :main parse-wikipedia-revisions.core
  :jvm-opts ["-Xmx3g" "-Xms3g"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.xml "0.0.8"]])
