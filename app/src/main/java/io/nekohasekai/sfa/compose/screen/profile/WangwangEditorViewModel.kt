package io.nekohasekai.sfa.compose.screen.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.database.Profile
import io.nekohasekai.sfa.database.ProfileManager
import io.nekohasekai.sfa.database.TypedProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class WangwangEditorUiState(
    val isLoading: Boolean = true,
    val server: String = "",
    val serverPort: String = "",
    val password: String = "",
    val method: String = "",
    val originalServer: String = "",
    val originalServerPort: String = "",
    val originalPassword: String = "",
    val originalMethod: String = "",
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    val showSaveSuccessMessage: Boolean = false,
    val errorMessage: String? = null,
    val profileName: String = "",
)

class WangwangEditorViewModel(
    application: Application,
    private val profileId: Long,
    initialProfileName: String = "",
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WangwangEditorUiState(profileName = initialProfileName))
    val uiState: StateFlow<WangwangEditorUiState> = _uiState.asStateFlow()

    private var profile: Profile? = null

    fun loadConfiguration() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loadedProfile = ProfileManager.get(profileId) ?: throw IllegalArgumentException("Profile not found")
                if (loadedProfile.typed.type != TypedProfile.Type.Local) {
                    throw IllegalArgumentException(getApplication<Application>().getString(R.string.wangwang_editor_local_only))
                }
                profile = loadedProfile
                val wangwang = readWangwangConfig(loadedProfile)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileName = loadedProfile.name,
                        server = wangwang.server,
                        serverPort = wangwang.serverPort,
                        password = wangwang.password,
                        method = wangwang.method,
                        originalServer = wangwang.server,
                        originalServerPort = wangwang.serverPort,
                        originalPassword = wangwang.password,
                        originalMethod = wangwang.method,
                        hasChanges = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: getApplication<Application>().getString(R.string.failed_read_configuration, "unknown"),
                    )
                }
            }
        }
    }

    fun updateServer(value: String) = updateField { copy(server = value) }

    fun updateServerPort(value: String) = updateField { copy(serverPort = value.filter(Char::isDigit)) }

    fun updatePassword(value: String) = updateField { copy(password = value) }

    fun updateMethod(value: String) = updateField { copy(method = value) }

    fun saveConfiguration() {
        val current = _uiState.value
        val currentProfile = profile ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true) }
            try {
                validate(current)
                val configFile = File(currentProfile.typed.path)
                val root = JSONObject(configFile.readText())
                val outbounds = root.optJSONArray("outbounds")
                    ?: throw IllegalArgumentException(getApplication<Application>().getString(R.string.wangwang_editor_missing_outbound))
                val outbound = findWangwangOutbound(outbounds)
                    ?: throw IllegalArgumentException(getApplication<Application>().getString(R.string.wangwang_editor_missing_outbound))

                outbound.put("server", current.server.trim())
                outbound.put("server_port", current.serverPort.toInt())
                outbound.put("password", current.password)
                outbound.put("method", current.method.trim())

                val updatedContent = root.toString(2)
                Libbox.checkConfig(updatedContent)
                configFile.writeText(updatedContent)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        server = current.server.trim(),
                        serverPort = current.serverPort,
                        password = current.password,
                        method = current.method.trim(),
                        originalServer = current.server.trim(),
                        originalServerPort = current.serverPort,
                        originalPassword = current.password,
                        originalMethod = current.method.trim(),
                        hasChanges = false,
                        showSaveSuccessMessage = true,
                    )
                }
                delay(2000)
                _uiState.update { it.copy(showSaveSuccessMessage = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: getApplication<Application>().getString(R.string.error_title),
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun updateField(transform: WangwangEditorUiState.() -> WangwangEditorUiState) {
        _uiState.update { state ->
            val updated = state.transform()
            updated.copy(hasChanges = updated.hasChanges())
        }
    }

    private fun WangwangEditorUiState.hasChanges(): Boolean = server != originalServer ||
        serverPort != originalServerPort ||
        password != originalPassword ||
        method != originalMethod

    private fun validate(state: WangwangEditorUiState) {
        val application = getApplication<Application>()
        if (state.server.isBlank()) {
            throw IllegalArgumentException(application.getString(R.string.profile_input_required))
        }
        val port = state.serverPort.toIntOrNull()
            ?: throw IllegalArgumentException(application.getString(R.string.wangwang_editor_invalid_port))
        if (port !in 1..65535) {
            throw IllegalArgumentException(application.getString(R.string.wangwang_editor_invalid_port))
        }
        if (state.password.isBlank()) {
            throw IllegalArgumentException(application.getString(R.string.profile_input_required))
        }
        if (state.method.isBlank()) {
            throw IllegalArgumentException(application.getString(R.string.profile_input_required))
        }
    }

    private fun readWangwangConfig(profile: Profile): WangwangConfig {
        val root = JSONObject(File(profile.typed.path).readText())
        val outbounds = root.optJSONArray("outbounds")
            ?: throw IllegalArgumentException(getApplication<Application>().getString(R.string.wangwang_editor_missing_outbound))
        val outbound = findWangwangOutbound(outbounds)
            ?: throw IllegalArgumentException(getApplication<Application>().getString(R.string.wangwang_editor_missing_outbound))
        return WangwangConfig(
            server = outbound.optString("server"),
            serverPort = outbound.optInt("server_port").takeIf { it > 0 }?.toString() ?: "",
            password = outbound.optString("password"),
            method = outbound.optString("method"),
        )
    }

    private fun findWangwangOutbound(outbounds: JSONArray): JSONObject? {
        for (index in 0 until outbounds.length()) {
            val outbound = outbounds.optJSONObject(index) ?: continue
            if (outbound.optString("type") == "wangwang") {
                return outbound
            }
        }
        return null
    }

    private data class WangwangConfig(
        val server: String,
        val serverPort: String,
        val password: String,
        val method: String,
    )
}
