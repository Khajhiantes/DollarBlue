package com.example.dolarblue

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.dolarblue.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.calculateButton.setOnClickListener {calculateDollarExchange()}
        binding.calculateGwei.setOnClickListener {getGweiToUsd()}


        setCheckedChangeListener()
        setCheckedDollars()
        waitTwoSecond()
        }


    private fun calculateDollarExchange() {
        hideSoftKeyboard()
        val amountToExchange = binding.costOfServiceEditText.text.toString()
        val dolarPrice = binding.dolarPrice.text.toString()

        val a = amountToExchange.toDoubleOrNull()
        val d = dolarPrice.toDoubleOrNull()

        val formatCurrency = NumberFormat.getCurrencyInstance()

        when{
            a == null && d == null -> {
                binding.totalAmount.text = getString(R.string.error_total)
                Toast.makeText(binding.root.context, getString(R.string.error_total), Toast.LENGTH_SHORT).show()
            }
            a == null && d != null -> {
                binding.totalAmount.text = getString(R.string.error_monto)
                Toast.makeText(binding.root.context, getString(R.string.error_monto), Toast.LENGTH_SHORT).show()
            }
            a != null && d == null -> {
                binding.totalAmount.text = getString(R.string.error_dolar)
                Toast.makeText(binding.root.context, getString(R.string.error_dolar), Toast.LENGTH_SHORT).show()

            }
            else -> {
                if(binding.switchID.isChecked){
                    val formattedTotal = formatCurrency.format(amountToExchange.toDouble() / dolarPrice.toDouble())
                    binding.totalAmount.text = getString(R.string.dolars_amount, formattedTotal)

                }else{
                    val formattedTotal = formatCurrency.format(amountToExchange.toDouble() * dolarPrice.toDouble())
                    binding.totalAmount.text = getString(R.string.pesos_amount, formattedTotal)
                }
            }
        }
    }

    private fun getDollarToArg() {
        val textView = findViewById<TextView>(R.id.dolarPrice)
        val queue = Volley.newRequestQueue(this)
        val url = "https://app.ripio.com/api/v3/public/rates/"

        val stringRequest = StringRequest(url,
            { response -> textView.text = usdcPriceArg(response)
            },  { Snackbar.make(binding.root, getString(R.string.internet_error), Snackbar.LENGTH_SHORT).setBackgroundTint(
                ContextCompat.getColor(this, R.color.purple_500)).show() })
        queue.add(stringRequest)
    }

    private fun getDollarBlue() {
        val textView = findViewById<TextView>(R.id.dolarPrice)
        val queue = Volley.newRequestQueue(this)
        val url = "https://dolarhoy.com/cotizaciondolarblue"

        val stringRequest = StringRequest(url,
            { response -> textView.text = dolarBluePrice(response)
            },  { Snackbar.make(binding.root, getString(R.string.internet_error), Snackbar.LENGTH_SHORT).setBackgroundTint(
                ContextCompat.getColor(this, R.color.purple_500)).show() })
        queue.add(stringRequest)
    }

    private fun getGweiToUsd() {
        val textView = findViewById<TextView>(R.id.gwei_text)
        val queue = Volley.newRequestQueue(this)
        val url = "https://etherscan.io/gastracker"

        val stringRequest = StringRequest(url,
            { response -> textView.text = gasEstimate(response)
            },  { Snackbar.make(binding.root, getString(R.string.internet_error), Snackbar.LENGTH_SHORT).setBackgroundTint(
                ContextCompat.getColor(this, R.color.purple_700)).show() })
        queue.add(stringRequest)
    }

    private fun usdcPriceArg(string: String): String {
        val array = string.split("{\"ticker\"")
        return array[2].split("\",\"")[2].replace("sell_rate\":\"", "")
    }

    private fun dolarBluePrice(string: String): String {
        val array = string.split("<div class=\"value\">\$")
        return array[1].split("</div>")[0]
    }

    private fun gasEstimate(string: String): String {
        //estimating coinbase gas price 1.5 dollars over the Gas cost
        val array = string.split("spanHighPriorityAndBase")
        val gweiAmount = array[0].split("spanHighPrice\">")[1].split("</span>")[0]
        val gweiToUsd = array[1].split("text-secondary\">")[1].split("|")[0].replace("\n",
            "").replace("$", "").toDouble() + 1.5
        val formatDecimal = String.format("%.2f", gweiToUsd)

        return "$gweiAmount Gwei = $formatDecimal USD"
    }

    private fun setCheckedChangeListener() {
        binding.switchID.setOnCheckedChangeListener { _, isChecked ->
            val msg = getString(if (isChecked) R.string.on else R.string.off)
            val exchangeButton = getString(if (isChecked) R.string.calculate_dollars else R.string.calculate_pesos)
            val exchangeText = getString(if (isChecked) R.string.dollar_to_exchange else R.string.pesos_to_exchange)
            //Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            binding.switchID.text = msg
            binding.calculateButton.text = exchangeButton
            binding.amountToPay.hint = exchangeText
        }
    }

    private fun setCheckedDollars(){
        binding.dollarSwitch.setOnCheckedChangeListener { _, isChecked ->
            val dollarKind = getString(if (isChecked) R.string.blue_dolar else R.string.ripio_dolar)
            binding.dollarSwitch.text = dollarKind
            if (isChecked) {
                getDollarBlue()
                binding.totalAmount.text =""
            } else {
                getDollarToArg()
                binding.totalAmount.text =""
            }
        }
    }

    private fun waitTwoSecond() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        getDollarToArg()
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            this.currentFocus?.windowToken ?: return, 0
        )
    }
}