package com.example.ataraxia.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaTextButton
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun WelcomeSetupScreen(
    onGetStarted: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = AtaraxiaTheme.spacing.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Spa,
            contentDescription = "Lotus Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
        
        Text(
            text = "Welcome to Ataraxia",
            style = MaterialTheme.typography.displayLarge,
            color = DesignTokens.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
        
        Text(
            text = "A quiet space for your mind. What should we call you?",
            style = MaterialTheme.typography.bodyLarge,
            color = DesignTokens.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
        
        PrimaryTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Your name",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
        
        AtaraxiaPrimaryButton(
            text = "Let's Begin",
            onClick = {
                if (name.isNotBlank()) {
                    onGetStarted(name.trim())
                }
            },
            enabled = name.isNotBlank()
        )

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

        AtaraxiaTextButton(
            text = "Skip",
            onClick = {
                onGetStarted("Guest")
            }
        )
    }
}
