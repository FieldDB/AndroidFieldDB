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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLUtils;

/**
 * Class implementing actual curl/page rendering.
 * 
 * @author harism
 */
public class CurlMesh {

  /**
   * Simple fixed size array implementation.
   */
  private class Array<T> {
    private Object[] mArray;
    private int      mCapacity;
    private int      mSize;

    public Array(int capacity) {
      this.mCapacity = capacity;
      this.mArray = new Object[capacity];
    }

    public void add(int index, T item) {
      if (index < 0 || index > this.mSize || this.mSize >= this.mCapacity) {
        throw new IndexOutOfBoundsException();
      }
      for (int i = this.mSize; i > index; --i) {
        this.mArray[i] = this.mArray[i - 1];
      }
      this.mArray[index] = item;
      ++this.mSize;
    }

    public void add(T item) {
      if (this.mSize >= this.mCapacity) {
        throw new IndexOutOfBoundsException();
      }
      this.mArray[this.mSize++] = item;
    }

    public void addAll(Array<T> array) {
      if (this.mSize + array.size() > this.mCapacity) {
        throw new IndexOutOfBoundsException();
      }
      for (int i = 0; i < array.size(); ++i) {
        this.mArray[this.mSize++] = array.get(i);
      }
    }

    public void clear() {
      this.mSize = 0;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
      if (index < 0 || index >= this.mSize) {
        throw new IndexOutOfBoundsException();
      }
      return (T) this.mArray[index];
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
      if (index < 0 || index >= this.mSize) {
        throw new IndexOutOfBoundsException();
      }
      T item = (T) this.mArray[index];
      for (int i = index; i < this.mSize - 1; ++i) {
        this.mArray[i] = this.mArray[i + 1];
      }
      --this.mSize;
      return item;
    }

    public int size() {
      return this.mSize;
    }

  }

  /**
   * Holder for shadow vertex information.
   */
  private class ShadowVertex {
    public double mPenumbraColor;
    public double mPenumbraX;
    public double mPenumbraY;
    public double mPosX;
    public double mPosY;
    public double mPosZ;
  }

  /**
   * Holder for vertex information.
   */
  private class Vertex {
    public double mAlpha;
    public double mColor;
    public double mPenumbraX;
    public double mPenumbraY;
    public double mPosX;
    public double mPosY;
    public double mPosZ;
    public double mTexX;
    public double mTexY;

    public Vertex() {
      this.mPosX = this.mPosY = this.mPosZ = this.mTexX = this.mTexY = 0;
      this.mColor = this.mAlpha = 1;
    }

    public void rotateZ(double theta) {
      double cos = Math.cos(theta);
      double sin = Math.sin(theta);
      double x = this.mPosX * cos + this.mPosY * sin;
      double y = this.mPosX * -sin + this.mPosY * cos;
      this.mPosX = x;
      this.mPosY = y;
      double px = this.mPenumbraX * cos + this.mPenumbraY * sin;
      double py = this.mPenumbraX * -sin + this.mPenumbraY * cos;
      this.mPenumbraX = px;
      this.mPenumbraY = py;
    }

    public void set(Vertex vertex) {
      this.mPosX = vertex.mPosX;
      this.mPosY = vertex.mPosY;
      this.mPosZ = vertex.mPosZ;
      this.mTexX = vertex.mTexX;
      this.mTexY = vertex.mTexY;
      this.mPenumbraX = vertex.mPenumbraX;
      this.mPenumbraY = vertex.mPenumbraY;
      this.mColor = vertex.mColor;
      this.mAlpha = vertex.mAlpha;
    }

    public void translate(double dx, double dy) {
      this.mPosX += dx;
      this.mPosY += dy;
    }
  }

  // Alpha values for front and back facing texture.
  private static final double  BACKFACE_ALPHA        = .2f;

  // Flag for rendering some lines used for developing. Shows
  // curl position and one for the direction from the
  // position given. Comes handy once playing around with different
  // ways for following pointer.
  private static final boolean DRAW_CURL_POSITION    = false;
  // Flag for drawing polygon outlines. Using this flag crashes on emulator
  // due to reason unknown to me. Leaving it here anyway as seeing polygon
  // outlines gives good insight how original rectangle is divided.
  private static final boolean DRAW_POLYGON_OUTLINES = false;

  // Flag for enabling shadow rendering.
  private static final boolean DRAW_SHADOW           = true;
  // Flag for texture rendering. While this is likely something you
  // don't want to do it's been used for development purposes as texture
  // rendering is rather slow on emulator.
  private static final boolean DRAW_TEXTURE          = true;
  private static final double  FRONTFACE_ALPHA       = 1f;

  // Colors for shadow. Inner one is the color drawn next to surface where
  // shadowed area starts and outer one is color shadow ends to.
  private static final float[] SHADOW_INNER_COLOR    = { 0f, 0f, 0f, .5f };
  private static final float[] SHADOW_OUTER_COLOR    = { 0f, 0f, 0f, .0f };

  private Bitmap               mBitmap;
  private FloatBuffer          mColors;
  private FloatBuffer          mCurlPositionLines;
  // For testing purposes.
  private int                  mCurlPositionLinesCount;
  private int                  mDropShadowCount;

  private Array<ShadowVertex>  mDropShadowVertices;
  // Boolean for 'flipping' texture sideways.
  private boolean              mFlipTexture          = false;
  private Array<Vertex>        mIntersections;
  // Maximum number of split lines used for creating a curl.
  private int                  mMaxCurlSplits;

  private Array<Vertex>        mOutputVertices;

  // Bounding rectangle for this mesh. mRectagle[0] = top-left corner,
  // mRectangle[1] = bottom-left, mRectangle[2] = top-right and mRectangle[3]
  // bottom-right.
  private Vertex[]             mRectangle            = new Vertex[4];

  private Array<Vertex>        mRotatedVertices;
  private Array<Double>        mScanLines;
  private int                  mSelfShadowCount;

  private Array<ShadowVertex>  mSelfShadowVertices;
  private FloatBuffer          mShadowColors;
  private FloatBuffer          mShadowVertices;
  private Array<ShadowVertex>  mTempShadowVertices;
  // Let's avoid using 'new' as much as possible. Meaning we introduce arrays
  // once here and reuse them on runtime. Doesn't really have very much effect
  // but avoids some garbage collections from happening.
  private Array<Vertex>        mTempVertices;
  private FloatBuffer          mTexCoords;
  // One and only texture id.
  private int[]                mTextureIds;
  private RectF                mTextureRect          = new RectF();

  // Buffers for feeding rasterizer.
  private FloatBuffer          mVertices;

  private int                  mVerticesCountBack;

  private int                  mVerticesCountFront;

  /**
   * Constructor for mesh object.
   * 
   * @param maxCurlSplits
   *          Maximum number curl can be divided into. The bigger the value the
   *          smoother curl will be. With the cost of having more polygons for
   *          drawing.
   */
  public CurlMesh(int maxCurlSplits) {
    // There really is no use for 0 splits.
    this.mMaxCurlSplits = maxCurlSplits < 1 ? 1 : maxCurlSplits;

    this.mScanLines = new Array<Double>(maxCurlSplits + 2);
    this.mOutputVertices = new Array<Vertex>(7);
    this.mRotatedVertices = new Array<Vertex>(4);
    this.mIntersections = new Array<Vertex>(2);
    this.mTempVertices = new Array<Vertex>(7 + 4);
    for (int i = 0; i < 7 + 4; ++i) {
      this.mTempVertices.add(new Vertex());
    }

    if (DRAW_SHADOW) {
      this.mSelfShadowVertices = new Array<ShadowVertex>((this.mMaxCurlSplits + 2) * 2);
      this.mDropShadowVertices = new Array<ShadowVertex>((this.mMaxCurlSplits + 2) * 2);
      this.mTempShadowVertices = new Array<ShadowVertex>((this.mMaxCurlSplits + 2) * 2);
      for (int i = 0; i < (this.mMaxCurlSplits + 2) * 2; ++i) {
        this.mTempShadowVertices.add(new ShadowVertex());
      }
    }

    // Rectangle consists of 4 vertices. Index 0 = top-left, index 1 =
    // bottom-left, index 2 = top-right and index 3 = bottom-right.
    for (int i = 0; i < 4; ++i) {
      this.mRectangle[i] = new Vertex();
    }
    // Set up shadow penumbra direction to each vertex. We do fake 'self
    // shadow' calculations based on this information.
    this.mRectangle[0].mPenumbraX = this.mRectangle[1].mPenumbraX = this.mRectangle[1].mPenumbraY = this.mRectangle[3].mPenumbraY = -1;
    this.mRectangle[0].mPenumbraY = this.mRectangle[2].mPenumbraX = this.mRectangle[2].mPenumbraY = this.mRectangle[3].mPenumbraX = 1;

    if (DRAW_CURL_POSITION) {
      this.mCurlPositionLinesCount = 3;
      ByteBuffer hvbb = ByteBuffer.allocateDirect(this.mCurlPositionLinesCount * 2 * 2 * 4);
      hvbb.order(ByteOrder.nativeOrder());
      this.mCurlPositionLines = hvbb.asFloatBuffer();
      this.mCurlPositionLines.position(0);
    }

    // There are 4 vertices from bounding rect, max 2 from adding split line
    // to two corners and curl consists of max mMaxCurlSplits lines each
    // outputting 2 vertices.
    int maxVerticesCount = 4 + 2 + (2 * this.mMaxCurlSplits);
    ByteBuffer vbb = ByteBuffer.allocateDirect(maxVerticesCount * 3 * 4);
    vbb.order(ByteOrder.nativeOrder());
    this.mVertices = vbb.asFloatBuffer();
    this.mVertices.position(0);

    if (DRAW_TEXTURE) {
      ByteBuffer tbb = ByteBuffer.allocateDirect(maxVerticesCount * 2 * 4);
      tbb.order(ByteOrder.nativeOrder());
      this.mTexCoords = tbb.asFloatBuffer();
      this.mTexCoords.position(0);
    }

    ByteBuffer cbb = ByteBuffer.allocateDirect(maxVerticesCount * 4 * 4);
    cbb.order(ByteOrder.nativeOrder());
    this.mColors = cbb.asFloatBuffer();
    this.mColors.position(0);

    if (DRAW_SHADOW) {
      int maxShadowVerticesCount = (this.mMaxCurlSplits + 2) * 2 * 2;
      ByteBuffer scbb = ByteBuffer.allocateDirect(maxShadowVerticesCount * 4 * 4);
      scbb.order(ByteOrder.nativeOrder());
      this.mShadowColors = scbb.asFloatBuffer();
      this.mShadowColors.position(0);

      ByteBuffer sibb = ByteBuffer.allocateDirect(maxShadowVerticesCount * 3 * 4);
      sibb.order(ByteOrder.nativeOrder());
      this.mShadowVertices = sibb.asFloatBuffer();
      this.mShadowVertices.position(0);

      this.mDropShadowCount = this.mSelfShadowCount = 0;
    }
  }

  /**
   * Adds vertex to buffers.
   */
  private void addVertex(Vertex vertex) {
    this.mVertices.put((float) vertex.mPosX);
    this.mVertices.put((float) vertex.mPosY);
    this.mVertices.put((float) vertex.mPosZ);
    this.mColors.put((float) vertex.mColor);
    this.mColors.put((float) vertex.mColor);
    this.mColors.put((float) vertex.mColor);
    this.mColors.put((float) vertex.mAlpha);
    if (DRAW_TEXTURE) {
      this.mTexCoords.put((float) vertex.mTexX);
      this.mTexCoords.put((float) vertex.mTexY);
    }
  }

  /**
   * Sets curl for this mesh.
   * 
   * @param curlPos
   *          Position for curl 'center'. Can be any point on line collinear to
   *          curl.
   * @param curlDir
   *          Curl direction, should be normalized.
   * @param radius
   *          Radius of curl.
   */
  public synchronized void curl(PointF curlPos, PointF curlDir, double radius) {

    // First add some 'helper' lines used for development.
    if (DRAW_CURL_POSITION) {
      this.mCurlPositionLines.position(0);

      this.mCurlPositionLines.put(curlPos.x);
      this.mCurlPositionLines.put(curlPos.y - 1.0f);
      this.mCurlPositionLines.put(curlPos.x);
      this.mCurlPositionLines.put(curlPos.y + 1.0f);
      this.mCurlPositionLines.put(curlPos.x - 1.0f);
      this.mCurlPositionLines.put(curlPos.y);
      this.mCurlPositionLines.put(curlPos.x + 1.0f);
      this.mCurlPositionLines.put(curlPos.y);

      this.mCurlPositionLines.put(curlPos.x);
      this.mCurlPositionLines.put(curlPos.y);
      this.mCurlPositionLines.put(curlPos.x + curlDir.x * 2);
      this.mCurlPositionLines.put(curlPos.y + curlDir.y * 2);

      this.mCurlPositionLines.position(0);
    }

    // Actual 'curl' implementation starts here.
    this.mVertices.position(0);
    this.mColors.position(0);
    if (DRAW_TEXTURE) {
      this.mTexCoords.position(0);
    }

    // Calculate curl angle from direction.
    double curlAngle = Math.acos(curlDir.x);
    curlAngle = curlDir.y > 0 ? -curlAngle : curlAngle;

    // Initiate rotated rectangle which's is translated to curlPos and
    // rotated so that curl direction heads to right (1,0). Vertices are
    // ordered in ascending order based on x -coordinate at the same time.
    // And using y -coordinate in very rare case in which two vertices have
    // same x -coordinate.
    this.mTempVertices.addAll(this.mRotatedVertices);
    this.mRotatedVertices.clear();
    for (int i = 0; i < 4; ++i) {
      Vertex v = this.mTempVertices.remove(0);
      v.set(this.mRectangle[i]);
      v.translate(-curlPos.x, -curlPos.y);
      v.rotateZ(-curlAngle);
      int j = 0;
      for (; j < this.mRotatedVertices.size(); ++j) {
        Vertex v2 = this.mRotatedVertices.get(j);
        if (v.mPosX > v2.mPosX) {
          break;
        }
        if (v.mPosX == v2.mPosX && v.mPosY > v2.mPosY) {
          break;
        }
      }
      this.mRotatedVertices.add(j, v);
    }

    // Rotated rectangle lines/vertex indices. We need to find bounding
    // lines for rotated rectangle. After sorting vertices according to
    // their x -coordinate we don't have to worry about vertices at indices
    // 0 and 1. But due to inaccuracy it's possible vertex 3 is not the
    // opposing corner from vertex 0. So we are calculating distance from
    // vertex 0 to vertices 2 and 3 - and altering line indices if needed.
    // Also vertices/lines are given in an order first one has x -coordinate
    // at least the latter one. This property is used in getIntersections to
    // see if there is an intersection.
    int lines[][] = { { 0, 1 }, { 0, 2 }, { 1, 3 }, { 2, 3 } };
    {
      // TODO: There really has to be more 'easier' way of doing this -
      // not including extensive use of sqrt.
      Vertex v0 = this.mRotatedVertices.get(0);
      Vertex v2 = this.mRotatedVertices.get(2);
      Vertex v3 = this.mRotatedVertices.get(3);
      double dist2 = Math.sqrt((v0.mPosX - v2.mPosX) * (v0.mPosX - v2.mPosX) + (v0.mPosY - v2.mPosY)
          * (v0.mPosY - v2.mPosY));
      double dist3 = Math.sqrt((v0.mPosX - v3.mPosX) * (v0.mPosX - v3.mPosX) + (v0.mPosY - v3.mPosY)
          * (v0.mPosY - v3.mPosY));
      if (dist2 > dist3) {
        lines[1][1] = 3;
        lines[2][1] = 2;
      }
    }

    this.mVerticesCountFront = this.mVerticesCountBack = 0;

    if (DRAW_SHADOW) {
      this.mTempShadowVertices.addAll(this.mDropShadowVertices);
      this.mTempShadowVertices.addAll(this.mSelfShadowVertices);
      this.mDropShadowVertices.clear();
      this.mSelfShadowVertices.clear();
    }

    // Length of 'curl' curve.
    double curlLength = Math.PI * radius;
    // Calculate scan lines.
    // TODO: Revisit this code one day. There is room for optimization here.
    this.mScanLines.clear();
    if (this.mMaxCurlSplits > 0) {
      this.mScanLines.add((double) 0);
    }
    for (int i = 1; i < this.mMaxCurlSplits; ++i) {
      this.mScanLines.add((-curlLength * i) / (this.mMaxCurlSplits - 1));
    }
    // As mRotatedVertices is ordered regarding x -coordinate, adding
    // this scan line produces scan area picking up vertices which are
    // rotated completely. One could say 'until infinity'.
    this.mScanLines.add(this.mRotatedVertices.get(3).mPosX - 1);

    // Start from right most vertex. Pretty much the same as first scan area
    // is starting from 'infinity'.
    double scanXmax = this.mRotatedVertices.get(0).mPosX + 1;

    for (int i = 0; i < this.mScanLines.size(); ++i) {
      // Once we have scanXmin and scanXmax we have a scan area to start
      // working with.
      double scanXmin = this.mScanLines.get(i);
      // First iterate 'original' rectangle vertices within scan area.
      for (int j = 0; j < this.mRotatedVertices.size(); ++j) {
        Vertex v = this.mRotatedVertices.get(j);
        // Test if vertex lies within this scan area.
        // TODO: Frankly speaking, can't remember why equality check was
        // added to both ends. Guessing it was somehow related to case
        // where radius=0f, which, given current implementation, could
        // be handled much more effectively anyway.
        if (v.mPosX >= scanXmin && v.mPosX <= scanXmax) {
          // Pop out a vertex from temp vertices.
          Vertex n = this.mTempVertices.remove(0);
          n.set(v);
          // This is done solely for triangulation reasons. Given a
          // rotated rectangle it has max 2 vertices having
          // intersection.
          Array<Vertex> intersections = this.getIntersections(this.mRotatedVertices, lines, n.mPosX);
          // In a sense one could say we're adding vertices always in
          // two, positioned at the ends of intersecting line. And for
          // triangulation to work properly they are added based on y
          // -coordinate. And this if-else is doing it for us.
          if (intersections.size() == 1 && intersections.get(0).mPosY > v.mPosY) {
            // In case intersecting vertex is higher add it first.
            this.mOutputVertices.addAll(intersections);
            this.mOutputVertices.add(n);
          } else if (intersections.size() <= 1) {
            // Otherwise add original vertex first.
            this.mOutputVertices.add(n);
            this.mOutputVertices.addAll(intersections);
          } else {
            // There should never be more than 1 intersecting
            // vertex. But if it happens as a fallback simply skip
            // everything.
            this.mTempVertices.add(n);
            this.mTempVertices.addAll(intersections);
          }
        }
      }

      // Search for scan line intersections.
      Array<Vertex> intersections = this.getIntersections(this.mRotatedVertices, lines, scanXmin);

      // We expect to get 0 or 2 vertices. In rare cases there's only one
      // but in general given a scan line intersecting rectangle there
      // should be 2 intersecting vertices.
      if (intersections.size() == 2) {
        // There were two intersections, add them based on y
        // -coordinate, higher first, lower last.
        Vertex v1 = intersections.get(0);
        Vertex v2 = intersections.get(1);
        if (v1.mPosY < v2.mPosY) {
          this.mOutputVertices.add(v2);
          this.mOutputVertices.add(v1);
        } else {
          this.mOutputVertices.addAll(intersections);
        }
      } else if (intersections.size() != 0) {
        // This happens in a case in which there is a original vertex
        // exactly at scan line or something went very much wrong if
        // there are 3+ vertices. What ever the reason just return the
        // vertices to temp vertices for later use. In former case it
        // was handled already earlier once iterating through
        // mRotatedVertices, in latter case it's better to avoid doing
        // anything with them.
        this.mTempVertices.addAll(intersections);
      }

      // Add vertices found during this iteration to vertex etc buffers.
      while (this.mOutputVertices.size() > 0) {
        Vertex v = this.mOutputVertices.remove(0);
        this.mTempVertices.add(v);

        // Untouched vertices.
        if (i == 0) {
          v.mAlpha = this.mFlipTexture ? BACKFACE_ALPHA : FRONTFACE_ALPHA;
          this.mVerticesCountFront++;
        }
        // 'Completely' rotated vertices.
        else if (i == this.mScanLines.size() - 1 || curlLength == 0) {
          v.mPosX = -(curlLength + v.mPosX);
          v.mPosZ = 2 * radius;
          v.mPenumbraX = -v.mPenumbraX;

          v.mAlpha = this.mFlipTexture ? FRONTFACE_ALPHA : BACKFACE_ALPHA;
          this.mVerticesCountBack++;
        }
        // Vertex lies within 'curl'.
        else {
          // Even though it's not obvious from the if-else clause,
          // here v.mPosX is between [-curlLength, 0]. And we can do
          // calculations around a half cylinder.
          double rotY = Math.PI * (v.mPosX / curlLength);
          v.mPosX = radius * Math.sin(rotY);
          v.mPosZ = radius - (radius * Math.cos(rotY));
          v.mPenumbraX *= Math.cos(rotY);
          // Map color multiplier to [.1f, 1f] range.
          v.mColor = .1f + .9f * Math.sqrt(Math.sin(rotY) + 1);

          if (v.mPosZ >= radius) {
            v.mAlpha = this.mFlipTexture ? FRONTFACE_ALPHA : BACKFACE_ALPHA;
            this.mVerticesCountBack++;
          } else {
            v.mAlpha = this.mFlipTexture ? BACKFACE_ALPHA : FRONTFACE_ALPHA;
            this.mVerticesCountFront++;
          }
        }

        // Move vertex back to 'world' coordinates.
        v.rotateZ(curlAngle);
        v.translate(curlPos.x, curlPos.y);
        this.addVertex(v);

        // Drop shadow is cast 'behind' the curl.
        if (DRAW_SHADOW && v.mPosZ > 0 && v.mPosZ <= radius) {
          ShadowVertex sv = this.mTempShadowVertices.remove(0);
          sv.mPosX = v.mPosX;
          sv.mPosY = v.mPosY;
          sv.mPosZ = v.mPosZ;
          sv.mPenumbraX = (v.mPosZ / 2) * -curlDir.x;
          sv.mPenumbraY = (v.mPosZ / 2) * -curlDir.y;
          sv.mPenumbraColor = v.mPosZ / radius;
          int idx = (this.mDropShadowVertices.size() + 1) / 2;
          this.mDropShadowVertices.add(idx, sv);
        }
        // Self shadow is cast partly over mesh.
        if (DRAW_SHADOW && v.mPosZ > radius) {
          ShadowVertex sv = this.mTempShadowVertices.remove(0);
          sv.mPosX = v.mPosX;
          sv.mPosY = v.mPosY;
          sv.mPosZ = v.mPosZ;
          sv.mPenumbraX = ((v.mPosZ - radius) / 3) * v.mPenumbraX;
          sv.mPenumbraY = ((v.mPosZ - radius) / 3) * v.mPenumbraY;
          sv.mPenumbraColor = (v.mPosZ - radius) / (2 * radius);
          int idx = (this.mSelfShadowVertices.size() + 1) / 2;
          this.mSelfShadowVertices.add(idx, sv);
        }
      }

      // Switch scanXmin as scanXmax for next iteration.
      scanXmax = scanXmin;
    }

    this.mVertices.position(0);
    this.mColors.position(0);
    if (DRAW_TEXTURE) {
      this.mTexCoords.position(0);
    }

    // Add shadow Vertices.
    if (DRAW_SHADOW) {
      this.mShadowColors.position(0);
      this.mShadowVertices.position(0);
      this.mDropShadowCount = 0;

      for (int i = 0; i < this.mDropShadowVertices.size(); ++i) {
        ShadowVertex sv = this.mDropShadowVertices.get(i);
        this.mShadowVertices.put((float) sv.mPosX);
        this.mShadowVertices.put((float) sv.mPosY);
        this.mShadowVertices.put((float) sv.mPosZ);
        this.mShadowVertices.put((float) (sv.mPosX + sv.mPenumbraX));
        this.mShadowVertices.put((float) (sv.mPosY + sv.mPenumbraY));
        this.mShadowVertices.put((float) sv.mPosZ);
        for (int j = 0; j < 4; ++j) {
          double color = SHADOW_OUTER_COLOR[j] + (SHADOW_INNER_COLOR[j] - SHADOW_OUTER_COLOR[j]) * sv.mPenumbraColor;
          this.mShadowColors.put((float) color);
        }
        this.mShadowColors.put(SHADOW_OUTER_COLOR);
        this.mDropShadowCount += 2;
      }
      this.mSelfShadowCount = 0;
      for (int i = 0; i < this.mSelfShadowVertices.size(); ++i) {
        ShadowVertex sv = this.mSelfShadowVertices.get(i);
        this.mShadowVertices.put((float) sv.mPosX);
        this.mShadowVertices.put((float) sv.mPosY);
        this.mShadowVertices.put((float) sv.mPosZ);
        this.mShadowVertices.put((float) (sv.mPosX + sv.mPenumbraX));
        this.mShadowVertices.put((float) (sv.mPosY + sv.mPenumbraY));
        this.mShadowVertices.put((float) sv.mPosZ);
        for (int j = 0; j < 4; ++j) {
          double color = SHADOW_OUTER_COLOR[j] + (SHADOW_INNER_COLOR[j] - SHADOW_OUTER_COLOR[j]) * sv.mPenumbraColor;
          this.mShadowColors.put((float) color);
        }
        this.mShadowColors.put(SHADOW_OUTER_COLOR);
        this.mSelfShadowCount += 2;
      }
      this.mShadowColors.position(0);
      this.mShadowVertices.position(0);
    }
  }

  /**
   * Draws our mesh.
   */
  public synchronized void draw(GL10 gl) {
    // First allocate texture if there is not one yet.
    if (DRAW_TEXTURE && this.mTextureIds == null) {
      // Generate texture.
      this.mTextureIds = new int[1];
      gl.glGenTextures(1, this.mTextureIds, 0);
      // Set texture attributes.
      gl.glBindTexture(GL10.GL_TEXTURE_2D, this.mTextureIds[0]);
      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
      gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
    }
    // If mBitmap != null we have a new texture.
    if (DRAW_TEXTURE && this.mBitmap != null) {
      gl.glBindTexture(GL10.GL_TEXTURE_2D, this.mTextureIds[0]);
      GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, this.mBitmap, 0);
      this.mBitmap.recycle();
      this.mBitmap = null;
    }

    if (DRAW_TEXTURE) {
      gl.glBindTexture(GL10.GL_TEXTURE_2D, this.mTextureIds[0]);
    }

    // Some 'global' settings.
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

    // TODO: Drop shadow drawing is done temporarily here to hide some
    // problems with its calculation.
    if (DRAW_SHADOW) {
      gl.glEnable(GL10.GL_BLEND);
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
      gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
      gl.glColorPointer(4, GL10.GL_FLOAT, 0, this.mShadowColors);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.mShadowVertices);
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, this.mDropShadowCount);
      gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
      gl.glDisable(GL10.GL_BLEND);
    }

    // Enable texture coordinates.
    if (DRAW_TEXTURE) {
      gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, this.mTexCoords);
    }
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.mVertices);

    // Enable color array.
    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    gl.glColorPointer(4, GL10.GL_FLOAT, 0, this.mColors);

    // Draw blank / 'white' front facing vertices.
    gl.glDisable(GL10.GL_TEXTURE_2D);
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, this.mVerticesCountFront);
    // Draw front facing texture.
    // TODO: Decide whether it's really needed to have alpha blending for
    // front facing texture. If not, GL_BLEND isn't needed, possibly
    // increasing performance. The heck, is it needed at all?
    if (DRAW_TEXTURE) {
      gl.glEnable(GL10.GL_BLEND);
      gl.glEnable(GL10.GL_TEXTURE_2D);
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, this.mVerticesCountFront);
      gl.glDisable(GL10.GL_TEXTURE_2D);
      gl.glDisable(GL10.GL_BLEND);
    }
    int backStartIdx = Math.max(0, this.mVerticesCountFront - 2);
    int backCount = this.mVerticesCountFront + this.mVerticesCountBack - backStartIdx;
    // Draw blank / 'white' back facing vertices.
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, backStartIdx, backCount);
    // Draw back facing texture.
    if (DRAW_TEXTURE) {
      gl.glEnable(GL10.GL_BLEND);
      gl.glEnable(GL10.GL_TEXTURE_2D);
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, backStartIdx, backCount);
      gl.glDisable(GL10.GL_TEXTURE_2D);
      gl.glDisable(GL10.GL_BLEND);
    }

    // Disable textures and color array.
    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

    if (DRAW_POLYGON_OUTLINES) {
      gl.glEnable(GL10.GL_BLEND);
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
      gl.glLineWidth(1.0f);
      gl.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.mVertices);
      gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, this.mVerticesCountFront);
      gl.glDisable(GL10.GL_BLEND);
    }

    if (DRAW_CURL_POSITION) {
      gl.glEnable(GL10.GL_BLEND);
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
      gl.glLineWidth(1.0f);
      gl.glColor4f(1.0f, 0.5f, 0.5f, 1.0f);
      gl.glVertexPointer(2, GL10.GL_FLOAT, 0, this.mCurlPositionLines);
      gl.glDrawArrays(GL10.GL_LINES, 0, this.mCurlPositionLinesCount * 2);
      gl.glDisable(GL10.GL_BLEND);
    }

    if (DRAW_SHADOW) {
      gl.glEnable(GL10.GL_BLEND);
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
      gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
      gl.glColorPointer(4, GL10.GL_FLOAT, 0, this.mShadowColors);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.mShadowVertices);
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, this.mDropShadowCount, this.mSelfShadowCount);
      gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
      gl.glDisable(GL10.GL_BLEND);
    }

    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
  }

  /**
   * Calculates intersections for given scan line.
   */
  private Array<Vertex> getIntersections(Array<Vertex> vertices, int[][] lineIndices, double scanX) {
    this.mIntersections.clear();
    // Iterate through rectangle lines each re-presented as a pair of
    // vertices.
    for (int[] lineIndice : lineIndices) {
      Vertex v1 = vertices.get(lineIndice[0]);
      Vertex v2 = vertices.get(lineIndice[1]);
      // Here we expect that v1.mPosX >= v2.mPosX and wont do intersection
      // test the opposite way.
      if (v1.mPosX > scanX && v2.mPosX < scanX) {
        // There is an intersection, calculate coefficient telling 'how
        // far' scanX is from v2.
        double c = (scanX - v2.mPosX) / (v1.mPosX - v2.mPosX);
        Vertex n = this.mTempVertices.remove(0);
        n.set(v2);
        n.mPosX = scanX;
        n.mPosY += (v1.mPosY - v2.mPosY) * c;
        if (DRAW_TEXTURE) {
          n.mTexX += (v1.mTexX - v2.mTexX) * c;
          n.mTexY += (v1.mTexY - v2.mTexY) * c;
        }
        if (DRAW_SHADOW) {
          n.mPenumbraX += (v1.mPenumbraX - v2.mPenumbraX) * c;
          n.mPenumbraY += (v1.mPenumbraY - v2.mPenumbraY) * c;
        }
        this.mIntersections.add(n);
      }
    }
    return this.mIntersections;
  }

  /**
   * Calculates the next highest power of two for a given integer.
   */
  private int getNextHighestPO2(int n) {
    n -= 1;
    n = n | (n >> 1);
    n = n | (n >> 2);
    n = n | (n >> 4);
    n = n | (n >> 8);
    n = n | (n >> 16);
    n = n | (n >> 32);
    return n + 1;
  }

  /**
   * Resets mesh to 'initial' state. Meaning this mesh will draw a plain
   * textured rectangle after call to this method.
   */
  public synchronized void reset() {
    this.mVertices.position(0);
    this.mColors.position(0);
    if (DRAW_TEXTURE) {
      this.mTexCoords.position(0);
    }
    for (int i = 0; i < 4; ++i) {
      this.addVertex(this.mRectangle[i]);
    }
    this.mVerticesCountFront = 4;
    this.mVerticesCountBack = 0;
    this.mVertices.position(0);
    this.mColors.position(0);
    if (DRAW_TEXTURE) {
      this.mTexCoords.position(0);
    }

    this.mDropShadowCount = this.mSelfShadowCount = 0;
  }

  /**
   * Resets allocated texture id forcing creation of new one. After calling this
   * method you most likely want to set bitmap too as it's lost. This method
   * should be called only once e.g GL context is re-created as this method does
   * not release previous texture id, only makes sure new one is requested on
   * next render.
   */
  public synchronized void resetTexture() {
    this.mTextureIds = null;
  }

  /**
   * Sets new texture for this mesh.
   */
  public synchronized void setBitmap(Bitmap bitmap) {
    if (DRAW_TEXTURE) {
      // Bitmap original size.
      int w = bitmap.getWidth();
      int h = bitmap.getHeight();
      // Bitmap size expanded to next power of two. This is done due to
      // the requirement on many devices, texture width and height should
      // be power of two.
      int newW = this.getNextHighestPO2(w);
      int newH = this.getNextHighestPO2(h);
      // Recycle the previous bitmap if it still exists.
      if (this.mBitmap != null) {
        this.mBitmap.recycle();
        this.mBitmap = null;
      }
      // TODO: Is there another way to create a bigger Bitmap and copy
      // original Bitmap to it more efficiently? Immutable bitmap anyone?
      this.mBitmap = Bitmap.createBitmap(newW, newH, bitmap.getConfig());
      Canvas c = new Canvas(this.mBitmap);
      c.drawBitmap(bitmap, 0, 0, null);

      // Recycle the now unused bitmap
      bitmap.recycle();
      bitmap = null;

      // Calculate final texture coordinates.
      float texX = (float) w / newW;
      float texY = (float) h / newH;
      this.mTextureRect.set(0f, 0f, texX, texY);
      if (this.mFlipTexture) {
        this.setTexCoords(texX, 0f, 0f, texY);
      } else {
        this.setTexCoords(0f, 0f, texX, texY);
      }
    }
  }

  /**
   * If true, flips texture sideways.
   */
  public synchronized void setFlipTexture(boolean flipTexture) {
    this.mFlipTexture = flipTexture;

    if (this.mFlipTexture) {
      this.setTexCoords(this.mTextureRect.right, this.mTextureRect.top, this.mTextureRect.left,
          this.mTextureRect.bottom);
    } else {
      this.setTexCoords(this.mTextureRect.left, this.mTextureRect.top, this.mTextureRect.right,
          this.mTextureRect.bottom);
    }

    for (int i = 0; i < 4; ++i) {
      this.mRectangle[i].mAlpha = this.mFlipTexture ? BACKFACE_ALPHA : FRONTFACE_ALPHA;
    }
  }

  /**
   * Update mesh bounds.
   */
  public void setRect(RectF r) {
    this.mRectangle[0].mPosX = r.left;
    this.mRectangle[0].mPosY = r.top;
    this.mRectangle[1].mPosX = r.left;
    this.mRectangle[1].mPosY = r.bottom;
    this.mRectangle[2].mPosX = r.right;
    this.mRectangle[2].mPosY = r.top;
    this.mRectangle[3].mPosX = r.right;
    this.mRectangle[3].mPosY = r.bottom;
  }

  /**
   * Sets texture coordinates to mRectangle vertices.
   */
  private synchronized void setTexCoords(float left, float top, float right, float bottom) {
    this.mRectangle[0].mTexX = left;
    this.mRectangle[0].mTexY = top;
    this.mRectangle[1].mTexX = left;
    this.mRectangle[1].mTexY = bottom;
    this.mRectangle[2].mTexX = right;
    this.mRectangle[2].mTexY = top;
    this.mRectangle[3].mTexX = right;
    this.mRectangle[3].mTexY = bottom;
  }
}
