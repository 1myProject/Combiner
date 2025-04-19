package by.morinosenshi.combiner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Разрешение получено, выполняем действия с Movies директорией
                Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show()
            }
            else {
                // Разрешение отклонено, уведомляем пользователя
                Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_SHORT).show()
            }
        }

        checkFirstLaunchAndRequestPermission(this, requestPermissionLauncher)

        replaceFragment(ItemFragment())

        chkVer()
    }

    fun chkVer() {
        CoroutineScope(Dispatchers.IO).launch {
            if (CheckVersion.checkNew()) runOnUiThread {
                Log.i("STAT", "OK")
                CheckVersion.showAlert(this@MainActivity)
            }
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main, fragment)
        fragmentTransaction.commit()
    }


    fun checkFirstLaunchAndRequestPermission(activity: FragmentActivity, requestPermissionLauncher: ActivityResultLauncher<String>) {
        if (isMoviesDirectoryWritable()) return
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение
            requestPermissionLauncher.launch(permission)
        }
    }


    fun isMoviesDirectoryWritable(): Boolean {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return moviesDir.exists() && moviesDir.canWrite()
    }
}