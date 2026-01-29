package com.securitysoftware.netspeak.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.securitysoftware.netspeak.data.model.Branch
import com.securitysoftware.netspeak.data.repository.BranchRepository

@Composable
fun AdminScreen() {

    val context = LocalContext.current
    val repository = remember { BranchRepository(context) }

    var branches by remember { mutableStateOf(repository.getAllBranches()) }
    var selectedBranch by remember { mutableStateOf<Branch?>(null) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Administración de Sucursales",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(branches) { branch ->
                BranchItem(
                    branch = branch,
                    isSelected = false,
                    onClick = { selectedBranch = branch }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    // TODO agregar sucursal
                }
            ) {
                Text("Agregar")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedBranch != null,
                onClick = {
                    // TODO editar sucursal
                }
            ) {
                Text("Editar")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedBranch != null,
                onClick = {
                    showConfirmDelete = true
                }
            ) {
                Text("Eliminar")
            }
        }
    }

    if (showConfirmDelete && selectedBranch != null) {
        ConfirmDialog(
            message = "¿Eliminar la sucursal '${selectedBranch!!.name}'?",
            onConfirm = {
                repository.deleteBranch(selectedBranch!!.id)
                branches = repository.getAllBranches()
                selectedBranch = null
                showConfirmDelete = false
            },
            onCancel = {
                showConfirmDelete = false
            }
        )
    }
}
