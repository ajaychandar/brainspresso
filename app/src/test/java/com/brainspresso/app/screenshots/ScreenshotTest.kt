package com.brainspresso.app.screenshots

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.brainspresso.app.data.db.AlarmEntity
import com.brainspresso.app.ui.alarmlist.AlarmListContent
import com.brainspresso.app.ui.puzzle.PuzzleScreen
import com.brainspresso.app.ui.puzzle.PuzzleUiState
import com.brainspresso.app.ui.settings.SettingsContent
import com.brainspresso.app.ui.splash.SplashScreen
import com.brainspresso.app.ui.theme.BrainspressoTheme
import com.brainspresso.core.puzzle.MathPuzzle
import com.brainspresso.core.puzzle.MemorySequencePuzzle
import com.brainspresso.core.puzzle.PuzzleSettings
import com.brainspresso.core.puzzle.TapPatternPuzzle
import java.io.File
import java.time.DayOfWeek
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the real screens with sample data to PNGs under build/screenshots. Skipped unless
 * CAPTURE_SCREENSHOTS=1 so the normal test run stays fast and can never block a build.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w412dp-h915dp-xxhdpi")
class ScreenshotTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun onlyWhenRequested() {
        assumeTrue(System.getenv("CAPTURE_SCREENSHOTS") == "1")
    }

    private fun capture(name: String, advanceMillis: Long = 3_000, content: @Composable () -> Unit) {
        val dir = File("build/screenshots").apply { mkdirs() }
        try {
            rule.setContent { BrainspressoTheme { content() } }
            rule.mainClock.advanceTimeBy(advanceMillis)
            rule.waitForIdle()
            // Draw the window ourselves: compose-ui-test's captureToImage waits on a callback
            // that Robolectric never delivers, so it times out.
            val view = rule.activity.window.decorView
            val bitmap = Bitmap.createBitmap(
                view.width.coerceAtLeast(1), view.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888,
            )
            rule.runOnUiThread { view.draw(Canvas(bitmap)) }
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val distinct = (0 until 200).map { bitmap.getPixel((it * 7) % bitmap.width, (it * 13) % bitmap.height) }.toSet().size
            File(dir, "diagnostics.txt").appendText("$name: ${bitmap.width}x${bitmap.height}, distinct sampled colors=$distinct\n")
        } catch (t: Throwable) {
            File(dir, "diagnostics.txt").appendText("$name FAILED: ${t::class.java.name}: ${t.message}\n${t.stackTrace.take(12).joinToString("\n")}\n")
            throw t
        }
    }

    private val weekdays = setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
    )

    private fun alarm(id: Long, hour: Int, minute: Int, label: String, days: Set<DayOfWeek>, enabled: Boolean) =
        AlarmEntity(
            id = id, hour = hour, minute = minute, label = label, repeatDays = days,
            enabled = enabled, soundUri = null, vibrate = true, createdAt = 0,
        )

    @Test
    fun splash() = capture("01-splash") { SplashScreen() }

    @Test
    fun alarmList() = capture("02-alarm-list") {
        AlarmListContent(
            alarms = listOf(
                alarm(1, 6, 30, "Morning jolt", weekdays, true),
                alarm(2, 7, 15, "Gym & espresso", setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), true),
                alarm(3, 8, 45, "Weekend lie-in", setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), false),
            ),
            onAddAlarm = {}, onEditAlarm = {}, onOpenSettings = {}, onToggle = { _, _ -> }, onDelete = {},
        )
    }

    @Test
    fun mathPuzzle() = capture("03-ringing-math") {
        PuzzleScreen(
            state = PuzzleUiState(
                puzzle = MathPuzzle("14 * 3 + 19", 61),
                alarmLabel = "Morning jolt",
                solvedCount = 1,
                required = 3,
                justFailed = true,
            ),
            onSolved = {}, onFailed = {}, animateIndicators = false,
        )
    }

    @Test
    fun memoryPuzzle() = capture("04-ringing-memory") {
        PuzzleScreen(
            state = PuzzleUiState(
                puzzle = MemorySequencePuzzle(sequence = listOf(0, 3, 1, 4, 2), padSize = 6),
                alarmLabel = "Morning jolt", solvedCount = 0, required = 2,
            ),
            onSolved = {}, onFailed = {}, animateIndicators = false,
        )
    }

    @Test
    fun tapPatternPuzzle() = capture("05-ringing-pattern") {
        PuzzleScreen(
            state = PuzzleUiState(
                puzzle = TapPatternPuzzle(gridSize = 4, pattern = setOf(0, 5, 6, 11, 13)),
                solvedCount = 1, required = 3,
            ),
            onSolved = {}, onFailed = {}, animateIndicators = false,
        )
    }

    @Test
    @Config(qualifiers = "w412dp-h1000dp-xxhdpi")
    fun walkToStop() = capture("06-walk-to-stop") {
        PuzzleScreen(
            state = PuzzleUiState(
                solvedCount = 3, required = 3, isWalking = true, stepsWalked = 7, stepsRequired = 15,
            ),
            onSolved = {}, onFailed = {}, animateIndicators = false,
        )
    }

    @Test
    @Config(qualifiers = "w412dp-h1900dp-xxhdpi")
    fun settings() = capture("07-settings") {
        SettingsContent(
            settings = PuzzleSettings(requiredStreak = 3, requireWalkToStop = true, walkStepsRequired = 15),
            onBack = {}, onToggleType = {}, onSetDifficulty = {}, onSetRequiredStreak = {},
            onToggleRequireWalk = {}, onSetWalkStepsRequired = {},
        )
    }
}
