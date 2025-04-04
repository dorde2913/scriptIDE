package org.example.scripta.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


data class UIState(
    val numLines: Int = 0,
    val outputList: List<String> = listOf(),
    val errorList: List<String> = listOf(),
    val running: Boolean = false,
    val error: Boolean = false
)

class ScriptViewModel(): ViewModel() {


    private val _state = MutableStateFlow(UIState())
    val state = _state.asStateFlow()

    val scriptExecutor = ScriptExecutor()


    suspend fun executeScript(script: String){
        _state.value = _state.value.copy(outputList = listOf(), errorList = listOf())
        scriptExecutor.script = script

        setRunning(true)

        scriptExecutor.startScript()
        scriptExecutor.getScriptResult().collect{
            _state.value = _state.value.copy(outputList = _state.value.outputList.plus(it))
        }
        scriptExecutor.getError().collect{
            _state.value = _state.value.copy(errorList = _state.value.errorList.plus(it.replace("\t","    ")))
        }
        //println(scriptExecutor.retValue)
        setRunning(false)
        setError(scriptExecutor.retValue != 0)
    }

    fun setRunning(value: Boolean) {
        _state.value = _state.value.copy(running = value)
    }

    fun setError(value: Boolean) {
        _state.value = _state.value.copy(error = value)
    }

    fun stopScript(){
        //val process = scriptExecutor.process?.destroyForcibly()
        val process = scriptExecutor.process ?: return
        if (scriptExecutor.process!!.children()!=null)
        {
            for (child in scriptExecutor.process!!.children()){
                child.destroyForcibly()
            }
        }

        process.destroyForcibly()
        setRunning(false)
    }

}