package org.example

import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext


//Continuation-можно рассматривать как callback который будет вызван по окончанию тела функции
//Unit — это специальный тип, который представляет отсутствие значения или «пустоту». Он аналогичен void в Java.
suspend fun doWork(){
    //долгая работа
}
fun doWork(completion: Continuation<Unit>){}

//Каждая корутина привязана к времени жизни scope
//Если в потоках мы использовали interupt для остановки потока, и затем была необходимость проверять состояние
//В корутинах же состояние корутины проверяется перед тем как продолжить ее выполнение после прерывания


fun main(): Unit = runBlocking{
    /*launch
    repeat(100){
        launch {
            val result = someWork(it.toString())
            println(result)
        }
    }
     */

    val coroutines : List<Deferred<String>> = List(100) {
        //условия старта меняются только тогда когда корутина будет явно запущена(то есть до await)
        //async(start = CoroutineStart.LAZY)
        async(start = CoroutineStart.DEFAULT) {
            //if (isActive){} один из видов проверок корутин на отмену
            someWork(it.toString())
        }
    }
    //coroutines.forEach { println(it.await()) }-вывод значений при async
    //coroutines.forEach { it.cancelAndJoin() }-отмена корутины и дожидаемся ее выполнения
    //coroutines.forEach { it.cancel() }-остановка корутин, только это не полностью надо проверять статус корутины


    //CoroutineContext ~ Map<Key<Element> , Element>
    //Каждое значение из контекста также является контекстом
    //При сложение контекстов значения будут обьединены, а содинаковым ключами будут взяты из правого

    //Job.Обьект корутины
    val job: Job = launch {  }
    //Всего шесть состояний New->Active->Completing||Canceling->Completed||Cancelled
    //ошибки или cancel завершит нетолько дочернюю, но и родительскую
    //чтобы такого не возникало используют SupervisorJob
    //cancel-отменяет выполнение Job и принимает опционально причину CancellationException
    //invokeOnCompletion-позволяет задать callback который будет выполнен по окончанию Job
    //join-приостанавливает выполнение корутины и ожидает выполнение Job
    //start-запускает корутину связанную с Job, если она еще не была запущена(при использовании Сoroutines start)
    //val children - получаем список дочерних Job
    //ensureActivity-проверяет активна ли job и выкинет если нет CancellationException

    //CoroutineDispatcher-поток для корутины
    //показывает на каком потоке или потоках будет выполняться корутина
    //Всего стандартных Dispatchers 4: Main, Default, Unconfined, IO

    //Default-стандартный, использует для интенсивно вычислительных операция, либо перенос задач в фон
    // (колчиество потоков в пуле равно количеству ядер в процессоре, но никогда не меньше 2)

    //IO-предназначен для выполнения IO-операция на специально выделенном пуле потоков
    // (колчиество потоков в пуле равно количеству ядер в процессоре, но никогда не меньше 64)
    //Такое количество потоков обеспечивает, чтобы не было частых простоев и постоянных блокировок

    //Main-выполнение работы на главном потоке

    //Unconfined-не привязан к каком либо потоку, выполнение корутины происходит в том же потоке
    //где она и была вызвана, а после вызова первой suspend функции корутина продолжит работу в контексте вызванной suspend функции

    //CoroutinesExceptionHandler(повлиять на выполнение программы мы не можем, он вызывается в последнюю очередь(может быть вызван на любом потоке))
    CoroutineExceptionHandler { coroutineContext, throwable ->
        println("Error")
    }
    //JavaServiceLoader-предоставляет возможность поиска и создания зарегестрированных экземпляров
    //интерфейсов или абстрактных классов

    //CoroutineName-осмысленное имя для корутины полезное для отладки приложения

    //Жизненный цикл корутин и CoroutineScope.StructuredConcurrency
    //CoroutineScope-жизненный цикл для выполнения асинхронных операций
    //runBlocking не использует CoroutineScope так как он и так блокирует поток в котором выполняется на время работы корутины
    //Scope может отменить выполнение все дочерних корутин, если возникнет ошибка или операция отменена
    //Scope знает про все корутины(любая корутина в scope будет храниться ссылкой через отношение родитель-ребенок)
    //Scope ожидает выполнения всех дочерних корутин, но не обязательно завершатся вместе с ними

    //CoroutineScope хоть и является обертой над CoroutineContext
    //Но имеет совершенно другое назначение, он предназначен для обьединения корутин и может предоставлять им общий контекст(под капотом передает корутинам Job который будет их обьединять и являться родительской)


    //GlobalScope не привязан к какой-либо Job. Все корутины запущенные в рамках него будут работать до свой остановки или остановки процесса
    //Не рекомендуется
    //Не следует правилам, и не соовтетсвует structured concurrency

    //Создание Scope
    val scope = CoroutineScope(Job()+Dispatchers.Default)
    //Основное, что job необходим для каждой scope(либо он сам создатся для контекста)
    //SuperVisorScope также позволяет не блокировать все остальные scope

    //остановка CoroutineScope
    scope.cancel("Stopping")
    //для отсановки всех корутин внутри scope, но не самого scope
    scope.coroutineContext[Job]?.cancel("Stopping coroutines")

    suspend fun loadingData(){
        withContext(Dispatchers.IO){//Смена контекста корутины
            someWork("Something")
        }
        //код выполнится после withContext
    }


    //обработка ошибок в корутинах

    //для каждого вызова корутины разные методы try catch
    launch {
        try {
            doWork()
        } catch (e: Exception){
            //обработка
        }
    }
    //Deferred обьект вовращаемые функцией async, он ожидает получение результата корутины. Он унаследован от Job
    val deferred : Deferred<String> = async {
        someWork("Coroutine")
    }
    try {
        deferred.await()
    } catch (e: Exception){
        //обработка
    }
    //(сверху)данный метод в случае вызывет блокировку всех оставшихся корутин обьединенный скоупом
    //решение(2 варианта)
    supervisorScope {
        val deferred2  = async {
            throw IOException()
        }
        try {
            deferred2.await()
        } catch (e: IOException){
            //обработка
        }
    }
    //остановка корутины-это тоже ошибка(чтобы понять что корутина именно остановлена есть CancellationException)
    //CancelException всегда лучше отдельно проверять отдельным catch

    withContext(NonCancellable){//использовать строго в withContext
        //то что доработает при завершении корутины
    }


    //SuperVisorJob и SuperVisorScope

    //SupervisorJob в Kotlin — это специальный тип работы (Job), который предоставляет механизм для изоляции ошибок в дочерних корутинах от родительской корутины.
    //Он обеспечивает независимость выполнения корутин, чтобы ошибка в одной дочерней корутине не приводила к автоматической отмене других корутин.

    //SupervisorScope в Kotlin — это инструмент для асинхронного программирования, при использовании которого ошибка в дочерней корутине не приводит к сбою всей области и не влияет на другие дочерние корутины
    //С помощью SupervisorScope каждое асинхронное обращение получает свой защитный блок с блоками try-catch.
}

//пока не завершится одна функция, не запустится другая, поэтому важно использвоать корутины
suspend fun someWork(name:String): String{
    delay(Random().nextInt(5000).toLong())//suspend функция для приостановления корутины
    return "Done $name"
}


