(ns site.db.articles
  (:require [site.db.connect :refer [pool]]
            [clojure.java.jdbc :as sql]))

(defn articles [page-number]
  (sql/with-db-transaction [db pool :isolation :serializable]
    (let [number-of-articles (-> (sql/query db [(str "SELECT count(id) FROM articles "
                                                     "WHERE published_at < CURRENT_TIMESTAMP")]) first :count)
          first-article (* page-number 10)
          articles-on-page (sql/query db [(str "SELECT created_at, published_at, title, body FROM articles "
                                               "WHERE published_at < CURRENT_TIMESTAMP ORDER BY published_at "
                                               "DESC LIMIT 10 OFFSET ?") first-article])]
      (with-meta articles-on-page {:prev-page (> page-number 0) 
                                   :next-page (> number-of-articles (+ first-article 10))}))))
