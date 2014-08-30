(ns site.db.articles
  (:require [site.db.connect :refer [pool]]
            [clojure.java.jdbc :as sql]))

(defn articles [page-number]
  (sql/with-db-transaction [db pool :isolation :serializable]
    (let [articles-per-page 10
          number-of-articles (-> (sql/query db [(str "SELECT count(id) FROM articles "
                                                     "WHERE published_at < CURRENT_TIMESTAMP")]) first :count)
          first-article (* page-number articles-per-page)
          articles-on-page (sql/query db [(str "SELECT created_at, published_at, title, body FROM articles "
                                               "WHERE published_at < CURRENT_TIMESTAMP ORDER BY "
                                               "published_at DESC LIMIT ? OFFSET ?")
                                          articles-per-page first-article])]
      (with-meta articles-on-page {:prev-page (> page-number 0) 
                                   :next-page (> number-of-articles (+ first-article articles-per-page))
                                   :total-pages
                                   (+ (quot number-of-articles articles-per-page)
                                      (if (pos? (rem number-of-articles articles-per-page)) 1 0))}))))

