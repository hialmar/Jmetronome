package net.torguet.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.File;

/**
 * Rotates all pages of a document.
 * Cf. <a href="https://github.com/mkl-public/testarea-pdfbox2/blob/master/src/test/java/mkl/testarea/pdfbox2/content/RotatePageContent.java#L160">this example</a>
 */
public class RotatePdf {

    public static void main(String ... args) throws Exception {
        if( args.length != 1 )
        {
            usage();
        }
        else
        {
            PDDocument document = Loader.loadPDF(new File(args[0]));
            for(PDPage page : document.getDocumentCatalog().getPages()) {
                PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false);
                Matrix matrix = Matrix.getRotateInstance(Math.toRadians(-90), 0, 0);
                cs.transform(matrix);
                cs.close();

                PDRectangle cropBox = page.getCropBox();
                Rectangle rectangle = cropBox.transform(matrix).getBounds();
                PDRectangle newBox = new PDRectangle((float) rectangle.getX(), (float) rectangle.getY(), (float) rectangle.getWidth(), (float) rectangle.getHeight());
                page.setCropBox(newBox);
                page.setMediaBox(newBox);
            }

            document.save(new File("Rotated_" + args[0]));
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println( "usage: " + RotatePdf.class.getName() + " <input-file>" );
    }
}
