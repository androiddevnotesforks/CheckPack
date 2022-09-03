package com.commandiron.vacationchecklist.presentation.checklist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.commandiron.vacationchecklist.domain.model.ChecklistItem
import com.commandiron.vacationchecklist.domain.model.vacations
import com.commandiron.vacationchecklist.domain.preferences.Preferences
import com.commandiron.vacationchecklist.domain.use_cases.UseCases
import com.commandiron.vacationchecklist.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val useCases: UseCases,
    private val preferences: Preferences
): ViewModel() {

    var state by mutableStateOf(ChecklistState())
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadSettings()
        getActiveVacation()
        getChecklistItems()
    }

    fun onEvent(userEvent: ChecklistUserEvent) {
        when (userEvent) {
            is ChecklistUserEvent.OnCheck -> {
                if(state.doubleCheckEnabled){
                    if(userEvent.checklistItem.isChecked){
                        check(userEvent.checklistItem)
                    }else{
                        state = state.copy(
                            showAlertDialog = true,
                            checkedChecklistItem = userEvent.checklistItem
                        )
                    }
                }else{
                    check(userEvent.checklistItem)
                }
            }
            ChecklistUserEvent.OnChecklistCompleteBack -> {
                state = state.copy(
                    isChecklistCompeted = false
                )
            }
            ChecklistUserEvent.OnGridViewClick -> {
                preferences.saveShouldShowGridView(true)
                loadSettings()

            }
            ChecklistUserEvent.OnListViewClick -> {
                preferences.saveShouldShowGridView(false)
                loadSettings()
            }
            is ChecklistUserEvent.OnSliderChange -> {
                state = state.copy(
                    sliderValue = userEvent.value,
                    gridColumnCount = when(userEvent.value){
                        0f -> 5
                        0.25f -> 4
                        0.50f -> 3
                        0.75f -> 2
                        1f -> 1
                        else -> 3
                    },
                    listItemHeightValue =  when(userEvent.value){
                        0f -> 24f
                        0.25f -> 32f
                        0.50f -> 64f
                        0.75f -> 96f
                        1f -> 128f
                        else -> 64f
                    }
                )
            }
            ChecklistUserEvent.OnSliderValueChangeFinished -> {
                preferences.saveSliderValue(state.sliderValue)
            }
            is ChecklistUserEvent.OnAlertDialogConfirm -> {
                state = state.copy(
                    showAlertDialog = false
                )
                state.checkedChecklistItem?.let {
                    check(it)
                }
            }
            ChecklistUserEvent.OnAlertDialogDismiss -> {
                state = state.copy(
                    showAlertDialog = false
                )
            }
        }
    }

    private fun getActiveVacation() {
        state = state.copy(
            activeVacation = vacations.find { it.id ==  preferences.loadActiveVacationId()}
        )
    }

    private fun getChecklistItems() {
        viewModelScope.launch {
            state = state.copy(
                checklistItems = useCases.getAllChecklistItems()
            )
            calculateCheckCount()
        }
    }

    private fun loadSettings() {
        state = state.copy(
            doubleCheckEnabled = preferences.loadShouldDoubleCheck(),
            gridViewEnabled = preferences.loadShouldShowGridView(),
            sliderValue = preferences.loadSliderValue()
        )
        state = state.copy(
            gridColumnCount = when(state.sliderValue){
                0f -> 5
                0.25f -> 4
                0.50f -> 3
                0.75f -> 2
                1f -> 1
                else -> 3
            },
            listItemHeightValue =  when(state.sliderValue){
                0f -> 24f
                0.25f -> 32f
                0.50f -> 64f
                0.75f -> 96f
                1f -> 128f
                else -> 64f
            }
        )
    }

    private fun check(checklistItem: ChecklistItem){
        state = state.copy(
            checklistItems = state.checklistItems?.toMutableList()?.also {
                it[it.indexOf(checklistItem)] = it[it.indexOf(checklistItem)].copy(
                    isChecked = !it[it.indexOf(checklistItem)].isChecked
                )
            }
        )
        viewModelScope.launch {
            useCases.insertChecklistItem(checklistItem.copy(isChecked = !checklistItem.isChecked))
        }
        calculateCheckCount()
    }


    private fun calculateCheckCount(){
        state.checklistItems?.let { checklistItem ->
            state = state.copy(
                totalCheckCount = checklistItem.size,
                checkCount = checklistItem.filter { it.isChecked }.size
            )
        }

        if(state.checkCount == state.totalCheckCount){
            state = state.copy(
                isChecklistCompeted = true
            )
        }
    }

    private fun sendUiEvent(uiEvent: UiEvent){
        viewModelScope.launch() {
            _uiEvent.send(uiEvent)
        }
    }
}