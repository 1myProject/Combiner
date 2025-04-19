package by.morinosenshi.combiner

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.VideoView
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.URL
import java.nio.CharBuffer
import javax.net.ssl.HttpsURLConnection

object CheckVersion {
    private const val URL_LATEST = "https://github.com/1myProject/Combiner/releases/latest"

    private suspend fun downVer(): String? {
        fun conv(s: InputStreamReader): String {
            val result = s.readText()
            s.close()
            return result
        }
        return withContext(Dispatchers.IO) c@{
            val con: HttpsURLConnection = try {
                val u = URL(URL_LATEST)
                u.openConnection() as HttpsURLConnection
            }
            catch (e: Exception) {
                return@c null
            }
            Log.i("CONN", con.responseCode.toString())
            if (con.responseCode !in 200..299) {
                return@c null
            }

            val html = conv(InputStreamReader(con.inputStream, "UTF-8"))
            return@c Regex(""">(\d\.\d)<""").find(html)?.groupValues?.get(1)
        }
    }

    suspend fun checkNew(): Boolean {
        val verD = downVer() ?: return false
        val verI = BuildConfig.VERSION_NAME
        return verD != verI
    }

     fun showAlert(context: Context) {
        val videoView = VideoView(context)
            videoView.setVideoURI("android.resource://${context.packageName}/${R.raw.upd}".toUri())
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = false
                mediaPlayer.start()
            }
            val alertDialog = AlertDialog.Builder(context).setTitle("Вышло новая версия!!!").setMessage("Обновись и получи новые фичи с багами или фиксы")
                    .setView(videoView).setPositiveButton("Даъ") { dialog, _ ->
                        // Открываем ссылку в браузере
                        val intent = Intent(Intent.ACTION_VIEW, URL_LATEST.toUri())
                        context.startActivity(intent)
                        dialog.dismiss()
                        context.toast("сам обновись, не маленький")
                    }.setNegativeButton("Нафиг надо") { dialog, _ ->
                        dialog.dismiss()
                    }.setOnDismissListener {
                        videoView.stopPlayback() // Останавливаем видео при закрытии
                    }.create()

            alertDialog.show()
        }

}