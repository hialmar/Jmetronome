package net.torguet.pdf;

/*
 * Copyright 2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPatternContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.util.Matrix;

/**
 * This is an example of how to create a page that uses patterns to paint areas.
 *
 * @author Tilman Hausherr
 */
public final class CreatePatternsPDF
{
    private CreatePatternsPDF()
    {
    }

    public static void main(String[] args) throws IOException
    {
        if( args.length != 1 )
        {
            usage();
        }
        else
        {
            System.out.println("Loading " + args[0]);
            try (PDDocument sourceDoc = Loader.loadPDF(new File(args[0]))) {

                // create a new PDF and add a blank page
                try (PDDocument doc = new PDDocument()) {
                    PDPage page = new PDPage(PDRectangle.A4);
                    page.setRotation(90);
                    doc.addPage(page);
                    PDRectangle pageSize = page.getMediaBox();
                    float pageWidth = pageSize.getWidth();

                    try (PDPageContentStream pcs = new PDPageContentStream(doc, page)) {

                        pcs.transform(new Matrix(0, 1, -1, 0, pageWidth, 0));

                        // Create a Form XObject from the source document using LayerUtility
                        LayerUtility layerUtility = new LayerUtility(doc);
                        PDFormXObject form = layerUtility.importPageAsForm(sourceDoc, 2);

                        // draw the full form
                        pcs.drawForm(form);

                        // Colored pattern, i.e. the pattern content stream will set its own color(s)
                        PDColorSpace patternCS1 = new PDPattern(null, PDDeviceRGB.INSTANCE);

                        // Table 75 spec
                        PDTilingPattern tilingPattern1 = new PDTilingPattern();
                        tilingPattern1.setBBox(new PDRectangle(0, 0, 10, 10));
                        tilingPattern1.setPaintType(PDTilingPattern.PAINT_COLORED);
                        tilingPattern1.setTilingType(PDTilingPattern.TILING_CONSTANT_SPACING);
                        tilingPattern1.setXStep(10);
                        tilingPattern1.setYStep(10);

                        COSName patternName1 = page.getResources().add(tilingPattern1);
                        try (PDPatternContentStream cs1 = new PDPatternContentStream(tilingPattern1)) {
                            // Set color, draw diagonal line + 2 more diagonals so that corners look good
                            cs1.setStrokingColor(Color.red);
                            cs1.moveTo(0, 0);
                            cs1.lineTo(10, 10);
                            cs1.moveTo(-1, 9);
                            cs1.lineTo(1, 11);
                            cs1.moveTo(9, -1);
                            cs1.lineTo(11, 1);
                            cs1.stroke();
                        }

                        PDColor patternColor1 = new PDColor(patternName1, patternCS1);

                        pcs.setNonStrokingColor(patternColor1);
                        pcs.addRect(50, 500, 200, 200);
                        pcs.fill();

                        // Uncolored pattern - the color is passed later
                        PDTilingPattern tilingPattern2 = new PDTilingPattern();
                        tilingPattern2.setBBox(new PDRectangle(0, 0, 10, 10));
                        tilingPattern2.setPaintType(PDTilingPattern.PAINT_UNCOLORED);
                        tilingPattern2.setTilingType(PDTilingPattern.TILING_NO_DISTORTION);
                        tilingPattern2.setXStep(10);
                        tilingPattern2.setYStep(10);

                        COSName patternName2 = page.getResources().add(tilingPattern2);
                        try (PDPatternContentStream cs2 = new PDPatternContentStream(tilingPattern2)) {
                            // draw a cross
                            cs2.moveTo(0, 5);
                            cs2.lineTo(10, 5);
                            cs2.moveTo(5, 0);
                            cs2.lineTo(5, 10);
                            cs2.stroke();
                        }

                        // Uncolored pattern colorspace needs to know the colorspace
                        // for the color values that will be passed when painting the fill
                        PDColorSpace patternCS2 = new PDPattern(null, PDDeviceRGB.INSTANCE);
                        PDColor patternColor2green = new PDColor(
                                new float[]{0, 1, 0},
                                patternName2,
                                patternCS2);

                        pcs.setNonStrokingColor(patternColor2green);
                        pcs.addRect(300, 500, 100, 100);
                        pcs.fill();

                        // same pattern again but with different color + different pattern start position
                        PDColor patternColor2blue = new PDColor(
                                new float[]{0, 0, 1},
                                patternName2,
                                patternCS2);
                        pcs.setNonStrokingColor(patternColor2blue);
                        pcs.addRect(455, 505, 100, 100);
                        pcs.fill();
                    }
                    doc.save("patterns.pdf");
                }
            }
        }
    }

        /**
         * This will print the usage for this document.
         */
        private static void usage()
        {
            System.err.println( "Usage: java " + CreatePatternsPDF.class.getName() + " <input-pdf>" );
        }
}