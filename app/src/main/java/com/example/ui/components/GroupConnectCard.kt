package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralRedLoss
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun GroupConnectCard(
    currentRoomInput: String,
    onRoomInputChange: (String) -> Unit,
    onConnectGroup: (groupCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val czechAnimalsList = remember {
        listOf(
            "KOČKY", "SLONI", "KOSATKY", "TYGŘI", "ORLI", "DELFÍNI", "PANDY", "VLCI",
            "LIŠKY", "MEDVĚDI", "SOVY", "GEOPARDI", "ŽABY", "KLOKANI", "VYDRY", "ŽRALOCI",
            "TUČŇÁCI", "VEVERKY", "LVI", "SOBÍCI", "KAMZÍCI", "SRNCI", "JEZEVCI", "HAVRANI",
            "TULEŇI", "HROŠI", "NOSOROŽCI", "SÝKORKY", "SOKOLI", "JESTŘÁBI", "PANTEŘI", "JAGUÁŘI",
            "ŠAKALI", "PUMY", "RYSI", "JELENI", "BOBŘI", "PAPOUŠCI", "KORMORÁNI", "PELIKÁNI",
            "PLAMEŇÁCI", "JEŽCI", "KRTCI", "VLAŠTOVKY", "KANÁRCI", "SÝKORY", "SVIŠTI", "SURIKATY",
            "KROKODÝLI", "ALIGÁTOŘI", "LEOPARDI", "KOALY", "KLOKÁNCI", "LEMURI", "HYENY", "ZEBRY",
            "ŽIRAFY", "CHAMELEÓNI", "MANTY", "GORILY", "ŠIMPANZI", "MANDRILI", "TAPÍŘI", "MRAVENČÍCI",
            "KONDORI", "KALONI", "ALBATROSY", "TUKANI", "KOLIBŘÍCI", "DANĚCI", "MUFLONI", "NETOPÝŘI",
            "DELFÍNCI", "MEDVÍDCI", "BOBŘÍCI", "SÝČCI", "VÝŘI", "POŠTOLKY", "KRAKENI", "NORKOVCI",
            "LOSOSI", "PSTRUZI", "ŠTIKY", "OKOUNI", "SUMCI", "KAPRÍCI", "REJSCI", "KORÁLOVCI",
            "BISONI", "MAMUTI", "LIPANI", "MĚSÍČNÍCI", "ORLOVCI", "ALPAKY", "LAMY", "CHINCHILY",
            "LEGUIÁNI", "VARANI", "SALAMANDŘI", "KARETY", "MEDŮZY", "MRAVENCI", "VČELKY", "ČMELÁCI",
            "MOTÝLI", "CRAKÁČI", "KAVKY", "STEHLÍCI", "SÝKOŘI", "SÝČKOVCI", "ŠPAČCI", "KOSI"
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF112217)
        ),
        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Icon with green background glow
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(ForestGreenPrimary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Připojit se ke skupině",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Zadejte kód skupiny (např. KOČKY) pro přístup k živým výsledkům nebo pro vytvoření nové skupiny.",
                fontSize = 12.5.sp,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Kód skupiny / klubu:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = currentRoomInput,
                    onValueChange = {
                        onRoomInputChange(it)
                        errorMessage = null
                    },
                    placeholder = { Text("např. VARANI", color = Color.White.copy(alpha = 0.35f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF182D20),
                        unfocusedContainerColor = Color(0xFF182D20),
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = Color(0xFF284833),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_code_input")
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = CoralRedLoss,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Connect Button
            Button(
                onClick = {
                    val code = currentRoomInput.trim()
                    if (code.isBlank()) {
                        errorMessage = "Prosím zadejte kód skupiny."
                    } else {
                        onConnectGroup(code)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("connect_group_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔌", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Připojit / Vytvořit skupinu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Generate Animal Code Button
            Button(
                onClick = {
                    val randomAnimal = czechAnimalsList.random()
                    onRoomInputChange(randomAnimal)
                    errorMessage = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F3528),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_animal_code_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎲", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vygenerovat kód zvířete",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
