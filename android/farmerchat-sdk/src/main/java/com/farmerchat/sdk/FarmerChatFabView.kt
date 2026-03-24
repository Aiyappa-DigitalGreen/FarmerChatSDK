package com.farmerchat.sdk

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.farmerchat.sdk.di.SdkKoinHolder
import com.farmerchat.sdk.ui.theme.SdkGreen800
import com.farmerchat.sdk.ui.theme.SdkTheme
import org.koin.compose.KoinContext

/**
 * View-based Floating Action Button for XML layout integration.
 *
 * Drop-in replacement for [FarmerChatFab] (the Compose version) — usable in any
 * Activity or Fragment that uses XML layouts. The host Activity must extend
 * [androidx.appcompat.app.AppCompatActivity] (or any [androidx.activity.ComponentActivity]).
 *
 * XML usage:
 * ```xml
 * <com.farmerchat.sdk.FarmerChatFabView
 *     android:id="@+id/farmerChatFab"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:layout_gravity="bottom|end"
 *     android:layout_margin="16dp"
 *     app:fc_label="Ask FarmerChat"
 *     app:fc_extended="true"
 *     app:fc_containerColor="@color/primary_green" />
 * ```
 *
 * Programmatic usage (Kotlin):
 * ```kotlin
 * val fab = FarmerChatFabView(context).apply {
 *     setLabel("Ask FarmerChat")
 *     setExtended(true)
 *     setConversationId("conv-123")   // optional
 * }
 * container.addView(fab)
 * ```
 *
 * Requires [FarmerChatSdk.initialize] to be called before this view is displayed.
 */
class FarmerChatFabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var label: String? = null
    private var extended: Boolean = true
    private var conversationId: String? = null
    private var containerColor: Color = SdkGreen800

    private val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    init {
        // Read XML attributes
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.FarmerChatFabView, defStyleAttr, 0)
            try {
                label = ta.getString(R.styleable.FarmerChatFabView_fc_label)
                extended = ta.getBoolean(R.styleable.FarmerChatFabView_fc_extended, true)
                conversationId = ta.getString(R.styleable.FarmerChatFabView_fc_conversationId)
                if (ta.hasValue(R.styleable.FarmerChatFabView_fc_containerColor)) {
                    val argb = ta.getColor(R.styleable.FarmerChatFabView_fc_containerColor, SdkGreen800.toArgb())
                    containerColor = Color(argb)
                }
            } finally {
                ta.recycle()
            }
        }

        addView(composeView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        updateContent()
    }

    // ── Public API ──────────────────────────────────────────────────────────────

    /** Override the FAB label. Pass null to fall back to [FarmerChatConfig.fabLabel]. */
    fun setLabel(value: String?) {
        label = value
        updateContent()
    }

    /** Show or hide the label text (extended = true shows icon + label). */
    fun setExtended(value: Boolean) {
        extended = value
        updateContent()
    }

    /** Pin the FAB to a specific conversation ID. Null starts a new conversation. */
    fun setConversationId(value: String?) {
        conversationId = value
        updateContent()
    }

    /** Change the FAB background color. */
    fun setContainerColor(color: Color) {
        containerColor = color
        updateContent()
    }

    // ── Internal ────────────────────────────────────────────────────────────────

    private fun updateContent() {
        val capturedLabel = label
        val capturedExtended = extended
        val capturedConversationId = conversationId
        val capturedColor = containerColor

        composeView.setContent {
            KoinContext(context = SdkKoinHolder.koin) {
                SdkTheme {
                    FarmerChatFab(
                        extended = capturedExtended,
                        label = capturedLabel,
                        conversationId = capturedConversationId,
                        fabBackgroundColor = capturedColor,
                        fabContentColor = Color.White
                    )
                }
            }
        }
    }
}
