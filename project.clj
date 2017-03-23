(defproject parse-wikipedia-revisions "0.4.0-SNAPSHOT"
  :description "Parse the Wikipedia stub-meta-history"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :aot :all
  :main parse-wikipedia-revisions.core
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.0.8"]])
