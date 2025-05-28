package com.miempresa.totalhealth.injuryreport

import com.google.firebase.firestore.FirebaseFirestore

object InjuryReportRepository {
    fun saveInjuryReport(
        userId: String,
        userName: String,
        userEmail: String,
        zona: String,
        gravedad: String,
        descripcion: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val report = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "userEmail" to userEmail,
            "zona" to zona,
            "gravedad" to gravedad,
            "descripcion" to descripcion,
            "fecha" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance()
            .collection("injury_reports")
            .add(report)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error desconocido") }
    }

    fun deleteInjuryReportById(
        reportId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("injury_reports")
            .document(reportId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al eliminar el reporte") }
    }
}