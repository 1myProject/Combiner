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
//        process.progress = num.toInt()
        if (isEnd) return@runUI
        s += progress
        if ('\n' in progress) {
            Log.i("FFMPEG_LOG", s)
            s = s.substringAfter('\n')
        }

        log.append(progress)

        if (log.count { c -> c == '\n' } > MAX_LINES) {
            val inx = log.indexOf('\n') + 1
            log.delete(0, inx)
        }

        text.text = log.toString()
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

    override fun onFinish() = runUI {
//        text.text = "Finish"
    }

    companion object {
        private const val MAX_LINES = 20
    }
}
