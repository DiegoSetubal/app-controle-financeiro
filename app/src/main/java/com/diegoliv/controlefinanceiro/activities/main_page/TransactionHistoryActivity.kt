package com.diegoliv.controlefinanceiro.activities.main_page

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diegoliv.controlefinanceiro.R
import com.diegoliv.controlefinanceiro.activities.main_page.adapters.TransactionsAdapter
import com.diegoliv.controlefinanceiro.infrastructure.firebase.FirebaseDbHelper
import com.diegoliv.controlefinanceiro.models.Transaction
import com.diegoliv.controlefinanceiro.util.Utilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.*


import kotlinx.android.synthetic.main.activity_transaction.*
import java.text.SimpleDateFormat

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: FirebaseDbHelper
    private lateinit var util: Utilities

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        auth = FirebaseAuth.getInstance()
        dbHelper = FirebaseDbHelper()
        util = Utilities()

        fab.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Deseja criar nova transação?")
                .setPositiveButton("Sim") { _, _ ->
                    startActivity(Intent(this@TransactionHistoryActivity, AddTransaction::class.java))
                }
                .setNegativeButton("Não") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }

        inflateRecyclerViewWithMocks()
    }

    private fun inflateRecyclerView() {
        var userId = auth.currentUser!!.uid

        dbHelper
            .getOrderBy("transactions", "user_id", userId, "date")
            .addOnSuccessListener { documents ->
                val transactions = ArrayList<Transaction>()

                for (document in documents) {
                    val transactionName = document["name"].toString()
                    val transactionDesc = document["description"].toString()
                    val balanceBefore = document["balanceBefore"].toString().toDouble()
                    val balanceAfter = document["balanceAfter"].toString().toDouble()
                    val isExpense = document["expense"].toString().toBoolean()
                    val date = SimpleDateFormat("dd/MM/yyyy").parse(document["data"].toString())
                    val value = document["value"].toString().toDouble()


                    transactions.add(Transaction(userId, date, isExpense, value, balanceBefore, balanceAfter, transactionName, transactionDesc))
                }

                setRecyclerViewAdapter(transactions)
            }
            .addOnFailureListener { exception ->
                Log.e("Ex", exception.message)
            }
    }

    private fun inflateRecyclerViewWithMocks() {
        var userId = auth.currentUser!!.uid
        val transactions = ArrayList<Transaction>()

        for (i in 1..10) {
            transactions.add(
                Transaction(
                    userId,
                    Date(),
                    true,
                    10.00,
                    100.00,
                    90.00,
                    "Compra $i",
                    "Compra $i no Supermercado"
                )
            )
        }

        setRecyclerViewAdapter(transactions)
    }

    private fun setRecyclerViewAdapter(transactions: ArrayList<Transaction>) {
        val transactionRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        transactionRecyclerView.adapter = TransactionsAdapter(this, transactions)
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}