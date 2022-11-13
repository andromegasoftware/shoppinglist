package com.example.shoppinglist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button




class EmptyShoppingListFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_empty_shopping_list, container, false)

        val createNewShoppingListButton : Button = view.findViewById(R.id.empty_list_fragment_new_list_button)
        createNewShoppingListButton.setOnClickListener {

        }

        return view
    }

}