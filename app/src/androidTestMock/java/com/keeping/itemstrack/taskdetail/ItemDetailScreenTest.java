package com.keeping.itemstrack.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.keeping.itemstrack.R;
import com.keeping.itemstrack.TestUtils;
import com.keeping.itemstrack.data.FakeItemsRemoteDataSource;
import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsRepository;
import com.keeping.itemstrack.itemdetail.ItemDetailActivity;
import com.keeping.itemstrack.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ItemDetailScreenTest {

    private static String ITEM_TITLE = "Title";

    private static String ITEM_DESCRIPTION = "Description";
    private static String ITEM_COST = "Cost";
    private static String ITEM_LOCATION = "Location";
    private static String ITEM_IMAGE_PATH = "Image Path";

    /**
     * {@link Item} stub that is added to the fake service API layer.
     */
    private static Item ITEM = new Item(ITEM_TITLE, ITEM_DESCRIPTION, ITEM_COST, ITEM_LOCATION, ITEM_IMAGE_PATH);

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     * <p>
     * <p>
     * Sometimes an {@link Activity} requires a custom start {@link Intent} to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target Activity.
     */
    @Rule
    public ActivityTestRule<ItemDetailActivity> mTaskDetailActivityTestRule =
            new ActivityTestRule<>(ItemDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);


    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    private void loadTask() {
        startActivityWithWithStubbedTask(ITEM);
    }

    /**
     * Setup your test fixture with a fake task id. The {@link ItemDetailActivity} is started with
     * a particular task id, which is then loaded from the service API.
     * <p>
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    private void startActivityWithWithStubbedTask(Item task) {
        // Add a task stub to the fake service api layer.
        ItemsRepository.destroyInstance();
        FakeItemsRemoteDataSource.getInstance().addTasks(task);

        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        Intent startIntent = new Intent();
        startIntent.putExtra(ItemDetailActivity.EXTRA_TASK_ID, task.getId());
        mTaskDetailActivityTestRule.launchActivity(startIntent);
    }

    @Test
    public void itemDetails_DisplayedInUi() throws Exception {
        loadTask();
        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(ITEM_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(ITEM_DESCRIPTION)));
        onView(withId(R.id.task_detail_cost)).check(matches(withText(ITEM_COST)));
        onView(withId(R.id.task_detail_location)).check(matches(withText(ITEM_LOCATION)));
    }

    @Test
    public void orientationChange_menuAndTaskPersist() {
        loadTask();

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));

        TestUtils.rotateOrientation(mTaskDetailActivityTestRule.getActivity());

        // Check that the task is shown
        onView(withId(R.id.task_detail_title)).check(matches(withText(ITEM_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(ITEM_DESCRIPTION)));
        onView(withId(R.id.task_detail_cost)).check(matches(withText(ITEM_COST)));
        onView(withId(R.id.task_detail_location)).check(matches(withText(ITEM_LOCATION)));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

}
