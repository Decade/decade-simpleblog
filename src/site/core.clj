(ns site.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [site.db.articles :as art]
            [site.page.index :as page]
            [org.httpkit.server :refer :all]
            [environ.core :refer [env]]))

(defn get-index [page-num]
  (let [index (page/index (art/articles page-num))]
    index))
(defn get-article [art-num]
  (println art-num)
  (let [art (page/article (art/article art-num))]
    (println art)
    art))

(defroutes all-routes
  (GET "/" [] (get-index 0))
  (GET ["/article/:id" :id #"[1-9][0-9]*"] [id] (get-article (read-string id)))
  (route/not-found (page/fnf)))

(defn -main []
  (run-server (handler/site #'all-routes) {:port (env :port)}))
