package by.morinosenshi.combiner

import android.app.Activity
import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.IOException


class VideoMerger private constructor(private val context: Context, private val callback: FFMpegCallback) {

    private var videos: List<File>? = null
    private var outputPath: File? = null

    fun setVideoFiles(originalFiles: List<File>): VideoMerger {
        this.videos = originalFiles
        return this
    }

    fun setOutputPath(output: File): VideoMerger {
        this.outputPath = output
        return this
    }

    fun mergeConcat() {
        if (!checkStar()) return

        val concat = StringBuilder()
        for (v in videos!!) {
            concat.append("file '${v.absolutePath}'\n")
        }

        val fls = File(context.cacheDir, "files").apply {
            writeText(concat.toString())
        }

        val command = StringBuilder().apply {
            append("-f concat ")
            append("-safe 0 ")
            append("-i \"${fls.absolutePath}\" ")
            append("-c copy -y ")
            append("\"${outputPath!!.absolutePath}\"")
        }

        Log.i("Command", command.toString())

        start(command.toString())
    }


    private fun checkStar(): Boolean {
        if (outputPath == null) {
            callback.onFailure(IOException("Output file not set"))
            return false
        }

        if (videos == null || videos!!.isEmpty()) {
            callback.onFailure(IOException("Files not set"))
            return false
        }

        for (v in videos!!) if (!v.canRead()) {
            callback.onFailure(IOException("Can't read the file. Missing permission?"))
            return false
        }
        return true
    }

    private fun start(command: String) {
        try {
            callback.onStart()
            FFmpegKit.executeAsync(command, { session ->
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    callback.onSuccess(outputPath!!)
                    clearCashDir()
                }
                else {
                    if (outputPath!!.exists()) outputPath!!.delete()
                    callback.onFailure(IOException("FFmpegKit failed"))
                }
            }, { log ->
                callback.onProgress(log.message)
            }, {})
        }
        catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    private fun clearCashDir() {
        context.cacheDir.deleteRecursively()
    }

    companion object {
        fun with(context: Activity): VideoMerger {
            val rUI = context::runOnUiThread
            val d = MyAlert(context, rUI)
            return VideoMerger(context, d)
        }
    }
}