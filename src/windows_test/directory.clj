(ns windows-test.directory
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(def os-name
  (str/lower-case (System/getProperty "os.name")))

(def os-type
  (condp #(str/includes? %2 %1) os-name
    "linux" :linux
    "mac" :mac
    "windows" :windows
    "bsd" :bsd
    :unsupported-os))

(defn run-commands [expected-result-lines commands]
  (let [builder (ProcessBuilder. (into-array String commands))
        process (.start builder)]
    (with-open [reader (io/reader (.getInputStream process))]
      (try
        (doall (repeatedly expected-result-lines #(.readLine reader)))
        (finally
          (.destroy process))))))

(defn get-win-dirs [guids]
  (let [buf (reduce (fn [res guid]
                      (str res "[Dir]::GetKnownFolderPath(\\\"" guid "\\\")\n"))
                    "" guids)]
    (run-commands (count guids)
                  ["powershell.exe"
                   "-Command"
                   (str "& {\n"
                        "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8\n"
                        "Add-Type @\\\"\n"
                        "using System;\n"
                        "using System.Runtime.InteropServices;\n"
                        "public class Dir {\n"
                        "   [DllImport(\\\"shell32.dll\\\")]\n"
                        "   private static extern int SHGetKnownFolderPath([MarshalAs(UnmanagedType.LPStruct)] Guid rfid, uint dwFlags, IntPtr hToken, out IntPtr pszPath);\n"
                        "   public static string GetKnownFolderPath(string rfid) {\n"
                        "       IntPtr pszPath;\n"
                        "       if (SHGetKnownFolderPath(new Guid(rfid), 0, IntPtr.Zero, out pszPath) != 0) return \\\"\\\";\n"
                        "       string path = Marshal.PtrToStringUni(pszPath);\n"
                        "       Marshal.FreeCoTaskMem(pszPath);\n"
                        "       return path;\n"
                        "   }\n"
                        "}\n"
                        "\\\"@\n"
                        buf
                        "}")])))

(comment
  (run-commands 3 ["ls" "-l"])
  )
