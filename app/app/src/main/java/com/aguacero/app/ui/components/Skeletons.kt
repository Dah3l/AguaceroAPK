package com.aguacero.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Efecto shimmer para skeletons. */
@Composable
fun shimmerModifier(): Modifier {
    val transicion = rememberInfiniteTransition(label = "shimmer")
    val alpha by transicion.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    return Modifier.background(Color.Gray.copy(alpha = alpha * 0.3f))
}

/** Skeleton de una tarjeta de evento. */
@Composable
fun SkeletonTarjeta(modificador: Modifier = Modifier) {
    Card(
        modifier = modificador.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .then(shimmerModifier())
            )
            Row(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(shimmerModifier())
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(22.dp)
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .then(shimmerModifier())
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .then(shimmerModifier())
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}

/** Lista de skeletons para la pantalla de inicio. */
@Composable
fun ListaSkeletons(cantidad: Int = 3) {
    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
    ) {
        repeat(cantidad) {
            SkeletonTarjeta()
        }
    }
}
