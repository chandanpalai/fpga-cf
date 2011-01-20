package edu.byu.cc.plieber.fpgaenet.examples;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt.DockWidgetArea;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QBoxLayout.Direction;

import edu.byu.cc.plieber.fpgaenet.debug.IcapReadback;
import edu.byu.cc.plieber.fpgaenet.debug.LogicalMapping;
import edu.byu.cc.plieber.fpgaenet.debug.llparse.LatchRBEntry;
import edu.byu.cc.plieber.fpgaenet.fcp.FCPException;
import edu.byu.cc.plieber.fpgaenet.fcp.FCPProtocol;
import edu.byu.cc.plieber.fpgaenet.icapif.IcapInterface;
import edu.byu.cc.plieber.fpgaenet.icapif.IcapTools;
import edu.byu.ece.bitstreamTools.bitstream.BitstreamException;
import edu.byu.ece.bitstreamTools.configuration.FPGA;
import edu.byu.ece.bitstreamTools.configuration.Frame;

public class GlobalSigMonitor extends QMainWindow{

	private QMenu fileMenu;
	private QMenu helpMenu;

	private QAction openLL;
	private QAction exitAct;
	private QAction aboutAct;
	private QAction aboutQtJambiAct;
	
	// Widgets
	private QLabel netLabel;
	private QLineEdit netValue;
	private QPushButton buttonGetValue;

	// Model/Views
	private QListView netListView;
	private QTableView netTableView;
	private NetListModel netListModel;
	private NetValueModel netValueModel;
	QSortFilterProxyModel tableProxyModel;
	QSortFilterProxyModel listProxyModel;

	// Connections
	FCPProtocol fcpProtocol;
	IcapInterface icapif;
	IcapTools icapTools;
	IcapReadback icapReadback;
	LatchRBEntry currentEntry;

	public static void main(String[] args) {
		QApplication.initialize(args);

		GlobalSigMonitor signalMonitorGUI = new GlobalSigMonitor(null);
		signalMonitorGUI.show();

		QApplication.exec();
		
		signalMonitorGUI.tearDown();
	}

	public  void tearDown() {
		fcpProtocol.disconnect();
	}

	public GlobalSigMonitor(QWidget parent) {
		super(parent);
		setWindowTitle("Signal Monitor");
		createActions();
		createMenus();
		createWidgets();
		setupConnections();
		connectSignals();
	}

	private void setupConnections() {
		try {
			fcpProtocol = new FCPProtocol();
			fcpProtocol.connect(InetAddress.getByName("192.168.1.222"), 0x3001);
		} catch (IOException e) {
			return;
		}
		icapif = new IcapInterface(fcpProtocol);
		icapTools = new IcapTools(icapif);
		icapReadback = new IcapReadback(icapTools);
		while(!fcpProtocol.isConnected());
		icapTools.synchIcap();
		setAllGlobalSigFrames(icapTools, false, false, true, 2);
	}

	private void createActions() {
		openLL = new QAction(tr("&Open LL File"), this);
		openLL.setShortcut(tr("Ctrl+O"));
		openLL.setStatusTip(tr("Open new LL file"));
		openLL.triggered.connect(this, "openLL()");

		exitAct = new QAction(tr("E&xit"), this);
		exitAct.setShortcut(tr("Ctrl+Q"));
		exitAct.setStatusTip(tr("Exit the application"));
		exitAct.triggered.connect(this, "close()");

		aboutAct = new QAction(tr("&About"), this);
		aboutAct.setStatusTip(tr("Show the application's About box"));
		aboutAct.triggered.connect(this, "about()");

		aboutQtJambiAct = new QAction(tr("About &Qt Jambi"), this);
		aboutQtJambiAct.setStatusTip(tr("Show the Qt Jambi's About box"));
		aboutQtJambiAct.triggered.connect(QApplication.instance(), "aboutQtJambi()");
	}

	private void createMenus() {
		fileMenu = menuBar().addMenu(tr("&File"));
		fileMenu.addAction(openLL);
		fileMenu.addAction(exitAct);

		helpMenu = menuBar().addMenu(tr("&Help"));
		helpMenu.addAction(aboutAct);
		helpMenu.addAction(aboutQtJambiAct);
	}

	private void createWidgets() {
		// Left Dock Net List
		netListView = new QListView(this);
		netListView.setSelectionMode(SelectionMode.ExtendedSelection);
		QDockWidget dockWidget = new QDockWidget(tr("Net List"), this);
		dockWidget.setAllowedAreas(DockWidgetArea.LeftDockWidgetArea);
		dockWidget.setWidget(netListView);
		addDockWidget(DockWidgetArea.LeftDockWidgetArea, dockWidget);
		
		// Main area Frame -------------------------------------------------
		QFrame mainFrame = new QFrame(this);
		setCentralWidget(mainFrame);
		netTableView = new QTableView(mainFrame);
		QVBoxLayout mainLayout = new QVBoxLayout(mainFrame);
		mainLayout.addWidget(netTableView);
		buttonGetValue = new QPushButton(tr("Get Value"));
		buttonGetValue.clicked.connect(this, "getNetValues()");
		mainLayout.addWidget(buttonGetValue);
		mainFrame.setLayout(mainLayout);
	}

	private void connectSignals() {
		netListView.doubleClicked.connect(this, "openStatusWidget(QModelIndex)");
	}

	protected void openLL() {
		String fName = QFileDialog.getOpenFileName(this, tr("Open LL File"), "", new QFileDialog.Filter(
				tr("LL Files (*.ll *.LL)")));
		LogicalMapping llMapping = new LogicalMapping(fName);
		netListModel = new NetListModel(llMapping);
		listProxyModel = new QSortFilterProxyModel(this);
		listProxyModel.setSourceModel(netListModel);
		listProxyModel.sort(0);
		netListView.setModel(listProxyModel);
		netValueModel = new NetValueModel(new ArrayList<LatchRBEntry>(), icapReadback);
		tableProxyModel = new QSortFilterProxyModel(this);
		tableProxyModel.setSourceModel(netValueModel);
		netTableView.setModel(tableProxyModel);
		netTableView.setSelectionMode(SelectionMode.SingleSelection);
		netListView.selectionModel().selectionChanged.connect(this,
				"netSelectionChanged(QItemSelection,QItemSelection)");
	}

	protected void about() {
		QMessageBox.information(this, "Info", "It's your turn now :-)");
	}

	protected void openStatusWidget(QModelIndex index) {
		QWidget statusWidget = new QWidget();
		QLayout layout = new QBoxLayout(Direction.LeftToRight);
		layout.addWidget(new QLabel(tr(listProxyModel.data(index).toString()), statusWidget));
		statusWidget.setLayout(layout);
		statusWidget.setWindowTitle(tr("Net Status"));
		statusWidget.show();
	}

	protected void filterTableView(QModelIndex index) {
		//tableProxyModel.setFilterFixedString(listProxyModel.data(index).toString());
	}

	protected void netSelectionChanged(QItemSelection deselected, QItemSelection selected) {
		ArrayList<LatchRBEntry> entries = new ArrayList<LatchRBEntry>();
		for (QModelIndex index : netListView.selectionModel().selection().indexes()) {
			entries.add(netListModel.getEntry(listProxyModel.mapToSource(index)));
		}
		netValueModel.replaceContents(entries);
		tableProxyModel.sort(0);
	}
	
	protected void getNetValues() {
		try {
			netValueModel.updateValues();
			tableProxyModel.sort(0);
		} catch (FCPException e) {
			QMessageBox.critical(this, "FCP Error", "Error during FCP communication, connection closed");
			this.fcpProtocol.disconnect();
			this.close();
		}
	}
	
	public static void setAllGlobalSigFrames(IcapTools icaptools, boolean GTS, boolean GHighGWE, boolean GRestoreGCaputure, 
            int blockType)
	{

			int num_cols = 66;
			System.out.println("Num_cols: " + num_cols);
			int num_rows = 4;
			// Mask off Global Signals for entire chip
			// All logic
			for (int top = 0; top < 2; top++) {
			for (int row = 0; row < num_rows; row++) {
			for (int col = 0; col < num_cols; col++) {
			int far = 0;
			far |= top << 20;
			far |= row << 15;
			far |= blockType << 21;
			far |= col << 7;
			
			writeGlobalSigFrame(icaptools, GTS, GHighGWE, GRestoreGCaputure, far);
			}
			}
			}
	}
	
	public static void writeGlobalSigFrame(IcapTools icaptools, boolean maskGlobalTriState,  boolean maskGHighGWE, boolean maskGRESTOREGCAP, int far) {

		Frame frame = null;
		try {
			frame = icaptools.readFrame(far);
			frame.getData().setBit(657, maskGlobalTriState ? 1 : 0);
			frame.getData().setBit(658, maskGHighGWE ? 1 : 0);
			frame.getData().setBit(659, maskGRESTOREGCAP ? 1 : 0);
		} catch (FCPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		try {
			icaptools.write(frame);
		} catch (BitstreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
