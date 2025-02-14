package com.whosmyqueen.oim.demo

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.whosmyqueen.oim.keyboard.InputModeSwitcher
import com.whosmyqueen.oim.keyboard.SkbContainer
import com.whosmyqueen.oim.utils.MeasureHelper

class MainActivity : AppCompatActivity() {
    private lateinit var mSkbContainer: SkbContainer
    private lateinit var edt: EditText

    //
    private val mInputModeSwitcher = InputModeSwitcher()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 读取屏幕的宽高.
        val measureHelper = MeasureHelper.getInstance()
        measureHelper.onConfigurationChanged(resources.configuration, this)
        edt = findViewById(R.id.edt)
        mSkbContainer = findViewById(R.id.skb)
        mSkbContainer.setInputModeSwitcher(mInputModeSwitcher)
        mSkbContainer.setFocusableInTouchMode(true)
        mInputModeSwitcher.setInputMode(EditorInfo().apply { inputType = EditorInfo.TYPE_CLASS_TEXT })
        mSkbContainer.updateInputMode(null)
    }
}