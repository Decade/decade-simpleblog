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
          [:h3 [:a {:href (str "/article/" (:id article))} (:title article)]]
          [:p (str "Created at " (->> article
                                      :created_at
                                      (.format (DateFormat/getDateTimeInstance 
                                                DateFormat/LONG DateFormat/LONG))))]])
       [:div (str "Page " (inc (:page-number metadata)) " out of " (:total-pages metadata))]
       (when (:prev-page metadata)
         [:div [:a {:href (str "/index/" (if (> (:page-number metadata) (:total-pages metadata))
                                            (:total-pages metadata)
                                            (dec (:page-number metadata))))} "Previous page"]])
       (when (:next-page metadata)
         [:div [:a {:href (str "/index/" (inc (:page-number metadata)))} "Next page"]])]])))

(defn article [item]
  (html
   [:html
    [:head
     [:title (str (:title item) " - Simple blog thing.")]]
    [:body
     [:h1 [:a {:href (str "/index/" (:page_number item))} "Simple blog thing."]]
     (if (:title item)
       (list [:h2 (:title item)]
             [:h3 (str "Created at " (->> item :created_at
                                          (.format (DateFormat/getDateTimeInstance
                                                    DateFormat/LONG DateFormat/LONG))))]
             [:div (:body item)])
       [:h2 "Content not found."])
     (when (:prev item)
       [:div "Previous page: " [:a {:href (str (:prev item))} (:prev_title item)]])
     (when (:next item)
       [:div "Next page: " [:a {:href (str (:next item))} (:next_title item)]])]]))

(defn fnf []
  (html
   [:html
    [:head
     [:title "404 - Simple blog thing."]]
    [:body
     [:h1 [:a {:href "/"} "Simple blog thing."]]
     [:h2 "404 Error: File not found."]]]))

(defn articlefnf [item]
  (html
   [:html
    [:head
     [:title "404 Article not found - Simple blog thing."]]
    [:body
     [:h1 [:a {:href "/"} "Simple blog thing."]]
     [:h2 "404 Error: Article not found."]
     [:div "Next page: " [:a {:href (str (:next item))} (:next_title item)]]]]))
