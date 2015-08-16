package com.ombrax.watchers.Fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.ombrax.watchers.Adapters.WatchListAdapter;
import com.ombrax.watchers.Controllers.MenuController;
import com.ombrax.watchers.Enums.MenuItemType;
import com.ombrax.watchers.Interfaces.Command;
import com.ombrax.watchers.Interfaces.IOnSecondaryMenuCloseObserver;
import com.ombrax.watchers.Interfaces.IOnSortMenuItemChangeListener;
import com.ombrax.watchers.Manager.SortingManager;
import com.ombrax.watchers.Models.WatchModel;
import com.ombrax.watchers.R;
import com.ombrax.watchers.Repositories.WatchRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Ombrax on 30/06/2015.
 */
public class WatchListFragment extends Fragment implements IOnSortMenuItemChangeListener, IOnSecondaryMenuCloseObserver {

    //region declaration
    //region inner field
    private MenuController mc;

    private List<WatchModel> watchModels;
    private WatchListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    //endregion

    //region variable
    private IOnFragmentActionBarListener listener;
    //endregion
    //endregion

    //region create
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mc = MenuController.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.watch_list_recycler_view);

        watchModels = WatchRepository.getInstance().getAll();
        adapter = new WatchListAdapter(getActivity(), watchModels);
        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mc.setOnSortMenuItemChangeListener(this);
        mc.registerOnSecondaryMenuCloseObserver(this);
        mc.onMenuItemSelect(MenuItemType.HOME);
        waitForGlobalLayout(new Command() {
            @Override
            public void execute() {
                expandToolbar();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mc.unregisterOnSecondaryMenuCloseObserver(this);
        collapseToolbar();
    }
    //endregion

    //region helper
    private void collapseToolbar() {
        if (!getActivity().isFinishing()) {
            if (listener != null) {
                listener.onActionBarStateChange(true);
            }
        }
    }

    private void expandToolbar() {
        if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            if (listener != null) {
                listener.onActionBarStateChange(false);
            }
        }
    }

    private void waitForGlobalLayout(final Command command) {
        getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getView() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    command.execute();
                }
            }
        });
    }
    //endregion


    //region interface implementation
    @Override
    public void onSortMenuItemChange(MenuItemType menuItemType, boolean isAscendingOrder) {
        adapter.sort(SortingManager.getInstance().get(menuItemType, isAscendingOrder));
        System.out.println("Sorting changed to : "+menuItemType+ " " + (isAscendingOrder ? "ASC" : "DESC"));
    }

    @Override
    public void onSecondaryMenuClose(boolean codeEnabled) {
        if(!codeEnabled) {
            //TODO get action from settings
            System.out.println("Secondary Menu Closed By User");
        }else{
            //Do nothing
            System.out.println("Secondary Menu Closed From Code");
        }
    }
    //endregion

    //region attach/detach
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (IOnFragmentActionBarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IOnFragmentActionBarListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
    //endregion

    //region interface
    public interface IOnFragmentActionBarListener {
        void onActionBarStateChange(boolean collapse);
    }
    //endregion
}