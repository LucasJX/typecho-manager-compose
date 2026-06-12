package com.flypigs.typechomanager.ui.creator

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.remote.CompanionApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatorUiState(
    val isUploading: Boolean = false,
    val uploadResult: String? = null,
    val error: String? = null,
)

@HiltViewModel
class CreatorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val companionApiClient: CompanionApiClient,
    private val configDataStore: ConfigDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatorUiState())
    val uiState: StateFlow<CreatorUiState> = _uiState.asStateFlow()

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            try {
                val config = configDataStore.getConfig()
                val companionBase = config.blogUrl.ifEmpty {
                    config.endpoint.substringBefore("/index.php")
                }.trimEnd('/')

                // 读取文件内容
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("无法读取文件")
                inputStream.close()

                // 获取文件名
                val fileName = getFileName(uri)

                val attachment = companionApiClient.uploadMedia(
                    companionBase = companionBase,
                    username = config.username,
                    password = config.password,
                    fileName = fileName,
                    bytes = bytes,
                )

                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadResult = "上传成功: ${attachment.title}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = "上传失败: ${e.message}"
                )
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(uploadResult = null, error = null)
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "upload_${System.currentTimeMillis()}"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
            }
        }
        return fileName
    }
}
