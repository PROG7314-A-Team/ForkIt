package com.example.forkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ForkItTheme {
                AccountScreen()
            }
        }
    }
}

@Composable
fun AccountScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header with back arrow and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "<", // Back arrow placeholder
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = "Account",
                fontSize = 20.sp,
                color = Color(0xFF22B27D),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Profile icon as a colored circle
        Card(
            shape = CircleShape,
            modifier = Modifier.size(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E9ECC)
            )
        ) {
            // Optionally, place an Image here
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Email
        Text(
            text = "john@gmail.com",
            fontSize = 18.sp,
            color = Color(0xBF1E9ECC) // 75% opacity
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Weight label
        Text(
            text = "Weight",
            fontSize = 18.sp,
            color = Color(0xFF1E9ECC),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Weight Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0x051E9ECC) // 2% opacity
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "75 kg",
                    fontSize = 18.sp,
                    color = Color(0xFF1E9ECD)
                )
            }
        }
    }
}

@Composable
fun AccountScreenPreview() {
    ForkItTheme {
        AccountScreen()
    }
}
