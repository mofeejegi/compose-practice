package com.raywenderlich.android.librarian.ui.composeUi

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.raywenderlich.android.librarian.R

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    isEnabled: Boolean,
    enabledColor: Color = colorResource(id = R.color.colorPrimary),
    disabledTextColor: Color = Color.Gray,
    onClick: () -> Unit
) {
    val backgroundColor = if (isEnabled) enabledColor else Color.LightGray

    TextButton(
        shape = RoundedCornerShape(16.dp),
        enabled = isEnabled,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = backgroundColor,
            contentColor = Color.White,
            disabledContentColor = disabledTextColor
        ),
        modifier = modifier.padding(16.dp),
        onClick = onClick
    ) {
        Text(text = text)
    }
}
