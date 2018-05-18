package com.keeping.itemstrack.data.source.local;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.keeping.itemstrack.data.Item;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ItemsDaoTest {

    private static final Item TASK = new Item("title", "description", "cost", "location", "id");

    private ItemsDatabase mDatabase;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ItemsDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertTaskAndGetById() {
        // When inserting a task
        mDatabase.itemDao().insertItem(TASK);

        // When getting the task by id from the database
        Item loaded = mDatabase.itemDao().getItemById(TASK.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title", "description");
    }

    @Test
    public void insertTaskReplacesOnConflict() {
        //Given that a task is inserted
        mDatabase.itemDao().insertItem(TASK);

        // When a task with the same id is inserted
        Item newTask = new Item("title2", "description2", "cost2", "location2", "id");
        mDatabase.itemDao().insertItem(newTask);
        // When getting the task by id from the database
        Item loaded = mDatabase.itemDao().getItemById(TASK.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2");
    }

    @Test
    public void insertTaskAndGetTasks() {
        // When inserting a task
        mDatabase.itemDao().insertItem(TASK);

        // When getting the tasks from the database
        List<Item> tasks = mDatabase.itemDao().getItems();

        // There is only 1 task in the database
        assertThat(tasks.size(), is(1));
        // The loaded data contains the expected values
        assertTask(tasks.get(0), "id", "title", "description");
    }

    @Test
    public void updateTaskAndGetById() {
        // When inserting a task
        mDatabase.itemDao().insertItem(TASK);

        // When the task is updated
        Item updatedTask = new Item("title2", "description2", "cost2", "location2", "id");
        mDatabase.itemDao().updateItem(updatedTask);

        // When getting the task by id from the database
        Item loaded = mDatabase.itemDao().getItemById("id");

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2");
    }


    @Test
    public void deleteTaskByIdAndGettingTasks() {
        //Given a task inserted
        mDatabase.itemDao().insertItem(TASK);

        //When deleting a task by id
        mDatabase.itemDao().deleteItemById(TASK.getId());

        //When getting the tasks
        List<Item> tasks = mDatabase.itemDao().getItems();
        // The list is empty
        assertThat(tasks.size(), is(0));
    }

    @Test
    public void deleteTasksAndGettingTasks() {
        //Given a task inserted
        mDatabase.itemDao().insertItem(TASK);

        //When deleting all tasks
        mDatabase.itemDao().deleteItems();

        //When getting the tasks
        List<Item> tasks = mDatabase.itemDao().getItems();
        // The list is empty
        assertThat(tasks.size(), is(0));
    }


    private void assertTask(Item task, String id, String title,
                            String description) {
        assertThat(task, notNullValue());
        assertThat(task.getId(), is(id));
        assertThat(task.getTitle(), is(title));
        assertThat(task.getDescription(), is(description));
    }
}
