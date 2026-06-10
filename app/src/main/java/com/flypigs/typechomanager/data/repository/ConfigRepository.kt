package com.flypigs.typechomanager.data.repository

import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.model.BlogConfig

class ConfigRepository(private val configDataStore: ConfigDataStore) {

    suspend fun saveConfig(config: BlogConfig) {
        configDataStore.saveConfig(config)
    }

    suspend fun getConfig(): BlogConfig {
        return configDataStore.getConfig()
    }

    suspend fun clearConfig() {
        configDataStore.clearConfig()
    }

    suspend fun isLoggedIn(): Boolean {
        return configDataStore.isLoggedIn()
    }
}
