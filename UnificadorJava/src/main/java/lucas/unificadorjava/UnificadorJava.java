package lucas.unificadorjava;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class UnificadorJava extends JFrame {
    private JPanel mainPanel;
    private JPanel dropPanel;
    private JLabel instructionLabel;
    private JButton selectButton;
    private File selectedFile;
    private static File tempDir;

    public UnificadorJava() {
        super("Unificador de Archivos Java");
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dropPanel = new JPanel(new BorderLayout());
        dropPanel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 5, 5));
        dropPanel.setPreferredSize(new Dimension(480, 250));

        instructionLabel = new JLabel("<html><center>Arrastra aquí una carpeta,<br>archivo ZIP o RAR</center></html>", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dropPanel.add(instructionLabel, BorderLayout.CENTER);

        selectButton = new JButton("O selecciona manualmente");
        selectButton.setFont(new Font("Arial", Font.PLAIN, 12));
        selectButton.addActionListener(e -> selectFile());

        mainPanel.add(dropPanel, BorderLayout.CENTER);
        mainPanel.add(selectButton, BorderLayout.SOUTH);

        setupDragAndDrop();

        add(mainPanel);
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setDialogTitle("Selecciona una carpeta o archivo");

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".zip") ||
                        f.getName().toLowerCase().endsWith(".rar");
            }
            public String getDescription() {
                return "Carpetas, archivos ZIP y RAR";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            processFile(selectedFile);
        }
    }

    private void setupDragAndDrop() {
        DropTarget dropTarget = new DropTarget(dropPanel, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dropPanel.setBorder(BorderFactory.createDashedBorder(Color.BLUE, 5, 5));
                instructionLabel.setForeground(Color.BLUE);
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                dropPanel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 5, 5));
                instructionLabel.setForeground(Color.BLACK);
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) { }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) { }

            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> droppedFiles = (List<File>) event.getTransferable()
                            .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);

                    if (!droppedFiles.isEmpty()) {
                        selectedFile = droppedFiles.get(0);
                        processFile(selectedFile);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Error al procesar el archivo: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    dropPanel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 5, 5));
                    instructionLabel.setForeground(Color.BLACK);
                }
            }
        });
        dropPanel.setDropTarget(dropTarget);
    }

    private List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        findJavaFilesRecursive(directory, javaFiles);
        return javaFiles;
    }

    private void findJavaFilesRecursive(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFilesRecursive(file, javaFiles);
                } else if (file.getName().toLowerCase().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }

    private void processFile(File file) {
        try {
            File workingDirectory;
            String fileName = file.getName().toLowerCase();

            if (file.isFile()) {
                if (fileName.endsWith(".zip")) {
                    workingDirectory = extractZip(file);
                } else if (fileName.endsWith(".rar")) {
                    workingDirectory = extractRar(file);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Solo se aceptan archivos ZIP, RAR o carpetas",
                            "Formato no soportado",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                workingDirectory = file;
            }

            List<File> files = findJavaFiles(workingDirectory);
            if (files.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron archivos .java",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Organizar archivos por paquetes
            Map<String, List<File>> packageMap = new TreeMap<>();
            for (File javaFile : files) {
                String packagePath = getRelativePath(workingDirectory, javaFile.getParentFile());
                packageMap.computeIfAbsent(packagePath, k -> new ArrayList<>()).add(javaFile);
            }

            // Mostrar diálogo de selección con vista previa
            showFileSelectionDialog(packageMap, workingDirectory);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }
    }

    /**
     * Muestra el diálogo para seleccionar los archivos con checkboxes y la vista previa.
     * Se incluye un botón para guardar (cerrando el diálogo) y otro para copiar al portapapeles sin cerrar.
     */
    private void showFileSelectionDialog(Map<String, List<File>> packageMap, File workingDirectory) {
        JDialog selectionDialog = new JDialog(this, "Seleccionar archivos a unificar", true);
        selectionDialog.setLayout(new BorderLayout(10, 10));

        // Declarar la vista previa antes de usarse en los listeners
        JTextArea previewTextArea = new JTextArea();
        previewTextArea.setEditable(false);
        previewTextArea.setMargin(new Insets(5, 5, 5, 5));

        // Panel para el listado de checkboxes
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        Map<JCheckBox, File> checkboxMap = new HashMap<>();

        // Para cada paquete
        for (Map.Entry<String, List<File>> entry : packageMap.entrySet()) {
            JPanel packagePanel = new JPanel();
            packagePanel.setLayout(new BoxLayout(packagePanel, BoxLayout.Y_AXIS));
            packagePanel.setBorder(BorderFactory.createTitledBorder("Paquete: " + entry.getKey()));
            for (File file : entry.getValue()) {
                JCheckBox checkbox = new JCheckBox(file.getName(), true);
                checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                // Al cambiar la selección, se actualiza la vista previa
                checkbox.addActionListener(e -> updatePreview(checkboxMap, workingDirectory, previewTextArea));
                checkboxMap.put(checkbox, file);
                packagePanel.add(checkbox);
            }
            checkboxPanel.add(packagePanel);
        }

        // Panel para botones generales
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectAllButton = new JButton("Seleccionar Todo");
        JButton deselectAllButton = new JButton("Deseleccionar Todo");
        selectAllButton.addActionListener(e -> {
            checkboxMap.keySet().forEach(cb -> cb.setSelected(true));
            updatePreview(checkboxMap, workingDirectory, previewTextArea);
        });
        deselectAllButton.addActionListener(e -> {
            checkboxMap.keySet().forEach(cb -> cb.setSelected(false));
            updatePreview(checkboxMap, workingDirectory, previewTextArea);
        });
        topPanel.add(selectAllButton);
        topPanel.add(deselectAllButton);

        // Área de scroll para los checkboxes
        JScrollPane checkboxScrollPane = new JScrollPane(checkboxPanel);
        checkboxScrollPane.setPreferredSize(new Dimension(300, 400));

        // Panel de Vista Previa
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Vista Previa"));
        JScrollPane previewScrollPane = new JScrollPane(previewTextArea);
        previewPanel.add(previewScrollPane, BorderLayout.CENTER);

        // Dividir en dos: listado y vista previa
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, checkboxScrollPane, previewPanel);
        splitPane.setDividerLocation(300);
        splitPane.setContinuousLayout(true);

        // Panel de acciones
        JPanel actionPanel = new JPanel();
        JButton acceptButton = new JButton("Aceptar");
        JButton copyButton = new JButton("📋 Copiar Todo");
        JButton cancelButton = new JButton("Cancelar");
        actionPanel.add(acceptButton);
        actionPanel.add(copyButton);
        actionPanel.add(cancelButton);

        // Actualizar la vista previa inicial
        updatePreview(checkboxMap, workingDirectory, previewTextArea);

        // Acción de Aceptar: guarda el archivo y cierra el diálogo
        acceptButton.addActionListener(e -> {
            Map<String, List<File>> selectedPackageMap = gatherSelectedFiles(checkboxMap, workingDirectory);
            if (selectedPackageMap.isEmpty()) {
                JOptionPane.showMessageDialog(selectionDialog, "No hay archivos seleccionados.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String unifiedCode = unifyFiles(selectedPackageMap);
            processSelectedFiles(unifiedCode, workingDirectory);
            selectionDialog.dispose();
        });

        // Acción de Copiar: copia el código unificado al portapapeles (sin cerrar el diálogo)
        copyButton.addActionListener(e -> {
            Map<String, List<File>> selectedPackageMap = gatherSelectedFiles(checkboxMap, workingDirectory);
            if (selectedPackageMap.isEmpty()) {
                JOptionPane.showMessageDialog(selectionDialog, "No hay archivos seleccionados.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String unifiedCode = unifyFiles(selectedPackageMap);
            StringSelection selection = new StringSelection(unifiedCode);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            JOptionPane.showMessageDialog(selectionDialog, "Código copiado al portapapeles", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        });

        cancelButton.addActionListener(e -> selectionDialog.dispose());

        selectionDialog.add(topPanel, BorderLayout.NORTH);
        selectionDialog.add(splitPane, BorderLayout.CENTER);
        selectionDialog.add(actionPanel, BorderLayout.SOUTH);
        selectionDialog.pack();
        selectionDialog.setLocationRelativeTo(this);
        selectionDialog.setVisible(true);
    }

    /**
     * Actualiza el contenido de la vista previa (unifica los archivos seleccionados).
     */
    private void updatePreview(Map<JCheckBox, File> checkboxMap, File workingDirectory, JTextArea previewTextArea) {
        Map<String, List<File>> selectedPackageMap = gatherSelectedFiles(checkboxMap, workingDirectory);
        String unifiedCode = unifyFiles(selectedPackageMap);
        previewTextArea.setText(unifiedCode);
    }

    /**
     * Recolecta los archivos seleccionados en el Map de checkboxes y los agrupa por paquete.
     */
    private Map<String, List<File>> gatherSelectedFiles(Map<JCheckBox, File> checkboxMap, File workingDirectory) {
        Map<String, List<File>> selectedPackageMap = new TreeMap<>();
        for (Map.Entry<JCheckBox, File> entry : checkboxMap.entrySet()) {
            if (entry.getKey().isSelected()) {
                File file = entry.getValue();
                String packagePath = getRelativePath(workingDirectory, file.getParentFile());
                selectedPackageMap.computeIfAbsent(packagePath, k -> new ArrayList<>()).add(file);
            }
        }
        return selectedPackageMap;
    }

    /**
     * Une los archivos agrupados por paquete en un único String.
     * Se añade como comentario el nombre del paquete y del archivo, y luego su contenido.
     * (No se agregan mensajes de error)
     */
    private String unifyFiles(Map<String, List<File>> packageMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<File>> entry : packageMap.entrySet()) {
            sb.append("//====\n");
            sb.append("// Paquete: ").append(entry.getKey()).append("\n");
            sb.append("//====\n\n");
            for (File javaFile : entry.getValue()) {
                sb.append("//----\n");
                sb.append("// Archivo: ").append(javaFile.getName()).append("\n");
                sb.append("//----\n\n");
                try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    sb.append("\n");
                } catch (IOException ex) {
                    sb.append("// Error leyendo archivo: ").append(javaFile.getName()).append("\n\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Solicita el nombre y la ubicación donde guardar el código unificado, y escribe el contenido en un archivo.
     */
    private void processSelectedFiles(String unifiedCode, File workingDirectory) {
        String outputFileName = JOptionPane.showInputDialog(this,
                "Se procesarán los archivos seleccionados.\nIntroduce el nombre del archivo de salida (sin extensión):",
                "Nombre del archivo",
                JOptionPane.INFORMATION_MESSAGE);
        if (outputFileName == null || outputFileName.trim().isEmpty()) {
            return;
        }

        String[] opciones = {"En la carpeta seleccionada", "En la carpeta Descargas", "Elegir ubicación"};
        int opcion = JOptionPane.showOptionDialog(this,
                "¿Dónde quieres guardar el archivo de salida?",
                "Guardar archivo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        String outputPath = getOutputPath(opcion, outputFileName, workingDirectory);
        if (outputPath == null)
            return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write(unifiedCode);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar el archivo: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Archivos unidos exitosamente en:\n" + outputPath,
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        try {
            Desktop.getDesktop().open(new File(outputPath).getParentFile());
        } catch (IOException ex) {
            // Se ignora si ocurre algún error al abrir la carpeta
        }
    }

    private String getRelativePath(File baseDir, File targetDir) {
        String basePath = baseDir.getAbsolutePath();
        String targetPath = targetDir.getAbsolutePath();

        if (targetPath.startsWith(basePath)) {
            String relativePath = targetPath.substring(basePath.length());
            return relativePath.isEmpty() ? "Raíz" :
                    relativePath.replace(File.separator, ".").replaceAll("^\\.", "");
        }
        return "Desconocido";
    }

    private String getOutputPath(int opcion, String outputFileName, File workingDirectory) {
        switch (opcion) {
            case 1: // En la carpeta Descargas
                return System.getProperty("user.home") + File.separator + "Downloads" +
                        File.separator + outputFileName + ".txt";
            case 2: // Elegir ubicación
                JFileChooser saveChooser = new JFileChooser();
                saveChooser.setDialogTitle("Selecciona dónde guardar el archivo de salida");
                saveChooser.setSelectedFile(new File(outputFileName + ".txt"));
                int saveResult = saveChooser.showSaveDialog(this);
                if (saveResult == JFileChooser.APPROVE_OPTION) {
                    String path = saveChooser.getSelectedFile().getAbsolutePath();
                    return path.endsWith(".txt") ? path : path + ".txt";
                }
                return null;
            default: // En la carpeta seleccionada
                return workingDirectory.getPath() + File.separator + outputFileName + ".txt";
        }
    }

    private static File extractZip(File zipFile) throws IOException {
        tempDir = Files.createTempDirectory("javaUnificador").toFile();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(tempDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                }
                new File(newFile.getParent()).mkdirs();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
        return tempDir;
    }

    private static File extractRar(File rarFile) throws Exception {
        tempDir = Files.createTempDirectory("javaUnificador").toFile();

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(rarFile, "r")) {
            IInArchive archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
            ISimpleInArchive simpleArchive = archive.getSimpleInterface();

            for (ISimpleInArchiveItem item : simpleArchive.getArchiveItems()) {
                String path = item.getPath();
                File newFile = new File(tempDir, path);

                if (item.isFolder()) {
                    newFile.mkdirs();
                    continue;
                }

                new File(newFile.getParent()).mkdirs();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    item.extractSlow(data -> {
                        try {
                            fos.write(data);
                            return data.length;
                        } catch (IOException e) {
                            return 0;
                        }
                    });
                }
            }
        }
        return tempDir;
    }

    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UnificadorJava frame = new UnificadorJava();
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}