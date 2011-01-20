package edu.byu.cc.plieber.fpgaenet.examples;

import java.io.IOException;
import java.net.InetAddress;

import edu.byu.cc.plieber.fpgaenet.fcp.FCPException;
import edu.byu.cc.plieber.fpgaenet.fcp.FCPProtocol;
import edu.byu.cc.plieber.fpgaenet.icapif.IcapInterface;
import edu.byu.cc.plieber.fpgaenet.icapif.IcapTools;
import edu.byu.ece.bitstreamTools.bitstream.BitstreamException;
import edu.byu.ece.bitstreamTools.bitstream.PacketList;
import edu.byu.ece.bitstreamTools.bitstream.PacketUtils;
import edu.byu.ece.bitstreamTools.bitstream.RegisterType;
import edu.byu.ece.bitstreamTools.configuration.FPGA;
import edu.byu.ece.bitstreamTools.configuration.Frame;
import edu.byu.ece.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.bitstreamTools.configurationSpecification.DeviceLookup;

public class GlobalSigs {
    
	
	public static void readRegisters(){
	  	
	}
	
	public static void setAllGlobalSigFrames(IcapTools icaptools, boolean GTS, boolean GHighGWE, boolean GRestoreGCaputure, 
			                                    int blockType,FPGA fpga){
		
		int num_cols = fpga.getDeviceSpecification().getOverallColumnLayout().size();
		System.out.println("Num_cols: " + num_cols);
		int num_rows = fpga.getDeviceSpecification().getTopNumberOfRows();
		// Mask off Global Signals for entire chip
		// All logic
		for (int top = 0; top < 2; top++) {
			for (int row = 0; row < num_rows; row++) {
				for (int col = 0; col < num_cols; col++) {
					int far = 0;
					far |= top << fpga.getDeviceSpecification().getTopBottomBitPos();
					far |= row << fpga.getDeviceSpecification().getRowBitPos();
					far |= blockType << fpga.getDeviceSpecification().getBlockTypeBitPos();
					far |= col << fpga.getDeviceSpecification().getColumnBitPos();

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

	/**
	 * @param args
	 * @throws FCPException
	 */
	public static void main(String[] args) throws FCPException {

		FCPProtocol protocol = null;
		System.out.println("Initializing Peter's Stuff.");
		try {
			protocol = new FCPProtocol();
			protocol.connect(InetAddress.getByName("192.168.1.222"), 0x3001);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Creating More of Peter's stuff.");
		IcapInterface icap = new IcapInterface(protocol);
		FPGA fpga = new FPGA(DeviceLookup.lookupPartV4V5V6("XC5VLX110T"));
		IcapTools icapTools = new IcapTools(icap);
		while (!protocol.isConnected())
			System.out.println("In the Infinite Loop.");
		System.out.println("Syncing ICAP.");
		icapTools.synchIcap();
       
		int Stat =0;
		//try {
		//	Stat = icapTools.readRegister(RegisterType.STAT);
		//} catch (BitstreamException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		System.out.println("Status Register " + Integer.toHexString(Stat));
		
		System.out.println("Masking all Global Signals.");
		setAllGlobalSigFrames(icapTools, true, true, true, 2,fpga);
		//setAllGlobalSigFrames(icapTools, true, true, true, 4,fpga);
        
		int pr_far =0; 
		
		pr_far =  1 << fpga.getDeviceSpecification().getTopBottomBitPos() | 
		          2 << fpga.getDeviceSpecification().getBlockTypeBitPos() |
		          2 << fpga.getDeviceSpecification().getRowBitPos() |
		          44<< fpga.getDeviceSpecification().getColumnBitPos();
		
		writeGlobalSigFrame(icapTools, false, false, false, pr_far);
	    
		try {
			//System.out.println("Please Send the Shutdown Command and press any key.");	
			//System.in.read();
			icapTools.write(RegisterType.CMD, 8); //AGHIGH Command
			//icapTools.write(RegisterType.CMD, 10); //GRESTORE Command
			//icapTools.write(RegisterType.CMD, 12); //GCAPTURE
			//Frame frame = new Frame(41, 2 << fpga.getDeviceSpecification().getBlockTypeBitPos());
			//icapTools.readFrame(frame);
			//System.out.println("Design Should still be running.");
			//System.out.println(frame);
			System.in.read();
			
			//Frame frame = new Frame(41, pr_far);
			//icapTools.readFrame(frame);
			//System.out.println(frame);
			icapTools.write(RegisterType.CMD, 3); //DGHIGH Command
			//icapTools.write(RegisterType.CMD, 3); //DGHIGH Command
			//icapTools.write(RegisterType.CMD, 3); //DGHIGH Command
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BitstreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	//	try {
			//icapTools.write(RegisterType.CMD, 11);
			//System.out.println("Design Should not be running.");
			System.out.println("Disconnecting...");
			protocol.disconnect();
            System.out.println("Done.");
		//}// catch (BitstreamException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
	}

}
