/*
   Copyright 2011 Harri Smått

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package ca.ilanguage.oprime.storybook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import ca.ilanguage.oprime.model.Touch;

/**
 * OpenGL ES View.
 * 
 * @author harism
 */
public class CurlView extends GLSurfaceView implements View.OnTouchListener, CurlRenderer.Observer {

  /**
   * Provider for feeding 'book' with bitmaps which are used for rendering
   * pages.
   */
  public interface BitmapProvider {

    /**
     * Called once new bitmap is needed. Width and height are in pixels telling
     * the size it will be drawn on screen and following them ensures that
     * aspect ratio remains. But it's possible to return bitmap of any size
     * though.<br/>
     * <br/>
     * Index is a number between 0 and getBitmapCount() - 1.
     */
    public Bitmap getBitmap(int width, int height, int index);

    /**
     * Return number of pages/bitmaps available.
     */
    public int getBitmapCount();

    void playAudioStimuli();

    public void playSound();

    public void recordTouchPoint(Touch touch, int stimuli);
  }

  /**
   * Simple holder for pointer position.
   */
  private class PointerPosition {
    PointF mPos = new PointF();
    float  mPressure;
  }

  /**
   * Observer interface for handling CurlView size changes.
   */
  public interface SizeChangedObserver {

    /**
     * Called once CurlView size changes.
     */
    public void onSizeChanged(int width, int height);
  }

  private static final int    CURL_LEFT              = 1;
  // Curl state. We are flipping none, left or right page.
  private static final int    CURL_NONE              = 0;

  private static final int    CURL_RIGHT             = 2;
  // Constants for mAnimationTargetEvent.
  private static final int    SET_CURL_TO_LEFT       = 1;

  private static final int    SET_CURL_TO_RIGHT      = 2;
  // Shows one page at the center of view.
  public static final int     SHOW_ONE_PAGE          = 1;
  // Shows two pages side by side.
  public static final int     SHOW_TWO_PAGES         = 2;

  public static final String  TAG                    = "CurlView";
  private boolean             mAllowLastPageCurl     = true;
  private boolean             mAnimate               = false;
  private long                mAnimationDurationTime = 300;

  private PointF              mAnimationSource       = new PointF();

  private long                mAnimationStartTime;
  private PointF              mAnimationTarget       = new PointF();

  private int                 mAnimationTargetEvent;
  private BitmapProvider      mBitmapProvider;
  private PointF              mCurlDir               = new PointF();
  private PointF              mCurlPos               = new PointF();

  private int                 mCurlState             = CURL_NONE;
  // Current page index. This is always showed on right page.
  private int                 mCurrentIndex          = 0;
  // Start position for dragging.
  private PointF              mDragStartPos          = new PointF();
  private boolean             mEnableTouchPressure   = false;
  private Handler             mHandlerDelayStimuli   = new Handler();
  private int                 mPageBitmapHeight      = -1;
  // Bitmap size. These are updated from renderer once it's initialized.
  private int                 mPageBitmapWidth       = -1;

  // Page meshes. Left and right meshes are 'static' while curl is used to
  // show page flipping.
  private CurlMesh            mPageCurl;
  private CurlMesh            mPageLeft;

  private CurlMesh            mPageRight;
  private PointerPosition     mPointerPos            = new PointerPosition();
  private CurlRenderer        mRenderer;

  private boolean             mRenderLeftPage        = true;

  private SizeChangedObserver mSizeChangedObserver;

  // One page is the default.
  private int                 mViewMode              = SHOW_ONE_PAGE;

  public int                  numberOfPages          = 1;

  /**
   * Default constructor.
   */
  public CurlView(Context ctx) {
    super(ctx);
    this.init(ctx);
  }

  /**
   * Default constructor.
   */
  public CurlView(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
    this.init(ctx);
  }

  /**
   * Default constructor.
   */
  public CurlView(Context ctx, AttributeSet attrs, int defStyle) {
    this(ctx, attrs);
  }

  /**
   * Set current page index.
   */
  public int getCurrentIndex() {
    return this.mCurrentIndex;
  }

  /**
   * Initialize method.
   */
  private void init(Context ctx) {
    this.mRenderer = new CurlRenderer(this);
    this.setRenderer(this.mRenderer);
    this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    this.setOnTouchListener(this);

    // Even though left and right pages are static we have to allocate room
    // for curl on them too as we are switching meshes. Another way would be
    // to swap texture ids only.
    this.mPageLeft = new CurlMesh(10);
    this.mPageRight = new CurlMesh(10);
    this.mPageCurl = new CurlMesh(10);
    this.mPageLeft.setFlipTexture(false);
    this.mPageRight.setFlipTexture(false);
  }

  @Override
  public void onDrawFrame() {
    // We are not animating.
    if (this.mAnimate == false) {
      return;
    }

    long currentTime = System.currentTimeMillis();
    // If animation is done.
    if (currentTime >= this.mAnimationStartTime + this.mAnimationDurationTime) {
      if (this.mAnimationTargetEvent == SET_CURL_TO_RIGHT) {
        // Switch curled page to right.
        CurlMesh right = this.mPageCurl;
        CurlMesh curl = this.mPageRight;// the new page
        right.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
        right.setFlipTexture(false);
        right.reset();
        this.mRenderer.removeCurlMesh(curl);
        this.mPageCurl = curl;
        this.mPageRight = right;
        // If we were curling left page update current index.
        if (this.mCurlState == CURL_LEFT) {
          this.mCurrentIndex = this.mCurrentIndex - this.numberOfPages;
          Log.d(TAG, "Subtracting " + this.numberOfPages);
        }
      } else if (this.mAnimationTargetEvent == SET_CURL_TO_LEFT) {
        // Switch curled page to left.
        CurlMesh left = this.mPageCurl;
        CurlMesh curl = this.mPageLeft; // the new page
        left.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
        left.setFlipTexture(false);
        left.reset();
        this.mRenderer.removeCurlMesh(curl);
        if (!this.mRenderLeftPage) {
          this.mRenderer.removeCurlMesh(left);
        }
        this.mPageCurl = curl;
        this.mPageLeft = left;
        // If we were curling right page update current index.
        if (this.mCurlState == CURL_RIGHT) {
          this.mCurrentIndex = this.mCurrentIndex + this.numberOfPages;
          Log.d(TAG, "Adding " + this.numberOfPages);
        }
      }
      this.mCurlState = CURL_NONE;
      this.mAnimate = false;
      this.requestRender();
    } else {
      this.mPointerPos.mPos.set(this.mAnimationSource);
      float t = (float) Math.sqrt((double) (currentTime - this.mAnimationStartTime) / this.mAnimationDurationTime);
      this.mPointerPos.mPos.x += (this.mAnimationTarget.x - this.mAnimationSource.x) * t;
      this.mPointerPos.mPos.y += (this.mAnimationTarget.y - this.mAnimationSource.y) * t;
      this.updateCurlPos(this.mPointerPos);
    }
  }

  @Override
  public void onPageSizeChanged(int width, int height) {
    this.mPageBitmapWidth = width;
    this.mPageBitmapHeight = height;
    this.updateBitmaps();
    this.requestRender();
  }

  @Override
  public void onSizeChanged(int w, int h, int ow, int oh) {
    super.onSizeChanged(w, h, ow, oh);
    this.requestRender();
    if (this.mSizeChangedObserver != null) {
      this.mSizeChangedObserver.onSizeChanged(w, h);
    }
  }

  @Override
  public void onSurfaceCreated() {
    // In case surface is recreated, let page meshes drop allocated texture
    // ids and ask for new ones. There's no need to set textures here as
    // onPageSizeChanged should be called later on.
    this.mPageLeft.resetTexture();
    this.mPageRight.resetTexture();
    this.mPageCurl.resetTexture();
  }

  @Override
  public boolean onTouch(View view, MotionEvent me) {
    // No dragging during animation at the moment.
    // TODO: Stop animation on touch event and return to drag mode.
    if (this.mAnimate || this.mBitmapProvider == null) {
      return false;
    }

    // We need page rects quite extensively so get them for later use.
    RectF rightRect = this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
    RectF leftRect = this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);

    // Store pointer position.
    this.mPointerPos.mPos.set(me.getX(), me.getY());
    this.mRenderer.translate(this.mPointerPos.mPos);
    if (this.mEnableTouchPressure) {
      this.mPointerPos.mPressure = me.getPressure();
    } else {
      this.mPointerPos.mPressure = 0.8f;
    }

    Touch t = new Touch();
    t.x = me.getX();
    t.y = me.getY();
    this.mBitmapProvider.recordTouchPoint(t, this.mCurrentIndex);

    switch (me.getAction()) {
    case MotionEvent.ACTION_DOWN: {

      // Once we receive pointer down event its position is mapped to
      // right or left edge of page and that'll be the position from where
      // user is holding the paper to make curl happen.
      this.mDragStartPos.set(this.mPointerPos.mPos);

      /*
       * Dont turn react if the touch is in the middle of the page
       */
      int width = this.mPageBitmapWidth;
      int turnSensitiveMarginWidth = width / 6;
      if (turnSensitiveMarginWidth < 100) {
        turnSensitiveMarginWidth = 100;
      }
      if ((me.getX() > turnSensitiveMarginWidth && me.getX() < width - turnSensitiveMarginWidth)) {
        return false;
      }
      // First we make sure it's not over or below page. Pages are
      // supposed to be same height so it really doesn't matter do we use
      // left or right one.
      if (this.mDragStartPos.y > rightRect.top) {
        this.mDragStartPos.y = rightRect.top;
      } else if (this.mDragStartPos.y < rightRect.bottom) {
        this.mDragStartPos.y = rightRect.bottom;
      }

      // Then we have to make decisions for the user whether curl is going
      // to happen from left or right, and on which page.
      if (this.mViewMode == SHOW_TWO_PAGES) {
        // If we have an open book and pointer is on the left from right
        // page we'll mark drag position to left edge of left page.
        // Additionally checking mCurrentIndex is higher than zero tells
        // us there is a visible page at all.
        if (this.mDragStartPos.x < rightRect.left && this.mCurrentIndex > 1) {
          this.mDragStartPos.x = leftRect.left;
          this.startCurl(CURL_LEFT);
        }
        // Otherwise check pointer is on right page's side.
        else if (this.mDragStartPos.x >= rightRect.left
            && this.mCurrentIndex < this.mBitmapProvider.getBitmapCount() - this.numberOfPages) {
          this.mDragStartPos.x = rightRect.right;
          if (!this.mAllowLastPageCurl && this.mCurrentIndex >= this.mBitmapProvider.getBitmapCount()) {
            return false;
          }
          this.startCurl(CURL_RIGHT);
        }
      } else if (this.mViewMode == SHOW_ONE_PAGE) {
        float halfX = (rightRect.right + rightRect.left) / 2;
        if (this.mDragStartPos.x < halfX && this.mCurrentIndex > 0) {
          this.mDragStartPos.x = rightRect.left;
          this.startCurl(CURL_LEFT);
        } else if (this.mDragStartPos.x >= halfX && this.mCurrentIndex < this.mBitmapProvider.getBitmapCount()) {
          this.mDragStartPos.x = rightRect.right;
          if (!this.mAllowLastPageCurl && this.mCurrentIndex >= this.mBitmapProvider.getBitmapCount()) {
            return false;
          }
          this.startCurl(CURL_RIGHT);
        }
      }
      // If we have are in curl state, let this case clause flow through
      // to next one. We have pointer position and drag position defined
      // and this will create first render request given these points.
      if (this.mCurlState == CURL_NONE) {
        return false;
      }
    }
    case MotionEvent.ACTION_MOVE: {
      this.updateCurlPos(this.mPointerPos);
      break;
    }
    case MotionEvent.ACTION_CANCEL:
    case MotionEvent.ACTION_UP: {
      if (this.mCurlState == CURL_LEFT || this.mCurlState == CURL_RIGHT) {
        // Animation source is the point from where animation starts.
        // Also it's handled in a way we actually simulate touch events
        // meaning the output is exactly the same as if user drags the
        // page to other side. While not producing the best looking
        // result (which is easier done by altering curl position and/or
        // direction directly), this is done in a hope it made code a
        // bit more readable and easier to maintain.
        this.mAnimationSource.set(this.mPointerPos.mPos);
        this.mAnimationStartTime = System.currentTimeMillis();

        this.mHandlerDelayStimuli.postDelayed(new Runnable() {
          @Override
          public void run() {
            CurlView.this.mBitmapProvider.playSound();
            CurlView.this.mBitmapProvider.playAudioStimuli();
          }
        }, this.mAnimationDurationTime);

        // Given the explanation, here we decide whether to simulate
        // drag to left or right end.
        if ((this.mViewMode == SHOW_ONE_PAGE && this.mPointerPos.mPos.x > (rightRect.left + rightRect.right) / 2)
            || this.mViewMode == SHOW_TWO_PAGES && this.mPointerPos.mPos.x > rightRect.left) {
          // On right side target is always right page's right border.
          this.mAnimationTarget.set(this.mDragStartPos);
          this.mAnimationTarget.x = this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).right;
          this.mAnimationTargetEvent = SET_CURL_TO_RIGHT;
        } else {
          // On left side target depends on visible pages.
          this.mAnimationTarget.set(this.mDragStartPos);
          if (this.mCurlState == CURL_RIGHT || this.mViewMode == SHOW_TWO_PAGES) {
            this.mAnimationTarget.x = leftRect.left;
          } else {
            this.mAnimationTarget.x = rightRect.left;
          }
          this.mAnimationTargetEvent = SET_CURL_TO_LEFT;
        }
        this.mAnimate = true;
        this.requestRender();
      }

      break;
    }
    }

    return true;
  }

  /**
   * Allow the last page to curl.
   */
  public void setAllowLastPageCurl(boolean allowLastPageCurl) {
    this.mAllowLastPageCurl = allowLastPageCurl;
  }

  /**
   * Sets background color - or OpenGL clear color to be more precise. Color is
   * a 32bit value consisting of 0xAARRGGBB and is extracted using
   * android.graphics.Color eventually.
   */
  @Override
  public void setBackgroundColor(int color) {
    this.mRenderer.setBackgroundColor(color);
    this.requestRender();
  }

  /**
   * Update/set bitmap provider.
   */
  public void setBitmapProvider(BitmapProvider bitmapProvider) {
    this.mBitmapProvider = bitmapProvider;
    this.mCurrentIndex = 0;
    this.updateBitmaps();
    this.requestRender();
  }

  /**
   * Sets mPageCurl curl position.
   */
  private void setCurlPos(PointF curlPos, PointF curlDir, double radius) {

    // First reposition curl so that page doesn't 'rip off' from book.
    if (this.mCurlState == CURL_RIGHT || (this.mCurlState == CURL_LEFT && this.mViewMode == SHOW_ONE_PAGE)) {

      if (this.mCurrentIndex + 1 <= this.mBitmapProvider.getBitmapCount() - this.numberOfPages) {// TODO
        // fix
        // for
        // 1
        // page
        // here
        Bitmap bitmap = this.mBitmapProvider.getBitmap(this.mPageBitmapWidth, this.mPageBitmapHeight,
            this.mCurrentIndex + 1);
        this.mPageCurl.setBitmap(bitmap);
      }

      RectF pageRect = this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
      if (curlPos.x >= pageRect.right) {
        this.mPageCurl.reset();
        this.requestRender();
        return;
      }
      if (curlPos.x < pageRect.left) {
        curlPos.x = pageRect.left;
      }
      if (curlDir.y != 0) {
        float diffX = curlPos.x - pageRect.left;
        float leftY = curlPos.y + (diffX * curlDir.x / curlDir.y);
        if (curlDir.y < 0 && leftY < pageRect.top) {
          curlDir.x = curlPos.y - pageRect.top;
          curlDir.y = pageRect.left - curlPos.x;
        } else if (curlDir.y > 0 && leftY > pageRect.bottom) {
          curlDir.x = pageRect.bottom - curlPos.y;
          curlDir.y = curlPos.x - pageRect.left;
        }
      }
    } else if (this.mCurlState == CURL_LEFT) {
      Log.d(TAG, "Curling left need to decide if its one or two pages to turn");
      if (this.mCurrentIndex - this.numberOfPages >= 0) {// TODO fix for 1 page
                                                         // here
        Bitmap bitmap = this.mBitmapProvider.getBitmap(this.mPageBitmapWidth, this.mPageBitmapHeight,
            this.mCurrentIndex - this.numberOfPages);
        this.mPageCurl.setBitmap(bitmap);
      }

      RectF pageRect = this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
      if (curlPos.x <= pageRect.left) {
        this.mPageCurl.reset();
        this.requestRender();
        return;
      }
      if (curlPos.x > pageRect.right) {
        curlPos.x = pageRect.right;
      }
      if (curlDir.y != 0) {
        float diffX = curlPos.x - pageRect.right;
        float rightY = curlPos.y + (diffX * curlDir.x / curlDir.y);
        if (curlDir.y < 0 && rightY < pageRect.top) {
          curlDir.x = pageRect.top - curlPos.y;
          curlDir.y = curlPos.x - pageRect.right;
        } else if (curlDir.y > 0 && rightY > pageRect.bottom) {
          curlDir.x = curlPos.y - pageRect.bottom;
          curlDir.y = pageRect.right - curlPos.x;
        }
      }
    }

    // Finally normalize direction vector and do rendering.
    double dist = Math.sqrt(curlDir.x * curlDir.x + curlDir.y * curlDir.y);
    if (dist != 0) {
      curlDir.x /= dist;
      curlDir.y /= dist;
      this.mPageCurl.curl(curlPos, curlDir, radius);
    } else {
      this.mPageCurl.reset();
    }

    this.requestRender();

  }

  /**
   * Set page index.
   */
  public void setCurrentIndex(int index) {
    if (this.mBitmapProvider == null || index <= 0) {
      this.mCurrentIndex = 0;
    } else {
      this.mCurrentIndex = Math.min(index, this.mBitmapProvider.getBitmapCount() - 1);
    }
    this.updateBitmaps();
    this.requestRender();
  }

  /**
   * If set to true, touch event pressure information is used to adjust curl
   * radius. The more you press, the flatter the curl becomes. This is somewhat
   * experimental and results may vary significantly between devices. On
   * emulator pressure information seems to be flat 1.0f which is maximum value
   * and therefore not very much of use.
   */
  public void setEnableTouchPressure(boolean enableTouchPressure) {
    this.mEnableTouchPressure = enableTouchPressure;
  }

  /**
   * Set margins (or padding). Note: margins are proportional. Meaning a value
   * of .1f will produce a 10% margin.
   */
  public void setMargins(float left, float top, float right, float bottom) {
    this.mRenderer.setMargins(left, top, right, bottom);
  }

  /**
   * Setter for whether left side page is rendered. This is useful mostly for
   * situations where right (main) page is aligned to left side of screen and
   * left page is not visible anyway.
   */
  public void setRenderLeftPage(boolean renderLeftPage) {
    this.mRenderLeftPage = renderLeftPage;
  }

  /**
   * Sets SizeChangedObserver for this View. Call back method is called from
   * this View's onSizeChanged method.
   */
  public void setSizeChangedObserver(SizeChangedObserver observer) {
    this.mSizeChangedObserver = observer;
  }

  /**
   * Sets view mode. Value can be either SHOW_ONE_PAGE or SHOW_TWO_PAGES. In
   * former case right page is made size of display, and in latter case two
   * pages are laid on visible area.
   */
  public void setViewMode(int viewMode) {
    switch (viewMode) {
    case SHOW_ONE_PAGE:
      this.mViewMode = viewMode;
      this.mRenderer.setViewMode(CurlRenderer.SHOW_ONE_PAGE);
      this.numberOfPages = 1;
      break;
    case SHOW_TWO_PAGES:
      this.mViewMode = viewMode;
      this.mRenderer.setViewMode(CurlRenderer.SHOW_TWO_PAGES);
      this.numberOfPages = 2;
      break;
    }
  }

  /**
   * Switches meshes and loads new bitmaps if available.
   */
  private void startCurl(int page) {
    switch (page) {

    // Once right side page is curled, first right page is assigned into
    // curled page. And if there are more bitmaps available new bitmap is
    // loaded into right side mesh.
    case CURL_RIGHT: {
      // Remove meshes from renderer.
      this.mRenderer.removeCurlMesh(this.mPageLeft);
      this.mRenderer.removeCurlMesh(this.mPageRight);
      this.mRenderer.removeCurlMesh(this.mPageCurl);

      // We are curling right page.
      CurlMesh curl = this.mPageRight;
      this.mPageRight = this.mPageCurl;
      this.mPageCurl = curl;

      // If there is something to show on left page, simply add it to
      // renderer.
      if (this.mCurrentIndex > 0) {
        this.mPageLeft.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
        this.mPageLeft.reset();
        if (this.mRenderLeftPage) {
          this.mRenderer.addCurlMesh(this.mPageLeft);
        }
      }

      // If there is new/next available, set it to right page.
      if (this.mCurrentIndex < this.mBitmapProvider.getBitmapCount() - this.numberOfPages) {
        Bitmap bitmap = this.mBitmapProvider.getBitmap(this.mPageBitmapWidth, this.mPageBitmapHeight,
            this.mCurrentIndex + this.numberOfPages);
        this.mPageRight.setBitmap(bitmap);
        this.mPageRight.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
        this.mPageRight.setFlipTexture(false);
        this.mPageRight.reset();
        this.mRenderer.addCurlMesh(this.mPageRight);
      }

      // Add curled page to renderer.
      this.mPageCurl.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
      this.mPageCurl.setFlipTexture(false);
      this.mPageCurl.reset();
      this.mRenderer.addCurlMesh(this.mPageCurl);

      this.mCurlState = CURL_RIGHT;
      break;
    }

    // On left side curl, left page is assigned to curled page. And if
    // there are more bitmaps available before currentIndex, new bitmap
    // is loaded into left page.
    case CURL_LEFT: {
      // Remove meshes from renderer.
      this.mRenderer.removeCurlMesh(this.mPageLeft);
      this.mRenderer.removeCurlMesh(this.mPageRight);
      this.mRenderer.removeCurlMesh(this.mPageCurl);

      // We are curling left page.
      CurlMesh curl = this.mPageLeft;
      this.mPageLeft = this.mPageCurl;
      this.mPageCurl = curl;

      // If there is new/previous bitmap available load it to left page.
      if (this.mCurrentIndex > 1) {
        Bitmap bitmap = this.mBitmapProvider.getBitmap(this.mPageBitmapWidth, this.mPageBitmapHeight,
            this.mCurrentIndex - this.numberOfPages - 1);
        this.mPageLeft.setBitmap(bitmap);
        this.mPageLeft.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
        this.mPageLeft.setFlipTexture(false);
        this.mPageLeft.reset();
        if (this.mRenderLeftPage) {
          this.mRenderer.addCurlMesh(this.mPageLeft);
        }
      }

      // If there is something to show on right page add it to renderer.
      if (this.mCurrentIndex < this.mBitmapProvider.getBitmapCount() - this.numberOfPages) {
        this.mPageRight.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
        this.mPageRight.reset();
        this.mRenderer.addCurlMesh(this.mPageRight);
      }

      // How dragging previous page happens depends on view mode.
      if (this.mViewMode == SHOW_ONE_PAGE) {
        this.mPageCurl.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
        this.mPageCurl.setFlipTexture(false);
      } else {
        this.mPageCurl.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
        this.mPageCurl.setFlipTexture(false);
      }
      this.mPageCurl.reset();
      this.mRenderer.addCurlMesh(this.mPageCurl);

      this.mCurlState = CURL_LEFT;
      break;
    }

    }
    // mBitmapProvider.playSound();
  }

  /**
   * Updates bitmaps for left and right meshes.
   */
  private void updateBitmaps() {
    if (this.mBitmapProvider == null || this.mPageBitmapWidth <= 0 || this.mPageBitmapHeight <= 0) {
      return;
    }

    // Remove meshes from renderer.
    this.mRenderer.removeCurlMesh(this.mPageLeft);
    this.mRenderer.removeCurlMesh(this.mPageRight);
    this.mRenderer.removeCurlMesh(this.mPageCurl);

    int leftIdx = this.mCurrentIndex - 1;
    int rightIdx = this.mCurrentIndex;
    int curlIdx = -1;
    if (this.mCurlState == CURL_LEFT) {
      curlIdx = leftIdx - 1;
      leftIdx--;
    } else if (this.mCurlState == CURL_RIGHT) {
      curlIdx = rightIdx + this.numberOfPages;
      rightIdx++;
    }

    if (rightIdx >= 0 && rightIdx < this.mBitmapProvider.getBitmapCount() - 1) {
      Bitmap bitmap = this.mBitmapProvider.getBitmap(this.mPageBitmapWidth, this.mPageBitmapHeight, rightIdx);
      this.mPageRight.setBitmap(bitmap);
      this.mPageRight.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
      this.mPageRight.reset();
      this.mRenderer.addCurlMesh(this.mPageRight);
    }
    if (leftIdx >= 0 && leftIdx < this.mBitmapProvider.getBitmapCount() - 1) {
      Bitmap bitmap = this.mBitmapProvider.getBitmap(this.mPageBitmapWidth, this.mPageBitmapHeight, leftIdx);
      this.mPageLeft.setBitmap(bitmap);
      this.mPageLeft.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
      this.mPageLeft.reset();
      if (this.mRenderLeftPage) {
        this.mRenderer.addCurlMesh(this.mPageLeft);
      }
    }
    if (curlIdx >= 0 && curlIdx < this.mBitmapProvider.getBitmapCount() - 1) {
      Bitmap bitmap = this.mBitmapProvider.getBitmap(this.mPageBitmapWidth, this.mPageBitmapHeight, curlIdx);
      this.mPageCurl.setBitmap(bitmap);
      if (this.mCurlState == CURL_RIGHT || (this.mCurlState == CURL_LEFT && this.mViewMode == SHOW_TWO_PAGES)) {
        this.mPageCurl.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
      } else {
        this.mPageCurl.setRect(this.mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
      }
      this.mPageCurl.reset();
      this.mRenderer.addCurlMesh(this.mPageCurl);
    }
  }

  /**
   * Updates curl position.
   */
  private void updateCurlPos(PointerPosition pointerPos) {

    // Default curl radius.
    double radius = this.mRenderer.getPageRect(CURL_RIGHT).width() / 3;
    // TODO: This is not an optimal solution. Based on feedback received so
    // far; pressure is not very accurate, it may be better not to map
    // coefficient to range [0f, 1f] but something like [.2f, 1f] instead.
    // Leaving it as is until get my hands on a real device. On emulator
    // this doesn't work anyway.
    radius *= Math.max(1f - pointerPos.mPressure, 0f);
    // NOTE: Here we set pointerPos to mCurlPos. It might be a bit confusing
    // later to see e.g "mCurlPos.x - mDragStartPos.x" used. But it's
    // actually pointerPos we are doing calculations against. Why? Simply to
    // optimize code a bit with the cost of making it unreadable. Otherwise
    // we had to this in both of the next if-else branches.
    this.mCurlPos.set(pointerPos.mPos);

    // If curl happens on right page, or on left page on two page mode,
    // we'll calculate curl position from pointerPos.
    if (this.mCurlState == CURL_RIGHT || (this.mCurlState == CURL_LEFT && this.mViewMode == SHOW_TWO_PAGES)) {

      this.mCurlDir.x = this.mCurlPos.x - this.mDragStartPos.x;
      this.mCurlDir.y = this.mCurlPos.y - this.mDragStartPos.y;
      float dist = (float) Math.sqrt(this.mCurlDir.x * this.mCurlDir.x + this.mCurlDir.y * this.mCurlDir.y);

      // Adjust curl radius so that if page is dragged far enough on
      // opposite side, radius gets closer to zero.
      float pageWidth = this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).width();
      double curlLen = radius * Math.PI;
      if (dist > (pageWidth * 2) - curlLen) {
        curlLen = Math.max((pageWidth * 2) - dist, 0f);
        radius = curlLen / Math.PI;
      }

      // Actual curl position calculation.
      if (dist >= curlLen) {
        double translate = (dist - curlLen) / 2;
        if (this.mViewMode == SHOW_TWO_PAGES) {
          this.mCurlPos.x -= this.mCurlDir.x * translate / dist;
        } else {
          float pageLeftX = this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).left;
          radius = Math.max(Math.min(this.mCurlPos.x - pageLeftX, radius), 0f);
        }
        this.mCurlPos.y -= this.mCurlDir.y * translate / dist;
      } else {
        double angle = Math.PI * Math.sqrt(dist / curlLen);
        double translate = radius * Math.sin(angle);
        this.mCurlPos.x += this.mCurlDir.x * translate / dist;
        this.mCurlPos.y += this.mCurlDir.y * translate / dist;
      }
    }
    // Otherwise we'll let curl follow pointer position.
    else if (this.mCurlState == CURL_LEFT) {

      // Adjust radius regarding how close to page edge we are.
      float pageLeftX = this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).left;
      radius = Math.max(Math.min(this.mCurlPos.x - pageLeftX, radius), 0f);

      float pageRightX = this.mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).right;
      this.mCurlPos.x -= Math.min(pageRightX - this.mCurlPos.x, radius);
      this.mCurlDir.x = this.mCurlPos.x + this.mDragStartPos.x;
      this.mCurlDir.y = this.mCurlPos.y - this.mDragStartPos.y;
    }

    this.setCurlPos(this.mCurlPos, this.mCurlDir, radius);
  }

}
