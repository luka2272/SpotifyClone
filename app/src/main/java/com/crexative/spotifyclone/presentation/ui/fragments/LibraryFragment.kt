package com.crexative.spotifyclone.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.crexative.spotifyclone.R
import com.crexative.spotifyclone.databinding.FragmentLibraryBinding
import com.crexative.spotifyclone.presentation.ui.SpotifyViewModel
import com.crexative.spotifyclone.presentation.ui.adapters.PlaylistAdapter
import com.crexative.spotifyclone.presentation.viewmodels.UserViewModel
import com.spotify.android.appremote.api.SpotifyAppRemote
import dagger.hilt.android.AndroidEntryPoint

private val TAG: String = LibraryFragment::class.java.simpleName

@AndroidEntryPoint
class LibraryFragment : Fragment(R.layout.fragment_library) {

    private val viewModel: SpotifyViewModel by activityViewModels()
    private lateinit var binding: FragmentLibraryBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var playLisAdapter: PlaylistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLibraryBinding.bind(view)

        observePlayLists()
        setupRecycler()
    }

    private fun setupRecycler() = with(binding) {
        rvPlaylist.apply {
            playLisAdapter = PlaylistAdapter()
            adapter = playLisAdapter
        }

        playLisAdapter.setOnItemClickListener { item ->
            Log.e(TAG, "setupView: $item")
            viewModel.spotifyAppRemote.observe(viewLifecycleOwner) { spotifyAppRemote ->
                if (spotifyAppRemote != null) {
                    // Now you can interact with SpotifyAppRemote
                    spotifyAppRemote.playerApi.play(item.uri)
                        .setResultCallback {
                            Log.d("LibraryFragment", "Playlist is playing")
                        }
                        .setErrorCallback {
                            Log.e("LibraryFragment", "Error playing playlist", it)
                        }
                } else {
                    // Handle the case where spotifyAppRemote is not connected
                }
            }
        }
    }

    private fun observePlayLists() {
        userViewModel.state.observe(viewLifecycleOwner) { result ->
            binding.progressBar.isVisible = result.isLoading
            playLisAdapter.items = result.playlist
        }
    }
}