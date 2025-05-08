# JavaSourceToTxt ☕

DESCARGAR ÚLTIMA VERSIÓN V3.0:
https://github.com/LucatorL/JavaSourceToTxt/releases/download/V2/UnificadorJava-3.0.jar


A desktop application built with Java Swing designed to **concatenate the text content** of multiple `.java` source files into **a single plain text (.txt) file**.

**⚠️ Important Note:** This tool *does not* perform any semantic code merging, linking, or compilation. It simply copies the text from selected `.java` files sequentially into one `.txt` file, adding comments to mark the origin of each code block. It's a utility for presentation and submission, not for building runnable code artifacts.

This tool is ideal for:
*   Students who need to submit all source code for a project as a single document.
*   Easily sharing multiple source files as one block of text.
*   Reviewing code from different files within a single view.
*   A quick and easy way to send ALL of your project's code and context to an AI

**(Optional: Add a screenshot here)**

![image](https://github.com/user-attachments/assets/eeb5e363-bd5b-4fd4-9858-0ca016128dd7)


![image](https://github.com/user-attachments/assets/19ecead5-74e8-4f44-95bb-b0d2f206a1bb)



## Key Features ✨

*   **Intuitive GUI:** Easy-to-use interface powered by Java Swing.
*   **Drag & Drop Support:** Drag folders, `.zip`, or `.rar` files containing `.java` source directly onto the application window.
*   **Manual Selection:** Option to browse and select folders or archive files using a file chooser.
*   **Automatic Extraction:** Decompresses `.zip` (natively) and `.rar` files (requires 7-Zip native libraries) into a temporary location to access `.java` files.
*   **Flexible File Selection:**
    *   Lists discovered `.java` files, grouped by their package structure.
    *   Checkboxes allow selecting/deselecting individual files for inclusion.
    *   "Select All" / "Deselect All" buttons for convenience.
*   **Real-time Preview:** See a live preview of the concatenated `.txt` output as you select files.
*   **Multiple Output Options:**
    *   **Save to File:** Saves the combined content to a `.txt` file.
    *   **Copy to Clipboard:** Copies the entire combined content directly to the system clipboard.
*   **Clear Output Format:** The final `.txt` file includes comments indicating the original package and filename before each respective code segment.
*   **Location Choices:** Save the resulting `.txt` file in the source directory, your Downloads folder, or choose a custom location.

## Language 🌐

Currently, the user interface is only available in **Spanish**.

Future localization (e.g., to English or other languages) will be considered if there's significant interest and downloads from the non-Spanish speaking community. Feel free to open an issue in the repository if you'd like to see support for your language!

## Requirements (Built for JRE 17) ⚙️

*   **Java Runtime Environment (JRE):** Version 17 or higher is required to run the application.
*   **Native 7-Zip Libraries (Optional):** Only needed if you want to extract `.rar` files. The `sevenzipjbinding` library used for RAR extraction relies on native 7-Zip libraries specific to your operating system. You might need to install them separately. ZIP extraction works out-of-the-box. See [sevenzipjbinding documentation](https://sevenzipjbinding.sourceforge.net/installation.html) for details.

## Usage 🚀

**Option 1: Download the Executable JAR (Recommended)**

1.  Go to the [**Releases**](https://github.com/LucatorL/JavaSourceToTxt/releases) section of this repository (You'll need to create releases yourself).
2.  Download the latest `JavaSourceToTxt.jar` file.
3.  Ensure you have JRE 17 or higher installed.
4.  Run the application, typically by double-clicking the `.jar` file or using the terminal:
    ```bash
    java -jar JavaSourceToTxt.jar
    ```
5.  If RAR extraction fails, ensure the native 7-Zip libraries are correctly installed and accessible.

**Option 2: Compile from Source Code**

1.  Clone this repository:
    ```bash
    git clone https://github.com/LucatorL/JavaSourceToTxt.git
    cd JavaSourceToTxt
    ```
2.  You need a Java Development Kit (JDK) version 17 or higher.
3.  This project depends on `sevenzipjbinding` (for RAR support). You'll need to manage this dependency. If you're not using a build tool like Maven or Gradle, download the `sevenzipjbinding.jar` and `sevenzipjbinding-all-platforms.jar` from [their official site](https://sevenzipjbinding.sourceforge.net/download.html) and include them in your classpath.
4.  Compile the main Java file (assuming the package structure `lucas.unificadorjava` remains from your original code - update the path if you change it). Make sure to include the dependency JARs in the classpath:
    ```bash
    # On Linux/macOS:
    javac -cp path/to/sevenzipjbinding.jar:path/to/sevenzipjbinding-all-platforms.jar:. lucas/unificadorjava/UnificadorJava.java

    # On Windows:
    javac -cp path\to\sevenzipjbinding.jar;path\to\sevenzipjbinding-all-platforms.jar;. lucas\unificadorjava\UnificadorJava.java
    ```
5.  Run the main class, again including the JARs in the classpath. You might also need to specify the path to the native libraries for `sevenzipjbinding`:
    ```bash
    # On Linux/macOS:
    java -cp path/to/sevenzipjbinding.jar:path/to/sevenzipjbinding-all-platforms.jar:. -Djava.library.path=path/to/native/libs lucas.unificadorjava.UnificadorJava

    # On Windows:
    java -cp path\to\sevenzipjbinding.jar;path\to\sevenzipjbinding-all-platforms.jar;. -Djava.library.path=path\to\native\libs lucas.unificadorjava.UnificadorJava
    ```
    *(Note: Building and running Java projects with native dependencies manually can be tricky. Consider using a build tool like Maven or Gradle in the future for easier dependency management and building).*

## Dependencies 📦

*   **Java Swing:** Standard part of Java SE.
*   **sevenzipjbinding:** External library for `.rar` file extraction (optional functionality).

## License 📄
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
