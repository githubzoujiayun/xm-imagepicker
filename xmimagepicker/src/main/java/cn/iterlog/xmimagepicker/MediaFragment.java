package cn.iterlog.xmimagepicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.File;

import cn.iterlog.xmimagepicker.Utils.MediaController;
import cn.iterlog.xmimagepicker.adapter.MediaAdapter;
import cn.iterlog.xmimagepicker.anim.SlideInBottomAnimationAdapter;
import cn.iterlog.xmimagepicker.corp.Crop;
import cn.iterlog.xmimagepicker.data.MediasLogic;
import cn.iterlog.xmimagepicker.videoplay.VideoActivity;

import static android.app.Activity.RESULT_OK;

public class MediaFragment extends Fragment implements MediasLogic.MediaListener {
    private static final String ARG_MEDIA_TYPE = "ARG_MEDIA_TYPE";
    private int mediaType = -1;
    private RecyclerView mRecy;
    private MediaAdapter mAdapter;
    private ProgressBar mProgressBar;

    public MediaFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MediaFragment newInstance(int sectionNumber) {
        MediaFragment fragment = new MediaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEDIA_TYPE, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_picker, container, false);
        mRecy = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.loadProgress);
        mediaType = getArguments().getInt(ARG_MEDIA_TYPE);
        int itemId = 0;
        if (itemId == 1) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecy.setLayoutManager(linearLayoutManager);
        } else if (itemId == 0) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
            gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
            mRecy.setLayoutManager(gridLayoutManager);
        }
        mAdapter = new MediaAdapter(MediasLogic.getInstance().loadMedias(mediaType));
        mRecy.setAdapter(new SlideInBottomAnimationAdapter(mAdapter));
        if (MediasLogic.getInstance().isLoading()) {
            mProgressBar.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mAdapter.setListener(new MediaAdapter.OnItemChangeListener() {

            @Override
            public void onMediaView(MediaAdapter.MediaHolder view, int position, MediaController.PhotoEntry photoEntry) {
                try {

                    if (photoEntry.isVideo) {
                        if (Configs.isPreviewVideo()) {
                            Intent intent = new Intent(getActivity(), VideoActivity.class);
                            intent.putExtra("src", photoEntry.path);
                            getActivity().startActivityForResult(intent, VideoActivity.REQUEST_PICK);
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra(Configs.MEDIA_TYPE, Configs.MEDIA_PICTURE);
                            Uri uri = Uri.fromFile(new File(photoEntry.path));
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().onBackPressed();
                        }
                    } else {
                        if (Configs.isEditImage()) {
                            Uri destination = Uri.fromFile(new File(getContext().getCacheDir(), "cropped"));
                            Uri src = Uri.fromFile(new File(photoEntry.path));
                            Crop.of(src, destination).asSquare().start(getActivity());
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra(Configs.MEDIA_TYPE, Configs.MEDIA_PICTURE);
                            Uri uri = Uri.fromFile(new File(photoEntry.path));
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().onBackPressed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        MediasLogic.getInstance().registerListener(this, this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        MediasLogic.getInstance().unRegisterListener(this);
        super.onDestroyView();
    }

    @Override
    public void onMediaLoaded(int type) {
        if (type == mediaType) {
            mAdapter.setmMedias(MediasLogic.getInstance().loadMedias(mediaType));
            mAdapter.notifyDataSetChanged();
        }
    }

}