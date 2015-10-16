(ns parse-wikipedia-revisions.core
  (:require [clojure.data.xml])
  (:gen-class)
)

(defn parse-xmlgz
  "lazy-parse gzipped xml"
  [filename]
  (-> filename
      (java.io.FileInputStream.)
      (java.util.zip.GZIPInputStream.)
      (clojure.data.xml/parse)))

(defn direct-children-of
  "css: %1 > %2"
  [parent tag-name]
  (filter #(= tag-name (:tag %)) (:content parent)))

(defn first-content-of
  "$('a').html()"
  [nodes]
  (let [[node] (take 1 nodes)
        [inner-text] (:content node)]
    inner-text))

(defn first-child-content-of
  [node tag]
  (first-content-of (direct-children-of node tag)))

(defn parse-iso8601-to-epoch
  [s]
  (if s (.toEpochSecond (java.time.ZonedDateTime/parse s))))

(defn mediawiki-to-pages
  "list of page elements from a <mediawiki> --- immediate children"
  [mediawikitree]
  (direct-children-of mediawikitree :page))

(defn pull-id
  [tree]
  (Long. (first-child-content-of tree :id)))

(defn page-to-revisions
  [pagetree]
  (direct-children-of pagetree :revision))

(defn revision-to-metadata
  [revision]
  (let [revid (first-child-content-of revision :id)
        timestamp (parse-iso8601-to-epoch (first-child-content-of revision :timestamp))
	contributor (first (direct-children-of revision :contributor))
	username (first-child-content-of contributor :username)
	userid (first-child-content-of contributor :id)
	userip (first-child-content-of contributor :ip)]
    [revid timestamp username userid userip]))

(defn page-to-revision-metadatas
  [pagetree]
  (let [pageid (pull-id pagetree)
        pagens (first-child-content-of pagetree :ns)]
    (if (= pagens "0")
      (map #(conj (revision-to-metadata %) pageid) (page-to-revisions pagetree))
      [])))

(defn metadata-to-tsv-line
  [vals]
  (.concat (clojure.string/join "\t" vals) "\n"))

(defn page-to-lines
  [pagetree]
  (map metadata-to-tsv-line (page-to-revision-metadatas pagetree)))

(defn process-xmlgz-to-tsv
  [xmlgz tsv]
  (with-open [w (clojure.java.io/writer tsv)]
    (doseq [lines (map page-to-lines (mediawiki-to-pages (parse-xmlgz xmlgz)))]
      (doseq [line lines]
        (.write w line)))))

(defn -main
  [xmlgz tsv]
  (process-xmlgz-to-tsv xmlgz tsv))
