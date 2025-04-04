package org.example.scripta.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ScriptExecutor {



    var script: String = ""
    var retValue: Int = 0

    fun getError() = flow{
        BufferedReader(InputStreamReader(File("error.txt").inputStream())).use{ reader ->
            reader.lineSequence().forEach { emit(it) }
        }
    }

    var process: Process? = null


    fun stopExecution(){

        //println(process?.pid())
        process?.destroyForcibly()

    }


    suspend fun startScript(){
        val scriptFile = File("tempscript.kts")
        scriptFile.printWriter().use {
            it.println(script)
        }
        val isWindows = System.getProperty("os.name").startsWith("Windows")

        val processBuilder = if (isWindows)ProcessBuilder("cmd", "/c", "kotlinc -script ${scriptFile.absolutePath}")
        else ProcessBuilder("kotlinc -script ${scriptFile.absolutePath}")


        process = withContext(Dispatchers.IO) {
            processBuilder//.redirectErrorStream(true)
                .redirectError(File("error.txt"))
                .start()
        }

        //println(process?.pid())
    }





    fun getScriptResult(): Flow<String> = flow {
        if (process == null )return@flow

        BufferedReader(InputStreamReader(process!!.inputStream)).use { reader ->
            reader.lineSequence().forEach {
                delay(10)
                emit(it)
            }
        }

        retValue = process!!.waitFor()

        //println("RET: $retValue")
    }
        .flowOn(Dispatchers.IO)
}