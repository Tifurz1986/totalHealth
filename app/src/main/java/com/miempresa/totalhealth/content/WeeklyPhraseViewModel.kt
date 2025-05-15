package com.miempresa.totalhealth.content

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

// Estado para la UI de la frase semanal
sealed class WeeklyPhraseUiState {
    object Loading : WeeklyPhraseUiState()
    // Corrección: Usar el nombre de clase 'WeeklyPhrase' (sin acentos graves)
    data class Success(val phrase: WeeklyPhrase) : WeeklyPhraseUiState()
    object Error : WeeklyPhraseUiState()
    object Empty : WeeklyPhraseUiState()
}

// Lista de frases motivacionales.
private val motivationalPhrasesList: List<Pair<String, String?>> = listOf(
    Pair("El único modo de hacer un gran trabajo es amar lo que haces.", "Steve Jobs"),
    Pair("La vida es un 10% lo que te ocurre y un 90% cómo reaccionas a ello.", "Charles R. Swindoll"),
    Pair("No importa lo lento que vayas mientras no te detengas.", "Confucio"),
    Pair("El mejor momento para plantar un árbol fue hace 20 años. El segundo mejor momento es ahora.", "Proverbio Chino"),
    Pair("Tu tiempo es limitado, así que no lo malgastes viviendo la vida de otra persona.", "Steve Jobs"),
    Pair("Cree que puedes y estarás a medio camino.", "Theodore Roosevelt"),
    // ... (Asegúrate de que tu lista completa esté aquí) ...
    Pair("La perseverancia es fallar 19 veces y tener éxito en la vigésima.", "Julie Andrews"),
    Pair("Haz de cada día tu obra maestra.", "John Wooden"),
    Pair("La actitud es una pequeña cosa que marca una gran diferencia.", "Winston Churchill"),
    Pair("Todo lo que siempre has querido está al otro lado del miedo.", "George Addair"),
    Pair("El que tiene un porqué para vivir puede soportar casi cualquier cómo.", "Friedrich Nietzsche"),
    Pair("No es la montaña lo que conquistamos, sino a nosotros mismos.", "Sir Edmund Hillary"),
    Pair("La simplicidad es la máxima sofisticación.", "Leonardo da Vinci"),
    Pair("La educación es el arma más poderosa que puedes usar para cambiar el mundo.", "Nelson Mandela"),
    Pair("La única sabiduría verdadera es saber que no sabes nada.", "Sócrates"),
    Pair("La vida es realmente simple, pero insistimos en hacerla complicada.", "Confucio"),
    Pair("Un líder es aquel que conoce el camino, recorre el camino y muestra el camino.", "John C. Maxwell"),
    Pair("La innovación distingue a un líder de un seguidor.", "Steve Jobs"),
    Pair("La forma de empezar es dejar de hablar y empezar a hacer.", "Walt Disney"),
    Pair("No te preocupes por los fracasos, preocúpate por las oportunidades que pierdes cuando ni siquiera lo intentas.", "Jack Canfield"),
    Pair("El éxito suele llegar a aquellos que están demasiado ocupados para buscarlo.", "Henry David Thoreau"),
    Pair("Si no te gusta algo, cámbialo. Si no puedes cambiarlo, cambia tu actitud.", "Maya Angelou"),
    Pair("Lo que obtienes al alcanzar tus metas no es tan importante como en lo que te conviertes al alcanzarlas.", "Zig Ziglar"),
    Pair("Cree en ti mismo y en todo lo que eres. Sabe que hay algo dentro de ti que es más grande que cualquier obstáculo.", "Christian D. Larson"),
    Pair("El único lugar donde el éxito viene antes que el trabajo es en el diccionario.", "Vidal Sassoon"),
    Pair("No tengas miedo de renunciar a lo bueno para ir por lo grandioso.", "John D. Rockefeller"),
    Pair("La oportunidad no ocurre, la creas.", "Chris Grosser"),
    Pair("Intenta no volverte un hombre de éxito, sino un hombre de valor.", "Albert Einstein"),
    Pair("Dos caminos se bifurcaban en un bosque y yo, yo tomé el menos transitado, y eso ha marcado toda la diferencia.", "Robert Frost"),
    Pair("He aprendido que la gente olvidará lo que dijiste, la gente olvidará lo que hiciste, pero la gente nunca olvidará cómo la hiciste sentir.", "Maya Angelou"),
    Pair("Ya sea que pienses que puedes o que no puedes, tienes razón.", "Henry Ford"),
    Pair("La persona que dice que no se puede hacer no debería interrumpir a la persona que lo está haciendo.", "Proverbio Chino"),
    Pair("No hay atascos en la milla extra.", "Roger Staubach"),
    Pair("Es difícil vencer a una persona que nunca se rinde.", "Babe Ruth"),
    Pair("Sueño mis pinturas, y luego pinto mis sueños.", "Vincent Van Gogh"),
    Pair("Los desafíos son los que hacen la vida interesante y superarlos es lo que hace la vida significativa.", "Joshua J. Marine"),
    Pair("Si no construyes tu sueño, alguien más te contratará para ayudarle a construir el suyo.", "Dhirubhai Ambani"),
    Pair("Construye tus propios sueños, o alguien más te contratará para construir los suyos.", "Farrah Gray"),
    Pair("El primer paso hacia el éxito se toma cuando te niegas a ser cautivo del entorno en el que te encuentras por primera vez.", "Mark Caine"),
    Pair("La gente suele decir que la motivación no dura. Bueno, tampoco lo hace el baño, por eso lo recomendamos a diario.", "Zig Ziglar"),
    Pair("Cuando todo parezca ir en tu contra, recuerda que el avión despega contra el viento, no a favor de él.", "Henry Ford"),
    Pair("Es nuestra luz, no nuestra oscuridad, lo que más nos asusta.", "Marianne Williamson"),
    Pair("La pregunta no es quién me va a dejar; es quién me va a detener.", "Ayn Rand"),
    Pair("Gana sin presumir, pierde sin excusas.", "Albert Payson Terhune"),
    Pair("La vida es un lienzo en blanco, y debes pintar tus propios colores.", "Danny Kaye"),
    Pair("No puedes agotar la creatividad. Cuanto más la usas, más tienes.", "Maya Angelou"),
    Pair("Lo que no te mata, te hace más fuerte.", "Friedrich Nietzsche"),
    Pair("La mejor preparación para el mañana es hacer tu mejor esfuerzo hoy.", "H. Jackson Brown, Jr."),
    Pair("La diferencia entre ordinario y extraordinario es ese pequeño extra.", "Jimmy Johnson"),
    Pair("El arte de vivir consiste menos en eliminar nuestros problemas que en crecer con ellos.", "Bernard M. Baruch"),
    Pair("Un hombre creativo está motivado por el deseo de alcanzar, no por el deseo de vencer a otros.", "Ayn Rand"),
    Pair("Si no valoras tu tiempo, tampoco lo harán los demás. Deja de regalar tu tiempo y talentos. Valora lo que sabes y empieza a cobrar por ello.", "Kim Garst"),
    Pair("Un ganador es un soñador que nunca se rinde.", "Nelson Mandela"),
    Pair("La magia es creer en ti mismo. Si puedes hacer eso, puedes hacer que cualquier cosa suceda.", "Johann Wolfgang von Goethe"),
    Pair("Si la oportunidad no llama, construye una puerta.", "Milton Berle"),
    Pair("Nunca confundas una sola derrota con una derrota final.", "F. Scott Fitzgerald"),
    Pair("La vida no se trata de encontrarte a ti mismo. La vida se trata de crearte a ti mismo.", "George Bernard Shaw"),
    Pair("Tu verdadera valía se determina por cuánto más das en valor de lo que recibes en pago.", "Bob Burg"),
    Pair("La motivación es lo que te pone en marcha. El hábito es lo que hace que sigas.", "Jim Ryun"),
    Pair("Sueña en grande y atrévete a fallar.", "Norman Vaughan"),
    Pair("Si puedes soñarlo, puedes hacerlo.", "Walt Disney")
)

class WeeklyPhraseViewModel : ViewModel() {

    private val _uiState = mutableStateOf<WeeklyPhraseUiState>(WeeklyPhraseUiState.Loading)
    val uiState: State<WeeklyPhraseUiState> = _uiState

    init {
        loadWeeklyPhrase()
    }

    fun loadWeeklyPhrase() {
        viewModelScope.launch {
            _uiState.value = WeeklyPhraseUiState.Loading
            try {
                delay(100)

                if (motivationalPhrasesList.isEmpty()) {
                    _uiState.value = WeeklyPhraseUiState.Empty
                    Log.w("WeeklyPhraseVM", "La lista de frases motivacionales está vacía.")
                    return@launch
                }

                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

                val yearBase = 2024
                val weeksInYearApproximation = 53
                val weekSeed = (currentYear - yearBase) * weeksInYearApproximation + currentWeekOfYear

                val phraseIndex = weekSeed % motivationalPhrasesList.size
                val finalIndex = if (phraseIndex < 0) phraseIndex + motivationalPhrasesList.size else phraseIndex


                if (finalIndex >= 0 && finalIndex < motivationalPhrasesList.size) {
                    val selectedPhraseData = motivationalPhrasesList[finalIndex]
                    // Corrección: Usar el nombre de clase 'WeeklyPhrase' (sin acentos graves)
                    val weeklyPhraseObject = WeeklyPhrase(
                        id = "phrase_${currentYear}_$currentWeekOfYear",
                        weekNumber = currentWeekOfYear,
                        phrase = selectedPhraseData.first,
                        author = selectedPhraseData.second,
                        createdAt = Date()
                    )
                    _uiState.value = WeeklyPhraseUiState.Success(weeklyPhraseObject)
                    Log.d("WeeklyPhraseVM", "Semana: $currentWeekOfYear, Índice: $finalIndex, Frase: ${weeklyPhraseObject.phrase}")
                } else {
                    Log.e("WeeklyPhraseVM", "Índice de frase fuera de rango: $finalIndex. Tamaño de lista: ${motivationalPhrasesList.size}")
                    _uiState.value = WeeklyPhraseUiState.Error
                }

            } catch (e: Exception) {
                Log.e("WeeklyPhraseVM", "Error al cargar la frase semanal de la lista local", e)
                _uiState.value = WeeklyPhraseUiState.Error
            }
        }
    }
}