package net.sourceforge.plantuml.eclipse.views.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.plantuml.eclipse.utils.Diagram;
import net.sourceforge.plantuml.eclipse.utils.WorkbenchUtil;

/**
 * manage the print of the diagram image.
 * 
 * @author durif_c
 * 
 */
public class PrintRightClickListener extends RightClickListener {
    /**
     * 
     */
    private final Composite composite;

    /**
     * 
     * @param diagram Diagram
     * @param container Composite
     */
    public PrintRightClickListener(Display display, Diagram diagram, Composite container) {
        super(display, diagram);
        this.composite = container;
    }

    @Override
    public void run() {
        final PrintDialog pDialog = new PrintDialog(this.composite.getShell(), SWT.APPLICATION_MODAL);
        pDialog.setText("UML Printing.");

        pDialog.setScope(PrinterData.ALL_PAGES);
        final PrinterData pData = pDialog.open();
        try {
            Printer printer = null;
            if (pData != null) {
                printer = new Printer(pData);
            }
            
            if (printer != null && printer.startJob("PlantUml Image")
                    && printer.startPage()) {

                final int coef = 5;
                final Rectangle trim = printer.computeTrim(0, 0, 0, 0);
                final Point dpi = printer.getDPI();
                final int horizontalMargin = dpi.x / 4 + trim.x;
                final int verticalMargin = dpi.y / 4 + trim.y;
                // We calculate the appropriate size of the image to be
                // print (because I have some problem with memory)
                int displayWidth = diagram.getImageData().width
                        * coef;
                int displayHeigth = diagram.getImageData().height
                        * coef;

                final Rectangle pageSize = printer.getBounds();
                int widthWithoutMargin = pageSize.width - 2 * horizontalMargin; 
                int heigthWithoutMargin = pageSize.height - 2 * verticalMargin;

                // Adjust image if it's bigger than page size.
                if (displayWidth > widthWithoutMargin) {
                    displayWidth = widthWithoutMargin;
                    displayHeigth = (displayHeigth * displayWidth) / widthWithoutMargin;

                    if (displayHeigth > heigthWithoutMargin) {
                        displayHeigth = heigthWithoutMargin;
                        displayWidth = (displayWidth * heigthWithoutMargin) / displayHeigth;
                    }
                } else if (displayHeigth > heigthWithoutMargin) {
                    displayHeigth = heigthWithoutMargin;
                    displayWidth = (displayWidth * heigthWithoutMargin) / displayHeigth;

                    if (displayWidth > widthWithoutMargin) {
                        displayWidth = widthWithoutMargin;
                        displayHeigth = (displayHeigth * widthWithoutMargin) / displayWidth;
                    }
                }

                printImage(printer, horizontalMargin, verticalMargin,
                        displayWidth, displayHeigth);

            }
            if (printer != null) {
				printer.endJob();
				printer.dispose();
			}

        } catch (Throwable e) {
            WorkbenchUtil.errorBox("OutOfMemoryError",
                    "Image to print is too big to be printed in one page.", e);
        }

    }

    /**
     * @param printer Printer
     * @param horizontalMargin int
     * @param verticalMargin int
     * @param displayWidth int
     * @param displayHeigth int
     * 
     * @throws Throwable
     */
    private void printImage(Printer printer, int horizontalMargin,
            int verticalMargin, int displayWidth, int displayHeigth)
            throws Throwable {

        final GC gc = new GC(printer);
        final ImageData iData = diagram.getImageData();
        final Image printerImage = new Image(printer, iData);

        // use of the gc method to reduce outOMemoryError
		gc.drawImage(printerImage, 0, 0, iData.width, iData.height,
				horizontalMargin, verticalMargin, displayWidth, displayHeigth);
        printerImage.dispose();
        gc.dispose();
        printer.endPage();
    }

}
