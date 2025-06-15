package dev.jalves.estg.trabalhopratico.ui.views

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.services.AuthService
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.ui.components.ProfilePicture
import dev.jalves.estg.trabalhopratico.ui.components.UserRoleBadge
import dev.jalves.estg.trabalhopratico.ui.components.fetchProfilePictureUrl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { isEditing = !isEditing }) {
                        Text(if (isEditing) stringResource(R.string.cancel) else stringResource(R.string.edit))
                    }
                }
            )
        }
    ) { padding ->
        if (!isEditing) {
            NormalProfile(
                profileViewModel = profileViewModel,
                navPadding = padding
            )
        } else {
            EditProfile(
                profileViewModel = profileViewModel,
                navController = navController,
                navPadding = padding,
                onFinish = { isEditing = false }
            )
        }
    }
}

@Composable
fun NormalProfile(
    profileViewModel: ProfileViewModel,
    navPadding: PaddingValues
) {
    val profile by profileViewModel.profile.collectAsState()
    var role by remember { mutableStateOf<Role?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch { role = UserService.getCurrentUserRole() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(navPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        profile?.let { user ->
            ProfilePicture(user = user, size = 100.dp)
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyLarge,
            )
            role?.let { UserRoleBadge(it) }
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Username",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.username),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.email),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(
    profileViewModel: ProfileViewModel,
    navController: NavHostController,
    navPadding: PaddingValues,
    onFinish: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf<Role?>(null) }

    var profilePicUrl by remember { mutableStateOf<String?>(null) }
    var imageLoadFailed by remember { mutableStateOf(false) }
    var profilePicLoading by remember { mutableStateOf(true) }

    val profile by profileViewModel.profile.collectAsState()
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                profilePicLoading = true
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

    val hasProfileChanged = remember(displayName, username, profile) {
        profile?.let { currentProfile ->
            displayName != currentProfile.displayName ||
                    username != currentProfile.username
        } ?: false
    }

    LaunchedEffect(profile?.id) {
        profile?.let {
            displayName = it.displayName
            username = it.username
            userRole = UserService.getCurrentUserRole()
            profilePicUrl = fetchProfilePictureUrl(it.id)
            profilePicLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (profile == null) {
                CircularProgressIndicator()
            } else {
                ProfilePicture(
                    user = profile!!,
                    size = 100.dp,
                    onClick = { imagePickerLauncher.launch("image/*") },
                    externalProfilePicUrl = profilePicUrl
                )
                Spacer(Modifier.height(12.dp))
                userRole?.let { UserRoleBadge(it) }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(stringResource(R.string.name_setting)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                EmailChangeCard(
                    originalEmail = profile!!.email,
                    profileViewModel = profileViewModel,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                PasswordChangeCard()

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            try {
                                val currentProfile = profile!!

                                UserService.updateUserInfo(
                                    UpdateUserDTO(
                                        id = currentProfile.id,
                                        displayName = displayName.takeIf { it != currentProfile.displayName },
                                        email = null,
                                        username = username.takeIf { it != currentProfile.username },
                                        password = null,
                                        status = currentProfile.status
                                    )
                                )
                                profileViewModel.fetchData()
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                onFinish()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to update user: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = hasProfileChanged && !loading
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text(stringResource(R.string.submit))
                }
            }
        }
    }
}

@Composable
fun EmailChangeCard(
    originalEmail: String,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf(originalEmail) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.change_email),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
                )
            }

            if (isExpanded) {
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.current_password)) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (email.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.email_required), Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            if (password.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.password_required), Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            if (email == originalEmail) {
                                Toast.makeText(context, context.getString(R.string.email_must_be_different), Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            isLoading = true
                            try {
                                val result = AuthService.updateEmail(password, email)
                                if (result.isSuccess) {
                                    password = ""
                                    // Refresh the profile data to get the updated email
                                    profileViewModel.fetchData()
                                    Toast.makeText(context, context.getString(R.string.email_updated_success), Toast.LENGTH_SHORT).show()
                                    isExpanded = false
                                } else {
                                    val exception = result.exceptionOrNull()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.failed_update_email, exception?.message ?: ""),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.failed_update_email, e.message ?: ""),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.update_email))
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordChangeCard() {
    var isExpanded by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.change_password),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
                )
            }

            if (isExpanded) {
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(stringResource(R.string.current_password)) },
                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCurrentPassword) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.new_password)) },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNewPassword) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPassword) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (currentPassword.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.current_password_required), Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            if (newPassword.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.new_password_required), Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            if (newPassword != confirmPassword) {
                                Toast.makeText(context, context.getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            isLoading = true
                            try {
                                val result = AuthService.updatePassword(currentPassword, newPassword)
                                if (result.isSuccess) {
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    Toast.makeText(context, context.getString(R.string.password_updated_success), Toast.LENGTH_SHORT).show()
                                    isExpanded = false
                                } else {
                                    val exception = result.exceptionOrNull()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.failed_update_password, exception?.message ?: ""),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.failed_update_password, e.message ?: ""),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.update_password))
                    }
                }
            }
        }
    }
}