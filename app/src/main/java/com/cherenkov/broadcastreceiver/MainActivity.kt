package com.cherenkov.broadcastreceiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var receiver: AirplaneModeChangedReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*
        receiver = AirplaneModeChangedReceiver()

        //IntentFilter используется чтобы система поределяла, какие приложения хотят получать какие intent
        //Здесь мы сообщаем что хотим получать intents об изменениях авиарежима
        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, it, Context.RECEIVER_NOT_EXPORTED)
            } else {
                // Для старых версий
                registerReceiver(receiver, it)
            }
        }
        */

        //Существуют два типа broadcast receiver: статические и динамические
        //Статические обновляются в манифесте и работают даже если ваше приложение закрыто
        //Динамические работают если приложение работает или свернуто


    }

    override fun onStop() {
        super.onStop()
        //unregisterReceiver(receiver)
    }
}