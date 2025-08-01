package com.google.android.libraries.gsa.d.a

import android.os.Handler
import android.os.Message
import java.io.PrintWriter

open class BaseCallback : Handler.Callback {

    override fun handleMessage(message: Message): Boolean {
        return true
    }

    open fun dump(printWriter: PrintWriter, str: String) {
        printWriter.println("$str: nothing to dump")
    }
}