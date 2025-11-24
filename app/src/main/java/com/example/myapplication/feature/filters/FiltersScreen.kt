@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.feature.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Public
import androidx.compose.foundation.clickable

@Composable
fun FiltersScreen(navController: NavController? = null, vm: FiltersViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Фильтры", modifier = Modifier.align(Alignment.Start))
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.searchIn,
                    onValueChange = vm::updateSearchIn,
                    label = { Text("Где искать (title, description, content)") },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                val langs = listOf("ru" to "Русский", "en" to "Английский", "de" to "Немецкий", "fr" to "Французский", "es" to "Испанский")
                val currentLangName = langs.firstOrNull { it.first == state.language }?.second ?: state.language
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    TextField(
                        readOnly = true,
                        value = currentLangName,
                        onValueChange = {},
                        label = { Text("Язык новостей") },
                        leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        langs.forEach { (code, name) ->
                            DropdownMenuItem(text = { Text(name) }, onClick = {
                                vm.updateLanguage(code)
                                expanded = false
                            })
                        }
                    }
                }

                var showDatePicker by remember { mutableStateOf(false) }
                val dateState = rememberDatePickerState()
                val formatter = remember { DateTimeFormatter.ISO_DATE.withZone(ZoneOffset.UTC) }
                OutlinedTextField(
                    value = state.minDateIso,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("С даты") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Выбрать дату")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
                if (showDatePicker) {
                    DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
                        Button(onClick = {
                            val millis = dateState.selectedDateMillis
                            if (millis != null) vm.updateMinDate(formatter.format(Instant.ofEpochMilli(millis)))
                            showDatePicker = false
                        }) { Text("Ок") }
                    }) {
                        DatePicker(state = dateState)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.save { navController?.navigateUp() } }) { Text("Готово") }
            Button(onClick = { vm.reset { } }) { Text("Сбросить") }
        }
    }
}


