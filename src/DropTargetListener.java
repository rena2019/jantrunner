/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JComponent;
import javax.swing.JList;

class DropTargetListener extends DropTargetAdapter {

	private DropTarget dropTarget;
	private JComponent component;

	public DropTargetListener(JComponent component) {
		this.component = component;

		dropTarget = new DropTarget(component, DnDConstants.ACTION_COPY, this,
				true, null);
	}

	public void drop(DropTargetDropEvent event) {
		try {
			if (event
					.isDataFlavorSupported(TransferableAntTarget.antTargetFlavor)) {
				Transferable tr = event.getTransferable();
				AntTarget[] antTargets = (AntTarget[]) tr
						.getTransferData(TransferableAntTarget.antTargetFlavor);

				event.acceptDrop(DnDConstants.ACTION_COPY);
				if (component instanceof ButtonPanel) {
					for(AntTarget target: antTargets)
						((ButtonPanel)component).addAntTarget(target, true);
					event.dropComplete(true);
					return;
				}else if (component instanceof BatchList) {
					for(AntTarget target: antTargets)
						((BatchList)component).addAntTarget(target);
					event.dropComplete(true);
					return;
				}
				else {
					//else TODO add !!!
					System.out.println("drop event");
				}
			}
			event.rejectDrop();
		} catch (Exception e) {
			e.printStackTrace();
			event.rejectDrop();
		}
	}
}// DropTargetListener