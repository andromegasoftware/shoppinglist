package com.example.shoppinglist

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source


class MainActivity : AppCompatActivity() {

    private var checkListTotalListArray = ArrayList<CheckListModelClass>()
    private lateinit var checkListRecyclerViewAdapterClass: CheckListRecyclerViewAdapterClass
    private lateinit var checkListModelClass: CheckListModelClass
    private lateinit var addItemToListEditText: EditText
    private lateinit var firebaseFireStore: FirebaseFirestore
    private lateinit var documentId: String

    private lateinit var collectionReference: CollectionReference
    private lateinit var checkListRecyclerView: RecyclerView

    private var updatedItemOnList: String = ""
    private var updatedItemId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseFireStore = FirebaseFirestore.getInstance()
        collectionReference = firebaseFireStore.collection("shoppingList")
        checkListTotalListArray = arrayListOf()

        checkListRecyclerViewAdapterClass =
            CheckListRecyclerViewAdapterClass(checkListTotalListArray) { checkListModelClassItem: CheckListModelClass ->
                checkListClickListener(checkListModelClassItem)
            }

        takeDataFromFireStore()

        //recyclerview
        checkListRecyclerView = findViewById(R.id.check_list_recycler_view)
        val checkListTotalListLayoutManager = LinearLayoutManager(this)
        checkListTotalListLayoutManager.orientation = LinearLayoutManager.VERTICAL
        checkListRecyclerView.layoutManager = checkListTotalListLayoutManager
        checkListRecyclerView.adapter = checkListRecyclerViewAdapterClass

        addItemToListEditText = findViewById(R.id.check_list_fragment_item_enter_edit_text)
        addItemToListEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        addItemToListEditText.isSingleLine = true
        addItemToList()

        swipeToDeleteMethod()

    }

    private fun checkListClickListener(checkListModelClass: CheckListModelClass) {

        updatedItemOnList = checkListModelClass.checkListItemName
        updatedItemId = checkListModelClass.documentId
        showDialog()

    }

    private fun takeDataFromFireStore(){
        checkListTotalListArray.clear()
        collectionReference.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                for (document in documents){
                    val resultForCheckListItems: CheckListModelClass = document.toObject(CheckListModelClass::class.java)
                    checkListTotalListArray.add(resultForCheckListItems)
                }

                checkListRecyclerViewAdapterClass.submitList(checkListTotalListArray)
                checkListRecyclerViewAdapterClass.notifyDataSetChanged()
            }
        }
            .addOnFailureListener { exception ->
            }

    }

    private fun saveDataToFireStore(){
            collectionReference.document(documentId)
                .set(checkListModelClass, SetOptions.merge())
                .addOnSuccessListener {
                    takeDataFromFireStore()
                }


    }

    private fun addItemToList(){
        addItemToListEditText.onDone { addItemToListEditText.hideKeyboard() }

    }
    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun EditText.onDone(callback: () -> Unit) {
        imeOptions = EditorInfo.IME_ACTION_DONE
        maxLines = 1
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                // My action on done
                if(addItemToListEditText.text.toString() != "") {
                    documentId = collectionReference.document().id
                    checkListModelClass = CheckListModelClass(false, addItemToListEditText.text.toString(), documentId)
                    saveDataToFireStore()
                    addItemToListEditText.text.clear()
                }

                callback.invoke()
                true
            }
            false
        }
    }

    private fun swipeToDeleteMethod(){
        ItemTouchHelper (object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItemOnList: CheckListModelClass = checkListTotalListArray[viewHolder.adapterPosition]
                val deletedItemId = deletedItemOnList.documentId
                collectionReference.document(deletedItemId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Item deleted from the Shopping List", Toast.LENGTH_LONG).show()
                        takeDataFromFireStore()
                    }
                //val deletedItemPosition = viewHolder.adapterPosition

                checkListRecyclerViewAdapterClass.notifyItemRemoved(viewHolder.adapterPosition)

            }
        }).attachToRecyclerView(checkListRecyclerView)

    }

    fun showDialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Update Shopping List Item")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(updatedItemOnList)
        builder.setView(input)

        builder.setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
            updatedItemOnList = input.text.toString()
            collectionReference.document(updatedItemId).update("checkListItemName", updatedItemOnList)
                .addOnSuccessListener {
                    takeDataFromFireStore()
                    Toast.makeText(applicationContext, "Shopping List is Updated", Toast.LENGTH_LONG).show()
                }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }
}