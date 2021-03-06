(defproject binaryage/clearcut "0.1.0-SNAPSHOT"
  :description "Unified logging overlay on top of console.log and clojure.tools.logging."
  :url "https://github.com/binaryage/clearcut"
  :license {:name         "MIT License"
            :url          "http://opensource.org/licenses/MIT"
            :distribution :repo}

  :scm {:name "git"
        :url  "https://github.com/binaryage/clearcut"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha16" :scope "provided"]
                 [org.clojure/clojurescript "1.9.542" :scope "provided"]
                 [org.clojure/tools.logging "0.3.1" :scope "provided"]
                 [binaryage/env-config "0.2.0"]
                 [funcool/cuerdas "2.0.3"]
                 [environ "1.1.0"]

                 [binaryage/devtools "0.9.4" :scope "test"]
                 [figwheel "0.5.10" :scope "test"]
                 [clj-logging-config "1.9.12" :scope "test"]
                 [clansi "1.0.0" :scope "test"]]

  :clean-targets ^{:protect false} ["target"
                                    "test/resources/.compiled"]

  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-shell "0.5.0"]
            [lein-figwheel "0.5.10"]]

  ; this is just for IntelliJ + Cursive to play well
  :source-paths ["src/lib"]
  :test-paths ["test/src"]
  :resource-paths ^:replace ["test/resources"
                             "scripts"]

  :cljsbuild {:builds {}}                                                                                                     ; prevent https://github.com/emezeske/lein-cljsbuild/issues/413

  :profiles {:nuke-aliases
             {:aliases ^:replace {}}

             :test-refresh
             {:plugins [[com.jakemccrary/lein-test-refresh "0.17.0"]]}

             :lib
             ^{:pom-scope :provided}                                                                                          ; ! to overcome default jar/pom behaviour, our :dependencies replacement would be ignored for some reason
             [:nuke-aliases
              {:dependencies   ~(let [project-str (slurp "project.clj")
                                      project (->> project-str read-string (drop 3) (apply hash-map))
                                      test-dep? #(->> % (drop 2) (apply hash-map) :scope (= "test"))
                                      non-test-deps (remove test-dep? (:dependencies project))]
                                  (with-meta (vec non-test-deps) {:replace true}))                                            ; so ugly!
               :source-paths   ^:replace ["src/lib"]
               :resource-paths ^:replace []
               :test-paths     ^:replace []}]

             :clojure18
             {:dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                             [clojure-future-spec "1.9.0-alpha16-1" :scope "provided"]]}

             :cooper
             {:plugins [[lein-cooper "1.2.2"]]}

             :figwheel
             {:figwheel {:server-port          7118
                         :server-logfile       ".figwheel/log.txt"
                         :validate-interactive false
                         :repl                 false}}

             :circus
             {:source-paths ["src/lib"
                             "test/src/circus"
                             "test/src/arena"
                             "test/src/tools"]}

             :testing-basic-onone
             {:cljsbuild {:builds {:basic-onone
                                   {:source-paths ["src/lib"
                                                   "test/src/runner"
                                                   "test/src/tools"
                                                   "test/src/tests"]
                                    :compiler     {:output-to       "test/resources/.compiled/basic_onone/main.js"
                                                   :output-dir      "test/resources/.compiled/basic_onone"
                                                   :asset-path      ".compiled/basic_onone"
                                                   :preloads        [devtools.preload]
                                                   :main            clearcut.runner
                                                   :optimizations   :none
                                                   :external-config {:devtools/config {:dont-detect-custom-formatters true}
                                                                     :clearcut/config {:debug true}}}
                                    :figwheel     true}}}}
             :testing-basic-oadvanced
             {:cljsbuild {:builds {:basic-oadvanced
                                   {:source-paths ["src/lib"
                                                   "test/src/runner"
                                                   "test/src/tools"
                                                   "test/src/tests"]
                                    :compiler     {:output-to       "test/resources/.compiled/basic_oadvanced/main.js"
                                                   :output-dir      "test/resources/.compiled/basic_oadvanced"
                                                   :asset-path      ".compiled/basic_oadvanced"
                                                   :main            clearcut.runner
                                                   :pseudo-names    true
                                                   :optimizations   :advanced
                                                   :external-config {:clearcut/config {:debug true}}}}}}}

             :testing-clojure
             {:dependencies   ^:replace [[org.clojure/clojure "1.9.0-alpha16"]
                                         [org.clojure/clojurescript "1.9.542"]
                                         [org.clojure/tools.logging "0.3.1"]
                                         [binaryage/env-config "0.2.0"]
                                         [funcool/cuerdas "2.0.3"]
                                         [environ "1.1.0"]]
              :source-paths   ^:replace ["src/lib"]
              :resource-paths ^:replace []
              :test-paths     ^:replace ["test/src/clojure"]}

             :auto-testing
             {:cljsbuild {:builds {:basic-onone     {:notify-command ["scripts/rerun-tests.sh" "basic_onone"]}
                                   :basic-oadvanced {:notify-command ["scripts/rerun-tests.sh" "basic_oadvanced"]}}}}


             :dev-basic-onone
             {:cooper {"server"     ["scripts/launch-fixtures-server.sh"]
                       "figwheel"   ["lein" "fig-basic-onone"]
                       "repl-agent" ["scripts/launch-repl-with-agent.sh"]
                       "browser"    ["scripts/launch-test-browser.sh"]}}}

  :aliases {"test"                 ["do"
                                    ["clean"]
                                    ["shell" "scripts/run-tests.sh"]]
            "test-clojure"         ["with-profile" "+nuke-aliases,+testing-clojure" "test"]
            "test-clojure-refresh" ["with-profile" "+nuke-aliases,+testing-clojure,+test-refresh" "test-refresh"]
            "test-all"             ["shell" "scripts/run-all-tests.sh"]
            "dev-functional-tests" ["shell" "scripts/dev-functional-tests.sh"]
            "run-functional-tests" ["do"
                                    ["clean"]
                                    ["shell" "scripts/run-functional-tests.sh"]]
            "run-circus-tests"     ["do"
                                    ["clean"]
                                    ["shell" "scripts/run-circus-tests.sh"]]
            "build-tests"          ["do"
                                    ["with-profile" "+testing-basic-onone" "cljsbuild" "once" "basic-onone"]
                                    ["with-profile" "+testing-basic-oadvanced" "cljsbuild" "once" "basic-oadvanced"]
                                    ["with-profile" "+testing-basic-oadvanced" "cljsbuild" "once" "basic-oadvanced"]]
            "auto-build-tests"     ["do"
                                    ["with-profile" "+testing-basic-onone,+auto-testing" "cljsbuild" "once" "basic-onone"]
                                    ["with-profile" "+testing-basic-oadvanced" "cljsbuild" "once" "basic-oadvanced"]
                                    ["with-profile" "+testing-basic-oadvanced" "cljsbuild" "once" "basic-oadvanced"]]
            "fig-basic-onone"      ["with-profile" "+testing-basic-onone,+figwheel" "figwheel"]
            "auto-basic-onone"     ["with-profile" "+testing-basic-onone,+auto-testing" "cljsbuild" "auto" "basic-onone"]
            "auto-test"            ["do"
                                    ["clean"]
                                    ["auto-build-tests"]]
            "toc"                  ["shell" "scripts/generate-toc.sh"]
            "install"              ["do"
                                    ["shell" "scripts/prepare-jar.sh"]
                                    ["shell" "scripts/local-install.sh"]]
            "jar"                  ["shell" "scripts/prepare-jar.sh"]
            "deploy"               ["shell" "scripts/deploy-clojars.sh"]
            "release"              ["do"
                                    ["clean"]
                                    ["shell" "scripts/check-versions.sh"]
                                    ["shell" "scripts/prepare-jar.sh"]
                                    ["shell" "scripts/check-release.sh"]
                                    ["shell" "scripts/deploy-clojars.sh"]]})
