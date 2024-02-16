import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.text.DefaultCaret;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;

public class PhotoProcessor {

     public static String imageSize="1280x720";
     public static String imageQuality="80";
     public static String thumbnailSize="300x300";
     public static String watermarkText="";
     public static String watermarkFont="Liberation-Sans";
     public static String watermarkFontSize="auto";
     public static String watermarkMargin="auto";
     public static String watermarkRotation="30";
     public static String watermarkDistortion="50";
     public static String watermarkOpacity="30";
     public static String watermarkColor="white";
     public static String scriptPath="./PhotoProcessor.sh";
     public static Boolean removeOriginal=false;
     public static String windowTitle="Photo Processor";
     public static String logoPath="./logo.png";

    public static void main(String[] args) {

         try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("defaults.json"));
            JSONObject jsonObject = (JSONObject) obj;
            imageSize = (String) jsonObject.get("imageSize");
            imageQuality = (String) jsonObject.get("imageQuality");
            thumbnailSize = (String) jsonObject.get("thumbnailSize");
            watermarkText = (String) jsonObject.get("watermarkText");
            watermarkFont = (String) jsonObject.get("watermarkFont");
            watermarkFontSize = (String) jsonObject.get("watermarkFontSize");
            watermarkMargin = (String) jsonObject.get("watermarkMargin");
            watermarkRotation = (String) jsonObject.get("watermarkRotation");
            watermarkDistortion = (String) jsonObject.get("watermarkDistortion");
            watermarkOpacity = (String) jsonObject.get("watermarkOpacity");
            watermarkColor = (String) jsonObject.get("watermarkColor");
            removeOriginal = (Boolean) jsonObject.get("removeOriginal");
            scriptPath = (String) jsonObject.get("scriptPath");
            windowTitle = (String) jsonObject.get("windowTitle");
            logoPath = (String) jsonObject.get("logoPath");


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame(windowTitle);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Load the face.png image (if available)
        BufferedImage logoImage = null;
        try {
            logoImage = ImageIO.read(new File(logoPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (logoImage != null) {
            frame.setIconImage(logoImage);
        }

        // Create components
        JButton chooseFolderButton = new JButton("Choose Gallery");
        JTextField imageSizeTextField = new JTextField(imageSize);
        JTextField imageQualityTextField = new JTextField(imageQuality);
        JCheckBox removeOriginalCheckBox = new JCheckBox("",removeOriginal);
        JTextField thumbnailSizeTextField = new JTextField(thumbnailSize);
        JTextField selectedFolderTextField = new JTextField(20);
        JTextField watermarkTextTextField = new JTextField(watermarkText);
        JTextField watermarkFontTextField = new JTextField(watermarkFont);
        JTextField watermarkFontSizeTextField = new JTextField(watermarkFontSize);
        JTextField watermarkMarginTextField = new JTextField(watermarkMargin);
        JTextField watermarkRotationTextField = new JTextField(watermarkRotation);
        JTextField watermarkDistortionTextField = new JTextField(watermarkDistortion);
        JTextField watermarkOpacityTextField = new JTextField(watermarkOpacity);
        JTextField watermarkColorTextField = new JTextField(watermarkColor);
        JButton locateScriptButton = new JButton("Locate Script");
        JTextField scriptPathTextField = new JTextField(scriptPath);
        JButton runScriptButton = new JButton("Process Photos");
        JTextArea outputTextArea = new JTextArea(10, 30);
        outputTextArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) outputTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Add action listener to chooseFolderButton
        chooseFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String selectedFolder = fileChooser.getSelectedFile().getAbsolutePath();
                    selectedFolderTextField.setText(selectedFolder);
                }
            }
        });

        locateScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser scriptChooser = new JFileChooser();
                File workingDirectory = new File(System.getProperty("user.dir"));
                scriptChooser.setCurrentDirectory(workingDirectory);
                int result = scriptChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String scriptPath = scriptChooser.getSelectedFile().getAbsolutePath();
                    scriptPathTextField.setText(scriptPath);
                }
            }
        });

        // Add action listener to runScriptButton
        runScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFolder = selectedFolderTextField.getText();
                String removeOriginal = "";
                if (removeOriginalCheckBox.isSelected()) {
                    removeOriginal = "-removeoriginal ";
                }
                String imageSize = imageSizeTextField.getText();
                String imageQuality = imageQualityTextField.getText();
                String thumbnailSize = thumbnailSizeTextField.getText();
                String watermarkText = watermarkTextTextField.getText();
                String watermarkFont = watermarkFontTextField.getText();
                String watermarkFontSize = watermarkFontSizeTextField.getText();
                String watermarkMargin = watermarkMarginTextField.getText();
                String watermarkRotation = watermarkRotationTextField.getText();
                String watermarkDistortion = watermarkDistortionTextField.getText();
                String watermarkOpacity = watermarkOpacityTextField.getText();
                String watermarkColor = watermarkColorTextField.getText();
                String scriptPath = scriptPathTextField.getText();
                String processString = scriptPath + " " + removeOriginal +
                                    "-quality \"" + imageQuality + "\" "+
                                    "-resize \"" + imageSize + "\" " +
                                    "-thumbnail \"" + thumbnailSize + "\" " +
                                    "-wtext \"" + watermarkText + "\" " +
                                    "-wfont \"" + watermarkFont + "\" " +
                                    "-wfontsize \"" + watermarkFontSize + "\" " +
                                    "-wmargin \"" + watermarkMargin + "\" " +
                                    "-wrotation \"" + watermarkRotation + "\" " +
                                    "-wdistortion \"" + watermarkDistortion + "\" " +
                                    "-walpha \"" + watermarkOpacity + "\" " +
                                    "-wcolor \"" + watermarkColor + "\" \"" +
                                    selectedFolder + "\"";
                outputTextArea.append("Running: " + processString + "\n");

                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",processString);
                    Process process = processBuilder.start();

                    // Read the script output in a separate thread
                    new Thread(() -> {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                final String outputLine = line; // Final variable for lambda expression
                                SwingUtilities.invokeLater(() -> {
                                    outputTextArea.append(outputLine + "\n");
                                });
                            }
                            reader.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                    new Thread(() -> {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                final String outputLine = line; // Final variable for lambda expression
                                SwingUtilities.invokeLater(() -> {
                                    outputTextArea.append(outputLine + "\n");
                                });
                            }
                            reader.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Create layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(15, 1));
        inputPanel.add(chooseFolderButton);
        inputPanel.add(selectedFolderTextField);
        inputPanel.add(new JLabel("Shrink to fit ($wx$h|off):"));
        inputPanel.add(imageSizeTextField);
        inputPanel.add(new JLabel("JPEG Quality ($n%|default):"));
        inputPanel.add(imageQualityTextField);
        inputPanel.add(new JLabel("Thumbnail ($wx$h|off):"));
        inputPanel.add(thumbnailSizeTextField);
        inputPanel.add(new JLabel("Watermark Text:"));
        inputPanel.add(watermarkTextTextField);
        inputPanel.add(new JLabel("Watermark Font:"));
        inputPanel.add(watermarkFontTextField);
        inputPanel.add(new JLabel("Watermark Font Size ($px|auto):"));
        inputPanel.add(watermarkFontSizeTextField);
        inputPanel.add(new JLabel("Watermark Margin ($px|auto):"));
        inputPanel.add(watermarkMarginTextField);
        inputPanel.add(new JLabel("Watermark Rotation ($n):"));
        inputPanel.add(watermarkRotationTextField);
        inputPanel.add(new JLabel("Watermark Distortion ($n%|off):"));
        inputPanel.add(watermarkDistortionTextField);
        inputPanel.add(new JLabel("Watermark Alpha ($n%):"));
        inputPanel.add(watermarkOpacityTextField);
        inputPanel.add(new JLabel("Watermark Color (e.g. green|#00ff00):"));
        inputPanel.add(watermarkColorTextField);
        inputPanel.add(new JLabel("Remove Original:"));
        inputPanel.add(removeOriginalCheckBox);
        inputPanel.add(new JLabel("Script Path:"));
        inputPanel.add(scriptPathTextField);
        inputPanel.add(runScriptButton);
        inputPanel.add(locateScriptButton);
        mainPanel.add(inputPanel, BorderLayout.WEST);
        mainPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
