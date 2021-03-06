(ns clearcut.schema
  (:require [clojure.string :as string]
            [clearcut.debug :refer [log debug-assert]]))

; -- static formatting of log args ------------------------------------------------------------------------------------------
; keep this in sync with dynamic version!

(defn style-param? [param]
  (= (first param) :style-param))

(defn format-param? [param]
  (= (first param) :format-param))

(defn native-param? [param]
  (= (first param) :native-param))

(defn formatted? [param]
  (or (style-param? param) (format-param? param)))

(defn valid? [param]
  (or (native-param? param) (style-param? param) (format-param? param)))

(defn prepare-formatted-log-args [params]
  (loop [result []
         format []
         items (seq params)
         carry nil]
    (if-not (nil? items)
      (let [item (first items)]
        (cond
          (style-param? item) (let [new-format (conj format "%c")
                                    new-result (conj result (:style (second item)))]
                                (recur new-result new-format (next items) nil))
          (format-param? item) (recur result format (next items) (str "%" (:fmtstr (second item))))
          :else (do
                  (debug-assert (native-param? item))
                  (let [val (second item)
                        new-format (conj format (or carry (if (string? val) "%s " "%o ")))
                        new-result (conj result val)]
                    (recur new-result new-format (next items) nil)))))
      (concat [(string/trim (string/join format))] result))))

(defn prepare-log-args [params]
  (debug-assert (every? valid? params))
  (if (some formatted? params)
    (prepare-formatted-log-args params)
    (map second params)))
