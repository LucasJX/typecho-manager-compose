1|package com.flypigs.typechomanager.ui.attachments
2|
3|import androidx.lifecycle.ViewModel
4|import androidx.lifecycle.viewModelScope
5|import com.flypigs.typechomanager.data.local.ConfigDataStore
6|import com.flypigs.typechomanager.data.model.Attachment
7|import com.flypigs.typechomanager.data.remote.CompanionApiClient
8|import dagger.hilt.android.lifecycle.HiltViewModel
9|import kotlinx.coroutines.flow.MutableStateFlow
10|import kotlinx.coroutines.flow.StateFlow
11|import kotlinx.coroutines.flow.asStateFlow
12|import kotlinx.coroutines.launch
13|import javax.inject.Inject
14|
15|data class AttachmentsUiState(
16|    val attachments: List<Attachment> = emptyList(),
17|    val total: Int = 0,
18|    val page: Int = 1,
19|    val totalPages: Int = 1,
20|    val isLoading: Boolean = false,
21|    val isLoadingMore: Boolean = false,
22|    val isRefreshing: Boolean = false,
23|    val error: String? = null
24|)
25|
26|@HiltViewModel
27|class AttachmentsViewModel @Inject constructor(
28|    private val apiClient: CompanionApiClient,
29|    private val configDataStore: ConfigDataStore
30|) : ViewModel() {
31|
32|    private val _uiState = MutableStateFlow(AttachmentsUiState())
33|    val uiState: StateFlow<AttachmentsUiState> = _uiState.asStateFlow()
34|
35|    companion object {
36|        private const val PAGE_SIZE = 20
37|    }
38|
39|    init {
40|        loadAttachments()
41|    }
42|
43|    /**
44|     * Initial load — fetches the first page of attachments.
45|     */
46|    fun loadAttachments() {
47|        viewModelScope.launch {
48|            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
49|            try {
50|                val config = configDataStore.getConfig()
51|                val companionBase = config.blogUrl.ifEmpty {
52|                    config.endpoint.substringBefore("/index.php")
53|                }.trimEnd('/')
54|                val (items, totalPages) = apiClient.listMedia(
55|                    companionBase = companionBase,
56|                    page = 1,
57|                    pageSize = PAGE_SIZE
58|                )
59|                _uiState.value = _uiState.value.copy(
60|                    attachments = items,
61|                    total = items.size,
62|                    page = 1,
63|                    totalPages = totalPages,
64|                    isLoading = false
65|                )
66|            } catch (e: Exception) {
67|                _uiState.value = _uiState.value.copy(
68|                    isLoading = false,
69|                    error = e.message ?: "Unknown error"
70|                )
71|            }
72|        }
73|    }
74|
75|    /**
76|     * Load the next page of attachments (infinite scroll).
77|     */
78|    fun loadMore() {
79|        val state = _uiState.value
80|        val nextPage = state.page + 1
81|        if (state.isLoadingMore || nextPage > state.totalPages) return
82|
83|        viewModelScope.launch {
84|            _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
85|            try {
86|                val config = configDataStore.getConfig()
87|                val companionBase = config.blogUrl.ifEmpty {
88|                    config.endpoint.substringBefore("/index.php")
89|                }.trimEnd('/')
90|                val (newItems, totalPages) = apiClient.listMedia(
91|                    companionBase = companionBase,
92|                    page = nextPage,
93|                    pageSize = PAGE_SIZE
94|                )
95|                _uiState.value = _uiState.value.copy(
96|                    attachments = state.attachments + newItems,
97|                    total = state.attachments.size + newItems.size,
98|                    page = nextPage,
99|                    totalPages = totalPages,
100|                    isLoadingMore = false
101|                )
102|            } catch (e: Exception) {
103|                _uiState.value = _uiState.value.copy(
104|                    isLoadingMore = false,
105|                    error = e.message ?: "Load more failed"
106|                )
107|            }
108|        }
109|    }
110|
111|    /**
112|     * Delete an attachment by CID and remove it from the local list immediately.
113|     */
114|    fun deleteAttachment(cid: Int) {
115|        viewModelScope.launch {
116|            try {
117|                val config = configDataStore.getConfig()
118|                val companionBase = config.blogUrl.ifEmpty {
119|                    config.endpoint.substringBefore("/index.php")
120|                }.trimEnd('/')
121|                apiClient.deleteMedia(companionBase, config.username, config.password, cid)
122|                val updated = _uiState.value.attachments.filter { it.cid != cid }
123|                _uiState.value = _uiState.value.copy(
124|                    attachments = updated,
125|                    total = updated.size
126|                )
127|            } catch (e: Exception) {
128|                _uiState.value = _uiState.value.copy(
129|                    error = e.message ?: "Delete failed"
130|                )
131|            }
132|        }
133|    }
134|
135|    /**
136|     * Pull-to-refresh — reload from page 1.
137|     */
138|    fun refresh() {
139|        viewModelScope.launch {
140|            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
141|            try {
142|                val config = configDataStore.getConfig()
143|                val companionBase = config.blogUrl.ifEmpty {
144|                    config.endpoint.substringBefore("/index.php")
145|                }.trimEnd('/')
146|                val (items, totalPages) = apiClient.listMedia(
147|                    companionBase = companionBase,
148|                    page = 1,
149|                    pageSize = PAGE_SIZE
150|                )
151|                _uiState.value = _uiState.value.copy(
152|                    attachments = items,
153|                    total = items.size,
154|                    page = 1,
155|                    totalPages = totalPages,
156|                    isRefreshing = false
157|                )
158|            } catch (e: Exception) {
159|                _uiState.value = _uiState.value.copy(
160|                    isRefreshing = false,
161|                    error = e.message ?: "Refresh failed"
162|                )
163|            }
164|        }
165|    }
166|}
167|