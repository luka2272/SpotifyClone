package com.crexative.spotifyclone.presentation.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crexative.spotifyclone.core.Constants
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpotifyViewModel @Inject constructor() : ViewModel() {

    private val _spotifyAppRemote = MutableLiveData<SpotifyAppRemote?>()
    val spotifyAppRemote: LiveData<SpotifyAppRemote?> get() = _spotifyAppRemote

    fun setSpotifyAppRemote(appRemote: SpotifyAppRemote) {
        _spotifyAppRemote.value = appRemote
    }

    fun connect(context: Context, onConnected: () -> Unit, onFailure: (Throwable) -> Unit) {
        val connectionParams = ConnectionParams.Builder(Constants.CLIENT_ID)
            .setRedirectUri(Constants.REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                _spotifyAppRemote.value = appRemote
                Log.d("SpotifyViewModel", "Connected! Yay!")
                onConnected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyViewModel", throwable.message, throwable)
                onFailure(throwable)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        _spotifyAppRemote.value?.let { SpotifyAppRemote.disconnect(it) }
    }
}
