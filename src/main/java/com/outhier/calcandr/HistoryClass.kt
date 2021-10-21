/*
 * CalcAndr - A calculator application for Android, written in Kotlin.
 *
 * @url https://github.com/moDevsome/CalcAndr
 * @author Mickaël Outhier <contact@mickael-outhier.fr>
 * @copyright (c) 2021 Mickaël Outhier (contact@mickael-outhier.fr)
 *
 * @licence  Apache License Version 2.0, January 2004
 * THE SOFTWARE CANNOT BE SALED OR SHARED UNDER A DIFFERENT TRADEMARK THAN THE INTIAL TRADEMARK.
 * ANYKIND OF APPROPRIATION IS STRICTLY FORBIDDEN.
 */

package com.outhier.calcandr

import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginLeft
import java.io.File
import java.io.IOException


class HistoryClass {

    private lateinit var wrapper: RelativeLayout
    private  lateinit var scrollView: View

    private lateinit var clearButton: Button

    private lateinit var context: MainActivity
    private lateinit var utilities: Utilities
    private lateinit var historyStream: File

    // Clear the current history
    public fun clear() {

        try {

            this.historyStream.delete()

        } catch (e: IOException) {

            this.utilities.traceout(e.message.toString(), "ERROR")
            this.utilities.alert("Sorry, The history could not be deleted yet.")

        }

        this.historyStream.createNewFile()
        this.fill()

    }

    // Fill the container with each history element (we use the dimension of the clear button)
    public fun fill() {

        // Remove all current children
        var i = 1;
        if(this.wrapper.childCount > 1) {

            this.wrapper.removeViews(1, this.wrapper.childCount - 1)

        }

        var baseMarginTop = 5;
        var counter = 1;
        this.historyStream.forEachLine {

            val line = it.split("|")

            if(line.size == 5) {

                val newButton = Button(this.context)
                var buttonLayoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                        this.clearButton.layoutParams.width,
                        this.clearButton.layoutParams.height
                );
                buttonLayoutParams.leftMargin = this.clearButton.marginLeft

                buttonLayoutParams.topMargin =
                        (this.clearButton.layoutParams.height * counter) + (baseMarginTop * counter);
                newButton.layoutParams = buttonLayoutParams;
                newButton.text = "${line[1]} ${line[2]} ${line[3]} = ${line[4]}"

                newButton.setOnClickListener {

                    this.context.operationFromHistory(line[1], line[3], line[2])

                }

                this.wrapper.addView(newButton)
                counter++

            }

        }

    }

    public fun open() {

        this.context.setContentView(R.layout.history)
        this.wrapper = this.context.findViewById(R.id.calculator_history_wrapper)
        this.clearButton = this.context.findViewById(R.id.history_delete)
        this.scrollView = this.context.findViewById(R.id.calculator_history_scrollview)

        this.context.supportActionBar?.setTitle(R.string.history_title)

        // @since 1.2
        // Fit the height of history scrollview
        this.scrollView.doOnAttach {

            val contextDisplayMetrics: DisplayMetrics = this.context.resources.displayMetrics
            val actionBarHeight: Int = this.context.supportActionBar?.height.toString().toInt()
            val bottomHeight: Int = contextDisplayMetrics.heightPixels - this.context.floatingActionButtonMeasureTop
            val fitHeight = contextDisplayMetrics.heightPixels - (actionBarHeight + bottomHeight)

            this.scrollView.layoutParams.height = fitHeight - ((fitHeight / 100) * 10)

        }

        this.fill()

    }

    public fun init(context: MainActivity, utilities: Utilities, historyStream: File) {

        this.context = context
        this.utilities = utilities
        this.historyStream = historyStream

    }

}
