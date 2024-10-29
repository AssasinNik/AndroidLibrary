package org.example

import com.sun.source.tree.Scope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.xml.crypto.Data
import kotlin.coroutines.CoroutineContext

class E {

}

suspend fun main(){
    //Каналы(Channels)
    //Основная цель коммуникация между несколькими корутинами
    //send и receive(suspend функции) основные методы для получения и отправки значения из канала и в канал
    //получить все значения из канала можно и просто проитерировав его
    val channel: Channel<E> = Channel()
    for(value in channel){
        //обрабаотка данных
    }
    //trySend и tryRecieve не suspend функцяи
    //может быть вызвана не из корутины
    //что позволяет отправить и получить значение, если канал имеет место в буффер, или у него уже есть значение в буффере

    //Закрытие канала
    channel.close()

    //проверить канал на то открыт ли он на отправку или получение модно с помощью
    channel.isClosedForSend
    channel.isClosedForReceive
    //Одновременно у канала может быть больше одного получателя так и больше одного отправщика
    //Каналы расчитаны на работу с несколькими корутинами

    //Channel может приводить к приостановке корутины, пока он не сможет обработать значение(на отправку/получение)
    //поэтому не стоит забывать закрывтаь channel

    //По умолчанию у канал нет буфера, следовательно send будет приостановлен
    //При создании канала можно указать capacity
    //Тогда операцию send можно будет даже отправлять, если еще нету никаких получателей, а значение поместится в буффер и будет отправлено позже
    val receiveChannel: Channel<E> = Channel()

    //Размеры буфера Channel
    //Rendezvous-без буфера(по умолчанию)
    //Conflated-размер буффера 1(хранит только последнее полученное значение)
    //Buffered-стандартный размер буфера определен в свойствах окружения(по умолчанию 64)
    //Максимально возможный размер буффера(Int.MAX_SIZE)
    //Можно в целом указать свою capacity(цифру) главное чтобы оно не перекрывала какую либо из константных
    val channel1: Channel<E> = Channel(capacity = BUFFERED)

    //При создании канала можно указать политику поведения send
    //suspend-политика по умолчанию
    //Корутина будет приостанволена, если значение принять сейчас некому, а буфер переполнен или отсутствует

    //DROP_OLDEST(очередь), DROP_LATEST(стек)
    //Удаляет значение(самые старые/самые новые) в буфере при его переполнении. Вызов send никогда не приостанвоит корутину,
    //а trySend() всегда успешно выполнится при наличии буффера

    //Горячий Flow
    //Значения могут быть получены лишь один раз
    // при повторном вызове collect у Flow будет краш
    receiveChannel.consumeAsFlow()

    //Горячий Flow
    //Может использоваться для нескольких получателей значений
    //множественный вызов collect
    receiveChannel.receiveAsFlow()

    //Чаще всего не придется создавать самому channel(Чаще всего встретим билдеры produce(receiveChannel) и actor(SendChannel))
    //produce-служит для генерации конечных или бесконечных потоков значений
    val scope =CoroutineScope(CoroutineName("coroutine1")+Dispatchers.IO+ SupervisorJob())
    val channel2 : ReceiveChannel<Int> =
        scope.produce {
            var x = 1
            while (true){
                send(x++)
            }
        }
    scope.launch {
        val data = channel.receive()
    }
    //actor служит для получения значения извне(помогате синхронизировать работу некольких корутин)
    val actor: SendChannel<Any> = scope.actor {
        val data = receive()
    }
    scope.launch {
        actor.send("")
    }

    


}



