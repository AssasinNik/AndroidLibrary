package com.nikitacherenkov.androidlibrary

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

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
        //Coroutine-обьект приостанавляемых вычислений, лишь концептуально похожа на поток
        //Позволяет приостановить выполнение функции и затем вернуться к ней(возобновить ее)
        //принцип Structed concurrency
        //встроенная поддержка отмены

        //suspend-функция является приостанавливаемой
        suspend fun showBooks(BooksId: Int){
            //suspend лишь сообщает что внутри фнкции есть блок кода который может быть приостановлен
            val book = api.fetchData(BooksId)//мы запускаем корутину и приостанавливаем выполнение корутины
            //в это время спокойно живет Main поток и отрисовывает графику
            //получив даннуе мы возвращяемся в ту  же точку где остановились
            showBooks_onScreen(book)
        }

        //Coroutines Builders(launch, async, runBlocking)

        //launch
        suspend fun coroutine(): Unit {
            //coroutineScope позволяет дожидать завершения выполнения дочерних корутин
            coroutineScope {//основная корутина выделяет место под выполнение двух корутин launch
                launch {//таким образом создается связь родительской корутины с дочерними-это и есть приницп Structed concurrency
                    delay(1000)//приостанавливаем корутину
                    println("coroutine 1")
                }
                launch {
                    delay(2000)
                    println("coroutine 2")
                }
                //async похож на launch но предназначен для возвращения результата
                val text: Deferred<String> = async {
                    "Text1"
                }
                //Deferred внутри себя хранит результат асинхронных операций и предоставляет интерфейс контракт для получения значения из его обьекта
                val result = text.await()//возвращает результат, как только готов иначе приостановит корутину пока не будет результата

            }
            //практически не используется
            runBlocking {//блокирует поток на котором он был создан до тех пор пока корутина которую он породил не будет завершена
                val text1: Deferred<String> = async {
                    delay(2000)
                    "Text"
                }
                text1.await()
            }
            //Принцип Structured Concurrency
            //CoroutineScope->Coroutine builders(launch,async)->Suspending Functions-> Coroutine Scope Functions(coroutine scope, withContext)
            //Coroutine Scope Functions -> Coroutines Builders, Coroutine Scope Functions -> Suspending Functions

            //В Android разработке уже есть готовые scope на пример при использовании Jetpack ViewModel
            viewModelScope.launch{

            }
            //Coroutine Context-индексированный набор элементов где каждый элемент является
            // CoroutineContext и имеет уникальный ключ для его индентификации
            //концептуально похож на map или set
            //Coroutine context хранится в корутинах и помогает им определять как корутины должны выполняться
            //в каком потоке должны выполняться, каково их состояние
            //CoroutineContext передается от родителей к предкам

            //Базовые элементы Контекста (Job, CoroutineName, CoroutineDispatcher, CoroutineExceptionHandler)

            val context: CoroutineContext = CoroutineName("Name")
            val coroutineName : CoroutineName? = context[CoroutineName]//Name
            val job : Job? = context[Job]//null

            //В контексте доступно обьединение, при обьедении с одинаковым ключом данные будут перезаписанный
            val context2 = CoroutineName("Name2")+context//Name2

            //Job-олицетворяет работу нашей корутины
            //Каждый launch и async создает из библиотеки свой job и они возвращают Job
            //Отменяемый обьект с изменяемым циклом

            val name = CoroutineName("Name")
            val job2 = Job()
            coroutineScope {
                launch(name + job2 ) {
                    val childName = coroutineContext[CoroutineName]
                    val childJob = coroutineContext[Job]

                    println(childName==name)//true

                    println(childJob==job2)//false
                    println(childJob==job2.children.first())//true
                }
            }

        }
    }
}