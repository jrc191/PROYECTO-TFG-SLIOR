package com.slior.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

// Extensión para acceder al DataStore desde cualquier Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "slior_prefs")

// Interceptor que añade el JWT en el header Authorization de cada petición
@Singleton
class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            context.dataStore.data.first()[TOKEN_KEY]
        }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}