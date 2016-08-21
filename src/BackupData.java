
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.mysql.jdbc.PreparedStatement;

public class BackupData {

	private JFrame frmBackupData;

	/**
	 * Launch the application.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection con;
	public static JComboBox sourceTable, destTable;
	public static final int MSG_LOG = 1, MSG_SUCC = 2, MSG_ERR = 3, MSG_TEXT = 0;

	public static void main(String[] args) throws ClassNotFoundException, SQLException {

		Class.forName("com.mysql.jdbc.Driver");

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BackupData window = new BackupData();
					window.frmBackupData.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws SQLException
	 */
	public static final Map<String, String> settings = new HashMap<String, String>();
	public static final Map<String, Database> databases = new HashMap<String, Database>();

	public BackupData() throws SQLException {
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/brettlee", "root", "");
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select * from settings");

		while (rs.next())
			settings.put(rs.getString(1), rs.getString(2));

		rs = stmt.executeQuery("select * from db");

		while (rs.next()) {
			databases.put(rs.getString(2), new Database(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
					rs.getString(5), rs.getString(6)));
		}

		initialize();
	}

	boolean started = false;

	final void startStop() {
		started = !started;
	}

	private void initialize() throws NullPointerException {
		frmBackupData = new JFrame();
		frmBackupData.setTitle("Backup Data");
		frmBackupData.setBounds(100, 100, 512, 364);
		frmBackupData.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmBackupData.getContentPane().setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(113dlu;default):grow"),
						FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(84dlu;default):grow"),
						FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("max(10dlu;default)"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblSrc = new JLabel("Source: ");
		lblSrc.setFont(new Font("Tahoma", Font.BOLD, 11));
		frmBackupData.getContentPane().add(lblSrc, "2, 2");

		JLabel lblDest = new JLabel("Destination: ");
		lblDest.setFont(new Font("Tahoma", Font.BOLD, 11));
		frmBackupData.getContentPane().add(lblDest, "4, 2");

		sourceTable = new JComboBox();
		frmBackupData.getContentPane().add(sourceTable, "2, 4, fill, default");

		destTable = new JComboBox();
		frmBackupData.getContentPane().add(destTable, "4, 4, fill, default");

		updateDropDown();
		JLabel statusLabel = new JLabel("Welcome");
		frmBackupData.getContentPane().add(statusLabel, "2, 8");

		JProgressBar progressBar = new JProgressBar();
		frmBackupData.getContentPane().add(progressBar, "2, 10, 5, 1");
		DefaultListModel<String> model = new DefaultListModel<>();

		JSeparator separator = new JSeparator();
		frmBackupData.getContentPane().add(separator, "2, 6, 5, 1");

		JScrollPane scrollPane = new JScrollPane();
		frmBackupData.getContentPane().add(scrollPane, "2, 12, 5, 1, fill, fill");
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (started)

					e.getAdjustable().setValue(e.getAdjustable().getMaximum());
			}
		});

		JTextPane textPane = new JTextPane();
		scrollPane.setViewportView(textPane);

		StyledDocument doc = textPane.getStyledDocument();

		JButton btnNewButton = new JButton("Copy");
		JLabel lblNewLabel = new JLabel("");
		frmBackupData.getContentPane().add(lblNewLabel, "2, 14");

		Runnable run = new Runnable() {

			@Override
			public void run() {
				try {

					Database sourceDB = databases.get(sourceTable.getSelectedItem());
					Database destDB = databases.get(destTable.getSelectedItem());
DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.S");

					writeToLog(doc, "Started at " + df.format(new Date()), MSG_LOG);

					Connection sourceCon = DriverManager.getConnection(sourceDB.getIp(), sourceDB.getUserName(),
							sourceDB.getPassword());
					Statement sourceStmt = sourceCon.createStatement();

					writeToLog(doc, "\nConnected to " + sourceDB.getDbName(), MSG_SUCC);

					Connection destCon = DriverManager.getConnection(destDB.getIp(), destDB.getUserName(),
							destDB.getPassword());
					Statement destStmt = destCon.createStatement();

					writeToLog(doc, "\nConnected to " + destDB.getDbName(), MSG_SUCC);

					writeToLog(doc, "\nFetching tables from " + sourceDB.getDbName(), MSG_LOG);

					ResultSet rs = sourceStmt.executeQuery(settings.get("get_tbl_query"));
					rs.last();
					int totTbl = rs.getRow();
					rs.beforeFirst();
					writeToLog(doc, "\n" + totTbl + " tables present in " + sourceDB.getDbName(), MSG_SUCC);
					statusLabel.setText("Fetching tables from " + sourceDB.getDbName());

					final List<String> tables = new ArrayList<String>();
					int k = 0;
					if ("1".equals(settings.get("get_dependent"))) {
						while (rs.next()) {
							writeToLog(doc, "\n" + "Fetching dependent tables for "
									+ rs.getString(settings.get("tbl_name_fld")), MSG_LOG);
							tables.addAll(getDependentTables(rs.getString(settings.get("tbl_name_fld"))));
							progressBar.setValue(100 * (++k) / totTbl);
						}
					} else {
						tables.add((rs.getString(settings.get("tbl_name_fld"))));

					}

					for (String table : tables) {
						writeToLog(doc, "\nTable is " + table, MSG_LOG);
						writeToLog(doc, "\nTruncating the table " + table + " from " + destDB.getDbName(), MSG_LOG);

						destStmt.executeUpdate("truncate table " + table);

						writeToLog(doc, "\nThe table " + table + " truncated from " + destDB.getDbName(), MSG_SUCC);

						ResultSet rs2 = sourceStmt.executeQuery("select * from " + table);

						rs2.last();
						final int total = rs2.getRow();
						rs2.beforeFirst();

						writeToLog(doc, "\nRetrieving records from " + table, MSG_LOG);
						writeToLog(doc, "\n" + total + " records present in " + table + " in " + sourceDB.getDbName(),
								MSG_TEXT);

						java.sql.ResultSetMetaData rsmd = rs2.getMetaData();
						final StringBuilder query = new StringBuilder("Insert into " + table + " values (");
						for (int i = 1; i <= rsmd.getColumnCount(); i++) {
							query.append("?");
							if (i != rsmd.getColumnCount())
								query.append(",");
						}
						query.append(")");

						writeToLog(doc, "\nInserting records to destination table " , MSG_LOG);
						
						writeToLog(doc, "\nInsertion to " +table +" started at " + df.format(new Date()) , MSG_LOG);
						int j = 0, n = 0, offset = 0;

						while (offset <= total) {
							
							ResultSet rs3 = sourceStmt.executeQuery(
									"select * from " + table + " limit " + offset + ", " + settings.get("max_records"));
							offset += Integer.parseInt(settings.get("max_records"));
							while (rs3.next()) {
								++n;
								try {
									final PreparedStatement p = (PreparedStatement) destCon
											.prepareStatement(query.toString());
									for (int i = 1; i <= rsmd.getColumnCount(); i++) {
										p.setObject(i, rs3.getObject(i));
									}
									p.execute();
									j++;
									progressBar.setValue(100 * (n) / total);
									statusLabel.setText(
											"Inserting records to " + table +  ".....".substring(0,1+((100 * (n) / total) % 5))+ "(" + 100 * (n) / total + "%)");
								} catch (SQLException e) {
									writeToLog(doc, "\nError inserting the following record to table " + table + "\n(",
											MSG_ERR);
									for (int i = 1; i <= rsmd.getColumnCount(); i++) {
										writeToLog(doc, "" + rs3.getObject(i), MSG_TEXT);
										if (i != rsmd.getColumnCount())
											writeToLog(doc, ",", MSG_TEXT);
									}
									writeToLog(doc, ")", MSG_TEXT);
								}
							}
						}
						writeToLog(doc, "\n" + j + " records inserted to " + table, MSG_LOG);
						
						writeToLog(doc, "\nInsertion to " +table +" ended at " + df.format(new Date()) , MSG_LOG);
						if (j == total)
							writeToLog(doc, "\nThe table " + table + " has been successfully copied", MSG_SUCC);
						else
							writeToLog(doc, "\nThe table " + table + " could not be successfully copied", MSG_ERR);
					}
					statusLabel.setText("Finished");
					JOptionPane.showMessageDialog(null, "Backup Completed", "Backup", 1);
					writeToLog(doc, "\nEnded at " + df.format(new Date()), MSG_LOG);
					scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
					btnNewButton.setText("Done");
					startStop();

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

			private List<String> getDependentTables(String tbl) {
				final List<String> depTables = new ArrayList<String>();
				depTables.add(tbl);
				return depTables;
			}

			private void writeToLog(StyledDocument doc, String log, int style) {
				SimpleAttributeSet styles[] = new SimpleAttributeSet[4];
				for (int i = 0; i < 4; i++)
					styles[i] = new SimpleAttributeSet();
				StyleConstants.setForeground(styles[0], Color.BLACK);
				StyleConstants.setForeground(styles[1], Color.BLUE);
				StyleConstants.setForeground(styles[2], new Color(32, 150, 64));
				StyleConstants.setForeground(styles[3], Color.RED);

				try {
					doc.insertString(doc.getLength(), log, styles[style]);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

		}

		;
		final Thread t = new Thread(run);
		;
		btnNewButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (sourceTable.getSelectedItem().toString().equals(destTable.getSelectedItem().toString())) {
					JOptionPane.showMessageDialog(null, "You have chosen the same source and destination");
					return;
				}

				btnNewButton.setText("Running");
				btnNewButton.setEnabled(false);

				startStop();
				t.start();

			}
		});
		frmBackupData.getContentPane().add(btnNewButton, "6, 4");

		JMenuBar menuBar = new JMenuBar();
		frmBackupData.setJMenuBar(menuBar);

		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				System.exit(0);
			}
		});
		ManageDatabase mngDB = new ManageDatabase();
		JMenuItem mntmManageSchemas = new JMenuItem("Manage Schemas");
		mntmManageSchemas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mngDB.setVisible(true);

			}
		});
		mnSettings.add(mntmManageSchemas);
		mnSettings.add(mntmExit);
	}

	public static void updateDropDown() {
		sourceTable.removeAllItems();
		destTable.removeAllItems();
		for (Entry<String, Database> d : databases.entrySet()) {
			sourceTable.addItem(d.getKey());
			destTable.addItem(d.getKey());
		}
		destTable.setSelectedIndex(1);
	}

}
