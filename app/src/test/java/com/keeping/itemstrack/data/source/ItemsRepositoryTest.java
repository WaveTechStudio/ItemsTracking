package com.keeping.itemstrack.data.source;

import com.google.common.collect.Lists;
import com.keeping.itemstrack.data.Item;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class ItemsRepositoryTest {

    private final static String ITEM_TITLE = "title";

    private final static String ITEM_TITLE2 = "title2";

    private final static String ITEM_TITLE3 = "title3";

    private static List<Item> ITEM = Lists.newArrayList(new Item("Title1", "Description1", "100", "Item Location", "path"),
            new Item("Title2", "Description2", "200", "Item Location", "path"));

    private ItemsRepository mItemsRepository;

    @Mock
    private ItemsDataSource mItemsRemoteDataSource;

    @Mock
    private ItemsDataSource mItemsLocalDataSource;

    @Mock
    private ItemsDataSource.GetItemCallback mGetItemsCallback;

    @Mock
    private ItemsDataSource.LoadItemsCallback mLoadItemsCallback;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<ItemsDataSource.LoadItemsCallback> mItemsCallbackCaptor;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<ItemsDataSource.GetItemCallback> mItemCallbackCaptor;

    @Before
    public void setupItemsRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mItemsRepository = ItemsRepository.getInstance(
                mItemsRemoteDataSource, mItemsLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        ItemsRepository.destroyInstance();
    }

    @Test
    public void getItems_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the tasks repository
        twoItemsLoadCallsToRepository(mLoadItemsCallback);

        // Then tasks were only requested once from Service API
        verify(mItemsRemoteDataSource).getItems(any(ItemsDataSource.LoadItemsCallback.class));
    }

    @Test
    public void getItems_requestsAllItemsFromLocalDataSource() {
        // When tasks are requested from the tasks repository
        mItemsRepository.getItems(mLoadItemsCallback);

        // Then tasks are loaded from the local data source
        verify(mItemsLocalDataSource).getItems(any(ItemsDataSource.LoadItemsCallback.class));
    }

    @Test
    public void saveItem_savesItemToServiceAPI() {
        // Given a stub task with title and description
        Item newItem = new Item(ITEM_TITLE, "Some Item Description", "Some Item Cost", "Some Item Location", "path");

        // When a task is saved to the tasks repository
        mItemsRepository.saveItem(newItem);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mItemsRemoteDataSource).saveItem(newItem);
        verify(mItemsLocalDataSource).saveItem(newItem);
        assertThat(mItemsRepository.mCachedItems.size(), is(1));
    }

    @Test
    public void getItem_requestsSingleItemFromLocalDataSource() {
        // When a task is requested from the tasks repository
        mItemsRepository.getItem(ITEM_TITLE, mGetItemsCallback);

        // Then the task is loaded from the database
        verify(mItemsLocalDataSource).getItem(eq(ITEM_TITLE), any(
                ItemsDataSource.GetItemCallback.class));
    }

    @Test
    public void deleteAllItems_deleteItemsToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        Item newItem = new Item(ITEM_TITLE, "Some Item Description", "Some Item Cost", "Some Item Location", "path");
        mItemsRepository.saveItem(newItem);
        Item newItem2 = new Item(ITEM_TITLE2, "Some Item Description", "Some Item Cost", "Some Item Location", "path");
        mItemsRepository.saveItem(newItem2);
        Item newItem3 = new Item(ITEM_TITLE3, "Some Item Description", "Some Item Cost", "Some Item Location", "path");
        mItemsRepository.saveItem(newItem3);

        // When all tasks are deleted to the tasks repository
        mItemsRepository.deleteAllItems();

        // Verify the data sources were called
        verify(mItemsRemoteDataSource).deleteAllItems();
        verify(mItemsLocalDataSource).deleteAllItems();

        assertThat(mItemsRepository.mCachedItems.size(), is(0));
    }

    @Test
    public void deleteTask_deleteTaskToServiceAPIRemovedFromCache() {
        // Given a task in the repository
        Item newTask = new Item(ITEM_TITLE, "Some Item Description", "Some Item Cost", "Some Task Location", "path");
        mItemsRepository.saveItem(newTask);
        assertThat(mItemsRepository.mCachedItems.containsKey(newTask.getId()), is(true));

        // When deleted
        mItemsRepository.deleteItem(newTask.getId());

        // Verify the data sources were called
        verify(mItemsRemoteDataSource).deleteItem(newTask.getId());
        verify(mItemsLocalDataSource).deleteItem(newTask.getId());

        // Verify it's removed from repository
        assertThat(mItemsRepository.mCachedItems.containsKey(newTask.getId()), is(false));
    }

    @Test
    public void getTasksWithDirtyCache_tasksAreRetrievedFromRemote() {
        // When calling getItems in the repository with dirty cache
        mItemsRepository.refreshItems();
        mItemsRepository.getItems(mLoadItemsCallback);

        // And the remote data source has data available
        setItemsAvailable(mItemsRemoteDataSource, ITEM);

        // Verify the tasks from the remote data source are returned, not the local
        verify(mItemsLocalDataSource, never()).getItems(mLoadItemsCallback);
        verify(mLoadItemsCallback).onItemsLoaded(ITEM);
    }

    @Test
    public void getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When calling getItems in the repository
        mItemsRepository.getItems(mLoadItemsCallback);

        // And the local data source has no data available
        setItemsNotAvailable(mItemsLocalDataSource);

        // And the remote data source has data available
        setItemsAvailable(mItemsRemoteDataSource, ITEM);

        // Verify the tasks from the local data source are returned
        verify(mLoadItemsCallback).onItemsLoaded(ITEM);
    }

    @Test
    public void getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getItems in the repository
        mItemsRepository.getItems(mLoadItemsCallback);

        // And the local data source has no data available
        setItemsNotAvailable(mItemsLocalDataSource);

        // And the remote data source has no data available
        setItemsNotAvailable(mItemsRemoteDataSource);

        // Verify no data is returned
        verify(mLoadItemsCallback).onDataNotAvailable();
    }

    @Test
    public void getTaskWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a task id
        final String taskId = "123";

        // When calling getItem in the repository
        mItemsRepository.getItem(taskId, mGetItemsCallback);

        // And the local data source has no data available
        setItemNotAvailable(mItemsLocalDataSource, taskId);

        // And the remote data source has no data available
        setItemNotAvailable(mItemsRemoteDataSource, taskId);

        // Verify no data is returned
        verify(mGetItemsCallback).onDataNotAvailable();
    }

    @Test
    public void getTasks_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mItemsRepository.refreshItems();

        // When calling getItems in the repository
        mItemsRepository.getItems(mLoadItemsCallback);

        // Make the remote data source return data
        setItemsAvailable(mItemsRemoteDataSource, ITEM);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mItemsLocalDataSource, times(ITEM.size())).saveItem(any(Item.class));
    }

    /**
     * Convenience method that issues two calls to the tasks repository
     */
    private void twoItemsLoadCallsToRepository(ItemsDataSource.LoadItemsCallback callback) {
        // When tasks are requested from repository
        mItemsRepository.getItems(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mItemsLocalDataSource).getItems(mItemsCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mItemsCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mItemsRemoteDataSource).getItems(mItemsCallbackCaptor.capture());

        // Trigger callback so tasks are cached
        mItemsCallbackCaptor.getValue().onItemsLoaded(ITEM);

        mItemsRepository.getItems(callback); // Second call to API
    }

    private void setItemsNotAvailable(ItemsDataSource dataSource) {
        verify(dataSource).getItems(mItemsCallbackCaptor.capture());
        mItemsCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setItemsAvailable(ItemsDataSource dataSource, List<Item> items) {
        verify(dataSource).getItems(mItemsCallbackCaptor.capture());
        mItemsCallbackCaptor.getValue().onItemsLoaded(items);
    }

    private void setItemNotAvailable(ItemsDataSource dataSource, String itemId) {
        verify(dataSource).getItem(eq(itemId), mItemCallbackCaptor.capture());
        mItemCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setItemAvailable(ItemsDataSource dataSource, Item item) {
        verify(dataSource).getItem(eq(item.getId()), mItemCallbackCaptor.capture());
        mItemCallbackCaptor.getValue().onItemLoaded(item);
    }
}
