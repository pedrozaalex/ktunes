import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.soaresalex.ktunes.app.App
import com.soaresalex.ktunes.di.initializeKoin
import java.awt.Dimension

fun main() = application {
    initializeKoin()

    Window(
        title = "KTunes",
        onCloseRequest = ::exitApplication,
        undecorated = true,
        transparent = true,
    ) {
        window.minimumSize = Dimension(350, 600)

        Surface(
            modifier = Modifier.fillMaxSize().padding(5.dp).shadow(3.dp, RoundedCornerShape(8.dp)),
            color = Color.Transparent,
            shape = RoundedCornerShape(8.dp)
        ) {
            App(titlebarContainer = { content ->
                WindowDraggableArea {
                    Row(
                        Modifier.padding(2.dp),
                        Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                        ) { content() }
                    }
                }
            })
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}