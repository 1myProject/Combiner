package by.morinosenshi.combiner

import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt


class VideoMerger private constructor(private val context: Context, private val callback: FFMpegCallback) {

    private class Vid(file: File) {
        val width: Float
        val height: Float
        val ratio: Float
            inline get() = width / height

        init {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toFloat()
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toFloat()
            retriever.release()
        }

        override fun toString(): String {
            val w = MAX_WIDTH.roundToInt()
            val h = MAX_HEIGHT.roundToInt()

            val str = StringBuilder("scale=$w:$h:force_original_aspect_ratio=decrease")
            if (MAX_RATIO != ratio) {
                str.append(",pad=$w:$h:(ow-iw)/2:(oh-ih)/2:color=black")
            }

            return str.toString()
        }

        companion object {
            var MAX_HEIGHT = 0f
            var MAX_WIDTH = 0f
            var MAX_RATIO = 0f
        }

    }

    private val sizes = mutableListOf<Vid>()
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

        val inputFile = StringBuilder()
        for (v in videos!!) {
            inputFile.append("-i '${v.absolutePath}' ")
            val sz = Vid(v)

            if (sz.height > Vid.MAX_HEIGHT) {
                Vid.MAX_HEIGHT = sz.height
            }
            if (sz.ratio > Vid.MAX_RATIO) {
                Vid.MAX_RATIO = sz.ratio
                Vid.MAX_WIDTH = Vid.MAX_RATIO * Vid.MAX_HEIGHT
            }

            Log.i("RATIO", "${sz.width}/${sz.height}=${sz.ratio}")

            sizes.add(sz)
        }

        val filterComplex = StringBuilder("-filter_complex \"")
        for (sz in sizes.withIndex()) {
            val i = sz.index
            filterComplex.append("[$i:v]${sz.value}[v$i];")
        }
        for (i in sizes.indices) {
            filterComplex.append("[v$i][$i:a]")
        }
        filterComplex.append("concat=n=${sizes.size}:v=1:a=1[v][a]\" ")

        val command = StringBuilder().apply {
            append(inputFile)
            append(filterComplex)
            append("-map \"[v]\" -map \"[a]\" -y ")
//            append("-c:v libx264 ")
            append("-crf 23 ")
            append("-preset medium ")
            append("-stats -loglevel error ")
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