package com.crexative.spotifyclone.presentation.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.crexative.spotifyclone.R
import com.crexative.spotifyclone.databinding.FragmentLoginBinding
import com.crexative.spotifyclone.presentation.ui.SpotifyViewModel
import com.google.android.material.snackbar.Snackbar

private val TAG: String = LoginFragment::class.java.simpleName

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var navController: NavController
    private val viewModel: SpotifyViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        handleLoginClick()
    }

    private fun handleLoginClick() {
        binding.btnLogin.setOnClickListener {
            connectToSpotify()
        }

        binding.btnCreateAccount.setOnClickListener {
            Snackbar.make(
                requireView(),
                getString(R.string.error_function_not_implemented),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun connectToSpotify() {
        viewModel.connect(requireContext(), {
            // Handle successful connection to Spotify
            navigateMainFragment()
        }, { throwable: Throwable ->
            // Handle connection failure
            Log.e(TAG, "Connection failed", throwable)
        })
    }

    private fun navigateMainFragment() {
        try {
            navController.navigate(R.id.action_loginFragment_to_homeScreenFragment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
