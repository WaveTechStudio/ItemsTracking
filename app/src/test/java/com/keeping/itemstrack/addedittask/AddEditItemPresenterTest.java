package com.keeping.itemstrack.addedittask;

import com.keeping.itemstrack.addedititem.AddEditItemContract;
import com.keeping.itemstrack.addedititem.AddEditItemPresenter;
import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.data.source.ItemsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link AddEditItemPresenter}.
 */
public class AddEditItemPresenterTest {

    @Mock
    private ItemsRepository mTasksRepository;

    @Mock
    private AddEditItemContract.View mAddEditTaskView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<ItemsDataSource.GetItemCallback> mGetTaskCallbackCaptor;

    private AddEditItemPresenter mAddEditTaskPresenter;

    @Before
    public void setupMocksAndView() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditItemPresenter(
                null, mTasksRepository, mAddEditTaskView, true);

        // Then the presenter is set to the view
        verify(mAddEditTaskView).setPresenter(mAddEditTaskPresenter);
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditItemPresenter(
                null, mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to save a task
        mAddEditTaskPresenter.saveItem("New Task Title", "Some Task Description", "Some Task Cost", "Some Task Location", "");

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveItem(any(Item.class)); // saved to the model
        verify(mAddEditTaskView).showItemsList(); // shown in the UI
    }

    @Test
    public void saveTask_emptyTaskShowsErrorUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditItemPresenter(
                null, mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to save an empty task
        mAddEditTaskPresenter.saveItem("", "", "", "", "");

        // Then an empty not error is shown in the UI
        verify(mAddEditTaskView).showEmptyItemError();
    }

    @Test
    public void saveExistingTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditItemPresenter(
                "1", mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to save an existing task
        mAddEditTaskPresenter.saveItem("Existing Task Title", "Some Task Description", "Some Task Cost", "Some Task Location", "");

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveItem(any(Item.class)); // saved to the model
        verify(mAddEditTaskView).showItemsList(); // shown in the UI
    }

    @Test
    public void populateItem_callsRepoAndUpdatesView() {
        Item testItem = new Item("TITLE", "DESCRIPTION", "COST", "LOCATION", "");
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditItemPresenter(testItem.getId(),
                mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to populate an existing task
        mAddEditTaskPresenter.populateItem();

        // Then the task repository is queried and the view updated
        verify(mTasksRepository).getItem(eq(testItem.getId()), mGetTaskCallbackCaptor.capture());
        assertThat(mAddEditTaskPresenter.isDataMissing(), is(true));

        // Simulate callback
        mGetTaskCallbackCaptor.getValue().onItemLoaded(testItem);

        verify(mAddEditTaskView).setTitle(testItem.getTitle());
        verify(mAddEditTaskView).setDescription(testItem.getDescription());
        verify(mAddEditTaskView).setCost(testItem.getCost());
        verify(mAddEditTaskView).setLocation(testItem.getLocation());
        assertThat(mAddEditTaskPresenter.isDataMissing(), is(true));
    }
}
