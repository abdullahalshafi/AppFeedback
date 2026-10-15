package com.shafi.app_feedback.compose_ui.feedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shafi.app_feedback.api.ApiClient
import com.shafi.app_feedback.api.Resource
import com.shafi.app_feedback.repository.MainRepository
import com.shafi.app_feedback.util.ApiFailure
import com.shafi.app_feedback.util.AppConstants.API_KEY
import com.shafi.app_feedback.util.AppConstants.BASE_URL
import com.shafi.app_feedback.util.AppConstants.FEEDBACK_MAX_LENGTH
import com.shafi.app_feedback.util.AppConstants.FEEDBACK_TEXT
import com.shafi.app_feedback.util.AppConstants.USER_ID
import com.shafi.app_feedback.util.AppConstants.USER_JSON
import com.shafi.app_feedback.util.AppUtil
import com.shafi.app_feedback.util.FormFieldType
import com.shafi.app_feedback.util.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Shafi on 14/10/2026.
 */
internal class FeedbackViewModel(
    private val repository: MainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    //intent extras land in the SavedStateHandle, so these survive process death too
    private val apiKey: String = savedStateHandle[API_KEY] ?: ""
    private val userJson: String = savedStateHandle[USER_JSON] ?: "{}"
    private val userId: String = savedStateHandle[USER_ID] ?: ""

    private val _state: MutableStateFlow<FeedbackState> = MutableStateFlow(
        FeedbackState(feedbackInfo = FeedbackInfo(feedback = FormFieldType(savedStateHandle[FEEDBACK_TEXT] ?: "")))
    )
    val state = _state.asStateFlow()

    fun onAction(action: FeedbackAction) {

        when (action) {

            is FeedbackAction.OnFormValueChange<*> -> {
                updateField(action.fieldType, action.data)
            }

            is FeedbackAction.OnSubmitClicked -> {
                validateAndSubmit()
            }
        }
    }

    private fun <T> updateField(fieldType: FeedbackFieldType, newValue: T) {

        when (fieldType) {

            FeedbackFieldType.FEEDBACK -> {
                //cut rather than rejected, so a long paste still keeps its first part
                val value = (newValue as? String ?: return).take(FEEDBACK_MAX_LENGTH)
                savedStateHandle[FEEDBACK_TEXT] = value
                _state.update { current ->
                    current.copy(
                        apiFailure = null,
                        apiErrorMessage = null,
                        feedbackInfo = current.feedbackInfo.copy(
                            feedback = current.feedbackInfo.feedback.copy(value = value, error = null)
                        )
                    )
                }
            }
        }
    }

    private fun validateAndSubmit() {

        if (state.value.isSubmitApiLoading || state.value.submitSuccessMessage != null) return

        val info = state.value.feedbackInfo

        if (info.feedback.value.isBlank()) {
            _state.update { current ->
                current.copy(
                    feedbackInfo = current.feedbackInfo.copy(
                        feedback = current.feedbackInfo.feedback.copy(error = "Please write your feedback")
                    ),
                    validationErrorMessage = UiMessage("Please write your feedback")
                )
            }
            return
        }

        _state.update { it.copy(isSubmitApiLoading = true, apiFailure = null, apiErrorMessage = null) }

        viewModelScope.launch {

            val feedbackUploadModel = Gson().fromJson(userJson, JsonObject::class.java).apply {
                addProperty("feedback", info.feedback.value.trim())
            }

            when (val result = repository.repoSubmitFeedback(apiKey, feedbackUploadModel)) {

                is Resource.Success -> {
                    //the submit response shape is not confirmed yet, so only an explicit error status fails it
                    if (result.response.status != "error") {
                        AppUtil.saveLastFeedbackAt(userId, System.currentTimeMillis())
                        _state.update {
                            it.copy(
                                isSubmitApiLoading = false,
                                submitSuccessMessage = UiMessage(result.response.message ?: "")
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isSubmitApiLoading = false,
                                apiErrorMessage = UiMessage(result.response.message ?: "Something went wrong")
                            )
                        }
                    }
                }

                is Resource.Failure -> {
                    _state.update { it.copy(isSubmitApiLoading = false, apiFailure = ApiFailure(result)) }
                }

                is Resource.DBFailure -> {
                    _state.update { it.copy(isSubmitApiLoading = false) }
                }
            }
        }
    }

    companion object {

        //no Hilt in the library (see ApiClient), so the ViewModel gets its repository here
        val Factory = viewModelFactory {
            initializer {
                val application = requireNotNull(this[APPLICATION_KEY]) { "Application missing from CreationExtras" }
                val savedStateHandle = createSavedStateHandle()
                AppUtil.initHawk(application)
                FeedbackViewModel(
                    repository = MainRepository(
                        ApiClient.getApiInterface(application, savedStateHandle[BASE_URL] ?: "")
                    ),
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
