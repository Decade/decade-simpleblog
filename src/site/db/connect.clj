(ns site.db.connect
  (:require [environ.core]
            ;;[clojure.java.jdbc]
            )
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(def spec (:database-url environ.core/env))

(defn connect [] (let [cpds (doto (ComboPooledDataSource.)
                              (.setJdbcUrl spec))]
                   cpds))

(def pool (connect))
