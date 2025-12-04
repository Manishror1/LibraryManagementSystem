package com.manish.librarysystemfinal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class PdfReceiptGenerator {

    public static String createPDF(Context context,
                                   String receiptId,
                                   String name,
                                   String maskedCard,
                                   String bookTitle,
                                   double amount,
                                   String type) {

        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(300, 500, 1).create();

        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Library Payment Receipt", 50, 40, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        canvas.drawText("Receipt ID: " + receiptId, 20, 80, paint);
        canvas.drawText("Name: " + name, 20, 110, paint);
        canvas.drawText("Card: " + maskedCard, 20, 140, paint);

        canvas.drawText("Book: " + bookTitle, 20, 180, paint);
        canvas.drawText("Payment Type: " + type, 20, 210, paint);
        canvas.drawText("Amount: $" + amount, 20, 240, paint);

        canvas.drawText("Date: " + new Date().toString(), 20, 280, paint);

        pdf.finishPage(page);

        // Folder inside internal storage
        File dir = new File(context.getFilesDir(), "receipts");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, receiptId + ".pdf");

        // ⭐ SAFE WRITE (no empty PDFs)
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (Exception ignored) { }
        }

        pdf.close();
        return file.getAbsolutePath();
    }
}
