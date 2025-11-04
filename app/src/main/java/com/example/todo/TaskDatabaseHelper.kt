package com.example.todo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{

    //static object that applies to the class
    companion object{
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_NAME = "tasks"

        //Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DEADLINE = "deadline"
        private const val COLUMN_COLOR = "color"
        private const val COLUMN_IS_DONE = "is_done"
        private const val COLUMN_IMAGE_URI = "image_uri"
    }


    //called when database created first time
    //""" is used for multiline strings which will be the query
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_DEADLINE TEXT,
                $COLUMN_COLOR INTEGER,
                $COLUMN_IS_DONE INTEGER,
                $COLUMN_IMAGE_URI TEXT
            )
        """.trimIndent()
        //execSQL executes the query to create the database table
        db?.execSQL(createTableQuery)
    }

    //called when database needs to be upgraded
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    //CRUD operations
    //returns a Long data type which will be the row ID
    fun addTask(task: Task) : Long{
        val db = this.writableDatabase
        //creates ContentValues object that is a map holding key-value pairs (column name and data)
        //ContentValues object is populated with key(column name) and value(data)
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_DEADLINE, task.deadline)
            put(COLUMN_COLOR, task.colorResId)
            put(COLUMN_IS_DONE, if (task.isDone) 1 else 0)
            put(COLUMN_IMAGE_URI, task.imageUri)
        }
        //inserts the ContentValues object into the database table
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun deleteTask(taskId: Long) {
        val db = this.writableDatabase
        val whereClause = "$COLUMN_ID = ?" //matches the taskID provided
        val whereArgs = arrayOf(taskId.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }

    fun getAllTasks():List<Task>{
        val taskList = mutableListOf<Task>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val task = Task(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    deadline = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEADLINE)),
                    colorResId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                    isDone = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DONE)) == 1,
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
                )
                taskList.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return taskList
    }

    fun getTaskById(taskId: Long): Task? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COLUMN_ID = ?", arrayOf(taskId.toString()), null, null, null)
        var task: Task? = null
        if (cursor.moveToFirst()) {
            task = Task(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                deadline = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEADLINE)),
                colorResId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                isDone = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DONE)) == 1,
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
            )
        }
        cursor.close()
        db.close()
        return task
    }
//update task
    fun updateTask(task: Task){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_DEADLINE, task.deadline)
            put(COLUMN_COLOR, task.colorResId)
            put(COLUMN_IS_DONE, if (task.isDone) 1 else 0)
            put(COLUMN_IMAGE_URI, task.imageUri)
        }
    db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    db.close()
    }
}