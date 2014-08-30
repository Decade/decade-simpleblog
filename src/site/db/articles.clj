(ns site.db.articles
  (:require [site.db.connect :refer [pool]]
            [clojure.java.jdbc :as sql]))

(def articles-per-page 10)

(defn articles [page-number]
  (sql/with-db-transaction [db pool :isolation :serializable]
    (let [first-article-number (* page-number articles-per-page)
          number-of-articles (-> (sql/query db ["SELECT count(id) FROM articles"]) first :count)
          articles (sql/query db [(str "SELECT id, created_at, published_at, title FROM articles "
                                       "WHERE published_at < CURRENT_TIMESTAMP "
                                       "ORDER BY published_at DESC LIMIT ? OFFSET ?")
                                  articles-per-page first-article-number])]
      (with-meta articles {:page-number page-number
                           :prev-page (> page-number 0) 
                           :next-page (> number-of-articles (+ first-article-number articles-per-page))
                           :total-pages
                           (+ (quot number-of-articles articles-per-page)
                              (if (pos? (rem number-of-articles articles-per-page)) 1 0))}))))

(defn article [article-number]
  (sql/with-db-transaction [db pool :isolation :serializable]
    (let [article
          (first (sql/query db [(str "SELECT c.created_at, c.published_at, c.title, c.body, "
                                     "prev.id as prev, prev.title as prev_title, "
                                     "next.id as next, next.title as next_title "
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
                         (first (sql/query db [(str "SELECT id as next, title as next_title FROM articles "
                                                    "WHERE published_at < CURRENT_TIMESTAMP "
                                                    "ORDER BY published_at DESC LIMIT 1")])))] 
      (or (and article (assoc article :page_number page-number)) next-article))))
