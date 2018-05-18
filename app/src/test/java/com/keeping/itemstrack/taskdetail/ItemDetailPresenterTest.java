package com.keeping.itemstrack.taskdetail;

import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.data.source.ItemsRepository;
import com.keeping.itemstrack.itemdetail.ItemDetailContract;
import com.keeping.itemstrack.itemdetail.ItemDetailPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link ItemDetailPresenter}
 */
public class ItemDetailPresenterTest {

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";
    public static final String COST_TEST = "cost";
    public static final String LOCATION_TEST = "location";
    public static final String IMAGE_TEST = "path";

    public static final String INVALID_TASK_ID = "";

    public static final Item ITEM = new Item(TITLE_TEST, DESCRIPTION_TEST, COST_TEST, LOCATION_TEST, IMAGE_TEST);

    @Mock
    private ItemsRepository mItemRepository;

    @Mock
    private ItemDetailContract.View mItemDetailView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<ItemsDataSource.GetItemCallback> mGetItemCallbackCaptor;

    private ItemDetailPresenter mItemDetailPresenter;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mItemDetailPresenter = new ItemDetailPresenter(
                ITEM.getId(), mItemRepository, mItemDetailView);

        // Then the presenter is set to the view
        verify(mItemDetailView).setPresenter(mItemDetailPresenter);
    }

    @Test
    public void getActiveItemFromRepositoryAndLoadIntoView() {
        // When tasks presenter is asked to open a task
        mItemDetailPresenter = new ItemDetailPresenter(
                ITEM.getId(), mItemRepository, mItemDetailView);
        mItemDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mItemRepository).getItem(eq(ITEM.getId()), mGetItemCallbackCaptor.capture());
        InOrder inOrder = inOrder(mItemDetailView);
        inOrder.verify(mItemDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetItemCallbackCaptor.getValue().onItemLoaded(ITEM); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(mItemDetailView).setLoadingIndicator(false);
        verify(mItemDetailView).showTitle(TITLE_TEST);
        verify(mItemDetailView).showDescription(DESCRIPTION_TEST);
    }

    @Test
    public void getCompletedItemFromRepositoryAndLoadIntoView() {
        mItemDetailPresenter = new ItemDetailPresenter(
                ITEM.getId(), mItemRepository, mItemDetailView);
        mItemDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mItemRepository).getItem(
                eq(ITEM.getId()), mGetItemCallbackCaptor.capture());
        InOrder inOrder = inOrder(mItemDetailView);
        inOrder.verify(mItemDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetItemCallbackCaptor.getValue().onItemLoaded(ITEM); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(mItemDetailView).setLoadingIndicator(false);
        verify(mItemDetailView).showTitle(TITLE_TEST);
        verify(mItemDetailView).showDescription(DESCRIPTION_TEST);
    }

    @Test
    public void getUnknownItemFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid Item ID.
        mItemDetailPresenter = new ItemDetailPresenter(
                INVALID_TASK_ID, mItemRepository, mItemDetailView);
        mItemDetailPresenter.start();
        verify(mItemDetailView).showMissingItem();
    }

    @Test
    public void deleteItem() {
        // Given an initialized TaskDetailPresenter with stubbed task
        Item item = new Item(TITLE_TEST, DESCRIPTION_TEST, COST_TEST, LOCATION_TEST, IMAGE_TEST);

        // When the deletion of a task is requested
        mItemDetailPresenter = new ItemDetailPresenter(
                item.getId(), mItemRepository, mItemDetailView);
        mItemDetailPresenter.deleteItem();

        // Then the repository and the view are notified
        verify(mItemRepository).deleteItem(item.getId());
        verify(mItemDetailView).showItemDeleted();
    }
}
