package com.miempresa.totalhealth.trainer.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.miempresa.totalhealth.trainer.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppointmentsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    /**
     * Obtiene las citas para un día concreto (por fecha yyyy-MM-dd).
     */
    fun fetchAppointmentsForDay(selectedDate: LocalDate) {
        val dateStr = selectedDate.format(DateTimeFormatter.ISO_DATE)
        db.collection("appointments")
            .get()
            .addOnSuccessListener { result ->
                val appointments = result.documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                }.filter { appointment ->
                    appointment.timestamp.startsWith(dateStr)
                }
                _appointments.value = appointments
            }
    }

    /**
     * Obtiene todas las citas de un usuario concreto (por userId).
     */
    fun fetchAppointmentsForUser(userId: String) {
        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val appointments = result.documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                }
                _appointments.value = appointments
            }
    }

    /**
     * Obtiene todas las citas de un entrenador concreto (por trainerId y día).
     */
    fun fetchAppointmentsForTrainerDay(trainerId: String, selectedDate: LocalDate) {
        val dateStr = selectedDate.format(DateTimeFormatter.ISO_DATE)
        db.collection("appointments")
            .whereEqualTo("trainerId", trainerId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val appointments = result.documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                }.filter { appointment ->
                    appointment.timestamp.startsWith(dateStr)
                }

                val userIds = appointments.map { it.userId }.distinct()
                if (userIds.isEmpty()) {
                    _appointments.value = appointments
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), userIds)
                    .get()
                    .addOnSuccessListener { userResult ->
                        val userIdToNameMap = userResult.documents.associate { doc ->
                            val id = doc.id
                            val name = doc.getString("name") ?: ""
                            val surname = doc.getString("surname") ?: ""
                            val fullName = "$name $surname".trim()
                            val email = doc.getString("email") ?: ""
                            id to when {
                                fullName.isNotBlank() -> fullName
                                email.isNotBlank() -> email
                                else -> "Usuario desconocido"
                            }
                        }
                        val enrichedAppointments = appointments.map { appointment ->
                            appointment.copy(userName = userIdToNameMap[appointment.userId])
                        }
                        _appointments.value = enrichedAppointments
                    }
                    .addOnFailureListener {
                        _appointments.value = appointments
                    }
            }
    }

    /**
     * Crea una cita nueva en Firestore.
     */
    fun createAppointment(appointment: Appointment, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        db.collection("appointments")
            .add(appointment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}