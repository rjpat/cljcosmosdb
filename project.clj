(defproject AzureClj "0.1.0-SNAPSHOT"
  :description "Clojure Client for Azure Cosmos DB"
  :url "https://azure.microsoft.com/en-us/services/cosmos-db/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [com.microsoft.azure/azure-documentdb "1.9.4"] ; documentdb java sdk                  
                 [org.clojure/tools.logging "0.3.1"]     
                 [org.slf4j/slf4j-log4j12 "1.7.12"]                  
                 [cheshire "5.7.0"]] ; to generate json https://github.com/dakrone/cheshire
  :aot [cljcosmosdb
        clojure.tools.logging.impl])