package com.keeping.itemstrack.items;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.keeping.itemstrack.R;
import com.keeping.itemstrack.addedititem.AddEditItemActivity;
import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.itemdetail.ItemDetailActivity;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Item}s. User can choose to view all, active or completed items.
 */
public class ItemsFragment extends Fragment implements ItemsContract.View {

    private ItemsContract.Presenter mPresenter;

    private ItemsAdapter mListAdapter;

    private View mNoItemsView;

    private ImageView mNoItemIcon;

    private TextView mNoItemMainView;

    private TextView mNoItemAddView;

    private LinearLayout mItemsView;

    public ItemsFragment() {
        // Requires empty public constructor
    }

    public static ItemsFragment newInstance() {
        return new ItemsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new ItemsAdapter(new ArrayList<Item>(0), mItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull ItemsContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.items_fragment, container, false);

        // Set up items view
        ListView listView = (ListView) root.findViewById(R.id.items_list);
        listView.setAdapter(mListAdapter);
        mItemsView = (LinearLayout) root.findViewById(R.id.itemsLL);

        // Set up  no items view
        mNoItemsView = root.findViewById(R.id.noItems);
        mNoItemIcon = (ImageView) root.findViewById(R.id.noItemsIcon);
        mNoItemMainView = (TextView) root.findViewById(R.id.noItemsMain);
        mNoItemAddView = (TextView) root.findViewById(R.id.noItemsAdd);
        mNoItemAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItem();
            }
        });

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_item);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.addNewItem();
            }
        });

        // Set up progress indicator
        final ScrollSwipeRefreshLayout swipeRefreshLayout =
                (ScrollSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadItems(false);
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sync:
                mPresenter.syncItemsWithCloud();
                break;
            case R.id.menu_refresh:
                mPresenter.loadItems(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.items_fragment_menu, menu);
    }

    /**
     * Listener for clicks on items in the ListView.
     */
    ItemItemListener mItemListener = new ItemItemListener() {
        @Override
        public void onItemClick(Item clickedItem) {
            mPresenter.openItemDetails(clickedItem);
        }
    };

    @Override
    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showItems(List<Item> items) {
        mListAdapter.replaceData(items);

        mItemsView.setVisibility(View.VISIBLE);
        mNoItemsView.setVisibility(View.GONE);
    }

    @Override
    public void showNoItems() {
        showNoItemsViews(
                getResources().getString(R.string.no_items_all),
                R.drawable.ic_assignment_turned_in_24dp,
                false
        );
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_item_message));
    }

    @Override
    public void showSuccessfullySyncingItems() {
        showMessage(getString(R.string.successfully_synced_items));
    }

    @Override
    public void showGeneralError(String message) {
        showMessage(message);
    }

    private void showNoItemsViews(String mainText, int iconRes, boolean showAddView) {
        mItemsView.setVisibility(View.GONE);
        mNoItemsView.setVisibility(View.VISIBLE);

        mNoItemMainView.setText(mainText);
        mNoItemIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoItemAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showAddItem() {
        Intent intent = new Intent(getContext(), AddEditItemActivity.class);
        startActivityForResult(intent, AddEditItemActivity.REQUEST_ADD_TASK);
    }

    @Override
    public void showItemDetailsUi(String taskId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        Intent intent = new Intent(getContext(), ItemDetailActivity.class);
        intent.putExtra(ItemDetailActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    @Override
    public void showLoadingItemsError() {
        showMessage(getString(R.string.loading_items_error));
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    private static class ItemsAdapter extends BaseAdapter {

        private List<Item> mItems;
        private ItemItemListener mItemListener;

        public ItemsAdapter(List<Item> items, ItemItemListener itemListener) {
            setList(items);
            mItemListener = itemListener;
        }

        public void replaceData(List<Item> items) {
            setList(items);
            notifyDataSetChanged();
        }

        private void setList(List<Item> items) {
            mItems = checkNotNull(items);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Item getItem(int i) {
            return mItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.listitem_item, viewGroup, false);
            }

            final Item item = getItem(i);

            ImageView list_item_image = (ImageView) rowView.findViewById(R.id.list_item_image);
            TextView titleTV = (TextView) rowView.findViewById(R.id.title);
            TextView costTV = (TextView) rowView.findViewById(R.id.cost);
            titleTV.setText(item.getTitleForList());
            costTV.setText("cost: " + item.getCost());

            if (!item.getImagePath().isEmpty() && viewGroup.getContext() != null) {
                Glide.with(viewGroup.getContext())
                        .applyDefaultRequestOptions(RequestOptions.centerCropTransform()
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                        .load(item.getImagePath())
                        .into(list_item_image);
            }

            rowView.setBackgroundDrawable(viewGroup.getContext()
                    .getResources().getDrawable(R.drawable.touch_feedback));

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemListener.onItemClick(item);
                }
            });

            return rowView;
        }
    }

    public interface ItemItemListener {

        void onItemClick(Item clickedItem);

    }

}
