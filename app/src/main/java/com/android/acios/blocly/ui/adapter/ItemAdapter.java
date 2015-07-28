package com.android.acios.blocly.ui.adapter;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.acios.blocly.R;
import com.android.acios.blocly.api.UIUtils;
import com.android.acios.blocly.api.model.RssFeed;
import com.android.acios.blocly.api.model.RssItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder> {

    /*public ItemAdapter(List<RssItem> rssItems) {

    }*/

    public interface ItemAdapterDelegate {
         void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem);
         void didFavorite(View view, boolean isChecked, RssItem rssItem);
         void didArchive(View view, boolean isChecked, RssItem rssItem);
         void onVisitClicked(ItemAdapter itemAdapter, RssItem rssItem);
    }

    public interface DataSource {
         RssItem getRssItem(ItemAdapter itemAdapter, int position);
         RssFeed getRssFeed(ItemAdapter itemAdapter, int position);
         int getItemCount(ItemAdapter itemAdapter);
    }

    private static String TAG = ItemAdapter.class.getSimpleName();
    private Map<Long, Integer> rssFeedToColor = new HashMap<>();
    private WeakReference<ItemAdapterDelegate> delegate;
    private WeakReference<DataSource> dataSource;
    private RssItem expandedItem = null;

    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rss_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        if (getDataSource() == null) {
            return;
        }

        RssItem rssItem = getDataSource().getRssItem(this, index);
        RssFeed rssFeed = getDataSource().getRssFeed(this, index);
        itemAdapterViewHolder.update(rssFeed, rssItem);
    }

    @Override
    public int getItemCount() {
        if (getDataSource() == null) {
            return 0;
        }
        return getDataSource().getItemCount(this);
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            return null;
        }
        return dataSource.get();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = new WeakReference<>(dataSource);
    }


    public ItemAdapterDelegate getDelegate() {
        if (delegate == null) {
            return null;
        } else {
            return delegate.get();
        }
    }

    public void setDelegate(ItemAdapterDelegate delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    public RssItem getExpandedItem() {
        return expandedItem;
    }

    public void setExpandedItem(RssItem expandedItem) {
        this.expandedItem = expandedItem;
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements ImageLoadingListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        boolean contentExpanded;
        boolean onTablet;
        TextView title;
        TextView content;
        //Phone only
        TextView feed;
        View headerWrapper;
        ImageView headerImage;
        RssItem rssItem;
        CheckBox archiveCheckBox;
        CheckBox favoriteCheckBox;
        View expandedContentWrapper;
        TextView expandedContent;
        TextView visitSite;
        //tablet only
        TextView callout;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_rss_item_title);
            content = (TextView) itemView.findViewById(R.id.tv_rss_item_content);

            if (itemView.findViewById(R.id.tv_rss_item_feed_title) != null) {
                feed = (TextView) itemView.findViewById(R.id.tv_rss_item_feed_title);
                headerWrapper = itemView.findViewById(R.id.fl_rss_item_image_header);
                headerImage = (ImageView) itemView.findViewById(R.id.iv_rss_item_image);
                archiveCheckBox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_check_mark);
                favoriteCheckBox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_favorite_star);
                expandedContentWrapper = itemView.findViewById(R.id.ll_rss_item_expanded_content_wrapper);
                expandedContent = (TextView) itemView.findViewById(R.id.tv_rss_item_content_full);
                visitSite = (TextView) expandedContentWrapper.findViewById(R.id.tv_rss_item_visit_site);
                visitSite.setOnClickListener(this);
                archiveCheckBox.setOnCheckedChangeListener(this);
                favoriteCheckBox.setOnCheckedChangeListener(this);
            } else {
                onTablet = true;
                callout = (TextView) itemView.findViewById(R.id.tv_rss_item_callout);

                if (Build.VERSION.SDK_INT >= 21) {
                    callout.setOutlineProvider(new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            outline.setOval(0, 0, view.getWidth(), view.getHeight());
                        }
                    });
                    callout.setClipToOutline(true);
                }
            }
            itemView.setOnClickListener(this);
        }

        void update(RssFeed rssFeed, RssItem rssItem) {
            this.rssItem = rssItem;
            title.setText(rssItem.getTitle());
            content.setText(rssItem.getDescription());

            if (onTablet) {
                callout.setText("" + Character.toUpperCase(rssFeed.getTitle().charAt(0)));
                Integer color = rssFeedToColor.get(rssFeed.getRowId());
                if (color == null) {
                    color = UIUtils.generateRandomColor(itemView.getResources().getColor(android.R.color.white));
                    rssFeedToColor.put(rssFeed.getRowId(), color);
                }
                callout.setBackgroundColor(color);
                return;
            }
            feed.setText(rssFeed.getTitle());

            expandedContent.setText(rssItem.getDescription());

            if (rssItem.getImageUrl() != null) {
                headerWrapper.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), this);
            } else {
                headerWrapper.setVisibility(View.GONE);
            }
            animateContent(getExpandedItem() == rssItem);
        }

        //imageLoadingListener interface
        @Override
        public void onLoadingStarted(String imageUri, View view) {}

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason){
            Throwable e = new Throwable();
            Log.e(TAG, "onLoadingFailed: " + failReason.toString(), e);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (imageUri.equals(rssItem.getImageUrl())) {
                headerImage.setImageBitmap(loadedImage);
                headerImage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            // Attempt a retry
            ImageLoader.getInstance().loadImage(imageUri, this);
        }

        /*
         *onClickListener interface
         */

        @Override
        public void onClick(View view) {
            if (view == itemView) {
                if (getDelegate() != null) {
                    getDelegate().onItemClicked(ItemAdapter.this, rssItem);
                }
            } else {
               if (getDelegate() != null) {
                   getDelegate().onVisitClicked(ItemAdapter.this, rssItem);
               }
            }
        }


        /*
         *onCheckedChangeListener interface
         */

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.cb_rss_item_check_mark) {
                getDelegate().didArchive(buttonView, isChecked, rssItem);
            } else if (buttonView.getId() == R.id.cb_rss_item_favorite_star) {
                getDelegate().didFavorite(buttonView, isChecked, rssItem);
            }
        }

        //Private methods

        private void animateContent(final boolean expand) {
            if ((expand && contentExpanded) || (!expand && !contentExpanded)) {
                return;
            }

            int startingHeight = expandedContentWrapper.getMeasuredHeight();
            int finalHeight = content.getMeasuredHeight();

            if (expand) {
                startingHeight = finalHeight;
                expandedContentWrapper.setAlpha(0f);
                expandedContentWrapper.setVisibility(View.VISIBLE);

                expandedContentWrapper.measure(
                        View.MeasureSpec.makeMeasureSpec(content.getWidth(), View.MeasureSpec.EXACTLY),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                finalHeight = expandedContentWrapper.getMeasuredHeight();
            } else {
                content.setVisibility(View.VISIBLE);
            }

            startAnimator(startingHeight, finalHeight, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float wrapperAlpha = expand ? animatedFraction : 1f-animatedFraction;
                    float contentAlpha = 1f - wrapperAlpha;

                    expandedContentWrapper.setAlpha(wrapperAlpha);
                    content.setAlpha(contentAlpha);

                    expandedContentWrapper.getLayoutParams().height = animatedFraction == 1f ?
                            ViewGroup.LayoutParams.WRAP_CONTENT :
                            (Integer) valueAnimator.getAnimatedValue();

                    expandedContentWrapper.requestLayout();
                    if (animatedFraction == 1f) {
                        if (expand) {
                            content.setVisibility(View.GONE);
                        } else {
                            expandedContentWrapper.setVisibility(View.GONE);
                        }
                    }
                }

            });
            contentExpanded = expand;

        }

        private void startAnimator(int start, int end, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
            valueAnimator.addUpdateListener(animatorUpdateListener);
            valueAnimator.setDuration(itemView.getResources().getInteger(android.R.integer.config_shortAnimTime));
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.start();
        }
    }

}