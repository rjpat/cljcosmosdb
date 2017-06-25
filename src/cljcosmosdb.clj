(ns cljcosmosdb
  (:import [com.microsoft.azure.documentdb
            DocumentClient
            ResourceResponse
            Database
            RequestOptions
            ConnectionPolicy
            ConsistencyLevel
            FeedOptions
            DocumentCollection
            Document
            QueryIterable])
  (:require [cheshire.core :refer :all]))

(defn ^Document document
  "Coverts a Clojure.lang.Map to Azure CosmosDB type Document"
  [m]
  (let [jsonString (generate-string m)]
    (Document. jsonString)))

(defn document->map
  "Converts a Azure CosmosDB type document to Clojure.lang.Map"
  [^Document d]
  (-> d (.toString) (parse-string)))

(defn iterable-seq
  "Turns a CosmosDB QueryIterable into a Clojure Seq"
  [^QueryIterable queryIterable]
  (-> queryIterable
      (.iterator)
      (iterator-seq)))

(defn GetDocumentUri
  "Takes databaseId, collectionId, DocumentId and returns a DocumentUri that
  can be passed in place of self/link"
  [^String DatabaseId
   ^String CollectionId
   ^String DocumentId]
  (str "dbs/" DatabaseId "/colls/" CollectionId "/docs/" DocumentId))

(defn GetDatabaseLink
  "Takes a database name, and returns the database Id"
  [^String DatabaseId]
  (str "dbs/" DatabaseId))

(defn GetCollectionLink
  "Takes a database name, collection name string and returns the collection Uri"
  [^String DatabaseId
   ^String CollectionId]
  (str "dbs/" DatabaseId "/colls/" CollectionId))

(defn ^DocumentClient client
  "Creates, Intiializes and Returns a new instance of Microsoft.Azure.Documents.Client.DocumentClient using the specified
  service endpoint and permissions"
  [^String endpoint-uri
   ^String primary-key]
  (let [connectionPolicy (. ConnectionPolicy GetDefault)
        consistencyLevel (. ConsistencyLevel Strong)]
    (DocumentClient. endpoint-uri primary-key connectionPolicy consistencyLevel)))

(defn ^Database CreateDatabase
  "Helper to Create a Database"
  [^DocumentClient documentclient
   ^String databaseId]
  (let [database (Database.)
        requestOptions (RequestOptions.)]
    (. database setId databaseId)
    (. (. documentclient createDatabase database requestOptions) getResource)))

(defn ReadDatabases
  "Read Database Account, Returns a vector of Databases in the account"
  [^DocumentClient documentClient]
  (let [readDatabasesResponse (. documentClient readDatabases (FeedOptions.))]
    (-> readDatabasesResponse
        (.getQueryIterable)
        (iterable-seq))))

(defn ^Database ReadDatabase
  "Reads a DocumentDB database"
  [^DocumentClient documentClient
   ^String databaseLink
   ^RequestOptions requestOptions]
  (. (. documentClient databaseLink requestOptions) getResource))

(defn ^ResourceResponse DeleteDatabase
  "Deletes a DocumentDB database"
  [^DocumentClient documentClient ^String databaseLink ^RequestOptions requestOptions]
  (. documentClient databaseLink requestOptions))

(defn ^DocumentCollection CreateCollection
  "Creates a DocumentDB Collection - This is a billable entity.
  Multi-arity functions"
  ([^DocumentClient documentClient
    ^String databaseLink
    ^DocumentCollection documentCollection
    ^RequestOptions requestOptions]
   (. documentClient createCollection databaseLink documentCollection requestOptions))
  ([^DocumentClient documentClient
    ^String databaseName
    ^String collectionName]
   (let [databaseLink (GetDatabaseLink databaseName)
         documentCollection (DocumentCollection.)
         requestOptions (RequestOptions.)]
     (.setId documentCollection collectionName)
     (. documentClient createCollection  databaseLink documentCollection requestOptions))))

(defn ReadCollections
  "Returns a seq of collections in a DocumentDB Database"
  [^DocumentClient documentClient
   ^String databaseLink
   ^FeedOptions feedOptions]
  (let [collectionsResponse (. documentClient readCollections databaseLink feedOptions)]
    (-> collectionsResponse
        (.getQueryIterable)
        (iterable-seq))))

(defn ^ResourceResponse CreateDocument
  "Create a Document. This takes a payload, which is a regular clojure map"
  [^DocumentClient documentClient
   ^String collectionSelfLink
   payload    ; type Clojure Map - that must contain {:id "<id_in_string_type>" }
   ^Boolean disableAutomaticIdGeneration]
  (let [payloadInJson (generate-string payload)
        doc (Document. payloadInJson)
        requestOptions (RequestOptions.)]
    (. documentClient createDocument collectionSelfLink doc requestOptions disableAutomaticIdGeneration)))

(defn ^ResourceResponse DeleteDocument
  "Deletes a Document"
  ([^DocumentClient documentClient
    ^String documentSelfLink
    ^RequestOptions requestOptions]
   (. documentClient deleteDocument documentSelfLink requestOptions))
  ([^DocumentClient documentClient
    ^String databaseId
    ^String collectionId
    ^String documentId]
   (. documentClient deleteDocument (GetDocumentUri databaseId collectionId documentId) (RequestOptions.))))

(defn ^ResourceResponse UpsertDocument
  "Upsert a given document"
  [^DocumentClient documentClient
   ^String databaseName
   ^String collectionName
   payload ; type of clojure map with {:id "my_id_string"}
   ^RequestOptions requestOptions
   ^Boolean disableAutomaticIdGeneration]
  (let [payloadInJson (generate-string payload)
        doc (Document. payloadInJson)]
    (. documentClient upsertDocument (GetCollectionLink databaseName collectionName) doc requestOptions disableAutomaticIdGeneration)))

(defn ^ResourceResponse ReplaceDocument
  "Multi-Arity Function to replace a Document"
  ([^DocumentClient documentClient
    ^String documentSelfLink
    payload
    ^RequestOptions requestOptions]
   (let [jsonPayload (generate-string payload)
         document (Document. jsonPayload)]
     (. documentClient replaceDocument documentSelfLink document requestOptions)))
  ([^DocumentClient documentClient
    ^String DatabaseName
    ^String CollectionName
    payload
    ^RequestOptions requestOptions]
   (let [jsonPayload (generate-string payload)
         document (Document. jsonPayload)
         documentUri (GetDocumentUri DatabaseName CollectionName (:id payload))]
     (. documentClient replaceDocument documentUri document requestOptions))))

(defn ^ResourceResponse ReadDocument
  "Multi-arity function to read a Document"
  ([^DocumentClient documentClient
    ^String documentLink
    ^RequestOptions requestOptions]
   (. documentClient readDocument documentLink requestOptions))
  ([^DocumentClient documentClient
    ^String databaseId
    ^String collectionId
    ^String documentId
    ^RequestOptions requestOptions]
   (. documentClient readDocument (GetDocumentUri databaseId collectionId documentId) requestOptions))
  ([^DocumentClient documentClient
    ^String databaseId
    ^String collectionId
    ^String documentId]
   (. documentClient readDocument (GetDocumentUri databaseId collectionId documentId) (RequestOptions.))))

(defn QueryDocuments
  "Executes SQL Query against CosmosDB collection - this is the most simple overload"
  [^DocumentClient documentClient
   ^String query
   ^RequestOptions requestOptions]
  (let [queryResponse (. documentClient queryDocuments query requestOptions)]
    (-> queryResponse
        (.getQueryIterable)
        (iterable-seq))))
