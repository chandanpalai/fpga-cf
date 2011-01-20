package edu.byu.cc.plieber.fpgaenet.modmod;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

import edu.byu.cc.plieber.fpgaenet.fcp.FCPProtocol;

public class StaticModulesWidget extends QWidget{

    public static void main(String[] args) {
        QApplication.initialize(args);

        StaticModulesWidget testStaticModulesWidget = new StaticModulesWidget(null, null);
        testStaticModulesWidget.show();

        QApplication.exec();
    }
    
    private FCPProtocol fcpprotocol;

    public StaticModulesWidget(QWidget parent, FCPProtocol protocol){
        super(parent);
        fcpprotocol = protocol;
        createWidgets();
        createLayout();
        connectSignalsAndSlots();
    }
    
    private QLabel labelStaticModules = new QLabel("Static Module Control");
    private QLabel labelLEDValue = new QLabel("LED Value:");
    private QLabel labelDIPValue = new QLabel("DIP Value:");
    private QLabel labelClockControl = new QLabel("Clock Control");
    private QLabel labelStepClock = new QLabel("Step Clock:");
    
    private QPushButton btnSetLED = new QPushButton("Set LED");
    private QPushButton btnGetDIP = new QPushButton("Get DIP");
    private QPushButton btnStep = new QPushButton("Step");
    private QPushButton btnSingleStep = new QPushButton("Single Step");
    private QPushButton btnCCReset = new QPushButton("Reset");
    
    private QLineEdit txtLEDValue = new QLineEdit();
    private QLineEdit txtDIPValue = new QLineEdit();
    private QLineEdit txtNumCycles = new QLineEdit();
    
    private void createWidgets() {
    	labelStaticModules.font().setPointSize(labelStaticModules.font().pointSize()+2);
    	labelLEDValue.setAlignment(Qt.AlignmentFlag.AlignRight);
    	labelDIPValue.setAlignment(Qt.AlignmentFlag.AlignRight);
    	labelStepClock.setAlignment(Qt.AlignmentFlag.AlignRight);
    	labelClockControl.font().setPointSize(labelClockControl.font().pointSize()+2);
    }
    
    private void createLayout() {
    	QVBoxLayout mainLayout = new QVBoxLayout(this);
    	QGridLayout leddipLayout = new QGridLayout();
    	QGridLayout clockControlLayout = new QGridLayout();
    	
    	leddipLayout.setColumnMinimumWidth(0, 20);
    	//leddipLayout.setColumnMinimumWidth(3, 20);
    	leddipLayout.setColumnStretch(2, 1);
    	leddipLayout.addWidget(labelLEDValue, 0, 1);
    	leddipLayout.addWidget(txtLEDValue, 0, 2);
    	leddipLayout.addWidget(btnSetLED, 0, 3);
    	leddipLayout.addWidget(labelDIPValue, 1, 1);
    	leddipLayout.addWidget(btnGetDIP, 1, 3);
    	leddipLayout.addWidget(txtDIPValue, 1, 2);
    	
    	clockControlLayout.setColumnMinimumWidth(0, 20);
    	//clockControlLayout.setColumnMinimumWidth(3, 20);
    	clockControlLayout.setColumnStretch(2, 1);
    	clockControlLayout.addWidget(labelStepClock, 0, 1);
    	clockControlLayout.addWidget(txtNumCycles, 0, 2);
    	clockControlLayout.addWidget(btnStep, 0, 3);
    	clockControlLayout.addWidget(btnSingleStep, 1, 1);
    	clockControlLayout.addWidget(btnCCReset, 1, 3);
    	
    	mainLayout.addWidget(labelStaticModules);
    	mainLayout.addLayout(leddipLayout);
    	mainLayout.addWidget(labelClockControl);
    	mainLayout.addLayout(clockControlLayout);
    	mainLayout.addStretch();
    }
    
    private void connectSignalsAndSlots() {
    	
    }
}
