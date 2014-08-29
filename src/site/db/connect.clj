(ns site.db.connect
  (:require environ.core
            [clojure.java.jdbc :as sql])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource
           java.net.URI))

(def spec (:database-url environ.core/env))

(defn- connect[] (let [uri (URI. spec)
                       userinfo (.split (.getUserInfo uri) ":")
                       username (nth userinfo 0)
                       password (nth userinfo 1)
                       dburl (str "jdbc:postgresql://" (.getHost uri) ":" (.getPort uri) (.getPath uri))
                       cpds (doto (ComboPooledDataSource.)
                              (.setJdbcUrl dburl)
                              (.setUser username)
                              (.setPassword password))
                       db {:datasource cpds}
                       hastable (-> (sql/query db (str "select count(table_name) from "
                                                       "information_schema.tables "
                                                       "where table_name = 'articles'"))
                                    first :count pos?)]
                   (when (not hastable)
                     (sql/with-db-transaction [t db]
                       (sql/db-do-commands
                        t
                        (sql/create-table-ddl :articles
                                              [:id :serial "PRIMARY KEY"]
                                              [:created_at :timestamp 
                                               "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]
                                              [:published_at :timestamp
                                               "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]
                                              [:title :varchar "NOT NULL"]
                                              [:body :varchar "NOT NULL"])
                        (sql/create-table-ddl :version
                                              [:version :serial "PRIMARY KEY"]))
                       (sql/insert! t :version nil [1])))
                   db))

(def pool (connect))
