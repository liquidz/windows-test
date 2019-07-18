(ns windows-test.directory-test
  (:require [clojure.test :as t]
            [windows-test.directory :as sut]))

(t/deftest cache-dir-windows-test
  (when (= ::sut/os-windows sut/os-type)
    (println "DEBUG:" (sut/cache-dir))
    (t/is (re-seq #"^C:\\Users\\.+\\AppData\\Local" (sut/cache-dir)))))
