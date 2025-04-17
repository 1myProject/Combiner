package by.morinosenshi.combiner

import java.io.File

interface FFMpegCallback {
    fun onStart()

    fun onProgress(progress: String)

    fun onSuccess(convertedFile: File)

    fun onFailure(error: Exception)

    fun onNotAvailable(error: Exception)

    fun onFinish()

}