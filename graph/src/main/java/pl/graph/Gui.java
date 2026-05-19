package pl.graph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class Gui {

    private JFrame frame;
    private GraphPanel graphPanel;

    public void createAndShowGui() {
        frame = new JFrame("Graph Visualizer");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        graphPanel = new GraphPanel();
        frame.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

        frame.add(createToolPanel(), BorderLayout.EAST);
        frame.setJMenuBar(createMenuBar());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("Plik");

        JMenuItem loadTxt = new JMenuItem(new AbstractAction("Wczytaj graf (tekstowy)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    try {
                        Graph g = Graph.loadGraph(f.getAbsolutePath());
                        graphPanel.setGraph(g);
                    } catch (IOException | IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(frame, "Błąd wczytywania: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem loadCoordsTxt = new JMenuItem(new AbstractAction("Wczytaj współrzędne (tekstowe)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Graph g = graphPanel.getGraph();
                if (g == null) { JOptionPane.showMessageDialog(frame, "Brak wczytanego grafu", "Błąd", JOptionPane.ERROR_MESSAGE); return; }
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    ExitCodes res = Graph.loadCoordinatesFromText(g, f.getAbsolutePath());
                    if (res != ExitCodes.SUCCESS) {
                        JOptionPane.showMessageDialog(frame, "Błąd wczytywania współrzędnych: " + res, "Błąd", JOptionPane.ERROR_MESSAGE);
                    } else {
                        graphPanel.setGraph(g);
                    }
                }
            }
        });

        JMenuItem loadCoordsBin = new JMenuItem(new AbstractAction("Wczytaj współrzędne (binarne)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Graph g = graphPanel.getGraph();
                if (g == null) { JOptionPane.showMessageDialog(frame, "Brak wczytanego grafu", "Błąd", JOptionPane.ERROR_MESSAGE); return; }
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    ExitCodes res = Graph.loadCoordinatesFromBinary(g, f.getAbsolutePath());
                    if (res != ExitCodes.SUCCESS) {
                        JOptionPane.showMessageDialog(frame, "Błąd wczytywania współrzędnych: " + res, "Błąd", JOptionPane.ERROR_MESSAGE);
                    } else {
                        graphPanel.setGraph(g);
                    }
                }
            }
        });

        JMenuItem saveTxt = new JMenuItem(new AbstractAction("Zapisz współrzędne (tekstowy)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphPanel == null || graphPanel == null) return;
                Graph g = getGraph();
                if (g == null) { JOptionPane.showMessageDialog(frame, "Brak danych do zapisu", "Błąd", JOptionPane.ERROR_MESSAGE); return; }
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    ExitCodes res = Graph.saveGraphAsText(g, f.getAbsolutePath());
                    if (res != ExitCodes.SUCCESS) JOptionPane.showMessageDialog(frame, "Błąd zapisu: " + res, "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem saveBin = new JMenuItem(new AbstractAction("Zapisz współrzędne (binarny)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Graph g = getGraph();
                if (g == null) { JOptionPane.showMessageDialog(frame, "Brak danych do zapisu", "Błąd", JOptionPane.ERROR_MESSAGE); return; }
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    ExitCodes res = Graph.saveGraphAsBinary(g, f.getAbsolutePath());
                    if (res != ExitCodes.SUCCESS) JOptionPane.showMessageDialog(frame, "Błąd zapisu: " + res, "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem about = new JMenuItem(new AbstractAction("O programie") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Graph Visualizer\nSwing GUI - zgodne z dokumentacją", "O programie", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        file.add(loadTxt);
        file.add(loadCoordsTxt);
        file.add(loadCoordsBin);
        file.addSeparator();
        file.add(saveTxt);
        file.add(saveBin);
        mb.add(file);

        JMenu help = new JMenu("Pomoc");
        help.add(about);
        mb.add(help);
        return mb;
    }

    private Component createToolPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(260, 0));

        JLabel algLabel = new JLabel("Algorytm:");
        String[] algs = {"fruchterman", "tutte"};
        JComboBox<String> algBox = new JComboBox<>(algs);

        JLabel iterLabel = new JLabel("Iteracje:");
        JTextField iterField = new JTextField("200");

        JLabel sizeLabel = new JLabel("Rozmiar obszaru:");
        JTextField sizeField = new JTextField("800.0");

        JButton runBtn = new JButton("Uruchom");
        runBtn.addActionListener(e -> {
            Graph g = getGraph();
            if (g == null) { JOptionPane.showMessageDialog(frame, "Brak grafu", "Błąd", JOptionPane.ERROR_MESSAGE); return; }
            String alg = (String) algBox.getSelectedItem();
            int iters;
            double size;
            try {
                iters = Integer.parseInt(iterField.getText());
                size = Double.parseDouble(sizeField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Nieprawidłowe parametry", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            runAlgorithmInBackground(alg, g, size, iters);
        });

        JButton fitBtn = new JButton("Dopasuj do okna");
        fitBtn.addActionListener(e -> graphPanel.fitToWindow());

        JButton zoomIn = new JButton("Powiększ");
        zoomIn.addActionListener(e -> graphPanel.zoomIn());
        JButton zoomOut = new JButton("Pomniejsz");
        zoomOut.addActionListener(e -> graphPanel.zoomOut());

        JCheckBox labels = new JCheckBox("Pokaż etykiety", true);
        labels.addActionListener(e -> graphPanel.setShowLabels(labels.isSelected()));

        JCheckBox edit = new JCheckBox("Tryb edycji", false);
        edit.addActionListener(e -> graphPanel.setEditMode(edit.isSelected()));

        p.add(Box.createRigidArea(new Dimension(0,8)));
        p.add(algLabel);
        p.add(algBox);
        p.add(iterLabel);
        p.add(iterField);
        p.add(sizeLabel);
        p.add(sizeField);
        p.add(Box.createRigidArea(new Dimension(0,8)));
        p.add(runBtn);
        p.add(Box.createRigidArea(new Dimension(0,8)));
        p.add(fitBtn);
        p.add(zoomIn);
        p.add(zoomOut);
        p.add(Box.createRigidArea(new Dimension(0,8)));
        p.add(labels);
        p.add(edit);

        return p;
    }

    private Graph getGraph() {
        try {
            // access graph via reflection of panel field
            java.lang.reflect.Field f = GraphPanel.class.getDeclaredField("graph");
            f.setAccessible(true);
            return (Graph) f.get(graphPanel);
        } catch (Exception e) {
            return null;
        }
    }

    private void runAlgorithmInBackground(String alg, Graph g, double size, int iters) {
        JDialog dlg = new JDialog(frame, "Running", true);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.setSize(200,100);
        dlg.setLocationRelativeTo(frame);
        JLabel lbl = new JLabel("Trwa wykonywanie...", SwingConstants.CENTER);
        dlg.add(lbl);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private ExitCodes result = ExitCodes.SUCCESS;

            @Override
            protected Void doInBackground() {
                if (alg.equals("tutte")) {
                    result = Tutte.runTutte(g, size, iters);
                } else {
                    Fruchterman f = new Fruchterman(size, iters, g);
                    f.layout(g);
                }
                return null;
            }

            @Override
            protected void done() {
                dlg.dispose();
                if (result != ExitCodes.SUCCESS) {
                    JOptionPane.showMessageDialog(frame, "Algorytm nie powiódł się: " + result, "Błąd", JOptionPane.ERROR_MESSAGE);
                }
                graphPanel.repaint();
            }
        };
        worker.execute();
        dlg.setVisible(true);
    }
}
