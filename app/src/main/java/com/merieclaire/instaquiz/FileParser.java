package com.merieclaire.instaquiz;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.Objects;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;


public class FileParser {

    public static String parseFile(Context context, Uri uri) {
        String extension = getFileExtension(context, uri);

        if (extension == null) return "";

        try {
            switch (extension.toLowerCase()) {
                case "txt":
                    return parseTxt(context, uri);
                case "docx":
                    return parseDocx(context, uri);
                case "pdf":
                    return parsePdf(context, uri);
                default:
                    return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String parseTxt(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("TXT input stream is null.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        reader.close();
        return builder.toString();
    }

    private static String parseDocx(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("Docx input stream is null.");
        XWPFDocument document = new XWPFDocument(inputStream);
        StringBuilder builder = new StringBuilder();

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            builder.append(paragraph.getText()).append("\n");
        }
        document.close();
        return builder.toString();
    }

    private static String parsePdf(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("PDF input stream is null.");

        PDDocument document = PDDocument.load(inputStream);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        return text;
    }


    private static String getFileExtension(Context context, Uri uri) {
        String extension = null;
        String fileName = null;

        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

            }
        }

        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return extension;
    }
}


