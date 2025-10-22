package com.example.todo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tasks"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DEADLINE = "deadline"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_IS_DONE = "isDone"
        private const val COLUMN_COLOR = "color"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DEADLINE TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_IS_DONE INTEGER,
                $COLUMN_COLOR INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertTask(task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DEADLINE, task.deadline)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_IS_DONE, if (task.isDone) 1 else 0)
            put(COLUMN_COLOR, task.colorResId)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    fun getAllTasks(): MutableList<Task> {
        val taskList = mutableListOf<Task>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val deadline = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEADLINE))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val isDone = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DONE)) == 1
                val color = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR))

                val task = Task(title, deadline, description, isDone, color)
                taskList.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return taskList
    }

    fun updateTaskStatus(title: String, isDone: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_DONE, if (isDone) 1 else 0)
        }
        db.update(TABLE_NAME, values, "$COLUMN_TITLE=?", arrayOf(title))
        db.close()
    }

    fun clearAll() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }
}
