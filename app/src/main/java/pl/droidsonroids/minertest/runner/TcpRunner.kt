package pl.droidsonroids.minertest.runner

import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult
import java.io.IOException
import javax.net.SocketFactory

class TcpRunner : JobRunner {
    override fun runJob(jobRequest: JobRequest): JobResult {
        try {
            //TODO check if RN supports SSL socket equivalent
            SocketFactory.getDefault().createSocket(jobRequest.endpointAddress, jobRequest.endpointPort).use {
                it.getOutputStream().bufferedWriter().write(jobRequest.payload)
                val responseBody = it.getInputStream().bufferedReader().readText()

                return JobResult(
                    responseBody = responseBody,
                    jobUuid = jobRequest.jobUuid,
                    responseTime = TODO(),
                    status = TODO()
                )
            }
        } catch (e: IOException) {

        }
        TODO()
    }
}