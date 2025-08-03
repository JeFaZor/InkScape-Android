package com.example.inkscape.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inkscape.R

data class TattooStyle(
    val name: String,
    val imageRes: Int
)

@Composable
fun StyleGrid(
    onStyleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val styles = listOf(
        TattooStyle("Traditional", R.drawable.tat1),
        TattooStyle("New School", R.drawable.tat2),
        TattooStyle("Japanese", R.drawable.tat3),
        TattooStyle("Fineline", R.drawable.tat4),
        TattooStyle("Geometric", R.drawable.tat5),
        TattooStyle("Micro Realism", R.drawable.tat6),
        TattooStyle("Realism", R.drawable.tat7),
        TattooStyle("Dot Work", R.drawable.tat8),
        TattooStyle("Dark Art", R.drawable.tat9),
        TattooStyle("Flowers", R.drawable.tat10),
        TattooStyle("Surrealism", R.drawable.tat11),
        TattooStyle("Trash Polka", R.drawable.tat12)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.heightIn(max = 600.dp)
    ) {
        items(styles) { style ->
            StyleCard(
                style = style,
                onStyleSelected = onStyleSelected
            )
        }
    }
}

@Composable
fun StyleCard(
    style: TattooStyle,
    onStyleSelected: (String) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable {
                isPressed = !isPressed
                onStyleSelected(style.name)
            }
            .border(
                width = if (isPressed) 2.dp else 1.dp,
                color = if (isPressed) Color(0xFF9C27B0) else Color(0xFF424242),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) Color(0x20FFFFFF) else Color(0x10FFFFFF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF424242)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = style.imageRes),
                    contentDescription = style.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = style.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}