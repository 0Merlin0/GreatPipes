package pipes.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import pipes.editing.TuneEditController;
import pipes.editing.TuneEditListener;
import pipes.editing.actions.EditAction;
import pipes.editing.io.TuneSerializer;
import pipes.model.NewTuneParameters;
import pipes.model.TimeSignature;
import pipes.view.tools.Toolbar;

public class AppView extends JFrame implements TuneEditListener {
	private static final long serialVersionUID = 1L;
	private static final String WINDOW_CAPTION = "Great Pipes";

	public static void main(String... arg) {
		new AppView();
	}
	
	public void tuneEdited(EditAction ation) {
		updateTitle();
	}
	
	public AppView() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		tuneView = new TuneView();
		JScrollPane tuneScroller = new JScrollPane(tuneView);

		controller = new TuneEditController(tuneView);
		controller.addEditListener(this);
		controller.newTune(NewTuneParameters.DEFAULT);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tuneScroller, BorderLayout.CENTER);
		
		Toolbar toolbar = new Toolbar(tuneView, controller);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		buildMenu();

		updateTitle();
		setSize(1200, 900);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				tuneView.updateMusic();
			}
		});
	}

	private void buildMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);

		JMenuItem newItem = new JMenuItem("New");
		fileMenu.add(newItem);
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newTune();
			}
		});

		JMenuItem openItem = new JMenuItem("Open...");
		fileMenu.add(openItem);
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});

		JMenuItem saveItem = new JMenuItem("Save");
		fileMenu.add(saveItem);
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		
		JMenuItem saveAsItem = new JMenuItem("Save As...");
		fileMenu.add(saveAsItem);
		saveAsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
	}
	
	private void save() {
		if (controller.getIsDirty()) {
			if (controller.getEditingFile() == null) {
				saveAs();
			} else {
				try {
					controller.saveTune();
					updateTitle();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, "An error occured while saving your tune.");
				}
			}
		}
	}
	
	private void saveAs() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Great Pipes tune file", TuneSerializer.FILE_EXTENSION));
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
				
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				controller.saveTune(chooser.getSelectedFile());
				updateTitle();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "An error occured while saving your tune.");
				e.printStackTrace();
			}
		}
	}

	private void open() {
		// save changes
		if (controller.getIsDirty()) {
			int result = JOptionPane.showConfirmDialog(this, "Save changes to your tune?", "Save changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
			if (result == JOptionPane.YES_OPTION)
				save();
		}
		
		// open
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Great Pipes tune file", TuneSerializer.FILE_EXTENSION));
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				controller.loadTune(chooser.getSelectedFile());
				updateTitle();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "An error occured while loading the file.");
				e.printStackTrace();
			}
		}
	}
	
	private void newTune() {
		// save changes
		if (controller.getIsDirty()) {
			int result = JOptionPane.showConfirmDialog(this, "Save changes to your tune?", "Save changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
			if (result == JOptionPane.YES_OPTION)
				save();
		}
		
		// New tune
		JTextField numLines = new JTextField("4", 2);
		JTextField measuresPerLine = new JTextField("4", 4);
		JComboBox<TimeSignature> timeSigs = new JComboBox<TimeSignature>(TimeSignature.STANDARD_TIMES);
		
		JComponent[] message = new JComponent[] {
				new JLabel("Lines: "),
				numLines,
				new JLabel("Measures per line: "),
				measuresPerLine,
				new JLabel("Time signature"),
				timeSigs};
		Object[] inputs = new Object[] {"Ok", "Cancel"};
		
		boolean valid = false;
		while (!valid) {
			if (JOptionPane.showOptionDialog(this, message, "New tune", JOptionPane.PLAIN_MESSAGE, JOptionPane.CLOSED_OPTION, null, inputs, inputs[0])
					== JOptionPane.OK_OPTION) {
				try {
					int numLinesChoice = Integer.parseInt(numLines.getText());
					int measuresPerChoice = Integer.parseInt(measuresPerLine.getText());
					TimeSignature timeSigChoice = (TimeSignature)timeSigs.getSelectedItem();
					controller.newTune(new NewTuneParameters(timeSigChoice, numLinesChoice, measuresPerChoice));
					updateTitle();
					valid = true;
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Please enter valid inputs.");
				}
			} else {
				break;
			}
		}
	}
	
	private void updateTitle() {
		if (controller.getEditingFile() != null) {
			setTitle(WINDOW_CAPTION + " - " + controller.getEditingFile().getName() + (controller.getIsDirty() ? "*" : ""));
		} else {
			setTitle(WINDOW_CAPTION);
		}
	}

	private TuneEditController controller;
	private TuneView tuneView;
}
