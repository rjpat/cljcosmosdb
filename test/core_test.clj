(ns cljcosmosdb-test
  (:require [clojure.test :refer :all]
            [cljcosmosdb :refer :all]))

(deftest document-maptest
  (testing "Document->Map->Document Type Conversion"
    (let [cljmapinput {"db" "cosmosdb"}
          cosmosdoc (cljcosmosdb/document cljmapinput)
          cljmapoutput (cljcosmosdb/document->map cosmosdoc)]
      (is (= cljmapinput cljmapoutput)))))