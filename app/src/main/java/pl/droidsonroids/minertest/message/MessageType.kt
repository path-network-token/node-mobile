package pl.droidsonroids.minertest.message

import kotlin.reflect.KClass

enum class MessageType(kclass: KClass<out MinerMessage>) {
    `check-in`(CheckIn::class),
    ack(Ack::class),
    error(MinerError::class),
    `job-request`(JobRequest::class),
    `job-result`(JobResult::class);

    val messageClass = kclass.java
}