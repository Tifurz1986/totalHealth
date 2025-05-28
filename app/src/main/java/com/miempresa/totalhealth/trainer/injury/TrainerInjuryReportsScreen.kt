package com.miempresa.totalhealth.trainer.injury

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrainerInjuryReportsScreen() {
    var reports by remember { mutableStateOf<List<InjuryReport>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Carga los reportes de Firestore al abrir la pantalla
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        loading = true
        db.collection("injury_reports")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    reports = emptyList()
                    loading = false
                    return@addOnSuccessListener
                }
                val tempReports = mutableListOf<InjuryReport>()
                var completed = 0
                val total = snapshot.size()
                snapshot.documents.forEach { doc ->
                    val userId = doc.getString("userId")
                    val area = doc.getString("affectedArea") ?: "Desconocida"
                    val severity = doc.getString("severity") ?: "Sin gravedad"
                    val description = doc.getString("description") ?: ""
                    val timestamp = doc.getString("timestamp") ?: ""
                    if (userId != null) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userSnap ->
                                val name = userSnap.getString("name")
                                val email = userSnap.getString("email")
                                tempReports.add(
                                    InjuryReport(
                                        userName = name ?: email ?: "Sin usuario",
                                        area = area,
                                        severity = severity,
                                        description = description,
                                        timestamp = timestamp
                                    )
                                )
                                completed++
                                if (completed == total) {
                                    reports = tempReports.toList()
                                    loading = false
                                }
                            }
                            .addOnFailureListener {
                                tempReports.add(
                                    InjuryReport(
                                        userName = "Sin usuario",
                                        area = area,
                                        severity = severity,
                                        description = description,
                                        timestamp = timestamp
                                    )
                                )
                                completed++
                                if (completed == total) {
                                    reports = tempReports.toList()
                                    loading = false
                                }
                            }
                    } else {
                        val userName = doc.getString("userName") ?: doc.getString("userEmail") ?: "Sin usuario"
                        tempReports.add(
                            InjuryReport(
                                userName = userName,
                                area = area,
                                severity = severity,
                                description = description,
                                timestamp = timestamp
                            )
                        )
                        completed++
                        if (completed == total) {
                            reports = tempReports.toList()
                            loading = false
                        }
                    }
                }
            }
            .addOnFailureListener {
                error = "Error al cargar lesiones: ${it.localizedMessage}"
                loading = false
            }
    }

    // Fondo degradado oscuro más marcado y margen superior aumentado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0C0F), Color(0xFF1A1D22))
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Barra superior con botón de retroceso a la izquierda y título centrado, más grande y destacado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { backDispatcher?.onBackPressed() },
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFFFFD700))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Lesiones",
                    color = Color(0xFFFFD700),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(3f)
                        .shadow(1.dp),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFFD700))
                    }
                }
                error != null -> {
                    Text(
                        text = error ?: "",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                reports.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay lesiones reportadas.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(22.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(reports) { report ->
                            InjuryReportCardPro(report)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InjuryReportCardPro(report: InjuryReport) {
    val gold = Color(0xFFFFD700)
    val premiumBlue = Color(0xFF3A6EA5)
    val cardBg = Color(0xFF2C3038)
    val textWhite = Color(0xFFF0F0F0)
    val textGray = Color(0xFFB0B8C1)
    val severityTextColor = if (report.severity.lowercase().contains("grave")) Color(0xFFFF4C4C) else gold
    val severityIconColor = severityTextColor

    // Formateo bonito de fecha si viene en formato epoch/string larga
    val dateText = remember(report.timestamp) {
        if (report.timestamp.length > 8 && report.timestamp.all { it.isDigit() }) {
            try {
                val date = Date(report.timestamp.toLong())
                SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es", "ES")).format(date)
            } catch (_: Exception) { report.timestamp }
        } else report.timestamp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E24)),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = premiumBlue)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Usuario: ${report.userName}",
                    color = textWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = premiumBlue)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Zona: ${report.area}",
                    color = gold,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = severityIconColor)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Gravedad: ${report.severity}",
                    color = severityTextColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            if (report.description.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "📝 Descripción: ${report.description}",
                    color = textWhite,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = gold)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Fecha: $dateText",
                    color = textWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // Botón de eliminar
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("injury_reports")
                        .whereEqualTo("timestamp", report.timestamp)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            for (doc in snapshot.documents) {
                                doc.reference.delete()
                            }
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Eliminar", color = Color.White)
            }
        }
    }
}

// Modelo de datos simple para lesiones (ajusta según tus campos en Firestore)
data class InjuryReport(
    val userName: String,
    val area: String,
    val severity: String,
    val description: String,
    val timestamp: String
)