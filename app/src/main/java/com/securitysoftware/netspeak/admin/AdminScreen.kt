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
import com.securitysoftware.netspeak.data.model.Device
import com.securitysoftware.netspeak.data.model.DeviceType
import com.securitysoftware.netspeak.data.repository.BranchRepository



@Composable
fun AdminScreen() {

    val context = LocalContext.current
    val repository = remember { BranchRepository(context) }

    var branches by remember { mutableStateOf(repository.getAllBranches()) }
    var selectedBranch by remember { mutableStateOf<Branch?>(null) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showEditDeviceDialog by remember { mutableStateOf(false) }

    val deviceTypes = listOf(
        DeviceType(1, "DVR"),
        DeviceType(2, "PANEL"),
        DeviceType(3, "ACCESS")
    )


    var showAddBranchDialog by remember { mutableStateOf(false) }
    var showEditBranchDialog by remember { mutableStateOf(false) }

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
                    onClick = {
                        selectedBranch = branch
                        devices = repository.getDevicesByBranch(branch.id)
                    }
                )
            }
        }

        if (selectedBranch != null) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dispositivos – ${selectedBranch!!.name}",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(devices) { device ->
                    DeviceItem(
                        device = device,
                        isSelected = device.id == selectedDevice?.id,
                        onClick = { selectedDevice = device }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedBranch != null,
                onClick = {
                    showAddDeviceDialog = true
                }
            ) {
                Text("Agregar Device")
            }


            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedDevice != null,
                onClick = {
                    showEditDeviceDialog = true
                }
            ) {
                Text("Editar Device")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedDevice != null,
                onClick = {
                    repository.deleteDevice(selectedDevice!!.id)
                    devices = repository.getDevicesByBranch(selectedBranch!!.id)
                    selectedDevice = null
                }
            ) { Text("Eliminar Device") }
        }

        if (showAddDeviceDialog && selectedBranch != null) {
            DeviceDialog(
                title = "Agregar dispositivo",
                deviceTypes = deviceTypes,
                onConfirm = { name, ip, typeId ->
                    repository.addDevice(
                        branchId = selectedBranch!!.id,
                        name = name,
                        ip = ip,
                        typeId = typeId
                    )
                    devices = repository.getDevicesByBranch(selectedBranch!!.id)
                    showAddDeviceDialog = false
                },
                onDismiss = { showAddDeviceDialog = false }
            )
        }

        if (showEditDeviceDialog && selectedDevice != null) {
            DeviceDialog(
                title = "Editar dispositivo",
                device = selectedDevice,
                deviceTypes = deviceTypes,
                onConfirm = { name, ip, typeId ->
                    repository.updateDevice(
                        deviceId = selectedDevice!!.id,
                        name = name,
                        ip = ip,
                        typeId = typeId
                    )
                    devices = repository.getDevicesByBranch(selectedBranch!!.id)
                    showEditDeviceDialog = false
                    selectedDevice = null
                },
                onDismiss = { showEditDeviceDialog = false }
            )
        }



        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    showAddBranchDialog = true
                }
            ) {
                Text("Agregar")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedBranch != null,
                onClick = {
                    showEditBranchDialog = true
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

    if (showAddBranchDialog) {
        BranchDialog(
            title = "Agregar sucursal",
            onConfirm = { name ->
                repository.addBranch(name)
                branches = repository.getAllBranches()
                showAddBranchDialog = false
            },
            onDismiss = { showAddBranchDialog = false }
        )
    }

    if (showEditBranchDialog && selectedBranch != null) {
        BranchDialog(
            title = "Editar sucursal",
            initialName = selectedBranch!!.name,
            onConfirm = { name ->
                repository.updateBranch(selectedBranch!!.id, name)
                branches = repository.getAllBranches()
                showEditBranchDialog = false
                selectedBranch = null
            },
            onDismiss = { showEditBranchDialog = false }
        )
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
