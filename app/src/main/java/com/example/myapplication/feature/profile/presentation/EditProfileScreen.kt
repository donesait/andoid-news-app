package com.example.myapplication.feature.profile.presentation

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.di.AppLocator
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@Composable
fun EditProfileRoute(
    onClose: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(factory = AppLocator.get().editProfileViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionList = remember { mediaPermissions() }
    var hasMediaPermission by remember { mutableStateOf(hasRequiredMediaPermissions(context)) }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        hasMediaPermission = granted
        if (!granted) {
            Toast.makeText(context, context.getString(R.string.profile_permission_denied), Toast.LENGTH_LONG).show()
            onClose()
        }
    }

    LaunchedEffect(hasMediaPermission) {
        if (!hasMediaPermission && permissionList.isNotEmpty()) {
            mediaPermissionLauncher.launch(permissionList)
        }
    }

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let { viewModel.onAvatarChange(it.toString()) }
        } else {
            pendingCameraUri?.let { context.contentResolver.delete(it, null, null) }
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.profile_camera_error)) }
        }
        pendingCameraUri = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onAvatarChange(it.toString()) }
    }

    var pendingSaveAfterPermission by remember { mutableStateOf(false) }
    var pendingExactAlarmRequest by remember { mutableStateOf(false) }
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!pendingExactAlarmRequest) return@rememberLauncherForActivityResult
        pendingExactAlarmRequest = false
        if (needsExactAlarmPermission(context, uiState.favoritePairTime)) {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.profile_exact_alarm_warning)) }
        }
        viewModel.saveProfile()
    }
    val continueAfterNotification: () -> Unit = {
        if (needsExactAlarmPermission(context, uiState.favoritePairTime)) {
            pendingExactAlarmRequest = true
            exactAlarmLauncher.launch(createExactAlarmIntent(context))
        } else {
            viewModel.saveProfile()
        }
    }
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!pendingSaveAfterPermission) return@rememberLauncherForActivityResult
        pendingSaveAfterPermission = false
        if (!granted) {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.profile_notification_permission_rationale)) }
        }
        continueAfterNotification()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.Saved -> onClose()
                is EditProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val onPickFromGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    val onPickFromCamera = {
        val uri = createImageUri(context)
        if (uri != null) {
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.profile_camera_error)) }
            Unit
        }
    }

    val onSave = {
        if (needsNotificationPermission(context, uiState.favoritePairTime)) {
            pendingSaveAfterPermission = true
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            continueAfterNotification()
        }
    }

    if (!hasMediaPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    EditProfileScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onFullNameChange = viewModel::onFullNameChange,
        onPositionChange = viewModel::onPositionChange,
        onResumeUrlChange = viewModel::onResumeUrlChange,
        onFavoriteTimeChange = viewModel::onFavoriteTimeChange,
        onAvatarClickGallery = onPickFromGallery,
        onAvatarClickCamera = onPickFromCamera,
        onSaveClick = onSave
    )
}

@Composable
private fun EditProfileScreen(
    uiState: EditProfileUiState,
    snackbarHostState: SnackbarHostState,
    onFullNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onResumeUrlChange: (String) -> Unit,
    onFavoriteTimeChange: (String) -> Unit,
    onAvatarClickGallery: () -> Unit,
    onAvatarClickCamera: () -> Unit,
    onSaveClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showPickerDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.profile_avatar_desc),
                style = MaterialTheme.typography.labelLarge
            )
            AvatarEditable(
                avatarUri = uiState.avatarUri,
                onClick = { showPickerDialog = true }
            )
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                label = { Text(stringResource(id = R.string.profile_full_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            OutlinedTextField(
                value = uiState.position,
                onValueChange = onPositionChange,
                label = { Text(stringResource(id = R.string.profile_position_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            OutlinedTextField(
                value = uiState.resumeUrl,
                onValueChange = onResumeUrlChange,
                label = { Text(stringResource(id = R.string.profile_resume_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            OutlinedTextField(
                value = uiState.favoritePairTime,
                onValueChange = onFavoriteTimeChange,
                label = { Text(stringResource(id = R.string.profile_time_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.showTimeError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = stringResource(id = R.string.profile_time_icon_desc)
                        )
                    }
                },
                supportingText = {
                    if (uiState.showTimeError) {
                        Text(
                            text = stringResource(id = R.string.profile_time_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSaveClick,
                enabled = uiState.isSaveEnabled && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(id = R.string.profile_done))
                }
            }
        }
    }

    if (showPickerDialog) {
        AvatarSourceDialog(
            onDismiss = { showPickerDialog = false },
            onGallery = {
                showPickerDialog = false
                onAvatarClickGallery()
            },
            onCamera = {
                showPickerDialog = false
                onAvatarClickCamera()
            }
        )
    }
    if (showTimePicker) {
        val (initialHour, initialMinute) = remember(uiState.favoritePairTime) {
            parseTimeComponents(uiState.favoritePairTime)
        }
        FavoritePairTimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                onFavoriteTimeChange(it)
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun AvatarEditable(
    avatarUri: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri.isNotBlank()) {
            AsyncImage(
                model = avatarUri,
                contentDescription = stringResource(id = R.string.profile_avatar_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(id = R.string.profile_edit),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun AvatarSourceDialog(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.profile_pick_image_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        onDismiss()
                        onGallery()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.profile_pick_gallery))
                }
                TextButton(
                    onClick = {
                        onDismiss()
                        onCamera()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.profile_pick_camera))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.close))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritePairTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val formatted = "%02d:%02d".format(pickerState.hour, pickerState.minute)
                onConfirm(formatted)
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        text = { TimePicker(state = pickerState) }
    )
}

private fun mediaPermissions(): Array<String> {
    val list = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        list += Manifest.permission.READ_MEDIA_IMAGES
    } else {
        @Suppress("DEPRECATION")
        list += Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            list += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
    }
    list += Manifest.permission.CAMERA
    return list.distinct().toTypedArray()
}

private fun hasRequiredMediaPermissions(context: Context): Boolean =
    mediaPermissions().all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

private fun createImageUri(context: Context): Uri? {
    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
    val imagesDir = File(baseDir, "profile_images")
    if (!imagesDir.exists() && !imagesDir.mkdirs()) {
        return null
    }
    return try {
        val file = File.createTempFile("avatar_", ".jpg", imagesDir)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: IOException) {
        null
    }
}

private fun parseTimeComponents(value: String): Pair<Int, Int> {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hour to minute
}

private fun needsNotificationPermission(context: Context, favoriteTime: String): Boolean {
    if (favoriteTime.isBlank()) return false
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) != PackageManager.PERMISSION_GRANTED
}

private fun needsExactAlarmPermission(context: Context, favoriteTime: String): Boolean {
    if (favoriteTime.isBlank()) return false
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
    return !alarmManager.canScheduleExactAlarms()
}

private fun createExactAlarmIntent(context: Context): Intent =
    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
    }

