(ns parse-wikipedia-revisions.core
  (:require [clojure.data.xml]
            [clojure.java.jdbc :as jdbc])
  (:gen-class)
)

(def ONLYTEXT (System/getenv "ONLYTEXT"))

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
    {:id revid, :epochsecond timestamp, :user (pr-str [username userid userip])} ))

(defn page-to-revision-metadatas
  [pagetree]
  (if ONLYTEXT
    (map (fn [r] {:id (pull-id r) :txt (first-child-content-of r :text)}) (page-to-revisions pagetree))
    (let [pageid (pull-id pagetree)
          title (first-child-content-of pagetree :title)
          pagens (first-child-content-of pagetree :ns)]
      (if (= pagens "0")
        (map #(merge (revision-to-metadata %) {:page_id pageid, :title title}) (page-to-revisions pagetree))
        []))))

(defn metadata-to-tsv-line
  [vals]
  (.concat (clojure.string/join "\t" vals) "\n"))

(defn page-to-lines
  [pagetree]
  (map metadata-to-tsv-line (page-to-revision-metadatas pagetree)))

(defn process-xmlgz-to-tsv
  [xmlgz db]
  (jdbc/with-db-connection [cnxn (str "jdbc:sqlite:" db)]
    (jdbc/with-db-transaction [txn cnxn]
      (doseq [rows (map (fn [p] (page-to-revision-metadatas p)) (mediawiki-to-pages (parse-xmlgz xmlgz)))]
        (doseq [row rows]
          (jdbc/insert! txn (if ONLYTEXT "revision_texts" "revisions") row :transaction? false))))))

(defn -main
  [xmlgz db]
  (process-xmlgz-to-tsv xmlgz db))

