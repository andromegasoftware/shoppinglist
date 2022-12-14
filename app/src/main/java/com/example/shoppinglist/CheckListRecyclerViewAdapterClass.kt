package com.example.shoppinglist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CheckListRecyclerViewAdapterClass (
    var checkList: List<CheckListModelClass>,
    private val clickListener: (CheckListModelClass) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var checkListTotalList: List<CheckListModelClass> = ArrayList()
    private lateinit var firebaseFireStore: FirebaseFirestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val myView = LayoutInflater.from(parent.context).inflate(R.layout.check_list_rec_view_model_layout, parent, false)

        firebaseFireStore = FirebaseFirestore.getInstance()

        return CheckListViewHolder(myView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as CheckListViewHolder).bind(checkListTotalList[position], clickListener)
        holder.setIsRecyclable(false)
        val documentId = checkListTotalList[position].documentId
        holder.checkListCheckBox.setOnCheckedChangeListener { view, isChecked ->
            if (view.isChecked) {
                checkListTotalList[position].checkListItemCheckListPosition = view.isChecked
                firebaseFireStore.collection("shoppingList").document(documentId)
                    .update("checkListItemCheckListPosition", checkListTotalList[position].checkListItemCheckListPosition)
            } else {
                checkListTotalList[position].checkListItemCheckListPosition = view.isChecked
                firebaseFireStore.collection("shoppingList").document(documentId)
                    .update("checkListItemCheckListPosition", checkListTotalList[position].checkListItemCheckListPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return checkListTotalList.size
    }

    fun submitList(checkList: List<CheckListModelClass>) {
        checkListTotalList = checkList
    }

    class CheckListViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val checkListCheckBox: CheckBox = itemView.findViewById(R.id.check_list_rec_view_model_layout_checkBox)
        private val checkListTextView: TextView = itemView.findViewById(R.id.check_list_rec_view_model_layout_textView)
        private val checkListCardView: CardView = itemView.findViewById(R.id.check_list_rec_view_model_layout_card_view)

        fun bind(
            checkListModelClass: CheckListModelClass,
            clickListener: (CheckListModelClass) -> Unit
        ) {
            checkListTextView.text = checkListModelClass.checkListItemName
            val checkListCheckBoxPosition = checkListModelClass.checkListItemCheckListPosition
            checkListCheckBox.isChecked = checkListCheckBoxPosition

            checkListCardView.setOnClickListener { clickListener(checkListModelClass) }
        }
    }

}