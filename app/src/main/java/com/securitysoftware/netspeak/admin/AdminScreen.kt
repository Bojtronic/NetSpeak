package com.securitysoftware.netspeak.admin

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.securitysoftware.netspeak.MainActivity
import com.securitysoftware.netspeak.data.export.DbExporter
import com.securitysoftware.netspeak.data.export.DbImporter
import com.securitysoftware.netspeak.data.model.Branch
import com.securitysoftware.netspeak.data.model.Device
import com.securitysoftware.netspeak.data.model.DeviceType
import com.securitysoftware.netspeak.data.repository.BranchRepository
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp



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
    var exportedFile by remember { mutableStateOf<java.io.File?>(null) }

    var deviceTypes by remember { mutableStateOf<List<DeviceType>>(emptyList()) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showAddBranchDialog by remember { mutableStateOf(false) }
    var showEditBranchDialog by remember { mutableStateOf(false) }


    var selectedDeviceType by remember { mutableStateOf<DeviceType?>(null) }
    var showAddDeviceTypeDialog by remember { mutableStateOf(false) }
    var showEditDeviceTypeDialog by remember { mutableStateOf(false) }
    var showDeleteDeviceTypeConfirm by remember { mutableStateOf(false) }

    val cardHeight = 260.dp

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            DbExporter(context).exportToUri(it)

            Toast.makeText(
                context,
                "Archivo guardado en Descargas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            DbImporter(context).importFromUri(uri)
            branches = repository.getAllBranches()
            selectedBranch = null
            devices = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        deviceTypes = repository.getAllDeviceTypes()
    }


    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar sesión")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {

        Button(
            modifier = Modifier.weight(1f),
            onClick = {
                showExportDialog = true
            }
        ) {
            Text("Exportar DB")
        }

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Exportar base de datos") },
                text = { Text("¿Qué deseas hacer con el archivo?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false

                            // 👉 COMPARTIR
                            val file = DbExporter(context).exportToJson()

                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(
                                Intent.createChooser(intent, "Compartir base de datos")
                            )
                        }
                    ) {
                        Text("Compartir")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false

                            // 👉 GUARDAR EN DESCARGAS
                            exportLauncher.launch("netspeak_backup.json")
                        }
                    ) {
                        Text("Guardar en el dispositivo")
                    }
                }
            )
        }



        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                val success = DbImporter(context).importFromUri(uri)
                if (success) {
                    branches = repository.getAllBranches()
                    selectedBranch = null
                    devices = emptyList()
                }
            }
        }

        Button(
            modifier = Modifier.weight(1f),
            onClick = {
                launcher.launch(arrayOf("application/json"))
            }
        ) {
            Text("Importar DB")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 96.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // 🔽 FILA PRINCIPAL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🟦 TIPOS DE DISPOSITIVO
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Tipos de Dispositivo", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(deviceTypes) { type ->
                            ListItem(
                                headlineContent = { Text(type.name) },
                                modifier = Modifier.clickable { selectedDeviceType = type }
                            )
                        }
                    }
                }
            }

            // 🟩 SUCURSALES
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Sucursales", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(branches) { branch ->
                            BranchItem(
                                branch = branch,
                                isSelected = branch.id == selectedBranch?.id,
                                onClick = {
                                    selectedBranch = branch
                                    devices = repository.getDevicesByBranch(branch.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 🔽 DISPOSITIVOS DE LA SUCURSAL
        if (selectedBranch != null) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dispositivos – ${selectedBranch!!.name}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(8.dp)
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
        }



    Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {


            Button(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                onClick = { showAddDeviceTypeDialog = true }
            ) {
                Text(
                    text = "Agregar Tipo",
                    fontSize = 12.sp,
                    maxLines = 2,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedDeviceType != null,
                onClick = { showEditDeviceTypeDialog = true }
            ) {
                Text("Editar Tipo")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedDeviceType != null,
                onClick = { showDeleteDeviceTypeConfirm = true }
            ) {
                Text("Eliminar Tipo")
            }
        }

        if (showAddDeviceTypeDialog) {
            DeviceTypeDialog(
                title = "Agregar tipo de dispositivo",
                onConfirm = { name ->
                    repository.addDeviceType(name)

                    // 🔁 refrescar lista
                    deviceTypes = repository.getAllDeviceTypes()

                    showAddDeviceTypeDialog = false
                },
                onDismiss = {
                    showAddDeviceTypeDialog = false
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedBranch != null,
                onClick = {
                    if (deviceTypes.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Primero debes crear un tipo de dispositivo",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showAddDeviceDialog = true
                    }
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
