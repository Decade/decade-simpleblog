(ns site.page.index
  (:require [hiccup.core :refer [html]])
  (:import java.text.DateFormat))

(defn index [page]
  (html
   (let [metadata (meta page)]
     [:html
      [:head
       [:title "Simple blog thing."]]
      [:body
       [:h1 "Simple blog thing."]
       (for [article page]
         [:article
          [:h3 [:a {:href (str "article?id=" (:id article))} (:title article)]]
          [:p (str "Created at " (->> article
                                      :created_at
                                      (.format (DateFormat/getDateTimeInstance 
                                                DateFormat/LONG DateFormat/LONG))))]])
       [:div (str "Page " (inc (:page-number metadata)) " out of " (:total-pages metadata))]
       (if (:prev-page metadata)
         [:div [:a {:href (str "index?p=" (if (> (:page-number metadata) (:total-pages metadata))
                                            (:total-pages metadata)
                                            (dec (:page-number metadata))))} "Previous page"]])
       (if (:next-page metadata)
         [:div [:a {:href (str "index?p=" (inc (:page-number metadata)))} "Next page"]])]])))
