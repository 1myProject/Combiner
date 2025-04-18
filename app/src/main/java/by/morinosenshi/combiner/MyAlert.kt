package by.morinosenshi.combiner

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import by.morinosenshi.combiner.FFMpegCallback
import java.io.File

class MyAlert(context: Context, private val runUI: (Runnable) -> Unit) : FFMpegCallback {
    //    private val process = ProgressBar(context)
    private var log = StringBuilder()
    private val liner = LinearLayout(context)
    private val text = TextView(context)

    private val alert = AlertDialog.Builder(context)
    private var dialog: AlertDialog? = null
    private var isEnd = false

    private val rec: (Int, String) -> String = { resId, arg ->
        context.getString(resId, arg)
    }

    init {
//        text.visibility = View.GONE
        liner.addView(text)
//
//        process.max = 100
//        process.progress = 0
//        process.visibility = View.VISIBLE
//
//        liner.addView(process)

        dialog = alert.setTitle("клеим ...").setView(liner).create()

    }


    override fun onStart() = runUI {
        dialog?.show()
    }


    var s = ""
    override fun onProgress(progress: String) = runUI {
        if (isEnd) return@runUI

        Log.i("FFMPEG_LOG", progress)

//        log.append(progress)
//
//        val count = log.count { c -> c == '\r' }
//        if (count > MAX_LINES) {
//            val inx = log.indexOf('\r') + 1
//            log.delete(0, inx)
//        }
//
//        text.text = log.toString()

        text.text = parseProgress(progress)
    }

    override fun onSuccess(convertedFile: File) = runUI {
        dialog?.setTitle("Success")

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
        val frame = Regex("frame=\\s*\\d+").find(row)?.value
        val fps = Regex("fps=\\s*\\d+").find(row)?.value
        val size = Regex("size=\\s*\\d+\\w\\w").find(row)?.value
        val time = Regex("time=\\s*[\\d:.]+").find(row)?.value
        val bitrate = Regex("bitrate=\\s*[^\\S]+").find(row)?.value
        val speed = Regex("speed=\\s*\\d+.\\d+x").find(row)?.value

        return "$frame\n$fps\n$size\n$time\n$bitrate\n$speed"
    }
}
