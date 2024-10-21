package com.nikitacherenkov.androidlibrary

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

        //Handler
        //Обработка Message из Looper
        //Если у потока есть Looper мы можем передать в него Handler и через
        // него мы будем подавать какие-то задачи на обработку
        val handler: Handler = Handler(Looper.getMainLooper())
        handler.post {
            //выполнить что-то на ui-потоке
        }
        //MessageQueue-очередь сообщения, она формируется из сообщений, составляющих
        // неограниченный по размеру целенаправленный односвязанный список
        //Message сортируются по timestamp
        //Handler модет регулировать и вставлять сообщения в определнную позицию
        //Lopper получает сообщение из очереди и передает ее тому handler который ее отправил
        //По умолчанию Looper только у UI потока, у нас есть возможность создать свой Looper и привязать его к фоновому потоку

        //WorkerThread
        class BasicWorkThread(name: String): HandlerThread(name){
            private val handler: Handler by lazy { Handler(looper) }

            fun postTask(runnable:() -> Unit){
                handler.post(runnable)
            }
        }


        //Так как создавая множество потоков в ручную вынуждает создавать и занимать большое количство пространства
        //А так же необходимо тщательно отслеживать состояния и обращения к ui
        //Были придуманы подходы к многопоточному программирования: Executor, LivaData, RxJava, Coroutines, EventBus
    }
}