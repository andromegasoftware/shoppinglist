package com.example.shoppinglist

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source


class MainActivity : AppCompatActivity() {

    private var checkListTotalListArray = ArrayList<CheckListModelClass>()
    private lateinit var checkListRecyclerViewAdapterClass: CheckListRecyclerViewAdapterClass
    private lateinit var checkListModelClass: CheckListModelClass
    private lateinit var addItemToListEditText: EditText
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var collectionReference: CollectionReference
    private lateinit var source: Source

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseFirestore = FirebaseFirestore.getInstance()
        collectionReference = firebaseFirestore.collection("shoppingList")
        checkListTotalListArray = arrayListOf()

        checkListRecyclerViewAdapterClass =
            CheckListRecyclerViewAdapterClass(checkListTotalListArray) { checkListModelClassItem: CheckListModelClass ->
                checkListClickListener(checkListModelClassItem)
            }

        takeDataFromFireStore()

        //recyclerview
        val checkListRecyclerView = findViewById<RecyclerView>(R.id.check_list_recycler_view)
        val checkListTotalListLayoutManager = LinearLayoutManager(this)
        checkListTotalListLayoutManager.orientation = LinearLayoutManager.VERTICAL
        checkListRecyclerView.layoutManager = checkListTotalListLayoutManager
        checkListRecyclerView.adapter = checkListRecyclerViewAdapterClass

        addItemToListEditText = findViewById(R.id.check_list_fragment_item_enter_edit_text)
        addItemToListEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        addItemToListEditText.isSingleLine = true
        addItemToList()

    }

    fun checkListClickListener(checkListModelClass: CheckListModelClass) {
        //handle click listener
        Toast.makeText(this, "first click listener", Toast.LENGTH_LONG).show()
    }

    fun checkListClickListenerTwo() {
        //handle click listener
        Toast.makeText(applicationContext, "first click listener two", Toast.LENGTH_LONG).show()
    }

    private fun takeDataFromFireStore(){
        checkListTotalListArray.clear()
        collectionReference.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                for (document in documents){
                    val resultForCheckListItems: CheckListModelClass = document.toObject<CheckListModelClass>(CheckListModelClass::class.java)
                    checkListTotalListArray.add(resultForCheckListItems)
                }

                checkListRecyclerViewAdapterClass.submitList(checkListTotalListArray)
                checkListRecyclerViewAdapterClass.notifyDataSetChanged()
            }
        }
            .addOnFailureListener { exception ->
                Log.w("listen_data", "Error getting documents: ", exception)
            }

    }

    fun saveDataToFireStore(){
        collectionReference.document()
            .set(checkListModelClass, SetOptions.merge())
            .addOnSuccessListener {
                takeDataFromFireStore()
                Log.d("TAG", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
    }

    private fun addItemToList(){
        addItemToListEditText.onDone { addItemToListEditText.hideKeyboard() }

    }
    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun EditText.onDone(callback: () -> Unit) {
        // These lines optional if you don't want to set in Xml
        imeOptions = EditorInfo.IME_ACTION_DONE
        maxLines = 1
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                // Your action on done
                if(addItemToListEditText.text.toString() != "") {
                    checkListModelClass =
                        CheckListModelClass(false, addItemToListEditText.text.toString())
                    saveDataToFireStore()
                    //checkListTotalListArray.add(checkListModelClass)
                    //checkListRecyclerViewAdapterClass.updateList(checkListTotalListArray)
                    addItemToListEditText.text.clear()
                }

                callback.invoke()
                true
            }
            false
        }
    }
}