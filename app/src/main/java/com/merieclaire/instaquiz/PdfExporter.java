package com.merieclaire.instaquiz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfExporter {
    public static void exportToPdf(Context context, List<String> content, String fileName) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = 50;
        for (String line : content) {
            canvas.drawText(line, 40, y, paint);
            y += 40;
        }

        pdfDocument.finishPage(page);

        File file = new File(context.getExternalFilesDir(null), fileName + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "PDF saved as: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }
}

