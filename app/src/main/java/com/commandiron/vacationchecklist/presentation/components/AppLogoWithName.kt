package com.commandiron.vacationchecklist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.commandiron.vacationchecklist.R
import com.commandiron.vacationchecklist.util.LocalSpacing
import com.commandiron.vacationchecklist.util.Strings.APP_NAME_FIRST_WORD

@Composable
fun AppLogoWithName(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppName()
        Spacer(modifier = Modifier.width(spacing.spaceMedium))
        AppLogo()
    }
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
) {
    Icon(
        modifier = modifier.size(size),
        painter = painterResource(id = R.drawable.checkpack_logo),
        contentDescription = null,
        tint = Color.Unspecified
    )
}

@Composable
fun AppName(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiaryContainer
) {
    Text(
        modifier = modifier,
        text = APP_NAME_FIRST_WORD,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = color
    )
}