package com.keeping.itemstrack.addedititem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddEditItemFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddEditItemPresenter implements AddEditItemContract.Presenter,
        ItemsDataSource.GetItemCallback {

    @NonNull
    private final ItemsDataSource mTasksRepository;

    @NonNull
    private final AddEditItemContract.View mAddTaskView;

    @Nullable
    private String mItemId;

    private boolean mIsDataMissing;


    /**
     * Creates a presenter for the add/edit view.
     *
     * @param itemId                 ID of the task to edit or null for a new task
     * @param tasksRepository        A repository of data for tasks
     * @param addTaskView            The add/edit view
     * @param shouldLoadDataFromRepo Whether data needs to be loaded or not (for config changes)
     */
    public AddEditItemPresenter(@Nullable String itemId, @NonNull ItemsDataSource tasksRepository,
                                @NonNull AddEditItemContract.View addTaskView, boolean shouldLoadDataFromRepo) {
        mItemId = itemId;
        mTasksRepository = checkNotNull(tasksRepository);
        mAddTaskView = checkNotNull(addTaskView);
        mIsDataMissing = shouldLoadDataFromRepo;
        mAddTaskView.setPresenter(this);
    }

    /**
     * Save or update item on the basis of item id
     *
     * @param title       Title of item
     * @param description Description of item
     * @param cost        Cost of item
     * @param location    Location of item
     * @param imagePath   Image Path of item
     */
    @Override
    public void saveItem(String title, String description, String cost, String location, String imagePath) {
        if (isNewItem()) {
            createItem(title, description, cost, location, imagePath);
        } else {
            updateItem(title, description, cost, location, imagePath);
        }
    }


    /**
     * Populate item on the basis of item id
     */
    @Override
    public void populateItem() {
        if (isNewItem()) {
            throw new RuntimeException("populateItem() was called but item is new.");
        }
        mTasksRepository.getItem(mItemId, this);
    }

    /**
     * If data is empty returns true otherwise returns false.
     */
    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }


    /**
     * Start method of presenter to initiate to populate items
     */
    @Override
    public void start() {
        if (!isNewItem() && mIsDataMissing) {
            populateItem();
        }
    }

    /**
     * Item is loaded from database so assign values to related views
     *
     * @param item Loaded item
     */
    @Override
    public void onItemLoaded(Item item) {
        // The view may not be able to handle UI updates anymore
        mAddTaskView.setTitle(item.getTitle());
        mAddTaskView.setDescription(item.getDescription());
        mAddTaskView.setCost(item.getCost());
        mAddTaskView.setLocation(item.getLocation());
        mAddTaskView.setImagePath(item.getImagePath());
    }

    /**
     * Item is not available or data is empty
     */
    @Override
    public void onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        mAddTaskView.showEmptyItemError();
    }


    /**
     * If item is new returns true otherwise false
     * If item id is null it returns true otherwise false
     */
    private boolean isNewItem() {
        return mItemId == null;
    }


    /**
     * Create item
     *
     * @param title       Title of item
     * @param description Description of item
     * @param cost        Cost of item
     * @param location    Location of item
     * @param imagePath   Image Path of item
     */
    private void createItem(String title, String description, String cost, String location, String imagePath) {
        Item newTask = new Item(title, description, cost, location, imagePath);
        if (newTask.isEmpty()) {
            mAddTaskView.showEmptyItemError();
        } else {
            mTasksRepository.saveItem(newTask);
            mAddTaskView.showItemsList();
        }
    }

    /**
     * Update item
     *
     * @param title       Title of item
     * @param description Description of item
     * @param cost        Cost of item
     * @param location    Location of item
     * @param imagePath   Image Path of item
     */
    private void updateItem(String title, String description, String cost, String location, String imagePath) {
        if (isNewItem()) {
            throw new RuntimeException("updateItem() was called but task is new.");
        }
        mTasksRepository.saveItem(new Item(title, description, cost, location, imagePath, mItemId));
        mAddTaskView.showItemsList(); // After an edit, go back to the list.
    }

    /**
     * Capture image from Camera
     *
     * @param context Reference to the activity
     */
    @Override
    public void attachImageFromCamera(Context context) {
        pickImageFromSource((Sources.CAMERA), context);
    }


    /**
     * Attach image from Gallery
     *
     * @param context Reference to the activity
     */
    @Override
    public void attachImageFromGallery(Context context) {
        pickImageFromSource((Sources.GALLERY), context);
    }


    /**
     * Attach image from Gallery
     *
     * @param context Reference to the activity
     * @param source  Source is either Camera or Gallery
     */
    @SuppressLint("CheckResult")
    private void pickImageFromSource(Sources source, Context context) {
        RxImagePicker.with(context).requestImage(source)
                .flatMap(uri -> {
                    return RxImageConverters.uriToFile(context, uri, createTempFile(context));
                })
                .subscribe(this::onImagePicked, throwable -> Toast.makeText(context, String.format("Error: %s", throwable), Toast.LENGTH_LONG).show());
    }


    /**
     * Image successfully picked from Camera/Gallery
     *
     * @param result Image path after getting from Camera/Gallery
     */
    private void onImagePicked(Object result) {
        mAddTaskView.setImagePath(result.toString());

    }

    private File createTempFile(Context context) {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + "_image.jpeg");
    }

}
