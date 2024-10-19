package org.example

import java.util.Observable
import java.util.Observer
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

fun main() {
    //пользовательский поток
    val thread =Thread{
        //указываем процессы в потоке
        println("Hello from thread ${Thread.currentThread().name}")
    }
    //запускаем поток
    thread.start()
    //поток из которого join будет заблокирован до тех пор пока не завершится поток который вызвал join
    thread.join()
    //останавливаем поток(terminate)-передаем намерение об остановке
    thread.interrupt()

    //daemon поток(фоновые задачи) не дожидаемся ее завершения
    val thread2 =Thread{
        Thread.sleep(2000)//блокировка потока
        //указываем процессы в потоке
        println("Hello from thread ${Thread.currentThread().name}")//не выведется так как программа завершится быстрее
    }
    thread2.isDaemon=true
    //запускаем поток
    thread2.start()

    //создает и вместе с этим запускает поток
    val thread3 = thread {
        println("dff")
    }

    val thread4 = thread {
        repeat(10000){
            if(!Thread.interrupted()){//проверяем вызвал ли кто завершении потока
                //если нет то продолжаем
            }
            else{
                //останавливаем поток и очищаем ресурсы
            }
        }
    }
    //mutex-механизм доступа к общим ресурсам только одному потоку в опредленной время
    //критическая секция
    val lock = Any()//ссылка на обьект должна быть неизменяемой
    synchronized(lock){//любой обьект можно передать
        //тело потока
    }
    //аналог lock с использованием библиотеки java.util.concurent
    var b = 0
    val lock_concurent: Lock = ReentrantLock()
    val thread_lock = thread {
        lock_concurent.withLock {
            b = 1
        }
    }
    thread_lock.join()
    //tryLock проверка на блокировку

    //Thread-safe ObserverList
    class ObservableSafe : Observable() {
        private val lock: Any = Any()
        private val observers: MutableSet<Observer> = mutableSetOf()

        override fun addObserver(o: Observer?) {
            synchronized(lock) {
                if (o != null) {
                    observers.add(o)
                }
            }
        }
        override fun deleteObserver(o: Observer?) {
            synchronized(lock) {
                observers.remove(o)
            }
        }
        override fun notifyObservers(arg: Any?) {
            val currentObservers: List<Observer>
            // Сначала копируем список наблюдателей под защитой блокировки
            synchronized(lock) {
                currentObservers = observers.toList() // Создаем копию списка для безопасной итерации
            }

            // Теперь уведомляем всех наблюдателей без блокировки
            for (observer in currentObservers) {
                observer.update(this, arg)
            }
        }
    }
}