package pl.droidsonroids.minertest.runner

import pl.droidsonroids.minertest.message.JobRequest
import java.io.IOException
import javax.net.SocketFactory

class TcpRunner : Runner {

    override fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest, ::runTcpJob)

    private fun runTcpJob(jobRequest: JobRequest): String {
        jobRequest.endpointPort ?: throw IOException("missing endpoint port")

        //TODO check if SSLSocket is supported by RN app
        return SocketFactory.getDefault().createSocket(jobRequest.endpointAddress, jobRequest.endpointPort).use {
            it.getOutputStream().bufferedWriter().write(jobRequest.payload)
            it.getInputStream().bufferedReader().readText()
        }
    }
}