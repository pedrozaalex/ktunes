import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import com.soaresalex.ktunes.app.App
import com.soaresalex.ktunes.di.initializeKoin

fun main() = application {
    Window(
        title = "KTunes",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        initializeKoin()
        App()
    }
}

@Preview
@Composable
fun AppPreview() { App() }