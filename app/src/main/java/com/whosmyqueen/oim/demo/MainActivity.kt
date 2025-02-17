package com.whosmyqueen.oim.demo

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.whosmyqueen.oim.keyboard.InputModeSwitcher
import com.whosmyqueen.oim.keyboard.SkbContainer
import com.whosmyqueen.oim.keyboard.SoftKey
import com.whosmyqueen.oim.keyboard.SoftKeyBoardListener
import com.whosmyqueen.oim.utils.MeasureHelper
import com.whosmyqueen.oim.utils.OPENLOG

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
        mSkbContainer.setOnSoftKeyBoardListener(object : SoftKeyBoardListener {
            override fun onCommitResultText(text: String?) {
                // 输入文字.
                edt.setText("${edt.getText()}${text}")
            }

            override fun onCommitText(key: SoftKey?) {
            }

            override fun onDelete() {
                val text: String = edt.getText().toString()
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(applicationContext, "文本已空", Toast.LENGTH_LONG).show()
                } else {
                    edt.setText(text.substring(0, text.length - 1))
                }
            }

            override fun onBack() {
            }
        })
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mSkbContainer.onSoftKeyDown(keyCode, event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (mSkbContainer.onSoftKeyUp(keyCode, event)) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}