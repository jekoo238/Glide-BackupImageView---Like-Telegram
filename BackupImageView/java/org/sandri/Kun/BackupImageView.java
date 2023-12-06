/*
* This is the source code of Telegram for Android v. 5.x.x.
* It is licensed under GNU GPL v. 2 or later.
* You should have received a copy of the license in this archive (see LICENSE).
*
* Copyright Sandri DEV, 2022-2023.
*/
package org.anime.project.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.text.TextUtils;
import android.util.TypedValue;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.ColorInt;
import androidx.annotation.GravityInt;
import androidx.core.content.ContextCompat;

import org.anime.project.tgnet.TLRPC;
import org.anime.project.tgnet.TLObject;
import org.anime.project.utils.FileLog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.net.URL;
import android.net.Uri;

public class BackupImageView extends ImageView implements RequestListener<Drawable> {
	
	protected static final String ASSETS_RESOURCE = "file:///android_asset/";
	
	protected int width = -1;
	protected int height = -1;
	private int roundRadius;
	
	private RequestOptions sharedOptions;
	private RequestBuilder<Drawable> glideRequest;
	private boolean allowLoadingOnAttachedOnly = false;
	private boolean needOverride = false;
	
	private boolean needPinchToZoom;
	private boolean circleCrop;
	private boolean loadFailed;
	private boolean attached;
	
	private float thumbalSize = 0f;
	
	public BackupImageView(@NonNull Context context) {
		this(context, null, 0);
	}
	
	public BackupImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public BackupImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		sharedOptions = new RequestOptions()
		.diskCacheStrategy(DiskCacheStrategy.ALL)
		.timeout(10000);
		
		setScaleType(ScaleType.CENTER_CROP);
		setCircleCrop(circleCrop);
	}
	
	private static RequestManager glide;
	
	public static RequestManager getGlide(Context context) {
		if (glide == null) {
			glide = Glide.with(context);
		}
		return glide;
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		attached = false;
		if (allowLoadingOnAttachedOnly) {
			cancelLoadImage();
		}
		if (needPinchToZoom) {
			
		}
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		attached = true;
		if (needPinchToZoom) {
			
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setSize(getMeasuredWidth(), getMeasuredHeight());
	}
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		final int width = getWidth();
		final int height = getHeight();
		setSize(width, height);
	}
	
	@Override
	public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
		loadFailed = true;
		return false;
	}
	
	@Override
	public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
		loadFailed = false;
		return false;
	}
	
	@NonNull
	public RequestOptions getRequestOptions() {
		return sharedOptions;
	}
	
	@Nullable
	public Drawable getStaticThumb() {
		return getDrawable();
	}
	
	public int getRoundRadius() {
		return roundRadius;
	}
	
	public boolean isCircleCrop() {
		return circleCrop;
	}
	
	public void setSize(int w, int h) {
		if (needOverride || (width == w && height == h) || w == 0 || h == 0) {
			return;
		}
		width = w;
		height = h;
		sharedOptions.override(width, height);
	}
	
	public void override(int w, int h) {
		override(true, w, h);
	}
	
	public void override(boolean override, int w, int h) {
		if ((needOverride == override) && (width == w && height == h) || w == 0 || h == 0) {
			return;
		}
		needOverride = override;
		width = w;
		height = h;
		sharedOptions.override(width, height);
	}
	
	public void setForUserOrChat(@NonNull TLObject object, Drawable placeholder) {
		setPlaceHolder(placeholder);
		if (object instanceof TLRPC.User) {
			TLRPC.User user = (TLRPC.User) object;
			if (user.photo != null) {
				setImage(user.photo);
			}
		} else if (object instanceof TLRPC.Chat) {
			TLRPC.Chat chat = (TLRPC.Chat) object;
			if (chat.photo != null) {
				setImage(chat.photo);
			}
		} else {
			setImageDrawable(placeholder);
		}
	}
	
	public void setSignature(String key) {
		sharedOptions.signature(new ObjectKey(key));
	}
	
	public void setPriorityHigh(boolean high) {
		sharedOptions.priority(high ? Priority.HIGH : Priority.NORMAL);
	}
	
	public void setThumnailSize(float size) {
		thumbalSize = size;
	}
	
	public void setTimeout(int timeout) {
		sharedOptions.timeout(timeout);
	}
	
	public void setRoundRadius(int rad) {
		roundRadius = rad;
		sharedOptions.transform(new CenterCrop(), new RoundedCorners(roundRadius));
	}
	
	public void setCircleCrop(boolean value) {
		circleCrop = value;
		if (value) {
			sharedOptions.circleCrop();
		} else {
			sharedOptions.transform(new CenterCrop());
		}
	}
	
	public void setAspectFit(boolean value) {
		if (value) {
			sharedOptions.fitCenter();
		}
	}
	
	public void setAllowLoadingOnAttachedOnly(boolean allow) {
		allowLoadingOnAttachedOnly = allow;
	}
	
	public void setPlaceHolder(@DrawableRes int resId) {
		sharedOptions.placeholder(resId);
	}
	
	public void setError(@DrawableRes int resId) {
		sharedOptions.error(resId);
	}
	
	public void setPlaceHolder(Drawable drawable) {
		sharedOptions.placeholder(drawable);
	}
	
	public void setError(Drawable drawable) {
		sharedOptions.error(drawable);
	}
	
	public void setImage(@NonNull Drawable drawable) {
		glideRequest = Glide.with(getContext())
		.load(drawable)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(this);
		if (thumbalSize != 0f) {
			glideRequest.thumbnail(thumbalSize);
		}
		glideRequest.into(this);
	}
	
	public void setImage(@DrawableRes int resId) {
		glideRequest = Glide.with(getContext())
		.load(resId)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(this);
		if (thumbalSize != 0f) {
			glideRequest.thumbnail(thumbalSize);
		}
		glideRequest.into(this);
	}
	
	public void setImage(@NonNull String url) {
		setImage(Uri.parse(url));
	}
	
	public void setImageFromAssets(@NonNull String imageFileName) {
		setImage(Uri.parse(ASSETS_RESOURCE + imageFileName));
	}
	
	public void setImage(@NonNull Uri uri, int rad) {
		setRoundRadius(rad);
		setImage(uri);
	}
	
	public void setImage(@NonNull Uri uri) {
		if (TextUtils.isEmpty(uri.toString())) {
			return;
		}
		glideRequest = Glide.with(getContext())
		.load(uri)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(this);
		if (thumbalSize != 0f) {
			glideRequest.thumbnail(thumbalSize);
		}
		glideRequest.into(this);
	}
	
	public void setImage(@NonNull File file) {
		if (file.length() == 0) {
			return;
		}
		glideRequest = Glide.with(getContext())
		.load(file)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(this);
		if (thumbalSize != 0f) {
			glideRequest.thumbnail(thumbalSize);
		}
		glideRequest.into(this);
	}
	
	public void setImage(byte[] model) {
		if (model == null || model.length == 0) {
			return;
		}
		glideRequest = Glide.with(getContext())
		.load(model)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(this);
		if (thumbalSize != 0f) {
			glideRequest.thumbnail(thumbalSize);
		}
		glideRequest.into(this);
	}
	
	public void clearImage() {
		Glide.with(getContext()).clear(this);
		glideRequest = null;
	}
	
	public void cancelLoadImage() {
		if (glideRequest != null) {
			clearImage();
		}
	}
	
	public void setLayerNum(int value) {
		setRoundRadius(value);
	}
      }
