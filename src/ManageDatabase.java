import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class ManageDatabase extends JFrame {

	private JPanel contentPane;
	private JTextField dbName;
	private JTextField iPPort;
	private JTextField userName;
	private JPasswordField password;
	private JTextField schema;
	public static JComboBox database;
	private static int curID = 0;

	/**
	 * Create the frame.
	 */

	public ManageDatabase() {
		setResizable(false);
		setType(Type.UTILITY);
		setTitle("Manage Databases");
		setBounds(100, 100, 428, 237);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
						FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(78dlu;default):grow"),
						FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblDatabase = new JLabel("Database:");
		contentPane.add(lblDatabase, "2, 2, right, default");

		database = new JComboBox();

		contentPane.add(database, "4, 2, 5, 1, fill, default");

		updateDD();

		JLabel lblName = new JLabel("Name:");
		contentPane.add(lblName, "2, 4, right, default");

		dbName = new JTextField();
		dbName.setEditable(false);
		contentPane.add(dbName, "4, 4, 5, 1, fill, default");
		dbName.setColumns(10);

		JLabel lblUser = new JLabel("IP/Port:");
		contentPane.add(lblUser, "2, 6, right, default");

		iPPort = new JTextField();
		contentPane.add(iPPort, "4, 6, 5, 1, fill, default");
		iPPort.setColumns(10);

		JLabel lblSchema = new JLabel("Schema:");
		contentPane.add(lblSchema, "2, 8, right, default");

		schema = new JTextField();
		contentPane.add(schema, "4, 8, 5, 1, fill, default");
		schema.setColumns(10);

		JLabel label = new JLabel("User:");
		contentPane.add(label, "2, 10, right, default");

		userName = new JTextField();
		contentPane.add(userName, "4, 10, 5, 1, fill, default");
		userName.setColumns(10);

		JLabel lblPassword = new JLabel("Password:");
		contentPane.add(lblPassword, "2, 12, right, default");

		password = new JPasswordField();
		contentPane.add(password, "4, 12, 5, 1, fill, default");

		JButton btnUpdate = new JButton("Update");
		contentPane.add(btnUpdate, "4, 14");

		JButton btnDelete = new JButton("Delete");
		contentPane.add(btnDelete, "6, 14");

		JButton btnAddNew = new JButton("Add New");
		contentPane.add(btnAddNew, "8, 14");
		database.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (database.getSelectedIndex() == 0) {

					dbName.setEditable(true);
					dbName.setText("");
					iPPort.setText("");
					schema.setText("");
					userName.setText("");
					password.setText("");
					btnDelete.setEnabled(false);
					btnUpdate.setEnabled(true);
					btnAddNew.setText("Clear");
					btnUpdate.setText("Submit");
					curID = 0;

					return;
				}
				Database d = BackupData.databases.get(database.getSelectedItem());
				dbName.setText(d.getDbName());
				iPPort.setText(d.getIp());
				schema.setText(d.getSchema());
				userName.setText(d.getUserName());
				password.setText(d.getPassword());
				curID = d.getId();
				dbName.setEditable(false);
				btnUpdate.setEnabled(true);
				btnDelete.setEnabled(true);
				btnAddNew.setText("Add New");
				btnUpdate.setText("Update");
			}
		});
		btnAddNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dbName.setEditable(true);
				dbName.setText("");
				iPPort.setText("");
				schema.setText("");
				userName.setText("");
				password.setText("");
				btnDelete.setEnabled(false);
				btnUpdate.setEnabled(true);
				btnAddNew.setText("Clear");
				btnUpdate.setText("Submit");
				curID = 0;
				database.setSelectedIndex(0);
			}
		});

		btnUpdate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (BackupData.databases.containsKey(dbName.getText()) && curID == 0) {
					JOptionPane.showMessageDialog(null, "This name already exists", "Name Already Exists",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				insertUpdateDB(dbName.getText(), iPPort.getText(), schema.getText(), userName.getText(),
						password.getText());

				dbName.setEditable(false);
				btnDelete.setEnabled(true);
				btnAddNew.setText("Add New");
				btnUpdate.setText("Update");

				database.setSelectedItem(dbName.getText());

				JOptionPane.showMessageDialog(null, "Schema has been updated successfully", "Updated Successfully",
						JOptionPane.INFORMATION_MESSAGE);

			}
		});

		btnDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteDB(dbName.getText());
				database.removeItemAt(database.getSelectedIndex());
				database.setSelectedItem(0);
			}
		});

		dbName.setEditable(true);
		dbName.setText("");
		iPPort.setText("");
		schema.setText("");
		userName.setText("");
		password.setText("");
		btnDelete.setEnabled(false);
		btnUpdate.setEnabled(true);
		btnAddNew.setText("Clear");
		btnUpdate.setText("Submit");
		curID = 0;
	}

	public void updateDD() {
		database.removeAllItems();
		database.addItem("Add New");
		for (Entry<String, Database> d : BackupData.databases.entrySet()) {
			database.addItem(d.getKey());
		}
	}

	void insertUpdateDB(String databaseName, String ip, String schema, String userName, String password) {

		BackupData.databases.put(databaseName, new Database(curID, databaseName, ip, schema, userName, password));
		if (curID == 0) {
			BackupData.updateDropDown();
			database.addItem(databaseName);
		}
		try {

			PreparedStatement p = BackupData.con.prepareStatement("select 1 from dual");
			if (curID == 0) {
				p = BackupData.con
						.prepareStatement("insert into db(dbname,ip,schm,username,password) values (?,?,?,?,?)");

				p.setString(1, databaseName);
				p.setString(2, ip);
				p.setString(3, schema);
				p.setString(4, userName);
				p.setString(5, password);
			} else {
				p = BackupData.con.prepareStatement(
						"update db set dbname = ?,ip = ?,schm = ?,username=? ,password=? where id = ?");

				p.setString(1, databaseName);
				p.setString(2, ip);
				p.setString(3, schema);
				p.setString(4, userName);
				p.setString(5, password);
				p.setInt(6, curID);
			}

			p.execute();
			if (curID == 0) {
				java.sql.Statement x = BackupData.con.createStatement();
				ResultSet r = x.executeQuery("select max(id) from db");
				r.next();
				curID = r.getInt(1);
				BackupData.databases.get(databaseName).setId(curID);

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void deleteDB(String databaseName) {
		BackupData.databases.remove(databaseName);
		BackupData.updateDropDown();
		try {
			java.sql.Statement x = BackupData.con.createStatement();

			x.executeUpdate("delete from db where id = " + curID);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
