package com.apk.editor.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Projects;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.text.DateFormat;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 06, 2021
 */
public class RecycleViewProjectsAdapter extends RecyclerView.Adapter<RecycleViewProjectsAdapter.ViewHolder> {

    private static List<String> data;

    public RecycleViewProjectsAdapter(List<String> data) {
        RecycleViewProjectsAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewProjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewProjectsAdapter.ViewHolder holder, int position) {
        try {
            if (AppData.isAppInstalled(new File(data.get(position)).getName(), holder.mAppIcon.getContext())) {
                holder.mAppIcon.setImageDrawable(AppData.getAppIcon(new File(data.get(position)).getName(), holder.mAppIcon.getContext()));
                if (Projects.mSearchText != null && AppData.getAppName(new File(data.get(position)).getName(), holder.mAppName.getContext()).toString().toLowerCase().contains(Projects.mSearchText)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(AppData.getAppName(new File(data.get(position)).getName(), holder.mAppName.getContext()).toString().toLowerCase().replace(Projects.mSearchText,
                            "<b><i><font color=\"" + Color.RED + "\">" + Projects.mSearchText + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(AppData.getAppName(new File(data.get(position)).getName(), holder.mAppName.getContext()));
                }
            } else {
                holder.mAppIcon.setImageDrawable(holder.mAppIcon.getContext().getResources().getDrawable(R.drawable.ic_projects));
                if (Projects.mSearchText != null && new File(data.get(position)).getName().toLowerCase().contains(Projects.mSearchText)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(new File(data.get(position)).getName().toLowerCase().replace(Projects.mSearchText,
                            "<b><i><font color=\"" + Color.RED + "\">" + Projects.mSearchText + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(new File(data.get(position)).getName());
                }
            }
            holder.mTotalSize.setText(holder.mAppName.getContext().getString(R.string.last_modified, DateFormat.getDateTimeInstance()
                    .format(new File(data.get(position)).lastModified())));
            holder.mCard.setOnClickListener(v -> {
                if (AppData.isAppInstalled(data.get(position).replace(v.getContext().getCacheDir().getPath() + "/",""), v.getContext())) {
                    APKExplorer.mAppID = data.get(position).replace(v.getContext().getCacheDir().getPath() + "/","");
                } else {
                    APKExplorer.mAppID = null;
                }
                APKExplorer.mPath = data.get(position);
                Intent explorer = new Intent(v.getContext(), APKExploreActivity.class);
                v.getContext().startActivity(explorer);
            });
            holder.mCard.setOnLongClickListener(v -> {
                if (!APKEditorUtils.isWritePermissionGranted(v.getContext())) {
                    ActivityCompat.requestPermissions((Activity) v.getContext(), new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    APKEditorUtils.snackbar(v, v.getContext().getString(R.string.permission_denied_message));
                } else {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setMessage(v.getContext().getString(R.string.export_project_question))
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setPositiveButton(R.string.export, (dialog, id) -> {
                                APKEditorUtils.dialogEditText(null,
                                        (dialogInterface, i) -> {
                                        }, text -> {
                                            if (text.isEmpty()) {
                                                APKEditorUtils.snackbar(v, v.getContext().getString(R.string.name_empty));
                                                return;
                                            }
                                            if (text.contains(" ")) {
                                                text = text.replace(" ", "_");
                                            }
                                            String mName = text;
                                            if (APKEditorUtils.exist(Projects.getExportPath(v.getContext()) + "/" + text)) {
                                                new MaterialAlertDialogBuilder(v.getContext())
                                                        .setMessage(v.getContext().getString(R.string.export_project_replace, text))
                                                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                                        })
                                                        .setPositiveButton(R.string.replace, (dialogInterface, i) -> {
                                                            Projects.exportProject(new File(data.get(position)), mName, v.getContext());
                                                        })
                                                        .show();
                                            } else {
                                                Projects.exportProject(new File(data.get(position)), mName, v.getContext());
                                            }
                                        }, v.getContext()).setOnDismissListener(dialogInterface -> {
                                }).show();
                            }).show();
                }
                return false;
            });
            holder.mDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(holder.mDelete.getContext())
                    .setMessage(holder.mDelete.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.delete, (dialog, id) -> {
                        APKEditorUtils.delete(data.get(position));
                        data.remove(position);
                        notifyDataSetChanged();
                    }).show());
            holder.mTotalSize.setVisibility(View.VISIBLE);
            holder.mTotalSize.setTextColor(APKEditorUtils.isDarkTheme(holder.mTotalSize.getContext()) ? Color.GREEN : Color.BLACK);
            holder.mDelete.setColorFilter(Color.RED);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageButton mAppIcon, mDelete;
        private MaterialCardView mCard;
        private MaterialTextView mAppName, mTotalSize;

        public ViewHolder(View view) {
            super(view);
            this.mCard = view.findViewById(R.id.card);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mDelete = view.findViewById(R.id.delete);
            this.mAppName = view.findViewById(R.id.title);
            this.mTotalSize = view.findViewById(R.id.version);
        }
    }

}