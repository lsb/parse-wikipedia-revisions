(ns parse-wikipedia-revisions.core
  (:require [clojure.data.xml]
            [clojure.data.json :as json])
  (:gen-class)
)

(defn direct-children-of
  "css: %1 > %2"
  [parent tag-name]
  (filter #(= tag-name (:tag %)) (:content parent)))

(defn first-content-of
  "$('a').html()"
  [nodes]
  (first (:content (first nodes))))

(defn first-child-content-of
  [node tag]
  (first-content-of (direct-children-of node tag)))

(defn mediawiki-to-pages
  "list of page elements from a <mediawiki> --- immediate children"
  [mediawikitree]
  (direct-children-of mediawikitree :page))

(defn page-to-revisions
  [pagetree]
  (direct-children-of pagetree :revision))

(defn revision-to-metadata
  [revision]
  (let [revid (first-child-content-of revision :id)
        timestamp (first-child-content-of revision :timestamp)
	contributor (first (direct-children-of revision :contributor))
        text (first-child-content-of revision :text)
	username (first-child-content-of contributor :username)
	userid (first-child-content-of contributor :id)
	userip (first-child-content-of contributor :ip)]
    {:id revid,
     :timestamp timestamp,
     :text text,
     :user_name username,
     :user_id userid,
     :user_ip userip}))

(defn revision-metadata-with-page-metadata
  "Merge page metadata into revisions'."
  [page-metadata revisions]
  (map #(merge (revision-to-metadata %) page-metadata) revisions))

(defn page-to-revision-metadatas
  [pagetree]
  (let [pageid (first-child-content-of pagetree :id)
        title (first-child-content-of pagetree :title)
        pagens (first-child-content-of pagetree :ns)]
    (revision-metadata-with-page-metadata {:page_ns pagens, :page_id pageid, :page_title title} (page-to-revisions pagetree))))

(defn process-xml
  []
  (doseq [rows (map page-to-revision-metadatas (mediawiki-to-pages (clojure.data.xml/parse *in*)))]
    (doseq [row rows]
      (println (json/write-str row)))))

(def -main process-xml)

