package dev.jalves.estg.trabalhopratico.ui.views

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.ui.components.ProfilePicture
import dev.jalves.estg.trabalhopratico.ui.components.UserRoleBadge
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

suspend fun fetchProfilePictureUrl(userId: String, pictureSize: Int = 100): String? {
    return try {
        UserService.getProfilePictureURL(
            pictureSize = pictureSize,
            userId = userId
        )
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf<Role?>(null) }

    var profilePicUrl by remember { mutableStateOf<String?>(null) }
    var imageLoadFailed by remember { mutableStateOf(false) }
    var profilePicLoading by remember { mutableStateOf(true) }

    val profile by profileViewModel.profile.collectAsState()
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                UserService.updateUserProfilePicture(uri, context)
                try {
                    UserService.updateUserProfilePicture(it, context)
                    profilePicUrl = fetchProfilePictureUrl(profile!!.id)
                    imageLoadFailed = false
                } catch (_: Exception) {
                    imageLoadFailed = true
                } finally {
                    profilePicLoading = false
                }
            }
        }
    }

    LaunchedEffect(profile?.id) {
        profile?.let {
            displayName = it.displayName
            username = it.username
            email = it.email
            userRole = UserService.getCurrentUserRole()

            profilePicLoading = true
            imageLoadFailed = false

            profilePicUrl = fetchProfilePictureUrl(it.id)

            profilePicLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(stringResource(R.string.profile))},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (profile == null) {
                CircularProgressIndicator()
            } else {
                ProfilePicture(
                    user = profile!!,
                    size = 100.dp,
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    externalProfilePicUrl = profilePicUrl,
                )

                userRole?.let { role ->
                    UserRoleBadge(role)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text(stringResource(R.string.name_setting)) },
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(stringResource(R.string.username)) },
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                    )

                    Button(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {
                            scope.launch {
                                try {
                                    loading = true

                                    val currentProfile = profile
                                    if (currentProfile == null) {
                                        Toast.makeText(context, "Profile not loaded", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }

                                    UserService.updateUserInfo(UpdateUserDTO(
                                        id = supabase.auth.currentUserOrNull()!!.id,
                                        displayName = if(displayName != currentProfile.displayName) displayName else null,
                                        email = if(email != currentProfile.email) email else null,
                                        username = if(username != currentProfile.username) username else null,
                                        password = password.ifEmpty { null }
                                    ))

                                    profileViewModel.fetchData()

                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to update user: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    ) {
                        if (loading) CircularProgressIndicator() else Text(stringResource(R.string.submit))
                    }
                }
            }


        }
    }
}