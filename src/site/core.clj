(ns site.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [site.db.articles :as art]
            [site.page.index :as page]
            [org.httpkit.server :refer :all]
            [environ.core :refer [env]])
  (:gen-class))

(defn get-index [page-num]
  (let [articles (art/articles page-num)]
    (if (first articles)
      (page/index articles)
      {:status 404
       :body (page/fnf)})))

(defn get-article [art-num]
  (let [art (art/article art-num)]
    (if (:title art)
      (page/article art)
      {:status 404
       :body (page/articlefnf art)})))

(defn get-first []
  (let [art (art/first-article)]
    (if (:title art)
      (page/article art)
      {:status 404
       :body (page/articlefnf art)})))

(defn index-redirect []
  {:status 301
   :headers {"Location" "/index/"}})
(defn article-redirect []
  {:status 301
   :headers {"Location" "/article/"}})

(defroutes all-routes
  (GET "/" [] (get-index 0))
  (GET ["/article/:id" :id #"[0-9]+"] [id] (get-article (read-string id)))
  (GET ["/index/:p" :p #"[0-9]+"] [p] (get-index (read-string p)))
  (GET "/article" [] (article-redirect))
  (GET "/article/" [] (get-first))
  (GET "/article/*" [] (get-article nil))
  (GET "/index" [] (index-redirect))
  (GET "/index/" [] (get-index 0))
  (route/not-found (page/fnf)))

(defn -main [& args]
  (run-server (handler/site all-routes) {:port (env :port)}))
