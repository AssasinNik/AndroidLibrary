package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.actor


fun main(){

}

//Синхронизация между корутинами
//Пример когда 2 корутины могут обращаться к одному ресурсу
suspend fun doWork(){
    var counter = 0
    coroutineScope {
        launch(Dispatchers.Default) {
            counter+=10
        }
        launch(Dispatchers.Default) {
            counter+=10
        }
    }
}

//Принципы синхронизации
//Только один поток может иметь доступ в критическую секцию в определнный момент времени
//Либо за счет ограничения доступа, либо за счет обеспечения очередности

//Снятие блокировки должно происходить на том же потоке, на котором она была захвачена
//Иначе ресурс зависнет навсегда

//Так как suspend функции могут происходить на разных потоках
//Следовательно между захватом и снятие блокировки не должно быть вызовов suspend функций

//Критическая секция-участок исполняемого кода программы, в котором производится доступ к общему ресурсу
//не должен быть одновременно использован более чем одним потоком


//Как обеспечить последовательность
//Один из вариантов создание своего dispatcher который будет работать на одном выделенном потоке
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
suspend fun doSome(){
    val counterContext = newSingleThreadContext("Counter")
    var counter = 0
    val scope = CoroutineScope(CoroutineName("Scope 1"))
    val jobs = List(100){
        scope.launch(start = CoroutineStart.LAZY) {
            repeat(1_000){
                withContext(counterContext){
                    counter+=10
                }
            }
        }
    }
    jobs.joinAll()
    counterContext.close()//обязательно закрыть CoroutineContext после того как он не нужен
}

//Channel для синхронизации
//Синхронизация через коммуникацию

//Вариант с actor
// Определяем различные типы сообщений для работы с общим ресурсом
sealed class CounterMsg{
    class Increment: CounterMsg()
    class GetValue(val response: CompletableDeferred<Int>): CounterMsg()
}
// Функция для создания актора, управляющего доступом к счетчику
@OptIn(ObsoleteCoroutinesApi::class)
fun CoroutineScope.counterActor() = actor<CounterMsg>(capacity = BUFFERED) {
    var counter = 0 // общий ресурс, защищенный актером
    for (msg in channel) { // обрабатываем каждое сообщение по очереди
        when (msg) {
            is CounterMsg.Increment -> counter++
            is CounterMsg.GetValue -> msg.response.complete(counter)
        }
    }
}
fun someWork() = runBlocking {
    // Создаем актер для управления счетчиком
    val counter = counterActor()

    // Запускаем 1000 корутин, каждая из которых увеличивает значение счетчика
    val jobs = List(1000) {
        launch {
            counter.send(CounterMsg.Increment())
        }
    }

    // Ждем завершения всех корутин
    jobs.forEach { it.join() }

    // Получаем текущее значение счетчика
    val response = CompletableDeferred<Int>()
    counter.send(CounterMsg.GetValue(response))
    println("Final counter value: ${response.await()}")

    // Закрываем актор
    counter.close()
}
