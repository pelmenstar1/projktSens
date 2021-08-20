package com.pelmenstar.projktSens.shared.android;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.ThemedSpinnerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReadonlyArrayAdapter<T> extends BaseAdapter implements ThemedSpinnerAdapter {
    private final Context context;
    private final int resource;
    private int dropDownResource;

    private final @NotNull T @NotNull [] objects;

    private final LayoutInflater inflater;

    @Nullable
    private LayoutInflater dropDownInflater;

    public ReadonlyArrayAdapter(@NotNull Context context, @LayoutRes int resource, @NotNull T @NotNull [] objects) {
        this.context = context;
        this.resource = resource;
        this.objects = objects;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.length;
    }

    @Override
    @NotNull
    public Object getItem(int position) {
        return objects[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @NotNull
    public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
        return createViewFromResource(inflater, position, convertView, parent, resource);
    }

    public void setDropDownResource(@LayoutRes int res) {
        dropDownResource = res;
    }

    @Override
    public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
        if (theme == null) {
            dropDownInflater = null;
        } else if (theme == inflater.getContext().getTheme()) {
            dropDownInflater = inflater;
        } else {
            Context wrapper = new ContextThemeWrapper(context, theme);
            dropDownInflater = LayoutInflater.from(wrapper);
        }
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return dropDownInflater == null ? null : dropDownInflater.getContext().getTheme();
    }

    @Override
    @NotNull
    public View getDropDownView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
        LayoutInflater inf = dropDownInflater != null ? dropDownInflater : inflater;

        return createViewFromResource(inf, position, convertView, parent, dropDownResource);
    }

    @NotNull
    private View createViewFromResource(
            @NotNull LayoutInflater inflater,
            int position,
            @Nullable View convertView,
            @NotNull ViewGroup parent,
            @LayoutRes int res) {
        View view;

        if (convertView == null) {
            view = inflater.inflate(res, parent, false);
        } else {
            view = convertView;
        }

        TextView textView;

        try {
            textView = (TextView) view;
        } catch (ClassCastException e) {
            throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView");
        }

        textView.setText(objects[position].toString());

        return view;
    }
}
