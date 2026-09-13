package com.example.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Authentication:
 * 1. Google Sign-In (using Web Client ID)
 * 2. Firebase Anonymous Login (converts guest into real Firebase UID)
 * 3. Email & Password Login / Registration
 */
object AuthManager {
    const val WEB_CLIENT_ID = "362682080745-o3jl218s1elvsfqmgqsmjnqq4s9ggenb.apps.googleusercontent.com"
    const val SHA1_FINGERPRINT = "D5:46:35:23:2E:16:A3:06:A1:B0:F7:F3:31:B4:4D:75:C3:58:9E:2B"
    const val SHA256_FINGERPRINT = "05:5F:B7:AF:F0:4E:53:DD:79:A4:FE:95:AA:08:92:5E:0B:CE:80:6D:A1:5C:BB:FF:51:59:0B:A3:13:D5:58:E8"

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun getRuntimeSha1(context: Context): String {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            }
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray() ?: return SHA1_FINGERPRINT
            val md = java.security.MessageDigest.getInstance("SHA-1")
            val digest = md.digest(cert)
            digest.joinToString(":") { String.format("%02X", it) }
        } catch (_: Exception) {
            SHA1_FINGERPRINT
        }
    }

    fun getWebClientId(context: Context): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            try {
                val str = context.getString(resId)
                if (str.isNotBlank()) return str
            } catch (_: Exception) {}
        }
        val resIdExample = context.resources.getIdentifier("default_web_client_id", "string", "com.example")
        if (resIdExample != 0) {
            try {
                val str = context.getString(resIdExample)
                if (str.isNotBlank()) return str
            } catch (_: Exception) {}
        }
        return WEB_CLIENT_ID
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val clientId = getWebClientId(context)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun signInWithGoogleCredential(account: GoogleSignInAccount): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("Firebase is not initialized"))
        return try {
            val idToken = account.idToken ?: return Result.failure(Exception("Missing Google ID Token"))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(Exception("Firebase user is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("Firebase is not initialized"))
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: return Result.failure(Exception("Firebase user is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("Firebase is not initialized"))
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return Result.failure(Exception("User not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("Firebase is not initialized"))
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return Result.failure(Exception("Registration failed"))
            if (name.isNotBlank()) {
                val updateReq = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(updateReq).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(context: Context) {
        try {
            auth?.signOut()
            getGoogleSignInClient(context).signOut()
        } catch (_: Exception) {}
    }
}
