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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton





@Composable
fun AdminScreen() {

    // ─────────────────────────────────────────
    // CONTEXT & REPOSITORY
    // ─────────────────────────────────────────
    val context = LocalContext.current
    val repository = remember { BranchRepository(context) }

    // ─────────────────────────────────────────
    // STATE
    // ─────────────────────────────────────────
    var branches by remember { mutableStateOf(repository.getAllBranches()) }
    var selectedBranch by remember { mutableStateOf<Branch?>(null) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showEditDeviceDialog by remember { mutableStateOf(false) }

    var deviceTypes by remember { mutableStateOf<List<DeviceType>>(emptyList()) }
    var selectedDeviceType by remember { mutableStateOf<DeviceType?>(null) }
    var showAddDeviceTypeDialog by remember { mutableStateOf(false) }
    var showEditDeviceTypeDialog by remember { mutableStateOf(false) }
    var showDeleteDeviceTypeConfirm by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showAddBranchDialog by remember { mutableStateOf(false) }
    var showEditBranchDialog by remember { mutableStateOf(false) }
    var showDeleteDeviceConfirm by remember { mutableStateOf(false) }

    var exportedFile by remember { mutableStateOf<java.io.File?>(null) }

    val cardHeight = 260.dp

    // ─────────────────────────────────────────
    // LAUNCHERS & EFFECTS
    // ─────────────────────────────────────────
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

    // ─────────────────────────────────────────
    // TOP ACTIONS (Cerrar sesión)
    // ─────────────────────────────────────────
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Cerrar sesión",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // ─────────────────────────────────────────
    // TOP ACTIONS (Importar / Exportar DB)
    // ─────────────────────────────────────────
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {

        Button(
            modifier = Modifier.weight(1f),
            onClick = { showExportDialog = true }
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
                    ) { Text("Compartir") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportLauncher.launch("netspeak_backup.json")
                        }
                    ) { Text("Guardar en el dispositivo") }
                }
            )
        }

        Button(
            modifier = Modifier.weight(1f),
            onClick = { importLauncher.launch(arrayOf("application/json")) }
        ) {
            Text("Importar DB")
        }
    }

    // ─────────────────────────────────────────
    // MAIN CONTENT (Cards, Lists)
    // ─────────────────────────────────────────
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
                    Text(
                        text = "Tipos de Dispositivo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(Modifier.fillMaxHeight()) {
                        items(deviceTypes) { type ->
                            DeviceTypeItem(
                                deviceType = type,
                                isSelected = type.id == selectedDeviceType?.id,
                                onClick = {
                                    selectedDeviceType = type
                                }
                            )
                        }
                    }
                }
            }


            // 🟩 SUCURSALES
            Card(
                modifier = Modifier.weight(1f).height(cardHeight),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = "Sucursales",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(8.dp))
                    LazyColumn(Modifier.fillMaxHeight()) {
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

        // 🔽 DISPOSITIVOS
        Spacer(Modifier.height(16.dp))

        Text(
            text = if (selectedBranch != null)
                "Dispositivos – ${selectedBranch!!.name}"
            else
                "Dispositivos",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        Card(elevation = CardDefaults.cardElevation(2.dp)) {

            if (selectedBranch == null) {

                // 💤 Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecciona una sucursal para ver los dispositivos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            } else {

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

        Spacer(modifier = Modifier.height(12.dp))

        // ─────────────────────────────────────────
        // ACTION BUTTONS (CRUD)
        // ─────────────────────────────────────────
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
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }

            Button(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                enabled = selectedDeviceType != null,
                onClick = { showEditDeviceTypeDialog = true }
            ) {
                Text("Editar Tipo",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }

            Button(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                enabled = selectedDeviceType != null,
                onClick = { showDeleteDeviceTypeConfirm = true }
            ) {
                Text("Eliminar Tipo",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
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
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
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
                Text("Agregar Device",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                enabled = selectedDevice != null,
                onClick = {
                    showEditDeviceDialog = true
                }
            ) {
                Text("Editar Device",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                enabled = selectedDevice != null,
                onClick = {
                    showDeleteDeviceConfirm = true
                }
            ) {
                Text(
                    "Eliminar Device",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
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
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                onClick = {
                    showAddBranchDialog = true
                }
            ) {
                Text("Agregar sucursal",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                enabled = selectedBranch != null,
                onClick = {
                    showEditBranchDialog = true
                }
            ) {
                Text("Editar sucursal",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                enabled = selectedBranch != null,
                onClick = {
                    showConfirmDelete = true
                }
            ) {
                Text("Eliminar sucursal",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }

    }

    // ─────────────────────────────────────────
    // DIALOGS
    // ─────────────────────────────────────────
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

    if (showEditDeviceTypeDialog && selectedDeviceType != null) {
        DeviceTypeDialog(
            title = "Editar tipo de dispositivo",
            initialName = selectedDeviceType!!.name,
            onConfirm = { newName ->
                repository.updateDeviceType(
                    id = selectedDeviceType!!.id,
                    name = newName
                )

                // 🔁 refrescar lista
                deviceTypes = repository.getAllDeviceTypes()

                showEditDeviceTypeDialog = false
                selectedDeviceType = null
            },
            onDismiss = {
                showEditDeviceTypeDialog = false
            }
        )
    }

    if (showDeleteDeviceTypeConfirm && selectedDeviceType != null) {

        val context = LocalContext.current

        ConfirmDialog(
            message = "¿Eliminar el tipo de dispositivo '${selectedDeviceType!!.name}'?",
            onConfirm = {

                val deleted = repository.deleteDeviceType(selectedDeviceType!!.id)

                if (!deleted) {
                    Toast.makeText(
                        context,
                        "No se puede eliminar: hay dispositivos usando este tipo",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // 🔁 refrescar lista SOLO si se eliminó
                    deviceTypes = repository.getAllDeviceTypes()
                    selectedDeviceType = null
                }

                // cerrar diálogo siempre
                showDeleteDeviceTypeConfirm = false
            },
            onCancel = {
                showDeleteDeviceTypeConfirm = false
            }
        )
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

    if (showDeleteDeviceConfirm && selectedDevice != null) {
        ConfirmDialog(
            message = "¿Eliminar el dispositivo '${selectedDevice!!.name}'?",
            onConfirm = {
                repository.deleteDevice(selectedDevice!!.id)

                // 🔁 refrescar lista
                devices = repository.getDevicesByBranch(selectedBranch!!.id)

                selectedDevice = null
                showDeleteDeviceConfirm = false
            },
            onCancel = {
                showDeleteDeviceConfirm = false
            }
        )
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







