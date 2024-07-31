package com.crexative.spotifyclone.presentation.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.crexative.spotifyclone.R
import com.crexative.spotifyclone.core.AppPreferences
import com.crexative.spotifyclone.core.Constants
import com.crexative.spotifyclone.databinding.FragmentLoginBinding
import com.crexative.spotifyclone.presentation.ui.SpotifyViewModel
import com.google.android.material.snackbar.Snackbar
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.security.MessageDigest
import java.security.SecureRandom


private val TAG: String = LoginFragment::class.java.simpleName

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var navController: NavController
    private val CLIENT_ID = Constants.CLIENT_ID
    private val REDIRECT_URI = Constants.REDIRECT_URI
    private val CODE_VERIFIER: String by lazy { getCodeVerifier() }
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
            // Start the authentication process
            connectToSpotify()
            showLoginActivityCode.launch(getLoginActivityCodeIntent())
        }

        binding.btnCreateAccount.setOnClickListener {
            Snackbar.make(
                requireView(),
                getString(R.string.error_function_not_implemented),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun getCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val code = ByteArray(64)
        secureRandom.nextBytes(code)
        return Base64.encodeToString(
            code,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    private fun getCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(bytes, 0, bytes.size)
        val digest = messageDigest.digest()
        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    private fun getLoginActivityCodeIntent(): Intent =
        AuthorizationClient.createLoginActivityIntent(
            requireActivity(),
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.CODE, REDIRECT_URI)
                .setScopes(
                    arrayOf(
                        "user-library-read", "user-library-modify",
                        "app-remote-control", "user-read-currently-playing"
                    )
                )
                .setCustomParam("code_challenge_method", "S256")
                .setCustomParam("code_challenge", getCodeChallenge(CODE_VERIFIER))
                .build()
        )

    private fun getLoginActivityTokenIntent(code: String): Intent =
        AuthorizationClient.createLoginActivityIntent(
            requireActivity(),
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
                .setCustomParam("grant_type", "authorization_code")
                .setCustomParam("code", code)
                .setCustomParam("code_verifier", CODE_VERIFIER)
                .build()
        )

    private val showLoginActivityCode: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val authorizationResponse = AuthorizationClient.getResponse(result.resultCode, result.data)
            when (authorizationResponse.type) {
                AuthorizationResponse.Type.CODE -> {
                    val authorizationCode = authorizationResponse.code
                    Log.d(TAG, "Authorization Code: $authorizationCode")
                    if (authorizationCode != null) {
                        showLoginActivityToken.launch(getLoginActivityTokenIntent(authorizationCode))
                    }
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "Authorization Error: ${authorizationResponse.error}")
                }
                else -> {
                    Log.e(TAG, "Authorization Response: ${authorizationResponse.code} ${authorizationResponse.error}")
                }
            }
        }

    private val showLoginActivityToken: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val authorizationResponse = AuthorizationClient.getResponse(result.resultCode, result.data)
            when (authorizationResponse.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    AppPreferences.token = authorizationResponse.accessToken
                    Log.d(TAG, "Access Token: ${authorizationResponse.accessToken}")
                    // Navigate to home screen or handle success
                    navigateMainFragment()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "Token Error: ${authorizationResponse.error}")
                }
                else -> {
                    Log.e(TAG, "Token Response: ${authorizationResponse.code} ${authorizationResponse.error}")
                }
            }
        }

    private fun navigateMainFragment() {
        try {
            navController.navigate(R.id.action_loginFragment_to_homeScreenFragment)
        } catch (e: Exception) {
            e.printStackTrace()
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

}
