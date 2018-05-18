package com.keeping.itemstrack.tasks;

import com.google.common.collect.Lists;
import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsDataSource.LoadItemsCallback;
import com.keeping.itemstrack.data.source.ItemsRepository;
import com.keeping.itemstrack.items.ItemsContract;
import com.keeping.itemstrack.items.ItemsPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link ItemsPresenter}
 */
public class ItemsPresenterTest {

    private static List<Item> TASKS;

    @Mock
    private ItemsRepository mTasksRepository;

    @Mock
    private ItemsContract.View mTasksView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LoadItemsCallback> mLoadTasksCallbackCaptor;

    private ItemsPresenter mTasksPresenter;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksPresenter = new ItemsPresenter(mTasksRepository, mTasksView);

        // We start the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Item("Title1", "Description1", "100", "Location1", "path"),
                new Item("Title2", "Description2", "200", "Location2", "path"),
                new Item("Title3", "Description3", "300", "Location3", "path"));
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mTasksPresenter = new ItemsPresenter(mTasksRepository, mTasksView);

        // Then the presenter is set to the view
        verify(mTasksView).setPresenter(mTasksPresenter);
    }

    @Test
    public void loadAllTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksPresenter with initialized tasks
        // When loading of Tasks is requested
        mTasksPresenter.loadItems(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getItems(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onItemsLoaded(TASKS);

        // Then progress indicator is shown
        InOrder inOrder = inOrder(mTasksView);
        inOrder.verify(mTasksView).setLoadingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        inOrder.verify(mTasksView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showItems(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 3);
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksPresenter with initialized tasks
        // When loading of Tasks is requested
        mTasksPresenter.loadItems(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getItems(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onItemsLoaded(TASKS);

        // Then progress indicator is hidden and active tasks are shown in UI
        verify(mTasksView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showItems(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 3);
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksPresenter with initialized tasks
        // When loading of Tasks is requested
        mTasksPresenter.loadItems(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getItems(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onItemsLoaded(TASKS);

        // Then progress indicator is hidden and completed tasks are shown in UI
        verify(mTasksView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showItems(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 3);
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {
        // When adding a new task
        mTasksPresenter.addNewItem();

        // Then add task UI is shown
        verify(mTasksView).showAddItem();
    }

    @Test
    public void clickOnTask_ShowsDetailUi() {
        // Given a stubbed active task
        Item requestedTask = new Item("Details Requested", "For this item", "200", "location", "path");

        // When open task details is requested
        mTasksPresenter.openItemDetails(requestedTask);

        // Then task detail UI is shown
        verify(mTasksView).showItemDetailsUi(any(String.class));
    }

    @Test
    public void unavailableTasks_ShowsError() {
        // When tasks are loaded
        mTasksPresenter.loadItems(true);

        // And the tasks aren't available in the repository
        verify(mTasksRepository).getItems(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        verify(mTasksView).showLoadingItemsError();
    }
}
