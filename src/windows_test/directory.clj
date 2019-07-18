(ns windows-test.directory
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.io BufferedReader]))

(def file-separator (System/getProperty "file.separator"))

(def os-name
  (str/lower-case (System/getProperty "os.name")))

(def os-type
  (condp #(str/includes? %2 %1) os-name
    "linux" ::os-linux
    "mac" ::os-mac
    "windows" ::os-windows
    "bsd" ::os-bsd
    ::not-supported-os))

(defn- run-commands [expected-result-lines commands]
  (let [commands ^"[Ljava.lang.String;" (into-array String commands)
        builder (ProcessBuilder. commands)
        process (.start builder)]
    (with-open [reader ^BufferedReader (io/reader (.getInputStream process))]
      (try
        (doall (repeatedly expected-result-lines #(.readLine reader)))
        (finally
          (.destroy process))))))

(defn- get-win-dirs [guids]
  (let [commands (concat ["& {"
                          "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8"
                          "Add-Type @\\\""
                          "using System;"
                          "using System.Runtime.InteropServices;"
                          "public class Dir {"
                          "   [DllImport(\\\"shell32.dll\\\")]"
                          "   private static extern int SHGetKnownFolderPath([MarshalAs(UnmanagedType.LPStruct)] Guid rfid, uint dwFlags, IntPtr hToken, out IntPtr pszPath);"
                          "   public static string GetKnownFolderPath(string rfid) {"
                          "       IntPtr pszPath;"
                          "       if (SHGetKnownFolderPath(new Guid(rfid), 0, IntPtr.Zero, out pszPath) != 0) return \\\"\\\";"
                          "       string path = Marshal.PtrToStringUni(pszPath);"
                          "       Marshal.FreeCoTaskMem(pszPath);"
                          "       return path;"
                          "   }"
                          "}"
                          "\\\"@"]
                         (map #(str "[Dir]::GetKnownFolderPath(\\\"" %  "\\\")") guids)
                         ["}"])]
    (run-commands (count guids) ["powershell.exe" "-Command" (str/join "\n" commands)])))

(defn cache-dir []
  (case os-type
    ::os-mac
    (str/join file-separator [(System/getProperty "user.home")
                              "Library"
                              "Caches"])

    ::os-windows
    (-> ["F1B32785-6FBA-4FCF-9D55-7B8E7F157091"]
        get-win-dirs
        first)

    (let [cache-home (System/getenv "XDG_CACHE_HOME")]
      (if (str/blank? cache-home)
        (str (System/getProperty "user.home") file-separator ".cache")
        cache-home))))
