package com.example.todo

data class Task(
    val id: Long=-1,
    val title: String,
    val deadline: String,
    val description: String,
    var isDone: Boolean = false,
    val colorResId: Int = R.color.meadow_beige // default task color
)