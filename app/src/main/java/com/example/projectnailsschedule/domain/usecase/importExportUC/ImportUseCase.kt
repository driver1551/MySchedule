package com.example.projectnailsschedule.domain.usecase.importExportUC

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.example.projectnailsschedule.utils.Util
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImportUseCase(var context: Context) {

    fun execute(treeUri: Uri) {
        val treeDocumentFile = DocumentFile.fromTreeUri(context, treeUri)
        if (treeDocumentFile != null && treeDocumentFile.exists() && treeDocumentFile.isDirectory) {
            val destinationDir = File(
                context.applicationInfo.dataDir,
                "databases"
            )

            Util().clearDir(destinationDir)

            val filesInDirectory = treeDocumentFile.listFiles()
            for (file in filesInDirectory) {
                val sourceUri = file.uri
                val sourceStream = context.contentResolver.openInputStream(sourceUri)

                if (sourceStream != null) {
                    val destinationFile = File(destinationDir, file.name)
                    val destinationStream = FileOutputStream(destinationFile)

                    try {
                        val buffer = ByteArray(1024)
                        var length: Int

                        while (sourceStream.read(buffer).also { length = it } > 0) {
                            destinationStream.write(buffer, 0, length)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "Error importing file ${file.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } finally {
                        sourceStream.close()
                        destinationStream.close()
                    }
                }
            }
        }
    }
}