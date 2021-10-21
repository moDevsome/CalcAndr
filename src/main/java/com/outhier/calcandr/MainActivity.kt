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

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.fromHtml
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var utilities: Utilities;
    private lateinit var historyStream: File;
    private lateinit var historyObject: HistoryClass

    private var numbLeft: String = "" // The number on the left side of the current operator
    private var numbRight: String = "" // The number on the right side of the current operator
    private var currentOperator: String = "" // The current operator (+, -, X, /)
    private var currentOperation: String = "" // The current operation

    private var historyRecordsCount: Int = 0

    private lateinit var calculatorFunctionsButtons: MutableList<TextView>

    var floatingActionButtonMeasureTop: Int = 0 // @since 1.2, public var used by the history class to fit the scrollview height

    /** Lock calculator functions depending on the context
     *
     * @since  1.0.0
     * @return void
     */
    private fun calculatorFunctionsLock() {

        if(currentOperation.isEmpty()) { // We lock all the functions

            this.calculatorFunctionsButtons[0].visibility = INVISIBLE
            this.calculatorFunctionsButtons[1].visibility = VISIBLE
            this.calculatorFunctionsButtons[2].visibility = INVISIBLE
            this.calculatorFunctionsButtons[3].visibility = VISIBLE
            this.calculatorFunctionsButtons[4].visibility = INVISIBLE
            this.calculatorFunctionsButtons[5].visibility = VISIBLE

        }
        else {

            if(this.currentOperator.isNotEmpty()) { // We lock square and root functions

                this.calculatorFunctionsButtons[0].visibility = INVISIBLE
                this.calculatorFunctionsButtons[1].visibility = VISIBLE
                this.calculatorFunctionsButtons[2].visibility = INVISIBLE
                this.calculatorFunctionsButtons[3].visibility = VISIBLE

            }
            else { // We can unlock the function

                this.calculatorFunctionsButtons[0].visibility = VISIBLE
                this.calculatorFunctionsButtons[1].visibility = INVISIBLE
                this.calculatorFunctionsButtons[2].visibility = VISIBLE
                this.calculatorFunctionsButtons[3].visibility = INVISIBLE
                this.calculatorFunctionsButtons[4].visibility = VISIBLE
                this.calculatorFunctionsButtons[5].visibility = INVISIBLE

            }

        }

    }

    /** Clean a numb string and return it to float
     *
     * @since  1.0.0
     * @return string Result
     */
    private fun floatNumb(numb: String) : Float {

        var theNumb = numb

        if(numb.filter{ it == '.' }.count() == 1 && theNumb[numb.length - 1] == '.') { // We add a 0 for preventing crash

            theNumb+= "0"

        }

        return theNumb.toFloat()

    }

    /** Display the CALCULATOR screen
     *
     * @since  1.0.0
     * @return void
     */
    private fun openMain() {

        setContentView(R.layout.main)

        // @since 1.2
        // we get the vertical position of the FloatingActionButton for using it when we fit the history scrollview
        var hiddenFloatingActionButton: View = findViewById(R.id.hidden_floating_action_button)
        hiddenFloatingActionButton.doOnLayout {

            this.floatingActionButtonMeasureTop = it.top

        }

        // @See https://stackoverflow.com/questions/28954445/set-toolbar-title
        this.supportActionBar?.setTitle(R.string.calculator_title)

        // Set the button number
        this.calculatorFunctionsButtons = mutableListOf()
        this.calculatorFunctionsButtons.add(0, findViewById(R.id.calculator_button_carre))
        this.calculatorFunctionsButtons.add(1, findViewById(R.id.calculator_button_carre_locked))
        this.calculatorFunctionsButtons.add(2, findViewById(R.id.calculator_button_root))
        this.calculatorFunctionsButtons.add(3, findViewById(R.id.calculator_button_root_locked))
        this.calculatorFunctionsButtons.add(4, findViewById(R.id.calculator_button_delete))
        this.calculatorFunctionsButtons.add(5, findViewById(R.id.calculator_button_delete_locked))

    }

    /** Display the ABOUT screen
     *
     * @since  1.0.0
     * @return void
     */
    public fun openAbout() {

        val packageInfos: PackageInfo = packageManager.getPackageInfo(packageName, 0)

        this.setContentView(R.layout.about)

        // @since 1.2
        // we display the current version informations
        val lastUpdate: String = SimpleDateFormat("dd/MM/yyyy").format( Date(packageInfos.lastUpdateTime) )
        val version: String = packageInfos.versionName +" ("+ lastUpdate +")"
        this.findViewById<TextView>(R.id.calculator_about_wrapper).text = fromHtml(this.getString(R.string.about_paragraphe), HtmlCompat.FROM_HTML_MODE_LEGACY)
                .replace(Regex("\\b(NUMVERSION)\\b"), version)
                .replace(Regex("\\b(YEAR)\\b"), lastUpdate.substring(6))

        supportActionBar?.setTitle(R.string.about_title)

    }

    /** Display the HISTORY screen
     *
     * @since  1.0.0
     * @return void
     */
    public fun openHistory(view: View) {

        this.historyObject.open()

    }

    /** Delete all the operations stored in history
     *
     * @since  1.0.0
     * @return void
     */
    public fun clearHistory(view: View) {

        val confirmDialog = AlertDialog.Builder(this)

        confirmDialog.setTitle(R.string.confirmation_string);
        confirmDialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->

            dialog.dismiss()
            this.historyObject.clear()

        });
        confirmDialog.setNegativeButton(R.string.cancel_string, DialogInterface.OnClickListener { dialog, id ->

            dialog.cancel()

        });

        confirmDialog.setMessage(R.string.history_clear_confirm_text)
        confirmDialog.show()

    }

    /** Return to the main screen
     *
     * @since  1.0.0
     * @return void
     */
    public fun backToMain(view: View) {

        this.resetOperation()
        this.openMain()

    }

    /** Open the Github project page in the device web browser
     *
     * @since  1.0.0
     * @return void
     */
    public fun goToGithub(view: View) {

        // @see https://stackoverflow.com/questions/9932775/easiest-way-to-have-button-open-browser-to-specific-url/18579371
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/moDevsome/CalcAndr")))

    }

    /** Import an operation from the history
     *
     * @since  1.0.0
     * @return void
     */
    public fun operationFromHistory(numbLeft: String, numbRight: String, operator: String) {

        this.openMain()

        var calculatorOperation: TextView = findViewById(R.id.calculator_operation)
        this.resetOperation()

        this.numbLeft = numbLeft
        this.numbRight = numbRight
        this.currentOperator = operator
        this.currentOperation = numbLeft + operator + numbRight
        calculatorOperation.text = numbLeft + operator + numbRight

        // We unlock the delete function
        this.calculatorFunctionsButtons[4].visibility = VISIBLE
        this.calculatorFunctionsButtons[5].visibility = INVISIBLE

    }

    /** Execute square operation
     *
     * @since  1.0.0
     * @return void
     */
    public fun squareFunction() : String {

        this.numbRight = this.numbLeft;
        this.currentOperator = "X";

        return this.executeOperation();

    }

    /** Execute square root operation
     *
     * @since  1.0.0
     * @return void
     */
    public fun squarerootFunction() : String {

        var result: String = sqrt(this.floatNumb(this.numbLeft)).toString();

        // We round the result to an entire number if the digit after the dot is 0
        val resultSegments: List<String> = result.split(".");
        if(resultSegments[1].toString() == "0") {

            result = resultSegments[0].toString();

        }

        this.numbLeft = result;
        this.numbRight = "";
        this.currentOperator = "";
        this.currentOperation = this.numbLeft;

        this.calculatorFunctionsLock();

        return this.currentOperation;

    }

    /** Delete a char in the current operation
     *
     * @since  1.0.0
     * @return string Result
     */
    public fun deleteOperationChar() : String {

        if(this.currentOperation.isNotEmpty()) {

            val lastCharIndex: Int = this.currentOperation.length - 1
            val lastChar: String = this.currentOperation[lastCharIndex].toString()
            val operators: List<String> = listOf("+","-","X","÷")

            if(lastChar in operators) {

                this.currentOperator = ""

            }
            else {

                if(this.numbRight.isNotEmpty()) { // La caractère est supprimé dans le segment de droite

                    this.numbRight = this.numbRight.substring(0, this.numbRight.length - 1)

                }
                else {

                    if(this.numbLeft.isNotEmpty()) { // La caractère est supprimé dans le segment de gauche

                        this.numbLeft = this.numbLeft.substring(0, this.numbLeft.length - 1)

                    }

                }

            }

            this.currentOperation = this.numbLeft + this.currentOperator + this.numbRight;

            this.calculatorFunctionsLock();

        }

        return this.currentOperation;

    }

    /** Concat the current operation string
     *
     * @since  1.0.0
     * @return string Result
     */
    public fun concatOperation(entry: String) : String {

        val operators: List<String> = listOf("+","-","X","÷");

        if(entry in operators) { // We add the operator

            if(this.numbLeft.isNotEmpty()) {

                if(currentOperator.isNotEmpty() && this.numbRight.isNotEmpty()) { // We execute the operation if the operator and the numbRight are already informed

                    val result = this.executeOperation()

                    this.currentOperator = entry
                    this.currentOperation = result + entry

                }
                else if(currentOperator.isEmpty() && this.numbRight.isEmpty()) { // We add the operator

                    this.currentOperator = entry;
                    this.currentOperation = this.currentOperation + entry

                }
                else { // If we're not in 1 of the 2 cases above, we don't change anything

                    this.currentOperation = this.currentOperation

                }

            }

        }
        else if(entry in listOf<String>(".")) { // We add a dot

            if(this.currentOperator.isEmpty()) { // The operator is NOT provided, so we update the numbLeft

                if(this.numbLeft.isNotEmpty() && this.numbLeft.filter{ it == '.' }.count() == 0) { // We change the current segment string only if the current string does not contain a dot

                    this.numbLeft = this.numbLeft + entry
                    this.currentOperation = this.currentOperation + entry

                }

            }
            else { // The operator is provided, so we update the numbRight

                if(this.numbRight.isNotEmpty() && this.numbRight.filter{ it == '.' }.count() == 0) { // We change the current segment string only if the current string does not contain a dot

                    this.numbRight = this.numbRight + entry
                    this.currentOperation = this.currentOperation + entry

                }

            }

        }
        else { // We add a digit

            if(this.currentOperator.isEmpty()) { // The operator is NOT provided, so we update the numbLeft

                if(this.numbLeft.length >= 10) { // We can't concat more than 10 digit per segment

                    this.utilities.alert(this.getString(R.string.calculator_too_many_digit))

                    this.calculatorFunctionsLock()

                    return this.currentOperation

                }
                else {

                    this.numbLeft = this.numbLeft + entry

                }

            }
            else { // The operator is provided, so we update the numbRight

                if(this.numbRight.length >= 10) { // We can't concat more than 10 digit per segment

                    this.utilities.alert(this.getString(R.string.calculator_too_many_digit))

                    this.calculatorFunctionsLock()

                    return this.currentOperation

                }
                else {

                    this.numbRight = this.numbRight + entry

                }

            }

            this.currentOperation = this.currentOperation + entry

        }

        this.calculatorFunctionsLock()

        return this.currentOperation

    }

    /** Reset the operation states (numbLeft, numbRight, currentOperator, currentOperation) and return an empty string
     *
     * @since  1.0.0
     * @return string Result
     */
    public fun resetOperation() : String {

        this.numbLeft = ""
        this.numbRight = ""
        this.currentOperator = ""
        this.currentOperation = ""

        this.calculatorFunctionsLock()

        return ""

    }

    /** Execute the operation, update operation states (numbLeft, numbRight, currentOperator, currentOperation) and return the result as string
     *
     * @since  1.0.0
     * @return string Result
     */
    public fun executeOperation() : String {

        if(this.numbLeft.isNotEmpty() && this.numbRight.isNotEmpty() && this.currentOperator.isNotEmpty()) {

            val numbLeft: Float = this.floatNumb(this.numbLeft)
            val numbRight: Float = this.floatNumb(this.numbRight)

            // @since  1.2.0
            // Prevent division by 0
            if(this.currentOperator == "÷" && numbRight.toString() == "0.0") {

                this.utilities.alert(this.getString(R.string.calculator_division_by_zero_error))

                this.numbRight = ""
                this.currentOperation = this.numbLeft +"÷"

                return this.currentOperation

            }

            var result: String = when(this.currentOperator) {
                "+" -> (numbLeft + numbRight).toString()
                "-" -> (numbLeft - numbRight).toString()
                "X" -> (numbLeft * numbRight).toString()
                "÷" -> (numbLeft / numbRight).toString()
                else -> ""
            }

            // We round the result to an entire number if the digit after the dot is 0
            val resultSegments: List<String> = result.split(".");
            if(resultSegments[1].toString() == "0") {

                result = resultSegments[0].toString()

            }

            // We save the last executed operation into the history
            try {

                if(this.historyRecordsCount >= 1000) {


                    this.utilities.alert(this.getString(R.string.calculator_too_many_history_records))

                }
                else {

                    var content: String = this.historyStream.readText();
                    content = "OP|"+ this.numbLeft +"|"+ this.currentOperator +"|"+ this.numbRight +"|"+ result + System.getProperty("line.separator") + content;

                    this.historyStream.writeText("")
                    historyStream.appendText( content )

                    this.historyRecordsCount++

                }

            }
            catch(e: Exception) {

                this.utilities.alert("Error")

            }

            this.numbLeft = result
            this.numbRight = ""
            this.currentOperator = ""
            this.currentOperation = result

        }

        this.calculatorFunctionsLock()

        return this.currentOperation

    }

    /**
     * Triggered by a tap on 1 of the calculator button
     *
     * @since 1.0.0
     * @return Void
     */
    public fun buttonCalculator(view: View) {

        var calculatorOperation: TextView = findViewById(R.id.calculator_operation)
        val buttonText: TextView = findViewById(view.id)

        when(val buttonValue = buttonText.text.toString()) {
            "=" -> calculatorOperation.text = this.executeOperation()
            "C" -> calculatorOperation.text = this.resetOperation()
            "X²" -> calculatorOperation.text = this.squareFunction()
            "√" -> calculatorOperation.text = this.squarerootFunction()
            "⇐" -> calculatorOperation.text = this.deleteOperationChar()
            else -> calculatorOperation.text = this.concatOperation(buttonValue)
        }

    }

    /**
     * Exit the application
     *
     * @since 1.0.0
     * @return Void
     */
    public fun appExit() {

        val confirmDialog = AlertDialog.Builder(this)

        confirmDialog.setTitle(R.string.confirmation_string);
        confirmDialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->

            dialog.dismiss()

            // Close all activities and finish process
            this.finishAffinity()
            exitProcess(0)

        });
        confirmDialog.setNegativeButton(R.string.cancel_string, DialogInterface.OnClickListener { dialog, id ->

            dialog.cancel()

        });

        confirmDialog.setMessage(R.string.close_main_confirm_message)
        confirmDialog.show()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item?.itemId) {
            R.id.about -> {
                this.openAbout()
            }
            else -> {
                this.appExit()
            }
        }

        return true

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)

        return true

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        this.utilities = Utilities(this)

        this.historyStream = File(this.filesDir, "history.txt")
        if(this.historyStream.exists()) {

            try {

                this.historyStream.delete()

            } catch(e: IOException) {

                this.utilities.traceout(e.message.toString(), "ERROR")
                this.utilities.alert("Sorry, The application can't be used.")
                exitProcess(0)

            }

        }

        this.historyStream.createNewFile()
        this.historyObject = HistoryClass()
        this.historyObject.init(this, this.utilities, historyStream)

        // Fill the status bar background with the darkOrangeColor
        val darkOrangeColor = ColorDrawable(Color.parseColor("#F77F00"))
        this.supportActionBar?.setBackgroundDrawable(darkOrangeColor)

        this.openMain()

    }

}
