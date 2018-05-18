package com.keeping.itemstrack.tasks;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.keeping.itemstrack.Injection;
import com.keeping.itemstrack.R;
import com.keeping.itemstrack.TestUtils;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.items.ItemsActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkArgument;
import static com.keeping.itemstrack.TestUtils.getCurrentActivity;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksScreenTest {

    private final static String TITLE1 = "TITLE1";

    private final static String DESCRIPTION = "DESCR";

    private final static String COST = "200";

    private final static String LOCATION = "Location";

    private final static String TITLE2 = "TITLE2";

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<ItemsActivity> mTasksActivityTestRule =
            new ActivityTestRule<ItemsActivity>(ItemsActivity.class) {

                /**
                 * To avoid a long list of tasks and the need to scroll through the list to find a
                 * task, we call {@link ItemsDataSource#deleteAllItems()} before each test.
                 */
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    // Doing this in @Before generates a race condition.
                    Injection.provideTasksRepository(InstrumentationRegistry.getTargetContext())
                            .deleteAllItems();
                }
            };

    /**
     * A custom {@link Matcher} which matches an item in a {@link ListView} by its text.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link ListView}
     * <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(ListView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void clickAddTaskButton_opensAddTaskUi() {
        // Click on the add task button
        onView(withId(R.id.fab_add_item)).perform(click());

        // Check if the add task screen is displayed
        onView(withId(R.id.add_item_title)).check(matches(isDisplayed()));
    }

    @Test
    public void editTask() throws Exception {
        // First add a task
        createTask(TITLE1, DESCRIPTION, COST, LOCATION);

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click());

        String editTaskTitle = TITLE2;
        String editTaskDescription = "New Description";

        // Edit task title and description
        onView(withId(R.id.add_item_title)).perform(replaceText(editTaskTitle));
        onView(withId(R.id.add_item_cost)).perform(replaceText(COST));
        onView(withId(R.id.add_item_location)).perform(replaceText(LOCATION));
        onView(withId(R.id.add_item_description)).perform(replaceText(editTaskDescription),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.menu_add_item)).perform(click());

        // Verify task is displayed on screen in the task list.
        onView(withItemText(editTaskTitle)).check(matches(isDisplayed()));

        // Verify previous task is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void addTaskToTasksList() throws Exception {
        createTask(TITLE1, DESCRIPTION, COST, LOCATION);

        // Verify task is displayed on screen
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void showAllTasks() {
        // Add 2 active tasks
        createTask(TITLE1, DESCRIPTION, COST, LOCATION);
        createTask(TITLE2, DESCRIPTION, COST, LOCATION);

        //Verify that all our tasks are shown
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void createOneTask_deleteTask() {

        // Add active task
        createTask(TITLE1, DESCRIPTION, COST, LOCATION);

        // Open it in details view
        onView(withText(TITLE1)).perform(click());

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify it was deleted
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createTwoTasks_deleteOneTask() {
        // Add 2 active tasks
        createTask(TITLE1, DESCRIPTION, COST, LOCATION);
        createTask(TITLE2, DESCRIPTION, COST, LOCATION);

        // Open the second task in details view
        onView(withText(TITLE2)).perform(click());

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify only one task was deleted
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(TITLE2)).check(doesNotExist());
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_NoDuplicate() throws IllegalStateException {
        // Add a completed task
        createTask(TITLE1, DESCRIPTION, COST, LOCATION);

        // Open the task in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click());

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Edit task title and description
        onView(withId(R.id.add_item_title))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_item_description)).perform(replaceText(DESCRIPTION),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.menu_add_item)).perform(click());

        // Verify task is displayed on screen in the task list.
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));

        // Verify previous task is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    private void createTask(String title, String description, String cost, String location) {
        // Click on the add task button
        onView(withId(R.id.fab_add_item)).perform(click());

        // Add task title and description
        onView(withId(R.id.add_item_title)).perform(typeText(title),
                closeSoftKeyboard()); // Type new task title

        onView(withId(R.id.add_item_description)).perform(typeText(description));

        onView(withId(R.id.add_item_cost)).perform(typeText(cost));

        onView(withId(R.id.add_item_location)).perform(typeText(location),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.menu_add_item)).perform(click());
    }

    private String getText(int stringId) {
        return mTasksActivityTestRule.getActivity().getResources().getString(stringId);
    }

    private String getToolbarNavigationContentDescription() {
        return TestUtils.getToolbarNavigationContentDescription(
                mTasksActivityTestRule.getActivity(), R.id.toolbar);
    }
}
