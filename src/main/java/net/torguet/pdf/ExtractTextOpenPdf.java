package net.torguet.pdf;

import com.lowagie.text.pdf.PdfDocument;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

public class ExtractTextOpenPdf {
    public static void main(String ... args) throws Exception {
        if (args.length != 1)
        {
            usage();
        }

        PdfReader reader = new PdfReader(args[0]);
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
        System.out.println("Page 1 text: " + pdfTextExtractor.getTextFromPage(1));
        System.out.println("Page 2 text: " + pdfTextExtractor.getTextFromPage(2));
        System.out.println("Page 3 table cell text: " + pdfTextExtractor.getTextFromPage(3));
    }

    private static void usage()
    {
        System.err.println("Usage: java " + ExtractTextOpenPdf.class.getName() + " <input-pdf>");
        System.exit(-1);
    }
}
