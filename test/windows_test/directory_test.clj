(ns windows-test.directory-test
  (:require [clojure.test :as t]
            [windows-test.directory :as sut]))

(t/deftest get-win-dirs-test
  (when (= sut/os-type :windows)
    (let [res (sut/get-win-dirs ["5E6C858F-0E22-4760-9AFE-EA3317B67173"
                                 "3EB685DB-65F9-4CF6-A03A-E3EF65729F3D"
                                 "F1B32785-6FBA-4FCF-9D55-7B8E7F157091"])
          [home-dir data-dir data-local-dir] res]
      (t/is (= "home-dir" home-dir))
      (t/is (= "data-dir" data-dir))
      (t/is (= "data-local-dir" data-local-dir)))))
