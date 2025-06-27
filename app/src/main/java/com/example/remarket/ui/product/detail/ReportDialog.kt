package com.example.remarket.ui.product.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportDialog(
    isReporting: Boolean,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    val reasons = listOf(
        "Contenido inapropiado",
        "Producto falso",
        "Precio sospechoso",
        "Descripción engañosa",
        "Otro"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Producto") },
        text = {
            Column {
                Text("Selecciona la razón del reporte:")
                Spacer(modifier = Modifier.height(8.dp))
                reasons.forEach { reason ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (reason == selectedReason),
                                onClick = { selectedReason = reason }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (reason == selectedReason),
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reason)
                    }
                }
            }
        },
        confirmButton = {
            if (isReporting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    onClick = { onReport(selectedReason) },
                    enabled = selectedReason.isNotEmpty()
                ) {
                    Text("Reportar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isReporting
            ) {
                Text("Cancelar")
            }
        }
    )
}
