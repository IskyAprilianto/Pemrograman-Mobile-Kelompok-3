package com.example.uas

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun generatePdf(context: Context, projects: List<Project>): File {
    val document = PdfDocument()
    val paint = Paint()
    paint.textSize = 12f

    val pageWidth = 300
    val pageHeight = 600
    val margin = 10
    val lineHeight = 15

    projects.forEachIndexed { index, project ->
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        var yPosition = margin.toFloat()

        canvas.drawText("Project Name: ${project.name}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("Tujuan: ${project.tujuan}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("Tanggal Mulai: ${project.startDate}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("Tanggal Selesai: ${project.endDate}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("Supervisor: ${project.supervisor}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("Anggota: ${project.anggota}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("Status: ${project.status}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("Catatan: ${project.notes}", margin.toFloat(), yPosition, paint)
        yPosition += lineHeight

        document.finishPage(page)
    }

    val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "projects.pdf")

    try {
        document.writeTo(FileOutputStream(filePath))
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        document.close()
    }

    return filePath
}
