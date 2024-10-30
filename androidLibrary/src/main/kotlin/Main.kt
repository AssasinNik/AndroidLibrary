package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*


fun main(){

}

//Flow-реализация реактивных потоков на основе корутин
//Flow- холодный асинхронный поток данных, который последовательно выдает значения
//Холодный поток: Flow начинает эмитировать значения только тогда, когда к нему кто-то подписывается (активируется).
//Flow изначально был толкьо холодным, но затем были реализованы StateFlow and SharedFlow

//Холодный стрим выдает значения только при запуске
//Горячий стрим начинает работать сразу при создании

//FlowBuilder
fun build(){
    //Создание flow из набора значений
    flowOf(1)
    flowOf("1", "2", "3")
    listOf("1", "2").asFlow()

    //Для самостоятельного эмит необходим оператор flow
    flow{
        emit(1)//отправляет значение в поток
    }
}
//Операторы
suspend fun oper(){
    //ПРОМЕЖУТОЧНЫЕ ОПЕРАТОРЫ
    //принимают входной поток upstream и возвращают выходной поток downstream
    //создает конечный flow, подписывается на входящий flow, получает все данные из входящего flow, выполняет операции с данными и emit их в выходной поток
    //map преобразует каждое значение Flow
    flowOf(1, 2, 3).map { it*2 }
    //filter фильтрует значение согласно условию
    flowOf(1,2,3).filter { it%2==0 }
    //take берет только заданное количество значений
    flowOf(1,2,3).take(2)

    //Терминальные оператор(suspend функции которые должны быть запущены в корутине в рамках какого-то scope)
    //Некоторые терминальные операторы не suspend функции, так как являются удобными обертками над suspend функциями
    //collect финальный оператор, который активирует поток и обрабатывает каждое значение(подписчик на поток)
    flowOf(1, 2, 3).collect { println(it) }
}

//чтобы для collect не создавать какждый раз корутину можно использвоать lauchin которая сама это сделает
suspend fun doLaunch(){
    flow{
        emit("Hello")
    }
        .filter { it=="Hello" }
        .launchIn(CoroutineScope(CoroutineName("Scope Flow")))
}

//Flow может быть бесконечным и эмитить значения пока его не остановят

//Buffer позволяет собирать все полученные значения из flow и затем передает их коллектору, когда он будет говто их обработать
fun dodl(){
    flow<String> {  }.buffer(
        capacity = BUFFERED,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
}

//Смена контекста выполнения для upstream
//flowOn меняет контекст для всех операторов цепочки до предыдущего вызова flowOn
//Context Preservation - особенность Flow, которая требует сохранения контекста коллектора
fun change(){
    flow<String> {  }.flowOn(Dispatchers.IO)
}

//реализовать оператор который эмитит только изменившееся значения
fun <T> Flow<T>.unique(): Flow<T> =flow {
        var lastValue: Any? = NoValue
        collect{ value: T ->
            if(lastValue!=value){
                lastValue=value
                emit(value)
            }
        }
    }
private object NoValue

//Exception Transparency-ошибки в выходном потоке всегда долзны выходить до коллектора
fun except(): Flow<Int> = flow {
    emit(1)
    emit(2)
    throw Exception("An error occurred!") // Искусственное исключение
    emit(3)
}.catch { e: Throwable ->
    println("Caught exception: ${e.message}")
}

//Горячий Flow

//SharedFlow-emit значение всем его подписчикам(эмиттит значения сразу при создании)
//бесконечный поток данных
//может быть множество подписчиков
//не имеет контекста выполнения(flowOn не работает)
//бывает также MutableSharedFlow которые позволяет и получать и отправлять значения
fun shared(){
    //replay cache-элементы доставляются всем новым подписчикам
    //extra buffer-элементы сохраняются при наличии подписчиков, когда они не могут быть доставлены сразу же(очищается при отсутсвии подписчиков)
    val flow: Flow<String> = MutableSharedFlow( replay = 5, extraBufferCapacity = 4)
}

//StateFlow-частный случай SharedFlow, хранит одно значение и доставляет его всем своим подписчикам
//новое значение будет доставляться только если оно изменилось
//StateFlow ~ SharedFlow<T>(replay = 1, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST)


