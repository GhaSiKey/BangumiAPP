package com.example.bangumi.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by gaoshiqi
 * on 2025/7/31 19:26
 * email: gaoshiqi@bilibili.com
 */
object KeyboardUtils {

    /**
     * 显示软键盘
     * @param context 上下文
     * @param view 通常是EditText
     */
    fun showSoftKeyboard(context: Context, view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏软键盘
     * @param context 上下文
     * @param view 当前界面任意可见View
     */
    fun hideSoftKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 切换软键盘显示/隐藏状态
     * @param context 上下文
     */
    fun toggleSoftKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}