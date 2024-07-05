package com.example.uas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.uas.ui.theme.UasTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val database = AppDatabase.getDatabase(this)
            UasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding), database)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, database: AppDatabase) {
    val context = LocalContext.current
    var projects by remember { mutableStateOf(listOf<Project>()) }
    val coroutineScope = rememberCoroutineScope()

    // Function to refresh the projects list
    val refreshProjects: () -> Unit = {
        coroutineScope.launch {
            projects = withContext(Dispatchers.IO) {
                database.projectDao().getAllProjects()
            }
        }
    }

    // Register the activity result launcher
    val dataEntryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshProjects()
        }
    }

    // Refresh projects when the screen is first launched
    LaunchedEffect(Unit) {
        refreshProjects()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Proyek Manajemen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            val intent = Intent(context, DataEntryActivity::class.java)
            dataEntryLauncher.launch(intent)
        }) {
            Text(text = "Create Data")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val file = generatePdf(context, projects)
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        }) {
            Text(text = "Print")
        }
        Spacer(modifier = Modifier.height(32.dp))
        LazyColumn {
            items(projects) { project ->
                var showEditDialog by remember { mutableStateOf(false) }

                if (showEditDialog) {
                    EditProjectDialog(
                        project = project,
                        onDismiss = { showEditDialog = false },
                        onSubmit = { updatedProject ->
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    database.projectDao().updateProject(updatedProject)
                                }
                                refreshProjects()
                                showEditDialog = false
                            }
                        }
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .padding(horizontal = 8.dp)
                        .shadow(1.dp, shape = MaterialTheme.shapes.medium)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(project.name, modifier = Modifier.weight(1f))
                            var showDialog by remember { mutableStateOf(false) }

                            if (showDialog) {
                                ShowProjectDetailsDialog(project = project, onDismiss = { showDialog = false })
                            }

                            IconButton(onClick = { showDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "View Details")
                            }
                            IconButton(onClick = {
                                showEditDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        database.projectDao().deleteProject(project)
                                    }
                                    refreshProjects()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun EditProjectDialog(project: Project, onDismiss: () -> Unit, onSubmit: (Project) -> Unit) {
    var projectName by remember { mutableStateOf(project.name) }
    var goal by remember { mutableStateOf(project.tujuan) }
    var startDate by remember { mutableStateOf(project.startDate) }
    var endDate by remember { mutableStateOf(project.endDate) }
    var supervisor by remember { mutableStateOf(project.supervisor) }
    var anggota by remember { mutableStateOf(project.anggota) }
    var status by remember { mutableStateOf(project.status) }
    var finalNotes by remember { mutableStateOf(project.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val updatedProject = project.copy(
                    name = projectName,
                    tujuan = goal,
                    startDate = startDate,
                    endDate = endDate,
                    supervisor = supervisor,
                    anggota = anggota,
                    status = status,
                    notes = finalNotes
                )
                onSubmit(updatedProject)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Project") },
        text = {
            Column {
                TextField(value = projectName, onValueChange = { projectName = it }, label = { Text("Nama Proyek") })
                TextField(value = goal, onValueChange = { goal = it }, label = { Text("Tujuan") })
                TextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Tanggal Mulai") })
                TextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Tanggal Selesai") })
                TextField(value = supervisor, onValueChange = { supervisor = it }, label = { Text("Supervisor") })
                TextField(value = anggota, onValueChange = { anggota = it }, label = { Text("Anggota") })
                TextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
                TextField(value = finalNotes, onValueChange = { finalNotes = it }, label = { Text("Catatan Akhir") })
            }
        }
    )
}

@Composable
fun ShowProjectDetailsDialog(project: Project, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = project.name) },
        text = {
            Text("Tujuan: ${project.tujuan}\nTanggal Mulai: ${project.startDate}\nTanggal Selesai: ${project.endDate}\nSupervisor: ${project.supervisor}\nAnggota: ${project.anggota}\nStatus: ${project.status}\nCatatan: ${project.notes}")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    UasTheme {
        HomeScreen(database = AppDatabase.getDatabase(LocalContext.current))
    }
}
