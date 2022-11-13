package com.example.shoppinglist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CheckListRecyclerViewAdapterClass (
    var checkList: List<CheckListModelClass>,
    private val clickListener: (CheckListModelClass) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var checkListTotalList: List<CheckListModelClass> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val myView = LayoutInflater.from(parent.context).inflate(R.layout.check_list_rec_view_model_layout, parent, false)

        return CheckListViewHolder(myView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as CheckListViewHolder).bind(checkListTotalList[position], clickListener)

    }

    override fun getItemCount(): Int {
        return checkListTotalList.size
    }

    fun submitList(checkList: List<CheckListModelClass>) {
        checkListTotalList = checkList
    }

    fun updateList(checkList: List<CheckListModelClass>) {
        checkListTotalList = checkList
        notifyDataSetChanged()
    }

    class CheckListViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val checkListCheckBox: CheckBox = itemView.findViewById(R.id.check_list_rec_view_model_layout_checkBox)
        private val checkListTextView: TextView = itemView.findViewById(R.id.check_list_rec_view_model_layout_textView)
        private val mainActivity: MainActivity = MainActivity()

        fun bind(
            checkListModelClass: CheckListModelClass,
            clickListener: (CheckListModelClass) -> Unit
        ) {

            checkListTextView.text = checkListModelClass.checkListItemName

            val checkListCheckBoxPosition = checkListModelClass.checkListItemCheckListPosition
            checkListCheckBox.isChecked = checkListCheckBoxPosition

            /*checkListTextView.setOnClickListener {

                    //mainActivity.saveDataToFireStore()// or simply if (isChecked) {..}
                    Toast.makeText(itemView.context, "checkbox is checked", Toast.LENGTH_LONG).show()

            }*/

            checkListTextView.setOnClickListener { clickListener(checkListModelClass) }
        }
    }

}