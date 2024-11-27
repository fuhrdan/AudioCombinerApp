import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.sound.sampled.*;

public class AudioCombinerApp extends JFrame {

    private File audioFile1 = null;
    private File audioFile2 = null;
    private JLabel file1Label;
    private JLabel file2Label;
    private JSlider positionSlider;
    private JSlider volumeSlider;

    public AudioCombinerApp() {
        setTitle("Audio Combiner");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel);

        // File selection section
        file1Label = new JLabel("First File: Not Selected");
        file2Label = new JLabel("Second File: Not Selected");
        JButton selectFile1Button = new JButton("Select First Audio File");
        JButton selectFile2Button = new JButton("Select Second Audio File");

        selectFile1Button.addActionListener(e -> selectFile(1));
        selectFile2Button.addActionListener(e -> selectFile(2));

        panel.add(file1Label);
        panel.add(selectFile1Button);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(file2Label);
        panel.add(selectFile2Button);

        // Sliders section
        positionSlider = new JSlider(0, 100, 50);
        volumeSlider = new JSlider(0, 100, 50);
        positionSlider.setMajorTickSpacing(25);
        positionSlider.setPaintTicks(true);
        positionSlider.setPaintLabels(true);

        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        panel.add(new JLabel("Overlay Start Position (%)"));
        panel.add(positionSlider);
        panel.add(new JLabel("Second File Volume (%)"));
        panel.add(volumeSlider);

        // Combine button
        JButton combineButton = new JButton("Combine Files");
        combineButton.addActionListener(e -> combineAudioFiles());
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(combineButton);
    }

    private void selectFile(int fileNumber) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files (*.wav)", "wav"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (fileNumber == 1) {
                audioFile1 = selectedFile;
                file1Label.setText("First File: " + selectedFile.getName());
            } else {
                audioFile2 = selectedFile;
                file2Label.setText("Second File: " + selectedFile.getName());
            }
        }
    }

    private void combineAudioFiles() {
        if (audioFile1 == null || audioFile2 == null) {
            JOptionPane.showMessageDialog(this, "Please select both audio files.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Read audio files
            AudioInputStream stream1 = AudioSystem.getAudioInputStream(audioFile1);
            AudioInputStream stream2 = AudioSystem.getAudioInputStream(audioFile2);

            AudioFormat format1 = stream1.getFormat();
            AudioFormat format2 = stream2.getFormat();

            if (!format1.matches(format2)) {
                JOptionPane.showMessageDialog(this, "Audio formats must match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte[] data1 = stream1.readAllBytes();
            byte[] data2 = stream2.readAllBytes();

            int position = positionSlider.getValue(); // In percentage
            int volume = volumeSlider.getValue();     // In percentage

            // Apply overlay
            int startIndex = (int) ((position / 100.0) * data1.length);
            for (int i = 0; i < data2.length && startIndex + i < data1.length; i++) {
                int sample1 = data1[startIndex + i];
                int sample2 = (int) (data2[i] * (volume / 100.0));
                data1[startIndex + i] = (byte) Math.min(127, Math.max(-128, sample1 + sample2));
            }

            // Write combined file
            File combinedFile = new File("combined_audio.wav");
            AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(data1), format1, data1.length), AudioFileFormat.Type.WAVE, combinedFile);

            JOptionPane.showMessageDialog(this, "Files combined successfully! Output: combined_audio.wav", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error combining files: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AudioCombinerApp app = new AudioCombinerApp();
            app.setVisible(true);
        });
    }
}
