import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
		window.minimumSize = Dimension(
			350, 600
		)

		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(5.dp)
				.shadow(
					3.dp, RoundedCornerShape(8.dp)
				), shape = RoundedCornerShape(8.dp), color = Color.Transparent
		) {
			App(titlebarContainer = { WindowDraggableArea { it() } })
		}
	}
}

@Preview
@Composable
fun AppPreview() {
	App()
}