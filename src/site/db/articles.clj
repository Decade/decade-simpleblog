(ns site.db.articles
  (:require [site.db.connect :refer [connect]]
            [clojure.java.jdbc :as sql]))

(def articles-per-page 10)

(defn articles [page-number]
  (sql/with-db-transaction [db (connect) :isolation :serializable]
    (let [first-article-number (* page-number articles-per-page)
          number-of-articles (-> (sql/query db ["SELECT count(id) FROM articles"]) first :count)
          articles (sql/query db [(str "SELECT id, created_at, published_at, title FROM articles "
                                       "WHERE published_at < CURRENT_TIMESTAMP "
                                       "ORDER BY published_at DESC LIMIT ? OFFSET ?")
                                  articles-per-page first-article-number])]
      (with-meta articles {:page-number page-number
                           :prev-page (> page-number 0) 
                           :next-page (> number-of-articles (+ first-article-number articles-per-page))
                           :total-pages (quot (+ number-of-articles (dec articles-per-page))
                                              articles-per-page)}))))

(defn article [article-number]
  (sql/with-db-transaction [db (connect) :isolation :serializable]
    (let [article
          (first (sql/query db [(str "SELECT c.created_at, c.published_at, c.title, c.body, "
                                     "prev.id AS prev, prev.title AS prev_title, "
                                     "next.id AS next, next.title AS next_title "
                                     "FROM articles c LEFT JOIN "
                                     "(SELECT l.id, l.title FROM articles l CROSS JOIN articles r "
                                     "WHERE r.id = ? AND l.published_at > r.published_at "
                                     "AND l.published_at < CURRENT_TIMESTAMP "
                                     "ORDER BY l.published_at ASC LIMIT 1) PREV ON TRUE LEFT JOIN "
                                     "(SELECT l.id, l.title FROM articles l cross join articles r "
                                     "WHERE r.id = ? AND l.published_at < r.published_at "
                                     "AND l.published_at < CURRENT_TIMESTAMP "
                                     "ORDER BY l.published_at DESC LIMIT 1) NEXT ON TRUE "
                                     "WHERE c.published_at < CURRENT_TIMESTAMP AND c.id = ?")
                         article-number article-number article-number]))
          page-number (-> (sql/query db [(str "SELECT count(id) FROM articles "
                                              "WHERE published_at < CURRENT_TIMESTAMP "
                                              "AND published_at > ?") (:published_at article)])
                          first
                          :count
                          (#(quot % articles-per-page)))
          next-article (if (:created_at article)
                         {:id (:next article) :title (:next_title article)}
                         (first (sql/query db [(str "SELECT id AS next, title AS next_title FROM articles "
                                                    "WHERE published_at < CURRENT_TIMESTAMP "
                                                    "ORDER BY published_at DESC LIMIT 1")])))] 
      (or (and article (assoc article :page_number page-number)) next-article))))

(defn first-article []
  (let [articles (sql/query (connect) [(str "SELECT id, title, created_at, published_at, title, body "
                                       "FROM articles WHERE published_at < CURRENT_TIMESTAMP "
                                       "ORDER BY published_at DESC LIMIT 2")])
        first-article (first articles)
        second-article (-> articles rest first)
        {:keys [created_at published_at title body]} first-article
        {next :id next_title :title} second-article]
    {:created_at created_at :published_at published_at :title title :body body 
     :next next :next_title next_title}))
