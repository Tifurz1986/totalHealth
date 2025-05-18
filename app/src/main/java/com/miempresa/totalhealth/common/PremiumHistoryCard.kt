package com.miempresa.totalhealth.ui.common // Asegúrate que este es el paquete

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.miempresa.totalhealth.R // Importa tu R para el placeholder
// Importaciones correctas de tus colores desde el paquete theme
import com.miempresa.totalhealth.ui.theme.PremiumBorderGold
import com.miempresa.totalhealth.ui.theme.PremiumDarkCharcoal
import com.miempresa.totalhealth.ui.theme.PremiumGold
import com.miempresa.totalhealth.ui.theme.PremiumTextGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PremiumHistoryCard(
    modifier: Modifier = Modifier,
    date: Long,
    title: String?,
    description: String,
    photoUrl: String? = null
) {
    val formattedDate = try {
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(date))
    } catch (e: Exception) {
        "Fecha inválida"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = PremiumDarkCharcoal, // Fondo de la card
            contentColor = PremiumTextGold    // Color de texto por defecto en la card
        ),
        border = BorderStroke(1.dp, PremiumBorderGold)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formattedDate,
                fontSize = 13.sp,
                color = PremiumGold, // Dorado brillante para la fecha
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            title?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PremiumGold, // Dorado brillante para el título
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = description,
                fontSize = 15.sp,
                color = Color(0xFFE0E0E0), // Gris muy claro para descripción sobre fondo oscuro
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = if (photoUrl != null) 12.dp else 0.dp)
            )
            photoUrl?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_default_profile_placeholder)
                        .error(R.drawable.ic_default_profile_placeholder)
                        .build(),
                    contentDescription = "Foto del reporte",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 250.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
