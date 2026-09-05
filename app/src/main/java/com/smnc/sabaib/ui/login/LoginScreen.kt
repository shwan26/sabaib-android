package com.smnc.sabaib.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiError
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiNavy
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import androidx.compose.runtime.collectAsState as collectAsStateSafe

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateSafe()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SabaiNavy,
        unfocusedBorderColor = SabaiLightGray,
        focusedContainerColor = SabaiWhite,
        unfocusedContainerColor = SabaiWhite
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SabaiWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(R.drawable.penguin_jump),
            contentDescription = "SabaiB penguin mascot",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SabaiB",
            color = SabaiYellow,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isSignUpMode) "Create your account" else "Log into your account",
            color = SabaiBlack,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Email", color = SabaiBlack, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Password", color = SabaiBlack, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!isSignUpMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onForgotPassword) {
                    Text(text = "Forgot password?", color = SabaiGray, fontSize = 13.sp)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator(color = SabaiYellow)
        } else {
            Button(
                onClick = {
                    if (isSignUpMode) {
                        viewModel.signUp(email, password)
                    } else {
                        viewModel.signIn(email, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SabaiYellow,
                    contentColor = SabaiBlack
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isSignUpMode) "Sign Up" else "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = SabaiError,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isSignUpMode) "Already have an account! " else "Don't have account! ",
                color = SabaiBlack,
                fontSize = 14.sp
            )
            TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                Text(
                    text = if (isSignUpMode) "Log in" else "Create account",
                    color = SabaiNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
