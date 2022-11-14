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

        //handle click listener
        //Toast.makeText(this, checkListModelClass.checkListItemName, Toast.LENGTH_LONG).show()

    }

    private fun takeDataFromFireStore(){
        checkListTotalListArray.clear()
        collectionReference.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                for (document in documents){
                    val resultForCheckListItems: CheckListModelClass = document.toObject(CheckListModelClass::class.java)
                    checkListTotalListArray.add(resultForCheckListItems)
                    //Log.w("listen_data", document.id)
                }

                checkListRecyclerViewAdapterClass.submitList(checkListTotalListArray)
                checkListRecyclerViewAdapterClass.notifyDataSetChanged()
            }
        }
            .addOnFailureListener { exception ->
                //Log.w("listen_data", "Error getting documents: ", exception)
            }

    }

    private fun saveDataToFireStore(){
            collectionReference.document(documentId)
                .set(checkListModelClass, SetOptions.merge())
                .addOnSuccessListener {
                    takeDataFromFireStore()
                    //Log.d("TAG", "DocumentSnapshot successfully written!")
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
        // These lines optional if you don't want to set in Xml
        imeOptions = EditorInfo.IME_ACTION_DONE
        maxLines = 1
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                // Your action on done
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
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                val deletedItemOnList: CheckListModelClass = checkListTotalListArray[viewHolder.adapterPosition]
                val deletedItemId = deletedItemOnList.documentId
                collectionReference.document(deletedItemId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Item deleted from the Shopping List", Toast.LENGTH_LONG).show()
                        takeDataFromFireStore()
                    }
                // below line is to get the position
                // of the item at that position.
                val deletedItemPosition = viewHolder.adapterPosition

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                //courseList.removeAt(viewHolder.adapterPosition)

                // below line is to notify our item is removed from adapter.
                checkListRecyclerViewAdapterClass.notifyItemRemoved(viewHolder.adapterPosition)

            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(checkListRecyclerView)

    }

    fun showDialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Update Shopping List Item")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.hint = "Enter Text"
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(updatedItemOnList)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
            // Here you get get input text from the Edittext
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