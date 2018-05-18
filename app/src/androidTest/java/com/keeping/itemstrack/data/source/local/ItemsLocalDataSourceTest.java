package com.keeping.itemstrack.data.source.local;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration test for the {@link ItemsDataSource}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ItemsLocalDataSourceTest {

    private final static String TITLE = "title";

    private ItemsLocalDataSource mLocalDataSource;

    private ItemsDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ItemsDatabase.class)
                .build();
        ItemsDao tasksDao = mDatabase.itemDao();

        // Make sure that we're not keeping a reference to the wrong instance.
        ItemsLocalDataSource.clearInstance();
        mLocalDataSource = ItemsLocalDataSource.getInstance(new SingleExecutors(), tasksDao);
    }

    @After
    public void cleanUp() {
        mDatabase.close();
        ItemsLocalDataSource.clearInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveTask_retrievesTask() {
        // Given a new task
        final Item newTask = new Item(TITLE, "", "", "", "");

        // When saved into the persistent repository
        mLocalDataSource.saveItem(newTask);

        // Then the task can be retrieved from the persistent repository
        mLocalDataSource.getItem(newTask.getId(), new ItemsDataSource.GetItemCallback() {
            @Override
            public void onItemLoaded(Item task) {
                assertThat(task, is(newTask));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Initialize mock for the callback.
        ItemsDataSource.GetItemCallback callback = mock(ItemsDataSource.GetItemCallback.class);
        // Given a new task in the persistent repository
        final Item newTask = new Item(TITLE, "", "", "", "");
        mLocalDataSource.saveItem(newTask);


        // Then the task can be retrieved from the persistent repository and is complete
        mLocalDataSource.getItem(newTask.getId(), new ItemsDataSource.GetItemCallback() {
            @Override
            public void onItemLoaded(Item task) {
                assertThat(task, is(newTask));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }


    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new task in the persistent repository and a mocked callback
        Item newTask = new Item(TITLE, "", "", "", "");
        mLocalDataSource.saveItem(newTask);
        ItemsDataSource.LoadItemsCallback callback = mock(ItemsDataSource.LoadItemsCallback.class);

        // When all tasks are deleted
        mLocalDataSource.deleteAllItems();

        // Then the retrieved tasks is an empty list
        mLocalDataSource.getItems(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onItemsLoaded(anyList());
    }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new tasks in the persistent repository
        final Item newTask1 = new Item(TITLE, "", "", "", "");
        mLocalDataSource.saveItem(newTask1);
        final Item newTask2 = new Item(TITLE, "", "", "", "");
        mLocalDataSource.saveItem(newTask2);

        // Then the tasks can be retrieved from the persistent repository
        mLocalDataSource.getItems(new ItemsDataSource.LoadItemsCallback() {
            @Override
            public void onItemsLoaded(List<Item> tasks) {
                assertNotNull(tasks);
                assertTrue(tasks.size() >= 2);

                boolean newTask1IdFound = false;
                boolean newTask2IdFound = false;
                for (Item task : tasks) {
                    if (task.getId().equals(newTask1.getId())) {
                        newTask1IdFound = true;
                    }
                    if (task.getId().equals(newTask2.getId())) {
                        newTask2IdFound = true;
                    }
                }
                assertTrue(newTask1IdFound);
                assertTrue(newTask2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
