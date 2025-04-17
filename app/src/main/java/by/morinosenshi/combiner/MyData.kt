package by.morinosenshi.combiner

import android.R
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

object MyData {
    val LIST: MutableList<VideoRow> = ArrayList()

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.path
        }

        // Для URI content:// используем DocumentProvider
        var filePath: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DATA)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                filePath = cursor.getString(columnIndex)
            }
        }

        // Если не получили путь через MediaStore, попробуем получить реальный файл
        if (filePath == null) {
            try {
                val file = DocumentFile.fromSingleUri(context, uri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, file?.name ?: "temp_file")
                FileOutputStream(tempFile).use { output ->
                    inputStream?.copyTo(output)
                }
                filePath = tempFile.absolutePath
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return filePath
    }

    fun addItem(context: Context, uri: Uri) {
        val p = getFilePathFromUri(context, uri) ?: return
        val f = File(p)

        LIST.add(VideoRow(f))
    }

    val List_FILES: List<File>
        get() {
            val arr = mutableListOf<File>()
            for (i in LIST) {
                arr.add(i.file)
            }
            return arr
        }

    data class VideoRow(val file: File) {
        private val img = CoroutineScope(Dispatchers.IO).async {
            try {
                val media = MediaMetadataRetriever()
                media.setDataSource(file.absolutePath)
                media.frameAtTime
            }
            catch (e: Exception) {
                e.printStackTrace()
                null // Возвращаем null в случае ошибки
            }
        }


        fun getImg(v: ImageView) {
            CoroutineScope(Dispatchers.Main).launch {
                val bitmap = img.await() // Ждём результат
                if (bitmap != null) {
                    v.setImageBitmap(bitmap) // Устанавливаем изображение в UI-потоке
                }
                else {
                    v.setImageResource(R.drawable.ic_menu_gallery)
                }
            }
        }
    }
}