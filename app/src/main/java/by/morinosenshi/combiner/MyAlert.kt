package by.morinosenshi.combiner

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File

class MyAlert(context: Context, private val runUI: (Runnable) -> Unit) : FFMpegCallback {
    private val liner = LinearLayout(context)
    private val text = TextView(context)

    private val alert = AlertDialog.Builder(context)
    private var dialog: AlertDialog? = null
    private var isEnd = false //останавливает выполнение функции onProgress

    private val rec: (Int, String) -> String = { resId, arg ->
        context.getString(resId, arg)
    }

    init {
        liner.addView(text)

        dialog = alert.setTitle("клеим ...").setView(liner).create()
    }


    override fun onStart() = runUI {
        dialog?.show()
    }

    override fun onProgress(progress: String) = runUI {
        if (isEnd) return@runUI

        Log.i("FFMPEG_LOG", progress)

        text.text = parseProgress(progress)
    }

    override fun onSuccess(convertedFile: File) = runUI {
        dialog?.setTitle("Success")

        isEnd = true

        text.text = rec(R.string.file_in, convertedFile.path)
    }

    override fun onFailure(error: Exception) = runUI {
        error.printStackTrace()
        text.text = error.toString()

        dialog?.setTitle("Error")
    }

    override fun onNotAvailable(error: Exception) = runUI {
        error.printStackTrace()
        text.text = error.toString()

        dialog?.setTitle("Error FFmpeg")
    }

    private fun parseProgress(row: String): String{
        val frame = Regex("""frame=\s*\d+""").find(row)?.value
        val fps = Regex("""fps=\s*\S+""").find(row)?.value
        val size = Regex("""size=\s*\d+\w\w""").find(row)?.value
        val time = Regex("""time=\s*[\d:.]+""").find(row)?.value
        val bitrate = Regex("""bitrate=\s*\S+""").find(row)?.value
        val speed = Regex("""speed=\s*\d+.\d+x""").find(row)?.value

        return "$frame\n$fps\n$size\n$time\n$bitrate\n$speed"
    }
}
