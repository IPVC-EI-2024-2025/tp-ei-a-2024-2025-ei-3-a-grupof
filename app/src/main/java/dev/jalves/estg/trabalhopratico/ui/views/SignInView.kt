package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jalves.estg.trabalhopratico.services.AuthService
import kotlinx.coroutines.launch

@Composable
fun SignIn(
    onRegisterButtonClicked: () -> Unit,
    onSuccessfulSignIn: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Projetos Asdrúbal",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Preencha o seu username e password para continuar",
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Palavra-passe") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                onClick = {
                    if(isLoading || email.isEmpty() || password.isEmpty())
                        return@Button

                    isLoading = true

                    scope.launch {
                        try {
                            val result = AuthService.signIn(context, email, password)

                            if(result.isSuccess) {
                                onSuccessfulSignIn()
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Entrar")
                }
            }

            TextButton(
                modifier = Modifier.height(36.dp),
                onClick = {
                    onRegisterButtonClicked()
                }
            ) {
                Text("Criar conta")
            }
        }
    }
}