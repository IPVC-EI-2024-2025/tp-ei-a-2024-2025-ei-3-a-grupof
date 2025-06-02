package dev.jalves.estg.trabalhopratico.ui.views

import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.ui.components.PlaceholderProfilePic
import dev.jalves.estg.trabalhopratico.ui.components.UserRole
import dev.jalves.estg.trabalhopratico.ui.components.UserRoleBadge
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

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

    val profile by profileViewModel.profile.collectAsState()
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(profile?.uid) {
        profile?.let {
            displayName = it.displayName
            username = it.username
            email = it.email
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Profile")},
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
                PlaceholderProfilePic(name = displayName, size = 100.dp)
                UserRoleBadge(UserRole.entries.toTypedArray().random())

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display name") },
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
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

                                    UserService.updateUserInfo(UpdateUserDTO(
                                        id = supabase.auth.currentUserOrNull()!!.id,
                                        if(displayName != profile!!.displayName) displayName else null,
                                        if(email != profile!!.email) email else null,
                                        if(username != profile!!.username) username else null,
                                        if(password.isNotEmpty()) password else null
                                    ))

                                    profileViewModel.fetchData()

                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Failed to update user", Toast.LENGTH_SHORT).show()
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    ) {
                        if (loading) CircularProgressIndicator() else Text("Submit")
                    }
                }
            }


        }
    }
}