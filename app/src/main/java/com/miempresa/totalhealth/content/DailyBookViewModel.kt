package com.miempresa.totalhealth.content

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

// Estado para la UI del libro
sealed class DailyBookUiState {
    object Loading : DailyBookUiState()
    data class Success(val book: DailyBook) : DailyBookUiState()
    object Error : DailyBookUiState()
    object Empty : DailyBookUiState() // Para cuando no hay libros
}

// Lista de libros predefinidos.
private val bookRecommendationsList: List<DailyBook> = listOf(
    DailyBook(id="B001", title = "El Poder del Ahora", author = "Eckhart Tolle", description = "Una guía para la iluminación espiritual y vivir plenamente en el presente.", genre = "Autoayuda", publicationDate = "1997", imageUrl = "https://covers.openlibrary.org/b/isbn/9780340733509-L.jpg"),
    DailyBook(id="B002", title = "Hábitos Atómicos", author = "James Clear", description = "Un método sencillo y probado para desarrollar buenos hábitos y romper los malos, logrando resultados notables.", genre = "Desarrollo Personal", publicationDate = "2018", imageUrl = "https://covers.openlibrary.org/b/isbn/9780735211292-L.jpg"),
    DailyBook(id="B003", title = "Sapiens: De animales a dioses", author = "Yuval Noah Harari", description = "Una breve y fascinante historia de la humanidad, desde nuestros orígenes hasta el presente.", genre = "Historia", publicationDate = "2011", imageUrl = "https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg"),
    DailyBook(id="B004", title = "El Hombre en Busca de Sentido", author = "Viktor Frankl", description = "La experiencia de un psiquiatra en los campos de concentración nazis y su búsqueda de significado.", genre = "Psicología", publicationDate = "1946", imageUrl = "https://covers.openlibrary.org/b/isbn/9780807014271-L.jpg"),
    DailyBook(id="B005", title = "Pensar rápido, pensar despacio", author = "Daniel Kahneman", description = "Explora los dos sistemas que modelan nuestro pensamiento y cómo afectan nuestras decisiones.", genre = "Psicología", publicationDate = "2011", imageUrl = "https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg"),
    DailyBook(id="B006", title = "Mindset: La actitud del éxito", author = "Carol S. Dweck", description = "Descubre cómo nuestra mentalidad (fija o de crecimiento) influye en todos los aspectos de nuestra vida.", genre = "Psicología", publicationDate = "2006", imageUrl = "https://covers.openlibrary.org/b/isbn/9780345472328-L.jpg"),
    DailyBook(id="B007", title = "Los 7 hábitos de la gente altamente efectiva", author = "Stephen R. Covey", description = "Un enfoque basado en principios para resolver problemas personales y profesionales.", genre = "Desarrollo Personal", publicationDate = "1989", imageUrl = "https://covers.openlibrary.org/b/isbn/9780743269513-L.jpg"),
    DailyBook(id="B008", title = "El Alquimista", author = "Paulo Coelho", description = "Una inspiradora fábula sobre seguir tus sueños y encontrar tu leyenda personal.", genre = "Ficción Alegórica", publicationDate = "1988", imageUrl = "https://covers.openlibrary.org/b/isbn/9780061122415-L.jpg"),
    DailyBook(id="B009", title = "1984", author = "George Orwell", description = "Una inquietante novela distópica sobre un futuro totalitario y la vigilancia masiva.", genre = "Ciencia Ficción Distópica", publicationDate = "1949", imageUrl = "https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg"),
    DailyBook(id="B010", title = "Cien años de soledad", author = "Gabriel García Márquez", description = "La épica historia de la familia Buendía a lo largo de siete generaciones en el mítico Macondo.", genre = "Realismo Mágico", publicationDate = "1967", imageUrl = "https://covers.openlibrary.org/b/isbn/9780060883287-L.jpg"),
    DailyBook(id="B011", title = "El Principito", author = "Antoine de Saint-Exupéry", description = "Una poética historia sobre la amistad, el amor, la pérdida y el sentido de la vida.", genre = "Fábula Filosófica", publicationDate = "1943", imageUrl = "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg"),
    DailyBook(id="B012", title = "Meditaciones", author = "Marco Aurelio", description = "Reflexiones personales del emperador romano sobre la filosofía estoica y el arte de vivir.", genre = "Filosofía", publicationDate = "c. 170-180 d.C.", imageUrl = "https://covers.openlibrary.org/b/isbn/9780140449334-L.jpg")
)

class DailyBookViewModel : ViewModel() {

    // Corrección: Usar MutableStateFlow
    private val _uiState = MutableStateFlow<DailyBookUiState>(DailyBookUiState.Loading)
    // Corrección: Exponer como StateFlow inmutable
    val uiState: StateFlow<DailyBookUiState> = _uiState.asStateFlow()

    init {
        loadBookOfTheMonth()
    }

    fun loadBookOfTheMonth() {
        viewModelScope.launch {
            _uiState.value = DailyBookUiState.Loading
            try {
                delay(350) // Simular carga de red/DB

                if (bookRecommendationsList.isEmpty()) {
                    _uiState.value = DailyBookUiState.Empty
                    Log.w("DailyBookVM", "La lista de libros está vacía.")
                    return@launch
                }

                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) // 0 (Enero) a 11 (Diciembre)

                // Algoritmo para seleccionar un libro basado en el mes actual
                val yearBase = 2024 // Un año base para el cálculo del seed
                val seed = (currentYear - yearBase) * 12 + currentMonth // Un seed único por mes

                val bookIndex = seed % bookRecommendationsList.size
                // Asegurar que el índice final sea positivo
                val finalIndex = if (bookIndex < 0) bookIndex + bookRecommendationsList.size else bookIndex


                if (finalIndex >= 0 && finalIndex < bookRecommendationsList.size) {
                    val selectedBook = bookRecommendationsList[finalIndex].copy(
                        id = "book_${currentYear}_${currentMonth + 1}", // ID único para el libro del mes
                        featuredAt = Date() // Fecha en que se destaca
                    )
                    _uiState.value = DailyBookUiState.Success(selectedBook)
                    Log.d("DailyBookVM", "Libro del mes cargado (Índice $finalIndex): ${selectedBook.title}")
                } else {
                    // Este caso no debería ocurrir si la lógica del índice es correcta y la lista no está vacía
                    Log.e("DailyBookVM", "Índice de libro fuera de rango: $finalIndex. Tamaño lista: ${bookRecommendationsList.size}")
                    _uiState.value = DailyBookUiState.Error
                }

            } catch (e: Exception) {
                Log.e("DailyBookVM", "Error al cargar el libro del mes", e)
                _uiState.value = DailyBookUiState.Error
            }
        }
    }
}
