package kr.co.diary.map;

import kr.co.diary.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;


/**
 * Wrapper class to provider resources on map view.
 *
 * @author kyjkim
 */
public class NMapViewerResourceProvider extends NMapResourceProvider {
	private static final String LOG_TAG = "NMapViewerResourceProvider";
	private static final boolean DEBUG = false;

	private static final Bitmap.Config BITMAP_CONFIG_DEFAULT = Bitmap.Config.ARGB_8888;

	private static final int POI_FONT_COLOR_NUMBER = 0xFF909090;
	private static final float POI_FONT_SIZE_NUMBER = 10.0F;

	private static final int POI_FONT_COLOR_ALPHABET = 0xFFFFFFFF;
	private static final float POI_FONT_OFFSET_ALPHABET = 6.0F;
	private static final Typeface POI_FONT_TYPEFACE = null;//Typeface.DEFAULT_BOLD;

	private final Rect mTempRect = new Rect();
	private final Paint mTextPaint = new Paint();

	public NMapViewerResourceProvider(final Context context) {
		super(context);

		mTextPaint.setAntiAlias(true);
	}

	/**
	 * Get drawable for markerId at focused state
	 *
	 * @param markerId unique id for POI or Number icons.
	 * @param focused true for focused state, false otherwise.
	 * @return
	 */
	public Drawable getDrawable(final int markerId, final boolean focused, final NMapOverlayItem item) {
		Drawable marker = null;

		int resourceId = findResourceIdForMarker(markerId, focused);
		if (resourceId > 0) {
			marker = mContext.getResources().getDrawable(resourceId);
		} else {
			resourceId = 4 * markerId;
			if (focused) {
				resourceId += 1;
			}

			marker = getDrawableForMarker(markerId, focused);
		}

		// set bounds
		if (marker != null) {
			setBounds(marker, markerId, item);
		}

		return marker;
	}

	public Bitmap getBitmap(final int markerId, final boolean focused) {
		Bitmap bitmap = null;

		Drawable marker = getDrawable(markerId, focused, null);
		if (marker != null) {
			bitmap = getBitmap(marker);
		}

		return bitmap;
	}

	public Bitmap getBitmap(final Drawable marker) {
		Bitmap bitmap = null;

		if (marker != null) {
			int width = marker.getIntrinsicWidth();
			int height = marker.getIntrinsicHeight();
			bitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG_DEFAULT);

			marker.setBounds(0, 0, width, height);

			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(0x00000000);

			marker.draw(canvas);
		}

		return bitmap;
	}

	public Bitmap getBitmap(final int resourceId) {
		Bitmap bitmap = null;

		Drawable marker = null;
		if (resourceId > 0) {
			marker = mContext.getResources().getDrawable(resourceId);
		}

		if (marker != null) {
			bitmap = getBitmap(marker);
		}

		return bitmap;
	}

	public Bitmap getBitmapWithNumber(final int resourceId, final String strNumber, final float offsetY, final int fontColor, final float fontSize) {
		Bitmap bitmap = null;

		Drawable marker = getDrawableWithNumber(resourceId, strNumber, offsetY, fontColor, fontSize);

		if (marker != null) {
			bitmap = getBitmap(marker);
		}

		return bitmap;
	}

	/**
	 * Class to find resource Ids on map view
	 */
	private class ResourceIdsOnMap {

		int markerId;
		int resourceId;
		int resourceIdFocused;

		ResourceIdsOnMap(final int markerId, final int resourceId, final int resourceIdFocused) {
			this.markerId = markerId;
			this.resourceId = resourceId;
			this.resourceIdFocused = resourceIdFocused;
		}
	}

	// Resource Ids for single icons
	private final ResourceIdsOnMap mResourceIdsForMarkerOnMap[] = {
		// Spot, Pin icons
		new ResourceIdsOnMap(NMapPOIflagType.PIN, R.drawable.ic_pin_01, R.drawable.ic_pin_02),
		new ResourceIdsOnMap(NMapPOIflagType.SPOT, R.drawable.ic_pin_01, R.drawable.ic_pin_02),

		// Direction POI icons: From, To
		new ResourceIdsOnMap(NMapPOIflagType.FROM, R.drawable.ic_map_start, R.drawable.ic_map_start_over),
		new ResourceIdsOnMap(NMapPOIflagType.TO, R.drawable.ic_map_arrive, R.drawable.ic_map_arrive_over),
	};

	/**
	 * Find resource id corresponding to the markerId.
	 *
	 * @param markerId marker id for a NMapPOIitem.
	 * @param focused flag to indicated focused or normal state of this marker.
	 *
	 * @return resource id for the given markerId.
	 *
	 * @see NMapPOIflagType
	 */
	@Override
	protected int findResourceIdForMarker(final int markerId, final boolean focused) {
		int resourceId = 0;

		if (DEBUG) {
			Log.i(LOG_TAG, "getResourceIdForMarker: markerId=" + markerId + ", focused=" + focused);
		}

		if (markerId < NMapPOIflagType.SINGLE_MARKER_END) {
			resourceId = getResourceIdOnMapView(markerId, focused, mResourceIdsForMarkerOnMap);
			if (resourceId > 0) {
				return resourceId;
			}
		}

		if (markerId >= NMapPOIflagType.NUMBER_BASE && markerId < NMapPOIflagType.NUMBER_END) { // Direction Number icons

		} else if (markerId >= NMapPOIflagType.CUSTOM_BASE && markerId < NMapPOIflagType.CUSTOM_END) { // Custom POI icons

		}

		return resourceId;
	}

	/**
	 * 	Set bounds for this marker depending on its shape.
	 *
	 */
	@Override
	protected void setBounds(final Drawable marker, final int markerId, final NMapOverlayItem item) {

		// check shape of the marker to set bounds correctly.
		if (NMapPOIflagType.isBoundsCentered(markerId)) {
			if (marker.getBounds().isEmpty()) {
				NMapPOIitem.boundCenter(marker);
			}

			if (null != item) {
				item.setAnchorRatio(0.5f, 0.5f);
			}

		} else {
			if (marker.getBounds().isEmpty()) {
				NMapPOIitem.boundCenterBottom(marker);
			}

			if (null != item) {
				item.setAnchorRatio(0.5f, 1.0f);
			}
		}
	}

	@Override
	public Drawable[] getLocationDot() {
		Drawable[] drawable = new Drawable[2];

		drawable[0] = mContext.getResources().getDrawable(R.drawable.ic_mylocation_off);
		drawable[1] = mContext.getResources().getDrawable(R.drawable.ic_mylocation_on);

		for (Drawable element : drawable) {
			int w = element.getIntrinsicWidth() / 2;
			int h = element.getIntrinsicHeight() / 2;

			element.setBounds(-w, -h, w, h);
		}

		return drawable;
	}

	@Override
	public Drawable getDirectionArrow() {

		Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_angle);

		if (drawable != null) {
			int w = drawable.getIntrinsicWidth() / 2;
			int h = drawable.getIntrinsicHeight() / 2;

			drawable.setBounds(-w, -h, w, h);
		}

		return drawable;
	}

	public Drawable getDrawableWithNumber(final int resourceId, final String strNumber, final float offsetY, final int fontColor, final float fontSize) {

		Bitmap textBitmap = getBitmapWithText(resourceId, strNumber, fontColor, fontSize, offsetY);

		//Log.i(LOG_TAG, "getDrawableWithNumber: width=" + textBitmap.getWidth() + ", height=" + textBitmap.getHeight() + ", density=" + textBitmap.getDensity());

		// set bounds
		Drawable marker = new BitmapDrawable(mContext.getResources(), textBitmap);
		if (marker != null) {
			NMapPOIitem.boundCenter(marker);
		}

		//Log.i(LOG_TAG, "getDrawableWithNumber: width=" + marker.getIntrinsicWidth() + ", height=" + marker.getIntrinsicHeight());

		return marker;
	}

	public Drawable getDrawableWithAlphabet(final int resourceId, final String strAlphabet, final int fontColor, final float fontSize) {

		Bitmap textBitmap = getBitmapWithText(resourceId, strAlphabet, fontColor, fontSize, POI_FONT_OFFSET_ALPHABET);

		// set bounds
		Drawable marker = new BitmapDrawable(mContext.getResources(), textBitmap);
		if (marker != null) {
			NMapPOIitem.boundCenterBottom(marker);
		}

		return marker;
	}

	@Override
	protected Drawable getDrawableForMarker(final int markerId, final boolean focused) {
		Drawable drawable = null;

		if (markerId >= NMapPOIflagType.NUMBER_BASE && markerId < NMapPOIflagType.NUMBER_END) { // Direction Number icons
			int resourceId = (focused) ? R.drawable.ic_map_no_02 : R.drawable.ic_map_no_01;
			int fontColor = (focused) ? POI_FONT_COLOR_ALPHABET : POI_FONT_COLOR_NUMBER;

			String strNumber = String.valueOf(markerId - NMapPOIflagType.NUMBER_BASE);

			drawable = getDrawableWithNumber(resourceId, strNumber, 0.0F, fontColor, POI_FONT_SIZE_NUMBER);
		} else if (markerId >= NMapPOIflagType.CUSTOM_BASE && markerId < NMapPOIflagType.CUSTOM_END) { // Custom POI icons

		}

		return drawable;
	}

	private Bitmap getBitmapWithText(final int resourceId, final String strNumber, final int fontColor, final float fontSize, float offsetY) {
		Bitmap bitmapBackground = BitmapFactory.decodeResource(mContext.getResources(), resourceId);

		int width = bitmapBackground.getWidth();
		int height = bitmapBackground.getHeight();
		//Log.i(LOG_TAG, "getBitmapWithText: width=" + width + ", height=" + height + ", density=" + bitmapBackground.getDensity());

		Bitmap textBitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG_DEFAULT);

		Canvas canvas = new Canvas(textBitmap);

		canvas.drawBitmap(bitmapBackground, 0, 0, null);

		// set font style
		mTextPaint.setColor(fontColor);
		// set font size
		mTextPaint.setTextSize(fontSize * mScaleFactor);
		// set font type
		if (POI_FONT_TYPEFACE != null) {
			mTextPaint.setTypeface(POI_FONT_TYPEFACE);
		}

		// get text offset
		mTextPaint.getTextBounds(strNumber, 0, strNumber.length(), mTempRect);
		float offsetX = (width - mTempRect.width()) / 2 - mTempRect.left;
		if (offsetY == 0.0F) {
			offsetY = (height - mTempRect.height()) / 2 + mTempRect.height();
		} else {
			offsetY = offsetY * mScaleFactor + mTempRect.height();
		}

		//Log.i(LOG_TAG, "getBitmapWithText: number=" + number + ", focused=" + focused);
		//Log.i(LOG_TAG, "getBitmapWithText: offsetX=" + offsetX + ", offsetY=" + offsetY + ", boundsWidth=" + mTempRect.width() + ", boundsHeight=" + mTempRect.height());

		// draw text
		canvas.drawText(strNumber, offsetX, offsetY, mTextPaint);

		return textBitmap;
	}

	@Override
	public Drawable getCalloutBackground() {

		Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_speech);

		return drawable;
	}

	@Override
	public String getCalloutRightButtonText(final NMapOverlayItem item) {
		if (item instanceof NMapPOIitem) {
			NMapPOIitem poiItem = (NMapPOIitem)item;

			if (poiItem.showRightButton()) {
				return "완료";
			}
		}

		return null;
	}

	@Override
	public Drawable[] getCalloutRightButton(final NMapOverlayItem item) {
		if (item instanceof NMapPOIitem) {
			NMapPOIitem poiItem = (NMapPOIitem)item;

			if (poiItem.showRightButton()) {
				Drawable[] drawable = new Drawable[3];

				drawable[0] = mContext.getResources().getDrawable(R.drawable.btn_green_normal);
				drawable[1] = mContext.getResources().getDrawable(R.drawable.btn_green_pressed);
				drawable[2] = mContext.getResources().getDrawable(R.drawable.btn_green_highlight);

				return drawable;
			}
		}

		return null;
	}

	@Override
	public Drawable[] getCalloutRightAccessory(final NMapOverlayItem item) {
		if (item instanceof NMapPOIitem) {
			NMapPOIitem poiItem = (NMapPOIitem)item;

			if (poiItem.hasRightAccessory() && (poiItem.getRightAccessoryId() > 0)) {
				Drawable[] drawable = new Drawable[3];

				switch (poiItem.getRightAccessoryId()) {
					case NMapPOIflagType.PANORAMA:
						drawable[0] = mContext.getResources().getDrawable(R.drawable.bt_panorama_normal);
						drawable[1] = mContext.getResources().getDrawable(R.drawable.bt_panorama_pressed);
						drawable[2] = mContext.getResources().getDrawable(R.drawable.bt_panorama_highlight);
						break;
				}

				return drawable;
			}
		}

		return null;
	}

	@Override
	public int getParentLayoutIdForOverlappedListView() {
		// not supported
		return 0;
	}

	@Override
	public int getOverlappedListViewId() {
		// not supported
		return 0;
	}

	@Override
	public int getLayoutIdForOverlappedListView() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemLayoutIdForOverlappedListView() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemTextViewId() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemImageViewId() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemDividerId() {
		// not supported
		return 0;
	}

	@Override
	public void setOverlappedListViewLayout(final ListView listView, final int itemCount, final int width, final int height) {
		// not supported
	}

	@Override
	public void setOverlappedItemResource(final NMapPOIitem poiItem, final ImageView imageView) {
		// not supported
	}

	private int getResourceIdOnMapView(final int markerId, final boolean focused, final ResourceIdsOnMap resourceIdsArray[]) {
		int resourceId = 0;

		for (ResourceIdsOnMap resourceIds : resourceIdsArray) {
			if (resourceIds.markerId == markerId) {
				resourceId = (focused) ? resourceIds.resourceIdFocused : resourceIds.resourceId;
				break;
			}
		}

		return resourceId;
	}
}
