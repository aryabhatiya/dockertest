(ns sgc.scan
  (:require
   [boot.core :refer :all]
   [clojure.java.io :as io]))

(defn compile-lc
  [lc-file uc-file]
  (->> lc-file
       slurp
       (spit uc-file))
  )
;;        .toUpperCase
(defn lc->uc-path
  [path]
  (.replaceAll (last  (clojure.string/split path #"/")) "\\.lc$" ".uc")
                                        ; (.replaceAll  "\\.lc$" ".uc")
  )

(deftask scan
  []
  (let [tmp (temp-dir!)
        state (atom nil)
        ]
    (with-pre-wrap fileset
      (empty-dir! tmp)
      (doseq [lc-tmp
              (->> fileset
                   (fileset-diff @state)
                   input-files
                   (by-ext [".lc"])
                   )
              :let [lc-file (tmpfile lc-tmp)
                    lc-path (tmppath lc-tmp)
                    uc-file (io/file tmp (lc->uc-path  lc-path))]]

        (if @state
          (do
            (println "Fetching ..."  lc-path)
            (compile-lc lc-file uc-file))
          )
        )
      (reset! state fileset)
      (->  fileset (add-resource tmp) commit!)
      )))
