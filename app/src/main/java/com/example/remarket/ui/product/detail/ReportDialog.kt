// Añade estos imports:
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Y pega este bloque tras tus otros @Composable:

@Composable
fun ReportDialog(
    isReporting: Boolean,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    var customComment  by remember { mutableStateOf("") }        // ⬅️ nuevo state
    val reasons = listOf("Contenido inapropiado","Producto falso",
        "Precio sospechoso","Descripción engañosa","Otro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Producto") },
        text = {
            Column {
                Text("Selecciona la razón del reporte:")
                Spacer(Modifier.height(8.dp))

                reasons.forEach { reason ->
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(
                                selected = reason == selectedReason,
                                onClick = { selectedReason = reason }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reason == selectedReason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(reason)
                    }
                }

                // ⬇️ Si elige “Otro”, aparece un campo para escribir
                if (selectedReason == "Otro") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customComment,
                        onValueChange = { customComment = it },
                        label = { Text("Describe el problema") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (isReporting) {
                CircularProgressIndicator(Modifier.size(24.dp))
            } else {
                val finalReason = if (selectedReason == "Otro") customComment else selectedReason
                TextButton(
                    onClick = { onReport(finalReason) },
                    enabled = finalReason.isNotBlank()
                ) { Text("Reportar") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isReporting) { Text("Cancelar") }
        }
    )
}
