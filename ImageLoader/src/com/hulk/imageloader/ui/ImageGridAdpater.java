package com.hulk.imageloader.ui;

import java.io.Serializable;
import java.util.List;

import com.hulk.imageloader.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageGridAdpater extends AbsAdapter<ImageGridAdpater.ImageItemInfo> {

	public static final String TAG = "ImageGridAdpater";

	LayoutInflater inflater;
	private boolean isLastAsAddItem = true;
	private boolean debug = true;
	private boolean largeMode = false;

	public ImageGridAdpater(Context context) {
	    this(context, null);
	}
	
	public ImageGridAdpater(Context context, List<ImageGridAdpater.ImageItemInfo> list) {
        super(context, list);
        inflater = LayoutInflater.from(context);
    }

	public void setLargeMode(boolean largeMode) {
        this.largeMode = largeMode;
    }

	public void setLastAsAddItem(boolean isLastAsAddItem) {
		this.isLastAsAddItem = isLastAsAddItem;
	}

	private View createHolderView(int defDrawableId, String tipText) {
		View v = getLayoutView(R.layout.image_grid_item_view);
		ImageItemHolder holder = new ImageItemHolder();
		holder.img = (ImageView) v.findViewById(R.id.grid_item_iv);
		holder.tipTv = (TextView) v.findViewById(R.id.grid_item_tip_tv);
		if (defDrawableId > 0) {
			holder.img.setImageResource(defDrawableId);
		}
		if (tipText != null && tipText.length() > 0) {
			holder.tipTv.setText(tipText);
		}
		v.setTag(holder);
		return v;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
	    ImageItemHolder holder = null;
	    if(convertView == null) {
	        convertView = createHolderView(0 , null);
	        holder = (ImageItemHolder) convertView.getTag();
	    } else {
	    	//鉴于该gridview数量比较少，先去掉重用,再想办法
	    	//反复快速刷新时, 重用View会导致[+]图标的图片显示位url的图片, 原因是url加载延时
	        holder = (ImageItemHolder) convertView.getTag();
	    }
		ImageItemInfo item = getItem(position);
		if(debug ) Log.i(TAG, "position=" + position + ", " + item);
		holder.tipTv.setText(item.tip);
		if (!TextUtils.isEmpty(item.tip)) {
		    holder.tipTv.setVisibility(View.GONE);
        } else {
            holder.tipTv.setVisibility(View.VISIBLE);
        }
		switch (item.type) {
            case ImageItemInfo.TYPE_RES:
                holder.img.setImageResource(item.resId);//special icon resource
                break;
            case ImageItemInfo.TYPE_URL:
                String url = item.url;
                break;
            case ImageItemInfo.TYPE_FILE_PATH:
                break;

            default:
                break;
        }

		return convertView;
	}

	private void setImgLayout(ImageView img) {
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = mContext.getResources().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams lp = img.getLayoutParams();
        //兼顾横竖屏的宽高变化，小的为准
        int imgSize = (width < height ? width : height) - 50;
        lp.width = imgSize;
        lp.height = imgSize;
        img.setLayoutParams(lp);
    }

	public class ImageItemHolder {
	    public ImageView img;
	    public TextView tipTv;
	}

	public static class ImageItemInfo implements Serializable {
        private static final long serialVersionUID = 6946839354653488968L;
        public final static int TYPE_URL = 0;
	    public final static int TYPE_RES = 1;
	    public final static int TYPE_FILE_PATH = 2;
	    
	    public final static int FLAG_UPLOADING = 0;
        public final static int FLAG_UPLOAD_FAILED = 1;
        public final static int FLAG_UPLOADED = 2;
	    
	    /**
	     * refer to {@link TYPE_*}
	     * default TYPE_URL
	     */
	    public int type = -1;
	    public int resId = 0;
	    public String url;
	    public String filePath;
	    public int flag = -1;
	    public String tip;
	    public ImageItemInfo() {
        }
	    public ImageItemInfo(int type) {
            this.type = type;
        }
		@Override
		public String toString() {
			return "ImageItemInfo [type=" + type + ", resId=" + resId
					+ ", url=" + url + ", filePath=" + filePath + ", flag="
					+ flag + ", tip=" + tip + "]";
		}
	    
	}
}
