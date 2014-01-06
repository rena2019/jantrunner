/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

class TransferableAntTarget implements Transferable {

	protected static DataFlavor antTargetFlavor = new DataFlavor(AntTarget.class,
			"AntTarget Object");

	protected static DataFlavor[] supportedFlavors = { antTargetFlavor,
			/*DataFlavor.stringFlavor,*/ };

	AntTarget[] antTargets;

	public TransferableAntTarget(AntTarget antTarget) {
		this.antTargets = new AntTarget[] { antTarget };
	}
	
	public TransferableAntTarget(AntTarget[] antTargets) {
		this.antTargets = antTargets;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(antTargetFlavor))
			return true;
		return false;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException {
		if (flavor.equals(antTargetFlavor))
			return antTargets;
		else
			throw new UnsupportedFlavorException(flavor);
	}
}//TransferableAntTarget