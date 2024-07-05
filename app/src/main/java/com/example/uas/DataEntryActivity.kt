package com.example.uas

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.uas.ui.theme.UasTheme
import kotlinx.coroutines.launch
import java.io.File

class DataEntryActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    companion object {
        internal const val CAMERA_PERMISSION_REQUEST_CODE = 101
        internal const val STORAGE_PERMISSION_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getDatabase(this)

        if (!checkPermission(Manifest.permission.CAMERA)) {
            requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE)
        }
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_REQUEST_CODE)
        }

        setContent {
            UasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DataEntryScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSubmit = { project ->
                            lifecycleScope.launch {
                                try {
                                    database.projectDao().insertProject(project)
                                    Toast.makeText(this@DataEntryActivity, "Project Saved", Toast.LENGTH_SHORT).show()
                                    setResult(RESULT_OK) // Set the result to OK
                                    finish() // Finish the activity to return to MainActivity1
                                } catch (e: Exception) {
                                    Toast.makeText(this@DataEntryActivity, "Error Saving Project: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(
    modifier: Modifier = Modifier,
    onSubmit: (Project) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }
    var anggota by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var finalNotes by remember { mutableStateOf("") }

    var showMemberDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Selesai", "Berlangsung", "Gagal")

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Text(
                    text = "Profil Projek",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(text = "Nama Proyek :")
                TextField(value = projectName, onValueChange = { projectName = it }, modifier = Modifier.fillMaxWidth())
            }

            item {
                Text(text = "Tujuan :")
                TextField(value = goal, onValueChange = { goal = it }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            }

            item {
                Text(text = "Tanggal Mulai :")
                TextField(value = startDate, onValueChange = { startDate = it }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            item {
                Text(text = "Tanggal Selesai :")
                TextField(value = endDate, onValueChange = { endDate = it }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            item {
                Text(text = "Supervisor :")
                TextField(value = supervisor, onValueChange = { supervisor = it }, modifier = Modifier.fillMaxWidth())
            }

            item {
                Text(text = "Anggota :")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = anggota,
                        onValueChange = { anggota = it },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showMemberDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Member")
                    }
                }
            }

            item {
                Text(text = "Status Penyelesaian :")
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    TextField(
                        value = status,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Pilih Status") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        statusOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    status = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(text = "Catatan Akhir Kegiatan :")
                TextField(value = finalNotes, onValueChange = { finalNotes = it }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val project = Project(
                        name = projectName,
                        tujuan = goal,
                        startDate = startDate,
                        endDate = endDate,
                        supervisor = supervisor,
                        anggota = anggota,
                        status = status,
                        notes = finalNotes
                    )
                    onSubmit(project)
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Submit")
                }
            }
        }

        if (showMemberDialog) {
            AddMemberDialog(onDismiss = { showMemberDialog = false }) { memberName ->
                anggota += if (anggota.isEmpty()) memberName else "\n$memberName"
                showMemberDialog = false
            }
        }
    }
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onMemberAdded: (String) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var tempatTanggalLahir by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var jenisKelamin by remember { mutableStateOf("") }
    var keahlian by remember { mutableStateOf("") }
    var peran by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val launcherGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profileImageUri = uri
    }

    val photoFile: File = remember {
        File(context.getExternalFilesDir(null), "profile_photo.jpg")
    }

    val photoUri: Uri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    val launcherCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            profileImageUri = photoUri
        }
    }

    LaunchedEffect(profileImageUri) {
        profileImageUri?.let {
            Toast.makeText(context, "Photo updated", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Profil Anggota") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    if (profileImageUri != null) {
                        val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, profileImageUri!!))
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, profileImageUri)
                        }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Member Photo",
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Member Photo",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcherGallery.launch("image/*") }) {
                    Text("Upload Foto")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        launcherCamera.launch(photoUri)
                    } else {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.CAMERA),
                            DataEntryActivity.CAMERA_PERMISSION_REQUEST_CODE
                        )
                    }
                }) {
                    Text("Take Foto")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = tempatTanggalLahir, onValueChange = { tempatTanggalLahir = it }, label = { Text("Tempat/Tanggal Lahir") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = alamat, onValueChange = { alamat = it }, label = { Text("Alamat") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = jenisKelamin, onValueChange = { jenisKelamin = it }, label = { Text("Jenis Kelamin") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = keahlian, onValueChange = { keahlian = it }, label = { Text("Keahlian") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = peran, onValueChange = { peran = it }, label = { Text("Peran") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onMemberAdded(nama)
            }) {
                Text("Tambah")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DataEntryScreenPreview() {
    UasTheme {
        DataEntryScreen(onSubmit = {})
    }
}
