(defproject parse-wikipedia-revisions "0.1.0-SNAPSHOT"
  :description "Parse the Wikipedia stub-meta-history"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :aot :all
  :main parse-wikipedia-revisions.core
  :jvm-opts ["-Xmx512m" "-Xms512m" "-XX:+UseCompressedOops"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.xml "0.0.8"]])
