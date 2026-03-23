package com.farmerchat.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sample.databinding.ActivityXmlDemoBinding

/**
 * Demonstrates [com.farmerchat.sdk.FarmerChatFabView] in a traditional XML-layout Activity.
 *
 * The host app only needs [AppCompatActivity] (or any [androidx.activity.ComponentActivity]).
 * No Compose dependency is needed in the host app's own code — it is only needed transitively
 * because the SDK uses Compose internally.
 */
class XmlDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityXmlDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXmlDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Programmatic alternative: open the chat without placing a FAB in the layout
        binding.btnOpenChatProgrammatic.setOnClickListener {
            FarmerChatSdk.openChat(this)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
